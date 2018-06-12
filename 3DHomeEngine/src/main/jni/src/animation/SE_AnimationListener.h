#ifndef SE_ANIMATIONLISTENER_H
#define SE_ANIMATIONLISTENER_H

#include "SE_Common.h"
#include "SE_Vector.h"
#include "SE_Quat.h"
#include<vector>
class SE_Animation;
class SE_Spatial;
class SE_GeometryDataList;

class SE_Interpolate;

class SE_AnimationListener
{
public:
    SE_AnimationListener()
    {
        mInterpolate = NULL;
        mStartFrame = 0;
        mEndFrame = 0;
    }
    virtual ~SE_AnimationListener()
    {
        if(mInterpolate)
        {
            delete mInterpolate;
        }
    }   

    virtual void onPrepare(SE_Animation* animation,int index = 0); //init something,here is not first frame
    virtual void onEnd(SE_Animation* animation){}
    virtual void onPause(SE_Animation* animation){}
    virtual void onRestore(SE_Animation* animation){}
    virtual void onBeforePerFrame(SE_Animation* animation){}//do something before every frame
    virtual void onAfterPerFrame(SE_Animation* animation){}//do something after every frame    
    virtual void onUpdateFrame(SE_Animation* animation){}//update one frame,move spatial,rotate object,etc.
    virtual bool inflate(const char* xmlPath){return false;}
    virtual void setAnimationSpatial(SE_Spatial* sp)
    {
        mAnimationSpatial = sp;
    }
    SE_Spatial* getAnimationSpatial()
    {
        return mAnimationSpatial;
    }

    virtual void setAnimationLeaderSpatial(SE_Spatial* sp)
    {
        mAnimationLeaderSpatial = sp;
    }
    SE_Spatial* getAnimationLeaderSpatial()
    {
        return mAnimationLeaderSpatial;
    }

    virtual void setInterpolate(SE_Interpolate* i)
    {
        mInterpolate = i;
    }
    virtual SE_Interpolate* getInterpolate()
    {
        return mInterpolate;
    }

    virtual void setListenerStartPoint(int start)
    {
        mStartFrame = start;
    }

    virtual void setListenerEndPoint(int end)
    {
        mEndFrame = end;
    }

    //maybe no useful
    virtual void onBeforFrame(int frameIndex,SE_Animation* animation){}
    virtual void onAfterFrame(int frameIndex,SE_Animation* animation){}
    virtual void resteFrame(){}
    virtual void changeStatus(bool changeStart){}
    virtual void saveInit(){}
protected:
    SE_Interpolate* mInterpolate;
    SE_Spatial* mAnimationSpatial;
    SE_Spatial* mAnimationLeaderSpatial;
    int mStartFrame;//which frame this listener start,if startFrame == endFrame,interpolate will use animation frameNum
    int mEndFrame;//which frame this listener end,if startFrame == endFrame,interpolate will use animation frameNum
};

class SE_MeshAnimationListener :public SE_AnimationListener
{
public:
    SE_MeshAnimationListener(SE_Spatial* sp = NULL);
    virtual ~SE_MeshAnimationListener();   

    virtual void onPrepare(SE_Animation* animation,int index = 0);
    virtual void onEnd(SE_Animation* animation);
    virtual void onUpdateFrame(SE_Animation* animation);
    virtual bool inflate(const char* xmlPath);
private:
    bool mInitOK;    
    SE_GeometryDataList* mGeoDataList;
};

class SE_RigidAnimationListener :public SE_AnimationListener
    {
public:    
    SE_RigidAnimationListener(SE_Spatial* sp = NULL);
    ~SE_RigidAnimationListener();

    virtual void onPrepare(SE_Animation* animation,int index = 0);
    virtual void onEnd(SE_Animation* animation);
    virtual void onUpdateFrame(SE_Animation* animation);
    virtual bool inflate(const char* xmlPath);
    virtual void resteFrame();
    virtual void changeStatus(bool changeStart);
     virtual void saveInit();
protected:
    bool mInitOK;    

    SE_Vector3f mInitPos;
    SE_Vector3f mStartPos;
    SE_Vector3f mEndPos;
    SE_Vector3f mBackupStartPos;
    SE_Vector3f mBackupEndPos;


    SE_Vector3f mInitScale;
    SE_Vector3f mStartScale;
    SE_Vector3f mEndScale;

    SE_Vector3f mBackupStartScale;
    SE_Vector3f mBackupEndScale;



    SE_Vector4f mInitRotate;
    SE_Vector3f mAxis;
    float mStartDegree;
    float mEndDegree;
    float mBackupStartDegree;
    float mBackupEndDegree;

    float mInitAlpha;
    float mStartAlpha;
    float mEndAlpha;
    float mBackupStartAlpha;
    float mBackupEndAlpha;

};

class SE_RigidAnimationRotateToZeroListener :public SE_RigidAnimationListener
{    
public:    
    SE_RigidAnimationRotateToZeroListener(SE_Spatial* sp = NULL);
    ~SE_RigidAnimationRotateToZeroListener();
    virtual void onPrepare(SE_Animation* animation,int index = 0);
    virtual void onEnd(SE_Animation* animation);
    
};

class SE_RigidAnimationFollowerListener :public SE_RigidAnimationListener
{    
public:    
    SE_RigidAnimationFollowerListener(SE_Spatial* sp = NULL);
    ~SE_RigidAnimationFollowerListener();
    virtual void onPrepare(SE_Animation* animation,int index = 0);   
    
};
class SE_AnimationListenerFactory
{
public:
    static std::vector<SE_AnimationListener*> createListener(const char* xmlPath);
    static SE_AnimationListener* createListener(ANIMATION_LISTENER_TYPE mode);
};
#endif