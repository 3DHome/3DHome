#ifndef SE_OBJECTMANAGER_H
#define SE_OBJECTMANAGER_H
#include <map>
template <class T>
struct SE_FindObjCondition
{
    virtual ~SE_FindObjCondition() {}
    virtual bool isSatisfy(T *) = 0;
};
template <class TID, class T>
class SE_ObjectManager
{
public:
    typedef std::map<TID, T*> RMap;
    T* get(const TID& id);
    bool set(const TID& id, T* data);
    void remove(const TID& id);
    void removeKey(const TID& id);
    T* find(SE_FindObjCondition<T>& fc);
    bool isContain(const TID& id);
    ~SE_ObjectManager();
    RMap* getMap()
    {
        return &m;
    }
private:
    RMap m;
};

template <class TID, class T>
void SE_ObjectManager<TID, T>::remove(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it != m.end())
    {
        delete it->second;
        m.erase(it);
    }
}

template <class TID, class T>
void SE_ObjectManager<TID, T>::removeKey(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it != m.end())
    {        
        m.erase(it);
    }
}

template <class TID, class T>
T* SE_ObjectManager<TID, T>::get(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
        return NULL;
    else
        return it->second;
}
template <class TID, class T>
bool SE_ObjectManager<TID, T>::set(const TID& id, T* data)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
    {
        m.insert(std::pair<TID, T*>(id, data));
        return true;
    }
    return false;    
}
template <class TID, class T>
SE_ObjectManager<TID, T>::~SE_ObjectManager()
{
    typename RMap::iterator it;
    for(it = m.begin() ; it != m.end() ; it++)
    {
        T* data = it->second;
        delete data;
    }
}
template <class TID, class T>
T* SE_ObjectManager<TID, T>::find(SE_FindObjCondition<T>& fc)
{
    typename RMap::iterator it;
    for(it = m.begin() ; it != m.end() ; it++)
    {
        T* data = it->second;
        if(fc.isSatisfy(data))
        {
            return data;
        }
    }
    return NULL;
}
template <class TID, class T>
bool SE_ObjectManager<TID, T>::isContain(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
        return false;
    else
        return true;
}
#endif
