#include "SE_DynamicLibType.h"
#include "SE_ObjectManager.h"
#include "SE_RenderUnit.h"
#include "SE_RenderUnitManager.h"
#include "SE_MemLeakDetector.h"

SE_RenderUnitManager::SE_RenderUnitManager()
{}

SE_RenderUnitManager::~SE_RenderUnitManager()
{}

bool SE_RenderUnitManager::insert(const char *ruName, SE_RenderUnit *ru,int i)
{
    std::string name(ruName);
    return mRenderUnitMap.set(name,ru);
}

SE_RenderUnit* SE_RenderUnitManager::find(const char *ruName)
{
    std::string name(ruName);
    return mRenderUnitMap.get(name);
}

void SE_RenderUnitManager::remove(const char *ruName)
{
    std::string name(ruName);
    mRenderUnitMap.remove(name);
}