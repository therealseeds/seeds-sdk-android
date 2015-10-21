package com.playseeds.android.demo.inappmessaging;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.playseeds.android.sdk.Seeds;
import com.playseeds.android.sdk.DeviceId;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.android.sdk.inappmessaging.InAppMessageManager;
import com.playseeds.demo.inappmessaging.R;



public class MainActivity extends Activity implements InAppMessageListener {

    //private static String YOUR_SERVER = "http://dash.playseeds.com"; // don't include trailing slash
    //private static String YOUR_APP_KEY = "test";
    private static String YOUR_SERVER = "http://devdash.playseeds.com";
    private static String YOUR_APP_KEY = "aa1fd1f255b25fb89b413f216f11e8719188129d";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Seeds.sharedInstance()
                .init(this, this, YOUR_SERVER, YOUR_APP_KEY)
                .setLoggingEnabled(true)
                .requestInAppMessage();
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

    public void iamButtonClicked(View view) {
        showInAppMessage();
    }

    public void purchaseEventButtonClicked(View view) {
        Log.d("Main", "purchase button clicked");
        Seeds.sharedInstance().recordIAPEvent("item1", 0.99);
    }

    public void seedsPurchaseEventButtonClicked(View view) {
        Log.d("Main", "purchase button clicked");
        Seeds.sharedInstance().recordSeedsIAPEvent("item1", 0.99);
    }


    public void showInAppMessage() {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (Seeds.sharedInstance().isInAppMessageLoaded()) {
                        Seeds.sharedInstance().showInAppMessage();


                    } else {
                        Seeds.sharedInstance().requestInAppMessage();
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
    public void inAppMessageClicked() {
        Toast.makeText(this, "inAppMessageClicked", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageClosed(InAppMessage inAppMessage, boolean completed) {
        Toast.makeText(this, "inAppMessageClosed(completed = " + completed + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {
        Toast.makeText(this, "inAppMessageLoadSucceeded", Toast.LENGTH_LONG).show();
    }

    @Override
    public void inAppMessageShown(InAppMessage inAppMessage, boolean succeeded) {
        Toast.makeText(this, "inAppMessageShown(succeeded = " + succeeded + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noInAppMessageFound() {
        Toast.makeText(this, "noInAppMessageFound", Toast.LENGTH_LONG).show();
    }


}
