package com.borqs.borqsweather.weather.yahoo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.w3c.dom.Document;

import com.borqs.borqsweather.weather.State;
import com.borqs.borqsweather.weather.Utils;
import com.borqs.borqsweather.weather.WeatherController;
import com.borqs.borqsweather.weather.yahoo.YahooLocationHelper.CityEntity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class WeatherDataModel {
    /** For debugging */
    private static final String TAG = "Weather_WeatherDataModel";

    private static final boolean DEBUG = WeatherController.DEBUG;

    /** URL for Yahoo API */
    private static final String URL_YAHOO_API_WEATHER = "http://query.yahooapis.com/v1/public/yql?q=select+*+from+weather.forecast+where+woeid+=+'%s'+and+u+=+'c'&format=xml";
    private static final String URL_LOCAL_API_WEATHER = "http://app.borqs.com:9328/forecastrss/city?%s";
    private static final String URL_LOCAL_NEW_API_WEATHER = "http://app.borqs.com:9328/forecastrss/latlon?%s";

    /** Query to get location */
    private static final String MANUAL_GET_LOCATION_WOEID = "http://query.yahooapis.com/v1/public/yql?q=select+woeid+,+country+,+admin1+,+admin2+,+centroid+from+geo.places+where+text+=+'%s'&format=xml";

   
    private static final String GET_CITY_AND_WOEID_BY_LOCATION = "http://query.yahooapis.com/v1/public/yql?q=select+city+,+neighborhood+,+woeid+from+geo.placefinder+where+text+=+'%f,%f'+and+gflags+=+'R'&format=xml";

    private static final int YAHOO_API = 1;
    private static final int LOCAL_API = 2;
    private static final int NEW_LOCAL_API = 3;

    /** Connect helper for connection */
    private HttpConnectHelper m_ConnectHelper = null;

    /** Data model instance */
    private static WeatherDataModel m_Instance = null;

    private WeatherDataModel() {
        /* Create for connect helper */
        m_ConnectHelper = new HttpConnectHelper();
    }

    public static synchronized WeatherDataModel getInstance() {
        if (m_Instance == null) {
            m_Instance = new WeatherDataModel();
        }

        return m_Instance;
    }

    public WeatherInfo getWeatherData(Bundle bundle, String woeid) {
        WeatherInfo weather = null;

        /* Firstly use Yahoo API */
        Document docWeather = null;
        if (!TextUtils.isEmpty(woeid)) {
            docWeather = getWeatherDocFromYahooServer(YAHOO_API, woeid);
            weather = YahooWeatherHelper.parserYahooWeatherInfo(docWeather);
        }

        if (weather == null) {
            /* Use local API */
            docWeather = getWeatherDocFromLocalServer(LOCAL_API, bundle, woeid);
            weather = YahooWeatherHelper.parserYahooWeatherInfo(docWeather);
        }
        if (weather == null || (weather != null && weather.isNoData())) {
            /* Use new local API */
            docWeather = getWeatherDocFromLocalServer(NEW_LOCAL_API, bundle, woeid);
            weather = YahooWeatherHelper.parserYahooWeatherInfo(docWeather);
        }

        return weather;
    }

    private Document getWeatherDocFromYahooServer(int apiType, String woeid) {
        Document docWeather = reqWeatherDoc(apiType, woeid);
        return docWeather;
    }

    public String transCoding(String code){
        if (!TextUtils.isEmpty(code)) {
            code = URLEncoder.encode(code);
        }
        return code;
    }

    private String assembledWeatherParameter(int apiType, Bundle bundle, String woeid){
        if (TextUtils.isEmpty(woeid)) {
            woeid = "";
        }
        String parameter = null;
        String country = transCoding(bundle.getString(State.BUNDLE_COUNTRY));
        String province = transCoding(bundle.getString(State.BUNDLE_PROVINCE));
        String city = transCoding(bundle.getString(State.BUNDLE_CITY));
        String latitude = bundle.getString(State.BUNDLE_LATITUDE);
        String longitude = bundle.getString(State.BUNDLE_LONGITUDE);
        if (TextUtils.isEmpty(country) && TextUtils.isEmpty(province) && TextUtils.isEmpty(city)) {
            parameter = "w=" + woeid;
        } else {
            parameter = "place=" + city + "," + province + "," + country + "&w=" + woeid;
        }

        if ( NEW_LOCAL_API == apiType) {
            parameter += "&q=" + latitude + "," + longitude; 
        }
        return parameter;
    }

    private Document getWeatherDocFromLocalServer(int apiType, Bundle bundle, String woeid) {
        if (DEBUG)
            Log.d(TAG, "get weather from local server");
        Document docWeather = null;
        String parameter = assembledWeatherParameter(apiType, bundle, woeid);
        if (parameter != null) {
            docWeather = reqWeatherDoc(apiType, parameter);
        }
        return docWeather;
    }

    private Document reqWeatherDoc(int apiType, String strData) {
        Document doc = null;
        String url = createURL(apiType, strData);
        if (url == null) {
            return null;
        }
        Utils.printToLogFile("获取天气 URL：");
        Utils.printToLogFile(url);
        if (DEBUG)
            Log.d(TAG, "Request to weather server: " + url);

        try {
            doc = m_ConnectHelper.getDocumentFromURL(url);
        } catch (Exception e) {
            if (DEBUG)
                Log.e(TAG, "Api get weather error:" + apiType + " | " + e.getMessage());
        }
        return doc;
    }

    public List<CityEntity> getCityList(String city) {
        try {
            String strQueryWOEID = createQueryGetWoeid(URLEncoder.encode(city, "utf-8"), MANUAL_GET_LOCATION_WOEID);
            if (WeatherController.DEBUG)
                Log.d("Weather_getCityList", "######URL#####" + strQueryWOEID);
            if (strQueryWOEID == null) {
                if (WeatherController.DEBUG)
                    Log.e(TAG, "getCityList Can not create WOEID");
                return null;
            }
            Document doc = m_ConnectHelper.getDocumentFromURL(strQueryWOEID);
            List<CityEntity> citys = YahooLocationHelper.getCitysFromDocument(city, doc);
            return citys;
        } catch (Exception e) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "getCityList XML Pasing error woeid:" + e);
            return null;
        }
    }

    private String createURL(int nRequestType, String strData) {
        if (strData == null) {
            if (DEBUG)
                Log.e(TAG, "Invalid input data");
            return null;
        }

        String strRegURL = null;
        switch (nRequestType) {
        case YAHOO_API:
            strRegURL = String.format(URL_YAHOO_API_WEATHER, strData);
            break;
        case LOCAL_API:
            strRegURL = String.format(URL_LOCAL_API_WEATHER, strData);
            break;
        case NEW_LOCAL_API:
            strRegURL = String.format(URL_LOCAL_NEW_API_WEATHER, strData);
            break;
        default:
            if (DEBUG)
                Log.e(TAG, "Not support this request:" + nRequestType);
            return null;
        }

        return strRegURL;
    }

    private String createQueryGetWoeid(String strQuerry, String url) {
        if (strQuerry == null) {
            return null;
        }
        return String.format(url, strQuerry);
    }


    public CityEntity getCityAndWoeidByLocation(double[] location) {
        String urlString = createGetCityAndWoeidURL(location);
        Utils.printToLogFile("获取城市以及WOEID URL：");
        Utils.printToLogFile(urlString);
        if (DEBUG)
            Log.d("Weather_getCityAndWoeidByLocation", "######URL#####" + urlString);
        try {
            Document loctionDoc = m_ConnectHelper.getDocumentFromURL(urlString);
            return YahooLocationHelper.parserCityAndWoeid(loctionDoc);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    private String createGetCityAndWoeidURL(double[] location) {
        if (location == null || location[0] == Double.MAX_VALUE || location[1] == Double.MAX_VALUE) {
            return null;
        }
        String url = String.format(GET_CITY_AND_WOEID_BY_LOCATION, location[0], location[1]);
        return url;
    }
}
