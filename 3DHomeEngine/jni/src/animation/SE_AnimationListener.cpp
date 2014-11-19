#include "SE_Interpolate.h"
#include "SE_AnimationListener.h"
#include "SE_Common.h"
#include "SE_Log.h"
#include "SE_Mesh.h"
#include "SE_Spatial.h"
#include "SE_MeshSimObject.h"
#include "SE_GeometryData.h"
#include "SE_Animation.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_SceneManager.h"

#include "tinyxml.h"
#include "SE_MemLeakDetector.h"

std::vector<SE_AnimationListener*> SE_AnimationListenerFactory::createListener(const char* xmlPath)
{
    std::string path = xmlPath;
    size_t pos = path.rfind('/');
    path = path.substr(0,pos + 1);

    std::vector<SE_AnimationListener*> listeners;
#ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile(data,len);
    delete data;
#else

    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif
    if(!ok)
    {
        LOGI("Error,load xml[%s] fail!!\n",xmlPath);
        return listeners;
    }    

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem = NULL;
    TiXmlElement* temp = NULL;
    TiXmlHandle hRoot(0);
    TiXmlElement* parent = NULL;

    parent = hDoc.FirstChildElement("animation_listeners").Element();

    if(!parent)
    {
        LOGI("animation_listener not found!!\n");
        return listeners;
    }

    pElem = parent->FirstChildElement("animation_listener");
    parent = parent->FirstChildElement("animation_listener");

    for(parent;parent;parent = parent->NextSiblingElement())
    {
        int start,end;
        parent->QueryIntAttribute("startFrame",&start);
        parent->QueryIntAttribute("endFrame",&end);
    
    int listenerMode = -1;
        pElem = parent->FirstChildElement("listener_type");


    if(!pElem)
    {
        LOGI("listener_type not found!!\n");
            return listeners;
    }
    pElem->QueryIntAttribute("listenertype",&listenerMode);

    std::string spatialName;
    pElem = pElem->NextSiblingElement("geometry_id");

    if(!pElem)
    {
        LOGI("geometry_id not found!!\n");
            return listeners;
    }
    spatialName = pElem->GetText();

        std::string leaderName;
        temp = pElem->NextSiblingElement("leader_id");
        if(temp)
        {
            leaderName = temp->GetText();
        }


        std::string actionFile;
        pElem = pElem->NextSiblingElement("action");

        if(!pElem)
        {
            LOGI("action not found!!\n");
            return listeners;
        }
        pElem->QueryStringAttribute("file",&actionFile);



        pElem = pElem->NextSiblingElement("interpolate");
        if(!pElem)
        {
            LOGI("interpolate not found!!\n");
            return listeners;
        }

        int interpolateType = -1;
        pElem = pElem->FirstChildElement("interpolate_type");
        if(!pElem)
        {
            LOGI("interpolate_type not found!!\n");
            return listeners;
        }
        pElem->QueryIntAttribute("interpolate",&interpolateType);

        float interpolateFactor = 1.0;
        pElem = pElem->NextSiblingElement("interpolate_factor");
        if(!pElem)
        {
            LOGI("interpolate_factor not found!!\n");
            return listeners;
        }
        pElem->QueryFloatAttribute("para",&interpolateFactor);


        SE_Spatial* sp = SE_Application::getInstance()->getSceneManager()->findSpatialByName(spatialName.c_str());

    if(!sp)
    {
        LOGI("geometry not found!!\n");
            return listeners;
    }
    
    SE_AnimationListener* l = SE_AnimationListenerFactory::createListener((ANIMATION_LISTENER_TYPE)listenerMode);  

    if(!l)
    {
            return listeners;
        }

        if(!leaderName.empty())
        {
            SE_Spatial* leader = SE_Application::getInstance()->getSceneManager()->findSpatialByName(leaderName.c_str());

            if(!leader)
            {
                return listeners;
            }

            l->setAnimationLeaderSpatial(leader);
        }

        l->setListenerStartPoint(start);
        l->setListenerEndPoint(end);

        if(!actionFile.empty())
        {
            std::string actionFilePath = path + actionFile;
            if(!l->inflate(actionFilePath.c_str()))
            {
                LOGI("listen inflat fail,return\n");
                delete l;
                return listeners;
            }
    }

        SE_Interpolate* interp = SE_InterpolateFactory::create((INTERPOLATE_TYPE)interpolateType);

        l->setInterpolate(interp);
        if(interp)
        {
            interp->setFactor(interpolateFactor);
        }

    l->setAnimationSpatial(sp);
        listeners.push_back(l);        
    }

    return listeners;
}
SE_AnimationListener* SE_AnimationListenerFactory::createListener(ANIMATION_LISTENER_TYPE mode)
{
    if(mode == MESH_ANIMATION)
    {
        return new SE_MeshAnimationListener();
    }
    else if(mode == RIGID_ANIMATION)
    {
        return new SE_RigidAnimationListener();
    }
    else if(mode == RIGID_ANIMATION_ROTATE_TO_ZERO)
    {
        return new SE_RigidAnimationRotateToZeroListener();
    }
    else if(mode == RIGID_ANIMATION_FOLLOWER)
    {
        return new SE_RigidAnimationFollowerListener();
    }

    LOGI("error listener mode!!!\n");
    return NULL;
}

void SE_AnimationListener::onPrepare(SE_Animation* animation,int index)
{
    if(mInterpolate)
    {
        mInterpolate->init();
    }
}

SE_MeshAnimationListener::SE_MeshAnimationListener(SE_Spatial* sp)
{
    mAnimationSpatial = sp;
    mGeoDataList = NULL;
    mInitOK = false;
}
SE_MeshAnimationListener::~SE_MeshAnimationListener()
{

}
void SE_MeshAnimationListener::onPrepare(SE_Animation* animation,int index)
{
    SE_AnimationListener::onPrepare(animation,index);

    if(!mAnimationSpatial)
    {
        mInitOK = false;
        LOGI("Error!!, animation spatial not found!!!!!\n");
        return;
    }

    int cloneindex = mAnimationSpatial->getCloneIndex();

    if(cloneindex != index)
    {
        std::string name = mAnimationSpatial->getSpatialName();
        mAnimationSpatial = SE_Application::getInstance()->getSceneManager()->findSpatialByName(name.c_str(),index);
    }

    if(mInitOK)
    {
        mAnimationSpatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::IGNORVERTEXBUFFER,true);
        return;
    }

    SE_GeometryDataID id = mAnimationSpatial->getSpatialName();
    SE_GeometryDataList* dl = SE_Application::getInstance()->getResourceManager()->getAnimationMeshList(id);

    if(!dl)
    {
        mInitOK = false;
        LOGI("Error,Mesh list not loaded!!!!\n");
        return;
    }

    SE_Application::getInstance()->setFrameRate(animation->getRequiredSystemFrameRate());

    float systemFrameRate = SE_Application::getInstance()->getFrameRate();
    if(animation->getFrameNum() != dl->mGeoDataList.size())
    {
        animation->setFrameNum((dl->mGeoDataList.size()));        
    }   
        
        animation->setTimePerFrame(systemFrameRate * animation->getSpeed());
    animation->setDuration(systemFrameRate*(dl->mGeoDataList.size()) * animation->getSpeed());

    

    mInitOK = true;
    mAnimationSpatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::IGNORVERTEXBUFFER,true);;//need use datalist replace current surface mesh
    mGeoDataList = dl;
    

    return;
}

void SE_MeshAnimationListener::onUpdateFrame(SE_Animation *animation)
{
    if(!mInitOK)
    {
        return;
    }
    int currentFrame = animation->getCurrentFrame();

    SE_Surface* sur = mAnimationSpatial->getCurrentAttachedSimObj()->getMesh()->getSurface(0);

    SE_Mesh* m = mAnimationSpatial->getCurrentAttachedSimObj()->getMesh();    

    int index = currentFrame - 1;
    sur->setGeometryData(mGeoDataList->mGeoDataList[index]);
    sur->upDateFaceVertex();
    m->setGeometryData(mGeoDataList->mGeoDataList[index]);

    mAnimationSpatial->updateWorldTransform();
    mAnimationSpatial->setChildrenHasTransform(true);
    mAnimationSpatial->setTransformChanged(true);
}

void SE_MeshAnimationListener::onEnd(SE_Animation *animation)
{
    if(!mInitOK)
    {
        return;
    }

    mAnimationSpatial->setSpatialRuntimeAttribute(SE_SpatialAttribute::IGNORVERTEXBUFFER,false);
    mAnimationSpatial->getCurrentAttachedSimObj()->getMesh()->restorDefaultGeometryData();
    mAnimationSpatial->updateWorldTransform();
    mAnimationSpatial->setChildrenHasTransform(true);
    mAnimationSpatial->setTransformChanged(true);

}

bool SE_MeshAnimationListener::inflate(const char *xmlPath)
{
    return false;
}

SE_RigidAnimationListener::SE_RigidAnimationListener(SE_Spatial* sp)
{

    mAnimationSpatial = sp;    
    mInitOK = false;
    mInterpolate = NULL;

    mStartDegree = 0.0;
    mEndDegree = 0.0;

    mBackupStartDegree = 0.0;
    mBackupEndDegree = 0.0;

    mInitAlpha = 0.0;
    mStartAlpha = 1.0;
    mEndAlpha = 1.0;
    mBackupStartAlpha = 0.0;
    mBackupEndAlpha = 0.0;

}

SE_RigidAnimationListener::~SE_RigidAnimationListener()
{    

}

void SE_RigidAnimationListener::onPrepare(SE_Animation* animation,int index)
{   

    if(!mAnimationSpatial)
    {
        mInitOK = false;
        LOGI("Error!!, rigid animation spatial not found!!!!!\n");
        return;
    }

    mBackupStartPos = mStartPos;
    mBackupEndPos = mEndPos;

    mBackupStartScale = mStartScale;
    mBackupEndScale = mEndScale;

    mBackupStartDegree = mStartDegree;
    mBackupEndDegree = mEndDegree;

    mBackupStartAlpha = mStartAlpha;
    mBackupEndAlpha = mEndAlpha;

    int cloneindex = mAnimationSpatial->getCloneIndex();

    if(cloneindex != index)
    {
        std::string name = mAnimationSpatial->getSpatialName();
        mAnimationSpatial = SE_Application::getInstance()->getSceneManager()->findSpatialByName(name.c_str(),index);
    }

    SE_AnimationListener::onPrepare(animation,index);

    saveInit();

    if(!mStartPos.isZero())
    {
        //set spatial pos
        mAnimationSpatial->setUserTranslateIncremental(mStartPos);
        mAnimationSpatial->updateWorldTransform();
    }

    return;
}
void SE_RigidAnimationListener::saveInit()
{
    mInitPos = mAnimationSpatial->getUserTranslate();
    mInitScale = mAnimationSpatial->getUserScale();

    float r = mAnimationSpatial->getUserRotate().toRotate().x;
    SE_Vector3f axis = SE_Vector3f(mAnimationSpatial->getUserRotate().toRotate().y,mAnimationSpatial->getUserRotate().toRotate().z,mAnimationSpatial->getUserRotate().toRotate().w);
    mInitRotate.set(axis,r);
    mInitAlpha = mAnimationSpatial->getAlpha();
}
void SE_RigidAnimationListener::resteFrame()
{
    
}

void SE_RigidAnimationListener::changeStatus(bool changeStart)
{
    if(changeStart)
    {
        mStartPos = mAnimationSpatial->getUserTranslate();
        mStartScale = mAnimationSpatial->getUserScale();
        SE_Quat curr = mAnimationSpatial->getUserRotate();
        mStartDegree = curr.toRotate().x;
        mStartAlpha = mAnimationSpatial->getAlpha();

        mEndPos = mBackupEndPos;
        mEndScale = mBackupEndScale;
        mEndDegree = mBackupEndDegree;
        mEndAlpha = mBackupEndAlpha;
    }
    else
    {
        mEndPos = mAnimationSpatial->getUserTranslate();
        mEndScale = mAnimationSpatial->getUserScale();
        SE_Quat curr = mAnimationSpatial->getUserRotate();
        mEndDegree = curr.toRotate().x;
        mEndAlpha = mAnimationSpatial->getAlpha();

        mStartPos = mBackupStartPos;
        mStartScale = mBackupStartScale;
        mStartDegree = mBackupStartDegree;
        mStartAlpha  = mBackupStartAlpha;
    }
    
}

void SE_RigidAnimationListener::onUpdateFrame(SE_Animation *animation)
{

    bool reverse = false;
    if(animation->getRunMode() == SE_Animation::REVERSE_NOT_REPEAT)
    {
        reverse = true;
    }
    int frameIndex = animation->getCurrentFrame();
    int frameNum = animation->getFrameNum();
    float speed = animation->getSpeed();

    

    if(this->mStartFrame != this->mEndFrame)
    {
        //listener has own duration
        if(frameIndex > this->mEndFrame || frameIndex < this->mStartFrame)
        {
            //own duration not included in animation duration
            return;
        }

        frameNum = mEndFrame - mStartFrame + 1;
        
    }

    frameNum *= speed;

    float input = 1.0;
    if(reverse)
    {
        input = (float)(frameNum - (frameIndex - mStartFrame)) / (float)frameNum; //reverse input should from 0 ~ 1,not 1~0
    }
    else
    {
        input = (float)(frameIndex - mStartFrame + 1) / (float)frameNum;
    }
    float interpolate = mInterpolate->calc(input);    

    SE_Vector3f deltaTranslate = mEndPos.subtract(mStartPos);    
    SE_Vector3f translatePerFrame;
    if(reverse)
    {
        translatePerFrame = mEndPos.subtract(deltaTranslate.mul(interpolate));
    }
    else
    {
        translatePerFrame = mStartPos.add(deltaTranslate.mul(interpolate));//.subtract(mPrePos);
    }

    SE_Vector3f deltaScale = mEndScale.subtract(mStartScale);
    SE_Vector3f scalePerFrame(1,1,1);

    if(reverse)
    {
        scalePerFrame = mEndScale.subtract(deltaScale.mul(interpolate));
    }
    else
    {
        scalePerFrame = mStartScale.add(deltaScale.mul(interpolate));
    }
    
    float deltaRotate = mEndDegree - mStartDegree;
    float degreePerFrame = deltaRotate *(interpolate);
    
    SE_Quat rotatePerFrame;
    if(reverse)
    {
        //degreePerFrame *= -1.0;
        rotatePerFrame.set(mEndDegree -degreePerFrame,mAxis);
    }
    else
    {
        rotatePerFrame.set(mStartDegree + degreePerFrame,mAxis);
    }

    float deltaAlpha = mEndAlpha - mStartAlpha;
    float alphaPerFrame = deltaAlpha *(interpolate);

    if(reverse)
    {
        alphaPerFrame = mEndAlpha - alphaPerFrame;
    }
    else
    {
        alphaPerFrame =  mStartAlpha + alphaPerFrame;
    }

    mAnimationSpatial->setUserTranslate(translatePerFrame);
    mAnimationSpatial->setUserRotate(rotatePerFrame);    
    mAnimationSpatial->setUserScale(scalePerFrame);

    if(mAnimationSpatial->isSpatialEffectHasAttribute(SE_SpatialAttribute::BLENDABLE) && mStartAlpha != mEndAlpha)
    {
        mAnimationSpatial->setAlpha(alphaPerFrame);
    }


    mAnimationSpatial->updateWorldTransform();

}

void SE_RigidAnimationListener::onEnd(SE_Animation *animation)
{
   
    mStartPos = mBackupStartPos;
    mEndPos = mBackupEndPos;

    mStartScale = mBackupStartScale;
    mEndScale = mBackupEndScale;

    mStartDegree = mBackupStartDegree;
    mEndDegree = mBackupEndDegree;
   
    mStartAlpha = mBackupStartAlpha;
    mEndAlpha = mBackupEndAlpha;

    if(animation->getRunMode() == SE_Animation::REVERSE_NOT_REPEAT)
    {
        SE_Quat r(0,0,0,1);
        mAnimationSpatial->setUserRotate(r);
        mAnimationSpatial->setUserTranslate(mStartPos);
        mAnimationSpatial->setUserScale(mStartScale);

    }
    else
    {
        SE_Quat r;
        r.set(mEndDegree,mAxis);
        mAnimationSpatial->setUserRotate(r);
        mAnimationSpatial->setUserTranslate(mEndPos);
        mAnimationSpatial->setUserScale(mEndScale);
    }
    mAnimationSpatial->updateWorldTransform();
}
bool SE_RigidAnimationListener::inflate(const char *xmlPath)
{
    #ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile(data,len);
    delete data;
#else
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif
    if(!ok)
    {
        LOGI("Error,load xml[%s] fail!!\n",xmlPath);
        return false;
    }    

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem = NULL;
    TiXmlHandle hRoot(0);
    TiXmlElement* parent = NULL;

    pElem = hDoc.FirstChildElement("action").Element();

    if(!pElem)
    {
        LOGI("action not found!!\n");
        return false;
    }

    parent = pElem = pElem->FirstChildElement("translate");
    if(!pElem)
    {
        LOGI("translate not found!!\n");
        return false;
    }
    //translat
    pElem = pElem->FirstChildElement("start");
    if(!pElem)
    {
        LOGI("start not found!!\n");
        return false;
    }
    
    pElem->QueryFloatAttribute("x",&mStartPos.d[0]);
    pElem->QueryFloatAttribute("y",&mStartPos.d[1]);
    pElem->QueryFloatAttribute("z",&mStartPos.d[2]);

    pElem = pElem->NextSiblingElement("end");
    if(!pElem)
    {
        LOGI("end not found!!\n");
        return false;
    }
    pElem->QueryFloatAttribute("x",&mEndPos.d[0]);
    pElem->QueryFloatAttribute("y",&mEndPos.d[1]);
    pElem->QueryFloatAttribute("z",&mEndPos.d[2]);


    //scale
    pElem = parent->NextSiblingElement("scale");
    if(!pElem)
    {
        LOGI("scale not found!!\n");
        return false;
    }
    pElem = pElem->FirstChildElement("start");
    if(!pElem)
    {
        LOGI("start not found!!\n");
        return false;
    }
    pElem->QueryFloatAttribute("x",&mStartScale.d[0]);
    pElem->QueryFloatAttribute("y",&mStartScale.d[1]);
    pElem->QueryFloatAttribute("z",&mStartScale.d[2]);

    pElem = pElem->NextSiblingElement("end");
    if(!pElem)
    {
        LOGI("end not found!!\n");
        return false;
    }
    pElem->QueryFloatAttribute("x",&mEndScale.d[0]);
    pElem->QueryFloatAttribute("y",&mEndScale.d[1]);
    pElem->QueryFloatAttribute("z",&mEndScale.d[2]);

    //rotate
    pElem = parent->NextSiblingElement("rotate");
    if(!pElem)
    {
        LOGI("rotate not found!!\n");
        return false;
    }
    pElem = pElem->FirstChildElement("axis");
    if(!pElem)
    {
        LOGI("axis not found!!\n");
        return false;
    }
    pElem->QueryFloatAttribute("x",&mAxis.d[0]);
    pElem->QueryFloatAttribute("y",&mAxis.d[1]);
    pElem->QueryFloatAttribute("z",&mAxis.d[2]);

    pElem = pElem->NextSiblingElement("degree");
    if(!pElem)
    {
        LOGI("start not found!!\n");
        return false;
    }
    pElem->QueryFloatAttribute("start",&mStartDegree);
    pElem->QueryFloatAttribute("end",&mEndDegree);  


    //additional status
    pElem = parent->NextSiblingElement("alpha");
    if(pElem)
    {
        pElem = pElem->FirstChildElement("start");
        if(pElem)
        {
            pElem->QueryFloatAttribute("value",&mStartAlpha);

            pElem = pElem->NextSiblingElement("end");
            if(pElem)
            {
                pElem->QueryFloatAttribute("value",&mEndAlpha);
            }            
        }        

    }   

    return true;
}

SE_RigidAnimationRotateToZeroListener::SE_RigidAnimationRotateToZeroListener(SE_Spatial* sp):SE_RigidAnimationListener(sp)
{
}

SE_RigidAnimationRotateToZeroListener::~SE_RigidAnimationRotateToZeroListener()
{}

void SE_RigidAnimationRotateToZeroListener::onPrepare(SE_Animation* animation,int index)
{

    SE_RigidAnimationListener::onPrepare(animation,index);

    //rotate to zero for group_desk

    SE_Quat currentQuat = mAnimationSpatial->getUserRotate();

    SE_Quat quat360;

    SE_Quat delta = currentQuat.inverse().mul(quat360);

    float deltaRotate = delta.toRotate().x;


    if(currentQuat.x >= 0 || currentQuat.y >= 0 || currentQuat.z >= 0)
    {
        //clock wise
        if(deltaRotate > 180.0)
        {
            mStartDegree = deltaRotate;
            mEndDegree = 0.0;
        }
        else
        {
            mStartDegree = 0.0;
            mEndDegree = 360.0 - deltaRotate;
        }

    }
    else
    {
        if(deltaRotate > 180.0)
        {
            mStartDegree = 0.0;
            mEndDegree = deltaRotate;
        }
        else
        {
            mStartDegree = 360.0 - deltaRotate;
            mEndDegree = 0.0;
        }
    }

}

void SE_RigidAnimationRotateToZeroListener::onEnd(SE_Animation* animation)
{
    SE_Quat zero;
    mAnimationSpatial->setUserRotate(zero);
    mAnimationSpatial->updateWorldTransform();

}

SE_RigidAnimationFollowerListener::SE_RigidAnimationFollowerListener(SE_Spatial* sp):SE_RigidAnimationListener(sp)
{}
SE_RigidAnimationFollowerListener::~SE_RigidAnimationFollowerListener()
{}

void SE_RigidAnimationFollowerListener::onPrepare(SE_Animation *animation,int index)
{
    SE_RigidAnimationListener::onPrepare(animation,index);

    if(mAnimationLeaderSpatial)
    {
        mAnimationSpatial->setLeader(mAnimationLeaderSpatial);

        mAnimationLeaderSpatial->addFollower(mAnimationSpatial);
    }

}
