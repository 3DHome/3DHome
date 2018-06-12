#include "SE_DynamicLibType.h"
#include "SE_RenderState.h"
#ifdef GLES_20
    #include <GLES2/gl2.h>
#else
    #include <GLES/gl.h>
#endif
#include "SE_MemLeakDetector.h"
void SE_RenderState::apply()
{}
/////////////////////////////////
SE_DepthTestState::SE_DepthTestState()
{
	mDepthTestProperty = DEPTHTEST_ENABLE;
}
SE_DepthTestState::~SE_DepthTestState()
{}
void SE_DepthTestState::apply()
{
    if(mDepthTestProperty == DEPTHTEST_ENABLE)
	{
		glEnable(GL_DEPTH_TEST);
	}
	else
	{
		glDisable(GL_DEPTH_TEST);
	}
}
/////////////
static GLenum blendFactor[] = {GL_ZERO, GL_ONE, GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR, GL_DST_COLOR,
                     GL_ONE_MINUS_DST_COLOR, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                     GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA, GL_SRC_ALPHA_SATURATE,
                     GL_CONSTANT_COLOR, GL_ONE_MINUS_CONSTANT_COLOR, GL_CONSTANT_ALPHA,
                     GL_ONE_MINUS_CONSTANT_ALPHA};

SE_BlendState::SE_BlendState()
{
	mBlendProperty = BLEND_DISABLE;
	mBlendSrcFunc = SRC_ALPHA;
	mBlendDstFunc = ONE_MINUS_SRC_ALPHA;
}
SE_BlendState::~SE_BlendState()
{}
void SE_BlendState::setBlendFunc()
{
	glBlendFunc(blendFactor[mBlendSrcFunc], blendFactor[mBlendDstFunc]);
}
void SE_BlendState::apply()
{
	if(mBlendProperty == BLEND_ENABLE)
	{
		glEnable(GL_BLEND);
		setBlendFunc();
	}
	else
	{
		glDisable(GL_BLEND);
	}
}