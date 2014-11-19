#include "SE_DynamicLibType.h"
#include "SE_Factory.h"
#include "SE_Vector.h"
#include <string>
#include "SE_Light.h"
#include "SE_MemLeakDetector.h"

SE_Light::SE_Light()
{
    /*mCanMove = false;
    mAttenuation = 0.5;
    mSpotLightCutOff = 0.1;
    mSpotLightExp = 40.0;

    mConstantAttenuation = 1.0;
    mLinearAttenuation = 0.0;
    mQuadraticAttenuation = 0.0;*/
}

SE_Light::~SE_Light()
{}

SE_Vector3f SE_Light::getLightDir()
{
    return mLightPara.mLightDir;
}

SE_Vector3f SE_Light::getLightPos()
{
    return mLightPara.mLightPos;
}

void SE_Light::setLightDir(SE_Vector3f dir)
{
    mLightPara.mLightDir = dir;
}

void SE_Light::setLightPos(SE_Vector3f pos)
{
    mLightPara.mLightPos = pos;
}

void SE_Light::setLightType(SE_Light::LightType type)
{
    mLightPara.mLightType = type;
}

SE_Light::LightType SE_Light::getLightType()
{
    return mLightPara.mLightType;
}

void SE_Light::setAttenuation(float value)
{
    mAttenuation = value;
}

float SE_Light::getAttenuation()
{
    return mAttenuation;
}
bool SE_Light::lightCanMove()
{
    return mLightPara.mCanMove;
}

void SE_Light::setLightCanMove(bool can)
{
    mLightPara.mCanMove = can;
}
void SE_Light::setLightName(const char *lightName)
{
    mLightPara.mLightName = lightName;
}

const char* SE_Light::getLightName()
{
    return mLightPara.mLightName.c_str();
}

void SE_Light::updateLightPos(SE_Vector3f pos)
{
    setLightPos(pos);
}

void SE_Light::updateLightDir(SE_Vector3f dir)
{
    setLightDir(dir);
}

void SE_Light::setSpotLightCutOff(float cutoff)
{
    mLightPara.mSpotLightCutOff = cutoff;
}

void SE_Light::setSpotLightExp(float exp)
{
    mLightPara.mSpotLightExp = exp;
}

float SE_Light::getSpotLightCutOff()
{
    return mLightPara.mSpotLightCutOff;
}

float SE_Light::getSpotLightExp()
{
    return mLightPara.mSpotLightExp;
}

void SE_Light::updateLightCutOff(float cutoff)
{
    setSpotLightCutOff(cutoff);
}

void SE_Light::updateLightExp(float exp)
{
    setSpotLightExp(exp);
}