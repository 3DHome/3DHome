#include "SE_DynamicLibType.h"
#include "SE_ThreadManager.h"
#include <list>
#include "SE_MemLeakDetector.h"
SE_ThreadID SE_ThreadManager::add(SE_Thread* thread)
{
    return mThreadManager.add(SE_ThreadID::NULLID, thread);
}
SE_Thread* SE_ThreadManager::remove(SE_ThreadID id)
{
    return mThreadManager.remove(id);
}
void SE_ThreadManager::release(SE_Thread* thread, int delay)
{
    mThreadManager.release(thread, delay);
}
class FindThreadByName
{
public:
    void operator()(SE_Thread* t)
    {
        if(t->getName() == name)
        {
            output->push_back(t);
        }
    }
    std::string name;
    std::list<SE_Thread*>* output;
};
std::vector<SE_Thread*> SE_ThreadManager::find(const char* name)
{
    FindThreadByName f;
    f.name = name;
    f.output = new std::list<SE_Thread*>;
    mThreadManager.traverseList(f);
    std::vector<SE_Thread*> ret(f.output->size());
    std::copy(f.output->begin(), f.output->end(), ret.begin());
    return ret;
}