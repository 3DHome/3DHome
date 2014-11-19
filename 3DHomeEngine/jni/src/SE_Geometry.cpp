#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Geometry.h"
#include "SE_SimObject.h"
#include "SE_Buffer.h"
#include "SE_Camera.h"
#include "SE_Common.h"
#include "SE_RenderUnit.h"
#include "SE_RenderManager.h"
#include "SE_BoundingVolume.h"

#include "SE_Application.h"
#include "SE_RenderTargetManager.h"
#include <list>
#include "SE_MeshSimObject.h"
#include "SE_SceneManager.h"
#include "SE_Log.h"
#include "SE_Mesh.h"
#include "SE_DataValueDefine.h"
#include "SE_GeometryData.h"
#include "SE_MemLeakDetector.h"
#include "SE_ObjectManager.h"

#include "SE_RenderUnitManager.h"
IMPLEMENT_OBJECT(SE_Geometry)
SE_Geometry::_Impl::~_Impl()
    {
        std::list<SE_SimObject*>::iterator it;
        for(it = attachObject.begin() ; it != attachObject.end() ; it++)
        {            
            SE_SimObject *obj = *it;

        //obj manager not use
        //SE_Application::getInstance()->getSimObjectManager()->remove((*it)->getID());
            //remove will delete object;
        
        
        delete obj;

        }
    attachObject.clear();
    }
///////////////////////////////////////////////
SE_Geometry::SE_Geometry(SE_Spatial* parent) : SE_Spatial(parent)
{
    mImpl = new SE_Geometry::_Impl;
    mirrorSpatial = NULL;
    mGenerateMipMap = true;
    mSimpleObject = NULL;
    mMirrorPlane = SE_XY;

    mIsGeometry = true;
    this->setNeedBlendSortOnZ(true);
}
SE_Geometry::SE_Geometry(SE_SpatialID id, SE_Spatial* parent) : SE_Spatial(id, parent)
{
    mImpl = new SE_Geometry::_Impl;
    mirrorSpatial = NULL;
    mGenerateMipMap = true;
    mSimpleObject = NULL;
    mMirrorPlane = SE_XY;
    mIsGeometry = true;
    this->setNeedBlendSortOnZ(true);
}
SE_Geometry::~SE_Geometry()
{
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORGENERATOR))
    {
        SE_Application::getInstance()->getRenderManager()->removeMirrorGenerator(this);
    }

    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORRENDER))
    {
        SE_Application::getInstance()->getRenderManager()->mirrorRenderChange(this);
    }
    delete mImpl;
}
void SE_Geometry::attachSimObject(SE_SimObject* go)
{
    mImpl->attachObject.push_back(go);
    go->setSpatial(this);
    //save current SimObject
    setCurrentAttachedSimObj(go);

}
void SE_Geometry::detachSimObject(SE_SimObject* go)
{
    mImpl->attachObject.remove(go);
    go->setSpatial(NULL);
    //clear current SimObject
    setCurrentAttachedSimObj(NULL);
}

void SE_Geometry::write(SE_BufferOutput& output)
{
    output.writeString("SE_Geometry");
    output.writeInt(0);//geometry has no child

    SE_Spatial::write(output);

    output.writeInt(mImpl->attachObject.size());
    SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        SE_SimObject* obj = *it;
        obj->write(output);
    }
    
}
void SE_Geometry::writeEffect(SE_BufferOutput& output)
{
    SE_Spatial::writeEffect(output);
        //save this spatial light status
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::LIGHTING))
    {
        output.writeInt(1);//this spatial need lighting

        //write light para
        std::vector<SE_Light*> dirlight;
        std::vector<SE_Light*> pointlight;
        std::vector<SE_Light*> spotlight;

        std::vector < std::string > namelist;
        getLightsNameList(namelist);
        for (int i = 0; i < namelist.size(); i++) {
            std::string name = namelist[i];
            SE_Light* l = getScene()->getLight(name.c_str());
            if (l) {
                if (l->getLightType() == SE_Light::SE_DIRECTION_LIGHT) {
                    dirlight.push_back(l);
                } else if (l->getLightType() == SE_Light::SE_POINT_LIGHT) {
                    pointlight.push_back(l);
                } else if (l->getLightType() == SE_Light::SE_SPOT_LIGHT) {
                    spotlight.push_back(l);
                }

            }
        }


        //write dir light
        output.writeInt(dirlight.size()); //write dirlight count
        for(int i = 0; i < dirlight.size(); ++i)
        {
            //write dir light info
            SE_Light* light = dirlight[i];
            SE_Vector3f dir = light->getLightDir();
            std::string name = light->getLightName();
            SE_Vector3f color = light->getLightColor();
            float dirstrength = light->getDirLightStrength();
            output.writeVector3f(dir);
            output.writeString(name.c_str());
            output.writeVector3f(color);
            output.writeFloat(dirstrength);


        }

        //write point light
        output.writeInt(pointlight.size()); //write point light count
        for(int i = 0; i < pointlight.size(); ++i)
        {
            //write dir light info
            SE_Light* light = pointlight[i];
            SE_Vector3f pos = light->getLightPos();
            float constAttr = light->getConstantAttenuation();
            float linearAttr = light->getLinearAttenuation();
            float quatAttr = light->getQuadraticAttenuation();
            std::string name = light->getLightName();
            SE_Vector3f color = light->getLightColor();

            output.writeVector3f(pos);
            output.writeFloat(constAttr);
            output.writeFloat(linearAttr);
            output.writeFloat(quatAttr);
            output.writeString(name.c_str());
            output.writeVector3f(color);
        }

        //write spot light
        output.writeInt(spotlight.size()); //write spot light count
        for(int i = 0; i < spotlight.size(); ++i)
        {
            //write dir light info
            SE_Light* light = spotlight[i];

            SE_Vector3f pos = light->getLightPos();
            SE_Vector3f spotdir = light->getLightDir();

            float constAttr = light->getConstantAttenuation();
            float linearAttr = light->getLinearAttenuation();
            float quatAttr = light->getQuadraticAttenuation();
            
            float cutoff = light->getSpotLightCutOff();
            float soptexp = light->getSpotLightExp();
            std::string name = light->getLightName();
            SE_Vector3f color = light->getLightColor();

            output.writeVector3f(pos);
            output.writeVector3f(spotdir);

            output.writeFloat(constAttr);
            output.writeFloat(linearAttr);
            output.writeFloat(quatAttr);

            output.writeFloat(cutoff);
            output.writeFloat(soptexp);
            output.writeString(name.c_str());
            output.writeVector3f(color);
        }
    }
    else
    {
        output.writeInt(0);//do not lighting
    }

    //write shadow generator status
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWGENERATOR))
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow generator!\n",getSpatialName());
        output.writeInt(1);
        SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);
        SE_Vector3f loc = s->getLocation();
        SE_Vector3f target = s->getTarget();
        SE_Vector3f up = s->getUp();
        float neara = s->getFrustum()->getNear();
        float fara = s->getFrustum()->getFar();
        float fov = s->getFovDegree();
        output.writeVector3f(loc);
        output.writeVector3f(target);
        output.writeVector3f(up);
        output.writeFloat(neara);
        output.writeFloat(fara);
        output.writeFloat(fov);

    }
    else
    {
        //do not generate shadow
        output.writeInt(0);
    }

    //write shadow render status
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWRENDER))
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow render!\n",getSpatialName());
        output.writeInt(1);
    }
    else
    {
        //do not render shadow
        output.writeInt(0);
    }

    //write mirror generator
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORGENERATOR))
    {
        output.writeInt(1);
        output.writeString(mMirrorInfo.c_str());
        output.writeInt((int)mMirrorPlane);
    }
    else
    {
        output.writeInt(0);
    }

    //write mirror render
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORRENDER))
    {
        output.writeInt(1);
        float alpha = this->getAlpha();
        output.writeFloat(alpha);
    }
    else
    {
    output.writeInt(0);
    }

    //write mipmap info
    if(mGenerateMipMap)
    {
        output.writeInt(1);
    }
    else
    {
        output.writeInt(0);
    }
    float alpha = this->getAlpha();
    output.writeFloat(alpha);
    SE_Vector4f mspatialData = this->getEffectData();
    output.writeVector4f(mspatialData);

    unsigned int effectstate = this->getEffectState();
    output.writeInt(effectstate);

}
void SE_Geometry::read(SE_BufferInput& input)
{

    SE_Spatial::read(input);

#if 0
    //get lighting status
    int need = input.readInt();
    if(need == 1)
    {
        this->setNeedLighting(true);
        int dirlightcount = input.readInt();//dirlight count
        for(int i = 0; i < dirlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f dir = input.readVector3f();//read dir
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();
            float dirlightstrength = input.readFloat();

            l->setLightDir(dir);
            l->setLightName(name.c_str());
            l->setLightType(SE_Light::SE_DIRECTION_LIGHT);
            l->setLightColor(color);
            l->setDirLightStrength(dirlightstrength);
            mLightsForInflate.push_back(l);
            mLightsNameList.push_back(name);

        }

        int pointlightcount = input.readInt();//pointlight count
        for(int i = 0; i < pointlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f pos = input.readVector3f();//read pos
            float constAttr = input.readFloat();
            float linearAttr = input.readFloat();
            float quatAttr = input.readFloat();
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();

            l->setLightPos(pos);
            l->setConstantAttenuation(constAttr);
            l->setLinearAttenuation(linearAttr);
            l->setQuadraticAttenuation(quatAttr);
            l->setLightName(name.c_str());
            l->setLightType(SE_Light::SE_POINT_LIGHT);
            l->setLightColor(color);
            mLightsForInflate.push_back(l);
            mLightsNameList.push_back(name);
        }

        int spotlightcount = input.readInt();//spotlight count
        for(int i = 0; i < spotlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f pos = input.readVector3f();//read pos
            SE_Vector3f spotdir = input.readVector3f();//read spotdir

            float constAttr = input.readFloat();
            float linearAttr = input.readFloat();
            float quatAttr = input.readFloat();

            float cutoff = input.readFloat();
            float spotexp = input.readFloat();
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();

            l->setLightPos(pos);
            l->setLightDir(spotdir);

            l->setConstantAttenuation(constAttr);
            l->setLinearAttenuation(linearAttr);
            l->setQuadraticAttenuation(quatAttr);
            l->setSpotLightCutOff(cutoff);
            l->setSpotLightExp(spotexp);
            l->setLightName(name.c_str());
            l->setLightColor(color);

            l->setLightType(SE_Light::SE_SPOT_LIGHT);
            mLightsForInflate.push_back(l);
            mLightsNameList.push_back(name);
        }        
    }
    else
    {
        this->setNeedLighting(false);
    }

    //read shadow generator
    int shadowGenerator = input.readInt();
    if(shadowGenerator == 1)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow generator!\n",getSpatialName());
        this->setNeedGenerateShadow(true);
        SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);
        SE_Vector3f loc = input.readVector3f();
        SE_Vector3f target = input.readVector3f();
        SE_Vector3f up = input.readVector3f();
        float neara = input.readFloat();
        float fara = input.readFloat();
        float fov = input.readFloat();
        s->create(loc, target, up, fov,1.0, neara, fara);
    }
    else
    {
        this->setNeedGenerateShadow(false);
    }

    //read shadow render
    int shadowRender = input.readInt();
    if(shadowRender == 1)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow render!\n",getSpatialName());
        this->setNeedRenderShadow(true);
    }
    else
    {
        this->setNeedRenderShadow(false);
    }

    //read mirror generator
    int mirrorGenerator = input.readInt();
    if(mirrorGenerator == 1)
    {
        this->setNeedGenerateMirror(true);
        mMirrorInfo = input.readString();
        mMirrorPlane = (SE_MirrorPlane)input.readInt();

    }
    else
    {
        this->setNeedGenerateMirror(false);
    }

    //read mirror 
    int mirrorRender = input.readInt();
    if(mirrorRender == 1)
    {
        this->setNeedRenderMirror(true);
        float alpha = input.readFloat();
        this->setAlpha(alpha);
    }
    else
    {
        this->setNeedRenderMirror(false);
    }

    int needGenerateMipMap = input.readInt();
    if(needGenerateMipMap == 0)
    {
        mGenerateMipMap = false;
    }
    else
    {
        mGenerateMipMap = true;
    }
#endif

    int attachObjNum = input.readInt();
    
    for(int i = 0 ; i < attachObjNum ; i++)
    {
        std::string str = input.readString();
        SE_SimObject* obj = (SE_SimObject*)SE_ObjectFactory::create(str.c_str());
        obj->read(input);
        attachSimObject(obj);
    }
    // hard code for fix bug, can remove it
    char* p;
    if ((p = strstr(getSpatialName(), "touying")) != 0) {
        setSpatialStateAttribute(SE_SpatialAttribute::TOUCHABLE, false);
    }
    
}
void SE_Geometry::readEffect(SE_BufferInput& input)
{
    SE_Spatial::readEffect(input);
    //get lighting status
    int need = input.readInt();
    if(need == 1)
    {
        this->setNeedLighting(true);
        int dirlightcount = input.readInt();//dirlight count
        for(int i = 0; i < dirlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f dir = input.readVector3f();//read dir
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();
            float dirlightstrength = input.readFloat();

            l->setLightDir(dir);
            l->setLightName(name.c_str());
            l->setLightType(SE_Light::SE_DIRECTION_LIGHT);
            l->setLightColor(color);
            l->setDirLightStrength(dirlightstrength);
            mLightsForInflate.push_back(l);
            this->addLightNameToList(name.c_str());

        }

        int pointlightcount = input.readInt();//pointlight count
        for(int i = 0; i < pointlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f pos = input.readVector3f();//read pos
            float constAttr = input.readFloat();
            float linearAttr = input.readFloat();
            float quatAttr = input.readFloat();
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();

            l->setLightPos(pos);
            l->setConstantAttenuation(constAttr);
            l->setLinearAttenuation(linearAttr);
            l->setQuadraticAttenuation(quatAttr);
            l->setLightName(name.c_str());
            l->setLightType(SE_Light::SE_POINT_LIGHT);
            l->setLightColor(color);
            mLightsForInflate.push_back(l);
            this->addLightNameToList(name.c_str());
        }

        int spotlightcount = input.readInt();//spotlight count
        for(int i = 0; i < spotlightcount; ++i)
        {
            SE_Light* l = new SE_Light();
            SE_Vector3f pos = input.readVector3f();//read pos
            SE_Vector3f spotdir = input.readVector3f();//read spotdir

            float constAttr = input.readFloat();
            float linearAttr = input.readFloat();
            float quatAttr = input.readFloat();

            float cutoff = input.readFloat();
            float spotexp = input.readFloat();
            std::string name = input.readString();
            SE_Vector3f color = input.readVector3f();

            l->setLightPos(pos);
            l->setLightDir(spotdir);

            l->setConstantAttenuation(constAttr);
            l->setLinearAttenuation(linearAttr);
            l->setQuadraticAttenuation(quatAttr);
            l->setSpotLightCutOff(cutoff);
            l->setSpotLightExp(spotexp);
            l->setLightName(name.c_str());
            l->setLightColor(color);

            l->setLightType(SE_Light::SE_SPOT_LIGHT);
            mLightsForInflate.push_back(l);
            this->addLightNameToList(name.c_str());
        }        
    }
    else
    {
        this->setNeedLighting(false);
    }

    //read shadow generator
    int shadowGenerator = input.readInt();
    if(shadowGenerator == 1)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow generator!\n",getSpatialName());
        this->setNeedGenerateShadow(true);
        SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);
        SE_Vector3f loc = input.readVector3f();
        SE_Vector3f target = input.readVector3f();
        SE_Vector3f up = input.readVector3f();
        float neara = input.readFloat();
        float fara = input.readFloat();
        float fov = input.readFloat();
        s->create(loc, target, up, fov,1.0, neara, fara);
    }
    else
    {
        this->setNeedGenerateShadow(false);
    }

    //read shadow render
    int shadowRender = input.readInt();
    if(shadowRender == 1)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("spatial[%s] is shadow render!\n",getSpatialName());
        this->setNeedRenderShadow(true);
    }
    else
    {
        this->setNeedRenderShadow(false);
    }

    //read mirror generator
    int mirrorGenerator = input.readInt();
    if(mirrorGenerator == 1)
    {
        this->setNeedGenerateMirror(true);
        mMirrorInfo = input.readString();
        mMirrorPlane = (SE_MirrorPlane)input.readInt();

    }
    else
    {
        this->setNeedGenerateMirror(false);
    }

    //read mirror 
    int mirrorRender = input.readInt();
    if(mirrorRender == 1)
    {
        this->setNeedRenderMirror(true);
        float alpha = input.readFloat();
        this->setAlpha(alpha);
    }
    else
    {
        this->setNeedRenderMirror(false);
    }

    int needGenerateMipMap = input.readInt();
    if(needGenerateMipMap == 0)
    {
        mGenerateMipMap = false;
    }
    else
    {
        mGenerateMipMap = true;
    }

    float alpha = input.readFloat();
    this->setAlpha(alpha);

    SE_Vector4f effectData = input.readVector4f();   
    this->setEffectData(effectData);
    
#if 1
    unsigned int effectstate = input.readInt();
    this->setEffectState(effectstate);
#endif
    
}
void SE_Geometry::updateRenderState()
{
    SE_Spatial::updateRenderState();
/*
    SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        SE_SimObject* obj = *it;
        for(int i = 0 ; i < RENDERSTATE_NUM ; i++)
        {
			obj->setRenderState((RENDER_STATE_TYPE)i, getRenderState((RENDER_STATE_TYPE)i));	
        }
    }
*/
}
void SE_Geometry::updateWorldTransform()
{
    //if(!mTransformHasChanged)
    //{
    //    return;
    //}
    SE_Spatial::updateWorldTransform();

    //world geometry not save by object

    /*SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        (*it)->doTransform(getWorldTransform());
    }*/
}
void SE_Geometry::updateBoundingVolume()
{ 
this->forceUpdateBoundingVolume();
    /*if((!this->isChangedTransform()) && mWorldBoundingVolume && (!this->getParent()->isChangedTransform()))
    {
        return;
    }*/
//LOGI("Geometry[%s] update bounding volume!!\n\n",this->getSpatialName());

#if 0
    if(mWorldBoundingVolume)
    {
        delete mWorldBoundingVolume;
        mWorldBoundingVolume = NULL;
    }
    switch(getBVType())
    {
    case AABB:
        mWorldBoundingVolume = new SE_AABBBV;
        break;
    case OBB:
        mWorldBoundingVolume = new SE_OBBBV;
        break;
    case SPHERE:
        mWorldBoundingVolume = new SE_SphereBV;
        break;
    }
    if(mWorldBoundingVolume)
    {
        SE_Geometry::_Impl::SimObjectList::iterator it;
        for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
        {
            //(*it)->doTransform(getWorldTransform());
            SE_Vector3f* points = (*it)->getVertexArray();
            if(!points)
            {
                continue;
            }
            int num = (*it)->getVertexNum();
            mWorldBoundingVolume->createFromPoints(points, num);
            (*it)->setCenter(mWorldBoundingVolume->getCenter());
        }
    }
    
    if (this->getScene()->getSceneManagerType() == SE_Scene::OCTREESCENEMANAGER)
    {
        this->getScene()->getOctreeSceneManager()->updateBounds(this->getSpatialName());
    }

    //mBoundingHasChanged = false;
#endif
}
int SE_Geometry::travel(SE_SpatialTravel* spatialTravel, bool travelAlways)
{
    int r = spatialTravel->visit(this);
    if(r)
        return r;
    SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        SE_SimObject* so = *it;
        int ret = spatialTravel->visit(so);
        if(ret && !travelAlways)
            break;
    }
    if(travelAlways)
        return 0;
    else
        return r;
}
SPATIAL_TYPE SE_Geometry::getSpatialType()
{
    return GEOMETRY;
}
static SE_RenderUnit* createSelectedFrame(SE_Geometry* spatial)
{
    SE_RenderUnit* ru = NULL;
    if(spatial)
    {
        SE_BoundingVolume* bv = spatial->getWorldBoundingVolume();
        if(bv)
        {
            SE_AABBBV* aabbBV = NULL;
            SE_SphereBV* sphereBV = NULL;
            SE_OBBBV* obbBV = NULL; 
            switch(bv->getType())
            {
            case AABB:
                {
                    aabbBV = (SE_AABBBV*)bv;
                    SE_AABB aabb = aabbBV->getGeometry();
                    SE_Segment edge[12];
                    aabb.getEdge(edge);
                    std::string renderUnitName = std::string(spatial->getSpatialName()) + "lineedge";
#ifdef USE_RUMANAGER
                    SE_RenderUnitManager* srum = SE_Application::getInstance()->getRenderUnitManager();
                    ru = srum->find(renderUnitName.c_str());
                    if(!ru)
                    {
                    ru = new SE_LineSegRenderUnit(edge, 12, SE_Vector3f(0, 1, 0));
                        srum->insert(renderUnitName.c_str(),ru);
                    }
#else
                    ru = new SE_LineSegRenderUnit(edge, 12, SE_Vector3f(0, 1, 0));
#endif
                    
                }
                break;
            case SPHERE:
                {
                    sphereBV = (SE_SphereBV*)bv;
                }
                break;
            case OBB:
                {
                    obbBV = (SE_OBBBV*)bv;
                }
                break;
            }
        }
    }
    return ru;
}
static float distanceToCameraLocation(SE_Camera* camera, const SE_Vector3f& center)
{
    SE_Vector3f location = camera->getLocation();
    SE_Vector3f zAxis = camera->getAxisZ();
    SE_Vector3f intersectP;
    float dist;
    if(!zAxis.isZero())
    {
        SE_GeometryIntersect::pointDistanceToLine(center, location, zAxis, dist, intersectP);
    }
    return SE_GeometryIntersect::pointDistance(intersectP, location);
}
void SE_Geometry::renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType)
{  
    if(!isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
    {        
        return;
    }

        SE_BoundingVolume* bv = getWorldBoundingVolume();
        if(bv && cullType == SE_PART_CULL)
        {
            int culled = camera->cullBV(*bv);
            if(culled == SE_FULL_CULL)
            {            
            //set cull status for optimize ray detect,now ray detect would not effect this spatial
            setSpatialRuntimeAttribute(SE_SpatialAttribute::CAMERAFULLCULL,true);
                return;
            }
        }
    //set cull status for optimize ray detect
    setSpatialRuntimeAttribute(SE_SpatialAttribute::CAMERAFULLCULL,false);
    SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        SE_SimObject* so = *it;

        if(!so->isNeedRender())
        {
            //Do not render this object,this is a data object,like simple-model data object
            continue;
        }

        SE_SimObject::RenderUnitVector renderUnitVector = so->createRenderUnit();
        SE_SimObject::RenderUnitVector::iterator itRU;
        for(itRU = renderUnitVector.begin() ; itRU!= renderUnitVector.end(); itRU++)
        {
            if(*itRU)
            {
                float f = distanceToCameraLocation(camera, so->getCenter());
                (*itRU)->setDistanceToCamera(f);

                //transparency object should be draw after normal object.
                    if((*itRU)->isNeedBlurVShadow())
                    {
                        //fbo shadow blurV first
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ1); 
                    }
                    else if((*itRU)->isNeedBlurHShadow())
                    {
                        //blurV first , then blurH
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ2); 
                    }
                    else if((*itRU)->isNeedGenerateShadow())
                    {
                        //shadow,fbo first
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ0); 
                    }
                    else if((*itRU)->isNeedDofGen())
                    {
                        //dof,fbo first
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ0); 
                    }
                else if((*itRU)->isNeedColorEffect())
                    {
                            renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ6); 
                        }
                        else if((*itRU)->isEnableBlend())
                        {
                    //blend unit last
                    if(isNeedBlendSortOnX())
                    {
                    renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ8); 
                }
                    else if(isNeedBlendSortOnY())
                    {
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ9); 
                    }
                    else
                    {
                        renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ10); 
                    }
                }
                else
                {
                    //normal render unit in rq7
				    renderManager->addRenderUnit(*itRU, SE_RenderManager::RQ7);
               }
        }
    }
    }
#ifndef ANDROID
    if(isSpatialHasRuntimeAttribute(SE_SpatialAttribute::SELECTED))
    {
        SE_RenderUnit* ru = createSelectedFrame(this);
        if(ru != NULL)
            renderManager->addRenderUnit(ru, SE_RenderManager::RQ12);
        else
        {
            SE_Geometry::_Impl::SimObjectList::iterator it;
            for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
            {
                SE_SimObject* so = *it;
                SE_RenderUnit* ru = so->createWireRenderUnit();
                renderManager->addRenderUnit(ru, SE_RenderManager::RQ12);
            }
        }
    }
#endif
}

SE_Spatial *SE_Geometry::clone(SE_SimObject *srcobj, int index)
{
#if 0
    SE_Geometry * dest = new SE_Geometry();

    SE_SimObject* destobj = srcobj->clone(index);
    
    std::string srcobjname = srcobj->getName();

    //attach obj
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();

    SE_SimObjectID id = SE_ID::createSimObjectID();
    destobj->setID(id);
    simObjectManager->set(id, destobj);

    

    //set spatial property
    SE_Spatial *srcSpatial = srcobj->getSpatial();

    SE_SpatialID destSpatialID = SE_ID::createSpatialID();

    dest->setSpatialID(destSpatialID);

    dest->setBVType(srcSpatial->getBVType());

    SE_Vector3f a = srcSpatial->getLocalTranslate();
    dest->setLocalTranslate(a);

    dest->setLocalRotate(srcSpatial->getLocalRotate());

    dest->setLocalScale(srcSpatial->getLocalScale());

    dest->setPrevMatrix(srcSpatial->getPrevMatrix());

    dest->setPostMatrix(srcSpatial->getPostMatrix());

    dest->setParent(srcSpatial->getParent());


    dest->setLocalLayer(srcSpatial->getLocalLayer());

    //set render state
    dest->setRenderState(DEPTHTESTSTATE ,this->getRenderState(DEPTHTESTSTATE));


    dest->attachSimObject(destobj);
    //SE_Application::getInstance()->getSceneManager()->updateSpatialIDMap();
    SE_Scene* scene = srcSpatial->getScene();
    scene->addSpatial(srcSpatial->getParent(), dest);
    srcSpatial->getParent()->updateWorldTransform();
    srcSpatial->getParent()->updateBoundingVolume();
    srcSpatial->getParent()->updateRenderState();
    srcSpatial->getParent()->updateWorldLayer();
    //return clone object
    return dest;
#endif
    return NULL;
}

SE_Spatial *SE_Geometry::clone(SE_Spatial* parent,  int index,bool createNewMesh,const char* statuslist)
{
    if(!parent)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("No parent,clone fail.!!!!");
        return NULL;
    }

    SE_Geometry * dest = new SE_Geometry();

    SE_SimObject* destobj = getCurrentAttachedSimObj()->clone(index,createNewMesh);

    dest->setSpatialName(this->getSpatialName());
    dest->setCloneIndex(index);
    
    
    //attach obj
    dest->attachSimObject(destobj);    

    //set spatial property

    dest->setBVType(getBVType());

    SE_Vector3f a = getLocalTranslate();
    dest->setLocalTranslate(a);

    dest->setLocalRotate(getLocalRotate());

    dest->setLocalScale(getLocalScale());

    dest->setPrevMatrix(getPrevMatrix());

    dest->setPostMatrix(getPostMatrix());

    dest->setParent(parent);

    SE_Layer* l = this->getWorldLayer();
    SE_Layer* destL = dest->getWorldLayer();
    destL->setLayer(l->getLayer());    

    //set render state
    dest->setRenderState(DEPTHTESTSTATE ,this->getRenderState(DEPTHTESTSTATE));
    

    //SE_Application::getInstance()->getSceneManager()->updateSpatialIDMap();
    SE_Scene* scene = getScene();
    scene->addSpatial(parent, dest);

    parent->updateWorldTransform();
    parent->updateBoundingVolume();
    parent->updateRenderState();
    //parent->updateWorldLayer();

    //copy geometry para
    dest->setHasInflate(true);
    
    dest->setAlpha(this->getAlpha());
    

    unsigned int status = 0;
    if(statuslist)
    {
        status = SE_Util::stringToInt(statuslist);
    }
    dest->setEffectState(status);

    dest->setSpatialState(this->getSpatialState());

    dest->setSpatialEffectData(this->getSpatialEffectData());

    std::vector<std::string> lights;
    this->getLightsNameList(lights);
    for(int i = 0; i < lights.size(); ++i)
    {
        dest->addLightNameToList(lights[i].c_str());
    }
    //return clone object
    return dest;
}
void SE_Geometry::setMirrorInfo(const char* mirrorName,SE_MirrorPlane mirrorPlane)
{
    //mirror info no need set.new render struct will do this automaticly
/*
    mMirrorInfo = mirrorName;
    mMirrorPlane = mirrorPlane;

    if(!isInflate())
    {
        return;
    }

    if(mMirrorInfo == "")
    {
        mirrorSpatial = NULL;
        return;
    }

    SE_Spatial* mirror = SE_Application::getInstance()->getSceneManager()->findSpatialByName(mMirrorInfo.c_str());
    if(!mirror)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("mirror [%s] not found",mMirrorInfo.c_str());
        mirrorSpatial = NULL;
        return;
    }

    mirrorSpatial = mirror;
    */

}
void SE_Geometry::inflate()
{
    if(this->isInflate())
    {
        return;
    }
    //send light to scene
    SE_Scene* scene = this->getScene();
    if(!scene)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Error!! geometry inflate fail,scene not found!!!\n\n");
        return;
    }
    this->setHasInflate(true);
    //add all light into scene
    std::vector<SE_Light*>::iterator it;
    for(it = mLightsForInflate.begin(); it != mLightsForInflate.end();)
    {
        SE_Light* l = *it;
        bool result = scene->addLightToScene(l);//scene will collect all light info
        if(!result)
        {
            //light has been in scene
            it = mLightsForInflate.erase(it);
            delete l;

        }
        else
        {
            it++;
        }
    }
    mLightsForInflate.clear();

}

void SE_Geometry::clearSpatialStatus()
{
    //SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
    SE_Application::getInstance()->getRenderManager()->clearSpatialStatus(this);
}
SE_SimObject* SE_Geometry::getSimpleObject()
{
    if(!mSimpleObject)
    {
        std::list<SE_SimObject*>::iterator it;
        int index = 0;
        for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
        {            
            SE_SimObject *obj = *it;
	        if(index == 1)
            {
                mSimpleObject = obj;
                break;
            }
            index++;
        }
    }

    return mSimpleObject;
}
void SE_Geometry::forceUpdateBoundingVolume()
{
    if(mWorldBoundingVolume)
    {
        delete mWorldBoundingVolume;
        mWorldBoundingVolume = NULL;
    }
    switch(getBVType())
    {
    case AABB:
        mWorldBoundingVolume = new SE_AABBBV;
        break;
    case OBB:
        mWorldBoundingVolume = new SE_OBBBV;
        break;
    case SPHERE:
        mWorldBoundingVolume = new SE_SphereBV;
        break;
    }
    if(mWorldBoundingVolume)
    {
        SE_Geometry::_Impl::SimObjectList::iterator it;
        for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
        {
            //generate world geometry data
            SE_GeometryData data;
            SE_Matrix4f m2w = this->getWorldTransform();
            SE_GeometryData::transform((*it)->getMesh()->getGeometryData(),m2w,&data);


            //(*it)->doTransform(getWorldTransform());
            SE_Vector3f* points = data.getVertexArray();
            if(!points)
            {
                continue;
            }
            int num = data.getVertexNum();
            mWorldBoundingVolume->createFromPoints(points, num);
            (*it)->setCenter(mWorldBoundingVolume->getCenter());
        }
    }
    
    //if this is a shadow creator,tell render to update shadow map
    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWGENERATOR))
    {
        //SE_Application::getInstance()->getSpatialManager()->shadowCreatorTransformed();
        SE_Application::getInstance()->getRenderManager()->shadowCreatorTransformed();
    }

    if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORGENERATOR))
    {
        //SE_Application::getInstance()->getSpatialManager()->mirrorCreatorTransformed();
        SE_Application::getInstance()->getRenderManager()->mirrorCreatorTransformed();
    }

}

void SE_Geometry::autoUpdateBoundingVolume()
{
//just geometry transform,not parent
    if(this->isChangedTransform())
    {
        this->forceUpdateBoundingVolume();
        this->setTransformChanged(false);
        //just clear status,geometry never use this value
        clearChildrenHasTransform();

        //if this is a shadow creator,tell render to update shadow map
        if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWGENERATOR))
        {
            //SE_Application::getInstance()->getSpatialManager()->shadowCreatorTransformed();
            SE_Application::getInstance()->getRenderManager()->shadowCreatorTransformed();
        }

        if(this->isSpatialEffectHasAttribute(SE_SpatialAttribute::MIRRORGENERATOR))
        {
            //SE_Application::getInstance()->getSpatialManager()->mirrorCreatorTransformed();
            SE_Application::getInstance()->getRenderManager()->mirrorCreatorTransformed();
        }
    }
}

bool SE_Geometry::changeImageKey(const char *newKey)
{

#if 0
    SE_MeshSimObject* obj = (SE_MeshSimObject*)(this->getCurrentAttachedSimObj());
    if(!obj)
    {
        return false;
    }

    return obj->changeImageKey(newKey);    
#endif
    return false;
}

void SE_Geometry::renderSpatialToTarget(RENDERTARGET_TYPE type,SE_Camera *camera,SE_RenderManager* renderManager, SE_RenderTarget* rt,SE_CULL_TYPE cullType,SE_Spatial* mirror)
{
#if 1

    SE_Geometry::_Impl::SimObjectList::iterator it;
    for(it = mImpl->attachObject.begin() ; it != mImpl->attachObject.end() ; it++)
    {
        SE_SimObject* so = *it;

        if(!so->isNeedRender())
        {
            //Do not render this object,this is a data object,like simple-model data object
            continue;
        }

        SE_SimObject::RenderUnitVector renderUnitVector = so->createUnitForTarget(type,rt);

        SE_SimObject::RenderUnitVector::iterator itRU;
        for(itRU = renderUnitVector.begin() ; itRU!= renderUnitVector.end(); itRU++)
        {
            if(*itRU)
            {
                float f = distanceToCameraLocation(camera, so->getCenter());
                (*itRU)->setDistanceToCamera(f);

                
                if((*itRU)->isNeedGenerateMirror())
                    {
                    (*itRU)->setMirrorObject(mirror);
                    renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ2); 
                    
                }
                else if((*itRU)->isNeedGenerateShadow())
                {                    
                    renderManager->addRenderUnit(*itRU,SE_RenderManager::RQ3);                    
                    }
                    
                
            }
        }
    }
#endif

}
void SE_Geometry::clearMirror()
{
    mMirrorInfo.clear();
    mirrorSpatial = NULL;
}

void SE_Geometry::inflateFromXML(const char *effectString)
{
    SE_TimeMS currTime = 0;
    if(SE_Application::getInstance()->SEHomeDebug)
    {
        currTime = SE_Time::getCurrentTimeMS();
    }
    std::string effect = effectString;

    if(effect.empty())
    {
        return;
    }    
        
    int pos = effect.find(",");

    //light num
    std::string lightNumString = effect.substr(0,pos);
    int lightNum = SE_Util::stringToInt(lightNumString.c_str());
    effect = effect.substr(pos + 1,effect.size());

    for(int i = 0; i < lightNum; ++i)
    {
        //load num light names
        pos = effect.find(",");

        std::string lightname = effect.substr(0,pos);
        effect = effect.substr(pos + 1,effect.size());

        this->addLightNameToList(lightname.c_str());
    }

    //load status
    pos = effect.find(",");
    std::string statusString = effect.substr(0,pos);
    unsigned int status = SE_Util::stringToInt(statusString.c_str());
    this->setEffectState(status);
    effect = effect.substr(pos + 1,effect.size());

    //load alpha
    pos = effect.find(",");
    std::string alphaString = effect.substr(0,pos);
    float alpha = SE_Util::stringToFloat(alphaString.c_str());
    this->setAlpha(alpha);
    effect = effect.substr(pos + 1,effect.size());

    //load spatialdata
    SE_Vector4f effectData;
    for(int i = 0; i < 4; ++i)
    {
        pos = effect.find(",");
        std::string dataString = effect.substr(0,pos);
        float data = SE_Util::stringToFloat(dataString.c_str());
        effectData.d[i] = data;
        effect = effect.substr(pos + 1,effect.size());
    }
    this->setEffectData(effectData);

    if(SE_Application::getInstance()->SEHomeDebug)
    {
        SE_TimeMS finishTime = SE_Time::getCurrentTimeMS();

        LOGI("\n\n SE_Geometry::inflateFromXML :[%s] load finish, duration = %lu ms\n\n",effectString,finishTime - currTime);
    }
}

const char* SE_Geometry::getImageDataID(int index)
{
    SE_Mesh* m = this->getCurrentAttachedSimObj()->getMesh();
    SE_TextureUnit* tu = m->getSurface(0)->getTexture()->getTextureUnit(0);
    SE_ImageDataID *idarray = tu->getImageDataID();
    return idarray[index].getStr();
}

bool SE_Geometry::hasSpotLight() {
    std::vector < std::string > namelist;
    getLightsNameList (namelist);
    for (int i = 0; i < namelist.size(); i++) {
        std::string name = namelist[i];
        SE_Light* l = getScene()->getLight(name.c_str());
        if (l && l->getLightType() == SE_Light::SE_SPOT_LIGHT) {
            return true;
        }
    }
    return false;
}
