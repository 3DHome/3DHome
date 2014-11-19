#ifndef SE_LAYER_H
#define SE_LAYER_H
#include "SE_Common.h"
class SE_ENTRY SE_Layer
{
public:
    SE_Layer();
    SE_Layer(int layer);
    ~SE_Layer();
    SE_Layer(const SE_Layer& layer);
    SE_Layer& operator=(const SE_Layer& layer);
    friend bool operator==(const SE_Layer& left, const SE_Layer& right);
    friend bool operator<(const SE_Layer& left, const SE_Layer& right);
    friend bool operator>(const SE_Layer& left, const SE_Layer& right);
    friend SE_Layer operator+(const SE_Layer& left, const SE_Layer& right);
    friend SE_Layer operator-(const SE_Layer& left, const SE_Layer& right);

    int getLayer() const
    {
        return mLayer;
    }
     
    void setLayer(int layer)
    {
        mLayer = layer;
    }
    
private:
    int mLayer;
};
#endif
