/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.market;


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.borqs.market.service.DownloadReciver;
import com.borqs.market.utils.BLog;

public class MarketApplication extends Application {
    public boolean isLogin = true; // 判断用户是否已经登录
    public static final String PREFERENCES_NAME = "BORQS_DIRECOTRY_SEETING";
    public static final String USER_ID = "USER_ID";
    public static final String LOGIN_NAME = "LOGIN_NAME";
    public static final String TICKET = "TICKET";
    public static final String NICK_NAME = "NICK_NAME";

    // /**
    // * 登录
    // *
    // * @param user
    // * 用户
    // */
    // public void Login(AccountSessionData account) {
    //
    // borqsAccount.uid = account.getUserId();
    // borqsAccount.sessionid = account.getSessionId();
    // borqsAccount.nickname = account.getNickName();
    //
    // isLogin = true;
    // // startService(new Intent(this,FriendsSyncService.class));
    // }

    DownloadReciver receiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // receiver = new DownloadReciver();
        // enableDownloadReciver(this);
    }

    // private void enableDownloadReciver(Context context){
    // BLog.d("DownloadReciver will be enabled in " + context.getPackageName());
    //
    // ComponentName[] components = new ComponentName[]{
    // new ComponentName(context, DownloadReciver.class),
    // };
    // PackageManager pm = context.getPackageManager();
    // for(ComponentName cn : components){
    // pm.setComponentEnabledSetting(
    // cn,
    // PackageManager.COMPONENT_ENABLED_STATE_ENABLED ,
    // PackageManager.DONT_KILL_APP);
    // }
    // }

    public void disableDownloadReciver(Context context) {
        BLog.d("DownloadReciver will be disabled in "
                + context.getPackageName());

        ComponentName[] components = new ComponentName[] { new ComponentName(
                context, DownloadReciver.class), };
        PackageManager pm = context.getPackageManager();
        for (ComponentName cn : components) {
            pm.setComponentEnabledSetting(cn,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public static MarketApplication getApplication(Context context) {
        return (MarketApplication) context.getApplicationContext();
    }

}
