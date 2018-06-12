#ifndef SE_CAMERA_H
#define SE_CAMERA_H
#include "SE_Factory.h"
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Quat.h"
#include "SE_Geometry3D.h"
#include "SE_Common.h"
#include "SE_Object.h"

#include "SE_BoundingVolume.h"
class SE_MotionEvent;
class SE_KeyEvent;
class SE_ENTRY SE_Camera : public SE_Object
{
DECLARE_OBJECT(SE_Camera)
public:
    SE_Camera();
    virtual ~SE_Camera();
    SE_Camera(const SE_Vector3f& location, const SE_Vector3f& target, float fov, float ratio, float near, float far);
    SE_Camera(const SE_Vector3f& location, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, const SE_Vector3f& zAxis, float fov, float ratio, float near, float far);
    SE_Camera(const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& up, float fov, float ratio, float near, float far);
    int cullBV(const SE_BoundingVolume& bv) const;
    void setViewport(int x, int y, int w, int h);
    SE_Rect<int> getViewport() const;
    SE_Matrix4f getWorldToViewMatrix() const;
    SE_Matrix4f getViewToWorldMatrix() const;
    void setFrustum(float fov, float ratio, float near, float far);
    void translateLocal(const SE_Vector3f& translate);
    SE_Ray screenCoordinateToRay(int x, int y);
    void getFrustumPlanes(SE_Plane planes[6]) const;
    void setLocation(const SE_Vector3f& loc);
    SE_Vector3f getLocation()
    {
        return mLocation;
    }
    SE_Vector3f getAxisX()
    {
        return mAxisX;
    }
    SE_Vector3f getAxisY()
    {
        return mAxisY;
    }
    SE_Vector3f getAxisZ()
    {
        return mAxisZ;
    }

    SE_Vector3f getUp()
    {
        return mUp;
    }
    SE_Vector3f getTarget()
    {
        return mTarget;
    }

    SE_Frustum * getFrustum()
    {
        return &mFrustum;
    }
    //0: x axis, 1
    void rotateLocal(float angle, SE_AXIS_TYPE axis);
    void rotateLocal(const SE_Quat& rotate);
    void create(const SE_Vector3f& location, const SE_Vector3f& target, float fov, float ratio, float near, float far);
    void create(const SE_Vector3f& location, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, const SE_Vector3f& zAxis, float fov, float ratio, float near, float far);
    void create(const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& up, float fov, float ratio, float near, float far);
    SE_Matrix4f getPerspectiveMatrix() const;
    //camera will own bv, camera will delete mBoundingVolume which is assigned by bv;
    void setBoundingVolume(const SE_BoundingVolume* bv)
    {
        if(mBoundingVolume)
            delete mBoundingVolume;
        mBoundingVolume = bv;
    }
    const SE_BoundingVolume* getBoundingVolume() const
    {
        return mBoundingVolume;
    }
    virtual void onKeyEvent(SE_KeyEvent* keyEvent);
    virtual void onMotionEvent(SE_MotionEvent* motionEvent);
	
	enum EViewportType
	{
		EOrthoYZ=0,
		EOrthoXZ,
		EPerspective,
		EOrthoXY,
        EOrthoCamera
	};

	void createOthro(EViewportType type,float left,float right,float bottom, float top, float near, float far);
    void createOthroCamera(float left,float right,float bottom, float top, float near, float far,const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& up);
	SE_Matrix4f getOrthoMatrix() const;
	EViewportType getType(){ return mType; }
	void setType(EViewportType type){ mType; };
    float getFovDegree()
    {
        return mFov;
    }

    bool isChanged()
    {
        return mChanged;
    }

    void setNeedDof(bool need);
    bool isNeedDof()
    {
        return mNeedDof;
    }

    void setDofPara(SE_Vector4f para)
    {
        mDofPara = para;
    }

    SE_Vector4f getDofPara()
    {
        return mDofPara;
    }
protected:
	EViewportType mType;

private:
    SE_Frustum mFrustum;
    SE_Rect<int> mViewport;
    SE_Vector3f mLocation;
    SE_Vector3f mAxisX;
    SE_Vector3f mAxisY;
    SE_Vector3f mAxisZ;
    SE_Vector3f mUp;
    SE_Vector3f mTarget;
    mutable SE_Plane mPlanes[6];
    bool mChanged;
    const SE_BoundingVolume* mBoundingVolume;
	SE_Matrix4f mOrthoMatrix;
    float mFov;
    bool mNeedDof;
    SE_Vector4f mDofPara;//x = near blur depht, y = focal plan depth ,z = far blur depth,w = blurriness cutoff for object behind the focal plan(0 - 1)
};
#endif
