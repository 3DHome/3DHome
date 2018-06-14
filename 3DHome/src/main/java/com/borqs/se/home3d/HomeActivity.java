package com.borqs.se.home3d;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.SERenderView;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.HomeUtils.SceneOrientation;
import com.borqs.se.upgrade.UpgradeTest;
import com.borqs.se.widget3d.ADViewController;
import com.funyoung.androidfacade.CommonHelperUtils;
import com.support.StaticFragmentActivity;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;

public class HomeActivity extends StaticFragmentActivity implements View.OnCreateContextMenuListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private AppSearchPane mAppSearchPane;
    private SERenderView mRenderView;
    private HomeManager mHomeManager;
    private ADViewController mADViewController;

    private HomeReceiver mHomeReceiver;
    private Handler mHandler;
    private boolean mHasLeave3DHome;
    private boolean mUserLeaveHint;
    private PowerManager mPowerManager;
    private boolean isLiving;

    private UpgradeTest mAutoChecker;
    private String mUpgradeUrl;
    private StringBuilder mReleaseNode;
    private static final String mUpgradePreferencesName = HomeUtils.PKG_CURRENT_NAME + ".upgrade";
    private final static String KEY_VERSION_NUMBER = "saved.version";
    private final static String KEY_USER_CANCELED = "canceled.by.user";
    private final static String KEY_CHECK_DATE = "checked.date";
    private final static String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";
    // dialog id should not conflict with dialog id in SE3DHomeScene.java, they
    // are use same onCreateDialog()and onPrepareDialog().
    private static final int ALERT_DIALOG_UPDATE_SW = 1000;
    public static final int MSG_GET_UPGRADE_INFO = 1000;

    // todo: fix memory leak by non static inner class.
    private final Handler mHandlerForUpgrade = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_GET_UPGRADE_INFO:
                Bundle data = msg.getData();
                int curVersion = data.getInt("cur_version");
                int latestVersion = data.getInt("latest_version");
                SharedPreferences upgradePreferences = getSharedPreferences(mUpgradePreferencesName, 0);
                int savedVersion = upgradePreferences.getInt(KEY_VERSION_NUMBER, -1);
                boolean userCanceled = upgradePreferences.getBoolean(KEY_USER_CANCELED, false);
                if (savedVersion < latestVersion) {
                    SharedPreferences.Editor editor = upgradePreferences.edit();
                    editor.putInt(KEY_VERSION_NUMBER, latestVersion);
                    editor.putBoolean(KEY_USER_CANCELED, false);
                    userCanceled = false;
                    editor.commit();
                }
                if (!userCanceled) {
                    long size = data.getLong("size");
                    DecimalFormat formatter = new DecimalFormat();
                    formatter.setMaximumFractionDigits(2);
                    formatter.setGroupingSize(0);
                    formatter.setRoundingMode(RoundingMode.FLOOR);
                    String fileSize = formatter.format(size / (1024f * 1024f)) + "MB";
                    mUpgradeUrl = data.getString("url");
                    String releaseNotes = data.getString("release_note");
                    mReleaseNode = new StringBuilder();
                    Resources res = getResources();
                    mReleaseNode.append(res.getString(R.string.upgrade_dialog_msg));
                    mReleaseNode.append("\r\n\r\n");
                    mReleaseNode.append(res.getString(R.string.upgrade_dialog_current_version));
                    mReleaseNode.append(curVersion);
                    mReleaseNode.append("\r\n");
                    mReleaseNode.append(res.getString(R.string.upgrade_dialog_latest_version));
                    mReleaseNode.append(latestVersion);
                    mReleaseNode.append("\r\n");
                    mReleaseNode.append(res.getString(R.string.upgrade_dialog_file_size));
                    mReleaseNode.append(fileSize);
                    mReleaseNode.append("\r\n");
                    mReleaseNode.append(res.getString(R.string.upgrade_dialog_update_changes));
                    mReleaseNode.append("\r\n");
                    mReleaseNode.append(releaseNotes);
                    if (!isFinishing()) {
                        showDialog(ALERT_DIALOG_UPDATE_SW);
                    }
                }
                break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle icicle) {
        getFilesDir();
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "SEHomeActivity onCreate time : " + System.currentTimeMillis());
        super.onCreate(icicle);
//        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        getWindow().addFlags(getValueOfFlagNeedsMenuKey());
        mHomeManager = HomeManager.getInstance();
        SESceneManager.getInstance().setGLActivity(this);
        mHandler = new Handler();
        SESceneManager.getInstance().setHandler(mHandler);
        mHomeManager.bindWeatherService();
        mHomeManager.startVideoThumbnailsService();
        initView();
        mHomeReceiver = new HomeReceiver();
        mADViewController = ADViewController.getInstance();
        mHasLeave3DHome = true;
        mUserLeaveHint = false;
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isLiving = true;

        // start thread for check latest version
        checkUpgrade();
    }

    // 获取WindowManger.LayoutParams.FLAG_NEEDS_MENU_KEY的值
    private int getValueOfFlagNeedsMenuKey() {
        try {
            Class<WindowManager.LayoutParams> c = WindowManager.LayoutParams.class;
            Field field = c.getField("FLAG_NEEDS_MENU_KEY");
            field.setAccessible(true);
            return field.getInt(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0x08000000;
    }

    private void checkUpgrade() {
        SharedPreferences upgradePreferences = getSharedPreferences(mUpgradePreferencesName, 0);
        String dateStr = upgradePreferences.getString(KEY_CHECK_DATE, "");
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String newDate = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);

        boolean autoChecking;
        try {
            getPackageManager().getApplicationInfo(GOOGLE_PLAY_PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
            autoChecking = true;
        } catch (NameNotFoundException e) {
            autoChecking = false;
        }
        if (!autoChecking && !newDate.equals(dateStr)) {
            mAutoChecker = new UpgradeTest(HomeActivity.this, mHandlerForUpgrade, UpgradeTest.TEST_TYEP_ACTIVITY);
            mAutoChecker.start();
            CommonHelperUtils.putString(upgradePreferences, KEY_CHECK_DATE, newDate);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        super.onMenuOpened(featureId, menu);
        return SESceneManager.getInstance().onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return SESceneManager.getInstance().onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return SESceneManager.getInstance().onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return SESceneManager.getInstance().onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case ALERT_DIALOG_UPDATE_SW:
            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(R.string.upgrade_dialog_title)
                    .setPositiveButton(R.string.upgrade_dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, final int whichButton) {
                            SharedPreferences upgradePreferences = getSharedPreferences(mUpgradePreferencesName, 0);
                            CommonHelperUtils.putBoolean(upgradePreferences, KEY_USER_CANCELED, true);
                            dialog.dismiss();
                            if (mUpgradeUrl != null) {
                                try {
                                    Uri uri = Uri.parse(mUpgradeUrl);
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(it);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(HomeManager.getInstance().getContext(), R.string.activity_not_found,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }).setNegativeButton(R.string.upgrade_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, final int whichButton) {
                            // user ignore this version
                            SharedPreferences upgradePreferences = getSharedPreferences(mUpgradePreferencesName, 0);
                            CommonHelperUtils.putBoolean(upgradePreferences, KEY_USER_CANCELED,true);
                            dialog.dismiss();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            SharedPreferences upgradePreferences = getSharedPreferences(mUpgradePreferencesName, 0);
                            CommonHelperUtils.putBoolean(upgradePreferences, KEY_USER_CANCELED,true);
                        }
                    }).create();
            if (mReleaseNode != null) {
                alertDialog.setMessage(mReleaseNode);
            }
            return alertDialog;
        }
        return SESceneManager.getInstance().onCreateDialog(id, bundle);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        switch (id) {
        case ALERT_DIALOG_UPDATE_SW:
            AlertDialog alertDialog = (AlertDialog) dialog;
            if (mReleaseNode != null) {
                alertDialog.setMessage(mReleaseNode.toString());
            }
            break;
        default:
            SESceneManager.getInstance().onPrepareDialog(id, dialog, bundle);
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        mUserLeaveHint = true;
    }

    private void initView() {
        boolean hasScene = mHomeManager.getHomeScene() != null;
        WorkSpace workSpace = findViewById(R.id.workspace);
        mHomeManager.setWorkSpace(workSpace);
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHomeManager.setSceneOrientation(SceneOrientation.AUTO_LAND); 
        } else {
            mHomeManager.setSceneOrientation(SceneOrientation.AUTO_PORT); 
        }
        mRenderView = new SERenderView(this, false);
        workSpace.addView(mRenderView);
        SESceneManager.getInstance().setGLSurfaceView(mRenderView);
        SESceneManager.getInstance().setGLActivity(this);
        if (SettingsActivity.getFPSSetting(this)) {
            mHomeManager.showFPSView();
            mRenderView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        } else {
            mHomeManager.clearFPSView();
            mRenderView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
        if (hasScene) {
            SESceneManager.getInstance().onActivityRestart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SettingsActivity.isEnableFullScreen(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mHomeReceiver, filter);
        SESceneManager.getInstance().setHandler(mHandler);
        SESceneManager.getInstance().setGLSurfaceView(mRenderView);
        SESceneManager.getInstance().setGLActivity(this);
        mHomeManager.onActivityResume();
        SESceneManager.getInstance().onActivityResume();
        mHomeManager.stopLocalService();
        mADViewController.onResume();
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "SEHomeActivity onResume time : " + System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "SEHomeActivity onPause time : " + System.currentTimeMillis());
        try {
            unregisterReceiver(mHomeReceiver);
        } catch (IllegalArgumentException e) {
            if (HomeUtils.DEBUG)
                Log.e(TAG, "onPause, exception " + e.getMessage());
        }
        mHasLeave3DHome = mUserLeaveHint || !isTopActivity() || !mPowerManager.isScreenOn();
        mUserLeaveHint = false;
        // 假如没有退出3DHome界面可以不执行释放硬件，比如在设置Home为默认Launcher的时候按Home key可以不执行释放和加载硬件
        SESceneManager.getInstance().setNeedDestoryHardware(mHasLeave3DHome);
        SESceneManager.getInstance().onActivityPause();
        mHomeManager.startLocalService();
        mADViewController.onPause();
        if (mAutoChecker != null) {
            mAutoChecker.stopUpgrade();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "SEHomeActivity onStop time : " + System.currentTimeMillis());
        System.gc();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "SEHomeActivity onStart time : " + System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isLiving = false;
        SESceneManager.getInstance().onActivityDestroy();
        mADViewController.onDestory();
    }

    public boolean isLiving() {
        return isLiving;
    }

    @Override
    public void onBackPressed() {
        mAppSearchPane = mHomeManager.getAppSearchPane();
        if (mAppSearchPane != null && mAppSearchPane.getVisibility() == View.VISIBLE) {
            mAppSearchPane.setVisibility(View.INVISIBLE);
            // mAppSearchPane = null;
        } else {
            SESceneManager.getInstance().handleBackKey();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (HomeUtils.DEBUG)
            Log.d(TAG, "onKeyUp, keyCode = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            SESceneManager.getInstance().handleMenuKey();
        }
        return keyCode != KeyEvent.KEYCODE_SEARCH && super.onKeyUp(keyCode, event);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SESceneManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            if (alreadyOnHome) {
                SESceneManager.getInstance().onNewIntent(intent);
                try {
                    mAppSearchPane = mHomeManager.getAppSearchPane();
                    if (mAppSearchPane != null && mAppSearchPane.getVisibility() == View.VISIBLE) {
                        mAppSearchPane.setVisibility(View.INVISIBLE);
                        CommonHelperUtils.hideIme(HomeManager.getInstance().getContext(), mAppSearchPane.getWindowToken());
                    }
                    dismissDialog(ALERT_DIALOG_UPDATE_SW);
                } catch (Exception e) {
                    if (HomeUtils.DEBUG)
                        Log.e(TAG, "onNewIntent, exception " + e.getMessage());
                }
            }
        }
    }

    private boolean isTopActivity() {
        return CommonHelperUtils.isTopActivity(this, getComponentName());
    }

    private class HomeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                for (HomeManager.TimeChangeCallBack callBack : mHomeManager.mTimeCallBacks) {
                    if (callBack != null) {
                        callBack.onTimeChanged();
                    }
                }
            }
        }
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

}
