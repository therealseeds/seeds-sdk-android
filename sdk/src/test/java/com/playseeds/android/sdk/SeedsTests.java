package com.playseeds.android.sdk;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(SeedsTestsRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SeedsTests {
    public static final String SERVER = "http://devdash.playseeds.com";
    public static final String NO_ADS_APP_KEY = "ef2444ec9f590d24db5054fad8385991138a394b";
    public static final String UNLIMITED_ADS_APP_KEY = "c30f02a55541cbe362449d29d83d777c125c8dd6";

    @Test
    public void testSeedsCustomId() {
        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext( ), null,
                        SERVER, UNLIMITED_ADS_APP_KEY, "some fake");
    }

    @Test
    public void testSeedsCustomIdExplicit() {
        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext( ), null,
                        SERVER, UNLIMITED_ADS_APP_KEY, "some fake", DeviceId.Type.DEVELOPER_SUPPLIED);
    }

    @Test
    public void testSeedsUDIDUsage() {
        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext( ), null,
                        SERVER, UNLIMITED_ADS_APP_KEY, null, DeviceId.Type.OPEN_UDID);
    }

    @Test
    public void testSeedsAdIdUsage() {
        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext(), null,
                        SERVER, UNLIMITED_ADS_APP_KEY, null, DeviceId.Type.ADVERTISING_ID);
    }

    @Test
    public void testSeedInAppMessageLoadSucceeded() throws Exception {
        InAppMessageLoadListener listener = new InAppMessageLoadListener();

        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext(), listener,
                        SERVER, UNLIMITED_ADS_APP_KEY);
        Seeds.sharedInstance().requestInAppMessage();
        synchronized (listener) {
            listener.wait(50000);
        }
        Assert.assertTrue(listener.isLoadSucceeded);
    }

    @Test
    public void testSeedInAppMessageLoadFailed() throws Exception {
        InAppMessageLoadListener listener = new InAppMessageLoadListener();

        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext(), listener,
                        SERVER, NO_ADS_APP_KEY);
        Seeds.sharedInstance().requestInAppMessage();
        synchronized (listener) {
            listener.wait(50000);
        }
        Assert.assertFalse(listener.isLoadSucceeded);
    }

    private class InAppMessageLoadListener implements InAppMessageListener {
        boolean isLoadSucceeded = false;


        @Override
        public void inAppMessageClicked() {}

        @Override
        public void inAppMessageClosed(InAppMessage inAppMessage, boolean completed) {}

        @Override
        public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {
            isLoadSucceeded = true;
        }

        @Override
        public void inAppMessageShown(InAppMessage inAppMessage, boolean succeeded) {}

        @Override
        public void noInAppMessageFound() {
        }
    }
}
