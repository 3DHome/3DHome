#ifdef NDK
#include <jni.h>
#include <string>
#include <vector>
#else
#endif
#include "SE_Application.h"
#include "SE_Spatial.h"
#include "SE_CommonNode.h"
#include "SE_Camera.h"
#include "SE_SpatialTravel.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_ID.h"
#include "SE_Light.h"
#include "SE_Log.h"
#include "SE_Common.h"
#include "SE_ObjectManager.h"
//#include "SE_SpatialManager.h"
static jfieldID sceneNameID = 0;

SE_Scene* findScene(const char* sceneName);

std::vector<std::string> split(std::string str,std::string pattern);

static void se_createScene(JNIEnv* env, jobject obj)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);

    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if (!scene) {
       scene = new SE_Scene(sceneName8);
       SE_Application::getInstance()->getSceneManager()->pushBack(SE_FRAMEBUFFER_SCENE, scene);
       SE_Camera* camera = new SE_Camera();
       scene->inflate("assets/base/scene_resource.xml");
       scene->setCamera(camera);
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

static void se_releaseScene(JNIEnv* env, jobject obj)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Application::getInstance()->getSceneManager()->removeScene(sceneName8, true);
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}
static jint se_loadResource(JNIEnv* env, jobject obj, jstring scenePath, jstring dataPath) {
    //Invoke by load thread
    //All resource will be insert in a loader,not resource manager
    SE_ResourceManager* loader = new SE_ResourceManager();
    const char* scenePath8 = env->GetStringUTFChars(scenePath, NULL);
    const char* dataPath8 = env->GetStringUTFChars(dataPath, NULL);
    SE_Spatial* s = loader->loadScene(scenePath8);
    s->setCBFLoader(loader);
    loader->loadBaseData(NULL, dataPath8);
    env->ReleaseStringUTFChars(dataPath, dataPath8);
    env->ReleaseStringUTFChars(scenePath, scenePath8);
    return (int)s;
}
static void se_deleteResource(JNIEnv* env, jobject obj, jint resource) {
	SE_Spatial* s = (SE_Spatial*)resource;
	if (s) {
       delete s;
	}
}
static void se_inflateResource(JNIEnv* env, jobject obj, jint resource, jstring objName, jint objIndex, jstring nodeName, jint nodeIndex, jboolean removeTopNode)
{
    //Invoke by gl thread
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    const char* nodeName8 = env->GetStringUTFChars(nodeName, NULL);
    const char* objName8 = env->GetStringUTFChars(objName, NULL);
    SE_Scene* scene =  SE_Application::getInstance()->getSceneManager()->getScene(SE_FRAMEBUFFER_SCENE, SE_StringID(sceneName8));
    SE_Spatial* node = SE_Application::getInstance()->getSceneManager()->findSpatialByName(nodeName8, nodeIndex);
    SE_Spatial* s = (SE_Spatial*)resource;
    if (!node)
    {
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        env->ReleaseStringUTFChars(nodeName, nodeName8);
        env->ReleaseStringUTFChars(objName, objName8);
        if (s) {
            delete s;
        }
        return;
    }
    if (s) {
        SE_ResourceManager* m = SE_Application::getInstance()->getResourceManager();
        SE_ResourceManager* loader = s->getCBFLoader();
        m->copy(loader);
        s->deleteCBFLoader();

        if (removeTopNode) {
            if (s->getChildByIndex(0)->getChildrenNum() >= 0) {
                s->getChildByIndex(0)->setSpatialName(objName8);
                s->getChildByIndex(0)->setCloneIndex(objIndex);
                s->replaceChildParent(node);
                node->setCloneIndex(objIndex);
                scene->inflate();
                node->updateWorldTransform();
                node->updateBoundingVolume();
                node->updateWorldLayer();
                delete s;
            } else {
                s->setSpatialName(objName8);
                s->setCloneIndex(objIndex);
                scene->addSpatial(node, s);
                scene->inflate();
                node = scene->getRoot();
                node->updateWorldTransform();
                node->updateBoundingVolume();
                node->updateWorldLayer();
            }
        } else {
            std::string modelName = std::string(objName8) + "_model";
            if (s->getChildByIndex(0)->getChildrenNum() >= 0) {
                s->setSpatialName(objName8);
                s->setCloneIndex(objIndex);
                s->getChildByIndex(0)->setSpatialName(modelName.c_str());
                s->getChildByIndex(0)->setCloneIndex(objIndex);
                scene->addSpatial(node, s);
            } else {
                SE_CommonNode* news = new SE_CommonNode(SE_ID::createSpatialID());
                news->setSpatialName(objName8);
                news->setCloneIndex(objIndex);
                scene->addSpatial(node, news);
                scene->addSpatial(news, s);
                s->setSpatialName(modelName.c_str());
                s->setCloneIndex(objIndex);
            }
            scene->inflate();
            node = scene->getRoot();
            node->updateWorldTransform();
            node->updateBoundingVolume();
            node->updateWorldLayer();
        }

    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
    env->ReleaseStringUTFChars(nodeName, nodeName8);
    env->ReleaseStringUTFChars(objName, objName8);
}

std::vector<std::string> split(std::string str,std::string pattern)
{
    std::string::size_type pos;
    std::vector<std::string> result;
    str += pattern;
    int size = str.size();

    for(int i = 0; i < size; i ++)
    {
        pos = str.find(pattern,i);
        if(pos < size)
        {
            std::string s = str.substr(i, pos-i);
            result.push_back(s);
            i = pos + pattern.size() - 1;
        }
    }
    return result;
}

static void se_setRoot(JNIEnv* env, jobject obj, jstring rootName, jint index)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if (scene)
    {
        const char* rootName8 = env->GetStringUTFChars(rootName, 0);
        SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->findSpatialByName(rootName8, index);
        if (!spatial) {
            spatial = new SE_CommonNode(SE_ID::createSpatialID());
            spatial->setIsEntirety(true);
            spatial->setSpatialName(rootName8);
            spatial->setCloneIndex(index);
            //save spatial name to map
            /*SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            sm->set(spatial->getSpatialName(),spatial);*/
            spatial->updateWorldLayer();
            spatial->updateWorldTransform();
            spatial->updateBoundingVolume();
        } 
        scene->setRoot(spatial);
        env->ReleaseStringUTFChars(rootName, rootName8);
        
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}
static void se_setRoot_II(JNIEnv* env, jobject obj, jint object)
{
    if (object > 0)
    {
        jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
        const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
        SE_Scene* scene = findScene(sceneName8);
        if (scene)
        {
            scene->setRoot((SE_Spatial*)object);
        }
        env->ReleaseStringUTFChars(sceneName, sceneName8);
    }
}
static void se_setNeedDraw(JNIEnv* env, jobject obj, jboolean need)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    if (need) {
         SE_Scene* scene = SE_Application::getInstance()->getSceneManager()->getSceneFromRemovedList(SE_FRAMEBUFFER_SCENE, sceneName8, true);
         if (scene)
         {
             SE_Application::getInstance()->getSceneManager()->pushBack(SE_FRAMEBUFFER_SCENE, scene);
         }
    } else {
         SE_Scene* scene = findScene(sceneName8);
         if (scene) {
              SE_Application::getInstance()->getSceneManager()->removeScene(scene, false);
         }
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}         

static void se_setVisibility(JNIEnv* env, jobject obj, jboolean visibility)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if (scene)
    {
        if (visibility) {
            scene->setVisibility(SE_VISIBLE);
        } else {
            scene->setVisibility(SE_NOVISIBLE);
        }
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);

}

static void se_setIsTranslucent(JNIEnv* env, jobject obj, jboolean translucent)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if (scene)
    {
        scene->setIsTranslucent(translucent);
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);

}

static void se_setHelperCamera(JNIEnv* env, jobject obj,jfloatArray location, jfloatArray axisZ, jfloatArray up, jfloat fov, jfloat ratio, float near, float far)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if(!scene)
    {
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        return;
    }
    SE_Camera* camera = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);
    if(!camera)
    {
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        return;
    }   
 if(_DEBUG)
            LOGD("BORQS## se_setHelperCamera get camera ok!!### \n"); 
    float* cLocation = env->GetFloatArrayElements(location, 0);
    float* cAxisZ = env->GetFloatArrayElements(axisZ, 0);
    float* cUp = env->GetFloatArrayElements(up, 0);
    
    SE_Vector3f mLocation = SE_Vector3f(cLocation[0],cLocation[1],cLocation[2]);
    SE_Vector3f mAxisZ = SE_Vector3f(cAxisZ[0],cAxisZ[1],cAxisZ[2]);
    SE_Vector3f mUp = SE_Vector3f(cUp[0],cUp[1],cUp[2]);

    camera->create(mLocation, mAxisZ, mUp, fov, ratio, near, far);
    
    env->ReleaseFloatArrayElements(location, cLocation, 0);
    env->ReleaseFloatArrayElements(axisZ, cAxisZ, 0);
    env->ReleaseFloatArrayElements(up, cUp, 0);
    env->ReleaseStringUTFChars(sceneName, sceneName8);
    
}

static void se_addLightToScene(JNIEnv* env, jobject obj, jstring lightName, jfloatArray lightpos, jfloatArray lightdir,jfloatArray spotdata,jint lighttype)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if(scene)
    {
        float* lpos = env->GetFloatArrayElements(lightpos, 0);
        float* ldir = env->GetFloatArrayElements(lightdir, 0);
        float* lspotdata = env->GetFloatArrayElements(spotdata, 0);
        SE_Vector3f pos = SE_Vector3f(lpos[0],lpos[1],lpos[2]);
        SE_Vector3f dir = SE_Vector3f(ldir[0],ldir[1],ldir[2]);
        SE_Light::LightType lt = (SE_Light::LightType)lighttype;
        SE_Light* light = new SE_Light();
        float att = lpos[3];
        float spot_cutoff = lspotdata[0];
        float spot_exp = lspotdata[1];
        light->setLightType(lt); 
        light->setLightPos(pos);
        light->setAttenuation(att);//point attenuation from 0 to 1.0, 0 means no attenuation
        light->setLightDir(dir);
        const char* lightName8 = env->GetStringUTFChars(lightName, 0);
        light->setLightName(lightName8);
        light->setSpotLightCutOff(spot_cutoff);
        light->setSpotLightExp(spot_exp);
        light->setDirLightStrength(1.0);
        scene->addLightToScene(light);
        env->ReleaseFloatArrayElements(lightpos, lpos, 0);
        env->ReleaseFloatArrayElements(lightdir, ldir, 0);
        env->ReleaseStringUTFChars(lightName, lightName8);
        env->ReleaseFloatArrayElements(spotdata, ldir, 0);
        
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

static void se_removeLightFromScene(JNIEnv* env, jobject obj, jstring lightName) {
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if(scene)
    {
        const char* lightName8 = env->GetStringUTFChars(lightName, 0);
        scene->removeLight(lightName8);
        env->ReleaseStringUTFChars(lightName, lightName8);
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

static void se_setLowestBrightness(JNIEnv* env, jobject obj, jfloat lowestBrightnesss)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if(scene)
    {
        scene->setLowestEnvBrightness(lowestBrightnesss);        
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

/**
 *set scene manager
 *
 *@type the type of scene manager
*/
static void se_setSceneType(JNIEnv* env, jobject obj, jint type)
{
    jstring sceneName = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Scene* scene = findScene(sceneName8);
    if(scene)
    {      
        scene->setSceneManagerType(type);
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);    
}

static const char *classPathName = "com/borqs/se/engine/SEScene";

static JNINativeMethod methods[] = {
    {"create_JNI", "()V", (void*)se_createScene},
    {"setRoot_JNI", "(Ljava/lang/String;I)V", (void*)se_setRoot},
    {"setRoot_JNI", "(I)V", (void*)se_setRoot_II},
    {"loadResource_JNI", "(Ljava/lang/String;Ljava/lang/String;)I", (void*)se_loadResource},
    {"deleteResource_JNI", "(I)V", (void*)se_deleteResource},
    {"inflateResource_JNI", "(ILjava/lang/String;ILjava/lang/String;IZ)V", (void*)se_inflateResource},
    {"setNeedDraw_JNI", "(Z)V", (void*)se_setNeedDraw},
    {"setIsTranslucent_JNI", "(Z)V", (void*)se_setIsTranslucent},
    {"release_JNI", "()V", (void*)se_releaseScene},
    {"setShadowMapCamera_JNI", "([F[F[FFFFF)V", (void*)se_setHelperCamera},
    {"addLightToScene_JNI", "(Ljava/lang/String;[F[F[FI)V", (void*)se_addLightToScene},
    {"removeLightFromScene_JNI", "(Ljava/lang/String;)V", (void*)se_removeLightFromScene},
    {"setLowestBrightness_JNI", "(F)V", (void*)se_setLowestBrightness},
    {"setSceneType_JNI", "(I)V", (void*)se_setSceneType},
    {"setVisibility_JNI", "(Z)V", (void*)se_setVisibility},
};

static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        fprintf(stderr,
            "Native registration unable to find class '%s'\n", className);
        return JNI_FALSE;
    }
    sceneNameID = env->GetFieldID(clazz, "mSceneName", "Ljava/lang/String;");
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

int register_com_android_se_scene(JNIEnv* env)
{
    return registerNatives(env);
}
