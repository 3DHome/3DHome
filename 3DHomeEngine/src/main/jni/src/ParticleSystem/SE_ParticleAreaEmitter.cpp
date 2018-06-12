#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleAreaEmitter.h"
#include "SE_MemLeakDetector.h"

bool AreaEmitter::initDefaults(const String& t)
{
    // called by the constructor as initDefaults("Type")
    // Defaults
    mDirection = Vector3::UNIT_Z;
    mUp = Vector3::UNIT_Y;
    setSize(100,100,100);
    mType = t;

    return false;
}

unsigned short AreaEmitter::_getEmissionCount(Real timeElapsed)
{
    // Use basic constant emission 
    return genConstantEmissionCount(timeElapsed);
}

void AreaEmitter::setDirection( const Vector3& inDirection )
{
    ParticleEmitter::setDirection( inDirection );

    // Update the ranges
    genAreaAxes();
}

void AreaEmitter::setSize(const Vector3& size)
{
    mSize = size;
    genAreaAxes();
}

void AreaEmitter::setSize(Real x, Real y, Real z)
{
    mSize.x = x;
    mSize.y = y;
    mSize.z = z;
    genAreaAxes();
}

void AreaEmitter::setWidth(Real width)
{
    mSize.x = width;
    genAreaAxes();
}

Real AreaEmitter::getWidth(void) const
{
    return mSize.x;
}

void AreaEmitter::setHeight(Real height)
{
    mSize.y = height;
    genAreaAxes();
}

Real AreaEmitter::getHeight(void) const
{
    return mSize.y;
}

void AreaEmitter::setDepth(Real depth)
{
    mSize.z = depth;
    genAreaAxes();
}

Real AreaEmitter::getDepth(void) const
{
    return mSize.z;
}

void AreaEmitter::genAreaAxes(void)
{
    Vector3 mLeft = mUp.crossProduct(mDirection);

    mXRange = mLeft * (mSize.x * 0.5f);
    mYRange = mUp * (mSize.y * 0.5f);
    mZRange = mDirection * (mSize.z * 0.5f);
}


