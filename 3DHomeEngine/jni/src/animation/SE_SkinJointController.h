#ifndef SE_SKINJOINTCONTROLLER_H
#define SE_SKINJOINTCONTROLLER_H
#include "SE_Matrix.h"
#include <string>
#include <vector>
class SE_Bone;
class SE_Vector3f;
class SE_ENTRY SE_BoneWeight
{
public:
    int boneIndex;
    float weight;
    SE_BoneWeight(int index = -1, float w = 0)
    {
        boneIndex = index;
        weight = w;
    }
};
class SE_ENTRY SE_SkinJointController
{
public:
virtual ~SE_SkinJointController()
{}
    // v is the world coordinate,for normal play mode
    virtual SE_Vector3f convert(int vertexIndex, int boneMatrixIndex,const SE_Vector3f& v);

    //for skined shader play mode
    virtual void convert(int vertexIndex, int boneMatrixIndex);

    virtual void createBoneBaseMatrixInverse();
    virtual void createBoneToWorldMatrix(int boneMatrixIndex);
public:
    typedef std::vector<SE_BoneWeight> BoneWeightVector;
    std::vector<SE_Bone*> mBoneVector;
    std::string mMeshName;
    std::vector<BoneWeightVector> mVertexBoneWeightInfo;
    std::vector<SE_Matrix4f> mBoneToWorldMatrix;
    std::vector<SE_Matrix4f> mBoneBaseMatrixInverse;

    //debug code
    std::vector<float> mWeightArray;
    std::vector<float> mBoneIndexArray;
    std::vector<float> mBoneMatricesPerFrame;
    std::vector<float> mInversMatricesPerFrame;
};
#endif
