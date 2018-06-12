#include "SE_DynamicLibType.h"
#include "SE_Thread.h"
#include "SE_Application.h"
#include "SE_ThreadManager.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
class _DeleteThreadCommand : public SE_Command
{
public:
    _DeleteThreadCommand(SE_Application* app) : SE_Command(app)
    {}
    SE_ThreadID threadid;
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
    {
        SE_ThreadManager* threadManager = mApp->getThreadManager();
        SE_Thread* thread = threadManager->remove(threadid);
        threadManager->release(thread, SE_RELEASE_NO_DELAY);
    }
};
SE_Thread::SE_Thread(bool deleteAfterEnd, const std::string& name) : mIsDeleteAfterEnd(deleteAfterEnd)
{
    mIsThreadRunEnd = false;
    mName = name;
}
SE_Thread::~SE_Thread()
{
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("##### thread deleted ######\n");
}
void SE_Thread::threadLoop()
{}
pthread_t SE_Thread::currentThread()
{
    return pthread_self();
}
bool SE_Thread::isEqual(pthread_t left, pthread_t right)
{
    return pthread_equal(left, right);
}
void SE_Thread::createThread(THREADFUNC f, void* args)
{
    SE_Thread* thread = static_cast<SE_Thread*>(args);
    pthread_create(&thread->mID, NULL, f, args); 
}
void* SE_Thread::threadFun(void * args)
{
    SE_Thread* thread = static_cast<SE_Thread*>(args);
    thread->run();
    thread->threadLoop();
    thread->mEndStateMutex.lock();
    thread->mIsThreadRunEnd = true;
    thread->mEndStateMutex.unlock();
    if(thread->mIsDeleteAfterEnd)
    {
        _DeleteThreadCommand* t = new _DeleteThreadCommand(SE_Application::getInstance());
        t->threadid = thread->getID();
        SE_Application::getInstance()->postCommand(t);
    }
    return NULL;
}
class _ThreadCommand : public SE_Command
{
public:
    SE_Thread* thread;
    bool deleteWhenEnd;
    _ThreadCommand(SE_Application* app) : SE_Command(app)
    {}
    ~_ThreadCommand()
    {
        if(deleteWhenEnd)
        {
            SE_ThreadManager* threadManager = mApp->getThreadManager();
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("@@@@ thread = %p @@@@@\n", thread);
            SE_Thread* tmpThread = threadManager->remove(thread->getID());
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("### release tmp thread =%p ###\n", tmpThread);
            threadManager->release(tmpThread, SE_RELEASE_NO_DELAY);
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("### release thread end ###\n");
        }
    }
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("### thread command run %p #####\n", thread);
        thread->run();
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("### thread command run end #####\n");
    }
};
void SE_Thread::start()
{
#if defined(SE_HAS_THREAD)
    LOGI("############## create thread ############\n");
    createThread(&SE_Thread::threadFun, this);
#else
    _ThreadCommand* threadCommand = new _ThreadCommand(SE_Application::getInstance());
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### thread this = %p ####\n", this);
    threadCommand->thread = this;
    threadCommand->deleteWhenEnd = mIsDeleteAfterEnd;
    SE_Application::getInstance()->postCommand(threadCommand);
#endif
}
bool SE_Thread::isEnd()
{
    bool end;
    mEndStateMutex.lock();
    end = mIsThreadRunEnd;
    mEndStateMutex.unlock();
    return end;
}
void SE_Thread::reset()
{
    mEndStateMutex.lock();
    mIsThreadRunEnd = false;
    mEndStateMutex.unlock();

}
///////////////////////
SE_QueueThread::SE_QueueThread()
{
    mCanAddCommand = true;
    mExit = false;
    mCommandQueueCondition.setMutex(&mCommandQueueMutex);
}
bool SE_QueueThread::postCommand(SE_Command* c)
{
    if(!canAddCommand())
        return false;
    mCommandQueueMutex.lock();
    mCommandQueue.push_back(c);
    mCommandQueueCondition.signal();
    mCommandQueueMutex.unlock();
    return true;
}
void SE_QueueThread::run()
{
    mTimer.start();
    while(!mExit)
    {
        SE_Timer::TimePair pt = mTimer.step();
        process(pt.realTimeDelta, pt.simulateTimeDelta);
    }
}
void SE_QueueThread::setExit(bool e)
{
    SE_AutoMutex m(&mExitMutex);
    mExit = e;
}
bool SE_QueueThread::isExit()
{
    SE_AutoMutex m(&mExitMutex);
    return mExit;
}
SE_QueueThread::~SE_QueueThread()
{
    SE_AutoMutex m(&mCommandQueueMutex);
    std::list<SE_Command*>::iterator it;
    for(it = mCommandQueue.begin();  it != mCommandQueue.end() ; it++)
    {
        delete *it;
    }
}
void SE_QueueThread::process(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    std::list<SE_Command*>::iterator it;
    std::list<SE_Command*> tmpList;
    mCommandQueueMutex.lock();
    if(mCommandQueue.empty())
    {
        mCommandQueueCondition.wait();
    }
    tmpList = mCommandQueue;
    mCommandQueue.clear();
      mCommandQueueMutex.unlock();
    for(it = tmpList.begin(); it != tmpList.end(); )
    {
        SE_Command* c = *it;
        if(c->expire(realDelta, simulateDelta))
        {
            c->handle(realDelta, simulateDelta);
            delete c;
            tmpList.erase(it++);
        }
        else
        {
            it++;
        }
    }
    if(tmpList.empty())
        return;
    mCommandQueueMutex.lock();
    mCommandQueue.splice(mCommandQueue.begin(), tmpList, tmpList.begin(), tmpList.end()); 
    mCommandQueueMutex.unlock();
}
void SE_QueueThread::setCanAddCommand(bool b)
{
    SE_AutoMutex m(&mCanAddCommandMutex);
    mCanAddCommand = b;
}
bool SE_QueueThread::canAddCommand()
{
    SE_AutoMutex m(&mCanAddCommandMutex);
    return mCanAddCommand;
}
