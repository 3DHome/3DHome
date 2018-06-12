#include "SE_DynamicLibType.h"
#include "SE_Camera.h"
#include "SE_ObjectAnimation.h"
#include "SE_SimObjectManager.h"
#include "SE_Application.h"
#include "SE_UserCommand.h"
#include "SE_Camera.h"
#include "SE_Mesh.h"
#include "SE_DataValueDefine.h"
#include "SE_Log.h"
#include "SE_MotionEventController.h"
#include "SE_InputManager.h"
#include "SE_MessageDefine.h"
#include "SE_MotionEventController.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_SceneManager.h"
#include "SE_MemLeakDetector.h"
SE_ObjectPhoneRingOnAnimation::SE_ObjectPhoneRingOnAnimation()
{
    mShakeAngle = 10.0;
    mShakeDistance = 5.0;
}

SE_ObjectPhoneRingOnAnimation::~SE_ObjectPhoneRingOnAnimation()
{}

void SE_ObjectPhoneRingOnAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode)
{
    SE_ResetSpatialCommand* c = (SE_ResetSpatialCommand*)SE_Application::getInstance()->createCommand("SE_ResetSpatialCommand");  
    
    c->mObjectName = mSimObject->getName();

    SE_Application::getInstance()->postCommand(c);
#if 1
    float angle = 0.0;
    if(frameIndex % 2 == 0)
    {
        angle = mShakeAngle;
    }
    else
    {
        angle = -mShakeAngle;
    }

    SE_RotateSpatialCommand* rc = (SE_RotateSpatialCommand*)SE_Application::getInstance()->createCommand("SE_RotateSpatialCommand");
    rc->mAxis = (SE_AXIS_TYPE)1;
    rc->mRotateAngle = angle;
    rc->mObjectName = mSimObject->getName();

    SE_Application::getInstance()->postCommand(rc);
        

    SE_TranslateSpatialCommand* tc = (SE_TranslateSpatialCommand*)SE_Application::getInstance()->createCommand("SE_TranslateSpatialCommand");
    tc->mTranslatedX = 0.0;
    tc->mTranslatedY = 0.0;
    tc->mTranslatedZ = mShakeDistance;
    tc->mObjectName = mSimObject->getName();

    SE_Application::getInstance()->postCommand(tc);

    SE_ScaleSpatialCommand* sc = (SE_ScaleSpatialCommand*)SE_Application::getInstance()->createCommand("SE_ScaleSpatialCommand");  
    
    sc->mObjectName = mSimObject->getName();
    sc->mScaledX = 2.0;
    sc->mScaledY = 2.0;
    sc->mScaledZ = 2.0;
    SE_Application::getInstance()->postCommand(sc);
#endif
}

void SE_ObjectPhoneRingOnAnimation::onRun()
{   

    int num = 30;
    setFrameNum(num);
    setTimePerFrame(getDuration() / num);    

}

void SE_ObjectPhoneRingOnAnimation::onEnd()
{
    SE_ResetSpatialCommand* c = (SE_ResetSpatialCommand*)SE_Application::getInstance()->createCommand("SE_ResetSpatialCommand");  
    
    c->mObjectName = mSimObject->getName();

    SE_Application::getInstance()->postCommand(c);
            
}

/////////////////////////////Clock Move///////////////////////////////

SE_ObjectClockMoveAnimation::SE_ObjectClockMoveAnimation()
{

    SE_SimObject *clock[3];

    clock[0] = SE_Application::getInstance()->getSimObjectManager()->findByName("large_clock");    
    clock[1] = SE_Application::getInstance()->getSimObjectManager()->findByName("large_minute");
    clock[2] = SE_Application::getInstance()->getSimObjectManager()->findByName("large_hour");

    for(int i = 0; i < 3; ++i)
    {
        if(clock[i] == NULL)
        {
            return;
        }
        mClockObject.push_back(clock[i]);
    }

    mBeforCameraDistance = 40.0;
    mPrevPercent = 0.0;
    mMotionEventController = NULL;
}

SE_ObjectClockMoveAnimation::~SE_ObjectClockMoveAnimation()
{
    clearlist();
}

void SE_ObjectClockMoveAnimation::clearlist()
{
    mClockObject.clear();
}

void SE_ObjectClockMoveAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode)
{
    LOGI("current frame = %d, current percent = %f\n",frameIndex,percent);
    if(getCurrentFrame() == frameIndex)
    {
        //Maybe on fast device.
        return;
    }

    if(getRunMode() == NOT_REPEAT)
    {
        translateGeo(percent);
    }
    else
    {
        returnTranslateGeo(percent);
    }
}

void SE_ObjectClockMoveAnimation::onRun()
{   

    int num = 30;
    setFrameNum(num);
    setTimePerFrame(getDuration() / num);

    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();

    SE_MotionEventCameraController* camera = controller->getCameraController();

    camera->lockCamera();

    if(getRunMode() == NOT_REPEAT)
    {
        initWhenGeoTranslateStart();
    }
    else
    {
        initWhenReturnGeoTranslateStart();
    }

}

void SE_ObjectClockMoveAnimation::onEnd()
{
    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();

    SE_MotionEventCameraController* camera = controller->getCameraController();

    

    mPrevPercent = 0.0;
    mGeoAllTranslateX = 0.0;
    mGeoAllTranslateY = 0.0;
    mGeoAllTranslateZ = 0.0;

    if(getRunMode() != NOT_REPEAT)
    {
        resetGeo();
        camera->unLockCamera();
    }
}

void SE_ObjectClockMoveAnimation::initWhenGeoTranslateStart()
{    

    if(!mClockObject.empty())
    {
        //find one component
        SE_SimObject * obj = mClockObject[0];
        
        SE_Vector3f basePosition = obj->getSpatial()->getLocalTranslate();

        mGeoAllTranslateX = SE_Application::getInstance()->getCurrentCamera()->getLocation().x - basePosition.x - mBeforCameraDistance;
        mGeoAllTranslateY = SE_Application::getInstance()->getCurrentCamera()->getLocation().y - basePosition.y;
        mGeoAllTranslateZ = SE_Application::getInstance()->getCurrentCamera()->getLocation().z - basePosition.z;

        for(int i = 0; i < mClockObject.size(); ++i )
        {
            SE_SimObject* partofobj = mClockObject[i];
            partofobj->getSpatial()->setLocalLayer(2);
            partofobj->getSpatial()->updateWorldLayer();
        }


    }
    generateAlphaBackground();
    
}

void SE_ObjectClockMoveAnimation::initWhenReturnGeoTranslateStart()
{
    if(!mClockObject.empty())
    {
        //find one component
        SE_SimObject * obj = mClockObject[0];

        //obj current position
        SE_Matrix4f m = obj->getSpatial()->getWorldTransform(); 

        SE_Vector3f currPos = m.getTranslate();
        
        //obj origine position
        SE_Vector3f basePosition = obj->getSpatial()->getLocalTranslate();

        mGeoAllTranslateX = basePosition.x - currPos.x;
        mGeoAllTranslateY = basePosition.y - currPos.y;
        mGeoAllTranslateZ = basePosition.z - currPos.z;        

    }
}

void SE_ObjectClockMoveAnimation::translateGeo(float percent)
{
    //need a delta-value between twice translate
    float translateInX = mGeoAllTranslateX * (percent - mPrevPercent);
    float translateInY = mGeoAllTranslateY * (percent - mPrevPercent);
    float translateInZ = mGeoAllTranslateZ * (percent - mPrevPercent);
    
    for(int i = 0; i < mClockObject.size(); ++i)
    {
        SE_SimObject* partofobj = mClockObject[i];

        SE_TranslateSpatialCommand* c = (SE_TranslateSpatialCommand*)SE_Application::getInstance()->createCommand("SE_TranslateSpatialCommand");

        //set rotate angle per ticket
        c->mTranslatedX = translateInX;
        c->mTranslatedY = translateInY;
        c->mTranslatedZ = translateInZ;

        //set spatialid. this is minute hand spatial
        c->mObjectName = partofobj->getName();

        //post this command to command queue
        SE_Application::getInstance()->postCommand(c);
    }
    fadeInBackground(percent);

    mPrevPercent = percent;
    
}

void SE_ObjectClockMoveAnimation::returnTranslateGeo(float percent)
{
    translateGeo(percent);
    fadeOutBackground(percent);
}

void SE_ObjectClockMoveAnimation::resetGeo()
{    
    for(int i = 0; i < mClockObject.size(); ++i)
    {
        SE_SimObject* partofobj = mClockObject[0];

        SE_ResetSpatialCommand* c = (SE_ResetSpatialCommand*)SE_Application::getInstance()->createCommand("SE_ResetSpatialCommand");

        //set spatialid. this is minute hand spatial
        c->mObjectName = partofobj->getName();

        //post this command to command queue
        SE_Application::getInstance()->postCommand(c);

        partofobj->getSpatial()->setLocalLayer(0);
        partofobj->getSpatial()->updateWorldLayer();

    }
    hideBackground();
}

void SE_ObjectClockMoveAnimation::fadeInBackground(float percent)
{    
    SE_SimObject* background_for_clock = SE_Application::getInstance()->getSimObjectManager()->findByName("background_for_clock");
    if(background_for_clock)
    {
        background_for_clock->getSpatial()->setVisible(true);
        
        float alpha = percent;
        background_for_clock->getSpatial()->setAlpha(alpha - 0.3);
    }
}


void SE_ObjectClockMoveAnimation::fadeOutBackground(float percent)
{    
    SE_SimObject* background_for_clock = SE_Application::getInstance()->getSimObjectManager()->findByName("background_for_clock");
    if(background_for_clock)
    {        
        float alpha = percent;
        background_for_clock->getSpatial()->setAlpha(0.7 - alpha);

        if(0.7 - alpha - 0.05 <= 0.05)
        {
            background_for_clock->getSpatial()->setVisible(false);
        }
    }

}

void SE_ObjectClockMoveAnimation::hideBackground()
{    
    SE_SimObject* background_for_clock = SE_Application::getInstance()->getSimObjectManager()->findByName("background_for_clock");
    if(background_for_clock)
    {        
        background_for_clock->getSpatial()->setVisible(false);
    }
}

void SE_ObjectClockMoveAnimation::generateAlphaBackground()
{
    SE_SimObject* background_for_clock = SE_Application::getInstance()->getSimObjectManager()->findByName("background_for_clock");
    if(!background_for_clock)
    {
        SE_Vector3f* va = new  SE_Vector3f[4];

        SE_Vector3i* faces = new SE_Vector3i[2];
        int* facet = new int[4];

        
        va[0].set(-100,-300,-500);
        va[1].set(-100,300,-500);
        va[2].set(-100,300,500);
        va[3].set(-100,-300,500);         


        faces[0].x = 0;
        faces[0].y = 1;
        faces[0].z = 2;

        faces[1].x = 0;
        faces[1].y = 2;
        faces[1].z = 3;        

        for(int i=0;i<2;i++) 
        {
            facet[i]=i;
        }

        SE_Vector2f* texVertex = new SE_Vector2f[4];
        SE_Vector3i* texFaces = new SE_Vector3i[2];
        texVertex[0] = SE_Vector2f(0, 0);
        texVertex[1] = SE_Vector2f(1, 0);
        texVertex[2] = SE_Vector2f(1, 1);
        texVertex[3] = SE_Vector2f(0, 1);
        

        SE_Vector3i* mfaces = new SE_Vector3i[4];
        mfaces[0].x = 0;
        mfaces[0].y = 1;
        mfaces[0].z = 2;

        mfaces[1].x = 0;
        mfaces[1].y = 2;
        mfaces[1].z = 3;        
        
        texFaces = mfaces;
    SE_AddUserObjectCommand* c = (SE_AddUserObjectCommand*)SE_Application::getInstance()->createCommand("SE_AddUserObjectCommand");
#ifdef WIN32
    c->mImagePath = "e:\\model\\newhome3\\background_for_clock.jpg";
#else
    c->mImagePath = "//sdcard//sedemo//background_for_clock.jpg";
#endif
    c->mObjectName = "background_for_clock";
    c->mProgramDataID = DEFAULT_SHADER;
    c->mRendererID = DEFAULT_RENDERER;
    c->mLayerIndex = 1;
        c->mNeedBlending = true;
        c->mNeedDepthTest =false;
        c->mAlpha = 0.0;
    c->mLastLayerInWorld = false;
    c->setVertexArray(va, 4);
    c->setVertexIndexArray(faces, 2);
    c->setTextureCoorArray(texVertex, 4);
    c->setTextureCoorIndexArray(texFaces, 2);
    c->setFacetIndexArray(facet, 2);
    //c->mObjectID = SE_ID::createSpatialID();
    //c->setLocalRotate(q);
    //c->setLocalTranslate(v);
    //c->setLocalScale(s);
        SE_Application::getInstance()->postCommand(c);

    }
}
///////////////////////////////Camera translate/////////////////////////
SE_ObjectCameraTranslateAnimation::SE_ObjectCameraTranslateAnimation()
{
    fovy = 60.0;
    mCurCamera = SE_Application::getInstance()->getCurrentCamera();
    mCameraController = NULL;

    mMainValue = 0.0;
    mCoefficient = 0;
    mSlope = 0.0;
    mIntercept = 0.0;

    mFirstTheta = 0.0; 
    mLastTheta = 0.0;
    mLastRotateRatio = 0.0;
    mLastRatio = 0.0;
    mFirstRatio = 0.0;
    mHalfMainValue = 0.0;

    mCurrCameraAngle = 0;
    mReturnStartAngle = 0.0;

    mTranslateStartTime = 0;
    mFirstTranslateDurationTime = 500;
    mLastTranslateDurationTime = 500;


    mTotalTranslateDurationTime = mFirstTranslateDurationTime + mLastTranslateDurationTime;
    mDelta = 0.0;
    mMotionEventController = NULL;
}

SE_ObjectCameraTranslateAnimation::~SE_ObjectCameraTranslateAnimation()
{

}

void SE_ObjectCameraTranslateAnimation::clearData()
{
    mMainValue = 0.0;
    mCoefficient = 0;
    mSlope = 0.0;
    mIntercept = 0.0;
    mFirstTheta = 0.0;
    mLastTheta = 0.0;
    mLastRotateRatio = 0.0;
    mLastRatio = 0.0;
    mFirstRatio = 0.0;
    mHalfMainValue = 0.0;    
    mTranslateStartTime = 0;
    mDelta = 0.0;
}

void SE_ObjectCameraTranslateAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode)
{    
    if(getCurrentFrame() == frameIndex)
    {
        //Maybe on fast device.
        return;
    }

    if(getRunMode() == NOT_REPEAT)
    {
        translateCamera();
    }
    else
    {
        returnTranslateCamera();
    }
}

void SE_ObjectCameraTranslateAnimation::onRun()
{   
    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
    mCameraController = controller->getCameraController();
    
    mCameraTranslateStartPos = mCameraController->getCameraTranslateStartPos();

    if(!mCameraController->getCanReturn())
    {
        mBackupStartPos = mCameraController->getCameraPostionAtStatic();
        mBackupStartZ = mCameraController->getCameraPositionStaticZ();
    }

    mCurrCameraAngle = mCameraController->getCurrCameraAngle();

    int num = 30;
    setFrameNum(num);
    setTimePerFrame(getDuration() / num);

    if(getRunMode() == NOT_REPEAT)
    {
        initWhenTranslateStart();
    }
    else
    {
        initWhenReturnTranslateStart();
    }

}

void SE_ObjectCameraTranslateAnimation::onEnd()
{
    
    if(getRunMode() == NOT_REPEAT)
    {
        //go to target
        mCameraController->setCameraToTarget(mTargetPos,mTargetZ);
        mCameraController->setCanReturn(true);
        sendFinishMessage();
    }
    else
    {
        //return to original point
        mCameraController->setCameraReturnToStart(mBackupStartPos,mBackupStartZ);
        mCameraController->setCanReturn(false);
    }

   clearData();
}

void SE_ObjectCameraTranslateAnimation::initWhenTranslateStart()
{
    //save status

    //mCameraTranslateStartPos = mCurCamera->getLocation(); 
    SE_Time time;
    mTranslateStartTime = time.getCurrentTimeMS();
    
    if(SE_Fabs(mCameraTranslateStartPos.x - mTargetPos.x) > SE_Fabs(mCameraTranslateStartPos.y - mTargetPos.y))
    {
        //mainValue on X
        mMainValue = SE_Fabs(mCameraTranslateStartPos.x - mTargetPos.x);
        mMainAxis = ON_X;

        //mainValue will increase or decrease
        if(mCameraTranslateStartPos.x > mTargetPos.x)
        {
            mCoefficient = -1 * (mMainValue / mLastTranslateDurationTime); //move just take place in last half            
        }
        else
        {
            mCoefficient = 1 * (mMainValue / mLastTranslateDurationTime); //move just take place in last half
        }
    }
    else
    {
        //mainValue on Y
        mMainValue = SE_Fabs(mCameraTranslateStartPos.y - mTargetPos.y);
        mMainAxis = ON_Y;

        //mainValue will increase or decrease
        if(mCameraTranslateStartPos.y > mTargetPos.y)
        {
            mCoefficient = -1 * (mMainValue / mLastTranslateDurationTime); //move just take place in last half
        }
        else
        {
            mCoefficient = 1 * (mMainValue / mLastTranslateDurationTime); //move just take place in last half
        }
    }

    //1.how to deal mCameraTranslateStartPos.x - mTargetPos.x = 0?
    mSlope = (mCameraTranslateStartPos.y - mTargetPos.y) / (mCameraTranslateStartPos.x - mTargetPos.x);

    mIntercept = mTargetPos.y - mSlope * mTargetPos.x;

    //construct targetToCamera
    SE_Vector3f targetToCamera = mCameraTranslateStartPos.subtract(mTargetPos);    

    SE_Vector3f cameraZ = mCurCamera->getAxisZ();

    mLastTheta = SE_Acosf(targetToCamera.dot(mTargetZ) / (targetToCamera.length() * mTargetZ.length()) );
    mFirstTheta = SE_Acosf(targetToCamera.dot(cameraZ) / (targetToCamera.length() * mCurCamera->getAxisZ().length()) );
    

    SE_Vector3f cameraRotateToMiddle = cameraZ.cross(targetToCamera);
    SE_Vector3f MiddleRotateToTarget = targetToCamera.cross(mTargetZ);
    

    int k = cameraRotateToMiddle.z < 0 ? -1 : 1;

    mFirstTheta *= k;

    k = MiddleRotateToTarget.z < 0 ? -1 : 1;

    mLastTheta *= k;

    mFirstRotateRatio = mFirstRatio = mFirstTheta / mFirstTranslateDurationTime;
    mLastRatio = mLastRotateRatio = mLastTheta / mLastTranslateDurationTime;

    mHalfMainValue = mMainValue/2;
    
}

void SE_ObjectCameraTranslateAnimation::translateCamera()
{    
    
    SE_Vector3f up(0, 0, 1);

    SE_TimeMS currentTime = SE_Time().getCurrentTimeMS();
    SE_TimeMS duration = currentTime - mTranslateStartTime;
    
    if(duration >= mFirstTranslateDurationTime)
    {
        if(mMainAxis == ON_X)
        {
            //change mainAxis value
            mCameraTranslateStartPos.x = mCameraTranslateStartPos.x + mCoefficient * (duration - mFirstTranslateDurationTime) - mDelta;
            mCameraTranslateStartPos.y = mSlope *(mCameraTranslateStartPos.x) + mIntercept;

            mDelta = mCoefficient * (duration - mFirstTranslateDurationTime);
            
        }
        else if(mMainAxis == ON_Y)
        {
            mCameraTranslateStartPos.y = mCameraTranslateStartPos.y + mCoefficient * (duration - mFirstTranslateDurationTime) - mDelta;
            mCameraTranslateStartPos.x = (mCameraTranslateStartPos.y - mIntercept) / mSlope; //slope can not equal zero,because you can not see the object that have a zero slope
            
            mDelta = mCoefficient * (duration - mFirstTranslateDurationTime);            
        }
    }

    SE_Vector3f newposition(mCameraTranslateStartPos.x,mCameraTranslateStartPos.y,mCameraTranslateStartPos.z);

    //1.rotate thetaratio,find xy

    float radius = mTargetPos.distance(mCameraTranslateStartPos);

    float radian = 0.0;

    if(duration >= mFirstTranslateDurationTime)
    {
        //last half
        float mFirstHasRotate = mFirstRotateRatio;
        //radian = mCurrCameraAngle + mFirstTheta + mLastRotateRatio;
        radian = mCurrCameraAngle + mFirstHasRotate + mLastRotateRatio;
       
    }
    else
    {      
        //first half
       
        radian = mCurrCameraAngle + mFirstRotateRatio;
    
    }

    float x = mTargetPos.x + radius*SE_Cosf(radian);
    float y = mTargetPos.y + radius*SE_Sinf(radian);

    //2.compute lookat

    SE_Vector3f newpos(x,y,mCameraTranslateStartPos.z);

    SE_Vector3f zAxis = newpos.subtract(mTargetPos);

    SE_Vector3f newzAxis(zAxis.x,zAxis.y,0);

    //3.ratio add
    if(duration >= mFirstTranslateDurationTime)
    {
        mLastRotateRatio = mLastRatio * (duration - mFirstTranslateDurationTime);
    }
    else
    {
        mFirstRotateRatio = mFirstRatio * duration;
    }
        
    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(newposition, newzAxis, up, fovy,((float)height)/ width, 1.0f, 1000.0f);

}

void SE_ObjectCameraTranslateAnimation::initWhenReturnTranslateStart() 
{
    //save status
    mCameraTranslateStartPos = mCurCamera->getLocation(); 
    SE_Time time;
    mTranslateStartTime = time.getCurrentTimeMS();
    
    if(SE_Fabs(mCameraTranslateStartPos.x - mBackupStartPos.x) > SE_Fabs(mCameraTranslateStartPos.y - mBackupStartPos.y))
    {
        mMainValue = SE_Fabs(mCameraTranslateStartPos.x - mBackupStartPos.x);
        mMainAxis = ON_X;
        if(mCameraTranslateStartPos.x > mBackupStartPos.x)
        {
            mCoefficient = -1 * (mMainValue / mTotalTranslateDurationTime); //move take place in whole process
        }
        else
        {
            mCoefficient = 1 * (mMainValue / mTotalTranslateDurationTime); //move take place in whole process
        }
    }
    else
    {
        mMainValue = SE_Fabs(mCameraTranslateStartPos.y - mBackupStartPos.y);
        mMainAxis = ON_Y;
        if(mCameraTranslateStartPos.y > mBackupStartPos.y)
        {
            mCoefficient = -1 * (mMainValue / mTotalTranslateDurationTime); //move take place in whole process
        }
        else
        {
            mCoefficient = 1 * (mMainValue / mTotalTranslateDurationTime); //move take place in whole process
        }
    }

    LOGI("mCoefficient = %f\n",mCoefficient);
    mSlope = (mCameraTranslateStartPos.y - mBackupStartPos.y) / (mCameraTranslateStartPos.x - mBackupStartPos.x);//1.how to deal mCameraTranslateStartPos.x - mTargetPos.x = 0
    mIntercept = mBackupStartPos.y - mSlope * mBackupStartPos.x;

    SE_Vector3f targetToCamera = mBackupStartPos.subtract(mTargetPos);    

    SE_Vector3f cameraZ = mCurCamera->getAxisZ();
    mLastTheta = SE_Acosf(targetToCamera.dot(mBackupStartZ) / (targetToCamera.length() * mBackupStartZ.length()) );
    mFirstTheta = SE_Acosf(targetToCamera.dot(cameraZ) / (targetToCamera.length() * mCurCamera->getAxisZ().length()) );

    SE_Vector3f right(1,0,0);
    mReturnStartAngle = SE_Acosf(right.dot(mTargetZ) / (right.length() * mTargetZ.length()) );
    

    SE_Vector3f cameraRotateToMiddle = cameraZ.cross(targetToCamera);
    SE_Vector3f MiddleRotateToTarget = targetToCamera.cross(mBackupStartZ);
    SE_Vector3f rightRotateToTarget = right.cross(mTargetZ);

    int k = cameraRotateToMiddle.z < 0 ? -1 : 1;

    mFirstTheta *= k;

    k = MiddleRotateToTarget.z < 0 ? -1 : 1;

    mLastTheta *= k;

    k = rightRotateToTarget.z < 0 ? -1 : 1;

    mReturnStartAngle *= k;

    mFirstRotateRatio = mFirstRatio = mFirstTheta / mFirstTranslateDurationTime;
    mLastRatio = mLastRotateRatio = mLastTheta / mLastTranslateDurationTime;

    mHalfMainValue = mMainValue/2;
}

void SE_ObjectCameraTranslateAnimation::returnTranslateCamera()
{

    SE_Vector3f up(0, 0, 1);

    SE_TimeMS currentTime = SE_Time().getCurrentTimeMS();
    SE_TimeMS duration = currentTime - mTranslateStartTime;


    if(mMainAxis == ON_X)
    {
        //change mainAxis value
        mCameraTranslateStartPos.x = mCameraTranslateStartPos.x + mCoefficient * duration - mDelta;
        mCameraTranslateStartPos.y = mSlope *(mCameraTranslateStartPos.x) + mIntercept;

        mDelta = mCoefficient * duration;
        
    }
    else if(mMainAxis == ON_Y)
    {
        mCameraTranslateStartPos.y = mCameraTranslateStartPos.y + mCoefficient * duration - mDelta;
        mCameraTranslateStartPos.x = (mCameraTranslateStartPos.y - mIntercept) / mSlope; //slope can not equal zero,because you can not see the object that have a zero slope
        
        mDelta = mCoefficient * duration;            
    }

    SE_Vector3f newposition(mCameraTranslateStartPos.x,mCameraTranslateStartPos.y,mCameraTranslateStartPos.z);

    //1.rotate thetaratio,find xy

    float radius = mTargetPos.distance(mCameraTranslateStartPos);

    float radian = 0.0;

    if(duration > mFirstTranslateDurationTime)
    {
        //last half
        radian = mReturnStartAngle + mFirstTheta + mLastRotateRatio;
        
       
    }
    else
    {      
        //first half
        radian = mReturnStartAngle + mFirstRotateRatio;    
    }

    float x = mTargetPos.x + radius*SE_Cosf(radian);
    float y = mTargetPos.y + radius*SE_Sinf(radian);

    //2.compute lookat

    SE_Vector3f newpos(x,y,mCameraTranslateStartPos.z);

    SE_Vector3f zAxis = newpos.subtract(mTargetPos);

    SE_Vector3f newzAxis(zAxis.x,zAxis.y,0);

    //3.ratio add
    if(duration >= mFirstTranslateDurationTime)
    {
        mLastRotateRatio = mLastRatio * (duration - mFirstTranslateDurationTime);
    }
    else
    {
        mFirstRotateRatio = mFirstRatio * duration;
    }
        
    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(newposition, newzAxis, up, fovy,((float)height)/ width, 1.0f, 1000.0f);
    mMainValue -= SE_Fabs(mCoefficient);
}

void SE_ObjectCameraTranslateAnimation::sendFinishMessage()
{    
    SE_Message* msg = new SE_Message;
    msg->type = SE_MSG_TRANSLATE_FINISH;
    SE_Struct* sestruct = new SE_Struct(1);
    SE_StructItem* sitem = new SE_StructItem(1);
    SE_StdString* stdString = new SE_StdString;
    stdString->data = "finish.";
    sitem->setDataItem(stdString);
    sestruct->setStructItem(0, sitem);
    msg->data = sestruct;
    SE_Application::getInstance()->sendMessage(msg);

}

/////////////////////////////////SE_ObjectFadeOutAnimation///////////////////////
SE_ObjectFadeInOutAnimation::SE_ObjectFadeInOutAnimation()
{
    mSimObject = NULL;
    mCameraController = NULL;
    mMotionEventController = NULL;
}

SE_ObjectFadeInOutAnimation::~SE_ObjectFadeInOutAnimation()
{}

void SE_ObjectFadeInOutAnimation::onRun()
{

    int num = 1;
    setFrameNum(1);
    setTimePerFrame(getDuration() / num);

    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
    mCameraController = controller->getCameraController();
    mCameraController->lockCamera();

    int surfaceNum = mSimObject->getSurfaceNum();
    for(int i = 0; i < surfaceNum; ++i)
    {
        mSimObject->getMesh()->getSurface(i)->setProgramDataID(DEFAULT_SHADER);
        mSimObject->getMesh()->getSurface(i)->setRendererID(DEFAULT_RENDERER); 

        SE_BlendState *rs_blend = (SE_BlendState *)mSimObject->getSpatial()->getRenderState(BLENDSTATE);
        rs_blend->setBlendDstFunc(SE_BlendState::ONE_MINUS_SRC_ALPHA);
        rs_blend->setBlendSrcFunc(SE_BlendState::SRC_ALPHA);
    }    
    mSimObject->getSpatial()->updateRenderState();
    
}

void SE_ObjectFadeInOutAnimation::onEnd()
{

    mCameraController->unLockCamera();

    int surfaceNum = mSimObject->getSurfaceNum();
    for(int i = 0; i < surfaceNum; ++i)
    {
        mSimObject->getMesh()->getSurface(i)->setProgramDataID(DEFAULT_SHADER);
        mSimObject->getMesh()->getSurface(i)->setRendererID(DEFAULT_RENDERER); 

        SE_BlendState* blendState = (SE_BlendState *)mSimObject->getSpatial()->getRenderState(BLENDSTATE);
        blendState->setBlendDstFunc(SE_BlendState::ZERO);
        blendState->setBlendSrcFunc(SE_BlendState::ONE);
        
    }
    mSimObject->getSpatial()->updateRenderState();

    if(getRunMode() == NOT_REPEAT)
    {
        //fade out
        mSimObject->getSpatial()->setVisible(false);
    }
}

void SE_ObjectFadeInOutAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex, SE_Animation::PLAY_MODE playmode)
{

    LOGI("current frame = %d, current percent = %f\n",frameIndex,percent);
    int surfaceNum = mSimObject->getSurfaceNum();
    for(int i = 0; i < surfaceNum; ++i)
    {
        SE_Surface * surface = mSimObject->getMesh()->getSurface(i);
        float alpha = 0.0;

        if(getRunMode() == NOT_REPEAT)
        {
            //fade out
            alpha = 1.0 - percent;
        }
        else
        {
            //fadein
            alpha = percent;
        }
        mSimObject->getSpatial()->setAlpha(alpha);
    }
}
