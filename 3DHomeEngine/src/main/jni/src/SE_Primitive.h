#ifndef SE_PRIMITIVE_H
#define SE_PRIMITIVE_H

#ifdef ANDROID
#include "SkBitmap.h"
#endif

#include "SE_Geometry3D.h"
#include "SE_Common.h"
#include "SE_MaterialData.h"
#include "SE_ID.h"
#include "SE_Common.h"
#include "SE_ImageData.h"
#include "SE_ImageCodec.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_Geometry.h"
class SE_Mesh;
class SE_GeometryData;
class SE_TextureCoordData;
class SE_Scene;
class SE_ENTRY SE_Primitive
{
public:
    //static SE_PrimitiveID normalizeRectPrimitiveID;
    //static SE_PrimitiveID normalizeCubePrimitiveID;
    virtual ~SE_Primitive() 
    {}
    virtual void createMesh(SE_Mesh**& outMesh, int& outMeshNum) 
    {
    }
    virtual SE_ImageData* getImageData()
    {
        return NULL;
    }
    virtual SE_PrimitiveID getPrimitiveID()
    {
        return SE_PrimitiveID();
    }
};
// primitive contain the data which used by SE_Mesh, so it can not allocate on stack
// it must be own by some global structure.
class SE_ENTRY SE_RectPrimitive : public SE_Primitive
{
private:
    struct _ImageData
    {
        SE_ImageData* imageData;
        SE_OWN_TYPE own;
        _ImageData()
        {
            imageData = NULL;
            own = NOT_OWN;
        }
        ~_ImageData()
        {
            if(own == OWN && imageData)
            {
                delete imageData;
                imageData = NULL;
            }
        }
    };
public:
    //SE_RectPrimitive(const SE_Rect3D& rect);
    static void create(const SE_Rect3D& rect, SE_RectPrimitive*& outPrimitive, SE_PrimitiveID& outPrimitiveID);
    SE_RectPrimitive* clone();
    ~SE_RectPrimitive();
    //when imageData's width and height is not power of 2
    //we need to adjust the texture coordidate
    void setImageData(SE_ImageData* imageData, SE_TEXUNIT_TYPE texUnitType, SE_OWN_TYPE own, SE_ImageDataPortion imageDataPortion = SE_ImageDataPortion::INVALID);
    //void setImagePortion(const SE_ImageDataPortion& portion);
    void setMaterialData(const SE_MaterialData& materialData)
    {
        if(mMaterialData)
            delete mMaterialData;
        mMaterialData = new SE_MaterialData;
        *mMaterialData = materialData;
    }
    void setColor(const SE_Vector3f& color)
    {
        mColor = color;
    }
    void setSampleMin(int smin)
    {
        mSampleMin = smin;
    }
    void setSampleMag(int smag)
    {
        mSampleMag = smag;
    }
    void setWrapS(int ws)
    {
        mWrapS = ws;
    }
    void setWrapT(int wt)
    {
        mWrapT = wt;
    }
    void setProgramDataID(const SE_ProgramDataID& programID)
    {
        mProgramDataID = programID;
    }

    SE_ImageData* getImageData()
    {
        return mImageData;
    }
    //virtual void read(SE_BufferInput& input);
    //virtual void write(SE_BufferOutput& output);
    virtual void createMesh(SE_Mesh**& outMesh, int& outMeshNum);
private:
    SE_RectPrimitive();
    SE_RectPrimitive(const SE_Rect3D& rect);
    SE_RectPrimitive(const SE_RectPrimitive&);
    SE_RectPrimitive& operator=(const SE_RectPrimitive&);

private:
    SE_Rect3D mRect3D;
    //_ImageData mImageDataArray[SE_Texture::TEXUNIT_NUM];
    //SE_GeometryData* mGeometryData;
    //SE_TextureCoordData* mTexCoordData;
    SE_Wrapper<_ImageData>* mImageDataArray[SE_TEXUNIT_NUM];
    SE_Wrapper<SE_GeometryData>* mGeometryData;
    SE_Wrapper<SE_TextureCoordData>* mTexCoordData;
    SE_MaterialData* mMaterialData;
    SE_ImageData* mImageData;
    SE_Vector3f mColor;
    int mSampleMin;
    int mSampleMag;
    int mWrapS;
    int mWrapT;
    SE_ProgramDataID mProgramDataID;
    SE_ImageDataPortion mImageDataPortion;
    int mAdjustedStartX;//the x coordinate after change width to power2 width
    int mAdjustedStartY;//the y coordinate after change height to power2 height
};

class SE_ENTRY SE_BoxPrimitive : public SE_Primitive
{
public:
    enum FACE_INDEX {LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK, ALL};
    static void create(const SE_Vector3f& scale, SE_BoxPrimitive*& outPrimitive, SE_PrimitiveID& outPrimitiveID);
    ~SE_BoxPrimitive();
    void setColor(FACE_INDEX index, const SE_Vector3f& color)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setColor(color);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setColor(color);
            }
        }
            
    }
    void setProgramDataID(FACE_INDEX index, const SE_ProgramDataID& programID)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setProgramDataID(programID);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setProgramDataID(programID);
            }
        }
    }
    void setImageData(FACE_INDEX index , SE_ImageData* imageData, SE_TEXUNIT_TYPE texUnitType, SE_OWN_TYPE own, 
                  SE_ImageDataPortion imageDataPortion = SE_ImageDataPortion::INVALID)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setImageData(imageData, texUnitType, own, imageDataPortion);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setImageData(imageData, texUnitType, own, imageDataPortion);
            }
        }
    }
    void setSampleMin(FACE_INDEX index ,int smin)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setSampleMin(smin);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setSampleMin(smin);
            }
        }
    }
    void setSampleMag(FACE_INDEX index ,int smag)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setSampleMag(smag);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setSampleMag(smag);
            }
        }
    }
    void setWrapS(FACE_INDEX index ,int ws)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setWrapS(ws);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setWrapS(ws);
            }
        }
    }
    void setWrapT(FACE_INDEX index ,int wt)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setWrapT(wt);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setWrapT(wt);
            }
        }
    }
    void setMaterialData(FACE_INDEX index ,const SE_MaterialData& materialData)
    {
        if(index < LEFT || index > ALL)
            return;
        if(index < ALL)
        {
            if(mRectPrimitive[index])
                mRectPrimitive[index]->setMaterialData(materialData);
        }
        else
        {
            for(int i = LEFT; i < ALL ; i++)
            {
                if(mRectPrimitive[i])
                    mRectPrimitive[i]->setMaterialData(materialData);
            }
        }
    }
    void createMesh(SE_Mesh**& outMesh, int& outMeshNum);
    SE_BoxPrimitive* clone();
private:
    SE_BoxPrimitive();
    SE_BoxPrimitive(const SE_Rect3D& rect);
    SE_BoxPrimitive(const SE_RectPrimitive&);
    SE_BoxPrimitive& operator=(const SE_RectPrimitive&);

private:
    SE_RectPrimitive* mRectPrimitive[6];
    SE_PrimitiveID mRectPrimitiveID[6];
    SE_Vector3f mScale;
};
/////////////////////////////////////////////////////////////
class SE_ENTRY SE_ObjectCreator
{
public:
    SE_ObjectCreator();
    SE_Spatial* create(SE_Scene* scene, SE_Spatial* parent);
    ~SE_ObjectCreator();
    void setImageDataID(SE_ImageDataID& imageDataID)
    {
        mImageDataIDArray = new SE_ImageDataID[1];
        mImageDataIDArray[0] = imageDataID;
    }
    void setObjectName(const char* objectName)
    {
        mObjectName = objectName;
    }
    const char* getObjectName()
    {
        return mObjectName;
    }
    void setColor(const SE_Vector3f& color)
    {
        mColor = color;
    }
    void setSampleMin(int smin)
    {
        mSampleMin = smin;
    }
    void setSampleMag(int smag)
    {
        mSampleMag = smag;
    }
    void setWrapS(int ws)
    {
        mWrapS = ws;
    }
    void setWrapT(int wt)
    {
        mWrapT = wt;
    }

    void setVertexArray(SE_Vector3f* vertexArray, int vertexNum)
    {
        mVertexArray = vertexArray;
        mVertexNum = vertexNum;
    }

    void setVertexIndexArray(SE_Vector3i* vertexIndexArray, int vertexIndexNum)
    {
        mVertexIndexArray = vertexIndexArray;
        mVertexIndexNum = vertexIndexNum;
    }

    void setTextureCoorArray(SE_Vector2f* textureCoorArray, int textureCoorNum)
    {
        mTextureCoorArray = textureCoorArray;
        mTextureCoorNum = textureCoorNum;
    }

    void setTextureCoorIndexArray(SE_Vector3i* textureCoorIndexArray, int textureCoorIndexNum)
    {
        mTextureCoorIndexArray = textureCoorIndexArray;
        mTextureCoorIndexNum = textureCoorIndexNum;
    }

    void setFacetIndexArray(int* facetIndexArray, int facetIndexNum)
    {
        mFacetIndexArray = facetIndexArray;
        mFacetIndexNum = facetIndexNum;
    }

    void setLayerIndex(int layerIndex)
    {
        mLayerIndex = layerIndex;
    } 
    
    int getLayerIndex()
    {
        return mLayerIndex;
    }

    void setIfLastLayerInWorld(bool lastLayer)
    {
        mLastLayerInWorld = lastLayer;
    }
    
    bool getIfLastLayerInWorld()
    {
        return mLastLayerInWorld;
    } 

    void setColor(SE_Vector3f& color)
    {
        mColor = color;
    }

    void setAlpha(float alpha)
    {
        mAlpha = alpha;
    }

    void setLocalRotate(SE_Quat& localRotate)
    {
        mLocalRotate = localRotate;
    }

    void setLocalTranslate(SE_Vector3f& localTranslate)
    {
        mLocalTranslate = localTranslate;
    }

    void setLocalScale(SE_Vector3f& localScale)
    {
        mLocalScale = localScale;
    }
    
    void setUserRotate(SE_Quat& userRotate)
    {
        mUserRotate = userRotate;
    }

    void setUserTranslate(SE_Vector3f& userTranslate)
    {
        mUserTranslate = userTranslate;
    }

    void setUserScale(SE_Vector3f& userScale)
    {
        mUserScale = userScale;
    }
  
    void setBvType(int type)
    {
        mBvType = type;
    }

    int getBvType()
    {
        return mBvType;
    }


    void createMesh(SE_Mesh**& outMesh, int& outMeshNum);

    void setLineWidth(float lineWidth)
    {
        mLineWidth = lineWidth;
    }

private:    
/*  
    void resizeImage();
*/
    SE_Vector3f mColor;
    int mSampleMin;
    int mSampleMag;
    int mWrapS;
    int mWrapT;
    SE_Vector3f* mVertexArray;
    int mVertexNum;
    SE_Vector3i* mVertexIndexArray;
    int mVertexIndexNum;
    SE_Vector2f* mTextureCoorArray;
    int mTextureCoorNum;
    SE_Vector3i* mTextureCoorIndexArray;
    int mTextureCoorIndexNum;
    int* mFacetIndexArray;
    int mFacetIndexNum;
    int mLayerIndex;
    bool mLastLayerInWorld;
    float mAlpha;
    const char* mObjectName;
    SE_Quat mLocalRotate;
    SE_Vector3f mLocalTranslate;
    SE_Vector3f mLocalScale;
    SE_Quat mUserRotate;
    SE_Vector3f mUserTranslate;
    SE_Vector3f mUserScale;
    int mTextureMode;
    int mBvType;
    int mState;
    int mRenderState;
    SE_ImageDataID* mImageDataIDArray;
    SE_GeometryDataID mGeometryDataID;
    SE_TextureCoordDataID mTexCoordDataID;
    SE_SpatialID mSpatialID;
    SE_MaterialDataID mMaterialDataID;
    int mLineWidth;
 
};
#endif
