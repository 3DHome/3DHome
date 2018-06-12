package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class GroundLayer extends VesselLayer {
    private List<ConflictObject> mExistentSlot;
    private ObjectSlot mObjectSlot;
    private SECamera mCamera;
    private House mHouse;
    private boolean mNeedRotateWall;
    private SEVector3f mRealLocation;
    private SETransParas mObjectTransParas;
    private float mVirtualWallRadius;
    private float mSkyRadius;
    private ACTION mPreAction;

    public GroundLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mHouse = (House) getVesselObject().getParent();
        mCamera = scene.getCamera();
        mVirtualWallRadius = mHouse.mWallRadius * 0.9f;
        mSkyRadius = mHouse.mWallRadius * 0.3f;
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        int slotType = object.getObjectInfo().mSlotType;
        return slotType == ObjectInfo.SLOT_TYPE_GROUND;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            mObjectSlot = onMoveObject.getObjectSlot().clone();
            mExistentSlot = getExistentSlot();
            setInRecycle(false);
        } else {
            cancelRotation();
            mHouse.toNearestFace(null, 2);
        }
        return true;
    }

    private float mTouchX;

    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        mTouchX = x;
        stopMoveAnimation();
        updateRecycleStatus(event, x, y);
        setMovePoint((int) x, (int) y);
        ObjectSlot objectSlot = calculateSlot();
        ConflictObject conflictSlot = null;
        boolean slotChanged = !cmpSlot(objectSlot, mObjectSlot);
        if (slotChanged) {
            conflictSlot = getConflictSlot(objectSlot);
        }
        switch (event) {
        case BEGIN:
            mPreAction = ACTION.BEGIN;
            mNeedRotateWall = false;
            SETransParas srcTransParas = getOnMoveObject().getUserTransParas().clone();
            SETransParas desTransParas = mObjectTransParas.clone();
            playMoveAnimation(srcTransParas, desTransParas, new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    if (!mNeedRotateWall) {
                        calculationWallRotation(800);
                    }
                }
            });
            if (!cmpSlot(objectSlot, mObjectSlot)) {
                mObjectSlot = objectSlot;
                playConflictAnimationTask(conflictSlot, 300);
            }
            break;
        case MOVE:
            mPreAction = ACTION.MOVE;
            getOnMoveObject().getUserTransParas().set(mObjectTransParas);
            getOnMoveObject().setUserTransParas();
            if (!cmpSlot(objectSlot, mObjectSlot)) {
                mObjectSlot = objectSlot;
                playConflictAnimationTask(conflictSlot, 300);
            }
            if (!mNeedRotateWall) {
                calculationWallRotation(500);
            }
            break;
        case UP:
            mPreAction = ACTION.UP;
            cancelConflictAnimationTask();
            getOnMoveObject().getUserTransParas().set(mObjectTransParas);
            getOnMoveObject().setUserTransParas();
            if (!cmpSlot(objectSlot, mObjectSlot)) {
                mObjectSlot = objectSlot;
            }
            cancelRotation();
            break;
        case FLY:
            cancelConflictAnimationTask();
            getOnMoveObject().getUserTransParas().set(mObjectTransParas);
            getOnMoveObject().setUserTransParas();
            if (!cmpSlot(objectSlot, mObjectSlot)) {
                mObjectSlot = objectSlot;
            }
            break;
        case FINISH:
            cancelConflictAnimationTask();
            if (isInRecycle()) {
                handleOutsideRoom();
            } else {
                mObjectSlot = calculateNearestSlot(mRealLocation, true);
                if (mObjectSlot == null) {
                    handleNoMoreRoom();
                    return true;
                }
                conflictSlot = getConflictSlot(mObjectSlot);
                playConflictAnimationTask(conflictSlot, 0);
                playSlotAnimation(mObjectSlot, new SEAnimFinishListener() {
                    public void onAnimationfinish() {
                        handleSlotSuccess();
                    }
                });

            }
            break;
        }
        return true;
    }

    /**
     * Calculate whether object is in rubbish box and update the box's color
     */
    private void updateRecycleStatus(ACTION event, float x, float y) {
        boolean force = false;
        switch (event) {
        case BEGIN:
            force = false;
            break;
        case MOVE:
            force = false;
            break;
        case UP:
            force = true;
            break;
        case FLY:
            force = true;
            break;
        case FINISH:
            force = true;
            break;
        }

        float touchY = y - getOnMoveObject().getAdjustTouch().getY();
        float touchX = x - getOnMoveObject().getAdjustTouch().getX();
        if (touchX >= mBoundOfRecycle.left && touchX <= mBoundOfRecycle.right && touchY <= mBoundOfRecycle.bottom
                && touchY >= mBoundOfRecycle.top) {
            setInRecycle(true);
        } else {
            if (!force) {
                setInRecycle(false);
            }
        }
    }

    public void setMovePoint(int touchX, int touchY) {
        SERay ray = mCamera.screenCoordinateToRay(touchX, touchY);
        mRealLocation = rayCrossWall(ray, mHouse.mWallRadius).mTranslate;
        mObjectTransParas = getObjectTransParas(ray);
    }

    private SETransParas getObjectTransParas(SERay ray) {
        SETransParas transParas = rayCrossWall(ray, mVirtualWallRadius);
        int minZ = 0;
        int maxZ = (int) (mHouse.mWallPaddingBottom / 2);
        if (transParas.mTranslate.getZ() < minZ) {
            transParas.mTranslate.mD[2] = minZ;
        } else if (transParas.mTranslate.getZ() > maxZ) {
            transParas.mTranslate = rayCrossCylinder(ray, mSkyRadius);
            SEVector2f touchLocZ = transParas.mTranslate.getVectorZ();
            double angle = touchLocZ.getAngle_II();
            transParas.mRotate.set((float) (angle * 180 / Math.PI), 0, 0, 1);
            float scale = (mSkyRadius + mCamera.getRadius()) / (mVirtualWallRadius + mCamera.getRadius());
            transParas.mScale.set(scale, scale, scale);
        }
        return transParas;
    }

    private SETransParas rayCrossWall(SERay ray, float wallRadius) {
        // ray cross the front wall
        SETransParas transParas = new SETransParas();
        float y = wallRadius;
        float para = (y - ray.getLocation().getY()) / ray.getDirection().getY();
        transParas.mTranslate = ray.getLocation().add(ray.getDirection().mul(para));
        float faceAngle = 360 / mHouse.mWallNum;
        float tanAngle = (float) Math.tan(faceAngle * Math.PI / 360);
        float halfFaceW = wallRadius * tanAngle;
        if (transParas.mTranslate.getX() < -halfFaceW) {
            // ray cross the left wall
            float Xa = ray.getLocation().getX();
            float Ya = ray.getLocation().getY();
            float Xb = ray.getDirection().getX();
            float Yb = ray.getDirection().getY();
            para = (tanAngle * Xa + tanAngle * halfFaceW + wallRadius - Ya) / (Yb - tanAngle * Xb);
            transParas.mTranslate = ray.getLocation().add(ray.getDirection().mul(para));
            transParas.mRotate.set(faceAngle, 0, 0, 1);
        } else if (transParas.mTranslate.getX() > halfFaceW) {
            // ray cross the right wall
            float Xa = ray.getLocation().getX();
            float Ya = ray.getLocation().getY();
            float Xb = ray.getDirection().getX();
            float Yb = ray.getDirection().getY();
            para = (-tanAngle * Xa + tanAngle * halfFaceW + wallRadius - Ya) / (Yb + tanAngle * Xb);
            transParas.mTranslate = ray.getLocation().add(ray.getDirection().mul(para));
            transParas.mRotate.set(-faceAngle, 0, 0, 1);
        }
        return transParas;

    }

    private SEVector3f rayCrossCylinder(SERay ray, float radius) {
        float Xa = ray.getLocation().getX();
        float Ya = ray.getLocation().getY();
        float Xb = ray.getDirection().getX();
        float Yb = ray.getDirection().getY();
        float a = Xb * Xb + Yb * Yb;
        float b = 2 * (Xa * Xb + Ya * Yb);
        float c = Xa * Xa + Ya * Ya - radius * radius;
        float para;
        if (b * b - 4 * a * c < 0) {
            para = -b / (2 * a);
        } else {
            para = (float) ((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
        }
        SEVector3f touchLoc = ray.getLocation().add(ray.getDirection().mul(para));
        return touchLoc;
    }

    private ObjectSlot calculateSlot() {
        if (onRight() || onLeft() || isInRecycle()) {
            return null;
        }
        return calculateNearestSlot(mRealLocation, false);
    }

    private ObjectSlot calculateNearestSlot(SEVector3f location, boolean handup) {
        ObjectSlot slot = null;
        if (mExistentSlot.size() == mHouse.mWallNum) {
            return slot;
        }
        float wallH = mHouse.mWallHeight;
        float wallW = mHouse.mWallWidth;
        int wallIndex = mHouse.getWallNearestIndex();
        // int index = Math.round(wallIndex - 0.5f);
        // if (index >= 0) {
        // index = index % getSceneInfo().mWallNum;
        // } else {
        // index = getSceneInfo().mWallNum + index % getSceneInfo().mWallNum;
        // if (index == getSceneInfo().mWallNum) {
        // index = 0;
        // }
        // }
        if (location.getZ() < wallH / 5 && !handup) {
            if (location.getX() < 0 && Math.abs(location.getX() + wallW / 2) < wallW / 5) {
                slot = getOnMoveObject().getObjectSlot().clone();
                slot.mSlotIndex = wallIndex;
            } else if (location.getX() > 0 && Math.abs(location.getX() - wallW / 2) < wallW / 5) {
                slot = getOnMoveObject().getObjectSlot().clone();
                slot.mSlotIndex = wallIndex - 1;
                if (slot.mSlotIndex < 0) {
                    slot.mSlotIndex += mHouse.mWallNum;
                }
            }
        } else if (handup && location.getX() < 0) {
            slot = getOnMoveObject().getObjectSlot().clone();
            slot.mSlotIndex = wallIndex;
        } else if (handup && location.getX() >= 0) {
            slot = getOnMoveObject().getObjectSlot().clone();
            slot.mSlotIndex = wallIndex - 1;
            if (slot.mSlotIndex < 0) {
                slot.mSlotIndex += mHouse.mWallNum;
            }
        }

        return slot;
    }

    private void playConflictAnimationTask(ConflictObject conflictObject, long delay) {
        cancelConflictAnimationTask();
        if (conflictObject != null) {
            mPlayConflictAnimationTask = new ConflictAnimationTask(conflictObject);
            if (delay == 0) {
                mPlayConflictAnimationTask.run();
            } else {
                SELoadResThread.getInstance().process(mPlayConflictAnimationTask, delay);
            }
        }
    }

    private void cancelConflictAnimationTask() {
        if (mPlayConflictAnimationTask != null) {
            SELoadResThread.getInstance().cancel(mPlayConflictAnimationTask);
            mPlayConflictAnimationTask = null;
        }
    }

    private class ConflictAnimationTask implements Runnable {
        private ConflictObject mMyConflictObject;

        public ConflictAnimationTask(ConflictObject conflictObject) {
            mMyConflictObject = conflictObject;
        }

        public void run() {
            mMyConflictObject.playConflictAnimation();

        }
    }

    private ConflictAnimationTask mPlayConflictAnimationTask;

    private List<ObjectSlot> searchEmptySlot(List<ConflictObject> existentSlot) {
        boolean[] slot = new boolean[mHouse.mWallNum];
        for (int i = 0; i < mHouse.mWallNum; i++) {
            slot[i] = true;
        }
        for (ConflictObject wallGapObject : existentSlot) {
            slot[wallGapObject.mConflictObject.getObjectInfo().getSlotIndex()] = false;
        }

        List<ObjectSlot> objectSlots = null;
        for (int i = 0; i < mHouse.mWallNum; i++) {
            if (slot[i]) {
                if (objectSlots == null) {
                    objectSlots = new ArrayList<ObjectSlot>();
                }
                ObjectSlot objectSlot = new ObjectSlot();
                objectSlot.mSlotIndex = i;
                objectSlots.add(objectSlot);
            }
        }
        return objectSlots;
    }

    private Runnable mRotateToNextFace = new Runnable() {
        public void run() {
            new SECommand(getHomeScene()) {
                public void run() {
                    if (onLeft()) {
                        mHouse.toLeftFace(new SEAnimFinishListener() {
                            public void onAnimationfinish() {
                                ObjectSlot objectSlot = calculateSlot();
                                if (!cmpSlot(objectSlot, mObjectSlot)) {
                                    mObjectSlot = objectSlot;
                                    ConflictObject conflictSlot = getConflictSlot(objectSlot);
                                    playConflictAnimationTask(conflictSlot, 1000);
                                }
                                if (mPreAction == ACTION.UP) {
                                    cancelRotation();
                                } else {
                                    calculationWallRotation(800);
                                }
                            }
                        }, 1.5f);
                    } else if (onRight()) {
                        mHouse.toRightFace(new SEAnimFinishListener() {
                            public void onAnimationfinish() {
                                ObjectSlot objectSlot = calculateSlot();
                                if (!cmpSlot(objectSlot, mObjectSlot)) {
                                    mObjectSlot = objectSlot;
                                    ConflictObject conflictSlot = getConflictSlot(objectSlot);
                                    playConflictAnimationTask(conflictSlot, 1000);
                                }
                                if (mPreAction == ACTION.UP) {
                                    cancelRotation();
                                } else {
                                    calculationWallRotation(800);
                                }
                            }
                        }, 1.5f);
                    } else {
                        mNeedRotateWall = false;
                    }

                }
            }.execute();
        }
    };

    private void calculationWallRotation(long delayTime) {
        if (onLeft() || onRight()) {
            mNeedRotateWall = true;
            SELoadResThread.getInstance().process(mRotateToNextFace, delayTime);
        } else {
            cancelRotation();
        }
    }

    private boolean onLeft() {
        int screenW = mCamera.getWidth();
        return mTouchX - getOnMoveObject().getAdjustTouch().getX() < screenW * 0.1f;
    }

    private boolean onRight() {
        int screenW = mCamera.getWidth();
        return mTouchX - getOnMoveObject().getAdjustTouch().getX() > screenW * 0.9f;
    }

    private void cancelRotation() {
        mNeedRotateWall = false;
        SELoadResThread.getInstance().cancel(mRotateToNextFace);
        mHouse.stopAllAnimation(null);
    }

    private ConflictObject getConflictSlot(ObjectSlot cmpSlot) {
        if (cmpSlot == null) {
            return null;
        }
        for (ConflictObject wallGapObject : mExistentSlot) {
            if (wallGapObject.mConflictObject.getObjectInfo().getSlotIndex() == cmpSlot.mSlotIndex) {
                wallGapObject.mMoveSlot = wallGapObject.mConflictObject.getObjectInfo().mObjectSlot.clone();
                List<ObjectSlot> emptySlots = searchEmptySlot(mExistentSlot);
                if (emptySlots != null) {
                    int minDistance = Integer.MAX_VALUE;
                    for (ObjectSlot emptySlot : emptySlots) {
                        int distance = (int) getTranslateInVessel(wallGapObject.mConflictObject.getObjectInfo().mObjectSlot)
                                .subtract(getTranslateInVessel(emptySlot)).getLength();
                        if (distance < minDistance) {
                            minDistance = distance;
                            wallGapObject.mMoveSlot.mSlotIndex = emptySlot.mSlotIndex;
                        } else if (distance == minDistance) {
                            if (emptySlot.mSlotIndex == mHouse.getWallNearestIndex()
                                    || emptySlot.mSlotIndex == mHouse.getWallNearestIndex() - 1
                                    || emptySlot.mSlotIndex == mHouse.getWallNearestIndex() - 1 + mHouse.mWallNum) {
                                wallGapObject.mMoveSlot.mSlotIndex = emptySlot.mSlotIndex;
                            }
                        }
                    }
                } else {
                    wallGapObject.mMoveSlot = null;
                }
                return wallGapObject;
            }
        }
        return null;
    }

    private List<ConflictObject> getExistentSlot() {
        List<ConflictObject> fillSlots = new ArrayList<ConflictObject>();
        for (SEObject object : getVesselChildren()) {
            if (object instanceof NormalObject) {
                NormalObject wallObject = (NormalObject) object;
                ObjectInfo objInfo = wallObject.getObjectInfo();
                if (objInfo.mSlotType == ObjectInfo.SLOT_TYPE_GROUND) {
                    ConflictObject conflictObject = new ConflictObject();
                    conflictObject.mConflictObject = wallObject;
                    fillSlots.add(conflictObject);
                }
            }
        }
        return fillSlots;
    }

    @Override
    protected SETransParas worldToVessel(SETransParas worldTransParas) {
        SETransParas wallTransParas = new SETransParas();
        SEVector2f touchLocZ = worldTransParas.mTranslate.getVectorZ();
        float objectToWorldAngle = (float) (touchLocZ.getAngle_II() * 180 / Math.PI);
        float wallToWorldAngle = mHouse.getAngle();
        float objectLocationAngle = objectToWorldAngle - wallToWorldAngle;
        float r = touchLocZ.getLength();
        float x = (float) (-r * Math.sin(objectLocationAngle * Math.PI / 180));
        float y = (float) (r * Math.cos(objectLocationAngle * Math.PI / 180));
        float z = worldTransParas.mTranslate.getZ();
        float objectToWallAngle = worldTransParas.mRotate.getAngle() - wallToWorldAngle;
        wallTransParas.mTranslate.set(x, y, z);
        wallTransParas.mRotate.set(objectToWallAngle, 0, 0, 1);
        wallTransParas.mScale = worldTransParas.mScale.clone();
        return wallTransParas;
    }

    @Override
    protected void playSlotAnimation(final SETransParas srcTransParas, final SETransParas desTransParas,
            final SEAnimFinishListener l) {
        mHouse.toNearestFace(new SEAnimFinishListener() {
            public void onAnimationfinish() {
                playMoveAnimation(srcTransParas, desTransParas, l);
            }
        }, 1.5f);
    }

    @Override
    public void handleOutsideRoom() {
        getOnMoveObject().handleOutsideRoom();
    }

    @Override
    public void handleNoMoreRoom() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.no_room, Toast.LENGTH_SHORT).show();
            }
        });
        getOnMoveObject().handleNoMoreRoom();
        getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();
        getOnMoveObject().handleSlotSuccess();
    }

    private class ConflictObject {
        public ConflictAnimation mConflictAnimation;
        public NormalObject mConflictObject;
        public ObjectSlot mMoveSlot;

        // confirm to next position
        private void playConflictAnimation() {
            if (mConflictAnimation != null) {
                mConflictAnimation.stop();
            }
            if (mMoveSlot == null) {
                return;
            }
            mConflictObject.getObjectSlot().set(mMoveSlot);
            mConflictAnimation = new ConflictAnimation(getHomeScene(), 3);
            mConflictAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mConflictObject.getObjectInfo().updateSlotDB();
                }
            });
            mConflictAnimation.execute();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            ConflictObject cmp = (ConflictObject) obj;
            return (mConflictObject.equals(cmp.mConflictObject));
        }

        private class ConflictAnimation extends CountAnimation {

            private SETransParas mDesTransParas;
            private SETransParas mSrcTransParas;
            private float mStep;
            private float mCurProcess;
            private boolean mIsEnbaleBlending;
            private boolean mHasGetBlending;

            public ConflictAnimation(SEScene scene, float step) {
                super(scene);
                mDesTransParas = getTransParasInVessel(null, mConflictObject.getObjectSlot());
                mSrcTransParas = mConflictObject.getUserTransParas().clone();
                mStep = step;
                mCurProcess = 0;
            }

            @Override
            public void runPatch(int count) {
                float needTranslate = 100 - mCurProcess;
                float absNTX = Math.abs(needTranslate);
                if (absNTX <= mStep) {
                    mCurProcess = 100;
                    stop();
                } else {
                    int step = (int) (mStep * Math.sqrt(absNTX));
                    if (needTranslate < 0) {
                        step = -step;
                    }
                    mCurProcess = mCurProcess + step;

                }
                float step = mCurProcess / 100;
                mConflictObject.getUserTransParas().mTranslate = mSrcTransParas.mTranslate
                        .add(mDesTransParas.mTranslate.subtract(mSrcTransParas.mTranslate).mul(step));
                mConflictObject.getUserTransParas().mScale = mSrcTransParas.mScale.add(mDesTransParas.mScale.subtract(
                        mSrcTransParas.mScale).mul(step));
                float desAngle = mDesTransParas.mRotate.getAngle();
                float srcAngle = mSrcTransParas.mRotate.getAngle();
                if (desAngle - srcAngle > 180) {
                    desAngle = desAngle - 360;
                } else if (desAngle - srcAngle < -180) {
                    desAngle = desAngle + 360;
                }
                float curAngle = srcAngle + (desAngle - srcAngle) * step;
                mConflictObject.getUserTransParas().mRotate.set(curAngle, 0, 0, 1);
                mConflictObject.setUserTransParas();
            }

            @Override
            public void onFirstly(int count) {
                if (!mHasGetBlending) {
                    mIsEnbaleBlending = mConflictObject.isBlendingable();
                    mHasGetBlending = true;
                }
                if (!mIsEnbaleBlending) {
                    mConflictObject.setBlendingable(true);
                }
                mConflictObject.setAlpha(0.1f);
            }

            @Override
            public void onFinish() {
                mConflictObject.getUserTransParas().set(mDesTransParas);
                mConflictObject.setUserTransParas();
                if (mHasGetBlending) {
                    mConflictObject.setAlpha(1);
                    if (!mIsEnbaleBlending) {
                        mConflictObject.setBlendingable(false);
                    } else {
                        mConflictObject.setBlendingable(true);
                    }
                }
            }
        }
    }

}
