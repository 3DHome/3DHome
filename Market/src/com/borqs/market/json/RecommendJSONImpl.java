package com.borqs.market.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.borqs.market.utils.MarketConfiguration;

public class RecommendJSONImpl extends Recommend {

    private static final long serialVersionUID = -8612461811928745848L;
    
    public RecommendJSONImpl(JSONObject obj) {
        if (obj == null)
            return;
        description = obj.optString("description");
        name = obj.optString("name");
        promotion_image = obj.optString("promotion_image");
        target = obj.optString("target");
        type = obj.optInt("type");
    }

    public static ArrayList<Recommend> createRecommendList(JSONArray array) {
        ArrayList<Recommend> list = new ArrayList<Recommend>();
        try {
            if(MarketConfiguration.isIS_DEBUG_SUGGESTION()) {
                Recommend r = new Recommend();
                r.promotion_image = "http://oss.aliyuncs.com/wutong-data/borqsmarket/products/com.borqs.freehdhome.auto_1i4e86r2bsa78e2dn5ib-cover-658469.jpg";
                r.target = "com.borqs.freehdhome.auto_1i4e86r2bsa78e2dn5ib";
                r.type = 1;
                list.add(r);
                Recommend r2 = new Recommend();
                r2.promotion_image = "http://oss.aliyuncs.com/wutong-data/borqsmarket/products/com.borqs.freehdhome.auto_rntqonphvcnvf9a2h7s-logo-431344.jpg";
                r2.target = "pn_2889188353183890970";
                r2.type = 2;
                r2.name = "推荐分区测试";
                list.add(r2);
                
                Recommend r1 = new Recommend();
                r1.promotion_image = "http://oss.aliyuncs.com/wutong-data/borqsmarket/products/com.borqs.freehdhome.auto_bjtvsoq4inaco8jll6o-logo-984668.jpg";
                r1.target = "tag";
                r1.type = 3;
                list.add(r1);
                Recommend r4 = new Recommend();
                r4.promotion_image = "http://oss.aliyuncs.com/wutong-data/borqsmarket/products/com.borqs.freehdhome.auto_btsh1b8tjtfp3boo6pp-logo-526341.jpg";
                r4.type = 4;
                list.add(r4);
                
                Recommend r5 = new Recommend();
                r5.promotion_image = "http://htimg2.cache.netease.com/image?w=230&h=230&url=http%3A%2F%2Fimg2.ph.126.net%2FHkGzfxo4Wnvz5km5pDDAng%3D%3D%2F6598277030425280915.jpg%233&gif=1&quality=100";
                r5.type = 5;
                r5.target = "1";
                list.add(r5);
            }else {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj;
                    obj = array.getJSONObject(i);
                    list.add(new RecommendJSONImpl(obj));
                }
            }
                
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Recommend createRecommendInfo(String response) {
        Recommend c = new Recommend();
        JSONArray array = null;
        try {
            array = new JSONArray(response);
            if (array.length() >= 1) {

                JSONObject obj;
                obj = array.getJSONObject(0);
                c = new RecommendJSONImpl(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return c;
    }
}
