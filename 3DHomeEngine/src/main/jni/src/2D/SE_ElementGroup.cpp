#include "SE_DynamicLibType.h"
#include "SE_ElementGroup.h"
#include "SE_CommonNode.h"
#include "SE_MemLeakDetector.h"
SE_ElementGroup::SE_ElementGroup()
{

}
SE_ElementGroup::~SE_ElementGroup()
{}
void SE_ElementGroup::addChild(SE_Element* e)
{
    mChildren.push_back(e);
    e->setParent(this);
}
void SE_ElementGroup::removeChild(SE_Element* e)
{
    mChildren.remove(e);
    e->setParent(NULL);
}
void SE_ElementGroup::removeChild(const char* id)
{
    _ElementList::iterator it;
    for(it = mChildren.begin() ; it != mChildren.end() ; it++)
    {
        SE_Element* e = *it;
        if(e->getID() == id)
            break;
    }
    if(it != mChildren.end())
    {
        mChildren.erase(it);
    }
}
SE_Spatial* SE_ElementGroup::createSpatial(SE_Spatial* parent)
{
    SE_SpatialID spatialID = SE_ID::createSpatialID();
    SE_CommonNode* commonNode = new SE_CommonNode(spatialID, parent);
    commonNode->setLocalTranslate(SE_Vector3f(getLeft() + getWidth() / 2, getTop() + getHeight() / 2, 0));
    commonNode->setLocalScale(SE_Vector3f(getWidth() / 2, getHeight() / 2, 1));
    if(parent)
        parent->addChild(commonNode);
    _ElementList::iterator it;
    for(it = mChildren.begin() ; it != mChildren.end() ; it++)
    {
        SE_Element* e = *it;
        SE_Spatial* spatial = e->createSpatial(commonNode);
        commonNode->addChild(spatial);
    }
    return commonNode;
}
void SE_ElementGroup::travel(SE_ElementTravel* travel)
{
    travel->visit(this);
    _ElementList::iterator it;
    for(it = mChildren.begin() ; it != mChildren.end() ; it++)
    {
        SE_Element* e = *it;
        travel->visit(e);
    }
}
