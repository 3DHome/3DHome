#ifndef __ParticleAffector_H__
#define __ParticleAffector_H__

#include <string>
typedef  std::string String;
class ParticleSystem;
class Particle;

/** Abstract class defining the interface to be implemented by particle affectors.
    @remarks
        Particle affectors modify particles in a particle system over their lifetime. They can be
        grouped into types, e.g. 'vector force' affectors, 'fader' affectors etc; each type will 
        modify particles in a different way, using different parameters.
    @par
        Because there are so many types of affectors you could use, OGRE chooses not to dictate
        the available types. It comes with some in-built, but allows plugins or applications to extend the affector types available.
        This is done by subclassing ParticleAffector to have the appropriate emission behaviour you want,
        and also creating a subclass of ParticleAffectorFactory which is responsible for creating instances 
        of your new affector type. You register this factory with the ParticleSystemManager using
        addAffectorFactory, and from then on affectors of this type can be created either from code or through
        text particle scripts by naming the type.
    @par
        This same approach is used for ParticleEmitters (which are the source of particles in a system).
        This means that OGRE is particularly flexible when it comes to creating particle system effects,
        with literally infinite combinations of affector and affector types, and parameters within those
        types.
*/
class  SE_ENTRY ParticleAffector 
{
protected:
    // Name of the type of affector, MUST be initialised by subclasses
    String mType;

    /** Internal method for setting up the basic parameter definitions for a subclass. 
    @remarks
        Because StringInterface holds a dictionary of parameters per class, subclasses need to
        call this to ask the base class to add it's parameters to their dictionary as well.
        Can't do this in the constructor because that runs in a non-virtual context.
    @par
        The subclass must have called it's own createParamDictionary before calling this method.
    */
    void addBaseParameters(void) { /* actually do nothing - for future possible use */ }

    ParticleSystem* mParent;
public:
    ParticleAffector(ParticleSystem* parent): mParent(parent) {}

    /** Virtual destructor essential. */
    virtual ~ParticleAffector();

    /** Method called to allow the affector to initialize all newly created particles in the system.
    @remarks
        This is where the affector gets the chance to initialize it's effects to the particles of a system.
        The affector is expected to initialize some or all of the particles in the system
        passed to it, depending on the affector's approach.
    @param
        pParticle Pointer to a Particle to initialize.
    */
    virtual void _initParticle(Particle* pParticle)
    {
        /* by default do nothing */
        (void)pParticle;
    }

    /** Method called to allow the affector to 'do it's stuff' on all active particles in the system.
    @remarks
        This is where the affector gets the chance to apply it's effects to the particles of a system.
        The affector is expected to apply it's effect to some or all of the particles in the system
        passed to it, depending on the affector's approach.
    @param
        pSystem Pointer to a ParticleSystem to affect.
    @param
        timeElapsed The number of seconds which have elapsed since the last call.
    */
    virtual void _affectParticles(ParticleSystem* pSystem, float timeElapsed) = 0;

    /** Returns the name of the type of affector. 
    @remarks
        This property is useful for determining the type of affector procedurally so another
        can be created.
    */
    const String &getType(void) const { return mType; }

};

#endif

