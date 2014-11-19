package com.borqs.market.db;

import android.text.TextUtils;

import com.borqs.market.json.Product;

public class PlugInInfo {
    public String name;
    public String product_id;
    public String version_name;
    public int version_code;
    public String type;
    public boolean is_apply;
    public String json_str;
    public String local_json_str;
    public String supported_mod;
    
    public static PlugInInfo fromProduct(Product product) {
        PlugInInfo info = new PlugInInfo();
        info.name = product.name;
        info.product_id = product.product_id;
        info.version_code = product.version_code;
        info.version_name = product.version_name;
        if (TextUtils.isEmpty(info.version_name)) {
            info.version_name = String.valueOf(info.version_code);
        }
        info.type = product.type;
        info.is_apply = false;
        info.supported_mod = product.supported_mod;
        return info;
    }
}
