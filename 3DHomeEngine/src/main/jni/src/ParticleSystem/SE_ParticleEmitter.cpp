#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleEmitter.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_ParticleEmitterFactory.h"
#include "SE_MemLeakDetector.h"
#include <vector>

ParticleEmitter::ParticleEmitter(ParticleSystem* psys)
    : mParent(psys),
    mStartTime(0),
    mDurationMin(0),
    mDurationMax(0),
    mDurationRemain(0),
    mRepeatDelayMin(0),
    mRepeatDelayMax(0),
    mRepeatDelayRemain(0)
{
    // Reasonable defaults
    mAngle = 0;
    setDirection(Vector3::UNIT_X);
    mEmissionRate = 10;
    mMaxSpeed = mMinSpeed = 1;
    mMaxTTL = mMinTTL = 5;
    mPosition = Vector3::ZERO;
    mColourRangeStart = mColourRangeEnd = ColourValue::White;
    mEnabled = true;
    mRemainder = 0;
    mName = "";
    mEmittedEmitter = "";
    mEmitted = false;
}

ParticleEmitter::~ParticleEmitter() 
{
}

void ParticleEmitter::setPosition(const Vector3& pos) 
{ 
    mPosition = pos; 
}

const Vector3& ParticleEmitter::getPosition(void) const 
{ 
    return mPosition; 
}

void ParticleEmitter::setDirection(const Vector3& inDirection) 
{ 
    mDirection = inDirection; 
    mDirection.normalise();
    // Generate an up vector (any will do)
    mUp = mDirection.perpendicular();
    mUp.normalise();
}

const Vector3& ParticleEmitter::getDirection(void) const
{ 
    return mDirection; 
}

void ParticleEmitter::setAngle(const Radian& angle)
{
    // Store as radians for efficiency
    mAngle = angle;
}

const Radian& ParticleEmitter::getAngle(void) const
{
    return mAngle;
}

void ParticleEmitter::setParticleVelocity(Real speed)
{
    mMinSpeed = mMaxSpeed = speed;
}

void ParticleEmitter::setParticleVelocity(Real min, Real max)
{
    mMinSpeed = min;
    mMaxSpeed = max;
}

void ParticleEmitter::setEmissionRate(Real particlesPerSecond) 
{ 
    mEmissionRate = particlesPerSecond; 
}

Real ParticleEmitter::getEmissionRate(void) const 
{ 
    return mEmissionRate; 
}

void ParticleEmitter::setTimeToLive(Real ttl)
{
    mMinTTL = mMaxTTL = ttl;
}

void ParticleEmitter::setTimeToLive(Real minTtl, Real maxTtl)
{
    mMinTTL = minTtl;
    mMaxTTL = maxTtl;
}

void ParticleEmitter::setColour(const ColourValue& inColour)
{
    mColourRangeStart = mColourRangeEnd = inColour;
}

void ParticleEmitter::setColour(const ColourValue& colourStart, const ColourValue& colourEnd)
{
    mColourRangeStart = colourStart;
    mColourRangeEnd = colourEnd;
}

const String& ParticleEmitter::getName(void) const
{
    return mName;
}

void ParticleEmitter::setName(const String& newName)
{
    mName = newName;
}

const String& ParticleEmitter::getEmittedEmitter(void) const
{
    return mEmittedEmitter;
}

void ParticleEmitter::setEmittedEmitter(const String& emittedEmitter)
{
    mEmittedEmitter = emittedEmitter;
}

bool ParticleEmitter::isEmitted(void) const
{
    return mEmitted;
}

void ParticleEmitter::setEmitted(bool emitted)
{
    mEmitted = emitted;
}

void ParticleEmitter::genEmissionDirection(Vector3& destVector)
{
    if (mAngle != Radian(0))
    {
        // Randomise angle
        Radian angle = Math::UnitRandom() * mAngle;

        // Randomise direction
        destVector = mDirection.randomDeviant(angle, mUp);
    }
    else
    {
        // Constant angle
        destVector = mDirection;
    }

    // Don't normalise, we can assume that it will still be a unit vector since
    // both direction and 'up' are.
}

void ParticleEmitter::genEmissionVelocity(Vector3& destVector)
{
    Real scalar;
    if (mMinSpeed != mMaxSpeed)
    {
        scalar = mMinSpeed + (Math::UnitRandom() * (mMaxSpeed - mMinSpeed));
    }
    else
    {
        scalar = mMinSpeed;
    }

    destVector *= scalar;
}

Real ParticleEmitter::genEmissionTTL(void)
{
    if (mMaxTTL != mMinTTL)
    {
        return mMinTTL + (Math::UnitRandom() * (mMaxTTL - mMinTTL));
    }
    else
    {
        return mMinTTL;
    }
}

unsigned short ParticleEmitter::genConstantEmissionCount(Real timeElapsed)
{
    unsigned short intRequest;
        
    if (mEnabled)
    {
        // Keep fractions, otherwise a high frame rate will result in zero emissions!
        mRemainder += mEmissionRate * timeElapsed;
        intRequest = (unsigned short)mRemainder;
        mRemainder -= intRequest;

        // Check duration
        if (mDurationMax)
        {
            mDurationRemain -= timeElapsed;
            if (mDurationRemain <= 0) 
            {
                // Disable, duration is out (takes effect next time)
                setEnabled(false);
            }
        }
        return intRequest;
    }
    else
    {
        // Check repeat
        if (mRepeatDelayMax)
        {
            mRepeatDelayRemain -= timeElapsed;
            if (mRepeatDelayRemain <= 0)
            {
                // Enable, repeat delay is out (takes effect next time)
                setEnabled(true);
            }
        }
        if (mStartTime)
        {
            mStartTime -= timeElapsed;
            if (mStartTime <= 0)
            {
                setEnabled(true);
                mStartTime = 0;
            }
        }
        return 0;
    }
}

void ParticleEmitter::genEmissionColour(ColourValue& destColour)
{
    if (mColourRangeStart != mColourRangeEnd)
    {
        // Randomise
        //Real t = Math::UnitRandom();
        destColour.r = mColourRangeStart.r + (Math::UnitRandom() * (mColourRangeEnd.r - mColourRangeStart.r));
        destColour.g = mColourRangeStart.g + (Math::UnitRandom() * (mColourRangeEnd.g - mColourRangeStart.g));
        destColour.b = mColourRangeStart.b + (Math::UnitRandom() * (mColourRangeEnd.b - mColourRangeStart.b));
        destColour.a = mColourRangeStart.a + (Math::UnitRandom() * (mColourRangeEnd.a - mColourRangeStart.a));
    }
    else
    {
        destColour = mColourRangeStart;
    }
}
   
Real ParticleEmitter::getParticleVelocity(void) const
{
    return mMinSpeed;
}

Real ParticleEmitter::getMinParticleVelocity(void) const
{
    return mMinSpeed;
}
 
Real ParticleEmitter::getMaxParticleVelocity(void) const
{
    return mMaxSpeed;
}

void ParticleEmitter::setMinParticleVelocity(Real min)
{
    mMinSpeed = min;
}
 
void ParticleEmitter::setMaxParticleVelocity(Real max)
{
    mMaxSpeed = max;
}

Real ParticleEmitter::getTimeToLive(void) const
{
    return mMinTTL;
}

Real ParticleEmitter::getMinTimeToLive(void) const
{
    return mMinTTL;
}

Real ParticleEmitter::getMaxTimeToLive(void) const
{
    return mMaxTTL;
}

void ParticleEmitter::setMinTimeToLive(Real min)
{
    mMinTTL = min;
}

void ParticleEmitter::setMaxTimeToLive(Real max)
{
    mMaxTTL = max;
}

const ColourValue& ParticleEmitter::getColour(void) const
{
    return mColourRangeStart;
}

const ColourValue& ParticleEmitter::getColourRangeStart(void) const
{
    return mColourRangeStart;
}

const ColourValue& ParticleEmitter::getColourRangeEnd(void) const
{
    return mColourRangeEnd;
}
  
void ParticleEmitter::setColourRangeStart(const ColourValue& val)
{
    mColourRangeStart = val;
}

void ParticleEmitter::setColourRangeEnd(const ColourValue& val )
{
    mColourRangeEnd = val;
}

void ParticleEmitter::setEnabled(bool enabled)
{
    mEnabled = enabled;
    // Reset duration & repeat
    initDurationRepeat();
}

bool ParticleEmitter::getEnabled(void) const
{
    return mEnabled;
}

void ParticleEmitter::setStartTime(Real startTime)
{
    setEnabled(false);
    mStartTime = startTime;
}

Real ParticleEmitter::getStartTime(void) const
{
    return mStartTime;
}

void ParticleEmitter::setDuration(Real duration)
{
    setDuration(duration, duration);
}

Real ParticleEmitter::getDuration(void) const
{
    return mDurationMin;
}

void ParticleEmitter::setDuration(Real min, Real max)
{
    mDurationMin = min;
    mDurationMax = max;
    initDurationRepeat();
}

void ParticleEmitter::setMinDuration(Real min)
{
    mDurationMin = min;
    initDurationRepeat();
}
 
void ParticleEmitter::setMaxDuration(Real max)
{
    mDurationMax = max;
    initDurationRepeat();
}

void ParticleEmitter::initDurationRepeat(void)
{
    if (mEnabled)
    {
        if (mDurationMin == mDurationMax)
        {
            mDurationRemain = mDurationMin;
        }
        else
        {
            mDurationRemain = Math::RangeRandom(mDurationMin, mDurationMax);
        }
    }
    else
    {
        // Reset repeat
        if (mRepeatDelayMin == mRepeatDelayMax)
        {
            mRepeatDelayRemain = mRepeatDelayMin;
        }
        else
        {
            mRepeatDelayRemain = Math::RangeRandom(mRepeatDelayMax, mRepeatDelayMin);
        }
    }
}

void ParticleEmitter::setRepeatDelay(Real delay)
{
    setRepeatDelay(delay, delay);
}

Real ParticleEmitter::getRepeatDelay(void) const
{
    return mRepeatDelayMin;
}

void ParticleEmitter::setRepeatDelay(Real min, Real max)
{
    mRepeatDelayMin = min;
    mRepeatDelayMax = max;
    initDurationRepeat();
}

void ParticleEmitter::setMinRepeatDelay(Real min)
{
    mRepeatDelayMin = min;
    initDurationRepeat();
}

void ParticleEmitter::setMaxRepeatDelay(Real max)
{
    mRepeatDelayMax = max;
    initDurationRepeat();
}

Real ParticleEmitter::getMinDuration(void) const
{
    return mDurationMin;
}

Real ParticleEmitter::getMaxDuration(void) const
{
    return mDurationMax;
}

Real ParticleEmitter::getMinRepeatDelay(void) const
{
    return mRepeatDelayMin;    
}

Real ParticleEmitter::getMaxRepeatDelay(void) const
{
    return mRepeatDelayMax;    
}

ParticleEmitterFactory::~ParticleEmitterFactory()
{
    // Destroy all emitters
    std::vector<ParticleEmitter*>::iterator i;
    for (i = mEmitters.begin(); i != mEmitters.end(); ++i)
    {
         delete (*i);
    }
            
    mEmitters.clear();
}

void ParticleEmitterFactory::destroyEmitter(ParticleEmitter* e)        
{
    std::vector<ParticleEmitter*>::iterator i;
    for (i = mEmitters.begin(); i != mEmitters.end(); ++i)
    {
        if ((*i) == e)
        {
            mEmitters.erase(i);
            delete e;
            break;
        }
    }
}

void ParticleEmitter::copyParametersTo(ParticleEmitter* e) 
{		
    e->setPosition(mPosition);
    e->setDirection(mDirection);
    e->setAngle(mAngle);
    e->setParticleVelocity(mMinSpeed,mMaxSpeed);
    e->setEmissionRate(mEmissionRate);
    e->setTimeToLive(mMinTTL,mMaxTTL);
    e->setColour(mColourRangeStart,mColourRangeEnd);
    e->setName(mName);
    e->setEmittedEmitter(mEmittedEmitter);
    e->setDuration(mDurationMin,mDurationMax);
    e->setRepeatDelay(mRepeatDelayMin,mRepeatDelayMax);
    initDurationRepeat();
}
