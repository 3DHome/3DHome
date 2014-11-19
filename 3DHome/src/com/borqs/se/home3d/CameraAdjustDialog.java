package com.borqs.se.home3d;

import java.text.NumberFormat;

import com.borqs.se.R;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEVector.SEVector3f;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraAdjustDialog extends Dialog implements OnClickListener, OnTouchListener {

    private static final int MSG_TYPE_ADD_RADIUS = 1;
    private static final int MSG_TYPE_REDUCE_RADIUS = 2;
    private static final int MSG_TYPE_ADD_DOWN_UP = 3;
    private static final int MSG_TYPE_REDUCE_DOWN_UP = 4;

    private Button mBtnReset;
    private TextView mCameraLocTextX;
    private TextView mCameraLocTextY;
    private TextView mCameraLocTextZ;
    private TextView mCameraFovText;

    private ImageView mBtnNear;
    private ImageView mBtnFar;
    private ImageView mBtnDown;
    private ImageView mBtnUp;

    private float mMinCameraRadius;
    private float mMaxCameraRadius;
    private float mMinCameraDownUp;
    private float mMaxCameraDownUp;
    private float mCurrentDownUp;
    private float mCurrentRadius;
    private SEScene mScene;
    private HomeSceneInfo mSceneInfo;
    private SECamera mCamera;
    private Handler mHandler;

    @Override
    public void show() {
        super.show();
        init();
    }

    public CameraAdjustDialog(Context context) {
        super(context, R.style.AdjustCameraDialog);
        setContentView(R.layout.camera_adjust_pane);
        getWindow().setWindowAnimations(R.style.adjust_camera_menu_anim_style);
        setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                if (mLastLocation != null) {
                    mSceneInfo.updateCameraDataToDB(mLastLocation, mLastFov);
                }

            }
        });
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int what = msg.what;
                switch (what) {
                case MSG_TYPE_ADD_RADIUS:
                    mCurrentRadius = mCurrentRadius + mStep;
                    mStep = mStep + 0.75f;
                    if (mStep > 10) {
                        mStep = 10;
                    }
                    if (mCurrentRadius >= mMaxCameraRadius) {
                        mCurrentRadius = mMaxCameraRadius;
                    } else {
                        sendEmptyMessageDelayed(MSG_TYPE_ADD_RADIUS, 100);
                    }
                    onProgressChanged(mCurrentDownUp, mCurrentRadius);
                    break;
                case MSG_TYPE_REDUCE_RADIUS:
                    mCurrentRadius = mCurrentRadius - mStep;
                    mStep = mStep + 0.75f;
                    if (mStep > 10) {
                        mStep = 10;
                    }
                    if (mCurrentRadius <= mMinCameraRadius) {
                        mCurrentRadius = mMinCameraRadius;
                    } else {
                        sendEmptyMessageDelayed(MSG_TYPE_REDUCE_RADIUS, 100);
                    }
                    onProgressChanged(mCurrentDownUp, mCurrentRadius);
                    break;
                case MSG_TYPE_ADD_DOWN_UP:
                    mCurrentDownUp = mCurrentDownUp + mStep;
                    mStep = mStep + 0.75f;
                    if (mStep > 10) {
                        mStep = 10;
                    }
                    if (mCurrentDownUp >= mMaxCameraDownUp) {
                        mCurrentDownUp = mMaxCameraDownUp;
                    } else {
                        sendEmptyMessageDelayed(MSG_TYPE_ADD_DOWN_UP, 100);
                    }
                    onProgressChanged(mCurrentDownUp, mCurrentRadius);
                    break;
                case MSG_TYPE_REDUCE_DOWN_UP:
                    mCurrentDownUp = mCurrentDownUp - mStep;
                    mStep = mStep + 0.75f;
                    if (mStep > 10) {
                        mStep = 10;
                    }
                    if (mCurrentDownUp <= mMinCameraDownUp) {
                        mCurrentDownUp = mMinCameraDownUp;
                    } else {
                        sendEmptyMessageDelayed(MSG_TYPE_REDUCE_DOWN_UP, 100);
                    }
                    onProgressChanged(mCurrentDownUp, mCurrentRadius);
                    break;
                }

            }

        };
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBtnReset = (Button) findViewById(R.id.btn_reset);
        mBtnReset.setOnClickListener(this);

        mBtnNear = (ImageView) findViewById(R.id.btn_expand);

        mBtnNear.setOnTouchListener(this);
        mBtnFar = (ImageView) findViewById(R.id.btn_narrow);
        mBtnFar.setOnTouchListener(this);
        mBtnDown = (ImageView) findViewById(R.id.btn_down);
        mBtnDown.setOnTouchListener(this);
        mBtnUp = (ImageView) findViewById(R.id.btn_up);
        mBtnUp.setOnTouchListener(this);
        mCameraLocTextX = (TextView) findViewById(R.id.camera_location_x_value);
        mCameraLocTextY = (TextView) findViewById(R.id.camera_location_y_value);
        mCameraLocTextZ = (TextView) findViewById(R.id.camera_location_z_value);
        mCameraFovText = (TextView) findViewById(R.id.camera_fov_value);
        init();
    }

    private void init() {
        mScene = HomeManager.getInstance().getHomeScene();
        mSceneInfo = HomeManager.getInstance().getHomeScene().getHomeSceneInfo();
        mCamera = HomeManager.getInstance().getHomeScene().getCamera();
        mMinCameraRadius = mSceneInfo.getCameraMinRadius();
        mMaxCameraRadius = mSceneInfo.getCameraMaxRadius();
        mMinCameraDownUp = mSceneInfo.getCameraMinDownUp();
        mMaxCameraDownUp = mSceneInfo.getCameraMaxDownUp();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        if (mCameraLocTextX != null) {
            mCameraLocTextX.setText("X=" + nf.format(mCamera.getLocation().getX()));
        }
        if (mCameraLocTextY != null) {
            mCameraLocTextY.setText("Y=" + nf.format(mCamera.getLocation().getY()));
        }

        if (mCameraLocTextZ != null) {
            mCameraLocTextZ.setText("Z=" + nf.format(mCamera.getLocation().getZ()));
        }
        if (mCameraFovText != null) {
            mCameraFovText.setText(nf.format(mCamera.getFov()) + "°");
        }
        mCurrentDownUp = mCamera.getLocation().getZ();
        mCurrentRadius = -mCamera.getLocation().getY();
    }

    @Override
    public void onClick(View v) {
        float curRadius = -mSceneInfo.getThemeInfo().mBestCameraLocation.getY();
        float curDownUp = mSceneInfo.getThemeInfo().mBestCameraLocation.getZ();
        mCurrentDownUp = curDownUp;
        mCurrentRadius = curRadius;
        onProgressChanged(curDownUp, curRadius);
    }

    private float mStep = 0.5f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mStep = 1;
            if (v == mBtnNear) {
                if (mCurrentRadius <= mMinCameraRadius) {
                    mCurrentRadius = mMinCameraRadius;
                } else {
                    mHandler.removeMessages(MSG_TYPE_REDUCE_RADIUS);
                    mHandler.sendEmptyMessage(MSG_TYPE_REDUCE_RADIUS);
                }
            } else if (v == mBtnFar) {
                if (mCurrentRadius >= mMaxCameraRadius) {
                    mCurrentRadius = mMaxCameraRadius;
                } else {
                    mHandler.removeMessages(MSG_TYPE_ADD_RADIUS);
                    mHandler.sendEmptyMessage(MSG_TYPE_ADD_RADIUS);
                }
            } else if (v == mBtnDown) {
                if (mCurrentDownUp <= mMinCameraDownUp) {
                    mCurrentDownUp = mMinCameraDownUp;
                } else {
                    mHandler.removeMessages(MSG_TYPE_REDUCE_DOWN_UP);
                    mHandler.sendEmptyMessage(MSG_TYPE_REDUCE_DOWN_UP);
                }
            } else if (v == mBtnUp) {
                if (mCurrentDownUp >= mMaxCameraDownUp) {
                    mCurrentDownUp = mMaxCameraDownUp;
                } else {
                    mHandler.removeMessages(MSG_TYPE_ADD_DOWN_UP);
                    mHandler.sendEmptyMessage(MSG_TYPE_ADD_DOWN_UP);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP
                || !v.isPressed()) {
            mStep = 1;
            mHandler.removeMessages(MSG_TYPE_REDUCE_RADIUS);
            mHandler.removeMessages(MSG_TYPE_ADD_RADIUS);
            mHandler.removeMessages(MSG_TYPE_REDUCE_DOWN_UP);
            mHandler.removeMessages(MSG_TYPE_ADD_DOWN_UP);
        }
        return false;
    }

    private SEVector3f mLastLocation;
    private float mLastFov;

    private void onProgressChanged(final float downUP, final float radius) {
        new SECommand(mScene) {
            public void run() {
                mCamera.getCurrentData().mLocation.set(0, -radius, downUP);
                mCamera.getCurrentData().mFov = mSceneInfo.getCameraFovByRadius(radius);
                mLastLocation = mCamera.getCurrentData().mLocation.clone();
                mLastFov = mCamera.getCurrentData().mFov;
                mCamera.setCamera();
                mHandler.post(new Runnable() {
                    public void run() {
                        NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMaximumFractionDigits(2);
                        if (mCameraLocTextX != null) {
                            mCameraLocTextX.setText("X=" + nf.format(mCamera.getLocation().getX()));
                        }
                        if (mCameraLocTextY != null) {
                            mCameraLocTextY.setText("Y=" + nf.format(mCamera.getLocation().getY()));
                        }

                        if (mCameraLocTextZ != null) {
                            mCameraLocTextZ.setText("Z=" + nf.format(mCamera.getLocation().getZ()));
                        }
                        if (mCameraFovText != null) {
                            mCameraFovText.setText(nf.format(mCamera.getFov()) + "°");
                        }
                    }
                });

            }
        }.execute();
    }

}
