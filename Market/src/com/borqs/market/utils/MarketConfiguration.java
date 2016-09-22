package com.borqs.market.utils;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;

import com.borqs.market.account.AccountSession;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownloadInfoColumns;
import com.iab.engine.MarketBillingResult;

public final class MarketConfiguration {

    public static final String SHARE_PREFERENCES_MARKET_CONFIG = "market_config";
    public static final String SP_EXTRAS_INIT_LOCAL_DATA = "SP_EXTRAS_INIT_LOCAL_DATA";

    public static final String SP_EXTRAS_INIT_ASSERT_OBJECT = "SP_EXTRAS_INIT_ASSERT_OBJECT";
    public static final String SP_EXTRAS_SHOW_OBJECT_USER_GUIDE = "SP_EXTRAS_SHOW_OBJECT_USER_GUIDE";
    public static final String SP_EXTRAS_SHOW_OBJECT_USER_TIPS = "SP_EXTRAS_SHOW_OBJECT_USER_TIPS";

    private static Context context;
    private static File systemThemeDir;
    public static String PACKAGE_NAME = null;
    public static int VERSION_CODE = 0;

    private static boolean IS_DEBUG_TESTSERVER = false;
    private static boolean IS_DEBUG_SUGGESTION = false;
    private static boolean IS_DEBUG_BETA_REQUEST = false;

    public static boolean isIS_DEBUG_TESTSERVER() {
        return IS_DEBUG_TESTSERVER;
    }

    public static void setIS_DEBUG_TESTSERVER(boolean iS_DEBUG_TESTSERVER) {
        IS_DEBUG_TESTSERVER = iS_DEBUG_TESTSERVER;
    }

    public static boolean isIS_DEBUG_SUGGESTION() {
        return IS_DEBUG_SUGGESTION;
    }

    public static void setIS_DEBUG_SUGGESTION(boolean iS_DEBUG_SUGGESTION) {
        IS_DEBUG_SUGGESTION = iS_DEBUG_SUGGESTION;
    }

    public static boolean isIS_DEBUG_BETA_REQUEST() {
        return IS_DEBUG_BETA_REQUEST;
    }

    public static void setIS_DEBUG_BETA_REQUEST(boolean iS_DEBUG_BETA_REQUEST) {
        IS_DEBUG_BETA_REQUEST = iS_DEBUG_BETA_REQUEST;
    }

    public static void init(Context mContext) {
        context = mContext.getApplicationContext();
        PACKAGE_NAME = context.getApplicationInfo().packageName;

        try {
            VERSION_CODE = context.getPackageManager().getPackageInfo(PACKAGE_NAME,
                    PackageManager.GET_UNINSTALLED_PACKAGES).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        QiupuHelper.formatUserAgent(context);
        if (!AccountSession.isLogin) {
            AccountSession.loadAccount(context);
        }
        initDownloadingMap();
    }

    public static int billingType = MarketBillingResult.TYPE_IAB;

    public static void setPayType(int billType) {
        billingType = billType;
    }

    public static String IABKEY = null;

    public static void setIabKey(String iabKey) {
        IABKEY = iabKey;
    }

    public static boolean hasShowedObjectUserGuide(final Context context) {
        SharedPreferences sp = getMarketConfigPreferences(context);
        return sp.getBoolean(SP_EXTRAS_SHOW_OBJECT_USER_GUIDE, false);
    }

    public static void setShowObjectUserGuide(Context context, boolean value) {
        SharedPreferences settings = getMarketConfigPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SP_EXTRAS_SHOW_OBJECT_USER_GUIDE, value);
        editor.commit();
    }

    public static boolean hasShowedObjectUserTips(final Context context) {
        SharedPreferences sp = getMarketConfigPreferences(context);
        return sp.getBoolean(SP_EXTRAS_SHOW_OBJECT_USER_TIPS, false);
    }

    public static void setShowObjectUserTips(Context context, boolean value) {
        SharedPreferences settings = getMarketConfigPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SP_EXTRAS_SHOW_OBJECT_USER_TIPS, value);
        editor.commit();
    }

    public static void setSystemThemeDir(File dir) {
        if (dir != null) {
            if (!dir.exists())
                dir.mkdirs();
            if (!dir.isDirectory()) {

            }
            systemThemeDir = dir;
        } else {
            throw new IllegalArgumentException("File can not null");
        }
    }

    public static File getSystemThemeDir() {
        return systemThemeDir;
    }

    private static void saveBooleanMarketConfigPreferences(Context context, String key, boolean value) {
        SharedPreferences settings = getMarketConfigPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static SharedPreferences getMarketConfigPreferences(Context context) {
        return context.getSharedPreferences(SHARE_PREFERENCES_MARKET_CONFIG, Context.MODE_PRIVATE);
    }

    public static String getPackageName() {
        return PACKAGE_NAME;
    }

    private static void initDownloadingMap() {
        Cursor cursor = DownLoadHelper.queryAllDownloading(context);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadInfoColumns.DOWNLOAD_ID));
                String productId = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.PRODUCT_ID));
                QiupuHelper.addDownloading(productId, downloadId);
            }
        }
    }

}
