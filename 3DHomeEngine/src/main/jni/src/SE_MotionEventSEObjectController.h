#ifndef SE_MotionEventSEObjectController_H
#define SE_MotionEventSEObjectController_H
#include "SE_Object.h"
#include "SE_SimObject.h"
#include "SE_InputEvent.h"
class SE_MotionEventController;
class SE_ENTRY SE_MotionEventSEObjectController :public SE_Object
{
public:
    SE_MotionEventSEObjectController(SE_MotionEventController* c);
    virtual ~SE_MotionEventSEObjectController();

    enum ControllerType
    {
        CAMERA_CONTROLLER,
        OBJECT_CONTROLLER
    };

    void setControllerName(const char *name)
    {
        mControllerName = name;    
    }

    const char * getControllerName()
    {
        return mControllerName.c_str();    
    }

    void setObjName(const char *objname)
    {
        mNameOfObj = objname;
    }

    const char *getObjName()
    {
        return mNameOfObj.c_str();
    }

    bool setObjPartial(const char *partialname,bool shown);

    bool objIsExist();
    bool objHasMove();
    void flushScreen();
    virtual void sendObjectName();

    std::string mControllerName;
    std::string mNameOfObj;
    SE_SimObject *mObject;
    std::vector<SE_SimObject *> mObjects;
    ControllerType mType;
    virtual void clearState();
protected:
    
    SE_MotionEvent::TYPE mPrevType;
    float mPrevX;
    float mPrevY;
    SE_MotionEventController* mMotionEventController;
};
#endif