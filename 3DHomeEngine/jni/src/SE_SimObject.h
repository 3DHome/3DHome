#ifndef SE_SIMOBJECT_H
#define SE_SIMOBJECT_H
#include "SE_Factory.h"
#include "SE_Object.h"
#include "SE_PropertySet.h"
#include "SE_Matrix.h"
#include "SE_Spatial.h"
#include "SE_Vector.h"
#include "SE_Quat.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include <vector>
#include <string>
class SE_RenderUnit;
class SE_BufferInput;
class SE_BufferOutput;
class SE_Vector3i;
class SE_Mesh;
class SE_RenderState;
class SE_RenderTarget;
class SE_ENTRY SE_SimObject : public SE_Object
{
    DECLARE_OBJECT(SE_SimObject)
public:
    typedef std::vector<SE_RenderUnit*> RenderUnitVector;
    SE_SimObject(SE_Spatial* parent = NULL);
    void setSpatial(SE_Spatial* parent)
    {
        mSpatial = parent;
    }
    SE_Spatial* getSpatial()
    {
        return mSpatial;
    }
    virtual ~SE_SimObject();
    virtual RenderUnitVector createRenderUnit();
    virtual SE_RenderUnit* createWireRenderUnit();
    virtual void doTransform(const SE_Matrix4f& m);
    virtual void doTransform(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    virtual void read(SE_BufferInput& input);
    virtual void write(SE_BufferOutput& output);
    
    virtual int getSurfaceNum();
    virtual void getSurfaceFacet(int surfaceIndex, int*& facets, int& faceNum);
    virtual void onClick();
    virtual SE_Mesh* getMesh();
    virtual void setMesh(SE_Mesh*, SE_OWN_TYPE own);
    virtual void setAlpha(float alpha);
    virtual SE_SimObject *clone(int index,bool createNewMesh = false);    
    const char* getPrefixName();
    const char* getSuffixName();
    virtual bool replaceImage(const char* filePath,int imaIndex = 0);
    virtual void inflate();
    virtual RenderUnitVector createUnitForTarget(RENDERTARGET_TYPE type,SE_RenderTarget* rt = NULL);
public:
    const char* getName()
    {
        return mName.c_str();
    }
    void setName(const char* name)
    {
        mName = name;
    }
    void setID(const SE_SimObjectID& simObjID)
    {
        mID = simObjID;
    }
    SE_SimObjectID getID()
    {
        return mID;
    }
    // SE_SimObject will own the state . it will delete the mState
    void setPropertySet(SE_PropertySet* pro)
    {
        if(mPropertySet)
            delete mPropertySet;
        mPropertySet =pro;
    }
    SE_PropertySet* getPropertySet()
    {
        return mPropertySet;
    }
    void setLocalMatrix(const SE_Matrix4f& localMatrix)
    {
        mLocalMatrix = localMatrix;
    }
    SE_Matrix4f getLocalMatrix()
    {
        return mLocalMatrix;
    }
    /*
    //this function is call by SE_Geometry. you can not invoke this function
    void setRenderState(RENDER_STATE_TYPE type, SE_RenderState* rs)
    {
        mRenderState[type] = rs;
    }
    SE_RenderState** getRenderState()
    {
        return mRenderState;
    }
    */
    //if world matrix is set , we will use this matrix be world transform
    //and will not use spatial's world transform . 
    void setWorldMatrix(const SE_Matrix4f& m)
    {
        mWorldMatrix = m;
    }
    SE_Matrix4f getWorldMatrix()
    {
        return mWorldMatrix;
    }
    void setUseWorldMatrix(bool v)
    {
        mUseWorldMatrix = v;
    }
    bool isUseWorldMatrix()
    {
        return mUseWorldMatrix;
    }
    SE_Vector3f localToWorld(const SE_Vector3f& v);
    void setPrimitiveType(SE_PRIMITIVE_TYPE ptype)
    {
        mPrimitiveType = ptype;
    }
    SE_PRIMITIVE_TYPE getPrimitiveType()
    {
        return mPrimitiveType;
    }
    SE_Vector3f getCenter() const 
    {
        return mCenter;
    }
    void setCenter(const SE_Vector3f& c)
    {
        mCenter = c;
    }
    bool hasBone()
    {
        return mHasBone;
    }

    void setBoneState(bool hasbone)
    {
        mHasBone = hasbone;
    }

    SE_BipedController * getBoneController()
    {
        return mBipControllerAP;
    }

    void setBoneController(SE_SkinJointController *bonecontroller)
    {
        mBipControllerAP = (SE_BipedController *)bonecontroller;
    }

    SE_SkeletonUnit * getSkeletonUnit()
    {
        return mSuAP;
    }

    void setSkeletonUnit(SE_SkeletonUnit *su)
    {
        mSuAP = su;
    }

    void setNeedRender(bool need)
    {
        mIsNeedRender = need;
    }

    bool isNeedRender()
    {
        return mIsNeedRender;
    }   

private:
    std::string mName;
    std::string mPrefixName;
    std::string mSuffixName;
    SE_SimObjectID mID;
    SE_Spatial* mSpatial;
    SE_PropertySet* mPropertySet;
    SE_Matrix4f mLocalMatrix;
    //renderstate save on spatial
    //SE_RenderState* mRenderState[RENDERSTATE_NUM];
    SE_Matrix4f mWorldMatrix;
    bool mUseWorldMatrix;
    SE_PRIMITIVE_TYPE mPrimitiveType;
    SE_Vector3f mCenter;
    bool mHasBone;
    SE_BipedController *mBipControllerAP;
    SE_SkeletonUnit *mSuAP;
    bool mIsNeedRender;
};
#endif
