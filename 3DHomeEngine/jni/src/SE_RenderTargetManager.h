#ifndef SE_RENDERTARGETMANAGER_H
#define SE_RENDERTARGETMANAGER_H
#include "SE_ID.h"
#include <vector>
#include "SE_ObjectManager.h"
class SE_RenderTarget;
class SE_ENTRY SE_RenderTargetManager
{
public:
    /*
    static SE_RenderTargetID SE_FRAMEBUFFER_TARGET;
    static SE_RenderTargetID SE_INVALID_RENDERTARGET;
    enum {RENDERTARGET_SIZE = 512};
    SE_RenderTargetManager();
    ~SE_RenderTargetManager();
    SE_RenderTargetID addRenderTarget(SE_RenderTarget* renderTarget);
    SE_RenderTarget* getRenderTarget(const SE_RenderTargetID& id);
    void removeRenderTarget(const SE_RenderTargetID& id);
    */

    SE_RenderTargetManager();
    ~SE_RenderTargetManager();
    bool set(const char* rtName, SE_RenderTarget* rt);
    SE_RenderTarget* get(const char* rtName);
    void remove(const char* rtName);
    SE_RenderTarget* getDefaultRenderTarget()
    {
        return mDefaultRenderTarget;
    }

    void setCurrentBoundTarget(SE_RenderTarget* rt)
    {
        mCurrentRenderTarget = rt;
    }

    SE_RenderTarget* getCurrentBoundTarget()
    {
        return mCurrentRenderTarget;
    }


    SE_RenderTarget* getShadowTarget()
    {
        return mShadowRenderTarget;
    }

    SE_RenderTarget* getWallShadowTarget()
    {
        return mWallShadowRenderTarget;
    }

    SE_RenderTarget* getGroundShadowTarget()
    {
        return mGroundShadowRenderTarget;
    }


    SE_RenderTarget* getBlurHTarget()
    {
        return mBlurHRenderTarget;
    }

    SE_RenderTarget* getBlurVTarget()
    {
        return mBlurVRenderTarget;
    }

    SE_RenderTarget* getDownSampleTarget()
    {
        return mDownSampleRenderTarget;
    }

    SE_RenderTarget* getDofGenTarget()
    {
        return mDofGenRenderTarget;
    }

    SE_RenderTarget* getDrawScreenToTarget()
    {
        return mDrawScreenToRenderTarget;
    }

    SE_RenderTarget* getColorEffectTarget()
    {
        return mColorEffectRenderTarget;
    }

    void releaseHardware();

    void saveTargetToFile(const char* fileName,const char* targetName = NULL);

private:
    /*
    typedef std::vector<SE_RenderTarget*> _RenderTargetSet;
    _RenderTargetSet mRenderTargets;
    int mCurrentIndex;
    */

    SE_ObjectManager<std::string, SE_RenderTarget> mRenderTargetManager;
    SE_RenderTarget* mCurrentRenderTarget;
    SE_RenderTarget* mDefaultRenderTarget;
    SE_RenderTarget* mShadowRenderTarget;
    
    SE_RenderTarget* mBlurHRenderTarget;
    SE_RenderTarget* mBlurVRenderTarget;
    
    SE_RenderTarget* mDownSampleRenderTarget;

    SE_RenderTarget* mDofGenRenderTarget;

    SE_RenderTarget* mDrawScreenToRenderTarget;

    SE_RenderTarget* mColorEffectRenderTarget;

    SE_RenderTarget* mWallShadowRenderTarget;
    SE_RenderTarget* mGroundShadowRenderTarget;
};
#endif
