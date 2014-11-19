#ifndef SE_DELAYDESTROY_H
#define SE_DELAYDESTROY_H
class SE_ENTRY SE_DelayDestroy
{
public:
    virtual ~SE_DelayDestroy() {}
    virtual void destroy() = 0;
    bool operator==(const SE_DelayDestroy& right) const
    {
        return eq(right);
    }
    bool operator !=(const SE_DelayDestroy& right) const
    {
        return !this->operator==(right);
    }
    virtual bool eq(const SE_DelayDestroy& right) const = 0;
    virtual void* getPtr() const = 0;
};
template <typename T>
class SE_ENTRY SE_DelayDestroyPointer : public SE_DelayDestroy
{
public:
    SE_DelayDestroyPointer(T* p)
    {
        pointer = p;
    }
    void destroy()
    {
        delete pointer;
    }
protected:
    bool eq(const SE_DelayDestroy& right) const
    {
        return pointer == right.getPtr();
    }
    void* getPtr() const
    {
        return pointer;
    }
private:
    T* pointer;
};
template <typename T>
class SE_ENTRY SE_DelayDestroyArray : public SE_DelayDestroy
{
public:
    SE_DelayDestroyArray(T* p)
    {
        pointer = p;
    }
    void destroy()
    {
        if(pointer)
        delete[] pointer;
    }
protected:
    bool eq(const SE_DelayDestroy& right)
    {
        return pointer == right.getPtr();
    }
    void* getPtr() const
    {
        return pointer;
    }
private:
    T* pointer;
};
#endif
