#ifndef SE_MATERIALDATA_H
#define SE_MATERIALDATA_H
#include "SE_Vector.h"
class SE_ENTRY SE_MaterialData
{
public:
    SE_MaterialData()
    {
        ambient.set(0.5,0.5,0.5);
        diffuse.set(0.5,0.5,0.5);
        shiny = 0.8;
        shinessStrength = 0.5;

    }

    SE_Vector3f ambient;
    SE_Vector3f diffuse;
    SE_Vector3f specular;
    float shiny;
    float shinessStrength;
};
#endif
