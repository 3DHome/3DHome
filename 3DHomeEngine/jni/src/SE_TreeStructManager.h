#ifndef SE_TREESTRUCTMANAGER_H
#define SE_TREESTRUCTMANAGER_H
// tree struct is used to manage tree struct
// and each tree node has a pointer, we can use
// an index to get the pointer, this index is the ID
#include "SE_ID.h"
#include "SE_Application.h"
#include "SE_DelayDestroy.h"
#include "SE_DynamicArray.h"
#include "SE_Log.h"
#include <list>
#include <vector>
/*
 * SE_TreeStructManager's T must derived from SE_TreeStruct or SE_ListStruct
 * */
template <typename T>
class SE_TreeStructManager
{
public:
    enum {MAX_SIZE = 2000000};
    enum {SE_NO_ERROR, SE_NO_SPACE, SE_DUP_POINTER, SE_EXCEPT_ERROR};
    SE_TreeStructManager(size_t size = 1000, size_t maxSize = MAX_SIZE);
    ~SE_TreeStructManager();
    T* find(const SE_TreeStructID& id);
    T* remove(const SE_TreeStructID& id);
    void release(T* p, int delay = SE_RELEASE_DELAY);
    void release(const SE_TreeStructID& id, int delay = SE_RELEASE_DELAY);
    //return is child's id
    SE_TreeStructID add(const SE_TreeStructID& parent, T* child, bool linkToParent = true);
    void add(T* parent, T* child)
    {
        parent->addChild(child);
    }
    T* getParent(const SE_TreeStructID& id);
    std::vector<T*> getChildren(const SE_TreeStructID& id);
    int getError() const
    {
        return mError;
    }
    void check();
    size_t getAllocatedNodeNum() const;
    T* clone(const SE_TreeStructID& id);
private:
    struct _Node
    {
        T* data;
        bool alloc;
        SE_TreeStructID parent;
        _Node()
        {
            data = NULL;
            alloc = false;
        }
    };
    struct _NodeData
    {
        _Node* node;
        size_t index;
    };
public:
    template <typename CONDITION>
    std::vector<T*> remove_if(CONDITION condition)
    {
#if defined(WIN32)
        std::list<_NodeData> removedNode;
#else
        std::list<_NodeData> removedNode;
#endif
        for(size_t i = 0 ; i < mNodes.size() ; i++)
        {
            _Node* node = &mNodes[i];
            if(node->alloc && node->data && condition(node->data))
            {
                _NodeData nd;
                nd.node = node;
                nd.index = i;
                removedNode.push_back(nd);
            }
        } 
        std::list<T*> removeList;
#if defined(WIN32)
        std::list<_NodeData>::iterator it;
#else
        typename std::list<_NodeData>::iterator it;
#endif
        for(it = removedNode.begin() ; it != removedNode.end() ; it++)
        {
            T* data = removeNode(it->index, it->node);
            removeList.push_back(data);
        }
        std::vector<T*> ret(removeList.size());
        copy(removeList.begin(), removeList.end(), ret.begin());
        return ret;
    }

private:
    SE_TreeStructID createID(size_t index);
    _Node* findNode(const SE_TreeStructID& id);
    bool findIndex(size_t index)
    {
        _NodeIndexList::iterator it;
        bool ret = false;
        for(it = mFreeNodeIndexList.begin() ; it != mFreeNodeIndexList.end() ; it++)
        {
            if(*it == index)
                return true;
        }
        return false;
    }
    void createFreeIndexList()
    {
        for(size_t i = 0 ; i < mNodes.size() ; i++)
        {
            _Node* node = &mNodes[i];
            if(!node->alloc)
            {
                SE_ASSERT(node->data == NULL);
                mFreeNodeIndexList.push_back(i);
            }
        }
    }
    typedef std::list<size_t> _NodeIndexList;
    T* removeNode(size_t index, _Node* node);
public:
    template <typename FUNC>
    void traverseList(FUNC f)
    {
        _NodeIndexList::iterator it;
        for(it = mBusyNodeIndexList.begin() ; it != mBusyNodeIndexList.end() ; it++)
        {
            size_t index = *it;
            _Node* node = &mNodes[index];
            f(node->data);
        }
    }
private:
    _NodeIndexList mFreeNodeIndexList;
    _NodeIndexList mBusyNodeIndexList;
    SE_DynamicArray<_Node> mNodes;
    int mError;
};
///////////////////////////////////////////////////////////////////

template <typename T>
SE_TreeStructManager<T>::SE_TreeStructManager(size_t size, size_t maxSize) : mNodes(size , maxSize), mError(0)
{
    for(size_t i = 0 ; i < mNodes.size() ; i++)
    {
        mFreeNodeIndexList.push_back(i);
    }
}
template <typename T>
SE_TreeStructManager<T>::~SE_TreeStructManager()
{
    for(size_t i = 0 ; i < mNodes.size() ; i++)
    {
        _Node* node = &mNodes[i];
        if(node->alloc)
        {
            SE_DelayDestroy* dd = new SE_DelayDestroyPointer<T>(node->data);
            bool ret = SE_Application::getInstance()->addDelayDestroy(dd);
            if(!ret)
                delete dd;
        }
    }
}
template <typename T>
SE_TreeStructID SE_TreeStructManager<T>::createID(size_t index)
{
    return SE_TreeStructID(index, SE_Application::getInstance()->getSeqNum());
}
template <typename T>
T* SE_TreeStructManager<T>::find(const SE_TreeStructID& id)
{
    _Node* node = findNode(id);
    if(!node)
        return NULL;
    return node->data;
}
template <typename T>
typename SE_TreeStructManager<T>::_Node* SE_TreeStructManager<T>::findNode(const SE_TreeStructID& id)
{
   size_t index = id.get(0);
   if(index < 0 || index >= mNodes.size()) 
       return NULL;
   _Node* node = &mNodes[index];
   if(!node->alloc || node->data == NULL)
       return NULL;
   const SE_TreeStructID& nodeDataID = node->data->getID();
   if(nodeDataID != id)
       return NULL;
   return node;    
}
template <typename T>
T* SE_TreeStructManager<T>::removeNode(size_t index, _Node* node)
{
    T* retData = node->data;
    node->alloc = false;
    node->data = NULL;
    _Node* parentNode = findNode(node->parent);
    if(parentNode)
    {
        SE_ASSERT(parentNode->alloc && parentNode->data != NULL);
        parentNode->data->removeChild(retData);
    }
    node->parent = SE_TreeStructID::NULLID;
    mFreeNodeIndexList.push_back(index);
    mBusyNodeIndexList.remove(index);
    std::vector<T*> children = retData->getChildren();
    for(size_t i = 0 ; i < children.size() ; i++)
    {
        T* cc = children[i];
        remove(cc->getID());
    }
#ifdef _DEBUG
    check();
#endif
    return retData;
}
template <typename T>
T* SE_TreeStructManager<T>::remove(const SE_TreeStructID& id)
{
    _Node* node = findNode(id);
    if(!node)
        return NULL;
    size_t index = id.get(0);
#ifdef _DEBUG
    {
        bool ret = findIndex(index);
        SE_ASSERT(!ret);
    }
#endif
    return removeNode(index, node);
}
template <typename T>
void SE_TreeStructManager<T>::release(T* p, int delay)
{
    if(p == NULL)
        return;
    if(delay == SE_RELEASE_NO_DELAY)
    {
        delete p;
    }
    else
    {
        SE_DelayDestroy* dd = new SE_DelayDestroyPointer<T>(p);
        bool ret = SE_Application::getInstance()->addDelayDestroy(dd);
        if(!ret)
        {
            delete dd;
        }
        std::vector<T*> children = p->getChildren();
        for(size_t i = 0 ; i < children.size() ; i++)
        {
            dd = new SE_DelayDestroyPointer<T>(children[i]);
            ret = SE_Application::getInstance()->addDelayDestroy(dd);
            if(!ret)
                delete dd;
        }
    }
}
template <typename T>
void SE_TreeStructManager<T>::release(const SE_TreeStructID& id, int delay)
{
    _Node* node = findNode(id);
    if(node)
    {
        SE_ASSERT(node->data);
        release(node->data, delay);
    }
}

template <typename T>
SE_TreeStructID SE_TreeStructManager<T>::add(const SE_TreeStructID& parentID, T* child, bool linkToParent)
{
    if(parentID != SE_TreeStructID::NULLID)
    {
        _Node* parentNode = findNode(parentID);
        if(!parentNode)
            return SE_TreeStructID::INVALID;
        if(mFreeNodeIndexList.empty())
        {
            mNodes.expand();
            createFreeIndexList();
            if(mFreeNodeIndexList.empty())
                return SE_TreeStructID::INVALID;
        }
        if(linkToParent)
            parentNode->data->addChild(child);
    }
    size_t index = mFreeNodeIndexList.front();
    mFreeNodeIndexList.pop_front();
    mBusyNodeIndexList.push_back(index);
    SE_ASSERT(index >= 0 && index < mNodes.size());
    _Node* node = &mNodes[index];
    node->alloc = true;
    node->data = child;
    node->parent = parentID;
    SE_TreeStructID childID = createID(index);
    child->setID(childID);
    std::vector<T*> children = child->getChildren();
    for(size_t i = 0 ; i < children.size() ; i++)
    {
        T* cc = children[i];
        add(childID, cc, linkToParent);
    }
#ifdef _DEBUG
    check();
#endif
    return childID;
}
template <typename T>
void SE_TreeStructManager<T>::check()
{
    if(mError != SE_NO_ERROR)
    {
        LOGE("error: %d\n", mError);
    }
    size_t allocNum = getAllocatedNodeNum();
    size_t num = 0;
    for(size_t i = 0 ; i < mNodes.size() ; i++)
    {
        if(mNodes[i].alloc)
            num++;
    }
    if(num != allocNum)
    {
        LOGE("error : node list alloc num is not equal free node list num\n");
    }
}
template <typename T>
size_t SE_TreeStructManager<T>::getAllocatedNodeNum() const
{
    size_t size = mFreeNodeIndexList.size();
    return mNodes.size() - size;
}
template <typename T>
T* SE_TreeStructManager<T>::getParent(const SE_TreeStructID& id)
{
    _Node* node = findNode(id);
    if(!node)
        return NULL;
    _Node* parentNode = findNode(node->parent);
    if(!parentNode)
        return NULL;
    SE_ASSERT(parentNode->data);
    return parentNode->data;
    
}
template <typename T>
std::vector<T*> SE_TreeStructManager<T>::getChildren(const SE_TreeStructID& id)
{
    std::vector<T*> ret;
    _Node* node = findNode(id);
    if(!node)
        return ret;
    SE_ASSERT(node->data);
    return node->data->getChildren();
}
#endif
