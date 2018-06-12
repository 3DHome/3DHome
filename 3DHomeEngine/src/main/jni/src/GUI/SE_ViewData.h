#ifndef __ViewData_H__
#define __ViewData_H__

#include "SE_MeshSimObject.h"

class RootView;
class SE_Vector3f;
class SE_ImageData;

class SE_ENTRY ViewData : public SE_MeshSimObject{

public:
    float mX ;
    float mY ;
    float mWidth ;
    float mHeight ;
    bool mHidden ;
    //RootView* mRootView;
    static const int POINT_NUM = 4;


	//SE_Vector3f* mVertex;		//SEHOME vertexdate;
	//SE_Vector3f* mColor;		//SEHOME vertexdate;
	//SE_Vector2f* mTexCoord;		//SEHOME vertexdate;
	//SE_Vector3i* mIndexes;
	//int* mFacet;  				//SEHOME facet;

    enum BillboardOrigin
    {
        BBO_TOP_LEFT,
        BBO_TOP_CENTER,
        BBO_TOP_RIGHT,
        BBO_CENTER_LEFT,
        BBO_CENTER,
        BBO_CENTER_RIGHT,
        BBO_BOTTOM_LEFT,
        BBO_BOTTOM_CENTER,
        BBO_BOTTOM_RIGHT
    };


public:
    ViewData(SE_Spatial* spatial);

    float getX() {
        return mX;
    }

    float getY() {
        return mY;
    }

    void setPosition(float x, float y) {
        mX = x;
        mY = y;
    }

    float getWidth() {
        return mWidth;
    }

    float getHeight() {
        return mHeight;
    }

    void setSize(float width, float height) ;

    bool isHidden() {
        return mHidden;
    }

    void setHidden(bool hidden) {
        if (mHidden != hidden) {
            mHidden = hidden;
            onHiddenChanged();
        }
    }

    //void generate(RenderView view, RenderView.Lists lists) = 0;

    // Returns true if something is animating.
    bool update(float frameInterval) {
        return false;
    }


    // Allows subclasses to further constrain the hit test defined by layer
    // bounds.
    bool containsPoint(float x, float y) {
        return true;
    }

    //void onSurfaceCreated(RenderView view, GL11 gl) {
   // }


    void onHiddenChanged(){};



    void Init(float x, float y, float w, float h, const char* imageName );
	void setImage(const char* imagename);
	void setImage(SE_ImageData* imgd);	
	void UpdateData();
	
};



#endif

