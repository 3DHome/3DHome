package com.borqs.se.widget3d;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.home3d.HomeScene;

public class DragDeskLayer extends VesselLayer {
    private SECamera mCamera;
    private SETransParas mObjectTransParas;

    public DragDeskLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mCamera = scene.getCamera();
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        return "DeskLand".equals(object.getObjectInfo().mType) || "DeskPort".equals(object.getObjectInfo().mType)
                || "Airship".equals(object.getObjectInfo().mType) || "Shelf".equals(object.getObjectInfo().mType)
                || object.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_UNKNOWN;
    }

    @Override
    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        stopMoveAnimation();
        updateRecycleStatus(event, x, y);
        setMovePoint((int) x, (int) y);
        switch (event) {
        case BEGIN:
            // Move事件刚开始发生时矫正物体的位置，可以播放移动动画来矫正物体的位置
            SETransParas srcTransParas = getOnMoveObject().getUserTransParas().clone();
            SETransParas desTransParas = mObjectTransParas.clone();
            playMoveAnimation(srcTransParas, desTransParas, null);
            break;
        case MOVE:
        case UP:
        case FLY:
            getOnMoveObject().getUserTransParas().set(mObjectTransParas);
            getOnMoveObject().setUserTransParas();
            break;
        case FINISH:
            if (isInRecycle()) {
                // 手抬起来的时候假如物体在垃圾框内，那么删除物体
                handleOutsideRoom();
            } else {
                slotToHouse();
            }
            break;
        }
        return true;
    }

    /**
     * 计算物体是否处在垃圾框中
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

    private void setMovePoint(int touchX, int touchY) {
        SERay ray = mCamera.screenCoordinateToRay(touchX, touchY);
        mObjectTransParas = getObjectTransParas(ray);
    }

    private SETransParas getObjectTransParas(SERay ray) {
        SETransParas transParas = rayCrossYFace(ray, 0);
        int minZ = 0;
        if (transParas.mTranslate.getZ() < minZ) {
            transParas.mTranslate.mD[2] = minZ;
        }
        return transParas;
    }

    private SETransParas rayCrossYFace(SERay ray, float faceY) {
        // ray cross the front wall
        SETransParas transParas = new SETransParas();
        float y = faceY;
        float para = (y - ray.getLocation().getY()) / ray.getDirection().getY();
        transParas.mTranslate = ray.getLocation().add(ray.getDirection().mul(para));
        return transParas;

    }

    private void slotToHouse() {
        SETransParas srcTransParas = getOnMoveObject().getUserTransParas().clone();
        SETransParas desTransParas = new SETransParas();
        if ("Shelf".equals(getOnMoveObject().getObjectInfo().mType)) {
            float z = getHomeScene().getHouse().mWallHeight / 2;
            float y = getHomeScene().getHouse().mWallRadius;
            desTransParas.mTranslate.set(0, y, z);
        } else if ("Airship".equals(getOnMoveObject().getObjectInfo().mType)) {
            desTransParas.mTranslate.set(0, 0, getHomeScene().getHouse().mWallHeight + 200);
        }
        playMoveAnimation(srcTransParas, desTransParas, new SEAnimFinishListener() {
            public void onAnimationfinish() {
                if ("DeskLand".equals(getOnMoveObject().getObjectInfo().mType)
                        || "DeskPort".equals(getOnMoveObject().getObjectInfo().mType)) {
                    handleSlotSuccess();
                } else {
                    getOnMoveObject().setOnMove(false);
                    getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
                }
            }
        });
    }

    @Override
    public void handleOutsideRoom() {
        getOnMoveObject().handleOutsideRoom();
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();
        getOnMoveObject().handleSlotSuccess();
    }
}
