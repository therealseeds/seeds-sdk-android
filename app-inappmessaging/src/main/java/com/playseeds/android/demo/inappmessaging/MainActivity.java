package com.playseeds.android.demo.inappmessaging;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.playseeds.android.sdk.Countly;
import com.playseeds.android.sdk.DeviceId;

//import ly.count.android.sdk.messaging.CountlyMessaging;
//import ly.count.android.sdk.messaging.Message;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.android.sdk.inappmessaging.InAppMessageManager;
import com.playseeds.demo.inappmessaging.R;



public class MainActivity extends Activity implements InAppMessageListener {

    private static String YOUR_SERVER = "http://devdashboard.playseeds.com"; // don't include trailing slash

    private static String YOUR_APP_KEY = "aa1fd1f255b25fb89b413f216f11e8719188129d"; // "f04df490114e12ccd358d28b84920faf788104f2"; //"test"; //


    private Button iamButton;
    private Button purchaseEventButton;
    private InAppMessageManager manager;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iamButton = (Button) findViewById(R.id.iamButton);
        purchaseEventButton = (Button) findViewById(R.id.purchaseEventButton);

        Countly.sharedInstance()
                .init(this, YOUR_SERVER, YOUR_APP_KEY, null, DeviceId.Type.ADVERTISING_ID)
                .initInAppMessaging(this)
                .setLoggingEnabled(true);

        manager = InAppMessageManager.sharedInstance();
        manager.setListener(this);
        manager.requestInAppMessage(); // preload Ad

        Countly.sharedInstance().recordEvent("test", 1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Countly.sharedInstance().recordEvent("test2", 1, 2);
            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Countly.sharedInstance().recordEvent("test3");
            }
        }, 10000);

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Countly.sharedInstance().onStart();
    }

    @Override
    public void onStop()
    {
        Countly.sharedInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        /** Register for broadcast action if you need to be notified when Countly message received */
//        messageReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Message message = intent.getParcelableExtra(CountlyMessaging.BROADCAST_RECEIVER_ACTION_MESSAGE);
//                Log.i("CountlyActivity", "Got a message with data: " + message.getData());
//            }
//        };
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(CountlyMessaging.getBroadcastAction(getApplicationContext()));
//        registerReceiver(messageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(messageReceiver);
    }

    public void iamButtonClicked(View view) {
        showInAppMessage();
    }

    public void purchaseEventButtonClicked(View view) {
        Log.d("Main", "purchase button clicked");
        Countly.sharedInstance().recordIAPEvent("item1", 0.99);
    }

    public void showInAppMessage() {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (manager.isInAppMessageLoaded()) {
                        manager.showInAppMessage();


                    } else {
                        manager.requestInAppMessage();
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
    }

    @Override
    public void inAppMessageClosed(InAppMessage inAppMessage, boolean completed) {

    }

    @Override
    public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {
        Toast.makeText(this, "InAppMessage loaded", Toast.LENGTH_LONG)
                                .show();
    }

    @Override
    public void inAppMessageShown(InAppMessage inAppMessage, boolean succeeded) {
    }

    @Override
    public void noInAppMessageFound() {

    }


}
