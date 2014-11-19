#include "SE_DynamicLibType.h"
#include "SE_SkinJointController.h"
#include "SE_Matrix.h"
#include "SE_Bone.h"
#include "SE_MemLeakDetector.h"
void SE_SkinJointController::createBoneBaseMatrixInverse()
{
    mBoneBaseMatrixInverse.resize(mBoneVector.size());
    for(int i = 0 ; i < mBoneVector.size() ; i++)
    {
        SE_Bone* bone = mBoneVector[i];
        SE_Matrix4f boneBaseMatrix = bone->getBaseMatrix();
        SE_Bone* parent = bone->getParent();
        while(parent)
        {
            SE_Matrix4f parentBaseMatrix = parent->getBaseMatrix();
            boneBaseMatrix = parentBaseMatrix.mul(boneBaseMatrix);
            parent = parent->getParent();
        }
        mBoneBaseMatrixInverse[i] = boneBaseMatrix.inverse();

        float a[16];
        mBoneBaseMatrixInverse[i].getColumnSequence(a);
        for(int j = 0; j < 16; ++j)
        {
            mInversMatricesPerFrame.push_back(a[j]);
        }
    }
}
void SE_SkinJointController::createBoneToWorldMatrix(int boneMatrixIndex)
{
    mBoneMatricesPerFrame.clear();
    mWeightArray.clear();
    mBoneIndexArray.clear();


    if(mBoneToWorldMatrix.size() == 0)
        mBoneToWorldMatrix.resize(mBoneVector.size());
    for(int i = 0 ; i < mBoneVector.size() ; i++)
    {
        SE_Bone* bone = mBoneVector[i];
        SE_Bone* parent = bone->getParent();
        SE_Matrix4f boneToWorld = bone->getMatrix(boneMatrixIndex);
        while(parent)
        {
            SE_Matrix4f parentBoneToWorld = parent->getMatrix(boneMatrixIndex);
            boneToWorld = parentBoneToWorld.mul(boneToWorld);
            parent = parent->getParent();
        }
        mBoneToWorldMatrix[i] = boneToWorld;

        float a[16];

        mBoneToWorldMatrix[i].getColumnSequence(a);
        for(int j = 0; j < 16; ++j)
        {
            mBoneMatricesPerFrame.push_back(a[j]);
        }
    }
}
SE_Vector3f SE_SkinJointController::convert(int vertexIndex, int boneMatrixIndex, const SE_Vector3f& v)
{
    BoneWeightVector bwv = mVertexBoneWeightInfo[vertexIndex];
    BoneWeightVector::iterator it;
    SE_Vector4f result(0, 0, 0, 0);
    SE_Vector4f inputV(v.x, v.y, v.z, 1.0);

    for(it = bwv.begin() ; it != bwv.end() ; it++)
    {
        int boneIndex = it->boneIndex;
        float weight = it->weight;
        SE_Bone* bone = mBoneVector[boneIndex - 1];
        /*
        SE_Matrix4f boneBaseMatrix = bone->getBaseMatrix();
        SE_Matrix4f worldToBone;
        SE_Matrix4f boneToWorld;
        if(boneMatrixIndex == -1)
        {
            boneToWorld = bone->getBaseMatrix();
        }
        else
        {
            boneToWorld = bone->getMatrix(boneMatrixIndex);
        }
        SE_Bone* parent = bone->getParent();
        while(parent != NULL)
        {
            SE_Matrix4f parentBoneMatrix;
            if(boneMatrixIndex == -1)
                parentBoneMatrix = parent->getBaseMatrix();
            else
                parentBoneMatrix = parent->getMatrix(boneMatrixIndex);
            boneToWorld = parentBoneMatrix.mul(boneToWorld);
            SE_Matrix4f parentBaseBoneMatrix = parent->getBaseMatrix();
            boneBaseMatrix = parentBaseBoneMatrix.mul(boneBaseMatrix);
            parent = parent->getParent();
        }
        SE_Matrix4f boneBaseMatrixInverse = boneBaseMatrix.inverse();
        SE_Matrix4f m = boneToWorld.mul(boneBaseMatrixInverse);
        */
        SE_Matrix4f boneBaseMatrixInverse = mBoneBaseMatrixInverse[boneIndex - 1];
        SE_Matrix4f boneToWorld = mBoneToWorldMatrix[boneIndex - 1];
        SE_Matrix4f m = boneToWorld.mul(boneBaseMatrixInverse);
        result = result + m.map(inputV) * weight;
    }
    return result.xyz();
}

void SE_SkinJointController::convert(int vertexIndex, int boneMatrixIndex)
{
    BoneWeightVector bwv = mVertexBoneWeightInfo[vertexIndex];
    BoneWeightVector::iterator it;

    //debug code
    it = bwv.begin() ;
    for(int i = 0; i < 4; ++i)
    {
        if(it != bwv.end())
        {
            mWeightArray.push_back(it->weight);
            mBoneIndexArray.push_back(it->boneIndex - 1);
            it++;
        }
        else
        {
            mWeightArray.push_back(0);
            mBoneIndexArray.push_back(255); // 255 means no bone attach this point
        }
    
    }

}
