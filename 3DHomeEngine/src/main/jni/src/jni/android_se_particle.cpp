#ifdef NDK
#include <jni.h>
#include <string>
#else
#include <nativehelper/jni.h>
#include <utils/Log.h>
#include <utils/String8.h>
#include <utils/String16.h>
#endif

#include <stdio.h>
#include <assert.h>
#include <dlfcn.h>
#include <GLES2/gl2.h>
#include "SE_Log.h"

#include "SE_Application.h"
#include "SE_Camera.h"
#include "SE_Vector.h"
#include "ParticleSystem/SE_ParticleSystemManager.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_ParticleBoxEmitter.h"
#include "SE_ResourceManager.h"
#include "SE_SceneManager.h"


static jfieldID imagePathID = 0;
static jfieldID quotaID = 0;
static jfieldID nameID = 0;
static jfieldID emitterTypeID = 0;
static jmethodID methodGetParticleDimentionsID = 0;

static jfieldID emitterAngleID = 0;
static jfieldID boxEmitterSizeID = 0;
static jfieldID emitterPositionID = 0;
static jfieldID emitterColorValueID = 0;
static jfieldID emitterEmissionRateID = 0;
static jmethodID methodGetEmitterDirectionID = 0;
static jmethodID methodGetEmitterParticleVelocityID = 0;
static jmethodID methodGetEmitterTimeToLiveID = 0;
static jfieldID emitterTranslateID = 0;
static jfieldID emitterRotateID = 0;
static jfieldID emitterScaleID = 0;

static jfieldID isLinerforceAffectorEnableID = 0;
static jmethodID methodGetForceVectorID = 0;

static jfieldID isColorFaderAffectorEnableID = 0;
static jfieldID colorFaderAdjustID = 0;

static jfieldID isColourInterpolatorAffectorEnableID = 0;
static jmethodID methodGetTimeAdjustID = 0;
static jmethodID methodGetColorAdjustID = 0;
static jmethodID methodGetColorValueID = 0;

static jfieldID isNeedDepthTestID = 0;
static jfieldID layerIndexID = 0;

static jfieldID attachObjNameID = 0;
static jfieldID attachObjIndexID = 0;

/**
 *create a particle object
*/
static void se_addParticle(JNIEnv* env, jobject obj)
{
	jstring nameStr = (jstring)env->GetObjectField(obj, nameID);
	const char* name = env->GetStringUTFChars(nameStr, NULL);
	ParticleSystemManager* psm = SE_Application::getInstance()->getParticleSystemManager();
	ParticleSystem* ps = psm->createParticleSystem(name);
	if (!ps) {
		env->ReleaseStringUTFChars(nameStr, name);
		return;
	}

	//set particle system attribute
	int quota = env->GetIntField(obj, quotaID);
	ps->setParticleQuota(quota);

	float* dimentions;
	jfloatArray dimArray = (jfloatArray)env->CallObjectMethod(obj, methodGetParticleDimentionsID);
	dimentions = env->GetFloatArrayElements(dimArray, 0);
	ps->setDefaultDimensions(dimentions[0], dimentions[1]);
	env->ReleaseFloatArrayElements(dimArray, dimentions, 0);

	jstring imgStr = (jstring)env->GetObjectField(obj, imagePathID);
	const char* imgPath = env->GetStringUTFChars(imgStr, NULL);

        SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
       // resourceManager->insertPathImageData(imgPath, NULL, false);  
       // resourceManager->setIdPath(imgPath, imgPath, true);

	ps->setImagePath(imgPath);
    
	env->ReleaseStringUTFChars(imgStr, imgPath);

	//set emitter type and attribute
	jstring emitterTypeStr = (jstring)env->GetObjectField(obj, emitterTypeID);
	const char* emitterType = env->GetStringUTFChars(emitterTypeStr, NULL);
	ParticleEmitter* emitter;
	emitter = ps->addEmitter(emitterType);
	env->ReleaseStringUTFChars(emitterTypeStr, emitterType);

	emitter->setEnabled(true);
	float emitterAngle = env->GetFloatField(obj, emitterAngleID);
	psm->setEmitterAttribute(emitter, ParticleAttritute(ANGLE), emitterAngle, 0, 0, 0);

	jfloatArray boxSizeArray = (jfloatArray)env->GetObjectField(obj, boxEmitterSizeID);
	float* boxSize = env->GetFloatArrayElements(boxSizeArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(BOXWIDTH), boxSize[0], 0, 0, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(BOXHEIGHT), 0, boxSize[1], 0, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(BOXDEPTH), 0, 0, boxSize[2], 0);
	env->ReleaseFloatArrayElements(boxSizeArray, boxSize, 0);

	jfloatArray emitterPositionArray = (jfloatArray)env->GetObjectField(obj, emitterPositionID);
	float* emitterPosition = env->GetFloatArrayElements(emitterPositionArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(POSITION), emitterPosition[0], emitterPosition[1], emitterPosition[2], 0);
	env->ReleaseFloatArrayElements(emitterPositionArray, emitterPosition, 0);

	jfloatArray emitterColorArray = (jfloatArray)env->GetObjectField(obj, emitterColorValueID);
	float* emitterColor = env->GetFloatArrayElements(emitterColorArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(COLOUR), emitterColor[0], emitterColor[1], emitterColor[2], emitterColor[3]);
	env->ReleaseFloatArrayElements(emitterColorArray, emitterColor, 0);

	float emitterEmissionRate = env->GetFloatField(obj, emitterEmissionRateID);
	psm->setEmitterAttribute(emitter, ParticleAttritute(EMISSIONRATE), emitterEmissionRate, 0, 0, 0);

	jfloatArray emitterDirecArray = (jfloatArray)env->CallObjectMethod(obj, methodGetEmitterDirectionID);
	float* emitterDirec = env->GetFloatArrayElements(emitterDirecArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(DIRECTION), emitterDirec[0], emitterDirec[1], emitterDirec[2], 0);
	env->ReleaseFloatArrayElements(emitterDirecArray, emitterDirec, 0);

	jfloatArray emitterPVArray = (jfloatArray)env->CallObjectMethod(obj, methodGetEmitterParticleVelocityID);
	float* emitterPV = env->GetFloatArrayElements(emitterPVArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(PARTICLEVELOCITY), emitterPV[0], emitterPV[1], 0, 0);
	env->ReleaseFloatArrayElements(emitterPVArray, emitterPV, 0);

	jfloatArray emitterTTLArray = (jfloatArray)env->CallObjectMethod(obj, methodGetEmitterTimeToLiveID);
	float* emitterTTL = env->GetFloatArrayElements(emitterTTLArray, 0);
	psm->setEmitterAttribute(emitter, ParticleAttritute(TIMETOLIVE), emitterTTL[0], emitterTTL[1], 0, 0);
	env->ReleaseFloatArrayElements(emitterTTLArray, emitterTTL, 0);

	//add affector and set attributes of affector
	bool isLinerforceAffectorEnable = env->GetBooleanField(obj, isLinerforceAffectorEnableID);
	if (isLinerforceAffectorEnable)
	{
		String affectorType = "LinearForce";
		ParticleAffector* affector = ps->addAffector(affectorType);

		jfloatArray forceVectorArray = (jfloatArray)env->CallObjectMethod(obj, methodGetForceVectorID);
		float* forceVector = env->GetFloatArrayElements(forceVectorArray, 0);
		psm->setAffectorAttribute(affector, ParticleAttritute(FORCEVECTOR), forceVector[0], forceVector[1], forceVector[2], 0, 0);
		env->ReleaseFloatArrayElements(forceVectorArray, forceVector, 0);
	}

	bool isColorFaderAffectorEnable = env->GetBooleanField(obj, isColorFaderAffectorEnableID);
	if (isColorFaderAffectorEnable)
	{
		String affectorType = "ColourFader";
		ParticleAffector* affector = ps->addAffector(affectorType);

		jfloatArray colorFaderAdjustArray = (jfloatArray)env->GetObjectField(obj, colorFaderAdjustID);
		float* colorFaderAdjust = env->GetFloatArrayElements(colorFaderAdjustArray, 0);
		psm->setAffectorAttribute(affector, ParticleAttritute(COLOURFADERADJUST), colorFaderAdjust[0], colorFaderAdjust[1], colorFaderAdjust[2], colorFaderAdjust[3], 0);
		env->ReleaseFloatArrayElements(colorFaderAdjustArray, colorFaderAdjust, 0);
	}

	bool isColourInterpolatorAffectorEnable = env->GetBooleanField(obj, isColourInterpolatorAffectorEnableID);
	if (isColourInterpolatorAffectorEnable)
	{
		String affectorType = "ColourInterpolator";
		ParticleAffector* affector = ps->addAffector(affectorType);

		jfloatArray timeAdjustArray = (jfloatArray)env->CallObjectMethod(obj, methodGetTimeAdjustID);
		float* timeAdjust = env->GetFloatArrayElements(timeAdjustArray, 0);
		int timeSize = env->GetArrayLength(timeAdjustArray);

		jobjectArray colorAdjustArray = (jobjectArray)env->CallObjectMethod(obj, methodGetColorAdjustID);
		int colorSize = env->GetArrayLength(colorAdjustArray);

		if ((timeSize == colorSize) && (timeSize > 0)) {
			for (int i = 0; i < timeSize; i ++)
			{
				jobject colorAdjust = env->GetObjectArrayElement(colorAdjustArray, i);
				jfloatArray colorValueArray = (jfloatArray)env->CallObjectMethod(colorAdjust, methodGetColorValueID);
				float* color = env->GetFloatArrayElements(colorValueArray, 0);
				psm->setAffectorAttribute(affector, ParticleAttritute(COLOURINTERPOLATORADJUST), color[0], color[1], color[2], color[3], i);
				psm->setAffectorAttribute(affector, ParticleAttritute(TIMEADJUST), timeAdjust[i], 0, 0, 0, i);
				env->ReleaseFloatArrayElements(colorValueArray, color, 0);
				env->DeleteLocalRef(colorAdjust);
			}
		}
		env->DeleteLocalRef(colorAdjustArray);
		env->ReleaseFloatArrayElements(timeAdjustArray, timeAdjust, 0);
	}

	bool isNeedDepthTest = env->GetBooleanField(obj, isNeedDepthTestID);
	int layerIndex = env->GetIntField(obj, layerIndexID);

	jfloatArray emitterTranslateArray = (jfloatArray)env->GetObjectField(obj, emitterTranslateID);
    float* emitterTranslate = env->GetFloatArrayElements(emitterTranslateArray, 0);
    SE_Vector3f newTranslate = SE_Vector3f(emitterTranslate[0],emitterTranslate[1],emitterTranslate[2]);
    ps->setTranslate(newTranslate);
    env->ReleaseFloatArrayElements(emitterTranslateArray, emitterTranslate, 0);

	jfloatArray emitterRotateArray = (jfloatArray)env->GetObjectField(obj, emitterRotateID);
    float* emitterRotate = env->GetFloatArrayElements(emitterRotateArray, 0);
    SE_Quat newRotate;
    newRotate.set(emitterRotate[0], SE_Vector3f(emitterRotate[1], emitterRotate[2], emitterRotate[3]));
    ps->setRotate(newRotate);
    env->ReleaseFloatArrayElements(emitterRotateArray, emitterRotate, 0);

	jfloatArray emitterScaleArray = (jfloatArray)env->GetObjectField(obj, emitterScaleID);
    float* emitterScale = env->GetFloatArrayElements(emitterScaleArray, 0);
    SE_Vector3f newScale= SE_Vector3f(emitterScale[0],emitterScale[1],emitterScale[2]);
    ps->setScale(newScale);

    jstring attachObjNameStr = (jstring)env->GetObjectField(obj, attachObjNameID);
    if (attachObjNameStr) {
        const char* attachObjName = env->GetStringUTFChars(attachObjNameStr, NULL);
        int attachObjIndex = env->GetIntField(obj, attachObjIndexID);
        SE_Spatial* spatial = SE_Application::getInstance()->getSceneManager()->getMainScene()->findSpatialByName(attachObjName, attachObjIndex);
        if (spatial) {
        	ps->setAttachedSpatial(spatial);
        	ps->setIsAttached(true);
        }
        env->ReleaseStringUTFChars(attachObjNameStr, attachObjName);
    }
    env->ReleaseFloatArrayElements(emitterScaleArray, emitterScale, 0);

	psm->addParticleSystem(name, ps, isNeedDepthTest, layerIndex);
	env->ReleaseStringUTFChars(nameStr, name);
}

/**
 *delete a particle object
*/
static void se_deleteParticleObject(JNIEnv* env, jobject clazz) {
    jstring nameString = (jstring)env->GetObjectField(clazz, nameID);
    const char* name = env->GetStringUTFChars(nameString, NULL);
    ParticleSystemManager* particleSystemManager =  SE_Application::getInstance()->getParticleSystemManager();
    particleSystemManager->destroyParticleSystem(name);
    env->ReleaseStringUTFChars(nameString, name);
}



static const char *classPathName = "com/borqs/se/engine/SEParticleSystem";

static JNINativeMethod methods[] = {
    {"addParticle_JNI", "()V", (void*)se_addParticle},
    {"deleteParticleObject_JNI", "()V", (void*)se_deleteParticleObject},
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

    jclass particleClass =  env->FindClass("com/borqs/se/engine/SEParticleSystem");
    imagePathID = env->GetFieldID(particleClass, "mImagePath", "Ljava/lang/String;");
    quotaID = env->GetFieldID(particleClass, "mParticleQuota", "I");
    nameID = env->GetFieldID(particleClass, "mParticleSystemName", "Ljava/lang/String;");
    emitterTypeID = env->GetFieldID(particleClass, "mEmitterType", "Ljava/lang/String;");
    methodGetParticleDimentionsID = env->GetMethodID(particleClass, "getParticleDimentions", "()[F");

    emitterAngleID = env->GetFieldID(particleClass, "mEmitterAngle", "F");
    boxEmitterSizeID = env->GetFieldID(particleClass, "mBoxEmitterSize", "[F");
    emitterPositionID = env->GetFieldID(particleClass, "mEmitterPosition", "[F");
    emitterColorValueID = env->GetFieldID(particleClass, "mEmitterColorValue", "[F");
    emitterEmissionRateID = env->GetFieldID(particleClass, "mEmitterEmissionRate", "F");
    methodGetEmitterDirectionID = env->GetMethodID(particleClass, "getEmitterDirection", "()[F");
    methodGetEmitterParticleVelocityID = env->GetMethodID(particleClass, "getEmitterParticleVelocity", "()[F");
    methodGetEmitterTimeToLiveID = env->GetMethodID(particleClass, "getEmitterTimeToLive", "()[F");
    emitterTranslateID = env->GetFieldID(particleClass, "mTranslate", "[F");
    emitterRotateID = env->GetFieldID(particleClass, "mRotate", "[F");
    emitterScaleID = env->GetFieldID(particleClass, "mScale", "[F");

    isLinerforceAffectorEnableID = env->GetFieldID(clazz, "mLinerforceAffectorEnable", "Z");
    methodGetForceVectorID = env->GetMethodID(particleClass, "getFoceVector", "()[F");

    isColorFaderAffectorEnableID = env->GetFieldID(clazz, "mColourFaderAffectorEnable", "Z");
    colorFaderAdjustID = env->GetFieldID(particleClass, "mColorFaderAdjust", "[F");

    isColourInterpolatorAffectorEnableID = env->GetFieldID(clazz, "mColourInterpolatorAffectorEnable", "Z");
    methodGetTimeAdjustID = env->GetMethodID(particleClass, "getTimeAdjust", "()[F");
    methodGetColorAdjustID = env->GetMethodID(particleClass, "getColorAdjust", "()[Lcom/borqs/se/engine/SEParticleSystem$ColorValue;");
    jclass colorValueClass =  env->FindClass("com/borqs/se/engine/SEParticleSystem$ColorValue");
    methodGetColorValueID = env->GetMethodID(colorValueClass, "getColorValue", "()[F");

    isNeedDepthTestID = env->GetFieldID(clazz, "mNeedDepthTest", "Z");
    layerIndexID = env->GetFieldID(particleClass, "mLayerIndex", "I");
    attachObjNameID  = env->GetFieldID(clazz, "mAttachObjName", "Ljava/lang/String;");
    attachObjIndexID  = env->GetFieldID(clazz, "mAttachObjIndex", "I");
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

int register_com_android_se_SEParticleSystem(JNIEnv* env)
{
    return registerNatives(env);
}
