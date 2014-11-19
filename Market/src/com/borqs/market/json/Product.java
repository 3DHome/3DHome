package com.borqs.market.json;

import java.io.Serializable;
import java.util.ArrayList;

import com.borqs.market.account.AccountSession;
import com.borqs.market.utils.MarketConfiguration;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Product implements Serializable, Comparable<Product>, Parcelable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public String product_id;
    public String name;
    public String author;
    public String author_email;
    public String type;
    public String description;
    public long size;
    public String cover_url;
    public String logo_url;
    public String promotion_image;
    public long created_time;
    public long updated_time;
    public String app_package;
    public boolean purchased;
    public String price;
    public String version_name;
    public int version_code;
    public String current_version_name;
    public int current_version_code;
    public int downloads_count;
    public String localUrl;
    public boolean isApplyed;
//    public boolean isInstalled;
    public boolean isFree;
    public String mm_paycode;
    public int mm_paycode_count;
    public String supported_mod;
    public String installed_file_path;
    public float rating;
    public int rating_count;
    public int comment_count;
    public int like_count;

    public ArrayList<String> screenshots;

    protected String mGoogleSku;
    public String getSku() {
        return mGoogleSku;
    }
    public String getPayload() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("product_id").append("=").append(product_id).append(",")
                .append("name").append("=").append(name).append(",")
                .append("user_id").append("=").append(AccountSession.account_id).append(",")
                .append("version_code").append("=").append(version_code).append(",")
                .append("price").append("=").append(price).append(",")
                .append("mGoogleSku").append("=").append(mGoogleSku).append(",");
        return stringBuilder.toString();
    }
    public static String getProductIdFromPayload(String payload) {
        if(TextUtils.isEmpty(payload)) return null;
        return getValue(payload, "product_id");
    }
    
    public static String getUserIdFromPayload(String payload) {
        if(TextUtils.isEmpty(payload)) return null;
        return getValue(payload, "user_id");
    }
    
    public static int getVersionCodeFromPayload(String payload) {
        if(!TextUtils.isEmpty(payload)) {
            String value =  getValue(payload, "version_code");
            if(!TextUtils.isEmpty(value)) {
                return Integer.valueOf(value);
            }
        }
        return 1;
    }
    
    private static String getValue(String payload, String key) {
        String[] pStrs = payload.split(",");
        String value = null;
        for(String str : pStrs) {
            if(str.contains(key)) {
                value = str.substring(key.length()+1, str.length());
                break;
            }
        }
        return value;
    }

    @Override
    public int compareTo(Product another) {
        return 0;
    }

    public void despose() {
        product_id = null;
        name = null;
        author = null;
        author_email = null;
        name = null;
        type = null;
        description = null;
        cover_url = null;
        logo_url = null;
        promotion_image = null;
        app_package = null;
        price = null;
        version_name = null;
        current_version_name = null;
        localUrl = null;

        if (screenshots != null) {
            screenshots.clear();
            screenshots = null;
        }
        mGoogleSku = null;
        mm_paycode = null;
        supported_mod = null;
        installed_file_path = null;
    }

    public static class ProductType {
        public static final String THEME = "theme";
        public static final String OBJECT = "object";
        public static final String WALL_PAPER = "wallpaper";
        public static final String SCENE = "scene";
        
        public static String getProductType(int position) {
            if (position == 1) {
                return OBJECT;
            } else if (position == 2) {
                return SCENE;
            } else if (position == 3) {
                return WALL_PAPER;
            }else {
                return THEME;
            }
        }
    }
    
    
    public static class SupportedMod {
        public static final String LANDSCAPE = "landscape";
        public static final String PORTRAIT = "portrait";
    }
    public static boolean isSupportApply(String category) {
        if(Product.ProductType.OBJECT.equals(category)) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(product_id);
        dest.writeString(name);
        dest.writeString(author_email);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeLong(size);
        dest.writeString(cover_url);
        dest.writeString(logo_url);
        dest.writeString(promotion_image);
        dest.writeLong(created_time);
        dest.writeLong(updated_time);
        dest.writeString(app_package);
        dest.writeInt(purchased ? 1 : 0);
        dest.writeString(price);
        dest.writeString(version_name);
        dest.writeInt(version_code);
        dest.writeString(current_version_name);
        dest.writeInt(current_version_code);
        dest.writeInt(downloads_count);
        dest.writeString(localUrl);
        dest.writeInt(isApplyed ? 1 : 0);
//        dest.writeInt(isInstalled ? 1 : 0);
        dest.writeInt(isFree ? 1 : 0);
        dest.writeString(mGoogleSku);
        dest.writeString(mm_paycode);
        dest.writeInt(mm_paycode_count);
        dest.writeString(supported_mod);
        dest.writeString(installed_file_path);
        dest.writeFloat(rating);
        dest.writeInt(rating_count);
        dest.writeInt(comment_count);
        dest.writeInt(like_count);
    }

    private void readFromParcel(Parcel in) {
        product_id = in.readString();
        name = in.readString();
        author_email = in.readString();
        type = in.readString();
        description = in.readString();
        size = in.readLong();
        cover_url = in.readString();
        logo_url = in.readString();
        promotion_image = in.readString();
        created_time = in.readLong();
        updated_time = in.readLong();
        app_package = in.readString();
        purchased = in.readInt() == 1 ? true : false;
        price = in.readString();
        version_name = in.readString();
        version_code = in.readInt();
        current_version_name = in.readString();
        current_version_code = in.readInt();
        downloads_count = in.readInt();
        localUrl = in.readString();
        isApplyed = in.readInt() == 1 ? true : false;
//        isInstalled = in.readInt() == 1 ? true : false;
        isFree = in.readInt() == 1 ? true : false;
        mGoogleSku = in.readString();
        mm_paycode = in.readString();
        mm_paycode_count = in.readInt();
        supported_mod = in.readString();
        installed_file_path = in.readString();
        rating = in.readFloat();
        rating_count = in.readInt();
        comment_count = in.readInt();
        like_count = in.readInt();
    }

    public Product(Parcel in) {
        readFromParcel(in);
    }

    public Product() {
        super();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    
    public static boolean isBelongSystem(String installed_file_path) {
        boolean isBelongSystem = false;
        if(MarketConfiguration.getSystemThemeDir() != null) {
            String dir = MarketConfiguration.getSystemThemeDir().getPath();
            if(!TextUtils.isEmpty(installed_file_path) && installed_file_path.startsWith(dir)) {
                isBelongSystem = true;
            }else {
                isBelongSystem = false;
            }
        }
        
        return isBelongSystem;
    }

}
