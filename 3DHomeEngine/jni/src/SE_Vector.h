#ifndef SE_VECTOR_H
#define SE_VECTOR_H
#include "SE_Math.h"
#include "SE_DynamicLibType.h"
class SE_ENTRY SE_Vector2f
{
public:
    SE_Vector2f()
    {
        x = y = 0.0f;
    }
    SE_Vector2f(float x, float y)
    {
        this->x = x;
        this->y = y;
    }
    SE_Vector2f(float data[2])
    {
        x = data[0];
        y = data[1];
    }
    SE_Vector2f(const SE_Vector2f& rv)
    {
        if(this == &rv)
            return;
        this->x = rv.x;
        this->y = rv.y;
    }
    SE_Vector2f& operator=(const SE_Vector2f& rv)
    {
        if(this == &rv)
            return *this;
        x = rv.x;
        y = rv.y;
        return *this;
    }
    inline SE_Vector2f mul(float f) const
    {
        return SE_Vector2f(x * f, y * f);
    }
    inline float dot(const SE_Vector2f& rv) const
    {
        return x * rv.x + y * rv.y;
    }
    inline SE_Vector2f subtract(const SE_Vector2f& rv) const
    {
        return SE_Vector2f(x - rv.x, y - rv.y);
    }
    inline SE_Vector2f add(const SE_Vector2f& rv) const
    {
        return SE_Vector2f(x + rv.x , y + rv.y);
    }
    inline float length() const
    {
        return SE_Sqrtf(dot(*this));
    }
    inline SE_Vector2f normalize() const
    {
        float len = length();
        return SE_Vector2f(x / len, y / len);
    }
    inline bool isZero() const
    {
        return x == 0.0f && y == 0.0f;
    }
    inline void setZero()
    {
        x = 0;
        y = 0;
    }
    inline SE_Vector2f neg() const
    {
        return SE_Vector2f(-x, -y);
    }
    inline void set(float x, float y)
    {
        this->x = x;
        this->y = y;
    }
    inline float operator[](int i)
    {
        return d[i];
    }
    inline float lengthSquare() const
    {
        return x * x + y * y;
    }
    friend SE_Vector2f operator+(const SE_Vector2f& lv, const SE_Vector2f& rv)
    {
        return lv.add(rv);
    }
    friend SE_Vector2f operator-(const SE_Vector2f& lv, const SE_Vector2f& rv)
    {
        return lv.subtract(rv);
    }
    friend SE_Vector2f operator*(const SE_Vector2f& v, float d)
    {
        return v.mul(d);
    }
    friend bool operator==(const SE_Vector2f& v1, const SE_Vector2f& v2)
    {
        for(int i = 0 ; i < 2 ; i++)
        {
            if(SE_Fabs(v1.d[i] - v2.d[i]) > SE_VEC_EQUAL_EPSILON)
                return false;
        }
        return true;

    }
    union
    {
        float d[2];
        struct
        {
            float x, y;
        };
    };
};
class SE_ENTRY SE_Vector3f
{
public:
    SE_Vector3f()
    {
        x = y = z = 0.0f;
    }
    SE_Vector3f(float x, float y, float z)
    {
        this->x = x;
        this->y = y;
        this->z = z;
    }
    SE_Vector3f(const SE_Vector2f& v, float z)
    {
        x = v.x;
        y = v.y;
        this->z = z;
    }
    SE_Vector3f(float d[3])
    {
        x = d[0];
        y = d[1];
        z = d[2];
    }
    SE_Vector3f(const SE_Vector3f& rv)
    {
        x = rv.x;
        y = rv.y;
        z = rv.z;
    }
    SE_Vector3f& operator=(const SE_Vector3f& rv)
    {
        if(this == &rv)
            return *this;
        x = rv.x;
        y = rv.y;
        z = rv.z;
        return *this;
    }
    friend bool operator==(const SE_Vector3f& v1, const SE_Vector3f& v2)
    {
        for(int i = 0 ; i < 3 ; i++)
        {
            if(SE_Fabs(v1.d[i] - v2.d[i]) > SE_VEC_EQUAL_EPSILON)
                return false;
        }
        return true;

    }

    inline float operator[](int i)
    {
        return d[i];
    }

    inline float dot(const SE_Vector3f& rv) const
    {
        return x * rv.x + y * rv.y + z * rv.z;
    }
    inline SE_Vector3f cross(const SE_Vector3f& rv) const
    {
        return SE_Vector3f(y * rv.z - z * rv.y, z * rv.x - x * rv.z, x * rv.y - y * rv.x);
    }
    inline SE_Vector3f mul(float f) const
    {
        return SE_Vector3f(x * f, y * f, z * f);
    }
    inline SE_Vector3f mul(const SE_Vector3f& v) const
    {
        return SE_Vector3f(x * v.x, y * v.y, z * v.z);
    }
    inline SE_Vector3f subtract(const SE_Vector3f& rv) const
    {
        return SE_Vector3f(x - rv.x, y - rv.y, z - rv.z);
    }
    inline SE_Vector3f add(const SE_Vector3f& rv) const
    {
        return SE_Vector3f(x + rv.x, y + rv.y , z + rv.z);
    }
    inline SE_Vector3f neg() const
    {
        return SE_Vector3f(-x, -y, -z);
    }
    inline SE_Vector3f normalize() const
    {
        float len = length();
        return SE_Vector3f(x / len, y / len, z / len);
    }
    inline float length() const
    {
        return SE_Sqrtf(x * x + y * y + z * z);
    }
    inline float lengthSquare() const
    {
        return x * x + y * y + z * z;
    }
    inline bool isZero() const
    {
        return x == 0.0f && y == 0.0f && z == 0.0f;
    }
    inline void setZero()
    {
        x = y = z = 0;
    }
    inline void set(float x, float y, float z)
    {
        this->x = x;
        this->y = y;
        this->z = z;
    }
    inline float distance(const SE_Vector3f& rv) const
    {
        SE_Vector3f dir = subtract(rv);
        return dir.length();
    }
    void decompose(const SE_Vector3f& axis, SE_Vector3f& vPerpendicular, SE_Vector3f& vParallel) const
    {
        float dotProduct, lenSquare;
        dotProduct = dot(axis);
        lenSquare = axis.lengthSquare();
        vPerpendicular = axis.mul(dotProduct / lenSquare);
        vParallel = axis - vPerpendicular;
    }
    inline SE_Vector2f xy() const
    {
        return SE_Vector2f(x, y);
    }
    inline SE_Vector2f xz() const
    {
        return SE_Vector2f(x, z);
    }
    inline SE_Vector2f yz() const
    {
        return SE_Vector2f(y, z);
    }
    inline SE_Vector2f yx() const
    {
        return SE_Vector2f(y, x);
    }
    inline SE_Vector2f zx() const
    {
        return SE_Vector2f(z, x);
    }
    friend SE_Vector3f operator+(const SE_Vector3f& lv, const SE_Vector3f& rv)
    {
        return lv.add(rv);
    }
    friend SE_Vector3f operator-(const SE_Vector3f& lv, const SE_Vector3f& rv)
    {
        return lv.subtract(rv);
    }
    friend SE_Vector3f operator*(const SE_Vector3f& v, float d)
    {
        return v.mul(d);
    }
    friend float scalerTripleProduct(const SE_Vector3f& u, const SE_Vector3f& v, const SE_Vector3f& w)
    {
        SE_Vector3f tmp = v.cross(w);
        return u.dot(tmp);
    }
    union
    {
        float d[3];
        struct
        {
            float x, y, z;
        };
    };

};
class SE_ENTRY SE_Vector4f
{
public:
    SE_Vector4f()
    {
        x = y = z = w = 0;
    }
    SE_Vector4f(float x, float y, float z, float w)
    {
        this->x = x;
        this->y = y;
        this->z = z;
        this->w = w;
    }
    SE_Vector4f(float d[4])
    {
        x = d[0];
        y = d[1];
        z = d[2];
        w = d[3];
    }
    SE_Vector4f(const SE_Vector4f& rv)
    {
        x = rv.x;
        y = rv.y;
        z = rv.z;
        w = rv.w;
    }
    SE_Vector4f(const SE_Vector3f& v, float w)
    {
        x = v.x;
        y = v.y;
        z = v.z;
        this->w = w;
    }
    SE_Vector4f& operator=(const SE_Vector4f& rv)
    {
        if(this == &rv)
            return *this;
        x = rv.x;
        y = rv.y;
        z = rv.z;
        w = rv.w;
        return *this;
    }
    friend bool operator==(const SE_Vector4f& v1, const SE_Vector4f& v2)
    {
        for(int i = 0 ; i < 4 ; i++)
        {
            if(SE_Fabs(v1.d[i] - v2.d[i]) > SE_VEC_EQUAL_EPSILON)
                return false;
        }
        return true;

    }

    SE_Vector3f xyz() const
    {
        return SE_Vector3f(x, y, z);
    }
    SE_Vector3f yzw() const
    {
        return SE_Vector3f(y, z, w);
    }
    SE_Vector3f zwx() const
    {
        return SE_Vector3f(z, w, x);
    }
    SE_Vector3f get(int indexX, int indexY, int indexZ) const
    {
        return SE_Vector3f(d[indexX], d[indexY], d[indexZ]);
    }
    bool isZero() const
    {
        return x == 0 && y == 0 && z == 0 && w == 0;
    }
    void setZero()
    {
        x = y = z = w = 0;
    }
    void set(float x, float y, float z, float w)
    {
        this->x = x;
        this->y = y;
        this->z = z;
        this->w = w;
    }
    SE_Vector4f add(const SE_Vector4f& rv) const
    {
        return SE_Vector4f(x + rv.x, y + rv.y , z + rv.z, w + rv.w);
    }
    SE_Vector4f mul(float f) const
    {
        return SE_Vector4f(x * f, y * f, z * f, w * f);
    }
    SE_Vector4f subtract(const SE_Vector4f& rv) const
    {
        return SE_Vector4f(x - rv.x, y - rv.y, z - rv.z, w - rv.w);
    }
    float length() const
    {
        return SE_Sqrtf(x * x + y * y + z * z + w * w);
    }
    inline float lengthSquare() const
    {
        return x * x + y * y + z * z + w * w;
    }
    float dot(const SE_Vector4f& rv) const
    {
        return x * rv.x + y * rv.y + z * rv.z + w * rv.w;
    }
    void set(const SE_Vector3f& v, float w)
    {
        x = v.x;
        y = v.y;
        z = v.z;
        this->w = w;
    }
    float distance(const SE_Vector4f& rv) const
    {
        SE_Vector4f dir = subtract(rv);
        return dir.length();
    }
    SE_Vector4f normalize() const
    {
        float len = length();
        return SE_Vector4f(x / len, y / len , z / len, w / len);
    }
    float operator[](int i)
    {
        return d[i];
    }
    friend SE_Vector4f operator+(const SE_Vector4f& lv, const SE_Vector4f& rv)
    {
        return lv.add(rv);
    }
    friend SE_Vector4f operator-(const SE_Vector4f& lv, const SE_Vector4f& rv)
    {
        return lv.subtract(rv);
    }
    friend SE_Vector4f operator*(const SE_Vector4f& v, float f)
    {
        return v.mul(f);
    }
    union
    {
        float d[4];
        struct
        {
            float x, y, z, w;
        };
    };

};
class SE_ENTRY SE_Vector3i
{
public:
    SE_Vector3i()
    {
        x = y = z = 0;
    }
    SE_Vector3i(int x, int y, int z)
    {
        this->x = x;
        this->y = y;
        this->z = z;
    }
    SE_Vector3i(int d[3])
    {
        this->d[0] = d[0];
        this->d[1] = d[1];
        this->d[2] = d[2];
    }    
    friend bool operator==(const SE_Vector3i& v1, const SE_Vector3i& v2)
    {
        for(int i = 0 ; i < 3 ; i++)
        {
            if(v1.d[i] != v2.d[i])
                return false;
        }
        return true;

    }

public:
    union
    {
        int d[3];
        struct
        {
            int x, y, z;
        };
    };
};
#endif
