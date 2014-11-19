#ifndef SE_RENDERUNIT_H
#define SE_RENDERUNIT_H
#include "SE_Vector.h"
#include "SE_Quat.h"
#include "SE_Matrix.h"
#include "SE_MaterialData.h"
#include "SE_ID.h"
#include "SE_Common.h"
#include "SE_ImageData.h"
#include "SE_Layer.h"
#include "SE_Spatial.h"
#include "SE_SimObject.h"
class SE_Surface;
class SE_Segment;
class SE_ImageData;
class SE_ShaderProgram;
class SE_RenderState;
class SE_RenderTarget;
class SE_ENTRY SE_RenderUnit
{
public:
    SE_RenderUnit();
    virtual ~SE_RenderUnit();
    //texIndex start from 0
    virtual void getTexImageID(int texIndex, SE_ImageDataID*& imageDataIDArray, int& imageDataIDNum);
    virtual void getTexImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum);
    virtual void getTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum);

    virtual void getVertex(_Vector3f*& vertex, int & vertexNum);
    virtual SE_MaterialData* getMaterialData();
    virtual SE_Vector3f getColor();
    virtual void draw(SE_RenderUnit* renderUnit);
    virtual SE_Surface* getSurface();
    virtual SE_ProgramDataID getShaderProgramID() const;
public:
    SE_PRIMITIVE_TYPE getPrimitiveType()
    {
        return mRenderableSpatial->getCurrentAttachedSimObj()->getPrimitiveType();
    }
    void setPrimitiveType(SE_PRIMITIVE_TYPE ptype)
    {
        mPrimitiveType = ptype;
    }
    void setLayer(const SE_Layer& layer)
    {
        mLayer = layer;
    }
    SE_Layer getLayer() const
    {
        //return *mRenderableSpatial->getWorldLayer();
        return mLayer;
    }
    void setWorldTransform(const SE_Matrix4f& m)
    {
        mWorldTransform = m;
    }
    SE_Matrix4f getWorldTransform()
    {
        return mWorldTransform;
    }
    void setViewToPerspectiveMatrix(const SE_Matrix4f& m)
    {
        mViewToPerspective = m;
    }
    SE_Matrix4f getViewToPerspectiveMatrix()
    {
        return mViewToPerspective;
    }
    void setName(const char* name)
    {
        mName = name;
    }
    const char* getName() const
    {
        return mName.c_str();
    }
    float getDistanceToCamera()
    {
        return mDistance;
    }
    void setDistanceToCamera(float f)
    {
        mDistance = f;
    }
    void setRenderState(RENDER_STATE_TYPE type, SE_RenderState* renderState, SE_OWN_TYPE own);
    
    void applyRenderState();
    void setDontUseVertexBuffer(bool use)
    {
        mDontUseVertexBuffer = use;
    }
    void setBlendState(bool enable);
    void setDepthState(bool enable);
    bool isEnableDepthTest();
    bool isEnableBlend();

    
   
    bool isDrawStencil();
    void setObjectIsDrawStencil(bool draw);

    void setEnableStencil(bool enable);
    bool isEnableStencil();

    void setDisableStencil(bool disable);
    bool isDisableStencil();
   
    void setRenderTarget(SE_RenderTarget* rt)
    {
        mRenderTarget = rt;
    }
    SE_RenderTarget* getRenderTarget()
    {
        return mRenderTarget;
    }

    void setSourceRenderTarget(SE_RenderTarget* rt)
    {
        mSourceRenderTarget = rt;
    }
    SE_RenderTarget* getSourceRenderTarget()
    {
        return mSourceRenderTarget;
    }

    virtual void reset();
   
    void setRenderTargetName(const char* rtname)
    {
        mRenderTargetName = rtname;
    }

    bool isUseFbo()
    {
        return mUseFbo;
    }

    void setIsUseFbo(bool use)
    {
        mUseFbo = use;
    }

    bool isRenderToFbo()
    {
        return mRenderToFbo;
    }

    void setRenderToFbo(bool render)
    {
        mRenderToFbo = render;
    }

    void setRenderToFboName(const char* name)
    {
        mRenderToFboName = name;
    }

    const char* getRenderToFboName()
    {
        return mRenderToFboName.c_str();
    }

    void setUsedFboName(const char* name)
    {
        mUsedFboName = name;
    }

    const char* getUsedFboName()
    {
        return mUsedFboName.c_str();
    }
   
     void setFboReplaceCurrentTexture(bool replace)
    {
        mFboReplaceCurrentTexture = replace;
    }

    bool isFboReplaceCurrentTexture()
    {
        return mFboReplaceCurrentTexture;
    }

    void setFboReplaceTextureIndex(int index)
    {
        mFboReplaceCurrentTextureIndex = index;
    }

    int getFboReplaceTextureIndex()
    {
        return mFboReplaceCurrentTextureIndex;
    }
   
    void setNeedGenerateShadow(bool need)
    {
        mNeedGenerateShadow = need;
    }

    bool isNeedGenerateShadow()
    {
        //return mRenderableSpatial->isNeedGenerateShadow();
        return mNeedGenerateShadow;
    }

    void setNeedRenderShadow(bool need)
    {
        mNeedRenderShadow = need;        
    }

    bool isNeedRenderShadow()
    {
        //return mRenderableSpatial->isSpatialEffectHasAttribute(SE_SpatialAttribute::SHADOWRENDER);
        return mNeedRenderShadow;
    }

    void setHelperCamera(SE_Camera* helper)
    {
        mShadowHelperCamera = helper;
    }

    SE_Camera* getHelperCamera()
    {
        return mShadowHelperCamera;
    }

    void setShaderName(const char* shadername)
    {
        mShaderName = shadername;
    }

    const char* getShaderName()
    {
        return mShaderName.c_str();
    }

    void setRenderName(const char* rendername)
    {
        mRenderName = rendername;
    }

    const char* getRenderName()
    {
        return mRenderName.c_str();
    }
    void setEnableCullFace(bool enable)
    {
        mEnableCullFace = enable;
    }
    bool isEnableCullFace()
    {
        return mEnableCullFace;
    }
   
    void setNeedGenerateReflection(bool use)
    {
        mNeedGenerateReflection = use;
    }
    bool isNeedGenerateReflection()
    {
        return mNeedGenerateReflection;
    }

    void setNeedDrawReflection(bool need)
    {
        mNeedDrawReflection = need;
    }
    bool isNeedDrawReflection()
    {
        return mNeedDrawReflection;
    }

    void setNeedGenerateMirror(bool use)
    {
        mNeedGenerateMirror = use;
    }
    bool isNeedGenerateMirror()
    {
        //return mRenderableSpatial->isNeedGenerateMirror();
        return mNeedGenerateMirror;
    }

    void setNeedRenderMirror(bool need)
    {
        mNeedRenderMirror = need;
    }
    bool isNeedRenderMirror()
    {
        //return mRenderableSpatial->isNeedRenderMirror();
        return mNeedRenderMirror;
    }
   
    void setNeedBlurHShadow(bool need)
    {
        mNeedBlurHShadow = need;
    }
    bool isNeedBlurHShadow()
    {
        return mNeedBlurHShadow;
    }  

    void setNeedBlurVShadow(bool need)
    {
        mNeedBlurVShadow = need;
    }
    bool isNeedBlurVShadow()
    {
        return mNeedBlurVShadow;
    } 

    void setNeedUseBluredShadowMap(bool need)
    {
        mNeedUseBluredShadowMap = need;
    }
    bool isNeedUseBluredShadow()
    {
        return mNeedUseBluredShadowMap;
    }
   
    void setMirrorObject(SE_Spatial* mirror)
    {
        mirrorObject = mirror;
    }

    SE_Spatial* getMirrorObject()
    {
        return mirrorObject;
    }

    void setMirrorPlan(SE_MirrorPlane plan)
    {
        mMirrorPlane = plan;
    }

    SE_MirrorPlane getMirrorPlan()
    {
        return mMirrorPlane;
    }

    void setAlpha(float alpha)
    {
        mAlpha = alpha;
    }

    float getAlpha()
    {
        return mAlpha;
    }
   
    void setTexCoordOffset(SE_Vector2f offset)
    {
        mTexCoordOffset = offset;
    }

    SE_Vector2f getTexCoordOffset()
    {
        return mTexCoordOffset;
    }

    virtual void setTexCoordXYReverse(SE_Vector2f reverse)
    {
        mTexCoordReverse = reverse;
    }
    SE_Vector2f getTexCoordXYReverse()
    {
        return  mTexCoordReverse;
    }
    void setNeedSpecLight(bool need)
    {
        mNeedSpecLight = need;
    }
    bool isNeedSpecLight()
    {
        return mRenderableSpatial->isNeedSpecLight();
        //return mNeedSpecLight;
    }
   
    void setIsAdditive(int is)
    {
        mIsForwardAdditive = is;
    }
    int isAdditive()
    {
        return mIsForwardAdditive;
    }
   
    void setRenderableSpatial(SE_Spatial* sp)
    {
        mRenderableSpatial = sp;
    }

    SE_Spatial* getRenderableSpatial()
    {
        return mRenderableSpatial;
    }
   
    void setNeedDownSample(bool need)
    {
        mNeedDownSample = need;
    }
    bool isNeedDownSample()
    {
        return mNeedDownSample;
    }

    void setNeedDofGen(bool need)
    {
        mNeedDofGen = need;
    }
    bool isNeedDofGen()
    {
        return mNeedDofGen;
    }

    void setNeedDrawQuad(bool need)
    {
        mNeedDrawQuad = need;
    }
    bool isNeedDrawQuad()
    {
        return mNeedDrawQuad;
    }
   
    float getLineWidth()
    {
        return mRenderableSpatial->getLineWidth();
    }
   
    void setNewImageKey(const char* key)
    {
        mNewImageKey = key;
    }

    const char* getNewImageKey()
    {
        return mNewImageKey.c_str();
    }
   
    void setNeedColorEffect(bool need)
    {
        mNeedDrawToColorEffect = need;
    }
    bool isNeedColorEffect()
    {
        return mNeedDrawToColorEffect;
    }
   
    void setMinBounding(SE_Vector3f min)
    {
        mMinBounding = min;
    }

    SE_Vector3f getMinBounding()
    {
        return mMinBounding;
    }

    void setMaxBounding(SE_Vector3f max)
    {
        mMaxBounding = max;
    }

    SE_Vector3f getMaxBounding()
    {
        return mMaxBounding;
    }
    
    //void loadTexture2D(int index, SE_ImageData* imageData, SE_WRAP_TYPE wrapS, SE_WRAP_TYPE wrapT, SE_SAMPLE_TYPE min, SE_SAMPLE_TYPE mag);
protected:
    SE_PRIMITIVE_TYPE mPrimitiveType;
    SE_Matrix4f mWorldTransform;
    SE_Matrix4f mViewToPerspective;
    SE_Layer mLayer;
    SE_PointerOwner<SE_RenderState> mRenderState[RENDERSTATE_NUM];
    int mHasTexCoord[SE_TEXUNIT_NUM];
    int mHasTexture[SE_TEXUNIT_NUM];    
    std::string mName;
    float mDistance;
    bool mDontUseVertexBuffer;
    bool mMirroredObject;
    bool mIsDrawStencil;

    bool mEnableStencil;//first draw stencil object will enable stencil,and last use stencil object will disable stencil buffer;
    bool mDisableStencil;
    SE_RenderTarget* mRenderTarget;
    SE_RenderTarget* mSourceRenderTarget;//down sample sample source
    std::string mRenderTargetName;
    bool mRenderToFbo;
    bool mUseFbo;
    std::string mRenderToFboName;
    std::string mUsedFboName;

    bool mFboReplaceCurrentTexture;//true:replace texture0. false:additional current texture list
    int mFboReplaceCurrentTextureIndex; //0-4
    //int mTexCoordIndex[SE_TEXUNIT_NUM];
    //int mColorBlendMode;
    
    bool mNeedGenerateShadow;
    bool mNeedRenderShadow;
    SE_Camera* mShadowHelperCamera;
    std::string mShaderName;
    std::string mRenderName;
    bool mEnableCullFace;

    bool mNeedGenerateReflection;
    bool mNeedDrawReflection;

    bool mNeedGenerateMirror;
    bool mNeedRenderMirror;

    bool mNeedBlurHShadow;
    bool mNeedBlurVShadow;

    bool mNeedUseBluredShadowMap;

    SE_Spatial* mirrorObject;
    SE_MirrorPlane mMirrorPlane;
    float mAlpha;
    bool mNeedSpecLight;

    SE_Vector2f mTexCoordOffset;

    SE_Vector2f mTexCoordReverse;

    int mIsForwardAdditive;

    SE_Spatial* mRenderableSpatial;

    bool mNeedDownSample;

    bool mNeedDofGen;

    bool mNeedDrawQuad;

    std::string mNewImageKey;

    bool mNeedDrawToColorEffect;

    SE_Vector3f mMinBounding;
    SE_Vector3f mMaxBounding;
};
class SE_ENTRY SE_TriSurfaceRenderUnit : public SE_RenderUnit
{
public:
    SE_TriSurfaceRenderUnit(SE_Surface* mesh = NULL);
    virtual ~SE_TriSurfaceRenderUnit();
    void getTexImageID(int texIndex, SE_ImageDataID*& imageDataIDArray, int& imageDataIDNum);
    void getTexImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum);
    void getTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum);

    virtual void getVertex(_Vector3f*& vertex, int & vertexNum);
    virtual SE_MaterialData* getMaterialData();
    virtual SE_Vector3f getColor();
    virtual void draw(SE_RenderUnit* renderUnit);
    virtual SE_Surface* getSurface();
    virtual SE_ProgramDataID getShaderProgramID() const;
private:
    //void setColorAndMaterial(SE_ShaderProgram* shaderProgram);
    //void setColor(SE_ShaderProgram* shaderProgram);
    //void setImage(int index , SE_ShaderProgram* shaderProgram);
    //void setImageAndColor(SE_ShaderProgram* shaderProgram);
    //void setVertex(SE_ShaderProgram* shaderProgram, _Vector3f*& vertex, int& vertexNum, int*& indexArray, int& indexNum);
    //void setTexVertex(SE_ShaderProgram* shaderProgram, int vertexNum);
    void setTexColorBlendMode(SE_ShaderProgram* shaderProgram);
    void getImageDataID(int texIndex, SE_ImageDataID*& imageDataIDArray, int& imageDataIDNum);
    void getImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum);
private:
    SE_Surface* mSurface;
    _Vector3f* mVertex;
    int mVertexNum;
    _Vector2f* mTexVertex;
    int mTexVertexNum;
};
class SE_ENTRY SE_LineSegRenderUnit : public SE_RenderUnit
{
public:
    SE_LineSegRenderUnit(SE_Segment* seg, int num, const SE_Vector3f& color);
    ~SE_LineSegRenderUnit();
    virtual void draw(SE_RenderUnit* renderUnit);
    SE_Vector3f getColor()
    {
        return mColor;
    }
    SE_Segment* getSegments()
    {
        return mSegments;
    }
    int getSegmentsNum()
    {
        return mSegmentNum;
    }
private:
    SE_Vector3f mColor;
    SE_Segment* mSegments;
    int mSegmentNum;
};

#endif
