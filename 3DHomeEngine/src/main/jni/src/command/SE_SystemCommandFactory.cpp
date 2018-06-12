#include "SE_DynamicLibType.h"
#include<vector>
#include "SE_SystemCommandFactory.h"
#include "SE_SystemCommand.h"
#include "SE_UserCommand.h"
#include "SE_MemLeakDetector.h"
/*
class SE_CreateInitAppCommand : public SE_CreateCommandFunc
{
public:
    SE_Command* create(SE_Application* app, const SE_CommandID& id)
    {
        return new SE_InitAppCommand(app);
    }
};
class SE_CreateUpdateCameraCommand : public SE_CreateCommandFunc
{
public:
    SE_Command* create(SE_Application* app, const SE_CommandID& id)
    {
        return new SE_UpdateCameraCommand(app);
    }
};
*/
SE_SystemCommandFactory::SE_SystemCommandFactory()
{    
    SE_CommandEntry* systemCommandEntry[42];
    systemCommandEntry[0] = new SE_CommandEntry(SE_CommandID("SE_InitAppCommand"), new SE_CommandCreateFunc<SE_InitAppCommand>);
    systemCommandEntry[1] = new SE_CommandEntry(SE_CommandID("SE_InitCameraCommand"), new SE_CommandCreateFunc<SE_InitCameraCommand>);
    systemCommandEntry[2] = new SE_CommandEntry(SE_CommandID("SE_UpdateCameraCommand"), new SE_CommandCreateFunc<SE_UpdateCameraCommand>);
    systemCommandEntry[3] = new SE_CommandEntry(SE_CommandID("SE_KeyEventCommand"), new SE_CommandCreateFunc<SE_KeyEventCommand>);
    systemCommandEntry[4] = new SE_CommandEntry(SE_CommandID("SE_MotionEventCommand"), new SE_CommandCreateFunc<SE_MotionEventCommand>);

    systemCommandEntry[5] = new SE_CommandEntry(SE_CommandID("SE_RotateSpatialCommand"), new SE_CommandCreateFunc<SE_RotateSpatialCommand>);
    systemCommandEntry[6] = new SE_CommandEntry(SE_CommandID("SE_ScaleSpatialCommand"), new SE_CommandCreateFunc<SE_ScaleSpatialCommand>);
    systemCommandEntry[7] = new SE_CommandEntry(SE_CommandID("SE_TranslateSpatialCommand"), new SE_CommandCreateFunc<SE_TranslateSpatialCommand>);
    systemCommandEntry[8] = new SE_CommandEntry(SE_CommandID("SE_ResetSpatialCommand"), new SE_CommandCreateFunc<SE_ResetSpatialCommand>);

    systemCommandEntry[9] = new SE_CommandEntry(SE_CommandID("SE_RemoveSpatialCommand"), new SE_CommandCreateFunc<SE_RemoveSpatialCommand>);
    systemCommandEntry[10] = new SE_CommandEntry(SE_CommandID("SE_AddUserObjectCommand"), new SE_CommandCreateFunc<SE_AddUserObjectCommand>);
    systemCommandEntry[11] = new SE_CommandEntry(SE_CommandID("SE_OperateObjectCommand"), new SE_CommandCreateFunc<SE_OperateObjectCommand>);

    systemCommandEntry[12] = new SE_CommandEntry(SE_CommandID("SE_ReLoadSpatialCommand"), new SE_CommandCreateFunc<SE_ReLoadSpatialCommand>);
    systemCommandEntry[13] = new SE_CommandEntry(SE_CommandID("SE_ReLoadAllSpatialCommand"), new SE_CommandCreateFunc<SE_ReLoadAllSpatialCommand>);
    systemCommandEntry[14] = new SE_CommandEntry(SE_CommandID("SE_AddNewCbfCommand"), new SE_CommandCreateFunc<SE_AddNewCbfCommand>);
    systemCommandEntry[15] = new SE_CommandEntry(SE_CommandID("SE_OperateCameraCommand"), new SE_CommandCreateFunc<SE_OperateCameraCommand>);
    systemCommandEntry[16] = new SE_CommandEntry(SE_CommandID("SE_SetObjectAlphaCommand"), new SE_CommandCreateFunc<SE_SetObjectAlphaCommand>);
    systemCommandEntry[17] = new SE_CommandEntry(SE_CommandID("SE_SetObjectVisibleCommand"), new SE_CommandCreateFunc<SE_SetObjectVisibleCommand>);
    systemCommandEntry[18] = new SE_CommandEntry(SE_CommandID("SE_SetObjectRenderStateCommand"), new SE_CommandCreateFunc<SE_SetObjectRenderStateCommand>);
    systemCommandEntry[19] = new SE_CommandEntry(SE_CommandID("SE_SetObjectLayerCommand"), new SE_CommandCreateFunc<SE_SetObjectLayerCommand>);
//////////////////////////////////////////////////////////camrea relates///
    systemCommandEntry[20] = new SE_CommandEntry(SE_CommandID("SE_SetCameraCommand"), new SE_CommandCreateFunc<SE_SetCameraCommand>);
    systemCommandEntry[21] = new SE_CommandEntry(SE_CommandID("SE_SetViewportCommand"), new SE_CommandCreateFunc<SE_SetViewportCommand>);
    systemCommandEntry[22] = new SE_CommandEntry(SE_CommandID("SE_SetFrustumCommand"), new SE_CommandCreateFunc<SE_SetFrustumCommand>);
    systemCommandEntry[23] = new SE_CommandEntry(SE_CommandID("SE_RotateLocalCommand_I"), new SE_CommandCreateFunc<SE_RotateLocalCommand_I>);
    systemCommandEntry[24] = new SE_CommandEntry(SE_CommandID("SE_RotateLocalCommand_II"), new SE_CommandCreateFunc<SE_RotateLocalCommand_II>);
    systemCommandEntry[25] = new SE_CommandEntry(SE_CommandID("SE_TranslateLocalCommand"), new SE_CommandCreateFunc<SE_TranslateLocalCommand>);
    systemCommandEntry[26] = new SE_CommandEntry(SE_CommandID("SE_SetLocationCommand"), new SE_CommandCreateFunc<SE_SetLocationCommand>);
    systemCommandEntry[27] = new SE_CommandEntry(SE_CommandID("SE_SetObjectLightingPositonCommand"), new SE_CommandCreateFunc<SE_SetObjectLightingPositonCommand>);
    systemCommandEntry[28] = new SE_CommandEntry(SE_CommandID("SE_SetObjectNormalMapCommand"), new SE_CommandCreateFunc<SE_SetObjectNormalMapCommand>);
    systemCommandEntry[29] = new SE_CommandEntry(SE_CommandID("SE_SetObjectDefaultShaderCommand"), new SE_CommandCreateFunc<SE_SetObjectDefaultShaderCommand>);
    systemCommandEntry[30] = new SE_CommandEntry(SE_CommandID("SE_UnLoadSceneCommand"), new SE_CommandCreateFunc<SE_UnLoadSceneCommand>);
    systemCommandEntry[31] = new SE_CommandEntry(SE_CommandID("SE_DeleteObjectCommand"), new SE_CommandCreateFunc<SE_DeleteObjectCommand>);
    systemCommandEntry[32] = new SE_CommandEntry(SE_CommandID("SE_CloneObjectCommand"), new SE_CommandCreateFunc<SE_CloneObjectCommand>);
    systemCommandEntry[33] = new SE_CommandEntry(SE_CommandID("SE_CreateSceneCommand"), new SE_CommandCreateFunc<SE_CreateSceneCommand>);
    systemCommandEntry[34] = new SE_CommandEntry(SE_CommandID("SE_TranslateSpatialByNameCommand"), new SE_CommandCreateFunc<SE_TranslateSpatialByNameCommand>);
systemCommandEntry[35] = new SE_CommandEntry(SE_CommandID("SE_PlayBipedAnimationCommand"), new SE_CommandCreateFunc<SE_PlayBipedAnimationCommand>); 
systemCommandEntry[36] = new SE_CommandEntry(SE_CommandID("SE_InitRenderCapabilitiesCommand"), new SE_CommandCreateFunc<SE_InitRenderCapabilitiesCommand>); 
    systemCommandEntry[37] = new SE_CommandEntry(SE_CommandID("SE_CreateCBFCommand"), new SE_CommandCreateFunc<SE_CreateCBFCommand>); 

    systemCommandEntry[38] = new SE_CommandEntry(SE_CommandID("SE_RotateSpatialByNameCommand"), new SE_CommandCreateFunc<SE_RotateSpatialByNameCommand>);
    systemCommandEntry[39] = new SE_CommandEntry(SE_CommandID("SE_ScaleSpatialByNameCommand"), new SE_CommandCreateFunc<SE_ScaleSpatialByNameCommand>);
    systemCommandEntry[40] = new SE_CommandEntry(SE_CommandID("SE_TranslateSpatialByNameCommand"), new SE_CommandCreateFunc<SE_TranslateSpatialByNameCommand>);
    systemCommandEntry[41] = new SE_CommandEntry(SE_CommandID("SE_SaveSceneCommand"), new SE_CommandCreateFunc<SE_SaveSceneCommand>);
    set(systemCommandEntry, 42);
}
