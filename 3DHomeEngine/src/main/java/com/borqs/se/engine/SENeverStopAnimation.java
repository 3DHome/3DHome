package com.borqs.se.engine;

import android.view.animation.Interpolator;


public class SENeverStopAnimation extends SECommand {
    private Interpolator mInterpolator;
    private int mPeriod;

    public SENeverStopAnimation(SEScene scene, int period) {
        super(scene);
        mPeriod = 20 * period;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

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
                mPreTime = System.currentTimeMillis();
                onBegin();
            }
            long currentTime = System.currentTimeMillis();
            long usingTime = currentTime - mPreTime;
            mPreTime = currentTime;
            if (usingTime > 100) {
                usingTime = 100;
            }
            mHasRunTime += usingTime;
            if (!isFinish()) {
                float interpolatorValue = (float) (mHasRunTime % mPeriod) / (float) mPeriod;
                if (mInterpolator != null) {
                    interpolatorValue = mInterpolator.getInterpolation(interpolatorValue);
                }
                float value = interpolatorValue;
                onAnimationRun(value);
            }
            if (isFinish() && mListener != null) {
                mListener.onAnimationfinish();
            }
        }
    }

    public void onAnimationRun(float value) {

    }

}
