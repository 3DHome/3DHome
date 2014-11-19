#include "SE_DynamicLibType.h"
#include<string>
#include <stdio.h>
#include<windows.h>
#include "SE_Log.h"
#include "SE_Plugin.h"
#include "SE_DynamicLibType.h"
#include "SE_Application.h"
#include "SE_DynamicLib.h"
#include "SE_Factory.h"
#include "SE_Utils.h"
#include "SE_MemLeakDetector.h"
typedef void*(*SEFunc)(void); //宏定义函数指针类型

SE_DynamicLib::SE_DynamicLib(const char* libName) : mLibName(libName)
{}

SE_DynamicLib::~SE_DynamicLib()
{
    unloadLib();
}


SE_Plugin* SE_DynamicLib::getPlugin()
{
    SEFunc createFunc; //函数指针    

    //SE_Plugin* a = (SE_Plugin*)SE_ObjectFactory::create("my_plugin");
    createFunc = (SEFunc)GetProcAddress(mDll, "se_plugin_create");

    if (!createFunc)
    {
        return NULL;
    }

    SE_Plugin* p = (SE_Plugin*)createFunc();

    if(!p)
    {
        return NULL;
    }

    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("dll compute result = %s\n", p->getPluginName());

    std::string className = p->getPluginName();    

    return p;
}

bool SE_DynamicLib::loadLib()
{
#ifdef _UNICODE
    TCHAR fileWideChar[512];

    //memset(fileWideChar, 0, sizeof(wchar_t) * 512);

    //MultiByteToWideChar(CP_ACP, 0, mLibName.c_str(), -1, fileWideChar, 511); 

    SE_Util::multiByteToWideChar(mLibName.c_str(),fileWideChar,512);
    mDll = LoadLibrary(fileWideChar);
#else
    mDll = LoadLibrary(mLibName.c_str());
#endif

    if(!mDll)
    {
        return false;
    }

    return true;
}

bool SE_DynamicLib::unloadLib()
{
    FreeLibrary(mDll);
    return true;
}

const char* SE_DynamicLib::getLibName()
{
    return mLibName.c_str();
}