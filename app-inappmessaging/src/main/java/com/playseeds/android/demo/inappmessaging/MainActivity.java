package com.playseeds.android.demo.inappmessaging;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.playseeds.android.sdk.Seeds;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.demo.inappmessaging.R;

public class MainActivity extends Activity implements InAppMessageListener {

    private static String YOUR_SERVER = "http://staging.playseeds.com";
    private static String YOUR_APP_KEY = "71ac2900e9d31647d68d0ddc6f0aaf52611a612d";
    private static String messageId0 = "575f872a64bc1e5b0eca506f";
    private static String messageId1 = "5746851bb29ee753053a7c9a";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        Seeds.sharedInstance()
                .init(this, null, this, YOUR_SERVER, YOUR_APP_KEY)
                .setLoggingEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Seeds.sharedInstance().onStart();
    }

    @Override
    public void onStop()
    {
        Seeds.sharedInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void iamButtonClicked0(View view) {
        showInAppMessage(messageId0);
    }

    public void iamButtonClicked1(View view) {
        showInAppMessage(messageId1);
    }

    public void purchaseEventButtonClicked(View view) {
        Log.d("Main", "purchase button clicked");
        Seeds.sharedInstance().recordIAPEvent("item1", 0.99);
    }

    public void seedsPurchaseEventButtonClicked(View view) {
        Log.d("Main", "purchase button clicked");
        Seeds.sharedInstance().recordSeedsIAPEvent("item1", 0.99);
    }

    public void showInAppMessage(final String messageId) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (Seeds.sharedInstance().isInAppMessageLoaded(messageId)) {
                        Seeds.sharedInstance().showInAppMessage(messageId);


                    } else {
                        Seeds.sharedInstance().requestInAppMessage(messageId);
//                        Toast.makeText(AndroidLauncher.this, "InAppMessage loading...", Toast.LENGTH_LONG)
//                                .show();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @Override
    public void inAppMessageClicked(String messageId, InAppMessage inAppMessage) {
        Toast.makeText(this, "inAppMessageClicked(messageId = " + messageId + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageClosed(String messageId, InAppMessage inAppMessage, boolean completed) {
        Toast.makeText(this, "inAppMessageClosed(completed = " + completed + ", messageId = " + messageId + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageLoadSucceeded(String messageId, InAppMessage inAppMessage) {
        Toast.makeText(this, "inAppMessageLoadSucceeded(messageId = " + messageId + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageShown(String messageId, InAppMessage inAppMessage, boolean succeeded) {
        Toast.makeText(this, "inAppMessageShown(succeeded = " + succeeded + ", messageId = " + messageId + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noInAppMessageFound(String messageId) {
        Toast.makeText(this, "noInAppMessageFound(messageId = " + messageId + ")", Toast.LENGTH_LONG).show();
    }

    IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            Seeds.sharedInstance()
                    .init(MainActivity.this, mService, MainActivity.this, YOUR_SERVER, YOUR_APP_KEY)
                    .setLoggingEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
}
