package com.borqs.borqsweather.weather;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.borqsweather.weather.yahoo.WeatherInfo;

public class WeatherState extends State {
    private static final String TAG = "Weather_WeatherState";
    private Bundle mBundle;
    private int mTryCount;
    private final static int MAX_TRY_COUNT = 5;
    private int mResultCode;
    private boolean mHasStoped = false;

    public WeatherState(WeatherController controller) {
        super(controller);
    }

    @Override
    public void run(Bundle data) {
        mBundle = data;
        String woeid = mBundle.getString(State.BUNDLE_WOEID);
        String country = mBundle.getString(State.BUNDLE_COUNTRY);
        String province = mBundle.getString(State.BUNDLE_PROVINCE);
        String city = mBundle.getString(State.BUNDLE_CITY);
        String latitude = mBundle.getString(State.BUNDLE_LATITUDE);
        String longitude = mBundle.getString(State.BUNDLE_LONGITUDE);
        WeatherInfo weatherInfo;
        Utils.printToLogFile("开始申请雅虎天气，参数："+ mBundle.toString());
        do {
            if (mHasStoped) {
                return;
            }
            mTryCount++;
            if (WeatherController.DEBUG)
                Log.d(TAG, "####try to get weather by yahoo server########mTryCount = " + mTryCount);
            weatherInfo = mDataModel.getWeatherData(mBundle, woeid);
        } while ((weatherInfo == null || weatherInfo.isNoData()) && mTryCount < MAX_TRY_COUNT);

        if (weatherInfo != null && !weatherInfo.isNoData()) {
            boolean isRight = true;
            Utils.printToLogFile("申请雅虎天气成功："+ weatherInfo.toString());
            if (mController.isAutoLocate()) {
                isRight = checkDate(weatherInfo.getFormatDate());
            }
            if (isRight) {
                checkTemp(weatherInfo);
                if (!TextUtils.isEmpty(city)) {
                    weatherInfo.setCity(city);
                }
                mPreferences.setWoeid(woeid);
                mPreferences.setLocationCountryName(country);
                mPreferences.setLocationProvinceName(province);
                mPreferences.setWoeidUpdateTime(System.currentTimeMillis());
                mPreferences.setLocationLatitude(latitude);
                mPreferences.setLocationLongitude(longitude);
                if (!TextUtils.isEmpty(city)) {
                    mPreferences.setLocationCityName(city);
                } else {
                    mPreferences.setLocationCityName(province);
                }
                mPreferences.setWeatherInfo(weatherInfo);
                onSuccess(null);
            } else {
                if (mHasStoped) {
                    return;
                }
                Utils.printToLogFile("天气时间错误以申请失败处理");
                if (WeatherController.DEBUG)
                    Log.d(TAG, "#### get weather failed due to system time is wrong ###### ");
                onFailed(null);
            }
        } else {
            if (mHasStoped) {
                return;
            }
            Utils.printToLogFile("申请雅虎天气失败");
            if (WeatherController.DEBUG)
                Log.d(TAG, "#### get weather failed from server after try " + mTryCount + " times");
            mResultCode = WeatherController.RESULT_ERROR_GET_WEATHER;
            onFailed(null);
        }

    }

    private boolean checkDate(Date date) {
        if (date == null) {
            mResultCode = WeatherController.RESULT_ERROR_GET_WEATHER;
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        long time = cal.getTimeInMillis();

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        long nowTime = now.getTimeInMillis();

        /*
         * Fix issue: can't update time for a peroid of time after
         * "New year day", "1st day of new month or 12:00AM".*Don't check year,
         * month and day. Check time difference only. Set 48 hours to adapt with
         * China Weather Network.
         */
        if (!Utils.isInvalidWeather(nowTime, time)) {
            mResultCode = WeatherController.RESULT_ERROR_WEATHER_INFO;
            return false;
        }
        return true;
    }

    private void checkTemp(WeatherInfo info) {
        try {
            int temp = Integer.parseInt(info.getTemperature(WeatherInfo.TEMPERATURE_FMT_CELSIUS));
            int tempH = Integer.parseInt(info.getTempHigh(WeatherInfo.TEMPERATURE_FMT_CELSIUS));
            int tempL = Integer.parseInt(info.getTempLow(WeatherInfo.TEMPERATURE_FMT_CELSIUS));
            if (temp < tempL) {
                temp = tempL;
            } else if (temp > tempH) {
                temp = tempH;
            }
            info.setTemperature(String.valueOf(temp));
        } catch (NumberFormatException e) {
            Log.e(TAG, "WeaherState checkTemp NumberformatException");
        }
    }

    @Override
    void stop() {
        mHasStoped = true;
        if (WeatherController.DEBUG)
            Log.d(TAG, "#### stop get weather###### ");
    }

    @Override
    void onSuccess(Bundle data) {
        if (mHasStoped) {
            return;
        }
        if (WeatherController.DEBUG)
            Log.d(TAG, "#### get weather successfully###### ");
        mHandler.sendEmptyMessage(WeatherController.EVENT_REQUEST_WEATHER_SUCCESS);

    }

    @Override
    void onFailed(Bundle data) {
        Message msg = Message.obtain(mHandler, WeatherController.EVENT_REQUEST_FAILE, mResultCode);
        mHandler.sendMessage(msg);

    }
}
