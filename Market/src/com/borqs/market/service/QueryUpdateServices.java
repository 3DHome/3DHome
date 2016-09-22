package com.borqs.market.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.IntentUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QueryUpdateServices extends IntentService {
    private static final String TAG = QueryUpdateServices.class.getSimpleName();
    
    public static final String SHARE_PREFERENCES_QUERY_UPDATE = "query_update";
    public static final String SP_EXTRAS_THEME_UPDATE_TIME = "theme_update_time";
    public static final String SP_EXTRAS_OBJECT_UPDATE_TIME = "object_update_time";
    public static final String SP_EXTRAS_WALLPAPER_UPDATE_TIME = "wallpaper_update_time";
    public static final String SP_EXTRAS_ERROR_COUNT = "SP_EXTRAS_ERROR_COUNT";
    
    private final static long WEEK = 7 * 24 * 60 * 60 * 1000L;
    
    private ApiUtil mApiUtil = null;
//    private int nErrorCount = 0;
    
    public QueryUpdateServices() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getCommentList();
    }
    
    
    private void rescheduleSync(boolean force, boolean nextWeek) {
        int nErrorCount = 0;
        long delaytime = 0L;
        
        if(force || nextWeek) {
            setErrorCount(0);
        }else {
            nErrorCount = getErrorCount();
        }
        if(nErrorCount >= 10) {
            nErrorCount = 0;
            setErrorCount(0);
        }
        if(nextWeek) {
            delaytime = WEEK;
        }else {
            delaytime = 60 * 1000L * ((int)Math.pow(2, nErrorCount) - 1);
        }
        
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        long current_time = System.currentTimeMillis();
        long nexttime = current_time + delaytime;

        Intent intent = new Intent(getApplicationContext(), QueryUpdateServices.class);
        PendingIntent phonebookpi = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.RTC, nexttime, phonebookpi);
    }
    
    private boolean isLoading;
    private final Object mLocked = new Object();

    private void getCommentList() {
        if (isLoading) {
            return;
        }
        
        if (!DataConnectionUtils.testValidConnection(this.getApplicationContext())) {
            setErrorCount(getErrorCount() + 1);
            rescheduleSync(false, false);
            return;
        }
        
        synchronized (mLocked) {
            isLoading = true;
        }
        
        if(mApiUtil == null) {
            mApiUtil = new ApiUtil();
        }
        mApiUtil.getNewest(getApplicationContext(), new RequestListener() {
            
            @Override
            public void onIOException(IOException e) {
                synchronized (mLocked) {
                    isLoading = false;
                }
                setErrorCount(getErrorCount() + 1);
                rescheduleSync(false, false);
            }
            
            @Override
            public void onError(WutongException e) {
                synchronized (mLocked) {
                    isLoading = false;
                }
                setErrorCount(getErrorCount() + 1);
                rescheduleSync(false, false);
            }
            
            @Override
            public void onComplete(String response) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    if (!obj.has("data"))
                        return;
                    JSONObject data = obj.optJSONObject("data");
                    if(data != null) {
                        long objectUpdateTime = data.optLong(ProductType.OBJECT);
                        long themeUpdateTime = data.optLong(ProductType.THEME);
                        long wallpaperUpdateTime = data.optLong(ProductType.WALL_PAPER);
                        
                        
                        long oldThemeTime = getSharePreferences().getLong(SP_EXTRAS_THEME_UPDATE_TIME, 0);
                        long oldObjectTime = getSharePreferences().getLong(SP_EXTRAS_OBJECT_UPDATE_TIME, 0);
                        long oldWallPaperTime = getSharePreferences().getLong(SP_EXTRAS_WALLPAPER_UPDATE_TIME, 0);
                        
                        boolean theme = themeUpdateTime > oldThemeTime;
                        boolean object = objectUpdateTime > oldObjectTime;
                        boolean wallpaper = wallpaperUpdateTime > oldWallPaperTime;
                        
                        if(theme || object || wallpaper) {
                            SharedPreferences.Editor editor = getSharePreferences().edit();
                            editor.putLong(SP_EXTRAS_THEME_UPDATE_TIME, themeUpdateTime);
                            editor.putLong(SP_EXTRAS_OBJECT_UPDATE_TIME, objectUpdateTime);
                            editor.putLong(SP_EXTRAS_WALLPAPER_UPDATE_TIME, wallpaperUpdateTime);
                            editor.commit();
                            
                            IntentUtil.sendBroadCastProductHasUpdate(getApplicationContext(), object, theme, wallpaper);
                        }
                        
                    }
                    rescheduleSync(false, true);
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                synchronized (mLocked) {
                    isLoading = false;
                }
            }
        });
    }
    
    private SharedPreferences getSharePreferences() {
        return getApplicationContext().getSharedPreferences(SHARE_PREFERENCES_QUERY_UPDATE, Context.MODE_PRIVATE);
    }
    private void setErrorCount(int count) {
        SharedPreferences.Editor Editor = getSharePreferences().edit();
        Editor.putInt(SP_EXTRAS_ERROR_COUNT, count);
        Editor.commit();
    }
    
    private int getErrorCount() {
        SharedPreferences sp = getSharePreferences();
        return sp.getInt(SP_EXTRAS_ERROR_COUNT, 0);
    }

}
