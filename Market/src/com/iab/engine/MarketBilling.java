package com.iab.engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.borqs.market.json.Product;

public interface MarketBilling {
    
    public void purchase(Activity act, Product product,MarketPurchaseListener mpListener);
    
    public void consumeAsync(Context context);
    
    public boolean onActivityResult(int requestCode, int resultCode, Intent data);
    
    public boolean isEngineReady();
    
    public void depose();
}
