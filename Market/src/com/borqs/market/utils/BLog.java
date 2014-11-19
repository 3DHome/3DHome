package com.borqs.market.utils;

import android.util.Log;

public class BLog {
    private static boolean SHOW_LOG = false;
    private static boolean SHOW_PERFORMANCE_LOG = false;
    public static String TAG = "BorqsMarket>>>";
    public static boolean isSHOW_LOG() {
        return SHOW_LOG;
    }

    public static void setSHOW_LOG(boolean showLog) {
        SHOW_LOG = showLog;
    }

    public static void v(String tag, String msg) {
        if (SHOW_LOG)
            Log.v(TAG+tag, msg);
    }

    public static void d(String tag, String msg) {
        if (SHOW_LOG)
            Log.d(TAG+tag, msg);
    }

    public static void e(String tag, String msg) {
        if (SHOW_LOG)
            Log.e(TAG+tag, msg);
    }

    public static void w(String tag, String msg) {
        if (SHOW_LOG)
            Log.w(TAG+tag, msg);
    }

    public static void d(String msg) {
        if (SHOW_LOG)
            Log.d(TAG, msg);
    }

    public static void setShowPerformanceLog(boolean showLog) {
        SHOW_PERFORMANCE_LOG = showLog;
    }

    public static void v(String msg) {
        if (SHOW_LOG)
            Log.v(TAG, msg);
    }

    public static void e(String msg) {
        if (SHOW_LOG)
            Log.e(TAG, msg);
    }

    public static void w(String msg) {
        if (SHOW_LOG)
            Log.w(TAG, msg);
    }

    public static void d_performance(String msg) {
        if (SHOW_PERFORMANCE_LOG)
            Log.d(TAG, msg);
    }
}
