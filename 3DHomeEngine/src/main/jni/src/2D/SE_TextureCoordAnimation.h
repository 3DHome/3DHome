#ifndef SE_TEXTURECOORDANIMATION_H
#define SE_TEXTURECOORDANIMATION_H
#include "SE_Animation.h"
#include "SE_ID.h"
#include <list>
class SE_ENTRY SE_TextureCoordAnimation : public SE_Animation
{
public:
    struct Point
    {
        int x;
        int y;
        Point()
        {
            x = y = 0;
        }
        Point(int x, int y)
        {
            this->x = x;
            this->y = y;
        }
        friend bool operator==(const Point& left, const Point& right)
        {
            return left.x == right.x && left.y == right.y;
        }
        friend bool operator!=(const Point& left, const Point& right)
        {
            return left.x != right.x || left.y != right.y;
        }
    };
    SE_TextureCoordAnimation();
    ~SE_TextureCoordAnimation();
    void setImageDataID(const SE_ImageDataID& imageDataID)
    {
        mImageDataID = imageDataID;
    }
    void setUnitWidth(int w)
    {
        mUnitWidth = w;
    }
    void setUnitHeight(int h)
    {
        mUnitHeight = h;
    }
    void addCoord(const Point& p)
    {
        mCoordList.push_back(p);
    }
    void removeCoord(const Point& p);
public:
    virtual void onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex);
    virtual void onRun();
    virtual SE_Animation* clone();
private:
    int mUnitWidth;
    int mUnitHeight;
    typedef std::list<Point> _CoordList;
    _CoordList mCoordList;
    SE_ImageDataID mImageDataID;
};
#endif
