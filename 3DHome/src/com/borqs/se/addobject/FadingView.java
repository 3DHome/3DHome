/*
 * Copyright © 2013 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, 
 * or disclosed, or disseminated outside of Borqs in whole or in part without Borqs 's permission.
 */

/* 
 * Description of the content in this file. 
 * 
 * History core-id Change purpose 
 * ---------- ------- ---------------------------------------------- 
 *
 */


package com.borqs.se.addobject;

import com.borqs.se.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;


/**
 * for fading  
 */
public class FadingView extends View {

    private Paint mPaint = null;
    private Shader mLGradient = null;

    private boolean mIsVisible = true;

    private boolean mIsFadeinAniDoing = false;
    private boolean mIsFadeOutAniDoing = false;

    
    public FadingView(Context context) {
        super(context);
    }
    
    public FadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        String tag = (String)getTag();
        if ((tag == null) || (tag.equals(""))) { return; }
        int w = getFVWidth(context);
        if (tag.equals("0")) {
            mLGradient = new LinearGradient(0, 0, w, 0, 0xee000000, 0x00333333, Shader.TileMode.CLAMP);
        } else {
            mLGradient = new LinearGradient(w, 0, 0, 0, 0xee000000, 0x00333333, Shader.TileMode.CLAMP);
        }
        mPaint = new Paint();
    }
    
    public FadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setShader(mLGradient);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }

    public static int getFVWidth(Context context) {
        return (int)(context.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_object_w) * 0.8f);
    }
    
    public void setVisibility(boolean bl) {
        float start = 0.0f;
        float end = 0.0f;
        if (mIsVisible == bl) { return; }
        if (bl == true) {
            if (mIsFadeinAniDoing == true) {
                return;
            }
        } else {
            if (mIsFadeOutAniDoing == true) {
                return;
            }
        }
        mIsVisible = bl; 
        if (bl == true) {
            setVisibility(View.VISIBLE);
            mIsFadeinAniDoing = true; 
            start = 0.0f;
            end = 1.0f;
        } else {
            mIsFadeOutAniDoing = true; 
            start = 1.0f;
            end = 0.0f;
        }
        
        final AlphaAnimation animation = new AlphaAnimation(start, end);
        animation.setDuration(450);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        animation.setFillBefore(false);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mIsFadeinAniDoing = false;
                mIsFadeOutAniDoing = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        setAnimation(animation);
        startAnimation(animation);
    }    
}
