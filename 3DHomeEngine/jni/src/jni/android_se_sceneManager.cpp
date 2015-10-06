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
#include <GLES2/gl2ext.h>
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
#include "SE_SpatialManager.h"
#include "SE_ImageData.h"
#include "SE_ImageCodec.h"
#include "SE_IO.h"
#include "SE_File.h"
#include "SE_Utils.h"
#include "SE_SystemCommandFactory.h"
#include "SE_SystemCommand.h"
#include "SE_AssetManager.h"
#include "SE_RenderSystemCapabilities.h"
#include "SE_Ase.h"

int register_com_android_se_SECamera(JNIEnv* env);
int register_com_android_se_SEObject(JNIEnv* env);
int register_com_android_se_scene(JNIEnv* env);
int register_com_android_se_SEParticleSystem(JNIEnv* env);
int register_com_android_se_XMLAnimation(JNIEnv* env);

static bool DEBUGE_OBJECT = true;
static const char *classPathName = "com/borqs/se/engine/SESceneManager";
static jclass bufferClass;
static jclass nioAccessClass;
static jmethodID getBasePointerID;
static jmethodID getBaseArrayID;
static jmethodID getBaseArrayOffsetID;
static jfieldID positionID;
static jfieldID limitID;
static jfieldID elementSizeShiftID;
static SE_Application* gApp = NULL;
static JavaVM* mJvm = 0;
static jobject mjavaApp;
static jmethodID method_javaCallback;
static jfieldID nativeAssetManagerID = 0;
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

static void *getPointer(JNIEnv *_env, jobject buffer, jarray *array, jint *remaining)
{
    jint position;
    jint limit;
    jint elementSizeShift;
    jlong pointer;
    jint offset;
    void *data;
    position = _env->GetIntField(buffer, positionID);
    limit = _env->GetIntField(buffer, limitID);
    elementSizeShift = _env->GetIntField(buffer, elementSizeShiftID);
    *remaining = (limit - position) << elementSizeShift;
    pointer = _env->CallStaticLongMethod(nioAccessClass,
            getBasePointerID, buffer);
    if (pointer != 0L) {
        *array = NULL;
        return (void *) (jint) pointer;
    }
    *array = (jarray) _env->CallStaticObjectMethod(nioAccessClass,
            getBaseArrayID, buffer);
    offset = _env->CallStaticIntMethod(nioAccessClass,
            getBaseArrayOffsetID, buffer);
    data = _env->GetPrimitiveArrayCritical(*array, (jboolean *) 0);

    return (void *) ((char *) data + offset);
}

static void
releasePointer(JNIEnv *_env, jarray array, void *data, jboolean commit)
{
    _env->ReleasePrimitiveArrayCritical(array, data,
                       commit ? 0 : JNI_ABORT);
}

static void se_setAsMainScene(JNIEnv* env, jobject obj, jstring sceneName)
{
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    SE_Application::getInstance()->getSceneManager()->setMainScene(sceneName8);
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

static void se_savePixelsData(JNIEnv *env, jobject obj, jstring savePath, jstring imageKey) {
    const char* imageKey8 = env->GetStringUTFChars(imageKey, 0);
    const char* savePath8 = env->GetStringUTFChars(savePath, 0);
    SE_ImageDataID imageDataid(imageKey8);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_ImageData* imgd = resourceManager->getImageData(imageDataid);
    if (imgd) {
        SE_File fbase(savePath8, SE_File::WRITE);
        fbase.write(imgd->getData(), imgd->getHeight() * imgd->getBytesPerRow());
        LOGI("SEHome:#################savePixelsData success!!!");
    }
    env->ReleaseStringUTFChars(imageKey, imageKey8);
    env->ReleaseStringUTFChars(savePath, savePath8);
}
static jboolean se_loadPixelsData(JNIEnv *env, jobject obj, jstring savePath, jstring imageKey, jint w, jint h) {
    const char* imageKey8 = env->GetStringUTFChars(imageKey, 0);
    const char* savePath8 = env->GetStringUTFChars(savePath, 0);
    SE_ImageDataID imageDataid(imageKey8);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_ImageData* imgd = resourceManager->getImageData(imageDataid);
    if (!imgd) {
        char* data = NULL;
        int len = 0;
        SE_IO::readFileAll(savePath8, data, len);
        if(len != 0) {
            int power2Width = w;
            int power2Height = h;
            int pixelFormat = SE_ImageData::RGBA;
            if(!SE_Util::isPower2(w))
            {
                power2Width = SE_Util::higherPower2(w);
            }
            if(!SE_Util::isPower2(h))
            {
                power2Height = SE_Util::higherPower2(h);
            }
            imgd = new SE_ImageData;
            imgd->setWidth(power2Width);
            imgd->setHeight(power2Height);
            imgd->setBytesPerRow(power2Width * 4);
            imgd->setPixelFormat(pixelFormat);
            imgd->setData(data);
            imgd->setCompressType(SE_ImageData::RAW);
            imgd->setName(imageDataid.getStr());
            //imgd->setPreWidth(w);
            //imgd->setPreHeight(h);
            resourceManager->insertPathImageData(imageDataid.getStr(), imgd); 
            resourceManager->setIdPath(imageDataid.getStr(),imageDataid.getStr());            
        }
    }
    env->ReleaseStringUTFChars(imageKey, imageKey8);
    env->ReleaseStringUTFChars(savePath, savePath8);
    if (!imgd)
    {
         return false;
    }
    return true;
}

static void se_glReadPixels(JNIEnv *env, jobject obj, jstring imageKey, jint x, jint y, jint w, jint h) {
    int width = w;
    int height = h;
    int rowBytes = w * 4;
    int pixelFormat = SE_ImageData::RGBA;
    int pixNum = width * height;
    char* newData = (char *)malloc(height * rowBytes);
    glReadPixels(
        (GLint)x,
        (GLint)y,
        (GLsizei)width,
        (GLsizei)height,
        (GLenum)GL_RGBA,
        (GLenum)GL_UNSIGNED_BYTE,
        (GLvoid *)newData
    );
    const char* imageKey8 = env->GetStringUTFChars(imageKey, 0);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_ImageData* newImgd = new SE_ImageData;
    newImgd->setWidth(width);
    newImgd->setHeight(height);
    newImgd->setBytesPerRow(rowBytes);
    newImgd->setPixelFormat(pixelFormat);
    newImgd->setData(newData);
    newImgd->setCompressType(SE_ImageData::RAW);
    newImgd->setName(imageKey8);
    resourceManager->insertPathImageData(imageKey8, newImgd, true);   
    env->ReleaseStringUTFChars(imageKey, imageKey8);
}

SE_Scene* findScene(const char* sceneName)
{
    SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
    if (sceneManager) {
            SE_Scene* scene =  sceneManager->getScene(SE_FRAMEBUFFER_SCENE, SE_StringID(sceneName));
            return scene;
    }
    return NULL;    
}

static JavaVM* jnienv_to_javavm(JNIEnv* env)
{
    JavaVM* vm;
    return env->GetJavaVM(&vm) >= 0 ? vm : NULL;
}

static JNIEnv* javavm_to_jnienv(JavaVM* vm)
{
    JNIEnv* env;
    return vm->GetEnv((void **)&env, JNI_VERSION_1_4) >= 0 ? env : NULL;
}


static bool invoke_javaCallback(int msgType, const char* msgName)
{
    jstring msg_name = NULL;
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    JNIEnv* env = NULL;
    bool detach = false;
    if (mJvm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK)
    {
        if (mJvm->AttachCurrentThread(&env, NULL) != JNI_OK)
        {
           LOGE("callback_handler: failed to attach current thread\n");
           return false;
        }
        detach = true;
    } else { 
        env = uenv.env;
    }    
    msg_name = env->NewStringUTF(msgName);
    bool result = env->CallBooleanMethod(mjavaApp, method_javaCallback, msgType, msg_name);
    env->DeleteLocalRef(msg_name);
    if (detach)
    {
        if (mJvm->DetachCurrentThread() != JNI_OK)
        {
            LOGE("callback_handler: failed to detach current thread\n");
        }
    }
    return result;
}

static void se_instance(JNIEnv* env, jobject obj, jint userid0, jint userid1)
{
    mJvm = jnienv_to_javavm(env);
    mjavaApp = env->NewGlobalRef(obj);
    jclass clazz = env->FindClass(classPathName);
    method_javaCallback = env->GetMethodID(clazz, "javaCallback", "(ILjava/lang/String;)Z");
    gApp = SE_Application::getInstance();
    gApp->setJavaCallback(&invoke_javaCallback);
    SE_Application::SE_APPID appid;
    appid.first = userid0;
    appid.second = userid1;
    gApp->setAppID(appid);
    gApp->start();
    SE_SystemCommandFactory* sf = new SE_SystemCommandFactory;
    gApp->registerCommandFactory("SystemCommand", sf);
    SE_AssetManager* assetManager = new SE_AssetManager();
    gApp->setAssetManager(assetManager); 
    gApp->getResourceManager()->generateDefaultRenderEnv();
}

static void se_addAssetPath(JNIEnv* env, jobject clazz, jstring path)
{

    SE_AssetManager* assetManager = SE_Application::getInstance()->getAssetManager();
    if (assetManager == NULL) {
        assetManager = new SE_AssetManager();
        SE_Application::getInstance()->setAssetManager(assetManager);
    }
   if (path == NULL) {
        return ;
    }
    const char* path8 = env->GetStringUTFChars(path, NULL);
    assetManager->addAssetPath(path8);
    env->ReleaseStringUTFChars(path, path8);
}

static void se_rmAssetPath(JNIEnv* env, jobject clazz, jstring path)
{
    if (path == NULL) {
        return;
    }
    SE_AssetManager* assetManager = SE_Application::getInstance()->getAssetManager();
    if (assetManager == NULL) {
        return;
    }
    const char* path8 = env->GetStringUTFChars(path, NULL);
    assetManager->rmAssetPath(path8);
    env->ReleaseStringUTFChars(path, path8);
}

static void se_loadPlugins(JNIEnv* env, jobject clazz, jstring path)
{
    if (path == NULL) {
        return;
    }
    const char* path8 = env->GetStringUTFChars(path, NULL);
    SE_Application::getInstance()->getResourceManager()->loadPlugins(path8);
    env->ReleaseStringUTFChars(path, path8);
}
static void se_destroy(JNIEnv* env, jobject clazz)
{
     LOGI("## se destroy command###");
    SE_Application::getInstance()->shutdown();
}

static void se_runOneFrame(JNIEnv* env, jobject clazz)
{
    gApp->run();
}

//se_initRenderCapabilities must be invoked in gl thread,otherwise the app will crash.
static void se_initRenderCapabilities(JNIEnv* env, jobject clazz)
{
    gApp->getRenderSystemCapabilities()->init();
}

static void se_createCBF(JNIEnv* env, jobject clazz,jstring path,jstring fileid)
{

    const char* path8 = env->GetStringUTFChars(path, NULL);
    const char* fileid8 = env->GetStringUTFChars(fileid, NULL);
    
    std::string Path(path8);
    std::string ASEFileName(fileid8);
    
    ASE_Loader loader;
    
    std::string ASFFile = Path + SE_SEP + ASEFileName + ".ASE";
    loader.Load(ASFFile.c_str(), 0,true);
    loader.LoadEnd();
    loader.Write(Path.c_str(), ASEFileName.c_str(),false);
    env->ReleaseStringUTFChars(path, path8);
    env->ReleaseStringUTFChars(fileid, fileid8);

}

static void se_setDebug(JNIEnv* env, jobject clazz, jboolean debug)
{
    SE_Application::getInstance()->SEHomeDebug = debug;
}

static void se_loadTextureImage(JNIEnv* env, jobject clazz, jstring imageKey,
        jobject jbitmap) {
    const char* imagePath = env->GetStringUTFChars(imageKey, NULL);
    SE_ResourceManager *resourceManager =
            SE_Application::getInstance()->getResourceManager();
    SE_ImageData* existdata = resourceManager->getImageDataFromPath(imagePath);
    if (existdata && !existdata->getData()) {
        GLuint texid = existdata->getTexID();
        if (texid != 0) {
            glDeleteTextures(1, &texid);
        }
        existdata->setTexID(0);
        AndroidBitmapInfo bitmapInfo;
        int ret;
        void* pixels;
        if ((ret = AndroidBitmap_getInfo(env, jbitmap, &bitmapInfo)) < 0) {
            LOGE("error: AndroidBitmap_getInfo() failed ! error = %d\n", ret);
            return;
        }
        if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGB_565
                && bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
                && bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_4444) {
            LOGE("error: Bitmap format is not supported\n");
            return;
        }
        if ((ret = AndroidBitmap_lockPixels(env, jbitmap, &pixels)) < 0) {
            LOGE("error: AndroidBitmap_lockPixels() failed ! error = %d\n",
                    ret);
            return;
        }

        GLint internalFormat = GL_RGB;
        GLenum format = GL_RGB;
        GLenum type = GL_UNSIGNED_BYTE;

        switch (bitmapInfo.format) {
        case ANDROID_BITMAP_FORMAT_RGB_565:
            type = GL_UNSIGNED_SHORT_5_6_5;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            internalFormat = GL_RGBA;
            format = GL_RGBA;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            internalFormat = GL_RGBA;
            format = GL_RGBA;
            type = GL_UNSIGNED_SHORT_4_4_4_4;
            break;
        default:
            return;
            break;
        }
        int width = bitmapInfo.width;
        int height = bitmapInfo.height;
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glActiveTexture(GL_TEXTURE0);
        glGenTextures(1, &texid);
        existdata->setTexID(texid);
        glBindTexture(GL_TEXTURE_2D, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format,
                type, pixels);
        glGenerateMipmap (GL_TEXTURE_2D);

        GLint wraps, wrapt;
        if (SE_Util::isPower2(width) && SE_Util::isPower2(height)) {
            wraps = GL_REPEAT;
        } else {
            wraps = GL_CLAMP_TO_EDGE;
        }

        if (SE_Util::isPower2(width) && SE_Util::isPower2(height)) {
            wrapt = GL_REPEAT;
        } else {
            wrapt = GL_CLAMP_TO_EDGE;
        }

        GLint sampleMin, sampleMag;
        sampleMin = GL_LINEAR_MIPMAP_LINEAR;
        sampleMag = GL_LINEAR;
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMin);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMag);
    }
    env->ReleaseStringUTFChars(imageKey, imagePath);
}

static void se_changeTextureImage(JNIEnv* env, jobject clazz,
        jstring imageKey) {
    const char* imagePath = env->GetStringUTFChars(imageKey, NULL);
    SE_ResourceManager *resourceManager =
            SE_Application::getInstance()->getResourceManager();
    SE_ImageData* existdata = resourceManager->getImageDataFromPath(imagePath);
    if (existdata && !existdata->getData()) {
        GLuint texid = existdata->getTexID();
        if (texid != 0) {
            glDeleteTextures(1, &texid);
        }
        existdata->setTexID(0);

    }

    env->ReleaseStringUTFChars(imageKey, imagePath);
}

static void se_setSceneManagerStatus(JNIEnv* env, jobject clazz,jint value, jboolean enableOrdisable)
{
    SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus((SCENEMANAGER_STATUS)value,enableOrdisable);
}
static JNINativeMethod methods[] = {
    {"setMainScene_JNI", "(Ljava/lang/String;)V", (void*)se_setAsMainScene},
    {"glReadPixels_JNI", "(Ljava/lang/String;IIII)V", (void *) se_glReadPixels},
    {"savePixelsData_JNI", "(Ljava/lang/String;Ljava/lang/String;)V", (void *) se_savePixelsData},
    {"loadPixelsData_JNI", "(Ljava/lang/String;Ljava/lang/String;II)Z", (void *) se_loadPixelsData},
    {"instance_JNI", "(II)V", (void*)se_instance},
    {"addAssetPath_JNI", "(Ljava/lang/String;)V", (void*)se_addAssetPath},
    {"rmAssetPath_JNI", "(Ljava/lang/String;)V", (void*)se_rmAssetPath},
    {"destroy_JNI", "()V", (void*)se_destroy},
    {"runOneFrame_JNI", "()V", (void*)se_runOneFrame},
    {"initRenderCapabilities_JNI", "()V", (void*)se_initRenderCapabilities},
    {"createCBF_JNI", "(Ljava/lang/String;Ljava/lang/String;)V", (void*)se_createCBF},
    {"loadPlugins_JNI","(Ljava/lang/String;)V",(void*)se_loadPlugins},
    {"setDebug_JNI","(Z)V",(void*)se_setDebug},
    {"loadTextureImage_JNI","(Ljava/lang/String;Landroid/graphics/Bitmap;)V",(void*)se_loadTextureImage},
    {"changeTextureImage_JNI","(Ljava/lang/String;)V",(void*)se_changeTextureImage},
    {"setSceneManagerStatus_JNI","(IZ)V",(void*)se_setSceneManagerStatus},
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
    jclass bufferClassLocal = env->FindClass("java/nio/Buffer");
    bufferClass = (jclass) env->NewGlobalRef(bufferClassLocal);
    jclass nioAccessClassLocal = env->FindClass("java/nio/NIOAccess");
    nioAccessClass = (jclass) env->NewGlobalRef(nioAccessClassLocal);
    getBasePointerID = env->GetStaticMethodID(nioAccessClass, "getBasePointer", "(Ljava/nio/Buffer;)J");
    getBaseArrayID = env->GetStaticMethodID(nioAccessClass, "getBaseArray", "(Ljava/nio/Buffer;)Ljava/lang/Object;");
    getBaseArrayOffsetID = env->GetStaticMethodID(nioAccessClass, "getBaseArrayOffset", "(Ljava/nio/Buffer;)I");
    positionID = env->GetFieldID(bufferClass, "position", "I");
    limitID = env->GetFieldID(bufferClass, "limit", "I");
    elementSizeShiftID = env->GetFieldID(bufferClass, "_elementSizeShift", "I");

    jclass assetManagerClass = env->FindClass("android/content/res/AssetManager");
    //nativeAssetManagerID = env->GetFieldID(assetManagerClass, "mObject", "I");
    // for android 5.0+
    nativeAssetManagerID = env->GetFieldID(assetManagerClass, "mObject", "J");

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

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        fprintf(stderr, "ERROR: GetEnv failed\n");
        goto bail;
    }
    env = uenv.env;

    assert(env != NULL);

    printf("In mgmain JNI_OnLoad\n");

    if (!registerNatives(env)) {
        fprintf(stderr, "ERROR: SEHome native registration failed\n");
        goto bail;
    }

    register_com_android_se_SECamera(env);
    register_com_android_se_SEObject(env);
    register_com_android_se_scene(env);
    register_com_android_se_SEParticleSystem(env);
    register_com_android_se_XMLAnimation(env);
//    register_com_android_se_uiManager(env);
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}
