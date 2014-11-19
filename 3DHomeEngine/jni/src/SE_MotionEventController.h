#ifndef SE_MotionEventController_H
#define SE_MotionEventController_H
#include "SE_Object.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_MotionEventCameraController.h"
//#include "SE_MotionEventSimObjectController.h"
#include "SE_InputEvent.h"
class SE_MotionEvent;
class SE_Camera;
class SE_SimObject;
class SE_ENTRY SE_MotionEventController : public SE_Object
{
DECLARE_OBJECT(SE_MotionEventController)
public:
    SE_MotionEventController();
    ~SE_MotionEventController();
    void onMotionEvent(SE_MotionEvent* motionEvent);

    SE_MotionEventCameraController *getCameraController()
    {
        return mCurrentCamera;
    }

    SE_Object *getCameraController(const char *controllername)
    {
        return NULL;
    }

    SE_Object *getObjectController(const char *controllername)
    {
        return findController(controllername);
    }

    const char * getCurrentControllerName()
    {
        return mCurrentControllerName.c_str();
    }

private:
    enum TargetType
    {
        NONE = -1,
        CAMERA_MOVE,
        OBJECT_MOVE,
        OTHER
    };

    TargetType mTargetType;

    void clearState();
    void MotionEventTarget(const char* currentObjName);
    void dispatchMotionEvent(SE_MotionEvent* motionEvent);

    void dispatchMotionEventToAll(SE_MotionEvent* motionEvent);

    void resetNonTargetController();

    SE_MotionEventSEObjectController *findController(const char *controllername);

    const char * getCurrentGeometryName(float screenX,float screenY);

    SE_MotionEvent::TYPE mPrevType;
    float mPrevX;
    float mPrevY;

    std::vector<SE_MotionEventSEObjectController *> mMotionControllers;    

    SE_MotionEventSEObjectController * mCurrentController;

    std::string mCurrentGeometryName;
    
    SE_MotionEventCameraController * mCurrentCamera;

    SE_MotionEventSEObjectController *mDoNotReset;

    std::string mCurrentControllerName;
    
};

#endif