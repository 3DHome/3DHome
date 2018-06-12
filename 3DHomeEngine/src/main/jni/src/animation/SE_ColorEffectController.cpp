#include "SE_ColorEffectController.h"
void SE_ColorEffect::run()
{

}
////////////////////
void SE_ColorEffectController::addKeyFrame(unsigned int key, SE_ColorEffectFrame* frame)
{
    SE_KeyFrame<SE_ColorEffectFrame*>* keyframe = new SE_KeyFrame<SE_ColorEffectFrame*>;
    keyframe->data = frame;
    keyframe->key = key;
    mKeySequence.addKeyFrame(keyframe);
}
SE_ColorEffectFrame* SE_ColorEffectController::getKeyFrame(unsigned int key)
{
    SE_KeyFrame<SE_ColorEffectFrame*>* frame = mKeySequence.getKeyFrame(key);
    return frame;
}
