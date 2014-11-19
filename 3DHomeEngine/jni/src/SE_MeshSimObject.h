#ifndef SE_MESHSIMOBJECT_H
#define SE_MESHSIMOBJECT_H
#include "SE_SimObject.h"
#include "SE_ID.h"
#include "SE_Common.h"
#include <vector>
class SE_BufferInput;
class SE_BufferOutput;
class SE_Mesh;
class SE_GeometryData;
class SE_Matrix4f;
class SE_RenderTarget;
class SE_ENTRY SE_MeshSimObject : public SE_SimObject
{
    DECLARE_OBJECT(SE_MeshSimObject)
public:
    SE_MeshSimObject(SE_Spatial* spatial = NULL);
    SE_MeshSimObject(const SE_MeshID& meshID);
    SE_MeshSimObject(SE_Mesh* mesh, SE_OWN_TYPE ownMesh);
    virtual ~SE_MeshSimObject();
    void read(SE_BufferInput& input);
    void write(SE_BufferOutput& output);
    int getSurfaceNum();
    // the geometry information are in world coordinate space

    void getSurfaceFacet(int surfaceIndex, int*& facets, int& faceNum);
    void onClick();
    RenderUnitVector createRenderUnit();
    
    SE_RenderUnit* createWireRenderUnit();
    SE_Mesh* getMesh();
    void setMesh(SE_Mesh* mesh, SE_OWN_TYPE own);
    void setMeshID(const SE_MeshID meshid)
    {
        mMeshID = meshid;
    }
    virtual SE_SimObject *clone(int index,bool createNewMesh = false);
    virtual bool replaceImage(const char* filePath,int imaIndex = 0);  
    

    //inflate will generate mesh and other infomation,independent from object read().
    virtual void inflate();
    virtual RenderUnitVector createUnitForTarget(RENDERTARGET_TYPE type,SE_RenderTarget* rt = NULL);
private:
    virtual bool changeImageKey(const char* newKey);

protected:
    
private:
    SE_Mesh* mMesh;
    SE_MeshID mMeshID;
    SE_OWN_TYPE mOwnMesh;
    bool mSelected;
    std::string mSecondImageKey;
};
#endif
