package com.borqs.se.widget3d;

import java.util.List;

import com.borqs.se.home3d.HomeScene;

public class HouseLayer extends VesselLayer {
    private VesselLayer mCurrentLayer;
    private List<VesselLayer> mAllLayer;

    public HouseLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mCurrentLayer = null;
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        int slotType = object.getObjectInfo().mSlotType;
        return slotType == ObjectInfo.SLOT_TYPE_WALL || slotType == ObjectInfo.SLOT_TYPE_APP_WALL
                || slotType == ObjectInfo.SLOT_TYPE_BOOKSHELF || slotType == ObjectInfo.SLOT_TYPE_GROUND;

    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            if (mAllLayer == null) {
                mAllLayer = getHomeScene().getDragDropLayerInHouse();
            }
            for (VesselLayer vesselLayer : mAllLayer) {
                if (vesselLayer.canHandleSlot(getOnMoveObject(), 0, 0)) {
                    mCurrentLayer = vesselLayer;
                    mCurrentLayer.setBoundOfRecycle(mBoundOfRecycle);
                    mCurrentLayer.setOnLayerModel(getOnMoveObject(), true);
                    break;
                }
            }
        } else {
            if (mCurrentLayer != null) {
                mCurrentLayer.setOnLayerModel(onMoveObject, false);
                mCurrentLayer = null;
            }
        }
        return true;
    }

    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        if (mCurrentLayer != null) {
            mCurrentLayer.onObjectMoveEvent(event, x, y);
            if (mCurrentLayer != null) {
                setInRecycle(mCurrentLayer.isInRecycle());
            }
        }
        return true;
    }

    @Override
    public boolean placeObjectToVessel(NormalObject normalObject) {
        super.placeObjectToVessel(normalObject);
        if (mAllLayer == null) {
            mAllLayer = getHomeScene().getDragDropLayerInHouse();
        }
        for (VesselLayer vesselLayer : mAllLayer) {
            if (vesselLayer.canHandleSlot(normalObject, 0, 0)) {
                return vesselLayer.placeObjectToVessel(normalObject);
            }
        }
        return false;
    }
}
