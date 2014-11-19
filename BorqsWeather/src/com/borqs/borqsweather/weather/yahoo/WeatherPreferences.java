package com.borqs.borqsweather.weather.yahoo;

import com.borqs.borqsweather.weather.WeatherController;
import com.borqs.borqsweather.weather.WeatherSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

public class WeatherPreferences {
    /** TAG for debugging */
    private static final String TAG = "Weather_WeatherPreferences";
    /** Preferences name */
    private static final String WEATHER_PREFERENCE = "weather_preference";
    
    public static final String WEATHER_SETTING_PREFERENCE = WeatherSettings.WEATHER_SETTING_PREFERENCE;
    

    /** Key for last manual location city */
    private static final String KEY_LOCATION_CITY_NAME = "location";

    /** Key for last manual location country */
    private static final String KEY_LOCATION_COUNTRY_NAME = "country";

    /** Key for last manual location province */
    private static final String KEY_LOCATION_PROVINCE_NAME = "province";

    private static final String KEY_LOCATION_LATITUDE = "latitude";

    private static final String KEY_LOCATION_LONGITUDE = "longitude";
    
    private static final String KEY_WOEID = "woeid";
    
    private static final String KEY_WOEID_TIME = "woeid_time";
    
    /** Key for the weather info */
    private static final String KEY_WEATHER_CITY = "weather_city";
    private static final String KEY_WEATHER_TEMP = "weather_temp";
    private static final String KEY_WEATHER_TEMP_HIGH = "weather_tempH";
    private static final String KEY_WEATHER_TEMP_LOW = "weather_tempL";
    private static final String KEY_WEATHER_CONDITION = "weather_condition";
    private static final String KEY_WEATHER_DATE = "weather_date";
    private static final String KEY_WEATHER_CODE = "weather_code";
    private static final String KEY_WEATHER_SUNRISE_TIME = "weather_sunrise_time";
    private static final String KEY_WEATHER_SUNSET_TIME = "weather_sunset_time";
    private static final String KEY_WEATHER_WIND_DIRECTION = "weather_wind_direction";
    private static final String KEY_WEATHER_WIND_SPEED = "weather_wind_speed";
    private static final String KEY_WEAther_UPDATE_TIME = "update_time";

    /** five more days weather */
    private static final String KEY_WEATHER_CONDITION1 = "weather_condition1";
    private static final String KEY_WEATHER_CODE1 = "weather_code1";
    private static final String KEY_WEATHER_DATE1 = "weather_date1";
    private static final String KEY_WEATHER_DAY1 = "weather_day1";
    private static final String KEY_WEATHER_TEMP_HIGH1 = "weather_tempH1";
    private static final String KEY_WEATHER_TEMP_LOW1 = "weather_tempL1";

    private static final String KEY_WEATHER_CONDITION2 = "weather_condition2";
    private static final String KEY_WEATHER_CODE2 = "weather_code2";
    private static final String KEY_WEATHER_DATE2 = "weather_date2";
    private static final String KEY_WEATHER_DAY2 = "weather_day2";
    private static final String KEY_WEATHER_TEMP_HIGH2 = "weather_tempH2";
    private static final String KEY_WEATHER_TEMP_LOW2 = "weather_tempL2";

    private static final String KEY_WEATHER_CONDITION3 = "weather_condition3";
    private static final String KEY_WEATHER_CODE3 = "weather_code3";
    private static final String KEY_WEATHER_DATE3 = "weather_date3";
    private static final String KEY_WEATHER_DAY3 = "weather_day3";
    private static final String KEY_WEATHER_TEMP_HIGH3 = "weather_tempH3";
    private static final String KEY_WEATHER_TEMP_LOW3 = "weather_tempL3";

    private static final String KEY_WEATHER_CONDITION4 = "weather_condition4";
    private static final String KEY_WEATHER_CODE4 = "weather_code4";
    private static final String KEY_WEATHER_DATE4 = "weather_date4";
    private static final String KEY_WEATHER_DAY4 = "weather_day4";
    private static final String KEY_WEATHER_TEMP_HIGH4 = "weather_tempH4";
    private static final String KEY_WEATHER_TEMP_LOW4 = "weather_tempL4";

    /** Instance */
    private static WeatherPreferences m_Instance;

    /** Context application */
    private Context m_Context;

    private WeatherPreferences(Context context) {
        this.m_Context = context;
    }

    public synchronized static WeatherPreferences getInstance(Context context) {
        if (m_Instance == null) {
            m_Instance = new WeatherPreferences(context);
        }

        return m_Instance;
    }
    //////////////////////////////////weather setting begin#################
      public String getLocationCityName() {
          SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
          return spf.getString(KEY_LOCATION_CITY_NAME, "");
    }

    public boolean setLocationCityName(String cityName) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_CITY_NAME, cityName);
        return editor.commit();
    }

    public String getLocationCountryName() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return spf.getString(KEY_LOCATION_COUNTRY_NAME, "");
    }

    public boolean setLocationCountryName(String countryName) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_COUNTRY_NAME, countryName);
        return editor.commit();
    }

    public String getLocationProvinceName() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return spf.getString(KEY_LOCATION_PROVINCE_NAME, "");
    }

    public boolean setLocationProvinceName(String provinceName) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_PROVINCE_NAME, provinceName);
        return editor.commit();
    }

    public String getLocationLatitude() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return spf.getString(KEY_LOCATION_LATITUDE, "");
    }

    public boolean setLocationLatitude(String latitude) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_LATITUDE, latitude);
        return editor.commit();
    }

    public String getLocationLongitude() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return spf.getString(KEY_LOCATION_LONGITUDE, "");
    }

    public boolean setLocationLongitude(String longitude) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_LONGITUDE, longitude);
        return editor.commit();
    }


    public boolean setWoeid(String woeid) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_WOEID, woeid);
        return editor.commit();
    }

    public String getWoeid() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return spf.getString(KEY_WOEID, "");
    }

    public void setWoeidUpdateTime(long time) {
        SharedPreferences.Editor editor = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE,
                Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_WOEID_TIME, time);
        editor.commit();
    }

    public long getWoeidUpdateTime() {
        SharedPreferences spf = m_Context.getSharedPreferences(WEATHER_SETTING_PREFERENCE, Context.MODE_PRIVATE);
        long time = spf.getLong(KEY_WOEID_TIME, -1);
        return time;
    }

    //////////////////////////////////weather setting end#################
    
    public void setWeatherInfo(WeatherInfo info) {
        if (info == null) {
            return;
        }
        _setStringPreferences(KEY_WEATHER_CITY, info.getCity());
        _setStringPreferences(KEY_WEATHER_CONDITION, info.getCondition());
        _setStringPreferences(KEY_WEATHER_DATE, info.getDate());
        _setStringPreferences(KEY_WEATHER_TEMP, info.getTemperature(0));
        _setStringPreferences(KEY_WEATHER_TEMP_HIGH, info.getTempHigh(0));
        _setStringPreferences(KEY_WEATHER_TEMP_LOW, info.getTempLow(0));
        _setStringPreferences(KEY_WEATHER_CODE, info.getCode());
        _setStringPreferences(KEY_WEATHER_SUNRISE_TIME, info.getSunriseTime());
        _setStringPreferences(KEY_WEATHER_SUNSET_TIME, info.getSunsetTime());
        _setStringPreferences(KEY_WEATHER_WIND_DIRECTION, info.getWindDirection());
        _setStringPreferences(KEY_WEATHER_WIND_SPEED, info.getWindSpeed());

        if (info.getForecastWeather1() != null) {
            _setStringPreferences(KEY_WEATHER_CONDITION1, info.getForecastWeather1().getCondition());
            _setStringPreferences(KEY_WEATHER_CODE1, info.getForecastWeather1().getCode());
            _setStringPreferences(KEY_WEATHER_DATE1, info.getForecastWeather1().getDate());
            _setStringPreferences(KEY_WEATHER_DAY1, info.getForecastWeather1().getDay());
            _setStringPreferences(KEY_WEATHER_TEMP_HIGH1, info.getForecastWeather1().getTempHigh());
            _setStringPreferences(KEY_WEATHER_TEMP_LOW1, info.getForecastWeather1().getTempLow());
        }

        if (info.getForecastWeather2() != null) {
            _setStringPreferences(KEY_WEATHER_CONDITION2, info.getForecastWeather2().getCondition());
            _setStringPreferences(KEY_WEATHER_CODE2, info.getForecastWeather2().getCode());
            _setStringPreferences(KEY_WEATHER_DATE2, info.getForecastWeather2().getDate());
            _setStringPreferences(KEY_WEATHER_DAY2, info.getForecastWeather2().getDay());
            _setStringPreferences(KEY_WEATHER_TEMP_HIGH2, info.getForecastWeather2().getTempHigh());
            _setStringPreferences(KEY_WEATHER_TEMP_LOW2, info.getForecastWeather2().getTempLow());
        }

        if (info.getForecaseWeather3() != null) {
            _setStringPreferences(KEY_WEATHER_CONDITION3, info.getForecaseWeather3().getCondition());
            _setStringPreferences(KEY_WEATHER_CODE3, info.getForecaseWeather3().getCode());
            _setStringPreferences(KEY_WEATHER_DATE3, info.getForecaseWeather3().getDate());
            _setStringPreferences(KEY_WEATHER_DAY3, info.getForecaseWeather3().getDay());
            _setStringPreferences(KEY_WEATHER_TEMP_HIGH3, info.getForecaseWeather3().getTempHigh());
            _setStringPreferences(KEY_WEATHER_TEMP_LOW3, info.getForecaseWeather3().getTempLow());
        }

        if (info.getForecastWeather4() != null) {
            _setStringPreferences(KEY_WEATHER_CONDITION4, info.getForecastWeather4().getCondition());
            _setStringPreferences(KEY_WEATHER_CODE4, info.getForecastWeather4().getCode());
            _setStringPreferences(KEY_WEATHER_DATE4, info.getForecastWeather4().getDate());
            _setStringPreferences(KEY_WEATHER_DAY4, info.getForecastWeather4().getDay());
            _setStringPreferences(KEY_WEATHER_TEMP_HIGH4, info.getForecastWeather4().getTempHigh());
            _setStringPreferences(KEY_WEATHER_TEMP_LOW4, info.getForecastWeather4().getTempLow());
        }

        _setLongPreferences(KEY_WEAther_UPDATE_TIME, System.currentTimeMillis());
    }
    public String getWeatherUpdateTimeString() {
        long time = 0;
        try {
            time = _getLongPreferences(KEY_WEAther_UPDATE_TIME, 0);
        } catch (ClassCastException e) {
            return _getStringPreferences(KEY_WEAther_UPDATE_TIME, "");
        }
        if (time != 0) {
            return DateFormat.format("MM/dd/yy kk:mm", time).toString();
        } else {
            return "";
        }
    }

    public long getWeatherUpdateTimeLong() {
        long time = 0;
        try {
            time = _getLongPreferences(KEY_WEAther_UPDATE_TIME, 0);
        } catch (ClassCastException e) {

        }
        return time;
    }
    public WeatherInfo getWeatherInfo() {
        WeatherInfo info = new WeatherInfo();
        info.setCity(_getStringPreferences(KEY_WEATHER_CITY, ""));
        info.setDate(_getStringPreferences(KEY_WEATHER_DATE, ""));
        info.setTemperature(_getStringPreferences(KEY_WEATHER_TEMP, ""));
        info.setCondition(_getStringPreferences(KEY_WEATHER_CONDITION, ""));
        info.setTempHigh(_getStringPreferences(KEY_WEATHER_TEMP_HIGH, ""));
        info.setTempLow(_getStringPreferences(KEY_WEATHER_TEMP_LOW, ""));
        info.setCode(_getStringPreferences(KEY_WEATHER_CODE, "3200"));
        info.setSunriseTime(_getStringPreferences(KEY_WEATHER_SUNRISE_TIME, ""));
        info.setSunsetTime(_getStringPreferences(KEY_WEATHER_SUNSET_TIME, ""));
        info.setWindDirection(_getStringPreferences(KEY_WEATHER_WIND_DIRECTION, ""));
        info.setWindSpeed(_getStringPreferences(KEY_WEATHER_WIND_SPEED, ""));

        String condition = null;
        String code = null;
        String date = null;
        String day = null;
        String tempHigh = null;
        String tempLow = null;

        ForecastWeatherInfo fWeather = null;

        condition = _getStringPreferences(KEY_WEATHER_CONDITION1, "");
        code = _getStringPreferences(KEY_WEATHER_CODE1, "");
        date = _getStringPreferences(KEY_WEATHER_DATE1, "");
        day = _getStringPreferences(KEY_WEATHER_DAY1, "");
        tempHigh = _getStringPreferences(KEY_WEATHER_TEMP_HIGH1, "");
        tempLow = _getStringPreferences(KEY_WEATHER_TEMP_LOW1, "");
        fWeather = new ForecastWeatherInfo(condition, code, date, day, tempHigh, tempLow);
        info.setForecastWeather1(fWeather);

        condition = _getStringPreferences(KEY_WEATHER_CONDITION2, "");
        code = _getStringPreferences(KEY_WEATHER_CODE2, "");
        date = _getStringPreferences(KEY_WEATHER_DATE2, "");
        day = _getStringPreferences(KEY_WEATHER_DAY2, "");
        tempHigh = _getStringPreferences(KEY_WEATHER_TEMP_HIGH2, "");
        tempLow = _getStringPreferences(KEY_WEATHER_TEMP_LOW2, "");
        fWeather = new ForecastWeatherInfo(condition, code, date, day, tempHigh, tempLow);
        info.setForecastWeather2(fWeather);

        condition = _getStringPreferences(KEY_WEATHER_CONDITION3, "");
        code = _getStringPreferences(KEY_WEATHER_CODE3, "");
        date = _getStringPreferences(KEY_WEATHER_DATE3, "");
        day = _getStringPreferences(KEY_WEATHER_DAY3, "");
        tempHigh = _getStringPreferences(KEY_WEATHER_TEMP_HIGH3, "");
        tempLow = _getStringPreferences(KEY_WEATHER_TEMP_LOW3, "");
        fWeather = new ForecastWeatherInfo(condition, code, date, day, tempHigh, tempLow);
        info.setForecastWeather3(fWeather);

        condition = _getStringPreferences(KEY_WEATHER_CONDITION4, "");
        code = _getStringPreferences(KEY_WEATHER_CODE4, "");
        date = _getStringPreferences(KEY_WEATHER_DATE4, "");
        day = _getStringPreferences(KEY_WEATHER_DAY4, "");
        tempHigh = _getStringPreferences(KEY_WEATHER_TEMP_HIGH4, "");
        tempLow = _getStringPreferences(KEY_WEATHER_TEMP_LOW4, "");
        fWeather = new ForecastWeatherInfo(condition, code, date, day, tempHigh, tempLow);
        info.setForecastWeather4(fWeather);

        return info;
    }

    public String getSunriseTime() {
        return _getStringPreferences(KEY_WEATHER_SUNRISE_TIME, "");
    }

    public String getSunsetTime() {
        return _getStringPreferences(KEY_WEATHER_SUNSET_TIME, "");
    }

    public void clearWeatherInfo() {
        _setStringPreferences(KEY_WEATHER_CITY, "");
        _setStringPreferences(KEY_WEATHER_CONDITION, "");
        _setStringPreferences(KEY_WEATHER_DATE, "");
        _setStringPreferences(KEY_WEATHER_TEMP, "");
        _setStringPreferences(KEY_WEATHER_TEMP_HIGH, "");
        _setStringPreferences(KEY_WEATHER_TEMP_LOW, "");
        _setStringPreferences(KEY_WEATHER_CODE, "3200");
    }


    private String _getStringPreferences(String strKey, String strDefaultValue) {
        /* Verify input */
        if (strKey == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid input parameter");
            return strDefaultValue;
        }

        /* Get value of setting from preference */
        SharedPreferences preferences = m_Context.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE);

        return preferences.getString(strKey, strDefaultValue);
    }

    private boolean _setStringPreferences(String strKey, String strValue) {
        /* Verify input parameter */
        if ((strKey == null) || (strValue == null)) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid input parameter");
            return false;
        }

        SharedPreferences preferences = m_Context.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = preferences.edit();
        settingEditor.putString(strKey, strValue);
        boolean bResult = settingEditor.commit();

        return bResult;
    }

    private long _getLongPreferences(String strKey, long longDefaultValue) {
        /* Verify input */
        if (strKey == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid input parameter");
            return longDefaultValue;
        }

        /* Get value of setting from preference */
        SharedPreferences preferences = m_Context.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE);

        return preferences.getLong(strKey, longDefaultValue);
    }

    private boolean _setLongPreferences(String strKey, long longValue) {
        /* Verify input parameter */
        if ((strKey == null)) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid input parameter");
            return false;
        }

        SharedPreferences preferences = m_Context.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = preferences.edit();
        settingEditor.putLong(strKey, longValue);
        boolean bResult = settingEditor.commit();

        return bResult;
    }
}
