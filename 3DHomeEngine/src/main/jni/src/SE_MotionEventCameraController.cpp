#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Geometry.h"
#include "SE_Time.h"
#include "SE_Object.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_MotionEventSimObjectController.h"
#include <vector>
#include "SE_CenterMainController.h"
#include "SE_MotionEventCameraController.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"
#include "SE_Geometry3D.h"
#include "SE_Spatial.h"
#include "SE_SpatialTravel.h"
#include "SE_SimObject.h"
#include "SE_Log.h"
#include "SE_SystemCommand.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"
#include "SE_SimObjectManager.h"
#include "SE_UserCommand.h"
#include "SE_CommonNode.h"
#include "SE_DataValueDefine.h"
#include "SE_Mesh.h"
#include "SE_MemLeakDetector.h"
#define TIMER

IMPLEMENT_OBJECT(SE_MotionEventCameraController)

//#define USE_OBJONTABLE_MODE
const float fovy = 60.0;
SE_MotionEventCameraController::SE_MotionEventCameraController(SE_MotionEventController* c ) : SE_MotionEventSEObjectController(c)
{
    mPrevType = SE_MotionEvent::UP;
    
    mPrevX = 0;
    mPrevY = 0;

    mIsAnimating = false; 
    //add by guohua
    mDone = false;
    mRadius = 120.0;
    mAngleRounded = 0.0;    
    mNeedRoundAngle = 0.0;
    mSaved = false;

    mCurrCameraAngle = 0;
    mReturnStartAngle = 0.0;

    mCanReturn = false;

    mAutoRotateVelocity = 7;

    mKeyMoveEnable = false;

    mHasPicth = false;
#ifdef USE_OBJONTABLE_MODE
    mOnTable = true;//just a switch
#else
    mOnTable = false;
#endif

    mMotionStatus = MOTION_STATIC;

    mCurCamera = NULL;

    mType = SE_MotionEventSEObjectController::CAMERA_CONTROLLER;

    mCenterMainController = new SE_CenterMainController(mOnTable);

}
SE_MotionEventCameraController::~SE_MotionEventCameraController()
{
    delete mCenterMainController;
    LOGI("### destroctor ~SE_MotionEventCameraController ####\n");
}

void SE_MotionEventCameraController::clearState()
{
        mPrevType = SE_MotionEvent::UP;
        mPrevX = 0;
        mPrevY = 0;
        mHasPicth = false;
}

void SE_MotionEventCameraController::clearData()
{

        clearState();
        mAngleRounded = 0;
        mDone = false;      

        mCanReturn = false;
        mKeyMoveEnable = false;
        mMotionStatus = MOTION_STATIC;

        mHasPicth = false;
        
}
void SE_MotionEventCameraController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(mIsAnimating)
    {
        return;//Animation is playing, not response motion event.
    }

    if(motionEvent == NULL)
        return;
    if((mPrevType ==  SE_MotionEvent::UP || mPrevType ==  SE_MotionEvent::AUTOROTATE )&& motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();        
        
        //save current camera location,
        mCameraTranslateStartPos = SE_Application::getInstance()->getCurrentCamera()->getLocation();

        //save autoRotate parameters
        if(mSaved == false)
        {        
            mCurCamera = SE_Application::getInstance()->getCurrentCamera();
            mSaved = true;
            mCenter.x = mCameraTranslateStartPos.x;
            mCenter.y = mCameraTranslateStartPos.y + mRadius;
            mCenter.z = mCameraTranslateStartPos.z;

            mBackupStartPos = mCurCamera->getLocation();
            mBackupStartZ = mCurCamera->getAxisZ();

            mHomeCameraPos = mCurCamera->getLocation();
            mHomeCameraZ = mCurCamera->getAxisZ();

            //need get current camera pos first.
            mCurrCameraAngle = angleFromZeroToCamera();
        }

        

    }
    else if((mPrevType == SE_MotionEvent::DOWN || mPrevType == SE_MotionEvent::MOVE)&& 
                motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        //maybe move
        float deltaX = motionEvent->getX() - mPrevX;
        float deltaY = motionEvent->getY() - mPrevY;

        if(mPrevType == SE_MotionEvent::DOWN && 
            (SE_Fabs(deltaX) > SE_MotionEvent::MOVE_SLOPE || SE_Fabs(deltaY) > SE_MotionEvent::MOVE_SLOPE))
        {
            mPrevType = SE_MotionEvent::MOVE;
        }

        if(mPrevType == SE_MotionEvent::MOVE && SE_Fabs(deltaX) > SE_MotionEvent::MOVE_SLOPE)
        {        
            SE_Rect<int> viewport = mCurCamera->getViewport();
            int viewportWidth = viewport.right - viewport.left;
            float ratio = 120.0f / viewportWidth;        
            float angle = ratio * deltaX;            
            
            SE_Vector3f startLocation = mCurCamera->getLocation();

            if(!mCanReturn)
            {                
                roundCamera(angle);

                mDone = false;
                    
#if 0
                SE_Vector3f endLocation = mCurCamera->getLocation();
                SE_Sphere sphere;
                sphere.set(startLocation, 2);
                SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
                SE_Spatial* rootScene = sceneManager->getRoot();
                SE_MovingSphereStaticSpatialIntersect moveTravel(sphere, endLocation);
                rootScene->travel(&moveTravel, true);
                if(moveTravel.intersected)
                {
                    //setLocation(moveTravel.location);
                }

                LOGI("### deltax = %f, deltay = %f, startLocation = (%f, %f, %f),  \
                     endLocation = (%f, %f, %f) ## \n" , deltaX, deltaY, startLocation.x, 
                                                       startLocation.y, startLocation.z,
                                                       endLocation.x, endLocation.y, endLocation.z);
#endif
                mPrevX = motionEvent->getX();
                mPrevY = motionEvent->getY();
            }
        }
        else if(mPrevType == SE_MotionEvent::MOVE && SE_Fabs(deltaY) > SE_MotionEvent::MOVE_SLOPE)
        {

            if(mOnTable)
            {
                //pitchMove();

                mPrevType = SE_MotionEvent::UP;

                mPrevX = motionEvent->getX();
                mPrevY = motionEvent->getY();
            }

            
        }
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        if(!mCanReturn)
        {
            SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
            if(c)
            {
                SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::AUTOROTATE, motionEvent->getX(), motionEvent->getY());                
                
                //Is Fling geuster?
                if(motionEvent->getVelocityX() != 0 || motionEvent->getVelocityY() != 0)
                {
                    ke->setVelocityX(motionEvent->getVelocityX());
                    ke->setVelocityY(motionEvent->getVelocityY());
                }
                c->motionEvent = ke;
                SE_Application::getInstance()->postCommand(c);
            }
        }        
        mPrevType = SE_MotionEvent::UP;
        flushScreen();
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::UP;

        //if autoXXX not complete,continue.

        if(mMotionStatus == MOTION_AUTOROTATE)
        {
            SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
            if(c)
            {
                //need geuster detector?
                SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::AUTOROTATE, motionEvent->getX(), motionEvent->getY());
                c->motionEvent = ke;
                SE_Application::getInstance()->postCommand(c);
            }
        }        
        else
        {
            //do nothing
        }

#if 0
        SE_Ray ray = mCurCamera->screenCoordinateToRay(mPrevX, mPrevY);
        SE_FindSpatialCollision spatialCollision(ray);
        SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getRoot();
        root->travel(&spatialCollision, true);
        SE_Spatial* collisionSpatial = spatialCollision.getCollisionSpatial();
        SE_SimObject* so = spatialCollision.getCollisionObject();
        if(collisionSpatial)
        {
            SE_Application::getInstance()->getSceneManager()->setSelectedSpatial(collisionSpatial);
            collisionSpatial->setSelected(true);
        }
        if(so)
        {
            LOGI("## selected object = %s ####\n", so->getName());
            if(!mCanReturn)//Do not save touched object name,when camera on ZOOM IN position.
            {
                mTouchObjName = so->getName();
                //doMoveTo(so->getName());
            }

            SE_Message* msg = new SE_Message;
            msg->type = SE_MSG_SIMOBJECT_NAME;
            SE_Struct* sestruct = new SE_Struct(1);
            SE_StructItem* sitem = new SE_StructItem(1);
            SE_StdString* stdString = new SE_StdString;
            stdString->data = so->getName();
            sitem->setDataItem(stdString);
            sestruct->setStructItem(0, sitem);
            msg->data = sestruct;
            SE_Application::getInstance()->sendMessage(msg);
        } 
#endif

    }
    else if( (mPrevType == SE_MotionEvent::UP || mPrevType == SE_MotionEvent::AUTOROTATE) && motionEvent->getType() == SE_MotionEvent::AUTOROTATE)
    {
        mPrevType = SE_MotionEvent::AUTOROTATE;
        mMotionStatus = MOTION_AUTOROTATE;
        
        if(mDone == false)
        {
            if(SE_Fabs(mAngleRounded) - 30.0 > 0.0 || motionEvent->getVelocityX() != 0 || motionEvent->getVelocityY() != 0)// rotate > 30 or FLING,go on 
            {   

                if(SE_Fabs(mAngleRounded) - 90.0 > 0 )
                {
                    mNeedRoundAngle = mAngleRounded > 0 ? 180.0 - mAngleRounded : -(180.0 + mAngleRounded); //roll front
                }
                else
                {
                    mNeedRoundAngle = mAngleRounded > 0 ? 90.0 - mAngleRounded : -(90.0 + mAngleRounded); //roll front
                }

            }
            else
            {
                mNeedRoundAngle = -mAngleRounded; //need roll back
            }            

            SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
            if(c)
            {
                SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::AUTOROTATE, motionEvent->getX(), motionEvent->getY());
                c->motionEvent = ke;
                SE_Application::getInstance()->postCommand(c);
            }

            flushScreen();
            mDone = true;            
            
        }
        else
        {
            if(SE_Fabs(mNeedRoundAngle) - mAutoRotateVelocity <= 0.01)
            {
                //1.reset camera position.                
                setCameraToAxis();

                //add by guohua, we have set a new camera position,so we need flush screen once again.
                flushScreen();

                clearData();
            }
            else
            {
                SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
                if(c)
                {
                    SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::AUTOROTATE, motionEvent->getX(), motionEvent->getY());
                    c->motionEvent = ke;
                    SE_Application::getInstance()->postCommand(c);
                }                

                flushScreen();
                
                mNeedRoundAngle = mNeedRoundAngle + (mNeedRoundAngle > 0 ? -mAutoRotateVelocity : mAutoRotateVelocity);
                if(mNeedRoundAngle > 0)
                {
                    roundCamera(mAutoRotateVelocity);
                }
                else
                {
                    roundCamera(-mAutoRotateVelocity);
                }
            }
        }
    }

}

void SE_MotionEventCameraController::roundCamera(float angle)
{
    if(mCanReturn)
    {
        return;//do not roundCamera when camera on ZOOM IN Postion.
    }

    mAngleRounded += angle;//save total angle

    SE_Vector3f startLocation = mCurCamera->getLocation();    

    float currentAngle = angleFromZeroToCamera();

    currentAngle += SE_AngleToRadian(angle);    

    //new position after move camera 
    float x = mCenter.x + mRadius*SE_Cosf(currentAngle);
    float y = mCenter.y + mRadius*SE_Sinf(currentAngle);    

    SE_Vector3f newposition(x, y, startLocation.z);

    SE_Vector3f newlookat = newposition.subtract(mCenter);    

    SE_Vector3f zAxis(newlookat.x,newlookat.y, 0);            
    SE_Vector3f up(0, 0, 1);

    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(newposition, zAxis, up, fovy,((float)height)/ width, 1.0f, 1000.0f);    

    return;
    //end of add
}

void SE_MotionEventCameraController::setCameraToAxis()
{
    //compute camera on axis position.
    float axisAngle = 0.0;
    SE_Vector3f zAxis;

    float currentAngle = angleFromZeroToCamera();
    
    //camera at 1,2 quadrant
    if(SE_Fabs(currentAngle) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = CAMERA_AT_0;
        axisAngle = 0.0;
    }
    else if(SE_Fabs(SE_Fabs(currentAngle) - 0.5 * SE_PI) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = currentAngle > 0 ? CAMERA_AT_90 : CAMERA_AT_270;
        axisAngle = currentAngle > 0 ? 0.5*SE_PI : -0.5*SE_PI;
    }
    else
    {
        mCameraPos = CAMERA_AT_180;
        axisAngle = SE_PI;
    }

    switch(mCameraPos)
    {
    case CAMERA_AT_0:
        mCurrCameraAngle = 0;
        zAxis.set(1,0,0);
        if(!mOnTable)
        {
            mCenterMainController->setNextController("positiveX");
            mCenterMainController->changeController();
        }
        break;
    case CAMERA_AT_90:
        mCurrCameraAngle = 0.5 * SE_PI;
        zAxis.set(0,1,0);
        if(!mOnTable)
        {
            mCenterMainController->setNextController("positiveY");
            mCenterMainController->changeController();
        }
        break;
    case CAMERA_AT_180:
        mCurrCameraAngle = SE_PI;
        zAxis.set(-1,0,0);
        if(!mOnTable)
        {
            mCenterMainController->setNextController("negativeX");
            mCenterMainController->changeController();
        }
        break;
    case CAMERA_AT_270:
        mCurrCameraAngle = 1.5 * SE_PI;
        zAxis.set(0,-1,0);
        if(!mOnTable)
        {
            mCenterMainController->setNextController("negativeY");
            mCenterMainController->changeController();
        }
        break;
    }

    //new position after move camera 
    float x = mCenter.x + mRadius*SE_Cosf(axisAngle);
    float y = mCenter.y + mRadius*SE_Sinf(axisAngle);    

    SE_Vector3f newposition(x, y, mCenter.z);    
    
    SE_Vector3f up(0, 0, 1);

    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(newposition, zAxis, up, fovy,((float)height)/ width, 1.0f, 1000.0f);

    mBackupStartPos = mCurCamera->getLocation();//rest current camera pos
    mBackupStartZ = mCurCamera->getAxisZ();
}

void SE_MotionEventCameraController::setCameraToTarget(SE_Vector3f &targetpos,SE_Vector3f &targetz)
{
    SE_Vector3f up(0, 0, 1);

    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(targetpos, targetz, up, fovy,((float)height)/ width, 1.0f, 1000.0f);

    //compute camera on axis position.
    float axisAngle = 0.0;
    SE_Vector3f zAxis;

    float currentAngle = angleFromZeroToCamera();
    
    //camera at 1,2 quadrant
    if(SE_Fabs(currentAngle) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = CAMERA_AT_0;
        axisAngle = 0.0;
    }
    else if(SE_Fabs(SE_Fabs(currentAngle) - 0.5 * SE_PI) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = currentAngle > 0 ? CAMERA_AT_90 : CAMERA_AT_270;
        axisAngle = currentAngle > 0 ? 0.5*SE_PI : -0.5*SE_PI;
    }
    else
    {
        mCameraPos = CAMERA_AT_180;
        axisAngle = SE_PI;
    }

    switch(mCameraPos)
    {
    case CAMERA_AT_0:
        mCurrCameraAngle = 0;
        zAxis.set(1,0,0);
        break;
    case CAMERA_AT_90:
        mCurrCameraAngle = 0.5 * SE_PI;
        zAxis.set(0,1,0);
        break;
    case CAMERA_AT_180:
        mCurrCameraAngle = SE_PI;
        zAxis.set(-1,0,0);
        break;
    case CAMERA_AT_270:
        mCurrCameraAngle = 1.5 * SE_PI;
        zAxis.set(0,-1,0);
        break;
    }

}

void SE_MotionEventCameraController::setCameraReturnToStart(SE_Vector3f &backupStartPos,SE_Vector3f &backupStartZ)
{
    SE_Vector3f up(0, 0, 1);

    SE_Rect<int> viewport = mCurCamera->getViewport();
    int height = viewport.bottom - viewport.top;
    int width = viewport.right - viewport.left;

    mCurCamera->create(backupStartPos, backupStartZ, up, fovy,((float)height)/ width, 1.0f, 1000.0f);

    //compute camera on axis position.
    float axisAngle = 0.0;
    SE_Vector3f zAxis;

    float currentAngle = angleFromZeroToCamera();
    
    //camera at 1,2 quadrant
    if(SE_Fabs(currentAngle) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = CAMERA_AT_0;
        axisAngle = 0.0;
    }
    else if(SE_Fabs(SE_Fabs(currentAngle) - 0.5 * SE_PI) < SE_AngleToRadian(mAutoRotateVelocity))
    {
        mCameraPos = currentAngle > 0 ? CAMERA_AT_90 : CAMERA_AT_270;
        axisAngle = currentAngle > 0 ? 0.5*SE_PI : -0.5*SE_PI;
    }
    else
    {
        mCameraPos = CAMERA_AT_180;
        axisAngle = SE_PI;
    }

    switch(mCameraPos)
    {
    case CAMERA_AT_0:
        mCurrCameraAngle = 0;
        zAxis.set(1,0,0);
        break;
    case CAMERA_AT_90:
        mCurrCameraAngle = 0.5 * SE_PI;
        zAxis.set(0,1,0);
        break;
    case CAMERA_AT_180:
        mCurrCameraAngle = SE_PI;
        zAxis.set(-1,0,0);
        break;
    case CAMERA_AT_270:
        mCurrCameraAngle = 1.5 * SE_PI;
        zAxis.set(0,-1,0);
        break;
    }

}

float SE_MotionEventCameraController::angleFromZeroToCamera()
{
    SE_Vector3f cameraZ = mCurCamera->getAxisZ();
    SE_Vector3f axis_x = SE_Vector3f(1,0,0);
    float angle = SE_Acosf(axis_x.dot(cameraZ) / (axis_x.length() * cameraZ.length()) );    

    SE_Vector3f x2c = axis_x.cross(cameraZ);

    return x2c.z > 0 ? angle : -angle;
    
}

void SE_MotionEventCameraController::pitchMove()
{
    if(!mHasPicth && !mCanReturn && !mDone)
    {
        mCurCamera->rotateLocal(-30,(SE_AXIS_TYPE)0);
        mHasPicth = true;
    }
}

void SE_MotionEventCameraController::resetPitchMove()
{
    if(mHasPicth)
    {
        mCurCamera->rotateLocal(30,(SE_AXIS_TYPE)0);
    }
}


void SE_MotionEventCameraController::flushScreen()
{
    SE_Message* msg = new SE_Message;
    msg->type = SE_MSG_UPATEWORLD;
    SE_Struct* sestruct = new SE_Struct(1);
    SE_StructItem* sitem = new SE_StructItem(1);
    SE_StdString* stdString = new SE_StdString;
    stdString->data = "update world.";
    sitem->setDataItem(stdString);
    sestruct->setStructItem(0, sitem);
    msg->data = sestruct;
    SE_Application::getInstance()->sendMessage(msg);
    
}