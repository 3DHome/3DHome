package com.borqs.market.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Set;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownloadInfoColumns;
import com.borqs.market.listener.DownloadListener;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.QiupuHelper;

public class DownloadReciver extends BroadcastReceiver {
    private static final String TAG = "DownloadReciver";
    DownloadManager dm = null;

    private DownloadManager getDM(Context context) {
        if (dm == null)
            dm = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
        return dm;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BLog.d(TAG, "downloadComplete: onReceive CALLED");
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Cursor downCusor = DownLoadHelper.getInstance(context)
                .queryDownloadingByDownloadId(id);
        String product_id = null;
        String version_name = null;
        int version_code = 0;
        if (downCusor.getCount() > 0 && downCusor.moveToFirst()) {
            product_id = downCusor.getString(downCusor
                    .getColumnIndexOrThrow(DownloadInfoColumns.PRODUCT_ID));
            version_name = downCusor.getString(downCusor
                    .getColumnIndexOrThrow(DownloadInfoColumns.VERSION_NAME));
            version_code = downCusor.getInt(downCusor
                    .getColumnIndexOrThrow(DownloadInfoColumns.VERSION_CODE));
            
            QiupuHelper.removeDownloading(product_id);
        } else {
            downCusor.close();
            return;
        }
        if (downCusor != null) {
            downCusor.close();
        }
        Query query = new Query();
        query.setFilterById(id);

        Cursor c = getDM(context).query(query);
        if (c.moveToFirst()) {

            int statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = c.getInt(statusIndex);
            Uri uri = getDM(context).getUriForDownloadedFile(id);
            String local_uri =  null;
            if(uri != null) {
                local_uri =  uri.toString();
            }
            c.close();
            switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                File file = new File(URI.create(local_uri));

                BLog.d(TAG,
                        "updateDownloadStatus: beginTime="
                                + System.currentTimeMillis());
                DownLoadHelper.getInstance(context).updateDownloadStatus(id,
                        DownloadInfoColumns.DOWNLOAD_STATUS_COMPLETED,
                        local_uri, file.length());
                BLog.d(TAG,
                        "updateDownloadStatus: endTime="
                                + System.currentTimeMillis());

                updateActivityUI(true, product_id, local_uri);

                IntentUtil.sendBroadCastDownloaded(context, local_uri,
                        product_id, version_code, version_name);
                break;
            case DownloadManager.STATUS_FAILED:
                updateActivityUI(false, product_id, null);
                break;
            default:
                BLog.d(TAG, "_ID " + id + " completed with status " + status);
            }
        }
        if(c != null) {
            c.close();
        }
    }

    private void updateActivityUI(boolean success, String productId, String fileUri) {
        synchronized (QiupuHelper.downloadlisteners) {
            Set<String> set = QiupuHelper.downloadlisteners.keySet();
            for (String key : set) {
                WeakReference<DownloadListener> ref = QiupuHelper.downloadlisteners.get(key);
                if(ref != null) {
                   DownloadListener  listener = ref.get();
                    if (listener != null) {
                        if (success) {
                            listener.downloadSuccess(productId,fileUri);
                        } else {
                            listener.downloadFailed(productId);
                        }
                    }
                }
            }
        }
    }
}
