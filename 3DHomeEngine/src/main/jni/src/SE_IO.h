#ifndef SE_IO_H
#define SE_IO_H
class SE_ImageData;
class SE_ENTRY SE_IO
{
public:
    /**
     * user of this function has the resposibility to release the data in outData
     * */
    static void readFileAll(const char* fileName, char*& outData, int& outLen);
};
#endif
