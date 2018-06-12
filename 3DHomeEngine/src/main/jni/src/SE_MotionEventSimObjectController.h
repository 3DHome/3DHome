#ifndef SE_MotionEventSimObjectController_H
#define SE_MotionEventSimObjectController_H
#include "SE_Object.h"
#include "SE_MotionEventSEObjectController.h"
#include "SE_Animation.h"
#include "SE_Time.h"
#include "SE_InputEvent.h"
#include <list>

class SE_ENTRY SE_MotionEventClockSimObjectController : public SE_MotionEventSEObjectController
{
DECLARE_OBJECT(SE_MotionEventClockSimObjectController)
public:
    SE_MotionEventClockSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_MotionEventClockSimObjectController();
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);

    bool canAdjust()
    {
        return objHasMove();
    }
    void setMotionEventController(SE_MotionEventController* c)
    {
        mMotionEventController = c;
    }
private:
    void doMoveTo(const char *objname);

    SE_Animation* mAnimation;    
};

class SE_ENTRY SE_MotionEventAdjustTimeSimObjectController : public SE_MotionEventSEObjectController
{
public:
    SE_MotionEventAdjustTimeSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_MotionEventAdjustTimeSimObjectController();
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
    
private:
    void rotate(float angle);
    SE_SimObject * detectObject();
};


class SE_ENTRY SE_MotionEventPhoneRingOnSimObjectController : public SE_MotionEventSEObjectController
{
public:
    SE_MotionEventPhoneRingOnSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_MotionEventPhoneRingOnSimObjectController();
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
    SE_Animation * getAnimation()
    {
        return mAnimation;
    }
private:    
    SE_Animation* mAnimation;
};

class SE_ENTRY SE_MotionEventPhoneAnswerCallSimObjectController : public SE_MotionEventSEObjectController
{
public:
    SE_MotionEventPhoneAnswerCallSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_MotionEventPhoneAnswerCallSimObjectController();
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
    SE_Animation * getAnimation()
    {
        return mAnimation;
    }
private:

    SE_Animation* mAnimation;
};

class SE_ENTRY SE_MotionEventCameraMoveSimObjectController : public SE_MotionEventSEObjectController
{
public:
    SE_MotionEventCameraMoveSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_MotionEventCameraMoveSimObjectController();
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
    SE_Animation * getAnimation()
    {
        return mAnimation;
    }

    void returnToBack();
    void returnToHome();

    void setTargetPos(SE_Vector3f target)
    {
        mTargetPos = target;
    }

    void setTargetZ(SE_Vector3f targetZ)
    {
        mTargetZ = targetZ;
    }
private:
    
    SE_Vector3f mTargetPos;
    SE_Vector3f mTargetZ;
    
    SE_Animation* mAnimation;
    
};

class SE_ENTRY SE_CenterMainSimObjectController : public SE_MotionEventSEObjectController
{
public:
    SE_CenterMainSimObjectController(SE_MotionEventController* c = NULL);
    ~SE_CenterMainSimObjectController();

    void fadeInAnimation();
    void fadeOutAnimation();

private:
    SE_Animation* mAnimation;
};
#endif