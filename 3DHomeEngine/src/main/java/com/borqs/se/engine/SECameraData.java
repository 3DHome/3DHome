package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.SEVector3f;

public class SECameraData {
    public SEVector3f mLocation;
    public SEVector3f mAxisZ;
    public SEVector3f mUp;
    public float mFov;
    public float mNear;
    public float mFar;
    public int mWidth;
    public int mHeight;
    private SECameraData mDataStack;

    public SECameraData() {
        mNear = 1;
        mFar = 2000;
        mFov = 60;
    }

    public void save() {
        mDataStack = clone();
    }

    public SECameraData restore() {
        return mDataStack;
    }

    public void update(int width, int height) {
        mHeight = height;
        mWidth = width;
        if (mDataStack != null) {
            mDataStack.update(width, height);
        }
    }

    public SECameraData clone() {
        SECameraData cameraData = new SECameraData();
        cameraData.mLocation = mLocation.clone();
        cameraData.mAxisZ = mAxisZ.clone();
        cameraData.mUp = mUp.clone();
        cameraData.mFov = mFov;
        cameraData.mNear = mNear;
        cameraData.mFar = mFar;
        cameraData.mWidth = mWidth;
        cameraData.mHeight = mHeight;
        return cameraData;
    }

    public void set(SECameraData data) {
        mLocation = data.mLocation.clone();
        mAxisZ = data.mAxisZ.clone();
        mUp = data.mUp.clone();
        mFov = data.mFov;
        mNear = data.mNear;
        mFar = data.mFar;
        mWidth = data.mWidth;
        mHeight = data.mHeight;
    }

    public void init(float[] data) {
        mLocation = new SEVector3f(data[0], data[1], data[2]);
        mAxisZ = new SEVector3f(data[3], data[4], data[5]);
        mUp = new SEVector3f(data[6], data[7], data[8]);
        mFov = data[9];
        mNear = data[10];
        mFar = data[11];
    }

    public void init(String[] data) {
        mLocation = new SEVector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
        mAxisZ = new SEVector3f(Float.parseFloat(data[3]), Float.parseFloat(data[4]), Float.parseFloat(data[5]));
        mUp = new SEVector3f(Float.parseFloat(data[6]), Float.parseFloat(data[7]), Float.parseFloat(data[8]));
        mFov = Float.parseFloat(data[9]);
        mNear = Float.parseFloat(data[10]);
        mFar = Float.parseFloat(data[11]);
    }

    public float[] getFloatArray() {
        float[] array = new float[12];
        array[0] = mLocation.mD[0];
        array[1] = mLocation.mD[1];
        array[2] = mLocation.mD[2];
        array[3] = mAxisZ.mD[0];
        array[4] = mAxisZ.mD[1];
        array[5] = mAxisZ.mD[2];
        array[6] = mUp.mD[0];
        array[7] = mUp.mD[1];
        array[8] = mUp.mD[2];
        array[9] = mFov;
        array[10] = mNear;
        array[11] = mFar;
        return array;
    }

    public String toString() {
        return "Location = " + mLocation + " AxisZ = " + mAxisZ + " Up = " + mUp + " Fov = " + mFov;
    }

    public float[] getLocation() {
        return mLocation.mD;
    }

    public float[] getAxisZ() {
        return mAxisZ.mD;
    }

    public float[] getUp() {
        return mUp.mD;
    }

}
