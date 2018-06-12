#include "SE_DynamicLibType.h"
//#include "SE_Common.h"
//#include "SE_Object.h"
#include "SE_Vector.h"
#include "SE_VertexBuffer.h"
#include "SE_Factory.h"
#include "SE_Renderer.h"
#include "SE_ShaderProgram.h"
#include "SE_RenderUnit.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_Mesh.h"
//#include "SE_Geometry3D.h"
#include "SE_ShaderProperty.h"
#include "SE_Log.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
//#include "SE_SimObjectManager.h"
#include "SE_Application.h"
#include "SE_Camera.h"
#include "SE_RenderTarget.h"
#include "SE_Light.h"
#include "SE_RenderTarget.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include "SE_SceneManager.h"
#ifdef GLES_20
    #include <GLES2/gl2.h>
    #include<GLES2/gl2ext.h>
#else
    #include <GLES/gl.h>
#endif
#include "SE_MemLeakDetector.h"
#include "SE_Utils.h"
static void checkGLError()
{
    /*
    GLenum error = glGetError();
    if(error != GL_NO_ERROR)
    {
        LOGI("### gl error = %d ####\n", error);
        SE_ASSERT(0);
    }
    */
}
static void setStatistics(int vertexNum, int faceNum)
{
	SE_Application::getInstance()->getStatistics().addVertexNum(vertexNum);
	SE_Application::getInstance()->getStatistics().addFaceNum(faceNum);
	SE_Application::getInstance()->getStatistics().setCurrentVertexNum(vertexNum);
	SE_Application::getInstance()->getStatistics().setCurrentFaceNum(faceNum);
}
//////////////////////////////////////
IMPLEMENT_OBJECT(SE_Renderer)
SE_Renderer::SE_Renderer()
{    
    mVertexBuffer = NULL;
    mHasImageID = false;
}
SE_Renderer::~SE_Renderer()
{
}

void SE_Renderer::free_render()
{
    delete this;    
}
void SE_Renderer::setMatrix(SE_RenderUnit* renderUnit)
{
    if(renderUnit->isNeedGenerateReflection())
    {
        SE_Camera* s = renderUnit->getHelperCamera();

        SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
        SE_Matrix4f m = v2p.mul(renderUnit->getWorldTransform());
        
        float matrixData[16];
        m.getColumnSequence(matrixData);
        if(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
        {
            glUniformMatrix4fv(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData);  
        }
    }
    else
    {
        SE_Matrix4f m = renderUnit->getViewToPerspectiveMatrix().mul(renderUnit->getWorldTransform());
        float matrixData[16];
        m.getColumnSequence(matrixData);
        if(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
        {
            glUniformMatrix4fv(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData); 
        }
    }
}
static void setTextureStat(SE_ImageData* imageData)
{
	SE_Application::getInstance()->getStatistics().setTexture(imageData->getName());
}

bool SE_Renderer::loadTexture2D(int index, SE_ImageData* imageData, SE_WRAP_TYPE wrapS, SE_WRAP_TYPE wrapT, SE_SAMPLE_TYPE min, SE_SAMPLE_TYPE mag,bool needReGenerateMipmap)
{
    if(imageData == NULL)
    {
        //LOGI("### can not load texture: ###\n");
        return false;
    }
    GLuint texid = imageData->getTexID();
    if(imageData->getData() == NULL && texid == 0 && !imageData->isFBOImage())
    {
        //LOGI("[%s]imageData->getData() is NULL return!!!!\n\n",imageData->getName());
#ifdef ANDROID
        //send message to java
        bool result = SE_Application::getInstance()->sendMessageToJava(10,imageData->getName());
        if (!result) {
            mHasTexture[index].hasTexture = false;
        }
        return result;
#endif
        mHasTexture[index].hasTexture = false;
        return false;
    }
	//setTextureStat(imageData);
	//for debug
	/*
	if(std::string(imageData->getName()) != "home_basedata.cbf\\demo_TV00CompleteMap.pvr")
	{
		return;
	    //SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
	    ///imageData = resourceManager->getImageData("home_basedata.cbf\\demo_TV00CompleteMap.pvr");
	}
	*/
	//end

        //glEnable(GL_TEXTURE_2D);

    //checkGLError();
    GLenum texType = GL_TEXTURE0;
    switch(index)
    {
    case 0:
        texType = GL_TEXTURE0;
        break;
    case 1:
        texType = GL_TEXTURE1;
        break;
    case 2:
        texType = GL_TEXTURE2;
        break;
    case 3:
        texType = GL_TEXTURE3;
        break;
    default:
        break;
    }
    glPixelStorei(GL_UNPACK_ALIGNMENT,1);
    checkGLError();
    glActiveTexture(texType);
    checkGLError();
#ifdef DEBUG0
    LOGI("## texid = %d ##\n", texid);
#endif
    if(texid == 0)
    {
        glGenTextures(1, &texid);
        checkGLError();
        imageData->setTexID(texid);
#ifdef DEBUG0
        LOGI("### texSize = %d ###\n", texSize);
        texSize++;
#endif
    }
    else
    {
        if(glIsTexture(texid) == GL_TRUE)
        {
            //LOGI("### is texture ####\n");
        }       
        glBindTexture(GL_TEXTURE_2D, texid);
        if(needReGenerateMipmap)
        {
            //for per-frame update fbo texture;
            //glGenerateMipmap (GL_TEXTURE_2D);
        }
        return true;
        /*
        GLenum error = glGetError();
        if(error == GL_NO_ERROR)
        {
            return;
        }
        else if(error == GL_INVALID_ENUM)
        {
            LOGI("### bindtexture error ###\n");
            glGenTextures(1, &texid);
            checkGLError();
            imageData->setTexID(texid);
        }
        */
    }
    glBindTexture(GL_TEXTURE_2D, texid);
    checkGLError();
    if(!imageData->isCompressTypeByHardware())
    {
        GLint internalFormat = GL_RGB;
        GLenum format = GL_RGB;
        GLenum type = GL_UNSIGNED_BYTE;
        if(imageData->getPixelFormat() == SE_ImageData::RGBA)
        {
            internalFormat = GL_RGBA;
            format = GL_RGBA;
        } 
        else if(imageData->getPixelFormat() == SE_ImageData::RGB_565)
        {
            type = GL_UNSIGNED_SHORT_5_6_5;
        } 
        else if (imageData->getPixelFormat() == SE_ImageData::RGBA_4444)
        {
            internalFormat = GL_RGBA;
            format = GL_RGBA;
            type = GL_UNSIGNED_SHORT_4_4_4_4;
        }
        
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, imageData->getWidth(), imageData->getHeight(),0, format, type, imageData->getData());        
        glGenerateMipmap (GL_TEXTURE_2D);
        checkGLError();
    }
    else
    {
        //upload compressed texture format to opengl
        GLint internalFormat = GL_RGB;
        if(imageData->getCompressType() == SE_ImageData::OGL_PVRTC2)
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("\n\nUse PVR compressed texture\n\n");
            internalFormat = GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;//GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
            glCompressedTexImage2D(GL_TEXTURE_2D, 0, internalFormat, imageData->getWidth(), imageData->getHeight(),0,imageData->getDataSizeInByte(),imageData->getData());
            checkGLError();

        }
        else if(imageData->getCompressType() == SE_ImageData::ETC_RGB_4BPP)
        {
            internalFormat = GL_ETC1_RGB8_OES;
            glCompressedTexImage2D(GL_TEXTURE_2D, 0, internalFormat, imageData->getWidth(), imageData->getHeight(),0,imageData->getDataSizeInByte(),imageData->getData());
            checkGLError();
        }

    }
    GLint wraps , wrapt;
    switch(wrapS)
    {
    case REPEAT:
        
        if(SE_Util::isPower2(imageData->getWidth()) && SE_Util::isPower2(imageData->getHeight()))
        {
        wraps = GL_REPEAT;
        }
        else
        {
        //LOGI("image is not pow2\n\n");
            wraps = GL_CLAMP_TO_EDGE;
        }
        break;
    case CLAMP:
        wraps = GL_CLAMP_TO_EDGE;
        break;
    default:
        wraps = GL_REPEAT;
    }
    switch(wrapT)
    {
    case REPEAT:
        if(SE_Util::isPower2(imageData->getWidth()) && SE_Util::isPower2(imageData->getHeight()))
        {
        wrapt = GL_REPEAT;
        }
        else
        {
        LOGI("image is not pow2\n\n");
            wrapt = GL_CLAMP_TO_EDGE;
        }
        break;
    case CLAMP:
        wrapt = GL_CLAMP_TO_EDGE;
        break;
    default:
        wrapt = GL_REPEAT;
        break;
    }
    GLint sampleMin, sampleMag;
    switch(min)
    {
    case NEAREST:
        sampleMin = GL_NEAREST;
        break;
    case LINEAR:
        sampleMin = GL_LINEAR;
        break;
    case MIPMAP:
        if(imageData->isCompressTypeByHardware())
        {
            sampleMin = GL_LINEAR;
        }
        else
        {
            sampleMin = GL_LINEAR_MIPMAP_LINEAR; 
        }
        break;
    default:
        sampleMin = GL_LINEAR;
    }
    switch(mag)
    {
    case NEAREST:
        sampleMag = GL_NEAREST;
        break;
    case LINEAR:
        sampleMag = GL_LINEAR;
        break;
    default:
        sampleMag = GL_LINEAR;
    }        
    
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
    checkGLError();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
    checkGLError();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMin );
    checkGLError();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMag ); 
    checkGLError();
    //glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
    return true;
}


void SE_Renderer::setVertexBuffer(SE_RenderUnit* renderUnit)
{}
void SE_Renderer::setImage(SE_RenderUnit* renderUnit)
{
}
void SE_Renderer::setImage(int texIndex, SE_RenderUnit* renderUnit)
{
    SE_ImageDataID* imageDataIDArray = NULL;
    int imageDataIDNum = 0;
    SE_ImageData** imageDataArray;
    int imageDataNum;
    bool hasTexture = false;
    float utiling = 1.0;
    float vtiling = 1.0;
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    //for specular
    //renderUnit->getTexImageID(texIndex, imageDataIDArray, imageDataIDNum);
    //renderUnit->getTexImage(texIndex, imageDataArray, imageDataNum);    


    SE_Texture* texture = mSurface->getTexture();
    SE_TextureUnit* texUnit = NULL;

    if(!texture)
    {
        mHasImageID = false;
        return;
    }
    else
    {
        int texUnitNum = texture->getTexUnitNum();

        int replaceIndex = -1;
        bool useFbo = renderUnit->isUseFbo();
        if(useFbo)
        {
            if(renderUnit->isFboReplaceCurrentTexture())
            {
                //replace
                replaceIndex = renderUnit->getFboReplaceTextureIndex();
            }            
        }

        for(int i = 0; i < texUnitNum; ++i)
        {
            renderUnit->getTexImageID(i, imageDataIDArray, imageDataIDNum);
            //renderUnit->getTexImage(i, imageDataArray, imageDataNum);
            if(texture)
            {
                texUnit = texture->getTextureUnit(i);
            }
            utiling = texUnit->getUTiling();
            vtiling = texUnit->getVTiling();
            if(imageDataIDNum > 0)
            {
                if(replaceIndex != -1 && replaceIndex == i)
                {   
                    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
                    SE_RenderTarget* rt = rtm->get(renderUnit->getUsedFboName());                
                    if(rt)
                    {
                        if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
                        {
                            SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
                            SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                                hasTexture = loadTexture2D(replaceIndex,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                            utiling = 1.0;
                            vtiling = 1.0;  
                        }

                    }
                }
                else
                {
                    //std::string newkey = renderUnit->getNewImageKey();

                    SE_ImageDataID imageID;

                    imageID = imageDataIDArray[0];
                    SE_ImageData* imageData = resourceManager->getImageData(imageID);

                    std::string imageId = imageID.getStr();
                    if(imageId.empty())
                    {
                        mHasImageID = false;
                    }
                    else
                    {
                        mHasImageID = true;
                    }
                    if(imageData)
                    {
                        hasTexture = loadTexture2D(i, imageData, (SE_WRAP_TYPE)texUnit->getWrapS(), (SE_WRAP_TYPE)texUnit->getWrapT(), (SE_SAMPLE_TYPE)texUnit->getSampleMin(), (SE_SAMPLE_TYPE)texUnit->getSampleMag());
                }
                    else
                    {
                        hasTexture = false;
                    }
                }

            }
            else
            {
	            mHasImageID = false;
            }
           
            mHasTexture[i].hasTexture = hasTexture;
            mHasTexture[i].uTiling = utiling;
            mHasTexture[i].vTiling = vtiling;
        }
    }
}
void SE_Renderer::setColor(SE_RenderUnit* renderUnit)
{}
void SE_Renderer::setVertex(SE_RenderUnit* renderUnit)
{
    if(mPrimitiveType == TRIANGLES)
    {
        mSurface->getFaceVertex(mVertex, mVertexNum);
    }
    else if(mPrimitiveType == TRIANGLE_STRIP || mPrimitiveType == TRIANGLE_FAN || mPrimitiveType == TRIANGLES_INDEX)
    {
        mSurface->getVertex(mVertex, mVertexNum);
        mSurface->getVertexIndex(mIndexArray, mIndexNum);
    }
    
    //no vbo mode
    if(mBaseShaderProgram->getPositionAttributeLoc() != -1)
    {
        glVertexAttribPointer(mBaseShaderProgram->getPositionAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, mVertex);
        glEnableVertexAttribArray(mBaseShaderProgram->getPositionAttributeLoc());
    }

}

void SE_Renderer::end()
{    
    if(mVertexBuffer)
    {
        mVertexBuffer->unBindVBO();
    } 
    mVertexBuffer = NULL;
    
}
void SE_Renderer::setTexVertex(SE_RenderUnit* renderUnit)
{}
void SE_Renderer::setDrawMode(SE_RenderUnit* renderUnit)
{}
void SE_Renderer::setTexVertex(int index, SE_RenderUnit* renderUnit)
{    
    renderUnit->getTexVertex(index, mTexVertex, mTexVertexNum);
    if(mTexVertexNum > 0)
    {
        SE_ASSERT(mVertexNum == mTexVertexNum);
        mHasTexCoord[index] = 1;
    }
    else
    {
        mHasTexCoord[index] = 0;
    }   

}
void SE_Renderer::begin(SE_ShaderProgram* shaderProgram)
{
    reset();
    mBaseShaderProgram = shaderProgram;
}
void SE_Renderer::reset()
{
    mBaseShaderProgram = NULL;
    mVertex = NULL;
    mVertexNum = 0;
    mIndexArray = NULL;
    mIndexNum = 0;
    mSurface = NULL;
    mTexVertexNum = 0;
    mTexVertex = NULL;
    mPrimitiveType = TRIANGLES;
    for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
    {
        mHasTexture[i].hasTexture = 0;
        mHasTexCoord[i] = 0;
    }
        mVertexBuffer = NULL;
}
void SE_Renderer::draw(SE_RenderUnit* renderUnit)
{    
    if(mPrimitiveType == TRIANGLES)
    {       
        bool imageNotFound = false;
        if(mHasImageID && !mHasTexture[0].hasTexture)
        {
            imageNotFound = true;
            return;
            if(!renderUnit->getRenderableSpatial()->isSpatialHasRuntimeAttribute(SE_SpatialAttribute::NEEDDRAWLINE))
            {
                return;
            }
        } 
        if(mVertexBuffer)
        {
            /*
            SE_Camera* s = SE_Application::getInstance()->getCurrentCamera();
            SE_Vector3f caloc = s->getLocation();
            SE_Matrix4f m2w = renderUnit->getWorldTransform();
            SE_Matrix4f w2m = m2w.inverse();
            SE_Vector3f locInm = w2m.map(SE_Vector4f(caloc,1.0)).xyz();
            
            unsigned short* index = mVertexBuffer->indexData;
            float* data = mVertexBuffer->vertexData;
            std::vector<unsigned short> mybuffer;
            int datastride = mVertexBuffer->getDataStride();
            int k = 0;
            for(int i = 0; i < mVertexBuffer->indexNum; ++i)
            {
                 int indexint = index[i];
                 SE_Vector3f pos;
                 pos.x = data[indexint * datastride + 0];
                 pos.y = data[indexint * datastride + 1];
                 pos.z = data[indexint * datastride + 2];

                 SE_Vector3f normal;
                 normal.x = data[indexint * datastride + 3];
                 normal.y = data[indexint * datastride + 4];
                 normal.z = data[indexint * datastride + 5];

                 
                 SE_Vector3f p2c = locInm.subtract(pos).normalize();

                 if(p2c.dot(normal) > 0)
                 {
                     mybuffer.push_back(index[i]);
                 }                
            }
            */
            if(!mVertexBuffer->vboHasInit())
            {
                if(imageNotFound)
                {
                    glLineWidth(1.0);
                    glDrawElements(GL_LINES, mVertexBuffer->indexNum, GL_UNSIGNED_SHORT, mVertexBuffer->indexData);
                }
                else
                {
                glDrawElements(GL_TRIANGLES, mVertexBuffer->indexNum, GL_UNSIGNED_SHORT, mVertexBuffer->indexData); 
            }
            }
            else
            {
                if(imageNotFound)
                {
                    glLineWidth(1.0);
                    glDrawElements(GL_LINES, mVertexBuffer->indexNum, GL_UNSIGNED_SHORT,0);
                }
            else
            {                
                glDrawElements(GL_TRIANGLES, mVertexBuffer->indexNum, GL_UNSIGNED_SHORT,0);               
                }

            }
            //glDrawElements(GL_TRIANGLES, mybuffer.size(), GL_UNSIGNED_SHORT, &mybuffer[0]); 
            //setStatistics(mVertexBuffer->vertexDataNum / mVertexBuffer->getVertexDataSize(), mVertexBuffer->indexNum / 3); 
        }        
        else
        {           

            glDrawArrays(GL_TRIANGLES, 0, mVertexNum);
            //setStatistics(mVertexNum, mVertexNum / 3);            
        }
    }
    else if(mPrimitiveType == TRIANGLE_STRIP)
    {        
        glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexNum);        
    }
    else if(mPrimitiveType == TRIANGLE_FAN)
    {
        glDrawArrays(GL_TRIANGLE_FAN, 0, mVertexNum);
    }
    else if(mPrimitiveType == TRIANGLES_INDEX)
    {
        glDrawElements(GL_TRIANGLES, mIndexNum, GL_UNSIGNED_INT, mIndexArray);
    }
}
void SE_Renderer::colorMask(bool red, bool green, bool blue, bool alpha)
{
    glColorMask(red, green, blue, alpha);
}
void SE_Renderer::setClearColor(const SE_Vector4f& color)
{
    glClearColor(color.x, color.y, color.z, color.w);
    //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
}
void SE_Renderer::clear(int pattern)
{
    switch(pattern)
    {
    case SE_DEPTH_BUFFER:
        glClear(GL_DEPTH_BUFFER_BIT);
        break;
    case SE_COLOR_BUFFER:
        glClear(GL_COLOR_BUFFER_BIT);
        break;
    case SE_DEPTH_BUFFER|SE_COLOR_BUFFER:
        glDepthMask(GL_TRUE);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        break;
    default:
        glDepthMask(GL_TRUE);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
void SE_Renderer::enableDepthTest(bool enable)
{
    if(enable)
	{
		glEnable(GL_DEPTH_TEST);
	}
	else
	{
		glDisable(GL_DEPTH_TEST);
    }
}

void SE_Renderer::enableBlend(bool enable)
{
    if(enable)
	{
		glEnable(GL_BLEND);
	}
	else
	{
		glDisable(GL_BLEND);
    }
}

void SE_Renderer::setDepthTestMask(bool enable)
{
    glDepthMask(enable);	
}
void SE_Renderer::setViewport(int x, int y, int w, int h)
{
    glViewport(x, y, w, h);
}
void SE_Renderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{}
void SE_Renderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{}
//////////////////////////////
IMPLEMENT_OBJECT(SE_ColorExtractRenderer)
SE_ColorExtractRenderer::SE_ColorExtractRenderer()
{
}
SE_ColorExtractRenderer::~SE_ColorExtractRenderer()
{}
void SE_ColorExtractRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mShaderProgram = (SE_ColorExtractShaderProgram*)shaderProgram;
}
void SE_ColorExtractRenderer::setImage(SE_RenderUnit* renderUnit)
{
    for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
    {
        SE_Renderer::setImage(i, renderUnit);
        {
            if(mShaderProgram->getTextureUniformLoc(i) != -1)
            {
                glUniform1i(mShaderProgram->getTextureUniformLoc(i), i);
            }
        }
    }
}
void SE_ColorExtractRenderer::setColor(SE_RenderUnit* renderUnit)
{
    /*
    SE_MaterialData* md = mSurface->getMaterialData();
    float color[3];
    SE_Vector3f c = mSurface->getColor();
    if(md)
    {
        color[0] = md->ambient.x;
        color[1] = md->ambient.y;
        color[2] = md->ambient.z;

    }
    else
    {
        color[0] = c.x;
        color[1] = c.y;
        color[2] = c.z;
    }
    //checkGLError();
    glUniform3fv(mShaderProgram->getColorUniformLoc(), 1, color);
    for(int i = 0 ; i < 4 ; i++)
    {
        c = mSurface->getMarkColor(i);
        color[0] = c.x;
        color[1] = c.y;
        color[2] = c.z;
        glUniform3fv(mShaderProgram->getMarkColorUniformLoc(i), 1, color);
    }
    */
}

void SE_ColorExtractRenderer::setTexVertex(SE_RenderUnit* renderUnit)
{
    for(int i = 0 ; i < SE_TEXUNIT_NUM ; i++)
    {
        SE_Renderer::setTexVertex(i, renderUnit);
        if(mHasTexCoord[i])
        {
            if(mShaderProgram->getTextureCoordAttributeLoc(i) != -1)
            {
                glVertexAttribPointer(mShaderProgram->getTextureCoordAttributeLoc(i), 2, GL_FLOAT, 0, 0, mTexVertex);
                glEnableVertexAttribArray(mShaderProgram->getTextureCoordAttributeLoc(i));
            }
        }
        else
        {
            glDisableVertexAttribArray(mShaderProgram->getTextureCoordAttributeLoc(i));
        }
    }
}
void SE_ColorExtractRenderer::setDrawMode(SE_RenderUnit* renderUnit)
{
    int colorOp;
    SE_ColorExtractShaderProperty* sp = (SE_ColorExtractShaderProperty*)mSurface->getShaderProperty();
    colorOp = sp->getColorOperationMode();
    if(mShaderProgram->getColorOpModeUniformLoc() != -1)
    {
        glUniform1i(mShaderProgram->getColorOpModeUniformLoc(), colorOp);
    }
}
////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleSurfaceRenderer)
void SE_SimpleSurfaceRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setImage(0, renderUnit);
    if(mHasTexture[0].hasTexture)
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc(), 0); 
        }

        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
        {
            glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 1.0);
        }

        float uvtiling[2];
        uvtiling[0] = mHasTexture[0].uTiling;
        uvtiling[1] = mHasTexture[0].vTiling;

        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc() != -1)
        {
            glUniform2fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc(), 1, uvtiling);  
        }

    }
    else
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
        {
            glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 0.0);
        }
    }
}
void SE_SimpleSurfaceRenderer::begin(SE_ShaderProgram* shaderProgram)
{

/*
    int ccount;
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS,&ccount);

    int *p = NULL;
    p = new int[ccount];
    glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS,p);

    for ( int i = 0; i < ccount; ++i )
    {
        LOGI( "                                           0x%X\n", p[i] );
    }

*/
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_SimpleSurfaceShaderProgram*)shaderProgram;
}
void SE_SimpleSurfaceRenderer::setColor(SE_RenderUnit* renderUnit)
{
    if(mSurface)
    {
    SE_MaterialData* md = mSurface->getMaterialData();
    float color[3];
    SE_Vector3f c = mSurface->getColor();

    //if object image not load yet,show a black color
    if(this->mHasImageID)
    {
        md->ambient.set(0.0,0.0,0.0);
        c.set(0.0,0.0,0.0);
    }

    if(md)
    {
        color[0] = md->ambient.x;
        color[1] = md->ambient.y;
        color[2] = md->ambient.z;

    }
    else
    {
        color[0] = c.x;
        color[1] = c.y;
        color[2] = c.z;
    }

    //checkGLError();
    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getColorUniformLoc() != -1)
    {
        glUniform3fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getColorUniformLoc(), 1, color);   
    }
}
}
void SE_SimpleSurfaceRenderer::setVertexBuffer(SE_RenderUnit* renderUnit)
{
    SE_VertexBuffer *vb = mSurface->getVertexBuffer();

    if(!vb)
    {
        return;
    }

    int vertex_pos_size = vb->mPosSize;
    int vertex_normal_size = vb->mNormalSize;
    int vertex_tex0_size = vb->mTex0Size;
    int vertex_tex0_offset = vb->mPosSize + vb->mNormalSize;// v0 v1 v2 s t|
    int vertex_normal_offset = vb->mPosSize;
    
    int vertex_color_size = vb->mColorSize;
    int vertex_tex0_color_offset = vb->mPosSize + vb->mTex0Size;

    int vertex_size = vb->getDataStride();

    vb->initVBO();
    vb->bindVBO();
    
    if(!vb->vboHasInit())
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), vertex_pos_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData);
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());
        }
        
        if(vb->hasVBState(SE_VertexBuffer::VBS_TEXTURE0))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), vertex_tex0_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_tex0_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
        }

        if(vb->hasVBState(SE_VertexBuffer::VBS_VERTEXCOLOR))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc(), vertex_color_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_tex0_color_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
        }   

        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_normal_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
            }
        }
    }
    else
    {
        //use vbo
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), vertex_pos_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)0);
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());  
        }
        
        if(vb->hasVBState(SE_VertexBuffer::VBS_TEXTURE0))
        {        
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), vertex_tex0_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)(vertex_tex0_offset * sizeof(float)));
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
        }

        if(vb->hasVBState(SE_VertexBuffer::VBS_VERTEXCOLOR))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc(), vertex_color_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float),(void*)(vertex_tex0_color_offset * sizeof(float)));
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
        }  

        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)(vertex_normal_offset * sizeof(float)));
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
        }
    }

   
    /*if(mVertexBuffer)
    {
        int vertex_pos_size = mVertexBuffer->mPosSize;
        int vertex_normal_size = mVertexBuffer->mNormalSize;
        int vertex_normal_offset = mVertexBuffer->mPosSize;
        int vertex_size = mVertexBuffer->getDataStride();

        glVertexAttribPointer(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), mVertexBuffer->vertexData + vertex_normal_offset);
        glEnableVertexAttribArray(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());

    }
    else
    {
        _Vector3f* normal = NULL;
        int normalNum = 0;

        mSurface->getFaceVertexNormal(normal,normalNum);
        glVertexAttribPointer(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, normal);
        glEnableVertexAttribArray(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
    }
    */

    mVertexBuffer = vb;
}
void SE_SimpleSurfaceRenderer::setTexVertex(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setTexVertex(0, renderUnit);
    if(mHasTexCoord[0])
    {        
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), 2, GL_FLOAT, 0, 0, mTexVertex);
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
        }

    }
    else
    {
        glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
    }    
}
void SE_SimpleSurfaceRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{    
    float user_frag_para[USER_ATTR_COUNT * 4] = {0};           

    //bool blendOn = renderUnit->isEnableBlend();
    //if(blendOn)
    //{
        //set alpha status     
        float alpha = renderUnit->getAlpha();
        user_frag_para[0] = 1.0;//use alpha
        user_frag_para[1] = alpha;//alpha
        user_frag_para[2] = 0.0;
        user_frag_para[3] = 0.0;


        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getFragAlpha() != -1)
        {
            float alpha = user_frag_para[1];
            glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getFragAlpha(), alpha); 

        }
        /*
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseUserAlpha() != -1)
        {
            glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseUserAlpha(), 1.0);
        }
    }
    else
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseUserAlpha() != -1)
        {
            glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseUserAlpha(), 0.0);
        }
    }*/

    //set user color
    if(mSurface)
    {        
        float useUserColor = renderUnit->getRenderableSpatial()->isUseUserColor();

        SE_Vector3f userColor = renderUnit->getRenderableSpatial()->getUserColor();

        user_frag_para[4] = userColor.x;
        user_frag_para[4 + 1] = userColor.y;
        user_frag_para[4 + 2] = userColor.z;
        user_frag_para[4 + 3] = 0.0;

            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getFragParam() != -1)
        {
            float color[3] = {1.0,1.0,1.0};
            if(useUserColor > 0.0)
            {
            color[0] = userColor.x;
            color[1] = userColor.y;
            color[2] = userColor.z;
            }
                glUniform3fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getFragParam(),1,color);
        }
    }
    
#if 0
    //set stencil status
    if(renderUnit->isDrawStencil())
    {
        //will draw a mirror,so need write stencil buffer        
        //enable stencil
        if(renderUnit->isEnableStencil())
        {
            glEnable(GL_STENCIL_TEST);
            glClear(GL_STENCIL_BUFFER_BIT);
        }

        //do not draw
        glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
        glStencilOp(GL_REPLACE,GL_REPLACE,GL_REPLACE);
        glStencilFunc(GL_ALWAYS,1,0xff);        

        //z-buffer will change stencil buffer,so read-only
        glDepthMask(GL_FALSE);
    }
    else if(renderUnit->isMirroredObject())
    {
        //use stencil,not change buffer   
        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        glStencilFunc(GL_EQUAL,1,0xff);  
        glStencilOp(GL_KEEP,GL_KEEP,GL_KEEP);  
        
    }
    else
    {
        //other object        
    }
#endif

    if(renderUnit->isEnableCullFace())
    {        
	    glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glCullFace(GL_BACK);

    }
    else
    {
        glDisable(GL_CULL_FACE);
    }



    //forward additive
    if(renderUnit->isAdditive())
    {
        //just let z <= currentdepth draw
        glDepthMask(GL_FALSE);
        glDepthFunc (GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE,GL_ONE);
    }
    else
    {
        //enable depth write
        //glDepthMask(GL_TRUE);
        //glDepthFunc (GL_LESS);
    }

    //for reverse texture
    SE_Vector2f reverse = renderUnit->getTexCoordXYReverse();
    float r[2];
    r[0] = reverse.x;
    r[1] = reverse.y;
        
    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getReverseTextureLoc() != -1)
    {
        
        glUniform2fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getReverseTextureLoc(),1, r);
    }
    
    if(renderUnit->getRenderableSpatial())
    {
        bool r = renderUnit->getRenderableSpatial()->isNeedBlackWhiteColor();
        SE_Vector3f bwswitcher = renderUnit->getRenderableSpatial()->getBlackWhiteColorSwitcher();
        float bw[3] = {1.0,1.0,1.0};
        if(r)
        {
            bw[0] = bwswitcher.x;
            bw[1] = bwswitcher.y;
            bw[2] = bwswitcher.z;
        
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseBWColorLoc() != -1)
        {
                glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseBWColorLoc(),1.0);
        }

            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUserBWColorLoc() != -1)
            {
                glUniform3fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUserBWColorLoc(),1,bw);

            }

        }
        else
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseBWColorLoc() != -1)
            {
                glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUseBWColorLoc(),0.0);
            }
        }
        
        
    }
}
void SE_SimpleSurfaceRenderer::setUserStatusEnd(SE_RenderUnit* renderUnit)
{
#if 0
    if(renderUnit->isDrawStencil())
    {
        //stencil object draw finish,now
        glDepthMask(GL_TRUE); 
        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        //do not disable stencil     
        
    }
    else if(renderUnit->isMirroredObject())
    {
        //disable stencle,mirrored object list has finished.
        if(renderUnit->isDisableStencil())
        {            
            glDisable(GL_STENCIL_TEST);
        }
    }
#endif

    if(renderUnit->isNeedGenerateShadow())
    {
        //glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        //glDisable(GL_CULL_FACE);
    }

    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
    {
    glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());
    }

    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
    {
    glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
    }    
       
}
void SE_SimpleSurfaceRenderer::setDrawMode(SE_RenderUnit* renderUnit)
{}
///////////////////////////////////////
////////////////////////////
IMPLEMENT_OBJECT(SE_FogRenderer)
void SE_FogRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);
    //push m2v
    SE_Camera *camera = renderUnit->getRenderableSpatial()->getScene()->getCamera();    
    
    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Matrix4f w2v = camera->getWorldToViewMatrix();
    SE_Matrix4f m2v = m2w.mul(w2v);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_FogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc() != -1)
    {
        glUniformMatrix4fv(((SE_FogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc(), 1, 0, matrixData); 
    }

    const float fFogStart = 0.0f;
	const float fFogEnd = 2000.0f;
	const float fFogDensity = 0.002f;
	const float fFogRcpEndStartDiff = 1.0f / (fFogEnd - fFogStart);
	const float afFogColor[3] = { 0.8f, 0.8f, 1.0f }; // the fog RGB color

    if(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogEndLoc() != -1)
    {
        glUniform1f(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogEndLoc(), fFogEnd); 
    }

    if(((SE_FogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc() != -1)
    {
        glUniform1f(((SE_FogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc(), fFogRcpEndStartDiff); 
    }

    if(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc() != -1)
    {
        glUniform1f(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc(), fFogDensity); 
    }

    if(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogColor() != -1)
    {
        glUniform3fv(((SE_FogShaderProgram*)mBaseShaderProgram)->getFogColor(),1, afFogColor); 
    }

}
//////////////////////////////////////
IMPLEMENT_OBJECT(SE_LineSegRenderer)
SE_LineSegRenderer::SE_LineSegRenderer()
{
    mPoints = NULL;
    mPointNum = 0;
}
SE_LineSegRenderer::~SE_LineSegRenderer()
{}
void SE_LineSegRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mPoints = NULL;
    mPointNum = 0;
    mShaderProgram = (SE_SimpleSurfaceShaderProgram*)shaderProgram;
}
void SE_LineSegRenderer::setColor(SE_RenderUnit* renderUnit)
{
    SE_LineSegRenderUnit* ru = (SE_LineSegRenderUnit*)renderUnit;
    SE_Vector3f colorv = ru->getColor();
    float color[3];
    color[0] = colorv.x;
    color[1] = colorv.y;
    color[2] = colorv.z;
    if(mShaderProgram->getColorUniformLoc() != -1)
    {
        glUniform3fv(mShaderProgram->getColorUniformLoc(), 1, color);
    }
}
void SE_LineSegRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Matrix4f m;
    m.identity();
    m = renderUnit->getViewToPerspectiveMatrix().mul(m);
    float data[16];
    m.getColumnSequence(data);
    if(mShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(mShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, data);
    }
}
void SE_LineSegRenderer::setVertex(SE_RenderUnit* renderUnit)
{
    SE_LineSegRenderUnit* ru  = (SE_LineSegRenderUnit*)renderUnit;
    int segmentNum = ru->getSegmentsNum();
    SE_Segment* segments = ru->getSegments();
    mPoints = new _Vector3f[segmentNum * 2];
    if(!mPoints)
        return;
    mPointNum = segmentNum * 2;
    int k = 0;
    for(int i = 0 ; i < segmentNum ; i++)
    {
        const SE_Segment& se = segments[i];
        const SE_Vector3f& start = se.getStart();
        const SE_Vector3f& end = se.getEnd();
        for(int i = 0 ; i < 3 ; i++)
            mPoints[k].d[i] = start.d[i];
        k++;
        for(int i = 0 ; i < 3 ; i++)
            mPoints[k].d[i] = end.d[i];
        k++;
    }
    if(mShaderProgram->getPositionAttributeLoc() != -1)
    {
        glVertexAttribPointer(mShaderProgram->getPositionAttributeLoc(), 3, GL_FLOAT,
                              GL_FALSE, 0, mPoints);
        glEnableVertexAttribArray(mShaderProgram->getPositionAttributeLoc());
    }
}


void SE_LineSegRenderer::draw(SE_RenderUnit* renderUnit)
{
    if(!mPoints)
        return;
    glDrawArrays(GL_LINES, 0, mPointNum);
}
void SE_LineSegRenderer::end()
{
    if(mPoints)
        delete[] mPoints;
}
void SE_LineSegRenderer::setDrawMode(SE_RenderUnit* renderUnit)
{
    if(mShaderProgram->getShadingModeUniformLoc() != -1)
    {
        glUniform1f(mShaderProgram->getShadingModeUniformLoc(), 0.0);
    }
}

///////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawLineRenderer)
SE_DrawLineRenderer::SE_DrawLineRenderer()
{
    mPoints = NULL;
    mPointNum = 0;
}
SE_DrawLineRenderer::~SE_DrawLineRenderer()
{}
void SE_DrawLineRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mPoints = NULL;
    mPointNum = 0;    
    mBaseShaderProgram = (SE_SimpleSurfaceShaderProgram*)shaderProgram;
}
void SE_DrawLineRenderer::setVertex(SE_RenderUnit* renderUnit)
{
    //SE_LineSegRenderUnit* ru  = (SE_LineSegRenderUnit*)renderUnit;
    //int segmentNum = ru->getSegmentsNum();
    //SE_Segment* segments = ru->getSegments();
    //mPoints = new _Vector3f[segmentNum * 2];
    //if(!mPoints)
    //    return;
    //mPointNum = segmentNum * 2;
    //int k = 0;
    //for(int i = 0 ; i < segmentNum ; i++)
    //{
    //    const SE_Segment& se = segments[i];
    //    const SE_Vector3f& start = se.getStart();
    //    const SE_Vector3f& end = se.getEnd();
    //    for(int i = 0 ; i < 3 ; i++)
    //        mPoints[k].d[i] = start.d[i];
    //    k++;
    //    for(int i = 0 ; i < 3 ; i++)
    //        mPoints[k].d[i] = end.d[i];
    //    k++;
    //}

    glLineWidth(renderUnit->getLineWidth());
    mSurface->getVertex(mPoints,mPointNum);    

    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
    {
        glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, mPoints);
        glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());
    }
}
void SE_DrawLineRenderer::draw(SE_RenderUnit* renderUnit)
{
    glDrawArrays(GL_LINES, 0, mPointNum);
}

void SE_DrawLineRenderer::setDrawMode(SE_RenderUnit* renderUnit)
{
    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
    {
        glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 0.0);
    }
}

void SE_DrawLineRenderer::setColor(SE_RenderUnit* renderUnit)
{
    if(mSurface)
    {
        SE_Vector3f c = mSurface->getColor();

        float color[3];

        color[0] = c.x;
        color[1] = c.y;
        color[2] = c.z;

        //checkGLError();
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getColorUniformLoc() != -1)
        {
            glUniform3fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getColorUniformLoc(), 1, color);   
        }
    }
}

void SE_DrawLineRenderer::end()
{    
}
///////////////////////////////
IMPLEMENT_OBJECT(SE_LightMapRenderer)

void SE_LightMapRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_LightMapShaderProgram*)shaderProgram;
}
void SE_LightMapRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setImage(0, renderUnit);
    if(mHasTexture[0].hasTexture)
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc(), 0);
        }
        //push uvtiling when all status
        float uvtiling[2];
        uvtiling[0] = mHasTexture[0].uTiling;
        uvtiling[1] = mHasTexture[0].vTiling;
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc() != -1)
        {
            glUniform2fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc(), 1, uvtiling);  
        }

        if(mHasTexture[1].hasTexture)
        {
            if(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getSpecularLightMapTextureUniformLoc() != -1)
            {
                glUniform1i(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getSpecularLightMapTextureUniformLoc(), 1);
                glUniform1f(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 2.0);
            }
            else
            {
                if(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
                {
                    glUniform1f(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 1.0);
                }
            }
        }
        else
        {
            if(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
            {
                glUniform1f(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 1.0);
            }
        }        
    }
    else
    {
        if(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
        {
            glUniform1f(((SE_LightMapShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 0.0);
        }
    }
    

}

////////////////////////////////////
IMPLEMENT_OBJECT(SE_SkeletalAnimationRenderer)
void SE_SkeletalAnimationRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setImage(0, renderUnit);
    if(mHasTexture[0].hasTexture)
    {
        if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc(), 0);
        }

        if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
        {
            glUniform1f(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 1.0);
        }
    }
    else
    {
        if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
        {
            glUniform1f(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 0.0);
        }
    }    
}
void SE_SkeletalAnimationRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_SkeletalAnimationShaderProgram*)shaderProgram;

}

void SE_SkeletalAnimationRenderer::setTexVertex(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setTexVertex(renderUnit);    
    //set skeleton info
    setAnimationData();
}

void SE_SkeletalAnimationRenderer::setAnimationData()
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();

    const char *controllerID = mSurface->getCurrentBipedControllerID();
    SE_BipedController* bipedController = mSurface->getCurrentBipedController();    

    //set boneIndex
    if(mSurface->getSkeletonIndex() != NULL)
    {
        if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneIndexAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneIndexAttributeLoc(), 4, GL_FLOAT, 0, 0, mSurface->getSkeletonIndex());
            glEnableVertexAttribArray(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneIndexAttributeLoc());
        }
    }
    else
    {
        glDisableVertexAttribArray(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneIndexAttributeLoc());
    }

    if(mSurface->getSkeletonWeight() != NULL)
    {
        if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneWeightAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneWeightAttributeLoc(), 4, GL_FLOAT, 0, 0, mSurface->getSkeletonWeight());
            glEnableVertexAttribArray(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneWeightAttributeLoc());
        }
    }
    else
    {
        glDisableVertexAttribArray(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneWeightAttributeLoc());
    }

    int frameindex = mSurface->getCurrentFrameIndex();
    int bipCount = bipedController->oneBipAnimation.size();

    if(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_SkeletalAnimationShaderProgram*)mBaseShaderProgram)->getBoneMatrixUniformLoc(),bipCount,0,&bipedController->AllFrameFinalTransformToShader[frameindex][0]);
    }

}

////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleLightingRenderer)
void SE_SimpleLightingRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_SimpleLightingShaderProgram*)shaderProgram;

}

void SE_SimpleLightingRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);  
    setMaterialData(renderUnit);
    setLightingData(renderUnit);
}

void SE_SimpleLightingRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setMatrix(renderUnit);    
}
void SE_SimpleLightingRenderer::setMaterialData(SE_RenderUnit* renderUnit)
{
    SE_MaterialData* md = mSurface->getMaterialData();
    
    if(md)
    {
        float amb[3];
        amb[0] = md->ambient.x;
        amb[1] = md->ambient.y;
        amb[2] = md->ambient.z;
        
        if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialAmbientUniformLoc() != -1)
        {
            glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialAmbientUniformLoc(), 1, amb);
        }


        float diff[3];
        diff[0] = md->diffuse.x;
        diff[1] = md->diffuse.y;
        diff[2] = md->diffuse.z;
        if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialDiffuseUniformLoc() != -1)
        {
            glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialDiffuseUniformLoc(), 1, diff);
        }
        

        float spec[3];
        spec[0] = md->specular.x;
        spec[1] = md->specular.y;
        spec[2] = md->specular.z;
        if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialSpecularUniformLoc() != -1)
        {
            glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialSpecularUniformLoc(), 1, spec);
        }
        


        float shiness = md->shiny;        
        if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialBiasUniformLoc() != -1)
        {
            glUniform1f(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialBiasUniformLoc(),shiness);
        }

        if(shiness == 1.0)
        {
            shiness = 0.99;
        }
        float shinessIntensity = md->shinessStrength / (1.0 - shiness);
        if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialScaleUniformLoc() != -1)
        {
            glUniform1f(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getMaterialScaleUniformLoc(),shinessIntensity);
        }


    }
    
}

void SE_SimpleLightingRenderer::setVertex(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setVertex(renderUnit);

    _Vector3f* normal = NULL;
    int normalNum = 0;

    mSurface->getFaceVertexNormal(normal,normalNum);
    glVertexAttribPointer(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, normal);
    glEnableVertexAttribArray(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());

}

void SE_SimpleLightingRenderer::setLightingData(SE_RenderUnit* renderUnit)
{   
    SE_Camera *camera = SE_Application::getInstance()->getCurrentCamera();
    SE_Vector3f cameraLocation = camera->getLocation();    

    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();

    std::vector<SE_Light*> dirlights;
    std::vector<SE_Light*> pointlights;
    std::vector<SE_Light*> spotlights;

    SE_Spatial* s = renderUnit->getRenderableSpatial();
    float envBrightness = s->getScene()->getLowestEnvBrightness();
    std::vector < std::string > namelist;
    s->getLightsNameList(namelist);
    for (int i = 0; i < namelist.size(); i++) {
        std::string name = namelist[i];
        SE_Light* l = s->getScene()->getLight(name.c_str());
        if (l) {
            if(l->getLightType() == SE_Light::SE_DIRECTION_LIGHT) {
                dirlights.push_back(l);
            } else if (l->getLightType() == SE_Light::SE_POINT_LIGHT) {
                pointlights.push_back(l);
            } else if (l->getLightType() == SE_Light::SE_SPOT_LIGHT) {
                spotlights.push_back(l);
            }

        }
    }

    for (int i = 0; i < dirlights.size(); ++i) {
        pointlights.push_back(dirlights[i]);
    }


   //point light
    int pointlightNum = pointlights.size();
    {
        float* lightpos = new float[4 * pointlightNum];   
        float* pointAttenuation = new float[3 * pointlightNum];        
        float* lightcolor = new float[3 * pointlightNum];
        float* dirlightstrength = new float[pointlightNum];

        for(int i = 0; i < pointlightNum; ++i)
        {
            SE_Light* light = pointlights[i];        
            if(light->getLightType() == SE_Light::SE_DIRECTION_LIGHT)
            {
                SE_Vector3f dir = light->getLightDir();    
                SE_Matrix3f t = w2m.toMatrix3f();
                SE_Matrix3f inverse = t.inverse();
                SE_Matrix3f inverseT = inverse.transpose();        
                dir = inverseT.map(dir);//model space 
  
                            
                lightpos[4 * i] = dir.x;
                lightpos[4 * i + 1] = dir.y;
                lightpos[4 * i + 2] = dir.z;
                lightpos[4 * i + 3] = 0.0;//dir or point light  
    
                pointAttenuation[3 * i] = 1.0;
                pointAttenuation[3 * i + 1] = 0.0;
                pointAttenuation[3 * i + 2] = 0.0;
                
                dirlightstrength[i] = light->getDirLightStrength();
                    
            }
            if(light->getLightType() == SE_Light::SE_POINT_LIGHT)
            {
                SE_Vector3f lightPos = light->getLightPos();   
                lightPos = w2m.map(SE_Vector4f(lightPos,1.0)).xyz();           
              
                lightpos[4 * i] = lightPos.x;
                lightpos[4 * i + 1] = lightPos.y;
                lightpos[4 * i + 2] = lightPos.z;
                lightpos[4 * i + 3] = 1.0;//dir or point light  
    
                pointAttenuation[3 * i] = light->getConstantAttenuation();
                pointAttenuation[3 * i + 1] = light->getLinearAttenuation();
                pointAttenuation[3 * i + 2] = light->getQuadraticAttenuation();
            }
            
            lightcolor[3 * i] = light->getLightColor().x;
            lightcolor[3 * i + 1] = light->getLightColor().y;
            lightcolor[3 * i + 2] = light->getLightColor().z;
            
            
        }

        if(pointlightNum > 0)
        {
            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getPointLightPosUniformLoc() != -1)
            {
                glUniform4fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getPointLightPosUniformLoc(), pointlightNum, lightpos);
            }

            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getPointAttenuationUniformLoc() != -1)
            {
                glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getPointAttenuationUniformLoc(), pointlightNum, pointAttenuation);
            }

            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getLightColorUniformLoc() != -1)
            {
                glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getLightColorUniformLoc(), pointlightNum, lightcolor);
            }
            
            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getDirLightStrengthUniformLoc() != -1)
            {
                glUniform1fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getDirLightStrengthUniformLoc(), pointlightNum, dirlightstrength);
            }
        }
        delete lightpos;
        delete pointAttenuation;
        delete dirlightstrength;
        delete lightcolor;
    }

    //spot light for pixel light
    int spotlightNum = spotlights.size();
    {
        float* spotlightpos = new float[4 * spotlightNum]; 
        float* spotlightdir = new float[3* spotlightNum];
        float* spotdata = new float[4 * spotlightNum];
        float* spotAttenuation = new float[3 * spotlightNum];         
        float* lightcolor = new float[3 * spotlightNum];
        for(int i = 0; i < spotlightNum; ++i)
        {
            SE_Light* light = spotlights[i];        
            
            SE_Vector3f lightPos = light->getLightPos();    
            if(light->lightCanMove())
            {
                SE_Vector4f lightP(cameraLocation,1.0);
                lightPos = w2m.map(lightP).xyz();  
            }
            else
            {
                SE_Vector4f lightP(lightPos,1.0);            
                lightPos = w2m.map(lightP).xyz();  
            }
          
            spotlightpos[4 * i] = lightPos.x;
            spotlightpos[4 * i + 1] = lightPos.y;
            spotlightpos[4 * i + 2] = lightPos.z;
            spotlightpos[4 * i + 3] = 0.0;//not use now


            //direction light
            SE_Vector3f dir = light->getLightDir();
            SE_Matrix3f t = w2m.toMatrix3f();
            SE_Matrix3f inverse = t.inverse();
            SE_Matrix3f inverseT = inverse.transpose();        
            dir = inverseT.map(dir);//model space        

            spotlightdir[3 * i] = dir.x;
            spotlightdir[3 * i + 1] = dir.y;
            spotlightdir[3 * i + 2] = dir.z;  

            spotdata[4 * i] = light->getSpotLightCutOff();
            spotdata[4 * i + 1] = light->getSpotLightExp();
            spotdata[4 * i + 2] = 0.0;
            spotdata[4 * i + 3] = 0.0;

            spotAttenuation[3 * i] = light->getConstantAttenuation();
            spotAttenuation[3 * i + 1] = light->getLinearAttenuation();
            spotAttenuation[3 * i + 2] = light->getQuadraticAttenuation();
            
            lightcolor[3 * i] = light->getLightColor().x;
            lightcolor[3 * i + 1] = light->getLightColor().y;
            lightcolor[3 * i + 2] = light->getLightColor().z;
            
        }

        if(spotlightNum > 0)
        {
            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightPosUniformLoc() != -1)
            {
                glUniform4fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightPosUniformLoc(), spotlightNum, spotlightpos);
            }
            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotDataUniformLoc() != -1)
            {
                glUniform4fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotDataUniformLoc(), spotlightNum, spotdata);
            }
            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightDirUniformLoc() != -1)
            {
                glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightDirUniformLoc(), spotlightNum, spotlightdir);
            }

            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotAttenuationUniformLoc() != -1)
            {
                glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotAttenuationUniformLoc(), spotlightNum, spotAttenuation);
            }

            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightColorUniformLoc() != -1)
            {
                glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightColorUniformLoc(), spotlightNum, lightcolor);
            }

            if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightNumUniformLoc() != -1)
            {
                glUniform1f(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getSpotLightNumUniformLoc(), spotlightNum);
            }

        }
        delete spotlightpos;
        delete spotlightdir;
        delete spotdata;
        delete spotAttenuation;
        delete lightcolor;
    }
    

#if 0
    float eyepos[3];
    SE_Vector4f eyeV = SE_Vector4f(cameraLocation,1.0);
    SE_Vector4f eyeModel = w2m.map(eyeV);
    
    eyepos[0] = eyeModel.x;
    eyepos[1] = eyeModel.y;
    eyepos[2] = eyeModel.z;

    if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getEyePosUniformLoc() != -1)
    {
        glUniform3fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getEyePosUniformLoc(), 1, eyepos);
    }
#endif


    //push light number and env light
    float lightsNum[4];
    lightsNum[0] = 0;
    lightsNum[1] = pointlightNum;
    lightsNum[2] = 0;
    lightsNum[3] = envBrightness;
    //LOGI("lightsNum.w = %f\n",lightsNum[3]);
    if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getLightsNumUniformLoc() != -1)
    {
        glUniform4fv(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getLightsNumUniformLoc(), 1, lightsNum);
    }

    //need spec light?
    bool needSpec = renderUnit->isNeedSpecLight();
    float need = needSpec ? 1.0 : 0.0;

    if(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getNeedSpecUniformLoc() != -1)
    {
        glUniform1f(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getNeedSpecUniformLoc(), need);
    }

    
    
}
////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleLightingFogRenderer)
void SE_SimpleLightingFogRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{
    SE_SimpleLightingRenderer::setUserStatusBegin(renderUnit);
    //push m2v
    SE_Camera *camera = renderUnit->getRenderableSpatial()->getScene()->getCamera();    
    
    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Matrix4f w2v = camera->getWorldToViewMatrix();
    SE_Matrix4f m2v = m2w.mul(w2v);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc() != -1)
    {
        glUniformMatrix4fv(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc(), 1, 0, matrixData); 
    }

    const float fFogStart = 0.0f;
	const float fFogEnd = 2200.0f;
	const float fFogDensity = 0.002f;
	const float fFogRcpEndStartDiff = 1.0f / (fFogEnd - fFogStart);
	const float afFogColor[3] = { 0.8f, 0.8f, 1.0f }; // the fog RGB color

    if(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc() != -1)
    {
        glUniform1f(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc(), fFogEnd); 
    }

    if(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc() != -1)
    {
        glUniform1f(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc(), fFogRcpEndStartDiff); 
    }

    if(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc() != -1)
    {
        glUniform1f(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc(), fFogDensity); 
    }

    if(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogColor() != -1)
    {
        glUniform3fv(((SE_SimpleLightingFogShaderProgram*)mBaseShaderProgram)->getFogColor(),1, afFogColor); 
    }

}
//////////////////////////////////////

////////////////////////////////////Normal Map////////////////////
IMPLEMENT_OBJECT(SE_NormalMapRenderer)
void SE_NormalMapRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_SimpleLightingRenderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_NormalMapShaderProgram*)shaderProgram;

}

void SE_NormalMapRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleLightingRenderer::setImage(renderUnit);

    glUniform1i(((SE_NormalMapShaderProgram*)mBaseShaderProgram)->getNormalMapUniformLoc(), 1);

    generateTangentSpace(renderUnit);
    
}

void SE_NormalMapRenderer::generateTangentSpace(SE_RenderUnit* renderUnit)
{   
    _Vector3f *tangentArray = NULL;
    int num = 0;
    renderUnit->getSurface()->getVertexTangent(tangentArray,num);

    if(tangentArray)
    {        
        if(((SE_NormalMapShaderProgram*)mBaseShaderProgram)->getTangentSpaceAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_NormalMapShaderProgram*)mBaseShaderProgram)->getTangentSpaceAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, tangentArray);
            glEnableVertexAttribArray(((SE_NormalMapShaderProgram*)mBaseShaderProgram)->getTangentSpaceAttributeLoc());
        }

    }
}

////////////////////////////////////
IMPLEMENT_OBJECT(SE_ParticleRenderer)
void SE_ParticleRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_ParticleShaderProgram*)shaderProgram;

}

void SE_ParticleRenderer::setColor(SE_RenderUnit* renderUnit)
{
    //Do not invoke base::setColor()
}

void SE_ParticleRenderer::setDrawMode(SE_RenderUnit* renderUnit)
{    
    float mode = renderUnit->getSurface()->getDrawMode();
    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc() != -1)
    {
        glUniform1f(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), mode);
    }
}
void SE_ParticleRenderer::setVertexBuffer(SE_RenderUnit* renderUnit)
{
    SE_VertexBuffer *vb = mSurface->getVertexBuffer();

    if(!vb)
    {
        return;
    }

    int vertex_pos_size = vb->mPosSize;
    int vertex_pos_offset = 0;
    //particle no normal data
    //int vertex_normal_size = vb->mNormalSize;

    int vertex_tex0_size = vb->mTex0Size;
    int vertex_tex0_offset = vb->mPosSize;// pos,tex,color

    //int vertex_normal_offset = vb->mPosSize;
    
    int vertex_color_size = vb->mColorSize;
    int vertex_tex0_color_offset = vb->mPosSize + vb->mTex0Size;

    int vertex_size = vb->getDataStride();

    vb->initVBO();
    vb->bindVBO();
    
    if(!vb->vboHasInit())
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), vertex_pos_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData);
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());
        }
        
        if(vb->hasVBState(SE_VertexBuffer::VBS_TEXTURE0))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), vertex_tex0_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_tex0_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
        }

        if(vb->hasVBState(SE_VertexBuffer::VBS_VERTEXCOLOR))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc(), vertex_color_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_tex0_color_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
        }   

        //particle no normal data
        /*if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), vb->vertexData + vertex_normal_offset);
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
            }
        }*/
    }
    else
    {
        //use vbo
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), vertex_pos_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)0);
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());  
        }
        
        if(vb->hasVBState(SE_VertexBuffer::VBS_TEXTURE0))
        {        
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), vertex_tex0_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)(vertex_tex0_offset * sizeof(float)));
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
        }

        if(vb->hasVBState(SE_VertexBuffer::VBS_VERTEXCOLOR))
        {
            if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc() != -1)
            {
                glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc(), vertex_color_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float),(void*)(vertex_tex0_color_offset * sizeof(float)));
                glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
            }
        }
        else
        {
            glDisableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexColorAttributeLoc());
        }  

        //particle no normal data
        /*if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), (void*)(vertex_normal_offset * sizeof(float)));
            glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
        }*/
    }

   
    /*if(mVertexBuffer)
    {
        int vertex_pos_size = mVertexBuffer->mPosSize;
        int vertex_normal_size = mVertexBuffer->mNormalSize;
        int vertex_normal_offset = mVertexBuffer->mPosSize;
        int vertex_size = mVertexBuffer->getDataStride();

        glVertexAttribPointer(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), mVertexBuffer->vertexData + vertex_normal_offset);
        glEnableVertexAttribArray(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());

    }
    else
    {
        _Vector3f* normal = NULL;
        int normalNum = 0;

        mSurface->getFaceVertexNormal(normal,normalNum);
        glVertexAttribPointer(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, normal);
        glEnableVertexAttribArray(((SE_SimpleLightingShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
    }
    */

    mVertexBuffer = vb;
}
void SE_ParticleRenderer::end()
{
    SE_SimpleSurfaceRenderer::end();    
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ShadowMapRenderer)
void SE_ShadowMapRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_ShadowMapShaderProgram*)shaderProgram;
}

void SE_ShadowMapRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setMatrix(renderUnit);   

    SE_Camera* s = NULL;
    if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISWALL))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_WALLSHADOWCAMERA);   
    }
    else if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISGROUND))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_GROUNDSHADOWCAMERA);   
    }
    else
    {
        LOGI("ERROR,shadow render not found camera!!!!\n");
        return;
    }
   
    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));


    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    if(s->getType() != SE_Camera::EPerspective)
	{
		v2p = s->getOrthoMatrix().mul(s->getWorldToViewMatrix());
	}
    SE_Matrix4f m = bias.mul(v2p.mul(renderUnit->getWorldTransform()));
    
    float matrixData[16];
    m.getColumnSequence(matrixData);

    if(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc(), 1, 0, matrixData);
    }

    int usePCF = 1;
    if(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getUsePCFUniformLoc() != -1)
    {
        glUniform1i(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getUsePCFUniformLoc(), usePCF);
    }


    
}

void SE_ShadowMapRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);

    SE_Texture* texture = mSurface->getTexture();
    int texUnitNum = texture->getTexUnitNum();

    if(!texture)
    {
        //not use
        bool needBlur = renderUnit->isNeedUseBluredShadow();

            SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
            SE_RenderTarget* rt = rtm->getShadowTarget();
            SE_RenderTarget* blur = rtm->getBlurHTarget();
            if(rt)
            {
                if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
                {
                    if(needBlur)
                    {
                        SE_ImageData* blurTexture = (SE_ImageData*)blur->getData();
                        SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                        loadTexture2D(0,blurTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                        //fixme:
                        mHasTexture[0].hasTexture = false;
                        mHasTexture[0].uTiling = 1.0;
                        mHasTexture[0].vTiling = 1.0;  
                    }
                    else
                    {
                        //original shadow map
                        SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
                        SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                        loadTexture2D(0,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                        //fixme:
                        mHasTexture[0].hasTexture = false;
                        mHasTexture[0].uTiling = 1.0;
                        mHasTexture[0].vTiling = 1.0;  
                    }
                }

            }

        
    }
    else
    {

        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

        //ISWALL or GROUND
        SE_RenderTarget* rt = NULL;
        if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISWALL))
        {
            rt= rtm->getWallShadowTarget();
        }
        else if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISGROUND))
        {
            rt= rtm->getGroundShadowTarget();
        }
        else
        {
            LOGI("ERROR!!!! Shadow Render is UNKNOWN type!!!\n");
        }            
        

        if(!rt)
        {
            return;
        }

        if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
        {

            bool needBlur = renderUnit->isNeedUseBluredShadow();

            SE_RenderTarget* blur = NULL;
            if(needBlur)
            {
                    //vsm
                    blur = rtm->getBlurHTarget();
                }
                else
                {
                blur = rt;
                }
                SE_ImageData* blurTexture = (SE_ImageData*)blur->getData();
                SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                loadTexture2D(texUnitNum,blurTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                //fixme
                mHasTexture[texUnitNum].hasTexture = true;
                mHasTexture[texUnitNum].uTiling = 1.0;
                mHasTexture[texUnitNum].vTiling = 1.0;  
            
        }
    }


    SE_Texture* t = mSurface->getTexture();

    int imageNum = 0;
    if(t)
    {
        imageNum = t->getTexUnitNum();
        
    }

    if(mHasTexture[imageNum].hasTexture)
    {
        if(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getShadowTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getShadowTextureUniformLoc(), imageNum);  
        }
    }
    if(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getShadowColorUniformLoc() != -1)
    {
        float shadowcolor = renderUnit->getRenderableSpatial()->getShadowColorDensity();
        glUniform1f(((SE_ShadowMapShaderProgram*)mBaseShaderProgram)->getShadowColorUniformLoc(), shadowcolor);  
    }
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ReflectionRenderer)
void SE_ReflectionRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_ReflectionShaderProgram*)shaderProgram;
}

void SE_ReflectionRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setMatrix(renderUnit);   

    //SE_Camera* camerahelper = renderUnit->getHelperCamera(); 

    SE_Camera* c = SE_Application::getInstance()->getCurrentCamera();
   
    SE_Vector3f loc = c->getLocation();
   
    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Vector3f cameraPosInModel = (w2m.map(SE_Vector4f(loc,1.0))).xyz();    

    float camerapos[3];
    camerapos[0] = cameraPosInModel.x;
    camerapos[1] = cameraPosInModel.y;
    camerapos[2] = cameraPosInModel.z;
    
    float matrixData[12];
    m2w.toMatrix3f().getColumnSequence(matrixData);
    if(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getM2WMatrixUniformLoc() != -1)
    {
        glUniformMatrix3fv(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getM2WMatrixUniformLoc(), 1, 0, matrixData); 
    }

    
    if(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getEyePosInModelUniformLoc() != -1)
    {
        glUniform3fv(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getEyePosInModelUniformLoc(), 1,camerapos);
    }

    mVertexBuffer = mSurface->getVertexBuffer();
    if(mVertexBuffer)
    {
        int vertex_pos_size = mVertexBuffer->mPosSize;
        int vertex_normal_size = mVertexBuffer->mNormalSize;
        int vertex_normal_offset = mVertexBuffer->mPosSize;
        int vertex_size = mVertexBuffer->getDataStride();

        if(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), vertex_normal_size, GL_FLOAT, GL_FALSE, vertex_size * sizeof(float), mVertexBuffer->vertexData + vertex_normal_offset);
            glEnableVertexAttribArray(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
        }

    }
    else
    {
        _Vector3f* normal = NULL;
        int normalNum = 0;

        mSurface->getFaceVertexNormal(normal,normalNum);
        if(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc() != -1)
        {
            glVertexAttribPointer(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, normal);
            glEnableVertexAttribArray(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());
        }
    }


    
}

void SE_ReflectionRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);

    SE_Texture* t = mSurface->getTexture();

    int imageNum = 0;
    if(t)
    {
        imageNum = t->getTexUnitNum();
        
    }

    if(mHasTexture[imageNum].hasTexture)
    {
        if(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getReflectionTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_ReflectionShaderProgram*)mBaseShaderProgram)->getReflectionTextureUniformLoc(), imageNum); 
        }
    }

}


//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_MirrorRenderer)
void SE_MirrorRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_MirrorShaderProgram*)shaderProgram;
}

void SE_MirrorRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setMatrix(renderUnit);   

    SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_MIRRORCAMERA);
   
#if 0
    SE_Camera* ca = SE_Application::getInstance()->getCurrentCamera();        

        SE_Vector3f mirrorLocation,mirrorZ,mirrorUp;
        SE_Vector3f location = ca->getLocation();
        SE_Vector3f zAxis = ca->getAxisZ();
        SE_Vector3f up = ca->getAxisY();

        SE_Spatial* ms = renderUnit->getMirrorObject();
        float delta = 0.0;
        if(ms)
        {
            delta = location.z - ms->getCenter().z;
        }
        mirrorLocation = location;
        mirrorLocation.z = location.z - 2 * delta;
       
        mirrorZ = zAxis;
        mirrorZ.z = -zAxis.z;

        mirrorUp = up;
        mirrorUp.z = -up.z;        
        s->create(mirrorLocation, mirrorZ, mirrorUp, 70.0,((float)512)/ 512, 1.0f, 2999.0f);
#endif

    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));


    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    SE_Matrix4f m = bias.mul(v2p.mul(renderUnit->getWorldTransform()));
    
    float matrixData[16];
    m.getColumnSequence(matrixData);

    if(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc(), 1, 0, matrixData); 
    }

        
}

void SE_MirrorRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

    SE_RenderTarget* rt = NULL;
    SE_Texture* texture = mSurface->getTexture();
    

    if(!texture)
    {
        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        SE_Spatial* mirror = renderUnit->getMirrorObject();
        if(mirror)
        {
            SE_RenderTarget* rt = rtm->get(mirror->getSpatialName());
            if(rt)
            {
                if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
                {
                    SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
                    SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                    loadTexture2D(0,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag());
                    mHasTexture[0].hasTexture = false;
                    mHasTexture[0].uTiling = 1.0;
                    mHasTexture[0].vTiling = 1.0;  
                }

            }
        }

    }
    else
    {

        int texUnitNum = texture->getTexUnitNum();
        //render mirror to object
        SE_Spatial* mirror = renderUnit->getRenderableSpatial();
        if(mirror)
        {
            rt = rtm->get(mirror->getSpatialName());
        }

        if(!rt)
        {
            return;
        }

        if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
        {        
            
            SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
            SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
            loadTexture2D(texUnitNum,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
            mHasTexture[texUnitNum].hasTexture = true;
            mHasTexture[texUnitNum].uTiling = 1.0;
            mHasTexture[texUnitNum].vTiling = 1.0; 

        }
    }



    SE_Texture* t = mSurface->getTexture();

    int imageNum = 0;
    if(t)
    {
        imageNum = t->getTexUnitNum();
        
    }

    if(mHasTexture[imageNum].hasTexture)
    {
        if(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getMirrorTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getMirrorTextureUniformLoc(), imageNum);
        }
    }

}

void SE_MirrorRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);
    float mirrordensity = renderUnit->getRenderableSpatial()->getMirrorColorDesity();
    if(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getMirrorDensityUniformLoc() != -1)
    {
        glUniform1f(((SE_MirrorShaderProgram*)mBaseShaderProgram)->getMirrorDensityUniformLoc(), mirrordensity);
    }
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawVSMRenderer)
void SE_DrawVSMRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Camera* s = NULL;

    if(renderUnit->getRenderableSpatial()->isSpatialHasRuntimeAttribute(SE_SpatialAttribute::BELONGTOWALL))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_WALLSHADOWCAMERA);
    }
    else if(renderUnit->getRenderableSpatial()->isSpatialHasRuntimeAttribute(SE_SpatialAttribute::BELONGTOGROUND))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_GROUNDSHADOWCAMERA);
    }
    else
    {
        LOGI("ERROR,shadow generator has no type!!!\n");
        return;
    }        

    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));        
        
		
    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    if(s->getType() != SE_Camera::EPerspective)
	{
		v2p = s->getOrthoMatrix().mul(s->getWorldToViewMatrix());
	}

    SE_Matrix4f m = v2p.mul(renderUnit->getWorldTransform());
        
    float matrixData[16];
    m.getColumnSequence(matrixData);
    if(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData);  
    }
}
void SE_DrawVSMRenderer::setImage(SE_RenderUnit* renderUnit)
{
    
}
void SE_DrawVSMRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{    
    
}

#if 0
//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_BlurHRenderer)
SE_BlurHRenderer::SE_BlurHRenderer()
{
    SE_Vector2f afLowerLeft(-1.0,-1.0);
    SE_Vector2f afUpperRight(1.0,1.0);

    afVertexData[0] = afLowerLeft.x;
    afVertexData[1] = afLowerLeft.y;
    afVertexData[2] = afUpperRight.x;
    afVertexData[3] = afLowerLeft.y;

    afVertexData[4] = afLowerLeft.x;
    afVertexData[5] = afUpperRight.y;
    afVertexData[6] = afUpperRight.x;
    afVertexData[7] = afUpperRight.y;
}
void SE_BlurHRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_BlurHShaderProgram*)shaderProgram;

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
    SE_RenderTarget* blurh = rtm->getBlurHTarget();
    SE_ImageData* texture = (SE_ImageData*)blurh->getData();
    imageSize = texture->getHeight();
}

void SE_BlurHRenderer::setMatrix(SE_RenderUnit* renderUnit)
{      
}

void SE_BlurHRenderer::setImage(SE_RenderUnit* renderUnit)
{
    //directly blur on blurv image
    SE_Renderer::setImage(0,renderUnit);

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexUniformLoc() != -1)
        {
            glUniform1i(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexUniformLoc(), 0);
        }
    }
}

void SE_BlurHRenderer::setColor(SE_RenderUnit *renderUnit)
{
}
void SE_BlurHRenderer::setDrawMode(SE_RenderUnit *renderUnit)
{
}

void SE_BlurHRenderer::setTexVertex(SE_RenderUnit *renderUnit) 
{
}

void SE_BlurHRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{  

     if(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHPosAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHPosAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHPosAttributeLoc());
        mVertexNum = 4;
     }

     if(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexSizeUniformLoc() != -1)
     {
        glUniform1f(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexSizeUniformLoc(), imageSize);
     }

     /*
     if(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexCoordUniformLoc() != -1)
     {
        glVertexAttribPointer(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexCoordUniformLoc(), 2, GL_FLOAT, GL_FALSE, 0, afTexCoordData);
        glEnableVertexAttribArray(((SE_BlurHShaderProgram*)mBaseShaderProgram)->getBlurHTexCoordUniformLoc());
     }
     */
    
    
}

void SE_BlurHRenderer::setVertex(SE_RenderUnit *renderUnit)
{
    
}

void SE_BlurHRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
}

void SE_BlurHRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_BlurVRenderer)
SE_BlurVRenderer::SE_BlurVRenderer()
{
    SE_Vector2f afLowerLeft(-1.0,-1.0);
    SE_Vector2f afUpperRight(1.0,1.0);

    afVertexData[0] = afLowerLeft.x;
    afVertexData[1] = afLowerLeft.y;
    afVertexData[2] = afUpperRight.x;
    afVertexData[3] = afLowerLeft.y;

    afVertexData[4] = afLowerLeft.x;
    afVertexData[5] = afUpperRight.y;
    afVertexData[6] = afUpperRight.x;
    afVertexData[7] = afUpperRight.y;
}
void SE_BlurVRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_BlurVShaderProgram*)shaderProgram;

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
    SE_RenderTarget* blurv = rtm->getBlurVTarget();
    SE_ImageData* texture = (SE_ImageData*)blurv->getData();
    imageSize = texture->getHeight();
}
void SE_BlurVRenderer::setMatrix(SE_RenderUnit* renderUnit)
{      
}
void SE_BlurVRenderer::setImage(SE_RenderUnit* renderUnit)
{
    //directly blur on shadow image
    SE_Renderer::setImage(0,renderUnit);

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVTexUniformLoc() != -1)
        {
            glUniform1i(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVTexUniformLoc(), 0); 
        }
    }
}
void SE_BlurVRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{     

     if(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVPosAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVPosAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVPosAttributeLoc());
        mVertexNum = 4;
     }     

     if(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVTexSizeUniformLoc() != -1)
     {
        glUniform1f(((SE_BlurVShaderProgram*)mBaseShaderProgram)->getBlurVTexSizeUniformLoc(), imageSize);
     }
    
    
}

void SE_BlurVRenderer::setVertex(SE_RenderUnit *renderUnit)
{

}
void SE_BlurVRenderer::setColor(SE_RenderUnit *renderUnit)
{
}
void SE_BlurVRenderer::setDrawMode(SE_RenderUnit *renderUnit)
{
}

void SE_BlurVRenderer::setTexVertex(SE_RenderUnit *renderUnit) 
{
}
void SE_BlurVRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
}

void SE_BlurVRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
}

//////////////////////////////////////////////
#endif
IMPLEMENT_OBJECT(SE_BlurRenderer)
SE_BlurRenderer::SE_BlurRenderer()
{
    SE_Vector2f afLowerLeft(-1.0,-1.0);
    SE_Vector2f afUpperRight(1.0,1.0);

    afVertexData[0] = afLowerLeft.x;
    afVertexData[1] = afLowerLeft.y;
    afVertexData[2] = afUpperRight.x;
    afVertexData[3] = afLowerLeft.y;

    afVertexData[4] = afLowerLeft.x;
    afVertexData[5] = afUpperRight.y;
    afVertexData[6] = afUpperRight.x;
    afVertexData[7] = afUpperRight.y;


    afTexCoordData[0] = 0.0;
    afTexCoordData[1] = 0.0;
    afTexCoordData[2] = 1.0;
    afTexCoordData[3] = 0.0;

    afTexCoordData[4] = 0.0;
    afTexCoordData[5] = 1.0;
    afTexCoordData[6] = 1.0;
    afTexCoordData[7] = 1.0;
}
void SE_BlurRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_BlurShaderProgram*)shaderProgram;

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
    SE_RenderTarget* blur = rtm->getBlurVTarget();
    SE_ImageData* texture = (SE_ImageData*)blur->getData();
    float imgsize = texture->getHeight();

    m_fTexelOffset = 1.0/imgsize;
    float w1 = 0.0555555f;
	float w2 = 0.2777777f;
	float intraTexelOffset = (w1 / (w1 + w2)) * m_fTexelOffset;
	m_fTexelOffset += intraTexelOffset;

}
void SE_BlurRenderer::setMatrix(SE_RenderUnit* renderUnit)
{      
}
void SE_BlurRenderer::setImage(SE_RenderUnit* renderUnit)
{
    //directly blur on shadow image
    //SE_Renderer::setImage(0,renderUnit);

    SE_ImageDataID* imageDataIDArray = NULL;
    int imageDataIDNum = 0;
    SE_ImageData** imageDataArray;
    int imageDataNum;
    bool hasTexture = false;
    float utiling = 1.0;
    float vtiling = 1.0;
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    //for specular
    //renderUnit->getTexImageID(texIndex, imageDataIDArray, imageDataIDNum);
    //renderUnit->getTexImage(texIndex, imageDataArray, imageDataNum);
    

    if(renderUnit->isNeedBlurVShadow())
    {
        //SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        //SE_RenderTarget* shadow = rtm->getShadowTarget();
        SE_RenderTarget* shadow = renderUnit->getSourceRenderTarget();//this should be shadow target.

        //SE_RenderTarget* blurv = rtm->getBlurVTarget();
        SE_RenderTarget* blurv = renderUnit->getRenderTarget();

        SE_ImageData* shadowMapTexture = (SE_ImageData*)shadow->getData();
        SE_TextureTarget* blurVfbo = (SE_TextureTarget*)blurv;
        

        //blur shadow map,so pick up shadow map image,for blurv shader
        loadTexture2D(0,shadowMapTexture,(SE_WRAP_TYPE)blurVfbo->getWrapS(),(SE_WRAP_TYPE)blurVfbo->getWrapT(),(SE_SAMPLE_TYPE)blurVfbo->getSampleMin(),(SE_SAMPLE_TYPE)blurVfbo->getSampleMag());

        mHasTexture[0].hasTexture = true;
        mHasTexture[0].uTiling = 1.0;
        mHasTexture[0].vTiling = 1.0;  
    }
    else if(renderUnit->isNeedBlurHShadow())
    {
        //SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        //SE_RenderTarget* blurv = rtm->getBlurVTarget();
        SE_RenderTarget* blurv = renderUnit->getSourceRenderTarget();//this should be blurV.

        //SE_RenderTarget* blurh = rtm->getBlurHTarget();
        SE_RenderTarget* blurh = renderUnit->getRenderTarget();

        SE_ImageData* blurVTexture = (SE_ImageData*)blurv->getData();
        SE_TextureTarget* blurHfbo = (SE_TextureTarget*)blurh;

        //blur blurv map,so pick up blurv image,for blurh shader
        loadTexture2D(0,blurVTexture,(SE_WRAP_TYPE)blurHfbo->getWrapS(),(SE_WRAP_TYPE)blurHfbo->getWrapT(),(SE_SAMPLE_TYPE)blurHfbo->getSampleMin(),(SE_SAMPLE_TYPE)blurHfbo->getSampleMag());

        mHasTexture[0].hasTexture = true;
        mHasTexture[0].uTiling = 1.0;
        mHasTexture[0].vTiling = 1.0;  
    }

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexUniformLoc() != -1)
        {
            glUniform1i(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexUniformLoc(), 0); 
        }
    }
}
void SE_BlurRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{     

     if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurPosAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurPosAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurPosAttributeLoc());
        mVertexNum = 4;
     }

     if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexCoordAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexCoordAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afTexCoordData);
        glEnableVertexAttribArray(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexCoordAttributeLoc());
     }

     if(renderUnit->isNeedBlurHShadow())
     {
         if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetXUniformLoc() != -1)
         {
            glUniform1f(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetXUniformLoc(), m_fTexelOffset);
         }

         if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetYUniformLoc() != -1)
         {
            glUniform1f(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetYUniformLoc(), 0.0);
         }
     }
     else if(renderUnit->isNeedBlurVShadow())
     {
        if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetYUniformLoc() != -1)
         {
            glUniform1f(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetYUniformLoc(), m_fTexelOffset);
         }

         if(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetXUniformLoc() != -1)
         {
            glUniform1f(((SE_BlurShaderProgram*)mBaseShaderProgram)->getBlurTexelOffsetXUniformLoc(), 0.0);
         }
     }
    
    
}

void SE_BlurRenderer::setVertex(SE_RenderUnit *renderUnit)
{

}
void SE_BlurRenderer::setColor(SE_RenderUnit *renderUnit)
{
}
void SE_BlurRenderer::setDrawMode(SE_RenderUnit *renderUnit)
{
}

void SE_BlurRenderer::setTexVertex(SE_RenderUnit *renderUnit) 
{
}
void SE_BlurRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
}

void SE_BlurRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
}

//////////////////////////////////////////////

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DownSampleRenderer)
SE_DownSampleRenderer::SE_DownSampleRenderer()
{
    SE_Vector2f afLowerLeft(-1.0,-1.0);
    SE_Vector2f afUpperRight(1.0,1.0);

    afVertexData[0] = afLowerLeft.x;
    afVertexData[1] = afLowerLeft.y;
    afVertexData[2] = afUpperRight.x;
    afVertexData[3] = afLowerLeft.y;

    afVertexData[4] = afLowerLeft.x;
    afVertexData[5] = afUpperRight.y;
    afVertexData[6] = afUpperRight.x;
    afVertexData[7] = afUpperRight.y;


    afTexCoordData[0] = 0.0;
    afTexCoordData[1] = 0.0;
    afTexCoordData[2] = 1.0;
    afTexCoordData[3] = 0.0;

    afTexCoordData[4] = 0.0;
    afTexCoordData[5] = 1.0;
    afTexCoordData[6] = 1.0;
    afTexCoordData[7] = 1.0;
}
void SE_DownSampleRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_DownSampleShaderProgram*)shaderProgram;    

}
void SE_DownSampleRenderer::setMatrix(SE_RenderUnit* renderUnit)
{      
}
void SE_DownSampleRenderer::setImage(SE_RenderUnit* renderUnit)
{   

    SE_ImageDataID* imageDataIDArray = NULL;
    int imageDataIDNum = 0;
    SE_ImageData** imageDataArray;
    int imageDataNum;
    bool hasTexture = false;
    float utiling = 1.0;
    float vtiling = 1.0;    

    SE_RenderTarget* sourceTarget = renderUnit->getSourceRenderTarget();

    SE_ImageData* sourceTexture = (SE_ImageData*)sourceTarget->getData();
    SE_TextureTarget* source = (SE_TextureTarget*)sourceTarget;

    //down sample map,so pick up map image,for down sample shader
    loadTexture2D(0,sourceTexture,(SE_WRAP_TYPE)source->getWrapS(),(SE_WRAP_TYPE)source->getWrapT(),(SE_SAMPLE_TYPE)source->getSampleMin(),(SE_SAMPLE_TYPE)source->getSampleMag());

    mHasTexture[0].hasTexture = true;
    mHasTexture[0].uTiling = 1.0;
    mHasTexture[0].vTiling = 1.0;    

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSampleTexUniformLoc() != -1)
        {
            glUniform1i(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSampleTexUniformLoc(), 0); 
        }
    }
}
void SE_DownSampleRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{
    if(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSamplePosAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSamplePosAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSamplePosAttributeLoc());
        mVertexNum = 4;
     }

    if(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSampleTexCoordAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSampleTexCoordAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afTexCoordData);
        glEnableVertexAttribArray(((SE_DownSampleShaderProgram*)mBaseShaderProgram)->getDownSampleTexCoordAttributeLoc());
     }
}

void SE_DownSampleRenderer::setVertex(SE_RenderUnit *renderUnit)
{

}
void SE_DownSampleRenderer::setColor(SE_RenderUnit *renderUnit)
{
}
void SE_DownSampleRenderer::setDrawMode(SE_RenderUnit *renderUnit)
{
}

void SE_DownSampleRenderer::setTexVertex(SE_RenderUnit *renderUnit) 
{
}
void SE_DownSampleRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
}

void SE_DownSampleRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_AlphaTestRenderer)


//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleNoImgRenderer)
SE_SimpleNoImgRenderer::SE_SimpleNoImgRenderer()
{    
}
void SE_SimpleNoImgRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_SimpleNoImgShaderProgram*)shaderProgram;  

}
void SE_SimpleNoImgRenderer::setImage(SE_RenderUnit* renderUnit)
{    
}
void SE_SimpleNoImgRenderer::setVertex(SE_RenderUnit *renderUnit)
{

}
void SE_SimpleNoImgRenderer::setColor(SE_RenderUnit *renderUnit)
{
}
void SE_SimpleNoImgRenderer::setDrawMode(SE_RenderUnit *renderUnit)
{
}

void SE_SimpleNoImgRenderer::setTexVertex(SE_RenderUnit *renderUnit) 
{
}
void SE_SimpleNoImgRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
}

void SE_SimpleNoImgRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleUVAnimationRenderer)
SE_SimpleUVAnimationRenderer::SE_SimpleUVAnimationRenderer()
{    
}
void SE_SimpleUVAnimationRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);

    float* texoffset = new float[2];
    texoffset[0] = renderUnit->getTexCoordOffset().x;
    texoffset[1] = renderUnit->getTexCoordOffset().y;

    if(((SE_SimpleUVAnimationShaderProgram*)mBaseShaderProgram)->getUVTexCoordOffsetUniformLoc() != -1)
    {
        glUniform2fv(((SE_SimpleUVAnimationShaderProgram*)mBaseShaderProgram)->getUVTexCoordOffsetUniformLoc(),1,texoffset);
    }
    delete texoffset;
}


//////////////////////////////////////////////

IMPLEMENT_OBJECT(SE_SimpleUVAnimationFogRenderer)
SE_SimpleUVAnimationFogRenderer::SE_SimpleUVAnimationFogRenderer()
{    
}
void SE_SimpleUVAnimationFogRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    SE_SimpleUVAnimationRenderer::setUserStatusBegin(renderUnit);

    //push m2v
    SE_Camera *camera = renderUnit->getRenderableSpatial()->getScene()->getCamera();    
    
    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Matrix4f w2v = camera->getWorldToViewMatrix();
    SE_Matrix4f m2v = m2w.mul(w2v);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc() != -1)
    {
        glUniformMatrix4fv(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc(), 1, 0, matrixData); 
    }

    const float fFogStart = 0.0f;
	const float fFogEnd = 30000.0f;
	const float fFogDensity = 0.002f;
	const float fFogRcpEndStartDiff = 1.0f / (fFogEnd - fFogStart);
	const float afFogColor[3] = { 0.8f, 0.8f, 1.0f }; // the fog RGB color

    if(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc() != -1)
    {
        glUniform1f(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc(), fFogEnd); 
    }

    if(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc() != -1)
    {
        glUniform1f(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc(), fFogRcpEndStartDiff); 
    }

    if(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc() != -1)
    {
        glUniform1f(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc(), fFogDensity); 
    }

    if(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogColor() != -1)
    {
        glUniform3fv(((SE_SimpleUVAnimationFogShaderProgram*)mBaseShaderProgram)->getFogColor(),1, afFogColor); 
    }
}
//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DofGenRenderer)
SE_DofGenRenderer::SE_DofGenRenderer()
{    
}
void SE_DofGenRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusEnd(renderUnit);
    SE_Vector4f dofpara = SE_Application::getInstance()->getCurrentCamera()->getDofPara();
    float dof_para[4] = {0};
    dof_para[0] = dofpara.x;
    dof_para[1] = dofpara.y;
    dof_para[2] = dofpara.z;
    dof_para[3] = dofpara.w;

    if(((SE_DofGenShaderProgram*)mBaseShaderProgram)->getDofParaUniformLoc() != -1)
    {
        glUniform4fv(((SE_DofGenShaderProgram*)mBaseShaderProgram)->getDofParaUniformLoc(), 1, dof_para); 
    }
}
void SE_DofGenRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);

}
void SE_DofGenRenderer::setMatrix(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setMatrix(renderUnit);

    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Camera* c = SE_Application::getInstance()->getCurrentCamera();
    SE_Matrix4f w2v = c->getWorldToViewMatrix();

    SE_Matrix4f m2v = w2v.mul(m2w);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_DofGenShaderProgram*)mBaseShaderProgram)->getM2VUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_DofGenShaderProgram*)mBaseShaderProgram)->getM2VUniformLoc(), 1, 0, matrixData); 
    }
}

//////////////////////////////////////////////

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DofLightGenRenderer)
SE_DofLightGenRenderer::SE_DofLightGenRenderer()
{    
}
void SE_DofLightGenRenderer::setUserStatusEnd(SE_RenderUnit *renderUnit)
{
    SE_SimpleLightingRenderer::setUserStatusEnd(renderUnit);
    SE_Vector4f dofpara = SE_Application::getInstance()->getCurrentCamera()->getDofPara();
    float dof_para[4] = {0};
    dof_para[0] = dofpara.x;
    dof_para[1] = dofpara.y;
    dof_para[2] = dofpara.z;
    dof_para[3] = dofpara.w;

    if(((SE_DofLightGenShaderProgram*)mBaseShaderProgram)->getDofParaUniformLoc() != -1)
    {
        glUniform4fv(((SE_DofLightGenShaderProgram*)mBaseShaderProgram)->getDofParaUniformLoc(), 1, dof_para); 
    }
}
void SE_DofLightGenRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    SE_SimpleLightingRenderer::setUserStatusBegin(renderUnit);

}
void SE_DofLightGenRenderer::setMatrix(SE_RenderUnit *renderUnit)
{
    SE_SimpleLightingRenderer::setMatrix(renderUnit);

    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Camera* c = SE_Application::getInstance()->getCurrentCamera();
    SE_Matrix4f w2v = c->getWorldToViewMatrix();

    SE_Matrix4f m2v = w2v.mul(m2w);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_DofLightGenShaderProgram*)mBaseShaderProgram)->getM2VUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_DofLightGenShaderProgram*)mBaseShaderProgram)->getM2VUniformLoc(), 1, 0, matrixData); 
    }
}

//////////////////////////////////////////////

IMPLEMENT_OBJECT(SE_DrawDofRenderer)
SE_DrawDofRenderer::SE_DrawDofRenderer()
{
    poisson8samples[0] = 0.0;
    poisson8samples[1] = 0.0;
    poisson8samples[2] = 0.527837;
    poisson8samples[3] = -0.085868;
    poisson8samples[4] = -0.040088;
    poisson8samples[5] = 0.536087;
    poisson8samples[6] = -0.670045;
    poisson8samples[7] = -0.179949;
    poisson8samples[8] = -0.419418;
    poisson8samples[9] = -0.616039;
    poisson8samples[10] = 0.440453;
    poisson8samples[11] = -0.639399;
    poisson8samples[12] = -0.757088;
    poisson8samples[13] = 0.349334;
    poisson8samples[14] = 0.574619;
    poisson8samples[15] = 0.685879;

}
void SE_DrawDofRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getPoissonSampleUniformLoc() != -1)
    {
        glUniform2fv(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getPoissonSampleUniformLoc(), 8,poisson8samples); 
    }
}
void SE_DrawDofRenderer::setImage(SE_RenderUnit *renderUnit)
{
    SE_ImageDataID* imageDataIDArray = NULL;
    int imageDataIDNum = 0;
    SE_ImageData** imageDataArray;
    int imageDataNum;
    bool hasTexture = false;
    float utiling = 1.0;
    float vtiling = 1.0;    

    //bind high res texture
    SE_RenderTarget* sourceTarget = renderUnit->getSourceRenderTarget();

    if(!sourceTarget)
    {
            LOGI("SE_DrawDofRenderer Error !! source target not found!!!\n");

        return;
    }

    SE_ImageData* sourceTexture = (SE_ImageData*)sourceTarget->getData();
    SE_TextureTarget* source = (SE_TextureTarget*)sourceTarget;

    //down sample map,so pick up map image,for down sample shader
    loadTexture2D(0,sourceTexture,(SE_WRAP_TYPE)source->getWrapS(),(SE_WRAP_TYPE)source->getWrapT(),(SE_SAMPLE_TYPE)source->getSampleMin(),(SE_SAMPLE_TYPE)source->getSampleMag());

    mHasTexture[0].hasTexture = true;
    mHasTexture[0].uTiling = 1.0;
    mHasTexture[0].vTiling = 1.0;   

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getHighResTexUniformLoc() != -1)
        {
            glUniform1i(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getHighResTexUniformLoc(), 0); 
        }
    }

    if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getOneOnPixelSizeHighUniformLoc() != -1)
    {
        glUniform2f(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getOneOnPixelSizeHighUniformLoc(), 1.0/sourceTexture->getWidth(),1.0/sourceTexture->getHeight()); 
    }


    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
    //bind low res texture
    SE_RenderTarget* lowResTarget = rtm->getBlurHTarget();

    if(!lowResTarget)
    {
        LOGI("SE_DrawDofRenderer Error !! lowResTarget not found!!!\n");
        return;
    }

    SE_ImageData* lowResTexture = (SE_ImageData*)lowResTarget->getData();
    SE_TextureTarget* lowRes = (SE_TextureTarget*)lowResTarget;

    //down sample map,so pick up map image,for down sample shader
    loadTexture2D(1,lowResTexture,(SE_WRAP_TYPE)lowRes->getWrapS(),(SE_WRAP_TYPE)lowRes->getWrapT(),(SE_SAMPLE_TYPE)lowRes->getSampleMin(),(SE_SAMPLE_TYPE)lowRes->getSampleMag());

    

    mHasTexture[1].hasTexture = true;
    mHasTexture[0].uTiling = 1.0;
    mHasTexture[0].vTiling = 1.0;    

    

    if(mHasTexture[1].hasTexture)
    {
        if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getLowResTexUniformLoc() != -1)
        {
            glUniform1i(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getLowResTexUniformLoc(), 1); 
        }
    }

    if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getOneOnPixelSizeHighUniformLoc() != -1)
    {
        glUniform2f(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getOneOnPixelSizeHighUniformLoc(), 1.0/lowResTexture->getWidth(),1.0/lowResTexture->getHeight()); 
    }
}
void SE_DrawDofRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{
    if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getScenePosAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getScenePosAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getScenePosAttributeLoc());
        mVertexNum = 4;
     }

    if(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getSceneTexCoordAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getSceneTexCoordAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afTexCoordData);
        glEnableVertexAttribArray(((SE_DrawDofShaderProgram*)mBaseShaderProgram)->getSceneTexCoordAttributeLoc());
     }
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_FlagWaveRenderer)
SE_FlagWaveRenderer::SE_FlagWaveRenderer()
{    
}
void SE_FlagWaveRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);
   //for flag effect
    static int time = 0;
    float waveSpeed = 5.0;//0.0 - 300;
    float strength = 1.5;//0.0 - 5.0;
    
    if(((SE_FlagWaveShaderProgram*)mBaseShaderProgram)->getFlagWaveParaUniformLoc() != -1)
    {      

        mFlagWavePara.x = 2*3.1415926*time/360;
        mFlagWavePara.y = waveSpeed;
        mFlagWavePara.z = strength;
        glUniform4fv(((SE_FlagWaveShaderProgram*)mBaseShaderProgram)->getFlagWaveParaUniformLoc(),1,&mFlagWavePara.d[0]);
        
    }    

    time++;
    if(time > 360)
    {
        time -= 360;
    }
}


//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_FlagWaveFogRenderer)
SE_FlagWaveFogRenderer::SE_FlagWaveFogRenderer()
{    
}
void SE_FlagWaveFogRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    
    SE_FlagWaveRenderer::setUserStatusBegin(renderUnit);
   //push m2v
    SE_Camera *camera = renderUnit->getRenderableSpatial()->getScene()->getCamera();    
    
    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();
    SE_Matrix4f w2v = camera->getWorldToViewMatrix();
    SE_Matrix4f m2v = m2w.mul(w2v);

    float matrixData[16];
    m2v.getColumnSequence(matrixData);
    if(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc() != -1)
    {
        glUniformMatrix4fv(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getM2VMatrixLoc(), 1, 0, matrixData); 
    }

    const float fFogStart = 0.0f;
	const float fFogEnd = 30000.0f;
	const float fFogDensity = 0.002f;
	const float fFogRcpEndStartDiff = 1.0f / (fFogEnd - fFogStart);
	const float afFogColor[3] = { 0.8f, 0.8f, 1.0f }; // the fog RGB color

    if(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc() != -1)
    {
        glUniform1f(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogEndLoc(), fFogEnd); 
    }

    if(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc() != -1)
    {
        glUniform1f(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getfogRcpEndStartDiffLoc(), fFogRcpEndStartDiff); 
    }

    if(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc() != -1)
    {
        glUniform1f(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogDensityLoc(), fFogDensity); 
    }

    if(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogColor() != -1)
    {
        glUniform3fv(((SE_FlagWaveFogShaderProgram*)mBaseShaderProgram)->getFogColor(),1, afFogColor); 
    }
}

//////////////////////////////////////////////

IMPLEMENT_OBJECT(SE_CloakFlagWaveRenderer)

///////////////////////////////////
IMPLEMENT_OBJECT(SE_ColorEffectRenderer)
SE_ColorEffectRenderer::SE_ColorEffectRenderer()
{    
}

void SE_ColorEffectRenderer::setImage(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);

    if(mHasTexture[0].hasTexture)
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc(), 0);
        }
    }

    if(mHasTexture[1].hasTexture)
    {
        //push uvtiling when all status
        float uvtiling[2];
        uvtiling[0] = mHasTexture[0].uTiling;
        uvtiling[1] = mHasTexture[0].vTiling;
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc() != -1)
        {
            glUniform2fv(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getUVTilingLoc(), 1, uvtiling);  
        }

        
        if(((SE_ColorEffectShaderProgram*)mBaseShaderProgram)->getSecondTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_ColorEffectShaderProgram*)mBaseShaderProgram)->getSecondTextureUniformLoc(), 1);
            glUniform1f(((SE_ColorEffectShaderProgram*)mBaseShaderProgram)->getShadingModeUniformLoc(), 2.0);
        }        


    }
}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawRenderTargetToScreenRenderer)
SE_DrawRenderTargetToScreenRenderer::SE_DrawRenderTargetToScreenRenderer()
{    
    SE_Vector2f afLowerLeft(-1.0,-1.0);
    SE_Vector2f afUpperRight(1.0,1.0);

    afVertexData[0] = afLowerLeft.x;
    afVertexData[1] = afLowerLeft.y;
    afVertexData[2] = afUpperRight.x;
    afVertexData[3] = afLowerLeft.y;

    afVertexData[4] = afLowerLeft.x;
    afVertexData[5] = afUpperRight.y;
    afVertexData[6] = afUpperRight.x;
    afVertexData[7] = afUpperRight.y;


    afTexCoordData[0] = 0.0;
    afTexCoordData[1] = 0.0;
    afTexCoordData[2] = 1.0;
    afTexCoordData[3] = 0.0;

    afTexCoordData[4] = 0.0;
    afTexCoordData[5] = 1.0;
    afTexCoordData[6] = 1.0;
    afTexCoordData[7] = 1.0;
}

void SE_DrawRenderTargetToScreenRenderer::setImage(SE_RenderUnit *renderUnit)
{
#if 1
    SE_RenderTarget* sourceTarget = renderUnit->getSourceRenderTarget();

    if(!sourceTarget)
    {
        LOGI("SE_DrawRenderTargetToScreenRenderer Error !! source target not found!!!\n");

        return;
    }

    SE_ImageData* sourceTexture = (SE_ImageData*)sourceTarget->getData();
    SE_TextureTarget* source = (SE_TextureTarget*)sourceTarget;

    //so pick up map image,for draw target to screen
    loadTexture2D(0,sourceTexture,(SE_WRAP_TYPE)source->getWrapS(),(SE_WRAP_TYPE)source->getWrapT(),(SE_SAMPLE_TYPE)source->getSampleMin(),(SE_SAMPLE_TYPE)source->getSampleMag());

    mHasTexture[0].hasTexture = true;
    if(mHasTexture[0].hasTexture)
    {
        if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTextureUniformLoc(), 0);
        }

    }

    SE_RenderTarget* sndtarget = SE_Application::getInstance()->getRenderTargetManager()->getColorEffectTarget();
    SE_ImageData* sndTexture = (SE_ImageData*)sndtarget->getData();
    SE_TextureTarget* snd = (SE_TextureTarget*)sndTexture;
    loadTexture2D(1,sndTexture,(SE_WRAP_TYPE)snd->getWrapS(),(SE_WRAP_TYPE)snd->getWrapT(),(SE_SAMPLE_TYPE)snd->getSampleMin(),(SE_SAMPLE_TYPE)snd->getSampleMag());
    mHasTexture[1].hasTexture = true;
    if(mHasTexture[1].hasTexture)
    {
        if(((SE_DrawRenderTargetToScreenShaderProgram*)mBaseShaderProgram)->getSecondTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_DrawRenderTargetToScreenShaderProgram*)mBaseShaderProgram)->getSecondTextureUniformLoc(), 1);
        }

    }
#endif
}

void SE_DrawRenderTargetToScreenRenderer::setVertexBuffer(SE_RenderUnit *renderUnit)
{
    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc() != -1)
     {
         glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afVertexData);
        glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getPositionAttributeLoc());
        mVertexNum = 4;
     }

    if(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc() != -1)
     {
        glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc(), 2, GL_FLOAT, GL_FALSE, 0, afTexCoordData);
        glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getTexCoordAttributeLoc());
     }
}
//////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawDepthRenderer)
void SE_DrawDepthRenderer::setUserStatusBegin(SE_RenderUnit *renderUnit)
{
    SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);
}
void SE_DrawDepthRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);

    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));        
        
		
    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    if(s->getType() != SE_Camera::EPerspective)
	{
		v2p = s->getOrthoMatrix().mul(s->getWorldToViewMatrix());
	}

    SE_Matrix4f m = v2p.mul(renderUnit->getWorldTransform());
        
    float matrixData[16];
    m.getColumnSequence(matrixData);
    if(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData);  
    }
}
/////////////////////////////////////

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_RenderDepthShadowRenderer)
void SE_RenderDepthShadowRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_MirrorShaderProgram*)shaderProgram;
}

void SE_RenderDepthShadowRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Renderer::setMatrix(renderUnit);   

    SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_SHADOWCAMERA);

    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));

    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    if(s->getType() != SE_Camera::EPerspective)
	{
		v2p = s->getOrthoMatrix().mul(s->getWorldToViewMatrix());
	}
    SE_Matrix4f m = bias.mul(v2p.mul(renderUnit->getWorldTransform()));
    
    float matrixData[16];
    m.getColumnSequence(matrixData);

    if(((SE_RenderDepthShadowShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_RenderDepthShadowShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc(), 1, 0, matrixData); 
    }

        
}

void SE_RenderDepthShadowRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

    SE_RenderTarget* rt = NULL;
    SE_Texture* texture = mSurface->getTexture();    

    if(!texture)
    {
        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        SE_Spatial* mirror = renderUnit->getMirrorObject();
        if(mirror)
        {
            SE_RenderTarget* rt = rtm->get(mirror->getSpatialName());
            if(rt)
            {
                if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
                {
                    SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
                    SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                    loadTexture2D(0,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag());
                    mHasTexture[0].hasTexture = false;
                    mHasTexture[0].uTiling = 1.0;
                    mHasTexture[0].vTiling = 1.0;  
                }

            }
        }

    }
    else
    {

        int texUnitNum = texture->getTexUnitNum();        
        
        //ISWALL or GROUND        
        if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISWALL))
        {
            rt= rtm->getWallShadowTarget();
        }
        else if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISGROUND))
        {
            rt= rtm->getGroundShadowTarget();
        }
        else
        {
            LOGI("ERROR!!!! Shadow Render is UNKNOWN type!!!\n");
        }            
        

        if(!rt)
        {
            return;
        }

        if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
        {            
            SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
            SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
            loadTexture2D(texUnitNum,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
            mHasTexture[texUnitNum].hasTexture = true;
            mHasTexture[texUnitNum].uTiling = 1.0;
            mHasTexture[texUnitNum].vTiling = 1.0; 

        }
    }



    SE_Texture* t = mSurface->getTexture();

    int imageNum = 0;
    if(t)
    {
        imageNum = t->getTexUnitNum();
        
    }

    if(mHasTexture[imageNum].hasTexture)
    {
        if(((SE_RenderDepthShadowShaderProgram*)mBaseShaderProgram)->getDepthShadowTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_RenderDepthShadowShaderProgram*)mBaseShaderProgram)->getDepthShadowTextureUniformLoc(), imageNum);
        }
    }

}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawMirrorRenderer)

void SE_DrawMirrorRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_Camera* s = SE_Application::getInstance()->getHelperCamera(SE_MIRRORCAMERA);

    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));


    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    SE_Matrix4f m = v2p.mul(renderUnit->getWorldTransform());
        
    float matrixData[16];
    m.getColumnSequence(matrixData);
    if(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(mBaseShaderProgram->getWorldViewPerspectiveMatrixUniformLoc(), 1, 0, matrixData);  
    }        
}

void SE_DrawMirrorRenderer::setUserStatusBegin(SE_RenderUnit* renderUnit)
{
        SE_SimpleSurfaceRenderer::setUserStatusBegin(renderUnit);

        SE_Camera* ca = SE_Application::getInstance()->getCurrentCamera();        
        SE_Camera* helpca = SE_Application::getInstance()->getHelperCamera(SE_MIRRORCAMERA);

        SE_Vector3f mirrorLocation,mirrorZ,mirrorUp;

        SE_Vector3f location = ca->getLocation();
        SE_Vector3f zAxis = ca->getAxisZ();
        SE_Vector3f up = ca->getAxisY();

        SE_Spatial* m = renderUnit->getMirrorObject();
        float delta = 0.0;
        if(m)
        {
            delta = location.z - m->getCenter().z;
        }

        mirrorLocation = location;
        mirrorLocation.z = location.z - 2 * delta;

        mirrorZ = zAxis;
        mirrorZ.z = -zAxis.z;

        mirrorUp = up;
        mirrorUp.z = -up.z;
        
        helpca->create(mirrorLocation, mirrorZ, mirrorUp, 70.0,((float)512)/ 512, 1.0f, 2999.0f);

}

//////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ShadowMapWithLightRenderer)
void SE_ShadowMapWithLightRenderer::begin(SE_ShaderProgram* shaderProgram)
{
    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_ShadowMapWithLightShaderProgram*)shaderProgram;
}

void SE_ShadowMapWithLightRenderer::setMatrix(SE_RenderUnit* renderUnit)
{
    SE_SimpleLightingRenderer::setMatrix(renderUnit);   

    SE_Camera* s = NULL;
    if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISWALL))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_WALLSHADOWCAMERA);   
    }
    else if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISGROUND))
    {
        s = SE_Application::getInstance()->getHelperCamera(SE_GROUNDSHADOWCAMERA);   
    }
    else
    {
        LOGI("ERROR,shadow render not found camera!!!!\n");
        return;
    }
    //generate bias matrix
    SE_Matrix4f bias;
    SE_Matrix3f identity;
    identity.identity();
    bias.set(identity,SE_Vector3f(0.5,0.5,0.5),SE_Vector3f(0.5,0.5,0.5));


    SE_Matrix4f v2p = s->getPerspectiveMatrix().mul(s->getWorldToViewMatrix());
    if(s->getType() != SE_Camera::EPerspective)
	{
		v2p = s->getOrthoMatrix().mul(s->getWorldToViewMatrix());
	}
    SE_Matrix4f m = bias.mul(v2p.mul(renderUnit->getWorldTransform()));
    
    float matrixData[16];
    m.getColumnSequence(matrixData);

    if(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc() != -1)
    {
        glUniformMatrix4fv(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getTextureProjectionMatrixUniformLoc(), 1, 0, matrixData);
    }

    int usePCF = 1;
    if(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getUsePCFUniformLoc() != -1)
    {
        glUniform1i(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getUsePCFUniformLoc(), usePCF);
    }


    
}

void SE_ShadowMapWithLightRenderer::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleLightingRenderer::setImage(renderUnit);

    SE_Texture* texture = mSurface->getTexture();
    int texUnitNum = texture->getTexUnitNum();

    if(!texture)
    {       
        //not use 
        bool needBlur = renderUnit->isNeedUseBluredShadow();

        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        SE_RenderTarget* rt = rtm->getShadowTarget();
        SE_RenderTarget* blur = rtm->getBlurHTarget();
        if(rt)
        {
            if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
            {
                if(needBlur)
                {
                    SE_ImageData* blurTexture = (SE_ImageData*)blur->getData();
                    SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                    loadTexture2D(0,blurTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                    //fixme:
                    mHasTexture[0].hasTexture = false;
                    mHasTexture[0].uTiling = 1.0;
                    mHasTexture[0].vTiling = 1.0;  
                }
                else
                {
                    //original shadow map
                    SE_ImageData* fboTexture = (SE_ImageData*)rt->getData();
                    SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
                    loadTexture2D(0,fboTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
                    //fixme:
                    mHasTexture[0].hasTexture = false;
                    mHasTexture[0].uTiling = 1.0;
                    mHasTexture[0].vTiling = 1.0;  
                }
            }

        }

        
    }
    else
    {

        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

        //ISWALL or GROUND
        SE_RenderTarget* rt = NULL;
        if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISWALL))
        {
            rt= rtm->getWallShadowTarget();
        }
        else if(renderUnit->getRenderableSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::ISGROUND))
        {
            rt= rtm->getGroundShadowTarget();
        }
        else
        {
            LOGI("ERROR!!!! Shadow Render is UNKNOWN type!!!\n");
        }            
        
        if(!rt)
        {
            return;
        }

        if(rt->getRenderTargetType() == SE_RenderTarget::TEXTURE)
        {

            bool needBlur = renderUnit->isNeedUseBluredShadow();
            
            SE_RenderTarget* blur = NULL;
            if(needBlur)
            {
                //vsm
                blur = rtm->getBlurHTarget();
            }
            else
            {
                blur = rt;
            }
            SE_ImageData* blurTexture = (SE_ImageData*)blur->getData();
            SE_TextureTarget* colorfbo = (SE_TextureTarget*)rt;
            loadTexture2D(texUnitNum,blurTexture,(SE_WRAP_TYPE)colorfbo->getWrapS(),(SE_WRAP_TYPE)colorfbo->getWrapT(),(SE_SAMPLE_TYPE)colorfbo->getSampleMin(),(SE_SAMPLE_TYPE)colorfbo->getSampleMag(),true);
            //fixme
            mHasTexture[texUnitNum].hasTexture = true;
            mHasTexture[texUnitNum].uTiling = 1.0;
            mHasTexture[texUnitNum].vTiling = 1.0;  
            
        }
    }


    SE_Texture* t = mSurface->getTexture();

    int imageNum = 0;
    if(t)
    {
        imageNum = t->getTexUnitNum();
        
    }

    if(mHasTexture[imageNum].hasTexture)
    {
        if(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getShadowTextureUniformLoc() != -1)
        {
            glUniform1i(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getShadowTextureUniformLoc(), imageNum);  
        }
    }
    if(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getShadowColorUniformLoc() != -1)
    {
        float shadowcolor = renderUnit->getRenderableSpatial()->getShadowColorDensity();
        glUniform1f(((SE_ShadowMapWithLightShaderProgram*)mBaseShaderProgram)->getShadowColorUniformLoc(), shadowcolor);  
    }
}
