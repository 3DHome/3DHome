package com.borqs.borqsweather.weather;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.borqs.borqsweather.weather.yahoo.WeatherDataModel;
import com.borqs.borqsweather.weather.yahoo.WeatherPreferences;

public abstract class State {
    public static final String INTENT_ACTION_RETRY = "3dhome.action.intent.RETRY";
    
    public static final String BUNDLE_COUNTRY = "country";
    public static final String BUNDLE_PROVINCE = "province";
    public static final String BUNDLE_CITY = "city";
    public static final String BUNDLE_WOEID = "woeid";
    public static final String BUNDLE_LATITUDE = "latitude";
    public static final String BUNDLE_LONGITUDE = "longitude";
    public static final String BUNDLE_HAS_LOCATION = "has_location";
    
    protected WeatherController mController;
    protected Context mContext;
    protected Handler mHandler;
    protected WeatherPreferences mPreferences;
    protected WeatherDataModel mDataModel;
    protected int mMaxRequestCount;

    State(WeatherController controller) {
        mController = controller;
        mContext = controller.getContext();
        mHandler = controller.getEventHandler();
        mPreferences = WeatherPreferences.getInstance(mContext);
        mDataModel = WeatherDataModel.getInstance();
    }

    abstract void run(Bundle data);

    abstract void stop();

    abstract void onSuccess(Bundle data);

    abstract void onFailed(Bundle data);

    void processTask(Runnable runnable, long delay) {
        mHandler.postDelayed(runnable, delay);
    }

}
