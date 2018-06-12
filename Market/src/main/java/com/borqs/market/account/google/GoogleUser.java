package com.borqs.market.account.google;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class GoogleUser {

    public String id;
    public String name;
    public String link;
    public String picture;
    public String gender;
    public String birthday;
    public String email;
    public String token;
    
    public GoogleUser(String response) {
        if (TextUtils.isEmpty(response))
            return;
        JSONObject obj;
        try {
            obj = new JSONObject(response);
            id = obj.optString(GoogleConstants.FIELD_USER_INFO_ID);
            name = obj.optString(GoogleConstants.FIELD_USER_INFO_NAME);
            link = obj.optString(GoogleConstants.FIELD_USER_INFO_LINK);
            picture = obj.optString(GoogleConstants.FIELD_USER_INFO_PICTURE);
            gender = obj.optString(GoogleConstants.FIELD_USER_INFO_GENDER);
            birthday = obj.optString(GoogleConstants.FIELD_USER_INFO_BIRTHDAY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
