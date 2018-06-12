#include "SE_DynamicLibType.h"
#include "SE_Vector.h"
#include "SE_Math.h"
#include <math.h>
#include "SE_MemLeakDetector.h"
int SE_IsEqual(float a, float b)
{
    if(fabs(a - b) <= SE_FLOAT_EQUAL_EPSILON)
        return 1;
    else
        return 0;
}
float SE_AngleToRadian(float angle)
{
    return angle * SE_PI / 180.0f;
}
float SE_RadianToAngle(float radian)
{
    return radian * 180.0f / SE_PI;
}
float SE_Sinf(float radian)
{
    return sinf(radian);
}
float SE_Cosf(float radian)
{
    return cosf(radian);
}
float SE_Sqrtf(float f)
{
    return sqrtf(f);
}
float SE_Fabs(float f)
{
    return fabs(f);
}
float SE_Tanf(float f)
{
    return tanf(f);
}

float SE_Acosf(float f)
{
    return acos(f);
}
float SE_Asinf(float f)
{
    return asin(f);
}
float SE_Atanf(float f)
{
    return atan(f);
}

float SE_Lerp(float a, float b, float t)
{
    return a + (b-a)*t;
}


SE_Vector3f SE_Math::calculateTangentSpaceVector(
        const SE_Vector3f& position1, const SE_Vector3f& position2, const SE_Vector3f& position3,
        float u1, float v1, float u2, float v2, float u3, float v3)
{
    //side0 is the vector along one side of the triangle of vertices passed in, 
    //and side1 is the vector along another side. Taking the cross product of these returns the normal.
    SE_Vector3f side0 = position1 - position2;
    SE_Vector3f side1 = position3 - position1;
    //Calculate face normal
    SE_Vector3f normal = side1.cross(side0);
    normal.normalize();
    //Now we use a formula to calculate the tangent. 
    float deltaV0 = v1 - v2;
    float deltaV1 = v3 - v1;
    SE_Vector3f tangent = side0.mul(deltaV1) -  side1.mul(deltaV0);
    tangent.normalize();
    //Calculate binormal
    float deltaU0 = u1 - u2;
    float deltaU1 = u3 - u1;
    SE_Vector3f binormal = side0.mul(deltaU1) - side1.mul(deltaU0);
    binormal.normalize();
    //Now, we take the cross product of the tangents to get a vector which 
    //should point in the same direction as our normal calculated above. 
    //If it points in the opposite direction (the dot product between the normals is less than zero), 
    //then we need to reverse the s and t tangents. 
    //This is because the triangle has been mirrored when going from tangent space to object space.
    //reverse tangents if necessary
    SE_Vector3f tangentCross = tangent.cross(binormal);
    if (tangentCross.dot(normal) < 0.0f)
    {
        tangent = tangent.neg();
        binormal = binormal.neg();
    }

    return tangent;

}