#ifndef SE_ASSETMANAGER_H
#define SE_ASSETMANAGER_H
#include <list>
#include <string>
class SE_AssetManager
{
public:
    virtual ~SE_AssetManager() {
    }
    SE_AssetManager();
    void readAsset(const char* fileName, char*& outData, int& outLen);
    void addAssetPath(const char* assetPath);
    void rmAssetPath(const char* assetPath);
private:
    std::list<std::string> mAssetPaths;  
};
#endif
