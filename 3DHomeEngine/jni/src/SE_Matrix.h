#ifndef SE_MATRIX_H
#define SE_MATRIX_H
#include "SE_Vector.h"
class SE_Quat;
class SE_ENTRY SE_Matrix2f
{
public:
    static const SE_Matrix2f IDENTITY;
    SE_Matrix2f();
    SE_Matrix2f(float d[4]);
    SE_Matrix2f(float m00, float m01, float m10, float m11);
    SE_Matrix2f(const SE_Vector2f& r0, const SE_Vector2f& r1);
    SE_Matrix2f(const SE_Matrix2f& rm);
    SE_Matrix2f& operator=(const SE_Matrix2f& rm);
    float operator()(int row, int column);
    void identity();
    float get(int row, int column) const;
    SE_Vector2f map(const SE_Vector2f& v) const;
    SE_Matrix2f mulScalar(float f) const;
    SE_Matrix2f mul(const SE_Matrix2f& rm) const;
    SE_Matrix2f inverse() const;
    SE_Vector2f getRow(int i) const;
    SE_Vector2f getColumn(int i) const;
    void set(int row, int column, float f)
    {
        d[row * 2 + column] = f;
    }
    bool isZero();
    void setZero();
    float det() const;
    bool hasInverse() const
    {
        return det() != 0;
    }
    bool operator ==(const SE_Matrix2f& right) const;
    bool operator !=(const SE_Matrix2f& right) const
    {
        return !this->operator==(right);
    }
private:
    union
    {
        float d[4];
        struct
        {
            float m00, m01 ,m10, m11;
        };
    };
};
class SE_ENTRY SE_Matrix3f
{
public:
    static const SE_Matrix3f IDENTITY;
    SE_Matrix3f();
    SE_Matrix3f(float d[9]);
    SE_Matrix3f(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22);
    SE_Matrix3f(const SE_Vector3f& r0, const SE_Vector3f& r1, const SE_Vector3f& r2);
    SE_Matrix3f(const SE_Matrix3f& rm);
    SE_Matrix3f& operator=(const SE_Matrix3f& rm);
    SE_Matrix3f mulScalar(float f) const;
    SE_Matrix3f mul(const SE_Matrix3f& rm) const;
    SE_Matrix3f add(const SE_Matrix3f& rm) const;
    float get(int row, int column) const;
    SE_Vector3f getScale();
    void set(int row, int column, float f)
    {
        d[row * 3 + column] = f;
    }
    float operator()(int row, int column);
    SE_Vector3f getRow(int i) const;
    SE_Vector3f getColumn(int i) const;
    void setColumn(const SE_Vector3f& c0, const SE_Vector3f& c1, const SE_Vector3f& c2);
    void setRow(const SE_Vector3f& r0, const SE_Vector3f& r1, const SE_Vector3f& r2);
    void setRow(int i, const SE_Vector3f& r);
    void setColumn(int i, const SE_Vector3f& c);
    SE_Vector3f map(const SE_Vector3f& v) const;
    void identity();
    void setZero();
    bool isZero() const;
    SE_Matrix3f subtract(const SE_Matrix3f& rm) const;
    SE_Matrix3f transpose() const;
    SE_Matrix3f inverse() const;
    float det() const;
    void getColumnSequence(float out[9]) const;
    void getSequence(float out[9]) const;
    void setRotateX(float angle);
    void setRotateY(float angle);
    void setRotateZ(float angle);
    void setScale(float x, float y, float z);
    void setRotateFromAxis(float angle, const SE_Vector3f& axis);
    bool hasInverse() const
    {
        return det() != 0;
    }

    SE_Quat toQuat();

    bool operator ==(const SE_Matrix3f& right) const;
    bool operator !=(const SE_Matrix3f& right) const
    {
        return !this->operator==(right);
    }
private:
    union
    {
        float d[9];
        struct
        {
            float m00, m01, m02;
            float m10, m11, m12;
            float m20, m21, m22;
        };
    };
    friend class SE_Matrix4f;
};
class SE_ENTRY SE_Matrix4f
{
public:
    enum  {COL_SEQUENCE, ROW_SEQUENCE};
    static const SE_Matrix4f IDENTITY;
    SE_Matrix4f();
    SE_Matrix4f(float d[16], int sequence = ROW_SEQUENCE);
    SE_Matrix4f(const SE_Vector4f& r0, const SE_Vector4f& r1, const SE_Vector4f& r2, const SE_Vector4f& r3);
    SE_Matrix4f(const SE_Matrix4f& rm);
    SE_Matrix4f(const SE_Matrix3f& m, const SE_Vector3f& t);
    SE_Matrix4f& operator=(const SE_Matrix4f& rm);
    float det() const;
    void identity()
    {
        memset(d, 0, sizeof(float) * 16);
        set(0 , 0 , 1.0f);
        set(1 , 1 , 1.0f);
        set(2 , 2 , 1.0f);
        set(3 , 3 , 1.0f);
    }
    float operator()(int row, int column);
    SE_Matrix3f toMatrix3f() const;
    SE_Vector4f map(const SE_Vector4f& v) const;
    SE_Matrix4f inverse() const;
    SE_Matrix3f cofactorM(int row, int column) const;
    SE_Matrix4f transpose() const;
    void set(const SE_Matrix3f& m, const SE_Vector3f& translate);
    SE_Matrix4f add(const SE_Matrix4f& rm) const;
    SE_Matrix4f subtract(const SE_Matrix4f& rm) const;
    SE_Matrix4f mulScalar(float f) const;
    SE_Matrix4f mul(const SE_Matrix4f& rm) const;
    SE_Vector4f getRow(int row) const;
    SE_Vector4f getColumn(int column) const;
    void setRow(int row, const SE_Vector4f& v);
    void setColumn(int column, const SE_Vector4f& v);
    void setRow(const SE_Vector4f& r0, const SE_Vector4f& r1, const SE_Vector4f& r2, const SE_Vector4f& r3);
    void setColumn(const SE_Vector4f& c0, const SE_Vector4f& c1, const SE_Vector4f& c2, const SE_Vector4f& c3);
    void set(int row, int column, float f)
    {
        d[row * 4 + column] = f;
    }
    void set(const SE_Matrix3f& rotate, const SE_Vector3f& scale, const SE_Vector3f& translate);
    SE_Vector3f getTranslate() const;
    bool hasInverse() const
    {
        return det() != 0;
    }
    float get(int row, int column) const
    {
        return d[row * 4 + column];
    }
    void getColumnSequence(float out[16]) const;
    void getSequence(float* out, int size) const;
    bool operator ==(const SE_Matrix4f& right) const;
    bool operator !=(const SE_Matrix4f& right) const
    {
        return !this->operator==(right);
    }
private:
    union
    {
        float d[16];
        struct
        {
            float m00, m01, m02, m03;
            float m10, m11, m12, m13;
            float m20, m21, m22, m23;
            float m30, m31, m32, m33;
        };
    };

};
#endif
