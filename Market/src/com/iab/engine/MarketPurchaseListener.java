package com.iab.engine;

public interface MarketPurchaseListener {
    
//    public void onPreparedPurchase();
    
    public void onBillingFinished(boolean isSuccess,MarketBillingResult result);
}
