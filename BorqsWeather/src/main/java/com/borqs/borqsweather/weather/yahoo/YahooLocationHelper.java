package com.borqs.borqsweather.weather.yahoo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.borqs.borqsweather.weather.State;
import com.borqs.borqsweather.weather.WeatherController;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class YahooLocationHelper {

    /** WOID */
    private static final String PARAM_WOEID = "woeid";

    /** Currently not support this country */
    public static final int WOEID_NOTSUPPORT = -1;

    /** TAG for debugging */
    private static final String TAG = "Weather_lhelper";

    /** Instance of yahoo location support */
    private static YahooLocationHelper m_Instance;

    private YahooLocationHelper() {
        /* Do nothing */
    }

    public synchronized YahooLocationHelper getInstance() {
        if (m_Instance == null) {
            m_Instance = new YahooLocationHelper();
        }

        return m_Instance;
    }

    // public int getWOEIDByLocation(String strLocation){
    // /* Verify input */
    // if (strLocation == null){
    // if (SEApplication.DEBUG) Log.e(TAG,"Invalid input parameter");
    // return WOEID_NOTSUPPORT;
    // }
    //
    // int nWOEID = WOEID_NOTSUPPORT;
    // /* T.B.D */
    //
    // return nWOEID;
    // }

    public static String parserWOEIDData(Document docWeather, Bundle bundle) {
        if (docWeather == null) {
            // if (WeatherController.DEBUG) Log.e(TAG, "Invalid doc weather");
            Log.e(TAG, "Invalid WOEID doc weather");
            return null;
        }
        double minDistance = Double.MAX_VALUE;
        String strWOEID = null;
        String latitude = bundle.getString(State.BUNDLE_LATITUDE);
        String longitude = bundle.getString(State.BUNDLE_LONGITUDE);
        try {
            Element root = docWeather.getDocumentElement();
            root.normalize();
            NodeList placeNodes = root.getElementsByTagName("place");
            for (int i = 0; i < placeNodes.getLength(); i++) {
                Element placeChildElement = (Element) placeNodes.item(i);
                String tWoeid = placeChildElement.getElementsByTagName("woeid").item(0).getTextContent();
                double tLatitude = Double.parseDouble(placeChildElement.getElementsByTagName("latitude").item(0)
                        .getTextContent());
                double tLongitude = Double.parseDouble(placeChildElement.getElementsByTagName("longitude").item(0)
                        .getTextContent());
                double tempDis = Math.abs(Double.parseDouble(latitude) - tLatitude) + Math.abs(Double.parseDouble(longitude) - tLongitude);
                if (tempDis < minDistance) {
                    minDistance = tempDis;
                    strWOEID = tWoeid;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parserWOEIDData error" + e.getMessage());
            return null;
        }

        //Log.d(TAG, "OK to get WOEID--" + strWOEID);
        return strWOEID;
    }

    public static List<CityEntity> getCitysFromDocument(String location, Document docWeather) {
        if (docWeather == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid doc weather getCitysFromDocument");
            Log.e(TAG, "Invalid doc weather getCitysFromDocument");

            return null;
        }

        List<CityEntity> citys = null;
        CityEntity c = null;
        List<CityEntity> cityWithAdmin2 = new ArrayList<CityEntity>();
        List<CityEntity> cityWithAdmin1 = new ArrayList<CityEntity>();
        location = location.replace("_", "").toLowerCase();
        try {
            Element root = docWeather.getDocumentElement();
            root.normalize();
            NodeList placeNodes = root.getElementsByTagName("place");
            citys = new ArrayList<CityEntity>();
            for (int i = 0; i < placeNodes.getLength(); i++) {
                Element child = (Element) placeNodes.item(i);
                c = new CityEntity();
                c.mCountry = child.getElementsByTagName("country").item(0).getTextContent();
                c.mProvince = child.getElementsByTagName("admin1").item(0).getTextContent();
                c.mCity = child.getElementsByTagName("admin2").item(0).getTextContent();
                c.mWoeid = child.getElementsByTagName("woeid").item(0).getTextContent();
                Element centroid =  (Element)child.getElementsByTagName("centroid").item(0);
                c.mLatitude = centroid.getChildNodes().item(0).getTextContent();
                c.mLongitude = centroid.getChildNodes().item(1).getTextContent();
                if (!TextUtils.isEmpty(c.mCity)) {
                    boolean shouldBeAdded = true;
                    for (CityEntity tempCity : cityWithAdmin2) {
                        if (c.mCountry.equals(tempCity.mCountry) && c.mProvince.equals(tempCity.mProvince)
                                && c.mCity.equals(tempCity.mCity)) {
                            shouldBeAdded = false;
                            break;
                        }
                    }
                    if (shouldBeAdded) {
                        cityWithAdmin2.add(c);
                    }
                } else if (!TextUtils.isEmpty(c.mProvince)) {
                    boolean shouldBeAdded = true;
                    for (CityEntity tempCity : cityWithAdmin1) {
                        if (c.mCountry.equals(tempCity.mCountry) && c.mProvince.equals(tempCity.mProvince)) {
                            shouldBeAdded = false;
                            break;
                        }
                    }
                    if (shouldBeAdded) {
                        cityWithAdmin1.add(c);
                    }
                }
            }
            citys.addAll(cityWithAdmin2);
            for (CityEntity city1 : cityWithAdmin1) {
                boolean shouldBeAdded = true;
                for (CityEntity city2 : citys) {
                    if (city1.mProvince.equals(city2.mProvince)) {
                        shouldBeAdded = false;
                        break;
                    }
                }
                if (shouldBeAdded) {
                    citys.add(city1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Something wroing with parser data");
            return null;
        }
        return citys;
    }

    public static class CityEntity {
        public String mWoeid;
        public String mCountry;
        public String mProvince;
        public String mCity;
        public String mLatitude;
        public String mLongitude;
        public String mNeighborhood;

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (!TextUtils.isEmpty(mCountry)) {
                buffer.append("Country : " + mCountry).append(",");
            }
            if (!TextUtils.isEmpty(mProvince)) {
                buffer.append("Province : " + mProvince).append(",");
            }

            if (!TextUtils.isEmpty(mCity)) {
                buffer.append("City : " + mCity).append(",");
            }
            if (!TextUtils.isEmpty(mWoeid)) {
                buffer.append("woeid : " + mWoeid).append(".");
            }
            return buffer.toString();
        }

    }
    public static CityEntity parserCityAndWoeid(Document location) {
        if (location == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "parserCityAndWoeid Invalid doc loction");
            return null;
        }
        CityEntity cityEntity = null;
        try {
            Element root = location.getDocumentElement();
            root.normalize();
            NodeList placeNodes = root.getElementsByTagName("Result");
            for (int i = 0; i < placeNodes.getLength(); i++) {
                Element placeChildElement = (Element) placeNodes.item(i);
                String neighborhood = placeChildElement.getElementsByTagName("neighborhood").item(0).getTextContent();
                String city = placeChildElement.getElementsByTagName("city").item(0).getTextContent();
                String woeid = placeChildElement.getElementsByTagName("woeid").item(0).getTextContent();
                cityEntity = new CityEntity();
                cityEntity.mCity = city;
                cityEntity.mNeighborhood = neighborhood;
                cityEntity.mWoeid = woeid;
                return cityEntity;
            }

        } catch (Exception e) {
            Log.e(TAG, "Something wrong with doc parser location data");
            return null;
        }
        return cityEntity;
    }

    public static String parserCityName(Document location) {
        if (location == null) {
            if (WeatherController.DEBUG)
                Log.e(TAG, "Invalid doc loction");
            return null;
        }

        try {
            Element root = location.getDocumentElement();
            root.normalize();
            NodeList resultNodes = root.getElementsByTagName("result");

            for (int i = 0; i < resultNodes.getLength(); i++) {

                Element resultNode = (Element) resultNodes.item(i);
                NodeList childList = resultNode.getChildNodes();
                for (int j = 0; j < childList.getLength(); j++) {
                    if ("locality".equals(childList.item(j).getTextContent())
                            && "type".equals(childList.item(j).getNodeName())) {
                        String s = resultNode.getElementsByTagName("formatted_address").item(0).getTextContent();
                        return s.split(",")[0];
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Something wrong with doc parser location data");
            return null;
        }

        return null;
    }

    public static String parserCityName(JSONObject json) {
        if (json == null) {
            Log.e(TAG, "Invalid json loction");
            return null;
        }
        String city = null;
        JSONObject arr;
        try {
            arr = json.getJSONObject("location");
            JSONObject address = arr.getJSONObject("address");
            city = address.getString("city");
        } catch (JSONException e) {
            Log.e(TAG, "Something wrong with json parser location data");
            return null;
        }
        return city;
    }
}
