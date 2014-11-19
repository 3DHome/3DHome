package com.borqs.se.widget3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.borqs.se.R;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEAnimation.TimeAnimation;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeManager.TimeChangeCallBack;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.LanguageChangeCallBack;

public class Calendary extends NormalObject implements TimeChangeCallBack, LanguageChangeCallBack {
    private boolean mOnDialogShow;
    private boolean mDisableTouch;
    private CalendaryDialog mDialog;
    private ShowDialogAnimation mShowDialogAnimation;
    private HideDialogAnimation mHideDialogAnimation;
    private Context mContext;
    private SEVector3f mShowLoc;
    private String mCalendar_Image_Key;

    private int mPreYear;
    private int mPreMonth;
    private int mPreDay;

    private String mFilePath;

    public Calendary(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mContext = getContext();
        mOnDialogShow = false;
        mDisableTouch = false;
        mPreYear = 0;
        mPreMonth = 0;
        mPreDay = 0;
        setClickable(true);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        mFilePath = getObjectInfo().mModelInfo.mAssetsPath + File.separator;
        mCalendar_Image_Key = mFilePath + "gualidanti.jpg";
        updateImage();
        HomeManager.getInstance().addTimeCallBack(this);
        LauncherModel.getInstance().addLanguageChangeCallBack(this);
        setOnClickListener(new OnTouchListener() {

            public void run(SEObject obj) {
                if (!getHomeScene().getStatus(HomeScene.STATUS_ON_DESK_SIGHT)
                        && !getHomeScene().getStatus(HomeScene.STATUS_ON_SKY_SIGHT)) {
                    showMonth();
                }
            }
        });
    }

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            HomeManager.getInstance().removeTimeCallBack(this);
            LauncherModel.getInstance().removeLanguageChangeCallBack(this);
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        HomeManager.getInstance().removeTimeCallBack(this);
        LauncherModel.getInstance().removeLanguageChangeCallBack(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void onTimeChanged() {
        updateImage();
    }

    public void onLanguageChanged() {
        mPreYear = 0;
        mPreMonth = 0;
        mPreDay = 0;
        updateImage();
    }

    private int getMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        return month;
    }

    private int getDay() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.DATE);
        return month;
    }

    private int getYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    private int getDaysOfMonth(int monthIndex) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex);
        return calendar.getActualMaximum(Calendar.DATE);
    }

    private int getFirstWeekDay(int monthIndex) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.DATE, 0);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 7) {
            dayOfWeek = 0;
        }
        return dayOfWeek;
    }

    private void updateImage() {
        int curYear = getYear();
        int curMonth = getMonth();
        int curDay = getDay();
        if (curDay != mPreDay || curMonth != mPreMonth || curYear != mPreYear) {
            mPreDay = curDay;
            mPreMonth = curMonth;
            mPreYear = curYear;
            if (mCalendar_Image_Key != null) {
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        InputStream is = null;
                        Bitmap background = null;
                        try {
                            is = new FileInputStream(mFilePath + "gualidanti.jpg");
                            background = BitmapFactory.decodeStream(is).copy(Bitmap.Config.RGB_565, true);
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        if (background == null) {
                            background = Bitmap.createBitmap(256, 512, Bitmap.Config.RGB_565);
                        }
                        Bitmap des = background;
                        Canvas canvas = new Canvas(des);
                        drawYear(canvas);
                        drawMonth(canvas);
                        drawWeeks(canvas);
                        drawDays(canvas);
                        final int imageData = loadImageData_JNI(des);
                        new SECommand(getHomeScene()) {
                            public void run() {
                                addImageData_JNI(mCalendar_Image_Key, imageData);
                            }
                        }.execute();
                    }
                });
            }
        }
    }

    private void drawYear(Canvas canvas) {
        canvas.save();
        TextPaint titlePaint = new TextPaint();
        titlePaint.setTextSize(28);
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setAntiAlias(true);
        String text = Integer.toString(mPreYear);
        StaticLayout titleLayout = new StaticLayout(text, titlePaint, 256, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
        canvas.translate(0, 120);
        titleLayout.draw(canvas);
        canvas.restore();
    }

    private void drawMonth(Canvas canvas) {
        canvas.save();
        String able = getContext().getResources().getConfiguration().locale.getCountry();
        if (able.equals("CN")) {
            try {
                Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                ColorMatrix cm = new ColorMatrix();
                float[] array = new float[] { 0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 1, 0, };
                cm.set(array);
                colorPaint.setColorFilter(new ColorMatrixColorFilter(cm));
                InputStream is;
                Bitmap bitmap;
                int month = mPreMonth + 1;
                if (month < 11) {
                    is = new FileInputStream(mFilePath + "number/" + month + ".png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.translate(88, 239);
                    canvas.drawBitmap(bitmap, 0, 0, colorPaint);
                } else if (month == 11) {
                    is = new FileInputStream(mFilePath + "number/10.png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.translate(68, 239);
                    canvas.drawBitmap(bitmap, 0, 0, colorPaint);
                    is = new FileInputStream(mFilePath + "number/1.png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.translate(40, 0);
                    canvas.drawBitmap(bitmap, 0, 0, colorPaint);
                } else {
                    is = new FileInputStream(mFilePath + "number/10.png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.translate(68, 239);
                    canvas.drawBitmap(bitmap, 0, 0, colorPaint);
                    is = new FileInputStream(mFilePath + "number/2.png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.translate(40, 0);
                    canvas.drawBitmap(bitmap, 0, 0, colorPaint);
                }
                is = new FileInputStream(mFilePath + "yue.png");
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                canvas.translate(40, 0);
                canvas.drawBitmap(bitmap, 0, 0, colorPaint);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            String[] months = mContext.getResources().getStringArray(R.array.months);
            String text = months[mPreMonth];
            TextPaint titlePaint = new TextPaint();
            titlePaint.setTextSize(40);
            titlePaint.setColor(Color.WHITE);
            titlePaint.setAntiAlias(true);
            StaticLayout titleLayout = new StaticLayout(text, titlePaint, 256, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
            canvas.translate(0, 235);
            titleLayout.draw(canvas);
        }
        canvas.restore();
    }

    private void drawWeeks(Canvas canvas) {
        String[] weeks = mContext.getResources().getStringArray(R.array.weeks);
        TextPaint titlePaint = new TextPaint();
        titlePaint.setTextSize(18);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setAntiAlias(true);
        int left = 2;
        for (String week : weeks) {
            StaticLayout titleLayout = new StaticLayout(week, titlePaint, 36, Alignment.ALIGN_CENTER, 1.0f, 0.0F, false);
            canvas.save();
            canvas.translate(left, 279);
            titleLayout.draw(canvas);
            canvas.restore();
            left = left + 36;
        }
    }

    private void drawDays(Canvas canvas) {
        int curMonth = getMonth();
        int curDay = -1;
        if (curMonth == mPreMonth) {
            curDay = getDay();
        }
        int allDays = getDaysOfMonth(mPreMonth);
        int firstDayIndex = getFirstWeekDay(mPreMonth);
        TextPaint titlePaint = new TextPaint();
        titlePaint.setTextSize(19);
        titlePaint.setAntiAlias(true);
        for (int day = 0; day < allDays; day++) {
            int leftIndex = (firstDayIndex + day) % 7;
            int topIndex = (firstDayIndex + day) / 7;
            int left = 2 + leftIndex * 36;
            int top = 304 + topIndex * 34;
            if (curDay == day + 1) {
                titlePaint.setColor(Color.RED);
            } else {
                titlePaint.setColor(Color.WHITE);
            }
            StaticLayout titleLayout = new StaticLayout(Integer.toString(day + 1), titlePaint, 36,
                    Alignment.ALIGN_CENTER, 1.0f, 0.0F, false);
            canvas.save();
            canvas.translate(left, top);
            titleLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDisableTouch) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    public void showMonth() {
        if (!mOnDialogShow) {
            stopAllAnimation(null);
            mDisableTouch = true;
            mOnDialogShow = true;
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, true);
            mDialog = new CalendaryDialog(getHomeScene(), "CalendaryDialog", getMonth());
            getHomeScene().getContentObject().addChild(mDialog, true);
            getHomeScene().setTouchDelegate(mDialog);
            mShowLoc = getCamera().getScreenLocation(0.1f);
            mShowDialogAnimation = new ShowDialogAnimation(getHomeScene(), new SEVector3f(0, 600, 1000), mShowLoc);
            mShowDialogAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDialog.payVelocityAnimation(0, 1);
                    mDisableTouch = false;
                }
            });
            mShowDialogAnimation.execute();
//            getHomeScene().getDesk().hide(null);
            getHomeScene().hideDesk(null);

        }
    }

    public void hideMonth(boolean fast, final SEAnimFinishListener l) {
        if (mOnDialogShow) {
            mDisableTouch = true;
//            getHomeScene().getDesk().show(null);
            getHomeScene().showDesk(null);
            stopAllAnimation(null);
            mHideDialogAnimation = new HideDialogAnimation(getHomeScene(), new SEVector3f(0, 600, 1000));
            mHideDialogAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mDisableTouch = false;
                    getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_SIGHT, false);
                    getHomeScene().removeTouchDelegate();
                    mOnDialogShow = false;
                    getHomeScene().getContentObject().removeChild(mDialog, true);
                    if (l != null) {
                        l.onAnimationfinish();
                    }
                }
            });
            mHideDialogAnimation.execute();
        }
    }

    public void stopAllAnimation(SEAnimFinishListener l) {
        if (mShowDialogAnimation != null) {
            mShowDialogAnimation.stop();
        }
        if (mHideDialogAnimation != null) {
            mHideDialogAnimation.stop();
        }
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (mOnDialogShow) {
            hideMonth(false, l);
        }
        return false;
    }

    private class ShowDialogAnimation extends CountAnimation {
        private SEVector3f mEndLocation;
        private SEVector3f mCurLocation;
        private SEVector3f mStepLocation;
        private SEVector3f mStepScale;
        private SEVector3f mCurScale;
        private SEVector3f mEndScale;

        public ShowDialogAnimation(SEScene scene, SEVector3f startLocation, SEVector3f endLocation) {
            super(scene);
            mCurLocation = startLocation.clone();
            mEndLocation = endLocation.clone();
            mCurScale = new SEVector3f(0.5f, 0.5f, 0.5f);
            mEndScale = new SEVector3f(1, 1, 1);
        }

        public void runPatch(int count) {
            if (mStepLocation != null) {
                mCurLocation.selfAdd(mStepLocation);
                if (mEndLocation.subtract(mCurLocation).getLength() <= mStepLocation.getLength()) {
                    mCurLocation = mEndLocation;
                    mStepLocation = null;
                }
                mDialog.setUserTranslate(mCurLocation);
            }
            if (mStepScale != null) {
                mCurScale.selfAdd(mStepScale);
                if (mEndScale.subtract(mCurScale).getLength() <= mStepScale.getLength()) {
                    mCurScale = mEndScale;
                    mStepScale = null;
                }
                mDialog.setUserScale(mCurScale);
            }

            if (mStepLocation == null && mStepScale == null) {
                stop();
            }
        }

        @Override
        public void onFirstly(int count) {
            mDialog.setVisible(true);
            if (!mCurLocation.equals(mEndLocation)) {
                mStepLocation = mEndLocation.subtract(mCurLocation).selfDiv(10);
                if (mStepLocation.getLength() < 20) {
                    mStepLocation.normalize();
                    mStepLocation.selfMul(20);
                }
            }
            if (!mCurScale.equals(mEndScale)) {
                mStepScale = mEndScale.subtract(mCurScale).selfDiv(10);
                if (mStepScale.getLength() < 0.17f) {
                    mStepScale.normalize();
                    mStepScale.selfMul(0.17f);
                }
            }
        }
    }

    private class HideDialogAnimation extends CountAnimation {
        private SEVector3f mEndLocation;
        private SEVector3f mCurLocation;
        private SEVector3f mStepLocation;
        private SEVector3f mStepScale;
        private SEVector3f mCurScale;
        private SEVector3f mEndScale;

        public HideDialogAnimation(SEScene scene, SEVector3f endLocation) {
            super(scene);
            mEndLocation = endLocation.clone();
            mEndScale = new SEVector3f(0.5f, 0.5f, 0.5f);
        }

        public void runPatch(int count) {
            if (mStepLocation != null) {
                mCurLocation.selfAdd(mStepLocation);
                if (mEndLocation.subtract(mCurLocation).getLength() <= mStepLocation.getLength()) {
                    mCurLocation = mEndLocation;
                    mStepLocation = null;
                }
                mDialog.setUserTranslate(mCurLocation);
            }
            if (mStepScale != null) {
                mCurScale.selfAdd(mStepScale);
                if (mEndScale.subtract(mCurScale).getLength() <= mStepScale.getLength()) {
                    mCurScale = mEndScale;
                    mStepScale = null;
                }
                mDialog.setUserScale(mCurScale);
            }

            if (mStepLocation == null && mStepScale == null) {
                stop();
            }
        }

        @Override
        public void onFirstly(int count) {
            mCurLocation = mDialog.getUserTranslate();
            if (!mCurLocation.equals(mEndLocation)) {
                mStepLocation = mEndLocation.subtract(mCurLocation).selfDiv(10);
                if (mStepLocation.getLength() < 20) {
                    mStepLocation.normalize();
                    mStepLocation.selfMul(20);
                }
            }
            mCurScale = mDialog.getUserScale();
            if (!mCurScale.equals(mEndScale)) {
                mStepScale = mEndScale.subtract(mCurScale).selfDiv(10);
                if (mStepScale.getLength() < 0.17f) {
                    mStepScale.normalize();
                    mStepScale.selfMul(0.17f);
                }
            }
        }
    }

    private class CalendaryDialog extends SEObject {
        private static final String IMAGE_KEY = "CalendaryDialog_face_image_key";
        private static final String IMAGE_NAME = "CalendaryDialog_face_image_name";
        private SEObject mHeadBody;
        private SEObject mHeadIn;
        private float[] mVertexArray;
        private final static int FACE_N = 500;
        private VelocityTracker mVelocityTracker;
        private VelocityAnimation mVelocityAnimation;
        private SwitchDialogAnimation mSwitchDialogAnimation;
        private int mYTex;
        private final static int YTEX_MIN = 0;
        private final static int YTEX_MAX = 492;
        private float mW;
        private float mH;
        private float mR;
        private boolean mHasGotAction;
        private boolean mActionUp;
        private float mDistance;
        private int mCurrentMonth;
        private Bitmap mBaseImage;

        public CalendaryDialog(SEScene scene, String name, int currentMonth) {
            super(scene, name);
            setIsGroup(true);
            setPressType(PRESS_TYPE.COLOR);
            mCurrentMonth = currentMonth;
            mHasGotAction = false;
            mActionUp = false;
            setOnClickListener(new OnTouchListener() {

                public void run(SEObject obj) {
                    handOnClick();
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (mDisableTouch) {
                return true;
            }
            trackVelocity(event);
            return super.dispatchTouchEvent(event);
        }

        private void payVelocityAnimation(float v, float step) {
            stopAllAnimation(null);
            mDisableTouch = true;
            mVelocityAnimation = new VelocityAnimation(getHomeScene(), v, step);
            mVelocityAnimation.setAnimFinishListener(new SEAnimFinishListener() {

                public void onAnimationfinish() {
                    mDisableTouch = false;
                }
            });
            mVelocityAnimation.execute();
        }

        private void trackVelocity(MotionEvent ev) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAllAnimation(null);
                setPreTouch();
                mHasGotAction = false;
                setPressed(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dY = getTouchY() - getPreTouchY();
                float dX = getTouchX() - getPreTouchX();
                if (!mHasGotAction) {
                    mActionUp = (Math.abs(dY) > Math.abs(dX));
                    mHasGotAction = true;
                }
                if (mActionUp) {
                    mYTex += dY / 2;
                    if (mYTex < 0) {
                        mYTex = 0;
                        hideMonth(false, null);
                    } else if (mYTex > FACE_N) {
                        mYTex = FACE_N;
                    }
                    updateVertex(mYTex, mR, FACE_N, mW, mH);
                    mHeadBody.updateVertex_JNI(mVertexArray);
                } else {
                    mDistance = mDistance + dX;
                    SEVector3f location = new SEVector3f(mDistance / 10, Math.abs(mDistance / 10), 0);
                    setUserTranslate(mShowLoc.add(location));
                }
                setPressed(false);
                setPreTouch();
                break;
            case MotionEvent.ACTION_UP:
                setPreTouch();
                if (isPressed() && mDistance == 0) {
                    performClick();
                }
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                if (mActionUp) {
                    payVelocityAnimation(mVelocityTracker.getYVelocity(), 3);
                } else {
                    float end = 0;
                    boolean needSwitch = false;
                    if (mVelocityTracker.getXVelocity() < -200) {
                        needSwitch = true;
                        end = -getCamera().getWidth();
                    } else if (mVelocityTracker.getXVelocity() > 200) {
                        needSwitch = true;
                        end = getCamera().getWidth();
                    } else {
                        if (mDistance < -getCamera().getWidth() / 2) {
                            needSwitch = true;
                            end = -getCamera().getWidth();
                        } else if (mDistance > getCamera().getWidth() / 2) {
                            needSwitch = true;
                            end = getCamera().getWidth();
                        }
                    }
                    stopAllAnimation(null);
                    mDisableTouch = true;
                    mSwitchDialogAnimation = new SwitchDialogAnimation(getHomeScene(), end, needSwitch);
                    mSwitchDialogAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                        public void onAnimationfinish() {
                            mDisableTouch = false;
                        }
                    });
                    mSwitchDialogAnimation.execute();
                }
                break;
            }
            return true;
        }

        @Override
        public void stopAllAnimation(SEAnimFinishListener l) {
            if (mSwitchDialogAnimation != null) {
                mSwitchDialogAnimation.stop();
            }
            if (mVelocityAnimation != null) {
                mVelocityAnimation.stop();
            }
        }

        private void createBody(SEObject obj, int faceNum, float w, float h, String imageName, String imageKey,
                Bitmap bitmap) {
            int vertexPointNum = faceNum * 2 + 2;
            mVertexArray = new float[vertexPointNum * 3];
            float stepH = h / faceNum;
            for (int i = 0; i < vertexPointNum; i++) {
                if ((i & 1) == 0) {
                    mVertexArray[3 * i] = -w / 2;
                } else {
                    mVertexArray[3 * i] = w / 2;
                }
                mVertexArray[3 * i + 2] = stepH * (i / 2) - h / 2;
                mVertexArray[3 * i + 1] = 0;
            }
            obj.setVertexArray(mVertexArray);
            int[] faceArray = new int[(vertexPointNum - 2) * 3];
            for (int i = 0; i < faceNum; i++) {
                faceArray[6 * i] = 2 * i;
                faceArray[6 * i + 1] = 2 * i + 1;
                faceArray[6 * i + 2] = 2 * i + 3;

                faceArray[6 * i + 3] = 2 * i + 3;
                faceArray[6 * i + 4] = 2 * i + 2;
                faceArray[6 * i + 5] = 2 * i;
            }
            obj.setFaceArray(faceArray);
            float[] texVertexArray = new float[vertexPointNum * 2];
            stepH = 1f / faceNum;
            for (int i = 0; i < vertexPointNum; i++) {
                texVertexArray[2 * i + 1] = (i / 2) * stepH;
                if ((i & 1) == 0) {
                    texVertexArray[2 * i] = 0;
                } else {
                    texVertexArray[2 * i] = 1;
                }
            }
            obj.setTexVertexArray(texVertexArray);
            obj.setImage(IMAGE_TYPE.BITMAP, imageName, imageKey);
            obj.setBitmap(bitmap);
            obj.setBlendingable(false);
            obj.setLocalTranslate(new SEVector3f(0, 0, -mR));
        }

        private void updateVertex(int faceIndex, float r, int faceNum, float w, float h) {
            float stepLength = h / faceNum;
            double perAngle = 2 * Math.asin(stepLength / (2 * r));
            int vertexPointNum = faceNum * 2 + 2;
            float stepH = h / faceNum;
            float mCompensation = -h / 2 + (faceNum - faceIndex) * stepH / 2;
            for (int i = 0; i < vertexPointNum; i++) {
                if (i < 2 * faceIndex) {
                    if ((i & 1) == 0) {
                        mVertexArray[3 * i] = -w / 2;
                    } else {
                        mVertexArray[3 * i] = w / 2;
                    }
                    mVertexArray[3 * i + 2] = stepH * (i / 2) + mCompensation;
                    mVertexArray[3 * i + 1] = 0;
                } else {
                    int newPoint = i - 2 * faceIndex;
                    double angle = (newPoint / 2) * perAngle;
                    if (angle >= Math.PI * 2 - 2 * perAngle) {
                        angle = Math.PI * 2 - 2 * perAngle;
                    }
                    mVertexArray[3 * i + 1] = (float) (r * Math.cos(angle) - r);
                    mVertexArray[3 * i + 2] = (float) (r * Math.sin(angle)) + faceIndex * stepH + mCompensation;
                    if ((newPoint & 1) == 0) {
                        mVertexArray[3 * i] = -w / 2;
                    } else {
                        mVertexArray[3 * i] = w / 2;
                    }

                }
            }
            if (mHeadIn != null) {
                float down = mCompensation + h / 2;
                mHeadIn.setUserTranslate(new SEVector3f(0, 0, -down));
            }
        }

        @Override
        public void onRenderFinish(final SECamera camera) {
            super.onRenderFinish(camera);
            setVisible(false);
            getHomeScene().getContentObject().addChild(mDialog, true);
            mDistance = 0;
            mHeadBody = new SEObject(getHomeScene(), "CalendaryDialog_face");
            mW = getCamera().getHeight() / 20.f;
            mH = mW * 2;
            mR = 0.06f * mW;
            createBody(mHeadBody, FACE_N, mW, mH, IMAGE_NAME, IMAGE_KEY, getBitmap(mCurrentMonth));
            mHeadBody.setClickable(false);
            addChild(mHeadBody, true);

            mHeadIn = new SEObject(getHomeScene(), "CalendaryDialog_headin");
            SEObjectFactory.createCylinder(mHeadIn, 50, mR * 0.8f, 1.1f * mW, new float[] { 0.5f, 0.5f, 0.5f });
            mHeadIn.getLocalTransParas().mRotate.set(90, 0, 1, 0);
            mHeadIn.getLocalTransParas().mTranslate.set(0, -mR, mH / 2 - mR);
            mHeadIn.setClickable(false);
            addChild(mHeadIn, true);
            getHomeScene().addDirectLightToScene("calendar_light", new float[] { 0, 1, 0 });
            mHeadIn.applyLight_JNI("calendar_light");
            mYTex = FACE_N;
        }

        @Override
        public void release() {
            if (mHeadIn != null) {
                mHeadIn.unApplyLight_JNI("calendar_light");
            }
            getHomeScene().removeLightFromScene_JNI("calendar_light");
            super.release();
        }

        private Bitmap getBitmap(int monthIndex) {
            InputStream is_one = null;
            Bitmap bitmapFrame = null;
            try {
                if (mBaseImage == null) {
                    is_one = new FileInputStream(mFilePath + "gualiD_g.jpg");
                    mBaseImage = BitmapFactory.decodeStream(is_one);
                    is_one.close();
                }
                int index = monthIndex + 1;
                String picIndex = null;
                if (index < 10) {
                    picIndex = "0" + index;
                } else {
                    picIndex = Integer.toString(index);
                }
                is_one = new FileInputStream(mFilePath + "guali" + picIndex + ".jpg");
                bitmapFrame = BitmapFactory.decodeStream(is_one);
                is_one.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Bitmap des = mBaseImage.copy(Bitmap.Config.RGB_565, true);
            Canvas canvas = new Canvas(des);
            Rect srcRect = new Rect(0, 0, bitmapFrame.getWidth(), bitmapFrame.getHeight());
            Rect desRect = new Rect(37, 37, 219, 310);
            canvas.drawBitmap(bitmapFrame, srcRect, desRect, new Paint());
            bitmapFrame.recycle();
            drawMonth(canvas, monthIndex);
            drawWeeks(canvas);
            drawDays(canvas, monthIndex);
            return des;
        }

        private void drawMonth(Canvas canvas, int monthIndex) {
            String able = getContext().getResources().getConfiguration().locale.getCountry();
            if (able.equals("CN")) {
                try {
                    int index = monthIndex + 1;
                    canvas.save();
                    canvas.translate(256 - 30 - 32, 19);
                    InputStream is;
                    Bitmap bitmap;
                    if (index < 11) {
                        is = new FileInputStream(mFilePath + "number/" + index + ".png");
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                    } else if (index == 11) {
                        is = new FileInputStream(mFilePath + "number/10.png");
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        canvas.translate(0, bitmap.getHeight());
                        is = new FileInputStream(mFilePath + "number/1.png");
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                    } else {
                        is = new FileInputStream(mFilePath + "number/10.png");
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        canvas.translate(0, bitmap.getHeight());
                        is = new FileInputStream(mFilePath + "number/2.png");
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                    }
                    canvas.translate(0, bitmap.getHeight());
                    is = new FileInputStream(mFilePath + "yue.png");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    canvas.restore();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                String[] months = mContext.getResources().getStringArray(R.array.months);
                String text = months[monthIndex];
                TextPaint titlePaint = new TextPaint();
                titlePaint.setTextSize(29);
                titlePaint.setColor(0xff3f5f75);
                titlePaint.setAntiAlias(true);
                int w = (int) (StaticLayout.getDesiredWidth(text, titlePaint) + 1);
                StaticLayout titleLayout = new StaticLayout(text, titlePaint, w, Alignment.ALIGN_CENTER, 0.8f, 0.0F,
                        false);
                canvas.save();
                canvas.translate(256 - 30 - w, 19);
                titleLayout.draw(canvas);
                canvas.restore();
            }
        }

        private void drawWeeks(Canvas canvas) {
            String[] weeks = mContext.getResources().getStringArray(R.array.weeks);
            TextPaint titlePaint = new TextPaint();
            titlePaint.setTextSize(14);
            titlePaint.setColor(0xff3f5f75);
            titlePaint.setAntiAlias(true);
            int left = 30;
            for (String week : weeks) {
                StaticLayout titleLayout = new StaticLayout(week, titlePaint, 28, Alignment.ALIGN_CENTER, 1.0f, 0.0F,
                        false);
                canvas.save();
                canvas.translate(left, 332);
                titleLayout.draw(canvas);
                canvas.restore();
                left = left + 28;
            }
        }

        private void drawDays(Canvas canvas, int monthIndex) {
            int curMonth = getMonth();
            int curDay = -1;
            if (curMonth == monthIndex) {
                curDay = getDay();
            }
            int allDays = getDaysOfMonth(monthIndex);
            int firstDayIndex = getFirstWeekDay(monthIndex);
            TextPaint titlePaint = new TextPaint();
            titlePaint.setTextSize(14);
            titlePaint.setAntiAlias(true);
            for (int day = 0; day < allDays; day++) {
                int leftIndex = (firstDayIndex + day) % 7;
                int topIndex = (firstDayIndex + day) / 7;
                int left = 30 + leftIndex * 28;
                int top = 352 + topIndex * 22;
                if (curDay == day + 1) {
                    titlePaint.setColor(Color.RED);
                } else {
                    titlePaint.setColor(0xff3f5f75);
                }
                StaticLayout titleLayout = new StaticLayout(Integer.toString(day + 1), titlePaint, 28,
                        Alignment.ALIGN_CENTER, 1.0f, 0.0F, false);
                canvas.save();
                canvas.translate(left, top);
                titleLayout.draw(canvas);
                canvas.restore();
            }
        }

        private class VelocityAnimation extends CountAnimation {
            private float mVelocity;
            private int mDesTranslateX;
            private float mStep;

            public VelocityAnimation(SEScene scene, float velocity, float step) {
                super(scene);
                if (Math.abs(velocity) < 100) {
                    mVelocity = 0;
                } else {
                    mVelocity = velocity;
                }
                mStep = step;
            }

            public void runPatch(int count) {
                int needTranslateX = mDesTranslateX - mYTex;
                int absNTX = Math.abs(needTranslateX);
                if (absNTX <= mStep) {
                    mYTex = mDesTranslateX;
                    if (mYTex <= YTEX_MIN) {
                        hideMonth(false, null);
                    }
                    stop();
                    updateVertex(mYTex, mR, FACE_N, mW, mH);
                } else {
                    int step = (int) (mStep * Math.sqrt(absNTX));
                    if (needTranslateX < 0) {
                        step = -step;
                    }
                    mYTex = mYTex + step;
                    updateVertex(mYTex, mR, FACE_N, mW, mH);
                    mHeadBody.updateVertex_JNI(mVertexArray);
                }

            }

            @Override
            public void onFirstly(int count) {
                int needTranslateX = (int) (mVelocity * 0.2f);
                mDesTranslateX = mYTex + needTranslateX;
                if (mDesTranslateX < YTEX_MIN) {
                    mDesTranslateX = YTEX_MIN;
                } else if (mDesTranslateX > YTEX_MAX) {
                    mDesTranslateX = YTEX_MAX;
                } else {
                    mDesTranslateX = YTEX_MAX;
                }
            }

        }

        private class SwitchDialogAnimation extends TimeAnimation {
            private float mEnd;
            private boolean mHasSwitch;
            private boolean mNeedSwitch;
            private float mNeedRunDistance1;
            private float mNeedRunDistance2;
            private long mTime;
            private float mRunDistance;

            public SwitchDialogAnimation(SEScene scene, float end, boolean needSwitch) {
                super(scene);
                mEnd = end;
                mHasSwitch = false;
                mNeedSwitch = needSwitch;
                if (mNeedSwitch) {
                    mTime = 500;
                } else {
                    mTime = 100;
                }
            }

            @Override
            public void onFirstly(long leftTime) {
                if (!mNeedSwitch) {
                    mNeedRunDistance1 = mEnd - mDistance;
                    mNeedRunDistance2 = 0;
                } else {
                    mNeedRunDistance1 = mEnd - mDistance;
                    mNeedRunDistance2 = mEnd;
                }
            }

            public void runPatch(long leftTime) {
                if (leftTime < 0) {
                    leftTime = 0;
                }
                if (!mNeedSwitch) {
                    mRunDistance = mDistance + mNeedRunDistance1 * (mTime - leftTime) / mTime;
                } else {
                    mRunDistance = (Math.abs(mNeedRunDistance1) + Math.abs(mNeedRunDistance2)) * (mTime - leftTime)
                            / mTime;
                    if (mRunDistance > Math.abs(mNeedRunDistance1)) {
                        mRunDistance = mRunDistance - Math.abs(mNeedRunDistance1);
                        if (!mHasSwitch) {
                            mHasSwitch = true;
                            mDistance = -mEnd;
                            mEnd = 0;
                            if (mDistance > 0) {
                                mCurrentMonth = mCurrentMonth + 1;
                            } else {
                                mCurrentMonth = mCurrentMonth - 1;
                            }
                            if (mCurrentMonth < 0) {
                                mCurrentMonth = 11;
                            } else if (mCurrentMonth > 11) {
                                mCurrentMonth = 0;
                            }
                            SELoadResThread.getInstance().process(new Runnable() {
                                public void run() {
                                    Bitmap bitmap = getBitmap(mCurrentMonth);
                                    final int imageData = loadImageData_JNI(bitmap);
                                    bitmap.recycle();
                                    new SECommand(getHomeScene()) {
                                        public void run() {
                                            addImageData_JNI(IMAGE_KEY, imageData);
                                        }
                                    }.execute();
                                }
                            });
                            SEVector3f location = new SEVector3f(mEnd / 10, Math.abs(mEnd / 10), 0);
                            setUserTranslate(mShowLoc.add(location));
                        }
                        if (mDistance > 0) {
                            mRunDistance = mDistance - mRunDistance;
                        } else {
                            mRunDistance = mDistance + mRunDistance;
                        }
                    } else {
                        if (mEnd > 0) {
                            mRunDistance = mDistance + mRunDistance;
                        } else {
                            mRunDistance = mDistance - mRunDistance;
                        }
                    }
                }
                SEVector3f location = new SEVector3f(mRunDistance / 10, Math.abs(mRunDistance / 10), 0);
                setUserTranslate(mShowLoc.add(location));
                if (mRunDistance == mEnd) {
                    mDistance = mEnd;
                }
            }

            @Override
            public long getAnimationTime() {
                return mTime;
            }
        }
    }

}
