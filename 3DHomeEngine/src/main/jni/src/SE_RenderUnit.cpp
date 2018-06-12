#include "SE_DynamicLibType.h"
#include "SE_RenderUnit.h"
#include "SE_Mesh.h"
#include "SE_ResourceManager.h"
#include "SE_Application.h"
#include "SE_ImageData.h"
#include "SE_Log.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"
#include "SE_ID.h"
#include "SE_ShaderProgram.h"
#include "SE_Geometry3D.h"
#include "SE_Spatial.h"
#include "SE_Renderer.h"
#include "SE_DataValueDefine.h"
#include <vector>
#include "SE_VertexBuffer.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include "SE_MemLeakDetector.h"
/////////////////////////////////
SE_RenderUnit::SE_RenderUnit()
{
    memset(mHasTexCoord, 0, sizeof(int) * SE_TEXUNIT_NUM);
    memset(mHasTexture, 0, sizeof(int) * SE_TEXUNIT_NUM);
    mDistance = 0;
    //memset(mTexCoordIndex, 0, sizeof(int) * SE_TEXUNIT_NUM);
    //mColorBlendMode = SE_TEXTURE0_MODE;
    mDontUseVertexBuffer = false;
    mMirroredObject = false;
    mIsDrawStencil = false;
    mEnableStencil = false;
    mDisableStencil = false;
    mRenderTarget = NULL;
    mRenderToFbo = false;
    mUseFbo = false;
    mFboReplaceCurrentTexture = false;
    mFboReplaceCurrentTextureIndex = 0;//default replace texture0,if mFboReplaceCurrentTexture is true

    mShadowHelperCamera = NULL;
    mNeedGenerateShadow = false;
    mNeedRenderShadow = false;
    mEnableCullFace = false;

    mNeedGenerateReflection = false;
    mNeedDrawReflection = false;

    mNeedGenerateMirror = false;
    mNeedRenderMirror = false;

    mNeedBlurHShadow = false;
    mNeedBlurVShadow = false;

    mNeedUseBluredShadowMap = false;

    mirrorObject = NULL;
    mNeedSpecLight = false;

    mIsForwardAdditive = 0;

    mRenderableSpatial = NULL;

    mNeedDownSample = false;
    mNeedDofGen = false;
    mNeedDrawQuad =false;

    mNeedDrawToColorEffect = false;
    mSourceRenderTarget = NULL;
    mAlpha = 1.0;

}
SE_RenderUnit::~SE_RenderUnit()
{}
void SE_RenderUnit::getTexImageID(int texIndex, SE_ImageDataID*& imageDataIDArray, int& imageDataIDNum)
{
}
void SE_RenderUnit::getTexImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum)
{}
void SE_RenderUnit::getVertex(_Vector3f*& vertex, int & vertexNum)
{
    vertex = NULL;
    vertexNum = 0;
}
void SE_RenderUnit::getTexVertex(int texIndex, _Vector2f*& texVertex, int& texVertexNum)
{
    texVertex = NULL;
    texVertexNum = 0;
}
SE_Surface* SE_RenderUnit::getSurface()
{
    return NULL;
}
SE_MaterialData* SE_RenderUnit::getMaterialData()
{
    return 0;
}
SE_Vector3f SE_RenderUnit::getColor()
{
    return SE_Vector3f(0, 0, 0);
}
SE_ProgramDataID SE_RenderUnit::getShaderProgramID() const
{
    return SE_ProgramDataID("");
}
void SE_RenderUnit::setRenderState(RENDER_STATE_TYPE type, SE_RenderState* renderState, SE_OWN_TYPE own)
{
    if(type < 0 || type >= RENDERSTATE_NUM)
        return;
    SE_PointerOwner<SE_RenderState>* p = &mRenderState[type];
    if(p->own == OWN && p->ptr)
        delete p->ptr;
    p->own = own;
    p->ptr = renderState;
}
void SE_RenderUnit::applyRenderState()
{
    if(!mRenderState)
        return;
    for(int i = 0 ; i < RENDERSTATE_NUM ; i++)
    {
        SE_RenderState* rs = mRenderState[i].ptr;
        if(rs)
            rs->apply();
    }
}
void SE_RenderUnit::draw(SE_RenderUnit* renderUnit)
{
	SE_Application::getInstance()->getStatistics().addSurfaceNum(1);
	SE_Application::getInstance()->getStatistics().setCurrentRenderUnit(mName.c_str());
}
#ifdef DEBUG0
static int texSize = 0;
#endif


void SE_RenderUnit::setBlendState(bool enable)
{
    SE_BlendState *rs_blend = (SE_BlendState *)(mRenderState[1].ptr);

    if(rs_blend)
        {
            if(enable)
            {
            rs_blend->setBlendProperty(SE_BlendState::BLEND_ENABLE);
            }
            else
            {
            rs_blend->setBlendProperty(SE_BlendState::BLEND_DISABLE);
        }
            }
        }
        
void SE_RenderUnit::setDepthState(bool enable)
{
    SE_DepthTestState *rs_depth = (SE_DepthTestState *)(mRenderState[0].ptr);

    if(rs_depth)
    {
        if(enable)
        {
            rs_depth->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_ENABLE);
        }
        else
        {
            rs_depth->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
        }
        }
}
        

bool SE_RenderUnit::isEnableDepthTest()
{

    SE_DepthTestState *rs_depth = (SE_DepthTestState *)(mRenderState[0].ptr);
    if(rs_depth)
    {
        return rs_depth->getDepthTestProperty() == SE_DepthTestState::DEPTHTEST_ENABLE;
    }
    return false;
    }

bool SE_RenderUnit::isEnableBlend()
    {
        SE_BlendState *rs_blend = (SE_BlendState *)(mRenderState[1].ptr);
        if(rs_blend)
        {
        return rs_blend->getBlendProperty() == SE_BlendState::BLEND_ENABLE;
    }    
    return false;
}
void SE_RenderUnit::setObjectIsDrawStencil(bool draw)
{
    mIsDrawStencil = draw;
}

bool SE_RenderUnit::isDrawStencil()
{
    return mIsDrawStencil;
}

void SE_RenderUnit::setEnableStencil(bool enable)
{
    mEnableStencil = enable;
}

bool SE_RenderUnit::isEnableStencil()
{
    return mEnableStencil;
}

void SE_RenderUnit::setDisableStencil(bool disable)
{
    mDisableStencil = disable;
}

bool SE_RenderUnit::isDisableStencil()
{
    return mDisableStencil;
}

void SE_RenderUnit::reset()
{    
    mDontUseVertexBuffer = false;
    mMirroredObject = false;
    mIsDrawStencil = false;
    mEnableStencil = false;
    mDisableStencil = false;
    mRenderToFbo = false;
    mUseFbo = false;
    mFboReplaceCurrentTexture = false;
    mFboReplaceCurrentTextureIndex = 0;//default replace texture0,if mFboReplaceCurrentTexture is true
    mNeedGenerateShadow = false;
    mNeedRenderShadow = false;
    mEnableCullFace = false;

    mNeedGenerateReflection = false;
    mNeedDrawReflection = false;

    mNeedGenerateMirror = false;
    mNeedRenderMirror = false;

    mNeedBlurHShadow = false;
    mNeedBlurVShadow = false;
    mNeedUseBluredShadowMap = false;
    mirrorObject = NULL;
    mNeedSpecLight= false;
    mIsForwardAdditive = 0;
    mRenderableSpatial = NULL;

    mNeedDownSample = false;
    mNeedDofGen = false;
    mNeedDrawQuad = false;
    mNeedDrawToColorEffect = false;
    mRenderTarget = NULL;
    mSourceRenderTarget = NULL;
    mAlpha = 1.0;
}

////////////////////////////////
SE_TriSurfaceRenderUnit::SE_TriSurfaceRenderUnit(SE_Surface* surface)
{
    mSurface = surface;
    mVertex = NULL;
    mTexVertex = NULL;
    mVertexNum = 0;
    mTexVertexNum = 0;
    mPrimitiveType = TRIANGLES;
}
SE_Surface* SE_TriSurfaceRenderUnit::getSurface()
{
    return mSurface;
}
void SE_TriSurfaceRenderUnit::getImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum)
{
    SE_Texture* tex = mSurface->getTexture();
    if(!tex)
    {
        imageDataArray = NULL;
        imageDataNum = 0;
        return;
    }
    SE_TextureUnit* texUnit = tex->getTextureUnit(texIndex);
    if(!texUnit)
    {
        imageDataArray = NULL;
        imageDataNum = 0;
        return;
    }
    texUnit->getImageData(imageDataArray, imageDataNum);
}
void SE_TriSurfaceRenderUnit::getImageDataID(int texIndex, SE_ImageDataID*& imageIDArray, int& imageIDNum)
{
   SE_Texture* tex = mSurface->getTexture();
    if(!tex)
    {
        imageIDArray = NULL;
        imageIDNum = 0;
        return;
    }
    SE_TextureUnit* texUnit = tex->getTextureUnit(texIndex);
    if(!texUnit)
    {
        imageIDArray = NULL;
        imageIDNum = 0;
        return;
    }
    imageIDNum = texUnit->getImageDataIDNum();
    imageIDArray = texUnit->getImageDataID();    

}
void SE_TriSurfaceRenderUnit::getTexImageID(int texIndex, SE_ImageDataID*& imageDataIDArray, int& imageDataIDNum)
{
    if(texIndex < SE_TEXTURE0 || texIndex >= SE_TEXUNIT_NUM)
    {
        imageDataIDArray = NULL;
        imageDataIDNum = 0;
        return;
    }
    return getImageDataID(texIndex, imageDataIDArray, imageDataIDNum);
}
void SE_TriSurfaceRenderUnit::getTexImage(int texIndex, SE_ImageData**& imageDataArray, int& imageDataNum)
{
    if(texIndex < SE_TEXTURE0 || texIndex >= SE_TEXUNIT_NUM)
    {
        imageDataArray = NULL;
        imageDataNum = 0;
        return;
    }
    getImage(texIndex, imageDataArray, imageDataNum);
}
void SE_TriSurfaceRenderUnit::getVertex(_Vector3f*& vertex, int & vertexNum)
{
    if(mVertex != NULL)
    {
        vertex = mVertex;
        vertexNum = mVertexNum;
        SE_ASSERT(0);
        return;
    }
    SE_GeometryData* geomData = mSurface->getGeometryData();
    int facetNum = mSurface->getFacetNum();
    int* facets = mSurface->getFacetArray();
    SE_Vector3i* faceArray = geomData->getFaceArray();
    SE_Vector3f* vertexArray = geomData->getVertexArray();
    mVertex = new _Vector3f[facetNum * 3];
    mVertexNum = facetNum * 3;
    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        SE_Vector3i f = faceArray[facets[i]];
        mVertex[k].d[0] = vertexArray[f.x].x;
        mVertex[k].d[1] = vertexArray[f.x].y;
        mVertex[k].d[2] = vertexArray[f.x].z;
        k++;
        mVertex[k].d[0] = vertexArray[f.y].x;
        mVertex[k].d[1] = vertexArray[f.y].y;
        mVertex[k].d[2] = vertexArray[f.y].z;
        k++;
        mVertex[k].d[0] = vertexArray[f.z].x;
        mVertex[k].d[1] = vertexArray[f.z].y;
        mVertex[k].d[2] = vertexArray[f.z].z;
        k++;
    }
    vertex = mVertex;
    vertexNum = mVertexNum;
}

SE_MaterialData* SE_TriSurfaceRenderUnit::getMaterialData()
{
    SE_MaterialData* md = mSurface->getMaterialData();
    return md;
}
SE_Vector3f SE_TriSurfaceRenderUnit::getColor()
{
    return mSurface->getColor();
}
SE_ProgramDataID SE_TriSurfaceRenderUnit::getShaderProgramID() const
{
    return mSurface->getProgramDataID();
}
SE_TriSurfaceRenderUnit::~SE_TriSurfaceRenderUnit()
{
    if(mVertex)
        delete[] mVertex;
    if(mTexVertex)
        delete[] mTexVertex;
}

void SE_TriSurfaceRenderUnit::getTexVertex(int index, _Vector2f*& texVertex, int& texVertexNum)
{
    if(mPrimitiveType == TRIANGLES)
    {
        mSurface->getFaceTexVertex(index, texVertex, texVertexNum);
    }
    else if(mPrimitiveType == TRIANGLE_STRIP || mPrimitiveType == TRIANGLE_FAN || mPrimitiveType == TRIANGLES_INDEX)
    {
        mSurface->getTexVertex(index, texVertex, texVertexNum);
    }
}

void SE_TriSurfaceRenderUnit::setTexColorBlendMode(SE_ShaderProgram* shaderProgram)
{

}

void SE_TriSurfaceRenderUnit::draw(SE_RenderUnit* renderUnit)
{
    if(mPrimitiveType != TRIANGLES &&
       mPrimitiveType != TRIANGLE_STRIP && 
       mPrimitiveType != TRIANGLE_FAN &&
       mPrimitiveType != TRIANGLES_INDEX)
    {
        return;
    }
    const SE_ProgramDataID& spID = getShaderName();
    SE_ShaderProgram* shaderProgram = SE_Application::getInstance()->getResourceManager()->getShaderProgram(spID);
    if(!shaderProgram)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("\n\n ######### Error!!! shader program[%s] is NULL!!!######\n\n",getShaderName());
        return;
    }
    shaderProgram->use();
    const SE_RendererID rendererID = getRenderName();
    SE_Renderer* renderer = SE_Application::getInstance()->getResourceManager()->getRenderer(rendererID);
    if(!renderer)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("\n\n ######### Error!!! render is NULL!!!######\n\n");
        return;
    }
	//SE_RenderUnit::draw();
    renderer->begin(shaderProgram);
    renderer->setSurface(mSurface);
    renderer->setPrimitiveType(mPrimitiveType);
    renderer->setUserStatusBegin(this);
    renderer->setMatrix(this);    
    renderer->setImage(this);
    renderer->setColor(this);

    if(mSurface)
    {
        if(!mDontUseVertexBuffer && mSurface->getVertexBuffer())
    {
        renderer->setVertexBuffer(this);
    }
    else
    {
        renderer->setVertex(this);
        renderer->setTexVertex(this);        
    }
    }
    else
    {
        if(!mDontUseVertexBuffer)
        {
            renderer->setVertexBuffer(this);
        }
    }
    renderer->setDrawMode(this);
    renderer->draw(this);
    renderer->setUserStatusEnd(this);
    renderer->end();
    //float matrixData[16];
    //m.getColumnSequence(matrixData);
    //glUniformMatrix4fv(shaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData); 
    //checkGLError();
    //shaderProgram->use();
    /*
    _Vector3f* vertex = NULL;
    int vertexNum = 0;
    int* indexArray = NULL;
    int indexNum = 0;
    setImageAndColor(shaderProgram);
    setVertex(shaderProgram, vertex, vertexNum, indexArray, indexNum);
    setTexVertex(shaderProgram, vertexNum);
    setTexColorBlendMode(shaderProgram);
#ifdef DEBUG0
    LOGI("### vertexNum = %d #####\n", vertexNum);
#endif
    if(mPrimitiveType == TRIANGLES)
    {
        glDrawArrays(GL_TRIANGLES, 0, vertexNum);
    }
    else if(mPrimitiveType == TRIANGLE_STRIP)
    {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexNum);
    }
    else if(mPrimitiveType == TRIANGLE_FAN)
    {
        glDrawArrays(GL_TRIANGLE_FAN, 0, vertexNum);
    }
    else if(mPrimitiveType == TRIANGLES_INDEX)
    {
        glDrawElements(GL_TRIANGLES, indexNum, GL_UNSIGNED_INT, indexArray);
    }
    //checkGLError();
    */

}
//////////////////////////////////
SE_LineSegRenderUnit::SE_LineSegRenderUnit(SE_Segment* seg, int num, const SE_Vector3f& color)
{
    mSegmentNum = num;
    mSegments = NULL;
    if(num > 0)
    {
        mSegments = new SE_Segment[num];
    }
    for(int i = 0 ; i < num;  i++)
    {
        mSegments[i] = seg[i];
    }
    mColor = color;
}
SE_LineSegRenderUnit::~SE_LineSegRenderUnit()
{
    if(mSegments)
        delete[] mSegments;
}
void SE_LineSegRenderUnit::draw(SE_RenderUnit* renderUnit)
{
    if(!mSegments)
        return;
    SE_ShaderProgram* shaderProgram = SE_Application::getInstance()->getResourceManager()->getShaderProgram(DEFAULT_SHADER);
    if(!shaderProgram)
        return;
    shaderProgram->use();
    SE_Renderer* renderer = SE_Application::getInstance()->getResourceManager()->getRenderer("lineseg_renderer");
    if(!renderer)
        return;
    renderer->begin(shaderProgram);
    renderer->setMatrix(this);
    renderer->setColor(this);
    renderer->setVertex(this);
    renderer->setDrawMode(this);
    renderer->draw(this);
    renderer->end();
}
