package com.borqs.market.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by IntelliJ IDEA. User: b608 Date: 11-10-17 Time: 下午12:23 To change
 * this template use File | Settings | File Templates.
 */
public class DataConnectionUtils {
    // private static final String TAG = "DataConnectionUtils";
    //
    // private static boolean mLastShowSavedMode;
    //
    // /**
    // * Global flag to assure not to show multiple alert dialog via repeatedly
    // invoking showConnectivityAlertDialog.
    // */
    // private static boolean mIsConnectivityAlertDialog = false;
    // private static boolean mIsPrivacyAlertDialog = false;

    public static boolean testValidConnection(Context context) {
        final NetworkInfo activeInfo = getActiveNetworkInfo(context);
        return (null != activeInfo);
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        return activeInfo;
    }

    // private static HashMap<String, String> mCachedServerUrl = new
    // HashMap<String, String>();
    // public static void cleanCurrentServerUrl() {
    // mCachedServerUrl.clear();
    // }
    //
    // private static final String API_HOST = "api_host";
    // public static String getCurrentApiHost(Context context) {
    // return getCurrentServerUrl(context, API_HOST);
    // }
    //
    // public static String getCurrentServerUrl(Context context, String appKey)
    // {
    // final boolean skipCache = appKey.equals(API_HOST);
    // String serverUrl = mCachedServerUrl.get(appKey);
    // if (TextUtils.isEmpty(serverUrl) || skipCache) {
    // String urlHost = QiupuORM.queryHostConfiguration(context, appKey);
    //
    // // NullPointException urlHost = null;
    // if(TextUtils.isEmpty(urlHost)) {
    // urlHost = ConfigurationBase.DEFAULT_BORQS_URL;
    // }
    //
    // Log.d(TAG, "getCurrentServerUrl, return url name: " + urlHost);
    // serverUrl = urlHost.endsWith("/") ? urlHost : urlHost + "/";
    //
    // if (!skipCache) {
    // mCachedServerUrl.put(appKey, serverUrl);
    // }
    // }
    //
    // return serverUrl;
    // }
    //
    // public static void showPrivacyPolicyInfo(Context context) {
    // if (mIsPrivacyAlertDialog) {
    // Log.i(TAG, "showPrivacyPolicyInfo, ignore the alert dialog is shown.");
    // return;
    // }
    //
    // mIsPrivacyAlertDialog = true;
    //
    // AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
    // dlgBuilder
    // .setTitle(R.string.pref_string_privacy_info_title)
    // .setMessage(R.string.pref_string_privacy_info_description)
    // .setNegativeButton(R.string.label_ok,
    // new DialogInterface.OnClickListener() {
    // public void onClick(DialogInterface dialog,
    // int whichButton) {
    // mIsPrivacyAlertDialog = false;
    // }
    // });
    // dlgBuilder.create().show();
    // }
}
