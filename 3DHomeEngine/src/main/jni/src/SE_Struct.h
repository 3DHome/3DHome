#ifndef SE_STRUCT_H
#define SE_STRUCT_H
#include <wchar.h>
#include <vector>
#include <string>
#include "SE_Vector.h"
#include "SE_Quat.h"
#include "SE_Matrix.h"
class SE_Spatial;
class SE_ENTRY SE_VirtualData
{
public:
    virtual ~SE_VirtualData() {};
    virtual SE_VirtualData* clone() = 0;
};
class SE_ENTRY SE_StdString : public SE_VirtualData
{
public:
    ~SE_StdString() {};
    SE_VirtualData* clone()
    {
        SE_StdString* str = new SE_StdString;
        str->data = data;
        return str;
    }
    std::string data;
};
class SE_ENTRY SE_SpatialData : public SE_VirtualData
{
public:
    SE_VirtualData* clone()
    {
        SE_SpatialData* s = new SE_SpatialData;
        s->spatial = spatial;
        return s;
    }
    SE_Spatial* spatial;
};
struct SE_ENTRY SE_DataItem
{
public:
    
    enum DATA_ITEM_TYPE {INVALID, CHAR_ITEM, UCHAR_ITEM, SHORT_ITEM, USHORT_ITEM, INT_ITEM, UINT_ITEM, FLOAT_ITEM,ASCII_ITEM, UTF8_ITEM, UNICODE_ITEM,
          VECTOR3F_ITEM, VECTOR4F_ITEM, VECTOR2F_ITEM, VECTOR3I_ITEM, QUAT_ITEM, MATRIX3F_ITEM, MATRIX2F_ITEM, MATRIX4F_ITEM, VIRTUALDATA_ITEM};
    DATA_ITEM_TYPE type;
    union _DataType
    {
        char c;
        unsigned char uc;
        short s;
        unsigned short us;
        int i;
        unsigned int ui;
        float f;
        char* ascii;
        char* utf8;
        wchar_t* unicode;
        SE_Vector3f* vec3f;
        SE_Vector2f* vec2f;
        SE_Vector4f* vec4f;
        SE_Vector3i* vec3i;
        SE_Quat* quat;
        SE_Matrix2f* mat2f;
        SE_Matrix3f* mat3f;
        SE_Matrix4f* mat4f;
        SE_VirtualData* virtualData;
    } data;
    SE_DataItem(DATA_ITEM_TYPE type = INVALID);
    virtual ~SE_DataItem();
    SE_DataItem(const SE_DataItem&);
    SE_DataItem& operator=(const SE_DataItem&);
};
class SE_ENTRY SE_DataItemGroup
{
public:
    SE_DataItemGroup(int size);
    int getDataItemCount();
    SE_DataItem getDataItem(int i);
    void setDataItem(int i , const SE_DataItem& di);
private:
    std::vector<SE_DataItem> mDataItems;
};
class  SE_ENTRY SE_StructItem
{
public:
    SE_StructItem(int dataItemCount);
    ~SE_StructItem();
    //SE_StructItem(const SE_StructItem&);
    //SE_StructItem& operator=(const SE_StructItem&);
    void setDataItem(char v, int index = 0);
    void setDataItem(unsigned char v, int index = 0);
    void setDataItem(short v, int index = 0);
    void setDataItem(unsigned short v, int index = 0);
    void setDataItem(int v, int index = 0);
    void setDataItem(unsigned int v, int index = 0);
    void setDataItem(float v, int index = 0);
    //v will be owned by SE_StructItem
    //it will be deleted by ~SE_StructItem
    void setAsciiDataItem(char* v, int index = 0);
    void setUtf8DataItem(char* v, int index = 0);
    void setUnicodeDataItem(wchar_t* v, int index = 0);
    void setDataItem(const SE_Vector2f& v, int index = 0);
    void setDataItem(const SE_Vector3f& v, int index = 0);
    void setDataItem(const SE_Vector4f& v, int index = 0);
    void setDataItem(const SE_Vector3i v, int index = 0);
    void setDataItem(const SE_Quat& v, int index = 0);
    void setDataItem(const SE_Matrix2f& v, int index = 0);
    void setDataItem(const SE_Matrix3f& v, int index = 0);
    void setDataItem(const SE_Matrix4f& v, int index = 0);
    void setDataItem(SE_VirtualData* data, int index = 0);
    SE_DataItem getDataItem(int index)
    {
        return mDataItemGroup.getDataItem(index);
    }
    int getDataItemCount()
    {
        return mDataItemGroup.getDataItemCount();
    }
private:
    SE_DataItemGroup mDataItemGroup;
};
class SE_ENTRY SE_Struct
{
public:
    SE_Struct(int structItemSize);
    ~SE_Struct();
    int getCount();
    SE_StructItem* getStructItem(int index);
    void setStructItem(int index, SE_StructItem* si);
private:
    SE_Struct(const SE_Struct& );
    SE_Struct& operator=(const SE_Struct&);
private:
    std::vector<SE_StructItem*> mStructItems;
};
#endif
