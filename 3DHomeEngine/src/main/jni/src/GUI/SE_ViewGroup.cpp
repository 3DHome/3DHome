#include "SE_DynamicLibType.h"
#include "SE_ViewGroup.h"
#include "SE_Application.h"
#include "SE_Spatial.h"
#include "SE_Geometry.h"
#include "SE_SceneManager.h"
#include "SE_ID.h"
#include "SE_View.h"
#include "SE_Application.h"

#include "SE_ViewData.h"
#include "SE_RootView.h"


ViewGroup::ViewGroup(SE_Spatial* parent):SE_CommonNode(parent)
{
	setNodeType(GROUP_NODE);

	SE_SpatialID spatialID = SE_ID::createSpatialID();
	unsigned int* ids = spatialID.getID();
	SE_SimObjectID id(ids[0], ids[1], ids[2], ids[3]);

/*
    SE_Geometry* geometry = new SE_Geometry(spatialID, root);
    geometry->attachSimObject(this);
	SE_Application::getInstance()->getSimObjectManager()->set(spatialID, this);*/
    updateWorldTransform();
    updateBoundingVolume();

	parent->addChild(this);

}

void ViewGroup::AddView(const char* viewName, int x,int y, int w, int h, const char* imageName){
	
    SE_SpatialID spatialID = SE_ID::createSpatialID();
    View* geometry = new View(spatialID,this);
	addChild(geometry);
	geometry->setParent(this);

	mViewMap[std::string(viewName)] = geometry;

    ViewData* simObj = new ViewData(geometry);
    simObj->setName(viewName);
	simObj->Init(0,0,w,h,imageName);
	//mViewList.push_back(simObj);
    geometry->SetViewdata(simObj);
    geometry->setPosition(x,y);
	
    geometry->setMovable(false);
    geometry->setCollisionable(false);
    geometry->updateWorldTransform();
    geometry->setBVType(1);
    geometry->updateBoundingVolume();

    SE_DepthTestState* rds = new SE_DepthTestState();
	rds->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
	geometry->setRenderState(DEPTHTESTSTATE, rds);
	geometry->updateRenderState();

    SE_BlendState *rs = new SE_BlendState();
    rs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
    geometry->setRenderState(BLENDSTATE,rs);
    //geometry->setLocalLayer(2);
    //geometry->updateRenderState();
    geometry->updateWorldLayer(); 
    geometry->updateRenderState();
 
    //geometry->updateWorldLayer();
    geometry->setAlpha(1);
    geometry->setVisible(true);
    
}

View* ViewGroup::GetView(const char* viewName)
{
	std::map<std::string, View*>::iterator iter;
	iter = mViewMap.find(std::string(viewName));
	if(iter != mViewMap.end())
	{
		return iter->second;
	}
	else
	{
		return NULL;
	}
}
void ViewGroup::SetVisible(bool visible)
{
	setVisible(visible);
}

/*
bool RootView::AddChildView(const char * parentName,const char* viewName, int x,int y, int w, int h, const char* imageName)
{
	ViewGroup* parentview = GetViewGroup(parentName);
	if(parentview==NULL)return false;
	
	//SE_Spatial* parentSpatial = parentview->getSpatial();
	//if(parentSpatial==NULL)return false;
	
    SE_SpatialID spatialID = SE_ID::createSpatialID();
    View* geometry = new View(spatialID,parentview);

	addChild(geometry);
	geometry->setParent(parentview);

	mViewMap[std::string(viewName)] = geometry;

    ViewData* simObj = new ViewData(geometry);
    simObj->setName(viewName);
	simObj->Init(x,y,w,h,imageName);
	//mViewList.push_back(simObj);

    geometry->attachSimObject(simObj);

	
    geometry->setMovable(false);
    geometry->setRemovable(true);
    geometry->setCollisionable(false);
//    geometry->setLocalTranslate(mLocalTranslate);
//    geometry->setLocalRotate(mLocalRotate);
//    geometry->setLocalScale(mLocalScale);
    geometry->updateWorldTransform();
    geometry->setBVType(1);
    geometry->updateBoundingVolume();

    SE_DepthTestState* rds = new SE_DepthTestState();
	rds->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
	geometry->setRenderState(DEPTHTESTSTATE, rds);
	geometry->updateRenderState();

    SE_BlendState *rs = new SE_BlendState();
    rs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
    geometry->setRenderState(BLENDSTATE,rs);
    geometry->setLocalLayer(2);
    geometry->updateRenderState();
    geometry->updateWorldLayer(); 
    geometry->updateRenderState();
 
    geometry->updateWorldLayer();
    geometry->setAlpha(1);
    geometry->setVisible(true);
	return true;
}
*/
