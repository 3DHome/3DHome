#ifndef SE_INTERPOLATE_H
#define SE_INTERPOLATE_H
#include "SE_Common.h"
class SE_Interpolate
{
public:
    SE_Interpolate()
    {
        mFactor = 1.0;
    }
    virtual ~SE_Interpolate() {}
    virtual float calc(float input){return input;}
    virtual SE_Interpolate* clone(){return NULL;}

    virtual void init(){}
    virtual void setFactor(float factor)
    {
        mFactor = factor;
    }

protected:
    float mFactor;
};

class SE_ConstInterpolate : public SE_Interpolate
{
public:
    virtual float calc(float input);
};

class SE_DecelerateInterpolate : public SE_Interpolate
{
public:

    SE_DecelerateInterpolate();
    virtual float calc(float input);

};

class SE_AccelerateInterpolate : public SE_Interpolate
{
public:

    SE_AccelerateInterpolate();
    virtual float calc(float input);
    
};

/**
 * An interpolator where the rate of change starts and ends slowly but
 * accelerates through the middle.
 * 
 */
class SE_AccelerateDecelerateInterpolate : public SE_Interpolate
{
public:

    SE_AccelerateDecelerateInterpolate(){}
    virtual float calc(float input);    
};


/**
 * An interpolator where the change starts backward then flings forward.
 */
class SE_AnticipateInterpolate : public SE_Interpolate
{
public:

    /**
     * @param tension Amount of anticipation. When tension equals 0.0f, there is
     *                no anticipation and the interpolator becomes a simple
     *                acceleration interpolator.
     */
    SE_AnticipateInterpolate()
    {
        mFactor = 2.0;
    }
    virtual float calc(float input);    
};


/**
 * An interpolator where the change bounces at the end.
 */
class SE_BounceInterpolate : public SE_Interpolate
{
public:

    SE_BounceInterpolate(){}
    virtual float calc(float input);   

private:
    float bounce(float t)
    {
        return t * t * 8.0;
    }
};

class SE_CycleInterpolate : public SE_Interpolate
{
public:

    SE_CycleInterpolate(){}
    virtual float calc(float input);
    
};

/**
 * An interpolator where the change flings forward and overshoots the last value
 * then comes back.
 */
class SE_OvershootInterpolate : public SE_Interpolate
{
public:

    /**
     * @param tension Amount of overshoot. When tension equals 0.0f, there is
     *                no overshoot and the interpolator becomes a simple
     *                deceleration interpolator.
     */
    SE_OvershootInterpolate()
    {
        mFactor = 2.0;
    }
    virtual float calc(float input);
    
};

/**
 * An interpolator where the change starts backward then flings forward and overshoots
 * the target value and finally goes back to the final value.
 */
class SE_AnticipateOvershootInterpolate : public SE_Interpolate
{
public:

    /**
     * @param tension Amount of anticipation/overshoot. When tension equals 0.0f,
     *                there is no anticipation/overshoot and the interpolator becomes
     *                a simple acceleration/deceleration interpolator.
     */
    SE_AnticipateOvershootInterpolate()
    {
        mFactor = 2.0 * 1.5;
    }
    virtual float calc(float input);
    virtual void setFactor(float input)
    {
        mFactor = input * 1.5;
    }
    virtual void setFactor(float factor,float extraFactor)
    {
        mFactor = factor * extraFactor;
    }

private:
    float a(float t,float s)
    {
        return t * t * ((s + 1) * t - s);
    }

    float o(float t, float s)
    {
        return t * t * ((s + 1) * t + s);
    }

};

class SE_VibrateInterpolate : public SE_Interpolate
{
public:

    SE_VibrateInterpolate(){}
    virtual float calc(float input);
    
};

class SE_DampedVibrateInterpolate : public SE_Interpolate
{
public:
    SE_DampedVibrateInterpolate(){}
    virtual float calc(float input);
    
};

class SE_RandomDampedVibrateInterpolate : public SE_Interpolate
{
public:
    SE_RandomDampedVibrateInterpolate()
    {
        mDir = 1.0;
        mRadian = 0.0;
    }
    virtual void init();
    virtual float calc(float input);
private:
    float mDir;
    float mRadian;
    
};

class SE_InterpolateFactory
{
public:
    static SE_Interpolate* create(INTERPOLATE_TYPE type);
};
#endif
