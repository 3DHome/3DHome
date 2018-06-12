#include "SE_DynamicLibType.h"
#include "SE_Mutex.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
/////////////////////////////////

SE_Mutex::SE_Mutex()
{
#if defined(SE_HAS_MUTEX)
    pthread_mutex_init(&mMutex, NULL);
#endif
}
SE_Mutex::~SE_Mutex()
{
    
}
bool SE_Mutex::lock()
{
#if defined(SE_HAS_MUTEX)
     pthread_mutex_lock(&mMutex);
#endif
     return true;
}
bool SE_Mutex::unlock()
{
#if defined(SE_HAS_MUTEX)
    pthread_mutex_unlock(&mMutex);
#endif
    return true;
}
SE_MutexCondition::SE_MutexCondition() : mMutex(NULL)
{
#if defined(SE_HAS_MUTEX)
    pthread_cond_init(&mCond, NULL);
#endif
}
void SE_MutexCondition::setMutex(SE_Mutex* m)
{
    mMutex = m;
}
void SE_MutexCondition::signal()
{
    if(!mMutex)
    {
        LOGE("#### condition mutex is NULL ####\n");
        return;
    }
#if defined(SE_HAS_MUTEX)
    pthread_cond_signal(&mCond);
#endif
}
void SE_MutexCondition::wait()
{
    if(!mMutex)
    {
        LOGE("### condition mutex is NULL ####\n");
        return;
    }
#if defined(SE_HAS_MUTEX)
    pthread_cond_wait(&mCond, mMutex->getMutex());
#endif
}
