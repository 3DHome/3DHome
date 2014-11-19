#include "SE_DynamicLibType.h"
#include "SE_Interpolate.h"
#include <math.h>
#include "SE_Common.h"
#include "SE_Time.h"
#include "SE_MemLeakDetector.h"
float SE_ConstInterpolate::calc(float input)
{
    return input;
}

SE_Interpolate* SE_InterpolateFactory::create(INTERPOLATE_TYPE type)
{
    if(type == CONST_INTERP)
{
        return new SE_ConstInterpolate();
    }
    else if(type == DECELERATE_INTERP)
    {
        return new SE_DecelerateInterpolate();
    }
    else if(type == ACCELERATE_INTERP)
    {
        return new SE_AccelerateInterpolate();
    }
    else if(type == ACCELERATEDECELERATE_INTERP)
    {
        return new SE_AccelerateDecelerateInterpolate();
    }
    else if(type == ANTICIPATE_INTERP)
    {
        return new SE_AnticipateInterpolate();
    }
    else if(type == BOUNCE_INTERP)
    {
        return new SE_BounceInterpolate();
    }
    else if(type == CYCLE_INTERP)
    {
        return new SE_CycleInterpolate();
    }
    else if(type == OVERSHOOT_INTERP)
    {
        return new SE_OvershootInterpolate();
    }
    else if(type == ANTICIPATEOVERSHOOT_INTERP)
    {
        return new SE_AnticipateOvershootInterpolate();
    }
    else if(type == VIBRATE_INTERP)
    {
        return new SE_VibrateInterpolate();
    }
    else if(type == DAMPEDVIBRATE_INTERP)
    {
        return new SE_DampedVibrateInterpolate();
    }
    else if(type == RANDOMDAMPEDVIBRATE_INTERP)
    {
        return new SE_RandomDampedVibrateInterpolate();
    }
    
    return NULL;
}

///////////////////////
SE_DecelerateInterpolate::SE_DecelerateInterpolate()
{
    mFactor = 1.0;

}
float SE_DecelerateInterpolate::calc(float input)
{
    float result = 0.0;
    if(mFactor == 1.0)
    {
        result = 1.0f - (1.0f - input) * (1.0f - input);
    }
    else
    {
        result = 1.0f - pow((1.0f - input), 2 * mFactor);
    }

    return result;
}
/////////////////////////////
SE_AccelerateInterpolate::SE_AccelerateInterpolate()
{
    mFactor = 1.0;
}

float SE_AccelerateInterpolate::calc(float input)
{
    float result = 0.0;
    if(mFactor == 1.0)
    {
        result = input * input;
    }
    else
    {
        result = pow(input,2 * mFactor);
    }

    return result;
}

float SE_AccelerateDecelerateInterpolate::calc(float input)
{
    float result = (float)(cos((input + 1) * SE_PI) / 2.0f) + 0.5f;

    return result;
}

float SE_AnticipateInterpolate::calc(float input)
{
    float result = input * input * ((mFactor + 1) * input - mFactor);

    return result;
}

float SE_BounceInterpolate::calc(float input)
{
    input *= 1.1226f;
    if (input < 0.3535f) 
    {
        return bounce(input);
    }
    else if (input < 0.7408f) 
    {
        return bounce(input - 0.54719f) + 0.7f;
    }
    else if (input < 0.9644f) 
    {
        return bounce(input - 0.8526f) + 0.9f;
    }
    return bounce(input - 1.0435f) + 0.95f;
}

float SE_CycleInterpolate::calc(float input)
{
    float result = (float)(sin(2 * mFactor * SE_PI * input));

    return result;
}

float SE_OvershootInterpolate::calc(float input)
{
    input -= 1.0f;
    float result = input * input * ((mFactor + 1) * input + mFactor) + 1.0f;

    return result;
}

float SE_AnticipateOvershootInterpolate::calc(float input)
{
    if (input < 0.5f)
    {
        return 0.5f * a(input * 2.0f, mFactor);
    }
     
    return 0.5f * (o(input * 2.0f - 2.0f, mFactor) + 2.0f);
}

float SE_VibrateInterpolate::calc(float input)
{
    float result = sin(input * SE_PI * mFactor);

    return result;
}

float SE_DampedVibrateInterpolate::calc(float input)
{
    float result = (1.0 - input) * sin(input * SE_PI * mFactor);

    return result;
}

void SE_RandomDampedVibrateInterpolate::init()
{
    srand(SE_Time::getCurrentTimeMS());

    mDir = (rand() % 11) / 10.0;
    mRadian = (float)(1.0 + rand() % 4) / 2;
}
float SE_RandomDampedVibrateInterpolate::calc(float input)
{
    float x = mDir >= 0.5 ? 1.0 : -1.0;

    
    float result = mRadian * x * (1.0 - input) * sin(input * SE_PI * mFactor);

    return result;
}
