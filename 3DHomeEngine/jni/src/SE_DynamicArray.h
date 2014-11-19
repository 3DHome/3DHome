#ifndef SE_DYNAMICARRAY_H
#define SE_DYNAMICARRAY_H
template <typename T>
class SE_DynamicArray
{
public:
    enum {SE_NO_ERROR, SE_OVERFLOW, SE_INDEX_ERROR};
    //size must > 1
    SE_DynamicArray(size_t size, size_t maxSize);
    SE_DynamicArray(const SE_DynamicArray& right);
    SE_DynamicArray& operator=(const SE_DynamicArray& right);
    ~SE_DynamicArray();
    T& operator[](size_t index);
    const T& operator[](size_t index) const;
    size_t size() const
    {
        return mSize;
    }
    void setInvalidValue(const T& v)
    {
        mInvalid = v;
    }
    T getInvalidValue() const
    {
        return mInvalid;
    }
    int getError() const
    {
        return mError;
    }
    void expand();
    
private:
    T* mArray;
    size_t mSize;
    size_t mMaxSize;
    T mInvalid;
    int mError;
};
template <typename T>
SE_DynamicArray<T>::SE_DynamicArray(size_t size, size_t maxsize) : mArray(NULL), mSize(0), mMaxSize(0), mError(0)
{
    if(size < 1)
        return;
    if(maxsize < 1)
        return;
    mArray = new T[size];
    mSize = size;
    mMaxSize = maxsize;
}
template <typename T>
SE_DynamicArray<T>::~SE_DynamicArray()
{
    if(mArray)
        delete[] mArray;
}
template <typename T>
void SE_DynamicArray<T>::expand()
{
    size_t size = mSize + (mSize * 3) / 4;
    if(size > mMaxSize)
    {
        mError = SE_OVERFLOW;
        return;
    }
    T* newArray = new T[size];
    if(!newArray)
    {
        mError = SE_OVERFLOW;
        return;
    }
    for(size_t i = 0 ; i < mSize ; i++)
    {
        newArray[i] = mArray[i];
    } 
    T* oldArray = mArray;
    mArray = newArray;
    mSize = size;
    delete[] oldArray;
}
template <typename T>
T& SE_DynamicArray<T>::operator[](size_t index)
{
    if(index >= 0 && index < mSize)
    {
        return mArray[index];
    }
    else
    {
        mError = SE_INDEX_ERROR;
        return mInvalid;
    }
}
template <typename T>
const T& SE_DynamicArray<T>::operator[](size_t index) const
{
    if(index < 0 || index >= mSize)
    {
        mError = SE_INDEX_ERROR;
        return mInvalid;
    }
    return mArray[index];
}
template <typename T>
SE_DynamicArray<T>::SE_DynamicArray(const SE_DynamicArray& right)
{
    mSize = right.size();
    mMaxSize = right.size();
    mError = right.mError;
    mInvalid = right.mInvalid;
    mArray = new T[mSize];
    if(!mArray)
        return;
    for(size_t i = 0 ; i < mSize ; i++)
    {
        mArray[i] = right.mArray[i];
    }
}
template <typename T>
SE_DynamicArray<T>& SE_DynamicArray<T>::operator=(const SE_DynamicArray& right)
{
    T* newArray = new T[right.mSize];
    if(!newArray)
        return *this;
    if(mArray)
        delete[] mArray;
    mArray = newArray;
    mSize = right.mSize;
    mMaxSize = right.mMaxSize;
    mError = right.mError;
    mInvalid = right.mInvalid;
    for(size_t i = 0 ; i < mSize ; i++)
    {
        mArray[i] = right.mArray[i];
    }
    return *this;
}
#endif
