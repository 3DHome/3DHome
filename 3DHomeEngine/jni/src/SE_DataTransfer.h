#ifndef SE_DATATRANSFER_H
#define SE_DATATRANSFER_H
#include "SE_ID.h"
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Mesh.h"
#include "SE_KeyFrame.h"
#include <list>
class SE_Mesh;
class SE_BufferInput;
class SE_BufferOutput;
class SE_ResourceManager;
class SE_ENTRY SE_TextureUnitTransfer
{
public:
    SE_TextureUnitTransfer()
    {
        mImageDataNum = 0;
        mType = 0;
        mImageDataIDArray = 0;
        mUTiling = 1.0;
        mVTiling = 1.0;
    }
    ~SE_TextureUnitTransfer()
    {
        if(mImageDataIDArray)
            delete[] mImageDataIDArray;
    }
    SE_TextureCoordDataID getTexCoordDataID()
    {
        return mTexCoordDataID;
    }
    int getImageDataNum()
    {
        return mImageDataNum;
    }
    SE_ImageDataID getImageDataID(int index)
    {
        if(index < 0 || index >= mImageDataNum)
            return SE_ImageDataID::INVALID;
        return mImageDataIDArray[index];
    }
    // this is the texture unit type: TEXTURE0, TEXTURE1, ... TEXTURE8,etc.
    int getType()
    {
        return mType;
    } 

    void setTextureCoordDataID(const SE_TextureCoordDataID& texCoordDataID)
    {
        mTexCoordDataID = texCoordDataID;
    }
    void setImageDataID(SE_ImageDataID* imageDataIDArray, int num)
    {
        mImageDataIDArray = imageDataIDArray;
        mImageDataNum = num;
    }
    void setType(int type)
    {
        mType = type;
    }
    float getUTiling()
    {
        return mUTiling;
    }
    void setUTiling(float utiling)
    {
        mUTiling = utiling;
    }

    float getVTiling()
    {
        return mVTiling;
    }
    void setVTiling(float vtiling)
    {
        mVTiling = vtiling;
    }
private:
    SE_TextureCoordDataID mTexCoordDataID;
    int mImageDataNum;
    int mType;
    SE_ImageDataID* mImageDataIDArray;
    float mUTiling;
    float mVTiling;
};
///////////////////////////////////////////////
class SE_ENTRY SE_TextureTransfer
{
public:
    SE_TextureTransfer()
    {
        mTexUnitNum = 0;
        mTexUnitTransfer = 0;
    }
    ~SE_TextureTransfer()
    {
        if(mTexUnitTransfer)
            delete[] mTexUnitTransfer;
    }

    int getTextureUnitNum()
    {
        return mTexUnitNum;
    }
    SE_TextureUnitTransfer* getTextureUnit(int index)
    {
        if(index < 0 || index >= mTexUnitNum)
            return 0;
        return &mTexUnitTransfer[index];
    }

    void setTextureUnitTransfer(SE_TextureUnitTransfer* texUnitTransfer, int num)
    {
        mTexUnitTransfer = texUnitTransfer;
        mTexUnitNum = num;
    }
private:
    int mTexUnitNum;
    SE_TextureUnitTransfer* mTexUnitTransfer;
};
/////////////////////////////////////////
class SE_ENTRY SE_SurfaceTransfer
{
public:
    SE_SurfaceTransfer()
    {
        mTexIndex = -1;
        mFacetNum = 0;
        mFacetArray = 0;
        mSampleMin = 0;
        mSampleMag = 0;
        mWrapS = 0;
        mWrapT = 0;
    }
    ~SE_SurfaceTransfer()
    {
        if(mFacetArray)
            delete[] mFacetArray;
    }
    int getTextureIndex()
    {
        return mTexIndex;
    }
    SE_MaterialDataID getMaterialDataID()
    {
        return mMaterialDataID;
    }
    SE_ProgramDataID getProgramDataID()
    {
        return mProgramDataID;
    }
    SE_RendererID getRendererID()
    {
        return mRendererID;
    }
    int getFacetNum()
    {
        return mFacetNum;
    }
    int* getFacetArray()
    {
        return mFacetArray;
    }
    int getSampleMin()
    {
        return mSampleMin;
    }
    int getSampleMag()
    {
        return mSampleMag;
    }
    int getWrapS()
    {
        return mWrapS;
    }
    int getWrapT()
    {
        return mWrapT;
    }
    
    SE_VertexBufferID getVertexBufferID()
    {
        return mVbID;
    }
    
    void setSampleMin(int min)
    {
        mSampleMin = min;
    }
    void setSampleMag(int mag)
    {
        mSampleMag = mag;
    }
    void setWrapS(int wraps)
    {
        mWrapS = wraps;
    }
    void setWrapT(int wrapt)
    {
        mWrapT = wrapt;
    }
    void setTextureIndex(int index)
    {
        mTexIndex = index;
    }
    void setMaterialDataID(const SE_MaterialDataID& materialDataID)
    {
        mMaterialDataID = materialDataID;
    }
    void setProgramDataID(const SE_ProgramDataID& pid)
    {
        mProgramDataID = pid;
    }
    void setRendererID(const SE_RendererID& id)
    {
        mRendererID = id;
    }
    void setFacets(int* facets, int num)
    {
        mFacetArray = facets;
        mFacetNum = num;
    }

    void setVertexBufferID(const SE_VertexBufferID& id)
    {
        mVbID = id;
    }
private:
    int mTexIndex;
    SE_MaterialDataID mMaterialDataID;
    SE_ProgramDataID mProgramDataID;
    SE_RendererID mRendererID;
    int mFacetNum;
    int* mFacetArray;
    int mSampleMin;
    int mSampleMag;
    int mWrapS;
    int mWrapT;

    SE_VertexBufferID mVbID;
};
//////////////////////////////////////////////////
class SE_ENTRY SE_MeshTransfer
{
public:
    SE_MeshTransfer()
    {
        mSurfaceNum = 0;
        mSurfaceTransferArray = 0;
        mTexNum = 0;
        mTexTransferArray = 0;
    }
    ~SE_MeshTransfer();
    SE_GeometryDataID getGeomDataID()
    {
        return mGeomDataID;
    }
    int getSurfaceNum()
    {
        return mSurfaceNum;
    }
    SE_SurfaceTransfer* getSurface(int index)
    {
        if(index < 0 || index >= mSurfaceNum)
            return 0;
        return &mSurfaceTransferArray[index];
    }
    int getTextureNum()
    {
        return mTexNum;
    }
    SE_TextureTransfer* getTexture(int index)
    {
        if(index < 0 || index >= mTexNum)
            return 0;
        return &mTexTransferArray[index];
    }
    SE_Vector3f getColor()
    {
        return mColor;
    }

    void setGeometryDataID(const SE_GeometryDataID& geomDataID)
    {
        mGeomDataID = geomDataID;
    }
    void setSurfaceTransfer(SE_SurfaceTransfer* surfaceData, int num)
    {
        mSurfaceTransferArray = surfaceData;
        mSurfaceNum = num;
    }
    void setTextureTransfer(SE_TextureTransfer* texTransfer, int num)
    {
        mTexTransferArray = texTransfer;
        mTexNum = num;
    }
    void setColor(const SE_Vector3f& color)
    {
        mColor = color;
    }
    /////////////////////////////////////////////////////
    SE_Mesh* createMesh(SE_ResourceManager* resourceManager);
    void createFromMesh(SE_Mesh* mesh);
    void read(SE_BufferInput& inputBuffer);
    void write(SE_BufferOutput& outputBuffer);
private:
    SE_GeometryDataID mGeomDataID;
    int mSurfaceNum;
    SE_SurfaceTransfer* mSurfaceTransferArray;
    int mTexNum;
    SE_TextureTransfer* mTexTransferArray;
    SE_TextureCoordDataID mJustForFreeTexDataID;;//just for no texture image but has texture coordinate
    SE_Vector3f mColor;
    
};

#endif
