package com.borqs.se.home3d;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.borqs.borqsweather.weather.IWeatherService;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.se.R;
import com.borqs.se.addobject.AddAppDialog;
import com.borqs.se.addobject.AddAppDialog.OnAppSelectedListener;
import com.borqs.se.addobject.AddFolderDialog;
import com.borqs.se.addobject.AddFolderDialog.OnFolderCreatedListener;
import com.borqs.se.addobject.AddObjectComponentNameExtend;
import com.borqs.se.addobject.AddObjectItemInfo;
import com.borqs.se.addobject.AddRemoveAppDialog;
import com.borqs.se.download.Utils;
import com.borqs.se.download.WallPaperItem;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEEmptyAnimation;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector2i;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager.ModelChangeCallBack;
import com.borqs.se.home3d.HomeUtils.CropImageInfo;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.VesselColumns;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.LoadAppFinishedListener;
import com.borqs.se.widget3d.ADViewController;
import com.borqs.se.widget3d.AppObject;
import com.borqs.se.widget3d.Cloud;
import com.borqs.se.widget3d.Desk;
import com.borqs.se.widget3d.DragLayer;
import com.borqs.se.widget3d.Folder;
import com.borqs.se.widget3d.House;
import com.borqs.se.widget3d.NormalObject;
import com.borqs.se.widget3d.ObjectInfo;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;
import com.borqs.se.widget3d.VesselLayer;
import com.borqs.se.widget3d.VesselObject;
import com.borqs.se.widget3d.WallCellLayer;
import com.borqs.se.widget3d.WallLayer;

import java.util.ArrayList;
import java.util.List;

public class HomeScene extends SEScene implements ModelChangeCallBack, OnAppSelectedListener,
        OnFolderCreatedListener, MarketUtils.OnDragListener {

    public static final String TAG = "HomeScene";

    public static final int STATUS_APP_MENU = 0x00000001;
    public static final int STATUS_HELPER_MENU = STATUS_APP_MENU << 1;
    public static final int STATUS_MOVE_OBJECT = STATUS_HELPER_MENU << 1;
    public static final int STATUS_ON_DESK_SIGHT = STATUS_MOVE_OBJECT << 1;
    public static final int STATUS_ON_SKY_SIGHT = STATUS_ON_DESK_SIGHT << 1;
    public static final int STATUS_ON_WIDGET_SIGHT = STATUS_ON_SKY_SIGHT << 1;
    public static final int STATUS_ON_SCALL = STATUS_ON_WIDGET_SIGHT << 1;
    public static final int STATUS_ON_WIDGET_TOUCH = STATUS_ON_SCALL << 1;
    public static final int STATUS_ON_WIDGET_RESIZE = STATUS_ON_WIDGET_TOUCH << 1;

    public static final int REQUEST_CODE_SELECT_WIDGET = 0;
    public static final int REQUEST_CODE_SELECT_SHORTCUT = REQUEST_CODE_SELECT_WIDGET + 1;
    public static final int REQUEST_CODE_BIND_WIDGET = REQUEST_CODE_SELECT_SHORTCUT + 1;
    public static final int REQUEST_CODE_SELECT_WALLPAPER_IMAGE = REQUEST_CODE_BIND_WIDGET + 1;
    public static final int REQUEST_CODE_SELECT_WALLPAPER_CAMERA = REQUEST_CODE_SELECT_WALLPAPER_IMAGE + 1;
    public static final int REQUEST_CODE_BIND_SHORTCUT = REQUEST_CODE_SELECT_WALLPAPER_CAMERA + 1;
    public static final int REQUESTCODE_APPOPTION_GETIMAGE = REQUEST_CODE_BIND_SHORTCUT + 1;
    public static final int REQUESTCODE_APPOPTION_GETIMAGE2 = REQUESTCODE_APPOPTION_GETIMAGE + 1;

    public static final int MSG_TYPE_SHOW_BIND_APP_DIALOG = 0;
    public static final int MSG_TYPE_SHOW_OBJECT_VIEW = MSG_TYPE_SHOW_BIND_APP_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_APP_VIEW = MSG_TYPE_SHOW_OBJECT_VIEW + 1;
    public static final int MSG_TYPE_SHOW_DELETE_OBJECTS = MSG_TYPE_SHOW_APP_VIEW + 1;
    public static final int MSG_TYPE_SHOW_SELECT_WALLPAPER_DIALOG = MSG_TYPE_SHOW_DELETE_OBJECTS + 1;
    public static final int MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG = MSG_TYPE_SHOW_SELECT_WALLPAPER_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_OBJECT_LONG_CLICK_DIALOG = MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_OPTION_MENU_DIALOG = MSG_TYPE_SHOW_OBJECT_LONG_CLICK_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_CAMERA_ADJUST_DIALOG = MSG_TYPE_SHOW_OPTION_MENU_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_WEATHER_DIALOG = MSG_TYPE_SHOW_CAMERA_ADJUST_DIALOG + 1;
    public static final int MSG_TYPE_DISMIS_WEATHER_DIALOG = MSG_TYPE_SHOW_WEATHER_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_EDITFOLDER_DIALOG = MSG_TYPE_DISMIS_WEATHER_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_CREATEFOLDER_DIALOG = MSG_TYPE_SHOW_EDITFOLDER_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_ADDAPP_DIALOG = MSG_TYPE_SHOW_CREATEFOLDER_DIALOG + 1;
    public static final int MSG_TYPE_SHOW_CHANGE_COLOR_DIALOG = MSG_TYPE_SHOW_ADDAPP_DIALOG + 1;
    public static final int MSG_TYPE_ADD_REMOVE_APP_DIALOG = MSG_TYPE_SHOW_CHANGE_COLOR_DIALOG + 1;
    public static final int MSG_TYPE_UPDATE_SCENE = MSG_TYPE_ADD_REMOVE_APP_DIALOG + 1;
    public static final int MSG_TYPE_UPDATE_SHELF = MSG_TYPE_UPDATE_SCENE + 1;

    private static final int DIALOG_DELETE_OBJECTS = 1;
    private static final int DIALOG_SELECT_WALLPAPER = DIALOG_DELETE_OBJECTS + 1;
    private static final int DIALOG_WALL_LONG_CLICK = DIALOG_SELECT_WALLPAPER + 1;
    private static final int DIALOG_OBJECT_LONG_CLICK = DIALOG_WALL_LONG_CLICK + 1;
    public static final int DIALOG_OBJECT_CHANGE_LABLE = DIALOG_OBJECT_LONG_CLICK + 1;
    public static final int DIALOG_OBJECT_CHANGE_ICON = DIALOG_OBJECT_CHANGE_LABLE + 1;
    private static final int DIALOG_HELPER = DIALOG_OBJECT_CHANGE_ICON + 1;
    public static final int DIALOG_ADJUST_CAMERA = DIALOG_HELPER + 1;
    private static final int DIALOG_WEATHER = DIALOG_ADJUST_CAMERA + 1;
    private static final int DIALOG_WALL_LONG_CLICK_ADD_APP = DIALOG_WEATHER + 1;
    private static final int DIALOG_WALL_LONG_CLICK_ADD_FOLDER = DIALOG_WALL_LONG_CLICK_ADD_APP + 1;
    private static final int DIALOG_EDITFOLDER = DIALOG_WALL_LONG_CLICK_ADD_FOLDER + 1;
    private static final int DIALOG_CHANGE_COLOR = DIALOG_EDITFOLDER + 1;
    private static final int DIALOG_WALL_INDICATOR = DIALOG_CHANGE_COLOR + 1;
    private static final int DIALOG_ADD_REMOVE_APP = DIALOG_WALL_INDICATOR + 1;
    private ApplicationMenu mWidgetsPreview;
    private NormalObject mSetBindObject;
    private CropImageInfo mCropImageInfo;
    private int mMenuCheckStatus;
    private boolean mLoadCompleted;
    private String mDeleteObjName;
    private NormalObject mSelectedObject;
    private OptionMenu mOptionMenu;
    private IWeatherService mWeatherSevice;
    private Folder mEditFolder;
//    private ArrayList<AppItemInfo> mPrefaceApps;
    public static final String MSG_CONTENT_IMAGE = "image";
    public static final String MSG_CONTENT_IMAGE_SIZE_X = "imgSizeX";
    public static final String MSG_CONTENT_IMAGE_SIZE_Y = "imgSizeY";
    public static final String MSG_CONTENT_IMAGE_OUTPUT = "output";
    private DragLayer mDraglayer;
    private HomeSceneInfo mSceneInfo;
    private boolean mShowWallIndicator;
    public int mStatus = 0;
    private boolean mIsLandScape;
    /**
     * 场景结构：
     *                          场景
     *                           |
     *                         根节点(NodeRoot)
     *                ___________|________________
     *               |                            |
     *              桌子(Desk)                  房间节点(House)
     *      _________|_____           ____________|______
     *     |      |        |         |        |          | 
     *    相机...电话   桌子_model    Wall0...wall7节点  ground节点
     *                  ___|___       ___|___            |
     *                 |       |     |       |          音像
     *                桌子腿...桌面   电视 ... 挂钟
     */
    public HomeScene(Context context, String sceneName) {
        super(context, sceneName);
        mMenuCheckStatus = STATUS_APP_MENU + STATUS_HELPER_MENU + STATUS_ON_WIDGET_SIGHT
                + STATUS_MOVE_OBJECT + STATUS_ON_WIDGET_RESIZE;
        mShowWallIndicator = HomeManager.getInstance().getWhetherShowWallIndicator();
        mDraglayer = new DragLayer(this);
    }

    @Override
    public void onSceneStart() {
        mLoadCompleted = false;
        mIsLandScape = HomeManager.getInstance().isLandscapeOrientation();
        ADViewController adController = ADViewController.getInstance();
        adController.setHomeScene(this);
        getCamera().addCameraChangedListener(adController);
        MarketUtils.setOnDragListener(this);
        initOptionMenu();
        LauncherModel.getInstance().setLoadAppFinishedListener(new LoadAppFinishedListener() {
            public void onFinished() {
                new SECommand(HomeScene.this) {
                    public void run() {
                        loading();
                    }
                }.execute();

            }
        });
    }

    public void setStatus(int type, boolean status) {
        if (status) {
            mStatus |= type;
        } else {
            mStatus &= ~type;
        }
    }

    public boolean getStatus(int type) {
        return (mStatus & type) != 0;
    }

    public int getStatus() {
        return mStatus;
    }

    public DragLayer getDragLayer() {
        return mDraglayer;
    }

    public void setHomeSceneInfo(HomeSceneInfo sceneInfo) {
        mSceneInfo = sceneInfo;
    }

    public HomeSceneInfo getHomeSceneInfo() {
        return mSceneInfo;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event) || mDraglayer.dispatchTouchEvent(event);
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        return super.handleBackKey(l);
    }

    @Override
    public void notifySurfaceChanged(int width, int height) {
        if (mIsLandScape != HomeManager.getInstance().isLandscapeOrientation()) {
            mIsLandScape = !mIsLandScape;
            ADViewController.getInstance().notifyFinish();
            changeSight(0, true);
        }
        mSceneInfo.notifySurfaceChanged(width, height);
        super.notifySurfaceChanged(width, height);
    }

    @Override
    public void onRelease() {
        super.onRelease();
    }

    @Override
    public void onActivityRestart() {
        super.onActivityRestart();
        if (mOptionMenu != null) {
            mOptionMenu = null;
            initOptionMenu();
        }
    }

    private void loading() {
        ThemeInfo themeInfo = HomeManager.getInstance().getCurrentThemeInfo();
        HomeUtils.updateHouseName(getContext(), themeInfo);        
        ObjectInfo rootVesselInfo = findRootVessel();
        if (rootVesselInfo == null) {
            return;
        }
        final NormalObject root = rootVesselInfo.CreateNormalObject(this);
        setContentObject(root);// set root object of scene
        HomeManager.getInstance().getModelManager().loadPreLoadModel(this, new Runnable() {
            @Override
            public void run() {
                root.loadAll(null, new Runnable() {
                    public void run() {
                        HomeManager.getInstance().getModelManager().loadAfterLoadModel(HomeScene.this, new Runnable() {
                            public void run() {
                                createAndLoadMenu();
                                mLoadCompleted = true;
                            }
                        });
                    }
                });
            }
        });
    }

    public boolean hasLoadCompleted() {
        return mLoadCompleted;
    }

    private ObjectInfo findRootVessel() {
        String where = ObjectInfoColumns.SCENE_NAME + "='" + getSceneName() + "' AND " + VesselColumns.VESSEL_ID
                + "=-1";
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = resolver.query(ObjectInfoColumns.OBJECT_LEFT_JOIN_ALL, null, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ObjectInfo objectInfo = ObjectInfo.CreateFromDB(cursor);
                cursor.close();
                return objectInfo;
            }
            cursor.close();
        }
        return null;
    }

    private void createAndLoadMenu() {
        initApplicationMenu();
        if (HomeManager.getInstance().getWhetherShowHelpDialog()) {
            showDialog(DIALOG_HELPER);
            HomeManager.getInstance().setShowHelpDialog(false);
            SettingsActivity.setHelpStatus(getContext(), true);
        }
    }

    @Override
    public void handleMenuKey() {
        if (mOptionMenu != null) {
            if ((mMenuCheckStatus & getStatus()) == 0) {
                handleMessage(HomeScene.MSG_TYPE_SHOW_OPTION_MENU_DIALOG, null);
            }
        }
    }

    private DialogFragment mObjectsListDialog;

    @Override
    public void handleMessage(int type, Object message) {
        switch (type) {
        case MSG_TYPE_SHOW_OBJECT_VIEW:
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    FragmentActivity FragmentActivity = (android.support.v4.app.FragmentActivity) SESceneManager
                            .getInstance().getGLActivity();
                    mObjectsListDialog = MarketUtils.showProductsDialog(FragmentActivity.getSupportFragmentManager(),
                            ProductType.OBJECT, null);
                }
            });
            break;
        case MSG_TYPE_SHOW_APP_VIEW:
            if (mLoadCompleted) {
                int showType = (Integer) message;
                if (mWidgetsPreview != null) {
                    mWidgetsPreview.show(showType, null);
                }
            }
            break;
        case MSG_TYPE_SHOW_BIND_APP_DIALOG:
            if (message instanceof NormalObject) {
                mSetBindObject = (NormalObject) message;
                if (mWidgetsPreview != null) {
                    mWidgetsPreview.show(ApplicationMenu.TYPE_BIND_APP, mSetBindObject);
                }
            }
            break;
        case MSG_TYPE_SHOW_DELETE_OBJECTS:
            mDeleteObjName = (String) message;
            showDialog(DIALOG_DELETE_OBJECTS);
            break;
        case MSG_TYPE_SHOW_SELECT_WALLPAPER_DIALOG:
            if (message != null) {
                mCropImageInfo = (CropImageInfo) message;
            }
            if (mLoadCompleted) {
                showDialog(DIALOG_SELECT_WALLPAPER);
            }
            break;
        case MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG:
            if (message != null) {
                mCropImageInfo = (CropImageInfo) message;
            } else if (mHouse != null) {
                mCropImageInfo = mHouse.getCropImageInfo();
            }
            if (mLoadCompleted) {
                showDialog(DIALOG_WALL_LONG_CLICK);
            }
            break;

        case MSG_TYPE_SHOW_OBJECT_LONG_CLICK_DIALOG:
            if (message instanceof NormalObject) {
                mSelectedObject = (NormalObject) message;
                if (mSelectedObject != null) {
                    Bundle bundle = setObjectLongClickDialogContent();
                    showDialog(DIALOG_OBJECT_LONG_CLICK, bundle);
                }
            }
            break;
        case MSG_TYPE_SHOW_OPTION_MENU_DIALOG:
            showOptionMenu();
            break;
        case MSG_TYPE_UPDATE_SCENE:
            onSceneChanged();
            break;
        case MSG_TYPE_SHOW_CAMERA_ADJUST_DIALOG:
            moveToWallSight(new SEAnimFinishListener() {
                @Override
                public void onAnimationfinish() {
                    showDialog(DIALOG_ADJUST_CAMERA);
                }
            });
            break;
        case MSG_TYPE_SHOW_WEATHER_DIALOG:
            if (message instanceof IWeatherService) {
                mWeatherSevice = (IWeatherService) message;
                removeDialog(DIALOG_WEATHER);
                showDialog(DIALOG_WEATHER);
            }
            break;
        case MSG_TYPE_DISMIS_WEATHER_DIALOG:
            dismissDialog(DIALOG_WEATHER);
            break;
        case MSG_TYPE_SHOW_ADDAPP_DIALOG:
            removeDialog(DIALOG_WALL_LONG_CLICK_ADD_APP);
            showDialog(DIALOG_WALL_LONG_CLICK_ADD_APP);
            break;
        case MSG_TYPE_SHOW_CREATEFOLDER_DIALOG:
            removeDialog(DIALOG_WALL_LONG_CLICK_ADD_FOLDER);
            showDialog(DIALOG_WALL_LONG_CLICK_ADD_FOLDER);
            break;
        case MSG_TYPE_SHOW_EDITFOLDER_DIALOG:
            removeDialog(DIALOG_EDITFOLDER);
            mEditFolder = (Folder) message;
            showDialog(DIALOG_EDITFOLDER);
            break;
        case MSG_TYPE_SHOW_CHANGE_COLOR_DIALOG:
            showDialog(DIALOG_CHANGE_COLOR);
            break;
        case MSG_TYPE_ADD_REMOVE_APP_DIALOG:
            removeDialog(DIALOG_ADD_REMOVE_APP);
            showDialog(DIALOG_ADD_REMOVE_APP);
            break;
        case MSG_TYPE_UPDATE_SHELF:
            updateShelfWall();
            break;
        }
    }

    private void showOptionMenu() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                mOptionMenu.showAtLocation(HomeManager.getInstance().getWorkSpace(), Gravity.BOTTOM, 0, 0);
                mOptionMenu.playAnimation();
            }
        });

    }

    private void initApplicationMenu() {
        mWidgetsPreview = new ApplicationMenu(HomeScene.this, "WidgetsPreview");
        getContentObject().addChild(mWidgetsPreview, true);
    }

    private void initOptionMenu() {
        if (mOptionMenu == null) {
            mOptionMenu = new OptionMenu(getContext());
        }
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        mShowWallIndicator = HomeManager.getInstance().getWhetherShowWallIndicator();
        if (!mShowWallIndicator) {
            removeDialog(DIALOG_WALL_INDICATOR);
        }
        if (HomeManager.getInstance().getWhetherShowHelpDialog()) {
            showDialog(DIALOG_HELPER);
            HomeManager.getInstance().setShowHelpDialog(false);
            SettingsActivity.setHelpStatus(getContext(), true);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!handleBackKey(null)) {
            if (mObjectsListDialog != null && mObjectsListDialog.isVisible()) {
                try {
                    mObjectsListDialog.dismiss();
                } catch (Exception e) {

                }
                return;
            }
            if (getContentObject() != null) {
                getContentObject().onPressHomeKey();
            }
            if (getCamera() != null) {
                moveToWallSight(new SEAnimFinishListener() {

                    public void onAnimationfinish() {
                        SEVector3f loc = getHomeSceneInfo().getThemeInfo().mCurrentCameraLoction.clone();
                        float endRadius = -loc.getY();
                        float endFov = getHomeSceneInfo().getThemeInfo().mCurrentCameraFov;
                        playSetRadiusAndFovAnim(endRadius, endFov, null);

                    }
                });
            }
        }
        dismissAllDialog();
    }

    private boolean mHadInitedDialog = false;

    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DIALOG_DELETE_OBJECTS:
            return createDeleteDialog();
        case DIALOG_SELECT_WALLPAPER:
            return createWallPaperDialog();
        case DIALOG_WALL_LONG_CLICK:
            return createWallDialog();
        case DIALOG_OBJECT_LONG_CLICK:
            return createObjectDialog();
        case DIALOG_OBJECT_CHANGE_LABLE:
            return createChangeLableDialog();
        case DIALOG_OBJECT_CHANGE_ICON:
            return createChangeIconDialog();
        case DIALOG_HELPER:
            return createHelperDialog();
        case DIALOG_ADJUST_CAMERA:
            return createAdjustCameraDialog();
        case DIALOG_WEATHER:
            return createWeatherDialog();
        case DIALOG_WALL_LONG_CLICK_ADD_APP:
            return createWallDialogAddApp();
        case DIALOG_WALL_LONG_CLICK_ADD_FOLDER:
            return createWallDialogAddFolder();
        case DIALOG_EDITFOLDER:
            return createEditFolderDialog(mEditFolder);
        case DIALOG_CHANGE_COLOR:
            return createChangeColorDialog();
        case DIALOG_WALL_INDICATOR:
            return createWallIndicatorDialog();
        case DIALOG_ADD_REMOVE_APP:
            return createAddRemoveAppDialog();
        }
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        if (mHadInitedDialog) {
            mHadInitedDialog = false;
            return;
        }
        switch (id) {
        case DIALOG_DELETE_OBJECTS:
            AlertDialog deleteDialog = (AlertDialog) dialog;
            String message = getContext().getResources().getString(R.string.confirm_delete_selected_objects,
                    mDeleteObjName);
            deleteDialog.setMessage(message);
            break;
        case DIALOG_OBJECT_LONG_CLICK:
            prepareObjectDialog(dialog, bundle);
            break;
        case DIALOG_OBJECT_CHANGE_LABLE:
            prepareChangeLabelDialog(dialog);
            break;
        case DIALOG_OBJECT_CHANGE_ICON:
            prepareChangeIconDialog(dialog);
            break;
        case DIALOG_CHANGE_COLOR:
            prepareChangeColorDialog(dialog);
            break;
        case DIALOG_ADD_REMOVE_APP:
            AddRemoveAppDialog addRemoveAppDialog = (AddRemoveAppDialog) dialog;
            addRemoveAppDialog.setOnAddRemoveListener(mWidgetsPreview);
            break;
        }
    }

    private Dialog createDeleteDialog() {
        Activity activity = SESceneManager.getInstance().getGLActivity();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = mDeleteObjName;
                String productId = HomeUtils.deleteModelAndObjectDBByName(getContext(), name);
                if (productId != null) {
                    MarketUtils.deletePlugIn(getContext(), productId);
                }
                ModelInfo modelInfo = HomeManager.getInstance().getModelManager().mModels.remove(name);
                if (modelInfo != null) {
                    onRemoveModelFromDB(modelInfo);
                }
            }
        };
        AlertDialog dialog = new AlertDialog.Builder(activity).setIcon(R.drawable.art_dialog_notice)
                .setPositiveButton(android.R.string.yes, listener).setNegativeButton(android.R.string.no, null)
                .create();
        String message = activity.getResources().getString(R.string.confirm_delete_selected_objects, mDeleteObjName);
        String title = activity.getResources().getString(R.string.delete_objects_title);
        dialog.setTitle(title);
        dialog.setMessage(message);
        return dialog;
    }

    private Dialog createWeatherDialog() {
        if (mWeatherSevice == null) {
            return null;
        }
        final WeatherDialog dialog = new WeatherDialog(SESceneManager.getInstance().getGLActivity(), mWeatherSevice);
        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    moveToWallSight(null);
                    dialog.dismiss();
                    break;
                }
                return true;
            }

        });
        return dialog;
    }

    /**/
    private Dialog createWallDialog() {
        return new LongPressWallDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createWallDialogAddApp() {
        if (getEmptySlotNumOfCurrentWall() <= 0) {
            Toast.makeText(getContext(), R.string.addobject_select_to_add_app_no_room, Toast.LENGTH_SHORT).show();
            return null;
        }
        ArrayList<AppItemInfo> apps = HomeUtils.getSortedAppsByName();
        ArrayList<AppItemInfo> appsShown = new ArrayList<AppItemInfo>();
        SharedPreferences pref = HomeManager.getInstance().getContext()
                .getSharedPreferences("add_or_hidde_app", Context.MODE_MULTI_PROCESS);
        for (AppItemInfo app : apps) {
            String componetName = app.getComponentName().toString();
            boolean isShow = true;
            isShow = pref.getBoolean(componetName, true);
            if (isShow) {
                appsShown.add(app);
            }
        }
        return (new AddAppDialog(SESceneManager.getInstance().getGLActivity(), SESceneManager.getInstance()
                .getGLActivity().getLayoutInflater(), appsShown, this, getEmptySlotNumOfCurrentWall()));
    }

    private Dialog createWallDialogAddFolder() {
        if (getEmptySlotNumOfCurrentWall() <= 0) {
            Toast.makeText(getContext(), R.string.addobject_select_to_add_app_no_room, Toast.LENGTH_SHORT).show();
            return null;
        }
        ArrayList<AppItemInfo> apps = HomeUtils.getSortedAppsByName();
        ArrayList<AppItemInfo> appsShown = new ArrayList<AppItemInfo>();
        SharedPreferences pref = HomeManager.getInstance().getContext()
                .getSharedPreferences("add_or_hidde_app", Context.MODE_MULTI_PROCESS);
        for (AppItemInfo app : apps) {
            String componetName = app.getComponentName().toString();
            boolean isShow = true;
            isShow = pref.getBoolean(componetName, true);
            if (isShow) {
                appsShown.add(app);
            }
        }
        return (new AddFolderDialog(SESceneManager.getInstance().getGLActivity(), SESceneManager.getInstance()
                .getGLActivity().getLayoutInflater(), appsShown, this, getEmptySlotNumOfCurrentWall(),
                getChildMaxNumInFolder()));
    }

    private Dialog createEditFolderDialog(Folder folder) {
        if (folder == null) {
            return null;
        }
        ArrayList<AppItemInfo> apps = HomeUtils.getSortedAppsByName();
        ArrayList<AppItemInfo> appsShown = new ArrayList<AppItemInfo>();
        SharedPreferences pref = HomeManager.getInstance().getContext()
                .getSharedPreferences("add_or_hidde_app", Context.MODE_MULTI_PROCESS);

        if (folder.getEditBundle() != null) {
            ArrayList<AddObjectComponentNameExtend> defaultSelectedComponentNameExs;
            ArrayList list = folder.getEditBundle().getParcelableArrayList("componentnameexs");
            if (list != null) {
                defaultSelectedComponentNameExs = (ArrayList<AddObjectComponentNameExtend>) list.get(0);

                for (AppItemInfo app : apps) {
                    String componetName = app.getComponentName().toString();
                    boolean isShow = true;
                    isShow = pref.getBoolean(componetName, true);
                    if ((isShow)
                            || (!isShow && defaultSelectedComponentNameExs.contains(new AddObjectComponentNameExtend(
                                    app.getComponentName(), null, null, false)))) {
                        appsShown.add(app);
                    }
                }
            }
        }

        return (new AddFolderDialog(SESceneManager.getInstance().getGLActivity(), SESceneManager.getInstance()
                .getGLActivity().getLayoutInflater(), appsShown, folder, getEmptySlotNumOfCurrentWall(),
                getChildMaxNumInFolder(), folder.getEditBundle()));
    }

    private Dialog createAddRemoveAppDialog() {
        List<AppItemInfo> mPrefaceApps = HomeUtils.getSortedAppsByName();
        List<AppItemInfo> sortedApps = new ArrayList<AppItemInfo>();
        for (int i = 0; i < mPrefaceApps.size(); i++) {
            if (!mPrefaceApps.get(i).isShowInPreface()) {
                sortedApps.add(mPrefaceApps.get(i));
            }
        }
        for (int i = 0; i < mPrefaceApps.size(); i++) {
            if (mPrefaceApps.get(i).isShowInPreface()) {
                sortedApps.add(mPrefaceApps.get(i));
            }
        }
        return (new AddRemoveAppDialog(SESceneManager.getInstance().getGLActivity(), SESceneManager.getInstance()
                .getGLActivity().getLayoutInflater(), sortedApps, sortedApps.size()));
    }

    public Dialog createWallPaperDialog() {
        if (mHouse == null) {
            return null;
        }
        final int itemRes = R.array.edit_wall_menu_item_new;
        Dialog dialog = new AlertDialog.Builder(SESceneManager.getInstance().getGLActivity())
                .setTitle(R.string.edit_wall_menu_title).setItems(itemRes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int itemIndex) {
                        dialog.dismiss();
                        switch (itemIndex) {
                        case 0:
                            Utils.showWallpapers(SESceneManager.getInstance().getGLActivity());
                            break;
                        case 1:
                            if (mCropImageInfo != null) {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                intent.putExtra("crop", "true");
                                intent.putExtra("aspectX", mCropImageInfo.mAspectX);
                                intent.putExtra("aspectY", mCropImageInfo.mAspectY);
                                intent.putExtra("output", Uri.fromFile(mCropImageInfo.mTempCropImageFile));
                                intent.putExtra("outputFormat", "JPEG");
                                HomeManager.getInstance().startActivityForResult(intent,
                                        HomeScene.REQUEST_CODE_SELECT_WALLPAPER_IMAGE);
                            }
                            break;
                        case 2:
                            if (mCropImageInfo != null) {
                                Uri u = Uri.fromFile(mCropImageInfo.mTempCropImageFile);
                                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                                intent2.putExtra(MediaStore.EXTRA_OUTPUT, u);
                                HomeManager.getInstance().startActivityForResult(intent2,
                                        HomeScene.REQUEST_CODE_SELECT_WALLPAPER_CAMERA);
                            }
                            break;
                        case 3:
                            if (mCropImageInfo != null) {
                                mCropImageInfo.mOnCropImageFinishedListener.onResetImage(mCropImageInfo.mImageName);
                            }
                            break;
                        }
                    }
                }).create();
        return dialog;
    }

    private Dialog createObjectDialog() {
        return new LongClickObjectDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createChangeLableDialog() {
        return new ChangeLabelDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createChangeIconDialog() {
        return new ChangeIconDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createHelperDialog() {
        return new HelperDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createAdjustCameraDialog() {
        return new CameraAdjustDialog(SESceneManager.getInstance().getGLActivity());
    }

    private Dialog createChangeColorDialog() {
        return new ChangeColorDialog(SESceneManager.getInstance().getGLActivity());
    }

    private WallIndicatorDialog mWallIndicatorDialog;

    private Dialog createWallIndicatorDialog() {
        mWallIndicatorDialog = new WallIndicatorDialog(SESceneManager.getInstance().getGLActivity());
        return mWallIndicatorDialog;
    }

    public void updateWallIndicater(float wallIndex, int wallNum) {
        if (mShowWallIndicator) {
            if (mWallIndicatorDialog != null) {
                mWallIndicatorDialog.updateWallIndex(wallIndex, wallNum);
            }
            if (wallIndex % 1 != 0) {
                showDialog(DIALOG_WALL_INDICATOR);
            } else {
                dismissDialog(DIALOG_WALL_INDICATOR);
            }
        }
    }

    private void prepareObjectDialog(Dialog dialog, Bundle bundle) {
        if (mSelectedObject != null) {
            LongClickObjectDialog d = (LongClickObjectDialog) dialog;
            d.prepareContent(mSelectedObject, bundle);
        }
    }

    private void prepareChangeIconDialog(Dialog dialog) {
        final NormalObject selectObject = mSelectedObject;
        if (selectObject != null) {
            ChangeIconDialog changeIconDialog = (ChangeIconDialog) dialog;
            if (selectObject.getObjectInfo().mDisplayName != null) {
                changeIconDialog.setCustomName(selectObject.getObjectInfo().mDisplayName);
            } else {
                changeIconDialog.setCustomName(HomeUtils.getAppLabel(getContext(), selectObject.getObjectInfo()
                        .getResolveInfo()));
            }
            Bitmap icon = selectObject.getObjectInfo().mShortcutIcon;
            if (icon == null) {
                Drawable drawable = HomeUtils.getAppIcon(getContext(), selectObject.getObjectInfo().getResolveInfo());
                changeIconDialog.setAppIcon(drawable);
            } else {
                changeIconDialog.setAppIcon(icon);
            }

            changeIconDialog.setOnDialogFinished(new ChangeIconDialog.OnDialogFinished() {
                @Override
                public void onFinish(Bitmap icon, boolean changed) {
                    if (changed) {
                        selectObject.updateIcon(icon);
                    } else {
                        selectObject.resetIcon();
                    }
                }
            });
        }
    }

    private void prepareChangeLabelDialog(Dialog dialog) {
        final NormalObject selectObject = mSelectedObject;
        if (selectObject != null) {
            ChangeLabelDialog changeLabelDialog = (ChangeLabelDialog) dialog;

            if (selectObject.getObjectInfo().mDisplayName != null) {
                changeLabelDialog.setCustomName(selectObject.getObjectInfo().mDisplayName);
            } else {
                changeLabelDialog.setCustomName(HomeUtils.getAppLabel(getContext(), selectObject.getObjectInfo()
                        .getResolveInfo()));
            }
            changeLabelDialog.setOnDialogFinished(new ChangeLabelDialog.OnDialogFinished() {
                @Override
                public void onFinish(String displayName, boolean changed) {
                    if (changed) {
                        selectObject.updateLabel(displayName);
                    }
                }

            });
        }
    }

    private void prepareChangeColorDialog(Dialog dialog) {
        ChangeColorDialog changecolorDialog = (ChangeColorDialog) dialog;
        changecolorDialog.setOperateObject(mSelectedObject);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUESTCODE_APPOPTION_GETIMAGE2:
            if (data != null && mHouse != null) {
                final Uri imageFileUri = data.getData();
                int iconSize = (int) (mHouse.mCellWidth - mHouse.mAppIconPaddingLeft - mHouse.mAppIconPaddingRight);
                Bitmap bitmap = null;
                if (imageFileUri != null) {
                    bitmap = HomeUtils.decodeSampledBitmapFromResource(getContext(), imageFileUri, iconSize, iconSize);
                } else {
                    bitmap = HomeUtils.decodeSampledBitmapFromResource(HomeUtils.getTmpImageFilePath(), iconSize,
                            iconSize);
                }
                if (bitmap != null) {
                    dismissDialog(DIALOG_OBJECT_CHANGE_ICON);
                    if (mSelectedObject != null) {
                        mSelectedObject.updateIcon(bitmap);
                    }
                }
            }
            break;
        case REQUEST_CODE_BIND_SHORTCUT:
            if (resultCode == Activity.RESULT_OK) {
                HomeManager.getInstance().startActivityForResult(data, REQUEST_CODE_SELECT_SHORTCUT);
            }
            break;
        case HomeScene.REQUEST_CODE_SELECT_WALLPAPER_IMAGE:
            if (resultCode == Activity.RESULT_OK && mCropImageInfo!= null) {
                Uri url = null;
                Bitmap pic = null;
                if (data != null) {
                    url = data.getData();
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        try {
                            pic = extras.getParcelable("data");
                        } catch (Exception e) {

                        }
                    }
                }
                final Uri imageFileUri = url;
                final Bitmap imageData = pic;
                final String imageName = mCropImageInfo.mImageName;
                final String imageKey = mCropImageInfo.mSaveImagePath;
                if (imageName == null) {
                    return;
                }
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        int imageMaxSizeX = mCropImageInfo.mSaveImageSizeX;
                        int imageMaxSizeY = mCropImageInfo.mSaveImageSizeY;
                        Bitmap bm = null;
                        if (imageFileUri != null) {
                            bm = HomeUtils.decodeSampledBitmapFromResource(getContext(), imageFileUri, imageMaxSizeX,
                                    imageMaxSizeY);
                        } else {
                            bm = HomeUtils.decodeSampledBitmapFromResource(
                                    mCropImageInfo.mTempCropImageFile.getAbsolutePath(), imageMaxSizeX, imageMaxSizeY);
                        }
                        if (bm == null) {
                            bm = imageData;
                        }
                        if (bm == null) {
                            return;
                        }
                        Bitmap des = HomeUtils.resizeBitmap(bm, imageMaxSizeX, imageMaxSizeY);
                        HomeUtils.saveBitmap(des, imageKey, Bitmap.CompressFormat.JPEG);
                        des.recycle();
                        final int image = SEObject.loadImageData_JNI(imageKey);
                        if (image != 0) {
                            new SECommand(HomeScene.this) {
                                public void run() {
                                    SEObject.applyImage_JNI(imageName, imageKey);
                                    SEObject.addImageData_JNI(imageKey, image);
                                }
                            }.execute();
                            mCropImageInfo.mOnCropImageFinishedListener.onChangeImage(imageName, imageKey);
                        }
                        System.gc();
                    }
                });
            }
            break;
        case HomeScene.REQUEST_CODE_SELECT_WALLPAPER_CAMERA:
            if (resultCode == Activity.RESULT_OK&& mCropImageInfo!= null) {
                Intent intent2 = new Intent("com.android.camera.action.CROP");
                Uri u = Uri.fromFile(mCropImageInfo.mTempCropImageFile);
                intent2.setDataAndType(u, "image/*");
                intent2.putExtra("output", u);
                intent2.putExtra("crop", "true");
                intent2.putExtra("aspectX", mCropImageInfo.mAspectX);
                intent2.putExtra("aspectY", mCropImageInfo.mAspectY);
                intent2.putExtra("outputFormat", "JPEG");
                HomeManager.getInstance()
                        .startActivityForResult(intent2, HomeScene.REQUEST_CODE_SELECT_WALLPAPER_IMAGE);
            }
            break;
        default:
            break;
        }
    }

    private Bundle setObjectLongClickDialogContent() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        SESceneManager.getInstance().getGLActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        if (mSelectedObject == null) {
            return null;
        }

        int slotType = mSelectedObject.getObjectInfo().mSlotType;
        int x = 0;
        int y = 0;
        if (slotType == ObjectInfo.SLOT_TYPE_WALL || slotType == ObjectInfo.SLOT_TYPE_APP_WALL
                || slotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            float top = mSelectedObject.getObjectSlot().mSpanY / 2f * mHouse.mCellHeight;
            SEVector3f vector3f = mSelectedObject.getAbsoluteTranslate().clone();
            vector3f.selfAdd(new SEVector3f(0, 0, top));
            SEVector2i position = getCamera().worldToScreenCoordinate(vector3f);
            // value 1024 is screen height of BKB phone (600X1024), hard code 55
            // is base on BKB phone.
            y = position.getY() - 55 * screenHeight / 1024;
            x = position.getX();
            if (mSelectedObject.getObjectSlot().mSpanY == 1) {
                y -= 55 * screenHeight / 1024;
            }
        } else if (slotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            SEVector3f vector3f = mSelectedObject.getAbsoluteTranslate().clone();
            vector3f.selfAdd(new SEVector3f(0, 0, 90));
            SEVector2i position = getCamera().worldToScreenCoordinate(vector3f);
            x = position.getX();
            y = position.getY() - 70 * screenHeight / 1024;
        } else if (slotType == ObjectInfo.SLOT_TYPE_GROUND) {
            float top = 2 * mHouse.mCellHeight;
            SEVector3f vector3f = mSelectedObject.getAbsoluteTranslate().clone();
            vector3f.selfAdd(new SEVector3f(0, 0, top));
            SEVector2i position = getCamera().worldToScreenCoordinate(vector3f);
            x = position.getX();
            y = position.getY() - 55 * screenHeight / 1024;
        } else {
            x = mSelectedObject.getTouchX();
            y = (int) (mSelectedObject.getTouchY() - 120 * getScreenDensity());
        }

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        boolean reversal;
        reversal = x > mSceneInfo.getSceneWidth() / 2;
        Bundle bundle = new Bundle();
        bundle.putInt("dlgx", x);
        bundle.putInt("dlgy", y);
        bundle.putBoolean("reversal", reversal);
        return bundle;
    }

    public class Item {
        public CharSequence mLabel;
        public Drawable mIcon;
        public String mPkgName;
        public String mClsName;

        public Item(CharSequence label, Drawable icon, String pkg, String cls) {
            mLabel = label;
            mIcon = icon;
            mPkgName = pkg;
            mClsName = cls;
        }

        @Override
        public String toString() {
            return mLabel.toString();
        }
    }

    public void updateComponentInDB(Item item) {
        String pkgName = item.mPkgName;
        String clsName = item.mClsName;
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(clsName)) {
            return;
        }
        if (mSetBindObject != null) {
            mSetBindObject.updateComponentName(new ComponentName(pkgName, clsName));
        }
    }

    public void onAddModelToDB(final ModelInfo modelInfo) {
        
        new SECommand(this) {
            public void run() {
                if (!hasLoadCompleted()) {
                    return;
                }
                getDragLayer().block(true);
                ObjectInfo objInfo = new ObjectInfo();
                objInfo.setModelInfo(modelInfo);
                objInfo.mIndex = ProviderUtils.searchMaxIndex(HomeScene.this, ProviderUtils.Tables.OBJECTS_INFO,
                        objInfo.mName) + 1;
                objInfo.mSceneName = getSceneName();
                objInfo.saveToDB();
                final int touchX = getSceneWidth() / 2;
                final int touchY = getSceneHeight() / 2;
                SERay ray = getCamera().screenCoordinateToRay(touchX, touchY);
                float alpha = (1000f - ray.getLocation().getY()) / ray.getDirection().getY();
                float posY = 1000f;
                float posX = ray.getLocation().getX() + alpha * ray.getDirection().getX();
                float posZ = ray.getLocation().getZ() + alpha * ray.getDirection().getZ();
                SEVector3f pos = new SEVector3f();
                pos.set(posX, posY, posZ + 100);
                final SETransParas startTransParas = new SETransParas();
                startTransParas.mTranslate = pos;
                final NormalObject newObject = objInfo.CreateNormalObject(HomeScene.this);
                HomeManager.getInstance().getModelManager().create(getContentObject(), newObject, new Runnable() {
                    public void run() {
                        getDragLayer().block(false);
                        getContentObject().addChild(newObject, false);
                        newObject.setIsFresh(true);
                        newObject.initStatus();
                        newObject.setUserTranslate(startTransParas.mTranslate);
                        newObject.setTouch(touchX, touchY);
                        newObject.setStartTranspara(startTransParas);
                        newObject.setOnMove(true);
                    }

                });

            }
        }.execute();
    }

    @Override
    public void onRemoveModelFromDB(final ModelInfo modelInfo) {
        new SECommand(this) {
            public void run() {
                int size = modelInfo.getInstances().size();
                /*
                 * remove all instances from house
                 */
                for (int i = 0; i < size; i++) {
                    SEObject delObj = modelInfo.getInstances().get(i);
                    SEObject parent = delObj.getParent();
                    parent.removeChild(delObj, true);
                }
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
            }
        }.execute();
    }

    /**
     * 主题切换前，矫正相机位置。 主题切换有两种模式： 一种是只换房间，以及微调物体摆放的位置和相机位置 第二种，重新加载整个场景
     */
    private void onSceneChanged() {
        if (!mLoadCompleted) {
            return;
        }
        moveToWallSight(new SEAnimFinishListener() {
            public void onAnimationfinish() {
                ThemeInfo themeInfo = HomeManager.getInstance().getCurrentThemeInfo();
                if (themeInfo.mSceneName.equals(getSceneName())) {
                    HomeUtils.updateHouseName(getContext(), themeInfo);
                    getHomeSceneInfo().setThemeInfo(themeInfo);
                    getCamera().notifySurfaceChanged(getHomeSceneInfo().getSceneWidth(),
                            getHomeSceneInfo().getSceneHeight());
                    // 通知每一个物体场景已经变了，false代表第一种情况
                    getContentObject().onSceneChanged(SCENE_CHANGED_TYPE.NEW_CONFIG);
                } else {
                    removeAllDialog();
                    // 通知每一个物体场景已经变了，true代表第二种情况
                    getContentObject().onSceneChanged(SCENE_CHANGED_TYPE.NEW_SCENE);
                    // 清除ModelInfo关于场景的信息
                    HomeManager.getInstance().getModelManager().clearModelStatus();
                    // 释放老场景
                    release();
                    // 加载新场景
                    HomeSceneInfo sceneInfo = new HomeSceneInfo();
                    sceneInfo.setThemeInfo(themeInfo);
                    HomeScene screen = new HomeScene(getContext(), sceneInfo.mSceneName);
                    screen.setHomeSceneInfo(sceneInfo);
                    SESceneManager.getInstance().setCurrentScene(screen);
                    HomeManager.getInstance().setCurrentScene(screen);
                    SESceneManager.getInstance().start3DScene(getHomeSceneInfo().getSceneWidth(),
                            getHomeSceneInfo().getSceneHeight());
                }
            }
        });
    }

    private void removeAllDialog() {
        removeDialog(DIALOG_DELETE_OBJECTS);
        removeDialog(DIALOG_SELECT_WALLPAPER);
        removeDialog(DIALOG_WALL_LONG_CLICK);
        removeDialog(DIALOG_OBJECT_LONG_CLICK);
        removeDialog(DIALOG_OBJECT_CHANGE_LABLE);
        removeDialog(DIALOG_OBJECT_CHANGE_ICON);
        removeDialog(DIALOG_HELPER);
        removeDialog(DIALOG_ADJUST_CAMERA);
        removeDialog(DIALOG_WEATHER);
        removeDialog(DIALOG_WALL_LONG_CLICK_ADD_APP);
        removeDialog(DIALOG_WALL_LONG_CLICK_ADD_FOLDER);
        removeDialog(DIALOG_EDITFOLDER);
        removeDialog(DIALOG_CHANGE_COLOR);
        removeDialog(DIALOG_WALL_INDICATOR);
        removeDialog(DIALOG_ADD_REMOVE_APP);
    }

    private void dismissAllDialog() {
        dismissDialog(DIALOG_DELETE_OBJECTS);
        dismissDialog(DIALOG_SELECT_WALLPAPER);
        dismissDialog(DIALOG_WALL_LONG_CLICK);
        dismissDialog(DIALOG_OBJECT_LONG_CLICK);
        dismissDialog(DIALOG_OBJECT_CHANGE_LABLE);
        dismissDialog(DIALOG_OBJECT_CHANGE_ICON);
        dismissDialog(DIALOG_HELPER);
        dismissDialog(DIALOG_ADJUST_CAMERA);
        dismissDialog(DIALOG_WEATHER);
        dismissDialog(DIALOG_WALL_LONG_CLICK_ADD_APP);
        dismissDialog(DIALOG_WALL_LONG_CLICK_ADD_FOLDER);
        dismissDialog(DIALOG_EDITFOLDER);
        dismissDialog(DIALOG_CHANGE_COLOR);
        dismissDialog(DIALOG_WALL_INDICATOR);
        dismissDialog(DIALOG_ADD_REMOVE_APP);
    }

    private House mHouse;

    public void setHouse(House house) {
        mHouse = house;
        if (mSky != null && !mSky.hasInit()) {
            mSky.initStatus();
        }
    }

    public House getHouse() {
        return mHouse;
    }

    private Desk mDesk;

    public void setDesk(Desk desk) {
        mDesk = desk;
    }

    public Desk getDesk() {
        return mDesk;
    }

    private Cloud mSky;

    public void setSky(Cloud cloud) {
        mSky = cloud;
    }

    public Cloud getSky() {
        return mSky;
    }

    /**
     * Get empty slots of current wall.
     */
    private int getEmptySlotNumOfCurrentWall() {
        if (mHouse == null) {
            return 0;
        }

        NormalObject wall = mHouse.getWall(getWallNearestIndex());
        if (wall == null) {
            return 0;
        }
        int fullCount = mHouse.mCellCountX * mHouse.mCellCountY;
        for (SEObject child : wall.getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                ObjectSlot slot = normalObject.getObjectSlot();
                fullCount = fullCount - slot.mSpanX * slot.mSpanY;
            }
        }
        return fullCount;
    }

    /**
     * Get max-num a floder can contains.
     */
    public static int getChildMaxNumInFolder() {
        // TODO
        return 12;
    }

    @Override
    public void onAppSelected(ArrayList<AddObjectItemInfo> selectedList, int availableNum) {
        if ((selectedList == null) || (selectedList.size() <= 0)) {
            return;
        }
        if (availableNum <= 0) {
            Toast.makeText(getContext(), R.string.no_room, Toast.LENGTH_SHORT).show();
            return;
        }

        final VesselLayer vesselLayer = mHouse.getVesselLayer();
        if (vesselLayer == null) {
            return;
        }

        final ArrayList<NormalObject> appObjList = new ArrayList<NormalObject>();
        int c = 0;
        for (ItemInfo iia : selectedList) {
            c++;
            if (c > availableNum) {
                if (selectedList.size() > availableNum) {
                    Toast.makeText(getContext(), R.string.no_room, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            if (iia == null) {
                continue;
            }
            ItemInfo ii = new AppItemInfo(getContext(), iia.getResolveInfo(), iia.getComponentName());
            ii.mItemType = ItemInfo.ITEM_TYPE_APP;
            final AppObject itemObject = AppObject.create(this, ii);
            itemObject.setIsFresh(false);
            appObjList.add(itemObject);
        }

        new SECommand(this) {
            public void run() {
                final SEObject parent = getContentObject();
                for (NormalObject itemObject : appObjList) {
                    parent.addChild(itemObject, true);
                    itemObject.initStatus();
                    boolean bl = vesselLayer.placeObjectToVessel(itemObject);
                    if (bl == false) {
                        // TODO
                    }
                }
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
            }
        }.execute();
    }

    // #########################################dialog relates##########
    public void showDialog(final int id) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (((HomeActivity) SESceneManager.getInstance().getGLActivity()).isLiving()) {
                    SESceneManager.getInstance().getGLActivity().showDialog(id);
                }
            }
        });
    }

    public void showDialog(final int id, final Bundle bundle) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                // if activity have been destroyed, no need pop up the dialog.
                if (((HomeActivity) SESceneManager.getInstance().getGLActivity()).isLiving()) {
                    try {
                        SESceneManager.getInstance().getGLActivity().showDialog(id, bundle);
                    } catch (Exception e) {
                        Log.i("test", "error is " + e.toString());
                    }

                }
            }
        });
    }

    public void removeDialog(final int id) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                try {
                    SESceneManager.getInstance().getGLActivity().removeDialog(id);
                } catch (Exception e) {
                }
            }
        });
    }

    public void dismissDialog(final int id) {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                try {
                    SESceneManager.getInstance().getGLActivity().dismissDialog(id);
                } catch (Exception e) {
                }
            }
        });
    }

    // #########################################below are the action of camera
    private SECameraData mDeskObserverSight;
    private SECameraData mSkyObserverSight;
    private SEEmptyAnimation mMoveSightAnimation;
    private SEEmptyAnimation mSetRadiusAndFovAnim;
    private float mSkyY = 0;

    public void playSetRadiusAndFovAnim(final float endRadius, final float endFov, SEAnimFinishListener listener) {
        stopAllAnimation();
        final float curRadius = getCamera().getRadius();
        final float curFov = getCamera().getFov();
        if (endRadius == curRadius) {
            if (listener != null) {
                listener.onAnimationfinish();
            }
            return;
        }
        int animTimes = (int) (Math.abs(endRadius - curRadius) / 20);
        mSetRadiusAndFovAnim = new SEEmptyAnimation(this, 0, 1, animTimes) {
            @Override
            public void onAnimationRun(float value) {
                float radius = (endRadius - curRadius) * value + curRadius;
                float fov = (endFov - curFov) * value + curFov;
                getCamera().setRadiusAndFov(radius, fov);
            }
        };
        mSetRadiusAndFovAnim.setAnimFinishListener(listener);
        mSetRadiusAndFovAnim.execute();
    }

    public void changeSight(float skyY, boolean breakAnimation) {
        if (breakAnimation) {
            stopAllAnimation();
        }
        if (mDesk == null || mSky == null || !mSky.hasInit()) {
            return;
        }
        if (mSkyY == 0) {
            getCamera().save();
            mDeskObserverSight = mDesk.getObserveCamera();
            mSkyObserverSight = mSky.getObserveCamera();
        }
        if (skyY > 1) {
            mSkyY = 1;
        } else if (skyY < -1) {
            mSkyY = -1;
        } else {
            mSkyY = skyY;
        }
        if (mSkyY == 0) {
            setStatus(HomeScene.STATUS_ON_DESK_SIGHT, false);
            setStatus(HomeScene.STATUS_ON_SKY_SIGHT, false);
        } else if (mSkyY < 0) {
            setStatus(HomeScene.STATUS_ON_DESK_SIGHT, true);
            setStatus(HomeScene.STATUS_ON_SKY_SIGHT, false);
        } else {
            setStatus(HomeScene.STATUS_ON_DESK_SIGHT, false);
            setStatus(HomeScene.STATUS_ON_SKY_SIGHT, true);
        }
        SEVector3f srcLoc = getCamera().restore().mLocation;
        SEVector3f srcAxisZ = getCamera().restore().mAxisZ;
        float srcFov = getCamera().restore().mFov;
        float paras = Math.abs(mSkyY);
        SEVector3f desAxisZ;
        SEVector3f desLoc;
        float desFov;
        if (mSkyY >= 0) {
            desAxisZ = mSkyObserverSight.mAxisZ;
            desLoc = mSkyObserverSight.mLocation;
            desFov = mSkyObserverSight.mFov;
        } else {
            desAxisZ = mDeskObserverSight.mAxisZ;
            desLoc = mDeskObserverSight.mLocation;
            desFov = mDeskObserverSight.mFov;
        }
        getCamera().getCurrentData().mLocation = srcLoc.add(desLoc.subtract(srcLoc).mul(paras));
        getCamera().getCurrentData().mAxisZ = srcAxisZ.add(desAxisZ.subtract(srcAxisZ).mul(paras));
        getCamera().getCurrentData().mFov = srcFov + (desFov - srcFov) * paras;
        getCamera().setCamera();
    }

    public float getSightValue() {
        return mSkyY;
    }

    public void moveToSkySight(final SEAnimFinishListener l) {
        moveSightTo(mSkyY, 1, l);
    }

    public void moveToDeskSight(SEAnimFinishListener l) {
        moveSightTo(mSkyY, -1, l);
    }

    public void moveSightTo(float from, float to, SEAnimFinishListener l) {
        if (from == to) {
            if (l != null) {
                l.onAnimationfinish();
            }
            return;
        }
        int animTimes = (int) (Math.abs(to - from) * 10);
        mMoveSightAnimation = new SEEmptyAnimation(this, from, to, animTimes) {

            @Override
            public void onAnimationRun(float value) {
                changeSight(value, false);
            }

        };
        mMoveSightAnimation.setAnimFinishListener(l);
        mMoveSightAnimation.execute();

    }

    public void moveToWallSight(final SEAnimFinishListener l) {
        moveSightTo(mSkyY, 0, l);
    }

    public boolean isBusy() {
        if (mMoveSightAnimation != null) {
            if (!mMoveSightAnimation.isFinish()) {
                return true;
            }
        }

        if (mSetRadiusAndFovAnim != null) {
            if (!mSetRadiusAndFovAnim.isFinish()) {
                return true;
            }
        }
        return false;
    }

    public void stopAllAnimation() {
        if (mMoveSightAnimation != null) {
            mMoveSightAnimation.stop();
        }
        if (mSetRadiusAndFovAnim != null) {
            mSetRadiusAndFovAnim.stop();
        }
    }

    @Override
    public SECameraData getCameraData() {
        return getHomeSceneInfo().mSECameraData;
    }

    @Override
    public void onFolderCreated(final String folderName, final ArrayList<AddObjectItemInfo> selectedList) {
        if ((selectedList == null) || (selectedList.size() <= 0)) {
            return;
        }
        new SECommand(this) {
            public void run() {
                VesselLayer vesselLayer = mHouse.getVesselLayer();
                Folder folder = Folder.create(HomeScene.this, folderName);
                folder.setVisible(false);
                getContentObject().addChild(folder, true);
                folder.initStatus();
                int c = 0;
                int max = getChildMaxNumInFolder();
                for (ItemInfo iia : selectedList) {
                    ItemInfo ii = new AppItemInfo(getContext(), iia.getResolveInfo(), iia.getComponentName());
                    ii.mItemType = ItemInfo.ITEM_TYPE_APP;
                    AppObject itemObject = AppObject.create(HomeScene.this, ii);
                    itemObject.getObjectInfo().mVesselName = folder.mName;
                    itemObject.getObjectInfo().mVesselIndex = folder.getObjectInfo().mIndex;
                    itemObject.getObjectInfo().mObjectSlot.mSlotIndex = c;
                    itemObject.setIsFresh(false);
                    itemObject.setVisible(false);
                    folder.addChild(itemObject, true);
                    itemObject.initStatus();
                    c++;
                    if (c >= max) {
                        break;
                    }
                }
                folder.updateFolderCover();
                vesselLayer.placeObjectToVessel(folder);
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
            }
        }.execute();
    }

    public List<WallCellLayer> getAllWallCellLayerList() {
        ArrayList<WallCellLayer> mAllLayer = new ArrayList<WallCellLayer>();
        if (null != mHouse) {
            for (int i = 0; i < mHouse.mWallNum; i++) {
                WallCellLayer wallCellLayer = (WallCellLayer) mHouse.getWall(i).getVesselLayer();
                mAllLayer.add(wallCellLayer);
            }
        }
        return mAllLayer;
    }

    // / invocation by normal object
    public void startResize(NormalObject object) {
        if (null != object && object.canBeResized()) {
            getDragLayer().startResize(object);
        }
    }

    public void showAllApps() {
        handleMessage(MSG_TYPE_SHOW_APP_VIEW, ApplicationMenu.TYPE_SHOW_KEEP_LAST);
    }

    public boolean switchDesk(Desk newDesk) {
        return null != mDesk && mDesk.switchDesk(newDesk);
    }

    public void showDesk(SEAnimFinishListener listener) {
        if (null != mDesk) {
            mDesk.show(listener);
        } else if (null != listener) {
            listener.onAnimationfinish();
        }
    }

    public void hideDesk(SEAnimFinishListener listener) {
        if (null != mDesk) {
            mDesk.hide(listener);
        } else if (null != listener) {
            listener.onAnimationfinish();
        }
    }

    public List<VesselLayer> getDragDropLayerList() {
        List<VesselLayer> targetList = new ArrayList<VesselLayer>();
        if (null != mDesk) {
            targetList.add(mDesk.getVesselLayer());
        }
        if (null != mHouse) {
            targetList.add(mHouse.getVesselLayer());
        }
        if (null != getContentObject()) {
            VesselObject root = (VesselObject) getContentObject();
            targetList.add(root.getVesselLayer());
        }
        return targetList;
    }

    public List<VesselLayer> getDragDropLayerInHouse() {
        List<VesselLayer> mAllLayer = new ArrayList<VesselLayer>();
        if (null != mHouse) {
            VesselObject vessel = mHouse.getGround();
            if (null != vessel) {
                mAllLayer.add(vessel.getVesselLayer());
            }
            mAllLayer.add(new WallLayer(this, mHouse));
        }
        return mAllLayer;
    }

    public void addWallRadiusChangedListener(House.WallRadiusChangedListener listener) {
        if (null != mHouse && null != listener) {
            mHouse.addWallRadiusChangedListener(listener);
        }
    }

    public void removeWallRadiusChangedListener(House.WallRadiusChangedListener listener) {
        if (null != mHouse && null != listener) {
            mHouse.removeWallRadiusChangedListener(listener);
        }
    }

    public void addWallRotateListener(House.WallRotateListener listener) {
        if (null != mHouse && null != listener) {
            mHouse.addWallRotateListener(listener);
        }
    }

    public void removeWallRotateListener(House.WallRotateListener listener) {
        if (null != mHouse && null != listener) {
            mHouse.removeWallRotateListener(listener);
        }
    }

    public void toNearestFace() {
        mHouse.toNearestFace(null, 2);
    }

    public boolean placeToNearestWallFace(NormalObject normalObject) {
        if (null == mHouse) {
            return false;
        }

        VesselLayer vesselLayer = mHouse.getWall(getWallNearestIndex()).getVesselLayer();
        boolean result = vesselLayer.placeObjectToVessel(normalObject);
        return result;
    }

    public int getWallNearestIndex() {
        return null == mHouse ? 0 : mHouse.getWallNearestIndex();
    }

    public void stopHouseAnimation() {
        if (null != mHouse) {
            mHouse.stopAllAnimation(null);
        }
    }

    public void toLeftWallFace(SEAnimFinishListener listener) {
        if (null != mHouse) {
            mHouse.toLeftFace(listener, 1.5f);
        }
    }

    public void toRightWallFace(SEAnimFinishListener listener) {
        mHouse.toRightFace(listener, 1.5f);
    }

    public void applyWallpaperFromMarket(ArrayList<WallPaperItem> wallPaperItems, String paperId) {
        if (null != mHouse) {
            mHouse.applyWallpaper(wallPaperItems, paperId);
        }
    }

    public ArrayList<RawPaperItem> exportWallPaperToMarket() {
        if (null != mHouse) {
            return mHouse.exportWallpaper();
        }
        return new ArrayList<RawPaperItem>();
    }

    @Override
    public void onStartDrag(final String resourceID, final float touchX, final float touchY) {
        new SECommand(this) {
            public void run() {
                if (!hasLoadCompleted()) {
                    return;
                }
                ModelInfo modelInfo = HomeManager.getInstance().getModelManager().findModelInfoByID(resourceID);
                if (modelInfo == null) {
                    return;
                }
                getDragLayer().block(true);
                ObjectInfo objInfo = new ObjectInfo();
                objInfo.setModelInfo(modelInfo);
                objInfo.mIndex = ProviderUtils.searchMaxIndex(HomeScene.this, ProviderUtils.Tables.OBJECTS_INFO,
                        objInfo.mName) + 1;
                objInfo.mSceneName = getSceneName();
                objInfo.saveToDB();
                SERay ray = getCamera().screenCoordinateToRay((int) touchX, (int) touchY);
                float alpha = (1000f - ray.getLocation().getY()) / ray.getDirection().getY();
                float posY = 1000f;
                float posX = ray.getLocation().getX() + alpha * ray.getDirection().getX();
                float posZ = ray.getLocation().getZ() + alpha * ray.getDirection().getZ();
                SEVector3f pos = new SEVector3f();
                pos.set(posX, posY, posZ + 100);
                final SETransParas startTransParas = new SETransParas();
                startTransParas.mTranslate = pos;
                final NormalObject newObject = objInfo.CreateNormalObject(HomeScene.this);
                HomeManager.getInstance().getModelManager().create(getContentObject(), newObject, new Runnable() {
                    public void run() {
                        getDragLayer().block(false);
                        getContentObject().addChild(newObject, false);
                        newObject.setIsFresh(true);
                        newObject.initStatus();
                        newObject.setUserTranslate(startTransParas.mTranslate);
                        newObject.setTouch((int) touchX, (int) touchY);
                        newObject.setStartTranspara(startTransParas);
                        newObject.setOnMove(true);
                    }

                });

            }
        }.execute();

    }

    @Override
    public boolean onDrag(MotionEvent ev) {
        return SESceneManager.getInstance().dispatchTouchEvent(ev);
    }

    public void updateShelfWall() {
        if (mHouse != null) {
            mHouse.updateShelfWall();
        }
    }

}
