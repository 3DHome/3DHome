#include "SE_DynamicLibType.h"
#include "SE_RenderTarget.h"
#include "SE_Camera.h"
#include "SE_ImageData.h"
#include "SE_Common.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include "SE_RenderSystemCapabilities.h"
#include "SE_Application.h"
#include "SE_ImageCodec.h"
#include "SE_Log.h"
#ifdef GLES_20
    #include <GLES2/gl2.h>
#include<GLES2/gl2ext.h>
#else
    #include <GLES/gl.h>
#endif
#include "SE_MemLeakDetector.h"
#include "SE_SceneManager.h"
SE_RenderTarget::SE_RenderTarget()
{
    mWidth = 10;
    mHeight = 10;
    //mCamera = NULL;
    //mRenderableType = RENDERABLE;
    mRenderTargetType = FRAMEBUFFER;
    mIsClearTarget = false; //Opaque scene will set true
    mIsInit = false;
    mClearColor.set(1.0,1.0,1.0,1.0);
}
SE_RenderTarget::~SE_RenderTarget()
{
    /*
    if(mCamera)
        delete mCamera;
        */
}

void SE_RenderTarget::saveToFile(const char* filename)
{
    unsigned char* buff = new unsigned char[mWidth * mHeight * 4];//rgba
    glReadPixels(0,0,mWidth,mHeight,GL_RGBA,GL_UNSIGNED_BYTE,buff);
    SE_ImageCodec::save(mWidth,mHeight,0,4,filename,buff);
    delete buff;
}
SE_FrameBufferTarget::SE_FrameBufferTarget()
{}
SE_FrameBufferTarget::~SE_FrameBufferTarget()
{}
void SE_FrameBufferTarget::create()
{
    if(isInit())
    {
        return;
    }
    setIsInit(true);
}
bool SE_FrameBufferTarget::prepare()
{
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0, 0,getWidth(),getHeight());
    //save current bound target
    SE_Application::getInstance()->getRenderTargetManager()->setCurrentBoundTarget(this);
    return true;
}
void SE_FrameBufferTarget::release()
{
    setIsInit(false);
}
//////////////

/*
void SE_TextureTarget::readPixels()
{
    GLint texWidth = mImageData->getWidth();
    GLint texHeight = mImageData->getHeight();
	glReadPixels(0,0,texWidth,texHeight,GL_RGBA,GL_UNSIGNED_BYTE,mImageData->getData());
}
*/

///////////////
struct SE_TextureTarget::Impl
{
    GLuint frameBuffer;
    GLuint depthRenderBuffer;
    GLuint stencilRenderBuffer;
    GLuint texture;
    GLuint depthTexture;
    GLint maxRenderBufferSize;
    Impl()
    {
        frameBuffer = 0;
        depthRenderBuffer = 0;
        stencilRenderBuffer = 0;
        texture = 0;
        maxRenderBufferSize = 0;
        depthTexture = 0;
    }
};

SE_TextureTarget::SE_TextureTarget(int objectwidth,int objectheight)
{
    mImageData = new SE_ImageData();
    mImageData->setWidth(objectwidth);
    mImageData->setHeight(objectheight);
    setWidth(objectwidth);
    setHeight(objectheight);

    mSampleMin = LINEAR;
    mSampleMag = LINEAR;
    mWrapS = CLAMP;
    mWrapT = CLAMP;

    mImpl = new SE_TextureTarget::Impl;
    mRenderTargetType = TEXTURE;
    mAttribute = RT_DEFAULT_ATTR;
    mImageData->setImageIsFBO(true);
}
SE_TextureTarget::~SE_TextureTarget()
{
    release();
    if(mImpl)
    {
        delete mImpl;
    }
    if(mImageData)
    {
        delete mImageData;
    }
}

void SE_TextureTarget::create()
{    
    if(isInit())
    {
        return;
    }
    int vendor = SE_Application::getInstance()->getRenderSystemCapabilities()->getVendor();
if(vendor == 11)
{
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLELIGHT,false);
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLEMIRROR,false);
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLEGROUNDSHADOW,false);
LOGI("vender is 11\n");
return;
}
    GLint texWidth = getWidth();
    GLint texHeight = getHeight();

    glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, &mImpl->maxRenderBufferSize);
    if(mImpl->maxRenderBufferSize <= mImageData->getWidth() ||
        mImpl->maxRenderBufferSize <= mImageData->getHeight())
    {
        LOGI("RenderTarget create texture size[w=%d,h=%d] larger than system require[max= %d]!!\n",texWidth,texHeight,mImpl->maxRenderBufferSize);
        return;
    }

            
    
    glGenFramebuffers(1, &mImpl->frameBuffer);  
    if(hasRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE))
    {
    glGenTextures(1, &mImpl->texture);
    glBindTexture(GL_TEXTURE_2D, mImpl->texture);
        //gen color texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    GLint wraps , wrapt;
    switch(mWrapS)
    {
    case REPEAT:
        wraps = GL_REPEAT;
        break;
    case CLAMP:
        wraps = GL_CLAMP_TO_EDGE;
        break;
    default:
        wraps = GL_REPEAT;
    }
    switch(mWrapT)
        {
    case REPEAT:
        wrapt = GL_REPEAT;
        break;
    case CLAMP:
        wrapt = GL_CLAMP_TO_EDGE;
        break;
    default:
        wrapt = GL_REPEAT;
        break;
        }
    GLint sampleMin, sampleMag;
    switch(mSampleMin)
    {
    case NEAREST:
        sampleMin = GL_NEAREST;
        break;
    case LINEAR:
        sampleMin = GL_LINEAR;
        break;
    case MIPMAP:        
        sampleMin = GL_LINEAR_MIPMAP_LINEAR;
        break;
    default:
        sampleMin = GL_LINEAR;
    }
    switch(mSampleMag)
    {
    case NEAREST:
        sampleMag = GL_NEAREST;
        break;
    case LINEAR:
        sampleMag = GL_LINEAR;
        break;
    default:
        sampleMag = GL_LINEAR;
    }    

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMag);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMin);
            

        glBindFramebuffer(GL_FRAMEBUFFER, mImpl->frameBuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mImpl->texture, 0);   
        
        //generate depth buffer if needed

    if(hasRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER) && vendor != 2)
    {
            glGenTextures(1, &mImpl->depthTexture);
            glBindTexture(GL_TEXTURE_2D, mImpl->depthTexture);
    
    GLint wraps , wrapt;
    switch(mWrapS)
    {
    case REPEAT:
        wraps = GL_REPEAT;
        break;
    case CLAMP:
        wraps = GL_CLAMP_TO_EDGE;
        break;
    default:
        wraps = GL_REPEAT;
    }
    switch(mWrapT)
    {
    case REPEAT:
        wrapt = GL_REPEAT;
        break;
    case CLAMP:
        wrapt = GL_CLAMP_TO_EDGE;
        break;
    default:
        wrapt = GL_REPEAT;
        break;
    }
    GLint sampleMin, sampleMag;
    switch(mSampleMin)
    {
    case NEAREST:
        sampleMin = GL_NEAREST;
        break;
    case LINEAR:
        sampleMin = GL_LINEAR;
        break;
    case MIPMAP:
            sampleMin = GL_LINEAR_MIPMAP_LINEAR;            
        break;
    default:
        sampleMin = GL_LINEAR;
    }
    switch(mSampleMag)
    {
    case NEAREST:
        sampleMag = GL_NEAREST;
        break;
    case LINEAR:
        sampleMag = GL_LINEAR;
        break;
    default:
        sampleMag = GL_LINEAR;
    }    

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMag);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMin);
    

            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, texWidth, texHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, NULL);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, mImpl->depthTexture, 0);

        }
    }
    else if(hasRenderTargetAttribute(SE_RenderTarget::RT_DEPTH_TEXTURE))
    {
        glGenTextures(1, &mImpl->texture);
        glBindTexture(GL_TEXTURE_2D, mImpl->texture);   
        //gen depth texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, texWidth, texHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, NULL);
        GLint wraps , wrapt;
        switch(mWrapS)
        {
        case REPEAT:
            wraps = GL_REPEAT;
            break;
        case CLAMP:
            wraps = GL_CLAMP_TO_EDGE;
            break;
        default:
            wraps = GL_REPEAT;
        }
        switch(mWrapT)
            {
        case REPEAT:
            wrapt = GL_REPEAT;
            break;
        case CLAMP:
            wrapt = GL_CLAMP_TO_EDGE;
            break;
        default:
            wrapt = GL_REPEAT;
            break;
            }
        GLint sampleMin, sampleMag;
        switch(mSampleMin)
        {
        case NEAREST:
            sampleMin = GL_NEAREST;
            break;
        case LINEAR:
            sampleMin = GL_LINEAR;
            break;
        case MIPMAP:        
            sampleMin = GL_LINEAR_MIPMAP_LINEAR;
            break;
        default:
            sampleMin = GL_LINEAR;
        }
        switch(mSampleMag)
        {
        case NEAREST:
            sampleMag = GL_NEAREST;
            break;
        case LINEAR:
            sampleMag = GL_LINEAR;
            break;
        default:
            sampleMag = GL_LINEAR;
        }    

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMag);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMin);        
        
        glBindFramebuffer(GL_FRAMEBUFFER, mImpl->frameBuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, mImpl->texture, 0);

            
    }
    else
    {
        
    }    

    
    GLint status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status == GL_FRAMEBUFFER_COMPLETE)
    {
        setIsInit(true);
        glClearColor(mClearColor.x,mClearColor.y,mClearColor.z,mClearColor.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    else
    {
        setIsInit(false);
        release();
        LOGI("FBO check error!!!\n");
    }   
    
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    mImageData->setTexID(mImpl->texture);
    mImageData->setWidth(texWidth);
    mImageData->setHeight(texHeight);
    
}
bool SE_TextureTarget::prepare()
{
    glBindFramebuffer(GL_FRAMEBUFFER, mImpl->frameBuffer);
    glClearColor(mClearColor.x,mClearColor.y,mClearColor.z,mClearColor.w);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    GLint status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status == GL_FRAMEBUFFER_COMPLETE)
    {
        //save current bound target
        SE_Application::getInstance()->getRenderTargetManager()->setCurrentBoundTarget(this);

            glViewport(1, 1,getWidth()-2,getHeight()-2);

        return true;
    }
    else
    {
        return false;
    }
}
void SE_TextureTarget::unbind()
{
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void SE_TextureTarget::release()
{
    GLuint result = mImpl->frameBuffer;
    if(result)
    {
    glDeleteFramebuffers(1, &mImpl->frameBuffer);
    }

    result = mImpl->texture;
    if(result)
    {
    glDeleteTextures(1, &mImpl->texture);
    }

    result = mImpl->depthTexture;
    if(result)
    {
    glDeleteTextures(1, &mImpl->depthTexture);
    }

    result = mImpl->depthRenderBuffer;
    if(result)
    {
    glDeleteRenderbuffers(1,&mImpl->depthRenderBuffer);
    }
    setIsInit(false);
}

void SE_TextureTarget::saveToFile(const char* filename)
{
    bind_4debug();
    unsigned char* buff = new unsigned char[getWidth() * getHeight() * 4];//rgba
    glReadPixels(0,0,getWidth(),getHeight(),GL_RGBA,GL_UNSIGNED_BYTE,buff);
    unbind();
    SE_ImageCodec::save(getWidth(),getHeight(),0,4,filename,buff);
    delete buff;
}
bool SE_TextureTarget::bind_4debug()
{
    glBindFramebuffer(GL_FRAMEBUFFER, mImpl->frameBuffer);   
    GLint status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status == GL_FRAMEBUFFER_COMPLETE)
    {
        //save current bound target
        SE_Application::getInstance()->getRenderTargetManager()->setCurrentBoundTarget(this);
        glViewport(0, 0,getWidth(),getHeight());
        return true;
    }
    else
    {
        return false;
    }
}

///////////////
SE_RenderBufferTarget::SE_RenderBufferTarget()
{}
SE_RenderBufferTarget::~SE_RenderBufferTarget()
{}
void SE_RenderBufferTarget::create()
{}
bool SE_RenderBufferTarget::prepare()
{
    return true;
}

