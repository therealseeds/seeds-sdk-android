package com.playseeds.android.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import java.lang.ref.WeakReference;

/**
 * Streamlines the integration experience on Android v4.0 and up by
 * - automating the resolving of the billing service
 * - listening to onStart, onStop and onDestroy of the activity and informing Seeds SDK
 *   about the changes in application state
 */
public class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks {
    private Context context;
    private WeakReference<Activity> currentActivityWeakReference;
    private ServiceConnection mServiceConn;
    IInAppBillingService mService;

    private boolean isBound;

    ActivityLifecycleManager(Context context) {
        this.context = context;

        resolve();
    }

    private void resolve() {

        ((Application) context).registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivityWeakReference = new WeakReference<>(activity);

        if (currentActivityWeakReference.get() != null) {

            Log.d(Seeds.TAG, "On Activity start");
            bindService();
            Seeds.sharedInstance().onStart();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (currentActivityWeakReference.get() != null) {

            Log.d(Seeds.TAG, "On Activity stop");
            Seeds.sharedInstance().onStop();
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (currentActivityWeakReference.get() != null && mService != null) {

            Log.d(Seeds.TAG, "On Activity destroyed");
            if (isBound) {

                currentActivityWeakReference.get().unbindService(mServiceConn);
                isBound = false;
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    public Activity getValidViewContext(){

        return currentActivityWeakReference.get();
    }

    private void bindService(){

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                Log.d(Seeds.TAG, "On Billing Service connected");

                mService = IInAppBillingService.Stub.asInterface(service);

                Seeds.setBillingService(mService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        currentActivityWeakReference.get().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        isBound = true;
    }
}