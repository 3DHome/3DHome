package com.borqs.se.widget3d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
//import android.util.Log;

public class ShakeDetector implements SensorEventListener {
    static final int UPDATE_INTERVAL = 100;

    long mLastUpdateTime;
    float mLastX, mLastY, mLastZ;
    Context mContext;
    SensorManager mSensorManager;

    OnShakeListener mListener;

    public int shakeThreshold = 2000;

    public ShakeDetector(Context context, OnShakeListener listener) {
        mContext = context;
        mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mListener = listener;
    }

    public interface OnShakeListener {
        void onShake(float shakeLevel);
    }

    public void start() {
        try {
            if (mSensorManager == null) {
                throw new UnsupportedOperationException();
            }

            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (sensor == null) {
                throw new UnsupportedOperationException();
            }

            boolean success = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            if (!success) {
               throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
        }
    }

    public void stop() {
        try {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - mLastUpdateTime; 
        if (diffTime < UPDATE_INTERVAL)
            return;

        mLastUpdateTime = currentTime;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        float deltaZ = z - mLastZ;
        mLastX = x;
        mLastY = y;
        mLastZ = z;
        float delta = FloatMath.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) 
                / diffTime * 10000;
        if (delta > shakeThreshold) {
            //Log.i("test", "delta is "+ delta);
            if (delta > shakeThreshold && delta <= shakeThreshold * 2)
                mListener.onShake(1);
            else if (delta > shakeThreshold * 2 && delta <= shakeThreshold * 3)
                mListener.onShake(1.3f);
            else if (delta > shakeThreshold * 3)
                mListener.onShake(1.6f);
        }
    }
}
