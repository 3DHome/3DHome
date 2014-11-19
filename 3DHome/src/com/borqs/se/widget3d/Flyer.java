package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.RemoteException;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import android.view.MotionEvent;

import com.borqs.borqsweather.weather.IWeatherService;
import com.borqs.borqsweather.weather.WeatherConditions;
import com.borqs.se.R;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETranslateAnimation;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.SettingsActivity;

public class Flyer extends NormalObject {   
    private SETranslateAnimation mMoveFlyerAnimation;
    private HomeManager mSESceneManager;
    private SEObject mBanner;
    private SEObject mBody;
    private boolean mShakeBody = true;
    private float mBodyRotateAngle;
    private List<SEObject> mWindSticks;
    private float mWindSticksRotateAngle;
    private ADViewController mController;
    private boolean mADOnShow = false;
    private String mBannerImageName;
    private float mSpeed = 2;
    private boolean mReversed;
    private int mStopTaskID;
    private float mSizeXOfFlyer;

    public Flyer(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mSESceneManager = HomeManager.getInstance();
        mController = ADViewController.getInstance();
        setClickable(true);
        setPressType(PRESS_TYPE.COLOR);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        if (isFresh()) {
            return;
        }
        mStopTaskID = (int) System.currentTimeMillis();
        setOnLongClickListener(null);
        setOnClickListener(new OnTouchListener() {
            public void run(SEObject obj) {
                if (mHasGivenMoneyToUs || mSESceneManager.withoutAD()) {
                    return;
                }
                if (!mADOnShow) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://search?q=pname:com.borqs.selockscreen"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mSESceneManager.startActivity(intent);
                    return;
                }
                SEVector3f vector = getSelectPoint(getTouchX(), getTouchY());
                if (vector != null) {
                    float x = vector.getX();
                    float z = vector.getZ();
                    if (mReversed) {
                        if ((x < 530f && x > 450f) && (z < 60f && z > -10f)) {
                            HomeUtils.gotoAdRemovalActivity();
                            return;
                        }
                    } else {
                        if ((x < -35f && x > -150f) && (z < 60f && z > -10f)) {
                            HomeUtils.gotoAdRemovalActivity();
                            return;
                        }
                    }
                }
                mController.doClick();
            }
        });
        initObject();
        setVisible(false);
    }

    @Override
    public void onActivityPause() {
        if (!mHasGivenMoneyToUs) {
            mController.stopCatchImage();
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        mController.notifyFinish();
    }

    @Override
    public void onActivityResume() {
        if (!mOnMovingFlyer || getHomeScene().getSightValue() != 1) {
            return;
        }
        if (!mHasGivenMoneyToUs) {
            mController.requestCatchImage(500);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            if (checkOutside((int) event.getX(), (int) event.getY())) {
                setPressed(false);
            }
        }
        return super.onTouchEvent(event);
    }

    private void onStartAnimation() {
        IWeatherService service = HomeManager.getInstance().getWeatherService();
        if (service != null) {
            int currentType;
            try {
                currentType = service.getConditionType();
                if (currentType == WeatherConditions.CONDITION_TYPE_FOG) {
                    setNeedFog(true);
                } else {
                    setNeedFog(false);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!mHasGivenMoneyToUs) {
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    mController.loadAD();
                    if (mController.hasLoadedADMod()) {
                        mADOnShow = true;
                        mController.setImageName(mBannerImageName);
                        mController.setImageSize(492, 80);
                        mController.requestCatchImage(200);
                    }
                }
            });
        }
    }


    private boolean mOnMovingFlyer = false;

    public void playAnimation(final float speed) {
        if (!mOnMovingFlyer) {
            mOnMovingFlyer = true;
            final boolean reversed;
            if (Math.random() > 0.5) {
                reversed = false;
            } else {
                reversed = true;
            }
            mReversed = reversed;
            onStartAnimation();
            playAnimation(speed, reversed, new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mReversed = !reversed;
                    playAnimation(speed, !reversed, new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            setVisible(false);
                            mOnMovingFlyer = false;
                            mController.notifyFinish();
                        }
                    });
                }

            });

        }
    }

    private void playAnimation(float speed, boolean reversed, SEAnimFinishListener l) {
        float skyHeight;
        float y;
        int animCount;
        if (HomeManager.getInstance().isLandscapeOrientation()) {
            skyHeight = getHomeScene().getHouse().mWallHeight + 480;
            y = (float) (Math.random() * 200) + getHomeScene().getHouse().mWallRadius * 0.3f;
            animCount = (int) (2500 / speed);
        } else {
            skyHeight = getHomeScene().getHouse().mWallHeight + 260;
            y = (float) (Math.random() * 100) + getHomeScene().getHouse().mWallRadius * 0.15f;
            animCount = (int) (2000 / speed);
        }
        SEVector3f beginLoc;
        SEVector3f endLoc;
        if (reversed) {
            mBanner.setTexCoordXYReverse_JNI(true, false);
            setUserRotate(new SERotate(180, 0, 0, 1));
            beginLoc = new SEVector3f(-mSizeXOfFlyer, y, skyHeight);
            endLoc = new SEVector3f(mSizeXOfFlyer, y, skyHeight);
        } else {
            mBanner.setTexCoordXYReverse_JNI(false, false);
            setUserRotate(new SERotate(0, 0, 0, 1));
            beginLoc = new SEVector3f(mSizeXOfFlyer, y, skyHeight);
            endLoc = new SEVector3f(-mSizeXOfFlyer, y, skyHeight);
        }
        mWindSticksRotateAngle = 0;
        mBodyRotateAngle = 0;
        mMoveFlyerAnimation = new SETranslateAnimation(getScene(), this, beginLoc, endLoc, animCount) {

            @Override
            public void onBegin() {
                setVisible(true);
            }

            @Override
            public void onAnimationRun(float value) {
                super.onAnimationRun(value);
                if (mShakeBody && mBody != null) {
                    mBodyRotateAngle = mBodyRotateAngle + 2;
                    mBody.setUserRotate(new SERotate((float) (10 * Math.sin(mBodyRotateAngle * Math.PI / 180)), 1, 0, 0));
                }
                if (mWindSticks != null) {
                    mWindSticksRotateAngle = mWindSticksRotateAngle + 30;
                    for (SEObject windStick : mWindSticks) {
                        windStick.setUserRotate(new SERotate(mWindSticksRotateAngle, 1, 0, 0));
                    }
                }
            }

        };
        mMoveFlyerAnimation.setAnimFinishListener(l);
        mMoveFlyerAnimation.execute();
    }

    private boolean mHasGivenMoneyToUs = false;

    public void setHasGivenMoneyToUs(boolean flag) {
        mHasGivenMoneyToUs = flag;
        if (mHasGivenMoneyToUs) {
            SELoadResThread.getInstance().process(new Runnable() {

                @Override
                public void run() {
                    Bitmap myCustomBitmp = Bitmap.createBitmap(512, 128, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(myCustomBitmp);
                    canvas.drawColor(Color.BLUE);
                    TextPaint paint = new TextPaint();
                    paint.setFakeBoldText(true);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(30f);
                    String text = SettingsActivity.getAdContent(getContext());
                    if (TextUtils.isEmpty(text)) {
                        text = getContext().getString(R.string.ad_default_blessings);
                    }
                    StaticLayout titleLayout = new StaticLayout(text, paint, 512, Alignment.ALIGN_CENTER, 1f, 0.1f,
                            false);
                    canvas.translate(0, (128 - titleLayout.getHeight()) / 2);
                    titleLayout.draw(canvas);
                    final int imageData = SEObject.loadImageData_JNI(myCustomBitmp);
                    myCustomBitmp.recycle();
                    new SECommand(getScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mBannerImageName, mBannerImageName);
                            SEObject.addImageData_JNI(mBannerImageName, imageData);
                        }
                    }.execute();
                }

            }, 250);

        }
    }

    public void fly(boolean hasGivenMoneyToUs) {
        cancelStopFly();
        setHasGivenMoneyToUs(hasGivenMoneyToUs);
        playAnimation(mSpeed);
    }

    public void stopFlyDelayed(long delayed) {
        cancelStopFly();
        SELoadResThread.getInstance().process(mStopFlyTask, delayed);
    }

    public void cancelStopFly() {
        getScene().removeMessage(mStopTaskID);
        SELoadResThread.getInstance().cancel(mStopFlyTask);
    }

    private Runnable mStopFlyTask = new Runnable() {
        public void run() {
            new SECommand(getScene(), mStopTaskID) {
                public void run() {
                    if (mOnMovingFlyer) {
                        if (mMoveFlyerAnimation != null) {
                            mMoveFlyerAnimation.stop();
                        }
                        mController.notifyFinish();
                    }
                }
            }.execute();
        }
    };

    private void initObject() {
        SEVector3f sizeOfFlyer = getObjectInfo().mMaxPoint.subtract(getObjectInfo().mMinPoint);
        float wallWidth = getHomeScene().getHouse().mWallWidth;
        if (HomeManager.getInstance().isLandscapeOrientation()) {
            mSizeXOfFlyer = 1.1f * wallWidth;
        } else {
            mSizeXOfFlyer = 1.38f * wallWidth;
        }
        float scale = mSizeXOfFlyer / sizeOfFlyer.getX();
        setUserScale(new SEVector3f(scale, scale, scale));
        if (!fixBug()) {
            mBody = findComponenetObjectByRegularName("body");
            mWindSticks = findComponenetObjectsByRegularName("windStick");
        }
        mBanner = findComponenetObjectByRegularName("banner");
        mBannerImageName = mBanner.getImageName();
        String speedConfig = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "speed");
        if (!TextUtils.isEmpty(speedConfig)) {
            mSpeed = Float.parseFloat(speedConfig);
        }
        String shakeBody = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "shakeBody");
        if (!TextUtils.isEmpty(shakeBody)) {
            mShakeBody = shakeBody.equals("true") ? true : false;
        }
    }

    private boolean fixBug() {
        if (mName.equals("airplanefighter")) {
            mBody = new SEObject(getScene(), "mainairplanefighter@airplanefighter", mIndex);
            mBody.setHasBeenAddedToEngine(true);
            mWindSticks = new ArrayList<SEObject>();
            SEObject windStick = new SEObject(getScene(), "airplane01_lxj01@mainairplanefighter@airplanefighter",
                    mIndex);
            windStick.setHasBeenAddedToEngine(true);
            mWindSticks.add(windStick);
            windStick = new SEObject(getScene(), "airplane01_lxj02@mainairplanefighter@airplanefighter", mIndex);
            windStick.setHasBeenAddedToEngine(true);
            mWindSticks.add(windStick);
            return true;
        }
        return false;
    }

}
