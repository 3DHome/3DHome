#ifndef SE_MOTIONEVENT_CAMERA_CONTROLLER_H
#define SE_MOTIONEVENT_CAMERA_CONTROLLER_H
#include "SE_Object.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_Camera.h"
#include "SE_InputEvent.h"
#include "SE_Geometry.h"
#include "SE_Time.h"
#include <list>

class SE_CenterMainController;

class SE_ENTRY SE_MotionEventCameraController : public SE_MotionEventSEObjectController
{
DECLARE_OBJECT(SE_MotionEventCameraController)
public:
    SE_MotionEventCameraController(SE_MotionEventController* c = NULL);
    virtual ~SE_MotionEventCameraController();
    void onMotionEvent(SE_MotionEvent* motionEvent);
    void clearState();  

    enum CameraPos
    {
        CAMERA_AT_0,
        CAMERA_AT_90,
        CAMERA_AT_180,
        CAMERA_AT_270
    }; 

    void setCameraToTarget(SE_Vector3f &targetpos,SE_Vector3f &targetz);
    void setCameraReturnToStart(SE_Vector3f &backupStartPos,SE_Vector3f &backupStartZ);

    void lockCamera()
    {
        mIsAnimating = true;
    }

    void unLockCamera()
    {
        mIsAnimating = false;
    }

    float angleFromZeroToCamera();

    SE_Vector3f getCameraPostionAtStatic()
    {
        return mBackupStartPos;
    }

    SE_Vector3f getCameraTranslateStartPos()
    {
        return mCameraTranslateStartPos;
    }

    SE_Vector3f getCameraPositionStaticZ()
    {
        return mBackupStartZ;
    }

    float getCurrCameraAngle()
    {
        return mCurrCameraAngle;
    }

    void setCanReturn(bool canreturn)
    {
        mCanReturn = canreturn;
    }

    bool getCanReturn()
    {
        return mCanReturn; 
    }    

    SE_Vector3f getCameraHomePositionAtStatic()
    {
        return mHomeCameraPos;
    }
    SE_Vector3f getCameraHomePositionStaticZ()
    {
        return mHomeCameraZ;
    }

    void useObjOnTableMode(bool onTable)
    {
        mOnTable = onTable;
    }
private:
    SE_MotionEvent::TYPE mPrevType;
    float mPrevX;
    float mPrevY;


    void pitchMove();
    void resetPitchMove();
    bool mHasPicth;
    bool mOnTable;


    void roundCamera(float angle);//helper function for round camera

    void flushScreen();

    void setCameraToAxis();
    

    SE_Vector3f mCenter; // save round center

    float mAngleRounded; //total angle per move action

    bool mDone; //camera being auto round

    bool mSaved;

    float mNeedRoundAngle; //camera should auto-round this angle to get to correct position 

    float mRadius;    

    enum MotionStatus
    {
        MOTION_STATIC,        
        MOTION_AUTOROTATE        
    };

    MotionStatus mMotionStatus;    

    CameraPos mCameraPos;
    
    SE_Vector3f mCameraTranslateStartPos;
    float mCurrCameraAngle;

    SE_Vector3f mTargetPos;
    SE_Vector3f mTargetZ;

    SE_Vector3f mBackupStartPos;
    SE_Vector3f mBackupStartZ;

    SE_Vector3f mHomeCameraPos;
    SE_Vector3f mHomeCameraZ;   

    
    float mReturnStartAngle;

    float mAutoRotateVelocity;

    bool mCanReturn;    

    bool mKeyMoveEnable;

    std::string mTouchObjName;

    SE_Camera * mCurCamera;

    bool mIsAnimating;

    void clearData();

    SE_CenterMainController *mCenterMainController;
};
#endif
