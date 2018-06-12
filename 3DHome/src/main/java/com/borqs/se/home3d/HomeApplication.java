package com.borqs.se.home3d;

import java.io.File;
import java.util.List;

import com.borqs.market.utils.MarketConfiguration;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.borqs.se.R;
import com.borqs.se.download.Utils;
import com.borqs.se.shortcut.LauncherModel;
import com.support.StaticUtil;


public class HomeApplication extends Application {
    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    LauncherModel mModel;


    private static HomeApplication mLauncherApplication;

    public static HomeApplication getInstance() {
        return mLauncherApplication;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        StaticUtil.initGLStatic(getApplicationContext());
        if (HomeUtils.PKG_CURRENT_NAME.equals(getCurProcessName())) {
            HomeUtils.attachApplication(this);
            MarketConfiguration.init(getApplicationContext());
            MarketConfiguration.setIabKey(getResources().getString(R.string.iab_google_public));
            String localFilePath = HomeUtils.getLocalFilePath(getApplicationContext(), null);
            MarketConfiguration.setSystemThemeDir(new File(localFilePath));
            mLauncherApplication = this;
            mModel = LauncherModel.getInstance();
            mModel.init(this);
            // 在SESceneManager初始化的时候会调用LauncherModel方法，所以LauncherModel应该靠前初始话
            HomeManager.getInstance().init(this);
           
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                        PackageManager.GET_META_DATA);
                boolean withoutAD = appInfo.metaData.getBoolean("WITHOUT_ADVERTISEMENT");
                HomeManager.getInstance().setWithoutAD(withoutAD);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Register intent receivers
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            registerReceiver(mModel, filter);
            filter = new IntentFilter();
            filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            registerReceiver(mModel, filter);
            filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mModel, filter);
            filter = new IntentFilter(ACTION_INSTALL_SHORTCUT);
            filter.addAction(ACTION_UNINSTALL_SHORTCUT);
            registerReceiver(mModel, filter);
            Utils.reportMonitoredApps(getApplicationContext());
            if (HomeUtils.isPad(this)) {
                SettingsActivity.saveEnableFullScreen(this, false);
            }
        }
    }

    private String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return HomeUtils.PKG_CURRENT_NAME;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mModel != null) {
            unregisterReceiver(mModel);
        }
    }


}
