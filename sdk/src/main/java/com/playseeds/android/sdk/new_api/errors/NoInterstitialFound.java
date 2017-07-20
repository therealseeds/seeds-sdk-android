package com.playseeds.android.sdk.new_api.errors;

public class NoInterstitialFound extends Exception {

    public NoInterstitialFound(String id) {
        super(String.format("Interstitial with id %s was not found", id));
    }
}
