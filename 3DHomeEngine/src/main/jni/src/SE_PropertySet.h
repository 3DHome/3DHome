#ifndef SE_PROPERTYSET_H
#define SE_PROPERTYSET_H
#include <map>
#include <string>
class SE_ENTRY SE_Data
{
public:
    virtual ~SE_Data()
    {}
};

class SE_ENTRY SE_PropertySet
{
public:
    SE_PropertySet();
    ~SE_PropertySet();
    char getChar(const char* name, char defaultValue = 0);
    unsigned char getUChar(const char* name, unsigned char defaultValue = 0);
    short getShort(const char* name, short defaultValue = 0);
    unsigned short getUShort(const char* name, unsigned short defaultValue = 0);
    int getInt(const char* name, int defaultValue = 0);
    unsigned int getUInt(const char* name, unsigned int defaultValue = 0);
    float getFloat(const char* name, float defaultValue = 0);
    const char* getString(const char* name, const char* defaultValue = NULL);
    const short* getUniString(const char* name, const short* defaultValue = NULL);
    SE_Data* getData(const char* name, SE_Data* defaultValue = NULL);
    void setChar(const char* name, char c);
    void setUChar(const char* name, unsigned char uc);
    void setShort(const char* name, short s);
    void setUShort(const char* name, unsigned short us);
    void setInt(const char* name, int i);
    void setUInt(const char* name, unsigned int ui);
    void setFloat(const char* name, float f);
    void setString(const char* name, const char* s, int len = 0);
    void setUniString(const char* name, const short* s16 , int len);
    void setData(const char* name, SE_Data* data);
private:
    SE_PropertySet(const SE_PropertySet&);
    SE_PropertySet& operator=(const SE_PropertySet&);
private:
    enum TYPE {CHAR, UCHAR, SHORT, USHORT, INT, UINT, FLOAT, STRING, UNISTRING, DATA};
    struct _Property
    {
        TYPE type;
        union
        {
            char c;
            unsigned char uc;
            short s;
            unsigned short us;
            int i;
            unsigned int ui;
            float f;
            char* str8;
            short* str16;
            SE_Data* data;
        } prop;
    };
    typedef std::map<std::string, _Property> PropertyMap;
    void clearContent(PropertyMap::iterator& it);
    void setContent(const char* name, const _Property& p);
private:
    PropertyMap mPropertyMap;
};
#endif
