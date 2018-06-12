#ifndef SE_KEYFRAME_H
#define SE_KEYFRAME_H
#include "SE_Quat.h"
#include "SE_Vector.h"
#include <list>
#include <vector>
struct SE_Transform
{
    SE_Quat rotate;
    SE_Vector3f translate;
    SE_Vector3f scale;
};

//T is the data type
template <typename T>
class SE_KeyFrame
{
public:
    SE_KeyFrame()
    {
        key = 0;
    }
    unsigned int key;// this the number of the  time sequence
    T data;
};
template <typename T>
class SE_KeyFrameSequence
{
public:
    SE_KeyFrameSequence();
    ~SE_KeyFrameSequence();
    SE_KeyFrameSequence(const SE_KeyFrameSequence&);
    SE_KeyFrameSequence& operator=(const SE_KeyFrameSequence&);
    void addKeyFrame(SE_KeyFrame<T>* kf)
    {
        mKeyFrameSequence.push_back(kf);
    }
    void setKeyFrame(SE_KeyFrame<T>* kf);
    //if key is not in key frame sequnce , it will return the 
    //first key frame
    SE_KeyFrame<T>* getKeyFrame(unsigned int key);
    int getKeyFrameNum()
    {
        return mKeyFrameSequence.size();
    }
private:
    typedef std::list<SE_KeyFrame<T>* > _KeyFrameSequence;
    _KeyFrameSequence mKeyFrameSequence;
};
template <typename T>
SE_KeyFrameSequence<T>::SE_KeyFrameSequence()
{}
template <typename T>
SE_KeyFrameSequence<T>::~SE_KeyFrameSequence()
{
    typename _KeyFrameSequence::iterator it ;
    for(it = mKeyFrameSequence.begin() ; it != mKeyFrameSequence.end() ; it++)
    {
        delete *it;
    }

}
template <typename T>
SE_KeyFrameSequence<T>::SE_KeyFrameSequence(const SE_KeyFrameSequence<T>& right)
{
    typename _KeyFrameSequence::iterator it ;
    for(it = right.begin() ; it != right.end() ; it++)   
    {
        SE_KeyFrame<T>* f = *it;
        SE_KeyFrame<T>* nf = new SE_KeyFrame<T>;
        *nf = *f;
        mKeyFrameSequence.push_back(nf);
    }
}
template <typename T>
SE_KeyFrameSequence<T>& SE_KeyFrameSequence<T>::operator=(const SE_KeyFrameSequence<T>& right)
{
    if(this == &right)
    {
        return;
    }
    mKeyFrameSequence.clear();
    typename _KeyFrameSequence::iterator it ;
    for(it = right.begin() ; it != right.end() ; it++)   
    {
        SE_KeyFrame<T>* f = *it;
        SE_KeyFrame<T>* nf = new SE_KeyFrame<T>;
        *nf = *f;
        mKeyFrameSequence.push_back(nf);
    }
    return *this;
}    
template <typename T>
void SE_KeyFrameSequence<T>::setKeyFrame(SE_KeyFrame<T>* kf)
{
    typename _KeyFrameSequence::iterator it ;
    for(it = mKeyFrameSequence.begin() ; it != mKeyFrameSequence.end() ; it++)
    {
        if((*it)->key == kf->key)
        {
            *it = kf;
            return;
        }
    }
    mKeyFrameSequence.push_back(kf);
}
template <typename T>
SE_KeyFrame<T>* SE_KeyFrameSequence<T>::getKeyFrame(unsigned int key)
{
    typename _KeyFrameSequence::iterator it ;
    for(it = mKeyFrameSequence.begin() ; it != mKeyFrameSequence.end() ; it++)
    {
        if((*it)->key == key)
        {
            return *it;
        }
    }
    return NULL;
}
#endif
