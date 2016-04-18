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
 *		Changes: 	moved from banner sub-package
 *					renamed from BannerAdView
 *					removed HttpClient
 */

package com.playseeds.android.sdk.inappmessaging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

@SuppressLint({ "ViewConstructor", "SetJavaScriptEnabled" })
public class InAppMessageView extends RelativeLayout {
	public static final int LIVE = 0;
	public static final int TEST = 1;

	private boolean animation;

	private boolean isInternalBrowser = false;
	private boolean wasUserAction = false;

	private InAppMessageResponse response;
	private Animation fadeInAnimation = null;
	// private Animation fadeOutAnimation = null;
	private WebSettings webSettings;

	private Context mContext = null;
	protected boolean mIsInForeground;

	private WebView webView;

	private int width;
	private int height;

	private BannerAdViewListener adListener;

	private static Method mWebView_SetLayerType;
	private static Field mWebView_LAYER_TYPE_SOFTWARE;

	public InAppMessageView(final Context context, final InAppMessageResponse response, int width, int height, final boolean animation, final BannerAdViewListener adListener) {
		super(context);
		mContext = context;
		this.response = response;
		this.width = width;
		this.height = height;
		this.animation = animation;
		this.adListener = adListener;
		this.initialize(context);
	}

	private WebView createWebView(final Context context) {
		final WebView webView = new WebView(this.getContext()) {

			@Override
			public boolean onTouchEvent(MotionEvent event) {
				wasUserAction = true;
				return super.onTouchEvent(event);
			}

			@Override
			public void draw(final Canvas canvas) {
				if (this.getWidth() > 0 && this.getHeight() > 0)
					super.draw(canvas);
			}
		};

		this.webSettings = webView.getSettings();
		this.webSettings.setJavaScriptEnabled(true);
		webView.setBackgroundColor(Color.TRANSPARENT);
		setLayer(webView);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				if (wasUserAction) {
					if (response.getSkipOverlay() == 1) {
						doOpenUrl(url);
						return true;
					}
					openLink();
					return true;
				}
				return false;
			}

		});

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);

		return webView;
	}

	private void doOpenUrl(final String url) {
		adListener.onClick();
		if (this.response.getClickUrl() != null && this.response.getSkipOverlay() == 1) {
			makeTrackingRequest(this.response.getClickUrl());
		}

		if (this.response.getClickType() != null && this.response.getClickType().equals(ClickType.INAPP) && (url.startsWith("http://") || url.startsWith("https://"))) {
			if (url.endsWith(".mp4")) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse(url), "video/mp4");
				startActivity(i);
				// this.getContext().startActivity(intent);
			} else {
				final Intent intent = new Intent(this.getContext(), InAppWebView.class);
				intent.putExtra(Const.REDIRECT_URI, url);
				startActivity(intent);
				// this.getContext().startActivity(intent);
			}
		} else if (url.equals("about:close")) {
			if (getContext() instanceof RichMediaActivity) {
				RichMediaActivity activity = (RichMediaActivity)getContext();
				activity.close();
			}
		} else {
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			// this.getContext().startActivity(intent);
		}
	}

	/**
	 * This method allows the activity to be started from outside an activity
	 */
	private void startActivity(Intent i) {
		if (!(getContext() instanceof Activity)) {
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		try {
			getContext().startActivity(i);
		} catch (Exception e) {
			Log.e("Failed to start activity using " + i.toString(), e);
			adListener.onError();
		}
	}

	private void makeTrackingRequest(final String clickUrl) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (clickUrl.startsWith("market")) { // just to stay safe
					return null;
				}
				HttpURLConnection urlConnection = null;
				try {
					urlConnection = (HttpURLConnection) new URL(clickUrl).openConnection();
					urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
					InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
					byte[] data = new byte[16384];
					while (inputStream.read(data, 0, data.length) != -1);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (urlConnection != null)
						urlConnection.disconnect();
				}
				return null;
			}

		};
		task.execute();

	}

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			for (Method m : WebView.class.getMethods()) {
				if (m.getName().equals("setLayerType")) {
					mWebView_SetLayerType = m;
					break;
				}
			}

			Log.v("set layer " + mWebView_SetLayerType);
			mWebView_LAYER_TYPE_SOFTWARE = WebView.class.getField("LAYER_TYPE_SOFTWARE");
			Log.v("set1 layer " + mWebView_LAYER_TYPE_SOFTWARE);

		} catch (SecurityException e) {

			Log.v("SecurityException");
		} catch (NoSuchFieldException e) {

			Log.v("NoSuchFieldException");
		}
	}

	private static void setLayer(WebView webView) {
		if (mWebView_SetLayerType != null && mWebView_LAYER_TYPE_SOFTWARE != null) {
			try {
				Log.v("Set Layer is supported");
				mWebView_SetLayerType.invoke(webView, mWebView_LAYER_TYPE_SOFTWARE.getInt(WebView.class), null);
			} catch (InvocationTargetException ite) {
				Log.v("Set InvocationTargetException");
			} catch (IllegalArgumentException e) {
				Log.v("Set IllegalArgumentException");
			} catch (IllegalAccessException e) {
				Log.v("Set IllegalAccessException");
			}
		} else {
			Log.v("Set Layer is not supported");
		}
	}

	private void buildBannerView() {
		this.webView = this.createWebView(mContext);
		Log.d("Create view flipper");
		if (this.response != null && this.response.getClickUrl() != null) {
			final float scale = mContext.getResources().getDisplayMetrics().density;
			if (width > 0 && height > 0) {
				this.setLayoutParams(new RelativeLayout.LayoutParams((int) (width * scale + 0.5f), (int) (height * scale + 0.5f)));
			} else {
				this.setLayoutParams(new RelativeLayout.LayoutParams((int) (300 * scale + 0.5f), (int) (50 * scale + 0.5f)));
			}
		} else {
			this.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(this.webView, params);

		Log.d("animation: " + this.animation);
		if (this.animation) {

			this.fadeInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
			this.fadeInAnimation.setDuration(1000);

			// this.fadeOutAnimation = new TranslateAnimation(
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, -1.0f);
			// this.fadeOutAnimation.setDuration(1000);
			this.webView.setAnimation(fadeInAnimation);
		}
	}

	private void initialize(final Context context) {
		initCompatibility();
		buildBannerView();
	}

	public boolean isInternalBrowser() {
		return this.isInternalBrowser;
	}

	private void openLink() {

		if (this.response != null && this.response.getClickUrl() != null)
			this.doOpenUrl(this.response.getClickUrl());

	}

	public void setAdListener(final BannerAdViewListener bannerListener) {
		this.adListener = bannerListener;
	}

	public void setInternalBrowser(final boolean isInternalBrowser) {
		this.isInternalBrowser = isInternalBrowser;
	}

	public void showContent() {

		try {
			if (this.response.getType() == Const.IMAGE) {

				String text = MessageFormat.format(Const.IMAGE_BODY, this.response.getImageUrl(), this.response.getBannerWidth(), this.response.getBannerHeight());
				Log.d("set image: " + text);
				if (!text.contains("<html>")) {
					text = "<html><head></head><body style='margin:0;padding:0;'>" + Const.HIDE_BORDER + text + "</body></html>";
				}
				webView.loadData(text, "text/html", Const.ENCODING);
				adListener.onLoad();
			} else if (this.response.getType() == Const.TEXT) {
				String text = this.response.getText();
				Log.d("set text: " + text);
				if (!text.contains("<html>")) {
					text = "<html><head></head><body style='margin:0;padding:0;'>" + Const.HIDE_BORDER + text + "</body></html>";
				}
				webView.loadData(text, "text/html", Const.ENCODING);
				adListener.onLoad();
			}
			if (animation) {
				webView.startAnimation(fadeInAnimation);
			}
		} catch (final Throwable t) {
			Log.e("Exception in show content", t);
		}
	}

	public interface BannerAdViewListener {
		public void onLoad();

		public void onClick();

		public void onError();
	}

}
