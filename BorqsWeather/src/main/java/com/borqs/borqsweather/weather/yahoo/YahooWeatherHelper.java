package com.borqs.borqsweather.weather.yahoo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.borqs.borqsweather.weather.Utils;
import com.borqs.borqsweather.weather.WeatherController;


import android.text.TextUtils;
import android.util.Log;

public class YahooWeatherHelper {
    /** For debugging */
    private static final String TAG = "Weather_whelper";

    /** Yahoo attribute */
    private static final String PARAM_YAHOO_LOCATION = "yweather:location";
    private static final String PARAM_YAHOO_CONDITION = "yweather:condition";
    private static final String PARAM_YAHOO_FORECAST = "yweather:forecast";
    private static final String PARAM_YAHOO_ASTRONOMY = "yweather:astronomy";
    private static final String PARAM_YAHOO_WIND = "yweather:wind";

    /** Attribute city */
    private static final String ATT_YAHOO_CITY = "city";
    private static final String ATT_YAHOO_COUNTRY = "country";
    private static final String ATT_YAHOO_CONDITION = "text";
    private static final String ATT_YAHOO_CODE = "code";
    private static final String ATT_YAHOO_DATE = "date";
    private static final String ATT_YAHOO_DAY = "day";
    private static final String ATT_YAHOO_TEMP = "temp";
    private static final String ATT_YAHOO_TEMP_HIGH = "high";
    private static final String ATT_YAHOO_TEMP_LOW = "low";
    private static final String ATT_YAHOO_SUN_RISE = "sunrise";
    private static final String ATT_YAHOO_SUN_SET = "sunset";
    private static final String ATT_YAHOO_WIND_DIRECTION = "direction";
    private static final String ATT_YAHOO_WIND_SPEED = "speed";

    public static WeatherInfo parserYahooWeatherInfo(Document docWeather) {
        if (docWeather == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid doc weatehr");
            return null;
        }

        String strCity = null;
        String strCountry = null;
        String strTemp = null;
        String strCondition = null;
        String strCode = null;
        String strDate = null;
        String strTempHigh = null;
        String strTempLow = null;
        
        String strSunrise = null;
        String strSunset = null;

        String strWindDirection = null;
        String strWindSpeed = null;

        // 4 more days weather info
        ForecastWeatherInfo forecastDay1=null;
        ForecastWeatherInfo forecastDay2=null;
        ForecastWeatherInfo forecastDay3=null;
        ForecastWeatherInfo forecastDay4=null;
        
        String strfText = null;
        String strfCode = null;
        String strfDate = null;
        String strfDay = null;
        String strfTempHigh = null;
        String strfTempLow = null;

        
        try {
            Element root = docWeather.getDocumentElement();
            root.normalize();

            NamedNodeMap locationNode = root.getElementsByTagName(PARAM_YAHOO_LOCATION).item(0).getAttributes();

            if (locationNode != null) {
                strCity = locationNode.getNamedItem(ATT_YAHOO_CITY).getNodeValue();
                strCountry = locationNode.getNamedItem(ATT_YAHOO_COUNTRY).getNodeValue();
            }


            NamedNodeMap conditionNode = root.getElementsByTagName(PARAM_YAHOO_CONDITION).item(0).getAttributes();
            if (conditionNode != null) {
                strCondition = conditionNode.getNamedItem(ATT_YAHOO_CONDITION).getNodeValue();
                strCode = conditionNode.getNamedItem(ATT_YAHOO_CODE).getNodeValue();
                strDate = conditionNode.getNamedItem(ATT_YAHOO_DATE).getNodeValue();
                strTemp = conditionNode.getNamedItem(ATT_YAHOO_TEMP).getNodeValue();
            }

            NamedNodeMap temHNode = root.getElementsByTagName(PARAM_YAHOO_FORECAST).item(0).getAttributes();
            if (temHNode != null) {
                strTempHigh = temHNode.getNamedItem(ATT_YAHOO_TEMP_HIGH).getNodeValue();
                strTempLow = temHNode.getNamedItem(ATT_YAHOO_TEMP_LOW).getNodeValue();
            }
            
            // 4 more days weather info
            NamedNodeMap temHNode1 = root
                    .getElementsByTagName(PARAM_YAHOO_FORECAST).item(1)
                    .getAttributes();
            if (temHNode1 != null) {
                strfText = temHNode1.getNamedItem(ATT_YAHOO_CONDITION)
                        .getNodeValue();
                strfCode = temHNode1.getNamedItem(ATT_YAHOO_CODE)
                        .getNodeValue();
                strfDate = temHNode1.getNamedItem(ATT_YAHOO_DATE)
                        .getNodeValue();
                strfDay = temHNode1.getNamedItem(ATT_YAHOO_DAY).getNodeValue();
                strfTempHigh = temHNode1.getNamedItem(ATT_YAHOO_TEMP_HIGH)
                        .getNodeValue();
                strfTempLow = temHNode1.getNamedItem(ATT_YAHOO_TEMP_LOW)
                        .getNodeValue();
                forecastDay1 = new ForecastWeatherInfo(strfText, strfCode, strfDate,
                        strfDay, strfTempHigh, strfTempLow);
            }

            NamedNodeMap temHNode2 = root
                    .getElementsByTagName(PARAM_YAHOO_FORECAST).item(2)
                    .getAttributes();
            if (temHNode2 != null) {
                strfText = temHNode2.getNamedItem(ATT_YAHOO_CONDITION)
                        .getNodeValue();
                strfCode = temHNode2.getNamedItem(ATT_YAHOO_CODE)
                        .getNodeValue();
                strfDate = temHNode2.getNamedItem(ATT_YAHOO_DATE)
                        .getNodeValue();
                strfDay = temHNode2.getNamedItem(ATT_YAHOO_DAY).getNodeValue();
                strfTempHigh = temHNode2.getNamedItem(ATT_YAHOO_TEMP_HIGH)
                        .getNodeValue();
                strfTempLow = temHNode2.getNamedItem(ATT_YAHOO_TEMP_LOW)
                        .getNodeValue();
                forecastDay2 = new ForecastWeatherInfo(strfText, strfCode, strfDate,
                        strfDay, strfTempHigh, strfTempLow);
            }

            NamedNodeMap temHNode3 = root
                    .getElementsByTagName(PARAM_YAHOO_FORECAST).item(3)
                    .getAttributes();
            if (temHNode3 != null) {
                strfText = temHNode3.getNamedItem(ATT_YAHOO_CONDITION)
                        .getNodeValue();
                strfCode = temHNode3.getNamedItem(ATT_YAHOO_CODE)
                        .getNodeValue();
                strfDate = temHNode3.getNamedItem(ATT_YAHOO_DATE)
                        .getNodeValue();
                strfDay = temHNode3.getNamedItem(ATT_YAHOO_DAY).getNodeValue();
                strfTempHigh = temHNode3.getNamedItem(ATT_YAHOO_TEMP_HIGH)
                        .getNodeValue();
                strfTempLow = temHNode3.getNamedItem(ATT_YAHOO_TEMP_LOW)
                        .getNodeValue();
                forecastDay3 = new ForecastWeatherInfo(strfText, strfCode, strfDate,
                        strfDay, strfTempHigh, strfTempLow);
            }

            NamedNodeMap temHNode4 = root
                    .getElementsByTagName(PARAM_YAHOO_FORECAST).item(4)
                    .getAttributes();
            if (temHNode4 != null) {
                strfText = temHNode4.getNamedItem(ATT_YAHOO_CONDITION)
                        .getNodeValue();
                strfCode = temHNode4.getNamedItem(ATT_YAHOO_CODE)
                        .getNodeValue();
                strfDate = temHNode4.getNamedItem(ATT_YAHOO_DATE)
                        .getNodeValue();
                strfDay = temHNode4.getNamedItem(ATT_YAHOO_DAY).getNodeValue();
                strfTempHigh = temHNode4.getNamedItem(ATT_YAHOO_TEMP_HIGH)
                        .getNodeValue();
                strfTempLow = temHNode4.getNamedItem(ATT_YAHOO_TEMP_LOW)
                        .getNodeValue();
                forecastDay4 = new ForecastWeatherInfo(strfText, strfCode, strfDate,
                        strfDay, strfTempHigh, strfTempLow);
            }

            
            NamedNodeMap astronomyNode = root.getElementsByTagName(PARAM_YAHOO_ASTRONOMY).item(0).getAttributes();

            if (astronomyNode != null) {
                strSunrise = astronomyNode.getNamedItem(ATT_YAHOO_SUN_RISE).getNodeValue();
                strSunset = astronomyNode.getNamedItem(ATT_YAHOO_SUN_SET).getNodeValue();
            }

            NamedNodeMap windNode = root.getElementsByTagName(PARAM_YAHOO_WIND).item(0).getAttributes();

            if (windNode != null) {
                strWindDirection = windNode.getNamedItem(ATT_YAHOO_WIND_DIRECTION).getNodeValue();
                if (strWindDirection == null) {
                    strWindDirection = "";
                }
                strWindSpeed = windNode.getNamedItem(ATT_YAHOO_WIND_SPEED).getNodeValue();
                if (strWindSpeed == null) {
                    strWindSpeed = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Something wrong with parser weather data");
            return null;
        }

        strSunrise = Utils.get24Time(strSunrise);
        if (TextUtils.isEmpty(strSunrise)) {
            strSunrise = "6:00";
        }
        strSunset = Utils.get24Time(strSunset);
        if (TextUtils.isEmpty(strSunset)) {
            strSunset = "19:00";
        }

        WeatherInfo yahooWeatherInfo = new WeatherInfo();
        yahooWeatherInfo.setCity(strCity);
        //yahooWeatherInfo.setCountry(strCountry);
        yahooWeatherInfo.setTemperature(strTemp);
        yahooWeatherInfo.setCondition(strCondition);
        yahooWeatherInfo.setCode(strCode);
        yahooWeatherInfo.setDate(strDate);
        yahooWeatherInfo.setTempHigh(strTempHigh);
        yahooWeatherInfo.setTempLow(strTempLow);
        yahooWeatherInfo.setSunriseTime(strSunrise);
        yahooWeatherInfo.setSunsetTime(strSunset);
        yahooWeatherInfo.setWindDirection(strWindDirection);
        yahooWeatherInfo.setWindSpeed(strWindSpeed);
        yahooWeatherInfo.setForecastWeather1(forecastDay1);
        yahooWeatherInfo.setForecastWeather2(forecastDay2);
        yahooWeatherInfo.setForecastWeather3(forecastDay3);
        yahooWeatherInfo.setForecastWeather4(forecastDay4);

        if(WeatherController.DEBUG){
            Log.d(TAG, "Weather information: Begin");
            Log.d(TAG, "Place: " + strCity + ", " +strCountry);
            Log.d(TAG, "Weather temp: " + strTemp + ", " + "condidtion: " + strCode + ", " + "high: " + strTempHigh + ", " + "low: " + strTempLow + ", ");
            Log.d(TAG, "Sunrise: " + strSunrise + ", " + "Sunset: " + strSunset);
            Log.d(TAG, "Weather information: End");
        }

        return yahooWeatherInfo;
    }
}
