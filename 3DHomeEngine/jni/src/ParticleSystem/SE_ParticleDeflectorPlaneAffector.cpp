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
#include "ParticleSystem/SE_ParticleDeflectorPlaneAffector.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

DeflectorPlaneAffector::DeflectorPlaneAffector(ParticleSystem* psys)
    : ParticleAffector(psys)
{
    mType = "DeflectorPlane";

    // defaults
    mPlanePoint = Vector3::ZERO;
    mPlaneNormal = Vector3::UNIT_Y;
    mBounce = 1.0;
}

void DeflectorPlaneAffector::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    // precalculate distance of plane from origin
    Real planeDistance = - mPlaneNormal.dotProduct(mPlanePoint) / Math::Sqrt(mPlaneNormal.dotProduct(mPlaneNormal));
    Vector3 directionPart;

    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;

    while (!pi.end())
    {
        p = pi.getNext();

        Vector3 direction(p->direction * timeElapsed);
        if (mPlaneNormal.dotProduct(p->position + direction) + planeDistance <= 0.0)
        {
            Real a = mPlaneNormal.dotProduct(p->position) + planeDistance;
            if (a > 0.0)
            {
                // for intersection point
		directionPart = direction * (- a / direction.dotProduct( mPlaneNormal ));
                // set new position
		p->position = (p->position + ( directionPart )) + (((directionPart) - direction) * mBounce);

                // reflect direction vector
                p->direction = (p->direction - (2.0 * p->direction.dotProduct( mPlaneNormal ) * mPlaneNormal)) * mBounce;
            }
        }
    }
}

void DeflectorPlaneAffector::setPlanePoint(const Vector3& pos)
{
    mPlanePoint = pos;
}

void DeflectorPlaneAffector::setPlaneNormal(const Vector3& normal)
{
    mPlaneNormal = normal;
}

void DeflectorPlaneAffector::setBounce(Real bounce)
{
    mBounce = bounce;
}

Vector3 DeflectorPlaneAffector::getPlanePoint(void) const
{
    return mPlanePoint;
}

Vector3 DeflectorPlaneAffector::getPlaneNormal(void) const
{
    return mPlaneNormal;
}

Real DeflectorPlaneAffector::getBounce(void) const
{
    return mBounce;
}

 
