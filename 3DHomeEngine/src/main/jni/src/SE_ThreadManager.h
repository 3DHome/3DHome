#ifndef SE_THREADMANAGER_H
#define SE_THREADMANAGER_H
#include "SE_Common.h"
#include "SE_TreeStruct.h"
#include "SE_TreeStructManager.h"
#include "SE_Thread.h"
#include <vector>
class SE_Thread;
class SE_ENTRY SE_ThreadManager
{
public:
    SE_ThreadID add(SE_Thread* thread);
    SE_Thread* remove(SE_ThreadID id);
    void release(SE_Thread* thread, int delay = SE_RELEASE_DELAY);
    std::vector<SE_Thread*> find(const char* name);
private:
    SE_TreeStructManager<SE_Thread> mThreadManager;
};
#endif
