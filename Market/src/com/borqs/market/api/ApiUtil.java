package com.borqs.market.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.borqs.market.account.AccountSession;
import com.borqs.market.db.OrderInfo;
import com.borqs.market.net.HttpManager;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.FileUploadTask;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.QiupuHelper;

public class ApiUtil extends WutongAPI {

    private static final String API_PRODUCTS_DETAIL = "api/v2/purchase/products/get";
    private static final String API_PRODUCTS_PURCHASE = "api/v2/purchase/purchase";
    private static final String API_PRODUCTS_PURCHASE_BATCH = "api/v2/purchase/purchase_batch";
    private static final String API_ACCOUNT_ACTIVE = "api/v2/account/active";
    private static final String API_PRODUCTS_LIST = "api/v2/purchase/products/list";
    private static final String API_RECOMMEND_LIST = "api/v2/purchase/promotions/get";
    private static final String API_RECOMMEND_PARTITIONS_LIST = "api/v2/purchase/partitions/products";
    private static final String API_COMMENT_LIST = "api/v2/purchase/products/comments/list";
    private static final String API_COMMENT_UPDATE = "api/v2/purchase/products/comments/update";
    private static final String API_COMMENT_MY = "api/v2/purchase/products/comments/my";
    private static final String API_SHARE_LIST = "api/v2/purchase/shares/list";
    private static final String API_HAS_PURCHASED = "api/v2/purchase/has_purchased";
    private static final String API_NEWEST_CREATED_AT = "api/v2/purchase/newest_created_at";

    public static final String SUPPORTED_MODE = "supported_mod";
    public static final String APP_MODE = "app_mod";

    public void getProductList(Context context, final int page,
            final int count, final String category, final int versionCode,
            final String packageName, String supported_mod, String order_by, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        bundle.add("app_version", versionCode);
        bundle.add("app_id", packageName);
        bundle.add("category_id", category);
        if (MarketUtils.IS_ONLY_FREE) {
            bundle.add("paid", 1);
        }else {
            bundle.add("paid", 0);
        }
        bundle.add("page", String.valueOf(page));
        bundle.add("count", String.valueOf(count));
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("locale", Locale.getDefault().toString());
        bundle.add("device_id", QiupuHelper.getDeviceID(context));
        if(!TextUtils.isEmpty(supported_mod)) {
            bundle.add(APP_MODE, supported_mod);
        }
        
        if(!TextUtils.isEmpty(order_by)) {
            bundle.add("order_by", order_by);
        }
        
        request(getApiUrl(API_PRODUCTS_LIST), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void getPartitionsList(Context context, final int page,
            final int count, final int versionCode,
            final String partitions_id, String supported_mod, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        bundle.add("id", partitions_id);
        bundle.add("app_version", versionCode);
        bundle.add("page", String.valueOf(page));
        bundle.add("count", String.valueOf(count));
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("locale", Locale.getDefault().toString());
        bundle.add("device_id", QiupuHelper.getDeviceID(context));
        if(!TextUtils.isEmpty(supported_mod)) {
            bundle.add(APP_MODE, supported_mod);
        }
        request(getApiUrl(API_RECOMMEND_PARTITIONS_LIST), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void getUserShareList(Context context, final int page,
            final int count, final String category, final String packageName, String supported_mod, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        bundle.add("app_id", packageName);
        bundle.add("category_id", category);
        bundle.add("page", String.valueOf(page));
        bundle.add("count", String.valueOf(count));
        if(!TextUtils.isEmpty(supported_mod)) {
            bundle.add(APP_MODE, supported_mod);
        }
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("locale", Locale.getDefault().toString());
        bundle.add("device_id", QiupuHelper.getDeviceID(context));
        request(getApiUrl(API_SHARE_LIST), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }

    public void getCommentList(Context context, final int page,
            final int count, final int version,
            final String product_id, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        
        if(version > 0) {
            bundle.add("version", version);
        }
        bundle.add("product_id", product_id);
        bundle.add("page", String.valueOf(page));
        bundle.add("count", String.valueOf(count));
        request(getApiUrl(API_COMMENT_LIST), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void createComment(Context context, final int version,
            final String product_id, final String message, final float rating, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        
        bundle.add("version", version);
        bundle.add("product_id", product_id);
        bundle.add("message", message);
        bundle.add("rating", String.valueOf(rating));
        bundle.add("ticket", AccountSession.account_session);
        request(getApiUrl(API_COMMENT_UPDATE), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void hasPurchase(Context context, final String product_id, final RequestListener listener) {
        
        WutongParameters bundle = new WutongParameters();
        
        bundle.add("ids", product_id);
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("for_one", 1);
        
        request(getApiUrl(API_HAS_PURCHASED), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }

    public void getMyComment(Context context, final int version,
            final String product_id, final RequestListener listener) {

        WutongParameters bundle = new WutongParameters();
        
        bundle.add("version", version);
        bundle.add("product_id", product_id);
        bundle.add("ticket", AccountSession.account_session);
        request(getApiUrl(API_COMMENT_MY), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }

    public void getProductDetail(Context context, final String product_id,
            final int version, final String supported_mod, final RequestListener listener) {

        WutongParameters bundle = new WutongParameters();
        bundle.add("id", product_id);
        if(version > 0) {
            bundle.add("version", version);
        }
        if(!TextUtils.isEmpty(supported_mod)) {
            bundle.add(APP_MODE, supported_mod);
        }
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("locale", Locale.getDefault().toString());
        bundle.add("device_id", QiupuHelper.getDeviceID(context));

        request(getApiUrl(API_PRODUCTS_DETAIL), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }

    public void productPurchase(Context context, final String product_id,
            final int version,WutongParameters bundle, String data1, final RequestListener listener) {
        if(bundle == null) {
            BLog.e("productPurchase()  why is bundle null?");
            return;
        }
        bundle.add("id", product_id);
        bundle.add("version", version);
        if(!TextUtils.isEmpty(data1)) {
            bundle.add("data1", data1);
        }
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("device_id", QiupuHelper.getDeviceID(context));
        bundle.add("locale", Locale.getDefault().toString());
        request(getApiUrl(API_PRODUCTS_PURCHASE), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void batchOrder(Context context, ArrayList<OrderInfo> orders, final RequestListener listener) {
        try {
        if(orders == null) {
            return;
        }
        if(orders.size() == 0) return;
        WutongParameters bundle = new WutongParameters();
        JSONArray arry = new JSONArray();
                for(OrderInfo order : orders) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", order.product_id);
                    obj.put("version", order.version_code);
                    obj.put("google_iab_order", order.iab_order_id);
                    if(order.purchase != null && !TextUtils.isEmpty(order.purchase.getOriginalJson())) {
                        obj.put("data1", order.purchase.getOriginalJson());
                    }
                    arry.put(obj);
                }
        bundle.add("data", arry.toString());
        bundle.add("ticket", AccountSession.account_session);
        bundle.add("device_id", QiupuHelper.getDeviceID(context));
        bundle.add("locale", Locale.getDefault().toString());
        request(getApiUrl(API_PRODUCTS_PURCHASE_BATCH), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void getRecommendList(Context context,
            final String category_id, final RequestListener listener) {
        WutongParameters bundle = new WutongParameters();
        bundle.add("app_id", context.getPackageName());
        bundle.add("category_id", category_id);
        bundle.add("locale", Locale.getDefault().toString());
        request(getApiUrl(API_RECOMMEND_LIST), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void getNewest(Context context, final RequestListener listener) {
        WutongParameters bundle = new WutongParameters();
        bundle.add("app_id", context.getPackageName());
        request(getApiUrl(API_NEWEST_CREATED_AT), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void accountActiveByPhone(Context context,final String phoneNumber, 
            final RequestListener listener) {
        WutongParameters bundle = new WutongParameters();
        bundle.add("phone", phoneNumber);
        request(getApiUrl(API_ACCOUNT_ACTIVE), bundle,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    public void accountActiveByGoogle(WutongParameters params, final RequestListener listener) {
        request(getApiUrl(API_ACCOUNT_ACTIVE), params,
                HttpManager.HTTPMETHOD_GET, listener, false);
    }
    
    public void shareUpload(String app_id, int app_version, String category_id, String supported_mod, File file, final RequestListener listener) {
        new  FileUploadTask(app_id, app_version, category_id, AccountSession.account_session, supported_mod, listener).execute(file);
    }

    public String getApiUrl(String url) {
        StringBuffer sb = null;
        if(MarketConfiguration.isIS_DEBUG_TESTSERVER()) {
            sb = new StringBuffer(API_SERVER_TEST);
//            sb = new StringBuffer("http://192.168.5.22:6789/");
        }else {
            sb = new StringBuffer(API_SERVER);
        }
        sb.append(url);
        return sb.toString();
    }
    
    private static ApiUtil instance = null;

    public synchronized static ApiUtil getInstance() {
        if (instance == null) {
            instance = new ApiUtil();
        }
        return instance;
    }

    @Override
    protected void request(final String url, final WutongParameters params,
                           final String httpMethod, RequestListener listener, boolean needToken) {
        setBetaParam(params, false);
        request(url, params, httpMethod, listener);
    }

    private void setBetaParam(WutongParameters params, boolean force) {
        if(force || MarketConfiguration.isIS_DEBUG_BETA_REQUEST()) {
            params.add("with_beta", 1);
        }else {
            params.add("with_beta", 0);
        }
    }
}
