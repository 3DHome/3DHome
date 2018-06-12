#ifndef SE_LODNODE_H
#define SE_LODNODE_H

#include <list>
class SE_ENTRY SE_LodNode : public SE_CommonNode
{
    DECLARE_OBJECT(SE_LodNode)
public:
    // -1 : indicate indefinite small
    enum {MIN_RANGE = -1, MAX_RANGE = 9999};
    struct _Range
    {
        float min;
        float max;
        int index;
        _Range()
        {
            min = MIN_RANGE;
            max = MAX_RANGE;
            index = -1;
        }
        _Range(float min, float max, int index)
        {
            this->min = min;
            this->max = max;
            this->index = index;
        }
    };
    SE_LodNode(SE_Spatial* parent = NULL);
    SE_LodNode(SE_SpatialID id, SE_Spatial* parent = NULL);
    ~SE_LodNode();
    SE_Vector3f getCenter();
    void renderScene(SE_Camera* camera, SE_RenderManager* renderManager, SE_CULL_TYPE cullType);
    void write(SE_BufferOutput& output);
    void read(SE_BufferInput& input);
    void addRange(float min, float max, int index);
    void removeRange(float min, float max, int index);
private:
    std::list<_Range> mRangeData;
};
#endif
