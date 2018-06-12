#ifndef SE_PLUGIN_H
#define SE_PLUGIN_H

class SE_ShaderDefine;
class SE_RenderDefine;
class SE_Plugin
{
public:
    enum SE_PluginType
    {
        SE_SHADER_RENDER = 0
    };

    SE_Plugin(){}
    virtual ~SE_Plugin(){};
    virtual const char* getPluginName() = 0;
    virtual const char* getPluginVersion() = 0;
    virtual int getPluginType() = 0;
    virtual void unLoadPlugin() = 0;

};

class SE_ShaderPlugin : public SE_Plugin
{
public:
    virtual SE_ShaderDefine* getPluginShaderDefine() = 0;
    virtual SE_RenderDefine* getPluginRenderDefine() = 0;
};

#endif