#ifdef NDK
#include <jni.h>
#include <string>
#include <android/bitmap.h>
#else
#include <nativehelper/jni.h>
#endif
#include "SE_DynamicLibType.h"
#include "SE_UIManager.h"
#include "SE_Application.h"
#include "SkBitmap.h"
#include "SE_ImageCodec.h"
#include "SE_Log.h"
#include "SE_ImageData.h"

static jfieldID nativeBitmapID = 0;

static void se_CreateGUI(JNIEnv* env,jobject clazz)
{
	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	uiManager->CreateGUIScene();
}

static void se_ReSizeUIViewPort(JNIEnv* env,jobject clazz,float w, float h)
{
	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	uiManager->ReSizeUIViewPort(w,h);
}


static void se_addView(JNIEnv* env,jobject clazz, jstring viewgroupName,jstring viewName,jfloat x, jfloat y, jfloat w, jfloat h, jstring imageName )
{
    const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);
    const char* imagename = env->GetStringUTFChars(imageName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->AddView(viewgroupname,viewname,x,y,w,h,imagename);
	}
}

static void se_addViewGroup(JNIEnv* env,jobject clazz, jstring viewgroupName )
{
    const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->AddViewGroup(viewgroupname);
	}
}



static void se_setViewPosition(JNIEnv* env, jobject clazz,jstring viewgroupName,jstring viewName,jfloat x, jfloat y)
{
	const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->SetViewPosition(viewgroupname,viewname,x,y);
	}
}

static void se_setViewSize(JNIEnv* env, jobject clazz,jstring viewgroupName,jstring viewName,jfloat w, jfloat h)
{
	const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->SetViewSize(viewgroupname,viewname,w,h);
	}
}

static void se_setViewImage(JNIEnv* env, jobject clazz,jstring viewgroupName,jstring viewName, jstring imageName)
{
	const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);
    const char* imagename = env->GetStringUTFChars(imageName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->SetViewImage(viewgroupname,viewname, imagename);
	}
	env->ReleaseStringUTFChars(viewgroupName, viewgroupname);
	env->ReleaseStringUTFChars(viewName, viewname);
	env->ReleaseStringUTFChars(imageName, imagename);
}

static void se_setViewImage2(JNIEnv* env, jobject clazz, jstring viewgroupName,jstring viewName, jobject jbitmap)
{
    const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);
#ifdef NDK
    AndroidBitmapInfo bitmapInfo;
    int ret;
    void* pixels;
    if((ret = AndroidBitmap_getInfo(env, jbitmap, &bitmapInfo)) < 0)
    {
        LOGE("AndroidBitmap_getInfo() failed ! error = %d", ret);
        return;
    }
    if(bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGB_565 && bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
    {
        LOGE("Bitmap format is not supported\n");
        return;
    }
    if((ret = AndroidBitmap_lockPixels(env, jbitmap, &pixels)) < 0)
    {
        LOGE("AndroidBitmap_lockPixels() failed ! error = %d", ret);
        return;
    }
    int pixelSize = 0;
    switch(bitmapInfo.format)
    {
        case ANDROID_BITMAP_FORMAT_RGB_565:
            pixelSize = 2;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            pixelSize = 4;
            break;
        default:
            break;
    }
    int bytesPerRow = pixelSize * bitmapInfo.width;
    if(bytesPerRow != bitmapInfo.stride)
    {
        LOGE("bytes per row is not align with original image");
        return;
    }

    SE_ImageData* newImage = new SE_ImageData;
    newImage->setWidth(bitmapInfo.width);
    newImage->setHeight(bitmapInfo.height);
    switch(bitmapInfo.format)
    {
        case ANDROID_BITMAP_FORMAT_RGB_565:
            newImage->setPixelFormat(SE_ImageData::RGB_565);
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            newImage->setPixelFormat(SE_ImageData::RGBA);
            break;
        default:
            break;
    }
    int width = newImage->getWidth();
    int height = newImage->getHeight();
    unsigned char* src = (unsigned char*) pixels;
    unsigned char* dst = new unsigned char[width * height * pixelSize];
    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);
    }   
    newImage->setBytesPerRow(bytesPerRow);
    newImage->setData((char*)dst);
    newImage->setCompressType(SE_ImageData::RAW);
    SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
    if(uiManager)
    {
	uiManager->SetViewImage(viewgroupname,viewname, newImage);
    }
    AndroidBitmap_unlockPixels(env, jbitmap);
#else
    SkBitmap* nativeBitmap = (SkBitmap*)env->GetIntField(jbitmap, nativeBitmapID);
    SkBitmap* bitmap = new  SkBitmap(*nativeBitmap);
    SE_ImageData*  newImgd = SE_ImageCodec::load(bitmap);
    SE_ImageCodec::resizeImageData(newImgd);

    LOGD("bitmap .config() %d",bitmap->config());
    LOGD("bitmap newImgd->getPixelFormat() %d",newImgd->getPixelFormat()); 
    SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
    if(uiManager)
    {
	uiManager->SetViewImage(viewgroupname,viewname, newImgd);
    }
#endif

}


static void se_setViewVisible(JNIEnv* env, jobject clazz,jstring viewgroupName,jstring viewName, jboolean visible)
{
	const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
    const char* viewname = env->GetStringUTFChars(viewName, 0);

	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->SetViewVisible(viewgroupname,viewname, visible);
	}
}

static void se_setViewGroupVisible(JNIEnv* env, jobject clazz,jstring viewgroupName, jboolean visible)
{
	const char* viewgroupname = env->GetStringUTFChars(viewgroupName, 0);
	SE_UIManager* uiManager = SE_Application::getInstance()->getUIManager();
	if(uiManager)
	{
		uiManager->SetViewGroupVisible(viewgroupname, visible);
	}

}


static const char *classPathName = "com/borqs/se/ui/SEUIManager";


static JNINativeMethod methods[] = {
	{"CreateGUI", "()V", (void*)se_CreateGUI},
	{"SetUIViewPort", "(FF)V", (void*)se_ReSizeUIViewPort},				
    {"AddView", "(Ljava/lang/String;Ljava/lang/String;FFFFLjava/lang/String;)V", (void*)se_addView},
	{"AddViewGroup", "(Ljava/lang/String;)V", (void*)se_addViewGroup},
    {"SetViewPosition", "(Ljava/lang/String;Ljava/lang/String;FF)V", (void*)se_setViewPosition},
    {"SetViewSize", "(Ljava/lang/String;Ljava/lang/String;FF)V", (void*)se_setViewSize},
    {"SetViewImage", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void*)se_setViewImage},
    {"SetViewImage", "(Ljava/lang/String;Ljava/lang/String;Landroid/graphics/Bitmap;)V", (void*)se_setViewImage2},
    {"SetViewVisible", "(Ljava/lang/String;Ljava/lang/String;Z)V", (void*)se_setViewVisible},
    {"SetViewGroupVisible", "(Ljava/lang/String;Z)V", (void*)se_setViewGroupVisible},
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
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    nativeBitmapID = env->GetFieldID(bitmapClass, "mNativeBitmap", "I");

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

int register_com_android_se_uiManager(JNIEnv* env)
{
    return registerNatives(env);
}

