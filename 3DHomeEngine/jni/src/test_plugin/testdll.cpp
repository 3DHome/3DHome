// testdll.cpp : Defines the exported functions for the DLL application.
//
#include<string>
#include<iostream>
#include "SE_DynamicLibType.h"

#include "SE_Factory.h"
#include "SE_Plugin.h"

#include "testdll.h"
#include "SE_ShaderDefine.h"
#include "SE_RenderDefine.h"


#include "my_renderdefine.h"
#include "my_shaderdefine.h"


my_plugin* my_plugin::mPlugin = NULL;

IMPLEMENT_OBJECT(my_plugin)
my_plugin::my_plugin()
{
    mPluginName = "my_plugin";
    mType = SE_Plugin::SE_SHADER_RENDER;
    mVersion = "0.01";
    mRenderdefine = new my_renderdefine();
    mShaderdefine = new my_shaderdefine();

}

my_plugin::~my_plugin()
{
    SE_ObjectFactory::unreg("my_plugin");

    if(mRenderdefine)
    {
        delete mRenderdefine;
    }

    if(mShaderdefine)
    {
        delete mShaderdefine;
    }
    
}

const char* my_plugin::getPluginName()
{
    return mPluginName.c_str();
}

const char* my_plugin::getPluginVersion()
{
    return mVersion.c_str();
}

int my_plugin::getPluginType()
{
    return mType;
}

void my_plugin::unLoadPlugin()
{
    if(mPlugin)
    {
        delete mPlugin;
    }
}

SE_RenderDefine* my_plugin::getPluginRenderDefine()
{
    return mRenderdefine;
}

SE_ShaderDefine* my_plugin::getPluginShaderDefine()
{
    return mShaderdefine;
}

extern "C" SE_ENTRY void* se_plugin_create()
{
    my_plugin::mPlugin = new my_plugin();
    return my_plugin::mPlugin;
}