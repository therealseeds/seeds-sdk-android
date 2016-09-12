package com.playseeds.android.sdk;
import com.google.gson.JsonElement;

public interface IUserBehaviorListener {
    void onUserBehaviorResponse(String errorMessage, JsonElement result);
}
