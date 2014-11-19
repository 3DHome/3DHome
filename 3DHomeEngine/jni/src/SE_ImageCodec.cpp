#include "SE_DynamicLibType.h"
#include "SE_ImageData.h"
#include "SE_ImageCodec.h"
#include "SE_Utils.h"

#include "SE_Application.h"
#include "SE_Log.h"
#include <string.h>
#include <stdio.h>
#include <sstream>
#include <iostream>
#ifdef ANDROID
    #ifdef NDK
        #include <IL/il.h>
    #else
        #include "SkImageDecoder.h"
        #include "SkBitmap.h"
        #include "SkCanvas.h"
        #include "SkString.h"
        #include "SkStream.h"
    #endif
#else
#define ILUT_USE_OPENGL
#include <IL/il.h>
#include <IL/ilu.h>
#include <IL/ilut.h>
#include "PVRTexLib.h"
using namespace pvrtexlib;
#endif

#ifdef GLES_20
#include <GLES2/gl2.h>
#else
#include <GLES/gl.h>
#endif
#include <GLES2/gl2ext.h>
#include "SE_MemLeakDetector.h"



typedef struct PVR_Header_TAG
{
    unsigned int dwHeaderSize;            /*!< size of the structure */
    unsigned int dwHeight;                /*!< height of surface to be created */
    unsigned int dwWidth;                /*!< width of input surface */
    unsigned int dwMipMapCount;            /*!< number of mip-map levels requested */
    unsigned int dwpfFlags;                /*!< pixel format flags */
    unsigned int dwTextureDataSize;        /*!< Total size in bytes */
    unsigned int dwBitCount;            /*!< number of bits per pixel  */
    unsigned int dwRBitMask;            /*!< mask for red bit */
    unsigned int dwGBitMask;            /*!< mask for green bits */
    unsigned int dwBBitMask;            /*!< mask for blue bits */
    unsigned int dwAlphaBitMask;        /*!< mask for alpha channel */
    unsigned int dwPVR;                    /*!< magic number identifying pvr file */
    unsigned int dwNumSurfs;            /*!< the number of surfaces present in the pvr */
}PVR_Header;

#define PVRTC_ARGB_4444     0x00000000
#define PVRTC_ARGB_1555     0x00000001
#define PVRTC_RGB_565       0x00000002
#define PVRTC_RGB_555       0x00000003
#define PVRTC_RGB_888       0x00000004
#define PVRTC_ARGB_8888     0x00000005
#define PVRTC_ARGB_8332     0x00000006
#define PVRTC_I_8           0x00000007
#define PVRTC_AI_88         0x00000008
#define PVRTC_1_BPP         0x00000009
#define PVRTC_VY1UY0        0x0000000A
#define PVRTC_Y1VY0U        0x0000000B
#define PVRTC_PVRTC2        0x0000000C
#define PVRTC_PVRTC4        0x0000000D
#define PVRTC_RGBA_4444     0x00000010
#define PVRTC_RGBA_5551     0x00000011
#define PVRTC_RGBA_8888     0x00000012
#define PVRTC_PVRTC2_GL     0x00000018
#define PVRTC_PVRTC4_GL     0x00000019
#define PVRTC_MIPMap        0x00000100
#define PVRTC_Twiddle       0x00000200
#define PVRTC_NormalMap     0x00000400

#ifdef ANDROID
#ifdef NDK
#else
static int getInternalFormat(SkBitmap::Config config)
{
    switch(config) {
        case SkBitmap::kA8_Config:
            return GL_ALPHA;
        case SkBitmap::kARGB_4444_Config:
            return GL_RGBA;
        case SkBitmap::kARGB_8888_Config:
            return GL_RGBA;
        case SkBitmap::kIndex8_Config:
            return GL_PALETTE8_RGBA8_OES;
        case SkBitmap::kRGB_565_Config:
            return GL_RGB;
        default:
            return -1;
    }
}
static int getType(SkBitmap::Config config)
{
    switch(config) {
        case SkBitmap::kA8_Config:
            return GL_UNSIGNED_BYTE;
        case SkBitmap::kARGB_4444_Config:
            return GL_UNSIGNED_SHORT_4_4_4_4;
        case SkBitmap::kARGB_8888_Config:
            return GL_UNSIGNED_BYTE;
        case SkBitmap::kIndex8_Config:
            return -1; // No type for compressed data.
        case SkBitmap::kRGB_565_Config:
            return GL_UNSIGNED_SHORT_5_6_5;
        default:
            return -1;
    }
}
static int checkFormat(SkBitmap::Config config, int format, int type)
{
    switch(config) {
        case SkBitmap::kIndex8_Config:
            if (format == GL_PALETTE8_RGBA8_OES)
                return 0;
        case SkBitmap::kARGB_8888_Config:
        case SkBitmap::kA8_Config:
            if (type == GL_UNSIGNED_BYTE)
                return 0;
        case SkBitmap::kARGB_4444_Config:
        case SkBitmap::kRGB_565_Config:
            switch (type) {
                case GL_UNSIGNED_SHORT_4_4_4_4:
                case GL_UNSIGNED_SHORT_5_6_5:
                case GL_UNSIGNED_SHORT_5_5_5_1:
                    return 0;
                case GL_UNSIGNED_BYTE:
                    if (format == GL_LUMINANCE_ALPHA)
                        return 0;
            }
            break;
        default:
            break;
    }
    return -1;
}
#endif
#endif


/* This expects "src" to be width*height*3 bytes in size   
This expects "dst" to be width*height*4 bytes in size   
src is in RGB format (you can edit it for BGR if you want)   
dst is in RGBA format afterwards */

void conver24To32(unsigned char *dst, const unsigned char *src, unsigned int width, unsigned int height) 
{  
    unsigned int a, size = width*height;  
    for(a=0; a<size; ++a) 
    {    
        *(dst++) = src[0]; /* R */    
        *(dst++) = src[1]; /* G */    
        *(dst++) = src[2]; /* B */        
        *(dst++) = 0xFF; /*A*/        
        src += 3;  
    }
}


SE_ImageData* SE_ImageCodec::load(const char* filePath,int type)
{
    //Load hardware compressed image.
    if(type == SE_ImageData::ETC_RGB_4BPP || type == SE_ImageData::OGL_PVRTC2)
    {
        return loadPvrtc(filePath,type);
    }

#if defined(WIN32)
    ILuint    imgId;
    ILenum    error;
    ilInit();
    iluInit();
    ilGenImages(1, &imgId);
    ilBindImage(imgId);
#if defined(WIN32)

#ifdef _UNICODE
    wchar_t fileWideChar[512];
    memset(fileWideChar, 0, sizeof(wchar_t) * 512);
    MultiByteToWideChar(CP_ACP, 0, filePath, -1, fileWideChar, 511);    
    if(!ilLoadImage(fileWideChar))    
    {
        return NULL;
    }
#else
    if(!ilLoadImage(filePath))
    {
        return NULL;
    }
#endif
#else
    if(!ilLoadImage(filePath))
    {
        return NULL;
    }
#endif

    int width = ilGetInteger(IL_IMAGE_WIDTH);
    int height = ilGetInteger(IL_IMAGE_HEIGHT);
    int bpp = ilGetInteger(IL_IMAGE_BITS_PER_PIXEL);
    unsigned char* src = ilGetData();
    int pixelSize = bpp / 8;
    unsigned char* dst = new unsigned char[width * height * pixelSize];
    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);
    }   

    //memcpy(dst, src, width * height * pixelSize);
    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    switch(pixelSize)
    {
    case 2:
        imageData->setPixelFormat(SE_ImageData::RGB_565);
        break;
    case 3:
        imageData->setPixelFormat(SE_ImageData::RGB);
        break;
    case 4:
        imageData->setPixelFormat(SE_ImageData::RGBA);
        break;
    default:
        LOGE("can not support the file format \n");
        break;
    }
    imageData->setBytesPerRow(width * pixelSize);
    imageData->setData((char*)dst);
    imageData->setCompressType(SE_ImageData::RAW);

    ilDeleteImages(1, &imgId);
    return imageData;
#else
#ifdef NDK
    ILuint    imgId;
    ILenum    error;
    ilInit();
    ilGenImages(1, &imgId);
    ilBindImage(imgId);
    if(!ilLoadImage(filePath))
    {
        return NULL;
    }
    int width = ilGetInteger(IL_IMAGE_WIDTH);
    int height = ilGetInteger(IL_IMAGE_HEIGHT);
    int bpp = ilGetInteger(IL_IMAGE_BITS_PER_PIXEL);
    unsigned char* src = ilGetData();
    int pixelSize = bpp / 8;
    unsigned char* dst = new unsigned char[width * height * pixelSize];
    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);
    }   

    //memcpy(dst, src, width * height * pixelSize);
    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    switch(pixelSize)
    {
    case 2:
        imageData->setPixelFormat(SE_ImageData::RGB_565);
        break;
    case 3:
        imageData->setPixelFormat(SE_ImageData::RGB);
        break;
    case 4:
        imageData->setPixelFormat(SE_ImageData::RGBA);
        break;
    default:
        LOGE("can not support the file format \n");
        break;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("load image width = %d, height = %d\n", width, height);
    imageData->setBytesPerRow(width * pixelSize);
    imageData->setData((char*)dst);
    imageData->setCompressType(SE_ImageData::RAW);

    ilDeleteImages(1, &imgId);
    return imageData;

#else
    SkFILEStream fileStream(filePath);
    if(!fileStream.isValid()) {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("error : can not open file %s\n",filePath);
        return NULL;
    }
    SkImageDecoder::Mode mode = SkImageDecoder::kDecodePixels_Mode;
    SkBitmap::Config prefConfig = SkBitmap::kNo_Config;
    SkImageDecoder* decoder = SkImageDecoder::Factory(&fileStream);
    SkBitmap* bitmap = new SkBitmap();
    if(!bitmap)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
         LOGI("Create bitmap fail,not enough memory!!!!!!!!!!\n");
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Create bitmap successs!!!!!!!!!!\n");
    }
    if(!decoder->decode(&fileStream, bitmap, prefConfig, mode))
    {
        printf("decode error \n");
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image width X height = %d X %d\n", bitmap->width(), bitmap->height());
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image config = %d \n", bitmap->config());
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image rowbytes = %d\n", bitmap->rowBytes());
    int width = bitmap->width();
    int height = bitmap->height();
    int rowBytes = bitmap->rowBytes();
    int pixelFormat;
    if(bitmap->getConfig() == SkBitmap::kARGB_8888_Config)
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    else if(bitmap->getConfig() == SkBitmap::kRGB_565_Config)
    {
        pixelFormat = SE_ImageData::RGB_565;
    }
    else
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    char* data = (char*)bitmap->getPixels();
    char* newData = (char*)malloc(height * rowBytes);
    if(newData)
    {
        int i,j;
        int rowbytes = rowBytes;
        for(i = 0 ; i < height ; i++)
        {
            memcpy(newData + i * rowbytes, data + (height - 1 - i) * rowbytes, rowbytes);
        }
    }

    delete bitmap;

    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    imageData->setBytesPerRow(rowBytes);
    imageData->setPixelFormat(pixelFormat);
    imageData->setData(newData);
    imageData->setCompressType(SE_ImageData::RAW);
    return imageData;
#endif
#endif
}
SE_ImageData* SE_ImageCodec::load(const wchar_t* filePath)
{
#if 0
#if defined(WIN32)
    ILuint    imgId;
    ILenum    error;
    ilInit();
    error = ilGetError();
    iluInit();
    error = ilGetError();
    ilGenImages(1, &imgId);
    error = ilGetError();
    ilBindImage(imgId);
    error = ilGetError();
    if(!ilLoadImage(filePath))
    {
        error = ilGetError();
        return NULL;
    }
    int width = ilGetInteger(IL_IMAGE_WIDTH);
    int height = ilGetInteger(IL_IMAGE_HEIGHT);
    int bpp = ilGetInteger(IL_IMAGE_BITS_PER_PIXEL);
    unsigned char* src = ilGetData();
    int pixelSize = bpp / 8;
    char* dst = new char[width * height * pixelSize];
    memcpy(dst, src, width * height * pixelSize);
    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    switch(pixelSize)
    {
    case 2:
        imageData->setPixelFormat(SE_ImageData::RGB_565);
        break;
    case 3:
        imageData->setPixelFormat(SE_ImageData::RGB);
        break;
    case 4:
        imageData->setPixelFormat(SE_ImageData::RGBA);
        break;
    default:
        LOGE("can not support the file format \n");
        break;
    }
    imageData->setBytesPerRow(width * pixelSize);
    imageData->setData(dst);
    imageData->setCompressType(SE_ImageData::RAW);
    return imageData;
#else 
    return NULL;
#endif
#endif
    return NULL;
}

#ifdef ANDROID
SE_ImageData* SE_ImageCodec::loadAsset(const char* filePath, int type)
{
    //Load hardware compressed image.
    if(type == (int)SE_ImageData::ETC_RGB_4BPP || type == (int)SE_ImageData::OGL_PVRTC2)
    {
        char* fileData = NULL;
    int len = 0;
        SE_Application::getInstance()->getAssetManager()->readAsset(filePath, fileData, len);
        if(len == 0) {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("error : can not open file %s\n",filePath);
            return NULL;
        }
        return loadPvrtc(fileData,len,type);
    }
    int w = 0;
    int h = 0;
    int r = 0;
    int pixelFormat = 0;
    char* pixData = NULL;
    loadAsset(filePath, pixData, w, h, r, pixelFormat);
    if (pixData) {
        SE_ImageData* imageData = new SE_ImageData;
        imageData->setWidth(w);
        imageData->setHeight(h);
        imageData->setPixelFormat(pixelFormat);
        imageData->setBytesPerRow(r);
        imageData->setData(pixData);
        imageData->setCompressType(SE_ImageData::RAW);
        return imageData;
    }
    return NULL;
}
void SE_ImageCodec::loadAsset(const char* filePath, char*& pixData, int& width, int& height, int& rowBytes, int& pixelFormat)
{
    char* fileData = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(filePath, fileData, len);
    if(len == 0) {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("error : can not open file %s\n",filePath);
        return;
    }
#ifdef NDK
    std::string filePathStr = filePath;
    size_t pos = filePathStr.rfind(".");
    std::string tmp = filePathStr.substr(pos+1);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("## load Asset file path = %s\n", filePath);
    ILuint    imgId;
    ILenum    error;
    ilInit();
    ilGenImages(1, &imgId);
    ilBindImage(imgId);
    ILenum fileType = IL_TYPE_UNKNOWN;
    if(tmp == "PNG" || tmp == "png")
    {
        fileType = IL_PNG;
    }
    else if(tmp == "JPEG" || tmp == "JPG" || tmp == "jpeg" || tmp == "jpg")
    {
        fileType = IL_JPG;
    }
    if(!ilLoadL(fileType, fileData, len))
    {
        delete[] fileData;
        return;
    }
    width = ilGetInteger(IL_IMAGE_WIDTH);
    height = ilGetInteger(IL_IMAGE_HEIGHT);
    int bpp = ilGetInteger(IL_IMAGE_BITS_PER_PIXEL);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("## loadAsset image with = %d, height = %d ##\n", width, height);
    unsigned char* src = ilGetData();
    int pixelSize = bpp / 8;
    unsigned char* dst = new unsigned char[width * height * pixelSize];
    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);
    }   

    //memcpy(dst, src, width * height * pixelSize);
    switch(pixelSize)
    {
    case 2:
        pixelFormat = SE_ImageData::RGB_565;
        break;
    case 3:
        pixelFormat = SE_ImageData::RGB;
        break;
    case 4:
        pixelFormat = SE_ImageData::RGBA;
        break;
    default:
        LOGE("can not support the file format \n");
        break;
    }
    rowBytes = width * pixelSize;
    pixData = (char*)dst;
    delete[] src;
    delete[] fileData;
#else
    SkMemoryStream fileStream;
    fileStream.setMemory(fileData, len, false);
    SkImageDecoder::Mode mode = SkImageDecoder::kDecodePixels_Mode;
    SkBitmap::Config prefConfig = SkBitmap::kNo_Config;
    SkImageDecoder* decoder = SkImageDecoder::Factory(&fileStream);
    SkBitmap* bitmap = new SkBitmap();
    if(!bitmap)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
         LOGI("Create bitmap fail,not enough memory!!!!!!!!!!\n");
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Create bitmap successs!!!!!!!!!!\n");
    }
    if(!decoder->decode(&fileStream, bitmap, prefConfig, mode))
    {
        printf("decode error \n");
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image width X height = %d X %d\n", bitmap->width(), bitmap->height());
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image config = %d \n", bitmap->config());
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("... image rowbytes = %d\n", bitmap->rowBytes());
    width = bitmap->width();
    height = bitmap->height();
    rowBytes = bitmap->rowBytes();

    pixelFormat = SE_ImageData::RGB;
    if(bitmap->getConfig() == SkBitmap::kARGB_8888_Config)
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    else if(bitmap->getConfig() == SkBitmap::kRGB_565_Config)
    {
        pixelFormat = SE_ImageData::RGB_565;
    }
    else if (bitmap->getConfig() == SkBitmap::kARGB_4444_Config)
    {
        pixelFormat = SE_ImageData::RGBA_4444;
    }
    char* data = (char*)bitmap->getPixels();
    pixData = NULL;
    pixData = (char*)malloc(height * rowBytes);
    if(pixData)
    {
        int i,j;
        int rowbytes = rowBytes;
        for(i = 0 ; i < height ; i++)
        {
            memcpy(pixData + i * rowbytes, data + (height - 1 - i) * rowbytes, rowbytes);
        }
    }
    
    delete bitmap;
#endif
}
#ifdef NDK
SE_ImageData* SE_ImageCodec::load(JNIEnv* env, jobject jbitmap)
{
    int w;
    int h;
    int r;
    int pixelFormat;
    char* des = NULL;
    load(env, jbitmap, des, w, h, r, pixelFormat);
    SE_ImageData* newImage = new SE_ImageData;
    newImage->setWidth(w);
    newImage->setHeight(h);
    newImage->setPixelFormat(pixelFormat);
    newImage->setBytesPerRow(r);
    newImage->setData(des);
    newImage->setCompressType(SE_ImageData::RAW);
    return newImage;
}
void SE_ImageCodec::load(JNIEnv* env, jobject jbitmap, char*& pixData, int& w, int&h, int&r, int& pixelFormat)
{
    AndroidBitmapInfo bitmapInfo;
    int ret;
    void* pixels;
    if((ret = AndroidBitmap_getInfo(env, jbitmap, &bitmapInfo)) < 0)
    {
        LOGE("error: AndroidBitmap_getInfo() failed ! error = %d\n", ret);
        return;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("## jbitmap bitmapInfo format = %d #\n", bitmapInfo.format);
    if(bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGB_565 && bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888  && bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_4444)
    {
        LOGE("error: Bitmap format is not supported\n");
        return;
    }
    if((ret = AndroidBitmap_lockPixels(env, jbitmap, &pixels)) < 0)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGE("error: AndroidBitmap_lockPixels() failed ! error = %d\n", ret);
        return;
    }
    int pixelSize = 0;
    int f = 0;
    switch(bitmapInfo.format)
    {
        case ANDROID_BITMAP_FORMAT_RGB_565:
            pixelSize = 2;
            f = SE_ImageData::RGB_565;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            f = SE_ImageData::RGBA;
            pixelSize = 4;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            f = SE_ImageData::RGBA_4444;
            pixelSize = 2;
            break;
        default:
            return;
            break;
    }
    int bytesPerRow = pixelSize * bitmapInfo.width;
    if(bytesPerRow != bitmapInfo.stride)
    {

        LOGE("error : bytes per row is not align with original image\n");
        return;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("## jbitmap width = %d, height = %d , format = %d ##\n", bitmapInfo.width, bitmapInfo.height, bitmapInfo.format);
    
    int width = bitmapInfo.width;
    int height = bitmapInfo.height;
    unsigned char* src = (unsigned char*) pixels;
    unsigned char* dst = new unsigned char[width * height * pixelSize];

    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);

    }   
    AndroidBitmap_unlockPixels(env, jbitmap);
    w = bitmapInfo.width;
    h = bitmapInfo.height;
    r = bytesPerRow;
    pixelFormat = f;
    pixData = (char*)dst;
}
#else
SE_ImageData* SE_ImageCodec::load(SkBitmap* bitmap)
{
    int w = 0;
    int h = 0;
    int r = 0;
    int pixelFormat = 0;
    char* pixData = NULL;
    load(bitmap, pixData, w, h, r, pixelFormat);
    if (pixData) { 
        SE_ImageData* imageData = new SE_ImageData;
        imageData->setWidth(w);
        imageData->setHeight(h);
        imageData->setPixelFormat(pixelFormat);
        imageData->setBytesPerRow(r);
        imageData->setData(pixData);
        imageData->setCompressType(SE_ImageData::RAW);
        return imageData;
   }
   return NULL;
}
void SE_ImageCodec::load(SkBitmap* bitmap, char*& pixData, int& w, int& h, int& r, int& pixelFormat)
{
    SkBitmap::Config config = bitmap->getConfig();
    int internalformat = getInternalFormat(config);
    int type = getType(config);
    #ifdef MRVL_BGRA_HACK
    internalformat = reverseRGBA(internalformat, type);
    #endif
    if (checkFormat(config, internalformat, type))
        return;
    bitmap->lockPixels();
    w = bitmap->width();
    h = bitmap->height();
    r = bitmap->rowBytes();
    const char* data = (char*)bitmap->getPixels();
    bitmap->unlockPixels();
    pixelFormat = SE_ImageData::RGB;
    if(bitmap->getConfig() == SkBitmap::kARGB_8888_Config)
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    else if(bitmap->getConfig() == SkBitmap::kRGB_565_Config)
    {
        pixelFormat = SE_ImageData::RGB_565;
    }
    else if (bitmap->getConfig() == SkBitmap::kARGB_4444_Config)
    {
        pixelFormat = SE_ImageData::RGBA_4444;
    }
    pixData = NULL;
    pixData = (char*)malloc(h * r);
    if(pixData)
    {
        int rowbytes = r;
        for(int i = 0 ; i < h ; i++)
        {
            memcpy(pixData + i * rowbytes, data + (h - 1 - i) * rowbytes, rowbytes);
        }
    }
}
#endif
#endif

/*void SE_ImageCodec::resizeImageData(SE_ImageData* imageData)
{
    if (!imageData)
        return;
    int width = imageData->getWidth();
    int height = imageData->getHeight();
    int power2Width = width;
    int power2Height = height;
    if(!SE_Util::isPower2(width))
    {
        power2Width = SE_Util::higherPower2(width);
    }
    if(!SE_Util::isPower2(height))
    {
        power2Height = SE_Util::higherPower2(height);
    }
    int startx = 0;
    int starty = 0;
    if(width != power2Width || height != power2Height)
    {
        int pixelSize = imageData->getPixelSize();
        int size = power2Width * power2Height * pixelSize;
        char* data = new char[size];
        memset(data, 0, size);
        char* src = imageData->getData();
        starty = (power2Height - height) >> 1;
        startx = (power2Width - width) >> 1;
        for(int y = 0 ; y < height ; y++)
        {
            char* ydst = &data[(starty + y) * power2Width * pixelSize];
            memcpy(&ydst[startx * pixelSize], src, width * pixelSize);
            src += width * pixelSize;
        }
        imageData->setWidth(power2Width);
        imageData->setHeight(power2Height);
        imageData->setPixelFormat(imageData->getPixelFormat());
        imageData->setBytesPerRow(power2Width * pixelSize);
        imageData->setCompressType(SE_ImageData::RAW);
        imageData->setData(data);
        imageData->setPreWidth(width);
        imageData->setPreHeight(height);
    }
}*/


SE_ImageData* SE_ImageCodec::loadPvrtc(const char *filedata,int filelengh,int type)
{
    //load from assets
    int format; // .pvr format map to gles format
    int width;
    int height;
    int levels; // mipmap levels

    long size;
    char * data;

    
    PVR_Header h;

    int headerlen = 0;

    memcpy(&headerlen,filedata,sizeof(unsigned int));

    memcpy(&h,filedata,headerlen);

    // format info
    width = h.dwWidth;
    height = h.dwHeight;
    levels = h.dwMipMapCount;
    size = h.dwTextureDataSize;
    
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("PVR file width = %d,height = %d\n\n",width,height);

    h.dwpfFlags &= ~PVRTC_MIPMap; // redundant with levels?
    h.dwpfFlags &= ~PVRTC_Twiddle; // todo : record?
    h.dwpfFlags &= ~PVRTC_NormalMap; // todo : handle?

    bool hasAlpha = h.dwAlphaBitMask != 0;

    switch( h.dwpfFlags )
    {

        case PVRTC_PVRTC4:
            format = hasAlpha? GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG : GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
            //if the texture contains alpha information, then use it.
            break;
        case PVRTC_PVRTC2:
            format = hasAlpha? GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG : GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
            //if the texture contains alpha information, then use it.
            break;
        // _GL format: the order of scan lines are reverted.
        case PVRTC_PVRTC4_GL:
            format = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
            break;
        case PVRTC_PVRTC2_GL:
            format = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
            break;
        default:
        // if not special compressed, why use PVRTC format?
        // todo : mapping more PVR possible content to GLES internal format
            format = -1;
    }
    
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("PVR data size is %d\n\n\n",h.dwTextureDataSize);
    data = new char[h.dwTextureDataSize];        
    memcpy(data,filedata + h.dwHeaderSize,h.dwTextureDataSize);

    SE_ImageData* imageData = new SE_ImageData;

    imageData->setWidth(width);
    imageData->setHeight(height);    
    imageData->setDataSizeInByte(h.dwTextureDataSize);
    imageData->setData(data);
    imageData->setCompressType(type);    
    return imageData;
}


SE_ImageData* SE_ImageCodec::loadPvrtc(const char *filePath,int type) 
{
    int format; // .pvr format map to gles format
    int width;
    int height;
    int levels; // mipmap levels

    long size;
    unsigned char * data;

    FILE *fp = fopen(filePath, "rb");
    PVR_Header h;

    fread(& h.dwHeaderSize, sizeof(unsigned int), 1, fp);

    fread(& h.dwHeight, h.dwHeaderSize - sizeof(unsigned int), 1, fp);

    // format info
    width = h.dwWidth;
    height = h.dwHeight;
    levels = h.dwMipMapCount;
    size = h.dwTextureDataSize;

    h.dwpfFlags &= ~PVRTC_MIPMap; // redundant with levels?
    h.dwpfFlags &= ~PVRTC_Twiddle; // todo : record?
    h.dwpfFlags &= ~PVRTC_NormalMap; // todo : handle?

    bool hasAlpha = h.dwAlphaBitMask != 0;

    switch( h.dwpfFlags )
    {

        case PVRTC_PVRTC4:
            format = hasAlpha? GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG : GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
            //if the texture contains alpha information, then use it.
            break;
        case PVRTC_PVRTC2:
            format = hasAlpha? GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG : GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
            //if the texture contains alpha information, then use it.
            break;
        // _GL format: the order of scan lines are reverted.
        case PVRTC_PVRTC4_GL:
            format = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
            break;
        case PVRTC_PVRTC2_GL:
            format = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
            break;
        default:
        // if not special compressed, why use PVRTC format?
        // todo : mapping more PVR possible content to GLES internal format
            format = -1;
    }

    data = new unsigned char[h.dwTextureDataSize];        
    if(fread(data, h.dwTextureDataSize, 1, fp ) != 1)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("read pvr file error!!!\n\n");
        return NULL;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("\n\n\nLoad [%s] pvr file success!!!!\n\n\n\n\n",filePath);
    fclose(fp);
   

    SE_ImageData* imageData = new SE_ImageData;

    imageData->setWidth(width);
    imageData->setHeight(height);    
    imageData->setDataSizeInByte(h.dwTextureDataSize);
    imageData->setData((char *)data);
    imageData->setCompressType(type);    
    return imageData;
}


SE_ImageData* SE_ImageCodec::loadARGB(const char* filePath)
{
#if defined(WIN32)
    ILuint    imgId;
    ILenum    error;
    ilInit();
    iluInit();
    ilGenImages(1, &imgId);
    ilBindImage(imgId);
#ifdef _UNICODE
    wchar_t fileWideChar[512];
    memset(fileWideChar, 0, sizeof(wchar_t) * 512);
    MultiByteToWideChar(CP_ACP, 0, filePath, -1, fileWideChar, 511);    
    if(!ilLoadImage(fileWideChar))    
    {
        return NULL;
    }
#else
    if(!ilLoadImage(filePath))
    {
        return NULL;
    }
#endif

    int width = ilGetInteger(IL_IMAGE_WIDTH);
    int height = ilGetInteger(IL_IMAGE_HEIGHT);
    int bpp = ilGetInteger(IL_IMAGE_BITS_PER_PIXEL);
    unsigned char* src = ilGetData();
    int pixelSize = bpp / 8;
    unsigned char* dst = new unsigned char[width * height * pixelSize];
    for(int y = height - 1 ; y >= 0 ; y--)
    {
        unsigned char* srcData = src + y * width * pixelSize;
        unsigned char* dstData = dst + (height - 1 - y) * width * pixelSize;
        memcpy(dstData, srcData, width * pixelSize);
    }

    unsigned char *des32 = NULL;

    //memcpy(dst, src, width * height * pixelSize);
    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    switch(pixelSize)
    {
    case 2:
        imageData->setPixelFormat(SE_ImageData::RGB_565);
        break;
    case 3:
        {
            imageData->setPixelFormat(SE_ImageData::RGB);
            //24to32
            des32= new unsigned char[width * height * 4];
            memset(des32,0,sizeof(des32));
            conver24To32(des32,dst,width,height);
            delete dst;
        }
        break;
    case 4:
        imageData->setPixelFormat(SE_ImageData::RGBA);
        break;
    default:
        LOGE("can not support the file format \n");
        break;
    }
    imageData->setBytesPerRow(width * pixelSize);
    if(des32)
    {
        imageData->setData((char*)des32);
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("FIXME:just for test\n\n\n");
    }
    else
    {
        imageData->setData((char*)dst);
    }
    imageData->setCompressType(SE_ImageData::RAW);
    return imageData;
#else
    /*
    SkFILEStream fileStream(filePath);
    if(!fileStream.isValid()) {
    if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("error : can not open file %s\n",filePath);
        return NULL;
    }
    SkImageDecoder::Mode mode = SkImageDecoder::kDecodePixels_Mode;
    SkBitmap::Config prefConfig = SkBitmap::kNo_Config;
    SkImageDecoder* decoder = SkImageDecoder::Factory(&fileStream);
    SkBitmap* bitmap = new SkBitmap();
    if(!bitmap)
    {
    if(SE_Application::getInstance()->SEHomeDebug)
         LOGI("Create bitmap fail,not enough memory!!!!!!!!!!\n");
    }
    else
    {
    if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Create bitmap successs!!!!!!!!!!\n");
    }
    if(!decoder->decode(&fileStream, bitmap, prefConfig, mode))
    {
        printf("decode error \n");
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    {
    LOGI("... image width X height = %d X %d\n", bitmap->width(), bitmap->height());
    LOGI("... image config = %d \n", bitmap->config());
    LOGI("... image rowbytes = %d\n", bitmap->rowBytes());
    }
    int width = bitmap->width();
    int height = bitmap->height();
    int rowBytes = bitmap->rowBytes();
    int pixelFormat;
    if(bitmap->getConfig() == SkBitmap::kARGB_8888_Config)
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    else if(bitmap->getConfig() == SkBitmap::kRGB_565_Config)
    {
        pixelFormat = SE_ImageData::RGB_565;
    }
    else
    {
        pixelFormat = SE_ImageData::RGBA;
    }
    char* data = (char*)bitmap->getPixels();
    char* newData = (char*)malloc(height * rowBytes);
    if(newData)
    {
        int i,j;
        int rowbytes = rowBytes;
        for(i = 0 ; i < height ; i++)
        {
            memcpy(newData + i * rowbytes, data + (height - 1 - i) * rowbytes, rowbytes);
        }
    }

    delete bitmap;

    SE_ImageData* imageData = new SE_ImageData;
    imageData->setWidth(width);
    imageData->setHeight(height);
    imageData->setBytesPerRow(rowBytes);
    imageData->setPixelFormat(pixelFormat);
    imageData->setData(newData);
    imageData->setCompressType(SE_ImageData::RAW);
    return imageData;
    */
    return NULL;
#endif
}

void SE_ImageCodec::save(int width,int height,int depth,int bpp,const char* fileName,void* buff)
{
#ifdef WIN32
    ILuint    imgId;
    ILenum    error;
    ilGenImages(1, &imgId);
    ilBindImage(imgId);
    ilEnable(IL_FILE_OVERWRITE);
    bool result = ilTexImage(width,height,depth,bpp,IL_RGBA,IL_UNSIGNED_BYTE,buff);
    
#ifdef UNICODE
    wchar_t wname[512];
	SE_Util::multiByteToWideChar(fileName,wname,512);

    result = ilSaveImage(wname);
#else
    result = ilSaveImage(fileName);
#endif
    ilDeleteImages(1, &imgId);
    if(SE_Application::getInstance()->SEHomeDebug)
    {
        if(result)
        {
            LOGI("save image to %s success!!\n",fileName);
        }
        else
        {
            LOGI("save image to %s fail!!\n",fileName);
        }
    }
#endif
}
