package com.borqs.se.test;

import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObject.IMAGE_TYPE;
import com.borqs.se.engine.SEObject.OnTouchListener;
import com.borqs.se.engine.SERenderView;
import com.borqs.se.engine.SERotateAnimation;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SESceneManager.getInstance().initEngine(getApplicationContext());
        // SESceneManager.setDebug_JNI(true);
        SESceneManager.getInstance().setGLActivity(this);
        SESceneManager.getInstance().setHandler(new Handler());
        SERenderView renderView = new SERenderView(this, false);
        setContentView(renderView);
        SESceneManager.getInstance().setGLSurfaceView(renderView);
        SESceneManager.getInstance().setCurrentScene(new TestScene(getApplicationContext(), "TestScene"));
        SESceneManager.getInstance().dataIsReady();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SESceneManager.getInstance().handleBackKey();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            SESceneManager.getInstance().handleMenuKey();
        }
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return false;
        }
        return super.onKeyUp(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SESceneManager.getInstance().onActivityDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SESceneManager.getInstance().onActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SESceneManager.getInstance().onActivityResume();
    }

    private class TestScene extends SEScene {
        private SECameraData mSECameraData;

        public TestScene(Context context, String sceneName) {
            super(context, sceneName);
            mSECameraData = new SECameraData();
            mSECameraData.mLocation = new SEVector3f(0, -500, 0);
            mSECameraData.mAxisZ = new SEVector3f(0, -1, 0);
            mSECameraData.mUp = new SEVector3f(0, 0, 1);
            mSECameraData.mFov = 60;
            mSECameraData.mNear = 1;
            mSECameraData.mFar = 1000;
        }

        @Override
        public void onSceneStart() {
            // case 1
            SEObject root = new SEObject(this, "Root");
            root.setIsGroup(true);
            setContentObject(root);
            testMirror();
        }

        /**
         * 添加一个红色三角形的片： 该三角形片朝向为(0,-1,0)，也就是y轴的反方向，屏幕朝外； 该三角形片放在世界坐标(0,0,0)点；
         * 该三角形片的左下角为（-100, 0, -100）点; 该三角形片的右下角为（-100, 0, -100）点; 该三角形片的顶点为（0,
         * 0, 100）点;
         */
        private SEObject testAddColorObject(String name) {
            // 创建物体，物体的名字为“child“
            SEObject child = new SEObject(this, name);
            // 设置物体的顶点
            child.setVertexArray(new float[] { -100, 0, -100, 100, 0, -100, 0, 0, 100 });
            // 三角形顶点，表示由0，1，2三个顶点组成的三角形，三角形的朝向为右手螺旋拇指的朝向
            child.setFaceArray(new int[] { 0, 1, 2 });
            // 三角形的颜色
            child.setColor(new float[] { 1, 0, 0 });
            // 添加物体到根节点
            getContentObject().addChild(child, true);
            return child;
        }

        /**
         * 添加一个红色三角形的片： 该三角形片朝向为(0,-1,0)，也就是y轴的反方向，屏幕朝外； 该三角形片放在世界坐标(0,0,0)点；
         * 该三角形片的左下角为（-100, 0, -100）点; 该三角形片的右下角为（-100, 0, -100）点; 该三角形片的顶点为（0,
         * 0, 100）点;
         */
        private SEObject testAddColorRectObject(String name) {
            // 创建物体，物体的名字为“child“
            SEObject child = new SEObject(this, name);
            // 设置物体的顶点
            child.setVertexArray(new float[] { -100, 0, -100, 100, 0, -100, 100, 0, 100, -100, 0, 100 });
            // 三角形顶点，表示由0，1，2三个顶点组成的三角形，三角形的朝向为右手螺旋拇指的朝向
            child.setFaceArray(new int[] { 0, 1, 2, 2, 3, 0 });
            // 三角形的颜色
            child.setColor(new float[] { 1, 0, 0 });
            // 添加物体到根节点
            getContentObject().addChild(child, true);
            return child;
        }

        private SEObject testAddTextureObject(String name) {
            SEObject child = new SEObject(this, name);
            child.setVertexArray(new float[] { -100, 0, -100, 100, 0, -100, 0, 0, 100 });
            child.setFaceArray(new int[] { 0, 1, 2 });
            child.setTexVertexArray(new float[] { 0, 0, 1, 0, 0.5f, 1 });
            child.setImage(IMAGE_TYPE.BITMAP, name + "_image_name", name + "_image_key");
            Resources res = getContext().getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(res, com.borqs.se.engine.R.drawable.app_icon);
            child.setBitmap(bitmap);
            getContentObject().addChild(child, true);
            return child;
        }

        private void testMirror() {
            SEObject mirror = testAddColorObject("mirror");
            mirror.setAlpha(0.5f);
            mirror.setBlendingable(true);
            mirror.setNeedForeverBlend(true);
            mirror.setNeedRenderMirror(true);
            mirror.setUserRotate(new SERotate(-90, 1, 0, 0));
            mirror.setUserTranslate(new SEVector3f(0, 0, -200));
            SEObject object = testAddTextureObject("object");
            object.setBlendingable(true);
            object.setNeedForeverBlend(true);

            object.setNeedGenerateMirror(true);
            object.setUserScale(new SEVector3f(0.5f, 0.5f, 0.5f));
            object.setUserRotate(new SERotate(-90, 1, 0, 0));
            object.setUserTranslate(new SEVector3f(0, 0, -150));

            new SERotateAnimation(this, object, AXIS.Y, 0, 3600, 10000).execute();
        }

        private void testSetClickListenerToObject() {
            setTouchDelegate(getContentObject());
            SEObject obj = testAddColorRectObject("test");
            obj.setOnClickListener(new OnTouchListener() {
                public void run(SEObject obj) {

                }
            });
        }

        @Override
        public void handleMessage(int type, Object message) {
            // TODO Auto-generated method stub

        }

        @Override
        public SECameraData getCameraData() {
            return mSECameraData;
        }

    }

}
