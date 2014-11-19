#ifndef SE_CENTERMAINCONTROLLER_H
#define SE_CENTERMAINCONTROLLER_H

class SE_MotionEventSEObjectController;
class SE_MotionEventSimObjectController;

class SE_ENTRY SE_CenterMainController
{
public:
    SE_CenterMainController(bool objOnTable);
    virtual ~SE_CenterMainController();

    void setCurrentController(const char *controllerName)
    {
        mCurretController = findController(controllerName);
    }

    void setNextController(const char *controllerName)
    {
        mNextController = findController(controllerName);
    }

    void changeController();

private:
    std::vector<SE_MotionEventSEObjectController*> mCenterControllers;
    SE_MotionEventSEObjectController * mCurretController;
    SE_MotionEventSEObjectController * mNextController;

    SE_MotionEventSEObjectController* findController(const char *controllerName);

    SE_MotionEventSEObjectController *mPositiveXController;
    SE_MotionEventSEObjectController *mPositiveYController;
    SE_MotionEventSEObjectController *mNegativeXController;
    SE_MotionEventSEObjectController *mNegativeYController;

};


#endif