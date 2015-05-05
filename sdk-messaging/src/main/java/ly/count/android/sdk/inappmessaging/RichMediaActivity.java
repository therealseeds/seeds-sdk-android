package ly.count.android.sdk.inappmessaging;

import java.lang.ref.WeakReference;
/*import java.util.Iterator;
import java.util.Set;
import java.util.Timer;*/
import java.util.TimerTask;





/*import java.util.Vector;
*/
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
/*import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;*/
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
/*import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;*/
import android.widget.FrameLayout;
import android.widget.ImageView;
/*import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;*/






import ly.count.android.sdk.inappmessaging.BannerAdView.BannerAdViewListener;

public class RichMediaActivity extends Activity {

	class CanSkipTask extends TimerTask {

		private final RichMediaActivity mActivity;

		public CanSkipTask(final RichMediaActivity activity) {
			this.mActivity = activity;
		}

		@Override
		public void run() {

			Log.v("###########TRACKING CAN CLOSE INTERSTITIAL");
			this.mActivity.mCanClose = true;
			if (this.mActivity.mSkipButton != null)
				this.mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						CanSkipTask.this.mActivity.mSkipButton.setVisibility(View.VISIBLE);
					}
				});
		}
	}

	/*class VideoTimeoutTask extends TimerTask {

		private final Activity mActivity;

		public VideoTimeoutTask(final Activity activity) {
			this.mActivity = activity;
		}

		@Override
		public void run() {

			Log.v("###########TRACKING VIDEO TIMEOUT");
			this.mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					VideoTimeoutTask.this.mActivity.finish();
				}
			});
		}
	}*/

	public static final int TYPE_UNKNOWN = -1;

	public static final int TYPE_BROWSER = 0;
/*	public static final int TYPE_VIDEO = 1;*/
	public static final int TYPE_INTERSTITIAL = 2;

	private ResourceManager mResourceManager;
	private FrameLayout mRootLayout;
/*	private FrameLayout mVideoLayout;
	private FrameLayout mLoadingView;
	private FrameLayout mCustomView;
	private FrameLayout videoFrame;*/

/*	private VideoView mCustomVideoView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private SDKVideoView mVideoView;
	private WebFrame mOverlayView;*/
/*	private WebFrame mWebBrowserView;*/
/*	private MediaController mMediaController;*/
	private ImageView mSkipButton;
	private AdResponse mAd;
/*	private VideoData mVideoData;*/
/*	private LinearLayout buttonsLayout;*/

	private Uri uri;
/*	private Timer mVideoTimeoutTimer;*/
	private int mWindowWidth;
	private int mWindowHeight;
/*	private int mVideoLastPosition;
	private int mVideoWidth;
	private int mVideoHeight;*/
	private boolean mCanClose;
	protected boolean mInterstitialAutocloseReset;
	private int mType;
	private boolean wasClicked;

	private boolean mResult;

	DisplayMetrics metrics;

	int paddingArg = 5;

	int marginArg = 8;

	int skipButtonSizeLand = 40;

	int skipButtonSizePort = 40;

	static class ResourceHandler extends Handler {

		WeakReference<RichMediaActivity> richMediaActivity;

		public ResourceHandler(RichMediaActivity activity) {
			richMediaActivity = new WeakReference<RichMediaActivity>(activity);
		}

		@Override
		public void handleMessage(final Message msg) {
			RichMediaActivity wRichMediaActivity = richMediaActivity.get();
			if (wRichMediaActivity != null) {
				wRichMediaActivity.handleMessage(msg);
			}
		}
	};

	public void handleMessage(final Message msg) {
		switch (msg.what) {
		case ResourceManager.RESOURCE_LOADED_MSG:
			switch (msg.arg1) {
			case ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID:
				if (RichMediaActivity.this.mSkipButton != null) {
					RichMediaActivity.this.mSkipButton.setImageDrawable(mResourceManager.getResource(this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));
				}
				break;
			}
			break;

		}
	}

	/*private final OnTimeEventListener mOverlayShowListener = new OnTimeEventListener() {

		@Override
		public void onTimeEvent(final int time) {

			Log.d("RichMediaActivity mOverlayShowListener show after:" + time);
			if (RichMediaActivity.this.mOverlayView != null) {
				RichMediaActivity.this.mOverlayView.setVisibility(View.VISIBLE);
				RichMediaActivity.this.mOverlayView.requestLayout();
			}
		}
	};
*/
/*	private final OnClickListener mOverlayClickListener = new OnClickListener() {

		@Override
		public void onClick(final View arg0) {

			if (RichMediaActivity.this.mVideoData.overlayClickThrough != null) {
				if (RichMediaActivity.this.mVideoData.overlayClickTracking != null) {
					trackClick(RichMediaActivity.this.mVideoData.overlayClickTracking);
				}

				String s = RichMediaActivity.this.mVideoData.overlayClickThrough.trim();

				notifyAdClicked();

				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
				RichMediaActivity.this.startActivity(intent);
			}

			Log.d("RichMediaActivity mOverlayClickListener");
		}
	};

	OnErrorListener mOnVideoErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(final MediaPlayer mp, final int what, final int extra) {
			Log.w("Cannot play video/ Error: " + what + " Extra: " + extra);
			finish();
			return false;
		}
	};
*/
	protected int mTimeTest;

/*	OnInfoListener mOnVideoInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
			Log.i("Info: " + what + " Extra: " + extra);
			if (what == 703) {
				mTimeTest = mVideoView.getCurrentPosition();
				new Handler().postDelayed(mCheckProgressTask, 5000);
			}
			return false;
		}
	};
*/
	/*private Runnable mCheckProgressTask = new Runnable() {

		public void run() {
			Log.w("Video playback is being checked");
			int test = mVideoView.getCurrentPosition();
			if (test - mTimeTest <= 1) {
				Log.w("Video playback too slow. Ending");
				finish();
			} else {
				Log.w("Video playback has restarted");
			}
		}
	};

	OnPreparedListener mOnVideoPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(final MediaPlayer mp) {

			Log.d("RichMediaActivity onPrepared MediaPlayer");
			if (RichMediaActivity.this.mVideoTimeoutTimer != null) {
				RichMediaActivity.this.mVideoTimeoutTimer.cancel();
				RichMediaActivity.this.mVideoTimeoutTimer = null;
			}
			if (RichMediaActivity.this.mLoadingView != null)
				RichMediaActivity.this.mLoadingView.setVisibility(View.GONE);

			mMediaController.setVisibility(View.VISIBLE);

			RichMediaActivity.this.videoFrame.requestFocus();
		}
	};

	OnClickListener mOnVideoClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (RichMediaActivity.this.mVideoData.videoClickThrough != null) {

				if (RichMediaActivity.this.mVideoData.videoClickTracking != null) {
					for (String tracking : RichMediaActivity.this.mVideoData.videoClickTracking) {
						trackClick(tracking);
					}
				}

				String s = RichMediaActivity.this.mVideoData.videoClickThrough.trim();
				notifyAdClicked();
				mOnVideoCanCloseEventListener.onTimeEvent(0); // to show skip button

				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
				RichMediaActivity.this.startActivity(intent);
			}
		}
	};

	OnCompletionListener mOnVideoCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(final MediaPlayer mp) {
			mp.seekTo(0);
			Log.d("###########TRACKING END VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.completeEvents;
			for (int i = 0; i < trackers.size(); i++) {

				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}
			RichMediaActivity.this.mResult = true;
			RichMediaActivity.this.setResult(Activity.RESULT_OK);

			mMediaController.hide();
			buttonsLayout.setVisibility(View.VISIBLE);
		}
	};

	OnStartListener mOnVideoStartListener = new OnStartListener() {

		@Override
		public void onVideoStart() {

			Log.d("###########TRACKING START VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.startEvents;
			trackers.addAll(RichMediaActivity.this.mVideoData.impressionEvents);

			for (int i = 0; i < trackers.size(); i++) {
				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}
			trackers.clear();
			RichMediaActivity.this.mVideoData.impressionEvents.clear();
		}
	};

	OnPauseListener mOnVideoPauseListener = new OnPauseListener() {

		@Override
		public void onVideoPause() {

			Log.d("###########TRACKING PAUSE VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.pauseEvents;
			for (int i = 0; i < trackers.size(); i++) {

				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}
		}
	};

	OnUnpauseListener mOnVideoUnpauseListener = new OnUnpauseListener() {

		@Override
		public void onVideoUnpause() {

			Log.d("###########TRACKING UNPAUSE VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.resumeEvents;
			for (int i = 0; i < trackers.size(); i++) {

				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}
		}
	};

	OnTimeEventListener mOnVideoTimeEventListener = new OnTimeEventListener() {

		@Override
		public void onTimeEvent(final int time) {

			Log.d("###########TRACKING TIME VIDEO:" + time);
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.timeTrackingEvents.get(time);
			if (trackers != null)
				for (int i = 0; i < trackers.size(); i++) {

					Log.d("Track url:" + trackers.get(i));
					final TrackEvent event = new TrackEvent();
					event.url = trackers.get(i);
					event.timestamp = System.currentTimeMillis();
					TrackerService.requestTrack(event);
				}
		}
	};

	OnTimeEventListener mOnVideoCanCloseEventListener = new OnTimeEventListener() {

		@Override
		public void onTimeEvent(final int time) {

			Log.d("###########CAN CLOSE VIDEO:" + time);
			RichMediaActivity.this.mCanClose = true;
			if (RichMediaActivity.this.mSkipButton != null && RichMediaActivity.this.mSkipButton.getVisibility() != View.VISIBLE && RichMediaActivity.this.mVideoData.showSkipButton) {

				RichMediaActivity.this.mSkipButton.setImageDrawable(mResourceManager.getResource(RichMediaActivity.this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));

				RichMediaActivity.this.mSkipButton.setVisibility(View.VISIBLE);
			}
		}
	};

	OnClickListener mOnVideoSkipListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {

			Log.v("###########TRACKING SKIP VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.skipEvents;
			for (int i = 0; i < trackers.size(); i++) {

				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}

			RichMediaActivity.this.finish();
		}
	};

	OnReplayListener mOnVideoReplayListener = new OnReplayListener() {

		@Override
		public void onVideoReplay() {

			Log.d("###########TRACKING REPLAY VIDEO");
			final Vector<String> trackers = RichMediaActivity.this.mVideoData.replayEvents;
			for (int i = 0; i < trackers.size(); i++) {

				Log.d("Track url:" + trackers.get(i));
				final TrackEvent event = new TrackEvent();
				event.url = trackers.get(i);
				event.timestamp = System.currentTimeMillis();
				TrackerService.requestTrack(event);
			}
		}
	};
*/
	OnClickListener mOnInterstitialSkipListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {

			Log.v("###########TRACKING SKIP INTERSTITIAL");

			RichMediaActivity.this.mResult = true;
			RichMediaActivity.this.setResult(Activity.RESULT_OK);
			RichMediaActivity.this.finish();
		}
	};

/*	private void trackClick(String trackUrl) {
		final TrackEvent event = new TrackEvent();
		event.url = trackUrl;
		event.timestamp = System.currentTimeMillis();
		TrackerService.requestTrack(event);
	}*/

	private ResourceHandler mHandler;

	@Override
	public void finish() {

		if (this.mAd != null) {
			Log.d("Finish Activity type:" + this.mType + " ad Type:" + this.mAd.getType());
			switch (this.mType) {
/*			case TYPE_VIDEO:
				if (this.mAd.getType() == Const.VIDEO)
					AdManager.closeRunningAd(this.mAd, this.mResult);
				break;*/
			case TYPE_INTERSTITIAL:
				AdManager.closeRunningAd(this.mAd, this.mResult);
				break;
			}
		}
		super.finish();
	}

	public int getDipSize(final int argSize) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, argSize, this.getResources().getDisplayMetrics());
	}

	public FrameLayout getRootLayout() {
		return this.mRootLayout;
	}

	public void goBack() {
/*		if (this.mCustomView != null) {

			Log.d("Closing custom view on back key pressed");
			this.onHideCustomView();
			return;
		}*/
		switch (this.mType) {
/*		case TYPE_VIDEO:
			if (this.mCanClose)
				this.finish();
			break;*/
		case TYPE_INTERSTITIAL:
			this.mResult = true;
			this.setResult(Activity.RESULT_OK);
			this.finish();
			break;
		case TYPE_BROWSER:
			this.finish();
			break;
		}

	}

	private void initInterstitialFromBannerView() {
		final FrameLayout layout = new FrameLayout(this);
		if (mAd.getType() == Const.TEXT || mAd.getType() == Const.IMAGE) {
			final float scale = this.getResources().getDisplayMetrics().density;
			int width, height;
			if (mAd.isHorizontalOrientationRequested()) {
				width = 480;
				height = 320;
			} else {
				width = 320;
				height = 480;
			}

			BannerAdView banner = new BannerAdView(this, mAd, width, height, false, createLocalAdListener());
			banner.setLayoutParams(new FrameLayout.LayoutParams((int) (width * scale + 0.5f), (int) (height * scale + 0.5f), Gravity.CENTER));
			banner.showContent();
			layout.addView(banner);
		}
/*		if (mAd.getType() == Const.MRAID) {
			MraidView mMRAIDView = new MraidView(this);
			layout.addView(mMRAIDView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mMRAIDView.setMraidListener(createMraidListener());
			mMRAIDView.loadHtmlData(mAd.getText());

		}*/

		this.mSkipButton = new ImageView(this);
		this.mSkipButton.setAdjustViewBounds(false);

		int buttonSize;
		if (mAd.isHorizontalOrientationRequested()) {
			buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizeLand, this.getResources().getDisplayMetrics());
		} else {
			buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizePort, this.getResources().getDisplayMetrics());
		}

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.TOP | Gravity.RIGHT);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources().getDisplayMetrics());
		params.topMargin = margin;
		params.rightMargin = margin;

		this.mSkipButton.setImageDrawable(mResourceManager.getResource(this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));

		this.mSkipButton.setOnClickListener(this.mOnInterstitialSkipListener);

		this.mCanClose = true;
		this.mSkipButton.setVisibility(View.VISIBLE);

		layout.addView(this.mSkipButton, params);

		this.mRootLayout.addView(layout);
	}

	private BannerAdViewListener createLocalAdListener() {
		return new BannerAdViewListener() {

			@Override
			public void onLoad() {
			}

			@Override
			public void onClick() {
				notifyAdClicked();
			}
		};
	}

/*	private MraidListener createMraidListener() {
		return new MraidListener() {

			@Override
			public void onReady(MraidView view) {
			}

			@Override
			public void onFailure(MraidView view) {
			}

			@Override
			public void onExpand(MraidView view) {
				notifyAdClicked();
			}

			@Override
			public void onClose(MraidView view, ViewState newViewState) {
			}
		};
	}*/

	private void notifyAdClicked() {
		wasClicked = true;
		AdManager.notifyAdClick(mAd);
	}

	private void initRootLayout() {
		this.mRootLayout = new FrameLayout(this);
		this.mRootLayout.setBackgroundColor(Color.BLACK);
	}

	/*private void initVideoView() {

		this.mVideoData = mAd.getVideoData();

		this.setRequestedOrientation(this.mVideoData.orientation);
		if (this.mVideoData.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			if (this.mWindowWidth < this.mWindowHeight) {
				final int size = this.mWindowWidth;
				this.mWindowWidth = this.mWindowHeight;
				this.mWindowHeight = size;
			}
		} else if (this.mWindowHeight < this.mWindowWidth) {
			final int size = this.mWindowHeight;
			this.mWindowHeight = this.mWindowWidth;
			this.mWindowWidth = size;
		}
		this.mVideoWidth = this.mVideoData.width;
		this.mVideoHeight = this.mVideoData.height;
		if (this.mVideoWidth <= 0) {
			this.mVideoWidth = this.mWindowWidth;
			this.mVideoHeight = this.mWindowHeight;
		} else {
			final DisplayMetrics m = this.getResources().getDisplayMetrics();
			this.mVideoWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.mVideoWidth, m);
			this.mVideoHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.mVideoHeight, m);

			if (this.mVideoWidth > this.mWindowWidth)
				this.mVideoWidth = this.mWindowWidth;
			if (this.mVideoHeight > this.mWindowHeight)
				this.mVideoHeight = this.mWindowHeight;
		}

		Log.d("Video size (" + this.mVideoWidth + "," + this.mVideoHeight + ")");

		this.mVideoLayout = new FrameLayout(this);
		videoFrame = new FrameLayout(this);

		this.mVideoView = new SDKVideoView(this, this.mVideoWidth, this.mVideoHeight, this.mVideoData.display);
		videoFrame.addView(mVideoView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		this.mVideoLayout.addView(videoFrame, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		if (this.mVideoData.showHtmlOverlay) {
			this.mOverlayView = new WebFrame(this, false, false, false);
			this.mOverlayView.setEnableZoom(false);
			this.mOverlayView.setOnClickListener(this.mOverlayClickListener);
			this.mOverlayView.setBackgroundColor(Color.TRANSPARENT);

			if (this.mVideoData.showHtmlOverlayAfter > 0) {
				this.mOverlayView.setVisibility(View.GONE);
				this.mVideoView.setOnTimeEventListener(this.mVideoData.showHtmlOverlayAfter, this.mOverlayShowListener);
			}
			if (this.mVideoData.htmlOverlayType == VideoData.OVERLAY_URL)
				this.mOverlayView.loadUrl(this.mVideoData.htmlOverlayUrl);
			else
				this.mOverlayView.setMarkup(this.mVideoData.htmlOverlayMarkup);

			final float scale = getResources().getDisplayMetrics().density;
			final FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams((int) (mVideoData.overlayWidth * scale + 0.5f), (int) (mVideoData.overlayHeight * scale + 0.5f));
			// final FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			overlayParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			this.mVideoLayout.addView(this.mOverlayView, overlayParams);
		}
		this.mMediaController = new MediaController(this, this.mVideoData);
		this.mVideoView.setMediaController(this.mMediaController);
		if (!this.mVideoData.pauseEvents.isEmpty())
			this.mMediaController.setOnPauseListener(this.mOnVideoPauseListener);
		if (!this.mVideoData.resumeEvents.isEmpty())
			this.mMediaController.setOnUnpauseListener(this.mOnVideoUnpauseListener);
		if (!this.mVideoData.replayEvents.isEmpty())
			this.mMediaController.setOnReplayListener(this.mOnVideoReplayListener);

		this.videoFrame.addView(this.mMediaController, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT));

		if (this.mVideoData.showSkipButton) {

			this.mSkipButton = new ImageView(this);
			this.mSkipButton.setAdjustViewBounds(false);
			FrameLayout.LayoutParams params = null;

			int buttonSize;
			if (mAd.isHorizontalOrientationRequested()) {
				buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizeLand, this.getResources().getDisplayMetrics());
			} else {
				buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizePort, this.getResources().getDisplayMetrics());
			}

			params = new FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.TOP | Gravity.RIGHT);
			if (this.mVideoData.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources().getDisplayMetrics());
				params.topMargin = margin;
				params.rightMargin = margin;
			} else {
				final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, this.getResources().getDisplayMetrics());
				params.topMargin = margin;
				params.rightMargin = margin;
			}

			if (this.mVideoData.skipButtonImage != null && this.mVideoData.skipButtonImage.length() > 0)
				this.mResourceManager.fetchResource(this, this.mVideoData.skipButtonImage, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID);
			else
				this.mSkipButton.setImageDrawable(mResourceManager.getResource(this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));
			this.mSkipButton.setOnClickListener(this.mOnVideoSkipListener);
			if (this.mVideoData.showSkipButtonAfter > 0) {
				this.mCanClose = false;
				this.mSkipButton.setVisibility(View.GONE);
			} else {
				this.mCanClose = true;
				this.mSkipButton.setVisibility(View.VISIBLE);
			}
			this.mVideoLayout.addView(this.mSkipButton, params);
		} else
			this.mCanClose = false;
		if (this.mVideoData.showSkipButtonAfter > 0)
			this.mVideoView.setOnTimeEventListener(this.mVideoData.showSkipButtonAfter, this.mOnVideoCanCloseEventListener);
		final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		this.mLoadingView = new FrameLayout(this);
		final TextView loadingText = new TextView(this);
		loadingText.setText(Const.LOADING);
		this.mLoadingView.addView(loadingText, params);
		this.mVideoLayout.addView(this.mLoadingView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));

		buttonsLayout = new LinearLayout(RichMediaActivity.this);
		buttonsLayout.setOrientation(LinearLayout.VERTICAL);

		Button clickButton = new Button(RichMediaActivity.this);
		clickButton.setText("Click here");
		clickButton.setTextColor(Color.BLACK);
		clickButton.setTextSize(18);
		clickButton.setTypeface(null, Typeface.BOLD);
		clickButton.setBackgroundColor(0xEFE7E8E9);
		clickButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mVideoView.performClick();
			}
		});

		int minimalButtonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 205, getResources().getDisplayMetrics());
		clickButton.setMinimumWidth(minimalButtonWidth);

		buttonsLayout.addView(clickButton, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		View separator = new View(this);
		LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		separatorParams.height = 4;
		separator.setBackgroundColor(Color.DKGRAY);

		buttonsLayout.addView(separator, separatorParams);

		Button replayButton = new Button(RichMediaActivity.this);
		replayButton.setText("↻");
		replayButton.setTypeface(null, Typeface.BOLD);
		replayButton.setTextColor(Color.BLACK);
		replayButton.setBackgroundColor(0xEFE7E8E9);
		replayButton.setTextSize(18);
		replayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				replayVideo();
				mMediaController.show();
				buttonsLayout.setVisibility(View.INVISIBLE);
			}
		});

		buttonsLayout.addView(replayButton, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		buttonsLayout.setVisibility(View.INVISIBLE);
		mVideoLayout.addView(buttonsLayout, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));

		if (this.mVideoData.videoClickThrough != null) {
			this.mVideoView.setOnClickListener(mOnVideoClickListener);
		}

		this.mVideoView.setOnPreparedListener(this.mOnVideoPreparedListener);
		this.mVideoView.setOnCompletionListener(this.mOnVideoCompletionListener);
		this.mVideoView.setOnErrorListener(this.mOnVideoErrorListener);
		this.mVideoView.setOnInfoListener(this.mOnVideoInfoListener);
		if (!this.mVideoData.startEvents.isEmpty() || !this.mVideoData.impressionEvents.isEmpty())
			this.mVideoView.setOnStartListener(this.mOnVideoStartListener);
		if (!this.mVideoData.timeTrackingEvents.isEmpty()) {
			final Set<Integer> keys = this.mVideoData.timeTrackingEvents.keySet();
			for (final Iterator<Integer> it = keys.iterator(); it.hasNext();) {
				final int key = it.next();
				this.mVideoView.setOnTimeEventListener(key, this.mOnVideoTimeEventListener);
			}
		}
		this.mVideoLastPosition = 0;
		String path = this.mVideoData.videoUrl;
		this.mVideoView.setVideoPath(path);
	}*/

/*	private void initWebBrowserView(final boolean showExit) {
		this.mWebBrowserView = new WebFrame(this, true, true, showExit);

		this.mRootLayout.addView(this.mWebBrowserView);
	}*/

	public void navigate(final String clickUrl) {
		notifyAdClicked();
		switch (this.mType) {
/*		case TYPE_BROWSER:
			this.mWebBrowserView.loadUrl(clickUrl);
			break;*/
		default:
			final Intent intent = new Intent(this, RichMediaActivity.class);
			intent.setData(Uri.parse(clickUrl));
			this.startActivity(intent);
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Log.d("RichMediaActivity onConfigurationChanged");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle icicle) {

		Log.d("RichMediaActivity onCreate");
		super.onCreate(icicle);

		try {

			this.mResult = false;
			this.setResult(Activity.RESULT_CANCELED);
			final Window win = this.getWindow();
			win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			final Display display = this.getWindowManager().getDefaultDisplay();
			this.metrics = new DisplayMetrics();
			final WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(this.metrics);
			this.mWindowWidth = display.getWidth();
			this.mWindowHeight = display.getHeight();
			win.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			Log.d("RichMediaActivity Window Size:(" + this.mWindowWidth + "," + this.mWindowHeight + ")");
/*
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
*/
			this.mType = RichMediaActivity.TYPE_UNKNOWN;
			final Intent intent = this.getIntent();
			final Bundle extras = intent.getExtras();
			if (extras == null || extras.getSerializable(Const.AD_EXTRA) == null) {
				this.uri = intent.getData();
				if (this.uri == null) {

					Log.d("url is null so do not load anything");
					this.finish();
					return;
				}
				this.mType = RichMediaActivity.TYPE_BROWSER;
			} else
				this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			mHandler = new ResourceHandler(this);

			this.mResourceManager = new ResourceManager(this, this.mHandler);
			this.initRootLayout();

/*			if (this.mType == RichMediaActivity.TYPE_BROWSER) {
				this.initWebBrowserView(true);
				this.mWebBrowserView.loadUrl(this.uri.toString());
			} else {*/
				this.mAd = (AdResponse) extras.getSerializable(Const.AD_EXTRA);

				this.mCanClose = false;
				this.mType = extras.getInt(Const.AD_TYPE_EXTRA, -1);
				if (this.mType == -1)
					switch (this.mAd.getType()) {
/*					case Const.VIDEO:
						this.mType = TYPE_VIDEO;
						break;*/
					case Const.TEXT:
/*					case Const.MRAID:*/
					case Const.IMAGE:
						if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
							setOrientationOldApi();
						} else {
							setOrientation();
						}
						this.mType = TYPE_INTERSTITIAL;
						break;
					}
				switch (this.mType) {
/*				case TYPE_VIDEO:
					Log.v("Type video");
					this.initVideoView();
					break;*/
				case TYPE_INTERSTITIAL:
					Log.v("Type interstitial like banner");
					this.initInterstitialFromBannerView();
					break;
				}
/*			}*/

			this.setContentView(this.mRootLayout);
			Log.d("RichMediaActivity onCreate done");

		} catch (Exception e) { // in unlikely case something goes terribly wrong
			finish();
		}
	}

	private void setOrientationOldApi() {
		if (mAd.isHorizontalOrientationRequested()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void setOrientation() {
		if (mAd.isHorizontalOrientationRequested()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}

	@Override
	protected void onDestroy() {
/*		mMediaController = null;*/
		if (mResourceManager != null)
			mResourceManager.releaseInstance();
/*		if (this.mVideoView != null)
			this.mVideoView.destroy();*/
		Log.d("RichMediaActivity onDestroy");
		super.onDestroy();

		Log.d("RichMediaActivity onDestroy done");
	}

/*	public void onHideCustomView() {

		Log.d("onHideCustomView Hidding Custom View");
		if (this.mCustomView != null) {

			this.mCustomView.setVisibility(View.GONE);
			this.mCustomView = null;
			if (this.mCustomVideoView != null) {
				try {

					Log.d("onHideCustomView stop video");
					this.mCustomVideoView.stopPlayback();
				} catch (final Exception e) {
					Log.d("Couldn't stop custom video view");
				}
				this.mCustomVideoView = null;
			}
		}

		Log.d("onHideCustomView calling callback");
		this.mCustomViewCallback.onCustomViewHidden();
		this.mRootLayout.setVisibility(View.VISIBLE);
		this.setContentView(this.mRootLayout);
	}*/

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {

		Log.d("RichMediaActivity onPause");
		super.onPause();
/*		switch (this.mType) {
		case TYPE_VIDEO:
			this.mVideoLastPosition = this.mVideoView.getCurrentPosition();
			this.mVideoView.stopPlayback();
			this.mRootLayout.removeView(this.mVideoLayout);
			if (this.mVideoTimeoutTimer != null) {
				this.mVideoTimeoutTimer.cancel();
				this.mVideoTimeoutTimer = null;
			}
			break;
		}*/

		Log.d("RichMediaActivity onPause done");
	}

	@Override
	protected void onResume() {
		if (wasClicked) { // close after coming back from click.
			RichMediaActivity.this.mResult = true;
			RichMediaActivity.this.setResult(Activity.RESULT_OK);
			RichMediaActivity.this.finish();
		}

		Log.d("RichMediaActivity onResume");
		super.onResume();
		switch (this.mType) {
/*		case TYPE_VIDEO:
			this.mRootLayout.addView(this.mVideoLayout);
			this.mVideoView.seekTo(this.mVideoLastPosition);
			this.mVideoView.start();
			if (this.mVideoTimeoutTimer == null) {
				final VideoTimeoutTask autocloseTask = new VideoTimeoutTask(RichMediaActivity.this);
				this.mVideoTimeoutTimer = new Timer();
				this.mVideoTimeoutTimer.schedule(autocloseTask, Const.VIDEO_LOAD_TIMEOUT);
			}

			break;*/
		case TYPE_BROWSER:
			break;
		}

		Log.d("RichMediaActivity onResume done");
	}

/*	public void onShowCustomView(final View view, final CustomViewCallback callback) {

		Log.d(" onShowCustomView");
		if (view instanceof FrameLayout) {

			this.mCustomView = (FrameLayout) view;
			this.mCustomViewCallback = callback;
			if (this.mCustomView.getFocusedChild() instanceof VideoView) {

				Log.d(" onShowCustomView Starting Video View");
				this.mCustomVideoView = (VideoView) this.mCustomView.getFocusedChild();
				this.mCustomVideoView.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(final MediaPlayer mp) {

						Log.d(" onCompletion Video");
						RichMediaActivity.this.onHideCustomView();
					}
				});
				this.mCustomVideoView.start();
			}
			this.mRootLayout.setVisibility(View.GONE);
			this.mCustomView.setVisibility(View.VISIBLE);
			this.setContentView(this.mCustomView);
		}
	}

	public void playVideo() {

		Log.d("RichMediaActivity play video:" + this.mType);
		switch (this.mType) {
		case TYPE_VIDEO:
			if (this.mMediaController != null)
				this.mMediaController.replay();
			break;
		}
	}

	public void replayVideo() {
		if (this.mMediaController != null)
			this.mMediaController.replay();
	}*/

}
