#ifndef SE_TIMER
#define SE_TIMER
#include "SE_Time.h"
class SE_ENTRY SE_Timer
{
public:
    struct TimePair
    {
        SE_TimeMS realTimeDelta;
        SE_TimeMS simulateTimeDelta;
    };
    SE_Timer(SE_TimeMS frameRate = 30);
    void start();
    TimePair step();
private:
    SE_TimeMS mStartTime;
    SE_TimeMS mLastTime;
    SE_TimeMS mFrameRate;
};
#endif
