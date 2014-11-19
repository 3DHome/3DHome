package com.borqs.borqsweather.weather;

import com.borqs.borqsweather.LunarCalendar;
import com.borqs.borqsweather.weather.yahoo.ForecastWeatherInfo;
import com.borqs.borqsweather.weather.yahoo.WeatherInfo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;

public class WeatherService extends Service{
    private LoopForInfoService mLoopForWeatherInfo;
    
    private final IWeatherService.Stub mBinder = new IWeatherService.Stub() {
        WeatherInfo info;
        @Override
        public WeatherInfo getWeather() throws RemoteException {
            info = mLoopForWeatherInfo.getWeather();
            return info;
        }

        @Override
        public int getConditionType() throws RemoteException {
            return mLoopForWeatherInfo.getConditionType();
        }

        @Override
        public String getDisplayCondition() throws RemoteException {
            return mLoopForWeatherInfo.getDisplayCondition();
        }

        @Override
        public boolean isNight() throws RemoteException {
            return mLoopForWeatherInfo.isNight();
        }

        @Override
        public void checkLocationServices() throws RemoteException {
            mLoopForWeatherInfo.checkLocationServices();
        }

        @Override
        public boolean checkWeatherInfoIsOld(WeatherInfo info)
                throws RemoteException {
            return mLoopForWeatherInfo.checkWeatherInfoIsOld(info);
        }

        @Override
        public void checkToUpdateWeather(long interval) throws RemoteException {
            mLoopForWeatherInfo.checkToUpdateWeather(interval);
        }

        @Override
        public boolean isCelsius() throws RemoteException {
            return WeatherSettings.isCelsius(WeatherService.this);
        }

        @Override
        public ForecastWeatherInfo getForcastWeather(int day_index) throws RemoteException {
            return info.getForecastWeather(day_index);
        }

        @Override
        public int getlunarDay() throws RemoteException {
            int lunarDay = (int) LunarCalendar.today()[2];
            return lunarDay;
        }

        @Override
        public String getDisplayWindCondition()  throws RemoteException {
            return mLoopForWeatherInfo.getDisplayWindCondition();
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();

        if (mLoopForWeatherInfo == null) {
            //mLoopForWeatherInfo = new LoopForInfoService(this.getApplicationContext());
            mLoopForWeatherInfo = LoopForInfoService.getInstance(this.getApplicationContext());
        }        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
