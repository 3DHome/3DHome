package com.funyoung.androidfacade;

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
}
