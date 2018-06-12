#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleBillboard.h"
#include "ParticleSystem/SE_ParticleBillboardSet.h"
#include "SE_MemLeakDetector.h"

Billboard::Billboard():
    mOwnDimensions(false),
    mUseTexcoordRect(false),
    mTexcoordIndex(0),
    mPosition(Vector3::ZERO),
    mDirection(Vector3::ZERO),        
    mParentSet(0),
    mColour(ColourValue::White),
    mRotation(0)
{
}

Billboard::~Billboard()
{
}

Billboard::Billboard(const Vector3& position, BillboardSet* owner, const ColourValue& colour)
    : mOwnDimensions(false)
    , mUseTexcoordRect(false)
    , mTexcoordIndex(0)
    , mPosition(position)
    , mDirection(Vector3::ZERO)
    , mParentSet(owner)
    , mColour(colour)
    , mRotation(0)
{
}

void Billboard::setRotation(const Radian& rotation)
{
    mRotation = rotation;
    if (mRotation != Radian(0)) {
        mParentSet->_notifyBillboardRotated();
    }
}

void Billboard::setPosition(const Vector3& position)
{
    mPosition = position;
}

void Billboard::setPosition(Real x, Real y, Real z)
{
    mPosition.x = x;
    mPosition.y = y;
    mPosition.z = z;
}

const Vector3& Billboard::getPosition(void) const
{
    return mPosition;
}

void Billboard::setDimensions(Real width, Real height)
{
    mOwnDimensions = true;
    mWidth = width;
    mHeight = height;
    mParentSet->_notifyBillboardResized();
}

bool Billboard::hasOwnDimensions(void) const
{
    return mOwnDimensions;
}

void Billboard::_notifyOwner(BillboardSet* owner)
{
    mParentSet = owner;
}

void Billboard::setColour(const ColourValue& colour)
{
    mColour = colour;
}

const ColourValue& Billboard::getColour(void) const
{
    return mColour;
}

Real Billboard::getOwnWidth(void) const
{
    return mWidth;
}

Real Billboard::getOwnHeight(void) const
{
    return mHeight;
}

void Billboard::setTexcoordIndex(uint16 texcoordIndex)
{
    mTexcoordIndex = texcoordIndex;
    mUseTexcoordRect = false;
}

void Billboard::setTexcoordRect(const FloatRect& texcoordRect)
{
    mTexcoordRect = texcoordRect;
    mUseTexcoordRect = true;
}

void Billboard::setTexcoordRect(Real u0, Real v0, Real u1, Real v1)
{
    setTexcoordRect(FloatRect(u0, v0, u1, v1));
}




