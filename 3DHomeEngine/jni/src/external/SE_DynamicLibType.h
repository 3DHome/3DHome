#ifndef SE_DYNAMICLIBTYPE_H
#define SE_DYNAMICLIBTYPE_H

#if defined(WIN32)
//windows
#ifdef SE_EXPORT
    //lib project use this
    #define SE_ENTRY __declspec(dllexport)
#else
#ifdef SE_IMPORT
    // client of DLL uses this
    #define SE_ENTRY __declspec(dllimport)
#else
    //for static lib
    #define SE_ENTRY
#endif
#endif

#else
//linux
#define SE_ENTRY
#endif

#endif
