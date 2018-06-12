#ifndef SE_UTILS_H
#define SE_UTILS_H
#include <list>
#include <string>
#include <wchar.h>
#include <vector>
template <typename T>
typename std::list<T>::iterator listElementRef(std::list<T>& data, int index)
{
    typename std::list<T>::iterator it = data.begin();
    int i = 0;
    while(it != data.end() && i < index)
        it++;
    return it;
}
struct SE_DeleteObject
{
    template <typename T>
    void operator()(T* ptr) const
    {
        delete ptr;
    }
};
struct SE_SignColor
{
    enum {SIGN_NO, SIGN_PLUS, SIGN_MINUS};
    struct 
    {
        int sign;
        int value;
    } data[3];
};
struct SE_ExtractImageStr
{
    std::string base;
    std::string red;
    std::string green;
    std::string blue;
    std::string alpha;
    int getImageNum()
    {
        int num = 0;
        std::string* pStr[5] = {&base, &red, &green, &blue, &alpha};
        for(int i = 0 ; i < 5 ; i++)
        {
            if(!pStr[i]->empty())
                num++;
        }
        return num;
    }
};
class SE_ENTRY SE_Util
{
public:
    static unsigned int host2NetInt32(unsigned int i);
    static unsigned short host2NetInt16(unsigned short s);
    static unsigned int net2HostInt32(unsigned int i);
    static unsigned short net2HostInt16(unsigned short s);
    static void sleep(unsigned int s);
    static int findHighBit(int v);
    static int higherPower2(int v);
    static int lowerPower2(int v);
    static bool isPower2(int v);
    static wchar_t* utf8ToUnicode(const char* utf8str);
    typedef std::vector<std::string>  SplitStringList;
    //splitstring will split string which has .xml sufix from other string
    //for example: "xxx/yyy.xml/zzz" --> "xxx/yyy.xml" "zzz"
    static SplitStringList splitString(const char* path, const char* split);
    //splitStringRaw will split string from each other whether it contain .xml or not
    //for example: "xxx/yyy.xml/zzz" --> "xxx" "yyy.xml" "zzz"
    static SplitStringList splitStringRaw(const char* path, const char* split);
    static std::string trimLeft(const char* str);
    static std::string trimRight(const char* str);
    static std::string trim(const char* str);
    static std::string stringReplace(std::string& src, const std::string& beReplacedStr, const std::string& replaceStr);
    static SE_SignColor stringToSignColor(const std::string& str); 
    static SE_ExtractImageStr stringToExtractImage(const std::string& url);
    static bool isDigit(const char* str);
    //return is a ascii string
    static std::string intToString(int i);
    static std::string floatToString(float f);
    //float is delimited by whitespace or tab
    //user need to delete[] out
    static void stringToFloatArray(const char* v, float*& out, int& outLen);
    static void stringToIntArray(const char* v, int*& out, int& len);
    static int stringToInt(const char* v);
    static float stringToFloat(const char* v);
    static size_t getUtf32LenFromUtf8(const char* src, size_t src_len); 
    static size_t utf8ToUtf32(const char* src, size_t src_len,
                       unsigned int* dst, size_t dst_len);

    static void toLowerCase(std::string& str);

    static bool replace_char(std::string& s,const char* src,const char* dest);

#if defined(WIN32)

    static void multiByteToWideChar(const char*multiByte,wchar_t *wideChar,int wideCharSize);
    static void wildCharToMultiByte(wchar_t* wideChar,char *multiByte,int multiByteSize);

#endif
};

#endif /** end SE_UTILS_H*/
