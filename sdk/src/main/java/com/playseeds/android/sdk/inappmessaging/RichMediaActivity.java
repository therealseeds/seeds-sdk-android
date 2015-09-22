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
 *		Changes: moved from video sub-package, removed video, MRAID and custome event
 *		code
 */

package com.playseeds.android.sdk.inappmessaging;

import java.lang.ref.WeakReference;

import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;

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
import android.view.Window;
import android.view.WindowManager;

import android.widget.FrameLayout;
import android.widget.ImageView;







import com.playseeds.android.sdk.inappmessaging.InAppMessageView.BannerAdViewListener;

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


	public static final int TYPE_UNKNOWN = -1;

	public static final int TYPE_BROWSER = 0;

	public static final int TYPE_INTERSTITIAL = 2;

	private ResourceManager mResourceManager;
	private FrameLayout mRootLayout;

	private ImageView mSkipButton;
	private InAppMessageResponse mAd;


	private Uri uri;

	private int mWindowWidth;
	private int mWindowHeight;

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


	OnClickListener mOnInterstitialSkipListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {

			Log.v("###########TRACKING SKIP INTERSTITIAL");

			RichMediaActivity.this.mResult = true;
			RichMediaActivity.this.setResult(Activity.RESULT_OK);
			RichMediaActivity.this.finish();
		}
	};


	private ResourceHandler mHandler;

	@Override
	public void finish() {

		if (this.mAd != null) {
			Log.d("Finish Activity type:" + this.mType + " ad Type:" + this.mAd.getType());
			InAppMessageManager.closeRunningInAppMessage(this.mAd, this.mResult);
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

		switch (this.mType) {
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
			final DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
			final float scale = displayMetrics.density;

			int adWidth, adHeight;
			if (mAd.isHorizontalOrientationRequested()) {
				adWidth = 1080;
				adHeight = 720;
			} else {
				adWidth = 720;
				adHeight = 1080;
			}

			int width = (int)(displayMetrics.widthPixels / displayMetrics.density);
			int height = (int)(displayMetrics.heightPixels / displayMetrics.density);

			final float adAspectRatio = adWidth / (float) adHeight;
			if (adAspectRatio >= 1.0f)
				width = (int)(height * adAspectRatio + 0.5f);
			else
				height = (int)(width / adAspectRatio + 0.5f);

			InAppMessageView banner = new InAppMessageView(this, mAd, width, height, false, createLocalAdListener());

			banner.setLayoutParams(new FrameLayout.LayoutParams(
					(int) (width * scale + 0.5f),
					(int) (height * scale + 0.5f),
					Gravity.CENTER));
			banner.showContent();
			layout.addView(banner);
		}

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

			@Override
			public void onError() {
				finish();
			}
		};
	}


	private void notifyAdClicked() {
		wasClicked = true;
		InAppMessageManager.notifyInAppMessageClick(mAd);
	}

	private void initRootLayout() {
		this.mRootLayout = new FrameLayout(this);
		this.mRootLayout.setBackgroundColor(Color.argb(128, 0, 0, 0));
	}


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
				Log.d("uri " + uri);
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
				this.mAd = (InAppMessageResponse) extras.getSerializable(Const.AD_EXTRA);

				this.mCanClose = false;
				this.mType = extras.getInt(Const.AD_TYPE_EXTRA, -1);
				if (this.mType == -1)
					switch (this.mAd.getType()) {

					case Const.TEXT:
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

				case TYPE_INTERSTITIAL:
					Log.v("Type interstitial like banner");
					this.initInterstitialFromBannerView();
					break;
				}


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

		if (mResourceManager != null)
			mResourceManager.releaseInstance();

		Log.d("RichMediaActivity onDestroy");
		super.onDestroy();

		Log.d("RichMediaActivity onDestroy done");
	}



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
		case TYPE_BROWSER:
			break;
		}

		Log.d("RichMediaActivity onResume done");
	}



}
