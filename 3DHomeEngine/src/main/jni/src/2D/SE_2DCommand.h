#ifndef SE_2DCOMMAND_H
#define SE_2DCOMMAND_H
#include "SE_Command.h"
#include "SE_Primitive.h"
#include <string>
class SE_Application;
struct EyeData
{
    SE_PrimitiveID leftEyeID;
    SE_PrimitiveID rightEyeID;
    SE_SpatialID leftSpatialID;
};
class SE_Init2D : public SE_Command
{
public:
    SE_Init2D(SE_Application* app);
    ~SE_Init2D();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    std::string dataPath;
    std::string fileName;
};
class SE_2DUpdateCameraCommand : public SE_Command
{
public:
    SE_2DUpdateCameraCommand(SE_Application* app);
    ~SE_2DUpdateCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    int width;
    int height;
};
class SE_2DRunAllAnimation : public SE_Command
{
public:
    SE_2DRunAllAnimation(SE_Application* app);
    ~SE_2DRunAllAnimation();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
};
#endif
