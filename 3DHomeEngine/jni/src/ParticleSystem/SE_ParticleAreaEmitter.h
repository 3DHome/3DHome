#ifndef __AreaEmitter_H__
#define __AreaEmitter_H__

#include "ParticleSystem/SE_ParticleEmitter.h"

/** Particle emitter which emits particles randomly from points inside
    an area (box, sphere, ellipsoid whatever subclasses choose to be).
@remarks
    This is an empty superclass and needs to be subclassed. Basic particle
    emitter emits particles from/in an (unspecified) area. The
    initial direction of these particles can either be a single direction
    (i.e. a line), a random scattering inside a cone, or a random
    scattering in all directions, depending the 'angle' parameter, which
    is the angle across which to scatter the particles either side of the
    base direction of the emitter. 
*/
class SE_ENTRY  AreaEmitter: public ParticleEmitter
{
public:
    AreaEmitter(ParticleSystem* psys) : ParticleEmitter(psys) {}

    /** See ParticleEmitter. */
    unsigned short _getEmissionCount(Real timeElapsed);

    /** Overloaded to update the trans. matrix */
    void setDirection( const Vector3& direction );

    /** Sets the size of the area from which particles are emitted.
    @param
        size Vector describing the size of the area. The area extends
        around the center point by half the x, y and z components of
        this vector. The box is aligned such that it's local Z axis points
        along it's direction (see setDirection)
    */
    void setSize(const Vector3& size);

    /** Sets the size of the area from which particles are emitted.
    @param x,y,z
        Individual axis lengths describing the size of the area. The area
        extends around the center point by half the x, y and z components
        of this vector. The box is aligned such that it's local Z axis
        points along it's direction (see setDirection)
    */
    void setSize(Real x, Real y, Real z);
    /** Sets the size of the clear space inside the area from where NO particles are emitted.
    @param x,y,z
        Individual axis lengths describing the size of the clear space.
        The clear space is aligned like the outer area.
        (see setDirection and setSize)
    */
    void setClearSpace(Real x, Real y, Real z);

    /** Sets the width (local x size) of the emitter. */
    void setWidth(Real width);
    /** Gets the width (local x size) of the emitter. */
    Real getWidth(void) const;
    /** Sets the height (local y size) of the emitter. */
    void setHeight(Real Height);
    /** Gets the height (local y size) of the emitter. */
    Real getHeight(void) const;
    /** Sets the depth (local y size) of the emitter. */
    void setDepth(Real Depth);
    /** Gets the depth (local y size) of the emitter. */
    Real getDepth(void) const;

protected:
    // Size of the area
    Vector3 mSize;

    // Local axes, not normalised, their magnitude reflects area size
    Vector3 mXRange, mYRange, mZRange;

    // Internal method for generating the area axes
    void genAreaAxes(void);
    /** Internal for initializing some defaults and parameters
    @returns True if custom parameters need initialising
    */
    bool initDefaults(const String& mType);
};

#endif

