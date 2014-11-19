#include "SE_DynamicLibType.h"
#include "SE_GeometryData.h"
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Quat.h"
#include "SE_Log.h"
#include "SE_MemLeakDetector.h"
SE_GeometryData::SE_GeometryData()
{
    vertexArray = NULL;
    vertexNum = 0;
    mOwnVertex = true;
    faceArray = NULL;
    faceNum = 0;
    mOwnFace = true;

    faceNormalArray = NULL;
    faceNormalNum = 0;
    mOwnNormal = true;

    faceVertexNormalArray = NULL;
    faceVertexNormalNum = 0;

    colorArray = NULL;
    colorNum = 0;
    mOwnColor = true;

}
SE_GeometryData::~SE_GeometryData()
{
    if(vertexArray && mOwnVertex)
        delete[] vertexArray;
    if(faceArray && mOwnFace)
        delete[] faceArray;
    if(faceNormalArray && mOwnNormal)
        delete[] faceNormalArray;
    if(colorArray && mOwnColor)
        delete[] colorArray;

    if(faceVertexNormalArray)
    {
        delete[] faceVertexNormalArray;
    }
}

SE_Vector3f* SE_GeometryData::getFaceNormals()
{
    return faceNormalArray;
}

SE_Vector3f* SE_GeometryData::getFaceVertexNormals()
{
    return faceVertexNormalArray;
}

int SE_GeometryData::getFaceVertexNormalsNum()
{
    return faceVertexNormalNum;
}

int SE_GeometryData::getFaceNormalsNum()
{
    return faceNum;
}



void SE_GeometryData::setVertexArray(SE_Vector3f* va, int num, bool own)
{
    if(vertexArray && mOwnVertex)
        delete[] vertexArray;
    vertexArray = va;
    vertexNum = num;
    mOwnVertex = own;
}
void SE_GeometryData::setFaceArray(SE_Vector3i* fa, int num, bool own)
{
    if(faceArray && mOwnFace)
        delete[] faceArray;
    faceArray = fa;
    faceNum = num;
    mOwnFace = own;
}
/**************  add for particle    ***************/
void SE_GeometryData::setParticleVertexArray(SE_Vector3f* va, int num, bool own)
{
    vertexArray = va;
    vertexNum = num;
    mOwnVertex = own;
}
/**************  add for particle    ***************/
void SE_GeometryData::setParticleFaceArray(SE_Vector3i* fa, int num, bool own)
{
    faceArray = fa;
    faceNum = num;
    mOwnFace = own;
}

/**************  add for particle    ***************/
void SE_GeometryData::setParticleColorArray(SE_Vector4f* ca, int num, bool own)
{
    colorArray = ca;
    colorNum = num;
    mOwnFace = own;
}

void SE_GeometryData::setFaceNormalArray(SE_Vector3f* na, int num, bool own)
{
    if(faceNormalArray && mOwnNormal)
        delete[] faceNormalArray;
    faceNormalArray = na;
    faceNormalNum = num;
    mOwnNormal = own;
}

void SE_GeometryData::setFaceVertexNormalArray(SE_Vector3f* fvna, int num, bool own)
{
    if(faceVertexNormalArray && mOwnNormal)
        delete[] faceVertexNormalArray;
    faceVertexNormalArray = fvna;
    faceVertexNormalNum = num;
    mOwnNormal = own;
}

void SE_GeometryData::transform(SE_GeometryData* src, const SE_Matrix4f& m, SE_GeometryData* dst)
{
    SE_Vector3f* vertex = NULL;
    SE_Vector3i* faces = NULL;
    SE_Vector3f* facenormal = NULL;
    SE_Vector3f* facevertexnormal = NULL;

    int vertexNum = 0;
    int faceNum = 0;
    int facenormalNum = 0;
    int facevertexnormalNum = 0;

    if(src->vertexArray)
    {
        vertex = new SE_Vector3f[src->vertexNum];
        for(int i = 0 ; i < src->vertexNum ; i++)
        {
            SE_Vector4f v(src->vertexArray[i], 1.0f);
            v = m.map(v);
            vertex[i] = v.xyz();
        }
        vertexNum = src->vertexNum;
    }
    /*
    if(src->faceArray)
    {
        faces = new SE_Vector3i[src->faceNum];
        for(int i = 0 ; i < src->faceNum; i++)
        {
            faces[i] = src->faceArray[i];
        }
        faceNum = src->faceNum;
    }
    */
    if(src->faceNormalArray)
    {
        /*
        //SE_Matrix3f t = m.toMatrix3f();
        //if(t.hasInverse())
        //{
            SE_Matrix4f inverse = m.inverse();
            inverse = inverse.transpose();
            facenormal = new SE_Vector3f[src->faceNormalNum];
            for(int i = 0 ; i < src->faceNormalNum ; i++)
            {
                facenormal[i] = (inverse.map(SE_Vector4f(src->faceNormalArray[i],0))).xyz();
                LOGI("facevertexnormal[%d].x = %f\n",i,facenormal[i].x);
                LOGI("facevertexnormal[%d].y = %f\n",i,facenormal[i].y);
                LOGI("facevertexnormal[%d].z = %f\n",i,facenormal[i].z);
            }
            facenormalNum = src->faceNormalNum;
        //}
        */
    }

    if(src->faceVertexNormalArray)
    {
        /*
        SE_Matrix3f t = m.toMatrix3f();
        if(t.hasInverse())
        {
            SE_Matrix3f inverse = t.inverse();
            inverse = inverse.transpose();
            facevertexnormal = new SE_Vector3f[src->faceVertexNormalNum];
            for(int i = 0 ; i < src->faceVertexNormalNum ; i++)
            {
                facevertexnormal[i] = inverse.map(src->faceVertexNormalArray[i]);
                LOGI("facevertexnormal[%d].x = %f\n",i,facevertexnormal[i].x);
                LOGI("facevertexnormal[%d].y = %f\n",i,facevertexnormal[i].y);
                LOGI("facevertexnormal[%d].z = %f\n",i,facevertexnormal[i].z);
            }
            facevertexnormalNum = src->faceVertexNormalNum;
        }
        */
    }

    dst->setVertexArray(vertex, vertexNum);
    dst->setFaceArray(src->faceArray, src->faceNum, false);
    dst->setFaceNormalArray(facenormal, facenormalNum);
    dst->setFaceVertexNormalArray(facevertexnormal,facevertexnormalNum);
}
void SE_GeometryData::transform(SE_GeometryData* src, const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate, SE_GeometryData* dst)
{
    SE_Vector3f* vertex = NULL;
    SE_Vector3i* faces = NULL;
    SE_Vector3f*facenormal = NULL;
    SE_Vector3f* facevertexnormal = NULL;

    int vertexNum = 0;
    int faceNum = 0;
    int normalNum = 0;
    int facevertexnormalNum = 0;

    if(src->vertexArray)
    {
        vertex = new SE_Vector3f[src->vertexNum];
        for(int i = 0 ; i < src->vertexNum ; i++)
        {
            SE_Vector3f v = scale.mul(src->vertexArray[i]);
            v = rotate.map(v);
            v = v + translate;
            vertex[i] = v;
        }
        vertexNum = src->vertexNum;
    }
    /*
    if(src->faceArray)
    {
        faces = new SE_Vector3i[src->faceNum];
        for(int i = 0 ; i < src->faceNum; i++)
        {
            faces[i] = src->faceArray[i];
        }
        faceNum = src->faceNum;
    }
    */
    if(src->faceNormalArray)
    {
        SE_Matrix3f t;
        t.setScale(scale.x, scale.y, scale.z);
        SE_Matrix3f m;
        m = rotate.toMatrix3f();
        m = m.mul(t);
        if(m.hasInverse())
        {
            SE_Matrix3f inverse = m.inverse();
            m = m.transpose();
            facenormal = new SE_Vector3f[src->faceNormalNum];
            for(int i = 0 ; i < src->faceNormalNum ; i++)
            {
                facenormal[i] = m.map(src->faceNormalArray[i]);
            }
            normalNum = src->faceNormalNum;
        }
    }

    if(src->faceVertexNormalArray)
    {
        SE_Matrix3f t;
        t.setScale(scale.x, scale.y, scale.z);
        SE_Matrix3f m;
        m = rotate.toMatrix3f();
        m = m.mul(t);
        if(m.hasInverse())
        {
            SE_Matrix3f inverse = m.inverse();
            m = m.transpose();
            facevertexnormal = new SE_Vector3f[src->faceVertexNormalNum];
            for(int i = 0 ; i < src->faceVertexNormalNum ; i++)
            {
                facevertexnormal[i] = m.map(src->faceVertexNormalArray[i]);
            }
            facevertexnormalNum = src->faceVertexNormalNum;
        }
    }

    dst->setVertexArray(vertex, vertexNum);
    dst->setFaceArray(src->faceArray, src->faceNum, false);
    dst->setFaceNormalArray(facenormal, normalNum);
    dst->setFaceVertexNormalArray(facevertexnormal,facevertexnormalNum);
}

SE_GeometryDataList::~SE_GeometryDataList()
{
    for(int i = 0; i < mGeoDataList.size(); ++i)
    {
        delete mGeoDataList[i];
    }
}

