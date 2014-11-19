package com.borqs.se.home3d;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class WorkSpace extends FrameLayout {

    private ScaleGestureDetector mScaleDetector;

    public WorkSpace(Context context) {
        super(context);
    }

    public WorkSpace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WorkSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScaleListener(ScaleGestureDetector.OnScaleGestureListener l) {
        mScaleDetector = new ScaleGestureDetector(getContext(), l);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getHomeScene() != null && getHomeScene().getStatus(HomeScene.STATUS_ON_SCALL)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1 && mScaleDetector != null) {
            mScaleDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private HomeScene getHomeScene() {
        return HomeManager.getInstance().getHomeScene();
    }

}
