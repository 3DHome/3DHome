package com.borqs.borqsweather.weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
//import android.telephony.CellLocation;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

import com.borqs.borqsweather.R;
import com.borqs.borqsweather.WeatherApplication;
import com.borqs.borqsweather.weather.yahoo.WeatherInfo;
import com.borqs.borqsweather.weather.yahoo.WeatherPreferences;

public class LoopForInfoService {
    // below string used for communicated with 3DHome begin
    public static final String UPDATE_WEATHER = "weather.action.intent.UPDATE";
    // end

    public static final String TAG = "Weather_LoopForInfoService";

    public static final String INTENT_ACTION_WEATHER_LOOP_UPDATE = "3dhome.action.intent.WEATHER_LOOP_UPDATE";
    public static final String INTENT_ACTION_WEATHER_DAY_NIGHT_CHANGED = "3dhome.action.intent.WEATHER_DAY_NIGHT_CHANGED";
    public int[] mSunriseTime;
    public int[] mSunsetTime;

    private AlarmManager mAlarmMgr;
    private boolean mIsNight;
    private WeatherConditions mConditions;
    private WeatherPreferences mPreferences;
    private long mLastWeatherUpdateTime = 0;

    private WeatherController mController;

    private GLSObserver mGLSObserver;
//    private CellStateListener mCellStateListener;

    private static LoopForInfoService mServiceInstance;
    private Context mContext;
    private boolean mHasPopupGPSSetting;

    private LoopForInfoService(Context context) {
        mContext = context;
        mHasPopupGPSSetting = false;
        mConditions = WeatherConditions.getInstance(mContext);
        mPreferences = WeatherPreferences.getInstance(mContext);
        mLastWeatherUpdateTime = mPreferences.getWeatherUpdateTimeLong();
        updateSunriseSunsetTime();
        mIsNight = isNight();
        updateDayNightChangedAlarm();
        WeatherApplication.getInstance().init(mContext);
        registerFilter();
        if (mGLSObserver == null) {
            mGLSObserver = new GLSObserver(new Handler(), mContext);
            mGLSObserver.startObserving();
        }
//        if (mCellStateListener == null) {
//            mCellStateListener = new CellStateListener();
//            TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//            manager.listen(mCellStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
//        }
        setAlarm();
    }

    public static LoopForInfoService getInstance(Context context) {
        if (mServiceInstance == null) {
            mServiceInstance = new LoopForInfoService(context);
        }
        return mServiceInstance;
    }

    private void registerFilter() {
        IntentFilter filter = new IntentFilter(INTENT_ACTION_WEATHER_LOOP_UPDATE);
        filter.addAction(INTENT_ACTION_WEATHER_DAY_NIGHT_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(WeatherController.INTENT_ACTION_WEATHER_UPDATE);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (INTENT_ACTION_WEATHER_LOOP_UPDATE.equals(action)) {
                if (WeatherController.DEBUG)
                    Log.d(TAG, "#### alarm arrival and begin request weather and set next alarm #####");
                requestWeather();
                setAlarm();
            } else if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                saveUpdateTime(-1);
                setAlarm();
                if (mIsNight != isNight()) {
                    mIsNight = !mIsNight;
                    onWeatherUpdate();
                }
                updateDayNightChangedAlarm();
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                mConditions.reload();
                onWeatherUpdate();
            } else if (WeatherController.INTENT_ACTION_WEATHER_UPDATE.equals(action)) {
                int resultCode = intent.getIntExtra(String.valueOf("state"), -1);
                if (resultCode >= 0) {
                    if (WeatherController.RESULT_SUCCESS == resultCode) {
                        saveUpdateTime(System.currentTimeMillis());
                    } else {
                        saveUpdateTime(-1);
                    }
                    updateSunriseSunsetTime();
                    WeatherInfo info = getWeather();
                    if (checkWeatherInfoIsOld(info)) {
                        Log.e(TAG, "weather info is out of date");
                        mPreferences.clearWeatherInfo();
                    }
                    onWeatherUpdate();
                    updateDayNightChangedAlarm();
                }
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                if (WeatherController.DEBUG)
                    Log.d(TAG, "### connectivity change and try to request weather ###");
                if (Utils.checkNetworkIsAvailable(mContext)) {
                    checkToUpdateWeather(1000 * 60 * 30);
                }
            } else if (INTENT_ACTION_WEATHER_DAY_NIGHT_CHANGED.equals(action)) {
                if (WeatherController.DEBUG)
                    Log.d(TAG, "#### day/night changed alarm arrivaled ###");
                onWeatherUpdate();
                updateDayNightChangedAlarm();
            }
        }
    };

    public void setAlarm() {
        removeAlarm();
        if (WeatherSettings.isAutoUpdate(mContext)) {
            long time = calculateNextAlarm();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);    
            if (mAlarmMgr == null) {
                mAlarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            }
            Intent intent = new Intent(INTENT_ACTION_WEATHER_LOOP_UPDATE);
            PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmMgr.set(AlarmManager.RTC_WAKEUP, time, sender);
            if (WeatherController.DEBUG) {
                Log.d(TAG, "#######set alarm #######: " + DateFormat.format("MM/dd/yy kk:mm:ss", c));
            }
        }
    }

    public void removeAlarm() {
        if (mAlarmMgr == null) {
            mAlarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        if (WeatherController.DEBUG)
            Log.d(TAG, "#########remove alarm############");
        Intent intent = new Intent(INTENT_ACTION_WEATHER_LOOP_UPDATE);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmMgr.cancel(sender);
    }

    /**
     * 计算下一个闹钟的时间：
     */
    private long calculateNextAlarm() {
        long nextTime = -1;

        long nowTimeInMillis = System.currentTimeMillis();
        //闹钟有可能会提前几秒钟到达，所以判断时间往后延长5分钟
        nowTimeInMillis = nowTimeInMillis + 1000 * 60 * 5;
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowTimeInMillis);

        int[] start =  WeatherSettings.getTime(mContext, true);
        int startHour = start[0];
        int startMinute = start[1];
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(nowTimeInMillis);
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, startMinute);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startTime = startCal.getTimeInMillis();

        int[] end = WeatherSettings.getTime(mContext, false);
        int endHour = end[0];
        int endMinute = end[1];
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(nowTimeInMillis);
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, endMinute);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        long endTime = endCal.getTimeInMillis();

        long intervalTime = WeatherSettings.getCurrentIntervalValue(mContext);
        if (nowTimeInMillis <= startTime) {
            nextTime = startTime;
        } else if (nowTimeInMillis >= endTime) {
            startCal.add(Calendar.DAY_OF_YEAR, 1);
            nextTime = startCal.getTimeInMillis();
        } else {
            nextTime = endTime;
            for (long time = startTime; time <= endTime;) {
                if (nowTimeInMillis >= time && nowTimeInMillis <= (time + intervalTime)) {
                    nextTime = time + intervalTime;
                    break;
                }
                time = time + intervalTime;
            }
        }
       
        return nextTime;
    }

    public void requestWeather() {
        if (mController != null) {
            if (mController.isControllerRunning()) {
                if (WeatherController.DEBUG) {
                    Log.d(TAG, "###begin request weather but weather thread is on runing###");
                }
                return;
            } else {
                mController.stopController();
            }
        }
        if (WeatherController.DEBUG) {
            Log.d(TAG, "###begin request weather ###");
        }
        mController = new WeatherController(mContext);
        mController.request();
    }

    public void requestWeatherByWoeid(Bundle bundle) {
        if (mController != null) {
            mController.stopController();
        }
        mController = new WeatherController(mContext);
        mController.request(bundle);
    }

    public void checkToUpdateWeather(long interval) {
        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = mLastWeatherUpdateTime;
        if (WeatherController.DEBUG) {
            Log.d(TAG, "###try to request weather ###currentTime = " + currentTime + ", lastUpdateTime = "
                    + lastUpdateTime + ", interval = " + interval);
        }
        if (Math.abs((currentTime - lastUpdateTime)) > interval) {
            requestWeather();
        }
    }

    public boolean checkWeatherInfoIsOld(WeatherInfo info) {
        if (info == null || !WeatherSettings.isAutoLocation(mContext)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = (Date) sdf.parseObject(info.getDate());
            if (date != null) {
                if (!Utils.isInvalidWeather(System.currentTimeMillis(), date.getTime())) {
                    return true;
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "check weather date : " + e.getMessage());
        }
        return false;
    }

    public WeatherInfo getWeather() {
        return mPreferences.getWeatherInfo();
    }

    public void onWeatherUpdate() {
        Intent intent = new Intent("com.borqs.borqsweather.update");
        mContext.sendBroadcast(intent);
    }

    public boolean isNight() {
        int riseHour = mSunriseTime[0];
        int riseMin = mSunriseTime[1];
        int setHour = mSunsetTime[0];
        int setMin = mSunsetTime[1];
        Time time = new Time();
        time.setToNow();
        int hour = time.hour;
        int min = time.minute;
        if ((hour > setHour || hour < riseHour) || (hour == riseHour && min < riseMin)
                || (hour == setHour && min >= setMin)) {
            return true;
        } else {
            return false;
        }
    }

    public void saveUpdateTime(long time) {
        mLastWeatherUpdateTime = time;
    }


    /**
     * Get weather type according to the weather code.
     * 
     * @return
     */
    public int getConditionType() {
        String code = mPreferences.getWeatherInfo().getCode();
        int type = mConditions.getConditionType(code);
        return type;
    }

    /**
     * Get weather condition string according to the weather code.
     * 
     * @return
     */
    public String getDisplayCondition() {
        String code = mPreferences.getWeatherInfo().getCode();
        String condition = mConditions.getConditionDisplayName(code);
        return condition;
    }

    public String getDisplayWindCondition() {
        String windDirection = mPreferences.getWeatherInfo().getWindDirection();
        if (TextUtils.isEmpty(windDirection)) {
            return null;
        }

        // weather source should be China meterological administration
        if (!TextUtils.isDigitsOnly(windDirection)) {
            return windDirection;
        }

        /**
         * There are 8/16 wind point, here i just use 8 wind point. 0-360,
         * divide 360 degree by 8 directions. 0 degree is a special value which
         * means variable wind sometimes
         */
        int windDirectionValue = Integer.valueOf(windDirection);
        String windDirectionString = null;
        if (windDirectionValue == 0) {
            windDirectionString = mContext.getString(R.string.wind_var);
        } else if (windDirectionValue < 22 || windDirectionValue >= 337) {
            windDirectionString = mContext.getString(R.string.wind_n);
        } else if (windDirectionValue < 67) {
            windDirectionString = mContext.getString(R.string.wind_ne);
        } else if (windDirectionValue < 112) {
            windDirectionString = mContext.getString(R.string.wind_e);
        } else if (windDirectionValue < 157) {
            windDirectionString = mContext.getString(R.string.wind_se);
        } else if (windDirectionValue < 202) {
            windDirectionString = mContext.getString(R.string.wind_s);
        } else if (windDirectionValue < 247) {
            windDirectionString = mContext.getString(R.string.wind_sw);
        } else if (windDirectionValue < 192) {
            windDirectionString = mContext.getString(R.string.wind_w);
        } else {
            windDirectionString = mContext.getString(R.string.wind_nw);
        }

        String windSpeed = mPreferences.getWeatherInfo().getWindSpeed();
        float windSpeedValue = 0f;
        // It is just handle the impossible exception, speed should be valid
        // value
        if (TextUtils.isEmpty(windSpeed)) {
            return windDirectionString;
        }
        try {
            windSpeedValue = Float.valueOf(windSpeed);
        } catch (NumberFormatException e) {
            return windDirectionString;
        }

        // the wind speed follow spec, 0 kph means no wind.
        if (windSpeedValue == 0) {
            return null;
        } else if (windSpeedValue < 12) {
            return mContext.getString(R.string.wind_condition_1, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 20) {
            return mContext.getString(R.string.wind_condition_3, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 29) {
            return mContext.getString(R.string.wind_condition_4, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 39) {
            return mContext.getString(R.string.wind_condition_5, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 50) {
            return mContext.getString(R.string.wind_condition_6, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 62) {
            return mContext.getString(R.string.wind_condition_7, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 75) {
            return mContext.getString(R.string.wind_condition_8, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 89) {
            return mContext.getString(R.string.wind_condition_9, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 103) {
            return mContext.getString(R.string.wind_condition_10, windDirectionString, windSpeedValue);
        } else if (windSpeedValue < 118) {
            return mContext.getString(R.string.wind_condition_11, windDirectionString, windSpeedValue);
        } else {
            return mContext.getString(R.string.wind_condition_hurricane, windDirectionString, windSpeedValue);
        }
    }

    /**
     * 提示用户开启GPS设置
     */
    public boolean checkLocationServices() {
        if (WeatherSettings.isAutoLocation(mContext) && ((460 != Utils.getCountryCode(mContext)))
                && !Utils.isLocationServicesEnabled(mContext) && !mHasPopupGPSSetting) {
            mHasPopupGPSSetting = true;
            Intent intent = new Intent(mContext, CheckLocServiceDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }

    class GLSObserver extends ContentObserver {

        private Context mContext;

        GLSObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED),
                    false, this);
        }

        void stopObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (WeatherController.DEBUG)
                Log.i(TAG, "### GPS status changed ###");
            if (WeatherSettings.isAutoLocation(mContext)) {
                checkToUpdateWeather(1000 * 60 * 5);
            }
        }
    }

//    class CellStateListener extends PhoneStateListener {
//        private int mLastCid = -1;
//        private int mLastLac = -1;
//
//        public void onCellLocationChanged(CellLocation location) {
//            if (location != null && (location instanceof GsmCellLocation)) {
//                GsmCellLocation gsmL = (GsmCellLocation) location;
//                
//                int cid = gsmL.getCid();
//                int lac = gsmL.getLac();
//                if (cid != mLastCid || lac != mLastLac) {
//                    mLastCid = cid;
//                    mLastLac = lac;
//                    if (WeatherController.DEBUG)
//                        Log.i(TAG, "### cell location changed ###cid = " + cid + ", lac = " + lac);
//                    if (WeatherSettings.isAutoLocation(mContext)) {
//                        checkToUpdateWeather(1000 * 60 * 30);
//                    }
//                }
//            }
//            super.onCellLocationChanged(location);
//        }
//    }
    
  
    /**
     * 设置白天黑夜更替闹钟 一：假如当前时间小于太阳升起或者大于太阳降落的时间，那么设置太阳升起的时间为闹钟时间；
     * 一：假如当前时间小于太阳降落的时间，那么设置太阳降落的时间为闹钟时间
     */
    private void updateDayNightChangedAlarm() {
        removeDayNightAlarm();
        if (mAlarmMgr == null) {
            mAlarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        Intent intent = new Intent(INTENT_ACTION_WEATHER_DAY_NIGHT_CHANGED);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int nowHour = cal.get(Calendar.HOUR_OF_DAY);
        int nowMin = cal.get(Calendar.MINUTE);
        //闹钟有可能会提前几秒钟到达，所以判断时间往后延长5分钟
        nowMin = nowMin + 5;
        if (nowHour < mSunriseTime[0] || ((nowHour == mSunriseTime[0]) && (nowMin < mSunriseTime[1]))) {
            cal.set(Calendar.HOUR_OF_DAY, mSunriseTime[0]);
            cal.set(Calendar.MINUTE, mSunriseTime[1]);
            cal.set(Calendar.SECOND, 0);
        } else if (nowHour < mSunsetTime[0] || ((nowHour == mSunsetTime[0]) && (nowMin < mSunsetTime[1]))) {
            cal.set(Calendar.HOUR_OF_DAY, mSunsetTime[0]);
            cal.set(Calendar.MINUTE, mSunsetTime[1]);
            cal.set(Calendar.SECOND, 0);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, mSunriseTime[0]);
            cal.set(Calendar.MINUTE, mSunriseTime[1]);
            cal.set(Calendar.SECOND, 0);
        }
        if (WeatherController.DEBUG) {
            Log.d(TAG, "#######set day/night switch alarm #######: " + DateFormat.format("MM/dd/yy kk:mm:ss", cal));
        }
        mAlarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }

    private void removeDayNightAlarm() {
        if (mAlarmMgr == null) {
            mAlarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        if (WeatherController.DEBUG)
            Log.d(TAG, "#########removeday/night switch alarm############");
        Intent intent = new Intent(INTENT_ACTION_WEATHER_DAY_NIGHT_CHANGED);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmMgr.cancel(sender);
    }

    /**
     * 从cache中获取太阳升起和落下的时间， 第一位为小时，第二位为分钟
     */
    private void updateSunriseSunsetTime() {
        if (mSunriseTime == null) {
            mSunriseTime = new int[] { 6, 0 };
        }
        if (mSunsetTime == null) {
            mSunsetTime = new int[] { 19, 0 };
        }
        String sunset = mPreferences.getSunsetTime();
        if (sunset != null) {
            String[] setTime = sunset.split(":");
            try {
                mSunsetTime[0] = Integer.parseInt(setTime[0]);
                mSunsetTime[1] = Integer.parseInt(setTime[1]);
            } catch (Exception e) {
                Log.e(TAG, "sun set error: " + sunset);
            }
        }
        String sunrise = mPreferences.getSunriseTime();
        if (sunrise != null) {
            String[] riseTime = sunrise.split(":");
            try {
                mSunriseTime[0] = Integer.parseInt(riseTime[0]);
                mSunriseTime[1] = Integer.parseInt(riseTime[1]);
            } catch (Exception e) {
                Log.e(TAG, "sun rise error: " + sunrise);
            }
        }
    }

}
