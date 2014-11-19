#ifndef __PointEmitter_H__
#define __PointEmitter_H__

#include "ParticleSystem/SE_ParticleEmitter.h"
#include "ParticleSystem/SE_ParticleEmitterFactory.h"

/** Particle emitter which emits particles from a single point.
@remarks
    This basic particle emitter emits particles from a single point in space. The
    initial direction of these particles can either be a single direction (i.e. a line),
    a random scattering inside a cone, or a random scattering in all directions, 
    depending the 'angle' parameter, which is the angle across which to scatter the 
    particles either side of the base direction of the emitter. 
*/
class SE_ENTRY PointEmitter: public ParticleEmitter
{
public:
    PointEmitter(ParticleSystem* psys);

    /** See ParticleEmitter. */
    void _initParticle(Particle* pParticle);

    /** See ParticleEmitter. */
    unsigned short _getEmissionCount(Real timeElapsed);
};

class SE_ENTRY  PointEmitterFactory: public ParticleEmitterFactory
{
protected:

public:
    /** See ParticleEmitterFactory */
    String getName() const
    { 
        return "Point"; 
    }

    /** See ParticleEmitterFactory */
    ParticleEmitter* createEmitter(ParticleSystem* psys) 
    {
        ParticleEmitter* emit = new PointEmitter(psys);
        mEmitters.push_back(emit);
        return emit;
    }
};

#endif
