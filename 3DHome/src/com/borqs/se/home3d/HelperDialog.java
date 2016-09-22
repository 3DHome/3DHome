package com.borqs.se.home3d;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;

public class HelperDialog extends Dialog {
//    private Context mContext;
    private int mScreenW;
//    private MyLayout mMyLayout;

    public HelperDialog(Context context) {
        super(context, R.style.HelperDialogStyle);
//        mContext = context;
        Configuration config = new Configuration();
        android.provider.Settings.System.getConfiguration(context.getContentResolver(), config);
        mScreenW = context.getResources().getDisplayMetrics().widthPixels;
        MyLayout myLayout = new MyLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT);
        myLayout.setLayoutParams(layoutParams);
        setContentView(myLayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        float pixDensity = context.getResources().getDisplayMetrics().density;
        params.leftMargin = (int) (6 * pixDensity);
        params.rightMargin = (int) (6 * pixDensity);
        LinearLayout view0 = (LinearLayout) getLayoutInflater().inflate(R.layout.helper_view_layout, null);
        view0.setBackgroundResource(R.drawable.help_bg_00);
        ViewGroup.LayoutParams helpLayout0 = new ViewGroup.LayoutParams(mScreenW, ViewGroup.LayoutParams.MATCH_PARENT);
        view0.setLayoutParams(helpLayout0);
        TextView helpTitle0 = (TextView) view0.findViewById(R.id.title);
        TextView helpTitleIllustrate0 = (TextView) view0.findViewById(R.id.title_illustrate);
        helpTitle0.setText(R.string.help_title_00);
        helpTitleIllustrate0.setText(R.string.help_content_00);
        LinearLayout page0 = (LinearLayout) view0.findViewById(R.id.pages);

        ImageView dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_visible);
        page0.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page0.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page0.addView(dot, params);
        myLayout.addView(view0);

        LinearLayout view1 = (LinearLayout) getLayoutInflater().inflate(R.layout.helper_view_layout, null);
        view1.setBackgroundResource(R.drawable.help_bg_01);
        ViewGroup.LayoutParams helpLayout1 = new ViewGroup.LayoutParams(mScreenW, ViewGroup.LayoutParams.MATCH_PARENT);
        view1.setLayoutParams(helpLayout1);
        TextView helpTitle1 = (TextView) view1.findViewById(R.id.title);
        TextView helpTitleIllustrate1 = (TextView) view1.findViewById(R.id.title_illustrate);
        helpTitle1.setText(R.string.help_title_01);
        helpTitleIllustrate1.setText(R.string.help_content_01);
        LinearLayout page1 = (LinearLayout) view1.findViewById(R.id.pages);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page1.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_visible);
        page1.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page1.addView(dot, params);
        myLayout.addView(view1);
        view1.setTranslationX(mScreenW);

        LinearLayout view2 = (LinearLayout) getLayoutInflater().inflate(R.layout.helper_view_layout, null);
        view2.setBackgroundResource(R.drawable.help_bg_02);
        ViewGroup.LayoutParams helpLayout2 = new ViewGroup.LayoutParams(mScreenW, ViewGroup.LayoutParams.MATCH_PARENT);
        view2.setLayoutParams(helpLayout2);
        TextView helpTitle2 = (TextView) view2.findViewById(R.id.title);
        TextView helpTitleIllustrate2 = (TextView) view2.findViewById(R.id.title_illustrate);
        helpTitle2.setText(R.string.help_title_02);
        helpTitleIllustrate2.setText(R.string.help_content_02);
        LinearLayout page2 = (LinearLayout) view2.findViewById(R.id.pages);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page2.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_invisible);
        page2.addView(dot, params);
        dot = new ImageView(context);
        dot.setBackgroundResource(R.drawable.dot_visible);
        page2.addView(dot, params);
        myLayout.addView(view2);
        view2.setTranslationX(mScreenW * 2);
        getWindow().setWindowAnimations(R.style.helper_dialog_anim_style);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setBackgroundDrawableResource(R.drawable.helper_translucent_background);
    }

    @Override
    public void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        getWindow().setBackgroundDrawableResource(R.drawable.transparent_background);
    }

    private class MyLayout extends FrameLayout {
        private VelocityTracker mVelocityTracker;
        private float mIndex = 0;
        private int mFaceAIndex = -1;
        private int mFaceBIndex = -1;
        private View mWallA;
        private View mWallB;

        private int mPreTouchX;
        private int mTouchSlop;
        private ToFaceAnimation mToFaceAnimation;

        public MyLayout(Context context) {
            super(context);
            setClickable(true);
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            updatePosition(0, true);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
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
        public boolean onTouchEvent(MotionEvent event) {
            trackVelocity(event);
            int touchX = (int) event.getX();
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mToFaceAnimation != null) {
                    mToFaceAnimation.stop();
                }
                mPreTouchX = touchX;
                break;
            case MotionEvent.ACTION_MOVE:
                int dX = mPreTouchX - touchX;
                if (Math.abs(dX) > mTouchSlop / 2) {
                    float ratio = 1.5f / mScreenW;
                    float transIndex = ratio * dX;
                    float curIndex = mIndex + transIndex;
                    int size = getChildCount();
                    float minX = -0.45f;
                    float maxX = size - 1 + 0.45f;
                    if (curIndex < minX) {
                        curIndex = minX;
                    } else if (curIndex > maxX) {
                        curIndex = maxX;
                    }
                    updatePosition(curIndex, false);
                    mPreTouchX = touchX;
                    if (curIndex == maxX) {
                        HelperDialog.this.cancel();
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPreTouchX = touchX;
                payVelocityAnimation(mVelocityTracker.getXVelocity());
                break;
            }
            return true;
        }

        private void payVelocityAnimation(float vX) {
            float desIndex;
            if (mVelocityTracker.getXVelocity() > 500) {
                desIndex = (int) mIndex;
            } else if (mVelocityTracker.getXVelocity() < -500) {
                desIndex = (int) mIndex + 1;
            } else {
                desIndex = Math.round(mIndex);
            }

            int size = getChildCount();
            int minX = 0;
            int maxX = size - 1;
            if (desIndex < minX) {
                desIndex = minX;
            } else if (desIndex > maxX) {
                desIndex = maxX;
            }
            if (mToFaceAnimation != null) {
                mToFaceAnimation.stop();
            }
            if (desIndex != mIndex) {
                int animationTimes = (int) (Math.abs(desIndex - mIndex) * 800);
                mToFaceAnimation = new ToFaceAnimation(mIndex, desIndex, animationTimes) {
                    @Override
                    public void onAnimationRun(float value) {
                        updatePosition(value, false);
                    }
                };
                mToFaceAnimation.setInterpolator(new DecelerateInterpolator(1));
                mToFaceAnimation.execute();
            }
        }

        private class ToFaceAnimation {
            private float mFrom;
            private float mTo;
            private Interpolator mInterpolator;
            private long mPreTime;
            private long mHasRunTime;
            private long mAnimationTime = Long.MAX_VALUE;
            private boolean mIsFinished = false;
            private boolean mHasBeenRun = false;
            private Handler mHandler;

            public ToFaceAnimation(float from, float to, long animationTime) {
                mFrom = from;
                mTo = to;
                mAnimationTime = animationTime;
                mHasRunTime = 0;
                mHandler = new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 0) {
                            run();
                            if (!isFinish()) {
                                sendEmptyMessage(0);
                            }
                        }
                    }

                };
            }

            public void setInterpolator(Interpolator interpolator) {
                mInterpolator = interpolator;
            }

            public void execute() {
                mHandler.sendEmptyMessage(0);
            }

            public void run() {
                if (!isFinish()) {
                    if (!mHasBeenRun) {
                        mHasBeenRun = true;
                        if (SESceneManager.USING_TIME_ANIMATION) {
                            mPreTime = System.currentTimeMillis();
                        }
                    }

                    long currentTime = System.currentTimeMillis();
                    long usingTime = currentTime - mPreTime;
                    mPreTime = currentTime;
                    if (usingTime > 100) {
                        usingTime = 100;
                    }
                    mHasRunTime += usingTime;
                    if (mHasRunTime > mAnimationTime) {
                        mHasRunTime = mAnimationTime;
                    }

                    if (!isFinish()) {
                        float interpolatorValue = (float) mHasRunTime / (float) mAnimationTime;
                        if (mInterpolator != null) {
                            interpolatorValue = mInterpolator.getInterpolation(interpolatorValue);
                        }
                        float distance = mTo - mFrom;
                        float value = mFrom + distance * interpolatorValue;
                        onAnimationRun(value);
                    }
                    if (mHasRunTime >= mAnimationTime) {
                        stop();
                    }
                }
            }

            public void onAnimationRun(float value) {

            }

            public void stop() {
                if (!isFinish()) {
                    mIsFinished = true;
                }
            }

            public boolean isFinish() {
                return mIsFinished;
            }
        }

        private void updatePosition(float index, boolean force) {
            if (mIndex == index && !force) {
                return;
            }
            mIndex = index;
            int size = getChildCount();
            int faceAIndex;
            int faceBIndex;
            if (mIndex < 0) {
                faceAIndex = (int) mIndex - 1;
                faceBIndex = (int) mIndex;
            } else {
                faceAIndex = (int) mIndex;
                faceBIndex = (int) mIndex + 1;
            }
            if (mFaceAIndex != faceAIndex || mFaceBIndex != faceBIndex || force) {
                mWallA = null;
                mWallB = null;
                for (int i = 0; i < size; i++) {
                    View child = getChildAt(i);
                    int slotIndex = i;
                    if (slotIndex == faceAIndex) {
                        mWallA = child;
                        mWallA.setVisibility(View.VISIBLE);
                    } else if (slotIndex == faceBIndex) {
                        mWallB = child;
                        mWallB.setVisibility(View.VISIBLE);
                    } else {
                        child.setVisibility(View.INVISIBLE);
                    }
                }
            }
            float wallIndex;
            if (index < 0) {
                wallIndex = 1 + index % 1;
            } else {
                wallIndex = index % 1;
            }
            float wallAX = -wallIndex;
            float wallBX = 1 - wallIndex;
            if (mWallA != null) {
                mWallA.setTranslationX(wallAX * mScreenW * 0.8f);
                mWallA.setScaleX(1 - wallIndex * 0.5f);
                mWallA.setScaleY(1 - wallIndex * 0.5f);
            }
            if (mWallB != null) {
                mWallB.setTranslationX(wallBX * mScreenW * 0.8f);
                mWallB.setScaleX(0.5f + wallIndex * 0.5f);
                mWallB.setScaleY(0.5f + wallIndex * 0.5f);
            }
        }

    }

}
