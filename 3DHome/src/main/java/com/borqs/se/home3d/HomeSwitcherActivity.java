package com.borqs.se.home3d;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

public class HomeSwitcherActivity extends Activity {

    private void switchHomes() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> homeList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        ResolveInfo ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (ri == null || ri.activityInfo == null) {
            return;
        }
        for(ResolveInfo info : homeList) {
            if (info == null || info.activityInfo == null) {
                continue;
            }
            if (info.activityInfo.packageName.equals(ri.activityInfo.packageName)) {
                ComponentName homeAlias1 = new ComponentName(this, getPackageName() + ".HomeAlias1");
                ComponentName homeAlias2 = new ComponentName(this, getPackageName() + ".HomeAlias2");
                if (pm.getComponentEnabledSetting(homeAlias1) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    getPackageManager().setComponentEnabledSetting(homeAlias1, 
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    getPackageManager().setComponentEnabledSetting(homeAlias2, 
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    getPackageManager().setComponentEnabledSetting(homeAlias1, 
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    getPackageManager().setComponentEnabledSetting(homeAlias2, 
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchHomes();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }
}
