package com.borqs.se;

import android.os.Build;

/**
 * Created by yangfeng on 15/10/25.
 */
public class OsUtils {
    public static boolean isObjectResizeEnabled() {
        return Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT;
    }
}
