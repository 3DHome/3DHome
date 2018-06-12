#ifndef SE_PluginLightShader_H
#define SE_PluginLightShader_H

class SE_PluginLightShader : public SE_SimpleSurfaceShaderProgram
{
    DECLARE_OBJECT(SE_PluginLightShader)
public:
    SE_PluginLightShader();
    SE_PluginLightShader(char* vertexShaderSrc, char* fragmentShaderSrc);
    ~SE_PluginLightShader();

    void setSource(char* vertexShaderSrc, char* fragmentShaderSrc);
    void free_shader();

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



protected:
    virtual void link();
private:

    //light direction 
    GLint m_u_point_lightPos_loc;
    GLint m_u_spot_lightPos_loc;

    GLint m_u_dir_lightDir_loc;
    GLint m_u_spot_lightDir_loc;
    GLint m_u_lightsNum_loc;

    GLint m_u_material_bias_loc;
    GLint m_u_material_scale;


    GLint m_u_spot_data_loc;

    GLint m_u_point_attenuation3_loc;
    GLint m_u_spot_attenuation3_loc;

    GLint m_u_dir_lightStrength_loc;
    GLint m_u_lightColor_loc;

    GLint m_u_needSpec_loc;

    //material
    GLint m_u_ambient_loc;
    GLint m_u_diffuse_loc;
    GLint m_u_specular_loc;
    GLint m_u_shiness_loc;

};

#endif
