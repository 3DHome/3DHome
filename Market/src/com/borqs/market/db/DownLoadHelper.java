package com.borqs.market.db;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.market.account.AccountSession;
import com.borqs.market.json.Product;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.ThemeXmlParser;
import com.iab.engine.MarketBillingResult;
import com.iab.engine.google.util.Purchase;
import com.support.StaticUtil;

public class DownLoadHelper {
    private final static String TAG = "DownLoadHelper";
    private static DownLoadHelper _instance;
    private Context mContext;
    
    private static final HandlerThread sWorkerThread = new HandlerThread("market-db");
    static {
        sWorkerThread.start();
    }
    public static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private DownLoadHelper(Context context) {
        mContext = context;
    }

    public DownLoadHelper(Context context, boolean forSync) {
        mContext = context;
    }

    public static DownLoadHelper getInstance(Context context) {
        if (_instance == null) {
            _instance = new DownLoadHelper(context);
        }

        return _instance;
    }

    public static void closeCursor(Cursor cursor) {
        if (null != cursor && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }

    public void insert(Product p, String path, long down_id, String jsonString) {
        ContentValues values = new ContentValues();
        values.put(DownloadInfoColumns.NAME, p.name);
        values.put(DownloadInfoColumns.LOCAL_PATH, path);
        values.put(DownloadInfoColumns.PRODUCT_ID, p.product_id);
        values.put(DownloadInfoColumns.DOWNLOAD_ID, down_id);
        values.put(DownloadInfoColumns.TYPE, p.type);
        values.put(DownloadInfoColumns.SIZE, p.size);
        values.put(DownloadInfoColumns.VERSION_NAME, p.version_name);
        values.put(DownloadInfoColumns.JSON_STR, jsonString);
        values.put(DownloadInfoColumns.SUPPORTED_MOD, p.supported_mod);
        values.put(DownloadInfoColumns.VERSION_CODE, p.version_code);
        values.put(DownloadInfoColumns.DOWNLOAD_STATUS,
                DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING);
        if(isHasDownloadFile(p.product_id)) {
            String where = DownloadInfoColumns.PRODUCT_ID + "='" + p.product_id
                    + "'";
            mContext.getContentResolver().update(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                    values, where, null);
        }else {
            mContext.getContentResolver().insert(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                    values);
        }
    }

    public void delete(String name) {
        mContext.getContentResolver().delete(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                DownloadInfoColumns.NAME + " = '" + name + "'", null);
    }

    public void update(ContentValues values, String where) {
        mContext.getContentResolver().update(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                values, where, null);
    }

    public void updateDownloadStatus(long downloadID, int status, String uri, long size) {
        ContentValues values = new ContentValues();
        String where = DownloadInfoColumns.DOWNLOAD_ID + "='" + downloadID
                + "'";
        values.put(DownloadInfoColumns.DOWNLOAD_STATUS, status);
        values.put(DownloadInfoColumns.LOCAL_PATH, uri);
        values.put(DownloadInfoColumns.SIZE, size);
        mContext.getContentResolver().update(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                values, where, null);
    }

    public Cursor queryDownloadingByDownloadId(long downloadID) {
        String where = DownloadInfoColumns.DOWNLOAD_ID + "='" + downloadID
                + "' and " + DownloadInfoColumns.DOWNLOAD_STATUS + "='"
                        + DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING + "'";
        return queryFile(where);
    }

    public boolean isHasDownloadFile(String productID) {
        String where = DownloadInfoColumns.PRODUCT_ID + "='" + productID + "'";
        Cursor c = queryFile(where);
        if (c.getCount() > 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    public void deleteFile(String productID, int version_code) {
        String where = DownloadInfoColumns.PRODUCT_ID + "='" + productID
                + "' and " + DownloadInfoColumns.VERSION_CODE + "='" + version_code
                + "' and " + DownloadInfoColumns.DOWNLOAD_STATUS + "='"
                + DownloadInfoColumns.DOWNLOAD_STATUS_COMPLETED + "'";
        mContext.getContentResolver().delete(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                where, null);
    }
    
    public void cancleDownloadFile(String productID) {
        String where = DownloadInfoColumns.PRODUCT_ID + "='" + productID +  "'";
        mContext.getContentResolver().delete(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                where, null);
    }

    public Cursor queryDownloadFile(String productID) {
        String where = DownloadInfoColumns.PRODUCT_ID + "='" + productID
                + "'";
        String orderby = DownloadInfoColumns.VERSION_CODE + " desc," + DownloadInfoColumns.VERSION_NAME + " desc";
        Cursor c = mContext.getContentResolver().query(
                DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                DownloadInfoColumns.PROJECTION, where, null,orderby);
        return c;
    }
    
    public static Cursor queryAllDownloading(Context context) {
        String where = DownloadInfoColumns.DOWNLOAD_STATUS + "='" + DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING
                + "'";
        String orderby = DownloadInfoColumns.DOWNLOAD_ID;
        Cursor c = context.getContentResolver().query(
                DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_DOWNLOAD),
                new String[]{DownloadInfoColumns.PRODUCT_ID, DownloadInfoColumns.DOWNLOAD_ID}, where, null,orderby);
        return c;
    }

    public static Product queryLocalProductInfo(Context context, String productID) {
        String where = PlugInColumns.PRODUCT_ID + " ='" + productID + "'";
        Cursor cursor = context.getContentResolver().query(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),PlugInColumns.PROJECTION, where, null, null);
        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            String json_str = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.JSON_STR));
            String local_json_str = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.LOCAL_JSON_STR));
            product = ProductJSONImpl.createProduct(json_str, local_json_str);
            
            if(product != null) {
                product.name = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.NAME));
                product.product_id = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.PRODUCT_ID));
                product.version_name = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.VERSION_NAME));
                product.version_code = cursor.getInt(cursor.getColumnIndexOrThrow(PlugInColumns.VERSION_CODE));
                product.type = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.TYPE));
                product.supported_mod = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.SUPPORTED_MOD));
                product.installed_file_path = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.FILE_PATH));
            }
        }
        DownLoadHelper.closeCursor(cursor);
        return product;
    }
    
     public static ArrayList<Product> queryLocalProductList(final Context context, String supported_mod, String category) {
         String orderby = null;
         if(Product.isSupportApply(category)) {
             orderby = PlugInColumns.IS_APPLY + " desc , " + PlugInColumns.BELONG_SYSTEM + " asc , " + PlugInColumns.UPDATE_TIME + " desc ";
         }else {
             orderby = PlugInColumns.BELONG_SYSTEM + " asc , " + PlugInColumns.UPDATE_TIME + " desc ";
         }
         StringBuilder where = new StringBuilder(PlugInColumns.TYPE).append("='").append(category).append("'");
         if (!TextUtils.isEmpty(supported_mod)) {
             where.append(" AND ").
                     append("(").
                     append(PlugInColumns.SUPPORTED_MOD).append(" IS NULL").
                     append(" OR ").
                     append(PlugInColumns.SUPPORTED_MOD).append(" LIKE '%").append(supported_mod).append("%'").
                     append(")");
         }
//         final String[] whereArgs = new String[]{category,};
         Cursor cursor = context.getContentResolver().query(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN),
                 PlugInColumns.PROJECTION, where.toString(), null, orderby);
         ArrayList<Product>  productList = null;
         StringBuilder invalidProductIds = null;
         if (cursor != null) {
             if( cursor.getCount() > 0) {
                 productList = new ArrayList<Product>();
                 while(cursor.moveToNext()) {
                     
                    String filePath =  cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.FILE_PATH));
                    long start_time = System.currentTimeMillis();
                    
                    if(!TextUtils.isEmpty(filePath) && ( filePath.startsWith("asset://") || (new File(filePath)).exists())) {
                        long span = System.currentTimeMillis() - start_time;
                        BLog.d(TAG, "queryLocalProductList file exists span time = " + span);
                        Product product = null;
                        String json_str = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.JSON_STR));
                        String local_json_str = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.LOCAL_JSON_STR));
                        product = ProductJSONImpl.createProduct(json_str, local_json_str);
                        
                        if(product != null) {
                            product.name = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.NAME));
                            product.product_id = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.PRODUCT_ID));
                            product.version_name = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.VERSION_NAME));
                            product.version_code = cursor.getInt(cursor.getColumnIndexOrThrow(PlugInColumns.VERSION_CODE));
                            product.type = cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.TYPE));
                            product.isApplyed = cursor.getInt(cursor.getColumnIndexOrThrow(PlugInColumns.IS_APPLY)) == 1? true : false;
                            product.installed_file_path = filePath;
                            
                            productList.add(product);
                        }
                    }else {
                        
                        String productId =  cursor.getString(cursor.getColumnIndexOrThrow(PlugInColumns.PRODUCT_ID));
                        if(!TextUtils.isEmpty(productId)) {
                            if(invalidProductIds == null) {
                                invalidProductIds = new StringBuilder();
                            }
                            
                            if(invalidProductIds.length() > 0) {
                                invalidProductIds.append(" , ");
                            }
                            invalidProductIds.append("'" + productId + "'");
                            productId = null;
                            
                        }
                    }
                 }
             }
             cursor.close();
         }
         
         if(invalidProductIds != null) {
             final String ids = invalidProductIds.toString();
             sWorker.post(new Runnable() {
                
                @Override
                public void run() {
                    Log.d(TAG, " delete invalid product ids= " + ids);
                    DownLoadHelper.deletePlugInByIds(context, ids);
                }
            });
         }
         return productList;
     }
     
     public static void deletePlugInById(Context context, String id) {
         try{
             if(!TextUtils.isEmpty(id)) {
                 String where = PlugInColumns.PRODUCT_ID + "='" + id + "'";
                 context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), where, null);
             }
         }catch(Exception e) {
             Log.e(TAG, e.getMessage());
         }
     }
     public static void deletePlugInByIds(Context context, String ids) {
         try{
             if(!TextUtils.isEmpty(ids)) {
                 String where = PlugInColumns.PRODUCT_ID + "  in(" + ids + ") ";
                 context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), where, null);
             }
         }catch(Exception e) {
             Log.e(TAG, e.getMessage());
         }
     }
    public static void deleteSystemPlugIn(Context context,boolean isSystem) {
        try{
                String where = null;
                if(isSystem) {
                    where = PlugInColumns.BELONG_SYSTEM + "  = 1 ";
                }else {
                    
                    where = PlugInColumns.BELONG_SYSTEM + "  = 0 ";
                }
                context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), where, null);
            }catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
    }
    
    public Cursor queryFile(String where) {
        Cursor cursor = mContext.getContentResolver().query(
                DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_DOWNLOAD),
                DownloadInfoColumns.PROJECTION, where, null, null);
        return cursor;
    }


    public Cursor queryTheme(String productID) {
        String where = PlugInColumns.PRODUCT_ID + " ='" + productID
                + "'";
        String orderby = PlugInColumns.VERSION_CODE + " desc ";
        Cursor cursor = mContext.getContentResolver().query(DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_PLUGIN),PlugInColumns.PROJECTION, where, null, orderby);
        return cursor;
    }

    public static void insertIabOder(Context context, Purchase p) {
        try{
            if(p == null) return;
            ContentValues values = new ContentValues();
            String payload = p.getDeveloperPayload();
            String product_id = Product.getProductIdFromPayload(payload);
            String user_id = Product.getUserIdFromPayload(payload);
            values.put(OrderInfoColumns.PRODUCT_ID, product_id);
            if(TextUtils.isEmpty(user_id)) {
                values.put(OrderInfoColumns.USER_ID, AccountSession.account_id);
            }else {
                values.put(OrderInfoColumns.USER_ID, user_id);
            }
            values.put(OrderInfoColumns.PAY_CODE, p.getSku());
            values.put(OrderInfoColumns.PAY_TYPE, MarketBillingResult.TYPE_IAB);
            values.put(OrderInfoColumns.ITEM_TYPE, p.getItemType());
            values.put(OrderInfoColumns.JSON_PURCHASE_INFO, p.getOriginalJson());
            values.put(OrderInfoColumns.SIGNATURE, p.getSignature());
            values.put(OrderInfoColumns.VERSION_CODE, Product.getVersionCodeFromPayload(payload));
            values.put(OrderInfoColumns.HAS_CONSUMED, 0);
            if(!TextUtils.isEmpty(product_id)) {
                String where = OrderInfoColumns.PRODUCT_ID + "='" + product_id + "'";
                int count = context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER), values, where, null);
                if(count == 0) {
                    values.put(OrderInfoColumns.HAS_ORDERED, 0);
                    context.getContentResolver().insert(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER), values);
                }
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    public static void updateIabOderStatus(Context context, String product_id, boolean has_ordered) {
        try{
            String userID = AccountSession.account_id;
            if(TextUtils.isEmpty(userID)) return;
            ContentValues values = new ContentValues();
            values.put(OrderInfoColumns.HAS_ORDERED, has_ordered? 1:0);
            if(!TextUtils.isEmpty(product_id)) {
                String where = OrderInfoColumns.PRODUCT_ID + "='" + product_id + "'   and " + OrderInfoColumns.USER_ID + " = '" + userID + "'";
                context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER), values, where, null);
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    public static void batchUpdateIabOderStatus(Context context, String ids, boolean has_ordered) {
        try{
            String userID = AccountSession.account_id;
            if(TextUtils.isEmpty(userID)) return;
            ContentValues values = new ContentValues();
            values.put(OrderInfoColumns.HAS_ORDERED, has_ordered? 1:0);
            if(!TextUtils.isEmpty(ids)) {
                String where = OrderInfoColumns.PRODUCT_ID + "  in(" + ids + ")  and " + OrderInfoColumns.USER_ID + " = '" + userID + "'";
                context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER), values, where, null);
            }
            }catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
    }
    
    public static void updateIabConsumeStatus(Context context, String product_id, boolean has_consumed) {
        try{
            String userID = AccountSession.account_id;
            if(TextUtils.isEmpty(userID)) return;
            ContentValues values = new ContentValues();
            values.put(OrderInfoColumns.HAS_CONSUMED, has_consumed? 1:0);
            if(!TextUtils.isEmpty(product_id)) {
                String where = OrderInfoColumns.PRODUCT_ID + "='" + product_id + "'   and " + OrderInfoColumns.USER_ID + " = '" + userID + "'";
                context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER), values, where, null);
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    public static ArrayList<OrderInfo> queryOrderList(Context context, boolean has_ordered, boolean has_consumed) {
        ArrayList<OrderInfo>  list = null;
        try{
            String userID = AccountSession.account_id;
            if(TextUtils.isEmpty(userID)) return null;
            StringBuilder where = new StringBuilder(OrderInfoColumns.HAS_ORDERED + " = '" + (has_ordered?1:0) + "'");
            where.append(" and " + OrderInfoColumns.HAS_CONSUMED + " = '" + (has_consumed?1:0) + "'");
            where.append(" and " + OrderInfoColumns.USER_ID + " = '" + userID + "'");
            Cursor cursor = context.getContentResolver().query(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_ORDER),OrderInfoColumns.PROJECTION, where.toString(), null, null);
            if (cursor != null) {
                if( cursor.getCount() > 0) {
                    list = new ArrayList<OrderInfo>();
                    while(cursor.moveToNext()) {
                        OrderInfo orderInfo = new OrderInfo();
                        orderInfo.product_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PRODUCT_ID));
                        orderInfo.user_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.USER_ID));
                        orderInfo.pay_code = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PAY_CODE));
                        orderInfo.pay_type = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PAY_TYPE));
                        orderInfo.item_type = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.ITEM_TYPE));
                        orderInfo.jsonPurchaseInfo = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.JSON_PURCHASE_INFO));
                        orderInfo.signature = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.SIGNATURE));
                        orderInfo.version_code = cursor.getInt(cursor.getColumnIndexOrThrow(OrderInfoColumns.VERSION_CODE));
                        orderInfo.iab_order_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.IAB_ORDER_ID));
                        orderInfo.has_ordered = has_ordered;
                        orderInfo.has_consumed = has_consumed;
                        try {
                            orderInfo.purchase = new Purchase(orderInfo.item_type, orderInfo.jsonPurchaseInfo, orderInfo.signature);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        list.add(orderInfo);
                    }
                }
                cursor.close();
            }
        }catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return list;
    }
    
    public OrderInfo isHasorder(String productID) {
        String userID = AccountSession.account_id;
        if(TextUtils.isEmpty(userID)) return null;
        String where = OrderInfoColumns.PRODUCT_ID + "='" + productID + "'   and " + OrderInfoColumns.USER_ID + " = '" + userID + "'";
        Cursor cursor = mContext.getContentResolver().query(
                DownLoadProvider.getContentURI(mContext, DownLoadProvider.TABLE_ORDER),
                OrderInfoColumns.PROJECTION, where, null, null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.product_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PRODUCT_ID));
            orderInfo.user_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.USER_ID));
            orderInfo.pay_code = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PAY_CODE));
            orderInfo.pay_type = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.PAY_TYPE));
            orderInfo.item_type = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.ITEM_TYPE));
            orderInfo.jsonPurchaseInfo = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.JSON_PURCHASE_INFO));
            orderInfo.signature = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.SIGNATURE));
            orderInfo.version_code = cursor.getInt(cursor.getColumnIndexOrThrow(OrderInfoColumns.VERSION_CODE));
            orderInfo.iab_order_id = cursor.getString(cursor.getColumnIndexOrThrow(OrderInfoColumns.IAB_ORDER_ID));
            orderInfo.has_ordered = cursor.getInt(cursor.getColumnIndexOrThrow(OrderInfoColumns.HAS_ORDERED)) == 1 ? true:false;
            orderInfo.has_consumed = cursor.getInt(cursor.getColumnIndexOrThrow(OrderInfoColumns.HAS_CONSUMED)) == 1 ? true:false;
            try {
                orderInfo.purchase = new Purchase(orderInfo.item_type, orderInfo.jsonPurchaseInfo, orderInfo.signature);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
            return orderInfo;
        } else {
            cursor.close();
            return null;
        }
    }
    
    public static int updatePluginWithDownloadInfo(Context context, Product info, File file) {
        ContentValues pluginValues = new ContentValues();
        String where = PlugInColumns.PRODUCT_ID + " = '" + info.product_id + "'";
        pluginValues.put(PlugInColumns.NAME, info.name);
        pluginValues.put(PlugInColumns.VERSION_NAME, info.version_name);
        pluginValues.put(PlugInColumns.VERSION_CODE, info.version_code);
        pluginValues.put(PlugInColumns.FILE_PATH, file.getPath());
        pluginValues.put(PlugInColumns.BELONG_SYSTEM, Product.isBelongSystem(file.getPath()));
        pluginValues.put(PlugInColumns.UPDATE_TIME, System.currentTimeMillis());
        pluginValues.put(PlugInColumns.LOCAL_JSON_STR, ProductJSONImpl.createJsonObjectString(info));
        pluginValues.put(PlugInColumns.SUPPORTED_MOD, info.supported_mod);
        
        int count =  context.getContentResolver().update(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), pluginValues, where, null);
        return count;
   }
    
    public static void insertPlugIn(Context context, Product info, File file) {
        if(file == null) {
            throw new IllegalAccessError("file can't null");
        }
        ContentValues pluginValues = new ContentValues();
        pluginValues.put(PlugInColumns.NAME, info.name);
        pluginValues.put(PlugInColumns.PRODUCT_ID, info.product_id);
        pluginValues.put(PlugInColumns.VERSION_NAME, info.version_name);
        pluginValues.put(PlugInColumns.VERSION_CODE, info.version_code);
        pluginValues.put(PlugInColumns.TYPE, info.type);
        pluginValues.put(PlugInColumns.FILE_PATH, file.getPath());
        pluginValues.put(PlugInColumns.BELONG_SYSTEM, Product.isBelongSystem(file.getPath()));
        pluginValues.put(PlugInColumns.UPDATE_TIME, System.currentTimeMillis());
        pluginValues.put(PlugInColumns.LOCAL_JSON_STR, ProductJSONImpl.createJsonObjectString(info));
        pluginValues.put(PlugInColumns.SUPPORTED_MOD, info.supported_mod);
        context.getContentResolver().insert(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), pluginValues);
    }
}
