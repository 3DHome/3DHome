package com.borqs.se.widget3d;

import android.view.MotionEvent;

public interface ResizeLayerInterface {
    public void startResize();

    public void endResize();

    public boolean handleResizeTouchEvent(MotionEvent ev);
}
