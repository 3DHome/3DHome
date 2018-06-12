package com.borqs.se.shortcut;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.borqs.se.home3d.HomeUtils;

public class ItemInfo {
    public static final int ITEM_TYPE_APP = 0;
    public static final int ITEM_TYPE_WIDGET = 1;
    public static final int ITEM_TYPE_SHORTCUT = 2;

    private int mSpanX;
    private int mSpanY;

    private boolean mIsDefault;
    private ResolveInfo mResolveInfo;
    private ComponentName mComponentName;
    public boolean mIsSysApp = false;
    private String mLabel = "";
    private int mWidth;
    private int mHeight;

    public int mItemType;
    private Context mContext;

    public long mInstallDateTime = 0;
    public int mLaunchCount = 0;
    boolean mShowInPreface = true;

    //relative ObjectInfo name
    public String mObjectInfoName = null;

    public boolean isShowInPreface() {
        return mShowInPreface;
    }

    public void setShowInPreface(boolean mShowInPreface) {
        this.mShowInPreface = mShowInPreface;
    }

    public ItemInfo(Context context, ResolveInfo resolveInfo, ComponentName name) {
        mContext = context;
        mItemType = -1;
        mComponentName = name;
        mResolveInfo = resolveInfo;
        mSpanX = mSpanY = 1;
        mIsDefault = true;
    }

    public Context getContext() {
        return mContext;
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    public ResolveInfo getResolveInfo() {
        return mResolveInfo;
    }

    public void setResolveInfo(ResolveInfo resolveInfo) {
        mResolveInfo = resolveInfo;
    }

    public String getPreviewName() {
        return null;
    }

    public String getLabel() {
        if (TextUtils.isEmpty(mLabel) && mResolveInfo != null) {
            mLabel = HomeUtils.getAppLabel(mContext, mResolveInfo);
        }
        return mLabel;
    }

    private Drawable mIcon;

    public Drawable getIcon() {
        if (mIcon == null) {
            mIcon = HomeUtils.getAppIcon(mContext, mResolveInfo);
        }
        return mIcon;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                ItemInfo other = (ItemInfo) obj;
                if (mComponentName != null) {
                    return mComponentName.equals(other.mComponentName);
                }
                if (mObjectInfoName != null) {
                    return mObjectInfoName.equals(other.mObjectInfoName);
                }
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    public void setComponentName(ComponentName name) {
        mComponentName = name;
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public void setIsDefault(boolean isDefault) {
        mIsDefault = isDefault;
    }

    public void setWidth(int w) {
        mWidth = w;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int h) {
        mHeight = h;
    }

    public int getSpanX() {
        return mSpanX;
    }

    public void setSpanX(int spanX) {
        this.mSpanX = spanX;
    }

    public int getSpanY() {
        return mSpanY;
    }

    public void setSpanY(int spanY) {
        this.mSpanY = spanY;
    }

    public void setSpan(int spanX, int spanY) {
        this.mSpanX = spanX;
        this.mSpanY = spanY;
    }

    public boolean equals(ItemInfo itemInfo) {
        return itemInfo != null && mComponentName != null && mComponentName.equals(itemInfo.getComponentName()) || super.equals(itemInfo);
    }

}
