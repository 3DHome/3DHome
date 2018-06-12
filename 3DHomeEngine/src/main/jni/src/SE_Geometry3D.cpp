#include "SE_DynamicLibType.h"
#include "SE_Geometry3D.h"
#include "SE_Math.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
static float e[2] = {1, 1};
SE_Rect3D SE_Rect3D::normalizeRect = SE_Rect3D(SE_Vector3f(0, 0, 0), SE_Vector3f(1, 0, 0), SE_Vector3f(0, 1, 0), e);
SE_Rect3D::SE_Rect3D()
{
    mExtent[0] = mExtent[1] = 0;
}
SE_Rect3D::SE_Rect3D(const SE_Vector3f& center, const SE_Vector3f& xAxis, const SE_Vector3f& yAxis, float e[2]) : mCenter(center)
{
    mAxis[0] = xAxis;
    mAxis[1] = yAxis;
    mExtent[0] = e[0];
    mExtent[1] = e[1];
}
SE_Vector3f SE_Rect3D::getCenter() const
{
    return mCenter;
}
SE_Vector3f SE_Rect3D::getXAxis() const
{
    return mAxis[0];
}
SE_Vector3f SE_Rect3D::getYAxis() const
{
    return mAxis[1];
}
void SE_Rect3D::getExtent(float out[2]) const
{
    out[0] = mExtent[0];
    out[1] = mExtent[1];
}
void SE_Rect3D::getVertex(SE_Vector3f* v) const
{
    v[0] = mCenter - mAxis[0] * mExtent[0] - mAxis[1] * mExtent[1];
    v[1] = mCenter + mAxis[0] * mExtent[0] - mAxis[1] * mExtent[1];
    v[2] = mCenter + mAxis[0] * mExtent[0] + mAxis[1] * mExtent[1];
    v[3] = mCenter - mAxis[0] * mExtent[0] + mAxis[1] * mExtent[1];
}
void SE_Rect3D::getFaces(SE_Vector3i* faces) const
{
    faces[0].x = 0;
    faces[0].y = 1;
    faces[0].z = 2;
    faces[1].x = 2;
    faces[1].y = 3;
    faces[1].z = 0;
}
void SE_Rect3D::setCenter(const SE_Vector3f& center)
{
    mCenter = center;
}
void SE_Rect3D::setAxis(const SE_Vector3f& xAxis, const SE_Vector3f& yAxis)
{
    mAxis[0] = xAxis;
    mAxis[1] = yAxis;
}
void SE_Rect3D::setExtent(float e[2])
{
    mExtent[0] = e[0];
    mExtent[1] = e[1];
}
/////////////////////

SE_Segment::SE_Segment()
{
    
}
SE_Segment::SE_Segment(const SE_Vector3f& start, const SE_Vector3f& end) : mStart(start), mEnd(end)
{
}
SE_Segment::SE_Segment(const SE_Vector3f& start, const SE_Vector3f& dir, float len)
{
    mStart = start;
    mEnd = start + dir * len;
}
SE_Vector3f SE_Segment::getStart() const
{
    return mStart;
}
SE_Vector3f SE_Segment::getEnd() const
{
    return mEnd;
}
SE_Vector3f SE_Segment::getDirection() const
{
    return mEnd - mStart;
}
///////////////
SE_Plane::SE_Plane()
{
    mNormal.set(0, 0, 0);
    mDistance = 0;
}
SE_Plane::SE_Plane(const SE_Vector3f& normal, float d) : mNormal(normal), mDistance(d)
{

}
SE_Plane::SE_Plane(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2)
{
    set(p0, p1, p2);
}
SE_Vector3f SE_Plane::getNormal() const
{
    return mNormal;
}
void SE_Plane::set(const SE_Vector3f& normal, float d)
{
    mNormal = normal;
    mDistance = d;
}
SE_Plane SE_Plane::transform(const SE_Matrix4f& m)
{
    SE_Vector4f v(mNormal, -mDistance);
    SE_Vector4f out = m.map(v);
    SE_Vector3f n = out.xyz();
    return SE_Plane(n.normalize(), out.w / n.length());
}
void SE_Plane::set(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2)
{
    SE_Vector3f v0 = p1 - p0;
    SE_Vector3f v1 = p2 - p0;
    SE_Vector3f n = v0.cross(v1);
    if(n.isZero())
    {
        mDistance = 0;
        mNormal.setZero();
    }
    else
    {
        mNormal = n.normalize();
        mDistance = mNormal.dot(p0);
    }
}

float SE_Plane::getDistance() const
{
    return mDistance;
}
SE_Plane_Side SE_Plane::whichSide(const SE_Vector3f& point) const
{
    float ret = mNormal.dot(point) - mDistance;
    if(ret == 0.0f)
        return SE_OTHER;
    else if(ret > 0.0f)
        return SE_POSITIVE;
    else
        return SE_NEGATIVE;
}
float SE_Plane::distance(const SE_Vector3f& point) const
{
    return SE_Fabs(mNormal.dot(point) - mDistance);
}
/////////////////////////
SE_Ray::SE_Ray()
{
}
SE_Ray::SE_Ray(const SE_Vector3f& start, const SE_Vector3f& end)
{
    mOrigin = start;
    SE_Vector3f dir = end - start;
    if(dir.isZero())
    {
        mDir.setZero();
    }
    else
        mDir = dir.normalize();
}
SE_Ray::SE_Ray(const SE_Vector3f& org, const SE_Vector3f& dir, bool normalized)
{
    mOrigin = org;
    if(!normalized)
        mDir = dir.normalize();
    else 
        mDir = dir;
}
SE_Vector3f SE_Ray::getOrigin() const
{
    return mOrigin;
}
SE_Vector3f SE_Ray::getDirection() const
{
   return mDir;
}
////////////////////////
SE_Triangle::SE_Triangle()
{}
SE_Triangle::SE_Triangle(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2) : mP0(p0), mP1(p1), mP2(p2)
{
}
bool SE_Triangle::isCollinear() const
{
    SE_Vector3f v0 = mP1 - mP0;
    SE_Vector3f v1 = mP2 - mP0;
    float d = v0.dot(v1) / (v0.length() * v1.length());
    if(d == 1.0f)
        return true;
    else
        return false;
}
void SE_Triangle::getPoint(SE_Vector3f point[3]) const
{
    point[0] = mP0;
    point[1] = mP1;
    point[2] = mP2;
}
void SE_Triangle::set(const SE_Vector3f& p0, const SE_Vector3f& p1, const SE_Vector3f& p2)
{
    mP0 = p0;
    mP1 = p1;
    mP2 = p2;
}
SE_IntersectResult SE_Triangle::intersect(const SE_Ray& ray) const
{
    SE_Vector3f origin, dir;
    SE_Vector3f normal;
    float d;
    SE_Vector3f p0, p1, p2;
    SE_Plane plane;
    SE_Vector3f p; /*the intersection of ray and plane*/
    float t;
    float nvDot; /*normal and dir dot*/
    SE_Vector3f r, q1, q2;
    SE_Matrix2f inverseM; /*matrix computed by q1, q2*/
    SE_Vector2f v; /*vector computed by r and q1, q2*/
    SE_Vector2f bc; /*barycentric coordinate w1, w2*/
    float w0; /*barycentric coordiate w0 */
    float data[4];
    SE_IntersectResult ret;
    plane.set(mP0, mP1, mP2);
    if(isCollinear())
    {
        //do intersection test for collinear
        return ret;
    }
    origin = ray.getOrigin();
    dir = ray.getDirection();
    normal = plane.getNormal();
    d = plane.getDistance();
    nvDot = normal.dot(dir);
    if(nvDot == 0.0f)
    {
        ret.intersected = false;
        return ret;
    }
    t = (d - normal.dot(origin)) / nvDot;
    if(t < 0)
    {
        ret.intersected = false;
        return ret;
    }
    p = origin + dir.mul(t);
    r = p - mP0;
    q1 = mP1 - mP0;
    q2 = mP2 - mP0;
    v.x = r.dot(q1);
    v.y = r.dot(q2);
    data[0] = q1.dot(q1);
    data[1] = q1.dot(q2);
    data[2] = q1.dot(q2);
    data[3] = q2.dot(q2);
    SE_Matrix2f m(data);
    if(!m.hasInverse())
    {
        ret.intersected = false;
        return ret;
    }
    inverseM = m.inverse();
    bc = inverseM.map(v);
    if((bc.x + bc.y) <= 1.0f && bc.x >= 0.0f && bc.y >= 0.0f)
    {
        w0 = 1 - bc.x - bc.y;
        ret.intersected = true;
        ret.intersectPoint.resize(2);
        ret.intersectPoint[0].x = w0;
        ret.intersectPoint[0].y = bc.x;
        ret.intersectPoint[0].z = bc.y;
        ret.intersectPoint[1].x = p.x;
        ret.intersectPoint[1].y = p.y;
        ret.intersectPoint[1].z = p.z;
        ret.distance.resize(1);
        ret.distance[0] = p.distance(origin);
        return ret;
    }
    else
    {
        ret.intersected = false;
        return ret;
    }
}
///////////////////////
SE_Frustum::SE_Frustum()
{
    mFovAngle = mRatio = mNear = mFar = 0;
}
SE_Frustum::SE_Frustum(float fovAngle, float ratio, float n, float f)
{
    set(fovAngle, ratio, n, f);
}
void SE_Frustum::set(float fovAngle, float ratio, float n, float f)
{
    float fovRadian, e, param1, param2;
    fovRadian = SE_AngleToRadian(fovAngle);
    e = 1.0f / SE_Tanf(fovRadian / 2);
    param1 = 1.0f / SE_Sqrtf(e * e + 1);
    param2 = 1.0f / SE_Sqrtf(e * e + ratio * ratio);
    SE_Vector3f v(e / param1, 0, -1 / param1);
    mLeftp.set(v, 0);
    v.set(-e / param1, 0, -1 / param1);
    mRightp.set(v, 0);
    v.set(0, -e / param2, -ratio / param2);
    mTopp.set(v, 0);
    v.set(0, e / param2, -ratio / param2);
    mBottomp.set(v, 0);
    v.set(0,0, -1);
    mNearp.set(v, n);
    v.set(0, 0, 1);
    mFarp.set(v, -f);
    mNear = n;
    mFar = f;
    mFovAngle = fovAngle;
    mRatio = ratio;
}
SE_Rect<float> SE_Frustum::getNearPlaneRect() const
{
    SE_Rect<float> out;
    float fovRadian, e;
    fovRadian = SE_AngleToRadian(mFovAngle);
    e = 1.0 / SE_Tanf(fovRadian / 2);
    out.left = - mNear / e;
    out.right = mNear / e;
    out.top = mNear * mRatio / e;
    out.bottom = - mNear * mRatio / e;
    return out;
}
SE_Matrix4f SE_Frustum::getPerspectiveMatrix() const
{
    SE_Matrix4f out;
    SE_Rect<float> nearrect;
    float n, f;
    float l, r, b , t;
    nearrect = getNearPlaneRect();
    n = mNear;
    f = mFar;
    l = nearrect.left;
    r = nearrect.right;
    b = nearrect.bottom;
    t = nearrect.top;
    /*
    out.set(0, 0, 2  / (r - l));
    out.set(0, 1,  0);
    out.set(0, 2,  0);
    out.set(0, 3, -(r + l) / (r - l));
    out.set(1, 0, 0);
    out.set(1, 1, 2/ (t - b));
    out.set(1, 2, 0);
    out.set(1, 3, -(t + b) / (t - b));
    out.set(2, 0, 0);
    out.set(2, 1, 0);
    out.set(2, 2, -2 / (f - n));
    out.set(2, 3, -(f + n) / (f - n));
    out.set(3, 0, 0);
    out.set(3, 1, 0);
    out.set(3, 2, 0);
    out.set(3, 3, 1);
    */
    out.set(0, 0, (2 * n) / (r - l));
    out.set(0, 1,  0);
    out.set(0, 2,  (r + l) / (r - l));
    out.set(0, 3, 0);
    out.set(1, 0, 0);
    out.set(1, 1, (2 * n) / (t - b));
    out.set(1, 2, (t + b) / (t - b));
    out.set(1, 3, 0);
    out.set(2, 0, 0);
    out.set(2, 1, 0);
    out.set(2, 2, -(f + n) / (f - n));
    out.set(2, 3, -(2 * n * f) / (f - n));
    out.set(3, 0, 0);
    out.set(3, 1, 0);
    out.set(3, 2, -1);
    out.set(3, 3, 0);
    return out;
}
SE_Plane SE_Frustum::getLeftPlane() const
{
    return mLeftp;
}
SE_Plane SE_Frustum::getRightPlane() const
{
    return mRightp;
}
SE_Plane SE_Frustum::getTopPlane() const
{
    return mTopp;
}
SE_Plane SE_Frustum::getBottomPlane() const
{
    return mBottomp;
}
SE_Plane SE_Frustum::getFarPlane() const
{
    return mFarp;
}
SE_Plane SE_Frustum::getNearPlane() const
{
    return mNearp;
}
float SE_Frustum::getNear() const
{
    return mNear;
}
float SE_Frustum::getFar() const
{
    return mFar;
}
////////////////////////////////////////////
SE_Sphere::SE_Sphere()
{
    mRadius = 0;
}
SE_Sphere::SE_Sphere(const SE_Vector3f& center, float r) : mCenter(center), mRadius(r)
{}
void SE_Sphere::set(const SE_Vector3f& center, float r)
{
    mCenter = center;
    mRadius = r;
}
void SE_Sphere::mostSeparatedPointsOnAABB(int* min , int* max, SE_Vector3f* points, int numPoint)
{
    int minx = 0, miny = 0, minz = 0, maxx = 0, maxy = 0, maxz = 0;
    SE_Vector3f xAxisDir, yAxisDir, zAxisDir;
    int i;
    float dist2x, dist2y, dist2z;
    for(i = 1 ; i < numPoint ; i++)
    {
        if(points[i].x < points[minx].x)
            minx = i;
        if(points[i].y < points[miny].y)
            miny = i;
        if(points[i].z < points[minz].z)
            minz = i;
        if(points[i].x > points[maxx].x)
            maxx = i;
        if(points[i].y > points[maxy].y)
            maxy = i;
        if(points[i].z > points[maxz].z)
            maxz = i;
    }
    xAxisDir = points[maxx] - points[minx];
    yAxisDir = points[maxy] - points[miny];
    zAxisDir = points[maxz] - points[minz];
    dist2x = xAxisDir.lengthSquare();
    dist2y = yAxisDir.lengthSquare();
    dist2z = zAxisDir.lengthSquare();
    *min = minx;
    *max = maxx;
    if(dist2y > dist2x && dist2y > dist2z)
    {
        *max = maxy;
        *min = miny;
    }
    if(dist2z > dist2x && dist2z > dist2y)
    {
        *max = maxz;
        *min = minz;
    }

}
void SE_Sphere::sphereFromDistantPoints(SE_Vector3f* points, int pointNum)
{
    int min, max;
    SE_Vector3f minP;
    SE_Vector3f maxP;
    mostSeparatedPointsOnAABB(&min, &max, points, pointNum);
    minP = points[min];
    maxP = points[max];
    mCenter = (minP + maxP) * 0.5;
    mRadius = (points[max] - mCenter).length();
}
void SE_Sphere::sphereOfSphereAndPoint(const SE_Vector3f& point)
{
    SE_Vector3f d, tmp1, tmp2;
    float dist2;
    d = point - mCenter;
    dist2 = d.lengthSquare();
    if(dist2 > mRadius * mRadius)
    {
        float dist = SE_Sqrtf(dist2);
        float newRadius = (mRadius + dist) * 0.5f;
        float k = (newRadius - mRadius) / dist;
        mRadius = newRadius;
        mCenter = d * k + mCenter;
    }
}

void SE_Sphere::ritterSphere(SE_Vector3f* points, int num)
{
    int i;
    sphereFromDistantPoints(points, num);
    for(i = 0 ; i < num ; i++)
    {
        sphereOfSphereAndPoint(points[i]);
    }
    
}
void SE_Sphere::createFromPoints(SE_Vector3f* points, int num)
{
    ritterSphere(points, num);
}
SE_Vector3f SE_Sphere::getCenter() const
{
    return mCenter;
}
float SE_Sphere::getRadius() const
{
    return mRadius;
}

SE_IntersectResult SE_Sphere::intersect(const SE_Ray& ray) const
{
    SE_IntersectResult ret;
    SE_Vector3f origin = ray.getOrigin();
    SE_Vector3f dir = ray.getDirection();
    SE_Vector3f dist = origin - mCenter;
    float b, c; // quadratic equation b and c : t2 + 2bt + c = 0
    b = dist.dot(dir);
    c = dist.dot(dist) - mRadius * mRadius;
    if(c > 0.0f && b > 0.0f)
    {
        ret.intersected = 0;
        return ret;
    }
    float discr = b * b - c;
    if(discr < 0.0f)
    {
        ret.intersected = 0;
        return ret;
    }
    float t = -b - SE_Sqrtf(discr);
    if(t < 0.0f)
    {
        t = 0.0f;
    }
    SE_Vector3f resultP = dir.mul(t);
    ret.intersected = 1;
    ret.intersectPoint.resize(1);
    ret.intersectPoint[0] = origin + resultP;
    ret.distance.resize(1);
    ret.distance[0] = resultP.length();
    return ret;
}

SE_Plane_Side SE_Sphere::whichSide(const SE_Plane& plane) const
{
    return SE_NEGATIVE;
}
SE_IntersectResult SE_Sphere::intersect(const SE_Sphere& sphere) const
{
    return SE_IntersectResult();
}
bool SE_Sphere::containPoint(const SE_Vector3f& point) const
{
    SE_Vector3f d = mCenter - point;
    float dist2 = d.lengthSquare();
    if(dist2 > mRadius * mRadius)
    {
        return false;
    }
    else
        return true;
    
}
//////////////////////////////////////////////
SE_AABB::SE_AABB()
{}
SE_AABB::SE_AABB(const SE_Vector3f& min, const SE_Vector3f& max) : mMin(min), mMax(max)
{}
void SE_AABB::createFromPoints(SE_Vector3f* points, int num)
{
    int i;
    SE_Vector3f min = points[0];
    SE_Vector3f max = points[0];
    for(i = 1 ; i < num ; i++)
    {
        SE_Vector3f& v = points[i];
        if(v.x < min.x)
            min.x = v.x;
        if(v.y < min.y)
            min.y = v.y;
        if(v.z < min.z)
            min.z = v.z;
        if(v.x > max.x)
            max.x = v.x;
        if(v.y > max.y)
            max.y = v.y;
        if(v.z > max.z)
            max.z = v.z;
    }
    mMin = min;
    mMax = max;
}
SE_Vector3f SE_AABB::getMin() const
{
    return mMin;
}
SE_Vector3f SE_AABB::getMax() const
{
    return mMax;
}
SE_Vector3f SE_AABB::getExtent() const
{
    return mMax - mMin;
}
SE_Vector3f SE_AABB::getCenter() const
{
    return mMin + (mMax - mMin) * 0.5;
}
SE_IntersectResult SE_AABB::intersect(const SE_AABB& aabb) const
{
    SE_IntersectResult ret;
    if(mMax.x < aabb.mMin.x || mMin.x > aabb.mMax.x)
        return ret;
    if(mMax.y < aabb.mMin.y || mMin.y > aabb.mMax.y)
        return ret;
    if(mMax.z < aabb.mMin.z || mMin.z > aabb.mMax.z)
        return ret;
    ret.intersected = true;
    return ret;
    
}


SE_IntersectResult SE_AABB::intersect(const SE_Ray& ray) const
{
    SE_IntersectResult ret;
    float tmin = -SE_FLT_MAX;
    float tmax = SE_FLT_MAX;
    float t1, t2;
    SE_Vector3f dir = ray.getDirection();
    SE_Vector3f origin = ray.getOrigin(); 
    for(int i = 0 ; i < 3 ; i++)
    {
        if(SE_Fabs(dir.d[i]) < SE_FLOAT_EQUAL_EPSILON)
        {
            if(origin.d[i] < mMin.d[i] || origin.d[i] > mMax.d[i])
            {
                ret.intersected = false;
                return ret;
            }
        }
        else
        {
            float ood = 1.0f / dir.d[i];
            t1 = (mMin.d[i] - origin.d[i]) * ood;
            t2 = (mMax.d[i] - origin.d[i]) * ood;
            if(t1 > t2)
            {
                float tmp;
                tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            if(t1 > tmin)
                tmin = t1;
            if(t2 < tmax)
                tmax = t2;
            if(tmin > tmax)
            {
                ret.intersected = false;
                return ret;
            }
            if(tmax < 0)
            {
                ret.intersected = false;
                return ret;
            }
        }
    }
    ret.intersected = true;
    ret.distance.resize(1);
    ret.distance[0] = tmin;
    ret.intersectPoint.resize(1);
    ret.intersectPoint[0] = origin + dir.mul(tmin);
    return ret;
}
SE_IntersectResult SE_AABB::intersect(const SE_Plane& plane) const
{
    SE_Vector3f center = getCenter();
    SE_Vector3f extent = mMax - center;
    SE_Vector3f normal = plane.getNormal();
    float r = extent.x * SE_Fabs(normal.x) + extent.y * SE_Fabs(normal.y) + extent.z * SE_Fabs(normal.z);
    float s = normal.dot(center) - plane.getDistance();
    SE_IntersectResult ret;
    if(SE_Fabs(s) <= r)
        ret.intersected = true;
    else
        ret.intersected = false;
    return ret;

}
SE_Plane_Side SE_AABB::whichSide(const SE_Plane& plane) const
{
    SE_IntersectResult ret = intersect(plane);
    if(ret.intersected)
        return SE_OTHER;
    SE_Vector3f center = getCenter();
    return plane.whichSide(center);
}
void SE_AABB::set(const SE_Vector3f& min, const SE_Vector3f& max)
{
    mMin = min;
    mMax = max;
}
void SE_AABB::getEdge(SE_Segment edge[12])
{
    SE_Vector3f p0, p1;
    //edge 1
    p0.x = mMin.x;
    p0.y = mMin.y;
    p0.z = mMin.z;

    p1.x = mMax.x;
    p1.y = mMin.y;
    p1.z = mMin.z;
    edge[0].set(p0, p1);
    //edge 2
    p0.x = mMax.x;
    p0.y = mMin.y;
    p0.z = mMin.z;

    p1.x = mMax.x;
    p1.y = mMin.y;
    p1.z = mMax.z;
    edge[1].set(p0, p1);

    //edge 3
    p0.x = mMax.x;
    p0.y = mMin.y;
    p0.z = mMax.z;

    p1.x = mMin.x;
    p1.y = mMin.y;
    p1.z = mMax.z;
    edge[2].set(p0, p1);

    //edge 4
    p0.x = mMin.x;
    p0.y = mMin.y;
    p0.z = mMax.z;
    
    p1.x = mMin.x;
    p1.y = mMin.y;
    p1.z = mMin.z;
    edge[3].set(p0, p1);

    //edge 5
    p0.x = mMin.x;
    p0.y = mMin.y;
    p0.z = mMin.z;

    p1.x = mMin.x;
    p1.y = mMax.y;
    p1.z = mMin.z;
    edge[4].set(p0, p1);

    //edge 6
    p0.x = mMin.x;
    p0.y = mMax.y;
    p0.z = mMin.z;
    
    p1.x = mMax.x;
    p1.y = mMax.y;
    p1.z = mMin.z;
    edge[5].set(p0, p1);

    //edge 7
    p0.x = mMax.x;
    p0.y = mMax.y;
    p0.z = mMin.z;

    p1.x = mMax.x;
    p1.y = mMin.y;
    p1.z = mMin.z;
    edge[6].set(p0, p1);

    //edge 8
    p0.x = mMax.x;
    p0.y = mMax.y;
    p0.z = mMax.z;
    
    p1.x = mMin.x;
    p1.y = mMax.y;
    p1.z = mMax.z;
    edge[7].set(p0, p1);

    //edge 9
    p0.x = mMin.x;
    p0.y = mMax.y;
    p0.z = mMax.z;

    p1.x = mMin.x;
    p1.y = mMax.y;
    p1.z = mMin.z;
    edge[8].set(p0, p1);

    //edge 10
    p0.x = mMax.x;
    p0.y = mMax.y;
    p0.z = mMax.z;

    p1.x = mMax.x;
    p1.y = mMin.y;
    p1.z = mMax.z;
    edge[9].set(p0, p1);

    //edge 11
    p0.x = mMax.x;
    p0.y = mMax.y;
    p0.z = mMax.z;

    p1.x = mMax.x;
    p1.y = mMax.y;
    p1.z = mMin.z;
    edge[10].set(p0, p1);

    //edge 12
    p0.x = mMin.x;
    p0.y = mMax.y;
    p0.z = mMax.z;

    p1.x = mMin.x;
    p1.y = mMin.y;
    p1.z = mMax.z;
    edge[11].set(p0, p1);
}
/////////////////////////////////////////////////
SE_OBB::SE_OBB()
{
    mExtent[0] = mExtent[1] = mExtent[2] = 0;
}
void SE_OBB::createFromPoints(SE_Vector3f* points, int num)
{}
void SE_OBB::createFromAABB(const SE_AABB& aabb)
{
    mCenter = aabb.getCenter();
    SE_Vector3f extent = aabb.getExtent();
    mAxis[0].set(1, 0, 0);
    mAxis[1].set(0, 1, 0);
    mAxis[2].set(0, 0, 1);
    mExtent[0] = extent.x / 2;
    mExtent[1] = extent.y / 2;
    mExtent[2] = extent.z / 2;
}
void SE_OBB::getBoxVertex(SE_Vector3f out[8]) const
{}
SE_Vector3f SE_OBB::getCenter() const
{
    return mCenter;
}
void SE_OBB::getAxis(SE_Vector3f axis[3]) const
{
    axis[0] = mAxis[0];
    axis[1] = mAxis[1];
    axis[2] = mAxis[2];
}
void SE_OBB::getExtent(float e[3]) const
{
    e[0] = mExtent[0];
    e[1] = mExtent[1];
    e[2] = mExtent[2];
}
void SE_OBB::transfrom(const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate)
{
    mCenter = mCenter + translate;
    for(int i = 0 ; i < 3 ; i++)
    {
        mExtent[i] *= scale.d[i];
    }
    SE_Vector3f xAxis = rotate.map(mAxis[0]);
    SE_Vector3f yAxis = rotate.map(mAxis[1]);
    SE_Vector3f zAxis = rotate.map(mAxis[2]);
    xAxis = xAxis.normalize();
    yAxis = yAxis.normalize();
    zAxis = zAxis.normalize();
#ifdef DEBUG
    if(!(xAxis.cross(yAxis) == zAxis))
    {
        LOGI("### rotate can not equal\n");
    }
#endif 
    zAxis = xAxis.cross(yAxis);
    zAxis = zAxis.normalize();
    mAxis[0] = xAxis;
    mAxis[1] = yAxis;
    mAxis[2] = zAxis;
}

SE_IntersectResult SE_OBB::intersect(const SE_OBB& obb) const
{
    float ra, rb;
    float x, y, z;
    SE_Matrix3f R, AbsR;
    int i , j;
    SE_Vector3f centerDistV, t;
    SE_Vector3f obbAxis[3];
    float obbExtent[3];
    SE_IntersectResult ret;
    obb.getAxis(obbAxis);
    obb.getExtent(obbExtent);
    for(i = 0 ; i < 3 ; i++)
    {
        for(j = 0 ; j < 3 ; j++)
        {
            R.set(i, j,  mAxis[i].dot(obbAxis[i]));
        }
    }
    centerDistV = obb.getCenter() - mCenter;
    x = centerDistV.dot(mAxis[0]);
    y = centerDistV.dot(mAxis[1]);
    z = centerDistV.dot(mAxis[2]);
    t.set(x, y, z);
    for(i = 0 ; i < 3 ; i++)
    {
        for(j = 0 ; j < 3 ; j++)
        {
            AbsR.set(i, j,  SE_Fabs(R(i , j) + SE_FLOAT_EQUAL_EPSILON));
        }
    }
    /*test axes L = A0, L = A1, L = A2*/
    for(i = 0 ; i < 3 ; i++)
    {
        ra = mExtent[i];
        rb = obbExtent[0] * AbsR(i, 0) + obbExtent[1] * AbsR(i , 1) + obbExtent[2] * AbsR(i , 2);
        if(SE_Fabs(t.d[i]) > (ra + rb))
            return ret;
    }
    /*test axes L = B0, L = B1, L = B2*/
    for(i = 0 ; i < 3 ; i++)
    {
        ra = mExtent[0] * AbsR(0, i) + mExtent[1] * AbsR(1 , i) + mExtent[2] * AbsR(2 , i);
        rb = obbExtent[i];
        if(SE_Fabs(t.d[0] * R(0, i) + t.d[1] * R(1 , i) + t.d[2] * R(2 ,i)) > ra + rb)
            return ret;
    }
    /*test axis L = A0 * B0 */
    ra = mExtent[1] * AbsR(2, 0) + mExtent[2] * AbsR(1, 0);
    rb = obbExtent[1] * AbsR(0, 2) + obbExtent[2] * AbsR(0, 1);
    if(SE_Fabs(t.d[2] * R(1, 0) - t.d[1] * R(2, 0)) > (ra + rb))
        return ret;
    /*test axis L = A0 * B1 */
    ra = mExtent[1] * AbsR(2, 1) + mExtent[2] * AbsR(1 ,  1);
    rb = obbExtent[0] * AbsR(0, 2) + obbExtent[2] * AbsR(0, 0);
    if(SE_Fabs(t.d[2] * R(1 , 1) - t.d[1] * R(2 ,  1)) > (ra + rb))
        return ret;
    /*test axis L = A0 * A2 */
    ra = mExtent[1] * AbsR(2 ,  2) + mExtent[2] * AbsR(1 ,  2);
    rb = obbExtent[0] * AbsR(0, 1) + obbExtent[1] * AbsR(0, 0);
    if(SE_Fabs(t.d[2] * R(1 ,  2) - t.d[1] * R(2 ,  2)) > (ra + rb))
        return ret;
    /*tes axis L = A1 * B0*/
    ra = mExtent[0] * AbsR(2 , 0) + mExtent[2] * AbsR(0 , 0);
    rb = obbExtent[1] * AbsR(1 ,  2) + obbExtent[2] * AbsR(1 , 1);
    if(SE_Fabs(t.d[0] * R(2, 0) - t.d[2] * R(0, 0)) > (ra + rb))
        return ret;
    /* test axis L = A1 * B1 */
    ra = mExtent[0] * AbsR(2 ,  1) + mExtent[2] * AbsR(0, 1);
    rb = obbExtent[0] * AbsR(1 ,  2) + obbExtent[2] * AbsR(1, 0);
    if(SE_Fabs(t.d[0] * R(2 , 1) - t.d[2] * R(0, 1)) > (ra + rb))
        return ret;
    
    /* test axis A1 * B2 */
    ra = mExtent[0] * AbsR(2 , 2) + mExtent[2] * AbsR(0, 2);
    rb = obbExtent[0] * AbsR(1 ,  1) + obbExtent[1] * AbsR(1 , 0);
    if(SE_Fabs(t.d[0] * R(2 , 2) - t.d[2] * R(0, 2)) > (ra + rb))
        return ret;

    /* test A2 * B0 */
    ra = mExtent[0] * AbsR(1 , 0) + mExtent[1] * AbsR(0, 0);
    rb = obbExtent[1] * AbsR(2 ,  2) + obbExtent[2] * AbsR(2 , 1);
    if(SE_Fabs(t.d[1] * R(0, 0) - t.d[0] * R(1 , 0)) > (ra + rb))
        return ret;

    /*test A2 * B1 */
    ra = mExtent[0] * AbsR(1 ,  1) + mExtent[1] * AbsR(0, 1);
    rb = obbExtent[0] * AbsR(2 ,  2) + obbExtent[2] * AbsR(2 , 0);
    if(SE_Fabs(t.d[1] * R(0, 1) - t.d[0] * R(1 ,  1)) > (ra + rb))
        return ret;
    
    /*test A2 * B2 */
    ra = mExtent[0] * AbsR(1 , 2) + mExtent[1] * AbsR(0, 2);
    rb = obbExtent[0] * AbsR(2 ,  1) + obbExtent[1] * AbsR(2 , 0);
    if(SE_Fabs(t.d[1] * R(0, 2) - t.d[0] * R(1 ,  2)) > (ra + rb))
        return ret;
    ret.intersected = true;
    return ret;


}

SE_IntersectResult SE_OBB::intersect(const SE_Ray& ray) const
{
    return SE_IntersectResult();
}
SE_Plane_Side SE_OBB::whichSide(const SE_Plane& plane) const
{
    return SE_NEGATIVE;
}
void SE_OBB::set(const SE_Vector3f& center, SE_Vector3f axis[3], float extent[3])
{
    mCenter = center;
    for(int i = 0 ; i < 3 ; i++)
    {
        mAxis[i] = axis[i];
        mExtent[i] = extent[3];
    }
}
float SE_GeometryIntersect::pointAABBDistanceSquare(const SE_Vector3f& point, const SE_AABB& aabb)
{
    float sqDist = 0.0f;
    int i;
    const SE_Vector3f& min = aabb.getMin();
    const SE_Vector3f& max = aabb.getMax();
    for(i = 0 ; i < 3 ; i++)
    {
        float v = point.d[i];
        if(v < min.d[i]) sqDist += (min.d[i] - v) * (min.d[i] - v);
        if(v > max.d[i]) sqDist += (v - max.d[i]) * (v - max.d[i]);
    }
    return sqDist;
}
int SE_GeometryIntersect::sphereAABB(const SE_Sphere& sphere, const SE_AABB& aabb)
{
    float sqDist = pointAABBDistanceSquare(sphere.getCenter(), aabb);
    return sqDist <= sphere.getRadius() * sphere.getRadius();
}
int SE_GeometryIntersect::movingSphereStaticAABB(const SE_Sphere& sphere, const SE_AABB& aabb, 
                                     const SE_Vector3f& endPoint, SE_Vector3f* out)
{
    SE_Sphere s;
    SE_Vector3f midDir;
    float r, interval;
    midDir = (endPoint - sphere.getCenter()).mul(0.5);
    s.set(sphere.getCenter() + midDir, midDir.length() + sphere.getRadius());
    if(!SE_GeometryIntersect::sphereAABB(s, aabb))
        return 0;
    interval = (endPoint - sphere.getCenter()).length();
    if(interval < 2.0f)
    {
        *out = sphere.getCenter();
        return 1;
    }
    if(movingSphereStaticAABB(sphere, aabb, s.getCenter(), out))
        return 1;
    return movingSphereStaticAABB(s, aabb, endPoint, out);
}
int SE_GeometryIntersect::movingSphereStaticPlane(const SE_Sphere& sphere, const SE_Plane& plane, const SE_Vector3f& dirOfSphere, SE_Vector3f* out)
{
    SE_Vector3f planeNormal = plane.getNormal();
    float planeD = plane.getDistance();
    float dist = planeNormal.dot(sphere.getCenter()) - planeD;
    if(SE_Fabs(dist) <= sphere.getRadius())
    {
        *out = sphere.getCenter();
        return 1;
    }
    else
    {
        float denom = planeNormal.dot(dirOfSphere);
        if(denom * dist >= 0.0f)
        {
            return 0;
        }
        else
        {
            float r = dist >0.0f ? sphere.getRadius() : -sphere.getRadius();
            float t = (r - dist) / denom;
            if(t >= 0.0f && t <= 1.0f)
            {
                *out = dirOfSphere.mul(t) + sphere.getCenter() - planeNormal.mul(r);
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }
}
int SE_GeometryIntersect::movingOBBStaticAABB(const SE_OBB obb, const SE_AABB& aabb, SE_AXIS_TYPE axis, float dist, SE_OBB* out)
{
    SE_Vector3f moveDir;
    SE_Vector3f obbAxis[3];
    obb.getAxis(obbAxis);
    switch(axis)
    {
    case SE_AXIS_X:
        moveDir = obbAxis[0].mul(dist);
        break;
    case SE_AXIS_Y:
        moveDir = obbAxis[1].mul(dist);
        break;
    case SE_AXIS_Z:
        moveDir = obbAxis[2].mul(dist);
        break;
    }
    SE_Vector3f endCenter = obb.getCenter() + moveDir;
    SE_Vector3f aabbCenter = aabb.getCenter();
    SE_Vector3f aabbExtent = aabb.getExtent();
    SE_OBB aabbObb;
    SE_Vector3f aabbObbAxis[3];
    aabbObbAxis[0].set(1, 0, 0);
    aabbObbAxis[1].set(0, 1, 0);
    aabbObbAxis[2].set(0, 0, 1);
    float aabbObbExtent[3];
    aabbObbExtent[0] = SE_Fabs(aabbExtent.x / 2);
    aabbObbExtent[1] = SE_Fabs(aabbExtent.y / 2);
    aabbObbExtent[2] = SE_Fabs(aabbExtent.z / 2);
    aabbObb.set(aabbCenter, aabbObbAxis, aabbObbExtent);
    SE_Vector3f distV = endCenter - obb.getCenter();
    SE_Vector3f mid = distV.mul(0.5);
    SE_Vector3f obbWithEndCenter = obb.getCenter() + mid;
    SE_Vector3f obbWithEndAxis[3];
    obb.getAxis(obbWithEndAxis);
    float obbWithEndExtent[3];
    obb.getExtent(obbWithEndExtent);
    int index = 0;
    switch(axis)
    {
    case SE_AXIS_X:
        index = 0;
        break;
    case SE_AXIS_Y:
        index = 1;
        break;
    case SE_AXIS_Z:
        index = 2;
        break;
    }
    float moveAxisExtent = distV.length() + 2 * obbWithEndExtent[index];
    obbWithEndExtent[index] = moveAxisExtent / 2;
    SE_OBB obbWithEnd;
    obbWithEnd.set(obbWithEndCenter, obbWithEndAxis, obbWithEndExtent);
    SE_IntersectResult ret = aabbObb.intersect(obbWithEnd);
    if(!ret.intersected)
        return 0;
    float interval = distV.length();
    if(interval <= 2.0f)
    {
        *out = obb;
        return 1;
    }
    if(movingOBBStaticAABB(obb, aabb, axis, dist / 2, out))
        return 1;
    obb.getAxis(obbWithEndAxis);
    obb.getExtent(obbWithEndExtent);
    obbWithEnd.set(obbWithEndCenter, obbWithEndAxis, obbWithEndExtent);
    return movingOBBStaticAABB(obbWithEnd, aabb, axis, dist / 2, out);

}
float SE_GeometryIntersect::pointDistance(const SE_Vector3f& p1, const SE_Vector3f& p2)
{
    SE_Vector3f t = p1 - p2;
    return t.length();
}
void SE_GeometryIntersect::pointDistanceToLine(const SE_Vector3f& point, const SE_Vector3f& start, const SE_Vector3f& dir, float& distance, SE_Vector3f& intersectPoint)
{
    SE_Vector3f ps = point - start;
    SE_Vector3f projV = dir.mul((ps.dot(dir) / dir.dot(dir)));
    float distSquare = ps.dot(ps) - projV.dot(projV);
    distance = SE_Sqrtf(distSquare);
    float t = ps.dot(dir);
    intersectPoint = start + projV;
    /*
    SE_Vector3f dirNormalize = dir.normalize();
    if(t > 0)
    {
        intersectPoint = start + dirNormalize * projV.length();
    }
    else if(t == 0)
    {
        intersectPoint = start;
    }
    else
    {
        intersectPoint = start - dirNormalize * projV.length();
    }
    */
}
