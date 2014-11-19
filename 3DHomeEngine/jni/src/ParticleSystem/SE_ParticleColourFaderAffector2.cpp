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
#include "ParticleSystem/SE_ParticleColourFaderAffector2.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

ColourFaderAffector2::ColourFaderAffector2(ParticleSystem* psys): ParticleAffector(psys)
{
    mRedAdj1 = mGreenAdj1 = mBlueAdj1 = mAlphaAdj1 = 0;
    mRedAdj2 = mGreenAdj2 = mBlueAdj2 = mAlphaAdj2 = 0;
    mType = "ColourFader2";
    StateChangeVal = 1;	// Switch when there is 1 second left on the TTL     
}

void ColourFaderAffector2::_affectParticles(ParticleSystem* pSystem, Real timeElapsed)
{
    ParticleIterator pi = pSystem->_getIterator();
    Particle *p;
    float dr1, dg1, db1, da1;
    float dr2, dg2, db2, da2;

    // Scale adjustments by time
    dr1 = mRedAdj1 * timeElapsed;
    dg1 = mGreenAdj1 * timeElapsed;
    db1 = mBlueAdj1 * timeElapsed;
    da1 = mAlphaAdj1 * timeElapsed;

    // Scale adjustments by time
    dr2 = mRedAdj2  * timeElapsed;
    dg2 = mGreenAdj2 * timeElapsed;
    db2 = mBlueAdj2 * timeElapsed;
    da2 = mAlphaAdj2 * timeElapsed;

    while (!pi.end())
    {
	p = pi.getNext();

	if (p->timeToLive > StateChangeVal)
	{
	    applyAdjustWithClamp(&p->colour.r, dr1);
	    applyAdjustWithClamp(&p->colour.g, dg1);
	    applyAdjustWithClamp(&p->colour.b, db1);
	    applyAdjustWithClamp(&p->colour.a, da1);
	}
	else
	{
	    applyAdjustWithClamp(&p->colour.r, dr2);
	    applyAdjustWithClamp(&p->colour.g, dg2);
	    applyAdjustWithClamp(&p->colour.b, db2);
	    applyAdjustWithClamp(&p->colour.a, da2);
	}
    }
}

void ColourFaderAffector2::setAdjust1(float red, float green, float blue, float alpha)
{
    mRedAdj1 = red;
    mGreenAdj1 = green;
    mBlueAdj1 = blue;
    mAlphaAdj1 = alpha;
}

void ColourFaderAffector2::setAdjust2(float red, float green, float blue, float alpha)
{
    mRedAdj2 = red;
    mGreenAdj2 = green;
    mBlueAdj2 = blue;
    mAlphaAdj2 = alpha;
}

void ColourFaderAffector2::setRedAdjust1(float red)
{
    mRedAdj1 = red;
}

void ColourFaderAffector2::setRedAdjust2(float red)
{
    mRedAdj2 = red;
}

float ColourFaderAffector2::getRedAdjust1(void) const
{
    return mRedAdj1;
}

float ColourFaderAffector2::getRedAdjust2(void) const
{
    return mRedAdj2;
}

void ColourFaderAffector2::setGreenAdjust1(float green)
{
    mGreenAdj1 = green;
}

void ColourFaderAffector2::setGreenAdjust2(float green)
{
    mGreenAdj2 = green;
}

float ColourFaderAffector2::getGreenAdjust1(void) const
{
    return mGreenAdj1;
}

float ColourFaderAffector2::getGreenAdjust2(void) const
{
    return mGreenAdj2;
}

void ColourFaderAffector2::setBlueAdjust1(float blue)
{
    mBlueAdj1 = blue;
}

void ColourFaderAffector2::setBlueAdjust2(float blue)
{
    mBlueAdj2 = blue;
}

float ColourFaderAffector2::getBlueAdjust1(void) const
{
    return mBlueAdj1;
}

float ColourFaderAffector2::getBlueAdjust2(void) const
{
    return mBlueAdj2;
}

void ColourFaderAffector2::setAlphaAdjust1(float alpha)
{
    mAlphaAdj1 = alpha;
}

void ColourFaderAffector2::setAlphaAdjust2(float alpha)
{
    mAlphaAdj2 = alpha;
}

float ColourFaderAffector2::getAlphaAdjust1(void) const
{
    return mAlphaAdj1;
}

float ColourFaderAffector2::getAlphaAdjust2(void) const
{
    return mAlphaAdj2;
}

void ColourFaderAffector2::setStateChange(Real NewValue)
{
    StateChangeVal = NewValue;
}

Real ColourFaderAffector2::getStateChange(void) const
{
    return StateChangeVal;
}
   



