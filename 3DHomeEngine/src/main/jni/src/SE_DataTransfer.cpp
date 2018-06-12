#include "SE_DynamicLibType.h"
#include "SE_DataTransfer.h"
#include "SE_Mesh.h"
#include "SE_Utils.h"
#include "SE_Buffer.h"
#include "SE_Common.h"
#include "SE_ResourceManager.h"
#include <string>
#include "SE_Log.h"
#include "SE_Application.h"
#include "SE_MemLeakDetector.h"
SE_MeshTransfer::~SE_MeshTransfer()
{
    if(mSurfaceTransferArray)
        delete[] mSurfaceTransferArray;
    if(mTexTransferArray)
        delete[] mTexTransferArray;
}
SE_Mesh* SE_MeshTransfer::createMesh(SE_ResourceManager* resourceManager)
{
    SE_Mesh* mesh = new SE_Mesh(mSurfaceNum, mTexNum);
    mesh->setGeometryData(resourceManager->getGeometryData(mGeomDataID));
  	mesh->setGeometryDataID(mGeomDataID);
    if(mSurfaceNum == 0)
    {
        mesh->setSurface(0, NULL); 
    }
    for(int i = 0 ; i < mTexNum ; i++)
    {
        SE_TextureTransfer* textureTransfer = &mTexTransferArray[i];
        SE_Texture* texture = new SE_Texture;
        texture->setTexUnitNum(textureTransfer->getTextureUnitNum());
        for(int j = 0 ; j < textureTransfer->getTextureUnitNum() ; j++)
        {
            SE_TextureUnitTransfer* texUnitTransfer = textureTransfer->getTextureUnit(j);
			SE_TextureUnit* texUnit = new SE_TextureUnit();
            texUnit->setTextureCoordData(resourceManager->getTextureCoordData(texUnitTransfer->getTexCoordDataID()));
			texUnit->setTextureCoordDataID(texUnitTransfer->getTexCoordDataID());
            SE_ImageDataID* imageDataArray = new SE_ImageDataID[texUnitTransfer->getImageDataNum()];
            for(int n = 0 ; n < texUnitTransfer->getImageDataNum() ; n++)
            {
                imageDataArray[n] = texUnitTransfer->getImageDataID(n);
                //SE_ImageDataID id = texUnitTransfer->getImageDataID(n);
				//SE_ImageData* imageData = resourceManager->getImageData(id);
                //imageDataArray[n] = *imageData;
            }
            //set uv tiling
            texUnit->setUTiling(texUnitTransfer->getUTiling());
            texUnit->setVTiling(texUnitTransfer->getVTiling());

            texUnit->setImageDataID(imageDataArray, texUnitTransfer->getImageDataNum());
            texture->setTextureUnit(texUnitTransfer->getType(), texUnit);
        }
        mesh->setTexture(i, texture);
    }
    for(int i = 0 ; i < mSurfaceNum; i++)
    {
        SE_SurfaceTransfer* surfaceTransfer = &mSurfaceTransferArray[i];
        SE_Surface* surface = new SE_Surface;
        surface->setMaterialData(resourceManager->getMaterialData(surfaceTransfer->getMaterialDataID()));
		surface->setMaterialDataID(surfaceTransfer->getMaterialDataID());
        surface->setProgramDataID(surfaceTransfer->getProgramDataID());
		surface->setRendererID(surfaceTransfer->getRendererID());
        int*  facetArray= new int[surfaceTransfer->getFacetNum()];
        memmove(facetArray, surfaceTransfer->getFacetArray(), sizeof(int) * surfaceTransfer->getFacetNum());
        surface->setGeometryData(mesh->getGeometryData());
        surface->setFacets(facetArray, surfaceTransfer->getFacetNum());
        int texIndex = surfaceTransfer->getTextureIndex();
        SE_Texture* texture = mesh->getTexture(texIndex);
        surface->setTexture(texture);   

        if(!texture)
        {
            surface->setTexCoordDataIdForFree(mJustForFreeTexDataID);
        }

        //set vertex buffer
        surface->setVertexBuffer(resourceManager->getVertexBuffer(surfaceTransfer->getVertexBufferID()));
		surface->setVertexBufferID(surfaceTransfer->getVertexBufferID());
        mesh->setSurface(i, surface); 
    }
    return mesh;
}
void SE_MeshTransfer::createFromMesh(SE_Mesh* mesh)
{
}
void SE_MeshTransfer::read(SE_BufferInput& inputBuffer)
{
    mGeomDataID.read(inputBuffer);

    //read vertex buffer id
    //mVbID.read(inputBuffer);

    int i, j;
#if 1
///////////////////add by liusong begin///////////////////////
    int keyFrameSize = inputBuffer.readInt();
////////////////////add by liusong end///////////////////////
#endif
    mColor = inputBuffer.readVector3f();
    mTexNum = inputBuffer.readInt();
    if(mTexNum > 0)
    {
        mTexTransferArray  = new SE_TextureTransfer[mTexNum];
    for(i = 0 ; i < mTexNum ; i++)
    {
        SE_TextureTransfer* textureTransfer = &mTexTransferArray[i];
        int texUnitNum = inputBuffer.readInt();
        SE_TextureUnitTransfer* texUnitTransfer = new SE_TextureUnitTransfer[texUnitNum];
        textureTransfer->setTextureUnitTransfer(texUnitTransfer, texUnitNum);
        for(int j = 0 ; j < texUnitNum ; j++)
        {
            int type = inputBuffer.readInt();
            SE_TextureCoordDataID texDataID;
            texDataID.read(inputBuffer);
            //for specular
            texUnitTransfer[j].setTextureCoordDataID(texDataID);
            int imageNum = inputBuffer.readInt();
            SE_ImageDataID* imageDataIDArray = new SE_ImageDataID[imageNum];
            for(int n = 0 ; n < imageNum ; n++)
            {
                std::string str = inputBuffer.readString();
                SE_ImageDataID imageID(str.c_str());
                imageDataIDArray[n] = imageID;
            }
            texUnitTransfer[j].setImageDataID(imageDataIDArray, imageNum);
            texUnitTransfer[j].setType(type);

                //stor uv tiling
                float utiling = inputBuffer.readFloat();
                float vtiling = inputBuffer.readFloat();
                texUnitTransfer[j].setUTiling(utiling);
                texUnitTransfer[j].setVTiling(vtiling);
        }
    } 
    }
    else
    {        
        mJustForFreeTexDataID.read(inputBuffer);        
    }
    
    mSurfaceNum = inputBuffer.readInt();
    mSurfaceTransferArray = new SE_SurfaceTransfer[mSurfaceNum];
    for(i = 0 ; i < mSurfaceNum ; i++)
    {
        SE_SurfaceTransfer* surfaceTransfer = &mSurfaceTransferArray[i];

        SE_VertexBufferID vbid;
        vbid.read(inputBuffer);
        surfaceTransfer->setVertexBufferID(vbid);


        SE_MaterialDataID materialID;
        materialID.read(inputBuffer);
        surfaceTransfer->setMaterialDataID(materialID);
        int facetNum = inputBuffer.readInt();
        int* facets = new int[facetNum];
        for(j = 0 ; j < facetNum ; j++)
        {
            facets[j] = inputBuffer.readInt();
        }
        surfaceTransfer->setFacets(facets, facetNum);
        SE_ProgramDataID pid;
        pid.read(inputBuffer);
        surfaceTransfer->setProgramDataID(pid);
		SE_RendererID renderid;
		renderid.read(inputBuffer);
		surfaceTransfer->setRendererID(renderid);
		int texIndex = inputBuffer.readInt();
        surfaceTransfer->setTextureIndex(texIndex);
    }

}
void SE_MeshTransfer::write(SE_BufferOutput& outBuffer)
{
    mGeomDataID.write(outBuffer);
    outBuffer.writeInt(mTexNum);
    if(mTexNum > 0)
    {
        for(int i = 0 ; i < mTexNum ; i++)
        {
            SE_TextureTransfer* textureTransfer = &mTexTransferArray[i];
            outBuffer.writeInt(textureTransfer->getTextureUnitNum());
            for(int j = 0 ; j < textureTransfer->getTextureUnitNum() ; j++)
            {
                SE_TextureUnitTransfer* texUnit = textureTransfer->getTextureUnit(j);
                outBuffer.writeInt(texUnit->getType());
                texUnit->getTexCoordDataID().write(outBuffer);
                outBuffer.writeInt(texUnit->getImageDataNum());
                for(int k = 0 ; k < texUnit->getImageDataNum() ; k++)
                {
                    texUnit->getImageDataID(k).write(outBuffer);
                }
            }
        }
    } 
    else
    {
        //write texture coordinate id?
    }
    outBuffer.writeInt(mSurfaceNum);
    SE_ASSERT(mSurfaceNum > 0);
    for(int i = 0 ; i < mSurfaceNum ; i++)
    {
        SE_SurfaceTransfer* surfaceTransfer = &mSurfaceTransferArray[i];
        outBuffer.writeInt(surfaceTransfer->getTextureIndex());
        surfaceTransfer->getMaterialDataID().write(outBuffer);
        outBuffer.writeInt(surfaceTransfer->getFacetNum());
        int* facetArray = surfaceTransfer->getFacetArray();
        for(int j = 0 ; j < surfaceTransfer->getFacetNum() ; j++)
        {
            outBuffer.writeInt(facetArray[i]);
        }
    }
}
