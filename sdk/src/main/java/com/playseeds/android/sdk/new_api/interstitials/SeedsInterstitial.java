package com.playseeds.android.sdk.new_api.interstitials;

import android.support.annotation.Nullable;

/**
 * Represents the wrapper for the interstitial with info data.
 *
 * @see InterstitialListener
 */
public class SeedsInterstitial {

    public static final String NO_CONTEXT = "";

    private String interstitialId;

    @Nullable
    private String context = NO_CONTEXT;

    private String productId = "";

    public SeedsInterstitial(String interstitialId) {
        this.interstitialId = interstitialId;
    }

    public SeedsInterstitial(String interstitialId, String productId) {
        this.interstitialId = interstitialId;
        this.productId = productId == null ? "" : productId;
    }

    public SeedsInterstitial(String interstitialId, @Nullable String context, String productId) {
        this.interstitialId = interstitialId;
        this.context = context;
        this.productId = productId;
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

    /**
     * Returns the ID of the product that is clicked on the interstitial with {@link SeedsInterstitial#interstitialId}.
     *
     * @return the ID of the product that is supposed to be bought. May be empty when no product ID
     * is assigned to the interstitial.
     */
    public String getProductId() {
        return productId;
    }
}
