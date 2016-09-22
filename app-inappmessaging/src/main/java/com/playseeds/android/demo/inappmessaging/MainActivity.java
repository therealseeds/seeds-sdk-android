package com.playseeds.android.demo.inappmessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.playseeds.android.sdk.Seeds;

import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.demo.inappmessaging.R;



public class MainActivity extends Activity implements InAppMessageListener {
    private static String SEEDS_SERVER = "https://dash.playseeds.com";
    private static String SEEDS_APP_KEY = "2db64b49085be463cade71ce22e6341d7f6bd901";

    private static String SEEDS_IAP_EVENT_KEY = "TestSeedsPurchase";
    private static String NORMAL_IAP_EVENT_KEY = "TestNormalPurchase";

    private static String APP_LAUNCH_INTERSTITIAL_ID = "57e362bead5957420e12083f";
    private static String PURCHASE_INTERSTITIAL_ID = "57e36337ad5957420e120842";
    private static String SHARING_INTERSTITIAL_ID = "57e36365ad5957420e120845";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Seeds.sharedInstance()
                .simpleInit(this, this, SEEDS_SERVER, SEEDS_APP_KEY)
                .setLoggingEnabled(true);

        // Preload all interstitials at once
        Seeds.sharedInstance().requestInAppMessage(APP_LAUNCH_INTERSTITIAL_ID);
        Seeds.sharedInstance().requestInAppMessage(PURCHASE_INTERSTITIAL_ID);
        Seeds.sharedInstance().requestInAppMessage(SHARING_INTERSTITIAL_ID);
    }


    public void startSocialGoodPurchase(View view) {
        showInterstitial(PURCHASE_INTERSTITIAL_ID, "in-store");
    }

    public void startNormalPurchase(View view) {
        triggerPayment(new Runnable() {
            @Override
            public void run() {
                // Use recordIAPEvent instead of recordSeedsIAPEvent
                Seeds.sharedInstance().recordIAPEvent(NORMAL_IAP_EVENT_KEY, 4.99);
            }
        });
    }

    public void showInterstitial(final String messageId, final String context) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (Seeds.sharedInstance().isInAppMessageLoaded(messageId)) {
                        Seeds.sharedInstance().showInAppMessage(messageId, context);

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

    public void triggerPayment(final Runnable paymentCompleted) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        paymentCompleted.run();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to confirm the in-app purchase? (Simulated payment)")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /*
     * InAppMessageListener implementation starts
     */

    @Override
    public void inAppMessageClicked(String messageId) {
        // Called when a user clicks the buy button. Handle the purchase here!
        // The interstitial is specified by messageId parameter

        if (messageId.equals(PURCHASE_INTERSTITIAL_ID)) {
            triggerPayment(new Runnable() {
                @Override
                public void run() {
                    // Use recordSeedsIAPEvent instead of recordIAPEvent
                    Seeds.sharedInstance().recordSeedsIAPEvent(SEEDS_IAP_EVENT_KEY, 0.99);
                    showInterstitial(SHARING_INTERSTITIAL_ID, "after-purchase");
                }
            });
        }

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

        // Show the app launch interstitial immediately after it's preloaded
        if (messageId.equals(APP_LAUNCH_INTERSTITIAL_ID)) {
            showInterstitial(APP_LAUNCH_INTERSTITIAL_ID, "app startup");
        }

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
        // Called when an interstitial couldn't be found or the preloading resulted in an error
        Toast.makeText(this, "noInAppMessageFound(messageId = " + messageId + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inAppMessageClickedWithDynamicPrice(String messageId, Double price) {
        // Called when an interstitial has multiple price options, and a user chooses one of them
        // Not needed in applications where the user can't choose the price in the Seeds interstitial
        Toast.makeText(this, "inAppMessageClickedWithDynamicPrice(messageId = " +
                messageId + ", price = " + price + ")", Toast.LENGTH_SHORT).show();
    }

    /*
     * InAppMessageListener implementation ends
     */
}
