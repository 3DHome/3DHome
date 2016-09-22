package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class DeskLayer extends VesselLayer {
    private MoveObject mConflictObject;
    private SECamera mCamera;
    private SEVector3f mRealLocation;
    private SETransParas mObjectTransParas;
    private boolean mHasChangedSlot;
    private List<SEVector3f> mAllLocationOnDesk;

    public DeskLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mCamera = scene.getCamera();
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        return object.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            getOnMoveObject().changeSlotType(ObjectInfo.SLOT_TYPE_DESKTOP);
            setInRecycle(false);
            mConflictObject = null;
            mHasChangedSlot = false;
            mAllLocationOnDesk = getAllLocationInVessel();
        } else {
            cancelConflictTask();
            clearMoveSlot();
            playAllConflictObjectsAnimation(false);
        }
        return true;
    }

    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        stopMoveAnimation();
        updateRecycleStatus(event, x, y);
        // 获取物体随着手移动的位置
        setMovePoint((int) x, (int) y);
        // 获取物体在桌面的最近的slot
        ObjectSlot objectSlot = calculateSlot();
        // 判断物体slot是否已经改变
        boolean slotChanged = !cmpSlot(objectSlot, getMoveObject().mMoveSlot);
        // 假如物体的slot已经改变或者Move事件刚开始发生，获取和该物体有冲突的物体
        if (event == ACTION.BEGIN || slotChanged) {
            mHasChangedSlot = true;
            getMoveObject().mMoveSlot = objectSlot;
            cancelConflictTask();
            mConflictObject = getConflictSlot(objectSlot);
        }
        switch (event) {
        case BEGIN:
            // Move事件刚开始发生时矫正物体的位置，可以播放移动动画来矫正物体的位置
            SETransParas srcTransParas = getOnMoveObject().getUserTransParas().clone();
            SETransParas desTransParas = mObjectTransParas.clone();
            playMoveAnimation(srcTransParas, desTransParas, null);
            // 假如物体slot有变化且有冲突的物体，那么隔600ms再把有冲突的物体移动到其它的空位置
            // 假如物体slot有变化但是没有有冲突的物体，那么通过播放动画来复位所有的物体
            if (mHasChangedSlot) {
                if (mConflictObject != null) {
                    performConflictTask(null, 600);
                } else {
                    clearMoveSlot();
                    playAllConflictObjectsAnimation(false);
                }
            }
            mHasChangedSlot = false;
            break;
        case MOVE:
        case UP:
        case FLY:
            // 物体随着手移动
            getOnMoveObject().getUserTransParas().set(mObjectTransParas);
            getOnMoveObject().setUserTransParas();
            // 假如物体slot有变化且有冲突的物体，那么隔400ms再把有冲突的物体移动到其它的空位置
            // 假如物体slot有变化但是没有有冲突的物体，那么通过播放动画来复位所有的物体
            if (mHasChangedSlot) {
                if (mConflictObject != null) {
                    performConflictTask(null, 400);
                } else {
                    clearMoveSlot();
                    playAllConflictObjectsAnimation(false);
                }
            }
            mHasChangedSlot = false;
            break;
        case FINISH:
            if (isInRecycle()) {
                // 手抬起来的时候假如物体在垃圾框内，那么删除物体
                handleOutsideRoom();
            } else {
                cancelConflictTask();
                // 手抬起来的时候假如物体不在垃圾框内，那么把物体直接摆放在桌面上，同时假如有冲突的物体，那么把有冲突的物体移动到其它的空位置
                slotToDesk();
                mHasChangedSlot = false;

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


    private void slotToDesk() {
        getMoveObject().mMoveSlot = calculateNearestSlot(mRealLocation, true);
        if (getMoveObject().mMoveSlot == null) {
            clearMoveSlot();
            playAllConflictObjectsAnimation(true);
            ObjectSlot objectSlot = getOnMoveObject().getObjectSlot();
            if (objectSlot.mVesselID < 0) {
                getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
                handleNoMoreRoom();
            } else {
                if (!getOnMoveObject().resetPostion()) {
                    getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
                }
                handleNoMoreRoom();
            }
        } else {
            mConflictObject = getConflictSlot(getMoveObject().mMoveSlot);
            if (mConflictObject == null) {
                clearMoveSlot();
                playAllConflictObjectsAnimation(true);
                playSlotAnimation(getMoveObject().mMoveSlot, new SEAnimFinishListener() {
                    public void onAnimationfinish() {
                        handleSlotSuccess();
                    }
                });
            } else {
                if (canPlaceToDesk()) {
                    playAllConflictObjectsAnimation(true);
                    playSlotAnimation(getMoveObject().mMoveSlot, new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            handleSlotSuccess();
                        }
                    });
                } else {
                    throw new IllegalArgumentException("wrong slot on desk: desk is full");
                }
            }
        }

    }

    @Override
    protected boolean placeObjectToVesselDirectly(NormalObject normalObject) {
        getOnMoveObject().changeSlotType(ObjectInfo.SLOT_TYPE_DESKTOP);
        return super.placeObjectToVesselDirectly(normalObject);
    }

    protected void onConflictTaskRun(MOVE_DIRECTION mStartDirection) {
        if (mConflictObject == null) {
            return;
        }
        if (!canPlaceToDesk()) {
            clearMoveSlot();
        }
        playAllConflictObjectsAnimation(false);
    }

    private boolean canPlaceToDesk() {
        clearMoveSlot();
        List<ObjectSlot> emptySlots = searchEmptySlot(getExistentObjects(false));
        if (emptySlots == null) {
            return false;
        }
        mConflictObject.mMoveSlot = mConflictObject.getObjectSlot().clone();
        float minDistance = Float.MAX_VALUE;
        SEVector3f currentPosition = getTranslateInVessel(mConflictObject.mMoveSlot);
        for (ObjectSlot emptySlot : emptySlots) {
            float distance = currentPosition.subtract(getTranslateInVessel(emptySlot)).getLength();
            if (distance < minDistance) {
                minDistance = distance;
                mConflictObject.mMoveSlot.mSlotIndex = emptySlot.mSlotIndex;
            }
        }
        return true;
    }

    protected List<MoveObject> getExistentObjects(boolean reset) {
        if (!reset && mExistentObjects != null) {
            return mExistentObjects;
        }
        mExistentObjects = new ArrayList<MoveObject>();
        for (SEObject child : getVesselChildren()) {
            if (child instanceof NormalObject) {
                NormalObject desktopObject = (NormalObject) child;
                if (desktopObject.getUserTranslate().getZ() > -10000) {
                    MoveObject conflictObject = new MoveObject();
                    conflictObject.mMoveObject = desktopObject;
                    mExistentObjects.add(conflictObject);
                }
            }
        }
        return mExistentObjects;
    }

    public void setMovePoint(int touchX, int touchY) {
        SERay Ray = mCamera.screenCoordinateToRay(touchX, touchY);
        mRealLocation = getTouchLocation(Ray, getVesselObject().getObjectSize().getZ());
        SEVector3f touchLocation = getTouchLocation(Ray, getVesselObject().getObjectSize().getZ() + 5);
        mObjectTransParas = new SETransParas();
        mObjectTransParas.mTranslate = touchLocation;
        mObjectTransParas.mRotate.set(getVesselObject().getAngle(), 0, 0, 1);
    }

    private SEVector3f getTouchLocation(SERay ray, float z) {
        SEVector3f touchLocZ = rayCrossZFace(ray, z);
        float distance = touchLocZ.getVectorZ().getLength();
        if (distance > getHomeScene().getHouse().mSkyRadius * 0.8f) {
            distance = getHomeScene().getHouse().mSkyRadius * 0.8f;
            if (touchLocZ.getY() > 0) {
                touchLocZ = rayCrossCylinder(ray, distance);
            }
        }
        return touchLocZ;
    }

    private SEVector3f rayCrossZFace(SERay ray, float z) {
        float para = (z - ray.getLocation().getZ()) / ray.getDirection().getZ();
        SEVector3f touchLoc = ray.getLocation().add(ray.getDirection().mul(para));
        if ((z < ray.getLocation().getZ() && ray.getDirection().getZ() > 0)
                || (z > ray.getLocation().getZ() && ray.getDirection().getZ() < 0)) {
            touchLoc.mD[0] = -touchLoc.getX();
            touchLoc.mD[1] = -touchLoc.getY();
        }
        return touchLoc;
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
        if (isInRecycle()) {
            return null;
        }
        return calculateNearestSlot(mRealLocation, false);
    }

    private ObjectSlot calculateNearestSlot(SEVector3f location, boolean handup) {
        if (getExistentObjects(false).size() == getVesselCapability()) {
            return null;
        }
        float distance = Float.MAX_VALUE;
        float maxDistance = Float.MAX_VALUE;
        if (!handup) {
            maxDistance = 200;
        }
        ObjectSlot slot = getOnMoveObject().getObjectSlot().clone();
        int index = 0;
        mAllLocationOnDesk = getAllLocationInVessel();
        for (SEVector3f canSlot : mAllLocationOnDesk) {
            float cmpDistance = location.subtract(canSlot).getLength();
            if (cmpDistance < distance && cmpDistance < maxDistance) {
                distance = cmpDistance;
                slot.mSlotIndex = index;
            }
            index++;
        }
        if (slot.mSlotIndex >= 0) {
            return slot;
        }
        return null;
    }

    private List<ObjectSlot> searchEmptySlot(List<MoveObject> existentSlot) {
        boolean[] slot = new boolean[getVesselCapability()];
        for (int i = 0; i < getVesselCapability(); i++) {
            slot[i] = true;
        }
        for (MoveObject desktopObject : existentSlot) {
            slot[desktopObject.mMoveObject.getObjectInfo().getSlotIndex()] = false;
        }

        List<ObjectSlot> objectSlots = null;
        for (int i = 0; i < getVesselCapability(); i++) {
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

    private MoveObject getConflictSlot(ObjectSlot cmpSlot) {
        if (cmpSlot == null) {
            return null;
        }
        for (MoveObject desktopObject : getExistentObjects(false)) {
            if (desktopObject.mMoveObject.getObjectInfo().getSlotIndex() == cmpSlot.mSlotIndex) {
                return desktopObject;
            }
        }
        return null;
    }

    @Override
    public void handleOutsideRoom() {
        getOnMoveObject().handleOutsideRoom();
    }

    @Override
    public void handleNoMoreRoom() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.no_room_desk, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        getOnMoveObject().handleNoMoreRoom();
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();
        getOnMoveObject().handleSlotSuccess();
    }


}
