package com.borqs.borqsweather.weather;
import com.borqs.borqsweather.weather.yahoo.WeatherInfo;
import com.borqs.borqsweather.weather.yahoo.ForecastWeatherInfo;

interface IWeatherService{
    WeatherInfo getWeather();
    int getConditionType();
    String getDisplayCondition();
    boolean isNight();
    void checkLocationServices();
    boolean checkWeatherInfoIsOld(inout WeatherInfo info);
    void checkToUpdateWeather(long interval);
    boolean isCelsius();
    ForecastWeatherInfo getForcastWeather(int day_index);
    int getlunarDay();
    String getDisplayWindCondition();
}