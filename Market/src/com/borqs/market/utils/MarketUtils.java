package com.borqs.market.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.borqs.market.MarketHomeActivity;
import com.borqs.market.PartitionsListActivity;
import com.borqs.market.ProductListActivity;
import com.borqs.market.UserShareListActivity;
import com.borqs.market.ProductTabActivity;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.api.PurchaseListener;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownLoadProvider;
import com.borqs.market.db.DownloadInfo;
import com.borqs.market.db.DownloadInfoColumns;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.fragment.ProductsDialog;
import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.market.wallpaper.WallpaperUtils;

public class MarketUtils {
    @SuppressWarnings("unused")
    private final static String TAG = "MarketUtils";

    public static void setLogVisibility(boolean show) {
        BLog.setSHOW_LOG(show);
    }

    public static void showPerformanceLog(boolean show) {
        BLog.setShowPerformanceLog(show);
    }

    public static final String EXTRA_APP_VERSION = "EXTRA_APP_VERSION";
    public static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    public static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    public static final String EXTRA_MOD = "EXTRA_MOD";
    public static final String EXTRA_TAB = "EXTRA_TAB";
    public static final String EXTRA_SHOW_HEADVIEW = "EXTRA_SHOW_HEADVIEW";
    public static final String EXTRA_ORDER_BY = "EXTRA_ORDER_BY";
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_ID = "EXTRA_ID";

    public static final String CATEGORY_THEME = ProductType.THEME;
    public static final String CATEGORY_OBJECT = ProductType.OBJECT;
    public static final String CATEGORY_SCENE = ProductType.SCENE;
    public static final String CATEGORY_WALLPAPER = ProductType.WALL_PAPER;
    public static final String TAB_LOCAL = "TAB_LOCAL";
    public static final String TAB_REMOTE = "TAB_REMOTE";
    // public static final String DOWNLOAD_AUTHORITY =
    // "com.borqs.freehdhome.download";//请更改为androidManifest中定义的DownloadProvider的authority

    public static boolean IS_ONLY_FREE = false; // 是否只取免费的
    public static final String MARKET_CONFIG_FILE_NAME = "ResourceManifest.xml";

    private static final String PREFS_SETTING_NAME = "com.borqs.se_preferences";
    private static final String KEY_ORIENTATION_PREFERRED = "orientation_preferred_key";
    private final static String ORIENTATION_ROTATION = "rotation";
    private final static String ORIENTATION_LANDSCAPE = "landscape";
    private final static String ORIENTATION_PORTRAIT = "portrait";

    /**
     * 
     * @param context
     * @param isOnlyFree
     *            application version code
     * @param category
     *            MarketUtils.CATEGORY_THEME
     *            MarketUtils.CATEGORY_OBJECT,MarketUtils.CATEGORY_WALLPAPER
     *            MarketUtils.CATEGORY_SCENE
     */
    public static void startMarketIntent(Context context, String category, boolean isOnlyFree) {
        IS_ONLY_FREE = isOnlyFree;
        Intent intent = new Intent(context, MarketHomeActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

    /**
     * 
     * @param context
     * @param isPortMode
     *            preferred portrait mode or not.
     * @param category
     *            MarketUtils.CATEGORY_THEME MarketUtils.CATEGORY_OBJECT
     *            MarketUtils.CATEGORY_SCENE
     */
    public static void startProductListIntent(Context context, String category, boolean isOnlyFree, boolean isPortMode) {
        startProductListIntent(context, category, isOnlyFree, isPortMode ? Product.SupportedMod.PORTRAIT
                : Product.SupportedMod.LANDSCAPE);

    }

    public static void startProductListIntent(Context context, String category, boolean isOnlyFree, String supportedMode) {
        IS_ONLY_FREE = isOnlyFree;
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_MOD, supportedMode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

    public static void startProductListIntent(Context context, String category, boolean isOnlyFree,
            String supportedMod, boolean showHeaderView, String orderBy) {
        IS_ONLY_FREE = isOnlyFree;
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_MOD, supportedMod);
        intent.putExtra(EXTRA_SHOW_HEADVIEW, showHeaderView);
        intent.putExtra(EXTRA_ORDER_BY, orderBy);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

    /**
     * 
     * @param context
     * @param category
     *            MarketUtils.CATEGORY_THEME MarketUtils.CATEGORY_OBJECT
     *            MarketUtils.CATEGORY_SCENE
     */
    public static void startUserShareListIntent(Context context, String category, String supportedMod) {
        Intent intent = new Intent(context, UserShareListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_MOD, supportedMod);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

    public static void startPartitionseListIntent(Context context, String category, String supportedMod, String name,
            String partitions_id) {
        Intent intent = new Intent(context, PartitionsListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_MOD, supportedMod);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_ID, partitions_id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

    public static void startLocalProductListIntent(Context context, String package_name, String category,
            boolean isOnlyFree, String supported_mod) {
        MarketUtils.IS_ONLY_FREE = isOnlyFree;
        if (TextUtils.isEmpty(package_name)) {
            throw new IllegalArgumentException("package name is null");
        }
        try {
            int version_code = context.getPackageManager().getPackageInfo(package_name,
                    PackageManager.GET_UNINSTALLED_PACKAGES).versionCode;

            Intent intent = new Intent(context, ProductTabActivity.class);
            intent.putExtra(MarketUtils.EXTRA_APP_VERSION, version_code);
            intent.putExtra(MarketUtils.EXTRA_PACKAGE_NAME, package_name);
            intent.putExtra(MarketUtils.EXTRA_CATEGORY, category);
            intent.putExtra(MarketUtils.EXTRA_MOD, supported_mod);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String EXTRA_FILE_URI = "EXTRA_FILE_URI";
    public static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    public static final String EXTRA_PRODUCT_ID = "EXTRA_PRODUCT_ID";
    public static final String EXTRA_VERSION_CODE = "EXTRA_VERSION_CODE";
    public static final String EXTRA_VERSION_NAME = "EXTRA_VERSION_NAME";
    public static final String ACTION_MARKET_DOWNLOAD_COMPLETE = MarketConfiguration.getPackageName()
            + ".market.intent.action.DOWNLOAD_COMPLETE";
    public static final String ACTION_MARKET_PURCHASE_SUCCESS = MarketConfiguration.getPackageName()
            + ".market.intent.action.PURCHASE_SUCCESS";
    public static final String ACTION_MARKET_THEME_INSTALL = MarketConfiguration.getPackageName()
            + ".market.intent.action.INSTALL";
    public static final String ACTION_MARKET_THEME_APPLY = MarketConfiguration.getPackageName()
            + ".market.intent.action.APPLY";
    public static final String ACTION_MARKET_THEME_DELETE = MarketConfiguration.getPackageName()
            + ".market.intent.action.DELETE";
    public static final String ACTION_BACK_TO_SCENE = MarketConfiguration.getPackageName()
            + ".market.intent..action.ACTION_BACK_TO_SCENE";
    public static final String ACTION_MARKET_SCAN_COMPLETE = MarketConfiguration.getPackageName()
            + ".market.intent.action.SCAN_COMPLETE";
    public static final String ACTION_MARKET_LONG_CLICK = MarketConfiguration.getPackageName()
            + ".market.intent.action.LONG_CLICK";
    public static final String ACTION_MARKET_PRODUCT_HAS_UPDATE = MarketConfiguration.getPackageName()
            + ".market.intent.action.PRODUCT_HAS_UPDATE";

    public static void insertPlugIn(Context context, DownloadInfo info, boolean isApply, File file) {
        if (file == null) {
            throw new IllegalAccessError("file can't null");
        }
        ContentValues pluginValues = new ContentValues();
        pluginValues.put(PlugInColumns.NAME, info.name);
        pluginValues.put(PlugInColumns.PRODUCT_ID, info.product_id);
        pluginValues.put(PlugInColumns.VERSION_NAME, info.version_name);
        pluginValues.put(PlugInColumns.VERSION_CODE, info.version_code);
        pluginValues.put(PlugInColumns.TYPE, info.type);
        pluginValues.put(PlugInColumns.IS_APPLY, isApply ? 1 : 0);
        pluginValues.put(PlugInColumns.FILE_PATH, file.getPath());
        pluginValues.put(PlugInColumns.BELONG_SYSTEM, Product.isBelongSystem(file.getPath()));
        pluginValues.put(PlugInColumns.UPDATE_TIME, System.currentTimeMillis());
        Product product = ThemeXmlParser.parser(file);
        if (product != null) {
            pluginValues.put(PlugInColumns.LOCAL_JSON_STR, ProductJSONImpl.createJsonObjectString(product));
        }
        pluginValues.put(PlugInColumns.JSON_STR, info.json_str);
        pluginValues.put(PlugInColumns.SUPPORTED_MOD, info.supported_mod);
        context.getContentResolver().insert(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                pluginValues);
    }

    public static void updatePlugIn(Context context, String id, boolean isApply, String exclusionType) {
        ContentValues values = new ContentValues();
        String where = PlugInColumns.TYPE + "=?";
        if (!TextUtils.isEmpty(exclusionType)) {
            values.put(PlugInColumns.IS_APPLY, 0);
            String[] whereArgs = { exclusionType };
            context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                    values, where, whereArgs);
        }

        where = PlugInColumns.PRODUCT_ID + " = '" + id + "'";
        values.put(PlugInColumns.IS_APPLY, isApply ? 1 : 0);
        context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                values, where, null);
    }

    // public static void updatePlugIn(Context context, String id, boolean
    // isPort, boolean isApply) {
    // ContentValues values = new ContentValues();
    // String where = PlugInColumns.PRODUCT_ID + "=?" + " AND " +
    // PlugInColumns.SUPPORTED_MOD + " LIKE '%?'%";
    // String[] whereArgs = new String[] {id, (isPort ?
    // Product.SupportedMod.PORTRAIT : Product.SupportedMod.LANDSCAPE)};
    // values.put(PlugInColumns.IS_APPLY,isApply ? 1 : 0);
    // context.getContentResolver().update(DownLoadProvider.getContentURI(context,
    // DownLoadProvider.TABLE_PLUGIN), values, where, whereArgs);
    // }

    public static boolean isProductApplied(Context context, String productId, boolean isPort) {
        String key = isPort ? Product.SupportedMod.PORTRAIT : Product.SupportedMod.LANDSCAPE;
        String where = PlugInColumns.PRODUCT_ID + "=?" + " AND " + PlugInColumns.SUPPORTED_MOD + " LIKE '%" + key
                + "%'";
        String[] whereArgs = new String[] { productId };
        Cursor cursor = context.getContentResolver().query(
                DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                new String[] { PlugInColumns.IS_APPLY }, where, whereArgs, null);
        boolean result = false;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = 1 == cursor.getInt(cursor.getColumnIndex(PlugInColumns.IS_APPLY));
            }
            cursor.close();
        }
        return result;
    }

    public static void updatePluginWithDownloadInfo(Context context, DownloadInfo info, File file) {
        ContentValues pluginValues = new ContentValues();
        String where = PlugInColumns.PRODUCT_ID + " = '" + info.product_id + "'";
        pluginValues.put(PlugInColumns.NAME, info.name);
        pluginValues.put(PlugInColumns.VERSION_NAME, info.version_name);
        pluginValues.put(PlugInColumns.VERSION_CODE, info.version_code);
        pluginValues.put(PlugInColumns.FILE_PATH, file.getPath());
        pluginValues.put(PlugInColumns.BELONG_SYSTEM, Product.isBelongSystem(file.getPath()));
        pluginValues.put(PlugInColumns.UPDATE_TIME, System.currentTimeMillis());
        Product product = ThemeXmlParser.parser(file);
        if (product != null) {
            pluginValues.put(PlugInColumns.LOCAL_JSON_STR, ProductJSONImpl.createJsonObjectString(product));
        }
        pluginValues.put(PlugInColumns.JSON_STR, info.json_str);
        pluginValues.put(PlugInColumns.SUPPORTED_MOD, info.supported_mod);

        context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                pluginValues, where, null);
    }

    // public static void updatePlugInVersion(Context context, String id, int
    // versionCode, String versionName) {
    // ContentValues values = new ContentValues();
    // String where = PlugInColumns.PRODUCT_ID + " = '" + id + "'";
    // values.put(PlugInColumns.VERSION_CODE,versionCode);
    // values.put(PlugInColumns.VERSION_NAME,versionName);
    // context.getContentResolver().update(DownLoadProvider.getContentURI(context,
    // DownLoadProvider.TABLE_PLUGIN), values, where, null);
    // }

    public static void deletePlugIn(Context context, String id) {
        String where = PlugInColumns.PRODUCT_ID + " = '" + id + "'";
        context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                where, null);
    }

    public static void deleteDownload(Context context, String id) {
        String where = DownloadInfoColumns.PRODUCT_ID + " = '" + id + "'";
        context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_DOWNLOAD),
                where, null);
    }

    public static Cursor queryPlugIn(Context context, String id) {
        String where = PlugInColumns.PRODUCT_ID + " = '" + id + "'";
        return context.getContentResolver().query(
                DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), PlugInColumns.PROJECTION,
                where, null, null);
    }

    public static DownloadInfo getDownloadInfoByProductId(Context context, String productId) {
        DownloadInfo info = null;
        Cursor cursor = null;
        try {
            String where = DownloadInfoColumns.PRODUCT_ID + " = '" + productId + "'";
            cursor = context.getContentResolver().query(
                    DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_DOWNLOAD), null, where, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                info = new DownloadInfo();
                info.product_id = productId;
                info.download_status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadInfoColumns.DOWNLOAD_STATUS));
                info.name = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.NAME));
                info.version_name = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.VERSION_NAME));
                info.version_code = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadInfoColumns.VERSION_CODE));
                info.local_path = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.LOCAL_PATH));
                info.type = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.TYPE));
                info.json_str = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.JSON_STR));
                info.supported_mod = cursor.getString(cursor.getColumnIndexOrThrow(DownloadInfoColumns.SUPPORTED_MOD));
                if (!TextUtils.isEmpty(info.local_path) && info.local_path.contains("file://")) {
                    info.local_path = info.local_path.replace("file://", "");
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }

    public static DownloadInfo getDownloadInfoFromPlugIn(Context context, String productId) {
        DownloadInfo info = null;
        Product product = DownLoadHelper.queryLocalProductInfo(context, productId);
        if (product != null) {
            info = new DownloadInfo();
            info.product_id = productId;
            info.download_status = DownloadInfoColumns.DOWNLOAD_STATUS_COMPLETED;
            info.name = product.name;
            info.version_name = product.version_name;
            info.version_code = product.version_code;
            info.local_path = product.installed_file_path;
            info.type = product.type;
            info.json_str = null;
            info.supported_mod = product.supported_mod;
        }
        return info;
    }

    // public static void bulkInsertPlugIn(Context context, List<PlugInInfo>
    // pluginList) {
    // final int size = null == pluginList ? 0 : pluginList.size();
    // if (size > 0) {
    // ContentValues valueList[] = new ContentValues[size];
    // ContentValues pluginValues;
    // PlugInInfo plugin;
    // for (int i = 0; i < size; i++) {
    // pluginValues = valueList[i] = new ContentValues();
    // plugin = pluginList.get(i);
    // pluginValues.put(PlugInColumns.NAME, plugin.name);
    // pluginValues.put(PlugInColumns.PRODUCT_ID, plugin.product_id);
    // pluginValues.put(PlugInColumns.VERSION_NAME, plugin.version_name);
    // pluginValues.put(PlugInColumns.VERSION_CODE, plugin.version_code);
    // pluginValues.put(PlugInColumns.TYPE, plugin.type);
    // pluginValues.put(PlugInColumns.IS_APPLY,plugin.is_apply?1:0);
    // pluginValues.put(PlugInColumns.JSON_STR, plugin.json_str);
    // pluginValues.put(PlugInColumns.LOCAL_JSON_STR, plugin.local_json_str);
    // pluginValues.put(PlugInColumns.SUPPORTED_MOD, plugin.supported_mod);
    // pluginValues.put(PlugInColumns.BELONG_SYSTEM,Product.isBelongSystem(product.installed_file_path));
    // pluginValues.put(PlugInColumns.UPDATE_TIME,System.currentTimeMillis());
    // }
    // context.getContentResolver().bulkInsert(DownLoadProvider.getContentURI(context,
    // DownLoadProvider.TABLE_PLUGIN), valueList);
    // }
    // }

    public static void bulkInsertPlugInFromProduct(Context context, List<Product> productList) {
        final int size = null == productList ? 0 : productList.size();
        if (size > 0) {
            ContentValues valueList[] = new ContentValues[size];
            ContentValues pluginValues;
            Product product;
            for (int i = 0; i < size; i++) {
                pluginValues = valueList[i] = new ContentValues();
                product = productList.get(i);
                pluginValues.put(PlugInColumns.NAME, product.name);
                pluginValues.put(PlugInColumns.PRODUCT_ID, product.product_id);
                pluginValues.put(PlugInColumns.VERSION_NAME, product.version_name);
                pluginValues.put(PlugInColumns.VERSION_CODE, product.version_code);
                pluginValues.put(PlugInColumns.TYPE, product.type);
                pluginValues.put(PlugInColumns.FILE_PATH, product.installed_file_path);
                pluginValues.put(PlugInColumns.BELONG_SYSTEM, Product.isBelongSystem(product.installed_file_path));
                pluginValues.put(PlugInColumns.UPDATE_TIME, System.currentTimeMillis());
                pluginValues.put(PlugInColumns.IS_APPLY, 0);
                if (product != null) {
                    pluginValues.put(PlugInColumns.LOCAL_JSON_STR, ProductJSONImpl.createJsonObjectString(product));
                }
                pluginValues.put(PlugInColumns.SUPPORTED_MOD, product.supported_mod);
            }
            context.getContentResolver().bulkInsert(
                    DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), valueList);
        }
    }

    public static int getPreferRotation(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        final String orientation = settings.getString(KEY_ORIENTATION_PREFERRED, ORIENTATION_LANDSCAPE);

        if (orientation.equals(ORIENTATION_PORTRAIT)) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (orientation.equals(ORIENTATION_LANDSCAPE)) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_USER;
        }
    }

    public static void hasPurchase(Context context, final String product_id, final PurchaseListener listener) {

        RequestListener requestListener = new RequestListener() {
            @Override
            public void onIOException(IOException e) {
                listener.onError(e);
            }

            @Override
            public void onError(WutongException e) {
                listener.onError(e);
            }

            @Override
            public void onComplete(String response) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    boolean hasPurchased = obj.optBoolean("data");
                    listener.onComplete(hasPurchased);
                } catch (JSONException e) {
                    listener.onError(e);
                    e.printStackTrace();
                }

            }
        };
        ApiUtil instance = ApiUtil.getInstance();
        instance.hasPurchase(context, product_id, requestListener);
    }

    public static DialogFragment showProductsDialog(FragmentManager fm, String product_type, String supported_mod) {

        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ProductsDialog dialog = new ProductsDialog();
        dialog.setArguments(ProductsDialog.getArguments(product_type, supported_mod));
        dialog.show(ft, "dialog");
        return dialog;
    }

    private static OnDragListener mOnDragListener;

    public static void setOnDragListener(OnDragListener l) {
        mOnDragListener = l;
    }

    public static OnDragListener getOnDragListener() {
        return mOnDragListener;
    }

    public static interface OnDragListener {
        public void onStartDrag(String resourceID, float startX, float startY);

        public boolean onDrag(MotionEvent ev);
    }

    public static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream, int outW, int outH) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream,null,options);
        options.inSampleSize = generateInSampleSize(options, outW, outH);
        options.inJustDecodeBounds = false;
        options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeStream(inputStream,null,options);
    }
    public static Bitmap decodeSampledBitmapFromResource(String bf, int outW, int outH) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bf, options);
        options.inSampleSize = generateInSampleSize(options, outW, outH);
        options.inJustDecodeBounds = false;
        options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(bf, options);
    }

    private static int generateInSampleSize(BitmapFactory.Options options, int outW, int outH) {
        int height = options.outHeight;
        int width = options.outWidth;
        int size = 1;
        if (height > outH || width > outW) {
            if (width > height) {
                size = Math.round((float) height / (float) outH);
            } else {
                size = Math.round((float) width / (float) outW);
            }
        }
        return size;
    }

    public static void saveBitmap(Bitmap bitmap, String path, Bitmap.CompressFormat format) {
        try {
            File f = new File(path);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(format, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static String getMarketPath() {
        String str = get3DHomePath() + File.separator + "market";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }

    public static String getImageCatchPath() {
        String str = getMarketPath() + File.separator + ".image_cache";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }
    public static String getExportWallpaperCatchPath() {
        String str = getMarketPath() + File.separator + ".image_wallpaper_cache";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }
    
    public static String getDownloadPath() {
        String str = getMarketPath() + File.separator + "download";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }

    public static String getImageFilePath(URL imageUrl, boolean addHostAndPath) {
        return getImageFilePath(getImageCatchPath(), imageUrl, addHostAndPath);
    }

    public static String getImageFilePath(String path, URL imageUrl, boolean addHostAndPath) {
        if (addHostAndPath == false) {
            return path + File.separator + new File(getImageFileName(imageUrl.getFile())).getName();
        } else {
            String filename = imageUrl.getFile();
            filename = removeChar(filename);

            String host = imageUrl.getHost();
            if (isInTrustHost(host) == false) {

                filename = host + "_" + filename;
                if (filename.contains("/")) {
                    filename = filename.replace("/", "");
                }
            } else {
                Log.d(TAG, "***********   i am in trust=" + host + " filename=" + filename);
            }

            return path + File.separator + new File(filename).getName();
        }
    }

    public static void deleteFile(String path) {
        File f = new File(path);
        if (!f.exists())
            return;
        if (!(f.delete())) {
            File subs[] = f.listFiles();
            for (int i = 0; i <= subs.length - 1; i++) {
                if (subs[i].isDirectory())
                    deleteFile(subs[i].getAbsolutePath());
                subs[i].delete();
            }
            f.delete();
        }
    }

    private static String getImageFileName(String filename) {
        if (filename.contains("=") || filename.contains("?") || filename.contains("&") || filename.contains("%")) {
            filename = filename.replace("?", "");
            filename = filename.replace("=", "");
            filename = filename.replace("&", "");
            filename = filename.replace("%", "");
        }

        return filename;
    }

    private static String removeChar(String filename) {
        if (filename.contains("=") || filename.contains("?") || filename.contains("&") || filename.contains("%")) {
            filename = filename.replace("?", "");
            filename = filename.replace("=", "");
            filename = filename.replace("&", "");
            filename = filename.replace("%", "");
            filename = filename.replace(",", "");
            filename = filename.replace(".", "");
            filename = filename.replace("-", "");

        }
        return filename;
    }

    /*
     * photos-a.ak.fbcdn.net api.facebook.com secure-profile.facebook.com
     * ssl.facebook.com www.facebook.com x.facebook.com api-video.facebook.com
     * developers.facebook.com iphone.facebook.com developer.facebook.com
     * m.facebook.com s-static.ak.facebook.com secure-profile.facebook.com
     * secure-media-sf2p.facebook.com ssl.facebook.com profile.ak.facebook.com
     * b.static.ak.facebook.com
     * 
     * photos-h.ak.fbcdn.net photos-f.ak.fbcdn.net
     */
    private static boolean isInTrustHost(String host) {
        if (host.contains(".fbcdn.net"))
            return true;

        if (host.contains("secure-profile.facebook.com"))
            return true;

        return false;
    }

}
