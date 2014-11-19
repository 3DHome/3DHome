#ifndef SE_LOG_H
#define SE_LOG_H
#include <stdarg.h>
#ifdef __cplusplus
extern "C" {
#endif
#ifdef ANDROID
#ifdef NDK
#include <android/log.h>
#define  LOG_TAG    "SE_HOME"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#include <cutils/log.h>
#endif
#else
extern void LOGE(const char* fmt, ...);
extern void LOGI(const char* fmt, ...);
extern void LOGVA(const char* fmt, va_list ap);
#endif
#ifdef __cplusplus
}
#endif
#endif
