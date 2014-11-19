package com.borqs.se.widget3d;


import android.content.pm.ActivityInfo;

import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.widget3d.House.WallRadiusChangedListener;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class Ground extends VesselObject implements WallRadiusChangedListener {
    private House mHouse;
    public Ground(HomeScene scene, String name, int index) {
        super(scene, name, index);
        setPressType(PRESS_TYPE.NONE);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        if (getParent() instanceof House) {
            mHouse = (House) getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        setVesselLayer(new GroundLayer(getHomeScene(), this));
        getHomeScene().addWallRadiusChangedListener(this);
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        SETransParas transparas = new SETransParas();
        float angle = (objectSlot.mSlotIndex + 0.5f) * 360.f / mHouse.mWallNum;
        SEVector2f yDirection = new SEVector2f((float) Math.cos((angle + 90) * Math.PI / 180),
                (float) Math.sin((angle + 90) * Math.PI / 180));
        SEVector2f xDirection = new SEVector2f((float) Math.cos(angle * Math.PI / 180), (float) Math.sin(angle
                * Math.PI / 180));
        float offsetY;
        if (HomeManager.getInstance().isLandscapeOrientation()) {
            offsetY = mHouse.mWallRadius * 1.268f;
        } else {
            offsetY = mHouse.mWallRadius;
        }
        float offsetX = 0;
        SEVector2f offset = yDirection.mul(offsetY).add(xDirection.mul(offsetX));
        float offsetZ = 0;
        float z = offsetZ;
        transparas.mTranslate.set(offset.getX(), offset.getY(), z);
        transparas.mRotate.set(angle, 0, 0, 1);
        return transparas;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        getHomeScene().removeWallRadiusChangedListener(this);
    }

    @Override
    public void onWallRadiusChanged(int faceIndex) {
        int wallNum = mHouse.mWallNum;
        int curIndex = faceIndex;
        int showFaceIndexA = curIndex - 1;
        if (showFaceIndexA < 0) {
            showFaceIndexA = wallNum - 1;
        }
        int showFaceIndexB = curIndex;
        int showFaceIndexC = curIndex + 1;
        if (showFaceIndexC > wallNum - 1) {
            showFaceIndexC = 0;
        }
        for (SEObject child : getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                int placeIndex = normalObject.getObjectInfo().getSlotIndex();
                if (placeIndex == showFaceIndexA || placeIndex == showFaceIndexB || placeIndex == showFaceIndexC) {
                    normalObject.setVisible(true);
                } else {
                    normalObject.setVisible(false);
                }
            }
        }
    }
}
