package com.borqs.market.json;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Recommend implements Serializable, Comparable<Recommend>,Parcelable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_SINGLE_PRODUCT = 1;
    public static final int TYPE_PARTITION = 2;
    public static final int TYPE_TAG = 3;
    public static final int TYPE_USER_SHARE = 4;
    public static final int TYPE_ORDER_BY = 5;
    
    public String description;
    public String name;
    public String promotion_image;
    public String target;
    public int type;


    @Override
    public int compareTo(Recommend another) {
        return 0;
    }

    public void despose() {
        name = null;
        target = null;
        description = null;
        promotion_image = null;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(name);
        dest.writeString(promotion_image);
        dest.writeString(target);
        dest.writeInt(type);
    }
    
    private void readFromParcel(Parcel in) {
        description = in.readString();
        name = in.readString();
        promotion_image = in.readString();
        target = in.readString();
        type = in.readInt();
    }

    public Recommend(Parcel in) {
        readFromParcel(in);
    }

    public Recommend() {
        super();
    }

    public static final Creator<Recommend> CREATOR = new Creator<Recommend>() {
        public Recommend createFromParcel(Parcel source) {
            return new Recommend(source);
        }

        public Recommend[] newArray(int size) {
            return new Recommend[size];
        }
    };

}
