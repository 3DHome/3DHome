package com.borqs.market.di;

import com.iab.engine.MarketBilling;

import dagger.Component;

/**
 * Created by yangfeng on 16-9-23.
 */
@Component(modules = { MarketBillingModule.class })
public interface  MarketBillingComponent {
    MarketBilling make();
}
