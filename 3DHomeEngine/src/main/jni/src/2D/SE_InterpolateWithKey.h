#ifndef SE_INTERPOLATEWITHKEY_H
#define SE_INTERPOLATEWITHKEY_H
struct SE_KeyRange
{
    unsigned int min;
    unsigned int max;
};
class SE_InterpolateWithKey
{
public:
private:
    std::vector<SE_KeyRange> mKeyRange;
    std::vector<SE_KeyFrame> mKeyFrames;
};
#endif
