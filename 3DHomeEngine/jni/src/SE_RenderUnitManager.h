#ifndef SE_RENDERUNITMANAGER_H
#define SE_RENDERUNITMANAGER_H

class SE_RenderUnit;
class SE_ENTRY SE_RenderUnitManager
{
public:
    SE_RenderUnitManager();
    ~SE_RenderUnitManager();
    bool insert(const char* ruName,SE_RenderUnit* ru,int i);
    SE_RenderUnit* find(const char* ruName);
    void remove(const char* ruName);

private:
    SE_ObjectManager<std::string, SE_RenderUnit> mRenderUnitMap;
};
#endif