#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_SimObject.h"
#include "SE_Time.h"
#include <map>
#include <string>
int main(int argc, char** argv)
{
    SE_SimObject* p = (SE_SimObject*)SE_Object::create("SE_SimObject");
    printf("##%p##\n", p);
    std::map<std::string, int> tMap;
    tMap["teeqerqdcgert"] = 1;
    tMap["sdffeqreadsff"] = 3;
    SE_TimeMS t0 = SE_Time::getCurrentTimeMS();
    std::map<std::string, int>::iterator it = tMap.find("teeqerqdcgert");
    SE_TimeMS t1 = SE_Time::getCurrentTimeMS();
    printf("## %d ##\n", t1 - t0);
    return 0;
}
