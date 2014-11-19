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

import android.content.Context;
import android.view.LayoutInflater;

import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.addobject.AddObjectDialog;
import com.borqs.se.addobject.AddObjectDialog.OnObjectsSelectedListener;


/**
 * Add apps
 */
public class AddAppDialog extends AddObjectDialog implements OnObjectsSelectedListener {

    /**/
    public static final String TAG = "AddAppDialog";

    /**/
    private OnAppSelectedListener mOnAppSelectedListener = null;


    public AddAppDialog(Context context, LayoutInflater inflater, ArrayList<AppItemInfo> itemInfos, 
                                                                  OnAppSelectedListener onAppSelectedListener, int max) {
        super(context, inflater, itemInfos, AddObjectDialog.APP, max, null,false);
        mOnAppSelectedListener = onAppSelectedListener;
        setInternalListener(this);
    }

    @Override
    public void onObjectSelected(ArrayList<AddObjectItemInfo> selectedList) {
        if (mOnAppSelectedListener != null) {
            mOnAppSelectedListener.onAppSelected(selectedList, getMax());
        }
    }

    @Override
    public void onObjectChecked(AddObjectItemInfo selected, boolean checked, int currentNum) {
    }

    @Override
    public void onObjectCheckedDirectly(AddObjectItemInfo selected) {
    }


    /**
     * Interface for select apps
     */ 
    public interface OnAppSelectedListener {
        public void onAppSelected(ArrayList<AddObjectItemInfo> selectedList, int availableNum);
    }

} // class AddAppDialog

