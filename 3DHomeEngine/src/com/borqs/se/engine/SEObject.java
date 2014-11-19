package com.borqs.se.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.ViewConfiguration;

import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;

public class SEObject {

    /**** SpatialEffect begin ****/
    private final static int BLENDABLE = 0x1;
    private final static int DEPTHENABLE = 0x1 << 1;
    private final static int FOREVERBLEND = 0x1 << 2;
    private final static int NEEDSPECULAR = 0x1 << 3;
    private final static int SHADOWOBJECT = 0x1 << 4;
    private final static int MIRRORGENERATOR = 0x1 << 5;
    private final static int MIRRORRENDER = 0x1 << 6;
    private final static int SHADOWGENERATOR = 0x1 << 7;
    private final static int SHADOWRENDER = 0x1 << 8;
    private final static int UVANIMATION = 0x1 << 9;
    private final static int LIGHTING = 0x1 << 10;
    private final static int ALPHATEST = 0x1 << 11;
    private final static int MINIBOX = 0x1 << 12;
    private final static int PARTICLE = 0x1 << 13;
    private final static int CULLFACE = 0x1 << 14;
    private final static int RENDERTOFBO = 0x1 << 15;
    private final static int FLAGWAVE = 0x1 << 16;
    private final static int DRAWLINE = 0x1 << 17;
    private final static int BLACKWHITE = 0x1 << 18;
    private final static int BLENDX = 0x1 << 19;
    private final static int BLENDY = 0x1 << 20;
    private final static int BLENDZ = 0x1 << 21;
    private final static int CLOAKFLAGWAVE = 0x1 << 22;
    private final static int NEEDFOG = 0x1 << 23;
    private final static int ISWALL = 0x1 << 24;
    private final static int ISGROUND = 0x1 << 25;
    private int mSpatialEffect = 0;
    /**** SpatialEffect end ****/

    /**** SpatialState begin ****/
    private final static int VISIBLE = 0x1;
    private final static int MOVABLE = 0x1 << 1;
    private final static int COLLISIONABLE = 0x1 << 2;
    private final static int TOUCHABLE = 0x1 << 3;
    private int mSpatialState = VISIBLE + MOVABLE + COLLISIONABLE + TOUCHABLE;
    /**** SpatialState end ****/

    /**** SpatialRuntime begin ****/
    private final static int IGNORVERTEXBUFFER = 0x1;
    private final static int SELECTED = 0x1 << 1;
    private final static int CAMERAFULLCULL = 0x1 << 2;
    private final static int NEEDDRAWLINE = 0x1 << 3;
    private final static int BELONGTONONE = 0x1 << 4;
    private final static int BELONGTOWALL = 0x1 << 5;
    private final static int BELONGTOGROUND = 0x1 << 6;
    private int mSpatialRuntime = BELONGTONONE;

    /**** SpatialRuntime end ****/

    public native void setSpatialEffectAttribute_JNI(int value, boolean enableOrDisable);

    public native void setSpatialStateAttribute_JNI(int value, boolean enableOrDisable);

    public native void setSpatialRuntimeAttribute_JNI(int value, boolean enableOrDisable);

    // shadow color should be from 0.001(black) to 0.1(white)
    public native void setShadowColorDesity_JNI(float value);

    // 镜像的清晰度0～1，值越大越清晰
    public native void setMirrorColorDesity_JNI(float value);

    private native void setVisible_JNI(boolean visible);

    /**
     * @param visible
     *            设置物体是否可见
     */
    public void setVisible(boolean visible) {
        if (visible) {
            mSpatialState |= VISIBLE;
        } else {
            mSpatialState &= ~VISIBLE;
        }
        if (mHasBeenAddedToEngine) {
            setVisible_JNI(visible);
        }
    }

    public boolean isVisible() {
        return (mSpatialState & VISIBLE) != 0;
    }

    /**
     * 设置物体是否需要产生镜像
     */
    public void setNeedGenerateMirror(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(MIRRORGENERATOR, open);
        }
        if (open) {
            mSpatialEffect |= MIRRORGENERATOR;
        } else {
            mSpatialEffect &= ~MIRRORGENERATOR;
        }
    }

    /**
     * 设置物体为镜子
     */
    public void setNeedRenderMirror(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(MIRRORRENDER, open);
        }
        if (open) {
            mSpatialEffect |= MIRRORRENDER;
        } else {
            mSpatialEffect &= ~MIRRORRENDER;
        }
    }

    /**
     * 设置物体需要产生影子
     */
    private void setNeedGenerateShadow(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(SHADOWGENERATOR, open);
        }
        if (open) {
            mSpatialEffect |= SHADOWGENERATOR;
        } else {
            mSpatialEffect &= ~SHADOWGENERATOR;
        }
    }

    public void setNeedGenerateShadowAtGround(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialRuntimeAttribute_JNI(BELONGTOGROUND, open);
            if (open) {
                setSpatialRuntimeAttribute_JNI(BELONGTOWALL, false);
                setSpatialRuntimeAttribute_JNI(BELONGTONONE, false);
            } else {
                setSpatialRuntimeAttribute_JNI(BELONGTOWALL, false);
                setSpatialRuntimeAttribute_JNI(BELONGTONONE, true);
            }
        }
        if (open) {
            mSpatialRuntime |= BELONGTOGROUND;
            mSpatialRuntime &= ~BELONGTOWALL;
            mSpatialRuntime &= ~BELONGTONONE;
        } else {
            mSpatialRuntime &= ~BELONGTOGROUND;
            mSpatialRuntime &= ~BELONGTOWALL;
            mSpatialRuntime |= BELONGTONONE;
        }
        setNeedGenerateShadow(open);
    }

    public void setNeedGenerateShadowAtWall(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialRuntimeAttribute_JNI(BELONGTOWALL, open);
            if (open) {
                setSpatialRuntimeAttribute_JNI(BELONGTOGROUND, false);
                setSpatialRuntimeAttribute_JNI(BELONGTONONE, false);
            } else {
                setSpatialRuntimeAttribute_JNI(BELONGTOGROUND, false);
                setSpatialRuntimeAttribute_JNI(BELONGTONONE, true);
            }
        }
        if (open) {
            mSpatialRuntime |= BELONGTOWALL;
            mSpatialRuntime &= ~BELONGTOGROUND;
            mSpatialRuntime &= ~BELONGTONONE;
        } else {
            mSpatialRuntime &= ~BELONGTOWALL;
            mSpatialRuntime &= ~BELONGTOGROUND;
            mSpatialRuntime |= BELONGTONONE;
        }
        setNeedGenerateShadow(open);
    }

    /**
     * 设置在该物体上产生影子
     */
    public void setNeedRenderShadow(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(SHADOWRENDER, open);
        }
        if (open) {
            mSpatialEffect |= SHADOWRENDER;
        } else {
            mSpatialEffect &= ~SHADOWRENDER;
        }
    }

    /**
     * @param cullFace
     *            是否开启正反面检测，cullFace=true 时物体的反面不会被画出
     */
    public void setNeedCullFace(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(CULLFACE, open);
        }
        if (open) {
            mSpatialEffect |= CULLFACE;
        } else {
            mSpatialEffect &= ~CULLFACE;
        }
    }

    public boolean isCullFaceOpend() {
        return (mSpatialEffect & CULLFACE) != 0;
    }

    /**
     * @param depthTest
     *            是否开启深度检测
     */
    public void setNeedDepthTest(boolean open) {

        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(DEPTHENABLE, open);
        }
        if (open) {
            mSpatialEffect |= DEPTHENABLE;
        } else {
            mSpatialEffect &= ~DEPTHENABLE;
        }
    }

    private boolean mHasSyncDepthTest = false;

    private native boolean isEnableDepth_JNI();

    public boolean isDepthTestOpened() {
        // 在获取物体是否开启深度检测前先和引擎同步下
        if (!mHasSyncDepthTest) {
            boolean enable = isEnableDepth_JNI();
            if (enable) {
                mSpatialEffect |= DEPTHENABLE;
            } else {
                mSpatialEffect &= ~DEPTHENABLE;
            }
            mHasSyncDepthTest = true;
        }
        return (mSpatialEffect & DEPTHENABLE) != 0;
    }

    /**
     * @param blend
     *            是否开启颜色混合，对于不透明物体不应该开启
     */
    public void setBlendingable(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(BLENDABLE, open);
        }
        if (open) {
            mSpatialEffect |= BLENDABLE;
        } else {
            mSpatialEffect &= ~BLENDABLE;
        }
    }

    public void setNeedForeverBlend(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(FOREVERBLEND, open);
        }
        if (open) {
            mSpatialEffect |= FOREVERBLEND;
        } else {
            mSpatialEffect &= ~FOREVERBLEND;
        }
    }

    private boolean mHasSyncBlending = false;

    private native boolean isEnableBlend_JNI();

    public boolean isBlendingable() {
        // 在获取物体是否打开颜色混合前先和引擎同步下
        if (!mHasSyncBlending) {
            boolean enable = isEnableBlend_JNI();
            if (enable) {
                mSpatialEffect |= BLENDABLE;
            } else {
                mSpatialEffect &= ~BLENDABLE;
            }
            mHasSyncBlending = true;
        }

        return (mSpatialEffect & BLENDABLE) != 0;
    }

    /**
     * 雾化物体
     * 
     * @param isNeedFog
     *            是否雾化物体
     */
    public void setNeedFog(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(NEEDFOG, open);
        }
        if (open) {
            mSpatialEffect |= NEEDFOG;
        } else {
            mSpatialEffect &= ~NEEDFOG;
        }
    }

    /**
     * @param alphaTest
     *            是否开启Alpha检测，alphaTest=true 时Alpha<0.01的区域不会被画出
     */
    public void setNeedAlphaTest(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(ALPHATEST, open);
        }
        if (open) {
            mSpatialEffect |= ALPHATEST;
        } else {
            mSpatialEffect &= ~ALPHATEST;
        }
    }

    public boolean isAlphaTestOpened() {
        return (mSpatialEffect & ALPHATEST) != 0;
    }

    /**
     * @param miniBox
     *            设置物体是否是小物体，miniBox=true 时物体的点击区域会变大，很容易就点中了
     */
    public void setIsMiniBox(boolean open) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(MINIBOX, open);
        }
        if (open) {
            mSpatialEffect |= MINIBOX;
        } else {
            mSpatialEffect &= ~MINIBOX;
        }
    }

    /**
     * @param selected
     *            设置物体是否被选中，解Bug用，物体被选中后引擎会最后一个画
     */
    public void setSelected(boolean selected) {
        if (mHasBeenAddedToEngine) {
            setSpatialRuntimeAttribute_JNI(SELECTED, selected);
        }
        if (selected) {
            mSpatialRuntime |= SELECTED;
        } else {
            mSpatialRuntime &= ~SELECTED;
        }
    }

    public void setIsLine(boolean isLine) {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(DRAWLINE, isLine);
        }
        if (isLine) {
            mSpatialEffect |= DRAWLINE;
        } else {
            mSpatialEffect &= ~DRAWLINE;
        }
    }

    /**
     * @param enable
     *            设置物体是否可以被点击，enable=true物体不参与射线检测
     */
    public void setTouchable(boolean enable) {
        if (mHasBeenAddedToEngine) {
            setSpatialStateAttribute_JNI(TOUCHABLE, enable);
        }
        if (enable) {
            mSpatialState |= TOUCHABLE;
        } else {
            mSpatialState &= ~TOUCHABLE;
        }
    }

    /**
     * 物体是整体的话，点中后返回选中的物体是本物体；假如不是整体的话返回的是其所在的组即其父亲。 也就是说不是整体的物体不可以被选中参与点击事件
     * 请参考：SE3DScene.getDownHitObject()方法
     * 
     * @param isEntirety
     *            物体是否是整体
     */

    /**
     * @param imagepath
     *            图片的路径如assets/base/aa.png或者sdcard/exp/bb.jpg
     * @return 返回引擎SEImageData指针
     */
    public native static int loadImageData_JNI(String imagepath);

    /**
     * @param bitmap
     *            需要加载到引擎的图片
     * @return 返回引擎SEImageData指针
     */
    public native static int loadImageData_JNI(Bitmap bitmap);

    /**
     * @param imageDataObj
     *            释放引擎SEImageData指针
     */
    public native static void releaseImageData_JNI(int imageDataObj);

    /**
     * 添加图片到引擎，同时给与图片一个键值
     * 
     * @param key
     *            图片的键值，可以任意取名。多个ImageName可以共用一个ImageKey，这个时候多个物体共有一张图
     * @param imageDataObj
     *            引擎SEImageData指针
     */
    public native static void addImageData_JNI(String key, int imageDataObj);

    /**
     * 从引擎中删除图片
     * 
     * @param imageKey
     *            图片的键值
     */
    public native static void removeImageData_JNI(String imageKey);

    private String mTextureImageName;

    /**
     * 获取物体所用图片的名称，特殊情况下一个物体有可能有多张图，只返回第一张
     * 
     * @return 返回图片名称 ImageName
     */
    private native String getImageName_JNI();

    public String getImageName() {
        if (mHasBeenAddedToEngine && mTextureImageName == null) {
            mTextureImageName = getImageName_JNI();
        }
        return mTextureImageName;
    }

    private String mTextureImageKey;

    /**
     * 物体通过ImageName来查找ImageKey然后通过ImageKey来查找SEImageData指针，物体的ImageName是唯一的，
     * 多个ImageName可以对应一个ImageKey来达到共同图片的目的
     * 
     * @param imageName
     *            图片名称
     * @return 图片键值
     */
    public native static String getImageKey_JNI(String imageName);

    public String getImageKey() {
        if (mTextureImageKey == null && mHasBeenAddedToEngine) {
            String imageName = getImageName();
            if (imageName != null) {
                mTextureImageKey = getImageKey_JNI(imageName);
            }
        }
        return mTextureImageKey;
    }

    /**
     * 把图片名称和图片键值对应起来，模型文件中存储的是图片的名称，在配置文件中配置名称对应的图片路径(3DHome中以图片路径当作ImageKey)
     * 
     * @param imageName
     *            图片名称
     * @param imageKey
     *            图片键值
     */
    public native static void applyImage_JNI(String imageName, String imageKey);

    /**
     * 指定物体使用何种灯光，在使用灯光前应该首先把灯光加入场景中
     * 
     * @param lightName
     *            灯光名
     */
    public native void applyLight_JNI(String lightName);

    /**
     * 解除物体和该灯光的绑定
     */
    public native void unApplyLight_JNI(String lightName);

    /**
     * 获得物体的相对位置在世界坐标下的位置，如：假如人身高170cm，头顶相对于人本身的坐标（0，0，170cm），人在房间的东北角坐标（500cm，
     * 500cm，0）那么头顶相对于房子的坐标应该是（500cm, 500cm, 170cm)
     * 
     * @param local
     *            物体相对于自己的坐标
     * @return 返回物体相对于世界的坐标
     */
    public native float[] localToWorld_JNI(float[] local);
    
    public native float[] worldToLocal_JNI(float[] local);

    /**
     * 例如人站在房间中，输入房间中头顶相对于人本身的坐标（0，0，170cm），获取头顶在手机屏幕的位置
     * 
     * @param local
     *            物体相对于自己的坐标
     * @return 返回屏幕坐标
     */
    public native float[] localToScreenCoordinate_JNI(float[] local);

    private native SEObject getLeader_JNI();

    private native void setLeader_JNI(String lname, int lindex);

    private native void addFollower_JNI(String fname, int findex);

    private native void removeFollower_JNI(String fname, int findex);

    private boolean mIsEntirety = true;

    private native void setIsEntirety_JNI(boolean isEntirety);

    /**
     * 设置物体是否作为一个整体存在，假如不是整体射线检测的时候会返回其父亲这个时候物体点击不上
     */
    public void setIsEntirety(boolean isEntirety) {
        mIsEntirety = isEntirety;
        if (mHasBeenAddedToEngine) {
            setIsEntirety_JNI(isEntirety);
        }
    }

    /**
     * 贴图坐标反向
     * 
     * @param x
     *            贴图坐标左右反向
     * @param y
     *            贴图坐标上下反向
     */
    public native void setTexCoordXYReverse_JNI(boolean x, boolean y);

    public void setLeader(SEObject leader) {
        if (leader != null) {
            setLeader_JNI(leader.getName(), leader.mIndex);
            leader.addFollower_JNI(mName, mIndex);
        }
    }

    public void removeFollower(String followerName, int index) {
        if (followerName != null) {
            removeFollower_JNI(followerName, index);
        }
    }

    public SEObject getLeaderName() {
        return getLeader_JNI();
    }

    private native void changeParent_JNI(String newParent, int parentIndex);

    /**
     * 
     * @param newParent
     *            物体新的父亲或者新的节点
     */
    public void changeParent(SEObject newParent) {
        if (newParent != null) {
            if (mParent != null) {
                mParent.removeChild(this, false);
            }
            changeParent_JNI(newParent.mName, newParent.mIndex);
            newParent.addChild(this, false);
        }
    }

    /**
     * object will be removed from scene, but will not be released. return its
     * pointer.
     * 
     */
    public native int remove_JNI();

    private native void release_JNI();

    public void release() {
        onRelease();
        release_JNI();
    }

    private float mAlpha = 1;

    private native void setAlpha_JNI(float alpha);

    public void setAlpha(float alpha) {
        mAlpha = alpha;
        if (mHasBeenAddedToEngine) {
            setAlpha_JNI(alpha);
        }
    }

    public float getAlpha() {
        return mAlpha;
    }

    private int mBVType = 1;

    public void setBVType(int type) {
        mBVType = type;
    }

    public int getBVType() {
        return mBVType;
    }

    private int mLineWidth = 0;

    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    /**
     * 变换物体的顶点坐标，可用来制作顶点动画
     * 
     * @param vertex
     *            物体的顶点坐标
     */
    public native void updateVertex_JNI(float[] vertex);

    /**
     * 变换物体的贴图坐标
     * 
     * @param texVertex
     *            物体的贴图坐标
     */
    public native void updateTexture_JNI(float[] texVertex);

    /**
     * 克隆出一个新的物体，新的物体和老物体名称一样但是编号不一样
     * 
     * @param parent
     *            新的物体的挂载点或者说是父亲也可以
     * @param index
     *            新的物体的编号
     * @return 成功或者失败
     */
    public native boolean cloneObject_JNI(SEObject parent, int index);

    private int mLayerIndex = 0;

    private native void setLayerIndex_JNI(int index);

    /**
     * @param layerIndex
     *            设置物体绘制的序号，该值越小越先画，只对透明物体有效
     */
    public void setLayerIndex(int layerIndex) {
        mLayerIndex = layerIndex;
        if (mHasBeenAddedToEngine) {
            setLayerIndex_JNI(layerIndex);
        }
    }

    // LocalTransParas 和 UserTransParas ：物体变换包含两个矩阵，物体最终的位置由这两个变换叠加而成
    // #####################对物体的LocalTransParas进行设置###########begin
    private SETransParas mLocalTransParas = new SETransParas();

    /**
     * 设置物体的Local变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    private native void setLocalTransParas_JNI(float[] translate, float[] rotate, float[] scale);

    /**
     * 设置物体的Local变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    public void setLocalTransParas(SETransParas trans) {
        mLocalTransParas = trans;
        if (mHasBeenAddedToEngine) {
            setLocalTransParas();
        }
    }

    /**
     * 设置物体的Local变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    public void setLocalTransParas() {
        if (mHasBeenAddedToEngine) {
            setLocalTransParas_JNI(getLocalTransParas().getTranslate(), getLocalTransParas().getRotate(),
                    getLocalTransParas().geScale());
        }
    }

    /**
     * LocalTransParas 和 UserTransParas ：物体变换包含两个矩阵，物体最终的位置由这两个变换叠加而成
     * 为什么需要两套矩阵呢？ 有了两套矩阵我们可以很简单地实现一些复杂的动画如：物体移动的时候摆动；物体绕X轴旋转的同时绕Y轴旋转
     * 
     * @return 返回物体的本地旋转，平移，和缩放信息
     */
    public SETransParas getLocalTransParas() {
        return mLocalTransParas;
    }

    public SEVector3f getLocalTranslate() {
        return mLocalTransParas.mTranslate;
    }

    public SEVector3f getLocalScale() {
        return mLocalTransParas.mScale;
    }

    public SERotate getLocalRotate() {
        return mLocalTransParas.mRotate;
    }

    private native void setLocalRotate_JNI(float[] rotate);

    public void setLocalRotate(SERotate rotate) {
        mLocalTransParas.mRotate = rotate;
        if (mHasBeenAddedToEngine) {
            setLocalRotate_JNI(rotate.mD);
        }
    }

    private native void setLocalTranslate_JNI(float[] translate);

    public void setLocalTranslate(SEVector3f translate) {
        mLocalTransParas.mTranslate = translate;
        if (mHasBeenAddedToEngine) {
            setLocalTranslate_JNI(translate.mD);
        }
    }

    private native void setLocalScale_JNI(float[] translate);

    public void setLocalScale(SEVector3f scale) {
        mLocalTransParas.mScale = scale;
        if (mHasBeenAddedToEngine) {
            setLocalScale_JNI(scale.mD);
        }
    }

    // #####################对物体的LocalTransParas进行设置###########end

    // #####################对物体的UserTransParas进行设置###########begin
    private SETransParas mUserTransParas = new SETransParas();

    /**
     * 设置物体的User变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    private native void setUserTransParas_JNI(float[] translate, float[] rotate, float[] scale);

    /**
     * 设置物体的User变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    public void setUserTransParas(SETransParas trans) {
        mUserTransParas = trans;
        if (mHasBeenAddedToEngine) {
            setUserTransParas();
        }
    }

    /**
     * 设置物体的User变换：平移，旋转，和缩放。 Local变换和User变换没有任何区别，最终物体的状态是由两个变换叠加而成
     */
    public void setUserTransParas() {
        if (mHasBeenAddedToEngine) {
            setUserTransParas_JNI(getUserTransParas().getTranslate(), getUserTransParas().getRotate(),
                    getUserTransParas().geScale());
        }
    }

    /**
     * LocalTransParas 和 UserTransParas ：物体变换包含两个矩阵，物体最终的位置由这两个变换叠加而成
     * 为什么需要两套矩阵呢？ 有了两套矩阵我们可以很简单地实现一些复杂的动画如：物体移动的时候摆动；物体绕X轴旋转的同时绕Y轴旋转
     * 
     * @return 返回物体的用户级旋转，平移，和缩放信息
     */
    public SETransParas getUserTransParas() {
        return mUserTransParas;
    }

    private native void setUserRotate_JNI(float[] rotate);

    public void setUserRotate(SERotate rotate) {
        mUserTransParas.mRotate = rotate;
        if (mHasBeenAddedToEngine) {
            setUserRotate_JNI(rotate.mD);
        }
    }

    private native void setUserTranslate_JNI(float[] translate);

    public void setUserTranslate(SEVector3f translate) {
        mUserTransParas.mTranslate = translate;
        if (mHasBeenAddedToEngine) {
            setUserTranslate_JNI(translate.mD);
        }
    }

    private native void setUserScale_JNI(float[] scale);

    public void setUserScale(SEVector3f scale) {
        mUserTransParas.mScale = scale;
        if (mHasBeenAddedToEngine) {
            setUserScale_JNI(scale.mD);
        }
    }

    public SERotate getUserRotate() {
        return mUserTransParas.mRotate;
    }

    public SEVector3f getUserTranslate() {
        return mUserTransParas.mTranslate;
    }

    public SEVector3f getUserScale() {
        return mUserTransParas.mScale;
    }

    // #####################对物体的UserTransParas进行设置###########end
    /**
     * @return 返回物体的中心点在世界坐标系下的位置
     */
    public SEVector3f getAbsoluteTranslate() {
        SEVector3f center = new SEVector3f();
        float[] local = localToWorld_JNI(new float[] { 0, 0, 0 });
        if (local != null) {
            center.set(local);
        }
        return center;
    }

    /**
     * child Object will be be hanged in this node
     * 
     * @param childObject
     *            object's pointer
     */
    public native void addChild_JNI(int childObject);

    /**
     * 
     * @param vect4f
     *            , vect4f[0] = R, vect4f[1] = G, vect4f[2] = B, vect4f[3] =
     *            1(use) or 0(not use) user color
     */
    private native void setUseUserColor_JNI(float[] vect4f);

    public void setUseUserColor(float r, float g, float b) {
        setUseUserColor_JNI(new float[] { r, g, b, 1 });
    }

    public void clearUserColor() {
        setUseUserColor_JNI(new float[] { 1, 1, 1, 0 });
    }

    /**
     * 
     * @param offset
     *            , offset.xy should be form -1.0 to 1.0
     * 
     */
    private native void uvAnimation_JNI(float[] offset, boolean enableUVAnimation);

    public void playUVAnimation(SEVector2f direction) {
        uvAnimation_JNI(direction.mD, true);
    }

    public void stopUVAnimation() {
        uvAnimation_JNI(null, false);
    }

    /**
     * @param x
     *            : screen point x
     * @param y
     *            : screen point y
     * @param selPoint
     *            : get the crossed point if screen point crossed this object
     */
    public native boolean isObjectSelected_JNI(int x, int y, float[] selPoint);

    /**
     * 在某个轴方向，由里向外，由上到下，由左到右排序，在前面的物体先画
     */
    public void setBlendSortAxis(AXIS axis) {
        if (mHasBeenAddedToEngine) {
            if (axis == AXIS.X) {
                setSpatialEffectAttribute_JNI(BLENDX, true);
                setSpatialEffectAttribute_JNI(BLENDY, false);
                setSpatialEffectAttribute_JNI(BLENDZ, false);
            } else if (axis == AXIS.Y) {
                setSpatialEffectAttribute_JNI(BLENDY, true);
                setSpatialEffectAttribute_JNI(BLENDX, false);
                setSpatialEffectAttribute_JNI(BLENDZ, false);
            } else if (axis == AXIS.Z) {
                setSpatialEffectAttribute_JNI(BLENDZ, true);
                setSpatialEffectAttribute_JNI(BLENDX, false);
                setSpatialEffectAttribute_JNI(BLENDY, false);
            }

        }
        if (axis == AXIS.X) {
            mSpatialEffect |= BLENDX;
            mSpatialEffect &= ~BLENDY;
            mSpatialEffect &= ~BLENDZ;
        } else if (axis == AXIS.Y) {
            mSpatialEffect |= BLENDY;
            mSpatialEffect &= ~BLENDZ;
            mSpatialEffect &= ~BLENDX;
        } else if (axis == AXIS.Z) {
            mSpatialEffect |= BLENDZ;
            mSpatialEffect &= ~BLENDY;
            mSpatialEffect &= ~BLENDX;
        }
    }

    public void clearBlendSortAxis() {
        if (mHasBeenAddedToEngine) {
            setSpatialEffectAttribute_JNI(BLENDX, false);
            setSpatialEffectAttribute_JNI(BLENDY, false);
            setSpatialEffectAttribute_JNI(BLENDZ, false);

        }
        mSpatialEffect &= ~BLENDX;
        mSpatialEffect &= ~BLENDY;
        mSpatialEffect &= ~BLENDZ;

    }

    public SEVector3f getSelectPoint(int x, int y) {
        SEVector3f result = new SEVector3f();
        if (isObjectSelected_JNI(x, y, result.mD)) {
            return result;
        }
        return null;
    }

    public native void showRBColor_JNI(boolean show);

    public native static boolean isImageExist_JNI(String imagePath);

    public native boolean isExistent_JNI();

    public native void setComponentAttribute_JNI(SEComponentAttribute componentAttribute);

    private native void setName_JNI(String name);

    /**
     * can change object's name via this method
     */
    public void setName(String name) {
        setName_JNI(name);
        mName = name;
    }

    /**
     * 估算值不是很准确，而且只有在物体已经添加进场景并且绘制后才能获取其大小
     * 返回六个浮点数，前三个表示物体在世界坐标系下最小的坐标点，后三个表示物体在世界坐标系下最大的坐标点
     */
    public native float[] getSizeOfObject_JNI();

    // -----------------------------------finish------------------jni
    // trans object end.

    private static final int PRESSED = 0x00000001;
    private static final int CLICKABLE = 0x00000002;
    private int mPrivateFlags;
    private boolean mHasPerformedLongPress;
    private Timer mTimer;
    private OnTouchListener mOnClickListener;
    private OnTouchListener mOnLongClickListener;
    private OnTouchListener mOnPressedListener;
    private OnTouchListener mOnUnpressedListener;
    private OnTouchListener mOnDoubleClickListener;
    protected DepriveTouchListener mDepriveTouchListener;
    private CheckForLongPress mCheckForLongPress;
    private int mPreTouchX;
    private int mPreTouchY;
    private int mTouchX;
    private int mTouchY;
    // private MediaPlayer mp;
    public String mName;
    public int mIndex;
    private SEObject mParent;
    private SEScene mScene;
    private String mSceneName;
    private String mParentName;
    private SECamera mSECamera;
    private float mPressValue;
    private PRESS_TYPE mPressType;
    private boolean mIsNode;
    private AudioManager mAudioManager;
    /**
     * Cache the touch slop from the context that created the view.
     */
    private int mTouchSlop;
    private Object mTag;

    private Context mContext;
    private ObjectPressedAnimation mObjectPressedAnimation;
    private ObjectUnpressedAnimation mObjectUnpressedAnimation;
    private boolean mHasBeenReleased;
    private boolean mHasBeenAddedToEngine;

    private Timer mDoublieClickTimer;
    private CheckForDoubleClick mCheckForDoubleClick;
    private int mClickCount;
    private List<SEObject> mChildObjects = new ArrayList<SEObject>();
    public SEObject mMotionTarget;

    public interface OnTouchListener {
        public void run(SEObject obj);
    }

    public interface DepriveTouchListener {
        boolean onTouch(SEObject obj, MotionEvent event);
    }

    /**
     * 创建物体的时候需要给定物体的名字和索引（索引默认为0），不同的物体名字可以相同，对于引擎来说索引大于0的物体是由索引为0的同名物体克隆而来
     */
    public SEObject(SEScene scene, String name) {
        mIndex = 0;
        mName = name;
        mScene = scene;
        init();

    }

    /**
     * 创建物体的时候需要给定物体的名字和索引，不同的物体名字可以相同，对于引擎来说索引大于0的物体是由索引为0的同名物体克隆而来
     */
    public SEObject(SEScene scene, String name, int index) {
        mIndex = index;
        mName = name;
        mScene = scene;
        init();
    }

    private SEObject(String name, int index) {
        mIndex = index;
        mName = name;
        init();
    }

    private void init() {
        mHasBeenAddedToEngine = false;
        mHasGetBlending = false;
        mPressValue = 1;
        mPressType = PRESS_TYPE.ALPHA;
        mIsNode = false;
        mPrivateFlags = 2;
        mSceneName = "home";
        mParentName = "home_root";
        mHasBeenReleased = false;
        if (mScene != null) {
            mSceneName = mScene.getSceneName();
            mContext = mScene.getContext();
            mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        }

    }

    /**
     * 设置物体是否已经添加到引擎了。
     * PS：模型物体及其组件在物体加载完后就已经添加到引擎了，所以该状态要手动设置为true；克隆物体在克隆完成后就加到引擎了
     * 
     * @param hasBeenAdded
     *            只有hasBeenAdded=true 时很多接口才可以使用
     */
    public void setHasBeenAddedToEngine(boolean hasBeenAdded) {
        mHasBeenAddedToEngine = hasBeenAdded;
    }

    public boolean hasBeenAddedToEngine() {
        return mHasBeenAddedToEngine;
    }

    public void setIsGroup(boolean isGroup) {
        mIsNode = isGroup;
    }

    /**
     * @param obj
     *            需要被添加的物体
     * @param create
     *            物体是否需要创建或者提交(模型文件中的物体在模型加载到引擎时已经被创建了，所有不需要再次创建)
     */
    public void addChild(SEObject obj, boolean create) {
        if (mIsNode) {
            obj.setParent(this);
            if (!mChildObjects.contains(obj)) {
                mChildObjects.add(obj);
                if (getScene() != null) {
                    obj.setScene(getScene());
                    if (create) {
                        obj.render();
                    }
                }
            }
        }
    }

    public void removeChild(SEObject child, boolean release) {
        if (mIsNode) {
            if (mChildObjects.contains(child)) {
                mChildObjects.remove(child);
                if (release) {
                    child.release();
                }
            }
        }
    }

    public List<SEObject> getChildObjects() {
        return mChildObjects;
    }

    public int getTouchSlop() {
        return mTouchSlop;
    }

    private SEObject findChild(String name, int index) {
        for (SEObject obj : mChildObjects) {
            if (index == obj.mIndex && obj.getName().equals(name)) {
                return obj;
            }
            if (obj.mIsNode) {
                SEObject found = obj.findChild(name, index);
                if (found != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    public Context getContext() {
        return mContext;
    }

    public SEScene getScene() {
        return mScene;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    private native void addUserObject_JNI();

    public void render() {
        if (mHasBeenReleased || mHasBeenAddedToEngine) {
            return;
        }
        onRender(getCamera());
        addUserObject_JNI();
        mHasBeenAddedToEngine = true;
        if (!mIsNode) {
            if (mImageType == IMAGE_TYPE.IMAGE) {
                applyImage_JNI(mTextureImageName, mTextureImageKey);
                SELoadResThread.getInstance().process(new Runnable() {
                    public void run() {
                        if (mHasBeenReleased) {
                            return;
                        }
                        final int image = loadImageData_JNI(mTextureImageKey);
                        new SECommand(getScene()) {
                            public void run() {
                                if (mHasBeenReleased) {
                                    return;
                                }
                                addImageData_JNI(mTextureImageKey, image);
                            }
                        }.execute();
                    }
                });
            } else if (mImageType == IMAGE_TYPE.BITMAP) {
                applyImage_JNI(mTextureImageName, mTextureImageKey);
                if (getBitmap() != null) {
                    SELoadResThread.getInstance().process(new Runnable() {
                        public void run() {
                            if (mHasBeenReleased) {
                                return;
                            }
                            Bitmap bitmap = getBitmap();
                            if (!SEUtils.isPower2(bitmap.getWidth()) || !SEUtils.isPower2(bitmap.getHeight())) {
                                int newImageWidth = SEUtils.higherPower2(bitmap.getWidth());
                                int newImageHeight = SEUtils.higherPower2(bitmap.getHeight());
                                bitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
                            }
                            final int image = loadImageData_JNI(bitmap);
                            recycleMemory();
                            new SECommand(getScene()) {
                                public void run() {
                                    addImageData_JNI(mTextureImageKey, image);
                                }
                            }.execute();
                        }
                    });
                } else {
                    SELoadResThread.getInstance().process(new Runnable() {
                        public void run() {
                            if (mHasBeenReleased) {
                                return;
                            }
                            Bitmap bitmap = onStartLoadImage();                            
                            if (bitmap != null) {
                                if (!SEUtils.isPower2(bitmap.getWidth()) || !SEUtils.isPower2(bitmap.getHeight())) {
                                    int newImageWidth = SEUtils.higherPower2(bitmap.getWidth());
                                    int newImageHeight = SEUtils.higherPower2(bitmap.getHeight());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
                                }
                                setBitmap(bitmap);
                                final int image = loadImageData_JNI(bitmap);
                                recycleMemory();
                                new SECommand(getScene()) {
                                    public void run() {
                                        addImageData_JNI(mTextureImageKey, image);
                                    }
                                }.execute();
                            }
                        }
                    });
                }
            }

        }
        onRenderFinish(getCamera());
    }

    public void onRender(SECamera camera) {

    }

    public void onRenderFinish(SECamera camera) {
    }

    public Bitmap onStartLoadImage() {
        return null;
    }

    public void stopAllAnimation(SEAnimFinishListener l) {

    }

    public String getName() {
        return mName;
    }

    public SEObject getParent() {
        return mParent;
    }

    public void setParent(SEObject parent) {
        mParent = parent;
    }

    public void setScene(SEScene scene) {
        mScene = scene;
        if (mIsNode) {
            for (SEObject obj : mChildObjects) {
                obj.setScene(scene);
            }
        }
    }

    private String getSceneName() {
        if (mScene != null) {
            return mScene.getSceneName();
        }
        return null;
    }

    public String getParentName() {
        if (mParent != null) {
            return mParent.getName();
        }
        return null;
    }

    public int getParentIndex() {
        if (mParent != null) {
            return mParent.mIndex;
        }
        return 0;
    }

    public void setPressed(boolean pressed) {
        if (pressed && !isPressed()) {
            mPrivateFlags |= PRESSED;
            setSelected(true);
            performPressed();
        } else if (!pressed && isPressed()) {
            mPrivateFlags &= ~PRESSED;
            setSelected(false);
            performUnpressed();
        }
    }

    public boolean isPressed() {
        return (mPrivateFlags & PRESSED) == PRESSED;
    }

    public void setClickable(boolean clickable) {
        if (clickable) {
            mPrivateFlags |= CLICKABLE;
        } else if (!clickable) {
            mPrivateFlags &= ~CLICKABLE;
        }
    }

    public boolean isClickable() {
        return (mPrivateFlags & CLICKABLE) == CLICKABLE;
    }

    public void notifySurfaceChanged(int width, int height) {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                object.notifySurfaceChanged(width, height);
            }
        }
    }

    /**
     * Register a callback to be invoked when this view is clicked. If this view
     * is not clickable, it becomes clickable.
     * 
     * @param l
     *            The callback that will run
     * 
     * @see #setClickable(boolean)
     */
    public void setOnClickListener(OnTouchListener l) {
        mOnClickListener = l;
    }

    public void setOnDoubleClickListener(OnTouchListener l) {
        mOnDoubleClickListener = l;
    }

    /**
     * Register a callback to be invoked when this view is clicked and held. If
     * this view is not long clickable, it becomes long clickable.
     * 
     * @param l
     *            The callback that will run
     * 
     */
    public void setOnLongClickListener(OnTouchListener l) {
        mOnLongClickListener = l;
    }

    public enum PRESS_TYPE {
        ALPHA, COLOR, NONE
    }

    public void setPressType(PRESS_TYPE type) {
        mPressType = type;
    }

    public void setPressedListener(OnTouchListener l) {
        mOnPressedListener = l;
    }

    public void setUnpressedListener(OnTouchListener l) {
        mOnUnpressedListener = l;
    }

    public void setDepriveTouchListener(DepriveTouchListener l) {
        mDepriveTouchListener = l;
    }

    public int getTouchX() {
        return mTouchX;
    }

    public int getTouchY() {
        return mTouchY;
    }

    public void setTouch(int x, int y) {
        mTouchX = x;
        mTouchY = y;
    }

    public int getPreTouchX() {
        return mPreTouchX;
    }

    public int getPreTouchY() {
        return mPreTouchY;
    }

    public void setPreTouch() {
        mPreTouchX = mTouchX;
        mPreTouchY = mTouchY;
    }

    public void setPreTouch(int x, int y) {
        mPreTouchX = x;
        mPreTouchY = y;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_CANCEL) {
            setTouch((int) event.getX(), (int) event.getY());
        }
        if (mDepriveTouchListener != null && mDepriveTouchListener.onTouch(this, event)) {
            return true;
        }
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mMotionTarget != null) {
                // this is weird, we got a pen down, but we thought it was
                // already down!
                // XXX: We should probably send an ACTION_UP to the current
                // target.
                mMotionTarget = null;
            }
            if (!onInterceptTouchEvent(event)) {
                if (mChildObjects.size() > 0) {
                    SEObject child = getHitObject(getTouchX(), getTouchY());
                    if (child != null) {
                        if (child.dispatchTouchEvent(event)) {
                            // Event handled, we have a target now.
                            mMotionTarget = child;
                            return true;
                        }
                    }
                }
            }
        }

        final SEObject target = mMotionTarget;
        if (target == null) {
            return onTouchEvent(event);
        }
        boolean isUpOrCancel = (action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL);
        if (!isUpOrCancel && onInterceptTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            if (!target.dispatchTouchEvent(event)) {
                // target didn't handle ACTION_CANCEL. not much we can do
                // but they should have.
            }
            mMotionTarget = null;
            return true;
        }
        if (isUpOrCancel) {
            mMotionTarget = null;
        }

        return target.dispatchTouchEvent(event);
    }

    public SEObject getHitObject(int x, int y) {
        if (!mIsNode) {
            return null;
        }
        SEObject hitObj = getScene().getDownHitObject();
        if (hitObj == null) {
            return null;
        }
        SEObject obj = findChild(hitObj.mName, hitObj.mIndex);
        if (obj != null && obj.isClickable()) {
            return obj;
        }
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            if (isPressed()) {
                setPressed(false);
                if (!mHasPerformedLongPress) {
                    // This is a tap, so remove the longpress check
                    removeCallbacks();
                    if (mClickCount > 1) {
                        removeDoublieClick();
                        mClickCount = 0;
                        performDoubleClick();
                    } else {
                        performClick();
                    }
                }
            }
            break;
        case MotionEvent.ACTION_DOWN:
            mClickCount++;
            postCheckForLongClick();
            removeDoublieClick();
            postCheckForDoubleClick();
            setPressed(true);
            break;

        case MotionEvent.ACTION_CANCEL:
            if (!mHasPerformedLongPress) {
                removeCallbacks();
            }
            setPressed(false);
            if (mObjectPressedAnimation != null) {
                mObjectPressedAnimation.stop();
            }
            if (mObjectUnpressedAnimation != null) {
                mObjectUnpressedAnimation.stop();
            }
            break;

        case MotionEvent.ACTION_MOVE:
            break;
        }
        return true;
    }

    public boolean checkOutside(int x, int y) {
        if (getCamera() == null) {
            return false;
        }
        SEObject selObj = getCamera().getSelectedObject_JNI(x, y);
        if (selObj != null) {
            if (selObj.equals(this))
                return false;
        }
        return true;
    }

    public void setCamera(SECamera camera) {
        mSECamera = camera;
    }

    public SECamera getCamera() {
        if (mSECamera == null) {
            if (mScene != null) {
                mSECamera = mScene.getCamera();
            }
        }
        return mSECamera;
    }

    public void performPressed() {
        if (mPressType != PRESS_TYPE.NONE) {
            if (mObjectPressedAnimation != null) {
                mObjectPressedAnimation.stop();
            }
            if (mObjectUnpressedAnimation != null) {
                mObjectUnpressedAnimation.stop();
            }
            mObjectPressedAnimation = new ObjectPressedAnimation(getScene());
            mObjectPressedAnimation.execute();
        }
        if (mOnPressedListener != null) {
            mOnPressedListener.run(this);
        }
    }

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return mAudioManager;
    }

    public void performUnpressed() {
        if (mPressType != PRESS_TYPE.NONE) {
            if (mObjectPressedAnimation != null) {
                mObjectPressedAnimation.stop();
            }
            if (mObjectUnpressedAnimation != null) {
                mObjectUnpressedAnimation.stop();
            }
            mObjectUnpressedAnimation = new ObjectUnpressedAnimation(getScene());
            mObjectUnpressedAnimation.execute();

        }
        if (mOnUnpressedListener != null) {
            mOnUnpressedListener.run(this);
        }
    }

    public boolean performLongClick() {
        if (mOnLongClickListener != null) {
            playLongClickVibrator();
            mOnLongClickListener.run(SEObject.this);
            return true;

        }
        return false;
    }

    private void playLongClickVibrator() {
        try {
            if (getAudioManager().shouldVibrate(AudioManager.VIBRATE_SETTING_ON)) {
                Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
            } else {
                Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                vibrator.vibrate(30);
            }
        } catch (SecurityException e) {

        }
    }

    public void performClick() {
        if (mOnClickListener != null) {
            playClickSound();
            mOnClickListener.run(this);
        }
    }

    public void playClickSound() {
        getAudioManager().playSoundEffect(SoundEffectConstants.CLICK);
    }

    public void performDoubleClick() {
        if (mOnDoubleClickListener != null) {
            getAudioManager().playSoundEffect(SoundEffectConstants.CLICK);
            mOnDoubleClickListener.run(this);
        }
    }

    public void removeLongClick() {
        removeCallbacks();
    }

    private void removeCallbacks() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void removeDoublieClick() {
        if (mDoublieClickTimer != null) {
            mDoublieClickTimer.cancel();
        }
    }

    public void postCheckForLongClick() {
        mHasPerformedLongPress = false;
        mCheckForLongPress = new CheckForLongPress();
        postDelayed(mCheckForLongPress, ViewConfiguration.getLongPressTimeout());
    }

    private void postCheckForDoubleClick() {
        mCheckForDoubleClick = new CheckForDoubleClick();
        mDoublieClickTimer = new Timer();
        mDoublieClickTimer.schedule(mCheckForDoubleClick, ViewConfiguration.getDoubleTapTimeout());
    }

    private void postDelayed(TimerTask task, long delayMillis) {
        mTimer = new Timer();
        mTimer.schedule(task, delayMillis);
    }

    public boolean hasPerformedLongPress() {
        return mHasPerformedLongPress;
    }

    class CheckForLongPress extends TimerTask {
        public void run() {
            new SECommand(getScene()) {
                public void run() {
                    if (isPressed()) {
                        if (performLongClick()) {
                            mHasPerformedLongPress = true;
                        }
                    }
                }
            }.execute();
        }
    }

    class CheckForDoubleClick extends TimerTask {
        public void run() {
            new SECommand(getScene()) {
                public void run() {
                    mClickCount = 0;
                }
            }.execute();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof SEObject)) {
            return false;
        }
        SEObject newobj = (SEObject) obj;
        if (mName.equals(newobj.mName) && mIndex == newobj.mIndex) {
            return true;
        } else {
            return false;
        }

    }

    public void onActivityRestart() {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onActivityRestart();
                }
            }
        }
    }

    public void onActivityResume() {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onActivityResume();
                }
            }
        }
    }

    public void onActivityPause() {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onActivityPause();
                }
            }
        }
    }

    public void onActivityDestory() {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onActivityDestory();
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    public boolean handleBackKey(SEAnimFinishListener l) {
        boolean result = false;
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object.handleBackKey(l)) {
                    result = true;
                }
            }
        }
        return result;
    }

    public void onPressHomeKey() {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onPressHomeKey();
                }
            }
        }
    }

    /**
     * @param sceneChanged
     *            主题切换有两种情：一种只是房间的样式，房间，相机有变化； 另一种是整个场景的变化，释放前一个场景的所有物体，加载新场景的物体
     * 
     */
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onSceneChanged(changeType);
                }
            }
        }
    }

    public boolean hasBeenReleased() {
        return mHasBeenReleased;
    }

    public void onRelease() {
        mHasBeenReleased = true;
        if (mIsNode) {
            List<SEObject> objects = new ArrayList<SEObject>();
            objects.addAll(mChildObjects);
            for (SEObject object : objects) {
                if (object != null) {
                    object.onRelease();
                }
            }
        }
    }

    // ################################################press animation
    private boolean mIsEnbaleBlending;
    private boolean mHasGetBlending;

    private class ObjectPressedAnimation extends CountAnimation {

        public ObjectPressedAnimation(SEScene scene) {
            super(scene);
        }

        public void runPatch(int times) {
            mPressValue = mPressValue - 0.05f;
            if (mPressValue <= 0.5f) {
                mPressValue = 0.5f;
                stop();
            } else {
                if (mPressType == PRESS_TYPE.ALPHA) {
                    setAlpha(mPressValue);
                } else {
                    setUseUserColor(mPressValue, mPressValue, mPressValue);
                }
            }
        }

        @Override
        public void onFinish() {
            mPressValue = 0.5f;
            if (mPressType == PRESS_TYPE.ALPHA) {
                setAlpha(mPressValue);
            } else {
                setUseUserColor(mPressValue, mPressValue, mPressValue);
            }
        }

        @Override
        public void onFirstly(int count) {
            if (mPressType == PRESS_TYPE.ALPHA) {
                if (!mHasGetBlending) {
                    mIsEnbaleBlending = isBlendingable();
                    mHasGetBlending = true;
                }
                if (!mIsEnbaleBlending) {
                    setBlendingable(true);
                }
            }
            if (!isVisible()) {
                stop();
            }
        }

    }

    private class ObjectUnpressedAnimation extends CountAnimation {

        public ObjectUnpressedAnimation(SEScene scene) {
            super(scene);
        }

        public void runPatch(int times) {
            mPressValue = mPressValue + 0.05f;
            if (mPressValue >= 1) {
                mPressValue = 1;
                stop();
            } else {
                if (mPressType == PRESS_TYPE.ALPHA) {
                    setAlpha(mPressValue);
                } else {
                    setUseUserColor(mPressValue, mPressValue, mPressValue);
                }
            }
        }

        @Override
        public void onFirstly(int count) {
            if (mPressType == PRESS_TYPE.ALPHA) {
                if (!mHasGetBlending) {
                    mIsEnbaleBlending = isBlendingable();
                    mHasGetBlending = true;
                }
            }
            if (!isVisible()) {
                stop();
            }
        }

        public void onFinish() {
            mPressValue = 1;
            if (mPressType == PRESS_TYPE.ALPHA) {
                setAlpha(mPressValue);
                if (mHasGetBlending) {
                    if (!mIsEnbaleBlending) {
                        setBlendingable(false);
                    } else {
                        setBlendingable(true);
                    }
                }
            } else {
                clearUserColor();
            }
        }
    }

    /** #########下面为创建物体相关的数据############### **/
    public enum IMAGE_TYPE {
        IMAGE, BITMAP, COLOR
    }

    private float[] mVertexArray;
    private float[] mTexVertexArray;
    private int[] mFaces;
    private float[] mColor = { 0, 0, 0 };
    private Bitmap mBitmap;
    private int mImageWidth;
    private int mImageHeight;
    private boolean mHasResizeImage = true;
    private boolean mNeedRevertImage = false;
    public IMAGE_TYPE mImageType = IMAGE_TYPE.COLOR;

    public void recycleMemory() {
        if (mBitmap != null) {
            if (!mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            mBitmap = null;
        }

        mVertexArray = null;
        mTexVertexArray = null;
        mFaces = null;
    }

    /**
     * 假如图片大小不是2的n次方需要对图片上下左右进行填充，有效区居中。 调用完该接口后会对贴图坐标进行调整
     * 
     * @param w
     *            图片有效区域的宽度
     * 
     * @param h
     *            图片有效区域的高度
     */
    public void setImageValidAreaSize(int w, int h) {
        mImageWidth = w;
        mImageHeight = h;
        if (SEUtils.isPower2(mImageWidth) && SEUtils.isPower2(mImageHeight)) {
            mHasResizeImage = true;
        } else {
            mHasResizeImage = false;
        }
    }

    /**
     * 上下反转贴图坐标，需要在物体提交或者添加到引擎之前执行
     */
    public void revertTextureImage() {
        mNeedRevertImage = true;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setColor(float[] color) {
        mColor = color;
    }

    public float[] getColor() {
        return mColor;
    }

    public void setVertexArray(float[] vertex) {
        mVertexArray = vertex;
    }

    public float[] getVertexArray() {
        return mVertexArray;
    }

    public void setTexVertexArray(float[] texVertex) {
        mTexVertexArray = texVertex;
    }

    public float[] getTexVertexArray() {
        if (!mHasResizeImage) {
            mHasResizeImage = true;
            int power2Width = SEUtils.higherPower2(mImageWidth);
            int power2Height = SEUtils.higherPower2(mImageHeight);
            float starty = (power2Height - mImageHeight) * 0.5f;
            float startx = (power2Width - mImageWidth) * 0.5f;
            int textureCoorNum = mTexVertexArray.length / 2;
            for (int i = 0; i < textureCoorNum; i++) {
                mTexVertexArray[2 * i] = (startx + mTexVertexArray[2 * i] * mImageWidth) / power2Width;
                mTexVertexArray[2 * i + 1] = (starty + mTexVertexArray[2 * i + 1] * mImageHeight) / power2Height;
            }
        }
        if (mNeedRevertImage) {
            mNeedRevertImage = false;
            int textureCoorNum = mTexVertexArray.length / 2;
            for (int i = 0; i < textureCoorNum; i++) {
                mTexVertexArray[2 * i + 1] = 1 - mTexVertexArray[2 * i + 1];
            }
        }
        return mTexVertexArray;
    }

    public void setFaceArray(int[] faces) {
        mFaces = faces;
    }

    public int[] getFaceArray() {
        return mFaces;
    }

    public void setImage(IMAGE_TYPE imageType, String imageName, String ImageKey) {
        mTextureImageName = imageName;
        mTextureImageKey = ImageKey;
        mImageType = imageType;
    }

    public int getImageType() {
        switch (mImageType) {
        case IMAGE:
            return 0;
        case BITMAP:
            return 1;
        case COLOR:
            return 2;
        }
        return 0;
    }

}
