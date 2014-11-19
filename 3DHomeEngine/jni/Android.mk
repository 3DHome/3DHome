LOCAL_PATH := $(call my-dir)
#for DevIL


#build libpng
include $(CLEAR_VARS)
png_SRC_FILES := ./src/libpng/png.c \
	./src/libpng/pngerror.c \
	./src/libpng/pnggccrd.c \
	./src/libpng/pngget.c \
	./src/libpng/pngmem.c \
	./src/libpng/pngpread.c \
	./src/libpng/pngread.c \
	./src/libpng/pngrio.c \
	./src/libpng/pngrtran.c \
	./src/libpng/pngrutil.c \
	./src/libpng/pngset.c \
	./src/libpng/pngtrans.c \
	./src/libpng/pngvcrd.c \
	./src/libpng/pngwio.c \
	./src/libpng/pngwrite.c \
	./src/libpng/pngwtran.c \
	./src/libpng/pngwutil.c
png_CFLAGS := -fvisibility=hidden ## -fomit-frame-pointer

# For the device
# =====================================================

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := $(png_SRC_FILES)

$(info $(LOCAL_SRC_FILES))
LOCAL_CFLAGS += $(png_CFLAGS)
#LOCAL_C_INCLUDES += external/zlib
#LOCAL_SHARED_LIBRARIES := libz
LOCAL_EXPORT_LDLIBS := -lz
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/src/libpng
LOCAL_MODULE:= mypng
include $(BUILD_STATIC_LIBRARY)

#build jpeg
include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm

JPEG_SRC_FILES := \
	jcapimin.c jcapistd.c jccoefct.c jccolor.c jcdctmgr.c jchuff.c \
	jcinit.c jcmainct.c jcmarker.c jcmaster.c jcomapi.c jcparam.c \
	jcphuff.c jcprepct.c jcsample.c jctrans.c jdapimin.c jdapistd.c \
	jdatadst.c jdatasrc.c jdcoefct.c jdcolor.c jddctmgr.c jdhuff.c \
	jdinput.c jdmainct.c jdmarker.c jdmaster.c jdmerge.c jdphuff.c \
	jdpostct.c jdsample.c jdtrans.c jerror.c jfdctflt.c jfdctfst.c \
	jfdctint.c jidctflt.c jidctfst.c jidctint.c jidctred.c jquant1.c \
	jquant2.c jutils.c jmemmgr.c armv6_idct.S jmem-android.c

LOCAL_SRC_FILES := $(addprefix ./src/libjpeg/,$(JPEG_SRC_FILES))
$(info $(LOCAL_SRC_FILES))
LOCAL_CFLAGS += -DAVOID_TABLES 
LOCAL_CFLAGS += -O3 -fstrict-aliasing -fprefetch-loop-arrays
LOCAL_MODULE := myjpeg

include $(BUILD_STATIC_LIBRARY)

#build devIL
include $(CLEAR_VARS)
DEV_IL_PATH := $(LOCAL_PATH)/src/devIL/src-IL/src
DEV_IL_INCLUDE := $(LOCAL_PATH)/src/devIL/include \
                  $(LOCAL_PATH)/src/devIL/src-IL/include

LS_C=$(subst $(1)/, ./src/devIL/src-IL/src/,$(wildcard $(1)/*.c))
LS_CPP=$(subst $(1)/, ./src/devIL/src-IL/src/,$(wildcard $(1)/*.cpp))

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/libpng \
		    $(LOCAL_PATH)/src/libjpeg \
		    $(DEV_IL_INCLUDE)
$(info $(LOCAL_C_INCLUDES))
DEV_IL_SRC_FILES := $(call LS_C,$(DEV_IL_PATH))
LOCAL_SRC_FILES := $(DEV_IL_SRC_FILES)
LOCAL_STATIC_LIBRARIES := mypng myjpeg
$(info $(LOCAL_SRC_FILES))
LOCAL_MODULE := libdevIL
LOCAL_ARM_MODE := arm

#LOCAL_LDLIBS += -lmypng -L/Users/chenyong/SourceCode/3dhome_newversion/SEHome/obj/local/armeabi/Users/chenyong/SourceCode/3dhome_newversion/SEHome/obj/local/armeabi
include $(BUILD_STATIC_LIBRARY)


#build factory lib
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := tests

LOCAL_CFLAGS += -DDEBUG -DNDK
LOCAL_C_INCLUDES += \
        $(LOCAL_PATH)/src\
        $(LOCAL_PATH)/src/external
        	
					
LOCAL_SRC_FILES:= ./src/external/SE_Factory.cpp     

LOCAL_LDLIBS := -L$(call host-path, $(TOP_LOCAL_PATH)/libs) \
								-lz -llog
LOCAL_SHARED_LIBRARIES := 

LOCAL_MODULE := libsefactory

LOCAL_ARM_MODE := arm

LOCAL_PRELINK_MODULE := false 

include $(BUILD_SHARED_LIBRARY)



include $(CLEAR_VARS)

LOCAL_CFLAGS += -DGLES_20 -DSE_HAS_THREAD -DSE_HAS_MUTEX -DNDK -D_DEBUG=0
LOCAL_C_INCLUDES += \
        $(LOCAL_PATH)/src \
		$(LOCAL_PATH)/src/command \
        $(LOCAL_PATH)/src/export \
        $(LOCAL_PATH)/src/external \
		$(LOCAL_PATH)/src/2D \
		$(LOCAL_PATH)/src/tinyxml \
		$(LOCAL_PATH)/src/animation \
		$(LOCAL_PATH)/src/ParticleSystem \
		$(LOCAL_PATH)/src/skia/core \
		$(LOCAL_PATH)/src/skia/images \
		$(LOCAL_PATH)/src/minizip \
		$(LOCAL_PATH)/src/GUI \
                $(LOCAL_PATH)/src/devIL/include
					
SE_SRC_FILES:= SE_Buffer.cpp   \
	SE_DataTransfer.cpp  \
	SE_ImageData.cpp      \
	SE_Mesh.cpp           \
	SE_ResFileHeader.cpp   \
	SE_Camera.cpp         \
	SE_File.cpp          \
	SE_IO.cpp            \
	SE_MeshSimObject.cpp  \
	SE_ResourceManager.cpp \
	SE_TextureCoordData.cpp \
	SE_Command.cpp         \
	SE_Geometry3D.cpp    \
	SE_Log.cpp           \
	SE_Object.cpp         \
	SE_ObjectManager.cpp \
	SE_Layer.cpp \
	SE_SceneManager.cpp    \
	SE_Texture.cpp          \
	SE_CommandFactory.cpp  \
	SE_Geometry.cpp \
	SE_MaterialData.cpp    \
	SE_Quat.cpp   \
	SE_ShaderProgram.cpp \
	SE_Time.cpp          \
	SE_GeometryData.cpp  \
	SE_Application.cpp    \
	SE_Common.cpp          \
	SE_Math.cpp     \
	SE_RenderManager.cpp  \
	SE_SimObject.cpp   \
	SE_PropertySet.cpp \
	SE_Utils.cpp \
	SE_BoundingVolume.cpp   \
	SE_CommonNode.cpp    \
	SE_ID.cpp         \
	SE_Matrix.cpp     \
	SE_RenderUnit.cpp  \
	SE_Spatial.cpp    \
	SE_Vector.cpp \
	SE_AnimationManager.cpp \
	SE_Primitive.cpp \
	SE_RenderState.cpp \
	SE_Struct.cpp \
	SE_ImageCodec.cpp \
	SE_SimObjectManager.cpp \
	SE_MessageDefine.cpp \
	SE_DataValueDefine.cpp \
	SE_Renderer.cpp \
	SE_RenderTarget.cpp \
	SE_RenderTargetManager.cpp \
	SE_ShaderProperty.cpp \
	SE_TreeStructManager.cpp \
	SE_Thread.cpp \
	SE_Mutex.cpp \
	SE_DynamicArray.cpp \
	SE_ThreadManager.cpp \
	SE_DelayDestroy.cpp \
	SE_Timer.cpp \
        SE_Octree.cpp \
        SE_OctreeNode.cpp \
        SE_OctreeSceneManager.cpp \
	./command/SE_SystemCommand.cpp \
	./command/SE_SystemCommandFactory.cpp \
	./command/SE_UserCommand.cpp \
	./command/SE_LoadThread.cpp \
	./animation/SE_Animation.cpp \
    ./animation/SE_AnimationSet.cpp \
	./animation/SE_BipedAnimation.cpp  \
	./animation/SE_AnimationListener.cpp  \
	./animation/SE_Interpolate.cpp   \
	./animation/SE_KeyFrameController.cpp \
	./animation/SE_KeyFrame.cpp  \
	./animation/SE_SpatialAnimation.cpp \
	./animation/SE_Bone.cpp \
	./animation/SE_SkinJointController.cpp \
        ./animation/SE_BipedController.cpp \
	./tinyxml/tinystr.cpp  \
	./tinyxml/tinyxml.cpp  \
	./tinyxml/tinyxmlerror.cpp  \
	./tinyxml/tinyxmlparser.cpp \
	SE_InputEvent.cpp \
    SE_InputManager.cpp \
    SE_MotionEventCamera.cpp \
SE_SpatialTravel.cpp \
    ./jni/android_se_camera.cpp \
    ./jni/android_se_object.cpp \
    ./jni/android_se_particle.cpp \
    ./jni/android_se_sceneManager.cpp \
    ./jni/android_se_scene.cpp \
    ./jni/android_se_XMLAnimation.cpp \
    ./export/SE_Ase.cpp \
    SE_AssetManager.cpp \
    SE_LodNode.cpp \
		SE_VertexBuffer.cpp \
	SE_RefBase.cpp \
		./ParticleSystem/SE_Particle.cpp \
		./ParticleSystem/SE_ParticleAreaEmitter.cpp \
		./ParticleSystem/SE_ParticleBillboardSet.cpp \
		./ParticleSystem/SE_ParticleBillboard.cpp \
		./ParticleSystem/SE_ParticleBillboardRenderer.cpp \
		./ParticleSystem/SE_ParticleBoxEmitter.cpp \
		./ParticleSystem/SE_ParticleColorValue.cpp \
		./ParticleSystem/SE_ParticleColourFaderAffector.cpp \
		./ParticleSystem/SE_ParticleColourFaderAffector2.cpp \
		./ParticleSystem/SE_ParticleColourInterpolatorAffector.cpp \
		./ParticleSystem/SE_ParticleCylinderEmitter.cpp \
		./ParticleSystem/SE_ParticleDeflectorPlaneAffector.cpp \
		./ParticleSystem/SE_ParticleDirectionRandomiserAffector.cpp \
		./ParticleSystem/SE_ParticleEllipsoidEmitter.cpp \
		./ParticleSystem/SE_ParticleEmitter.cpp \
		./ParticleSystem/SE_ParticleHollowEllipsoidEmitter.cpp \
		./ParticleSystem/SE_ParticleLinearForceAffector.cpp \
		./ParticleSystem/SE_ParticleMath.cpp \
		./ParticleSystem/SE_ParticlePoinEmitter.cpp \
		./ParticleSystem/SE_ParticleQuaternion.cpp \
		./ParticleSystem/SE_ParticleRingEmitter.cpp \
		./ParticleSystem/SE_ParticleRotationAffector.cpp \
		./ParticleSystem/SE_ParticleScaleAffector.cpp \
		./ParticleSystem/SE_ParticleSystem.cpp \
		./ParticleSystem/SE_ParticleSystemManager.cpp \
		./ParticleSystem/SE_ParticleVector3.cpp \
		./ParticleSystem/SE_ParticleMatrix3.cpp \
		./ParticleSystem/SE_ParticleFollowAttachedPointPositionAffector.cpp \
        ./minizip/ioapi.c \
        ./minizip/unzip.c \
        SE_Config.cpp \
	    SE_Value.cpp \
	    SE_RenderSystemCapabilities.cpp \
	    ./export/SE_ShaderDefine.cpp \
      ./export/SE_RenderDefine.cpp \
        SE_PluginsManager.cpp \
        SE_DynamicLib.cpp \
        SE_DynamicLibManager.cpp \
	SE_Light.cpp    \
	SE_SpatialManager.cpp \
	SE_RenderUnitManager.cpp

LOCAL_SRC_FILES := $(addprefix ./src/,$(SE_SRC_FILES))
    

LOCAL_LDLIBS := -L$(call host-path, $(TOP_LOCAL_PATH)/libs) \
								-lz -ljnigraphics -llog -lGLESv1_CM -lGLESv2 -ldl
LOCAL_SHARED_LIBRARIES := sefactory
LOCAL_STATIC_LIBRARIES := devIL
LOCAL_MODULE := libse

LOCAL_ARM_MODE := arm

LOCAL_PRELINK_MODULE := false 

include $(BUILD_SHARED_LIBRARY)

#build test_plugin lib
#include $(CLEAR_VARS)
##LOCAL_MODULE_TAGS := tests

#LOCAL_CFLAGS += -DGLES_20 -DNDK -D_DEBUG=0
#LOCAL_C_INCLUDES += \
#        $(LOCAL_PATH)/src \
#		$(LOCAL_PATH)/src/command \
#        $(LOCAL_PATH)/src/export \
#		$(LOCAL_PATH)/src/2D \
#		$(LOCAL_PATH)/src/tinyxml \
#		$(LOCAL_PATH)/src/animation \
#		$(LOCAL_PATH)/src/ParticleSystem \
#		$(LOCAL_PATH)/src/minizip \
#		$(LOCAL_PATH)/src/GUI \
#                $(LOCAL_PATH)/src/external \
#                $(LOCAL_PATH)/src/test_plugin
        	
					
#LOCAL_SRC_FILES:= ./src/test_plugin/testdll.cpp \
#./src/test_plugin/SE_PluginLightShader.cpp \
#./src/test_plugin/my_shaderdefine.cpp \
#./src/test_plugin/SE_PluginLightRender.cpp \
#./src/test_plugin/my_renderdefine.cpp
  

#LOCAL_LDLIBS := -L$(call host-path, $(TOP_LOCAL_PATH)/libs) \
								-lz -llog -lGLESv1_CM -lGLESv2
#LOCAL_SHARED_LIBRARIES := libse sefactory

#LOCAL_MODULE := libmyplugin

#LOCAL_ARM_MODE := arm

#LOCAL_PRELINK_MODULE := false 

#include $(BUILD_SHARED_LIBRARY)

