#include "SE_Log.h"
#include "SE_Common.h"
#include <stdarg.h>
#include <wchar.h>
#include <string.h>
#if defined(WIN32)
#include <windows.h>
#endif
#ifndef ANDROID
static void outputString(const char* buf, int size)
{
#if defined(WIN32)
    TCHAR wbuf[512];
#ifdef _UNICODE
    memset(wbuf, 0, 512 * sizeof(wchar_t));
    _snwprintf(wbuf, 512, L"%S", buf);
    OutputDebugString(wbuf);
#else
    OutputDebugString(buf);
#endif
#else
    fprintf(stderr, "%s", buf);
#endif
}
#include "SE_MemLeakDetector.h"
void LOGE(const char* fmt, ...)
{
    va_list ap;
    char buf[4096];
    va_start(ap, fmt);
    memset(buf, 0, 4096);
    vsnprintf(buf, 4096, fmt, ap);
    buf[4096 - 1] = '\0';
    va_end(ap);
    outputString(buf, strlen(buf));
    exit(-1);
}
void LOGI(const char* fmt, ...)
{
    va_list ap;
    char buf[4096];
    va_start(ap, fmt);
    memset(buf, 0, 4096);
    vsnprintf(buf, 4096, fmt, ap);
    buf[4096 - 1] = '\0';
    va_end(ap);
    outputString(buf, strlen(buf));
}
void LOGVA(const char* fmt, va_list ap)
{
    char buf[4096];
    memset(buf, 0, 4096);
    vsnprintf(buf, 4096, fmt, ap);
    buf[4096 - 1] = '\0';
    outputString(buf, strlen(buf));
}
#endif
