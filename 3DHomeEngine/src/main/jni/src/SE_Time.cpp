#include "SE_DynamicLibType.h"
#include "SE_Time.h"
#if defined(WIN32)
#include <time.h>
#include <windows.h>
#else
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#endif
#include "SE_MemLeakDetector.h"
SE_TimeMS SE_Time::getCurrentTimeMS()
{
#if defined(WIN32)
    //return GetTickCount();
    time_t clock;
    struct tm tm;
    SYSTEMTIME wtm;
    GetLocalTime(&wtm);
    tm.tm_year = wtm.wYear - 1900;
    tm.tm_mon = wtm.wMonth - 1;
    tm.tm_mday = wtm.wDay;
    tm.tm_hour = wtm.wHour;
    tm.tm_min  = wtm.wMinute;
    tm.tm_sec = wtm.wSecond;
    tm.tm_isdst = -1;
    clock = mktime(&tm);
    return clock * 1000 + wtm.wMilliseconds;
#else
    struct timespec tp;
    clock_gettime(CLOCK_MONOTONIC,&tp);

    unsigned int timeMS = tp.tv_sec * 1000 + tp.tv_nsec / (1000 * 1000);   
    return timeMS; 
#endif    
}
SE_TimeUS SE_Time::getCurrentTimeUS()
{
#if defined(WIN32)
    //return GetTickCount();
    time_t clock;
    struct tm tm;
    SYSTEMTIME wtm;
    GetLocalTime(&wtm);
    tm.tm_year = wtm.wYear - 1900;
    tm.tm_mon = wtm.wMonth - 1;
    tm.tm_mday = wtm.wDay;
    tm.tm_hour = wtm.wHour;
    tm.tm_min  = wtm.wMinute;
    tm.tm_sec = wtm.wSecond;
    tm.tm_isdst = -1;
    clock = mktime(&tm);
    return clock * 1000 + wtm.wMilliseconds;
#else
struct timespec tp;
    clock_gettime(CLOCK_MONOTONIC,&tp);

    unsigned int timeMS = tp.tv_sec * 1000 + tp.tv_nsec / (1000 * 1000);   
    return timeMS; 
#endif    
}
float SE_Time::div(SE_TimeMS a, SE_TimeMS b)
{
    return a / (float)b;
}
/*
SE_Timer::SE_Timer(int frameRate)
{
    mFrameRate = frameRate;
    mStartTime = 0;
    mSimulateStartTime = 0;
}
void SE_Timer::start()
{
    mStartTime = getCurrentTimeMS();
    mSimulateStartTime = 0;
}
SE_TimeMS SE_Timer::getSpanMS()
{
    if(mStartTime == 0)
        return 0;
    SE_TimeMS currTime = getCurrentTimeMS();
    return (currTime - mStartTime);
}
SE_Timer::update()
{
    mSimulateStartTime += mFrameRate * 
}
*/
