#include "SE_DynamicLibType.h"
#include "SE_Timer.h"
#include "SE_MemLeakDetector.h"
SE_Timer::SE_Timer(SE_TimeMS frameRate)
{
    mStartTime = 0;
    mLastTime = 0;
    mFrameRate = frameRate;
}
void SE_Timer::start()
{
    mStartTime = SE_Time::getCurrentTimeMS();
    mLastTime = mStartTime;
}
SE_Timer::TimePair SE_Timer::step()
{
    SE_TimeMS currTime = SE_Time::getCurrentTimeMS();
    TimePair t;
    t.realTimeDelta = currTime - mLastTime;
    t.simulateTimeDelta = mFrameRate;
    mLastTime = currTime;
    return t;
}
