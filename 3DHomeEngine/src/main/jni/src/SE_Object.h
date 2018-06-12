#ifndef SE_OBJECT_H
#define SE_OBJECT_H
#include <map>
#include <string>
#include "SE_Factory.h"
class SE_Object;
class SE_BufferInput;
class SE_BufferOutput;
class SE_KeyEvent;
class SE_MotionEvent;
/*
class SE_ObjectCreateFunc
{
public:
    virtual ~SE_ObjectCreateFunc() {}
    virtual SE_Object* create() = 0;
};
*/
class SE_ENTRY SE_Object
{
public:
    SE_Object();
    virtual ~SE_Object();
    virtual void read(SE_BufferInput& input);
    virtual void write(SE_BufferOutput& output);
    virtual void onKeyEvent(SE_KeyEvent* keyEvent);
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
    //static SE_Object* create(const char* className);
    //static void reg(const char* className, SE_ObjectCreateFunc* ocFunc);
    //static void unreg(const char* className);

    //static void factoryRelease();
protected:
    /*
    struct ObjProperty
    {
        //ObjProperty();
        bool init;
    };
    */
    //typedef std::map<std::string, SE_ObjectCreateFunc*> ObjectCreateMap;
    //static ObjectCreateMap* mObjectCreateMap; 
    //static ObjProperty mObjProp;
};
/*
#define DECLARE_OBJECT(className) \
    class className##CreateFunc : public SE_ObjectCreateFunc \
    { \
    public: \
        SE_Object* create(); \
    }; \
    class className##Reg \
    { \
    public: \
        className##Reg(); \
    }; \
    static className##Reg objReg;
#define IMPLEMENT_OBJECT(className) \
    SE_Object* className::className##CreateFunc::create() \
    { \
        return new className; \
    } \
    className::className##Reg::className##Reg() \
    { \
        className::className##CreateFunc* objcreateFunc = new className::className##CreateFunc; \
        SE_Object::reg(#className, objcreateFunc); \
    } \
    className::className##Reg className::objReg; 
    */
#endif
