#ifndef SE_GEOMETRY_H
#define SE_GEOMETRY_H
#include "SE_Spatial.h"
#include <vector>
class SE_SimObject;
class SE_BufferOutput;
class SE_BufferInput;
class SE_ENTRY SE_Geometry : public SE_Spatial
{
    DECLARE_OBJECT(SE_Geometry)
public:
    SE_Geometry(SE_Spatial* parent = NULL);
    SE_Geometry(SE_SpatialID id, SE_Spatial* parent = NULL);
    virtual ~SE_Geometry();
    void attachSimObject(SE_SimObject* go);
    void detachSimObject(SE_SimObject* go);
    void updateWorldTransform();
	void updateRenderState();
    void updateBoundingVolume();
    int travel(SE_SpatialTravel* spatialTravel, bool travalAways);
    void renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType);

    virtual void renderSpatialToTarget(RENDERTARGET_TYPE type,SE_Camera* camera,SE_RenderManager* renderManager, SE_RenderTarget* rt, SE_CULL_TYPE cullType,SE_Spatial* mirror = NULL);

    virtual void write(SE_BufferOutput& output);
    virtual void writeEffect(SE_BufferOutput& output);
    virtual void read(SE_BufferInput& input);
    virtual void readEffect(SE_BufferInput& input);
	SPATIAL_TYPE getSpatialType();
    SE_Spatial* getMirrorObject()
    {
        return mirrorSpatial;
    }

    SE_MirrorPlane getMirrorPlan()
    {
        return mMirrorPlane;
    }
    bool isNeedGenerateMipMap()
    {
        return mGenerateMipMap;
    }

    virtual SE_Spatial *clone(SE_SimObject *src, int index);
    virtual SE_Spatial *clone(SE_Spatial* parent, int index,bool createNewMesh = false,const char* statuslist = NULL);    

    virtual void setMirrorInfo(const char* mirrorName,SE_MirrorPlane mirrorPlane = SE_XY);
    virtual void inflate();

    virtual void clearSpatialStatus();
    virtual void setNeedGenerateMipMap(bool need)
    {
        mGenerateMipMap = need;
    }
    virtual SE_SimObject* getSimpleObject();
    virtual void forceUpdateBoundingVolume();
    virtual void autoUpdateBoundingVolume();
    
    virtual void clearMirror();

    virtual void inflateFromXML(const char* effectString);

    virtual const char* getImageDataID(int index = 0);

    virtual bool hasSpotLight();
private:
    class _Impl
    {
    public:
        typedef std::list<SE_SimObject*> SimObjectList;
        SimObjectList attachObject;

        ~_Impl();

    };
    _Impl* mImpl;
    std::vector<SE_Light*> mLightsForInflate;
    
    std::string mMirrorInfo;
    SE_MirrorPlane mMirrorPlane;
    SE_Spatial *mirrorSpatial;
    bool mGenerateMipMap;
    SE_SimObject* mSimpleObject;

    virtual bool changeImageKey(const char* newKey);

};
#endif
