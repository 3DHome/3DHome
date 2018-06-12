#include "SE_AssetManager.h"
#include <stdio.h>
#include "SE_Log.h"
#include <string>
#include <string.h>
#include "unzip.h"
#include "SE_DynamicLibType.h"
#include "SE_IO.h"

SE_AssetManager::SE_AssetManager() {

}

void SE_AssetManager::addAssetPath(const char* assetPath)
{
    std::string stringPath = assetPath;
    std::list<std::string>::iterator it = mAssetPaths.begin();
    for(; it != mAssetPaths.end() ; it++)
    {
       if (stringPath == *it)
       {
           return;
       }
    }
    mAssetPaths.push_back(stringPath);
}

void SE_AssetManager::rmAssetPath(const char* assetPath)
{
    std::string stringPath = assetPath;
    mAssetPaths.remove(stringPath);
}

void SE_AssetManager::readAsset(const char* fileName, char*& outData, int& outLen)
{
 if(_DEBUG)
    LOGD("SE_AssertManager : readAsset file name = '%s'\n", fileName);

    outData = NULL;
    outLen = 0;
    std::list<std::string>::iterator it = mAssetPaths.begin();    
    for(; it != mAssetPaths.end() ; it++)
    {
        std::string stringPath = *it;
        unzFile uf = NULL;
        uf = unzOpen(stringPath.c_str());
        if (uf != NULL)
        {
            if (unzLocateFile(uf,fileName,1) == UNZ_OK)
            {
 if(_DEBUG)
                LOGD("SE_AssertManager : find file at '%s'\n", stringPath.c_str());

                unz_file_info file_info;
                if (unzGetCurrentFileInfo(uf,&file_info,NULL,0,NULL,0,NULL,0) == UNZ_OK)
                {

                    if (unzOpenCurrentFile(uf) == UNZ_OK)
                    {
                        outLen = file_info.uncompressed_size;
                        outData = new char[outLen];
                        if(unzReadCurrentFile(uf, outData, outLen) == outLen)
                        {
 if(_DEBUG)
                             LOGD("SE_AssertManager : readAsset '%s' success\n", fileName);

                        }
                        else
                        {
 if(_DEBUG)
                             LOGD("SE_AssertManager : readAsset '%s' failes\n", fileName);

                         }                         
                     }
                     unzCloseCurrentFile(uf);
                 }
            }
            unzClose(uf);
        }
        if (outData != NULL)
        {
             return;
        }
        std::string file = stringPath + fileName;
        SE_IO::readFileAll(file.c_str(), outData, outLen);
        if (outData != NULL)
        {
 if(_DEBUG) {
             LOGD("SE_AssertManager : find file at '%s'\n", stringPath.c_str());
             LOGD("SE_AssertManager : readAsset '%s' success\n", fileName);
}
             return;
        }
    }
 if(_DEBUG)
    LOGD("SE_AssertManager : can not find file '%s' at any where", fileName);

}
