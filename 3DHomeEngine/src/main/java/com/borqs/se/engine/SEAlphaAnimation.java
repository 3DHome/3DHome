package com.borqs.se.engine;


public class SEAlphaAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private boolean mIsBlending;

    public SEAlphaAnimation(SEScene scene, SEObject obj, float from, float to, int times) {
        super(scene, from, to, times);
        mObj = obj;
    }

    @Override
    public void onFinish() {
        if (!mIsBlending) {
            mObj.setBlendingable(false);
        }
    }

    @Override
    public void onBegin() {
        mIsBlending = mObj.isBlendingable();
        if (!mIsBlending) {
            mObj.setBlendingable(true);
        }
    }

    public void onAnimationRun(float value) {
        mObj.setAlpha(value);
    }

}
