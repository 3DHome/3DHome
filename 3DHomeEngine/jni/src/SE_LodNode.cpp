#include "SE_DynamicLibType.h"
#include "SE_Spatial.h"
#include "SE_Factory.h"
#include "SE_CommonNode.h"
#include "SE_LodNode.h"
#include "SE_Camera.h"
#include "SE_Buffer.h"
#include "SE_BoundingVolume.h"
#include "SE_MemLeakDetector.h"
IMPLEMENT_OBJECT(SE_LodNode)

class _IsRemoved
{
public:
    _IsRemoved(float min, float max, float index) : sr(min, max, index)
    {
        
    }
    bool operator()(const SE_LodNode::_Range& r)
    {
        if(sr.min == r.min && sr.max == r.max && sr.index == r.index)
            return true;
        else
            return false;
    }
    SE_LodNode::_Range sr;
};


SE_LodNode::SE_LodNode(SE_Spatial* parent) : SE_CommonNode(parent)
{
    this->addRange(1,170.9999,1);
	this->addRange(171,9998,0);
}
SE_LodNode::SE_LodNode(SE_SpatialID id, SE_Spatial* parent) : SE_CommonNode(id, parent)
{
}
SE_LodNode::~SE_LodNode()
{}
void SE_LodNode::addRange(float min, float max, int index)
{
    _Range r; 
    r.min = min;
    r.max = max;
    r.index = index;
    mRangeData.push_back(r);
}

SE_Vector3f SE_LodNode::getCenter()
{
    SE_Spatial* s = getSpatialByIndex(0);
    if(s)
    {
        SE_BoundingVolume* bv = s->getWorldBoundingVolume();
        return bv->getCenter();
    }
    return SE_Vector3f();
}
void SE_LodNode::removeRange(float min, float max, int index)
{
    _IsRemoved r(min, max, index);
    mRangeData.remove_if(r);
}
void SE_LodNode::renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType)
{
	if(!isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
		return;
    SE_BoundingVolume* bv = getWorldBoundingVolume();
	SE_CULL_TYPE currCullType = SE_PART_CULL;
    if(bv && cullType == SE_PART_CULL)
    {
        int culled = camera->cullBV(*bv);
        if(culled == SE_FULL_CULL)
            return;
        else
            currCullType = (SE_CULL_TYPE)culled;
    }
    SE_Vector3f center = getCenter();
    SE_Vector3f distance = camera->getLocation() - center;
    float f = distance.length();
    int index = -1;
    //std::list<SE_Spatial*>::iterator it;
    //for(it = mImpl->children.begin() ; it != mImpl->children.end() ; it++)
    std::list<_Range>::iterator it;
    for(it = mRangeData.begin() ; it != mRangeData.end() ; it++)
    {
        _Range r = *it;
        if(f >= r.min && f < r.max)
        {
            index = r.index;;
            break;
        }
    }
    SE_ASSERT(index != -1);
    SE_Spatial* s = getSpatialByIndex(index);
    if(s)
    {
        s->renderScene(camera, renderManager, currCullType);
    } 
}
void SE_LodNode::write(SE_BufferOutput& output)
{
    output.writeString("SE_LodNode");
    output.writeInt(mImpl->children.size());
	int type = (int)getNodeType();
	output.writeInt(type);
    SE_Spatial::write(output);
}
void SE_LodNode::read(SE_BufferInput& input)
{
	int type = input.readInt();
	switch(type)
	{
	case 0:
		setNodeType(ROOT_NODE);
		break;
	case 1:
		setNodeType(GROUP_NODE);
		break;
	case 2:
		setNodeType(LOD_NODE);
		break;
	}
    SE_Spatial::read(input);
}

