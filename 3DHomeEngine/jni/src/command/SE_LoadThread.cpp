#include "SE_DynamicLibType.h"
#include "SE_LoadThread.h"
#include "SE_ResourceManager.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"

#include "SE_Spatial.h"
#include "SE_RenderState.h"
#include "SE_Log.h"
#include "SE_MotionEventCamera.h"
#include "SE_Common.h"
#include "SE_SystemCommand.h"
#include "SE_CommonNode.h"
#include "SE_UserCommand.h"

#include "SE_MemLeakDetector.h"
SE_LoadThread::SE_LoadThread(SE_ResourceManager* rs, const std::string& p,const std::string& f)
{
    resourceLoader = rs;
    fileName = f;
    path = p;
}
void SE_LoadThread::run()
{
    resourceLoader->setDataPath(path.c_str());
    std::string dataPath = path;
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### data path = %s ####", dataPath.c_str());
    if(dataPath != "")
    {
        SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();


        //For multi-scene load
        SE_Scene* scene = sceneManager->getMainScene();

        

        if(!scene)
        {
                if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("Not found scene!!!!! Load resource fail!!! scene name = %s\n", fileName.c_str());
            return;
        }

            //1. create scene
            SE_Spatial* root = scene->getRoot();
            if(!root)
            {

                /*SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
                sm->set(root->getSpatialName(),root);*/

                //no root 
                //scene->createRoot(fileName.c_str());
                //scene->setRoot(root);

                //1.load scene data
                if(SE_Application::getInstance()->SEHomeDebug)
                {
            LOGI("##### begin load resource ####### fileName = %s", fileName.c_str());
                }
                SE_Spatial* child = resourceLoader->loadScene(fileName.c_str());

                //2. load base date                 
                resourceLoader->loadBaseData(NULL, fileName.c_str(),"_resource");

                //push loader to list
                SE_Application::getInstance()->pushLoaderToList(resourceLoader);
                

            //3. inflate MeshSimobject             
                SE_SceneInflateCommand* sc = new SE_SceneInflateCommand(SE_Application::getInstance());
                sc->sceneName = fileName;
                sc->child = child;
                SE_Application::getInstance()->postCommand(sc); 
                    

        SE_ResourceReadyCommand* rd = new SE_ResourceReadyCommand(SE_Application::getInstance());
        SE_Application::getInstance()->postCommand(rd); 

#ifdef ANDROID
        SE_Application::getInstance()->sendMessageToJava(0, fileName.c_str()); 
#endif
                if(SE_Application::getInstance()->SEHomeDebug)
                {
        LOGI("####### load resource end ############\n");
    }

                if(0)
                {
                    std::string datapath = "E:/codebase/win_svn_base/3DHome/assets/base/pc";;//"D:\\model\\jme\\home\\newhome3";//"e:\\model\\testM";        
                    std::string filename = "pc";
    
                    SE_CreateSceneCommand *cs = (SE_CreateSceneCommand*)SE_Application::getInstance()->createCommand("SE_CreateSceneCommand");
                    cs->dataPath = datapath;
                    cs->fileName = filename;
                    SE_Application::getInstance()->postCommand(cs);
                }
    }
            else
            {

                SE_Spatial* child = resourceLoader->loadScene(fileName.c_str());
                resourceLoader->loadBaseData(NULL, fileName.c_str());
                

                root->updateWorldLayer();
                root->updateRenderState();    

                SE_SceneInflateCommand* sc = new SE_SceneInflateCommand(SE_Application::getInstance());
                sc->sceneName = scene->getName().getStr();
                sc->child = child;
                sc->loader = resourceLoader;
                SE_Application::getInstance()->postCommand(sc); 
            }
            
        
    }

}
SE_ResourceReadyCommand::SE_ResourceReadyCommand(SE_Application* app) : SE_Command(app)
{}
void SE_ResourceReadyCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    mApp->setState(RUNNING);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("#### set app running ####");
}

