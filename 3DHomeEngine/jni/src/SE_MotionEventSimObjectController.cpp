#include "SE_DynamicLibType.h"
#include "SE_MotionEventSimObjectController.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"
#include "SE_SimObject.h"
#include "SE_Factory.h"
#include "SE_Camera.h"
#include "SE_UserCommand.h"
#include "SE_SystemCommand.h"
#include "SE_SimObjectManager.h"
#include "SE_Mesh.h"
#include "SE_DataValueDefine.h"
#include "SE_Log.h"
#include "SE_Geometry3D.h"
#include "SE_Spatial.h"
#include "SE_SpatialTravel.h"

#include "SE_AnimationManager.h"
#include "SE_Animation.h"
#include "SE_ObjectAnimation.h"
#include "SE_InputManager.h"
#include "SE_MotionEventController.h"
#include "SE_MotionEventCameraController.h"
#include "SE_MemLeakDetector.h"

SE_MotionEventClockSimObjectController::SE_MotionEventClockSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{
    mPrevType = SE_MotionEvent::UP;
    mType = SE_MotionEventSEObjectController::OBJECT_CONTROLLER;

}

SE_MotionEventClockSimObjectController::~SE_MotionEventClockSimObjectController()
{
}

void SE_MotionEventClockSimObjectController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
        return;
    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();        

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
        
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        mPrevType =SE_MotionEvent::UP;

    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::UP;

        //if autoXXX not complete,continue.

        if(!mObject)
        {
            SE_Ray ray = SE_Application::getInstance()->getCurrentCamera()->screenCoordinateToRay(mPrevX, mPrevY);
            SE_FindSpatialCollision spatialCollision(ray);
            SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getTopScene()->getRoot();
            root->travel(&spatialCollision, true);
            SE_Spatial* collisionSpatial = spatialCollision.getCollisionSpatial();
            SE_SimObject* so = spatialCollision.getCollisionObject();
            if(collisionSpatial)
            {
                SE_Application::getInstance()->getSceneManager()->getTopScene()->setSelectedSpatial(collisionSpatial);
                //collisionSpatial->setSelected(true);
            }
            if(so)
            {
                LOGI("## selected object = %s ####\n", so->getName());
                
            }
            //when move geometry need touch obj on screen can load a "return" process
            doMoveTo(so->getName());
        }
        else
        {
            doMoveTo(mObject->getName());
        }
        
        
    }
}

void SE_MotionEventClockSimObjectController::doMoveTo(const char *objname)
{

    if(!strcmp(objname,"large_clock"))
    {
        SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
        SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();


        mObject = simObjectManager->findByName("large_clock");


        if(!mObject)
        {
            return;
        }

        SE_Spatial* spatial = mObject->getSpatial();

        if(!spatial->isVisible())
        {
            spatial->setVisible(true);
        }

        mAnimation = animManager->getAnimation(spatial->getAnimationID());

        if(mAnimation)
        {
            if(mAnimation->getAnimState() == SE_Animation::RUNNING)
            {
                return;
            }
            else
            {
                if(!objHasMove())
                {
                    mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
                }
                else
                {
                    mAnimation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
                }
                mAnimation->run();
            }
        }
        else
        {
            SE_ObjectClockMoveAnimation* objClockMoveAnimation = new SE_ObjectClockMoveAnimation();
            objClockMoveAnimation->setMotionEventController(mMotionEventController);
            mAnimation = objClockMoveAnimation;
            

            
            mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
           

            mAnimation->setTimeMode(SE_Animation::REAL);
            mAnimation->setDuration(1000);
            SE_ObjectClockMoveAnimation * anim = (SE_ObjectClockMoveAnimation *)mAnimation;
            //anim->setSimObject(mObject);
            

            //set play mode with SHADER
            //anim->setPlayMode(SE_Animation::GPU_SKELETON_SHADER);
            mAnimation->setPlayMode(SE_Animation::CPU_NO_SHADER);


            animManager->removeAnimation(spatial->getAnimationID());
            SE_AnimationID animID = animManager->addAnimation(mAnimation,"");
            spatial->setAnimationID(animID);
            mAnimation->run();
        }       

    }
    else
    {
        return;
    }
    
}
///////////////////////////////////minute///////////////////
SE_MotionEventAdjustTimeSimObjectController::SE_MotionEventAdjustTimeSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{
    mPrevType = SE_MotionEvent::UP;
    mType = SE_MotionEventSEObjectController::OBJECT_CONTROLLER;

}

SE_MotionEventAdjustTimeSimObjectController::~SE_MotionEventAdjustTimeSimObjectController()
{
}

void SE_MotionEventAdjustTimeSimObjectController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
        return;
    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        detectObject();

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();

    }
    else if((mPrevType == SE_MotionEvent::DOWN || mPrevType == SE_MotionEvent::MOVE)&& 
                motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        //maybe move
        float deltaX = motionEvent->getX() - mPrevX;
        float deltaY = motionEvent->getY() - mPrevY;

        
        mPrevType = SE_MotionEvent::MOVE;

        if(mObject)
        {
            //User push this controller object(minute or hour)
            SE_Rect<int> viewport = SE_Application::getInstance()->getCurrentCamera()->getViewport();
            int viewportWidth = viewport.right - viewport.left;
            float ratio = 45.0f / viewportWidth;        
            float angle = ratio * deltaX; 
            rotate(angle);
        }

        
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        mObject = NULL;
        mPrevType =SE_MotionEvent::UP;

    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mObject = NULL;
        mPrevType = SE_MotionEvent::UP;        
    }
}

void SE_MotionEventAdjustTimeSimObjectController::rotate(float angle)
{
    SE_RotateSpatialCommand* c = (SE_RotateSpatialCommand*)SE_Application::getInstance()->createCommand("SE_RotateSpatialCommand");

    //set rotate angle per ticket
    c->mAxis = (SE_AXIS_TYPE)2;
    c->mRotateAngle = angle;
    c->mObjectName = getControllerName();        

    //post this command to command queue
    SE_Application::getInstance()->postCommand(c);
}

SE_SimObject *SE_MotionEventAdjustTimeSimObjectController::detectObject()
{
    if(mObject)
    {
        return mObject;
    }

    SE_Ray ray = SE_Application::getInstance()->getCurrentCamera()->screenCoordinateToRay(mPrevX, mPrevY);
    SE_FindSpatialCollision spatialCollision(ray);
	SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getTopScene()->getRoot();
    root->travel(&spatialCollision, true);
    SE_Spatial* collisionSpatial = spatialCollision.getCollisionSpatial();
    SE_SimObject* so = spatialCollision.getCollisionObject();

    if(so)
    {
    if(!strcmp(so->getName(),getControllerName()))
    {
        mObject = so;
        return mObject;
    }
    else
    {
        return NULL;
    }
}
    else
    {
        return NULL;
    }
}

///////////////////////////////////phone////////////////////
SE_MotionEventPhoneRingOnSimObjectController::SE_MotionEventPhoneRingOnSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{

    mPrevType = SE_MotionEvent::UP;
    
    mType = SE_MotionEventSEObjectController::OBJECT_CONTROLLER;

}

SE_MotionEventPhoneRingOnSimObjectController::~SE_MotionEventPhoneRingOnSimObjectController()
{

}

void SE_MotionEventPhoneRingOnSimObjectController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
        return;
    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();        

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
        
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        mPrevType =SE_MotionEvent::UP;        
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::UP;


        SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
        SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();


        mObject = mObjects[1];


        if(!mObject)
        {
            return;
        }

        SE_Spatial* spatial = mObject->getSpatial();

        if(!spatial->isVisible())
        {
            spatial->setVisible(true);
        }

        mAnimation = animManager->getAnimation(spatial->getAnimationID());

        if(mAnimation)
        {
            if(mAnimation->getAnimState() == SE_Animation::RUNNING)
            {
                return;
            }
            else
            {
                animManager->removeAnimation(spatial->getAnimationID());
            }
        }
       

        mAnimation = new SE_ObjectPhoneRingOnAnimation();


        mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
        mAnimation->setTimeMode(SE_Animation::REAL);
        mAnimation->setDuration(3000);
        SE_ObjectPhoneRingOnAnimation * anim = (SE_ObjectPhoneRingOnAnimation *)mAnimation;
        anim->setSimObject(mObject);        
        
        mAnimation->setPlayMode(SE_Animation::CPU_NO_SHADER);

        animManager->removeAnimation(spatial->getAnimationID());
        SE_AnimationID animID = animManager->addAnimation(mAnimation,"");
        spatial->setAnimationID(animID);
        mAnimation->run();
       
    }   

}

///////////////////////////////answer call/////////////////
SE_MotionEventPhoneAnswerCallSimObjectController::SE_MotionEventPhoneAnswerCallSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{

    mPrevType = SE_MotionEvent::UP;
    
    mType = SE_MotionEventSEObjectController::OBJECT_CONTROLLER;

}

SE_MotionEventPhoneAnswerCallSimObjectController::~SE_MotionEventPhoneAnswerCallSimObjectController()
{

}

void SE_MotionEventPhoneAnswerCallSimObjectController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
        return;
    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();        

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
        
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        mPrevType =SE_MotionEvent::UP;        
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::UP;

        sendObjectName();
        
    }   

}


///////////////////////////////////readbook////////////////////
SE_MotionEventCameraMoveSimObjectController::SE_MotionEventCameraMoveSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{

    mPrevType = SE_MotionEvent::UP;
    
    mType = SE_MotionEventSEObjectController::OBJECT_CONTROLLER;
}

SE_MotionEventCameraMoveSimObjectController::~SE_MotionEventCameraMoveSimObjectController()
{

}

void SE_MotionEventCameraMoveSimObjectController::onMotionEvent(SE_MotionEvent* motionEvent)
{
    if(motionEvent == NULL)
        return;
    if(mPrevType ==  SE_MotionEvent::UP && motionEvent->getType() == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::DOWN;

        mPrevX = motionEvent->getX();
        mPrevY = motionEvent->getY();        

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
        
     }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::MOVE)
    {
        mPrevType =SE_MotionEvent::UP;        
    }
    else if(motionEvent->getType() == SE_MotionEvent::UP && mPrevType == SE_MotionEvent::DOWN)
    {
        mPrevType = SE_MotionEvent::UP;


        SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
        SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();


        mObject = simObjectManager->findByName(getObjName());


        if(!mObject)
        {
            return;
        }

        SE_Spatial* spatial = mObject->getSpatial();
        

        mAnimation = animManager->getAnimation(spatial->getAnimationID());

        if(mAnimation)
        {
            if(mAnimation->getAnimState() == SE_Animation::RUNNING)
            {
                return;
            }
            else
            {
                animManager->removeAnimation(spatial->getAnimationID());
#if 0
                SE_MotionEventController * controller = (SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
                SE_MotionEventCameraController *cameraController = controller->getCameraController();

                if(!cameraController->getCanReturn())
                {
                    mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
                    mAnimation->run();
                    sendObjectName();
                }
                else
                {
                    //mAnimation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
                }
#endif
            }
        }        
        SE_ObjectCameraTranslateAnimation* objCameraTranslateAnimation = new SE_ObjectCameraTranslateAnimation();
        objCameraTranslateAnimation->setMotionEventController(mMotionEventController);
        mAnimation = objCameraTranslateAnimation;

        mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
        mAnimation->setTimeMode(SE_Animation::REAL);
        mAnimation->setDuration(1000);
        SE_ObjectPhoneRingOnAnimation * anim = (SE_ObjectPhoneRingOnAnimation *)mAnimation;
        anim->setSimObject(mObject);
        

        //set play mode with SHADER
        //anim->setPlayMode(SE_Animation::GPU_SKELETON_SHADER);
        mAnimation->setPlayMode(SE_Animation::CPU_NO_SHADER);

        //init camera animation info
        ((SE_ObjectCameraTranslateAnimation*)mAnimation)->setTargetPosition(mTargetPos);
        ((SE_ObjectCameraTranslateAnimation*)mAnimation)->setTargetZ(mTargetZ);


        animManager->removeAnimation(spatial->getAnimationID());
        SE_AnimationID animID = animManager->addAnimation(mAnimation,"");
        spatial->setAnimationID(animID);
        mAnimation->run();
        sendObjectName();
    }    

}

void SE_MotionEventCameraMoveSimObjectController::returnToBack()
{
    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
    SE_MotionEventCameraController *cameraController = controller->getCameraController();

    if(cameraController->getCanReturn())
    {
        mAnimation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
        mAnimation->run();
    }

    
}

void SE_MotionEventCameraMoveSimObjectController::returnToHome()
{
    SE_MotionEventController * controller = mMotionEventController;//(SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
    SE_MotionEventCameraController *cameraController = controller->getCameraController();

    if(cameraController->getCanReturn())
    {
        ((SE_ObjectCameraTranslateAnimation *)mAnimation)->setBackupCameraPos(cameraController->getCameraHomePositionAtStatic());
        ((SE_ObjectCameraTranslateAnimation *)mAnimation)->setBackupCameraPosZ(cameraController->getCameraHomePositionStaticZ());

        mAnimation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
        mAnimation->run();
    }


}


/////////////////////////////////center main controller///////////////////////////////
SE_CenterMainSimObjectController::SE_CenterMainSimObjectController(SE_MotionEventController* c) : SE_MotionEventSEObjectController(c)
{
}

SE_CenterMainSimObjectController::~SE_CenterMainSimObjectController()
{}

void SE_CenterMainSimObjectController::fadeInAnimation()
{
    LOGI("%s fade in !!\n",getControllerName());

    SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();    


    if(mObjects.size() == 0)
    {
        return;
    }

    for(int i = 0; i < mObjects.size(); ++i)
    {

        SE_Spatial* spatial = mObjects[i]->getSpatial();

        if(!spatial->isVisible())
        {
            spatial->setVisible(true);
        }

        SE_Animation *animation = animManager->getAnimation(spatial->getAnimationID());

        if(animation)
        {
            if(animation->getAnimState() == SE_Animation::RUNNING)
            {
                return;
            }
            else
            {
                animManager->removeAnimation(spatial->getAnimationID());
                //mAnimation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
                //mAnimation->run();
            }
        }        
        SE_ObjectFadeInOutAnimation* objFadeInOutAnim =  new SE_ObjectFadeInOutAnimation();
        objFadeInOutAnim->setMotionEventController(mMotionEventController);
        animation = objFadeInOutAnim;
        animation->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
        animation->setTimeMode(SE_Animation::REAL);
        animation->setDuration(1000);
        SE_ObjectFadeInOutAnimation * anim = (SE_ObjectFadeInOutAnimation *)animation;
        anim->setSimObject(mObjects[i]);
        

        //set play mode with SHADER
        //anim->setPlayMode(SE_Animation::GPU_SKELETON_SHADER);
        animation->setPlayMode(SE_Animation::CPU_NO_SHADER);


        animManager->removeAnimation(spatial->getAnimationID());
        SE_AnimationID animID = animManager->addAnimation(animation,"");
        spatial->setAnimationID(animID);
        animation->run();
         
    }

    
}

void SE_CenterMainSimObjectController::fadeOutAnimation()
{
    LOGI("%s fade out !!\n",getControllerName());
    SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();

    if(mObjects.size() == 0)
    {
        return;
    }

    for(int i = 0; i < mObjects.size(); ++i)
    {
        SE_Spatial* spatial = mObjects[i]->getSpatial();

        SE_Animation *animation = animManager->getAnimation(spatial->getAnimationID());

        if(animation)
        {
            if(animation->getAnimState() == SE_Animation::RUNNING)
            {
                return;
            }
            else
            {
                animManager->removeAnimation(spatial->getAnimationID());
                //mAnimation->setRunMode(SE_Animation::NOT_REPEAT);
                //mAnimation->run();
            }
        }       

        animation = new SE_ObjectFadeInOutAnimation();

        animation->setRunMode(SE_Animation::NOT_REPEAT);
        animation->setTimeMode(SE_Animation::REAL);
        animation->setDuration(1000);
        SE_ObjectFadeInOutAnimation * anim = (SE_ObjectFadeInOutAnimation *)animation;
        anim->setSimObject(mObjects[i]);
        

        //set play mode with SHADER
        //anim->setPlayMode(SE_Animation::GPU_SKELETON_SHADER);
        animation->setPlayMode(SE_Animation::CPU_NO_SHADER);


        animManager->removeAnimation(spatial->getAnimationID());
        SE_AnimationID animID = animManager->addAnimation(animation,"");
        spatial->setAnimationID(animID);
        animation->run();
        
    }

}
