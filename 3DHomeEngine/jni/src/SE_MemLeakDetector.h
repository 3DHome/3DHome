#ifndef SE_MEMLEAKDETECTOR_H
#define SE_MEMLEAKDETECTOR_H

#if defined (WIN32)

#ifdef _DEBUG
#include <stdlib.h>
#include <crtdbg.h>

#ifndef DEBUG_NEW
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

#endif

#endif
#endif