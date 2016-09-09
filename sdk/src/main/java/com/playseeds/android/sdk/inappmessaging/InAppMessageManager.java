/*
 *		Copyright 2015 MobFox
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *		Unless required by applicable law or agreed to in writing, software
 *		distributed under the License is distributed on an "AS IS" BASIS,
 *		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *		See the License for the specific language governing permissions and
 *		limitations under the License.
 *
 *		Changes: 	removed video, MRAID and custom ad-specific code
 *					renamed from AdManager
 */

package com.playseeds.android.sdk.inappmessaging;

import static com.playseeds.android.sdk.inappmessaging.Const.AD_EXTRA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;

import android.os.Bundle;
import android.os.Handler;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.playseeds.android.sdk.DeviceId;
import com.playseeds.android.sdk.Seeds;

public class InAppMessageManager {
	private String mAppKey;
	private boolean adDoNotTrack;
	private boolean mIncludeLocation;
	private static Context mContext;
	private static IInAppBillingService mBillingService;
	private HashMap<String, Thread> mRequestThreads;
	private InAppMessageListener mListener;
	private HashMap<String, InAppMessageResponse> mResponses;
	private String interstitialRequestURL;
	private String mDeviceID;
	private DeviceId.Type mIdMode;
	private boolean alreadyRequestedInterstitial;
	private boolean requestedHorizontalAd;
	private HashMap<String, String> segmentation;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private boolean doNotShow = false;
	private HashMap<String, InAppMessageRequest> mRequests = null;
	private static HashMap<Long, InAppMessageManager> sRunningAds = new HashMap<>();

	/**
	 * Private Constructor
	 */
	private InAppMessageManager() {
	}

	/**
	 * Returns the InAppMessageManager singleton.
	 */
	public static InAppMessageManager sharedInstance() {
		return SingletonHolder.instance;
	}

	// see http://stackoverflow.com/questions/7048198/thread-safe-singletons-in-java
	private static class SingletonHolder {
		static final InAppMessageManager instance = new InAppMessageManager();
	}

	public void init(Context context, IInAppBillingService billingService, final String interstitialRequestURL, final String appKey, final String deviceID, final DeviceId.Type idMode) {
		Util.prepareAndroidAdId(context);
		InAppMessageManager.setmContext(context);
		InAppMessageManager.setmBillingService(billingService);
		this.interstitialRequestURL = interstitialRequestURL;
		mAppKey = appKey;
		mDeviceID= deviceID;
		mIdMode = idMode;
		mContext = context;
		mResponses = new HashMap<>();
		mRequestThreads = new HashMap<>();
		mRequests = new HashMap<>();
	}

	public static void closeRunningInAppMessage(InAppMessageResponse ad, boolean result, boolean wasClicked) {
		InAppMessageManager inAppMessageManager = sRunningAds.remove(ad.getTimestamp());

		if (inAppMessageManager == null) {
			Log.d("Cannot find InAppMessageManager with running ad:" + ad.getTimestamp());
			return;
		}

		if (!wasClicked) {
			Log.d("Notify dismissal event to InAppMessageManager with running ad:" + ad.getTimestamp());
			inAppMessageManager.notifyAdDismiss(ad, result);
		}
	}

	public static void notifyInAppMessageClick(InAppMessageResponse ad) {
		InAppMessageManager inAppMessageManager = sRunningAds.get(ad.getTimestamp());
		if (inAppMessageManager != null) {
			inAppMessageManager.notifyAdClicked(ad);
		}
	}

	public void requestInAppMessage(String messageId) {
		requestInAppMessageInternal(messageId);
	}


	private void requestInAppMessageInternal(final String messageId) {
		if (mRequestThreads.get(messageId) == null) {
			Log.d("Requesting InAppMessage (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponses.remove(messageId);
			mRequestThreads.put(messageId, getRequestThread(messageId));
			mRequestThreads.get(messageId).setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					InAppMessageResponse mResponse = new InAppMessageResponse();
					mResponse.setType(Const.AD_FAILED);
					mResponse.setMessageId(messageId);
					mResponses.put(messageId, mResponse);
					Log.e("Handling exception in ad request thread", ex);
					mRequestThreads.remove(messageId);
				}
			});

			mRequestThreads.get(messageId).start();
		} else {
			Log.w("Request thread already running");
		}
	}

	private Thread getRequestThread(final String messageId) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				while (ResourceManager.isDownloading()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
				Log.d("starting request thread");
				try {
					GeneralInAppMessageProvider requestAd = new GeneralInAppMessageProvider();
					mRequests.put(messageId, getInterstitialRequest(messageId));

					try {
						mResponses.put(messageId, requestAd.obtainInAppMessage(mRequests.get(messageId)));
						mResponses.get(messageId).setMessageId(messageId);
					} catch (Exception e) {
						File cachedInAppMessageFile = new File(mContext.getCacheDir(),
								URLEncoder.encode(mRequests.get(messageId).countlyUriToString(), "UTF-8"));
						if (cachedInAppMessageFile.exists()) {
							BufferedReader cacheReader = new BufferedReader(new FileReader(cachedInAppMessageFile));
							mResponses.put(messageId, new Gson().fromJson(cacheReader, InAppMessageResponse.class));
							cacheReader.close();
						}
					}
					if (mResponses.get(messageId).getType() == Const.NO_AD) {
						if (!alreadyRequestedInterstitial) {
							mRequests.put(messageId, getInterstitialRequest(messageId));
							alreadyRequestedInterstitial = true;
							mResponses.put(messageId, requestAd.obtainInAppMessage(mRequests.get(messageId)));
						}
					}

					if (mResponses.get(messageId).getProductId() != null && (mBillingService != null)) {
						try {
							ArrayList<String> productsList = new ArrayList<String>();
							productsList.add(mResponses.get(messageId).getProductId());
							Bundle skuBundle = new Bundle();
							skuBundle.putStringArrayList("ITEM_ID_LIST", productsList);

							Bundle productsDetails;

							productsDetails = mBillingService.getSkuDetails(3,
									mContext.getPackageName(), "inapp", skuBundle);

							if (productsDetails.getInt("RESPONSE_CODE", -1) == 0) {
								ArrayList<String> productsDetailsCollection
										= productsDetails.getStringArrayList("DETAILS_LIST");
								Log.i("detailsCollection = " + productsDetailsCollection);

								for (String productDetails : productsDetailsCollection) {
									JsonObject jsonProductDetails = new JsonParser().parse(productDetails).getAsJsonObject();

									String productId = jsonProductDetails.get("productId").getAsString();
									if (!mResponses.get(messageId).getProductId().equals(productId))
										continue;

									String formattedPrice = jsonProductDetails.get("price").getAsString();
									mResponses.get(messageId).setFormattedPrice(formattedPrice);

									String exactPrice = jsonProductDetails.get("price_amount_micros").getAsString();
									mResponses.get(messageId).setExactPrice(exactPrice);

									String currencyCode = jsonProductDetails.get("price_currency_code").getAsString();
									mResponses.get(messageId).setPriceCurrencyCode(currencyCode);

									break;
								}
							}
						} catch (Exception e) {
							Log.e("BillingService", e);
						}
					}
					String text = mResponses.get(messageId).getText();
					text = text.replace("%{LocalizedPrice}",
							mResponses.get(messageId).getFormattedPrice() != null
									? mResponses.get(messageId).getFormattedPrice()
									: "BUY");
					mResponses.get(messageId).setText(text);

					//TODO: remove debug code
					Log.i("mResponse is: " + mResponses.get(messageId));

					if (mResponses.get(messageId).getType() == Const.TEXT ||
							mResponses.get(messageId).getType() == Const.IMAGE) {

						notifyAdLoaded(mResponses.get(messageId));

						BufferedWriter cacheWriter = null;
						try {
							File cachedInAppMessageFile = new File(mContext.getCacheDir(),
									URLEncoder.encode(mRequests.get(messageId).countlyUriToString(), "UTF-8"));
							cacheWriter = new BufferedWriter(new FileWriter(cachedInAppMessageFile));
							cacheWriter.write(new Gson().toJson(mResponses.get(messageId)));
						} catch (Exception e) {
							Log.e("Cache", e);
						} finally {
							try {
								// Close the writer regardless of what happens...
								cacheWriter.close();
							} catch (Exception e) {
							}
						}
					} else if (mResponses.get(messageId).getType() == Const.NO_AD) {
						Log.d("response NO AD received");
						notifyNoAdFound(messageId);
					} else {
						notifyNoAdFound(messageId);
					}
				} catch (Throwable t) {
					Log.e("ad request failed", t);

					if (!alreadyRequestedInterstitial) {
						mRequestThreads.remove(messageId);
						requestInAppMessageInternal(messageId);
					} else {
						mResponses.put(messageId, new InAppMessageResponse());
						mResponses.get(messageId).setType(Const.AD_FAILED);
						mResponses.get(messageId).setMessageId(messageId);
						notifyNoAdFound(messageId);
					}
				}
				Log.d("finishing ad request thread");
				mRequestThreads.remove(messageId);
			}
		});
	}

	public void showInAppMessage(String messageId, String messageContext) {
		InAppMessageResponse mResponse = mResponses.get(messageId);

		if (((mResponse == null)
				|| (mResponse.getType() == Const.NO_AD)
				|| (mResponse.getType() == Const.AD_FAILED))
				|| doNotShow) {
			notifyAdShown(mResponse, false);
			return;
		}

		if (messageId != null && !messageId.equals(mResponse.getMessageIdRequested())) {
			notifyAdShown(mResponse, false);
			return;
		}

		InAppMessageResponse ad = mResponse;
		boolean result = false;
		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				ad.setHorizontalOrientationRequested(requestedHorizontalAd);
				ad.setMessageId(messageId);
				Log.v("Showing InAppMessage:" + ad);

				Intent intent = new Intent(getContext(), RichMediaActivity.class);
				intent.putExtra(AD_EXTRA, ad);
				getContext().startActivity(intent);

				result = true;
				sRunningAds.put(ad.getTimestamp(), this);
			} else {
				Log.d("No network available. Cannot show InAppMessage.");
			}
		} catch (Exception e) {
			Log.e("Unknown exception when showing InAppMessage", e);
		} finally {
			if (result)
				Seeds.sharedInstance().setMessageContext(messageContext);
			notifyAdShown(ad, result);
		}
	}

	public boolean isInAppMessageLoaded(String messageId) {
		InAppMessageResponse mResponse = mResponses.get(messageId);

		if (mResponse == null
            || (mResponse.getType() == Const.NO_AD)
            || (mResponse.getType() == Const.AD_FAILED))
			return false;

		return messageId == null || messageId.equals(mResponse.getMessageIdRequested());
	}

	private void notifyNoAdFound(final String messageId) {
		if (mListener != null) {
			Log.d("No ad found " + messageId);
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.noInAppMessageFound(messageId);
				}
			});
		}
		this.mResponses.put(messageId, null);
	}

	private void notifyAdClicked(final InAppMessageResponse ad) {
		// Here the dynamic inAppMessageClickedWithDynamicPrice
		String linkUrl = ad.getSeedsLinkUrl();

		if (mListener != null) {
			if (linkUrl != null && linkUrl.contains("/price/")) {
				final Double price = Double.parseDouble(linkUrl.substring(linkUrl.lastIndexOf('/') + 1));
				sendNotification(new Runnable() {
					@Override
					public void run() {
						mListener.inAppMessageClickedWithDynamicPrice(ad.getMessageIdRequested(), price);
					}
				});
			} else {
				sendNotification(new Runnable() {
					@Override
					public void run() {
						mListener.inAppMessageClicked(ad.getMessageIdRequested());
					}
				});
			}
		}

		setSegmentation();
		Seeds.sharedInstance().recordEvent("message clicked", segmentation, 1);
		Seeds.sharedInstance().setAdClicked(true);
		android.util.Log.d("Main", "message shown: " + segmentation);
	}

	private void notifyAdLoaded(final InAppMessageResponse ad) {
		if (mListener != null) {
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.inAppMessageLoadSucceeded(ad.getMessageIdRequested());
				}
			});
		}
	}

	private void notifyAdShown(final InAppMessageResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("InAppMessage Shown. Result:" + ok);
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.inAppMessageShown(ad.getMessageIdRequested(), ok);
				}
			});
		}

		// mResponses.remove(ad.getMessageIdRequested());

		if (ok) {
			setSegmentation();
			Seeds.sharedInstance().recordEvent("message shown", segmentation, 1);
			android.util.Log.d("Main", "message shown: " + segmentation);
		}
		// sanity check
		Seeds.sharedInstance().setAdClicked(false);
	}

	private void notifyAdDismiss(final InAppMessageResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("InAppMessage Close. Result:" + ok);
			sendNotification(new Runnable() {
				@Override
				public void run() {
					// TODO: Trigger this only when the interstitial is being dismissed
					mListener.inAppMessageDismissed(ad.getMessageIdRequested());
				}
			});
		}
	}

	private InAppMessageRequest getInterstitialRequest(String messageId) {
		if (this.mRequests.get(messageId) == null) {
			this.mRequests.put(messageId, new InAppMessageRequest());

			mRequests.get(messageId).setAdDoNotTrack(adDoNotTrack);
			mRequests.get(messageId).setUserAgent(Util.getDefaultUserAgentString());
			mRequests.get(messageId).setUserAgent2(Util.buildUserAgent());
		}

		InAppMessageRequest request = this.mRequests.get(messageId);

		Location location = null;

		if (this.mIncludeLocation) {
			location = Util.getLocation(mContext);
		}

		if (location != null) {
			Log.d("location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			request.setLatitude(location.getLatitude());
			request.setLongitude(location.getLongitude());
		} else {
			request.setLatitude(0.0);
			request.setLongitude(0.0);
		}

		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			requestedHorizontalAd = true;
		} else {
			requestedHorizontalAd = false;
		}

		request.setAdspaceStrict(false);

		request.setConnectionType(Util.getConnectionType(getContext()));
		request.setIpAddress(Util.getLocalIpAddress());
		request.setTimestamp(System.currentTimeMillis());
		request.setRequestURL(interstitialRequestURL);
		request.setOrientation(getOrientation());
		request.setAppKey(mAppKey);
		request.setDeviceId(mDeviceID);
		request.setIdMode(mIdMode);
		request.setMessageId(messageId);
		return request;
	}

	public void showInAppMessage() {
		InAppMessageResponse mResponse = mResponses.get(null);
		InAppMessageResponse ad = mResponse;

		boolean result = false;

		if (((mResponse == null)
				|| (mResponse.getType() == Const.NO_AD)
				|| (mResponse.getType() == Const.AD_FAILED))
				|| doNotShow) {
			notifyAdShown(mResponse, false);
			return;
		}

		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				ad.setHorizontalOrientationRequested(requestedHorizontalAd);
				Log.v("Showing InAppMessage:" + ad);

				Intent intent = new Intent(getContext(), RichMediaActivity.class);
				intent.putExtra(AD_EXTRA, ad);
				getContext().startActivity(intent);

				result = true;
				sRunningAds.put(ad.getTimestamp(), this);
			} else {
				Log.d("No network available. Cannot show InAppMessage.");
			}
		} catch (Exception e) {
			Log.e("Unknown exception when showing InAppMessage", e);
		} finally {
			notifyAdShown(ad, result);
		}
	}

	/**
	 * This method handles sending notifications to the listeners
	 */
	private void sendNotification(Runnable runnable) {
		// added for testing purposes
		if (mContext.getPackageName().equals("com.playseeds.android.sdk")) {
			new Thread(runnable).start();
		}

		Handler mainHandler = new Handler(mContext.getMainLooper());
		mainHandler.post(runnable);
	}

	public void setListener(InAppMessageListener listener) {
		this.mListener = listener;
	}

	protected void setRunningAds(HashMap<Long, InAppMessageManager> ads) {
		sRunningAds = ads;
	}

	private String getOrientation() {
		if (requestedHorizontalAd) {
			return "landscape";
		} else {
			return "portrait";
		}
	}

	private Context getContext() {
		return getmContext();
	}

	private static Context getmContext() {
		return mContext;
	}

	private static void setmContext(Context mContext) {
		InAppMessageManager.mContext = mContext;
	}

	private static void setmBillingService(IInAppBillingService mBillingService) {
		InAppMessageManager.mBillingService = mBillingService;
	}

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void setSegmentation() {
		segmentation = new HashMap<>();
		segmentation.put("message", Seeds.sharedInstance().getMessageVariantName());
		segmentation.put("context", Seeds.sharedInstance().getMessageContext());
	}

	public void doNotShow(boolean doNotShow) {
		this.doNotShow = doNotShow;
	}
}