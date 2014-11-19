#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleLinearForceAffector.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

LinearForceAffector::LinearForceAffector(ParticleSystem* psys)
    :ParticleAffector(psys)
{
    mType = "LinearForce";

    // Default to gravity-like
    mForceApplication = FA_ADD;
    mForceVector.x = mForceVector.z = 0;
    mForceVector.y = -100;
}

void LinearForceAffector::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;

    Vector3 scaledVector = Vector3::ZERO;

    // Precalc scaled force for optimisation
    if (mForceApplication == FA_ADD)
    {
        // Scale force by time
        scaledVector = mForceVector * timeElapsed;
    }

    while (!pi.end())
    {
        p = pi.getNext();
        if (mForceApplication == FA_ADD)
        {
            p->direction += scaledVector;
        }
        else // FA_AVERAGE
        {
            p->direction = (p->direction + mForceVector) / 2;
        }
    }      
}

void LinearForceAffector::setForceVector(const Vector3& force)
{
    mForceVector = force;
}

void LinearForceAffector::setForceApplication(ForceApplication fa)
{
    mForceApplication = fa;
}

Vector3 LinearForceAffector::getForceVector(void) const
{
    return mForceVector;
}

LinearForceAffector::ForceApplication LinearForceAffector::getForceApplication(void) const
{
    return mForceApplication;
}






