#ifndef SE_BONEANIMATION_H
#define SE_BONEANIMATION_H
#include "SE_Animation.h"
#include "SE_Common.h"
class SE_SkinJointController;
class SE_Mesh;
class SE_SimObject;
class SE_ENTRY SE_BipedAnimation : public SE_Animation
{
public:
    SE_BipedAnimation();
    ~SE_BipedAnimation();
    void setSkinJointController(SE_BipedController* skinJointController)
    {
        mSkinJointController = skinJointController;
    }
    void setSimObject(SE_SimObject* simObject)
    {
        mSimObject = simObject;
    }
    static void generateBipedData();
public:
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode);
    virtual void onRun();
    virtual SE_Animation* clone();
    virtual void onEnd();
private:
    SE_BipedController* mSkinJointController;
    SE_Mesh* mMesh;  
    SE_SimObject* mSimObject;  
    _Vector3f* mVertex;
    int mVertexNum;

    std::vector<_Vector3f*> mSurfaceFaceVertexList;
    std::vector<int*> mSurfaceFaceVertexIndexList;

    void normalPlayMode(int frameIndex);

    void skinedShaderPlayMode(int frameIndex);

    //debug code    
    std::vector<SE_Vector3f *> mVertexWorldAfterBipTransform;
};
#endif
