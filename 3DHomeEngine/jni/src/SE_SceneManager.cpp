#include "SE_DynamicLibType.h"
#include "SE_CommonNode.h"
#include "SE_Mutex.h"
#include "SE_Light.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"
#include "SE_Camera.h"
#include "SE_RenderManager.h"
#include "SE_ResourceManager.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include "SE_SimObject.h"
#include "SE_Log.h"
#include "SE_Mesh.h"
#include "SE_Utils.h"
//#include "SE_MotionEventController.h"
#include "SE_KeyEventController.h"
#include "SE_InputEvent.h"
#include "SE_DelayDestroy.h"
#include <algorithm>
#include "SE_ThreadManager.h"
#include "SE_LoadThread.h"
#include "SE_MemLeakDetector.h"
#include "SE_ObjectManager.h"
#include "tinyxml.h"
#include "SE_RenderTargetManager.h"
#include "SE_SystemCommand.h"
#include "SE_Common.h"

/////////////////////////////////////
struct SE_Scene::SpatialIDMap
{
    typedef std::map<SE_SpatialID, SE_Spatial*> Map;
    Map spatialIDMap;
    SE_SceneManager* sceneManger;
    void add(const SE_SpatialID& spatialID, SE_Spatial* spatial)
    {
        Map::iterator it = spatialIDMap.find(spatialID);
        if(it == spatialIDMap.end())
            spatialIDMap[spatialID] = spatial;
        else
        {
            it->second = spatial;
        }
    }
    void remove(const SE_SpatialID& spatialID)
    {
        Map::iterator it = spatialIDMap.find(spatialID);
        if(it != spatialIDMap.end())
            spatialIDMap.erase(it);
    }
    void clear()
    {
        spatialIDMap.clear();
    }
};
////////////////////////////
///////////////////////////////////
class _SpatialIDTravel : public SE_SpatialTravel
{
public:
	enum {ADD_SPATIAL, REMOVE_SPATIAL};
    int visit(SE_Spatial* spatial)
    {
            if(op == ADD_SPATIAL)
            {
#if 0
                if (spatial->getScene()->getSceneManagerType() == OCTREESCENEMANAGER)
                {     
                    if(spatial->getSpatialType() == GEOMETRY && spatial->getAddOctree() == NO_ADD)  
                    {  
                        spatial->setAddOctree(YES_ADD);
                        OctreeSceneManager* octreeSceneManager = spatial->getScene()->getOctreeSceneManager();          
                        octreeSceneManager->addObject(spatial);
                    } 
                } 
#endif
            }
            else if(op == REMOVE_SPATIAL)
            {        
#if 0
                if (spatial->getScene()->getSceneManagerType() == OCTREESCENEMANAGER)
                {
                    if(spatial->getSpatialType() == GEOMETRY)
                    {
                        spatial->getScene()->getOctreeSceneManager()->destroySceneNode(spatial->getSpatialName());
                    }
                }
#endif
            } 

        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }
    SE_Scene::SpatialIDMap* spatialIDMap;
    int op;
};

class _FindSpatialByNameTravel : public SE_SpatialTravel
{
public:
    _FindSpatialByNameTravel()
    {
        result = NULL;
        index = 0;
    }
    
    int visit(SE_Spatial* spatial)
    {
        std::string spname = spatial->getSpatialName();
        if(spatial->getCloneIndex() == index && spname.compare(findName) == 0)
            {
                result = spatial;
                return 1;
            }
            return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }
    std::string findName;
    SE_Spatial* result;
    int index;
};

class _FindMaxSpatialLayerTravel : public SE_SpatialTravel
{
public:
    _FindMaxSpatialLayerTravel()
    {
        mMax = 0;
    }
    
    int visit(SE_Spatial* spatial)
    {        
       int layercount = spatial->getWorldLayer()->getLayer();
       if(layercount > mMax)
       {
           mMax = layercount;
       }
            
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }    
    int mMax;
};

class _InflateObjectTravel : public SE_SpatialTravel
{
public:
    _InflateObjectTravel()
    {
    }
    
    int visit(SE_Spatial* spatial)
    {      
        spatial->inflate();//just geomotry will do inflate;
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        simObject->inflate();
        return 0;
    }    
};

class _AABBIntersectsTravel : public SE_SpatialTravel
{
public:
    _AABBIntersectsTravel()
    {
        mMovedSaptial = NULL;
        mCollision = false;
    }
    
    int visit(SE_Spatial* spatial)
    {   
        if(spatial->getParent() == NULL || spatial == mMovedSaptial)
        {
            //Do not detect self and root, root include all spatial
            return 0;
        }
        if(spatial->getWorldBoundingVolume()->getType() == AABB)
        {
            SE_AABBBV* aabb = (SE_AABBBV*)spatial->getWorldBoundingVolume();

            SE_AABBBV* move_aabb = (SE_AABBBV*)mMovedSaptial->getWorldBoundingVolume();

            SE_Vector3f min = move_aabb->getGeometry().getMin();
            SE_Vector3f max = move_aabb->getGeometry().getMax();

            //generate new min max
            SE_Vector3f newmin = min.add(SE_Vector3f(min.x/2,min.y/2,min.z/2)) +  mTranslate;
            SE_Vector3f newmax = max.subtract(SE_Vector3f(max.x/2,max.y/2,max.z/2)) + mTranslate;

            SE_AABB temp;
            temp.set(newmin,newmax);            

            SE_IntersectResult result = temp.intersect(aabb->getGeometry());

            if(result.intersected)
            {
                mCollisionSpatialName = spatial->getSpatialName();
                mCollision = true;
                return 1;

            }
        }
        
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {        
        return 0;
    }

    SE_Spatial* mMovedSaptial;
    bool mCollision;
    std::string mCollisionSpatialName;
    SE_Vector3f mTranslate;
};

class _ChangeShaderTravel : public SE_SpatialTravel
{
public:
    _ChangeShaderTravel()
    {
    }
    
    int visit(SE_Spatial* spatial)
    {
        spatial->setRenderType(rendername.c_str());
        spatial->setShaderType(shadername.c_str());
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }
    std::string shadername;
    std::string rendername;
};

class _addLightTravel : public SE_SpatialTravel
{
public:
    _addLightTravel()
    {
    }

    int visit(SE_Spatial* spatial)
    {      
        if(!lightName.empty())
        {
            spatial->addLightNameToList(lightName.c_str());
        }
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        
        return 0;
    }

    std::string lightName;
};
class _RemoveALLLightTravel : public SE_SpatialTravel
{
public:
    _RemoveALLLightTravel(){}

    int visit(SE_Spatial* spatial)
    {        
        spatial->removeAllLight();
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }

};

class _RemoveLightTravel : public SE_SpatialTravel
{
public:
    _RemoveLightTravel(){}

    int visit(SE_Spatial* spatial)
    {
        spatial->removeLight(lightname.c_str());
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {        
        return 0;
    }
    std::string lightname;

};

class _FindGeometryTravel : public SE_SpatialTravel
{
public:
    enum {ADD_OCTREESPATIAL};
    int visit(SE_Spatial* spatial)
    {
				
	    if(op == ADD_OCTREESPATIAL)
            {
#if 0
                if (spatial->getScene()->getSceneManagerType() == OCTREESCENEMANAGER)
                {     
                    if(spatial->getSpatialType() == GEOMETRY && spatial->getAddOctree() == NO_ADD)  
                    {
                        spatial->setAddOctree(YES_ADD);
                        OctreeSceneManager* octreeSceneManager = spatial->getScene()->getOctreeSceneManager();          
                        octreeSceneManager->addObject(spatial);
                    } 
                }
#endif
            }

	return 0;
    }

    int visit(SE_SimObject* simObject)
    {
	return 0;
    }
    int op;
};

class _FindAllInvisibleSpatialTravel : public SE_SpatialTravel
{
public:
    int visit(SE_Spatial* spatial)
    {
        if(!spatial->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
        {
            std::string invname = spatial->getSpatialName();
            invisibleSpatialName.push_back(invname);
        }
	    return 0;
    }
    int visit(SE_SimObject* simObject)
    {        
        return 0;
    }
    std::vector<std::string> invisibleSpatialName;

};
SE_Scene::SE_Scene(const SE_StringID& sceneName, SE_SCENE_TYPE t,const char* xmlPath)
{
    mSceneRoot = NULL;
    mSelectedSpatial = NULL;
    mSpatialIDMap = new SE_Scene::SpatialIDMap;
    mName = sceneName;
    mType = t;
    mNeedDraw = true;
    mCamera = NULL;    
    mIsTranslucent = false;
    mVisibility = SE_VISIBLE;
    if(mType == SE_FRAMEBUFFER_SCENE)
    {
        mCanHandleInput = true;
    }
    else
    {
        mCanHandleInput = false;
    }

    mLowestBrightness = 0.0;
    mSceneManagerType = DEFAULT;
    //mSceneManagerType = OCTREESCENEMANAGER;
    //SE_AABB aabb;    
    //aabb.set(SE_Vector3f(-10000, -10000, -10000),SE_Vector3f(10000, 10000, 10000));
    //mOctreeSceneManager = new OctreeSceneManager("octree", aabb, 3);
    mOctreeSceneManager = NULL;
    mNeedClear = false;
}
SE_Scene::~SE_Scene()
{
    if(mSceneRoot)
    {
        if (!mSceneRoot->getParent())
        {
            delete mSceneRoot;
        }
    }
    delete mSpatialIDMap;
#ifndef EDITOR
    delete mCamera;
#endif

    //render target manager will do this
    //delete mRenderTarget;

    std::map<std::string,SE_Light*>::iterator it;
    for(it = mSceneLights.begin(); it != mSceneLights.end(); it++)
    {
        SE_Light* l = it->second;
        delete l;
    }
    mSceneLights.clear();
    if(mOctreeSceneManager)
    {
	delete mOctreeSceneManager;
    }

   // SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
   // rtm->releaseHardware();

    //SE_ResourceManager* rm = SE_Application::getInstance()->getResourceManager();
   // rm->clearIdPath();
    
}
void SE_Scene::renderScene(SE_RenderManager& renderManager)
{
    SE_AutoMutex m(&mSceneMutex);
    if(mCamera && mSceneRoot)
    {
        if(mSceneManagerType == DEFAULT) 
        {   
            mSceneRoot->updateBoundingVolume();
        mSceneRoot->renderScene(mCamera, &renderManager, SE_PART_CULL);
    }
        else if (mSceneManagerType == OCTREESCENEMANAGER)
        {
            mSceneRoot->updateBoundingVolume();
            mOctreeSceneManager->renderScene(this->getCamera(), renderManager);
        }    
    }
    //renderManager.sort();
}
//void SE_Scene::setMotionEventController(SE_MotionEventController* obj)
//{
//    if(mMotionEventController)
//    {
//        delete mMotionEventController;
//    }
//    mMotionEventController = obj;
//}
//
//void SE_Scene::releaseMotionEventController()
//{
//    if(mMotionEventController)
//    {
//        delete mMotionEventController;
//    }
//    mMotionEventController = NULL;
//}
//SE_MotionEventController* SE_Scene::getMotionEventController() const
//{
//    return mMotionEventController;
//}
void SE_Scene::setKeyEventController(SE_KeyEventController* obj)
{
    if(mKeyEventController)
    {
        delete mKeyEventController;
    }
    mKeyEventController = obj;
}
void SE_Scene::releaseKeyEventController()
{
    if(mKeyEventController)
    {
        delete mKeyEventController;
    }
    mKeyEventController = NULL;
}
SE_KeyEventController* SE_Scene::getKeyEventController() const
{
    return mKeyEventController;
}
//void SE_Scene::dispatchMotionEvent(SE_MotionEvent* motionEvent)
//{
//    if(mMotionEventController)
//    {
//        mMotionEventController->onMotionEvent(motionEvent);
//    }
//    else
//    {
//        if(mCamera)
//        {
//            mCamera->onMotionEvent(motionEvent);
//        }
//    }
//}
void SE_Scene::dispatchKeyEvent(SE_KeyEvent* keyEvent)
{}
SE_Spatial* SE_Scene::getRoot()
{
    return mSceneRoot;
}
void SE_Scene::setRoot(SE_Spatial* root)
{
   // if(mSceneRoot)
   //     delete mSceneRoot;
    mSceneRoot = root;
    mSceneRoot->mScene = this;
    //updateSpatialIDMap();
}
void SE_Scene::setSelectedSpatial(SE_Spatial* spatial)
{
    if(mSelectedSpatial)
    {
        mSelectedSpatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::SELECTED,false);
    }
    mSelectedSpatial = spatial;
    if(mSelectedSpatial)
        mSelectedSpatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::SELECTED,true);
}

void SE_Scene::setUnSelectSpatial(SE_Spatial* spatial)
{
    if(spatial)
    {
        spatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::SELECTED,false);
    }
    mSelectedSpatial = NULL;
    
}
SE_Spatial* SE_Scene::find(const SE_SpatialID& spatialID)
{
    SE_ASSERT(mSpatialIDMap);
    SE_Scene::SpatialIDMap::Map::iterator it = mSpatialIDMap->spatialIDMap.find(spatialID);
    if(it != mSpatialIDMap->spatialIDMap.end())
        return it->second;
    else
        return NULL;
}
void SE_Scene::setCamera(SE_Camera* camera) 
{
	if(camera == mCamera) return;
#ifndef EDITOR
    if(mCamera)
        delete mCamera;
#endif
    mCamera = camera;
}

void SE_Scene::createRoot(const char* sceneFileName)
{
    if(mSceneRoot != NULL)
    {
        //add new cbf to the exist scene
        delete mSceneRoot;    
    }
    mSceneRoot = SE_Application::getInstance()->getResourceManager()->loadScene(sceneFileName);
    mSceneRoot->mScene = this;
    //updateSpatialIDMap();
}
void SE_Scene::updateSpatialIDMap()
{
    mSpatialIDMap->clear();
    _SpatialIDTravel addSpatialIDTravel;
    addSpatialIDTravel.spatialIDMap = mSpatialIDMap;
    addSpatialIDTravel.op = _SpatialIDTravel::ADD_SPATIAL;
        
    if(mSceneRoot)
        mSceneRoot->travel(&addSpatialIDTravel, true);    
}

void SE_Scene::setSkeletonState()
{
}

void SE_Scene::addSpatial(SE_Spatial* parent, SE_Spatial* child)
{
    SE_AutoMutex m(&mSceneMutex);
    if(!child)
        return;
    if(parent == NULL)
    {
        if(mSceneRoot)
        {
            mSceneRoot->addChild(child);
            child->setParent(mSceneRoot);
        }
        else
        {
            mSceneRoot = child;
            child->setParent(NULL);
        }
    }
    else
    {
        parent->addChild(child);
        child->setParent(parent);
    }

    /*_SpatialIDTravel addSpatialIDTravel;
    addSpatialIDTravel.spatialIDMap = mSpatialIDMap;
    addSpatialIDTravel.op = _SpatialIDTravel::ADD_SPATIAL;
    child->travel(&addSpatialIDTravel, true);*/
}
SE_Spatial* SE_Scene::removeSpatial(const SE_SpatialID& spatialID)
{
#if 0
    SE_AutoMutex m(&mSceneMutex);
    SE_Spatial* spatial = find(spatialID);
    if(!spatial)
        return NULL;
    SE_Spatial* retSpatial = spatial;
    SE_Spatial* parent = spatial->getParent();
    if(parent)
    {
        parent->removeChild(spatial);
    }
    else
    {
        mSceneRoot = NULL;
    }
    _SpatialIDTravel removeSpatialIDTravel;
    removeSpatialIDTravel.spatialIDMap = mSpatialIDMap;
    removeSpatialIDTravel.op = _SpatialIDTravel::REMOVE_SPATIAL;
    retSpatial->travel(&removeSpatialIDTravel, true);
    return retSpatial;
#endif
    return NULL;
}

SE_Spatial* SE_Scene::removeSpatial(SE_Spatial* spatial)
{
    SE_AutoMutex m(&mSceneMutex);
	if(!spatial)
		return NULL;    

	SE_Spatial* retSpatial = spatial;
	SE_Spatial* parent = spatial->getParent();
	if(parent)
	{
		parent->removeChild(spatial);
	}
	else
	{
        mSceneRoot = NULL;
	}
	/*_SpatialIDTravel removeSpatialIDTravel;
	removeSpatialIDTravel.spatialIDMap = mSpatialIDMap;
	removeSpatialIDTravel.op = _SpatialIDTravel::REMOVE_SPATIAL;
	retSpatial->travel(&removeSpatialIDTravel, true);*/
	return retSpatial;
}

void SE_Scene::checkSpatialIDMap()
{}

void SE_Scene::unLoadScene()
{
    //release this after resourceManager free;
    mSpatialIDMap->clear();
}
void SE_Scene::releaseSpatial(SE_Spatial* spatial, int delay)
{
    if(spatial == NULL)
        return;

    if(delay == SE_RELEASE_NO_DELAY)
    {
        delete spatial;
    }
    else
    {
        SE_DelayDestroy* dd = new SE_DelayDestroyPointer<SE_Spatial>(spatial);
        bool ret = SE_Application::getInstance()->addDelayDestroy(dd);
        if(!ret)
        {
            delete dd;
        }
    }
}
SE_Spatial* SE_Scene::findSpatialByName(const char* spatialName,int index)
{
#if 1
    _FindSpatialByNameTravel ft;
    ft.findName = spatialName;
    ft.index = index;

    if(mSceneRoot)
    {
        mSceneRoot->travel(&ft, false);
    }

    if(ft.result)
    {
        return ft.result;
    }

    return NULL;
#endif
    /*SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
    return sm->findByName(spatialName);*/
}

void SE_Scene::loadResource(const char* path,const char* filename)
{
    //SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    //resourceManager->setDataPath(path);

    SE_ResourceManager* loader = SE_Application::getInstance()->createNewLoader();

    SE_LoadThread* lthread = new SE_LoadThread(loader, path,filename);
    lthread->setName("loadresource");
    SE_ThreadManager* threadManager = SE_Application::getInstance()->getThreadManager();
    threadManager->add(lthread);
    lthread->start();
}

int SE_Scene::findMaxSpatialLayer()
{
    _FindMaxSpatialLayerTravel ft;    

    if(mSceneRoot)
    {
        mSceneRoot->travel(&ft, true);
    }
    return ft.mMax;
}

void SE_Scene::inflate(const char* xmlPath)
{    
    if(xmlPath)
    {
        this->parseLight(xmlPath);
    }
    _InflateObjectTravel it;
    if(mSceneRoot)
    {
        
        mSceneRoot->travel(&it, true);
        mSceneRoot->updateWorldTransform();
        mSceneRoot->updateBoundingVolume();
        mSceneRoot->updateWorldLayer();
        mSceneRoot->updateRenderState();
        mSceneRoot->updateUsetRenderState();

        if (mSceneManagerType == OCTREESCENEMANAGER)
        {
            _FindGeometryTravel addGeometryTravel;
            addGeometryTravel.op = _FindGeometryTravel::ADD_OCTREESPATIAL;
            mSceneRoot->travel(&addGeometryTravel, true); 
        }    		            
    }    
    
    if (mSceneManagerType == OCTREESCENEMANAGER)
    {
        mOctreeSceneManager->resize(((SE_AABBBV*)mSceneRoot->getWorldBoundingVolume())->getGeometry());
    }
}

bool SE_Scene::intersectDetector(SE_Spatial *moveSpatial,SE_Vector3f& translate)
{
    _AABBIntersectsTravel aabbit;
    aabbit.mMovedSaptial = moveSpatial;
    aabbit.mTranslate = translate;
    if(mSceneRoot)
    {
        mSceneRoot->travel(&aabbit,true);

        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Collision Detect!! the name is %s\n",aabbit.mCollisionSpatialName.c_str());
        
        return aabbit.mCollision;
    }
    return false;
}

bool SE_Scene::moveCollisionDetect(const char* moveSpatialName,SE_Vector3f& nextTranslate)
{
    SE_Spatial* move = findSpatialByName(moveSpatialName);

    if(move)
    {
        return intersectDetector(move,nextTranslate);
    }

    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### the spatial not found ###\n");
    return false;
}

bool SE_Scene::moveCollisionDetect(SE_Spatial *moveSpatial, SE_Vector3f& nextTranslate)
{
    if(moveSpatial)
    {
        return intersectDetector(moveSpatial,nextTranslate);
    }

    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### the spatial not found ###\n");
    return false;
}

void SE_Scene::changeSceneShader(const char* shaderName,const char* renderName)
{
    SE_AutoMutex m(&mSceneMutex);
    _ChangeShaderTravel lt;
    lt.rendername = renderName;
    lt.shadername = shaderName;
    if(mSceneRoot)
    {
        mSceneRoot->travel(&lt,true);
    }

}

bool SE_Scene::addLightToScene(SE_Light *light)
{    
    //show light has mutex
    if(!light)
    {
        return false;
    }

    std::string name = light->getLightName();
    std::map<std::string,SE_Light*>::iterator it = mSceneLights.find(name);

    if(it != mSceneLights.end())
    {
        //light has been in scene
        return false;
    }        

    mSceneLights.insert(std::pair<std::string,SE_Light*>(name,light));
    return true;
}

void SE_Scene::setLowestEnvBrightness(float lowestbright)
{
    mLowestBrightness = lowestbright;
}

void SE_Scene::removeAllLights()
{
    SE_AutoMutex m(&mSceneMutex);
    _RemoveALLLightTravel rml;    
    if(mSceneRoot)
    {
        mSceneRoot->travel(&rml,true);    
    }

    std::map<std::string,SE_Light*>::iterator it;
    for(it = mSceneLights.begin(); it != mSceneLights.end(); it++)
    {
        SE_Light* l = it->second;
        delete l;
    }
    mSceneLights.clear();
}

void SE_Scene::removeLight(const char* lightName)
{    

    std::string ln = lightName;
    std::map<std::string,SE_Light*>::iterator it = mSceneLights.find(ln);
    if(it != mSceneLights.end())
    {
        SE_Light* l = it->second;
        delete l;
        mSceneLights.erase(it);

        _RemoveLightTravel rml;
        rml.lightname = lightName;
        if(mSceneRoot)
        {
            mSceneRoot->travel(&rml,true);    
        }
    }
}

void SE_Scene::getAllLights(std::vector<SE_Light*> &list)
{
    std::map<std::string,SE_Light*>::iterator it;
    for(it = mSceneLights.begin(); it != mSceneLights.end(); it++)
    {
        SE_Light* l = it->second;
        list.push_back(l);
    }
}

SE_Light* SE_Scene::getLight(const char* lightName)
{
    std::string name = lightName;
    std::map<std::string,SE_Light*>::iterator it = mSceneLights.find(name);
    if(it != mSceneLights.end())
    {
        return it->second;
    }
    return NULL;   
}

void SE_Scene::setSceneManagerType(int type)
{
    mSceneManagerType = (SE_SCENE_MANAGER_TYPE)type;

    if (type == OCTREESCENEMANAGER)
    {
        if (!mOctreeSceneManager)
        {
            //SE_AABB aabb;
            //aabb.set(SE_Vector3f(-2314, -2314, -110),SE_Vector3f(2314, 2314, 1001));
	    mOctreeSceneManager = new OctreeSceneManager("octree");
        }   
    }   
}
void SE_Scene::setSceneManagerDepthType(int max_depth, int type)
{
    mSceneManagerType = (SE_SCENE_MANAGER_TYPE)type;
    if (type == OCTREESCENEMANAGER)
    {
        if (!mOctreeSceneManager)
        {
        SE_AABB aabb;
            aabb.set(SE_Vector3f(-10000, -10000, -10000),SE_Vector3f(10000, 10000, 10000));
        mOctreeSceneManager = new OctreeSceneManager("octree", aabb, max_depth); 
    }  
}
}
int SE_Scene::getSceneManagerType()
{
    return mSceneManagerType;
}

void SE_Scene::getInvisibleSpatialName(std::vector<std::string>& namelist)
{
    _FindAllInvisibleSpatialTravel fiv;
    if(mSceneRoot)
    {
        mSceneRoot->travel(&fiv,true);    
    }
    namelist = fiv.invisibleSpatialName;
}

bool SE_Scene::parseLight(const char *xmlPath)
{
    SE_TimeMS currTime = 0;
    if(SE_Application::getInstance()->SEHomeDebug)
    {
        currTime = SE_Time::getCurrentTimeMS();
    }

    std::string path = xmlPath;
    size_t pos = path.rfind('/');
    path = path.substr(0,pos + 1);
#ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile(data,len);
    delete data;
#else    
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif

    if(!ok)
    {
        return NULL;
    }
    LOGI("\n %s: \n",xmlPath);   

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem;
    TiXmlHandle hRoot(0);
    TiXmlElement* parent = NULL;

    pElem = hDoc.FirstChildElement("resource").Element();

    if(!pElem)
    {
        LOGI("resource not found!!\n");
        return false;
    }

    pElem = pElem->FirstChildElement("lights");
    
    if(!pElem)
    {
        LOGI("lights not found!!\n");
        return false;
    }

    pElem = pElem->FirstChildElement("light");
    if(!pElem)
    {
        LOGI("light not found!!\n");
        return false;
    }

    parent = pElem;
    for(parent;parent;parent = parent->NextSiblingElement())
    {
        int type = -1;
        pElem = parent->FirstChildElement("lightType");
        if(!pElem)
        {
            LOGI("lightType not found!!\n");
            return false;
        }
        pElem->QueryIntAttribute("lighttype",&type);

        std::string lightname;
        pElem = parent->FirstChildElement("lightName");
        if(!pElem)
        {
            LOGI("lighttype not found!!\n");
            return false;
        }
        pElem->QueryStringAttribute("lightname",&lightname);

        SE_Vector3f lightcolor;
        pElem = parent->FirstChildElement("lightColor");
        if(!pElem)
        {
            LOGI("lightColor not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("x",&lightcolor.x);
        pElem->QueryFloatAttribute("y",&lightcolor.y);
        pElem->QueryFloatAttribute("z",&lightcolor.z);

        SE_Vector3f lightpos;
        pElem = parent->FirstChildElement("lightPos");
        if(!pElem)
        {
            LOGI("lightPos not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("x",&lightpos.x);
        pElem->QueryFloatAttribute("y",&lightpos.y);
        pElem->QueryFloatAttribute("z",&lightpos.z);
        //pElem->QueryFloatAttribute("w",&lightpos.w);

        SE_Vector3f lightdir;
        pElem = parent->FirstChildElement("lightDir");
        if(!pElem)
        {
            LOGI("lightDir not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("x",&lightdir.x);
        pElem->QueryFloatAttribute("y",&lightdir.y);
        pElem->QueryFloatAttribute("z",&lightdir.z);


        SE_Vector3f lightattenuation;
        pElem = parent->FirstChildElement("lightAttenuation");
        if(!pElem)
        {
            LOGI("lightAttenuation not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("constant",&lightattenuation.x);
        pElem->QueryFloatAttribute("linear",&lightattenuation.y);
        pElem->QueryFloatAttribute("quadratic",&lightattenuation.z);

        float dirlightstrength = 0;
        pElem = parent->FirstChildElement("lightStrength");
        if(!pElem)
        {
            LOGI("lightStrength not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("strength",&dirlightstrength);


        SE_Vector2f spotlightpara;
        pElem = parent->FirstChildElement("lightSpot");
        if(!pElem)
        {
            LOGI("lightSpot not found!!\n");
            return false;
        }
        pElem->QueryFloatAttribute("cutoff",&spotlightpara.x);
        pElem->QueryFloatAttribute("exp",&spotlightpara.y);

        SE_Light* light = new SE_Light();
        light->setLightType((SE_Light::LightType)type);
        light->setLightName(lightname.c_str());
        light->setLightColor(lightcolor);        
        light->setLightPos(lightpos);
        light->setLightDir(lightdir);
        light->setConstantAttenuation(lightattenuation.x);
        light->setLinearAttenuation(lightattenuation.y);
        light->setQuadraticAttenuation(lightattenuation.z);
        light->setDirLightStrength(dirlightstrength);
        light->setSpotLightCutOff(spotlightpara.x);
        light->setSpotLightExp(spotlightpara.y);

        this->addLightToScene(light);      

    }

    if(SE_Application::getInstance()->SEHomeDebug)
    {
        SE_TimeMS finishTime = SE_Time::getCurrentTimeMS();
        LOGI("\n\n SE_Scene::parseLight  xmlpath[ %s], duration = %lu ms\n\n",xmlPath,finishTime - currTime);
    }
    return true;
}

void SE_Scene::saveScene(const char* xmlPath)
{
    TiXmlDocument doc;
	TiXmlDeclaration * decl = new TiXmlDeclaration( "1.0", "utf-8", "" );
    doc.LinkEndChild(decl);

	TiXmlElement * root = new TiXmlElement( "resource" );
    doc.LinkEndChild(root);    

    TiXmlElement * lights = new TiXmlElement( "lights" );
    root->LinkEndChild(lights); 

    std::map<std::string,SE_Light*>::iterator it = mSceneLights.begin();
    for(; it != mSceneLights.end();it++)
    {
        SE_Light* l = it->second;

        TiXmlElement * light = new TiXmlElement("light");
        lights->LinkEndChild(light); 

        TiXmlElement * lighttype = new TiXmlElement("lightType");
        light->LinkEndChild(lighttype);
        lighttype->SetAttribute("lighttype",(int)l->getLightType());

        TiXmlElement * lightname = new TiXmlElement("lightName");
        light->LinkEndChild(lightname);
        lightname->SetAttribute("lightname",l->getLightName());

        TiXmlElement * lightcolor = new TiXmlElement("lightColor");
        light->LinkEndChild(lightcolor);
        lightcolor->SetDoubleAttribute("x",l->getLightColor().x);
        lightcolor->SetDoubleAttribute("y",l->getLightColor().y);
        lightcolor->SetDoubleAttribute("z",l->getLightColor().z);

        TiXmlElement * lightpos = new TiXmlElement("lightPos");
        light->LinkEndChild(lightpos);
        lightpos->SetDoubleAttribute("x",l->getLightPos().x);
        lightpos->SetDoubleAttribute("y",l->getLightPos().y);
        lightpos->SetDoubleAttribute("z",l->getLightPos().z);
        //lightpos->SetDoubleAttribute("w",l->getLightPos().w);

        TiXmlElement * lightdir = new TiXmlElement("lightDir");
        light->LinkEndChild(lightdir);
        lightdir->SetDoubleAttribute("x",l->getLightDir().x);
        lightdir->SetDoubleAttribute("y",l->getLightDir().y);
        lightdir->SetDoubleAttribute("z",l->getLightDir().z);

        TiXmlElement * lightattenuation = new TiXmlElement("lightAttenuation");
        light->LinkEndChild(lightattenuation);
        lightattenuation->SetDoubleAttribute("constant",l->getConstantAttenuation());
        lightattenuation->SetDoubleAttribute("linear",l->getLinearAttenuation());
        lightattenuation->SetDoubleAttribute("quadratic",l->getQuadraticAttenuation());

        TiXmlElement * lightstrength = new TiXmlElement("lightStrength");
        light->LinkEndChild(lightstrength);
        lightstrength->SetDoubleAttribute("strength",l->getDirLightStrength());

        TiXmlElement * lightspot = new TiXmlElement("lightSpot");
        light->LinkEndChild(lightspot);
        lightspot->SetDoubleAttribute("cutoff",l->getSpotLightCutOff());
        lightspot->SetDoubleAttribute("exp",l->getSpotLightExp());
        
    }

    doc.SaveFile(xmlPath);
}

/////////////
SE_SceneManager::SE_SceneManager()
{
    mMainScene = NULL;
    mSceneManagerStatus = 0;
    this->setSceneManagerStatus(ENABLELIGHT,true);
    this->setSceneManagerStatus(ENABLEMIRROR,true);
    this->setSceneManagerStatus(ENABLEGROUNDSHADOW,true);
    this->setSceneManagerStatus(ENABLEWALLSHADOW,true);
}
SE_SceneManager::~SE_SceneManager()
{
    clear();
}
void SE_SceneManager::pushFront(SE_SCENE_TYPE t, SE_Scene* scene)
{
    if(t < 0 || t >= SE_SCENE_TYPE_NUM)
        return;
    if(scene == NULL)
        return;
    _SceneList* sceneList = &mSceneListArray[t];
    sceneList->push_front(scene);
}
void SE_SceneManager::pushBack(SE_SCENE_TYPE t, SE_Scene* scene)
{
    if(t < 0 || t >= SE_SCENE_TYPE_NUM)
        return;
    if(scene == NULL)
        return;
    _SceneList* sceneList = &mSceneListArray[t];
    sceneList->push_back(scene);
}
void SE_SceneManager::removeScene(SE_Scene* scene, bool isRelease)
{
    if(scene == NULL)
        return;
    bool found = false;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList::iterator it;
        _SceneList* sceneList = &mSceneListArray[i];
        for(it = sceneList->begin(); it != sceneList->end(); it++)
        {
            if(scene == *it)
            {
                found = true;
                sceneList->remove(*it);
                break;
            }
        }
    }
    if(found)
    {
        if(isRelease)
        {
			if(mMainScene == scene)
			{
				mMainScene = NULL;
			}
            delete scene;
        }
        else
        {
            mRemovedSceneListArray[scene->getType()].push_back(scene);
        }
    }
}
void SE_SceneManager::removeScene(const SE_StringID& sceneName, bool isRelease)
{
    _SceneList rmSceneList;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList::iterator it;
        _SceneList* sceneList = &mSceneListArray[i];
        for(it = sceneList->begin(); it != sceneList->end(); it++)
        {
            if(sceneName == (*it)->getName())
            {
                rmSceneList.push_back(*it);
                sceneList->remove(*it);
                break;
            }
        }
    }
    _SceneList::iterator it;
    for(it = rmSceneList.begin() ; it != rmSceneList.end() ; it++)
    {
        if(isRelease)
        {
            if (mMainScene == *it)
            {
                 mMainScene = NULL;
            }
            delete *it;
        }
        else
        {
            mRemovedSceneListArray[(*it)->getType()].push_back(*it);
        }
    }
}
SE_SceneManager::_SceneSet SE_SceneManager::getScene(const SE_StringID& sceneName)
{
    _SceneList foundList;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList::iterator it;
        _SceneList* sceneList = &mSceneListArray[i];
        for(it = sceneList->begin(); it != sceneList->end(); it++)
        {
            if(sceneName == (*it)->getName())
            {
                foundList.push_back(*it);
            }
        }
    }
    _SceneSet retV(foundList.size());
    copy(foundList.begin(), foundList.end(), retV.begin());
    return retV;
}

SE_SceneManager::_SceneSet SE_SceneManager::getSceneSet()
{
    _SceneList foundList;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList::iterator it;
        _SceneList* sceneList = &mSceneListArray[i];
        for(it = sceneList->begin(); it != sceneList->end(); it++)
        {            
            foundList.push_back(*it);
        }
    }
    _SceneSet retV(foundList.size());
    copy(foundList.begin(), foundList.end(), retV.begin());
    return retV;
}
SE_Scene* SE_SceneManager::getScene(SE_SCENE_TYPE t, const SE_StringID& sceneName)
{
    if(t < 0 || t >= SE_SCENE_TYPE_NUM)
        return NULL;
    _SceneList* sceneList = &mSceneListArray[t];
    _SceneList::iterator it;
    for(it = sceneList->begin() ; it != sceneList->end() ; it++)
    {
        if((*it)->getName() == sceneName)
            return *it;
    }
    return NULL;
}
void SE_SceneManager::insert(SE_SCENE_TYPE t, int index, SE_Scene* scene)
{
    if(t < 0 || t >= SE_SCENE_TYPE_NUM)
        return;
    if(scene == NULL)
        return;
    _SceneList* sceneList = &mSceneListArray[t];
    _SceneList::iterator it = listElementRef(*sceneList, index);
    sceneList->insert(it, scene);
}
class _RemoveScene
{
public:
    bool operator()(SE_Scene* scene)
    {
        if(scene->getName() == name)
            return true;
        else
            return false;
    }
    SE_StringID name;
};
void SE_SceneManager::renderScene(int& index, _SceneList* sceneList, SE_RenderManager& renderManager)
{
    _SceneList::iterator it;
    for(it = sceneList->begin() ; it != sceneList->end() ; it++)
    {
        SE_Scene* scene = *it;
        render(index, scene, renderManager);
    }
}
void SE_SceneManager::render(int& index , SE_Scene* scene, SE_RenderManager& renderManager)
{
    if(scene->needDraw() && scene->getVisibility() == SE_VISIBLE)
    {
        bool ret = renderManager.setCurrentScene(index);
        if(ret)
        {
            renderManager.setCurrentSceneNeedClear(scene->isSceneNeedClear());
            renderManager.setCurrentCamera(scene->getCamera());
            renderManager.setCurrentHelperCamera(scene->getHelperCamera());
            renderManager.setCurrentBackgroundColor(scene->getBackgroundColor());
            scene->renderScene(renderManager);
            index++;
        }
        else
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("set current scene failed\n");
        }
    }
}
void SE_SceneManager::renderFrameBufferScene(int& index, _SceneList* sceneList, SE_RenderManager& renderManager)
{
    _SceneList renderScene;
    _SceneList::reverse_iterator it;
    _SceneList::reverse_iterator firstOpaqueSceneIt = sceneList->rend();
    for(it = sceneList->rbegin() ; it != sceneList->rend() ; it++)
    {
        SE_Scene* scene = *it;
        if(!scene->isTranslucent() && scene->getVisibility() == SE_VISIBLE)
        {
            firstOpaqueSceneIt = it;
            //there is a opaque scene,opengl should clear screen,otherwise do not clear sceen.for multi-scene can draw.
            (*it)->setSceneNeedClear(true);;
            break; 
        }
    }
    if(firstOpaqueSceneIt == sceneList->rend())
    {
        _SceneList::iterator it1;
        for(it1 = sceneList->begin() ; it1 != sceneList->end() ; it1++)
        {
            SE_Scene* scene = *it1;
            render(index, scene, renderManager);
        }
    }
    else
    {
        firstOpaqueSceneIt++;
        for(it = sceneList->rbegin() ; it != firstOpaqueSceneIt ; it++)
        {
            SE_Scene* scene = *it;
            renderScene.push_front(scene);
        }
        _SceneList::iterator it1;
        for(it1 = renderScene.begin() ; it1 != renderScene.end() ; it1++)
        {
            SE_Scene* scene = *it1;
            render(index, scene, renderManager);
        }
    }
}
bool SE_SceneManager::isPostEffectSceneTranslucent() 
{
    _SceneList* sceneList = &mSceneListArray[SE_POST_EFFECT_SCENE];
    _SceneList::reverse_iterator it;
    _SceneList::reverse_iterator firstOpaqueSceneIt = sceneList->rend();
    for(it = sceneList->rbegin() ; it != sceneList->rend() ; it++)
    {
        SE_Scene* scene = *it;
        if(!scene->isTranslucent() && scene->getVisibility() == SE_VISIBLE)
        {
            firstOpaqueSceneIt = it;
            break; 
        }
    }
    if(firstOpaqueSceneIt != sceneList->rend())
    {
        return false;
    }
    else
        return true;
}
void SE_SceneManager::renderScene(SE_RenderManager& renderManager)
{
    int index = 0;
    _SceneList* sceneList = &mSceneListArray[SE_TEXTURE_SCENE];
    renderScene(index, sceneList, renderManager);
 
    sceneList = &mSceneListArray[SE_RENDERBUFFER_SCENE];
    renderScene(index, sceneList, renderManager);
 
    if(isPostEffectSceneTranslucent())
    {
        _SceneList* sceneList = &mSceneListArray[SE_FRAMEBUFFER_SCENE];
        renderFrameBufferScene(index, sceneList, renderManager);
    }
 
    sceneList = &mSceneListArray[SE_POST_EFFECT_SCENE];
    renderFrameBufferScene(index, sceneList, renderManager);
}
SE_SceneManager::_SceneSet SE_SceneManager::getSceneFromRemovedList(const SE_StringID& sceneName, bool remove)
{
    _SceneList newSceneList;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList* sceneList = &mRemovedSceneListArray[i];
        _SceneList::iterator it;
        for(it = sceneList->begin() ; it != sceneList->end() ; it++)
        {
            if((*it)->getName() == sceneName)
            {
                newSceneList.push_back(*it);
            }
        }
    }
    _SceneSet retV(newSceneList.size());
    copy(newSceneList.begin(), newSceneList.end(), retV.begin());
    if(remove)
    {
        _RemoveScene rs;
        rs.name = sceneName;
        for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
        {
            _SceneList* sceneList = &mRemovedSceneListArray[i];
            sceneList->remove_if(rs);
        }
    }
    return retV;
}
SE_Scene* SE_SceneManager::getSceneFromRemovedList(SE_SCENE_TYPE t, const SE_StringID& sceneName, bool remove)
{
    if(t < 0 || t >= SE_SCENE_TYPE_NUM)
        return NULL;
    _SceneList* sceneList = &mRemovedSceneListArray[t];
    _SceneList::iterator it;
    SE_Scene* ret = NULL;
    for(it = sceneList->begin() ; it != sceneList->end() ; it++)
    {
        if((*it)->getName() == sceneName)
        {
            ret = *it;
            break;
        }
    }
    if(remove)
    {
        _RemoveScene rs;
        rs.name = sceneName;
        sceneList->remove_if(rs);
    }
    return ret;
}
void SE_SceneManager::clear()
{
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
    {
        _SceneList* sceneList = &mSceneListArray[i];
        _SceneList* removeList = &mRemovedSceneListArray[i];
        for_each(sceneList->begin(), sceneList->end(), SE_DeleteObject());
        for_each(removeList->begin(), removeList->end(), SE_DeleteObject());
    }
}
SE_Scene* SE_SceneManager::getTopScene()
{
    _SceneList* sceneList = &mSceneListArray[SE_POST_EFFECT_SCENE];
    _SceneList::reverse_iterator it;
    for(it = sceneList->rbegin() ; it != sceneList->rend() ; it++)
    {
        SE_Scene* scene = *it;
        if(scene->getVisibility() == SE_VISIBLE && scene->canHandleInput())
        {
            return *it;
        }
    }
    sceneList = &mSceneListArray[SE_FRAMEBUFFER_SCENE];
    for(it = sceneList->rbegin() ; it != sceneList->rend() ; it++)
    {
        SE_Scene* scene = *it;
        if(scene->getVisibility() == SE_VISIBLE && scene->canHandleInput())
        {
            return *it;
        }
    }
    return NULL;
}
void SE_SceneManager::setMainScene(const SE_StringID& name)
{
    SE_Scene* scene = getScene(SE_FRAMEBUFFER_SCENE, name);
    if(scene)
    {
        mMainScene = scene;
    }
}

SE_Spatial* SE_SceneManager::findSpatialByName(const char *spatialName,int index)
{
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; ++i)
    {
        _SceneList* sceneList = &mSceneListArray[i];
        _SceneList::iterator it;
        for(it = sceneList->begin() ; it != sceneList->end() ; it++)
        {
            SE_Scene* scene = *it;
            SE_Spatial* s = scene->findSpatialByName(spatialName,index);
            if(s != NULL)
                return s;
        }
    }
    return NULL;
}
void SE_SceneManager::setSelectedSpatial(SE_Spatial* spatial)
{	
    SE_Scene* scene = spatial->getScene();
    if(scene)
    {
        scene->setSelectedSpatial(spatial);
    }
}
SE_Spatial* SE_SceneManager::getSelectedSpatial() const
{
    SE_Scene* scene = getMainScene();
    if(scene)
    {
        return scene->getSelectedSpatial();
    }
    return NULL;
}

int SE_SceneManager::findMaxSpatialLayer() 
{
    int maxlayer = 0;
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; ++i)
    {
        _SceneList* sceneList = &mSceneListArray[i];
        _SceneList::iterator it;
        for(it = sceneList->begin() ; it != sceneList->end() ; it++)
        {
            SE_Scene* scene = *it;
            int max = scene->findMaxSpatialLayer();
            
            if(max > maxlayer)
            {
                maxlayer = max;
            }
        }
    }

    return maxlayer;
}

void SE_SceneManager::inflate()
{
    for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; ++i)
    {
        _SceneList* sceneList = &mSceneListArray[i];
        _SceneList::iterator it;
        for(it = sceneList->begin() ; it != sceneList->end() ; it++)
        {
            SE_Scene* scene = *it;
            scene->inflate();
        }
    }
}
