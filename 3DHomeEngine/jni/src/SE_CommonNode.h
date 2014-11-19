#ifndef SE_COMMONNODE_H
#define SE_COMMONNODE_H
#include "SE_Spatial.h"
#include <list>
class SE_ENTRY SE_CommonNode : public SE_Spatial
{
    DECLARE_OBJECT(SE_CommonNode)
public:
	
    SE_CommonNode(SE_Spatial* parent = NULL);
    SE_CommonNode(SE_SpatialID id, SE_Spatial* parent = NULL);
    ~SE_CommonNode();
    void addChild(SE_Spatial* child);
    void removeChild(SE_Spatial* child);
    void updateWorldTransform();
    void updateBoundingVolume();
    void updateRenderState();
    void updateWorldLayer();
    int travel(SE_SpatialTravel* spatialTravel, bool travelAways);
    void renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType);
    virtual void write(SE_BufferOutput& output);
    virtual void writeEffect(SE_BufferOutput& output);
    virtual void read(SE_BufferInput& input);
    virtual void readEffect(SE_BufferInput& input);
    
    void unLoadSceneMustInvokeByCommand();
    int getLastestLayerInWorld();
	SPATIAL_TYPE getSpatialType();
    SE_Spatial* getSpatialByIndex(int index);
	const char * getGroupName()
	{
		return mGroupName.c_str();
	}

	void setGroupName(const char *groupname)
	{
		mGroupName = groupname;			
	}

	NodeTypes getNodeType()
	{
		return mType;
	}
	void setNodeType(NodeTypes type)
	{
		mType = type;
	}
	SE_Spatial * getGroupNode(const char *groupname,NodeTypes type);
    void setAlpha(float alpha);

    void replaceChildParent(SE_Spatial* parent);
    
public:
    virtual void showAllNode();
    virtual void hideAllNode();
    virtual SE_Spatial *clone(SE_Spatial* parent,  int index,bool createNewMesh = false,const char* statuslist = NULL);

    //set children render type
    virtual void setRenderType(const char* renderType);

    //set children shader type
    virtual void setShaderType(const char* shaderType);

    virtual void changeRenderState(int type,bool enable);

    virtual void setUseStencil(bool useStencil,const char* mirrorName,SE_MirrorPlane mirrorPlane = SE_XY,float translateAlignedAxis = 0.0);
    virtual void setChangeStencilPermit(bool hasChangePermit);

    virtual void setUserColor(SE_Vector3f color);
    virtual void setUseUserColor(int use);
    virtual void updateUsetRenderState();


    virtual void setNeedGenerateShadow(bool need);
    virtual void setNeedRenderShadow(bool need);
    
    virtual void setNeedBlackWhiteColor(bool need);
    

    virtual void setEnableCullFace(bool enable);

    //virtual void setNeedGenerateMirror(bool need);
    //virtual void setNeedRenderMirror(bool need);
    virtual void setMirrorInfo(const char* mirrorName,SE_MirrorPlane mirrorPlane = SE_XY);
    //virtual void setNeedLighting(bool need);
    virtual void removeLight(const char* lightName);
    virtual void removeAllLight();
    virtual void clearSpatialStatus();
    virtual void setNeedGenerateMipMap(bool need);
    //virtual void setNeedBlend(bool need);
    //virtual void setNeedAlphaTest(bool need);
    //virtual void setIsMiniBox(bool miniBox);
    virtual void forceUpdateBoundingVolume();
    virtual void autoUpdateBoundingVolume();
    //virtual void setNeedUVAnimation(bool need);
    virtual void setTexCoordOffset(SE_Vector2f offet);
    virtual void setNeedParticle(bool need);

    virtual void setSpatialState(unsigned int s);
    virtual void setEffectState(unsigned int s);
    virtual unsigned int getState();

    virtual void setShadowObjectVisibility(bool v);

    //virtual void setSelected(bool selected);

    //virtual void setTouchable(bool touchable);

    virtual void setTexCoordXYReverse(bool x,bool y);

    virtual int getChildrenNum();
    virtual SE_Spatial* getChildByIndex(int index);

    virtual std::string getChildrenStatus();
    virtual void clearMirror();
    virtual void setNeedBlendSortOnX(bool need);
    virtual void setNeedBlendSortOnY(bool need);
    virtual void setNeedBlendSortOnZ(bool need);

    virtual void setSpatialEffectAttribute(SE_SpatialAttribute::SpatialEffect effect,bool enableOrDisable);
    
    virtual void setSpatialStateAttribute(SE_SpatialAttribute::SpatialProperty spatialState,bool enableOrDisable);
    virtual void setSpatialRuntimeAttribute(SE_SpatialAttribute::SpatialRuntimeStatus effect,bool enableOrDisable);
    //shadow color should be from 0.001(black) to 0.1(white)
    virtual void setShadowColorDensity(float density);

    //shadow color should be from 0.001(black) to 0.1(white)
    virtual void setMirrorColorDesity(float density);

    /*virtual void setUserTranslate(const SE_Vector3f& translate);
    virtual void setUserRotate(const SE_Quat& rotate);
    virtual void setUserRotate(const SE_Matrix3f& rotate);
    virtual void setUserScale(const SE_Vector3f& scale);

    virtual void setUserTranslateIncremental(const SE_Vector3f& translate);
    virtual void setUserRotateIncremental(const SE_Quat& rotate);
    virtual void setUserRotateIncremental(const SE_Matrix3f& rotate);
    virtual void setUserScaleIncremental(const SE_Vector3f& scale);*/
protected:
	struct _Impl
	{
		std::list<SE_Spatial*> children;
		~_Impl()
		{
			std::list<SE_Spatial*>::iterator it;
			for(it = children.begin() ; it != children.end() ; it++)
			{
				delete *it;
			}
            children.clear();
		}
	};
    _Impl* mImpl;
private:
	NodeTypes mType;

    SE_Mutex mCommonNodeMutex;
};
#endif
