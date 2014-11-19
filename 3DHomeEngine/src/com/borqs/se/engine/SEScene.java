package com.borqs.se.engine;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public abstract class SEScene {
    public enum SCENE_CHANGED_TYPE {
        NEW_CONFIG, NEW_SCENE
    };

    public final static int LIGHT_TYPE_POINT_LIGHT = 0;
    public final static int LIGHT_TYPE_DIRECT_LIGHT = 1;
    public final static int LIGHT_TYPE_SPOT_LIGHT = 2;

    public interface SESensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy);

        public void onSensorChanged(SensorEvent event);
    }

    public native void create_JNI();

    private native void release_JNI();

    public native void setIsTranslucent_JNI(boolean translucent);

    // It will be drawed at first, if a translucent scene

    public native void setNeedDraw_JNI(boolean needDraw);

    public native void setVisibility_JNI(boolean visibility);

    private native void setRoot_JNI(String rootName, int index);

    /**
     * @param object
     *            object's pointer
     */
    public native void setRoot_JNI(int object);

    public void setRoot(SEObject object) {
        setRoot_JNI(object.mName, object.mIndex);
    }

    public native static int loadResource_JNI(String scenePath, String dataPath);
    
    public native static void deleteResource_JNI(int resource);
    
    public native void inflateResource_JNI(int resource, String Objname, int ObjIndex, String nodeName, int nodeIndex, boolean removeTopNode);

    public native void setShadowMapCamera_JNI(float[] location, float[] axisZ, float[] up, float fov, float ratio,
            float near, float far);

    /**
     * @param type
     *            is 0,1 or 2, 2 is spot light, 0 is point light, 1 is direction
     *            light.
     * @param pos
     *            point light has position(world coordinate) only,pos[3] is
     *            attenuation parameter,from 0 to 1.0,0 means no attenuation.
     *            (no need set param dir,just set it to (0,0,0))
     * @param dir
     *            direction light has direction(world coordinate) only. (no need
     *            set param pos,just set it to (0,0,0)) if the light direction
     *            is from (0,0,1) to (0,0,0),the dir should be (0,0,-1);
     * @param spotdata
     *            spotdata is a vec4 struct, x is cutoff value from 0.1 to 0.9,
     *            0.1 means a little spot light angle,0.9 means a larger, y is
     *            exp value(a attenuation) min-exp = 0.0, no attenuation. z,w is
     *            not use now.
     */
    private native void addLightToScene_JNI(String lightName, float[] pos, float[] dir, float[] spotdata, int type);

    public native void removeLightFromScene_JNI(String lightName);

    public void addPointLightToScene(String lightName, float[] pos, float para) {
        addLightToScene_JNI(lightName, new float[] { pos[0], pos[1], pos[2], para }, new float[] { 0, 0, 0 },
                new float[] { 0, 0, 0, 0 }, 0);
    }

    public void addDirectLightToScene(String lightName, float[] dir) {
        addLightToScene_JNI(lightName, new float[] { 0, 0, 0 }, dir, new float[] { 0, 0, 0, 0 }, 1);
    }

    public void addSpotLightToScene(String lightName, float[] dir, float[] pos, float[] vec4spotdata) {
        addLightToScene_JNI(lightName, pos, dir, vec4spotdata, 2);
    }

    /**
     * @param lowestEvnBrightness
     *            is lowest environment brightness that there is no light reach
     *            0.0 is black, 1.0 is texture color;
     */
    public native void setLowestBrightness_JNI(float lowestEnvBrightness);

    /* SceneManagerType */
    public native void setSceneType_JNI(int type);

    private String mSceneName;
    private SECamera mSECamera;
    private int mScreenW;
    private int mScreenH;
    private boolean mIsScreenLarge;
    private float mScreenDensity;
    public List<SESensorEventListener> mSESensorEventListeners;
    private SEObject mContectObject;
    private Context mContext;
    private SEEventQueue mEventQueue;
    private SEObject mDownHitObject;
    private SEObject mMenuObject;
    /**
     * The delegate to handle touch events that are physically in this view but
     * should be handled by another object.
     */
    private SEObject mTouchDelegate = null;
    private boolean mHadBeenStarted;
    private boolean mHasBeenReleased;    


    public SEScene(Context context, String name) {
        mSceneName = name;
        mSECamera = new SECamera(this);
        mSESensorEventListeners = new ArrayList<SESensorEventListener>();
        mContext = context;
        final int screenSize = mContext.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        mIsScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        mScreenDensity = mContext.getResources().getDisplayMetrics().density;
        mEventQueue = new SEEventQueue();
        mHasBeenReleased = true;
        mHadBeenStarted = false;

    }

    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }

    public float getScreenDensity() {
        return mScreenDensity;
    }

    public void removeMessage(int id) {
        mEventQueue.removeEvent(id);
    }

    public float getFontScale() {
        Configuration config = new Configuration();
        android.provider.Settings.System.getConfiguration(mContext.getContentResolver(), config);
        return config.fontScale;
    }

    public abstract void handleMessage(int type, Object message);

    public abstract SECameraData getCameraData();

    public void addSESensorEventListener(SESensorEventListener listener) {
        if (listener != null && !mSESensorEventListeners.contains(listener)) {
            mSESensorEventListeners.add(listener);
        }
    }

    public void removeSESensorEventListener(SESensorEventListener listener) {
        if (listener != null && !mSESensorEventListeners.contains(listener)) {
            mSESensorEventListeners.remove(listener);
        }
    }

    public void clearSESensorEventListener() {
        mSESensorEventListeners.clear();
    }

    public Context getContext() {
        return mContext;
    }

    public void startScene(int screenW, int screenH) {
        mHadBeenStarted = true;
        mScreenW = screenW;
        mScreenH = screenH;
        create_JNI();
        SESceneManager.setMainScene_JNI(mSceneName);
        notifySurfaceChanged(screenW, screenH);
        onSceneStart();
    }

    public void onSceneStart() {

    }

    public boolean hasBeenStarted() {
        return mHadBeenStarted;
    }

    public String getSceneName() {
        return mSceneName;
    }

    public void release() {
        onRelease();
        release_JNI();
        mHasBeenReleased = true;
    }
    
    public SEEventQueue getEventQuene() {
        return mEventQueue;
    }

    public SEObject getContentObject() {
        return mContectObject;
    }

    public SECamera getCamera() {
        return mSECamera;
    }

    public void setContentObject(SEObject contentObject) {
        mContectObject = contentObject;
        mContectObject.setCamera(mSECamera);
        mContectObject.setScene(this);
        setRoot(contentObject);
    }
    public void setObjectsMenu(SEObject menu) {
        mMenuObject = menu;
        mContectObject.addChild(mMenuObject, true);
    }
    
    public SEObject getObjectsMenu() {
        return mMenuObject;
    }

    public boolean update() {
        return mEventQueue.render();
    }

    public void notifySurfaceChanged(int width, int height) {
        mScreenW = width;
        mScreenH = height;
        if (mSECamera != null) {
            mSECamera.notifySurfaceChanged(width, height);
        }
        if (mContectObject != null) {
            mContectObject.notifySurfaceChanged(width, height);
        }
    }

    public int getSceneWidth() {
        return mScreenW;
    }

    public int getSceneHeight() {
        return mScreenH;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownHitObject = getCamera().getSelectedObject_JNI((int) event.getX(), (int) event.getY());
        }
        if (mTouchDelegate != null) {
            mTouchDelegate.dispatchTouchEvent(event);
            return true;
        } else {
            return false;
        }
    }

    public SEObject getDownHitObject() {
        return mDownHitObject;
    }

    public void setDownHitObject(SEObject obj) {
        mDownHitObject = obj;
    }

    public boolean handleBackKey(SEAnimFinishListener l) {
        boolean result = false;
        if (mContectObject != null) {
            result = mContectObject.handleBackKey(l);
        }
        return result;
    }

    public void handleMenuKey() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mContectObject != null) {
            mContectObject.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onActivityPause() {
        if (mContectObject != null) {
            mContectObject.onActivityPause();
        }
    }

    public void onActivityRestart() {
        if (mContectObject != null) {
            mContectObject.onActivityRestart();
        }
    }

    public void onActivityResume() {
        if (mContectObject != null) {
            mContectObject.onActivityResume();
        }
    }

    public void onNewIntent(Intent intent) {

    }

    public void onActivityDestory() {
        if (mContectObject != null) {
            mContectObject.onActivityDestory();
        }
    }

    public void onRelease() {
        mEventQueue.clear();
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public Dialog onCreateDialog(int id, Bundle bundle) {
        return null;
    }

    public void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {

    }

    /**
     * Sets the TouchDelegate for this Object.
     */
    public void setTouchDelegate(SEObject delegate) {
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
        dispatchTouchEvent(event);
        mTouchDelegate = delegate;
    }


    public void removeTouchDelegate() {
        mTouchDelegate = null;
    }

    /**
     * Gets the TouchDelegate for this View.
     */
    public SEObject getTouchDelegate() {
        return mTouchDelegate;
    }    

}
