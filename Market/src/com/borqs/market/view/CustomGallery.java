package com.borqs.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class CustomGallery extends Gallery {

    public CustomGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public CustomGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CustomGallery(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    // @Override
    // public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
    // float velocityY) {
    // return super.onFling(e1, e2, 0, velocityY);
    // // return false;
    // }
}
