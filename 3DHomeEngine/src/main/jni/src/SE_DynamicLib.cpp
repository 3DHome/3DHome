#include "SE_DynamicLibType.h"
#include<string>
#include <dlfcn.h>

#include "SE_Log.h"
#include "SE_Plugin.h"
#include "SE_DynamicLibType.h"

#include "SE_DynamicLib.h"

#include "SE_Utils.h"
#include "SE_MemLeakDetector.h"


typedef void*(*SEFunc)(void); 

SE_DynamicLib::SE_DynamicLib(const char* libName) : mLibName(libName)
{
    mSo = NULL;
}

SE_DynamicLib::~SE_DynamicLib()
{
    unloadLib();
}


SE_Plugin* SE_DynamicLib::getPlugin()
{

    SEFunc createFunc; //function pointor

    //SE_Plugin* a = (SE_Plugin*)SE_ObjectFactory::create("my_plugin");
    LOGI("start find key function\n\n");
    createFunc = (SEFunc)dlsym(mSo, "se_plugin_create");
    
    if (!createFunc)
    {
        LOGI("key function not found!!\n\n");
        return NULL;
    }

    LOGI("key function found!!!!\n\n");
    SE_Plugin* p = (SE_Plugin*)createFunc();

    if(!p)
    {
        return NULL;
    }

    LOGI("dll compute result = %s\n", p->getPluginName());

    std::string className = p->getPluginName();    

    return p;
}

bool SE_DynamicLib::loadLib()
{ 

    mSo = dlopen(mLibName.c_str(),RTLD_LAZY);

    if(!mSo)
    {
        LOGI("%s load fail...\n\n",mLibName.c_str());
        return false;
    }

    LOGI("%s load success!!!\n\n",mLibName.c_str());
    return true;
}

bool SE_DynamicLib::unloadLib()
{
    if(mSo)
    {
    dlclose(mSo);
    }
    return true;
}

const char* SE_DynamicLib::getLibName()
{
    return mLibName.c_str();
}
