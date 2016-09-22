package com.borqs.borqsweather.weather;

import java.util.Calendar;

import com.borqs.borqsweather.weather.yahoo.YahooLocationHelper.CityEntity;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class WoeidAndCityState extends State {
    private static final String TAG = "Weather_WoeidState";
    private Bundle mData;
    private int mTryCount;
    private final static int MAX_TRY_COUNT = 3;
    private boolean mHasStoped = false;

    public WoeidAndCityState(WeatherController controller) {
        super(controller);
        mTryCount = 0;
    }

    @Override
    public void run(Bundle data) {
        Utils.printToLogFile("开始获取WOEID位置信息");
        mData =  data;
        if (WeatherController.DEBUG)
            Log.d(TAG, "Start to get WOEID and city " + mData);
        CityEntity cityEntity = getWoeid(mData);       
        if (cityEntity != null) {
            mData.putString(State.BUNDLE_WOEID, cityEntity.mWoeid);
            if (TextUtils.isEmpty(mData.getString(State.BUNDLE_CITY))) {
                mData.putString(State.BUNDLE_CITY, cityEntity.mCity);
            }
            onSuccess(mData);
        } else {
            onFailed(null);
        }

    }

    private CityEntity getWoeid(Bundle bundle) {
        CityEntity cityEntity = null;
        String currCity = bundle.getString(State.BUNDLE_CITY);
        String latitude = bundle.getString(State.BUNDLE_LATITUDE);
        String longitude = bundle.getString(State.BUNDLE_LONGITUDE);
/*        String saveWoeid = mPreferences.getWoeid();
        String savedCity = mPreferences.getLocationCityName();
        long savetime = mPreferences.getWoeidUpdateTime();

        if (!TextUtils.isEmpty(savedCity)) {
            savedCity = savedCity.toLowerCase().trim().replaceAll(" ", "_");
            if (savedCity.equals(currCity) && !isWoeidOutOfDate(savetime)) {
                if (!TextUtils.isEmpty(saveWoeid)) {
                    cityEntity = new CityEntity();
                    cityEntity.mCity = savedCity;
                    cityEntity.mWoeid = saveWoeid;
                    return cityEntity;
                }
            }
        }*/
        do {
            if (mHasStoped) {
                return null;
            }
            mTryCount++;
            if (WeatherController.DEBUG)
                Log.d(TAG, "####try to get woeid and city by yahoo server########mTryCount = " + mTryCount);
            cityEntity = mDataModel.getCityAndWoeidByLocation(new double[] { Double.parseDouble(latitude),
                    Double.parseDouble(longitude) });
        } while (cityEntity == null && mTryCount < MAX_TRY_COUNT);

        return cityEntity;
    }

    private boolean isWoeidOutOfDate(long time) {
        if (time < 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(now);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return !((nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
                && (nowCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH))
                && (nowCal.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)));
    }

    @Override
    void stop() {
        mHasStoped = true;
        if (WeatherController.DEBUG)
            Log.d(TAG, "#### stop get woeid ######");
    }

    @Override
    void onSuccess(Bundle data) {
        if (mHasStoped) {
            return;
        }
        Utils.printToLogFile("开始获取WOEID位置信息成功 ： " + data.toString());
        if (WeatherController.DEBUG)
            Log.d(TAG, "#### get woeid and city successfully######woeid = " + mData.getString(State.BUNDLE_WOEID)
                    + ", city = " + mData.getString(State.BUNDLE_CITY));
        Message msg = Message.obtain(mHandler, WeatherController.EVENT_REQUEST_WOEID_SUCCESS);
        msg.setData(mData);
        mHandler.sendMessage(msg);
    }

    @Override
    void onFailed(Bundle data) {
        if (mHasStoped) {
            return;
        }
        Utils.printToLogFile("获取WOEID位置信息失败");
        if (WeatherController.DEBUG)
            Log.i(TAG, "########## get woeid and city  failed after try " + mTryCount + " times");
        Message msg = Message.obtain(mHandler, WeatherController.EVENT_REQUEST_FAILE,
                WeatherController.RESULT_ERROR_GET_WEATHER);
        mHandler.sendMessage(msg);

    }

}
