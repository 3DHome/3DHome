/*
 * Copyright © 2013 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, 
 * or disclosed, or disseminated outside of Borqs in whole or in part without Borqs 's permission.
 */

/* 
 * Description of the content in this file. 
 * 
 * History core-id Change purpose 
 * ---------- ------- ---------------------------------------------- 
 *
 */


package com.borqs.se.addobject;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.borqs.se.shortcut.ItemInfo;

public class AddObjectItemInfo extends ItemInfo {

    private Drawable mDwIcon = null;
    
    private boolean mIsOriginal = false;

    public AddObjectItemInfo(Context context, ResolveInfo resolveInfo, ComponentName name) {
        super(context, resolveInfo, name);
    }

    public AddObjectItemInfo(ItemInfo itemInfo) {
        super(itemInfo.getContext(), itemInfo.getResolveInfo(), itemInfo.getComponentName());
        mDwIcon = itemInfo.getIcon();
        setLabel(itemInfo.getLabel());
        mIsOriginal = false;
        setShowInPreface(itemInfo.isShowInPreface());
    }

    public Drawable getIcon() {
        return mDwIcon;
    }

    public Drawable getIconWithCreating() {
        if (mDwIcon == null) {
            mDwIcon = super.getIcon();
        }
        return mDwIcon;
    }

    public boolean isOriginal() {
        return mIsOriginal;
    }

    public void setIcon(Drawable dw) {
        if (dw == null) { return; }
        if (mDwIcon != null) {
            mDwIcon.setCallback(null);
        }
        mDwIcon = dw;
    }

    public void setOriginal(boolean bl) {
        mIsOriginal = bl;
    }

    @Override
    public AddObjectItemInfo clone() {
        AddObjectItemInfo newOne = new AddObjectItemInfo(getContext(), getResolveInfo(), getComponentName());
        newOne.mItemType = mItemType;
        newOne.mObjectInfoName = mObjectInfoName;
        newOne.mIsOriginal = mIsOriginal;
        newOne.setLabel(getLabel());
        if (mDwIcon != null) {
            newOne.mDwIcon = mDwIcon.getConstantState().newDrawable();
        }
        return newOne;
    }
}
