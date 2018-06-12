package com.borqs.se.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;

import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERect3D;

public class SETextView extends SEObject {
    private float mWidth = 0;
    private float mHeight = 0;
    private String mImageName;
    private String mImageKey;
    private boolean mSingleLine;
    private String mText;
    private TextPaint mTextPaint;
    private boolean mHasBeenRender = false;

    public SETextView(SEScene scene, String name, int index) {
        super(scene, name, index);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = scene.getScreenDensity();
        mTextPaint.setTextSize(mTextPaint.density * 14);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setFakeBoldText(true);
        mImageName = mName + mIndex + "_image";
        mImageKey = mName + mIndex + "_imagekey";
    }

    public SETextView(SEScene scene, String name) {
        super(scene, name);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = scene.getScreenDensity();
        mTextPaint.setTextSize(mTextPaint.density * 14);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setFakeBoldText(true);
        mImageName = mName + mIndex + "_image";
        mImageKey = mName + mIndex + "_imagekey";
    }

    /**
     * 自动计算View所需的最小空间
     * 
     * @param maxWidth
     *            允许View的最大宽度
     */
    public void autoCalculateSize(float maxWidth) {
        float w = StaticLayout.getDesiredWidth(mText, mTextPaint);
        if (w > maxWidth) {
            w = maxWidth;
        }
        StaticLayout titleLayout = new StaticLayout(mText, mTextPaint, (int) w, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
        float h = titleLayout.getHeight();
        setSize(w, h);
    }

    public SETextView(SEScene scene, String name, TextPaint paint) {
        super(scene, name);
        mTextPaint = paint;
        mImageName = mName + mIndex + "_image";
        mImageKey = mName + mIndex + "_imagekey";
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;
        SERect3D rect = new SERect3D(mWidth, mHeight);
        SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.BITMAP, mImageName, mImageKey, null, null, true);
        // 设置图片有效区，调用该接口后会调整物体的贴图坐标
        setImageValidAreaSize((int) mWidth, (int) mHeight);
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mWidth;
    }

    public void setSingleLine(boolean isSingleLine) {
        mSingleLine = isSingleLine;
    }

    public void setText(String text) {
        mText = text;
        if (mSingleLine) {
            int maxLength = mTextPaint.breakText(mText, true, mWidth, null);

            if (maxLength < mText.length()) {
                mText = mText.substring(0, maxLength);
            }
        }
        changeImage();
    }

    private void changeImage() {
        if (!mHasBeenRender || hasBeenReleased()) {
            return;
        }
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                Bitmap image = getImage();
                final int imageData = SEObject.loadImageData_JNI(image);
                image.recycle();
                if (imageData != 0) {
                    new SECommand(getScene()) {
                        public void run() {
                            if (hasBeenReleased()) {
                                SEObject.releaseImageData_JNI(imageData);
                            } else {
                                SEObject.addImageData_JNI(mImageKey, imageData);
                            }
                        }
                    }.execute();
                }
            }
        });
    }

    private Bitmap getImage() {
        Bitmap des;
        // 图片的大小和物体的大小一致
        int bitmapW = (int) mWidth;
        int bitmapH = (int) mHeight;
        // 图片的大小不是2的n次方，需要转化成2的n次方
        int newW = SEUtils.higherPower2(bitmapW);
        int newH = SEUtils.higherPower2(bitmapH);
        des = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
        // 文字画在图片有效区内，图片的有效区为物体的大小
        Canvas canvas = new Canvas(des);
        canvas.translate((newW - bitmapW) * 0.5f, (newH - bitmapH) * 0.5f);
        StaticLayout titleLayout = new StaticLayout(mText, mTextPaint, bitmapW, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
        titleLayout.draw(canvas);
        return des;
    }

    @Override
    public void onRenderFinish(SECamera camera) {
        setBlendSortAxis(AXIS.Y);
        mHasBeenRender = true;
        changeImage();
    }

}
