#include "SE_DynamicLibType.h"
#include "SE_InputManager.h"
#include "SE_Object.h"
#include "SE_InputEvent.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_MotionEventCamera.h"
#include "SE_MemLeakDetector.h"

#ifdef EDITOR
#include "SE_EditorManager.h"
#endif

SE_InputManager::SE_InputManager()
{
    //mKeyEventObserverList.clear();
   // mMotionEventObserverList.clear();
   mSceneManager = NULL;
}
SE_InputManager::~SE_InputManager()
{}
void SE_InputManager::update(SE_KeyEvent* keyEvent)
{
    /*
	KeyEventObserverList::iterator it ;
	for(it = mKeyEventObserverList.begin() ; it != mKeyEventObserverList.end() ; it++)
	{
		SE_Object* obj = *it;
		obj->onKeyEvent(keyEvent);
	}
    */
	if(!mSceneManager)
		mSceneManager = SE_Application::getInstance()->getSceneManager();
    SE_Scene* topScene = mSceneManager->getTopScene();

#ifdef EDITOR
	if(keyEvent->getViewportId()!=-1)
	{
		SE_MotionEventCamera* camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(keyEvent->getViewportId());
		camera->onKeyEvent(keyEvent);

	}
#else
    if(topScene)
    {
        SE_Camera* camera = topScene->getCamera();
        camera->onKeyEvent(keyEvent);
    }
#endif
}
void SE_InputManager::update(SE_MotionEvent* motionEvent)
{
    /*
    MotionEventObserverList::iterator it ;
	for(it = mMotionEventObserverList.begin() ; it != mMotionEventObserverList.end() ; it++)
	{
		SE_Object* obj = *it;
		obj->onMotionEvent(motionEvent);
	}
    */
	if(!mSceneManager)
		mSceneManager = SE_Application::getInstance()->getSceneManager();
    SE_Scene* topScene = mSceneManager->getTopScene();

#ifdef EDITOR
	if(motionEvent->getViewportId()!=-1)
	{
		SE_MotionEventCamera* camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(motionEvent->getViewportId());
		camera->onMotionEvent(motionEvent);


		if(motionEvent!=NULL || motionEvent->getType()!=SE_MotionEvent::UP)
		{
			float x = motionEvent->getX();
			float y = motionEvent->getY();
			if( motionEvent->getViewportId() == SE_Camera::EOrthoXZ)
			{
				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoYZ);
				motionEvent->setX(0);
				motionEvent->setY(y);
				camera->onMotionEvent(motionEvent);

				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoXY);
				motionEvent->setX(x);
				motionEvent->setY(0);
				camera->onMotionEvent(motionEvent);
			}
			else if( motionEvent->getViewportId() == SE_Camera::EOrthoYZ)
			{
				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoXZ);
				motionEvent->setX(0);
				motionEvent->setY(y);
				camera->onMotionEvent(motionEvent);

				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoXY);
				motionEvent->setX(0);
				motionEvent->setY(x);
				camera->onMotionEvent(motionEvent);
			}
			else if( motionEvent->getViewportId() == SE_Camera::EOrthoXY)
			{
				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoYZ);
				motionEvent->setX(y);
				motionEvent->setY(0);
				camera->onMotionEvent(motionEvent);

				camera = SE_Application::getInstance()->getEditorManager()->getViewportCamera(SE_Camera::EOrthoXZ);
				motionEvent->setX(x);
				motionEvent->setY(0);
				camera->onMotionEvent(motionEvent);
			}
			else
			{

			}
		}



	}
#else
    if(topScene)
    {
        SE_Camera* camera = topScene->getCamera();
        camera->onMotionEvent(motionEvent);
    }
#endif
}
/*
void SE_InputManager::addKeyEventOberver(SE_Object* keyEventObserver)
{
	mKeyEventObserverList.push_back(keyEventObserver);
}
void SE_InputManager::addMotionEventOberver(SE_Object* motionEventObserver)
{
    mMotionEventObserverList.push_back(motionEventObserver);
}
void SE_InputManager::removeKeyEventObserver(SE_Object* keyEventObserver)
{
	if(keyEventObserver == NULL)
	{
		mKeyEventObserverList.clear();
		return;
	}
	mKeyEventObserverList.remove(keyEventObserver);
}
void SE_InputManager::removeMotionEventObserver(SE_Object* motionEventObserver)
{
	if(motionEventObserver == NULL)
	{
		mMotionEventObserverList.clear();
		return;
	}
	mMotionEventObserverList.remove(motionEventObserver);
}

SE_Object * SE_InputManager::getCurrentMotionEventObserve()
{
    SE_Object *motionEventObserver = mMotionEventObserverList.back();
    return motionEventObserver;    
}
*/
