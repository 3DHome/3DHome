package com.borqs.se.widget3d;

import java.util.TimeZone;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;

import com.borqs.se.engine.SEComponentAttribute;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETranslateAnimation;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeManager.TimeChangeCallBack;

public class Clock extends NormalObject implements TimeChangeCallBack {

    private static final String TAG = "clock";
    private SEObject mHourHand;
    private SEObject mMinuteHand;
    private SEObject mSecondHand;
    private Time mTime;
    private float mHours;
    private float mMinutes;
    private float mSeconds;
    private boolean mOnShow;
    private boolean mDisableTouch;
    private SETranslateAnimation mShowAnimation;
    private SETranslateAnimation mHideAnimation;
    private CloneClock mCloneClock;

    public Clock(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mTime = new Time();
    }

    @Override
    public void initStatus() {
        super.initStatus();
        mHourHand = findComponenetObjectByRegularName("hour_hand");
        mMinuteHand = findComponenetObjectByRegularName("min_hand");
        mSecondHand = findComponenetObjectByRegularName("sec_hand");
        mSecondHand.setVisible(false);
        HomeManager.getInstance().addTimeCallBack(this);
        setOnClickListener(new OnTouchListener() {
            public void run(SEObject obj) {
                show();
            }
        });
    }

    @Override
    public void onRelease() {
        super.onRelease();
        HomeManager.getInstance().removeTimeCallBack(this);
    }

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            HomeManager.getInstance().removeTimeCallBack(this);
        }
    }

    private void setToCurrentTime(boolean post) {
        if (mHourHand == null || mMinuteHand == null || mSecondHand == null) {
            Log.e(TAG, "hands have not be initialized");
            return;
        }
        updateCurrentTime();
        updateClockModel(post);
    }

    private void updateCurrentTime() {
        mTime.setToNow();
        if (HomeUtils.DEBUG)
            Log.i(TAG, "set clock to current time: " + mTime.toString());

        int hour = mTime.hour;
        int minute = mTime.minute;
        int second = mTime.second;
        mMinutes = minute + second / 60.0f;
        mHours = hour + mMinutes / 60.0f;
        mMinutes = mMinutes % 60;
        mHours = mHours % 12;
        mSeconds = second % 60.0f;
    }

    private void updateClockModel(boolean post) {

        final float hourDegree = mHours / 12.0f * 360.0f;
        final float minuteDegree = mMinutes / 60.0f * 360.0f;
        final float secondDegree = mSeconds / 60.0f * 360.0f;
        final Runnable task = new Runnable() {
            public void run() {
                mHourHand.setUserRotate(new SERotate(hourDegree, 0, 1, 0));
                mMinuteHand.setUserRotate(new SERotate(minuteDegree, 0, 1, 0));
                mSecondHand.setUserRotate(new SERotate(secondDegree, 0, 1, 0));
            }
        };
        if (post) {
            new SECommand(getHomeScene()) {
                public void run() {
                    task.run();
                }
            }.execute();
        } else {
            task.run();
        }
    }

    private void show() {
        if (!mOnShow) {
            stopAllAnimation(null);
            mOnShow = true;
            mDisableTouch = true;
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, true);
            loadCloneClock(getAbsoluteTranslate());
            setVisible(false);
            getHomeScene().setTouchDelegate(mCloneClock);
            House house;
            if (getParent().getParent() instanceof House) {
                house = (House) getParent().getParent();
            } else {
                house = getHomeScene().getHouse();
            }
            float clockW = (house.mCellWidth + house.mWidthGap) * getObjectInfo().getSpanX() - house.mWidthGap;
            // Clock will take 2/3 of screen width;
            SEVector3f desLocation = getCamera().getScreenLocation(clockW * 1.5f / getCamera().getWidth());
            mShowAnimation = new SETranslateAnimation(getHomeScene(), mCloneClock, mCloneClock.getUserTranslate().clone(),
                    desLocation, 15);
            mShowAnimation.setInterpolator(new OvershootInterpolator());
            mShowAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDisableTouch = false;
                }
            });
            mShowAnimation.execute();
        }
    }

    private void loadCloneClock(SEVector3f location) {
        ObjectInfo objInfo = getObjectInfo();
        mCloneClock = new CloneClock(getHomeScene(), objInfo.mName, 1001);
        HomeManager.getInstance().getModelManager().createQuickly(getHomeScene().getContentObject(), mCloneClock);
        mCloneClock.setUserTranslate(location);
        mCloneClock.setIsEntirety(true);
        getHomeScene().getContentObject().addChild(mCloneClock, false);
        mCloneClock.init();
    }

    private void unloadCloneClock() {
        SEObject root = getHomeScene().getContentObject();
        root.removeChild(mCloneClock, true);
        mCloneClock.stopWork();
        HomeManager.getInstance().getModelManager().unRegister(mCloneClock);
    }

    private void hide(boolean fast, final SEAnimFinishListener l) {
        if (mOnShow) {
            mDisableTouch = true;
            stopAllAnimation(null);
            if (fast) {
                unloadCloneClock();
                setVisible(true);
                mDisableTouch = false;
                mOnShow = false;
                getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                getHomeScene().removeTouchDelegate();
                if (l != null) {
                    l.onAnimationfinish();
                }
            } else {
                mHideAnimation = new SETranslateAnimation(getHomeScene(), mCloneClock, mCloneClock.getUserTranslate()
                        .clone(), getAbsoluteTranslate(), 10);
                mHideAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                    public void onAnimationfinish() {
                        unloadCloneClock();
                        setVisible(true);
                        mDisableTouch = false;
                        mOnShow = false;
                        getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                        getHomeScene().removeTouchDelegate();
                        if (l != null) {
                            l.onAnimationfinish();
                        }
                    }
                });
                mHideAnimation.execute();
            }
        }
    }

    @Override
    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mShowAnimation != null) {
            mShowAnimation.stop();
        }
        if (mHideAnimation != null) {
            mHideAnimation.stop();
        }
        super.stopAllAnimation(l);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDisableTouch) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mOnShow) {
            hide(false, l);
        }
        return false;
    }

    public void onTimeChanged() {
        mTime.switchTimezone(TimeZone.getDefault().getID());
        mTime.setToNow();
        setToCurrentTime(true);
    }

    public class CloneClock extends SEObject implements TimeChangeCallBack {
        private static final int RUN_SECOND = 0;
        private SEObject mHourHand;
        private SEObject mMinuteHand;
        private SEObject mSecondHand;
        private Time mTime;
        private float mHours;
        private float mMinutes;
        private float mSeconds;
        private Handler mHandler;
        private HandlerThread mHandlerThread;

        public CloneClock(SEScene scene, String name, int index) {
            super(scene, name, index);
            setIsGroup(true);
        }

        private void init() {
            SEComponentAttribute componentAttribute = getObjectInfo().mModelInfo
                    .findComponentAttrByRegularName("hour_hand");
            mHourHand = new SEObject(getHomeScene(), componentAttribute.mComponentName, mIndex);
            mHourHand.setHasBeenAddedToEngine(true);
            componentAttribute = getObjectInfo().mModelInfo.findComponentAttrByRegularName("min_hand");
            mMinuteHand = new SEObject(getHomeScene(), componentAttribute.mComponentName, mIndex);
            mMinuteHand.setHasBeenAddedToEngine(true);
            componentAttribute = getObjectInfo().mModelInfo.findComponentAttrByRegularName("sec_hand");
            mSecondHand = new SEObject(getHomeScene(), componentAttribute.mComponentName, mIndex);
            mSecondHand.setHasBeenAddedToEngine(true);
            mSecondHand.setVisible(true);
            mTime = new Time();
            HomeManager.getInstance().addTimeCallBack(this);
            mHandlerThread = new HandlerThread("Clock.update", Process.THREAD_PRIORITY_DEFAULT);
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case RUN_SECOND:
                        mHours += 1 / 3600f;
                        mMinutes += 1 / 60f;
                        mSeconds += 1;
                        mSeconds = mSeconds % 60;
                        updateClockModel(true);
                        removeMessages(RUN_SECOND);
                        sendEmptyMessageDelayed(RUN_SECOND, 1000);
                        break;
                    }
                }
            };
            mHandler.removeMessages(RUN_SECOND);
            mHandler.sendEmptyMessageDelayed(RUN_SECOND, 1000);
            setOnClickListener(new OnTouchListener() {
                public void run(SEObject obj) {
                    if (getHomeScene().getDownHitObject().equals(CloneClock.this)) {
                        handOnClick();
                    } else {
                        hide(false, null);
                    }
                }
            });
        }

        @Override
        public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
            super.onSceneChanged(changeType);
            if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
                HomeManager.getInstance().removeTimeCallBack(this);
            }
        }

        @Override
        public void onRelease() {
            super.onRelease();
            HomeManager.getInstance().removeTimeCallBack(this);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (mDisableTouch) {
                return true;
            }
            return super.dispatchTouchEvent(event);
        }

        public void stopWork() {
            mHandler.removeMessages(RUN_SECOND);
            mHandlerThread.quit();
        }

        public void onTimeChanged() {
            updateCurrentTime();
            updateClockModel(true);
        }

        private void updateCurrentTime() {
            mTime.switchTimezone(TimeZone.getDefault().getID());
            mTime.setToNow();
            int hour = mTime.hour;
            int minute = mTime.minute;
            int second = mTime.second;
            mMinutes = minute + second / 60.0f;
            mHours = hour + mMinutes / 60.0f;
            mMinutes = mMinutes % 60;
            mHours = mHours % 12;
            mSeconds = second % 60.0f;
        }

        private void updateClockModel(boolean post) {

            final float hourDegree = mHours / 12.0f * 360.0f;
            final float minuteDegree = mMinutes / 60.0f * 360.0f;
            final float secondDegree = mSeconds / 60.0f * 360.0f;
            final Runnable task = new Runnable() {
                public void run() {
                    mHourHand.setUserRotate(new SERotate(hourDegree, 0, 1, 0));
                    mMinuteHand.setUserRotate(new SERotate(minuteDegree, 0, 1, 0));
                    mSecondHand.setUserRotate(new SERotate(secondDegree, 0, 1, 0));

                }
            };
            if (post) {
                new SECommand(getHomeScene()) {
                    public void run() {
                        task.run();
                    }
                }.execute();
            } else {
                task.run();
            }
        }
    }
}
