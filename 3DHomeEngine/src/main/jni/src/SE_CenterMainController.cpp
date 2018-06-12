#include "SE_DynamicLibType.h"
#include "SE_Object.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_MotionEventSimObjectController.h"
#include <vector>

#include "SE_CenterMainController.h"
#include "SE_SimObjectManager.h"
#include "SE_Application.h"
#include "SE_MotionEventController.h"
#include "SE_InputManager.h"

#include "SE_MemLeakDetector.h"


SE_CenterMainController::SE_CenterMainController(bool objOnTable) 
{

    mCurretController = NULL;
    mNextController = NULL;

    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();

    mPositiveXController = new SE_CenterMainSimObjectController();
    mPositiveXController->setControllerName("positiveX");
    mPositiveXController->setObjPartial("laptop",objOnTable);    
    mCenterControllers.push_back(mPositiveXController);

    mPositiveYController = new SE_CenterMainSimObjectController();
    mPositiveYController->setControllerName("positiveY");
    mPositiveYController->setObjPartial("phone_base",objOnTable);
    mPositiveYController->setObjPartial("phone_receiver",objOnTable);    
    mCenterControllers.push_back(mPositiveYController);

    mNegativeXController = new SE_CenterMainSimObjectController(NULL);
    mNegativeXController->setControllerName("negativeX");
    mNegativeXController->setObjPartial("other",objOnTable);    
    mCenterControllers.push_back(mNegativeXController);

    mNegativeYController = new SE_CenterMainSimObjectController(NULL);
    mNegativeYController->setControllerName("negativeY");
    mNegativeYController->setObjPartial("book",true);
    mCenterControllers.push_back(mNegativeYController);

    mCurretController = mNegativeYController;

}

SE_CenterMainController::~SE_CenterMainController()
{
    std::vector<SE_MotionEventSEObjectController*>::iterator it;
    it = mCenterControllers.begin();
    for(;it != mCenterControllers.end();it++)
    {
        SE_MotionEventSEObjectController *c = *it;
        delete c;
    }
    mCenterControllers.clear();    
}

SE_MotionEventSEObjectController* SE_CenterMainController::findController(const char *controllerName)
{
    for(int i = 0; i < mCenterControllers.size(); ++i)
    {
        if(!strcmp(controllerName,mCenterControllers[i]->getControllerName()))
        {
            return mCenterControllers[i];
        }
    }
    
    return NULL;
}

void SE_CenterMainController::changeController()
{
    if(mCurretController == mNextController)
    {
        return;
    }

    if(mCurretController)
    {
        ((SE_CenterMainSimObjectController *)mCurretController)->fadeOutAnimation();
    }

    if(mNextController)
    {
        ((SE_CenterMainSimObjectController *)mNextController)->fadeInAnimation();
    }

    mCurretController = mNextController;
}