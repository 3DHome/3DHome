#ifndef SE_SHADERPROGRAM_H
#define SE_SHADERPROGRAM_H

#ifdef GLES_20
#include <GLES2/gl2.h>
#endif

class SE_ENTRY SE_ShaderProgram
{
    DECLARE_OBJECT(SE_ShaderProgram)
public:
    SE_ShaderProgram();
    SE_ShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    virtual ~SE_ShaderProgram();
    virtual void free_shader();
    virtual void releaseHardwareResource();
    virtual void create(char* vertexShader, char* fragmentShader);
    virtual void setSource(char* vertexShaderSrc, char* fragmentShaderSrc);
    virtual GLuint getHandler();
    virtual void recreate();
    virtual bool initOK();
    virtual void use();
    GLint getPositionAttributeLoc()
    {
        return m_a_position_loc;
    }
    GLint getWorldViewPerspectiveMatrixUniformLoc()
    {
        return m_u_wvp_matrix_loc;
    }
protected:
    virtual void link();
    virtual void init(char* vertexShaderSrc, char* fragmentShaderSrc);
protected:
    GLuint mShaderProgramObject;
    bool mHasInit;
    GLint m_a_position_loc;
    GLint m_u_wvp_matrix_loc;
    char* mVertexShaderSrc;
    char* mFragmentShaderSrc;
};
class SE_ENTRY SE_SimpleSurfaceShaderProgram : public SE_ShaderProgram
{
    DECLARE_OBJECT(SE_SimpleSurfaceShaderProgram)
public:
    SE_SimpleSurfaceShaderProgram();
    SE_SimpleSurfaceShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleSurfaceShaderProgram();
    virtual GLint getTexCoordAttributeLoc()
    {
        return m_a_tex_coord_loc;
    }
    virtual GLint getTextureUniformLoc()
    {
        return m_u_texture_loc;
    }
    virtual GLint getColorUniformLoc()
    {
        return m_u_color_loc;
    }

    //vertex color
    virtual GLint getVertexColorAttributeLoc()
    {
        return m_u_vertex_color_loc;
    }

    virtual GLint getShadingModeUniformLoc()
    {
        return m_u_shading_mode_loc;
    }
    /*
    virtual GLint getUserFragParaUniformLoc()
    {
        return m_u_frag_para_loc;
    }
    */
    virtual GLint getFragParam()
    {
        return m_u_frag_para_loc;
    }
    virtual GLint getFragAlpha()
    {
        return m_u_frag_alpha_loc;
    }
    virtual GLint getUseUserAlpha()
    {
        return m_UseUserAlpha;
    }
    virtual GLint getUseUserColor()
    {
        return m_UseUserColor;
    }

    virtual GLint getUVTilingLoc()
    {
        return m_u_tex_untiling_loc;
    }
    virtual GLint getNormalLoc()
    {
        return my_normal_loc;
    }

    GLint getVertexNormalAttributeLoc()
    {
        return m_a_normal_loc;
    }

    GLint getReverseTextureLoc()
    {
        return m_u_reverse_texture_loc;
    }

    GLint getUseBWColorLoc()
    {
        return m_u_useBWColor_loc;
    }

    GLint getUserBWColorLoc()
    {
        return m_u_userBWColor_loc;
    }

protected:
    virtual void link();
private:
    GLint m_u_color_loc;
    GLint m_a_tex_coord_loc;
    GLint m_u_texture_loc;
    GLint m_u_shading_mode_loc;
    //single alpha 
    GLint m_u_frag_para_loc;
    GLint m_u_frag_alpha_loc;
    GLint m_UseUserAlpha;
    GLint m_UseUserColor;
    GLint m_u_vertex_color_loc;

    GLint m_u_tex_untiling_loc;
    GLint my_normal_loc;
    GLint m_a_normal_loc;

    GLint m_u_reverse_texture_loc;

    GLint m_u_useBWColor_loc;
    GLint m_u_userBWColor_loc;

   
};

class SE_ENTRY SE_FogShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_FogShaderProgram)
public:
    SE_FogShaderProgram();
    SE_FogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_FogShaderProgram();

    GLint getFogEndLoc()
    {
        return m_u_fogEnd;
    }

    GLint getfogRcpEndStartDiffLoc()
    {
        return m_u_fogRcpEndStartDiff;
    }

    GLint getFogDensityLoc()
    {
        return m_u_FogDensity;
    }

    GLint getM2VMatrixLoc()
    {
        return m_u_M2VMatrix;
    }

    GLint getFogColor()
    {
        return m_u_FogColor;
    }
protected:
    virtual void link();
private:
    GLint m_u_fogEnd;
    GLint m_u_fogRcpEndStartDiff;
    GLint m_u_FogDensity;
    GLint m_u_M2VMatrix;
    GLint m_u_FogColor;
};

class SE_ENTRY SE_DrawMirrorShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DrawMirrorShaderProgram)
public:
    SE_DrawMirrorShaderProgram();
    SE_DrawMirrorShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DrawMirrorShaderProgram();
};
class SE_ENTRY SE_ColorExtractShaderProgram : public SE_ShaderProgram
{
    DECLARE_OBJECT(SE_ColorExtractShaderProgram)
public:
    SE_ColorExtractShaderProgram();
    SE_ColorExtractShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ColorExtractShaderProgram();
    //index start from 0
    GLint getTextureUniformLoc(int index);
    GLint getTexCombineModeUniformLoc()
    {
        return m_u_tex_combine_mode_loc;
    }
    GLint getColorOpModeUniformLoc()
    {
        return m_u_color_op_mode_loc;
    }
    GLint getColorChannelIndexUniformLoc(int index);
    // index start from 1
    GLint getTexCoordIndexUniformLoc(int index);
    GLint getMarkColorUniformLoc(int index);
        // index start from 0
    GLint getTextureCoordAttributeLoc(int index);
protected:
    void link();
private:
    GLint m_a_tex_coord0_loc;
    GLint m_a_tex_coord1_loc;
    GLint m_a_tex_coord2_loc;
    GLint m_a_tex_coord3_loc;
    GLint m_u_texture0_loc;
    GLint m_u_texture1_loc;
    GLint m_u_texture2_loc;
    GLint m_u_texture3_loc;
    GLint m_u_tex_combine_mode_loc;
    GLint m_u_color_op_mode_loc;
    //tex_coord_index indicate which texture coordinate will be used in
    //this texture. for example: m_u_tex0_coord_index is 1 that means texture0 use m_a_tex_coord1
    GLint m_u_tex0_coord_index_loc;
    GLint m_u_tex1_coord_index_loc;
    GLint m_u_tex2_coord_index_loc;
    GLint m_u_tex3_coord_index_loc;
    GLint m_u_colora_loc;
    GLint m_u_colorr_loc;
    GLint m_u_colorg_loc;
    GLint m_u_colorb_loc;
    GLint m_u_rchannelindex_loc;
    GLint m_u_gchannelindex_loc;
    GLint m_u_bchannelindex_loc;
    GLint m_u_achannelindex_loc;
};
class SE_ENTRY SE_ColorEffectShaderProgram1 : public SE_ShaderProgram
{
    DECLARE_OBJECT(SE_ColorEffectShaderProgram1)
public:
    SE_ColorEffectShaderProgram1();
    GLint getTexCoordAttribLoc()
    {
        return m_a_tex_coord0;
    }
    // 0 : background
    // 1 : channel
    // 2 - 5 : texr, texg, texb, texa
    GLint getTextureUniformLoc(int index)
    {
        GLint ret = -1;
        switch(index)
        {
        case 0:
            ret = m_u_texture_background;
            break;
        case 1:
            ret = m_u_texture_channel;
            break;
        case 2:
            ret = m_u_texture_texr;
            break;
        case 3:
            ret = m_u_texture_texg;
            break;
        case 4:
            ret = m_u_texture_texb;
            break;
        case 5:
            ret = m_u_texture_texa;
            break;
        default:
            break;
        }
        return ret;
    }
    // 0, 1, 2, 3 | r g b a
    GLint getHasTextureUniformLoc(int index)
    {
        GLint ret = -1;
        switch(index)
        {
        case 0:
            ret = m_u_has_texr;
            break;
        case 1:
            ret = m_u_has_texg;
            break;
        case 2:
            ret = m_u_has_texb;
            break;
        case 3:
            ret = m_u_has_texa;
            break;
        default:
            break;
        }
        return ret;
    }
    // 0, 1, 2, 3, 4 | markr markg markb marka backgroudnalpha
    GLint getMarkAlphaUniformLoc(int index)
    {
        switch(index)
        {
        case 0:
            return m_u_markr_alpha;
        case 1:
            return m_u_markg_alpha;
        case 2:
            return m_u_markb_alpha;
        case 3:
            return m_u_marka_alpha;
        case 4:
            return m_u_background_alpha;
        default:
            return -1;
        }
    }
    // 0, 1, 2, 3 | r g b a
    GLint getMarkFunctionUniformLoc(int index)
    {
        switch(index)
        {
        case 0:
            return m_u_markr_fn;
        case 1:
            return m_u_markg_fn;
        case 2:
            return m_u_markb_fn;
        case 3:
            return m_u_marka_fn;
        default:
            return -1;
        }
    }
    // 0, 1, 2, 3 | r g b a
    GLint getMarkColorUniformLoc(int index)
    {
        switch(index)
        {
        case 0:
            return m_u_colorr;
        case 1:
            return m_u_colorg;
        case 2:
            return m_u_colorb;
        case 3:
            return m_u_colora;
        default:
            return -1;
        }
    }
protected:
    virtual void link();
private:
    GLint m_a_tex_coord0;
    GLint m_u_texture_background;
    GLint m_u_texture_channel;
    GLint m_u_texture_texr;
    GLint m_u_texture_texg;
    GLint m_u_texture_texb;
    GLint m_u_texture_texa;
    GLint m_u_has_texr;
    GLint m_u_has_texg;
    GLint m_u_has_texb;
    GLint m_u_has_texa;
    GLint m_u_markr_alpha;
    GLint m_u_markg_alpha;
    GLint m_u_markb_alpha;
    GLint m_u_marka_alpha;
    GLint m_u_background_alpha;
    GLint m_u_markr_fn;
    GLint m_u_markg_fn;
    GLint m_u_markb_fn;
    GLint m_u_marka_fn;
    GLint m_u_colorr;
    GLint m_u_colorg;
    GLint m_u_colorb;
    GLint m_u_colora;
};

class SE_ENTRY SE_LightMapShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_LightMapShaderProgram)
public:
    SE_LightMapShaderProgram();
    SE_LightMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_LightMapShaderProgram();

    virtual GLint getSpecularLightMapTextureUniformLoc()
    {
        return m_u_specular_loc;
    }    
    
protected:
    virtual void link();
private:    
    //for specular mpa
    GLint m_u_specular_loc;

};

class SE_ENTRY SE_SkeletalAnimationShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_SkeletalAnimationShaderProgram)
public:
    SE_SkeletalAnimationShaderProgram();
    SE_SkeletalAnimationShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SkeletalAnimationShaderProgram();

/*
    GLint getTexCoordAttributeLoc()
    {
        return m_a_tex_coord_loc;
    }
    GLint getTextureUniformLoc()
    {
        return m_u_texture_loc;
    }
    GLint getColorUniformLoc()
    {
        return m_u_color_loc;
    }
    GLint getShadingModeUniformLoc()
    {
        return m_u_shading_mode_loc;
    }*/

    GLint getAlphaUniformLoc()
    {
        return m_u_alpha_loc;
    }

    GLint getBoneIndexAttributeLoc()
    {
        return m_u_boneindex_loc;
    }
    GLint getBoneWeightAttributeLoc()
    {
        return m_u_boneweight_loc;
    }
    GLint getBoneMatrixUniformLoc()
    {
        return m_u_bonematrix_loc;
    }
    GLint getInversMatrixUniformLoc()
    {
        return m_u_inversmatrix_loc;
    }
protected:
    virtual void link();
private:    

    /*GLint m_u_color_loc;
    GLint m_a_tex_coord_loc;
    GLint m_u_texture_loc;
    GLint m_u_shading_mode_loc;*/
    //single alpha 
    GLint m_u_alpha_loc;

    //debug code
    GLint m_u_boneindex_loc;
    GLint m_u_boneweight_loc;
    GLint m_u_bonematrix_loc;
    GLint m_u_inversmatrix_loc;
};

class SE_ENTRY SE_SimpleLightingShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_SimpleLightingShaderProgram)
public:
    SE_SimpleLightingShaderProgram();
    SE_SimpleLightingShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleLightingShaderProgram();

    GLint getPointLightPosUniformLoc()
    {
        return m_u_point_lightPos_loc;
    }

    GLint getSpotLightPosUniformLoc()
    {
        return m_u_spot_lightPos_loc;
    }

    GLint getDirectionLightDirUniformLoc()
    {
        return m_u_dir_lightDir_loc;
    } 

    GLint getSpotLightDirUniformLoc()
    {
        return m_u_spot_lightDir_loc;
    }

    GLint getMaterialAmbientUniformLoc()
    {
        return m_u_ambient_loc;
    }

    GLint getMaterialDiffuseUniformLoc()
    {
        return m_u_diffuse_loc;
    }

    GLint getMaterialSpecularUniformLoc()
    {
        return m_u_specular_loc;
    }

    GLint getMaterialShinessUniformLoc()
    {
        return m_u_shiness_loc;
    }

    GLint getLightsNumUniformLoc()
    {
        return m_u_lightsNum_loc;
    }

    GLint getMaterialBiasUniformLoc()
    {
        return m_u_material_bias_loc;
    }

    GLint getMaterialScaleUniformLoc()
    {
        return m_u_material_scale;
    }    

    GLint getSpotDataUniformLoc()
    {
        return m_u_spot_data_loc;
    }    

    GLint getPointAttenuationUniformLoc()
    {
        return m_u_point_attenuation3_loc;
    }

    GLint getSpotAttenuationUniformLoc()
    {
        return m_u_spot_attenuation3_loc;
    }

    GLint getDirLightStrengthUniformLoc()
    {
        return m_u_dir_lightStrength_loc;
    }

    GLint getLightColorUniformLoc()
    {
        return m_u_lightColor_loc;
    }

    GLint getNeedSpecUniformLoc()
    {
        return m_u_needSpec_loc;
    }

    GLint getSpotLightColorUniformLoc()
    {
        return m_u_spot_lightColor_loc;
    }

    GLint getSpotLightNumUniformLoc()
    {
        return m_u_spot_lightNum_loc;
    }

protected:
    virtual void link();
private:    

    //light direction 
    GLint m_u_point_lightPos_loc;

    GLint m_u_dir_lightDir_loc;    
    GLint m_u_lightsNum_loc;

    GLint m_u_material_bias_loc;
    GLint m_u_material_scale;
    
    GLint m_u_spot_data_loc;
    GLint m_u_point_attenuation3_loc;
    GLint m_u_spot_attenuation3_loc;
    GLint m_u_spot_lightDir_loc;
    GLint m_u_spot_lightPos_loc;
    GLint m_u_spot_lightNum_loc;
    GLint m_u_spot_lightColor_loc;


    GLint m_u_dir_lightStrength_loc;
    GLint m_u_lightColor_loc;

    GLint m_u_needSpec_loc;

    //material
    GLint m_u_ambient_loc;
    GLint m_u_diffuse_loc;
    GLint m_u_specular_loc;
    GLint m_u_shiness_loc;

};

class SE_ENTRY SE_SimpleLightingFogShaderProgram : public SE_SimpleLightingShaderProgram
{
    DECLARE_OBJECT(SE_SimpleLightingFogShaderProgram)
public:
    SE_SimpleLightingFogShaderProgram();
    SE_SimpleLightingFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleLightingFogShaderProgram();

    GLint getFogEndLoc()
    {
        return m_u_fogEnd;
    }

    GLint getfogRcpEndStartDiffLoc()
    {
        return m_u_fogRcpEndStartDiff;
    }

    GLint getFogDensityLoc()
    {
        return m_u_FogDensity;
    }

    GLint getM2VMatrixLoc()
    {
        return m_u_M2VMatrix;
    }

    GLint getFogColor()
    {
        return m_u_FogColor;
    }
protected:
    virtual void link();
private:
    GLint m_u_fogEnd;
    GLint m_u_fogRcpEndStartDiff;
    GLint m_u_FogDensity;
    GLint m_u_M2VMatrix;
    GLint m_u_FogColor;
};

class SE_ENTRY SE_NormalMapShaderProgram : public SE_SimpleLightingShaderProgram
{
    DECLARE_OBJECT(SE_NormalMapShaderProgram)
public:
    SE_NormalMapShaderProgram();
    SE_NormalMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_NormalMapShaderProgram();
    
    //Normal map
    GLint getTangentSpaceAttributeLoc()
    {
        return m_u_vertex_tangent_loc;
    }

    GLint getNormalMapUniformLoc()
    {
        return m_u_normal_map_loc;
    }
    
protected:
    virtual void link();
private:
    GLint m_u_vertex_tangent_loc;
    GLint m_u_normal_map_loc;
    
};

class SE_ENTRY SE_ParticleShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_ParticleShaderProgram)
public:
    SE_ParticleShaderProgram();
    SE_ParticleShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ParticleShaderProgram();  
    
protected:
    virtual void link();
private:
    
    
};

class SE_ENTRY SE_ShadowMapShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_ShadowMapShaderProgram)
public:
    SE_ShadowMapShaderProgram();
    SE_ShadowMapShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ShadowMapShaderProgram();  

    GLint getTextureProjectionMatrixUniformLoc()
    {
        return m_texture_projection_matrix_loc;
    }

    GLint getShadowTextureUniformLoc()
    {
        return m_shadow_texture_loc;
    }
    
    GLint getUsePCFUniformLoc()
    {
        return m_usePcf_loc;
    }
    
    GLint getUseVSMUniformLoc()
    {
        return m_useVSM_loc;
    }
    
    GLint getShadowColorUniformLoc()
    {
        return m_shadow_color;
    }
    
protected:
    virtual void link();
private:
    GLint m_texture_projection_matrix_loc;
    GLint m_shadow_texture_loc;
    GLint m_usePcf_loc;
    GLint m_useVSM_loc;
    GLint m_shadow_color;
};

class SE_ENTRY SE_ShadowMapWithLightShaderProgram : public SE_SimpleLightingShaderProgram
{
    DECLARE_OBJECT(SE_ShadowMapWithLightShaderProgram)
public:
    SE_ShadowMapWithLightShaderProgram();
    SE_ShadowMapWithLightShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ShadowMapWithLightShaderProgram();  

    GLint getTextureProjectionMatrixUniformLoc()
    {
        return m_texture_projection_matrix_loc;
    }

    GLint getShadowTextureUniformLoc()
    {
        return m_shadow_texture_loc;
    }
    
    GLint getUsePCFUniformLoc()
    {
        return m_usePcf_loc;
    }
    
    GLint getUseVSMUniformLoc()
    {
        return m_useVSM_loc;
    }

    GLint getShadowColorUniformLoc()
    {
        return m_shadow_color;
    }
    
protected:
    virtual void link();
private:
    GLint m_texture_projection_matrix_loc;
    GLint m_shadow_texture_loc;
    GLint m_usePcf_loc;
    GLint m_useVSM_loc;
    GLint m_shadow_color;
};

class SE_ENTRY SE_ReflectionShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_ReflectionShaderProgram)
public:
    SE_ReflectionShaderProgram();
    SE_ReflectionShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ReflectionShaderProgram();  

    GLint getM2WMatrixUniformLoc()
    {
        return m_m2w_matrix_loc;
    }

    GLint getReflectionTextureUniformLoc()
    {
        return m_reflection_texture_loc;
    }    
    GLint getVertexNormalAttributeLoc()
    {
        return m_a_normal_loc;
    }
    GLint getEyePosInModelUniformLoc()
    {
        return m_u_eyePosInModel;
    }
protected:
    virtual void link();
private:
    GLint m_m2w_matrix_loc;
    GLint m_reflection_texture_loc;
    GLint m_a_normal_loc;
    GLint m_u_eyePosInModel;

};

class SE_ENTRY SE_MirrorShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_MirrorShaderProgram)
public:
    SE_MirrorShaderProgram();
    SE_MirrorShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_MirrorShaderProgram();  

    GLint getTextureProjectionMatrixUniformLoc()
    {
        return m_texture_projection_matrix_loc;
    }

    GLint getMirrorTextureUniformLoc()
    {
        return m_mirror_texture_loc;
    }
    
    GLint getMirrorDensityUniformLoc()
    {
        return m_mirror_density_loc;
    }
    
protected:
    virtual void link();
private:
    GLint m_texture_projection_matrix_loc;
    GLint m_mirror_texture_loc;
    GLint m_mirror_density_loc;
};

class SE_ENTRY SE_DrawVSMShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DrawVSMShaderProgram)
public:
    SE_DrawVSMShaderProgram();
    SE_DrawVSMShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DrawVSMShaderProgram();    
    
protected:
    virtual void link();
};

class SE_ENTRY SE_BlurHShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_BlurHShaderProgram)
public:
    SE_BlurHShaderProgram();
    SE_BlurHShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_BlurHShaderProgram();    
    GLint getBlurHPosAttributeLoc()
    {
        return m_blurh_pos_loc;
    }

    GLint getBlurHTexUniformLoc()
    {
        return m_blurh_tex_loc;
    }

    GLint getBlurHTexSizeUniformLoc()
    {
        return m_blurh_tex_size_loc;
    }
protected:
    virtual void link();
    GLint m_blurh_pos_loc;
    GLint m_blurh_tex_loc;
    GLint m_blurh_tex_size_loc;
};

class SE_ENTRY SE_BlurVShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_BlurVShaderProgram)
public:
    SE_BlurVShaderProgram();
    SE_BlurVShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_BlurVShaderProgram();

    GLint getBlurVPosAttributeLoc()
    {
        return m_blurv_pos_loc;
    }

    GLint getBlurVTexUniformLoc()
    {
        return m_blurv_tex_loc;
    }
    
    GLint getBlurVTexSizeUniformLoc()
    {
        return m_blurv_tex_size_loc;
    }
    
protected:
    virtual void link();

private:
    GLint m_blurv_pos_loc;
    GLint m_blurv_tex_loc;
    GLint m_blurv_tex_size_loc;
};

class SE_ENTRY SE_BlurShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_BlurShaderProgram)
public:
    SE_BlurShaderProgram();
    SE_BlurShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_BlurShaderProgram();

    GLint getBlurPosAttributeLoc()
    {
        return m_blur_pos_loc;
    }

    GLint getBlurTexUniformLoc()
    {
        return m_blur_tex_loc;
    }

    GLint getBlurTexCoordAttributeLoc()
    {
        return m_blur_texCoord_loc;
    }

    GLint getBlurTexelOffsetXUniformLoc()
    {
        return m_blur_texelOffsetX_loc;
    }

    GLint getBlurTexelOffsetYUniformLoc()
    {
        return m_blur_texelOffsetY_loc;
    }
    
protected:
    virtual void link();

private:
    GLint m_blur_pos_loc;
    GLint m_blur_tex_loc;

    GLint m_blur_texCoord_loc;
    GLint m_blur_texelOffsetX_loc;
    GLint m_blur_texelOffsetY_loc;
};

class SE_ENTRY SE_DownSampleShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DownSampleShaderProgram)
public:
    SE_DownSampleShaderProgram();
    SE_DownSampleShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DownSampleShaderProgram(); 

    GLint getDownSamplePosAttributeLoc()
    {
        return m_ds_pos_loc;
    }

    GLint getDownSampleTexUniformLoc()
    {
        return m_ds_texture_loc;
    }

    GLint getDownSampleTexCoordAttributeLoc()
    {
        return m_ds_texCoord_loc;
    }
    
protected:
    virtual void link();
private:
    GLint m_ds_pos_loc;
    GLint m_ds_texture_loc;
    GLint m_ds_texCoord_loc;

};

class SE_ENTRY SE_AlphaTestShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_AlphaTestShaderProgram)
public:
    SE_AlphaTestShaderProgram();
    SE_AlphaTestShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_AlphaTestShaderProgram();
    
};

class SE_ENTRY SE_SimpleNoImgShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_SimpleNoImgShaderProgram)
public:
    SE_SimpleNoImgShaderProgram();
    SE_SimpleNoImgShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleNoImgShaderProgram();
    
};

class SE_ENTRY SE_SimpleUVAnimationShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_SimpleUVAnimationShaderProgram)
public:
    SE_SimpleUVAnimationShaderProgram();
    SE_SimpleUVAnimationShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleUVAnimationShaderProgram();

    GLint getUVTexCoordOffsetUniformLoc()
    {
        return m_uv_offset_loc;
    } 
protected:
    virtual void link();
private:
    GLint m_uv_offset_loc;
    
};

class SE_ENTRY SE_SimpleUVAnimationFogShaderProgram : public SE_SimpleUVAnimationShaderProgram
{
    DECLARE_OBJECT(SE_SimpleUVAnimationFogShaderProgram)
public:
    SE_SimpleUVAnimationFogShaderProgram();
    SE_SimpleUVAnimationFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_SimpleUVAnimationFogShaderProgram();

    GLint getFogEndLoc()
    {
        return m_u_fogEnd;
    }

    GLint getfogRcpEndStartDiffLoc()
    {
        return m_u_fogRcpEndStartDiff;
    }

    GLint getFogDensityLoc()
    {
        return m_u_FogDensity;
    }

    GLint getM2VMatrixLoc()
    {
        return m_u_M2VMatrix;
    }

    GLint getFogColor()
    {
        return m_u_FogColor;
    }
protected:
    virtual void link();
private:
    GLint m_u_fogEnd;
    GLint m_u_fogRcpEndStartDiff;
    GLint m_u_FogDensity;
    GLint m_u_M2VMatrix;
    GLint m_u_FogColor;
    
};

class SE_ENTRY SE_DofGenShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DofGenShaderProgram)
public:
    SE_DofGenShaderProgram();
    SE_DofGenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DofGenShaderProgram();

    GLint getM2VUniformLoc()
    {
        return u_m2v_matrix_loc;
    }

    GLint getDofParaUniformLoc()
    {
        return u_dof_para_loc;
    }

    
    
protected:
    virtual void link();
private:
    GLint u_m2v_matrix_loc;
    GLint u_dof_para_loc;

};

class SE_ENTRY SE_DrawDofShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DrawDofShaderProgram)
public:
    SE_DrawDofShaderProgram();
    SE_DrawDofShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DrawDofShaderProgram();

    GLint getScenePosAttributeLoc()
    {
        return m_scene_pos_loc;
    }

    GLint getSceneTexCoordAttributeLoc()
    {
        return m_scene_texCoord_loc;
    }

    GLint getHighResTexUniformLoc()
    {
        return m_source_highres_texture_loc;
    }

    GLint getLowResTexUniformLoc()
    {
        return m_source_lowres_texture_loc;
    }

    GLint getPoissonSampleUniformLoc()
    {
        return m_poisson_sample_loc;
    }

    GLint getOneOnPixelSizeHighUniformLoc()
    {
        return m_oneOnpixelsize_high_loc;
    }

    GLint getOneOnPixelSizeLowUniformLoc()
    {
        return m_oneOnpixelsize_low_loc;
    }    
    
protected:
    virtual void link();
private:
    GLint m_scene_pos_loc;
    
    GLint m_scene_texCoord_loc;

    GLint m_source_highres_texture_loc;
    GLint m_source_lowres_texture_loc;
    GLint m_poisson_sample_loc;
    GLint m_oneOnpixelsize_high_loc;
    GLint m_oneOnpixelsize_low_loc;

};

class SE_ENTRY SE_DofLightGenShaderProgram : public SE_SimpleLightingShaderProgram
{
    DECLARE_OBJECT(SE_DofLightGenShaderProgram)
public:
    SE_DofLightGenShaderProgram();
    SE_DofLightGenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DofLightGenShaderProgram();

    GLint getM2VUniformLoc()
    {
        return u_m2v_matrix_loc;
    }

    GLint getDofParaUniformLoc()
    {
        return u_dof_para_loc;
    }

    
    
protected:
    virtual void link();
private:
    GLint u_m2v_matrix_loc;
    GLint u_dof_para_loc;

};

class SE_ENTRY SE_FlagWaveShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_FlagWaveShaderProgram)
public:
    SE_FlagWaveShaderProgram();
    SE_FlagWaveShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_FlagWaveShaderProgram();

    GLint getFlagWaveParaUniformLoc()
    {
        return m_u_flag_loc;
    }    
    
protected:
    virtual void link();
private:
     GLint m_u_flag_loc;

};

class SE_ENTRY SE_FlagWaveFogShaderProgram : public SE_FlagWaveShaderProgram
{
    DECLARE_OBJECT(SE_FlagWaveFogShaderProgram)
public:
    SE_FlagWaveFogShaderProgram();
    SE_FlagWaveFogShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_FlagWaveFogShaderProgram();        
    
    GLint getFogEndLoc()
    {
        return m_u_fogEnd;
    }

    GLint getfogRcpEndStartDiffLoc()
    {
        return m_u_fogRcpEndStartDiff;
    }

    GLint getFogDensityLoc()
    {
        return m_u_FogDensity;
    }

    GLint getM2VMatrixLoc()
    {
        return m_u_M2VMatrix;
    }

    GLint getFogColor()
    {
        return m_u_FogColor;
    }
protected:
    virtual void link();
private:
    GLint m_u_fogEnd;
    GLint m_u_fogRcpEndStartDiff;
    GLint m_u_FogDensity;
    GLint m_u_M2VMatrix;
    GLint m_u_FogColor;
     

};

class SE_ENTRY SE_CloakFlagWaveShaderProgram : public SE_FlagWaveShaderProgram
{
    DECLARE_OBJECT(SE_CloakFlagWaveShaderProgram)
public:
    SE_CloakFlagWaveShaderProgram();
    SE_CloakFlagWaveShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_CloakFlagWaveShaderProgram();  

};

class SE_ENTRY SE_ColorEffectShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_ColorEffectShaderProgram)
public:
    SE_ColorEffectShaderProgram();
    SE_ColorEffectShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_ColorEffectShaderProgram();

    GLint getSecondTextureUniformLoc()
    {
        return m_u_2ndTex_loc;
    }    
    
protected:
    virtual void link();
private:
     GLint m_u_2ndTex_loc;

};

class SE_ENTRY SE_DrawRenderTargetToScreenShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DrawRenderTargetToScreenShaderProgram)
public:
    SE_DrawRenderTargetToScreenShaderProgram();
    SE_DrawRenderTargetToScreenShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DrawRenderTargetToScreenShaderProgram();

    GLint getSecondTextureUniformLoc()
    {
        return m_u_2ndTex_loc;
    }    
    
protected:
    virtual void link();
private:
     GLint m_u_2ndTex_loc;
};

class SE_ENTRY SE_DrawDepthShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_DrawDepthShaderProgram)
public:
    SE_DrawDepthShaderProgram();
    SE_DrawDepthShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_DrawDepthShaderProgram();     
};

class SE_ENTRY SE_RenderDepthShadowShaderProgram : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_RenderDepthShadowShaderProgram)
public:
    SE_RenderDepthShadowShaderProgram();
    SE_RenderDepthShadowShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_RenderDepthShadowShaderProgram();  

    GLint getTextureProjectionMatrixUniformLoc()
    {
        return m_texture_projection_matrix_loc;
    }

    GLint getDepthShadowTextureUniformLoc()
    {
        return m_depth_texture_loc;
    }
    
protected:
    virtual void link();
private:
    GLint m_texture_projection_matrix_loc;
    GLint m_depth_texture_loc;
};

//class SE_ENTRY SE_LightAndShadowShaderProgram : public SE_SimpleLightingShaderProgram
//{
//    DECLARE_OBJECT(SE_LightAndShadowShaderProgram)
//public:
//    SE_LightAndShadowShaderProgram();
//    SE_LightAndShadowShaderProgram(char* vertexShaderSrc, char* fragmentShaderSrc);
//    ~SE_LightAndShadowShaderProgram();
//
//    GLint getTextureProjectionMatrixUniformLoc()
//    {
//        return m_texture_projection_matrix_loc;
//    }
//
//    GLint getShadowTextureUniformLoc()
//    {
//        return m_shadow_texture_loc;
//    }
//
//    GLint getUsePCFUniformLoc()
//    {
//        return m_usePcf_loc;
//    }
//
//    GLint getUseVSMUniformLoc()
//    {
//        return m_useVSM_loc;
//    }
//    
//protected:
//    virtual void link();
//private:
//    GLint m_texture_projection_matrix_loc;
//    GLint m_shadow_texture_loc;
//    GLint m_usePcf_loc;
//    GLint m_useVSM_loc;
//
//};
#endif
