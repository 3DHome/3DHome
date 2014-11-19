#ifndef SE_RESOURCEMANAGER_H
#define SE_RESOURCEMANAGER_H
#include "SE_ID.h"
#include<map>
class SE_GeometryData;
class SE_TextureCoordData;
class SE_ImageData;
class SE_MaterialData;
class SE_Mesh;
class SE_MeshTransfer;
class SE_ShaderProgram;
class SE_Spatial;
class SE_SpatialEffectData;
class SE_Primitive;
class SE_Bone;
class SE_Biped;
class SE_SkinJointController;
class SE_BipedController;
class SE_SkeletonController;
class SE_Renderer;
class SE_CameraPositionList;
class SE_VertexBuffer;
class SE_GeometryDataList;
class SE_MountPoint;

template <typename T>
struct Resource
{
    T* data;
    int num;
    Resource()
    {
        num = 0;
        data = NULL;
    }
    Resource(T* data, int n)
    {
        this->data = data;
        num = n;
    }
};
/////////////////////////////////////////
template <typename TID, typename T>
class ResourceMap
{
public:
    typedef std::map<TID, Resource<T> > RMap;
    T* get(const TID& id);
    int getNum(const TID& id);
    bool set(const TID& id, T* data,bool replace = false);
    void remove(const TID& id);
	void inc(const TID& id);
	void dec(const TID& id);
    ~ResourceMap();
    RMap* getMap()
    {
        return &m;
    }
private:
    RMap m;
};
template <typename TID, typename T>
void ResourceMap<TID, T>::remove(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it != m.end())
    {
        Resource<T> res = it->second;
        //delete it->second;
        delete res.data;
        m.erase(it);
    }
}

template <typename TID, typename T>
T* ResourceMap<TID, T>::get(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
        return NULL;
    else
    {
        Resource<T> res = it->second;
        return res.data;
        //return it->second;
    }
}
template <typename TID, typename T>
bool ResourceMap<TID, T>::set(const TID& id, T* data,bool replace)
{
    typename RMap::iterator it = m.find(id); 
    if(it == m.end())
    {
        Resource<T> res(data, 0);
        m.insert(std::pair<TID, Resource<T> >(id, res));
        return true;
    }

    if(!replace)
    {
        return false;
    }

    //SE_ASSERT(0);
    Resource<T> res = it->second;
    T* oldData = res.data;//it->second;    
    res.data = data;
    it->second = res;
    if(oldData)
    {
        delete oldData;
    }
    return true;

}
template <typename TID, typename T>
ResourceMap<TID, T>::~ResourceMap()
{
    typename RMap::iterator it;
    for(it = m.begin() ; it != m.end() ; it++)
    {
        //T* data = it->second;
        //delete data;
        Resource<T> res = it->second;
        delete res.data;
    }
}
template <typename TID, typename T>
int ResourceMap<TID, T>::getNum(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
    {
        return 0;
    }
    else
    {
        return it->second.num;
    }
}
template <typename TID, typename T>
void ResourceMap<TID, T>::inc(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
    {
        return;
    }
    else
    {
        it->second.num++;
    }
}
template <typename TID, typename T>
void ResourceMap<TID, T>::dec(const TID& id)
{
    typename RMap::iterator it = m.find(id);
    if(it == m.end())
    {
        return;
    }
    else
    {
        it->second.num--;
        //delete resource when num is zero
        if(it->second.num <= 0)
        {            
			Resource<T> res = it->second;
            delete res.data;
            m.erase(it); 
        }
    }
}

class SE_ENTRY SE_ResourceManager
{
public:
    SE_ResourceManager();
    SE_ResourceManager(const char* dataPath);
    ~SE_ResourceManager();
    enum RES_TYPE {GEOM_RES, TEXCOORD_RES, IMAGE_RES, MATERIAL_RES,
                   SHADER_RES, RENDERER_RES, PRIMITIVE_RES, SKINJOINT_RES,
                   SKELETON_RES, VERTEXBUFFER_RES, CAMRERAPOS_RES,MESH_RES,
                    MESHLIST_RES,OBJECT_MESH_RES};
    SE_GeometryData* getGeometryData(const SE_GeometryDataID& geomID);
    bool setGeometryData(const SE_GeometryDataID& geomID, SE_GeometryData* data);
    void removeGeometryData(const SE_GeometryDataID& geomID);
    
    SE_TextureCoordData* getTextureCoordData(const SE_TextureCoordDataID& texID);
    bool setTextureCoordData(const SE_TextureCoordDataID& texID, SE_TextureCoordData* data);
    void removeTextureCoordData(const SE_TextureCoordDataID& texID);
    
    SE_ImageData* getImageData(const SE_ImageDataID& imageID);
    
    bool insertPathImageData(const char* pathID,SE_ImageData* data,bool replace = false);
    SE_ImageData* getImageDataFromPath(const char* imagePath);
   


    SE_MaterialData* getMaterialData(const SE_MaterialDataID& materialID);
    bool setMaterialData(const SE_MaterialDataID& materialID, SE_MaterialData* data);
    void removeMaterialData(const SE_MaterialDataID& materialID);

    SE_MeshTransfer* getMeshTransfer(const SE_MeshID& meshID);
    bool setMeshTransfer(const SE_MeshID& meshID, SE_MeshTransfer* meshTransfer);
    void removeMeshTransfer(const SE_MeshID& meshID); 

    SE_ShaderProgram* getShaderProgram(const SE_ProgramDataID& programDataID);
    bool hasLoadShaderProgram(const SE_ProgramDataID& programDataID);
    bool setShaderProgram(const SE_ProgramDataID& programDataID, SE_ShaderProgram* shaderProgram, char* vertexShader, char* fragmentShader);
    bool insertShaderProgram(const SE_ProgramDataID& programDataID, SE_ShaderProgram* shaderProgram);
    void removeShaderProgram(const SE_ProgramDataID& programDataID);
    
    SE_Primitive* getPrimitive(const SE_PrimitiveID& primitveID);
    bool setPrimitive(const SE_PrimitiveID& primitiveID , SE_Primitive* primitive);
    void removePrimitive(const SE_PrimitiveID& primitiveID);

    SE_SkinJointController* getSkinJointController(const SE_SkinJointControllerID& id);
    bool setSkinJointController(const SE_SkinJointControllerID& id, SE_SkinJointController* c);
    void removeSkinJointController(const SE_SkinJointControllerID& id);

    SE_SkeletonController* getSkeletonController(const SE_SkeletonControllerID& id);
    bool setSkeletonController(const SE_SkeletonControllerID& id, SE_SkeletonController* c);
    void removeSkeletonController(const SE_SkeletonControllerID& id);
    
    SE_Renderer* getRenderer(const SE_RendererID& rendererID);
    bool setRenderer(const SE_RendererID& rendererID, SE_Renderer* renderer);
    void removeRenderer(const SE_RendererID& rendererID);

    SE_CameraPositionList* getCameraPositionList(const SE_CameraPositionID& id);
    bool setCameraPositionList(const SE_CameraPositionID& id, SE_CameraPositionList* c);
    void removeCameraPositionList(const SE_CameraPositionID& id);

    SE_VertexBuffer* getVertexBuffer(const SE_VertexBufferID& vbID);
    bool setVertexBuffer(const SE_VertexBufferID& vbID, SE_VertexBuffer* data);
    void removeVertexBuffer(const SE_VertexBufferID& vbID);

    SE_GeometryDataList*  getAnimationMeshList(const SE_GeometryDataID& goID);
    bool setAnimationMeshList(const SE_GeometryDataID& goID, SE_GeometryDataList* data );
    void removeAnimationMeshList(const SE_GeometryDataID& goID);

    SE_Mesh*  getObjectMesh(const SE_MeshID& mID);
    bool setObjectMesh(const SE_MeshID& mID, SE_Mesh* data );
    void removeObjectMesh(const SE_MeshID& mID);

    const char* getIdPath(const char* imageid);
    bool setIdPath(const char* imageid, const char* imagePath,bool replace = false);    
    void clearIdPath();
    void removeIdPath(const char* imageid);

    void getMountPointsByName(const char* groupName,std::vector<SE_MountPoint*>& result);
    void getGroupNamesHasMountPoint(std::vector<std::string>& result);
    
    bool inserGeometryAttribute(const char* spatialName, const char* spatialAttribute);


    bool registerRes(RES_TYPE type, void* id);
    bool unregisterRes(RES_TYPE type, void* id);
    /*
     * base data contains: geometry data, texture coord data, material data, image data and mesh data
     * */
    void loadBaseData(const char* imagePath, const char* baseResourceName,const char* xmlName = NULL);
    SE_Spatial* loadScene(const char* sceneName,SE_Spatial* externalSpatial = NULL);

    const char* getDataPath();
    void setDataPath(const char* datapath);

    bool checkHeader(SE_BufferInput& input);
    void releaseHardwareResource();
    
    void generateDefaultRenderEnv();
    void loadPlugins(const char* filePath = NULL);
    void loadPlugin(std::vector<std::string>& pluginslist);

    bool inflate(const char* xmlPath);
    
    void copy(SE_ResourceManager* ori);
private:
    bool setImageData(const SE_ImageDataID& imageID,const char* pathID, SE_ImageData* data,bool replace = false);
//    SE_Mesh* createMesh(SE_MeshTransfer* meshTransfer);
    SE_Spatial* createSceneNode(SE_BufferInput& inputBuffer, SE_Spatial* parent,SE_BufferInput* effectBuffer = NULL);
    void unLoadScene();
    bool loadEffectXML(const char* xmlPath,std::map<std::string,SE_SpatialEffectData*> &spDataMap);

    SE_SpatialEffectData* getGeometryAttribute(const char* spatialName);
    void clearGeometryAttributeMap();
    void removeImageData(const SE_ImageDataID& imageID);
    bool replaceImageData(const SE_ImageDataID& imageID,const char* pathID,SE_ImageData* data);

    SE_ImageData* loadCommonCompressImage(const char* imageName,int type);
    SE_ImageData* loadImage(const char* imageName, int type);
    SE_ImageData* loadRawImage(const char* imageName);
    void processGeometryData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processTextureCoordData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processMaterialData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processImageData(const char* imagePath, SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processRendererData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processMeshData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    SE_Bone* findBone(const std::string& boneName, std::vector<SE_Bone*>& boneVector);
    void processSkinJointController(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    SE_Biped* findBiped(const std::string& bipName, std::vector<SE_Biped*>& bipVector);
    void processBipedController(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processShaderProgram(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processCameraData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processPrimitive(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processVertexBufferData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processAnimationMeshListData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void processMountPointData(SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);
    void process(const char* imagePath, SE_BufferInput& inputBuffer, SE_ResourceManager* resourceManager);

private:


    class _Impl
    {
    public:
        ~_Impl();

    ResourceMap<SE_GeometryDataID, SE_GeometryData> geomDataMap;
    ResourceMap<SE_ImageDataID, SE_ImageData> imageDataMap;
    ResourceMap<SE_TextureCoordDataID, SE_TextureCoordData> texCoordDataMap;
    ResourceMap<SE_MaterialDataID, SE_MaterialData> materialDataMap;
    ResourceMap<SE_MeshID, SE_MeshTransfer> meshMap;
    ResourceMap<SE_ProgramDataID, SE_ShaderProgram> shaderMap;
    ResourceMap<SE_PrimitiveID, SE_Primitive> primitiveMap;
    ResourceMap<SE_SkinJointControllerID, SE_SkinJointController> skinJointControllerMap;
    ResourceMap<SE_SkeletonControllerID, SE_SkeletonController> skeletonControllerMap;
    ResourceMap<SE_CameraPositionID, SE_CameraPositionList> cameraPositionListMap;
    ResourceMap<SE_RendererID, SE_Renderer> rendererMap;

    ResourceMap<SE_VertexBufferID, SE_VertexBuffer> vertexBufferMap;

    ResourceMap<SE_GeometryDataID, SE_GeometryDataList> animationMeshListMap;

    ResourceMap<SE_MeshID, SE_Mesh> objectMeshMap;

    typedef std::map<std::string, std::string> ImageIdPathMap;
    ImageIdPathMap imageIdPathMap;//for dynamic change image path

    typedef std::vector<SE_MountPoint*> MountPointArray;
    std::map<std::string,MountPointArray> mMountPointMap;

    std::string dataPath;
    SE_ResourceManager* resourceManager;
//////////////////////////////////
    void process_file(const char* imagePath, SE_BufferInput& inputBuffer);
    void clear();
    
    };
    _Impl* mImpl;

    std::map<std::string,std::string> mEffectmap;

    std::map<std::string,SE_SpatialEffectData*> mSpatialDataMap;

    
};
#endif
