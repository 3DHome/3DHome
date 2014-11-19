package com.borqs.se.upgrade;

import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.SettingsActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateServerChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (HomeUtils.DEBUG)
            Log.d("3DHome: UpgradeTest ##UpdateServerChangeReceiver", "action = " + action);
        boolean isTest = intent.getBooleanExtra("is_test_server", false);
        if (HomeUtils.DEBUG)
            Log.d("3DHome: UpgradeTest ##UpdateServerChangeReceiver", "is_test_server = " + isTest);
        SettingsActivity.saveIsTestOrRelease(context, isTest);
    }

}
