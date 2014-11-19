package com.borqs.se.home3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;

import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.AXIS;

public class WidgetResizeFrame extends SEObject {
    private float mWidth;
    private float mHeight;

    private float mReSizeWidth;
    private float mReSizeHeight;

    private int mImageWidth;
    private int mImageHeight;
    private String mImagePath;
    private Bitmap mBitmap;
    private int mImageID = 0;

    private String mImageName;
    private String mImageKey;
    float[] mVertexArray;

    public WidgetResizeFrame(SEScene scene, String name, int index) {
        super(scene, name, index);
    }

    public WidgetResizeFrame(SEScene scene, String name) {
        super(scene, name);
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        if (!SEUtils.isPower2(mImageWidth) || !SEUtils.isPower2(mImageHeight)) {
            throw new IllegalArgumentException("Bitmap size is not right, engine support power2 image only");
        }
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

    /**
     * such as "assets/bask/background.png"
     */
    public void setBackgroundImage(String path, int imageW, int imageH) {
        mImageWidth = imageW;
        mImageHeight = imageH;
        if (!SEUtils.isPower2(mImageWidth) || !SEUtils.isPower2(mImageHeight)) {
            throw new IllegalArgumentException("Bitmap size is not right, engine support power2 image only");
        }
        mImagePath = path;
        mBitmap = null;
        changeImage();
    }

    private void changeImage() {
        if (mImageKey == null) {
            return;
        }
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                final int imageData;
                if (mImagePath != null) {
                    imageData = SEObject.loadImageData_JNI(mImagePath);
                } else if (mBitmap != null && !mBitmap.isRecycled()) {
                    imageData = SEObject.loadImageData_JNI(mBitmap);
                    mBitmap.recycle();
                } else if (mImageID != 0) {
                    Resources res = getContext().getResources();
                    Bitmap bitmap = BitmapFactory.decodeResource(res, mImageID);
                    if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
                        NinePatch npImage = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
                        int imageWidth = (int) mWidth;
                        int imageHeight = (int) mHeight;
                        int newImageWidth = SEUtils.higherPower2(imageWidth);
                        int newImageHeight = SEUtils.higherPower2(imageHeight);
                        mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                        int left = (newImageWidth - imageWidth) / 2;
                        int top = (newImageHeight - imageHeight) / 2;
                        Canvas canvas = new Canvas(mBitmap);
                        Rect imageRect = new Rect(left, top, left + imageWidth, top + imageHeight);
                        npImage.draw(canvas, imageRect);
                    } else {
                        int imageWidth = bitmap.getWidth();
                        int imageHeight = bitmap.getHeight();
                        if (!SEUtils.isPower2(imageWidth) || !SEUtils.isPower2(imageHeight)) {
                            int newImageWidth = SEUtils.higherPower2(imageWidth);
                            int newImageHeight = SEUtils.higherPower2(imageHeight);
                            mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                            int left = (newImageWidth - imageWidth) / 2;
                            int top = (newImageHeight - imageHeight) / 2;
                            Canvas canvas = new Canvas(mBitmap);
                            canvas.drawBitmap(bitmap, left, top, null);
                        } else {
                            mBitmap = bitmap;
                        }
                    }
                    imageData = SEObject.loadImageData_JNI(mBitmap);
                    mBitmap.recycle();
                } else {
                    imageData = 0;
                }
                new SECommand(getScene()) {
                    public void run() {
                        SEObject.addImageData_JNI(mImageKey, imageData);
                    }
                }.execute();

            }
        });
    }

    public void reSize(float w, float h) {
        mReSizeWidth = w;
        mReSizeHeight = h;
        if (mImageKey == null) {
            return;
        }
        for (int index = 0; index < 4; index++) {
            float moveX = 0;
            float moveZ = 0;
            if (index == 0 || index == 1) {
                moveZ = mReSizeHeight / 2 - mHeight * 0.24f;
            } else {
                moveZ = -mReSizeHeight / 2 + mHeight * 0.24f;
            }
            if (index == 0 || index == 3) {
                moveX = -mReSizeWidth / 2 + mWidth * 0.24f;
            } else {
                moveX = mReSizeWidth / 2 - mWidth * 0.24f;
            }
            mVertexArray[12 * index] = -mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 1] = 0;
            mVertexArray[12 * index + 2] = -mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 3] = mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 4] = 0;
            mVertexArray[12 * index + 5] = -mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 6] = mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 7] = 0;
            mVertexArray[12 * index + 8] = mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 9] = -mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 10] = 0;
            mVertexArray[12 * index + 11] = mHeight * 0.24f + moveZ;
        }
        updateVertex_JNI(mVertexArray);
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;

    }

    public float getWidth() {
        return mReSizeWidth;
    }

    public float getHeight() {
        return mReSizeHeight;
    }

    @Override
    public void onRender(SECamera camera) {
        mImageName = mName + "_image";
        if (mImagePath != null) {
            mImageKey = mImagePath;
            createRectangle(this, IMAGE_TYPE.IMAGE, mImageName, mImagePath, null, null, true);
            setImageValidAreaSize(mImageWidth, mImageHeight);

        } else if (mBitmap != null) {
            mImageKey = mName + "_image_key";
            createRectangle(this, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null, true);
            setImageValidAreaSize(mImageWidth, mImageHeight);

        } else {
            mImageKey = mName + "_image_key";
            Resources res = getContext().getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(res, mImageID);
            if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
                NinePatch npImage = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
                int imageWidth = (int) mWidth;
                int imageHeight = (int) mHeight;
                int newImageWidth = SEUtils.higherPower2(imageWidth);
                int newImageHeight = SEUtils.higherPower2(imageHeight);
                mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                int left = (newImageWidth - imageWidth) / 2;
                int top = (newImageHeight - imageHeight) / 2;
                Canvas canvas = new Canvas(mBitmap);
                Rect imageRect = new Rect(left, top, left + imageWidth, top + imageHeight);
                npImage.draw(canvas, imageRect);
                createRectangle(this, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null, true);
                setImageValidAreaSize(imageWidth, imageHeight);
            } else {
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                if (!SEUtils.isPower2(imageWidth) || !SEUtils.isPower2(imageHeight)) {
                    int newImageWidth = SEUtils.higherPower2(imageWidth);
                    int newImageHeight = SEUtils.higherPower2(imageHeight);
                    mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                    int left = (newImageWidth - imageWidth) / 2;
                    int top = (newImageHeight - imageHeight) / 2;
                    Canvas canvas = new Canvas(mBitmap);
                    canvas.drawBitmap(bitmap, left, top, null);
                } else {
                    mBitmap = bitmap;
                }

                createRectangle(this, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null, true);
                setImageValidAreaSize(imageWidth, imageHeight);

            }
        }

    }

    private void createRectangle(SEObject obj, IMAGE_TYPE imageType, String imageName, String imagePath, Bitmap bitmap,
            float[] color, boolean blendingable) {
        mVertexArray = new float[12 * 4];
        for (int index = 0; index < 4; index++) {
            float moveX = 0;
            float moveZ = 0;
            if (index == 0 || index == 1) {
                moveZ = mReSizeHeight / 2 - mHeight * 0.24f;
            } else {
                moveZ = -mReSizeHeight / 2 + mHeight * 0.24f;
            }
            if (index == 0 || index == 3) {
                moveX = -mReSizeWidth / 2 + mWidth * 0.24f;
            } else {
                moveX = mReSizeWidth / 2 - mWidth * 0.24f;
            }
            mVertexArray[12 * index] = -mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 1] = 0;
            mVertexArray[12 * index + 2] = -mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 3] = mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 4] = 0;
            mVertexArray[12 * index + 5] = -mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 6] = mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 7] = 0;
            mVertexArray[12 * index + 8] = mHeight * 0.24f + moveZ;

            mVertexArray[12 * index + 9] = -mWidth * 0.24f + moveX;
            mVertexArray[12 * index + 10] = 0;
            mVertexArray[12 * index + 11] = mHeight * 0.24f + moveZ;
        }
        obj.setVertexArray(mVertexArray);
        int[] faceIndex = new int[] { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10, 11, 8, 12, 13, 14, 14, 15, 12,
                1, 4, 7, 7, 2, 1, 11, 10, 5, 5, 4, 11, 13, 8, 11, 11, 14, 13, 15, 14, 1, 1, 0, 15,

        };

        obj.setFaceArray(faceIndex);
        float[] texVertexArray = new float[8 * 4];
        for (int index = 0; index < 4; index++) {
            float moveX = 0;
            float moveZ = 0;
            if (index == 0 || index == 3) {
                moveX = -0.26f;
            } else {
                moveX = 0.26f;
            }

            if (index == 0 || index == 1) {
                moveZ = 0.26f;
            } else {
                moveZ = -0.26f;
            }

            texVertexArray[8 * index] = 0.26f + moveX;
            texVertexArray[8 * index + 1] = 0.26f + moveZ;

            texVertexArray[8 * index + 2] = 0.74f + moveX;
            texVertexArray[8 * index + 3] = 0.26f + moveZ;

            texVertexArray[8 * index + 4] = 0.74f + moveX;
            texVertexArray[8 * index + 5] = 0.74f + moveZ;

            texVertexArray[8 * index + 6] = 0.26f + moveX;
            texVertexArray[8 * index + 7] = 0.74f + moveZ;
        }
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imagePath);
        obj.setBitmap(bitmap);
        obj.setColor(color);
        obj.setBlendingable(true);
        if (blendingable) {
            obj.setNeedForeverBlend(true);
        }
    }

    @Override
    public void onRenderFinish(SECamera camera) {
        setBlendSortAxis(AXIS.Y);
    }

}
