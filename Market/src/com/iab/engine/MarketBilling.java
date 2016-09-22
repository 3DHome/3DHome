package com.iab.engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.borqs.market.json.Product;

public interface MarketBilling {
    
    void purchase(Activity act, Product product, MarketPurchaseListener mpListener);
    
    void consumeAsync(Context context);
    
    boolean onActivityResult(int requestCode, int resultCode, Intent data);
    
    boolean isEngineReady();
    
    void depose();
}
