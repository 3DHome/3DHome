#ifndef SE_RENDERER_H
#define SE_RENDERER_H

class SE_ImageData;
class SE_ShaderProgram;
class SE_Surface;
class SE_ColorExtractShaderProgram;
class SE_SimpleSurfaceShaderProgram;
class SE_SkeletalAnimationShaderProgram;
class SE_LightMapShaderProgram;
class SE_SimpleLightingShaderProgram;
class SE_NormalMapShaderProgram;
class SE_RenderUnit;
/*
    Usage:
	  renderer->begin(shaderProgram);
	  renderer->setVertex(); // must first invoke setVertex and then invoke setTexVertex
	  renderer->setTexVertex();
	  renderer->end();
*/
class SE_ENTRY SE_Renderer
{
    DECLARE_OBJECT(SE_Renderer)
public:
    struct TextureAttribute
    {

        TextureAttribute()
        {
            hasTexture = false;
            uTiling = 1.0;
            vTiling = 1.0;

        }
        bool hasTexture;
        float uTiling;
        float vTiling;
    };
	enum {SE_DEPTH_BUFFER = 0x01, SE_COLOR_BUFFER = 0x02};
    SE_Renderer();
    virtual ~SE_Renderer();
    virtual void free_render();
    virtual void setMatrix(SE_RenderUnit* renderUnit);
	virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
	virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void draw(SE_RenderUnit* renderUnit);
	virtual void end();
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
	virtual void setPrimitiveType(int primitiveType)
	{
		mPrimitiveType = primitiveType;
	}
	virtual void setSurface(SE_Surface* surface)
	{
		mSurface = surface;
	}
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
	static void setClearColor(const SE_Vector4f& color);
	static void setViewport(int x, int y, int w, int h);
	static void clear(int pattern);
    static void colorMask(bool red, bool green, bool blue, bool alpha);
    static void enableDepthTest(bool enable);

    //just switch on|off
    static void enableBlend(bool enable);
    static void setDepthTestMask(bool enable);


protected:
	virtual void reset();
    virtual void setImage(int texIndex, SE_RenderUnit* renderUnit);
	virtual void setTexVertex(int index, SE_RenderUnit* renderUnit);
    bool loadTexture2D(int index, SE_ImageData* imageData, SE_WRAP_TYPE wrapS, SE_WRAP_TYPE wrapT, SE_SAMPLE_TYPE min, SE_SAMPLE_TYPE mag,bool needReGenerateMipmap = false);
protected:
    SE_ShaderProgram* mBaseShaderProgram;
    _Vector3f* mVertex;
	int mVertexNum;
	int* mIndexArray;
	int mIndexNum;
	int mPrimitiveType;
	SE_Surface* mSurface;
	int mTexVertexNum;
	_Vector2f* mTexVertex;
	int mHasTexCoord[SE_TEXUNIT_NUM];
    TextureAttribute mHasTexture[SE_TEXUNIT_NUM];
    SE_VertexBuffer *mVertexBuffer;
    bool mHasImageID;
};
class SE_ENTRY SE_ColorExtractRenderer : public SE_Renderer
{
	DECLARE_OBJECT(SE_ColorExtractRenderer)
public:
	SE_ColorExtractRenderer();
	~SE_ColorExtractRenderer();
	virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
	virtual void begin(SE_ShaderProgram* shaderProgram);
protected:
	SE_ColorExtractShaderProgram* mShaderProgram;
};
class SE_ENTRY SE_SimpleSurfaceRenderer : public SE_Renderer
{
    DECLARE_OBJECT(SE_SimpleSurfaceRenderer)
public:
	virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
	virtual void begin(SE_ShaderProgram* shaderProgram);	
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);	
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
};
class SE_ENTRY SE_FogRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_FogRenderer)
public:	
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
};

class SE_ENTRY SE_LineSegRenderer : public SE_SimpleSurfaceRenderer
{
	DECLARE_OBJECT(SE_LineSegRenderer)
public:
	SE_LineSegRenderer();
	~SE_LineSegRenderer();
	virtual void setColor(SE_RenderUnit* renderUnit);
	virtual void setMatrix(SE_RenderUnit* renderUnit);
	virtual void setVertex(SE_RenderUnit* renderUnit);
	virtual void setDrawMode(SE_RenderUnit* renderUnit);
	virtual void end();
	virtual void begin(SE_ShaderProgram* shaderProgram);
	virtual void draw(SE_RenderUnit* renderUnit);
private:
	 _Vector3f* mPoints;
	 int mPointNum;
	 SE_SimpleSurfaceShaderProgram* mShaderProgram;
};

class SE_ENTRY SE_DrawLineRenderer : public SE_SimpleSurfaceRenderer
{
	DECLARE_OBJECT(SE_DrawLineRenderer)
public:
	SE_DrawLineRenderer();
	~SE_DrawLineRenderer();


	virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit){}
    virtual void setImage(SE_RenderUnit* renderUnit){}
    virtual void setColor(SE_RenderUnit* renderUnit);
	virtual void setDrawMode(SE_RenderUnit* renderUnit);
	virtual void end();
	virtual void begin(SE_ShaderProgram* shaderProgram);
	virtual void draw(SE_RenderUnit* renderUnit);
private:
	 _Vector3f* mPoints;
	 int mPointNum;	 
};

class SE_ENTRY SE_LightMapRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_LightMapRenderer)
public:
	virtual void begin(SE_ShaderProgram* shaderProgram);	
    virtual void setImage(SE_RenderUnit* renderUnit);
};

class SE_ENTRY SE_SkeletalAnimationRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_SkeletalAnimationRenderer)
public:
	virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
	virtual void begin(SE_ShaderProgram* shaderProgram);
private:	
    void setAnimationData();
};

class SE_ENTRY SE_SimpleLightingRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_SimpleLightingRenderer)
public:	
    virtual void begin(SE_ShaderProgram* shaderProgram);    
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
private:	
    void setLightingData(SE_RenderUnit* renderUnit);
    void setMaterialData(SE_RenderUnit* renderUnit);
    
};

class SE_ENTRY SE_SimpleLightingFogRenderer : public SE_SimpleLightingRenderer
{
    DECLARE_OBJECT(SE_SimpleLightingFogRenderer)
public:	
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
};

class SE_ENTRY SE_NormalMapRenderer : public SE_SimpleLightingRenderer
{
    DECLARE_OBJECT(SE_NormalMapRenderer)
public:	
    virtual void begin(SE_ShaderProgram* shaderProgram);    
    virtual void setImage(SE_RenderUnit* renderUnit);    
private:	
    void generateTangentSpace(SE_RenderUnit* renderUnit);
    
};

class SE_ENTRY SE_ParticleRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_ParticleRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void end();
    
    
};

class SE_ENTRY SE_ShadowMapRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_ShadowMapRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setImage(SE_RenderUnit* renderUnit);   

};
class SE_ENTRY SE_ShadowMapWithLightRenderer : public SE_SimpleLightingRenderer
{
    DECLARE_OBJECT(SE_ShadowMapWithLightRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setImage(SE_RenderUnit* renderUnit);   

};

class SE_ENTRY SE_ReflectionRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_ReflectionRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setImage(SE_RenderUnit* renderUnit);   
};


class SE_ENTRY SE_MirrorRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_MirrorRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setImage(SE_RenderUnit* renderUnit);   
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
};

class SE_ENTRY SE_DrawVSMRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DrawVSMRenderer)
public:
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit){}
    virtual void setDrawMode(SE_RenderUnit* renderUnit){}
    //virtual void begin(SE_ShaderProgram* shaderProgram){}
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit){};

};

#if 0
class SE_ENTRY SE_BlurHRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_BlurHRenderer)
public:    
    SE_BlurHRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
private:   
    float afVertexData[8];
    float imageSize;
    
};

class SE_ENTRY SE_BlurVRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_BlurVRenderer)
public:    
    SE_BlurVRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);    
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);    
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
    float afVertexData[8];    
    float imageSize;
};
#endif

class SE_ENTRY SE_BlurRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_BlurRenderer)
public:    
    SE_BlurRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
    float afVertexData[8]; 
    float afTexCoordData[8];

    float m_fTexelOffset;
};

class SE_ENTRY SE_DownSampleRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DownSampleRenderer)
public:    
    SE_DownSampleRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
    float afVertexData[8]; 
    float afTexCoordData[8];

};

class SE_ENTRY SE_AlphaTestRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_AlphaTestRenderer)
public:
    
};

class SE_ENTRY SE_SimpleNoImgRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_SimpleNoImgRenderer)
public:    
    SE_SimpleNoImgRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setTexVertex(SE_RenderUnit* renderUnit);
    virtual void setVertex(SE_RenderUnit* renderUnit);
    virtual void setDrawMode(SE_RenderUnit* renderUnit);
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);

};

class SE_ENTRY SE_SimpleUVAnimationRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_SimpleUVAnimationRenderer)
public:    
    SE_SimpleUVAnimationRenderer();    
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);   

};

class SE_ENTRY SE_SimpleUVAnimationFogRenderer : public SE_SimpleUVAnimationRenderer
{
    DECLARE_OBJECT(SE_SimpleUVAnimationFogRenderer)
public:    
    SE_SimpleUVAnimationFogRenderer();    
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);   

};

class SE_ENTRY SE_DofGenRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DofGenRenderer)
public:    
    SE_DofGenRenderer();
    //virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    //virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);    
};

class SE_ENTRY SE_DofLightGenRenderer : public SE_SimpleLightingRenderer
{
    DECLARE_OBJECT(SE_DofLightGenRenderer)
public:    
    SE_DofLightGenRenderer();
    //virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    //virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);    
};

class SE_ENTRY SE_DrawDofRenderer : public SE_DownSampleRenderer
{
    DECLARE_OBJECT(SE_DrawDofRenderer)
public:    
    SE_DrawDofRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    //virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
    float poisson8samples[16];
};

class SE_ENTRY SE_FlagWaveRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_FlagWaveRenderer)
public:    
    SE_FlagWaveRenderer();
    //virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    //virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    //virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
protected:
    SE_Vector4f mFlagWavePara;
    
};

class SE_ENTRY SE_FlagWaveFogRenderer : public SE_FlagWaveRenderer
{
    DECLARE_OBJECT(SE_FlagWaveFogRenderer)
public:    
    SE_FlagWaveFogRenderer();    
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    
protected:
    
    
};

class SE_ENTRY SE_CloakFlagWaveRenderer : public SE_FlagWaveRenderer
{
    DECLARE_OBJECT(SE_CloakFlagWaveRenderer)
public:    
    SE_CloakFlagWaveRenderer()
    {}
    
};


class SE_ENTRY SE_ColorEffectRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_ColorEffectRenderer)
public:    
    SE_ColorEffectRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    //virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    //virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
private:
    float afVertexData[8]; 
    float afTexCoordData[8];
    
};

class SE_ENTRY SE_DrawRenderTargetToScreenRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DrawRenderTargetToScreenRenderer)
public:    
    SE_DrawRenderTargetToScreenRenderer();
    virtual void setImage(SE_RenderUnit* renderUnit);
    //virtual void setColor(SE_RenderUnit* renderUnit);
    //virtual void setMatrix(SE_RenderUnit* renderUnit);
    //virtual void setTexVertex(SE_RenderUnit* renderUnit);
    //virtual void setVertex(SE_RenderUnit* renderUnit);
    //virtual void setDrawMode(SE_RenderUnit* renderUnit);
    //virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setVertexBuffer(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    //virtual void setUserStatusEnd(SE_RenderUnit* renderUnit);
private:
    float afVertexData[8]; 
    float afTexCoordData[8];
    
};

class SE_ENTRY SE_DrawDepthRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DrawDepthRenderer)
public:	
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    virtual void setMatrix(SE_RenderUnit* renderUnit);
};

class SE_ENTRY SE_RenderDepthShadowRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_RenderDepthShadowRenderer)
public:    
    virtual void begin(SE_ShaderProgram* shaderProgram);
    virtual void setMatrix(SE_RenderUnit* renderUnit);

    virtual void setImage(SE_RenderUnit* renderUnit);   

    
};

class SE_ENTRY SE_DrawMirrorRenderer : public SE_SimpleSurfaceRenderer
{
    DECLARE_OBJECT(SE_DrawMirrorRenderer)
public:    
    virtual void setMatrix(SE_RenderUnit* renderUnit);
    virtual void setUserStatusBegin(SE_RenderUnit* renderUnit);
    
};
//class SE_ENTRY SE_LightAndShadowRenderer : public SE_ShadowMapRenderer,public SE_SimpleLightingRenderer
//{
//    DECLARE_OBJECT(SE_LightAndShadowRenderer)
//public:    
//    virtual void begin(SE_ShaderProgram* shaderProgram);
//    virtual void setMatrix(SE_RenderUnit* renderUnit);
//    virtual void setImage(SE_RenderUnit* renderUnit);
//    
//};
#endif
