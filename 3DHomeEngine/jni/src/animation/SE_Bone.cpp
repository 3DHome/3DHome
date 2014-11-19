#include "SE_DynamicLibType.h"
#include "SE_Bone.h"
#include "SE_MemLeakDetector.h"
SE_Bone::SE_Bone()
{
    mParent = NULL;
    mMatrixBase.identity();
}
SE_Bone::~SE_Bone()
{}
void SE_Bone::setMatrixArray(float* data, int num)
{
    if(data == NULL)
        return;
    mMatrixArray.resize(num);
    float* src = data;
    for(int i = 0 ; i < num ; i++)
    {
        float md[16];
        memcpy(md, src, sizeof(float) * 16);
        SE_Matrix4f m(md);
        mMatrixArray[i] = m;
        src += 16; 
    }
}
