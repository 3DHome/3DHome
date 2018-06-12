#ifndef SE_VERTEXBUFFER_H
#define SE_VERTEXBUFFER_H

#include <vector>

class SE_ENTRY SE_VertexFormat
{
public:
    enum TYPE {POSITION, POSITION_COLOR, POSITION_TEX0, POSITION_TEX0_TEX1, 
          POSITION_TEX0_TEX1_COLOR, POSITION_TEX0_NORMAL,
          POSITION_TEX0_TEX1_NORMAL, POSITION_TEX0_TEX1_NORMAL_COLOR
    };

    struct Pos
    {
        Pos()
        {
            pos.d[0] = 0.0;
            pos.d[1] = 0.0;
            pos.d[2] = 0.0;

            normal.d[0] = 0.0;
            normal.d[1] = 0.0;
            normal.d[2] = 0.0;
        }
        _Vector3f pos;
        _Vector3f normal;
    };

    struct PosTex0
    {
        PosTex0()
        {
            pos.d[0] = 0.0;
            pos.d[1] = 0.0;
            pos.d[2] = 0.0;

            tex0.d[0] = 0.0;
            tex0.d[1] = 0.0;

            normal.d[0] = 0.0;
            normal.d[1] = 0.0;
            normal.d[2] = 0.0;
        }
        _Vector3f pos;
        _Vector3f normal;
        _Vector2f tex0;
        
    };

    struct PosTex0Tex1
    {
        PosTex0Tex1()
        {
            pos.d[0] = 0.0;
            pos.d[1] = 0.0;
            pos.d[2] = 0.0;

            tex0.d[0] = 0.0;
            tex0.d[1] = 0.0;

            tex1.d[0] = 0.0;
            tex1.d[1] = 0.0;
        }
        _Vector3f pos;
        _Vector2f tex0;
        _Vector2f tex1;
    };

    struct PosColor
    {
        PosColor()
        {
            pos.d[0] = 0.0;
            pos.d[1] = 0.0;
            pos.d[2] = 0.0;
            color.d[0] = 0.0;
            color.d[1] = 0.0;
            color.d[2] = 0.0;
            color.d[3] = 0.0;
        }
        _Vector3f pos;
        _Vector4f color;
    };
};


class SE_ENTRY SE_VertexBuffer
{
public:
    enum VertexBufferState
    {
        VBS_NONE = 0x00000000,
        VBS_VERTEXPOS = 0x00000001,
        VBS_TEXTURE0 = 0x00000002,
        VBS_TEXTURE1 = 0x00000004,
        VBS_VERTEXCOLOR = 0x00000008,
        VBS_NORMAL = 0x00000010,
        VBS_TANGENTSPACE = 0x00000020
    };

    SE_VertexBuffer();
    ~SE_VertexBuffer();

    void addVBState(VertexBufferState state)
    {
        mVBS |= state;
    }
    bool hasVBState(VertexBufferState state)
    {
        return (mVBS & state) == state;
    }
    void clearVBState()
    {
        mVBS = VBS_NONE;
    }
    bool isNonState()
    {
        return mVBS & VBS_NONE;
    }
    unsigned int getVBState()
    {
        return mVBS;
    }
    void setVBState(unsigned int state)
    {
        mVBS = state;
    }

    void createVertexBuffer(const char*objname,
                            int facenum,                        //number of trangles(raw data from 3d max)
                            _Vector3i *faces,                   //vertex index of the trangle(raw data from 3d max)
                            _Vector3f *vertexPos,               //vertex data for trangle(raw data from 3d max)
                            _Vector3i *tfaces = NULL,           //texture coordinate index(raw data from 3d max)
                            _Vector2f *textureCoord0 = NULL,    //texture coordinate data(raw data from 3d max)
                            _Vector2f *textureCoord1 = NULL,

                            _Vector4f *color = NULL,
                            _Vector3f *normal = NULL                      
                            
                            );

    void initVBO();
    void bindVBO();
    void unBindVBO();
    void releaseHardware();
    bool vboHasInit()
    {
        return mVboHasInit;
    }    

    void setDyNamicDraw(bool is)
    {
        mDynamicDraw = is;
    }
public:
    int getDataStride();
    int getVertexDataSize();
    float* vertexData;
    int vertexDataNum;
    unsigned short* indexData;
    int indexNum;
    bool mHasTexture;

    int mPosSize;
    int mTex0Size;
    int mTex1Size;
    int mColorSize;
    int mNormalSize;
    int mTangentSize;

    
    
private:
    

    int addVertexData(SE_VertexFormat::Pos v);

    int addVertexData(SE_VertexFormat::PosTex0 v);

    int addVertexData(SE_VertexFormat::PosTex0Tex1 v);

    int addVertexData(SE_VertexFormat::PosColor v);


    void createVertexBuffer(int facenum,                        //number of trangles(raw data from 3d max)
                            _Vector3i *faces,                   //vertex index of the trangle(raw data from 3d max)
                            _Vector3f *vertexPos,               //vertex data for trangle(raw data from 3d max)
                            _Vector3f *vertexNormal
                            );

    void createVertexBuffer(int facenum,                        //number of trangles(raw data from 3d max)
                            _Vector3i *faces,                   //vertex index of the trangle(raw data from 3d max)
                            _Vector3f *vertexPos,              //vertex data for trangle(raw data from 3d max)
                            _Vector3i *tfaces,           //texture coordinate index(raw data from 3d max)
                            _Vector2f *textureCoord0,    //texture coordinate data(raw data from 3d max)
                            _Vector3f *vertexNormal
                            );

    void createVertexBuffer(int facenum,                        //number of trangles(raw data from 3d max)
                            _Vector3i *faces,                   //vertex index of the trangle(raw data from 3d max)
                            _Vector3f *vertexPos,              //vertex data for trangle(raw data from 3d max)
                            _Vector3i *tfaces,           //texture coordinate index(raw data from 3d max)
                            _Vector2f *textureCoord0,    //texture coordinate data(raw data from 3d max)
                            _Vector2f *textureCoord1
                            );

    void createVertexBuffer(int facenum,                        //number of trangles(raw data from 3d max)
                            _Vector3i *faces,                   //vertex index of the trangle(raw data from 3d max)
                            _Vector3f *vertexPos,               //vertex data for trangle(raw data from 3d max)
                            _Vector4f *color
                            );

private:
    struct Impl;
    Impl* mImpl;
    unsigned int mVBS;
    std::vector<SE_VertexFormat::Pos> posDataList;
    std::vector<SE_VertexFormat::PosTex0> postex0DataList;
    std::vector<SE_VertexFormat::PosTex0Tex1> postex0tex1DataList;
    std::vector<SE_VertexFormat::PosColor> poscolorDataList;
    int mVertexDataSize;
    int mDataStride;
    bool mVboHasInit;
    bool mDynamicDraw;
    
};
#endif
