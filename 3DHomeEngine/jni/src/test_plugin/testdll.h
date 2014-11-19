#ifndef LIB_H
#define LIB_H

class SE_RenderDefine;
class SE_ShaderDefine;

class my_plugin : public SE_ShaderPlugin
{
    DECLARE_OBJECT(my_plugin)
public:
    my_plugin();
    virtual ~my_plugin();

    virtual const char* getPluginName();
    virtual const char* getPluginVersion();
    virtual int getPluginType();
    virtual void unLoadPlugin();
    virtual SE_ShaderDefine* getPluginShaderDefine();
    virtual SE_RenderDefine* getPluginRenderDefine();
    

public:
    static my_plugin* mPlugin;
    SE_RenderDefine* mRenderdefine;
    SE_ShaderDefine* mShaderdefine;

private:
    std::string mPluginName;
    int mType;
    std::string mVersion;
};

#endif