#include "PVRShell.h"
#include <stdio.h>
#include <math.h>
#include "SE_Ase.h"
#include "SE_ResourceManager.h"
#include "SE_Spatial.h"
#include "SE_Log.h"
#include "SE_Common.h"
#include "SE_Camera.h"
#include "SE_Application.h"
#include "SE_SystemCommand.h"
#include "SE_SystemCommandFactory.h"
#include "SE_InputEvent.h"
#include "SE_Primitive.h"
#include "SE_Geometry.h"
#include "SE_MeshSimObject.h"
#include "SE_SceneManager.h"
#include "SE_ImageCodec.h"
#include "SE_CommonNode.h"
#include "SE_MessageDefine.h"
//#include "SE_Physics.h"
#include "SE_2DCommand.h"
#include "SE_ElementManager.h"
#include "SE_AnimationManager.h"
#include <ctype.h>
#include <stdarg.h>
#ifdef WIN32
#else
#include <stdint.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <vector>
#define SCREEN_WIDTH  480
#define SCREEN_HEIGHT 800
static void drawScene(int width, int height)
{
}

class SEDemo : public PVRShell
{
public:
	SEDemo()
	{
		//mPhysics = NULL;
		mSelectedSpatial = NULL;
	}
	virtual bool InitApplication();
	virtual bool InitView();
	virtual bool ReleaseView();
	virtual bool QuitApplication();
	virtual bool RenderScene();
private:
	void handleInput(int width, int height);
private:
	//SE_Physics* mPhysics;
    SE_Spatial* mSelectedSpatial;
};
bool SEDemo::InitApplication()
{
	//PVRShellSet(prefWidth, SCREEN_WIDTH);
	//PVRShellSet(prefHeight, SCREEN_HEIGHT);
	SE_Application::SE_APPID appid;
	appid.first = 137;
	appid.second = 18215879;
	SE_Application::getInstance()->setAppID(appid);
	SE_SystemCommandFactory* sf = new SE_SystemCommandFactory;
	SE_Application::getInstance()->registerCommandFactory("SystemCommand", sf);
	SE_Init2D* c = new SE_Init2D(SE_Application::getInstance());
	//SE_InitAppCommand* c = (SE_InitAppCommand*)SE_Application::getInstance()->createCommand("SE_InitAppCommand");
#ifdef WIN32
	c->dataPath = "c:\\model\\newhome3";//"D:\\model\\jme\\home\\newhome3";
#else
	c->dataPath = "/home/luwei/model/jme/home/newhome3";
#endif
	c->fileName = "TestElement.xml";
	SE_Application::getInstance()->postCommand(c);
	return true;
}
bool SEDemo::InitView()
{
	int dwCurrentWidth = PVRShellGet (prefWidth);
	int dwCurrentHeight = PVRShellGet (prefHeight);
	LOGI("## width = %d, height = %d ###\n", dwCurrentWidth,dwCurrentHeight);
	SE_2DUpdateCameraCommand* c = new SE_2DUpdateCameraCommand(SE_Application::getInstance());
    c->width = dwCurrentWidth;
    c->height = dwCurrentHeight;
	SE_Application::getInstance()->postCommand(c);
	return true;
}
bool SEDemo::ReleaseView()
{
	
	return true;
}
bool SEDemo::QuitApplication()
{
	return true;
}
class SE_RunAllAnimationTravel : public SE_SpatialTravel
{
public:
    int visit(SE_Spatial* spatial)
    {
		SE_ElementManager* elementManager = SE_Application::getInstance()->getElementManager();
		SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
		SE_ElementID elementID = spatial->getElementID();
		SE_AnimationID animID = spatial->getAnimationID();
		animManager->removeAnimation(animID);
		SE_Element* element = elementManager->findByName(elementID.getStr());
		if(element)
		{
			SE_Animation* anim = element->getAnimation();
			SE_Animation* cloneAnim = NULL;
			if(anim)
			{
				cloneAnim = anim->clone();
				animID = animManager->addAnimation(cloneAnim);
				spatial->setAnimationID(animID);
				cloneAnim->setRunMode(SE_Animation::REPEAT);
				cloneAnim->run();
			}
		}
		return 0;
    }
    int visit(SE_SimObject* simObject)
	{
        return 1;
	}
};
class SE_PauseAllAnimationTravel : public SE_SpatialTravel
{
public:
	int visit(SE_Spatial* spatial)
	{
        SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
		SE_AnimationID animID = spatial->getAnimationID();
		SE_Animation* anim = animManager->getAnimation(animID);
		if(anim)
		{
			anim->setRunMode(SE_Animation::ONE_FRAME);
			anim->pause();
		}
		return 0;
	}
	int visit(SE_SimObject* simObject)
	{
        return 0;
	}
};
void SEDemo::handleInput(int width, int height)
{
    static float prevPointer[2];
    static bool bPressed = false;
    int buttonState = PVRShellGet(prefButtonState);
    float* pointerLocation = (float*)PVRShellGet(prefPointerLocation);
    /*LOGI("## buttonstate = %d ##\n", buttonState);*/
    if(pointerLocation)
    {
		//LOGI("### pointer location = %f, %f ###\n", pointerLocation[0], pointerLocation[1]);
        prevPointer[0] = pointerLocation[0];
        prevPointer[1] = pointerLocation[1];
    }
    if((buttonState & ePVRShellButtonLeft))
    {
		SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
		if(c)
		{
			SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::DOWN, prevPointer[0] * width, prevPointer[1] * height);
			c->motionEvent = ke;
			SE_Application::getInstance()->postCommand(c);
		}
	    bPressed = 1;
    }
    else if(bPressed)
    {
        SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
		if(c)
		{
			SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::UP, prevPointer[0] * width, prevPointer[1] * height);
			c->motionEvent = ke;
			SE_Application::getInstance()->postCommand(c);
		}
        bPressed = 0;
    }
    if(PVRShellIsKeyPressed(PVRShellKeyNameLEFT))
    {
		if(mSelectedSpatial)
		{
            SE_ElementManager* elementManager = SE_Application::getInstance()->getElementManager();
			SE_Element* element = elementManager->findByName(mSelectedSpatial->getElementID().getStr());
            if(element)
            {
                SE_Animation* anim = element->getAnimation();
				if(anim)
				{
					SE_Animation* newAnim = anim->clone();
                    SE_AnimationManager* animationManager = SE_Application::getInstance()->getAnimationManager();
					SE_AnimationID animID = mSelectedSpatial->getAnimationID();
					animationManager->removeAnimation(animID);
					animID = animationManager->addAnimation(newAnim);
			        mSelectedSpatial->setAnimationID(animID);
                    newAnim->run();
				}
			}
        }
		else
		{
			SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
	        SE_Spatial* root = sceneManager->getRoot();
	        SE_RunAllAnimationTravel rat;
	        root->travel(&rat, true);
		}
        LOGI("## left ##\n");
    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameRIGHT))
    {
		if(mSelectedSpatial)
		{
		    SE_AnimationID animID = mSelectedSpatial->getAnimationID();
		    SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
		    SE_Animation* anim = animManager->getAnimation(animID);
			if(anim)
		        anim->nextFrame(30, 30);
		}
        LOGI("## right ##\n");
    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameUP))
    {
		if(mSelectedSpatial)
		{
			SE_PauseAllAnimationTravel rat;
			mSelectedSpatial->travel(&rat, true);
		}
		else
		{
            SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
	        SE_Spatial* root = sceneManager->getRoot();
	        SE_PauseAllAnimationTravel rat;
	        root->travel(&rat, true);
		}
  	    LOGI("## up ##\n");
    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameDOWN))
    {
		SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
		sceneManager->setSelectedSpatial(NULL);
		mSelectedSpatial = NULL;
	    LOGI("## down ##\n");
    }
}
bool SEDemo::RenderScene()
{
	int dwCurrentWidth = PVRShellGet (prefWidth);
	int dwCurrentHeight = PVRShellGet (prefHeight);
    handleInput(dwCurrentWidth, dwCurrentHeight);
	/*
	if(mPhysics)
	{
	    mPhysics->stepSimulation(1.0f / 60);
	    SE_Matrix4f m = mPhysics->getObjMatrix();
		SE_Vector3f v = m.getTranslate();
		if(groupNode)
		{
			groupNode->setLocalTranslate(v);
			groupNode->updateWorldTransform();
		}
		for(int i = 0 ; i < 4 ; i++)
		{
			SE_Vector4f v = m.getColumn(i);
			LOGI("## %d : %f %f %f %f\n", i, v.x, v.y, v.z, v.w);
		}
	}
	*/
	SE_Application::getInstance()->run();
	int messageCount = SE_Application::getInstance()->getMessageCount();
	if(messageCount > 0)
	{
	    SE_Application::_MessageVector messageVector = SE_Application::getInstance()->getMessage();
		for(int i = 0 ; i < messageVector.size() ; i++)
		{
			SE_Message* msg = messageVector[i];
			LOGI("### msg type = %d ####\n", msg->type);
			if(msg->type == SE_MSG_SIMOBJECT_NAME)
			{
				mSelectedSpatial = SE_Application::getInstance()->getSceneManager()->getSelectedSpatial();
			}
			SE_Struct* structData = msg->data;
			int structItemSize = structData->getCount();
			LOGI("### struct item size = %d ####\n", structItemSize);
			SE_StructItem* item = structData->getStructItem(0);
			SE_DataItem di = item->getDataItem(0);
			SE_StdString* strData = (SE_StdString*)di.data.virtualData;
			LOGI("#### obj name = %s #### \n", strData->data.c_str());
		}
		SE_Application::getInstance()->releaseMessage();
	}
	return true;
}
PVRShell* NewDemo()
{
	return new SEDemo();
}

