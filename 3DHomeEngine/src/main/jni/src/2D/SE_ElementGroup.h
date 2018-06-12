#ifndef SE_ELEMENTGROUP_H
#define SE_ELEMENTGROUP_H
#include "SE_Element.h"
#include <list>
class SE_ENTRY SE_ElementGroup : public SE_Element
{
public:
    SE_ElementGroup();
    ~SE_ElementGroup();
    void addChild(SE_Element* e);
    void removeChild(SE_Element* e);
    void removeChild(const char* name);
public:
    virtual SE_Spatial* createSpatial(SE_Spatial* parent);
    virtual void travel(SE_ElementTravel* travel);
private:
    SE_ElementGroup(const SE_ElementGroup&);
    SE_ElementGroup& operator=(const SE_ElementGroup&);
private:
    typedef std::list<SE_Element*> _ElementList;
    _ElementList mChildren;
};
#endif
