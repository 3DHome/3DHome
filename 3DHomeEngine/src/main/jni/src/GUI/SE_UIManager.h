#ifndef __UIManager_H__
#define __UIManager_H__

class RootView;
class SE_Camera;
class SE_ImageData;

class SE_ENTRY SE_UIManager
{
public:
	SE_UIManager();
	~SE_UIManager();
	void CreateGUIScene();
	void ReSizeUIViewPort(float w, float h);
	
	void AddViewGroup(const char* viewName);
	void SetViewGroupVisible(const char* groupName, bool visible);
	void AddView(const char* groupName,const char* viewName,float x, float y, float w, float h, const char* imageName);
	void SetViewPosition(const char* groupName,const char* viewName, float x, float y);
	void SetViewSize(const char* groupName,const char* viewName,float w, float h);
	void SetViewImage(const char* groupName,const char* viewName, const char* imageName);
	void SetViewImage(const char* groupName,const char* viewName, SE_ImageData*  imgd);
	void SetViewVisible(const char* groupName,const char* viewName, bool visible);
private:
	RootView* mRootView;
	SE_Camera* mCamera;
	
};

#endif

