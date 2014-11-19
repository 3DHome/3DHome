#ifndef SE_BIPEDCONTROLLER_H
#define SE_BIPEDCONTROLLER_H
#include "SE_Matrix.h"
#include <string>
#include <vector>
#include<list>
#include "SE_ID.h"

class SE_Vector3f;
class SE_Quat;

class SE_ENTRY SE_BipedKeyFrame
{
public:
    int frameIndex;
    SE_Quat rotateQ;
    SE_Vector3f scale;
    SE_Vector3f translate;
};

class SE_ENTRY SE_Biped
{
public:
    ~SE_Biped();
    //all biped node will be free at BipedController
    std::string bipName;
    std::list<SE_Biped*> children;
    SE_Biped* parent;
    SE_Matrix4f bind_pose;
    SE_Biped()
    {
        parent = NULL;
    }

    std::vector<SE_BipedKeyFrame*> animationInfo; //SE_BipedKeyFrame* will be free automatic

    std::vector<SE_BipedKeyFrame*> boneToWorld;

    std::vector<SE_Matrix4f*> boneToWorldPerFrame;
};

class SE_ENTRY SE_BipedWeight
{
public:
    std::vector<int> bipedIndex; //The number of bip may not be one
    std::vector<float> weight; //same as above
};

class SE_ENTRY SE_SkeletonUnit
{
public:
    SE_SkeletonUnit();
    ~SE_SkeletonUnit();
    int bipedNum; //The number of bip to bind to the object
    int vertexNum;
    std::string objHasBiped;//which bip bind to this obj
    std::string controllerId;
    std::vector<std::string> bipedNamesOnObj;
    std::vector<SE_BipedWeight *> objVertexBlendInfo;//index,weight list on this obj

    struct _cache
    {
        int bipIndexOnObjInfo;
        int bipIndexOnBipAnimation;
    };

    std::vector<_cache *> bipCache;

    int mBipNumPerVertext_max;
   
};

class SE_ENTRY SE_BipedController 
    :public SE_SkinJointController
{
public:

    SE_BipedController()
    {
        mSkeletonNum = 0; 
        mHasInit = false;
    }

    virtual ~SE_BipedController();
    std::string mBipedControllerId;
    std::vector<SE_Biped*> oneBipAnimation;
    std::vector<SE_SkeletonUnit*> bipAndObjInfo;

    int getSkeletonNum()
    {
        return mSkeletonNum;
    }

    // v is the world coordinate,for normal play mode,objname: contorller hold all obj and biped info,use objname to find which obj animation is playing now
    virtual SE_Vector3f convert(int vertexIndex,int frameindex,const char * objname,const SE_Vector3f& v);

    //for skined shader play mode
    virtual void convert(int vertexIndex, int boneMatrixIndex,const char * objname);    

    void initSkeletonAnimation();
    
    int findMaxFrameIndex();
    
    SE_SkeletonUnit* findSU(const char* objname);    

    void setOriginalModelToWorld(SE_Matrix4f m)
    {
        mOriginalModelToWorld = m;
    }

    typedef std::vector<SE_Matrix4f> FinalTransformMatrixPerFrame;
    std::vector<FinalTransformMatrixPerFrame> AllFrameFinalTransformMatrix;

    typedef std::vector<float> FinalTransformToShaderPerFrame;
    std::vector<FinalTransformToShaderPerFrame> AllFrameFinalTransformToShader;

    
private:

    virtual void createBoneBaseMatrixInverse();
    void fillBoneToWorldPerFrame();
    void finalTransformMatrix(int frameindex);
    void cacheBipindex();
    void fillFrame();
    SE_Biped * findBiped(const char* bipname);
    int findBipedIndex(const char* bipname);
    bool needInterpolation(int currFrameIndex,int nextFrameIndex);
    void interpolation(std::vector<SE_BipedKeyFrame*>::iterator &source,std::vector<SE_BipedKeyFrame*>::iterator &target,std::vector<SE_BipedKeyFrame*> &targetVector);
    void createBoneToWorldMatrix(int frameindex);

    std::vector<SE_Matrix4f> mBindposeMatrixInverse;
    std::vector<SE_Matrix4f> mAfterTransformMatrixToWorld;
    int mSkeletonNum;
    SE_Matrix4f mOriginalModelToWorld;

    bool mHasInit;
    
};

class SE_ENTRY SE_SkeletonController
{
public:
    ~SE_SkeletonController();
    std::vector<SE_BipedController*> mSkeletonController;
};
#endif
