#include "SE_DynamicLibType.h"
#include "SE_Animation.h"
#include "SE_Interpolate.h"
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_Message.h"
#include "SE_MessageDefine.h"
#include "SE_AnimationListener.h"
#include "tinyxml.h"
#include "SE_MemLeakDetector.h"
#include "SE_Application.h"

SE_Animation::SE_Animation()
{
    mAnimState = END;
    mRunMode = NOT_REPEAT;
    mTimeMode = SIMULATE;
    mDuration = 0;
    mPassedTime = 0;
    mInterpolate = NULL;
    mCurrFrame = -1;
    mFrameNum = 0;
    mTimePerFrame = 0;
    mSpeed = 1.0;
    mSystemFrameRate = 30;

    mBackupFrameNum = 0;
    mPlayMode = CPU_NO_SHADER;
}
SE_Animation::~SE_Animation()
{
    if(mInterpolate)
    {
        delete mInterpolate;
}
    for(int i = 0; i < mListeners.size(); ++i)
    {
        delete mListeners[i];
    }
    mListeners.clear();
}
void SE_Animation::run(int cloneIndex)
{
    if(mAnimState == END)
    {
        mAnimState = RUNNING;
        onRun(cloneIndex);
    }
}
void SE_Animation::restore()
{
    if(mAnimState == PAUSE)
    {
        mAnimState = RUNNING;
        onRestore();
    }
}
void SE_Animation::pause()
{
    if(mAnimState == RUNNING)
    {
        mAnimState = PAUSE;
        onPause();
    }
}
void SE_Animation::end()
{
    if(mAnimState != END)
    {
        mAnimState = END;
        onEnd();
        mPassedTime = 0;
    }
}
void SE_Animation::nextFrame(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    if(mRunMode == ONE_FRAME || mRunMode == REVERSE_ONE_FRAME)
    {
        oneFrame(realDelta, simulateDelta);
    }
}
void SE_Animation::oneFrame(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    if(mPassedTime >= mDuration)
    {
        if(mRunMode == NOT_REPEAT || mRunMode == REVERSE_NOT_REPEAT)
        {
            end();
            return;
        }
        else
        {
            mPassedTime -= mDuration;
        }
    }

    

    float percent = SE_Time::div(mPassedTime, mDuration);
    if(mInterpolate)
    {
        percent = mInterpolate->calc(percent);
    }

    SE_TimeMS passedTime = getPassedTime();
    if(SE_Application::getInstance()->SEHomeDebug)
    {
    LOGI("## passedTime = %d ##\n", passedTime);
    }
    int frame = mCurrFrame;

    if(mRunMode != ONE_FRAME && mRunMode != REVERSE_ONE_FRAME)
    {
        if(mRunMode == REPEAT || mRunMode == NOT_REPEAT)
        {
            frame = passedTime / mTimePerFrame;
        }
        else if(mRunMode == REVERSE_REPEAT || mRunMode == REVERSE_NOT_REPEAT)
        {
            frame = (mDuration - passedTime - simulateDelta) /  mTimePerFrame;
        }
    }
    else
    {
        if(mRunMode == ONE_FRAME)
        {
            frame++;
            if(frame >= mFrameNum)
                frame = 0;
        }
        else
        {
            frame--;
            if(frame < 0)
                frame = mFrameNum - 1;
        }
    }

    mCurrFrame = frame + 1;//animation no 0 frame, so +1

    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onBeforePerFrame(this);
    }
    onUpdate(realDelta, simulateDelta, percent, frame,mPlayMode);

    if(mTimeMode == REAL)
    {
        mPassedTime += realDelta;
    }
    else
    {
        mPassedTime += simulateDelta;
}

    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onAfterPerFrame(this);
    }
}
void SE_Animation::update(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{

    if(mRunMode == ONE_FRAME || mRunMode == REVERSE_ONE_FRAME)
    {       
        return;
    }

    if(mAnimState != RUNNING)
        return;

    oneFrame(realDelta, simulateDelta);
    
}
void SE_Animation::onRun(int index)
{
    mBackupFrameNum = mFrameNum;//save framenum
    SE_Application::getInstance()->setFrameRate(mSystemFrameRate);    
    mDuration = mSystemFrameRate * mFrameNum;
    mTimePerFrame = mSystemFrameRate;
    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onPrepare(this,index);
    }
}
void SE_Animation::onPause()
{
    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onPause(this);
    }
}
void SE_Animation::onEnd()
{
    

    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onEnd(this);
    }
    resetAnimation();
}
void SE_Animation::onRestore()
{
    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->onRestore(this);
    }
}
void SE_Animation::onUpdate(SE_TimeMS realDelta, SE_TimeMS simulateDelta, float percent, int frameIndex,PLAY_MODE playmode)
{
    for(int i = 0; i < mListeners.size(); ++i)
    {
        mListeners[i]->resteFrame();
        mListeners[i]->onUpdateFrame(this);
    }
}
SE_Animation* SE_Animation::clone()
{
    return NULL;
}

SE_Animation* SE_Animation::inflate(const char *xmlPath)
{

    std::string path = xmlPath;
    size_t pos = path.rfind('/');
    path = path.substr(0,pos + 1);
#ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    if (!data || len == 0) {
    	 return NULL;
    }
    TiXmlDocument doc;
    bool ok = doc.LoadFile(data,len);
    delete data;
#else    
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif
    if(!ok)
    {
        return NULL;
    }
    LOGI("\n %s: \n",xmlPath);   

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem;
    TiXmlHandle hRoot(0);
    
    pElem = hDoc.FirstChildElement("animation").Element();

    if(!pElem)
    {
        LOGI("animation not found!!\n");
        return NULL;
    }

    std::string id;
    pElem->QueryStringAttribute("id",&id);


    int runMode = -1;
    pElem = pElem->FirstChildElement("run_mode");

    if(!pElem)
    {
        LOGI("run_mode not found!!\n");
        return NULL;
    }
    pElem->QueryIntAttribute("mode",&runMode);


    int timeMode = -1;
    pElem = pElem->NextSiblingElement("time_mode");

    if(!pElem)
    {
        LOGI("time_mode not found!!\n");
        return NULL;
    }
    pElem->QueryIntAttribute("mode",&timeMode);


    int timePerFrame = -1;
    pElem = pElem->NextSiblingElement("time_per_frame");

    if(!pElem)
    {
        LOGI("time_per_frame not found!!\n");
        return NULL;
    }
    pElem->QueryIntAttribute("tpf",&timePerFrame);



    int frameNumber = -1;
    pElem = pElem->NextSiblingElement("frame_number");

    if(!pElem)
    {
        LOGI("frame_number not found!!\n");
        return NULL;
    }
    pElem->QueryIntAttribute("fn",&frameNumber);


    int duration = -1;
    pElem = pElem->NextSiblingElement("duration");

    if(!pElem)
    {
        LOGI("duration not found!!\n");
        return NULL;
    }
    pElem->QueryIntAttribute("d",&duration);

    float speed = 1.0;
    pElem = pElem->NextSiblingElement("speed");

    if(!pElem)
    {
        LOGI("speed not found!!\n");
        return NULL;
    }
    pElem->QueryFloatAttribute("s",&speed);
    if(speed == 0.0)
    {
        speed = 1.0;
    }

    float defaultFR = 30.0;
    pElem = pElem->NextSiblingElement("system_frame_rate");

    if(!pElem)
    {
        LOGI("system_frame_rate not found!!\n");
        return NULL;
    }
    pElem->QueryFloatAttribute("rate",&defaultFR);
    

    std::string listenerName;
    pElem = pElem->NextSiblingElement("listener");

    if(!pElem)
    {
        LOGI("listener not found!!\n");
        return NULL;
    }
    listenerName = pElem->GetText();

    listenerName = path + listenerName;

    std::vector<SE_AnimationListener*> listeners;
    listeners = SE_AnimationListenerFactory::createListener(listenerName.c_str());
    if(listeners.size() == 0)
    {      
        LOGI("animation listener create fail!!\n");
        return NULL;
    }

    SE_Animation* a = new SE_Animation();
    a->setRunMode((SE_Animation::RUN_MODE)runMode);
    a->setTimeMode((SE_Animation::TIME_MODE)timeMode); 
    a->setSpeed(1.0/speed);
    a->setTimePerFrame(timePerFrame);
    a->setFrameNum(frameNumber);
    a->setDuration(duration);
    a->setAnimationID(id.c_str());

    a->setRequiredSystemFrameRate(defaultFR);

    for(int i = 0; i < listeners.size(); ++i)
    {
        SE_AnimationListener* l = listeners[i];
    a->addListener(l);
    }
    return a;
}
void SE_Animation::playFrameIndex(int frameIndex)
{
    //frameIndex from 0 - frameNum-1
    SE_TimeMS simulateDelta = frameIndex * mSystemFrameRate;

    if(frameIndex > mFrameNum)
    {
        return;
    }

    if(mCurrFrame != frameIndex + 1)
    {
        mPassedTime = simulateDelta;   
        this->oneFrame(0,mSystemFrameRate);
    }
}
void SE_Animation::setReversePlay(bool reverse)
{
    if(mAnimState == RUNNING)
    {
        SE_Animation::RUN_MODE currentMode = this->getRunMode();

        bool currentIsReverse = (currentMode == SE_Animation::REVERSE_NOT_REPEAT) ? true : false;

        if(currentIsReverse)
        {
            if(!reverse)
            {
                this->setRunMode(SE_Animation::NOT_REPEAT);
                int newFrameNum = mBackupFrameNum - getCurrentFrame();
                this->setFrameNum(newFrameNum);
                this->setDuration(mTimePerFrame * mFrameNum);

                //change end status to current status
    for(int i = 0; i < mListeners.size(); ++i)
    {
        SE_AnimationListener* l = mListeners[i];
                    l->changeStatus(true);
                    l->saveInit();
    }
    
            }

        }
        else
        {
            if(reverse)
            {            
                this->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
                int newFrameNum = getCurrentFrame();
                this->setFrameNum(newFrameNum);
                this->setDuration(mTimePerFrame * mFrameNum);

                //change start status to current status
                for(int i = 0; i < mListeners.size(); ++i)
                {
                    SE_AnimationListener* l = mListeners[i];
                    l->changeStatus(false);
                    l->saveInit();
                }
            }
        }
    }
}

void SE_Animation::resetAnimation()
{
    mFrameNum = mBackupFrameNum;//restore frame num

    SE_Animation::RUN_MODE currentMode = this->getRunMode();
    bool currentIsReverse = (currentMode == SE_Animation::REVERSE_NOT_REPEAT) ? true : false;

    if(currentIsReverse)
    {
        mCurrFrame = 0;        
    }
    else
    {
        mCurrFrame = mFrameNum;
    }
    mPassedTime = 0;

    this->setRunMode(SE_Animation::NOT_REPEAT);
}