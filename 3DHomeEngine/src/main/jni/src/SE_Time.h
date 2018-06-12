#ifndef SE_TIME_H
#define SE_TIME_H
#if defined(WIN32)

typedef unsigned long SE_TimeMS;
typedef unsigned long SE_TimeUS;
#include "SE_DynamicLibType.h"
class SE_ENTRY SE_Time
{
public:
    static SE_TimeMS getCurrentTimeMS();
    static SE_TimeUS getCurrentTimeUS();
    static float div(SE_TimeMS a, SE_TimeMS b);
};
#else
typedef unsigned int SE_TimeMS;
typedef unsigned long SE_TimeUS;
#include "SE_DynamicLibType.h"
class SE_ENTRY SE_Time
{
public:
    static SE_TimeMS getCurrentTimeMS();
    static SE_TimeUS getCurrentTimeUS();
    static float div(SE_TimeMS a, SE_TimeMS b);
};
#endif
#endif
