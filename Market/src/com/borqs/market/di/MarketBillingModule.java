package com.borqs.market.di;

import android.content.Context;

import com.iab.engine.MarketBilling;
import com.iab.framework.MarketIab;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yangfeng on 16-9-23.
 */

@Module
public class MarketBillingModule {
    private Context context;
    public MarketBillingModule(Context context) {
        this.context = context;
    }

    @Provides MarketBilling provideMarketBilling() {
        return new MarketIab(context, null);
    }
}
