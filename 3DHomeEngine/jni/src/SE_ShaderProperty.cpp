#include "SE_DynamicLibType.h"
#include "SE_ShaderProperty.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
/*
std::vector<int> SE_ShaderColorOperation::textureModeProperty[SE_BLENDMODE_NUM];
bool SE_ShaderColorOperation::textureModeInited = false;
void SE_ShaderColorOperation::initTextureModeProperty()
{
        textureModeProperty[SE_TEXTURE0_MODE].resize(1);
        textureModeProperty[SE_TEXTURE0_MODE][0] = 0;

        textureModeProperty[SE_TEXTURE1_MODE].resize(1);
        textureModeProperty[SE_TEXTURE1_MODE][0] = 1;
        
        textureModeProperty[SE_TEXTURE2_MODE].resize(1);
        textureModeProperty[SE_TEXTURE2_MODE][0] = 2;
        
        textureModeProperty[SE_TEXTURE3_MODE].resize(1);
        textureModeProperty[SE_TEXTURE3_MODE][0] = 3;
        
        textureModeProperty[SE_COLOR_TEXTURE0_MODE].resize(1);
        textureModeProperty[SE_COLOR_TEXTURE0_MODE][0] = 0;
        
        textureModeProperty[SE_COLOR_TEXTURE1_MODE].resize(1);
        textureModeProperty[SE_COLOR_TEXTURE1_MODE][0] = 1;
        
        textureModeProperty[SE_COLOR_TEXTURE2_MODE].resize(1);
        textureModeProperty[SE_COLOR_TEXTURE2_MODE][0] = 2;
        
        textureModeProperty[SE_COLOR_TEXTURE3_MODE].resize(1);
        textureModeProperty[SE_COLOR_TEXTURE3_MODE][0] = 3;

        textureModeProperty[SE_TEXTURE0_TEXTURE1_MODE].resize(2);
        textureModeProperty[SE_TEXTURE0_TEXTURE1_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_MODE][1] = 1;

        textureModeProperty[SE_TEXTURE0_TEXTURE2_MODE].resize(2);
        textureModeProperty[SE_TEXTURE0_TEXTURE2_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE2_MODE][1] = 2;

        textureModeProperty[SE_TEXTURE0_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_TEXTURE0_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_TEXTURE1_TEXTURE2_MODE].resize(2);
        textureModeProperty[SE_TEXTURE1_TEXTURE2_MODE][0] = 1;
        textureModeProperty[SE_TEXTURE1_TEXTURE2_MODE][1] = 2;

        textureModeProperty[SE_TEXTURE1_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_TEXTURE1_TEXTURE3_MODE][0] = 1;
        textureModeProperty[SE_TEXTURE1_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_TEXTURE2_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_TEXTURE2_TEXTURE3_MODE][0] = 2;
        textureModeProperty[SE_TEXTURE2_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_MODE][1] = 1;

        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_MODE][1] = 2;

        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_MODE][0] = 1;
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_MODE][1] = 2;

        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE3_MODE][0] = 1;
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_COLOR_TEXTURE2_TEXTURE3_MODE].resize(2);
        textureModeProperty[SE_COLOR_TEXTURE2_TEXTURE3_MODE][0] = 2;
        textureModeProperty[SE_COLOR_TEXTURE2_TEXTURE3_MODE][1] = 3;

        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_MODE].resize(3);
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_MODE][1] = 1;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_MODE][2] = 2;

        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE3_MODE][1] = 1;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE3_MODE][2] = 3;

        textureModeProperty[SE_TEXTURE0_TEXTURE2_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_TEXTURE0_TEXTURE2_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE2_TEXTURE3_MODE][1] = 2;
        textureModeProperty[SE_TEXTURE0_TEXTURE2_TEXTURE3_MODE][2] = 3;

        textureModeProperty[SE_TEXTURE1_TEXTURE2_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_TEXTURE1_TEXTURE2_TEXTURE3_MODE][0] = 1;
        textureModeProperty[SE_TEXTURE1_TEXTURE2_TEXTURE3_MODE][1] = 2;
        textureModeProperty[SE_TEXTURE1_TEXTURE2_TEXTURE3_MODE][2] = 3;


        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_MODE].resize(3);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_MODE][1] = 1;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_MODE][2] = 2;

        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE3_MODE][1] = 1;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE3_MODE][2] = 3;

        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_TEXTURE3_MODE][1] = 2;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE2_TEXTURE3_MODE][2] = 3;

        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_TEXTURE3_MODE].resize(3);
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_TEXTURE3_MODE][0] = 1;
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_TEXTURE3_MODE][1] = 2;
        textureModeProperty[SE_COLOR_TEXTURE1_TEXTURE2_TEXTURE3_MODE][2] = 3;

        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE].resize(4);
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][1] = 1;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][2] = 2;
        textureModeProperty[SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][3] = 3;
        
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE].resize(4);
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][0] = 0;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][1] = 1;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][2] = 2;
        textureModeProperty[SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE][3] = 3;

}
*/
SE_ColorExtractShaderProperty::SE_ColorExtractShaderProperty()
{
    //if(!textureModeInited)
    //{
     //   textureModeInited = true;
      //  initTextureModeProperty();
    //}
    mColorOp = 0;
    for(int i = 0 ; i < 4 ; i++)
    {
        mColorChannelIndex[i] = -1;
    }
    //mTextureMode = SE_TEXTURE0;
}
/*
void SE_ShaderColorOperation::getColorOpMode(int* hasTexture, int num, int& outColorOp)
{
    if(num != SE_TEXUNIT_NUM)
    {
        outColorOp = 0;
        return;
    }
    bool textureAllFound = true;
    int index = 0;
    std::vector<int> textureIndexVector = textureModeProperty[mTextureMode];
    for(int i = 0 ; i < textureIndexVector.size() ; i++)
    {
        if(!hasTexture[textureIndexVector[i]])
        {
            textureAllFound = false;
            index = textureIndexVector[i];
            break;
        }
    }
    if(textureAllFound)
    {
        outTexMode = mTextureMode;
        outColorOp = mColorOp;
    }
    else
    {
        LOGI("... error: texture %d has no image\n", index);
        outTexMode = SE_COLOR_MODE;
        outColorOp = 0;
    }
}
*/