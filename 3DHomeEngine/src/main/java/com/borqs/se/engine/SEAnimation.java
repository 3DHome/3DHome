package com.borqs.se.engine;


public class SEAnimation {

    public static abstract class CountAnimation extends SECommand {

        private int mCount = 0;
        private SEAnimFinishListener mListener;
        private boolean mIsFinished = false;
        private boolean mHasBeenExecuted = false;
        private Runnable mExecuteTask = new Runnable() {
            public void run() {
                execute();
            }
        };

        public CountAnimation(SEScene scene) {
            super(scene);
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

        public abstract void runPatch(int count);

        public void onFinish() {

        }

        public int getAnimationCount() {
            return Integer.MAX_VALUE;
        }

        public void onFirstly(int count) {

        }

        public void run() {
            if (!isFinish()) {
                mCount++;
                if (mCount == 1) {
                    onFirstly(mCount);
                }
                if (!isFinish()) {
                    runPatch(mCount);
                }
                if (mCount >= getAnimationCount()) {
                    stop();
                }
                if (isFinish() && mListener != null) {
                    mListener.onAnimationfinish();
                }
            }
        }
    }

    public static abstract class TimeAnimation extends SECommand {

        private SEAnimFinishListener mListener;
        private boolean mIsFinished = false;

        private long mLeftTime;
        private long mStartTime = 0;

        public TimeAnimation(SEScene scene) {
            super(scene);
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
            if (!isFinish()) {
                mIsFinished = true;
                mStartTime = 0;
                onFinish();
            }
        }

        @Override
        public void execute() {
            if (mStartTime == 0) {
                mIsFinished = false;
                super.execute();
            }
        }

        public abstract void runPatch(long leftTime);

        public void onFinish() {

        }

        public long getAnimationTime() {
            return Long.MAX_VALUE;
        }

        public void onFirstly(long leftTime) {

        }

        public void run() {
            if (!isFinish()) {
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                    onFirstly(getAnimationTime());
                }
                mLeftTime = mStartTime + getAnimationTime() - System.currentTimeMillis();
                runPatch(mLeftTime);
                if (mLeftTime <= 0) {
                    stop();
                }
                if (isFinish() && mListener != null) {
                    mListener.onAnimationfinish();
                }
            }
        }
    }
}
