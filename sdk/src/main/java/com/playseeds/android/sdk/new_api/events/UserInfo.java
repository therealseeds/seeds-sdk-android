package com.playseeds.android.sdk.new_api.events;

import android.util.Log;

import com.playseeds.android.sdk.Event;
import com.playseeds.android.sdk.Seeds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

/**
 *
 */
public class UserInfo extends Event {

    public static final String EVENT_NAME = "User Info";

    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ORGANIZATION = "organization";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PICTURE = "picture";
    private static final String KEY_PICTURE_PATH = "picturePath";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BYEAR = "byear";

    private String name;
    private String username;
    private String email;
    private String organization;
    private String phone;
    private String picture;
    private String picturePath;
    private String gender;

    private int byear;

    public UserInfo(){
        super.eventName = EVENT_NAME;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPhone() {
        return phone;
    }

    public String getPicture() {
        return picture;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public String getGender() {
        return gender;
    }

    public int getByear() {
        return byear;
    }

    public void setName(String name) {
        this.name = name;

        putAttribute(KEY_NAME, name);
    }

    public void setUsername(String username) {
        this.username = username;

        putAttribute(KEY_USERNAME, username);
    }

    public void setEmail(String email) {
        this.email = email;

        putAttribute(KEY_EMAIL, email);
    }

    public void setOrganization(String organization) {
        this.organization = organization;

        putAttribute(KEY_ORGANIZATION, organization);
    }

    public void setPhone(String phone) {
        this.phone = phone;

        putAttribute(KEY_PHONE, phone);
    }

    public void setPicture(String picture) {
        this.picture = picture;

        putAttribute(KEY_PICTURE, picture);
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;

        putAttribute(KEY_PICTURE_PATH, picturePath);
    }

    public void setGender(String gender) {
        this.gender = gender;

        putAttribute(KEY_GENDER, gender);
    }

    public void setByear(int byear) {
        this.byear = byear;

        putAttribute(KEY_BYEAR, String.valueOf(byear));
    }

    /**
     * Returns &user_details= prefixed url to add to request data when making request to server
     * @return a String user_details url part with provided user data
     */
    @Deprecated
    public String getDataForRequest(){
        final JSONObject json = toJSON();
        if(json != null){
            String result = json.toString();

            try {
                result = java.net.URLEncoder.encode(result, "UTF-8");

                if(result != null && !result.equals("")){
                    result = "&user_details="+result;
                    if(picturePath != null)
                        result += "&" + KEY_PICTURE_PATH + "="+java.net.URLEncoder.encode(picturePath, "UTF-8");
                }
                else{
                    result = "";
                    if(picturePath != null)
                        result += "&user_details&" + KEY_PICTURE_PATH + "=" + java.net.URLEncoder.encode(picturePath, "UTF-8");
                }
            } catch (UnsupportedEncodingException ignored) {
                // should never happen because Android guarantees UTF-8 support
            }

            if(result != null)
                return result;
        }

        return "";
    }

    @Deprecated
    JSONObject toJSON() {
        final JSONObject json = new JSONObject();

        try {
            if (name != null)
                if(name.isEmpty())
                    json.put(KEY_NAME, JSONObject.NULL);
                else
                    json.put(KEY_NAME, name);
            if (username != null)
                if(username.isEmpty())
                    json.put(KEY_USERNAME, JSONObject.NULL);
                else
                    json.put(KEY_USERNAME, username);
            if (email != null)
                if(email.isEmpty())
                    json.put(KEY_EMAIL, JSONObject.NULL);
                else
                    json.put(KEY_EMAIL, email);
            if (organization != null)
                if(organization.isEmpty())
                    json.put(KEY_ORGANIZATION, JSONObject.NULL);
                else
                    json.put(KEY_ORGANIZATION, organization);
            if (phone != null)
                if(phone.isEmpty())
                    json.put(KEY_PHONE, JSONObject.NULL);
                else
                    json.put(KEY_PHONE, phone);
            if (picture != null)
                if(picture.isEmpty())
                    json.put(KEY_PICTURE, JSONObject.NULL);
                else
                    json.put(KEY_PICTURE, picture);
            if (gender != null)
                if(gender.isEmpty())
                    json.put(KEY_GENDER, JSONObject.NULL);
                else
                    json.put(KEY_GENDER, gender);
            if (byear != 0)
                if(byear > 0)
                    json.put(KEY_BYEAR, byear);
                else
                    json.put(KEY_BYEAR, JSONObject.NULL);
        } catch (JSONException e) {

            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.w(Seeds.TAG, "Got exception converting the UserData to JSON", e);
            }
        }

        return json;
    }

    @Deprecated
    public String getPicturePathFromQuery(URL url){
        String query = url.getQuery();
        String[] pairs = query.split("&");
        String ret = "";
        if(url.getQuery().contains(KEY_PICTURE_PATH)){
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if(pair.substring(0, idx).equals(KEY_PICTURE_PATH)){
                    try {
                        ret = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        ret = "";
                    }
                    break;
                }
            }
        }
        return ret;
    }
}
