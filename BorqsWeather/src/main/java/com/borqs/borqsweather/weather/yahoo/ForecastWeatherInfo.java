package com.borqs.borqsweather.weather.yahoo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ForecastWeatherInfo implements Parcelable{

    String mCondtion = null;
    String mCode = null;
    String mDate = null;
    String mDay = null;
    String mTempHigh = null;
    String mTempLow = null;

    public ForecastWeatherInfo(String condition,String code,String date,String day,String tempHigh,String tempLow){
        mCondtion = (condition == null ? WeatherInfo.DEFAULT_DATA : condition);
        mCode = (code == null ? "3200" : code);
        mDate = (date == null ? WeatherInfo.DEFAULT_DATA : date);
        mDay =(day == null ? WeatherInfo.DEFAULT_DATA : day);
        mTempHigh = (tempHigh == null ? WeatherInfo.DEFAULT_DATA : tempHigh);
        mTempLow = (tempLow == null ? WeatherInfo.DEFAULT_DATA : tempLow);
    }
    
    
    public String getCondition() {
        return mCondtion;
    }

    public void setCondition(String condition) {
        mCondtion = condition;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDay() {
        return mDay;
    }

    public void setDay(String day) {
        mDay = day;
    }

    public String getTempHigh(int nTempFmt) {
        if (nTempFmt == WeatherInfo.TEMPERATURE_FMT_FAHRENHEIT) {
            return celsiusToFahrenheit(mTempHigh);
        }
        return mTempHigh;
    }
    
    private String celsiusToFahrenheit(String cTemp) {
        if (TextUtils.isEmpty(cTemp) || WeatherInfo.DEFAULT_DATA.equals(cTemp)) {
            return "";
        }
        int c = Integer.parseInt(cTemp);
        float f = 9.0f * (float) c / 5.0f + 32.0f;
        return Integer.toString(Math.round(f));
    }
    
    public String getTempHigh() {
        return mTempHigh;
    }

    public void setTempHigh(String tempHigh) {
        mTempHigh = tempHigh;
    }

    public String getTempLow() {
        return mTempLow;
    }
    
    public String getTempLow(int nTempFmt) {
        if (nTempFmt == WeatherInfo.TEMPERATURE_FMT_FAHRENHEIT) {
            return celsiusToFahrenheit(mTempLow);
        }
        return mTempLow;
    }

    public void setTempLow(String tempLow) {
        mTempLow = tempLow;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCondtion);
        dest.writeString(mCode);
        dest.writeString(mDate);
        dest.writeString(mDay);
        dest.writeString(mTempHigh);
        dest.writeString(mTempLow);
    }
    
    public static final Parcelable.Creator<ForecastWeatherInfo> CREATOR = new Parcelable.Creator<ForecastWeatherInfo>() {

        @Override
        public ForecastWeatherInfo createFromParcel(Parcel source) {
            return new ForecastWeatherInfo(source);
        }

        @Override
        public ForecastWeatherInfo[] newArray(int size) {
            return new ForecastWeatherInfo[size];
        }

    };

    private ForecastWeatherInfo(Parcel in) {
        readFromParcel(in);
    }
    
    public void readFromParcel(Parcel _reply) {
        mCondtion = _reply.readString();
        mCode = _reply.readString();
        mDate = _reply.readString();
        mDay = _reply.readString();
        mTempHigh = _reply.readString();
        mTempLow = _reply.readString();
    }

}
