#ifndef SE_RENDERMANAGER_H
#define SE_RENDERMANAGER_H
#include "SE_Matrix.h"
#include "SE_Vector.h"
#include "SE_ID.h"
#include <list>
#include <vector>
#include <map>
class SE_RenderUnit;
class SE_RenderTarget;
class SE_Camera;
class SE_Spatial;
class SE_ENTRY SE_RenderManager
{
public:
    enum RENDER_QUEUE {RQ0, RQ1, RQ2, RQ3, RQ4, RQ5, RQ6, RQ7,RQ8,RQ9,RQ10,RQ11,RQ12,RQ13,RQ14,RQ15,RQ_NUM};
    enum {RENDERTARGET_SIZE = 8};
	enum RENDER_SORT_TYPE
	{
		SORT_BY_RESOURCE,
		SORT_BY_DISTANCE
	};
    SE_RenderManager();
    ~SE_RenderManager();
    void beginDraw();
    void check();
    void endDraw();
    void draw();
    void sort();
    void addRenderUnit(SE_RenderUnit* ru, RENDER_QUEUE rq);
    
    
    bool setCurrentScene(int index)
    {
        if(index < 0 || index >= SE_MAX_SCENE_SIZE)
            return false;
        mCurrentScene = index;
        return true;
    }
    int getCurrentScene() const
    {
        return mCurrentScene;
    }
    //void setCurrentRenderTarget(SE_RenderTarget* renderTarget);
    //SE_RenderTarget* getCurrentRenderTarget() const;

    void setCurrentSceneNeedClear(bool need);
    bool isCurrentSceneNeedClear() const;

    void setCurrentCamera(SE_Camera* camera);
    SE_Camera* getCurrentCamera() const;
    void setCurrentHelperCamera(SE_Camera* camera);
    SE_Camera* getCurrentHelperCamera() const;
    void setCurrentBackgroundColor(const SE_Vector4f& c);
    SE_Vector4f getCurrentBackgroundColor() const;
	void setRenderSortType(RENDER_SORT_TYPE t)
	{
		mRenderSortType = t;
	}
	RENDER_SORT_TYPE getRenderSortType() const
	{
		return mRenderSortType;
	}

    void setNeedBlurShadow(bool need)
    {
        mNeedBlurShadow = need;
    }
    bool isNeedBlurShadow()
    {
        return mNeedBlurShadow;
    }

    void insertMirrorGenerator(SE_Spatial* spatial);
    void insertShadowGenerator(SE_Spatial* spatial);
    void removeMirrorGenerator(SE_Spatial* spatial);
    void removeShadowGenerator(SE_Spatial* spatial);
    void removeAllMirrorGenerator();
    void removeAllShadowGenerator();

    int mirrorGeneratorCount()
    {
        return mMirrorGeneratorCount.size();
    }

    int shadowGeneratorCount()
    {
        return mShadowGeneratorCount.size();
    }
    void clearSpatialStatus(SE_Spatial* spatial);

    void shadowCreatorTransformed()
    {
        ++mTransformedShadowCreatorNum;
    }

    void mirrorCreatorTransformed()
    {
        ++mTransformedMirrorCreatorNum;
    }

    void clearRenderTargetCreatorTransform()
    {
        mTransformedShadowCreatorNum = 0;
        mTransformedMirrorCreatorNum = 0;
    }


    bool isNeedUpdateShadowMap()
    {
        return mTransformedShadowCreatorNum > 0 ? true : false; 
    }

    bool isNeedUpdateMirrorMap()
    {
        return mTransformedMirrorCreatorNum > 0 ? true : false; 
    }


    void mirrorRenderChange(SE_Spatial* mirrorRender);
private:
    void drawMirrorGenerator();
    void drawShadowGenerator();

    SE_RenderTarget* createShadowTarget(int type);
    SE_RenderTarget* createMirrorTarget(const char* targetName);

private:
    typedef std::list<SE_RenderUnit*> RenderUnitList;
    struct _SceneUnit
    {
        //RenderUnitList mRenderQueue[RQ_NUM];

        std::vector<RenderUnitList> mRenderQueue;
        //SE_RenderTarget* mRenderTarget;
        bool mNeedClear;
        SE_Camera* mCamera;
        SE_Camera* mHelperCamera;
        SE_Vector4f mBgColor;
        _SceneUnit()
        {
            /*
            for(int i = 0 ; i < RQ_NUM ; i++)
            {
                mRenderQueue[i] = new RenderUnitList;
            }
            */
            //mRenderTarget = NULL;
            mNeedClear = false;
            mCamera = NULL;
            mHelperCamera = NULL;
            mRenderQueue.resize(RQ_NUM);
        }
    };
    //std::list<_RenderTargetUnit*> mRenderTargetList;
    
private:
    //_RenderTargetUnit* findTarget(const SE_RenderTargetID& id);
    /*
    static bool CompareRenderTarget(_SceneUnit* first, _RenderTargetUnit* second)
    {
        if(first->mRenderTargetID < second->mRenderTargetID)
            return true;
        else
            return false;
    }
    */
    void clear();
   
private:
    class _MirrorGroupElement
    {
    public:
        std::string mGroupName;
        std::vector<SE_RenderUnit*> mRuList;

    };
    _SceneUnit mSceneUnits[SE_MAX_SCENE_SIZE];

    
    int mCurrentScene;
	RENDER_SORT_TYPE mRenderSortType;
    bool mNeedBlurShadow;
    std::vector<_MirrorGroupElement*> mMirrorGroup;//for multi mirror
    
    RenderUnitList mMirrorRUObjectList;
    RenderUnitList mWallShadowRUObjectList;
    RenderUnitList mGroundShadowRUObjectList;

    

    typedef std::list<SE_Spatial*> SpatialList;
    SpatialList mMirrorGeneratorCount;

    SpatialList mShadowGeneratorCount;
    int mTransformedShadowCreatorNum;
    int mTransformedMirrorCreatorNum;

};
#endif
