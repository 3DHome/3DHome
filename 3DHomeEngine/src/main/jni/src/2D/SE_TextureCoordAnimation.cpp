#include "SE_DynamicLibType.h"
#include "SE_TextureCoordAnimation.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_ImageData.h"
#include "SE_Primitive.h"
#include "SE_SimObject.h"
#include "SE_SimObjectManager.h"
#include "SE_Log.h"
#include "SE_Interpolate.h"
#include <utility>
#include <algorithm>
#include "SE_MemLeakDetector.h"
SE_TextureCoordAnimation::SE_TextureCoordAnimation()
{
    mUnitWidth = 1;
    mUnitHeight = 1;
}
SE_TextureCoordAnimation::~SE_TextureCoordAnimation()
{}
void SE_TextureCoordAnimation::removeCoord(const Point& p)
{
    _CoordList::iterator it = find(mCoordList.begin(), mCoordList.end(), p);
    if(it != mCoordList.end())
    {
        mCoordList.remove(p);
    }
}
void SE_TextureCoordAnimation::onRun()
{
    if(mCoordList.size() > 0)
    {
        setTimePerFrame(getDuration() / mCoordList.size());
    }
    setFrameNum(mCoordList.size());
}
void SE_TextureCoordAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex)
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();
    if(frameIndex == getCurrentFrame())
        return;
    _CoordList::iterator it = mCoordList.begin();
    for(int i = 0 ; i < frameIndex && it != mCoordList.end(); i++)
    {
        it++;
    }
    if(it != mCoordList.end())
    {
        int x = it->x * mUnitWidth;
        int y = it->y * mUnitHeight;
        int w = mUnitWidth;
        int h = mUnitHeight;
        SE_RectPrimitive* primitive = (SE_RectPrimitive*)resourceManager->getPrimitive(getPrimitiveID());
        SE_ImageData* imageData = resourceManager->getImageData(mImageDataID);
        primitive->setImageData(imageData, SE_TEXTURE0, NOT_OWN, SE_ImageDataPortion(x, y, w, h));
        SE_Mesh** meshArray = NULL;
        int meshNum = 0;
        primitive->createMesh(meshArray, meshNum);
        SE_ASSERT(meshNum == 1);
        SE_SimObject* simObject = simObjectManager->get(getSimObjectID());
        simObject->setMesh(meshArray[0], OWN);
        delete[] meshArray;
    }
}

SE_Animation* SE_TextureCoordAnimation::clone()
{
    SE_TextureCoordAnimation* anim = new SE_TextureCoordAnimation;
    if(!anim)
        return NULL;
    anim->setAnimState(getAnimState());
    anim->setRunMode(getRunMode());
    anim->setTimeMode(getTimeMode());
    anim->setDuration(getDuration());
    anim->setPassedTime(getPassedTime());
    SE_Interpolate* interpolate = getInterpolate();
    if(interpolate)
        anim->setInterpolate(interpolate->clone());
    anim->setSpatialID(getSpatialID());
    anim->setPrimitiveID(getPrimitiveID());
    anim->setSimObjectID(getSimObjectID());
    anim->setFrameNum(getFrameNum());
    anim->setCurrentFrame(getCurrentFrame());
    anim->setTimePerFrame(getTimePerFrame());
    anim->mUnitWidth = mUnitWidth;
    anim->mUnitHeight = mUnitHeight;
    anim->mCoordList = mCoordList;
    anim->mImageDataID = mImageDataID;
    return anim;
}
