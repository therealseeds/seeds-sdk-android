package ly.count.android.demo.messaging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ly.count.android.sdk.Countly;
import ly.count.android.sdk.DeviceId;

import ly.count.android.sdk.messaging.CountlyMessaging;
import ly.count.android.sdk.messaging.Message;

import ly.count.android.sdk.inappmessaging.InAppMessage;
import ly.count.android.sdk.inappmessaging.InAppMessageListener;
import ly.count.android.sdk.inappmessaging.InAppMessageManager;


public class MainActivity extends Activity implements InAppMessageListener {

    private static String YOUR_SERVER = "http://ec2-52-7-175-75.compute-1.amazonaws.com"; // don't include trailing slash
    private static String YOUR_APP_KEY = "aa1fd1f255b25fb89b413f216f11e8719188129d";
    private static String GCM_PROJECT_NUM = "1079042128983";


    private BroadcastReceiver messageReceiver;

    private Button iamButton;
    private InAppMessageManager manager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iamButton = (Button) findViewById(R.id.iamButton);

        /** You should use cloud.count.ly instead of YOUR_SERVER for the line below if you are using Countly Cloud service */
        Countly.sharedInstance()
                .init(this, YOUR_SERVER, YOUR_APP_KEY, null, DeviceId.Type.ADVERTISING_ID)
                .initMessaging(this, MainActivity.class, GCM_PROJECT_NUM, Countly.CountlyMessagingMode.TEST)
                .initInAppMessaging(this)
//                .setLocation(LATITUDE, LONGITUDE);
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

        /** Register for broadcast action if you need to be notified when Countly message received */
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = intent.getParcelableExtra(CountlyMessaging.BROADCAST_RECEIVER_ACTION_MESSAGE);
                Log.i("CountlyActivity", "Got a message with data: " + message.getData());
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(CountlyMessaging.getBroadcastAction(getApplicationContext()));
        registerReceiver(messageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }

    public void iamButtonClicked(View view) {
        showInAppMessage();
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
