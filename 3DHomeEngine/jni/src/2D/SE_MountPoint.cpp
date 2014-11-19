#include "SE_DynamicLibType.h"
#include "SE_MountPoint.h"
#include "SE_MemLeakDetector.h"
SE_MountPoint::SE_MountPoint()
{
    mX = mY = 0;
}
SE_MountPoint::~SE_MountPoint()
{}
SE_MountPoint::SE_MountPoint(float x, float y)
{
    mX = x;
    mY = y;
}
SE_MountPoint::SE_MountPoint(const SE_MountPoint& p)
{
    mX = p.mX;
    mY = p.mY;
}
SE_MountPoint& SE_MountPoint::operator=(const SE_MountPoint& p)
{
    if(this == &p)
        return *this;
    mX = p.mX;
    mY = p.mY;
    return *this;
}
