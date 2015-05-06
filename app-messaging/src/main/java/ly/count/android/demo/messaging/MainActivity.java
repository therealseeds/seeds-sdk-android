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
import ly.count.android.sdk.inappmessaging.Ad;
import ly.count.android.sdk.inappmessaging.AdListener;
import ly.count.android.sdk.inappmessaging.AdManager;
import ly.count.android.sdk.messaging.CountlyMessaging;
import ly.count.android.sdk.messaging.Message;

public class MainActivity extends Activity implements AdListener {

    private static String YOUR_SERVER = "http://ec2-52-7-34-112.compute-1.amazonaws.com/";
    private static String YOUR_APP_KEY = "b1eed6c8bf769ffded7332893b62b8e6f4d73a32";

    private static final String MOB_FOX_PUB_ID = "86e0aa6e7fbd2cdb02ec15e338d6b722";
    private static final String MOB_FOX_AD_URL = "http://my.mobfox.com/request.php";

    private BroadcastReceiver messageReceiver;

    private Button iamButton;
    private AdManager manager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iamButton = (Button) findViewById(R.id.iamButton);
        manager = new AdManager(this, MOB_FOX_AD_URL, MOB_FOX_PUB_ID, false);
        manager.setListener(this);

        /** You should use cloud.count.ly instead of YOUR_SERVER for the line below if you are using Countly Cloud service */
        Countly.sharedInstance()
                .init(this, YOUR_SERVER, YOUR_APP_KEY)
                .initMessaging(this, MainActivity.class, "GCM_PROJECT_ID", Countly.CountlyMessagingMode.TEST);
//                .setLocation(LATITUDE, LONGITUDE);
//                .setLoggingEnabled(true);

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
                    if (manager.isAdLoaded()) {
                        manager.showAd();


                    } else {
                        manager.requestAd();
//                        Toast.makeText(AndroidLauncher.this, "Ad loading...", Toast.LENGTH_LONG)
//                                .show();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }


    @Override
    public void adClicked() {

    }

    @Override
    public void adClosed(Ad ad, boolean completed) {

    }

    @Override
    public void adLoadSucceeded(Ad ad) {

    }

    @Override
    public void adShown(Ad ad, boolean succeeded) {

    }

    @Override
    public void noAdFound() {

    }
}
