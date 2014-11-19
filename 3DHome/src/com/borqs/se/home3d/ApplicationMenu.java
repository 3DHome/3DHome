package com.borqs.se.home3d;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;

import com.borqs.se.R;
import com.borqs.se.addobject.AddObjectItemInfo;
import com.borqs.se.addobject.AddRemoveAppDialog.OnAddRemoveListener;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAlphaAnimation;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEEmptyAnimation;
import com.borqs.se.engine.SEImageView;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETextView;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SETranslateAnimation;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.ProviderUtils.AppsDrawerColumns;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.AppCallBack;
import com.borqs.se.widget3d.AppObject;
import com.borqs.se.widget3d.NormalObject;
import com.support.StaticUtil;

public class ApplicationMenu extends SEObject implements AppCallBack, OnAddRemoveListener {

    private static final int CYLINDER_FACE_NUM = 4;
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_RECENTLY_INSTALL = 1;
    public static final int SORT_BY_USED_FREQUENCY = 2;
    public static final float ALPHA_OF_BACKGROUND = 0.88f;
    private int mSortOption = SORT_BY_NAME;

    private List<PreViewFace> mPreViewFaces;
    private List<PreViewObject> mApps;
    private float mFaceAngle;
    private float mFaceRadius;

    private int mFaceNumber;
    private int mAppFaceNum;
    private VelocityTracker mVelocityTracker;
    private float mCurAngle;
    private LauncherModel mModel;
    private OnTouchListener mLongClickListener;
    private boolean mDisableTouch;
    private boolean mHasLoaded;

    private int mIconCount;

    public static final int TYPE_SHOW_KEEP_LAST = 0;
    public static final int TYPE_SHOW_APP = 1;
    public static final int TYPE_BIND_APP = 2;

    private final float SCALE = 0.1f;
    private final float MENU_SCALE = 0.05f;

    private float mCylinderIndex = -1;
    private int mFaceAIndex = -1;
    private int mFaceBIndex = -1;
    private int mCylinderIndexOfFaceA = -1;
    private int mCylinderIndexOfFaceB = -1;
    private DocMenuGroup mDocMenuGroup;

    /**
     * 包含滚筒，以及应用图标组成的面
     */
    private SEObject mRotateMenu;
    /**
     * 滚筒只有在旋转的时候才显示
     */
    private PreViewCylinder mPreViewCylinder;
    private SEImageView mBackground;
    private SEImageView mIndicatorA;
    private SEImageView mIndicatorB;
    private SEImageView mIndicatorEnd;
    private AppSearchPane mAppSearchPane;

    private int mScreenW;
    private int mScreenH;
    private float mScreenDensity;
    private int mAppMenuIconSize;
    private int mAppMenuIconTextSize;
    private int mAppMenuIconPaddingTop;
    private int mAppMenuIconPadding;
    private int mAppsMenuCellCountX;
    private int mAppsMenuCellCountY;
    private float mAppsMenuCellWidth;
    private float mAppsMenuCellHeight;
    private float mAppsMenuWidthGap;
    private float mAppsMenuHeightGap;
    private float mAppsMenuWallWidth;
    private float mAppsMenuWallHeight;
    private float mAppsMenuWallPaddingLeft;
    private float mAppsMenuWallPaddingRight;
    private float mAppsMenuWallPaddingTop;
    private float mAppsMenuWallPaddingBottom;

    private MoreMenuAdapter mMoreMenuAdapter;

    private AlertDialog mSortAppListDialog;
    private HomeScene mHomeScene;
    private boolean mIsFirstLaunch = false;
    private boolean mIsBusy = false;
    private MoreMenuDialog mMoreMenuDialog;
    private NormalObject mNeedSetBindObject;
    private int mCurrentShowType;
    private float mSavedAngle;

    private SEEmptyAnimation mUpdateSceneAnimation;
    private SETranslateAnimation mMoveDockAnimation;
    private SEEmptyAnimation mVAnimation;

    public ApplicationMenu(SEScene scene, String name) {
        super(scene, name);
        setIsGroup(true);
        mHomeScene = (HomeScene) scene;
        mModel = LauncherModel.getInstance();
        mCurAngle = 0;
        mHasLoaded = false;
        mDisableTouch = true;
        mIsBusy = false;

        mIsFirstLaunch = true;
        if (mIsFirstLaunch) {
            SharedPreferences pref = HomeManager.getInstance().getContext()
                    .getSharedPreferences("add_or_hidde_app", Context.MODE_MULTI_PROCESS);
            for (AppItemInfo ii : mModel.getApps()) {
                String componetName = ii.getComponentName().toString();
                boolean shouldShow = pref.getBoolean(componetName, true);
                ii.setShowInPreface(shouldShow);
            }
            mIsFirstLaunch = false;
        }

        setOnLongClickListener(new OnTouchListener() {
            public void run(SEObject obj) {
                hide(true, null);
                PreViewObject preViewObject = (PreViewObject) obj;
                SETransParas startTranspara = new SETransParas();
                startTranspara.mTranslate = obj.getAbsoluteTranslate();
                AppObject itemObject;
                itemObject = AppObject.create(mHomeScene, preViewObject.getItemInfo());
                getScene().getContentObject().addChild(itemObject, true);

                itemObject.initStatus();

                itemObject.setTouch(obj.getTouchX(), obj.getTouchY());
                itemObject.setStartTranspara(startTranspara);
                itemObject.setOnMove(true);
            }
        });
        mScreenW = scene.getSceneWidth();
        mScreenH = scene.getSceneHeight();
        caculateAppsMenuWallSize(mScreenW, mScreenH);
    }

    private SETransParas getBackgroundTransparas() {
        SETransParas transParas = new SETransParas();
        float y = (float) (mAppsMenuWallWidth / (2 * Math.tan(getCamera().getFov() * Math.PI / 360)));
        float actualY = y + 3 * mFaceRadius;
        float scale = actualY * SCALE / y;
        transParas.mTranslate = getCamera().getScreenLocation(scale);
        transParas.mScale.set(scale * mAppsMenuWallWidth, scale, scale * mAppsMenuWallHeight);
        return transParas;
    }

    @Override
    public void onRenderFinish(final SECamera camera) {
        super.onRenderFinish(camera);

        setVisible(false);
        mRotateMenu = new SEObject(getScene(), "RotateMenu");
        mRotateMenu.setIsGroup(true);
        addChild(mRotateMenu, true);
        mRotateMenu.setPressType(PRESS_TYPE.NONE);

        mBackground = new SEImageView(getScene(), "application_background");
        mBackground.setSize(1, 1);
        mBackground.setBackgroundColor(Color.BLACK);
        mBackground.setAlpha(ALPHA_OF_BACKGROUND);
        addChild(mBackground, true);
        mBackground.setBlendSortAxis(AXIS.Y);
        mBackground.setIsEntirety(false);
        mBackground.setClickable(false);
        readSortOptionConfiguration();
        mModel.addAppCallBack(this);
        if (mModel.hasLoadedAllApps()) {
            packagesUpdate(false);
        }
    }

    @Override
    public void notifySurfaceChanged(int width, int height) {
        super.notifySurfaceChanged(width, height);
        if (mScreenW != width || mScreenH != height) {
            mScreenW = width;
            mScreenH = height;
            mExecuteTask.run();
        }
    }

    private Runnable mExecuteTask = new Runnable() {
        public void run() {
            if (hasBeenAddedToEngine() && mHasLoaded) {
                stopAllAnimation(null);
                mDisableTouch = false;
                caculateAppsMenuWallSize(mScreenW, mScreenH);
                mBackground.getUserTransParas().set(getBackgroundTransparas());
                mBackground.setUserTransParas();
                mBackground.setAlpha(ALPHA_OF_BACKGROUND);

                mRotateMenu.getUserTransParas().mScale = new SEVector3f(SCALE, SCALE, SCALE);
                float y = (float) (mAppsMenuWallWidth / (2 * Math.tan(getCamera().getFov() * Math.PI / 360)) + mFaceRadius)
                        * SCALE;
                mRotateMenu.getUserTransParas().mTranslate = new SEVector3f(0, y, 0).add(getCamera().getLocation());
                mCurAngle = getFaceAngle(Math.round(mCylinderIndex));
                mRotateMenu.getUserTransParas().mRotate.set(mCurAngle, 0, 0, 1);
                mRotateMenu.setUserTransParas();
                mRotateMenu.setAlpha(1);

                if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                    setVisible(true);
                    if (mPreViewCylinder == null) {
                        mPreViewCylinder = new PreViewCylinder(getScene(), "PreViewCylinder");
                        mRotateMenu.addChild(mPreViewCylinder, true);
                    }
                    mPreViewCylinder.stopAllAnimation(null);
                    mPreViewCylinder.setUserScale(new SEVector3f(mAppsMenuWallWidth, mAppsMenuWallWidth,
                            mAppsMenuWallHeight));
                    mPreViewCylinder.setAlpha(0);
                    if (mDocMenuGroup == null) {
                        mDocMenuGroup = new DocMenuGroup(getScene(), "DocMenuGroup");
                        addChild(mDocMenuGroup, true);
                    }
                    Resources res = getContext().getResources();
                    int dockSize = res.getDimensionPixelSize(R.dimen.apps_customize_dock_size);
                    SEVector3f yAxis = getCamera().getAxisY();
                    float scale = MENU_SCALE;
                    SEVector3f yTrans = yAxis.mul((dockSize - getCamera().getHeight()) / 2).selfMul(scale);
                    SEVector3f toLocation = getCamera().getScreenLocation(scale).selfAdd(yTrans);
                    mDocMenuGroup.getUserTransParas().mTranslate = toLocation.clone();
                    mDocMenuGroup.getUserTransParas().mScale.set(scale, scale, scale);
                    mDocMenuGroup.setUserTransParas();
                    mDocMenuGroup.setAlpha(1);
                    mDocMenuGroup.updateItemPosition();
                } else {
                    if (mPreViewCylinder != null) {
                        mRotateMenu.removeChild(mPreViewCylinder, true);
                        mPreViewCylinder = null;
                    }
                    if (mDocMenuGroup != null) {
                        removeChild(mDocMenuGroup, true);
                        mDocMenuGroup = null;
                    }
                    setVisible(false);
                }

                updatePosition();
                updateFaceIndex(true);
                if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                    updateIndicator(true);
                }
            }
        }
    };

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            LauncherModel.getInstance().removeAppCallBack(this);
        }
    }

    private SETransParas getIconSlotTransParas(int left, int top) {
        SETransParas transparas = new SETransParas();
        float gridSizeX = mAppsMenuCellWidth + mAppsMenuWidthGap;
        float gridSizeY = mAppsMenuCellHeight + mAppsMenuHeightGap;
        float offsetX = (left + 1 / 2.f) * gridSizeX - mAppsMenuCellCountX * gridSizeX / 2.f
                + (mAppsMenuWallPaddingLeft - mAppsMenuWallPaddingRight) / 2;
        float offsetZ = mAppsMenuCellCountY * gridSizeY / 2.f - (top + 1 / 2.f) * gridSizeY
                + (mAppsMenuWallPaddingBottom - mAppsMenuWallPaddingTop) / 2;
        transparas.mTranslate.set(offsetX, 0, offsetZ);
        return transparas;
    }

    private SETransParas getFaceSlotTransParas(int face) {
        SETransParas transparas = new SETransParas();
        float angle = face * 90;
        SEVector2f direction = new SEVector2f((float) Math.sin(angle * Math.PI / 180), (float) -Math.cos(angle
                * Math.PI / 180));

        float y = (float) (mAppsMenuWallWidth / (2 * Math.tan(getCamera().getFov() * Math.PI / 360)));
        float faceY = y - 0.5f * mFaceRadius;
        float faceScale = faceY / y;
        float faceRadius = 1.5f * mFaceRadius;
        SEVector2f offset = direction.mul(faceRadius);
        transparas.mTranslate.set(offset.getX(), offset.getY(), 0);
        transparas.mRotate.set(angle, 0, 0, 1);
        transparas.mScale.set(faceScale, faceScale, faceScale);
        return transparas;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }

    public void show(final int type, NormalObject needSetBindObject) {
        if (!mHomeScene.getStatus(HomeScene.STATUS_APP_MENU) && mHasLoaded) {
            mNeedSetBindObject = needSetBindObject;
            if (type == TYPE_BIND_APP && mNeedSetBindObject == null) {
                return;
            }
            mCurrentShowType = type;
            mDisableTouch = true;
            stopAllAnimation(null);
            mHomeScene.setStatus(HomeScene.STATUS_APP_MENU, true);
            if (type == TYPE_SHOW_APP) {
                mCurAngle = 0;
            } else if (type == TYPE_SHOW_KEEP_LAST) {
                mCurAngle = mSavedAngle;
            } else if (type == TYPE_BIND_APP) {
                mNeedSetBindObject.getObjectInfo().getCategoryComponentName();
                mCurAngle = 0;
            }

            for (PreViewFace preViewFace : mPreViewFaces) {
                for (SEObject child : preViewFace.getChildObjects()) {
                    PreViewObject preViewObject = (PreViewObject) child;
                    preViewObject.updateStatus();
                    if (type == TYPE_BIND_APP
                            && preViewObject.mItemInfo.getComponentName().equals(
                                    mNeedSetBindObject.getObjectInfo().mComponentName)) {
                        int faceIndex = preViewFace.mFaceIndex;
                        mCurAngle = -faceIndex * mFaceAngle;
                    }
                }
            }
            mHomeScene.moveToWallSight(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mHomeScene.hideDesk(null);
                    setVisible(true);
                    setCubeToRightPosition();
                }
            });
        }
    }

    private void setCubeToRightPosition() {
        stopAllAnimation(null);
        if (getScene() instanceof HomeScene) {
            final SEVector3f fromScale = new SEVector3f(0, 0, 0);
            final SEVector3f toScale = new SEVector3f(SCALE, SCALE, SCALE);
            float y = (float) (mAppsMenuWallWidth / (2 * Math.tan(getCamera().getFov() * Math.PI / 360)) + mFaceRadius)
                    * SCALE;
            final SEVector3f fromLocation = new SEVector3f(0, 0, 0);
            final SEVector3f toLocation = new SEVector3f(0, y, 0).add(getCamera().getLocation());
            mIndexEnd = -1;
            setRotate(mCurAngle);
            updateFaceIndex(true);
            mRotateMenu.getUserTransParas().mTranslate = fromLocation.clone();
            mRotateMenu.getUserTransParas().mRotate.set(mCurAngle, 0, 0, 1);
            mRotateMenu.getUserTransParas().mScale = fromScale.clone();
            mRotateMenu.setUserTransParas();
            mRotateMenu.setAlpha(0);

            mBackground.setAlpha(0);
            mBackground.getUserTransParas().set(getBackgroundTransparas());
            mBackground.setUserTransParas();
            mUpdateSceneAnimation = new SEEmptyAnimation(getScene(), 0, 1, 10) {
                @Override
                public void onAnimationRun(float value) {
                    SEVector3f distanceScale = toScale.subtract(fromScale);
                    SEVector3f scale = fromScale.add(distanceScale.mul(value));
                    mRotateMenu.setUserScale(scale);

                    SEVector3f distanceLocation = toLocation.subtract(fromLocation);
                    SEVector3f location = fromLocation.add(distanceLocation.mul(value));
                    mRotateMenu.setUserTranslate(location);
                    mRotateMenu.setAlpha(value);
                    mBackground.setAlpha(ALPHA_OF_BACKGROUND * value);
                }

            };
            mUpdateSceneAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    Resources res = getContext().getResources();
                    int dockSize = res.getDimensionPixelSize(R.dimen.apps_customize_dock_size);
                    SEVector3f yAxis = getCamera().getAxisY();
                    float scale = MENU_SCALE;
                    SEVector3f yTrans = yAxis.mul((dockSize - getCamera().getHeight()) / 2).selfMul(scale);
                    SEVector3f toLocation = getCamera().getScreenLocation(scale).selfAdd(yTrans);
                    SEVector3f fromLocation = toLocation.add(new SEVector3f(0, 0, -dockSize * scale));
                    mDocMenuGroup = new DocMenuGroup(getScene(), "DocMenuGroup");
                    addChild(mDocMenuGroup, true);
                    mDocMenuGroup.getUserTransParas().mTranslate = fromLocation.clone();
                    mDocMenuGroup.getUserTransParas().mScale.set(scale, scale, scale);
                    mDocMenuGroup.setUserTransParas();
                    mDocMenuGroup.setAlpha(1);
                    mMoveDockAnimation = new SETranslateAnimation(getScene(), mDocMenuGroup, fromLocation, toLocation,
                            4);
                    mMoveDockAnimation.execute();
                    mMoveDockAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            mDisableTouch = false;
                            mPreViewCylinder = new PreViewCylinder(getScene(), "PreViewCylinder");
                            mRotateMenu.addChild(mPreViewCylinder, true);
                            mPreViewCylinder.setUserScale(new SEVector3f(mAppsMenuWallWidth, mAppsMenuWallWidth,
                                    mAppsMenuWallHeight));
                        }
                    });

                }
            });
            AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
            mUpdateSceneAnimation.setInterpolator(accelerateInterpolator);
            mUpdateSceneAnimation.execute();

        }
    }

    public void hide(boolean fast, final SEAnimFinishListener l) {
        if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU) && !mDisableTouch) {
            if (mNeedSetBindObject != null) {
                mNeedSetBindObject = null;
            }
            stopAllAnimation(null);
            mDisableTouch = true;
            final SEVector3f fromScale = mRotateMenu.getUserTransParas().mScale.clone();
            final SEVector3f toScale = new SEVector3f(0, 0, 0);

            final SEVector3f fromLocation = mRotateMenu.getUserTransParas().mTranslate.clone();
            final SEVector3f toLocation = new SEVector3f(0, 0, 0);
            Resources res = getContext().getResources();
            int dockSize = res.getDimensionPixelSize(R.dimen.apps_customize_dock_size);
            final SEVector3f menuFromLocation;
            if (mDocMenuGroup != null) {
                menuFromLocation = mDocMenuGroup.getUserTranslate().clone();
            } else {
                menuFromLocation = new SEVector3f();
            }
            final SEVector3f menuToLocation = menuFromLocation.add(new SEVector3f(0, 0, -dockSize * 0.1f));
            mUpdateSceneAnimation = new SEEmptyAnimation(getScene(), 0, 1, 15) {

                public void onAnimationRun(float value) {
                    SEVector3f scaleDistance = toScale.subtract(fromScale);
                    SEVector3f scale = fromScale.add(scaleDistance.mul(value));
                    mRotateMenu.setUserScale(scale);
                    SEVector3f distanceLocation = toLocation.subtract(fromLocation);
                    SEVector3f location = fromLocation.add(distanceLocation.mul(value));
                    mRotateMenu.setUserTranslate(location);
                    mRotateMenu.setAlpha(1 - value);
                    if (mDocMenuGroup != null) {
                        SEVector3f distance = menuToLocation.subtract(menuFromLocation);
                        SEVector3f nemuLocation = menuFromLocation.add(distance.mul(value));
                        mDocMenuGroup.setUserTranslate(nemuLocation);
                        mDocMenuGroup.setAlpha(1 - value);
                        mBackground.setAlpha(ALPHA_OF_BACKGROUND * (1 - value));
                    }
                }

            };
            mUpdateSceneAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    setVisible(false);
                    if (mCurrentShowType != TYPE_BIND_APP) {
                        mSavedAngle = mCurAngle;
                    }
                    mHomeScene.showDesk(new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            if (l != null) {
                                l.onAnimationfinish();
                            }
                            mDisableTouch = false;
                            mHomeScene.setStatus(HomeScene.STATUS_APP_MENU, false);
                        }
                    });
                    if (mDocMenuGroup != null) {
                        mDocMenuGroup.getParent().removeChild(mDocMenuGroup, true);
                        mDocMenuGroup = null;
                    }
                    if (mPreViewCylinder != null) {
                        mPreViewCylinder.getParent().removeChild(mPreViewCylinder, true);
                        mPreViewCylinder = null;
                    }
                }
            });
            AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
            mUpdateSceneAnimation.setInterpolator(accelerateInterpolator);

            final float fromAngle = mCurAngle;
            final float toAngle = getFaceAngle(Math.round(mCylinderIndex));
            if (fromAngle != toAngle) {
                int animationTimes = (int) (Math.abs(toAngle - fromAngle) / 3);
                if (animationTimes == 0) {
                    animationTimes = 1;
                }
                mVAnimation = new SEEmptyAnimation(getScene(), 0, 1, animationTimes) {
                    @Override
                    public void onAnimationRun(float value) {
                        float distance = toAngle - fromAngle;
                        float angle = fromAngle + distance * value;
                        setRotate(angle);

                    }
                };
                mVAnimation.setInterpolator(new DecelerateInterpolator());
                mVAnimation.setAnimFinishListener(new SEAnimFinishListener() {

                    public void onAnimationfinish() {
                        mUpdateSceneAnimation.execute();
                        if (mPreViewCylinder != null) {
                            mPreViewCylinder.hide();
                        }
                        if (mIndicatorB != null) {
                            mIndicatorB.getParent().removeChild(mIndicatorB, true);
                            mIndicatorB = null;
                        }

                        if (mIndicatorA != null) {
                            mIndicatorA.getParent().removeChild(mIndicatorA, true);
                            mIndicatorA = null;
                        }

                        if (mIndicatorEnd != null) {
                            mIndicatorEnd.getParent().removeChild(mIndicatorEnd, true);
                            mIndicatorEnd = null;
                        }

                    }
                });
                mVAnimation.execute();
            } else {
                mUpdateSceneAnimation.execute();
                if (mIndicatorB != null) {
                    mIndicatorB.getParent().removeChild(mIndicatorB, true);
                    mIndicatorB = null;
                }
                if (mIndicatorA != null) {
                    mIndicatorA.getParent().removeChild(mIndicatorA, true);
                    mIndicatorA = null;
                }

                if (mIndicatorEnd != null) {
                    mIndicatorEnd.getParent().removeChild(mIndicatorEnd, true);
                    mIndicatorEnd = null;
                }
            }

        }
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
            hide(false, l);
            return true;
        }
        return false;
    }

    private void setRotate(float angle) {
        mCurAngle = angle;
        updateFaceIndex(false);
        updateScale();
        updateIndicator(false);
        mRotateMenu.setUserRotate(new SERotate(mCurAngle, 0, 0, 1));
    }

    private void updateScale() {
        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
        float input = Math.abs(Math.abs(mCylinderIndex) % 1 - 0.5f) * 2;
        float output = accelerateInterpolator.getInterpolation(input);
        float scalePara = output * 0.2f + 0.8f;
        SEVector3f toScale = new SEVector3f(scalePara * SCALE, scalePara * SCALE, scalePara * SCALE);
        mRotateMenu.setUserScale(toScale);
    }

    private void updateFaceIndex(boolean force) {
        mCylinderIndex = -mCurAngle / mFaceAngle;
        int cylinderIndexOfFaceA;
        int faceAIndex;
        int cylinderIndexOfFaceB;
        int faceBIndex;
        if (mCylinderIndex < 0) {
            cylinderIndexOfFaceA = (int) mCylinderIndex - 1;
            faceAIndex = mFaceNumber + cylinderIndexOfFaceA % mFaceNumber;
            cylinderIndexOfFaceB = (int) mCylinderIndex;
            faceBIndex = mFaceNumber + cylinderIndexOfFaceB % mFaceNumber;
            faceAIndex = faceAIndex % mFaceNumber;
            faceBIndex = faceBIndex % mFaceNumber;
        } else {
            cylinderIndexOfFaceA = (int) mCylinderIndex;
            faceAIndex = cylinderIndexOfFaceA % mFaceNumber;
            cylinderIndexOfFaceB = ((int) mCylinderIndex + 1);
            faceBIndex = cylinderIndexOfFaceB % mFaceNumber;
        }
        if (faceAIndex != mFaceAIndex || faceBIndex != mFaceBIndex || cylinderIndexOfFaceA != mCylinderIndexOfFaceA
                || cylinderIndexOfFaceB != mCylinderIndexOfFaceB || force) {
            mFaceAIndex = faceAIndex;
            mFaceBIndex = faceBIndex;
            mCylinderIndexOfFaceA = cylinderIndexOfFaceA;
            mCylinderIndexOfFaceB = cylinderIndexOfFaceB;
            for (PreViewFace preViewFace : mPreViewFaces) {
                if (preViewFace.mFaceIndex == mFaceAIndex) {
                    preViewFace.getUserTransParas().set(getFaceSlotTransParas(mCylinderIndexOfFaceA));
                    preViewFace.setUserTransParas();
                    preViewFace.setVisible(true);
                } else if (preViewFace.mFaceIndex == mFaceBIndex) {
                    preViewFace.getUserTransParas().set(getFaceSlotTransParas(mCylinderIndexOfFaceB));
                    preViewFace.setUserTransParas();
                    preViewFace.setVisible(true);
                } else {
                    preViewFace.setVisible(false);
                }

            }
        }
    }

    private int mIndexEnd = -1;

    private void updateIndicator(boolean force) {
        if (force) {
            if (mIndicatorB != null) {
                mIndicatorB.getParent().removeChild(mIndicatorB, true);
                mIndicatorB = null;
            }

            if (mIndicatorA != null) {
                mIndicatorA.getParent().removeChild(mIndicatorA, true);
                mIndicatorA = null;
            }

            if (mIndicatorEnd != null) {
                mIndicatorEnd.getParent().removeChild(mIndicatorEnd, true);
                mIndicatorEnd = null;
            }

        }
        if (mIndicatorA == null) {
            mIndicatorA = new SEImageView(getScene(), "application_indicator");
            mIndicatorEnd = new SEImageView(getScene(), "application_indicator_end");
            int sceneW = getCamera().getWidth();
            int sceneH = getCamera().getHeight();
            int w = sceneW / mFaceNumber;
            int h = (int) (8 * getScene().getScreenDensity());
            mIndicatorA.setBackground(R.drawable.indicator_slide_scroll);
            mIndicatorEnd.setBackground(R.drawable.indicator_slide_scroll_end);
            mIndicatorA.setSize(w, h);

            mIndicatorEnd.setSize(w, h);
            addChild(mIndicatorA, true);
            addChild(mIndicatorEnd, true);
            mIndicatorA.setLocalScale(new SEVector3f(MENU_SCALE, MENU_SCALE, MENU_SCALE));
            mIndicatorEnd.setLocalScale(new SEVector3f(MENU_SCALE, MENU_SCALE, MENU_SCALE));
            SEVector3f location = getCamera().getScreenLocation(MENU_SCALE);
            location.selfAdd(new SEVector3f((w - sceneW) / 2f, 0, (sceneH - h) / 2).mul(MENU_SCALE));
            mIndicatorA.setLocalTranslate(location);
            mIndicatorEnd.setLocalTranslate(location.add(new SEVector3f(0, MENU_SCALE * 2, MENU_SCALE * 0.5f)));
            mIndicatorA.cloneObject_JNI(this, 1);
            mIndicatorB = new SEImageView(getScene(), "application_indicator", 1);
            mIndicatorB.setHasBeenAddedToEngine(true);
            addChild(mIndicatorB, false);
            mIndicatorA.setBlendSortAxis(AXIS.Y);
            mIndicatorB.setBlendSortAxis(AXIS.Y);
            mIndicatorEnd.setBlendSortAxis(AXIS.Y);
            mIndicatorEnd.setAlpha(0);
        }
        float index;

        if (mCylinderIndex < 0) {
            index = mFaceNumber + mCylinderIndex % mFaceNumber;
        } else {
            index = mCylinderIndex % mFaceNumber;
        }
        int sceneW = getCamera().getWidth();
        float indicatorW = MENU_SCALE * sceneW / mFaceNumber;
        mIndicatorA.setUserTranslate(new SEVector3f(indicatorW * index, 0, 0));
        mIndicatorB.setUserTranslate(new SEVector3f(indicatorW * (index - mFaceNumber), 0, 0));

        int indexEnd;
        if (Math.abs(mCylinderIndex % 1) <= 0.1f) {
            indexEnd = Math.round(mCylinderIndex) % mFaceNumber;
            if (indexEnd < 0) {
                indexEnd = mFaceNumber + indexEnd;
            }
            if (mIndexEnd != indexEnd || force) {
                mIndexEnd = indexEnd;
                mIndicatorEnd.setUserTranslate(new SEVector3f(indicatorW * mIndexEnd, 0, 0));
            }
        }
        if (mIndexEnd == 0) {
            if (index > mFaceNumber - 1) {
                mIndicatorEnd.setUserRotate(new SERotate(180, 0, 0, 1));
                mIndicatorEnd.setAlpha(index - mFaceNumber + 1);
            } else {
                mIndicatorEnd.setUserRotate(new SERotate(0, 0, 0, 1));
                mIndicatorEnd.setAlpha(1 - Math.abs(-index));
            }

        } else {
            if (index > mIndexEnd) {
                mIndicatorEnd.setUserRotate(new SERotate(0, 0, 0, 1));
            } else {
                mIndicatorEnd.setUserRotate(new SERotate(180, 0, 0, 1));
            }
            mIndicatorEnd.setAlpha(1 - Math.abs(mIndexEnd - index));
        }

    }

    private float getFaceAngle(float index) {
        float to = -index * mFaceAngle;
        return to;
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDisableTouch) {
            return true;
        }
        trackVelocity(event);
        return super.dispatchTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPreTouch();
            if (mVAnimation != null) {
                mVAnimation.stop();
            }
            if (isBusy()) {
                return true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (Math.abs(getTouchX() - getPreTouchX()) > getTouchSlop() / 2) {
                if (mVAnimation != null) {
                    mVAnimation.stop();
                }
                return true;
            }
            break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPreTouch();
            break;
        case MotionEvent.ACTION_MOVE:
            mIsBusy = true;
            if (mPreViewCylinder != null) {
                mPreViewCylinder.show();
            }
            int width = getCamera().getWidth();
            float ratio = mFaceAngle / width;
            float transAngle = ratio * (getTouchX() - getPreTouchX());
            float curAngle = mCurAngle + transAngle;
            setRotate(curAngle);
            setPreTouch();
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            setPreTouch();
            if (mIsBusy) {
                float srcAngle = mCurAngle;
                float desAngle;
                if (mVelocityTracker.getXVelocity() > 500) {
                    if (mCylinderIndex < 0) {
                        desAngle = getFaceAngle((int) mCylinderIndex - 1);
                    } else {
                        desAngle = getFaceAngle((int) mCylinderIndex);
                    }
                } else if (mVelocityTracker.getXVelocity() < -500) {
                    if (mCylinderIndex < 0) {
                        desAngle = getFaceAngle((int) mCylinderIndex);
                    } else {
                        desAngle = getFaceAngle((int) mCylinderIndex + 1);
                    }
                } else {
                    desAngle = getFaceAngle(Math.round(mCylinderIndex));
                }
                final float fromAngle = srcAngle;
                final float toAngle = desAngle;
                int animationTimes = (int) (Math.sqrt(Math.abs(toAngle - fromAngle)) * 1.5f);
                mVAnimation = new SEEmptyAnimation(getScene(), 0, 1, animationTimes) {
                    @Override
                    public void onAnimationRun(float value) {
                        float distance = toAngle - fromAngle;
                        float angle = fromAngle + distance * value;
                        setRotate(angle);

                    }
                };
                mVAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
                mVAnimation.setAnimFinishListener(new SEAnimFinishListener() {

                    public void onAnimationfinish() {
                        if (mPreViewCylinder != null) {
                            mPreViewCylinder.hide();
                        }
                        mIsBusy = false;
                    }
                });
                mVAnimation.execute();
            }
            break;
        }
        return true;
    }

    @Override
    public void setOnLongClickListener(OnTouchListener l) {
        mLongClickListener = l;
    }

    @Override
    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mUpdateSceneAnimation != null) {
            mUpdateSceneAnimation.stop();
        }
        if (mMoveDockAnimation != null) {
            mMoveDockAnimation.stop();
        }
        if (mVAnimation != null) {
            mVAnimation.stop();
        }
    }

    private boolean isBusy() {
        return mIsBusy;

    }

    private class PreViewCylinder extends SEObject {
        private boolean mHasShowed;
        private SEAlphaAnimation mShowAnimation;

        public PreViewCylinder(SEScene scene, String name) {
            super(scene, name);
            setIsGroup(true);
            mHasShowed = false;
            setPressType(PRESS_TYPE.NONE);
        }

        @Override
        public void stopAllAnimation(SEAnimFinishListener l) {
            if (mShowAnimation != null) {
                mShowAnimation.stop();
            }
        }

        public void show() {
            if (!mHasShowed) {
                mHasShowed = true;
                if (mShowAnimation != null) {
                    mShowAnimation.stop();
                }
                setVisible(true);
                setAlpha(0);
                float fromAlpha = 0;
                float toAlpha = 1;
                mShowAnimation = new SEAlphaAnimation(getScene(), this, fromAlpha, toAlpha, 12);
                mShowAnimation.execute();
            }
        }

        public void hide() {
            if (mHasShowed) {
                mHasShowed = false;
                if (mShowAnimation != null) {
                    mShowAnimation.stop();
                }
                float fromAlpha = getAlpha();
                float toAlpha = 0;
                mShowAnimation = new SEAlphaAnimation(getScene(), this, fromAlpha, toAlpha, 6);
                mShowAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                    public void onAnimationfinish() {
                        setVisible(false);
                    }
                });
                mShowAnimation.execute();

            }

        }

        @Override
        public void onRenderFinish(SECamera camera) {
            setVisible(false);
            SEImageView faceBackground = new SEImageView(getScene(), "faceBackground");
            faceBackground.setSize(1, 1);
            faceBackground.setBackground(R.drawable.appdrawer_background);
            float angle = 0 * 90;
            SEVector2f direction = new SEVector2f((float) Math.sin(angle * Math.PI / 180), (float) -Math.cos(angle
                    * Math.PI / 180));
            float faceRadius = (float) (1 / (2 * Math.tan(90 * Math.PI / 360)));
            SEVector2f offset = direction.mul(faceRadius);
            faceBackground.getUserTransParas().mTranslate.set(offset.getX(), offset.getY(), 0);
            faceBackground.getUserTransParas().mRotate.set(angle, 0, 0, 1);
            faceBackground.getUserTransParas().mScale.set(1, 1, 1);
            faceBackground.setIsEntirety(false);
            addChild(faceBackground, true);
            for (int i = 1; i < 4; i++) {
                faceBackground.cloneObject_JNI(this, i);
                SEObject clone = new SEObject(getScene(), "faceBackground", i);
                clone.setHasBeenAddedToEngine(true);
                angle = i * 90;
                direction = new SEVector2f((float) Math.sin(angle * Math.PI / 180), (float) -Math.cos(angle * Math.PI
                        / 180));
                faceRadius = (float) (1 / (2 * Math.tan(90 * Math.PI / 360)));
                offset = direction.mul(faceRadius);
                clone.getUserTransParas().mTranslate.set(offset.getX(), offset.getY(), 0);
                clone.getUserTransParas().mRotate.set(angle, 0, 0, 1);
                clone.getUserTransParas().mScale.set(1, 1, 1);
                clone.setUserTransParas();
                addChild(clone, false);
            }
            setBlendSortAxis(AXIS.Y);
            setIsEntirety(false);
        }
    }

    private class PreViewFace extends SEObject {
        public int mFaceIndex;

        public PreViewFace(SEScene scene, String name) {
            super(scene, name);
            setIsGroup(true);
        }

    }

    public class PreViewObject extends SEObject {
        private ItemInfo mItemInfo;
        public int mFaceIndex;
        public int mLeft;
        public int mTop;
        public SEObject mIcon;
        private SEImageView mTickIcon;

        private PreViewObject(SEScene scene, String name, ItemInfo itemInfo, int w, int h) {
            super(scene, name);
            setIsGroup(true);
            setPressType(PRESS_TYPE.COLOR);
            mIcon = new SEObject(scene, name + "_icon") {
                @Override
                public Bitmap onStartLoadImage() {
                    Bitmap bitmap = getAppMenuIconBitmap(mItemInfo.getIcon(), mItemInfo.getLabel());
                    return bitmap;
                }

            };
            String imageName = mIcon.mName + "_imageName";
            String imagekey = mIcon.mName + "_imageKey";
            SERect3D rect = new SERect3D(w, h);
            SEObjectFactory.createRectangle(mIcon, rect, IMAGE_TYPE.BITMAP, imageName, imagekey, null, null, true);
            mIcon.setNeedCullFace(true);
            float scale = 1;
            int imageW = w;
            int imageH = h;
            if (w > 128) {
                scale = 128f / imageW;
                imageH = (int) (imageH * scale);
                imageW = 128;
            }
            mIcon.setImageValidAreaSize(imageW, imageH);
            mItemInfo = itemInfo;
        }

        @Override
        public void onRenderFinish(SECamera camera) {
            super.onRenderFinish(camera);
            addChild(mIcon, true);
            mIcon.setIsEntirety(false);
            updateStatus();
        }

        private void showTickIcon(boolean show) {
            if (show) {
                if (mTickIcon == null) {
                    mTickIcon = new SEImageView(getScene(), mName + "_nick");
                    mTickIcon.setBackground(R.drawable.tick_icon);
                    mTickIcon.setSize(mAppMenuIconSize * 0.6f, mAppMenuIconSize * 0.6f);
                    addChild(mTickIcon, true);
                    float z = mAppsMenuCellHeight / 2 - mAppMenuIconPaddingTop - mAppMenuIconSize / 6;
                    float x = mAppMenuIconSize / 3;
                    mTickIcon.setUserTranslate(new SEVector3f(x, -10, z));
                    mTickIcon.setBlendSortAxis(AXIS.Y);
                    mTickIcon.setIsEntirety(false);
                }
            } else {
                if (mTickIcon != null) {
                    removeChild(mTickIcon, true);
                    mTickIcon = null;
                }
            }
        }

        public void updateStatus() {
            if (mCurrentShowType != TYPE_BIND_APP) {
                showTickIcon(false);
                setOnLongClickListener(new OnTouchListener() {
                    public void run(SEObject obj) {
                        if (mCurrentShowType != TYPE_BIND_APP) {
                            obj.setPressed(false);
                            if (mLongClickListener != null) {
                                mLongClickListener.run(obj);
                            }
                        }
                    }
                });

                setOnClickListener(new OnTouchListener() {
                    public void run(SEObject obj) {
                        final PreViewObject preViewObject = (PreViewObject) obj;
                        final Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setComponent(preViewObject.getItemInfo().getComponentName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        HomeManager.getInstance().startActivity(intent);
                        preViewObject.getItemInfo().mLaunchCount++;
                        UpdateDBThread.getInstance().process(new Runnable() {
                            public void run() {
                                String where = AppsDrawerColumns.COMPONENTNAME + "='"
                                        + preViewObject.getItemInfo().getComponentName().toShortString() + "'";
                                ContentValues values = new ContentValues();
                                values.put(AppsDrawerColumns.LAUNCHERCOUNT, preViewObject.getItemInfo().mLaunchCount);
                                getContext().getContentResolver().update(AppsDrawerColumns.CONTENT_URI, values, where,
                                        null);

                            }
                        });

                    }
                });
            } else {
                if (mNeedSetBindObject != null
                        && mItemInfo.getComponentName().equals(mNeedSetBindObject.getObjectInfo().mComponentName)) {
                    showTickIcon(true);
                } else {
                    showTickIcon(false);
                }
                setOnLongClickListener(null);
                setOnClickListener(new OnTouchListener() {
                    public void run(SEObject obj) {
                        if (mNeedSetBindObject != null) {
                            mNeedSetBindObject.updateComponentName(mItemInfo.getComponentName());
                            mNeedSetBindObject = null;
                            hide(true, null);
                        }
                    }
                });
            }
        }

        public void update() {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    Bitmap bitmap = getAppMenuIconBitmap(mItemInfo.getIcon(), mItemInfo.getLabel());
                    final int imageData = loadImageData_JNI(bitmap);
                    bitmap.recycle();
                    new SECommand(getScene()) {
                        public void run() {
                            String imagekey = mIcon.getName() + "_imageKey";
                            addImageData_JNI(imagekey, imageData);
                        }
                    }.execute();
                }
            });
        }

        private Bitmap getAppMenuIconBitmap(Drawable icon, String title) {
            int bitmapW = (int) mAppsMenuCellWidth;
            int bitmapH = (int) mAppsMenuCellHeight;
            float scale = 1;
            if (bitmapW > 128) {
                scale = 128f / bitmapW;
                bitmapH = (int) (bitmapH * scale);
                bitmapW = 128;

            }
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.density = mScreenDensity;
            float fontScale = HomeUtils.getFontSize(getContext());
            textPaint.setTextSize(mAppMenuIconTextSize * fontScale * scale);
            textPaint.setColor(Color.WHITE);
            textPaint.setShadowLayer(mScreenDensity * 2 * scale, mScreenDensity * scale, mScreenDensity * scale,
                    Color.BLACK);
            int newW = SEUtils.higherPower2(bitmapW);
            int newH = SEUtils.higherPower2(bitmapH);
            Bitmap preview = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(preview);
            canvas.translate((newW - bitmapW) * 0.5f, (newH - bitmapH) * 0.5f);

            int iconSize = (int) (mAppMenuIconSize * scale);
            int iconLeft = (int) ((bitmapW - iconSize) / 2);
            int iconTop = (int) (mAppMenuIconPaddingTop * scale);
            try {
                Rect oldBounds = icon.copyBounds();
                icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                icon.draw(canvas);
                icon.setBounds(oldBounds); // Restore the bounds
            } catch (Exception e) {
                StaticUtil.reportError(getContext(), "draw icon failed:" + mItemInfo.getComponentName().getClassName());
            }
            String label = title;

            StaticLayout titleLayout = new StaticLayout(label, textPaint, bitmapW, Alignment.ALIGN_CENTER, 1f, 0.0F,
                    false);
            int lineCount = titleLayout.getLineCount();
            if (lineCount > 2) {
                int index = titleLayout.getLineEnd(1);
                if (index > 0) {
                    label = label.substring(0, index);
                    titleLayout = new StaticLayout(label, textPaint, bitmapW, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
                }
            }
            float left = 0;
            float top = iconSize + iconTop + mAppMenuIconPadding * scale;
            canvas.save();
            canvas.translate(left, top);
            titleLayout.draw(canvas);
            canvas.restore();
            return preview;
        }

        public ItemInfo getItemInfo() {
            return mItemInfo;
        }
    }

    private void forceReload(List<ItemInfo> appsAll) {
        List<ItemInfo> apps = new ArrayList<ItemInfo>();// should show in
                                                        // prefaces app
        for (int i = 0; i < appsAll.size(); i++) {
            if (appsAll.get(i).isShowInPreface()) {
                apps.add(appsAll.get(i));
            }
        }

        if (mDocMenuGroup != null && mCurrentShowType == TYPE_BIND_APP) {
            mDocMenuGroup.updateText();
        }
        if (mPreViewFaces != null) {
            for (PreViewFace face : mPreViewFaces) {
                face.getParent().removeChild(face, true);
            }
        }
        mPreViewFaces = new ArrayList<PreViewFace>();
        mApps = new ArrayList<PreViewObject>();
        mAppFaceNum = (int) ((apps.size() + mIconCount - 1) / mIconCount);
        mFaceNumber = mAppFaceNum < 2 ? 2 : mAppFaceNum;

        for (int i = 0; i < mFaceNumber; i++) {
            PreViewFace preViewFace = new PreViewFace(getScene(), "preViewFace_" + i);
            preViewFace.mFaceIndex = i;
            mRotateMenu.addChild(preViewFace, true);
            mPreViewFaces.add(preViewFace);
        }
        int size = apps.size();
        for (int i = 0; i < size; i++) {
            ItemInfo appInfo = apps.get(i);
            String objName = appInfo.getPreviewName();
            PreViewObject obj = new PreViewObject(getScene(), objName, appInfo, (int) mAppsMenuCellWidth,
                    (int) mAppsMenuCellHeight);
            mApps.add(obj);
        }

        updatePosition();
        updateFaceIndex(true);
        if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
            updateIndicator(true);
        }
        setBlendSortAxis(AXIS.Y);
    }

    private void updatePosition() {
        Collections.sort(mApps, new SortByType());
        mAppFaceNum = (int) ((mApps.size() + mIconCount - 1) / mIconCount);
        int faceNumber = mAppFaceNum < 2 ? 2 : mAppFaceNum;
        if (faceNumber > mFaceNumber) {
            for (int i = mFaceNumber; i < faceNumber; i++) {
                PreViewFace preViewFace = new PreViewFace(getScene(), "preViewFace_" + i);
                preViewFace.mFaceIndex = i;
                mRotateMenu.addChild(preViewFace, true);
                mPreViewFaces.add(preViewFace);
            }
            mFaceNumber = faceNumber;
        }

        int size = mApps.size();
        for (int i = 0; i < size; i++) {
            PreViewObject obj = mApps.get(i);
            obj.mFaceIndex = i / mIconCount;
            int inFacePosition = i - mIconCount * obj.mFaceIndex;
            obj.mLeft = inFacePosition % mAppsMenuCellCountX;
            obj.mTop = inFacePosition / mAppsMenuCellCountX;

            SEObject parent = mPreViewFaces.get(obj.mFaceIndex);
            if (obj.getParent() == parent) {
                obj.getUserTransParas().set(getIconSlotTransParas(obj.mLeft, obj.mTop));
                obj.setUserTransParas();
            } else if (obj.getParent() != null) {
                obj.getUserTransParas().set(getIconSlotTransParas(obj.mLeft, obj.mTop));
                obj.changeParent(parent);
                obj.setUserTransParas();
            } else {
                parent.addChild(obj, true);
                obj.getUserTransParas().set(getIconSlotTransParas(obj.mLeft, obj.mTop));
                obj.setUserTransParas();
            }
        }

        if (faceNumber < mFaceNumber) {
            for (int i = faceNumber; i < mFaceNumber; i++) {
                PreViewFace preViewFace = mPreViewFaces.get(i);
                preViewFace.getParent().removeChild(preViewFace, true);
                mPreViewFaces.remove(preViewFace);
                mFaceNumber--;
                i--;
            }
        }

    }

    public void addApps(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        if (mSortOption == SORT_BY_USED_FREQUENCY) {
            getLauncherCount(myApps, new Runnable() {
                public void run() {
                    new SECommand(getScene()) {
                        public void run() {
                            if (!mHasLoaded) {
                                return;
                            }
                            for (ItemInfo itemInfo : myApps) {
                                switch (itemInfo.mItemType) {
                                case ItemInfo.ITEM_TYPE_APP:
                                    String objNameApp = itemInfo.getPreviewName();
                                    PreViewObject app = new PreViewObject(getScene(), objNameApp, itemInfo,
                                            (int) mAppsMenuCellWidth, (int) mAppsMenuCellHeight);
                                    mApps.add(app);
                                    break;
                                }
                            }
                            updatePosition();
                            updateFaceIndex(true);
                            if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                                updateIndicator(true);
                            }
                        }
                    }.execute();

                }

            });
        } else {
            new SECommand(getScene()) {
                public void run() {
                    if (!mHasLoaded) {
                        return;
                    }
                    for (ItemInfo itemInfo : myApps) {
                        switch (itemInfo.mItemType) {
                        case ItemInfo.ITEM_TYPE_APP:
                            String objNameApp = itemInfo.getPreviewName();
                            PreViewObject app = new PreViewObject(getScene(), objNameApp, itemInfo,
                                    (int) mAppsMenuCellWidth, (int) mAppsMenuCellHeight);
                            mApps.add(app);
                            break;
                        }
                    }
                    updatePosition();
                    updateFaceIndex(true);
                    if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                        updateIndicator(true);
                    }
                }
            }.execute();
        }
    }

    public void removeApps(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        new SECommand(getScene()) {
            public void run() {
                if (!mHasLoaded) {
                    return;
                }
                for (ItemInfo info : myApps) {
                    Iterator<PreViewObject> iterator;
                    if (info.mItemType == ItemInfo.ITEM_TYPE_APP) {
                        iterator = mApps.iterator();
                        while (iterator.hasNext()) {
                            final PreViewObject app = iterator.next();
                            if (app.getItemInfo().equals(info)) {
                                iterator.remove();
                                app.getParent().removeChild(app, true);
                            }
                        }
                    }
                }
                updatePosition();
                updateFaceIndex(true);
                if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                    updateIndicator(true);
                }
            }
        }.execute();
    }

    public void updateApps(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        mModel.wait(new Runnable() {
            public void run() {
                new SECommand(getScene()) {
                    public void run() {
                        if (!mHasLoaded) {
                            return;
                        }
                        for (ItemInfo info : myApps) {
                            Iterator<PreViewObject> iterator;
                            if (info.mItemType == ItemInfo.ITEM_TYPE_APP) {
                                iterator = mApps.iterator();
                                while (iterator.hasNext()) {
                                    final PreViewObject app = iterator.next();
                                    if (app.getItemInfo().equals(info)) {
                                        app.getItemInfo().setResolveInfo(info.getResolveInfo());
                                        app.update();
                                    }
                                }
                            }
                        }
                        updatePosition();
                        updateFaceIndex(true);
                        if (mHomeScene.getStatus(HomeScene.STATUS_APP_MENU)) {
                            updateIndicator(true);
                        }
                    }
                }.execute();
            }
        }, 500);
    }

    public void packagesUpdate(final boolean force) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(mModel.getApps());

        if (mSortOption == SORT_BY_USED_FREQUENCY) {
            getLauncherCount(myApps, new Runnable() {
                public void run() {
                    new SECommand(getScene()) {
                        public void run() {
                            if (force || !mHasLoaded) {
                                forceReload(myApps);
                                mHasLoaded = true;
                            }
                        }
                    }.execute();

                }

            });
        } else {
            new SECommand(getScene()) {
                public void run() {
                    if (force || !mHasLoaded) {
                        forceReload(myApps);
                        mHasLoaded = true;
                    }
                }
            }.execute();
        }
    }

    @Override
    public void bindAppsAdded(List<ItemInfo> apps) {
        addApps(apps);

    }

    @Override
    public void bindAppsUpdated(List<ItemInfo> apps) {
        updateApps(apps);

    }

    @Override
    public void bindAppsRemoved(List<ItemInfo> apps) {
        removeApps(apps);

    }

    @Override
    public void bindAllPackagesUpdated() {
        packagesUpdate(true);

    }

    @Override
    public void bindAllPackages() {
        packagesUpdate(false);

    }

    @Override
    public void bindUnavailableApps(List<ItemInfo> apps) {
        removeApps(apps);
    }

    @Override
    public void bindAvailableApps(List<ItemInfo> apps) {
        addApps(apps);
    }

    private class DocMenuGroup extends SEObject {
        SEImageView mMenuA;
        SEImageView mMenuB;
        SEImageView mMenuMore;
        SETextView mTextView;

        public DocMenuGroup(SEScene scene, String name) {
            super(scene, name);
            setIsGroup(true);
        }

        public void updateText() {
            // 当绑定应用状态下需要显示“选择绑定的应用程序“
            if (mTextView != null) {
                removeChild(mTextView, true);
            }
            int screenW = getCamera().getWidth();
            String title = getContext().getResources().getString(R.string.select_app);
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.density = getScene().getScreenDensity();
            textPaint.setTextSize(textPaint.density * 18);
            textPaint.setColor(Color.WHITE);
            textPaint.setFakeBoldText(true);
            mTextView = new SETextView(getScene(), "DocMenu_D", textPaint);
            mTextView.setText(title);
            mTextView.autoCalculateSize(screenW - mAppsMenuWallPaddingLeft - mAppsMenuWallPaddingRight);
            addChild(mTextView, true);
        }

        public void updateItemPosition() {
            if (mMenuA != null)
                mMenuA.setUserTranslate(new SEVector3f(-mScreenW * 3 / 8, -10, 0));
            if (mMenuB != null)
                mMenuB.setUserTranslate(new SEVector3f(0, -10, 0));
            if (mMenuMore != null)
                mMenuMore.setUserTranslate(new SEVector3f(mScreenW * 3 / 8, -10, 0));
        }

        @Override
        public void onRenderFinish(SECamera camera) {
            if (mCurrentShowType == TYPE_BIND_APP) {
                updateText();
            } else {
                Resources res = getContext().getResources();
                int dockSize = res.getDimensionPixelSize(R.dimen.apps_customize_dock_size);
                int screenW = mScreenW;
                mMenuA = new SEImageView(getScene(), "DocMenu_A");
                mMenuA.setBackground(R.drawable.dock_search_normal);
                mMenuA.setSize(dockSize, dockSize);
                addChild(mMenuA, true);
                mMenuA.setUserTranslate(new SEVector3f(-screenW * 3 / 8, -10, 0));
                mMenuA.setOnClickListener(new OnTouchListener() {
                    @Override
                    public void run(SEObject obj) {
                        SESceneManager.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mAppSearchPane != null) {
                                    HomeManager.getInstance().getWorkSpace().removeView(mAppSearchPane);
                                    mAppSearchPane = null;
                                }
                                mAppSearchPane = new AppSearchPane(HomeManager.getInstance().getContext());
                                if (mAppSearchPane != null) {
                                    HomeManager.getInstance().getWorkSpace().addView(mAppSearchPane);
                                    HomeManager.getInstance().setAppSearchPane(mAppSearchPane);
                                    mAppSearchPane.setVisibility(View.VISIBLE);
                                    mAppSearchPane.invalidate();
                                    mAppSearchPane.setItemLongClickListener(new AppSearchPaneItemLongClickListener());
                                }
                            }
                        });
                    }
                });

                mMenuB = new SEImageView(getScene(), "DocMenu_B");
                mMenuB.setBackground(R.drawable.dock_home_normal);
                mMenuB.setSize(dockSize, dockSize);
                addChild(mMenuB, true);
                mMenuB.setUserTranslate(new SEVector3f(0, -10, 0));
                mMenuB.setOnClickListener(new OnTouchListener() {
                    @Override
                    public void run(SEObject obj) {
                        hide(true, null);
                    }
                });

                mMenuMore = new SEImageView(getScene(), "DocMenu_More");
                mMenuMore.setBackground(R.drawable.dock_dian_normal);
                mMenuMore.setSize(dockSize, dockSize);
                addChild(mMenuMore, true);
                mMenuMore.setUserTranslate(new SEVector3f(screenW * 3 / 8, -10, 0));
                mMenuMore.setOnClickListener(new OnTouchListener() {
                    @Override
                    public void run(SEObject obj) {
                        showMoreMenu();
                    }
                });
            }
        }

    }

    private static final String PREF_KEY_SORT_OPTION = "PREF_KEY_SORT_OPTION";

    class SortByType implements Comparator<PreViewObject> {
        public int compare(PreViewObject lhs, PreViewObject rhs) {
            if (mSortOption == SORT_BY_NAME) {
                return Collator.getInstance().compare(lhs.getItemInfo().getLabel(), rhs.getItemInfo().getLabel());
            } else if (mSortOption == SORT_BY_RECENTLY_INSTALL) {
                if (lhs.getItemInfo().mInstallDateTime == rhs.getItemInfo().mInstallDateTime) {
                    return 0;
                } else if (lhs.getItemInfo().mInstallDateTime < rhs.getItemInfo().mInstallDateTime) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (mSortOption == SORT_BY_USED_FREQUENCY) {
                if (lhs.getItemInfo().mLaunchCount == rhs.getItemInfo().mLaunchCount) {
                    return 0;
                } else if (lhs.getItemInfo().mLaunchCount < rhs.getItemInfo().mLaunchCount) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return -1;
        }
    }

    private void readSortOptionConfiguration() {
        final String preferenceName = SettingsActivity.PREFS_SETTING_NAME;
        SharedPreferences settings = HomeManager.getInstance().getContext().getSharedPreferences(preferenceName, 0);
        mSortOption = settings.getInt(PREF_KEY_SORT_OPTION, SORT_BY_NAME);
    }

    private void writeSortOptionConfiguration() {
        final String preferenceName = SettingsActivity.PREFS_SETTING_NAME;
        SharedPreferences settings = HomeManager.getInstance().getContext().getSharedPreferences(preferenceName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(PREF_KEY_SORT_OPTION, mSortOption);
        editor.commit();
    }

    private void showMoreMenu() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mMoreMenuDialog = new MoreMenuDialog(SESceneManager.getInstance().getGLActivity());
                MoreMenuDialog.Builder builder = new MoreMenuDialog.Builder(SESceneManager.getInstance()
                        .getGLActivity());

                if (mMoreMenuAdapter == null) {
                    mMoreMenuAdapter = new MoreMenuAdapter(SESceneManager.getInstance().getGLActivity());
                }
                builder.setAdapter(mMoreMenuAdapter);
                builder.setOnClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        onMoreMenuSelected(mMoreMenuAdapter.buttonToCommand(arg2));
                        mMoreMenuDialog.dismiss();
                    }
                });

                mMoreMenuDialog = builder.create();
                Window w = mMoreMenuDialog.getWindow();
                WindowManager.LayoutParams lp = w.getAttributes();

                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                lp.width = mScreenW * 3 / 5;
                lp.y = 60;
                mMoreMenuDialog.show();
            }
        });
    }

    private void onMoreMenuSelected(int type) {
        switch (type) {
        case MoreMenuAdapter.SORT_APP:
            showSortTypes();
            break;
        case MoreMenuAdapter.HIDE_APP:
            getScene().handleMessage(HomeScene.MSG_TYPE_ADD_REMOVE_APP_DIALOG, null);
            break;
        }
    }

    private void showSortTypes() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                readSortOptionConfiguration();
                mSortAppListDialog = new AlertDialog.Builder(SESceneManager.getInstance().getGLActivity())
                        .setTitle(R.string.title_select_drawer_sort)
                        .setSingleChoiceItems(R.array.app_drawer_sort_type, mSortOption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int position) {
                                        mSortOption = position;
                                        writeSortOptionConfiguration();
                                        new SECommand(getScene()) {
                                            public void run() {
                                                updatePosition();
                                            }
                                        }.execute();
                                        mSortAppListDialog.dismiss();
                                    }
                                }).create();

                mSortAppListDialog.show();
            }
        });
    }

    private void getLauncherCount(final List<ItemInfo> apps, final Runnable finish) {
        LauncherModel.getInstance().process(new Runnable() {
            public void run() {
                for (ItemInfo app : apps) {
                    if (app.mItemType == ItemInfo.ITEM_TYPE_APP) {
                        String where = AppsDrawerColumns.COMPONENTNAME + "='" + app.getComponentName().toShortString()
                                + "'";
                        String[] select = new String[] { AppsDrawerColumns.LAUNCHERCOUNT };
                        Cursor cursor = getContext().getContentResolver().query(AppsDrawerColumns.CONTENT_URI, select,
                                where, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                app.mLaunchCount = cursor.getInt(0);
                            } else {
                                ContentValues values = new ContentValues();
                                values.put(AppsDrawerColumns.COMPONENTNAME, app.getComponentName().toShortString());
                                values.put(AppsDrawerColumns.LAUNCHERCOUNT, 0);
                                getContext().getContentResolver().insert(AppsDrawerColumns.CONTENT_URI, values);
                            }
                            cursor.close();
                        }
                    }
                }
                if (finish != null) {
                    finish.run();
                }
            }
        });
    }

    // Move all application menu related code from SESceneManager to here
    private void caculateAppsMenuWallSize(int w, int h) {
        Resources res = getContext().getResources();
        mAppsMenuWallWidth = w;
        mAppsMenuWallHeight = h;
        mAppsMenuWallPaddingLeft = res.getDimension(R.dimen.apps_customize_pageLayoutPaddingLeft);
        mAppsMenuWallPaddingRight = res.getDimension(R.dimen.apps_customize_pageLayoutPaddingRight);
        mAppsMenuWallPaddingTop = res.getDimension(R.dimen.apps_customize_pageLayoutPaddingTop);
        mAppsMenuWallPaddingBottom = res.getDimension(R.dimen.apps_customize_pageLayoutPaddingBottom);
        caculateAppsMenuCellCount();

        mFaceAngle = 360.f / CYLINDER_FACE_NUM;
        mFaceRadius = (float) (mAppsMenuWallWidth / (2 * Math.tan(mFaceAngle * Math.PI / 360)));
    }

    private void caculateAppsMenuCellCount() {
        Resources res = getContext().getResources();
        mAppsMenuCellCountX = 1;
        while (widthInPortraitOfAppsMenu(res, mAppsMenuCellCountX + 1) <= mAppsMenuWallWidth) {
            mAppsMenuCellCountX++;
        }
        mAppsMenuCellCountY = 1;
        while (heightInLandscapeOfAppsMenu(res, mAppsMenuCellCountY + 1) <= mAppsMenuWallHeight) {
            mAppsMenuCellCountY++;
        }
        caculateAppsMenuIconSize();
        caculateAppsMenuCellSize();
        mIconCount = mAppsMenuCellCountX * mAppsMenuCellCountY;
    }

    private void caculateAppsMenuIconSize() {
        Resources res = getContext().getResources();
        mScreenDensity = getScene().getScreenDensity();
        mAppMenuIconSize = res.getDimensionPixelSize(R.dimen.app_customize_icon_size);
        mAppMenuIconTextSize = res.getDimensionPixelSize(R.dimen.app_customize_icon_text_size);
        mAppMenuIconPaddingTop = res.getDimensionPixelSize(R.dimen.app_customize_icon_PaddingTop);
        mAppMenuIconPadding = res.getDimensionPixelSize(R.dimen.app_customize_icon_Padding);
    }

    private void caculateAppsMenuCellSize() {
        Resources res = getContext().getResources();
        mAppsMenuCellWidth = res.getDimensionPixelSize(R.dimen.apps_customize_cell_width);
        mAppsMenuCellHeight = res.getDimensionPixelSize(R.dimen.apps_customize_cell_height);
        float hSpace = mAppsMenuWallWidth - mAppsMenuWallPaddingLeft - mAppsMenuWallPaddingRight;
        float vSpace = mAppsMenuWallHeight - mAppsMenuWallPaddingTop - mAppsMenuWallPaddingBottom;
        float hFreeSpace = hSpace - (mAppsMenuCellCountX * mAppsMenuCellWidth);
        float vFreeSpace = vSpace - (mAppsMenuCellCountY * mAppsMenuCellHeight);
        int numAppsMenuWidthGaps = mAppsMenuCellCountX - 1;
        int numAppsMenuHeightGaps = mAppsMenuCellCountY - 1;
        mAppsMenuWidthGap = hFreeSpace / numAppsMenuWidthGaps;
        mAppsMenuHeightGap = vFreeSpace / numAppsMenuHeightGaps;
    }

    private int widthInPortraitOfAppsMenu(Resources r, int numCells) {
        int cellWidth = r.getDimensionPixelSize(R.dimen.apps_customize_cell_width);
        int minGap = r.getDimensionPixelSize(R.dimen.apps_customize_pageLayoutWidthGap);
        return minGap * (numCells - 1) + cellWidth * numCells;
    }

    private int heightInLandscapeOfAppsMenu(Resources r, int numCells) {
        int cellHeight = r.getDimensionPixelSize(R.dimen.apps_customize_cell_height);
        int minGap = r.getDimensionPixelSize(R.dimen.apps_customize_pageLayoutHeightGap);
        return minGap * (numCells - 1) + cellHeight * numCells;
    }

    public class AppSearchPaneItemLongClickListener implements AppSearchPane.OnAppSearchItemLongClickListener {
        @Override
        public void onItemLongClick(final AppSearchPane.Item holder) {
            new SECommand(getScene()) {
                public void run() {
                    hide(true, null);
                }
            }.execute();
            if (mAppSearchPane != null) {
                mAppSearchPane.setVisibility(View.INVISIBLE);
            }

            int size = mApps.size();
            String packageName = "";
            String className = "";
            PreViewObject obj = null;
            for (int i = 0; i < size; i++) {
                obj = mApps.get(i);
                packageName = obj.mItemInfo.getComponentName().getPackageName();
                className = obj.mItemInfo.getComponentName().getClassName();
                if (holder.mPkgName.equals(packageName) && holder.mClsName.equals(className)) {
                    break;
                } else {
                    obj = null;
                }
            }

            SERay ray = getCamera().screenCoordinateToRay(holder.mTouchX, holder.mTouchY);
            float alpha = (1000f - ray.getLocation().getY()) / ray.getDirection().getY();
            float posY = 1000f;
            float posX = ray.getLocation().getX() + alpha * ray.getDirection().getX();
            float posZ = ray.getLocation().getZ() + alpha * ray.getDirection().getZ();
            SEVector3f pos = new SEVector3f();
            pos.set(posX, posY, posZ);
            final SETransParas startTranspara = new SETransParas();
            startTranspara.mTranslate = pos;

            final AppObject itemObject;
            if (obj != null) {
                itemObject = AppObject.create(mHomeScene, obj.getItemInfo());
                new SECommand(getScene()) {
                    public void run() {
                        getScene().getContentObject().addChild(itemObject, true);

                        itemObject.initStatus();

                        itemObject.setTouch(holder.mTouchX, holder.mTouchY);
                        itemObject.setStartTranspara(startTranspara);
                        itemObject.setOnMove(true);
                        SESceneManager.getInstance().runInUIThread(new Runnable() {
                            public void run() {
                                long now = SystemClock.uptimeMillis();
                                MotionEvent eventDown = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
                                        holder.mTouchX, holder.mTouchY, 0);
                                HomeManager.getInstance().getWorkSpace().dispatchTouchEvent(eventDown);
                            }
                        });
                    }
                }.execute();
            }
        }
    }

    @Override
    public void OnAddRemoveApps(ArrayList<AddObjectItemInfo> selectedList) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();

        SharedPreferences pref = HomeManager.getInstance().getContext()
                .getSharedPreferences("add_or_hidde_app", Context.MODE_MULTI_PROCESS);
        Editor editor = pref.edit();

        ArrayList<AppItemInfo> allData = mModel.getApps();
        for (AppItemInfo ii : mModel.getApps()) {
            String componetName = ii.getComponentName().toString();
            editor.putBoolean(componetName, true);
            ii.setShowInPreface(true);
        }
        editor.commit();

        for (int i = 0; i < selectedList.size(); i++) {
            String selectedName = selectedList.get(i).getComponentName().toString();
            for (int j = 0; j < allData.size(); j++) {
                String componentName = allData.get(j).getComponentName().toString();
                if (selectedName.equals(componentName)) {
                    allData.get(j).setShowInPreface(false);
                    editor.putBoolean(componentName, false);
                    break;
                }
            }
        }
        editor.commit();

        myApps.addAll(allData);
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                new SECommand(getScene()) {
                    public void run() {
                        forceReload(myApps);
                        mHasLoaded = true;
                    }
                }.execute();
            }
        });
    }

}
