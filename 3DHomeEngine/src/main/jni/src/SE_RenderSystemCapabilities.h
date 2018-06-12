#ifndef SE_RENDERSYSTEMCAPABILITIES_H
#define SE_RENDERSYSTEMCAPABILITIES_H


class SE_ENTRY SE_RenderSystemCapabilities
{
public:
    SE_RenderSystemCapabilities();
    ~SE_RenderSystemCapabilities();

    void setInitStatus(bool init)
    {
        mHasInit = init;
    }

    bool isInit()
    {
        return mHasInit;
    }

    void init();

    // Enumeration of GPU vendors.
    enum GPUVendor
    {
	    GPU_UNKNOWN = 0,
        GPU_IMAGINATION_TECHNOLOGIES = 1,
        GPU_NVIDIA = 2,
	    GPU_ATI = 3, 
	    GPU_INTEL = 4,
	    GPU_S3 = 5,
	    GPU_MATROX = 6,
	    GPU_3DLABS = 7,
	    GPU_SIS = 8,                
        GPU_APPLE = 9,  // Apple Software Renderer
        GPU_NOKIA = 10,
        GPU_BROADCOM = 11,

	    /// placeholder
	    GPU_VENDOR_COUNT = 12
    };

    void setVendor(GPUVendor v)
    {
        mVendor = v;
    }

    GPUVendor getVendor() const
    {
        return mVendor;
    }

    GPUVendor vendorFromString(const char* vendorString);

    const char* vendorToString(GPUVendor v);

    bool isSupportPowerVR();



private:
    bool mHasInit;
    typedef std::vector<std::string> StringVector;
    StringVector mGPUVendorStrings;

    /// GPU Vendor
    GPUVendor mVendor;

    int mTextureCompressedTypeNum;

    typedef std::vector<int> IntVector;
    IntVector mTextureCompressTypes;

    //shader
    int mMaxTextureImageUnitNum;
    int mMaxCombinedTextureImageUnitNum;    

    int mMaxStencilBufferNum;


private:
   void initVendorStrings();
   void createHardwareInfo();
};



#endif
