#ifndef SE_REFBASE_H
#define SE_REFBASE_H
#include "SE_Mutex.h"
#define SE_COMPARE(_op_)									\
inline bool operator _op_ (const SE_sp<T>& o) const {	\
	return m_ptr _op_ o.m_ptr;						\
}        
class SE_ENTRY SE_RefBase
{
public:
	void incStrong(void* id);
	void decStrong(void* id);
	//use for debug, please don't use it
	int getStrongCount() const
	{
		return mRefCount;
	}
	//please don't invoke delete function
	//please use decStrong
	virtual ~SE_RefBase();
protected:
	SE_RefBase(bool threadSafe = true);

private:
	SE_RefBase(const SE_RefBase& ref);
	SE_RefBase& operator=(const SE_RefBase& ref);		
private:
	volatile int mRefCount;
	SE_Mutex* mMutex;
};
template <typename T>
class SE_sp
{
public:

	inline SE_sp() : m_ptr(0) { }

	SE_sp(T* other);
	SE_sp(const SE_sp<T>& other);
	template<typename U> SE_sp(U* other);
	template<typename U> SE_sp(const SE_sp<U>& other);

	~SE_sp();

	// Assignment

	SE_sp& operator = (T* other);
	SE_sp& operator = (const SE_sp<T>& other);

	template<typename U> SE_sp& operator = (const SE_sp<U>& other);
	template<typename U> SE_sp& operator = (U* other);

	// Reset

	void clear();

	// Accessors

	inline  T&      operator* () const  { return *m_ptr; }
	inline  T*      operator-> () const { return m_ptr;  }
	inline  T*      get() const         { return m_ptr; }

	// Operators
    
	SE_COMPARE(==)
	SE_COMPARE(!=)
	SE_COMPARE(>)
	SE_COMPARE(<)
	SE_COMPARE(<=)
	SE_COMPARE(>=)

private:    
	T*              m_ptr;
};

template<typename T>
	SE_sp<T>::SE_sp(T* other)
	: m_ptr(other)
{
	if (other) other->incStrong(this);
}

template<typename T>
	SE_sp<T>::SE_sp(const SE_sp<T>& other)
	: m_ptr(other.m_ptr)
{
	if (m_ptr) m_ptr->incStrong(this);
}

template<typename T> template<typename U>
	SE_sp<T>::SE_sp(U* other) : m_ptr(other)
{
	if (other) other->incStrong(this);
}

template<typename T> template<typename U>
	SE_sp<T>::SE_sp(const SE_sp<U>& other)
	: m_ptr(other.m_ptr)
{
	if (m_ptr) m_ptr->incStrong(this);
}

template<typename T>
	SE_sp<T>::~SE_sp()
{
	if (m_ptr) m_ptr->decStrong(this);
}

template<typename T>
	SE_sp<T>& SE_sp<T>::operator = (const SE_sp<T>& other) {
	if (other.m_ptr) other.m_ptr->incStrong(this);
	if (m_ptr) m_ptr->decStrong(this);
	m_ptr = other.m_ptr;
	return *this;
}

template<typename T>
	SE_sp<T>& SE_sp<T>::operator = (T* other)
	{
		if (other) other->incStrong(this);
		if (m_ptr) m_ptr->decStrong(this);
		m_ptr = other;
		return *this;
	}

template<typename T>
	template<typename U>
	SE_sp<T>& SE_sp<T>::operator = (const SE_sp<U>& other)
	{
		if (other.m_ptr) other.m_ptr->incStrong(this);
		if (m_ptr) m_ptr->decStrong(this);
		m_ptr = other.m_ptr;
		return *this;
	}

template<typename T>
	template<typename U>
	SE_sp<T>& SE_sp<T>::operator = (U* other)
	{
		if (other) other->incStrong(this);
		if (m_ptr) m_ptr->decStrong(this);
		m_ptr = other;
		return *this;
	}


template<typename T>
void SE_sp<T>::clear()
{
	if (m_ptr) {
		m_ptr->decStrong(this);
		m_ptr = 0;
	}
}
#endif
