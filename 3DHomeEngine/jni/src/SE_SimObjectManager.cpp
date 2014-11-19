#include "SE_DynamicLibType.h"
#include "SE_SimObjectManager.h"
#include <string>
#include "SE_MemLeakDetector.h"
bool SE_SimObjectManager::set(const SE_SimObjectID& simObjectID, SE_SimObject* simObject)
{
    return mSimObjectManager.set(simObjectID, simObject);
}
SE_SimObject* SE_SimObjectManager::get(const SE_SimObjectID& simObjectID)
{
    return mSimObjectManager.get(simObjectID);
}
void SE_SimObjectManager::remove(const SE_SimObjectID& simObjectID)
{
    mSimObjectManager.remove(simObjectID);
}
struct _FindSimObj : public SE_FindObjCondition<SE_SimObject>
{
    bool isSatisfy(SE_SimObject* obj)
    {
        if(objname == obj->getName())
            return true;
        else
            return false;
    }
    std::string objname;
};
SE_SimObject* SE_SimObjectManager::findByName(const char* name)
{
    _FindSimObj fo;
    fo.objname = name;
    SE_SimObject* ret = mSimObjectManager.find(fo);
    return ret;
}

void SE_SimObjectManager::unLoadScene()
{
    
}