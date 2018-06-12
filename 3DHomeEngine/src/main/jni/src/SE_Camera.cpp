#include "SE_DynamicLibType.h"
#include "SE_Factory.h"

#include "SE_Camera.h"
#include "SE_Log.h"
#include "SE_BoundingVolume.h"

#include "SE_UserCommand.h"
#include "SE_MemLeakDetector.h"
IMPLEMENT_OBJECT(SE_Camera)
///////////////
SE_Camera::~SE_Camera()
{
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("### destroctor ~SE_Camera ####\n");
    if(mBoundingVolume)
        delete mBoundingVolume;
}
SE_Camera::SE_Camera()
{
    mChanged = true;
    mBoundingVolume = NULL;
	mType = EPerspective;
    mFov = 45.0;
    mNeedDof = false;
    mDofPara.set(100,500,1000,1);
}
SE_Camera::SE_Camera(const SE_Vector3f& location, const SE_Vector3f& target, float fov, float ratio, float near, float far)
{
    create(location, target, fov, ratio, near, far);
    mBoundingVolume = NULL;
	mType = EPerspective;
    mFov = 45.0;
}
SE_Camera::SE_Camera(const SE_Vector3f& location, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, const SE_Vector3f& zAxis, float fov, float ratio, float near, float far)
{
    create(location, xAxis, yAxis, zAxis, fov, ratio, near, far);
    mBoundingVolume = NULL;
	mType = EPerspective;
    mFov = 45.0;
}
SE_Camera::SE_Camera(const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& yAxis, float fov, float ratio, float near, float far)
{
    create(location, zAxis, yAxis, fov, ratio, near, far);
    mBoundingVolume = NULL;
	mType = EPerspective;
    mFov = 45.0;
}
SE_Rect<int> SE_Camera::getViewport() const
{
    return mViewport;
}
int SE_Camera::cullBV(const SE_BoundingVolume& bv) const
{
    SE_Plane cullPlanes[6];
    SE_Plane_Side planeSide = SE_NEGATIVE;
    getFrustumPlanes(cullPlanes);
    for(int i = 0 ; i < 6 ; i++)
    {
        SE_Plane_Side p = bv.whichSide(cullPlanes[i]);
        if(p == SE_POSITIVE)
            return SE_FULL_CULL;
        if(p != SE_NEGATIVE)
            planeSide = p;
    }
    if(planeSide == SE_NEGATIVE)
        return SE_NOT_CULL;
    else
        return SE_PART_CULL;
}
void SE_Camera::setViewport(int x, int y, int w, int h)
{
    mViewport.left = x;
    mViewport.right = x + w;
    mViewport.top = y;
    mViewport.bottom = y + h;
}
SE_Matrix4f SE_Camera::getWorldToViewMatrix() const
{
    SE_Matrix4f vtow = getViewToWorldMatrix();
    return vtow.inverse(); 
}
SE_Matrix4f SE_Camera::getViewToWorldMatrix() const
{
    SE_Matrix3f rm;
    rm.setColumn(0, mAxisX);
    rm.setColumn(1, mAxisY);
    rm.setColumn(2, mAxisZ);
    return SE_Matrix4f(rm, mLocation);
}
void SE_Camera::setFrustum(float fov, float ratio, float near, float far)
{
    mFrustum.set(fov, ratio, near, far);
    mChanged = true;
}
void SE_Camera::translateLocal(const SE_Vector3f& translate)
{
    mLocation = mLocation + mAxisX * translate.x + mAxisY * translate.y + mAxisZ * translate.z;
    mChanged = true; 
}
SE_Ray SE_Camera::screenCoordinateToRay(int x, int y)
{ 
	if(mType==EPerspective)
	{
		float xp = ((float)x) / (mViewport.right - mViewport.left);
		float yp = 1 - ((float)y) / (mViewport.bottom - mViewport.top);
		SE_Rect<float> nearRect = mFrustum.getNearPlaneRect();
		float xv = (1 - xp) * nearRect.left + xp * nearRect.right;
		float yv = (1 - yp) * nearRect.bottom + yp * nearRect.top;
		float dirLen = SE_Sqrtf(xv * xv + yv * yv + mFrustum.getNear() * mFrustum.getNear());
		SE_Vector3f dir;
		dir.x = mAxisX.x * xv / dirLen + mAxisY.x * yv / dirLen + mAxisZ.x * (-mFrustum.getNear()) / dirLen;
		dir.y = mAxisX.y * xv / dirLen + mAxisY.y * yv /dirLen + mAxisZ.y * (-mFrustum.getNear()) / dirLen;
		dir.z = mAxisX.z * xv / dirLen + mAxisY.z * yv / dirLen + mAxisZ.z * (-mFrustum.getNear()) / dirLen;
		return SE_Ray(mLocation, dir, false);
	}
	else
	{

		SE_Vector3f location;
		SE_Vector3f dir;
		float w = mViewport.right-mViewport.left;
		float h = mViewport.bottom-mViewport.top;
		switch(mType)
		{
		case EOrthoXY:
			location.x = mLocation.x + x-w/2;
			location.y = mLocation.y - (y-h/2);
			location.z = mLocation.z;
			dir.x = 0;
			dir.y = 0;
			dir.z = -1;
			break;
		case EOrthoXZ:
			location.x = mLocation.x + x-w/2;
			location.y = mLocation.y;
			location.z = mLocation.z - (y-h/2);
			dir.x = 0;
			dir.y = -1;
			dir.z = 0;
			break;
		case EOrthoYZ:
			location.x = mLocation.x;
			location.y = mLocation.y + x-w/2;
			location.z = mLocation.z - (y-h/2);
			dir.x = -1;
			dir.y = 0;
			dir.z = 0;
			break;

		}
		return SE_Ray(location, dir, false);
	}

}
void SE_Camera::getFrustumPlanes(SE_Plane planes[6]) const
{
    if(!mChanged)
    {
        for(int i = 0 ; i < 6 ; i++)
        {
            planes[i] = mPlanes[i];
        }
        return;
    }
    SE_Plane lplanes[6];
    lplanes[0] = mFrustum.getLeftPlane();
    lplanes[1] = mFrustum.getRightPlane();
    lplanes[2] = mFrustum.getTopPlane();
    lplanes[3] = mFrustum.getBottomPlane();
    lplanes[4] = mFrustum.getNearPlane();
    lplanes[5] = mFrustum.getFarPlane();
    SE_Matrix4f vtom = getViewToWorldMatrix();
    vtom = vtom.inverse();
    vtom = vtom.transpose();
    for(int i = 0 ; i < 6 ; i++)
    {
        SE_Plane p = lplanes[i].transform(vtom);
        planes[i].set(p.getNormal().neg(), p.getDistance());
        mPlanes[i] = planes[i];
    }
#ifdef DEBUG
    /*
    SE_Plane nplanes[6];
    SE_Vector3f NearLeftBottom, NearLeftTop, NearRightBottom, NearRightTop,
                FarLeftBottom, FarLeftTop, FarRightBottom, FarRightTop;
    SE_Vector3f tmp1;
    SE_Vector3f tmp[4];
    SE_Rect<float> nearplane = mFrustum.getNearPlaneRect();
    tmp[0] = mAxisZ.mul(-mFrustum.getNear());
    tmp[1] = mAxisX.mul(nearplane.left);
    tmp[2] = mAxisY.mul(nearplane.bottom);
    tmp[3] = mLocation;
    NearLeftBottom = tmp[0] + tmp[1] + tmp[2] + tmp[3];
    tmp1 = tmp[0] + tmp[1] + tmp[2];
    tmp1 = tmp1.mul(mFrustum.getFar() / mFrustum.getNear());
    FarLeftBottom = mLocation + tmp1;

    tmp[1] = mAxisX.mul(nearplane.left);
    tmp[2] = mAxisY.mul(nearplane.top);
    NearLeftTop = tmp[0] + tmp[1] + tmp[2] + tmp[3];
    tmp1 = tmp[0] + tmp[1] + tmp[2];
    tmp1 = tmp1.mul(mFrustum.getFar() / mFrustum.getNear());
    FarLeftTop = mLocation + tmp1;

    tmp[1] = mAxisX.mul(nearplane.right);
    tmp[2] = mAxisY.mul(nearplane.bottom);
    NearRightBottom = tmp[0] + tmp[1] + tmp[2] + tmp[3];
    tmp1 = tmp[0] + tmp[1] + tmp[2];
    tmp1 = tmp1.mul(mFrustum.getFar() / mFrustum.getNear());
    FarRightBottom = mLocation + tmp1;

    tmp[1] = mAxisX.mul(nearplane.right);
    tmp[2] = mAxisY.mul(nearplane.top);
    NearRightTop = tmp[0] + tmp[1] + tmp[2] + tmp[3];

    tmp1 = tmp[0] + tmp[1] + tmp[2];
    tmp1 = tmp1.mul(mFrustum.getFar() / mFrustum.getNear());
    FarRightTop = mLocation + tmp1;

    nplanes[0].set(NearLeftBottom, mLocation, NearLeftTop);
    nplanes[1].set(NearRightTop, mLocation, NearRightBottom);
    nplanes[2].set(mLocation, NearLeftBottom, NearRightBottom);
    nplanes[3].set(mLocation, NearRightTop, NearLeftTop);
    nplanes[4].set(NearLeftBottom, NearRightBottom, NearRightTop);
    nplanes[5].set(FarLeftBottom, FarLeftTop, FarRightTop);
    for(int i = 0 ; i < 6 ; i++)
    {
        planes[i] = nplanes[i];
    }
    */
#endif
}
void SE_Camera::setLocation(const SE_Vector3f& loc)
{
    mLocation = loc;
    mChanged = true;
}
void SE_Camera::rotateLocal(float angle, SE_AXIS_TYPE axis)
{
    SE_Quat q;
    switch(axis)
    {
    case SE_AXIS_X:
        q.set(angle, SE_Vector3f(1, 0, 0));
        break;
    case SE_AXIS_Y:
        q.set(angle, SE_Vector3f(0, 1, 0));
        break;
    case SE_AXIS_Z:
        q.set(angle, SE_Vector3f(0, 0, 1));
        break;
    }
    return rotateLocal(q);
}
void SE_Camera::rotateLocal(const SE_Quat& rotate)
{
    SE_Vector3f localxAxis(1, 0, 0);
    SE_Vector3f localyAxis(0, 1, 0);
    SE_Vector3f localzAxis(0, 0, 1);
    localxAxis = rotate.map(localxAxis);
    localyAxis = rotate.map(localyAxis);
    //localzAxis = rotate.map(zAxis);
    SE_Matrix4f vtom = getViewToWorldMatrix();
    SE_Vector4f worldxAxis = vtom.map(SE_Vector4f(localxAxis, 0));
    SE_Vector4f worldyAxis = vtom.map(SE_Vector4f(localyAxis, 0));
    //SE_Vector4f worldzAxis = vtom.map(SE_Vector4f(localzAxis, 0));
    SE_Vector4f worldzAxis(worldxAxis.xyz().cross(worldyAxis.xyz()), 0);
    mAxisX = worldxAxis.normalize().xyz();
    mAxisY = worldyAxis.normalize().xyz();
    mAxisZ = worldzAxis.normalize().xyz();
    mChanged = true;
}
void SE_Camera::create(const SE_Vector3f& location, const SE_Vector3f& target, float fov, float ratio, float near, float far)
{
    mFrustum.set(fov, ratio, near, far);
    SE_Vector3f zDir = location - target;
    if(zDir.isZero())
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("### camera direction is zero ####\n");
        zDir.set(0, 0, 1);
    }
    zDir = zDir.normalize();
    SE_Vector3f upDir(0, 1, 0);
    if(upDir == zDir)
    {
        upDir.set(0, 0, -1);
    }
    SE_Vector3f leftDir = upDir.cross(zDir);
    upDir = zDir.cross(leftDir);
    mAxisX = leftDir.normalize();
    mAxisY = upDir.normalize();
    mAxisZ = zDir;
    mLocation = location;
    mChanged = true;
    mViewport.left = 0;
    mViewport.right = 0;
    mViewport.top = 0;
    mViewport.bottom = 0;
    mFov = fov;
}
void SE_Camera::create(const SE_Vector3f& location, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, const SE_Vector3f& zAxis, float fov, float ratio, float near, float far)
{
    mFrustum.set(fov, ratio, near, far);
    mLocation = location;
    mAxisX = xAxis;
    mAxisY = yAxis;
    mAxisZ = zAxis;
    mViewport.left = 0;
    mViewport.right = 0;
    mViewport.top = 0;
    mViewport.bottom = 0;
    mChanged = true;    
    mFov = fov;
}
void SE_Camera::create(const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& up, float fov, float ratio, float near, float far)
{
    mUp = up;
    mTarget = zAxis;
    mFrustum.set(fov, ratio, near, far);
    SE_Vector3f xAxis = up.cross(zAxis);
    mAxisX = xAxis.normalize();
    mAxisY = zAxis.cross(xAxis).normalize();
    mAxisZ = zAxis.normalize();
    mLocation = location;
    mChanged = true;
    mFov = fov;
}
SE_Matrix4f SE_Camera::getPerspectiveMatrix() const
{
    return mFrustum.getPerspectiveMatrix();
}
void SE_Camera::onKeyEvent(SE_KeyEvent* keyEvent)
{}
void SE_Camera::onMotionEvent(SE_MotionEvent* motionEvent)
{}


void SE_Camera::createOthro(EViewportType type, float left,float right,float bottom, float top, float near, float far)
{

	SE_Vector3f location(0,0,0);
	SE_Vector3f zAxis;
	SE_Vector3f up;
	
	switch(type)
	{
	case EOrthoXY:
		location = SE_Vector3f(0,0,300);
		zAxis = SE_Vector3f(0,0,1);
		up = SE_Vector3f(0,1,0);
		break;
	case EOrthoXZ:
		location = SE_Vector3f(0,300,0);
		zAxis = SE_Vector3f(0,1,0);
		up = SE_Vector3f(0,0,1);
		break;
	case EOrthoYZ:
		location = SE_Vector3f(300,0,0);
		zAxis = SE_Vector3f(1,0,0);
		up = SE_Vector3f(0,0,1);
		break;

	}

    SE_Vector3f xAxis = up.cross(zAxis);
    mAxisX = xAxis.normalize();
    mAxisY = zAxis.cross(xAxis).normalize();
    mAxisZ = zAxis.normalize();
    mLocation = location;
    mChanged = true;


    const float r_width  = 1.0f/(right - left);
    const float r_height = 1.0f/(top - bottom);
    const float r_depth  = 1.0f/(far - near);
    const float x =  2.0f*(r_width);
    const float y =  2.0f*(r_height);
    const float z =  -2.0f*(r_depth);
    const float tx = -(right + left)*r_width;
    const float ty = -(top + bottom)*r_height;
    const float tz = -(far + near)*r_depth;

    float f[16];
    f[ 0] = x;
    f[ 5] = y;
    f[10] = z;
    f[12] = tx;
    f[13] = ty;
    f[14] = tz;
    f[15] = 1.0f;
    f[ 1] = f[ 2] = f[ 3] =
    f[ 4] = f[ 6] = f[ 7] =
    f[ 8] = f[ 9] = f[11] = 0.0f;

	mOrthoMatrix=SE_Matrix4f(f,SE_Matrix4f::COL_SEQUENCE);

	mType = type;
}
void SE_Camera::createOthroCamera(float left,float right,float bottom, float top, float near, float far,const SE_Vector3f& location, const SE_Vector3f& zAxis, const SE_Vector3f& up)
{   

    SE_Vector3f xAxis = up.cross(zAxis);
    mAxisX = xAxis.normalize();
    mAxisY = zAxis.cross(xAxis).normalize();
    mAxisZ = zAxis.normalize();
    mLocation = location;
    mChanged = true;


    const float r_width  = 1.0f/(right - left);
    const float r_height = 1.0f/(top - bottom);
    const float r_depth  = 1.0f/(far - near);
    const float x =  2.0f*(r_width);
    const float y =  2.0f*(r_height);
    const float z =  -2.0f*(r_depth);
    const float tx = -(right + left)*r_width;
    const float ty = -(top + bottom)*r_height;
    const float tz = -(far + near)*r_depth;

    float f[16];
    f[ 0] = x;
    f[ 5] = y;
    f[10] = z;
    f[12] = tx;
    f[13] = ty;
    f[14] = tz;
    f[15] = 1.0f;
    f[ 1] = f[ 2] = f[ 3] =
    f[ 4] = f[ 6] = f[ 7] =
    f[ 8] = f[ 9] = f[11] = 0.0f;

	mOrthoMatrix=SE_Matrix4f(f,SE_Matrix4f::COL_SEQUENCE);

	mType = EOrthoCamera;
}


SE_Matrix4f SE_Camera::getOrthoMatrix() const
{
	SE_Matrix4f m = mOrthoMatrix;
	return m;
}
void SE_Camera::setNeedDof(bool need)
{
    mNeedDof = need;
    SE_SwitchCameraDOFCommand* dd = new SE_SwitchCameraDOFCommand(SE_Application::getInstance());
    dd->enable = need;
    SE_Application::getInstance()->postCommand(dd); 
}
