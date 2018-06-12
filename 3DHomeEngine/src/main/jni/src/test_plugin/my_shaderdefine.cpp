#include <vector>
#include <string>

#ifdef SE_EXPORT
#undef SE_EXPORT
#define SE_IMPORT
#endif
#include "SE_DynamicLibType.h"
#include "SE_ShaderDefine.h"
#include "my_shaderdefine.h"

my_shaderdefine::my_shaderdefine()
{

    //my shader
    shader my_shader;
    my_shader.shaderId = "pluginLighting_shader";
    my_shader.shaderClassName = "SE_PluginLightShader";

    const char my_vertex_shader[] = 
"attribute vec3 a_position;\r\n"                                                               
"attribute vec2 a_tex_coord;\r\n"                                                              
"attribute vec3 a_normal;\r\n"                          
"uniform mat4 u_wvp_matrix;\r\n"                          
"uniform vec4 u_PointLightPos[4];\r\n"                   
"uniform vec4 u_lightsNum;\r\n"                                 
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_cale;\r\n"                                                           
"uniform vec4 u_spot_data;\r\n"                                                                
"uniform vec3 u_distance_point_para[4];\r\n"       
"uniform vec3 u_light_color[4];\r\n"
"uniform float u_dirlight_strength[4];\r\n"
"varying vec3  SpecularLight;\r\n"                                                             
"varying vec2 v_tex_coord;\r\n"                                                                
"varying vec3 DiffuseLight;\r\n"                                                               
"vec3 diff = vec3(0.0);\r\n"                                                                   
"vec3 spec = vec3(0.0);\r\n"  
"float shiness = 64.0;\r\n"  
"vec3 amb = vec3(0.0);\r\n"                                                              
"uniform vec2 u_uvtiling;\r\n"  
"uniform float u_needSpec;\r\n"                                                              
"void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
"{\r\n"   

		"float ndotl = max(0.0,dot(normal, lightDir));\r\n" 
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n" 
    "spec += u_needSpec * vec3(max((ndotl - 0.8) * 2.5 , 0.0));\r\n"
  
#if 0   
    "if(ndotl <= 0.0 || u_needSpec == 0)\r\n" 
    "{\r\n"         
    "    spec += vec3(0.0);\r\n"         
    "}\r\n" 
    "else\r\n" 
    "{\r\n" 
    "    vec3 halfVector = normalize(lightDir + eyeDir);\r\n" 
    "    float NdotH = max(dot(halfVector, normal), 0.0);\r\n" 	
    "    float specular = attenuation * pow(NdotH, u_shiness);\r\n" 
    "    spec += vec3(specular);\r\n" 
    "} \r\n"                                                                                                       
#endif
"}\r\n"  
"void Light(int lightNum)\r\n"                                                            
"{\r\n"  
                                     
"    for(int i = 0; i < lightNum; ++i)\r\n"                                                    
"    {\r\n" 
        "vec4 pos = u_PointLightPos[i];\r\n"                                                   
        "vec3 att = u_distance_point_para[i];\r\n"                                             
        //now direction light push dir to pos
        "vec3 lightdir = pos.w * (pos.xyz - a_position) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
        "vec3 lightvec = a_position - pos.xyz;\r\n"                                        
        "float distance = length(lightvec);\r\n"                                               
        "float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n" 
        "float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"    
        "doLighting(a_normal,lightdir,resultatt,u_light_color[i]);\r\n"                                                 
"    }\r\n"                                                                                    
"    DiffuseLight = diff;\r\n"                                                                 
"    SpecularLight = spec;\r\n"                                                                
"}\r\n"                                                                  
                                                                                       
"void main()\r\n"                                                                              
"{\r\n"
    "Light(int(clamp(u_lightsNum.y,0.0,4.0)));\r\n"
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                                  
    "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"                                            
"}\r\n";
    my_shader.vertex_shader = my_vertex_shader;    

const char my_fragment_shader[] =  
//"precision mediump float;\r\n"                                                     
"varying highp vec2 v_tex_coord;\r\n"                                                            
"uniform lowp sampler2D u_texture;\r\n"                                                         
"uniform lowp vec3 u_color;\r\n"                                                                
"uniform lowp float u_shading_mode;\r\n"                                                          
"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n"                                                             
"uniform lowp vec3 u_frag_para;\r\n"                                                            
"uniform lowp float u_frag_alpha;\r\n"                                                          
"uniform lowp float useUserAlpha;\r\n"                                                            
"uniform lowp float useUserColor;\r\n"                                                            
"varying lowp vec3  SpecularLight;\r\n"                                                         
"varying lowp vec3 DiffuseLight;\r\n"                                                           
"void main()\r\n"                                                                          
"{\r\n"                                                                                                               
"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
"lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * DiffuseLight + SpecularLight,texcolor.a);\r\n"                                    
"lowp float result = (useUserAlpha * u_frag_alpha) + (1.0- useUserAlpha);\r\n"                         
"lowp vec3 usercolor = useUserColor * u_frag_para.rgb + vec3(1.0 - useUserColor);\r\n"
"gl_FragColor = vec4(lightcolor.rgb * usercolor,lightcolor.a * result);\r\n"
"}\r\n";                                                                            
    my_shader.fragment_shader = my_fragment_shader;

    shaderArray.push_back(my_shader);                
}
