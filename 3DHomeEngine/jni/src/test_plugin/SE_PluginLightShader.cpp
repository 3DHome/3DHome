
#include<string>
#include<iostream>


#ifdef SE_EXPORT
#undef SE_EXPORT
#define SE_IMPORT
#endif
#define SE_IMPORT

#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Log.h"
#include "SE_ShaderProgram.h"
#include "SE_PluginLightShader.h"


IMPLEMENT_OBJECT(SE_PluginLightShader)
SE_PluginLightShader::SE_PluginLightShader()
{}

SE_PluginLightShader::SE_PluginLightShader(char* vertexShaderSrc, char* fragmentShaderSrc):SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    /* m_a_tex_coord_loc = -1;
    m_u_texture_loc = -1;
    m_u_shading_mode_loc = -1; 
    m_u_color_loc = -1;*/
    m_u_point_lightPos_loc = -1;

    m_u_dir_lightDir_loc = -1;

    m_u_ambient_loc = -1;
    m_u_diffuse_loc = -1;
    m_u_specular_loc = -1;
    m_u_shiness_loc = -1;
    m_u_lightsNum_loc = -1;

    m_u_material_bias_loc = -1;
    m_u_material_scale = -1;

    m_u_spot_data_loc = -1;
    m_u_point_attenuation3_loc = -1;
    m_u_spot_attenuation3_loc = -1;

    m_u_spot_lightPos_loc = -1;
    m_u_spot_lightDir_loc = -1;

    m_u_lightColor_loc = -1;
    m_u_dir_lightStrength_loc = -1;

}

SE_PluginLightShader::~SE_PluginLightShader()
{

    SE_ObjectFactory::unreg("SE_PluginLightShader");
}

void SE_PluginLightShader::free_shader()
{
    delete this;
}

void SE_PluginLightShader::setSource(char* vertexShaderSrc, char* fragmentShaderSrc)
{

    if(mVertexShaderSrc)
        delete[] mVertexShaderSrc;
    if(mFragmentShaderSrc)
        delete[] mFragmentShaderSrc;
    int vertexShaderSrcLen = strlen(vertexShaderSrc);
    int fragmentShaderSrcLen = strlen(fragmentShaderSrc);
    mVertexShaderSrc = new char[vertexShaderSrcLen + 1];
    mFragmentShaderSrc = new char[fragmentShaderSrcLen + 1];
    strncpy(mVertexShaderSrc, vertexShaderSrc, vertexShaderSrcLen);
    strncpy(mFragmentShaderSrc, fragmentShaderSrc, fragmentShaderSrcLen);
    mVertexShaderSrc[vertexShaderSrcLen] = '\0';
    mFragmentShaderSrc[fragmentShaderSrcLen] = '\0';
}

void SE_PluginLightShader::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();
        /*m_a_tex_coord_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord");
    m_u_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_texture");
    m_u_shading_mode_loc = glGetUniformLocation(mShaderProgramObject, "u_shading_mode");
    m_u_color_loc = glGetUniformLocation(mShaderProgramObject, "u_color");*/
    m_u_point_lightPos_loc = glGetUniformLocation(mShaderProgramObject, "u_PointLightPos");
    m_u_dir_lightDir_loc = glGetUniformLocation(mShaderProgramObject, "u_DirectionLightDir"); 

    m_u_spot_lightPos_loc = glGetUniformLocation(mShaderProgramObject, "u_SpotLightPos");
    m_u_spot_lightDir_loc = glGetUniformLocation(mShaderProgramObject, "u_SpotLightDir");


    
    

    m_u_ambient_loc = glGetUniformLocation(mShaderProgramObject, "u_ambient");
    m_u_diffuse_loc = glGetUniformLocation(mShaderProgramObject, "u_diffuse");
    m_u_specular_loc = glGetUniformLocation(mShaderProgramObject, "u_specular");
    m_u_shiness_loc = glGetUniformLocation(mShaderProgramObject, "u_shiness");
    
    m_u_lightsNum_loc = glGetUniformLocation(mShaderProgramObject, "u_lightsNum");
   
    m_u_material_bias_loc = glGetUniformLocation(mShaderProgramObject, "u_material_bias");
    m_u_material_scale = glGetUniformLocation(mShaderProgramObject, "u_material_scale");

    m_u_spot_data_loc = glGetUniformLocation(mShaderProgramObject, "u_spot_data");
    m_u_point_attenuation3_loc = glGetUniformLocation(mShaderProgramObject, "u_distance_point_para");
    m_u_spot_attenuation3_loc = glGetUniformLocation(mShaderProgramObject, "u_distance_spot_para");

    m_u_lightColor_loc = glGetUniformLocation(mShaderProgramObject, "u_light_color");
    m_u_dir_lightStrength_loc = glGetUniformLocation(mShaderProgramObject, "u_dirlight_strength");
    m_u_needSpec_loc = glGetUniformLocation(mShaderProgramObject, "u_needSpec");
#endif
}
