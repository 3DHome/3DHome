#include "SE_DynamicLibType.h"
#include "SE_Spatial.h"
#include "SE_UserCommand.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_SceneManager.h"

#ifndef ANDROID
#include "SE_Ase.h"
#endif
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Quat.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"

#include "SE_Log.h"
#include "SE_Primitive.h"
#include "SE_ImageCodec.h"
#include "SE_Geometry.h"
#include "SE_MeshSimObject.h"
#include "SE_DataValueDefine.h"
#include "SE_Mesh.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"
#include "SE_CommonNode.h"
#include "SE_Camera.h"
#include "SE_BipedAnimation.h"
#include "SE_AnimationManager.h"
#include "SE_RenderSystemCapabilities.h"
#include "SE_MemLeakDetector.h"
#include "SE_Utils.h"

#include "SE_Buffer.h"
#include "SE_ResFileHeader.h"
#include "SE_File.h"
#include <string>
#include <map>
#include "tinyxml.h"

class _saveChildrenEffectSpatialTravel : public SE_SpatialTravel
{
public:
    int visit(SE_Spatial* spatial)
    {
        if(spatial->isGeometry())
        {
            childrenEffect.push_back(spatial);
        }
	    return 0;
    }
    int visit(SE_SimObject* simObject)
    {        
        return 0;
    }
    std::list<SE_Spatial*> childrenEffect;

};

SE_RotateSpatialCommand::SE_RotateSpatialCommand(SE_Application* app) : SE_Command(app)
{
    mRotateAngle = 0.0;
}
SE_RotateSpatialCommand::~SE_RotateSpatialCommand()
{
}
void SE_RotateSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    


}

//
SE_ScaleSpatialCommand::SE_ScaleSpatialCommand(SE_Application* app) : SE_Command(app)
{
    mScaledX = 1.0;
    mScaledY = 1.0;
    mScaledZ = 1.0;
}
SE_ScaleSpatialCommand::~SE_ScaleSpatialCommand()
{
}
void SE_ScaleSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

     

}

//
SE_TranslateSpatialCommand::SE_TranslateSpatialCommand(SE_Application* app) : SE_Command(app)
{
    mTranslatedX = 0.0;
    mTranslatedY = 0.0;
    mTranslatedZ = 0.0;
}
SE_TranslateSpatialCommand::~SE_TranslateSpatialCommand()
{
}
void SE_TranslateSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    


}

SE_ResetSpatialCommand::SE_ResetSpatialCommand(SE_Application* app) : SE_Command(app)
{    
}
SE_ResetSpatialCommand::~SE_ResetSpatialCommand()
{
}
void SE_ResetSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
     
}

SE_RemoveSpatialCommand::SE_RemoveSpatialCommand(SE_Application* app) : SE_Command(app)
{
}
SE_RemoveSpatialCommand::~SE_RemoveSpatialCommand()
{
}
void SE_RemoveSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

}


SE_ReLoadSpatialCommand::SE_ReLoadSpatialCommand(SE_Application* app) : SE_Command(app)
{
}
SE_ReLoadSpatialCommand::~SE_ReLoadSpatialCommand()
{
}
void SE_ReLoadSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

}

SE_ReLoadAllSpatialCommand::SE_ReLoadAllSpatialCommand(SE_Application* app) : SE_Command(app)
{
}
SE_ReLoadAllSpatialCommand::~SE_ReLoadAllSpatialCommand()
{
}
void SE_ReLoadAllSpatialCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getMainScene()->getRoot();

    if(root)
    {
        root->showAllNode();
    }
    else
    {
        LOGI("$$ Reload fail, root node not exist !!\n");
        return;
    }
    
    LOGI("$$ AllSpatial Reload !!\n");
}

SE_AddUserObjectCommand::SE_AddUserObjectCommand(SE_Application* app) : SE_Command(app)
{
    mVertexArray = NULL;
    mVertexIndexArray = NULL;
    mFacetIndexArray = NULL;
    mTextureCoorArray = NULL;
    mTextureCoorIndexArray = NULL;
    mVertexNum = 0;
    mVertexIndexNum = 0;
    mFacetIndexNum = 0;
    mTextureCoorNum = 0;
    mTextureCoorIndexNum = 0;
    mNeedBlending = false;
    mNeedDepthTest = true;
    mLastLayerInWorld = false;
    mLayerIndex = 0;
    mAlpha = 1.0;
    mProgramDataID = DEFAULT_SHADER;
    mRendererID = DEFAULT_RENDERER;
    mLocalScale = SE_Vector3f(1, 1, 1);
    mLocalTranslate = SE_Vector3f(0, 0, 0);
    mLocalRotate.set(0, SE_Vector3f(0, 0, 0));
    mState = 7;
    mRenderState = 2;
}
SE_AddUserObjectCommand::~SE_AddUserObjectCommand()
{
}
void SE_AddUserObjectCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

    SE_ObjectCreator* primitive = new SE_ObjectCreator();
    mPrimitive->setVertexArray(mVertexArray, mVertexNum);
    mPrimitive->setVertexIndexArray(mVertexIndexArray, mVertexIndexNum);
    mPrimitive->setTextureCoorArray(mTextureCoorArray, mTextureCoorNum);
    mPrimitive->setTextureCoorIndexArray(mTextureCoorIndexArray, mTextureCoorIndexNum);
    mPrimitive->setFacetIndexArray(mFacetIndexArray, mFacetIndexNum);
    mPrimitive->setLocalRotate(mLocalRotate);
    mPrimitive->setLocalTranslate(mLocalTranslate);
    mPrimitive->setLocalScale(mLocalScale);
    mPrimitive->setObjectName(mObjectName.c_str());
    mPrimitive->setLayerIndex(mLayerIndex);
    mPrimitive->setIfLastLayerInWorld(mLastLayerInWorld);
    mPrimitive->setColor(mColor);
    mPrimitive->setAlpha(mAlpha);
    if (!mImagePath.empty())
    {
        SE_ImageDataID imageDataid(mImagePath.c_str());
        SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
        SE_ImageData* imgd = resourceManager->getImageData(imageDataid);
        if (imgd) {
            mPrimitive->setImageDataID(imageDataid);
        } else {
            imgd = SE_ImageCodec::load(mImagePath.c_str());
            mPrimitive->setImageDataID(imageDataid);
            if (imgd) {
                resourceManager->insertPathImageData(mImagePath.c_str(), imgd); 
                resourceManager->setIdPath(imageDataid.getStr(),mImagePath.c_str());
            }

        }
    }  
    SE_Scene* scene = SE_Application::getInstance()->getSceneManager()->getMainScene();
    SE_Spatial* mainSceneRoot = scene->getRoot();
    mPrimitive->create(scene, mainSceneRoot);
    delete mPrimitive;
}

SE_OperateCameraCommand::SE_OperateCameraCommand(SE_Application* app) : SE_Command(app)
{
}
SE_OperateCameraCommand::~SE_OperateCameraCommand()
{
}
void SE_OperateCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Camera* camera = SE_Application::getInstance()->getCurrentCamera();
    if (mTranslate)
    {
    camera->translateLocal(mLocation);
    } else
    {
    camera->setLocation(mLocation);
    }
    camera->rotateLocal(mRotate);
}

SE_OperateObjectCommand::SE_OperateObjectCommand(SE_Application* app) : SE_Command(app)
{
}
SE_OperateObjectCommand::~SE_OperateObjectCommand()
{
}
void SE_OperateObjectCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
   /* SE_Spatial * spatial = NULL;    
     
    if(!mObjectName.empty())
    {   
        SE_SimObject* simObject = SE_Application::getInstance()->getSimObjectManager()->findByName(mObjectName.c_str());
        if(simObject)
            spatial = simObject->getSpatial();
    } else {
        spatial = SE_Application::getInstance()->getSceneManager()->find(mSpatialID);
    }*/
    if ( mSpatial!= NULL)
    {    
        SE_Matrix3f identity;
        identity.identity();
        SE_Matrix4f transform;
        transform.identity();
        transform.set(identity,mTranslate);
        mSpatial->setPrevMatrix(transform);
        transform.identity(); 
        identity = identity.mul(mRotate.toMatrix3f());
        transform.set(identity,SE_Vector3f(0,0,0));
        identity.identity();
        identity.setScale(mScale.x,mScale.y,mScale.z);
        SE_Matrix4f transScale;
        transScale.identity();
        transScale.set(identity,SE_Vector3f(0,0,0));
        mSpatial->setPostMatrix(transform.mul(transScale));
        mSpatial->updateWorldTransform();
        mSpatial->updateBoundingVolume();
    }
}

SE_AddNewCbfCommand::SE_AddNewCbfCommand(SE_Application* app) : SE_Command(app)
{
}
SE_AddNewCbfCommand::~SE_AddNewCbfCommand()
{
}
void SE_AddNewCbfCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    if(!resourceManager)
    {
        return;
    }

    resourceManager->setDataPath(dataPath.c_str());
    
    if(!dataPath.empty())
    {        
        //size_t pos = mCbfFileName.find('.');
        //std::string name = mCbfFileName.substr(0, pos);//Do not need file ext name.
        std::string name = mCbfFileNameID;//Do not need file ext name.

        resourceManager->loadBaseData(NULL, name.c_str()); 
		SE_Spatial* s = resourceManager->loadScene(name.c_str());
        SE_SceneManager* sceneManager = mApp->getSceneManager();
		//TODO: we need point out which scene will be added with this cbf file
		//current is add to main scene
		SE_Scene* scene = sceneManager->getMainScene();
        //sceneM->createScene(name.c_str());
        SE_Spatial* rootScene = scene->getRoot();
		scene->addSpatial(rootScene, s);
        scene->inflate();
        rootScene->updateWorldTransform();
        rootScene->updateBoundingVolume();
#ifdef ANDROID
         SE_Application::getInstance()->sendMessageToJava(0, name.c_str());
#endif
    }
}

SE_SetObjectAlphaCommand::SE_SetObjectAlphaCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetObjectAlphaCommand::~SE_SetObjectAlphaCommand()
{
}
void SE_SetObjectAlphaCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    /*SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->find(mSpatialID);
    if (spatial)
    {
    spatial->setAlpha(mAlpha);
    if (mAlpha < 0.1)
    {
        spatial->setVisible(false);
    } else 
    {
        spatial->setVisible(true);
    }
    }*/
}

SE_SetObjectVisibleCommand::SE_SetObjectVisibleCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetObjectVisibleCommand::~SE_SetObjectVisibleCommand()
{
}
void SE_SetObjectVisibleCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    SE_SimObject* simObject = SE_Application::getInstance()->getSimObjectManager()->findByName(mName.c_str());
    if (simObject) {
        SE_Spatial * spatial = simObject->getSpatial();
        if (spatial)
        {
            spatial->setVisible(mVisible);
        }
    }
#endif
}

SE_SetObjectRenderStateCommand::SE_SetObjectRenderStateCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetObjectRenderStateCommand::~SE_SetObjectRenderStateCommand()
{
}
void SE_SetObjectRenderStateCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    /*SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->find(mSpatialID);
    if (spatial)
    {
    SE_BlendState* blendState = (SE_BlendState *)spatial->getRenderState(BLENDSTATE);
    if(!blendState)
    {
        blendState = new SE_BlendState();
        spatial->setRenderState(BLENDSTATE, blendState);     
    }
    if(mIsBlending)
    {
        blendState->setBlendProperty(SE_BlendState::BLEND_ENABLE);
    }
    else
    {
        blendState->setBlendProperty(SE_BlendState::BLEND_DISABLE);
    }

    SE_DepthTestState* depthTestState = (SE_DepthTestState *)spatial->getRenderState(DEPTHTESTSTATE);
    if(!depthTestState)
    {
        depthTestState = new SE_DepthTestState();
        spatial->setRenderState(DEPTHTESTSTATE, depthTestState);
    }
    if(mDepthTest)
    {
        depthTestState->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_ENABLE);
    }
    else
    {
        depthTestState->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
    }
    spatial->updateRenderState();
    }*/
}

SE_SetObjectLayerCommand::SE_SetObjectLayerCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetObjectLayerCommand::~SE_SetObjectLayerCommand()
{
}
void SE_SetObjectLayerCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    /*SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->find(mSpatialID);
    if (spatial)
    {
    spatial->setLocalLayer(mLayerIndex);
    spatial->updateWorldLayer();
    }*/
}

SE_SetObjectLightingPositonCommand::SE_SetObjectLightingPositonCommand(SE_Application* app) : SE_Command(app)
{
    mUseVbo = false;
}
SE_SetObjectLightingPositonCommand::~SE_SetObjectLightingPositonCommand()
{
}
void SE_SetObjectLightingPositonCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    SE_SimObject *simobj = SE_Application::getInstance()->getSimObjectManager()->findByName(mName.c_str());

    if(simobj)
    {
        for(int i = 0; i < simobj->getSurfaceNum(); ++i)
        {
            simobj->getMesh()->getSurface(i)->setLightPos(mWorldPositon);
            simobj->getMesh()->getSurface(i)->setProgramDataID(SIMPLELIGHTING_SHADER);        
            simobj->getMesh()->getSurface(i)->setRendererID(SIMPLELIGHTING_RENDERER);
        }
    }
#endif
}

SE_SetObjectNormalMapCommand::SE_SetObjectNormalMapCommand(SE_Application* app) : SE_Command(app)
{
    mUseVbo = false;
}
SE_SetObjectNormalMapCommand::~SE_SetObjectNormalMapCommand()
{
}
void SE_SetObjectNormalMapCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

#if 0
    SE_SimObject *simobj = SE_Application::getInstance()->getSimObjectManager()->findByName(mName.c_str());

    if(simobj)
    {
        for(int i = 0; i < simobj->getSurfaceNum(); ++i)
        {
            simobj->getMesh()->getSurface(i)->setLightPos(mWorldPositon);
            simobj->getMesh()->getSurface(i)->setProgramDataID(NORMALMAP_SHADER);        
            simobj->getMesh()->getSurface(i)->setRendererID(NORMALMAP_RENDERER);
        }
    }
#endif
}

SE_SetObjectDefaultShaderCommand::SE_SetObjectDefaultShaderCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetObjectDefaultShaderCommand::~SE_SetObjectDefaultShaderCommand()
{
}
void SE_SetObjectDefaultShaderCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    SE_SimObject *simobj = SE_Application::getInstance()->getSimObjectManager()->findByName(mName.c_str());

    if(simobj)
    {
        for(int i = 0; i < simobj->getSurfaceNum(); ++i)
        {            
            simobj->getMesh()->getSurface(i)->setProgramDataID(DEFAULT_SHADER);        
            simobj->getMesh()->getSurface(i)->setRendererID(DEFAULT_RENDERER);
        }
    }
#endif
}

SE_UnLoadSceneCommand::SE_UnLoadSceneCommand(SE_Application* app) : SE_Command(app)
{
}
SE_UnLoadSceneCommand::~SE_UnLoadSceneCommand()
{
}
void SE_UnLoadSceneCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    if(!resourceManager)
    {
        return;
    }

    resourceManager->releaseHardwareResource();

    delete SE_Application::getInstance();

}

SE_DeleteObjectCommand::SE_DeleteObjectCommand(SE_Application* app) : SE_Command(app)
{
}
SE_DeleteObjectCommand::~SE_DeleteObjectCommand()
{
}
void SE_DeleteObjectCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
	SE_SimObject * obj = NULL;
	SE_Spatial *spatial = NULL;
    SE_SimObjectManager *simobjectManager = SE_Application::getInstance()->getSimObjectManager();
    if(!simobjectManager)
    {
        return;
    }
    else
    {
        obj = simobjectManager->findByName(mObjectName.c_str());

		if(!obj)
		{
			return;
		}
		else
		{			
			spatial = obj->getSpatial();
		}

    }

	SE_CommonNode *root = (SE_CommonNode*)SE_Application::getInstance()->getSceneManager()->getMainScene()->getRoot();

    if(!root)
    {
		LOGI("Unload Scene Fail!! Can not find a root!!\n");
		return;        
    }
    else
    {
        root->removeChild(spatial);
		delete spatial;
    }
#endif

    
}

SE_CloneObjectCommand::SE_CloneObjectCommand(SE_Application* app) : SE_Command(app)
{
}
SE_CloneObjectCommand::~SE_CloneObjectCommand()
{
}
void SE_CloneObjectCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    SE_SimObject * src = SE_Application::getInstance()->getSimObjectManager()->findByName(mObjectName.c_str());
        
    if(src)
    {
        SE_Spatial *dest = src->getSpatial()->clone(src, 1);
    }
    else
    {
        LOGI("## The src not found! ##\n\n");
    }
#endif
}

SE_RotateSpatialByNameCommand::SE_RotateSpatialByNameCommand(SE_Application* app) : SE_Command(app)
{
	mRotateAngle = 0.0;
}
SE_RotateSpatialByNameCommand::~SE_RotateSpatialByNameCommand()
{
}
void SE_RotateSpatialByNameCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
	
	SE_Spatial * spatial = NULL;

    if(!mSpatialName.empty())
    {     
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

        if(!scm)
        {
            return;
        }
        spatial = scm->findSpatialByName(mSpatialName.c_str());
    }    

	if(spatial == NULL)
	{
        LOGI("$$ Can not get a spatial through a name or id.\n");
		return;
	}	

    SE_Matrix3f rotateM;
    rotateM.identity();

    SE_Matrix4f transform;
    transform.identity(); 


	//get current local matrix
	SE_Quat curVector = spatial->getLocalRotate();

	//generate rotate quat
	SE_Quat rotateQ;

	switch(mAxis)
	{
	case SE_AXIS_X:
		rotateQ.set(mRotateAngle,SE_Vector3f(1,0,0));        
		break;
	case SE_AXIS_Y:
		rotateQ.set(mRotateAngle,SE_Vector3f(0,1,0));        
		break;
	case SE_AXIS_Z:
		rotateQ.set(mRotateAngle,SE_Vector3f(0,0,1));        
		break;
	}

	//get new vector, after rotate	
    rotateM = rotateM.mul(rotateQ.toMatrix3f());

	//set new rotate matrix
    transform.set(rotateM,SE_Vector3f(0,0,0));

	spatial->setPostMatrix(spatial->getPostMatrix().mul(transform));

	//update 
	spatial->updateWorldTransform();
	spatial->updateBoundingVolume();

	

	SE_Message* msg = new SE_Message;
	msg->type = SE_MSG_UPATEWORLD;
	SE_Struct* sestruct = new SE_Struct(1);
	SE_StructItem* sitem = new SE_StructItem(1);
	SE_StdString* stdString = new SE_StdString;
	stdString->data = "update world.";
	sitem->setDataItem(stdString);
	sestruct->setStructItem(0, sitem);
	msg->data = sestruct;
	SE_Application::getInstance()->sendMessage(msg);

}

SE_ScaleSpatialByNameCommand::SE_ScaleSpatialByNameCommand(SE_Application* app) : SE_Command(app)
{
	mScaledX = 1.0;
	mScaledY = 1.0;
    mScaledZ = 1.0;
}
SE_ScaleSpatialByNameCommand::~SE_ScaleSpatialByNameCommand()
{
}
void SE_ScaleSpatialByNameCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

    SE_Spatial * spatial = NULL;	
     
    if(!mSpatialName.empty())
    {     
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

        if(!scm)
        {
            return;
        }
        spatial = scm->findSpatialByName(mSpatialName.c_str());
    }    

	if(spatial == NULL)
	{
        LOGI("$$ Can not get a spatial through a name or id.\n");
		return;
	}	

    SE_Matrix3f scaleM;
    scaleM.identity();

    SE_Matrix4f transform;
    transform.identity();

    scaleM.setScale(mScaledX,mScaledY,mScaledZ);
    transform.set(scaleM,SE_Vector3f(0,0,0));

	spatial->setPostMatrix(spatial->getPostMatrix().mul(transform));

	//update 
	spatial->updateWorldTransform();
	spatial->updateBoundingVolume();


	SE_Message* msg = new SE_Message;
	msg->type = SE_MSG_UPATEWORLD;
	SE_Struct* sestruct = new SE_Struct(1);
	SE_StructItem* sitem = new SE_StructItem(1);
	SE_StdString* stdString = new SE_StdString;
	stdString->data = "update world.";
	sitem->setDataItem(stdString);
	sestruct->setStructItem(0, sitem);
	msg->data = sestruct;
	SE_Application::getInstance()->sendMessage(msg);

}


SE_TranslateSpatialByNameCommand::SE_TranslateSpatialByNameCommand(SE_Application* app) : SE_Command(app)
{
	mTranslatedX = 0.0;
	mTranslatedY = 0.0;
    mTranslatedZ = 0.0;
}
SE_TranslateSpatialByNameCommand::~SE_TranslateSpatialByNameCommand()
{
}
void SE_TranslateSpatialByNameCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
	
	SE_Spatial * spatial = NULL;	
     
    if(!mSpatialName.empty())
    {     
		SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

        if(!scm)
        {
            return;
        }
        spatial = scm->findSpatialByName(mSpatialName.c_str());
    }    

	if(spatial == NULL)
	{
        LOGI("$$ Can not get a spatial through a name or id.\n");
		return;
	}	

	//the spatial is a child of the root,not a child of a group

	SE_Matrix3f identity3;
	identity3.identity();

	SE_Matrix4f transform;
	transform.identity();

	transform.set(identity3,SE_Vector3f(mTranslatedX,mTranslatedY,mTranslatedZ));

	spatial->setPrevMatrix(spatial->getPrevMatrix().mul(transform));

	//update
	spatial->updateWorldTransform();
	spatial->updateBoundingVolume();


	

	SE_Message* msg = new SE_Message;
	msg->type = SE_MSG_UPATEWORLD;
	SE_Struct* sestruct = new SE_Struct(1);
	SE_StructItem* sitem = new SE_StructItem(1);
	SE_StdString* stdString = new SE_StdString;
	stdString->data = "update world.";
	sitem->setDataItem(stdString);
	sestruct->setStructItem(0, sitem);
	msg->data = sestruct;
	SE_Application::getInstance()->sendMessage(msg);

}


SE_ResetSpatialByNameCommand::SE_ResetSpatialByNameCommand(SE_Application* app) : SE_Command(app)
{	
}
SE_ResetSpatialByNameCommand::~SE_ResetSpatialByNameCommand()
{
}
void SE_ResetSpatialByNameCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Spatial * spatial = NULL;	
     
    if(!mSpatialName.empty())
    {     
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

        if(!scm)
        {
            return;
        }
        spatial = scm->findSpatialByName(mSpatialName.c_str());
    }    

	if(spatial == NULL)
	{
        LOGI("$$ Can not get a spatial through a name or id.\n");
		return;
	}

    //reset translate    
    SE_Matrix4f transform;
    transform.identity();
    
    spatial->setPostMatrix(transform);
    spatial->setPrevMatrix(transform);

    //update 
	spatial->updateWorldTransform();
    spatial->updateBoundingVolume();
    

    SE_Message* msg = new SE_Message;
	msg->type = SE_MSG_UPATEWORLD;
	SE_Struct* sestruct = new SE_Struct(1);
	SE_StructItem* sitem = new SE_StructItem(1);
	SE_StdString* stdString = new SE_StdString;
	stdString->data = "update world.";
	sitem->setDataItem(stdString);
	sestruct->setStructItem(0, sitem);
	msg->data = sestruct;
	SE_Application::getInstance()->sendMessage(msg);
}


SE_PlayBipedAnimationCommand::SE_PlayBipedAnimationCommand(SE_Application* app) : SE_Command(app)
{	
}
SE_PlayBipedAnimationCommand::~SE_PlayBipedAnimationCommand()
{
}
void SE_PlayBipedAnimationCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    LOGI("******************************************************* **********************");
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
        //SE_SkinJointController* skinJointController = resourceManager->getSkinJointController("objLoft07");
        //SE_SkinJointController* skinJointController = resourceManager->getSkinJointController("man_head");

        SE_SkeletonController *sk = resourceManager->getSkeletonController(SE_SKELETONCONTROLLER);
        if(!sk)
        {
            return;
        }
        
        int bipcontrollerNum = sk->mSkeletonController.size();

        for(int j = 0; j < bipcontrollerNum; ++j)
        {
            SE_BipedController* bipedController = sk->mSkeletonController[j];

            int animNum = bipedController->bipAndObjInfo.size();

            for(int i = 0; i < animNum; ++i)
            {
                SE_BipedAnimation* anim = new SE_BipedAnimation();

                SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();
                //SE_SimObject* simobj = simObjectManager->findByName("objLoft07");
                SE_SimObject* simobj = simObjectManager->findByName(bipedController->bipAndObjInfo[i]->objHasBiped.c_str());

                bipedController->setOriginalModelToWorld(simobj->getSpatial()->getOriginalTransform());
                bipedController->initSkeletonAnimation();

                for(int j = 0; j < simobj->getSurfaceNum(); ++j)
                {
                    simobj->getMesh()->getSurface(j)->setProgramDataID("skeletalanimation_shader");        
                    simobj->getMesh()->getSurface(j)->setRendererID("skeletalanimation_renderer");
                }

                SE_Spatial* spatial = simobj->getSpatial();

                

                anim->setRunMode(SE_Animation::NOT_REPEAT);
                anim->setTimeMode(SE_Animation::SIMULATE);
                anim->setDuration(3000);
                anim->setSimObject(simobj);
                //anim->setSkinJointController(skinJointController);
                anim->setSkinJointController(bipedController);

                //set play mode with SHADER
                anim->setPlayMode(SE_Animation::GPU_SKELETON_SHADER);
                //anim->setPlayMode(SE_Animation::CPU_NO_SHADER);

                SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
                animManager->removeAnimation(spatial->getAnimationID());
                SE_AnimationID animID = animManager->addAnimation(anim,"id");
                spatial->setAnimationID(animID);
                //anim->run();
            }
              LOGI("## up ##\n");
        }
    
        SE_BipedAnimation::generateBipedData();

        for(int j = 0; j < bipcontrollerNum; ++j)
        {
            SE_BipedController* bipedController = sk->mSkeletonController[j];

            int animNum = bipedController->bipAndObjInfo.size();

            for(int i = 0; i < animNum; ++i)
            {                

                SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();
                //SE_SimObject* simobj = simObjectManager->findByName("objLoft07");
                SE_SimObject* simobj = simObjectManager->findByName(bipedController->bipAndObjInfo[i]->objHasBiped.c_str());

                SE_Spatial* spatial = simobj->getSpatial();

                SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();

                animManager->getAnimation(spatial->getAnimationID())->run();

            }
        }
    

    SE_Message* msg = new SE_Message;
	msg->type = SE_MSG_UPATEWORLD;
	SE_Struct* sestruct = new SE_Struct(1);
	SE_StructItem* sitem = new SE_StructItem(1);
	SE_StdString* stdString = new SE_StdString;
	stdString->data = "update world.";
	sitem->setDataItem(stdString);
	sestruct->setStructItem(0, sitem);
	msg->data = sestruct;
	SE_Application::getInstance()->sendMessage(msg);
#endif
}

SE_InitRenderCapabilitiesCommand::SE_InitRenderCapabilitiesCommand(SE_Application* app) : SE_Command(app)
{	
}
SE_InitRenderCapabilitiesCommand::~SE_InitRenderCapabilitiesCommand()
{
}
void SE_InitRenderCapabilitiesCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Application::getInstance()->getRenderSystemCapabilities()->init();
}

SE_DeleteObjectsCommand::SE_DeleteObjectsCommand(SE_Application* app): SE_Command(app)
{

}

SE_DeleteObjectsCommand::~SE_DeleteObjectsCommand()
{
}

void SE_DeleteObjectsCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    //SE_Spatial* spatial = SE_Application::getInstance()->getSpatialManager()->findByName(mSpatialName.c_str());
	std::string name = mSpatialName.substr(0, mSpatialName.find('_'));
	SE_Spatial* spatial = NULL;
	
	    std::string sceneName = std::string("root") + "@" + mSpatialName + "_basedata.cbf";
        spatial = SE_Application::getInstance()->getSceneManager()->getMainScene()->findSpatialByName(sceneName.c_str());

	if(spatial)
	{
	    SE_Scene*  scene = spatial->getScene();
	    SE_Spatial* rs = scene->removeSpatial(spatial);
        if (rs) 
	    {
			delete rs;
		}
   }
}

class Attribute
{
public:
	Attribute()
	{
	}


	SE_Vector3f mTranslate;
	SE_Quat mRotate;
	SE_Vector3f mScale;
	//std::string mName;
};

std::map<std::string, GroupAttribute> mGroupAttributeListMap;

typedef std::map<std::string, Attribute> SpatialAttribute;
SpatialAttribute mSpatialAttribute;

class _WriteSceneTravels : public SE_SpatialTravel
{
public:
    _WriteSceneTravels(SE_BufferOutput& out,SE_BufferOutput& effectout) : mOut(out),mEffectOut(effectout)
    {
	    mTranslateSave = false;
		mScaleSave = false;
        mRotateSave = false;
	}
    int visit(SE_Spatial* spatial)
    {

        spatial->write(mOut);
        spatial->writeEffect(mEffectOut);
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }
public:
    SE_BufferOutput& mOut;
    SE_BufferOutput& mEffectOut;
	bool mTranslateSave;
	bool mScaleSave;
	bool mRotateSave;
};

class _RecoverSceneTravels : public SE_SpatialTravel
{
public:
    _RecoverSceneTravels(SE_BufferOutput& out) : mOut(out)
    {
	}
    int visit(SE_Spatial* spatial)
    {
        std::string name = spatial->getSpatialName();
		   std::map<std::string, Attribute>::iterator it = mSpatialAttribute.find(name);
		   if(it != mSpatialAttribute.end())
         {
               Attribute attribute = it->second;
               if(!mTranslateSave)
         {
		           spatial->setLocalTranslate(attribute.mTranslate);
         }
			   if(!mRotateSave)
			   {
		           spatial->setLocalRotate(attribute.mRotate);
			   }
			   if(!mScaleSave)
			   {
                   spatial->setLocalScale(attribute.mScale);
         }
		   }
       	
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 0;
    }
public:
    SE_BufferOutput& mOut;
	bool mTranslateSave;
	bool mScaleSave;
	bool mRotateSave;
};

SE_SaveSceneCommand::SE_SaveSceneCommand(SE_Application* app): SE_Command(app)
{
	mIsTranslateSave = false;
	mIsScaleSave = false;
	mIsRotateSave = false;
}

SE_SaveSceneCommand::~SE_SaveSceneCommand()
{
}
void SE_SaveSceneCommand::writeHeader(SE_BufferOutput& output, int dataLen)
{
    output.writeInt(SE_MAGIC);
    output.writeInt(SE_VERSION);
    output.writeInt(SE_ENDIAN);
    output.writeInt(dataLen);
}
void SE_SaveSceneCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_BufferOutput outScene;
    SE_BufferOutput outSceneEffect;
    SE_BufferOutput outSceneHeader;

    std::string outSceneFileName(mOutSceneName);
    std::string outSceneEffectFileName(mOutSceneName);
	//outSceneFileName = std::string(SE_Application::getInstance()->getResourceManager()->getDataPath() )+ SE_SEP + outSceneFileName + "_scene.cbf";
    outSceneFileName = mOutFilePath + SE_SEP + outSceneFileName + "_scene.cbf";
    outSceneEffectFileName = mOutFilePath + SE_SEP + outSceneEffectFileName + "_effect.cbf";
    SE_SceneID sceneID = SE_Application::getInstance()->createCommonID();

    sceneID.write(outScene);
    _WriteSceneTravels wst(outScene,outSceneEffect);
	wst.mTranslateSave = mIsTranslateSave;
	wst.mScaleSave = mIsScaleSave;
	wst.mRotateSave = mIsRotateSave;

    if(SE_Application::getInstance()->getSceneManager()->getMainScene()==NULL)
    {
        return;
    }
	std::string sceneName = std::string("root") + "@" + mOutSceneName + "_basedata.cbf";
		//rootNode = (SE_CommonNode*)SE_Application::getInstance()->getSpatialManager()->findByName("root@desk_basedata.cbf");
    SE_CommonNode* rootNode = (SE_CommonNode*)SE_Application::getInstance()->getSceneManager()->findSpatialByName(sceneName.c_str());
    //SE_Application::getInstance()->getSceneManager()->getMainScene()->setSaveScene(true);
	if(rootNode == NULL)
	{
        return;
	}

    SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->findSpatialByName(mGroupName.c_str());
	Attribute attribute;
			attribute.mTranslate = spatial->getLocalTranslate();
            attribute.mRotate = spatial->getLocalRotate();
            attribute.mScale = spatial->getLocalScale();

			mSpatialAttribute[mGroupName] = attribute;


	//std::string name = spatial->getParent()->getSpatialName();
	        std::map<std::string, GroupAttribute>::iterator it = mGroupAttributeListMap.find(mGroupName);
			//SE_Spatial* spatial = SE_Application::getInstance()->getSpatialManager()->findByName(mGroupName.c_str());
	        if(it != mGroupAttributeListMap.end())
	        {
				GroupAttribute groupattribute = it->second;
                if(!mIsTranslateSave)
			    {
                     //spatial->setLocalTranslate(SE_Vector3f(0,0,0));
					spatial->setLocalTranslate(groupattribute.mTranslate);
			    }
				else
				{
                    mGroupAttributeListMap[mGroupName].mTranslate = spatial->getLocalTranslate();
				}

			    if(!mIsScaleSave)
			    {
				    //spatial->setLocalScale(SE_Vector3f(1,1,1));  
					spatial->setLocalScale(groupattribute.mScale);
			    }
				else
				{
                    mGroupAttributeListMap[mGroupName].mScale = spatial->getLocalScale();
				}

			    if(!mIsRotateSave)
			    {
                    //spatial->setLocalRotate(SE_Quat(0,0,0,1));
                    spatial->setLocalRotate(groupattribute.mRotate);
			    }
				else
				{
                    mGroupAttributeListMap[mGroupName].mRotate = spatial->getLocalRotate();
				}



	        }
	        /*else
	        {
		        GroupAttribute attribute;
		        attribute.mTranslate = spatial->getLocalTranslate();
                attribute.mRotate = spatial->getLocalRotate();
                attribute.mScale = spatial->getLocalScale();

		         mGroupAttributeListMap[spatial->getSpatialName()] = attribute;
	        }*/
   

    rootNode->travel(&wst, true);

    writeHeader(outSceneHeader, outScene.getDataLen());
    SE_File fscene(outSceneFileName.c_str(), SE_File::WRITE);
    fscene.write(outSceneHeader);
    fscene.write(outScene);


    SE_File feffectscene(outSceneEffectFileName.c_str(), SE_File::WRITE);    
    feffectscene.write(outSceneEffect);

    if(!mIsTranslateSave || !mIsScaleSave || !mIsRotateSave)
	{
		 //std::string name = spatial->getSpatialName();
		   std::map<std::string, Attribute>::iterator it = mSpatialAttribute.find(mGroupName);
		   if(it != mSpatialAttribute.end())
		   {
               Attribute attribute = it->second;
               if(!mIsTranslateSave)
			   {
		           spatial->setLocalTranslate(attribute.mTranslate);
			   }
			   if(!mIsScaleSave)
			   {
		           spatial->setLocalRotate(attribute.mRotate);
			   }
			   if(!mIsRotateSave)
			   {
                   spatial->setLocalScale(attribute.mScale);
			   }
		   }
	}
}

SE_QuitApplicationCommand::SE_QuitApplicationCommand(SE_Application* app): SE_Command(app)
{

}

SE_QuitApplicationCommand::~SE_QuitApplicationCommand()
{
}

void SE_QuitApplicationCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Application::getInstance()->setState(SUSPEND);
}

SE_SwitchCameraDOFCommand::SE_SwitchCameraDOFCommand(SE_Application* app) : SE_Command(app)
{
    enable = false;
}
SE_SwitchCameraDOFCommand::~SE_SwitchCameraDOFCommand()
{
}
void SE_SwitchCameraDOFCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Application::getInstance()->getCurrentCamera()->setNeedDof(enable);
}
