package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SEVector3f;

public class SEMoveAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private SETransParas mSrcTransParas;
    private SETransParas mDesTransParas;
    private AXIS mAxis;

    public SEMoveAnimation(SEScene scene, SEObject obj, AXIS axis, SETransParas from, SETransParas to, int times) {
        super(scene, 0, 1, times);
        mObj = obj;
        mSrcTransParas = from;
        mDesTransParas = to;
        mAxis = axis;
    }
    
    public void adjustAngle() {
        float srcAngle = mSrcTransParas.mRotate.getAngle();
        float desAngle = mDesTransParas.mRotate.getAngle();
        if (desAngle - srcAngle > 180) {
            desAngle = desAngle - 360;
        } else if (desAngle - srcAngle < -180) {
            desAngle = 360 + desAngle;
        }
        mSrcTransParas.mRotate.set(srcAngle, 0, 0, 1);
        mDesTransParas.mRotate.set(desAngle, 0, 0, 1);
    }

    @Override
    public void onAnimationRun(float value) {
        SEVector3f distance = mDesTransParas.mTranslate.subtract(mSrcTransParas.mTranslate);
        mObj.getUserTransParas().mTranslate = mSrcTransParas.mTranslate.add(distance.mul(value));

        SEVector3f distanceScale = mDesTransParas.mScale.subtract(mSrcTransParas.mScale);
        mObj.getUserTransParas().mScale = mSrcTransParas.mScale.add(distanceScale.mul(value));

        float desAngle = mDesTransParas.mRotate.mD[0];
        float srcAngle = mSrcTransParas.mRotate.mD[0];
        float distanceAngle = desAngle - srcAngle;
        float angle = srcAngle + distanceAngle * value;
        switch (mAxis) {
        case X:
            mObj.getUserTransParas().mRotate.set(angle, 1, 0, 0);
            break;
        case Y:
            mObj.getUserTransParas().mRotate.set(angle, 0, 1, 0);
            break;
        case Z:
            mObj.getUserTransParas().mRotate.set(angle, 0, 0, 1);
            break;
        }
        mObj.setUserTransParas();
    }

}
