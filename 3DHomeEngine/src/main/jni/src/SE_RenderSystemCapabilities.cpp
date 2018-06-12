#include "SE_DynamicLibType.h"
#include <vector>
#include "SE_Utils.h"
#include "SE_Log.h"
#include "SE_RenderSystemCapabilities.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#ifdef GLES_20
    #include <GLES2/gl2.h>
    #include<GLES2/gl2ext.h>
#else
    #include <GLES/gl.h>
#endif
#include "SE_MemLeakDetector.h"


SE_RenderSystemCapabilities::SE_RenderSystemCapabilities()
{    
    mHasInit = false;
    mMaxTextureImageUnitNum = 0;
    mMaxCombinedTextureImageUnitNum = 0;
    mMaxStencilBufferNum = 0;
}

SE_RenderSystemCapabilities::~SE_RenderSystemCapabilities()
{}

void SE_RenderSystemCapabilities::init()
{
    if(mHasInit)
    {
        return;
    }
    initVendorStrings();
    createHardwareInfo();

    mHasInit = true;
}

void SE_RenderSystemCapabilities::initVendorStrings()
{
    if (mGPUVendorStrings.empty())
	{
		// Always lower case!
		mGPUVendorStrings.resize(GPU_VENDOR_COUNT);
		mGPUVendorStrings[GPU_UNKNOWN] = "unknown";
        mGPUVendorStrings[GPU_IMAGINATION_TECHNOLOGIES] = "imagination";
		mGPUVendorStrings[GPU_NVIDIA] = "nvidia";        
		mGPUVendorStrings[GPU_ATI] = "ati";
		mGPUVendorStrings[GPU_INTEL] = "intel";		
		mGPUVendorStrings[GPU_S3] = "s3";
		mGPUVendorStrings[GPU_MATROX] = "matrox";
        mGPUVendorStrings[GPU_3DLABS] = "3dlabs";
		mGPUVendorStrings[GPU_SIS] = "sis";		
		mGPUVendorStrings[GPU_APPLE] = "apple";    // iPhone Simulator
        mGPUVendorStrings[GPU_NOKIA] = "nokia";
        mGPUVendorStrings[GPU_BROADCOM] = "broadcom";
	}
}

SE_RenderSystemCapabilities::GPUVendor SE_RenderSystemCapabilities::vendorFromString(const char* vendorString)
{
    GPUVendor ret = GPU_UNKNOWN;
    std::string cmpString = vendorString;
	SE_Util::toLowerCase(cmpString);
	for (int i = 0; i < GPU_VENDOR_COUNT; ++i)
	{
		// case insensitive (lower case)
        if (cmpString.find(mGPUVendorStrings[i]) != std::string::npos)
		{
			ret = static_cast<GPUVendor>(i);
			break;
		}
	}

	return ret;
}

const char* SE_RenderSystemCapabilities::vendorToString(GPUVendor v)
{
    return mGPUVendorStrings[v].c_str();
}

void SE_RenderSystemCapabilities::createHardwareInfo()
{
   //get gpu vendor
    const char* vendorName = (const char*)glGetString(GL_VENDOR);
    setVendor(vendorFromString(vendorName));

if(getVendor() == 11)
{
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLELIGHT,false);
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLEMIRROR,false);
SE_Application::getInstance()->getSceneManager()->setSceneManagerStatus(ENABLEGROUNDSHADOW,false);
}

    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("vendorName = %s\n",vendorName);

    //get compressed texture type num
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS,&mTextureCompressedTypeNum);

    //get compressed texture supported type
    int* compressTextureTypes = new int[mTextureCompressedTypeNum];
    glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS,compressTextureTypes);

    for(int i = 0; i < mTextureCompressedTypeNum; ++i)
    {
        mTextureCompressTypes.push_back(compressTextureTypes[i]);
    }    

    delete compressTextureTypes;

    //get maxium texture image unit number
    glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS,&mMaxTextureImageUnitNum);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("GL_MAX_TEXTURE_IMAGE_UNITS = %d\n",mMaxTextureImageUnitNum);

    glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS,&mMaxCombinedTextureImageUnitNum);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = %d\n",mMaxCombinedTextureImageUnitNum); 
         
    glGetIntegerv(GL_STENCIL_BITS,&mMaxStencilBufferNum);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("\n\nGL_STENCIL_BITS = %d\n\n",mMaxStencilBufferNum);
 
         
}

bool SE_RenderSystemCapabilities::isSupportPowerVR()
{
    return mVendor == GPU_IMAGINATION_TECHNOLOGIES;
}
