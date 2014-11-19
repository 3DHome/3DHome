#ifndef SE_LIGHT_H
#define SE_LIGHT_H


class SE_ENTRY SE_Light
{
public:
    
    enum LightType
    {
        SE_POINT_LIGHT = 0,//no dir
        SE_DIRECTION_LIGHT = 1,//no pos
        SE_SPOT_LIGHT = 2
    };

    class LightPara
    {
    public:
        LightPara()
        {
            mSpotLightCutOff = 0.1;
            mSpotLightExp = 20.0;
            mLightColor.set(1.0,1.0,1.0);
            mRange = 10.0;
            mIntensity = 1.0;
            mIsImportantLight = false;

            //need replace
            mConstantAttenuation = 1.0;
            mLinearAttenuation = 0.0;
            mQuadraticAttenuation = 0.0;
            mCanMove = false;
            mDirLightStrength = 1.0;

        }

        SE_Vector3f mLightPos;
        SE_Vector3f mLightDir;
        SE_Vector3f mLightColor;
        float mSpotLightCutOff;
        float mSpotLightExp;

        //just for point light and spot light
        float mRange;
        bool mIsImportantLight;

        float mIntensity;
        std::string mLightName;
        bool mCanMove;
        LightType mLightType;

        //need replace
        float mConstantAttenuation;
        float mLinearAttenuation;
        float mQuadraticAttenuation;
        float mDirLightStrength;

    };
    SE_Light();
    virtual ~SE_Light();

    SE_Vector3f getLightPos();
    SE_Vector3f getLightDir();

    void setLightPos(SE_Vector3f pos);
    void setLightDir(SE_Vector3f dir);

    LightType getLightType();
    void setLightType(LightType type);

    bool lightCanMove();
    void setLightCanMove(bool can);

    void setAttenuation(float value = 0.5);
    float getAttenuation();

    void setLightName(const char* lightName);
    const char* getLightName();

    void updateLightPos(SE_Vector3f pos);
    void updateLightDir(SE_Vector3f dir);
    void updateLightCutOff(float cutoff);
    void updateLightExp(float exp);

    void setSpotLightCutOff(float cutoff);
    void setSpotLightExp(float exp);
    float getSpotLightCutOff();
    float getSpotLightExp();

    float getConstantAttenuation()
    {
        return mLightPara.mConstantAttenuation;
    }

    void setConstantAttenuation(float attr)
    {
        mLightPara.mConstantAttenuation = attr;
    }

    float getLinearAttenuation()
    {
        return mLightPara.mLinearAttenuation;
    }

    void setLinearAttenuation(float attr)
    {
        mLightPara.mLinearAttenuation = attr;
    }

    float getQuadraticAttenuation()
    {
        return mLightPara.mQuadraticAttenuation;
    }

    void setQuadraticAttenuation(float attr)
    {
        mLightPara.mQuadraticAttenuation = attr;
    }
    

    LightPara* getLightPara()
    {
        return &mLightPara;
    }

    void setLightColor(SE_Vector3f color)
    {
        mLightPara.mLightColor = color;
    }

    SE_Vector3f getLightColor()
    {
        return mLightPara.mLightColor;
    }

    void setDirLightStrength(float strength)
    {
        mLightPara.mDirLightStrength = strength;
    }

    float getDirLightStrength()
    {
        return mLightPara.mDirLightStrength;
    }

    void setLightRange(float r)
    {
        mLightPara.mRange = r;
    }
    float getLightRange()
    {
        return mLightPara.mRange;
    }

    void setLightIntensity(float r)
    {
        mLightPara.mIntensity = r;
    }
    float getLightIntensity()
    {
        return mLightPara.mIntensity;
    }

    void setLightIsImportant(bool is)
    {
        mLightPara.mIsImportantLight = is;
    }
    bool isImportantLight()
    {
        return mLightPara.mIsImportantLight;
    }
private:
    LightPara mLightPara;
    float mAttenuation; //not use now

};
#endif