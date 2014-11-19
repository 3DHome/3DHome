#ifndef SE_ACTION_H
#define SE_ACTION_H
class SE_ActionUnit
{
public:
    virtual ~SE_ActionUnit() {}
    virtual void action() {}
    SE_ActionUnitID getActionUnitID()
    {
        return mID;
    }
    void setActionUnitID(const SE_ActionUnitID& id)
    {
        mID = id;
    }
    void setKey(unsigned int key)
    {
        mKey = key;
    }
    unsigned int getKey()
    {
        return mKey;
    }
private:
    SE_ActionUnitID mID;
    unsigned int mKey;
};
class SE_AnimationObject : public SE_ActionUnit
{
public:
    void action();
    void setImageUnitRef(const SE_StringID& imageUnitRef)
    {
        mImageUnitRef = imageUnitRef;
    }
    SE_StringID getImageUnitRef()
    {
        return mImageUnitRef;
    }
    void setSequenceFrameRef(const SE_StringID& sequenceFrameRef)
    {
        mSequenceFrameRef = sequenceFrameRef;
    }
    SE_StringID getSequenceFrameRef()
    {
        return mSequenceFrameRef;
    }
    void setControllerRef(const SE_StringID& controllerref)
    {
        mControllerRef = controllerref;
    }
    SE_StringID getControllerRef()
    {
        mControllerRef;
    }
    void setTextureRef(const SE_StringID& textureRef)
    {
        mTextureRef = textureRef;
    }
    SE_StringID getTextureRef()
    {
        return mTextureRef;
    }
    void setLayer(const SE_Layer& layer)
    {
        mLayer = layer;
    }
    SE_Layer getLayer()
    {
        return mLayer;
    }
    int getRepeatMode()
    {
        return mRepeatMode;
    }
    void setRepeatMode(int repeatMode)
    {
        mRepeatMode = repeatMode;
    }
    void setTimeMode(int timeMode)
    {
        mTimeMode = timeMode;
    }
    int getTimeMode()
    {
        return mTimeMode;
    }

private:
    SE_StringID mImageUnitRef;
    SE_StringID mSequenceFrameRef;
    SE_StringID mControllerRef;
    SE_StringID mTextureRef;
    SE_Layer mLayer;
    int mTimeMode;
    int mRepeatMode;
};
class SE_DeleteAction : public SE_ActionUnit
{
public:
    void setRef(const SE_StringID& ref)
    {
        mRef = ref;
    }
    SE_StringID getRef()
    {
        return mRef;
    }
    void action();
private:
    SE_StringID mRef;
};

class SE_MusicObjectAction : public SE_ActionUnit
{};
class SE_Action
{
public:
    enum {RENDER_TO_BUFFER, RENDER_TO_TEXTURE};
    SE_Action();
    ~SE_Action();
    void addActionUnit(SE_ActionUnit* au)
    {
        mActionUnitList.push_back(au);
    }
    void removeActionUnit(const SE_StringID& auID);
    SE_ActionUnit* getActionUnit(const SE_StringID& auID);
    void setRenderMode(int renderMode)
    {
        mRenderMode = renderMode;
    }
    int getRenderMode()
    {
        return mRenderMode;
    }
private:
    typedef std::list<SE_ActionUnit*> _ActionUnitList;
    _ActionUnitList mActionUnitList;
    SE_StringID mImagePath;
    int mRenderMode;
};
#endif
