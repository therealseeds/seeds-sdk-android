package com.playseeds.android.sdk;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

public class SeedsTestsRunner extends RobolectricGradleTestRunner {

    public SeedsTestsRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        final String manifestProperty = "src/test/AndroidManifest.xml";
        final String resProperty = "src/main/res";
        final String assetsProperty = "src/main/assets";
        return new AndroidManifest(Fs.fileFromPath(manifestProperty), Fs.fileFromPath(resProperty),
                Fs.fileFromPath(assetsProperty));
    }
}
