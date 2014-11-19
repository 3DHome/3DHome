#include "SE_DynamicLibType.h"
#include "SE_Quat.h"
#include "SE_Matrix.h"
#include "SE_Math.h"
#include "SE_Common.h"
#include <string.h>
#include "SE_MemLeakDetector.h"
const SE_Matrix2f SE_Matrix2f::IDENTITY = SE_Matrix2f(1.0f, 0.0f, 0.0f, 1.0f);
SE_Matrix2f::SE_Matrix2f()
{
    memset(d, 0, sizeof(float) * 4);
}
SE_Matrix2f::SE_Matrix2f(float d[4])
{
    for(int i = 0 ; i < 4 ; i++)
    {
        this->d[i] = d[i];
    }
}
SE_Matrix2f::SE_Matrix2f(float m00, float m01, float m10, float m11)
{
    this->m00 = m00;
    this->m01 = m01;
    this->m10 = m10;
    this->m11 = m11;
}
SE_Matrix2f::SE_Matrix2f(const SE_Vector2f& r0, const SE_Vector2f& r1)
{
    this->m00 = r0.x;
    this->m01 = r0.y;
    this->m10 = r1.x;
    this->m11 = r1.y;
}
float SE_Matrix2f::operator()(int row, int column)
{
    return d[row * 2 + column];
}
bool SE_Matrix2f::operator ==(const SE_Matrix2f& right) const
{
    for(int i = 0 ; i < 4 ; i++)
    {
        if(this->d[i] != right.d[i])
            return false;
    }
    return true;
}
SE_Matrix2f::SE_Matrix2f(const SE_Matrix2f& rm)
{
    for(int i = 0 ; i < 4 ; i++)
    {
        d[i] = rm.d[i];
    }
}
SE_Matrix2f& SE_Matrix2f::operator=(const SE_Matrix2f& rm)
{
    if(this == &rm)
        return *this;
    for(int i = 0 ; i < 4 ; i++)
    {
        d[i] = rm.d[i];
    }
    return *this;
}
void SE_Matrix2f::identity()
{
    m00 = 1.0f;
    m01 = 0.0f;
    m10 = 0.0f;
    m11 = 1.0f;
}
float SE_Matrix2f::get(int row , int column) const
{
    SE_ASSERT(row >= 0 && row < 2);
    SE_ASSERT(column >= 0 && row < 2);
    return d[row * 2 + column];
}
SE_Vector2f SE_Matrix2f::map(const SE_Vector2f& v) const
{
    return SE_Vector2f(m00 * v.x + m01 * v.y , m10 * v.x + m11 * v.y);
}
SE_Matrix2f SE_Matrix2f::mulScalar(float f) const
{
    return SE_Matrix2f(m00 * f, m01 * f, m10 * f, m11 * f);
}
SE_Matrix2f SE_Matrix2f::mul(const SE_Matrix2f& rm) const
{
    SE_Matrix2f ret;
    ret.m00 = m00 * rm.m00 + m01 * rm.m10;
    ret.m01 = m00 * rm.m01 + m01 * rm.m11;
    ret.m10 = m10 * rm.m00 + m11 * rm.m10;
    ret.m11 = m10 * rm.m01 + m11 * rm.m11;
    return ret;
}
SE_Matrix2f SE_Matrix2f::inverse() const
{
    float det = this->det();
    if(det == 0.0f)
    {
        return IDENTITY;    
    }
    return SE_Matrix2f(m11 / det, -m01 / det, -m10 / det, m00 / det);
}
SE_Vector2f SE_Matrix2f::getRow(int i) const
{
    return SE_Vector2f(d[i * 2], d[i * 2 + 1]);
}
SE_Vector2f SE_Matrix2f::getColumn(int i) const
{
    return SE_Vector2f(d[i], d[i + 2]);
}
void SE_Matrix2f::setZero()
{
    memset(d, 0, sizeof(float) * 4);
}
float SE_Matrix2f::det() const
{
    return m00 * m11 - m01 * m10;
}

////////////////////////////
const SE_Matrix3f SE_Matrix3f::IDENTITY = SE_Matrix3f(1.0f, 0.0f, 0.0f,
                                                  0.0f, 1.0f, 0.0f,
                              0.0f, 0.0f, 1.0f);
SE_Matrix3f::SE_Matrix3f()
{
    memset(d, 0, sizeof(float) * 9);
}
SE_Matrix3f::SE_Matrix3f(float d[9])
{
    for(int i = 0 ; i < 9 ; i++)
    {
        this->d[i] = d[i];
    }
}
SE_Matrix3f::SE_Matrix3f(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
{
    this->m00 = m00;
    this->m01 = m01;
    this->m02 = m02;
    this->m10 = m10;
    this->m11 = m11;
    this->m12 = m12;
    this->m20 = m20;
    this->m21 = m21;
    this->m22 = m22;
}
SE_Matrix3f::SE_Matrix3f(const SE_Vector3f& r0, const SE_Vector3f& r1, const SE_Vector3f& r2)
{
    m00 = r0.x;
    m01 = r0.y;
    m02 = r0.z;

    m10 = r1.x;
    m11 = r1.y;
    m12 = r1.z;

    m20 = r2.x;
    m21 = r2.y;
    m22 = r2.z;
}
SE_Matrix3f::SE_Matrix3f(const SE_Matrix3f& rm)
{
    for(int i = 0 ; i < 9 ; i++)
    {
        d[i] = rm.d[i];
    }
}
SE_Matrix3f& SE_Matrix3f::operator=(const SE_Matrix3f& rm)
{
    if(this == &rm)
        return *this;
    for(int i = 0 ; i < 9 ; i++)
    {
        d[i] = rm.d[i];
    }
    return *this;
}
float SE_Matrix3f::operator()(int row, int column)
{
    return d[row * 3 + column];
}
bool SE_Matrix3f::operator ==(const SE_Matrix3f& right) const
{
    for(int i = 0 ; i < 9 ; i++)
    {
        if(this->d[i] != right.d[i])
            return false;
    }
    return true;
}
SE_Matrix3f SE_Matrix3f::mulScalar(float f) const
{
    SE_Matrix3f ret;
    for(int i = 0 ; i < 9 ; i++)
    {
        ret.d[i] = d[i] * f;
    }
    return ret;
}
SE_Matrix3f SE_Matrix3f::mul(const SE_Matrix3f& rm) const
{
    SE_Matrix3f ret;
    for(int i = 0 ; i < 3 ; i++)
    {
        SE_Vector3f r = getRow(i);
        for(int j = 0 ; j < 3 ; j++)
        {
            SE_Vector3f c = rm.getColumn(j);
            ret.d[i * 3 + j] = r.dot(c);
        }
    }
    return ret;
}
float SE_Matrix3f::get(int row, int column) const
{
    return d[row * 3 + column];
}

SE_Vector3f SE_Matrix3f::getScale()
{
    SE_Vector3f ret;
    ret.x = get(0,0);
    ret.y = get(1,1);
    ret.z = get(2,2);
    return ret;
}
SE_Vector3f SE_Matrix3f::getRow(int i) const
{
    return SE_Vector3f(d[i * 3], d[i * 3 + 1], d[i * 3 + 2]);
}
SE_Vector3f SE_Matrix3f::getColumn(int i) const
{
    return SE_Vector3f(d[i], d[i + 3], d[i + 6]);
}
void SE_Matrix3f::setRow(int i, const SE_Vector3f& r)
{
    d[i * 3] = r.x;
    d[i * 3 + 1] = r.y;
    d[i * 3 + 2] = r.z;
}
void SE_Matrix3f::setColumn(int i, const SE_Vector3f& c)
{
    d[i] = c.x;
    d[i + 3] = c.y;
    d[i + 6] = c.z;
}
SE_Vector3f SE_Matrix3f::map(const SE_Vector3f& v) const
{
    SE_Vector3f ret;
    for(int i = 0 ; i < 3 ; i++)
    {
        SE_Vector3f r = getRow(i);
        ret.d[i] = r.dot(v);
    }
    return ret;
}
void SE_Matrix3f::identity()
{
    m00 = 1.0f;
    m01 = 0.0f;
    m02 = 0.0f;
    m10 = 0.0f;
    m11 = 1.0f;
    m12 = 0.0f;
    m20 = 0.0f;
    m21 = 0.0f;
    m22 = 1.0f;
}
void SE_Matrix3f::setZero()
{
    memset(d, 0, sizeof(float ) * 9);
}
bool SE_Matrix3f::isZero() const
{
    for(int i = 0 ; i < 9 ; i++)
    {
        if(d[i] != 0.0f)
            return false;
    }
    return true;
}
SE_Matrix3f SE_Matrix3f::subtract(const SE_Matrix3f& rm) const
{
    return SE_Matrix3f(m00 - rm.m00, m01 - rm.m01, m02 - rm.m02,
                   m10 - rm.m10, m11 - rm.m11, m12 - rm.m12,
               m20 - rm.m20, m21 - rm.m21, m22 - rm.m22);
}
SE_Matrix3f SE_Matrix3f::add(const SE_Matrix3f& rm) const
{
    return SE_Matrix3f(m00 + rm.m00, m01 + rm.m01, m02 + rm.m02,
                   m10 + rm.m10, m11 + rm.m11, m12 + rm.m12,
               m20 + rm.m20, m21 + rm.m21, m22 + rm.m22);
}
SE_Matrix3f SE_Matrix3f::transpose() const
{
    return SE_Matrix3f(m00, m10, m20,
                   m01, m11, m21,
               m02, m12, m22);
}
SE_Matrix3f SE_Matrix3f::inverse() const
{
    float det = this->det();
    SE_Matrix3f adjM;
    SE_Matrix3f adjMT;
    if(det == 0.0f)
    {
        return SE_Matrix3f();
    }
    adjM.m00 = m11 * m22 - m12 * m21;
    adjM.m01 = -(m10 * m22 - m12 * m20);
    adjM.m02 = m10 * m21 - m11 * m20;

    adjM.m10 = -(m01 * m22 - m02 * m21);
    adjM.m11 = (m00 * m22 - m02 * m20);
    adjM.m12 = -(m00 * m21 - m01 * m20);

    adjM.m20 = m01 * m12 - m02 * m11;
    adjM.m21 = -(m00 * m12 - m02 * m10);
    adjM.m22 = m00 * m11 - m01 * m10;
    adjMT = adjM.transpose();
    SE_Matrix3f ret;
    for(int i = 0 ; i < 9 ; i++)
    {
        ret.d[i] = adjMT.d[i] / det;
    }
    return ret;
}
float SE_Matrix3f::det() const
{
    return m00 * m11 * m22 + m01 * m12 * m20 + m02 * m10 * m21 - m00 * m12 * m21 - m01 * m10 * m22 - m02 * m11 * m20;
}
void SE_Matrix3f::getColumnSequence(float out[9]) const
{
    out[0] = m00;
    out[1] = m10;
    out[2] = m20;

    out[3] = m01;
    out[4] = m11;
    out[5] = m21;

    out[6] = m02;
    out[7] = m12;
    out[8] = m22; 
}
void SE_Matrix3f::getSequence(float out[9]) const
{
    for(int i = 0 ; i < 9 ; i++)
    {
        out[i] = d[i];
    }
}
void SE_Matrix3f::setColumn(const SE_Vector3f& c0, const SE_Vector3f& c1, const SE_Vector3f& c2)
{
    m00 = c0.x;
    m10 = c0.y;
    m20 = c0.z;
    m01 = c1.x;
    m11 = c1.y;
    m21 = c1.z;
    m02 = c2.x;
    m12 = c2.y;
    m22 = c2.z;
}
void SE_Matrix3f::setRow(const SE_Vector3f& r0, const SE_Vector3f& r1, const SE_Vector3f& r2)
{
    m00 = r0.x;
    m01 = r0.y;
    m02 = r0.z;
    m10 = r1.x;
    m11 = r1.y;
    m12 = r1.z;
    m20 = r2.x;
    m21 = r2.y;
    m22 = r2.z;
}
void SE_Matrix3f::setRotateX(float angle)
{
    float radian = SE_AngleToRadian(angle);
    SE_Vector3f c1(1.0f, 0.0f, 0.0f);
    SE_Vector3f c2(0.0f, SE_Cosf(radian), SE_Sinf(radian));
    SE_Vector3f c3(0.0f, -SE_Sinf(radian), SE_Cosf(radian));
    setColumn(c1, c2, c3);
}
void SE_Matrix3f::setRotateY(float angle)
{
    float radian = SE_AngleToRadian(angle);
    SE_Vector3f c1(SE_Cosf(radian), 0.0f, -SE_Sinf(radian));
    SE_Vector3f c2(0.0f, 1.0f , 0.0f);
    SE_Vector3f c3(SE_Sinf(radian), 0.0f, SE_Cosf(radian));
    setColumn(c1, c2, c3);
}
void SE_Matrix3f::setRotateZ(float angle)
{
    float radian = SE_AngleToRadian(angle);
    SE_Vector3f c1(SE_Cosf(radian), SE_Sinf(radian), 0.0f);
    SE_Vector3f c2(-SE_Sinf(radian), SE_Cosf(radian), 0.0f);
    SE_Vector3f c3(0.0f, 0.0f, 1.0f);
    setColumn(c1, c2, c3);
}
void SE_Matrix3f::setScale(float x, float y, float z)
{
    m00 = x;
    m10 = 0.0f;
    m20 = 0.0f;

    m01 = 0.0f;
    m11 = y;
    m21 = 0.0f;

    m02 = 0.0f;
    m12 = 0.0f;
    m22 = z;
}
void SE_Matrix3f::setRotateFromAxis(float angle, const SE_Vector3f& axis)
{
    if(axis.isZero())
    {
        identity();
    return;
    }
    float radian = SE_AngleToRadian(angle);
    SE_Vector3f n = axis.normalize();
    float param1 = 1 - SE_Cosf(radian);
    m00 = n.x * n.x * param1 + SE_Cosf(radian);
    m01 = n.x * n.y * param1 - n.z * SE_Sinf(radian);
    m02 = n.x * n.z * param1 + n.y * SE_Sinf(radian);

    m10 = n.x * n.y * param1 + n.z * SE_Sinf(radian);
    m11 = n.y * n.y * param1 + SE_Cosf(radian);
    m12 = n.y * n.z * param1 - n.x * SE_Sinf(radian);

    m20 = n.x * n.z * param1 - SE_Sinf(radian) * n.y;
    m21 = n.y * n.z * param1 + SE_Sinf(radian) * n.x;
    m22 = n.z * n.z * param1 + SE_Cosf(radian);
}


SE_Quat SE_Matrix3f::toQuat()
{
    SE_Quat result;

    float fourWMin = m00 + m11 + m22;
    float fourXMin = m00 - m11 - m22;
    float fourYMin = m11 - m00 - m22;
    float fourZMin = m22 - m00 - m11;

    int biggestIndex = 0;

    float fourBiggestMin = fourWMin;

    if(fourXMin > fourBiggestMin)
    {
        fourBiggestMin = fourXMin;
        biggestIndex = 1;
    }

    if(fourYMin > fourBiggestMin)
    {
        fourBiggestMin = fourYMin;
        biggestIndex = 2;
    }

    if(fourZMin > fourBiggestMin)
    {
        fourBiggestMin = fourZMin;
        biggestIndex = 3;
    }

    float biggestVal = SE_Sqrtf(fourBiggestMin + 1.0f) * 0.5f;
    float mult = 0.25 / biggestVal;

    switch(biggestIndex)
    {
    case 0:
        result.w = biggestVal;
        result.x = -(m12 - m21) * mult;
        result.y = -(m20 - m02) * mult;
        result.z = -(m01 - m10) * mult;
        break;

    case 1:
        result.x = -biggestVal;
        result.w = (m12 - m21) * mult;
        result.y = -(m10 +  m01) * mult;
        result.z = -(m02 + m20) * mult;
        break;

    case 2:
        result.y = -biggestVal;
        result.w = (m20 - m02) * mult;
        result.x = -(m10 +  m01) * mult;
        result.z = -(m21 + m12) * mult;
        break;

    case 3:
        result.z = -biggestVal;
        result.w = (m01 - m10) * mult;
        result.x = -(m02 +  m20) * mult;
        result.y = -(m21 + m12) * mult;
        break;

    }

    return result;
}



///////////////////////////////////////////////////////
const SE_Matrix4f SE_Matrix4f::IDENTITY = SE_Matrix4f(SE_Vector4f(1, 0, 0, 0),
                                                  SE_Vector4f(0, 1, 0, 0),
                              SE_Vector4f(0, 0, 1, 0),
                              SE_Vector4f(0, 0, 0, 1));
SE_Matrix4f::SE_Matrix4f()
{
    memset(d, 0, sizeof(float) * 16);
    identity();
}
SE_Matrix4f::SE_Matrix4f(float d[16], int sequence)
{
    if(sequence == ROW_SEQUENCE)
    {
        for(int i = 0 ; i < 16 ; i++)
        {
            this->d[i] = d[i];
        }
    }
    else
    {
        for(int i = 0 ; i < 16 ; i += 4)
        {
            int column = i / 4;
            SE_Vector4f v(d[i], d[i + 1], d[i + 2], d[i + 3]);
            setColumn(column, v);
        }
    }
}
SE_Matrix4f::SE_Matrix4f(const SE_Vector4f& r0, const SE_Vector4f& r1, const SE_Vector4f& r2, const SE_Vector4f& r3)
{
    m00 = r0.x;
    m01 = r0.y;
    m02 = r0.z;
    m03 = r0.w;

    m10 = r1.x;
    m11 = r1.y;
    m12 = r1.z;
    m13 = r1.w;

    m20 = r2.x;
    m21 = r2.y;
    m22 = r2.z;
    m23 = r2.w;

    m30 = r3.x;
    m31 = r3.y;
    m32 = r3.z;
    m33 = r3.w;
}
SE_Matrix4f::SE_Matrix4f(const SE_Matrix4f& rm)
{
    for(int i = 0 ; i < 16 ; i++)
    {
        d[i] = rm.d[i];
    }
}
SE_Matrix4f::SE_Matrix4f(const SE_Matrix3f& m, const SE_Vector3f& t)
{
    set(m, t);
}
SE_Matrix4f& SE_Matrix4f::operator=(const SE_Matrix4f& rm)
{
    if(this == &rm)
        return *this;
    for(int i = 0 ; i < 16 ; i++)
    {
        d[i] = rm.d[i];
    }
    return *this;
}
float SE_Matrix4f::operator()(int row, int column)
{
    return d[row * 4 + column];
}
bool SE_Matrix4f::operator ==(const SE_Matrix4f& right) const
{
    for(int i = 0 ; i < 16 ; i++)
    {
        if(this->d[i] != right.d[i])
            return false;
    }
    return true;
}
float SE_Matrix4f::det() const
{
    SE_Matrix3f m1(m11, m12, m13, m21, m22, m23, m31, m32, m33);
    SE_Matrix3f m2(m10, m12, m13, m20, m22, m23, m30 ,m32, m33);
    SE_Matrix3f m3(m10, m11, m13, m20, m21, m23, m30, m31, m33);
    SE_Matrix3f m4(m10, m11, m12, m20, m21, m22, m30, m31, m32);
    float det = m00 * m1.det() - m01 * m2.det() + m02 * m3.det() - m03 * m4.det();
    return det;
}
SE_Matrix3f SE_Matrix4f::toMatrix3f() const
{
    return SE_Matrix3f(m00, m01, m02, m10, m11, m12, m20, m21, m22);
}
SE_Vector4f SE_Matrix4f::map(const SE_Vector4f& v) const
{
    SE_Vector4f ret;
    for(int i = 0 ; i < 4 ; i++)
    {
        SE_Vector4f r = getRow(i);
        ret.d[i] = r.dot(v);
    }
    return ret;
}
SE_Vector3f SE_Matrix4f::getTranslate() const
{
    return SE_Vector3f(m03, m13, m23);
}
SE_Matrix4f SE_Matrix4f::inverse() const
{
    float det = this->det();
    if(det == 0.0f)
    {
        return IDENTITY;
    }
    SE_Vector4f lastRow = getRow(3);
    if(lastRow.x == 0.0f && lastRow.y == 0.0f && lastRow.z == 0.0f && lastRow.w == 1.0f)
    {
        SE_Matrix3f rs = toMatrix3f();
        SE_Vector3f t = getTranslate();
        SE_Matrix3f inverseRs = rs.inverse();
        t = inverseRs.map(t);
        t = t.neg();
        SE_Matrix4f ret;
        ret.set(inverseRs, t);
        return ret;
    }
    else
    {
        SE_Matrix4f adjM;
        for(int i = 0 ; i < 4 ; i++)
        {
            for(int j = 0 ; j < 4 ; j++)
            {
                SE_Matrix3f coffM = cofactorM(i, j);
                float coffDet = coffM.det();
                if(((i + j) % 2) == 0)
                {
                    adjM.d[i * 4 + j] = coffDet / det;
                }
                else
                {
                    adjM.d[i * 4 + j] = -coffDet / det;
                }
            }
        }
        SE_Matrix4f ret = adjM.transpose();
        return ret;
    }
}
SE_Matrix3f SE_Matrix4f::cofactorM(int row, int column) const
{
    SE_Matrix3f ret;
    int outIndex = 0;
    for(int i = 0 ; i < 4 ; i++)
    {
        if(i != row)
        {
            for(int j = 0 ; j < 4 ; j++)
            {
                if(j != column)
                {
                    ret.d[outIndex++] = d[i * 4 + j];
                }
            }
        }
    }
    return ret;
}
SE_Matrix4f SE_Matrix4f::transpose() const
{
    SE_Matrix4f ret;
    for(int i = 0 ; i < 4 ; i++)
    {
        SE_Vector4f v = getRow(i);
        ret.setColumn(i, v);
    }
    return ret;
}
void SE_Matrix4f::set(const SE_Matrix3f& m, const SE_Vector3f& translate)
{
    SE_Vector4f r0, r1, r2, r3;
    SE_Vector3f sr0, sr1, sr2;
    sr0 = m.getRow(0);
    sr1 = m.getRow(1);
    sr2 = m.getRow(2);
    r0.set(sr0, translate.x);
    r1.set(sr1, translate.y);
    r2.set(sr2, translate.z);
    r3.set(0, 0, 0, 1);
    setRow(r0, r1, r2, r3); 
}
SE_Matrix4f SE_Matrix4f::add(const SE_Matrix4f& rm) const
{
    SE_Matrix4f ret;
    for(int i = 0 ; i < 16 ; i++)
    {
        ret.d[i] = d[i] + rm.d[i];
    }
    return ret;
}
SE_Matrix4f SE_Matrix4f::subtract(const SE_Matrix4f& rm) const
{
    SE_Matrix4f ret;
    for(int i = 0 ; i < 16 ; i++)
    {
        ret.d[i] = d[i] - rm.d[i];
    }
    return ret;
}
SE_Matrix4f SE_Matrix4f::mulScalar(float f) const
{
    SE_Matrix4f ret;
    for(int i = 0 ; i < 16 ; i++)
    {
        ret.d[i] = d[i] * f;
    }
    return ret;
}
SE_Matrix4f SE_Matrix4f::mul(const SE_Matrix4f& rm) const
{
    SE_Matrix4f ret;
    for(int i = 0 ; i < 4 ; i++)
    {
        SE_Vector4f r = getRow(i);
        for(int j = 0 ; j < 4 ; j++)
        {
            SE_Vector4f c = rm.getColumn(j);
            ret.d[i * 4 + j] = r.dot(c);
        }
    }
    return ret;
}
SE_Vector4f SE_Matrix4f::getRow(int row) const
{
    return SE_Vector4f(d[row * 4], d[row * 4 + 1], d[row * 4 + 2] , d[row * 4 + 3]);
}
SE_Vector4f SE_Matrix4f::getColumn(int column) const
{
    return SE_Vector4f(d[column], d[column + 4], d[column + 8], d[column + 12]);
}
void SE_Matrix4f::setRow(int row, const SE_Vector4f& v)
{
    d[row * 4] = v.x;
    d[row * 4 + 1] = v.y;
    d[row * 4 + 2] = v.z;
    d[row * 4 + 3] = v.w;
}
void SE_Matrix4f::setColumn(int column, const SE_Vector4f& v)
{
    d[column] = v.x;
    d[column + 4] = v.y;
    d[column + 8] = v.z;
    d[column + 12] = v.w;
}
void SE_Matrix4f::setRow(const SE_Vector4f& r0, const SE_Vector4f& r1, const SE_Vector4f& r2, const SE_Vector4f& r3)
{
    SE_Vector4f vr[] = {r0, r1, r2, r3};
    for(int i = 0 ; i < 4 ; i++)
    {
        setRow(i, vr[i]);
    }
}
void SE_Matrix4f::setColumn(const SE_Vector4f& c0, const SE_Vector4f& c1, const SE_Vector4f& c2, const SE_Vector4f& c3)
{
    SE_Vector4f cr[] = {c0, c1, c2, c3};
    for(int i = 0 ; i < 4 ; i++)
    {
        setColumn(i, cr[i]);
    }
}
void  SE_Matrix4f::set(const SE_Matrix3f& rotate, const SE_Vector3f& scale, const SE_Vector3f& translate)
{
    SE_Matrix3f sm;
    sm.set(0 , 0, scale.x);
    sm.set(1, 1, scale.y);
    sm.set(2, 2, scale.z);
    SE_Matrix3f rs = rotate.mul(sm);
    set(rs, translate);
}
void SE_Matrix4f::getSequence(float* out, int size) const
{
    for(int i = 0 ; i < 16 && i < size ; i++)
    {
        out[i] = d[i];
    }
}
void SE_Matrix4f::getColumnSequence(float out[16]) const
{
    out[0] = m00;
    out[1] = m10;
    out[2] = m20;
    out[3] = m30;

    out[4] = m01;
    out[5] = m11;
    out[6] = m21;
    out[7] = m31;

    out[8] = m02;
    out[9] = m12;
    out[10] = m22;
    out[11] = m32;

    out[12] = m03;
    out[13] = m13;
    out[14] = m23;
    out[15] = m33;
}
