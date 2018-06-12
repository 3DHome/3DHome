#ifndef SE_RENDERTARGET_H
#define SE_RENDERTARGET_H
#include "SE_ID.h"
#include "SE_Vector.h"
class SE_Camera;
class SE_ImageData;
class SE_ENTRY SE_RenderTarget
{
public:
    
    enum RENDER_TARGET_TYPE {TEXTURE, RENDERBUFFER, FRAMEBUFFER};
    //enum RENDERABLE_TYPE {RENDERABLE, NO_RENDERABLE};
    
    enum RenderTargetAttr
    {
        RT_DEFAULT_ATTR = 0x0,
        RT_MIPMAP_ATTR = 0x01,
        RT_MULTISAMPLE_ATTR = 0x02,
        RT_DEPTHBUFFER = 0x04,
        RT_STENCILEBUFFER = 0x08,
        RT_COLOR_TEXTURE = 0x10,
        RT_DEPTH_TEXTURE = 0x20
        
    };
    SE_RenderTarget();
    virtual ~SE_RenderTarget();
/*
    SE_RenderTargetID getRenderTarget()
    {
        return mID;
    }
    void setRenderTarget(const SE_RenderTargetID& id)
    {
        mID = id;
    }
    */
    void setWidth(int w)
    {
        mWidth = w;
    }
    int getWidth()
    {
        return mWidth;
    }
    void setHeight(int h)
    {
        mHeight = h;
    }
    int getHeight()
    {
        return mHeight;
    }
    RENDER_TARGET_TYPE getRenderTargetType()
    {
        return mRenderTargetType;
    }
    /*
    SE_Camera* getCamera()
    {
        return mCamera;
    }
    void setCamera(SE_Camera* camera)
    {
        mCamera = camera;
    }
    */
    /*
    void setRenderableType(RENDERABLE_TYPE t)
    {
        mRenderableType = t;
    }
    RENDERABLE_TYPE getRenderableType()
    {
        return mRenderableType;
    }
    */
    void setIsClearTargetColor(bool clearTarget)
    {
        mIsClearTarget = clearTarget;
    }
    bool isClearTargetColor()
    {
        return mIsClearTarget;
    }
    virtual void create() = 0;
    virtual bool prepare() = 0;
    virtual void release() = 0;
    virtual void unbind(){}
    virtual void* getData()
    {
        return NULL;
    }

    void setRenderTargetAttribute(RenderTargetAttr rta)
    {
          mAttribute |= rta;
    }

    void removeRenderTargetAttribute(RenderTargetAttr rta)
    {
        mAttribute &= (~rta);
    }
    bool hasRenderTargetAttribute(RenderTargetAttr rta)
    {
        return (mAttribute & rta) == rta;
    }


    void setRTName(const char* rtname)
    {
        mRenderTargetName = rtname;
    }

    const char* getRTName()
    {
        return mRenderTargetName.c_str();
    }

    bool isInit()
    {
        return mIsInit;
    }

    void setIsInit(bool init)
    {
        mIsInit = init;
    }
    virtual void saveToFile(const char* filename);

    virtual void setClearColor(float r,float g,float b,float a)
    {
        mClearColor.set(r,g,b,a);
    }

protected:
    RENDER_TARGET_TYPE mRenderTargetType;
    unsigned int mAttribute;
    SE_Vector4f mClearColor;
private:
    //SE_RenderTargetID mID;
    //RENDERABLE_TYPE mRenderableType;
    int mWidth;
    int mHeight;
    bool mIsClearTarget;
    //SE_Camera* mCamera;
    std::string mRenderTargetName;
    bool mIsInit;
};
class SE_ENTRY SE_FrameBufferTarget : public SE_RenderTarget
{
public:
    SE_FrameBufferTarget();
    ~SE_FrameBufferTarget();
    void create();
    bool prepare();    
    void release();
private:
    
};

class SE_ENTRY SE_TextureTarget : public SE_RenderTarget
{
public:
    SE_TextureTarget(int objectwidth = 512,int objectheight = 512);
    virtual ~SE_TextureTarget();
    virtual void create();
    virtual bool prepare();
    virtual void unbind();
    virtual void release();
    virtual void* getData()
    {
        return mImageData;
    }
    int getSampleMin()
    {
        return mSampleMin;
    }
    int getSampleMag()
    {
        return mSampleMag;
    }
    int getWrapS()
    {
        return mWrapS;
    }
    int getWrapT()
    {
        return mWrapT;
    }
    
    void setSampleMin(int min)
    {
        mSampleMin = min;
    }
    void setSampleMag(int mag)
    {
        mSampleMag = mag;
    }
    void setWrapS(int wraps)
    {
        mWrapS = wraps;
    }
    void setWrapT(int wrapt)
    {
        mWrapT = wrapt;
    }
    virtual void saveToFile(const char* filename);
protected:
    struct Impl;
    Impl* mImpl;

private:
    bool bind_4debug();
private:
    SE_ImageData* mImageData;
    int mSampleMin;
    int mSampleMag;
    int mWrapS;
    int mWrapT;

};
class SE_ENTRY SE_RenderBufferTarget : public SE_RenderTarget
{
public:
    SE_RenderBufferTarget();
    ~SE_RenderBufferTarget();
    void create();
    bool prepare();
};
#endif
