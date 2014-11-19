package com.borqs.market.wallpaper;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by b608 on 13-8-20.
 */
public class RawPaperItem implements Parcelable {
    public static final String TYPE_WALL = "TYPE_WALL";
    public static final String TYPE_GROUND = "TYPE_GROUND";

    public RawPaperItem(String imageName, String imagePath, String type, int wallPaperCropSizeX, int wallPaperCropSizeY) {
        mKeyName = imageName;
        mImagePath = imagePath;
        mType = type;
        mWallPaperCropSizeX = wallPaperCropSizeX;
        mWallPaperCropSizeY = wallPaperCropSizeY;
    }

    public RawPaperItem(RawPaperItem item) {
        mKeyName = item.mKeyName;
        mType = item.mType;
        mImagePath = item.mImagePath;
        mWallPaperCropSizeX = item.mWallPaperCropSizeX;
        mWallPaperCropSizeY = item.mWallPaperCropSizeX;
    }

    public String mKeyName;
    public String mType;
    public int mWallPaperCropSizeY;
    public int mWallPaperCropSizeX;
    public String mImagePath;

    public static final Parcelable.Creator<RawPaperItem> CREATOR = new Creator<RawPaperItem>() {

        public RawPaperItem createFromParcel(Parcel source) {
            RawPaperItem item = new RawPaperItem(source.readString(), source.readString(), source.readString(),
                    source.readInt(), source.readInt());
            return item;
        }

        public RawPaperItem[] newArray(int size) {
            return new RawPaperItem[size];
        }

    };

    //
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mKeyName);
        dest.writeString(mImagePath);
        dest.writeString(mType);
        dest.writeInt(mWallPaperCropSizeX);
        dest.writeInt(mWallPaperCropSizeY);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}