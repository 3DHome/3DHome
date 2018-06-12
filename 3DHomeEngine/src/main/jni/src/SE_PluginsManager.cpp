#include "SE_DynamicLibType.h"
#include<map>
#include<string>
#include "SE_Log.h"
#include "SE_Plugin.h"
#include "SE_DynamicLibManager.h"
#include "SE_PluginsManager.h"
#include "SE_Application.h"
#include "SE_MemLeakDetector.h"


SE_PluginsManager::SE_PluginsManager()
{

}

SE_PluginsManager::~SE_PluginsManager()
{
    PluginsMap::iterator it = mLoadedPlugins.begin();

    for(;it != mLoadedPlugins.end();)
    {
        SE_Plugin* p = it->second;
        p->unLoadPlugin();
        mLoadedPlugins.erase(it++);
    }
}

bool SE_PluginsManager::pluginHasLoaded(const char* libName)
{
    std::string fn = libName;
    PluginsMap::iterator it = mLoadedPlugins.find(fn);

    if(it != mLoadedPlugins.end())
    {
        //plugin has been loaded,return
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("Dynamic Lib has been loaded!!!\n");
        return true;
    }
    return false;
}


SE_Plugin* SE_PluginsManager::loadPlugin(const char *libName)
{
    if(pluginHasLoaded(libName))
    {
        //the plugin has been loaded
        return false;
    }

    SE_Plugin* p = SE_Application::getInstance()->getDynamicManager()->loadPluginFromLib(libName);    

    if(!p)
    {
        return NULL;
    }

    mLoadedPlugins.insert(std::pair<std::string,SE_Plugin*>(std::string(libName),p));
    return p;
}

bool SE_PluginsManager::unLoadPlugin(const char *libName)
{
    if(!pluginHasLoaded(libName))
    {
        //the plugin has not been loaded
        return false;
    }

    SE_Application::getInstance()->getDynamicManager()->unloadLib(libName);
    
    return true;

}