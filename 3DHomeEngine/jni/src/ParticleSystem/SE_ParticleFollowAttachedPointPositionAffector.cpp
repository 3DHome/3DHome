#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleFollowAttachedPointPositionAffector.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"
#include "SE_Log.h"

FollowAffector::FollowAffector(ParticleSystem* psys)
    :ParticleAffector(psys)
{
    mType = "Follower";
}

void FollowAffector::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;
    while (!pi.end())
    {
        p = pi.getNext();	    
        p->position = mPosition; 
    }
}

void FollowAffector::setCurrentPos(Vector3 pos)
{
    mPosition = pos;
}

Vector3 FollowAffector::getCurrentPos() const
{
    return mPosition;
}
