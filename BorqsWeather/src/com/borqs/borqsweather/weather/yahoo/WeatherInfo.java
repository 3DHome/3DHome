package com.borqs.borqsweather.weather.yahoo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;

public class WeatherInfo implements Parcelable{
    //private static final String TAG = "Weather_weather_info";

    /** Initialize description */
    public static final String DEFAULT_DATA = "No data";

    /** Celsius temperature */
    public static final int TEMPERATURE_FMT_CELSIUS = 1;

    /** Fahrenheit temperature */
    public static final int TEMPERATURE_FMT_FAHRENHEIT = 2;
    
    /**Forecast day index*/
    public static final int FORECAST_DAY_1 = 1;
    public static final int FORECAST_DAY_2 = 2;
    public static final int FORECAST_DAY_3 = 3;
    public static final int FORECAST_DAY_4 = 4;

    /** City */
    private String mCity;

    /** Weather Temperature, in Celsius */
    private String mTemperature;

    /** Country */
    //private String mCountry;
    /** Humidity */
    //private String mHumidity;
    /** Visibility */
    //private String mVisi;

    /** Weather condition text */
    private String mCondition;

    /** Weather condition code */
    private String mCode;

    /** Date */
    private String mDate;

    /** Temp unit */
    private String mTemperatureUnit;

    /** High temperature */
    private String mTempHigh;

    /** Low temperature */
    private String mTempLow;

    /** Sun rise, sun set **/
    private String mSunriseTime;
    private String mSunsetTime;
    
    /** Wind condition **/
    private String mWindDirection;
    private String mWindSpeed;
    
    // 4 more days weather info
    ForecastWeatherInfo mForecastWeather1 = null;
    ForecastWeatherInfo mForecastWeather2 = null;
    ForecastWeatherInfo mForecastWeather3 = null;
    ForecastWeatherInfo mForecastWeather4 = null;


    public WeatherInfo() {
        mCity = DEFAULT_DATA;
        //mCountry = DEFAULT_DATA;
        mTemperature = DEFAULT_DATA;
        //mHumidity = DEFAULT_DATA;
        mCondition = DEFAULT_DATA;
        mCode = "3200";
        mDate = DEFAULT_DATA;
        mTemperatureUnit = DEFAULT_DATA;
        //mVisi = DEFAULT_DATA;
        mTempHigh = DEFAULT_DATA;
        mTempLow = DEFAULT_DATA;
        mSunriseTime = null;
        mSunsetTime = null;
        mWindDirection = DEFAULT_DATA;
        mWindSpeed = DEFAULT_DATA;
        
        mForecastWeather1 = null;
        mForecastWeather2 = null;
        mForecastWeather3 = null;
        mForecastWeather4 = null;
    }

    public void setTemperature(String strTemp) {
        mTemperature = strTemp;
    }

    public String getTemperature(int nTempFmt) {
        if (nTempFmt == TEMPERATURE_FMT_FAHRENHEIT) {
            return celsiusToFahrenheit(mTemperature);
        }

        return mTemperature;
    }
    
    public ForecastWeatherInfo getForecastWeather(int day_index){
        switch(day_index){
        case FORECAST_DAY_1:
            return this.mForecastWeather1;
        case FORECAST_DAY_2:
            return this.mForecastWeather2;
        case FORECAST_DAY_3:
            return this.mForecastWeather3;
        case FORECAST_DAY_4:
            return this.mForecastWeather4;
            default :return null;
        }
    }

    public void setTempUnit(String strTempUnit) {
        mTemperatureUnit = strTempUnit;
    }

    public String getTempUnit() {
        return mTemperatureUnit;
    }

    public void setCode(String strCode) {
        mCode = strCode;
    }

    public String getCode() {
        return mCode;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String strCity) {
        mCity = strCity;
    }

    public String getDate() {
        if (mDate != null) {
            mDate = formatDate(mDate);
        }
        return mDate;
    }

    public void setDate(String strDate) {
        mDate = strDate;
    }

//    public String getCountry() {
//        return mCountry;
//    }
//
//    public void setCountry(String strCountry) {
//        mCountry = strCountry;
//    }
//
//    public String getHumidity() {
//        return mHumidity;
//    }
//
//    public void setHumidity(String strHumidity) {
//        mHumidity = strHumidity;
//    }
//    
//    public String getVisibility() {
//        return mVisi;
//    }
//
//    public void setVisibility(String strVisibility) {
//        mVisi = strVisibility;
//    }

    public String getCondition() {
        return mCondition;
    }

    public void setCondition(String strCondition) {
        mCondition = strCondition;
    }

    public String getTempHigh(int nTempFmt) {
        if (nTempFmt == TEMPERATURE_FMT_FAHRENHEIT) {
            return celsiusToFahrenheit(mTempHigh);
        }
        return mTempHigh;
    }

    public void setTempHigh(String strTempHigh) {
        this.mTempHigh = strTempHigh;
    }

    public String getTempLow(int nTempFmt) {
        if (nTempFmt == TEMPERATURE_FMT_FAHRENHEIT) {
            return celsiusToFahrenheit(mTempLow);
        }
        return mTempLow;
    }

    public void setTempLow(String strTempLow) {
        this.mTempLow = strTempLow;
    }

    public String getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTime(String strSunriseTime) {
        mSunriseTime = strSunriseTime;
    }

    public String getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(String strSunsetTime) {
        mSunsetTime = strSunsetTime;
    }
    
    public String getWindDirection() {
        return mWindDirection;
    }
    
    public void setWindDirection(String strWindDirection) {
        mWindDirection = strWindDirection;
    }
    
    public String getWindSpeed() {
        return mWindSpeed;
    }
    
    public void setWindSpeed(String strWindSpeed) {
        mWindSpeed = strWindSpeed;
    }

    public boolean isNoData() {
        return TextUtils.isEmpty(mCity) || TextUtils.isEmpty(mDate)
                || TextUtils.isEmpty(mCondition)
                || TextUtils.isEmpty(mTemperature)
                || TextUtils.isEmpty(mTempHigh)
                || TextUtils.isEmpty(mTempLow) || TextUtils.isEmpty(mCode)
                || DEFAULT_DATA.equals(mCity) || DEFAULT_DATA.equals(mDate)
                || DEFAULT_DATA.equals(mCondition)
                || DEFAULT_DATA.equals(mTemperature)
                || DEFAULT_DATA.equals(mTempHigh)
                || DEFAULT_DATA.equals(mTempLow) || "3200".equals(mCode);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof WeatherInfo)) {
            return false;
        }

        WeatherInfo info = (WeatherInfo) object;
        return info.getCity().equals(mCity) && info.getDate().equals(mDate)
                && info.getCondition().equals(mCondition)
                && info.getTemperature(0).equals(mTemperature)
                && info.getTempHigh(0).equals(mTempHigh)
                && info.getTempLow(0).equals(mTempLow)
                && info.getCode().equals(mCode);
    }

    public Date getFormatDate() {
        if (!TextUtils.isEmpty(mDate) && !DEFAULT_DATA.equals(mDate)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm aa", Locale.ENGLISH);
                Date date = (Date) sdf.parseObject(mDate);
                return date;
            } catch (Exception e) {
                // Log.e(TAG, "formate date has error: " + e.getMessage());
            }
        }
        return null;
    }

    private String formatDate(String strDateToParse) {
        StringBuilder sBuilder = new StringBuilder(strDateToParse);
        String strFormatDate = strDateToParse;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm aa", Locale.ENGLISH);
            Date date = (Date) sdf.parseObject(sBuilder.toString());
            strFormatDate = DateFormat.format("yyyy-MM-dd", date).toString();
        } catch (Exception e) {
            // Log.e(TAG, "formate date has error: " + e.getMessage());
        }
        return strFormatDate;
    }

    private String celsiusToFahrenheit(String cTemp) {
        if (TextUtils.isEmpty(cTemp) || DEFAULT_DATA.equals(cTemp)) {
            return "";
        }
        int c = Integer.parseInt(cTemp);
        float f = 9.0f * (float) c / 5.0f + 32.0f;
        return Integer.toString(Math.round(f));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCity);
        //dest.writeString(this.mCountry);
        dest.writeString(this.mTemperature);
        //dest.writeString(this.mHumidity);
        dest.writeString(this.mCondition);
        dest.writeString(this.mCode);
        dest.writeString(this.mDate);
        dest.writeString(this.mTemperatureUnit);
        //dest.writeString(this.mVisi);
        dest.writeString(this.mTempHigh);
        dest.writeString(this.mTempLow);
        dest.writeString(this.mSunriseTime);
        dest.writeString(this.mSunsetTime);
        dest.writeString(this.mWindDirection);
        dest.writeString(this.mWindSpeed);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!TextUtils.isEmpty(mCity)) {
            buffer.append("City : " + mCity).append(",");
        }
        if (!TextUtils.isEmpty(mDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            buffer.append("Date : " + formatter.format(getFormatDate())).append(",");
        }
        if (!TextUtils.isEmpty(mCondition)) {
            buffer.append("Condition : " + mCondition).append(",");
        }
        if (!TextUtils.isEmpty(mTemperature)) {
            buffer.append("Temperature : " + mTemperature).append(",");
        }
        if (!TextUtils.isEmpty(mTempLow)) {
            buffer.append("TempLow : " + mTempLow).append(".");
        }
        if (!TextUtils.isEmpty(mTempHigh)) {
            buffer.append("TempHigh : " + mTempHigh).append(".");
        }
        return buffer.toString();
    }

    public static final Parcelable.Creator<WeatherInfo> CREATOR = new Parcelable.Creator<WeatherInfo>() {

            @Override
            public WeatherInfo createFromParcel(Parcel source) {
                    return new WeatherInfo(source);
            }

            @Override
            public WeatherInfo[] newArray(int size) {
                    return new WeatherInfo[size];
            }

    };

    public void readFromParcel(Parcel _reply) {
        mCity = _reply.readString();
        //mCountry = _reply.readString();
        mTemperature = _reply.readString();
        //mHumidity = _reply.readString();
        mCondition = _reply.readString();
        mCode = _reply.readString();
        mDate = _reply.readString();
        mTemperatureUnit = _reply.readString();
        //mVisi = _reply.readString();
        mTempHigh = _reply.readString();
        mTempLow = _reply.readString();
        mSunriseTime = _reply.readString();
        mSunsetTime = _reply.readString();
        mWindDirection = _reply.readString();
        mWindSpeed = _reply.readString();
    } 
    
    private WeatherInfo(Parcel in){
        readFromParcel(in);
    }

    public ForecastWeatherInfo getForecastWeather1() {
        return mForecastWeather1;
    }

    public void setForecastWeather1(ForecastWeatherInfo forecastWeather1) {
        this.mForecastWeather1 = forecastWeather1;
    }

    public ForecastWeatherInfo getForecastWeather2() {
        return mForecastWeather2;
    }

    public void setForecastWeather2(ForecastWeatherInfo forecastWeather2) {
        this.mForecastWeather2 = forecastWeather2;
    }

    public ForecastWeatherInfo getForecaseWeather3() {
        return mForecastWeather3;
    }

    public void setForecastWeather3(ForecastWeatherInfo forecastWeather3) {
        this.mForecastWeather3 = forecastWeather3;
    }

    public ForecastWeatherInfo getForecastWeather4() {
        return mForecastWeather4;
    }

    public void setForecastWeather4(ForecastWeatherInfo forecastWeather4) {
        this.mForecastWeather4 = forecastWeather4;
    }
}
