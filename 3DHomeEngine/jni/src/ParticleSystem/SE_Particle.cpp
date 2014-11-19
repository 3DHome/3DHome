#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_Particle.h"
#include "SE_MemLeakDetector.h"

void Particle::setRotation(const Radian& rot)
{
    rotation = rot;
    if (rotation != Radian(0)) {
        mParentSystem->_notifyParticleRotated();
    }
}

void Particle::setDimensions(Real width, Real height)
{
    mOwnDimensions = true;
    mWidth = width;
    mHeight = height;
    mParentSystem->_notifyParticleResized();
}

void Particle::_notifyOwner(ParticleSystem* owner)
{
    mParentSystem = owner;
}

void Particle::resetDimensions(void)
{
    mOwnDimensions = false;
}

