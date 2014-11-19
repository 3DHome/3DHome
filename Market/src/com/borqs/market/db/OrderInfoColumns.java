package com.borqs.market.db;

import android.provider.BaseColumns;

public class OrderInfoColumns implements BaseColumns {

    public static final String PRODUCT_ID = "product_id";
    public static final String USER_ID = "user_id";
    public static final String PAY_CODE = "pay_code";
    public static final String PAY_TYPE = "pay_type";
    public static final String ITEM_TYPE = "item_type";
    public static final String JSON_PURCHASE_INFO = "jsonPurchaseInfo";
    public static final String SIGNATURE = "signature";
    public static final String VERSION_CODE = "version_code";
    public static final String IAB_ORDER_ID = "iab_order_id";
    
    public static final String HAS_ORDERED = "has_ordered";
    public static final String HAS_CONSUMED = "has_consumed";

    public static final String[] PROJECTION = { PRODUCT_ID, USER_ID, PAY_CODE, PAY_TYPE,
        ITEM_TYPE, JSON_PURCHASE_INFO, SIGNATURE, VERSION_CODE, IAB_ORDER_ID, HAS_ORDERED,HAS_CONSUMED};

}
