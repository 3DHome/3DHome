#include "SE_DynamicLibType.h"
#include <vector>
#include "SE_Quat.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include "SE_BipedAnimation.h"
#include "SE_GeometryData.h"
#include "SE_Mesh.h"
#include "SE_SimObject.h"
#include "SE_Bone.h"
#include "SE_Spatial.h"
#include "SE_Log.h"


#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_MemLeakDetector.h"

class _setBoneState : public SE_SpatialTravel
{
public:    
    int visit(SE_Spatial* spatial)
    {        
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        if(!simObject)
        {
            return 0;
        }
        //generate skeleton info
        SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
        SE_SkeletonController *sk = resourceManager->getSkeletonController(SE_SKELETONCONTROLLER);
        
        if(sk)
        {
            int bipcontrollerNum = sk->mSkeletonController.size();

            for(int c = 0; c < bipcontrollerNum; ++c)
            {
                SE_BipedController * bipedController = sk->mSkeletonController[c];

                if(bipedController)
                {
                    SE_SkeletonUnit *su = bipedController->findSU(simObject->getName());

                    if(su)
                    {
                        simObject->setBoneState(true);
                        simObject->setBoneController(bipedController);
                        simObject->setSkeletonUnit(su);

                        int surfaceNum = simObject->getSurfaceNum();

                        for(int i = 0; i < surfaceNum; ++i)
                        {
                            SE_Surface * surface = simObject->getMesh()->getSurface(i);

                            surface->setCurrentBipedControllerID(su->controllerId.c_str());

                            surface->setCurrentBipedController(bipedController);

                            _Vector3f* vertexT = NULL;
                            int vertexNumT = 0;
                            int* vertexIndexT = NULL;
                            int vertexIndexNumT = 0;

                            surface->getFaceVertex(vertexT, vertexNumT);

                            surface->getVertexIndexInGeometryData(vertexIndexT, vertexIndexNumT);

                            float *weightarray = new float[vertexIndexNumT * 4];//four bone info per vertex
                            float *indexarray = new float[vertexIndexNumT * 4];//four bone info per vertex


                            for(int j = 0; j < vertexIndexNumT; ++j)
                            {                      

                                for(int k = 0; k < 4; ++k)
                                {
                                    if(su->objVertexBlendInfo[vertexIndexT[j]]->weight.size() > 4)
                                    {
                                        if(SE_Application::getInstance()->SEHomeDebug)
                                        LOGI("Error. skeleton num > 4 per vertex!\n");
                                    }

                                    if(k < su->objVertexBlendInfo[vertexIndexT[j]]->weight.size())
                                    {
                                        int bipIndex = su->objVertexBlendInfo[vertexIndexT[j]]->bipedIndex[k];//bipIndex is start from 1, not 0.

                                        int bipindexfromcache = su->bipCache[bipIndex-1]->bipIndexOnBipAnimation;

                                        if(bipindexfromcache >= 30)
                                        {
                                            if(SE_Application::getInstance()->SEHomeDebug)
                                            LOGI("skeleton num too much!!!\n");
                                        }
                                        
                                        //skeleton num >= k
                                        weightarray[j*4 + k] = su->objVertexBlendInfo[vertexIndexT[j]]->weight[k];

                                        indexarray[j*4 + k] = (float)bipindexfromcache;                                
             
                                    }
                                    else
                                    {
                                        //skeleton num < k, and < 4
                                        weightarray[j*4 + k] = 0.0;

                                        indexarray[j*4 + k] = 0.0;
                                    }
                                }
                            }

                            surface->setSkeletonWeight(weightarray);
                            surface->setSkeletonIndex(indexarray);
                           
                        }
                    }
                }
            }
        }

        
        return 0;
    }
    

};

SE_BipedAnimation::SE_BipedAnimation()
{
    mSkinJointController = NULL;
    mMesh = NULL;
    mVertex = NULL;
    mVertexNum = 0;
}
SE_BipedAnimation::~SE_BipedAnimation()
{
    if(mVertex)
        delete[] mVertex;

    for(int i = 0; i < mSurfaceFaceVertexList.size();++i)
    {
        delete [] mSurfaceFaceVertexList[i];
    }

    for(int i = 0; i < mSurfaceFaceVertexIndexList.size();++i)
    {
        delete [] mSurfaceFaceVertexIndexList[i];
    }
}
void SE_BipedAnimation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode)
{
    if(playmode == CPU_NO_SHADER)
    {
        normalPlayMode(frameIndex);
    }
    else if(playmode == GPU_SKELETON_SHADER)
    {
        skinedShaderPlayMode(frameIndex);
    }
    else
    {
        //Do nothing Error;
    }
}
void SE_BipedAnimation::onRun()
{
    if(!mSkinJointController)
        return;

    int num = mSkinJointController->findMaxFrameIndex();
    setFrameNum(num + 1);
    setTimePerFrame(getDuration() / num);

    mMesh = mSimObject->getMesh();
    //Use local coordinate.
    //SE_Matrix4f m;
    //m.identity();
    //mSimObject->setWorldMatrix(m);
    //mSimObject->setUseWorldMatrix(true);    
    mMesh->clearVertexInfo();    

}
SE_Animation* SE_BipedAnimation::clone()
{
    return NULL;
}
void SE_BipedAnimation::onEnd()
{
    if(!mSimObject)
        return;
    if(!mMesh)
        return;
    SE_Matrix4f m;
    m.identity();
    mSimObject->setWorldMatrix(m);
    mSimObject->setUseWorldMatrix(false);
    mSimObject->setPrimitiveType(TRIANGLES);

    for(int j = 0; j < mSimObject->getSurfaceNum(); ++j)
    {
        mSimObject->getMesh()->getSurface(j)->setProgramDataID("default_shader");        
        mSimObject->getMesh()->getSurface(j)->setRendererID("default_renderer");
    }

    mMesh->clearVertexInfo();
}

void SE_BipedAnimation::normalPlayMode(int frameIndex)
{
    //noraml play mode,use default shader
    if(getCurrentFrame() == frameIndex)
        return;
    if(!mSkinJointController)
        return;
    if(!mMesh)
        return;

    //get world coordinate 
    //generate world geometry data
    SE_GeometryData data;
    SE_Matrix4f m2w = mSimObject->getSpatial()->getWorldTransform();
    SE_GeometryData::transform(mSimObject->getMesh()->getGeometryData(),m2w,&data);

    int vNum = data.getVertexNum();
    SE_Vector3f * vertexWrold = data.getVertexArray();


    mVertexWorldAfterBipTransform.resize(vNum,NULL);

    for(int i = 0 ; i < vNum ; i++)
    {
        if(mVertexWorldAfterBipTransform[i] == NULL)
        {
            SE_Vector3f *vt = new SE_Vector3f();
            
            mVertexWorldAfterBipTransform[i] = vt;
        }
        
        *mVertexWorldAfterBipTransform[i] = mSkinJointController->convert(i,frameIndex,mSimObject->getName(), vertexWrold[i]);
        
    }
    
    int surfaceNum = mMesh->getSurfaceNum();
    for(int i = 0 ; i < surfaceNum ; i++)
    {
        _Vector3f* vertexT = NULL;
        int vertexNumT = 0;
        int* vertexIndexT = NULL;
        int vertexIndexNumT = 0;

        SE_Surface* surface = mMesh->getSurface(i);
        
        surface->getFaceVertex(vertexT, vertexNumT);

        surface->getVertexIndexInGeometryData(vertexIndexT, vertexIndexNumT);

        for(int j = 0; j < vertexNumT; ++j)
        {
            vertexT[j].d[0] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[0];
            vertexT[j].d[1] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[1];
            vertexT[j].d[2] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[2];
        }
    }
}

void SE_BipedAnimation::skinedShaderPlayMode(int frameIndex)
{
    //skined shader play mode,use skeleton shader
    if(getCurrentFrame() == frameIndex)
        return;
    if(!mSkinJointController)
        return;
    if(!mMesh)
        return;


    int surfaceNum = mSimObject->getSurfaceNum();
    for(int i = 0; i < surfaceNum; ++i)
    {
        SE_Surface * surface = mSimObject->getMesh()->getSurface(i);
        surface->setCurrentFrameIndex(frameIndex);
    }
    

    //use local coordinate.
#if 0
    //get world coordinate 
    int vNum = mSimObject->getVertexNum();
    SE_Vector3f * vertexWrold = mSimObject->getVertexArray();


    mVertexWorldAfterBipTransform.resize(vNum,NULL);

    for(int i = 0 ; i < vNum ; i++)
    {
        if(mVertexWorldAfterBipTransform[i] == NULL)
        {
            SE_Vector3f *vt = new SE_Vector3f();
            
            mVertexWorldAfterBipTransform[i] = vt;
        }
        
        *mVertexWorldAfterBipTransform[i] = vertexWrold[i];
        
    }
    
    
    for(int i = 0 ; i < surfaceNum ; i++)
    {
        _Vector3f* vertexT = NULL;
        int vertexNumT = 0;
        int* vertexIndexT = NULL;
        int vertexIndexNumT = 0;

        SE_Surface* surface = mMesh->getSurface(i);
        
        surface->getFaceVertex(vertexT, vertexNumT);

        surface->getVertexIndexInGeometryData(vertexIndexT, vertexIndexNumT);

        for(int j = 0; j < vertexNumT; ++j)
        {
            vertexT[j].d[0] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[0];
            vertexT[j].d[1] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[1];
            vertexT[j].d[2] = mVertexWorldAfterBipTransform[vertexIndexT[j]]->d[2];
        }
    }
#endif

}

void SE_BipedAnimation::generateBipedData()
{
    SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
    SE_Scene* sc = scm->getMainScene();
    SE_Spatial* scroot = sc->getRoot();

    _setBoneState setBoneStateTravel;
    if(scroot)
    {
        scroot->travel(&setBoneStateTravel, true);
    }
}
