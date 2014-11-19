#include "SE_DynamicLibType.h"
#include "SE_RootView.h"
#include <list>


//#include <assert.h>
#include "SE_Application.h"
#include "SE_Spatial.h"
#include "SE_Geometry.h"
#include "SE_SceneManager.h"
#include "SE_ID.h"
#include "SE_View.h"
#include "SE_Application.h"

#include "SE_ViewGroup.h"
#include "SE_ViewData.h"



/*
template<> RootView* Singleton<RootView>::ms_Singleton = 0;
RootView* RootView::getSingletonPtr(void)
{
	return ms_Singleton;
}
RootView& RootView::getSingleton(void)
{
	assert( ms_Singleton );  return ( *ms_Singleton );
}
*/

RootView::RootView(SE_Spatial* parent):SE_CommonNode(parent)
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


ViewGroup* RootView::GetViewGroup(const char* viewGroupName)
{
	std::map<std::string, ViewGroup*>::iterator iter;
	iter = mViewGroupMap.find(std::string(viewGroupName));
	if(iter != mViewGroupMap.end())
	{
		return iter->second;
	}
	else
	{
		return NULL;
	}
}


void RootView::AddViewGroup(const char* viewgroupName)
{

	ViewGroup* viewgroup = new ViewGroup(this);
	mViewGroupMap[std::string(viewgroupName)] = viewgroup;
}