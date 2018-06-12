#include <vector>
#include <string>

#ifdef SE_EXPORT
#undef SE_EXPORT
#define SE_IMPORT
#endif
#include "SE_DynamicLibType.h"
#include "SE_RenderDefine.h"
#include "my_renderdefine.h"

my_renderdefine::my_renderdefine()
{
    std::string my_render("pluginLighting_renderer");
    std::string my_render_class("SE_PluginLightRender");
    mRenderIdArray.push_back(my_render);
    mRenderClassName.push_back(my_render_class);
}

