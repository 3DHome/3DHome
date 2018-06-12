package com.borqs.market.utils;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

public class DownloadUtils {
    public static final String TAG = DownloadUtils.class.getSimpleName();
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    
//    private Context mContext;
    private DownloadManager dm;
    
    public DownloadUtils(Context context) {
        super();
//        this.mContext = context;
        dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public long download(String url, String name, String id) {
        try {
            Uri uri = Uri.parse(url);
            Request request = new Request(uri);
            String suffix = url.substring(url.lastIndexOf("."), url.length());
            String filepath = MarketUtils.getDownloadPath() + File.separator + id + suffix;
            request.setTitle(name);
            request.setDestinationUri(Uri.fromFile(new File(filepath)));
            long downloadID = dm.enqueue(request);
            QiupuHelper.addDownloading(id, downloadID);
            return downloadID;
        } catch (Exception e) {
            if (!TextUtils.isEmpty(e.getMessage())) {
                Log.e(TAG, e.getMessage());
            }
        }
        return -1;
    }

    /**
     * get downloaded byte, total byte and download status
     * 
     * @param downloadId
     * @return a int array with three elements
     * <ul>
     * <li>result[0] represents downloaded bytes, This will initially be -1.</li>
     * <li>result[1] represents total bytes, This will initially be -1.</li>
     * <li>result[2] represents download status, This will initially be 0.</li>
     * </ul>
     */
    public static int[] getBytesAndStatus(Context context,long downloadId) {
        DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        int[] bytesAndStatus = new int[] { -1, -1, 0 };
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = dm.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            dm = null;
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }
    
    public static void cancleDownload(Context context,long downloadId,String key) {
        DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        dm.remove(downloadId);
        QiupuHelper.removeDownloading(key);
    }
    
    public int getStatusById(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_STATUS);
    }
    
    /**
     * get int column
     * 
     * @param downloadId
     * @param columnName
     * @return
     */
    private int getInt(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        int result = -1;
        Cursor c = null;
        try {
            c = dm.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(columnName));
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }
    
    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int    MB_2_BYTE             = 1024 * 1024;
    public static final int    KB_2_BYTE             = 1024;

    /**
     * @param size
     * @return
     */
    public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size
                                                                             / MB_2_BYTE))
                                        .append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size
                                                                             / KB_2_BYTE))
                                        .append("K");
        } else {
            return size + "B";
        }
    }
    
    public static String getNotiPercent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int)((double)progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }
    
    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
               || downloadManagerStatus == DownloadManager.STATUS_PAUSED
               || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }
}
