//#include "SE_Log.h"
#include "SE_DynamicLibType.h"

#include "SE_Factory.h"
#include "SE_MemLeakDetector.h"
SE_ObjectFactory::ObjectCreateMap* SE_ObjectFactory::mObjectCreateMap = NULL;
SE_ObjectFactory::ObjProperty SE_ObjectFactory::mObjProp;

SE_ObjectFactory::~SE_ObjectFactory()
{
    
}

void SE_ObjectFactory::reg(const char* className, SE_CreateObjectFunc* ocFunc)
{
    if(!mObjProp.init)
    {
        mObjProp.init = true;
        mObjectCreateMap = new ObjectCreateMap();
    }
    std::string str(className);
    ObjectCreateMap::iterator it = mObjectCreateMap->find(str);
    if(it == mObjectCreateMap->end())
    {
        mObjectCreateMap->insert(std::pair<std::string, SE_CreateObjectFunc*>(str, ocFunc));
    }
    else
    {
        //LOGE("error when register class %s\n", className);
    }
}
void SE_ObjectFactory::unreg(const char* className)
{
    if(!mObjProp.init)
    {
        mObjProp.init = true;
        mObjectCreateMap = new ObjectCreateMap();
    }
    std::string str(className);

    ObjectCreateMap::iterator it = mObjectCreateMap->find(str);
    if(it == mObjectCreateMap->end())
    {
        return;
    }
    else
    {
        SE_CreateObjectFunc *func = it->second;
        if(func)
        {
            delete it->second;
        }
        mObjectCreateMap->erase(it++);

    }

}
void* SE_ObjectFactory::create(const char* className)
{
    std::string str(className);
    ObjectCreateMap::iterator it = mObjectCreateMap->find(str);
    if(it == mObjectCreateMap->end())
        return NULL;
    else
    {
        return (SE_Object*)(it->second->create());
    }

}


void SE_ObjectFactory::factoryRelease()
{
    if(mObjectCreateMap)
    {
        ObjectCreateMap::iterator it = mObjectCreateMap->begin();
        for(;it != mObjectCreateMap->end();)
        {
            SE_CreateObjectFunc *func = it->second;
            if(func)
            {
                delete func;
            }

            mObjectCreateMap->erase(it++);
            
        }
        mObjectCreateMap->clear();

        delete mObjectCreateMap;
        mObjectCreateMap = NULL;

    }
}
