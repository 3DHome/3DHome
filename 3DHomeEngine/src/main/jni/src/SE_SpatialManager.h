#ifndef SE_SPATIALMANAGER_H
#define SE_SPATIALMANAGER_H

class SE_ENTRY SE_SpatialManager
{
public:
    SE_SpatialManager();
    ~SE_SpatialManager();
    void set(const char* spatialName, SE_Spatial* spatial);    
    SE_Spatial* findByName(const char* spatialName, int index = 0); 
    void removeKey(const char* spatialName);

    void insertMirrorGenerator(const char* spatialName,SE_Spatial* spatial);
    void insertShadowGenerator(const char* spatialName,SE_Spatial* spatial);
    void removeMirrorGenerator(const char* spatialName);
    void removeShadowGenerator(const char* spatialName);
    void removeAllMirrorGenerator();
    void removeAllShadowGenerator();
    
    int mirrorGeneratorCount()
    {
        return mMirrorGeneratorCount.size();
    }

    int shadowGeneratorCount()
    {
        return mShadowGeneratorCount.size();
    }

    void clearSpatialStatus(const char* spatialName);

    void shadowCreatorTransformed()
    {
        ++mTransformedShadowCreatorNum;
    }

    void mirrorCreatorTransformed()
    {
        ++mTransformedMirrorCreatorNum;
    }

    void clearRenderTargetCreatorTransform()
    {
        mTransformedShadowCreatorNum = 0;
        mTransformedMirrorCreatorNum = 0;
    }


    bool isNeedUpdateShadowMap()
    {
        return mTransformedShadowCreatorNum > 0 ? true : false; 
    }

    bool isNeedUpdateMirrorMap()
    {
        return mTransformedMirrorCreatorNum > 0 ? true : false; 
    }


    void mirrorRenderChange(SE_Spatial* mirrorRender);

private:
    void remove(const char* spatialName);
    
private:
    typedef std::map<std::string,SE_Spatial*> SpatialManager;
    SpatialManager mSpatialManager;

    SpatialManager mMirrorGeneratorCount;
    SpatialManager mShadowGeneratorCount;

    int mTransformedShadowCreatorNum;
    int mTransformedMirrorCreatorNum;


};
#endif