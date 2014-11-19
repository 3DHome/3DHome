#ifndef SE_MESH_H
#define SE_MESH_H
#include "SE_Vector.h"
#include "SE_ID.h"
#include "SE_Common.h"
#include "SE_ImageData.h"
#include "SE_VertexBuffer.h"
#include "SE_Quat.h"
#include "SE_Light.h"
#include <string.h>
#include "SE_KeyFrame.h"
#include <list>
class SE_TextureCoordData;
class SE_GeometryData;
class SE_MaterialData;
class SE_ShaderProperty;
class SE_BipedController;
class SE_Light;
class SE_Spatial;
class SE_ENTRY SE_TextureUnit
{
public:
    SE_TextureUnit();
    ~SE_TextureUnit();
    void setTextureCoordData(SE_TextureCoordData* texCoordData)
    {
        mTexCoord = texCoordData;
    }
	void setTextureCoordDataID(const SE_TextureCoordDataID& id);
    SE_TextureCoordDataID getTextureCoordDataID()
    {
        return mTextureCoordDataID;
    }
    void setImageDataID(SE_ImageDataID* imageIDArray, int num);
    void setImageDataID(SE_ImageDataID& id);
    void setImageDataNum(int num);

    //this function should be hide?
    void setImageData(int index, SE_ImageData* imageData)
	{
		if(index < 0 || index >= mImageDataNum)
		{
			return;
		}
		mImageDataArray[index] = imageData;
	}
	SE_ImageData* getImageData(int index)
	{
		if(index < 0 || index >= mImageDataNum)
		{
			return NULL;
		}
		SE_ImageData* imageData = mImageDataArray[index];
		return imageData;
	}
	void getImageData(SE_ImageData**& imageDataArray, int& imageDataNum)
	{
        imageDataArray = mImageDataArray;
		imageDataNum = mImageDataNum;
	}
    SE_TextureCoordData* getTextureCoordData()
    {
        return mTexCoord;
    }
    int getImageDataIDNum()
    {
        return mImageDataIDNum;
    }
    SE_ImageDataID getImageDataID(int index)
    {
        if(index < 0 || index >= mImageDataIDNum)
			return SE_ImageDataID::INVALID;
        return mImageDataIDArray[index];
    }
    SE_ImageDataID* getImageDataID()
    {
        return mImageDataIDArray;
    }
    bool hasMultiImage()
    {
        return mImageDataIDNum > 1 || mImageDataNum > 1;
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
    SE_TextureCoordData* mTexCoord;
    SE_ImageDataID* mImageDataIDArray;
    int mImageDataIDNum;

	SE_ImageData** mImageDataArray;
	int mImageDataNum;
	int mSampleMin;
    int mSampleMag;
    int mWrapS;
    int mWrapT;
	SE_TextureCoordDataID mTextureCoordDataID;

    float mUTiling;
    float mVTiling;

    bool replaceImageData(const char* filePath,int imaIndex = 0);

};
class SE_ENTRY SE_Texture
{
public:
    SE_Texture();
    ~SE_Texture();
    void setTextureUnit(int texType, SE_TextureUnit* texUnit);
    void removeTextureUnit(int texType);
    SE_TextureUnit* getTextureUnit(int texType);
    void setTexUnitNum(int num)
    {
        mTextureUnitNum = num;
    }
    int getTexUnitNum()
    {
        return mTextureUnitNum;
    }
private:
    SE_TextureUnit** mTexUnitArray;
    int mTextureUnitNum;
};
class SE_ENTRY SE_Surface
{
public:
    enum DrawMode{DRAW_COLOR,DRAW_TEXTURE,DRAW_TEXTURE_COLOR};
    SE_Surface();
    ~SE_Surface();
    SE_GeometryData* getGeometryData();
    SE_Texture* getTexture();
    SE_MaterialData* getMaterialData();
    int getFacetNum();
    int* getFacetArray();
    SE_Vector3f getColor();
    void getVertexIndex(int*& index, int& indexNum);
	void getVertex(_Vector3f*& vertex, int & vertexNum);
	void getTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum);
    void getFaceVertex(_Vector3f*& vertex, int& vertexNUm);
   
    void getLineVertex(_Vector3f*& vertex, int& vertexNUm);//for draw line only

    void getFaceTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum);
    void getVertexTangent(_Vector3f*& vertexTangent, int& vertexTangentNum);

    void getFaceNormal(_Vector3f*& faceNormal, int & faceNormalNum);
    void getFaceVertexNormal(_Vector3f*& faceVertexNormal, int & faceVertexNormalNum);
    void updateVertexNormal();
    SE_ProgramDataID getProgramDataID();
	SE_RendererID getRendererID();
	void setRendererID(const SE_RendererID& id);
    void setGeometryData(SE_GeometryData* geomData);
    void setMaterialData(SE_MaterialData* materialData);
    void setTexture(SE_Texture* texture);
    void setFacets(int* facets, int num);
    void setColor(const SE_Vector3f& color);
    void setProgramDataID(const SE_ProgramDataID& programID);
	void setShaderProperty(SE_ShaderProperty* sp);
    SE_VertexBuffer *getVertexBuffer()
    {
        return mVertexBuffer;
    }
    
	SE_ShaderProperty* getShaderProperty()
	{
		return mShaderProperty;
	}
    void setNeedBlend(bool blend);
    bool isNeedBlend();

    void setSkeletonWeight(float * weightarray)
    {
        if(mSkeletonWeightForSurface)
        {
            delete[] mSkeletonWeightForSurface;
        }
        mSkeletonWeightForSurface = weightarray;
    }

    float *getSkeletonWeight()
    {
        return mSkeletonWeightForSurface;
    }

    void setSkeletonIndex(float * indexarray)
    {
        if(SkeletonIndexForSurface)
        {
            delete[] SkeletonIndexForSurface;
        }
        SkeletonIndexForSurface = indexarray;
    }

    float *getSkeletonIndex()
    {
        return SkeletonIndexForSurface;
    }

    void setCurrentFrameIndex(int index)
    {
        mCurrentAnimationFrameIndex = index;
    }

    int getCurrentFrameIndex()
    {
        return mCurrentAnimationFrameIndex;
    }

    const char * getCurrentBipedControllerID()
    {
        return mCurrentBipedControllerID.c_str();
    }

    void setCurrentBipedControllerID(const char *controllerID)
    {
        mCurrentBipedControllerID = controllerID;
    }

    SE_BipedController * getCurrentBipedController()
    {
        return mBipedController;
    }

    void setCurrentBipedController(SE_BipedController *controller)
    {
        mBipedController = controller;
    }

    void clearVertexInfo()
    {
        if(mVertex)
        {
            delete[] mVertex;
            mVertex = NULL;
        }
		for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
		{
            if(mTexVertex[i])
            {
                delete[] mTexVertex[i];
                mTexVertex[i] = NULL;
            }
			mTexVertexNum[i] = 0;
		}
        if(mFaceVertex)
        {
            delete[] mFaceVertex;
            mFaceVertex = NULL;
        }
		for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
		{
            if(mFaceTexVertex[i])
            { 
                delete[] mFaceTexVertex[i];
                mFaceTexVertex[i] = NULL;
            }
            mFaceTexVertexNum[i] = 0;
		}
        mVertexNum = 0;
        mFaceVertexNum = 0;
    }
    void getVertexIndexInGeometryData(int*& outArray , int& outNum);
    void upDateFaceVertex();//call after update vertex, add by liusong//
    void upDateFaceTexVertex(int texIndex);//call after update TexVertex, add by liusong//

    void upDateParticleFaceVertex();//particle system call after update vertex//
    void upDateParticleFaceTexVertex(int texIndex);//particle system call after update TexVertex//
    void upDateParticleFaceColor();//particle system call after update FaceColor//

    void setParticleData(float* vertexData,unsigned short *indexData,int indexNum,bool hasVertex = false, bool hasTex0 = false, bool hasColor = false);
    void updateParticleData(int indexNum);

    void setVertexBuffer(SE_VertexBuffer * vb)
    {
        mVertexBuffer = vb;
    }
	  void setMaterialDataID(const SE_MaterialDataID& id);
    SE_MaterialDataID getMaterialDataID()
    {
        return mMaterialDataID;
    }
	  void setVertexBufferID(const SE_VertexBufferID& id);

    DrawMode getDrawMode()
    {
        return mDrawMode;
    }
    void setDrawMode(DrawMode drawmode)
    {
        mDrawMode = drawmode;
    }

    void getFaceVertexColorArray(_Vector4f*& colorarray,int &count);   

    SE_VertexBufferID getVertexBufferID()
    {
        return mVertexBufferID;
    }
   
	/*
	void setTexCoordIndex(int texIndex, int indexHasTexCoord)
	{
		if(texIndex < SE_TEXTURE0 || texIndex >= SE_TEXUNIT_NUM)
			return;
		mTexCoordIndex[texIndex] = indexHasTexCoord;
	}
	int getTexCoordIndex(int texIndex)
	{
		if(texIndex < SE_TEXTURE0 || texIndex >= SE_TEXUNIT_NUM)
			return SE_TEXTURE0;
		return mTexCoordIndex[texIndex];
	}

	*/
    

    void setUseStencil(bool use,SE_Spatial* mirrorSpatial = NULL,SE_MirrorPlane mirrorPlane = SE_XY,float translateAlignedAxis = 0.0);

    float getMirroredObjTranslate()
    {
        return mMirroredObjTranslate;
    }



    bool isUseStencil()
    {
        return mNeedUseStencil;
    }

    void setChangeStencilPermit(bool permit)
    {
        mChangeStencilPermit = permit;
    }

    bool isChangeStencilPermit()
    {
        return mChangeStencilPermit;
    }
    
    void setTexCoordDataIdForFree(const SE_TextureCoordDataID& id);
    
    void setRenderToFboName(const char* name)
    {
        mRenderToFboName = name;
    }

    const char* getRenderToFboName()
    {
        return mRenderToFboName.c_str();
    }

    void setUsedFboName(const char* name)
    {
        mUsedFboName = name;
    }

    const char* getUsedFboName()
    {
        return mUsedFboName.c_str();
    }

    void setNeedFboReplaceCurrentTexture(bool replace)
    {
        mNeedFboReplaceCurrentTexture = replace;
    }

    bool isNeedFboReplaceCurrentTexture()
    {
        return mNeedFboReplaceCurrentTexture;
    }

    void setFboReplaceTextureIndex(int index)
    {
        mFboReplaceCurrentTextureIndex = index;
    }

    int getFboReplaceTextureIndex()
    {
        return mFboReplaceCurrentTextureIndex;
    }

    void setNeedGenerateReflection(bool use)
    {
        mNeedGenerateReflection = use;
    }
    bool isNeedGenerateReflection()
    {
        return mNeedGenerateReflection;
    }

    void setNeedDrawReflection(bool need)
    {
        mNeedDrawReflection = need;
    }
    bool isNeedDrawReflection()
    {
        return mNeedDrawReflection;
    }

    void setMirrorInfo(SE_Spatial* mirrorSpatial,SE_MirrorPlane mirrorPlane);
private:
    void generateFaceNormalFromVertex();
    void generateFaceVertexNormalFromVertex();
    void generateFaceVertexIndexList();
    void removeElement(std::vector<SE_Light*> &vec,SE_Light* l);
    
private:
    SE_Texture* mTexture;
    SE_MaterialData* mMaterialData;
    SE_MaterialDataID mMaterialDataID;
    int* mFacetArray;
    int mFacetNum;
    SE_GeometryData* mGeometryData;
    SE_Vector3f mColor;
    SE_VertexBufferID mVertexBufferID;
    SE_ProgramDataID mProgramDataID;
	SE_RendererID mRendererID;

	_Vector3f* mVertex;
	int mVertexNum;

    _Vector3f* mFaceNormal;
    int mFaceNormalNum;

    _Vector3f* mFaceVertexNormal;
    int mFaceVertexNormalNum;


	_Vector2f* mTexVertex[SE_TEXUNIT_NUM];
	int mTexVertexNum[SE_TEXUNIT_NUM];

    _Vector3f* mFaceVertex;
    int mFaceVertexNum;

    _Vector2f* mFaceTexVertex[SE_TEXUNIT_NUM];
    int mFaceTexVertexNum[SE_TEXUNIT_NUM];


    _Vector3f* mTangentArray;
    int mTangentArrayNum;

    int* mIndex;
    int mIndexNum; 
    int* mIndexInGeometryData;
    int mIndexInGeometryDataNum;
	SE_ShaderProperty* mShaderProperty;

    bool mNeedBlend;

    float *mSkeletonWeightForSurface;
    float *SkeletonIndexForSurface;
    int mCurrentAnimationFrameIndex;
    std::string mCurrentBipedControllerID;
    SE_BipedController *mBipedController;

	//int mTextureMode;
	//SE_Vector3f mMarkColor[4];
	//int mTexCoordIndex[SE_TEXUNIT_NUM];
	//SE_ShaderColorOperation mShaderColorOperation;
    SE_VertexBuffer *mVertexBuffer; //no need free,resource manager will release it

    _Vector4f* mFaceVertexColor;
    int mFaceVertexColorNum;
    DrawMode mDrawMode;

    std::vector<std::vector<int> > mFaceVertexIndexList;

    bool mNeedUseStencil;
    bool mChangeStencilPermit;

    float mMirroredObjTranslate;
    

    //for free texData if the surface has no image-texture
    SE_TextureCoordDataID mTextureCoordDataIdForFree;

    bool mNeedRenderToFbo;
    bool mNeedUseFbo;
    std::string mRenderToFboName;
    std::string mUsedFboName;
    bool mNeedFboReplaceCurrentTexture;//true:replace texture0. false:additional current texture list
    int mFboReplaceCurrentTextureIndex; //0-3

    bool mNeedGenerateReflection;
    bool mNeedDrawReflection;

};
// SE_Mesh and SE_Surface , SE_Texture , SE_TextureUnit are the wrapper class 
// about the data they use. So they will not release the pointer they own.
// this pointer data are released by the data provider. The data provider can be 
// resource manager, or a primitive.
class SE_ENTRY SE_Mesh
{
public:
    SE_Mesh(int surfaceNum, int texNum);
    ~SE_Mesh();
    SE_GeometryData* getGeometryData()
	{
        return mGeometryData;
	}
    SE_Vector3f getColor()
    {
        return mColor;
    }
    SE_Texture* getTexture(int index)
    {
		if(index < 0 || index >= mTextureNum)
			return NULL;
        return mTextureArray[index];
    }
    SE_Surface* getSurface(int index)
    {
        return mSurfaceArray[index];
    }
	int getSurfaceNum()
	{
		return mSurfaceNum;
	}
    void setSurface(int index, SE_Surface* surface)
    {
        mSurfaceArray[index] = surface;
    }
    void setTexture(int index, SE_Texture* texture)
    {
        mTextureArray[index] = texture;
    }
    void setColor(const SE_Vector3f& c)
    {
        mColor = c;
    }
	void setGeometryDataID(const SE_GeometryDataID& id);
    void setGeometryData(SE_GeometryData* geomData)
    {
        mGeometryData = geomData;
    }
    void restorDefaultGeometryData();
    void clearVertexInfo();
    SE_Mesh* clone();

    //meshID same from meshTransferID,just for management
    void setMeshID(const SE_GeometryDataID& id)
    {
        mMeshID = id;
    }
    const char* getMeshID()
    {
        return mMeshID.getStr();
    }
private:
    SE_GeometryData* mGeometryData;
    SE_Surface** mSurfaceArray;
    int mSurfaceNum;
    SE_Texture** mTextureArray;
    int mTextureNum;
    SE_Vector3f mColor;
	SE_GeometryDataID mGeometryDataID;

    SE_MeshID mMeshID;
};
#endif
