#include "SE_DynamicLibType.h"
#include "SE_Quat.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include "SE_Light.h"
#include "SE_Common.h"
#include "SE_Mesh.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"
#include "SE_ShaderProperty.h"
#include "SE_Math.h"
#include "SE_Log.h"
#include "SE_Spatial.h"

#include "SE_ResourceManager.h"
#include "SE_Application.h"
#include <string.h>
#include <set>
#include "SE_ImageCodec.h"
#include "SE_MemLeakDetector.h"
SE_TextureUnit::SE_TextureUnit()
{
    mTexCoord = NULL;
    mImageDataIDArray = NULL;
    mImageDataIDNum = 0;
	mImageDataArray = NULL;
	mImageDataNum = 0;
	mSampleMin = MIPMAP;
    mSampleMag = LINEAR;
    mWrapS = REPEAT;
    mWrapT = REPEAT;
    mUTiling = 1.0;
    mVTiling = 1.0;
}
SE_TextureUnit::~SE_TextureUnit()
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    for(int i = 0 ; i < mImageDataIDNum; i++)
	{
		resourceManager->unregisterRes(SE_ResourceManager::IMAGE_RES, &mImageDataIDArray[i]);
        resourceManager->removeIdPath(mImageDataIDArray[i].getStr());
	}

    if(mImageDataIDArray)
    {
        mImageDataIDArray[0].print();
        delete[] mImageDataIDArray;
    }
	if(mImageDataArray)
    {
		delete[] mImageDataArray;
    }
    
	resourceManager->unregisterRes(SE_ResourceManager::TEXCOORD_RES, &mTextureCoordDataID);
}
void SE_TextureUnit::setTextureCoordDataID(const SE_TextureCoordDataID& id)
{
	mTextureCoordDataID = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::TEXCOORD_RES, &mTextureCoordDataID);
}
void SE_TextureUnit::setImageDataID(SE_ImageDataID* imageIDArray, int num)
{
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	if(mImageDataArray)
	{
		delete[] mImageDataArray;
		mImageDataArray = NULL;
		mImageDataNum = 0;
	}
	if(mImageDataIDArray)
	{
		for(int i = 0 ; i < mImageDataIDNum ; i++)
		{
			resourceManager->unregisterRes(SE_ResourceManager::IMAGE_RES, &mImageDataIDArray[i]);
		}
		delete[] mImageDataIDArray;
		mImageDataIDArray = NULL;
		mImageDataIDNum = 0;
	}
    mImageDataIDArray = imageIDArray;
    mImageDataIDNum = num;

    //clone should not invoke this
#ifdef WIN32
	for(int i = 0 ; i < mImageDataIDNum; i++)
	{
		resourceManager->registerRes(SE_ResourceManager::IMAGE_RES, &mImageDataIDArray[i]);
	}
#endif
}

void SE_TextureUnit::setImageDataID(SE_ImageDataID& id)
{
#ifdef WIN32
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    resourceManager->registerRes(SE_ResourceManager::IMAGE_RES, &id);
#endif
}

void SE_TextureUnit::setImageDataNum(int num)
{
    if(num <= 0)
            return;
        mImageDataArray = new SE_ImageData*[num];
        memset(mImageDataArray, 0, sizeof(SE_ImageData*) * num);
        if(!mImageDataArray)
            return;
        mImageDataNum = num;
        if(mImageDataIDArray)
        {
            delete[] mImageDataIDArray;
            mImageDataIDArray = NULL;
            mImageDataIDNum = 0;
        }

}
bool SE_TextureUnit::replaceImageData(const char *filePath,int imaIndex)
{
#if 0
#ifdef ANDROID
    SE_ImageData* img = SE_ImageCodec::loadAsset(filePath);
#else
    SE_ImageData* img = SE_ImageCodec::load(filePath);
#endif       

        SE_ImageDataID id = getImageDataID(imaIndex);

        SE_ResourceManager* resm = SE_Application::getInstance()->getResourceManager();
        return resm->replaceImageData(id,img);
#endif
        return false;
}
//////////
SE_Surface::SE_Surface()
{
    mTexture = NULL;
    mMaterialData = NULL;
    mFacetArray = NULL;
    mFacetNum = NULL;

	mVertex = NULL;
	mVertexNum = 0;
	memset(mTexVertex, 0, sizeof(_Vector2f*) * SE_TEXUNIT_NUM);
    memset(mTexVertexNum, 0, sizeof(int) * SE_TEXUNIT_NUM);
    mFaceVertex = NULL;
    mFaceVertexNum = 0;
    memset(mFaceTexVertex, 0, sizeof(_Vector2f*) * SE_TEXUNIT_NUM);
    memset(mFaceTexVertexNum, 0, sizeof(int) * SE_TEXUNIT_NUM);

    mFaceNormal = NULL;
    mFaceNormalNum = 0;

    mFaceVertexNormal = NULL;
    mFaceVertexNormalNum = 0;


    mIndex = NULL;
    mIndexNum = 0; 
    mIndexInGeometryData = NULL;
    mIndexInGeometryDataNum = 0;
	mShaderProperty = NULL;

    mSkeletonWeightForSurface = NULL;
    SkeletonIndexForSurface = NULL;
    mBipedController = NULL;
    mTangentArray = NULL;

    mVertexBuffer = NULL;

    mFaceVertexColor = NULL;
    mFaceVertexColorNum = 0;
    //memset(mTexCoordIndex, 0, sizeof(int) * SE_TEXUNIT_NUM);
	//memset(mColorChannelIndex, 0, sizeof(int) * 4);

    mDrawMode = DRAW_COLOR;

    mNeedUseStencil = false;
    mChangeStencilPermit = false;
    mNeedBlend = false;
    mMirroredObjTranslate = 0.0;
    

    mNeedRenderToFbo = false;
    mNeedUseFbo = false;

    mNeedFboReplaceCurrentTexture = false;
    mFboReplaceCurrentTextureIndex = 0;//default replace texture0,if mFboReplaceCurrentTexture is true

    mNeedGenerateReflection = false;
    mNeedDrawReflection = false;
    


}
SE_Surface::~SE_Surface()
{
    if(mFacetArray)
    {
        delete[] mFacetArray;
    }
	if(mVertex)
    {
		delete[] mVertex;
    }
	for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
	{
	    if(mTexVertex[i])
        {
		    delete[] mTexVertex[i];
        }
		if(mFaceTexVertex[i])
        {
            delete[] mFaceTexVertex[i];
	}
    }
    if(mFaceVertex)
    {
        delete[] mFaceVertex;

    }
    if(mIndex)
    {
        delete[] mIndex;
    }
	if(mIndexInGeometryData)
    {
        delete[] mIndexInGeometryData;
    }
	if(mShaderProperty)
    {
		delete mShaderProperty;
    }

    if(mSkeletonWeightForSurface)
    {
        delete[] mSkeletonWeightForSurface;
    }

    if(SkeletonIndexForSurface)
    {
        delete[] SkeletonIndexForSurface;
    }

    if(mTangentArray)
    {
        delete[] mTangentArray;
    }
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->unregisterRes(SE_ResourceManager::MATERIAL_RES, &mMaterialDataID);
	//resourceManager->unregisterRes(SE_ResourceManager::SHADER_RES, &mProgramDataID);
	//resourceManager->unregisterRes(SE_ResourceManager::RENDERER_RES, &mRendererID);
	resourceManager->unregisterRes(SE_ResourceManager::VERTEXBUFFER_RES, &mVertexBufferID);

    //the surface has no image texture,free its texture coordinate info here!
    if(!mTexture)
    {
        resourceManager->unregisterRes(SE_ResourceManager::TEXCOORD_RES, &mTextureCoordDataIdForFree);     
    }

    if(mFaceVertexColor)
    {
        delete[] mFaceVertexColor;
    }

    if(mFaceVertexNormal)
    {
        delete[] mFaceVertexNormal;
    }

    if(mFaceNormal)
    {
        delete[] mFaceNormal;
    }
}
void SE_Surface::setShaderProperty(SE_ShaderProperty* sp)
{
	if(mShaderProperty)
		delete mShaderProperty;
	mShaderProperty = sp;
}
SE_GeometryData* SE_Surface::getGeometryData()
{
    return mGeometryData;
}
SE_Texture* SE_Surface::getTexture()
{
    return mTexture;
}
SE_MaterialData* SE_Surface::getMaterialData()
{
    return mMaterialData;
}
int SE_Surface::getFacetNum()
{
    return mFacetNum;
}
int* SE_Surface::getFacetArray()
{
    return mFacetArray;
}

void SE_Surface::setGeometryData(SE_GeometryData* geomData)
{
    mGeometryData = geomData;
}
void SE_Surface::setMaterialData(SE_MaterialData* materialData)
{
    mMaterialData = materialData;
}
void SE_Surface::setTexture(SE_Texture* texture)
{
    mTexture = texture;
}
void SE_Surface::setFacets(int* facets, int num)
{
    mFacetArray = facets;
    mFacetNum = num;
}
void SE_Surface::setColor(const SE_Vector3f& color)
{
    mColor = color;
}
SE_Vector3f SE_Surface::getColor()
{
    return mColor;
}
void SE_Surface::setNeedBlend(bool blend)
{
    mNeedBlend = blend;
}
bool SE_Surface::isNeedBlend()
{
    return mNeedBlend;
}
SE_ProgramDataID SE_Surface::getProgramDataID()
{
    return mProgramDataID;
}
SE_RendererID SE_Surface::getRendererID()
{
	return mRendererID;
}
void SE_Surface::getVertexIndexInGeometryData(int*& outArray, int& outNum)
{
    if(mIndexInGeometryData)
    {
        outArray = mIndexInGeometryData;
        outNum = mIndexInGeometryDataNum;
        return;
    }    
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    SE_Vector3i* faceArray = geomData->getFaceArray();
    mIndexInGeometryData = new int[facetNum * 3];
    mIndexInGeometryDataNum = facetNum * 3;
    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        SE_Vector3i face = faceArray[facets[i]];
        mIndexInGeometryData[k++] = face.x;
        mIndexInGeometryData[k++] = face.y;
        mIndexInGeometryData[k++] = face.z;
    }
    outArray = mIndexInGeometryData;
    outNum = mIndexInGeometryDataNum;
}
void SE_Surface::getFaceVertex(_Vector3f*& vertex, int& vertexNum)
{
    if(mVertex)
    {
        delete[] mVertex;
        mVertex = NULL;
        mVertexNum = 0;
    }
    if(mFaceVertex != NULL)
    {
        vertex = mFaceVertex;
        vertexNum = mFaceVertexNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
    mFaceVertex = new _Vector3f[facetNum * 3];//(_Vector3f*)malloc(sizeof(_Vector3f) * facetNum * 3);//
    mFaceVertexNum = facetNum * 3;
    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        SE_Vector3i f = faceArray[facets[i]];
        mFaceVertex[k].d[0] = vertexArray[f.x].x;
        mFaceVertex[k].d[1] = vertexArray[f.x].y;
        mFaceVertex[k].d[2] = vertexArray[f.x].z;
        /*
        LOGI("mFaceVertex[%d].d[0] = %f\n",k,vertexArray[f.x].x);
        LOGI("mFaceVertex[%d].d[1] = %f\n",k,vertexArray[f.x].y);
        LOGI("mFaceVertex[%d].d[2] = %f\n",k,vertexArray[f.x].z);
        */
        k++;
        mFaceVertex[k].d[0] = vertexArray[f.y].x;
        mFaceVertex[k].d[1] = vertexArray[f.y].y;
        mFaceVertex[k].d[2] = vertexArray[f.y].z;
        /*
        LOGI("mFaceVertex[%d].d[0] = %f\n",k,vertexArray[f.y].x);
        LOGI("mFaceVertex[%d].d[1] = %f\n",k,vertexArray[f.y].y);
        LOGI("mFaceVertex[%d].d[2] = %f\n",k,vertexArray[f.y].z);
        */
        k++;
        mFaceVertex[k].d[0] = vertexArray[f.z].x;
        mFaceVertex[k].d[1] = vertexArray[f.z].y;
        mFaceVertex[k].d[2] = vertexArray[f.z].z;
        /*
        LOGI("mFaceVertex[%d].d[0] = %f\n",k,vertexArray[f.z].x);
        LOGI("mFaceVertex[%d].d[1] = %f\n",k,vertexArray[f.z].y);
        LOGI("mFaceVertex[%d].d[2] = %f\n",k,vertexArray[f.z].z);
        */
        k++;
    }
    vertex = mFaceVertex;
    vertexNum = mFaceVertexNum;
}

void SE_Surface::getLineVertex(_Vector3f*& vertex, int& vertexNum)
{
    if(mVertex)
    {
        delete[] mVertex;
        mVertex = NULL;
        mVertexNum = 0;
    }
    if(mFaceVertex != NULL)
    {
        vertex = mFaceVertex;
        vertexNum = mFaceVertexNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();

    int facetNum = getFacetNum();//line number

    int* facets = getFacetArray();//line
    SE_Vector3i* faceArray = geomData->getFaceArray();//line

    SE_Vector3f* vertexArray = geomData->getVertexArray();//line points

    mFaceVertex = new _Vector3f[facetNum * 2];//2 points per line
    mFaceVertexNum = facetNum * 2;//2 points per line

    int k = 0;
    for(int i = 0 ; i < mFaceVertexNum ; i++)
    {        

        mFaceVertex[i].d[0] = vertexArray[i].x;
        mFaceVertex[i].d[1] = vertexArray[i].y;
        mFaceVertex[i].d[2] = vertexArray[i].z;
        /*
        LOGI("mFaceVertex[%d].d[0] = %f\n",k,vertexArray[f.x].x);
        LOGI("mFaceVertex[%d].d[1] = %f\n",k,vertexArray[f.x].y);
        LOGI("mFaceVertex[%d].d[2] = %f\n",k,vertexArray[f.x].z);
        */
        
    }
    vertex = mFaceVertex;
    vertexNum = mFaceVertexNum;
}

void SE_Surface::getFaceVertexColorArray(_Vector4f*& colorarray,int &count)
{
    if(mVertex)
    {
        delete[] mVertex;
        mVertex = NULL;
        mVertexNum = 0;
    }
    if(mFaceVertexColor != NULL)
    {        
        colorarray = mFaceVertexColor;
        count = mFaceVertexColorNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    SE_Vector3i* faceArray = geomData->getFaceArray();

    SE_Vector4f* colorArray = geomData->getColorArray();

    mFaceVertexColor = new _Vector4f[facetNum * 3];//(_Vector3f*)malloc(sizeof(_Vector3f) * facetNum * 3);//

    mFaceVertexColorNum = facetNum * 3;

    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        SE_Vector3i f = faceArray[facets[i]];

        mFaceVertexColor[k].d[0] = colorArray[f.x].x;
        mFaceVertexColor[k].d[1] = colorArray[f.x].y;
        mFaceVertexColor[k].d[2] = colorArray[f.x].z;
        mFaceVertexColor[k].d[3] = colorArray[f.x].w;
        k++;

        mFaceVertexColor[k].d[0] = colorArray[f.y].x;
        mFaceVertexColor[k].d[1] = colorArray[f.y].y;
        mFaceVertexColor[k].d[2] = colorArray[f.y].z;
        mFaceVertexColor[k].d[3] = colorArray[f.y].w;
        k++;

        mFaceVertexColor[k].d[0] = colorArray[f.z].x;
        mFaceVertexColor[k].d[1] = colorArray[f.z].y;
        mFaceVertexColor[k].d[2] = colorArray[f.z].z;
        mFaceVertexColor[k].d[3] = colorArray[f.z].w;
        k++;
    }
    colorarray = mFaceVertexColor;
    count = mFaceVertexColorNum;
}

void SE_Surface::getFaceNormal(_Vector3f*& faceNormal, int & faceNormalNum)
{
    if(mFaceNormal)
    {
        faceNormal = mFaceNormal;
        faceNormalNum = mFaceNormalNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();    

    if(geomData->getFaceNormalsNum() > 0)
    {        

        mFaceNormal = new _Vector3f[facetNum];

        SE_Vector3f* faceN = geomData->getFaceNormals();
        for(int i = 0; i < facetNum; ++i)
        {
            int index = facets[i];
            mFaceNormal[i].d[0] = faceN[index].x;
            mFaceNormal[i].d[1] = faceN[index].y;
            mFaceNormal[i].d[2] = faceN[index].z;
        }
        mFaceNormalNum = geomData->getFaceNormalsNum();
    }
    else
    {
        LOGI("the object no face normal data,generate...\n");
        generateFaceNormalFromVertex();        
    }

    faceNormal = mFaceNormal;
    faceNormalNum = mFaceNormalNum;   

}
void SE_Surface::updateVertexNormal()
{
    if(mFaceVertexNormal)
    {
        delete mFaceVertexNormal;
        mFaceVertexNormalNum = 0;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();    

    int fVertexNormalNum = geomData->getFaceVertexNormalsNum();

    if(fVertexNormalNum > 0)
    {        
        mFaceVertexNormal = new _Vector3f[facetNum * 3];

        SE_Vector3f* faceN = geomData->getFaceVertexNormals();
        for(int i = 0; i < facetNum; ++i)
        {
            int k = facets[i];
            //p0
            mFaceVertexNormal[i * 3].d[0] = faceN[k * 3].x;
            mFaceVertexNormal[i * 3].d[1] = faceN[k * 3].y;
            mFaceVertexNormal[i * 3].d[2] = faceN[k * 3].z; 

            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3,faceN[k * 3].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3,faceN[k * 3].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3,faceN[k * 3].z);
            }
            

            //p1
            mFaceVertexNormal[i * 3 + 1].d[0] = faceN[k * 3 + 1].x;
            mFaceVertexNormal[i * 3 + 1].d[1] = faceN[k * 3 + 1].y;
            mFaceVertexNormal[i * 3 + 1].d[2] = faceN[k * 3 + 1].z;

            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3 + 1,faceN[k * 3 + 1].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3 + 1,faceN[k * 3 + 1].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3 + 1,faceN[k * 3 + 1].z);
            }
            

            //p2
            mFaceVertexNormal[i * 3 + 2].d[0] = faceN[k * 3 + 2].x;
            mFaceVertexNormal[i * 3 + 2].d[1] = faceN[k * 3 + 2].y;
            mFaceVertexNormal[i * 3 + 2].d[2] = faceN[k * 3 + 2].z;
            
            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3 + 2,faceN[k * 3 + 2].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3 + 2,faceN[k * 3 + 2].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3 + 2,faceN[k * 3 + 2].z);
            }         
            
        }

        mFaceVertexNormalNum = facetNum * 3;
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("\n\nthe object no vertex normal data,generate...\n\n");
        generateFaceVertexNormalFromVertex();
    }    
}
void SE_Surface::getFaceVertexNormal(_Vector3f*& faceVertexNormal, int & faceVertexNormalNum)
{
    if(mFaceVertexNormal)
    {
        faceVertexNormal = mFaceVertexNormal;
        faceVertexNormalNum = mFaceVertexNormalNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();    

    int fVertexNormalNum = geomData->getFaceVertexNormalsNum();

    if(fVertexNormalNum > 0)
    {        
        mFaceVertexNormal = new _Vector3f[facetNum * 3];

        SE_Vector3f* faceN = geomData->getFaceVertexNormals();
        for(int i = 0; i < facetNum; ++i)
        {
            int k = facets[i];
            //p0
            mFaceVertexNormal[i * 3].d[0] = faceN[k * 3].x;
            mFaceVertexNormal[i * 3].d[1] = faceN[k * 3].y;
            mFaceVertexNormal[i * 3].d[2] = faceN[k * 3].z; 

            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3,faceN[k * 3].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3,faceN[k * 3].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3,faceN[k * 3].z);
            }
            

            //p1
            mFaceVertexNormal[i * 3 + 1].d[0] = faceN[k * 3 + 1].x;
            mFaceVertexNormal[i * 3 + 1].d[1] = faceN[k * 3 + 1].y;
            mFaceVertexNormal[i * 3 + 1].d[2] = faceN[k * 3 + 1].z;
            
            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3 + 1,faceN[k * 3 + 1].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3 + 1,faceN[k * 3 + 1].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3 + 1,faceN[k * 3 + 1].z);
            }
            

            //p2
            mFaceVertexNormal[i * 3 + 2].d[0] = faceN[k * 3 + 2].x;
            mFaceVertexNormal[i * 3 + 2].d[1] = faceN[k * 3 + 2].y;
            mFaceVertexNormal[i * 3 + 2].d[2] = faceN[k * 3 + 2].z;
            
            if(0)
            {
            LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3 + 2,faceN[k * 3 + 2].x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3 + 2,faceN[k * 3 + 2].y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3 + 2,faceN[k * 3 + 2].z);
            }         
            
        }

        mFaceVertexNormalNum = facetNum * 3;
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("\n\nthe object no vertex normal data,generate...\n\n");
        generateFaceVertexNormalFromVertex();
    }

    faceVertexNormal = mFaceVertexNormal;
    faceVertexNormalNum = mFaceVertexNormalNum;   

}

void SE_Surface::generateFaceVertexIndexList()
{
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();  
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
    int vNum = geomData->getVertexNum();


    for(int i = 0 ; i < vNum ; i++)
    {
        std::vector<int> indexlist;
        mFaceVertexIndexList.push_back(indexlist);
    }


    // which face the vertex belone to.
    for(int i = 0; i < facetNum; ++i)
    {
        int faceIndex = facets[i];
        _Vector3i vertexIndex;
        vertexIndex.d[0] = faceArray[faceIndex].x;
        vertexIndex.d[1] = faceArray[faceIndex].y;
        vertexIndex.d[2] = faceArray[faceIndex].z;

        mFaceVertexIndexList[vertexIndex.d[0]].push_back(i);
        mFaceVertexIndexList[vertexIndex.d[1]].push_back(i);
        mFaceVertexIndexList[vertexIndex.d[2]].push_back(i);
    }
}
void SE_Surface::generateFaceNormalFromVertex()
{
    if(mFaceNormalNum > 0)
    {
        delete mFaceNormal;
    }

    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();  
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
    
    //generate face normal
    mFaceNormalNum = facetNum;
    mFaceNormal = new _Vector3f[facetNum];

    SE_Vector3f prevNormal(1.0,0,0);
    for(int i = 0; i < facetNum; ++i)
    {
        int faceIndex = facets[i];
        _Vector3i vertexIndex;
        vertexIndex.d[0] = faceArray[faceIndex].x;
        vertexIndex.d[1] = faceArray[faceIndex].y;
        vertexIndex.d[2] = faceArray[faceIndex].z;

        SE_Vector3f localp[3];

        for(int i = 0; i < 3; ++i)
        {
            localp[i].set(vertexArray[vertexIndex.d[i]].x, vertexArray[vertexIndex.d[i]].y, vertexArray[vertexIndex.d[i]].z);            
        }

        SE_Vector3f e1 = localp[1] - localp[0];
        SE_Vector3f e2 = localp[2] - localp[1];

        SE_Vector3f facenormal;

        if(e1.length() <= 0.001 || e2.length() < 0.001)
        {
            facenormal = prevNormal;
        }
        else
        {
           facenormal = (e1.cross(e2)).normalize();  
        }

        prevNormal = facenormal;
        //save face normal to list
        mFaceNormal[i].d[0] = facenormal.x;
        mFaceNormal[i].d[1] = facenormal.y;
        mFaceNormal[i].d[2] = facenormal.z;
       
    }
    

}

void SE_Surface::generateFaceVertexNormalFromVertex()
{

    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();  
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
    
    //generate face vertex normal
    mFaceVertexNormalNum = facetNum * 3;
    mFaceVertexNormal = new _Vector3f[facetNum * 3];            
    for(int i = 0; i < facetNum; ++i)
    {
        bool useAverageValue = false;
        int faceIndex = facets[i];
        _Vector3i vertexIndex;
        vertexIndex.d[0] = faceArray[faceIndex].x;//p0
        vertexIndex.d[1] = faceArray[faceIndex].y;//p1
        vertexIndex.d[2] = faceArray[faceIndex].z;//p2

        if(!useAverageValue)
        {
            if(mFaceNormalNum == 0)
            {
                //face normal has not generate, yet
                generateFaceNormalFromVertex();
            }
            mFaceVertexNormal[i*3].d[0] = mFaceNormal[i].d[0];
            mFaceVertexNormal[i*3].d[1] = mFaceNormal[i].d[1];
            mFaceVertexNormal[i*3].d[2] = mFaceNormal[i].d[2];
            if(0)
            {
                LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3,mFaceNormal[i].d[0]);
                LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3,mFaceNormal[i].d[1]);
                LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3,mFaceNormal[i].d[2]);
            }

            mFaceVertexNormal[i*3 + 1].d[0] = mFaceNormal[i].d[0];
            mFaceVertexNormal[i*3 + 1].d[1] = mFaceNormal[i].d[1];
            mFaceVertexNormal[i*3 + 1].d[2] = mFaceNormal[i].d[2];
            if(0)
            {
                LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3 +1,mFaceNormal[i].d[0]);
                LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3+1,mFaceNormal[i].d[1]);
                LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3+1,mFaceNormal[i].d[2]);
            }

            mFaceVertexNormal[i*3 + 2].d[0] = mFaceNormal[i].d[0];
            mFaceVertexNormal[i*3 + 2].d[1] = mFaceNormal[i].d[1];
            mFaceVertexNormal[i*3 + 2].d[2] = mFaceNormal[i].d[2];
            if(0)
            {
                LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3+2,mFaceNormal[i].d[0]);
                LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3+2,mFaceNormal[i].d[1]);
                LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3+2,mFaceNormal[i].d[2]);
            }
        }
        else
        {
            if(mFaceNormalNum == 0)
            {
                //face normal has not generate, yet
                generateFaceNormalFromVertex();
            }

            if(mFaceVertexIndexList.size() == 0)
            {
                generateFaceVertexIndexList();
            }

            {
                //use average value
                int p0faceNum = mFaceVertexIndexList[vertexIndex.d[0]].size();//how many faces use this vertex
                SE_Vector3f p0normal;
                for(int i = 0; i < p0faceNum; ++i)
                {
                    SE_Vector3f facenormal;                    
                    int faceIndex = mFaceVertexIndexList[vertexIndex.d[0]][i];
                    //get face normal
                    facenormal.x = mFaceNormal[faceIndex].d[0];
                    facenormal.y = mFaceNormal[faceIndex].d[1];
                    facenormal.z = mFaceNormal[faceIndex].d[2];

                    p0normal = p0normal.add(facenormal);
                    
                }
                //get average value for this vertex
                p0normal.x = p0normal.x / p0faceNum;
                p0normal.y = p0normal.y / p0faceNum;
                p0normal.z = p0normal.z / p0faceNum;

                mFaceVertexNormal[i*3].d[0] = p0normal.x;
                mFaceVertexNormal[i*3].d[1] = p0normal.y;
                mFaceVertexNormal[i*3].d[2] = p0normal.z;

                if(0)
                {
	    LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3,p0normal.x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3,p0normal.y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3,p0normal.z);
            }
            }



            {
                int p1faceNum = mFaceVertexIndexList[vertexIndex.d[1]].size();//how many faces use this vertex
                SE_Vector3f p1normal;
                for(int i = 0; i < p1faceNum; ++i)
                {
                    SE_Vector3f facenormal;                    
                    int faceIndex = mFaceVertexIndexList[vertexIndex.d[1]][i];
                    //get face normal
                    facenormal.x = mFaceNormal[faceIndex].d[0];
                    facenormal.y = mFaceNormal[faceIndex].d[1];
                    facenormal.z = mFaceNormal[faceIndex].d[2];

                    p1normal = p1normal.add(facenormal);
                    
                }
                //get average value for this vertex
                p1normal.x = p1normal.x / p1faceNum;
                p1normal.y = p1normal.y / p1faceNum;
                p1normal.z = p1normal.z / p1faceNum;

                mFaceVertexNormal[i*3 + 1].d[0] = p1normal.x;
                mFaceVertexNormal[i*3 + 1].d[1] = p1normal.y;
                mFaceVertexNormal[i*3 + 1].d[2] = p1normal.z;
                if(0)
                {
 LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3+1,p1normal.x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3 +1,p1normal.y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3+1,p1normal.z);
            }
            }



            int p2faceNum = mFaceVertexIndexList[vertexIndex.d[2]].size();//how many faces use this vertex
            SE_Vector3f p2normal;
            for(int i = 0; i < p2faceNum; ++i)
            {
                SE_Vector3f facenormal;                    
                int faceIndex = mFaceVertexIndexList[vertexIndex.d[2]][i];
                //get face normal
                facenormal.x = mFaceNormal[faceIndex].d[0];
                facenormal.y = mFaceNormal[faceIndex].d[1];
                facenormal.z = mFaceNormal[faceIndex].d[2];

                p2normal = p2normal.add(facenormal);
                
            }
            //get average value for this vertex
            p2normal.x = p2normal.x / p2faceNum;
            p2normal.y = p2normal.y / p2faceNum;
            p2normal.z = p2normal.z / p2faceNum;

            mFaceVertexNormal[i*3 + 2].d[0] = p2normal.x;
            mFaceVertexNormal[i*3 + 2].d[1] = p2normal.y;
            mFaceVertexNormal[i*3 + 2].d[2] = p2normal.z;
            if(0)
            {
 LOGI("mFaceVertexNormal[%d].d0 = %f\n",i * 3+2,p2normal.x);
            LOGI("mFaceVertexNormal[%d].d1 = %f\n",i * 3+2,p2normal.y);
            LOGI("mFaceVertexNormal[%d].d2 = %f\n",i * 3+2,p2normal.z);
            }

        }
    }
}

void SE_Surface::getVertexTangent(_Vector3f*& vertexTangent, int& vertexTangentNum)
{    
    if(mTangentArray != NULL)
    {
        vertexTangent = mTangentArray;
        vertexTangentNum = mTangentArrayNum;
        return;
    }
    _Vector3f *faceVertex = NULL;
    int faceVertexNum = 0;

    _Vector2f *faceTextueVertex = NULL;
    int faceTextureVertexNum = 0;
    
    getFaceVertex(faceVertex,faceVertexNum);

    getFaceTexVertex(0, faceTextueVertex, faceTextureVertexNum);

    if(!faceTextueVertex)
    {
        return;
    }
    mTangentArray = new _Vector3f[faceVertexNum];
    mTangentArrayNum = faceVertexNum;

    for(int i = 0; i < faceVertexNum;i += 3)
    {

        SE_Vector3f result,a,b,c;
        float au,av,bu,bv,cu,cv;

        a.set(faceVertex[i].d[0],faceVertex[i].d[1],faceVertex[i].d[2]);
        au = faceTextueVertex[i].d[0];
        av = faceTextueVertex[i].d[1];


        b.set(faceVertex[i+1].d[0],faceVertex[i+1].d[1],faceVertex[i+1].d[2]);
        bu = faceTextueVertex[i+1].d[0];
        bv = faceTextueVertex[i+1].d[1];


        c.set(faceVertex[i+2].d[0],faceVertex[i+2].d[1],faceVertex[i+2].d[2]);
        cu = faceTextueVertex[i+2].d[0];
        cv = faceTextueVertex[i+2].d[1];

        

        //point
        result = SE_Math::calculateTangentSpaceVector(a,b,c,au,av,bu,bv,cu,cv);

        mTangentArray[i].d[0] = result.x;
        mTangentArray[i].d[1] = result.y;
        mTangentArray[i].d[2] = result.z;

        mTangentArray[i+1].d[0] = result.x;
        mTangentArray[i+1].d[1] = result.y;
        mTangentArray[i+1].d[2] = result.z; 

        mTangentArray[i+2].d[0] = result.x;
        mTangentArray[i+2].d[1] = result.y;
        mTangentArray[i+2].d[2] = result.z; 

    }
    
    vertexTangent = mTangentArray;
    vertexTangentNum = mTangentArrayNum;
}

void SE_Surface::upDateFaceVertex()//call after update vertex, add by liusong//
{
    if(mFaceVertex)
    {
	delete[] mFaceVertex;
	mFaceVertex = NULL;
	mFaceVertexNum = 0;
    }
    updateVertexNormal();
}

void SE_Surface::upDateFaceTexVertex(int texIndex)//call after update vertex, add by liusong//
{
    if(mFaceTexVertex[texIndex])
    {
	delete[] mFaceTexVertex[texIndex];;
	mFaceTexVertex[texIndex] = NULL;
	mFaceTexVertexNum[texIndex] = 0;
    }
}
void SE_Surface::upDateParticleFaceVertex()//particle system call after update vertex//
{
    if(mFaceVertex)
    {
	delete[] mFaceVertex;
	mFaceVertex = NULL;
	mFaceVertexNum = 0;
    }
}
void SE_Surface::upDateParticleFaceTexVertex(int texIndex)//particle system call after update vertex//
{
    if(mFaceTexVertex[texIndex])
    {
	delete[] mFaceTexVertex[texIndex];;
	mFaceTexVertex[texIndex] = NULL;
	mFaceTexVertexNum[texIndex] = 0;
    }
}
void SE_Surface::upDateParticleFaceColor()//particle system call after update FaceColor//
{
    if(mFaceVertexColor)
    {
	delete[] mFaceVertexColor;
	mFaceVertexColor = NULL;
	mFaceVertexColorNum = 0;
    }
}

void SE_Surface::getFaceTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum)
{
	if(texIndex < SE_TEXTURE0 || texIndex >= SE_TEXUNIT_NUM)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    if(mTexVertex[texIndex])
    {
        delete[] mTexVertex[texIndex];
        mTexVertexNum[texIndex] = 0;
    }
    if(mFaceTexVertex[texIndex])
    {
        texVertex = mFaceTexVertex[texIndex];
        texVertexNum = mFaceTexVertexNum[texIndex];
        return;
    }
    SE_Texture* tex = getTexture();
    if(!tex)
    {
        texVertex = NULL;
        texVertexNum = 0;
        return;
    }
    SE_TextureUnit* texUnit = tex->getTextureUnit(texIndex);
    if(!texUnit)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    SE_TextureCoordData* texCoordData = texUnit->getTextureCoordData();
	if(!texCoordData)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    SE_Vector3i* texFaceArray = texCoordData->getTexFaceArray();
    SE_Vector2f* texVertexArray = texCoordData->getTexVertexArray();
	if(texVertexArray == NULL || texFaceArray == NULL)
	{
        texVertex = 0;
        texVertexNum = 0;
		return;
	}
    int texFaceNum = texCoordData->getTexFaceNum();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    mFaceTexVertex[texIndex] = new _Vector2f[facetNum * 3];
    mFaceTexVertexNum[texIndex] = facetNum * 3;
    int k = 0 ;
    for(int i = 0 ; i < facetNum ; i++)
    {
        SE_ASSERT(facets[i] < texFaceNum);
        SE_Vector3i f = texFaceArray[facets[i]];
        mFaceTexVertex[texIndex][k].d[0] = texVertexArray[f.x].x;
        mFaceTexVertex[texIndex][k].d[1] = texVertexArray[f.x].y;
        k++;
        mFaceTexVertex[texIndex][k].d[0] = texVertexArray[f.y].x;
        mFaceTexVertex[texIndex][k].d[1] = texVertexArray[f.y].y;
        k++;
        mFaceTexVertex[texIndex][k].d[0] = texVertexArray[f.z].x;
        mFaceTexVertex[texIndex][k].d[1] = texVertexArray[f.z].y;
        k++;
    }
    texVertex = mFaceTexVertex[texIndex];
    texVertexNum = mFaceTexVertexNum[texIndex];
}
void SE_Surface::getVertexIndex(int*& index, int& indexNum)
{
    if(mIndex)
    {
        index = mIndex;
        indexNum = mIndexNum;
        return;
    }
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    SE_GeometryData* geomData = getGeometryData();
    SE_Vector3i* faceArray = geomData->getFaceArray();
    mIndexNum = facetNum * 3;
    mIndex = new int[mIndexNum];
    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    { 
        SE_Vector3i f = faceArray[facets[i]];
        mIndex[k++] = f.x;
        mIndex[k++] = f.y;
        mIndex[k++] = f.z;
    } 
    index = mIndex;
    indexNum = mIndexNum;
}
void SE_Surface::getVertex(_Vector3f*& vertex, int & vertexNum)
{
    if(mFaceVertex)
    {
        delete[] mFaceVertex;
        mFaceVertexNum = 0;
    }
    if(mVertex)
    {
        vertex = mVertex;
        vertexNum = mVertexNum;
        return;
    }
    SE_GeometryData* geomData = getGeometryData();
    int facetNum = getFacetNum();
    int* facets = getFacetArray();
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
	mVertexNum = geomData->getVertexNum();
    mVertex = new _Vector3f[mVertexNum];
	for(int i = 0 ; i < mVertexNum ; i++)
    {
		mVertex[i].d[0] = vertexArray[i].x;
		mVertex[i].d[1] = vertexArray[i].y;
		mVertex[i].d[2] = vertexArray[i].z;
    }
    vertex = mVertex;
    vertexNum = mVertexNum;
}
void SE_Surface::getTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum)
{
	if(texIndex < 0 || texIndex >= SE_TEXUNIT_NUM)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    if(mFaceTexVertex[texIndex])
    {
        delete[] mFaceTexVertex[texIndex];
        mFaceTexVertex[texIndex] = NULL;
        mFaceTexVertexNum[texIndex] = 0;
    }
    if(mTexVertex[texIndex])
    {
        texVertex = mTexVertex[texIndex];
        texVertexNum = mTexVertexNum[texIndex];
        return;
    }
    SE_Texture* tex = getTexture();
    if(!tex)
    {
        texVertex = NULL;
        texVertexNum = 0;
        return;
    }
    SE_TextureUnit* texUnit = tex->getTextureUnit(texIndex);
    if(!texUnit)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    SE_TextureCoordData* texCoordData = texUnit->getTextureCoordData();
	if(!texCoordData)
	{
		texVertex = NULL;
		texVertexNum = 0;
		return;
	}
    SE_Vector3i* texFaceArray = texCoordData->getTexFaceArray();
    SE_Vector2f* texVertexArray = texCoordData->getTexVertexArray();
	if(texVertexArray == NULL || texFaceArray == NULL)
	{
        texVertex = 0;
        texVertexNum = 0;
		return;
	}
	mTexVertexNum[texIndex]  = texCoordData->getTexVertexNum();
	mTexVertex[texIndex] = new _Vector2f[mTexVertexNum[texIndex]];
	for(int i = 0 ; i < mTexVertexNum[texIndex] ; i++)
	{
		mTexVertex[texIndex][i].d[0] = texVertexArray[i].x;
		mTexVertex[texIndex][i].d[1] = texVertexArray[i].y;
	}
	texVertex = mTexVertex[texIndex];
	texVertexNum = mTexVertexNum[texIndex];
}
void SE_Surface::setProgramDataID(const SE_ProgramDataID& programID)
{
    mProgramDataID = programID;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::SHADER_RES, &mProgramDataID);
}
void SE_Surface::setRendererID(const SE_RendererID& id)
{
	mRendererID = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::RENDERER_RES, &mRendererID);
}
void SE_Surface::setMaterialDataID(const SE_MaterialDataID& id)
{
	mMaterialDataID = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::MATERIAL_RES, &mMaterialDataID);
}
void SE_Surface::setVertexBufferID(const SE_VertexBufferID& id)
{
	mVertexBufferID = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::VERTEXBUFFER_RES, &mVertexBufferID);
}
void SE_Surface::setParticleData(float *vertexData, unsigned short *indexData, int indexNum, bool hasVertex, bool hasTex0, bool hasColor) 
{
    if(mVertexBuffer)
    {
        //LOGI("\n\n\n ***particle data has init.***\n\n\n");
        return;
    }
    SE_VertexBufferID vbID = SE_ID::createVertexBufferID();
    mVertexBuffer = new SE_VertexBuffer();

    mVertexBuffer->setDyNamicDraw(true);
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    resourceManager->setVertexBuffer(vbID,mVertexBuffer);
    setVertexBufferID(vbID);

    if(hasVertex)
    {
        mVertexBuffer->addVBState(SE_VertexBuffer::VBS_VERTEXPOS);
    }

    if(hasTex0)
    {
        mVertexBuffer->addVBState(SE_VertexBuffer::VBS_TEXTURE0);
    }

    if(hasColor)
    {
        mVertexBuffer->addVBState(SE_VertexBuffer::VBS_VERTEXCOLOR);
    }

    mVertexBuffer->vertexData = vertexData;
    mVertexBuffer->indexData = indexData;
    mVertexBuffer->indexNum  = indexNum;
}

void SE_Surface::updateParticleData(int indexNum)
{
    if(mVertexBuffer)
    {
        mVertexBuffer->indexNum = indexNum;
    }
}
void SE_Surface::setUseStencil(bool use,SE_Spatial* mirrorSpatial,SE_MirrorPlane mirrorPlane,float translateAlignedAxis)
{
    mNeedUseStencil = use;
    mMirroredObjTranslate = translateAlignedAxis;
   
}

void SE_Surface::setTexCoordDataIdForFree(const SE_TextureCoordDataID& id)
{
    mTextureCoordDataIdForFree = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::TEXCOORD_RES, &mTextureCoordDataIdForFree);
}
///////
SE_Texture::SE_Texture()
{
    mTexUnitArray = new SE_TextureUnit*[SE_TEXUNIT_NUM];
    memset(mTexUnitArray, 0, sizeof(SE_TextureUnit*) * SE_TEXUNIT_NUM);
    mTextureUnitNum = 0;
}
SE_Texture::~SE_Texture()
{
    if(mTexUnitArray)
    {
        for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
        {
            if(mTexUnitArray[i])
                delete mTexUnitArray[i];
        }
        delete[] mTexUnitArray;
    }
}
void SE_Texture::setTextureUnit(int texType, SE_TextureUnit* texUnit)
{
    if(texType < 0 || texType >= SE_TEXUNIT_NUM)
        return ;
    mTexUnitArray[texType] = texUnit;
}
SE_TextureUnit* SE_Texture::getTextureUnit(int texType)
{
    if(texType < 0 || texType >= SE_TEXUNIT_NUM)
        return 0;
	return mTexUnitArray[texType];
}
////////
SE_Mesh::SE_Mesh(int surfaceNum, int texNum)
{
    mGeometryData = NULL;
    mSurfaceNum = surfaceNum;
    mTextureNum = texNum;
    mSurfaceArray = new SE_Surface*[mSurfaceNum];

    if(mTextureNum > 0)
    {
        mTextureArray = new SE_Texture*[mTextureNum];
    }
    else
    {
        mTextureArray = NULL;
    }
    memset(mSurfaceArray, 0, sizeof(SE_Surface*) * mSurfaceNum);
    memset(mTextureArray, 0, sizeof(SE_Texture*) * mTextureNum);
}
SE_Mesh::~SE_Mesh()
{
    for(int i = 0 ; i < mSurfaceNum ; i++)
    {
        if(mSurfaceArray[i])
        {
            delete mSurfaceArray[i];
    }
    }

    delete[] mSurfaceArray;


    if(mTextureArray != NULL)
    {

        for(int i = 0 ; i < mTextureNum ; i++)
        {
            if(mTextureArray[i])
            {
                delete mTextureArray[i];
            }
        }

        delete[] mTextureArray;
    }

	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->unregisterRes(SE_ResourceManager::GEOM_RES, &mGeometryDataID);

    resourceManager->unregisterRes(SE_ResourceManager::MESH_RES, &mMeshID);
}
void SE_Mesh::clearVertexInfo()
{
    for(int i = 0 ; i < mSurfaceNum ; i++)
    {
        SE_Surface* surface = mSurfaceArray[i];
        surface->clearVertexInfo();
    }
}
void SE_Mesh::setGeometryDataID(const SE_GeometryDataID& id)
{
	mGeometryDataID = id;
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	resourceManager->registerRes(SE_ResourceManager::GEOM_RES, &mGeometryDataID);
}

SE_Mesh* SE_Mesh::clone()
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_Mesh* dest = new SE_Mesh(mSurfaceNum,mTextureNum);

    //register geoID
    dest->setGeometryDataID(mGeometryDataID);
    dest->setGeometryData(resourceManager->getGeometryData(mGeometryDataID));

    if(mSurfaceNum == 0)
    {
        dest->setSurface(0, NULL); 
    }

    for(int i = 0; i < mTextureNum; ++i)
    {
        SE_Texture* texture = new SE_Texture();
        int textureUnitNum = this->getTexture(i)->getTexUnitNum();

        for(int j = 0; j < textureUnitNum; ++j)
        {
            SE_TextureUnit* tu = new SE_TextureUnit();
            SE_TextureCoordDataID id = this->getTexture(i)->getTextureUnit(j)->getTextureCoordDataID();

            tu->setTextureCoordData(resourceManager->getTextureCoordData(id));
            tu->setTextureCoordDataID(id);

            int imgnum = this->getTexture(i)->getTextureUnit(j)->getImageDataIDNum();

            if(imgnum > 0)
            {
            SE_ImageDataID* imageDataIDArray = new SE_ImageDataID[imgnum];
            for(int k = 0; k < imgnum; ++k)
            {
                imageDataIDArray[k] = this->getTexture(i)->getTextureUnit(j)->getImageDataID(k);
            }
            tu->setImageDataID(imageDataIDArray,imgnum);
            }
            else
            {
                tu->setImageDataID(NULL,0);
            }
            texture->setTextureUnit(0,tu);//default texture type is 0;            
        }
        texture->setTexUnitNum(textureUnitNum);
        dest->setTexture(i,texture);
    }

    for(int x = 0; x < mSurfaceNum; ++x)
    {
        SE_Surface* surface = new SE_Surface();


        surface->setMaterialData(resourceManager->getMaterialData(this->getSurface(x)->getMaterialDataID()));
		surface->setMaterialDataID(this->getSurface(x)->getMaterialDataID());

        surface->setProgramDataID(this->getSurface(x)->getProgramDataID());
        surface->setRendererID(this->getSurface(x)->getRendererID());

        int facetNum = this->getSurface(x)->getFacetNum();
        int* destfacetArray= new int[facetNum];
        int* srcfacetArray = this->getSurface(x)->getFacetArray();
        memcpy(destfacetArray,srcfacetArray,sizeof(int)*facetNum);

        surface->setGeometryData(dest->getGeometryData());
        surface->setFacets(destfacetArray, facetNum);

        SE_Texture* texture = dest->getTexture(0);
        surface->setTexture(texture);  


        SE_VertexBufferID vbid= this->getSurface(x)->getVertexBufferID();
        surface->setVertexBuffer(resourceManager->getVertexBuffer(vbid));
		surface->setVertexBufferID(vbid);
        dest->setSurface(x, surface);
    }

    
    //return
    return dest;

}

void SE_Mesh::restorDefaultGeometryData()
{
    SE_ResourceManager* rm = SE_Application::getInstance()->getResourceManager();
    SE_GeometryData* defaultData = rm->getGeometryData(mGeometryDataID);

    if(!defaultData)
    {
        LOGI("Error,mesh restorDefaultGeometryData fail!!!!!\n");
        return;
    }
    this->setGeometryData(defaultData);
    this->getSurface(0)->setGeometryData(defaultData);
    this->getSurface(0)->upDateFaceVertex();
}
