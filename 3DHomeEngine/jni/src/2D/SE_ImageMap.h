#ifndef SE_IMAGEMAP_H
#define SE_IMAGEMAP_H
#include "SE_ID.h"
#include "SE_ObjectManager.h"
#include "SE_TableManager.h"
struct SE_ImageRect
{
    int x;
    int y;
    int pivotx;
    int pivoty;
    int width;
    int height;
    SE_ImageRect()
    {
        x = y = 0;
        pivotx = pivoty = 0;
        width = height = -1;
    }
};
template<>
struct SE_IDTrait<SE_ImageMapID>
{
    static SE_ImageMapID SE_ConvertToID(const char* str)
    {
        return SE_ID::createImageMapID(str);
    }
}
class SE_ImageMap : public SE_Table<SE_StringID, SE_ImageRect*, SE_IDTrait<SE_StringID> >
{
public:
	/*
    typedef SE_ImageMapID TABLE_ITEM_ID;
    typedef SE_ImageRect* TABLE_ITEM_VALUE;
    typedef SE_IDTrait<SE_ImageMapID> TABLE_ITEM_IDTRAIT;
	*/
    enum {NO_MIRROR, MIRROR_X, MIRROR_Y, MIRROR_XY};
    SE_ImageMap()
    {
        mUnitWidth = -1;
        mUnitHeight = -1;
        mMirrorType = NO_MIRROR;
    }
    void setMirrorType(int mt)
    {
        mMirrorType = mt;
    }
    int getMirrorType()
    {
        return mMirrorType;
    }
    void setID(const SE_ImageMapID& id)
    {
        mID = id;
    }
    SE_ImageMapID getID()
    {
        return mID;
    }
    SE_ImageDataID getImageDataID()
    {
        return mImageDataID;
    }
    void setImageDataID(const SE_ImageDataID& imageDataID)
    {
        mImageDataID = imageDataID;
    }
    int getUnitWidth()
    {
        return mUnitWidth;
    }
    void setUnitWidth(int w)
    {
        mUnitWidth = w;
    }
    int getUnitHeight()
    {
        return mUnitHeight;
    }
    void setUnitHeight(int h)
    {
        mUnitHeight = h;
    }
	/*
    void setImageRect(const SE_ImageRectID& imageRectID, SE_ImageRect* rect)
    {
        mImageRectSet.set(imageRectID, rect);
    }
    SE_ImageRect* getImageRect(const SE_ImageRectID& imageRectID)
    {
        return mImageRectSet.get(imageRectID);
    }
    void removeImageRect(const SE_ImageRectID& imageRectID)
    {
        mImageRectSet.remove(imageRectID);
    }
	*/
private:
    SE_ImageMapID mID;
    SE_ImageDataID mImageDataID;
    int mUnitWidth;
    int mUnitHeight;
    int mMirrorType;
    SE_ObjectManager<SE_ImageRectID, SE_ImageRect> mImageRectSet;
};
typedef SE_TableSet<SE_ImageMapID, SE_ImageMap> SE_ImageMapSet;
typedef SE_TableManager<SE_ImageMapSetID, SE_ImageMapSet> SE_ImageMapManager;
#endif
