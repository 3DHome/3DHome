#include "SE_DynamicLibType.h"
#include<map>
#include <vector>
#include "SE_ResourceManager.h"
#include "SE_Utils.h"
#include "SE_Buffer.h"
#include "SE_ResFileHeader.h"
#include "SE_IO.h"
#include "SE_ImageData.h"
#include "SE_MaterialData.h"
#include "SE_GeometryData.h"
#include "SE_Geometry3D.h"
#include "SE_TextureCoordData.h"
#include "SE_DataTransfer.h"
#include "SE_Spatial.h"
#include "SE_ShaderProgram.h"
#include "SE_Primitive.h"
#include "SE_Log.h"
#include "SE_ImageCodec.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"
#include "SE_Bone.h"
#include "SE_Renderer.h"
#include "SE_CameraBestPosition.h"
#include "SE_Mesh.h"
#include "SE_SimObject.h"
#include "SE_SceneManager.h"
#include "SE_VertexBuffer.h"
#include "SE_File.h"
#include "SE_CommonNode.h"
#include "SE_Utils.h"
#ifdef ANDROID
#include "SE_AssetManager.h"
#endif

#include "SE_RenderSystemCapabilities.h"
#include "SE_ShaderDefine.h"
#include "SE_RenderDefine.h"
#include "SE_PluginsManager.h"
#include "SE_Plugin.h"
#include "SE_MemLeakDetector.h"
#include "SE_ObjectManager.h"
#include "tinyxml.h"
#include "SE_RenderTargetManager.h"

#define USE_EFFECT_CBF

SE_ImageData* SE_ResourceManager::loadCommonCompressImage(const char* imageName,int type)
{
#ifdef ANDROID
        return SE_ImageCodec::loadAsset(imageName,type);
#else 
    return SE_ImageCodec::load(imageName,type);   
#endif
}

SE_ImageData* SE_ResourceManager::loadRawImage(const char* imageName)
{
    char* data = NULL;
    int len = 0;
#ifdef ANDROID
    SE_Application::getInstance()->getAssetManager()->readAsset(imageName, data, len);
#else
    SE_IO::readFileAll(imageName, data, len);
#endif
    if(len == 0)
        return NULL;
    SE_ImageData* imageData = new SE_ImageData;
    if(!imageData)
        return NULL;
    int height, width, pixelFormat, bytesPerRow;
    SE_BufferInput inputBuffer(data, len);
    width = inputBuffer.readInt();
    height = inputBuffer.readInt();
    pixelFormat = inputBuffer.readInt();
    bytesPerRow = inputBuffer.readInt();
    int pixelDataLen = len - sizeof(int) * 4;
    char* pixelData = new char[pixelDataLen];
    if(!pixelData)
    {
        delete imageData;
        return NULL;
    }
    inputBuffer.readBytes(pixelData, pixelDataLen);
    imageData->setWidth(width);
    imageData->setHeight(height);
    imageData->setPixelFormat(pixelFormat);
    imageData->setBytesPerRow(bytesPerRow);
    imageData->setData(pixelData);
    imageData->setCompressType(SE_ImageData::RAW);
    return imageData;
}
SE_ImageData* SE_ResourceManager::loadImage(const char* imageName, int type)
{
    switch(type)
    {
    case SE_ImageData::RAW:
        return loadRawImage(imageName); 
        break;
    case SE_ImageData::JPEG:
    case SE_ImageData::PNG:
    case SE_ImageData::TGA:        
    case SE_ImageData::ETC_RGB_4BPP:        
    case SE_ImageData::OGL_PVRTC2:
        return loadCommonCompressImage(imageName,type);
        break;
    default:
        break;
    } 
    return NULL;
}
void SE_ResourceManager::processGeometryData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int geomDataNum = inputBuffer.readInt();
    for(int n = 0 ; n < geomDataNum ; n++)
    {
        SE_GeometryDataID geomDataID;
        geomDataID.read(inputBuffer);
        int vertexNum = inputBuffer.readInt();
        int faceNum = inputBuffer.readInt();

        int faceNormalNum = inputBuffer.readInt();
        int faceVertexNormalNum = inputBuffer.readInt();

        SE_Vector3f* vertexArray = new SE_Vector3f[vertexNum];
        SE_Vector3i* faceArray = new SE_Vector3i[faceNum];
        for(int i = 0 ; i < vertexNum ; i++)
        {
            vertexArray[i].x = inputBuffer.readFloat();
            vertexArray[i].y = inputBuffer.readFloat();
            vertexArray[i].z = inputBuffer.readFloat();
        }
        for(int i = 0 ; i < faceNum ; i++)
        {
            faceArray[i].d[0] = inputBuffer.readInt();
            faceArray[i].d[1] = inputBuffer.readInt();
            faceArray[i].d[2] = inputBuffer.readInt();
        }
        SE_Vector3f* faceNormalArray = NULL;
        if(faceNormalNum > 0)
        {
            faceNormalArray = new SE_Vector3f[faceNormalNum];
            for(int i = 0 ; i < faceNormalNum ; i++)
            {
                faceNormalArray[i].x = inputBuffer.readFloat();
                faceNormalArray[i].y = inputBuffer.readFloat();
                faceNormalArray[i].z = inputBuffer.readFloat();
            }
        }

        //facevertex is sort by face
        SE_Vector3f* faceVertexNormalArray = NULL;
        if(faceVertexNormalNum > 0)
        {
            faceVertexNormalArray = new SE_Vector3f[faceVertexNormalNum];
            for(int i = 0 ; i < faceVertexNormalNum ; i++)
            {
                faceVertexNormalArray[i].x = inputBuffer.readFloat();
                faceVertexNormalArray[i].y = inputBuffer.readFloat();
                faceVertexNormalArray[i].z = inputBuffer.readFloat();
                
            }
        }

        SE_GeometryData* geomData = new SE_GeometryData;
        geomData->setVertexArray(vertexArray, vertexNum);
        geomData->setFaceArray(faceArray, faceNum);
        geomData->setFaceNormalArray(faceNormalArray, faceNormalNum);
        geomData->setFaceVertexNormalArray(faceVertexNormalArray,faceVertexNormalNum);

        if(!resourceManager->setGeometryData(geomDataID, geomData))
        {
            delete geomData;
        }
    }
}
void SE_ResourceManager::processTextureCoordData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int texCoordNum = inputBuffer.readInt();
    for(int n = 0 ; n < texCoordNum ; n++)
    {
        SE_TextureCoordDataID texCoordID;
        texCoordID.read(inputBuffer);
        int texVertexNum = inputBuffer.readInt();
        int texFaceNum = inputBuffer.readInt();
        SE_Vector2f* texVertexArray = NULL;
        SE_Vector3i* texFaceArray = NULL;
        if(texVertexNum > 0)
        {
            texVertexArray = new SE_Vector2f[texVertexNum];
            texFaceArray = new SE_Vector3i[texFaceNum];
            for(int i = 0 ; i < texVertexNum ; i++)
            {
                texVertexArray[i].x = inputBuffer.readFloat();
                texVertexArray[i].y = inputBuffer.readFloat();
            }
            for(int i = 0 ; i  < texFaceNum ; i++)
            {
                texFaceArray[i].d[0] = inputBuffer.readInt();
                texFaceArray[i].d[1] = inputBuffer.readInt();
                texFaceArray[i].d[2] = inputBuffer.readInt();
            }
        }
        SE_TextureCoordData* texCoordData = new SE_TextureCoordData;
        texCoordData->setTexVertexArray(texVertexArray, texVertexNum);
        texCoordData->setTexFaceArray(texFaceArray, texFaceNum);

        if(!resourceManager->setTextureCoordData(texCoordID, texCoordData))
        {
            delete texCoordData;
        }
    }
}
void SE_ResourceManager::processMaterialData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int materialDataNum = inputBuffer.readInt();
    for(int n = 0 ; n < materialDataNum ; n++)
    {
        SE_MaterialDataID materialDataID;
        materialDataID.read(inputBuffer);
        SE_MaterialData* materialData = new SE_MaterialData;
        materialData->ambient = inputBuffer.readVector3f();
        materialData->diffuse = inputBuffer.readVector3f();
        materialData->specular = inputBuffer.readVector3f();
        materialData->shiny = inputBuffer.readFloat();
        //used for fast specular light
        materialData->shiny = 0.5;
        materialData->shinessStrength = 0.8;

        if(!resourceManager->setMaterialData(materialDataID, materialData))
        {
            delete materialData;
        }
    }

}
void SE_ResourceManager::processImageData(const char* imagePath, SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int imageDataNum = inputBuffer.readInt();
    for(int i = 0 ; i < imageDataNum ; i++)
    {
        SE_ImageDataID imageDataid;
        imageDataid.read(inputBuffer);
        int imageType = inputBuffer.readInt();
        std::string str = inputBuffer.readString();
#ifdef WIN32
        std::string dataPath;
#ifdef WIN32
        dataPath = resourceManager->getDataPath();
        dataPath += SE_SEP + str;
        resourceManager->setIdPath(imageDataid.getStr(),dataPath.c_str(),true);
#endif
        std::string path ;        
        const char* s = resourceManager->getIdPath(imageDataid.getStr());
        if(s)
        {
            path = s;
        }
        
        if(imagePath)
        {
          dataPath = imagePath;
        }
        else if(path == "/" || path.empty())
        {
            dataPath = resourceManager->getDataPath();
            dataPath += SE_SEP + str;
        }
        else
        {
            dataPath = path;
        }


        std::string imageDataPath;
        if(imagePath)
        {
            imageDataPath = dataPath + SE_SEP + str;
        }
        else
        {
            imageDataPath = dataPath;
        }
        if(!SE_Application::getInstance()->getRenderSystemCapabilities()->isSupportPowerVR())
        {
#if 0
            //Load jpg format or other primary format picture
            imageType = SE_ImageData::JPEG;
            size_t pos = imageDataPath.rfind(".");
            std::string tmp = imageDataPath.substr(0,pos);
            tmp = tmp + ".jpg";
            imageDataPath = tmp;
#endif

            //LOGI("Sorry! Your system is not support PowerVR texture compress,and you can not see any compressed picture!!\n");
        }

        SE_ImageData* existdata = this->getImageDataFromPath(imageDataPath.c_str());

        if(!existdata)
        {
        SE_ImageData* imageData = loadImage(imageDataPath.c_str(), imageType); 

        if(!imageData)
        {
            //Load other format or other primary format picture
            imageType = SE_ImageData::PNG;
            size_t pos = imageDataPath.rfind(".");
            std::string tmp = imageDataPath.substr(0,pos);
            tmp = tmp + ".png";
            imageData = loadImage(tmp.c_str(), imageType);  
        }

        if(!imageData)
        {
            //Load other format or other primary format picture
            imageType = SE_ImageData::JPEG;
            size_t pos = imageDataPath.rfind(".");
            std::string tmp = imageDataPath.substr(0,pos);
            tmp = tmp + ".jpg";
            imageData = loadImage(tmp.c_str(), imageType); 
        }

        if(!imageData)
        {
                if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("\nImage [%s] (.jpg and .png)is not found!!!!!\n\n",imageDataPath.c_str());
        }

        if (imageData)
        {
			imageData->setName(imageDataPath.c_str());

                //use path instead image        
                resourceManager->insertPathImageData(imageDataPath.c_str(), imageData);               
        }
        }
#endif
        
    }
}
void SE_ResourceManager::processRendererData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int rendererNum = inputBuffer.readInt();
    for(int i = 0 ; i < rendererNum ; i++)
    {
        std::string rendererID = inputBuffer.readString();
        std::string rendererClassName = inputBuffer.readString();

        //avoid same id add to map when load second cbf file to running app.
        if(resourceManager->getRenderer(rendererID.c_str()))
        {
            //Renderer has been loaded.
            continue;
        }
        else
        {
            SE_Renderer* renderer = (SE_Renderer*)SE_ObjectFactory::create(rendererClassName.c_str());
            if(renderer)
            {
                if(!resourceManager->setRenderer(rendererID.c_str(), renderer))
                {
                    delete renderer;
                }
            }
            else
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("... error renderer %s is not define\n", rendererClassName.c_str());
        }
            
            }
        }
    }
void SE_ResourceManager::processMeshData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int meshNum = inputBuffer.readInt();
    for(int i = 0 ; i < meshNum ; i++)
    {
        SE_MeshID meshID;
        meshID.read(inputBuffer);
        SE_MeshTransfer* meshTransfer = resourceManager->getMeshTransfer(meshID);
        if(meshTransfer)
        {
            if(SE_Application::getInstance()->SEHomeDebug)
LOGI("\n\nmeshTransfer[%s] have been in list!!!!!!!!\n\n",meshID.getStr());
            continue;
        }                   
        meshTransfer =  new SE_MeshTransfer;
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("## process mesh i = %d ##\n", i);
        if(meshTransfer)
        {
            meshTransfer->read(inputBuffer);
            if(!resourceManager->setMeshTransfer(meshID, meshTransfer))
            {
                delete meshTransfer;
            }
        }
    }

}
SE_Bone* SE_ResourceManager::findBone(const std::string& boneName, std::vector<SE_Bone*>& boneVector)
{
    std::vector<SE_Bone*>::iterator it;
    for(it = boneVector.begin(); it != boneVector.end() ; it++)
    {
        if((*it)->getName() == boneName)
            return *it;
    }
    return NULL;
}
void SE_ResourceManager::processSkinJointController(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int skinControllerNum = inputBuffer.readInt();
    for(int i = 0 ; i < skinControllerNum ; i++)
    {
        SE_SkinJointController* skinJointController = new SE_SkinJointController;
        int boneNum = inputBuffer.readInt();
        skinJointController->mBoneVector.resize(boneNum);
        for(int j = 0 ; j < boneNum ; j++)
        {
            SE_Bone* bone = new SE_Bone;
            skinJointController->mBoneVector[j] = bone;
            std::string boneName = inputBuffer.readString();
            bone->setName(boneName.c_str());
            int matrixnum = inputBuffer.readInt();
            float* mdata = NULL;
            if(matrixnum > 0)
            {
                mdata = new float[matrixnum * 16];
            }
            float* dst = mdata;
            for(int n = 0 ; n < matrixnum ; n++)
            {
                for(int k = 0 ; k < 16 ; k++)
                {
                    dst[k] = inputBuffer.readFloat();
                } 
                dst += 16;
            }
            if(matrixnum > 0)
            {
                bone->setMatrixArray(mdata, matrixnum);
            }
            float basedata[16];
            for(int n = 0 ; n < 16 ; n++)
            {
                basedata[n] = inputBuffer.readFloat();
            }
            SE_Matrix4f m(basedata);
            bone->setBaseMatrix(m);
        }
        for(int j = 0 ; j < boneNum ; j++)
        {
            SE_Bone* bone = skinJointController->mBoneVector[j];
            int childsize = inputBuffer.readInt();
            for(int n = 0 ; n < childsize ; n++)
            {
                std::string childname = inputBuffer.readString();
                SE_Bone* boneHasName = findBone(childname, skinJointController->mBoneVector);
                SE_ASSERT(boneHasName != NULL);
                bone->addChild(boneHasName);
                boneHasName->setParent(bone);
            }
        }
        std::string meshName = inputBuffer.readString();
        skinJointController->mMeshName = meshName;
        int vertexNum = inputBuffer.readInt();
        skinJointController->mVertexBoneWeightInfo.resize(vertexNum);
        for(int n = 0 ; n < vertexNum ; n++)
        {
            int boneWeightNum = inputBuffer.readInt();
            skinJointController->mVertexBoneWeightInfo[n].resize(boneWeightNum);
            for(int k = 0 ; k < boneWeightNum ; k++)
            {
                int boneIndex = inputBuffer.readInt();
                float weight = inputBuffer.readFloat();
                SE_BoneWeight bw(boneIndex, weight);
                skinJointController->mVertexBoneWeightInfo[n][k] = bw;
            }
        }
        SE_SkinJointControllerID skinJointControllerID = SE_ID::createSkinJointControllerID(meshName.c_str());
        if(!resourceManager->setSkinJointController(skinJointControllerID, skinJointController))
        {
            delete skinJointController;
        }
    }
}


SE_Biped* SE_ResourceManager::findBiped(const std::string& bipName, std::vector<SE_Biped*>& bipVector)
{
    std::vector<SE_Biped*>::iterator it;
    for(it = bipVector.begin(); it != bipVector.end() ; it++)
    {
        if((*it)->bipName == bipName)
            return *it;
    }
    return NULL;
}

void SE_ResourceManager::processBipedController(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{

    SE_SkeletonController * sk = resourceManager->getSkeletonController(SE_SKELETONCONTROLLER);

    if(!sk)
    {
        sk = new SE_SkeletonController();

        if(!resourceManager->setSkeletonController(SE_SKELETONCONTROLLER,sk))
        {
            delete sk;
        }

    }

    int bipControllerNum = inputBuffer.readInt();
    for(int i = 0 ; i < bipControllerNum ; i++)
    {
        SE_BipedController* bipedController = new SE_BipedController;

        std::string id = inputBuffer.readString();
        std::string controllerid(id.c_str());
        bipedController->mBipedControllerId = controllerid;


        int oneBipAnimationNum = inputBuffer.readInt();
        for(int j = 0; j < oneBipAnimationNum; ++j)
        {
            SE_Biped * bip = new SE_Biped();
            std::string bipName = inputBuffer.readString();
            bip->bipName = bipName;

            float basedata[16];
            for(int n = 0 ; n < 16 ; n++)
            {
                basedata[n] = inputBuffer.readFloat();
            }

            SE_Matrix4f m(basedata);
            bip->bind_pose = m;            
            int frameNum = inputBuffer.readInt();

            for(int f = 0; f < frameNum; ++f)
            {
                SE_BipedKeyFrame * oneFrame = new SE_BipedKeyFrame();

                oneFrame->frameIndex = inputBuffer.readInt();

                oneFrame->rotateQ.x = inputBuffer.readFloat();
                oneFrame->rotateQ.y = inputBuffer.readFloat();
                oneFrame->rotateQ.z = inputBuffer.readFloat();
                oneFrame->rotateQ.w = inputBuffer.readFloat();

                oneFrame->translate.x = inputBuffer.readFloat();
                oneFrame->translate.y = inputBuffer.readFloat();
                oneFrame->translate.z = inputBuffer.readFloat();

                oneFrame->scale.x = inputBuffer.readFloat();
                oneFrame->scale.y = inputBuffer.readFloat();
                oneFrame->scale.z = inputBuffer.readFloat();


                bip->animationInfo.push_back(oneFrame);

            }

            bipedController->oneBipAnimation.push_back(bip);
        }

        for(int k = 0 ; k < bipedController->oneBipAnimation.size() ; ++k)
        {
            int childNum = inputBuffer.readInt();
            for(int child = 0; child < childNum; ++child)
            {
                std::string childName = inputBuffer.readString();
                SE_Biped *childbip = findBiped(childName,bipedController->oneBipAnimation);
                childbip->parent = bipedController->oneBipAnimation[k];
                bipedController->oneBipAnimation[k]->children.push_back(childbip);
            }
        }

        int suNum = inputBuffer.readInt();
        for(int s = 0; s < suNum; ++s)
        {
            SE_SkeletonUnit *su = new SE_SkeletonUnit();
            int bipNum = inputBuffer.readInt();
            su->bipedNum = bipNum;
            su->objHasBiped = inputBuffer.readString();
            su->controllerId = inputBuffer.readString();

            for(int bipnameNum = 0; bipnameNum < bipNum; ++bipnameNum)
            {
                std::string bipname = inputBuffer.readString();
                su->bipedNamesOnObj.push_back(bipname); 
            }

            int vertexNum = inputBuffer.readInt();
            su->vertexNum = vertexNum;
            for(int vNum = 0; vNum < vertexNum; ++vNum)
            {
                SE_BipedWeight * w = new SE_BipedWeight();

                int indexNum = inputBuffer.readInt();

                if(indexNum > su->mBipNumPerVertext_max)
                {
                    su->mBipNumPerVertext_max = indexNum;
                }

                for(int iNum = 0; iNum < indexNum; ++iNum)
                {
                    int index = inputBuffer.readInt();
                    float weight = inputBuffer.readFloat();

                    w->bipedIndex.push_back(index);
                    w->weight.push_back(weight);
                }
                //push weight to vector
                su->objVertexBlendInfo.push_back(w);
            }
            //push su to vector
            bipedController->bipAndObjInfo.push_back(su);
        }

        //generate skeleton animation data-source
        //Move to scene after scene loaded.
        //bipedController->initSkeletonAnimation();
        
        sk->mSkeletonController.push_back(bipedController);

    }
    
}

void SE_ResourceManager::processShaderProgram(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{

    int spNum = inputBuffer.readInt();
    for(int i = 0 ; i < spNum ; i++)
    {
        SE_ProgramDataID programDataID;
        programDataID.read(inputBuffer);
        std::string shaderClassName = inputBuffer.readString();
        int vertexShaderLen = inputBuffer.readInt();
        int fragmentShaderLen = inputBuffer.readInt();
        char* vertexShaderBytes = new char[vertexShaderLen + 1];
        char* fragmentShaderBytes = new char[fragmentShaderLen + 1];
        inputBuffer.readBytes(vertexShaderBytes, vertexShaderLen);
        inputBuffer.readBytes(fragmentShaderBytes, fragmentShaderLen);
        vertexShaderBytes[vertexShaderLen] = '\0';
        fragmentShaderBytes[fragmentShaderLen] = '\0';

        //avoid same id add to map when load second cbf file to running app.
        /*
        if(!resourceManager->hasLoadShaderProgram(programDataID))
        {
            SE_ShaderProgram* shaderProgram = (SE_ShaderProgram*)SE_ObjectFactory::create(shaderClassName.c_str());
            if(shaderProgram)
            {
                if(!resourceManager->setShaderProgram(programDataID, shaderProgram,vertexShaderBytes, fragmentShaderBytes))
                {
                    delete shaderProgram;
                }

            }
            else
            {
                if(SE_Application::getInstance()->SEHomeDebug)
                LOGI("Error!! Wrong type shader!!\n\n");
            }
        }
        */
        delete[] vertexShaderBytes;
        delete[] fragmentShaderBytes;
    }
}

void SE_ResourceManager::processCameraData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    SE_CameraPositionList * cp = resourceManager->getCameraPositionList(SE_CAMERABESTPOSITION);

    if(!cp)
    {
        cp = new SE_CameraPositionList();

        if(!resourceManager->setCameraPositionList(SE_CAMERABESTPOSITION,cp))
        {
            delete cp;
        }

    }

    int cpNum = inputBuffer.readInt();
    for(int i = 0; i < cpNum; ++i)
    {
        SE_CameraBestPosition *pos = new SE_CameraBestPosition();
        pos->mCamraName = inputBuffer.readString();
        pos->mCameraType = inputBuffer.readString();
        pos->mCameraTargetName = inputBuffer.readString();

        pos->mCameraPos.x = inputBuffer.readFloat();
        pos->mCameraPos.y = inputBuffer.readFloat();
        pos->mCameraPos.z = inputBuffer.readFloat();

        pos->mCameraTargetPos.x = inputBuffer.readFloat();
        pos->mCameraTargetPos.y = inputBuffer.readFloat();
        pos->mCameraTargetPos.z = inputBuffer.readFloat();

        cp->mPositions.push_back(pos);

    }
}

void SE_ResourceManager::processPrimitive(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    /*
    int primitiveNum = inputBuffer.readInt();
    for(int i = 0 ; i < primitiveNum ; i++)
    {
        std::string primitiveName = inputBuffer.readString();
        SE_Primitive* primitive = SE_ObjectFactory::create(primitiveName.c_str());
        primitive->read(inputBuffer);
    }
    */
}

void SE_ResourceManager::processVertexBufferData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int geoNum = inputBuffer.readInt();
    for(int i = 0; i < geoNum; ++i)
    {
    int vbNum = inputBuffer.readInt();
        for(int j = 0; j < vbNum; ++j)
    {
        SE_VertexBufferID vbID;
        vbID.read(inputBuffer);
        SE_VertexBuffer *vb = new SE_VertexBuffer();

        unsigned int state = inputBuffer.readInt();
        vb->setVBState(state);

        int vertexdataNum = inputBuffer.readInt();
        float * vertexdata = new float[vertexdataNum];
        for(int i = 0; i < vertexdataNum; ++i)
        {
            vertexdata[i] = inputBuffer.readFloat();
        }
        vb->vertexData = vertexdata;
        vb->vertexDataNum = vertexdataNum;

        int indexdataNum = inputBuffer.readInt();
        unsigned short *indexdata = new unsigned short[indexdataNum];
        for(int i = 0; i < indexdataNum; ++i)
        {
            indexdata[i] = inputBuffer.readInt();
        }

        vb->indexData = indexdata;
        vb->indexNum = indexdataNum;

        if(!resourceManager->setVertexBuffer(vbID,vb))
        {
            delete vb;
        }
    }
}
}

void SE_ResourceManager::processAnimationMeshListData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    SE_GeometryDataList* list = new SE_GeometryDataList();
    SE_GeometryDataID geomDataID;

    int meshListCount = inputBuffer.readInt();
    for(int n = 0 ; n < meshListCount ; n++)
    {
        
        geomDataID.read(inputBuffer);
        int vertexNum = inputBuffer.readInt();
        int faceNum = inputBuffer.readInt();

        int faceNormalNum = inputBuffer.readInt();
        int faceVertexNormalNum = inputBuffer.readInt();

        int timeValue = inputBuffer.readInt();

        SE_Vector3f* vertexArray = new SE_Vector3f[vertexNum];
        SE_Vector3i* faceArray = new SE_Vector3i[faceNum];
        for(int i = 0 ; i < vertexNum ; i++)
        {
            vertexArray[i].x = inputBuffer.readFloat();
            vertexArray[i].y = inputBuffer.readFloat();
            vertexArray[i].z = inputBuffer.readFloat();
        }
        for(int i = 0 ; i < faceNum ; i++)
        {
            faceArray[i].d[0] = inputBuffer.readInt();
            faceArray[i].d[1] = inputBuffer.readInt();
            faceArray[i].d[2] = inputBuffer.readInt();
        }
        SE_Vector3f* faceNormalArray = NULL;
        if(faceNormalNum > 0)
        {
            faceNormalArray = new SE_Vector3f[faceNormalNum];
            for(int i = 0 ; i < faceNormalNum ; i++)
            {
                faceNormalArray[i].x = inputBuffer.readFloat();
                faceNormalArray[i].y = inputBuffer.readFloat();
                faceNormalArray[i].z = inputBuffer.readFloat();
            }
        }

        //facevertex is sort by face
        SE_Vector3f* faceVertexNormalArray = NULL;
        if(faceVertexNormalNum > 0)
        {
            faceVertexNormalArray = new SE_Vector3f[faceVertexNormalNum];
            for(int i = 0 ; i < faceVertexNormalNum ; i++)
            {
                faceVertexNormalArray[i].x = inputBuffer.readFloat();
                faceVertexNormalArray[i].y = inputBuffer.readFloat();
                faceVertexNormalArray[i].z = inputBuffer.readFloat();
                
            }
        }

        SE_GeometryData* geomData = new SE_GeometryData;
        geomData->setVertexArray(vertexArray, vertexNum);
        geomData->setFaceArray(faceArray, faceNum);
        geomData->setFaceNormalArray(faceNormalArray, faceNormalNum);
        geomData->setFaceVertexNormalArray(faceVertexNormalArray,faceVertexNormalNum);
        
        list->mGeoDataList.push_back(geomData);        
    }

    if(!resourceManager->setAnimationMeshList(geomDataID, list))
    {
        delete list;      
    }
}
void SE_ResourceManager::processMountPointData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    int mountPointNum = inputBuffer.readInt();
    std::string groupName;
    _Impl::MountPointArray mparray;
    for(int i = 0; i < mountPointNum;++i)
    {
        SE_MountPoint* mp = new SE_MountPoint();

        mp->mGroupName = inputBuffer.readString();
        groupName = mp->mGroupName;
        mp->mountPointName = inputBuffer.readString();

        mp->translate.x = inputBuffer.readFloat();
        mp->translate.y = inputBuffer.readFloat();
        mp->translate.z = inputBuffer.readFloat();

        mp->rotateAxis.x = inputBuffer.readFloat();
        mp->rotateAxis.y = inputBuffer.readFloat();
        mp->rotateAxis.z = inputBuffer.readFloat();
        mp->rotateAngle = inputBuffer.readFloat();

        mp->scale.x = inputBuffer.readFloat();
        mp->scale.y = inputBuffer.readFloat();
        mp->scale.z = inputBuffer.readFloat();

        mparray.push_back(mp);
    }
    mImpl->mMountPointMap.insert(std::pair<std::string,_Impl::MountPointArray>(groupName,mparray));   

}
void SE_ResourceManager::process(const char* imagePath, SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager)
{
    if(!resourceManager->checkHeader(inputBuffer))
        return;
    while(inputBuffer.hasMore())
    {
        short id = inputBuffer.readShort();
        switch(id)
        {
            case SE_GEOMETRYDATA_ID:
                processGeometryData(inputBuffer, resourceManager);
                break;
            case SE_TEXCOORDDATA_ID:
                processTextureCoordData(inputBuffer, resourceManager);
                break;
            case SE_MATERIALDATA_ID:
                processMaterialData(inputBuffer, resourceManager);
                break;
            case SE_IMAGEDATA_ID:
                processImageData(imagePath, inputBuffer, resourceManager);
                break;
            case SE_MESHDATA_ID:
                processMeshData(inputBuffer, resourceManager);
                break;
            case SE_SHADERPROGRAMDATA_ID:
                processShaderProgram(inputBuffer, resourceManager);
                break;
            case SE_PRIMITIVEDATA_ID:
                processPrimitive(inputBuffer, resourceManager);
                break;
            case SE_SKINJOINTCONTROLLER_ID:
                processSkinJointController(inputBuffer, resourceManager);
            case SE_BIPEDCONTROLLER_ID:
                processBipedController(inputBuffer, resourceManager);
                break;
            case SE_RENDERERINFO_ID:
                processRendererData(inputBuffer, resourceManager);
                break;
            case SE_CAMERADATA_ID:
                processCameraData(inputBuffer, resourceManager);
                break;
            case SE_VERTEXBUFFER_ID:
                processVertexBufferData(inputBuffer,resourceManager);
                break;
            case SE_MESHANIMATION_ID:                
                processAnimationMeshListData(inputBuffer,resourceManager);
                break;
            case SE_MOUNTPOINT_ID:                
                processMountPointData(inputBuffer,resourceManager);
                break;
        }
    }
}
///////////////////////////////////////


/////////////////////////////////////////
SE_ResourceManager::_Impl::~_Impl()
{    
    geomDataMap.getMap()->clear();
    imageDataMap.getMap()->clear();
    texCoordDataMap.getMap()->clear();
    materialDataMap.getMap()->clear();
    meshMap.getMap()->clear();
    shaderMap.getMap()->clear();
    primitiveMap.getMap()->clear();
    skinJointControllerMap.getMap()->clear();
    skeletonControllerMap.getMap()->clear();
    cameraPositionListMap.getMap()->clear();
    rendererMap.getMap()->clear();
    vertexBufferMap.getMap()->clear();
    animationMeshListMap.getMap()->clear();
    objectMeshMap.getMap()->clear();

    imageIdPathMap.clear();

}
void SE_ResourceManager::_Impl::clear()
{
    geomDataMap.getMap()->clear();
    imageDataMap.getMap()->clear();
    texCoordDataMap.getMap()->clear();
    materialDataMap.getMap()->clear();
    meshMap.getMap()->clear();
    shaderMap.getMap()->clear();
    primitiveMap.getMap()->clear();
    skinJointControllerMap.getMap()->clear();
    skeletonControllerMap.getMap()->clear();
    cameraPositionListMap.getMap()->clear();
    rendererMap.getMap()->clear();
    vertexBufferMap.getMap()->clear();
    animationMeshListMap.getMap()->clear();
    objectMeshMap.getMap()->clear();

    imageIdPathMap.clear();
    mMountPointMap.clear();
}
void SE_ResourceManager::_Impl::process_file(const char* imagePath, SE_BufferInput& inputBuffer)
{
    this->resourceManager->process(imagePath, inputBuffer, this->resourceManager);
}
//////////////////////
SE_ResourceManager::SE_ResourceManager()
{
    mImpl = new SE_ResourceManager::_Impl;
    mImpl->resourceManager = this;
}
SE_ResourceManager::SE_ResourceManager(const char* dataPath)
{
    mImpl = new SE_ResourceManager::_Impl;
    mImpl->dataPath = dataPath;
    mImpl->resourceManager = this;
}
SE_ResourceManager::~SE_ResourceManager()
{
    unLoadScene();
    if(mImpl)
        delete mImpl;
}

SE_GeometryData* SE_ResourceManager::getGeometryData(const SE_GeometryDataID& geomID)
{
    return mImpl->geomDataMap.get(geomID);

}
bool SE_ResourceManager::setGeometryData(const SE_GeometryDataID& geomID, SE_GeometryData* data)
{
    return mImpl->geomDataMap.set(geomID, data);

}
void SE_ResourceManager::removeGeometryData(const SE_GeometryDataID& geomID)
{
    mImpl->geomDataMap.remove(geomID);
} 
SE_Primitive* SE_ResourceManager::getPrimitive(const SE_PrimitiveID& primitiveID)
{
    return mImpl->primitiveMap.get(primitiveID);
}
bool SE_ResourceManager::setPrimitive(const SE_PrimitiveID& primitiveID , SE_Primitive* primitive)
{
    return mImpl->primitiveMap.set(primitiveID, primitive);
}
void SE_ResourceManager::removePrimitive(const SE_PrimitiveID& primitiveID)
{
    mImpl->primitiveMap.remove(primitiveID);
}
SE_TextureCoordData* SE_ResourceManager::getTextureCoordData(const SE_TextureCoordDataID& texCoordID)
{
    return mImpl->texCoordDataMap.get(texCoordID);
    /*
     SE_ResourceManager::_Impl::_TextureCoordDataMap::iterator it = mImpl->texUnitDataMap.find(texID);
    if(it == mImpl->texUnitDataMap.end())
        return NULL;
    else
        return it->second;
        */
}
bool SE_ResourceManager::setTextureCoordData(const SE_TextureCoordDataID& texCoordID, SE_TextureCoordData* data)
{
    return mImpl->texCoordDataMap.set(texCoordID, data);
}
void SE_ResourceManager::removeTextureCoordData(const SE_TextureCoordDataID& texCoordID)
{
    mImpl->texCoordDataMap.remove(texCoordID);
}
    
//image data
SE_ImageData* SE_ResourceManager::getImageData(const SE_ImageDataID& imageID)
{
    const char* path = getIdPath(imageID.getStr());
    if(path)
    {
        std::string pathid = path;
        SE_ImageDataID keypath = pathid.c_str();
        return mImpl->imageDataMap.get(keypath);
    }
    return NULL;
}

SE_ImageData* SE_ResourceManager::getImageDataFromPath(const char* imagePath)
{
    SE_ImageDataID keypath = imagePath;
    return mImpl->imageDataMap.get(keypath);
}
bool SE_ResourceManager::setImageData(const SE_ImageDataID& imageID,const char* pathID, SE_ImageData* data,bool replace)
    {
    //found path
    const char* path  = this->getIdPath(imageID.getStr());

    if(!path)
    {
        //path not in map
        this->setIdPath(imageID.getStr(),pathID);

        SE_ImageDataID pathkey = pathID;
        return mImpl->imageDataMap.set(pathkey, data,replace);
        }
    SE_ImageDataID pathkey = pathID;
    return mImpl->imageDataMap.set(path, data,replace);    
    }
bool SE_ResourceManager::insertPathImageData(const char* pathID,SE_ImageData* data,bool replace)
{
    SE_ImageDataID pathkey = pathID;
    return mImpl->imageDataMap.set(pathkey, data,replace);    
}
void SE_ResourceManager::removeImageData(const SE_ImageDataID& imageID)
{
    //can not remove directly
    mImpl->imageDataMap.remove(imageID);
}

bool SE_ResourceManager::replaceImageData(const SE_ImageDataID& imageID,const char* pathID, SE_ImageData* data)
{

    //the id is legal
    return setImageData(imageID,pathID,data);//"set" will replace the data
}
//material data
SE_MaterialData* SE_ResourceManager::getMaterialData(const SE_MaterialDataID& materialID)
{
    return mImpl->materialDataMap.get(materialID);
}
bool SE_ResourceManager::setMaterialData(const SE_MaterialDataID& materialID, SE_MaterialData* data)
{
    return mImpl->materialDataMap.set(materialID, data);
}
void SE_ResourceManager::removeMaterialData(const SE_MaterialDataID& materialID)
{
    mImpl->materialDataMap.remove(materialID);
}
//camera position
SE_CameraPositionList* SE_ResourceManager::getCameraPositionList(const SE_CameraPositionID& imageID)
{
    return mImpl->cameraPositionListMap.get(imageID);
}
bool SE_ResourceManager::setCameraPositionList(const SE_CameraPositionID& imageID, SE_CameraPositionList* data)
{
    return mImpl->cameraPositionListMap.set(imageID, data);
}
void SE_ResourceManager::removeCameraPositionList(const SE_CameraPositionID& imageID)
{
    mImpl->cameraPositionListMap.remove(imageID);
}
//vertex buffer
SE_VertexBuffer* SE_ResourceManager::getVertexBuffer(const SE_VertexBufferID& vbID)
{
    return mImpl->vertexBufferMap.get(vbID);
}
bool SE_ResourceManager::setVertexBuffer(const SE_VertexBufferID& vbID, SE_VertexBuffer* data)
{
    return mImpl->vertexBufferMap.set(vbID, data);
}
void SE_ResourceManager::removeVertexBuffer(const SE_VertexBufferID& vbID)
{
    mImpl->vertexBufferMap.remove(vbID);
}

//mesh animation list
SE_GeometryDataList* SE_ResourceManager::getAnimationMeshList(const SE_GeometryDataID& goID)
{
    return mImpl->animationMeshListMap.get(goID) ;
}
bool SE_ResourceManager::setAnimationMeshList(const SE_GeometryDataID& goID, SE_GeometryDataList* data)
{
    return mImpl->animationMeshListMap.set(goID, data) ;
}
void SE_ResourceManager::removeAnimationMeshList(const SE_GeometryDataID& goID)
{
    mImpl->animationMeshListMap.remove(goID);
}

//object mesh 
SE_Mesh* SE_ResourceManager::getObjectMesh(const SE_MeshID& mID)
{
    return mImpl->objectMeshMap.get(mID) ;
}
bool SE_ResourceManager::setObjectMesh(const SE_MeshID& mID, SE_Mesh* data)
{
    return mImpl->objectMeshMap.set(mID, data) ;
}
void SE_ResourceManager::removeObjectMesh(const SE_MeshID& mID)
{
    mImpl->objectMeshMap.remove(mID);
}
//
const char* SE_ResourceManager::getIdPath(const char* imageid)
{
    _Impl::ImageIdPathMap::iterator it;
    std::string  id = imageid;
    it = mImpl->imageIdPathMap.find(id);

    if(it != mImpl->imageIdPathMap.end())
    {
        return it->second.c_str();
    }
    return NULL;
}
bool SE_ResourceManager::setIdPath(const char* imageid, const char* imagePath,bool replace)
{
    _Impl::ImageIdPathMap::iterator it;
    std::string  id = imageid;
    it = mImpl->imageIdPathMap.find(id);

    if(it != mImpl->imageIdPathMap.end())
    {
        if(!replace)
        {
        return false;
    }
        it->second = imagePath;
        return true;
    }
    std::string path = imagePath;
    mImpl->imageIdPathMap.insert(std::pair<std::string,std::string>(id,path));
    return true;

}
void SE_ResourceManager::clearIdPath()
{
    mImpl->imageIdPathMap.clear();
}
void SE_ResourceManager::removeIdPath(const char* imageid)
{
    _Impl::ImageIdPathMap::iterator it;
    std::string id = imageid;
    it = mImpl->imageIdPathMap.find(id);
    std::string path;
    if(it != mImpl->imageIdPathMap.end())
    {
        path = it->second;
        mImpl->imageIdPathMap.erase(it);
        if(SE_Application::getInstance()->SEHomeDebug)
        {
            LOGI("3DHome ######## removeIdPath[%s] success!!!! path is %s\n",imageid,path.c_str());
        }
    }
}

void SE_ResourceManager::loadBaseData(const char* imagePath, const char* baseResourceName,const char* xmlName)
{    

#ifdef ANDROID
    std::string resourcePath = baseResourceName;    
#else
    std::string resourcePath = mImpl->dataPath + SE_SEP + baseResourceName + "_basedata.cbf";
    if(xmlName)
    {
        std::string xmlpath = mImpl->dataPath + SE_SEP + baseResourceName + xmlName + ".xml";
        inflate(xmlpath.c_str());
    }
 #endif

    char* data = NULL;
    int len = 0;
#ifndef ANDROID
    SE_IO::readFileAll(resourcePath.c_str(), data, len);
#else
     SE_Application::getInstance()->getAssetManager()->readAsset(resourcePath.c_str(), data, len);
#endif
    if(data)
    {
        SE_BufferInput inputBuffer(data, len);
        mImpl->process_file(imagePath, inputBuffer);
    }
}
bool SE_ResourceManager::checkHeader(SE_BufferInput& inputBuffer)
{
    int magic = inputBuffer.readInt();
    if(magic != SE_MAGIC)
        return false;
    int version = inputBuffer.readInt();
    if(version != SE_VERSION)
        return false;
    int endian = inputBuffer.readInt();
    int dataLen = inputBuffer.readInt();
    if(dataLen != (inputBuffer.getDataLen() - 16))
        return false;
    return true;
}
static SE_Spatial* createSpatial(std::string& spatialType, SE_Spatial* parent)
{
    SE_Spatial* spatial = (SE_Spatial*)SE_ObjectFactory::create(spatialType.c_str());
    spatial->setParent(parent);
    if(parent)
        parent->addChild(spatial);
    return spatial;
}
SE_Spatial* SE_ResourceManager::createSceneNode(SE_BufferInput& inputBuffer, SE_Spatial* parent,SE_BufferInput* effectBuffer)
{
    std::string spatialType = inputBuffer.readString();
    int childNum = inputBuffer.readInt();
    SE_Spatial* spatial = createSpatial(spatialType, parent);
    spatial->read(inputBuffer);

#ifdef USE_EFFECT_CBF
    if(effectBuffer)
    {
        //load effect 
        spatial->readEffect(*effectBuffer);
    }

#else
    std::string spatialname = spatial->getSpatialName();
    SE_SpatialEffectData* spdata = getGeometryAttribute(spatialname.c_str());

    if(spdata != NULL)
    {          
        spatial->setSpatialEffectData(spdata);
    }
#endif
    for(int i = 0 ; i < childNum ; i++)
    {
        createSceneNode(inputBuffer, spatial,effectBuffer);
    }
    return spatial;
}
SE_ShaderProgram* SE_ResourceManager::getShaderProgram(const SE_ProgramDataID& programDataID)
{
    SE_ShaderProgram* shader = mImpl->shaderMap.get(programDataID);
    if(shader && shader->initOK())
        return shader;
    if(shader)
        shader->recreate();
    return shader;
}
bool SE_ResourceManager::setShaderProgram(const SE_ProgramDataID& programDataID, SE_ShaderProgram* shaderProgram, char* vertexShader, char* fragmentShader)
{
    if(vertexShader == NULL || fragmentShader == NULL)
    {
        return false;
    }
    //SE_ShaderProgram* shaderProgram = (SE_ShaderProgram*)SE_ObjectFactory::create(shaderClassName);
    //shaderProgram->create(vertexShader, fragmentShader);
    shaderProgram->setSource(vertexShader, fragmentShader);
    return mImpl->shaderMap.set(programDataID, shaderProgram);

}

bool SE_ResourceManager::insertShaderProgram(const SE_ProgramDataID& programDataID, SE_ShaderProgram* shaderProgram)
{    
    return mImpl->shaderMap.set(programDataID, shaderProgram);
}
void SE_ResourceManager::removeShaderProgram(const SE_ProgramDataID& programDataID)
{
    mImpl->shaderMap.remove(programDataID);
}

bool SE_ResourceManager::hasLoadShaderProgram(const SE_ProgramDataID& programDataID)
{
    SE_ShaderProgram* shader = mImpl->shaderMap.get(programDataID);
    if(!shader)
    {
        return false;
    }
    return true;
}
SE_Renderer* SE_ResourceManager::getRenderer(const SE_RendererID& rendererID)
{
    return mImpl->rendererMap.get(rendererID);
}
bool SE_ResourceManager::setRenderer(const SE_RendererID& rendererID, SE_Renderer* renderer)
{
    return mImpl->rendererMap.set(rendererID, renderer);
}
void SE_ResourceManager::removeRenderer(const SE_RendererID& rendererID)
{
    mImpl->rendererMap.remove(rendererID);
}
    
const char* SE_ResourceManager::getDataPath()
{
    return mImpl->dataPath.c_str();
}
void SE_ResourceManager::setDataPath(const char* datapath)
{
    mImpl->dataPath = datapath;
}

SE_SpatialEffectData* SE_ResourceManager::getGeometryAttribute(const char* spatialName)
{
    std::string name = spatialName;
    std::map<std::string,SE_SpatialEffectData*>::iterator it;
    it = mSpatialDataMap.find(name);

    if(it != mSpatialDataMap.end())
    {
        return it->second;
    }
    return NULL;
}

bool SE_ResourceManager::inserGeometryAttribute(const char* spatialName, const char* spatialAttribute)
{
    std::string name = spatialName;
    std::string attr = spatialAttribute;

    std::map<std::string,std::string>::iterator it;
    it = mEffectmap.find(name);

    if(it != mEffectmap.end())
    {
        return false;
    }

    mEffectmap.insert(std::pair<std::string,std::string>(name,attr));
    return true;
}

void SE_ResourceManager::clearGeometryAttributeMap()
{
    std::map<std::string,SE_SpatialEffectData*>::iterator it = mSpatialDataMap.begin();
    for(;it != mSpatialDataMap.end();it++)
    {
        delete it->second;
    }
    mSpatialDataMap.clear();
}


SE_Spatial* SE_ResourceManager::loadScene(const char* sceneName, SE_Spatial* externalSpatial)
{
#ifdef ANDROID
    std::string scenePath = sceneName;
#else
    std::string scenePath = mImpl->dataPath + SE_SEP + sceneName + "_scene.cbf";
#endif

    std::string effectPath = scenePath;

#ifdef USE_EFFECT_CBF
    if(!SE_Util::replace_char(effectPath,"_scene.cbf","_effect.cbf"))
    {
        effectPath.clear();
    }

    char* effectdata = NULL;
    int effectlen = 0;    
#else
    std::string effectXMLPath = scenePath;
    if(!SE_Util::replace_char(effectXMLPath,"_scene.cbf","_effect.xml"))
    {
        effectXMLPath.clear();
    }  

    if(mSpatialDataMap.size() == 0)
    {
        bool result = loadEffectXML(effectXMLPath.c_str(),mSpatialDataMap);
        if(!result)
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("\n\n loadEffectXML[%s] fail!!!\n",effectXMLPath.c_str());
            }
        }
    }
#endif

    char* data = NULL;
    int len = 0;

    
#ifndef ANDROID

    SE_IO::readFileAll(scenePath.c_str(), data, len);
#ifdef USE_EFFECT_CBF
    SE_IO::readFileAll(effectPath.c_str(), effectdata, effectlen);    
#endif
#else
    SE_Application::getInstance()->getAssetManager()->readAsset(scenePath.c_str(), data, len);
#ifdef USE_EFFECT_CBF
    SE_Application::getInstance()->getAssetManager()->readAsset(effectPath.c_str(), effectdata, effectlen);
#endif
#endif
    if(data)
    {
        SE_BufferInput inputBuffer(data , len);

        if(!checkHeader(inputBuffer))
        {
            return NULL;
        }
        
        SE_SceneID sceneID;
        sceneID.read(inputBuffer);
#ifdef USE_EFFECT_CBF
        SE_BufferInput* effectBuffer = NULL;
        if(effectdata)
        {
            effectBuffer = new SE_BufferInput(effectdata , effectlen);            
        }
        SE_Spatial* spatial = createSceneNode(inputBuffer, externalSpatial,effectBuffer); 

        if(effectBuffer)
        {
            delete effectBuffer;
        }
#else
        SE_Spatial* spatial = createSceneNode(inputBuffer, externalSpatial); 
        this->clearGeometryAttributeMap();
#endif

//SE_CommonNode* root = new SE_CommonNode;
        //root->read(inputBuffer);
        return spatial;
    }
    else
        return NULL;
}

SE_MeshTransfer* SE_ResourceManager::getMeshTransfer(const SE_MeshID& meshID)
{
    return mImpl->meshMap.get(meshID);
}
bool SE_ResourceManager::setMeshTransfer(const SE_MeshID& meshID, SE_MeshTransfer* meshTransfer)
{
    return mImpl->meshMap.set(meshID, meshTransfer);

}
SE_SkinJointController* SE_ResourceManager::getSkinJointController(const SE_SkinJointControllerID& id)
{
    return mImpl->skinJointControllerMap.get(id);
}
bool SE_ResourceManager::setSkinJointController(const SE_SkinJointControllerID& id, SE_SkinJointController* c)
{
    return mImpl->skinJointControllerMap.set(id, c);
}
void SE_ResourceManager::removeSkinJointController(const SE_SkinJointControllerID& id)
{
    mImpl->skinJointControllerMap.remove(id);
}
//
SE_SkeletonController* SE_ResourceManager::getSkeletonController(const SE_SkeletonControllerID& id)
{
    return mImpl->skeletonControllerMap.get(id);
}
bool SE_ResourceManager::setSkeletonController(const SE_SkeletonControllerID& id, SE_SkeletonController* c)
{
    return mImpl->skeletonControllerMap.set(id, c);
}
void SE_ResourceManager::removeSkeletonController(const SE_SkeletonControllerID& id)
{
    mImpl->skeletonControllerMap.remove(id);
}
//
void SE_ResourceManager::releaseHardwareResource()
{
    ResourceMap<SE_VertexBufferID, SE_VertexBuffer>::RMap::iterator itvb;
    for(itvb = mImpl->vertexBufferMap.getMap()->begin() ; itvb != mImpl->vertexBufferMap.getMap()->end() ; itvb++)
    {
        SE_VertexBuffer* vb = itvb->second.data;
        vb->releaseHardware();
    }

    ResourceMap<SE_ImageDataID, SE_ImageData>::RMap::iterator it;
    for(it = mImpl->imageDataMap.getMap()->begin() ; it != mImpl->imageDataMap.getMap()->end() ; it++)
    {
		SE_ImageData* imgData = it->second.data;
        SE_ImageDataID id = it->first;
        if(imgData)
        {
            GLuint texid = imgData->getTexID();
            if(texid != 0)
            {
                if(SE_Application::getInstance()->SEHomeDebug)
                LOGI("### delete texture %s ##\n", id.getStr());
                glDeleteTextures(1, &texid);
            }
            imgData->setTexID(0);
        }
    }

   /* ResourceMap<SE_PrimitiveID, SE_Primitive>::RMap::iterator itPrimitive;
    for(itPrimitive = mImpl->primitiveMap.getMap()->begin() ; itPrimitive != mImpl->primitiveMap.getMap()->end() ; itPrimitive++)
    {
		SE_Primitive* primitive = itPrimitive->second.data;
        SE_ImageData* imgData = primitive->getImageData();
        if (imgData != NULL)
        {
            GLuint texid = imgData->getTexID();
            if(texid != 0)
            {
                glDeleteTextures(1, &texid);
            }
            imgData->setTexID(0);
        }
    }*/

    ResourceMap<SE_ProgramDataID, SE_ShaderProgram>::RMap::iterator itShader;
    for(itShader = mImpl->shaderMap.getMap()->begin() ; itShader != mImpl->shaderMap.getMap()->end() ; itShader++)
    {
        SE_ShaderProgram* shader = itShader->second.data;
        shader->releaseHardwareResource();
    }

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
    rtm->releaseHardware();
}
bool SE_ResourceManager::registerRes(RES_TYPE type, void* id)
{
    bool ret = true;
	switch(type)
	{
	case GEOM_RES:
		{
			SE_GeometryDataID geomID = *(SE_GeometryDataID*)id;
            mImpl->geomDataMap.inc(geomID);
			break;
		}
	case TEXCOORD_RES:
		{
			SE_TextureCoordDataID texCoordID = *(SE_TextureCoordDataID*)id;
            mImpl->texCoordDataMap.inc(texCoordID);
			break;
		}
	case IMAGE_RES:
		{
			SE_ImageDataID imageDataID = *(SE_ImageDataID*)id;
            const char* pathid = this->getIdPath(imageDataID.getStr());
            if(pathid)
            {
                SE_ImageDataID key = pathid;
                mImpl->imageDataMap.inc(key);
            }
			break;
		}
	case MATERIAL_RES:
		{
			SE_MaterialDataID materialDataID = *(SE_MaterialDataID*)id;
            mImpl->materialDataMap.inc(materialDataID);
			break;
		}
	case SHADER_RES:
		{
			SE_ProgramDataID programDataID = *(SE_ProgramDataID*)id;
            mImpl->shaderMap.inc(programDataID);
			break;
		}
	case RENDERER_RES:
		{
			SE_RendererID rendererID = *(SE_RendererID*)id;
            mImpl->rendererMap.inc(rendererID);
			break;
		}
	case PRIMITIVE_RES:
		{
			SE_PrimitiveID primitiveID = *(SE_PrimitiveID*)id;
            mImpl->primitiveMap.inc(primitiveID);
			break;
		}
	case SKINJOINT_RES:
		{
			SE_SkinJointControllerID skinJointID = *(SE_SkinJointControllerID*)id;
            mImpl->skinJointControllerMap.inc(skinJointID);
			break;
		}
	case SKELETON_RES:
		{
			SE_SkeletonControllerID skeletonID = *(SE_SkeletonControllerID*)id;
            mImpl->skeletonControllerMap.inc(skeletonID);
			break;
		}
	case VERTEXBUFFER_RES:
		{
			SE_VertexBufferID vertexBufferID = *(SE_VertexBufferID*)id;
            mImpl->vertexBufferMap.inc(vertexBufferID);
			break;
		}
	case CAMRERAPOS_RES:
		{
			SE_CameraPositionID cameraPositionID = *(SE_CameraPositionID*)id;
            mImpl->cameraPositionListMap.inc(cameraPositionID);
			break;
		}
    case MESH_RES:
		{
			SE_MeshID meshID = *(SE_MeshID*)id;
            mImpl->meshMap.inc(meshID);
			break;
		}
    case MESHLIST_RES:
		{
			SE_GeometryDataID geid = *(SE_GeometryDataID*)id;
            mImpl->animationMeshListMap.inc(geid);
			break;
		}
    case OBJECT_MESH_RES:
		{
			SE_MeshID meshID = *(SE_MeshID*)id;
            mImpl->objectMeshMap.inc(meshID);
			break;
		}
	default:
        if(SE_Application::getInstance()->SEHomeDebug)
		LOGI("## not support this res id ##\n");
        ret = false;
		break;
	}
    return ret;
}
bool SE_ResourceManager::unregisterRes(RES_TYPE type, void* id)
{
    bool ret = true;
	switch(type)
	{
	case GEOM_RES:
		{
			SE_GeometryDataID geomID = *(SE_GeometryDataID*)id;
            mImpl->geomDataMap.dec(geomID);
			break;
		}
	case TEXCOORD_RES:
		{
			SE_TextureCoordDataID texCoordID = *(SE_TextureCoordDataID*)id;
            mImpl->texCoordDataMap.dec(texCoordID);
			break;
		}
	case IMAGE_RES:
		{
			SE_ImageDataID imageDataID = *(SE_ImageDataID*)id;
            const char* pathid = this->getIdPath(imageDataID.getStr());
            if(pathid)
            {
                SE_ImageDataID key = pathid;
                mImpl->imageDataMap.dec(key);
            }
			break;
		}
	case MATERIAL_RES:
		{
			SE_MaterialDataID materialDataID = *(SE_MaterialDataID*)id;
            mImpl->materialDataMap.dec(materialDataID);
			break;
		}
	case SHADER_RES:
		{
			SE_ProgramDataID programDataID = *(SE_ProgramDataID*)id;
            mImpl->shaderMap.dec(programDataID);
			break;
		}
	case RENDERER_RES:
		{
			SE_RendererID rendererID = *(SE_RendererID*)id;
            mImpl->rendererMap.dec(rendererID);
			break;
		}
	case PRIMITIVE_RES:
		{
			SE_PrimitiveID primitiveID = *(SE_PrimitiveID*)id;
            mImpl->primitiveMap.dec(primitiveID);
			break;
		}
	case SKINJOINT_RES:
		{
			SE_SkinJointControllerID skinJointID = *(SE_SkinJointControllerID*)id;
            mImpl->skinJointControllerMap.dec(skinJointID);
			break;
		}
	case SKELETON_RES:
		{
			SE_SkeletonControllerID skeletonID = *(SE_SkeletonControllerID*)id;
            mImpl->skeletonControllerMap.dec(skeletonID);
			break;
		}
	case VERTEXBUFFER_RES:
		{
			SE_VertexBufferID vertexBufferID = *(SE_VertexBufferID*)id;
            mImpl->vertexBufferMap.dec(vertexBufferID);
			break;
		}
	case CAMRERAPOS_RES:
		{
			SE_CameraPositionID cameraPositionID = *(SE_CameraPositionID*)id;
            mImpl->cameraPositionListMap.dec(cameraPositionID);
			break;
		}
    case MESH_RES:
		{
			SE_MeshID meshID = *(SE_MeshID*)id;
            mImpl->meshMap.dec(meshID);
			break;
		}
    case MESHLIST_RES:
		{
			SE_GeometryDataID geid = *(SE_GeometryDataID*)id;
            mImpl->animationMeshListMap.dec(geid);
			break;
		}
    case OBJECT_MESH_RES:
		{
			SE_MeshID meshID = *(SE_MeshID*)id;
            mImpl->objectMeshMap.dec(meshID);
			break;
		}
	default:
        if(SE_Application::getInstance()->SEHomeDebug)
		LOGI("## not support this res id ##\n");
        ret = false;
		break;
	}
    return ret;
}

void SE_ResourceManager::unLoadScene()
{
    //unload geometry data
    ResourceMap<SE_GeometryDataID, SE_GeometryData>::RMap::iterator geometry_it;
    for(geometry_it = mImpl->geomDataMap.getMap()->begin() ; geometry_it != mImpl->geomDataMap.getMap()->end() ;)
    {
        SE_GeometryDataID id = geometry_it->first;
        if(geometry_it->second.num <= 0)
        {
            geometry_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::GEOM_RES, &id);  
            geometry_it = mImpl->geomDataMap.getMap()->begin();
        }
    }
    

    //unload image data
    ResourceMap<SE_ImageDataID, SE_ImageData>::RMap::iterator image_it;
    for(image_it = mImpl->imageDataMap.getMap()->begin() ; image_it != mImpl->imageDataMap.getMap()->end() ;)
    {
		SE_ImageDataID id = image_it->first;
        if(image_it->second.num <= 0)
        {
            image_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::IMAGE_RES, &id);  
            image_it = mImpl->imageDataMap.getMap()->begin();
        }
    }
    

    //unload primitive data
    ResourceMap<SE_PrimitiveID, SE_Primitive>::RMap::iterator primitive_it;
    for(primitive_it = mImpl->primitiveMap.getMap()->begin() ; primitive_it != mImpl->primitiveMap.getMap()->end() ;)
    {
        SE_PrimitiveID id = primitive_it->first;
        if(primitive_it->second.num <= 0)
        {
            primitive_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::PRIMITIVE_RES, &id);  
            primitive_it = mImpl->primitiveMap.getMap()->begin();
        }       
        
    }
    

    //unload material data
    ResourceMap<SE_MaterialDataID, SE_MaterialData>::RMap::iterator material_it;
    for(material_it = mImpl->materialDataMap.getMap()->begin() ; material_it != mImpl->materialDataMap.getMap()->end() ;)
    {
        SE_MaterialDataID id = material_it->first;
        if(material_it->second.num <= 0)
        {
            material_it++;
            continue;
    }
        else
        {
            unregisterRes(SE_ResourceManager::MATERIAL_RES, &id);
            material_it = mImpl->materialDataMap.getMap()->begin();
        }        
    }
    

    //unload texture data
    ResourceMap<SE_TextureCoordDataID, SE_TextureCoordData> ::RMap::iterator texture_it;
    for(texture_it = mImpl->texCoordDataMap.getMap()->begin() ; texture_it != mImpl->texCoordDataMap.getMap()->end() ;)
    {
        SE_TextureCoordDataID id = texture_it->first;
        if(texture_it->second.num <= 0)
        {
            texture_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::TEXCOORD_RES, &id);  
            texture_it = mImpl->texCoordDataMap.getMap()->begin();
        }            
    }
    

    //unload SE_MeshTransfer
    ResourceMap<SE_MeshID, SE_MeshTransfer>::RMap::iterator meshtran_it;
    for(meshtran_it = mImpl->meshMap.getMap()->begin() ; meshtran_it != mImpl->meshMap.getMap()->end() ; meshtran_it++)
    {
        if(meshtran_it->second.num < 0)
        {
            continue;
        }
        else
        {
        //special map
        SE_MeshTransfer* data = meshtran_it->second.data;
            meshtran_it->second.num--;
        delete data;
    }
    }
   

    //unload skinJointControllerMap
    ResourceMap<SE_SkinJointControllerID, SE_SkinJointController>::RMap::iterator skinjoint_it;
    for(skinjoint_it = mImpl->skinJointControllerMap.getMap()->begin() ; skinjoint_it != mImpl->skinJointControllerMap.getMap()->end() ;)
    {
        SE_SkinJointControllerID id = skinjoint_it->first;
        if(skinjoint_it->second.num <= 0)
        {
			skinjoint_it++;
            continue; 
        }
        else
        {
            unregisterRes(SE_ResourceManager::SKINJOINT_RES, &id); 
            skinjoint_it = mImpl->skinJointControllerMap.getMap()->begin();
        }

    }
    

    //unload SE_SkeletonController
    ResourceMap<SE_SkeletonControllerID, SE_SkeletonController>::RMap::iterator skeleton_it;
    for(skeleton_it = mImpl->skeletonControllerMap.getMap()->begin() ; skeleton_it != mImpl->skeletonControllerMap.getMap()->end() ;)
    {
        SE_SkeletonControllerID id = skeleton_it->first;
        if(skeleton_it->second.num <= 0)
        {
            skeleton_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::SKELETON_RES, &id);
            skeleton_it = mImpl->skeletonControllerMap.getMap()->begin();
        }        
    }
    

    //unload camerapostionmap
    ResourceMap<SE_CameraPositionID, SE_CameraPositionList>::RMap::iterator cameraposition_it;
    for(cameraposition_it = mImpl->cameraPositionListMap.getMap()->begin() ; cameraposition_it != mImpl->cameraPositionListMap.getMap()->end() ;)
    {
        SE_CameraPositionID id = cameraposition_it->first;
        if(cameraposition_it->second.num <= 0)
        {
            cameraposition_it++;
            continue;
    }
        else
        {
            unregisterRes(SE_ResourceManager::CAMRERAPOS_RES, &id); 
            cameraposition_it = mImpl->cameraPositionListMap.getMap()->begin() ;
        }         
    }
    

    //unload rendermap
    ResourceMap<SE_RendererID, SE_Renderer> ::RMap::iterator render_it;
    for(render_it = mImpl->rendererMap.getMap()->begin() ; render_it != mImpl->rendererMap.getMap()->end();)
    {
        if(render_it->second.num < 0)
        {
            render_it++;
            continue;
        }
        else
        {
        SE_RendererID id = render_it->first;
        SE_Renderer* data = render_it->second.data;
        data->free_render();
            render_it->second.num--;
        mImpl->rendererMap.getMap()->erase(render_it++);
        }

    }
    

    //unload shadermap
    ResourceMap<SE_ProgramDataID, SE_ShaderProgram> ::RMap::iterator shader_it;
    for(shader_it = mImpl->shaderMap.getMap()->begin() ; shader_it != mImpl->shaderMap.getMap()->end() ;)
    {
        if(shader_it->second.num < 0)
        {
            shader_it++;
            continue;
        }
        else
        {
        SE_ProgramDataID id = shader_it->first;
        shader_it->second.data->free_shader();
            shader_it->second.num--;
        mImpl->shaderMap.getMap()->erase(shader_it++);
        }
       
    }
    

    //unload vertex buffer
    ResourceMap<SE_VertexBufferID, SE_VertexBuffer>::RMap::iterator vb_it;
    for(vb_it = mImpl->vertexBufferMap.getMap()->begin() ; vb_it != mImpl->vertexBufferMap.getMap()->end() ; )
    {
        SE_VertexBufferID id = vb_it->first;
        if(vb_it->second.num <= 0)
        {
            vb_it++;
            continue;
        }
        else
        {
        unregisterRes(SE_ResourceManager::VERTEXBUFFER_RES, &id);
            vb_it = mImpl->vertexBufferMap.getMap()->begin();
        }        
    }

    //unload meshlist

    ResourceMap<SE_GeometryDataID, SE_GeometryDataList>::RMap::iterator ml_it;
    for(ml_it = mImpl->animationMeshListMap.getMap()->begin() ; ml_it != mImpl->animationMeshListMap.getMap()->end() ; )
    {
        SE_GeometryDataID id = ml_it->first;
        if(ml_it->second.num <= 0)
        {
            ml_it++;
            continue;
        }
        else
        {
            unregisterRes(SE_ResourceManager::MESHLIST_RES, &id);
            ml_it = mImpl->animationMeshListMap.getMap()->begin();
        }        
    }
    
}

void SE_ResourceManager::generateDefaultRenderEnv()
{
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("#####generateDefaultRendEnv:set default render and shader....\n\n");

    SE_ShaderDefine* sd = new SE_ShaderDefine();       

    char* vertexShaderBytes;
    char* fragmentShaderBytes;
    for(int i = 0; i < sd->shaderArray.size(); ++i)
    {
        int vertexnum = sd->shaderArray[i].vertex_shader.size();
        int fragmentnum = sd->shaderArray[i].fragment_shader.size();            

        SE_ProgramDataID programDataID = sd->shaderArray[i].shaderId.c_str();
        vertexShaderBytes = new char[vertexnum + 1];
        fragmentShaderBytes = new char[fragmentnum + 1];

        memset(vertexShaderBytes,0,sizeof(vertexShaderBytes));
        memset(fragmentShaderBytes,0,sizeof(fragmentShaderBytes));

        memcpy(vertexShaderBytes,sd->shaderArray[i].vertex_shader.data(),sd->shaderArray[i].vertex_shader.size());
        memcpy(fragmentShaderBytes,sd->shaderArray[i].fragment_shader.data(),sd->shaderArray[i].fragment_shader.size());

        vertexShaderBytes[vertexnum] = '\0';
        fragmentShaderBytes[fragmentnum] = '\0';

        //avoid same id add to map when load second cbf file to running app.
        if(!this->hasLoadShaderProgram(programDataID))
        {
            SE_ShaderProgram* shaderProgram = (SE_ShaderProgram*)SE_ObjectFactory::create(sd->shaderArray[i].shaderClassName.c_str());
            this->setShaderProgram(programDataID, shaderProgram,vertexShaderBytes, fragmentShaderBytes);
        }
        delete[] vertexShaderBytes;
        delete[] fragmentShaderBytes;

    }

    SE_RenderDefine* rd = new SE_RenderDefine();        
    
    for(int i = 0; i < rd->mRenderClassName.size(); ++i)
    {    

        std::string rendererID = rd->mRenderIdArray[i];
        std::string rendererClassName = rd->mRenderClassName[i];

        //avoid same id add to map when load second cbf file to running app.
        if(this->getRenderer(rendererID.c_str()))
        {
            //Renderer has been loaded.
            continue;
        }
        else
        {
            SE_Renderer* renderer = (SE_Renderer*)SE_ObjectFactory::create(rendererClassName.c_str());
            if(!renderer)
            {
                if(SE_Application::getInstance()->SEHomeDebug)
                LOGI("... error renderer %s is not define\n", rendererClassName.c_str());
            }
            this->setRenderer(rendererID.c_str(), renderer);
        }
    }    
    
    delete sd;
    delete rd;
}

void SE_ResourceManager::loadPlugin(std::vector<std::string>& pluginslist)
{
    SE_PluginsManager* pm = SE_Application::getInstance()->getPluginsManager();

    for(int i = 0; i < pluginslist.size();++i)
    {

        SE_ShaderPlugin* shderplugin = NULL;

        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("##now ,loading %s ##\n\n",pluginslist[i].c_str());
        SE_Plugin* p= pm->loadPlugin(pluginslist[i].c_str());
        

        if(p && p->getPluginType() == SE_Plugin::SE_SHADER_RENDER)
        {
            shderplugin = (SE_ShaderPlugin*)p;
        }
        else
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("##Not found plugin in %s ,continue...##\n\n",pluginslist[i].c_str());
            continue;
        }


        SE_ShaderDefine* sd = NULL;

        if(!shderplugin)
        {
            //plugin is not about shader,create default shader
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("no plugins, load default!!\n\n");
            continue;
        }
        else
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            LOGI("found a plugin,loading...\n\n");
            sd = shderplugin->getPluginShaderDefine();
        }

    char* vertexShaderBytes;
    char* fragmentShaderBytes;
        for(int i = 0; i < sd->shaderArray.size(); ++i)
    {
            int vertexnum = sd->shaderArray[i].vertex_shader.size();
            int fragmentnum = sd->shaderArray[i].fragment_shader.size();            

            SE_ProgramDataID programDataID = sd->shaderArray[i].shaderId.c_str();
        vertexShaderBytes = new char[vertexnum + 1];
        fragmentShaderBytes = new char[fragmentnum + 1];

        memset(vertexShaderBytes,0,sizeof(vertexShaderBytes));
        memset(fragmentShaderBytes,0,sizeof(fragmentShaderBytes));

            memcpy(vertexShaderBytes,sd->shaderArray[i].vertex_shader.data(),sd->shaderArray[i].vertex_shader.size());
            memcpy(fragmentShaderBytes,sd->shaderArray[i].fragment_shader.data(),sd->shaderArray[i].fragment_shader.size());

        vertexShaderBytes[vertexnum] = '\0';
        fragmentShaderBytes[fragmentnum] = '\0';

        //avoid same id add to map when load second cbf file to running app.
        if(!this->hasLoadShaderProgram(programDataID))
        {
                SE_ShaderProgram* shaderProgram = (SE_ShaderProgram*)SE_ObjectFactory::create(sd->shaderArray[i].shaderClassName.c_str());
                this->setShaderProgram(programDataID, shaderProgram,vertexShaderBytes, fragmentShaderBytes);
        }
        delete[] vertexShaderBytes;
        delete[] fragmentShaderBytes;

    }

        SE_RenderDefine* rd = NULL;
        if(!shderplugin)
        {
            //plugin is not about shader,create default shader
            continue;
        }
        else
        {
            rd = shderplugin->getPluginRenderDefine();
        }
    
        for(int i = 0; i < rd->mRenderClassName.size(); ++i)

    {    

            std::string rendererID = rd->mRenderIdArray[i];
            std::string rendererClassName = rd->mRenderClassName[i];

        //avoid same id add to map when load second cbf file to running app.
        if(this->getRenderer(rendererID.c_str()))
        {
            //Renderer has been loaded.
            continue;
        }
        else
        {
                SE_Renderer* renderer = (SE_Renderer*)SE_ObjectFactory::create(rendererClassName.c_str());
            if(!renderer)
            {
                if(SE_Application::getInstance()->SEHomeDebug)
                LOGI("... error renderer %s is not define\n", rendererClassName.c_str());
            }
            this->setRenderer(rendererID.c_str(), renderer);
        }
    }

    }

}

void SE_ResourceManager::loadPlugins(const char* filePath)
{
    std::vector<std::string> result;
    
#ifdef WIN32
    SE_File::FindFile(result,"plugins","*.dll");    
#else
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("in linux:push a test to result\n\n");
    SE_File::FindFile(result, filePath, "*.so");    
#endif

    if(result.size() > 0)
        {
        loadPlugin(result);
        }
    
        generateDefaultRenderEnv();
}

bool SE_ResourceManager::inflate(const char* xmlPath)
{
    std::string path = xmlPath;
    size_t pos = path.rfind('/');
    path = path.substr(0,pos + 1);
#ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile(data,len);
    delete data;

#else    
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif
    if(!ok)
    {
        return NULL;
    }
    LOGI("\n %s: \n",xmlPath);   

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem;
    TiXmlHandle hRoot(0);
    TiXmlElement* parent = NULL;

    //parse imageid - path
    pElem = hDoc.FirstChildElement("resource").Element();

    if(!pElem)
    {
        LOGI("resource not found!!\n");
        return false;
    }

    pElem = pElem->FirstChildElement("imageKeys");
    if(!pElem)
    {
        LOGI("imageKeys not found!!\n");
        return false;
    }


    parent = pElem->FirstChildElement("imageKey");
    pElem = pElem->FirstChildElement("imageKey");
    

    for(parent;parent;parent = parent->NextSiblingElement())
    {
        std::string id,path;
        if(parent)
        {
            parent->QueryStringAttribute("imageid",&id);
            parent->QueryStringAttribute("path",&path);
        }

        if(!getIdPath(id.c_str()))
        {
            this->setIdPath(id.c_str(),path.c_str());
        }
        else
        {
            LOGI("id = %s has exist!!!\n",id.c_str());
        }
    }
}

bool SE_ResourceManager::loadEffectXML(const char *xmlPath,std::map<std::string,SE_SpatialEffectData*> &spDataMap)
{
    SE_TimeMS currTime = 0;
    if(SE_Application::getInstance()->SEHomeDebug)
    {
        currTime = SE_Time::getCurrentTimeMS();
    }

    std::string path = xmlPath;
    size_t pos = path.rfind('/');
    path = path.substr(0,pos + 1);
#ifdef ANDROID
    char* data = NULL;
    int len = 0;
    SE_Application::getInstance()->getAssetManager()->readAsset(xmlPath, data, len);
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile(data,len);
    delete data;

#else    
    TiXmlDocument doc(xmlPath);
    bool ok = doc.LoadFile();
#endif
    if(!ok)
    {
        return false;
    }
    if(SE_Application::getInstance()->SEHomeDebug)
    {        
        LOGI("\n %s: load ok\n",xmlPath);
    }

    TiXmlHandle hDoc(&doc);
    TiXmlElement* pElem;
    TiXmlHandle hRoot(0);
    TiXmlElement* parent = NULL;

    SE_SpatialEffectData* spdata = NULL;

    //parse imageid - path
    pElem = hDoc.FirstChildElement("childrenAttribute").Element();

    if(!pElem)
    {
        LOGI("childrenAttribute not found!!\n");
        return false;
    }   


    parent = pElem->FirstChildElement("child");
    pElem = pElem->FirstChildElement("child");
    

    //new child
    for(parent;parent;parent = parent->NextSiblingElement())
    {
        std::string spatialName;
        pElem = parent->FirstChildElement("geometry");
        pElem->QueryStringAttribute("spatialName",&spatialName);

        std::vector<std::string> lightList;
        TiXmlElement* lightparent = NULL;
        pElem = parent->FirstChildElement("lights");
        pElem = pElem->FirstChildElement("light");
        lightparent = pElem;
        for(lightparent;lightparent;lightparent = lightparent->NextSiblingElement())
        {
            std::string lightName;
            
            lightparent->QueryStringAttribute("lightName",&lightName);
            lightList.push_back(lightName);
        }

        //status
        unsigned int status = 0;
        pElem = parent->FirstChildElement("geostatus");
        pElem->QueryUnsignedAttribute("statusValue",&status);

        //alpha
        float alpha = 1.0;
        pElem = parent->FirstChildElement("alpha");
        pElem->QueryFloatAttribute("alphaValue",&alpha);

        //effectdata
        SE_Vector4f effectData;
        pElem = parent->FirstChildElement("effectData");
        pElem->QueryFloatAttribute("effectDataX",&effectData.x);
        pElem->QueryFloatAttribute("effectDataY",&effectData.y);
        pElem->QueryFloatAttribute("effectDataZ",&effectData.z);
        pElem->QueryFloatAttribute("effectDataW",&effectData.w);

        spdata = new SE_SpatialEffectData();
        spdata->mSpatialName = spatialName;
        spdata->mLightsNameList = lightList;
        spdata->mEffectState = status;
        spdata->mAlpha = alpha;
        spdata->mEffectData = effectData;

        spDataMap.insert(std::pair<std::string,SE_SpatialEffectData*> (spatialName,spdata));

    }

    if(SE_Application::getInstance()->SEHomeDebug)
    {
        SE_TimeMS finishTime = SE_Time::getCurrentTimeMS();
        LOGI("\n\n SE_ResourceManager::loadEffectXML:[%s] load finish, duration = %lu ms\n\n",xmlPath,finishTime - currTime);
    }
    
    return spdata;
}

void SE_ResourceManager::copy(SE_ResourceManager* ori)
{
    if(!ori)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        {
            LOGI("SE_ResourceManager::copy ori is NULL!!!! \n");
        }
        return;
    }
    //1.geomap     
    ResourceMap<SE_GeometryDataID, SE_GeometryData>::RMap::iterator geometry_it = ori->mImpl->geomDataMap.getMap()->begin();
    for( ; geometry_it != ori->mImpl->geomDataMap.getMap()->end() ;geometry_it++)
    {
        if(!this->setGeometryData(geometry_it->first,geometry_it->second.data))
        {
            //exist
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("geometry add fail!!!!!!!!!!\n");
            }
        }
    }

    //2.imageID path map
    _Impl::ImageIdPathMap::iterator id_it;
    for(id_it = ori->mImpl->imageIdPathMap.begin();id_it != ori->mImpl->imageIdPathMap.end();)
    {    
        SE_ImageDataID id = id_it->first.c_str();
        this->setIdPath(id_it->first.c_str(),id_it->second.c_str());
        id_it++;
    }

    //2.5 imagedata map
    ResourceMap<SE_ImageDataID, SE_ImageData>::RMap::iterator img_it;
    for(img_it = ori->mImpl->imageDataMap.getMap()->begin();img_it != ori->mImpl->imageDataMap.getMap()->end();)
    {
        this->insertPathImageData(img_it->first.getStr(),img_it->second.data);
        img_it++;
    }

    //3.texCoordDataMap
    ResourceMap<SE_TextureCoordDataID, SE_TextureCoordData>::RMap::iterator texture_it = ori->mImpl->texCoordDataMap.getMap()->begin();
    for( ; texture_it != ori->mImpl->texCoordDataMap.getMap()->end() ;texture_it++)
    {
        if(!this->setTextureCoordData(texture_it->first,texture_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("SE_TextureCoordData add fail!!!!!!!!!!\n");
            }
            delete texture_it->second.data;
        }
    }

    //4.materialDataMap
    ResourceMap<SE_MaterialDataID, SE_MaterialData>::RMap::iterator mat_it = ori->mImpl->materialDataMap.getMap()->begin();
    for( ; mat_it != ori->mImpl->materialDataMap.getMap()->end() ;mat_it++)
    {
        if(!this->setMaterialData(mat_it->first,mat_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("setMaterialData add fail!!!!!!!!!!\n");
            }
            delete mat_it->second.data;
        }
    }

    //5.meshMap
    ResourceMap<SE_MeshID, SE_MeshTransfer>::RMap::iterator meshtransfer_it = ori->mImpl->meshMap.getMap()->begin();
    for( ; meshtransfer_it != ori->mImpl->meshMap.getMap()->end() ;meshtransfer_it++)
    {
        //*(this->mImpl->meshMap.getMap()) = *(ori->mImpl->meshMap.getMap());
        if(!this->setMeshTransfer(meshtransfer_it->first,meshtransfer_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("meshtransfer add fail!!!!!!!!!!\n");
            }
            delete meshtransfer_it->second.data;
        }
    }

    //6.shaderMap
    ResourceMap<SE_ProgramDataID, SE_ShaderProgram>::RMap::iterator shader_it = ori->mImpl->shaderMap.getMap()->begin();
    for( ; shader_it != ori->mImpl->shaderMap.getMap()->end() ;shader_it++)
    {
        //*(this->mImpl->shaderMap.getMap()) = *(ori->mImpl->shaderMap.getMap());
        if(!this->insertShaderProgram(shader_it->first,shader_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("shader_it add fail!!!!!!!!!!\n");
            }
            delete shader_it->second.data;
        }
    }

    //7.primitiveMap
    ResourceMap<SE_PrimitiveID, SE_Primitive>::RMap::iterator primitive_it = ori->mImpl->primitiveMap.getMap()->begin();
    for( ; primitive_it != ori->mImpl->primitiveMap.getMap()->end() ;primitive_it++)
    {
        if(!this->setPrimitive(primitive_it->first,primitive_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("primitive_it add fail!!!!!!!!!!\n");
            }
            delete primitive_it->second.data;
        }
    }

    //8.skinJointControllerMap
    ResourceMap<SE_SkinJointControllerID, SE_SkinJointController>::RMap::iterator skj_it = ori->mImpl->skinJointControllerMap.getMap()->begin();
    for( ; skj_it != ori->mImpl->skinJointControllerMap.getMap()->end() ;skj_it++)
    {
        if(!this->setSkinJointController(skj_it->first,skj_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("skj_it add fail!!!!!!!!!!\n");
            }
            delete skj_it->second.data;
        }
    }

    //9.skeletonControllerMap
    ResourceMap<SE_SkeletonControllerID, SE_SkeletonController>::RMap::iterator skl_it = ori->mImpl->skeletonControllerMap.getMap()->begin();
    for( ; skl_it != ori->mImpl->skeletonControllerMap.getMap()->end() ;skl_it++)
    {
        if(!this->setSkeletonController(skl_it->first,skl_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("skl_it add fail!!!!!!!!!!\n");
            }
            delete skl_it->second.data;
        }
    }

    //10.cameraPositionListMap
    ResourceMap<SE_CameraPositionID, SE_CameraPositionList>::RMap::iterator cam_it = ori->mImpl->cameraPositionListMap.getMap()->begin();
    for( ; cam_it != ori->mImpl->cameraPositionListMap.getMap()->end() ;cam_it++)
    {
        if(!this->setCameraPositionList(cam_it->first,cam_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("cam_it add fail!!!!!!!!!!\n");
            }
            delete cam_it->second.data;
        }
    }

    //11.rendererMap
    ResourceMap<SE_RendererID, SE_Renderer>::RMap::iterator render_it = ori->mImpl->rendererMap.getMap()->begin();
    for( ; render_it != ori->mImpl->rendererMap.getMap()->end() ;render_it++)
    {
        if(!this->setRenderer(render_it->first,render_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("render_it add fail!!!!!!!!!!\n");
            }
            delete render_it->second.data;
        }
    }

    //12.vertexBufferMap
    ResourceMap<SE_VertexBufferID, SE_VertexBuffer>::RMap::iterator vb_it = ori->mImpl->vertexBufferMap.getMap()->begin();
    for( ; vb_it != ori->mImpl->vertexBufferMap.getMap()->end() ;vb_it++)
    {
        if(!this->setVertexBuffer(vb_it->first,vb_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("vb_it add fail!!!!!!!!!!\n");
            }
            delete vb_it->second.data;
        }
    }

    //13.animationMeshListMap
    ResourceMap<SE_GeometryDataID, SE_GeometryDataList>::RMap::iterator animation_it = ori->mImpl->animationMeshListMap.getMap()->begin();
    for( ; animation_it != ori->mImpl->animationMeshListMap.getMap()->end() ;animation_it++)
    {
        if(!this->setAnimationMeshList(animation_it->first,animation_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("animation_it add fail!!!!!!!!!!\n");
            }
            delete animation_it->second.data;
        }
    }

    //14.objectMeshMap
    ResourceMap<SE_MeshID, SE_Mesh>::RMap::iterator mesh_it = ori->mImpl->objectMeshMap.getMap()->begin();
    for( ; mesh_it != ori->mImpl->objectMeshMap.getMap()->end() ;mesh_it++)
    {
        if(!this->setObjectMesh(mesh_it->first,mesh_it->second.data))
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("mesh_it add fail!!!!!!!!!!\n");
            }
            delete mesh_it->second.data;
        }
    }

    //effect data map
    std::map<std::string,SE_SpatialEffectData*>::iterator eff_it = ori->mSpatialDataMap.begin();
    for( ; eff_it != ori->mSpatialDataMap.end() ;eff_it++)
    {
        std::pair<std::map<std::string,SE_SpatialEffectData*>::iterator,bool> result;
        result = this->mSpatialDataMap.insert(std::pair<std::string,SE_SpatialEffectData*> (eff_it->first,eff_it->second));
        if(!result.second)
        {
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("eff_it add fail!!!!!!!!!!\n");
            }
            delete eff_it->second;
        }
    
    }
	std::map<std::string,_Impl::MountPointArray>::iterator mount_it = ori->mImpl->mMountPointMap.begin();
    for(;mount_it != ori->mImpl->mMountPointMap.end();mount_it++)
    {
        this->mImpl->mMountPointMap.insert(std::pair<std::string,_Impl::MountPointArray>(mount_it->first,mount_it->second));
    }

    //clear
    ori->mImpl->clear();

}

void SE_ResourceManager::getMountPointsByName(const char* groupName,std::vector<SE_MountPoint*>& result)
{
    std::string name = groupName;
    std::map<std::string,_Impl::MountPointArray>::iterator mount_it = mImpl->mMountPointMap.find(name);

    if(mount_it != mImpl->mMountPointMap.end())
    {
        //found
        result = mount_it->second;
    }
}

void SE_ResourceManager::getGroupNamesHasMountPoint(std::vector<std::string>& result)
{
    std::map<std::string,_Impl::MountPointArray>::iterator mount_it = mImpl->mMountPointMap.begin();

    for(;mount_it != mImpl->mMountPointMap.end(); mount_it++)
    {
        result.push_back(mount_it->first);
    }
}