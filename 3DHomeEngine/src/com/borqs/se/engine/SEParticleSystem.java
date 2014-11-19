package com.borqs.se.engine;

import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;


public class SEParticleSystem {
    /** Types of emitter */
    public final static String EMITTER_TYPE_BOX = "Box";
//    public final static String EMITTER_TYPE_POINT = "Point";
//    public final static String EMITTER_TYPE_CYLINDER = "Cylinder";
//    public final static String EMITTER_TYPE_ELLIPSOID = "Ellipsoid";
//    public final static String EMITTER_TYPE_HOLLOWELLIPSOID = "HollowEllipsoid";
//    public final static String EMITTER_TYPE_RING = "Ring";
    
    private boolean mColourInterpolatorAffectorEnable;
    private boolean mColourFaderAffectorEnable;
    private boolean mLinerforceAffectorEnable;
    
    private String mParticleSystemName;
    private float[] mParticleDimentions;
    private int mParticleQuota;
    private String mImagePath;
    
    private boolean mNeedDepthTest;
    private int mLayerIndex;
    
    private String mEmitterType;
    private float[] mBoxEmitterSize;
    private float mEmitterAngle;
    private float[] mEmitterPosition;
    private SEVector3f mEmitterDirection;
    private float mEmitterMinSpeed;
    private float mEmitterMaxSpeed;
    private float[] mEmitterColorValue;
    private float mEmitterEmissionRate;
    private float mEmitterMinTimeToLive;
    private float mEmitterMaxTimeToLive;
    
    private SEVector3f mForceVector;
    
    private float[] mColorFaderAdjust;
    private float[] mTranslate;
    private float[] mRotate;
    private float[] mScale;
    private ColourInterpolatorAffector mColorInterpolatorAffector;
    
    private String mAttachObjName;
    private int mAttachObjIndex;

    public native void addParticle_JNI();

    public native void deleteParticleObject_JNI();
    
    public SEParticleSystem(String name) {
        mParticleSystemName = name;
        
        mParticleDimentions = new float[] {0, 0};
        mParticleQuota = 0;
        mImagePath = null;
        
        mNeedDepthTest = false;
        mLayerIndex = 0;
        
        mEmitterType = EMITTER_TYPE_BOX;
        mBoxEmitterSize = new float[] {0, 0, 0};
        mEmitterAngle = 0;
        mEmitterPosition = new float[] {0, 0, 0};
        mEmitterDirection = new SEVector3f(0, 0, 0);
        mEmitterMinSpeed = 0;
        mEmitterMaxSpeed = 0;
        mTranslate = new float[] { 0, 0, 0 };
        mRotate = new float[] { 0, 1, 0, 0 };
        mScale = new float[] { 1, 1, 1 };
        mEmitterColorValue = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        mEmitterEmissionRate = 0;
        mEmitterMinTimeToLive = 0;
        mEmitterMaxTimeToLive = 0;
        
        mColourInterpolatorAffectorEnable = false;
        mColourFaderAffectorEnable = false;
        mLinerforceAffectorEnable = false;
        
        mForceVector = new SEVector3f();
        mColorFaderAdjust = new float[] {-0.0f, -0.0f, -0.0f, -0.0f};
        mColorInterpolatorAffector = new ColourInterpolatorAffector();
    }
    
    /**
     * Set default value to attributes of particle system
     * @param defaultWidth
     * @param defaultHeight
     * @param defaultDimentions, the size of particle
     * @param particleQuota, the count of particles
     * @param emittedEmitterQuota
     */
    private void setParticleSystemAttribute(float defaultWidth, float defaultHeight, float[] defaultDimentions, int particleQuota, float emittedEmitterQuota) {
        mParticleDimentions = defaultDimentions;
        mParticleQuota = particleQuota;
    }
    
    /**
     * Set Set default value to attributes of particle system
     * @param defaultDimentions, the size of particle
     * @param particleQuota, the count of particles
     */
    public void setParticleSystemAttribute(float[] defaultDimentions, int particleQuota) {
        setParticleSystemAttribute(0, 0, defaultDimentions, particleQuota, 0);
    }
    
    public String getParticleSystemName() {
        return mParticleSystemName;
    }
    
    public String getImagePath() {
        return mImagePath;
    }
    
    public float[] getParticleDimentions() {
        return mParticleDimentions;
    }
    
    public int getParticleQuota() {
        return mParticleQuota;
    }
    
    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }
    
    public void setNeedDepthTest(boolean need) {
        mNeedDepthTest = need;
    }
    
    public boolean isNeedDepthTest() {
        return mNeedDepthTest;
    }
    
    public void setLayerIndex(int layerIndex) {
        mLayerIndex = layerIndex;
    }
    
    public int getLayerIndex() {
        return mLayerIndex;
    }
    
//    public void setEmitterType(String type) {
//        mEmitterType = type;
//    }
    
    public String getEmitterType() {
        return mEmitterType;
    }
    
    /**
     * If emitter type is "Box", should set the size of box emitter.
     * @param width
     * @param height
     * @param depth
     */
    public void setBoxEmitterSize(float width, float height, float depth) {
        mBoxEmitterSize[0] = width;
        mBoxEmitterSize[1] = height;
        mBoxEmitterSize[2] = depth;
    }
    
    public float[] getBoxEmitterSize() {
        return mBoxEmitterSize;
    }
    
    /**
     * Set the angle of emitter.
     * @param angle
     */
    public void setEmitterAngle(float angle) {
        mEmitterAngle = angle;
    }
    
    public float getEmitterAngle() {
        return mEmitterAngle;
    }
    
    /**
     * Set the direction of emitter.
     * @param direction
     */
    public void setEmitterDirection(SEVector3f direction) {
        mEmitterDirection = direction;
    }
    
    public float[] getEmitterDirection() {
        return mEmitterDirection.mD;
    }
    
    /**
     * Set the position of emitter.
     * @param direction
     */
    public void setEmitterPosition(float x, float y, float z) {
        mEmitterPosition[0] = x;
        mEmitterPosition[1] = y;
        mEmitterPosition[2] = z;
    }
    
    /**
     * Set the speed of the emitter.
     * @param minSpeed
     * @param maxSpeed
     */
    public void setEmitterParticleVelocity(float minSpeed, float maxSpeed) {
        mEmitterMinSpeed = minSpeed;
        mEmitterMaxSpeed = maxSpeed;
    }
    
    public float[] getEmitterParticleVelocity() {
        return new float[] {mEmitterMinSpeed, mEmitterMaxSpeed};
    }
    
    /**
     * Set the color of the emitter.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public void setEmitterColorValue(float red, float green, float blue, float alpha) {
        mEmitterColorValue[0] = red;
        mEmitterColorValue[1] = green;
        mEmitterColorValue[2] = blue;
        mEmitterColorValue[3] = alpha;
    }
    
    /**
     * Set the emission rate of the emitter, the count of particles per second.
     * @param particlesPerSecond
     */
    public void setEmitterEmissionRate(float particlesPerSecond) {
        mEmitterEmissionRate = particlesPerSecond;
    }
    
    public float getEmitterEmissionRate() {
        return mEmitterEmissionRate;
    }
    
    /**
     * Set the min and max time that the emitter to live.
     * @param minTtl
     * @param maxTtl
     */
    public void setEmitterTimeToLive(float minTtl, float maxTtl) {
        mEmitterMinTimeToLive = minTtl;
        mEmitterMaxTimeToLive = maxTtl;
    }
    
    public float[] getEmitterTimeToLive() {
        return new float[] {mEmitterMinTimeToLive, mEmitterMaxTimeToLive};
    }
    
    /**
     * Enable / Disable color interpolator affector.
     * @param enable
     */
    public void setColourInterpolatorAffectorEnable(boolean enable) {
        mColourInterpolatorAffectorEnable = enable;
    }
    
    public boolean isColorInterpolatorAffectorEnabled() {
        return mColourInterpolatorAffectorEnable;
    }
    
    public void setColourFaderAffectorEnable(boolean enable) {
        mColourFaderAffectorEnable = enable;
    }
    
    public boolean isColourFaderAffectorEnabled() {
        return mColourFaderAffectorEnable;
    }
    
    public void setLinearForceAffectorEnable(boolean enable) {
        mLinerforceAffectorEnable = enable;
    }
    
    public boolean isLinearForceAffectorEnabled() {
        return mLinerforceAffectorEnable;
    }
    
    /**
     * Call this method after enable linear force affector.
     * @param x
     * @param y
     * @param z
     */
    public void setForceVector(float x, float y, float z) {
        mForceVector.mD[0] = x;
        mForceVector.mD[1] = y;
        mForceVector.mD[2] = z;
    }
    
    public float[] getFoceVector() {
        return mForceVector.mD;
    }
    
    /**
     * Call this method after enable colour fader affector.
     * @param redAdjust
     * @param greedAdjust
     * @param blueAdjust
     * @param alphaAdjuest
     */
    public void setColorFaderAdjust(float redAdjust, float greedAdjust, float blueAdjust, float alphaAdjuest) {
        mColorFaderAdjust[0] = redAdjust;
        mColorFaderAdjust[1] = greedAdjust;
        mColorFaderAdjust[2] = blueAdjust;
        mColorFaderAdjust[3] = alphaAdjuest;
    }
    
    public float[] getColorFaderAdjust() {
        return mColorFaderAdjust;
    }
    
    /**
     * Call this method after enable colour interpolator affector.
     * As long as the setting time adjust, must set up corresponding colour adjust.
     * @param index, must be same of the index of colour adjust. The max value is 5 (0, 1, 2, 3, 4, 5).
     * @param time
     */
    public void setTimeAdjust(int index, float time) {
        if (mColorInterpolatorAffector == null) {
            mColorInterpolatorAffector = new ColourInterpolatorAffector();
        }
        mColorInterpolatorAffector.setTimeAdjust(index, time);
    }
    
    /**
     * Call this method after enable colour interpolator affector. Must have corresponding time adjust.
     * @param index, must be same of the index of time adjust. The max value is 5 (0, 1, 2, 3, 4, 5).
     * @param colorValue
     */
    public void setColorAdjust(int index, ColorValue colorValue) {
        if (mColorInterpolatorAffector == null) {
            mColorInterpolatorAffector = new ColourInterpolatorAffector();
        }
        mColorInterpolatorAffector.setColorAdjust(index, colorValue);
    }
    
    /**
     * Get the list of time adjust.
     * @return
     */
    public float[] getTimeAdjust() {
        if (mColorInterpolatorAffector != null) {
            return mColorInterpolatorAffector.mTimeAdj;
        }
        return null;
    }
    
    /**
     * Get the list of colour adjust.
     * @return
     */
    public ColorValue[] getColorAdjust() {
        if (mColorInterpolatorAffector != null) {
            return mColorInterpolatorAffector.mColorAdj;
        }
        return null;
    }

    public void setEmitterTranslate(SEVector3f translate) {
        mTranslate = translate.mD;
    }

    public void setEmitterRotate(SERotate rotate) {
        mRotate = rotate.mD;
    }

    public void setEmitterScale(SEVector3f scale) {
        mScale = scale.mD;
    }

    
    public void attachObject(SEObject obj) {
        mAttachObjName = obj.mName;
        mAttachObjIndex = obj.mIndex;
    }

    /**
     * The colour interpolator affector of particle system.
     */
    public class ColourInterpolatorAffector {
        private final static int MAX = 6;
        float[] mTimeAdj;
        ColorValue[] mColorAdj;
        
        public ColourInterpolatorAffector() {
            mTimeAdj = new float[MAX];
            mColorAdj = new ColorValue[MAX];
            for(int i = 0; i < MAX; i ++) {
                mTimeAdj[i] = 1.0f;
                mColorAdj[i] = new ColorValue(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        
        public void setTimeAdjust(int index, float time) {
            if (index > MAX - 1) {
                return;
            }
            mTimeAdj[index] = time;
        }
        
        public void setColorAdjust(int index, ColorValue colorValue) {
            if (index > MAX - 1) {
                return;
            }
            mColorAdj[index] = colorValue;
        }
    }
    
    public class ColorValue {
        float mRed;
        float mGreen;
        float mBlue;
        float mAlpha;
        
        public ColorValue(float r, float g, float b, float a) {
            mRed = r;
            mGreen = g;
            mBlue = b;
            mAlpha = a;
        }
        
        public float[] getColorValue() {
            return new float[] {mRed, mGreen, mBlue, mAlpha};
        }
    }
}
