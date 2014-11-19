#include "SE_Action.h"
struct _EqualTest
{
    bool operator()(const SE_ActionUnit* data)
    {
        if(data->getID() == id)
            return true;
        else
            return false;
    }
    SE_StringID id    
};
void SE_Action::removeActionUnit(const SE_StringID& auID)
{
    _EqualTest et;
    et.id = auID;
    mActionUnitList.remove_if(_EqualTest());
}
SE_ActionUnit* SE_Action::getActionUnit(const SE_StringID& auID)
{
    _ActionUnitList::iterator it = find_if(mActionUnitList.begin(), mActionUnitList.end() , _EqualTest());
    if(it != mActionUnitList.end())
        return *it;
    else
        return NULL;
}
