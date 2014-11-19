#ifndef __ViewGroup_H__
#define __ViewGroup_H__

#include "SE_CommonNode.h"

class View;



class SE_ENTRY ViewGroup :public SE_CommonNode{
public:
	std::map<std::string, View*> mViewMap;
public:
	ViewGroup(SE_Spatial* parent);
	void AddView(const char* viewName, int x,int y, int w, int h, const char* imageName);
	View* GetView(const char* viewName);
	bool AddChildView(const char * parentName,const char* viewName, int x,int y, int w, int h, const char* imageName);
	void SetVisible(bool visible);

};
#endif

