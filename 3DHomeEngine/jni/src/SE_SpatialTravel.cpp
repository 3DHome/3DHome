#include "SE_DynamicLibType.h"
#include "SE_SpatialTravel.h"
#include "SE_SimObject.h"
#include "SE_BoundingVolume.h"
#include "SE_GeometryData.h"
#include "SE_Log.h"
#include "SE_Mesh.h"
#include "SE_MemLeakDetector.h"
SE_FindSpatialCollision::SE_FindSpatialCollision(const SE_Ray& ray): mRay(ray)
{
    
}
SE_FindSpatialCollision::~SE_FindSpatialCollision()
{}
int SE_FindSpatialCollision::visit(SE_Spatial* spatial)
{
    SE_BoundingVolume* bv = spatial->getWorldBoundingVolume();
    if(bv == NULL)
        return 0;

    //Ray Detector do not test a invisible spatial
    if(!spatial->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
    {        
        return 1;
    }

    if(!spatial->isSpatialStateHasAttribute(SE_SpatialAttribute::TOUCHABLE))
    {
        return 1;
    }

    if(spatial->isSpatialHasRuntimeAttribute(SE_SpatialAttribute::CAMERAFULLCULL))
    {
        return 1;
    }

    SE_IntersectResult ret = bv->intersect(mRay);
    if(ret.intersected)
    {
        mResult.spatial = spatial;
        return 0;
    }
    else 
        return 1;
}
int SE_FindSpatialCollision::visit(SE_SimObject* so)
{
    //Ray Detector do not test a invisible spatial
    if(!so->getSpatial()->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
    {
        return 0;
    }
    float distance = SE_FLT_MAX;
    bool intersected = 0;
    SE_Vector3f intersectP;
    if(so->getSpatial()->isSpatialEffectHasAttribute(SE_SpatialAttribute::MINIBOX))
    {
        SE_Vector3f cameraLoc = mRay.getOrigin();
        SE_Vector3f objectLoc = mResult.spatial->getCenter();
        distance = cameraLoc.subtract(objectLoc).length();
        intersected = 1;
    } else {

        //generate world geometry data
        SE_GeometryData data;
        SE_Matrix4f m2w = so->getSpatial()->getWorldTransform();
        SE_GeometryData::transform(so->getMesh()->getGeometryData(),m2w,&data);

        int faceNum = data.getFaceNum();
        SE_Vector3i* faces = data.getFaceArray();
        SE_Vector3f* vertex = data.getVertexArray();
        for(int i = 0 ; i < faceNum ; i++)
        {
            SE_Triangle triangle(vertex[faces[i].x], vertex[faces[i].y], vertex[faces[i].z]);
            SE_IntersectResult ret = triangle.intersect(mRay);
            if(ret.intersected && ret.distance[0] < distance)
            {
                distance = ret.distance[0];
                intersectP = ret.intersectPoint[1];
                intersected = 1;
            }
        }
    }
    if(intersected)
    {
        mResult.simObject = so;
        mResult.distance = distance;
        mResult.spatial = so->getSpatial();
        mResult.mIntersectPoint = intersectP;
        
        if(mResult.spatial != NULL && mMinResult.spatial == NULL)
        {
            mMinResult = mResult;
        }
        else if(0 && mResult.spatial->getWorldLayer() > mMinResult.spatial->getWorldLayer())
        {
            mMinResult = mResult;
        }
        else if(mResult.distance < mMinResult.distance)
        {
            mMinResult = mResult;
        }
    }
    return 0;
}
SE_SimObject* SE_FindSpatialCollision::getCollisionObject()
{
    return mMinResult.simObject;
}
SE_Spatial* SE_FindSpatialCollision::getCollisionSpatial()
{
    if (mMinResult.spatial) {
        return mMinResult.spatial;
    } else {
        return mResult.spatial;
    }
}
SE_MovingSphereStaticSpatialIntersect::SE_MovingSphereStaticSpatialIntersect(const SE_Sphere& sphere, const SE_Vector3f& endPoint)
{
    intersected = false;
    this->sphere = sphere;
    this->endPoint = endPoint;
    location.set(endPoint.x, endPoint.y, endPoint.z);
}
int SE_MovingSphereStaticSpatialIntersect::visit(SE_Spatial* spatial)
{
    SE_Vector3f intersectPoint;
    if(!spatial->isSpatialStateHasAttribute(SE_SpatialAttribute::COLLISIONABLE) || !spatial->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
        return 1;
    SE_BoundingVolume* bv = spatial->getWorldBoundingVolume();
    if(!bv)
        return 0;
    int ret = bv->movingSphereIntersect(sphere, endPoint, &intersectPoint);
    if(ret)
    {
        if(spatial->getSpatialType() == GEOMETRY)
        {
            SE_Vector3f center = sphere.getCenter();
            SE_Vector3f dir = (endPoint - center).normalize();
            float minDist = (location - center).dot(dir);
            float dist = (intersectPoint - center).dot(dir);
            if(minDist > dist)
            {
                location = intersectPoint;
            }
            intersected = true;
        }
        return 0;
    }
    else
        return 1;
}
int SE_MovingSphereStaticSpatialIntersect::visit(SE_SimObject* so)
{
    if (false)
        LOGI("### collision object = %s ####\n", so->getName());
    return 0;
}

SE_MovingSpatialIntersect::SE_MovingSpatialIntersect(SE_Spatial* s)
{
    moveSpatial = s;
}

int SE_MovingSpatialIntersect::visit(SE_Spatial* spatial)
{
    SE_BoundingVolume* bv = spatial->getWorldBoundingVolume();
    if(!bv)
    {
        return 0;
    }
    SE_BoundingVolume* moveBV = moveSpatial->getWorldBoundingVolume();
    if(!moveBV)
    {
        return 1;
    }
    SE_OBB obb;
    SE_AABB aabb;
    SE_Sphere sphere;
    switch(moveBV->getType())
    {
    case AABB:
        aabb = ((SE_AABBBV*)moveBV)->getGeometry();
        obb.createFromAABB(aabb);

        break;
    case OBB:
        obb = ((SE_OBBBV*)moveBV)->getGeometry();
        break;
    case SPHERE:
        sphere = ((SE_SphereBV*)moveBV)->getGeometry();
        break;
    }
    return 0;
}
int SE_MovingSpatialIntersect::visit(SE_SimObject* so)
{
    return 0;
}
