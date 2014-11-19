#include "SE_DynamicLibType.h"
#include "SE_View.h"
#include "SE_RootView.h"
#include "SE_ViewGroup.h"

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
#include "SE_ViewData.h"

View::View(SE_SpatialID id, SE_Spatial* parent):SE_Geometry( id, parent)
{
	mX = 0.0f;
	mY = 0.0f;
	mWidth = 0;
	mHeight = 0;

	mHidden = false;
}


void View::SetImage(const char* imagename){
	/*if(strlen(imagename)>0)
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
		getCurrentAttachedSimObj()->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageData(0, imgd);
	}*/
	mViewData->setImage(imagename);
}

void View::SetImage(SE_ImageData*  imgd)
{
	mViewData->setImage(imgd);
}


void View::setPosition(float x, float y) {
	mX = x;
	mY = y;
	SE_Matrix3f identity3;
	identity3.identity();

	SE_Matrix4f transform;
	transform.identity();

	transform.set(identity3,SE_Vector3f(mX,mY,0));

	setPrevMatrix(transform);

	//update
	updateWorldTransform();
	updateBoundingVolume();
}
void View::setSize(float width, float height) {
	if (mWidth != width || mHeight != height) {
		mWidth = width;
		mHeight = height;
		setSize(width, height);
		updateWorldTransform();
	}
}

void View::UpdateData(){

}
