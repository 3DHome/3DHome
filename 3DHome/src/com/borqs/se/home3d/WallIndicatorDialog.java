package com.borqs.se.home3d;

import java.util.HashMap;

import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class WallIndicatorDialog extends Dialog {
    private WallIndicatorView mWallIndicatorView;
    private float mWallIndex = 0;
    private int mWallNum = 0;
    private ImageView mWallIndicatorNum;

    private HashMap<Integer, Drawable> mImageCache;

    public WallIndicatorDialog(Context context) {
        super(context, R.style.WallIndicatorDialogStyle);
        Window win = getWindow();
        WindowManager.LayoutParams p = win.getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(p);
        win.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        win.setWindowAnimations(R.style.wall_indicator_anim_style);
        win.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        setContentView(R.layout.dialog_wall_indicator);
        mImageCache = new HashMap<Integer, Drawable>();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return SESceneManager.getInstance().dispatchTouchEvent(ev);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWallIndicatorView = (WallIndicatorView) findViewById(R.id.wall_indicator_view);
        if (mWallNum != 0) {
            mWallIndicatorView.updateWallIndex(mWallIndex, mWallNum);
        }
        mWallIndicatorNum = (ImageView) findViewById(R.id.wall_indicator_num);
    }

    public void updateWallIndex(float wallIndex, int wallNum) {
        mWallNum = wallNum;
        mWallIndex = wallIndex;

        if (mWallIndicatorView != null && isShowing()) {
            mWallIndicatorView.updateWallIndex(mWallIndex, wallNum);
        }
        setNum(wallIndex, wallNum);
        if (mWallIndicatorNum != null) {
            final float a = Math.abs(wallIndex % 1 - 0.5f) * 1.6f + 0.2f;
            mWallIndicatorNum.post(new Runnable() {
                public void run() {
                    mWallIndicatorNum.setAlpha(a);
                }
            });
        }
    }

    int mCurrentNum = -1;

    private void setNum(float wallIndex, int maxNum) {
        int picIndex = Math.round(wallIndex);
        if (picIndex == 0) {
            picIndex = 1;
        } else {
            picIndex = maxNum + 1 - picIndex;
        }
        final int num = picIndex;
        if (num == mCurrentNum) {
            return;
        }
        mCurrentNum = num;
        if (mWallIndicatorNum != null) {
            mWallIndicatorNum.post(new Runnable() {
                public void run() {
                    switch (num) {
                    case 1:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_1));
                        }
                        break;
                    case 2:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_2));
                        }
                        break;
                    case 3:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_3));
                        }
                        break;
                    case 4:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_4));
                        }
                        break;
                    case 5:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_5));
                        }
                        break;
                    case 6:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_6));
                        }
                        break;
                    case 7:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_7));
                        }
                        break;
                    case 8:
                        if (mImageCache.get(num) == null) {
                            mImageCache.put(num, getContext().getResources().getDrawable(R.drawable.number_8));
                        }
                        break;
                    }
                    mWallIndicatorNum.setBackgroundDrawable(mImageCache.get(num));
                }

            });
        }
    }
}
