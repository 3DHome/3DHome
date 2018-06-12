#ifndef SE_MESSAGE_H
#define SE_MESSAGE_H
#include "SE_Struct.h"
struct SE_Message
{
    SE_Struct* data;
    int type;
    SE_Message()
    {
        data = NULL;
        type = 0;
    }
    ~SE_Message()
    {
        if(data)
            delete data;
    }
};
#endif
