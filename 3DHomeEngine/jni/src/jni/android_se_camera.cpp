#ifdef NDK
#include <jni.h>
#include <string>
#else
#include <nativehelper/jni.h>
#include <utils/Log.h>
#include <utils/String8.h>
#include <utils/String16.h>
#endif

#include <stdio.h>
#include <assert.h>
#include <dlfcn.h>
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_SystemCommand.h"
#include "SE_UserCommand.h"
#include "SE_Spatial.h"
#include "SE_Camera.h"
#include "SE_SpatialTravel.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_SimObject.h"
#include "SE_ID.h"
#include "SE_CameraBestPosition.h"

/********************add by liusong*************************/
static jfieldID sceneNameID = 0;
static jmethodID methodInitObject;
static jclass classObject;
SE_Scene* findScene(const char* sceneName);
/*SE_Scene* findScene(const char* sceneName)
{
    SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
    if (sceneManager) {
        for(int i = 0 ; i < SE_SCENE_TYPE_NUM ; i++)
        {
            SE_Scene* scene =  sceneManager->getScene((SE_SCENE_TYPE)i, SE_StringID(sceneName));
            if (scene)
            {
                 return scene;
            }
        }
    }
    return NULL;    
}*/

SE_Camera* findCamera(JNIEnv* env, jobject obj)
{
    jstring name = (jstring)env->GetObjectField(obj, sceneNameID);
    const char* name8 = env->GetStringUTFChars(name, 0);
    SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
    SE_Camera* camera = NULL;
    if (sceneManager) {
        SE_Scene* scene =  findScene(name8);
        if (scene) {
            camera = scene->getCamera();
        }
    }
    env->ReleaseStringUTFChars(name, name8);
    return camera;
}

static jobject se_getSelectedObject(JNIEnv* env, jobject clazz, jint X, jint Y)
{
    jstring name = (jstring)env->GetObjectField(clazz, sceneNameID);
    const char* name8 = env->GetStringUTFChars(name, 0);
    SE_Scene* scene =  findScene(name8);
    if (scene) {
        SE_Camera* camera = scene->getCamera();
        if (camera)
        {
             SE_Ray ray = camera->screenCoordinateToRay(X, Y);
             SE_Spatial* spatial = NULL;
             if (scene->getSceneManagerType() == DEFAULT)
             {    
                 SE_FindSpatialCollision spatialCollision(ray);
                 SE_Spatial* root = scene->getRoot();
                 if (root) {
                     root->travel(&spatialCollision, true);
                     spatial = spatialCollision.getCollisionSpatial();
                 }
             }
             else if (scene->getSceneManagerType() == OCTREESCENEMANAGER)
             {    
                 OctreeSceneManager* octreeSceneManager = scene->getOctreeSceneManager();
                 spatial = octreeSceneManager->travel(ray); 
             }
             if (spatial && SE_Application::getInstance()->SEHomeDebug)
                  LOGD("3DHome ## touch object ### name = %s\n", spatial->getSpatialName()); 
             while (spatial) {
                 if (spatial->isEntirety()) {
                     jobject myObject = env->NewObject(classObject, methodInitObject, env->NewStringUTF(spatial->getSpatialName()), spatial->getCloneIndex());
                     env->ReleaseStringUTFChars(name, name8);
                     return myObject;
                 }
                 spatial = spatial->getParent();
              }
        }
    }


    env->ReleaseStringUTFChars(name, name8);
    return NULL;
}


static void se_setCamera(JNIEnv* env, jobject clazz, jfloatArray location, jfloatArray axisZ, jfloatArray up, jfloat fov, jfloat ratio, float near, float far)
{
    SE_Camera* camera = findCamera(env, clazz);
    if (_DEBUG)
        LOGD("## set camera create ###\n");
    if (!camera)
        return;
    float* cLocation = env->GetFloatArrayElements(location, 0);
    float* cAxisZ = env->GetFloatArrayElements(axisZ, 0);
    float* cUp = env->GetFloatArrayElements(up, 0);
    SE_Vector3f mLocation = SE_Vector3f(cLocation[0],cLocation[1],cLocation[2]);
    SE_Vector3f mAxisZ = SE_Vector3f(cAxisZ[0],cAxisZ[1],cAxisZ[2]);
    SE_Vector3f mUp = SE_Vector3f(cUp[0],cUp[1],cUp[2]);
    SE_Rect<int> rect = camera->getViewport();
    int right = rect.right;
    int left = rect.left;
    int bottom = rect.bottom;
    int top = rect.top;
    camera->create(mLocation, mAxisZ, mUp, fov, ratio, near, far);
    camera->setViewport(left, top, right - left, bottom - top);
    env->ReleaseFloatArrayElements(location, cLocation, 0);
    env->ReleaseFloatArrayElements(axisZ, cAxisZ, 0);
    env->ReleaseFloatArrayElements(up, cUp, 0);
    
}

static void se_setCamera_II(JNIEnv* env, jobject clazz, jfloatArray location, jfloatArray target, jfloat fov, jfloat ratio, float near, float far)
{

    SE_Camera* camera = findCamera(env, clazz);
    if (_DEBUG)
        LOGD("## set camera create ###\n");
    if (!camera)
        return;
    float* cLocation = env->GetFloatArrayElements(location, 0);
    float* cTarget = env->GetFloatArrayElements(target, 0);
    SE_Vector3f mLocation = SE_Vector3f(cLocation[0],cLocation[1],cLocation[2]);
    SE_Vector3f mTarget = SE_Vector3f(cTarget[0],cTarget[1],cTarget[2]);
    camera->create(mLocation, mTarget, fov, ratio, near, far);
    env->ReleaseFloatArrayElements(location, cLocation, 0);
    env->ReleaseFloatArrayElements(target, cTarget, 0);
    
}

static void se_operateCamera(JNIEnv* env, jobject clazz, jfloatArray location, jfloatArray rotate, jboolean translate)
{
    if (_DEBUG)
        LOGD("## operate camera ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (!camera)
        return;
    float* cLocation = env->GetFloatArrayElements(location, 0);
    float* cRotate = env->GetFloatArrayElements(rotate, 0);
    SE_Vector3f mLocation = SE_Vector3f(cLocation[0],cLocation[1],cLocation[2]);
    SE_Quat q;
    q.set(cRotate[0], SE_Vector3f(cRotate[1], cRotate[2], cRotate[3]));
    if (translate)
    {
        camera->translateLocal(mLocation);
    } else
    {
        camera->setLocation(mLocation);
    }
    camera->rotateLocal(q);
    env->ReleaseFloatArrayElements(location, cLocation, 0);
    env->ReleaseFloatArrayElements(rotate, cRotate, 0);
}

static void se_getLocation(JNIEnv* env, jobject clazz, jfloatArray location)
{
    if (_DEBUG)
        LOGD("## get loaction of camera ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera)
    {
	SE_Vector3f cameraLocation = camera->getLocation();
	env->SetFloatArrayRegion(location, 0, 3, cameraLocation.d);
    } else 
    {
        if (_DEBUG)
	    LOGD("## get current camera failed!!!  ###\n");
    }
}

static void se_getAxisZ(JNIEnv* env, jobject clazz, jfloatArray axisZ)
{
    if (_DEBUG)
        LOGD("## get axisZ of camera ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera)
    {
	SE_Vector3f cameraAxisZ = camera->getAxisZ();
	env->SetFloatArrayRegion(axisZ, 0, 3, cameraAxisZ.d);
    } else 
    {
        if (_DEBUG)
	     LOGD("## get current camera failed!!!  ###\n");
    }
}

static void se_getAxisX(JNIEnv* env, jobject clazz, jfloatArray axisX)
{
    if (_DEBUG)
        LOGD("## get axisZ of camera ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera)
    {
	SE_Vector3f cameraAxisX = camera->getAxisX();
	env->SetFloatArrayRegion(axisX, 0, 3, cameraAxisX.d);
    } else 
    {
        if (_DEBUG)
	     LOGD("## get current camera failed!!!  ###\n");
    }
}

static void se_getAxisY(JNIEnv* env, jobject clazz, jfloatArray axisY)
{
    if (_DEBUG)
        LOGD("## get axisZ of camera ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera != NULL)
    {
	SE_Vector3f cameraAxisY = camera->getAxisY();
	env->SetFloatArrayRegion(axisY, 0, 3, cameraAxisY.d);
    } else 
    {
        if (_DEBUG)
	     LOGD("## get current camera failed!!!  ###\n");
    }
}

static void se_setFrustum(JNIEnv* env, jobject clazz, jfloat fov, jfloat ratio, jfloat near, jfloat far)
{
    SE_Camera* camera = findCamera(env, clazz);
    camera->setFrustum(fov, ratio, near, far);
}

static void se_rotateLocal_I(JNIEnv* env, jobject clazz, jfloatArray rotate)
{
    if (_DEBUG)
        LOGD("## rotate camera I ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (!camera)
        return;
    float* cRotate = env->GetFloatArrayElements(rotate, 0);
    SE_Quat q;
    q.set(cRotate[0], SE_Vector3f(cRotate[1], cRotate[2], cRotate[3]));
    camera->rotateLocal(q);
    env->ReleaseFloatArrayElements(rotate, cRotate, 0);
}

static void se_rotateLocal_II(JNIEnv* env, jobject clazz, jfloat angle, jint axis)
{
    if (_DEBUG)
        LOGD("## rotate camera II ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (!camera)
        return;
    camera->rotateLocal(angle, (SE_AXIS_TYPE)axis);
}

static void se_translateLocal(JNIEnv* env, jobject clazz, jfloatArray translate)
{
    if (_DEBUG)
        LOGD("## translateLocal ###\n");
    jstring name = (jstring)env->GetObjectField(clazz, sceneNameID);
    const char* name8 = env->GetStringUTFChars(name, 0);
    SE_Scene* scene =  findScene(name8);
    if (scene) {
        SE_Camera* camera = scene->getCamera();
        if (camera)
        {
            float* translate8 = env->GetFloatArrayElements(translate, 0);
            SE_Vector3f location = SE_Vector3f(translate8[0],translate8[1],translate8[2]);
            SE_Vector3f startLocation = camera->getLocation();
            camera->translateLocal(location);
            SE_Vector3f endLocation = camera->getLocation();
            SE_Sphere sphere;
            sphere.set(startLocation, 5);
            SE_Spatial* rootScene = scene->getRoot();
            SE_MovingSphereStaticSpatialIntersect moveTravel(sphere, endLocation);
            rootScene->travel(&moveTravel, true);
            if(moveTravel.intersected)
            {
               camera->setLocation(moveTravel.location);
            }
            env->ReleaseFloatArrayElements(translate, translate8, 0);
        }
    }
    env->ReleaseStringUTFChars(name, name8);
}

static void se_setLocation(JNIEnv* env, jobject clazz, jfloatArray location)
{
    if (_DEBUG)
        LOGD("## setLocation ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (!camera)
        return;
    float* cLocation = env->GetFloatArrayElements(location, 0);
    SE_Vector3f mLocation = SE_Vector3f(cLocation[0],cLocation[1],cLocation[2]);
    camera->setLocation(mLocation);
    env->ReleaseFloatArrayElements(location, cLocation, 0);


}

static void se_setViewport(JNIEnv* env, jobject clazz, jint x, jint y, jint w, jint h)
{
    if (_DEBUG)
        LOGD("## setViewport ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (!camera)
        return;
    camera->setViewport(x, y, w, h);
}

static void se_screenCoordinateToRay(JNIEnv* env, jobject clazz, jint x, jint y, jfloatArray ray)
{
    if (_DEBUG)
        LOGD("## screenCoordinateToRay ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera)
    {
	SE_Ray cameraRay = camera->screenCoordinateToRay(x, y);
	env->SetFloatArrayRegion(ray, 0, 3, cameraRay.getOrigin().d);
        env->SetFloatArrayRegion(ray, 3, 3, cameraRay.getDirection().d);
    } else 
    {
        if (_DEBUG)
	     LOGD("## get current camera failed!!!  ###\n");
    } 
}

static jfloatArray se_worldToScreenCoordinate(JNIEnv* env, jobject clazz, jfloatArray inValues)
{
    if (_DEBUG)
        LOGD("## worldToScreenCoordinate ###\n");
    SE_Camera* camera = findCamera(env, clazz);
    if (camera)
    {
        float* jinValues = env->GetFloatArrayElements(inValues, 0);
        SE_Vector4f in = SE_Vector4f(jinValues[0], jinValues[1], jinValues[2], 1);
        env->ReleaseFloatArrayElements(inValues, jinValues, 0);
        SE_Matrix4f m = camera->getPerspectiveMatrix().mul(camera->getWorldToViewMatrix());
        SE_Vector4f out = m.map(in);
        SE_Vector2f result;
        result.x = out.x / out.w;
        result.y = out.y / out.w;
        SE_Rect<int> viewport = camera->getViewport();
        int width = viewport.right - viewport.left;
        int height = viewport.bottom - viewport.top;
        result.x = (result.x + 1) * width / 2;
        result.y = (1 - result.y) * height / 2;
        jfloatArray outValuesArray = env->NewFloatArray(2);
        env->SetFloatArrayRegion(outValuesArray, 0, 2, result.d);
        return outValuesArray;
    }
    return NULL;
}

static int _CameraSize;
static int _Count = 0;
static jstring se_getBestPosition(JNIEnv* env, jobject clazz, jfloatArray pos, jfloatArray targetPos)
{
    SE_CameraPositionList* cp = SE_Application::getInstance()->getResourceManager()->getCameraPositionList(SE_CAMERABESTPOSITION);
    if (!cp) {
        return NULL;
    }
    if(_Count == 0)
        _CameraSize = cp->mPositions.size();
    if(_Count < _CameraSize) {
	SE_CameraBestPosition* bp = cp->mPositions[_Count];
	_Count ++;
	env->SetFloatArrayRegion(pos, 0, 3,bp->mCameraPos.d);
	env->SetFloatArrayRegion(targetPos, 0, 3,bp->mCameraTargetPos.d);
	return env->NewStringUTF(bp->mCamraName.c_str());
    }
    return NULL;
}
static const char *classPathName = "com/borqs/se/engine/SECamera";

static JNINativeMethod methods[] = {
  {"getLocation_JNI", "([F)V", (void*)se_getLocation},
  {"getAxisZ_JNI", "([F)V", (void*)se_getAxisZ},
  {"getAxisX_JNI", "([F)V", (void*)se_getAxisX},
  {"getAxisY_JNI", "([F)V", (void*)se_getAxisY},
  {"setCamera_JNI", "([F[F[FFFFF)V", (void*)se_setCamera},
  {"setCamera_JNI", "([F[FFFFF)V", (void*)se_setCamera_II},
  {"setFrustum_JNI", "(FFFF)V", (void*)se_setFrustum},
  {"rotateLocal_JNI", "([F)V", (void*)se_rotateLocal_I},
  {"rotateLocal_JNI", "(FI)V", (void*)se_rotateLocal_II},
  {"translateLocal_JNI", "([F)V", (void*)se_translateLocal},
  {"setLocation_JNI", "([F)V", (void*)se_setLocation},
  {"operateCamera_JNI", "([F[FZ)V", (void*)se_operateCamera},
  {"setViewport_JNI", "(IIII)V", (void*)se_setViewport},
  {"getBestPosition_JNI", "([F[F)Ljava/lang/String;", (void*)se_getBestPosition},
  {"screenCoordinateToRay_JNI","(II[F)V", (void*)se_screenCoordinateToRay},
  {"worldToScreenCoordinate_JNI","([F)[F", (void*)se_worldToScreenCoordinate},
  {"getSelectedObject_JNI", "(II)Lcom/borqs/se/engine/SEObject;", (void*)se_getSelectedObject},
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

    classObject =  env->FindClass("com/borqs/se/engine/SEObject");
    classObject = (jclass)env->NewGlobalRef(classObject);
    methodInitObject = env->GetMethodID(classObject, "<init>", "(Ljava/lang/String;I)V");


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


int register_com_android_se_SECamera(JNIEnv* env)
{
    return registerNatives(env);
}



