package com.borqs.se.home3d;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;


public class OptionMenu extends PopupWindow {
    private TextView mMenu0, mMenu1, mMenu2, mMenu3;
    private Rotate3d mAnimation0, mAnimation1, mAnimation2, mAnimation3;
    private RotateListener mListener0, mListener1, mListener2, mListener3;

    public OptionMenu(Context context) {
        super(context, null, R.style.OptionMenuWindow);
        
        FrameLayout view = (FrameLayout)SESceneManager.getInstance().getGLActivity().getLayoutInflater()
                .inflate(R.layout.option_menu_pop, null);
        String[] menuStrings = context.getResources().getStringArray(R.array.menus);
        mMenu0 = (TextView)view.findViewById(R.id.menu00);
        mMenu0.setText(menuStrings[0]);
        mMenu0.setOnClickListener(mClickListener);
        mMenu1 = (TextView)view.findViewById(R.id.menu01);
        mMenu1.setOnClickListener(mClickListener);
        mMenu1.setText(menuStrings[1]);
        mMenu2 = (TextView)view.findViewById(R.id.menu02);
        mMenu2.setOnClickListener(mClickListener);
        mMenu2.setText(menuStrings[2]);
        mMenu3 = (TextView)view.findViewById(R.id.menu03);
        mMenu3.setOnClickListener(mClickListener);
        mMenu3.setText(menuStrings[3]);
        
        view.setOnKeyListener(mOnKeyListener);
        view.setFocusableInTouchMode(true);
        setContentView(view);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        
        setAnimationStyle(R.style.option_menu_anim_style);
        
        mAnimation0 = new Rotate3d(180, 0, mMenu0);
        mAnimation0.setDuration(700);
        mAnimation0.setFillAfter(false);
        mAnimation0.setInterpolator(new AccelerateInterpolator());
        mListener0 = new RotateListener(mMenu0);
        mAnimation0.setAnimationListener(mListener0);
        
        mAnimation1 = new Rotate3d(180, 0, mMenu1);
        mAnimation1.setDuration(700);
        mAnimation1.setFillAfter(false);
        mAnimation1.setInterpolator(new AccelerateInterpolator());
        mListener1 = new RotateListener(mMenu1);
        mAnimation1.setAnimationListener(mListener1);
        
        mAnimation2 = new Rotate3d(180, 0, mMenu2);
        mAnimation2.setDuration(700);
        mAnimation2.setFillAfter(false);
        mAnimation2.setInterpolator(new AccelerateInterpolator());
        mListener2 = new RotateListener(mMenu2);
        mAnimation2.setAnimationListener(mListener2);
        
        mAnimation3 = new Rotate3d(180, 0, mMenu3);
        mAnimation3.setDuration(700);
        mAnimation3.setFillAfter(false);
        mAnimation3.setInterpolator(new AccelerateInterpolator());
        mListener3 = new RotateListener(mMenu3);
        mAnimation3.setAnimationListener(mListener3);

        // 只有设置背景后点击空白区Window才能消失（即点击无背景区Window消失）
        ColorDrawable cd = new ColorDrawable(Color.TRANSPARENT);
        setBackgroundDrawable(cd);
    }
    
    public void playAnimation() {
        mMenu0.setRotationX(180);
        mMenu1.setRotationX(180);
        mMenu2.setRotationX(180);
        mMenu3.setRotationX(180);
        
        mMenu0.startAnimation(mAnimation0);
        mMenu1.startAnimation(mAnimation1);
        mMenu2.startAnimation(mAnimation2);
        mMenu3.startAnimation(mAnimation3);
    }
    
    public class Rotate3d extends Animation {
        private final float mFromDegrees;
        private final float mToDegrees;
        private View mView;

        public Rotate3d(float fromDegrees, float toDegrees, View view) {
            mFromDegrees = fromDegrees;
            mToDegrees = toDegrees;
            mView = view;
        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                Transformation t) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees
                    + ((mToDegrees - fromDegrees) * interpolatedTime);
            mView.setRotationX(degrees);
        }
    }
    
    public class RotateListener implements AnimationListener {
        private View mView;
        public RotateListener(View view) {
            mView = view;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mView.setRotationX(0);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub
            
        }
    }
    
    private OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.menu00:
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_WALL_LONG_CLICK_DIALOG, null);
                break;
            case R.id.menu01:
                HomeUtils.gotoSettingsActivity();
                break;
            case R.id.menu02:
                Intent intentSetting = new Intent();
                intentSetting.setAction("android.settings.SETTINGS");
                intentSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                HomeManager.getInstance().startActivity(intentSetting);
                break;
            case R.id.menu03:
                Intent intentAppMgr = new Intent();
                intentAppMgr.setClassName("com.android.settings",
                        "com.android.settings.Settings$ManageApplicationsActivity");
                intentAppMgr.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                HomeManager.getInstance().startActivity(intentAppMgr);
                break;

            default:
                break;
            }
            
            OptionMenu.this.dismiss();
        }
    };

    private OnKeyListener mOnKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP
                    && keyCode == KeyEvent.KEYCODE_MENU) {
                if (OptionMenu.this.isShowing()) {
                    OptionMenu.this.dismiss();
                }
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (OptionMenu.this.isShowing()) {
                    OptionMenu.this.dismiss();
                }
            }
            return false;
        }
    };

}
