package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;


import junit.framework.AssertionFailedError;

import java.io.InputStream;
import java.util.List;
import java.util.Map;


public class GeneralInAppMessageProviderTest extends AndroidTestCase {
    GeneralInAppMessageProvider generalInAppMessageProvider;

    public void setUp() throws Exception {
        generalInAppMessageProvider = new GeneralInAppMessageProvider();
    }

    public void testParseCountlyJsonWithNullValues() throws Exception {
        InputStream inputStream = null;
        Map<String, List<String>> header = null;

        try {
            generalInAppMessageProvider.parseCountlyJSON(inputStream, header);
        } catch (RequestException e) {

        }
    }

}
