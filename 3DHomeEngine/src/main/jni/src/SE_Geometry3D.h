#ifndef SE_GEOMETRY3D_H
#define SE_GEOMETRY3D_H
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Quat.h"
#include "SE_Common.h"
#include <vector>
#include<string>
#include "SE_Vector.h"
class SE_AABB;
class SE_Sphere;
class SE_OBB;

class SE_ENTRY SE_IntersectResult
{
public:
    SE_IntersectResult()
    {
        intersected = false;
    }
    std::vector<SE_Vector3f> intersectPoint;
    std::vector<float> distance;
    bool intersected; //
};
template <class T>
struct SE_Rect
{
    T left ,right, top, bottom;
    SE_Rect()
    {
        left = right = top = bottom = 0;
    }
};
class SE_ENTRY SE_Rect3D
{
public:
    static SE_Rect3D normalizeRect;
    SE_Rect3D();
    SE_Rect3D(const SE_Vector3f& center, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, 
              float e[2]);
    SE_Vector3f getCenter() const;
    SE_Vector3f getXAxis() const;
    SE_Vector3f getYAxis() const;
    void getExtent(float out[2]) const;
    // v is array of 4 element
    void getVertex(SE_Vector3f* v) const;
    //faces is array of 2 element
    void getFaces(SE_Vector3i* faces) const;
    void setCenter(const SE_Vector3f& center);
    //xAxis must perpendicular with yAxis
    void setAxis(const SE_Vector3f& xAxis, const SE_Vector3f& yAxis);
    void setExtent(float e[2]);
private:
    SE_Vector3f mCenter;
    SE_Vector3f mAxis[2];
    float mExtent[2];
};
class SE_ENTRY SE_Segment
{
public:
    SE_Segment();
    SE_Segment(const SE_Vector3f& start, const SE_Vector3f& end);
    //if dir is normalized , the len is the length of dir. otherwise len == 1.0
    SE_Segment(const SE_Vector3f& start, const SE_Vector3f& dir, float len);
    SE_Vector3f getStart() const;
    SE_Vector3f getEnd() const;
    SE_Vector3f getDirection() const;
    void set(const SE_Vector3f& p0, const SE_Vector3f& p1)
    {
        mStart = p0;
        mEnd = p1;
    }

private:
    SE_Vector3f mStart;
    SE_Vector3f mEnd;
};
class SE_ENTRY SE_Plane
{
    /*
     * n * x - d = 0;
     * */
public:
    SE_Plane();
    SE_Plane(const SE_Vector3f& normal, float d);
    SE_Plane(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2);
    void set(const SE_Vector3f& normal, float d);
    void set(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2);
    SE_Vector3f getNormal() const;
    float getDistance() const;
    SE_Plane_Side whichSide(const SE_Vector3f& point) const;
    float distance(const SE_Vector3f& point) const;
    SE_Plane transform(const SE_Matrix4f& m);
private:
    SE_Vector3f mNormal;
    float mDistance; 
};
class SE_ENTRY SE_Ray
{
public:
    SE_Ray();
    SE_Ray(const SE_Vector3f& start, const SE_Vector3f& end);
    // normalized indicate that whether dir is normalized.
    SE_Ray(const SE_Vector3f& org, const SE_Vector3f& dir, bool normalized);
    SE_Vector3f getOrigin() const;
    SE_Vector3f getDirection() const;
private:
    SE_Vector3f mOrigin;
    SE_Vector3f mDir;
};
class SE_ENTRY SE_Triangle
{
public:
    SE_Triangle();
    SE_Triangle(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2);
    bool isCollinear() const;
    void getPoint(SE_Vector3f point[3]) const;
    void set(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2);
    SE_IntersectResult intersect(const SE_Ray& ray) const;
private:
    SE_Vector3f mP0;
    SE_Vector3f mP1;
    SE_Vector3f mP2;
};
class SE_ENTRY SE_Frustum
{
public:
    SE_Frustum();
    SE_Frustum(float fovAngle, float ratio, float n, float f);
    SE_Rect<float> getNearPlaneRect() const;
    SE_Matrix4f getPerspectiveMatrix() const;
    SE_Plane getLeftPlane() const;
    SE_Plane getRightPlane() const;
    SE_Plane getTopPlane() const;
    SE_Plane getBottomPlane() const;
    SE_Plane getFarPlane() const;
    SE_Plane getNearPlane() const;
    float getNear() const;
    float getFar() const;
    void set(float fovAngle, float ratio, float n, float f);
private:
    SE_Plane mLeftp;
    SE_Plane mRightp;
    SE_Plane mTopp;
    SE_Plane mBottomp;
    SE_Plane mNearp;
    SE_Plane mFarp;
    float mFovAngle;
    float mRatio;
    float mNear;
    float mFar;
};
class SE_ENTRY SE_Sphere
{
public:
    SE_Sphere();
    SE_Sphere(const SE_Vector3f& center, float r);
    void set(const SE_Vector3f& center, float r);
    void createFromPoints(SE_Vector3f* points, int num);
    SE_Vector3f getCenter() const;
    float getRadius() const;
    SE_IntersectResult intersect(const SE_Ray& ray) const;
    SE_Plane_Side whichSide(const SE_Plane& plane) const;
    SE_IntersectResult intersect(const SE_Sphere& sphere) const;
    bool containPoint(const SE_Vector3f& point) const;
private:
    void ritterSphere(SE_Vector3f* points, int num);
    void sphereOfSphereAndPoint(const SE_Vector3f& point);
    void sphereFromDistantPoints(SE_Vector3f* points, int pointNum);
    void mostSeparatedPointsOnAABB(int* min , int* max, SE_Vector3f* points, int numPoint);

private:
    SE_Vector3f mCenter;
    float mRadius;
};
class SE_ENTRY SE_AABB
{
public:
    SE_AABB();
    SE_AABB(const SE_Vector3f& min, const SE_Vector3f& max);
    void set(const SE_Vector3f& min, const SE_Vector3f& max);
    void createFromPoints(SE_Vector3f* points, int num);
    SE_Vector3f getMin() const;
    SE_Vector3f getMax() const;
    SE_Vector3f getExtent() const;
    SE_Vector3f getCenter() const;
    SE_IntersectResult intersect(const SE_AABB& aabb) const;
    SE_IntersectResult intersect(const SE_Ray& ray) const;
    SE_IntersectResult intersect(const SE_Plane& plane) const;
    SE_Plane_Side whichSide(const SE_Plane& plane) const;
    void getEdge(SE_Segment edge[12]);
private:
    SE_Vector3f mMin;
    SE_Vector3f mMax;
};
class SE_ENTRY SE_OBB
{
public:
    SE_OBB();
    void set(const SE_Vector3f& center, SE_Vector3f axis[3], float extent[3]);
    void createFromPoints(SE_Vector3f* points, int num);
    void createFromAABB(const SE_AABB& aabb);
    void getBoxVertex(SE_Vector3f out[8]) const;
    SE_Vector3f getCenter() const;
    void getAxis(SE_Vector3f axis[3]) const;
    void getExtent(float e[3]) const;
    void transfrom(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    SE_IntersectResult intersect(const SE_OBB& obb) const;
    SE_IntersectResult intersect(const SE_Ray& ray) const;
    SE_Plane_Side whichSide(const SE_Plane& plane) const;
private:
    SE_Vector3f mCenter;
    SE_Vector3f mAxis[3];
    float mExtent[3];
};
class SE_ENTRY SE_GeometryIntersect
{
public:
    static int movingSphereStaticAABB(const SE_Sphere& sphere, const SE_AABB& aabb, 
                                     const SE_Vector3f& endPoint, SE_Vector3f* out);
    static int sphereAABB(const SE_Sphere& sphere, const SE_AABB& aabb);
    static float pointAABBDistanceSquare(const SE_Vector3f& point, const SE_AABB& aabb);
    static int movingSphereStaticPlane(const SE_Sphere& sphere, const SE_Plane& plane, const SE_Vector3f& dirOfSphere, SE_Vector3f* out);
    static int movingOBBStaticAABB(const SE_OBB obb, const SE_AABB& aabb, SE_AXIS_TYPE axis, float dist, SE_OBB* out);
    static float pointDistance(const SE_Vector3f& p1, const SE_Vector3f& p2);
    static void pointDistanceToLine(const SE_Vector3f& point, const SE_Vector3f& start, const SE_Vector3f& dir, float& distance, SE_Vector3f& intersectPoint);
};
class SE_ENTRY SE_MountPoint
{
public:
    std::string mountPointName;
    std::string mGroupName;
    SE_Vector3f rotateAxis;
    SE_Vector3f scale;    
    float rotateAngle;
    SE_Vector3f translate;
};
#endif
