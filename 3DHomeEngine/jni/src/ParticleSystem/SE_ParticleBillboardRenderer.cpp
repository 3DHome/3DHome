#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleBillboardRenderer.h"
#include "SE_Mesh.h"
#include "SE_Geometry.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"
#include "SE_ImageCodec.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"

#include "SE_MemLeakDetector.h"
#include "SE_Log.h"

String rendererTypeName = "billboard";

BillboardParticleRenderer::BillboardParticleRenderer()
{
    // Create billboard set
    int wrapS = 1;
    int wrapT = 1;
   // int* facet = new int;
    SE_GeometryData* geomData = new SE_GeometryData;
    SE_TextureCoordData* texCoordData = new SE_TextureCoordData;
    SE_TextureUnit* texUnit = new SE_TextureUnit();	
    //texUnit->setImageDataNum(1);
    texUnit->setTextureCoordData(texCoordData); 
    texUnit->setWrapS(wrapS);
    texUnit->setWrapT(wrapT);  
		    
    SE_Texture* texture = new SE_Texture;
    texture->setTextureUnit(0, texUnit); 
    texture->setTexUnitNum(1);

    SE_Mesh* mesh = new SE_Mesh(1, 1);
    mesh->setGeometryData(geomData);
    mesh->setTexture(0, texture);

    SE_Surface* surface = new SE_Surface;
    surface->setGeometryData(geomData);
    //surface->setFacets(facet,0);
    surface->setTexture(mesh->getTexture(0));//must
    mesh->setSurface(0, surface);

    mBillboardSet = new BillboardSet("", 0, true, mesh);

    //SE_ProgramDataID ProgramDataID = "particle_shader";
    //SE_RendererID RendererID = "particle_renderer"; 
    SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getMainScene()->getRoot();
    SE_SpatialID spatialID = SE_ID::createSpatialID();
       
    //mBillboardSet->getMesh()->getSurface(0)->setProgramDataID(ProgramDataID);
    //mBillboardSet->getMesh()->getSurface(0)->setRendererID(RendererID);

    SE_Geometry*  geometry = new SE_Geometry(spatialID, root); 
    geometry->attachSimObject(mBillboardSet);
    SE_SimObjectID id = SE_ID::createSimObjectID();
    mBillboardSet->setID(id);
    /*if(!SE_Application::getInstance()->getSimObjectManager()->set(id, mBillboardSet))
    {
        LOGI("mbillbordset has exist!!!\n");
        delete mBillboardSet;
    }*/
    geometry->updateWorldTransform();
    geometry->updateBoundingVolume();
    root->addChild(geometry);
        
    SE_DepthTestState* rds = new SE_DepthTestState();
    rds->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
    geometry->setRenderState(DEPTHTESTSTATE, rds);
    geometry->updateRenderState();

    SE_BlendState *rs = new SE_BlendState();
    rs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
    geometry->setRenderState(BLENDSTATE,rs);
    SE_Layer* l = geometry->getWorldLayer();
    l->setLayer(2);    
    
    geometry->updateRenderState();
    geometry->updateWorldLayer(); 
    geometry->setNeedParticle(true);
    geometry->setSpatialEffectAttribute(SE_SpatialAttribute::BLENDABLE,true);
		   
		   
    // World-relative axes
    mBillboardSet->setBillboardsInWorldSpace(true);
}

BillboardParticleRenderer::~BillboardParticleRenderer()
{
    //delete  mBillboardSet;
    //SE_SpatialManager* spManager = SE_Application::getInstance()->getSpatialManager();
    //SE_Spatial* particle = spManager->findByName(this->getBillboardSet()->getSpatial()->getSpatialName());

    SE_Spatial* particle = SE_Application::getInstance()->getSceneManager()->findSpatialByName(this->getBillboardSet()->getSpatial()->getSpatialName());
    if(particle) 
    {        
        particle->getScene()->removeSpatial(particle);
        delete particle;
    }
}

const String& BillboardParticleRenderer::getType(void) const
{
    return rendererTypeName;
}

void BillboardParticleRenderer::_updateRenderQueue(std::list<Particle*>& currentParticles, bool cullIndividually)
{
    mBillboardSet->setCullIndividually(cullIndividually);

    // Update billboard set geometry
    mBillboardSet->beginBillboards(currentParticles.size());
    Billboard bb;
    for (std::list<Particle*>::iterator i = currentParticles.begin();
            i != currentParticles.end(); ++i)
    {
        Particle* p = *i;
        bb.mPosition = p->position;
	if (mBillboardSet->getBillboardType() == BBT_ORIENTED_SELF ||
		mBillboardSet->getBillboardType() == BBT_PERPENDICULAR_SELF)
	{
	    // Normalise direction vector
	    bb.mDirection = p->direction;
	    bb.mDirection.normalise();
	}
        bb.mColour = p->colour;
        bb.mRotation = p->rotation;
        // Assign and compare at the same time
        if ((bb.mOwnDimensions = p->mOwnDimensions) == true)
        {
            bb.mWidth = p->mWidth;
            bb.mHeight = p->mHeight;
        }
        mBillboardSet->injectBillboard(bb);

   }
        
   mBillboardSet->endBillboards();
}

void BillboardParticleRenderer::setBillboardType(BillboardType bbt)
{
    mBillboardSet->setBillboardType(bbt);
}

void BillboardParticleRenderer::setUseAccurateFacing(bool acc)
{
    mBillboardSet->setUseAccurateFacing(acc);
}

bool BillboardParticleRenderer::getUseAccurateFacing(void) const
{
    return mBillboardSet->getUseAccurateFacing();
}

BillboardType BillboardParticleRenderer::getBillboardType(void) const
{
    return mBillboardSet->getBillboardType();
}

void BillboardParticleRenderer::setBillboardRotationType(BillboardRotationType rotationType)
{
    mBillboardSet->setBillboardRotationType(rotationType);
}

BillboardRotationType BillboardParticleRenderer::getBillboardRotationType(void) const
{
    return mBillboardSet->getBillboardRotationType();
}

void BillboardParticleRenderer::setCommonDirection(const Vector3& vec)
{
    mBillboardSet->setCommonDirection(vec);
}

const Vector3& BillboardParticleRenderer::getCommonDirection(void) const
{
    return mBillboardSet->getCommonDirection();
}

void BillboardParticleRenderer::setCommonUpVector(const Vector3& vec)
{
    mBillboardSet->setCommonUpVector(vec);
}

const Vector3& BillboardParticleRenderer::getCommonUpVector(void) const
{
    return mBillboardSet->getCommonUpVector();
}

void BillboardParticleRenderer::_notifyParticleRotated(void)
{
    mBillboardSet->_notifyBillboardRotated();
}

void BillboardParticleRenderer::_notifyDefaultDimensions(Real width, Real height)
{
    mBillboardSet->setDefaultDimensions(width, height);
}

void BillboardParticleRenderer::_notifyParticleResized(void)
{
    mBillboardSet->_notifyBillboardResized();
}

void BillboardParticleRenderer::_notifyParticleQuota(size_t quota)
{
    mBillboardSet->setPoolSize(quota);
}

void BillboardParticleRenderer::setKeepParticlesInLocalSpace(bool keepLocal)
{
    mBillboardSet->setBillboardsInWorldSpace(!keepLocal);
}

void BillboardParticleRenderer::setPointRenderingEnabled(bool enabled)
{
    mBillboardSet->setPointRenderingEnabled(enabled);
}

bool BillboardParticleRenderer::isPointRenderingEnabled(void) const
{
    return mBillboardSet->isPointRenderingEnabled();
}

const String& BillboardParticleRendererFactory::getType() const
{
    return rendererTypeName;
}

ParticleSystemRenderer* BillboardParticleRendererFactory::createInstance( 
        const String& name )
{
    return new BillboardParticleRenderer();
}

void BillboardParticleRendererFactory::destroyInstance( 
        ParticleSystemRenderer* inst)
{
    delete inst;
}  

void BillboardParticleRenderer::setBillboardSetName(const String& billboardSetName)
{
    mBillboardSet->setName(billboardSetName.c_str());
    mBillboardSet->getSpatial()->setSpatialName(billboardSetName.c_str());
    //SE_Application::getInstance()->getSpatialManager()->set(billboardSetName.c_str(),mBillboardSet->getSpatial());

    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_TextureCoordDataID textureCoordDataID = SE_ID::createTextureCoordDataID(billboardSetName.c_str());
    resourceManager->setTextureCoordData(textureCoordDataID, mBillboardSet->getMesh()->getSurface(0)->getTexture()
        ->getTextureUnit(0)->getTextureCoordData());
    mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setTextureCoordDataID(textureCoordDataID);

    SE_GeometryDataID  geometryDataID = SE_ID::createGeometryDataID(billboardSetName.c_str());
    resourceManager->setGeometryData(geometryDataID, mBillboardSet->getMesh()->getGeometryData());
    mBillboardSet->getMesh()->setGeometryDataID(geometryDataID);
}

void BillboardParticleRenderer::setTextureImage(const char* imagePath)
{   
    //SE_ImageDataID imageDataId = SE_ID::createImageDataID(imagePath);
    SE_ImageDataID imageDataId = imagePath;
    SE_ImageDataID* imageIDArray = new SE_ImageDataID[1];
    imageIDArray[0] = imageDataId;
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager(); 
    SE_ImageData* imgd = resourceManager->getImageData(imageDataId);  
    if (!imgd) 
    {   

        #ifdef WIN32 
        imgd = SE_ImageCodec::load(imagePath);
        if (imgd) 
        {            
            resourceManager->insertPathImageData(imagePath, imgd); 
            resourceManager->setIdPath(imageDataId.getStr(),imagePath);
        }
        #endif
        
    }
    //mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageDataNum(1);
    mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageDataID(imageIDArray,1);
    //mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageData(0,imgd);
}

void BillboardParticleRenderer::setTranslate(const SE_Vector3f& translate) {
	mBillboardSet->getSpatial()->setLocalTranslate(translate);
}
void BillboardParticleRenderer::setRotate(const SE_Quat& rotate) {
	mBillboardSet->getSpatial()->setLocalRotate(rotate);
}
void BillboardParticleRenderer::setScale(const SE_Vector3f& scale){
	mBillboardSet->getSpatial()->setLocalScale(scale);
}
