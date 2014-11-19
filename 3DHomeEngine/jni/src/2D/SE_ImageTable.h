#ifndef SE_IMAGETABLE_H
#define SE_IMAGETABLE_H
#include "SE_ID.h"
#include "SE_TableManager.h"

/*
class SE_ImageTable
{
public:
    SE_ImageTable();
    ~SE_ImageTable();
    void setImageItem(const SE_ImageItemID& itemID, const SE_ImageDataID& imageDataID);
    SE_ImageDataID getImageDataID(const SE_ImageItemID& itemID);
    void removeImageItem(const SE_ImageItemID& itemID);
    bool isContain(const SE_ImageItemID& itemID);
private:
    typedef std::map<SE_ImageItemID, SE_ImageDataID> _ImageItemMap;
    _ImageItemMap mImageItemMap;
};
class SE_ImageTableSet
{
public:
    SE_ImageTableSet();
    ~SE_ImageTableSet();
    void setImageTable(const SE_ImageTableID& imageTableID, SE_ImageTable* imageTable);
    SE_ImageTable* getImageTable(const SE_ImageTableID& imageTableID);
    void removeImageTable(const SE_ImageTableID& imageTableID);
private:
    SE_ObjectManager<SE_ImageTableID, SE_ImageTable> mImageTableMap; 
};
class SE_ImageTableManager
{
public:
    SE_ImageTableManager();
    ~SE_ImageTableManager();
    void setImageTableSet(const SE_ImageTableSetID& tableSetID, SE_ImageTableSet* imageTableSet);
    SE_TableSet* getImageTableSet(const SE_ImageTableSetID& tableSetID);
    void removeImageTableSet(const SE_ImageTableSetID& imageTableSetID);
    SE_ImageDataID getImageDataID(const char* imageDataRef);
    bool isContain(const char* imageDataRef);
private:
    SE_ObjectManager<SE_ImageTableSetID, SE_ImageTableSet> mImageTableSetMap;
};
*/
#endif
