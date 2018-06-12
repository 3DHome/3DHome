#ifndef SE_BOUNDINGVOLUME_H
#define SE_BOUNDINGVOLUME_H
#include "SE_Vector.h"
#include "SE_Quat.h"
#include "SE_Matrix.h"
#include "SE_Geometry3D.h"
class SE_BufferInput;
class SE_BufferOutput;
class SE_ENTRY SE_BoundingVolume
{
public:
    
    SE_BoundingVolume();
    virtual ~SE_BoundingVolume();
    virtual void write(SE_BufferOutput& output) const = 0;
    virtual void read(SE_BufferInput& input) = 0;
    virtual void createFromPoints(SE_Vector3f* points, int num);
    virtual void transform(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    virtual SE_Plane_Side whichSide(const SE_Plane& plane) const = 0;
    virtual SE_IntersectResult intersect(const SE_Ray& ray) const = 0;
    virtual bool intersect(const SE_BoundingVolume& bv) const = 0;
    virtual BV_TYPE getType() const = 0;
    virtual void merge(const SE_BoundingVolume* bv) = 0;
    virtual int movingSphereIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint, SE_Vector3f* out) = 0;
    virtual int movingOBBIntersect(const SE_OBB& obb, SE_AXIS_TYPE axis, float dist, SE_Vector3f* out) = 0;
    virtual SE_Vector3f getCenter();
};
class SE_ENTRY SE_SphereBV : public SE_BoundingVolume
{
public:
    SE_SphereBV();
    SE_SphereBV(const SE_Sphere& sphere);
    ~SE_SphereBV();
    virtual void write(SE_BufferOutput& output) const;
    virtual void read(SE_BufferInput& input);
    virtual void createFromPoints(SE_Vector3f* points, int num);
    virtual void transform(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    virtual SE_Plane_Side whichSide(const SE_Plane& plane) const;
    virtual SE_IntersectResult intersect(const SE_Ray& ray) const;
    virtual bool intersect(const SE_BoundingVolume& bv) const;
    virtual BV_TYPE getType() const;
    virtual void merge(const SE_BoundingVolume* bv);
    virtual int movingSphereIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint, SE_Vector3f* out);
    virtual int movingOBBIntersect(const SE_OBB& obb, SE_AXIS_TYPE axis, float dist, SE_Vector3f* out);
    virtual SE_Vector3f getCenter();
    SE_Sphere getGeometry()
    {
        return mSphere;
    }
private:
    SE_Sphere mSphere;
};
class SE_ENTRY SE_AABBBV : public SE_BoundingVolume
{
public:
    SE_AABBBV();
    SE_AABBBV(const SE_AABB& aabb);
    ~SE_AABBBV();
    virtual void write(SE_BufferOutput& output) const;
    virtual void read(SE_BufferInput& input);
    virtual void createFromPoints(SE_Vector3f* points, int num);
    virtual void transform(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    virtual SE_Plane_Side whichSide(const SE_Plane& plane) const;
    virtual SE_IntersectResult intersect(const SE_Ray& ray) const;
    virtual bool intersect(const SE_BoundingVolume& bv) const;
    virtual BV_TYPE getType() const;
    virtual void merge(const SE_BoundingVolume* bv);
    virtual int movingSphereIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint, SE_Vector3f* out);
    virtual int movingOBBIntersect(const SE_OBB& obb, SE_AXIS_TYPE axis, float dist, SE_Vector3f* out);
    virtual SE_Vector3f getCenter();
    SE_AABB getGeometry()
    {
        return mAABB;
    }
private:
    SE_AABB mAABB;
};
class SE_ENTRY SE_OBBBV : public SE_BoundingVolume
{
public:
    SE_OBBBV();
    SE_OBBBV(const SE_OBB& obb);
    ~SE_OBBBV();
    virtual void write(SE_BufferOutput& output) const;
    virtual void read(SE_BufferInput& input);
    virtual void createFromPoints(SE_Vector3f* points, int num);
    virtual void transform(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate);
    virtual SE_Plane_Side whichSide(const SE_Plane& plane) const;
    virtual SE_IntersectResult intersect(const SE_Ray& ray) const;
    virtual bool intersect(const SE_BoundingVolume& bv) const; 
    virtual BV_TYPE getType() const;
    virtual void merge(const SE_BoundingVolume* bv);
    virtual int movingSphereIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint, SE_Vector3f* out);
    virtual int movingOBBIntersect(const SE_OBB& obb, SE_AXIS_TYPE axis, float dist, SE_Vector3f* out);
    virtual SE_Vector3f getCenter();
    SE_OBB getGeometry()
    {
        return mOBB;
    }
private:
    SE_OBB mOBB;
};
#endif
