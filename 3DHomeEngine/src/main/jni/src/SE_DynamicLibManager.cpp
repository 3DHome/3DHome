#include<map>
#include<string>
#include "SE_DynamicLibType.h"
#include "SE_Log.h"

#if defined(WIN32)
#include<windows.h>
#endif
#include "SE_DynamicLib.h"
#include "SE_DynamicLibManager.h"
#include "SE_Plugin.h"
#include "SE_Application.h"
#include "SE_MemLeakDetector.h"



SE_DynamicLibManager* SE_DynamicLibManager::mInstance = NULL;

SE_DynamicLibManager::SE_DynamicLibManager()
{
    
}

SE_DynamicLibManager::~SE_DynamicLibManager()
{
    LibMap::iterator it = mLoadedLibMap.begin();

    for(;it != mLoadedLibMap.end();)
    {
        SE_DynamicLib* dLib = it->second;
        delete dLib;
        mLoadedLibMap.erase(it++);
    }   
}

SE_DynamicLib* SE_DynamicLibManager::loadDynamicLib(const char* libName)
{
    LibMap::iterator it = mLoadedLibMap.find(std::string(libName));

    if(it != mLoadedLibMap.end())
    {
        return (*it).second;
    }

    //lib not load
    SE_DynamicLib* dLib = new SE_DynamicLib(libName);
    if(dLib->loadLib())
    {
        
        return dLib;
    }

    delete dLib;
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("Error on load lib!!!!\n\n");
    return NULL;    
}

void SE_DynamicLibManager::unloadLib(const char* libName)
{
    LibMap::iterator it = mLoadedLibMap.find(std::string(libName));

    if(it != mLoadedLibMap.end())
    {
        (*it).second->unloadLib();
    }
}

SE_Plugin* SE_DynamicLibManager::loadPluginFromLib(const char* libName)
{
    SE_DynamicLib* dLib = loadDynamicLib(libName);

    if(!dLib)
    {
        return NULL;
    }

    mLoadedLibMap.insert(std::pair<std::string,SE_DynamicLib*>(std::string(libName),dLib));

    SE_Plugin* p = dLib->getPlugin();

    if(!p)
    {
        unloadLib(libName);
    }

    return p;
}