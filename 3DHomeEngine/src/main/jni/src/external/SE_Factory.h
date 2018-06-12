#ifndef SE_FACTORY_H
#define SE_FACTORY_H

#include <map>
#include <string>
#include "SE_DynamicLibType.h"
class SE_Object;
class SE_CreateObjectFunc
{
public:
    virtual ~SE_CreateObjectFunc() {}
    virtual void* create() = 0;
};

class SE_ENTRY SE_ObjectFactory
{
public:
    virtual ~SE_ObjectFactory();
    static void* create(const char* className);

    static void reg(const char* className, SE_CreateObjectFunc* ocFunc);
    static void unreg(const char* className);
    static void factoryRelease();

    struct ObjProperty
    {
        //ObjProperty();
        bool init;
    };

private:
    static ObjProperty mObjProp;

    typedef std::map<std::string, SE_CreateObjectFunc*> ObjectCreateMap;
    static ObjectCreateMap* mObjectCreateMap; 
};


#define DECLARE_OBJECT(className) \
    class className##CreateFunc : public SE_CreateObjectFunc \
    { \
    public: \
        void* create(); \
    }; \
    class className##Reg \
    { \
    public: \
        className##Reg(); \
    }; \
    static className##Reg objReg;
#define IMPLEMENT_OBJECT(className) \
    void* className::className##CreateFunc::create() \
    { \
        return new className; \
    } \
    className::className##Reg::className##Reg() \
    { \
        className::className##CreateFunc* objcreateFunc = new className::className##CreateFunc; \
        SE_ObjectFactory::reg(#className, objcreateFunc); \
    } \
    className::className##Reg className::objReg; 
#endif
