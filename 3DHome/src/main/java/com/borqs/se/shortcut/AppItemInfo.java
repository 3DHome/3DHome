package com.borqs.se.shortcut;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;

public class AppItemInfo extends ItemInfo {

    public AppItemInfo(Context context, ResolveInfo resolveInfo, ComponentName name) {
        super(context, resolveInfo, name);
        mItemType = ItemInfo.ITEM_TYPE_APP;
    }

    public String getPreviewName() {
        return "previewapp_" + getResolveInfo().activityInfo.packageName + "_" + getResolveInfo().activityInfo.name;
    }

}
