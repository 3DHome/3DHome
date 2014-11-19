package com.borqs.se.engine;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;

import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERect3D;

public class SEImageView extends SEObject {
    private float mWidth;
    private float mHeight;
    private String mImagePath;
    private Bitmap mBitmap;
    private int mImageID = 0;

    private String mImageName;
    private String mImageKey;
    private boolean mHasBeenRender = false;

    public SEImageView(SEScene scene, String name, int index) {
        super(scene, name, index);
        mImageName = mName + mIndex + "_image";
        mImageKey = mName + mIndex + "_imagekey";
    }

    public SEImageView(SEScene scene, String name) {
        super(scene, name);
        mImageName = mName + mIndex + "_image";
        mImageKey = mName + mIndex + "_imagekey";
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        mImageID = 0;
        mBitmap = bitmap;
        mImagePath = null;
        changeImage();
    }

    public void setBackground(int id) {
        mImageID = id;
        mImagePath = null;
        mBitmap = null;
        changeImage();
    }

    public void setBackgroundImage(String path) {
        mImageID = 0;
        mImagePath = path;
        mBitmap = null;
        changeImage();
    }

    public void setBackgroundColor(int color) {
        mImageID = 0;
        mImagePath = null;
        mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        mBitmap.setPixel(0, 0, color);
        changeImage();
    }

    /**
     * 提交图片,注意事项：
     * 1，图片的高和宽必须是2的n次方，假如是的话直接使用，不是的话强制拉伸
     * 2, 假如是.9图，图片的高度和宽度采用物体的宽度和高度
     */
    private void changeImage() {
        //物体还没有提交或者已经被释放了，返回
        if (!mHasBeenRender || hasBeenReleased()) {
            return;
        }
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                Bitmap bitmap = null;
                if (mImagePath != null) {
                    try {
                        InputStream is = getContext().getAssets().open(mImagePath);
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        if (!SEUtils.isPower2(bitmap.getWidth()) || !SEUtils.isPower2(bitmap.getHeight())) {
                            int newImageWidth = SEUtils.higherPower2(bitmap.getWidth());
                            int newImageHeight = SEUtils.higherPower2(bitmap.getHeight());
                            //图片的高和宽必须是2的n次方，假如是的话直接使用，不是的话强制拉伸
                            bitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
                        }
                    } catch (IOException e) {
                    }
                } else if (mBitmap != null && !mBitmap.isRecycled()) {
                    if (!SEUtils.isPower2(mBitmap.getWidth()) || !SEUtils.isPower2(mBitmap.getHeight())) {
                        int newImageWidth = SEUtils.higherPower2(mBitmap.getWidth());
                        int newImageHeight = SEUtils.higherPower2(mBitmap.getHeight());
                        bitmap = Bitmap.createScaledBitmap(mBitmap, newImageWidth, newImageHeight, true);
                        if (mBitmap != bitmap) {
                            mBitmap.recycle();
                        }
                    } else {
                        bitmap = mBitmap;
                    }
                } else if (mImageID != 0) {
                    Resources res = getContext().getResources();
                    bitmap = BitmapFactory.decodeResource(res, mImageID);
                    if (bitmap != null) {
                        if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
                            NinePatch npImage = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
                            // 图片的大小和物体的大小一致
                            int imageWidth = (int) mWidth;
                            int imageHeight = (int) mHeight;
                            // 图片的大小不是2的n次方，需要转化成2的n次方
                            int newImageWidth = SEUtils.higherPower2(imageWidth);
                            int newImageHeight = SEUtils.higherPower2(imageHeight);
                            bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            Rect imageRect = new Rect(0, 0, imageWidth, imageHeight);
                            npImage.draw(canvas, imageRect);
                            bitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
                        } else {
                            int imageWidth = bitmap.getWidth();
                            int imageHeight = bitmap.getHeight();
                            if (!SEUtils.isPower2(imageWidth) || !SEUtils.isPower2(imageHeight)) {
                                int newImageWidth = SEUtils.higherPower2(imageWidth);
                                int newImageHeight = SEUtils.higherPower2(imageHeight);
                                bitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
                            }
                        }
                    }
                }
                if (bitmap != null) {
                    final int imageData = SEObject.loadImageData_JNI(bitmap);
                    bitmap.recycle();
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
            }
        });
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;
        SERect3D rect = new SERect3D(mWidth, mHeight);
        SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.BITMAP, mImageName, mImageKey, null, null, true);
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    @Override
    public void onRenderFinish(SECamera camera) {
        setBlendSortAxis(AXIS.Y);
        mHasBeenRender = true;
        changeImage();
    }

}
