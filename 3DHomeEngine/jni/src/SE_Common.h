#ifndef SE_COMMON_H
#define SE_COMMON_H
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#define SE_ASSERT(x) assert((x))
typedef int SE_Result;
#define SE_VALID 1
#define SE_INVALID 0

/**
 * 0 : invalid
 * 1 : valid
 * */
#define SE_Result_IsValid(r) ((r) != 0)
#define SE_SKELETONCONTROLLER "SE_SkeletonController"
#define SE_CAMERABESTPOSITION "SE_CameraBestPosition"
#define SE_DECLARE_NONECOPY(Class) \
    Class(const Class&); \
    Class& operator=(const Class&);

#define SE_PI 3.14159265358979323846

enum {SE_RELEASE_DELAY, SE_RELEASE_NO_DELAY};
enum SE_AXIS_TYPE {SE_AXIS_NOAXIS = -1, SE_AXIS_X, SE_AXIS_Y, SE_AXIS_Z};
enum SE_CULL_TYPE {SE_FULL_CULL, SE_PART_CULL, SE_NOT_CULL};
enum SE_Plane_Side {SE_POSITIVE, SE_NEGATIVE, SE_OTHER};
enum SE_MirrorPlane {SE_XY, SE_XZ, SE_YZ};
enum SE_PRIMITIVE_TYPE {LINES, LINE_STRIP, TRIANGLES, TRIANGLES_INDEX,TRIANGLE_FAN, TRIANGLE_STRIP};
enum SE_SAMPLE_TYPE {NEAREST, LINEAR,MIPMAP};
enum SE_WRAP_TYPE {REPEAT, CLAMP};
enum SE_OWN_TYPE {NOT_OWN, OWN};// this is used to determine whether to delete a pointer
enum SE_TEXUNIT_TYPE {SE_TEXTURE0, SE_TEXTURE1, SE_TEXTURE2, SE_TEXTURE3, SE_TEXUNIT_NUM};
enum SE_SCENE_TYPE {SE_TEXTURE_SCENE, SE_RENDERBUFFER_SCENE, SE_FRAMEBUFFER_SCENE, SE_POST_EFFECT_SCENE, SE_SCENE_TYPE_NUM};
enum {SE_MAX_SCENE_SIZE = 8};
enum SE_COLORBLEND_MODE {
	SE_COLOR_MODE = 0,
    SE_TEXTURE0_MODE = 1,
    SE_TEXTURE1_MODE = 2,
    SE_TEXTURE2_MODE = 3,
    SE_TEXTURE3_MODE = 4,
    SE_COLOR_TEXTURE0_MODE = 5,
    SE_COLOR_TEXTURE1_MODE = 6,
    SE_COLOR_TEXTURE2_MODE = 7,
    SE_COLOR_TEXTURE3_MODE = 8,
    SE_TEXTURE0_TEXTURE1_MODE = 9,
    SE_TEXTURE0_TEXTURE2_MODE = 10,
    SE_TEXTURE0_TEXTURE3_MODE = 11,
    SE_TEXTURE1_TEXTURE2_MODE = 12,
    SE_TEXTURE1_TEXTURE3_MODE = 13,
    SE_TEXTURE2_TEXTURE3_MODE = 14,
    SE_COLOR_TEXTURE0_TEXTURE1_MODE = 15,
    SE_COLOR_TEXTURE0_TEXTURE2_MODE = 16,
    SE_COLOR_TEXTURE0_TEXTURE3_MODE = 17,
    SE_COLOR_TEXTURE1_TEXTURE2_MODE = 18,
    SE_COLOR_TEXTURE1_TEXTURE3_MODE = 19,
    SE_COLOR_TEXTURE2_TEXTURE3_MODE = 20,
    SE_TEXTURE0_TEXTURE1_TEXTURE2_MODE = 21,
    SE_TEXTURE0_TEXTURE1_TEXTURE3_MODE = 22,
    SE_TEXTURE0_TEXTURE2_TEXTURE3_MODE = 23,
    SE_TEXTURE1_TEXTURE2_TEXTURE3_MODE = 24,
    SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_MODE = 25,
    SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE3_MODE = 26,
    SE_COLOR_TEXTURE1_TEXTURE2_TEXTURE3_MODE = 27,
    SE_COLOR_TEXTURE0_TEXTURE2_TEXTURE3_MODE = 28,
    SE_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE = 29,
    SE_COLOR_TEXTURE0_TEXTURE1_TEXTURE2_TEXTURE3_MODE = 30,
    SE_BLENDMODE_NUM = 31
};

class SE_SpatialAttribute
{
public:
enum SpatialProperty
{
    VISIBLE = 0x1, 
    MOVABLE = 0x1 << 1, 
    COLLISIONABLE = 0x1 << 2, 
        TOUCHABLE = 0x1 << 3,
    };

    enum SpatialRuntimeStatus
    {
        IGNORVERTEXBUFFER = 0x1,
        SELECTED = 0x1 << 1,
        CAMERAFULLCULL = 0x1 << 2,
        NEEDDRAWLINE = 0x1 << 3,
        BELONGTONONE = 0x1 << 4,
        BELONGTOWALL = 0x1 << 5,
        BELONGTOGROUND = 0x1 << 6
};
enum SpatialEffect
{
    BLENDABLE = 0x1,
    DEPTHENABLE = 0x1 << 1,
    FOREVERBLEND = 0x1 << 2,
    NEEDSPECULAR = 0x1 << 3,
    SHADOWOBJECT = 0x1 << 4,
    MIRRORGENERATOR = 0x1 << 5,
    MIRRORRENDER = 0x1 << 6,
    SHADOWGENERATOR = 0x1 << 7,
    SHADOWRENDER = 0x1 << 8,
    UVANIMATION = 0x1 << 9,
    LIGHTING = 0x1 << 10,
    ALPHATEST = 0x1 << 11,
    MINIBOX = 0x1 << 12,
    PARTICLE = 0x1 << 13,
    CULLFACE = 0x1 << 14,
    RENDERTOFBO = 0x1 << 15,
    FLAGWAVE = 0x1 << 16,
    DRAWLINE = 0x1 << 17,
    BLACKWHITE = 0x1 << 18,
    BLENDX = 0x1 << 19,
    BLENDY = 0x1 << 20,
    BLENDZ = 0x1 << 21,
        CLOAKFLAGWAVE = 0x1 << 22,
        NEEDFOG = 0x1 << 23,
        ISWALL = 0x1 << 24,
        ISGROUND = 0x1 << 25

};
};
enum SCENEMANAGER_STATUS
{
    DEFAULTSTATUS = 0x1,
    ENABLELIGHT = 0x1 << 1,
    ENABLEMIRROR = 0x1 << 2,
    ENABLEWALLSHADOW = 0x1 << 3,
    ENABLEGROUNDSHADOW = 0x1 << 4


};
enum SPATIAL_TYPE {NONE, NODE, GEOMETRY};

enum RENDER_STATE_TYPE 
{
DEPTHTESTSTATE = 0, 
BLENDSTATE, 
RENDERSTATE_NUM
};
enum RENDER_STATE_SOURCE {INHERIT_PARENT, SELF_OWN};
enum USER_DEFINE_ATTRIBUTE 
{
    USER_ALPHA = 0,
    USER_COLOR,
    USER_ATTR_COUNT
    };
enum ADD_OCTREE {NO_ADD, YES_ADD};

enum NodeTypes
{
	ROOT_NODE,
	GROUP_NODE,
	LOD_NODE
};

enum ApplicationStatus
{
    PREPARE, 
    RUNNING, 
    SUSPEND, 
    EXIT
};
enum ApplicationCameraType
{
    SE_SHADOWCAMERA,
    SE_MIRRORCAMERA,
    SE_WALLSHADOWCAMERA,
    SE_GROUNDSHADOWCAMERA,
    MAX_CAMERA_NUM
};

enum SE_ERROR 
{
    SE_NO_ERROR, 
    CHILD_INVALID_ID, 
    CHILD_DUP_ID
};
enum SE_SCENE_VISIBILITY 
{
    SE_VISIBLE, 
    SE_NOVISIBLE
};
enum SE_SCENE_MANAGER_TYPE 
{
    DEFAULT = 0, 
    OCTREESCENEMANAGER
};
enum BV_TYPE 
{
    SPHERE, 
    AABB, 
    OBB
    };
enum ANIMATION_LISTENER_TYPE
{
    MESH_ANIMATION = 0,
    RIGID_ANIMATION,
    RIGID_ANIMATION_ROTATE_TO_ZERO,
    RIGID_ANIMATION_FOLLOWER
};

enum INTERPOLATE_TYPE
{
    CONST_INTERP = 0,
    ACCELERATE_INTERP,
    DECELERATE_INTERP,
    ACCELERATEDECELERATE_INTERP,
    ANTICIPATE_INTERP,
    BOUNCE_INTERP,
    CYCLE_INTERP,
    OVERSHOOT_INTERP,
    ANTICIPATEOVERSHOOT_INTERP,
    VIBRATE_INTERP,
    DAMPEDVIBRATE_INTERP,
    RANDOMDAMPEDVIBRATE_INTERP


};

enum RENDERTARGET_TYPE
{
    MIRROR = 0,
    SHADOW
};

struct _Vector4f
{
    _Vector4f()
    {
        d[0] = 0.0;
        d[1] = 0.0;
        d[2] = 0.0;
        d[3] = 0.0;
    }
    float d[4];
};

struct _Vector3f
{
    float d[3];
};

struct _Vector3i
{
    int d[3];
};

struct _Vector2f
{
    float d[2];
};
extern const char* SE_SEP;
template <class T>
class SE_Wrapper
{
public:
	enum PTR_TYPE {ARRAY, NOT_ARRAY};
	SE_Wrapper()
	{
		mPtr = 0;
		mNum = 1;
		mPtrType = NOT_ARRAY;
	}
	~SE_Wrapper()
	{
        release();
	}
	SE_Wrapper(T* ptr, PTR_TYPE ptrType)
	{
		mPtr = ptr;
		mPtrType = ptrType;
		mNum = 1;
	}
	void inc()
	{
		mNum++;
	}
	void dec()
	{
		mNum--;
	}
	int getNum() const
	{
		return mNum;
	}
	T* getPtr() const
	{
		return mPtr;
	}
	PTR_TYPE getPtrType() const
	{
		return mPtrType;
	}
private:
	void release()
	{
		if(mPtr)
		{
			if(mPtrType == NOT_ARRAY)
				delete mPtr;
			else
				delete[] mPtr;
		}
		mPtr = NULL;
	}
private:
	SE_Wrapper(const SE_Wrapper& );
	SE_Wrapper& operator=(const SE_Wrapper&);
private:
	T* mPtr;
	int mNum;
	PTR_TYPE mPtrType;
};
template <class T>
struct SE_PointerOwner
{
    T* ptr;
	SE_OWN_TYPE own;
	SE_PointerOwner()
	{
		ptr = NULL;
		own = NOT_OWN;
	} 
	~SE_PointerOwner()
	{
		if(own == OWN && ptr)
			delete ptr;
	}
};
#endif
