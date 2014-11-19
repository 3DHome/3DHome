package com.borqs.se.widget3d;

import java.util.List;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class WallLayer extends VesselLayer {
    private ObjectSlot mObjectSlot;
    private SECamera mCamera;
    private boolean mOnRotateWall;
    private SEVector3f mRealLocation;
    private SETransParas mObjectTransParas;
    private ACTION mPreAction;
    private List<WallCellLayer> mAllLayer;
    private VesselLayer mCurrentLayer;
    private float mVirtualWallRadius;
    private float mSkyRadius;
    private boolean mIsLandscapeTheme;
    private House mHouse;

    public WallLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mHouse = (House) vesselObject;
        mCurrentLayer = null;
        mCamera = scene.getCamera();
        if (HomeManager.getInstance().isLandscapeOrientation()) {
            mIsLandscapeTheme = true;
        } else {
            mIsLandscapeTheme = false;
        }
        mVirtualWallRadius = mHouse.mWallRadius * 0.8f;
        mSkyRadius = mHouse.mSkyRadius * 0.6f;
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        int slotType = object.getObjectInfo().mSlotType;
        if (slotType == ObjectInfo.SLOT_TYPE_WALL || slotType == ObjectInfo.SLOT_TYPE_APP_WALL
                || slotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            return true;
        }
        return false;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            if (null == mAllLayer) {
                mAllLayer = getHomeScene().getAllWallCellLayerList();
            }
            mObjectSlot = null;
            setInRecycle(false);
        } else {
            disableCurrentLayer();
            cancelRotation();
            getHomeScene().toNearestFace();
        }
        return true;
    }
    
    private WallCellLayer findVesselLayer(int index) {
        for (WallCellLayer vesselLayer : mAllLayer) {
            if (index == vesselLayer.getWallIndex()) {
                return vesselLayer;
            }
        }
        return null;
    }

    private float mTouchX;
    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        mTouchX = x;
        stopMoveAnimation();
        updateRecycleStatus(event, x, y);
        setMovePoint((int) x, (int) y);
        ObjectSlot objectSlot;
        if (event == ACTION.FINISH) {
            objectSlot = calculateSlot(true);
        } else {
            objectSlot = calculateSlot(false);
        }
        if (!cmpSlot(objectSlot, mObjectSlot)) {
            mObjectSlot = objectSlot;
            switchLayer(objectSlot);
        }
        switch (event) {
        case BEGIN:
            mPreAction = ACTION.BEGIN;
            mOnRotateWall = false;
            SETransParas srcTransParas = getOnMoveObject().getUserTransParas().clone();
            SETransParas desTransParas = mObjectTransParas.clone();
            playMoveAnimation(srcTransParas, desTransParas, new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    if (!mOnRotateWall) {
                        calculationWallRotation(800);
                    }
                }
            });
            if (mCurrentLayer != null) {
                return mCurrentLayer.onObjectMoveEvent(event, mRealLocation);
            }
            break;
        case MOVE:
            mPreAction = ACTION.MOVE;
            applyMovementTransParas();
            if (mCurrentLayer != null) {
                return mCurrentLayer.onObjectMoveEvent(event, mRealLocation);
            }
            if (!mOnRotateWall) {
                calculationWallRotation(400);
            }
            break;
        case UP:
            mPreAction = ACTION.UP;
            applyMovementTransParas();
            if (mCurrentLayer != null) {
                return mCurrentLayer.onObjectMoveEvent(event, mRealLocation);
            }
            cancelRotation();
            getHomeScene().toNearestFace();
            break;
        case FLY:
            applyMovementTransParas();
            if (mCurrentLayer != null) {
                return mCurrentLayer.onObjectMoveEvent(event, mRealLocation);
            }
            break;
        case FINISH:
            applyMovementTransParas();
            if (isInRecycle()) {
                handleOutsideRoom();
            } else if (mCurrentLayer != null) {
                return mCurrentLayer.onObjectMoveEvent(event, mRealLocation);
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

    @Override
    public void handleOutsideRoom() {
        getOnMoveObject().handleOutsideRoom();
    }

    private void switchLayer(ObjectSlot objectSlot) {     
        if (objectSlot == null) {
            disableCurrentLayer();
            return;
        }
        VesselLayer vesselLayer = findVesselLayer(objectSlot.mSlotIndex);
        if (mCurrentLayer != vesselLayer) {
            disableCurrentLayer();
            vesselLayer.setOnLayerModel(getOnMoveObject(), true);
            vesselLayer.onObjectMoveEvent(ACTION.BEGIN, mRealLocation);
            mCurrentLayer = vesselLayer;
        }
    }

    private void disableCurrentLayer() {
        if (mCurrentLayer != null) {
            mCurrentLayer.setOnLayerModel(getOnMoveObject(), false);
            mCurrentLayer = null;
        }
    }

    public void setMovePoint(int touchX, int touchY) {
        SERay ray = mCamera.screenCoordinateToRay(touchX, touchY);
        mRealLocation = rayCrossWall(ray,mHouse.mWallRadius).mTranslate;
        mObjectTransParas = getObjectTransParas(ray);
    }

    private SETransParas getObjectTransParas(SERay ray) {
        SETransParas transParas = rayCrossWall(ray, mVirtualWallRadius);
        int minZ = (int) (getOnMoveObject().getObjectSlot().mSpanY * mHouse.mCellHeight / 2);
        int maxZ = (int) (mHouse.mWallHeight - minZ);
        if (transParas.mTranslate.getZ() < minZ) {
            transParas.mTranslate.mD[2] = minZ;
        } else if (!mIsLandscapeTheme && transParas.mTranslate.getZ() > maxZ) {
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
            para = (float) (-b / (2 * a));
        } else {
            para = (float) ((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
        }
        SEVector3f touchLoc = ray.getLocation().add(ray.getDirection().mul(para));
        return touchLoc;
    }

    private Runnable mRotateToNextFace = new Runnable() {
        public void run() {
            new SECommand(getHomeScene()) {
                public void run() {
                    if (onLeft()) {
                        getHomeScene().toLeftWallFace(new SEAnimFinishListener() {
                            public void onAnimationfinish() {
                                if (mPreAction == VesselLayer.ACTION.UP) {
                                    cancelRotation();
                                } else {
                                    calculationWallRotation(600);
                                }
                            }
                        });
                    } else if (onRight()) {
                        getHomeScene().toRightWallFace(new SEAnimFinishListener() {
                            public void onAnimationfinish() {
                                if (mPreAction == ACTION.UP) {
                                    cancelRotation();
                                } else {
                                    calculationWallRotation(600);
                                }
                            }
                        });
                    } else {
                        mOnRotateWall = false;
                    }
                }
            }.execute();
        }
    };

    private void calculationWallRotation(long delayTime) {
        if (onLeft() || onRight()) {
            mOnRotateWall = true;
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
        mOnRotateWall = false;
        SELoadResThread.getInstance().cancel(mRotateToNextFace);
        getHomeScene().stopHouseAnimation();
    }

    private ObjectSlot calculateSlot(boolean force) {
        if (isInRecycle()) {
            return null;
        }
        if (!force) {
            if (onRight() || onLeft()) {
                return null;
            }
        }
        ObjectSlot slot = getOnMoveObject().getObjectSlot().clone();
        slot.mSlotIndex = getHomeScene().getWallNearestIndex();
        return slot;
    }

    @Override
    public boolean placeObjectToVessel(NormalObject normalObject) {
        super.placeObjectToVessel(normalObject);
        return getHomeScene().placeToNearestWallFace(normalObject);
    }

    private void applyMovementTransParas() {
        getOnMoveObject().setUserTransParas(mObjectTransParas);
    }
}
