package com.funyoung.androidfacade;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

public class AndroidServiceUtils {
    private static final String TAG = AndroidServiceUtils.class.getSimpleName();

    public static boolean isInChina(Context context) {
//        return 460 == getCountryCode(context);
        return "CN".equalsIgnoreCase(getCountryCode(context));
    }

    public static String getDeviceId(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } else {
            return null;
        }
    }

    public static String getSubscriberId(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        } else {
            return null;
        }
    }

    public static String getLine1Number(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getLine1Number();
        } else {
            return null;
        }
    }

    public static String getCountryCode(Context context) {
        String countryZipCode = "";
        Locale locale = context.getResources().getConfiguration().locale;
        countryZipCode = locale.getCountry();
        Log.d(TAG, " zip code " + countryZipCode);
        return countryZipCode;

//        int mcc = -1;
//        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                || context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            CellLocation cl = telManager.getCellLocation();
//            if (cl != null && telManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
//                String strOperator = telManager.getNetworkOperator();
//                if (!TextUtils.isEmpty(strOperator)) {
//                    try {
//                        mcc = Integer.valueOf(strOperator.substring(0, 3));
//                    } catch (Exception e) {
//                        Log.e("Test_Utils", "Get country code error. " + e.getMessage());
//                    }
//                }
//            }
//        } else {
//            // request permission
//        }
//
//        return mcc;
    }
}
