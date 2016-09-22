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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Animation.AnimationListener;

import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.AppItemInfo;

import com.borqs.se.R;
import com.borqs.se.addobject.AddObjectDialog;
import com.borqs.se.addobject.AddObjectDialog.OnObjectsSelectedListener;
import com.borqs.se.home3d.HomeUtils;



/**
 * For adding folder
 */
public class AddFolderDialog extends AddObjectDialog implements OnObjectsSelectedListener {

    /**/
    public static final String TAG = "AddFolderDialog";

    private static final int MAX_ICON_NUM = 4;

    /**/
    private OnFolderCreatedListener mOnFolderCreatedListener;
    private OnFolderEditedListener mOnFolderEditedListener;
    private ArrayList<AddObjectItemInfo> mHolders;
    private Drawable mDefDw;


    public AddFolderDialog(Context context, LayoutInflater inflater, ArrayList<AppItemInfo> itemInfos, 
                                            OnFolderCreatedListener onFolderCreatedListener, int max, int childNum) {
        super(context, inflater, itemInfos, AddObjectDialog.FOLDER, childNum, null,false);
        mOnFolderCreatedListener = onFolderCreatedListener;
        mOnFolderEditedListener = null;
        setInternalListener(this);
    }

    public AddFolderDialog(Context context, LayoutInflater inflater, ArrayList<AppItemInfo> itemInfos, 
            OnFolderEditedListener onFolderEditedListener, int max, int childNum, Bundle bundle) {
        super(context, inflater, itemInfos, AddObjectDialog.FOLDER, childNum, bundle,false);
        mOnFolderCreatedListener = null;
        mOnFolderEditedListener = onFolderEditedListener;
        setInternalListener(this);
        if (bundle != null) {
            setDefault();
        }
    }    
    
    @Override
    public void onObjectSelected(ArrayList<AddObjectItemInfo> selectedList) {
        try {
            if (mOnFolderCreatedListener != null) {
                mOnFolderCreatedListener.onFolderCreated(getContentTitle(), selectedList);
            }
            if (mOnFolderEditedListener != null) {
                mOnFolderEditedListener.onFolderEdited(getContentTitle(), selectedList);
            }
        } catch (Exception e) {}
    }

    @Override
    public void onObjectChecked(AddObjectItemInfo selected, boolean checked, int currentNum) {
        hideIM();
        if (checked == true) {
            if (currentNum > getMax()) {
                return;
            } else {
                putInFolder(selected);
            }
        } else {
            takeOutOfFolder(selected);
        }
    }

    @Override
    public void onObjectCheckedDirectly(AddObjectItemInfo selected) {
        putInFolder(selected);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDefDw != null) {
            mDefDw.setCallback(null);
        }
        if (mHolders != null) {
            for (AddObjectItemInfo ii : mHolders) {
                if (ii.getIcon() != null) {
                    ii.getIcon().setCallback(null);
                }
            }
            mHolders.clear();
            mHolders = null;
        }
    }

    private void putInFolder(AddObjectItemInfo selected) {
        if (mHolders == null) { mHolders = new ArrayList<AddObjectItemInfo>(); }
        AddObjectItemInfo newii = selected.clone();
        if (newii.getIcon() == null) {
            newii.setIcon(HomeUtils.getAppIcon(getContext(), newii.getResolveInfo()));
        }
        mHolders.add(0, newii);
        updateFolder();
    }

    private void takeOutOfFolder(AddObjectItemInfo selected) {
        if (mHolders == null) { return; }
        int idx = mHolders.indexOf(selected);
        if (idx >= 0) {
            mHolders.remove(idx);
            if (idx < MAX_ICON_NUM) {
                removeIt(idx);
            }
        }
    }

    private void updateFolder() {
        // TODO: sync
        ArrayList<ImageView> ivs = getFolderImageViews();
        if ((ivs == null) || (ivs.size() != MAX_ICON_NUM)) { return; }
        for (int i = 0; i < MAX_ICON_NUM; i++) {
            ivs.get(i).clearAnimation();
            if (i >= mHolders.size()) {
                if (mDefDw == null) { mDefDw = getContext().getResources().getDrawable(R.drawable.addobject_dialog_folder_default_iv); }
                // mDefDW?
                ivs.get(i).setImageDrawable(null);
            } else {
                ivs.get(i).setImageDrawable(mHolders.get(i).getIcon());
            }
        }
    }

    private void removeIt(int idx) {
        ArrayList<ImageView> ivs = getFolderImageViews();
        if ((ivs == null) || (ivs.size() != MAX_ICON_NUM)) { return; }
        ImageView iv = ivs.get(idx);
        if (iv.getDrawable() == null) { return; }

        ScaleAnimation animation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(animation);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                updateFolder();
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
            @Override
            public void onAnimationStart(Animation animation) { }
        });
        iv.startAnimation(animationSet);
    }

    /**/
    public interface OnFolderCreatedListener {
        void onFolderCreated(String folderName, ArrayList<AddObjectItemInfo> selectedList);
    }
    
    /**/
    public interface OnFolderEditedListener {
        void onFolderEdited(String folderName, ArrayList<AddObjectItemInfo> selectedList);
    }
} // class AddFolderDialog

