#include "SE_DynamicLibType.h"
#include<vector>
#include "SE_SystemCommand.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_SceneManager.h"
#ifdef WIN32
#include "SE_Ase.h"
#endif
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Camera.h"
#include "SE_Quat.h"
#include "SE_CommonNode.h"
#include "SE_Geometry3D.h"
#include "SE_SpatialTravel.h"
#include "SE_InputManager.h"
#include "SE_InputEvent.h"
#include "SE_MotionEventCamera.h"
#include "SE_MotionEventController.h"
#include "SE_SceneManager.h"
#include "SE_UserCommand.h"

#include "SE_ThreadManager.h"
#include "SE_LoadThread.h"
#include "SE_Log.h"
#include "SE_RenderSystemCapabilities.h"
#include "SE_MemLeakDetector.h"
SE_InitAppCommand::SE_InitAppCommand(SE_Application* app) : SE_Command(app)
{}
SE_InitAppCommand::~SE_InitAppCommand()
{}
void SE_InitAppCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    std::string inPath = dataPath + SE_SEP + fileName + ".ASE";
    std::string outPath = dataPath + SE_SEP + fileName;
   
    //Do not use this command create cbf file, instead use SE_CreateCBFCommand 
#if 0
    ASE_Loader loader;
    loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "LOD1_clock.ase";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "test.ASE";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + fileName + ".SEINFO";
    //loader.Load(inPath.c_str(), 0);
   

    //inPath = dataPath + SE_SEP + "phone_base.ASE";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "phone_receiver.ASE";
    //loader.Load(inPath.c_str(), 0);


    //inPath = dataPath + SE_SEP + "laptop.ASE";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "book.ASE";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "mesh.ASE";
    //loader.Load(inPath.c_str(), 0);
    //inPath = dataPath + SE_SEP + "camera.ASE";
//    loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "book.ASE";
    //loader.Load(inPath.c_str(), 0);
   

    //inPath = dataPath + SE_SEP + "bone.txt";
    //loader.Load(inPath.c_str(), 0);

    inPath = dataPath + SE_SEP + "rendererdefine.ASE";
    loader.Load(inPath.c_str(), 0);

    inPath = dataPath + SE_SEP + "shaderdefine.ASE";
    loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "ttt.ASE";
    //loader.Load(inPath.c_str(), 0);
   
    loader.LoadEnd();
    loader.Write(dataPath.c_str(), outPath.c_str());
#endif 
   
    SE_ResourceManager* resourceManager = mApp->getResourceManager();
    resourceManager->setDataPath(dataPath.c_str());
   
}

////////////////////////////////////////////////////////

////////////////////////////////////////////////////////
SE_KeyEventCommand::SE_KeyEventCommand(SE_Application* app) : SE_Command(app)
{
    keyEvent = NULL;
}
SE_KeyEventCommand::~SE_KeyEventCommand()
{
    if(keyEvent)
        delete keyEvent;
}
void SE_KeyEventCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_InputManager* inputManager = mApp->getInputManager();
    inputManager->update(keyEvent);
}

SE_MotionEventCommand::SE_MotionEventCommand(SE_Application* app) : SE_Command(app)
{
    motionEvent = NULL;
}
SE_MotionEventCommand::~SE_MotionEventCommand()
{
    if(motionEvent)
        delete motionEvent;
}
void SE_MotionEventCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_InputManager* inputManager = mApp->getInputManager();
    inputManager->update(motionEvent);
}
//////////////
SE_LoadSceneCommand::SE_LoadSceneCommand(SE_Application* app) : SE_Command(app)
{}
void SE_LoadSceneCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    if(sceneName == "")
        return;
    SE_ResourceManager* resourceManager = mApp->getResourceManager();
    resourceManager->loadBaseData(NULL, sceneName.c_str());
    SE_SceneManager* sceneManager = mApp->getSceneManager();
	SE_Scene* scene = new SE_Scene(sceneName.c_str());
    scene->createRoot(sceneName.c_str());
    SE_Spatial* rootScene = scene->getRoot();
    rootScene->updateWorldTransform();
    rootScene->updateBoundingVolume();   
	sceneManager->pushBack(scene->getType(), scene);
}

////////////////////////////////////////////////////////
/*
SE_MoveCameraCommand::SE_MoveCameraCommand(SE_Application* app) : SE_Command(app)
{
}
SE_MoveCameraCommand::~SE_MoveCameraCommand()
{}
void SE_MoveCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Vector3f startLocation = camera->getLocation();
    camera->rotateLocal(rotateAngle, axis);
    camera->translateLocal(translate);
    SE_Vector3f endLocation = camera->getLocation();
    SE_Sphere sphere;
    sphere.set(startLocation, 2);
    SE_SceneManager* sceneManager = mApp->getSceneManager();
    SE_Spatial* rootScene = sceneManager->getRoot();
    SE_MovingSphereStaticSpatialIntersect moveTravel(sphere, endLocation);
    rootScene->travel(&moveTravel, true);
    if(moveTravel.intersected)
    {
        camera->setLocation(moveTravel.location);
    }
}
*/
///////////////////////////////////camera ralates////////////////////
SE_InitCameraCommand::SE_InitCameraCommand(SE_Application* app) : SE_Command(app)
{}
SE_InitCameraCommand::~SE_InitCameraCommand()
{}
#if defined (WIN32)
void SE_InitCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
    {
    //SE_Vector3f location(0,3,1.5);//man
    SE_Vector3f location(0,-200,100);//home
	SE_Vector3f zAxis(0, -1, 0);	
	SE_Vector3f up(0, 0, 1);
	SE_Camera* c = mApp->getCurrentCamera();
	c->create(location, zAxis, up, 60.0f,((float)height)/ width, 1.0f, 5000.0f);
    c->setViewport(0, 0, width, height);
}
#else
void SE_InitCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Camera* c = mApp->getCurrentCamera();
    c->create(mLocation, mAxisZ, mUp, mFov,((float)height)/ width, mNear, mFar);
    c->setViewport(0, 0, width, height);
}
#endif


#if defined (WIN32)
SE_UpdateCameraCommand::SE_UpdateCameraCommand(SE_Application* app) : SE_Command(app)
{}
SE_UpdateCameraCommand::~SE_UpdateCameraCommand()
{}
void SE_UpdateCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Camera* c = mApp->getCurrentCamera();
    if(c)
    {
        SE_Vector3f location = c->getLocation();
        SE_Vector3f zAxis = c->getAxisZ();
        SE_Vector3f up(0, 0, 1);        
        c->create(location, zAxis, up, 60.0f,((float)height)/ width, 1.0f, 1000.0f);
        c->setViewport(0, 0, width, height);  
    }
}
#else
SE_UpdateCameraCommand::SE_UpdateCameraCommand(SE_Application* app) : SE_Command(app)
{}
SE_UpdateCameraCommand::~SE_UpdateCameraCommand()
{}
void SE_UpdateCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Camera* c = mApp->getCurrentCamera();
    if(c)
    {
       SE_Vector3f location = c->getLocation();
       SE_Vector3f zAxis = c->getAxisZ();
       SE_Vector3f up = c->getAxisY();
       c->create(location, zAxis, up, mFov,((float)mHeight)/ mWidth, mNear, mFar);
       c->setViewport(0, 0, mWidth, mHeight);
    }
}
#endif

SE_SetCameraCommand::SE_SetCameraCommand(SE_Application* app) : SE_Command(app)
{
    mFlag = false;
}
SE_SetCameraCommand::~SE_SetCameraCommand()
{}
void SE_SetCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    if (mFlag) {
        mCamera->create(mLocation, mTarget, mFov, mRatio, mNear, mFar);
    } else {
    mCamera->create(mLocation, mAxisZ, mUp, mFov, mRatio, mNear, mFar);
    }
}

SE_SetViewportCommand::SE_SetViewportCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetViewportCommand::~SE_SetViewportCommand()
{}
void SE_SetViewportCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    mCamera->setViewport(mX, mY, mW, mH);
}

SE_SetFrustumCommand::SE_SetFrustumCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetFrustumCommand::~SE_SetFrustumCommand()
{}

void SE_SetFrustumCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
	mCamera->setFrustum(mFov, mRatio, mNear, mFar);
}

SE_RotateLocalCommand_I::SE_RotateLocalCommand_I(SE_Application* app) : SE_Command(app)
{
}
SE_RotateLocalCommand_I::~SE_RotateLocalCommand_I()
{}
void SE_RotateLocalCommand_I::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    mCamera->rotateLocal(mRotate);
}

SE_RotateLocalCommand_II::SE_RotateLocalCommand_II(SE_Application* app) : SE_Command(app)
{
}
SE_RotateLocalCommand_II::~SE_RotateLocalCommand_II()
{}
void SE_RotateLocalCommand_II::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("## rotate camera ###mAngle =%f,  mAxis=%d\n",mAngle, mAxis);
    mCamera->rotateLocal(mAngle, mAxis);
}

SE_TranslateLocalCommand::SE_TranslateLocalCommand(SE_Application* app) : SE_Command(app)
{
}
SE_TranslateLocalCommand::~SE_TranslateLocalCommand()
{}
void SE_TranslateLocalCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_Vector3f startLocation = mCamera->getLocation();
    mCamera->translateLocal(mTranslate);
    SE_Vector3f endLocation = mCamera->getLocation();
    SE_Sphere sphere;
    sphere.set(startLocation, 5);
    SE_SceneManager* sceneManager = mApp->getSceneManager();
    SE_Spatial* rootScene = sceneManager->getMainScene()->getRoot();
    SE_MovingSphereStaticSpatialIntersect moveTravel(sphere, endLocation);
    rootScene->travel(&moveTravel, true);
    if(moveTravel.intersected)
    {
        mCamera->setLocation(moveTravel.location);
    }
}

SE_SetLocationCommand::SE_SetLocationCommand(SE_Application* app) : SE_Command(app)
{
}
SE_SetLocationCommand::~SE_SetLocationCommand()
{}
void SE_SetLocationCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    mCamera->setLocation(mLocation);
}

SE_CreateSceneCommand::SE_CreateSceneCommand(SE_Application* app) : SE_Command(app)
{
}
SE_CreateSceneCommand::~SE_CreateSceneCommand()
{}
void SE_CreateSceneCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    //create scene and load resource.
    SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();

    SE_Scene* mainScene = sceneManager->getMainScene();

    if(!mainScene)
    {
        mainScene = new SE_Scene(fileName.c_str()); 

        if(!xmlpath.empty())
        {
            mainScene->inflate(xmlpath.c_str());
        }
        sceneManager->pushBack(mainScene->getType(), mainScene);
        sceneManager->setMainScene(fileName.c_str());
    }

#ifndef EDITOR
    SE_Camera* camera = mainScene->getCamera();

    if(!camera)
    {
        camera = new SE_MotionEventCamera();
        mainScene->setCamera(camera);
    }
    
#endif
    mainScene->loadResource(dataPath.c_str(),fileName.c_str());
}

SE_CreateCBFCommand::SE_CreateCBFCommand(SE_Application* app) : SE_Command(app)
{
    averageNormal = false;
}
SE_CreateCBFCommand::~SE_CreateCBFCommand()
{}
void SE_CreateCBFCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#ifdef WIN32


#ifdef ASE
    std::string inPath;
    ASE_Loader loader;
    
    for(int i = 0; i < sceneDataFile.size(); ++i)
    {        
        inPath = dataPath + SE_SEP + sceneDataFile[i] + ".ASE";
        loader.Load(inPath.c_str(), 0,true);

        inPath = dataPath + SE_SEP + "rendererdefine.ASE";
        loader.Load(inPath.c_str(), 0);

        inPath = dataPath + SE_SEP + "shaderdefine.ASE";
    loader.Load(inPath.c_str(), 0);

        loader.LoadEnd();
    }
    std::string outPath = dataPath + SE_SEP + sceneDataFile[0];    

    //inPath = dataPath + SE_SEP + "LOD1_clock.ase";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + "test.ASE";
    //loader.Load(inPath.c_str(), 0);

    //inPath = dataPath + SE_SEP + fileName + ".SEINFO";
    //loader.Load(inPath.c_str(), 0);
    

    loader.Write(dataPath.c_str(), outPath.c_str(),averageNormal);
#endif
#endif
}

SE_SceneInflateCommand::SE_SceneInflateCommand(SE_Application* app) : SE_Command(app)
{
    loader = NULL;
    child = NULL;
}
SE_SceneInflateCommand::~SE_SceneInflateCommand()
{}
void SE_SceneInflateCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_SceneManager::_SceneSet sset = sceneManager->getScene(sceneName.c_str());

    
    SE_Scene * scene = sceneManager->getMainScene();
        if(!scene)
        {
        if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("Not found scene!!!!! Load resource fail!!! scene name = %s\n", sceneName.c_str());
            return;
        }

    SE_Spatial* root = scene->getRoot();
    if(!root)
    {
        root = new SE_CommonNode();
        scene->setRoot(root);
        root->setSpatialName(sceneName.c_str());
    }

        loader = SE_Application::getInstance()->popLoader();
        resourceManager->copy(loader);
        delete loader;
        //load finish post insert command
        child->replaceChildParent(root); 
        delete child;
                
        root->updateWorldLayer();
        root->updateRenderState();
    
        scene->inflate();

}
