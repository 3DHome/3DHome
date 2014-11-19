package com.borqs.market.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;

public class ProductJSONImpl extends Product {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8612461811920245848L;
    public static final String TAG_PRODUCT_ID = "id";
    public static final String TAG_PRODUCT_FILE_ID = "file_id";

    public static final String TAG_NAME = "name";
    public static final String TAG_TYPE = "category_id";
    public static final String TAG_COVER = "cover_image";
    public static final String TAG_LOGO = "logo_image";
    public static final String TAG_IS_FREE = "paid";
    public static final String TAG_PRICE = "price";
    public static final String TAG_PRICE_DISPLAY = "display";
    public static final String TAG_PURCHASED = "purchased";
    public static final String TAG_AUTHOR_NAME = "author_name";
    public static final String TAG_AUTHOR_EMAIL = "author_email";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_CREATE_TIME = "created_at";
    public static final String TAG_UPDATE_TIME = "updated_at";
    public static final String TAG_PROMOTION_IMAGE = "promotion_image";
    public static final String TAG_SIZE = "file_size";
    public static final String TAG_PACKAGE = "app_package";
    public static final String TAG_GOOGLE_SKU = "google_iab_sku";
    public static final String TAG_MM_PAY_CODE = "cmcc_mm_paycode";
    public static final String TAG_MM_AMOUNT = "cmcc_mm_amount";
    public static final String TAG_VERSION_NAME = "version_name";
    public static final String TAG_VERSION_CODE = "version";
    public static final String TAG_DOWNLOAD_COUNT = "purchase_count";
    public static final String TAG_SCREENSHOT1 = "screenshot1_image";
    public static final String TAG_SCREENSHOT2 = "screenshot2_image";
    public static final String TAG_SCREENSHOT3 = "screenshot3_image";
    public static final String TAG_SCREENSHOT4 = "screenshot4_image";
    public static final String TAG_SCREENSHOT5 = "screenshot5_image";
    public static final String TAG_SUPPORTED_MOD = "supported_mod";
    public static final String TAG_RATING = "rating";
    public static final String TAG_RATING_COUNT = "rating_count";
    public static final String TAG_COMMENT_COUNT = "comment_count";
    public static final String TAG_LIKE_COUNT = "like_count";

    public ProductJSONImpl(JSONObject obj) {
        if (obj == null)
            return;
        // ugly code from remote design begin
        product_id = obj.optString(TAG_PRODUCT_FILE_ID);
        if (TextUtils.isEmpty(product_id)) {
            product_id = obj.optString(TAG_PRODUCT_ID);
        }
//        product_id = obj.optString(TAG_PRODUCT_ID);
        // ugly code from remote design end
        name = obj.optString(TAG_NAME);
        type = obj.optString(TAG_TYPE);
        cover_url = obj.optString(TAG_COVER);
        logo_url = obj.optString(TAG_LOGO);
        isFree = obj.optInt(TAG_IS_FREE,1)==1?true:false;   //1为免费，2为收费
        if(!isFree) {
            if(obj.has(TAG_PRICE)) {
                JSONObject priceObj =  obj.optJSONObject(TAG_PRICE);
                price = priceObj.optString(TAG_PRICE_DISPLAY);
                priceObj = null;
            }
        }
        purchased = obj.optBoolean(TAG_PURCHASED);
        author = obj.optString(TAG_AUTHOR_NAME);
        author_email = obj.optString(TAG_AUTHOR_EMAIL);
        description = obj.optString(TAG_DESCRIPTION);
        created_time = obj.optLong(TAG_CREATE_TIME);
        updated_time = obj.optLong(TAG_UPDATE_TIME);
        promotion_image = obj.optString(TAG_PROMOTION_IMAGE);
        size = obj.optLong(TAG_SIZE);
        app_package = obj.optString(TAG_PACKAGE);
        mGoogleSku = obj.optString(TAG_GOOGLE_SKU);
        mm_paycode = obj.optString(TAG_MM_PAY_CODE);
        mm_paycode_count = obj.optInt(TAG_MM_AMOUNT,1);
        version_name = obj.optString(TAG_VERSION_NAME);
        version_code = obj.optInt(TAG_VERSION_CODE);
        downloads_count = obj.optInt(TAG_DOWNLOAD_COUNT);
        if(obj.has(TAG_RATING)) {
            rating = (float)obj.optDouble(TAG_RATING)*5;
        }else {
            rating = 0;
        }
        rating_count = obj.optInt(TAG_RATING_COUNT);
        comment_count = obj.optInt(TAG_COMMENT_COUNT);
        like_count = obj.optInt(TAG_LIKE_COUNT);

        screenshots = new ArrayList<String>();
        String screenshot1 =  obj.optString(TAG_SCREENSHOT1);
        String screenshot2 =  obj.optString(TAG_SCREENSHOT2);
        String screenshot3 =  obj.optString(TAG_SCREENSHOT3);
        String screenshot4 =  obj.optString(TAG_SCREENSHOT4);
        String screenshot5 =  obj.optString(TAG_SCREENSHOT5);
        if(!TextUtils.isEmpty(screenshot1)) {
            screenshots.add(screenshot1);
        }
        if(!TextUtils.isEmpty(screenshot2)) {
            screenshots.add(screenshot2);
        }
        if(!TextUtils.isEmpty(screenshot3)) {
            screenshots.add(screenshot3);
        }
        if(!TextUtils.isEmpty(screenshot4)) {
            screenshots.add(screenshot4);
        }
        if(!TextUtils.isEmpty(screenshot5)) {
            screenshots.add(screenshot5);
        }
        JSONArray modArr = obj.optJSONArray(TAG_SUPPORTED_MOD);
        if(modArr != null) {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < modArr.length(); i++) {
                    String modObj = (String)modArr.get(i);
                    if(!TextUtils.isEmpty(modObj)) {
                        if(sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(modObj);
                    }
                }
                supported_mod = sb.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    public static Product createProduct(String jsonStr,String localJsonstr) {
        Product product = null;
        if(!TextUtils.isEmpty(jsonStr)) {
            try {
                product = new ProductJSONImpl(new JSONObject(jsonStr));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!TextUtils.isEmpty(localJsonstr)) {
            try {
                JSONObject localObj = new JSONObject(localJsonstr);
                if(product != null) {
                    if(product.screenshots != null) {
                        product.screenshots.clear();
                    }else {
                        product.screenshots = new ArrayList<String>();
                    }
                    String screenshot1 =  localObj.optString(TAG_SCREENSHOT1);
                    String screenshot2 =  localObj.optString(TAG_SCREENSHOT2);
                    String screenshot3 =  localObj.optString(TAG_SCREENSHOT3);
                    String screenshot4 =  localObj.optString(TAG_SCREENSHOT4);
                    String screenshot5 =  localObj.optString(TAG_SCREENSHOT5);
                    if(!TextUtils.isEmpty(screenshot1)) {
                        product.screenshots.add(screenshot1);
                    }
                    if(!TextUtils.isEmpty(screenshot2)) {
                        product.screenshots.add(screenshot2);
                    }
                    if(!TextUtils.isEmpty(screenshot3)) {
                        product.screenshots.add(screenshot3);
                    }
                    if(!TextUtils.isEmpty(screenshot4)) {
                        product.screenshots.add(screenshot4);
                    }
                    if(!TextUtils.isEmpty(screenshot5)) {
                        product.screenshots.add(screenshot5);
                    }
                    product.promotion_image = localObj.optString(TAG_PROMOTION_IMAGE);
                    product.logo_url = localObj.optString(TAG_LOGO);
                    product.cover_url = localObj.optString(TAG_COVER);
                }else {
                    product = new ProductJSONImpl(new JSONObject(localJsonstr));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return product;
    }

    public static ArrayList<Product> createProductList(JSONArray array) {
        ArrayList<Product> list = new ArrayList<Product>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                obj = array.getJSONObject(i);
                list.add(new ProductJSONImpl(obj));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Product createProductInfo(String response) {
        Product c = new Product();
        JSONArray array = null;
        try {
            array = new JSONArray(response);
            if (array.length() >= 1) {

                JSONObject obj;
                obj = array.getJSONObject(0);
                c = new ProductJSONImpl(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return c;
    }
    
    public static String createJsonObjectString(Product product) {
        String str = null;
        if(product != null) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(TAG_PRODUCT_ID, product.product_id);
                obj.put(TAG_NAME, product.name);
                obj.put(TAG_TYPE, product.type);
                obj.put(TAG_PROMOTION_IMAGE, product.promotion_image);
                obj.put(TAG_COVER, product.cover_url);
                obj.put(TAG_LOGO, product.logo_url);
                obj.put(TAG_AUTHOR_NAME, product.author);
                obj.put(TAG_AUTHOR_EMAIL, product.author_email);
                obj.put(TAG_DESCRIPTION, product.description);
                obj.put(TAG_DESCRIPTION, product.description);
                obj.put(TAG_PACKAGE, product.app_package);
                obj.put(TAG_VERSION_CODE, product.version_code);
                obj.put(TAG_VERSION_NAME, product.version_name);
                obj.put(TAG_SUPPORTED_MOD, product.supported_mod);
                
                if(product.screenshots != null && product.screenshots.size() > 0) {
                    for(int i =0; i< product.screenshots.size(); i++) {
                        if(i == 0) {
                            obj.put(TAG_SCREENSHOT1, product.screenshots.get(i));
                        }else if(i == 1) {
                            obj.put(TAG_SCREENSHOT2, product.screenshots.get(i));
                        }else if(i == 2) {
                            obj.put(TAG_SCREENSHOT3, product.screenshots.get(i));
                        }else if(i == 3) {
                            obj.put(TAG_SCREENSHOT4, product.screenshots.get(i));
                        }else if(i == 4) {
                            obj.put(TAG_SCREENSHOT5, product.screenshots.get(i));
                        }
                    }
                }
                str = obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return str;
    }
}
