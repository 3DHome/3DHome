#ifndef SE_GEOMETRYDATA_H
#define SE_GEOMETRYDATA_H
#include<vector>
class SE_Vector3f;
class SE_Vector3i;
class SE_Vector4f;
class SE_Quat;
class SE_Matrix4f;
class SE_Matrix3f;

class SE_ENTRY SE_GeometryData
{
public:
    SE_GeometryData();
    ~SE_GeometryData();
    SE_Vector3f* getVertexArray()
    {
        return vertexArray;
    }
    int getVertexNum()
    {
        return vertexNum;
    }

    SE_Vector3i* getFaceArray()
    {
        return faceArray;
    }
    int getFaceNum()
    {
        return faceNum;
    }

    SE_Vector4f* getColorArray()
    {
        return colorArray;
    }
    int getColorNum()
    {
        return colorNum;
    }

    SE_Vector3f* getFaceNormals();
    int getFaceNormalsNum();

    SE_Vector3f* getFaceVertexNormals();
    int getFaceVertexNormalsNum();

    void setVertexArray(SE_Vector3f* va, int num, bool own = true);
    void setFaceArray(SE_Vector3i* fa, int num, bool own = true);
    void setFaceNormalArray(SE_Vector3f* na, int num, bool own = true);

    void setFaceVertexNormalArray(SE_Vector3f* fvna, int num, bool own = true);

    /**************  add for particle    ***************/
    void setParticleVertexArray(SE_Vector3f* va, int num, bool own = true);
    void setParticleFaceArray(SE_Vector3i* fa, int num, bool own = true);
    void setParticleColorArray(SE_Vector4f* ca, int num, bool own = true);

    static void transform(SE_GeometryData* src, const SE_Matrix4f& m, SE_GeometryData* dst);
    static void transform(SE_GeometryData* src, const SE_Vector3f& scale, const SE_Quat& rotate, const SE_Vector3f& translate, SE_GeometryData* dst);
private:
    SE_GeometryData(const SE_GeometryData&);
    SE_GeometryData& operator=(const SE_GeometryData&);
private:
    SE_Vector3f* vertexArray;
    int vertexNum;
    bool mOwnVertex;
    SE_Vector3i* faceArray;
    int faceNum;
    bool mOwnFace;

    int faceNormalNum;
    //normal per face(trangle)
    SE_Vector3f* faceNormalArray;

    int faceVertexNormalNum;
    //vertex normal per face(trangle),the vertexs normal are sort by face
    SE_Vector3f* faceVertexNormalArray;

    
    bool mOwnNormal;

    SE_Vector4f* colorArray;
    int colorNum;
    bool mOwnColor;
};

class SE_GeometryDataList 
{
public:

    virtual ~SE_GeometryDataList();
    
    std::vector<SE_GeometryData*> mGeoDataList;
};
#endif
