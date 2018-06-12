package com.borqs.se.download;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.borqs.market.utils.MarketUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.SettingsActivity;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;

public class DownloadChangeReceiver extends BroadcastReceiver {
    
    private DownloadManager mDownloadManager;

    @Override
    public void onReceive(final Context context, Intent intent) {

        //Get the plug-in package information by product ID
        final String productId = intent.getStringExtra(MarketUtils.EXTRA_PRODUCT_ID);
        if (mDownloadManager == null) {
            mDownloadManager = DownloadManager.getInstance(context);
        }

        if (intent.getAction().equals(MarketUtils.ACTION_MARKET_DOWNLOAD_COMPLETE) || intent.getAction().equals(MarketUtils.ACTION_MARKET_THEME_INSTALL)) {
            if ("com.borqs.se.object.fighter".equals(productId)) {
                SettingsActivity.setHasGivenMoneyToUs(context, true);
            }
            new Thread() {
                @Override
                public void run() {
                    mDownloadManager.installDownload(productId);
                }
            }.start();
        } else if (intent.getAction().equals(MarketUtils.ACTION_MARKET_THEME_APPLY)) {
            new Thread() {
                @Override
                public void run() {
                    mDownloadManager.applyDownload(productId);
                }
            }.start();
            
        } else if (intent.getAction().equals(MarketUtils.ACTION_MARKET_THEME_DELETE)) {
            new Thread() {
                @Override
                public void run() {
                    mDownloadManager.removeDownload(productId);
                }
            }.start();
        } else if (intent.getAction().equals(MarketUtils.ACTION_MARKET_PURCHASE_SUCCESS)) {
            if ("com.borqs.se.object.fighter".equals(productId)) {
                SettingsActivity.setHasGivenMoneyToUs(context, true);
            }
        } else if (intent.getAction().equals(MarketUtils.ACTION_MARKET_SCAN_COMPLETE)) {
            SettingsActivity.setUpdateLocalResFinished(context, true);
            String where = ThemeColumns.IS_APPLY + "=" + 1;
            Cursor cursor = context.getContentResolver().query(ThemeColumns.CONTENT_URI, new String[]{ThemeColumns.PRODUCT_ID}, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String themeID = cursor.getString(0);
                    if (themeID != null) {
                        MarketUtils.updatePlugIn(context, themeID, true, MarketUtils.CATEGORY_THEME);
                    }
                }
                cursor.close();
            }
        }else if (intent.getAction().equals("com.borqs.se.intent.action.ADD_MODEL_TO_HOUSE")) {
            boolean update = intent.getBooleanExtra("NEED_UPDATE", true);
            String resourcePath = intent.getStringExtra("RESOURCE_PATH");
            HomeManager.getInstance().addModelToScene(resourcePath, update);
            
            Intent insert = new Intent("com.borqs.market.intent.action.Insert");
            insert.putExtra("RESOURCE_PATH", resourcePath);
            context.sendBroadcast(insert);
        }
    }

}
