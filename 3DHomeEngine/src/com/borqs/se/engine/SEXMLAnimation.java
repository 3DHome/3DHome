package com.borqs.se.engine;


public class SEXMLAnimation extends SECommand {
    private int mNativeAnimation = 0;
    private int mObjectIndex = 0;
    private int mCount = 0;
    private int mAnimationTimes = 0;
    private boolean mIsFinished;;
    private boolean mIsReversed;

    public native int addAnimation_JNI(String xmlPath);

    public native void playAnimation_JNI(int count);

    public native void removeAnimation_JNI();

    public native void setReversePlay_JNI(boolean isReversed);

    public native boolean isNeedRepeat_JNI();

    public SEXMLAnimation(SEScene scene, String xmlPath, int objectIndex) {
        super(scene);
        mObjectIndex = objectIndex;
        mIsReversed = false;
        mIsFinished = false;
        mAnimationTimes = addAnimation_JNI(xmlPath);
    }

    @Override
    public boolean isFinish() {
        return mIsFinished;
    }

    public void setIsReversed(boolean isReversed) {
        if (isReversed != mIsReversed) {
            mCount = 0;
            mIsReversed = isReversed;
            setReversePlay_JNI(isReversed);
        }
    }

    public void pause() {
        mIsFinished = true;
    }

    public void reset() {
        mCount = 0;
    }

    @Override
    public void execute() {
        mIsFinished = false;
        super.execute();
    }

    public int getAnimationCount() {
        return mAnimationTimes;
    }

    public void run() {
        if (!isFinish()) {
            playAnimation_JNI(mCount);
            mCount++;
            if (mCount >= mAnimationTimes) {
                pause();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (mNativeAnimation != 0) {
            removeAnimation_JNI();
        }
        super.finalize();
    }

}
