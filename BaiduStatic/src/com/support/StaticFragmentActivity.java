package com.support;

import com.baidu.mobstat.StatService;

import android.support.v4.app.FragmentActivity;

/**
 * Created by b608 on 13-10-17.
 */
public class StaticFragmentActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }
}
