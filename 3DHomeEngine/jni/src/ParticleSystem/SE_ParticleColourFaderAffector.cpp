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
#include "ParticleSystem/SE_ParticleColourFaderAffector.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

ColourFaderAffector::ColourFaderAffector(ParticleSystem* psys) : ParticleAffector(psys)
{
    mRedAdj = mGreenAdj = mBlueAdj = mAlphaAdj = 0;
    mType = "ColourFader";
}

void ColourFaderAffector::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;
    float dr, dg, db, da;

    // Scale adjustments by time
    dr = mRedAdj * timeElapsed;
    dg = mGreenAdj * timeElapsed;
    db = mBlueAdj * timeElapsed;
    da = mAlphaAdj * timeElapsed;

    while (!pi.end())
    {
        p = pi.getNext();
        applyAdjustWithClamp(&p->colour.r, dr);
        applyAdjustWithClamp(&p->colour.g, dg);
        applyAdjustWithClamp(&p->colour.b, db);
        applyAdjustWithClamp(&p->colour.a, da);
    }
}

void ColourFaderAffector::setAdjust(float red, float green, float blue, float alpha)
{
    mRedAdj = red;
    mGreenAdj = green;
    mBlueAdj = blue;
    mAlphaAdj = alpha;
}

void ColourFaderAffector::setRedAdjust(float red)
{
    mRedAdj = red;
}

float ColourFaderAffector::getRedAdjust(void) const
{
    return mRedAdj;
}

void ColourFaderAffector::setGreenAdjust(float green)
{
    mGreenAdj = green;
}

float ColourFaderAffector::getGreenAdjust(void) const
{
    return mGreenAdj;
}

void ColourFaderAffector::setBlueAdjust(float blue)
{
    mBlueAdj = blue;
}

float ColourFaderAffector::getBlueAdjust(void) const
{
    return mBlueAdj;
}

void ColourFaderAffector::setAlphaAdjust(float alpha)
{
    mAlphaAdj = alpha;
}

float ColourFaderAffector::getAlphaAdjust(void) const
{
    return mAlphaAdj;
}




