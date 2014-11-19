package com.borqs.market.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.borqs.market.CommentListActivity;
import com.borqs.market.CreateCommentActivity;
import com.borqs.market.ProductDetailActivity;
import com.borqs.market.ProductLocalDetailActivity;
import com.borqs.market.WallpaperProductDetailActivity;
import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;

/**
 * Created by IntelliJ IDEA. User: b608 Date: 11-8-29 Time: 下午4:47 To change
 * this template use File | Settings | File Templates.
 */
public class IntentUtil {
    private final static String TAG = "IntentUtil";

    public static final String EXTRA_KEY_ID = "EXTRA_KEY_ID";
    public static final String EXTRA_KEY_VERSION = "EXTRA_KEY_VERSION";
    public static final String EXTRA_KEY_NAME = "EXTRA_KEY_NAME";
    public static final String EXTRA_MOD = "EXTRA_MOD";
    public static final String EXTRA_KEY_RATING = "EXTRA_KEY_RATING";

    public static void startProductDetailActivity(Activity context,
            String product_id, int version_code,String name, boolean isPortMode) {
        startProductDetailActivity(context, product_id, version_code, name,
                isPortMode ? Product.SupportedMod.PORTRAIT : Product.SupportedMod.LANDSCAPE);
    }

    public static void startProductDetailActivity(Activity context,
                String product_id, int version_code,String name, String supportMode) {
        Intent intent = new Intent();
        intent.setClass(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_KEY_ID, product_id);
        intent.putExtra(EXTRA_KEY_VERSION, version_code);
        intent.putExtra(EXTRA_KEY_NAME, name);
        intent.putExtra(EXTRA_MOD, supportMode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static void startProductLocalDetailActivity(Activity context,
            String product_id, int version_code,String name, String supportedMod) {
        Intent intent = new Intent();
        intent.setClass(context, ProductLocalDetailActivity.class);
        intent.putExtra(EXTRA_KEY_ID, product_id);
        intent.putExtra(EXTRA_KEY_VERSION, version_code);
        intent.putExtra(EXTRA_KEY_NAME, name);
        intent.putExtra(EXTRA_MOD, supportedMod);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public static void startCommentListIntent(Context context, String product_id, float rating, int version_code) {
        try {
            Intent intent = new Intent(context, CommentListActivity.class);
            intent.putExtra(EXTRA_KEY_ID, product_id);
            intent.putExtra(EXTRA_KEY_RATING, rating);
            intent.putExtra(EXTRA_KEY_VERSION, version_code);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void startCreateCommentActivity(Context context,
            String product_id, int version_code) {
        Intent intent = new Intent();
        intent.setClass(context, CreateCommentActivity.class);
        intent.putExtra(EXTRA_KEY_ID, product_id);
        intent.putExtra(EXTRA_KEY_VERSION, version_code);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void sendBroadCastforInstall(Context context, String fileUri,
            String productId) {
        Intent downIntent = new Intent(MarketUtils.ACTION_MARKET_THEME_INSTALL);
        downIntent.putExtra(MarketUtils.EXTRA_FILE_URI, fileUri);
        downIntent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        context.sendBroadcast(downIntent, null);
    }
    
    public static void sendBroadCastforApplyed(Context context, String fileUri,
            String productId) {
        Intent downIntent = new Intent(MarketUtils.ACTION_MARKET_THEME_APPLY);
        downIntent.putExtra(MarketUtils.EXTRA_FILE_URI, fileUri);
        downIntent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        context.sendBroadcast(downIntent, null);
    }
    
    public static void sendBroadCastforScanComplete(Context context) {
        Intent intent = new Intent(MarketUtils.ACTION_MARKET_SCAN_COMPLETE);
        context.sendBroadcast(intent, null);
    }

    public static void sendBroadBackToScene(Context context) {
        Intent downIntent = new Intent(MarketUtils.ACTION_BACK_TO_SCENE);
        context.sendBroadcast(downIntent, null);
    }
    
    public static void sendBroadCastforPurchaseSuccess(Context context, String productId) {
        Intent intent = new Intent(MarketUtils.ACTION_MARKET_PURCHASE_SUCCESS);
        intent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        context.sendBroadcast(intent, null);
    }
    
    public static void sendBroadCastforLongClick(Context context, String productId) {
        Intent intent = new Intent(MarketUtils.ACTION_MARKET_LONG_CLICK);
        intent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        context.sendBroadcast(intent, null);
    }

    public static void sendBroadCastDownloaded(Context context, String fileUri,
            String productId, int version_code, String version_name) {
        Intent downIntent = new Intent(
                MarketUtils.ACTION_MARKET_DOWNLOAD_COMPLETE);
        downIntent.putExtra(MarketUtils.EXTRA_FILE_URI, fileUri);
        downIntent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        downIntent.putExtra(MarketUtils.EXTRA_VERSION_CODE, version_code);
        downIntent.putExtra(MarketUtils.EXTRA_VERSION_NAME, version_name);
        context.sendBroadcast(downIntent, null);
    }
    
    public static void sendBroadCastProductHasUpdate(Context context, boolean objectHasUpdate,
            boolean themeHasUpdate, boolean wallpaperHasUpdate) {
        Intent intent = new Intent(
                MarketUtils.ACTION_MARKET_PRODUCT_HAS_UPDATE);
        intent.putExtra(ProductType.OBJECT, objectHasUpdate);
        intent.putExtra(ProductType.THEME, themeHasUpdate);
        intent.putExtra(ProductType.WALL_PAPER, wallpaperHasUpdate);
        context.sendBroadcast(intent, null);
    }

    public static void sendBroadCastDelete(Context context, String productId) {
        Intent downIntent = new Intent(MarketUtils.ACTION_MARKET_THEME_DELETE);
        downIntent.putExtra(MarketUtils.EXTRA_PRODUCT_ID, productId);
        context.sendBroadcast(downIntent, null);
    }
    
    public static void startWireLessSetting(Context context) {
        Intent intent=null;
        if(android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.HONEYCOMB){
            intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        }else{
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings","com.android.settings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        context.startActivity(intent);
    }
    
    public static void startAddAccountSetting(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
        context.startActivity(intent);
    }

}
