package com.playseeds.android.demo.inappmessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.playseeds.android.sdk.IInAppMessageShowCountListener;
import com.playseeds.android.sdk.Seeds;

import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.android.sdk.new_api.interstitials.InterstitialListener;
import com.playseeds.android.sdk.new_api.interstitials.SeedsInterstitial;
import com.playseeds.demo.inappmessaging.R;



public class MainActivity extends Activity implements InterstitialListener {

    private static String SEEDS_IAP_EVENT_KEY = "TestSeedsPurchase";
    private static String NORMAL_IAP_EVENT_KEY = "TestNormalPurchase";

    private static String APP_LAUNCH_INTERSTITIAL_ID = "example-interstitial";
    private static String PURCHASE_INTERSTITIAL_ID = "example-interstitial";
    private static String SHARING_INTERSTITIAL_ID = "example-interstitial";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Seeds.interstitials().setListener(this);

        // Preload all interstitials at once
        Seeds.interstitials().fetch(APP_LAUNCH_INTERSTITIAL_ID);
        Seeds.interstitials().fetch(PURCHASE_INTERSTITIAL_ID);
        Seeds.interstitials().fetch(SHARING_INTERSTITIAL_ID);
    }

    public void startSocialGoodPurchase(View view) {
        showInterstitial(PURCHASE_INTERSTITIAL_ID, "in-store");
    }

    public void startNormalPurchase(View view) {
        final Context context = this;
        triggerPayment(new Runnable() {
            @Override
            public void run() {
                // Use recordIAPEvent instead of recordSeedsIAPEvent
                Seeds.events().logIAPPayment(NORMAL_IAP_EVENT_KEY, 4.99, null);
                Toast.makeText(context, "Event " + NORMAL_IAP_EVENT_KEY + " tracked as a non-Seeds purchase",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showInterstitial(final String messageId, final String context) {

        if (Seeds.interstitials().isLoaded(messageId)) {

            Seeds.interstitials().show(messageId, context);
        } else {

            Seeds.interstitials().fetch(messageId);
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

    @Override
    public void onLoaded(SeedsInterstitial seedsInterstitial) {

        if (seedsInterstitial.getInterstitialId().equals(APP_LAUNCH_INTERSTITIAL_ID)) {
            showInterstitial(APP_LAUNCH_INTERSTITIAL_ID, "app startup");
        }

        Toast.makeText(getBaseContext(), "inAppMessageLoadSucceeded(interstitialId = " + seedsInterstitial.getInterstitialId() + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(SeedsInterstitial seedsInterstitial) {

        final Context context = this;
        if (seedsInterstitial.getInterstitialId().equals(PURCHASE_INTERSTITIAL_ID)) {
            triggerPayment(new Runnable() {
                @Override
                public void run() {
                    // Use recordSeedsIAPEvent instead of recordIAPEvent
                    Seeds.events().logSeedsIAPPayment(SEEDS_IAP_EVENT_KEY, 0.99, null);
                    Toast.makeText(context, "Event " + SEEDS_IAP_EVENT_KEY + " tracked as a Seeds purchase",
                            Toast.LENGTH_SHORT).show();

                    showInterstitial(SHARING_INTERSTITIAL_ID, "after-purchase");
                }
            });
        } else if (seedsInterstitial.getInterstitialId().equals(APP_LAUNCH_INTERSTITIAL_ID)) {
            Toast.makeText(context, "App launch interstitial button clicked", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "inAppMessageClicked(interstitialId = " + seedsInterstitial.getInterstitialId() + ")", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onShown(SeedsInterstitial seedsInterstitial) {

        Toast.makeText(this, "inAppMessageShown(interstitialId = " + seedsInterstitial.getInterstitialId() + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDismissed(SeedsInterstitial seedsInterstitial) {

        Toast.makeText(this, "inAppMessageDismissed(interstitialId = " + seedsInterstitial.getInterstitialId() + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String interstitialId, Exception exception) {

        Toast.makeText(this, exception.getMessage() + " at interstitial with " + interstitialId + " id", Toast.LENGTH_SHORT).show();
    }
}
