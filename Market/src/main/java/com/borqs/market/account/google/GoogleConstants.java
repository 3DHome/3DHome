package com.borqs.market.account.google;

public class GoogleConstants {

    public static final String APIS_GOOGLE_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";
    
    public static final String FIELD_USER_INFO_NAME = "name";
    public static final String FIELD_USER_INFO_GIVE_NAME = "given_name";
    public static final String FIELD_USER_INFO_ID = "id";
    public static final String FIELD_USER_INFO_GENDER = "gender";
    public static final String FIELD_USER_INFO_BIRTHDAY = "birthday";
    public static final String FIELD_USER_INFO_PICTURE = "picture";
    public static final String FIELD_USER_INFO_LINK = "link";
    
    public final static String G_PLUS_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.me";
    public final static String USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    public final static String PLUS_LOGIN_SCOPE = "https://www.googleapis.com/auth/plus.login";
    public final static String SCOPE = G_PLUS_SCOPE + " " + USERINFO_SCOPE
            + " " + PLUS_LOGIN_SCOPE;

    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    public static final String SHARE_PREFERENCES_ACCOUNT_GOOGLE = "google_account";
    public static final String SP_EXTRAS_TOKEN = "token";
    public static final String SP_EXTRAS_EMAIL = "email";
    public static final String SP_EXTRAS_RESULT_JSON = "result_json";
    
}
