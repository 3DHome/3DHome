#ifndef SE_COLOREFFECTCONTROLLER_H
#define SE_COLOREFFECTCONTROLLER_H
#include "SE_KeyFrame.h"
#include "SE_ID.h"
#include "SE_Vector.h"
class SE_ColorEffectFrame
{
public:
    virtual ~SE_ColorEffectFrameFunc()
    {}
    virtual void run() {}
};
class SE_ColorEffect : public SE_ColorEffectFrame
{
public:
    enum {MARK_A, MARK_R, MARK_G, MARK_B, MARK_NUM};
    struct _TextureColor
    {
        SE_StringID mTextureID;
        SE_Vector3i mColor;
        SE_StringID mMark;
    };
    void setBackground(const SE_StringID& background)
    {
        mBackGroundID = background;
    }
    SE_StringID getBackground()
    {
        return mBackgroundID;
    }
    void setChannel(const SE_StringID& channel)
    {
        mChannelID = channel;
    }
    SE_StringID getChannel()
    {
        return mChannelID;
    }
    void setAlpha(int alpha)
    {
        mAlpha = alpha;
    }
    int getAlpha()
    {
        return mAlpha;
    }
    void setTextureColor(int index, _TextureColor* tc)
    {
        if(index < MARK_A || index >= MARK_NUM)
            return;
        _TextureColor* oldtc = mTextureColorData[index];
        if(oldtc)
            delete oldtc;
        mTextureColorData[index] = tc;
    }
    _TextureColor* getTextureColor(int index)
    {
        if(index < MARK_A || index >= MARK_NUM)
            return;
        return mTextureColorData[index];
    }
    void run();
private:
    SE_StringID mBackgroundID;
    SE_StringID mChannelID;
    int mAlpha;
    std::vector<_TextureColor*> mTextureColorData;
};
class SE_ColorEffectReload : public SE_ColorEffectFrame
{
public:
    void run();
    void setMark(int mark)
    {
        mMark = mark;
    }
    int getMark()
    {
        return mMark;
    }
    void setTexture(const SE_StringID& texture)
    {
        mTexture = texture;
    }
    SE_StringID getTexture()
    {
        return mTexture;
    }
private:
    int mMark;
    SE_StringID mTexture;
};
class SE_ColorEffectController
{
public:
    void addKeyFrame(unsigned int key, SE_ColorEffectFrame* frame);
    SE_ColorEffectFrame* getKeyFrame(unsigned int key);
private:
    SE_KeyFrameSequence<SE_ColorEffectFrame*> mKeySequence;
};
#endif
