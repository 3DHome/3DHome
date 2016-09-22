package com.borqs.borqsweather.weather;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import com.borqs.borqsweather.weather.yahoo.WeatherPreferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class WeatherController {
    private static final String TAG = "Weather_Controller";
    public static boolean DEBUG = false;
    public static final String INTENT_ACTION_WEATHER_UPDATE = "3dhome.action.intent.UPDATE_WEATHER";

    enum StateType {
        IDLE, LOCATION, WOEID, WEATHER
    }

    public static final int EVENT_START_REQUEST = 1;
    public static final int EVENT_REQUEST_LOCATION_SUCCESS = EVENT_START_REQUEST + 1;
    public static final int EVENT_REQUEST_WOEID_SUCCESS = EVENT_REQUEST_LOCATION_SUCCESS + 1;
    public static final int EVENT_REQUEST_WEATHER_SUCCESS = EVENT_REQUEST_WOEID_SUCCESS + 1;
    public static final int EVENT_REQUEST_FAILE = EVENT_REQUEST_WEATHER_SUCCESS + 1;

    public static final int RESULT_NONE = -1;
    public static final int RESULT_ERROR_LOCATION = RESULT_NONE + 1;
    public static final int RESULT_ERROR_GET_WEATHER = RESULT_ERROR_LOCATION + 1;
    public static final int RESULT_ERROR_WEATHER_INFO = RESULT_ERROR_GET_WEATHER + 1;
    public static final int RESULT_ERROR_NETWORK = RESULT_ERROR_WEATHER_INFO + 1;
    public static final int RESULT_ERROR_SHOULD_OPEN_GPS = RESULT_ERROR_NETWORK + 1;

    public static final int RESULT_SUCCESS = RESULT_ERROR_SHOULD_OPEN_GPS + 1;
    

    private StateType mCurrentState;
    private HashMap<StateType, State> mStateMap;

    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;

    private boolean mIsAutoLocate;
    private boolean mIsStoped;
    private Context mContext;
    private long mRequestTime;

    public WeatherController(Context context) {
        File file = new File(Utils.get3DHomePath() + File.separator + ".debug");
        if (file.exists()) {
            DEBUG = true;
        }
        mContext = context;
        mCurrentState = StateType.IDLE;
        mHandlerThread = new HandlerThread("Weather_request_" + System.currentTimeMillis());
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());
        initStateMap();
    }
    /**
     * 一：假如是自动获取位置流程: 获取经纬度->通过经纬度获取城市->通过城市获取WOEID->通过WIEID获取天气
     * 二：假如非自动获取天气流程: 通过WIEID获取天气
     */
    public void request() {
        mRequestTime = System.currentTimeMillis();
        if (!Utils.checkNetworkIsAvailable(mContext)) {
            if (DEBUG)
                Log.d(TAG, "######begin request weather failed due to network is not available#######");
            Message msg = Message.obtain(mEventHandler, WeatherController.EVENT_REQUEST_FAILE,
                    WeatherController.RESULT_ERROR_NETWORK);
            mEventHandler.sendMessage(msg);
            return;
        }
        Utils.printToLogFile("开始申请位置和天气");
        mIsStoped = false;
        mIsAutoLocate = WeatherSettings.isAutoLocation(mContext);
        if (DEBUG)
            Log.d(TAG, "######begin request weather####### is auto locate = " + mIsAutoLocate);
        if (mIsAutoLocate) {
            // 假如手机不是在中国，那么要求打开GPS，因为国外不支持百度SDK
            if ((460 != Utils.getCountryCode(mContext)) && !Utils.isLocationServicesEnabled(mContext)) {
                Message msg = Message.obtain(mEventHandler, WeatherController.EVENT_REQUEST_FAILE,
                        WeatherController.RESULT_ERROR_SHOULD_OPEN_GPS);
                mEventHandler.sendMessage(msg);
                Utils.printToLogFile("手机不在中国通知用户打开GPS");
                return;
            } else {
                Utils.printToLogFile("手机在中国或者已经打开了GPS");
                transferToTargetState(StateType.LOCATION);
                mEventHandler.sendEmptyMessage(EVENT_START_REQUEST);
            }
        } else {
            Utils.printToLogFile("已经开启手动设置城市,直接通过城市Woeid获取天气");
            String woeid = WeatherPreferences.getInstance(mContext).getWoeid();
            String localcountry = WeatherPreferences.getInstance(mContext).getLocationCountryName();
            String localProvicne = WeatherPreferences.getInstance(mContext).getLocationProvinceName();
            String localCity = WeatherPreferences.getInstance(mContext).getLocationCityName();
            String latitude = WeatherPreferences.getInstance(mContext).getLocationLatitude();
            String longitude = WeatherPreferences.getInstance(mContext).getLocationLongitude();
            if (TextUtils.isEmpty(woeid)) {
                Utils.printToLogFile("城市Woeid为空，切换到自动获取位置和天气");
                transferToTargetState(StateType.LOCATION);
                mEventHandler.sendEmptyMessage(EVENT_START_REQUEST);
                return;
            }
            transferToTargetState(StateType.WEATHER);
            Bundle bundle = new Bundle();
            bundle.putString(State.BUNDLE_COUNTRY, localcountry);
            bundle.putString(State.BUNDLE_PROVINCE, localProvicne);
            bundle.putString(State.BUNDLE_CITY, localCity);
            bundle.putString(State.BUNDLE_WOEID, woeid);
            bundle.putString(State.BUNDLE_LATITUDE, latitude);
            bundle.putString(State.BUNDLE_LONGITUDE, longitude);
            Message msg = mEventHandler.obtainMessage(EVENT_START_REQUEST);
            msg.setData(bundle);
            mEventHandler.sendMessage(msg);
            Utils.printToLogFile("城市名：" + localCity + ", woeid = " + woeid);
        }

    }

    /**
     * 直接通过WOEID申请天气
     */
    public void request(Bundle bundle) {
        mRequestTime = System.currentTimeMillis();
        if (DEBUG)
            Log.d(TAG, "######begin request weather with woeid #######");
        if (!Utils.checkNetworkIsAvailable(mContext)) {
            if (DEBUG)
                Log.d(TAG, "######begin request weather with woeid failed due to network is not available#######");
            Message msg = Message.obtain(mEventHandler, WeatherController.EVENT_REQUEST_FAILE,
                    WeatherController.RESULT_ERROR_NETWORK);
            mEventHandler.sendMessage(msg);
            Utils.printToLogFile("手动申请天气失败：无网路链接");
            return;
        }
        Utils.printToLogFile("开始手动申请天气");
        mIsStoped = false;
        transferToTargetState(StateType.WEATHER);
        Message msg = mEventHandler.obtainMessage(EVENT_START_REQUEST);
        msg.setData(bundle);
        mEventHandler.sendMessage(msg);
    }

    StateType getCurrentStateType() {
        return mCurrentState;
    }

    public Context getContext() {
        return mContext;
    }

    public EventHandler getEventHandler() {
        return mEventHandler;
    }

    public boolean isAutoLocate() {
        return mIsAutoLocate;
    }

    public boolean isControllerRunning() {
        return !(mCurrentState == StateType.IDLE || Math.abs(System.currentTimeMillis() - mRequestTime) > 1000 * 60 * 10);
    }

    public void stopController() {
        mIsStoped = true;
        if (mStateMap != null) {
            Iterator<State> states = mStateMap.values().iterator();
            while (states.hasNext()) {
                State state = states.next();
                state.stop();
            }
            mStateMap.clear();
            mEventHandler.removeCallbacksAndMessages(null);
            // Quit event loop to avoid thread leak
            mHandlerThread.quit();
        }
    }

    public boolean isStop() {
        return mIsStoped;
    }

    private void initStateMap() {
        mStateMap = new HashMap<StateType, State>();
        mStateMap.put(StateType.IDLE, new Idle(this));
        mStateMap.put(StateType.LOCATION, new LocationState(this));
        mStateMap.put(StateType.WOEID, new WoeidAndCityState(this));
        mStateMap.put(StateType.WEATHER, new WeatherState(this));
    }

    private void transferToNextState() {
        switch (mCurrentState) {
        case IDLE:
            mCurrentState = StateType.LOCATION;
            break;
        case LOCATION:
            mCurrentState = StateType.WOEID;
            break;
        case WOEID:
            mCurrentState = StateType.WEATHER;
            break;
        case WEATHER:
            mCurrentState = StateType.IDLE;
            break;
        default:
            break;
        }
    }

    private void transferToTargetState(StateType targetState) {
        mCurrentState = targetState;
    }

    private void transferToIdleState() {
        mCurrentState = StateType.IDLE;
    }

    private State getState(StateType type) {
        switch (type) {
        case IDLE:
            return mStateMap.get(StateType.IDLE);

        case LOCATION:
            return mStateMap.get(StateType.LOCATION);

        case WOEID:
            return mStateMap.get(StateType.WOEID);

        case WEATHER:
            return mStateMap.get(StateType.WEATHER);

        default:
            return null;
        }
    }

    private void sendResult(int resultCode) {
        if (mIsStoped) {
            return;
        }
        Intent result = new Intent(INTENT_ACTION_WEATHER_UPDATE);
        result.putExtra("state", resultCode);
        mContext.sendBroadcast(result);
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            State state;
            if (mIsStoped) {
                if (DEBUG)
                    Log.i(TAG, "stop to request weather");
                return;
            }
            Bundle data = msg.getData();
            switch (msg.what) {
            case EVENT_START_REQUEST:
                state = getState(mCurrentState);
                if (state != null) {
                    state.run(data);
                }
                break;
            case EVENT_REQUEST_LOCATION_SUCCESS:
                transferToNextState();
                state = getState(mCurrentState);
                if (state != null) {
                    state.run(data);
                }
                break;
            case EVENT_REQUEST_WOEID_SUCCESS:
                transferToNextState();
                state = getState(mCurrentState);
                if (state != null) {
                    state.run(data);
                }
                break;
            case EVENT_REQUEST_WEATHER_SUCCESS:
                sendResult(RESULT_SUCCESS);
                transferToIdleState();
                break;
            case EVENT_REQUEST_FAILE:
                int resultCode = (Integer) msg.obj;
                sendResult(resultCode);
                transferToIdleState();
                break;
            default:
                break;
            }
        }

    }

    class Idle extends State {
        public Idle(WeatherController controller) {
            super(controller);
        }

        @Override
        void run(Bundle data) {
            // TODO Auto-generated method stub

        }

        @Override
        void stop() {
            // TODO Auto-generated method stub

        }

        @Override
        void onSuccess(Bundle data) {
            // TODO Auto-generated method stub

        }

        @Override
        void onFailed(Bundle data) {
            // TODO Auto-generated method stub

        }
    }
}
