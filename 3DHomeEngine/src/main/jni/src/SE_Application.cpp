
#include "SE_DynamicLibType.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_RenderManager.h"
#include "SE_Camera.h"
#include "SE_Command.h"
#include "SE_CommandFactory.h"
#include "SE_RenderManager.h"
#include "SE_InputManager.h"
#include "SE_AnimationManager.h"
//#include "SE_ElementManager.h"

#include "SE_RenderTargetManager.h"
#include "SE_ThreadManager.h"
#include "SE_Log.h"
#include <string.h>
#include <algorithm>
#include "SE_Mutex.h"
#include "SE_Config.h"
#include "SE_RenderSystemCapabilities.h"
//#include "SE_UIManager.h"
#include "SE_PluginsManager.h"
#include "SE_DynamicLibManager.h"
#include "SE_MemLeakDetector.h"
#include "SE_RenderTarget.h"

#include "SE_RenderUnitManager.h"
#include "ParticleSystem/SE_ParticleSystemManager.h"
#ifdef EDITOR
#include "SE_EditorManager.h"
#include "MyShellOS.h"
#endif
SE_Application* SE_Application::mInstance = NULL;
SE_Application* SE_Application::getInstance()
{
    if(mInstance == NULL)
    {
        mInstance = new SE_Application;
    }
    return mInstance;
}
SE_Application::SE_Application()
{
	SEHomeDebug = false;
    memset(mCameraArray, 0, sizeof(SE_Camera*) * MAX_CAMERA_NUM);
    for(int i = 0; i < (int)MAX_CAMERA_NUM;++i)
    {
        mCameraArray[i] = new SE_Camera();
    }
    mCurrentCamera = NULL;
#ifdef ANDROID
    mAssetManager = NULL;
#endif
    mRenderTargetManager = new SE_RenderTargetManager;
    mSceneManager = new SE_SceneManager;
    mResourceManager = new SE_ResourceManager;
    mRenderManager = new SE_RenderManager;
	mInputManager = new SE_InputManager;
	mAnimationManager = new SE_AnimationManager;
	//mElementManager = new SE_ElementManager;
    
    mThreadManager = new SE_ThreadManager;
	//particle system manager
	{
	mParticleSystemManager = new ParticleSystemManager;
    mParticleSystemManager->_initialise(); 
 
	}
    mRenderSystemCapabilities = new SE_RenderSystemCapabilities();
	//mImageMapManager = new SE_ImageMapManager;
	//mImageTableManager = new SE_ImageTableManager;

	//mUIManager = new SE_UIManager;
#ifdef EDITOR
	mEditorManager = new SE_EditorManager;
#endif
    //mSpatialManager = new SE_SpatialManager();
    mRenderUnitManager = new SE_RenderUnitManager();
    
    mFrameNum = 0;
    mStartTime = 0;
    mPrevTime = 0;
    mFrameRate = 30;
    mStarted = false;
    mFpsPrevTime = 0;
    mFpsFrameNum = 0;
	mObjectCount = 0;
#ifdef ANDROID
    mState = RUNNING;
#else
    mState = PREPARE;
#endif
	mSeqNum = 0;
    mConfig = NULL;

    mPluginsManager = new SE_PluginsManager();
    mDynamicLibManager = new SE_DynamicLibManager();
	mCommandList.clear();

    

    mCameraArray[(int)SE_WALLSHADOWCAMERA]->createOthroCamera(-800,800,-800,800,200,1500,SE_Vector3f(0,-200,500),SE_Vector3f(-0.1,-1,0.1),SE_Vector3f(0,0,1));
    mCameraArray[(int)SE_GROUNDSHADOWCAMERA]->createOthroCamera(-500,500,-500,500,1,1500,SE_Vector3f(0,0,500),SE_Vector3f(0,0,1),SE_Vector3f(0,1,0));
}
SE_Application::~SE_Application()
{
    for(int i = 0 ; i < MAX_CAMERA_NUM ; i++)
    {
        if(mCameraArray[i])
        {
            delete mCameraArray[i];
        }        
    }
    if(mParticleSystemManager)
        delete mParticleSystemManager;
    if(mSceneManager)
        delete mSceneManager;
    if(mResourceManager)
        delete mResourceManager;
	if(mInputManager)
		delete mInputManager;
	if(mAnimationManager)
		delete mAnimationManager;
    /*
	if(mElementManager)
		delete mElementManager;
        */
    
	if(mRenderTargetManager)
		delete mRenderTargetManager;
	
    SE_CommandFactoryList::iterator it;
    for(it = mCommandFactoryList.begin() ; it != mCommandFactoryList.end() ; it++)
    {
        delete it->factory;
    }
    delete mRenderManager;
#ifdef ANDROID
    if(mAssetManager)
    {
        delete mAssetManager;
    }
#endif

    if(mThreadManager)
    {
        delete mThreadManager;
    }

    if(mRenderSystemCapabilities)
    {
        delete mRenderSystemCapabilities;
    }

//    if(mUIManager)
//    {
//        delete mUIManager;
//    }
#ifdef EDITOR
	if(mEditorManager)
	{
		delete mEditorManager;
	}
#endif
    if(mPluginsManager)
    {
        delete mPluginsManager;
    }

    if(mDynamicLibManager)
    {
        delete mDynamicLibManager;
    }

    if(mRenderUnitManager)
    {
        delete mRenderUnitManager;
    }

    doDelayDestroy();

    std::list<SE_ResourceManager*>::iterator itloader = mLoaderList.begin();
    for(;itloader != mLoaderList.end();itloader++)
    {
        SE_ResourceManager* r = *itloader;
        delete r;
    }
    mLoaderList.clear();
}
void SE_Application::doDelayDestroy()
{
    mDelayDestroyListMutex.lock();
    SE_DelayDestroyList::iterator it;
    for(it = mDelayDestroyList.begin() ; it != mDelayDestroyList.end() ; it++)
    {
        SE_DelayDestroy* dd = *it;
        dd->destroy();
        delete dd;
    }
    mDelayDestroyList.clear();
    mDelayDestroyListMutex.unlock();
}
void SE_Application::update(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
	
    if(mState == SUSPEND)	
	{ 
       mState = EXIT;
       return;
	}    
    processCommand(realDelta, simulateDelta);
	
    if(mState == RUNNING)
    {
	mAnimationManager->update(realDelta, simulateDelta);
	mParticleSystemManager->update(realDelta, simulateDelta);
#ifdef EDITOR
		for(int i=0;i<4;++i)
		{
			PVRShellInitAPI& shellAPI = MyShellInit::getInstance()->mShellAPI[i];
			eglMakeCurrent(shellAPI.m_EGLDisplay, shellAPI.m_EGLWindow, shellAPI.m_EGLWindow, MyShellInit::getInstance()->mShellAPI[0].m_EGLContext);

			mRenderManager->beginDraw();
			mSceneManager->getMainScene()->setCamera((SE_Camera*)mEditorManager->getViewportCamera(i));
			mSceneManager->renderScene(*mRenderManager);
			//mRenderManager->setCurrentCamera((SE_Camera*)mEditorManager->getViewportCamera(i));
			mRenderManager->sort();
			mRenderManager->draw();
			mRenderManager->endDraw();

		}
#else
			mRenderManager->beginDraw();
			mSceneManager->renderScene(*mRenderManager);
			mRenderManager->sort();
			mRenderManager->draw();
			mRenderManager->endDraw();
#endif
    }
	if(mConfig)
	{
		int printRenderInfo = mConfig->getInt("OutputRenderInfo", 0);
		if(printRenderInfo)
		{			
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                if(SE_Application::getInstance()->SEHomeDebug)
			LOGI("### total surface num = %d, total vertex num = %d , total face num = %d ##\n", 
				SE_Application::getInstance()->getStatistics().renderSurfaceNum,
				SE_Application::getInstance()->getStatistics().renderVertexNum, 
				SE_Application::getInstance()->getStatistics().renderFaceNum
				);
            }
			std::list<SE_Application::RenderUnitStatistics*>::iterator ruStatIt;
			SE_Application::Statistics& stat = SE_Application::getInstance()->getStatistics();
			for(ruStatIt = stat.renderUnitData.begin() ; ruStatIt != stat.renderUnitData.end(); ruStatIt++)
			{
				SE_Application::RenderUnitStatistics* rus = *ruStatIt;
                if(SE_Application::getInstance()->SEHomeDebug)
				LOGI("## surface name = %s, vertex num = %d, face num = %d ##\n", rus->name.c_str(), rus->vertexNum, rus->faceNum);
			}
            if(SE_Application::getInstance()->SEHomeDebug)
			LOGI("## texture num = %d ##\n", stat.textureList.size());
			std::list<SE_Application::TexUseData>::iterator texIt;
			for(texIt = stat.textureList.begin() ; 
				texIt != stat.textureList.end() ; 
				texIt++)
			{
                if(SE_Application::getInstance()->SEHomeDebug)
				LOGI("## texture : %s , num = %d\n", texIt->name.c_str(), texIt->num);
			}
		}
	}
    doDelayDestroy();
}
void SE_Application::start()
{    
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("###########App start,prepare to load plugins #########\n");
#ifdef WIN32
	mResourceManager->loadPlugins();
#else
    //mResourceManager->loadPlugins();
#endif    
}
void SE_Application::shutdown()
{
    if(mResourceManager)
    {
        mResourceManager->releaseHardwareResource();
    }
    if(mRenderTargetManager)
    {
        mRenderTargetManager->releaseHardware();
    }

}
void SE_Application::run()
{
    if(!mStarted)
    {
        mStarted = true;
        mStartTime = SE_Time::getCurrentTimeMS();
        mPrevTime = mStartTime;
    }
    SE_TimeMS currTime = SE_Time::getCurrentTimeMS();
    SE_TimeMS delta = currTime - mPrevTime;
    mPrevTime = currTime;
    update(delta, mFrameRate);
    mFrameNum++;   
    SE_TimeMS fpsDelta = currTime - mFpsPrevTime;
    mFpsFrameNum++;
    if(fpsDelta > 1000)
    {
        float fFPS = 1000.0f * mFpsFrameNum /(float)fpsDelta;
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("FPS : %f\n", fFPS);
        mFpsFrameNum = 0;
        mFpsPrevTime = currTime;
    }
}
/*
void SE_Application::setCamera(int index, SE_Camera* camera)
{
	if(index < 0 || index >= MAX_CAMERA_NUM)
		return;
    SE_Camera* c = mCameraArray[index];
	if(c)
		delete c;
	mCameraArray[index] = camera;
}
SE_Camera* SE_Application::getCamera(int index)
{
	if(index < 0 || index >= MAX_CAMERA_NUM)
		return NULL;
	return mCameraArray[index];
}
void SE_Application::setCurrentCamera(int index)
{
	if(index < 0 || index >= MAX_CAMERA_NUM)
		return;
	mCurrentCamera = mCameraArray[index];
}
*/
SE_Camera* SE_Application::getCurrentCamera()
{
	//return mCurrentCamera;
	SE_Scene* scene = mSceneManager->getMainScene();
    if (scene)
    {
        return scene->getCamera();
    }
    return NULL;
}

SE_Camera* SE_Application::getUICamera()
{
	SE_Scene* scene = mSceneManager->getScene(SE_FRAMEBUFFER_SCENE,"GUI");
    if (scene)
    {
        return scene->getCamera();
    }
    return NULL;

}

void SE_Application::sendCommand(SE_Command* command)
{
    if(command)
    {
        command->handle(0, 0);
        delete command;
    }
}
SE_CommonID SE_Application::createCommonID()
{
    SE_TimeUS currTime = SE_Time::getCurrentTimeUS();
	return SE_CommonID(mAppID.first, mAppID.second, (unsigned int)currTime, mObjectCount++);
}
/*
class isPriorityLessThan
{
public:
    isPriorityLessThan(int priority) : mPriority(priority)
    {
    }
	bool operator()(const SE_Application::_CommandWrapper& cw)
    {
        if(cw.command->priority() < mPriority)
            return true;
        else 
            return false;
    }
private:
    int mPriority;
};
*/
void SE_Application::postCommand(SE_Command* command)
{
    SE_AutoMutex m(&mCommandListMutex);
    mCommandList.push_back(command);
}
void SE_Application::setUpEnv()
{}
bool SE_Application::isRemoved(const _CommandWrapper& c)
{
    if(c.canDelete)
        return true;
    else
        return false;
}
void SE_Application::processCommand(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_CommandList::iterator it;
	mCommandListMutex.lock();
    SE_CommandList tmpList = mCommandList;
    mCommandList.clear();
	mCommandListMutex.unlock();
    for(it = tmpList.begin(); it != tmpList.end(); )
    {
        SE_Command* c = *it;
		if(c->expire(realDelta, simulateDelta) && c->isFulfilled())
        {
            c->handle(realDelta, simulateDelta);
            delete c;
            tmpList.erase(it++);
        }
		else
		{
			it++;
		}
    }
    if(tmpList.empty())
        return;
	mCommandListMutex.lock();
    mCommandList.splice(mCommandList.begin(), tmpList, tmpList.begin(), tmpList.end()); 
	mCommandListMutex.unlock();
}
bool SE_Application::addDelayDestroy(SE_DelayDestroy* dd)
{
    _FindDelayDestroy fd;
    fd.src = dd;
    mDelayDestroyListMutex.lock();
    SE_DelayDestroyList::iterator it = find_if(mDelayDestroyList.begin(), mDelayDestroyList.end(), fd);
    if(it != mDelayDestroyList.end())
    {
        mDelayDestroyListMutex.unlock();
        return false;
    }
    mDelayDestroyList.push_back(dd);
    mDelayDestroyListMutex.unlock();
    return true;
}

bool SE_Application::registerCommandFactory(const SE_CommandFactoryID& cfID, SE_CommandFactory* commandFactory)
{
    _CommandFactoryEntry fe;
    fe.id = cfID;
    fe.factory = commandFactory;
    SE_CommandFactoryList::iterator it = find_if(mCommandFactoryList.begin(), mCommandFactoryList.end(), isCommandFactoryIDEqual(cfID));
    if(it != mCommandFactoryList.end())
        return false;
    mCommandFactoryList.push_back(fe);
    return true;
}
bool SE_Application::unreginsterCommandFactory(const SE_CommandFactoryID& cfID)
{
    remove_if(mCommandFactoryList.begin(), mCommandFactoryList.end(), isCommandFactoryIDEqual(cfID));
    return true;
}
SE_Command* SE_Application::createCommand(const SE_CommandID& commandID)
{
    SE_CommandFactoryList::iterator it;
    for(it = mCommandFactoryList.begin(); it != mCommandFactoryList.end(); it++)
    {
        _CommandFactoryEntry e = *it;
        SE_Command* command = e.factory->create(this, commandID);
        if(command)
            return command;
    }
	return NULL;
}

void SE_Application::sendMessage(SE_Message* message)
{
	mMessageList.push_back(message);
}
int SE_Application::getMessageCount()
{
	return mMessageList.size();
}
void SE_Application::releaseMessage()
{
	SE_MessageList::iterator it;
	for(it = mMessageList.begin() ; it != mMessageList.end() ; it++)
	{
		SE_Message* msg = *it;
		delete msg;
		msg = NULL;
	}
	mMessageList.clear();
}
SE_Application::_MessageVector SE_Application::getMessage()
{
    _MessageVector v;
	v.resize(mMessageList.size());
	SE_MessageList::iterator it;
	int i = 0;
	for(it = mMessageList.begin() ; it != mMessageList.end() ; it++)
	{
		SE_Message* msg = *it;
		v[i++] = msg;
	}
	return v;
}
void SE_Application::setConfig(const char* configFilePath)
{
	if(mConfig)
		delete mConfig;
	mConfig = new SE_Config(configFilePath);
}
SE_ResourceManager* SE_Application::createNewLoader()
{    
    return new SE_ResourceManager();;
}
void SE_Application::pushLoaderToList(SE_ResourceManager* m)
{
    if(m)
    {
        mLoaderList.push_back(m);
    }
}
SE_ResourceManager* SE_Application::popLoader()
{
    std::list<SE_ResourceManager*>::iterator it = mLoaderList.begin();

    if(it != mLoaderList.end())
    {
        SE_ResourceManager* m = *it;
        mLoaderList.erase(it);
        return m;
    }
    return NULL;
}
