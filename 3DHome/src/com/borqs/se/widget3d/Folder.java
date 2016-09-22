package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.graphics.Paint.FontMetrics;
import android.graphics.BitmapFactory;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.borqs.se.R;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEImageView;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEMoveAnimation;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector2i;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeDataBaseHelper;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;
import com.borqs.se.addobject.AddFolderDialog.OnFolderEditedListener;
import com.borqs.se.addobject.AddObjectComponentNameExtend;
import com.borqs.se.addobject.AddObjectItemInfo;


public class Folder extends VesselObject implements OnFolderEditedListener {
    private boolean mOnDialogShow;
    private FolderExpandDialog mDialog;
    private List<NormalObject> mIcons;
    private boolean mDisableTouch;
    private SEVector3f mDialogCenter;
    private SEMoveAnimation mShowDialogFolderAnimation;
    public SEImageView mIconObject;
    private boolean mOnExpandFolderUpdate = false;
    private boolean mNeedShowLable = true;
    private House mHouse;

    public Folder(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mOnDialogShow = false;
        mDisableTouch = false;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        mNeedShowLable = !HomeManager.getInstance().getWhetherShowAppShelf();
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else if (getParent().getParent() != null && getParent().getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        setCanChangeBind(false);
        setCanChangeLabel(true);
        setVesselLayer(new FolderLayer(getHomeScene(), this));
        setOnClickListener(new OnTouchListener() {
            public void run(SEObject obj) {
                if (!getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)
                        && !getHomeScene().getStatus(HomeScene.STATUS_ON_SKY_SIGHT)) {
                    showExpand();
                }
            }
        });
        LauncherModel.getInstance().addAppCallBack(this);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        boolean needShowLable = !HomeManager.getInstance().getWhetherShowAppShelf();
        if (needShowLable != mNeedShowLable) {
            mNeedShowLable = needShowLable;
            new SECommand(getScene()) {
                public void run() {
                    if (mIconObject != null) {
                        createIconObject();
                        if (getParent() != null) {
                            changeTransParasInParent();
                        }
                    }
                }
            }.execute();
        }

    }
    /**
     * Create a folder
     */
    public static Folder create(HomeScene scene, String name) {
        final ObjectInfo objInfo = new ObjectInfo();
        objInfo.mIndex = (int) System.currentTimeMillis();
        objInfo.mName = "folder";
        objInfo.mSceneName = scene.getSceneName();
        objInfo.mDisplayName = name;
        objInfo.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        objInfo.mType = "Folder";
        objInfo.mObjectSlot.mSpanX = 1;
        objInfo.mObjectSlot.mSpanY = 1;
        objInfo.mObjectSlot.mSlotIndex = 0;
        Folder folder = new Folder(scene, objInfo.mName, objInfo.mIndex);
        folder.setIsFresh(false);
        objInfo.saveToDB(HomeDataBaseHelper.getInstance(scene.getContext())
                .getWritableDatabase());
        folder.setObjectInfo(objInfo);
        return folder;
    }

    private void createIconObject() {
        new SECommand(getScene()) {
            public void run() {
                if (!hasBeenReleased()) {
                    if (mIconObject != null) {
                        removeComponenetObject(mIconObject, true);
                    }
                    if (TextUtils.isEmpty(getObjectInfo().mDisplayName)) {
                        getObjectInfo().mDisplayName = getContext().getString(R.string.folder_title);
                    }

                    Bitmap icon = createIcon();
                    String label = null;
                    if (mNeedShowLable) {
                        label = getObjectInfo().mDisplayName;
                    }
                    Bitmap iconWithText = HomeUtils.getAppIconBitmap(mHouse, icon, null, label, false);
                    iconWithText = Bitmap.createScaledBitmap(iconWithText, 128, 128, true);
                    mIconObject = new SEImageView(getScene(), "folder_icon_" + mIndex);
                    mIconObject.setSize(mHouse.mCellWidth, mHouse.mCellHeight);
                    mIconObject.setBackgroundBitmap(iconWithText);
                    addComponenetObject(mIconObject, true);
                }
            }
        }.execute();
    }

    private Bitmap createIcon() {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = getContext().getResources().getDrawable(R.drawable.folder);
        background.setBounds(0, 0, 256, 256);
        background.draw(canvas);
        for (SEObject child : getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject icon = (NormalObject) child;
                fillBitmap(canvas, icon);
            }
        }
        return bitmap;
    }

    private void fillBitmap(Canvas canvas, NormalObject icon) {
        Bitmap b = icon.getObjectInfo().mShortcutIcon;
        Drawable drawable = null;
        if (b == null) {
            drawable = HomeUtils.getAppIcon(getContext(), icon.getObjectInfo().getResolveInfo());
        }
        if (icon.getObjectSlot().mSlotIndex == 0) {
            canvas.save();
            if (b != null) {
                Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                Rect dst = new Rect(40, 40, 120, 120);
                canvas.drawBitmap(b, src, dst, null);
            } else {
                drawable.setBounds(40, 40, 120, 120);
                drawable.draw(canvas);
            }
            canvas.restore();
        } else if (icon.getObjectSlot().mSlotIndex == 1) {
            canvas.save();
            if (b != null) {
                Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                Rect dst = new Rect(136, 40, 216, 120);
                canvas.drawBitmap(b, src, dst, null);
            } else {
                drawable.setBounds(136, 40, 216, 120);
                drawable.draw(canvas);
            }
            canvas.restore();
        } else if (icon.getObjectSlot().mSlotIndex == 2) {
            canvas.save();
            if (b != null) {
                Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                Rect dst = new Rect(40, 136, 120, 216);
                canvas.drawBitmap(b, src, dst, null);
            } else {
                drawable.setBounds(40, 136, 120, 216);
                drawable.draw(canvas);
            }
            canvas.restore();
        } else if (icon.getObjectSlot().mSlotIndex == 3) {
            canvas.save();
            if (b != null) {
                Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                Rect dst = new Rect(136, 136, 216, 216);
                canvas.drawBitmap(b, src, dst, null);
            } else {
                drawable.setBounds(136, 136, 216, 216);
                drawable.draw(canvas);
            }
            canvas.restore();
        }
    }

    @Override
    public void updateLabel(String label) {
        if (TextUtils.isEmpty(label)) {
            label = getContext().getString(R.string.folder_title);
        }
        if (!label.equals(getObjectInfo().mDisplayName)) {
            getObjectInfo().mDisplayName = label;
            getObjectInfo().updateToDB(false, true, false);
            updateFolderCover();
        }
    }

    public void updateFolderCover() {
        createIconObject();
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }

    @Override
    public void onFolderEdited(String folderName, ArrayList<AddObjectItemInfo> selectedList) {
        if (mOnDialogShow && mDialog != null) {
            mDialog.update(folderName, selectedList);
        }
    }

    public Bundle getEditBundle() {
        if (mDialog != null) {
            return mDialog.getBundle();
        }
        return null;
    }

    private void showExpand() {
        if (!mOnDialogShow && !mOnExpandFolderUpdate) {
            stopAllAnimation(null);           
            mOnDialogShow = true;
            mDisableTouch = true;
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, true);
            ObjectInfo objInfo = new ObjectInfo();
            objInfo.setModelInfo(HomeManager.getInstance().getModelManager().findModelInfo("woodfolderopen"));
            mDialog = new FolderExpandDialog(getHomeScene(), objInfo.mName, 1, getObjectInfo().mDisplayName);
            mDialog.setObjectInfo(objInfo);
            HomeManager.getInstance().getModelManager().createQuickly(getScene().getContentObject(), mDialog);
            getScene().setTouchDelegate(mDialog);

            mIcons = new ArrayList<NormalObject>();
            for (SEObject icon : getChildObjects()) {
                if (icon instanceof NormalObject) {
                    mIcons.add((NormalObject) icon);
                }
            }
            SETransParas srcTransParas = new SETransParas();
            srcTransParas.mTranslate = getAbsoluteTranslate().clone();
            srcTransParas.mScale.set(0.2f, 0.2f, 0.2f);
            mDialog.getUserTransParas().set(srcTransParas);
            mDialog.setUserTransParas();
            mDialog.initStatus();

            if (HomeManager.getInstance().isLandscapeOrientation()) {
                //在横屏的情况下dialog填充屏幕高度
                mDialogCenter = getCamera().getScreenLocation(mDialog.mModelSizeZ / getCamera().getHeight());
//                getHomeScene().getDesk().hide(null);
                getHomeScene().hideDesk(null);
            } else {
                // 调整展开后的Folder dialog中心点位置使得其和墙面中心点和相机的位置在一条直线上
                // 获取墙面中心
                SEVector3f centerOfWall = new SEVector3f(0, mHouse.mWallRadius, mHouse.mWallHeight / 2);
                // 获取相机位置到墙面中心的向量
                SEVector3f ori = centerOfWall.subtract(getCamera().getLocation()).normalize();
                // 获取dialog位置的Y值
                float dialogY = getCamera().getScreenLocation(mDialog.mModelSizeX / getCamera().getWidth()).getY();
                // 获取dialog位置的Z值
                float para = (dialogY - getCamera().getLocation().getY()) / ori.getY();
                float dialogZ = getCamera().getLocation().getZ() + ori.getZ() * para;
                mDialogCenter = new SEVector3f(0, dialogY, dialogZ);
            }

            SETransParas desTransParas = new SETransParas();
            desTransParas.mTranslate = mDialogCenter.clone();
            desTransParas.mScale = new SEVector3f(1, 1, 1);
            setVisible(false);
            mShowDialogFolderAnimation = new SEMoveAnimation(getScene(), mDialog, AXIS.Z, srcTransParas, desTransParas,
                    8);
            mShowDialogFolderAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDisableTouch = false;
                }
            });
            mShowDialogFolderAnimation.execute();
        }
    }
    
    private void hideExpand(boolean fast, final SEAnimFinishListener l) {
        if (mOnDialogShow && !mOnExpandFolderUpdate) {
            mDisableTouch = true;
            stopAllAnimation(null);
            if (HomeManager.getInstance().isLandscapeOrientation()) {
//                getHomeScene().getDesk().show(null);
                getHomeScene().showDesk(null);
            }
            if (fast) {
                mOnDialogShow = false;
                mDisableTouch = false;
                setVisible(true);
                for (SEObject child : mIcons) {
                    NormalObject icon = (NormalObject) child;
                    icon.changeParent(this);
                    icon.initStatus();
                    icon.setAlpha(1);
                    icon.setIsEntirety(false);
                    icon.getObjectInfo().updateSlotDB();
                    icon.setVisible(false);
                }
                mIcons = null;
                if (getChildObjects().size() == 1) {
                    NormalObject icon = changeToAppIcon();
                    icon.getObjectInfo().updateSlotDB();
                } else {
                    if (!getObjectInfo().mDisplayName.equals(mDialog.mDisplayName)) {
                        getObjectInfo().mDisplayName =  mDialog.mDisplayName;
                        getObjectInfo().updateToDB(false, true, false);
                    }
                    updateFolderCover();
                }
                getScene().getContentObject().removeChild(mDialog, true);
                HomeManager.getInstance().getModelManager().unRegister(mDialog);
                getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                getScene().removeTouchDelegate();
                if (l != null) {
                    l.onAnimationfinish();
                }
                if ((getChildObjects() == null) || (getChildObjects().size() <= 0)) {
                    getParent().removeChild(this, true);
                }

            } else {
                SETransParas srcTransParas = mDialog.getUserTransParas().clone();
                SETransParas desTransParas = new SETransParas();
                desTransParas.mTranslate = getAbsoluteTranslate().clone();
                desTransParas.mScale.set(0.2f, 0.2f, 0.2f);

                mShowDialogFolderAnimation = new SEMoveAnimation(getScene(), mDialog, AXIS.Z, srcTransParas,
                        desTransParas, 6);
                mShowDialogFolderAnimation.setAnimFinishListener(new SEAnimFinishListener() {

                    public void onAnimationfinish() {
                        mOnDialogShow = false;
                        mDisableTouch = false;
                        setVisible(true);
                        for (SEObject child : mIcons) {
                            NormalObject icon = (NormalObject) child;
                            icon.changeParent(Folder.this);
                            icon.initStatus();
                            icon.setAlpha(1);
                            icon.setIsEntirety(false);
                            icon.getObjectInfo().updateSlotDB();
                            icon.hideBackground();
                            icon.setVisible(false);
                        }
                        mIcons = null;
                        if (getChildObjects().size() == 1) {
                            NormalObject icon = changeToAppIcon();
                            icon.getObjectInfo().updateSlotDB();
                        } else {
                            if (!getObjectInfo().mDisplayName.equals(mDialog.mDisplayName)) {
                                getObjectInfo().mDisplayName =  mDialog.mDisplayName;
                                getObjectInfo().updateToDB(false, true, false);
                            }
                            updateFolderCover();
                        }
                        getScene().getContentObject().removeChild(mDialog, true);
                        HomeManager.getInstance().getModelManager().unRegister(mDialog);
                        getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                        getScene().removeTouchDelegate();
                        if (l != null) {
                            l.onAnimationfinish();
                        }
                        if ((getChildObjects() == null) || (getChildObjects().size() <= 0)) {
                            getParent().removeChild(Folder.this, true);
                        }
                    }
                });
                mShowDialogFolderAnimation.execute();
            }
        }
    }

    @Override
    public void stopAllAnimation(SEAnimFinishListener l) {
        super.stopAllAnimation(l);
        if (mShowDialogFolderAnimation != null) {
            mShowDialogFolderAnimation.stop();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mDisableTouch || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mOnDialogShow) {
            hideExpand(false, l);
            return true;
        }
        return false;

    }

    public NormalObject changeToAppIcon() {
        NormalObject icon = (NormalObject) getChildObjects().get(0);
        VesselObject parent = (VesselObject) getParent();
        icon.getObjectSlot().set(getObjectSlot());
        icon.getObjectSlot().mVesselID = parent.getObjectInfo().mID;
        icon.changeParent(parent);
//        String backgroundName = SettingsActivity.getAppIconBackgroundName(getContext());
//        if (!"none".equals(backgroundName)) {
//            icon.showBackground();
//        } else {
//            icon.hideBackground();
//        }
        icon.initStatus();
        icon.setIsEntirety(true);
        icon.setVisible(true);
        parent.removeChild(this, true);
        return icon;
    }

    @Override
    public void bindAllPackagesUpdated() {
        super.bindAllPackagesUpdated();
        if (mOnDialogShow && mDialog != null) {
            mDialog.bindAllPackagesUpdated();
        }
        new SECommand(getScene()) {
            public void run() {
                if (!mOnDialogShow) {
                    updateFolderCover();
                }
            }
        };
    }

    @Override
    public void bindAppsUpdated(List<ItemInfo> apps) {
        super.bindAppsUpdated(apps);
        if (mOnDialogShow && mDialog != null) {
            mDialog.bindAppsUpdated(apps);
        }
        new SECommand(getScene()) {
            public void run() {
                if (!mOnDialogShow) {
                    updateFolderCover();
                }
            }
        };
    }

    @Override
    public void bindAppsRemoved(List<ItemInfo> apps) {
        super.bindAppsRemoved(apps);
        if (mOnDialogShow && mDialog != null) {
            mDialog.bindAppsRemoved(apps);
        }
        new SECommand(getScene()) {
            public void run() {
                if (mOnDialogShow) {
                    return;
                }
                if (getChildObjects().size() == 0) {
                    getParent().removeChild(Folder.this, true);
                } else if (getChildObjects().size() == 1) {
                    NormalObject icon = changeToAppIcon();
                    icon.getObjectInfo().updateSlotDB();
                } else {
                    Collections.sort(getChildObjects(), new SortByPosition());
                    int index = 0;
                    for (SEObject child : getChildObjects()) {
                        NormalObject icon = (NormalObject) child;
                        icon.getObjectSlot().mSlotIndex = index;
                        index++;
                        icon.getObjectInfo().updateSlotDB();
                    }
                    updateFolderCover();
                }

            }
        }.execute();
    }

    class SortByPosition implements Comparator<SEObject> {
        public int compare(SEObject lhs, SEObject rhs) {
            NormalObject icon1 = (NormalObject) lhs;
            NormalObject icon2 = (NormalObject) rhs;
            return icon1.getObjectSlot().mSlotIndex - icon2.getObjectSlot().mSlotIndex;
        }
    }

    @Override
    public void onLoadFinished() {
        if (getChildObjects().size() == 0) {
            getParent().removeChild(this, true);
        } else if (getChildObjects().size() == 1) {
            NormalObject icon = changeToAppIcon();
            icon.getObjectInfo().updateSlotDB();
        } else {
            for (SEObject child : getChildObjects()) {
                child.setVisible(false);
            }

            updateFolderCover();
        }
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        SETransParas transparas = new SETransParas();
        if (objectSlot.mSlotIndex == 0) {
            transparas.mTranslate.set(-26, -25.0f, 47);
            transparas.mScale.set(0.28f, 0.28f, 0.28f);
        } else if (objectSlot.mSlotIndex == 1) {
            transparas.mTranslate.set(26, -25.0f, 47);
            transparas.mScale.set(0.28f, 0.28f, 0.28f);
        } else if (objectSlot.mSlotIndex == 2) {
            transparas.mTranslate.set(-26, -25.0f, 3);
            transparas.mScale.set(0.28f, 0.28f, 0.28f);
        } else if (objectSlot.mSlotIndex == 3) {
            transparas.mTranslate.set(26, -25.0f, 3);
            transparas.mScale.set(0.28f, 0.28f, 0.28f);
        } else {
            transparas.mTranslate.set(0, 20, 25);
            transparas.mScale.set(0, 0, 0);
        }
        return transparas;
    }

    public boolean isShortcutOrUserUpdate(ObjectInfo objectInfo) {
        return objectInfo != null && ((objectInfo.isShortcut()) || (objectInfo.isAppUserUpdate()));
    }

    private class FolderExpandDialog extends VesselObject {
        private int mSpanX = 3;
        private int mSpanZ = 4;
        
        private NormalObject mOnMoveIcon;
        private SEVector2i mAdjustTouch;
        private SEVector3f mTouchLocation;
        private ObjectSlot mObjectSlot;
        private List<ConflictAnimationTask> mConflictAnimationTasks;
        private boolean mStopConflictAnimation;

        protected SEObject mFolderName;
        protected SEObject mEdit;
        protected String mDisplayName;
        protected String mImageName;

        private float mGridSizeX;
        private float mGridSizeZ;  
        private float mAdjustZ;
        private float mModelSizeX;
        private float mModelSizeZ;
        private float mModelOutY;

        // for generating folder name 
        private int FOLDER_NAME_PADDING_TOP = 23;    // by model, the plane of name
        private int FOLDER_NAME_PADDING_BOTTOM = 17; // by model, the plane of
                                                     // name
        
        public FolderExpandDialog(HomeScene scene, String name, int index, String displayName) {
            super(scene, name, index);
            setIsGroup(true);
            mStopConflictAnimation = false;
            setPressType(PRESS_TYPE.NONE);
            mDisplayName = displayName;
        }

        @Override
        public void initStatus() {
            super.initStatus();
            mFolderName = findComponenetObjectByRegularName("foldername");
            mImageName = mFolderName.getImageName();
            updateFolderName(mDisplayName);
            mEdit = this.findComponenetObjectByRegularName("editfolder");
            mEdit.setIsEntirety(true);
            mEdit.setClickable(true);
            mEdit.setPressType(PRESS_TYPE.COLOR);
            mEdit.setOnClickListener(new OnTouchListener() {
                public void run(SEObject obj) {
                    getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_EDITFOLDER_DIALOG, Folder.this);
                }
            });            
            mModelSizeX = getObjectInfo().mModelInfo.mMaxPoint.getX() - getObjectInfo().mModelInfo.mMinPoint.getX();
            mModelSizeZ = getObjectInfo().mModelInfo.mMaxPoint.getZ() - getObjectInfo().mModelInfo.mMinPoint.getZ();
            mModelOutY = getObjectInfo().mModelInfo.mMaxPoint.getY() - getObjectInfo().mModelInfo.mMinPoint.getY();
            mGridSizeX = mModelSizeX / 4;
            mGridSizeZ = mModelSizeZ / 5 + 1;
            if (HomeManager.getInstance().getWhetherShowAppShelf()) {
                mAdjustZ = -17;
            } else {
                mAdjustZ = -7;
            }
            setOnClickListener(new OnTouchListener() {
                public void run(SEObject obj) {
                    SERay ray = getCamera().screenCoordinateToRay(getTouchX(), getTouchY());
                    SEVector3f point = rayCrossY(ray, mDialogCenter.getY() - mModelOutY);
                    float maxX = mModelSizeX / 2;
                    float maxZ = mModelSizeZ / 2;
                    float centerZ = mDialogCenter.getZ();
                    if (point.getX() < -maxX || point.getX() > maxX || point.getZ() > centerZ + maxZ
                            || point.getZ() < centerZ - maxZ) {
                        hideExpand(false, null);
                    }
                }
            });
                        
            setPressedListener(null);
            setUnpressedListener(null);
            setOnLongClickListener(null);
            updateIconPosition();
        }

        @Override
        public void bindAppsRemoved(List<ItemInfo> apps) {
            super.bindAppsRemoved(apps);
            final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
            myApps.addAll(apps);
            new SECommand(getScene()) {
                public void run() {
                    if (mOnDialogShow && mIcons != null) {
                        int size = mIcons.size();
                        for (int i = 0; i < size; i++) {
                            NormalObject icon = mIcons.get(i);
                            for (ItemInfo info : myApps) {
                                if (info.getComponentName().equals(icon.getObjectInfo().mComponentName)) {
                                    i--;
                                    size--;
                                    mIcons.remove(icon);
                                    break;
                                }
                            }
                        }
                        sortIcons();
                        updateIconPosition();
                    }
                }
            }.execute();
        }

        private void updateIconPosition() {
            for (NormalObject icon : mIcons) {
                icon.changeParent(this);
                icon.setUserTransParas(getTransParasInVessel(icon, icon.getObjectSlot()));
                icon.setAlpha(1);
                icon.setIsEntirety(true);
                icon.setVisible(true);
                icon.setOnLongClickListener(new OnTouchListener() {
                    public void run(SEObject obj) {
                        mOnMoveIcon = (NormalObject) obj;
                        SEVector3f userTranslate = mOnMoveIcon.getAbsoluteTranslate().clone();
                        mAdjustTouch = getCamera().worldToScreenCoordinate(userTranslate);
                        mAdjustTouch.selfSubtract(new SEVector2i(mOnMoveIcon.getTouchX(), mOnMoveIcon.getTouchY()));
                        setMovePoint(obj.getTouchX(), obj.getTouchY());
                        mOnMoveIcon.setUserTranslate(mTouchLocation);
                        mObjectSlot = mOnMoveIcon.getObjectSlot().clone();
                    }
                });
            }
        }

        @Override
        public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
            int slotIndex = objectSlot.mSlotIndex;
            int startX = slotIndex % mSpanX;
            int startY = slotIndex / mSpanX;
            SETransParas transParas = new SETransParas();
            transParas.mTranslate.set(-mGridSizeX * (mSpanX - 1) / 2 + startX * mGridSizeX, -mModelOutY, (mSpanZ - 1)
                    * mGridSizeZ / 2 - startY * mGridSizeZ + mAdjustZ);
            float scale = mGridSizeX / mHouse.mCellWidth * 0.70f;
            transParas.mScale.set(scale, scale, scale);
            return transParas;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return mOnMoveIcon != null;
        }

        private SEVector3f rayCrossY(SERay ray, float y) {
            float para = (y - ray.getLocation().getY()) / ray.getDirection().getY();
            SEVector3f crossPoint = ray.getLocation().add(ray.getDirection().mul(para));
            return crossPoint;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mOnMoveIcon = null;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                if (mOnMoveIcon != null) {
                    SELoadResThread.getInstance().cancel(mPlayConflictAnimationTask);
                    mConflictAnimationTasks = getConflictTask(mObjectSlot);
                    if (mConflictAnimationTasks != null) {
                        mPlayConflictAnimationTask.run();
                    }
                    if (mObjectSlot.mSlotIndex >= mIcons.size()) {
                        mObjectSlot.mSlotIndex = mIcons.size() - 1;
                    }
                    mOnMoveIcon.getObjectSlot().set(mObjectSlot);
                    mOnMoveIcon.setPressed(false);
                    SEVector3f src = mOnMoveIcon.getUserTransParas().mTranslate.clone();
                    SEVector3f des = getTransParasInVessel(mOnMoveIcon, mOnMoveIcon.getObjectSlot()).mTranslate.clone();
                    MovePositionAnimation movePositionAnimation = new MovePositionAnimation(getScene(), mOnMoveIcon,
                            src, des, 8);
                    movePositionAnimation.execute();
                }
            }
            return mDisableTouch || super.dispatchTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mOnMoveIcon == null) {
                return super.onTouchEvent(event);
            }
            if (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL) {
                setMovePoint((int) event.getX(), (int) event.getY());
                mOnMoveIcon.setUserTranslate(mTouchLocation);
                mOnMoveIcon.setPressed(true);
                ObjectSlot objectSlot = calculateSlot();
                if (!cmpSlot(objectSlot, mObjectSlot)) {
                    mObjectSlot = objectSlot;
                    if (mObjectSlot.mSlotIndex == mSpanX * mSpanZ) {
                        SELoadResThread.getInstance().cancel(mPlayConflictAnimationTask);
                        if (mConflictAnimationTasks != null) {
                            for (ConflictAnimationTask conflictAnimationTask : mConflictAnimationTasks) {
                                conflictAnimationTask.mIcon.getObjectSlot().set(conflictAnimationTask.mMoveSlot);
                            }
                        }
                        mStopConflictAnimation = true;
                        mIcons.remove(mOnMoveIcon);
                        SETransParas startTranspara = new SETransParas();
                        startTranspara.mTranslate = mOnMoveIcon.getAbsoluteTranslate();
                        mOnMoveIcon.changeParent(getRoot());
                        mOnMoveIcon.initStatus();
                        hideExpand(true, null);
                        mOnMoveIcon.setStartTranspara(startTranspara);
                        mOnMoveIcon.getUserTransParas().set(startTranspara);
                        mOnMoveIcon.setUserTransParas();
                        mOnMoveIcon.setTouch(getTouchX(), getTouchY());
                        mOnMoveIcon.setOnMove(true);
                    } else {
                        updateStatus(200);
                    }
                }
            }
            return true;
        }

        private void updateStatus(long delay) {
            SELoadResThread.getInstance().cancel(mPlayConflictAnimationTask);
            if (mConflictAnimationTasks != null) {
                SELoadResThread.getInstance().process(mPlayConflictAnimationTask, delay);
            }
        }

        private Runnable mPlayConflictAnimationTask = new Runnable() {
            public void run() {
                if (mConflictAnimationTasks != null) {
                    List<ConflictAnimationTask> tasks = new ArrayList<ConflictAnimationTask>();
                    tasks.addAll(mConflictAnimationTasks);
                    for (ConflictAnimationTask task : tasks) {
                        task.mIcon.getObjectSlot().set(task.mMoveSlot);
                        task.mSrcLocation = task.mIcon.getUserTransParas().mTranslate.clone();
                        task.mDesLocation = getTransParasInVessel(task.mIcon, task.mIcon.getObjectSlot()).mTranslate.clone();
                    }
                    ConflictAnimation conflictAnimation = new ConflictAnimation(getScene(), tasks, 8);
                    conflictAnimation.execute();
                }
            }
        };

        private ObjectSlot calculateSlot() {
            float x = (mTouchLocation.getX() + (mSpanX - 1) * mGridSizeX / 2) / mGridSizeX;
            float y = ((mSpanZ - 1) * mGridSizeZ / 2 - mTouchLocation.getZ()) / mGridSizeZ;
            int startX = Math.round(x);
            int startY = Math.round(y);
            ObjectSlot objectSlot = mOnMoveIcon.getObjectSlot().clone();
            if (startX < 0 || startX >= mSpanX || startY < 0 || startY >= mSpanZ) {
                objectSlot.mSlotIndex = mSpanX * mSpanZ;
            } else {
                objectSlot.mSlotIndex = startY * mSpanX + startX;
            }
            if (!objectSlot.equals(mObjectSlot)) {
                mConflictAnimationTasks = getConflictTask(objectSlot);
            }
            return objectSlot;
        }

        private List<ConflictAnimationTask> getConflictTask(ObjectSlot slot) {
            List<ConflictAnimationTask> tasks = null;
            NormalObject conflictIcon = getConflictIcon(slot);
            if (conflictIcon != null) {
                tasks = new ArrayList<ConflictAnimationTask>();
                if (canPlaceToLeft(tasks, conflictIcon)) {
                    return tasks;
                }
                tasks = new ArrayList<ConflictAnimationTask>();
                if (canPlaceToRight(tasks, conflictIcon)) {
                    return tasks;
                }
            } else {
                if (slot.mSlotIndex >= mIcons.size()) {
                    conflictIcon = getMaxIndexIcon();
                    if (conflictIcon != null) {
                        tasks = new ArrayList<ConflictAnimationTask>();
                        if (canPlaceToLeft(tasks, conflictIcon)) {
                            return tasks;
                        }
                    }
                }
            }
            return null;
        }

        private boolean canPlaceToLeft(List<ConflictAnimationTask> tasks, NormalObject conflictIcon) {
            if (conflictIcon.getObjectSlot().mSlotIndex > 0) {
                ConflictAnimationTask conflictAnimationTask = new ConflictAnimationTask();
                conflictAnimationTask.mIcon = conflictIcon;
                ObjectSlot moveSlot = conflictIcon.getObjectSlot().clone();
                moveSlot.mSlotIndex = moveSlot.mSlotIndex - 1;
                conflictAnimationTask.mMoveSlot = moveSlot;
                tasks.add(conflictAnimationTask);
                NormalObject newConflictIcon = getConflictIcon(moveSlot);
                return newConflictIcon == null || (canPlaceToLeft(tasks, newConflictIcon));
            } else {
                return false;
            }
        }

        private boolean canPlaceToRight(List<ConflictAnimationTask> tasks, NormalObject conflictIcon) {
            if (conflictIcon.getObjectSlot().mSlotIndex < mSpanX * mSpanZ) {
                ConflictAnimationTask conflictAnimationTask = new ConflictAnimationTask();
                conflictAnimationTask.mIcon = conflictIcon;
                ObjectSlot moveSlot = conflictIcon.getObjectSlot().clone();
                moveSlot.mSlotIndex = moveSlot.mSlotIndex + 1;
                conflictAnimationTask.mMoveSlot = moveSlot;
                tasks.add(conflictAnimationTask);
                NormalObject newConflictIcon = getConflictIcon(moveSlot);
                return newConflictIcon == null || (canPlaceToRight(tasks, newConflictIcon));
            } else {
                return false;
            }
        }

        private NormalObject getMaxIndexIcon() {
            NormalObject maxIcon = null;
            for (NormalObject icon : mIcons) {
                if (icon != mOnMoveIcon) {
                    if (maxIcon == null) {
                        maxIcon = icon;
                    } else if (icon.getObjectSlot().mSlotIndex > maxIcon.getObjectSlot().mSlotIndex) {
                        maxIcon = icon;
                    }
                }
            }
            return maxIcon;
        }

        private NormalObject getConflictIcon(ObjectSlot slot) {
            for (NormalObject icon : mIcons) {
                if (icon != mOnMoveIcon) {
                    if (icon.getObjectSlot().mSlotIndex == slot.mSlotIndex) {
                        return icon;
                    }
                }
            }
            return null;
        }

        private void setMovePoint(int touchX, int touchY) {
            SERay ray = getCamera().screenCoordinateToRay(mAdjustTouch.getX() + touchX, mAdjustTouch.getY() + touchY);
            mTouchLocation = getTouchLocation(ray);
        }

        private SEVector3f getTouchLocation(SERay ray) {
            float y = mDialogCenter.getY() - mModelOutY;
            float para = (y - ray.getLocation().getY()) / ray.getDirection().getY();
            SEVector3f touchLocation = ray.getLocation().add(ray.getDirection().mul(para));
            touchLocation.selfSubtract(new SEVector3f(0, mDialogCenter.getY() + mModelOutY, mDialogCenter.getZ()));
            return touchLocation;
        }

        private boolean cmpSlot(ObjectSlot objectSlot1, ObjectSlot objectSlot2) {
            if (objectSlot1 == null && objectSlot2 == null) {
                return true;
            }
            return !((objectSlot1 != null && objectSlot2 == null) || (objectSlot1 == null && objectSlot2 != null)) && objectSlot1.equals(objectSlot2);
        }

        private class MovePositionAnimation extends CountAnimation {
            private SEVector3f mSrcLocation;
            private SEVector3f mDesLocation;
            private NormalObject mObject;
            private int mCount;
            private float mStep;

            public MovePositionAnimation(SEScene scene, NormalObject obj, SEVector3f srcLocation,
                    SEVector3f desLocation, int count) {
                super(scene);
                mObject = obj;
                mSrcLocation = srcLocation;
                mDesLocation = desLocation;
                mCount = count;
            }

            public void runPatch(int count) {
                float step = mStep * count;
                SEVector3f location = mSrcLocation.add(mDesLocation.subtract(mSrcLocation).mul(step));
                mObject.setUserTranslate(location);
            }

            @Override
            public void onFirstly(int count) {
                mStep = 1f / getAnimationCount();
            }

            @Override
            public int getAnimationCount() {
                return mCount;
            }
        }

        private class ConflictAnimation extends CountAnimation {
            private List<ConflictAnimationTask> mTasks;
            private int mCount;
            private float mStep;

            public ConflictAnimation(SEScene scene, List<ConflictAnimationTask> conflictAnimationTasks, int count) {
                super(scene);
                mCount = count;
                mTasks = conflictAnimationTasks;
            }

            public void runPatch(int count) {
                if (mStopConflictAnimation) {
                    stop();
                } else {
                    float step = mStep * count;
                    for (ConflictAnimationTask task : mTasks) {
                        SEVector3f location = task.mSrcLocation.add(task.mDesLocation.subtract(task.mSrcLocation).mul(
                                step));
                        task.mIcon.setUserTranslate(location);
                    }
                }
            }

            @Override
            public void onFinish() {
                for (ConflictAnimationTask task : mTasks) {
                    task.mIcon.setAlpha(1);
                }
            }

            @Override
            public void onFirstly(int count) {
                for (ConflictAnimationTask task : mTasks) {
                    task.mIcon.setAlpha(0.6f);
                }
                mStep = 1f / getAnimationCount();
            }

            @Override
            public int getAnimationCount() {
                return mCount;
            }
        }

        private class ConflictAnimationTask {
            public NormalObject mIcon;
            public ObjectSlot mMoveSlot;
            private SEVector3f mSrcLocation;
            private SEVector3f mDesLocation;
        }

        public Bundle getBundle() {
            if (mIcons == null) {
                return null;
            }
            ArrayList<AddObjectComponentNameExtend> componentNameExs = new ArrayList<AddObjectComponentNameExtend>();
            AddObjectComponentNameExtend[] listTmp = new AddObjectComponentNameExtend[mIcons.size()];
            for (int i = 0; i < listTmp.length; i++) {
                listTmp[i] = null;
            }
            int idx = 0;
            for (SEObject child : mIcons) {
                NormalObject icon = (NormalObject) child;
                idx = icon.getObjectInfo().mObjectSlot.mSlotIndex;
                if (idx >= listTmp.length) {
                    continue;
                }
                AddObjectComponentNameExtend ex = null;
                if (Folder.this.isShortcutOrUserUpdate(icon.getObjectInfo())) {
                    ex = new AddObjectComponentNameExtend(icon.getObjectInfo().mComponentName,
                            icon.getObjectInfo().mName, icon.getObjectInfo().isShortcut() == true ? "Shortcut" : "App", true);
                } else {
                    ex = new AddObjectComponentNameExtend(icon.getObjectInfo().mComponentName, 
                            icon.getObjectInfo().mName, "App", false);
                }
                listTmp[idx] = ex;
            }
            for (int i = 0; i < listTmp.length; i++) {
                if (listTmp[i] != null) {
                    componentNameExs.add(listTmp[i]);
                }
            }

            Bundle bd = new Bundle();
            ArrayList list = new ArrayList();
            list.add(componentNameExs);
            bd.putParcelableArrayList("componentnameexs", list);
            bd.putString("foldername", mDisplayName);
            return bd;
        }

        public void update(String fname, ArrayList<AddObjectItemInfo> selectedList) {
            mOnExpandFolderUpdate = true;
            if (TextUtils.isEmpty(fname)) {
                mDisplayName = getContext().getString(R.string.folder_title);
                updateFolderName(mDisplayName);
                updateLabel(mDisplayName);
            } else {
                if (mDisplayName.equals(fname) == false) {
                    mDisplayName = fname;
                    updateFolderName(mDisplayName);
                    updateLabel(mDisplayName);
                }
            }
            
            // null: no any change. simply resolve the refresh icons even user do nothing when 'ok'.
            if (selectedList == null) {
                mOnExpandFolderUpdate = false;
                return;
            }
            
            // Here allow selections is empty (but not null)
            final List<AddObjectItemInfo> addIcons = new ArrayList<AddObjectItemInfo>();
            addIcons.addAll(selectedList);
            new SECommand(getScene()) {
                public void run() {
                    List<NormalObject> removeIcons = new ArrayList<NormalObject>();
                    removeIcons.addAll(mIcons);
                    int startIdx = 0;
                    for (int i = 0; i < addIcons.size(); i++) {
                        startIdx = i + 1;
                        AddObjectItemInfo itemInfo = addIcons.get(i);
                        if (itemInfo.isOriginal()) {
                            for (NormalObject icon : mIcons) {
                                if (icon.getName().equals(itemInfo.mObjectInfoName)) {
                                    removeIcons.remove(icon);
                                    break;
                                }
                            }
                        } else {
                            startIdx = i;
                            break;
                        }
                    }
                    removeIconsFromFolder(removeIcons);
                    sortIcons();
                    addIconsToFolder(addIcons.subList(startIdx, addIcons.size()));
                    updateIconPosition();
                    mOnExpandFolderUpdate = false;
                }
            }.execute();
        }

        private void sortIcons() {
            Collections.sort(mIcons, new SortByPosition());
            int index = 0;
            for (NormalObject icon : mIcons) {
                icon.getObjectSlot().mSlotIndex = index;
                index++;
            }
        }

        private void removeIconsFromFolder(List<NormalObject> removeIcons) {
            if (mIcons == null) {
                return;
            }
            if ((removeIcons == null) || (removeIcons.size() <= 0)) {
                return;
            }
            for(NormalObject icon:removeIcons) {
                removeChild(icon, true);
            }
            mIcons.removeAll(removeIcons);
        }

        private void addIconsToFolder(List<AddObjectItemInfo> itemInfos) {
            int size = mIcons.size();
            for (ItemInfo itemInfo : itemInfos) {
                AppObject itemObject = AppObject.create(getHomeScene(), itemInfo.getComponentName(), size);
                itemObject.getObjectInfo().mVesselName = Folder.this.mName;
                itemObject.getObjectInfo().mVesselIndex = Folder.this.getObjectInfo().mIndex;
                itemObject.getObjectInfo().mObjectSlot.mSlotIndex = size;
                itemObject.getObjectInfo().saveToDB();
                addChild(itemObject, true);
                itemObject.initStatus();
                mIcons.add(itemObject);
                size++;
            }
        }

        private void updateFolderName(final String fname) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    Bitmap des = null;
                    AssetManager am = getContext().getResources().getAssets();
                    try {
                        InputStream is = am.open("base/woodfolderopen/object_folder02b_fhq_name.png");
                        des = BitmapFactory.decodeStream(is).copy(Bitmap.Config.ARGB_8888, true);
                        is.close();
                    } catch (Exception e) {
                    }
                    if (des == null) {
                        return;
                    }

                    int w = des.getWidth();
                    int h = des.getHeight();
                    boolean bl = generateFolderNameBitmap(des, w, h, fname);
                    if (bl == false) {
                        return;
                    }
                    final int imageData = SEObject.loadImageData_JNI(des);
                    new SECommand(getScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mImageName, mImageName);
                            SEObject.addImageData_JNI(mImageName, imageData);
                        }
                    }.execute();
                }
            });
        }

        private String processTextForSingle(String text, int allw, Paint paint) {
            int size = paint.breakText(text, true, allw, null);
            if (size >= text.length()) {
                return text;
            }
            return text.substring(0, size);
        }

        private boolean generateFolderNameBitmap(Bitmap des, int w, int h, String text) {
            if (TextUtils.isEmpty(text)) {
                return false;
            }
            if (des == null) {
                return false;
            }
            if ((w <= 0) || (h <= 0)) {
                return false;
            }

            String FOOTER_END = "...";
            int TRY_TIME = 50;
            int FONT_DEFAULT = 6;
            int PADDING_LEFT = 5; // px
            int PADDING_RIGHT = 5;
            int Y = FOLDER_NAME_PADDING_TOP + 2;

            int fsize = FONT_DEFAULT;
            int fnamew = w - PADDING_LEFT - PADDING_RIGHT;
            int maxfonth = (int) (((float) (h - FOLDER_NAME_PADDING_TOP - FOLDER_NAME_PADDING_BOTTOM)));

            Paint myFPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            myFPaint.setColor(Color.BLACK);

            Paint pFont = new Paint();
            int cache = 0;
            int cc = 0;
            while (true) {
                pFont.setTextSize(fsize);
                FontMetrics fm = pFont.getFontMetrics();
                int fonth = (int) Math.ceil(fm.descent - fm.top);
                if (fonth >= maxfonth) {
                    break;
                }
                cc++;
                if (cc > TRY_TIME) {
                    break;
                }
                cache = fsize;
                fsize++;
            }
            fsize = cache;

            myFPaint.setTextSize(fsize);
            FontMetrics fm = myFPaint.getFontMetrics();

            int len = text.length();
            String finalfname = processTextForSingle(text, fnamew, myFPaint);
            int x = PADDING_LEFT;
            int y = Y - (int) fm.ascent;
            if (finalfname.length() != len) {
                int endl = (int) myFPaint.measureText(FOOTER_END);
                finalfname = processTextForSingle(text + FOOTER_END, fnamew - endl, myFPaint);
                finalfname += FOOTER_END;
            }

            Canvas cv = new Canvas(des);
            cv.drawText(finalfname, x, y, myFPaint);
            return true;
        }

    }

    @Override
    public boolean update(SEScene scene) {
        createIconObject();
        changeTransParasInParent();
        return true;
    }
}
