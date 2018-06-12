#ifndef __RootView_H__
#define __RootView_H__

#include "SE_CommonNode.h"

class View;
class ViewGroup;

//class RootView : public Singleton<RootView>{
class SE_ENTRY RootView :public SE_CommonNode{
private:
	float mViewHeight;
	float mViewWidth;
	//float mFrameInterval;
	std::map<std::string, ViewGroup*> mViewGroupMap;

public:
    //static RootView& getSingleton(void);
    //static RootView* getSingletonPtr(void);

	RootView(SE_Spatial* parent);
	void AddViewGroup(const char* viewgroupName);
	ViewGroup* GetViewGroup(const char* viewName);

};
#endif
