package com.borqs.se.engine;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SEVector2i;
import com.borqs.se.engine.SEVector.SEVector3f;

public class SECamera {
    /******************************* native interface begin ************************/

    public native void getLocation_JNI(float[] location);

    public native void getAxisZ_JNI(float[] axisZ);

    public native void getAxisY_JNI(float[] axisY);

    public native void getAxisX_JNI(float[] axisX);

    private native void screenCoordinateToRay_JNI(int x, int y, float[] ray);

    private native void setCamera_JNI(float[] location, float[] axisZ, float[] up, float fov, float ratio, float near,
            float far);

    public native void setCamera_JNI(float[] location, float[] target, float fov, float ratio, float near, float far);

    public native void setFrustum_JNI(float fov, float ratio, float near, float far);

    private native void rotateLocal_JNI(float[] rotate);

    private native void rotateLocal_JNI(float angle, int axis);

    private native void translateLocal_JNI(float[] translate);

    private native void setLocation_JNI(float[] loc);

    private native void setViewport_JNI(int x, int y, int w, int h);

    public native void operateCamera_JNI(float[] location, float[] rotate, boolean transLocal);

    public native static String getBestPosition_JNI(float[] pos, float[] targetPos);

    public native SEObject getSelectedObject_JNI(int x, int y);

    private native float[] worldToScreenCoordinate_JNI(float[] location);

    public SEVector2i worldToScreenCoordinate(SEVector3f location) {
        float[] out = worldToScreenCoordinate_JNI(location.mD);
        SEVector2i screenCoordinate = new SEVector2i(Math.round(out[0]), Math.round(out[1]));
        return screenCoordinate;
    }

    /******************************* native interface end ************************/
    private SEScene mScene;
    private String mSceneName;

    private List<CameraChangedListener> mCameraChangedListeners;


    public interface CameraChangedListener {
        public void onCameraChanged();
    }

    public SECamera(SEScene scene) {
        mScene = scene;
        mSceneName = mScene.getSceneName();
        mCameraChangedListeners = new ArrayList<CameraChangedListener>();

    }

    public synchronized void addCameraChangedListener(CameraChangedListener l) {
        if (!mCameraChangedListeners.contains(l)) {
            mCameraChangedListeners.add(l);
            l.onCameraChanged();
        }
    }

    public synchronized void removeCameraChangedListener(CameraChangedListener l) {
        if (mCameraChangedListeners.contains(l)) {
            mCameraChangedListeners.remove(l);
        }
    }

    public synchronized void clearCameraChangedListener() {
        mCameraChangedListeners.clear();
    }

    protected void setSceneName(String sceneName) {
        mSceneName = sceneName;
    }

    public int getWidth() {
        return getCurrentData().mWidth;
    }

    public int getHeight() {
        return getCurrentData().mHeight;
    }

    public float getFov() {
        return getCurrentData().mFov;
    }

    public SEVector3f getLocation() {
        return getCurrentData().mLocation;
    }

    public SEVector3f getAxisZ() {
        return getCurrentData().mAxisZ.normalize();
    }

    public SEVector3f getAxisX() {
        return getCurrentData().mUp.cross(getAxisZ()).normalize();
    }

    public SEVector3f getAxisY() {
        return getCurrentData().mAxisZ.cross(getAxisX()).normalize();
    }

    public SEVector3f getUp() {
        return getCurrentData().mUp;
    }

    public float getNear() {
        return getCurrentData().mNear;
    }

    public float getFar() {
        return getCurrentData().mFar;
    }

    public float getRatio() {
        return ((float) getHeight()) / getWidth();
    }

    public void save() {
        getCurrentData().save();
    }

    public SECameraData restore() {
        return getCurrentData().restore();
    }

    public synchronized void notifySurfaceChanged(int width, int height) {
        getCurrentData().update(width, height);
        setCamera();
        setViewport_JNI(0, 0, width, height);
        for (CameraChangedListener l : mCameraChangedListeners) {
            l.onCameraChanged();
        }
    }

    public SECameraData getCurrentData() {
        return mScene.getCameraData();
    }

    public void setRadiusAndFov(float radius, float fov) {
        getCurrentData().mFov = fov;
        getCurrentData().mLocation.mD[1] = -radius;
        setCamera();
    }

    public float getRadius() {
        return Math.abs(getLocation().getY());
    }

    public SEVector3f getScreenLocation(float scale) {
        float paras = (float) (getWidth() / (2 * Math.tan(getFov() * Math.PI / 360)));
        SEVector3f loc = getLocation().add(getScreenOrientation().mul(paras * scale));
        return loc;
    }

    public SEVector3f getScreenOrientation() {
        return getAxisZ().mul(-1);
    }

    public SERay screenCoordinateToRay(int x, int y) {
        float[] data = new float[6];
        screenCoordinateToRay_JNI(x, y, data);
        return new SERay(data);
    }

    public void setCamera() {
        for (CameraChangedListener l : mCameraChangedListeners) {
            l.onCameraChanged();
        }
        setCamera_JNI(getLocation().mD, getAxisZ().mD, getUp().mD, getFov(), getRatio(), getNear(), getFar());
    }

    private float getLocationDistance(SEVector3f from, SEVector3f to) {
        return to.subtract(from).getLength();
    }

    public void printCameraStatus() {
        Log.d("BORQS###############", "location:" + getLocation().toString());
        Log.d("BORQS###############", "axisZ:" + getAxisZ().toString());
        Log.d("BORQS###############", "up:" + getUp());
        Log.d("BORQS###############", "fov:" + getFov());
        Log.d("BORQS###############", "height:" + getHeight());
        Log.d("BORQS###############", "width:" + getWidth());
        Log.d("BORQS###############", "near:" + getNear());
        Log.d("BORQS###############", "far:" + getFar());
    }

}
