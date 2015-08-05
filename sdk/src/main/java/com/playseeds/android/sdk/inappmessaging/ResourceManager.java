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
 *		Changes: moved from video sub-package
 */

package com.playseeds.android.sdk.inappmessaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.playseeds.android.sdk.BuildConfig;

public class ResourceManager {

	public static final int RESOURCE_LOADED_MSG = 100;
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_FILE = 0;
	public static final int TYPE_ZIP = 1;
	public static boolean sDownloading = false;
	public static boolean sCancel = false;
	public static HttpGet sDownloadGet;

	public static final String VERSION = "version.txt";

	public static final int DEFAULT_TOPBAR_BG_RESOURCE_ID = -1;
	public static final int DEFAULT_BOTTOMBAR_BG_RESOURCE_ID = -2;

	public static final int DEFAULT_PLAY_IMAGE_RESOURCE_ID = -11;
	public static final int DEFAULT_PAUSE_IMAGE_RESOURCE_ID = -12;
	public static final int DEFAULT_REPLAY_IMAGE_RESOURCE_ID = -13;
	public static final int DEFAULT_BACK_IMAGE_RESOURCE_ID = -14;
	public static final int DEFAULT_FORWARD_IMAGE_RESOURCE_ID = -15;
	public static final int DEFAULT_RELOAD_IMAGE_RESOURCE_ID = -16;
	public static final int DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID = -17;
	public static final int DEFAULT_SKIP_IMAGE_RESOURCE_ID = -18;

	public static final int DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID = -29;
	public static final int DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID = -30;

	public static final String PLAY_IMAGE_DRAWABLE = "video_play";
	public static final String PAUSE_IMAGE_DRAWABLE = "video_pause";
	public static final String REPLAY_IMAGE_DRAWABLE = "video_replay";
	public static final String BACK_IMAGE_DRAWABLE = "browser_back";
	public static final String FORWARD_IMAGE_DRAWABLE = "browser_forward";
	public static final String RELOAD_IMAGE_DRAWABLE = "browser_reload";
	public static final String EXTERNAL_IMAGE_DRAWABLE = "browser_external";
	public static final String SKIP_IMAGE_DRAWABLE = "skip";
	public static final String BAR_IMAGE_DRAWABLE = "bar";
	public static final String CLOSE_BUTTON_NORMAL_IMAGE_DRAWABLE = "close_button_normal";
	public static final String CLOSE_BUTTON_PRESSED_IMAGE_DRAWABLE = "close_button_pressed";

	private static HashMap<Integer, Drawable> sResources = new HashMap<Integer,Drawable>();

	private Handler mHandler;
	private HashMap<Integer, Drawable> mResources = new HashMap<Integer,Drawable>();

	public static Drawable getDefaultResource(int resId) {
		return sResources.get(resId);
	}

	public static Drawable getDefaultSkipButton(Context ctx){
		return buildDrawable(ctx, SKIP_IMAGE_DRAWABLE);
	}
	
	public static boolean resourcesInstalled(Context ctx) {
		boolean result = false;
		String[] files = ctx.fileList();
		for (int i = 0; i < files.length; i++) {
			if (VERSION.equals(files[i])) {
				Log.d("Resources already installed");
				return true;
			}
		}
		return result;
	}

	public static long getInstalledVersion(Context ctx) {
		long result = -1;
		FileInputStream in = null;
		try {
			in = ctx.openFileInput(VERSION);
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			String version = reader.readLine();
			result = Long.valueOf(version).longValue();
		} catch (Exception e) {

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {

				}
			}
		}
		Log.d("Resources installed version:" + result);
		return result;
	}

	public static void saveInstalledVersion(Context ctx, long version) {
		FileOutputStream out = null;
		try {
			out = ctx.openFileOutput(VERSION, Context.MODE_PRIVATE);
			OutputStreamWriter osr = new OutputStreamWriter(out, "UTF-8");
			osr.write(String.valueOf(version));
			osr.flush();
		} catch (Exception e) {

		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {

				}
			}
		}
	}

	public void releaseInstance(){
		Iterator<Entry<Integer, Drawable>> it = mResources.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Drawable> pairsEntry = (Entry<Integer, Drawable>)it.next();
			it.remove();
			BitmapDrawable d = (BitmapDrawable) pairsEntry.getValue();

		}
		assert(mResources.size()==0);
		System.gc();
	}

	private static void initDefaultResource(Context ctx, int resource) {
		switch (resource) {
		case DEFAULT_PLAY_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_PLAY_IMAGE_RESOURCE_ID, PLAY_IMAGE_DRAWABLE);
			break;
		case DEFAULT_PAUSE_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_PAUSE_IMAGE_RESOURCE_ID, PAUSE_IMAGE_DRAWABLE);
			break;
		case DEFAULT_REPLAY_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_REPLAY_IMAGE_RESOURCE_ID, REPLAY_IMAGE_DRAWABLE);
			break;
		case DEFAULT_BACK_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_BACK_IMAGE_RESOURCE_ID, BACK_IMAGE_DRAWABLE);
			break;
		case DEFAULT_FORWARD_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_FORWARD_IMAGE_RESOURCE_ID, FORWARD_IMAGE_DRAWABLE);
			break;
		case DEFAULT_RELOAD_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_RELOAD_IMAGE_RESOURCE_ID, RELOAD_IMAGE_DRAWABLE);
			break;
		case DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID, EXTERNAL_IMAGE_DRAWABLE);
			break;
		case DEFAULT_SKIP_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_SKIP_IMAGE_RESOURCE_ID, SKIP_IMAGE_DRAWABLE);
			break;
		case DEFAULT_TOPBAR_BG_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_TOPBAR_BG_RESOURCE_ID, BAR_IMAGE_DRAWABLE);
			break;
		case DEFAULT_BOTTOMBAR_BG_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_BOTTOMBAR_BG_RESOURCE_ID, BAR_IMAGE_DRAWABLE);
			break;
		case DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID,
					CLOSE_BUTTON_NORMAL_IMAGE_DRAWABLE);
			break;
		case DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID,
					CLOSE_BUTTON_PRESSED_IMAGE_DRAWABLE);
			break;
		}
	}

	private static void registerImageResource(Context ctx, int resId,
											  String drawableName) {
		Drawable d = buildDrawable(ctx, drawableName);
		if (d != null) {
			sResources.put(resId, d);
		} else {
			Log.i("registerImageResource: drawable was null " + drawableName);
			Log.i("Context was " + ctx);
		}
	}

	private static Drawable buildDrawable(Context ctx, String drawableName) {
		try {
			int resourceId = ctx.getResources().getIdentifier(drawableName, "drawable",
					BuildConfig.APPLICATION_ID);
			if (resourceId == 0) {
				resourceId = ctx.getResources().getIdentifier(drawableName, "drawable",
						ctx.getApplicationContext().getPackageName());
			}

			Bitmap b = BitmapFactory.decodeResource(ctx.getResources(), resourceId);
			if (b != null) {

				DisplayMetrics m = ctx.getResources().getDisplayMetrics();
				int w = b.getWidth();
				int h = b.getHeight();
				int imageWidth = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, w, m);
				int imageHeight = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, h, m);
				if ((imageWidth != w) || (imageHeight != h)) {
					b = Bitmap.createScaledBitmap(b, imageWidth, imageHeight,
							false);
				}
				return new BitmapDrawable(ctx.getResources(), b);
			} else {
				throw new FileNotFoundException();
			}
		} catch (Exception e) {
			Log.i("ResourceManager cannot find resource " + drawableName);
		}
		return null;
	}

	public static boolean isDownloading() {
		return sDownloading;
	}

	public static void cancel() {
		sCancel = true;
		if (sDownloadGet != null) {
			sDownloadGet.abort();
			sDownloadGet = null;
		}

		sResources.clear();
	}

	public ResourceManager(Context ctx, Handler h) {

		mHandler = h;
	}

	public void fetchResource(Context ctx, String url, int resourceId) {
		if (sResources.get(resourceId) == null) {
			new FetchImageTask(ctx, url, resourceId).execute();
		}
	}

	public boolean containsResource(int resourceId) {
		return (mResources.get(resourceId) != null || mResources
				.get(resourceId) != null);
	}

	public Drawable getResource(Context ctx, int resourceId) {
		BitmapDrawable d;
		d = (BitmapDrawable) mResources.get(resourceId);
		if(d!=null){
			return d;
		}
		return ResourceManager.getStaticResource(ctx, resourceId);
	}

	public static Drawable getStaticResource(Context ctx, int resourceId) {
		BitmapDrawable d = (BitmapDrawable) sResources.get(resourceId);
		if (d == null || d.getBitmap().isRecycled()) {

			initDefaultResource(ctx, resourceId);

			d = (BitmapDrawable) sResources.get(resourceId);
		}
		return d;
	}
	
	private class FetchImageTask extends AsyncTask<Void, Void, Boolean> {
		String mUrl;
		int mResourceId;
		Context mContext;

		public FetchImageTask(Context ctx, String url, int resId) {
			mContext = ctx;
			mUrl = url;
			mResourceId = resId;
			Log.i("Fetching: "+mUrl);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.i("Fetched: "+mUrl);
			Message msg = mHandler.obtainMessage(RESOURCE_LOADED_MSG,
					mResourceId, 0);
			mHandler.sendMessage(msg);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Drawable d = null;

			Log.i("mUrl: "+mUrl);

			if ((mUrl != null) && (mUrl.length() > 0)) {
				d = fetchImage(mUrl);
			}
			if (d != null) {
				mResources.put(mResourceId, d);
				return true;
			}
			return false;
		}

		private Drawable fetchImage(String urlString) {
			try {
				URL url = new URL(urlString);
				InputStream is = (InputStream) url.getContent();
				Bitmap b = BitmapFactory.decodeStream(is);
				if (b != null) {
					DisplayMetrics m = mContext.getResources()
							.getDisplayMetrics();
					int w = b.getWidth();
					int h = b.getHeight();
					int imageWidth = (int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, w, m);
					int imageHeight = (int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, h, m);
					if ((imageWidth != w) || (imageHeight != h)) {
						b = Bitmap.createScaledBitmap(b, imageWidth,
								imageHeight, false);
					}
					return new BitmapDrawable(mContext.getResources(), b);
				}
			} catch (Exception e) {
				Log.e("Cannot fetch image:" + urlString, e);
			}
			return null;
		}

	}

}
