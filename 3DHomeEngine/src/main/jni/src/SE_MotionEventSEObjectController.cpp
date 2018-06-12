#include "SE_DynamicLibType.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"
#include "SE_Application.h"
#include "SE_SimObject.h"
#include "SE_SimObjectManager.h"
#include "SE_MemLeakDetector.h"

SE_MotionEventSEObjectController::SE_MotionEventSEObjectController(SE_MotionEventController* c)
{
    mObject = NULL;
    mMotionEventController = c;
}

SE_MotionEventSEObjectController::~SE_MotionEventSEObjectController()
{
    std::vector<SE_SimObject*>::iterator it;
    it = mObjects.begin();
    for(;it != mObjects.end();)
    {
        SE_SimObject *c = *it;
        delete c;
    }
    mObjects.clear();    
}


void SE_MotionEventSEObjectController::flushScreen()
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

bool SE_MotionEventSEObjectController::setObjPartial(const char *partialname,bool shown)
{
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();

    SE_SimObject *partial = simObjectManager->findByName(partialname);

    if(partial)
    {
        partial->getSpatial()->setVisible(shown);
        mObjects.push_back(partial);
        return true;
    }
    else
    {
        return false;
    }
}

bool SE_MotionEventSEObjectController::objIsExist()
{    
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();

    mObject = simObjectManager->findByName(mControllerName.c_str());

    if(mObject)
    {
        return true;
    }
    else
    {
        return false;
    }

}

bool SE_MotionEventSEObjectController::objHasMove()
{
    if(mObject)
    {
        //obj current position
        SE_Matrix4f m = mObject->getSpatial()->getWorldTransform(); 

        SE_Vector3f currPos = m.getTranslate();
        
        //obj origine position
        SE_Vector3f basePosition = mObject->getSpatial()->getLocalTranslate();

        if(currPos.subtract(basePosition).isZero())
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    return false;
}

void SE_MotionEventSEObjectController::clearState()
{

    mPrevX = 0.0;
    mPrevY = 0.0;
    mPrevType = SE_MotionEvent::UP;
}

void SE_MotionEventSEObjectController::sendObjectName()
{
    SE_Message* msg = new SE_Message;
    msg->type = SE_MSG_SIMOBJECT_NAME;
    SE_Struct* sestruct = new SE_Struct(1);
    SE_StructItem* sitem = new SE_StructItem(1);
    SE_StdString* stdString = new SE_StdString;
    stdString->data = getObjName();
    sitem->setDataItem(stdString);
    sestruct->setStructItem(0, sitem);
    msg->data = sestruct;
    SE_Application::getInstance()->sendMessage(msg);
}
