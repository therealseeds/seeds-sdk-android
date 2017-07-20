package com.playseeds.android.sdk.new_api.interstitials;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * An interface, that is used in the {@link com.playseeds.android.sdk.Seeds} to wrap the block of the
 * functionality, that is provided by the Seeds SDK.
 */
public interface Interstitials {

    /**
     * Downloads the required interstitial.
     *
     * @param interstitialId the interstitialId of the interstitial.
     */
    void fetch(String interstitialId);

    /**
     * Shows the requested interstitial. At first, will look for the interstitialId and then for the context in
     * the downloaded interstitials.
     *
     * @param interstitialId the interstitialId of the interstitial to show.
     * @param context the context of the interstitial to show.
     */
    void show(String interstitialId, String context);

    /**
     * Check by interstitialId if the requested interstitial is already loaded.
     *
     * @param interstitialId the interstitialId to look for.
     * @return true if the interstitial is loaded, false otherwise.
     */
    boolean isLoaded(String interstitialId);

    /**
     * Set the global {@link InterstitialListener}.
     *
     * @param listener the listener that is being set.
     */
    void setListener(InterstitialListener listener);
}
