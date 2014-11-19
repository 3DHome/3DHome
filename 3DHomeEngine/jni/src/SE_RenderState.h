#ifndef SE_RENDERSTATE_H
#define SE_RENDERSTATE_H
#include "SE_RefBase.h"
class SE_ENTRY SE_RenderState : public SE_RefBase
{
public:
	virtual ~SE_RenderState() {}
	virtual void apply();
};
class SE_ENTRY SE_DepthTestState : public SE_RenderState
{
public:
	enum DEPTHTEST_PROPERTY {DEPTHTEST_ENABLE, DEPTHTEST_DISABLE};
	SE_DepthTestState();
	~SE_DepthTestState();
	void apply();
	void setDepthTestProperty(DEPTHTEST_PROPERTY depthTest)
	{
		mDepthTestProperty = depthTest;
	}
	DEPTHTEST_PROPERTY getDepthTestProperty()
	{
		return mDepthTestProperty;
	}
private:
    DEPTHTEST_PROPERTY mDepthTestProperty;
};
class SE_ENTRY SE_BlendState : public SE_RenderState
{
public:
	enum BLENDSTATE_PROPERTY {BLEND_ENABLE, BLEND_DISABLE};
    enum BLEND_FUNC {ZERO, ONE, SRC_COLOR,ONE_MINUS_SRC_COLOR,DST_COLOR,
                     ONE_MINUS_DST_COLOR, SRC_ALPHA, ONE_MINUS_SRC_ALPHA,
                     DST_ALPHA, ONE_MINUS_DST_ALPHA, SRC_ALPHA_SATURATE,
                     CONSTANT_COLOR, ONE_MINUS_CONSTANT_COLOR, CONSTANT_ALPHA,
                     ONE_MINUS_CONSTANT_ALPHA};
	SE_BlendState();
	~SE_BlendState();
	void apply();
	void setBlendProperty(BLENDSTATE_PROPERTY bp)
	{
		mBlendProperty = bp;
	}
	BLENDSTATE_PROPERTY getBlendProperty()
	{
		return mBlendProperty;
	}
    void setBlendSrcFunc(BLEND_FUNC bsf)
    {
        mBlendSrcFunc = bsf;
    }
    BLEND_FUNC getBlendSrcFunc()
    {
        return mBlendSrcFunc;
    }
    void setBlendDstFunc(BLEND_FUNC bdf)
    {
        mBlendDstFunc = bdf;
    }
    BLEND_FUNC getBlendDstFunc()
    {
        return mBlendDstFunc;
    }
private:
	void setBlendFunc();
private:
	BLENDSTATE_PROPERTY mBlendProperty;
    BLEND_FUNC mBlendSrcFunc;
    BLEND_FUNC mBlendDstFunc;
};
#endif
