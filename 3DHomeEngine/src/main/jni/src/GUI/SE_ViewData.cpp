#include "SE_DynamicLibType.h"
#include "SE_ViewData.h"
#include "SE_RootView.h"

#include "SE_Mesh.h"
#include "SE_Application.h"

#include "SE_Spatial.h"
#include "SE_Geometry.h"
#include "SE_SceneManager.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"

#include "SE_ImageCodec.h"
#include "SE_ResourceManager.h"
#include "SE_MaterialData.h"
#include "SE_ImageData.h"
#include "SE_Log.h"

ViewData::ViewData(SE_Spatial* spatial):SE_MeshSimObject(spatial)
{
	mX = 0.0f;
	mY = 0.0f;
	mWidth = 0;
	mHeight = 0;

	mHidden = false;

	SE_SimObjectID id = SE_ID::createSimObjectID();
	setID(id);
	/*if(!SE_Application::getInstance()->getSimObjectManager()->set(id, this))
    {
        LOGI("view data init fail!!!!\n");
        delete this;
    }*/
	
}



void ViewData::Init(float x, float y, float w, float h, const char* imageName )
{
	//mX = x;
	//mY = y;
	mWidth = w;
	mHeight = h;

	SE_Vector3f* mVertex = new SE_Vector3f[4];
	SE_Vector2f* mTexCoord = new SE_Vector2f[4];
	SE_Vector3f* mColor = new SE_Vector3f[4];
	SE_Vector3i* mIndexes = new SE_Vector3i[2];
	int* mFacet = new int[2];

	mIndexes[0].x = 0; // + 0;, for clarity
	mIndexes[0].y = 2;
	mIndexes[0].z = 1;
	mIndexes[1].x = 1;
	mIndexes[1].y = 2;
	mIndexes[1].z = 3;

	mFacet[0] = 0;
	mFacet[1] = 1;

    // Left-top
    // Positions
	mVertex[0].x = mX - mWidth/2;
	mVertex[0].y = mY + mHeight/2;
	mVertex[0].z = 0;

    // Texture coords
	mTexCoord[0].x = 0;
	mTexCoord[0].y = 1;

    // Right-top
    // Positions
	mVertex[1].x = mX + mWidth/2;
	mVertex[1].y = mY + mHeight/2;
	mVertex[1].z = 0;

    // Texture coords
	mTexCoord[1].x = 1;
	mTexCoord[1].y = 1;


    // Left-bottom
    // Positions
	mVertex[2].x = mX - mWidth/2;
	mVertex[2].y = mY - mHeight/2;
	mVertex[2].z = 0;

	mTexCoord[2].x = 0;
	mTexCoord[2].y = 0;


    // Right-bottom
    // Positions
	mVertex[3].x = mX + mWidth/2;
	mVertex[3].y = mY - mHeight/2;
	mVertex[3].z = 0;

    // Texture coords
	mTexCoord[3].x = 1;
	mTexCoord[3].y = 0;


    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_GeometryData* geometryData = new SE_GeometryData;

    resourceManager->setGeometryData(getName(), geometryData);

    geometryData->setVertexArray(mVertex, 4);
    geometryData->setFaceArray(mIndexes,  2);


    SE_TextureCoordData* texCoordData = new SE_TextureCoordData;
    resourceManager->setTextureCoordData(getName(), texCoordData);
    texCoordData->setTexVertexArray(mTexCoord, 4);
    texCoordData->setTexFaceArray(mIndexes, 2);

    SE_MaterialData* materialData = new SE_MaterialData;
    resourceManager->setMaterialData(getName(), materialData);

    int wrapS = 1;
    int wrapT = 1;
	SE_TextureUnit* texUnit = new SE_TextureUnit();	
    texUnit->setImageDataNum(1);
    texUnit->setTextureCoordData(texCoordData);
    texUnit->setWrapS(wrapS);
    texUnit->setWrapT(wrapT);  
	
	if(strlen(imageName)>0){
		SE_ImageDataID imageDataid(imageName);
	    SE_ImageData* imgd = resourceManager->getImageData(imageDataid);
	    if (!imgd) {  
		#ifdef WIN32 
			imgd = SE_ImageCodec::load(imageName);
		#endif
		#ifdef ANDROID
	        imgd = SE_ImageCodec::loadAsset(imageName);
		#endif
	        if (imgd) {
                resourceManager->insertPathImageData(imageName, imgd); 
                resourceManager->setIdPath(imageDataid.getStr(),imageName);	           
	        }
	    }
		texUnit->setImageData(0, imgd);
	}

	
	SE_Texture* texture = new SE_Texture;
    texture->setTextureUnit(0, texUnit);  


    SE_Surface* surface = new SE_Surface;
    surface->setGeometryData(geometryData);
    surface->setFacets(mFacet, 2);
    surface->setTexture(texture);//must

    SE_Mesh* mesh = new SE_Mesh(1, 1);
    mesh->setGeometryData(geometryData);
    mesh->setSurface(0, surface);

	setMesh(mesh,OWN);

	SE_ProgramDataID ProgramDataID = "default_shader" ;
    SE_RendererID RendererID = "default_renderer";

	getMesh()->getSurface(0)->setProgramDataID(ProgramDataID);
    getMesh()->getSurface(0)->setRendererID(RendererID);


}	
void ViewData::setImage(const char* imagename){
#if 0
	if(strlen(imagename)>0)
	{
		SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
		SE_ImageDataID imageDataid(imagename);
	    SE_ImageData* imgd = resourceManager->getImageData(imageDataid);
	    if (!imgd) {  
	        #ifdef WIN32 
			imgd = SE_ImageCodec::load(imagename);
	        #endif
	        #ifdef ANDROID
	        imgd = SE_ImageCodec::loadAsset(imagename);
	        #endif
	        if (imgd) {
	            resourceManager->setImageData(imageDataid, imgd);
	           
	        }
	    }
		getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageData(0, imgd);
	}
#endif
}

void ViewData::setImage(SE_ImageData*  imgd){

    if (imgd) {
		SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();

        resourceManager->insertPathImageData(getName(), imgd); 
        resourceManager->setIdPath(getName(),getName());		    
    }
	getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageData(0, imgd);	
}
void ViewData::setSize(float width, float height) {
	if (mWidth != width || mHeight != height) {
		mWidth = width;
		mHeight = height;
		
		SE_Vector3f* mVertex = new SE_Vector3f[4];
		// Left-top
	    // Positions
		mVertex[0].x = mX - mWidth/2;
		mVertex[0].y = mY + mHeight/2;
		mVertex[0].z = 0;

	    // Right-top
	    // Positions
		mVertex[1].x = mX + mWidth/2;
		mVertex[1].y = mY + mHeight/2;
		mVertex[1].z = 0;

	    // Left-bottom
	    // Positions
		mVertex[2].x = mX - mWidth/2;
		mVertex[2].y = mY - mHeight/2;
		mVertex[2].z = 0;

	    // Right-bottom
	    // Positions
		mVertex[3].x = mX + mWidth/2;
		mVertex[3].y = mY - mHeight/2;
		mVertex[3].z = 0;
		
    	getMesh()->getSurface(0)->getGeometryData()->setVertexArray(mVertex, 4);

	
	}
}

void ViewData::UpdateData(){


}

