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

import android.os.Parcel;
import android.os.Parcelable;
import android.content.ComponentName;


/**
 * Class for transfer the component array with extend.
 */
public final class AddObjectComponentNameExtend implements Parcelable {

    private ComponentName mComponentName;
    private String mObjectInfoName;
    private String mType;
    private int mIsShortcutOrUserUpdate;

    /**
     * Create a new componentname extend.
     */
    public AddObjectComponentNameExtend(ComponentName componentName, String objectInfoName, String type, boolean shortcutOrUserUpdate) {
        this.mComponentName = componentName;
        this.mObjectInfoName = objectInfoName;
        this.mType = type;
        this.mIsShortcutOrUserUpdate = (shortcutOrUserUpdate == true ? 1 : 0);
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }
    
    public String getObjectInfoName() {
        return mObjectInfoName;
    }
    
    public String getType() {
        return mType;
    }
    
    public boolean getIsShortcutOrUserUpdate() {
        return (mIsShortcutOrUserUpdate == 1 ? true : false);
    }
    
    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable((Parcelable)mComponentName, 0);
        out.writeString(mObjectInfoName);
        out.writeString(mType);
        out.writeInt(mIsShortcutOrUserUpdate);
    }

    public static void writeToParcel(AddObjectComponentNameExtend c, Parcel out) {
        if (c != null) {
            c.writeToParcel(out, 0);
        } else {
            out.writeString(null);
        }
    }
    
    public static AddObjectComponentNameExtend readFromParcel(Parcel in) {
        ComponentName c = (ComponentName)in.readParcelable(null);
        if (c == null) { return null; }
        String s1 = in.readString();
        String s2 = in.readString();
        int i = in.readInt();
        return new AddObjectComponentNameExtend(c, s1, s2, (i == 1 ? true : false));
    }
    
    public static final Parcelable.Creator<AddObjectComponentNameExtend> CREATOR
            = new Parcelable.Creator<AddObjectComponentNameExtend>() {
        public AddObjectComponentNameExtend createFromParcel(Parcel in) {
            return new AddObjectComponentNameExtend(in);
        }

        public AddObjectComponentNameExtend[] newArray(int size) {
            return new AddObjectComponentNameExtend[size];
        }
    };

    public AddObjectComponentNameExtend(Parcel in) {
        mComponentName = (ComponentName)in.readParcelable(null);
        mObjectInfoName = in.readString();
        mType = in.readString();
        mIsShortcutOrUserUpdate = in.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                AddObjectComponentNameExtend other = (AddObjectComponentNameExtend)obj;
                return mComponentName.equals(other.getComponentName());
            }
        } catch (Exception e) {
        }
        return false;
    }

} //class AddObjectComponentNameExtend

