#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Mutex.h"
#include "SE_CommonNode.h"
#include "SE_Buffer.h"
#include "SE_Camera.h"
#include "SE_BoundingVolume.h"
#include "SE_MeshSimObject.h"
#include "SE_Log.h"
#include "SE_SceneManager.h"
#include "SE_MemLeakDetector.h"
#include "SE_Application.h"
#include "SE_ObjectManager.h"


IMPLEMENT_OBJECT(SE_CommonNode)

SE_CommonNode::SE_CommonNode(SE_Spatial* parent) : SE_Spatial(parent)
{
    mType = ROOT_NODE;
    setBVType(AABB);
    mImpl = new SE_CommonNode::_Impl;
}
SE_CommonNode::SE_CommonNode(SE_SpatialID id, SE_Spatial* parent) : SE_Spatial(id, parent)
{
    mType = ROOT_NODE;
    setBVType(AABB);
    mImpl = new SE_CommonNode::_Impl;
}
SE_CommonNode::~SE_CommonNode()
{
    delete mImpl;
}
SE_Spatial* SE_CommonNode::getSpatialByIndex(int index)
{
    if(index < 0 || index >= mImpl->children.size())
        return NULL;
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    int i = 0;

    for(; it != mImpl->children.end(); it++)
    {
        SE_Spatial* s = *it;
        s->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,false);
    }

    it = mImpl->children.begin();
    for(; it != mImpl->children.end(); it++)
    {
        if(i < index)
        {            
            i++;
        }
        else
            break;
    }
    SE_ASSERT(it != mImpl->children.end());
    SE_Spatial* s = *it;
    s->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
    return *it;

}

void SE_CommonNode::addChild(SE_Spatial* child)
{
	std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
		if(s == child)
			return;
	}
    mImpl->children.push_back(child);
}
void SE_CommonNode::removeChild(SE_Spatial* child)
{
    mImpl->children.remove(child);
}

void SE_CommonNode::replaceChildParent(SE_Spatial* parent)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        parent->addChild(s);
        s->setParent(parent);
    }
    mImpl->children.clear();
}
void SE_CommonNode::updateRenderState()
{
	SE_Spatial::updateRenderState();
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
		s->updateRenderState();
	}
}
void SE_CommonNode::updateWorldTransform()
{
        SE_Spatial::updateWorldTransform();
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        s->updateWorldTransform();
    }
}
void SE_CommonNode::updateWorldLayer()
{
    SE_Spatial::updateWorldLayer();
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        s->updateWorldLayer();
    }
}
SPATIAL_TYPE SE_CommonNode::getSpatialType()
{
	return NODE;
}
int SE_CommonNode::travel(SE_SpatialTravel* spatialTravel, bool travelAlways)
{
    int ret = spatialTravel->visit(this);
    if(ret)
        return ret;
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;

        int r = s->travel(spatialTravel, travelAlways);
        if(r && !travelAlways)
            break;

    }
    return ret;
}
void SE_CommonNode::renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType)
{
	if(!isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
    {
        /*std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
        for(; it != mImpl->children.end() ; it++)
        {
            SE_Spatial* s = *it;
            s->clearSpatialStatus();
        }*/
        
		return;
    }
    SE_BoundingVolume* bv = getWorldBoundingVolume();
    SE_CULL_TYPE currCullType = SE_PART_CULL;
    if(bv && cullType == SE_PART_CULL)
    {
        int culled = camera->cullBV(*bv);
        if(culled == SE_FULL_CULL)
        {
            setSpatialRuntimeAttribute(SE_SpatialAttribute::CAMERAFULLCULL,true);
            return;
        }
        else
        {
            currCullType = (SE_CULL_TYPE)culled;
    }
    }
    setSpatialRuntimeAttribute(SE_SpatialAttribute::CAMERAFULLCULL,false);
    std::list<SE_Spatial*>::iterator it;
    for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        s->renderScene(camera, renderManager, currCullType);
    }

}
void SE_CommonNode::updateBoundingVolume()
{ 
    if(this->getParent())
    {    
        this->getParent()->updateBoundingVolume();
    }
    autoUpdateBoundingVolume();    
}
void SE_CommonNode::write(SE_BufferOutput& output)
{
    output.writeString("SE_CommonNode");
    output.writeInt(mImpl->children.size());
	int type = (int)mType;
	output.writeInt(type);
    SE_Spatial::write(output);
    /*
    std::list<SE_Spatial*>::iterator it;
    for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        s->write(output);
    }
    */
}
void SE_CommonNode::writeEffect(SE_BufferOutput& output)
{
    SE_Spatial::writeEffect(output);
}

void SE_CommonNode::read(SE_BufferInput& input)
{
    //std::string str = input.readString();
    //int childNum = input.readInt();
	int type = input.readInt();
	switch(type)
	{
	case 0:
		mType = ROOT_NODE;
		break;
	case 1:
		mType = GROUP_NODE;
		break;
	case 2:
		mType = LOD_NODE;
		break;
	}
    SE_Spatial::read(input);
    /*
    if(childNum > 0)
    {

    }
    */
}
void SE_CommonNode::readEffect(SE_BufferInput& input)
{
    SE_Spatial::readEffect(input);
}

void SE_CommonNode::showAllNode()
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;

        if(!s->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
        {
            s->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,true);
        }
    }
}

void SE_CommonNode::hideAllNode()
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;

        if(s->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
        {
            s->setSpatialStateAttribute(SE_SpatialAttribute::VISIBLE,false);
        }
    }
}

void SE_CommonNode::unLoadSceneMustInvokeByCommand()
{    
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        delete sp;        
    }
    mImpl->children.clear();
}


int SE_CommonNode::getLastestLayerInWorld()
{
    int max = 0;
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;

        
        int curr = s->getWorldLayer()->getLayer();
        if(curr > max)
        {
            max = curr;
        }
    }
    return max;
}

SE_Spatial *SE_CommonNode::getGroupNode(const char *groupname,NodeTypes type)
{
	std::string gname = groupname;
	std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
	{
		SE_CommonNode * cn = (SE_CommonNode *)(*it);

		if(!gname.compare(cn->getGroupName()) && cn->getNodeType() == type)
		{
			return cn;
		}
	}
	return NULL;
}

SE_Spatial* SE_CommonNode::clone(SE_Spatial* parent, int index,bool createNewMesh,const char* statuslist)
{
    SE_TimeMS currTime = 0;
    if(SE_Application::getInstance()->SEHomeDebug)
    {
        currTime = SE_Time::getCurrentTimeMS();
    }

    if(!parent)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("No parent,clone fail.!!!!");
        return NULL;
    }

    SE_CommonNode * dest = new SE_CommonNode();
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

    dest->setRenderState(DEPTHTESTSTATE ,this->getRenderState(DEPTHTESTSTATE));   

    /*char buff[512] = {0};
    sprintf(buff,"%d",index);
    std::string num(buff);
    
    std::string objName = getSpatialName();
    int x = objName.find("_clone_");
    std::string destName;
    if (x > 0) {
        destName = objName.substr(x + 7) + num;
    } else {
        destName = objName + "_clone_" + num;
    }*/

    dest->setSpatialName(this->getSpatialName());
    dest->setCloneIndex(index);

    /* app control clone spatial attachment*/
    SE_Scene* scene = getScene();
    scene->addSpatial(parent, dest);

    /*std::string statusList;
    if(statuslist == NULL)
    {
        for(int i = 0; i < mImpl->children.size();++i)
        {
            std::string zero = "0,";
            statusList += zero;
        }
    }
    else
    {
        statusList = statuslist;
    }    

    std::vector<std::string> result;

    while(1)
    {
        if(statusList.size() <= 1)
        {
            break;
        }
        int pos = statusList.find(",");

        std::string s = statusList.substr(0,pos);

        statusList = statusList.substr(pos + 1,statusList.size());

        result.push_back(s);
    }
    */
    //parent->updateWorldLayer();
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
   // std::vector<std::string>::iterator statusit = result.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;
        sp->clone(dest, index,createNewMesh, NULL);
       // statusit++;

    }

    dest->updateWorldTransform();
    dest->updateBoundingVolume();
    dest->updateRenderState();

    if(SE_Application::getInstance()->SEHomeDebug)
    {
        SE_TimeMS finishTime = SE_Time::getCurrentTimeMS();
        LOGI("\n\n SE_CommonNode::clone  spatial %s, duration = %lu ms\n\n",this->getSpatialName(),finishTime - currTime);
    }

    return dest;
}

void SE_CommonNode::setAlpha(float alpha)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setAlpha(alpha);
    }
}

void SE_CommonNode::setShaderType(const char* shaderType)
{
    SE_Spatial::setShaderType(shaderType);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setShaderType(shaderType);
    }
}

void SE_CommonNode::setRenderType(const char* renderType)
{
    SE_Spatial::setRenderType(renderType);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setRenderType(renderType);
    }
}

void SE_CommonNode::changeRenderState(int type,bool enable)
{
    SE_Spatial::changeRenderState(type,enable);

    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->changeRenderState(type,enable);
    }
}

void SE_CommonNode::setUseStencil(bool useStencil,const char* mirrorName,SE_MirrorPlane mirrorPlane,float translateAlignedAxis)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setUseStencil(useStencil,mirrorName,mirrorPlane,translateAlignedAxis);        
    }
}

void SE_CommonNode::setChangeStencilPermit(bool hasChangePermit)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setChangeStencilPermit(hasChangePermit);        
    }
}


void SE_CommonNode::setUserColor(SE_Vector3f color)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setUserColor(color);        
    }
}

void SE_CommonNode::setUseUserColor(int use)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setUseUserColor(use);
    }
}
void SE_CommonNode::updateUsetRenderState()
{
    if(isSpatialEffectHasAttribute(SE_SpatialAttribute::BLENDABLE))
    {
        changeRenderState(1,true);
    }
}
void SE_CommonNode::setNeedGenerateShadow(bool need)
{

    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedGenerateShadow(need);
    }
}
void SE_CommonNode::setNeedRenderShadow(bool need)
{

    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedRenderShadow(need);
    }
}


void SE_CommonNode::setEnableCullFace(bool enable)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setSpatialEffectAttribute(SE_SpatialAttribute::CULLFACE,enable);
    }
}
void SE_CommonNode::setMirrorInfo(const char* mirrorName,SE_MirrorPlane mirrorPlane)
{

    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setMirrorInfo(mirrorName,mirrorPlane);
    }
}
void SE_CommonNode::removeAllLight()
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->removeAllLight();
    }
}
void SE_CommonNode::removeLight(const char *lightName)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->removeLight(lightName);
    }
}
void SE_CommonNode::clearSpatialStatus()
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->clearSpatialStatus();
    }
}

void SE_CommonNode::setNeedGenerateMipMap(bool need)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedGenerateMipMap(need);
    }
}
void SE_CommonNode::forceUpdateBoundingVolume()
{
    std::list<SE_Spatial*>::iterator it; 

    for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
    {
        SE_Spatial* s = *it;
        s->forceUpdateBoundingVolume();            
        s->setTransformChanged(false);
    }
    //force clear this status
    clearChildrenHasTransform();
    setTransformChanged(false);

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
		for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
		{
			SE_Spatial* s = *it;
			mWorldBoundingVolume->merge(s->getWorldBoundingVolume());	        
		}
	}
}

void SE_CommonNode::autoUpdateBoundingVolume()
{
    if(this->isChangedTransform())
    {
        this->forceUpdateBoundingVolume();        
    }
    else if(this->getChildrenHasTransform())
    {
        //on of my children transform
	    std::list<SE_Spatial*>::iterator it;
        for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
        {
            SE_Spatial* s = *it;            
            s->autoUpdateBoundingVolume();           
        }

        clearChildrenHasTransform();

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
	        for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
	        {
		        SE_Spatial* s = *it;
		        mWorldBoundingVolume->merge(s->getWorldBoundingVolume());	        
	        }
        }
    }
    else
    {
        //no change about this node
        return;
    }
}
void SE_CommonNode::setTexCoordOffset(SE_Vector2f offet)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setTexCoordOffset(offet);
    }
}
void SE_CommonNode::setNeedParticle(bool need)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedParticle(need);
    }
}

void SE_CommonNode::setSpatialState(unsigned int s)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setSpatialState(s);
    }
}

void SE_CommonNode::setEffectState(unsigned int s)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setEffectState(s);
    }
}

unsigned SE_CommonNode::getState()
{
    return 0;
}

void SE_CommonNode::setShadowObjectVisibility(bool v)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setShadowObjectVisibility(v);
    }
}
void SE_CommonNode::setTexCoordXYReverse(bool x,bool y)
{    
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setTexCoordXYReverse(x,y);
    }
}

int SE_CommonNode::getChildrenNum()
{
    return mImpl->children.size();
}
SE_Spatial* SE_CommonNode::getChildByIndex(int index)
{
    int i = 0;
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();

    for(; it != mImpl->children.end() ; it++)
    {        
        if(i == index)
        {
            return *it;
        }
        i++;
    }
}

std::string SE_CommonNode::getChildrenStatus()
{    
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();

    std::string statusstring;
    for(; it != mImpl->children.end();it++)
    {
        SE_Spatial* s = *it;
        unsigned int status = s->getEffectState();

        statusstring += SE_Util::intToString(status);
            statusstring +=",";
        }
    return statusstring;
}

void SE_CommonNode::clearMirror()
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->clearMirror();
    }
}

void SE_CommonNode::setNeedBlackWhiteColor(bool need)
{
    SE_Spatial::setNeedBlackWhiteColor(need);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedBlackWhiteColor(need);
    }
}
void SE_CommonNode::setNeedBlendSortOnX(bool need)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedBlendSortOnX(need);
    }
}

void SE_CommonNode::setNeedBlendSortOnY(bool need)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedBlendSortOnY(need);
    }
}

void SE_CommonNode::setNeedBlendSortOnZ(bool need)
{
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setNeedBlendSortOnZ(need);
    }
}

void SE_CommonNode::setSpatialEffectAttribute(SE_SpatialAttribute::SpatialEffect effect,bool enableOrDisable)
{
    SE_Spatial::setSpatialEffectAttribute(effect,enableOrDisable);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setSpatialEffectAttribute(effect,enableOrDisable);
    }
}

void SE_CommonNode::setSpatialStateAttribute(SE_SpatialAttribute::SpatialProperty spatialState,bool enableOrDisable)
{
    SE_Spatial::setSpatialStateAttribute(spatialState,enableOrDisable);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setSpatialStateAttribute(spatialState,enableOrDisable);
    }
}

void SE_CommonNode::setSpatialRuntimeAttribute(SE_SpatialAttribute::SpatialRuntimeStatus spatialState,bool enableOrDisable)
{
    SE_Spatial::setSpatialRuntimeAttribute(spatialState,enableOrDisable);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {        
        SE_Spatial *sp = *it;        
        sp->setSpatialRuntimeAttribute(spatialState,enableOrDisable);
    }
}

void SE_CommonNode::setShadowColorDensity(float density)
{
    SE_Spatial::setShadowColorDensity(density);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial *sp = *it;
        sp->setShadowColorDensity(density);
    }
}

void SE_CommonNode::setMirrorColorDesity(float density)
{
    SE_Spatial::setMirrorColorDesity(density);
    std::list<SE_Spatial*>::iterator it = mImpl->children.begin();
    for(; it != mImpl->children.end() ; it++)
    {
        SE_Spatial *sp = *it;
        sp->setMirrorColorDesity(density);
    }
}


