#ifndef SE_THREAD_H
#define SE_THREAD_H
#include <pthread.h>
#include "SE_Common.h"
#include "SE_Mutex.h"
#include "SE_TreeStruct.h"
#include "SE_Timer.h"
#include <string>
class SE_Command;
typedef void* (*THREADFUNC)(void*);
class SE_ENTRY SE_Thread : public SE_ListStruct<SE_Thread>
{
    friend class _ThreadCommand;
public:
    enum PRIORITY {LOW, NORMAL, HIGHT};
    SE_Thread(bool deleteAfterEnd = true, const std::string& name = "");
    virtual ~SE_Thread();
    pthread_t id() const
    {
        return mID;
    }
    void setName(const std::string& name)
    {
        mName = name;
    }
    std::string getName() const
    {
        return mName;
    }
    void start();
    static pthread_t currentThread();
    static bool isEqual(pthread_t left, pthread_t right);
    bool isEnd();
    void reset();
protected:
    virtual void run() = 0;
    virtual void threadLoop();
private:
    static void createThread(THREADFUNC f, void* args);
    static void* threadFun(void *);
private:
    SE_DECLARE_NONECOPY(SE_Thread);
private:
    pthread_t mID;
    bool mIsThreadRunEnd;
    SE_Mutex mEndStateMutex;
    const bool mIsDeleteAfterEnd;
    std::string mName;
};
class SE_ENTRY SE_QueueThread : public SE_Thread
{
public:
    SE_QueueThread();
    ~SE_QueueThread();
    //if return false command is not add to queue. user need to delete c by himself
    bool postCommand(SE_Command* c);
    void setExit(bool e);
    bool isExit();
    void setCanAddCommand(bool b);
    bool canAddCommand();
protected:
    void run();
private:
    void process(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
private:
    std::list<SE_Command*> mCommandQueue;
    SE_Mutex mCommandQueueMutex;
    SE_MutexCondition mCommandQueueCondition;
    bool mCanAddCommand;
    SE_Mutex mCanAddCommandMutex;
    SE_Timer mTimer;
    bool mExit;
    SE_Mutex mExitMutex;
};
#endif
