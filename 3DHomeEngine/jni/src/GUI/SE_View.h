#ifndef __View_H__
#define __View_H__

#include "SE_Geometry.h"

class RootView;
class SE_Vector3f;
class ViewData;
class ViewGroup;
class SE_ImageData;

class SE_ENTRY View : public SE_Geometry{

public:
    float mX ;
    float mY ;
    float mWidth ;
    float mHeight ;
    bool mHidden ;
    RootView* mRootView;
	ViewData* mViewData;



public:
    View(SE_SpatialID id, SE_Spatial* parent = NULL);

    float getX() {
        return mX;
    }

    float getY() {
        return mY;
    }

    void setPosition(float x, float y);

    float getWidth() {
        return mWidth;
    }

    float getHeight() {
        return mHeight;
    }

    void setSize(float width, float height);


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

	void SetViewdata(ViewData* viewData){
		mViewData = viewData;
		attachSimObject((SE_SimObject*)viewData);
    }
	
 //   void Init(float x, float y, float w, float h, const char* imageName );
	void SetImage(const char* imagename);
 	void SetImage(SE_ImageData*  imgd);
	//void SetVisible(bool visible);
	void UpdateData();
	
};



#endif
