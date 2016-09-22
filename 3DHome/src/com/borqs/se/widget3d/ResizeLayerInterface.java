package com.borqs.se.widget3d;

import android.view.MotionEvent;

public interface ResizeLayerInterface {
    void startResize();

    void endResize();

    boolean handleResizeTouchEvent(MotionEvent ev);
}
