package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.SEVector4f;


public class SEComponentAttribute {
    public String mComponentName;
    public String mRegularName;
    public String mLightNames;
    public int mStatusValue;
    public int mSpatialDataValue = 15;
    public float mAlphaValue;
    public SEVector4f mEffectData;
    // 镜像的清晰度0～1，值越大越清晰
    public float mMirrorColorDesity = 0.5f;
    // shadow color should be from 0.001(black) to 0.1(white)
    public float mShadowColorDesity = 0.04f;

    public float[] getEffectData() {
        return mEffectData.mD;
    }

    public float getAlphaValue() {
        return mAlphaValue;
    }

    public int getStatusValue() {
        return mStatusValue;
    }

    public String getLightNames() {
        return mLightNames;
    }

    public float getMirrorColorDesity() {
        return mMirrorColorDesity;
    }

    public float getShadowColorDesity() {
        return mShadowColorDesity;
    }

    public int getSpatialDataValue() {
        return mSpatialDataValue;
    }

}
