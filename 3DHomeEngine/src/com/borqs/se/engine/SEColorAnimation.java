package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.SEVector3f;


public class SEColorAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private SEVector3f mFrom;
    private SEVector3f mTo;

    public SEColorAnimation(SEScene scene, SEObject obj, SEVector3f from, SEVector3f to, int times) {
        super(scene, 0, 1, times);
        mObj = obj;
        mFrom = from;
        mTo = to;
    }

    @Override
    public void onAnimationRun(float value) {
        SEVector3f distance = mTo.subtract(mFrom);
        SEVector3f color = mFrom.add(distance.mul(value));
        mObj.setUseUserColor(color.getX(), color.getY(), color.getZ());
    }

    @Override
    public void onFinish() {
        mObj.clearUserColor();
    }

}
