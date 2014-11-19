package com.borqs.borqsweather.weather;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.borqsweather.R;
import com.borqs.borqsweather.XmlUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;



public class WeatherConditions {

    private final static boolean DEBUG = false;
    public static final String TAG = "Weather_WeatherConditions";

    private Context mContext;
    private static WeatherConditions sInstance = null;
    private Map<Object, Condition> mAllConditions = new HashMap<Object, Condition>();
    private int mResouseId;

    public static final int CONDITION_TYPE_NONE = -1;
    public static final int CONDITION_TYPE_CLOUDY = 0;
    public static final int CONDITION_TYPE_RAIN = 1;
    public static final int CONDITION_TYPE_SNOW = 2;
    public static final int CONDITION_TYPE_THUNDER = 3;
    public static final int CONDITION_TYPE_SUN = 4;
    public static final int CONDITION_TYPE_OVERCAST = 5;
    public static final int CONDITION_TYPE_FOG = 6;
    public static final int CONDITION_TYPE_DUST = 7;
    public static final int CONDITION_TYPE_RAIN_SNOW = 8;
    public static final int CONDITION_TYPE_HAIL = 9;
    public static final int CONDITION_TYPE_SLEET = 10;

    private WeatherConditions(Context ctx) {
        mContext = ctx;
        mResouseId = R.xml.yahoo_weather_condition_list;
        init();
    }

    public static synchronized WeatherConditions getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new WeatherConditions(ctx);
            return sInstance;
        }
        return sInstance;
    }

    private void init() {
        loadConditionsFromXml();
    }

    private void loadConditionsFromXml() {
        XmlPullParser parser = mContext.getResources().getXml(mResouseId);
        try {
            XmlUtils.beginDocument(parser, "resources");
            boolean hasPushNext = false;
            String item;

            while (true) {
                if (!hasPushNext) {
                    XmlUtils.nextElement(parser);
                }
                item = parser.getName();

                if ("condition".equals(item)) {
                    int code = 3200;
                    String strCode = parser.getAttributeValue(null, "code");
                    if (!TextUtils.isEmpty(strCode)) {
                        code = Integer.parseInt(strCode);
                    }
                    String key = parser.getAttributeValue(null, "key");
                    String displayName = parser.getAttributeValue(null, "displayName");
                    String strType = parser.getAttributeValue(null, "type");
                    int type = -1;
                    if (!TextUtils.isEmpty(strType)) {
                        type = Integer.parseInt(strType);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "code = " + code);
                        Log.d(TAG, "key = " + key);
                        Log.d(TAG, "displayName = " + displayName);
                        Log.d(TAG, "type = " + type);
                    }
                    Condition con = new Condition(code, key, displayName, type);
                    mAllConditions.put(con.mCode, con);
                } else {
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got exception parsing XmlPullParserException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Got exception parsing IOException: " + e.getMessage());
        }
    }

    private void clearCondition() {
        mAllConditions.clear();
    }

    public void reload() {
        clearCondition();
        loadConditionsFromXml();
    }

    // public int getConditionType() {
    // String condition = Utils.getCondition(mContext);
    // if (!TextUtils.isEmpty(condition)) {
    // Condition con = mAllConditions.get(condition.toLowerCase());
    // if (con != null) {
    // return con.mType;
    // } else if
    // (condition.equals(mContext.getResources().getString(R.string.no_info))) {
    // return CONDITION_TYPE_NONE;
    // }
    // }
    // return CONDITION_TYPE_SUN;
    // }

    public int getConditionType(String code) {
        try {
            Condition con = mAllConditions.get(Integer.parseInt(code));
            if (con != null) {
                return con.mType;
            } else if ("3200".equals(code)) {
                return CONDITION_TYPE_NONE;
            }
        } catch (Exception e) {
            Log.e(TAG, "get condition type, code is error");
        }
        return CONDITION_TYPE_NONE;
    }
    
    public String getConditionDisplayName(String code) {
        if (DEBUG)
            Log.d(TAG, "get condition display name, condition: " + code);
        if (TextUtils.isEmpty(code)) {
            code = "3200";
        }
        Condition con = mAllConditions.get(Integer.parseInt(code));
        if (con != null) {
            return con.mDisplayName;
        } else {
            if (DEBUG) {
                Log.d(TAG, "get condition display name, condition not found");
            }
            return "";
        }
    }

    private class Condition {
        int mCode;
        String mkey;
        String mDisplayName;
        int mType;

        public Condition(int code, String key, String displayName, int type) {
            mCode = code;
            mkey = key;
            mDisplayName = displayName;
            mType = type;
        }
    }
}
