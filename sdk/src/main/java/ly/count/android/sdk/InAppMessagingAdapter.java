package ly.count.android.sdk;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;


public class InAppMessagingAdapter {
    private static final String TAG = "InAppMessagingAdapter";
    private final static String MESSAGING_CLASS_NAME = "ly.count.android.sdk.inappmessaging.InAppMessageManager";

    public static boolean isInAppMessagingAvailable() {
        boolean messagingAvailable = false;
        try {
            Class.forName(MESSAGING_CLASS_NAME);
            messagingAvailable = true;
        }
        catch (ClassNotFoundException ignored) {}
        return messagingAvailable;
    }



    public static boolean init(Context context, String serverURL, String appKey, String deviceID, DeviceId.Type idMode) {
        try {
            final Class<?> cls = Class.forName(MESSAGING_CLASS_NAME);
            final Method method = cls.getMethod("init", Context.class, String.class, String.class, String.class, DeviceId.Type.class);
            final Method getInstance = cls.getMethod("sharedInstance");
            method.invoke(getInstance.invoke(null), context, serverURL, appKey, deviceID, idMode);
            return true;
        }
        catch (Throwable logged) {
            Log.e(TAG, "Couldn't init Countly InAppMessaging", logged);
            return false;
        }
    }
}
