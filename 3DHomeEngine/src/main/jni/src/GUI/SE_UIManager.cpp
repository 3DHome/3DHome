#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_UIManager.h"

#include "SE_View.h"
#include "SE_RootView.h"
#include "SE_ViewGroup.h"
#include "SE_Vector.h"
#include "SE_Camera.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"



SE_UIManager::SE_UIManager()
{
	mRootView = NULL;

}
SE_UIManager::~SE_UIManager()
{
}
void SE_UIManager::CreateGUIScene()
{
	if(mRootView==NULL){
		SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
		SE_Scene* scene = new SE_Scene("GUI");        
		scene->setIsTranslucent(true);
		float width = 320;
		float height = 480;
		//SE_Rect<int> viewport = sceneManager->getMainScene()->getCamera()->getViewport();
		//int width = viewport.right - viewport.left;
		//int height = viewport.bottom - viewport.top;
		SE_Vector3f location(0,0,320);//vb
		SE_Vector3f zAxis(0, 0, 1);    
		SE_Vector3f up(0, 1, 0);	
		mCamera = new SE_Camera();
		mCamera->create(location, zAxis, up, 90.0f,height/ width, 1.0f, 1000.0f);
		mCamera->setViewport(0, 0, width, height);
		scene->setCamera(mCamera);
		sceneManager->pushBack(scene->getType(), scene);

		SE_CommonNode* rootnode = (SE_CommonNode*)SE_ObjectFactory::create("SE_CommonNode");
        rootnode->setNodeType(GROUP_NODE);
		
		scene->setRoot((SE_Spatial*)rootnode);
		mRootView = new RootView(rootnode);

	}

}

void SE_UIManager::ReSizeUIViewPort(float w, float h)
{
	mCamera->setFrustum(90.0f, h/w, 1.0f, 1000.0f);
	mCamera->setViewport(0, 0, w, h);
	SE_Vector3f location(0,0,w/2);
	mCamera->setLocation(location);

}


void SE_UIManager::AddView(const char* groupName,const char* viewName,float x, float y, float w, float h, const char* imageName)
{
	if(mRootView){
		ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
		if(viewGroup)
		{
			viewGroup->AddView(viewName,x, y, w, h, imageName);
		}
	}
}


void SE_UIManager::SetViewPosition(const char* groupName,const char* viewName, float x, float y)
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		View* view = viewGroup->GetView(viewName);
		if(view)
		{
			view->setPosition( x, y);
		}

	}



}
void SE_UIManager::SetViewSize(const char* groupName,const char* viewName,float w, float h )
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		View* view = viewGroup->GetView(viewName);
		if(view)
		{
			view->setSize(w, h);
		}
	}

}
void SE_UIManager::SetViewImage(const char* groupName,const char* viewName, const char* imageName)
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		View* view = viewGroup->GetView(viewName);
		if(view)
		{
			view->SetImage(imageName);
		}
	}
}

void SE_UIManager::SetViewImage(const char* groupName,const char* viewName, SE_ImageData*  imgd)
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		View* view = viewGroup->GetView(viewName);
		if(view)
		{
			view->SetImage(imgd);
		}
	}
}


void SE_UIManager::SetViewVisible(const char* groupName,const char* viewName, bool visible)
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		View* view = viewGroup->GetView(viewName);
		if(view)
		{
			view->setVisible(visible);
		}
	}

}
void SE_UIManager::AddViewGroup(const char* viewName)
{
	mRootView->AddViewGroup(viewName);
}

void SE_UIManager::SetViewGroupVisible(const char* groupName, bool visible)
{
	ViewGroup* viewGroup = mRootView->GetViewGroup(groupName);
	if(viewGroup)
	{
		viewGroup->setVisible(visible);
	}
}


