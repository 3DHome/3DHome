#ifndef SE_SHADERDEFINE_H
#define SE_SHADERDEFINE_H
#include <vector>
#include <string>

class SE_ENTRY SE_ShaderDefine
{
public:
    SE_ShaderDefine();
    virtual ~SE_ShaderDefine();
    struct shader
    {
        std::string shaderId;
        std::string shaderClassName;
        std::string vertex_shader;
        std::string fragment_shader;
        
    };
    
    std::vector<shader> shaderArray;

};
#endif