package com.borqs.se.engine;


public abstract class SECommand {
    public long when;
    private SEEventQueue mSEEventQueue;
    private boolean mIsFinished = false;
    private boolean mIsLazy = false;
    private int mID = -1;

    public SECommand(SEScene scene) {
        if (scene != null) {
            when = 0;
            mSEEventQueue = scene.getEventQuene();
        }
    }

    public SECommand(SEScene scene, int id) {
        if (scene != null) {
            when = 0;
            mSEEventQueue = scene.getEventQuene();
            mID = id;
        }
    }
    
    public int getID() {
        return mID;
    }

    public void stop() {
        mIsFinished = true;
    }

    public boolean isFinish() {
        return mIsFinished;
    }

    /**
     * @param isLazy 指令是否被动运行
     * 被动运行的动画在每一帧都会运行，与主动动画的区别是主动动画会主动申请render screen
     */
    public void setIsLazy(boolean isLazy) {
        mIsLazy = isLazy;
    }

    public boolean isLazy() {
        return mIsLazy;
    }

    public void execute() {
        if (mSEEventQueue != null) {
            mSEEventQueue.addEvent(this);
        }
    }

    public void executeRun() {
        run();
        if (!mIsLazy) {
            mIsFinished = true;
        }
    }

    public abstract void run();
}
