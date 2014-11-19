#include "SE_DynamicLibType.h"
#include "SE_ElementManager.h"
#include "SE_Log.h"
#include "SE_Element.h"
#include "SE_ElementGroup.h"
#include "SE_TextureCoordAnimation.h"
#include "SE_Common.h"
#include "SE_Spatial.h"
#include "SE_Application.h"
#include "SE_ResourceManager.h"
#include "SE_IO.h"
#include <stdlib.h>
#include <string.h>
#include <utility>
#include "SE_MemLeakDetector.h"
//////////////////////////////////
void SE_ElementHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    SE_Element* element = new SE_Element;
    if(parent)
    {
        parent->addChild(element);
        element->setParent(parent);
    }
    else
    {
        element->setParent(NULL);
        elementManager->setRoot(element);
    }
    bool hasLayer = false;
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        int ival = -1;
        if(!strcmp(name, "id"))
        {
            element->setID(value);
        }
        else if(!strcmp(name, "x"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setLeft(ival);
            }
            else
            {
                LOGI("... parse x value error\n");
            }
        }
        else if(!strcmp(name, "y"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setTop(ival);
            }
            else
            {
                LOGI("... parse y value error\n");
            }
        }
        else if(!strcmp(name, "width"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setWidth(ival);
            }
            else
            {
                LOGI("... parse width value error\n");
            }
        }
        else if(!strcmp(name, "height"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setHeight(ival);
            }
            else
            {
                LOGI("... parse height value error\n");
            }
        }
        else if(!strcmp(name, "layer"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setLocalLayer(ival);
            }
            else
            {
                LOGI("... parse layer value error\n");
            }
            hasLayer = true;
        }
        else if(!strcmp(name, "pivotx"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setPivotX(ival);
            }
            else
            {
                LOGI("... parse pivotx value error\n");
            }
        }
        else if(!strcmp(name, "pivoty"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setPivotY(ival);
            }
            else
            {
                LOGI("... parse pivoty value error\n");
            }
        }
        pAttribute = pAttribute->Next();
    }
    if(!hasLayer)
    {
        element->setLocalLayer(indent);
    }
    TiXmlNode* currNode = xmlElement;
    TiXmlNode* pChild = NULL;
    for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
    {
        elementManager->handleXmlChild(element, pChild, indent + 1);
    }
}
void SE_ElementGroupHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    SE_ElementGroup* element = new SE_ElementGroup;
    if(parent)
    {
        parent->addChild(element);
    }
    else
    {
        elementManager->setRoot(element);
    }
    element->setParent(parent);
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        int ival = -1;
        if(!strcmp(name, "id"))
        {
            element->setID(value);
        }
        else if(!strcmp(name, "x"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setLeft(ival);
            }
            else
            {
                LOGI("... parse x value error\n");
            }
        }
        else if(!strcmp(name, "y"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setTop(ival);
            }
            else
            {
                LOGI("... parse y value error\n");
            }
        }
        else if(!strcmp(name, "width"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setWidth(ival);
            }
            else
            {
                LOGI("... parse width value error\n");
            }
        }
        else if(!strcmp(name, "height"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setHeight(ival);
            }
            else
            {
                LOGI("... parse height value error\n");
            }
        }
        else if(!strcmp(name, "layer"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                element->setLocalLayer(ival);
            }
            else
            {
                LOGI("... parse layer value error\n");
            }
        }
        pAttribute = pAttribute->Next();
    }
    TiXmlNode* currNode = xmlElement;
    TiXmlNode* pChild = NULL;
    int i = 1;
    for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
    {
        elementManager->handleXmlChild(element, pChild, i++);
    }
}
void SE_AnimationHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        if(!strcmp(name , "FileName"))
        {
            elementManager->load(parent, value);
        }
        pAttribute = pAttribute->Next();
    }
}
void SE_TextureCoordAnimationHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    int ival = -1;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    SE_TextureCoordAnimation* anim = new SE_TextureCoordAnimation;
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        if(!strcmp(name, "FileName"))
        {
            anim->setImageDataID(value);
        }
        else if(!strcmp(name, "TimeMode"))
        {
            if(!strcmp(value, "SIMULATE_TIME"))
            {
                anim->setTimeMode(SE_Animation::SIMULATE);
            }
            else
            {
                anim->setTimeMode(SE_Animation::REAL);
            }

        }
        else if(!strcmp(name, "RunMode"))
        {
            if(!strcmp(value, "NOT_REPEAT"))
            {
                anim->setRunMode(SE_Animation::NOT_REPEAT);
            }
            else if(!strcmp(value, "REPEAT"))
            {
                anim->setRunMode(SE_Animation::REPEAT);
            }
            else if(!strcmp(value, "ONE_FRAME"))
            {
                anim->setRunMode(SE_Animation::ONE_FRAME);
            }
            else if(!strcmp(value, "REVERSE_NOT_REPEAT"))
            {
                anim->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
            }
            else if(!strcmp(value, "REVERSE_REPEAT"))
            {
                anim->setRunMode(SE_Animation::REVERSE_REPEAT);
            }
            else if(!strcmp(value, "REVERSE_ONE_FRAME"))
            {
                anim->setRunMode(SE_Animation::REVERSE_ONE_FRAME);
            }
        }
        else if(!strcmp(name, "Duration"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                anim->setDuration(ival);
            }
            else
            {
                LOGI("...parse animation duration error\n");
            }
        }
        pAttribute = pAttribute->Next();
    }
    parent->setAnimation(anim);
    TiXmlNode* currNode = xmlElement;
    TiXmlNode* childNode = NULL;
    for(childNode = currNode->FirstChild() ; childNode != NULL; childNode = childNode->NextSibling())
    {
        elementManager->handleXmlChild(parent, childNode, indent + 1);
    }
}
void SE_ImageDataHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    int ival = -1;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        if(!strcmp(name , "FileName"))
        {
            parent->setImageDataID(value);
        }
        else if(!strcmp(name, "x"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                parent->setImageDataX(ival);
            }
            else
            {
                LOGI("... parse image data x error\n");
            }
        }
        else if(!strcmp(name, "y"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                parent->setImageDataY(ival);
            }
            else
            {
                LOGI("... parse image data y error\n");
            }
        }
        else if(!strcmp(name, "width"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                parent->setImageDataWidth(ival);
            }
            else
            {
                LOGI("... parse image data width error\n");
            }
        }
        else if(!strcmp(name, "height"))
        {
            if(pAttribute->QueryIntValue(&ival) == TIXML_SUCCESS)
            {
                parent->setImageDataHeight(ival);
            }
            else
            {
                LOGI("... parse image data height error\n");
            }
        }
        pAttribute = pAttribute->Next();
    }
}
void SE_UnitWidthHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    SE_TextureCoordAnimation* anim = (SE_TextureCoordAnimation*)parent->getAnimation();
    if(anim == NULL)
        return;
    TiXmlNode* pChild;
    TiXmlNode* currNode = xmlElement;
    for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
    {
        int t = pChild->Type();
        if(t != TiXmlNode::TINYXML_TEXT)
            return;
        TiXmlText* pText = pChild->ToText();
        const char* value = pText->Value();
        int w = atoi(value);
        anim->setUnitWidth(w);
    }
}
void SE_UnitHeightHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    SE_TextureCoordAnimation* anim = (SE_TextureCoordAnimation*)parent->getAnimation();
    if(anim == NULL)
        return;
    TiXmlNode* pChild;
    TiXmlNode* currNode = xmlElement;
    for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
    {
        int t = pChild->Type();
        if(t != TiXmlNode::TINYXML_TEXT)
            return;
        TiXmlText* pText = pChild->ToText();
        const char* value = pText->Value();
        int h = atoi(value);
        anim->setUnitHeight(h);
    }
}
void SE_CoordHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    SE_TextureCoordAnimation* anim = (SE_TextureCoordAnimation*)parent->getAnimation();
    if(anim == NULL)
        return;
    TiXmlNode* pChild;
    TiXmlNode* currNode = xmlElement;
    int coord[2];
    int i = 0;
    std::string numrics = "0123456789";
    for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
    {
        int t = pChild->Type();
        if(t != TiXmlNode::TINYXML_TEXT)
            return;
        TiXmlText* pText = pChild->ToText();
        std::string value = pText->Value();
        std::string::size_type pos = 0;
        while((pos = value.find_first_of(numrics, pos)) != std::string::npos)
        {
            int v = value[pos] - '0';
            SE_ASSERT(i < 2);
            coord[i++] = v;
            pos++;
        }
    }
    anim->addCoord(SE_TextureCoordAnimation::Point(coord[0], coord[1]));
}
void SE_ShaderHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    if(!xmlElement)
        return;
    TiXmlAttribute* pAttribute = xmlElement->FirstAttribute();
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    std::string vertexShaderFilePath;
    std::string fragmentShaderFilePath;
    while(pAttribute)
    {
        const char* name = pAttribute->Name();
        const char* value = pAttribute->Value();
        if(!strcmp(name , "VertexShader"))
        {
            vertexShaderFilePath = std::string(resourceManager->getDataPath()) + SE_SEP + value;
        }
        else if(!strcmp(name, "FragmentShader"))
        {
            fragmentShaderFilePath = std::string(resourceManager->getDataPath()) + SE_SEP + value;
        }
        pAttribute = pAttribute->Next();
    }
    char* vertexShader;
    char* fragmentShader;
    int vertexShaderLen =0;
    int fragmentShaderLen = 0;
    SE_IO::readFileAll(vertexShaderFilePath.c_str(), vertexShader, vertexShaderLen);
    SE_IO::readFileAll(fragmentShaderFilePath.c_str(), fragmentShader, fragmentShaderLen);
    char* vs = new char[vertexShaderLen + 1];
    char* fs = new char[fragmentShaderLen + 1];
    memset(vs, 0, vertexShaderLen + 1);
    memset(fs, 0, fragmentShaderLen + 1);
    memcpy(vs, vertexShader, vertexShaderLen);
    memcpy(fs, fragmentShader, fragmentShaderLen);
    SE_ProgramDataID id("main_vertex_shader");
    //resourceManager->setShaderProgram(id, vs, fs);
    delete[] vertexShader;
    delete[] fragmentShader;
}
/////////////////////////////////
SE_ElementManager::SE_ElementManager()
{
    mRoot = NULL;
    addXmlElementHandler("Element", new SE_ElementHandler(this));
    addXmlElementHandler("ElementGroup", new SE_ElementGroupHandler(this));
    addXmlElementHandler("ImageData", new SE_ImageDataHandler(this));
    addXmlElementHandler("TextureCoordAnimation", new SE_TextureCoordAnimationHandler(this));
    addXmlElementHandler("Animation", new SE_AnimationHandler(this));
    addXmlElementHandler("UnitWidth", new SE_UnitWidthHandler(this));
    addXmlElementHandler("UnitHeight", new SE_UnitHeightHandler(this));
    addXmlElementHandler("Coord", new SE_CoordHandler(this));
    addXmlElementHandler("Shader", new SE_ShaderHandler(this));
}
SE_ElementManager::~SE_ElementManager()
{
    if(mRoot)
        delete mRoot;
}
void SE_ElementManager::handleElement(SE_Element* parent, const char* elementName, TiXmlElement* pElement, unsigned int indent)
{
    if(!pElement)
        return;
    SE_XmlElementHandler* pXmlElementHandler = mXmlElementHandlerMap[elementName];
    if(!pXmlElementHandler)
        return;
    pXmlElementHandler->handle(parent, pElement, indent);
}
void SE_ElementManager::handleText(SE_Element* parent, TiXmlText* text)
{}
void SE_ElementManager::handleXmlChild(SE_Element* parent, TiXmlNode* currNode, unsigned int indent)
{
    if(!currNode)
        return;
    TiXmlNode* pChild;
    TiXmlText* pText;
    int t = currNode->Type();
    int num = 0;
    switch(t)
    {
    case TiXmlNode::TINYXML_DOCUMENT:
        LOGI("...Document\n");
        for(pChild = currNode->FirstChild() ; pChild != NULL ; pChild = pChild->NextSibling())
        {
            handleXmlChild(parent, pChild, indent + 1);
        }
        break;
    case TiXmlNode::TINYXML_ELEMENT:
        LOGI("...Element[%s]\n", currNode->Value());
        handleElement(parent, currNode->Value(), currNode->ToElement(), indent + 1);
        break;
    case TiXmlNode::TINYXML_COMMENT:
        LOGI("...Comment:[%s]\n", currNode->Value());
        break;
    case TiXmlNode::TINYXML_TEXT:
        pText = currNode->ToText();
        LOGI("...Text: [%s]\n", pText->Value());
        handleText(parent, pText);
        break;
    case TiXmlNode::TINYXML_DECLARATION:
        LOGI("...Declaration\n");
        handleDeclaration(currNode->ToDeclaration());
        break;
    }
}
void SE_MountPointHandler::handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent)
{
    SE_ElementGroup* elementGroup = (SE_ElementGroup*)parent;

}
void SE_ElementManager::handleDeclaration(TiXmlDeclaration* decl)
{}
void SE_ElementManager::load(SE_Element* parent, const char* filePath)
{
    SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    std::string fileFullPath = std::string(resourceManager->getDataPath()) + "\\" + filePath;
    TiXmlDocument doc(fileFullPath.c_str());
    doc.LoadFile();
    if(doc.Error() && doc.ErrorId() ==TiXmlBase::TIXML_ERROR_OPENING_FILE)
    {
        LOGI("can not open xml file: %s\n", filePath);
        return;
    }
    handleXmlChild(parent, &doc);
    
}
void SE_ElementManager::addXmlElementHandler(const char* elementName, SE_XmlElementHandler* handler)
{
    _XmlElementHandlerMap::iterator it = mXmlElementHandlerMap.find(elementName);
    if(it == mXmlElementHandlerMap.end())
    {
        mXmlElementHandlerMap.insert(make_pair(std::string(elementName), handler));
    }
    else
    {
        SE_XmlElementHandler* h = it->second;
        delete it->second;
        it->second = handler;
    }
}
void SE_ElementManager::removeXmlElementHandler(const char* elementName)
{
    _XmlElementHandlerMap::iterator it = mXmlElementHandlerMap.find(elementName);
    if(it != mXmlElementHandlerMap.end())
    {
        delete it->second;
        mXmlElementHandlerMap.erase(it);
    }
}
SE_Spatial* SE_ElementManager::createSpatial()
{
    if(!mRoot)
        return NULL;
    SE_Spatial* spatial = mRoot->createSpatial(NULL);
    spatial->setLocalTranslate(SE_Vector3f(0, 0, 0));
    spatial->setLocalScale(SE_Vector3f(1, 1, 1));
    SE_Vector4f c1(1, 0, 0, 0);
    SE_Vector4f c2(0, -1, 0, 0);
    SE_Vector4f c3(0, 0, 1, 0);
    SE_Vector4f c4(-mRoot->getWidth() / 2, mRoot->getHeight() / 2, 0, 1);
    SE_Matrix4f localM;
    localM.setColumn(0, c1);
    localM.setColumn(1, c2);
    localM.setColumn(2, c3);
    localM.setColumn(3, c4);
    spatial->setPostMatrix(localM);
    return spatial;
}
class SE_FindByName : public SE_ElementTravel
{
public:
    SE_FindByName()
    {
        selectedElement = NULL;
    }
    void visit(SE_Element* e)
    {
        std::string name = e->getID().getStr();
        if(elementName == name)
            selectedElement = e;
    }
    SE_Element* selectedElement;
    std::string elementName;
};
SE_Element* SE_ElementManager::findByName(const char* name)
{
    SE_FindByName fbn;
    fbn.elementName = name;
    if(mRoot)
        mRoot->travel(&fbn);
    return fbn.selectedElement;
}
void SE_ElementManager::addElement(SE_Element* parent, SE_Element* child)
{}
void SE_ElementManager::removeElement(SE_Element* e)
{}
