#include "SE_2DCommand.h"
#include "SE_Application.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_Spatial.h"
#include "SE_CommonNode.h"
#include "SE_Primitive.h"
#include "SE_ImageCodec.h"
#include "SE_MotionEventCamera.h"
#include "SE_Geometry.h"
#include "SE_MeshSimObject.h"
#include "SE_ImageData.h"
#include "SE_InputManager.h"
#include "SE_IO.h"
#include "SE_ElementManager.h"
#include "SE_RenderState.h"
#include "SE_SceneManager.h"
#include "SE_SimObject.h"
#include "SE_AnimationManager.h"
#include "SE_Animation.h"
#include "SE_Spatial.h"
#include <math.h>
#include <wchar.h>
#include <string.h>
#include "SE_MemLeakDetector.h"
SE_Init2D::SE_Init2D(SE_Application* app) : SE_Command(app)
{
}
SE_Init2D::~SE_Init2D()
{}

void SE_Init2D::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_ResourceManager* resourceManager = mApp->getResourceManager();
    resourceManager->setDataPath(dataPath.c_str());
    SE_ElementManager* elementManager = mApp->getElementManager();
    elementManager->load(NULL, fileName.c_str());
    SE_SceneManager* sceneManager = mApp->getSceneManager();
    SE_Spatial* root = elementManager->createSpatial();
    SE_DepthTestState* rs = new SE_DepthTestState();
    rs->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
    SE_BlendState* blendRs = new SE_BlendState;
    blendRs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
    blendRs->setBlendSrcFunc(SE_BlendState::SRC_ALPHA);
    blendRs->setBlendDstFunc(SE_BlendState::ONE_MINUS_SRC_ALPHA);
    root->setRenderState(SE_Spatial::DEPTHTESTSTATE, rs, OWN);
    root->setRenderState(SE_Spatial::BLENDSTATE, blendRs, OWN);
    root->updateWorldTransform();
    root->updateWorldLayer();
    root->updateRenderState();
    //sceneManager->addSpatial(NULL, root);
    }
//////////////
SE_2DUpdateCameraCommand::SE_2DUpdateCameraCommand(SE_Application* app) : SE_Command(app)
    {
    width = 480;
    height = 800;
    }
SE_2DUpdateCameraCommand::~SE_2DUpdateCameraCommand()
{}
void SE_2DUpdateCameraCommand::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
    {
    SE_Vector3f location(0, 0, 10);
    float ratio = height / (float)width;
    float angle = 2 * SE_RadianToAngle(atanf(width / 20.0f));
    SE_Camera* camera = new SE_MotionEventCamera;
    camera->create(location, SE_Vector3f(0, 0, 1), SE_Vector3f(0, 1, 0), angle, ratio, 1, 50);//(location, SE_Vector3f(1, 0, 0), SE_Vector3f(0, 1, 0), SE_Vector3f(0, 0, 1), angle * 2, ratio, 1, 20);
    camera->setViewport(0, 0, width, height);
    //mApp->setCamera(SE_Application::MAIN_CAMERA, camera);
    //mApp->setCurrentCamera(SE_Application::MAIN_CAMERA);
    //SE_InputManager* inputManager = mApp->getInputManager();
    //inputManager->removeMotionEventObserver(NULL);
    //inputManager->addMotionEventOberver(mApp->getCurrentCamera());
}
////////////////
SE_2DRunAllAnimation::SE_2DRunAllAnimation(SE_Application* app) : SE_Command(app)
{}
SE_2DRunAllAnimation::~SE_2DRunAllAnimation()
{}
class SE_RunAllAnimationTravel : public SE_SpatialTravel
{
public:
    int visit(SE_Spatial* spatial)
{
        SE_ElementManager* elementManager = SE_Application::getInstance()->getElementManager();
        SE_AnimationManager* animManager = SE_Application::getInstance()->getAnimationManager();
        SE_ElementID elementID = spatial->getElementID();
        SE_AnimationID animID = spatial->getAnimationID();
        animManager->removeAnimation(animID);
        SE_Element* element = elementManager->findByName(elementID.getStr());
        if(element)
    {
            SE_Animation* anim = element->getAnimation();
            SE_Animation* cloneAnim = NULL;
            if(anim)
        {
                cloneAnim = anim->clone();
                animID = animManager->addAnimation(cloneAnim);
                spatial->setAnimationID(animID);
                cloneAnim->setRunMode(SE_Animation::REPEAT);
                cloneAnim->run();
        }
    }
        return 0;
    }
    int visit(SE_SimObject* simObject)
    {
        return 1;
    }
};
void SE_2DRunAllAnimation::handle(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    SE_SceneManager* sceneManager = mApp->getSceneManager();
    SE_Spatial* root = sceneManager->getTopScene()->getRoot();
    SE_RunAllAnimationTravel rat;
    root->travel(&rat, true);
}
