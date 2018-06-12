#ifndef SE_PLUGINSMANAGER_H
#define SE_PLUGINSMANAGER_H


class SE_Plugin;

class SE_ENTRY SE_PluginsManager
{
public:
    SE_PluginsManager();
    ~SE_PluginsManager();

    SE_Plugin* loadPlugin(const char* libName);
    bool unLoadPlugin(const char* libName);

private:
    typedef std::map<std::string, SE_Plugin*> PluginsMap;
    PluginsMap mLoadedPlugins;

private:
    bool pluginHasLoaded(const char* libName);    


};
#endif