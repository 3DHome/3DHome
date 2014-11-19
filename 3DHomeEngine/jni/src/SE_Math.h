#ifndef SE_MATH_H
#define SE_MATH_H
#include "SE_Common.h"
#include <float.h>
#include "SE_Vector.h"
class SE_Vector3f;
#ifdef __cplusplus
extern "C" {
#endif
#define SE_VEC_EQUAL_EPSILON 0.00001
#define SE_FLOAT_EQUAL_EPSILON 0.00001
#define SE_FLT_MAX FLT_MAX
extern int SE_IsEqual(float a, float b);
extern float SE_AngleToRadian(float angle);
extern float SE_RadianToAngle(float radian);
extern float SE_Sinf(float radian);
extern float SE_Cosf(float radian);
extern float SE_Tanf(float radian);
extern float SE_Asinf(float f);
extern float SE_Acosf(float f);
extern float SE_Atanf(float f);
extern float SE_Sqrtf(float f);
extern float SE_Fabs(float f);
extern float SE_Lerp(float a,float b,float t);

#ifdef __cplusplus
}
#endif

class SE_Math
{
public:
    /** Calculates the tangent space vector for a given set of positions / texture coords. */
static SE_Vector3f calculateTangentSpaceVector(
            const SE_Vector3f& position1, const SE_Vector3f& position2, const SE_Vector3f& position3,
            float u1, float v1, float u2, float v2, float u3, float v3);
};
#endif
