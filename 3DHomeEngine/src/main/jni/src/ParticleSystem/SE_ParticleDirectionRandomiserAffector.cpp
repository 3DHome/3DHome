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
#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleDirectionRandomiserAffector.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

DirectionRandomiserAffector::DirectionRandomiserAffector(ParticleSystem* psys)
    : ParticleAffector(psys)
{
    mType = "DirectionRandomiser";

    // defaults
    mRandomness = 1.0;
    mScope = 1.0;
    mKeepVelocity = false;       
}

void DirectionRandomiserAffector::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;
    Real length = 0;

    while (!pi.end())
    {
        p = pi.getNext();
        if (mScope > Math::UnitRandom())
        {
            if (!p->direction.isZeroLength())
            {
                if (mKeepVelocity)
                {
                    length = p->direction.length();
                }

                p->direction += Vector3(Math::RangeRandom(-mRandomness, mRandomness) * timeElapsed,
                        Math::RangeRandom(-mRandomness, mRandomness) * timeElapsed,
                        Math::RangeRandom(-mRandomness, mRandomness) * timeElapsed);

                if (mKeepVelocity)
                {
                    p->direction *= length / p->direction.length();
                }
            }
        }
    }
}

void DirectionRandomiserAffector::setRandomness(Real force)
{
    mRandomness = force;
}

void DirectionRandomiserAffector::setScope(Real scope)
{
    mScope = scope;
}

void DirectionRandomiserAffector::setKeepVelocity(bool keepVelocity)
{
    mKeepVelocity = keepVelocity;
}

Real DirectionRandomiserAffector::getRandomness(void) const
{
    return mRandomness;
}

Real DirectionRandomiserAffector::getScope(void) const
{
    return mScope;
}

bool DirectionRandomiserAffector::getKeepVelocity(void) const
{
    return mKeepVelocity;
}


