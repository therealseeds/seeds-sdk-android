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

package ly.count.android.sdk.inappmessaging;

import static ly.count.android.sdk.inappmessaging.Const.AD_EXTRA;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Handler;

public class InAppMessageManager {

	private static HashMap<Long, InAppMessageManager> sRunningAds = new HashMap<Long, InAppMessageManager>();

	private String mPublisherId;
	private String androidAdId;
	private boolean adDoNotTrack;
	private boolean mIncludeLocation;
	private static Context mContext;
	private Thread mRequestThread;
	private Handler mHandler;
	private InAppMessageRequest request = null;
	private InAppMessageListener mListener;
	private boolean mEnabled = true;
	private InAppMessageResponse mResponse;
	private String interstitialRequestURL;
	private boolean alreadyRequestedInterstitial;
	private boolean requestedHorizontalAd;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private static final String MOB_FOX_PUB_ID = "86e0aa6e7fbd2cdb02ec15e338d6b722";
	private static final String MOB_FOX_AD_URL = "http://my.mobfox.com/request.php";

	public static InAppMessageManager getAdManager(InAppMessageResponse ad) {
		InAppMessageManager inAppMessageManager = sRunningAds.remove(ad.getTimestamp());
		if (inAppMessageManager == null) {
			Log.d("Cannot find InAppMessageManager with running ad:" + ad.getTimestamp());
		}
		return inAppMessageManager;
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

	public InAppMessageManager(Context ctx) throws IllegalArgumentException {
		this(ctx, MOB_FOX_AD_URL, MOB_FOX_PUB_ID, false);
	}


	public InAppMessageManager(Context ctx, final String interstitialRequestURL, final String publisherId, final boolean includeLocation) throws IllegalArgumentException {
		Util.prepareAndroidAdId(ctx);
		InAppMessageManager.setmContext(ctx);
		this.interstitialRequestURL = interstitialRequestURL;
		this.mPublisherId = publisherId;
		this.mIncludeLocation = includeLocation;
		this.mRequestThread = null;
		this.mHandler = new Handler();
		initialize();
	}

	public void setListener(InAppMessageListener listener) {
		this.mListener = listener;
	}

	public void requestInAppMessage() {
		requestInAppMessageInternal(false);
	}

	private void requestInAppMessageInternal(boolean keepFlags) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		if (!keepFlags) {
			alreadyRequestedInterstitial = false;
		}

		if (mRequestThread == null) {
			Log.d("Requesting InAppMessage (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;

			mRequestThread = new Thread(new Runnable() {
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
						RequestGeneralInAppMessage requestAd = new RequestGeneralInAppMessage();
						if (!alreadyRequestedInterstitial) {
							request = getInterstitialRequest();
							alreadyRequestedInterstitial = true;
						} else {
							Log.d("Already requested interstitial");
							notifyNoAdFound();
							mRequestThread = null;
							return;
						}

						mResponse = requestAd.sendRequest(request);
						if (mResponse.getType() == Const.NO_AD) {
							 if (!alreadyRequestedInterstitial) {
								request = getInterstitialRequest();
								alreadyRequestedInterstitial = true;
								mResponse = requestAd.sendRequest(request);
							}
						}
						
						if (mResponse.getType() == Const.TEXT ||  mResponse.getType() == Const.IMAGE) {
							notifyAdLoaded(mResponse);
						} else if (mResponse.getType() == Const.NO_AD) {
							Log.d("response NO AD received");
							notifyNoAdFound();
						} else {
							notifyNoAdFound();
						}
					} catch (Throwable t) {
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
			});
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

	public void setInterstitialRequestURL(String requestURL) {
		this.interstitialRequestURL = requestURL;
	}

	public void requestInAppMessage(final InputStream xml) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		alreadyRequestedInterstitial = false;


		if (mRequestThread == null) {
			Log.d("Requesting InAppMessage (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;
			mRequestThread = new Thread(new Runnable() {
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
						RequestGeneralInAppMessage requestAd = new RequestGeneralInAppMessage(xml);
						request = getInterstitialRequest();
						mResponse = requestAd.sendRequest(request);

						if (mResponse.getType() == Const.NO_AD) {
							if (!alreadyRequestedInterstitial) {
								request = getInterstitialRequest();
								alreadyRequestedInterstitial = true;
								mResponse = requestAd.sendRequest(request);
							}
						}

						if (mResponse.getType() != Const.NO_AD) {
							Log.d("response OK received");
							notifyAdLoaded(mResponse);
						} 
					} catch (Throwable t) {
						mResponse = new InAppMessageResponse();
						mResponse.setType(Const.AD_FAILED);
						notifyNoAdFound();
					}
					Log.d("finishing ad request thread");
					mRequestThread = null;
				}
			});
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

	public boolean isInAppMessageLoaded() {
		return (mResponse != null);
	}

	public void requestInAppMessageAndShow(long timeout) {
		InAppMessageListener l = mListener;

		mListener = null;
		requestInAppMessage();
		long now = System.currentTimeMillis();
		long timeoutTime = now + timeout;
		while ((!isInAppMessageLoaded()) && (now < timeoutTime)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			now = System.currentTimeMillis();
		}
		mListener = l;
		showInAppMessage();
	}

	public void showInAppMessage() {

		if (((mResponse == null) || (mResponse.getType() == Const.NO_AD) || (mResponse.getType() == Const.AD_FAILED))) {
			notifyAdShown(mResponse, false);
			return;
		}
		InAppMessageResponse ad = mResponse;
		boolean result = false;
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

	private void initialize() throws IllegalArgumentException {
		Log.d("InAppMessage SDK Version:" + Const.VERSION);

		this.androidAdId = Util.getAndroidAdId();
		this.adDoNotTrack = Util.hasAdDoNotTrack();

		if ((mPublisherId == null) || (mPublisherId.length() == 0)) {
			Log.e("Publisher Id cannot be null or empty");
			throw new IllegalArgumentException("User Id cannot be null or empty");
		}

		Log.d("InAppMessageManager Publisher Id:" + mPublisherId + " Advertising Id:" + androidAdId);
		mEnabled = (Util.getMemoryClass(getContext()) > 16);
	}

	private void notifyNoAdFound() {
		if (mListener != null) {
			Log.d("No ad found.");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.noInAppMessageFound();
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdLoaded(final InAppMessageResponse ad) {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.inAppMessageLoadSucceeded(ad);
				}
			});
		}
	}

	private void notifyAdClicked() {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.inAppMessageClicked();
				}
			});
		}
	}

	private void notifyAdShown(final InAppMessageResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("InAppMessage Shown. Result:" + ok);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.inAppMessageShown(ad, ok);
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdClose(final InAppMessageResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("InAppMessage Close. Result:" + ok);
			mHandler.post(new Runnable() {
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
			request.setAndroidAdId(androidAdId);
			request.setAdDoNotTrack(adDoNotTrack);
			this.request.setPublisherId(this.mPublisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(mContext));
			this.request.setUserAgent2(Util.buildUserAgent());
		}
		Location location = null;
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setKeywords(keywords);

		if (this.mIncludeLocation)
			location = Util.getLocation(mContext);
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
			this.request.setAdspaceHeight(320);
			this.request.setAdspaceWidth(480);
		} else {
			requestedHorizontalAd = false;
			this.request.setAdspaceHeight(480);
			this.request.setAdspaceWidth(320);
		}

		this.request.setAdspaceStrict(false);

		request.setConnectionType(Util.getConnectionType(getContext()));
		request.setIpAddress(Util.getLocalIpAddress());
		request.setTimestamp(System.currentTimeMillis());

		this.request.setRequestURL(interstitialRequestURL);
		return this.request;
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

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

}
