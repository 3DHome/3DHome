package com.borqs.se.engine;

import android.view.animation.Interpolator;


public class SEEmptyAnimation extends SECommand {
    private float mFrom;
    private float mTo;
    private Interpolator mInterpolator;

    private long mPreTime;
    private long mHasRunTime;

    private int mCount = 0;
    private int mAnimationTimes = Integer.MAX_VALUE;
    private long mAnimationTime = Long.MAX_VALUE;
    private SEAnimFinishListener mListener;
    private boolean mIsFinished = false;
    private boolean mHasBeenExecuted = false;
    private boolean mHasBeenRun = false;
    private Runnable mExecuteTask = new Runnable() {
        public void run() {
            execute();
        }
    };

    public SEEmptyAnimation(SEScene scene, float from, float to, int times) {
        super(scene);
        mFrom = from;
        mTo = to;
        mHasRunTime = 0;
        if (times != mAnimationTimes) {
            setAnimationTimes(times);
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setAnimationTimes(int times) {
        mAnimationTimes = times;
        if (mAnimationTimes == 0) {
            mAnimationTimes = 1;
        }
        mAnimationTime = mAnimationTimes * SESceneManager.TIME_ONE_FRAME;
    }

    public void setAnimFinishListener(SEAnimFinishListener listener) {
        mListener = listener;
    }

    public SEAnimFinishListener getAnimFinishListener() {
        return mListener;
    }

    @Override
    public boolean isFinish() {
        return mIsFinished;
    }

    @Override
    public void stop() {
        SELoadResThread.getInstance().cancel(mExecuteTask);
        if (!isFinish()) {
            mIsFinished = true;
            mCount = 0;
            onFinish();
        }
    }

    @Override
    public void execute() {
        if (mCount == 0) {
            mHasBeenExecuted = true;
            mIsFinished = false;
            super.execute();
        }
    }

    public boolean hasBeenExecuted() {
        return mHasBeenExecuted;
    }

    public void executeDelayed(long delay) {
        SELoadResThread.getInstance().cancel(mExecuteTask);
        SELoadResThread.getInstance().process(mExecuteTask, delay);
    }

    public void onFinish() {

    }

    public int getAnimationTimes() {
        return mAnimationTimes;
    }

    public long getAnimationTime() {
        return mAnimationTime;
    }

    public void onBegin() {

    }

    public void run() {
        if (!isFinish()) {
            if (!mHasBeenRun) {
                mHasBeenRun = true;
                if (SESceneManager.USING_TIME_ANIMATION) {
                    mPreTime = System.currentTimeMillis();
                }
                onBegin();
            }
            long usingTime;
            if (SESceneManager.USING_TIME_ANIMATION) {
                long currentTime = System.currentTimeMillis();
                usingTime = currentTime - mPreTime;
                mPreTime = currentTime;
                if (usingTime > 100) {
                    usingTime = 100;
                }
            } else {
                usingTime = (long) (SESceneManager.TIME_ONE_FRAME / SESceneManager.getInstance().getAnimationSpeed());
            }
            mHasRunTime += usingTime;
            if (mHasRunTime > mAnimationTime) {
                mHasRunTime = mAnimationTime;
            }

            if (!isFinish()) {
                float interpolatorValue = (float) mHasRunTime / (float) mAnimationTime;
                if (mInterpolator != null) {
                    interpolatorValue = mInterpolator.getInterpolation(interpolatorValue);
                }
                float distance = mTo - mFrom;
                float value = mFrom + distance * interpolatorValue;
                onAnimationRun(value);
            }
            if (mHasRunTime >= mAnimationTime) {
                stop();
            }
            if (isFinish() && mListener != null) {
                mListener.onAnimationfinish();
            }
        }
    }

    public void onAnimationRun(float value) {

    }
}
