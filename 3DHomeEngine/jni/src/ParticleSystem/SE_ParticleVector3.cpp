#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticleMath.h"
#include "ParticleSystem/SE_ParticleQuaternion.h"
#include "SE_MemLeakDetector.h"

const Vector3 Vector3::ZERO( 0, 0, 0 );
const Vector3 Vector3::UNIT_X( 1, 0, 0 );
const Vector3 Vector3::UNIT_Y( 0, 1, 0 );
const Vector3 Vector3::UNIT_Z( 0, 0, 1 );
const Vector3 Vector3::NEGATIVE_UNIT_X( -1,  0,  0 );
const Vector3 Vector3::NEGATIVE_UNIT_Y(  0, -1,  0 );
const Vector3 Vector3::NEGATIVE_UNIT_Z(  0,  0, -1 );
const Vector3 Vector3::UNIT_SCALE(1, 1, 1);

Vector3 Vector3::randomDeviant(const Radian& angle, const Vector3& up) 
{
    Vector3 newUp;

    if (up == Vector3::ZERO)
    {
        // Generate an up vector
        newUp = this->perpendicular();
    }
    else
    {
        newUp = up;
    }

    // Rotate up vector by random amount around this
    Quaternion q;
    q.FromAngleAxis( Radian(Math::UnitRandom() * Math::TWO_PI), *this );
    newUp = q * newUp;

    // Finally rotate this by given angle around randomised up
    q.FromAngleAxis( angle, newUp );
    return q * (*this);
}
