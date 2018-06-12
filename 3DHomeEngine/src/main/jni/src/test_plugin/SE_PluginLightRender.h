#ifndef SE_PluginLightRender_H
#define SE_PluginLightRender_H

class SE_PluginLightRender : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_PluginLightRender)
public:
    SE_PluginLightRender();
    ~SE_PluginLightRender();
    virtual void free_render();

    virtual void begin(SE_ShaderProgram* shaderProgram);    
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
private:    
    void setLightingData(SE_RenderUnit* renderUnit);
    void setMaterialData(SE_RenderUnit* renderUnit);
};
#endif
