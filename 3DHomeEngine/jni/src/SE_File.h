#ifndef SE_FILE_H
#define SE_FILE_H
#include <stdio.h>
class SE_BufferOutput;
class SE_ENTRY SE_File
{
public:
    enum IO_TYPE {READ, WRITE};
    SE_File(const char* name, IO_TYPE);
    ~SE_File();
    void write(SE_BufferOutput& output);
    void write(const char* data, int len);
    static bool isExist(const char* filePath);
    static void FindFile(std::vector<std::string>& result,const char* pluginsDirName,const char* ext);
private:
    FILE* mFile;
    IO_TYPE mType;
};
#endif
