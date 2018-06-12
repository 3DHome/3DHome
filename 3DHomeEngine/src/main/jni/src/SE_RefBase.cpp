#include "SE_DynamicLibType.h"
#include "SE_RefBase.h"
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_DelayDestroy.h"
#ifdef ANDROID
#include <sys/atomics.h>
#endif
SE_RefBase::SE_RefBase(bool threadSafe) : mRefCount(0) , mMutex(NULL)
	{
		if(threadSafe)
		{
			mMutex = new SE_Mutex;
		}
	}
	void SE_RefBase::incStrong(void* id)
	{
#ifdef ANDROID
        __atomic_inc(&mRefCount);
#else
		if(mMutex)
			mMutex->lock();
		++mRefCount;
		if(mMutex)
			mMutex->unlock();
#endif
	}
	void SE_RefBase::decStrong(void* id)
	{
#ifdef ANDROID
        __atomic_dec(&mRefCount);
#else
		if(mMutex)
			mMutex->lock();
		--mRefCount;
		if(mMutex)
			mMutex->unlock();
#endif
		if(mRefCount <= 0)
        {
            //delete this;
            SE_DelayDestroy* dd = new SE_DelayDestroyPointer<SE_RefBase>(this);
            bool ret = SE_Application::getInstance()->addDelayDestroy(dd);
            if(!ret)
                delete dd;
        }
	}
    SE_RefBase::~SE_RefBase()
    {
		if(mMutex)
			delete mMutex;
		if(mRefCount > 0)
		{
            if(SE_Application::getInstance()->SEHomeDebug)
			LOGI("## error: delete object which ref count > 0 ##\n");
		}
	}
