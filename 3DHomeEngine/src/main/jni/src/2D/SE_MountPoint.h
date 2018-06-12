#ifndef SE_MOUNTPOINT_H
#define SE_MOUNTPOINT_H
#include "SE_ID.h"
//ELementPoint indicate the access point of one element in its element group
class SE_ENTRY SE_MountPoint
{
public:
    SE_MountPoint();
    ~SE_MountPoint();
    SE_MountPoint(float x, float y);
    SE_MountPoint(const SE_MountPoint& );
    SE_MountPoint& operator=(const SE_MountPoint&);
    float getX() const
    {
        return mX;
    }
    float getY() const
    {
        return mY;
    }
    void setX(float x)
    {
        mX = x;
    }
    void setY(float y)
    {
        mY = y;
    }
    SE_MountPointID getID() const
    {
        return mID;
    }
    void setID(const SE_MountPointID& mpID)
    {
        mID = mpID;
    }
    friend bool operator==(const SE_MountPoint& left, const SE_MountPoint& right)
    {
        return left.mID == right.mID;
    }
    friend bool operator!=(const SE_MountPoint& left, const SE_MountPoint& right)
    {
        return left.mID == right.mID;
    }
    bool isValid() const
    {
        return mID.isValid();
    }
private:
    float mX;
    float mY;
    SE_MountPointID mID;
};
#endif
