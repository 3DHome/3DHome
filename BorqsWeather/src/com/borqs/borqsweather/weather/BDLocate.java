package com.borqs.borqsweather.weather;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.borqs.borqsweather.WeatherApplication;
import com.borqs.borqsweather.weather.LocationState.Locate;

import java.util.Timer;
import java.util.TimerTask;

public class BDLocate implements Locate {
    private static final String TAG = "Weather_LocationState";
    private static boolean DEBUG = WeatherController.DEBUG;
    private static final String BUNDLE_PRIORITY = "priority";
    private int mRequestPriority = LocationClientOption.NetWorkFirst;
    private LocationState mState;
    private LocationClient mBDLocationClient;
    private BDLocationListener mBDLocationListener;
//    private LocationClientOption mBDLocationClientOption;
    private String mCountry = "China";
    private String mProvince;
    private String mCity;
    private String mLatitude;
    private String mLongitude;
    private Timer mTimer;
    private Bundle mData;
    private boolean mHasGotLocation = false;

    public BDLocate(LocationState state) {
        mState = state;
    }

    public void requestLocationAndCity(Bundle data) {
        mData = data;
        if (mData!= null) {
           int preRequestPriority = data.getInt(BUNDLE_PRIORITY, LocationClientOption.GpsFirst);
            if (preRequestPriority == LocationClientOption.NetWorkFirst) {
                mRequestPriority = LocationClientOption.GpsFirst;
            } else {
                mRequestPriority = LocationClientOption.NetWorkFirst;
            }
        }
        reqCityFromBaiduServer();
    }

    private void reqCityFromBaiduServer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new CancelLocateTask(), 1000 * 60);
        mBDLocationClient = WeatherApplication.getInstance().getBDLocationClient();
        mBDLocationListener = new BDLocationListener() {

            public void onReceivePoi(BDLocation arg0) {

            }

            public void onReceiveLocation(BDLocation location) {
                if (location == null) {
                    return;
                }
                mCity = location.getCity();
                if (TextUtils.isEmpty(mCity)) {
                    if (DEBUG)
                        Log.d(TAG, "###  get location failed due to city is null### mRequestPriority = "
                                + mRequestPriority);
                } else {
                    if (mData == null) {
                        mData = new Bundle();
                    }
                    mHasGotLocation = true;
                    mProvince = location.getProvince();
                    mLatitude = String.valueOf(location.getLatitude());
                    mLongitude = String.valueOf(location.getLongitude());
                    mData.putString(State.BUNDLE_COUNTRY, mCountry);
                    mData.putString(State.BUNDLE_PROVINCE, mProvince);
                    mData.putString(State.BUNDLE_CITY, mCity);
                    mData.putString(State.BUNDLE_LATITUDE, mLatitude);
                    mData.putString(State.BUNDLE_LONGITUDE, mLongitude);
                    mData.putInt(BUNDLE_PRIORITY, mRequestPriority);
                }
                mState.onReceiveCity(true, getBundle());
                stop();
            }
        };
        mBDLocationClient.registerLocationListener(mBDLocationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setAddrType("all");
        locationClientOption.setTimeOut(15);
        locationClientOption.setProdName("3DHome");
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setPriority(mRequestPriority);
        mBDLocationClient.setLocOption(locationClientOption);
        mBDLocationClient.start();
        mBDLocationClient.requestLocation();
    }

    private Bundle getBundle() {
        if (mData == null) {
            mData = new Bundle();
        }
        mData.putBoolean(State.BUNDLE_HAS_LOCATION, mHasGotLocation);
        mData.putInt(BUNDLE_PRIORITY, mRequestPriority);
        return mData;
    }

    private class CancelLocateTask extends TimerTask {
        @Override
        public void run() {
            if (DEBUG)
                Log.d(TAG, "###  get location failed due to timeout 60s ###mRequestPriority = " + mRequestPriority);
            stop();
            mState.onReceiveCity(true, getBundle());
        }
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mBDLocationClient != null && mBDLocationListener != null) {
            mBDLocationClient.unRegisterLocationListener(mBDLocationListener);
        }
        if (mBDLocationClient != null && mBDLocationClient.isStarted()) {
            mBDLocationClient.stop();
        }
    }

}
