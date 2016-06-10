package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;

import com.playseeds.android.sdk.DeviceId;
import com.playseeds.android.sdk.Seeds;

import java.util.Date;
import java.util.HashMap;

public class InAppMessageManagerTest extends AndroidTestCase {
    InAppMessageResponse inAppMessageResponse;
    testInAppMessageListener inAppMessageListener;
    HashMap<Long, InAppMessageManager> runningAds;
    Long timeStamp;

    public void setUp() throws Exception {
        timeStamp = new Date().getTime();
        inAppMessageResponse = new InAppMessageResponse();
        inAppMessageListener = new testInAppMessageListener();
        runningAds = new HashMap<>();

        inAppMessageResponse.setTimestamp(timeStamp);
        runningAds.put(timeStamp, InAppMessageManager.sharedInstance());
        InAppMessageManager.sharedInstance().setRunningAds(runningAds);
        InAppMessageManager.sharedInstance().setListener(inAppMessageListener);
        Seeds.sharedInstance().init(getContext(), inAppMessageListener, "https://mobfox.com", "12345");
        Seeds.sharedInstance().setMessageVariantName("Test message");
        InAppMessageManager.sharedInstance().init(getContext(), "https://mobfox.com", "12345", "Nexus-XLR", DeviceId.Type.ADVERTISING_ID);
    }

    public void testNotifyInAppMessageClick() throws Exception {
        InAppMessageManager.notifyInAppMessageClick(inAppMessageResponse);
        assertTrue(inAppMessageListener.isClicked);
    }

    public void testCloseRunningInAppMessage() throws Exception {
        InAppMessageManager.closeRunningInAppMessage(inAppMessageResponse, false);
        synchronized (inAppMessageListener) {
            inAppMessageListener.wait(1000);
        }
        assertTrue(inAppMessageListener.isClosed);
    }

    public void testRequestInAppMessage_WhenBadRequest() throws Exception {
        InAppMessageManager.sharedInstance().requestInAppMessage();
        synchronized (inAppMessageListener) {
            inAppMessageListener.wait(1000);
        }
        assertTrue(inAppMessageListener.notFound);
    }

    public void testRequestInAppMessage() throws Exception {
        InAppMessageManager.sharedInstance().init(getContext(), "http://devdash.playseeds.com", "c30f02a55541cbe362449d29d83d777c125c8dd6", "Nexus-XLR", DeviceId.Type.ADVERTISING_ID);
        InAppMessageManager.sharedInstance().requestInAppMessage();
        synchronized (inAppMessageListener) {
            inAppMessageListener.wait(1000);
        }
        assertTrue(inAppMessageListener.isLoadSucceeded);
    }

    private class testInAppMessageListener implements InAppMessageListener {
        boolean isClicked = false;
        boolean isClosed = false;
        boolean isLoadSucceeded = false;
        boolean isShown = false;
        boolean notFound = false;

        @Override
        public void inAppMessageClicked() {
            isClicked = true;
        }

        @Override
        public void inAppMessageClosed(InAppMessage inAppmessage, boolean completed) {
            isClosed = true;
        }

        @Override
        public void inAppMessageShown(InAppMessage inAppMessage, boolean succeeded) {
            isShown = true;
        }

        @Override
        public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {
            isLoadSucceeded = true;
        }

        @Override
        public void noInAppMessageFound() {
            notFound = true;
        }
    }
}
