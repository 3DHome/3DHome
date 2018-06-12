package com.borqs.se.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.MotionEvent;

import android.util.Log;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.GL10;

import com.borqs.se.engine.GLSurfaceView;
import com.borqs.se.engine.SESceneManager;

public class SERenderView extends GLSurfaceView {
    private Renderer mRender = new Renderer();
    private int mEGLContextClientVersion;
    private int mTryCount;

    public SERenderView(Context context, boolean translucent) {
        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(translucent ? new ComponentSizeChooser(5, 6, 5, 8, 16, 0, 0, 0) : new ComponentSizeChooser(
                5, 6, 5, 0, 16, 0, 0, 0));
        if (translucent)
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLContextFactory(new SEContextFactory());
        setEGLWindowSurfaceFactory(new SESurfaceFactory());
        setRenderer(mRender);
    }

    @Override
    public void setEGLContextClientVersion(int version) {
        mEGLContextClientVersion = version;
        super.setEGLContextClientVersion(version);
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }
            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);

        protected int[] mConfigSpec;

        private int[] filterConfigSpec(int[] configSpec) {
            if (mEGLContextClientVersion != 2) {
                return configSpec;
            }
            /*
             * We know none of the subclasses define EGL_RENDERABLE_TYPE. And we
             * know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len + 1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes, and at
     * least the specified depth and stencil sizes.
     */
    private class ComponentSizeChooser extends BaseConfigChooser {
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize,
                int samples, int samplesperpixel, int stencilSize) {
            super(new int[] { EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
                    blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_SAMPLE_BUFFERS, samples, EGL10.EGL_STENCIL_SIZE, stencilSize, EGL10.EGL_NONE });
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mSamples = samples;
            mSamplesPerPixel = samplesperpixel;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            ArrayList<EGLConfig> result = new ArrayList<EGLConfig>();
            for (int i = 0; i < configs.length; i++) {
                // if there are multi-sample support
                EGLConfig config = configs[i];
                int thereis = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLE_BUFFERS, 0);
                int perpixelsamples = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0);
                if (thereis > 0 && perpixelsamples > 0) {
                    result.add(config);
                }
            }

            if (result.size() > 0) {
                ArrayList<EGLConfig> best = new ArrayList<EGLConfig>();
                // found support multi-sample
                for (int i = 0; i < result.size(); i++) {
                    EGLConfig config = result.get(i);

                    int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                    int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                    int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

                    if (d >= mDepthSize && s >= mStencilSize && r >= mRedSize && g >= mGreenSize && b >= mBlueSize
                            && a >= mAlphaSize) {
                        best.add(config);
                        // now just return first support config, not best
                        return config;
                    }
                }

            }

            for (int i = 0; i < configs.length; i++) {
                EGLConfig config = configs[i];

                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);

                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == mRedSize) && (g == mGreenSize) && (b == mBlueSize) && (a == mAlphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mSamples;
        protected int mSamplesPerPixel;
        protected int mStencilSize;
    }

    private class SEContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            mTryCount = 0;
            // try to create egl context 5 times
            while ((context == null || context == EGL10.EGL_NO_CONTEXT) && mTryCount < 5) {
                mTryCount++;
                try {
                    Thread.sleep(10 * mTryCount * mTryCount);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            }
            if (context == null || context == EGL10.EGL_NO_CONTEXT) {
                android.os.Process.killProcess(android.os.Process.myPid());

            }
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private class SESurfaceFactory implements EGLWindowSurfaceFactory {

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            mTryCount = 0;
            while (result == null && mTryCount < 5) {
                mTryCount++;
                try {
                    result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
                } catch (IllegalArgumentException e) {
                    // This exception indicates that the surface flinger surface
                    // is not valid. This can happen if the surface flinger
                    // surface
                    // has
                    // been torn down, but the application has not yet been
                    // notified via SurfaceHolder.Callback.surfaceDestroyed.
                    // In theory the application should be notified first,
                    // but in practice sometimes it is not. See b/4588890
                    Log.e("3DHome:", "eglCreateWindowSurface", e);
                }
                if (result == null) {
                    try {
                        Thread.sleep(10 * mTryCount * mTryCount);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            if (result == null) {
                SESceneManager.getInstance().runInUIThread(new Runnable() {
                    public void run() {
                        SESceneManager.getInstance().finish();
                    }
                });
            }
            return result;
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            SESceneManager.getInstance().destroy_JNI();
            egl.eglDestroySurface(display, surface);
        }

    }

    private class Renderer implements GLSurfaceView.Renderer {
        public GL10 mGL;

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mGL = gl;
            SESceneManager.getInstance().initRenderCapabilities_JNI();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            SESceneManager.getInstance().notifySurfaceChanged(width, height);
        }

        public void onDrawFrame(GL10 gl) {
            if (SESceneManager.getInstance().update()) {
                requestRender();
            }

            SESceneManager.getInstance().runOneFrame_JNI();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // SEUIManager.getInstance().processTouchEvent();
        return SESceneManager.getInstance().dispatchTouchEvent(ev);
    }

    public Bitmap getBitmap(int width, int height) {
        int screenshotSize = width * height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        mRender.mGL.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        bb = null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0, 0, width, height);
        pixelsBuffer = null;

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        // Making created bitmap (from OpenGL points) compatible with
        // Android bitmap
        for (int i = 0; i < screenshotSize; ++i) {
            short v = sBuffer[i];
            sBuffer[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        return bitmap;
    }
}
