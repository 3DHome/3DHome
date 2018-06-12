#include "SE_DynamicLibType.h"
#include "SE_ObjectManager.h"
#include "SE_Spatial.h"
#include "SE_SpatialManager.h"
#include "SE_SceneManager.h"
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_MemLeakDetector.h"

SE_SpatialManager::SE_SpatialManager()
{
    mTransformedShadowCreatorNum = 0;
    mTransformedMirrorCreatorNum = 0;
}

SE_SpatialManager::~SE_SpatialManager()
{}

void SE_SpatialManager::set(const char *spatialName, SE_Spatial *spatial)
{
#if 0
    std::string name(spatialName);
    SE_Spatial* duplicate = findByName(spatialName);
    if(duplicate)
    {
        if(_DEBUG)
        LOGI("\n\nError!!!! duplicate spatial !!!!!!!!!\n\n\n");
        duplicate->getScene()->removeSpatial(duplicate);
        delete duplicate;
    }
    mSpatialManager.insert(std::pair<std::string,SE_Spatial*> (name,spatial));
#endif

}

void SE_SpatialManager::remove(const char *spatialName)
{
#if 0
    std::string name(spatialName);
    SpatialManager::iterator it = mSpatialManager.find(name);
    if(it != mSpatialManager.end())
    {
        delete it->second;
        mSpatialManager.erase(it);
    }
#endif
}

void SE_SpatialManager::removeKey(const char *spatialName)
{
#if 0
    std::string name(spatialName);
    SpatialManager::iterator it = mSpatialManager.find(name);
    if(it != mSpatialManager.end())
    {        
        clearSpatialStatus(spatialName);
        mSpatialManager.erase(it);
    }
#endif
}

SE_Spatial* SE_SpatialManager::findByName(const char* spatialName,int index)
{
#if 0
    std::string name(spatialName);
    SpatialManager::iterator it = mSpatialManager.find(name);
    if(it != mSpatialManager.end())
    {
        return it->second;
    }

    return NULL;
#endif
    SE_SceneManager* sm = SE_Application::getInstance()->getSceneManager();
    return sm->findSpatialByName(spatialName,index);
}

void SE_SpatialManager::insertMirrorGenerator(const char *spatialName, SE_Spatial *spatial)
{
    std::string name(spatialName);
    SpatialManager::iterator it = mMirrorGeneratorCount.find(name);
    if(it != mMirrorGeneratorCount.end())
    {
        if(_DEBUG)
        LOGI("insert mirror generator[%s] fail,the spatial has been in list.\n\n",spatialName);
        return;
    }
    mMirrorGeneratorCount.insert(std::pair<std::string,SE_Spatial*> (name,spatial));
    if(_DEBUG)
    LOGI("insert mirror generator[%s] success! there are [%d] mirror generators.\n\n",spatialName,mMirrorGeneratorCount.size());
}

void SE_SpatialManager::insertShadowGenerator(const char *spatialName, SE_Spatial *spatial)
{
    std::string name(spatialName);
    SpatialManager::iterator it = mShadowGeneratorCount.find(name);
    if(it != mShadowGeneratorCount.end())
    {
        if(_DEBUG)
        LOGI("insert shadow generator[%s] fail,the spatial has been in list.\n\n",spatialName);
        return;
    }
    mShadowGeneratorCount.insert(std::pair<std::string,SE_Spatial*> (name,spatial));
    if(_DEBUG)
    LOGI("insert shadow generator[%s] success! there are [%d] shadow generators.\n\n",spatialName,mShadowGeneratorCount.size());
}

void SE_SpatialManager::removeMirrorGenerator(const char *spatialName)
{
    std::string name(spatialName);
    SpatialManager::iterator it = mMirrorGeneratorCount.find(name);
    if(it != mMirrorGeneratorCount.end())
    {        
        mMirrorGeneratorCount.erase(it);
        if(_DEBUG)
        LOGI("remove mirror generator[%s] success! there are [%d] mirror generators.\n\n",spatialName,mMirrorGeneratorCount.size());
    }
}

void SE_SpatialManager::removeShadowGenerator(const char *spatialName)
{
    std::string name(spatialName);
    SpatialManager::iterator it = mShadowGeneratorCount.find(name);
    if(it != mShadowGeneratorCount.end())
    {
        mShadowGeneratorCount.erase(it);
        if(_DEBUG)
        LOGI("remove shadow generator[%s] success! there are [%d] shadow generators.\n\n",spatialName,mShadowGeneratorCount.size());
    }
}

void SE_SpatialManager::clearSpatialStatus(const char *spatialName)
{
    removeMirrorGenerator(spatialName);
    removeShadowGenerator(spatialName);
}

void SE_SpatialManager::removeAllMirrorGenerator()
{
    mMirrorGeneratorCount.clear();
}
void SE_SpatialManager::removeAllShadowGenerator()
{
    mShadowGeneratorCount.clear();
}
void SE_SpatialManager::mirrorRenderChange(SE_Spatial *mirrorRender)
{
    SpatialManager::iterator it = mMirrorGeneratorCount.begin();

    for(;it != mMirrorGeneratorCount.end();it++)
    {
        SE_Spatial* sp = it->second;
        if(sp->getMirrorObject() == mirrorRender)
        {
            it->second->setMirrorInfo("");
        }

    }
}