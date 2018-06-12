#ifndef SE_SIMOBJECTMANAGER_H
#define SE_SIMOBJECTMANAGER_H
#include "SE_ID.h"
#include "SE_SimObject.h"
#include "SE_ObjectManager.h"
class SE_ENTRY SE_SimObjectManager
{
public:
    bool set(const SE_SimObjectID& simObjectID, SE_SimObject* simObject);
    SE_SimObject* get(const SE_SimObjectID& simObjectID);
    void remove(const SE_SimObjectID& simObjectiD);
    SE_SimObject* findByName(const char* name);
    void unLoadScene();
private:
    SE_ObjectManager<SE_SimObjectID, SE_SimObject> mSimObjectManager;
};
#endif
