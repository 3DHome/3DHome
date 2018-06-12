/*
-----------------------------------------------------------------------------
This source file is part of OGRE
    (Object-oriented Graphics Rendering Engine)
For the latest info, see http://www.ogre3d.org/

Copyright (c) 2000-2009 Torus Knot Software Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-----------------------------------------------------------------------------
*/
#ifndef __DirectionRandomiserAffector_H__
#define __DirectionRandomiserAffector_H__

#include "ParticleSystem/SE_ParticleAffector.h"
#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticleAffectorFactory.h"

/** This class defines a ParticleAffector which applies randomness to the movement of the particles.
@remarks
    This affector (see ParticleAffector) applies randomness to the movement of the particles by
    changing the direction vectors.
@par
    The most important parameter to control the effect is randomness. It controls the range in which changes
    are applied to each axis of the direction vector.
    The parameter scope can be used to limit the effect to a certain percentage of the particles.
*/
class SE_ENTRY  DirectionRandomiserAffector: public ParticleAffector
{
public:
    // Default constructor
    DirectionRandomiserAffector(ParticleSystem* psys);

    /** See ParticleAffector. */
    void _affectParticles(ParticleSystem* pSystem, Real timeElapsed);

    /** Sets the randomness to apply to the particles in a system. */
    void setRandomness(Real force);
    /** Sets the scope (percentage of particles which are randomised). */
    void setScope(Real force);
    /** Set flag which detemines whether particle speed is changed. */
    void setKeepVelocity(bool keepVelocity);

    /** Gets the randomness to apply to the particles in a system. */
    Real getRandomness(void) const;
    /** Gets the scope (percentage of particles which are randomised). */
    Real getScope(void) const;
    /** Gets flag which detemines whether particle speed is changed. */
    bool getKeepVelocity(void) const;

protected:
    Real mRandomness;
    Real mScope;
    bool mKeepVelocity;
};

/** Factory class for DirectionRandomiserAffector. */
class SE_ENTRY  DirectionRandomiserAffectorFactory: public ParticleAffectorFactory
{
    /** See ParticleAffectorFactory */
    String getName() const { return "DirectionRandomiser"; }

    /** See ParticleAffectorFactory */
    ParticleAffector* createAffector(ParticleSystem* psys)
    {
        ParticleAffector* p = new DirectionRandomiserAffector(psys);
        mAffectors.push_back(p);
        return p;
    }
};

#endif
