#include "SE_DynamicLibType.h"
#include<vector>
#include<string>
#include "SE_File.h"
#include "SE_Buffer.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
#ifdef WIN32
#include "SE_Utils.h"
#include<windows.h>
#else
#include<dirent.h>
#include<sys/stat.h>
#include<stdio.h>
#endif
#include "SE_Application.h"
SE_File::SE_File(const char* name, IO_TYPE type) : mFile(0)
{
    if(name == NULL)
        return;
    FILE* f = 0;
    if(type == WRITE)
    {
        f = fopen(name, "wb");
        mType = WRITE;
    }
    else
    {
        f = fopen(name, "rb");
        mType = READ;
    }
    if(f == NULL)
        return;
    mFile = f;
}
SE_File::~SE_File()
{
    if(mFile)
    {
        fclose(mFile);
    }
}
void SE_File::write(SE_BufferOutput& output)
{
    if(!mFile || mType == READ)
        return;
    int len = output.getDataLen();
    const char* data = output.getData();
    write(data, len);
}

void SE_File::write(const char* data, int len)
{
    if(!mFile)
        return;
    fwrite(data, 1, len, mFile);
}

bool SE_File::isExist(const char* filePath)
{
    FILE* f = fopen(filePath, "rb");
    if(!f)
        return false;
    else
        return true;
}

#ifdef WIN32
void SE_File::FindFile(std::vector<std::string>& result,const char* pluginsDirName,const char* ext)
{
    TCHAR modulePath[1024];
    memset(modulePath,0,sizeof(modulePath));
    GetModuleFileName(NULL,modulePath,1024);
    

#ifdef _UNICODE
    char dir[1024];
    SE_Util::wildCharToMultiByte(modulePath,dir,1024);

    std::string temp(dir);
#else
    std::string temp(modulePath);
#endif
    size_t pos = temp.rfind("\\");
    std::string path = temp.substr(0,pos);

    std::string moduleFullPath(path);

    std::string strtemp;  
    std::string token = ".";  
    std::string tokend = ".."; 


    std::string fullname = moduleFullPath + "\\" + pluginsDirName + "\\"  + ext;

#ifdef _UNICODE
    TCHAR fileWideChar[1024];
    SE_Util::multiByteToWideChar(fullname.c_str(),fileWideChar,1024);
#endif
    
    HANDLE hfile; 

    WIN32_FIND_DATA fileDate;//WIN32_FIND_DATA struct
    DWORD errorcode = 0;  
#ifdef _UNICODE
    hfile = FindFirstFile(fileWideChar ,&fileDate);  
#else
    hfile = FindFirstFile(fullname.c_str() ,&fileDate);
#endif
    while(hfile!= INVALID_HANDLE_VALUE && errorcode != ERROR_NO_MORE_FILES)  
    {  
#ifdef _UNICODE
        char buff[512];
        SE_Util::wildCharToMultiByte(fileDate.cFileName,buff,512);
        strtemp = buff;  
#else
        strtemp = fileDate.cFileName;
#endif
        bool flag = false;  
        if((fileDate.dwFileAttributes == FILE_ATTRIBUTE_DIRECTORY)&&(strtemp != token)&& (strtemp != tokend))  
        {  
            //is dir,findFile  
            flag = true;  
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("%s is a direcotry\n",strtemp.c_str());
            //FindFile(start + "\\" + strtemp);
        }  
        else  
        {
            std::string filename = moduleFullPath + "\\" + pluginsDirName + "\\" + strtemp;
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("%s is a file\n",filename.c_str());
            result.push_back(filename);
        }

        bool isNextFile = FindNextFile(hfile,&fileDate);//Is there any other file 

        if(flag == true && isNextFile == true)//yes go on 
        {
            SetLastError(NO_ERROR);  
        }
        else 
        {
            errorcode=GetLastError();
        }

    }  

    FindClose(hfile);


}

#else
void SE_File::FindFile(std::vector<std::string>& result,const char* pluginsDirName,const char* ext)
{
    DIR*   p;   
    struct dirent*  dirlist;   
    struct stat filestat;   
    char indir[1024];

    p=opendir(pluginsDirName);

    while((dirlist = readdir(p))!=NULL)     
    {   
      sprintf(indir,"%s/%s",pluginsDirName,dirlist->d_name);

      stat(indir,&filestat);

      if(S_ISREG(filestat.st_mode))     
      {   
          LOGI("## we found the %s in the folder.\n",indir); 
            std::string filename(indir);
            result.push_back(filename);  
      }   
      else     
      {   
          if(S_ISDIR(filestat.st_mode) && dirlist->d_name[0]!='.')     
          {   
                 //check_dir(indir);   //check subdir

                 continue;
          }   
      }   
    }   
}
#endif
