package com.funyoung.androidfacade;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;

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
}
