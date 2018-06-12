package com.borqs.market.db;

import com.iab.engine.google.util.Purchase;

public class OrderInfo {

    public String product_id;
    public String user_id;
    public String pay_code;
    public String pay_type;
    public String item_type;
    public String jsonPurchaseInfo;
    public String signature;
    public int version_code;
    public String iab_order_id;
    public Purchase purchase;
    public boolean has_ordered;
    public boolean has_consumed;

}
