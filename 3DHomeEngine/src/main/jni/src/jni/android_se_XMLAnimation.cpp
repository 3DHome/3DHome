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
#include <GLES2/gl2.h>
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_Animation.h"
#include "SE_AnimationListener.h"
#include "SE_AnimationManager.h"
static jfieldID nativeAnimationID = 0;
static jfieldID objectIndexID = 0;
static jfieldID isReversedID = 0;
static jint se_addAnimation(JNIEnv* env, jobject obj, jstring xmlFilePath)
{
    if(_DEBUG) LOGD("BORQS## add animation###");
    const char* xmlFilePath8 = env->GetStringUTFChars(xmlFilePath, 0);
    SE_Animation* a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML(xmlFilePath8);
    if (!a) {
    	return 0;
    }
    int objectIndex = env->GetIntField(obj, objectIndexID);  
    a->run(objectIndex);
    env->SetIntField(obj, nativeAnimationID, (jint)a);
    env->ReleaseStringUTFChars(xmlFilePath, xmlFilePath8);  
    return a->getFrameNum();
}

static jboolean se_isNeedRepeat(JNIEnv* env, jobject obj)
{
    int animation = env->GetIntField(obj, nativeAnimationID);
    if (animation == 0) {
        return false;
    }
    SE_Animation* a = (SE_Animation*)animation;
    return a->getRunMode() == SE_Animation::REPEAT;
}

static void se_playAnimation(JNIEnv* env, jobject obj, jint frameIndex) {
    int animation = env->GetIntField(obj, nativeAnimationID);
    if (animation == 0) {
        return;
    }
    SE_Animation* a = (SE_Animation*)animation;

    a->playFrameIndex(frameIndex);
}

static void se_removeAnimation(JNIEnv* env, jobject obj)
{
    
    if(_DEBUG) LOGD("BORQS## add animation###");
    int animation = env->GetIntField(obj, nativeAnimationID);
    if (animation == 0) {
        return;
    }
    SE_Animation* a = (SE_Animation*)animation;
    delete a;
    env->SetIntField(obj, nativeAnimationID, (jint)0);
}

static void se_setReversePlay(JNIEnv* env, jobject obj, jboolean isReversed)
{
    
    if(_DEBUG) LOGD("BORQS## add animation###");
    int animation = env->GetIntField(obj, nativeAnimationID);
    if (animation == 0) {
        return;
    }
    SE_Animation* a = (SE_Animation*)animation;
    int objectIndex = env->GetIntField(obj, objectIndexID);  
    a->run(objectIndex);
    a->setReversePlay(isReversed);
}


static const char *classPathName = "com/borqs/se/engine/SEXMLAnimation";

static JNINativeMethod methods[] = {
  {"addAnimation_JNI", "(Ljava/lang/String;)I", (void*)se_addAnimation},
  {"playAnimation_JNI","(I)V", (void*)se_playAnimation},
  {"removeAnimation_JNI", "()V", (void*)se_removeAnimation},
  {"setReversePlay_JNI", "(Z)V", (void*)se_setReversePlay},
  {"isNeedRepeat_JNI", "()Z", (void*)se_isNeedRepeat},
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
    nativeAnimationID = env->GetFieldID(clazz, "mNativeAnimation", "I");
    objectIndexID = env->GetFieldID(clazz, "mObjectIndex", "I");
    isReversedID = env->GetFieldID(clazz, "mIsReversed", "Z");
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


int register_com_android_se_XMLAnimation(JNIEnv* env)
{
    return registerNatives(env);
}
