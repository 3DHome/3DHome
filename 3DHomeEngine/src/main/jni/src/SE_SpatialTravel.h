#ifndef SE_SPATIALTRAVEL_H
#define SE_SPATIALTRAVEL_H
#include "SE_Spatial.h"
#include "SE_Geometry3D.h"
#include "SE_Math.h"
#include "SE_Vector.h"
#include <list>
class SE_SimObject;
class SE_ENTRY SE_FindSpatialCollision : public SE_SpatialTravel
{
public:
    SE_FindSpatialCollision(const SE_Ray& ray);
    ~SE_FindSpatialCollision();
    int visit(SE_Spatial* spatial);
    int visit(SE_SimObject* so);
    SE_SimObject* getCollisionObject();
    SE_Spatial* getCollisionSpatial();
    SE_Vector3f getIntersectPoint()
    {
        return mMinResult.mIntersectPoint;
    }
private:
    struct CollisionResult
    {
        SE_Spatial* spatial;
        SE_SimObject* simObject;
        float distance;
        SE_Vector3f mIntersectPoint;
        CollisionResult()
        {
            spatial = NULL;
            simObject = NULL;
            distance = SE_FLT_MAX;
        }
    };
    CollisionResult mResult;
    CollisionResult mMinResult;
    //std::list<CollisionResult> mCollisionResult;
    SE_Ray mRay;    
};
class SE_ENTRY SE_MovingSphereStaticSpatialIntersect : public SE_SpatialTravel
{
public:
    SE_MovingSphereStaticSpatialIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint);
    int visit(SE_Spatial* spatial);
    int visit(SE_SimObject* so);
public:
    SE_Vector3f location;
    bool intersected;
private:
    SE_Sphere sphere;
    SE_Vector3f endPoint;
};
class SE_ENTRY SE_MovingSpatialIntersect : public SE_SpatialTravel
{
public:
    SE_MovingSpatialIntersect(SE_Spatial* s);
    int visit(SE_Spatial* spatial);
    int visit(SE_SimObject* so);
private:
    SE_Spatial* moveSpatial;
};
#endif
