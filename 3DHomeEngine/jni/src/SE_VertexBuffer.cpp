#include "SE_DynamicLibType.h"
#include <list>
#include "SE_Vector.h"
#include "SE_Common.h"
#include "SE_VertexBuffer.h"
#include "SE_Log.h"
#include<iostream>
#include "SE_MemLeakDetector.h"
#include "SE_Application.h"
#ifdef GLES_20
    #include <GLES2/gl2.h>
#else
    #include <GLES/gl.h>
#endif

struct SE_VertexBuffer::Impl
{
public:
    GLuint dataID;
    GLuint indexID;
    Impl()
    {
        dataID = 0; 
        indexID = 0;
    }
};

SE_VertexBuffer::SE_VertexBuffer()
{

    vertexData = NULL;
    vertexDataNum = 0;
    indexData = NULL;
    indexNum = 0;
    mHasTexture = true;
    mVBS = 0;
    mPosSize = 3;
    mTex0Size = 2;
    mTex1Size = 2;
    mColorSize = 4;
    mNormalSize = 3;
    mTangentSize= 3;
    mVertexDataSize = 0;
    mDataStride = 0;
    mImpl = NULL;
    mVboHasInit = false;
    mImpl = new Impl();
    mDynamicDraw = false;
}
SE_VertexBuffer::~SE_VertexBuffer()
{
    if(vertexData)
    {
        delete[] vertexData;
    }

    if(indexData)
    {
        delete[] indexData;
    }
    if(mVboHasInit)
    {
        glDeleteBuffers(1, &mImpl->dataID);
	    glDeleteBuffers(1, &mImpl->indexID);
    }
    if(mImpl)
    {
        delete mImpl;
    }
}

int SE_VertexBuffer::addVertexData(SE_VertexFormat::Pos v)
{
    //LOGI("## Prepare data struct, v[0] = %f ##\n",v.pos.d[0]);
    std::cout<<"## Prepare data struct, v[0] ="<<v.pos.d[0]<<std::endl;
    std::vector<SE_VertexFormat::Pos>::iterator it;
    int i;
    for(i = 0, it = posDataList.begin() ; 
        it != posDataList.end() ; it++, i++)
    {
        if(it->pos.d[0] == v.pos.d[0] &&
           it->pos.d[1] == v.pos.d[1] &&
           it->pos.d[2] == v.pos.d[2] &&
           it->normal.d[0] == v.normal.d[0] &&
           it->normal.d[1] == v.normal.d[1] &&
           it->normal.d[2] == v.normal.d[2])
        {
            return i;
        }
    }
    SE_ASSERT(it == posDataList.end());
    posDataList.push_back(v);
    return i;
}

int SE_VertexBuffer::addVertexData(SE_VertexFormat::PosTex0 v)
{
    //LOGI("## Prepare data struct, v[0] = %f ##\n",v.pos.d[0]);
    std::cout<<"## Prepare data struct, v[0] ="<<v.pos.d[0]<<std::endl;
    std::vector<SE_VertexFormat::PosTex0>::iterator it;
    int i;
    for(i = 0, it = postex0DataList.begin() ; 
        it != postex0DataList.end() ; it++, i++)
    {
        if(it->pos.d[0] == v.pos.d[0] &&
           it->pos.d[1] == v.pos.d[1] &&
           it->pos.d[2] == v.pos.d[2] &&
           it->tex0.d[0] == v.tex0.d[0] &&
           it->tex0.d[1] == v.tex0.d[1] &&
            it->normal.d[0] == v.normal.d[0] &&
           it->normal.d[1] == v.normal.d[1] &&
           it->normal.d[2] == v.normal.d[2]
        )
        {
            return i;
        }
    }
    SE_ASSERT(it == postex0DataList.end());
    postex0DataList.push_back(v);
    return i;
}

int SE_VertexBuffer::addVertexData(SE_VertexFormat::PosTex0Tex1 v)
{
    //LOGI("## Prepare data struct, v[0] = %f ##\n",v.pos.d[0]);
    std::cout<<"## Prepare data struct, v[0] ="<<v.pos.d[0]<<std::endl;
    std::vector<SE_VertexFormat::PosTex0Tex1>::iterator it;
    int i;
    for(i = 0, it = postex0tex1DataList.begin() ; 
        it != postex0tex1DataList.end() ; it++, i++)
    {
        if(it->pos.d[0] == v.pos.d[0] &&
           it->pos.d[1] == v.pos.d[1] &&
           it->pos.d[2] == v.pos.d[2] &&
           it->tex0.d[0] == v.tex0.d[0] &&
           it->tex0.d[1] == v.tex0.d[1] &&
           it->tex1.d[0] == v.tex1.d[0] &&
           it->tex1.d[1] == v.tex1.d[1])
        {
            return i;
        }
    }
    SE_ASSERT(it == postex0tex1DataList.end());
    postex0tex1DataList.push_back(v);
    return i;
}

int SE_VertexBuffer::addVertexData(SE_VertexFormat::PosColor v)
{
    //LOGI("## Prepare data struct, v[0] = %f ##\n",v.pos.d[0]);
    std::cout<<"## Prepare data struct, v[0] ="<<v.pos.d[0]<<std::endl;
    std::vector<SE_VertexFormat::PosColor>::iterator it;
    int i;
    for(i = 0, it = poscolorDataList.begin() ; 
        it != poscolorDataList.end() ; it++, i++)
    {
        if(it->pos.d[0] == v.pos.d[0] &&
           it->pos.d[1] == v.pos.d[1] &&
           it->pos.d[2] == v.pos.d[2] &&
           it->color.d[0] == v.color.d[0] &&
           it->color.d[1] == v.color.d[1] &&
           it->color.d[2] == v.color.d[2] &&
           it->color.d[3] == v.color.d[3])
        {
            return i;
        }
    }
    SE_ASSERT(it == poscolorDataList.end());
    poscolorDataList.push_back(v);
    return i;
}


#if 0
void SE_VertexBuffer::createVertexBuffer(const char*objname,int facenum,_Vector3i *faces,_Vector3f *vertexPos,
                                         _Vector3i *tfaces,
                                         _Vector2f *textureCoord0,
                                         _Vector2f *textureCoord1,
                                        _Vector4f *color,
                                        _Vector3f *normal)
{   
    int facetNum = facenum;    

    _Vector3i* faceArray = faces;

    _Vector3f* vertexArray = vertexPos; 

    /*
    switch(mVBS)
    {
    case VBS_VERTEXPOS:
        createPosVertexBuffer(facenum,faces,vertexPos);
        break;
    case VBS_VERTEXPOS + VBS_TEXTURE0 :
        createPosTex0VertexBuffer(facenum,faces,vertexPos,tfaces,textureCoord0);
        break;
    case VBS_VERTEXPOS + VBS_TEXTURE0 + VBS_TEXTURE1:
        createPosTex0Tex1VertexBuffer(facenum,faces,vertexPos,tfaces,textureCoord0,textureCoord1);
        break;


    }
    */

    if(!hasVBState(VBS_TEXTURE0))    
    {
        //the obj has no texture,store vertex only.
        std::list<SE_Vector3i> indexDataList;
        for(int i = 0 ; i < facetNum ; i++)
        {
            int faceIndex = i;
            _Vector3i posFace = faceArray[faceIndex];

            SE_VertexFormat::Pos v0, v1, v2;

            v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
            v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
            v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];

            int index0 = addVertexData(v0);
            v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
            v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
            v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];

            int index1 = addVertexData(v1);
            v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
            v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
            v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];

            int index2 = addVertexData(v2);

            indexDataList.push_back(SE_Vector3i(index0, index1, index2));
        }

        SE_VertexFormat::Pos* data = new SE_VertexFormat::Pos[posDataList.size()];

        std::vector<SE_VertexFormat::Pos>::iterator it;
        int i;
        for(i = 0 , it = posDataList.begin() ; it != posDataList.end() ; it++, i++)
        {
            data[i] = *it;
        }
        unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];
        std::list<SE_Vector3i>::iterator itIndex;

        int j;
        for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
        {
            indexdata[j] = itIndex->x;
            indexdata[j + 1] = itIndex->y;
            indexdata[j + 2] = itIndex->z;       
        }        
        vertexData = (float*)data;
        vertexDataNum = posDataList.size() * getVertexDataSize();
        indexData = indexdata;
        indexNum = indexDataList.size() * 3;     
    }
    else
    {
        _Vector3i* texFaceArray = tfaces;
        _Vector2f* texVertexArray = textureCoord0;


        //std::list<SE_VertexFormat::PosTex0> vertexDataList;
        std::list<SE_Vector3i> indexDataList;

        for(int i = 0 ; i < facetNum ; i++)
        {
            int faceIndex = i;

            _Vector3i posFace = faceArray[faceIndex];
            _Vector3i texFace = texFaceArray[faceIndex];

            SE_VertexFormat::PosTex0 v0, v1, v2;

            v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
            v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
            v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];
            v0.tex0.d[0] = texVertexArray[texFace.d[0]].d[0];
            v0.tex0.d[1] = texVertexArray[texFace.d[0]].d[1];
            int index0 = addVertexData(v0);

            v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
            v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
            v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];
            v1.tex0.d[0] = texVertexArray[texFace.d[1]].d[0];
            v1.tex0.d[1] = texVertexArray[texFace.d[1]].d[1];
            int index1 = addVertexData(v1);


            v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
            v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
            v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];
            v2.tex0.d[0] = texVertexArray[texFace.d[2]].d[0];
            v2.tex0.d[1] = texVertexArray[texFace.d[2]].d[1];
            int index2 = addVertexData(v2);
            indexDataList.push_back(SE_Vector3i(index0, index1, index2));
        }
        SE_VertexFormat::PosTex0* data = new SE_VertexFormat::PosTex0[postex0DataList.size()];
        std::vector<SE_VertexFormat::PosTex0>::iterator it;
        int i;
        for(i = 0 , it = postex0DataList.begin() ; it != postex0DataList.end() ; it++, i++)
        {
            data[i] = *it;
        }

        unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];

        std::list<SE_Vector3i>::iterator itIndex;

        int j;
        for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
        {
            indexdata[j] = itIndex->x;
            indexdata[j + 1] = itIndex->y;
            indexdata[j + 2] = itIndex->z;       
        }

        vertexData = (float*)data;
        vertexDataNum = postex0DataList.size() * getVertexDataSize();
        indexData = indexdata;
        indexNum = indexDataList.size() * 3;
        
    }
}
#endif

void SE_VertexBuffer::createVertexBuffer(const char*objname,int facenum,_Vector3i *faces,_Vector3f *vertexPos,
                                         _Vector3i *tfaces,
                                         _Vector2f *textureCoord0,
                                         _Vector2f *textureCoord1,
                                        _Vector4f *color,
                                        _Vector3f *normal)
{

    switch(mVBS)
    {
    case VBS_VERTEXPOS:
        createVertexBuffer(facenum,faces,vertexPos,normal);
        break;
    case VBS_VERTEXPOS + VBS_TEXTURE0 :
        createVertexBuffer(facenum,faces,vertexPos,tfaces,textureCoord0,normal);
        break;
    case VBS_VERTEXPOS + VBS_TEXTURE0 + VBS_TEXTURE1:
        createVertexBuffer(facenum,faces,vertexPos,tfaces,textureCoord0,textureCoord1);
        break;

    }
}

int SE_VertexBuffer::getVertexDataSize()
{
    if(mVBS == 0)
    {
        return 0;
    }
    else if(mVBS == VBS_VERTEXPOS)
    {
        mVertexDataSize = 3 + 3;//pos + normal
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0)
    {
        mVertexDataSize = 5 + 3;// +normal
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0 + VBS_TEXTURE1)
    {
        mVertexDataSize = 7;
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_VERTEXCOLOR)
    {
        mVertexDataSize = 7;
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0 +VBS_VERTEXCOLOR)
    {
        mVertexDataSize = 9;
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("The data structor is unknown!!\n");
    }

    return mVertexDataSize; 
}

int SE_VertexBuffer::getDataStride()
{
    if(mVBS == 0)
    {
        return 0;
    }
    else if(mVBS == VBS_VERTEXPOS)
    {
        mDataStride = 3 + 3;//+normal
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0)
    {
        mDataStride = 5 + 3;//+normal
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0 + VBS_TEXTURE1)
    {
        mDataStride = 7;
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_VERTEXCOLOR)
    {
        mDataStride = 7;
    }
    else if(mVBS == VBS_VERTEXPOS + VBS_TEXTURE0 + VBS_VERTEXCOLOR)
    {
        mDataStride = 9;
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("The data structor is unknown!!\n");
    }

    return mDataStride; 
}

void SE_VertexBuffer::createVertexBuffer(int facenum,_Vector3i *faces,_Vector3f *vertexPos,_Vector3f *vertexNormal)
{
    //pos
    int facetNum = facenum;    

    _Vector3i* faceArray = faces;

    _Vector3f* vertexArray = vertexPos; 

    _Vector3f* normalArray = vertexNormal;

    std::list<SE_Vector3i> indexDataList;

    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        int faceIndex = i;
        _Vector3i posFace = faceArray[faceIndex];

        SE_VertexFormat::Pos v0, v1, v2;

        v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
        v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
        v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];

        v0.normal.d[0] = normalArray[k].d[0];//normal array is normal per face struct
        v0.normal.d[1] = normalArray[k].d[1];
        v0.normal.d[2] = normalArray[k].d[2];
        int index0 = addVertexData(v0);

        k++;
        v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
        v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
        v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];

        v1.normal.d[0] = normalArray[k].d[0];//normal array is normal per face struct
        v1.normal.d[1] = normalArray[k].d[1];
        v1.normal.d[2] = normalArray[k].d[2];
        int index1 = addVertexData(v1);

        k++;
        v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
        v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
        v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];

        v2.normal.d[0] = normalArray[k].d[0];//normal array is normal per face struct
        v2.normal.d[1] = normalArray[k].d[1];
        v2.normal.d[2] = normalArray[k].d[2];
        int index2 = addVertexData(v2);
        k++;

        indexDataList.push_back(SE_Vector3i(index0, index1, index2));
    }

    SE_VertexFormat::Pos* data = new SE_VertexFormat::Pos[posDataList.size()];

    std::vector<SE_VertexFormat::Pos>::iterator it;
    int i;
    for(i = 0 , it = posDataList.begin() ; it != posDataList.end() ; it++, i++)
    {
        data[i] = *it;
    }
    unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];
    std::list<SE_Vector3i>::iterator itIndex;

    int j;
    for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
    {
        indexdata[j] = itIndex->x;
        indexdata[j + 1] = itIndex->y;
        indexdata[j + 2] = itIndex->z;       
    }        
    vertexData = (float*)data;
    vertexDataNum = posDataList.size() * getVertexDataSize();
    indexData = indexdata;
    indexNum = indexDataList.size() * 3;    
}

void SE_VertexBuffer::createVertexBuffer(int facenum,_Vector3i *faces,_Vector3f *vertexPos,_Vector3i *tfaces,_Vector2f *textureCoord0,_Vector3f *vertexNormal)
{
    //pos_tex0
    int facetNum = facenum;

    _Vector3i* faceArray = faces;

    _Vector3f* vertexArray = vertexPos; 

    _Vector3i* texFaceArray = tfaces;

    _Vector2f* texVertexArray = textureCoord0;

    _Vector3f* normalArray = vertexNormal;


    //std::list<SE_VertexFormat::PosTex0> vertexDataList;
    std::list<SE_Vector3i> indexDataList;

    int k = 0;
    for(int i = 0 ; i < facetNum ; i++)
    {
        int faceIndex = i;

        _Vector3i posFace = faceArray[faceIndex];
        _Vector3i texFace = texFaceArray[faceIndex];

        SE_VertexFormat::PosTex0 v0, v1, v2;

        v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
        v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
        v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];
        v0.tex0.d[0] = texVertexArray[texFace.d[0]].d[0];
        v0.tex0.d[1] = texVertexArray[texFace.d[0]].d[1];
        v0.normal.d[0] = normalArray[k].d[0];//normal array is normal per face struct
        v0.normal.d[1] = normalArray[k].d[1];
        v0.normal.d[2] = normalArray[k].d[2];
        int index0 = addVertexData(v0);
        k++;

        v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
        v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
        v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];
        v1.tex0.d[0] = texVertexArray[texFace.d[1]].d[0];
        v1.tex0.d[1] = texVertexArray[texFace.d[1]].d[1];
        v1.normal.d[0] = normalArray[k].d[0];//add normal data
        v1.normal.d[1] = normalArray[k].d[1];
        v1.normal.d[2] = normalArray[k].d[2];
        int index1 = addVertexData(v1);
        k++;


        v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
        v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
        v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];
        v2.tex0.d[0] = texVertexArray[texFace.d[2]].d[0];
        v2.tex0.d[1] = texVertexArray[texFace.d[2]].d[1];
        v2.normal.d[0] = normalArray[k].d[0];//add normal data
        v2.normal.d[1] = normalArray[k].d[1];
        v2.normal.d[2] = normalArray[k].d[2];
        int index2 = addVertexData(v2);
        k++;
        indexDataList.push_back(SE_Vector3i(index0, index1, index2));
    }
    SE_VertexFormat::PosTex0* data = new SE_VertexFormat::PosTex0[postex0DataList.size()];
    std::vector<SE_VertexFormat::PosTex0>::iterator it;
    int i;
    for(i = 0 , it = postex0DataList.begin() ; it != postex0DataList.end() ; it++, i++)
    {
        data[i] = *it;
    }

    unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];

    std::list<SE_Vector3i>::iterator itIndex;

    int j;
    for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
    {
        indexdata[j] = itIndex->x;
        indexdata[j + 1] = itIndex->y;
        indexdata[j + 2] = itIndex->z;       
    }

    vertexData = (float*)data;
    vertexDataNum = postex0DataList.size() * getVertexDataSize();
    indexData = indexdata;
    indexNum = indexDataList.size() * 3;
}

void SE_VertexBuffer::createVertexBuffer(int facenum,_Vector3i *faces,_Vector3f *vertexPos,_Vector3i *tfaces,_Vector2f *textureCoord0,_Vector2f *textureCoord1)
{
    //pos_tex0_tex1
    int facetNum = facenum;    

    _Vector3i* faceArray = faces;

    _Vector3f* vertexArray = vertexPos; 

    _Vector3i* texFaceArray = tfaces;

    _Vector2f* texVertexArray = textureCoord0;

    _Vector2f* texVertexArray1 = textureCoord1;

    
    std::list<SE_Vector3i> indexDataList;

    for(int i = 0 ; i < facetNum ; i++)
    {
        int faceIndex = i;

        _Vector3i posFace = faceArray[faceIndex];
        _Vector3i texFace = texFaceArray[faceIndex];

        SE_VertexFormat::PosTex0Tex1 v0, v1, v2;

        v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
        v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
        v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];
        v0.tex0.d[0] = texVertexArray[texFace.d[0]].d[0];
        v0.tex0.d[1] = texVertexArray[texFace.d[0]].d[1];
        v0.tex1.d[0] = texVertexArray1[texFace.d[0]].d[0];
        v0.tex1.d[1] = texVertexArray1[texFace.d[0]].d[1];
        int index0 = addVertexData(v0);

        v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
        v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
        v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];
        v1.tex0.d[0] = texVertexArray[texFace.d[1]].d[0];
        v1.tex0.d[1] = texVertexArray[texFace.d[1]].d[1];
        v1.tex1.d[0] = texVertexArray1[texFace.d[1]].d[0];
        v1.tex1.d[1] = texVertexArray1[texFace.d[1]].d[1];
        int index1 = addVertexData(v1);


        v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
        v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
        v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];
        v2.tex0.d[0] = texVertexArray[texFace.d[2]].d[0];
        v2.tex0.d[1] = texVertexArray[texFace.d[2]].d[1];
        v2.tex1.d[0] = texVertexArray1[texFace.d[2]].d[0];
        v2.tex1.d[1] = texVertexArray1[texFace.d[2]].d[1];
        int index2 = addVertexData(v2);
        indexDataList.push_back(SE_Vector3i(index0, index1, index2));
    }

    SE_VertexFormat::PosTex0Tex1* data = new SE_VertexFormat::PosTex0Tex1[postex0tex1DataList.size()];
    std::vector<SE_VertexFormat::PosTex0Tex1>::iterator it;
    int i;
    for(i = 0 , it = postex0tex1DataList.begin() ; it != postex0tex1DataList.end() ; it++, i++)
    {
        data[i] = *it;
    }

    unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];

    std::list<SE_Vector3i>::iterator itIndex;

    int j;
    for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
    {
        indexdata[j] = itIndex->x;
        indexdata[j + 1] = itIndex->y;
        indexdata[j + 2] = itIndex->z;       
    }

    vertexData = (float*)data;
    vertexDataNum = postex0tex1DataList.size() * getVertexDataSize();
    indexData = indexdata;
    indexNum = indexDataList.size() * 3;
}

void SE_VertexBuffer::createVertexBuffer(int facenum,_Vector3i *faces,_Vector3f *vertexPos,_Vector4f *color)
{
    //pos_color
    int facetNum = facenum;

    _Vector3i* faceArray = faces;

    _Vector3f* vertexArray = vertexPos;    

    _Vector4f* colorArray = color;



    //std::list<SE_VertexFormat::PosTex0> vertexDataList;
    std::list<SE_Vector3i> indexDataList;

    for(int i = 0 ; i < facetNum ; i++)
    {
        int faceIndex = i;

        _Vector3i posFace = faceArray[faceIndex];        

        SE_VertexFormat::PosColor v0, v1, v2;

        v0.pos.d[0] = vertexArray[posFace.d[0]].d[0];
        v0.pos.d[1] = vertexArray[posFace.d[0]].d[1];
        v0.pos.d[2] = vertexArray[posFace.d[0]].d[2];
        v0.color.d[0] = colorArray[posFace.d[0]].d[0];
        v0.color.d[1] = colorArray[posFace.d[0]].d[1];
        v0.color.d[2] = colorArray[posFace.d[0]].d[2];
        v0.color.d[3] = colorArray[posFace.d[0]].d[3];
        int index0 = addVertexData(v0);

        v1.pos.d[0] = vertexArray[posFace.d[1]].d[0];
        v1.pos.d[1] = vertexArray[posFace.d[1]].d[1];
        v1.pos.d[2] = vertexArray[posFace.d[1]].d[2];
        v1.color.d[0] = colorArray[posFace.d[1]].d[0];
        v1.color.d[1] = colorArray[posFace.d[1]].d[1];
        v1.color.d[2] = colorArray[posFace.d[1]].d[2];
        v1.color.d[3] = colorArray[posFace.d[1]].d[3];
        int index1 = addVertexData(v1);


        v2.pos.d[0] = vertexArray[posFace.d[2]].d[0];
        v2.pos.d[1] = vertexArray[posFace.d[2]].d[1];
        v2.pos.d[2] = vertexArray[posFace.d[2]].d[2];
        v2.color.d[0] = colorArray[posFace.d[2]].d[0];
        v2.color.d[1] = colorArray[posFace.d[2]].d[1];
        v2.color.d[2] = colorArray[posFace.d[2]].d[2];
        v2.color.d[3] = colorArray[posFace.d[2]].d[3];
        int index2 = addVertexData(v2);
        indexDataList.push_back(SE_Vector3i(index0, index1, index2));
    }
    SE_VertexFormat::PosColor* data = new SE_VertexFormat::PosColor[poscolorDataList.size()];
    std::vector<SE_VertexFormat::PosColor>::iterator it;
    int i;
    for(i = 0 , it = poscolorDataList.begin() ; it != poscolorDataList.end() ; it++, i++)
    {
        data[i] = *it;
    }

    unsigned short* indexdata = new unsigned short[indexDataList.size() * 3];

    std::list<SE_Vector3i>::iterator itIndex;

    int j;
    for(j = 0 , itIndex = indexDataList.begin() ; itIndex != indexDataList.end(); itIndex++, j += 3)
    {
        indexdata[j] = itIndex->x;
        indexdata[j + 1] = itIndex->y;
        indexdata[j + 2] = itIndex->z;       
    }

    vertexData = (float*)data;
    vertexDataNum = poscolorDataList.size() * getVertexDataSize();
    indexData = indexdata;
    indexNum = indexDataList.size() * 3;

}

void SE_VertexBuffer::initVBO()
{
    if(mVboHasInit || mDynamicDraw)
    {
        return;
    }
    glGenBuffers(1,&(mImpl->dataID));
    int datasize = this->vertexDataNum * sizeof(float);
    glBindBuffer(GL_ARRAY_BUFFER, mImpl->dataID);
    glBufferData(GL_ARRAY_BUFFER, datasize, this->vertexData, GL_STATIC_DRAW);    

    glGenBuffers(1,&(mImpl->indexID));
    int indexsize = this->indexNum * sizeof(unsigned short);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mImpl->indexID);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexsize, this->indexData, GL_STATIC_DRAW);
    

    glBindBuffer(GL_ARRAY_BUFFER, 0);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    mVboHasInit = true;
}

void SE_VertexBuffer::bindVBO()
{    
    if(!mVboHasInit)
    {
        return;
    }
    glBindBuffer(GL_ARRAY_BUFFER, mImpl->dataID);    
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mImpl->indexID);    

}

void SE_VertexBuffer::unBindVBO()
{
    if(!mVboHasInit)
    {
        return;
    }
    glBindBuffer(GL_ARRAY_BUFFER, 0);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}
void SE_VertexBuffer::releaseHardware()
{
    if(mVboHasInit)
    {
        unBindVBO();
        glDeleteBuffers(1, &mImpl->dataID);
	    glDeleteBuffers(1, &mImpl->indexID);
    }
    mVboHasInit = false;
}