#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Vector.h"
#include<vector>
#include "SE_Light.h"
#include "SE_Spatial.h"
#include "SE_Buffer.h"
#include "SE_BoundingVolume.h"
#include "SE_RenderManager.h"
#include "SE_Log.h"
#include "SE_SimObject.h"
#include "SE_Mesh.h"
#include "SE_Common.h"
#include "SE_DataValueDefine.h"
#include "SE_MemLeakDetector.h"
#include "SE_Application.h"
#include "SE_ObjectManager.h"
 
#include "SE_SceneManager.h"

IMPLEMENT_OBJECT(SE_Spatial)
SE_Spatial::SE_Spatial(SE_Spatial* parent)
{
    mWorldTransform.identity();
    mPrevMatrix.identity();
    mPostMatrix.identity();
    mLocalTranslate.setZero();
    mLocalScale.set(1.0f, 1.0f, 1.0f);
    mLocalRotate.identity();
    mUserTranslate.setZero();
    mUserScale.set(1.0, 1.0, 1.0);
    mUserRotate.identity();
    mWorldBoundingVolume = NULL;
    mParent = parent;
    mLeader = NULL;
    
    mBVType = 0;
    setSpatialStateAttribute(SE_SpatialAttribute::MOVABLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::COLLISIONABLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::TOUCHABLE,true);
	mRQ = SE_RenderManager::RQ0;
    mAttachedSimObject = NULL;
    mOriTransformHasSave = false;
    mRenderType = DEFAULT_RENDERER;
    mShaderType = DEFAULT_SHADER;
    mTransformHasChanged = true;
    mBoundingHasChanged = false;
    mAddOctree = NO_ADD;
    mHasInflate = false;
    
    mChildrenHasTransform = true;
    mImplFollow = NULL;

    mLineWidth = 1.0;
    mCloneIndex = 0;
    mIsEntirety = false;

    mIsGeometry = false;

    mBlackWhiteColorSwitcher.set(0.299,0.587,0.114);

    this->setSpatialRuntimeAttribute(SE_SpatialAttribute::BELONGTONONE,true);
    mCBFLoader = NULL;
}
SE_Spatial::SE_Spatial(SE_SpatialID spatialID, SE_Spatial* parent)
{
    mWorldTransform.identity();
    mPrevMatrix.identity();
    mPostMatrix.identity();
    mLocalTranslate.setZero();
    mLocalScale.set(1.0f, 1.0f, 1.0f);
    mLocalRotate.identity();
    mUserTranslate.setZero();
    mUserScale.set(1.0, 1.0, 1.0);
    mUserRotate.identity();
    mWorldBoundingVolume = NULL;
    mParent = parent;
    mLeader = NULL;    
    
    mBVType = 0;
    setSpatialStateAttribute(SE_SpatialAttribute::MOVABLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::COLLISIONABLE,true);
    setSpatialStateAttribute(SE_SpatialAttribute::TOUCHABLE,true);
    mRQ = SE_RenderManager::RQ0;
    mAttachedSimObject = NULL;
    mOriTransformHasSave = false;
    mRenderType = DEFAULT_RENDERER;
    mShaderType = DEFAULT_SHADER;
    mTransformHasChanged = true;
    mBoundingHasChanged = false;
    mAddOctree = NO_ADD;

    mChildrenHasTransform = true;
    mImplFollow = NULL;

    mLineWidth = 1.0;
    mCloneIndex = 0;

    mIsGeometry = false;
    mBlackWhiteColorSwitcher.set(0.299,0.587,0.114);

    this->setSpatialRuntimeAttribute(SE_SpatialAttribute::BELONGTONONE,true);
    mCBFLoader = NULL;
}
SE_Spatial::~SE_Spatial() 
{
    if(mWorldBoundingVolume)
        delete mWorldBoundingVolume;
    if(mCBFLoader) {
    	delete mCBFLoader;
    }

    /*SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
    sm->removeKey(mSpatialData.mSpatialName.c_str());

    if(this->isNeedRenderMirror())
    {
        sm->mirrorRenderChange(this);
    }*/

	/*
	for(int i = 0 ; i < RENDERSTATE_NUM ; i++)
	{
		_RenderStateProperty* p = &mRenderState[i];
		if(p->renderData)
		{
			p->renderData->dec();
			if(p->renderData->getNum() == 0)
				delete p->renderData;
		}
	}
	*/
    clearFollowers();
    delete mImplFollow;
}
SPATIAL_TYPE SE_Spatial::getSpatialType()
{
	return NONE;
}
void SE_Spatial::updateWorldTransform()
{
    //updateWorldScale();
    //updateWorldRotate();
    //updateWorldTranslate();
    if(mParent)
    {
        SE_Matrix4f parentM = mParent->getWorldTransform();
        SE_Matrix4f localM;
        localM.set(mLocalRotate.toMatrix3f(), mLocalScale, mLocalTranslate);
        if (mTransformHasChanged) 
        {
            SE_Matrix3f identity;
            identity.identity();
	        mPrevMatrix.set(identity, mUserTranslate);
            mPostMatrix.set(mUserRotate.toMatrix3f(), mUserScale, SE_Vector3f(0,0,0));
        }
        localM = mPrevMatrix.mul(localM).mul(mPostMatrix);

        if (mLeader)
        {
            SE_Matrix4f fLocalM;
            fLocalM.set(mLeader->getLocalRotateMatrix(), mLeader->getLocalScale(), mLeader->getLocalTranslate());
            SE_Matrix4f fWorldTransform = mLeader->getWorldTransform();
            mWorldTransform = fWorldTransform.mul(fLocalM.inverse()).mul(localM);
        }
        else
        {
        mWorldTransform = parentM.mul(localM);
        }

        if(!mOriTransformHasSave)
        {
            mOriginalWorldTransform = mWorldTransform;
            mOriTransformHasSave = true;
        }
    }
    else
    {
        SE_Matrix4f localM;
        localM.set(mLocalRotate.toMatrix3f(), mLocalScale, mLocalTranslate);
        if (mTransformHasChanged) 
        {
            SE_Matrix3f identity;
            identity.identity();
	    mPrevMatrix.set(identity, mUserTranslate);
            mPostMatrix.set(mUserRotate.toMatrix3f(), mUserScale, SE_Vector3f(0,0,0));
        }
        
        if (mLeader)
        {
            SE_Matrix4f fLocalM;
            fLocalM.set(mLeader->getLocalRotateMatrix(), mLeader->getLocalScale(), mLeader->getLocalTranslate());
            SE_Matrix4f fWorldTransform = mLeader->getWorldTransform();
            mWorldTransform = fWorldTransform.mul(fLocalM.inverse()).mul(localM);
        }
        else
        {
        mWorldTransform = mPrevMatrix.mul(localM).mul(mPostMatrix);
        }

        if(!mOriTransformHasSave)
        {
            mOriginalWorldTransform = mWorldTransform;
            mOriTransformHasSave = true;
        }
        //mWorldTransform.set(mWorldRotate.toMatrix3f(), mWorldScale, mWorldTranslate);
    }
    updateFollower();
}
void SE_Spatial::updateBoundingVolume()
{

}
void SE_Spatial::setRenderStateSource(RENDER_STATE_TYPE type, RENDER_STATE_SOURCE rsSource)
{
    if(type < 0 || type >= RENDERSTATE_NUM)
		return;
	mRenderState[type].renderSource = rsSource;
}
void SE_Spatial::setRenderState(RENDER_STATE_TYPE type, SE_RenderState* rs)
{
	if(type < 0 || type >= RENDERSTATE_NUM)
		return;
	_RenderStateProperty* p = &mRenderState[type];
	/*
	SE_Wrapper<_RenderStateData>* pRenderStateData = p->renderData;
	if(pRenderStateData)
	{
		pRenderStateData->dec();
		if(pRenderStateData->getNum() == 0)
			delete pRenderStateData;
	}
	_RenderStateData* rsd = new _RenderStateData;
	rsd->own = own;
	rsd->renderState = rs;
	pRenderStateData = new SE_Wrapper<_RenderStateData>(rsd, SE_Wrapper<_RenderStateData>::NOT_ARRAY);
	*/
	p->renderState = rs;
	p->renderSource = SELF_OWN;
}
SE_RenderState* SE_Spatial::getRenderState(RENDER_STATE_TYPE type)
{
    if(type < 0 || type >= RENDERSTATE_NUM)
		return NULL;
	_RenderStateProperty* p = &mRenderState[type];
	return p->renderState.get();
	/*
	SE_Wrapper<_RenderStateData>* renderData = p->renderData;
	if(!renderData)
		return NULL;
	return renderData->getPtr()->renderState;
	*/
}
void SE_Spatial::updateRenderState()
{
	if(!mParent)
		return;

	for(int i = 0 ; i < RENDERSTATE_NUM ; i++)
	{
		_RenderStateProperty* parentRenderProperty = &mParent->mRenderState[i];
		//SE_Wrapper<_RenderStateData>* parentRenderStateData = parentRenderProperty->renderData;
		_RenderStateProperty* pRenderProperty = &mRenderState[i];
		SE_RenderState* parentRenderState = parentRenderProperty->renderState.get();
		if(pRenderProperty->renderSource == INHERIT_PARENT && parentRenderState)
		{
			pRenderProperty->renderState = parentRenderState;
		}
        else if(pRenderProperty->renderSource == SELF_OWN)
        {
            //every spatial has own render state
            if((RENDER_STATE_TYPE)i == DEPTHTESTSTATE)
            {
                if(!getRenderState(DEPTHTESTSTATE))
                {
                SE_DepthTestState* rs = new SE_DepthTestState();
                rs->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_ENABLE);
                setRenderState(DEPTHTESTSTATE, rs);
            }
            }
            else if((RENDER_STATE_TYPE)i == BLENDSTATE)
            {
                if(!getRenderState(BLENDSTATE))
                {
                SE_BlendState *rs_blend = new SE_BlendState();
                rs_blend->setBlendProperty(SE_BlendState::BLEND_DISABLE);       
                setRenderState(BLENDSTATE,rs_blend);
            }
	}
        }

    }
    
}
const SE_Matrix4f& SE_Spatial::getWorldTransform()
{

    return mWorldTransform;
}

SE_Spatial* SE_Spatial::getParent() const
{
    return mParent;
}
SE_Spatial* SE_Spatial::setParent(SE_Spatial* parent)
{    
    mParent = parent;
    return mParent;
}
/*
SE_Vector3f SE_Spatial::getWorldTranslate()
{
    return mWorldTranslate;
}
SE_Quat SE_Spatial::getWorldRotate()
{
    return mWorldRotate;
}
SE_Matrix3f SE_Spatial::getWorldRotateMatrix()
{
    return mWorldRotate.toMatrix3f();
}
SE_Vector3f SE_Spatial::getWorldScale()
{
    return mWorldScale;
}
*/
void SE_Spatial::updateWorldLayer()
{
#if 0
    if(mParent)
    {
        SE_Layer parentLayer = *mParent->getWorldLayer();
        mWorldLayer = parentLayer + mLocalLayer;
    }
    else
    {
        mWorldLayer = mLocalLayer;
    }
#endif
}
/*
void SE_Spatial::updateWorldTranslate()
{
    if(mParent)
    {
        mWorldTranslate = mParent->localToWorld(mLocalTranslate);
    }
    else 
    {
        mWorldTranslate = mLocalTranslate;
    }
}
void SE_Spatial::updateWorldRotate()
{
    if(mParent)
    {
        mWorldRotate = mParent->getWorldRotate().mul(mLocalRotate);
    }
    else
    {
        mWorldRotate = mLocalRotate;
    }
}
void SE_Spatial::updateWorldScale()
{
    if(mParent)
    {
        mWorldScale = mParent->getWorldScale().mul(mLocalScale);
    }
    else
    {
        mWorldScale = mLocalScale;
    }
}
*/
SE_Vector3f SE_Spatial::localToWorld(const SE_Vector3f& v)
{
    /*
    SE_Vector3f scaledV = mWorldScale.mul(v);
    SE_Vector3f rotatedV = mWorldRotate.map(scaledV);
    SE_Vector3f translateV = mWorldTranslate.add(rotatedV);
    */
    SE_Vector4f v4(v.x, v.y, v.z, 0);
    v4 = mWorldTransform.map(v4);
    return v4.xyz();
}
SE_Vector3f SE_Spatial::worldToLocal(const SE_Vector3f& v)
{
    /*
    SE_Vector3f translatedV = v.subtract(mWorldTranslate);
    SE_Vector3f rotatedV = mWorldRotate.inverse().map(translatedV);
    SE_Vector3f inverseScale(1 / mWorldScale.x, 1 / mWorldScale.y, 1 / mWorldScale.z);
    SE_Vector3f scaledV = inverseScale.mul(rotatedV);
    */
    SE_Matrix4f inverseTransform = mWorldTransform.inverse();
    SE_Vector4f v4(v.x, v.y, v.z, 0);
    v4 = inverseTransform.map(v4);
    return v4.xyz();
}

SE_Vector3f SE_Spatial::getLocalTranslate()
{
    return mLocalTranslate;
}
SE_Matrix3f SE_Spatial::getLocalRotateMatrix()
{
    return mLocalRotate.toMatrix3f();
}
SE_Quat SE_Spatial::getLocalRotate()
{
    return mLocalRotate;
}
SE_Vector3f SE_Spatial::getLocalScale()
{
    return mLocalScale;
}

SE_Vector3f SE_Spatial::getUserTranslate()
{
    return mUserTranslate;
}
SE_Matrix3f SE_Spatial::getUserRotateMatrix()
{
    return mUserRotate.toMatrix3f();
}
SE_Quat SE_Spatial::getUserRotate()
{
    return mUserRotate;
}
SE_Vector3f SE_Spatial::getUserScale()
{
    return mUserScale;
}

void SE_Spatial::setLocalRotate(const SE_Matrix3f& rotate)
{

}

void SE_Spatial::setUserRotate(const SE_Matrix3f& rotate)
{

}
SE_Vector3f SE_Spatial::getCenter()
{
    if (mWorldBoundingVolume)
    {
        return mWorldBoundingVolume->getCenter();
    }
    return SE_Vector3f();
}
void SE_Spatial::setLocalTranslate(const SE_Vector3f& translate)
{
    mTransformHasChanged = true;
    mLocalTranslate = translate;

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setLocalRotate(const SE_Quat& rotate)
{
    mTransformHasChanged = true;
    mLocalRotate = rotate;

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setLocalScale(const SE_Vector3f& scale)
{
    mTransformHasChanged = true;
    mLocalScale = scale;

    //notice root update child
    setChildrenHasTransform(true);
}

void SE_Spatial::setUserTranslate(const SE_Vector3f& translate)
{
    mTransformHasChanged = true;
    mUserTranslate = translate;

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setUserRotate(const SE_Quat& rotate)
{
    mTransformHasChanged = true;
    mUserRotate = rotate;

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setUserScale(const SE_Vector3f& scale)
{
    mTransformHasChanged = true;
    mUserScale = scale;

    //notice root update child
    setChildrenHasTransform(true);
}

void SE_Spatial::addChild(SE_Spatial* child)
{}
void SE_Spatial::removeChild(SE_Spatial* child)
{}
void SE_Spatial::replaceChildParent(SE_Spatial* parent)
{}
void SE_Spatial::attachSimObject(SE_SimObject* go)
{}
void SE_Spatial::detachSimObject(SE_SimObject* go)
{}

SE_Spatial *SE_Spatial::clone(SE_SimObject *src, int index)
{
    return NULL;
}
SE_Spatial *SE_Spatial::clone(SE_Spatial* parent, int index,bool createNewMesh,const char* statuslist)
{
    return NULL;
}
void SE_Spatial::showAllNode()
{
    this->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
}

void SE_Spatial::hideAllNode()
{
    this->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,false);
}
void SE_Spatial::setAlpha(float alpha)
{
    mSpatialData.mAlpha = alpha;
}
float SE_Spatial::getAlpha()
{
    return mSpatialData.mAlpha;
}
int SE_Spatial::travel(SE_SpatialTravel* spatialTravel, bool tranvelAlways)
{
	return 0;
}
void SE_Spatial::read(SE_BufferInput& input)
{
    SE_SpatialID mSpatialID;
    mSpatialID.read(input);
    int tmp = input.readInt();
    mBVType = input.readInt();
    mLocalTranslate = input.readVector3f();
    mLocalScale = input.readVector3f();
    mLocalRotate = input.readQuat();

    mUserTranslate = input.readVector3f();
    mUserScale = input.readVector3f();
    mUserRotate = input.readQuat();

    mTransformHasChanged = true;
    //SE_Matrix3f identity;
    //identity.identity();
	//mPrevMatrix.set(identity, mUserTranslate);
    //mPostMatrix.set(mUserRotate.toMatrix3f(), mUserScale, SE_Vector3f(0,0,0));

    /*
	mPrevMatrix = input.readMatrix4f();
	mPostMatrix = input.readMatrix4f();
    */

    this->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
    mSpatialData.mSpatialName = input.readString();
    setSpatialRuntimeAttribute(SE_SpatialAttribute::SELECTED,false);
    setSpatialStateAttribute(SE_SpatialAttribute::TOUCHABLE,true);
    setSpatialRuntimeAttribute(SE_SpatialAttribute::NEEDDRAWLINE,true);
#if 0
    mSpatialData.mAlpha = input.readFloat();
    //mSpatialData = input.readVector4f();
#endif

}
void SE_Spatial::readEffect(SE_BufferInput& input)
{
}
void SE_Spatial::write(SE_BufferOutput& output)
{
    SE_SpatialID mSpatialID;
    mSpatialID.write(output);
    output.writeInt(mSpatialData.mSpatialState);
    output.writeInt(mBVType);
    output.writeVector3f(mLocalTranslate);
    output.writeVector3f(mLocalScale);
    output.writeQuat(mLocalRotate);

    output.writeVector3f(mUserTranslate);
    output.writeVector3f(mUserScale);
    output.writeQuat(mUserRotate);

    /*
	float m[16];
	mPrevMatrix.getSequence(m, 16);
	output.writeFloatArray(m, 16);
	mPostMatrix.getSequence(m, 16);
	output.writeFloatArray(m, 16);
    */

    //For get spatial directly
    if(mSpatialData.mSpatialName.empty())
    {
        output.writeString("No_Name_Spatial");
    }
    output.writeString(mSpatialData.mSpatialName.c_str());
   
}
void SE_Spatial::writeEffect(SE_BufferOutput& output)
{   
}
void SE_Spatial::renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType)
{}
void SE_Spatial::renderSpatialToTarget(RENDERTARGET_TYPE type,SE_Camera* camera, SE_RenderManager* renderManager,SE_RenderTarget* rt, SE_CULL_TYPE cullType,SE_Spatial* mirror)
{}
SE_Spatial* SE_Spatial::getRoot()
{
    SE_Spatial* parent = getParent();
    SE_Spatial* spatial = this;
    while(parent != NULL)
    {
        spatial = parent;
        parent = parent->getParent();
    }
    return spatial;
}
SE_Scene* SE_Spatial::getScene()
{
    SE_Spatial* spatial = getRoot();
    if(spatial)
    {
        return spatial->mScene;
    }
    else
        return NULL;
}
/*
void SE_Spatial::setRenderType(SE_RENDER_TYPE type) 
{
    mRenderType = type;
    std::string rendername;

    switch(type)
    {
    case (int)SE_DEFAULT_RENDER:
        rendername = "default_renderer";
        break;
    case (int)SE_FADEINOUT_RENDER:
        rendername = "fadeinout_renderer";
        break;
    case (int)SE_SKELETALANMATION_RENDER:
        rendername = "skeletalanimation_renderer";
        break;
    case (int)SE_SIMPLELIGHTING_RENDER:
        rendername = "simplelighting_renderer";
        break;
    case (int)SE_NORMALMAP_RENDER:
        rendername = "normalmap_renderer";
        break;
    case (int)SE_PARTICLE_RENDER:
        rendername = "particle_renderer";
        break;
    case (int)SE_COLOREXTRACT_RENDER:
        rendername = "colorextract_renderer";
        break;
    case (int)SE_COLOREFFECT_RENDER:
        rendername = "coloreffect_renderer";
        break;

    }
    if (mAttachedSimObject)
    {
        int num = mAttachedSimObject->getSurfaceNum();
        for(int i = 0; i < num; ++i)
        {
            mAttachedSimObject->getMesh()->getSurface(i)->setRendererID(rendername.c_str());
        }
    }
}

void SE_Spatial::setShaderType(SE_SHADER_TYPE type) 
{
    mShaderType = type;
    std::string shadername;

    switch(type)
    {
    case (int)SE_DEFAULT_SHADER:
        shadername = "default_shader";
        break;
    case (int)SE_FADEINOUT_SHADER:
        shadername = "fadeinout_shader";
        break;
    case (int)SE_SKELETALANMATION_SHADER:
        shadername = "skeletalanimation_shader";
        break;
    case (int)SE_SIMPLELIGHTING_SHADER:
        shadername = "simplelighting_shader";
        break;
    case (int)SE_NORMALMAP_SHADER:
        shadername = "normalmap_shader";
        break;
    case (int)SE_PARTICLE_SHADER:
        shadername = "particle_shader";
        break;
    case (int)SE_COLOREXTRACT_SHADER:
        shadername = "colorextract_shader";
        break;
    case (int)SE_COLOREFFECT_SHADER:
        shadername = "coloreffect_shader";
        break;

    }
    if (mAttachedSimObject)
    {
         int num = mAttachedSimObject->getSurfaceNum();
         for(int i = 0; i < num; ++i)
         {
             mAttachedSimObject->getMesh()->getSurface(i)->setProgramDataID(shadername.c_str());
         }
    }
}
*/
void SE_Spatial::setShaderType(const char* shaderType)
{
    mShaderType = shaderType;
    if (mAttachedSimObject)
    {
        int num = mAttachedSimObject->getSurfaceNum();
        for(int i = 0; i < num; ++i)
        {
            mAttachedSimObject->getMesh()->getSurface(i)->setProgramDataID(shaderType);
        }
    }
}

void SE_Spatial::setRenderType(const char* renderType)
{
    mRenderType = renderType;
    if (mAttachedSimObject)
    {
        int num = mAttachedSimObject->getSurfaceNum();
        for(int i = 0; i < num; ++i)
        {
            mAttachedSimObject->getMesh()->getSurface(i)->setRendererID(renderType);
        }
    }
}

void SE_Spatial::setUserTranslateIncremental(const SE_Vector3f& translate)
{
    mTransformHasChanged = true;
    mUserTranslate = mUserTranslate.add(translate);

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setUserRotateIncremental(const SE_Quat& rotate)
{
    mTransformHasChanged = true;
    mUserRotate = mUserRotate.mul(rotate);

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setUserRotateIncremental(const SE_Matrix3f& rotate)
{    
}
void SE_Spatial::setUserScaleIncremental(const SE_Vector3f& scale)
{
    mTransformHasChanged = true;
    mUserScale = mUserScale.add(scale);

    //notice root update child
    setChildrenHasTransform(true);
}

void SE_Spatial::setLocalTranslateIncremental(const SE_Vector3f& translate)
{
    mTransformHasChanged = true;
    mLocalTranslate = mLocalTranslate.add(translate);

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setLocalRotateIncremental(const SE_Quat& rotate)
{
    mTransformHasChanged = true;
    mLocalRotate = mLocalRotate.mul(rotate);

    //notice root update child
    setChildrenHasTransform(true);
}
void SE_Spatial::setLocalRotateIncremental(const SE_Matrix3f& rotate)
{    
}
void SE_Spatial::setLocalScaleIncremental(const SE_Vector3f& scale)
{
    mTransformHasChanged = true;
    mLocalScale = mLocalScale.mul(scale);

    //notice root update child
    setChildrenHasTransform(true);
}


void SE_Spatial::changeRenderState(int type,bool enable)
{   
    _RenderStateProperty* p = &mRenderState[type];

    if(type == DEPTHTESTSTATE)
    {

        SE_DepthTestState* rs = (SE_DepthTestState*)p->renderState.get();

        if(!rs)
        {
            return;
        }

        if(enable)
        {
            rs->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_ENABLE);
        }
        else
        {
            rs->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
        }
    }
    else if(type == BLENDSTATE)
    {   
        SE_BlendState* rs = (SE_BlendState*)p->renderState.get();

        if(!rs)
        {
            return;
        }

        if(enable)
        {
            rs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
                SE_SimObject* obj = this->getCurrentAttachedSimObj();
                if(!obj)
                {
                    return;
                }
                int num = obj->getSurfaceNum();
                for(int i = 0; i < num; ++i)
                {
                    obj->getMesh()->getSurface(i)->setNeedBlend(enable);
                }
        }
        else
        {
            rs->setBlendProperty(SE_BlendState::BLEND_DISABLE);
                SE_SimObject* obj = this->getCurrentAttachedSimObj();
                if(!obj)
                {
                    return;
                }
                int num = obj->getSurfaceNum();
                for(int i = 0; i < num; ++i)
                {
                    obj->getMesh()->getSurface(i)->setNeedBlend(enable);
                }
        }
    }
}

//bool SE_Spatial::isEnableDepth()
//{   
//    _RenderStateProperty* p = &mRenderState[DEPTHTESTSTATE];
//    SE_DepthTestState* rs = (SE_DepthTestState*)p->renderState.get();
//
//    if(!rs)
//    {
//        return false;
//    }
//
//    if(rs->getDepthTestProperty() == SE_DepthTestState::DEPTHTEST_ENABLE)
//    {
//        return true;
//    }
//
//    return false;
//}

//bool SE_Spatial::isEnableBlend()
//{   
//    _RenderStateProperty* p = &mRenderState[BLENDSTATE];
//    SE_BlendState* rs = (SE_BlendState*)p->renderState.get();
//
//    if(!rs)
//    {
//        return false;
//    }
//
//    if(rs->getBlendProperty() == SE_BlendState::BLEND_ENABLE)
//    {
//        return true;
//    }
//    return false;
//}

void SE_Spatial::setUseStencil(bool useStencil,const char* mirrorName,SE_MirrorPlane mirrorPlane,float translateAlignedAxis)
{
    
}

void SE_Spatial::setChangeStencilPermit(bool hasChangePermit)
{
    
}

void SE_Spatial::setUserColor(SE_Vector3f color)
{
    mSpatialData.mUserColor.set(color,0.0);
}

void SE_Spatial::setUseUserColor(int use)
{
    mSpatialData.mUserColor.w = use;
}

void SE_Spatial::updateUsetRenderState()
{
    if(isSpatialEffectHasAttribute(SE_SpatialAttribute::BLENDABLE))
    {
        changeRenderState(1,true);
    }
}

void SE_Spatial::setNeedGenerateShadow(bool need)
{
    //save shadow status
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::SHADOWGENERATOR;
        
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::SHADOWGENERATOR);
        
    }
}
void SE_Spatial::setNeedRenderShadow(bool need)
{
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::SHADOWRENDER;
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::SHADOWRENDER);
    }
}
void SE_Spatial::setEnableCullFace(bool enable)
{
    if(enable)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::CULLFACE;
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::CULLFACE);
    }
}
bool SE_Spatial::isEnableCullFace()
{
    return ((mSpatialData.mEffectState & SE_SpatialAttribute::CULLFACE) == SE_SpatialAttribute::CULLFACE);
}

void SE_Spatial::setNeedGenerateMirror(bool need)
{
    //save mirror status
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::MIRRORGENERATOR;        
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::MIRRORGENERATOR);        
    }
}
void SE_Spatial::setNeedRenderMirror(bool need)
{
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::MIRRORRENDER;
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::MIRRORRENDER);
    }
}
void SE_Spatial::setMirrorInfo(const char *mirrorName, SE_MirrorPlane mirrorPlane)
{}
void SE_Spatial::setNeedLighting(bool need)
{
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::LIGHTING;
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::LIGHTING);
    }
}
#if 0
void SE_Spatial::addLight(SE_Light *light, SE_Light::LightType type)
{
    if(!mAttachedSimObject)
    {
        return;
    }

    int num = mAttachedSimObject->getMesh()->getSurfaceNum();
    for(int i = 0; i < num; ++i)
    {
        mAttachedSimObject->getMesh()->getSurface(i)->addLight(light,type);
    }
}
#endif

void SE_Spatial::removeLight(const char* lightName)
{ 
    std::string ln = lightName;
    std::vector<std::string>::iterator it = mSpatialData.mLightsNameList.begin();

    for(;it != mSpatialData.mLightsNameList.end();it++)
    {
        if(ln.compare(*it) == 0)
        {
            mSpatialData.mLightsNameList.erase(it);
            break;
        }
        
    }

    if(mSpatialData.mLightsNameList.size() == 0)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("This spatial has no light,disable lighting!\n\n");
        this->setNeedLighting(false);
    }
}
void SE_Spatial::removeAllLight()
{    
    mSpatialData.mLightsNameList.clear();
    this->setNeedLighting(false);
}

void SE_Spatial::inflate()
{}

void SE_Spatial::getLightsNameList(std::vector<std::string> &list)
{
    list = mSpatialData.mLightsNameList;
}

void SE_Spatial::addLightNameToList(const char* lightname)
{
    std::string name = lightname;

    std::vector<std::string>::iterator it = mSpatialData.mLightsNameList.begin();

    for(;it != mSpatialData.mLightsNameList.end();it++)
    {
        if(name.compare(*it) == 0)
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("The light[%s] has been in scene,do not excute addLightNameToList!\n\n",lightname);
            return;
        }
    }
    mSpatialData.mLightsNameList.push_back(name);  
}
void SE_Spatial::clearSpatialStatus()
{}
SE_SimObject* SE_Spatial::getSimpleObject()
{
    return NULL;
}
void SE_Spatial::forceUpdateBoundingVolume()
{}
void SE_Spatial::autoUpdateBoundingVolume()
{}
void SE_Spatial::setNeedUVAnimation(bool need)
{
    if(need)
    {
        mSpatialData.mEffectState |= SE_SpatialAttribute::UVANIMATION;
    }
    else
    {
        mSpatialData.mEffectState &= (~SE_SpatialAttribute::UVANIMATION);
    }
}

void SE_Spatial::setTexCoordOffset(SE_Vector2f offet)
{
    mTexCoordOffset = offet;
}

SE_Vector2f SE_Spatial::getTexCoordOffset()
{
    return mTexCoordOffset;
}

void SE_Spatial::setShadowObjectVisibility(bool v)
{
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWOBJECT))
    {
        this->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,v);
    }
}
SE_Spatial* SE_Spatial::getLeader() const
{
    return mLeader;
}
SE_Spatial* SE_Spatial::setLeader(SE_Spatial* leader)
{    
    mLeader = leader;
    return mLeader;
}
void SE_Spatial::addFollower(SE_Spatial* follower)
{
    if (!mImplFollow)
    {
        mImplFollow = new SE_Spatial::_ImplFollow;
    }
    std::list<SE_Spatial*>::iterator it = mImplFollow->followers.begin();
    for(; it != mImplFollow->followers.end() ; it++)
    {
        SE_Spatial* s = *it;
	    if(s == follower)
	        return;
    }
    mImplFollow->followers.push_back(follower);
}
void SE_Spatial::removeFollower(SE_Spatial* follower)
{
    if (mImplFollow)
    {
        mImplFollow->followers.remove(follower);
    }
}
void SE_Spatial::updateFollower()
{
    if (mImplFollow)
    {
        std::list<SE_Spatial*>::iterator it = mImplFollow->followers.begin();
        for(; it != mImplFollow->followers.end() ; it++)
        {
            SE_Spatial* s = *it;
            s->updateWorldTransform();
        }
    }
}
void SE_Spatial::clearFollowers()
{
    if (mImplFollow)
    {
        std::list<SE_Spatial*>::iterator it = mImplFollow->followers.begin();
        for(; it != mImplFollow->followers.end() ; it++)
        {
            SE_Spatial* s = *it;
            s->setLeader(NULL);
        }
    }
}

void SE_Spatial::setTexCoordXYReverse(bool x,bool y)
{
    if(x)
    {
        mTexCoordReverse.x = 1.0;
    }
    else
    {
        mTexCoordReverse.x = 0.0;
    }

    if(y)
    {
        mTexCoordReverse.y = 1.0;
    }
    else
    {
        mTexCoordReverse.y = 0.0;
    }
}

bool SE_Spatial::changeImageKey(const char *newKey)
{
    return false;
}

int SE_Spatial::getChildrenNum()
{
    return -1;
}

SE_Spatial* SE_Spatial::getChildByIndex(int index)
{
    return NULL;
}

std::string SE_Spatial::getChildrenStatus()
{
    return std::string();
}
void SE_Spatial::clearMirror()
{

}
void SE_Spatial::inflateFromXML(const char *effectString)
{}
void SE_Spatial::setSpatialEffectData(SE_SpatialEffectData *spdata)
{
    mSpatialData.mAlpha = spdata->mAlpha;
    mSpatialData.mEffectData = spdata->mEffectData;
    mSpatialData.mEffectState = spdata->mEffectState;
    mSpatialData.mSpatialName = spdata->mSpatialName;
    mSpatialData.mSpatialState = spdata->mSpatialState;
    mSpatialData.mLightsNameList = spdata->mLightsNameList;
    mSpatialData.mSpatialState = spdata->mSpatialState;

    mSpatialData.mShadowColorDensity = spdata->mShadowColorDensity;
    mSpatialData.mMirrorColorDesity = spdata->mMirrorColorDesity;

}

void SE_Spatial::deleteCBFLoader() {
    if (mCBFLoader) {
        delete mCBFLoader;
        mCBFLoader = NULL;
    }
}


