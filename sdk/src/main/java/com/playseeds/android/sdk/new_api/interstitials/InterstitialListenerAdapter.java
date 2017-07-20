package com.playseeds.android.sdk.new_api.interstitials;

abstract public class InterstitialListenerAdapter implements InterstitialListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaded(SeedsInterstitial seedsInterstitial) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(SeedsInterstitial seedsInterstitial) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShown(SeedsInterstitial seedsInterstitial) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(String interstitialId, Exception exception) {}
}
