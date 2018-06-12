package com.borqs.market.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerInListView extends ViewPager {
    public ViewPagerInListView(Context context) {
        super(context);
    }
    
    public ViewPagerInListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_MOVE: 
            getParent().requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            getParent().requestDisallowInterceptTouchEvent(false);
            break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
