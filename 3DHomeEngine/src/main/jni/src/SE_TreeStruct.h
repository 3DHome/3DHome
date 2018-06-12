#ifndef SE_TREESTRUCT_H
#define SE_TREESTRUCT_H
#include "SE_ID.h"
#include <list>
#include <vector>
#include <algorithm>
template <typename T>
class SE_TreeStructManager;

template <typename T>
class SE_TreeStruct
{
    friend class SE_TreeStructManager<T>;
public:
    SE_TreeStructID getID() const
    {
        return mID;
    }
    virtual ~SE_TreeStruct() {}
    std::vector<T*> getChildren() const;
private:
    void removeChild(T* child);
    void setID(const SE_TreeStructID& id)
    {
        mID = id;
    }
    void addChild(T* child);
private:
    SE_TreeStructID mID;
    std::list<T*> mChildren;
};
template <typename T>
void SE_TreeStruct<T>::removeChild(T* child)
{
    mChildren.remove(child);
}
template <typename T>
void SE_TreeStruct<T>::addChild(T* child)
{
#if defined(WIN32)
    std::list<T*>::iterator it = find(mChildren.begin(), mChildren.end(), child);
#else
    typename std::list<T*>::iterator it = find(mChildren.begin(), mChildren.end(), child);
#endif
    if(it == mChildren.end())
        mChildren.push_back(child);
}

template <typename T>
std::vector<T*> SE_TreeStruct<T>::getChildren() const
{
    std::vector<T*> ret(mChildren.size());
    copy(mChildren.begin(), mChildren.end() , ret.begin());
    return ret;
}
////////////////
template <typename T>
class SE_ListStruct
{
    friend class SE_TreeStructManager<T>;
public:
    virtual ~SE_ListStruct() {}
    SE_TreeStructID getID() const
    {
        return mID;
    }
protected:
    std::vector<T*> getChildren() const;
private:
    void removeChild(T* child);
    void setID(const SE_TreeStructID& id)
    {
        mID = id;
    }
    void addChild(T* child);
private:
    SE_TreeStructID mID;
    
};
template <typename T>
void SE_ListStruct<T>::removeChild(T* child)
{}
template <typename T>
void SE_ListStruct<T>::addChild(T* child)
{}
template <typename T>
std::vector<T*> SE_ListStruct<T>::getChildren() const
{
    std::vector<T*> ret;
    return ret;
}

#endif
