#ifndef SE_IMAGECODEC_H
#define SE_IMAGECODEC_H

#ifdef ANDROID
    #ifdef NDK
        #include <android/bitmap.h>
        #include <jni.h>
    #else
        #include "SkBitmap.h"
    #endif
#endif
#include <wchar.h>
class SE_ImageData;
class SE_ENTRY SE_ImageCodec
{
public:
    //filePath use UTF-8 encoding
    static SE_ImageData* load(const char* filePath,int type = 1);//type is jpg by default
    //filePath use Unicode encoding
    static SE_ImageData* load(const wchar_t* filePath);

    static SE_ImageData* loadARGB(const char *filePath);
    

#ifdef ANDROID
#ifdef NDK
    static SE_ImageData* load(JNIEnv* env, jobject jbitmap);
    static void load(JNIEnv* env, jobject, char*& pixData, int& w, int&h, int&r, int& pixelFormat);
#else
    static SE_ImageData* load(SkBitmap* bitmap);
    static void load(SkBitmap* bitmap, char*& pixData, int& w, int& h, int& r, int& pixelFormat);
#endif
    static SE_ImageData* loadAsset(const char* filePath, int type = 1);//default type is jpg
    static void loadAsset(const char* filePath, char*& pixData, int& width, int& height, int& rowBytes, int& pixelFormat);

#else
    
#endif
/*
    static void resizeImageData(SE_ImageData* imageData);
*/
    static void save(int width,int height,int depth,int bpp,const char* fileName,void* buff);

private:
    //load a hardware impressed image,need correct lib
    static SE_ImageData* loadPvrtc(const char *filedata,int filelengh,int type);
    static SE_ImageData* loadPvrtc(const char *filePath,int type);
};
#endif
