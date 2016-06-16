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
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;

import com.google.gson.Gson;
import com.playseeds.android.sdk.DeviceId;
import com.playseeds.android.sdk.Seeds;

public class InAppMessageManager {
	private String mAppKey;
	private boolean adDoNotTrack;
	private boolean mIncludeLocation;
	private static Context mContext;
	private Thread mRequestThread;
	private InAppMessageListener mListener;
	private InAppMessageResponse mResponse;
	private String interstitialRequestURL;
	private String mDeviceID;
	private DeviceId.Type mIdMode;
	private boolean alreadyRequestedInterstitial;
	private boolean requestedHorizontalAd;
	private HashMap<String, String> segmentation;

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

	public void init(Context context, final String interstitialRequestURL, final String appKey, final String deviceID, final DeviceId.Type idMode) {
		Util.prepareAndroidAdId(context);
		InAppMessageManager.setmContext(context);
		this.interstitialRequestURL = interstitialRequestURL;
		mAppKey = appKey;
		mDeviceID= deviceID;
		mIdMode = idMode;
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
			inAppMessageManager.notifyAdClicked();
		}
	}

	public void requestInAppMessage() {
		requestInAppMessageInternal(false);
	}

	private void requestInAppMessageInternal(boolean keepFlags) {
		if (!keepFlags) {
			alreadyRequestedInterstitial = false;
		}

		if (mRequestThread == null) {
			Log.d("Requesting InAppMessage (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;

			mRequestThread = getRequestThread();
			mRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					mResponse = new InAppMessageResponse();
					mResponse.setType(Const.AD_FAILED);
					Log.e("Handling exception in ad request thread", ex);
					mRequestThread = null;
				}
			});
			mRequestThread.start();
		} else {
			Log.w("Request thread already running");
		}
	}

	private Thread getRequestThread() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (ResourceManager.isDownloading()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Log.d("starting request thread");

				try {
					GeneralInAppMessageProvider requestAd = new GeneralInAppMessageProvider();
					if (!alreadyRequestedInterstitial) {
						request = getInterstitialRequest();
						alreadyRequestedInterstitial = true;
					} else {
						Log.d("Already requested interstitial");
						notifyNoAdFound();
						mRequestThread = null;
						return;
					}

					try {
						mResponse = requestAd.obtainInAppMessage(request);
					} catch (Exception e) {
						File cachedInAppMessageFile = new File(mContext.getCacheDir(),
								URLEncoder.encode(request.countlyUriToString(), "UTF-8"));
						if (cachedInAppMessageFile.exists()) {
							BufferedReader cacheReader = new BufferedReader(new FileReader(cachedInAppMessageFile));
							mResponse = new Gson().fromJson(cacheReader, InAppMessageResponse.class);
							cacheReader.close();
						}
					}
					if (mResponse.getType() == Const.NO_AD) {
						if (!alreadyRequestedInterstitial) {
							request = getInterstitialRequest();
							alreadyRequestedInterstitial = true;
							mResponse = requestAd.obtainInAppMessage(request);
						}
					}

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
								assert cacheWriter != null;
								cacheWriter.close();
							} catch (Exception e) {
								e.printStackTrace();
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
						requestInAppMessageInternal(true);
					} else {
						mResponse = new InAppMessageResponse();
						mResponse.setType(Const.AD_FAILED);
						notifyNoAdFound();
					}
				}
				Log.d("finishing ad request thread");
				mRequestThread = null;
			}
		};

		return new Thread(runnable);
	}

	public boolean isInAppMessageLoaded() {
		return (mResponse != null);
	}

	private void notifyNoAdFound() {
		if (mListener != null) {

			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.noInAppMessageFound();
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdClicked() {
		if (mListener != null) {
			sendNotification(new Runnable() {
				@Override
				public void run() {
					mListener.inAppMessageClicked();
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
					mListener.inAppMessageLoadSucceeded(ad);
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
					mListener.inAppMessageShown(ad, ok);
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
					mListener.inAppMessageClosed(ad, ok);
				}
			});
		}

	}

	private InAppMessageRequest getInterstitialRequest() {
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

		this.request.setAdspaceStrict(false);

		request.setConnectionType(Util.getConnectionType(getContext()));
		request.setIpAddress(Util.getLocalIpAddress());
		request.setTimestamp(System.currentTimeMillis());
		request.setRequestURL(interstitialRequestURL);
		request.setOrientation(getOrientation());
		request.setAppKey(mAppKey);
		request.setDeviceId(mDeviceID);
		request.setIdMode(mIdMode);
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
		new Thread(runnable).start();
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

	public void setSegmentation() {
		segmentation = new HashMap<>();
		segmentation.put("message", Seeds.sharedInstance().getMessageVariantName());
	}

	public void doNotShow(boolean doNotShow) {
		this.doNotShow = doNotShow;
	}
}
