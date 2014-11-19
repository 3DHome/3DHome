#ifndef __Particle_H__
#define __Particle_H__

#include "ParticleSystem/SE_ParticleColorValue.h"
#include "ParticleSystem/SE_ParticleMath.h"
#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticleSystem.h"

class ParticleSystem;

class SE_ENTRY ParticleVisualData
{
public:
    ParticleVisualData() {}
    virtual ~ParticleVisualData() {}

};

/** Class representing a single particle instance. */
class SE_ENTRY Particle 
{
protected:
    // Parent ParticleSystem
    ParticleSystem* mParentSystem;
    // Additional visual data you might want to associate with the Particle
    ParticleVisualData* mVisual;
public:
    // Type of particle
    enum ParticleType
    {
        Visual,
        Emitter
    };

    // Does this particle have it's own dimensions?
    bool mOwnDimensions;
    // Personal width if mOwnDimensions == true
    float mWidth;
    // Personal height if mOwnDimensions == true
    float mHeight;
    // Current rotation value   Radian
    Radian rotation;  
    // Note the intentional public access to internal variables
    // Accessing via get/set would be too costly for 000's of particles
    // World position
    Vector3 position;
    // Direction (and speed) 
    Vector3 direction;
    // Current colour
    ColourValue colour;
    // Time to live, number of seconds left of particles natural life
    float timeToLive;
    // Total Time to live, number of seconds of particles natural life
    float totalTimeToLive;
    // Speed of rotation in radians/sec
    Radian rotationSpeed;
    // Determines the type of particle.
    ParticleType particleType;

    Particle()
        : mParentSystem(0), mVisual(0), mOwnDimensions(false), rotation(0), 
        position(Vector3::ZERO), direction(Vector3::ZERO), 
        colour(ColourValue::White), timeToLive(10), totalTimeToLive(10), 
        rotationSpeed(0), particleType(Visual)
    {
    }

    /** Sets the width and height for this particle.
    @remarks
    Note that it is most efficient for every particle in a ParticleSystem to have the same dimensions. If you
    choose to alter the dimensions of an individual particle the set will be less efficient. Do not call
    this method unless you really need to have different particle dimensions within the same set. Otherwise
    just call the ParticleSystem::setDefaultDimensions method instead.
    */
    void setDimensions(float width, float height); 

    /** Returns true if this particle deviates from the ParticleSystem's default dimensions (i.e. if the
    particle::setDimensions method has been called for this instance).
    @see
    particle::setDimensions
    */
    bool hasOwnDimensions(void) const { return mOwnDimensions; }

    /** Retrieves the particle's personal width, if hasOwnDimensions is true. */
    float getOwnWidth(void) const { return mWidth; }

    /** Retrieves the particle's personal width, if hasOwnDimensions is true. */
    float getOwnHeight(void) const { return mHeight; }
        
    /** Sets the current rotation */
    void setRotation(const Radian& rad);

    const Radian& getRotation(void) const { return rotation; }

    /** Internal method for notifying the particle of it's owner.
    */
    void _notifyOwner(ParticleSystem* owner);

    /** Internal method for notifying the particle of it's optional visual data.
    */
    void _notifyVisualData(ParticleVisualData* vis) { mVisual = vis; }

    // Get the optional visual data associated with the class
    ParticleVisualData* getVisualData(void) const { return mVisual; }

    // Utility method to reset this particle
    void resetDimensions(void);
};

#endif
