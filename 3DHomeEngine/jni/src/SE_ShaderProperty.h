#ifndef SE_SHADERCOLOROPERATION_H
#define SE_SHADERCOLOROPERATION_H
#include "SE_Common.h"
#include "SE_Vector.h"
#include <vector>
class SE_ENTRY SE_ShaderProperty
{
public:
    virtual ~SE_ShaderProperty() {}
};
class SE_ENTRY SE_ColorEffectShaderProperty : public SE_ShaderProperty
{
public:
    SE_ColorEffectShaderProperty()
    {
        for(int i = 0 ; i < 4 ; i++)
        {
            mHasTex[i] = 0;
            mMarkAlpha[i] = 1.0f;
            mMarkFunction[i] = 0;
            mMarkTexture[i] = -1;
            mBackgroundAlpha = 1.0f;
            mBackgroundTexture = -1;
            mChannelTexture = -1;
        }

    }
    void setHasTex(int index, bool v)
    {
        mHasTex[index] = v;
    }
    bool getHasTex(int index)
    {
        return mHasTex[index];
    }
    void setMarkAlpha(int index, float a)
    {
        mMarkAlpha[index] = a;
    }
    float getMarkAlphs(int index)
    {
        return mMarkAlpha[index];
    }
    void setBackgroundAlpha(float a)
    {
        mBackgroundAlpha = a;
    }
    float getBackgroudnAlpha()
    {
        return mBackgroundAlpha;
    }
    void setMarkFunction(int index, int fn)
    {
        mMarkFunction[index] = fn;
    }
    int getMarkFunction(int index)
    {
        return mMarkFunction[index];
    }
    void setMarkColor(int index, const SE_Vector3f& c)
    {
        mMarkColor[index] = c;
    }
    SE_Vector3f getMarkColor(int index)
    {
        return mMarkColor[index];
    }
    void setBackgroundTexture(int texIndex)
    {
        mBackgroundTexture = texIndex;
    }
    int getBackgroundTexture()
    {
        return mBackgroundTexture;
    }
    void setChannelTexture(int texIndex)
    {
        mChannelTexture = texIndex;
    }
    int getChannelTexture()
    {
        return mChannelTexture;
    }
    void setMarkTexture(int index, int texIndex)
    {
        mMarkTexture[index] = texIndex;
    }
    int getMarkTexture(int index)
    {
        return mMarkTexture[index];
    }
private:
    bool mHasTex[4];// r , g , b, a
    float mMarkAlpha[4];//rgba
    float mBackgroundAlpha;
    int mMarkFunction[4];//rgba
    SE_Vector3f mMarkColor[4];//rgba
    int mBackgroundTexture;
    int mChannelTexture;
    int mMarkTexture[4];//rgba
};
class SE_ENTRY SE_ColorExtractShaderProperty : public SE_ShaderProperty
{
public:
    SE_ColorExtractShaderProperty();
    int getColorOperationMode()
    {
        return mColorOp;
    }
    void setColorOperationMode(int op)
    {
        mColorOp = op;
    }
    void setColorChannelIndex(int index, int v)
    {
        mColorChannelIndex[index] = v;
    }
    int getColorChannelIndex(int index)
    {
        return mColorChannelIndex[index];
    }
    //void getColorOpMode(int* hasTexture, int num, int& outTexMode, int& outColorOp);
private:
    int mColorOp;
    int mColorChannelIndex[4]; // r,g, b,a
};
#endif
