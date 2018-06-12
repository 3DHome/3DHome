#ifndef SE_OBJECTANIMATION_H
#define SE_OBJECTANIMATION_H
#include "SE_Animation.h"
#include "SE_Common.h"
#include "SE_Vector.h"
#include <vector>
#include "SE_MotionEventCameraController.h"
class SE_SimObject;
class SE_ENTRY SE_ObjectPhoneRingOnAnimation : public SE_Animation
{
public:
    SE_ObjectPhoneRingOnAnimation();
    ~SE_ObjectPhoneRingOnAnimation();

    void setSimObject(SE_SimObject* simObject)
    {
        mSimObject = simObject;
    }
public:
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual void onRun();
    virtual void onEnd();
private:

    float mShakeAngle;
    float mShakeDistance;
    
    SE_SimObject* mSimObject; 
    
};

class SE_ENTRY SE_ObjectClockMoveAnimation : public SE_Animation
{
public:
    SE_ObjectClockMoveAnimation();
    ~SE_ObjectClockMoveAnimation();
    void setMotionEventController(SE_MotionEventController* c)
    {
        mMotionEventController = c;
    }
public:
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual void onRun();
    virtual void onEnd();
private:
    void initWhenGeoTranslateStart();
    void initWhenReturnGeoTranslateStart();

    void translateGeo(float percent);
    void returnTranslateGeo(float percent);

    void resetGeo();
    void generateAlphaBackground();

    void fadeInBackground(float percent);
    void fadeOutBackground(float percent);
    void hideBackground();   

    void clearlist();

    float mGeoAllTranslateX;
    float mGeoAllTranslateY;
    float mGeoAllTranslateZ;
    
    float mBeforCameraDistance;   
    
    std::vector<SE_SimObject*> mClockObject; 

    float mPrevPercent;
    SE_MotionEventController* mMotionEventController;
};

class SE_ENTRY SE_ObjectCameraTranslateAnimation : public SE_Animation
{
public:
    SE_ObjectCameraTranslateAnimation();
    ~SE_ObjectCameraTranslateAnimation();

    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual void onRun();
    virtual void onEnd();

    void translateCamera();
    void returnTranslateCamera();

    void initWhenTranslateStart();
    void initWhenReturnTranslateStart(); 

    void doMoveTo(const char *objname);

    void setTargetPosition(SE_Vector3f &target)
    {
        mTargetPos = target;
    }

    void setTargetZ(SE_Vector3f &z)
    {
        mTargetZ = z;
    }

    void setHomeCameraPos(SE_Vector3f &home)
    {
        mHomeCameraPos = home;
    }

    void setHomeCameraPosZ(SE_Vector3f &homez)
    {
        mHomeCameraZ = homez;
    }

    void setBackupCameraPos(SE_Vector3f backup)
    {
        mBackupStartPos = backup;
    }

    void setBackupCameraPosZ(SE_Vector3f backupz)
    {
        mBackupStartZ = backupz;
    }
    void setMotionEventController(SE_MotionEventController* c)
    {
        mMotionEventController = c;
    }
private:
    enum MainAxis
    {
        ON_X,
        ON_Y
    };

    MainAxis mMainAxis;
    float mMainValue;
    float mCoefficient;
    SE_Vector3f mCameraTranslateStartPos;
    float mCurrCameraAngle;

    SE_Vector3f mTargetPos;
    SE_Vector3f mTargetZ;

    SE_Vector3f mBackupStartPos;
    SE_Vector3f mBackupStartZ;
    

    SE_Vector3f mHomeCameraPos;
    SE_Vector3f mHomeCameraZ;

    float mSlope;
    float mIntercept;
    float mFirstTheta;
    float mLastTheta;
    float mLastRotateRatio;
    float mFirstRotateRatio;
    float mLastRatio;
    float mFirstRatio;

    float mHalfMainValue;
    float mReturnStartAngle;

    SE_TimeMS mTranslateStartTime;
    SE_TimeMS mFirstTranslateDurationTime;
    SE_TimeMS mLastTranslateDurationTime;
    SE_TimeMS mTotalTranslateDurationTime;

    float mDelta;
    float fovy;
    SE_Camera * mCurCamera;
    SE_MotionEventCameraController* mCameraController;

    void clearData();

    void sendFinishMessage();
    SE_MotionEventController* mMotionEventController;
};

class SE_ENTRY SE_ObjectFadeInOutAnimation : public SE_Animation
{
public:
    SE_ObjectFadeInOutAnimation();
    ~SE_ObjectFadeInOutAnimation();

    void setSimObject(SE_SimObject* simObject)
    {
        mSimObject = simObject;
    }
    void setMotionEventController(SE_MotionEventController* c)
    {
        mMotionEventController = c;
    }
public:
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual void onRun();
    virtual void onEnd();
private:
    SE_SimObject* mSimObject; 
    SE_MotionEventCameraController* mCameraController;
    SE_MotionEventController* mMotionEventController;
};
#endif
