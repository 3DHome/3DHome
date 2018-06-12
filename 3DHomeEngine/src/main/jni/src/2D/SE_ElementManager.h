#ifndef SE_ELEMENTMANAGER_H
#define SE_ELEMENTMANAGER_H
#include <wchar.h>
#include <string>
#include <map>
#include "tinyxml.h"
#include "SE_Element.h"
class SE_Spatial;
class SE_ElementManager;
class SE_ENTRY SE_XmlElementHandler
{
public:
    virtual ~SE_XmlElementHandler() {}
    SE_XmlElementHandler(SE_ElementManager* em)
    {
        elementManager = em;
    }
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent) = 0;
    SE_ElementManager* elementManager;
};
class SE_ENTRY SE_ElementHandler : public SE_XmlElementHandler
{
public:
    SE_ElementHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_ElementGroupHandler : public SE_XmlElementHandler
{
public:
    SE_ElementGroupHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_AnimationHandler : public SE_XmlElementHandler
{
public:
    SE_AnimationHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_TextureCoordAnimationHandler : public SE_XmlElementHandler
{
public:
    SE_TextureCoordAnimationHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_ImageDataHandler : public SE_XmlElementHandler
{
public:
    SE_ImageDataHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_UnitWidthHandler : public SE_XmlElementHandler
{
public:
    SE_UnitWidthHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
   virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent); 
};
class SE_ENTRY SE_UnitHeightHandler : public SE_XmlElementHandler
{
public:
    SE_UnitHeightHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);  
};
class SE_ENTRY SE_CoordHandler : public SE_XmlElementHandler
{
public:
    SE_CoordHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_ShaderHandler : public SE_XmlElementHandler
{
public:
    SE_ShaderHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent);
};
class SE_ENTRY SE_MountPointHandler : public SE_XmlElementHandler
{
public:
    SE_MountPointHandler(SE_ElementManager* em) : SE_XmlElementHandler(em)
    {}
    virtual void handle(SE_Element* parent, TiXmlElement* xmlElement, unsigned int indent); 
};
class SE_ENTRY SE_ElementManager
{
public:
    SE_ElementManager();
    ~SE_ElementManager();
    void load(SE_Element* parent, const char* filePath);
    SE_Spatial* createSpatial();
    void addElement(SE_Element* parent, SE_Element* child);
    void removeElement(SE_Element* e);
    SE_Element* getRoot() const
    {
        return mRoot;
    }
    void setRoot(SE_Element* root)
    {
        if(mRoot)
            delete mRoot;
        mRoot = root;
    }
    SE_Element* findByName(const char* name);
    void addXmlElementHandler(const char* elementName, SE_XmlElementHandler* handler);
    void removeXmlElementHandler(const char* elementName);
    void handleXmlChild(SE_Element* parent, TiXmlNode* currNode, unsigned int indent = 0);
    void setPath(const char* path)
    {
        mPath = path;
    }
    void handleElement(SE_Element* parent, const char* elementName, TiXmlElement* pElement, unsigned int indent);
    void handleText(SE_Element* parent, TiXmlText* text);
    void handleDeclaration(TiXmlDeclaration* decl);
private:
    SE_ElementManager(const SE_ElementManager&);
    SE_ElementManager& operator=(const SE_ElementManager&);
private:
    SE_Element* mRoot;
    typedef std::map<std::string, SE_XmlElementHandler*> _XmlElementHandlerMap;
    _XmlElementHandlerMap mXmlElementHandlerMap;
    std::string mPath;
};
#endif
