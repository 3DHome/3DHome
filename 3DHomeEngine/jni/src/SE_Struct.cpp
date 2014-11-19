#include "SE_DynamicLibType.h"
#include "SE_Struct.h"
#include "SE_MemLeakDetector.h"
SE_DataItem::SE_DataItem(SE_DataItem::DATA_ITEM_TYPE type)
{
    this->type = type;
    memset(&data, 0, sizeof(_DataType));
}
SE_DataItem::~SE_DataItem()
{
    switch(type)
    {
    case ASCII_ITEM:
        delete[] data.ascii;
        break;
    case UTF8_ITEM:
        delete[] data.utf8;
        break;
    case UNICODE_ITEM:
        delete[] data.unicode;
        break;
    case VECTOR3F_ITEM:
        delete data.vec3f;
        break;
    case VECTOR4F_ITEM:
        delete data.vec4f;
        break;
    case VECTOR2F_ITEM:
        delete data.vec2f;
        break;
    case VECTOR3I_ITEM:
        delete data.vec3i;
        break;
    case QUAT_ITEM:
        delete data.quat;
        break;
    case MATRIX3F_ITEM:
        delete data.mat3f;
        break;
    case MATRIX2F_ITEM:
        delete data.mat2f;
        break;
    case MATRIX4F_ITEM:
        delete data.mat4f;
        break;
    case VIRTUALDATA_ITEM:
        delete data.virtualData;
        break;
    }
}
SE_DataItem::SE_DataItem(const SE_DataItem& right)
{
    type = right.type;
    switch(type)
    {
    case CHAR_ITEM:
        data.c = right.data.c;
        break;
    case UCHAR_ITEM:
        data.uc = right.data.uc;
        break;
    case SHORT_ITEM:
        data.s = right.data.s;
        break;
    case USHORT_ITEM:
        data.us = right.data.us;
        break;
    case INT_ITEM:
        data.i = right.data.i;
        break;
    case UINT_ITEM:
        data.ui = right.data.ui;
        break;
    case FLOAT_ITEM:
        data.f = right.data.f;
        break;
    case ASCII_ITEM:
        if(right.data.ascii)
        {
            size_t len = strlen(right.data.ascii);
            data.ascii = new char[len + 1];
            memset(data.ascii, 0, len + 1);
            strncpy(data.ascii, right.data.ascii, len);
        }
        break;
    case UTF8_ITEM:
        if(right.data.utf8)
        {
            size_t len = strlen(right.data.utf8);
            data.utf8 = new char[len + 1];
            memset(data.utf8, 0, len + 1);
            strncpy(data.utf8, right.data.utf8, len);
        }
        break;
    case UNICODE_ITEM:
        if(right.data.unicode)
        {
            size_t len = wcslen(right.data.unicode);
            data.unicode = new wchar_t[len + 1];
            memset(data.unicode, 0, sizeof(wchar_t) * (len + 1));
            wcscpy(data.unicode, right.data.unicode);
        }
        break;
    case VECTOR3F_ITEM:
        data.vec3f = new SE_Vector3f;
        *data.vec3f = *right.data.vec3f;
        break;
    case VECTOR4F_ITEM:
        data.vec4f = new SE_Vector4f;
        *data.vec4f = *right.data.vec4f;
        break;
    case VECTOR2F_ITEM:
        data.vec2f = new SE_Vector2f;
        *data.vec2f = *right.data.vec2f;
        break;
    case VECTOR3I_ITEM:
        data.vec3i = new SE_Vector3i;
        *data.vec3i = *right.data.vec3i;
        break;
    case QUAT_ITEM:
        data.quat = new SE_Quat;
        *data.quat = *right.data.quat;
        break;
    case MATRIX3F_ITEM:
        data.mat3f = new SE_Matrix3f;
        *data.mat3f = *right.data.mat3f;
        break;
    case MATRIX2F_ITEM:
        data.mat2f = new SE_Matrix2f;
        *data.mat2f = *right.data.mat2f;
        break;
    case MATRIX4F_ITEM:
        data.mat4f = new SE_Matrix4f;
        *data.mat4f = *right.data.mat4f;
        break;
    case VIRTUALDATA_ITEM:
        data.virtualData = right.data.virtualData->clone();
        break;
    }
}
SE_DataItem& SE_DataItem::operator=(const SE_DataItem& right)
{
    if(this == &right)
        return *this;
    switch(type)
    {
    case CHAR_ITEM:
        break;
    case UCHAR_ITEM:
        break;
    case SHORT_ITEM:
        break;
    case USHORT_ITEM:
        break;
    case INT_ITEM:
        break;
    case UINT_ITEM:
        break;
    case FLOAT_ITEM:
        break;
    case ASCII_ITEM:
        if(data.ascii)
            delete[] data.ascii;
        break;
    case UTF8_ITEM:
        if(data.utf8)
            delete[] data.utf8;
        break;
    case UNICODE_ITEM:
        if(data.unicode)
            delete[] data.unicode;
        break;
    case VECTOR3F_ITEM:
        if(data.vec3f)
            delete data.vec3f;
        break;
    case VECTOR4F_ITEM:
        if(data.vec4f)
            delete data.vec4f;
        break;
    case VECTOR2F_ITEM:
        if(data.vec2f)
            delete data.vec2f;
        break;
    case VECTOR3I_ITEM:
        if(data.vec3i)
            delete data.vec3i;
        break;
    case QUAT_ITEM:
        if(data.quat)
            delete data.quat;
        break;
    case MATRIX3F_ITEM:
        if(data.mat3f)
            delete data.mat3f;
        break;
    case MATRIX2F_ITEM:
        if(data.mat2f)
            delete data.mat2f;
        break;
    case MATRIX4F_ITEM:
        if(data.mat4f)
            delete data.mat4f;
        break;
    case VIRTUALDATA_ITEM:
        if(data.virtualData)
            delete data.virtualData;
        break;
    }
    type = right.type;
    switch(type)
    {
    case CHAR_ITEM:
        data.c = right.data.c;
        break;
    case UCHAR_ITEM:
        data.uc = right.data.uc;
        break;
    case SHORT_ITEM:
        data.s = right.data.s;
        break;
    case USHORT_ITEM:
        data.us = right.data.us;
        break;
    case INT_ITEM:
        data.i = right.data.i;
        break;
    case UINT_ITEM:
        data.ui = right.data.ui;
        break;
    case FLOAT_ITEM:
        data.f = right.data.f;
        break;
    case ASCII_ITEM:
        if(right.data.ascii)
        {
            int len = strlen(right.data.ascii);
            data.ascii = new char[len + 1];
            memset(data.ascii, 0, len + 1);
            strncpy(data.ascii, right.data.ascii, len);
        }
        break;
    case UTF8_ITEM:
        if(right.data.utf8)
        {
            int len = strlen(right.data.utf8);
            data.utf8 = new char[len + 1];
            memset(data.utf8, 0, len + 1);
            strncpy(data.utf8, right.data.utf8, len);
        }
        break;
    case UNICODE_ITEM:
        if(right.data.unicode)
        {
            size_t len = wcslen(right.data.unicode);
            data.unicode = new wchar_t[len + 1];
            memset(data.unicode, 0, sizeof(wchar_t) * (len + 1));
            wcscpy(data.unicode, right.data.unicode);
        }
        break;
    case VECTOR3F_ITEM:
        data.vec3f = new SE_Vector3f;
        *data.vec3f = *right.data.vec3f;
        break;
    case VECTOR4F_ITEM:
        data.vec4f = new SE_Vector4f;
        *data.vec4f = *right.data.vec4f;
        break;
    case VECTOR2F_ITEM:
        data.vec2f = new SE_Vector2f;
        *data.vec2f = *right.data.vec2f;
        break;
    case VECTOR3I_ITEM:
        data.vec3i = new SE_Vector3i;
        *data.vec3i = *right.data.vec3i;
        break;
    case QUAT_ITEM:
        data.quat = new SE_Quat;
        *data.quat = *right.data.quat;
        break;
    case MATRIX3F_ITEM:
        data.mat3f = new SE_Matrix3f;
        *data.mat3f = *right.data.mat3f;
        break;
    case MATRIX2F_ITEM:
        data.mat2f = new SE_Matrix2f;
        *data.mat2f = *right.data.mat2f;
        break;
    case MATRIX4F_ITEM:
        data.mat4f = new SE_Matrix4f;
        *data.mat4f = *right.data.mat4f;
        break;
    case VIRTUALDATA_ITEM:
        data.virtualData = right.data.virtualData->clone();
        break;
    }
    return *this;
}
//////////////
SE_DataItemGroup::SE_DataItemGroup(int size)
{
    mDataItems.resize(size);
}
int SE_DataItemGroup::getDataItemCount()
{
    return mDataItems.size();
}
SE_DataItem SE_DataItemGroup::getDataItem(int i)
{
    if(i < 0 || i >= mDataItems.size())
        return SE_DataItem();
    return mDataItems[i];
}
void SE_DataItemGroup::setDataItem(int i , const SE_DataItem& di)
{
    if(i < 0 || i >= mDataItems.size())
        return ;
    mDataItems[i] = di;  
}
/////////
SE_StructItem::SE_StructItem(int dataItemCount) : mDataItemGroup(dataItemCount)
{
    
}
SE_StructItem::~SE_StructItem()
{
    /*
    switch(type)
    {
    case DATA_ITEM:
        delete data.item;
        break;
    case DATA_GROUP:
        delete data.group;
        break;
    }
    */
}
/*
SE_StructItem::SE_StructItem(const SE_StructItem& right)
{

    
}
SE_StructItem& SE_StructItem::operator=(const SE_StructItem&)
{

}
*/
#define SET_DATA_ITEM(di, index) \
{\
    mDataItemGroup.setDataItem(index, di); \
}
void SE_StructItem::setDataItem(char v, int index)
{
    SE_DataItem di(SE_DataItem::CHAR_ITEM);
    di.data.c = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(unsigned char v, int index)
{
    SE_DataItem di(SE_DataItem::UCHAR_ITEM);
    di.data.uc = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(short v, int index)
{
    SE_DataItem di(SE_DataItem::SHORT_ITEM);
    di.data.s = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(unsigned short v, int index)
{
    SE_DataItem di(SE_DataItem::USHORT_ITEM);
    di.data.us = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(int v, int index)
{
    SE_DataItem di(SE_DataItem::INT_ITEM);
    di.data.i = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(unsigned int v, int index)
{
    SE_DataItem di(SE_DataItem::UINT_ITEM);
    di.data.ui = v;
    SET_DATA_ITEM(di, index);
}

void SE_StructItem::setDataItem(float v, int index)
{
    SE_DataItem di(SE_DataItem::FLOAT_ITEM);
    di.data.f = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setAsciiDataItem(char* v, int index)
{
    SE_DataItem di(SE_DataItem::ASCII_ITEM);
    di.data.ascii = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setUtf8DataItem(char* v, int index)
{
    SE_DataItem di(SE_DataItem::UTF8_ITEM);
    di.data.utf8 = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setUnicodeDataItem(wchar_t* v, int index)
{
    SE_DataItem di(SE_DataItem::UNICODE_ITEM);
    di.data.unicode = v;
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Vector2f& v, int index)
{
    SE_DataItem di(SE_DataItem::VECTOR2F_ITEM);
    di.data.vec2f = new SE_Vector2f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Vector3f& v, int index)
{
    SE_DataItem di(SE_DataItem::VECTOR3F_ITEM);
    di.data.vec3f = new SE_Vector3f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Vector4f& v, int index)
{
    SE_DataItem di(SE_DataItem::VECTOR4F_ITEM);
    di.data.vec4f = new SE_Vector4f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Vector3i v, int index)
{
    SE_DataItem di(SE_DataItem::VECTOR3I_ITEM);
    di.data.vec3i = new SE_Vector3i(v);
    SET_DATA_ITEM(di, index);    
}
void SE_StructItem::setDataItem(const SE_Quat& v, int index)
{
    SE_DataItem di(SE_DataItem::QUAT_ITEM);
    di.data.quat = new SE_Quat(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Matrix2f& v, int index)
{
    SE_DataItem di(SE_DataItem::MATRIX2F_ITEM);
    di.data.mat2f = new SE_Matrix2f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Matrix3f& v, int index)
{
    SE_DataItem di(SE_DataItem::MATRIX3F_ITEM);
    di.data.mat3f = new SE_Matrix3f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(const SE_Matrix4f& v, int index)
{
    SE_DataItem di(SE_DataItem::MATRIX4F_ITEM);
    di.data.mat4f = new SE_Matrix4f(v);
    SET_DATA_ITEM(di, index);
}
void SE_StructItem::setDataItem(SE_VirtualData* data, int index)
{
    SE_DataItem di(SE_DataItem::VIRTUALDATA_ITEM);
    di.data.virtualData = data;
    SET_DATA_ITEM(di, index);
}
////////////////////////////
SE_Struct::SE_Struct(int structItemSize) : mStructItems(structItemSize, (SE_StructItem *)NULL)
{
}
SE_Struct::~SE_Struct()
{
    for(int i = 0 ; i < mStructItems.size() ; i++)
    {
        SE_StructItem* item = mStructItems[i];
        if(item)
            delete item;
    }
    
}
int SE_Struct::getCount()
{
    return mStructItems.size();
}
SE_StructItem* SE_Struct::getStructItem(int index)
{
    if(index < 0 || index >= mStructItems.size())
        return NULL;
    return mStructItems[index];
}
void SE_Struct::setStructItem(int index, SE_StructItem* si)
{
    if(index < 0 || index >= mStructItems.size())
        return;
    SE_StructItem* item = mStructItems[index];
    if(item)
        delete item;
    mStructItems[index] = si;
}
