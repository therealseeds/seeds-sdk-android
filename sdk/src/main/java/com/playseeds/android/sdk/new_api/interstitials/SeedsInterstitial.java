package com.playseeds.android.sdk.new_api.interstitials;

import android.support.annotation.Nullable;

/**
 * Represents the wrapper for the interstitial with info data.
 *
 * @see InterstitialListener
 */
public class SeedsInterstitial {

    public static final String NO_CONTEXT = "";

    /**
     * Id of the interstitial
     */
    private String interstitialId;

    /**
     * Context, that represent the current interstitial.
     * This field is {@link SeedsInterstitial#NO_CONTEXT} by default.
     */
    @Nullable
    private String context = NO_CONTEXT;

    public SeedsInterstitial(String interstitialId) {
        this.interstitialId = interstitialId;
    }

    public SeedsInterstitial(String interstitialId, @Nullable String context) {

        this.interstitialId = interstitialId;
        this.context = context == null ? NO_CONTEXT : context;
    }

    /**
     * Returns the interstitialId of the interstitial.
     *
     * @return the interstitialId of the interstitial.
     */
    public String getInterstitialId() {
        return interstitialId;
    }

    /**
     * Returns the context of the interstitial. Might be null.
     *
     * @return the context of the interstitial.
     */
    @Nullable
    public String getContext() {
        return context;
    }
}
