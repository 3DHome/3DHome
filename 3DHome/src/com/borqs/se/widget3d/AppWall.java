package com.borqs.se.widget3d;

import android.graphics.Rect;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class AppWall extends VesselObject {

    private float mScaleX;
    private float mScaleY;
    private float mGridSizeX;
    private float mGridSizeY;
    private float mScaleOfChild;
    private float mMoveY;
    private int mFrameWidth;
    private int mFrameHeight;
    private House mHouse;

    public AppWall(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mFrameWidth = 0;
        mFrameHeight = 0;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanBeResized(true);
        setCanChangeBind(false);
        setVesselLayer(new AppWallLayer((HomeScene) getScene(), this));
        LauncherModel.getInstance().addAppCallBack(this);
        setOnClickListener(null);
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        mFrameWidth = (int) (2 * Math.max(getObjectInfo().mModelInfo.mMaxPoint.getX(),
                -getObjectInfo().mModelInfo.mMinPoint.getX()));
        mFrameHeight = (int) (2 * Math.max(getObjectInfo().mModelInfo.mMaxPoint.getZ(),
                -getObjectInfo().mModelInfo.mMinPoint.getZ()));
        resizeFrame();

        mGridSizeX = mFrameWidth * mScaleX / getObjectInfo().mObjectSlot.mSpanX;
        mGridSizeY = mFrameHeight * mScaleY / getObjectInfo().mObjectSlot.mSpanY;
        mScaleOfChild = Math.min(mGridSizeX / mHouse.mCellWidth, mGridSizeY / mHouse.mCellHeight) * 0.88f;
        mMoveY = -Math.max(getObjectInfo().mModelInfo.mMaxPoint.getY(), -getObjectInfo().mModelInfo.mMinPoint.getY()) * 0.3f;
    }
    @Override
    public void onSizeAndPositionChanged(Rect sizeRect) {
        getObjectSlot().mSpanX = sizeRect.width();
        getObjectSlot().mSpanY = sizeRect.height();
        getObjectSlot().mStartX = sizeRect.left;
        getObjectSlot().mStartY = sizeRect.top;
        getObjectInfo().updateSlotDB();
        resizeFrame();
        if (getParent() != null && (getParent() instanceof VesselObject)) {
            VesselObject vessel = (VesselObject) getParent();
            SETransParas transParas = vessel.getTransParasInVessel(this, getObjectSlot());
            if (transParas != null) {
                getUserTransParas().set(transParas);
                setUserTransParas();
            }
        }
        updateChildPostion();
    }

    private void resizeFrame() {
        getScale(getObjectSlot().mSpanX, getObjectSlot().mSpanY);
        SEObject model = getModelComponenet();
        model.setUserScale(new SEVector3f(mScaleX, 1, mScaleY));
    }

    public void getScale(int spanX, int spanY) {
        float newW = getWidthOfWidget(spanX);
        float newH = getHeightOfWidget(spanY);
        mScaleX = newW / mFrameWidth;
        mScaleY = newH / mFrameHeight;
    }
    private float getWidthOfWidget(float spanX) {
        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        return spanX * gridSizeX - mHouse.mWidthGap;
    }

    private float getHeightOfWidget(float spanY) {
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        return spanY * gridSizeY - mHouse.mHeightGap;
    }

    private void updateChildPostion() {
        mGridSizeX = mFrameWidth * mScaleX / getObjectInfo().mObjectSlot.mSpanX;
        mGridSizeY = mFrameHeight * mScaleY / getObjectInfo().mObjectSlot.mSpanY;
        mScaleOfChild = Math.min(mGridSizeX / mHouse.mCellWidth, mGridSizeY / mHouse.mCellHeight) * 0.88f;
        for (SEObject child : getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                normalObject.setUserTransParas(getTransParasInVessel(normalObject, normalObject.getObjectSlot()));
            }
        }
    }
    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        super.handleBackKey(l);
        if (getHomeScene().getDragLayer().isOnResize()) {
            getHomeScene().getDragLayer().finishResize();
            return true;
        }
        return false;
    }
    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        SETransParas transparas = new SETransParas();
        ObjectSlot vesselSlot = getObjectInfo().mObjectSlot;
        if (objectSlot.mStartX + objectSlot.mSpanX > vesselSlot.mSpanX
                || objectSlot.mStartY + objectSlot.mSpanY > vesselSlot.mSpanY) {
            transparas.mTranslate.set(0, -10000, 0);
            transparas.mScale.set(0, 0, 0);
            return transparas;
        } else {
            float offsetX = (objectSlot.mStartX + objectSlot.mSpanX / 2.f) * mGridSizeX - vesselSlot.mSpanX
                    * mGridSizeX / 2.f;
            float offsetZ = vesselSlot.mSpanY * mGridSizeY / 2.f - (objectSlot.mStartY + objectSlot.mSpanY / 2.f)
                    * mGridSizeY;
            transparas.mTranslate.set(offsetX, mMoveY, offsetZ);
            transparas.mScale.set(mScaleOfChild, mScaleOfChild, mScaleOfChild);
            return transparas;
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }
}
