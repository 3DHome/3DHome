#ifndef SE_ANIMATION_H
#define SE_ANIMATION_H
#include "SE_Time.h"
#include "SE_ID.h"
#include<vector>
class SE_Interpolate;
class SE_AnimationListener;

class SE_ENTRY SE_Animation
{
public:
    enum RUN_MODE {NOT_REPEAT, REPEAT, ONE_FRAME, REVERSE_NOT_REPEAT, REVERSE_REPEAT, REVERSE_ONE_FRAME};
    enum TIME_MODE {REAL, SIMULATE};
    enum ANIM_STATE{RUNNING, END, PAUSE};
    enum PLAY_MODE{CPU_NO_SHADER,GPU_SKELETON_SHADER};
    SE_Animation();
    virtual ~SE_Animation();
    void run(int cloneIndex = 0);
    void pause();
    void restore();
    void end();
    void nextFrame(SE_TimeMS realDelta, SE_TimeMS simulateDeltaTime);
    void setDuration(SE_TimeMS duration)
    {
        mDuration = duration;
    }
    ANIM_STATE getAnimState()
    {
        return mAnimState;
    }
    SE_TimeMS getDuration()
    {
        return mDuration;
    }
    void setInterpolate(SE_Interpolate* interpolate)
    {
        mInterpolate = interpolate;
    }
    SE_Interpolate* getInterpolate()
    {
        return mInterpolate;
    }
    void setRunMode(RUN_MODE m)
    {
        mRunMode = m;
    }
    RUN_MODE getRunMode()
    {
        return mRunMode;
    }
    void setTimeMode(TIME_MODE tmode)
    {
        mTimeMode = tmode;
    }
    TIME_MODE getTimeMode()
    {
        return mTimeMode;
    }
    SE_TimeMS getPassedTime()
    {
        return mPassedTime;
    }
    bool isEnd()
    {
        return mAnimState == END;
    }
    bool isRunning()
    {
        return mAnimState == RUNNING;
    }
    bool isPause()
    {
        return mAnimState == PAUSE;
    }
    void setSpatialID(const SE_SpatialID& spatialID)
    {
        mSpatialID = spatialID;
    }
    void setPrimitiveID(const SE_PrimitiveID& primitiveID)
    {
        mPrimitiveID = primitiveID;
    }
    void setSimObjectID(const SE_SimObjectID& simObjectID)
    {
        mSimObjectID = simObjectID;
    }
    SE_SpatialID getSpatialID()
    {
        return mSpatialID;
    }
    SE_PrimitiveID getPrimitiveID()
    {
        return mPrimitiveID;
    }
    SE_SimObjectID getSimObjectID()
    {
        return mSimObjectID;
    }
    void setFrameNum(int frameNum)
    {
        mFrameNum = frameNum;
    }
    int getFrameNum()
    {
        return mFrameNum;
    }
    SE_TimeMS getTimePerFrame()
    {
        return mTimePerFrame;
    }
    void setTimePerFrame(SE_TimeMS t)
    {
        mTimePerFrame = t;
    }
    int getCurrentFrame()
    {
        return mCurrFrame;
    }
    void reset()
    {
        mPassedTime = 0;
        mCurrFrame = -1;
    }

    void setPlayMode(PLAY_MODE t)
    {
        mPlayMode = t;
    }
    PLAY_MODE getPlayMode()
    {
        return mPlayMode;
    }

    void addListener(SE_AnimationListener* l)
    {
        mListeners.push_back(l);
    }
    SE_AnimationListener* getListener(int index)
    {
        if(index >= 0 && index < mListeners.size())
        {
            return mListeners[index];
        }        
        return NULL;
    }
    void setAnimationID(const char* id)
    {
        mAnimationID = id;
    }
    const char* getAnimationID()
    {
        return mAnimationID.getStr();
    }
    static SE_Animation* inflate(const char* xmlPath);

    void setSpeed(float s)
    {
        mSpeed = s;
    }
    float getSpeed()
    {
        return mSpeed;
    }

    void setRequiredSystemFrameRate(float f)
    {
        mSystemFrameRate = f;
    }
    float getRequiredSystemFrameRate()
    {
        return mSystemFrameRate;
    }
    void setReversePlay(bool reverse = false);
public:
    virtual void update(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    virtual void onRun(int index = 0);
    virtual void onPause();
    virtual void onEnd();
    virtual void onRestore();
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual SE_Animation* clone();
    virtual void playFrameIndex(int frameIndex);
protected:
    void setAnimState(ANIM_STATE as)
    {
        mAnimState = as;
    }
    void setPassedTime(SE_TimeMS passt)
    {
        mPassedTime = passt;
    }
    void setCurrentFrame(int f)
    {
        mCurrFrame = f;
    }
private:
    void oneFrame(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    void resetAnimation();
private:
    ANIM_STATE mAnimState;
    RUN_MODE mRunMode;
    TIME_MODE mTimeMode;
    SE_TimeMS mDuration;
    SE_TimeMS mPassedTime;
    SE_Interpolate* mInterpolate;
    SE_SpatialID mSpatialID;
    SE_PrimitiveID mPrimitiveID;
    SE_SimObjectID mSimObjectID;
   
    
    SE_AnimationID mAnimationID;
    int mFrameNum;
    int mBackupFrameNum;
    int mCurrFrame;
    SE_TimeMS mTimePerFrame;

    float mSpeed;
    int mSystemFrameRate;

    std::vector<SE_AnimationListener*> mListeners;
    PLAY_MODE mPlayMode;
};
#endif
