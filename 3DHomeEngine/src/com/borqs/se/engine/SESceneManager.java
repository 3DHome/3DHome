package com.borqs.se.engine;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class SESceneManager {
    private native void instance_JNI(int userid0, int userid1);

    public native void addAssetPath_JNI(String assetPath);

    public native void rmAssetPath_JNI(String assetPath);

    public native void loadPlugins_JNI(String filePath);

    public native void runOneFrame_JNI();

    public native void releaseResource_JNI();

    public native void destroy_JNI();

    public native void initRenderCapabilities_JNI();

    public native void createCBF_JNI(String datapath, String fileNameId);

    public boolean javaCallback(int msgType, String msgName) {
        if (msgType == 10) {
            if (mTextureImages != null) {
                String imageKey = msgName;
                Bitmap bitmap = mTextureImages.get(imageKey);
                if (bitmap != null) {
                    loadTextureImage_JNI(msgName, bitmap);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public native static void setMainScene_JNI(String sceneName);

    public native static void glReadPixels_JNI(String imageKey, int x, int y, int width, int height);

    public native static void savePixelsData_JNI(String savePath, String imageKey);

    public native static boolean loadPixelsData_JNI(String savePath, String imageKey, int width, int height);

    public native static void loadTextureImage_JNI(String imageKey, Bitmap bitmap);

    public native static void changeTextureImage_JNI(String imageKey);

    public native static void setDebug_JNI(boolean debug);

    private static final int DEFAULTSTATUS = 0x1;
    private static final int ENABLELIGHT = 0x1 << 1;
    private static final int ENABLEMIRROR = 0x1 << 2;
    private static final int ENABLEWALLSHADOW = 0x1 << 3;
    private static final int ENABLEGROUNDSHADOW = 0x1 << 4;

    private native static void setSceneManagerStatus_JNI(int value, boolean debug);

    public void enableLight(boolean enable) {
        setSceneManagerStatus_JNI(ENABLELIGHT, enable);
    }

    public void enableMirror(boolean enable) {
        setSceneManagerStatus_JNI(ENABLEMIRROR, enable);
    }

    public void enableWallShadow(boolean enable) {
        setSceneManagerStatus_JNI(ENABLEWALLSHADOW, enable);
    }

    public void enableGroundShadow(boolean enable) {
        setSceneManagerStatus_JNI(ENABLEGROUNDSHADOW, enable);
    }

    private float mAnimationSpeed = 1;
    public static final boolean USING_TIME_ANIMATION = false;
    public static final long TIME_ONE_FRAME = 20;
    private Activity mGLActivity;
    private Context mContext;
    private SEScene mCurrentScene;
    private Handler mHandler;
    private boolean mShouldWait = true;
    private TextView mFPSView;
    private Map<String, Bitmap> mTextureImages;
    private static SESceneManager mSESceneManager;
    private GLSurfaceView mGLSurfaceView;
    private int mTouchSlop;
    private boolean mHasBeenInited = false;
    private boolean mHasLeaveScene = true;

    private SESceneManager() {

    }

    public static SESceneManager getInstance() {
        if (mSESceneManager == null) {
            mSESceneManager = new SESceneManager();
        }
        return mSESceneManager;
    }

    /*
     * 需要在Application的OnCreate()方法中执行
     */
    public void initEngine(Context context) {
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        if (!mHasBeenInited) {
            System.loadLibrary("sefactory");
            System.loadLibrary("se");
            instance_JNI(137, 18215879);
//            loadPlugins_JNI("/data/data/" + context.getPackageName() + "/lib");
            addAssetPath_JNI(context.getApplicationInfo().sourceDir);
            addAssetPath_JNI("/");
            mHasBeenInited = true;
        }

    }

    public Context getContext() {
        return mContext;
    }

    public Activity getGLActivity() {
        return mGLActivity;
    }

    public void setGLActivity(Activity activity) {
        mGLActivity = activity;

    }

    public void finish() {
        if (mGLActivity != null && !mGLActivity.isFinishing()) {
            mGLActivity.finish();
        }
    }

    public void setGLSurfaceView(GLSurfaceView surfaceView) {
        mGLSurfaceView = surfaceView;
    }

    public GLSurfaceView getGLSurfaceView() {
        return mGLSurfaceView;
    }

    public void requestRender() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.requestRender();
        }
    }

    public float getAnimationSpeed() {
        return mAnimationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        mAnimationSpeed = animationSpeed;
    }

    public void addTextureImage(String key, Bitmap textureImage) {
        if (mTextureImages == null) {
            mTextureImages = new HashMap<String, Bitmap>();
        }
        mTextureImages.put(key, textureImage);
    }

    public void changeTextureImage(String key) {
        changeTextureImage_JNI(key);
    }

    public Bitmap getTextureImage(String key) {
        if (mTextureImages != null) {
            return mTextureImages.get(key);
        }
        return null;
    }

    public Bitmap removeTextureImage(String key) {
        if (mTextureImages != null) {
            return mTextureImages.remove(key);
        }
        return null;
    }

    public int getRequestedOrientation() {
        if (mGLActivity != null) {
            return mGLActivity.getRequestedOrientation();
        }
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    public SEScene getCurrentScene() {
        return mCurrentScene;
    }

    public void setCurrentScene(SEScene scene) {
        mCurrentScene = scene;
    }

    public void notifySurfaceChanged(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if ((width <= 0 || height <= 0)
                || (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && height > width)
                || (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && width > height)) {
            return;
        }
        final int minSize = Math.min(width, height);
        if (minSize < 480) {
            SESceneManager.getInstance().setAnimationSpeed(0.3f);
            SESceneManager.getInstance().enableMirror(false);
            SESceneManager.getInstance().enableWallShadow(false);
            SESceneManager.getInstance().enableGroundShadow(false);
        } else if (minSize < 720) {
            SESceneManager.getInstance().setAnimationSpeed(0.5f);
            SESceneManager.getInstance().enableMirror(true);
            SESceneManager.getInstance().enableWallShadow(false);
            SESceneManager.getInstance().enableGroundShadow(false);
        } else if (minSize < 800) {
            SESceneManager.getInstance().setAnimationSpeed(0.6f);
            SESceneManager.getInstance().enableMirror(true);
            SESceneManager.getInstance().enableWallShadow(false);
            SESceneManager.getInstance().enableGroundShadow(true);
        } else {
            SESceneManager.getInstance().setAnimationSpeed(0.68f);
            SESceneManager.getInstance().enableMirror(true);
            SESceneManager.getInstance().enableWallShadow(false);
            SESceneManager.getInstance().enableGroundShadow(true);
        }

        if (mCurrentScene == null || !mCurrentScene.hasBeenStarted()) {
            start3DScene(width, height);
        } else {
            mCurrentScene.notifySurfaceChanged(width, height);
        }
    }

    public void releaseCurrentScene() {
        if (mCurrentScene != null) {
            mCurrentScene.release();
        }
    }

    public synchronized void dataIsReady() {
        mShouldWait = false;
        this.notifyAll();
    }

    public synchronized void start3DScene(int sceneWidth, int sceneHeight) {
        while (mShouldWait) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mCurrentScene != null) {
            mCurrentScene.startScene(sceneWidth, sceneHeight);
        }
    }

    public void clearFPSView() {
        mFPSView = null;
    }

    public void setFPSView(TextView view) {
        mFPSView = view;
    }

    private long mPreTime = 0;
    int count = 0;

    public boolean update() {
        if (mFPSView != null) {
            long curTime = System.currentTimeMillis();
            count++;
            if (curTime - mPreTime >= 1000) {
                final float fps = count * 1000.0f / (curTime - mPreTime);
                runInUIThread(new Runnable() {
                    public void run() {
                        mFPSView.setText(fps + " fps");
                    }
                });
                mPreTime = curTime;
                count = 0;
            }
        }
        if (mCurrentScene != null) {
            return mCurrentScene.update();
        }
        return false;
    }

    public void handleMessage(final int msgType, final Object message) {
        if (mCurrentScene != null) {
            final SEScene scene = mCurrentScene;
            new SECommand(scene, msgType) {
                @Override
                public void run() {
                    scene.handleMessage(msgType, message);
                }
            }.execute();
        }
    }

    public void removeMessage(final int msgType) {
        if (mCurrentScene != null) {
            mCurrentScene.removeMessage(msgType);
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void runInUIThread(Runnable runnable, long delayMillis) {
        if (mHandler == null) {
            return;
        }
        if (delayMillis <= 0) {
            mHandler.post(runnable);
        } else {
            mHandler.postDelayed(runnable, delayMillis);
        }
    }

    public void runInUIThread(Runnable runnable) {
        runInUIThread(runnable, 0);
    }

    public void onActivityDestroy() {
        if (mCurrentScene != null) {
            mCurrentScene.onActivityDestory();
        }
    }

    public void setNeedDestoryHardware(boolean need) {
        mHasLeaveScene = need;
    }

    public void onActivityPause() {
        if (mCurrentScene != null) {
            mCurrentScene.onActivityPause();
        }
        if (mHasLeaveScene) {
            if (mGLSurfaceView != null) {
                mGLSurfaceView.onPause();
            }
        }
    }

    public void onActivityResume() {
        if (mCurrentScene != null) {
            mCurrentScene.onActivityResume();
        }
        if (mHasLeaveScene) {
            if (mGLSurfaceView != null) {
                mGLSurfaceView.onResume();

            }
        }
    }

    public void onActivityRestart() {
        if (mCurrentScene != null) {
            mCurrentScene.onActivityRestart();
        }
    }

    private int mPreX;
    private int mPreY;

    private boolean mMoveStarted = false;

    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (mCurrentScene != null) {
            final MotionEvent event = MotionEvent.obtain(ev);
            switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
                mPreX = x;
                mPreY = y;
                new SECommand(mCurrentScene) {
                    public void run() {
                        mCurrentScene.dispatchTouchEvent(event);
                    }
                }.execute();
                mMoveStarted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int slop = mTouchSlop;
                boolean shouldMove = false;
                if (!mMoveStarted) {
                    shouldMove = Math.pow((x - mPreX), 2) + Math.pow((y - mPreY), 2) > Math.pow(slop, 2);
                } else {
                    shouldMove = true;
                }
                if (shouldMove) {
                    new SECommand(mCurrentScene) {
                        public void run() {
                            mCurrentScene.dispatchTouchEvent(event);
                        }
                    }.execute();
                    mPreX = x;
                    mPreY = y;
                    mMoveStarted = true;
                }
                break;
            default:
                mMoveStarted = false;
                break;
            }
        }
        return true;
    }

    public void handleMenuKey() {
        if (mCurrentScene != null) {
            new SECommand(mCurrentScene) {
                public void run() {
                    mCurrentScene.handleMenuKey();
                }
            }.execute();
        }
    }

    public void handleBackKey() {
        if (mCurrentScene != null) {
            new SECommand(mCurrentScene) {
                public void run() {
                    mCurrentScene.handleBackKey(null);
                }
            }.execute();
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mCurrentScene != null) {
            mCurrentScene.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onNewIntent(final Intent intent) {
        if (mCurrentScene != null) {
            new SECommand(mCurrentScene) {
                public void run() {
                    mCurrentScene.onNewIntent(intent);
                }
            }.execute();
        }
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        if (mCurrentScene != null) {
            return mCurrentScene.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCurrentScene != null) {
            return mCurrentScene.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentScene != null) {
            return mCurrentScene.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mCurrentScene != null) {
            new SECommand(mCurrentScene) {
                public void run() {
                    mCurrentScene.onOptionsItemSelected(item);
                }
            }.execute();
        }
        return true;
    }

    public Dialog onCreateDialog(int id, Bundle bundle) {
        if (mCurrentScene != null) {
            return mCurrentScene.onCreateDialog(id, bundle);
        }
        return null;
    }

    public void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        if (mCurrentScene != null) {
            mCurrentScene.onPrepareDialog(id, dialog, bundle);
        }
    }

    public SECamera getCurrentScreenCamera() {
        if (mCurrentScene != null) {
            return mCurrentScene.getCamera();
        }
        return null;
    }

}
