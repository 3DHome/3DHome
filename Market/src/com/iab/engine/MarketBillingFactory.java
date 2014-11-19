package com.iab.engine;

import android.content.Context;

import com.iab.framework.MarketIab;

public class MarketBillingFactory {

    public static MarketBilling createMarketBilling(Context context) {
        return new MarketIab(context, null);
    }

}
