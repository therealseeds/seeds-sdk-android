package ly.count.android.sdk.inappmessaging;

public final class Log {

	/*
	 * Enable logging DEBUG logs on a device: 
	 * adb shell setprop log.tag.ADSDK DEBUG
	 */

	public static final String TAG = "Countly IAM";
	public static final boolean LOG_AD_RESPONSES = false;

	public static boolean isLoggable(int logLevel) {
		return android.util.Log.isLoggable(TAG, logLevel);
	}

	public static void d(final String msg) {
		if (isLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg);
		}
	}

	public static void d(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg, tr);
		}
	}

	public static void e(final String msg) {
		if (isLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(TAG, msg);
		}
	}

	public static void e(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.ERROR)) {
			android.util.Log.w(TAG, msg, tr);
		}
	}

	public static void i(final String msg) {
		if (isLoggable(android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg);
		}
	}

	public static void i(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg, tr);
		}
	}

	public static void v(final String msg) {
		if (isLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg);
		}
	}

	public static void v(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg, tr);
		}
	}

	public static void w(final String msg) {
		if (isLoggable(android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg);
		}
	}

	public static void w(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg, tr);
		}
	}
}