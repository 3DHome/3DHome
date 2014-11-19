#ifdef NDK
#include <jni.h>
#include <string>
#else
#include <nativehelper/jni.h>
#include <utils/Log.h>
#include <utils/String8.h>
#include <utils/String16.h>
#endif

#ifdef GLES_20
#include <GLES2/gl2.h>
#endif

#include <list>
#include <string>
#include <vector>
#include <algorithm>
#include <limits>
#include <assert.h>
#include "SE_DynamicLibType.h"

#include <stdio.h>
#include <assert.h>
#include <dlfcn.h>
#include <GLES2/gl2.h>
#include "SE_Log.h"
#include "SE_Common.h"
#include "SE_Application.h"
#include "SE_ParticleSystemManager.h"
#include "SE_SystemCommandFactory.h"
#include "SE_SystemCommand.h"
#include "SE_InputEvent.h"
#include "SE_Struct.h"
#include "SE_UserCommand.h"
#include "SE_Spatial.h"
#include "SE_Camera.h"
#include "SE_SpatialTravel.h"
#include "SE_SceneManager.h"
#include "SE_ResourceManager.h"
#include "SE_MotionEventController.h"
#include "SE_ID.h"
#include "SE_Geometry.h"
#include "SE_Mesh.h"
#include "SE_GeometryData.h"
#include "SE_SimObjectManager.h"
#include "SE_InputManager.h"
#include "SkBitmap.h"
#include "SE_TextureCoordData.h"
//#include "SE_NewGeometry.h"
#include "SE_CommonNode.h"
#include "SE_DataValueDefine.h"
#include "SE_OctreeNode.h"
#include "SE_ObjectManager.h"
//#include "SE_SpatialManager.h"
#include "SE_Light.h"
#include "SE_Geometry3D.h"
#include "SE_BoundingVolume.h"

/********************add by liusong*************************/
static jclass classObject;

static jfieldID nativeBitmapID = 0;
static jfieldID objectNameID = 0;
static jfieldID objectIndexID = 0;
static jfieldID isEntiretyID = 0;
static jfieldID isNodeID = 0;
static jfieldID localTransParasID = 0;
static jfieldID userTransParasID = 0;
static jfieldID bvTypeID = 0;
static jfieldID layerIndexID = 0;

static jfieldID spatialEffectID = 0;
static jfieldID spatialStateID = 0;
static jfieldID spatialRuntimeID = 0;

static jmethodID methodInitObject;
static jmethodID methodGetVertexArray;
static jmethodID methodGetTexVertexArray;
static jmethodID methodGetFaceArray;
static jmethodID methodGetAlpha;
static jmethodID methodGetColor;

static jmethodID methodGetImageName;
static jmethodID methodGetImageType;

static jmethodID methodGetParentName;
static jmethodID methodGetParentIndex;
static jmethodID methodGetSceneName;
static jmethodID methodGetTranslate;
static jmethodID methodGetRotate;
static jmethodID methodGetScale;
static jmethodID methodGetLineWidth;

static jmethodID methodgetEffectData;
static jmethodID methodgetAlphaValue;
static jmethodID methodgetMirrorDensity;
static jmethodID methodgetShadowDensity;
static jmethodID methodgetStatusValue;
static jmethodID methodgetLightNames;
static jmethodID methodgetSpatialDataValue;

SE_Scene* findScene(const char* sceneName);
static bool isEmpty(const char* str) {
    return !strcmp(str, "");
}
SE_Spatial* findSpatial(JNIEnv* env, jobject obj) {

    jstring name = (jstring) env->GetObjectField(obj, objectNameID);
    const char* name8 = env->GetStringUTFChars(name, 0);
    int objectIndex = env->GetIntField(obj, objectIndexID);
    SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->getMainScene()->findSpatialByName(name8,
            objectIndex);
    if (!spatial) {
        if (SE_Application::getInstance()->SEHomeDebug)
            LOGD("BORQS## object %s not founded #######################\n", name8);
    }
    env->ReleaseStringUTFChars(name, name8);
    return spatial;
}

void getObjectData(JNIEnv* env, jobject objData, SE_ObjectCreator* creator) {

    int imageType = env->CallIntMethod(objData, methodGetImageType);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    if (imageType == 2) {
        jfloatArray jColorArray = (jfloatArray) env->CallObjectMethod(objData, methodGetColor);
        float* colorArray = env->GetFloatArrayElements(jColorArray, 0);
        SE_Vector3f color = SE_Vector3f(colorArray[0], colorArray[1], colorArray[2]);
        creator->setColor(color);
        env->ReleaseFloatArrayElements(jColorArray, colorArray, 0);
    } else {
        jstring imageName = (jstring) env->CallObjectMethod(objData, methodGetImageName);
        const char* imageName8 = env->GetStringUTFChars(imageName, 0);
        SE_ImageDataID imageDataid = SE_ImageDataID(imageName8);
        creator->setImageDataID(imageDataid);
        jfloatArray jTexVertexArray = (jfloatArray) env->CallObjectMethod(objData, methodGetTexVertexArray);
        int texVertexSize = env->GetArrayLength(jTexVertexArray) / 2;
        float* texVertexArray = env->GetFloatArrayElements(jTexVertexArray, 0);
        SE_Vector2f* texVertex = new SE_Vector2f[texVertexSize];
        env->ReleaseFloatArrayElements(jTexVertexArray, texVertexArray, 0);
        for (int i = 0; i < texVertexSize; i++) {
            texVertex[i] = SE_Vector2f(texVertexArray[2 * i], texVertexArray[2 * i + 1]);
        }
        creator->setTextureCoorArray(texVertex, texVertexSize);
        env->ReleaseStringUTFChars(imageName, imageName8);
    }
    int bvType = env->GetIntField(objData, bvTypeID);
    creator->setBvType(bvType);
    float alpha = env->CallFloatMethod(objData, methodGetAlpha);
    creator->setAlpha(alpha);
    jfloatArray jVertexArray = (jfloatArray) env->CallObjectMethod(objData, methodGetVertexArray);
    int vertexSize = env->GetArrayLength(jVertexArray) / 3;
    float* vertexArray = env->GetFloatArrayElements(jVertexArray, 0);
    SE_Vector3f* vertex = new SE_Vector3f[vertexSize];
    for (int i = 0; i < vertexSize; i++) {
        vertex[i] = SE_Vector3f(vertexArray[3 * i], vertexArray[3 * i + 1], vertexArray[3 * i + 2]);
    }
    creator->setVertexArray(vertex, vertexSize);

    jintArray jFaceArray = (jintArray) env->CallObjectMethod(objData, methodGetFaceArray);
    int facesSize = env->GetArrayLength(jFaceArray) / 3;
    int* faceArray = env->GetIntArrayElements(jFaceArray, 0);
    SE_Vector3i* faces = new SE_Vector3i[facesSize];
    for (int i = 0; i < facesSize; i++) {
        faces[i] = SE_Vector3i(faceArray[3 * i], faceArray[3 * i + 1], faceArray[3 * i + 2]);
    }
    int* facet = new int[facesSize];
    for (int i = 0; i < facesSize; i++) {
        facet[i] = i;
    }
    creator->setVertexIndexArray(faces, facesSize);
    creator->setFacetIndexArray(facet, facesSize);

    SE_Vector3i* texFaces = new SE_Vector3i[facesSize];
    memcpy(texFaces, faces, sizeof(SE_Vector3i) * facesSize);
    creator->setTextureCoorIndexArray(texFaces, facesSize);

    env->ReleaseFloatArrayElements(jVertexArray, vertexArray, 0);
    env->ReleaseIntArrayElements(jFaceArray, faceArray, 0);

    float lineWidth = env->CallFloatMethod(objData, methodGetLineWidth);
    creator->setLineWidth(lineWidth);
    int layerIndex = env->GetIntField(objData, layerIndexID);
    creator->setLayerIndex(layerIndex);
}

static void se_addUserObject(JNIEnv* env, jobject obj) {
    jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    if (SE_Application::getInstance()->SEHomeDebug)
        LOGD("## add user object ### sceneName = %s\n", sceneName8);
    SE_Scene* scene = findScene(sceneName8);
    if (!scene) {
        if (SE_Application::getInstance()->SEHomeDebug)
            LOGD("## add user object failed, scene not found### scene name = %s\n", sceneName8);
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        return;
    }

    jstring parentName = (jstring) env->CallObjectMethod(obj, methodGetParentName);
    int parentIndex = env->CallIntMethod(obj, methodGetParentIndex);
    const char* parentName8 = env->GetStringUTFChars(parentName, NULL);
    if (SE_Application::getInstance()->SEHomeDebug)
        LOGD("## add user object ### parentName = %s\n", parentName8);
    SE_Spatial* parent = SE_Application::getInstance()->getSceneManager()->findSpatialByName(parentName8, parentIndex);
    if (!parent) {
        if (SE_Application::getInstance()->SEHomeDebug)
            LOGD("## add user object failed, parent not found ### parentName = %s\n", parentName8);
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        env->ReleaseStringUTFChars(parentName, parentName8);
        return;
    }

    jstring name = (jstring) env->GetObjectField(obj, objectNameID);
    const char* name8 = env->GetStringUTFChars(name, 0);
    int objectIndex = env->GetIntField(obj, objectIndexID);
    bool isEntirety = env->GetBooleanField(obj, isEntiretyID);
    if (SE_Application::getInstance()->SEHomeDebug)
        LOGD("## add user object ### name = %s\n", name8);
    SE_Spatial* hasExist = findSpatial(env, obj);
    if (hasExist) {
        if (SE_Application::getInstance()->SEHomeDebug)
            LOGD("## add user object failed, object hasExist### object name = %s\n", name8);
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        env->ReleaseStringUTFChars(parentName, parentName8);
        env->ReleaseStringUTFChars(name, name8);
        return;
    }
    int spatialEffect = env->GetIntField(obj, spatialEffectID);
    int spatialState = env->GetIntField(obj, spatialStateID);
    int spatialRuntime = env->GetIntField(obj, spatialRuntimeID);

    jobject transParas = env->GetObjectField(obj, localTransParasID);
    jfloatArray localTranslateArray = (jfloatArray) env->CallObjectMethod(transParas, methodGetTranslate);
    float* localTranslate = env->GetFloatArrayElements(localTranslateArray, 0);
    jfloatArray localRotateArray = (jfloatArray) env->CallObjectMethod(transParas, methodGetRotate);
    float* localRotate = env->GetFloatArrayElements(localRotateArray, 0);
    jfloatArray localScaleArray = (jfloatArray) env->CallObjectMethod(transParas, methodGetScale);
    float* localScale = env->GetFloatArrayElements(localScaleArray, 0);
    SE_Vector3f v = SE_Vector3f(localTranslate[0], localTranslate[1], localTranslate[2]);
    SE_Quat q;
    q.set(localRotate[0], SE_Vector3f(localRotate[1], localRotate[2], localRotate[3]));
    SE_Vector3f s = SE_Vector3f(localScale[0], localScale[1], localScale[2]);

    jobject userTransParas = env->GetObjectField(obj, userTransParasID);
    jfloatArray userTranslateArray = (jfloatArray) env->CallObjectMethod(userTransParas, methodGetTranslate);
    float* userTranslate = env->GetFloatArrayElements(userTranslateArray, 0);
    jfloatArray userRotateArray = (jfloatArray) env->CallObjectMethod(userTransParas, methodGetRotate);
    float* userRotate = env->GetFloatArrayElements(userRotateArray, 0);
    jfloatArray userScaleArray = (jfloatArray) env->CallObjectMethod(userTransParas, methodGetScale);
    float* userScale = env->GetFloatArrayElements(userScaleArray, 0);
    SE_Vector3f uv = SE_Vector3f(userTranslate[0], userTranslate[1], userTranslate[2]);
    SE_Quat uq;
    uq.set(userRotate[0], SE_Vector3f(userRotate[1], userRotate[2], userRotate[3]));
    SE_Vector3f us = SE_Vector3f(userScale[0], userScale[1], userScale[2]);

    bool isNode = env->GetBooleanField(obj, isNodeID);
    if (isNode) {
        if (SE_Application::getInstance()->SEHomeDebug)
            LOGD("## add user object successfully ### isNode name = %s\n", name8);
        SE_CommonNode* node = new SE_CommonNode(SE_ID::createSpatialID());
        node->setSpatialName(name8);
        node->setCloneIndex(objectIndex);
        scene->addSpatial(parent, node);
        node->setIsEntirety(isEntirety);
        SE_SpatialEffectData* spatialEffectData = node->getSpatialEffectData();
        spatialEffectData->mEffectState = spatialEffect;
        spatialEffectData->mSpatialState = spatialState;
        spatialEffectData->mSpatialRuntimeState = spatialRuntime;
        node->updateRenderState();

        node->setLocalTranslate(v);
        node->setLocalRotate(q);
        node->setLocalScale(s);
        node->setUserTranslate(uv);
        node->setUserRotate(uq);
        node->setUserScale(us);
        node->setBVType(1);
        node->updateWorldTransform();
        node->setChildrenHasTransform(true);
        node->setTransformChanged(true);
        env->ReleaseStringUTFChars(sceneName, sceneName8);
        env->ReleaseStringUTFChars(parentName, parentName8);
        env->ReleaseStringUTFChars(name, name8);
        env->ReleaseFloatArrayElements(localTranslateArray, localTranslate, 0);
        env->ReleaseFloatArrayElements(localRotateArray, localRotate, 0);
        env->ReleaseFloatArrayElements(localScaleArray, localScale, 0);
        env->ReleaseFloatArrayElements(userTranslateArray, userTranslate, 0);
        env->ReleaseFloatArrayElements(userRotateArray, userRotate, 0);
        env->ReleaseFloatArrayElements(userScaleArray, userScale, 0);
        return;
    }
    LOGD("## add user object successfully ### name = %s\n", name8);
    SE_ObjectCreator* creator = new SE_ObjectCreator();
    creator->setObjectName(name8);
    getObjectData(env, obj, creator);
    SE_Spatial* spatial = creator->create(scene, parent);
    spatial->setSpatialName(name8);
    spatial->setCloneIndex(objectIndex);
    spatial->setIsEntirety(isEntirety);
    SE_SpatialEffectData* spatialEffectData = spatial->getSpatialEffectData();
    spatialEffectData->mEffectState = spatialEffect;
    spatialEffectData->mSpatialState = spatialState;
    spatialEffectData->mSpatialRuntimeState = spatialRuntime;
    spatial->updateRenderState();
    spatial->setLocalTranslate(v);
    spatial->setLocalRotate(q);
    spatial->setLocalScale(s);
    spatial->setUserTranslate(uv);
    spatial->setUserRotate(uq);
    spatial->setUserScale(us);
    spatial->setBVType(1);
    spatial->updateWorldTransform();
    spatial->setTransformChanged(true);
    delete creator;
    env->ReleaseStringUTFChars(sceneName, sceneName8);
    env->ReleaseStringUTFChars(parentName, parentName8);
    env->ReleaseStringUTFChars(name, name8);
    env->ReleaseFloatArrayElements(localTranslateArray, localTranslate, 0);
    env->ReleaseFloatArrayElements(localRotateArray, localRotate, 0);
    env->ReleaseFloatArrayElements(localScaleArray, localScale, 0);
    env->ReleaseFloatArrayElements(userTranslateArray, userTranslate, 0);
    env->ReleaseFloatArrayElements(userRotateArray, userRotate, 0);
    env->ReleaseFloatArrayElements(userScaleArray, userScale, 0);
}

static void se_setName(JNIEnv* env, jobject obj, jstring name) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        const char* name8 = env->GetStringUTFChars(name, NULL);
        spatial->setSpatialName(name8);
        env->ReleaseStringUTFChars(name, name8);
    }
}

static jboolean se_isImageExist(JNIEnv* env, jobject clazz, jstring path) {
    const char* imagePath = env->GetStringUTFChars(path, NULL);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    SE_ImageData* existdata = resourceManager->getImageDataFromPath(imagePath);
    if (existdata && existdata->getData()) {
        if (_DEBUG)
            LOGD("BORQS## Image is Exist ### %s\n", imagePath);
        env->ReleaseStringUTFChars(path, imagePath);
        return true;
    } else {
        if (_DEBUG)
            LOGD("BORQS## Image is not Exist ### %s\n", imagePath);
        env->ReleaseStringUTFChars(path, imagePath);
        return false;
    }
}

static jint se_loadImageData(JNIEnv* env, jobject clazz, jstring path) {
    if (_DEBUG)
        LOGD("BORQS## loadImageData ###");
    const char* imagePath = env->GetStringUTFChars(path, NULL);
    SE_ImageData* newImgd = SE_ImageCodec::loadAsset(imagePath);
    env->ReleaseStringUTFChars(path, imagePath);
    return (int) newImgd;
}

static jint se_loadImageData_II(JNIEnv* env, jobject clazz, jobject jbitmap) {
    if (_DEBUG)
        LOGD("BORQS## loadImageData II ###");
#ifdef NDK
    SE_ImageData* newImgd = SE_ImageCodec::load(env, jbitmap);
#else
    SkBitmap* bitmap = (SkBitmap*) env->GetIntField(jbitmap, nativeBitmapID);
    SE_ImageData* newImgd = SE_ImageCodec::load(bitmap);
#endif
    /*
     SE_ImageCodec::resizeImageData(newImgd);
     */
    return (int) newImgd;
}
static void se_releaseImageData(JNIEnv* env, jobject clazz, jint imageDataObj) {
    SE_ImageData* imgd = (SE_ImageData*) imageDataObj;
    delete imgd;
}

static void se_addImageData(JNIEnv* env, jobject clazz, jstring imageKey, jint imageDataObj) {
    if (imageDataObj == 0) {
        return;
    }
    const char* imageKey8 = env->GetStringUTFChars(imageKey, NULL);
    if (_DEBUG)
        LOGD("BORQS## addImageData ###imageKey = %s\n", imageKey8);

    SE_ImageData* newImgd = (SE_ImageData*) imageDataObj;
    newImgd->setName(imageKey8);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();

    resourceManager->insertPathImageData(imageKey8, newImgd, true);
    env->ReleaseStringUTFChars(imageKey, imageKey8);
}

static jstring se_getImageName(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        SE_SimObject * simObject = spatial->getCurrentAttachedSimObj();
        if (simObject) {
            SE_Mesh* mesh = simObject->getMesh();
            if (mesh) {
                SE_Texture* texture = mesh->getTexture(0);
                if (texture) {
                    SE_TextureUnit* texUnit = texture->getTextureUnit(0);
                    if (texUnit) {
                        SE_ImageDataID imgID = texUnit->getImageDataID(0);
                        return env->NewStringUTF(imgID.getStr());
                    }
                }
            }
        }
    }
    return NULL;
}

static jstring se_getImageKey(JNIEnv* env, jobject obj, jstring name) {
    const char* name8 = env->GetStringUTFChars(name, NULL);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    const char* path = resourceManager->getIdPath(name8);
    env->ReleaseStringUTFChars(name, name8);
    return env->NewStringUTF(path);
}

static void se_removeImageData(JNIEnv* env, jobject clazz, jstring key) {
    if (_DEBUG)
        LOGD("BORQS## removeImageData ###");
#if 0
    const char* imageKey = env->GetStringUTFChars(key, NULL);
    SE_ImageDataID imageDataid(imageKey);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    resourceManager->removeImageData(imageDataid);
    env->ReleaseStringUTFChars(key, imageKey);
#endif
}

static void se_applyImage(JNIEnv* env, jobject clazz, jstring imageName, jstring imageKey) {
    const char* imageName8 = env->GetStringUTFChars(imageName, NULL);
    const char* imageKey8 = env->GetStringUTFChars(imageKey, NULL);
    SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
    const char* oldKey = resourceManager->getIdPath(imageName8);
    if (oldKey && strcmp(oldKey, imageKey8) == 0) {
        env->ReleaseStringUTFChars(imageName, imageName8);
        env->ReleaseStringUTFChars(imageKey, imageKey8);
        return;
    }
    //release old image data
    SE_ImageDataID imageDataid = SE_ImageDataID(imageName8);
    resourceManager->unregisterRes(SE_ResourceManager::IMAGE_RES, &imageDataid);

    //insert new image data if not exist
    SE_ImageData* existdata = resourceManager->getImageDataFromPath(imageKey8);
    if (!existdata) {
        SE_ImageData* newImgd = new SE_ImageData();
        newImgd->setName(imageKey8);
        resourceManager->insertPathImageData(imageKey8, newImgd, true);
    }

    resourceManager->setIdPath(imageName8, imageKey8, true);
    resourceManager->registerRes(SE_ResourceManager::IMAGE_RES, &imageDataid);
    env->ReleaseStringUTFChars(imageName, imageName8);
    env->ReleaseStringUTFChars(imageKey, imageKey8);
}

static void se_updateVertex(JNIEnv* env, jobject obj, jfloatArray vertex) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* newVertex = env->GetFloatArrayElements(vertex, 0);
        SE_Mesh* mesh = spatial->getCurrentAttachedSimObj()->getMesh();
        SE_GeometryData* geometryData = mesh->getGeometryData();
        SE_Vector3f* oldVertex = geometryData->getVertexArray();
        int vertexNum = geometryData->getVertexNum();
        SE_Surface* surface = mesh->getSurface(0);
        for (int i = 0; i < vertexNum; i++) {
            oldVertex[i].x = newVertex[3 * i];
            oldVertex[i].y = newVertex[3 * i + 1];
            oldVertex[i].z = newVertex[3 * i + 2];
        }
        surface->upDateFaceVertex();
        spatial->updateWorldTransform();
        spatial->setTransformChanged(true);
        env->ReleaseFloatArrayElements(vertex, newVertex, 0);
    }

}

static void se_updateTexture(JNIEnv* env, jobject obj, jfloatArray texVertex) {
    if (_DEBUG) {
        jstring stringName = (jstring) env->GetObjectField(obj, objectNameID);
        const char* name = env->GetStringUTFChars(stringName, 0);
        LOGD("BORQS## updateTexture### name = %s\n", name);
        env->ReleaseStringUTFChars(stringName, name);
    }
    SE_Spatial* spatial = findSpatial(env, obj);
    float* newTexVertex = env->GetFloatArrayElements(texVertex, 0);
    if (spatial) {
        SE_Mesh* mesh = spatial->getCurrentAttachedSimObj()->getMesh();
        SE_Texture* tex = mesh->getTexture(0);
        SE_Surface* surface = mesh->getSurface(0);
        SE_TextureUnit* texUnit = tex->getTextureUnit(0);
        SE_TextureCoordData* texCoordData = texUnit->getTextureCoordData();
        SE_Vector2f* texVertexArray = texCoordData->getTexVertexArray();
        int num = texCoordData->getTexVertexNum();
        for (int i = 0; i < num; i++) {
            texVertexArray[i].x = newTexVertex[2 * i];
            texVertexArray[i].y = newTexVertex[2 * i + 1];
        }
        surface->upDateFaceTexVertex(0);
    }
    env->ReleaseFloatArrayElements(texVertex, newTexVertex, 0);
}

static void se_setUserTransParas(JNIEnv* env, jobject obj, jfloatArray translate, jfloatArray rotate,
        jfloatArray scale) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## operateObject### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/

    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ls = env->GetFloatArrayElements(translate, 0);
        float* rs = env->GetFloatArrayElements(rotate, 0);
        float* ss = env->GetFloatArrayElements(scale, 0);
        SE_Vector3f newTranslate = SE_Vector3f(ls[0], ls[1], ls[2]);
        SE_Vector3f newScale = SE_Vector3f(ss[0], ss[1], ss[2]);
        SE_Quat newRotate;
        newRotate.set(rs[0], SE_Vector3f(rs[1], rs[2], rs[3]));
        spatial->setUserTranslate(newTranslate);
        spatial->setUserRotate(newRotate);
        spatial->setUserScale(newScale);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(translate, ls, 0);
        env->ReleaseFloatArrayElements(rotate, rs, 0);
        env->ReleaseFloatArrayElements(scale, ss, 0);
    }
}

static void se_setUserRotate(JNIEnv* env, jobject obj, jfloatArray rotate) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## setRotate### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {

        float* rs = env->GetFloatArrayElements(rotate, 0);
        //SE_Matrix4f postMatrix;
        SE_Quat newRotate;
        newRotate.set(rs[0], SE_Vector3f(rs[1], rs[2], rs[3]));
        spatial->setUserRotate(newRotate);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(rotate, rs, 0);
    }
}

static void se_setUserTranslate(JNIEnv* env, jobject obj, jfloatArray translate) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## setTranslate### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ls = env->GetFloatArrayElements(translate, 0);
        SE_Vector3f newTranslate = SE_Vector3f(ls[0], ls[1], ls[2]);
        spatial->setUserTranslate(newTranslate);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(translate, ls, 0);
    }
}

static void se_setUserScale(JNIEnv* env, jobject obj, jfloatArray scale) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## setScale### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ss = env->GetFloatArrayElements(scale, 0);
        SE_Vector3f newScale = SE_Vector3f(ss[0], ss[1], ss[2]);
        spatial->setUserScale(newScale);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(scale, ss, 0);
    }
}
static void se_setLocalTransParas(JNIEnv* env, jobject obj, jfloatArray translate, jfloatArray rotate,
        jfloatArray scale) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## operateObject### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/

    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ls = env->GetFloatArrayElements(translate, 0);
        float* rs = env->GetFloatArrayElements(rotate, 0);
        float* ss = env->GetFloatArrayElements(scale, 0);
        SE_Vector3f newTranslate = SE_Vector3f(ls[0], ls[1], ls[2]);
        SE_Vector3f newScale = SE_Vector3f(ss[0], ss[1], ss[2]);
        SE_Quat newRotate;
        newRotate.set(rs[0], SE_Vector3f(rs[1], rs[2], rs[3]));
        spatial->setLocalTranslate(newTranslate);
        spatial->setLocalRotate(newRotate);
        spatial->setLocalScale(newScale);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(translate, ls, 0);
        env->ReleaseFloatArrayElements(rotate, rs, 0);
        env->ReleaseFloatArrayElements(scale, ss, 0);
    }
}
static void se_setLocalRotate(JNIEnv* env, jobject obj, jfloatArray rotate) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## set local rotate### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {

        float* rs = env->GetFloatArrayElements(rotate, 0);
        SE_Quat newRotate;
        newRotate.set(rs[0], SE_Vector3f(rs[1], rs[2], rs[3]));
        spatial->setLocalRotate(newRotate);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(rotate, rs, 0);
    }
}

static void se_setLocalTranslate(JNIEnv* env, jobject obj, jfloatArray translate) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## set local translate### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ls = env->GetFloatArrayElements(translate, 0);
        SE_Vector3f newTranslate = SE_Vector3f(ls[0], ls[1], ls[2]);
        spatial->setLocalTranslate(newTranslate);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(translate, ls, 0);
    }
}

static void se_setLocalScale(JNIEnv* env, jobject obj, jfloatArray scale) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## set local scale### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* ss = env->GetFloatArrayElements(scale, 0);
        SE_Vector3f newScale = SE_Vector3f(ss[0], ss[1], ss[2]);
        spatial->setLocalScale(newScale);
        spatial->updateWorldTransform();
        env->ReleaseFloatArrayElements(scale, ss, 0);
    }
}
static jfloatArray se_worldToLocal(JNIEnv* env, jobject obj, jfloatArray inValues) {

    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* jinValues = env->GetFloatArrayElements(inValues, 0);
        SE_Vector4f in = SE_Vector4f(jinValues[0], jinValues[1], jinValues[2], 1);
        SE_Vector4f out = spatial->getWorldTransform().inverse().map(in);
        jfloatArray outValuesArray = env->NewFloatArray(3);
        env->SetFloatArrayRegion(outValuesArray, 0, 3, out.d);
        env->ReleaseFloatArrayElements(inValues, jinValues, 0);
        return outValuesArray;
    }
    return NULL;
}

static jfloatArray se_localToWorld(JNIEnv* env, jobject obj, jfloatArray inValues) {

    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        float* jinValues = env->GetFloatArrayElements(inValues, 0);
        SE_Vector4f in = SE_Vector4f(jinValues[0], jinValues[1], jinValues[2], 1);
        SE_Vector4f out = spatial->getWorldTransform().map(in);
        jfloatArray outValuesArray = env->NewFloatArray(3);
        env->SetFloatArrayRegion(outValuesArray, 0, 3, out.d);
        env->ReleaseFloatArrayElements(inValues, jinValues, 0);
        return outValuesArray;
    }
    return NULL;
}

static jfloatArray se_localToScreenCoordinate(JNIEnv* env, jobject obj, jfloatArray inValues) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
        const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
        SE_Scene* scene = findScene(sceneName8);
        if (scene) {
            SE_Camera* camera = scene->getCamera();
            if (camera) {
                float* jinValues = env->GetFloatArrayElements(inValues, 0);
                SE_Vector4f in = SE_Vector4f(jinValues[0], jinValues[1], jinValues[2], 1);
                SE_Vector4f world = spatial->getWorldTransform().map(in);
                env->ReleaseFloatArrayElements(inValues, jinValues, 0);
                SE_Matrix4f m = camera->getPerspectiveMatrix().mul(camera->getWorldToViewMatrix());
                SE_Vector4f out = m.map(world);
                SE_Vector2f result;
                result.x = out.x / out.w;
                result.y = out.y / out.w;
                SE_Rect<int> viewport = camera->getViewport();
                int width = viewport.right - viewport.left;
                int height = viewport.bottom - viewport.top;
                result.x = (result.x + 1) * width / 2;
                result.y = (1 - result.y) * height / 2;
                jfloatArray outValuesArray = env->NewFloatArray(2);
                env->SetFloatArrayRegion(outValuesArray, 0, 2, result.d);
                env->ReleaseFloatArrayElements(inValues, jinValues, 0);
                env->ReleaseStringUTFChars(sceneName, sceneName8);
                return outValuesArray;
            }
        }
        env->ReleaseStringUTFChars(sceneName, sceneName8);
    }
    return NULL;

}

static void se_applyLight(JNIEnv* env, jobject obj, jstring lightName) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
        const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
        SE_Scene* scene = findScene(sceneName8);
        const char* lightName8 = env->GetStringUTFChars(lightName, NULL);
        if (scene->getLight(lightName8)) {
            spatial->addLightNameToList(lightName8);
            spatial->setNeedLighting(true);
        }
        env->ReleaseStringUTFChars(lightName, lightName8);
        env->ReleaseStringUTFChars(sceneName, sceneName8);
    }
}

static void se_unApplyLight(JNIEnv* env, jobject obj, jstring lightName) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        const char* lightName8 = env->GetStringUTFChars(lightName, NULL);
        spatial->removeLight(lightName8);
        env->ReleaseStringUTFChars(lightName, lightName8);
    }
}

static void se_changeParent(JNIEnv* env, jobject obj, jstring parentName, jint parentIndex) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## set object Parent### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    const char* parentName8 = env->GetStringUTFChars(parentName, NULL);
    SE_Spatial* parent = SE_Application::getInstance()->getSceneManager()->findSpatialByName(parentName8, parentIndex);
    if (parent) {
        SE_Spatial* spatial = findSpatial(env, obj);
        if (spatial) {
            spatial->getScene()->removeSpatial(spatial);
            if (spatial->getParent()) {
                spatial->getParent()->setChildrenHasTransform(true);
                spatial->getParent()->setTransformChanged(true);
            }
            parent->getScene()->addSpatial(parent, spatial);
            parent->setChildrenHasTransform(true);
            parent->setTransformChanged(true);
        }
    }
    env->ReleaseStringUTFChars(parentName, parentName8);
}

static void se_release(JNIEnv* env, jobject obj) {
    if (_DEBUG) {
        jstring stringName = (jstring) env->GetObjectField(obj, objectNameID);
        const char* name = env->GetStringUTFChars(stringName, 0);
        LOGD("BORQS## release### name = %s\n", name);
        env->ReleaseStringUTFChars(stringName, name);
    }
    jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    if (_DEBUG)
        LOGD("## release user object ### sceneName = %s\n", sceneName8);
    SE_Scene* scene = findScene(sceneName8);
    if (scene) {
        SE_Spatial* spatial = findSpatial(env, obj);
        if (spatial) {
            SE_Spatial* leader = spatial->getLeader();
            if (leader) {
                leader->removeFollower(spatial);
            }
            SE_Spatial* rs = scene->removeSpatial(spatial);
            if (rs)
                delete rs;
        }
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
}

static jint se_remove(JNIEnv* env, jobject obj) {
    if (_DEBUG) {
        jstring stringName = (jstring) env->GetObjectField(obj, objectNameID);
        const char* name = env->GetStringUTFChars(stringName, 0);
        LOGD("BORQS## remove### name = %s\n", name);
        env->ReleaseStringUTFChars(stringName, name);
    }
    jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
    const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
    if (_DEBUG)
        LOGD("## remove user object ### sceneName = %s\n", sceneName8);
    SE_Scene* scene = findScene(sceneName8);
    if (scene) {
        SE_Spatial* spatial = findSpatial(env, obj);
        if (spatial) {
            SE_Spatial* leader = spatial->getLeader();
            if (leader) {
                leader->removeFollower(spatial);
            }
            SE_Spatial* rs = scene->removeSpatial(spatial);
            env->ReleaseStringUTFChars(sceneName, sceneName8);
            if (rs)
                return (int) rs;
        }
    }
    env->ReleaseStringUTFChars(sceneName, sceneName8);
    return 0;
}

static void se_setVisible(JNIEnv* env, jobject obj, jboolean visible) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## setVisible### name = %s\n", name);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setVisible(visible);
    }
}

static void se_setAlpha(JNIEnv* env, jobject obj, jfloat alpha) {
    /*if (_DEBUG) {
     jstring stringName = (jstring)env->GetObjectField(obj, objectNameID);
     const char* name = env->GetStringUTFChars(stringName, 0);
     LOGD("BORQS## setAlpha### name = %s, alpha = %f\n", name, alpha);
     env->ReleaseStringUTFChars(stringName, name);
     }*/
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setAlpha(alpha);
    }
}

static jboolean se_cloneObject(JNIEnv* env, jobject clazz, jobject parent, jint index) {
    if (_DEBUG) {
        jstring stringName = (jstring) env->GetObjectField(clazz, objectNameID);
        const char* name = env->GetStringUTFChars(stringName, 0);
        LOGD("BORQS## cloneObject### name = %s\n", name);
        env->ReleaseStringUTFChars(stringName, name);
    }
    SE_Spatial* srcSpatial = findSpatial(env, clazz);

    if (srcSpatial) {
        std::string status = srcSpatial->getChildrenStatus();
        SE_Spatial* parentSpatial = findSpatial(env, parent);
        if (!parentSpatial) {
            return false;
        }
        SE_Spatial* dest = srcSpatial->clone(parentSpatial, index, true, status.c_str());
        parentSpatial->setChildrenHasTransform(true);
        parentSpatial->setTransformChanged(true);
        parentSpatial->updateWorldTransform();
#if 0
        SE_BlendState *rs_blend = new SE_BlendState();
        rs_blend->setBlendProperty(SE_BlendState::BLEND_ENABLE);
        dest->setRenderState(SE_Spatial::BLENDSTATE,rs_blend);
        dest->updateRenderState();
#endif
        return true;
    } else {
        if (_DEBUG)
            LOGD("## The src not found! ##\n\n");
        return false;
    }
}

static jboolean se_isEnableDepth(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        return spatial->isNeedDepthTest();
    }
    return false;
}

static jboolean se_isEnableBlend(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        return spatial->isSpatialEffectHasAttribute(SE_SpatialAttribute::BLENDABLE);
    }
    return false;
}

static void se_setUseUserColor(JNIEnv* env, jobject obj, jfloatArray usercolor4f) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setUseUserColor ### spatial name = %s\n", spatial->getSpatialName());
        float* uc = env->GetFloatArrayElements(usercolor4f, 0);
        SE_Vector4f useUserColor = SE_Vector4f(uc[0], uc[1], uc[2], uc[3]);
        spatial->setUserColor(useUserColor.xyz());
        spatial->setUseUserColor(useUserColor.w);
        env->ReleaseFloatArrayElements(usercolor4f, uc, 0);
    }
}

static void se_uvAnimation(JNIEnv* env, jobject obj, jfloatArray texcoordoffset, jboolean enableUVAnimation) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setSpatialEffectAttribute(SE_SpatialAttribute::UVANIMATION, enableUVAnimation);
        if (enableUVAnimation) {
            float* to = env->GetFloatArrayElements(texcoordoffset, 0);
            SE_Vector2f texoffset = SE_Vector2f(to[0], to[1]);
            spatial->setTexCoordOffset(texoffset);
            env->ReleaseFloatArrayElements(texcoordoffset, to, 0);
        }
    }
}

static void se_setLeader(JNIEnv* env, jobject obj, jstring lfname, jint leaderIndex) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setLeader###");
        const char* lName8 = env->GetStringUTFChars(lfname, 0);
        SE_Spatial* leader = SE_Application::getInstance()->getSceneManager()->findSpatialByName(lName8, leaderIndex);
        if (leader) {
            if (_DEBUG)
                LOGD("BORQS##, set leader### fffffname = %s\n", lName8);
            spatial->setLeader(leader);
        }
        env->ReleaseStringUTFChars(lfname, lName8);
    }
}

static jobject se_getLeader(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setLeader###");
        SE_Spatial* leader = spatial->getLeader();
        if (leader) {
            jobject myObject = env->NewObject(classObject, methodInitObject,
                    env->NewStringUTF(leader->getSpatialName()), spatial->getCloneIndex());
            return myObject;
        }
    }
    return NULL;
}

static void se_addFollower(JNIEnv* env, jobject obj, jstring followerName, jint followerIndex) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## addFollower###");
        const char* fName8 = env->GetStringUTFChars(followerName, 0);
        SE_Spatial* follower = SE_Application::getInstance()->getSceneManager()->findSpatialByName(fName8,
                followerIndex);
        if (follower) {
            if (_DEBUG)
                LOGD("BORQS##, add follower### fffffname = %s\n", fName8);
            spatial->addFollower(follower);
        }
        env->ReleaseStringUTFChars(followerName, fName8);
    }
}

static void se_removeFollower(JNIEnv* env, jobject obj, jstring followerName, jint followerIndex) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## test removeFollower###");
        const char* fName8 = env->GetStringUTFChars(followerName, 0);
        SE_Spatial* follower = SE_Application::getInstance()->getSceneManager()->findSpatialByName(fName8,
                followerIndex);
        if (follower) {
            spatial->removeFollower(follower);
        }
        env->ReleaseStringUTFChars(followerName, fName8);
    }
}

static void se_setTexCoordXYReverse(JNIEnv* env, jobject obj, jboolean x, jboolean y) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setTexCoordXYReverse(x, y);
    }
}

static void se_setIsEntirety(JNIEnv* env, jobject obj, jboolean isEntirety) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setIsEntirety(isEntirety);
    }
}

static jboolean se_isObjectSelected(JNIEnv* env, jobject obj, jint X, jint Y, jfloatArray selPoint) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
        const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
        SE_Scene* scene = findScene(sceneName8);
        if (scene) {
            SE_Camera* camera = scene->getCamera();
            if (camera) {
                SE_Ray ray = camera->screenCoordinateToRay(X, Y);
                SE_SimObject* so = NULL;
                if (scene->getSceneManagerType() == DEFAULT) {
                    SE_FindSpatialCollision spatialCollision(ray);
                    spatial->travel(&spatialCollision, true);
                    so = spatialCollision.getCollisionObject();
                    if (so) {
                        SE_Vector3f worldIntersectPoint = spatialCollision.getIntersectPoint();
                        SE_Matrix4f w2m = spatial->getWorldTransform().inverse();
                        SE_Vector3f localIntersectPoint = w2m.map(SE_Vector4f(worldIntersectPoint, 1.0)).xyz();
                        env->SetFloatArrayRegion(selPoint, 0, 3, localIntersectPoint.d);
                        env->ReleaseStringUTFChars(sceneName, sceneName8);
                        return true;
                    }
                }

            }
        }
        env->ReleaseStringUTFChars(sceneName, sceneName8);
    }
    return false;
}
static void se_addChild(JNIEnv* env, jobject obj, jint childObject) {
    if (childObject > 0) {
        SE_Spatial* parentSpatial = findSpatial(env, obj);
        if (parentSpatial) {
            jstring sceneName = (jstring) env->CallObjectMethod(obj, methodGetSceneName);
            const char* sceneName8 = env->GetStringUTFChars(sceneName, 0);
            SE_Scene* scene = findScene(sceneName8);
            if (scene) {
                scene->addSpatial(parentSpatial, (SE_Spatial*) childObject);
            }
            env->ReleaseStringUTFChars(sceneName, sceneName8);
        }
    }
}

static void se_setLayerIndex(JNIEnv* env, jobject obj, jint index) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->updateWorldLayer();
        spatial->getWorldLayer()->setLayer(index);
    }
}

static void se_showRBColor(JNIEnv* env, jobject obj, jboolean show) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setNeedBlackWhiteColor(show);
    }
}

static jboolean se_isExistent(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        return true;
    }
    return false;
}

static void se_setComponentAttribute(JNIEnv* env, jobject obj, jobject componentAttribute) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        SE_SpatialEffectData* spatialEffectData = spatial->getSpatialEffectData();
        spatialEffectData->mSpatialName = spatial->getSpatialName();
        jfloatArray jEffectData = (jfloatArray) env->CallObjectMethod(componentAttribute, methodgetEffectData);
        float* effectData = env->GetFloatArrayElements(jEffectData, 0);
        spatialEffectData->mEffectData = SE_Vector4f(effectData);
        env->ReleaseFloatArrayElements(jEffectData, effectData, 0);
        jstring lightNames = (jstring) env->CallObjectMethod(componentAttribute, methodgetLightNames);
        if (lightNames != NULL) {
            const char* lightNames8 = env->GetStringUTFChars(lightNames, 0);
            char* result = strtok((char*) lightNames8, ",");
            spatialEffectData->mLightsNameList.clear();
            while (result != NULL) {
                spatialEffectData->mLightsNameList.push_back(result);
                result = strtok(NULL, ",");
            }
            env->ReleaseStringUTFChars(lightNames, lightNames8);
        }

        spatialEffectData->mEffectState = env->CallIntMethod(componentAttribute, methodgetStatusValue);
        spatialEffectData->mAlpha = env->CallFloatMethod(componentAttribute, methodgetAlphaValue);
        spatialEffectData->mSpatialState = env->CallIntMethod(componentAttribute, methodgetSpatialDataValue);
        spatial->setNeedForeverBlend(spatial->isNeedForeverBlend());
        spatialEffectData->mShadowColorDensity = env->CallFloatMethod(componentAttribute, methodgetShadowDensity);
        spatialEffectData->mMirrorColorDesity = env->CallFloatMethod(componentAttribute, methodgetMirrorDensity);
    }
}

static jfloatArray se_getSizeOfObject(JNIEnv* env, jobject obj) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        SE_BoundingVolume* moveBV = spatial->getWorldBoundingVolume();
        if (!moveBV) {
            return NULL;
        }
        SE_Vector3f min = SE_Vector3f(1000000, 1000000, 1000000);
        SE_Vector3f max = SE_Vector3f(-1000000, -1000000, -1000000);
        ;
        SE_AABB aabb;
        SE_OBB obb;
        SE_Sphere sphere;
        SE_Vector3f obbPoint[8];
        SE_Vector3f center;
        float radius;
        switch (moveBV->getType()) {
        case AABB:
            aabb = ((SE_AABBBV*) moveBV)->getGeometry();
            min = aabb.getMin();
            max = aabb.getMax();
            break;
        case OBB:
            obb = ((SE_OBBBV*) moveBV)->getGeometry();
            obb.getBoxVertex(obbPoint);
            for (int i = 0; i < 8; i++) {
                if (obbPoint[i].x <= min.x) {
                    min.x = obbPoint[i].x;
                }
                if (obbPoint[i].x >= max.x) {
                    max.x = obbPoint[i].x;
                }
                if (obbPoint[i].y <= min.y) {
                    min.y = obbPoint[i].y;
                }
                if (obbPoint[i].y >= max.y) {
                    max.y = obbPoint[i].y;
                }
                if (obbPoint[i].z <= min.z) {
                    min.z = obbPoint[i].z;
                }
                if (obbPoint[i].z >= max.z) {
                    max.z = obbPoint[i].z;
                }
            }
            break;
        case SPHERE:
            sphere = ((SE_SphereBV*) moveBV)->getGeometry();
            center = sphere.getCenter();
            radius = sphere.getRadius();
            min.x = center.x - radius;
            min.y = center.y - radius;
            min.z = center.z - radius;
            max.x = center.x + radius;
            max.y = center.y + radius;
            max.z = center.z + radius;
            break;
        }
        jfloatArray outValuesArray = env->NewFloatArray(6);
        env->SetFloatArrayRegion(outValuesArray, 0, 3, min.d);
        env->SetFloatArrayRegion(outValuesArray, 3, 3, max.d);
        return outValuesArray;
    }
    return NULL;
}
static void se_setSpatialEffectAttribute(JNIEnv* env, jobject obj, jint value, jboolean enableOrDisable) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setSpatialEffectAttribute ### spatial name = %s\n", spatial->getSpatialName());
        spatial->setSpatialEffectAttribute((SE_SpatialAttribute::SpatialEffect) value, enableOrDisable);
    }
}
static void se_setSpatialStateAttribute(JNIEnv* env, jobject obj, jint value, jboolean enableOrDisable) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setSpatialStateAttribute ### spatial name = %s\n", spatial->getSpatialName());
        spatial->setSpatialStateAttribute((SE_SpatialAttribute::SpatialProperty) value, enableOrDisable);
    }
}
static void se_setSpatialRuntimeAttribute(JNIEnv* env, jobject obj, jint value, jboolean enableOrDisable) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        if (_DEBUG)
            LOGD("BORQS## setSpatialRuntimeAttribute ### spatial name = %s\n", spatial->getSpatialName());
        spatial->setSpatialRuntimeAttribute((SE_SpatialAttribute::SpatialRuntimeStatus) value, enableOrDisable);
    }
}

static void se_setMirrorColorDesity(JNIEnv* env, jobject obj, jfloat value) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setMirrorColorDesity(value);
    }
}

static void se_setShadowColorDesity(JNIEnv* env, jobject obj, jfloat value) {
    SE_Spatial* spatial = findSpatial(env, obj);
    if (spatial) {
        spatial->setShadowColorDensity(value);
    }
}
static const char *classPathName = "com/borqs/se/engine/SEObject";

static JNINativeMethod methods[] = {
  {"setName_JNI","(Ljava/lang/String;)V",(void*)se_setName},
  {"addUserObject_JNI", "()V", (void*)se_addUserObject},
  {"loadImageData_JNI", "(Ljava/lang/String;)I",(void*)se_loadImageData},
  {"loadImageData_JNI", "(Landroid/graphics/Bitmap;)I",(void*)se_loadImageData_II},
  {"addImageData_JNI", "(Ljava/lang/String;I)V",(void*)se_addImageData},
  {"releaseImageData_JNI","(I)V",(void*)se_releaseImageData},
  {"getImageName_JNI", "()Ljava/lang/String;",(void*)se_getImageName},
  {"getImageKey_JNI", "(Ljava/lang/String;)Ljava/lang/String;",(void*)se_getImageKey},
  {"applyImage_JNI","(Ljava/lang/String;Ljava/lang/String;)V",(void*)se_applyImage},
  {"updateVertex_JNI", "([F)V", (void*)se_updateVertex},
  {"updateTexture_JNI","([F)V", (void*)se_updateTexture},
  {"removeImageData_JNI","(Ljava/lang/String;)V",(void*)se_removeImageData},
  {"applyLight_JNI", "(Ljava/lang/String;)V", (void*)se_applyLight},
  {"unApplyLight_JNI", "(Ljava/lang/String;)V", (void*)se_unApplyLight},
  {"changeParent_JNI", "(Ljava/lang/String;I)V", (void*)se_changeParent},
  {"remove_JNI", "()I", (void*)se_remove},
  {"release_JNI", "()V", (void*)se_release},
  {"setVisible_JNI", "(Z)V", (void*)se_setVisible},
  {"setAlpha_JNI", "(F)V", (void*)se_setAlpha},
  {"cloneObject_JNI","(Lcom/borqs/se/engine/SEObject;I)Z",(void*)se_cloneObject},

  {"setUserRotate_JNI", "([F)V", (void*)se_setUserRotate},
  {"setUserTranslate_JNI", "([F)V", (void*)se_setUserTranslate},
  {"setUserScale_JNI", "([F)V", (void*)se_setUserScale},
  {"setUserTransParas_JNI", "([F[F[F)V", (void*)se_setUserTransParas},

  {"setLocalRotate_JNI", "([F)V", (void*)se_setLocalRotate},
  {"setLocalTranslate_JNI", "([F)V", (void*)se_setLocalTranslate},
  {"setLocalScale_JNI", "([F)V", (void*)se_setLocalScale},
  {"setLocalTransParas_JNI", "([F[F[F)V", (void*)se_setLocalTransParas},

  {"isEnableBlend_JNI", "()Z", (void*)se_isEnableBlend},
  {"setUseUserColor_JNI", "([F)V", (void*)se_setUseUserColor},
  {"uvAnimation_JNI", "([FZ)V", (void*)se_uvAnimation},
  {"setLeader_JNI", "(Ljava/lang/String;I)V", (void*)se_setLeader},
  {"getLeader_JNI", "()Lcom/borqs/se/engine/SEObject;", (void*)se_getLeader},
  {"removeFollower_JNI", "(Ljava/lang/String;I)V", (void*)se_removeFollower},
  {"addFollower_JNI", "(Ljava/lang/String;I)V", (void*)se_addFollower},
  
  {"setTexCoordXYReverse_JNI", "(ZZ)V", (void*)se_setTexCoordXYReverse},
  {"isObjectSelected_JNI","(II[F)Z", (void*)se_isObjectSelected},
  {"setIsEntirety_JNI","(Z)V", (void*)se_setIsEntirety},
  {"addChild_JNI","(I)V", (void*)se_addChild},
  {"setLayerIndex_JNI","(I)V", (void*)se_setLayerIndex},
  {"showRBColor_JNI", "(Z)V", (void*)se_showRBColor},
  {"isImageExist_JNI", "(Ljava/lang/String;)Z", (void*)se_isImageExist},
  {"worldToLocal_JNI","([F)[F", (void*)se_worldToLocal},
  {"localToWorld_JNI","([F)[F", (void*)se_localToWorld},
  {"localToScreenCoordinate_JNI","([F)[F", (void*)se_localToScreenCoordinate},
  {"isExistent_JNI","()Z", (void*)se_isExistent},

  {"setComponentAttribute_JNI", "(Lcom/borqs/se/engine/SEComponentAttribute;)V", (void*)se_setComponentAttribute},
  {"getSizeOfObject_JNI", "()[F", (void*)se_getSizeOfObject},
  {"setSpatialEffectAttribute_JNI", "(IZ)V", (void*)se_setSpatialEffectAttribute},
  {"setSpatialStateAttribute_JNI", "(IZ)V", (void*)se_setSpatialStateAttribute},
  {"setSpatialRuntimeAttribute_JNI", "(IZ)V", (void*)se_setSpatialRuntimeAttribute},
  {"setMirrorColorDesity_JNI", "(F)V", (void*)se_setMirrorColorDesity},
  {"setShadowColorDesity_JNI", "(F)V", (void*)se_setShadowColorDesity},
};

static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        fprintf(stderr,
            "Native registration unable to find class '%s'\n", className);
        return JNI_FALSE;
    }
    classObject = (jclass)env->NewGlobalRef(clazz);
    methodInitObject = env->GetMethodID(classObject, "<init>", "(Ljava/lang/String;I)V");

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    nativeBitmapID = env->GetFieldID(bitmapClass, "mNativeBitmap", "I");
    methodGetParentName = env->GetMethodID(clazz, "getParentName", "()Ljava/lang/String;");
    methodGetParentIndex = env->GetMethodID(clazz, "getParentIndex", "()I");
    methodGetSceneName = env->GetMethodID(clazz, "getSceneName", "()Ljava/lang/String;");
    objectNameID = env->GetFieldID(clazz, "mName", "Ljava/lang/String;");
    objectIndexID = env->GetFieldID(clazz, "mIndex", "I");
    isEntiretyID = env->GetFieldID(clazz, "mIsEntirety", "Z");
    isNodeID = env->GetFieldID(clazz, "mIsNode", "Z");
    spatialEffectID = env->GetFieldID(clazz, "mSpatialEffect", "I");
    spatialStateID = env->GetFieldID(clazz, "mSpatialState", "I");
    spatialRuntimeID = env->GetFieldID(clazz, "mSpatialRuntime", "I");
    localTransParasID = env->GetFieldID(clazz, "mLocalTransParas", "Lcom/borqs/se/engine/SETransParas;");
    userTransParasID = env->GetFieldID(clazz, "mUserTransParas", "Lcom/borqs/se/engine/SETransParas;");
    bvTypeID = env->GetFieldID(clazz, "mBVType", "I");
    layerIndexID = env->GetFieldID(clazz, "mLayerIndex", "I");
    methodGetVertexArray = env->GetMethodID(clazz, "getVertexArray", "()[F");
    methodGetTexVertexArray = env->GetMethodID(clazz, "getTexVertexArray", "()[F");
    methodGetFaceArray = env->GetMethodID(clazz, "getFaceArray", "()[I");
    methodGetAlpha = env->GetMethodID(clazz, "getAlpha", "()F");
    methodGetColor = env->GetMethodID(clazz, "getColor", "()[F");

    methodGetImageName = env->GetMethodID(clazz, "getImageName", "()Ljava/lang/String;");
    methodGetImageType = env->GetMethodID(clazz, "getImageType", "()I");

    methodGetLineWidth = env->GetMethodID(clazz, "getLineWidth", "()F");


    jclass transParasClass =  env->FindClass("com/borqs/se/engine/SETransParas");
    methodGetTranslate = env->GetMethodID(transParasClass, "getTranslate", "()[F");
    methodGetRotate = env->GetMethodID(transParasClass, "getRotate", "()[F");
    methodGetScale = env->GetMethodID(transParasClass, "geScale", "()[F");


    jclass componentAttribute =  env->FindClass("com/borqs/se/engine/SEComponentAttribute");
    methodgetEffectData = env->GetMethodID(componentAttribute, "getEffectData", "()[F");
    methodgetAlphaValue = env->GetMethodID(componentAttribute, "getAlphaValue", "()F");
    methodgetMirrorDensity = env->GetMethodID(componentAttribute, "getMirrorColorDesity", "()F");
    methodgetShadowDensity = env->GetMethodID(componentAttribute, "getShadowColorDesity", "()F");
    methodgetStatusValue = env->GetMethodID(componentAttribute, "getStatusValue", "()I");
    methodgetLightNames = env->GetMethodID(componentAttribute, "getLightNames", "()Ljava/lang/String;");
    methodgetSpatialDataValue= env->GetMethodID(componentAttribute, "getSpatialDataValue", "()I");
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


int register_com_android_se_SEObject(JNIEnv* env)
{
    return registerNatives(env);
}
