package com.borqs.se.widget3d;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.borqs.borqsweather.weather.IWeatherService;
import com.borqs.borqsweather.weather.WeatherConditions;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECamera.CameraChangedListener;
import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SEParticleSystem;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERay;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeManager.WeatherBindedCallBack;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.ProviderUtils;

import java.util.ArrayList;
import java.util.List;

public class Cloud extends NormalObject implements CameraChangedListener, WeatherBindedCallBack {
    private String mSkyImgKey;
    private SkyObject mThunderObject;
    private SkyObject mSunObject;
    private SkyObject mSunLayer;
    private SkyObject mSunShine;
//    private SkyObject mMoon;
    private SkyObject mBlank;
    private List<SkyObject> mSkyObjects;
    private SkyObject mCloudLayer1;
    private SkyObject mCloudLayer2;
    private int mThunderRandomTime;
    private UpdateSkyAnimation mUpdateSkyAnimation;
    private int mCurrentType = WeatherConditions.CONDITION_TYPE_NONE;
    private boolean mIsNight = false;
    private boolean mOnShow;
    private VelocityTracker mVelocityTracker;
    private Context mContext;
    private IWeatherService mService;
    private boolean mCancelClick;
    private int mSkyAnimationCount;
    private long mRequestWeatherTime;
    private SEParticleSystem mLargeSnowflakes;
    private SEParticleSystem mLittleSnowflakes;
    private SEParticleSystem mBigRaindrops;
    private SEParticleSystem mSmallRaindrops;
    private SEParticleSystem mStars;
    private SEParticleSystem mFlashStars;
    private SEParticleSystem mShootingStar;
    private SECameraData mObserverCameraData_Land;
    private SECameraData mObserverCameraData_Port;
    private boolean mForceChange = false;
    private boolean mIsLandScape;
    
    public Cloud(HomeScene scene, String name, int index) {
        super(scene, name, index);
        setClickable(true);
        mCancelClick = false;
        mContext = getContext();
        mOnShow = false;
        mSkyObjects = new ArrayList<SkyObject>();
        mRequestWeatherTime = 0;
        HomeManager.getInstance().setWeatherBindedCallBack(this);
    }

    @Override
    public void initStatus() {
        //天空需要用到房间的参数，所以不能在房间还没初始化完时初始化房间
        if (getHomeScene().getHouse() == null) {
            getHomeScene().setSky(this);
            return;
        }
        super.initStatus();
        mIsLandScape = HomeManager.getInstance().isLandscapeOrientation();
        String observerCameraData = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "observation_land");
        if (!TextUtils.isEmpty(observerCameraData)) {
            mObserverCameraData_Land = new SECameraData();
            String[] data = observerCameraData.split(ProviderUtils.SPLIT_SYMBOL);
            mObserverCameraData_Land.init(data);

        }
        observerCameraData = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "observation_port");
        if (!TextUtils.isEmpty(observerCameraData)) {
            mObserverCameraData_Port = new SECameraData();
            String[] data = observerCameraData.split(ProviderUtils.SPLIT_SYMBOL);
            mObserverCameraData_Port.init(data);

        }
        initSkyFacePara();
        getHomeScene().setSky(this);
        getCamera().addCameraChangedListener(this);
        setOnLongClickListener(null);
        setOnClickListener(new OnTouchListener() {
            public void run(SEObject obj) {
                if (getHomeScene().getSightValue() >= 0) {
                    getHomeScene().moveToSkySight(null);
                }
            }
        });
        IWeatherService service = HomeManager.getInstance().getWeatherService();
        if (service != null) {
            mService = service;
            try {
                int currentType = mService.getConditionType();
                boolean isNight = mService.isNight();
                if (Math.abs(System.currentTimeMillis() - mRequestWeatherTime) > 1000 * 60 * 30) {
                    mRequestWeatherTime = System.currentTimeMillis();
                    mService.checkToUpdateWeather(1000 * 60);
                }
                processWeather_changed(currentType, isNight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            HomeManager.getInstance().bindWeatherService();
        }

        IntentFilter filter = new IntentFilter("com.borqs.borqsweather.update");
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    public void notifySurfaceChanged(int width, int height) {
        super.notifySurfaceChanged(width, height);
        if (mIsLandScape != HomeManager.getInstance().isLandscapeOrientation()) {
            mIsLandScape = HomeManager.getInstance().isLandscapeOrientation();
            if (hasInit()) {
                initSkyFacePara();
                if (mCloudLayer1 != null) {
                    mCloudLayer1.getParent().removeChild(mCloudLayer1, true);
                    mCloudLayer1 = null;
                }
                if (mCloudLayer2 != null) {
                    mCloudLayer2.getParent().removeChild(mCloudLayer2, true);
                    mCloudLayer2 = null;
                }
                processWeather_LoadCloudObject();
            }
        }
    }

    public SECameraData getObserveCamera() {
        if (mIsLandScape) {
            return mObserverCameraData_Land;
        } else {
            return mObserverCameraData_Port;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        trackVelocity(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        new SECommand(getScene()) {
            public void run() {
                if (mService == null) {
                    mService = HomeManager.getInstance().getWeatherService();
                    if (mService == null) {
                        HomeManager.getInstance().bindWeatherService();
                    }
                } else {
                    try {
                        boolean isNight = mService.isNight();
                        if (mIsNight != isNight) {
                            processWeather_changed(mCurrentType, isNight);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.execute();
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPreTouch();
            stopAllAnimation(null);
            mCancelClick = false;
            break;
        case MotionEvent.ACTION_MOVE:
            if (getHomeScene().getSightValue() >= 0) {
                float skyY = (getTouchY() - getPreTouchY()) * 2f / getCamera().getHeight()
                        + getHomeScene().getSightValue();
                if (skyY < 0) {
                    skyY = 0;
                }
                getHomeScene().changeSight(skyY, true);
            }
            setPreTouch();
            mCancelClick = true;
            break;
        case MotionEvent.ACTION_UP:
            setPreTouch();
            if (!mCancelClick) {
                performClick();
                return true;
            }

        case MotionEvent.ACTION_CANCEL:
            if (getHomeScene().getSightValue() >= 0) {
                if (mVelocityTracker.getYVelocity() > 200) {
                    getHomeScene().moveToSkySight(null);
                } else if (mVelocityTracker.getYVelocity() < -200) {
                    getHomeScene().moveToWallSight(null);
                } else {
                    if (getHomeScene().getSightValue() > 0.5f) {
                        getHomeScene().moveToSkySight(null);
                    } else {
                        getHomeScene().moveToWallSight(null);
                    }
                }
            }
            break;
        }
        return true;
    }

    private void show() {
        if (!mOnShow || mForceChange) {
            if (mService != null) {
                try {
                    mService.checkLocationServices();
                    if (Math.abs(System.currentTimeMillis() - mRequestWeatherTime) > 1000 * 60 * 30) {
                        mRequestWeatherTime = System.currentTimeMillis();
                        mService.checkToUpdateWeather(1000 * 60 * 30);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mOnShow = true;
            mForceChange = false;
            stopAllAnimation(null);
            if (mService == null) {
                mCurrentType = WeatherConditions.CONDITION_TYPE_NONE;
                mIsNight = false;
            }
            processWeather_LoadSkyObject(mCurrentType, mIsNight);
            processWeather_ShowWeatherImage();
            processWeather_CreateParticle(mCurrentType, mIsNight);
            processWeather_PlaySkyAnimation(mCurrentType, mIsNight);
        }
    }

    private void hide(final SEAnimFinishListener l) {
        if (mOnShow) {
            mOnShow = false;
            stopAllAnimation(null);
            processWeather_StopSkyAnimation();
            processWeather_Unload();
            processWeather_DestroyParticle();
            if (l != null) {
                l.onAnimationfinish();
            }
        }
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mOnShow) {
            getHomeScene().moveToWallSight(null);
        }
        return false;
    }

    private void processWeather_changed(int type, boolean isNight) {
        if (!hasInit() || (mCurrentType == type && mIsNight == isNight)) {
            return;
        }
        if (HomeUtils.DEBUG)
            Log.d(HomeUtils.TAG, "####sky processWeather_changed ####");
        mSkyAnimationCount = 0;
        processWeather_UpdateSkyImage(type, isNight);
        if (mOnShow) {
            processWeather_StopSkyAnimation();
            processWeather_Unload();
            processWeather_DestroyParticle();

            processWeather_LoadSkyObject(type, isNight);
            processWeather_ShowWeatherImage();
            processWeather_CreateParticle(type, isNight);
            processWeather_PlaySkyAnimation(type, isNight);
        }
        processWeather_LoadCloudObject();
        processWeather_UpdateClouds(type, isNight);
        mCurrentType = type;
        mIsNight = isNight;
    }

    private void processWeather_UpdateSkyImage(int type, boolean isNight) {
        String img = null;
        switch (type) {
        case WeatherConditions.CONDITION_TYPE_FOG:
        case WeatherConditions.CONDITION_TYPE_SNOW:
            if (isNight) {
                img = "assets/base/sky/bg_night_sun_cloudy.jpg";
            } else {
                img = "assets/base/sky/bg_fog.jpg";
            }
            // mFontColorID = R.color.blue;
            break;
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_HAIL:
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
        case WeatherConditions.CONDITION_TYPE_THUNDER:
            if (isNight) {
                img = "assets/base/sky/night_dark_cloud.jpg";
            } else {
                img = "assets/base/sky/dark_cloud.jpg";
            }

            // mFontColorID = R.color.yellow;
            break;
        case WeatherConditions.CONDITION_TYPE_SUN:
            if (isNight) {
                img = "assets/base/sky/bg_night_sun_cloudy.jpg";
            } else {
                img = "assets/base/sky/qingtianbogyun.jpg";
            }
            // mFontColorID = R.color.yellow;
            break;
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
        case WeatherConditions.CONDITION_TYPE_NONE:
            if (isNight) {
                img = "assets/base/sky/bg_night_sun_cloudy.jpg";
            } else {
                img = "assets/base/sky/qingtianbogyun.jpg";
            }
            // mFontColorID = R.color.yellow;
            break;
        }
        if (!TextUtils.isEmpty(img)) {
            final String imgPath = img;
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData = loadImageData_JNI(imgPath);
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI(mSkyImgKey, imageData);
                        }
                    }.execute();

                }
            });

        }
    }

    private void processWeather_PlaySkyAnimation(int type, boolean isNight) {
        switch (type) {
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_FOG:
        case WeatherConditions.CONDITION_TYPE_HAIL:
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_SNOW:
        case WeatherConditions.CONDITION_TYPE_THUNDER:
        case WeatherConditions.CONDITION_TYPE_SUN:
            if (mUpdateSkyAnimation != null) {
                mUpdateSkyAnimation.stop();
            }
            mUpdateSkyAnimation = new UpdateSkyAnimation(getScene(), isNight);
            mUpdateSkyAnimation.execute();
            break;
        default:
            break;
        }
    }

    private void processWeather_OnAnimation(int type, boolean isNight, int count) {
        switch (type) {
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_HAIL:
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_SNOW:
            float step = (count % 2000) / 2000f;
            SEVector2f dir = new SEVector2f(step, 0);
            playUVAnimation(dir);
            break;
        case WeatherConditions.CONDITION_TYPE_THUNDER:
            float stepT = (count % 2000) / 2000f;
            SEVector2f dirT = new SEVector2f(stepT, 0);
            playUVAnimation(dirT);

            int frameT = (count - 1) % mThunderRandomTime;
            if (mThunderObject != null && frameT <= 25) {
                if (frameT <= 5) {
                    if (frameT == 0) {
                        mThunderObject.setVisible(true);
                    }
                    if (frameT == 2) {
                        mBlank.setVisible(true);
                    }
                    mThunderObject.setAlpha(1f - frameT * 0.2f);
                    if (frameT > 2) {
                        mBlank.setAlpha(1f - (float) frameT * 0.1f);
                    }
                } else if (frameT <= 10) {
                    mThunderObject.setAlpha((frameT - 5) * 0.2f);
                    mBlank.setAlpha(1f - (float) frameT * 0.1f);
                } else if (frameT <= 20) {
                    mThunderObject.setAlpha(1f - (frameT - 10) * 0.1f);
                    if (frameT == 20) {
                        mBlank.setVisible(false);
                        mThunderObject.setVisible(false);
                    }
                } else if (frameT == 25) {
                    mThunderRandomTime = getRandom(100, 350, 50);
                    final String img = "assets/base/sky/thunder" + getRandom(0, 3, 1) + ".png";
                    SELoadResThread.getInstance().process(new Runnable() {
                        public void run() {
                            final int imageData = loadImageData_JNI(img);
                            new SECommand(getScene()) {
                                public void run() {
                                    addImageData_JNI(mThunderObject.mImgKey, imageData);
                                }
                            }.execute();

                        }
                    });
                }
            }
            break;
        case WeatherConditions.CONDITION_TYPE_SUN:
            if (!isNight) {
                if (mSunObject != null) {
                    int frameS = count % 360;
                    float angle = mSunShine.getUserRotate().getAngle() + 0.5f;
                    mSunLayer.setUserRotate(new SERotate(angle, 0, 1, 0));
                    if (frameS == 0) {
                        mSunShine.setVisible(true);
                    } else if (frameS < 90) {
                        mSunShine.setUserRotate(new SERotate(-frameS, 0, 1, 0));
                    } else if (frameS == 90) {
                        mSunShine.setVisible(false);
                    }
                }
                float stepBg = ((mSkyAnimationCount + count) % 2000f) / 2000f;
                SEVector2f dirBg = new SEVector2f(stepBg, 0);
                playUVAnimation(dirBg);
            }
            playCloudUVAnimation(count);
            break;
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
            count = mSkyAnimationCount + count;
            playCloudUVAnimation(count);
            if (!isNight) {
                float stepBg = (count % 2000f) / 2000f;
                SEVector2f dirBg = new SEVector2f(stepBg, 0);
                playUVAnimation(dirBg);
            }
            break;
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
        case WeatherConditions.CONDITION_TYPE_FOG:// TODO
            count = mSkyAnimationCount + count;
            float stepBg = (count % 2000f) / 2000f;
            SEVector2f dirBg = new SEVector2f(stepBg, 0);
            playUVAnimation(dirBg);
            playCloudUVAnimation(count);
            break;
        default:
            break;
        }
    }

    private void processWeather_StopSkyAnimation() {
        if (mUpdateSkyAnimation != null) {
            mUpdateSkyAnimation.stop();
        }
    }

    private void processWeather_CreateParticle(int type, boolean isNight) {
        float houseW = getHomeScene().getHouse().mWallRadius * 2;
        float houseH = getHomeScene().getHouse().mWallHeight;
        switch (type) {
        case WeatherConditions.CONDITION_TYPE_SNOW:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/particle/mainsnow.png");
                    final int imageData2 = loadImageData_JNI("assets/base/particle/helpsnow.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI("assets/base/particle/mainsnow.png", imageData1);
                            addImageData_JNI("assets/base/particle/helpsnow.png", imageData2);

                            applyImage_JNI("assets/base/particle/mainsnow.png", "assets/base/particle/mainsnow.png");
                            applyImage_JNI("assets/base/particle/helpsnow.png", "assets/base/particle/helpsnow.png");
                        }
                    }.execute();
                }
            });
            mLargeSnowflakes = new SEParticleSystem("LargeSnowflakes");
            mLargeSnowflakes.setParticleSystemAttribute(new float[] { 40, 40 }, 600);
            mLargeSnowflakes.setEmitterAngle(10);
            mLargeSnowflakes.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mLargeSnowflakes.setEmitterPosition(0, 0, houseH);
            mLargeSnowflakes.setEmitterDirection(new SEVector3f(0, 0, -1));
            mLargeSnowflakes.setEmitterParticleVelocity(50, 200);
            mLargeSnowflakes.setEmitterTimeToLive(5, 10);
            mLargeSnowflakes.setEmitterEmissionRate(50);
            mLargeSnowflakes.setLinearForceAffectorEnable(true);
            mLargeSnowflakes.setForceVector(0, 0, -1);
            mLargeSnowflakes.setColourInterpolatorAffectorEnable(true);
            mLargeSnowflakes.setTimeAdjust(0, 0);
            mLargeSnowflakes.setColorAdjust(0, mLargeSnowflakes.new ColorValue(1, 1, 1, 1.0f));
            mLargeSnowflakes.setTimeAdjust(1, 2.5f);
            mLargeSnowflakes.setColorAdjust(1, mLargeSnowflakes.new ColorValue(1, 1, 1, 0.5f));
            mLargeSnowflakes.setTimeAdjust(2, 5.0f);
            mLargeSnowflakes.setColorAdjust(2, mLargeSnowflakes.new ColorValue(1, 1, 1, 0.0f));
            mLargeSnowflakes.setImagePath("assets/base/particle/mainsnow.png");
            mLargeSnowflakes.addParticle_JNI();

            mLittleSnowflakes = new SEParticleSystem("LittleSnowflakes");
            mLittleSnowflakes.setParticleSystemAttribute(new float[] { 20, 20 }, 300);
            mLittleSnowflakes.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mLittleSnowflakes.setEmitterPosition(0, 0, houseH);
            mLittleSnowflakes.setEmitterDirection(new SEVector3f(0.5f, 0, -1));
            mLittleSnowflakes.setEmitterParticleVelocity(300, 500);
            mLittleSnowflakes.setEmitterTimeToLive(5, 10);
            mLittleSnowflakes.setEmitterEmissionRate(50);
            mLittleSnowflakes.setLinearForceAffectorEnable(true);
            mLittleSnowflakes.setForceVector(0, 0, -1);
            mLittleSnowflakes.setImagePath("assets/base/particle/helpsnow.png");
            mLittleSnowflakes.addParticle_JNI();
            break;
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_THUNDER:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/particle/mainrain.png");
                    final int imageData2 = loadImageData_JNI("assets/base/particle/helprain.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI("assets/base/particle/mainrain.png", imageData1);
                            addImageData_JNI("assets/base/particle/helprain.png", imageData2);

                            applyImage_JNI("assets/base/particle/mainrain.png", "assets/base/particle/mainrain.png");
                            applyImage_JNI("assets/base/particle/helprain.png", "assets/base/particle/helprain.png");
                        }
                    }.execute();
                }
            });
            mBigRaindrops = new SEParticleSystem("BigRaindrops");
            mBigRaindrops.setParticleSystemAttribute(new float[] { 20, 40 }, 600);
            mBigRaindrops.setEmitterAngle(10);
            mBigRaindrops.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mBigRaindrops.setEmitterPosition(0, 0, houseH);
            mBigRaindrops.setEmitterDirection(new SEVector3f(0, 0, -1));
            mBigRaindrops.setEmitterParticleVelocity(600f, 800f);
            mBigRaindrops.setEmitterTimeToLive(2, 4);
            mBigRaindrops.setEmitterEmissionRate(200);
            mBigRaindrops.setLinearForceAffectorEnable(true);
            mBigRaindrops.setForceVector(0, 0, -1);
            mBigRaindrops.setColourInterpolatorAffectorEnable(true);
            mBigRaindrops.setTimeAdjust(0, 0);
            mBigRaindrops.setColorAdjust(0, mBigRaindrops.new ColorValue(1, 1, 1, 1.0f));
            mBigRaindrops.setTimeAdjust(1, 2.5f);
            mBigRaindrops.setColorAdjust(1, mBigRaindrops.new ColorValue(1, 1, 1, 0.5f));
            mBigRaindrops.setTimeAdjust(2, 5.0f);
            mBigRaindrops.setColorAdjust(2, mBigRaindrops.new ColorValue(1, 1, 1, 0.0f));
            mBigRaindrops.setImagePath("assets/base/particle/mainrain.png");
            mBigRaindrops.addParticle_JNI();

            mSmallRaindrops = new SEParticleSystem("SmallRaindrops");
            mSmallRaindrops.setParticleSystemAttribute(new float[] { 20, 20 }, 300);
            mSmallRaindrops.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mSmallRaindrops.setEmitterPosition(0, 0, houseH);
            mSmallRaindrops.setEmitterDirection(new SEVector3f(0, 0, -1));
            mSmallRaindrops.setEmitterParticleVelocity(600f, 800f);
            mSmallRaindrops.setEmitterTimeToLive(2, 4);
            mSmallRaindrops.setEmitterEmissionRate(100);
            mSmallRaindrops.setLinearForceAffectorEnable(true);
            mSmallRaindrops.setForceVector(0, 0, -1);
            mSmallRaindrops.setImagePath("assets/base/particle/helprain.png");
            mSmallRaindrops.addParticle_JNI();
            break;
        case WeatherConditions.CONDITION_TYPE_HAIL:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/particle/mainhaily.png");
                    final int imageData2 = loadImageData_JNI("assets/base/particle/helphaily.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI("assets/base/particle/mainhaily.png", imageData1);
                            addImageData_JNI("assets/base/particle/helphaily.png", imageData2);

                            applyImage_JNI("assets/base/particle/mainhaily.png", "assets/base/particle/mainhaily.png");
                            applyImage_JNI("assets/base/particle/helphaily.png", "assets/base/particle/helphaily.png");

                        }
                    }.execute();
                }
            });
            mBigRaindrops = new SEParticleSystem("BigRaindrops");
            mBigRaindrops.setParticleSystemAttribute(new float[] { 30, 30 }, 800);
            mBigRaindrops.setEmitterAngle(10);
            mBigRaindrops.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mBigRaindrops.setEmitterPosition(0, 0, houseH);
            mBigRaindrops.setEmitterDirection(new SEVector3f(0, 0, -1));
            mBigRaindrops.setEmitterParticleVelocity(1200f, 1600f);
            mBigRaindrops.setEmitterTimeToLive(2, 3);
            mBigRaindrops.setEmitterEmissionRate(200);
            mBigRaindrops.setLinearForceAffectorEnable(true);
            mBigRaindrops.setForceVector(0, 0, -1);
            mBigRaindrops.setColourInterpolatorAffectorEnable(true);
            mBigRaindrops.setTimeAdjust(0, 0);
            mBigRaindrops.setColorAdjust(0, mBigRaindrops.new ColorValue(1, 1, 1, 1.0f));
            mBigRaindrops.setTimeAdjust(1, 2.5f);
            mBigRaindrops.setColorAdjust(1, mBigRaindrops.new ColorValue(1, 1, 1, 0.5f));
            mBigRaindrops.setTimeAdjust(2, 5.0f);
            mBigRaindrops.setColorAdjust(2, mBigRaindrops.new ColorValue(1, 1, 1, 0.0f));
            mBigRaindrops.setImagePath("assets/base/particle/mainhaily.png");
            mBigRaindrops.addParticle_JNI();

            mSmallRaindrops = new SEParticleSystem("SmallRaindrops");
            mSmallRaindrops.setParticleSystemAttribute(new float[] { 20, 20 }, 400);
            mSmallRaindrops.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mSmallRaindrops.setEmitterPosition(0, 0, houseH);
            mSmallRaindrops.setEmitterDirection(new SEVector3f(0.2f, 0, -1));
            mSmallRaindrops.setEmitterParticleVelocity(1200f, 1600f);
            mSmallRaindrops.setEmitterTimeToLive(2, 3);
            mSmallRaindrops.setEmitterEmissionRate(100);
            mSmallRaindrops.setLinearForceAffectorEnable(true);
            mSmallRaindrops.setForceVector(0, 0, -1);
            mSmallRaindrops.setImagePath("assets/base/particle/helphaily.png");
            mSmallRaindrops.addParticle_JNI();
            break;
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_FOG:
        case WeatherConditions.CONDITION_TYPE_SUN:
            if (isNight) {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        final int imageData1 = loadImageData_JNI("assets/base/particle/mainstar.png");
                        final int imageData2 = loadImageData_JNI("assets/base/particle/helpstar.png");
                        new SECommand(getScene()) {
                            public void run() {
                                addImageData_JNI("assets/base/particle/mainstar.png", imageData1);
                                addImageData_JNI("assets/base/particle/helpstar.png", imageData2);

                                applyImage_JNI("assets/base/particle/mainstar.png", "assets/base/particle/mainstar.png");
                                applyImage_JNI("assets/base/particle/helpstar.png", "assets/base/particle/helpstar.png");
                            }
                        }.execute();
                    }
                });

                SEVector3f center = mCenterOfFace.add(getDistance(SkyObject.LAYER_INDEX_Particle));
                float[] screenSize = calculateScreenSize(center);
                mStars = new SEParticleSystem("Stars");
                mStars.setParticleSystemAttribute(new float[] { 25, 25 }, 60);
                mStars.setBoxEmitterSize(screenSize[0], 10, screenSize[1]);
                mStars.setEmitterRotate(new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0));
                mStars.setEmitterTranslate(center);
                mStars.setEmitterPosition(0, 0, 0);
                mStars.setEmitterDirection(new SEVector3f(0, 0, 1));
                mStars.setEmitterParticleVelocity(0, 0);
                mStars.setEmitterTimeToLive(10000, 10000);
                mStars.setEmitterAngle(5);
                mStars.setEmitterEmissionRate(60);
                mStars.setImagePath("assets/base/particle/mainstar.png");
                mStars.setNeedDepthTest(true);
                mStars.setLayerIndex(SkyObject.LAYER_INDEX_Particle);
                mStars.addParticle_JNI();

                mFlashStars = new SEParticleSystem("FlashStars");
                mFlashStars.setParticleSystemAttribute(new float[] { 25, 25 }, 30);
                mFlashStars.setBoxEmitterSize(screenSize[0], 10, screenSize[1]);
                mFlashStars.setEmitterRotate(new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0));
                mFlashStars.setEmitterPosition(0, 0, 0);
                mFlashStars.setEmitterTranslate(center);
                mFlashStars.setEmitterDirection(new SEVector3f(0, 0, 1));
                mFlashStars.setEmitterParticleVelocity(0, 0);
                mFlashStars.setEmitterTimeToLive(5, 10);
                mFlashStars.setEmitterAngle(5);
                mFlashStars.setEmitterEmissionRate(6);
                mFlashStars.setEmitterColorValue(1, 1, 1, 0);
                mFlashStars.setColourInterpolatorAffectorEnable(true);
                mFlashStars.setTimeAdjust(0, 0);
                mFlashStars.setColorAdjust(0, mFlashStars.new ColorValue(1, 1, 1, 0));
                mFlashStars.setTimeAdjust(1, 0.2f);
                mFlashStars.setColorAdjust(1, mFlashStars.new ColorValue(1, 1, 1, 1));
                mFlashStars.setTimeAdjust(2, 0.4f);
                mFlashStars.setColorAdjust(2, mFlashStars.new ColorValue(1, 1, 1, 0));
                mFlashStars.setTimeAdjust(3, 0.6f);
                mFlashStars.setColorAdjust(3, mFlashStars.new ColorValue(1, 1, 1, 1));
                mFlashStars.setTimeAdjust(4, 0.8f);
                mFlashStars.setColorAdjust(4, mFlashStars.new ColorValue(1, 1, 1, 0));
                mFlashStars.setTimeAdjust(5, 1.0f);
                mFlashStars.setColorAdjust(5, mFlashStars.new ColorValue(1, 1, 1, 1));
                mFlashStars.setNeedDepthTest(true);
                mFlashStars.setLayerIndex(SkyObject.LAYER_INDEX_Particle);
                mFlashStars.setImagePath("assets/base/particle/mainstar.png");
                mFlashStars.addParticle_JNI();

                mShootingStar = new SEParticleSystem("ShootingStar");
                mShootingStar.setParticleSystemAttribute(new float[] { 100, 100 }, 5);
                mShootingStar.setBoxEmitterSize(screenSize[0], 10, screenSize[1]);
                mShootingStar.setEmitterRotate(new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0));
                mShootingStar.setEmitterPosition(0, 0, 0);
                mShootingStar.setEmitterTranslate(center);
                mShootingStar.setEmitterDirection(new SEVector3f(-1, 0, -1));
                mShootingStar.setEmitterParticleVelocity(300, 500);
                mShootingStar.setEmitterTimeToLive(2, 2);
                mShootingStar.setEmitterAngle(30);
                mShootingStar.setEmitterEmissionRate(1);
                mShootingStar.setColourFaderAffectorEnable(true);
                mShootingStar.setColorFaderAdjust(-0.0f, -0.0f, -0.0f, -0.5f);
                mShootingStar.setNeedDepthTest(true);
                mShootingStar.setLayerIndex(SkyObject.LAYER_INDEX_Particle);
                mShootingStar.setImagePath("assets/base/particle/helpstar.png");
                mShootingStar.addParticle_JNI();
            }
            break;
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/particle/mainrain.png");
                    final int imageData2 = loadImageData_JNI("assets/base/particle/helpsnow.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI("assets/base/particle/mainrain.png", imageData1);
                            addImageData_JNI("assets/base/particle/helpsnow.png", imageData2);

                            applyImage_JNI("assets/base/particle/mainrain.png", "assets/base/particle/mainrain.png");
                            applyImage_JNI("assets/base/particle/helpsnow.png", "assets/base/particle/helpsnow.png");
                        }
                    }.execute();
                }
            });
            mBigRaindrops = new SEParticleSystem("BigRaindrops");
            mBigRaindrops.setParticleSystemAttribute(new float[] { 10, 40 }, 600);
            mBigRaindrops.setEmitterAngle(10);
            mBigRaindrops.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mBigRaindrops.setEmitterPosition(0, 0, houseH);
            mBigRaindrops.setEmitterDirection(new SEVector3f(0, 0, -1));
            mBigRaindrops.setEmitterParticleVelocity(600f, 800f);
            mBigRaindrops.setEmitterTimeToLive(2, 4);
            mBigRaindrops.setEmitterEmissionRate(200);
            mBigRaindrops.setLinearForceAffectorEnable(true);
            mBigRaindrops.setForceVector(0, 0, -1);
            mBigRaindrops.setColourInterpolatorAffectorEnable(true);
            mBigRaindrops.setTimeAdjust(0, 0);
            mBigRaindrops.setColorAdjust(0, mBigRaindrops.new ColorValue(1, 1, 1, 1.0f));
            mBigRaindrops.setTimeAdjust(1, 2.5f);
            mBigRaindrops.setColorAdjust(1, mBigRaindrops.new ColorValue(1, 1, 1, 0.5f));
            mBigRaindrops.setTimeAdjust(2, 5.0f);
            mBigRaindrops.setColorAdjust(2, mBigRaindrops.new ColorValue(1, 1, 1, 0.0f));
            mBigRaindrops.setImagePath("assets/base/particle/mainrain.png");
            mBigRaindrops.addParticle_JNI();

            mLittleSnowflakes = new SEParticleSystem("LittleSnowflakes");
            mLittleSnowflakes.setParticleSystemAttribute(new float[] { 20, 20 }, 300);
            mLittleSnowflakes.setBoxEmitterSize(houseW, houseW, 2 * houseH);
            mLittleSnowflakes.setEmitterPosition(0, 0, houseH);
            mLittleSnowflakes.setEmitterDirection(new SEVector3f(0.5f, 0, -1));
            mLittleSnowflakes.setEmitterParticleVelocity(300, 500);
            mLittleSnowflakes.setEmitterTimeToLive(5, 10);
            mLittleSnowflakes.setEmitterEmissionRate(100);
            mLittleSnowflakes.setLinearForceAffectorEnable(true);
            mLittleSnowflakes.setForceVector(0, 0, -1);
            mLittleSnowflakes.setImagePath("assets/base/particle/helpsnow.png");
            mLittleSnowflakes.addParticle_JNI();
            break;
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
            break;
        default:
            break;
        }
    }

    private void processWeather_DestroyParticle() {
        if (mLargeSnowflakes != null) {
            mLargeSnowflakes.deleteParticleObject_JNI();
            mLargeSnowflakes = null;
        }
        if (mLittleSnowflakes != null) {
            mLittleSnowflakes.deleteParticleObject_JNI();
            mLittleSnowflakes = null;
        }
        if (mBigRaindrops != null) {
            mBigRaindrops.deleteParticleObject_JNI();
            mBigRaindrops = null;
        }
        if (mSmallRaindrops != null) {
            mSmallRaindrops.deleteParticleObject_JNI();
            mSmallRaindrops = null;
        }
        if (mStars != null) {
            mStars.deleteParticleObject_JNI();
            mStars = null;
        }
        if (mFlashStars != null) {
            mFlashStars.deleteParticleObject_JNI();
            mFlashStars = null;
        }
        if (mShootingStar != null) {
            mShootingStar.deleteParticleObject_JNI();
            mShootingStar = null;
        }
    }

    private void processWeather_LoadSkyObject(int type, boolean isNight) {

        switch (type) {
        case WeatherConditions.CONDITION_TYPE_SUN:
            if (isNight) {
                int lunarDay;
                if (mService != null) {
                    try {
                        lunarDay = mService.getlunarDay();
                        if (lunarDay > 1 && lunarDay < 30) {
                            SkyObject moon = new SkyObject(getScene(), "moon");
                            moon.createMoon("assets/base/sky/moon" + lunarDay + ".png", SkyObject.LAYER_INDEX_1,
                                    lunarDay);
                            getScene().getContentObject().addChild(moon, true);
                            mSkyObjects.add(moon);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mSunShine = new SkyObject(getScene(), "sun_shine");
                mSunShine.createSun("assets/base/sky/sun_shine.png", SkyObject.LAYER_INDEX_0);
                getScene().getContentObject().addChild(mSunShine, true);
                mSkyObjects.add(mSunShine);
                mSunLayer = new SkyObject(getScene(), "sun_layer");
                mSunLayer.createSun("assets/base/sky/sun_layer.png", SkyObject.LAYER_INDEX_1);
                getScene().getContentObject().addChild(mSunLayer, true);
                mSkyObjects.add(mSunLayer);
                mSunObject = new SkyObject(getScene(), "sun");
                mSunObject.createSun("assets/base/sky/sun.png", SkyObject.LAYER_INDEX_2);
                getScene().getContentObject().addChild(mSunObject, true);
                mSkyObjects.add(mSunObject);
            }
            break;
        case WeatherConditions.CONDITION_TYPE_THUNDER:
            mThunderRandomTime = 100;
            mThunderObject = new SkyObject(getScene(), "thunder");
            mThunderObject.createThunder(SkyObject.LAYER_INDEX_0);
            mSkyObjects.add(mThunderObject);
            getScene().getContentObject().addChild(mThunderObject, true);
            mBlank = new SkyObject(getScene(), "thunder_blank");
            mBlank.createFlash("assets/base/sky/blank.png", SkyObject.LAYER_INDEX_1);
            getScene().getContentObject().addChild(mBlank, true);
            mSkyObjects.add(mBlank);
            break;
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_SNOW:
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
        case WeatherConditions.CONDITION_TYPE_HAIL:
            break;
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
            break;
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_FOG:
            if (isNight) {
                int lunarDay;
                if (mService != null) {
                    try {
                        lunarDay = mService.getlunarDay();
                        if (lunarDay > 1 && lunarDay < 30) {
                            SkyObject moon = new SkyObject(getScene(), "moon");
                            moon.createMoon("assets/base/sky/moon" + lunarDay + ".png", SkyObject.LAYER_INDEX_0,
                                    lunarDay);
                            getScene().getContentObject().addChild(moon, true);
                            mSkyObjects.add(moon);
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
        default:
            break;
        }
    }

    private void processWeather_UpdateClouds(int weatherType, boolean isNight) {
        mCloudLayer1.setVisible(false);
        mCloudLayer2.setVisible(false);
        switch (weatherType) {
        case WeatherConditions.CONDITION_TYPE_SUN:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/sky/fog.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                            mCloudLayer1.setVisible(true);
                        }
                    }.execute();
                }
            });
            break;
        case WeatherConditions.CONDITION_TYPE_THUNDER:
        case WeatherConditions.CONDITION_TYPE_SLEET:
        case WeatherConditions.CONDITION_TYPE_RAIN:
        case WeatherConditions.CONDITION_TYPE_SNOW:
        case WeatherConditions.CONDITION_TYPE_RAIN_SNOW:
        case WeatherConditions.CONDITION_TYPE_HAIL:
            break;
        case WeatherConditions.CONDITION_TYPE_DUST:
        case WeatherConditions.CONDITION_TYPE_FOG:
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData1 = loadImageData_JNI("assets/base/sky/cloud_dn_small_overcast.png");
                    new SECommand(getScene()) {
                        public void run() {
                            addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                            mCloudLayer1.setVisible(true);
                        }
                    }.execute();
                }
            });
            break;
        case WeatherConditions.CONDITION_TYPE_OVERCAST:
            if (isNight) {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        final int imageData1 = loadImageData_JNI("assets/base/sky/cloud_dn_small_overcast.png");
                        new SECommand(getScene()) {
                            public void run() {
                                addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                                mCloudLayer1.setVisible(true);
                            }
                        }.execute();
                    }
                });
            } else {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        final int imageData1 = loadImageData_JNI("assets/base/sky/cloud_dn_small_overcast.png");
                        final int imageData2 = loadImageData_JNI("assets/base/sky/cloud_day_large_overcast.png");
                        new SECommand(getScene()) {
                            public void run() {
                                addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                                addImageData_JNI(mCloudLayer2.mImgKey, imageData2);
                                mCloudLayer1.setVisible(true);
                                mCloudLayer2.setVisible(true);
                            }
                        }.execute();
                    }
                });
            }
            break;
        case WeatherConditions.CONDITION_TYPE_CLOUDY:
            if (isNight) {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        final int imageData1 = loadImageData_JNI("assets/base/sky/cloud_night_large_cloudy.png");
                        final int imageData2 = loadImageData_JNI("assets/base/sky/fog.png");
                        new SECommand(getScene()) {
                            public void run() {
                                addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                                addImageData_JNI(mCloudLayer1.mImgKey, imageData2);
                                mCloudLayer1.setVisible(true);
                                mCloudLayer2.setVisible(true);
                            }
                        }.execute();
                    }
                });
            } else {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        final int imageData1 = loadImageData_JNI("assets/base/sky/fog.png");
                        final int imageData2 = loadImageData_JNI("assets/base/sky/cloud_day_large_cloudy.png");
                        new SECommand(getScene()) {
                            public void run() {
                                addImageData_JNI(mCloudLayer1.mImgKey, imageData1);
                                addImageData_JNI(mCloudLayer2.mImgKey, imageData2);
                                mCloudLayer1.setVisible(true);
                                mCloudLayer2.setVisible(true);
                            }
                        }.execute();
                    }
                });
            }
            break;
        default:
            break;
        }
    }

    private void processWeather_LoadCloudObject() {
        if (mCloudLayer1 == null) {
            mCloudLayer1 = new SkyObject(getScene(), "cloudy_layer1");
            mCloudLayer1.createCloudy("assets/base/sky/fog.png", SkyObject.LAYER_INDEX_3);
            getScene().getContentObject().addChild(mCloudLayer1, true);
        }

        if (mCloudLayer2 == null) {
            mCloudLayer2 = new SkyObject(getScene(), "cloudy_layer2");
            mCloudLayer2.createCloudy("assets/base/sky/fog.png", SkyObject.LAYER_INDEX_4);
            getScene().getContentObject().addChild(mCloudLayer2, true);
        }
    }

    private void processWeather_ShowWeatherImage() {
        if (mService != null) {
            getScene().handleMessage(HomeScene.MSG_TYPE_SHOW_WEATHER_DIALOG, mService);
        }
    }

    private void processWeather_Unload() {
        if (mSkyObjects != null) {
            for (SkyObject obj : mSkyObjects) {
                getScene().getContentObject().removeChild(obj, true);
            }
            mSkyObjects.clear();
        }
    }

    public void weatherUpdate(final boolean forceUpdate) {
        if (mService == null) {
            return;
        }

        new SECommand(getScene()) {
            @Override
            public void run() {
                if (mService != null) {
                    try {
                        boolean isN = mService.isNight();
                        int type = mService.getConditionType();
                        processWeather_changed(type, isN);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.execute();
    }

    private class UpdateSkyAnimation extends CountAnimation {
        private boolean mNight;
        private int mCount;

        public UpdateSkyAnimation(SEScene scene, boolean isNight) {
            super(scene);
            mNight = isNight;
            mCount = 0;
        }

        public void runPatch(int count) {
            processWeather_OnAnimation(mCurrentType, mNight, count);
            mCount = count;
        }

        public void onFinish() {
            super.onFinish();
            mSkyAnimationCount = mSkyAnimationCount + mCount;
        }

    }

    private void playCloudUVAnimation(int count) {
        float step = ((float) count % 1500f) / 1500f;
        if (mCloudLayer1 != null && mCloudLayer1.isVisible()) {
            SEVector2f dirCloud = new SEVector2f(step, 0);
            mCloudLayer1.playUVAnimation(dirCloud);
        }
        if (mCloudLayer2 != null && mCloudLayer1.isVisible()) {
            step = ((float) count % 3000f) / 3000f;
            SEVector2f dirCloud = new SEVector2f(-step, 0);
            mCloudLayer2.playUVAnimation(dirCloud);
        }
    }

    private int getRandom(int min, int max, int step) {
        int count = (max - min) / step;
        double x = Math.random() * count;
        return (int) (Math.round(x) * step + min);
    }

    private SEVector3f mCameraDirection;
    private SEVector3f mCenterOfFace;
    private SEVector3f mFaceDirection;
//    private float mWallRadius;
//    private float mSkyHeight;
    private double mLayerAngle;

    private void initSkyFacePara() {
        mSkyImgKey = "assets/base/sky/qingtianbogyun.jpg";
        SECameraData cameraData = getObserveCamera();
        mLayerAngle = Math.atan(cameraData.mAxisZ.getZ() / cameraData.mAxisZ.getY());
        float skyHeight = getHomeScene().getHouse().mWallHeight + 100;
        float wallRadius = getHomeScene().getHouse().mWallRadius + 100;
        mFaceDirection = new SEVector3f(0, (float) -Math.sin(mLayerAngle), (float) Math.cos(mLayerAngle));
        mCameraDirection = new SEVector3f(0, (float) Math.cos(mLayerAngle), (float) Math.sin(mLayerAngle));
        SERay rayA = new SERay(new SEVector3f(0, wallRadius, skyHeight), mFaceDirection);
        SERay rayB = new SERay(cameraData.mLocation, mCameraDirection);
        SEVector2f intersect = SERay.rayIntersectRay(rayA, rayB, com.borqs.se.engine.SEVector.AXIS.X);
        mCenterOfFace = new SEVector3f(0, intersect.getX(), intersect.getY());
        setUserTranslate(new SEVector3f(0, 0, skyHeight));
    }

    private float[] calculateScreenSize(SEVector3f centerOfFace) {
        SECameraData cameraData = getObserveCamera();
        float paras = (float) (getScene().getSceneWidth() / (2 * Math.tan(cameraData.mFov * Math.PI / 360)));
        SEVector3f screenLoc = cameraData.mLocation.add(cameraData.mAxisZ.mul(-1).mul(paras));
        float screenToCamera = screenLoc.subtract(cameraData.mLocation).getLength();
        float faceCenterToCamera = mCenterOfFace.subtract(cameraData.mLocation).getLength();
        float screenWidth = faceCenterToCamera * getScene().getSceneWidth() / screenToCamera;
        float screenHeight = faceCenterToCamera * getScene().getSceneHeight() / screenToCamera;
        return new float[] { screenWidth, screenHeight };
    }

    private SEVector3f getDistance(int layerIndex) {
        return mCameraDirection.mul(-layerIndex * 10);
    }

    private enum ObjectType {
        CLOUDY, SUN, MOON, THUNDER, BLANK, FOG
    }

    private class SkyObject extends SEObject {
        private static final int LAYER_INDEX_Particle = -6;
        private static final int LAYER_INDEX_0 = -5;
        private static final int LAYER_INDEX_1 = -4;
        private static final int LAYER_INDEX_2 = -3;
        private static final int LAYER_INDEX_3 = -2;
        private static final int LAYER_INDEX_4 = -1;

        private ObjectType mObjectType;
        private String mImgKey;

        /**
         * 云，太阳，月亮所在的面的中心点
         */

        public SkyObject(SEScene scene, String name) {
            super(scene, name);

        }

        @Override
        public void onRenderFinish(SECamera camera) {
            super.onRenderFinish(camera);
            setTouchable(false);
            mImgKey = getImageName();
        }

        @Override
        public Bitmap onStartLoadImage() {
            switch (mObjectType) {
            case SUN:
                break;
            case MOON:
                break;
            case THUNDER:
                break;
            case BLANK:
                break;
            }
            return null;
        }

        public void onCameraChanged() {
            switch (mObjectType) {
            case SUN:
                break;
            case MOON:
                break;
            case THUNDER:
                break;
            case BLANK:
                SEVector3f cameraYAxis2 = getCamera().getAxisY();
                SEVector2f yAxis2f2 = new SEVector2f(cameraYAxis2.getZ(), cameraYAxis2.getY());
                float angle2 = (float) (180 * yAxis2f2.getAngle() / Math.PI);
                getUserTransParas().mTranslate = getCamera().getScreenLocation(0.2f);
                getUserTransParas().mRotate = new SERotate(-angle2, 1, 0, 0);
                setUserTransParas();
                break;
            }
        }

        public void createSun(String imagePath, int layerIndex) {

            mObjectType = ObjectType.SUN;
            SEVector3f center = mCenterOfFace.add(getDistance(layerIndex));
            float[] screenSize = calculateScreenSize(center);
            float sunSize = Math.min(screenSize[0], screenSize[1]) * 0.8f;
            SERect3D rect = new SERect3D();
            rect.setSize(-65f, -87f, 512, 512, sunSize / 512);
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, imagePath, imagePath, null, null, true);

            float timeValue = getTimeValue();
            float moveX = (12 - timeValue) * screenSize[0] / 12;
            float moveZ = screenSize[1] * 0.5f - sunSize * 0.2f;
            SEVector3f moveCenter = mFaceDirection.mul(moveZ);
            moveCenter.selfAdd(new SEVector3f(moveX, 0, 0));
            getLocalTransParas().mRotate = new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0);
            getLocalTransParas().mTranslate = center.add(moveCenter);
            setLayerIndex(layerIndex);
            setBlendSortAxis(AXIS.Y);
        }

        public void createMoon(String imagePath, int layerIndex, int lunarDay) {
            mObjectType = ObjectType.MOON;
            SEVector3f center = mCenterOfFace.add(getDistance(layerIndex));
            float[] screenSize = calculateScreenSize(center);
            float moonSize = Math.min(screenSize[0], screenSize[1]) * 0.5f;
            SERect3D rect = new SERect3D(moonSize, moonSize);
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, imagePath, imagePath, null, null, true);
            float moveX = (lunarDay - 15) * screenSize[0] / 30;
            float moveZ = screenSize[1] * 0.5f - moonSize * 0.5f;
            SEVector3f moveCenter = mFaceDirection.mul(moveZ);
            moveCenter.selfAdd(new SEVector3f(moveX, 0, 0));
            getLocalTransParas().mRotate = new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0);
            getLocalTransParas().mTranslate = center.add(moveCenter);
            setLayerIndex(layerIndex);
            setBlendSortAxis(AXIS.Y);
        }

        public void createThunder(int layerIndex) {
            mObjectType = ObjectType.THUNDER;
            SEVector3f center = mCenterOfFace.add(getDistance(layerIndex));
            float[] screenSize = calculateScreenSize(center);
            SERect3D rect = new SERect3D(screenSize[1], screenSize[1]);
            String imagePath = "assets/base/sky/thunder" + getRandom(0, 3, 1) + ".png";
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, imagePath, imagePath, null, null, true);
            getLocalTransParas().mTranslate = center;
            getLocalTransParas().mRotate = new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0);
            setLayerIndex(layerIndex);
            setBlendSortAxis(AXIS.Y);
        }

        public void createCloudy(String imagePath, int layerIndex) {
            mObjectType = ObjectType.CLOUDY;
            SEVector3f center = mCenterOfFace.add(getDistance(layerIndex));
            float[] screenSize = calculateScreenSize(center);
            SERect3D rect = new SERect3D(screenSize[0] * 1.5f, screenSize[1]);
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, imagePath, imagePath, null, null, true);
            getLocalTransParas().mRotate = new SERotate((float) (mLayerAngle * 180 / Math.PI), 1, 0, 0);
            getLocalTransParas().mTranslate = center;
            setLayerIndex(layerIndex);
            setBlendSortAxis(AXIS.Y);
            setVisible(false);
        }

        public void createFlash(String imagePath, int layerIndex) {
            mObjectType = ObjectType.BLANK;
            float scale = 0.2f;
            int w = getCamera().getWidth();
            int h = getCamera().getHeight();
            SERect3D rect = new SERect3D();
            rect.setSize(w, h, scale);
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, imagePath, imagePath, null, null, true);
            setLayerIndex(layerIndex);
            setBlendSortAxis(AXIS.Y);
            onCameraChanged();
        }

        private int getTimeValue() {
            Time time = new Time();
            time.setToNow();
            return time.hour;
        }
    }

    public void onCameraChanged() {
        float skyY = getHomeScene().getSightValue();
        if (skyY > 0.5f) {
            show();
        } else {
            hide(null);
            if (mService != null) {
                getScene().handleMessage(HomeScene.MSG_TYPE_DISMIS_WEATHER_DIALOG, mService);
            }
        }
        if (mOnShow) {
            if (mSkyObjects != null) {
                for (SkyObject obj : mSkyObjects) {
                    obj.onCameraChanged();
                }
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        getCamera().removeCameraChangedListener(this);
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.borqs.borqsweather.update")) {
                mForceChange = true;
                weatherUpdate(true);
            }
        }

    };

    @Override
    public void onWeatherServiceBinded(IWeatherService service) {
        mService = service;
        if (mService == null) {
            return;
        }
        try {
            final int currentType = mService.getConditionType();
            final boolean isNight = mService.isNight();
            if (Math.abs(System.currentTimeMillis() - mRequestWeatherTime) > 1000 * 60 * 30) {
                mRequestWeatherTime = System.currentTimeMillis();
                mService.checkToUpdateWeather(1000 * 60);
            }
            new SECommand(getScene()) {
                public void run() {
                    processWeather_changed(currentType, isNight);
                }
            }.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
