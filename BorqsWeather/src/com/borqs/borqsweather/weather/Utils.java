package com.borqs.borqsweather.weather;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.borqsweather.weather.yahoo.PinYinUtil;

public class Utils {

    public static boolean hasSDcard() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File("sdcard-ext");
            if (file.exists() && file.canWrite()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static String getSdcardPath() {
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File("sdcard-ext");
            if (file.exists() && file.canWrite()) {
                sdcardPath = file.getPath();
            }
        }
        return sdcardPath;
    }

    public static String get3DHomePath() {
        String str =  getSdcardPath() + File.separator + "3DHome";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo networkinfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkinfo != null && networkinfo.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkNetworkIsAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo networkinfo = connManager.getActiveNetworkInfo();
            if (networkinfo == null || !networkinfo.isAvailable()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            return true;
        }
        return false;
    }

    public static String changeHanZiToPinYin(String hanZi) {
        List<String> strList = PinYinUtil.getPinYin(hanZi);
        StringBuilder strB = new StringBuilder();
        for (String token : strList) {
            strB.append(token);
        }
        return strB.toString();
    }

    public static int getCountryCode(Context context) {
        int mcc = -1;
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation cl = telManager.getCellLocation();
        if (cl != null && telManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            String strOperator = telManager.getNetworkOperator();
            if (!TextUtils.isEmpty(strOperator)) {
                try {
                    mcc = Integer.valueOf(strOperator.substring(0, 3));
                } catch (Exception e) {
                    Log.e("Test_Utils", "Get country code error. " + e.getMessage());
                }
            }
        }
        return mcc;
    }

    public static boolean isSameDate(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);

        if ((cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
                && (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInvalidWeather(long CurrentTime, long SpecifiedTime) {
        //Set 48 hours to adapt with China Weather Network.  
        //If weather you get is not elder than 48 hours, we think it is invalid.

        if ((CurrentTime - SpecifiedTime) > 48 * 1000 * 60 * 60) {
            return false;
        } else {
            return true;
        }
    }
    
    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager lm= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        return false;
    }

    public static String get24Time(String time){
        String result = null;
        if (!TextUtils.isEmpty(time)) {
            result = time.toLowerCase();
            if (time.contains("am")) {
                result = time.replace("am", "").trim();
            } else if (time.contains("pm")) {
                result = time.replace("pm", "").trim();
                String[] hm = result.split(":");
                int h = 0;
                try {
                    h = Integer.parseInt(hm[0]);
                    if (h < 12)
                        h = h + 12;
                } catch (Exception e) {
                    return null;
                }
                result = "" + h + ":" + hm[1];
            } else {
                result = time.trim();
            }
        }
        return result;
    }

    private static LogWriter mLogWriter;

    public static void printToLogFile(String str) {
        if (mLogWriter == null) {
            mLogWriter = new LogWriter(".weather.log");
        }
        try {
            mLogWriter.print(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static class LogWriter {


        private FileOutputStream mWriter;
        private SimpleDateFormat mFormatter;
        private String mLogBasePath;
        private File mFile;
        private LogWriter(String file_path) {
            mWriter = null;
            mLogBasePath = file_path;
            mFormatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
            try {
                open(mLogBasePath + ".0");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void open(String file_path) throws IOException {
            File file = new File(get3DHomePath());
            if (file.exists()) {
                if (!file.isDirectory()) {
                    file.delete();
                    file.mkdir();
                } else {
                   
                }
            } else {
                file.mkdir();
            }
            if (file.exists()) {
                String fileName = get3DHomePath() + File.separator + file_path;
                mFile = new File(fileName);

                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
                mWriter = new FileOutputStream (mFile, true);
            }

        }

        private void close() throws IOException {
            if (mWriter != null) {
                mWriter.close();
                mWriter = null;
            }
        }

        public void print(String log) throws IOException {
            if (mWriter != null) {
                Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                String time = mFormatter.format(curDate) + ":";
                mWriter.write(time.getBytes());
                mWriter.write(log.getBytes());
                String s = "\n";
                mWriter.write(s.getBytes());
                mWriter.flush();
                if (mFile.length() > 100000) {
                    switchWriter();
                }
            }
        }
        
        private void switchWriter() {
            try {
                close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            File from =new File(get3DHomePath() + File.separator + mLogBasePath + ".4") ;
            if (from.exists()) {
                from.delete();
            }
            from =new File(get3DHomePath() + File.separator + mLogBasePath + ".3") ;
            if (from.exists()) {
                File to=new File(get3DHomePath() + File.separator + mLogBasePath + ".4") ;
                from.renameTo(to) ;
            }
            from =new File(get3DHomePath() + File.separator + mLogBasePath + ".2") ;
            if (from.exists()) {
                File to=new File(get3DHomePath() + File.separator + mLogBasePath + ".3") ;
                from.renameTo(to) ;
            }
            from =new File(get3DHomePath() + File.separator + mLogBasePath + ".1") ;
            if (from.exists()) {
                File to=new File(get3DHomePath() + File.separator + mLogBasePath + ".2") ;
                from.renameTo(to) ;
            }
            from =new File(get3DHomePath() + File.separator + mLogBasePath + ".0") ;
            if (from.exists()) {
                File to=new File(get3DHomePath() + File.separator + mLogBasePath + ".1") ;
                from.renameTo(to) ;
            }
            try {
                open(mLogBasePath + ".0");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void zipFolder(String[] srcFilePaths, String zipFilePath) throws Exception {
        // 创建Zip包
        File zipfile = new File(zipFilePath);
        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(

        zipfile));

        // 打开要输出的文件
        for (String srcFilePath : srcFilePaths) {
            if (!TextUtils.isEmpty(srcFilePath)) {
                java.io.File file = new java.io.File(srcFilePath);

                // 压缩

                zipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);

                // 完成,关闭
            }
        }
        outZip.finish();

        outZip.close();

    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)

    throws Exception {

        if (zipOut == null) {

            return;

        }

        java.io.File file = new java.io.File(folderPath + filePath);

        // 判断是不是文件

        if (file.isFile()) {

            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);

            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);

            zipOut.putNextEntry(zipEntry);

            int len;

            byte[] buffer = new byte[100000];

            while ((len = inputStream.read(buffer)) != -1) {

                zipOut.write(buffer, 0, len);

            }

            inputStream.close();

            zipOut.closeEntry();

        } else {

            // 文件夹的方式,获取文件夹下的子文件

            String fileList[] = file.list();

            // 如果没有子文件, 则添加进去即可

            if (fileList.length <= 0) {

                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + java.io.File.separator);

                zipOut.putNextEntry(zipEntry);

                zipOut.closeEntry();

            }

            // 如果有子文件, 遍历子文件

            for (int i = 0; i < fileList.length; i++) {

                zipFiles(folderPath, filePath + java.io.File.separator + fileList[i], zipOut);

            }

        }

    }
            

}
