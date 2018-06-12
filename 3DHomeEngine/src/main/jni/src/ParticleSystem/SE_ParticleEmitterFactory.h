#ifndef __ParticleEmitterFactory_H__
#define __ParticleEmitterFactory_H__

#include "ParticleSystem/SE_ParticleEmitter.h"
#include <vector>

/** Abstract class defining the interface to be implemented by creators of ParticleEmitter subclasses.
 @remarks
    Plugins or 3rd party applications can add new types of particle emitters to Ogre by creating
    subclasses of the ParticleEmitter class. Because multiple instances of these emitters may be
    required, a factory class to manage the instances is also required. 
@par
    ParticleEmitterFactory subclasses must allow the creation and destruction of ParticleEmitter
    subclasses. They must also be registered with the ParticleSystemManager. All factories have
    a name which identifies them, examples might be 'point', 'cone', or 'box', and these can be 
    also be used from particle system scripts.
*/
class SE_ENTRY ParticleEmitterFactory 
{
protected:
    std::vector<ParticleEmitter*> mEmitters;
public:
    ParticleEmitterFactory() {}
    virtual ~ParticleEmitterFactory();

    /** Returns the name of the factory, the name which identifies the particle emitter type this factory creates. */
    virtual String getName() const = 0;

    /** Creates a new emitter instance.
    @remarks
        The subclass MUST add a pointer to the created instance to mEmitters.
    */
    virtual ParticleEmitter* createEmitter(ParticleSystem* psys) = 0;

    /** Destroys the emitter pointed to by the parameter (for early clean up if required). */
    virtual void destroyEmitter(ParticleEmitter* e);
};

#endif

