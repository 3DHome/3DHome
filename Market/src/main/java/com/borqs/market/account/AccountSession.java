package com.borqs.market.account;

import com.borqs.information.IabManager;
import com.borqs.information.InformationDownloadService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AccountSession {
    
    public static final String SHARE_PREFERENCES_ACCOUNT_SETTING = "account_setting";
    public static final String SP_EXTRAS_TOKEN = "token";
    public static final String SP_EXTRAS_ID = "id";
    public static final String SP_EXTRAS_EMAIL = "email";
    public static final String SP_EXTRAS_NAME = "name";
    
    public static boolean isLogin = false;
    public static String account_id;
    public static String account_session;
    public static String account_email;
    public static String account_name;

    public static String getAccount_name() {
        return account_name;
    }

    public static void setAccount_name(String account_name) {
        AccountSession.account_name = account_name;
    }

    public static String getAccount_email() {
        return account_email;
    }

    public static void setAccount_email(String account_email) {
        AccountSession.account_email = account_email;
    }

    public static boolean isLogin() {
        return isLogin;
    }

    public static void setLogin(boolean isLogin) {
        AccountSession.isLogin = isLogin;
    }

    public static String getAccount_id() {
        return account_id;
    }

    public static void setAccount_id(String account_id) {
        AccountSession.account_id = account_id;
    }

    public static String getAccount_session() {
        return account_session;
    }

    public static void setAccount_session(String account_session) {
        AccountSession.account_session = account_session;
    }
    
    public static void loadAccount(Context context) {
        SharedPreferences sp = getAccountSharePreferences(context);
        String token = sp.getString(SP_EXTRAS_TOKEN, null);
        String userId = sp.getString(SP_EXTRAS_ID, null);
        String email = sp.getString(SP_EXTRAS_EMAIL, null);
        String name = sp.getString(SP_EXTRAS_NAME, null);
        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(userId)) {
            setAccount_id(null);
            setAccount_session(null);
            setAccount_email(null);
            setAccount_name(null);
            setLogin(false);
        }else {
            setAccount_id(userId);
            setAccount_session(token);
            setAccount_email(email);
            setAccount_name(name);
            setLogin(true);
            AccountObserver.login();
            
            IabManager.initErrorCount(context);
            Intent intent = new Intent(context, InformationDownloadService.class);
//            intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE);
            context.startService(intent);
        }
    }
    protected static void login(Context context,String userId,String token,String email,String name) {
        SharedPreferences sp = getAccountSharePreferences(context);
        SharedPreferences.Editor Editor = sp.edit();
        Editor.putString(SP_EXTRAS_ID, userId);
        Editor.putString(SP_EXTRAS_TOKEN, token);
        Editor.putString(SP_EXTRAS_EMAIL, email);
        Editor.putString(SP_EXTRAS_NAME, name);
        Editor.commit();
        setAccount_id(userId);
        setAccount_session(token);
        setAccount_email(email);
        setAccount_name(name);
        setLogin(true);
        AccountObserver.login();
        
        IabManager.initErrorCount(context);
        Intent intent = new Intent(context, InformationDownloadService.class);
//        intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE);
        context.startService(intent);
    }
    
    public static void logout(Context context) {
        SharedPreferences sp = getAccountSharePreferences(context);
        SharedPreferences.Editor Editor = sp.edit();
        Editor.remove(SP_EXTRAS_ID);
        Editor.remove(SP_EXTRAS_TOKEN);
        Editor.remove(SP_EXTRAS_EMAIL);
        Editor.remove(SP_EXTRAS_NAME);
        Editor.commit();
        setAccount_id(null);
        setAccount_session(null);
        setAccount_email(null);
        setAccount_name(null);
        setLogin(false);
        AccountObserver.logout();
    }
    
    private static SharedPreferences getAccountSharePreferences(Context context) {
        return context.getSharedPreferences(SHARE_PREFERENCES_ACCOUNT_SETTING, Context.MODE_PRIVATE);
    }
    
}
