package com.borqs.information;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.information.util.InformationConstant;
import com.borqs.market.account.AccountListener;
import com.borqs.market.account.AccountObserver;
import com.borqs.market.account.AccountSession;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.OrderInfo;
import com.borqs.market.json.Product;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.iab.engine.google.util.IabHelper;
import com.iab.engine.google.util.IabResult;
import com.iab.engine.google.util.Inventory;
import com.iab.engine.google.util.Purchase;
import com.iab.framework.MarketIab;

public class IabManager  implements AccountListener{
    private static final String TAG = IabManager.class.getSimpleName();
    
    public static final String SHARE_PREFERENCES_IAB_SETTING = "iab_setting";
    public static final String SP_EXTRAS_LAST_SYNC = "last_sync_time";
    public static final String SP_EXTRAS_ERROR_COUNT = "SP_EXTRAS_ERROR_COUNT";

    private ApiUtil mApiUtil;
    private Context mContext;
    private Service mService;
    private MarketIab marketIab;

    private Handler mHandler;
    public static int nErrorCount = 0;

    public IabManager(Context context,Service service) {
    	mContext = context;
    	mService = service;
        mHandler = new MainHandler();
        marketIab = new MarketIab(mContext, queryInvListener);
        nErrorCount = getErrorCount(context);
        AccountObserver.registerAccountListener(IabManager.class.getName(), this);
    }

    public void destroy() {
        AccountObserver.unRegisterAccountListener(IabManager.class.getName());
        marketIab.depose();
    }

    private final static int BATCH_PURCHASE = 1;
    private final static int BATCH_PURCHASE_END = 2;
    private final static int QUERY_PURCHASES = 3;
    private final static int CONSUME_PURCHASE = 4;
    private final static String RESULT = "RESULT";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
            Log.d(TAG, "new FriendsHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            if(!AccountSession.isLogin()) return;
            switch (msg.what) {
            case BATCH_PURCHASE: {
                batchOrder();
                break;
            }
            case QUERY_PURCHASES: {
                queryPurchases();
                break;
            }
            case BATCH_PURCHASE_END: {
                boolean suc = msg.getData().getBoolean("RESULT");
                if (suc) {
                    setErrorCount(mContext, 0);
                    mHandler.sendEmptyMessage(CONSUME_PURCHASE);
                } else {
                    setErrorCount(mContext, nErrorCount + 1);
                    rescheduleSync(false, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_UPLOAD);
                    destroy();
                }
                break;
            }
            case CONSUME_PURCHASE: {
                consumeAsync();
                break;
            }
            }
        }
    }

//    private final Object mLocked = new Object();
    public void batchOrder() {
        BLog.v(TAG, "-------------------batchOrder()------------------");
        final ArrayList<OrderInfo> orders = DownLoadHelper.queryOrderList(mContext, false, false);
        if(orders != null && orders.size() > 0) {
            mApiUtil = ApiUtil.getInstance();
            mApiUtil.batchOrder(mContext, orders,
                    new RequestListener() {
                
                @Override
                public void onIOException(IOException e) {
                    BLog.e(TAG,
                            "productPurchase onIOException  "
                                    + e.getMessage());
//                    synchronized (mLocked) {
//                        isLoading = false;
//                    }
                    
                    Message mds = mHandler.obtainMessage(BATCH_PURCHASE_END);
                    mds.getData().putBoolean(RESULT, false);
                    mHandler.sendMessage(mds);
                    
                    if(mContext != null) {
                        BLog.d(TAG, "Market Report: user_id = " + AccountSession.account_id + "  batchOrder()  IOException:" + e.toString());
                    }
                }
                
                @Override
                public void onError(WutongException e) {
                    BLog.e(TAG,
                            "productPurchase onError  " + e.getMessage());
//                    synchronized (mLocked) {
//                        isLoading = false;
//                    }
                    
                    Message mds = mHandler.obtainMessage(BATCH_PURCHASE_END);
                    mds.getData().putBoolean(RESULT, false);
                    mHandler.sendMessage(mds);
                    
                    if(mContext != null) {
                        BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + "  batchOrder()  WutongException:" + e.toString());
                    } 
                    
                }
                
                @Override
                public void onComplete(String response) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject(response);
                        if (!obj.has("data"))
                            return;
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
//                    synchronized (mLocked) {
//                        isLoading = false;
//                    }
                    
                    if(orders != null && orders.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for(int index = 0; index < orders.size(); index++) {
                            OrderInfo order = orders.get(index);
                            if(order != null) {
                                if(!TextUtils.isEmpty(order.product_id)) {
                                    if(sb.length() > 0) {
                                        sb.append(" , ");
                                    }
                                    sb.append("'" + order.product_id + "'");
                                    order.product_id = null;
                                }
                            }
                        }
                        
                        if(sb.length() > 0) {
                            DownLoadHelper.batchUpdateIabOderStatus(mContext, sb.toString(), true);
                        }
                        
                    }
                    
                    Message mds = mHandler.obtainMessage(BATCH_PURCHASE_END);
                    mds.getData().putBoolean(RESULT, true);
                    mHandler.sendMessage(mds);
                    if(mContext != null) {
                        BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + " batchOrder()  response = " + response);
                    } 
                    
                }
            });
        }else {
            Message mds = mHandler.obtainMessage(BATCH_PURCHASE_END);
            mds.getData().putBoolean(RESULT, true);
            mHandler.sendMessage(mds);
        }
    }
    
    public void rescheduleSync(boolean force , int type) {
        BLog.v(TAG, "-------------------rescheduleSync()------------------type=" + type);
        long delaytime = 0L;
        
        if(force) {
            setErrorCount(mContext, 0);
        }else {
            nErrorCount = getErrorCount(mContext);
        }
        if(nErrorCount >= 10) {
            setErrorCount(mContext, 0);
        }
        delaytime = 60 * 1000L * ((int)Math.pow(2, nErrorCount) - 1);
        
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        long current_time = System.currentTimeMillis();
        long nexttime = current_time + delaytime;
        BLog.v(TAG, "-------------------rescheduleSync()------------------delaytime = " + delaytime);

        Intent intent = new Intent(mContext, mService.getClass());
        intent.setAction(InformationConstant.MARKET_IAB_SERVICE_ACTION);
        intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, type);
        PendingIntent phonebookpi = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.RTC, nexttime, phonebookpi);
    }
    
    public void queryPurchases() {
        BLog.v(TAG, "-------------------queryPurchases()------------------");
        marketIab.queryPurchases(new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory, List<Purchase> allPurchases) {
            BLog.v(TAG, "-------------------queryPurchases()------------result.isSuccess=" + result.isSuccess() +"   "+ result.getMessage());
            if (result.isFailure()) {
                setErrorCount(mContext, nErrorCount + 1);
                rescheduleSync(false, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE);
                destroy();
                return;
            }
            setErrorCount(mContext, 0);
            BLog.v(TAG, "-------------------queryPurchases()------------result.isSucess------");
            if(mContext != null) {
                BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + "  queryPurchases()  result.isFailure()" + result.isFailure());
            } 
            if(allPurchases != null && allPurchases.size() > 0) {
                BLog.v(TAG, "-------------------queryPurchases()------------ size = " + allPurchases.size());
                // write to DB
                for(Purchase p : allPurchases) {
                    DownLoadHelper.insertIabOder(mContext, p);
                }
                
                //upload order
                Message msd = mHandler.obtainMessage(BATCH_PURCHASE);
                mHandler.sendMessage(msd);
            } else {
                BLog.v(TAG, "-------------------queryPurchases()------------ size = 0");
            }
        }
        });
    }
    
    IabHelper.QueryInventoryFinishedListener queryInvListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory, List<Purchase> allPurchases) {
            BLog.d(TAG, "onQueryInventoryFinished ");
            BLog.v(TAG, "-------------------queryPurchases()------------result.isSuccess=" + result.isSuccess() +"   "+ result.getMessage());
            if (result.isFailure()) {
                setErrorCount(mContext, nErrorCount + 1);
                rescheduleSync(false, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE);
                destroy();
                return;
            }
            setErrorCount(mContext, 0);
            BLog.d(TAG, "-------------------queryPurchases()------------result.isSucess------");
            if(mContext != null) {
                BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + "  queryPurchases()  result.isFailure()" + result.isFailure());
            } 
            if(allPurchases != null && allPurchases.size() > 0) {
                BLog.d(TAG, "onQueryInventoryFinished unConsume purchase size = " + allPurchases.size());
                // write to DB
                for(Purchase p : allPurchases) {
                    DownLoadHelper.insertIabOder(mContext, p);
                }
                
                //upload order
                Message msd = mHandler.obtainMessage(BATCH_PURCHASE);
                mHandler.sendMessage(msd);
            }else {
                BLog.d(TAG, "onQueryInventoryFinished unConsume purchase size = 0");
            }
        }
        };
    
    public void consumeAsync() {
        BLog.v(TAG, "-------------------consumeAsync()------------------");
        List<OrderInfo> orderList = DownLoadHelper.queryOrderList(mContext, true, false);
        List<Purchase> purchases = null;
        if(orderList != null && orderList.size() > 0) {
            for(OrderInfo order : orderList) {
                if (!isConsumableProduct(order.purchase.getSku())) {
                    continue;
                }

                if(order != null && order.purchase != null) {
                    if(purchases == null) {
                        purchases = new ArrayList<Purchase>();
                    }
                    purchases.add(order.purchase);
                }
            }
        }
        if(purchases != null && purchases.size() > 0) {
            try{
                marketIab.consumeAsync(purchases, new IabHelper.OnConsumeMultiFinishedListener() {
                    
                    @Override
                    public void onConsumeMultiFinished(List<Purchase> purchases,
                            List<IabResult> results) {
                        BLog.d(TAG, "onConsumeMultiFinished");
                        try{
                            setErrorCount(mContext, 0);
                            if(results != null && results.size() > 0) {
                                BLog.d(TAG, "onConsumeMultiFinished results size = "+ results.size());
                                for(int i = 0; i< results.size(); i++) {
                                    if(results.get(i).isSuccess()) {
                                        BLog.d(TAG, "onConsumeMultiFinished results sucess = "+ purchases.get(i).toString());
                                        String productId = Product.getProductIdFromPayload(purchases.get(i).getDeveloperPayload());
                                        if(!TextUtils.isEmpty(productId)) {
                                            DownLoadHelper.updateIabConsumeStatus(mContext, productId, true);
                                        }
                                    }else {
                                        BLog.d(TAG, "onConsumeMultiFinished results failed  "+ results.get(i).getMessage());
                                        if(mContext != null) {
                                            BLog.d(TAG, "Market Report: consumeAsync()---   " + purchases.get(i).toString());
                                        } 
                                    }
                                }
                                
                            }else {
                                BLog.d(TAG, "onConsumeMultiFinished results size = 0");
                            }
                        }catch(Exception e) {
                            if(mContext != null) {
                                BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + "  Exception consumeAsync()  " + e.getMessage());
                            } 
                            BLog.d(TAG, "onConsumeMultiFinished exception: "+ e.getMessage());
                        }
                        
                    }
                });
            }catch(Exception e) {
                BLog.d(TAG, "onConsumeMultiFinished exception: "+ e.getMessage());
                if(mContext != null) {
                    setErrorCount(mContext, nErrorCount + 1); 
                    rescheduleSync(false, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_CONSUME);
                    BLog.d(TAG, "Market Report: user_id = " +AccountSession.account_id  + "  Exception consumeAsync()  " + e.getMessage());
                }
            }
        }
    }

    private static final String SKU_AD_REMOVAL = "sku_ad_remove";
    private boolean isConsumableProduct(String sku) {
        if (SKU_AD_REMOVAL.equalsIgnoreCase(sku)) {
            return false;
        }
        return true;
    }

    @Override
    public void onLogin() {
//        BLog.v(TAG, "------------------onLogin()--------------------");
//        setErrorCount(mContext, 0);
//        rescheduleSync(true, InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE);
        
    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCancelLogin() {
        // TODO Auto-generated method stub
        
    }
    
    private SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences(SHARE_PREFERENCES_IAB_SETTING, Context.MODE_PRIVATE);
    }
    private void setErrorCount(Context context, int count) {
        nErrorCount = count;
        SharedPreferences sp = getSharePreferences(context);
        SharedPreferences.Editor Editor = sp.edit();
        Editor.putInt(SP_EXTRAS_ERROR_COUNT, count);
        Editor.commit();
    }
    public static void initErrorCount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARE_PREFERENCES_IAB_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor Editor = sp.edit();
        Editor.putInt(SP_EXTRAS_ERROR_COUNT, 0);
        Editor.commit();
    }
    
    private int getErrorCount(Context context) {
        SharedPreferences sp = getSharePreferences(context);
        return sp.getInt(SP_EXTRAS_ERROR_COUNT, 0);
    }

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub
        
    }
}
