
#ifndef __FollowAffector_H__
#define __FollowAffector_H__

#include "ParticleSystem/SE_ParticleAffector.h"
#include "ParticleSystem/SE_ParticleMath.h"
#include "ParticleSystem/SE_ParticleAffectorFactory.h"
#include "ParticleSystem/SE_ParticleVector3.h"

//this affector can modify particle position with attached spatial
class SE_ENTRY  FollowAffector: public ParticleAffector
{
public:
    /** Default constructor. */
    FollowAffector(ParticleSystem* psys);

    /** See ParticleAffector. */
    void _affectParticles(ParticleSystem* pSystem, Real timeElapsed);

    //set current position
    void setCurrentPos(Vector3 pos );

    /** Gets the scale adjustment to be made per second to particles. */
    Vector3 getCurrentPos(void) const;

protected:
    Vector3 mPosition;
};

/** Factory class for FollowAffector. */
class SE_ENTRY  FollowAffectorFactory: public ParticleAffectorFactory
{
    /** See ParticleAffectorFactory */
    String getName() const { return "Follower"; }

    /** See ParticleAffectorFactory */
    ParticleAffector* createAffector(ParticleSystem* psys)
    {
        ParticleAffector* p = new FollowAffector(psys);
        mAffectors.push_back(p);
        return p;
    }
};

#endif

