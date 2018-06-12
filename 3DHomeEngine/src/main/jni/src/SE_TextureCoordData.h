#ifndef SE_TEXTURECOORDDATA_H
#define SE_TEXTURECOORDDATA_H
#include "SE_Vector.h"
class SE_ENTRY SE_TextureCoordData
{
public:
    SE_TextureCoordData()
    {
        texVertexArray = NULL;
        texVertexNum = 0;
        texFaceArray = NULL;
        texFaceNum = 0;
    }
    ~SE_TextureCoordData()
    {
        if(texVertexArray)
            delete[] texVertexArray;
        if(texFaceArray)
            delete[] texFaceArray;
    }
    SE_Vector2f* getTexVertexArray()
    {
        return texVertexArray;
    }
    int getTexVertexNum()
    {
        return texVertexNum;
    }
    SE_Vector3i* getTexFaceArray()
    {
        return texFaceArray;
    }
    int getTexFaceNum()
    {
        return texFaceNum;
    }
    void setTexVertexArray(SE_Vector2f* tva, int num)
    {
        if(texVertexArray)
            delete[] texVertexArray;
        texVertexArray = tva;
        texVertexNum = num;
    }
    void setTexFaceArray(SE_Vector3i* tfa, int num)
    {
        if(texFaceArray)
            delete[] texFaceArray;
        texFaceArray = tfa;
        texFaceNum = num;
    }
    /**************  add for particle    ***************/
    void setParticleTexVertexArray(SE_Vector2f* tva, int num)
    {
        texVertexArray = tva;
        texVertexNum = num;
    }
    /**************  add for particle    ***************/
    void setParticleTexFaceArray(SE_Vector3i* tfa, int num)
    {
        texFaceArray = tfa;
        texFaceNum = num;
    }
    
private:
    SE_TextureCoordData(const SE_TextureCoordData&);
    SE_TextureCoordData& operator=(const SE_TextureCoordData&);
private:
    SE_Vector2f* texVertexArray;
    int texVertexNum;
    SE_Vector3i* texFaceArray;
    int texFaceNum;
};
#endif
