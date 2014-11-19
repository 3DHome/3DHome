#include "SE_DynamicLibType.h"
#include "SE_ShaderDefine.h"

SE_ShaderDefine::SE_ShaderDefine()
{

    //default shader
    shader default_shader;
    default_shader.shaderId = "default_shader";
    default_shader.shaderClassName = "SE_SimpleSurfaceShaderProgram";

    const char default_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n" 
                "uniform lowp vec2 u_reverse_texture;\r\n"     
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"
                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    default_shader.vertex_shader = default_vertex_shader;    

    const char default_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

                    "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"

                    "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
                    "lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"

                    "gl_FragColor = vec4(final,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    default_shader.fragment_shader = default_fragment_shader;

    shaderArray.push_back(default_shader);                
                
    //default fog shader
    shader default_fog_shader;
    default_fog_shader.shaderId = "default_fog_shader";
    default_fog_shader.shaderClassName = "SE_FogShaderProgram";

    const char fog_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n" 
                "uniform lowp vec2 u_reverse_texture;\r\n"
                "uniform highp mat4 M2VMatrix;\n"
                "uniform highp float u_fogEnd;\r\n"
                "uniform highp float u_fogRcpEndStartDiff;\r\n"
                "uniform highp float u_FogDensity;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                
                
                "void main()\r\n"
                "{\r\n"               

                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "highp vec4 viewPos = M2VMatrix * vec4(a_position, 1.0);\r\n"
                "highp float eyeDist = length(viewPos);\r\n"
                "highp float fogIntensity = (u_fogEnd - eyeDist) * u_fogRcpEndStartDiff;\r\n"
                "FogIntensity = vec3(clamp(fogIntensity, 0.0, 1.0));\r\n"

                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"
                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    default_fog_shader.vertex_shader = fog_vertex_shader;    

    const char fog_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "uniform lowp vec3 u_FogColor;\r\n"                

                "void main()\r\n"
                "{\r\n"                    
                    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

                    "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"

                    "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
                    "lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"
                    "lowp vec3 finalfog = mix(u_FogColor,final,FogIntensity);\r\n"
                    "gl_FragColor = vec4(finalfog,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    default_fog_shader.fragment_shader = fog_fragment_shader;

    shaderArray.push_back(default_fog_shader);


    //lightmap shader
    shader lightmap_shader;
    lightmap_shader.shaderId = "lightmap_shader";
    lightmap_shader.shaderClassName = "SE_LightMapShaderProgram";
    const char lightmap_vertex_shader[] = 
                "attribute vec3 a_position;\r\n"
                "attribute vec2 a_tex_coord;\r\n"
                "uniform mat4 u_wvp_matrix;\r\n"
                "varying vec2 v_tex_coord;\r\n"
                "uniform vec2 u_uvtiling;\r\n"
                "varying vec2 v_tex_coord_tiling;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "v_tex_coord = a_tex_coord;\r\n"
                "v_tex_coord_tiling = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"

                "}";
    lightmap_shader.vertex_shader = lightmap_vertex_shader;

    const char lightmap_fragment_shader[] =
                "precision mediump float;\r\n"
                "varying vec2 v_tex_coord;\r\n"
                "uniform sampler2D u_texture;\r\n"
                "uniform sampler2D u_specular_map;\r\n"                                         
                "uniform vec3 u_color;\r\n"
                "uniform int u_shading_mode;\r\n"
                "uniform vec3 u_frag_para;\r\n" 
                "uniform float u_frag_alpha;\r\n"
                "uniform int useUserAlpha;\r\n" 
                "uniform int useUserColor;\r\n"               
                "varying vec2 v_tex_coord_tiling;\r\n"
                "void main()\r\n"
                "{\r\n"                
                "if(u_shading_mode == 0)\r\n"
                "{\r\n"
                "gl_FragColor = vec4(u_color, 1.0);\r\n"                                        
                "float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * result;\r\n"
                "vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(usercolor,1.0);\r\n"                                                 
                "}\r\n"                                                                         
                "else if(u_shading_mode == 1)\r\n"                                              
                "{\r\n"                                                                         
                //"vec2 texcoord = v_tex_coord_tiling;\r\n"                                              
                "vec4 basecolor = texture2D(u_texture, v_tex_coord_tiling);\r\n"                          
                "gl_FragColor = basecolor;\r\n"                                                 
                "float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * result;\r\n"
                "vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(usercolor,1.0);\r\n"
                  
                "}\r\n"                                                                         
                "}\r\n"
                "else\r\n"
                "{\r\n"     
                //"vec2 texcoord = v_tex_coord;\r\n"                                              
                "vec4 basecolor = texture2D(u_texture, v_tex_coord_tiling);\r\n"                          
                "vec4 specularcolor = texture2D(u_specular_map, v_tex_coord);\r\n"                 
                "gl_FragColor = basecolor * specularcolor;\r\n"                                 
                "if(useUserAlpha > 0)\r\n"                                                      
                "{\r\n"                                                                         
                "gl_FragColor.a = gl_FragColor.a * result;\r\n"                       
                "}\r\n"                                                                         
                "if(useUserColor > 0)\r\n"                                                      
                "{\r\n"                                                                         
                "vec3 usercolor = vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z);\r\n"
                "gl_FragColor = gl_FragColor * vec4(usercolor,1.0);\r\n"                        
                "}\r\n"                                                                         
                "}\r\n" 
                "}\r\n";

    lightmap_shader.fragment_shader = lightmap_fragment_shader;
    shaderArray.push_back(lightmap_shader);


#if 1
        //shadow shader
    shader shadowmap_shader;
    shadowmap_shader.shaderId = "shadowmap_shader";
    shadowmap_shader.shaderClassName = "SE_ShadowMapShaderProgram";

    const char shadowmap_vertex_shader[] = 
"attribute highp vec3 a_position;\r\n"                                                    
"attribute highp vec2 a_tex_coord;\r\n"                                                   
"uniform highp mat4 u_wvp_matrix;\r\n"                                                    
"uniform mediump vec2 u_uvtiling;\r\n"                                                      
"uniform highp mat4 u_texProjection_matrix;\r\n"                                          
"varying mediump vec2 v_tex_coord;\r\n"                                                     
"varying highp vec4 projCoord;\r\n"                                                       
"void main()\r\n"                                                                   
"{\r\n"                                                                             
"gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                           
"v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"
"projCoord = u_texProjection_matrix * vec4(a_position, 1.0);\r\n"                   
"}\r\n";                                                                            
              
    shadowmap_shader.vertex_shader = shadowmap_vertex_shader;    

    const char shadowmap_fragment_shader[] =
"varying mediump vec2 v_tex_coord;\r\n"                                        
"uniform lowp sampler2D u_texture;\r\n"                                     
"uniform lowp vec3 u_color;\r\n"                                            
"uniform lowp float u_shading_mode;\r\n"                                      
"uniform mediump vec3 u_frag_para;\r\n"                                        
"uniform highp float u_frag_alpha;\r\n"                                      
"uniform int useUserAlpha;\r\n"                                        
"uniform int useUserColor;\r\n"                                        
"uniform highp sampler2D u_shadow_texture;\r\n"                              
"varying highp vec4 projCoord;\r\n"
"uniform int useVSM;\r\n"                                                                        
"uniform lowp vec3 u_userBWColor;\r\n"
"uniform lowp float useBWColor;\r\n"
"uniform mediump float u_ShadowColor;\r\n"
"highp vec4 ShadowCoordPostW;\r\n"                                                                     
"highp float chebyshevUpperBound(highp float distance)\r\n"                                                 
"{\r\n"                                                                                          
    "// We retrive the two moments previously stored (depth and depth*depth)\r\n"                    
    "highp vec2 moments = texture2D(u_shadow_texture,ShadowCoordPostW.xy).rg;\r\n"                         
    "\r\n"                                                                                           
    "// Surface is fully lit. as the current fragment is before the light occluder\r\n"              
    "if (distance <= moments.x)\r\n"                                                                 
    "{\r\n"                                                                                          
        "return 1.0;\r\n"                                                                                
    "}\r\n"                                                                                          
    "// The fragment is either in shadow or penumbra. We now use chebyshev's upperBound to check\r\n"
    "// How likely this pixel is to be lit (p_max)\r\n"                                              
    "highp float variance = moments.y - (moments.x*moments.x);\r\n"                                        
    "variance = max(variance,u_ShadowColor);\r\n"                                                            
                                                                                        
    "highp float d = distance - moments.x;\r\n"                                                            
    "highp float p_max = variance / (variance + d*d);\r\n"                                                                           
        "return p_max;\r\n"                                                                              
"}\r\n"

"void main()\r\n"                                                      
"{\r\n"
                                             
"ShadowCoordPostW = projCoord / projCoord.w;\r\n"                                                
"highp float result = chebyshevUpperBound(ShadowCoordPostW .z);\r\n"
"result = max(result, 0.5);\r\n"
"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

"lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"

"lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"                    
                    
"lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"
                                                                                          
"gl_FragColor = vec4(final * result,texcolor.a * u_frag_alpha);\r\n"
"}\r\n";                                                                
    shadowmap_shader.fragment_shader = shadowmap_fragment_shader;

    shaderArray.push_back(shadowmap_shader);
#endif


    //mirror shader
    shader mirror_shader;
    mirror_shader.shaderId = "mirror_shader";
    mirror_shader.shaderClassName = "SE_MirrorShaderProgram";

    const char mirror_vertex_shader[] = 
"attribute highp vec3 a_position;\r\n"                                                    
"attribute highp vec2 a_tex_coord;\r\n"                                                   
"uniform highp mat4 u_wvp_matrix;\r\n"                                                    
"uniform mediump vec2 u_uvtiling;\r\n"                                                      
"uniform highp mat4 u_texProjection_matrix;\r\n"                                          
"varying mediump vec2 v_tex_coord;\r\n"                                                     
"varying mediump vec4 projCoord;\r\n"                                                       
"void main()\r\n"                                                                   
"{\r\n"                                                                             
"gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                           
"v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"
"projCoord = u_texProjection_matrix * vec4(a_position, 1.0);\r\n"                   
"}\r\n";                                                                            
    mirror_shader.vertex_shader = mirror_vertex_shader;    


    const char mirror_fragment_shader[] =
"varying mediump vec2 v_tex_coord;\r\n"                                                           
"uniform lowp sampler2D u_texture;\r\n"                                                        
"uniform lowp vec3 u_color;\r\n"                                                               
"uniform lowp float u_shading_mode;\r\n"                                                         
"uniform lowp vec3 u_frag_para;\r\n"                                                           
"uniform lowp float u_frag_alpha;\r\n"                                                       
"uniform highp sampler2D u_mirror_texture;\r\n"  
"uniform lowp float useBWColor;\r\n"
"uniform lowp vec3 u_userBWColor;\r\n"
"uniform lowp float u_mirrordensity;\r\n"
"varying mediump vec4 projCoord;\r\n"                                                             
"void main()\r\n"                                                                         
"{\r\n"                                                                                   
"lowp vec4 texcolor = (u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color, 1.0));\r\n"
"lowp vec4 mirrorcolor = texture2DProj(u_mirror_texture, projCoord);\r\n"
"lowp vec4 temp = texcolor * mirrorcolor;\r\n"
"lowp vec4 mixcolor = mix(texcolor,temp,u_mirrordensity);\r\n"
"lowp vec3 resultColor = mixcolor.rgb * u_frag_para;\r\n"

"lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
"lowp vec3 final = (1.0 - useBWColor) * resultColor + useBWColor * bwcolor;\r\n"

"gl_FragColor = vec4(final,mixcolor.a * u_frag_alpha);\r\n"

"}\r\n";                                                                                 
    mirror_shader.fragment_shader = mirror_fragment_shader;

    shaderArray.push_back(mirror_shader);



    //draw mirror shader
    shader drawmirror_shader;
    drawmirror_shader.shaderId = "drawmirror_shader";
    drawmirror_shader.shaderClassName = "SE_DrawMirrorShaderProgram";

    const char drawmirror_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n" 
                "uniform lowp vec2 u_reverse_texture;\r\n"     
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"
                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    drawmirror_shader.vertex_shader = drawmirror_vertex_shader;    

    const char drawmirror_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

                    "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"

                    "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"                    
                    
                    "lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"

                    "gl_FragColor = vec4(final,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    drawmirror_shader.fragment_shader = drawmirror_fragment_shader;

    shaderArray.push_back(drawmirror_shader);    



    //vsm shader
    shader vsm_shader;
    vsm_shader.shaderId = "vsm_shader";
    vsm_shader.shaderClassName = "SE_DrawVSMShaderProgram";

    const char vsm_vertex_shader[] = 
"attribute highp vec3 a_position;\r\n"
"uniform highp mat4 u_wvp_matrix;\r\n"
"varying highp vec4 v_position;\r\n"
"void main()\r\n"                                            
"{\r\n"                                                      
"    gl_Position= u_wvp_matrix * vec4(a_position, 1.0);\r\n"
"    v_position = gl_Position;\r\n"                          
"}\r\n";                                                      
    vsm_shader.vertex_shader = vsm_vertex_shader;    

    const char vsm_fragment_shader[] =
"#extension GL_OES_standard_derivatives:enable \r\n"
"varying highp vec4 v_position;\r\n"
"void main()\r\n"                                      
"{\r\n"                                                
"    highp float depth = v_position.z / v_position.w;\r\n"
"    depth = depth* 0.5 + 0.5;\r\n"                                               
"    highp float moment1 = depth;\r\n"
"    highp float moment2 = depth * depth;\r\n"
"    highp float dx = dFdx(depth);\r\n"
"    highp float dy = dFdy(depth);\r\n"
"    moment2 += 0.25 * (dx*dx + dy*dy);\r\n"           
"\r\n"                                                 
"    gl_FragColor = vec4(moment1,moment2,0.0,1.0);\r\n"
"}\r\n";                                                
    vsm_shader.fragment_shader = vsm_fragment_shader;

    shaderArray.push_back(vsm_shader);   

#if 0
    //blurv shader
    shader blurv_shader;
    blurv_shader.shaderId = "blurv_shader";
    blurv_shader.shaderClassName = "SE_BlurVShaderProgram";

    const char blurv_vertex_shader[] = 
"attribute vec2 a_blurv_pos;\r\n" 
"void main(void)\r\n"  
"{\r\n" 
"	gl_Position = vec4(a_blurv_pos, 0.0, 1.0);\r\n" 
"}\r\n";                                            
    blurv_shader.vertex_shader = blurv_vertex_shader;    

    const char blurv_fragment_shader[] =                
"precision highp float;\r\n"                                                                                
"uniform sampler2D blurv_texture;\r\n"                                                                      
"uniform float texturev_size;\r\n"
"float offset[3];\r\n"                                                                                      
"float weight[3];\r\n"                                                                                      
"void main()\r\n"                                                                                           
"{\r\n"                                                                                                     
"offset[0] = 0.0;\r\n"                                                                                      
"offset[1] = 1.3846153846;\r\n"                                                                             
"offset[2] =3.2307692308;\r\n"                                                                              
"weight[0] = 0.2270270270;\r\n"                                                                             
"weight[1] = 0.3162162162;\r\n"                                                                             
"weight[2] = 0.0702702703;\r\n"                                                                             
"  vec4 color = texture2D( blurv_texture, vec2(gl_FragCoord)/texturev_size ) * weight[0];\r\n"                      
" for (int i=1; i<3; i++)\r\n"                                                                              
" {\r\n"                                                                                                    
"   color += texture2D( blurv_texture, ( vec2(gl_FragCoord)+vec2(0.0, offset[i]) )/texturev_size ) * weight[i];\r\n"
"   color += texture2D( blurv_texture, ( vec2(gl_FragCoord)-vec2(0.0, offset[i]) )/texturev_size ) * weight[i];\r\n"
" }\r\n"                                                                                                    
" gl_FragColor = color;\r\n"                                                                                
"}\r\n" ;                                                                                                    
    blurv_shader.fragment_shader = blurv_fragment_shader;

    shaderArray.push_back(blurv_shader);  


    //blurh shader
    shader blurh_shader;
    blurh_shader.shaderId = "blurh_shader";
    blurh_shader.shaderClassName = "SE_BlurHShaderProgram";

    const char blurh_vertex_shader[] = 
"attribute vec2 a_blurh_pos;\r\n" 
"void main(void)\r\n"  
"{\r\n" 
"	gl_Position = vec4(a_blurh_pos, 0.0, 1.0);\r\n" 
"}\r\n";                                            
    blurh_shader.vertex_shader = blurh_vertex_shader;    

    const char blurh_fragment_shader[] =                
"precision highp float;\r\n"                                                                                
"uniform sampler2D blurh_texture;\r\n"                                                                      
"uniform float textureh_size;\r\n"                                                                      
"float offset[3];\r\n"                                                                                      
"float weight[3];\r\n"                                                                                      
"void main()\r\n"                                                                                           
"{\r\n"                                                                                                     
"offset[0] = 0.0;\r\n"                                                                                      
"offset[1] = 1.3846153846;\r\n"                                                                             
"offset[2] =3.2307692308;\r\n"                                                                              
"weight[0] = 0.2270270270;\r\n"                                                                             
"weight[1] = 0.3162162162;\r\n"                                                                             
"weight[2] = 0.0702702703;\r\n"                                                                             
"  vec4 color = texture2D( blurh_texture, vec2(gl_FragCoord)/textureh_size ) * weight[0];\r\n"                      
" for (int i=1; i<3; i++)\r\n"                                                                              
" {\r\n"                                                                                                    
"   color += texture2D( blurh_texture, ( vec2(gl_FragCoord)+vec2(offset[i],0.0 ) )/textureh_size ) * weight[i];\r\n"
"   color += texture2D( blurh_texture, ( vec2(gl_FragCoord)-vec2(offset[i],0.0 ) )/textureh_size ) * weight[i];\r\n"
" }\r\n"                                                                                                    
" gl_FragColor = color;\r\n"                                                                                
"}\r\n" ;                                                                                                    
    blurh_shader.fragment_shader = blurh_fragment_shader;

    shaderArray.push_back(blurh_shader);  
#endif

    //full blur shader
    shader blur_shader;
    blur_shader.shaderId = "blur_shader";
    blur_shader.shaderClassName = "SE_BlurShaderProgram";

    const char blur_vertex_shader[] = 
"attribute highp vec2 a_blur_pos;\r\n"                           
"attribute mediump vec2 a_blur_texCoord;\r\n"                    
"uniform mediump float  TexelOffsetX;\r\n"                       
"uniform mediump float  TexelOffsetY;\r\n"                       
"varying mediump vec2  TexCoord0;\r\n"                           
"varying mediump vec2  TexCoord1;\r\n"                           
"varying mediump vec2  TexCoord2;\r\n"                           
"void main(void) {\r\n"                                          
"    gl_Position = vec4(a_blur_pos, 0.0, 1.0);\r\n"              
"    mediump vec2 offset = vec2(TexelOffsetX, TexelOffsetY);\r\n"    
"    TexCoord0 = a_blur_texCoord - offset;\r\n"                  
"    TexCoord1 = a_blur_texCoord;\r\n"                           
"    TexCoord2 = a_blur_texCoord + offset;\r\n"                  
"}\r\n" ;                                                         
    blur_shader.vertex_shader = blur_vertex_shader;    

    const char blur_fragment_shader[] =                
"precision highp float;\r\n"                                                  
"uniform sampler2D blur_texture;\r\n"                                         
"varying mediump vec2  TexCoord0;\r\n"                                        
"varying mediump vec2  TexCoord1;\r\n"                                        
"varying mediump vec2  TexCoord2;\r\n"                                        
"void main()\r\n"                                                             
"{\r\n"                                                                       
"    lowp vec3 color = texture2D(blur_texture, TexCoord0).rgb * 0.333333;\r\n"
"    color = color + texture2D(blur_texture, TexCoord1).rgb * 0.333333;\r\n"  
"    color = color + texture2D(blur_texture, TexCoord2).rgb * 0.333333;\r\n"    
"    gl_FragColor = vec4(color,1.0);\r\n"                                           
"\r\n"                                                                        
"}\r\n";                                                                              
    blur_shader.fragment_shader = blur_fragment_shader;

    shaderArray.push_back(blur_shader);  


    //alpha test shader
    shader alphatest_shader;
    alphatest_shader.shaderId = "alphatest_shader";
    alphatest_shader.shaderClassName = "SE_AlphaTestShaderProgram";

    const char alphatest_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"
                "}";
    alphatest_shader.vertex_shader = alphatest_vertex_shader;    

    const char alphatest_fragment_shader[] = 
        "precision mediump float;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform mediump int u_shading_mode;\r\n"
                "uniform mediump vec3 u_frag_para;\r\n"
                "uniform highp float u_frag_alpha;\r\n"
                "uniform int useUserAlpha;\r\n"
                "uniform int useUserColor;\r\n"
                "void main()\r\n"
                "{\r\n"                
                "if(u_shading_mode == 0)\r\n"
                "{\r\n"
                "gl_FragColor = vec4(u_color, 1.0);\r\n"
                //"float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * u_frag_alpha;\r\n"
                //"vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(u_frag_para,1.0);\r\n" 
                "}\r\n"
                "else\r\n"
                "{\r\n"  
                //"vec2 texcoord = v_tex_coord;\r\n"
                //"vec4 basecolor = texture2D(u_texture, v_tex_coord);\r\n"               
                "gl_FragColor = texture2D(u_texture, v_tex_coord);\r\n"                
                "if(gl_FragColor.a <= 0.1)\r\n"
                "{\r\n"                
                "discard;\r\n"                
                "}\r\n"
                //"float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * u_frag_alpha;\r\n"
                //"vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(u_frag_para,1.0);\r\n"
                "}\r\n"                 
                "}\r\n";
    alphatest_shader.fragment_shader = alphatest_fragment_shader;

    shaderArray.push_back(alphatest_shader);      


    //simple noimg shader
    shader defaultnoimg_shader;
    defaultnoimg_shader.shaderId = "defaultnoimg_shader";
    defaultnoimg_shader.shaderClassName = "SE_SimpleNoImgShaderProgram";

    const char defaultnoimg_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"                
                "uniform mat4 u_wvp_matrix;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                
                "}";
    defaultnoimg_shader.vertex_shader = defaultnoimg_vertex_shader;    

    const char defaultnoimg_fragment_shader[] =                
                "void main()\r\n"
                "{\r\n"                
                "gl_FragColor = vec4(0.0,0.0,0.0,1.0);\r\n"                
                "}\r\n";
    defaultnoimg_shader.fragment_shader = defaultnoimg_fragment_shader;

    shaderArray.push_back(defaultnoimg_shader);  




    //uvanimation shader
    shader uvanimation_shader;
    uvanimation_shader.shaderId = "uvanimation_shader";
    uvanimation_shader.shaderClassName = "SE_SimpleUVAnimationShaderProgram";

    const char uvanimation_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform mediump vec2 u_texcoord_offset;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "vec2 texcoord = vec2(a_tex_coord.x + u_texcoord_offset.x, a_tex_coord.y + u_texcoord_offset.y);\r\n"  
                "v_tex_coord = vec2(texcoord.x * u_uvtiling.x,texcoord.y * u_uvtiling.y);\r\n"
                "}";
    uvanimation_shader.vertex_shader = uvanimation_vertex_shader;    

    const char uvanimation_fragment_shader[] =
                "varying mediump vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "void main()\r\n"
                "{\r\n"                
                "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
                "gl_FragColor = vec4(texcolor.rgb * u_frag_para,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    uvanimation_shader.fragment_shader = uvanimation_fragment_shader;

    shaderArray.push_back(uvanimation_shader);    

#if 0
    //uvanimationfog shader
    shader uvanimationfog_shader;
    uvanimationfog_shader.shaderId = "uvanimationfog_shader";
    uvanimationfog_shader.shaderClassName = "SE_SimpleUVAnimationFogShaderProgram";

    const char uvanimationfog_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform mediump vec2 u_texcoord_offset;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "uniform highp mat4 M2VMatrix;\n"
                "uniform highp float u_fogEnd;\r\n"
                "uniform highp float u_fogRcpEndStartDiff;\r\n"
                "uniform highp float u_FogDensity;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"

                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "highp vec4 viewPos = M2VMatrix * vec4(a_position, 1.0);\r\n"
                "highp float eyeDist = length(viewPos);\r\n"
                "highp float fogIntensity = (u_fogEnd - eyeDist) * u_fogRcpEndStartDiff;\r\n"
                "FogIntensity = vec3(clamp(fogIntensity, 0.0, 1.0));\r\n"
                "vec2 texcoord = vec2(a_tex_coord.x + u_texcoord_offset.x, a_tex_coord.y + u_texcoord_offset.y);\r\n"  
                "v_tex_coord = vec2(texcoord.x * u_uvtiling.x,texcoord.y * u_uvtiling.y);\r\n"
                "}";
    uvanimationfog_shader.vertex_shader = uvanimationfog_vertex_shader;    

    const char uvanimationfog_fragment_shader[] =
                "varying mediump vec2 v_tex_coord;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp vec3 u_FogColor;\r\n"     
                "void main()\r\n"
                "{\r\n"                
                "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
                "lowp vec3 finalfog = mix(u_FogColor,texcolor.rgb,FogIntensity);\r\n"
                "gl_FragColor = vec4(finalfog.rgb * u_frag_para,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    uvanimationfog_shader.fragment_shader = uvanimationfog_fragment_shader;

    shaderArray.push_back(uvanimationfog_shader);  
#endif

    //particle shader
    shader particle_shader;
    particle_shader.shaderId = "particle_shader";
    particle_shader.shaderClassName = "SE_ParticleShaderProgram";

    const char particle_vertex_shader[] = 
"attribute highp vec3 a_position;\r\n"
"attribute mediump vec2 a_tex_coord;\r\n"
"attribute lowp vec4 a_vertex_color;\r\n"
"uniform highp mat4 u_wvp_matrix;\r\n"
"varying mediump vec2 v_tex_coord;\r\n"
"varying lowp vec4 v_vertex_color;\r\n"
"void main()\r\n"
"{\r\n"
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0); \r\n"   
    "v_tex_coord = a_tex_coord;\r\n"
    "v_vertex_color = a_vertex_color;\r\n"
"}\r\n";
    particle_shader.vertex_shader = particle_vertex_shader;    

    const char particle_fragment_shader[] = 
                "varying mediump vec2 v_tex_coord;\r\n"\
                "varying lowp vec4 v_vertex_color;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "void main()\r\n"
                "{\r\n"
                    "if(u_shading_mode == 0.0)\r\n"
                    "{\r\n"
                        "lowp vec4 texcolor = v_vertex_color;\r\n"
            //"lowp float result = (useUserAlpha * u_frag_alpha) + (1.0- useUserAlpha);\r\n"                         
            //"lowp vec3 usercolor = (useUserColor * u_frag_para.rgb) + vec3(1.0 - useUserColor);\r\n"
            "gl_FragColor = vec4(texcolor.rgb,texcolor.a * u_frag_alpha);\r\n"
                    "}\r\n"
                    "else if(u_shading_mode == 1.0)\r\n"
                    "{\r\n"     
                        "lowp vec4 texcolor = texture2D(u_texture, v_tex_coord);\r\n"
            //"lowp float result = (useUserAlpha * u_frag_alpha) + (1.0- useUserAlpha);\r\n"                         
            //"lowp vec3 usercolor = (useUserColor * u_frag_para.rgb) + vec3(1.0 - useUserColor);\r\n"
            "gl_FragColor = vec4(texcolor.rgb,texcolor.a * u_frag_alpha);\r\n"
                    "}\r\n"
                    "else\r\n"
                    "{\r\n"
                        "lowp vec4 texcolor = texture2D(u_texture, v_tex_coord) * v_vertex_color;\r\n"
            //"lowp float result = (useUserAlpha * u_frag_alpha) + (1.0- useUserAlpha);\r\n"                         
            //"lowp vec3 usercolor = (useUserColor * u_frag_para.rgb) + vec3(1.0 - useUserColor);\r\n"
            "gl_FragColor = vec4(texcolor.rgb,texcolor.a * u_frag_alpha);\r\n"
                    "}\r\n"
                "}\r\n";


    particle_shader.fragment_shader = particle_fragment_shader;

    shaderArray.push_back(particle_shader);

    //simple lighting shader
    shader simplelighting_shader;
    simplelighting_shader.shaderId = "simplelighting_shader";
    simplelighting_shader.shaderClassName = "SE_SimpleLightingShaderProgram";

    const char simplelighting_vertex_shader[] = 
"attribute vec3 a_position;\r\n"                                                               
"attribute vec2 a_tex_coord;\r\n"                                                              
"attribute vec3 a_normal;\r\n"                          
"uniform mat4 u_wvp_matrix;\r\n"
"uniform vec4 u_PointLightPos[4];\r\n"                                                         
"uniform vec4 u_lightsNum;\r\n"          
"uniform vec3 u_distance_point_para[4];\r\n"
"uniform vec3 u_light_color[4];\r\n"
"uniform float u_dirlight_strength[4];\r\n"
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_scale;\r\n"
"varying vec3  SpecularLight;\r\n"                                                             
"varying vec2 v_tex_coord;\r\n"                                                                
"varying vec3 DiffuseLight;\r\n"                                                               
"vec3 diff = vec3(0.0);\r\n"                                                                   
"vec3 spec = vec3(0.0);\r\n" 
"uniform vec2 u_uvtiling;\r\n"  
"uniform float u_needSpec;\r\n"

"void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
"{\r\n"   

	"float ndotl = max(0.0,dot(normal, lightDir));\r\n"
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
    "spec += u_needSpec * vec3(max((ndotl - u_material_bias) * u_material_scale, 0.0));\r\n"
     
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
"void processLight(int i)\r\n"
"{\r\n"
    "vec4 pos = u_PointLightPos[i];\r\n"
    "vec3 att = u_distance_point_para[i];\r\n"
    //now direction light push dir to pos
    "vec3 lightdir = pos.w * (pos.xyz - a_position) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
    "vec3 lightvec = a_position - pos.xyz;\r\n"
    "float distance = length(lightvec);\r\n"
    "float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"
    "float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"
    "doLighting(a_normal,lightdir,resultatt,u_light_color[i]);\r\n"
"}\r\n"
/**
 * 有些GPU不支持While和for所有只能用if取代
 */
"void Light(int lightNum)\r\n"
"{\r\n"
    "if (lightNum > 0)\r\n"
        "processLight(0);\r\n"
    "if (lightNum > 1)\r\n"
        "processLight(1);\r\n"
    "if (lightNum > 2)\r\n"
        "processLight(2);\r\n"
    "DiffuseLight = diff;\r\n"
    "SpecularLight = spec;\r\n"
"}\r\n"                                                  
                                                            
"void main()\r\n"                                                                              
"{\r\n"
    "Light(int(u_lightsNum.y));\r\n"
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                                  
    "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"                                            
"}\r\n";
    simplelighting_shader.vertex_shader = simplelighting_vertex_shader;    

    const char simplelighting_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"                                                            
"uniform lowp sampler2D u_texture;\r\n"                                                         
"uniform lowp vec3 u_color;\r\n"                                                                
"uniform lowp float u_shading_mode;\r\n"                                                          
"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n"                                                             
"uniform lowp vec3 u_frag_para;\r\n"                                                            
"uniform lowp float u_frag_alpha;\r\n"                                              

"uniform lowp float useBWColor;\r\n"
"uniform lowp vec3 u_userBWColor;\r\n"
"varying lowp vec3  SpecularLight;\r\n"                                                         
"varying lowp vec3 DiffuseLight;\r\n"                                                           
"lowp vec3 amb = vec3(0.0);\r\n"
"void main()\r\n"                                                                          
"{\r\n"                                                                                                               
"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
"lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * DiffuseLight + SpecularLight + amb,texcolor.a);\r\n"

"lowp vec3 resultColor = lightcolor.rgb * u_frag_para;\r\n"

//"lowp float IntenSity = 0.2990 * resultColor.r + 0.5870 * resultColor.g  + 0.1140 * resultColor.b;\r\n"
"lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
"lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"
"gl_FragColor = vec4(final ,lightcolor.a * u_frag_alpha);\r\n"

"}\r\n";                              
    simplelighting_shader.fragment_shader = simplelighting_fragment_shader;

    shaderArray.push_back(simplelighting_shader);
    //////////////////////////


    //simple lighting shader
    shader simplelighting_fog_shader;
    simplelighting_fog_shader.shaderId = "simplelighting_fog_shader";
    simplelighting_fog_shader.shaderClassName = "SE_SimpleLightingFogShaderProgram";

    const char simplelightingfog_vertex_shader[] = 
"attribute vec3 a_position;\r\n"                                                               
"attribute vec2 a_tex_coord;\r\n"                                                              
"attribute vec3 a_normal;\r\n"                          
"uniform mat4 u_wvp_matrix;\r\n"
"uniform vec4 u_PointLightPos[4];\r\n"                                                         
"uniform vec4 u_lightsNum;\r\n"          
"uniform vec3 u_distance_point_para[4];\r\n"
"uniform vec3 u_light_color[4];\r\n"
"uniform float u_dirlight_strength[4];\r\n"
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_scale;\r\n"
"varying vec3  SpecularLight;\r\n"                                                             
"varying vec2 v_tex_coord;\r\n"                                                                
"varying vec3 DiffuseLight;\r\n"                                                               
"vec3 diff = vec3(0.0);\r\n"                                                                   
"vec3 spec = vec3(0.0);\r\n" 
"uniform vec2 u_uvtiling;\r\n"  
"uniform float u_needSpec;\r\n"

"uniform highp mat4 M2VMatrix;\n"
"uniform highp float u_fogEnd;\r\n"
"uniform highp float u_fogRcpEndStartDiff;\r\n"
"uniform highp float u_FogDensity;\r\n"
"varying mediump vec3 FogIntensity; \r\n"
"void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
"{\r\n"   

		"float ndotl = max(0.0,dot(normal, lightDir));\r\n" 
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
    "spec += u_needSpec * vec3(max((ndotl - u_material_bias) * u_material_scale, 0.0));\r\n"
     
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
"void processLight(int i)\r\n"
    "{\r\n"
        "vec4 pos = u_PointLightPos[i];\r\n"
        "vec3 att = u_distance_point_para[i];\r\n"
        //now direction light push dir to pos
        "vec3 lightdir = pos.w * (pos.xyz - a_position) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
        "vec3 lightvec = a_position - pos.xyz;\r\n"
        "float distance = length(lightvec);\r\n"
        "float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"
        "float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"
        "doLighting(a_normal,lightdir,resultatt,u_light_color[i]);\r\n"
"}\r\n"
/**
 * 有些GPU不支持While和for所有只能用if取代
 */
"void Light(int lightNum)\r\n"
"{\r\n"
    "if (lightNum > 0)\r\n"
        "processLight(0);\r\n"
    "if (lightNum > 1)\r\n"
        "processLight(1);\r\n"
    "if (lightNum > 2)\r\n"
        "processLight(2);\r\n"
    "DiffuseLight = diff;\r\n"
    "SpecularLight = spec;\r\n"
"}\r\n"                                                  
                                                            
"void main()\r\n"                                                                              
"{\r\n"
    "Light(int(u_lightsNum.y));\r\n"
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                                  
    "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"   

    "highp vec4 viewPos = M2VMatrix * vec4(a_position, 1.0);\r\n"
    "highp float eyeDist = length(viewPos);\r\n"
    "highp float fogIntensity = (u_fogEnd - eyeDist) * u_fogRcpEndStartDiff;\r\n"
    "FogIntensity = vec3(clamp(fogIntensity, 0.0, 1.0));\r\n"

"}\r\n";
    simplelighting_fog_shader.vertex_shader = simplelightingfog_vertex_shader;    

    const char simplelightingfog_fragment_shader[] =
"varying highp vec2 v_tex_coord;\r\n"  
"varying mediump vec3 FogIntensity; \r\n"
"uniform lowp sampler2D u_texture;\r\n"                                                         
"uniform lowp vec3 u_color;\r\n"                                                                
"uniform lowp float u_shading_mode;\r\n"                                                          
"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n"                                                             
"uniform lowp vec3 u_frag_para;\r\n"                                                            
"uniform lowp float u_frag_alpha;\r\n"                                              

"uniform lowp float useBWColor;\r\n"
"uniform lowp vec3 u_userBWColor;\r\n"
"varying lowp vec3  SpecularLight;\r\n"                                                         
"varying lowp vec3 DiffuseLight;\r\n"
"uniform lowp vec3 u_FogColor;\r\n"  
"lowp vec3 amb = vec3(0.0);\r\n"
"void main()\r\n"                                                                          
"{\r\n"                                                                                                               
"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
"lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * DiffuseLight + SpecularLight + amb,texcolor.a);\r\n"

"lowp vec3 resultColor = lightcolor.rgb * u_frag_para;\r\n"

"lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
"lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"
"lowp vec3 finalfog = mix(u_FogColor,final,FogIntensity);\r\n"
"gl_FragColor = vec4(finalfog ,lightcolor.a * u_frag_alpha);\r\n"

"}\r\n";                              
    simplelighting_fog_shader.fragment_shader = simplelightingfog_fragment_shader;

    shaderArray.push_back(simplelighting_fog_shader);



        //vertex simple light with spot light(spot light use per-pixel)
    shader simplelighting_spot_shader;
    simplelighting_spot_shader.shaderId = "simplelighting_spot_shader";
    simplelighting_spot_shader.shaderClassName = "SE_SimpleLightingShaderProgram";

    const char simplelighting_spot_vertex_shader[] = 
"attribute vec3 a_position;\r\n"                                                               
"attribute vec2 a_tex_coord;\r\n"                                                              
"attribute vec3 a_normal;\r\n"                          
"uniform mat4 u_wvp_matrix;\r\n"                                                              
"uniform vec4 u_PointLightPos[4];\r\n"              
"uniform vec4 u_lightsNum;\r\n"                                                                 
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_scale;\r\n"
"uniform vec3 u_distance_point_para[4];\r\n"
"uniform vec3 u_light_color[4];\r\n"
"uniform float u_dirlight_strength[4];\r\n"              
"varying vec2 v_tex_coord;\r\n"                                                                
"varying vec3 DiffuseLight;\r\n" 
"varying lowp vec3 vertex_normal;\r\n"
"varying mediump vec3 vertex_pos;\r\n"

"vec3 diff = vec3(0.0);\r\n"
"vec3 amb = vec3(0.0);\r\n"                                                              
"uniform vec2 u_uvtiling;\r\n"

"void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
"{\r\n"
    "float ndotl = max(0.0,dot(normal, lightDir));\r\n"
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
"}\r\n"
"void processLight(int i)\r\n"
"{\r\n"
    "vec4 pos = u_PointLightPos[i];\r\n"
    "vec3 att = u_distance_point_para[i];\r\n"
    //now direction light push dir to pos
    "vec3 lightdir = pos.w * (pos.xyz - a_position) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
    "vec3 lightvec = a_position - pos.xyz;\r\n"
    "float distance = length(lightvec);\r\n"
    "float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"
    "float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"
    "doLighting(a_normal,lightdir,resultatt,u_light_color[i]);\r\n"
"}\r\n"
"void Light(int lightNum)\r\n"
"{\r\n"   
    "if (lightNum > 0)\r\n"
        "processLight(0);\r\n"
    "if (lightNum > 1)\r\n"
        "processLight(1);\r\n"
    "if (lightNum > 2)\r\n"
        "processLight(2);\r\n"
"    DiffuseLight = diff;\r\n"                 
"}\r\n"                                                  
                                                                            
"void main()\r\n"                                                                              
"{\r\n"
    "Light(int(clamp(u_lightsNum.y,0.0,4.0)));\r\n" 
    "vertex_normal = a_normal;\r\n" 
    "vertex_pos = a_position;\r\n"
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                                  
    "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"                                            
"}\r\n";
    simplelighting_spot_shader.vertex_shader = simplelighting_spot_vertex_shader;    

    const char simplelighting_spot_fragment_shader[] =
"precision mediump float;"
"varying highp vec2 v_tex_coord;\r\n"                                                            
"uniform lowp sampler2D u_texture;\r\n"                                                         
"uniform lowp vec3 u_color;\r\n"                                                                
"uniform lowp float u_shading_mode;\r\n"                                                          
"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n"                                                             
"uniform lowp vec3 u_frag_para;\r\n"                                                            
"uniform lowp float u_frag_alpha;\r\n"                                                          
 
"varying lowp vec3 DiffuseLight;\r\n"
"varying lowp vec3 vertex_normal;\r\n"
"varying mediump vec3 vertex_pos;\r\n"
"uniform mediump vec4 u_SpotLightPos[4];\r\n"   
"uniform lowp vec3 u_SpotLightDir[4];\r\n"
"uniform mediump vec4 u_spot_data;\r\n"
"uniform highp vec3 u_distance_spot_para[4];\r\n" 
"uniform lowp vec3 u_spotlight_color[4];\r\n"
"uniform mediump float u_spot_number;\r\n"
"uniform lowp float u_needSpec;\r\n"
"lowp vec3 spec = vec3(0.0);\r\n"
"lowp vec3 diff = vec3(0.0);\r\n"
"mediump float shiness = 64.0;\r\n"
"lowp vec3 amb = vec3(0.0);\r\n"  
"void doLighting(mediump vec3 normal, mediump vec3 lightDir,vec3 lightcolor,float attenuation)\r\n"                                        
"{\r\n"

	"mediump float ndotl = max(0.0,dot(normal, lightDir));\r\n" 
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
    "spec += u_needSpec * vec3(max((ndotl - 0.8) * 2.5, 0.0));\r\n"
#if 0
    "if(ndotl <= 0.0 || u_needSpec == 0)\r\n" 
    "{\r\n"         
    "    spec +=  vec3(0.0);\r\n"
    "}\r\n" 
    "else\r\n" 
    "{\r\n" 
    "    mediump vec3 halfVector = normalize(lightDir + eyeDir);\r\n" 
    "    mediump float NdotH = max(dot(halfVector, normal), 0.0);\r\n" 	
    "    mediump float specular = attenuation * pow(NdotH, shiness);\r\n" 
    "    spec += vec3(specular);\r\n" 
    "} \r\n" 
#endif
    
"}\r\n" 

"void SpotLight(int lightNum)\r\n"                                                             
"{\r\n"
    "mediump vec3 v_normal = normalize(vertex_normal);\r\n"
    "int i = 0;\r\n"
    "mediump vec4 pos = u_SpotLightPos[i];\r\n"
    "mediump vec3 lightpos = pos.xyz;\r\n"
    "mediump vec3 indexdir = u_SpotLightDir[i];\r\n"
    "mediump vec3 lightdirection = -normalize(indexdir);\r\n"
    "mediump vec3 lightdir = normalize(lightpos - vertex_pos);\r\n"
    "mediump vec3 att = u_distance_spot_para[i];\r\n"
    //"LightDirection is spot direction here\r\n"
    "mediump float spotDot = dot(lightdir,lightdirection);\r\n"
    "mediump float attenuation = 0.0;\r\n"
    "mediump float u_spot_cutoff = u_spot_data.x;\r\n"
    "mediump float u_spot_exp = u_spot_data.y;\r\n"
"if (spotDot > u_spot_cutoff)\r\n"
"{\r\n"
        "mediump vec3 lightvec = vertex_pos - lightpos;\r\n"
        "mediump float distance = length(lightvec);\r\n"
        "mediump float attr = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"
        "attenuation = pow(spotDot, u_spot_exp) * attr;\r\n"
    "}\r\n"
    "doLighting(v_normal, lightdir,u_spotlight_color[i],attenuation);\r\n"
"}\r\n"

"void main()\r\n"                                                                          
"{\r\n"  
    "SpotLight(int(clamp(u_spot_number,0.0,4.0)));\r\n"
    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
    "lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * (DiffuseLight + diff) + spec + amb,texcolor.a);\r\n"
    "gl_FragColor = vec4(lightcolor.rgb * u_frag_para,lightcolor.a * u_frag_alpha);\r\n"
"}\r\n";                              
    simplelighting_spot_shader.fragment_shader = simplelighting_spot_fragment_shader;

    shaderArray.push_back(simplelighting_spot_shader);



    //per-pixel light
    //simple lighting shader
    shader simplelighting_perpixel_shader;
    simplelighting_perpixel_shader.shaderId = "simplelighting_perpixel_shader";
    simplelighting_perpixel_shader.shaderClassName = "SE_SimpleLightingShaderProgram";

    const char simplelighting_perpixel_vertex_shader[] = 
"attribute vec3 a_position;\r\n"   
"attribute vec2 a_tex_coord;\r\n"
"attribute vec3 a_normal;\r\n"
"uniform mat4 u_wvp_matrix;\r\n"
//"uniform mat4 u_w2m_matrix;\r\n"                                                                 
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_cale;\r\n" 
//"varying vec3  SpecularLight;\r\n"                                                             
"varying vec2 v_tex_coord;\r\n"                                                                
//"varying vec3 DiffuseLight;\r\n"  
"vec3 amb = vec3(0.0);\r\n"                                                              
"uniform vec2 u_uvtiling;\r\n"
"varying lowp vec3 vertex_normal;\r\n"
"varying mediump vec3 vertex_pos;\r\n"
"void main()\r\n"                                                                              
"{\r\n"
    "vertex_normal = a_normal;\r\n"
    "vertex_pos = a_position;\r\n"
    //"SpotLight(int(clamp(u_lightsNum.z,0.0,4.0)));\r\n"   
    "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                                  
    "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"                                            
"}\r\n";

    simplelighting_perpixel_shader.vertex_shader = simplelighting_perpixel_vertex_shader;    

    const char simplelighting_perpixel_fragment_shader[] =
//"precision mediump float;"
"varying highp vec2 v_tex_coord;\r\n"                                                            
"uniform lowp sampler2D u_texture;\r\n"                                                         
"uniform lowp vec3 u_color;\r\n"                                                                
"uniform lowp float u_shading_mode;\r\n"                                                          
"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n"                                                             
"uniform lowp vec3 u_frag_para;\r\n"                                                            
"uniform lowp float u_frag_alpha;\r\n"                                                          

"uniform mediump float u_shiness;\r\n"
"uniform lowp float u_needSpec;\r\n"
"uniform lowp vec3 u_light_color[4];\r\n"
"uniform lowp float u_dirlight_strength[4];\r\n"
"uniform highp vec3 u_distance_point_para[4];\r\n"  
"uniform mediump vec3 u_eyePos;\r\n"
"uniform mediump vec4 u_lightsNum;\r\n"
"uniform mediump vec4 u_PointLightPos[4];\r\n" 
"uniform mediump vec4 u_SpotLightPos[4];\r\n"
"uniform lowp vec3 u_SpotLightDir[4];\r\n"
"uniform mediump vec4 u_spot_data;\r\n"                
"uniform highp vec3 u_distance_spot_para[4];\r\n"
"varying lowp vec3 vertex_normal;\r\n" 
"varying mediump vec3 vertex_pos;\r\n" 
"mediump vec3 diff = vec3(0.0);\r\n"                                                                   
"mediump vec3 spec = vec3(0.0);\r\n" 
"mediump float shiness = 64.0;\r\n"
//"varying lowp vec3  SpecularLight;\r\n"                                                         
//"varying lowp vec3 DiffuseLight;\r\n" 

#if 1
"void doLighting(mediump vec3 normal, mediump vec3 lightDir, mediump vec3 eyeDir, lowp vec3 lightcolor,mediump float attenuation)\r\n"                                        
"{\r\n"   

	"mediump float ndotl = max(0.0,dot(normal, lightDir));\r\n" 
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n" 
     
    "if(ndotl <= 0.0 || u_needSpec == 0)\r\n" 
    "{\r\n"         
    "    spec +=  vec3(0.0);\r\n"
    "}\r\n" 
    "else\r\n" 
    "{\r\n" 
    "    mediump vec3 halfVector = normalize(lightDir + eyeDir);\r\n" 
    "    mediump float NdotH = max(dot(halfVector, normal), 0.0);\r\n" 	
    "    mediump float specular = attenuation * pow(NdotH, shiness);\r\n" 
    "    spec += vec3(specular);\r\n" 
    "} \r\n"                                      
    
"}\r\n" 
#endif
"void SpotLight(int lightNum)\r\n"                                                             
"{\r\n"          
    "mediump vec3 eyedir = normalize(u_eyePos - vertex_pos);\r\n"  
    "mediump vec3 v_normal = normalize(vertex_normal);\r\n"
"    for(int i = 0; i < 4; ++i)\r\n"                                                    
"    {\r\n"    
        "if(i >= lightNum)\r\n"
        "{\r\n"
           " return;\r\n"
        "}\r\n"
"       mediump vec4 pos = u_SpotLightPos[i];\r\n"                                                    
"       mediump vec3 lightpos = pos.xyz;\r\n"                                                         
"       mediump vec3 indexdir = u_SpotLightDir[i];\r\n"                                               
"       mediump vec3 lightdirection = -normalize(indexdir);\r\n"                                          
"       mediump vec3 lightdir = normalize(lightpos - vertex_pos);\r\n"                            
"       mediump vec3 att = u_distance_spot_para[i];\r\n"                          
        //"LightDirection is spot direction here\r\n"                                         
"       mediump float spotDot = dot(lightdir,lightdirection);\r\n"                                    
"       mediump float attenuation = 0.0;\r\n"                                                         
"       mediump float u_spot_cutoff = u_spot_data.x;\r\n"                                             
"       mediump float u_spot_exp = u_spot_data.y;\r\n"                                                                                                                                       
"        if (spotDot > u_spot_cutoff)\r\n"                                                     
"        {\r\n"                                                                                
"           mediump vec3 lightvec = vertex_pos - lightpos;\r\n"                                    
"           mediump float distance = length(lightvec);\r\n"                                           
"           mediump float attr = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"   
"           attenuation = pow(spotDot, u_spot_exp) * attr;\r\n"                                      
"        }\r\n"                                                                                
"        doLighting(v_normal, lightdir, eyedir, u_light_color[i],attenuation);\r\n"                               
"    }\r\n"                       
"}\r\n"  

"void Light(int lightNum)\r\n"
"{\r\n"   
    "mediump vec3 eyedir = normalize(u_eyePos - vertex_pos);\r\n"
    "mediump vec3 v_normal = normalize(vertex_normal);\r\n"
"    for(int i = 0; i < 4; ++i)\r\n"                                                    
    "{\r\n" 
        "if(i >= lightNum)\r\n"
        "{\r\n"
           " return;\r\n"
        "}\r\n"
        "mediump vec4 pos = u_PointLightPos[i];\r\n"                                                   
        "mediump vec3 att = u_distance_point_para[i];\r\n"                                             
        //now direction light push dir to pos
        "mediump vec3 lightdir = pos.w * (pos.xyz - vertex_pos) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
        "mediump vec3 lightvec = vertex_pos - pos.xyz;\r\n"                                        
        "mediump float distance = length(lightvec);\r\n"                                               
        "mediump float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n" 
        "mediump float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"    
        "doLighting(v_normal,lightdir,eyedir,u_light_color[i],resultatt);\r\n"                                                 
"    }\r\n"                                                                                    
//"    DiffuseLight = diff;\r\n"                                                                 
//"    SpecularLight = spec;\r\n"                                                                
"}\r\n"

"void main()\r\n"                                                                          
"{\r\n"    
"Light(int(clamp(u_lightsNum.y,0.0,4.0)));\r\n"  
"SpotLight(int(clamp(u_lightsNum.z,0.0,4.0)));\r\n"  

"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
"lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * diff + spec,texcolor.a);\r\n"                                    

"gl_FragColor = vec4(lightcolor.rgb * u_frag_para,lightcolor.a * u_frag_alpha);\r\n"
"}\r\n";                              
    simplelighting_perpixel_shader.fragment_shader = simplelighting_perpixel_fragment_shader;

    shaderArray.push_back(simplelighting_perpixel_shader);



    //downsample shader
    shader downsample_shader;
    downsample_shader.shaderId = "downsample_shader";
    downsample_shader.shaderClassName = "SE_DownSampleShaderProgram";

    const char downsample_vertex_shader[] = 
                "attribute highp vec2 ds_position;\r\n"
                "attribute mediump vec2 ds_tex_coord;\r\n"                
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = vec4(ds_position, 0.0, 1.0);\r\n"           
                "v_tex_coord = ds_tex_coord;\r\n"
                "}";
    downsample_shader.vertex_shader = downsample_vertex_shader;    

    const char downsample_fragment_shader[] =
                "varying mediump vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D ds_texture;\r\n"
                "void main()\r\n"
                "{\r\n"            
                "gl_FragColor = texture2D(ds_texture, v_tex_coord);\r\n"
                "}\r\n";
    downsample_shader.fragment_shader = downsample_fragment_shader;

    shaderArray.push_back(downsample_shader);   


    //generate dof scene shader
    shader dofgen_shader;
    dofgen_shader.shaderId = "dofgen_shader";
    dofgen_shader.shaderClassName = "SE_DofGenShaderProgram";

    const char dofgen_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform highp mat4 u_m2v_matrix;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "varying mediump float pos_depth;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "vec4 pInView = u_m2v_matrix * vec4(a_position, 1.0);\r\n"
                "pos_depth = pInView.z;\r\n"
                "v_tex_coord = a_tex_coord;\r\n"
                "}";
    dofgen_shader.vertex_shader = dofgen_vertex_shader;    

    const char dofgen_fragment_shader[] =
                "varying mediump vec2 v_tex_coord;\r\n"
                "varying mediump float pos_depth;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"
                "uniform highp vec4 u_dof_para;\r\n"
                "mediump float ComputeDepthBlur(mediump float depth)\r\n"
                "{\r\n"
                    "mediump float f;\r\n"
                    "if(depth < u_dof_para.y)\r\n"
                    "{\r\n"
                        "f = (depth - u_dof_para.y) / (u_dof_para.y - u_dof_para.x);\r\n"
                    "}\r\n"
                    "else\r\n"
                    "{\r\n"
                        "f = (depth - u_dof_para.y) /(u_dof_para.z - u_dof_para.y);\r\n"
                        "f = clamp(f,0.0,u_dof_para.w);\r\n"
                    "}\r\n"
                    "return f * 0.5 + 0.5;\r\n"                
                "}\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp vec4 texcolor = texture2D(u_texture, v_tex_coord);\r\n"
                    "mediump float result = ComputeDepthBlur(-pos_depth);\r\n"
                    "gl_FragColor = vec4(texcolor.rgb,result);\r\n"                    
                "}\r\n";
    dofgen_shader.fragment_shader = dofgen_fragment_shader;

    shaderArray.push_back(dofgen_shader);

    //render dof scene shader
    shader drawdof_shader;
    drawdof_shader.shaderId = "drawdof_shader";
    drawdof_shader.shaderClassName = "SE_DrawDofShaderProgram";

    const char drawdof_vertex_shader[] = 
                "attribute highp vec2 scene_position;\r\n"
                "attribute highp vec2 scene_tex_coord;\r\n"                
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = vec4(scene_position, 0.0, 1.0);\r\n"
                "v_tex_coord = scene_tex_coord;\r\n"
                "}";
    drawdof_shader.vertex_shader = drawdof_vertex_shader;    

    const char drawdof_fragment_shader[] =
                "#define NUM_TAPS 8\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "uniform highp sampler2D u_texture_source_high;\r\n"
                "uniform highp sampler2D u_texture_source_low;\r\n"
                "uniform highp vec2 poisson[NUM_TAPS];\r\n"
                "uniform mediump vec2 OneOnPixelSizeHigh;\r\n"
                "uniform mediump vec2 OneOnPixelSizeLow;\r\n"
                "mediump vec2 maxcoc = vec2(5.0,10.0);\r\n"
                "lowp float radiusScale = 0.4;\r\n"

                "lowp vec4 PoissonDofFilter(mediump vec2 scene_texCoord)\r\n"
                "{\r\n"
                    "mediump vec4 cOut;\r\n"
                    "mediump float discRadius, discRadiusLow,centerDepth;\r\n"
                    "cOut = texture2D(u_texture_source_high, v_tex_coord);\r\n"
                    "centerDepth = cOut.a;\r\n"
                    "discRadius = abs(cOut.a * maxcoc.y - maxcoc.x);\r\n"
                    "discRadiusLow = discRadius * radiusScale;\r\n"
                    "for(int t = 0; t < NUM_TAPS; t++)\r\n"
                    "{\r\n"
                        "mediump vec2 coordLow = scene_texCoord + (OneOnPixelSizeLow * poisson[t] * discRadiusLow);\r\n"
                        "mediump vec2 coordHigh = scene_texCoord + (OneOnPixelSizeHigh * poisson[t] * discRadius);\r\n"
                        "mediump vec4 tapLow = texture2D(u_texture_source_low,coordLow);\r\n"
                        "mediump vec4 tapHigh = texture2D(u_texture_source_high,coordHigh);\r\n"
                        "mediump float tapBlur = abs(tapHigh.a * 2.0 - 1.0);\r\n"
                        "mediump vec4 tap = mix(tapHigh,tapLow,tapBlur);\r\n"
                        "tap.a = (tap.a >= centerDepth) ? 1.0 : abs(tap.a * 2.0 - 1.0);\r\n"
                        "cOut.rgb += tap.rgb * tap.a;\r\n"
                        "cOut.a += tap.a;\r\n"
                    "}\r\n"
                    "return (cOut / cOut.a);\r\n"
                "}\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp vec4 texcolor = PoissonDofFilter(v_tex_coord);\r\n"                    
                    "gl_FragColor = texcolor;\r\n"
                "}\r\n";
    drawdof_shader.fragment_shader = drawdof_fragment_shader;

    shaderArray.push_back(drawdof_shader);

    //generate light dof scene shader
    shader doflightgen_shader;
    doflightgen_shader.shaderId = "doflightgen_shader";
    doflightgen_shader.shaderClassName = "SE_DofLightGenShaderProgram";

    const char doflightgen_vertex_shader[] = 
        //for light                                
                "attribute vec3 a_normal;\r\n"
                "uniform vec4 u_PointLightPos[4];\r\n"                                                         
                "uniform vec4 u_lightsNum;\r\n"          
                "uniform vec3 u_distance_point_para[4];\r\n"
                "uniform vec3 u_light_color[4];\r\n"
                "uniform float u_dirlight_strength[4];\r\n"
                "uniform float u_material_bias;\r\n"                                                           
                "uniform float u_material_scale;\r\n"
                "varying vec3  SpecularLight;\r\n"            
                "varying vec3 DiffuseLight;\r\n"                                                               
                "vec3 diff = vec3(0.0);\r\n"                                                                   
                "vec3 spec = vec3(0.0);\r\n"                                                           
                "uniform vec2 u_uvtiling;\r\n"  
                "uniform float u_needSpec;\r\n"
        //for dofgen
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform highp mat4 u_m2v_matrix;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "varying mediump float pos_depth;\r\n"

//light
                "void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
                "{\r\n"   

		                "float ndotl = max(0.0,dot(normal, lightDir));\r\n" 
                    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
                    "spec += u_needSpec * vec3(max((ndotl - u_material_bias) * u_material_scale, 0.0));\r\n"    
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
                "vec4 pInView = u_m2v_matrix * vec4(a_position, 1.0);\r\n"
                "pos_depth = pInView.z;\r\n"
                "v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"
                "}";
    doflightgen_shader.vertex_shader = doflightgen_vertex_shader;    

    const char doflightgen_fragment_shader[] =
//for light
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
                "lowp vec3 amb = vec3(0.0);\r\n"
        //dof
                "varying mediump vec2 v_tex_coord;\r\n"
                "varying mediump float pos_depth;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"
                "uniform highp vec4 u_dof_para;\r\n"
                "mediump float ComputeDepthBlur(mediump float depth)\r\n"
                "{\r\n"
                    "mediump float f;\r\n"
                    "if(depth < u_dof_para.y)\r\n"
                    "{\r\n"
                        "f = (depth - u_dof_para.y) / (u_dof_para.y - u_dof_para.x);\r\n"
                    "}\r\n"
                    "else\r\n"
                    "{\r\n"
                        "f = (depth - u_dof_para.y) /(u_dof_para.z - u_dof_para.y);\r\n"
                        "f = clamp(f,0.0,u_dof_para.w);\r\n"
                    "}\r\n"
                    "return f * 0.5 + 0.5;\r\n"                
                "}\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
                    "lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * DiffuseLight + SpecularLight + amb,texcolor.a);\r\n"                                    
                    "lowp float resultalpha = (useUserAlpha * u_frag_alpha) + (1.0- useUserAlpha);\r\n"                         
                    "lowp vec3 usercolor = useUserColor * u_frag_para.rgb + vec3(1.0 - useUserColor);\r\n"
                    "mediump float result = ComputeDepthBlur(-pos_depth);\r\n"
                    "gl_FragColor = vec4(lightcolor.rgb * usercolor,result);\r\n"   
                    //"lowp float IntenSity = 0.2990 * gl_FragColor.r + 0.5870 * gl_FragColor.g  + 0.1140 * gl_FragColor.b;\r\n"
                    //"gl_FragColor = vec4(IntenSity,IntenSity,IntenSity,lightcolor.a * result);\r\n"
                "}\r\n";
    doflightgen_shader.fragment_shader = doflightgen_fragment_shader;
    shaderArray.push_back(doflightgen_shader);


    //flag effect shader
    shader flagwave_shader;
    flagwave_shader.shaderId = "flagwave_shader";
    flagwave_shader.shaderClassName = "SE_FlagWaveShaderProgram";

    const char flagwave_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "uniform highp vec4 u_flagwave_para;\r\n"
                "uniform lowp vec2 u_reverse_texture;\r\n"    
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "vec3 v = a_position;\r\n"
#if 1
                "float sinOff = (v.x+ v.y + v.z) * u_flagwave_para.z/18.0; \r\n"
                "float t= u_flagwave_para.x * u_flagwave_para.y; \r\n"                
                "float fx = a_tex_coord.x; \r\n"
                "float fy = a_tex_coord.x*a_tex_coord.y; \r\n"                


                "v.x += sin(t*1.45 + sinOff) * fx * 0.2 * 18.0;\r\n" 
                "v.y = (sin(t * 3.12 + sinOff) * fx * 0.5 - fy * 0.6) * 18.0;\r\n" 
                "v.z -= sin(t * 2.2 + sinOff) * fx * 0.1* 18.0; \r\n"
#endif
                //"float sinOff = (v.x+ v.y + v.z) * 1.0; \r\n"
                //"float t= u_flagwave_para.x * 50.0; \r\n"
                //"float fx = a_tex_coord.x; \r\n"
                //"float fy = a_tex_coord.x*a_tex_coord.y; \r\n"
                //"v.y = sin(t*3.12+sinOff)*fx*0.5-fy*0.9;\r\n" 
                //"v.y = 2.0 * a_tex_coord.x * sin(v.x + u_flagwave_para.x) ;\r\n"
                //"v.y += sin(v.z/2.0 + u_flagwave_para.x);\r\n"
                //"v.y *= sin(v.x * 0.1);\r\n"
                "gl_Position = u_wvp_matrix * vec4(v, 1.0);\r\n"

                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"

                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    flagwave_shader.vertex_shader = flagwave_vertex_shader;    

    const char flagwave_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"

                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
            "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
            "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"
            //"lowp float IntenSity = 0.2990 * resultColor.r + 0.5870 * resultColor.g  + 0.1140 * resultColor.b;\r\n"
            "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
            "lowp vec3 final = (1.0 - useBWColor) * resultColor + useBWColor * bwcolor;\r\n"
            "gl_FragColor = vec4(final,texcolor.a * u_frag_alpha);\r\n"            
                "}\r\n";
    flagwave_shader.fragment_shader = flagwave_fragment_shader;
    shaderArray.push_back(flagwave_shader); 

    //flag fog effect shader
    shader flagwavefog_shader;
    flagwavefog_shader.shaderId = "flagwave_fog_shader";
    flagwavefog_shader.shaderClassName = "SE_FlagWaveFogShaderProgram";

    const char flagwavefog_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "uniform highp vec4 u_flagwave_para;\r\n"
                "uniform lowp vec2 u_reverse_texture;\r\n"  
                "uniform highp mat4 M2VMatrix;\n"
                "uniform highp float u_fogEnd;\r\n"
                "uniform highp float u_fogRcpEndStartDiff;\r\n"
                "uniform highp float u_FogDensity;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "vec3 v = a_position;\r\n"
#if 1
                "float sinOff = (v.x+ v.y + v.z) * u_flagwave_para.z / 18.0; \r\n"
                "float t= u_flagwave_para.x * u_flagwave_para.y; \r\n"                
                "float fx = a_tex_coord.x; \r\n"
                "float fy = a_tex_coord.x*a_tex_coord.y; \r\n"          
               

                "v.x += sin(t*1.45 + sinOff) * fx * 0.2 * 18.0;\r\n" 
                "v.y = (sin(t * 3.12 + sinOff) * fx * 0.5 - fy * 0.6) * 18.0;\r\n" 
                "v.z -= sin(t * 2.2 + sinOff) * fx * 0.1 * 18.0; \r\n"
#endif
                //"float sinOff = (v.x+ v.y + v.z) * 1.0; \r\n"
                //"float t= u_flagwave_para.x * 50.0; \r\n"
                //"float fx = a_tex_coord.x; \r\n"
                //"float fy = a_tex_coord.x*a_tex_coord.y; \r\n"
                //"v.y = sin(t*3.12+sinOff)*fx*0.5-fy*0.9;\r\n" 
                //"v.y = 2.0 * a_tex_coord.x * sin(v.x + u_flagwave_para.x) ;\r\n"
                //"v.y += sin(v.z/2.0 + u_flagwave_para.x);\r\n"
                //"v.y *= sin(v.x * 0.1);\r\n"
                "gl_Position = u_wvp_matrix * vec4(v, 1.0);\r\n"

                "highp vec4 viewPos = M2VMatrix * vec4(a_position, 1.0);\r\n"
                "highp float eyeDist = length(viewPos);\r\n"
                "highp float fogIntensity = (u_fogEnd - eyeDist) * u_fogRcpEndStartDiff;\r\n"
                "FogIntensity = vec3(clamp(fogIntensity, 0.0, 1.0));\r\n"

                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"

                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    flagwavefog_shader.vertex_shader = flagwavefog_vertex_shader;    

    const char flagwavefog_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "varying mediump vec3 FogIntensity; \r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp vec3 u_FogColor;\r\n"
                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
            "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
            "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"
            //"lowp float IntenSity = 0.2990 * resultColor.r + 0.5870 * resultColor.g  + 0.1140 * resultColor.b;\r\n"
            "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
            "lowp vec3 final = (1.0 - useBWColor) * resultColor + useBWColor * bwcolor;\r\n"
            "lowp vec3 finalfog = mix(u_FogColor,final,FogIntensity);\r\n"
            "gl_FragColor = vec4(finalfog,texcolor.a * u_frag_alpha);\r\n"            
                "}\r\n";
    flagwavefog_shader.fragment_shader = flagwavefog_fragment_shader;

    shaderArray.push_back(flagwavefog_shader); 
    /////////////////



    //flag1 effect shader
    shader cloakflagwave_shader;
    cloakflagwave_shader.shaderId = "cloakflagwave_shader";
    cloakflagwave_shader.shaderClassName = "SE_CloakFlagWaveShaderProgram";

    const char cloakflagwave_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n"
                "uniform highp vec4 u_flagwave_para;\r\n"
                "uniform lowp vec2 u_reverse_texture;\r\n"    
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "vec3 v = a_position;\r\n"
#if 1
                "float sinOff = (v.x+ v.y + v.z) * u_flagwave_para.z; \r\n"
                "float t= u_flagwave_para.x * u_flagwave_para.y; \r\n"                
                "float fx = a_tex_coord.x; \r\n"
                "float fy = a_tex_coord.x*a_tex_coord.y; \r\n"          
               

                "v.x += sin(t*1.45 + sinOff) * fx * 0.2;\r\n" 
                //"v.y += sin(t * 3.12 + sinOff) * fx * 0.5 - fy * 0.6;\r\n" 
                "v.z -= sin(t * 2.2 + sinOff) * fx * 0.9; \r\n"
#endif
                //"float sinOff = (v.x+ v.y + v.z) * 1.0; \r\n"
                //"float t= u_flagwave_para.x * 50.0; \r\n"
                //"float fx = a_tex_coord.x; \r\n"
                //"float fy = a_tex_coord.x*a_tex_coord.y; \r\n"
                //"v.y = sin(t*3.12+sinOff)*fx*0.5-fy*0.9;\r\n" 
                //"v.y = 2.0 * a_tex_coord.x * sin(v.x + u_flagwave_para.x) ;\r\n"
                //"v.y += sin(v.z/2.0 + u_flagwave_para.x);\r\n"
                //"v.y *= sin(v.x * 0.1);\r\n"
                "gl_Position = u_wvp_matrix * vec4(v, 1.0);\r\n"

                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"

                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    cloakflagwave_shader.vertex_shader = cloakflagwave_vertex_shader;    

    const char cloakflagwave_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"

                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
            "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"
            "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"
            //"lowp float IntenSity = 0.2990 * resultColor.r + 0.5870 * resultColor.g  + 0.1140 * resultColor.b;\r\n"
            "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"
                    
            "lowp vec3 final = (1.0 - useBWColor) * resultColor + useBWColor * bwcolor;\r\n"
            "gl_FragColor = vec4(final,texcolor.a * u_frag_alpha);\r\n"            
                "}\r\n";
    cloakflagwave_shader.fragment_shader = cloakflagwave_fragment_shader;

    shaderArray.push_back(cloakflagwave_shader); 


    //color effect shader
    shader coloreffect_shader;
    coloreffect_shader.shaderId = "coloreffect_shader";
    coloreffect_shader.shaderClassName = "SE_ColorEffectShaderProgram";
    const char coloreffect_vertex_shader[] = 
                "attribute vec3 a_position;\r\n"
                "attribute vec2 a_tex_coord;\r\n"
                "uniform mat4 u_wvp_matrix;\r\n"
                "varying vec2 v_tex_coord;\r\n"
                "uniform vec2 u_uvtiling;\r\n"
                "varying vec2 v_tex_coord_tiling;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "v_tex_coord = a_tex_coord;\r\n"
                "v_tex_coord_tiling = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"

                "}";
    coloreffect_shader.vertex_shader = coloreffect_vertex_shader;

    const char coloreffect_fragment_shader[] =
                "precision mediump float;\r\n"
                "varying vec2 v_tex_coord;\r\n"
                "uniform sampler2D u_texture;\r\n"
                "uniform sampler2D u_2ndTexture;\r\n"                                         
                "uniform vec3 u_color;\r\n"
                "uniform int u_shading_mode;\r\n"
                "uniform vec3 u_frag_para;\r\n" 
                "uniform float u_frag_alpha;\r\n"
                "uniform int useUserAlpha;\r\n" 
                "uniform int useUserColor;\r\n"               
                "varying vec2 v_tex_coord_tiling;\r\n"
                "void main()\r\n"
                "{\r\n"                
                "if(u_shading_mode == 0)\r\n"
                "{\r\n"
                "gl_FragColor = vec4(u_color, 1.0);\r\n"                                        
                //"float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * u_frag_alpha;\r\n"
                //"vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(u_frag_para,1.0);\r\n"                                                 
                "}\r\n"                                                                         
                "else if(u_shading_mode == 1)\r\n"                                              
                "{\r\n"                                                                         
                //"vec2 texcoord = v_tex_coord_tiling;\r\n"                                              
                "vec4 basecolor = texture2D(u_texture, v_tex_coord_tiling);\r\n"                          
                "gl_FragColor = basecolor;\r\n"                                                 
                //"float result = float(useUserAlpha) * u_frag_alpha + float(1- useUserAlpha) * 1.0;\r\n"
                "gl_FragColor.a = gl_FragColor.a * u_frag_alpha;\r\n"
                //"vec3 usercolor = float(useUserColor) * vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z) + float(1 - useUserColor) * vec3(1.0);\r\n"
                "gl_FragColor = gl_FragColor * vec4(u_frag_para,1.0);\r\n"
                  
                "}\r\n"                                                                         
                "}\r\n"
                "else\r\n"
                "{\r\n"     
                //"vec2 texcoord = v_tex_coord;\r\n"                                              
                "vec4 basecolor = texture2D(u_texture, v_tex_coord_tiling);\r\n"                          
                "vec4 helpcolor = texture2D(u_2ndTexture, v_tex_coord);\r\n"                 
                "gl_FragColor = min(basecolor + (basecolor * helpcolor) / (vec4(1.0) - helpcolor),vec4(1.0));\r\n"                                 
                "if(useUserAlpha > 0)\r\n"                                                      
                "{\r\n"                                                                         
                "gl_FragColor.a = gl_FragColor.a * u_frag_alpha;\r\n"                       
                "}\r\n"                                                                         
                "if(useUserColor > 0)\r\n"                                                      
                "{\r\n"                                                                         
                //"vec3 usercolor = vec3(u_frag_para.x,u_frag_para.y,u_frag_para.z);\r\n"
                "gl_FragColor = gl_FragColor * vec4(u_frag_para,1.0);\r\n"                        
                "}\r\n"                                                                         
                "}\r\n" 
                "}\r\n";

   // MIN( A +��A��B��/��255-B��,255)
    coloreffect_shader.fragment_shader = coloreffect_fragment_shader;
    shaderArray.push_back(coloreffect_shader);

    //draw target to screen shader
    shader drawrendertargettoscreen_shader;
    drawrendertargettoscreen_shader.shaderId = "drawrendertargettoscreen_shader";
    drawrendertargettoscreen_shader.shaderClassName = "SE_DrawRenderTargetToScreenShaderProgram";

    const char drawrendertargettoscreen_vertex_shader[] = 
                "attribute highp vec2 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"             
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = vec4(a_position, 0.0, 1.0);\r\n"           
                "v_tex_coord = a_tex_coord;\r\n"
                "}";
    drawrendertargettoscreen_shader.vertex_shader = drawrendertargettoscreen_vertex_shader;    

    const char drawrendertargettoscreen_fragment_shader[] =
                "varying mediump vec2 v_tex_coord;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"
                "uniform lowp sampler2D u_2ndTexture;\r\n"
                "void main()\r\n"
                "{\r\n"            
                "lowp vec3 basecolor = texture2D(u_texture, v_tex_coord).rgb;\r\n"                          
                "lowp vec3 helpcolor = texture2D(u_2ndTexture, v_tex_coord).rgb;\r\n" 
                "lowp vec3 result = min(basecolor + (basecolor * helpcolor) / (vec3(1.0) - helpcolor),vec3(1.0)); \r\n"
                //"lowp vec3 result = basecolor * helpcolor; \r\n"
                "gl_FragColor = vec4(result,1.0);\r\n"       
                "}\r\n";
    drawrendertargettoscreen_shader.fragment_shader = drawrendertargettoscreen_fragment_shader;

    shaderArray.push_back(drawrendertargettoscreen_shader); 

    //draw depth shader
    shader drawdepth_shader;
    drawdepth_shader.shaderId = "drawdepth_shader";
    drawdepth_shader.shaderClassName = "SE_DrawDepthShaderProgram";

    const char drawdepth_vertex_shader[] = 
                "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"                
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                
                "v_tex_coord = a_tex_coord;\r\n"
                "}";
    drawdepth_shader.vertex_shader = drawdepth_vertex_shader;    

    const char drawdepth_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"                            
                "void main()\r\n"
                "{\r\n"
                    "gl_FragColor = vec4(1.0,1.0,1.0,1.0);\r\n"
                "}\r\n";
    drawdepth_shader.fragment_shader = drawdepth_fragment_shader;

    shaderArray.push_back(drawdepth_shader);  

    //render depthmap
    shader renderdepthshadow_shader;
    renderdepthshadow_shader.shaderId = "renderdepthshadow_shader";
    renderdepthshadow_shader.shaderClassName = "SE_RenderDepthShadowShaderProgram";

    const char renderdepth_vertex_shader[] = 
                 "attribute highp vec3 a_position;\r\n"
                "attribute highp vec2 a_tex_coord;\r\n"
                "uniform highp mat4 u_wvp_matrix;\r\n"
                "uniform mediump vec2 u_uvtiling;\r\n" 
                "uniform lowp vec2 u_reverse_texture;\r\n"

                "uniform highp mat4 u_texProjection_matrix;\r\n"   
                "varying highp vec4 projCoord;\r\n"
                "varying mediump vec2 v_tex_coord;\r\n"
                "void main()\r\n"
                "{\r\n"
                "gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"
                "projCoord = u_texProjection_matrix * vec4(a_position, 1.0);\r\n"  
                "vec2 tex = a_tex_coord;\r\n"
                "tex.x = u_reverse_texture.x * (1.0 - a_tex_coord.x) + (1.0 - u_reverse_texture.x) * a_tex_coord.x;\r\n"
                "tex.y = u_reverse_texture.y * (1.0 - a_tex_coord.y) + (1.0 - u_reverse_texture.y) * a_tex_coord.y;\r\n"
                "v_tex_coord = vec2(tex.x * u_uvtiling.x,tex.y * u_uvtiling.y);\r\n"
                "}";
    renderdepthshadow_shader.vertex_shader = renderdepth_vertex_shader;    

    const char renderdepth_fragment_shader[] =
                "varying highp vec2 v_tex_coord;\r\n"
                "varying highp vec4 projCoord;\r\n"
                "uniform highp sampler2D u_depthShadow_texture;\r\n"
                "uniform lowp sampler2D u_texture;\r\n"                
                "uniform lowp vec3 u_color;\r\n"
                "uniform lowp float u_shading_mode;\r\n"
                "uniform lowp vec3 u_frag_para;\r\n"
                "uniform lowp float u_frag_alpha;\r\n"
                "uniform lowp float useBWColor;\r\n"
                "uniform lowp vec3 u_userBWColor;\r\n"
                "void main()\r\n"
                "{\r\n"
                    "lowp float fAmbient = 0.6;\r\n"
                    "highp float comp = (projCoord.z / projCoord.w) - 0.03;\n"
	                "highp float depth = texture2DProj(u_depthShadow_texture, projCoord).r;\n"
	                "lowp float shadowVal = comp <= depth ? 1.0 : fAmbient;\n"

                    "lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

                    "lowp vec3 resultColor = texcolor.rgb * u_frag_para;\r\n"

                    "lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"                    
                    
                    "lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"

                    "gl_FragColor = vec4(final * shadowVal,texcolor.a * u_frag_alpha);\r\n"
                "}\r\n";
    renderdepthshadow_shader.fragment_shader = renderdepth_fragment_shader;

    shaderArray.push_back(renderdepthshadow_shader);  


    //shadowmap with light shader
    shader shadowmapwithlight_shader;
    shadowmapwithlight_shader.shaderId = "shadowmapwithlight_shader";
    shadowmapwithlight_shader.shaderClassName = "SE_ShadowMapWithLightShaderProgram";

    const char shadowmapwithlight_vertex_shader[] = 

"attribute vec3 a_normal;\r\n"  
"uniform vec4 u_PointLightPos[4];\r\n"                                                         
"uniform vec4 u_lightsNum;\r\n"          
"uniform vec3 u_distance_point_para[4];\r\n"
"uniform vec3 u_light_color[4];\r\n"
"uniform float u_dirlight_strength[4];\r\n"
"uniform float u_material_bias;\r\n"                                                           
"uniform float u_material_scale;\r\n"
"varying vec3  SpecularLight;\r\n" 
"varying vec3 DiffuseLight;\r\n"  
"vec3 diff = vec3(0.0);\r\n"                                                                   
"vec3 spec = vec3(0.0);\r\n" 
"uniform float u_needSpec;\r\n"

"attribute highp vec3 a_position;\r\n"                                                    
"attribute highp vec2 a_tex_coord;\r\n"                                                   
"uniform highp mat4 u_wvp_matrix;\r\n"                                                    
"uniform mediump vec2 u_uvtiling;\r\n"                                                      
"uniform highp mat4 u_texProjection_matrix;\r\n"                                          
"varying mediump vec2 v_tex_coord;\r\n"                                                     
"varying highp vec4 projCoord;\r\n"  


"void doLighting(vec3 normal, vec3 lightDir,float attenuation,vec3 lightcolor)\r\n"                                        
"{\r\n"
    "float ndotl = max(0.0,dot(normal, lightDir));\r\n"
    "diff += vec3(ndotl) * attenuation *lightcolor;\r\n"
    "spec += u_needSpec * vec3(max((ndotl - u_material_bias) * u_material_scale, 0.0));\r\n"
     
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
            "void processLight(int i)\r\n"
            "{\r\n"
                "vec4 pos = u_PointLightPos[i];\r\n"
                "vec3 att = u_distance_point_para[i];\r\n"
                //now direction light push dir to pos
                "vec3 lightdir = pos.w * (pos.xyz - a_position) + (1.0 - pos.w) * normalize(-pos.xyz);\r\n"
                "vec3 lightvec = a_position - pos.xyz;\r\n"
                "float distance = length(lightvec);\r\n"
                "float attenuation = 1.0/(att.x + att.y * distance + att.z * distance * distance);\r\n"
                "float resultatt = pos.w * attenuation + (1.0 - pos.w) * u_dirlight_strength[i]; \r\n"
                "doLighting(a_normal,lightdir,resultatt,u_light_color[i]);\r\n"
            "}\r\n"
"void Light(int lightNum)\r\n"
"{\r\n"
    "if (lightNum > 0)\r\n"
        "processLight(0);\r\n"
     "if (lightNum > 1)\r\n"
         "processLight(1);\r\n"
     "if (lightNum > 2)\r\n"
          "processLight(2);\r\n"
    "DiffuseLight = diff;\r\n"
    "SpecularLight = spec;\r\n"
"}\r\n"

"void main()\r\n"                                                                   
"{\r\n"
"Light(int(u_lightsNum.y));\r\n"
"gl_Position = u_wvp_matrix * vec4(a_position, 1.0);\r\n"                           
"v_tex_coord = vec2(a_tex_coord.x * u_uvtiling.x,a_tex_coord.y * u_uvtiling.y);\r\n"
"projCoord = u_texProjection_matrix * vec4(a_position, 1.0);\r\n"                   
"}\r\n";                                                                            
              
    shadowmapwithlight_shader.vertex_shader = shadowmapwithlight_vertex_shader;    

    const char shadowmapwithlight_fragment_shader[] =

"uniform lowp vec3 u_ambient;\r\n"                                                              
"uniform lowp vec3 u_diffuse;\r\n"                                                              
"uniform lowp vec3 u_specular;\r\n" 
"varying lowp vec3  SpecularLight;\r\n"                                                         
"varying lowp vec3 DiffuseLight;\r\n"                                                           
"lowp vec3 amb = vec3(0.0);\r\n"

"varying mediump vec2 v_tex_coord;\r\n"                                        
"uniform lowp sampler2D u_texture;\r\n"                                     
"uniform lowp vec3 u_color;\r\n"                                            
"uniform lowp float u_shading_mode;\r\n"                                      
"uniform mediump vec3 u_frag_para;\r\n"                                        
"uniform highp float u_frag_alpha;\r\n"                                      
"uniform int useUserAlpha;\r\n"                                        
"uniform int useUserColor;\r\n"                                        
"uniform highp sampler2D u_shadow_texture;\r\n"                              
"varying highp vec4 projCoord;\r\n"
"uniform int useVSM;\r\n"
"uniform lowp vec3 u_userBWColor;\r\n"
"uniform lowp float useBWColor;\r\n"
"uniform mediump float u_ShadowColor;\r\n"
"highp vec4 ShadowCoordPostW;\r\n"                                                                     
"highp float chebyshevUpperBound(highp float distance)\r\n"                                                 
"{\r\n"                                                                                          
    "// We retrive the two moments previously stored (depth and depth*depth)\r\n"                    
    "highp vec2 moments = texture2D(u_shadow_texture,ShadowCoordPostW.xy).rg;\r\n"                         
    "\r\n"                                                                                           
    "// Surface is fully lit. as the current fragment is before the light occluder\r\n"              
    "if (distance <= moments.x)\r\n"                                                                 
    "{\r\n"                                                                                          
        "return 1.0;\r\n"                                                                                
    "}\r\n"                                                                                          
    "// The fragment is either in shadow or penumbra. We now use chebyshev's upperBound to check\r\n"
    "// How likely this pixel is to be lit (p_max)\r\n"                                              
    "highp float variance = moments.y - (moments.x*moments.x);\r\n"                                        
    "variance = max(variance,u_ShadowColor);\r\n"                                                            
                                                                                        
    "highp float d = distance - moments.x;\r\n"                                                            
    "highp float p_max = variance / (variance + d*d);\r\n"                                                                           
    "return p_max;\r\n"                                                                                       
"}\r\n"               

"void main()\r\n"                                                      
"{\r\n"
                                             
"ShadowCoordPostW = projCoord / projCoord.w;\r\n"                                                
"highp float result = chebyshevUpperBound(ShadowCoordPostW .z);\r\n"
"result = max(result, 0.5);\r\n"
"lowp vec4 texcolor = u_shading_mode * texture2D(u_texture, v_tex_coord) + (1.0 - u_shading_mode) * vec4(u_color,1.0);\r\n"

"lowp vec4 lightcolor = vec4(vec3(texcolor.rgb) * DiffuseLight + SpecularLight + amb,texcolor.a);\r\n"

"lowp vec3 resultColor = lightcolor.rgb * u_frag_para;\r\n"

"lowp vec3 bwcolor = vec3(dot(resultColor,u_userBWColor));\r\n"                    
                    
"lowp vec3 final = mix(resultColor,bwcolor,useBWColor);\r\n"

"gl_FragColor = vec4(final * result,texcolor.a * u_frag_alpha);\r\n"
"}\r\n";                                                                
    shadowmapwithlight_shader.fragment_shader = shadowmapwithlight_fragment_shader;

    shaderArray.push_back(shadowmapwithlight_shader);
    }



SE_ShaderDefine::~SE_ShaderDefine()
{}
