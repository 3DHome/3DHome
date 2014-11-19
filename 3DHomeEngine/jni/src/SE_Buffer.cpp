#include "SE_DynamicLibType.h"
#include "SE_Buffer.h"
#include "SE_Vector.h"
#include "SE_Matrix.h"
#include "SE_Quat.h"
#include "SE_Utils.h"
#include "SE_Log.h"
#include <string.h>
#include "SE_Application.h"
#include "SE_MemLeakDetector.h"
SE_BufferOutput::SE_BufferOutput(bool netOrder)
{
    mLen = DEFAULT_LEN;
    mOffset = 0;
    mNetOrder = netOrder;
    mData = new char[mLen];
}
SE_BufferOutput::SE_BufferOutput(int size, bool netOrder)
{
    mLen = size;
    mNetOrder = netOrder;
    mOffset = 0;
    mData = new char[mLen];
}
SE_BufferOutput::~SE_BufferOutput()
{
    delete[] mData;
}
void SE_BufferOutput::writeByte(char c)
{
    writeBytes(&c, 1);
}
void SE_BufferOutput::writeInt(int i)
{
    int outI = i;
    if(mNetOrder)
    {
        outI = SE_Util::host2NetInt32(i);
    }
    writeBytes((char*)&outI, 4);
}
void SE_BufferOutput::writeShort(short s)
{
    short outS = s;
    if(mNetOrder)
    {
        outS = SE_Util::host2NetInt16(s);
    }
    writeBytes((char*)&outS, 2);
}
void SE_BufferOutput::writeFloat(float f)
{
    int* outF = (int*)&f;
    float tmp;
    if(mNetOrder)
    {
        tmp = SE_Util::host2NetInt32(*outF);
        outF = (int*)&tmp;
    }
    writeBytes((char*)outF, 4);
}
void SE_BufferOutput::writeVector2f(const SE_Vector2f& v)
{
    for(int i = 0 ; i < 2 ; i++)
    {
        writeFloat(v.d[i]);
    }
}
void SE_BufferOutput::writeVector3f(const SE_Vector3f& v)
{
    for(int i = 0 ; i < 3 ; i++)
    {
        writeFloat(v.d[i]);
    }
}
void SE_BufferOutput::writeVector3i(const SE_Vector3i& v)
{
    for(int i = 0 ; i < 3 ; i++)
    {
        writeInt(v.d[i]);
    }
}
void SE_BufferOutput::writeVector4f(const SE_Vector4f& v)
{
    for(int i = 0 ; i < 4 ; i++)
    {
        writeFloat(v.d[i]);
    }
}
void SE_BufferOutput::writeMatrix2f(const SE_Matrix2f& m)
{
    for(int i = 0 ; i < 2 ; i++)
    {
        for(int j = 0 ; j < 2 ; j++)
            writeFloat(m.get(i, j));
    }
}
void SE_BufferOutput::writeMatrix3f(const SE_Matrix3f& m)
{
    for(int i = 0 ; i < 3 ; i++)
    {
        for(int j = 0 ; j < 3 ; j++)
            writeFloat(m.get(i, j));
    }
}
void SE_BufferOutput::writeMatrix4f(const SE_Matrix4f& m)
{
    for(int i = 0 ; i < 4 ; i++)
    {
        for(int j = 0 ; j < 4 ; j++)
            writeFloat(m.get(i, j));
    }
}
void SE_BufferOutput::writeQuat(const SE_Quat& q)
{
    writeVector4f(q.toVector4f());
}
void SE_BufferOutput::writeString(const char* str, int count)
{
    writeInt(count);
    writeBytes(str, count);
}
void SE_BufferOutput::writeString(const char* str)
{
    int len = strlen(str);
    writeString(str, len);
}
void SE_BufferOutput::writeBytes(const char* cb, int count)
{
    if((mOffset + count) <= mLen)
    {
        memcpy(mData + mOffset, cb, count);
        mOffset += count;
    }
    else
    {
        int newLen = mLen + count + DEFAULT_LEN * 3 / 4;
        char* newData = new char[newLen];
        if(!newData)
            return;
        memcpy(newData, mData, mOffset);
        delete[] mData;
        mLen = newLen;
        mData = newData;
        memcpy(mData + mOffset, cb, count);
        mOffset += count;        
    }
}
void SE_BufferOutput::writeIntArray(int* ia, int count)
{
    for(int i = 0 ; i < count ; i++)
    {
        writeInt(ia[i]);
    }
}
void SE_BufferOutput::writeShortArray(short* sa, int count)
{
    for(int i = 0 ; i < count ; i++)
    {
        writeShort(sa[i]);
    }
}
void SE_BufferOutput::writeFloatArray(float* fa, int count)
{
    for(int i = 0 ; i < count ; i++)
    {
        writeFloat(fa[i]);
    }
}
const char* SE_BufferOutput::getData()
{
    return mData;
}
int SE_BufferOutput::getDataLen()
{
    return mOffset;
}
////////////////////////////////////////////////////////////////////////////
SE_BufferInput::SE_BufferInput(char* data, int len, bool netOrder, bool own)
{
    mData = data;
    mLen = len;
    mOffset = 0;
    mNetOrder = netOrder;
    mOwn = own;
}
SE_BufferInput::~SE_BufferInput()
{
    if(mOwn && mData)
    {
        delete[] mData;
    }
}
bool SE_BufferInput::hasMore()
{
    return mOffset < mLen;
}
void SE_BufferInput::pushLocation()
{
	mLocationStack.push_back(mOffset);
}
int SE_BufferInput::popLocation()
{
	if(mLocationStack.empty())
	{
        if(SE_Application::getInstance()->SEHomeDebug)
		LOGI("## buffer input poplocation error ##\n");
		return -1;
	}
	int location = mLocationStack.back();
	mLocationStack.pop_back();
	return mOffset - location;
}
char SE_BufferInput::readByte()
{
    return mData[mOffset++];
}
short SE_BufferInput::readShort()
{
    short out = 0xFFFF;
    readBytes((char*)&out, sizeof(short));
    if(mNetOrder)
    {
        out = SE_Util::net2HostInt16(out);
    }    
    return out;
}
int SE_BufferInput::readInt()
{
    int out = 0xFFFFFFFF;
    readBytes((char*)&out, sizeof(int));
    if(mNetOrder)
    {
        out = SE_Util::net2HostInt32(out);
    }
    return out;
}
float SE_BufferInput::readFloat()
{
    float out = 0xFFFFFFFF;
    readBytes((char*)&out, sizeof(float));
    if(mNetOrder)
    {
        int i = *(int*)&out;
        i = SE_Util::net2HostInt32(i);
        out = (float)i;
    }
    return out;
}
SE_Vector2f SE_BufferInput::readVector2f()
{
    SE_Vector2f out;
    for(int i = 0 ; i < 2 ; i++)
    {
        out.d[i] = readFloat();
    }
    return out;
}
SE_Vector3f SE_BufferInput::readVector3f()
{
    SE_Vector3f out;
    for(int i = 0 ; i < 3 ; i++)
    {
        out.d[i] = readFloat();
    }
    return out;
}
SE_Vector3i SE_BufferInput::readVector3i()
{
    SE_Vector3i out;
    for(int i = 0 ; i < 3 ; i++)
        out.d[i] = readInt();
    return out;
}
SE_Vector4f SE_BufferInput::readVector4f()
{
    SE_Vector4f out;
    for(int i = 0 ; i < 4 ; i++)
    {
        out.d[i] = readFloat();
    }
    return out;
}
SE_Matrix2f SE_BufferInput::readMatrix2f()
{
    SE_Matrix2f m;
    for(int i = 0 ; i < 2 ; i++)
    {
        for(int j = 0 ; j < 2 ; j++)
        {
            m.set(i, j, readFloat());
        }

    }
    return m;
}
SE_Matrix3f SE_BufferInput::readMatrix3f()
{
    SE_Matrix3f m;
    for(int i = 0 ; i < 3 ; i++)
    {
        for(int j = 0 ; j < 3 ; j++)
        {
            m.set(i, j, readFloat());
        }

    }
    return m;
}
SE_Matrix4f SE_BufferInput::readMatrix4f()
{
    SE_Matrix4f m;
    for(int i = 0 ; i < 4 ; i++)
    {
        for(int j = 0 ; j < 4 ; j++)
        {
            m.set(i, j, readFloat());
        }

    }
    return m;
}
SE_Quat SE_BufferInput::readQuat()
{
    SE_Vector4f v = readVector4f();
    return SE_Quat(v.x, v.y, v.z, v.w);
}
std::string SE_BufferInput::readString(int len)
{
    char* data = new char[len + 1];
    if(data)
    {
        memset(data, 0, len + 1);
        readBytes(data, len);
        std::string ret(data);
        delete[] data;
        return ret;
    }
    else
        return std::string();    
}
std::string SE_BufferInput::readString()
{
    int len = readInt();
    return readString(len);
    /*
    char* data = new char[len + 1];
    if(data)
    {
        memset(data, 0, len + 1);
        readBytes(data, len);
        std::string ret(data);
        delete[] data;
        return ret;
    }
    else
        return std::string();
        */
}
bool SE_BufferInput::readBytes(char* out, int len)
{
    bool exceed = (mOffset + len) > mLen;
    int copyLen = len;
    if(exceed)
    {
        copyLen = mLen - mOffset;
    }
    if(copyLen)
        memcpy(out, mData + mOffset, copyLen);
    mOffset += copyLen;
    if(copyLen < len)
        return false;
    else
        return true;
}
