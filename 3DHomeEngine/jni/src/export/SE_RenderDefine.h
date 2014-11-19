#ifndef SE_RENDERDEFINE_H
#define SE_RENDERDEFINE_H
#include <vector>
#include <string>

class SE_ENTRY SE_RenderDefine
{
public:
    SE_RenderDefine();
    virtual ~SE_RenderDefine();
    std::vector<std::string> mRenderIdArray;
    std::vector<std::string> mRenderClassName;
};
#endif