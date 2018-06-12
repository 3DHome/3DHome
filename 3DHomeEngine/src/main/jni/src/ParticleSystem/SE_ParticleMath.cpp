#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleMath.h"
#include "SE_MemLeakDetector.h"

const Real Math::POS_INFINITY = std::numeric_limits<Real>::infinity();
const Real Math::NEG_INFINITY = -std::numeric_limits<Real>::infinity();
const Real Math::PI = Real( 4.0 * atan( 1.0 ) );
const Real Math::TWO_PI = Real( 2.0 * PI );
const Real Math::HALF_PI = Real( 0.5 * PI );
const Real Math::fDeg2Rad = PI / Real(180.0);
const Real Math::fRad2Deg = Real(180.0) / PI;
const Real Math::LOG2 = log(Real(2.0));

Math::Math( unsigned int trigTableSize )
{
}

Math::~Math()
{
}

Real Math::UnitRandom ()
{
    return rand() / float( RAND_MAX );
}

Real Math::RangeRandom (Real fLow, Real fHigh)
{
    return (fHigh-fLow)*UnitRandom() + fLow;
}

Real Math::SymmetricRandom ()
{
    return 2.0f * UnitRandom() - 1.0f;
}

Radian Math::ACos (Real fValue)
{
    if (-1.0 < fValue)
    {
        if (fValue < 1.0)
        {
            return Radian(acos(fValue));
        }
        else
        {
            return Radian(0.0);
        }
    }
    else
    {
        return Radian(PI);
    }
}

Radian Math::ASin (Real fValue)
{
    if (-1.0 < fValue)
    {
        if (fValue < 1.0) 
        {
            return Radian(asin(fValue));
        }
        else
        {
            return Radian(HALF_PI);
        }
    }
    else
    {
        return Radian(-HALF_PI);
    }
}

Real Math::InvSqrt(Real fValue)
{
    return Real(1. / sqrt(fValue));
}

bool Math::RealEqual( Real a, Real b, Real tolerance )
{
    if (fabs(b-a) <= tolerance)
    {
        return true;
    }
    else
    {
        return false;
    }
}


