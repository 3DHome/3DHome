#ifndef SE_SEQUENCE_H
#define SE_SEQUENCE_H
#include "SE_TableManager.h"
#include "SE_ID.h"
#include "SE_KeyFrame.h"
class SE_Sequence
{
public:
    typedef SE_StringID TABLE_ITEM_ID;
    typedef SE_StringID TABLE_ITEM_VALUE;
    typedef SE_IDTrait<SE_StringID> TABLE_ITEM_IDTRAIT;
    SE_StringID getImageMapRef()
    {
        return mImageMapRef;
    }
    void setImageMapRef(const SE_StringID& ref)
    {
        mImageMapRef = ref;
    }
    void setItem(unsigned int key, const SE_StringID& unitID)
    {
        mKeyFrames.setKeyFrame(key, new SE_StringID(unitiD));
    }
    bool getItem(unsigned int key, SE_StringID& out)
    {
        SE_StringID* frame = mKeyFrames.getKeyFrame(key);
        if(frame)
        {
            out = *frame;
            return true;
        }
        else
        {
            out = SE_StringID::INVALID;
            return false;
        }
    }
private:
    SE_StringID mImageMapRef;
    SE_KeyFrameSequence<SE_StringID> mKeyFrames;
};
typedef SE_TableSet<SE_StringID, SE_Sequence> SE_SequenceSet;
typedef SE_TableManager<SE_StringID, SE_SequenceSet> SE_SequenceManager;
#endif
