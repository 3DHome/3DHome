package com.borqs.se.widget3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.MotionEvent;

import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;

public class Bookshelf extends NormalObject {
    private Book mBook;
    private SEObject mResponseObj;
    private boolean mBookOnShow;
    private boolean mDisableTouch;
    private ShowDialogAnimation mShowDialogAnimation;
    private HideDialogAnimation mHideDialogAnimation;
    private float mOpenBookAngle;
    private String mFilePath;

    public Bookshelf(HomeScene scene, String name, int index) {
        super(scene, name, index);   
        mBookOnShow = false;
        mDisableTouch = false;
        mOpenBookAngle = 0;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        List<SEObject> books = findComponenetObjectsByRegularName("book");
        for (SEObject book : books) {
            book.setOnClickListener(new SEObject.OnTouchListener() {
                public void run(SEObject obj) {
                    if (!getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)
                            && !getHomeScene().getStatus(HomeScene.STATUS_ON_SKY_SIGHT)) {
                        mResponseObj = obj;
                        show();
                    }
                }
            });
            book.setOnLongClickListener(new SEObject.OnTouchListener() {
                public void run(SEObject obj) {
                    SETransParas startTranspara = new SETransParas();
                    startTranspara.mTranslate = getAbsoluteTranslate();
                    float angle = getUserRotate().getAngle();
                    SEObject parent = getParent();
                    while (parent != null) {
                        angle = angle + parent.getUserRotate().getAngle();
                        parent = parent.getParent();
                    }
                    startTranspara.mRotate.set(angle, 0, 0, 1);
                    setStartTranspara(startTranspara);
                    setOnMove(true);
                }
            });
            book.setClickable(true);
            book.setIsEntirety(true);
        }
        mFilePath = getObjectInfo().mModelInfo.mAssetsPath + File.separator;
        setOnClickListener(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mDisableTouch || super.dispatchTouchEvent(event);
    }

    private void show() {
        if (!mBookOnShow) {
            stopAllAnimation(null);
            mDisableTouch = true;
            mBookOnShow = true;
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, true);
            mBook = new Book(getHomeScene(), "BookDialog");
            getHomeScene().getContentObject().addChild(mBook, true);
            getHomeScene().setTouchDelegate(this);
            SEVector3f showLoc = getCamera().getScreenLocation(0.1f);
            mShowDialogAnimation = new ShowDialogAnimation(getHomeScene(), mResponseObj.getAbsoluteTranslate(), showLoc);
            mShowDialogAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDisableTouch = false;
                    handOnClick();
                }
            });
            mShowDialogAnimation.execute();
        }
    }

    public void hide(boolean fast, final SEAnimFinishListener l) {
        if (mBookOnShow) {
            mDisableTouch = true;
            stopAllAnimation(null);
            mHideDialogAnimation = new HideDialogAnimation(getHomeScene(), mResponseObj.getAbsoluteTranslate());
            mHideDialogAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDisableTouch = false;
                    getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                    getHomeScene().removeTouchDelegate();
                    mBookOnShow = false;
                    getHomeScene().getContentObject().removeChild(mBook, true);
                    mResponseObj.setVisible(true);
                    if (l != null) {
                        l.onAnimationfinish();
                    }
                }
            });
            mHideDialogAnimation.execute();
        }
    }

    @Override
    public void onActivityResume() {
        new SECommand(getHomeScene()) {
            public void run() {
                if (mBookOnShow) {
                    hide(false, null);
                }
            }
        }.execute();
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mBookOnShow) {
            hide(false, l);
        }
        return false;
    }

    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mShowDialogAnimation != null) {
            mShowDialogAnimation.stop();
        }
        if (mHideDialogAnimation != null) {
            mHideDialogAnimation.stop();
        }
    }

    private class Book extends SEObject {
        private float[] mVector;

        public Book(SEScene scene, String name) {
            super(scene, name);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            return !mDisableTouch && super.dispatchTouchEvent(event);
        }

        @Override
        public void onRender(SECamera camera) {
            float w = getCamera().getWidth() * 0.1f;
            float h = getCamera().getHeight() * 0.1f;
            mVector = new float[] { -w / 2, 0, -h / 2f,
                    w / 2, 0, -h / 2f,
                    w / 2, 0, h / 2f, 
                    -w / 2, 0, h / 2f, 
                    -w / 2, -w / 5, -h / 2f,
                    w / 2, -w / 5, -h / 2f, 
                    w / 2, -w / 5, h / 2f,
                    -w / 2, -w / 5, h / 2f };
            setVertexArray(mVector);
            // data.setFaceArray(new int[] { 1, 0, 3, 3, 2, 1, 0, 4, 7, 7, 3, 0,
            // 4, 5, 6, 6, 7, 4 });
            setFaceArray(new int[] { 0, 1, 2, 2, 3, 0, 4, 0, 3, 3, 7, 4, 4, 5, 6, 6, 7, 4 });
            // data.setTexVertexArray(new float[] { 0.47619f, 0, 0, 0, 0, 1,
            // 0.47619f, 1, 0.5238f, 0, 1, 0, 1, 1, 0.5238f,
            // 1 });
            setTexVertexArray(new float[] { 0.54545f, 0, 1, 0, 1, 1, 0.54545f, 1, 0.454545f, 0, 0, 0, 0, 1, 0.454545f,
                    1 });
            setImage(IMAGE_TYPE.BITMAP, mName + "_imageName", mName + "_imageKey");
            setVisible(false);
            int imageW = 512;
            int imageH = (int) (getCamera().getHeight() * 512 / (getCamera().getWidth() * 2.2f));
            setBitmap(createFace(imageW, imageH));
            setColor(new float[] { 1, 0, 0 });
            setImageValidAreaSize(imageW, imageH);
        }

        public void updateVector(float angle) {
            float w = getCamera().getWidth() * 0.1f;
            float sin = (float) Math.sin(Math.PI * angle / 180);
            float cos = (float) Math.cos(Math.PI * angle / 180);

            float sin2 = (float) Math.sin(Math.PI * angle / 90);
            float cos2 = (float) Math.cos(Math.PI * angle / 90);
            mVector[12] = -w / 2 - sin * w / 5;
            mVector[13] = -cos * w / 5;

            mVector[21] = -w / 2 - sin * w / 5;
            mVector[22] = -cos * w / 5;

            mVector[15] = cos2 * w - w / 2;
            mVector[16] = -sin2 * w - w / 5;

            mVector[18] = cos2 * w - w / 2;
            mVector[19] = -sin2 * w - w / 5;
        }

    }

    private Bitmap createFace(int w, int h) {

        InputStream is_one = null;
        Bitmap bitmap_one = null;
        try {
            is_one = new FileInputStream(mFilePath + "shu001.jpg");
            bitmap_one = BitmapFactory.decodeStream(is_one);
            is_one.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        int newW = SEUtils.higherPower2(w);
        int newH = SEUtils.higherPower2(h);
        Bitmap des = Bitmap.createBitmap(newW, newH, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(des);
        canvas.translate((newW - w) * 0.5f, (newH - h) * 0.5f);
        if (bitmap_one != null) {
            canvas.drawBitmap(bitmap_one, 0, 0, null);
        }
        if (getObjectInfo().mComponentName == null) {
            getObjectInfo().getCategoryComponentName();
        }
        ResolveInfo resolveInfo = getObjectInfo().getResolveInfo();
        if (resolveInfo != null) {
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(28);
            textPaint.setColor(0xff550714);
            textPaint.setAntiAlias(true);
            textPaint.setStrokeWidth(10);
            textPaint.setStrokeCap(Paint.Cap.ROUND);
            String label = getLabel(getContext(), resolveInfo);
            StaticLayout titleLayout = new StaticLayout(label, textPaint, 244, Alignment.ALIGN_CENTER, 1f, 0.0f, false);
            int lineCount = titleLayout.getLineCount();
            if (lineCount > 2) {
                int index = titleLayout.getLineEnd(1);
                if (index > 0) {
                    label = label.substring(0, index);
                    titleLayout = new StaticLayout(label, textPaint, w, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
                }
            }
            canvas.save();
            canvas.translate(268, 50);
            titleLayout.draw(canvas);
            canvas.restore();
        }
        return des;
    }

    private String getLabel(Context context, ResolveInfo resolveInfo) {
        return resolveInfo.loadLabel(context.getPackageManager()).toString();
    }

    private class ShowDialogAnimation extends CountAnimation {
        private SEVector3f mEndLocation;
        private SEVector3f mCurLocation;
        private SEVector3f mStepLocation;

        public ShowDialogAnimation(SEScene scene, SEVector3f startLocation, SEVector3f endLocation) {
            super(scene);
            mCurLocation = startLocation.clone();
            mEndLocation = endLocation.clone();
        }

        public void runPatch(int count) {
            float angle = count * 10;
            mCurLocation.selfAdd(mStepLocation);
            if (angle <= 90) {
                mOpenBookAngle = angle;
                mBook.updateVector(mOpenBookAngle);
                mBook.updateVertex_JNI(mBook.mVector);
            }
            if (count == 13) {
                mCurLocation = mEndLocation;
            }
            mBook.setUserTranslate(mCurLocation);
        }

        @Override
        public void onFirstly(int count) {
            mOpenBookAngle = 0;
            mBook.setVisible(true);
            mResponseObj.setVisible(false);
            mStepLocation = mEndLocation.subtract(mCurLocation).selfDiv(12);

        }

        @Override
        public int getAnimationCount() {
            return 13;
        }

    }

    private class HideDialogAnimation extends CountAnimation {
        private SEVector3f mEndLocation;
        private SEVector3f mCurLocation;
        private SEVector3f mStepLocation;

        public HideDialogAnimation(SEScene scene, SEVector3f endLocation) {
            super(scene);
            mEndLocation = endLocation.clone();
        }

        public void runPatch(int count) {
            mOpenBookAngle = mOpenBookAngle - 10;
            mCurLocation.selfAdd(mStepLocation);
            if (mOpenBookAngle <= 0) {
                mOpenBookAngle = 0;
                mCurLocation = mEndLocation;
                stop();
            }
            mBook.setUserTranslate(mCurLocation);
            mBook.updateVector(mOpenBookAngle);
            mBook.updateVertex_JNI(mBook.mVector);
        }

        @Override
        public void onFirstly(int count) {
            mCurLocation = mBook.getUserTranslate();
            float step = mOpenBookAngle / 10;
            if (step > 0) {
                mStepLocation = mEndLocation.subtract(mCurLocation).selfDiv(step);
            } else {
                mStepLocation = new SEVector3f();
            }
        }
    }

}
