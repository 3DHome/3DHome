package com.borqs.se.home3d;

import com.borqs.se.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

public class WallIndicatorView extends View {
    private Bitmap mIndicatorBitmap;
    private float mNewWallIndex;
    private float mWallIndex;
    private int mIndicatorA;
    private int mIndicatorB;
    private int mWallNum = 0;

    public WallIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WallIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallIndicatorView(Context context) {
        super(context);
    }

    public void updateWallIndex(float wallIndex, int wallNun) {
        mWallNum = wallNun;
        mNewWallIndex = wallIndex;
        postInvalidate();
    }

    private int mSizeIndicatorW = 0;
    private int mSizeIndicatorH = 0;

    private void initIndicatorBitmap(int sizeW, int sizeH) {
        if (sizeW != mSizeIndicatorW || sizeH != mSizeIndicatorH) {
            mSizeIndicatorW = sizeW;
            mSizeIndicatorH = sizeH;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.wall_indicator_8);
            mIndicatorBitmap = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), mSizeIndicatorW, mSizeIndicatorH,
                    true);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mWallNum == 0) {
            return;
        }
        mWallIndex = mNewWallIndex;
        mIndicatorA = (int) mWallIndex;
        mIndicatorB = mIndicatorA + 1;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float r;
        int sizeW;
        int sizeH;
        if (mWallNum == 4) {
            r = w * 0.34f;
            sizeW = (int) (2.3 * r * Math.tan(Math.PI / mWallNum));
            sizeH = (int) (0.1f * w);
        } else {
            r = w * 0.4f;
            sizeW = (int) (2.2 * r * Math.tan(Math.PI / mWallNum));
            sizeH = (int) (0.1f * w);
        }

        initIndicatorBitmap(sizeW, sizeH);
        canvas.save();
        double angle = mIndicatorA * 2 * Math.PI / mWallNum;
        float translateX = (float) (-Math.sin(angle) * r);
        float translateY = (float) (-Math.cos(angle) * r);
        canvas.translate(w * 0.5f - sizeW * 0.5f + translateX, h * 0.5f + translateY);
        float rotate = -mIndicatorA * 360 / mWallNum + 180;
        canvas.rotate(rotate, sizeW * 0.5f, 0);
        drawIndicatorA(canvas, sizeW, sizeH);
        canvas.restore();

        canvas.save();
        angle = mIndicatorB * 2 * Math.PI / mWallNum;
        translateX = (float) (-Math.sin(angle) * r);
        translateY = (float) (-Math.cos(angle) * r);
        canvas.translate(w * 0.5f - sizeW * 0.5f + translateX, h * 0.5f + translateY);
        rotate = -mIndicatorB * 360 / mWallNum + 180;
        canvas.rotate(rotate, sizeW * 0.5f, 0);
        drawIndicatorB(canvas, sizeW, sizeH);
        canvas.restore();
    }

    public void drawIndicatorA(Canvas canvas, int sizeW, int sizeH) {
        int newSizeW = (int) ((mIndicatorB - mWallIndex) * sizeW);
        if (newSizeW < 4) {
            return;
        }
        if (newSizeW == sizeW) {
            canvas.drawBitmap(mIndicatorBitmap, 0, 0, null);
            return;
        }
        Rect src = new Rect(0, 0, newSizeW / 2, mIndicatorBitmap.getHeight());
        Rect des = new Rect(sizeW - newSizeW, 0, sizeW - newSizeW / 2, mIndicatorBitmap.getHeight());
        canvas.drawBitmap(mIndicatorBitmap, src, des, null);

        src = new Rect(mIndicatorBitmap.getWidth() - newSizeW / 2, 0, mIndicatorBitmap.getWidth(),
                mIndicatorBitmap.getHeight());
        des = new Rect(sizeW - newSizeW / 2, 0, sizeW, mIndicatorBitmap.getHeight());
        canvas.drawBitmap(mIndicatorBitmap, src, des, null);

    }

    public void drawIndicatorB(Canvas canvas, int sizeW, int sizeH) {
        int newSizeW = (int) ((mWallIndex - mIndicatorA) * sizeW);
        if (newSizeW < 4) {
            return;
        }
        if (newSizeW == sizeW) {
            canvas.drawBitmap(mIndicatorBitmap, 0, 0, null);
            return;
        }
        Rect src = new Rect(0, 0, newSizeW / 2, mIndicatorBitmap.getHeight());
        Rect des = new Rect(0, 0, newSizeW / 2, mIndicatorBitmap.getHeight());
        canvas.drawBitmap(mIndicatorBitmap, src, des, null);

        src = new Rect(mIndicatorBitmap.getWidth() - newSizeW / 2, 0, mIndicatorBitmap.getWidth(),
                mIndicatorBitmap.getHeight());
        des = new Rect(newSizeW / 2, 0, newSizeW, mIndicatorBitmap.getHeight());
        canvas.drawBitmap(mIndicatorBitmap, src, des, null);
    }
}
