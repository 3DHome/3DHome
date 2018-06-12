#include "SE_DynamicLibType.h"
#include "SE_MotionEventSimObjectController.h"
#include"SE_MotionEventController.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"
#include "SE_Geometry3D.h"
#include "SE_Spatial.h"
#include "SE_SpatialTravel.h"
#include "SE_SimObject.h"
#include "SE_Log.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"
#include "SE_Camera.h"
#include "SE_MemLeakDetector.h"
IMPLEMENT_OBJECT(SE_MotionEventController)
SE_MotionEventController::SE_MotionEventController()
{
    mPrevType = SE_MotionEvent::UP;
    mPrevX = 0;
    mPrevY = 0;

    mCurrentController = NULL;
    mDoNotReset = NULL;


    SE_MotionEventSEObjectController * clock = new SE_MotionEventClockSimObjectController(this);
    clock->setControllerName("CLOCK");
    mMotionControllers.push_back(clock);

    SE_MotionEventSEObjectController * phone = new SE_MotionEventPhoneRingOnSimObjectController(this);
    phone->setControllerName("PHONE_BASE");
    phone->setObjPartial("phone_base",true);
    phone->setObjPartial("phone_receiver",true);
    mMotionControllers.push_back(phone);

    SE_MotionEventSEObjectController * answercall = new SE_MotionEventPhoneAnswerCallSimObjectController(this);
    answercall->setControllerName("PHONE_RECEIVER");
    answercall->setObjName("phone_receiver");    
    mMotionControllers.push_back(answercall);

    SE_MotionEventSEObjectController * minute = new SE_MotionEventAdjustTimeSimObjectController(this);
    minute->setControllerName("large_minute");
    mMotionControllers.push_back(minute);

    SE_MotionEventSEObjectController * hour = new SE_MotionEventAdjustTimeSimObjectController(this);
    hour->setControllerName("large_hour");
    mMotionControllers.push_back(hour);

    SE_MotionEventCameraMoveSimObjectController * readbook= new SE_MotionEventCameraMoveSimObjectController(this);
    readbook->setTargetPos(SE_Vector3f(134,-60,93));
    readbook->setTargetZ(SE_Vector3f(0,-1,0));
    readbook->setControllerName("read_book");
    readbook->setObjName("book");
    mMotionControllers.push_back(readbook);

    SE_MotionEventCameraMoveSimObjectController * shortcut= new SE_MotionEventCameraMoveSimObjectController(this);
    shortcut->setTargetPos(SE_Vector3f(20,-270,93));
    shortcut->setTargetZ(SE_Vector3f(1,0,0));
    shortcut->setControllerName("read_shortcut");
    shortcut->setObjName("laptop");
    mMotionControllers.push_back(shortcut);

    mCurrentCamera = new SE_MotionEventCameraController(this);
    mCurrentCamera->setControllerName("ROTATECAMERA");
    mMotionControllers.push_back(mCurrentCamera);      
}

SE_MotionEventController::~SE_MotionEventController()
{    
    for(int i = 0; i < mMotionControllers.size(); ++i)
    {
        if(mMotionControllers[i])
        {
            delete mMotionControllers[i];
        }
    }
    
    LOGI("### destroctor ~SE_MotionEventController ####\n");
}

void SE_MotionEventController::clearState()
{
    mPrevType = SE_MotionEvent::UP;
    mPrevX = 0;
    mPrevY = 0;

    // Do not reset mTargetType for autoxxx 

}

void SE_MotionEventController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
    {
        return;
    }

    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {

        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();

        dispatchMotionEventToAll(motionEvent);
     
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
        dispatchMotionEventToAll(motionEvent);
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        //Click
         const char * name = getCurrentGeometryName(motionEvent->getX(),motionEvent->getY());

        if(name != NULL)
        {
            mCurrentGeometryName = name;
            MotionEventTarget(mCurrentGeometryName.c_str());
        }
        else
        {
            //camera move
            MotionEventTarget(mCurrentGeometryName.c_str());
        }

        resetNonTargetController();
        dispatchMotionEvent(motionEvent); 
        clearState();
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        dispatchMotionEventToAll(motionEvent);
        clearState();
    }
    else
    {
        //auto-xxx event
        dispatchMotionEventToAll(motionEvent);
    }
    
    //clearState();

}

SE_MotionEventSEObjectController * SE_MotionEventController::findController(const char * controllername)
{
    std::string name = controllername;

    SE_MotionEventSEObjectController * controller = NULL;

    for(int i = 0; i < mMotionControllers.size(); ++i)
    {

        if(!strcmp(name.c_str(),mMotionControllers[i]->mControllerName.c_str()))
        {
            controller = mMotionControllers[i];
            return mMotionControllers[i];
        }
    }    
    return NULL;
}

const char *SE_MotionEventController::getCurrentGeometryName(float screenX,float screenY)
{
    SE_Camera *camera = SE_Application::getInstance()->getCurrentCamera();
    SE_Ray ray = camera->screenCoordinateToRay(screenX, screenY);
    SE_FindSpatialCollision spatialCollision(ray);
    SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getTopScene()->getRoot();
    root->travel(&spatialCollision, true);
    SE_Spatial* collisionSpatial = spatialCollision.getCollisionSpatial();
    SE_SimObject *so = spatialCollision.getCollisionObject();
    if(collisionSpatial)
    {
        SE_Application::getInstance()->getSceneManager()->getTopScene()->setSelectedSpatial(collisionSpatial);
        //collisionSpatial->setSelected(true);
    }

    if(so)
    {
        LOGI("## selected object = %s ####\n", so->getName());
        SE_Vector3f loc = so->getSpatial()->getLocalTranslate();
        LOGI("## object location is x = %f y = %f z = %f ###\n",loc.x,loc.y,loc.z);
        return so->getName(); 
    }
    else
    {
        return NULL;
    }
    
}

void SE_MotionEventController::dispatchMotionEvent(SE_MotionEvent* motionEvent) 
{
    mCurrentControllerName = mCurrentController->getControllerName();

    if(mCurrentCamera->getCanReturn())
    {
        mCurrentController->clearState();
        return;
    }

    switch(mTargetType)
    {
    case NONE:
        break;
    case CAMERA_MOVE:
        mCurrentController->onMotionEvent(motionEvent);
        break;
    case OBJECT_MOVE:
        mCurrentController->onMotionEvent(motionEvent);
        break;
    case OTHER:
        mCurrentController->onMotionEvent(motionEvent);        
        break;
    }
}

void SE_MotionEventController::dispatchMotionEventToAll(SE_MotionEvent* motionEvent) 
{
    for(int i = 0; i < mMotionControllers.size(); ++i)
    {
        SE_MotionEventSEObjectController * controller = mMotionControllers[i];
        controller->onMotionEvent(motionEvent);
    }    
}

void SE_MotionEventController::resetNonTargetController() 
{
    for(int i = 0; i < mMotionControllers.size(); ++i)
    {
        SE_MotionEventSEObjectController * controller = mMotionControllers[i];

        if(controller == mCurrentController || controller == mDoNotReset)
        {
            continue;
        }
        controller->clearState();
    }
    
}

void SE_MotionEventController::MotionEventTarget(const char *currentObjName) 
{
    if(!strcmp(currentObjName,"large_clock") || !strcmp(currentObjName,"background_for_clock"))
    {        
        std::string name = "CLOCK";
        mCurrentController = findController(name.c_str());
        if(mCurrentController)
        {
            if(mCurrentController->mType  == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {
                mTargetType = OBJECT_MOVE;
            }
        }
    }    
    else if(!strcmp(currentObjName,"book") || !strcmp(currentObjName,"____"))
    {        
        std::string name = "read_book";
        mCurrentController = findController(name.c_str());
        if(mCurrentController)
        {
            if(mCurrentController->mType  == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {
                mTargetType = OBJECT_MOVE;
            }
        }
    }
    else if(!strcmp(currentObjName,"laptop"))
    {        
        std::string name = "read_shortcut";
        mCurrentController = findController(name.c_str());
        if(mCurrentController)
        {
            if(mCurrentController->mType  == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {
                mTargetType = OBJECT_MOVE;
            }
        }
    }    
    else if(!strcmp(currentObjName,"phone_base"))
    {
        mCurrentController = findController("PHONE_BASE");
        if(mCurrentController)
        {
            if(mCurrentController->mType == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {                
                mTargetType = OBJECT_MOVE;
            }
        }
    }
    else if(!strcmp(currentObjName,"phone_receiver"))
    {
        mCurrentController = findController("PHONE_RECEIVER");
        if(mCurrentController)
        {
            if(mCurrentController->mType == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {                
                mTargetType = OBJECT_MOVE;
            }
        }
    }
    else if(!strcmp(currentObjName,"app_box"))
    {
        mCurrentController = findController("app_background");
        if(mCurrentController)
        {
            if(mCurrentController->mType == SE_MotionEventSEObjectController::OBJECT_CONTROLLER)
            {                
                mTargetType = OBJECT_MOVE;
            }
        }
    }    
    else
    {
        

        if(!mCurrentCamera->getCanReturn())
        {
            mTargetType = CAMERA_MOVE;
            mCurrentController = findController("ROTATECAMERA");
        }
    }
}
