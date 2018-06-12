package com.borqs.market.service;

import java.io.File;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.json.Product;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.ThemeXmlParser;

public class LocalDataInitReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.borqs.market.intent.action.Init".equals(intent.getAction())) {
            File dirctory = MarketConfiguration.getSystemThemeDir();
            insertProductFromFolder(context, dirctory);
        } else if ("com.borqs.market.intent.action.Insert".equals(intent.getAction())) {
            String resourcePath = intent.getStringExtra("RESOURCE_PATH");
            insertSingleProduct(context, new File(resourcePath));
        }
    }

    private void insertSingleProduct(final Context ctx, final File folder) {
        new Thread(new Runnable() {
            private void parseProductConf(ArrayList<Product> productList, File conf) {
                Product p = ThemeXmlParser.parser(conf);
                if (p != null) {
                    productList.add(p);
                }
            }

            private boolean parseProductFolder(ArrayList<Product> productList, File dirctory) {
                parseProductConf(productList, dirctory);
                return true;

            }

            @Override
            public void run() {
                ArrayList<Product> productList = new ArrayList<Product>();
                if (parseProductFolder(productList, folder)) {
                    for (Product product : productList) {
                        DownLoadHelper.deletePlugInById(ctx, product.product_id);
                    }
                }

                if (!productList.isEmpty()) {
                    MarketUtils.bulkInsertPlugInFromProduct(ctx, productList);
                }
                IntentUtil.sendBroadCastforScanComplete(ctx);
            }
        }, "LocalDataInitReciver").start();
    }

    private void insertProductFromFolder(final Context ctx, final File folder) {
        new Thread(new Runnable() {
            private void parseProductConf(ArrayList<Product> productList, File conf) {
                Product p = ThemeXmlParser.parser(conf);
                if (p != null) {
                    productList.add(p);
                }
            }

            private boolean parseProductFolder(ArrayList<Product> productList, File dirctory) {
                if (null != dirctory && dirctory.exists() && dirctory.isDirectory()) {
                    File[] files = dirctory.listFiles();
                    if (files != null) {
                        for (File sub : files) {
                            parseProductConf(productList, sub);
                        }
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    BLog.v("LocalDataInitReciver", "dirctory is exists");
                    return false;
                }
            }

            @Override
            public void run() {
                ArrayList<Product> productList = new ArrayList<Product>();

                if (parseProductFolder(productList, folder)) {
                    DownLoadHelper.deleteSystemPlugIn(ctx, true);
                }

                if (!productList.isEmpty()) {
                    MarketUtils.bulkInsertPlugInFromProduct(ctx, productList);
                }
                IntentUtil.sendBroadCastforScanComplete(ctx);
            }
        }, "LocalDataInitReciver").start();
    }

}
