package com.borqs.se.shortcut;

import java.util.ArrayList;
import java.util.List;

import com.borqs.se.home3d.HomeUtils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;

/**
 * Stores the list of all applications, shortcuts, widgets for the all apps
 * view.
 */
public class AllAppsList {
    public static final int DEFAULT_APPLICATIONS_NUMBER = 100;

    /** The list of all apps, shortcuts and widgets */
    public List<ItemInfo> data = new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);

    /** The list of apps that have been added since the last notify() call. */
    public List<ItemInfo> added = new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);

    /** The list of apps that have been removed since the last notify() call. */
    public List<ItemInfo> removed = new ArrayList<ItemInfo>();

    /** The list of apps that have been modified since the last notify() call. */
    public List<ItemInfo> modified = new ArrayList<ItemInfo>();

    public List<ItemInfo> unavailable = new ArrayList<ItemInfo>();
    public List<ItemInfo> available = new ArrayList<ItemInfo>();

    private Context mContext;
    //不建议使用缓存PackageManager，PackageManager有可能会重新启动
//    private PackageManager mPackageManager;

    /**
     * Boring constructor.
     */
    public AllAppsList(Context context) {
        mContext = context;
//        mPackageManager = mContext.getPackageManager();
    }

    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
        unavailable.clear();
        available.clear();
    }

    public int size() {
        return data.size();
    }

    public ItemInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the data for the supplied apk called packageName.
     */
    public void addPackage(String packageName) {
        if (mContext.getPackageName().equals(packageName)) {
            return;
        }
        List<ItemInfo> apps = getCurForPackage(packageName);
        for (ItemInfo aInfo : apps) {
            if (!data.contains(aInfo)) {
                data.add(aInfo);
                added.add(aInfo);
            }
        }
    }

    /**
     * Remove the data for the given apk identified by packageName.
     */
    public void removePackage(String packageName) {
        List<ItemInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ItemInfo info = data.get(i);
            ComponentName component = info.getComponentName();
            if (packageName.equals(component.getPackageName())) {
                removed.add(info);
                if (HomeUtils.DEBUG)
                    Log.d("AllAppsList", "remove add : " + info.getComponentName());
                data.remove(i);
            }
        }
    }

    public void setPackageUnavailable(String packageName) {
        List<ItemInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ItemInfo info = data.get(i);
            ComponentName component = info.getComponentName();
            if (packageName.equals(component.getPackageName())) {
                unavailable.add(info);
                data.remove(i);
            }
        }
    }

    public void setPackageAvailable(String packageName) {
        if (mContext.getPackageName().equals(packageName)) {
            return;
        }
        List<ItemInfo> apps = getCurForPackage(packageName);
        for (ItemInfo aInfo : apps) {
            if (!data.contains(aInfo)) {
                data.add(aInfo);
                available.add(aInfo);
            }
        }
    }

    /**
     * Add and remove data for this package which has been updated.
     */
    public void updatePackage(String packageName) {
        if (mContext.getPackageName().equals(packageName)) {
            return;
        }
        List<ItemInfo> curList = getCurForPackage(packageName);// from system
        List<ItemInfo> preList = findPreForPackage(packageName);// from local
        int size = preList.size();
        for (int i = 0; i < size; i++) {
            ItemInfo preItem = preList.get(i);
            if (curList.contains(preItem)) {
                int index = curList.indexOf(preItem);
                if (index >= 0 && index < curList.size()) {
                    modified.add(curList.get(index));
                }
                curList.remove(preItem);
            } else {
                removed.add(preItem);
                data.remove(preItem);
            }
        }
        for (ItemInfo info : curList) {
            added.add(info);
            data.add(info);
        }
    }

    /**
     * Load all data (applications, shortcuts, widgets), and save them to the
     * lists.
     * 
     * @param context
     */
    public void loadAll() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : apps) {
            ComponentName name = new ComponentName(info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name);
            if (mContext.getPackageName().equals(name.getPackageName())) {
                continue;
            }
            AppItemInfo appInfo = new AppItemInfo(mContext, info, name);
            appInfo.mItemType = ItemInfo.ITEM_TYPE_APP;
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            boolean isUpdatedSysApp = (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            boolean isSysApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (isSysApp || isUpdatedSysApp) {
                appInfo.mIsSysApp = true;
            }
            data.add(appInfo);
            try {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(info.activityInfo.packageName, 0);
                appInfo.mInstallDateTime = packageInfo.firstInstallTime;              
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
//        List<AppWidgetProviderInfo> widgets = AppWidgetManager.getInstance(mContext).getInstalledProviders();
//        for (AppWidgetProviderInfo info : widgets) {
//            WidgetItemInfo wInfo = new WidgetItemInfo(mContext, info, info.provider);
//            wInfo.mItemType = ItemInfo.ITEM_TYPE_WIDGET;
//            data.add(wInfo);
//        }
    }

    public List<ItemInfo> findPreForPackage(String packageName) {
        List<ItemInfo> found = new ArrayList<ItemInfo>();
        for (ItemInfo item : data) {
            ComponentName component = item.getComponentName();
            if (component.getPackageName().equals(packageName)) {
                found.add(item);
            }
        }
        return found;
    }

    public List<ItemInfo> getCurForPackage(String packageName) {
        List<ItemInfo> get = new ArrayList<ItemInfo>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
        if (apps != null) {
            for (ResolveInfo info : apps) {
                ComponentName componentName = new ComponentName(packageName, info.activityInfo.name);
                AppItemInfo itemInfo = new AppItemInfo(mContext, info, componentName);
                try {
                    PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(info.activityInfo.packageName, 0);
                    itemInfo.mInstallDateTime = packageInfo.firstInstallTime;
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                get.add(itemInfo);
            }
        }
        List<AppWidgetProviderInfo> widgets = AppWidgetManager.getInstance(mContext).getInstalledProviders();
        if (widgets != null) {
            for (AppWidgetProviderInfo widget : widgets) {
                if (widget.provider.getPackageName().equals(packageName)) {
                    WidgetItemInfo wInfo = new WidgetItemInfo(mContext, widget, widget.provider);
                    get.add(wInfo);
                }
            }
        }
        return get;
    }
}
