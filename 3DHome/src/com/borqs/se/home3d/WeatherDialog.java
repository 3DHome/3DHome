package com.borqs.se.home3d;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import com.borqs.borqsweather.weather.CheckLocServiceDialogActivity;
import com.borqs.borqsweather.weather.IWeatherService;
import com.borqs.borqsweather.weather.yahoo.ForecastWeatherInfo;
import com.borqs.borqsweather.weather.yahoo.WeatherInfo;
import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeatherDialog extends Dialog{
    private MyLayout mMyLayout;
    private Context mContext;
    private int mScreenW;
    private View mWeather;
    private View mForeWeather;

    private IWeatherService mService; 
    private WeatherInfo mWeather0;// today
    private ForecastWeatherInfo mWeather1 = null;
    private ForecastWeatherInfo mWeather2 = null;
    private ForecastWeatherInfo mWeather3 = null;
    private ForecastWeatherInfo mWeather4 = null;

    // current weather
    private TextView mCurAddr;
    private TextView mCurDate;
    private ImageView mFuImg;
    private ImageView mCurTempImg3;
    private ImageView mCurTempImg2;
    private ImageView mCurTempImg1;
    private ImageView mCurTempDu;
    private TextView mCurTemrange;
    private TextView mCurCondition;
    private ImageView mSettingsBtn;

    //forecast weather
    private TextView mWeatherWeek1;
    private TextView mWeatherWeek2;
    private TextView mWeatherWeek3;
    private TextView mWeatherWeek4;
    private ImageView mWeatherCondition1;
    private ImageView mWeatherCondition2;
    private ImageView mWeatherCondition3;
    private ImageView mWeatherCondition4;
    private TextView mWeatherTempHigh1;
    private TextView mWeatherTempHigh2;
    private TextView mWeatherTempHigh3;
    private TextView mWeatherTempHigh4;
    private TextView mWeatherTempLow1;
    private TextView mWeatherTempLow2;
    private TextView mWeatherTempLow3;
    private TextView mWeatherTempLow4;

    private TextView mNoInfo;

    private long mCurTime;
    private String[] mWeek = new String[5];

    private boolean mIsWeatherShown = true;

    private static final String WEATHER_PREFERENCE = "weather_preference";

    public WeatherDialog(Context context,IWeatherService service) {
        super(context,R.style.WeatherDialogStyle);        
        Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams p = win.getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.FILL_PARENT;
        win.setAttributes(p);
        win.setGravity(Gravity.TOP);
        win.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        mContext = context;
        mService = service;

        initWeatherInfo();

        mCurTime = System.currentTimeMillis();
        initWeekOfDate();

        mScreenW = context.getResources().getDisplayMetrics().widthPixels;
        initContentView();
    }

    private void initContentView(){
        mMyLayout = new MyLayout(mContext);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mMyLayout.setLayoutParams(layoutParams);
        setContentView(mMyLayout);
        initAndAddWeatherView();
    }

    private void initAndAddWeatherView(){
        boolean isCelsius = true;
        try {
            isCelsius = mService.isCelsius();
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }

        mWeather=View.inflate(mContext,R.layout.dialog_weather,null);  
        this.mCurAddr = (TextView)this.mWeather.findViewById(R.id.curweather_addr);
        this.mCurDate = (TextView)this.mWeather.findViewById(R.id.curweather_date);
        this.mFuImg = (ImageView)this.mWeather.findViewById(R.id.below_zero);
        this.mCurTempImg3 = (ImageView)this.mWeather.findViewById(R.id.curweather_image3);
        this.mCurTempImg2 = (ImageView)this.mWeather.findViewById(R.id.curweather_image2);
        this.mCurTempImg1 = (ImageView)this.mWeather.findViewById(R.id.curweather_image1);
        this.mCurTempDu = (ImageView)this.mWeather.findViewById(R.id.curweather_imagedu);
        this.mCurTemrange = (TextView)this.mWeather.findViewById(R.id.curweather_temp_range);
        this.mCurCondition = (TextView)this.mWeather.findViewById(R.id.curweather_condition);
        this.mNoInfo = (TextView)this.mWeather.findViewById(R.id.curweather_noinfo);
        this.mSettingsBtn = (ImageView)this.mWeather.findViewById(R.id.weather_settings_btn);
        setCurWeatherInfo(isCelsius);

        mForeWeather = View.inflate(mContext,R.layout.dialog_foreweather,null);
        this.mWeatherWeek1 = (TextView)mForeWeather.findViewById(R.id.fweather_1).findViewById(R.id.fweather_week);
        this.mWeatherCondition1 = (ImageView)mForeWeather.findViewById(R.id.fweather_1).findViewById(R.id.fweather_condition);
        this.mWeatherTempHigh1 = (TextView)mForeWeather.findViewById(R.id.fweather_1).findViewById(R.id.fweather_temp_high);
        this.mWeatherTempLow1 = (TextView)mForeWeather.findViewById(R.id.fweather_1).findViewById(R.id.fweather_temp_low);
        this.mWeatherWeek2 = (TextView)mForeWeather.findViewById(R.id.fweather_2).findViewById(R.id.fweather_week);
        this.mWeatherCondition2 = (ImageView)mForeWeather.findViewById(R.id.fweather_2).findViewById(R.id.fweather_condition);
        this.mWeatherTempHigh2 = (TextView)mForeWeather.findViewById(R.id.fweather_2).findViewById(R.id.fweather_temp_high);
        this.mWeatherTempLow2 = (TextView)mForeWeather.findViewById(R.id.fweather_2).findViewById(R.id.fweather_temp_low);
        this.mWeatherWeek3 = (TextView)mForeWeather.findViewById(R.id.fweather_3).findViewById(R.id.fweather_week);
        this.mWeatherCondition3 = (ImageView)mForeWeather.findViewById(R.id.fweather_3).findViewById(R.id.fweather_condition);
        this.mWeatherTempHigh3 = (TextView)mForeWeather.findViewById(R.id.fweather_3).findViewById(R.id.fweather_temp_high);
        this.mWeatherTempLow3 = (TextView)mForeWeather.findViewById(R.id.fweather_3).findViewById(R.id.fweather_temp_low);
        this.mWeatherWeek4 = (TextView)mForeWeather.findViewById(R.id.fweather_4).findViewById(R.id.fweather_week);
        this.mWeatherCondition4 = (ImageView)mForeWeather.findViewById(R.id.fweather_4).findViewById(R.id.fweather_condition);
        this.mWeatherTempHigh4 = (TextView)mForeWeather.findViewById(R.id.fweather_4).findViewById(R.id.fweather_temp_high);
        this.mWeatherTempLow4 = (TextView)mForeWeather.findViewById(R.id.fweather_4).findViewById(R.id.fweather_temp_low);
        setForeWeatherInfo(isCelsius);

        SharedPreferences spf = mContext.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE);
        mIsWeatherShown = spf.getBoolean("is_weather_shown", true);

        if(mIsWeatherShown){
            mMyLayout.addView(mWeather,0); 
            mMyLayout.addView(mForeWeather,1);
            mForeWeather.setTranslationX(mScreenW);
        }else{
            mMyLayout.addView(mWeather,0); 
            mMyLayout.addView(mForeWeather,1);
            mWeather.setTranslationX(-mScreenW);
        }
    }

    private void setForeWeatherInfo(boolean isCelsius){
        setForeweatherWeek();
        setForeweatherCondition();
        setForeweatherTem(isCelsius);
    }

    private void setForeweatherTem(boolean isCelsius){
        int symbolTempResId = isCelsius ? R.string.temp_symbol_c : R.string.temp_symbol_f;
        String symbolTemp = mContext.getString(symbolTempResId);
        int tempType = isCelsius ? WeatherInfo.TEMPERATURE_FMT_CELSIUS : WeatherInfo.TEMPERATURE_FMT_FAHRENHEIT;
        String tempHigh = null;
        String tempLow = null;
        ForecastWeatherInfo info = null;
        info = getForecastWeatherByDay(1);
        if (info != null) {
            tempHigh = info.getTempHigh(tempType);
        }
        if (TextUtils.isEmpty(tempHigh)) {
            tempHigh = "0";
        }
        if (info != null) {
        tempLow = info.getTempLow(tempType);
        }
        if (TextUtils.isEmpty(tempLow)) {
            tempLow = "0";
        }
        this.mWeatherTempHigh1.setText(tempHigh+symbolTemp);
        this.mWeatherTempLow1.setText("/"+tempLow+symbolTemp);

        info = getForecastWeatherByDay(2);
        if (info != null) {
            tempHigh = info.getTempHigh(tempType);
        }
        if (TextUtils.isEmpty(tempHigh)) {
            tempHigh = "0";
        }
        if (info != null) {
            tempLow = info.getTempLow(tempType);
        }
        if (TextUtils.isEmpty(tempLow)) {
            tempLow = "0";
        }
        this.mWeatherTempHigh2.setText(tempHigh + symbolTemp);
        this.mWeatherTempLow2.setText("/" + tempLow + symbolTemp);

        info = getForecastWeatherByDay(3);
        if (info != null) {
            tempHigh = info.getTempHigh(tempType);
        }
        if (TextUtils.isEmpty(tempHigh)) {
            tempHigh = "0";
        }
        if (info != null) {
            tempLow = info.getTempLow(tempType);
        }
        if (TextUtils.isEmpty(tempLow)) {
            tempLow = "0";
        }
        this.mWeatherTempHigh3.setText(tempHigh + symbolTemp);
        this.mWeatherTempLow3.setText("/" + tempLow + symbolTemp);

        info = getForecastWeatherByDay(4);
        if (info != null) {
            tempHigh = info.getTempHigh(tempType);
        }
        if (TextUtils.isEmpty(tempHigh)) {
            tempHigh = "0";
        }
        if (info != null) {
            tempLow = info.getTempLow(tempType);
        }
        if (TextUtils.isEmpty(tempLow)) {
            tempLow = "0";
        }
        this.mWeatherTempHigh4.setText(tempHigh+symbolTemp);
        this.mWeatherTempLow4.setText("/"+tempLow+symbolTemp);
    }

    private void setForeweatherCondition(){
        ForecastWeatherInfo info = null;
        String code = null;
        Bitmap bCondition = null;
        info = getForecastWeatherByDay(1);
        if (info == null) {
            code = "0";
        } else {
            code = info.getCode();
        }
        bCondition = getConditionBitmap(code);
        this.mWeatherCondition1.setBackgroundDrawable(new BitmapDrawable(bCondition));

        info = getForecastWeatherByDay(2);
        if (info == null) {
            code = "0";
        } else {
            code = info.getCode();
        }
        bCondition = getConditionBitmap(code);
        this.mWeatherCondition2.setBackgroundDrawable(new BitmapDrawable(bCondition));

        info = getForecastWeatherByDay(3);
        if (info == null) {
            code = "0";
        } else {
            code = info.getCode();
        }
        bCondition = getConditionBitmap(code);
        this.mWeatherCondition3.setBackgroundDrawable(new BitmapDrawable(bCondition));

        info = getForecastWeatherByDay(4);
        if (info == null) {
            code = "0";
        } else {
            code = info.getCode();
        }
        bCondition = getConditionBitmap(code);
        this.mWeatherCondition4.setBackgroundDrawable(new BitmapDrawable(bCondition));
    }

    private void setForeweatherWeek(){
        if(TextUtils.isEmpty(mWeek[1])){
            this.mWeatherWeek1.setText("N/A");
        }else{
            this.mWeatherWeek1.setText(mWeek[1]);
        }

        if(TextUtils.isEmpty(mWeek[2])){
            this.mWeatherWeek2.setText("N/A");
        }else{
            this.mWeatherWeek2.setText(mWeek[2]);
        }

        if(TextUtils.isEmpty(mWeek[3])){
            this.mWeatherWeek3.setText("N/A");
        }else{
            this.mWeatherWeek3.setText(mWeek[3]);
        }

        if(TextUtils.isEmpty(mWeek[4])){
            this.mWeatherWeek4.setText("N/A");
        }else{
            this.mWeatherWeek4.setText(mWeek[4]);
        }
    }
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
    private void setCurWeatherInfo(boolean isCelsius){
        boolean isNoInfo = mWeather0 == null || mWeather0.isNoData();

        String cTemp;

        int symbolTempResId = isCelsius ? R.string.temp_symbol_c : R.string.temp_symbol_f;
        String symbolTemp = mContext.getString(symbolTempResId);
        int tempType = isCelsius ? WeatherInfo.TEMPERATURE_FMT_CELSIUS : WeatherInfo.TEMPERATURE_FMT_FAHRENHEIT;

        if(isNoInfo){
            cTemp = mContext.getString(R.string.no_info);
            this.mNoInfo.setVisibility(View.VISIBLE);
            this.mCurAddr.setVisibility(View.GONE);
        }else{
            this.mCurAddr.setVisibility(View.VISIBLE);
            this.mCurAddr.setText(mWeather0.getCity());

            cTemp = mWeather0.getTemperature(tempType);

            int cTempNum = 0;
            try{
                cTempNum = Integer.parseInt(cTemp);
            } catch (NumberFormatException ne) {
                if (HomeUtils.DEBUG)
                    Log.e(HomeUtils.TAG, ne.getMessage());
            }

            Bitmap fu = null;
            if(cTempNum <0){
                fu = this.getFuBitmap();
                cTempNum = -cTempNum;
            }
            Bitmap b3 = getDigitBitmap(cTempNum,3);
            Bitmap b2 = getDigitBitmap(cTempNum,2);  
            Bitmap b1 = getDigitBitmap(cTempNum,1);
            Bitmap du = getDuBitmap(isCelsius);

            if(fu == null){
                this.mFuImg.setVisibility(View.GONE);
            }else{
                this.mFuImg.setVisibility(View.VISIBLE);
                this.mFuImg.setBackgroundDrawable(new BitmapDrawable(fu));
            }

            if(b3 == null || b2 == null || b1 == null || du == null){
                this.mCurTempImg3.setVisibility(View.GONE);
                this.mCurTempImg2.setVisibility(View.GONE);
                this.mCurTempImg1.setVisibility(View.GONE);
                this.mCurTempDu.setVisibility(View.GONE);
                this.mNoInfo.setVisibility(View.VISIBLE);
            }else{
                this.mNoInfo.setVisibility(View.GONE);

                if(getDigit(cTempNum,3)==0 ){
                    this.mCurTempImg3.setVisibility(View.GONE);
                    if(getDigit(cTempNum,2)==0) {
                        this.mCurTempImg2.setVisibility(View.GONE);
                    }else{
                        this.mCurTempImg2.setVisibility(View.VISIBLE);
                        this.mCurTempImg2.setBackgroundDrawable(new BitmapDrawable(b2));
                    }
                }else{
                    this.mCurTempImg3.setVisibility(View.VISIBLE);
                    this.mCurTempImg3.setBackgroundDrawable(new BitmapDrawable(b3));
                    this.mCurTempImg2.setVisibility(View.VISIBLE);
                    this.mCurTempImg2.setBackgroundDrawable(new BitmapDrawable(b2));
                }
                this.mCurTempImg1.setVisibility(View.VISIBLE);
                this.mCurTempImg1.setBackgroundDrawable(new BitmapDrawable(b1));

                this.mCurTempDu.setVisibility(View.VISIBLE);
                this.mCurTempDu.setBackgroundDrawable(new BitmapDrawable(du));
            }

            try {
                String condition = mService.getDisplayCondition();
                this.mCurCondition.setText(condition);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            String low = this.mWeather0.getTempLow(tempType) + symbolTemp;
            String high = this.mWeather0.getTempHigh(tempType) + symbolTemp;
            this.mCurTemrange.setText(high+"~"+low);
        }

        String date = DateFormat.format("MM-dd", mCurTime).toString()+"  "+mWeek[0];
        this.mCurDate.setText(date);

    } 

    private ForecastWeatherInfo getForecastWeatherByDay(int day_index){
        switch(day_index){
        case 1:
            return mWeather1;
        case 2:
            return mWeather2;
        case 3:
            return mWeather3;
        case 4:
            return mWeather4;
            default : return null;
        }
    }

    private Bitmap getConditionBitmap(String code){
        Bitmap bitmap =null;
        String path;
        if (TextUtils.isEmpty(code)) {
            path = "common.png";
        } else {
            path = "code_" + code + ".png";
        }
        path = "base/weather/" + path;
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open(path);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            path = "base/weather/common.png";
            try {
                inputStream = mContext.getAssets().open(path);
                bitmap  = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }finally{
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }

    private Bitmap getDuBitmap(boolean isCelsius){
        Bitmap bitmap =null;

        String name = null;
        if(isCelsius){
            name = "du_C.png";
        }else{
            name = "du_F.png";
        }
        bitmap = getTempBitmap(name);

        return bitmap;
    }

    private Bitmap getFuBitmap(){
        return getTempBitmap("fu.png");
    }

    private Bitmap getDigitBitmap(int cTempNum, int pos){
        Bitmap bitmap= null;

        int digit = getDigit(cTempNum,pos);
        String name = "num_"+digit+".png";
        bitmap = getTempBitmap(name);

        return bitmap;
    }

    private Bitmap getTempBitmap(String displayName){
        Bitmap bitmap =null;
        if(!TextUtils.isEmpty(displayName)){
            String path = "base/weather/"+displayName;
            InputStream inputStream=null;
            try {
                inputStream = mContext.getAssets().open(path);
                bitmap  = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    private int getDigit(int cTempNum, int pos){
        switch(pos){
        case 1:
            //single digit 
            return cTempNum%10;
        case 2:
            //tens digit 
            return cTempNum/10 %10;
        case 3:
            //The digits
            return cTempNum/100%10;
        }
        return 0;
    }

    private void initWeekOfDate(){
        int[] weekDays = {R.string.day_of_week_short_sunday,
                R.string.day_of_week_short_monday,
                R.string.day_of_week_short_tuesday,
                R.string.day_of_week_short_wednesday,
                R.string.day_of_week_short_thursday,
                R.string.day_of_week_short_friday,
                R.string.day_of_week_short_saturday};
        Calendar cal = Calendar.getInstance();
        Date curDate = new Date(mCurTime);
        cal.setTime(curDate);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) w = 0;
        for(int i = 0;i<5;i++){
            if(w > 6) w = 0;
            mWeek[i]= mContext.getString(weekDays[w]);
            w++;
        }
    }

    private void initWeatherInfo(){
        try {
            mWeather0 = mService.getWeather();
            mWeather1 =  mService.getForcastWeather(1);
            mWeather2 =  mService.getForcastWeather(2);
            mWeather3 =  mService.getForcastWeather(3);
            mWeather4 =  mService.getForcastWeather(4);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class MyLayout extends FrameLayout{
        private int mPreTouchX;
        
        public MyLayout(Context context) { 
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            int touchX = (int) event.getX();
            switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPreTouchX = touchX;
                if(mIsWeatherShown && touchX>mScreenW*3/4){
                    mSettingsBtn.setBackgroundResource(R.drawable.weather_setting_icon_pressed);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int changeX = touchX - mPreTouchX;
                boolean RToL = true;
                if(mIsWeatherShown){
                    //here should show fore weather ,only support fling right to left
                    RToL = true;
                }else{
                    //here should show weather, only support fling left to right
                    RToL = false;
                }
                translateWeather(RToL,changeX);
                break;
            case MotionEvent.ACTION_UP:
                changeX = Math.abs(touchX - mPreTouchX);
                if(changeX > mScreenW/8){
                    if(touchX > mPreTouchX){
                        resetWeatherPos(true);
                    }else{
                        resetWeatherPos(false);
                    }
                } else if (changeX > 3){
                    if(mIsWeatherShown){
                        resetWeatherPos(true);
                    }else{
                        resetWeatherPos(false);
                    }
                }

                if(changeX <= 3){ //this is a click event
                    if(mIsWeatherShown){
                        if(touchX>mScreenW*3/4){
                            mSettingsBtn.setBackgroundResource(R.drawable.weather_setting_icon_normal);
                            Intent intent = new Intent(mContext, com.borqs.borqsweather.weather.WeatherSettings.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }else{
                            resetWeatherPos(false);
                        }
                    }else{
                        resetWeatherPos(true);
                    }
                }
                break;
            }
            return true;
        }
    }


    private void translateWeather(boolean RToL,int changeX){
        if(RToL){
            this.mWeather.setTranslationX(changeX);
            this.mForeWeather.setTranslationX(mScreenW +changeX);
        }else{ 
            this.mWeather.setTranslationX(-mScreenW +changeX);
            this.mForeWeather.setTranslationX(changeX);
        }
    }

    private void resetWeatherPos(boolean showWeather){
        TranslateAnimation foreWeatherAni = null;
        TranslateAnimation weatherAni = null;
        if (showWeather) {
            weatherAni = new TranslateAnimation(mWeather.getX(), 0, 0f, 0f);
            weatherAni.setInterpolator(AnimationUtils.loadInterpolator(mContext,
                    android.R.anim.decelerate_interpolator));
            weatherAni.setDuration(300);
            mWeather.startAnimation(weatherAni);
            mWeather.setX(0);
            mForeWeather.setX(mScreenW);
            mIsWeatherShown = true;
        } else {
            foreWeatherAni = new TranslateAnimation(mForeWeather.getX(), 0f, 0f, 0f);
            foreWeatherAni.setInterpolator(AnimationUtils.loadInterpolator(mContext,
                    android.R.anim.decelerate_interpolator));
            foreWeatherAni.setDuration(300);
            mForeWeather.startAnimation(foreWeatherAni);
            mWeather.setX(-mScreenW);
            mForeWeather.setX(0);
            mIsWeatherShown = false;
        }

        SharedPreferences.Editor editor = mContext.getSharedPreferences(WEATHER_PREFERENCE, Context.MODE_PRIVATE)
                .edit();
        editor.putBoolean("is_weather_shown", mIsWeatherShown);
        editor.commit();
    }

}
