#ifndef SE_SYSTEMCOMMAND_H
#define SE_SYSTEMCOMMAND_H
#include "SE_Command.h"
#include "SE_Vector.h"
#include "SE_Quat.h"
#include <string>
class SE_Application;
class SE_Camera;
class SE_KeyEvent;
class SE_MotionEvent;
class SE_ResourceManager;
class SE_Spatial;
class SE_ENTRY SE_InitAppCommand : public SE_Command
{
public:
    SE_InitAppCommand(SE_Application* app);
    ~SE_InitAppCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    std::string dataPath;
    std::string fileName; // the prefix of the file
};

#if defined (WIN32)
//////////////////////////////////////
class SE_ENTRY SE_UpdateCameraCommand : public SE_Command
{
public:
    SE_UpdateCameraCommand(SE_Application* app);
    ~SE_UpdateCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    int width;
    int height;
};
#endif
///////////////////////////////////////
class SE_ENTRY SE_KeyEventCommand : public SE_Command
{
public:
    SE_KeyEventCommand(SE_Application* app);
    ~SE_KeyEventCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    SE_KeyEvent* keyEvent;
};
class SE_ENTRY SE_MotionEventCommand : public SE_Command
{
public:
    SE_MotionEventCommand(SE_Application* app);
    ~SE_MotionEventCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    SE_MotionEvent* motionEvent;
};
////////////////////////////////////////
class SE_ENTRY SE_LoadSceneCommand : public SE_Command
{
public:
    SE_LoadSceneCommand(SE_Application* app);
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    std::string sceneName;
};
////////////////////////////////////////
/*class SE_MoveCameraCommand : public SE_Command
{
public:
    SE_MoveCameraCommand(SE_Application* app);
    ~SE_MoveCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* camera;
    float rotateAngle;
    SE_AXIS_TYPE axis;
    SE_Vector3f translate;

};*/
//////////////////////////////////////camera relates////////////////
class SE_ENTRY SE_InitCameraCommand : public SE_Command
{
public:
    SE_InitCameraCommand(SE_Application* app);
    ~SE_InitCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    int width;
    int height;
    SE_Vector3f mLocation;
    SE_Vector3f mAxisZ;
    SE_Vector3f mUp;
    float mFov;
    float mNear;
    float mFar;
};

#ifdef ANDROID
class SE_ENTRY SE_UpdateCameraCommand : public SE_Command
{
public:
    SE_UpdateCameraCommand(SE_Application* app);
    ~SE_UpdateCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    int mWidth;
    int mHeight;
    float mFov;
    float mNear;
    float mFar;
};
#endif
class SE_ENTRY SE_SetCameraCommand : public SE_Command
{
public:
    SE_SetCameraCommand(SE_Application* app);
    ~SE_SetCameraCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    SE_Vector3f mLocation;
    SE_Vector3f mAxisZ;
    SE_Vector3f mUp;
    SE_Vector3f mTarget;
    bool mFlag;
    float mFov;
    float mRatio;
    float mNear;
    float mFar;
};

class SE_ENTRY SE_SetViewportCommand : public SE_Command
{
public:
    SE_SetViewportCommand(SE_Application* app);
    ~SE_SetViewportCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    int mX;
    int mY;
    int mW;
    int mH;
};

class SE_ENTRY SE_SetFrustumCommand : public SE_Command
{
public:
    SE_SetFrustumCommand(SE_Application* app);
    ~SE_SetFrustumCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    float mFov;
    float mRatio;
    float mNear;
    float mFar;
};

class SE_ENTRY SE_RotateLocalCommand_I : public SE_Command
{
public:
    SE_RotateLocalCommand_I(SE_Application* app);
    ~SE_RotateLocalCommand_I();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    SE_Quat mRotate;
};

class SE_ENTRY SE_RotateLocalCommand_II : public SE_Command
{
public:
    SE_RotateLocalCommand_II(SE_Application* app);
    ~SE_RotateLocalCommand_II();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    float mAngle;
    SE_AXIS_TYPE mAxis;
};

class SE_ENTRY SE_TranslateLocalCommand : public SE_Command
{
public:
    SE_TranslateLocalCommand(SE_Application* app);
    ~SE_TranslateLocalCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    SE_Vector3f mTranslate;
};

class SE_ENTRY SE_SetLocationCommand : public SE_Command
{
public:
    SE_SetLocationCommand(SE_Application* app);
    ~SE_SetLocationCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    SE_Camera* mCamera;
    SE_Vector3f mLocation;
};

class SE_ENTRY SE_CreateSceneCommand : public SE_Command
{
public:
    SE_CreateSceneCommand(SE_Application* app);
    ~SE_CreateSceneCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    std::string dataPath;
    std::string fileName; // the prefix of the file
    std::string xmlpath;
};

class SE_ENTRY SE_CreateCBFCommand : public SE_Command
{
public:
    SE_CreateCBFCommand(SE_Application* app);
    ~SE_CreateCBFCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
public:
    std::string dataPath;
    std::vector<std::string> sceneDataFile;
    //std::string fileName; // the prefix of the file
    bool averageNormal;
};

class SE_SceneInflateCommand : public SE_Command
{
public:
    SE_SceneInflateCommand(SE_Application* app);
    ~SE_SceneInflateCommand();
    void handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    std::string sceneName;
    SE_ResourceManager* loader;
    SE_Spatial* child;
public:
};
#endif
