#include "SE_DynamicLibType.h"
#include "SE_AnimationManager.h"
#include "SE_Animation.h"
#include "SE_Application.h"
#include <list>
#include "SE_MemLeakDetector.h"
SE_AnimationManager::~SE_AnimationManager()
{
    _AnimationMap::iterator it;
    std::list<SE_AnimationID> needToRemove;
    for(it = mAnimationMap.begin() ; it != mAnimationMap.end() ; it++)
    {
        SE_Animation* anim = it->second;
        needToRemove.push_back(it->first);
        delete anim;
    }
    std::list<SE_AnimationID>::iterator itRemove;
    for(itRemove = needToRemove.begin() ; itRemove != needToRemove.end() ; itRemove++)
    {
        mAnimationMap.erase(*itRemove);
    }
}
SE_AnimationID SE_AnimationManager::addAnimation(SE_Animation* anim,const char* id)
{
    if(!anim)
        return SE_AnimationID::INVALID;
    SE_AnimationID animID = SE_ID::createAnimationID(id);
    mAnimationMap[animID] = anim;
    return animID;
}
void SE_AnimationManager::removeAnimation(const SE_AnimationID& animID)
{
    _AnimationMap::iterator it = mAnimationMap.find(animID);
    if(it != mAnimationMap.end())
    {
        SE_Animation* anim = it->second;
        anim->end();
        delete anim;
        mAnimationMap.erase(it);
    }
}
SE_Animation*  SE_AnimationManager::getAnimation(const SE_AnimationID& animID)
{
    _AnimationMap::iterator it = mAnimationMap.find(animID);
    if(it != mAnimationMap.end())
    {
        SE_Animation* anim = it->second;
        return anim;
    }    
    return NULL;
}
void SE_AnimationManager::removeAllEndedAnimation()
{
    _AnimationMap::iterator it;
    std::list<SE_AnimationID> needToRemove;
    for(it = mAnimationMap.begin() ; it != mAnimationMap.end() ; it++)
    {
        SE_Animation* anim = it->second;
        if(anim->isEnd())
        {
            needToRemove.push_back(it->first);
            delete anim;
        }
    }
    std::list<SE_AnimationID>::iterator itRemove;
    for(itRemove = needToRemove.begin() ; itRemove != needToRemove.end() ; itRemove++)
    {
        mAnimationMap.erase(*itRemove);
    }
}

void SE_AnimationManager::update(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    _AnimationMap::iterator it;
    for(it = mAnimationMap.begin() ; it != mAnimationMap.end() ; it++)
    {
        SE_Animation* anim = it->second;
        if(anim->isRunning())
        {
            anim->update(realDelta, simulateDelta);
        }
    }
}

SE_Animation* SE_AnimationManager::inflateAnimationFromXML(const char *xmlPath,bool insert)
{    
    SE_Animation* a = SE_Animation::inflate(xmlPath);
    if(!a)
    {        
        return NULL;
    }
    
    if(insert)
    {
    const char* id = a->getAnimationID();
    this->addAnimation(a,id);
    }
    return a;   

}
