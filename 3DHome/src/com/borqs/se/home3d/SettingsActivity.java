package com.borqs.se.home3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;
import com.borqs.se.R;
import com.borqs.se.download.Utils;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.upgrade.UpgradeTest;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {
    private static final String KEY_FPS = "fps_key";
    private static final String KEY_BETA_DATA = "show_beta_data";
    private static final String KEY_DISPLAYSETTING = "DisplaySetting";
    private static final String KEY_HELP = "key_help";
    private static final String KEY_TEST_OR_RELEASE = "key_test_or_release";
    private static final String KEY_UPGRADE_VIEW = "upgrade_key";
    public static final String PREFS_SETTING_NAME = "com.borqs.se_preferences";
    private static final String KEY_HELP_MENU = "help";
    private static final String KEY_FEED_BACK = "feedback";
    private static final String KEY_SCORE = "score";
    private static final String KEY_SHARE = "share";
    private static final String KEY_CAMERA_ADJUST = "camera_adjust";
    private static final String KEY_AD_CONTENT = "ad_content";
    private static final String KEY_DEBUG_BETA_DATA = "market_test_beta_data_enable";
    private static final String FULL_SCREEN_KEY = "full_screen_key";
    private static final String KEY_SHOW_WALL_INDICATOR = "show_wall_indicator";
    private static final String KEY_UPDATE_LOCAL_RESOURCE_FINISHED = "update_local_resource_finished";
    private static final String KEY_HAS_GIVEN_MONEY_TO_US = "has_bought_airplane";
    private static final String KEY_APPICON_BACKGROUND = "appicon_background_key";
    private static final String DOWNLOAD_OBJECT_KEY = "download_object_key";
    private static final String THEME_KEY = "theme_key";

    public static final int ALERT_DIALOG_UPDATE_SW = 0;
    public static final int PROGRESS_DIALOG_UPDATE_SW = 1;

    public static final int MSG_SHOW_DIALOG_UPGRADE = 0;
    public static final int MSG_START_UPGRADE = MSG_SHOW_DIALOG_UPGRADE + 1;
    public static final int MSG_ERROR_REMOVE_UPGRADE_PD = MSG_START_UPGRADE + 1;
    public static final int MSG_LATEST_REMOVE_UPGRADE_PD = MSG_ERROR_REMOVE_UPGRADE_PD + 1;

    private PreferenceGroup mDisplay;
    private CheckBoxPreference mPrefFPS;
    private CheckBoxPreference mPrefBetaData;
    private PreferenceScreen mPrefUpgrade;
    private CheckBoxPreference mFullScreen;
    private CheckBoxPreference mShowWallIndicator;
    private EditTextPreference mADContent;
    private Preference mScore;
    private ListPreference mAppIconBackground;

    private ProgressDialog mUpdateSwPD;
    private String mUpgradeUrl;
    private StringBuilder mReleaseNode;
    private UpgradeTest mUpgradeDetector;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SHOW_DIALOG_UPGRADE:
                if (!isFinishing()) {
                    removeDialog(PROGRESS_DIALOG_UPDATE_SW);
                }
                Bundle data = msg.getData();
                int curVersion = data.getInt("cur_version");
                int latestVersion = data.getInt("latest_version");
                long size = data.getLong("size");
                DecimalFormat formater = new DecimalFormat();
                formater.setMaximumFractionDigits(2);
                formater.setGroupingSize(0);
                formater.setRoundingMode(RoundingMode.FLOOR);
                String fileSize = formater.format(size / (1024f * 1024f)) + "MB";
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
                break;
            case MSG_START_UPGRADE:
                if (!isFinishing()) {
                    showDialog(PROGRESS_DIALOG_UPDATE_SW);
                }
                if (mUpgradeDetector != null) {
                    mUpgradeDetector.stopUpgrade();
                }
                mUpgradeDetector = new UpgradeTest(SettingsActivity.this, this, UpgradeTest.TEST_TYPE_SETTINGS);
                mUpgradeDetector.start();
                break;
            case MSG_LATEST_REMOVE_UPGRADE_PD:
                if (!isFinishing()) {
                    removeDialog(PROGRESS_DIALOG_UPDATE_SW);
                }
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.no_update, Toast.LENGTH_SHORT)
                        .show();
                break;
            case MSG_ERROR_REMOVE_UPGRADE_PD:
                if (!isFinishing()) {
                    removeDialog(PROGRESS_DIALOG_UPDATE_SW);
                }
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.check_update_error,
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_preference);
            actionBar.setDisplayShowTitleEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        addPreferencesFromResource(R.xml.settings);
        initSetting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case android.R.id.home:
//            finish();
//            return true;
//        }

        return true;
    }

    @Override
    protected void onPause() {
        mHandler.removeMessages(MSG_START_UPGRADE);
        mHandler.removeMessages(MSG_SHOW_DIALOG_UPGRADE);
        removeDialog(ALERT_DIALOG_UPDATE_SW);
        removeDialog(PROGRESS_DIALOG_UPDATE_SW);
        if (mUpgradeDetector != null) {
            mUpgradeDetector.stopUpgrade();

        }
        super.onPause();
        listenBackToScene();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasGivenMoneyToUs(SESceneManager.getInstance().getContext())) {
            mDisplay.removePreference(mADContent);
        } else {
            mDisplay.addPreference(mADContent);
        }
        if (getString(R.string.channel_amazon).equalsIgnoreCase(getChannelCode(this))) {
            getPreferenceScreen().removePreference(mScore);
        }
        cancelListenBackToScene();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case ALERT_DIALOG_UPDATE_SW:
            AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.upgrade_dialog_title)
                    .setPositiveButton(R.string.upgrade_dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, final int whichButton) {
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
                            dialog.dismiss();
                        }
                    }).create();
            if (mReleaseNode != null) {
                alertDialog.setMessage(mReleaseNode);
            }
            return alertDialog;
        case PROGRESS_DIALOG_UPDATE_SW:
            if (mUpdateSwPD == null) {
                mUpdateSwPD = new ProgressDialog(SettingsActivity.this);
                mUpdateSwPD.setTitle(R.string.wait_dialog_title);
                mUpdateSwPD.setMessage(getString(R.string.wait_dialog_msg_sw));
                mUpdateSwPD.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.removeMessages(MSG_START_UPGRADE);
                        mHandler.removeMessages(MSG_SHOW_DIALOG_UPGRADE);
                        if (mUpgradeDetector != null) {
                            mUpgradeDetector.stopUpgrade();

                        }
                    }
                });
            }
            if (!mUpdateSwPD.isShowing()) {
                return mUpdateSwPD;
            }
            break;
        default:
            break;
        }
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case ALERT_DIALOG_UPDATE_SW:
            AlertDialog alertDialog = (AlertDialog) dialog;
            if (mReleaseNode != null) {
                alertDialog.setMessage(mReleaseNode.toString());
            }
            break;
        }
    }

    private void initSetting() {
        // clear padding for listview
        if (getListView() != null) {
            if (getListView().getParent() != null && getListView().getParent() instanceof View) {
                View pp = (View) getListView().getParent();
                pp.setPadding(0, 0, 0, 0);
            }
            getListView().setPadding(0, 0, 0, 0);
        }

    	mDisplay = (PreferenceGroup) getPreferenceScreen().findPreference(KEY_DISPLAYSETTING);
        mPrefFPS = (CheckBoxPreference) findPreference(KEY_FPS);
        mPrefFPS.setChecked(getFPSSetting(this));
        mPrefFPS.setOnPreferenceChangeListener(this);
        if (!HomeUtils.DEBUG) {
        	mDisplay.removePreference(mPrefFPS);
        }
        mPrefBetaData = (CheckBoxPreference) findPreference(KEY_BETA_DATA);
        mPrefBetaData.setChecked(getShowBetaDataSettings(this));
        mPrefBetaData.setOnPreferenceChangeListener(this);
        if (!HomeUtils.DEBUG) {
            mDisplay.removePreference(mPrefBetaData);
        } else if(getShowBetaDataSettings(this)) {
            MarketConfiguration.setIS_DEBUG_BETA_REQUEST(mPrefBetaData.isChecked());
        }

        mFullScreen = (CheckBoxPreference) findPreference(FULL_SCREEN_KEY);
        mFullScreen.setChecked(isEnableFullScreen(this));
        mFullScreen.setOnPreferenceChangeListener(this);
        if (HomeUtils.isPad(this)) {
        	mDisplay.removePreference(mFullScreen);
        }
        mShowWallIndicator = (CheckBoxPreference) findPreference(KEY_SHOW_WALL_INDICATOR);
        mShowWallIndicator.setChecked(isEnableWallIndicator(this));
        mShowWallIndicator.setOnPreferenceChangeListener(this);
        
        mPrefUpgrade = (PreferenceScreen) findPreference(KEY_UPGRADE_VIEW);
        String summary = this.getResources().getString(R.string.upgrade_dialog_current_version);
        String packageName = getPackageName();
        String version = "";
        try {
            version = getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        mPrefUpgrade.setSummary(summary + version);
        mPrefUpgrade.setOnPreferenceClickListener(this);
        Preference help = findPreference(KEY_HELP_MENU);
        help.setOnPreferenceClickListener(this);

        mScore = findPreference(KEY_SCORE);
        mScore.setOnPreferenceClickListener(this);

        Preference share = findPreference(KEY_SHARE);
        share.setOnPreferenceClickListener(this);

//        PreferenceScreen feedback = (PreferenceScreen) findPreference(KEY_FEED_BACK);
//        feedback.setOnPreferenceClickListener(this);

        Preference cameraAdjust = findPreference(KEY_CAMERA_ADJUST);
        cameraAdjust.setOnPreferenceClickListener(this);

        mADContent = (EditTextPreference) findPreference(KEY_AD_CONTENT);
        mADContent.setOnPreferenceChangeListener(this);
        
        Preference downloadObjPre = findPreference(DOWNLOAD_OBJECT_KEY);
        if(downloadObjPre != null) {
            downloadObjPre.setOnPreferenceClickListener(this);
        }
        
        Preference themeKeyPre = findPreference(THEME_KEY);
        if(themeKeyPre != null) {
            themeKeyPre.setOnPreferenceClickListener(this);
        }

        initWallpaperAndUserShareSettings();

        initShelfSetting();

        mAppIconBackground = (ListPreference) findPreference(KEY_APPICON_BACKGROUND);
        int iconType = getAppIconBackgroundType(this);
        String[] iconSummary = getResources().getStringArray(R.array.appicon_background_entries);
        mAppIconBackground.setSummary(iconSummary[iconType]);
        mAppIconBackground.setValueIndex(iconType);
        mAppIconBackground.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_UPGRADE_VIEW)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                mHandler.removeMessages(MSG_START_UPGRADE);
                mHandler.sendEmptyMessageDelayed(MSG_START_UPGRADE, 500);
            } else {
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.network_error, Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        } else if (KEY_HELP_MENU.equals(preference.getKey())) {
            finish();
            setHelpStatus(this, false);
            HomeManager.getInstance().setShowHelpDialog(true);
        } else if (KEY_FEED_BACK.equals(preference.getKey())) {
            HomeUtils.showFeedbackActivity(this);
        } else if (KEY_SCORE.equals(preference.getKey())) {
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e) {
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.activity_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (KEY_SHARE.equals(preference.getKey())) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content, getChannelSharedContent(), SettingsActivity.this.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e) {
                Toast.makeText(SESceneManager.getInstance().getContext(), R.string.activity_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (KEY_CAMERA_ADJUST.equals(preference.getKey())) {
            finish();
            SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_SHOW_CAMERA_ADJUST_DIALOG);
            SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_CAMERA_ADJUST_DIALOG, null);
        } else if (onWallpaperOrUserSharePreference(preference.getKey())) {
            return true;
        } else if(DOWNLOAD_OBJECT_KEY.equals(preference.getKey())) {
            finish();
            SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_SHOW_OBJECT_VIEW);
            SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_OBJECT_VIEW, null);
        } else if(THEME_KEY.equals(preference.getKey())) {
            Utils.showThemes(this);
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference.getKey().equals(KEY_FPS)) {
            boolean state = (Boolean) objValue;
            SharedPreferences settings = getSharedPreferences(PREFS_SETTING_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(KEY_FPS, state);
            editor.commit();
            getFPSSetting(this);
            if (state) {
                HomeManager.getInstance().showFPSView();
                SESceneManager.getInstance().getGLSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            } else {
                HomeManager.getInstance().clearFPSView();
                SESceneManager.getInstance().getGLSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
        } else if (FULL_SCREEN_KEY.equals(preference.getKey())) {
            boolean state = (Boolean) objValue;
            mFullScreen.setChecked(state);
            saveEnableFullScreen(this, state);
        } else if (KEY_SHOW_WALL_INDICATOR.equals(preference.getKey())) {
            boolean state = (Boolean) objValue;
            mShowWallIndicator.setChecked(state);
            saveEnableWallIndicator(this, state);
            HomeManager.getInstance().setShowWallIndicator(state);
        } else if (KEY_AD_CONTENT.equals(preference.getKey())) {
            if (!TextUtils.isEmpty((String) objValue)) {
                saveAdContent(this, (String) objValue);
                preference.setSummary((String) objValue);
            }
        } else if (KEY_BETA_DATA.equals(preference.getKey())) {
            SharedPreferences settings = getSharedPreferences(PREFS_SETTING_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(KEY_DEBUG_BETA_DATA, (Boolean)objValue);
            editor.commit();
            MarketConfiguration.setIS_DEBUG_BETA_REQUEST((Boolean)objValue);
        } else if (KEY_SHELF_LABEL.equalsIgnoreCase(preference.getKey())) {
            boolean state = (Boolean) objValue;
            setShowShelfSetting(state);
            HomeManager.getInstance().setShowAppShelf(state);
        } else if (KEY_SHELF_DESK_OBJECT.equalsIgnoreCase(preference.getKey())) {
            boolean state = (Boolean) objValue;
            setShowDeskObjectShelfSetting(state);
            HomeManager.getInstance().setShowDeskObjectShelf(state);
        } else if (KEY_APPICON_BACKGROUND.equalsIgnoreCase(preference.getKey())) {
            int iconBackgroundType = Integer.parseInt((String) (objValue));
            setAppIconBackgroundType(this, iconBackgroundType);
            String[] iconSummary = getResources().getStringArray(R.array.appicon_background_entries);
            mAppIconBackground.setSummary(iconSummary[iconBackgroundType]);
            mAppIconBackground.setValueIndex(iconBackgroundType);
            HomeManager.getInstance().setAppIconBackgroundType(iconBackgroundType);
        }
        return true;
    }

    public static boolean getHelpStatus(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_HELP, false);
    }

    public static void setHelpStatus(Context context, boolean flag) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_HELP, flag);
        editor.commit();
    }

    public static boolean getFPSSetting(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_FPS, false);
    }

    public static boolean getIsTestOrRelease(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_TEST_OR_RELEASE, false);
    }

    public static void saveIsTestOrRelease(Context context, boolean isTest) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_TEST_OR_RELEASE, isTest);
        editor.commit();
    }

    public static boolean isEnableFullScreen(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(FULL_SCREEN_KEY, false);
    }

    public static void saveEnableFullScreen(Context context, boolean enable) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(FULL_SCREEN_KEY, enable);
        editor.commit();
    }

    public static boolean isEnableWallIndicator(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_SHOW_WALL_INDICATOR, false);
    }

    public static void saveEnableWallIndicator(Context context, boolean enable){
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SHOW_WALL_INDICATOR, enable);
        editor.commit();
    }

	public static boolean hasUpdatedLocalRes(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFS_SETTING_NAME, 0);
		return settings.getBoolean(KEY_UPDATE_LOCAL_RESOURCE_FINISHED, false);
	}

	public static void setUpdateLocalResFinished(Context context,
			boolean finished) {
		SharedPreferences settings = context.getSharedPreferences(
				SettingsActivity.PREFS_SETTING_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_UPDATE_LOCAL_RESOURCE_FINISHED, finished);
		editor.commit();
	}

    public static String getAdContent(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getString(KEY_AD_CONTENT, "");
    }

    private void saveAdContent(Context context, String content) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_AD_CONTENT, content);
        editor.commit();
    }

    private String getChannelSharedContent() {
        final String channelCode = getChannelCode(this);
        if (getString(R.string.channel_amazon).equalsIgnoreCase(channelCode)) {
            return getString(R.string.share_content_link_amazon);
        }

        return getString(R.string.share_content_link_google);
    }

    public static String getChannelCode(Context context) {
        String code = getMetaData(context, context.getString(R.string.key_channel));
        if (code != null) {
            return code;
        }
        return context.getString(R.string.channel_default);
    }

    private static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private boolean getShowBetaDataSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_DEBUG_BETA_DATA, false);
    }
    
    public static boolean hasGivenMoneyToUs(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_HAS_GIVEN_MONEY_TO_US, false);
    }

    public static void setHasGivenMoneyToUs(Context context, boolean flag) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_HAS_GIVEN_MONEY_TO_US, flag);
        editor.commit();
    }

    public static int getAppIconBackgroundType(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        String type= settings.getString(KEY_APPICON_BACKGROUND, "0");
        return Integer.parseInt(type);
    }

    public static void setAppIconBackgroundType(Context context, int type) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_APPICON_BACKGROUND,Integer.toString(type));
        editor.commit();
    }
    /// wallpaper and user share begin
    private static final boolean ENABLED_WALLPAPER = true;
    private static final String KEY_WALLPAPER = "wallpaper_key";
    private static final String KEY_USER_SHARE = "user_share_key";
    private void initWallpaperAndUserShareSettings() {
        Preference wallpaper = findPreference(KEY_WALLPAPER);
        if (null != wallpaper) {
            wallpaper.setOnPreferenceClickListener(this);
            if (!ENABLED_WALLPAPER) {
                mDisplay.removePreference(wallpaper);
            }
        }

        Preference userShare = findPreference(KEY_USER_SHARE);
        if (null != userShare) {
            userShare.setOnPreferenceClickListener(this);
        }
    }

    private boolean onWallpaperOrUserSharePreference(String key) {
        if (KEY_WALLPAPER.equalsIgnoreCase(key)) {
            Utils.showWallpapers(this);
            return true;
        } else if (KEY_USER_SHARE.equalsIgnoreCase(key)) {
            Utils.exportOrImportWallpaper(this);
            return true;
        } else {
            return false;
        }
    }

    private class BackToSceneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MarketUtils.ACTION_MARKET_THEME_APPLY.equals(action)
                    || MarketUtils.ACTION_BACK_TO_SCENE.equalsIgnoreCase(action)
                    || "com.borqs.se.intent.action.ADD_MODEL_TO_HOUSE".equals(action)) {
                try {
                    mHandler.post(new Runnable() {
                        public void run() {
                            finish();
                        }
                    });
                } catch (Exception e) {

                }
            }
        }

    }

    private BroadcastReceiver mReceiver;
    private void cancelListenBackToScene() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void listenBackToScene() {
        if (mReceiver == null) {
            mReceiver = new BackToSceneReceiver();
        }
        registerReceiver(mReceiver, new IntentFilter(MarketUtils.ACTION_MARKET_THEME_APPLY));
        registerReceiver(mReceiver, new IntentFilter(MarketUtils.ACTION_BACK_TO_SCENE));
        registerReceiver(mReceiver, new IntentFilter("com.borqs.se.intent.action.ADD_MODEL_TO_HOUSE"));
    }

    // / wallpaper and user share code end

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelListenBackToScene();
    }

    public static void setWallpaperId(Context context, String themeId, String wallpaperId) {
        setStringSettings(context, themeId, wallpaperId);
    }

    public static String getWallpaperId(Context context, String themeId) {
        return getStringSettings(context, themeId, "");
    }

    private static void setStringSettings(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getStringSettings(Context context, String key, String defValue) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        return settings.getString(key, defValue);
    }

   
    private static int getIntSettings(Context context, String key, int defValue) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        return settings.getInt(key, defValue);
    }


    private static void setIntSettings(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    
    private static final String PREF_KEY_PAPER_SIZE = "wallpaper_max_size";
    public static void setPreferredWallpaperSize(Context context, int value) {
        setIntSettings(context, PREF_KEY_PAPER_SIZE, value);
    }

    public static int getPreferredWallpaperSize(Context context, int defVal) {
        return getIntSettings(context, PREF_KEY_PAPER_SIZE, defVal);
    }

    
    
    
    
    
    // setting methods of wall shelf for desk object begin
    private static final String KEY_SHELF_LABEL = "show_shelf";
    private static final String KEY_SHELF_DESK_OBJECT = "show_shelf_for_desktop_object";
    private CheckBoxPreference mPrefShelf;
    private CheckBoxPreference mPrefDeskObjectShelf;

    private void initShelfSetting() {
        boolean showFlagA = getShowShelfSetting(this);
        mPrefShelf = (CheckBoxPreference) findPreference(KEY_SHELF_LABEL);
        if (null != mPrefShelf) {
            mPrefShelf.setOnPreferenceChangeListener(this);
            mPrefShelf.setChecked(showFlagA);
        }
        if (showFlagA) {
            setShowDeskObjectShelfSetting(true);
            HomeManager.getInstance().setShowAppShelf(true);
        }
        boolean showFlagB = getShowDeskObjectShelfSetting(this);
        mPrefDeskObjectShelf = (CheckBoxPreference) findPreference(KEY_SHELF_DESK_OBJECT);
        if (null != mPrefDeskObjectShelf) {
            mPrefDeskObjectShelf.setOnPreferenceChangeListener(this);
            mPrefDeskObjectShelf.setChecked(showFlagB);
            if (showFlagA) {
                mPrefDeskObjectShelf.setEnabled(false);
            } else {
                mPrefDeskObjectShelf.setEnabled(true);
            }
        }
    }

    public static boolean getShowDeskObjectShelfSetting(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_SHELF_DESK_OBJECT, true);

    }

    private void setShowDeskObjectShelfSetting(boolean state) {
        SharedPreferences settings = getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SHELF_DESK_OBJECT, state);
        editor.commit();
    }

    public static boolean getShowShelfSetting(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_SETTING_NAME, 0);
        return settings.getBoolean(KEY_SHELF_LABEL, false);

    }

    private void setShowShelfSetting(boolean state) {
        SharedPreferences settings = getSharedPreferences(PREFS_SETTING_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SHELF_LABEL, state);
        editor.commit();
        if (state) {
            if (mPrefDeskObjectShelf != null) {
                mPrefDeskObjectShelf.setChecked(true);
                setShowDeskObjectShelfSetting(true);
                mPrefDeskObjectShelf.setEnabled(false);
                HomeManager.getInstance().setShowDeskObjectShelf(true);
            }
        } else {
            if (mPrefDeskObjectShelf != null) {
                mPrefDeskObjectShelf.setEnabled(true);
            }
        }

    }

}
