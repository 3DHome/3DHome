#include "SE_DynamicLibType.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include "SE_RenderTarget.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
#include "SE_ImageData.h"
#include "SE_Application.h"

//SE_RenderTargetID SE_RenderTargetManager::SE_FRAMEBUFFER_TARGET = 0;
//SE_RenderTargetID SE_RenderTargetManager::SE_INVALID_RENDERTARGET = -1;
SE_RenderTargetManager::SE_RenderTargetManager()
{/*
    mRenderTargets.resize(RENDERTARGET_SIZE, NULL);
    mCurrentIndex = 0;
    //SE_FrameBufferTarget* r = new SE_FrameBufferTarget;
    //mRenderTargets[mCurrentIndex] = r;
    mCurrentIndex++;
*/
    mCurrentRenderTarget = NULL;
    mDefaultRenderTarget = new SE_FrameBufferTarget();
    mShadowRenderTarget = new SE_TextureTarget();

    mBlurHRenderTarget = new SE_TextureTarget();
    mBlurVRenderTarget = new SE_TextureTarget();
   
    mDownSampleRenderTarget = new SE_TextureTarget();

    mDofGenRenderTarget = new SE_TextureTarget();
   
    mDrawScreenToRenderTarget = new SE_TextureTarget();
   
    mColorEffectRenderTarget = new SE_TextureTarget();
   
    mWallShadowRenderTarget = new SE_TextureTarget();;
    mGroundShadowRenderTarget = new SE_TextureTarget();
   
}
SE_RenderTargetManager::~SE_RenderTargetManager()
{
    /*
    _RenderTargetSet::iterator it;
    for(it = mRenderTargets.begin() ; it != mRenderTargets.end() ; it++)
    {
        SE_RenderTarget* r = *it;
        if(r)
            delete r;
    }
    */
    if(mDefaultRenderTarget)
    {
        delete mDefaultRenderTarget;
    }

    if(mShadowRenderTarget)
    {
        delete mShadowRenderTarget;
    }

    if(mBlurHRenderTarget)
    {
        delete mBlurHRenderTarget;
    }

    if(mBlurVRenderTarget)
    {
        delete mBlurVRenderTarget;
    }

    if(mDownSampleRenderTarget)
    {
        delete mDownSampleRenderTarget;
    }

    if(mDofGenRenderTarget)
    {
        delete mDofGenRenderTarget;
    }

    if(mDrawScreenToRenderTarget)
    {
        delete mDrawScreenToRenderTarget;
    }

    if(mColorEffectRenderTarget)
    {
        delete mColorEffectRenderTarget;
    }

    if(mWallShadowRenderTarget)
    {
        delete mWallShadowRenderTarget;
    }

    if(mGroundShadowRenderTarget)
    {
        delete mGroundShadowRenderTarget;
    }



    SE_ObjectManager<std::string, SE_RenderTarget>::RMap::iterator _it;
    for(_it = mRenderTargetManager.getMap()->begin() ; _it != mRenderTargetManager.getMap()->end() ;)
    {
        std::string name = _it->first;
        mRenderTargetManager.remove(name);
        _it = mRenderTargetManager.getMap()->begin();
    }
    mRenderTargetManager.getMap()->clear();
}

bool SE_RenderTargetManager::set(const char *rtName, SE_RenderTarget *rt)
{
    std::string name(rtName);
    return mRenderTargetManager.set(name,rt);
}

SE_RenderTarget* SE_RenderTargetManager::get(const char *rtName)
{
    std::string name(rtName);
    return mRenderTargetManager.get(name);
}

void SE_RenderTargetManager::remove(const char *rtName)
{
    std::string name(rtName);
    mRenderTargetManager.remove(name);
}
void SE_RenderTargetManager::releaseHardware()
{
    mShadowRenderTarget->release();
    mBlurHRenderTarget->release();
    mBlurVRenderTarget->release();
    mDownSampleRenderTarget->release();
    mDofGenRenderTarget->release();
    mDrawScreenToRenderTarget->release();
    mColorEffectRenderTarget->release();
    mWallShadowRenderTarget->release();
    mGroundShadowRenderTarget->release();


    SE_ObjectManager<std::string, SE_RenderTarget>::RMap::iterator _it;
    for(_it = mRenderTargetManager.getMap()->begin() ; _it != mRenderTargetManager.getMap()->end() ; _it++)
    {
        _it->second->release();
    }
}

/*
SE_RenderTargetID SE_RenderTargetManager::addRenderTarget(SE_RenderTarget* renderTarget)
{
    if(mCurrentIndex == RENDERTARGET_SIZE)
    {
        for(int i = 0 ; i < mRenderTargets.size() ; i++)
        {
            if(mRenderTargets[i] == NULL)
            {
                mRenderTargets[i] = renderTarget;
                return i;
            }
        }
        LOGE("... error exceed the max render target is %d\n", RENDERTARGET_SIZE);
        return SE_INVALID_RENDERTARGET;
    }
    else
    {
        mRenderTargets[mCurrentIndex] = renderTarget;
        int ret = mCurrentIndex;
        mCurrentIndex++;
        return ret;
    }
}
SE_RenderTarget* SE_RenderTargetManager::getRenderTarget(const SE_RenderTargetID& id)
{
    if(id < 0 || id >= RENDERTARGET_SIZE)
        return NULL;
    return mRenderTargets[id];
}
void SE_RenderTargetManager::removeRenderTarget(const SE_RenderTargetID& id)
{
    if(id <= 0 || id >= RENDERTARGET_SIZE)
        return;
    SE_RenderTarget* r = mRenderTargets[id];
    mRenderTargets[id] = NULL;
    delete r;
}
*/
void SE_RenderTargetManager::saveTargetToFile(const char* fileName,const char* targetName)
{
    if(targetName)
    {
        SE_RenderTarget* rt = get(targetName);
        if(rt)
        {
            rt->saveToFile(fileName);
        }
        else
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("render target not found,save fail!!!\n");
            }
        }
    }
    else
    {
        SE_ObjectManager<std::string, SE_RenderTarget>::RMap::iterator _it;
        for(_it = mRenderTargetManager.getMap()->begin() ; _it != mRenderTargetManager.getMap()->end() ; _it++)
        {
            _it->second->saveToFile(fileName);
        }  
    }
}
