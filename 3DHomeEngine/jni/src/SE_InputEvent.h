#ifndef SE_INPUTEVENT_H
#define SE_INPUTEVENT_H
// motion event is caused by touch or mouse movement.
// mX and mY is the x and y coordinate of screen.
// the origin is left top
class SE_ENTRY SE_MotionEvent
{
public:
    enum TYPE {DOWN, UP, MOVE,AUTOROTATE,RDOWN,RUP,};
    enum {MOVE_SLOPE = 5};
    SE_MotionEvent(TYPE t, int x = 0, int y = 0)
    {
        mMotionType = t;
        mX = x;
        mY = y;
        mVelocityX = 0.0;
        mVelocityY = 0.0;
		mViewportId = -1;
    }
    int getX()
    {
        return mX;
    }
    int getY()
    {
        return mY;
    }
    float getVelocityX()
    {
        return mVelocityX;
    }
    float getVelocityY()
    {
        return mVelocityY;
    }
    TYPE getType()
    {
        return mMotionType;
    }
    void setX(int x)
    {
        mX = x;
    }
    void setY(int y)
    {
        mY = y;
    }
    void setVelocityX(float vx)
    {
        mVelocityX = vx;
    }
    void setVelocityY(float vy)
    {
        mVelocityY = vy;
    }
	void setViewportId(int viewport)
	{
		mViewportId = viewport;
	}
	int getViewportId()
	{
		return mViewportId ;
	}
private:
    TYPE mMotionType;
    int mX;
    int mY;
    float mVelocityX;
    float mVelocityY;
	int mViewportId;
};
class SE_ENTRY SE_KeyEvent
{
public:
    enum TYPE {DOWN, UP};
    enum CODE {
                  KEY_UP,
                  KEY_DOWN,
                  KEY_LEFT,
                  KEY_RIGHT,
                  KEY_F1,
                  KEY_F2,
                  KEY_F3,
                  KEY_F4,
                  KEY_MENU,
                  KEY_1,
                  KEY_2,
                  KEY_3,
                  KEY_4,
                  KEY_5,
                  KEY_6,
                  KEY_7,
                  KEY_8,
                  KEY_9,
                  KEY_0,
                  KEY_NUN
              };
    SE_KeyEvent(TYPE t, CODE k)
	{
		mKeyType = t;
		mKeyCode = k;
		mViewportId = -1;
	};
    TYPE getType()
    {
        return mKeyType;
    }
    CODE getCode()
    {
        return mKeyCode;
    }
	void setViewportId(int viewport)
	{
		mViewportId = viewport;
	}
	int getViewportId()
	{
		return mViewportId ;
	}
private:
    TYPE mKeyType;
    CODE mKeyCode;
	int mViewportId;
};
#endif