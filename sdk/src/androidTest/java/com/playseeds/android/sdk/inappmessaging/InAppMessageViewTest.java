package com.playseeds.android.sdk.inappmessaging;

import android.content.Context;
import android.test.AndroidTestCase;

public class InAppMessageViewTest extends AndroidTestCase {
    InAppMessageView inAppMessageView;
    InAppMessageResponse response;
    InAppMessageView.BannerAdViewListener viewListener;

    public void setUp() throws Exception {
        Context context = getContext();
        response = new InAppMessageResponse();
        viewListener = new InAppMessageView.BannerAdViewListener() {
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

        inAppMessageView = new InAppMessageView(context, response, 0, 0, false, viewListener);
    }

    public void testConstructor() throws Exception {
        assertSame(viewListener, inAppMessageView.getAdListener());
        assertSame(response, inAppMessageView.getResponse());
    }
}
