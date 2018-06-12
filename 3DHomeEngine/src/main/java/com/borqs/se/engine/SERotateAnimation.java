package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERotate;


public class SERotateAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private AXIS mAxis;
    private boolean mIsLocal;

    public SERotateAnimation(SEScene scene, SEObject obj, AXIS axis, float from, float to, int times) {
        super(scene, from, to, times);
        mObj = obj;
        setAnimationTimes(times);
        mAxis = axis;
        mIsLocal = false;
    }

    public void setIsLocal(boolean isLocal) {
        mIsLocal = isLocal;
    }

    @Override
    public void onAnimationRun(float value) {
        switch (mAxis) {
        case X:
            if (mIsLocal) {
                mObj.setLocalRotate(new SERotate(value, 1, 0, 0));
            } else {
                mObj.setUserRotate(new SERotate(value, 1, 0, 0));
            }
            break;
        case Y:
            if (mIsLocal) {
                mObj.setLocalRotate(new SERotate(value, 0, 1, 0));
            } else {
                mObj.setUserRotate(new SERotate(value, 0, 1, 0));
            }
            break;
        case Z:
            if (mIsLocal) {
                mObj.setLocalRotate(new SERotate(value, 0, 0, 1));
            } else {
                mObj.setUserRotate(new SERotate(value, 0, 0, 1));
            }
            break;
        }
    }
}
