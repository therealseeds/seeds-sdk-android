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

public class AdManager {

	private static HashMap<Long, AdManager> sRunningAds = new HashMap<Long, AdManager>();

	private String mPublisherId;
	private String androidAdId;
	private boolean adDoNotTrack;
	private boolean mIncludeLocation;
	private static Context mContext;
	private Thread mRequestThread;
	private Handler mHandler;
	private AdRequest request = null;
	private AdListener mListener;
	private boolean mEnabled = true;
	private AdResponse mResponse;
	private String interstitialRequestURL;
	private boolean alreadyRequestedInterstitial;
	private boolean requestedHorizontalAd;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private static final String MOB_FOX_PUB_ID = "86e0aa6e7fbd2cdb02ec15e338d6b722";
	private static final String MOB_FOX_AD_URL = "http://my.mobfox.com/request.php";

	public static AdManager getAdManager(AdResponse ad) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d("Cannot find AdManager with running ad:" + ad.getTimestamp());
		}
		return adManager;
	}

	public static void closeRunningAd(AdResponse ad, boolean result) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d("Cannot find AdManager with running ad:" + ad.getTimestamp());
			return;
		}
		Log.d("Notify closing event to AdManager with running ad:" + ad.getTimestamp());
		adManager.notifyAdClose(ad, result);
	}

	public static void notifyAdClick(AdResponse ad) {
		AdManager adManager = sRunningAds.get(ad.getTimestamp());
		if (adManager != null) {
			adManager.notifyAdClicked();
		}
	}

	public AdManager(Context ctx) throws IllegalArgumentException {
		this(ctx, MOB_FOX_AD_URL, MOB_FOX_PUB_ID, false);
	}


	public AdManager(Context ctx, final String interstitialRequestURL, final String publisherId, final boolean includeLocation) throws IllegalArgumentException {
		Util.prepareAndroidAdId(ctx);
		AdManager.setmContext(ctx);
		this.interstitialRequestURL = interstitialRequestURL;
		this.mPublisherId = publisherId;
		this.mIncludeLocation = includeLocation;
		this.mRequestThread = null;
		this.mHandler = new Handler();
		initialize();
	}

	public void setListener(AdListener listener) {
		this.mListener = listener;
	}

	public void requestAd() {
		requestAdInternal(false);
	}

	private void requestAdInternal(boolean keepFlags) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		if (!keepFlags) {
			alreadyRequestedInterstitial = false;
		}

		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;

			mRequestThread = new Thread(new Runnable() {
				@Override
				public void run() {
/*					while (ResourceManager.isDownloading()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}*/
					Log.d("starting request thread");
					try {
						RequestGeneralAd requestAd = new RequestGeneralAd();
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
							requestAdInternal(true);
						} else {

							mResponse = new AdResponse();
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
					mResponse = new AdResponse();
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

	public void requestAd(final InputStream xml) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		alreadyRequestedInterstitial = false;


		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;
			mRequestThread = new Thread(new Runnable() {
				@Override
				public void run() {
/*					while (ResourceManager.isDownloading()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}*/
					Log.d("starting request thread");
					try {
						RequestGeneralAd requestAd = new RequestGeneralAd(xml);
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
						mResponse = new AdResponse();
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
					mResponse = new AdResponse();
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

	public boolean isAdLoaded() {
		return (mResponse != null);
	}

	public void requestAdAndShow(long timeout) {
		AdListener l = mListener;

		mListener = null;
		requestAd();
		long now = System.currentTimeMillis();
		long timeoutTime = now + timeout;
		while ((!isAdLoaded()) && (now < timeoutTime)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			now = System.currentTimeMillis();
		}
		mListener = l;
		showAd();
	}

	public void showAd() {

		if (((mResponse == null) || (mResponse.getType() == Const.NO_AD) || (mResponse.getType() == Const.AD_FAILED))) {
			notifyAdShown(mResponse, false);
			return;
		}
		AdResponse ad = mResponse;
		boolean result = false;
		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				ad.setHorizontalOrientationRequested(requestedHorizontalAd);
				Log.v("Showing Ad:" + ad);

				Intent intent = new Intent(getContext(), RichMediaActivity.class);
				intent.putExtra(AD_EXTRA, ad);
				getContext().startActivity(intent);

				result = true;
				sRunningAds.put(ad.getTimestamp(), this);
			} else {
				Log.d("No network available. Cannot show Ad.");
			}
		} catch (Exception e) {
			Log.e("Unknown exception when showing Ad", e);
		} finally {
			notifyAdShown(ad, result);
		}
	}

	private void initialize() throws IllegalArgumentException {
		Log.d("Ad SDK Version:" + Const.VERSION);

		this.androidAdId = Util.getAndroidAdId();
		this.adDoNotTrack = Util.hasAdDoNotTrack();

		if ((mPublisherId == null) || (mPublisherId.length() == 0)) {
			Log.e("Publisher Id cannot be null or empty");
			throw new IllegalArgumentException("User Id cannot be null or empty");
		}

		Log.d("AdManager Publisher Id:" + mPublisherId + " Advertising Id:" + androidAdId);
		mEnabled = (Util.getMemoryClass(getContext()) > 16);
	}

	private void notifyNoAdFound() {
		if (mListener != null) {
			Log.d("No ad found.");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.noAdFound();
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdLoaded(final AdResponse ad) {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.adLoadSucceeded(ad);
				}
			});
		}
	}

	private void notifyAdClicked() {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.adClicked();
				}
			});
		}
	}

	private void notifyAdShown(final AdResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("Ad Shown. Result:" + ok);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.adShown(ad, ok);
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdClose(final AdResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("Ad Close. Result:" + ok);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.adClosed(ad, ok);
				}
			});
		}
	}

	private AdRequest getInterstitialRequest() {
		if (this.request == null) {
			this.request = new AdRequest();
			request.setAndroidAdId(androidAdId);
			request.setAdDoNotTrack(adDoNotTrack);
			this.request.setPublisherId(this.mPublisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(mContext));
			this.request.setUserAgent2(Util.buildUserAgent());
		}
		Location location = null;
/*		request.setVideoRequest(false);*/
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
		AdManager.mContext = mContext;
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
