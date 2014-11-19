#ifndef SE_BUFFER_H
#define SE_BUFFER_H
#include <string>
#include <list>
class SE_Vector2f;
class SE_Vector3f;
class SE_Vector4f;
class SE_Vector3i;
class SE_Matrix2f;
class SE_Matrix3f;
class SE_Matrix4f;
class SE_Quat;
class SE_ENTRY SE_BufferOutput
{
public:
    enum {DEFAULT_LEN = 1024};
    SE_BufferOutput(bool netOrder = false);
    SE_BufferOutput(int size, bool netOrder = false);
    ~SE_BufferOutput();
    void writeByte(char c);
    void writeInt(int i);
    void writeShort(short s);
    void writeFloat(float f);
    void writeVector2f(const SE_Vector2f& v);
    void writeVector3f(const SE_Vector3f& v);
    void writeVector3i(const SE_Vector3i& v);
    void writeVector4f(const SE_Vector4f& v);
    void writeMatrix2f(const SE_Matrix2f& m);
    void writeMatrix3f(const SE_Matrix3f& m);
    void writeMatrix4f(const SE_Matrix4f& m);
    void writeQuat(const SE_Quat& q);
    void writeString(const char* str, int count);
    void writeString(const char* str);
    void writeBytes(const char* cb, int count);
    void writeIntArray(int* ia, int count);
    void writeShortArray(short* sa, int count);
    void writeFloatArray(float* fa, int count);
    const char* getData();
    int getDataLen();
private:
    char* mData;
    int mLen;
    int mOffset;
    bool mNetOrder;
};
class SE_ENTRY SE_BufferInput
{
public:
    SE_BufferInput(char* data, int len, bool netOrder = false, bool own = true);
    ~SE_BufferInput();
    bool hasMore();
    char readByte();
    short readShort();
    int readInt();
    float readFloat();
    SE_Vector2f readVector2f();
    SE_Vector3f readVector3f();
    SE_Vector3i readVector3i();
    SE_Vector4f readVector4f();
    SE_Matrix2f readMatrix2f();
    SE_Matrix3f readMatrix3f();
    SE_Matrix4f readMatrix4f();
    SE_Quat readQuat();
    //this is the string ended by '\0', user need to release the memory returned
    std::string readString();
    std::string readString(int len);
    char* readBytes(int len);
    bool readBytes(char* out, int len);

    int getDataLen()
    {
        return mLen;
    }
    void reset()
    {
        mOffset = 0;
    }
	void pushLocation();
	int popLocation();
private:
    char* mData;
    int mLen;
    int mOffset;
    bool mNetOrder;
    bool mOwn;
	std::list<int> mLocationStack;
};
#endif
