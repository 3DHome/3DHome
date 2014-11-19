package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.*;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.ProviderUtils;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.UpdateDBThread;
import com.borqs.se.home3d.MountPointData.SEMountPointGroup;
import com.borqs.se.home3d.MountPointData.SEMountPointItem;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class Desk extends VesselObject {
    private final static double MAX_SIGHT_ANGLE = Math.PI / 6;
    private final static float SIGHT_FOV = 30;
    private VelocityTracker mVelocityTracker;
    private DeskVelocityAnimation mDeskVelocityAnimation;
    private ToFaceAnimation mToFaceAnimation;
    private float mAngle;
    private float mDeskTranslateZ;
    private SEEmptyAnimation mMoveDeskAnimation;
    private float mPreTouchAngle;
    private float[] mRecordRotate;
    private boolean mDisableTouch;
    private boolean mCanRotateDesk;
    private float mRoateFanAngle;
    public int mMAXChildNum;
    protected SEVector3f mDeskSize;
    private SECameraData mObserverCameraData;
    private boolean mIsLandScape;

    public Desk(HomeScene scene, String name, int index) {
        super(scene, name, index);
        setClickable(true);
        mDeskTranslateZ = 0;
        mAngle = 0;
        mRecordRotate = new float[] { 0, 0, 0, 0, 0 };
        mDisableTouch = false;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setNeedCullFace(true);
        setOnClickListener(null);
        mIsLandScape = HomeManager.getInstance().isLandscapeOrientation();
        mMAXChildNum = getContainerMountPoint().getMountPointItems().size();
        mDeskSize = getObjectInfo().mModelInfo.mMaxPoint.subtract(getObjectInfo().mModelInfo.mMinPoint);
        String canBeRotated = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "canBeRotated");
        mCanRotateDesk = "true".equals(canBeRotated);
        if (mCanRotateDesk) {
            String roateFanAngle = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "fanAngle");
            mRoateFanAngle = Float.parseFloat(roateFanAngle);
        }
        String observerCameraData = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "observation");
        if (!TextUtils.isEmpty(observerCameraData)) {
            mObserverCameraData = new SECameraData();
            String[] data = observerCameraData.split(ProviderUtils.SPLIT_SYMBOL);
            mObserverCameraData.init(data);
        }
        String mirrordensity = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "mirrordensity");
        if (!TextUtils.isEmpty(mirrordensity)) {
            float density = Float.parseFloat(mirrordensity);
            setMirrorColorDesity_JNI(density);
        }
        setVesselLayer(new DeskLayer(getHomeScene(), this));
        LauncherModel.getInstance().addAppCallBack(this);
        setPressType(PRESS_TYPE.NONE);
        setOnLongClickListener(new OnTouchListener() {
            @Override
            public void run(SEObject obj) {
                getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_OBJECT_LONG_CLICK_DIALOG, Desk.this);
            }

        });
        // 桌子正在被拖拽
        if (!isFresh()) {
            getHomeScene().setDesk(this);
        }
    }

    @Override
    public void onSlotSuccess() {
        if (!getObjectInfo().mType.equals(getHomeScene().getDesk().getObjectInfo().mType)) {
            getParent().removeChild(this, true);
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    Toast.makeText(HomeManager.getInstance().getContext(), R.string.can_not_put_object_in_room,
                            Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        if (!getHomeScene().switchDesk(this)) {
            getParent().removeChild(this, true);
        }
    }

    @Override
    public void handleOutsideRoom() {
        setOnMove(false);
        getParent().removeChild(this, true);
        if (isFresh()) {
            setIsFresh(false);
            if (canUninstall()) {
                getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_DELETE_OBJECTS, this.mName);
            } else if (mName.equals(getHomeScene().getDesk().getName())) {
                // 不能删除正在被使用的桌面
                final int messageId = R.string.delete_being_used_desk_text;
                SESceneManager.getInstance().runInUIThread(new Runnable() {
                    public void run() {
                        Toast.makeText(HomeManager.getInstance().getContext(), messageId, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                final int messageId = R.string.delete_preset_object_text;
                SESceneManager.getInstance().runInUIThread(new Runnable() {
                    public void run() {
                        Toast.makeText(HomeManager.getInstance().getContext(), messageId, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public boolean switchDesk(Desk newDesk) {
        if (mName.equals(newDesk.mName)) {
            return false;
        }
        moveAllChildToVesselObject(newDesk);
        getParent().removeChild(this, true);
        newDesk.updateAllChildSlotDB();
        getHomeScene().setDesk(newDesk);
        return true;
    }

    private void updateAllChildSlotDB() {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                new SECommand(getScene()) {
                    public void run() {
                        for (SEObject child : getChildObjects()) {
                            if (child instanceof NormalObject) {
                                NormalObject normalObject = (NormalObject) child;
                                normalObject.getObjectSlot().mVesselID = getObjectInfo().mID;
                                normalObject.getObjectInfo().updateSlotDB();
                            }
                        }
                    }
                }.execute();
            }
        });
    }

    public SEVector3f getDeskSize() {
        return mDeskSize;
    }

    public SECameraData getObserveCamera() {
        if (mObserverCameraData != null) {
            return mObserverCameraData;
        }
        SECameraData cameraData = new SECameraData();
        cameraData.mAxisZ = new SEVector3f(0, -(float) Math.cos(MAX_SIGHT_ANGLE), (float) Math.sin(MAX_SIGHT_ANGLE));
        cameraData.mFov = SIGHT_FOV;
        SEVector3f centerLocation = new SEVector3f(0, 0, mDeskSize.getZ());
        float paras = (float) (mDeskSize.getX() * 0.5f / Math.tan(SIGHT_FOV * Math.PI / 360));
        SEVector3f screenOrientation = new SEVector3f(0, (float) Math.cos(-MAX_SIGHT_ANGLE),
                (float) Math.sin(-MAX_SIGHT_ANGLE));
        SEVector3f loc = centerLocation.subtract(screenOrientation.mul(paras));
        SEVector3f locOri = new SEVector3f(0, (float) Math.sin(MAX_SIGHT_ANGLE), (float) Math.cos(MAX_SIGHT_ANGLE));
        float distance = (float) (getCamera().getWidth() / (2 * Math.tan(SIGHT_FOV * Math.PI / 360)));
        double sightFovH = Math.atan(getCamera().getHeight() / (2 * distance)) + MAX_SIGHT_ANGLE;
        SEVector3f topOri = new SEVector3f(0, (float) Math.cos(sightFovH), (float) -Math.sin(sightFovH));
        SEVector3f top = new SEVector3f(0, -mDeskSize.getY() / 2, mDeskSize.getZ());
        float para = ((loc.getY() - top.getY()) / topOri.getY() - (loc.getZ() - top.getZ()) / topOri.getZ())
                / (locOri.getZ() / topOri.getZ() - locOri.getY() / topOri.getY());
        cameraData.mLocation = new SEVector3f(0, loc.getY() + locOri.getY() * para, loc.getZ() + locOri.getZ() * para);
        return cameraData;
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        if (needPlaceObj != null && needPlaceObj.getObjectInfo().mIsNativeObject
                && needPlaceObj.getObjectInfo().mModelInfo.mMountPointData != null) {
            SEMountPointGroup objMountPointInVessel = needPlaceObj.getObjectInfo().mModelInfo.mMountPointData
                    .getObjectMountPointInContainer(getContainerMountPoint().getContainerName());
            if (objMountPointInVessel != null) {
                SEMountPointItem mountPointItem = objMountPointInVessel.getMountPointItem(objectSlot.mSlotIndex);
                if (mountPointItem != null) {
                    return mountPointItem.getTransParas();
                }
            }
        }
        if (objectSlot.mSlotIndex < mMAXChildNum) {
            return getContainerMountPoint().getMountPointItem(objectSlot.mSlotIndex).getTransParas();
        } else {
            SETransParas transParas = new SETransParas();
            transParas.mTranslate.set(0, 0, -100000);
            return transParas;
        }
    }

    @Override
    public void onPressHomeKey() {
        super.onPressHomeKey();
        toFace(0, null, 10);
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }

    private void clearRecord() {
        for (int i = 0; i < 5; i++) {
            mRecordRotate[i] = 0;
        }
    }

    private void pushToRecord(float rotate) {
        for (int i = 0; i < 4; i++) {
            mRecordRotate[i] = mRecordRotate[i + 1];
        }
        mRecordRotate[4] = rotate;
    }

    private float getAverageRecord() {
        float average = 0;
        for (int i = 0; i < 5; i++) {
            average = average + mRecordRotate[i];
        }
        return average / 5;
    }

    public void setRotate(SERotate rotate) {
        mAngle = rotate.getAngle();
        setUserRotate(rotate);
    }

    public float getAngle() {
        return mAngle;
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
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (mDeskVelocityAnimation != null && !mDeskVelocityAnimation.isFinish()) {
                stopAllAnimation(null);
                return true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mMotionTarget != null && mMotionTarget instanceof NormalObject) {
                NormalObject motionTarget = (NormalObject) mMotionTarget;
                if ("group_pc".equals(motionTarget.getObjectInfo().mName)) {
                    return false;
                }
            }
            mPreTouchAngle = getTouchRotate(getTouchX(), getTouchY());
            stopAllAnimation(null);
            clearRecord();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCanRotateDesk) {
            return onTouchEventOfDesk(event);
        } else {
            return onTouchEventOfSquareDesk(event);
        }
    }

    public boolean onTouchEventOfDesk(MotionEvent event) {
        trackVelocity(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            getHomeScene().moveToDeskSight(null);
            mPreTouchAngle = getTouchRotate(getTouchX(), getTouchY());
            clearRecord();
            setPressed(true);
            postCheckForLongClick();
            break;
        case MotionEvent.ACTION_MOVE:
            getHomeScene().moveToDeskSight(null);
            float touchAngle = getTouchRotate(getTouchX(), getTouchY());
            float needRotate = touchAngle - mPreTouchAngle;
            if (needRotate > 180) {
                needRotate = needRotate - 360;
            } else if (needRotate < -180) {
                needRotate = needRotate + 360;
            }
            pushToRecord(needRotate);
            float curAngle = getAngle() + needRotate;
            setRotate(new SERotate(curAngle));
            mPreTouchAngle = touchAngle;
            if (!hasPerformedLongPress()) {
                removeLongClick();
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            setPressed(false);
            if (!hasPerformedLongPress()) {
                removeLongClick();
            }
            float xV = mVelocityTracker.getXVelocity();
            float yV = mVelocityTracker.getYVelocity();
            float v = (float) Math.sqrt(xV * xV + yV * yV);
            mDeskVelocityAnimation = new DeskVelocityAnimation(getScene(), v, getAverageRecord());
            mDeskVelocityAnimation.execute();
            break;
        }
        return true;
    }

    public boolean onTouchEventOfSquareDesk(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            getHomeScene().moveToDeskSight(null);
            setPressed(true);
            if (getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT))
                postCheckForLongClick();
            break;
        case MotionEvent.ACTION_MOVE:
            getHomeScene().moveToDeskSight(null);
            if (!hasPerformedLongPress()) {
                removeLongClick();
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            setPressed(false);
            if (!hasPerformedLongPress()) {
                removeLongClick();
            }
            break;
        }
        return true;
    }

    private float getTouchRotate(int x, int y) {
        SERay Ray = getCamera().screenCoordinateToRay(getTouchX(), getTouchY());
        SEVector3f touchLoc = rayCrossZ(Ray, mDeskSize.getZ());
        SEVector2f vectorZ = touchLoc.getVectorZ();
        return (float) (vectorZ.getAngle() * 180 / Math.PI);
    }

    private SEVector3f rayCrossZ(SERay ray, float crossZ) {
        float para = (crossZ - ray.getLocation().getZ()) / ray.getDirection().getZ();
        SEVector3f touchLoc = ray.getLocation().add(ray.getDirection().mul(para));

        return touchLoc;
    }

    public void playDeskCorrectAnimation() {
        stopAllAnimation(null);
        mDeskVelocityAnimation = new DeskVelocityAnimation(getScene(), 0, 0);
        mDeskVelocityAnimation.execute();
    }

    public void toFace(int face, SEAnimFinishListener listener, float step) {
        stopAllAnimation(null);
        float desAngle = 360 - face * mRoateFanAngle;
        if (mAngle == desAngle) {
            if (listener != null) {
                listener.onAnimationfinish();
            }
            return;
        }
        mToFaceAnimation = new ToFaceAnimation(getScene(), face, step);
        mToFaceAnimation.setAnimFinishListener(listener);
        mToFaceAnimation.execute();
    }

    @Override
    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mDeskVelocityAnimation != null) {
            mDeskVelocityAnimation.stop();
        }
        if (mToFaceAnimation != null) {
            mToFaceAnimation.stop();
        }
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)) {
            getHomeScene().moveToWallSight(l);
        }
        return false;
    }

    private class DeskVelocityAnimation extends CountAnimation {
        private float mVelocity;
        private float mCurrentAngle;
        private float mDesAngle;
        private float mPerFaceAngle;

        public DeskVelocityAnimation(SEScene scene, float velocity, float direct) {
            super(scene);
            mVelocity = velocity;
            if (Math.abs(mVelocity) < 100) {
                mVelocity = 0;
            }
            if (direct < 0) {
                mVelocity = -mVelocity;
            }
            mPerFaceAngle = mRoateFanAngle;
        }

        @Override
        public void runPatch(int count) {
            float needRotateAngle = mDesAngle - mCurrentAngle;
            float absNRA = Math.abs(needRotateAngle);
            if (absNRA < 1) {
                mCurrentAngle = mDesAngle;
                stop();
            } else {
                float step = (float) Math.sqrt(absNRA);
                if (needRotateAngle < 0) {
                    step = -step;
                }
                mCurrentAngle = mCurrentAngle + step;
            }
            setRotate(new SERotate(mCurrentAngle));
        }

        @Override
        public void onFirstly(int count) {
            mCurrentAngle = mAngle;
            float needRotateAngle = mVelocity * 0.05f;
            float desAngle = mCurrentAngle + needRotateAngle;
            if (desAngle >= 0) {
                int index = (int) ((desAngle + mPerFaceAngle / 2) / mPerFaceAngle);
                mDesAngle = mPerFaceAngle * index;
            } else {
                int index = (int) ((desAngle - mPerFaceAngle / 2) / mPerFaceAngle);
                mDesAngle = mPerFaceAngle * index;
            }
        }
    }

    private class ToFaceAnimation extends CountAnimation {
        private float mStep;
        private float mDesAngle;
        private float mCurAngle;
        private int mFace;

        public ToFaceAnimation(SEScene scene, int face, float step) {
            super(scene);
            mStep = step;
            mFace = face;
        }

        public void runPatch(int count) {
            float needRotate = mDesAngle - mCurAngle;
            float absNTR = Math.abs(needRotate);
            if (absNTR <= mStep) {
                mCurAngle = mDesAngle;
                stop();
            } else {
                float step = mStep;
                if (needRotate < 0) {
                    step = -mStep;
                }
                mCurAngle = mCurAngle + step;
            }
            setRotate(new SERotate(mCurAngle));
        }

        public void onFirstly(int count) {
            mCurAngle = getAngle();
            mDesAngle = 360 - mFace * mRoateFanAngle;
            float needRotateAngle = Math.abs(mDesAngle - mCurAngle);
            if (needRotateAngle > 180) {
                needRotateAngle = 360 - needRotateAngle;
                if (mDesAngle - mCurAngle > 180) {
                    mDesAngle = mCurAngle - needRotateAngle;
                } else if (mDesAngle - mCurAngle < -180) {
                    mDesAngle = mCurAngle + needRotateAngle;
                }
            }
        }
    }

    public void show(SEAnimFinishListener l) {
        stopDeskAnimation();
        final float toZ = 0;
        final float fromZ = mDeskTranslateZ;
        int animTimes = (int) (Math.abs(toZ - fromZ) / 20);
        mMoveDeskAnimation = new SEEmptyAnimation(getScene(), 0, 1, animTimes) {
            @Override
            public void onAnimationRun(float value) {
                mDeskTranslateZ = fromZ + (toZ - fromZ) * value;
                float scale = (mDeskTranslateZ + mDeskSize.getZ()) / mDeskSize.getZ();
                getUserTransParas().mScale.set(scale, scale, scale);
                getUserTransParas().mTranslate.set(0, 0, mDeskTranslateZ);
                setUserTransParas();
            }

            @Override
            public void onBegin() {
                setVisible(true);
            }

        };
        mMoveDeskAnimation.setAnimFinishListener(l);
        mMoveDeskAnimation.execute();
    }

    public void hide(SEAnimFinishListener l) {
        stopDeskAnimation();
        final float fromZ = mDeskTranslateZ;
        final float toZ = -mDeskSize.getZ();
        int animTimes = (int) (Math.abs(toZ - fromZ) / 20);
        mMoveDeskAnimation = new SEEmptyAnimation(getScene(), 0, 1, animTimes) {
            @Override
            public void onAnimationRun(float value) {
                mDeskTranslateZ = fromZ + (toZ - fromZ) * value;
                float scale = (mDeskTranslateZ + mDeskSize.getZ()) / mDeskSize.getZ();
                getUserTransParas().mScale.set(scale, scale, scale);
                getUserTransParas().mTranslate.set(0, 0, mDeskTranslateZ);
                setUserTransParas();
            }

            @Override
            public void onFinish() {
                setVisible(false);
            }
        };
        mMoveDeskAnimation.setAnimFinishListener(l);
        mMoveDeskAnimation.execute();
    }

    private void stopDeskAnimation() {
        if (mMoveDeskAnimation != null) {
            mMoveDeskAnimation.stop();
        }
    }

    // override super method in VesselLayer, which use spanX x spanY grid for vessel space
    // calculation, here, desk prefer to use its mount point configuration instead.
    @Override
    public List<SEVector.SEVector3f> getAllLocationForObject(NormalObject object) {
        List<SEVector3f> allLocation = new ArrayList<SEVector3f>();
        SEMountPointGroup deskMountPoint = getContainerMountPoint();
        for (int index = 0; index < mMAXChildNum; index++) {
            SEVector3f mountLocation = deskMountPoint.getMountPointItem(index).getTranslate().clone();
            SEVector3f location = new SEVector3f(localToWorld_JNI(mountLocation.mD));
            location.mD[2] = getDeskSize().getZ();
            allLocation.add(location);
        }
        return allLocation;
    }

    @Override
    public int getVesselCapability() {
        return mMAXChildNum;
    }

}
