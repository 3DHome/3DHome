#ifndef ASE_LIB_H
#define ASE_LIB_H
#include <list>
#include <vector>
#include <string>
#include <iostream>
#include "SE_KeyFrame.h"
#include "SE_Log.h"
#include "SE_CommonNode.h"
#include "SE_Application.h"

#define ASE_OK 1
#define ASE_ERROR 0
class SE_BufferOutput;
class SE_Spatial;
class SE_CommonNode;
typedef float Vector4[4];

struct ASE_Vertex
{
    float x, y, z;
    ASE_Vertex()
    {
        x=y=z=0.0f;
    }
} ;

struct ASE_Vector3f
{
    float x, y, z;
    ASE_Vector3f()
    {
        x=y=z=0.0f;
    }
};

struct ASE_Quat4f
{
    float x, y, z,w;
    ASE_Quat4f()
    {
        x=y=z=w=0.0f;
    }
};

struct ASE_TVertex
{
    float s, t;
    ASE_TVertex()
    {
        s = t = 0.0f;
    }
} ;
struct ASE_Face
{
    int vi[3];
    int materialID;
    ASE_Face()
    {
        memset(vi, 0, sizeof(int) * 3);
        materialID = -1;
    }
} ;
struct ASE_Matrix4f
{
    float m[16];
    ASE_Matrix4f()
    {
        memset(m, 0, sizeof(float) * 16);
    }
};
struct ASE_Bone
{
    std::string name;
    std::list<ASE_Bone*> children;
    ASE_Bone* parent;
    ASE_Matrix4f* matrixseq;
    int matrixseqnum;
    ASE_Matrix4f matrixbase;
    ASE_Bone()
    {
        parent = NULL;
        matrixseq = NULL;
        matrixseqnum = 0;
    }
};
struct ASE_BoneWeight
{
    enum {INVALID_BONE_INDEX = -1};
    int boneIndex;
    float weight;
    ASE_BoneWeight()
    {
        boneIndex = INVALID_BONE_INDEX;
        weight = 0;
    }
};
struct ASE_SkinJointController
{
    typedef std::vector<ASE_BoneWeight> JointList;
    std::vector<ASE_Bone*> jointVector;
    std::string objName;
    std::vector<JointList> vertexJointVector;//every vertex in object has a cooresponding JointList

};

//biped
struct ASE_BipedKeyFrame
{
    int frameIndex;
    ASE_Quat4f rotateQ;
    ASE_Vector3f scale;
    ASE_Vector3f translate;
};

class ASE_Biped
{
public:
    ASE_Biped()
    {
        parent = NULL;
    }
    ~ASE_Biped()
    {
        std::vector<ASE_BipedKeyFrame*>::iterator it;
        for(it = animationInfo.begin() ; it != animationInfo.end() ; it++)
        {
            ASE_BipedKeyFrame* go = *it;
            delete go;
        }
    }

    std::string bipName;
    std::list<ASE_Biped*> children;
    ASE_Biped* parent;
    ASE_Matrix4f bind_pose;
    

    std::vector<ASE_BipedKeyFrame*> animationInfo;

};

struct ASE_BipedWeight
{    
    std::vector<int> bipedIndex; //The number of bip may not be one
    std::vector<float> weight; //same as above
};

class ASE_SkeletonUnit
{
public:
    ASE_SkeletonUnit(){}
    ~ASE_SkeletonUnit()
    {
        std::vector<ASE_BipedWeight*>::iterator it;
        for(it = objVertexBlendInfo.begin() ; it != objVertexBlendInfo.end() ; it++)
        {
            ASE_BipedWeight* go = *it;
            delete go;
        }

    }

    int bipedNum; //The number of bip to bind to the object
    int vertexNum;
    std::string objHasBiped;
    std::string controllerId;
    std::vector<std::string> bipedNamesOnObj;//which bip bind to this obj
    std::vector<ASE_BipedWeight *> objVertexBlendInfo;//index,weight list on this obj
};

class ASE_BipedController
{
public:
    ASE_BipedController(){}
    ~ASE_BipedController()
    {
        {
            std::vector<ASE_Biped*>::iterator it;
            for(it = oneBipAnimation.begin() ; it != oneBipAnimation.end() ; it++)
            {
                ASE_Biped* go = *it;
                delete go;
            }
        }

        {
            std::vector<ASE_SkeletonUnit*>::iterator it;
            for(it = bipAndObjInfo.begin() ; it != bipAndObjInfo.end() ; it++)
            {
                ASE_SkeletonUnit* go = *it;
                delete go;
            }
        }
    }
    std::string controllerId;
    std::vector<ASE_Biped*> oneBipAnimation;
    std::vector<ASE_SkeletonUnit*> bipAndObjInfo;
};

struct ASE_Normal
{
    ASE_Vertex faceNormal;
    ASE_Vertex faceVertexNoraml[3];
};

struct ASE_Mesh
{
    int numFaces;
    int numVertexes;
    int numTVertexes;
    int timeValue;
    int faceVertextNormalIndex;
    ASE_Vertex        *vertexes;
    ASE_TVertex    *tvertexes;
    std::vector<ASE_Normal> faceAndVertexNormal;
    ASE_Face        *faces, *tfaces;
    int numFaceGroup;
    std::vector<std::list<int> > faceGroup;
    std::vector<SE_VertexBufferID> surfacesVbID;
    ASE_Mesh()
    {
        timeValue = 0;
        numFaces = 0;
        numVertexes = 0;
        numTVertexes = 0;
        vertexes = NULL;
        tvertexes = NULL;
        faces = tfaces = NULL;
        numFaceGroup = 0;
        faceVertextNormalIndex = 0;
    }
    ~ASE_Mesh()
    {
        delete[] vertexes;
        delete[] tvertexes;
        delete[] faces;
        delete[] tfaces;
    }
} ;

struct ASE_Transform
{
    SE_Quat rotate;
    SE_Vector3f translate;
    SE_Vector3f scale;
    SE_Matrix4f matrix;
};
struct ASE_GeometryObject
{
typedef float Vector3[3];
    char name[256];
    std::string parentName;
    std::string mGroupName;
    ASE_Mesh* mesh;
    int materialref;
    std::list<ASE_Mesh*> keyMeshs;
    std::list<SE_KeyFrame<ASE_Transform>*> keyFrames;
    Vector3 rotateAxis;
    Vector3 scale;
    Vector3 scaleAxis;
    float rotateAngle;
    Vector3 translate;
    Vector3 wireframeColor;
    ASE_GeometryObject()
    {
        memset(name, 0, 256);
        mesh = NULL;
        rotateAxis[0] = rotateAxis[1] = rotateAxis[2] = 0;
        scale[0] = scale[1] = scale[2] = 0;
        scaleAxis[0] = scaleAxis[1] = scaleAxis[2] = 0;
        rotateAngle = 0;
        translate[0] = translate[1] = translate[2] = 0;
        materialref = -1;
        wireframeColor[0] = wireframeColor[1] = wireframeColor[2] = 0;
    }
    ~ASE_GeometryObject()
    {
        if(mesh)
        delete mesh;
    }
};
struct ASE_MaterialData
{
typedef float Vector3[3];
    char texName[256];
    Vector3 ambient;
    Vector3 diffuse;
    Vector3 specular;
    float shine;
    float shineStrength;

    float u_tiling;
    float v_tiling;
    bool hasAlpha;
    ASE_MaterialData()
    {
        memset(texName, 0 , 256);
        ambient[0] = ambient[1] = ambient[2] = 0.0f;
        diffuse[0] = diffuse[1] = diffuse[2] = 0.0f;
        specular[0] = specular[1] = specular[2] = 0.0f;
        shine = 0.5f;
        shineStrength = 0.8;
        u_tiling = 1.0;
        v_tiling = 1.0;
        hasAlpha = false;
    }
};
struct ASE_Material
{
    ASE_MaterialData materialData;
    ASE_MaterialData specularMapData;
    ASE_MaterialData *bumpMaterialData;
    ASE_MaterialData* submaterials;
    int numsubmaterials;
    int numMaterials;
    ASE_Material()
    {
        submaterials = NULL;
        numsubmaterials = 0;
        numMaterials = 0;

        bumpMaterialData = NULL;
    }
    ASE_Material(const ASE_Material& mat)
    {
        submaterials = NULL;
        bumpMaterialData = NULL;
        numsubmaterials = 0;
        numMaterials = 0;
        materialData = mat.materialData;
        numsubmaterials = mat.numsubmaterials;
        numMaterials = mat.numMaterials;
        if(numsubmaterials > 0)
        {
            submaterials = new ASE_MaterialData[numsubmaterials];
            memcpy(submaterials, mat.submaterials, sizeof(ASE_MaterialData) * numsubmaterials);
        }

        if(mat.bumpMaterialData)
        {
            bumpMaterialData = new ASE_MaterialData();
            memcpy(bumpMaterialData, mat.bumpMaterialData, sizeof(ASE_MaterialData));
        }
    }
    ASE_Material& operator=(const ASE_Material& mat)
    {
        if(this == &mat)
            return *this;
        if(SE_Application::getInstance()->SEHomeDebug)
LOGI("///////////////////////////////////////////////////%d\n", numsubmaterials);
        if(numsubmaterials > 0)
            delete[] submaterials;

        if(bumpMaterialData)
        {
            delete bumpMaterialData;
        }

        numsubmaterials = 0;
        numMaterials = 0;
        submaterials = NULL;
        materialData = mat.materialData;
        numsubmaterials = mat.numsubmaterials;
        numMaterials = mat.numMaterials;
        if(numsubmaterials > 0)
        {
            submaterials = new ASE_MaterialData[numsubmaterials];
            memcpy(submaterials, mat.submaterials, sizeof(ASE_MaterialData) * numsubmaterials);
        }

        if(mat.bumpMaterialData)
        {
            bumpMaterialData = new ASE_MaterialData();
            memcpy(bumpMaterialData, mat.bumpMaterialData, sizeof(ASE_MaterialData));
        }
        return *this;
    }
    ~ASE_Material()
    {
        if(numsubmaterials > 0)
            delete[] submaterials;

        if(bumpMaterialData)
        {
            delete bumpMaterialData;
        }
    }
};

struct _MaterialData {
    int subMaterialNum;
    ASE_MaterialData md;
    SE_MaterialDataID mid;
    SE_ImageDataID tid;

    ASE_MaterialData mSpecularData;
    SE_MaterialDataID mSpecularid;
    SE_ImageDataID sptid;
    int mUsingCount;
    _MaterialData() {
        mUsingCount = 0;
        subMaterialNum = 0;
    }
};
struct _GeomTexCoordData {
    SE_GeometryDataID geomID;
    SE_TextureCoordDataID texCoordID;
};
struct ASE_Shader
{
    std::string shaderID;
    std::string vertexShaderName;
    std::string fragmentShaderName;
    std::string shaderClassName;
};
struct ASE_Renderer
{
    std::string rendererID;
    std::string rendererClassName;
};
struct ASE_HelperObject
{
typedef float Vector3[3];
    std::string name;
    Vector3 baseTranslate;
    Vector3 baseScale;
    Vector4 baseRotate;
    std::list<SE_KeyFrame<ASE_Transform>*> keyFrames;
    SE_Spatial* spatial;
    ASE_HelperObject()
    {
        baseTranslate[0] = baseTranslate[1] = baseTranslate[2] = 0;
        baseScale[0] = baseScale[1] = baseScale[2] = 1;
        baseRotate[0] = baseRotate[1] = baseRotate[2] = 0;
        baseRotate[3] = 1;
        spatial = NULL; 
    }
};
struct ASE_GeometryObjectGroup
{
    ASE_HelperObject parent;
    std::list<ASE_GeometryObject*> children;
};

struct ASE_CameraObjectPos
{
    SE_Vector3f mCameraPos;
    SE_Vector3f mCameraTargetPos;
    std::string mCamraName;
    std::string mCameraTargetName;
    std::string mCameraType;
};

struct ASE_SceneObject
{
    std::list<ASE_GeometryObject*> mGeomObjects;
    std::vector<ASE_Material> mMats;
    std::list<ASE_SkinJointController*> mSkinJointController;

    std::vector<ASE_BipedController*> mBipsController;

    std::list<ASE_GeometryObjectGroup*> mGeometryObjectGroup;
    std::vector<ASE_Shader*> mShaderObjects;
    std::vector<ASE_Renderer*> mRendererObjects;

    std::vector<ASE_CameraObjectPos*> mBestPositon;


    ~ASE_SceneObject()
    {
        {
            std::list<ASE_GeometryObject*>::iterator it;
            for(it = mGeomObjects.begin() ; it != mGeomObjects.end() ; it++)
            {
                ASE_GeometryObject* go = *it;
                delete go;
            }
            mGeomObjects.clear();
        }

        //free skinJoint
        {
            std::list<ASE_SkinJointController*>::iterator it;
            for(it = mSkinJointController.begin() ; it != mSkinJointController.end() ; it++)
            {
                ASE_SkinJointController* go = *it;
                delete go;
            }
            mSkinJointController.clear();
        }

        //free biped Controller
        {
            std::vector<ASE_BipedController*>::iterator it;
            for(it = mBipsController.begin() ; it != mBipsController.end() ; it++)
            {
                ASE_BipedController* go = *it;
                delete go;
            }
            mBipsController.clear();
        }

        //free geometry object group
        {
            std::list<ASE_GeometryObjectGroup*>::iterator it;
            for(it = mGeometryObjectGroup.begin() ; it != mGeometryObjectGroup.end() ; it++)
            {
                ASE_GeometryObjectGroup* go = *it;
                delete go;
            }
            mGeometryObjectGroup.clear();
        }

        //free shader object
        {
            std::vector<ASE_Shader*>::iterator it;
            for(it = mShaderObjects.begin() ; it != mShaderObjects.end() ; it++)
            {
                ASE_Shader* go = *it;
                delete go;
            }
            mShaderObjects.clear();
        }

        //free render object
        {
            std::vector<ASE_Renderer*>::iterator it;
            for(it = mRendererObjects.begin() ; it != mRendererObjects.end() ; it++)
            {
                ASE_Renderer* go = *it;
                delete go;
            }
            mRendererObjects.clear();
        }

        //free best position camera
        {
            std::vector<ASE_CameraObjectPos*>::iterator it;
            for(it = mBestPositon.begin() ; it != mBestPositon.end() ; it++)
            {
                ASE_CameraObjectPos* go = *it;
                delete go;
            }
            mBestPositon.clear();
        }
    }
};

class ASE_Loader
{
public:
    ASE_Loader();
    ~ASE_Loader();
    void Load( const char *filename, bool verbose,bool isSceneData = false);
    ASE_SceneObject* getSceneObject()
    {
        return mSceneObject;
    }
    void Write(const char* outPath, const char* filename,bool averageNormal);
    void Write(SE_BufferOutput& output, SE_BufferOutput& outputScene, const char* shaderPath,const char *cbfName,bool averageNormal);
    void LoadEnd();
    typedef void (ASE_Loader::*ParserFun)( const char * );
private:
    int CharIsTokenDelimiter( int ch );
    void ASE_Process(bool isSceneData);
    
    int ASE_GetToken(bool restOfLine);
    void ASE_ParseBracedBlock( ParserFun parser );
    void ASE_SkipEnclosingBraces();
    void ASE_SkipRestOfLine();
    void ASE_KeyMAP_DIFFUSE( const char *token );
    void ASE_KeyMAP_SPECULAR( const char *token );
    void ASE_KeyMAP_BUMP( const char *token );
    void ASE_KeyMAP_GENERIC( const char *token );
    void ASE_KeyMATERIAL( const char *token );
    void ASE_KeyMATERIAL_LIST( const char *token );
    void ASE_KeyMESH_VERTEX_LIST( const char *token );
    void ASE_KeyMESH_FACE_LIST( const char *token );
    void ASE_KeyTFACE_LIST( const char *token );
    void ASE_KeyMESH_TVERTLIST( const char *token );
    void ASE_KeyMAPINGPCHANNELMESH_TVERTLIST( const char *token );
    void ASE_KeyMESH_FACEANDVERTEX_NORMALS( const char *token );
    void ASE_KeyMESH( const char *token );
    void ASE_KeyMESH_ANIMATION( const char *token );
    void fillKeyMesh(ASE_GeometryObject* parent);
    void ASE_KeyGEOMOBJECT( const char *token );
    void ASE_KeyMAP_SUBMATERIAL(const char* token);
    void ASE_KeyNODETM(const char* token);
    void ASE_KeyBONEINFO(const char* token);
    void ASE_KeyBONEVERTEXINFO(const char* token);
    void ASE_KeyBONEMATRIX(const char* token);
    void ASE_KeyBONEMATRIXINFO(const char* token);

    void ASE_KeyBIPEDINFO(const char* token);
    void ASE_KeySKELETONINFO(const char* token);
    void ASE_KeyNODEINFO(const char* token);
    void ASE_KeyTRANSFORMINFO_INFO(const char* token);
    void ASE_KeyTRANSFORMINFO(const char* token);
    void ASE_KeyKEYFRAMEINFO(const char* token);
    void ASE_KeyBIPEDVERTEXINFO(const char* token);
    void ASE_KeyBIPEDWEIGHTINFO(const char* token);

    void ASE_KeyCAMERABESTPOSITIONINFO(const char* token);
    void ASE_KeyCAMERAPOSITIONINFO(const char* token);
    void ASE_KeyCAMERATARGETPOSITIONINFO(const char* token);

    void ASE_KeyHELPEROBJECT(const char* token);
    void ASE_KeyCONTROLROTTRACK(const char* token);
    void ASE_KeyCONTROLPOSTRACK(const char* token);
    void ASE_KeyTMANIMATION(const char* token);
    void ASE_KeySHADER(const char* token);
    void ASE_KeyRENDERER(const char* token);
    void ASE_AdjustSubMtl();
    ASE_GeometryObjectGroup* findGroup(std::string parentname);
    SE_KeyFrame<ASE_Transform>* findKeyFrame(std::list<SE_KeyFrame<ASE_Transform>*>& keyFrames, unsigned int key);
    ASE_Mesh* findAnimMesh(std::list<ASE_Mesh*>& keyMeshs, unsigned int key);
    
    void ASE_KeyGROUPNODE(const char* token);
    SE_Spatial *findGroupNode(SE_Spatial *root,const char *groupname,NodeTypes type);
    bool isLodGroup(const char *groupname);
private:
    std::vector<ASE_SceneObject*> mSceneList;
    ASE_SceneObject* mSceneObject;
    typedef std::list<ASE_GeometryObject*> GeomObjectList;
    int mInitOK;
    ASE_GeometryObject* mCurrGeomObject;
    ASE_Material* mCurrMtl;
    ASE_MaterialData* mCurrSubMtl;
    ASE_Mesh* mCurrMesh;
    ASE_SkinJointController* mCurrSkinJointController;
    ASE_Bone* mCurrBone;
    ASE_GeometryObjectGroup* mCurrGeometryObjectGroup;
    ASE_Shader* mCurrShader;
    bool mInSubDiffuse;
    int mMatStartPos;

    ASE_SkeletonUnit *mCurrSU;
    ASE_Biped * mCurrNode;
    bool isLineEnd();
    ASE_BipedController *findCurrBipedController(const char *controllerID);
    ASE_BipedController *mCurrBipedController;

    ASE_CameraObjectPos *mCurrentCameraPos;
    bool mQuoteMarkFinish; //for "bip01 head" this SPACE before "head" should not be as a delimiter
    bool mIsKeyMesh;
    int mFrameMax;

    bool mIsGroupNode;
    std::string mCurrentGroupName;

    std::vector<_MaterialData> materialVector;
};
#endif

