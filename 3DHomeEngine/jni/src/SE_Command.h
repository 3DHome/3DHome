#ifndef SE_COMMAND_H
#define SE_COMMAND_H
#include "SE_ID.h"
#include "SE_Time.h"
class SE_Application;
class SE_ENTRY SE_CommandExecuteCondition
{
public:
	virtual ~SE_CommandExecuteCondition() {}
	virtual bool isFulfilled() = 0;
};
class SE_ENTRY SE_Command
{
public:
    enum TIME_TYPE {REAL, SIMULATE};
    SE_Command(SE_Application* app, SE_TimeMS waitTime = 0, TIME_TYPE timeType = REAL) : mApp(app), mWaitTime(waitTime), mTimeType(timeType)
    {
		mCondition = NULL;
    }
    virtual ~SE_Command();
    virtual void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta) = 0;
    virtual bool expire(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
	void setCondition(SE_CommandExecuteCondition* c)
	{
		if(mCondition)
			delete mCondition;
		mCondition = c;
	}
	bool isFulfilled()
	{
		if(!mCondition)
			return true;
		return mCondition->isFulfilled();
	}
protected:
    SE_Application* mApp;
    SE_TimeMS mWaitTime;
    TIME_TYPE mTimeType;
    SE_CommandExecuteCondition* mCondition;
};
#endif
