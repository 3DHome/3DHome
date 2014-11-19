#ifndef SE_INPUTMANAGER_H
#define SE_INPUTMANAGER_H
#include <list>
class SE_KeyEvent;
class SE_MotionEvent;
class SE_Object;
class SE_SceneManager;
class SE_ENTRY SE_InputManager
{
public:
    SE_InputManager();
    ~SE_InputManager();
    void update(SE_KeyEvent* keyEvent);
    void update(SE_MotionEvent* motionEvent);
    //void addKeyEventOberver(SE_Object* keyEventObserver);
    //void addMotionEventOberver(SE_Object* motionEventObserver);
    //SE_Object *getCurrentMotionEventObserve();
    //void removeKeyEventObserver(SE_Object* keyEventObserver = NULL);
    //void removeMotionEventObserver(SE_Object* motionEventObserver = NULL);
private:
    //typedef std::list<SE_Object*> KeyEventObserverList;
    //typedef std::list<SE_Object*> MotionEventObserverList;
    //std::list<SE_Object*> mKeyEventObserverList;
    //std::list<SE_Object*> mMotionEventObserverList;
    SE_SceneManager* mSceneManager;
};
#endif