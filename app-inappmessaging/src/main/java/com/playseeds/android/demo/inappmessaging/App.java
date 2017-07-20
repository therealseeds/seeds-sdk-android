package com.playseeds.android.demo.inappmessaging;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.playseeds.android.sdk.Seeds;

public class App extends MultiDexApplication {

    private static String SEEDS_APP_KEY = "1e1764151dfd60fb20affaa89813b42dd9e163fe";

    @Override
    public void onCreate() {
        super.onCreate();

        Seeds.init(getApplicationContext(), SEEDS_APP_KEY).setLoggingEnabled(true);
    }
}
