#include "SE_DynamicLibType.h"
#include "SE_Quat.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include "SE_Matrix.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
SE_BipedController::~SE_BipedController()
{
    std::vector<SE_Biped*>::iterator it1 = oneBipAnimation.begin();
    for(;it1 != oneBipAnimation.end(); ++it1)
    {
        if(*it1 != NULL)
        {
            delete *it1;
            *it1 = NULL;
        }
    }

    oneBipAnimation.clear();


    std::vector<SE_SkeletonUnit*>::iterator it2 = bipAndObjInfo.begin();
    for(;it2 != bipAndObjInfo.end(); ++it2)
    {
        if(*it2 != NULL)
        {
            delete *it2;
            *it2 = NULL;
        }
    }

    bipAndObjInfo.clear();

}

SE_SkeletonUnit::SE_SkeletonUnit()
{
    mBipNumPerVertext_max = 0;
    bipedNum = 0;
    vertexNum = 0;
}

SE_SkeletonUnit::~SE_SkeletonUnit()
{
    std::vector<SE_BipedWeight*>::iterator it = objVertexBlendInfo.begin();
    for(;it != objVertexBlendInfo.end(); ++it)
    {
        if(*it != NULL)
        {
            delete *it;
            *it = NULL;
        }
    }

    objVertexBlendInfo.clear();


    std::vector<_cache*>::iterator it1 = bipCache.begin();
    for(;it1 != bipCache.end(); ++it1)
    {
        if(*it1 != NULL)
        {
            delete *it1;
            *it1 = NULL;
        }
    }

    bipCache.clear();
}

SE_Biped::~SE_Biped()
{
    std::vector<SE_BipedKeyFrame*>::iterator it = animationInfo.begin();
    for(;it != animationInfo.end(); ++it)
    {
        if(*it != NULL)
        {
            delete *it;
            *it = NULL;
        }
    }

    animationInfo.clear();
}
void SE_BipedController::createBoneBaseMatrixInverse()
{
    mBindposeMatrixInverse.clear();
    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        SE_Matrix4f bp = oneBipAnimation[i]->bind_pose;

        mBindposeMatrixInverse.push_back(bp.inverse());
    }    
}
void SE_BipedController::createBoneToWorldMatrix(int frameindex)
{

    //per frame refresh;
    mAfterTransformMatrixToWorld.clear();    

    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        SE_Biped * bip = oneBipAnimation[i];

        SE_Matrix4f transform;        

        if(bip->animationInfo.size() == 0)
        {
            transform.identity();            
        }
        else
        {
            SE_Quat worldR = bip->animationInfo[frameindex]->rotateQ;
            SE_Vector3f worldT = bip->animationInfo[frameindex]->translate;
            SE_Vector3f worldS = bip->animationInfo[frameindex]->scale;
            
            //not use scale
            transform.set(worldR.toMatrix3f(),SE_Vector3f(1,1,1),worldT);//myTransform relate parent
        
        }

        SE_Matrix4f parentBoneToWorld;
        parentBoneToWorld.identity();


        if(bip->parent)
        {
            parentBoneToWorld = *(bip->parent->boneToWorldPerFrame[frameindex]);
        }
        else
        {
            //this bip is ROOT,the transform is world relative;
        }

        SE_Matrix4f tranToWorld = parentBoneToWorld.mul(transform);
        mAfterTransformMatrixToWorld.push_back(tranToWorld);
    }    
}

void SE_BipedController::finalTransformMatrix(int frameindex)
{
    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        //use local coordinate.
        SE_Matrix4f worldToModel = mOriginalModelToWorld.inverse();

        SE_Matrix4f finalMatrix = worldToModel.mul(mAfterTransformMatrixToWorld[i]).mul(mBindposeMatrixInverse[i]).mul(mOriginalModelToWorld);

        AllFrameFinalTransformMatrix[frameindex].push_back(finalMatrix);
    }


    for(int i = 0; i < AllFrameFinalTransformMatrix[frameindex].size(); ++i)
    {
        float f[16];
        
        SE_Matrix4f m = AllFrameFinalTransformMatrix[frameindex][i];        

        m.getColumnSequence(f);

        for(int j = 0; j < 16; ++j)
        {
            AllFrameFinalTransformToShader[frameindex].push_back(f[j]);
        }
    }        

}

void SE_BipedController::initSkeletonAnimation()
{
    if(mHasInit)
    {
        return;
    }
    mSkeletonNum = oneBipAnimation.size();

    int framecount = findMaxFrameIndex() + 1;

    fillFrame();

    fillBoneToWorldPerFrame();

    cacheBipindex();

    createBoneBaseMatrixInverse();

    AllFrameFinalTransformMatrix.clear();

    AllFrameFinalTransformMatrix.resize(framecount);
    AllFrameFinalTransformToShader.resize(framecount);
    for(int i = 0; i < framecount; ++i)
    {
        createBoneToWorldMatrix(i);
        finalTransformMatrix(i);
    }    

    mHasInit = true;
}

SE_Vector3f SE_BipedController::convert(int vertexIndex,int frameindex,const char * objname, const SE_Vector3f& v)
{
    SE_Vector4f input;
    input.set(v,1);
    SE_Vector4f result(0,0,0,0);

    SE_SkeletonUnit *su = findSU(objname);

    //bipedIndex.size is number that how many bips effact this vertext.
    int bipNumPerVertex = su->objVertexBlendInfo[vertexIndex]->bipedIndex.size();  

    //how many bips will take effact to one vertex
    for(int i = 0; i < bipNumPerVertex; ++i)
    {
        int bipIndex = su->objVertexBlendInfo[vertexIndex]->bipedIndex[i];//bipIndex is start from 1, not 0.

        int bipindexfromcache = su->bipCache[bipIndex-1]->bipIndexOnBipAnimation;

        SE_Biped *bip = oneBipAnimation[bipindexfromcache];// find bip from all bips

        if(bip->animationInfo.size() == 0)
        {
            continue;
        }

#ifdef _FORDEBUG
        SE_Matrix4f bindpos = bip->bind_pose;
        SE_Matrix4f inversOfbp = bindpos.inverse();

        SE_Quat worldR = bip->animationInfo[frameindex]->rotateQ;
        SE_Vector3f worldT = bip->animationInfo[frameindex]->translate;
        SE_Vector3f worldS = bip->animationInfo[frameindex]->scale;

        SE_Matrix4f transform;
        transform.identity();
        //not use scale
        transform.set(worldR.toMatrix3f(),SE_Vector3f(1,1,1),worldT);//myTransform relate parent        

        SE_Matrix4f parentBoneToWorld;
        parentBoneToWorld.identity();

        if(bip->parent)
        {
            parentBoneToWorld = *(bip->parent->boneToWorldPerFrame[frameindex]);
        }
        else
        {
            //this bip is ROOT,the transform is world relative;
        }        

        SE_Matrix4f m = parentBoneToWorld.mul(transform).mul(inversOfbp);

        SE_Matrix4f m = mAfterTransformMatrixToWorld[bipindexfromcache].mul(mBindposeMatrixInverse[bipindexfromcache]);
#else
        SE_Matrix4f m = AllFrameFinalTransformMatrix[frameindex][bipindexfromcache];
#endif

        result = result + m.map(input) * su->objVertexBlendInfo[vertexIndex]->weight[i];
    }
    return result.xyz();
}

void SE_BipedController::convert(int vertexIndex, int boneMatrixIndex,const char * objname)
{    
}

SE_Biped* SE_BipedController::findBiped(const char* bipname)
{
    std::vector<SE_Biped*>::iterator it;
    for(it = oneBipAnimation.begin(); it != oneBipAnimation.end(); it++)
    {
        SE_Biped* bip = *it;
        if(!strcmp(bip->bipName.c_str() , bipname))
        {
            return bip;
        }
    }
    return NULL;
}

int SE_BipedController::findBipedIndex(const char* bipname)
{
    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        SE_Biped* bip = oneBipAnimation[i];
        if(!strcmp(bip->bipName.c_str() , bipname))
        {
            return i;
        }
    }
    return -1;
}

SE_SkeletonUnit* SE_BipedController::findSU(const char* objname)
{
    std::vector<SE_SkeletonUnit*>::iterator it;
    for(it = bipAndObjInfo.begin(); it != bipAndObjInfo.end(); it++)
    {
        SE_SkeletonUnit* su = *it;
        if(!strcmp(su->objHasBiped.c_str() , objname))
        {
            return su;
        }
    }
    return NULL;
}

void SE_BipedController::cacheBipindex()
{

    for(int s = 0; s < bipAndObjInfo.size(); ++s)
    {
        SE_SkeletonUnit* su = bipAndObjInfo[s];

        for(int i = 0; i < su->bipedNamesOnObj.size(); ++i)
        {
            std::string bipname = su->bipedNamesOnObj[i];

            int index = findBipedIndex(bipname.c_str());
            if(index != -1)
            {
                SE_SkeletonUnit::_cache * cache = new SE_SkeletonUnit::_cache;
                cache->bipIndexOnObjInfo = i;
                cache->bipIndexOnBipAnimation = index;

                su->bipCache.push_back(cache);
            }
        }
    }
}

bool SE_BipedController::needInterpolation(int currFrameIndex,int nextFrameIndex)
{
    return (nextFrameIndex - currFrameIndex) > 1 ? true : false;
}

void SE_BipedController::interpolation(std::vector<SE_BipedKeyFrame*>::iterator &source,std::vector<SE_BipedKeyFrame*>::iterator &target,
                                       std::vector<SE_BipedKeyFrame*> &targetVector)
{
    SE_BipedKeyFrame* node_s = *source;
    SE_BipedKeyFrame* node_t = *target;

    //new frame counts,than push_back(node_s).For example:node_s = 3,node_t=0,
    //needGenerate 2 frames,then,push_back(node_s),now fullframe vector is 0,  1,2,  3
    int needGenerate = node_s->frameIndex - node_t->frameIndex - 1;

    for(int i = 0; i < needGenerate; ++i)
    {
        SE_Quat q_s = node_s->rotateQ;
        SE_Vector3f t_s = node_s->translate;

        SE_Quat q_t = node_t->rotateQ;
        SE_Vector3f t_t = node_t->translate;

        float t = (i+1.0) / (needGenerate+1.0);

        SE_Quat q_insert = SE_Quat::slerp(q_t,q_s,t);

        float x = SE_Lerp(t_t.x,t_s.x,t);
        float y = SE_Lerp(t_t.y,t_s.y,t);
        float z = SE_Lerp(t_t.z,t_s.z,t);

        SE_Vector3f t_insert(x,y,z);
        SE_Vector3f s_insert(1,1,1);//scale,not use

        SE_BipedKeyFrame* insertFrame = new SE_BipedKeyFrame();
        insertFrame->rotateQ = q_insert;
        insertFrame->translate = t_insert;
        insertFrame->scale = s_insert;
        insertFrame->frameIndex = node_t->frameIndex + i + 1;

        targetVector.push_back(insertFrame);
        target = targetVector.end();
        --target;
        
    }

    //push source node to target
    SE_BipedKeyFrame* insertSourceFrame = new SE_BipedKeyFrame();
    insertSourceFrame->rotateQ = node_s->rotateQ;
    insertSourceFrame->translate = node_s->translate;
    insertSourceFrame->scale = node_s->scale;
    insertSourceFrame->frameIndex = node_s->frameIndex;
    targetVector.push_back(insertSourceFrame);
    target = targetVector.end();
    --target;

    //don't increase source,next test will automatic increase

}

int SE_BipedController::findMaxFrameIndex()
{
    int lastIndex = -1;
    int maxFrame = 0;
    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        SE_Biped *bip = oneBipAnimation[i];

        if(bip->animationInfo.size() != 0)
        {
            lastIndex = bip->animationInfo.size() -1;

            //last frame index,total frame number per bip
            int lastFrameIndex = bip->animationInfo[lastIndex]->frameIndex;

            if(lastFrameIndex > maxFrame)
            {
                maxFrame = lastFrameIndex;
            }
        }
    }
    return maxFrame;
}


void SE_BipedController::fillFrame()
{
    int maxFrame = findMaxFrameIndex();    

    std::vector<SE_BipedKeyFrame*> fullFrame;
    //fullFrame.resize(maxFrame,NULL);

    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        //get bip one by one
        SE_Biped *bip = oneBipAnimation[i];
         
        if(bip->animationInfo.size() == 0)
        {
            continue;//this bip has no keyframe
        }

        //generate 0 frame, bind_pos relative parent
        SE_Matrix4f bindposT;
        if(bip->parent)
        {
            bindposT = (bip->parent->bind_pose.inverse()).mul(bip->bind_pose);            
        }
        else
        {
            //this bip is ROOT
            bindposT = bip->bind_pose;
        }

        SE_Quat rotate = bindposT.toMatrix3f().toQuat();//no rotate
        rotate = rotate.inverse();
        

        SE_Vector3f translate = bindposT.getTranslate();

        SE_Vector3f scale(1,1,1); //scale no use

        SE_BipedKeyFrame *zeroFrame = new SE_BipedKeyFrame();
        zeroFrame->frameIndex = 0;            

        zeroFrame->rotateQ = rotate;
        zeroFrame->scale = scale;
        zeroFrame->translate = translate;

        //push first (0) frame to target
        fullFrame.push_back(zeroFrame);

        
     
        std::vector<SE_BipedKeyFrame*>::iterator it_s = bip->animationInfo.begin();
        std::vector<SE_BipedKeyFrame*>::iterator it_t = fullFrame.end();
        -- it_t;

        SE_BipedKeyFrame* node_s = NULL;
        SE_BipedKeyFrame* node_t = NULL;
        
        while(it_s != bip->animationInfo.end())
        {            
            node_s = *it_s;
            node_t = *it_t;
            if(needInterpolation(node_t->frameIndex,node_s->frameIndex))
            {
                //interpolation
                interpolation(it_s,it_t,fullFrame);
            
            }
            else if(node_s->frameIndex == node_t->frameIndex)//current source frame index == target frame index
            {
                //goto next frame
                ++it_s;
            }
            else //source frame index - target frame index == 1
            {
                //push source frame to target frame vector
                SE_BipedKeyFrame *nextFrame = new SE_BipedKeyFrame();

                //current source node is just next frame
                nextFrame->frameIndex = node_s->frameIndex;
                nextFrame->rotateQ = node_s->rotateQ;
                nextFrame->translate = node_s->translate;
                nextFrame->scale = node_s->scale;

                fullFrame.push_back(nextFrame);
                it_t = fullFrame.end();
                --it_t;

            }
            
        }

        //total frame little than maxFrame?
        //1.yes fill last frame to vector
        
        int lastframe = bip->animationInfo.size() - 1;
        int lastframeIndex = bip->animationInfo[lastframe]->frameIndex;
        if(lastframeIndex < maxFrame)
        {
            SE_BipedKeyFrame * lastframenode = bip->animationInfo[lastframe];

            for(int needfill = 0; needfill < maxFrame - lastframeIndex; ++needfill)
            {
                SE_BipedKeyFrame *fillFrame = new SE_BipedKeyFrame();

                //next (maxFrame - lastframeIndex) frames are same 
                fillFrame->frameIndex = lastframenode->frameIndex + needfill + 1;
                fillFrame->rotateQ = lastframenode->rotateQ;
                fillFrame->translate = lastframenode->translate;
                fillFrame->scale = lastframenode->scale;

                fullFrame.push_back(fillFrame);

            }
        }

        //2.no,finish this biped,copy target to source,clear target
        for(it_s = bip->animationInfo.begin(); it_s != bip->animationInfo.end(); ++it_s)
        {
            if(*it_s != NULL)
            {
                delete *it_s;
            }
        }
        bip->animationInfo.clear();

        bip->animationInfo.resize(fullFrame.size());
        
        //copy target to source
        std::copy(fullFrame.begin(),fullFrame.end(),bip->animationInfo.begin());

        //clear fullFrame vector
        fullFrame.clear();        

    }

}

void SE_BipedController::fillBoneToWorldPerFrame()
{

    int maxFrameIndex = findMaxFrameIndex();

    for(int i = 0; i < oneBipAnimation.size(); ++i)
    {
        //get bip one by one
        SE_Biped *bip = oneBipAnimation[i];
        
        //fill bonetoworld for every frame
        for(int j = 0; j < maxFrameIndex + 1; ++j)
        {
            if(bip->parent)
            {
                SE_Matrix4f * m = new SE_Matrix4f();
                SE_Matrix4f myTrans;
                if(bip->animationInfo.size() > 0)
                {
                    //FIXME: the parent Allways has keyframe when child has keyframe
                    myTrans.set(bip->animationInfo[j]->rotateQ.toMatrix3f(),SE_Vector3f(1,1,1),bip->animationInfo[j]->translate);
                    *m = bip->parent->boneToWorldPerFrame[j]->mul(myTrans);
                }
                else
                {
                    //this bip on this frame has no key.
                    *m = bip->bind_pose;
                }
                bip->boneToWorldPerFrame.push_back(m);
            }
            else
            {
                //this bip is ROOT
                SE_Matrix4f * m = new SE_Matrix4f();
                *m = bip->bind_pose;
                bip->boneToWorldPerFrame.push_back(m);
            }

        }
       
    }

}

SE_SkeletonController::~SE_SkeletonController()
{
    for(int i = 0; i < mSkeletonController.size(); ++i)
    {
        delete mSkeletonController[i];
    }
}