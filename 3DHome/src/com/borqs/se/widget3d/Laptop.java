package com.borqs.se.widget3d;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.BounceInterpolator;

import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.home3d.HomeScene;

public class Laptop extends NormalObject{
    private final static float CLOSE_SHELL_ANGLE = 115;
    private SEObject mStand;
    private SEObject mScreen;
    private float mCurrentShellAngle;
    private boolean mCancelClick;
    private OpenOrCloseAnimation mOpenOrCloseAnimation;
    private VelocityTracker mVelocityTracker;
    private boolean mCanOpenAndClose = true;

    public Laptop(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mCurrentShellAngle = 0;
        mCancelClick = false;
        if (name.equals("group_pc")) {
            mCanOpenAndClose = true;
        } else {
            mCanOpenAndClose = false;
        }
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        if (mCanOpenAndClose) {
            mScreen = findComponenetObjectByRegularName("screen");
            mStand = findComponenetObjectByRegularName("pillar");
            mStand.setLeader(mScreen);
        }
        setOnClickListener(new SEObject.OnTouchListener() {
            public void run(SEObject obj) {
                if (mCurrentShellAngle < 5) {
                    getHomeScene().showAllApps();
                } else {
                    runOpenOrCloseAnimation(new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            getHomeScene().showAllApps();
                        }
                    }, 5, true);
                }
            }
        });
    }

    @Override
    public void onRelease() {
        super.onRelease();
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    private float mStartX, mStartY;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mCanOpenAndClose) {
            return super.onTouchEvent(event);
        }
        trackVelocity(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPreTouch();
            mCancelClick = false;
            mStartX = event.getX();
            mStartY = event.getY();
            setPressed(true);
            return super.onTouchEvent(event);
        case MotionEvent.ACTION_MOVE:
            if (mOpenOrCloseAnimation != null) {
                mOpenOrCloseAnimation.stop();
            }
            
//            if (!mCancelClick && ViewConfiguration.get(getContext()).getScaledTouchSlop() >= Math
//                    .abs(event.getX() - mStartY)) {
//                return super.onTouchEvent(event);
//            }
            
            float changeY = 0;
            float changeX = 0;
            if (!mCancelClick) {
                mCancelClick = true;
                changeY = -getTouchY() + mStartY;
                changeX = mStartX - getPreTouchX();
                MotionEvent e = MotionEvent.obtain(event);
                e.setAction(MotionEvent.ACTION_CANCEL);
                super.onTouchEvent(e);
            } else {
                changeY = -getTouchY() + getPreTouchY();
                changeX = getTouchX() - getPreTouchX();
            }
            double angle = (getParent().getUserRotate().getAngle() - 90) * Math.PI / 180;
            float change = (float) (changeX * Math.cos(angle) + changeY * Math.sin(angle));
            mCurrentShellAngle = mCurrentShellAngle + change / 2;
            if (mCurrentShellAngle < 0) {
                mCurrentShellAngle = 0;
            } else if (mCurrentShellAngle > CLOSE_SHELL_ANGLE) {
                mCurrentShellAngle = CLOSE_SHELL_ANGLE;
            }
            setShellAngle(mCurrentShellAngle);
            setPreTouch();
            break;
        case MotionEvent.ACTION_UP:
            setPreTouch();
            setPressed(false);
            if (!mCancelClick) {
                super.onTouchEvent(event);
                if (!isOnMove()) {
                    performClick();
                    return true;
                }
            }

        case MotionEvent.ACTION_CANCEL:
            setPressed(false);
            boolean isOpen = true;
            float vY = -mVelocityTracker.getYVelocity();
            float vX = mVelocityTracker.getXVelocity();
            double deskAngle = (getParent().getUserRotate().getAngle() - 90) * Math.PI / 180;
            float changeV = (float) (vX * Math.cos(deskAngle) + vY * Math.sin(deskAngle));
            if (changeV < -500) {
                isOpen = true;
            } else if (changeV > 500) {
                isOpen = false;
            } else {
                if (mCurrentShellAngle < CLOSE_SHELL_ANGLE / 2) {
                    isOpen = true;
                } else {
                    isOpen = false;
                }
            }
            runOpenOrCloseAnimation(null, 5, isOpen);
            break;
        }
        return true;
    }

    public void setClose() {
        if (mCurrentShellAngle != CLOSE_SHELL_ANGLE) {
            if (mOpenOrCloseAnimation != null) {
                mOpenOrCloseAnimation.stop();
            }
            setShellAngle(CLOSE_SHELL_ANGLE);
        }
    }

    private void setShellAngle(float angle) {
        mCurrentShellAngle = angle;
        mScreen.setUserRotate(new SERotate(angle, 1, 0, 0));
        mStand.setUserRotate(new SERotate(-angle / 2.6f, 1, 0, 0));
    }

    private void runOpenOrCloseAnimation(SEAnimFinishListener listener, float step, boolean openOrClose) {
        if (mOpenOrCloseAnimation != null) {
            mOpenOrCloseAnimation.stop();
        }
        mOpenOrCloseAnimation = new OpenOrCloseAnimation(getScene(), step, openOrClose);
        mOpenOrCloseAnimation.setAnimFinishListener(listener);
        mOpenOrCloseAnimation.execute();
    }

    class OpenOrCloseAnimation extends CountAnimation {
        private float mShellStartAngle;
        private float mShellRotateStep;
        private int mAnimCount;
        private BounceInterpolator mBounceInterpolator;
        private boolean mIsOpen;

        public OpenOrCloseAnimation(SEScene scene, float step, boolean openOrClose) {
            super(scene);
            mIsOpen = openOrClose;
            if (mIsOpen) {
                mAnimCount = (int) (mCurrentShellAngle / step);
                if (mAnimCount != 0) {
                    mBounceInterpolator = new BounceInterpolator();
                    mShellStartAngle = mCurrentShellAngle;
                    mShellRotateStep = -mCurrentShellAngle / mAnimCount;
                }
            } else {
                mAnimCount = (int) ((CLOSE_SHELL_ANGLE - mCurrentShellAngle) / step);
                if (mAnimCount != 0) {
                    mShellStartAngle = mCurrentShellAngle;
                    mShellRotateStep = (CLOSE_SHELL_ANGLE - mCurrentShellAngle) / mAnimCount;
                }
            }
        }

        @Override
        public void runPatch(int count) {
            if (mIsOpen) {
                int newCount = (int) (mAnimCount * mBounceInterpolator.getInterpolation((float) count / mAnimCount));
                if (count == mAnimCount) {
                    mCurrentShellAngle = 0;
                    stop();
                } else {
                    mCurrentShellAngle = mShellStartAngle + newCount * mShellRotateStep;
                }
            } else {
                if (count == mAnimCount) {
                    mCurrentShellAngle = CLOSE_SHELL_ANGLE;
                    stop();
                } else {
                    mCurrentShellAngle = mShellStartAngle + count * mShellRotateStep;
                }
            }
            setShellAngle(mCurrentShellAngle);
        }

        @Override
        public void onFirstly(int count) {
            if (mAnimCount == 0) {
                stop();
            }
        }

        @Override
        public int getAnimationCount() {
            return mAnimCount;
        }

    }
}
