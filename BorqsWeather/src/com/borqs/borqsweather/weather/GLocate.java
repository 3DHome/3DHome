package com.borqs.borqsweather.weather;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.borqs.borqsweather.weather.LocationState.Locate;

public class GLocate implements Locate {

    private static final String TAG = "Weather_LocationState";
    private static boolean DEBUG = WeatherController.DEBUG;
    private LocationState mState;
    private String mLatitude;
    private String mLongitude;
    private Timer mTimer;
    private LocationManager mLocationManager = null;
    private boolean mHasGotLocation = false;
    private Bundle mData;

    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mHasGotLocation = true;
            mLocationManager.removeUpdates(mLocationListener);
            mLatitude = String.valueOf(location.getLatitude());
            mLongitude = String.valueOf(location.getLongitude());
            if (DEBUG)
                Log.d(TAG, "###  request location success###");
            mState.onReceiveCity(false, getBundle());
        }

        public void onProviderDisabled(String provider) {
            if (DEBUG)
                Log.i(TAG, "#########GPS onProviderDisabled####");
        }

        public void onProviderEnabled(String provider) {
            if (DEBUG)
                Log.i(TAG, "#########GPS onProviderEnabled####");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (DEBUG)
                Log.i(TAG, "#########GPS onStatusChanged####");
        }
    };

    public GLocate(LocationState state) {
        mState = state;
        mLocationManager = (LocationManager) mState.mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private void requestCurrentLocation() {
        if (!Utils.isLocationServicesEnabled(mState.mContext)) {
            if (DEBUG)
                Log.d(TAG, "###  get location failed due to  GPS closed ###");
            mState.onReceiveCity(false, getBundle());
            return;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new CancelLocateTask(), 1000 * 60);
        if (isLocationProviderEnabled()) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
    }

    private class CancelLocateTask extends TimerTask {
        @Override
        public void run() {
            if (DEBUG)
                Log.d(TAG, "###  get location due to timeout 60s ###");
            stop();
            mState.onReceiveCity(false, getBundle());
        }
    }

    private boolean isLocationProviderEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private Bundle getBundle() {
        if (mData == null) {
            mData = new Bundle();
        }
        mData.putBoolean(State.BUNDLE_HAS_LOCATION, mHasGotLocation);
        mData.putString(State.BUNDLE_LATITUDE, mLatitude);
        mData.putString(State.BUNDLE_LONGITUDE, mLongitude);
        return mData;
    }

    public void requestLocationAndCity(Bundle data) {
        if (DEBUG)
            Log.d(TAG, "###  begin request location ###");
        mData = data;
        requestCurrentLocation();
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
