#ifndef SE_CAMERABESTPOSITION_H
#define SE_CAMERABESTPOSITION_H

#include <vector>
#include "SE_Vector.h"

class SE_ENTRY SE_CameraBestPosition
{
public:
    SE_Vector3f mCameraPos;
    SE_Vector3f mCameraTargetPos;
    std::string mCamraName;
    std::string mCameraTargetName;
    std::string mCameraType;
};

class SE_CameraPositionList
{
public:
    ~SE_CameraPositionList()
    {
        for(int i = 0; i < mPositions.size();++i)
        {
            delete mPositions[i];
        }
    }
    std::vector<SE_CameraBestPosition*>  mPositions;
};
#endif