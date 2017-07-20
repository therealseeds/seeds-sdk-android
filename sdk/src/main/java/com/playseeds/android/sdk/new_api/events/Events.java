package com.playseeds.android.sdk.new_api.events;

import android.support.annotation.Nullable;

import com.playseeds.android.sdk.Event;

/**
 * An interface, that is used in the {@link com.playseeds.android.sdk.Seeds} to wrap the block of the
 * functionality, that is provided by the Seeds SDK.
 */
public interface Events {

    /**
     * Logs the passed event to the server.
     *
     * @param event is the event to log.
     */
    void logEvent(Event event);

    /**
     * Logs the user data to the server.
     *
     * @param info is the user data to log.
     */
    void logUser(UserInfo info);

    /**
     * Used to log any IAP purchase event to keep the Seeds' statistic mechanisms up-to-date
     *
     * @param key key of the product
     * @param price price of the product
     * @param transactionId id of the transaction. Might be null.
     */
    void logIAPPayment(String key, double price, @Nullable String transactionId);

    /**
     * Used to log IAP purchase event with additional Seeds fee to keep the Seeds' statistic
     * mechanisms up-to-date
     *
     * @param key key of the product
     * @param price price of the product
     * @param transactionId id of the transaction. Might be null.
     */
    void logSeedsIAPPayment(String key, double price, @Nullable String transactionId);
}
