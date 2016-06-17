package com.playseeds.android.sdk.inappmessaging;

import android.content.Context;
import android.test.AndroidTestCase;

public class InAppMessageViewTest extends AndroidTestCase {

    public void testConstructor() throws Exception {
        InAppMessageResponse response = new InAppMessageResponse();
        InAppMessageView.BannerAdViewListener viewListener = new InAppMessageView.BannerAdViewListener() {
            @Override
            public void onLoad() {
            }

            @Override
            public void onClick() {
            }

            @Override
            public void onError() {
            }
        };

        Context context = getContext();
        InAppMessageView inAppMessageView = new InAppMessageView(context, response, 0, 0, false, viewListener);
        assertSame(viewListener, inAppMessageView.getAdListener());
        assertSame(response, inAppMessageView.getResponse());
    }
}
