package com.playseeds.android.sdk;

import android.content.Intent;
import android.test.AndroidTestCase;

public class ReferrerReceiverTest extends AndroidTestCase {
    ReferrerReceiver receiver;

    public void setUp() throws Exception {
        receiver = new ReferrerReceiver();
    }

    public void testOnReceive() throws Exception {
        Intent intent = new Intent();
        intent.setAction("com.android.vending.INSTALL_REFERRER");
        intent.putExtra("referrer", "testReferrer");

        try {
            receiver.onReceive(getContext(), intent);
        } catch (Exception e) {
        }
    }
}
