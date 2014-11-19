#ifndef SE_BONE_H
#define SE_BONE_H
#include "SE_Matrix.h"
#include <string>
#include <list>
#include <vector>
class SE_ENTRY SE_Bone
{
public:
    SE_Bone();
    ~SE_Bone();
    void setParent(SE_Bone* parent)
    {
        mParent = parent;
    }
    SE_Bone* getParent()
    {
        return mParent;
    }
    void setName(const char* name)
    {
        mName = name;
    }
    std::string getName()
    {
        return mName;
    }
    void setMatrixArray(float* data, int num);
    SE_Matrix4f getMatrix(int index)
    {
        int num = mMatrixArray.size();
        if(index < 0 || index >= num)
        {
            return mMatrixBase;
        }
        return mMatrixArray[index];
    }
    int getMatrixNum()
    {
        return mMatrixArray.size();
    }
    void setBaseMatrix(const SE_Matrix4f& m)
    {
        mMatrixBase = m;
    }
    SE_Matrix4f getBaseMatrix()
    {
        return mMatrixBase;
    }
    void addChild(SE_Bone* b)
    {
        mChildrenList.push_back(b);
    }
private:
    typedef std::list<SE_Bone*> _ChildrenList;
    std::string mName;
    _ChildrenList mChildrenList;
    SE_Bone* mParent;
    std::vector<SE_Matrix4f> mMatrixArray;
    SE_Matrix4f mMatrixBase;
};
#endif
