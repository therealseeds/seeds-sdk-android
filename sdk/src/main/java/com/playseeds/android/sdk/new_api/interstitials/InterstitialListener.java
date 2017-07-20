package com.playseeds.android.sdk.new_api.interstitials;

public interface InterstitialListener {

    /**
     * Called when the {@link SeedsInterstitial} is loaded.
     *
     * @param seedsInterstitial The loaded {@link SeedsInterstitial}.
     */
    void onLoaded(SeedsInterstitial seedsInterstitial);

    /**
     * Called when the user clicked on the {@link SeedsInterstitial}.
     *
     * @param seedsInterstitial The clicked {@link SeedsInterstitial}.
     */
    void onClick(SeedsInterstitial seedsInterstitial);

    /**
     * Called when {@link SeedsInterstitial}, to what this listener is assigned, was shown.
     *
     * @param seedsInterstitial The shown {@link SeedsInterstitial}.
     */
    void onShown(SeedsInterstitial seedsInterstitial);

    /**
     * Called when {@link SeedsInterstitial}, to what this listener is assigned, was dismissed.
     *
     * @param seedsInterstitial The shown {@link SeedsInterstitial}.
     */
    void onDismissed(SeedsInterstitial seedsInterstitial);

    /**
     * Called when the request could not be executed due to some reasons.
     *
     * @param interstitialId The interstitialId of the interstitial, that caused the error.
     * @param exception The reason of the error.
     */
    void onError(String interstitialId, Exception exception);
}
