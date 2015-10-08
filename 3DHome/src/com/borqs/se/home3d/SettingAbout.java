package com.borqs.se.home3d;

import com.borqs.se.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingAbout extends Activity {
    private int mClickCount = 0;

    @Bind(R.id.person_group_0) View mPersonGroup_0;
    @Bind(R.id.person_group_1) View mPersonGroup_1;

    private Handler mHandler;
    private final int MSG_SWITCH_PERSON = 1;

    @OnClick(R.id.app_name)
    void onAppNameClick() {
        mClickCount++;
        if (mClickCount % 5 == 0) {
            HomeManager.getInstance().debug(!HomeUtils.DEBUG);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ButterKnife.bind(this);

        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowTitleEnabled(true);

        TextView versionTV = ButterKnife.findById(this, R.id.version);
        String packageName = getPackageName();
        String version = "0.1.1(1001)";
        try {
            version = getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        versionTV.setText(String.format(SettingAbout.this.getString(R.string.about_version), String.valueOf(version)));

        if (mPersonGroup_0 != null && mPersonGroup_1 != null) {
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case MSG_SWITCH_PERSON:
                        playSwitchAnimation(mPersonGroup_0, mPersonGroup_1);
                        sendEmptyMessageDelayed(MSG_SWITCH_PERSON, 3800);
                        break;
                    }
                }

            };
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_SWITCH_PERSON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_SWITCH_PERSON);
            mHandler.sendEmptyMessageDelayed(MSG_SWITCH_PERSON, 500);
        }
    }

    private void playSwitchAnimation(View view_0, View view_1) {
        final View viewA;
        final View viewB;
        if (view_0.getVisibility() == View.GONE) {
            viewA = view_1;
            viewB = view_0;
        } else {
            viewA = view_0;
            viewB = view_1;
        }

        AlphaAnimation alphaAnimationA = new AlphaAnimation(1, 0);
        alphaAnimationA.setDuration(500);
        alphaAnimationA.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {

                AlphaAnimation alphaAnimationB = new AlphaAnimation(0, 1);
                alphaAnimationB.setDuration(500);
                alphaAnimationB.setStartOffset(0);
                alphaAnimationB.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                        viewA.setVisibility(View.GONE);
                        viewB.setVisibility(View.VISIBLE);

                    }

                });
                viewB.startAnimation(alphaAnimationB);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation animation) {
                viewA.setVisibility(View.VISIBLE);
                viewB.setVisibility(View.GONE);

            }
        });
        viewA.startAnimation(alphaAnimationA);
    }
}
