#ifndef SE_KEYEVENT_CONTROLLER_H
#define SE_KEYEVENT_CONTROLLER_H
#include "SE_Object.h"
#include "SE_InputEvent.h"
class SE_ENTRY SE_KeyEventController : public SE_Object
{
public:
    SE_KeyEventController();
    virtual ~SE_KeyEventController();
    virtual void onKeyEvent(SE_KeyEvent* keyEvent);
};
#endif
