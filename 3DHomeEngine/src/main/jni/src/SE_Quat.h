#ifndef SE_QUAT_H
#define SE_QUAT_H
#include "SE_Vector.h"
#include "SE_Matrix.h"
class SE_ENTRY SE_Quat
{
public:
    static const SE_Quat IDENTITY;
    SE_Quat()
    {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }
    SE_Quat(float x, float y, float z, float w)
    {
        this->x = x;
        this->y = y;
         this->z = z;
        this->w = w;
    }
    SE_Quat(const SE_Vector3f& v, float w)
    {
        this->x = v.x;
        this->y = v.y;
        this->z = v.z;
        this->w = w;
    }
    SE_Quat(float d[4])
    {
        for(int i = 0 ; i < 4 ; i++)
        {
            this->d[i] = d[i];
        }
    }
    SE_Quat(float angle, const SE_Vector3f& axis);
    SE_Quat(const SE_Quat& q)
    {
        for(int i = 0 ; i < 4 ; i++)
        {
            d[i] = q.d[i];
        }
    }
    SE_Quat& operator=(const SE_Quat& rq)
    {
        if(this == &rq)
            return *this;
        for(int i = 0 ; i < 4 ; i++)
        {
            d[i] = rq.d[i];
        }
        return *this;
    }
    bool operator==(const SE_Quat& right) const
    {
        return x == right.x && y == right.y && z == right.z && w == right.w;
    }
    bool operator !=(const SE_Quat& right) const
    {
        return !this->operator==(right);
    }
    void identity()
    {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }
    SE_Quat neg() const
    {
        return SE_Quat(-x, -y, -z, -w);
    }
    SE_Quat add(const SE_Quat& rq) const
    {
        return SE_Quat(x + rq.x , y + rq.y , z + rq.z, w + rq.w);
    }
    bool hasInverse()
    {
        return lengthSquare() != 0.0f;
    }
    SE_Quat subtract(const SE_Quat& rq) const
    {
        return SE_Quat(x - rq.x, y - rq.y, z - rq.z, w - rq.w);
    }
    SE_Quat mul(const SE_Quat& rq) const;
    SE_Quat conjugate() const;
    SE_Quat inverse() const;
    float length() const;
    float lengthSquare() const;
    SE_Vector3f map(const SE_Vector3f& v) const;
    SE_Matrix3f toMatrix3f() const;
    SE_Vector4f toRotate() const;
    bool isZero() const
    {
        return x == 0 && y ==0 && z == 0  && w == 0;
    }
    void setZero()
    {
        x = y = z = w = 0;
    }
    void set(float angle, const SE_Vector3f& axis);
    SE_Vector4f toVector4f() const
    {
        return SE_Vector4f(x, y, z, w);
    }
 static SE_Quat slerp(SE_Quat& starting,SE_Quat& ending,float t);
public:
    union
    {
        float d[4];
        struct
        {
            float x, y, z , w;
        };
    };

};
#endif
