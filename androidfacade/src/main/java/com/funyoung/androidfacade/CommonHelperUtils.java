package com.funyoung.androidfacade;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.List;

public class CommonHelperUtils {
    public static String formatDate(long timeStamp) {
//        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
//        return fmt.format(new Date(timeStamp));
        return SimpleDateFormat.getDateInstance().format(timeStamp);
    }

    public static String formatDateTime(long timeStamp) {
        return SimpleDateFormat.getDateTimeInstance().format(timeStamp);
    }

    public static void hideIme(Context context, IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static void showIme(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public static void putBoolean(SharedPreferences preferences, String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void putString(SharedPreferences preferences, String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean isTopActivity(Context context, ComponentName componentName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null != am) {
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                return topActivity.equals(componentName);
            }
        }

        return false;
    }
}
