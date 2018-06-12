#ifndef SE_DYNAMICLIB_H
#define SE_DYNAMICLIB_H


class SE_Plugin;
class SE_ENTRY SE_DynamicLib
{
public:
    SE_DynamicLib(const char* libName);
    ~SE_DynamicLib();
    const char* getLibName();
    bool loadLib();
    bool unloadLib();
    SE_Plugin* getPlugin();

private:
    std::string mLibName;

#if defined(WIN32)
    HINSTANCE mDll;
#else
    void* mSo;
#endif

};
#endif