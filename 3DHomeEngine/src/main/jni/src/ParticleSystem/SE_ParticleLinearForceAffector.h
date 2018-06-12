#ifndef __LinearForceAffector_H__
#define __LinearForceAffector_H__

#include "ParticleSystem/SE_ParticleAffector.h"
#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticleAffectorFactory.h"

/** This class defines a ParticleAffector which applies a linear force to particles in a system.
@remarks
    This affector (see ParticleAffector) applies a linear force, such as gravity, to a particle system.
    This force can be applied in 2 ways: by taking the average of the particle's current momentum and the 
    force vector, or by adding the force vector to the current particle's momentum. 
@par
    The former approach is self-stabilising i.e. once a particle's momentum
    is equal to the force vector, no further change is made to it's momentum. It also results in
    a non-linear acceleration of particles.
    The latter approach is simpler and applies a constant acceleration to particles. However,
    it is not self-stabilising and can lead to perpetually increasing particle velocities. 
    You choose the approach by calling the setForceApplication method.
*/
class SE_ENTRY  LinearForceAffector: public ParticleAffector
{
public:

    // Choice of how to apply the force vector to particles
    enum ForceApplication
    {
        // Take the average of the force vector and the particle momentum
        FA_AVERAGE,
        // Add the force vector to the particle momentum
        FA_ADD
    };
    // Default constructor
    LinearForceAffector(ParticleSystem* psys);

    /** See ParticleAffector. */
    void _affectParticles(ParticleSystem* pSystem, Real timeElapsed);

    /** Sets the force vector to apply to the particles in a system. */
    void setForceVector(const Vector3& force);

    /** Gets the force vector to apply to the particles in a system. */
    Vector3 getForceVector(void) const;

    /** Sets how the force vector is applied to a particle. 
    @remarks
        The default is FA_ADD.
    @param fa A member of the ForceApplication enum.
    */
    void setForceApplication(ForceApplication fa);

    /** Retrieves how the force vector is applied to a particle. 
    @param fa A member of the ForceApplication enum.
    */
    ForceApplication getForceApplication(void) const;

protected:
        /// Force vector
        Vector3 mForceVector;

        /// How to apply force
        ForceApplication mForceApplication;
};

/** Factory class for LinearForceAffector. */
class SE_ENTRY  LinearForceAffectorFactory: public ParticleAffectorFactory
{
    /** See ParticleAffectorFactory */
    String getName() const { return "LinearForce"; }

    /** See ParticleAffectorFactory */
    ParticleAffector* createAffector(ParticleSystem* psys)
    {
        ParticleAffector* p = new LinearForceAffector(psys);
        mAffectors.push_back(p);
        return p;
    }
};

#endif

