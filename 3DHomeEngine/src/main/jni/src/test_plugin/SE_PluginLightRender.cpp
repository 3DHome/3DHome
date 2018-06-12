#include<string>
#include<iostream>


#ifdef SE_EXPORT
#undef SE_EXPORT
#define SE_IMPORT
#endif

#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Common.h"
#include "SE_Vector.h"
#include "SE_VertexBuffer.h"
#include "SE_RenderUnit.h"
#include "SE_ShaderProgram.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_Mesh.h"
#include "SE_ShaderProperty.h"
#include "SE_Log.h"
#include "SE_Camera.h"
#include "SE_Renderer.h"
#include "SE_Light.h"
#include "SE_PluginLightRender.h"
#include "SE_PluginLightShader.h"
#include "SE_Light.h"

#include "SE_Log.h"


IMPLEMENT_OBJECT(SE_PluginLightRender)
SE_PluginLightRender::SE_PluginLightRender()
{    
}

SE_PluginLightRender::~SE_PluginLightRender()
{
    
    SE_ObjectFactory::unreg("SE_PluginLightRender");
}

void SE_PluginLightRender::free_render()
{
    delete this;
}

void SE_PluginLightRender::begin(SE_ShaderProgram* shaderProgram)
{

    SE_Renderer::begin(shaderProgram);
    mBaseShaderProgram = (SE_PluginLightShader*)shaderProgram;
}
void SE_PluginLightRender::setImage(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setImage(renderUnit);  
    setMaterialData(renderUnit);
    setLightingData(renderUnit);
}

void SE_PluginLightRender::setMaterialData(SE_RenderUnit* renderUnit)
{
    SE_MaterialData* md = mSurface->getMaterialData();
    float material[3];
    if(md)
    {
        float amb[3];
        amb[0] = md->ambient.x;
        amb[1] = md->ambient.y;
        amb[2] = md->ambient.z;
        
        if(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialAmbientUniformLoc() != -1)
        {
            glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialAmbientUniformLoc(), 1, amb);
        }


        float diff[3];
        diff[0] = md->diffuse.x;
        diff[1] = md->diffuse.y;
        diff[2] = md->diffuse.z;
        if(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialDiffuseUniformLoc() != -1)
        {
            glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialDiffuseUniformLoc(), 1, diff);
        }
        

        float spec[3];
        spec[0] = md->specular.x;
        spec[1] = md->specular.y;
        spec[2] = md->specular.z;
        if(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialSpecularUniformLoc() != -1)
        {
            glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialSpecularUniformLoc(), 1, spec);
        }
        


        float shiness = md->shiny;        
        if(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialShinessUniformLoc() != -1)
        {
            glUniform1f(((SE_PluginLightShader*)mBaseShaderProgram)->getMaterialShinessUniformLoc(),shiness);
        }        

    }
    
}

void SE_PluginLightRender::setLightingData(SE_RenderUnit* renderUnit)
{   
    SE_Camera *camera = SE_Application::getInstance()->getCurrentCamera();
    SE_Vector3f cameraLocation = camera->getLocation();    

    SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
    SE_Matrix4f m2w = renderUnit->getWorldTransform();

    std::vector<SE_Light*> dirlights;
    std::vector<SE_Light*> pointlights;
    std::vector<SE_Light*> spotlights;

    mSurface->getDirLights(dirlights);
    mSurface->getPointLights(pointlights);
    mSurface->getSpotLights(spotlights);
    
    for(int i = 0; i < dirlights.size(); ++i)
    {
        pointlights.push_back(dirlights[i]);
    }

#if 0
    //dir light
    int dirlightNum = dirlights.size();
    {
        float* lightdirs = new float[4 * dirlightNum];    
        float* lightcolor = new float[3 * dirlightNum];
        float* pointAttenuation = new float[3 * dirlightNum];      
        float* dirlightstrength = new float[dirlightNum];
        for(int i = 0; i < dirlightNum; ++i)
        {
            SE_Light* light = dirlights[i];

            //direction light
            SE_Vector3f dir = light->getLightDir();
            SE_Matrix3f t = w2m.toMatrix3f();
            SE_Matrix3f inverse = t.inverse();
            SE_Matrix3f inverseT = inverse.transpose();        
            //dir = inverseT.map(dir);//model space       


#if 1
            lightdirs[3 * i] = dir.x;
            lightdirs[3 * i + 1] = dir.y;
            lightdirs[3 * i + 2] = dir.z;
            lightdirs[3 * i + 3] = 0.0;
            //LOGI("dir.x = %f, y=%f,z=%f\n",dir.x,dir.y,dir.z);
#endif

            pointAttenuation[3 * i] = 1.0;
            pointAttenuation[3 * i + 1] = 0.0;
            pointAttenuation[3 * i + 2] = 0.0;
            
            
            lightcolor[3 * i] = light->getLightColor().x;
            lightcolor[3 * i + 1] = light->getLightColor().y;
            lightcolor[3 * i + 2] = light->getLightColor().z;

            dirlightstrength[i] = light->getDirLightStrength();
            
        }

        if(dirlightNum > 0)
        {
            //glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getDirectionLightDirUniformLoc(), dirlightNum, lightdirs);
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getPointLightPosUniformLoc() != -1)
            {
                //now temp push direction light dir to pointlight pos
                glUniform4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getPointLightPosUniformLoc(), dirlightNum, lightdirs);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc(), dirlightNum, lightcolor);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getDirLightStrengthUniformLoc() != -1)
            {
                glUniform1fv(((SE_PluginLightShader*)mBaseShaderProgram)->getDirLightStrengthUniformLoc(), dirlightNum, dirlightstrength);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getPointAttenuationUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getPointAttenuationUniformLoc(), dirlightNum, pointAttenuation);
            }

        }
        delete lightdirs;
        delete lightcolor;
        delete dirlightstrength;
        delete pointAttenuation;
    }
#endif

    //point light + dir light
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
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getPointLightPosUniformLoc() != -1)
            {
                glUniform4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getPointLightPosUniformLoc(), pointlightNum, lightpos);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getPointAttenuationUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getPointAttenuationUniformLoc(), pointlightNum, pointAttenuation);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc(), pointlightNum, lightcolor);
            }
            
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getDirLightStrengthUniformLoc() != -1)
            {
                glUniform1fv(((SE_PluginLightShader*)mBaseShaderProgram)->getDirLightStrengthUniformLoc(), pointlightNum, dirlightstrength);
            }
        }
        delete lightpos;
        delete pointAttenuation;
        delete dirlightstrength;
        delete lightcolor;
    }

    //TODO: spot light 
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
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotLightPosUniformLoc() != -1)
            {
                glUniform4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotLightPosUniformLoc(), spotlightNum, spotlightpos);
            }
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotDataUniformLoc() != -1)
            {
                glUniform4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotDataUniformLoc(), spotlightNum, spotdata);
            }
            if(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotLightDirUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotLightDirUniformLoc(), spotlightNum, spotlightdir);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotAttenuationUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getSpotAttenuationUniformLoc(), spotlightNum, spotAttenuation);
            }

            if(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc() != -1)
            {
                glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getLightColorUniformLoc(), spotlightNum, lightcolor);
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
    SE_Vector4f eyeV = SE_Vector4f(cameraLocation,1.0);//world pos
    SE_Vector3f eyeInModel = w2m.map(eyeV).xyz();
    
    eyepos[0] = eyeInModel.x;
    eyepos[1] = eyeInModel.y;
    eyepos[2] = eyeInModel.z;

    if(((SE_PluginLightShader*)mBaseShaderProgram)->getEyePosUniformLoc() != -1)
    {
        glUniform3fv(((SE_PluginLightShader*)mBaseShaderProgram)->getEyePosUniformLoc(), 1, eyepos);
    }
#endif

    //push light number and env light
    float lightsNum[4];
    lightsNum[0] = 0;
    lightsNum[1] = pointlightNum;
    lightsNum[2] = spotlightNum;
    lightsNum[3] = mSurface->getEnvBrightness();
    //LOGI("lightsNum.w = %f\n",lightsNum[3]);
    if(((SE_PluginLightShader*)mBaseShaderProgram)->getLightsNumUniformLoc() != -1)
    {
        glUniform4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getLightsNumUniformLoc(), 1, lightsNum);
    }    


    //need spec light?
    bool needSpec = renderUnit->isNeedSpecLight();
    float need = needSpec ? 1.0 : 0.0;

    if(((SE_PluginLightShader*)mBaseShaderProgram)->getNeedSpecUniformLoc() != -1)
    {
        glUniform1f(((SE_PluginLightShader*)mBaseShaderProgram)->getNeedSpecUniformLoc(), need);
    }
    
}
void SE_PluginLightRender::setVertex(SE_RenderUnit* renderUnit)
{
    SE_SimpleSurfaceRenderer::setVertex(renderUnit);

    _Vector3f* normal = NULL;
    int normalNum = 0;

    mSurface->getFaceVertexNormal(normal,normalNum);
    glVertexAttribPointer(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc(), 3, GL_FLOAT, GL_FALSE, 0, normal);
    glEnableVertexAttribArray(((SE_SimpleSurfaceShaderProgram*)mBaseShaderProgram)->getVertexNormalAttributeLoc());

}
void SE_PluginLightRender::setMatrix(SE_RenderUnit* renderUnit)
    {
    SE_SimpleSurfaceRenderer::setMatrix(renderUnit);
#if 0
    if(((SE_PluginLightShader*)mBaseShaderProgram)->getW2MLoc() != -1)
    {
        SE_Matrix4f w2m = renderUnit->getWorldTransform().inverse();
        float matrixData[16];
        w2m.getColumnSequence(matrixData);
        glUniformMatrix4fv(((SE_PluginLightShader*)mBaseShaderProgram)->getW2MLoc(), 1, 0, matrixData); 
    }
#endif
}
