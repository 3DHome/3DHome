#include "SE_DynamicLibType.h"
#include "SE_IO.h"
#include "SE_Log.h"
#include <stdio.h>
#include <string>
#include "SE_Common.h"
#ifdef WIN32
#include <windows.h>
#endif
#include "SE_MemLeakDetector.h"
static int getFileSize(FILE* fp)
{
    int        pos;
    int        end;

    pos = ftell (fp);
    fseek (fp, 0, SEEK_END);
    end = ftell (fp);
    fseek (fp, pos, SEEK_SET);

    return end;
}
#ifdef WIN32
void SE_IO::readFileAll(const char* fileName, char*& outData, int& outLen)
{
    outData = NULL;
    outLen = 0;
#ifdef _UNICODE
    TCHAR fileWideChar[MAX_PATH];
    memset(fileWideChar, 0, sizeof(wchar_t) * MAX_PATH);

    MultiByteToWideChar(CP_ACP, 0, fileName, -1, fileWideChar, MAX_PATH - 1);
#endif
    HANDLE pfile;
#ifdef _UNICODE
    pfile = ::CreateFile(fileWideChar, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 
                         FILE_ATTRIBUTE_NORMAL, NULL);
#else
    pfile = ::CreateFile(fileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 
                         FILE_ATTRIBUTE_NORMAL, NULL);
#endif
    DWORD err = GetLastError();
    if ( pfile == INVALID_HANDLE_VALUE)
    {
        return;
    }
    DWORD filesize = GetFileSize(pfile,NULL);
    char* buffer=new char[filesize];
    DWORD readsize;
    ReadFile(pfile, buffer, filesize, &readsize,NULL);
    SE_ASSERT(readsize == filesize);
    CloseHandle(pfile);
    outData = buffer;
    outLen = filesize;
}
#else
void SE_IO::readFileAll(const char* fileName, char*& outData, int& outLen)
{
    std::string filePath = fileName;
    FILE* fin = fopen(filePath.c_str(), "rb");
    outData = NULL;
    outLen = 0;
    if(!fin)
        return;
    int fileSize = getFileSize(fin);
    outData = new char[fileSize];
    if(!outData)
    {
        LOGE("out of memory when read file\n");
        return;
    }
    outLen = fileSize;
    size_t lenLeft = fileSize;
    char* p = outData;
    while(lenLeft > 0)
    {
        size_t readNum = fread(p, 1, lenLeft, fin);
        lenLeft -= readNum;
        p += readNum;
    }
    fclose(fin);
}
#endif
