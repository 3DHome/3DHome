package com.iab.engine;

public interface MarketPurchaseListener {
    
//    public void onPreparedPurchase();
    
    void onBillingFinished(boolean isSuccess, MarketBillingResult result);
}
