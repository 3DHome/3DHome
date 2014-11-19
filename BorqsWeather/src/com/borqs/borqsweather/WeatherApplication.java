package com.borqs.borqsweather;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.LocationClient;
import com.borqs.borqsweather.weather.WeatherSettings;
import com.borqs.borqsweather.weather.yahoo.WeatherPreferences;

public class WeatherApplication{
    private static WeatherApplication mWeatherApplication;
    private LocationClient mBDLocationClient;
    private WeatherPreferences mPreferences;
    private Context mContext;
    

    public static WeatherApplication getInstance() {
        if(mWeatherApplication == null){
            mWeatherApplication = new WeatherApplication();
        }
        
        return mWeatherApplication;
    }

    public void init(Context context) {
        mContext = context;
        mBDLocationClient = new LocationClient(mContext);
        mPreferences = WeatherPreferences.getInstance(mContext);
        String woeid = mPreferences.getWoeid();
        if (TextUtils.isEmpty(woeid)) {
            WeatherSettings.setAutoLocation(mContext, true);
        }
    }

    public LocationClient getBDLocationClient() {
        if (mBDLocationClient == null) {
            mBDLocationClient = new LocationClient(mContext);
        }
        return mBDLocationClient;
    }
}
