#ifndef SE_CONFIG_H
#define SE_CONFIG_H
#include "SE_Vector.h"
#include <SE_Value.h>
#include <map>
#include <string>
class SE_ENTRY SE_Config
{
public:
    SE_Config(const char* fileName);
	bool initOK();
	int getInt(const char* id, int defaultValue = -1);
	std::string getString(const char* id, const std::string& defaultValue);
	float getFloat(const char* id, float defaultValue = 0);
	SE_Vector2f getVector2f(const char* id, const SE_Vector2f& defaultValue = SE_Vector2f());
	SE_Vector3f getVector3f(const char* id, const SE_Vector3f& defaultValue = SE_Vector3f());
	SE_Vector3i getVector3i(const char* id, const SE_Vector3i& defaultValue = SE_Vector3i());
private:
	void parse(char* data, int dataLen);
	void handleLine(const std::string& line);
private:
	std::map<std::string, SE_Value> mDataMap;
	bool mIsInitOK;
};
#endif
