#ifndef SE_SPATIAL_H
#define SE_SPATIAL_H
#include "SE_Matrix.h"
#include "SE_Vector.h"
#include "SE_Quat.h"
#include "SE_ID.h"
#include "SE_Layer.h"
#include "SE_Object.h"
#include "SE_RenderState.h"
#include "SE_RefBase.h"
#include "SE_Common.h"
#include "SE_Light.h"
#include <vector>
#include <list>
class SE_BoundingVolume;
class SE_Spatial;
class SE_SimObject;
class SE_BufferInput;
class SE_BufferOutput;
class SE_Camera;
class SE_RenderManager;
class SE_Scene;
class SE_Light;
class SE_RenderTarget;
class SE_ResourceManager;
class SE_ENTRY SE_SpatialTravel
{
public:
    virtual ~SE_SpatialTravel() {}
    virtual int visit(SE_Spatial* spatial) = 0;
    virtual int visit(SE_SimObject* simObject) = 0;
};

class SE_SpatialEffectData
{
public:
    SE_SpatialEffectData()
    {
        mAlpha = 1.0;
        mSpatialState = 0;
        mEffectState = 0;
        mSpatialRuntimeState = 0;
        mUserColor.set(1.0,1.0,1.0,0.0);
        mShadowColorDensity = 0.04;
        mMirrorColorDesity = 0.5;
    }

    unsigned int mEffectState;
    unsigned int mSpatialState;
    unsigned int mSpatialRuntimeState;
    float mAlpha;
    //shadow color should be from 0.001(black) to 0.1(white)
    float mShadowColorDensity;
    float mMirrorColorDesity;
    //user can found this spatial through scene manager by findspatialbyName function.
    std::string mSpatialName;
    SE_Vector4f mEffectData;//x = specularConcentrate, y = specularIntensity, z = needIgnore vertex buffer,w = reserve for ohter
    std::vector<std::string> mLightsNameList;
    SE_Vector4f mUserColor;
};

class SE_ENTRY SE_Spatial : public SE_Object
{
    DECLARE_OBJECT(SE_Spatial);
    friend class SE_Scene;
public:
    
    
    SE_Spatial(SE_Spatial* parent = NULL);
    SE_Spatial(SE_SpatialID spatialID, SE_Spatial* parent = NULL);
    virtual ~SE_Spatial();
    const SE_Matrix4f& getWorldTransform();
    SE_Spatial* getParent() const;
    SE_Spatial* setParent(SE_Spatial* parent);
    SE_Spatial* getLeader() const;
    SE_Spatial* setLeader(SE_Spatial* leader);
    //SE_Vector3f getWorldTranslate();
    //SE_Matrix3f getWorldRotateMatrix();
    //SE_Quat getWorldRotate();
    //SE_Vector3f getWorldScale();
    SE_Vector3f getLocalTranslate();
    SE_Matrix3f getLocalRotateMatrix();
    SE_Quat getLocalRotate();
    SE_Vector3f getLocalScale();

    SE_Vector3f getUserTranslate();
    SE_Matrix3f getUserRotateMatrix();
    SE_Quat getUserRotate();
    SE_Vector3f getUserScale();    
	void setSpatialName(const char* name)
	{
		mSpatialData.mSpatialName = name;
	}
	const char* getSpatialName()  const
	{
		return mSpatialData.mSpatialName.c_str();
	}
    virtual void setLocalTranslate(const SE_Vector3f& translate);
    virtual void setLocalRotate(const SE_Quat& rotate);
    virtual void setLocalRotate(const SE_Matrix3f& rotate);
    virtual void setLocalScale(const SE_Vector3f& scale);

    virtual void setLocalTranslateIncremental(const SE_Vector3f& translate);
    virtual void setLocalRotateIncremental(const SE_Quat& rotate);
    virtual void setLocalRotateIncremental(const SE_Matrix3f& rotate);
    virtual void setLocalScaleIncremental(const SE_Vector3f& scale);

    virtual void setUserTranslate(const SE_Vector3f& translate);
    virtual void setUserRotate(const SE_Quat& rotate);
    virtual void setUserRotate(const SE_Matrix3f& rotate);
    virtual void setUserScale(const SE_Vector3f& scale);

    virtual void setUserTranslateIncremental(const SE_Vector3f& translate);
    virtual void setUserRotateIncremental(const SE_Quat& rotate);
    virtual void setUserRotateIncremental(const SE_Matrix3f& rotate);
    virtual void setUserScaleIncremental(const SE_Vector3f& scale);

    SE_Vector3f localToWorld(const SE_Vector3f& v);
    SE_Vector3f worldToLocal(const SE_Vector3f& v);
    void setPrevMatrix(const SE_Matrix4f& m)
    {
        mPrevMatrix = m;
    }
    SE_Matrix4f getPrevMatrix()
    {
        return mPrevMatrix;
    }
    void setPostMatrix(const SE_Matrix4f& m)
    {
        mPostMatrix = m;
    } 
    SE_Matrix4f getPostMatrix()
    {
        return mPostMatrix;
    }
	SE_BoundingVolume* getWorldBoundingVolume()
	{
		return mWorldBoundingVolume;
	}
	void setWorldBoundingVolume(SE_BoundingVolume* bv)
	{
		mWorldBoundingVolume = bv;
	}
    int getBVType()
    {
        return mBVType;
    }
    void setBVType(int bvType)
    {
        mBVType = bvType;
    }
    bool isVisible()
    {
        return (mSpatialData.mSpatialState & SE_SpatialAttribute::VISIBLE) == SE_SpatialAttribute::VISIBLE;
    }
    virtual void setVisible(bool v)
    {
        if(v)
        {
            mSpatialData.mSpatialState |= SE_SpatialAttribute::VISIBLE;
        }
        else
        {
            mSpatialData.mSpatialState &= (~SE_SpatialAttribute::VISIBLE);
        }
    }

    virtual void setNeedDepthTest(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::DEPTHENABLE;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::DEPTHENABLE);
		}
    }

    bool isNeedDepthTest()
    {
        return ((mSpatialData.mEffectState & SE_SpatialAttribute::DEPTHENABLE) == SE_SpatialAttribute::DEPTHENABLE);
    }

	void setRenderQueue(int rq)
	{
		mRQ = rq;
	}
	int getRenderQueue()
	{
		return mRQ;
	}
    /*void setLocalLayer(const SE_Layer& layer)
    {
        mLocalLayer = layer;
    }
    SE_Layer getLocalLayer()
    {
        return mLocalLayer;
    }*/
    SE_Layer* getWorldLayer()
    {
        return &mWorldLayer;
    }

	void setAnimationID(const SE_AnimationID& animID)
	{
		mAnimationID = animID;
	}
	SE_AnimationID getAnimationID()
	{
		return mAnimationID;
	}
	void setRenderState(RENDER_STATE_TYPE type, SE_RenderState* rs);
	//dont use getRenderState to get some spatial's renderstate and then 
	// set it to the other spatial;
	SE_RenderState* getRenderState(RENDER_STATE_TYPE type);
	void setRenderStateSource(RENDER_STATE_TYPE type, RENDER_STATE_SOURCE rsSource);
    void setCurrentAttachedSimObj(SE_SimObject *obj)
    {
        mAttachedSimObject = obj;
    }

    SE_SimObject * getCurrentAttachedSimObj()
    {
        return mAttachedSimObject;
    }
    //return the scene contain current spatial
    SE_Scene* getScene();
    SE_Spatial* getRoot();
    SE_Matrix4f& getOriginalTransform()
    {
        return mOriginalWorldTransform;
    }
    bool isChangedTransform()
    {
        return mTransformHasChanged;
    }
    bool isChangedBounding()
    {
        return mBoundingHasChanged;
    }

    void setTransformChanged(bool changed)
    {
        mTransformHasChanged = changed;
    }

    void setBoundingChanged(bool changed)
    {
        mBoundingHasChanged = changed;
    }
    //bool isEnableDepth();
    //bool isEnableBlend();

    const char* getRenderType()
    {
        return mRenderType.c_str();
    }
    const char* getShaderType()
    {
        return mShaderType.c_str();
    }
    int getAddOctree()
    {
        return mAddOctree;
    }   
    void setAddOctree(int addOctree)
    {
         mAddOctree = (ADD_OCTREE)addOctree;
    }
    
    void setHasInflate(bool has)
    {
        mHasInflate = has;
    }

    bool isInflate()
    {
        return mHasInflate;
    }
    
    void setChildrenHasTransform(bool t)
    {
        mChildrenHasTransform = t;
        if(this->getParent())
        {
            this->getParent()->setChildrenHasTransform(mChildrenHasTransform);
        }
    }
    void clearChildrenHasTransform()
    {
        mChildrenHasTransform = false;
    }
    bool getChildrenHasTransform()
    {
        return mChildrenHasTransform;
    }
    
    bool isNeedParticle()
    {
        return ((mSpatialData.mEffectState & SE_SpatialAttribute::PARTICLE) == SE_SpatialAttribute::PARTICLE);
    }

    void setNeedSpecLight(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::NEEDSPECULAR;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::NEEDSPECULAR);
		}
        //mNeedSpecLight = need;
    }
    bool isNeedSpecLight()
    {
       return ((mSpatialData.mEffectState & SE_SpatialAttribute::NEEDSPECULAR) == SE_SpatialAttribute::NEEDSPECULAR);
    }

    void setNeedForeverBlend(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::FOREVERBLEND;
            setSpatialEffectAttribute(SE_SpatialAttribute::BLENDABLE,true);
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::FOREVERBLEND);
            setSpatialEffectAttribute(SE_SpatialAttribute::BLENDABLE,false);
		}
        //mNeedSpecLight = need;
    }
    bool isNeedForeverBlend()
    {
       return ((mSpatialData.mEffectState & SE_SpatialAttribute::FOREVERBLEND) == SE_SpatialAttribute::FOREVERBLEND);
    }

    void setNeedDrawLine(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::DRAWLINE;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::DRAWLINE);
		}        
    }
    bool isNeedDrawLine()
    {
       return ((mSpatialData.mEffectState & SE_SpatialAttribute::DRAWLINE) == SE_SpatialAttribute::DRAWLINE);
    }

    void setLineWidth(float width)
    {
        mLineWidth = width;
    }

    float getLineWidth()
    {
        return mLineWidth;
    }

    void setCloneIndex(int index)
    {
        mCloneIndex = index;
    }

    int getCloneIndex()
    {
        return mCloneIndex;
    }
    void setIsEntirety(bool isEntirety)
    {
        mIsEntirety = isEntirety;
    }

    bool isEntirety()
    {
        return mIsEntirety;
    }
    virtual void setSpatialEffectAttribute(SE_SpatialAttribute::SpatialEffect effect,bool enableOrDisable)
    {
        if(enableOrDisable)
        {
            mSpatialData.mEffectState |= effect;
        }
        else
        {
            mSpatialData.mEffectState &= (~effect);
        }
    }
    bool isSpatialEffectHasAttribute(SE_SpatialAttribute::SpatialEffect effect)
    {
        return ((mSpatialData.mEffectState & effect) == effect);
    }

    virtual void setSpatialStateAttribute(SE_SpatialAttribute::SpatialProperty spatialState,bool enableOrDisable)
    {
        if(enableOrDisable)
        {
            mSpatialData.mSpatialState |= spatialState;
        }
        else
        {
            mSpatialData.mSpatialState &= (~spatialState);
        }
    }
    bool isSpatialStateHasAttribute(SE_SpatialAttribute::SpatialProperty spatialState)
    {
        return ((mSpatialData.mSpatialState & spatialState) == spatialState);
    }

    virtual void setSpatialRuntimeAttribute(SE_SpatialAttribute::SpatialRuntimeStatus effect,bool enableOrDisable)
    {
        if(enableOrDisable)
        {
            mSpatialData.mSpatialRuntimeState |= effect;
        }
        else
        {
            mSpatialData.mSpatialRuntimeState &= (~effect);
        }
    }
    bool isSpatialHasRuntimeAttribute(SE_SpatialAttribute::SpatialRuntimeStatus effect)
    {
        return ((mSpatialData.mSpatialRuntimeState & effect) == effect);
    }
public:
    virtual SE_SimObject* getSimpleObject();
    virtual void addChild(SE_Spatial* child);
    virtual void removeChild(SE_Spatial* child);
    virtual void attachSimObject(SE_SimObject* go);
    virtual void detachSimObject(SE_SimObject* go);
    virtual int travel(SE_SpatialTravel* spatialTravel, bool travelAways);
    virtual void updateWorldTransform();
    virtual void updateBoundingVolume();
    virtual void updateWorldLayer();
	virtual void updateRenderState();
    virtual void write(SE_BufferOutput& output);
    virtual void writeEffect(SE_BufferOutput& output);
    virtual void showAllNode();
    virtual void hideAllNode();
    virtual void read(SE_BufferInput& input);
    virtual void readEffect(SE_BufferInput& input);
    virtual void renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType);

    virtual void renderSpatialToTarget(RENDERTARGET_TYPE type,SE_Camera* camera,SE_RenderManager* renderManager, SE_RenderTarget* rt, SE_CULL_TYPE cullType,SE_Spatial* mirror = NULL);

	virtual SPATIAL_TYPE getSpatialType();
    virtual void setAlpha(float alpha);
    virtual float getAlpha();
    virtual SE_Vector3f getCenter();
    virtual SE_Spatial *clone(SE_SimObject *src, int index);
    virtual SE_Spatial *clone(SE_Spatial* parent, int index,bool createNewMesh = false,const char* statuslist = NULL);
   // virtual void setRenderType(SE_RENDER_TYPE type);
   // virtual void setShaderType(SE_SHADER_TYPE type);

    virtual void setRenderType(const char* renderType);
    virtual void setShaderType(const char* shaderType);

    virtual void changeRenderState(int type,bool enable);
    virtual void setUseStencil(bool useStencil,const char* mirrorName,SE_MirrorPlane mirrorPlane = SE_XY,float translateAlignedAxis = 0.0);
    virtual void setChangeStencilPermit(bool hasChangePermit);

    virtual void setUserColor(SE_Vector3f color);
    virtual void setUseUserColor(int use);
    virtual SE_Vector3f getUserColor()
    {
        return mSpatialData.mUserColor.xyz();
    }
    virtual float isUseUserColor()
    {
        return mSpatialData.mUserColor.w;
    }

    virtual void replaceChildParent(SE_Spatial* parent);

    virtual void updateUsetRenderState();


    virtual void setEnableCullFace(bool enable);
    virtual bool isEnableCullFace();

    virtual void setNeedGenerateMirror(bool need);
    virtual void setNeedRenderMirror(bool need);
    virtual void setMirrorInfo(const char* mirrorName,SE_MirrorPlane mirrorPlane = SE_XY);
    virtual void setNeedGenerateShadow(bool need);
    virtual void setNeedRenderShadow(bool need);
    virtual void setNeedLighting(bool need);
    //virtual void addLight(SE_Light* light,SE_Light::LightType type);
    virtual void removeLight(const char* lightName);
    virtual void removeAllLight();
    virtual void inflate();
    virtual void getLightsNameList(std::vector<std::string> &list);
    virtual void addLightNameToList(const char* lightname);
    virtual SE_Spatial* getMirrorObject()
    {
        return NULL;
    }
    virtual SE_MirrorPlane getMirrorPlan()
    {
        return (SE_MirrorPlane)-1;
    }
    virtual void clearSpatialStatus();
    virtual void setNeedGenerateMipMap(bool need)
    {
    }

    
    virtual void forceUpdateBoundingVolume();
    virtual void autoUpdateBoundingVolume();

    virtual void setNeedUVAnimation(bool need);
    virtual void setTexCoordOffset(SE_Vector2f offet);
    virtual void setTexCoordXYReverse(bool x,bool y);
    SE_Vector2f getTexCoordXYReverse()
    {
        return  mTexCoordReverse;
    }
    virtual SE_Vector2f getTexCoordOffset();
    virtual void setNeedParticle(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::PARTICLE;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::PARTICLE);
		}
    }

    virtual void setNeedBlackWhiteColor(bool need)
    {
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::BLACKWHITE;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLACKWHITE);
		}
    }
    virtual bool isNeedBlackWhiteColor()
    {
       return ((mSpatialData.mEffectState & SE_SpatialAttribute::BLACKWHITE) == SE_SpatialAttribute::BLACKWHITE);
    }
    virtual void setSpatialState(unsigned int s)
    {
        mSpatialData.mSpatialState = s;
    }
    virtual unsigned int getSpatialState()
    {
        return mSpatialData.mSpatialState;
    }

    virtual void setEffectState(unsigned int s)
    {
        mSpatialData.mEffectState = s;
    }
    virtual unsigned int getEffectState()
    {
        return mSpatialData.mEffectState;
    }

    virtual void setShadowObjectVisibility(bool v);

    virtual void setEffectData(SE_Vector4f d)
    {
        mSpatialData.mEffectData = d;
    }
    virtual SE_Vector4f getEffectData()
    {
        return mSpatialData.mEffectData;
    }

    virtual void addFollower(SE_Spatial* follower);
    virtual void removeFollower(SE_Spatial* follower);
    virtual void updateFollower();
    virtual void clearFollowers();
    virtual bool changeImageKey(const char* newKey);
    virtual int getChildrenNum();
    virtual SE_Spatial* getChildByIndex(int index);

    virtual std::string getChildrenStatus();

    virtual void clearMirror();

    virtual bool isGeometry()
    {
        return mIsGeometry;
    }

    virtual void inflateFromXML(const char* effectString);

    SE_Vector3f getBlackWhiteColorSwitcher()
    {
        return mBlackWhiteColorSwitcher;
    }

    virtual void setNeedBlendSortOnX(bool need)
    {      
        clearBlendSortAxis();
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::BLENDX;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDX);
		}   
    }

    bool isNeedBlendSortOnX()
    {
        return ((mSpatialData.mEffectState & SE_SpatialAttribute::BLENDX) == SE_SpatialAttribute::BLENDX);
    }

    virtual void setNeedBlendSortOnY(bool need)
    {
        clearBlendSortAxis();
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::BLENDY;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDY);
		}   
    }

    bool isNeedBlendSortOnY()
    {
        return ((mSpatialData.mEffectState & SE_SpatialAttribute::BLENDY) == SE_SpatialAttribute::BLENDY);
    }

    virtual void setNeedBlendSortOnZ(bool need)
    {
        clearBlendSortAxis();
        if(need)
		{
            mSpatialData.mEffectState |= SE_SpatialAttribute::BLENDZ;
		}
		else
		{
			mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDZ);
		}   
    }

    bool isNeedBlendSortOnZ()
    {
        return ((mSpatialData.mEffectState & SE_SpatialAttribute::BLENDZ) == SE_SpatialAttribute::BLENDZ);
    }

    void setSpatialEffectData(SE_SpatialEffectData* spdata);


	SE_SpatialEffectData* getSpatialEffectData()
	{
	    return &mSpatialData;
	}

    virtual const char* getImageDataID(int index = 0)
    {
        return NULL;
    }

    
    float getShadowColorDensity()
    {
        return mSpatialData.mShadowColorDensity;
    }

    //shadow color should be from 0.001(black) to 0.1(white)
    virtual void setShadowColorDensity(float density)
    {
        mSpatialData.mShadowColorDensity = density;
    }

    float getMirrorColorDesity()
    {
        return mSpatialData.mMirrorColorDesity;
    }

    //shadow color should be from 0.001(black) to 0.1(white)
    virtual void setMirrorColorDesity(float density)
    {
        mSpatialData.mMirrorColorDesity = density;
    }

    void setCBFLoader(SE_ResourceManager* loader) {
    	mCBFLoader = loader;
    }

    SE_ResourceManager* getCBFLoader() {
    	return mCBFLoader;
    }

    void deleteCBFLoader();

    virtual bool hasSpotLight() {
        return false;
    }
protected:
    //void updateWorldTranslate();
    //void updateWorldRotate();
    //void updateWorldScale();
protected:
    SE_BoundingVolume* mWorldBoundingVolume;
    SE_Matrix4f mWorldTransform;
	std::string mGroupName;
    
    bool mBoundingHasChanged;

    struct _ImplFollow
    {
        std::list<SE_Spatial*> followers;
	~_ImplFollow()
	{
        followers.clear();
	}
    };
    _ImplFollow* mImplFollow;

    bool mIsGeometry;

    

private:
    void clearBlendSortAxis()
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDX);
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDY);
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::BLENDZ);
    }
        
private:
    bool mTransformHasChanged;
    bool mRemovable;
	
	struct _RenderStateProperty
	{
        //SE_Wrapper<_RenderStateData>* renderData;
		SE_sp<SE_RenderState> renderState;
		RENDER_STATE_SOURCE renderSource;
		_RenderStateProperty()
		{
			//renderData = NULL;
			renderSource = SELF_OWN;
		}
	};
    SE_Vector3f mLocalTranslate;
    SE_Vector3f mLocalScale;
    SE_Quat mLocalRotate;

    SE_Vector3f mWorldTranslate;
    SE_Vector3f mWorldScale;
    SE_Quat mWorldRotate;

    SE_Spatial* mParent;
    SE_Spatial* mLeader;
    
    
    
    int mBVType;
    //remove local layer next version
    //SE_Layer mLocalLayer;
    SE_Layer mWorldLayer;
	_RenderStateProperty mRenderState[RENDERSTATE_NUM];
    //SE_sp<SE_RenderState> mRenderState[RENDERSTATE_NUM];
    SE_Matrix4f mPrevMatrix;
    SE_Matrix4f mPostMatrix;
	SE_AnimationID mAnimationID;
	int mRQ;
    SE_SimObject *mAttachedSimObject;
    SE_Scene* mScene;

    

    SE_Matrix4f mOriginalWorldTransform;
    bool mOriTransformHasSave;

    //for init object position
    SE_Vector3f mUserScale;
    SE_Quat mUserRotate;
    SE_Vector3f mUserTranslate;

    std::string mRenderType;
    std::string mShaderType;

    ADD_OCTREE mAddOctree;

    bool mHasInflate;

    
    bool mChildrenHasTransform;
    SE_Vector2f mTexCoordOffset;

    
    float mLineWidth;

    SE_Vector2f mTexCoordReverse;

    int mCloneIndex;
    bool mIsEntirety;

    SE_Vector3f mBlackWhiteColorSwitcher;

    SE_SpatialEffectData mSpatialData;

    SE_ResourceManager *mCBFLoader;
};
#endif
