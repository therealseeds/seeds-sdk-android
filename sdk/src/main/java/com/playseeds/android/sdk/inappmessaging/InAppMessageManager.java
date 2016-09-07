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
import java.io.InputStream;
import java.io.StringReader;
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
import android.os.Looper;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
	private Thread mRequestThread;
	private InAppMessageListener mListener;
	private InAppMessageResponse mResponse;
	private String interstitialRequestURL;
	private String mDeviceID;
	private DeviceId.Type mIdMode;
	private boolean alreadyRequestedInterstitial;
	private boolean requestedHorizontalAd;
	private HashMap<String, String> segmentation;
	private String requestedMessageId;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private boolean doNotShow = false;
	private InAppMessageRequest request = null;
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
		sharedInstance().mRequestThread = null;
	}

	public static void closeRunningInAppMessage(InAppMessageResponse ad, boolean result) {
		InAppMessageManager inAppMessageManager = sRunningAds.remove(ad.getTimestamp());

		if (inAppMessageManager == null) {
			Log.d("Cannot find InAppMessageManager with running ad:" + ad.getTimestamp());
			return;
		}
		Log.d("Notify closing event to InAppMessageManager with running ad:" + ad.getTimestamp());
		inAppMessageManager.notifyAdClose(ad, result);
	}

	public static void notifyInAppMessageClick(InAppMessageResponse ad) {
		InAppMessageManager inAppMessageManager = sRunningAds.get(ad.getTimestamp());
		if (inAppMessageManager != null) {
			inAppMessageManager.notifyAdClicked(ad);
		}
	}

	public void requestInAppMessage(String messageId) {
		requestInAppMessageInternal(messageId, false);
	}

	private void requestInAppMessageInternal(final String messageId, boolean keepFlags) {

		if (!keepFlags) {
			alreadyRequestedInterstitial = false;
		}

		if (mRequestThread == null) {
			Log.d("Requesting InAppMessage (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;
			mRequestThread = getRequestThread(messageId);
			mRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					mResponse = new InAppMessageResponse();
					mResponse.setType(Const.AD_FAILED);
					mResponse.setMessageIdRequested(requestedMessageId);
					Log.e("Handling exception in ad request thread", ex);
					mRequestThread = null;
				}
			});

			mRequestThread.start();
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
					if (!alreadyRequestedInterstitial) {
						request = getInterstitialRequest(messageId);
						alreadyRequestedInterstitial = true;
					} else {
						Log.d("Already requested interstitial");
						notifyNoAdFound();
						mRequestThread = null;
						return;
					}

					try {
						mResponse = requestAd.obtainInAppMessage(request);
						mResponse.setMessageIdRequested(requestedMessageId);
					} catch (Exception e) {
						File cachedInAppMessageFile = new File(mContext.getCacheDir(),
								URLEncoder.encode(request.countlyUriToString(), "UTF-8"));
						if (cachedInAppMessageFile.exists()) {
							BufferedReader cacheReader = new BufferedReader(new FileReader(cachedInAppMessageFile));
							mResponse = new Gson().fromJson(cacheReader, InAppMessageResponse.class);
							mResponse.setMessageIdRequested(requestedMessageId);
							cacheReader.close();
						}
					}
					if (mResponse.getType() == Const.NO_AD) {
						if (!alreadyRequestedInterstitial) {
							request = getInterstitialRequest(messageId);
							alreadyRequestedInterstitial = true;
							mResponse = requestAd.obtainInAppMessage(request);
							mResponse.setMessageIdRequested(requestedMessageId);
						}
					}

					boolean fakeBillingService = false;

					if (mResponse.getProductId() != null && (mBillingService != null || fakeBillingService)) {
						try {
							ArrayList<String> productsList = new ArrayList<String>();
							productsList.add(mResponse.getProductId());
							Bundle skuBundle = new Bundle();
							skuBundle.putStringArrayList("ITEM_ID_LIST", productsList);

							Bundle productsDetails;
							if (fakeBillingService) {
								productsDetails = new Bundle();
								productsDetails.putInt("RESPONSE_CODE", 0);
								ArrayList<String> detailsList = new ArrayList<String>();
								detailsList.add("{" +
										"\"productId\":\"" + mResponse.getProductId() + "\"," +
										"\"price\":\"$9.99\"," +
										"\"price_amount_micros\":\"9990000\"," +
										"\"price_currency_code\":\"USD\"}");
								productsDetails.putStringArrayList("DETAILS_LIST", detailsList);
							} else {
								productsDetails = mBillingService.getSkuDetails(3,
										mContext.getPackageName(), "inapp", skuBundle);
							}

							if (productsDetails.getInt("RESPONSE_CODE", -1) == 0) {
								ArrayList<String> productsDetailsCollection
										= productsDetails.getStringArrayList("DETAILS_LIST");
								Log.i("detailsCollection = " + productsDetailsCollection);

								for (String productDetails : productsDetailsCollection) {
									JsonObject jsonProductDetails = new JsonParser().parse(productDetails).getAsJsonObject();

									String productId = jsonProductDetails.get("productId").getAsString();
									if (!mResponse.getProductId().equals(productId))
										continue;

									String formattedPrice = jsonProductDetails.get("price").getAsString();
									mResponse.setFormattedPrice(formattedPrice);

									String exactPrice = jsonProductDetails.get("price_amount_micros").getAsString();
									mResponse.setExactPrice(exactPrice);

									String currencyCode = jsonProductDetails.get("price_currency_code").getAsString();
									mResponse.setPriceCurrencyCode(currencyCode);

									break;
								}
							}
						} catch (Exception e) {
							Log.e("BillingService", e);
						}
					}
					String text = mResponse.getText();
					text = text.replace("%{LocalizedPrice}",
							mResponse.getFormattedPrice() != null
									? mResponse.getFormattedPrice()
									: "BUY");
					mResponse.setText(text);

					//TODO: remove debug code
					Log.i("mResponse is: " + mResponse);

					if (mResponse.getType() == Const.TEXT || mResponse.getType() == Const.IMAGE) {
						notifyAdLoaded(mResponse);

						BufferedWriter cacheWriter = null;
						try {
							File cachedInAppMessageFile = new File(mContext.getCacheDir(),
									URLEncoder.encode(request.countlyUriToString(), "UTF-8"));
							cacheWriter = new BufferedWriter(new FileWriter(cachedInAppMessageFile));
							cacheWriter.write(new Gson().toJson(mResponse));
						} catch (Exception e) {
							Log.e("Cache", e);
						} finally {
							try {
								// Close the writer regardless of what happens...
								cacheWriter.close();
							} catch (Exception e) {
							}
						}
					} else if (mResponse.getType() == Const.NO_AD) {
						Log.d("response NO AD received");
						notifyNoAdFound();
					} else {
						notifyNoAdFound();
					}
				} catch (Throwable t) {
					Log.e("ad request failed", t);

					if (!alreadyRequestedInterstitial) {
						mRequestThread = null;
						requestInAppMessageInternal(messageId, true);
					} else {
						mResponse = new InAppMessageResponse();
						mResponse.setType(Const.AD_FAILED);
						mResponse.setMessageIdRequested(requestedMessageId);
						notifyNoAdFound();
					}
				}
				Log.d("finishing ad request thread");
				mRequestThread = null;
			}
		});
	}

	public void showInAppMessage(String messageId, String messageContext) {
		if (((mResponse == null)
				|| (mResponse.getType() == Const.NO_AD)
				|| (mResponse.getType() == Const.AD_FAILED))
				|| doNotShow) {
			notifyAdShown(mResponse, false);
			return;
		}

		if (messageId != null && !messageId.equals(requestedMessageId)) {
			notifyAdShown(mResponse, false);
			return;
		}

		InAppMessageResponse ad = mResponse;
		boolean result = false;
		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				ad.setHorizontalOrientationRequested(requestedHorizontalAd);
				ad.setMessageIdRequested(requestedMessageId);
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
		if (mResponse == null)
			return false;
		return messageId == null || messageId.equals(mResponse.getMessageIdRequested());
	}

	private void notifyNoAdFound() {
		if (mListener != null) {
			Log.d("No ad found " + requestedMessageId);
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.noInAppMessageFound(requestedMessageId);
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdClicked(final InAppMessageResponse ad) {
		if (mListener != null) {
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.inAppMessageClicked(ad.getMessageIdRequested());
				}
			});
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
		this.mResponse = null;

		if (ok) {
			setSegmentation();
			Seeds.sharedInstance().recordEvent("message shown", segmentation, 1);
			android.util.Log.d("Main", "message shown: " + segmentation);
		}
		// sanity check
		Seeds.sharedInstance().setAdClicked(false);
	}

	private void notifyAdClose(final InAppMessageResponse ad, final boolean ok) {
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
		if (this.request == null) {
			this.request = new InAppMessageRequest();

			request.setAdDoNotTrack(adDoNotTrack);
			request.setUserAgent(Util.getDefaultUserAgentString());
			request.setUserAgent2(Util.buildUserAgent());
		}
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
		requestedMessageId = messageId;

		this.request.setAdspaceStrict(false);

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