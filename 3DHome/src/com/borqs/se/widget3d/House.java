package com.borqs.se.widget3d;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;

import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.market.wallpaper.WallpaperUtils;
import com.borqs.se.download.WallPaperItem;
import com.borqs.se.engine.*;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.home3d.HomeUtils.CropImageInfo;
import com.borqs.se.home3d.HomeUtils.OnCropImageFinishedListener;
import com.borqs.se.home3d.ModelInfo.ImageItem;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.ThemeInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.ShortcutCallBack;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class House extends VesselObject implements ShortcutCallBack,OnCropImageFinishedListener{
    public float mSkyRadius = 0;
    public float mWallWidth = 768;
    public float mWallHeight = 1013;
    public float mWallPaddingLeft = 0;
    public float mWallPaddingRight = 0;
    public float mWallPaddingTop = 0;
    public float mWallPaddingBottom = 45;
    public int mWallNum = 8;
    public float mWallRadius = 1000;
    public int mCellCountX = 4;
    public int mCellCountY = 4;
    public float mCellWidth = 189;
    public float mCellHeight = 231;
    public float mWidthGap = 4;
    public float mHeightGap = 15;
    public int mAppIconPaddingTop = 16;
    public int mAppIconPaddingLeft = 18;
    public int mAppIconPaddingRight = 18;
    public int mAppIconPaddingBottom = 8;
    
    
    public String mCurrentTheme;
    private VelocityTracker mVelocityTracker;
    private SEEmptyAnimation mToFaceAnimation;
    private float mCylinderIndex;
    private float mPerFaceAngle;
    private float mCurrentAngle;
    private ArrayList<WallRadiusChangedListener> mWallRadiusChangedListeners;
    private ArrayList<WallRotateListener> mWallRotateListeners;

    private boolean mOnMoveSight;
    private boolean mCancelClick;
    private HomeManager mHomeManager;

    private List<String> mImageNamesOfAllWall;
    private String mImageNamesOfGround;
    // 墙纸的裁剪大小以及墙纸的实际大小：
    // 实际大小指的是实际前面模新贴图大小，墙纸的裁剪大小指贴图有效区域
    private int mWallPaperCropSizeX;
    private int mWallPaperCropSizeY;
    private int mWallPaperSizeX;
    private int mWallPaperSizeY;
    private int mPreAction;
    private Ground mGround;
    private Map<Integer, Wall> mAllWall;
    private boolean mHasBeenLoadedFinish = false;
    private ShelfManager mShelfManager;
    private boolean mIsLandScape;
    private boolean mShowDeskObjectShef = true;
    private boolean mShowAppShef = false;
    
    public interface WallRadiusChangedListener {
        void onWallRadiusChanged(int faceIndex);
    }

    public interface WallRotateListener {
        void onWallRotate(float faceIndex, int wallNum);
    }

    public House(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mHomeManager = HomeManager.getInstance();
        mWallRadiusChangedListeners = new ArrayList<WallRadiusChangedListener>();
        mWallRotateListeners = new ArrayList<WallRotateListener>();
        mCurrentAngle = 0;
        setPressType(PRESS_TYPE.NONE);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setOnClickListener(null);
        setOnLongClickListener(null);
        getSpecialConfigFromXML();
        mIsLandScape = HomeManager.getInstance().isLandscapeOrientation();
        mCurrentTheme = HomeManager.getInstance().getCurrentThemeInfo().mThemeName;
        mPerFaceAngle = 360.0f / mWallNum;
        mOnMoveSight = false;
        mCancelClick = false;       
        LauncherModel.getInstance().addAppCallBack(this);
        LauncherModel.getInstance().setShortcutCallBack(this);
        setVesselLayer(new HouseLayer(getHomeScene(), this));
        initComponent();
        float currentAngle = -getHomeSceneInfo().mWallIndex * mPerFaceAngle;
        setRotate(currentAngle, true);
        getHomeScene().setHouse(this);
    }

    private void getSpecialConfigFromXML() {
        mWallPaperSizeX = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallpaperImageSizeX"));
        mWallPaperSizeY = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallpaperImageSizeY"));
        mWallPaperCropSizeX = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallpaperImageCropSizeX"));
        mWallPaperCropSizeY = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallpaperImageCropSizeY"));

        mSkyRadius = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "SkyRadius"));
        mWallPaddingLeft = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallPaddingLeft"));
        mWallPaddingRight = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallPaddingRight"));
        mWallPaddingTop = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallPaddingTop"));
        mWallPaddingBottom = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "WallPaddingBottom"));
        mWallNum = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "WallNum"));
        mWallRadius = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "WallRadius"));
        mCellCountX = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "CellCountX"));
        mCellCountY = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "CellCountY"));
        mCellWidth = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "CellWidth"));
        mCellHeight = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "CellHeight"));
        mWidthGap = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "WidthGap"));
        mHeightGap = Float.parseFloat(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "HeightGap"));
        mAppIconPaddingTop = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "AppIconPaddingTop"));
        mAppIconPaddingLeft = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "AppIconPaddingLeft"));
        mAppIconPaddingRight = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "AppIconPaddingRight"));
        mAppIconPaddingBottom = Integer.parseInt(getObjectInfo().mModelInfo.getSpecialAttribute(getContext(),
                "AppIconPaddingBottom"));

        mWallWidth = mWallPaddingLeft + mWallPaddingRight + mCellWidth * mCellCountX + mWidthGap * (mCellCountX - 1);
        mWallHeight = mWallPaddingTop + mWallPaddingBottom + mCellHeight * mCellCountY + mHeightGap * (mCellCountY - 1);
    }

    public SEVector2f getWallCellSize() {
        return new SEVector.SEVector2f(mCellWidth + mWidthGap, mCellHeight + mHeightGap);
    }

    public float getWallCellWidth(float spanX) {
        float gridSizeX = mCellWidth + mWidthGap;
        return spanX * gridSizeX - mWidthGap;
    }

    public float getWallCellHeight(float spanY) {
        float gridSizeY = mCellHeight + mHeightGap;
        return spanY * gridSizeY - mHeightGap;
    }

    private void initComponent() {
        String path = getCurrentThemeFolderPath();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String regularName = "ground";
        final SEObject ground = findComponenetObjectByRegularName(regularName);
        if (ground != null) {
            mImageNamesOfGround = ground.getImageName();
            ground.setClickable(true);
            ground.setIsEntirety(true);
            ground.setPressType(PRESS_TYPE.COLOR);
            ground.setOnLongClickListener(new OnTouchListener() {
                public void run(SEObject obj) {
                    CropImageInfo cropImageInfo = new CropImageInfo();
                    cropImageInfo.mAspectX =1;
                    cropImageInfo.mAspectY = 1;
                    cropImageInfo.mSaveImageSizeX = 1024;
                    cropImageInfo.mSaveImageSizeY = 1024;
                    cropImageInfo.mImageName = mImageNamesOfGround;
                    cropImageInfo.mTempCropImageFile = new File(HomeUtils.getTmpImageFilePath());
                    cropImageInfo.mSaveImagePath = getGroundPaperSavedPath(0);
                    cropImageInfo.mOnCropImageFinishedListener = House.this;
                    getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_SELECT_WALLPAPER_DIALOG, cropImageInfo);
                }
            });
        }

        SEObject roof = findComponenetObjectByRegularName("roof");
        if (roof != null) {
            roof.setClickable(true);
            roof.setIsEntirety(true);
            roof.setPressType(PRESS_TYPE.NONE);
            roof.setOnDoubleClickListener(new OnTouchListener() {
                @Override
                public void run(SEObject obj) {
                    performDoubleTap();
                }
            });
        }

        mImageNamesOfAllWall = new ArrayList<String>();
        for (int i = 0; i < mWallNum; i++) {
            regularName = "wall_face_" + i;
            SEObject obj = findComponenetObjectByRegularName(regularName);
            final int wallIndex = i;
            if (obj != null) {
                final String imageName = obj.getImageName();
                mImageNamesOfAllWall.add(imageName);
                obj.setClickable(true);
                obj.setIsEntirety(true);
                obj.setPressType(PRESS_TYPE.NONE);
                obj.setOnLongClickListener(new OnTouchListener() {
                    public void run(SEObject obj) {
                        CropImageInfo cropImageInfo = new CropImageInfo();
                        cropImageInfo.mAspectX = mWallPaperCropSizeX;
                        cropImageInfo.mAspectY = mWallPaperCropSizeY;
                        cropImageInfo.mSaveImageSizeX = mWallPaperSizeX;
                        cropImageInfo.mSaveImageSizeY = mWallPaperSizeY;
                        cropImageInfo.mImageName = imageName;
                        cropImageInfo.mTempCropImageFile = new File(HomeUtils.getTmpImageFilePath());
                        cropImageInfo.mSaveImagePath = getWallPaperSavePath(wallIndex);
                        cropImageInfo.mOnCropImageFinishedListener = House.this;
                        getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG, cropImageInfo);
                    }
                });

                obj.setOnDoubleClickListener(new OnTouchListener() {
                    @Override
                    public void run(SEObject obj) {
                        performDoubleTap();
                    }
                });
            }
        }

        updateGroundAndWallsNode();
    }

    @Override
    public void onLoadFinished() {
        updateGroundAndWallsNode();
        mShowDeskObjectShef = HomeManager.getInstance().getWhetherShowDeskObjectShelf();
        mShowAppShef = HomeManager.getInstance().getWhetherShowAppShelf();
        mShelfManager = new ShelfManager(getHomeScene());
        mHasBeenLoadedFinish = true;
    }

    public ShelfManager getShelfManager() {
        return mShelfManager;
    }

    // / reform duplicate code
    private void updateGroundAndWallsNode() {
        if (null == mAllWall) {
            mAllWall = new HashMap<Integer, Wall>();
        } else {
            mAllWall.clear();
        }

        for (SEObject child : getChildObjects()) {
            if (child instanceof Ground) {
                mGround = (Ground) child;
            } else if (child instanceof Wall) {
                Wall wall = (Wall) child;
                mAllWall.put(wall.getObjectSlot().mSlotIndex, wall);
            }
        }
    }

    public Ground getGround() {
        return mGround;
    }

    public Wall getWall(int index) {
        Wall wall = null;
        if (mAllWall != null) {
            wall = mAllWall.get(index);
        }
        return wall;
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        if (objectSlot.mSlotIndex >= 0) {
            SETransParas transparas = new SETransParas();
            float angle = objectSlot.mSlotIndex * 360.f / mWallNum;
            SEVector2f yDirection = new SEVector2f((float) Math.cos((angle + 90) * Math.PI / 180),
                    (float) Math.sin((angle + 90) * Math.PI / 180));
            SEVector2f xDirection = new SEVector2f((float) Math.cos(angle * Math.PI / 180), (float) Math.sin(angle
                    * Math.PI / 180));
            float offsetY = mWallRadius;
            float offsetX = 0;
            SEVector2f offset = yDirection.mul(offsetY).add(xDirection.mul(offsetX));
            float offsetZ = mWallHeight / 2;
            float z = offsetZ;
            transparas.mTranslate.set(offset.getX(), offset.getY(), z);
            transparas.mRotate.set(angle, 0, 0, 1);
            return transparas;
        }
        return null;
    }

    /**
     * 当主题改变时我们需要做：
     * 第一，找到主题下面的房子，且更新当前房子ObjectInfo表的物体名字
     * 第二，找到当前场景中的房子，删除房子节点下的名称为 house.mName +“_model”的物体（该物体为模型节点）
     * 第三，更改房子名称，并更改ModelInfo
     * 第四，从新加载房子的模型节点。
     * 第五，初始化房子
     */
    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_CONFIG) {
            // 第一，找到主题下面的房子，且更新当前房子ObjectInfo表的物体名字
            ThemeInfo themeInfo = mHomeManager.getCurrentThemeInfo();
            ModelInfo modelInfo = mHomeManager.getModelManager().findModelInfo(themeInfo.mHouseName);
            if (modelInfo == null) {
                return;
            }
            // 第二，找到当前场景中的房子，删除房子节点下的名称为 house.mName +“_model”的物体（该物体为模型节点）
            clearShelfWall();
            getModelComponenet().release();
            // 第三，更改房子名称，并更改ModelInfo
            setName(modelInfo.mName);
            getObjectInfo().mModelInfo.moveInstancesTo(modelInfo);
            changeModelInfo(modelInfo);
            // 第四，从新加载房子的模型节点。
            mHomeManager.getModelManager().loadModelOnly(modelInfo, House.this, getModelComponenet(), new Runnable() {
                public void run() {
                    // 第五，初始化房子
                    initStatus();
                    SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                    SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
                }
            });
        }

    }

    public void addWallRadiusChangedListener(WallRadiusChangedListener l) {
        if (!mWallRadiusChangedListeners.contains(l)) {
            mWallRadiusChangedListeners.add(l);
            l.onWallRadiusChanged(getWallNearestIndex());
        }
    }

    public void removeWallRadiusChangedListener(WallRadiusChangedListener l) {
        if (l != null) {
            mWallRadiusChangedListeners.remove(l);
        }
    }
    public void addWallRotateListener(WallRotateListener l) {
        if (!mWallRotateListeners.contains(l)) {
            mWallRotateListeners.add(l);
            if (hasInit()) {
                float wallIndex;
                if (mCylinderIndex < 0) {
                    wallIndex = (mWallNum + mCylinderIndex % mWallNum) % mWallNum;
                } else {
                    wallIndex = mCylinderIndex % mWallNum;
                }
                l.onWallRotate(wallIndex, mWallNum);
            }
        }
    }

    public void removeWallRotateListener(WallRotateListener l) {
        if (l != null) {
            mWallRotateListeners.remove(l);
        }
    }
    private void setRotate(float angle, boolean force) {
        if (mCurrentAngle == angle && !force) {
            return;
        }
        SERotate rotate = new SERotate(angle);
        mCurrentAngle = angle;
        int preIndex = getWallNearestIndex();
        mCylinderIndex = -mCurrentAngle / mPerFaceAngle;
        /**notify wall position changed begin*/
        float wallIndex;
        if (mCylinderIndex < 0) {
            wallIndex = (mWallNum + mCylinderIndex % mWallNum) % mWallNum;
        } else {
            wallIndex = mCylinderIndex % mWallNum;
        }
        if (mWallRotateListeners!= null) {
            for (WallRotateListener l : mWallRotateListeners) {
                l.onWallRotate(wallIndex, mWallNum);
            }
        }
        getHomeScene().updateWallIndicater(wallIndex, mWallNum);
        /**notify wall position changed end*/
        
        /**notify wall index changed begin*/
        int curIndex = getWallNearestIndex();
        if (preIndex != curIndex || force) {
            for (WallRadiusChangedListener l : mWallRadiusChangedListeners) {
                l.onWallRadiusChanged(curIndex);
            }
            updateCurrentDisplayedWall();
        }
        /**notify wall index changed end*/
        setUserRotate(rotate);
    }

    private void updateCurrentDisplayedWall() {
        int curIndex = getWallNearestIndex();
        int showFaceIndexA = curIndex - 1;
        if (showFaceIndexA < 0) {
            showFaceIndexA = mWallNum - 1;
        }
        int showFaceIndexB = curIndex;
        int showFaceIndexC = curIndex + 1;
        if (showFaceIndexC > mWallNum - 1) {
            showFaceIndexC = 0;
        }
        if (mAllWall != null) {
            Iterator<Entry<Integer, Wall>> iter = mAllWall.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, Wall> entry = iter.next();
                Wall wall = entry.getValue();
                if (entry.getKey() == showFaceIndexA || entry.getKey() == showFaceIndexB
                        || entry.getKey() == showFaceIndexC) {
                    wall.setVisible(true);
                } else {
                    wall.setVisible(false);
                }

            }
        }
    }

    private float getFaceAngle(float index) {
        float to = -index * mPerFaceAngle;
        return to;
    }

    public float getWallIndex() {
        return mCylinderIndex;
    }

    public float getAngle() {
        return mCurrentAngle;
    }

    public int getWallNearestIndex() {
        float index;
        if (mCylinderIndex < 0) {
            index = mWallNum + mCylinderIndex % mWallNum;
        } else {
            index = mCylinderIndex % mWallNum;
        }
        int nearestIndex = Math.round(index);
        if (nearestIndex == mWallNum) {
            nearestIndex = 0;
        }
        return nearestIndex;
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    public void toNearestFace(SEAnimFinishListener l, float interpolatorfactor) {
        stopAllAnimation(null);
        toFace(Math.round(mCylinderIndex), l, interpolatorfactor);
    }

    public void toLeftFace(SEAnimFinishListener listener, float interpolatorfactor) {
        stopAllAnimation(null);
        toFace(Math.round(mCylinderIndex) + 1, listener, interpolatorfactor);
    }

    public void toLeftHalfFace(SEAnimFinishListener listener, float interpolatorfactor) {
        stopAllAnimation(null);
        float toFace = mCylinderIndex + 1;
        toFace(Math.round(toFace + 0.5f) - 0.5f, listener, interpolatorfactor);
    }

    public void toRightFace(SEAnimFinishListener listener, float interpolatorfactor) {
        stopAllAnimation(null);
        toFace(Math.round(mCylinderIndex) - 1, listener, interpolatorfactor);
    }

    public void toRightHalfFace(SEAnimFinishListener listener, float interpolatorfactor) {
        stopAllAnimation(null);
        float toFace = mCylinderIndex - 1;
        toFace(Math.round(toFace + 0.5f) - 0.5f, listener, interpolatorfactor);
    }

    public void toFace(float face, final SEAnimFinishListener listener, float interpolatorfactor) {
        stopAllAnimation(null);
        float srcAngle = mCurrentAngle;
        float desAngle = getFaceAngle(face);
        if (mCurrentAngle == desAngle) {
            if (listener != null) {
                listener.onAnimationfinish();
            }
            return;
        }
        int animationTimes = (int) (Math.sqrt(Math.abs(desAngle - srcAngle)) * interpolatorfactor * 3);
        mToFaceAnimation = new SEEmptyAnimation(getScene(), srcAngle, desAngle, animationTimes) {

            @Override
            public void onAnimationRun(float value) {
                setRotate(value, false);
            }

        };
        mToFaceAnimation.setInterpolator(new DecelerateInterpolator(interpolatorfactor));
        mToFaceAnimation.setAnimFinishListener(new SEAnimFinishListener() {
            public void onAnimationfinish() {
                getHomeSceneInfo().updateWallIndex(getWallNearestIndex());
                if (listener != null) {
                    listener.onAnimationfinish();
                }
            }
        });
        mToFaceAnimation.execute();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        trackVelocity(event);
        return super.dispatchTouchEvent(event);
    }

    private boolean mHasGotAction = false;
    private boolean mOnMoveSightToWall = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPreTouch();
            mOnMoveSightToWall = false;
            mPreAction = ev.getAction();
            if (isBusy() || getHomeScene().getStatus(HomeScene.STATUS_ON_SKY_SIGHT)
                    || getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)) {
                if (getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)) {
                    getHomeScene().moveToWallSight(null);
                    mOnMoveSight = false;
                    mHasGotAction = false;
                    mOnMoveSightToWall = true;
                    return true;
                } else if (getHomeScene().getStatus(HomeScene.STATUS_ON_SKY_SIGHT)) {
                    mOnMoveSight = true;
                    mHasGotAction = true;
                }
                stopAllAnimation(null);
                return true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (getHomeScene().getStatus(HomeScene.STATUS_ON_WIDGET_TOUCH)) {
                if (mPreAction == MotionEvent.ACTION_DOWN
                        && Math.abs(getTouchX() - getPreTouchX()) > Math.abs(getTouchY() - getPreTouchY())) {
                    setPreTouch();
                    mHasGotAction = true;
                    mOnMoveSight = false;
                    mPreAction = ev.getAction();
                    return true;
                } else {
                    setPreTouch();
                    mHasGotAction = true;
                    mOnMoveSight = false;
                    mPreAction = ev.getAction();
                    return false;
                }
            }
            setPreTouch();
            mHasGotAction = false;
            stopAllAnimation(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mOnMoveSightToWall) {
            return true;
        }
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            stopAllAnimation(null);
            setPreTouch();
            mCancelClick = false;
            break;
        case MotionEvent.ACTION_MOVE:
            float dY = getTouchY() - getPreTouchY();
            float dX = getTouchX() - getPreTouchX();
            if (!mHasGotAction) {
                if (Math.abs(dY) > 2.2f * Math.abs(dX)) {
                    mOnMoveSight = true;
                }
            }
            mHasGotAction = true;
            if (mOnMoveSight) {
                float skyY = (dY) * 2f / getCamera().getHeight() + getHomeScene().getSightValue();
                if (skyY < -1) {
                    skyY = -1;
                } else if (skyY > 1) {
                    skyY = 1;
                }
                getHomeScene().changeSight(skyY, true);
                mCancelClick = true;
            } else {
                int width = getCamera().getWidth();
                float ratio = mPerFaceAngle * 2 / width;
                float transAngle = ratio * (getTouchX() - getPreTouchX());
                float curAngle = mCurrentAngle - transAngle;
                setRotate(curAngle, false);
            }
            setPreTouch();
            break;
        case MotionEvent.ACTION_UP:
            setPreTouch();
            if (mOnMoveSight) {
                if (!mCancelClick) {
                    mOnMoveSight = false;
                    mHasGotAction = false;
                    getHomeScene().moveToWallSight(null);
                    stopAllAnimation(null);
                    toNearestFace(null, 2);
                    return true;
                }
            }
        case MotionEvent.ACTION_CANCEL:
            if (mOnMoveSight) {
                mOnMoveSight = false;
                if (mVelocityTracker.getYVelocity() > 500) {
                    if (getHomeScene().getSightValue() > 0) {
                        getHomeScene().moveToSkySight(null);
                    } else {
                        getHomeScene().moveToWallSight(null);
                    }
                } else if (mVelocityTracker.getYVelocity() < -500) {
                    if (getHomeScene().getSightValue() < 0) {
                        getHomeScene().moveToDeskSight(null);
                    } else {
                        getHomeScene().moveToWallSight(null);
                    }
                } else {
                    if (getHomeScene().getSightValue() > 0.5f) {
                        getHomeScene().moveToSkySight(null);
                    } else if (getHomeScene().getSightValue() < -0.5f) {
                        getHomeScene().moveToDeskSight(null);
                    } else {
                        getHomeScene().moveToWallSight(null);
                    }
                }
                stopAllAnimation(null);
                toNearestFace(null, 2);
            } else {
                stopAllAnimation(null);
                playVelocityAnimation(mVelocityTracker.getXVelocity() / getHomeScene().getScreenDensity());
            }
            mHasGotAction = false;
            break;
        }
        return true;
    }

    private void playVelocityAnimation(float vX) {
        int toFace;
        if (Math.abs(vX) < 5000) {
            if (vX > 150) {
                if (mCylinderIndex < 0) {
                    toFace = (int) mCylinderIndex;
                } else {
                    toFace = (int) mCylinderIndex + 1;
                }
            } else if (vX < -150) {
                if (mCylinderIndex < 0) {
                    toFace = (int) mCylinderIndex - 1;
                } else {
                    toFace = (int) mCylinderIndex;
                }
            } else {
                toFace = Math.round(mCylinderIndex);
            }
        } else {
            toFace = Math.round(mCylinderIndex + vX / 1666);
        }
        toFace(toFace, null, 3);
    }

    private boolean isBusy() {
        return mToFaceAnimation != null && !mToFaceAnimation.isFinish();
    }

    @Override
    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mToFaceAnimation != null) {
            mToFaceAnimation.stop();
        }
    }

    private boolean performDoubleTap() {
        if (mOnMoveSight) {
            return false;
        }
        float curCameraRadius = getCamera().getRadius();
        float minCameraRadius = getHomeSceneInfo().getCameraMinRadius();
        float maxCameraRadius = getHomeSceneInfo().getCameraMaxRadius();
        float distanceMax = maxCameraRadius - curCameraRadius;
        float distanceMin = curCameraRadius - minCameraRadius;
        if (distanceMax > distanceMin) {
            float endFov = getHomeSceneInfo().getCameraFovByRadius(maxCameraRadius);
            getHomeScene().playSetRadiusAndFovAnim(maxCameraRadius, endFov, null);
        } else {
            float endFov = getHomeSceneInfo().getCameraFovByRadius(minCameraRadius);
            getHomeScene().playSetRadiusAndFovAnim(minCameraRadius, endFov, null);
        }
        return true;
    }

    @Override
    public void onActivityRestart() {
        super.onActivityRestart();
        forceReloadWidget();
    }

    private void forceReloadWidget() {
        new SECommand(getScene()) {
            public void run() {
                List<NormalObject> matchApps = findAPP(null, "Widget");
                for (NormalObject widget : matchApps) {
                    WidgetObject myWidget = (WidgetObject) widget;
                    myWidget.bind();
                }
            }
        }.execute();
    }

    @Override
    public void onActivityDestory() {
        super.onActivityDestory();

    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        boolean showDeskObjectShef = HomeManager.getInstance().getWhetherShowDeskObjectShelf();
        boolean showAppShef = HomeManager.getInstance().getWhetherShowAppShelf();
        if (showDeskObjectShef != mShowDeskObjectShef || showAppShef != mShowAppShef) {
            mShowDeskObjectShef = showDeskObjectShef;
            mShowAppShef = showAppShef;
            SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
            SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
        }
    }

    @Override
    public void onPressHomeKey() {
        super.onPressHomeKey();
        if (hasInit()) {
            int toFace = Math.round(mCylinderIndex / mWallNum) * mWallNum;
            toFace(toFace, null, 3);
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
        LauncherModel.getInstance().setShortcutCallBack(null);
    }

    @Override
    public void shortcutAction(Context context, Intent data) {
        final Intent shortIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

        if (!mHasBeenLoadedFinish || shortIntent == null) {
            return;
        }
        String action = shortIntent.getAction();
        if (!TextUtils.isEmpty(action) && action.equals("android.intent.action.CALL_PRIVILEGED")) {
            action = "android.intent.action.CALL";
            shortIntent.setAction(action);
        }

        final String shortUri = shortIntent.toURI();
        final String title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Bitmap icon = null;
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = (Bitmap) bitmap;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    ShortcutIconResource iconResource = (ShortcutIconResource) extra;
                    Resources resources = getContext().getPackageManager().getResourcesForApplication(
                            iconResource.packageName);
                    int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = BitmapFactory.decodeResource(resources, id);
                } catch (Exception e) {
                }
            }
        }
        if (icon == null || TextUtils.isEmpty(title) || TextUtils.isEmpty(shortUri)) {
            return;
        }
        final Bitmap shortIcon = icon;
        boolean duplicate = data.getBooleanExtra("duplicate", true);
        if (duplicate || !shortcutExists(shortUri)) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final ObjectInfo info = new ObjectInfo();
                    info.mName = "shortcut_" + System.currentTimeMillis();
                    info.mSceneName = getScene().getSceneName();
                    info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
                    info.mType = "Shortcut";
                    info.mObjectSlot.mSpanX = 1;
                    info.mObjectSlot.mSpanY = 1;
                    info.mShortcutUrl = shortUri;
                    info.mDisplayName = title;
                    info.mShortcutIcon = shortIcon;
                    final ShortcutObject shortcutObject = new ShortcutObject(getHomeScene(), info.mName, info.mIndex);
                    shortcutObject.setObjectInfo(info);
                    info.saveToDB(new Runnable() {
                        public void run() {
                            new SECommand(getScene()) {
                                public void run() {
                                    getScene().getContentObject().addChild(shortcutObject, true);
                                    shortcutObject.initStatus();
                                    getVesselLayer().placeObjectToVessel(shortcutObject);
                                    SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                                    SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
                                }
                            }.execute();

                        }
                    });
                }
            });
        } else {
            shortIcon.recycle();
            RemoveExistsShortcut(shortUri);
        }
    }

    private void RemoveExistsShortcut(final String uri) {
        new SECommand(getScene()) {
            public void run() {
                List<NormalObject> newItems = findShortcut(uri);
                for (NormalObject obj : newItems) {
                    removeChild(obj, true);
                }
            }
        }.execute();
    }

    private List<NormalObject> findShortcut(String uri) {
        List<NormalObject> newItems = new ArrayList<NormalObject>();
        for (SEObject item : getChildObjects()) {
            if (item instanceof ShortcutObject) {
                NormalObject appObject = (NormalObject) item;
                if (uri.equals(appObject.getObjectInfo().mShortcutUrl)) {
                    newItems.add(appObject);
                }
            }
        }
        return newItems;
    }

    private boolean shortcutExists(String uri) {
        for (SEObject item : getChildObjects()) {
            if (item instanceof ShortcutObject) {
                NormalObject appObject = (NormalObject) item;
                if (uri.equals(appObject.getObjectInfo().mShortcutUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case HomeScene.REQUEST_CODE_BIND_WIDGET:
            if (data != null) {
                int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                if (data.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    appWidgetId = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                }
                if (resultCode == Activity.RESULT_OK) {
                    setAppWidget(appWidgetId);
                } else {
                    mHomeManager.getAppWidgetHost().deleteAppWidgetId(appWidgetId);
                }
            }
            break;
        case HomeScene.REQUEST_CODE_SELECT_WIDGET:
            if (data != null) {
                int appWidgetId = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                if (resultCode == Activity.RESULT_OK) {
                    finishSetAppWidget(appWidgetId);
                } else {
                    mHomeManager.getAppWidgetHost().deleteAppWidgetId(appWidgetId);
                }
            }
            break;
        case HomeScene.REQUEST_CODE_SELECT_SHORTCUT:
            if (resultCode == Activity.RESULT_OK) {
                shortcutAction(getContext(), data);
            }
            break;
        }
    }

    public CropImageInfo getCropImageInfo() {
        int wallIndex = getHomeScene().getHouse().getWallNearestIndex();
        CropImageInfo cropImageInfo = new CropImageInfo();
        cropImageInfo.mAspectX = mWallPaperCropSizeX;
        cropImageInfo.mAspectY = mWallPaperCropSizeY;
        cropImageInfo.mSaveImageSizeX = mWallPaperSizeX;
        cropImageInfo.mSaveImageSizeY = mWallPaperSizeY;
        cropImageInfo.mImageName = mImageNamesOfAllWall.get(wallIndex);
        cropImageInfo.mTempCropImageFile = new File(HomeUtils.getTmpImageFilePath());
        cropImageInfo.mSaveImagePath = getWallPaperSavePath(wallIndex);
        cropImageInfo.mOnCropImageFinishedListener = House.this;
        return cropImageInfo;
    }

    private void setAppWidget(int appWidgetId) {
        /* Check for configuration */
        AppWidgetProviderInfo providerInfo = mHomeManager.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        if (providerInfo == null) {
            mHomeManager.getAppWidgetHost().deleteAppWidgetId(appWidgetId);
            return;
        }
        if (providerInfo.configure != null) {
            Intent configureIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            configureIntent.setComponent(providerInfo.configure);
            configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            if (configureIntent != null) {
                mHomeManager.startActivityForResult(configureIntent, HomeScene.REQUEST_CODE_SELECT_WIDGET);
            }
        } else {
            finishSetAppWidget(appWidgetId);
        }
    }

    private void finishSetAppWidget(int appWidgetId) {
        final AppWidgetProviderInfo providerInfo = mHomeManager.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        if (providerInfo != null) {
            final ObjectInfo info = new ObjectInfo();
            info.mAppWidgetId = appWidgetId;
            info.mName = "widget_" + System.currentTimeMillis();
            info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
            info.mSceneName = getScene().getSceneName();
            info.mType = "Widget";
            final int[] span = HomeUtils.getSpanForWidget(getContext(), providerInfo);
            info.mObjectSlot.mSpanX = span[0];
            info.mObjectSlot.mSpanY = span[1];
            info.mComponentName = providerInfo.provider;
            final WidgetObject widget = new WidgetObject(getHomeScene(), info.mName, info.mIndex);
            widget.setObjectInfo(info);
            info.saveToDB();
            new SECommand(getScene()) {
                public void run() {
                    final SEObject root = getScene().getContentObject();
                    root.addChild(widget, false);
                    widget.loadMyself(getScene().getContentObject(), new Runnable() {
                        public void run() {
                            if (root.getChildObjects().contains(widget)) {
                                getVesselLayer().placeObjectToVessel(widget);
                                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
                            }
                        }
                    });
                }
            }.execute();

        }
    }
    @Override
    public void onResetImage(final String imageName) {
        String imageNewKey=  getObjectInfo().mModelInfo.getImageNewKey(imageName);
        final String imageOldKey = getObjectInfo().mModelInfo.getImageOldKey(imageName);
        if (imageOldKey == null || imageOldKey.equals(imageNewKey)) {
            return;
        }
        File f = new File(imageNewKey);
        if (f.exists()) {
            f.delete();
        }
        new SECommand(getScene()) {
            public void run() {
                boolean exist = SEObject.isImageExist_JNI(imageOldKey);
                if (exist) {
                    SEObject.applyImage_JNI(imageName, imageOldKey);
                } else {
                    SELoadResThread.getInstance().process(new Runnable() {
                        public void run() {
                            final int image = SEObject.loadImageData_JNI(imageOldKey);
                            new SECommand(getScene()) {
                                public void run() {
                                    SEObject.applyImage_JNI(imageName, imageOldKey);
                                    SEObject.addImageData_JNI(imageOldKey, image);
                                }
                            }.execute();
                        }
                    });
                }
            }
        }.execute();
        updateWallpaperDB(imageName, imageOldKey);
    }

    @Override
    public void onChangeImage(String imageName, String imageNewPath) {
        updateWallpaperDB(imageName, imageNewPath);
    }
    /************************************ wallpaper relates *****************************/

    public void applyWallpaper(final ArrayList<WallPaperItem> wallPaperItems, final String paperId) {
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                ObjectInfo info = getObjectInfo();
                ModelInfo modelInfo = info.mModelInfo;
                int index;
                final int size = wallPaperItems.size();
                for (int i = 0; i < size; i++) {
                    index = (getWallNearestIndex() + i) % size;
                    WallPaperItem wallPaperItem = wallPaperItems.get(index);
                    String imageName = null;
                    if (wallPaperItem.mType == WallPaperItem.WallPaperType.WALLPAPER) {
                        wallPaperItem.mWallPaperImagePath = getWallPaperSavePath(wallPaperItem.mIndex);
                        imageName = mImageNamesOfAllWall.get(wallPaperItem.mIndex);
                    } else if (wallPaperItem.mType == WallPaperItem.WallPaperType.GROUNDPAPER) {
                        wallPaperItem.mWallPaperImagePath = getGroundPaperSavedPath(wallPaperItem.mIndex);
                        imageName = mImageNamesOfGround;
                    }
                    if (imageName != null) {
                        ImageItem imageItem = modelInfo.mImageInfo.getImageItem(imageName);
                        imageItem.mNewPath = wallPaperItem.mWallPaperImagePath;
                        wallPaperItem.processWallPaper(mWallPaperCropSizeX, mWallPaperCropSizeY, mWallPaperSizeX,
                                mWallPaperSizeY);
                        loadImageItem(imageItem);
                        modelInfo.updateImageItem(imageItem);
                    }
                }
                HomeUtils.markPaperAsApply(getContext(), paperId);
            }
        });
    }

    private void loadImageItem(final ModelInfo.ImageItem imageItem) {
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                final int imageData = SEObject.loadImageData_JNI(imageItem.mNewPath);
                if (imageData != 0) {
                    new SECommand(getHomeScene()) {
                        public void run() {
                            SEObject.addImageData_JNI(imageItem.mNewPath, imageData);
                            SEObject.applyImage_JNI(imageItem.mImageName, imageItem.mNewPath);
                        }
                    }.execute();
                }
            }
        });
    }

    public ArrayList<RawPaperItem> exportWallpaper() {
        ArrayList<RawPaperItem> rawPaperItems = new ArrayList<RawPaperItem>();

        ModelInfo modelInfo = getObjectInfo().mModelInfo;

        RawPaperItem item;
        item = new RawPaperItem(mImageNamesOfGround, getPaperPath(modelInfo, mImageNamesOfGround),
                RawPaperItem.TYPE_GROUND, 1, 1);
        rawPaperItems.add(item);

        for (String imageName : mImageNamesOfAllWall) {
            item = new RawPaperItem(imageName, getPaperPath(modelInfo, imageName), RawPaperItem.TYPE_WALL,
                    mWallPaperCropSizeX, mWallPaperCropSizeY);
            rawPaperItems.add(item);
        }

        return rawPaperItems;
    }

    private static String getPaperPath(ModelInfo modelInfo, String imageName) {
        if (null == modelInfo || TextUtils.isEmpty(imageName)) {
            return null;
        }
        String path = modelInfo.getImageNewKey(imageName);
        if (TextUtils.isEmpty(path)) {
            path = modelInfo.getImageOldKey(imageName);
        }

        return path;
    }

    private String getCurrentThemeFolderPath() {
        return getContext().getFilesDir() + File.separator + mCurrentTheme;
    }

    private String getWallPaperSavePath(int wallIndex) {
        return getCurrentThemeFolderPath() + File.separator + "wall_paper_" + wallIndex;
    }

    private String getGroundPaperSavedPath(int groundIndex) {
        return getCurrentThemeFolderPath() + File.separator + "ground_paper_" + groundIndex;
    }

    private void updateWallpaperDB(String imageName, String imageKey) {
        ObjectInfo info = getObjectInfo();
        if (null != info) {
            info.mModelInfo.updateImageKey(imageName, imageKey);
            WallpaperUtils.clearAppliedFlag(getContext());
        }
    }

    /************************************ wallpaper relates end *****************************/
    /**
     * 更新架子
     */
    public void updateShelfWall() {
        if (mShelfManager != null) {
            mShelfManager.updateShelfs();
        }
    }

    /**
     * 强制清除架子
     */
    private void clearShelfWall() {
        if (mShelfManager != null) {
            mShelfManager.clearShelfs();
        }
    }
}
