#include "SE_DynamicLibType.h"
#include "SE_Log.h"
#ifdef GLES_20
#include <GLES2/gl2.h>
#else
#include <GLES/gl.h>
#endif
#include "SE_ImageData.h"

#include "SE_MemLeakDetector.h"
#include "SE_Application.h"
SE_ImageDataPortion SE_ImageDataPortion::INVALID = SE_ImageDataPortion(0, 0, 0, 0);
////////////////////////////////
bool SE_ImageData::isCompressTypeByHardware()
{
    switch(mCompressType)
    {
    case ETC_RGB_4BPP:
        return true;
        break;
    case OGL_PVRTC2:
        return true;
        break;
    default:
        return false;
    }

}
SE_ImageData::~SE_ImageData()
{
    if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("BORQS#####################release imagedata name = %s\n", mName.c_str());
    if(mTexid != 0)
    {
        glDeleteTextures(1, &mTexid);
        mTexid = 0;
    }
        if(mData)
        delete[] mData;
     }
