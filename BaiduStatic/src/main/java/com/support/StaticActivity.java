package com.support;

import com.baidu.mobstat.StatService;

import android.app.Activity;


/**
 * Created by b608 on 13-10-17.
 */
public class StaticActivity extends Activity {

    @Override
    protected void onResume() {
        super.onStart();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onStop();
        StatService.onPause(this);
    }
}
