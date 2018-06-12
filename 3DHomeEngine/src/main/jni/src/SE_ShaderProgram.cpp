#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_ShaderProgram.h"
#include "SE_Log.h"
#include <string.h>
#include "SE_MemLeakDetector.h"
#include "SE_Application.h"
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
static GLuint loadShader(GLenum type, const char* shaderSrc)
{
    GLuint shader;
    GLint compiled;
    /*const char* shaderString[] = {shaderSrc, 0};*/
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("#### start load shader ##\n");
    shader = glCreateShader(type);
    if(shader == 0)
        return 0;

    glShaderSource(shader, 1, &shaderSrc, 0);
    checkGLError();
    glCompileShader(shader);
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### compile ok ####\n");
    checkGLError();
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    checkGLError();
    if(!compiled)
    {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        checkGLError();
        if(infoLen > 1)
        {
            char* infoLog = new char[sizeof(char) * infoLen];
            glGetShaderInfoLog(shader, infoLen, 0, infoLog);
            checkGLError();
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("Error compiling shader: \n%s\n", infoLog);
            delete[] infoLog;
        }
        glDeleteShader(shader);
        checkGLError();
        return 0;
    }
    return shader;
}
IMPLEMENT_OBJECT(SE_ShaderProgram)
SE_ShaderProgram::SE_ShaderProgram()
{
    mShaderProgramObject = -1;
    mHasInit = false;
    m_a_position_loc = -1;
    m_u_wvp_matrix_loc = -1;
    mVertexShaderSrc = NULL;
    mFragmentShaderSrc = NULL;
}
SE_ShaderProgram::SE_ShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : mHasInit(false) , mVertexShaderSrc(NULL), mFragmentShaderSrc(NULL)
{
#ifdef GLES_20
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### construct shader program ###");

    init(vertexShaderSrc, fragmentShaderSrc);
    setSource(vertexShaderSrc, fragmentShaderSrc);
#endif
}
void SE_ShaderProgram::setSource(char* vertexShaderSrc, char* fragmentShaderSrc)
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
void SE_ShaderProgram::create(char* vertexShaderSrc, char* fragmentShaderSrc)
{
#ifdef GLES_20
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### create shader program ###");
    init(vertexShaderSrc, fragmentShaderSrc);
    setSource(vertexShaderSrc, fragmentShaderSrc);
#endif
}
GLuint SE_ShaderProgram::getHandler()
{
    return mShaderProgramObject;
}
bool SE_ShaderProgram::initOK()
{
    return mHasInit;
}
void SE_ShaderProgram::releaseHardwareResource()
{
    if(mHasInit)
    {
        glDeleteProgram(mShaderProgramObject);
    }
    mShaderProgramObject = 0;
    mHasInit = false;
}
void SE_ShaderProgram::recreate()
{
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### recreate shader #####\n");
    if(mVertexShaderSrc == NULL)
        return;
    if(mFragmentShaderSrc == NULL)
        return;
    init(mVertexShaderSrc, mFragmentShaderSrc);
}
void SE_ShaderProgram::init(char* vertexShaderSrc, char* fragmentShaderSrc)
{
#ifdef GLES_20
    GLuint vertexShader;
    GLuint fragmentShader;
    GLuint programObject;
    GLint linked;
    vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderSrc);
    if(vertexShader == 0)
    {
        mHasInit = false;
        return;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### load vertex shader OK ###\n");
    fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderSrc);
    if(fragmentShader == 0)
    {
        mHasInit = false;
        return ;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### load fragment shader OK ####\n");
    programObject = glCreateProgram();
    if(programObject == 0)
    {
        mHasInit = false;
        return ;
    }
    glAttachShader(programObject, vertexShader);
    checkGLError();
    glAttachShader(programObject, fragmentShader);
    checkGLError();
    glLinkProgram(programObject);
    checkGLError();
    glGetProgramiv(programObject, GL_LINK_STATUS, &linked);
    checkGLError();
    if(!linked)
    {
        GLint infoLen = 0;
        glGetProgramiv(programObject, GL_INFO_LOG_LENGTH, &infoLen);
        checkGLError();
        if(infoLen > 1)
        {
            char* infoLog = new char[sizeof(char) * infoLen];
            glGetProgramInfoLog(programObject, infoLen, 0, infoLog);
            checkGLError();
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("Error linking program: \n%s\n", infoLog);
            delete[] infoLog;
        }
        glDeleteProgram(programObject);
        checkGLError();
        mHasInit = false;
        return;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### link shader OK ####\n");
    glDeleteShader(vertexShader);
    checkGLError();
    glDeleteShader(fragmentShader);
    checkGLError();
    mShaderProgramObject = programObject;
    mHasInit = true;
#endif
}
void SE_ShaderProgram::use()
{
#ifdef GLES_20
    if(!mHasInit)
        return;
    link();
    glUseProgram(mShaderProgramObject);
    checkGLError();
#endif
}
SE_ShaderProgram::~SE_ShaderProgram()
{
    if(mHasInit)
    {
        glDeleteProgram(mShaderProgramObject);
    }
    if(mVertexShaderSrc)
        delete[] mVertexShaderSrc;
    if(mFragmentShaderSrc)
        delete[] mFragmentShaderSrc;
}

void SE_ShaderProgram::free_shader()
{
    delete this;
}

void SE_ShaderProgram::link()
{
#ifdef GLES_20
    m_a_position_loc = glGetAttribLocation(mShaderProgramObject, "a_position");
    m_u_wvp_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_wvp_matrix");
#ifdef DEBUG0
    LOGI("### m_a_position_loc = %d ###\n", m_a_position_loc);
    LOGI("### m_u_wvp_matrix_loc = %d ###\n", m_u_wvp_matrix_loc);
#endif
#endif
}
/////////////
IMPLEMENT_OBJECT(SE_SimpleSurfaceShaderProgram)
SE_SimpleSurfaceShaderProgram::SE_SimpleSurfaceShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_ShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{}
SE_SimpleSurfaceShaderProgram::~SE_SimpleSurfaceShaderProgram()
{}
SE_SimpleSurfaceShaderProgram::SE_SimpleSurfaceShaderProgram()
{
    m_a_tex_coord_loc = -1;
    m_u_texture_loc = -1;
    m_u_shading_mode_loc = -1; 
    m_u_color_loc = -1;
    m_u_vertex_color_loc = -1;   
    m_u_frag_para_loc = -1;
    m_u_frag_alpha_loc = -1;
    m_UseUserAlpha = -1;
    m_UseUserColor = -1;
    m_u_tex_untiling_loc = -1;
    my_normal_loc = -1;

    m_u_reverse_texture_loc = -1;

    m_u_useBWColor_loc = -1;
    m_u_userBWColor_loc = -1;

}
void SE_SimpleSurfaceShaderProgram::link()
{
#ifdef GLES_20
    SE_ShaderProgram::link();
    m_a_tex_coord_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord");
    m_u_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_texture");
    m_u_shading_mode_loc = glGetUniformLocation(mShaderProgramObject, "u_shading_mode");
    m_u_color_loc = glGetUniformLocation(mShaderProgramObject, "u_color");
    m_u_frag_para_loc = glGetUniformLocation(mShaderProgramObject, "u_frag_para");
    m_u_frag_alpha_loc = glGetUniformLocation(mShaderProgramObject, "u_frag_alpha");
    m_UseUserAlpha = glGetUniformLocation(mShaderProgramObject, "useUserAlpha");
    m_UseUserColor = glGetUniformLocation(mShaderProgramObject, "useUserColor");
    
    //color_texture
    m_u_vertex_color_loc = glGetAttribLocation(mShaderProgramObject, "a_vertex_color");    

    m_u_tex_untiling_loc = glGetUniformLocation(mShaderProgramObject, "u_uvtiling");
    m_a_normal_loc = glGetAttribLocation(mShaderProgramObject,"a_normal");

    m_u_reverse_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_reverse_texture");

    m_u_useBWColor_loc = glGetUniformLocation(mShaderProgramObject, "useBWColor");
    m_u_userBWColor_loc = glGetUniformLocation(mShaderProgramObject, "u_userBWColor");
    
#endif
}
/////////////
/////////////
IMPLEMENT_OBJECT(SE_FogShaderProgram)
SE_FogShaderProgram::SE_FogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_fogEnd = -1;
    m_u_fogRcpEndStartDiff = -1;
    m_u_FogDensity = -1;
    m_u_M2VMatrix = -1;
    m_u_FogColor = -1;
}
SE_FogShaderProgram::~SE_FogShaderProgram()
{}
SE_FogShaderProgram::SE_FogShaderProgram()
{ 

}
void SE_FogShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();
    m_u_fogEnd = glGetUniformLocation(mShaderProgramObject, "u_fogEnd");
    m_u_fogRcpEndStartDiff = glGetUniformLocation(mShaderProgramObject, "u_fogRcpEndStartDiff");
    m_u_FogDensity = glGetUniformLocation(mShaderProgramObject, "u_FogDensity");
    m_u_M2VMatrix = glGetUniformLocation(mShaderProgramObject, "M2VMatrix");
    m_u_FogColor = glGetUniformLocation(mShaderProgramObject, "u_FogColor");
   
#endif
}
////////////////

IMPLEMENT_OBJECT(SE_ColorExtractShaderProgram)
SE_ColorExtractShaderProgram::SE_ColorExtractShaderProgram()
{}
SE_ColorExtractShaderProgram::SE_ColorExtractShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_ShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{}
SE_ColorExtractShaderProgram::~SE_ColorExtractShaderProgram()
{}
void SE_ColorExtractShaderProgram::link()
{
#ifdef GLES_20
    SE_ShaderProgram::link();
    m_a_tex_coord0_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord0");
    m_a_tex_coord1_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord1");
    m_a_tex_coord2_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord2");
    m_a_tex_coord3_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord3");
    m_u_texture0_loc = glGetUniformLocation(mShaderProgramObject, "u_texture0");
    m_u_texture1_loc = glGetUniformLocation(mShaderProgramObject, "u_texture1");
    m_u_texture2_loc = glGetUniformLocation(mShaderProgramObject, "u_texture2");
    m_u_texture3_loc = glGetUniformLocation(mShaderProgramObject, "u_texture3");
    m_u_tex0_coord_index_loc = glGetUniformLocation(mShaderProgramObject, "u_tex0_coord_index");
    m_u_tex1_coord_index_loc = glGetUniformLocation(mShaderProgramObject, "u_tex1_coord_index");
    m_u_tex2_coord_index_loc = glGetUniformLocation(mShaderProgramObject, "u_tex2_coord_index");
    m_u_tex3_coord_index_loc = glGetUniformLocation(mShaderProgramObject, "u_tex3_coord_index");
    m_u_tex_combine_mode_loc = glGetUniformLocation(mShaderProgramObject, "u_tex_combine_mode");
    m_u_color_op_mode_loc = glGetUniformLocation(mShaderProgramObject, "u_color_op_mode");
    m_u_colora_loc = glGetUniformLocation(mShaderProgramObject, "u_colora");
    m_u_colorr_loc = glGetUniformLocation(mShaderProgramObject, "u_colorr");
    m_u_colorg_loc = glGetUniformLocation(mShaderProgramObject, "u_colorg");
    m_u_colorb_loc = glGetUniformLocation(mShaderProgramObject, "u_colorb");
    m_u_rchannelindex_loc = glGetUniformLocation(mShaderProgramObject, "u_rchannelindex");
    m_u_gchannelindex_loc = glGetUniformLocation(mShaderProgramObject, "u_gchannelindex");
    m_u_bchannelindex_loc = glGetUniformLocation(mShaderProgramObject, "u_bchannelindex");
    m_u_achannelindex_loc = glGetUniformLocation(mShaderProgramObject, "u_achannelindex");
#endif
}
GLint SE_ColorExtractShaderProgram::getTextureCoordAttributeLoc(int index)
{
    GLint ret = -1;
    switch(index)
    {
    case 0:
        ret = m_a_tex_coord0_loc;
        break;
    case 1:
        ret = m_a_tex_coord1_loc;
        break;
    case 2:
        ret = m_a_tex_coord2_loc;
        break;
    case 3:
        ret = m_a_tex_coord3_loc;
        break;
    default:
        break;
    }
    return ret;
}
GLint SE_ColorExtractShaderProgram::getTextureUniformLoc(int index)
{
    GLint ret = -1;
    switch(index)
    {
    case 0:
        ret = m_u_texture0_loc;
        break;
    case 1:
        ret = m_u_texture1_loc;
        break;
    case 2:
        ret = m_u_texture2_loc;
        break;
    case 3:
        ret = m_u_texture3_loc;
        break;
    default:
        break;
    }
    return ret;
}
GLint SE_ColorExtractShaderProgram::getTexCoordIndexUniformLoc(int index)
{
    GLint ret = -1;
    switch(index)
    {
    case 0:
        ret = m_u_tex0_coord_index_loc;
        break;
    case 1:
        ret = m_u_tex1_coord_index_loc;
        break;
    case 2:
        ret = m_u_tex2_coord_index_loc;
        break;
    case 3:
        ret = m_u_tex3_coord_index_loc;
        break;
    }
    return ret;
}
GLint SE_ColorExtractShaderProgram::getMarkColorUniformLoc(int index)
{
    GLint ret = -1;
    switch(index)
    {
    case 0:
        ret = m_u_colora_loc;
        break;
    case 1:
        ret = m_u_colorr_loc;
        break;
    case 2:
        ret = m_u_colorg_loc;
        break;
    case 3:
        ret = m_u_colorb_loc;
        break;
    }
    return ret;
}
GLint SE_ColorExtractShaderProgram::getColorChannelIndexUniformLoc(int index)
{
    GLint ret = -1;
    switch(index)
    {
    case 0:
        ret = m_u_rchannelindex_loc;
        break;
    case 1:
        ret = m_u_gchannelindex_loc;
        break;
    case 2:
        ret = m_u_bchannelindex_loc;
        break;
    case 3:
        ret = m_u_achannelindex_loc;
        break;
    }
    return ret;
}
/////////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ColorEffectShaderProgram1)
SE_ColorEffectShaderProgram1::SE_ColorEffectShaderProgram1()
{
    m_a_tex_coord0 = -1;
    m_u_texture_background = -1;
    m_u_texture_channel = -1;
    m_u_texture_texr = -1;
    m_u_texture_texg = -1;
    m_u_texture_texb = -1;
    m_u_texture_texa = -1;
    m_u_has_texr = -1;
    m_u_has_texg = -1;
    m_u_has_texb = -1;
    m_u_has_texa = -1;
    m_u_markr_alpha = -1;
    m_u_markg_alpha = -1;
    m_u_markb_alpha = -1;
    m_u_marka_alpha = -1;
    m_u_background_alpha = -1;
    m_u_markr_fn = -1;
    m_u_markg_fn = -1;
    m_u_markb_fn = -1;
    m_u_marka_fn = -1;
    m_u_colorr = -1;
    m_u_colorg = -1;
    m_u_colorb = -1;
    m_u_colora = -1;
}
void SE_ColorEffectShaderProgram1::link()
{
#ifdef GLES_20
    SE_ShaderProgram::link();
    m_a_tex_coord0 = glGetAttribLocation(mShaderProgramObject, "a_tex_coord0");
    m_u_texture_background = glGetUniformLocation(mShaderProgramObject, "u_texture_background");
    m_u_texture_channel = glGetUniformLocation(mShaderProgramObject, "u_texture_channel");
    m_u_texture_texr = glGetUniformLocation(mShaderProgramObject, "u_texture_texr");
    m_u_texture_texg = glGetUniformLocation(mShaderProgramObject, "u_texture_texg");
    m_u_texture_texb = glGetUniformLocation(mShaderProgramObject, "u_texture_texb");
    m_u_texture_texa = glGetUniformLocation(mShaderProgramObject, "u_texture_texa");
    m_u_has_texr = glGetUniformLocation(mShaderProgramObject, "u_has_texr");
    m_u_has_texg = glGetUniformLocation(mShaderProgramObject, "u_has_texg");
    m_u_has_texb = glGetUniformLocation(mShaderProgramObject, "u_has_texb");
    m_u_has_texa = glGetUniformLocation(mShaderProgramObject, "u_has_texa");
    m_u_markr_alpha = glGetUniformLocation(mShaderProgramObject, "u_markr_alpha");
    m_u_markg_alpha = glGetUniformLocation(mShaderProgramObject, "u_markg_alpha");
    m_u_markb_alpha = glGetUniformLocation(mShaderProgramObject, "u_markb_alpha");
    m_u_marka_alpha = glGetUniformLocation(mShaderProgramObject, "u_marka_alpha");
    m_u_background_alpha = glGetUniformLocation(mShaderProgramObject, "u_background_alpha");
    m_u_markr_fn = glGetUniformLocation(mShaderProgramObject, "u_markr_fn");
    m_u_markg_fn = glGetUniformLocation(mShaderProgramObject, "u_markg_fn");
    m_u_markb_fn = glGetUniformLocation(mShaderProgramObject, "u_markb_fn");
    m_u_marka_fn = glGetUniformLocation(mShaderProgramObject, "u_marka_fn");
    m_u_colorr = glGetUniformLocation(mShaderProgramObject, "u_colorr");
    m_u_colorg = glGetUniformLocation(mShaderProgramObject, "u_colorg");
    m_u_colorb = glGetUniformLocation(mShaderProgramObject, "u_colorb");
    m_u_colora = glGetUniformLocation(mShaderProgramObject, "u_colora");
#endif
}

////////////////////

IMPLEMENT_OBJECT(SE_LightMapShaderProgram)
SE_LightMapShaderProgram::SE_LightMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
   m_u_specular_loc = -1;
}
SE_LightMapShaderProgram::~SE_LightMapShaderProgram()
{}
SE_LightMapShaderProgram::SE_LightMapShaderProgram()
{    
}
void SE_LightMapShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();
    m_u_specular_loc = glGetUniformLocation(mShaderProgramObject, "u_specular_map");
#endif
}
////////////////////////////////////

/////////////
IMPLEMENT_OBJECT(SE_SkeletalAnimationShaderProgram)
SE_SkeletalAnimationShaderProgram::SE_SkeletalAnimationShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    /*m_a_tex_coord_loc = -1;
    m_u_texture_loc = -1;
    m_u_shading_mode_loc = -1; 
    m_u_color_loc = -1;*/
}
SE_SkeletalAnimationShaderProgram::~SE_SkeletalAnimationShaderProgram()
{}
SE_SkeletalAnimationShaderProgram::SE_SkeletalAnimationShaderProgram()
{    
}
void SE_SkeletalAnimationShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();
    //m_a_tex_coord_loc = glGetAttribLocation(mShaderProgramObject, "a_tex_coord");
   // m_u_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_texture");
    //m_u_shading_mode_loc = glGetUniformLocation(mShaderProgramObject, "u_shading_mode");
   // m_u_color_loc = glGetUniformLocation(mShaderProgramObject, "u_color");
    m_u_alpha_loc = glGetUniformLocation(mShaderProgramObject, "u_alpha");


    //debug code
    m_u_boneindex_loc = glGetAttribLocation(mShaderProgramObject, "boneIndex");
    m_u_boneweight_loc = glGetAttribLocation(mShaderProgramObject, "boneWeight");
    m_u_bonematrix_loc =glGetUniformLocation(mShaderProgramObject, "boneMatricesPerFrame");
#endif
}
/////////////

////////////////////

IMPLEMENT_OBJECT(SE_SimpleLightingShaderProgram)
SE_SimpleLightingShaderProgram::SE_SimpleLightingShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
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

    m_u_spot_lightNum_loc = -1;
    m_u_spot_lightColor_loc = -1;

    m_u_spot_lightPos_loc = -1;
    m_u_spot_lightDir_loc = -1;
    
     m_u_lightColor_loc = -1;
    m_u_dir_lightStrength_loc = -1;
    
}
SE_SimpleLightingShaderProgram::~SE_SimpleLightingShaderProgram()
{}
SE_SimpleLightingShaderProgram::SE_SimpleLightingShaderProgram()
{    
}
void SE_SimpleLightingShaderProgram::link()
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

    m_u_spot_lightNum_loc = glGetUniformLocation(mShaderProgramObject, "u_spot_number");
    m_u_spot_lightColor_loc = glGetUniformLocation(mShaderProgramObject, "u_spotlight_color");
#endif
}
////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleLightingFogShaderProgram)
SE_SimpleLightingFogShaderProgram::SE_SimpleLightingFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleLightingShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_fogEnd = -1;
    m_u_fogRcpEndStartDiff = -1;
    m_u_FogDensity = -1;
    m_u_M2VMatrix = -1;
    m_u_FogColor = -1;
}
SE_SimpleLightingFogShaderProgram::~SE_SimpleLightingFogShaderProgram()
{}
SE_SimpleLightingFogShaderProgram::SE_SimpleLightingFogShaderProgram()
{ 

}
void SE_SimpleLightingFogShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleLightingShaderProgram::link();
    m_u_fogEnd = glGetUniformLocation(mShaderProgramObject, "u_fogEnd");
    m_u_fogRcpEndStartDiff = glGetUniformLocation(mShaderProgramObject, "u_fogRcpEndStartDiff");
    m_u_FogDensity = glGetUniformLocation(mShaderProgramObject, "u_FogDensity");
    m_u_M2VMatrix = glGetUniformLocation(mShaderProgramObject, "M2VMatrix");
    m_u_FogColor = glGetUniformLocation(mShaderProgramObject, "u_FogColor");

#endif
}
////////////////////

IMPLEMENT_OBJECT(SE_NormalMapShaderProgram)
SE_NormalMapShaderProgram::SE_NormalMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleLightingShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_vertex_tangent_loc = -1;
    m_u_normal_map_loc = -1;
}
SE_NormalMapShaderProgram::~SE_NormalMapShaderProgram()
{}
SE_NormalMapShaderProgram::SE_NormalMapShaderProgram()
{    
}
void SE_NormalMapShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleLightingShaderProgram::link();    

    //Normal map
    m_u_vertex_tangent_loc = glGetAttribLocation(mShaderProgramObject, "a_vert_tangent");
    m_u_normal_map_loc = glGetUniformLocation(mShaderProgramObject, "u_normal_map");
    
#endif
}
////////////////////////////////////

IMPLEMENT_OBJECT(SE_ParticleShaderProgram)
SE_ParticleShaderProgram::SE_ParticleShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
}
SE_ParticleShaderProgram::~SE_ParticleShaderProgram()
{

}
SE_ParticleShaderProgram::SE_ParticleShaderProgram()
{    
}
void SE_ParticleShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();    
#endif
}
//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ShadowMapShaderProgram)
SE_ShadowMapShaderProgram::SE_ShadowMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_texture_projection_matrix_loc = -1;
    m_shadow_texture_loc = -1;
    m_usePcf_loc = -1;
    m_useVSM_loc = -1;
}
SE_ShadowMapShaderProgram::~SE_ShadowMapShaderProgram()
{}
SE_ShadowMapShaderProgram::SE_ShadowMapShaderProgram()
{    
}
void SE_ShadowMapShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();  

    m_texture_projection_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_texProjection_matrix");;
    m_shadow_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_shadow_texture");
    m_useVSM_loc = glGetUniformLocation(mShaderProgramObject, "useVSM");
    m_usePcf_loc = glGetUniformLocation(mShaderProgramObject, "usePCF");
    m_shadow_color = glGetUniformLocation(mShaderProgramObject, "u_ShadowColor");
#endif
}


//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ShadowMapWithLightShaderProgram)
SE_ShadowMapWithLightShaderProgram::SE_ShadowMapWithLightShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleLightingShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_texture_projection_matrix_loc = -1;
    m_shadow_texture_loc = -1;
    m_usePcf_loc = -1;
    m_useVSM_loc = -1;
}
SE_ShadowMapWithLightShaderProgram::~SE_ShadowMapWithLightShaderProgram()
{}
SE_ShadowMapWithLightShaderProgram::SE_ShadowMapWithLightShaderProgram()
{    
}
void SE_ShadowMapWithLightShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleLightingShaderProgram::link();  

    m_texture_projection_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_texProjection_matrix");;
    m_shadow_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_shadow_texture");
    m_useVSM_loc = glGetUniformLocation(mShaderProgramObject, "useVSM");
    m_usePcf_loc = glGetUniformLocation(mShaderProgramObject, "usePCF");
    m_shadow_color = glGetUniformLocation(mShaderProgramObject, "u_ShadowColor");
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ReflectionShaderProgram)
SE_ReflectionShaderProgram::SE_ReflectionShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_m2w_matrix_loc = -1;
    m_reflection_texture_loc = -1;
    m_a_normal_loc = -1;
    m_u_eyePosInModel = -1;
}
SE_ReflectionShaderProgram::~SE_ReflectionShaderProgram()
{}
SE_ReflectionShaderProgram::SE_ReflectionShaderProgram()
{    
}
void SE_ReflectionShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();  

    m_m2w_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_model2world");;
    m_reflection_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_reflectMap");
    m_u_eyePosInModel = glGetUniformLocation(mShaderProgramObject, "u_eyePosInModel");
    m_a_normal_loc = glGetAttribLocation(mShaderProgramObject,"a_normal");
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_MirrorShaderProgram)
SE_MirrorShaderProgram::SE_MirrorShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_texture_projection_matrix_loc = -1;
    m_mirror_texture_loc = -1;
    m_mirror_density_loc = -1;
}
SE_MirrorShaderProgram::~SE_MirrorShaderProgram()
{}
SE_MirrorShaderProgram::SE_MirrorShaderProgram()
{    
}
void SE_MirrorShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();  

    m_texture_projection_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_texProjection_matrix");;
    m_mirror_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_mirror_texture");
    m_mirror_density_loc = glGetUniformLocation(mShaderProgramObject, "u_mirrordensity");
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawVSMShaderProgram)
SE_DrawVSMShaderProgram::SE_DrawVSMShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
}
SE_DrawVSMShaderProgram::~SE_DrawVSMShaderProgram()
{}
SE_DrawVSMShaderProgram::SE_DrawVSMShaderProgram()
{    
}
void SE_DrawVSMShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();    
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_BlurHShaderProgram)
SE_BlurHShaderProgram::SE_BlurHShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
     m_blurh_pos_loc = -1;
    m_blurh_tex_loc = -1;
    m_blurh_tex_size_loc = -1;
}
SE_BlurHShaderProgram::~SE_BlurHShaderProgram()
{}
SE_BlurHShaderProgram::SE_BlurHShaderProgram()
{    
}
void SE_BlurHShaderProgram::link()
{
#ifdef GLES_20
     m_blurh_pos_loc = glGetAttribLocation(mShaderProgramObject,"a_blurh_pos");
    m_blurh_tex_loc = glGetUniformLocation(mShaderProgramObject,"blurh_texture");
    m_blurh_tex_size_loc = glGetUniformLocation(mShaderProgramObject,"textureh_size");
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_BlurVShaderProgram)
SE_BlurVShaderProgram::SE_BlurVShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_blurv_pos_loc = -1;
    m_blurv_tex_loc = -1;
    m_blurv_tex_size_loc = -1;
}
SE_BlurVShaderProgram::~SE_BlurVShaderProgram()
{}
SE_BlurVShaderProgram::SE_BlurVShaderProgram()
{    
}
void SE_BlurVShaderProgram::link()
{
#ifdef GLES_20
    m_blurv_pos_loc = glGetAttribLocation(mShaderProgramObject,"a_blurv_pos");
    m_blurv_tex_loc = glGetUniformLocation(mShaderProgramObject, "blurv_texture");
    m_blurv_tex_size_loc = glGetUniformLocation(mShaderProgramObject,"texturev_size");
#endif
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_BlurShaderProgram)
SE_BlurShaderProgram::SE_BlurShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_blur_pos_loc = -1;
    m_blur_tex_loc = -1;
    m_blur_texCoord_loc = -1;
    m_blur_texelOffsetX_loc = -1;
    m_blur_texelOffsetY_loc = -1;
}
SE_BlurShaderProgram::~SE_BlurShaderProgram()
{}
SE_BlurShaderProgram::SE_BlurShaderProgram()
{    
}
void SE_BlurShaderProgram::link()
{
#ifdef GLES_20
    m_blur_pos_loc = glGetAttribLocation(mShaderProgramObject,"a_blur_pos");
    m_blur_texCoord_loc = glGetAttribLocation(mShaderProgramObject,"a_blur_texCoord");
    m_blur_texelOffsetX_loc = glGetUniformLocation(mShaderProgramObject, "TexelOffsetX");
    m_blur_texelOffsetY_loc = glGetUniformLocation(mShaderProgramObject, "TexelOffsetY");

    m_blur_tex_loc = glGetUniformLocation(mShaderProgramObject, "blur_texture");
    
#endif
}

//////////////////////////////////////////

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DownSampleShaderProgram)
SE_DownSampleShaderProgram::SE_DownSampleShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_ds_pos_loc = -1;
    m_ds_texture_loc = -1;
    m_ds_texCoord_loc = -1;
}
SE_DownSampleShaderProgram::~SE_DownSampleShaderProgram()
{}
SE_DownSampleShaderProgram::SE_DownSampleShaderProgram()
{    
}
void SE_DownSampleShaderProgram::link()
{
#ifdef GLES_20
    m_ds_pos_loc = glGetAttribLocation(mShaderProgramObject,"ds_position");
    m_ds_texCoord_loc = glGetAttribLocation(mShaderProgramObject,"ds_tex_coord");    
    m_ds_texture_loc = glGetUniformLocation(mShaderProgramObject, "ds_texture");
    
#endif
}
/////////////////////////////////////////////

IMPLEMENT_OBJECT(SE_AlphaTestShaderProgram)
SE_AlphaTestShaderProgram::SE_AlphaTestShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
}
SE_AlphaTestShaderProgram::~SE_AlphaTestShaderProgram()
{}
SE_AlphaTestShaderProgram::SE_AlphaTestShaderProgram()
{    
}
//////////////////////////////////////////

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleNoImgShaderProgram)
SE_SimpleNoImgShaderProgram::SE_SimpleNoImgShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
}
SE_SimpleNoImgShaderProgram::~SE_SimpleNoImgShaderProgram()
{}
SE_SimpleNoImgShaderProgram::SE_SimpleNoImgShaderProgram()
{
}
////////////////////////////////////////////////

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleUVAnimationShaderProgram)
SE_SimpleUVAnimationShaderProgram::SE_SimpleUVAnimationShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_uv_offset_loc = -1;
}
SE_SimpleUVAnimationShaderProgram::~SE_SimpleUVAnimationShaderProgram()
{}
SE_SimpleUVAnimationShaderProgram::SE_SimpleUVAnimationShaderProgram()
{
}

void SE_SimpleUVAnimationShaderProgram::link()
{
    SE_SimpleSurfaceShaderProgram::link();  
    m_uv_offset_loc = glGetUniformLocation(mShaderProgramObject, "u_texcoord_offset");
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_SimpleUVAnimationFogShaderProgram)
SE_SimpleUVAnimationFogShaderProgram::SE_SimpleUVAnimationFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleUVAnimationShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_fogEnd = -1;
    m_u_fogRcpEndStartDiff = -1;
    m_u_FogDensity = -1;
    m_u_M2VMatrix = -1;
    m_u_FogColor = -1;
}
SE_SimpleUVAnimationFogShaderProgram::~SE_SimpleUVAnimationFogShaderProgram()
{}
SE_SimpleUVAnimationFogShaderProgram::SE_SimpleUVAnimationFogShaderProgram()
{
}

void SE_SimpleUVAnimationFogShaderProgram::link()
{
    SE_SimpleUVAnimationShaderProgram::link();  
    m_u_fogEnd = glGetUniformLocation(mShaderProgramObject, "u_fogEnd");
    m_u_fogRcpEndStartDiff = glGetUniformLocation(mShaderProgramObject, "u_fogRcpEndStartDiff");
    m_u_FogDensity = glGetUniformLocation(mShaderProgramObject, "u_FogDensity");
    m_u_M2VMatrix = glGetUniformLocation(mShaderProgramObject, "M2VMatrix");
    m_u_FogColor = glGetUniformLocation(mShaderProgramObject, "u_FogColor");
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DofGenShaderProgram)
SE_DofGenShaderProgram::SE_DofGenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    u_m2v_matrix_loc = -1;
    u_dof_para_loc = -1;
}
SE_DofGenShaderProgram::~SE_DofGenShaderProgram()
{}
SE_DofGenShaderProgram::SE_DofGenShaderProgram()
{
}

void SE_DofGenShaderProgram::link()
{
    SE_SimpleSurfaceShaderProgram::link();  
    u_dof_para_loc = glGetUniformLocation(mShaderProgramObject, "u_dof_para");
    u_m2v_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_m2v_matrix");
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DrawDofShaderProgram)
SE_DrawDofShaderProgram::SE_DrawDofShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
     m_scene_pos_loc = -1;    
     m_scene_texCoord_loc = -1;
     m_source_highres_texture_loc = -1;
     m_source_lowres_texture_loc = -1;
     m_poisson_sample_loc = -1;
     m_oneOnpixelsize_high_loc = -1;
     m_oneOnpixelsize_low_loc = -1;
}
SE_DrawDofShaderProgram::~SE_DrawDofShaderProgram()
{}
SE_DrawDofShaderProgram::SE_DrawDofShaderProgram()
{
}

void SE_DrawDofShaderProgram::link()
{
    m_scene_pos_loc = glGetAttribLocation(mShaderProgramObject,"scene_position");
    m_scene_texCoord_loc = glGetAttribLocation(mShaderProgramObject,"scene_tex_coord");
    m_source_highres_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_texture_source_high");
    m_source_lowres_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_texture_source_low");
    m_poisson_sample_loc = glGetUniformLocation(mShaderProgramObject, "poisson");
    m_oneOnpixelsize_high_loc = glGetUniformLocation(mShaderProgramObject, "OneOnPixelSizeHigh");
    m_oneOnpixelsize_low_loc = glGetUniformLocation(mShaderProgramObject, "OneOnPixelSizeLow");
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_DofLightGenShaderProgram)
SE_DofLightGenShaderProgram::SE_DofLightGenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleLightingShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    u_m2v_matrix_loc = -1;
    u_dof_para_loc = -1;
}
SE_DofLightGenShaderProgram::~SE_DofLightGenShaderProgram()
{}
SE_DofLightGenShaderProgram::SE_DofLightGenShaderProgram()
{
}

void SE_DofLightGenShaderProgram::link()
{
    SE_SimpleLightingShaderProgram::link();  
    u_dof_para_loc = glGetUniformLocation(mShaderProgramObject, "u_dof_para");
    u_m2v_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_m2v_matrix");
}

//////////////////////////////////////////

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_FlagWaveShaderProgram)
SE_FlagWaveShaderProgram::SE_FlagWaveShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_flag_loc = -1;
}
SE_FlagWaveShaderProgram::~SE_FlagWaveShaderProgram()
{}
SE_FlagWaveShaderProgram::SE_FlagWaveShaderProgram()
{
}

void SE_FlagWaveShaderProgram::link()
{
    SE_SimpleSurfaceShaderProgram::link();  
    m_u_flag_loc = glGetUniformLocation(mShaderProgramObject, "u_flagwave_para");
}

//////////////////////////////////////////


//////////////////////////////////////////
//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_FlagWaveFogShaderProgram)
SE_FlagWaveFogShaderProgram::SE_FlagWaveFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_FlagWaveShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_fogEnd = -1;
    m_u_fogRcpEndStartDiff = -1;
    m_u_FogDensity = -1;
    m_u_M2VMatrix = -1;
    m_u_FogColor = -1;
}
SE_FlagWaveFogShaderProgram::~SE_FlagWaveFogShaderProgram()
{}
SE_FlagWaveFogShaderProgram::SE_FlagWaveFogShaderProgram()
{
}

void SE_FlagWaveFogShaderProgram::link()
{
    SE_FlagWaveShaderProgram::link();  
    m_u_fogEnd = glGetUniformLocation(mShaderProgramObject, "u_fogEnd");
    m_u_fogRcpEndStartDiff = glGetUniformLocation(mShaderProgramObject, "u_fogRcpEndStartDiff");
    m_u_FogDensity = glGetUniformLocation(mShaderProgramObject, "u_FogDensity");
    m_u_M2VMatrix = glGetUniformLocation(mShaderProgramObject, "M2VMatrix");
    m_u_FogColor = glGetUniformLocation(mShaderProgramObject, "u_FogColor");
}

//////////////////////////////////////////


//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_CloakFlagWaveShaderProgram)
SE_CloakFlagWaveShaderProgram::SE_CloakFlagWaveShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_FlagWaveShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
}
SE_CloakFlagWaveShaderProgram::~SE_CloakFlagWaveShaderProgram()
{}
SE_CloakFlagWaveShaderProgram::SE_CloakFlagWaveShaderProgram()
{
}

//////////////////////////////////////////
IMPLEMENT_OBJECT(SE_ColorEffectShaderProgram)
SE_ColorEffectShaderProgram::SE_ColorEffectShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_u_2ndTex_loc = -1;
}
SE_ColorEffectShaderProgram::~SE_ColorEffectShaderProgram()
{}
SE_ColorEffectShaderProgram::SE_ColorEffectShaderProgram()
{
}

void SE_ColorEffectShaderProgram::link()
{
    SE_SimpleSurfaceShaderProgram::link();  
    m_u_2ndTex_loc = glGetUniformLocation(mShaderProgramObject, "u_2ndTexture");
}

IMPLEMENT_OBJECT(SE_DrawRenderTargetToScreenShaderProgram)
SE_DrawRenderTargetToScreenShaderProgram::SE_DrawRenderTargetToScreenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{   
}
SE_DrawRenderTargetToScreenShaderProgram::~SE_DrawRenderTargetToScreenShaderProgram()
{}
SE_DrawRenderTargetToScreenShaderProgram::SE_DrawRenderTargetToScreenShaderProgram()
{
}

void SE_DrawRenderTargetToScreenShaderProgram::link()
{
    SE_SimpleSurfaceShaderProgram::link();  
    m_u_2ndTex_loc = glGetUniformLocation(mShaderProgramObject, "u_2ndTexture");
}

IMPLEMENT_OBJECT(SE_DrawMirrorShaderProgram)
SE_DrawMirrorShaderProgram::SE_DrawMirrorShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
}
SE_DrawMirrorShaderProgram::~SE_DrawMirrorShaderProgram()
{}
SE_DrawMirrorShaderProgram::SE_DrawMirrorShaderProgram()
{    
}
//////////////////////////
IMPLEMENT_OBJECT(SE_DrawDepthShaderProgram)
SE_DrawDepthShaderProgram::SE_DrawDepthShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
}
SE_DrawDepthShaderProgram::~SE_DrawDepthShaderProgram()
{}
SE_DrawDepthShaderProgram::SE_DrawDepthShaderProgram()
{    
}
/////////
IMPLEMENT_OBJECT(SE_RenderDepthShadowShaderProgram)
SE_RenderDepthShadowShaderProgram::SE_RenderDepthShadowShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleSurfaceShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{
    m_texture_projection_matrix_loc = -1;
    m_depth_texture_loc = -1;
}
SE_RenderDepthShadowShaderProgram::~SE_RenderDepthShadowShaderProgram()
{}
SE_RenderDepthShadowShaderProgram::SE_RenderDepthShadowShaderProgram()
{    
}

void SE_RenderDepthShadowShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleSurfaceShaderProgram::link();  

    m_texture_projection_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_texProjection_matrix");;
    m_depth_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_depthShadow_texture");
#endif
}

#if 0
IMPLEMENT_OBJECT(SE_LightAndShadowShaderProgram)
SE_LightAndShadowShaderProgram::SE_LightAndShadowShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc) : SE_SimpleLightingShaderProgram(vertexShaderSrc, fragmentShaderSrc)
{    
    m_texture_projection_matrix_loc = -1;
    m_shadow_texture_loc = -1;
    m_usePcf_loc = -1;
    m_useVSM_loc = -1;
}
SE_LightAndShadowShaderProgram::~SE_LightAndShadowShaderProgram()
{}
SE_LightAndShadowShaderProgram::SE_LightAndShadowShaderProgram()
{    
}
void SE_LightAndShadowShaderProgram::link()
{
#ifdef GLES_20
    SE_SimpleLightingShaderProgram::link();  

    m_texture_projection_matrix_loc = glGetUniformLocation(mShaderProgramObject, "u_texProjection_matrix");;
    m_shadow_texture_loc = glGetUniformLocation(mShaderProgramObject, "u_shadow_texture");
    m_useVSM_loc = glGetUniformLocation(mShaderProgramObject, "useVSM");
    m_usePcf_loc = glGetUniformLocation(mShaderProgramObject, "usePCF");
#endif
}
#endif
