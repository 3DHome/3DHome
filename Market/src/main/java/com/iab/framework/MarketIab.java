package com.iab.framework;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.OrderInfo;
import com.borqs.market.json.Product;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketConfiguration;
import com.iab.engine.MarketBilling;
import com.iab.engine.MarketBillingResult;
import com.iab.engine.MarketPurchaseListener;
import com.iab.engine.google.util.IabHelper;
import com.iab.engine.google.util.IabHelper.QueryInventoryFinishedListener;
import com.iab.engine.google.util.IabResult;
import com.iab.engine.google.util.Purchase;

public class MarketIab implements MarketBilling {
    private static final String TAG = MarketIab.class.getSimpleName();
    
    public static final int RC_REQUEST = 10001;
    
    private Context context;
    IabHelper mHelper = null;
    boolean mEngineReady = false;
    boolean hasInited = false;
    public MarketIab(Context context, QueryInventoryFinishedListener listener) {
        hasInited = false;
        this.context = context.getApplicationContext();
        createIabHelper(MarketConfiguration.IABKEY, listener);
    }
    
// // Listener that's called when we finish querying the items and subscriptions we own
//    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
//            Log.d(TAG, "Query inventory finished.");
//            if (result.isFailure()) {
////                complain("Failed to query inventory: " + result);
//                return;
//            }
//
//            Log.d(TAG, "Query inventory was successful.");
//            
//            if(inventory != null) {
//                //TODO:如果有未consume的产品，写入DB，上传订单。
//            }
//
//            /*
//             * Check for items we own. Notice that for each purchase, we check
//             * the developer payload to see if it's correct! See
//             * verifyDeveloperPayload().
//             */
//
////            // Do we have the premium upgrade?
////            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
////            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
////            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
////
////            // Do we have the infinite gas plan?
////            Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
////            mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
////                    verifyDeveloperPayload(infiniteGasPurchase));
////            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
////                        + " infinite gas subscription.");
////            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;
////
////            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
////            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
////            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
////                Log.d(TAG, "We have gas. Consuming it.");
////                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
////                return;
////            }
////
////            updateUi();
////            setWaitScreen(false);
//            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
//        }
//    };
    public void createIabHelper(String base64EncodedPublicKey, final QueryInventoryFinishedListener listener) {
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(context, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                mEngineReady = result.isSuccess();
                hasInited = true;
                if (!mEngineReady) {
                    // Oh noes, there was a problem.
                    BLog.e(TAG, "Problem setting up in-app billing: " + result);
                    if(AccountSession.isLogin()) {
                        BLog.d(TAG, "Market Report:user_id = " + AccountSession.account_id
                                + "------ and email = " + AccountSession.account_email + "Problem setting up in-app billing: " + result);
                    }
                    return;
                }
//                
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    BLog.d(TAG, "mHelper == null.");
                    return;
                } 
//              
//                if(listener == null) {
//                    BLog.d(TAG, "mHelper == null.");
//                    return;
//                } 
//                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
//                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    BLog.d(TAG, "Querying inventory.");
                    mHelper.queryInventoryAsync(listener);
                } catch (Exception e) {
                    Log.e(TAG, "queryInventoryAsync, failed after setup succeed? should not be here!");
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void queryPurchases(final IabHelper.QueryInventoryFinishedListener listener) {
        if(mHelper != null && mEngineReady) {
            try {
                mHelper.queryInventoryAsync(listener);
            } catch (Exception e) {
                Log.e(TAG, "queryInventoryAsync, failed after setup succeed? should not be here!");
                e.printStackTrace();
            }
        }else {
            BLog.v(TAG, "(mHelper != null && mEngineReady) is false" ); 
        }
    }
    
//    private void consumeAsync(Purchase purchase, IabHelper.OnConsumeFinishedListener listener) {
//        if (mEngineReady) {
//            mHelper.consumeAsync(purchase, listener);
//        }
//    }
    
    public void consumeAsync(List<Purchase> purchases, IabHelper.OnConsumeMultiFinishedListener listener) {
        if (mEngineReady) {
            mHelper.consumeAsync(purchases, listener);
        }
    }

    private void launchPurchaseFlow(Activity act, String sku,
                IabHelper.OnIabPurchaseFinishedListener listener, String payload,  MarketPurchaseListener mpListener) {
        if(act == null) return;
        if(act.isFinishing()) return;
        if (!mEngineReady) {
            BLog.d(TAG, "Market Report:user_id = " + AccountSession.account_id
                    + "------ and email = " + AccountSession.account_email + "Problem setting up in-app billing: Bill Unavailable for " + payload );
            BLog.e(TAG, "Problem setting up in-app billing: Bill Unavailable for " + payload);
            
            if(mpListener != null) {
                MarketBillingResult result = new MarketBillingResult(MarketBillingResult.TYPE_IAB);
                result.response = act.getString(R.string.iab_unavailable);
                mpListener.onBillingFinished(false, result);
            }
            
        } else {
            mHelper.launchPurchaseFlow(act, sku, RC_REQUEST,
                    listener, payload);
        }
    }
    
    @Override
    public void purchase(Activity act, Product product,final MarketPurchaseListener mpListener) {
        try{
            final String sku = product.getSku();
            final String payload = product.getPayload();
            launchPurchaseFlow(act, sku,
                    new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                    try{
                        Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
                        if (result.isFailure()) {
                            BLog.v(TAG, "Error purchasing: " + result);
                            MarketBillingResult mResult = new MarketBillingResult(MarketBillingResult.TYPE_IAB);
                            mResult.response = result.getMessage();
                            mpListener.onBillingFinished(false, mResult);
                            return;
                        }
                        if(context != null) {
                            DownLoadHelper.insertIabOder(context, purchase);
                        }
                        
                        
                        MarketBillingResult billingResult = null;
                        if(result.isSuccess()) {
                            billingResult = new MarketBillingResult(MarketBillingResult.TYPE_IAB);
                            if (purchase != null) {
                                billingResult.orderId = purchase.getOrderId();
                                billingResult.payCode = purchase.getSku();
                                billingResult.originalJson = purchase.getOriginalJson();
                            }
                        }
                        mpListener.onBillingFinished(result.isSuccess(), billingResult);
                        Log.d(TAG, "Purchase successful.");
                        if(context != null) {
                            BLog.d(TAG,
                                    "Market Report: purchase()  result.isSuccess() = " + result.isSuccess()
                                    + "    purchase.tostring  = " + purchase.toString());
                        }
                    }catch(Exception e) {
                        if(context != null) {
                            BLog.d(TAG, "Market Report: purchase()  " + e.getMessage());
                        } 
                    }
                }
            }
            , payload, mpListener);
        }catch(Exception e) {
            if(context != null) {
                BLog.d(TAG, "Market Report Exception purchase()  " + e.getMessage());
            }
        } 
        
    }
    
    public void consumeAsync(final Context ctx) {
        if(TextUtils.isEmpty(AccountSession.account_id)) return;
        List<OrderInfo> orderList = DownLoadHelper.queryOrderList(ctx, true, false);
        List<Purchase> purchases = null;
        if(orderList != null && orderList.size() > 0) {
            for(OrderInfo order : orderList) {
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
                consumeAsync(purchases, new IabHelper.OnConsumeMultiFinishedListener() {
                    
                    @Override
                    public void onConsumeMultiFinished(List<Purchase> purchases,
                            List<IabResult> results) {
                        try{
                            if(results != null && results.size() > 0) {
                                for(int i = 0; i< results.size(); i++) {
                                    if(results.get(i).isSuccess()) {
                                        String productId = Product.getProductIdFromPayload(purchases.get(i).getDeveloperPayload());
                                        if(!TextUtils.isEmpty(productId)) {
                                            DownLoadHelper.updateIabConsumeStatus(ctx, productId, true);
                                        }
                                    }
                                }
                            }
                        }catch(Exception e) {
                            if(ctx != null) {
                                BLog.d(TAG, "Market Report: consumeAsync()  " + e.getMessage());
                            } 
                        }
                        
                    }
                });
            }catch(Exception e) {
                BLog.d(TAG, "consumeAsync exception :" + e.getMessage());
                if(ctx != null) {
                    BLog.d(TAG, "Market Report: consumeAsync()  " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RC_REQUEST == requestCode) {
            // TODO: handle the result.
            // Pass on the activity result to the helper for handling
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to
                // in-app
                // billing...
                return false;
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isEngineReady() {
        return mEngineReady;
    }


    @Override
    public void depose() {
        if (mEngineReady && mHelper != null) mHelper.dispose();
        mEngineReady = false;
        mHelper = null;
    }


}
