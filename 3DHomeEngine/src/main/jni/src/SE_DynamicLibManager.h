#ifndef SE_DYNAMICLIBMANAGER_H
#define SE_DYNAMICLIBMANAGER_H
class SE_DynamicLib;
class SE_Plugin;

class SE_ENTRY SE_DynamicLibManager
{
public:
    SE_DynamicLibManager();
    ~SE_DynamicLibManager();
    
    SE_Plugin* loadPluginFromLib(const char* libName);
    void unloadLib(const char* libName);
    

private:
    SE_DynamicLib* loadDynamicLib(const char* libName);


private:
    typedef std::map<std::string,SE_DynamicLib*> LibMap;
    LibMap mLoadedLibMap;

    static SE_DynamicLibManager* mInstance;

};
#endif