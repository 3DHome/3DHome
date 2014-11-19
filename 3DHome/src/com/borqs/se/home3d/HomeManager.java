package com.borqs.se.home3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.se.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.borqsweather.weather.IWeatherService;
import com.borqs.se.download.WallPaperItem;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.HomeUtils.CropImageInfo;
import com.borqs.se.home3d.HomeUtils.SceneOrientation;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;
import com.borqs.se.shortcut.HomeAppWidgetHost;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.thumbnails.VideoThumbnailsService;

public class HomeManager {

    public static final int MSG_REMOVE_LOADING = 0;
    public static final int MSG_FINISH_LOADING = 1;
    private static final int APPWIDGET_HOST_ID = 2048;

    private static HomeManager mHomeManager;
    private WorkSpace mWorkSpace;
    private AppSearchPane mAppSearchPane;
    private TextView mFPSView;
    private HomeAppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
    private ModelObjectsManager mModelObjectsManager;
    private HomeScene mHomeScene;
    private ThemeInfo mCurrentThemeInfo;
    private SceneOrientation mCurSceneOrientation;
    public List<TimeChangeCallBack> mTimeCallBacks;
    public WeatherBindedCallBack mWeatherBindedCallBack;
    private boolean mHasStartVideoThumbTask = false;
    private boolean mWithoutAD = false;
    private int mRequestCode = 100;
    private int mAppIconBackgroundType = 0;
    private boolean mShowAppShef = false;
    private boolean mShowDeskObjectShef = true;
    private boolean mShowAllIndicator = false;
    private boolean mShowHelpDialog = false;

    public interface TimeChangeCallBack {
        public void onTimeChanged();
    }

    public interface WeatherBindedCallBack {
        public void onWeatherServiceBinded(IWeatherService service);
    }

    public interface ModelChangeCallBack {
        public void onAddModelToDB(ModelInfo modelInfo);

        public void onRemoveModelFromDB(ModelInfo modelInfo);
    }

    private HomeManager() {

    }

    public void init(Context context) {
        File file = new File(HomeUtils.get3DHomePath() + File.separator + ".debug");
        if (file.exists()) {
            HomeUtils.DEBUG = true;
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        SESceneManager.getInstance().initEngine(context);
        SESceneManager.getInstance().enableLight(true);
        SESceneManager.setDebug_JNI(HomeUtils.DEBUG);
        createOrUpgradeDB();
        LauncherModel.getInstance().loadAllData(false);
        mTimeCallBacks = new ArrayList<TimeChangeCallBack>();
        mAppWidgetManager = AppWidgetManager.getInstance(context);
        mAppIconBackgroundType = SettingsActivity.getAppIconBackgroundType(getContext());
        mShowAppShef = SettingsActivity.getShowShelfSetting(getContext());
        mShowDeskObjectShef = SettingsActivity.getShowDeskObjectShelfSetting(getContext());
        mShowAllIndicator = SettingsActivity.isEnableWallIndicator(getContext());
        mShowHelpDialog = !SettingsActivity.getHelpStatus(getContext());
        mCurSceneOrientation = SceneOrientation.AUTO_PORT;

    }

    public int getRequestCode() {
        mRequestCode++;
        if (mRequestCode > 0xFFFF) {
            mRequestCode = 100;
        }
        return mRequestCode;
    }

    public void setWithoutAD(boolean withoutAD) {
        mWithoutAD = withoutAD;
    }

    public boolean withoutAD() {
        return mWithoutAD;
    }

    public void debug(boolean enable) {
        HomeUtils.DEBUG = enable;
        File file = new File(HomeUtils.get3DHomePath() + File.separator + ".debug");
        if (enable) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Toast.makeText(HomeManager.getInstance().getContext(), "Open debug!!!", Toast.LENGTH_SHORT).show();
            SELoadResThread.getInstance().process(mDetectMemoryTask, 10000);
        } else {
            if (file.exists()) {
                file.delete();
            }
            Toast.makeText(HomeManager.getInstance().getContext(), "Close debug!!!", Toast.LENGTH_SHORT).show();
            SELoadResThread.getInstance().cancel(mDetectMemoryTask);
        }
        SESceneManager.setDebug_JNI(enable);
    }

    /**
     * 加载数据库并且启动场景
     */
    private void createOrUpgradeDB() {
        UpdateDBThread.getInstance().process(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = getContext().getContentResolver();
                Cursor cursor = resolver.query(ObjectInfoColumns.CONTENT_URI, null, "_id=1", null, null);
                if (cursor != null) {
                    cursor.close();
                }
                HomeUtils.loadDebugThemeAndModelFromSDCard(getContext());
                if (mModelObjectsManager == null) {
                    mModelObjectsManager = new ModelObjectsManager();
                    // 数据库错误,重启
                    if (mModelObjectsManager.initFailed()) {
                        Process.killProcess(Process.myPid());
                    }
                }
                HomeSceneInfo sceneInfo = new HomeSceneInfo();
                sceneInfo.setThemeInfo(getCurrentThemeInfo());
                if (HomeUtils.DEBUG) {
                    Log.d("SE3DAppManager:", "start3DScreen name :" + sceneInfo.mSceneName);
                }
                mHomeScene = new HomeScene(getContext(), sceneInfo.mSceneName);
                mHomeScene.setHomeSceneInfo(sceneInfo);
                SESceneManager.getInstance().setCurrentScene(mHomeScene);
                SESceneManager.getInstance().dataIsReady();
                HomeUtils.moveAssetToExternal(getContext());
            }
        });
    }

    public void setCurrentScene(HomeScene homeScene) {
        mHomeScene = homeScene;
    }

    public void startVideoThumbnailsService() {
        if (!mHasStartVideoThumbTask) {
            VideoThumbnailsService task = new VideoThumbnailsService();
            task.start(getContext());
            mHasStartVideoThumbTask = true;
        }
    }

    private Runnable mDetectMemoryTask = new Runnable() {
        public void run() {
            ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo outInfo = new MemoryInfo();
            activityManager.getMemoryInfo(outInfo);
            int myProcessID = Process.myPid();
            android.os.Debug.MemoryInfo[] myInfo = activityManager.getProcessMemoryInfo(new int[] { myProcessID });
            String value = "TotalPrivateDirty :" + (myInfo[0].getTotalPrivateDirty() / 1024f);
            Log.d(HomeUtils.TAG, "#### memory usage :" + value);
            SELoadResThread.getInstance().cancel(mDetectMemoryTask);
            SELoadResThread.getInstance().process(mDetectMemoryTask, 10000);
        }
    };

    public static HomeManager getInstance() {
        if (mHomeManager == null) {
            mHomeManager = new HomeManager();
        }
        return mHomeManager;
    }

    public void startLocalService() {
        getContext().startService(new Intent(getContext(), LocalService.class));
    }

    public void stopLocalService() {
        getContext().stopService(new Intent(getContext(), LocalService.class));
    }

    public void addTimeCallBack(final TimeChangeCallBack callBack) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (!mTimeCallBacks.contains(callBack)) {
                    mTimeCallBacks.add(callBack);
                    callBack.onTimeChanged();
                }
            }
        });
    }

    public void removeTimeCallBack(final TimeChangeCallBack callBack) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (callBack != null) {
                    mTimeCallBacks.remove(callBack);
                }
            }
        });
    }

    public HomeAppWidgetHost getAppWidgetHost() {
        if (mAppWidgetHost == null) {
            mAppWidgetHost = new HomeAppWidgetHost(getContext(), APPWIDGET_HOST_ID);
            // 在应用启动的时候立马启动AppWidgetHost监听，用可能出现检测不到应用程序的进程号的情况，从而抛出异常
            mAppWidgetHost.startListening();
        }
        return mAppWidgetHost;
    }

    public AppWidgetManager getAppWidgetManager() {
        return mAppWidgetManager;
    }

    public void setScaleListener(ScaleGestureDetector.OnScaleGestureListener l) {
        mWorkSpace.setScaleListener(l);
    }

    public void setWorkSpace(WorkSpace workSpace) {
        mWorkSpace = workSpace;
    }

    public void setAppSearchPane(AppSearchPane appSearchPane) {
        mAppSearchPane = appSearchPane;
    }

    public WorkSpace getWorkSpace() {
        return mWorkSpace;
    }

    public AppSearchPane getAppSearchPane() {
        return mAppSearchPane;
    }

    public void clearFPSView() {
        if (mFPSView != null) {
            mWorkSpace.removeView(mFPSView);
            SESceneManager.getInstance().clearFPSView();
            mFPSView = null;
        }
    }

    public void showFPSView() {
        if (mFPSView == null) {
            mFPSView = new TextView(getContext());
            mFPSView.setTextColor(Color.RED);
            mWorkSpace.addView(mFPSView);
            SESceneManager.getInstance().setFPSView(mFPSView);
        }
    }

    public Context getContext() {
        return HomeApplication.getInstance().getApplicationContext();
    }

    private Intent mIntent;

    /**
     * 防止在短时间内快速点击Icon，导致应用被启动多次
     */
    private void clearStartActivityIntent() {
        mIntent = null;
    }

    public boolean startActivityForResult(final Intent intent, final int requestCode) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                try {
                    SESceneManager.getInstance().getGLActivity().startActivityForResult(intent, requestCode);
                } catch (Exception e) {

                }
            }
        });
        return true;
    }

    public boolean startActivity(final Intent intent) {
        mIntent = intent;
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (mIntent != null) {
                    try {
                        getContext().startActivity(mIntent);
                    } catch (Exception e) {

                    }
                }
            }
        });
        return true;
    }

    public HomeScene getHomeScene() {
        return mHomeScene;
    }

    public void onActivityResume() {
        checkSceneRotation(getCurrentThemeInfo());
        clearStartActivityIntent();
        for (TimeChangeCallBack callBack : mTimeCallBacks) {
            if (callBack != null) {
                callBack.onTimeChanged();
            }
        }
    }

    private void checkSceneRotation(ThemeInfo themeInfo) {
        if (null == themeInfo) {
            return;
        }

        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (themeInfo.mThemeName.equals("landscape")) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        if (orientation != getRequestedOrientation()) {
            setRequestedOrientation(orientation);
        }
    }

    private void setRequestedOrientation(int requestedOrientation) {
        if (SESceneManager.getInstance().getGLActivity() != null) {
            SESceneManager.getInstance().getGLActivity().setRequestedOrientation(requestedOrientation);
        }
    }

    private int getRequestedOrientation() {
        if (SESceneManager.getInstance().getGLActivity() == null) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return SESceneManager.getInstance().getGLActivity().getRequestedOrientation();
    }

    private IWeatherService mWeatherService;

    public void bindWeatherService() {
        if (mWeatherService == null) {
            Context context = getContext();
            Intent intent = new Intent("com.borqs.borqsweather.weatherservice");
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindWeatherService() {
        getContext().unbindService(mConnection);
    }

    public void setWeatherBindedCallBack(WeatherBindedCallBack callBack) {
        mWeatherBindedCallBack = callBack;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            if (service == null) {
                return;
            }

            mWeatherService = IWeatherService.Stub.asInterface(service);
            if (mWeatherBindedCallBack != null) {
                mWeatherBindedCallBack.onWeatherServiceBinded(mWeatherService);
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mWeatherService = null;
            if (mWeatherBindedCallBack != null) {
                mWeatherBindedCallBack.onWeatherServiceBinded(null);
            }
        }
    };

    public IWeatherService getWeatherService() {
        return mWeatherService;
    }

    public ModelObjectsManager getModelManager() {
        return mModelObjectsManager;
    }

    public void addModelToScene(ModelInfo modelInfo) {
        if (mModelObjectsManager != null) {
            mModelObjectsManager.mModels.put(modelInfo.mName, modelInfo);
        }
    }

    public void removeModelFromScene(String name) {
        if (mModelObjectsManager != null) {
            ModelInfo modelInfo = mModelObjectsManager.mModels.remove(name);
            if (modelInfo == null) {
                return;
            }
            ModelChangeCallBack modelChangeCallBack = (ModelChangeCallBack) mHomeScene;
            if (modelChangeCallBack != null) {
                modelChangeCallBack.onRemoveModelFromDB(modelInfo);
            }
        }

    }

    public void addModelToScene(final String resourcePath, final boolean update) {

        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                String name = null;
                String where = ModelColumns.ASSETS_PATH + "='" + resourcePath + "'";
                Cursor cursor = getContext().getContentResolver().query(ModelColumns.CONTENT_URI, null, where, null,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.OBJECT_NAME));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (update) {
                    getContext().getContentResolver().delete(ModelColumns.CONTENT_URI, where, null);
                    if (name != null) {
                        removeModelFromScene(name);
                    }
                }
                ModelInfo modelInfo = null;
                if (name != null) {
                    modelInfo = mModelObjectsManager.findModelInfo(name);
                }
                if (modelInfo == null) {
                    modelInfo = HomeUtils.getModelInfo(resourcePath);
                    addModelToScene(modelInfo);
                    modelInfo.saveToDB();
                }
                if (modelInfo != null) {
                    ModelChangeCallBack modelChangeCallBack = (ModelChangeCallBack) mHomeScene;
                    if (modelChangeCallBack != null) {
                        modelChangeCallBack.onAddModelToDB(modelInfo);
                    }
                }
            }
        });
    }

    public void changeTheme(ThemeInfo themeInfo) {
        if (mCurrentThemeInfo != null && mModelObjectsManager != null) {
            mCurrentThemeInfo = themeInfo;
            mCurrentThemeInfo.initFromXML(getContext());
            HomeUtils.staticUsingTheme(getContext(), themeInfo.mThemeName);
            SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SCENE);
            SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SCENE, themeInfo);
            checkSceneRotation(themeInfo);
        }
    }

    public ThemeInfo getCurrentThemeInfo() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (mCurrentThemeInfo == null) {
            String where = ThemeColumns.IS_APPLY + "=" + 1;
            Cursor cursor = contentResolver.query(ThemeColumns.CONTENT_URI, null, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentThemeInfo = ThemeInfo.CreateFromDB(cursor);
                }
                cursor.close();
            }
            if (mCurrentThemeInfo == null) {
                HomeUtils.markThemeAsApply(getContext(), HomeDataBaseHelper.getInstance(getContext())
                        .getDefaultThemeID());
                cursor = contentResolver.query(ThemeColumns.CONTENT_URI, null, where, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mCurrentThemeInfo = ThemeInfo.CreateFromDB(cursor);
                    }
                    cursor.close();
                }
            }
            if (mCurrentThemeInfo == null) {
                // 数据库错误，删除数据库
                HomeUtils.deleteFile(getContext().getDatabasePath(ProviderUtils.DATABASE_NAME).getParent());
                Process.killProcess(Process.myPid());
            }
        }
        mCurrentThemeInfo.initFromXML(getContext());
        return mCurrentThemeInfo;
    }

    public void setSceneOrientation(SceneOrientation sceneOrientation) {
        if (mCurSceneOrientation != SceneOrientation.ONLY_LAND && mCurSceneOrientation != SceneOrientation.ONLY_PORT) {
            mCurSceneOrientation = sceneOrientation;
        }
    }

    public SceneOrientation getCurrentSceneOrientation() {
        return mCurSceneOrientation;
    }

    public boolean isLandscapeOrientation() {
        return (mCurSceneOrientation == SceneOrientation.AUTO_LAND || mCurSceneOrientation == SceneOrientation.ONLY_LAND);
    }

    public int getAppIconBackgroundType() {
        return mAppIconBackgroundType;
    }

    public void setAppIconBackgroundType(int type) {
        mAppIconBackgroundType = type;
    }

    public boolean getWhetherShowAppShelf() {
        return mShowAppShef;
    }

    public void setShowAppShelf(boolean show) {
        mShowAppShef = show;
    }

    public boolean getWhetherShowDeskObjectShelf() {
        return mShowDeskObjectShef;
    }

    public void setShowDeskObjectShelf(boolean show) {
        mShowDeskObjectShef = show;
    }

    public boolean getWhetherShowWallIndicator() {
        return mShowAllIndicator;
    }

    public void setShowWallIndicator(boolean show) {
        mShowAllIndicator = show;
    }

    public boolean getWhetherShowHelpDialog() {
        return mShowHelpDialog;
    }

    public void setShowHelpDialog(boolean show) {
        mShowHelpDialog = show;
    }

    /******************** for wall paper ************************************/
    public static void applyWallpaperFromMarket(ArrayList<WallPaperItem> wallpapers, String paperId) {
        if (null != mHomeManager) {
            HomeScene scene = mHomeManager.getHomeScene();
            if (null != scene) {
                scene.applyWallpaperFromMarket(wallpapers, paperId);
            }
        }
    }

    public static ArrayList<RawPaperItem> exportWallPaperToMarket() {
        if (null != mHomeManager) {
            HomeScene scene = mHomeManager.getHomeScene();
            if (null != scene) {
                return scene.exportWallPaperToMarket();
            }
        }
        return new ArrayList<RawPaperItem>();
    }

    public static int getWallpaperMaxSize(Activity context) {
        int result = SettingsActivity.getPreferredWallpaperSize(context, -1);

        if (result < 0) {
            result = context.getResources().getInteger(R.integer.wallpaper_max_size);
            int sizeByResolution = getPaperSizeByResolution(context);
            if (result < sizeByResolution) {
                result = sizeByResolution;
            }
            SettingsActivity.setPreferredWallpaperSize(context, result);
        }

        if (HomeUtils.DEBUG) {
            Log.v(HomeUtils.TAG, "getWallpaperMaxSize maxSize = " + result + ", result =" + result);
        }
        return result;
    }

    private static final int PAPER_LOW_SIZE = 512;
    private static final int PAPER_HD_SIZE = 1024;
    private static final int PAPER_TV_HIGH_SIZE = 2048;

    private static final int PAPER_SIZE_THRESHOLD = 1080;
    private static final int PAPER_SIZE_THRESHOLD_TV = 2160;

    private static int getPaperSizeByResolution(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        final int minPixels = Math.min(metric.widthPixels, metric.heightPixels);
        if (minPixels < PAPER_SIZE_THRESHOLD) {
            return PAPER_LOW_SIZE;
        } else if (minPixels >= PAPER_SIZE_THRESHOLD_TV) {
            return PAPER_TV_HIGH_SIZE;
        }
        return PAPER_HD_SIZE;
    }
}
