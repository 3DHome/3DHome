#ifndef SE_APPLICATION_H
#define SE_APPLICATION_H
#include "SE_Time.h"
#include "SE_ID.h"
#include "SE_Command.h"
#include "SE_Message.h"
#include "SE_DelayDestroy.h"
#include "SE_Utils.h"
#ifdef ANDROID
#include "SE_AssetManager.h"
#endif
#include <list>
#include <string>
#include <vector>
#include <algorithm>
#include "SE_Mutex.h"
#include "ParticleSystem/SE_ParticleSystemManager.h"
class SE_CommandFactory;
class SE_Camera;
class SE_ResourceManager;
class SE_SceneManager;
class SE_RenderManager;
class SE_InputManager;
class SE_AnimationManager;
//class SE_ElementManager;

class SE_RenderTargetManager;
class SE_ThreadManager;
class SE_UIManager;
class SE_Config;
class SE_RenderSystemCapabilities;
class SE_PluginsManager;
class SE_DynamicLibManager;

class SE_RenderUnitManager;
class ParticleSystemManager;
#ifdef EDITOR
class SE_EditorManager;
#endif
class SE_ENTRY SE_Application
{
public:
    struct SE_APPID
	{
		int first;
		int second;
	};
	struct RenderUnitStatistics
	{
		int vertexNum;
		int faceNum;
		std::string name;
		RenderUnitStatistics()
		{
			vertexNum = 0;
			faceNum = 0;
		}
	};
	struct TexUseData
	{
		std::string name;
		int num;
		TexUseData(const char* name)
		{
			this->name = name;
			num = 1;
		}
	};
    struct Statistics
    {
        int renderVertexNum;
        int renderFaceNum;
        int renderSurfaceNum;
		    std::list<RenderUnitStatistics*> renderUnitData;
        std::list<TexUseData> textureList;
		RenderUnitStatistics* currRenderUnitData;
        Statistics()
        {
            clear();
        }
        ~Statistics()
        {
            clear();
        }
		void setCurrentRenderUnit(const char* name)
		{
            RenderUnitStatistics* ru = new RenderUnitStatistics;
			ru->name = name;
			currRenderUnitData = ru;
			renderUnitData.push_back(ru);
		}
		void setCurrentVertexNum(int num)
		{
			currRenderUnitData->vertexNum = num;
		}
		void setCurrentFaceNum(int num)
		{
			currRenderUnitData->faceNum = num;
		}
        void clear()
        {
            renderVertexNum = 0;
            renderFaceNum = 0;
            renderSurfaceNum = 0;
			      for_each(renderUnitData.begin(), renderUnitData.end(), SE_DeleteObject());
			      renderUnitData.clear();
			      currRenderUnitData = NULL;
            textureList.clear();
        }
        void addVertexNum(int num)
        {
            renderVertexNum += num;
        }
        void addFaceNum(int num)
        {
            renderFaceNum += num;
        }
        void addSurfaceNum(int num)
        {
            renderSurfaceNum += num;
        }
		void setTexture(const char* name)
		{
			std::list<TexUseData>::iterator it;
			for(it = textureList.begin() ; 
				it != textureList.end();
				it++)
			{
				if(it->name == std::string(name))
				{
					it->num++;
					return;
				}
			}
			textureList.push_back(TexUseData(name));
		}
    };
    int getState() const
    {
        return mState;
    }
    void setState(int s)
    {
        mState = s;
    }
	typedef std::vector<SE_Message*> _MessageVector;
    virtual ~SE_Application();
    void run();
    void start();
    void shutdown();
    void sendCommand(SE_Command* command);
    void postCommand(SE_Command* command);
	void sendMessage(SE_Message* message);
	int getMessageCount();
	//after getMessage , the message's pointer are copy to _MessageVector
	_MessageVector getMessage();
	void releaseMessage();
    bool registerCommandFactory(const SE_CommandFactoryID& cfID, SE_CommandFactory* commandFactory);
    bool unreginsterCommandFactory(const SE_CommandFactoryID& cfID);
    SE_Command* createCommand(const SE_CommandID& commandID);
    void setAppID(SE_APPID id)
    {
        mAppID = id;
    }
    SE_APPID getAppID()
    {
        return mAppID;
    }
	void setFrameRate(int frameRate)
	{
		mFrameRate = frameRate;
	}
	int getFrameRate()
	{
		return mFrameRate;
	}
    Statistics& getStatistics()
    {
        return mRenderStatistics;
    }
    SE_CommonID createCommonID();
    SE_ResourceManager* getResourceManager()
	{
		return mResourceManager;
	}
    SE_SceneManager* getSceneManager()
	{
		return mSceneManager;
	}
	SE_RenderManager* getRenderManager()
	{
		return mRenderManager;
	}
	SE_InputManager* getInputManager()
	{
		return mInputManager;
	}
	SE_AnimationManager* getAnimationManager()
	{
		return mAnimationManager;
	}
    /*
	SE_ElementManager* getElementManager()
	{
		return mElementManager;
	}
    */
	SE_RenderTargetManager* getRenderTargetManager()
	{
		return mRenderTargetManager;
	}
    SE_ThreadManager* getThreadManager()
    {
        return mThreadManager;
    }

    ParticleSystemManager* getParticleSystemManager()
	{
		return mParticleSystemManager;
	}  

//	SE_UIManager* getUIManager()
//    {
//        return mUIManager;
//    }

    SE_PluginsManager* getPluginsManager()
    {
        return mPluginsManager;
    }

    SE_DynamicLibManager* getDynamicManager()
    {
        return mDynamicLibManager;
    }
#ifdef EDITOR
	SE_EditorManager* getEditorManager()
	{
		return mEditorManager;
	}
#endif
    static SE_Application* getInstance();
	//SE_Camera* getMainCamera();
	//index 0 to max camera num
	//void setCamera(int index, SE_Camera* camera);
	//SE_Camera* getCamera(int index);
	//void setCurrentCamera(int index);
	SE_Camera* getCurrentCamera();
	SE_Camera* getUICamera();
    bool addDelayDestroy(SE_DelayDestroy* dd);
	size_t getSeqNum()
	{
		return mSeqNum++;
	}
	void setConfig(const char* configFilePath);
	SE_Config* getConfig()
	{
        return mConfig;
	}
    SE_RenderSystemCapabilities* getRenderSystemCapabilities()
    {
        return mRenderSystemCapabilities;
    }
#ifdef ANDROID
    void setJavaCallback(bool (*javaCallback)(int, const char*))
    {
        mJavaCallback = javaCallback;
    }
    bool sendMessageToJava(int msgType, const char* msgName)
    {
        return mJavaCallback(msgType, msgName);
    }
    void setAssetManager(SE_AssetManager* assetManager) {
        mAssetManager = assetManager;
    }
    SE_AssetManager* getAssetManager() {
        return mAssetManager;
    }
#endif

    SE_RenderUnitManager* getRenderUnitManager()
    {
        return mRenderUnitManager;
    }
    SE_Camera* getHelperCamera(int index)
    {
        return mCameraArray[index];
    }


    SE_ResourceManager* createNewLoader();
    
    bool SEHomeDebug;
    void pushLoaderToList(SE_ResourceManager* m);
    SE_ResourceManager* popLoader();
protected:
	class _CommandWrapper
    {
    public:
        _CommandWrapper(SE_Command* c) : command(c)
        {
            canDelete = false;
            canDestroy = true;
        }
        ~_CommandWrapper()
        {
            if(canDestroy)
                delete command;
        } 
        SE_Command* command;
        bool canDelete;
        bool canDestroy;
    };

    virtual void setUpEnv();
    virtual void processCommand(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    void update(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    SE_Application();
	bool isRemoved(const _CommandWrapper& c);
    void doDelayDestroy();

protected:
    struct _CommandFactoryEntry
    {
        SE_CommandFactoryID id;
        SE_CommandFactory* factory;
    };
	class isCommandFactoryIDEqual
	{
	public:
		isCommandFactoryIDEqual(const SE_CommandFactoryID& id): mID(id)
		{}
		bool operator()(const _CommandFactoryEntry& e)
		{
			if(mID == e.id)
				return true;
			else 
				return false;
		}
	private:
		SE_CommandFactoryID mID;
	};
    class _FindDelayDestroy
    {
    public:
        bool operator()(SE_DelayDestroy* dd) const
        {
            if(*src == *dd)
                return true;
            else
                return false;
        }
        SE_DelayDestroy* src;
    };

    //typedef std::list<_CommanDWrapper> SE_CommandList;
    typedef std::list<SE_Command*> SE_CommandList;
    typedef std::list<_CommandFactoryEntry> SE_CommandFactoryList;
	typedef std::list<SE_Message*> SE_MessageList;
    typedef std::list<SE_DelayDestroy*> SE_DelayDestroyList;
    SE_Camera* mCameraArray[MAX_CAMERA_NUM];
    SE_Camera* mCurrentCamera;
    SE_SceneManager* mSceneManager;
    SE_ResourceManager* mResourceManager;
    SE_RenderManager* mRenderManager;
	SE_InputManager* mInputManager;
	SE_AnimationManager* mAnimationManager;
	//SE_ElementManager* mElementManager;
    
	SE_RenderTargetManager* mRenderTargetManager;
    SE_ThreadManager* mThreadManager;
	ParticleSystemManager* mParticleSystemManager;
//	SE_UIManager* mUIManager;
    
    SE_RenderUnitManager* mRenderUnitManager;

    std::list<SE_ResourceManager*> mLoaderList;
#ifdef EDITOR
	SE_EditorManager* mEditorManager;
#endif
    int mFrameNum;
    int mFrameRate;
    SE_TimeMS mStartTime;
    SE_TimeMS mPrevTime;
    SE_CommandList mCommandList;
    SE_Mutex mCommandListMutex;
    SE_CommandFactoryList mCommandFactoryList;
    bool mStarted;
    int mFpsFrameNum;
    SE_TimeMS mFpsPrevTime;
    SE_APPID mAppID;
	int mObjectCount;
	SE_MessageList mMessageList;
    static SE_Application* mInstance;
    int mState;
    size_t mSeqNum; 
	SE_Config* mConfig;
    SE_DelayDestroyList mDelayDestroyList;
    SE_Mutex mDelayDestroyListMutex;
    Statistics mRenderStatistics;
#ifdef ANDROID
    bool (*mJavaCallback)(int, const char*);
    SE_AssetManager* mAssetManager;
#endif
    private:
        SE_RenderSystemCapabilities* mRenderSystemCapabilities;
        SE_PluginsManager* mPluginsManager;
        SE_DynamicLibManager* mDynamicLibManager;        
};
#endif
