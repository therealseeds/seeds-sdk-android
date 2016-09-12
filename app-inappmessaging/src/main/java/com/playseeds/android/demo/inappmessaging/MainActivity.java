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
import com.playseeds.android.sdk.IInAppMessageShowCountListener;
import com.playseeds.android.sdk.IInAppPurchaseCountListener;
import com.playseeds.android.sdk.Seeds;

import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.demo.inappmessaging.R;



public class MainActivity extends Activity implements InAppMessageListener {
    private static String YOUR_SERVER = "http://staging.playseeds.com";
    private static String YOUR_APP_KEY = "71ac2900e9d31647d68d0ddc6f0aaf52611a612d";
    private static String messageId0 = "575f872a64bc1e5b0eca506f";
    private static String messageId1 = "5746851bb29ee753053a7c9a";
    private static String iapEventKey = "test_iap_event";

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

        // Currently not possible to do in parallel!
        Seeds.sharedInstance().requestInAppMessage(messageId0);
        Seeds.sharedInstance().requestInAppMessage(messageId1);
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
        final Context context = this;

        Log.d("Main", "purchase button clicked");
        Seeds.sharedInstance().recordIAPEvent(iapEventKey, 0.99);

        // TODO: Separate button for running the user behaviour requests
        Seeds.sharedInstance().requestTotalInAppPurchaseCount(new IInAppPurchaseCountListener() {
            @Override
            public void onInAppPurchaseCount(String errorMessage, int purchasesCount, String _) {
                if (errorMessage != null) return;
                Toast.makeText(context, "requestTotalInAppPurchaseCount, purchaseCount = " + purchasesCount, Toast.LENGTH_SHORT).show();
            }
        });

        Seeds.sharedInstance().requestInAppPurchaseCount(iapEventKey, new IInAppPurchaseCountListener() {
            @Override
            public void onInAppPurchaseCount(String errorMessage, int purchasesCount, String key) {
                if (errorMessage != null) return;
                String text = "requestInAppPurchaseCount(iapEventKey=" + iapEventKey + "), purchaseCount = " + purchasesCount;
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });

        Seeds.sharedInstance().requestTotalInAppMessageShowCount(new IInAppMessageShowCountListener() {
            @Override
            public void onInAppMessageShowCount(String errorMessage, int showCount, String _) {
                if (errorMessage != null) return;
                Toast.makeText(context, "requestTotalInAppMessageShowCount, showCount = " + showCount, Toast.LENGTH_SHORT).show();
            }
        });

        Seeds.sharedInstance().requestInAppMessageShowCount(messageId0, new IInAppMessageShowCountListener() {
            @Override
            public void onInAppMessageShowCount(String errorMessage, int showCount, String message_id) {
                if (errorMessage != null) return;
                String text = "requestInAppMessageShowCount(iapEventKey=" + messageId0 + "), showCount = " + showCount;
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
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
                        Seeds.sharedInstance().showInAppMessage(messageId, "in-store");

                    } else {
                        // Skip the interstitial showing this time and try to reload the interstitial
                        Seeds.sharedInstance().requestInAppMessage(messageId);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @Override
    public void inAppMessageClicked(String messageId) {
        // Called when a user clicks the buy button. Handle the purchase here!
        // The interstitial is specified by messageId parameter
        Toast.makeText(this, "inAppMessageClicked(messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inAppMessageDismissed(String messageId) {
        // Called when a user dismisses the interstitial and no purchase is being made
        // The interstitial is specified by messageId parameter
        Toast.makeText(this, "inAppMessageDismissed(messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inAppMessageLoadSucceeded(String messageId) {
        // Called when an interstitial is loaded
        // The interstitial is specified by messageId parameter
        Toast.makeText(getBaseContext(), "inAppMessageLoadSucceeded(messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inAppMessageShown(String messageId, boolean succeeded) {
        // Called when an interstitial is successfully opened
        // The interstitial is specified by messageId parameter
        Toast.makeText(this, "inAppMessageShown(succeeded = " + succeeded + ", messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noInAppMessageFound(String messageId) {
        // Called when an interstitial couldn't be found or there is an error with loading it
        Toast.makeText(this, "noInAppMessageFound(messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inAppMessageClickedWithDynamicPrice(String messageId, Double price) {
        // Called when an interstitial has multiple price options, and a user chooses one of them
        // Not needed in applications where the user can't choose the price in the Seeds interstitial
        Toast.makeText(this, "inAppMessageClickedWithDynamicPrice(messageId = " +
                messageId + ", price = " + price + ")", Toast.LENGTH_SHORT).show();
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
