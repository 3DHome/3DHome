#ifndef SE_SCENEMANAGER_H
#define SE_SCENEMANAGER_H
#include "SE_ID.h"
#include <list>
#include <vector>
#include "SE_Mutex.h"
#include "SE_OctreeSceneManager.h"
class SE_Spatial;
class SE_Camera;
class SE_CommonNode;
class SE_RenderManager;
class SE_MotionEventController;
class SE_KeyEventController;
class SE_MotionEvent;
class SE_KeyEvent;
class SE_Mutex;
class SE_Light;
class SE_ENTRY SE_Scene
{
public:
    
    SE_Scene(const SE_StringID& sceneName, SE_SCENE_TYPE t = SE_FRAMEBUFFER_SCENE,const char* xmlPath = NULL);
    ~SE_Scene();
    SE_StringID getName() const
    {
        return mName;
    }
    SE_SCENE_TYPE getType() const
    {
        return mType;
    }
    void setVisibility(SE_SCENE_VISIBILITY s)
    {
        mVisibility = s;
    }
    SE_SCENE_VISIBILITY getVisibility() const
    {
        return mVisibility;
    }
    void setCamera(SE_Camera* camera);
    SE_Camera* getCamera() const
    {
        return mCamera;
    }
    
    SE_Camera* getHelperCamera()
    {
        return &mHelperCamera;
    }    
    void setNeedDraw(bool bDraw)
    {
        mNeedDraw = bDraw;
    }
    bool needDraw() const
    {
        return mNeedDraw;
    }
    void setIsTranslucent(bool translucent)
    {
        mIsTranslucent = translucent;
    }
    bool isTranslucent() const
    {
        return mIsTranslucent;
    }
    void setBackgroundColor(const SE_Vector4f& c)
    {
        mBackgroundColor = c;
    }
    SE_Vector4f getBackgroundColor() const
    {
        return mBackgroundColor;
    }
    void setCanHandleInput(bool b)
    {
        mCanHandleInput = b;
    }
    bool canHandleInput() const
    {
        return mCanHandleInput;
    }
    void setSceneNeedClear(bool need)
    {
        mNeedClear = need;
    }
    bool isSceneNeedClear()
    {
        return mNeedClear;
    }
    /*void setMotionEventController(SE_MotionEventController* obj);
    void releaseMotionEventController();
    SE_MotionEventController* getMotionEventController() const;*/
    void setKeyEventController(SE_KeyEventController* obj);
    void releaseKeyEventController();
    SE_KeyEventController* getKeyEventController() const;
    // render a scene to render manager which will render it to render target
    void renderScene(SE_RenderManager& renderManager);
    SE_Spatial* getRoot();
    void setRoot(SE_Spatial* spatial);
    void createRoot(const char* sceneFileName);
    
    SE_Spatial* findSpatialByName(const char* spatialName,int index = 0);
    int findMaxSpatialLayer();
    //add a spatial child to scene manager, it must indicate a parent
    //the parent must be added in scene
    //if parent is NULL , child is the child of scene root.
    //if parent is NULL and scene root is NULL, child will be scene root
    void addSpatial(SE_Spatial* parent, SE_Spatial* child);
    
    SE_Spatial* removeSpatial(SE_Spatial* spatial);
    static void releaseSpatial(SE_Spatial* spatial, int delay = SE_RELEASE_DELAY);
    void checkSpatialIDMap();
    SE_ERROR getError();
    

    void setSkeletonState();

    void setSelectedSpatial(SE_Spatial* spatial);
    void setUnSelectSpatial(SE_Spatial* spatial);
	SE_Spatial* getSelectedSpatial() const
    {
        return mSelectedSpatial;
    }
    void unLoadScene();
    //void dispatchMotionEvent(SE_MotionEvent* motionEvent);
    void dispatchKeyEvent(SE_KeyEvent* keyEvent);
    void loadResource(const char* path,const char* filename);

    void inflate(const char* xmlPath = NULL);
    bool moveCollisionDetect(const char* moveSpatialName,SE_Vector3f& nextTranslate);
    bool moveCollisionDetect(SE_Spatial* movedSpatial, SE_Vector3f& nextTranslate);
    void changeSceneShader(const char* shaderName,const char* renderName);
    bool addLightToScene(SE_Light* light);
    void setLowestEnvBrightness(float lowestbright);
    float getLowestEnvBrightness()
    {
        return mLowestBrightness;
    }
    void removeAllLights();
    void removeLight(const char* lightName);
    SE_Light* getLight(const char* lightName);
    void getAllLights(std::vector<SE_Light*> &list);
    void setSceneManagerType(int type);
    void setSceneManagerDepthType(int max_depth, int type);
    int  getSceneManagerType();
    OctreeSceneManager* getOctreeSceneManager()
    {
	return mOctreeSceneManager;
    }
    void getInvisibleSpatialName(std::vector<std::string>& namelist);

    void saveScene(const char* xmlPath);
    //void saveChildrenEffect(const char* xmlPath,SE_Spatial* saveroot = NULL,const char* assetPath = NULL,const char* type = NULL);
public:
    struct SpatialIDMap;
private:
    SE_Scene(const SE_Scene&);
    SE_Scene& operator=(const SE_Scene&);
    void setError(SE_ERROR e);
    bool intersectDetector(SE_Spatial* movedSpatial,SE_Vector3f& translate);
    SE_SCENE_MANAGER_TYPE mSceneManagerType;
    OctreeSceneManager* mOctreeSceneManager;
    SE_Spatial* find(const SE_SpatialID& spatialID);
    SE_Spatial* removeSpatial(const SE_SpatialID& spatialID);
    void updateSpatialIDMap();
    bool parseLight(const char* xmlPath);

private:
    SE_Spatial* mSceneRoot;
    SE_Spatial* mSelectedSpatial;
    SpatialIDMap* mSpatialIDMap;
    SE_StringID mName;
    SE_SCENE_TYPE mType;
    SE_Camera* mCamera;
    SE_Camera mHelperCamera;
    
    bool mNeedDraw;
    bool mIsTranslucent;
    SE_Vector4f mBackgroundColor;
    bool mCanHandleInput;
    SE_SCENE_VISIBILITY mVisibility;
    //SE_MotionEventController* mMotionEventController;
    SE_KeyEventController* mKeyEventController;
    SE_Mutex mSceneMutex;
    std::map<std::string,SE_Light*> mSceneLights;
    float mLowestBrightness;
    bool mNeedClear;
};
/*
 * scene manager contain all scene in it
 * it has resposibility to release scene
 * other user can not release scene
 * it must invoke removeScene to remove and release scene
 * */
class SE_ENTRY SE_SceneManager
{
public:
    SE_SceneManager();
    ~SE_SceneManager();
    typedef std::vector<SE_Scene*> _SceneSet;
    void pushFront(SE_SCENE_TYPE t, SE_Scene* scene);
    void pushBack(SE_SCENE_TYPE t, SE_Scene* scene);
    void removeScene(SE_Scene* scene, bool isRelease = true);
    void removeScene(const SE_StringID& sceneName, bool isRelease = true);
    _SceneSet getScene(const SE_StringID& sceneName);
    _SceneSet getSceneSet();
    SE_Scene* getScene(SE_SCENE_TYPE t, const SE_StringID& sceneName);
    void insert(SE_SCENE_TYPE t, int index, SE_Scene* scene);
    SE_Scene* getTopScene();
    void setMainScene(const SE_StringID& name);
    SE_Scene* getMainScene() const
    {
        return mMainScene;
    }

    void renderScene(SE_RenderManager& renderManager);
    _SceneSet getSceneFromRemovedList(const SE_StringID& sceneName, bool remove = true);
    SE_Scene* getSceneFromRemovedList(SE_SCENE_TYPE t, const SE_StringID& sceneName, bool remove = true);
    
	SE_Spatial* findSpatialByName(const char* spatialName,int index = 0);
    
    void setSelectedSpatial(SE_Spatial* spatial);
    SE_Spatial* getSelectedSpatial() const;
    int findMaxSpatialLayer();
    void inflate();

    void setSceneManagerStatus(SCENEMANAGER_STATUS status,bool enableOrdisable)
    {
        if(enableOrdisable)
        {
            mSceneManagerStatus |= status;
        }
        else
        {
            mSceneManagerStatus &= (~status);
        }
    }

    bool isSceneManagerHasStatus(SCENEMANAGER_STATUS status)
    {
        return ((mSceneManagerStatus & status) == status);
    }    
private:
    typedef std::list<SE_Scene*> _SceneList;
private:
    void clear();
    void renderScene(int& index, _SceneList* sceneList, SE_RenderManager& renderManager);
    void render(int& index , SE_Scene* scene, SE_RenderManager& renderManager);
    void renderFrameBufferScene(int& index, _SceneList* sceneList, SE_RenderManager& renderManager);
    bool isPostEffectSceneTranslucent();
    SE_SceneManager(const SE_SceneManager&);
    SE_SceneManager& operator=(const SE_SceneManager&);
private:
    unsigned int mSceneManagerStatus;
    SE_Scene* mMainScene;
    _SceneList mSceneListArray[SE_SCENE_TYPE_NUM];
    _SceneList mRemovedSceneListArray[SE_SCENE_TYPE_NUM];
};

#endif
