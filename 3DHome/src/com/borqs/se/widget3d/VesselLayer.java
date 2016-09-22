package com.borqs.se.widget3d;

import android.graphics.Rect;

import android.view.animation.CycleInterpolator;
import com.borqs.se.engine.*;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeSceneInfo;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

import java.util.ArrayList;
import java.util.List;

public class VesselLayer {
    private HomeScene mHomeScene;
    private VesselLayer mVesselInfo;
    private VesselObject mVesselObject;
    private MoveObject mOnMoveObject;
    private boolean mOnLayerModel;
    protected Rect mBoundOfRecycle;
    private boolean mInRecycle;
    protected List<MoveObject> mExistentObjects;

    public enum ACTION {
        BEGIN, MOVE, UP, FLY, FINISH
    }

    public VesselLayer(HomeScene scene, VesselObject vesselObject) {
        mHomeScene = scene;
        mVesselObject = vesselObject;
        mOnLayerModel = false;
    }

    public void setInRecycle(boolean inRecycle) {
        mInRecycle = inRecycle;
    }

    public boolean isInRecycle() {
        return mInRecycle;
    }

    public HomeScene getHomeScene() {
        return mHomeScene;
    }

    public HomeSceneInfo getHomeSceneInfo() {
        return mHomeScene.getHomeSceneInfo();
    }

    public void setVesselInfo(VesselLayer info) {
        mVesselInfo = info;
    }

    public VesselLayer getVesselInfo() {
        return mVesselInfo;
    }

    /**
     * Decide whether this draglayer can deal with the moving object
     * 
     * @param onDragObject
     *            the object which is on drag
     * @param touchX
     *            the move point x on screen
     * @param touchY
     *            the move point y on screen
     */
    public boolean canHandleSlot(NormalObject onDragObject, float touchX, float touchY) {
        return false;
    }

    /**
     * Decide whether this draglayer can deal with the moving object
     * 
     * @param onMoveObject
     *            the object which is on drag
     * @param onLayerModel
     *            this draglayer will handle the touch event if set it as true
     */
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        mOnMoveObject = new MoveObject();
        mOnMoveObject.mMoveObject = onMoveObject;
        mOnMoveObject.mMoveSlot = onMoveObject.getObjectSlot().clone();
        mOnLayerModel = onLayerModel;
        if (onLayerModel) {
            getExistentObjects(true);
        }

        return true;
    }

    /**
     * 直接把物体摆放入容器中，需要经过位置冲突判断
     */
    public boolean placeObjectToVessel(NormalObject normalObject) {
        return false;
    }

    /**
     * 直接把物体摆放入容器中，不需要经过位置冲突判断
     */
    protected boolean placeObjectToVesselDirectly(NormalObject normalObject) {
        if (normalObject.getObjectSlot().mVesselID == getVesselId()) {
            mVesselObject.placeObjectToVessel(normalObject, false);
            normalObject.setVisible(true);
            normalObject.onSlotSuccess();
            return true;
        }
        return false;
    }

    /**
     * this draglayer will handle the touch event via this or another same name
     * method
     * 
     */
    public boolean onObjectMoveEvent(ACTION event, float x, float y) {
        return false;
    }

    /**
     * this draglayer will handle the touch event via this or another same name
     * method
     * 
     */
    public boolean onObjectMoveEvent(ACTION event, SEVector3f location) {
        return false;
    }

    public MoveObject getMoveObject() {
        return mOnMoveObject;
    }

    public NormalObject getOnMoveObject() {
        return mOnMoveObject.mMoveObject;
    }

    public VesselObject getVesselObject() {
        return mVesselObject;
    }

    public boolean isOnLayerModel() {
        return mOnLayerModel;
    }

    public void handleOutsideRoom() {

    }

    public void handleNoMoreRoom() {

    }

    public void handleSlotSuccess() {
        if (mOnMoveObject != null && mVesselObject != null) {
            ObjectInfo info = mOnMoveObject.getObjectInfo();
            info.mObjectSlot.mVesselID = mVesselObject.getObjectInfo().mID;
        }
    }

    public void setBoundOfRecycle(Rect boundOfRecycle) {
        mBoundOfRecycle = boundOfRecycle;
    }

    public static boolean cmpSlot(ObjectSlot objectSlot1, ObjectSlot objectSlot2) {
        if (objectSlot1 == null && objectSlot2 == null) {
            return true;
        }
        return !((objectSlot1 != null && objectSlot2 == null) || (objectSlot1 == null && objectSlot2 != null)) && objectSlot1.equals(objectSlot2);
    }

    protected SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        return mVesselObject.getTransParasInVessel(needPlaceObj, objectSlot);
    }

    protected SEVector3f getTranslateInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        return getTransParasInVessel(needPlaceObj, objectSlot).mTranslate;
    }

    protected SEVector3f getTranslateInVessel(ObjectSlot objectSlot) {
        return getTransParasInVessel(null, objectSlot).mTranslate;
    }

    protected int getVesselId() {
        return mVesselObject.getObjectInfo().mID;
    }

    protected ObjectSlot getVesselSlot() {
        return mVesselObject.getObjectSlot();
    }

    protected ObjectInfo getVesselObjectInfo() {
        return mVesselObject.getObjectInfo();
    }

    protected List<SEObject> getVesselChildren() {
        return mVesselObject.getChildObjects();
    }

    protected List<MoveObject> getExistentObjects(boolean reset) {
        if (!reset && mExistentObjects != null) {
            return mExistentObjects;
        }
        mExistentObjects = new ArrayList<MoveObject>();
        for (SEObject child : getVesselChildren()) {
            if (child instanceof NormalObject) {
                MoveObject conflictObject = new MoveObject();
                conflictObject.mMoveObject = (NormalObject) child;
                mExistentObjects.add(conflictObject);
            }
        }
        return mExistentObjects;
    }

    /**
     * 返回所有的可以摆放物体的位置
     */
    protected List<SEVector3f> getAllLocationInVessel() {
        return mVesselObject.getAllLocationForObject(getOnMoveObject());
    }

    protected SETransParas worldToVessel(SETransParas worldTransParas) {
        SETransParas vesselTransParas = new SETransParas();
        vesselTransParas.mScale = worldTransParas.mScale.clone();
        SEVector3f vesselLocation = new SEVector3f(mVesselObject.worldToLocal_JNI(worldTransParas.mTranslate.mD));
        vesselTransParas.mTranslate = vesselLocation;
        return vesselTransParas;
    }

    /**
     * 把物体放置到空位，并且播放一段移动动画
     */
    protected void playSlotAnimation(ObjectSlot moveSlot, SEAnimFinishListener l) {

        getOnMoveObject().changeParent(mVesselObject);
        getOnMoveObject().getObjectSlot().set(moveSlot);
        SETransParas srcLocation = worldToVessel(getOnMoveObject().getUserTransParas());
        SETransParas desLocation = mVesselObject.getTransParasInVessel(getOnMoveObject(), getOnMoveObject()
                .getObjectSlot());
        srcLocation.mRotate = desLocation.mRotate.clone();
        getOnMoveObject().getUserTransParas().set(srcLocation);
        getOnMoveObject().setUserTransParas();

        playMoveAnimation(srcLocation, desLocation, l);
    }

    SEMoveAnimation mMoveAnimation;

    protected void playSlotAnimation(SETransParas srcTransParas, SETransParas desTransParas, SEAnimFinishListener l) {
        playMoveAnimation(srcTransParas, desTransParas, l);
    }

    protected void playMoveAnimation(SETransParas srcTransParas, SETransParas desTransParas, SEAnimFinishListener l) {
        stopMoveAnimation();
        int times = (int) (desTransParas.mTranslate.subtract(srcTransParas.mTranslate).getLength() / 10);
        if (times > 20) {
            times = 20;
        }
        mMoveAnimation = new SEMoveAnimation(getHomeScene(), getMoveObject().mMoveObject, SEVector.AXIS.Z,
                srcTransParas, desTransParas, times);
        mMoveAnimation.adjustAngle();
        mMoveAnimation.setAnimFinishListener(l);
        mMoveAnimation.execute();
    }

    protected void stopMoveAnimation() {
        if (mMoveAnimation != null) {
            mMoveAnimation.stop();
        }
    }

    protected int getVesselCapability() {
        return mVesselObject.getVesselCapability();
    }

    protected enum MOVE_DIRECTION {
        LEFT, RIGHT, DOWN, UP
    }

    protected void cancelConflictTask() {
        if (mConflictTask != null) {
            mConflictTask.cancel();
        }
    }

    protected void performConflictTask(MOVE_DIRECTION moveDirection, long delayOfConflict) {
        mConflictTask = new ConflictTask();
        mConflictTask.execute(moveDirection, delayOfConflict);
    }

    private ConflictTask mConflictTask;

    private class ConflictTask implements Runnable {
        private int mCommandID;
        private boolean mHasBeenCancel;
        private MOVE_DIRECTION mStartDirection;

        public ConflictTask() {
            mCommandID = (int) System.currentTimeMillis();
            mHasBeenCancel = false;
        }

        public void execute(MOVE_DIRECTION moveDirection, long delay) {
            mStartDirection = moveDirection;
            SELoadResThread.getInstance().process(this, delay);
        }

        public void cancel() {
            SELoadResThread.getInstance().cancel(this);
            getHomeScene().removeMessage(mCommandID);
            synchronized (ConflictTask.this) {
                mHasBeenCancel = true;
            }
        }

        public void run() {
            new SECommand(getHomeScene(), mCommandID) {
                public void run() {
                    synchronized (ConflictTask.this) {
                        if (mHasBeenCancel) {
                            return;
                        }
                        onConflictTaskRun(mStartDirection);
                    }
                }
            }.execute();

        }
    }

    protected void onConflictTaskRun(MOVE_DIRECTION mStartDirection) {
    }

    protected void changeExistentObject(NormalObject src, NormalObject des) {
        if (mExistentObjects != null) {
            for (MoveObject existentObject : mExistentObjects) {
                if (existentObject.mMoveObject == src) {
                    existentObject.mMoveObject = des;
                    break;
                }
            }
        }
    }

    protected List<ObjectSlot> getExistentSlots() {
        List<ObjectSlot> existentSlots = new ArrayList<ObjectSlot>();
        for (MoveObject moveObject : getExistentObjects(false)) {
            existentSlots.add(moveObject.getObjectSlot());
        }
        return existentSlots;
    }

    /**
     * @param end
     *            是否停止动画 移动所有有冲突的物体到新的位置，并且播放移动以及震动动画
     */
    protected void playAllConflictObjectsAnimation(boolean end) {
        for (MoveObject moveObject : mExistentObjects) {
            moveObject.performConflictAnimation(mVesselObject, end);
        }
    }

    protected void clearMoveSlot() {
        for (MoveObject moveObject : mExistentObjects) {
            moveObject.mMoveSlot = null;
        }
    }

    protected static SEVector.SEVector2i getNearestEmptySlotAtWall(int sizeX, int sizeY, List<ObjectSlot> moveSlot,
            List<ObjectSlot> existentSlot) {
        boolean[][] fullSlot = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                fullSlot[y][x] = true;
            }
        }
        for (ObjectSlot objectSlot : existentSlot) {
            int startY = objectSlot.mStartY;
            int startX = objectSlot.mStartX;
            float sX = objectSlot.mSpanX;
            float sY = objectSlot.mSpanY;
            for (int y = startY; y < startY + sY; y++) {
                if (y < sizeY) {
                    for (int x = startX; x < startX + sX; x++) {
                        if (x < sizeX) {
                            fullSlot[y][x] = false;
                        }
                    }
                }
            }
        }
        Rect rect = getRectOfMoveSlots(moveSlot);
        int rectW = rect.width();
        int rectH = rect.height();
        boolean[][] rectSlot = new boolean[rectH][rectW];
        for (int y = 0; y < rectH; y++) {
            for (int x = 0; x < rectW; x++) {
                rectSlot[y][x] = true;
            }
        }
        for (ObjectSlot objectSlot : moveSlot) {
            int startY = objectSlot.mStartY - rect.top;
            int startX = objectSlot.mStartX - rect.left;
            float sX = objectSlot.mSpanX;
            float sY = objectSlot.mSpanY;
            for (int y = startY; y < startY + sY; y++) {
                if (y < rectH) {
                    for (int x = startX; x < startX + sX; x++) {
                        if (x < rectW) {
                            rectSlot[y][x] = false;
                        }
                    }
                }
            }
        }
        int moveMinX = 0;
        int moveMaxX = sizeX - rectW;
        int moveMinY = 0;
        int moveMaxY = sizeY - rectH;
        List<SEVector.SEVector2i> objectSlots = new ArrayList<SEVector.SEVector2i>();
        for (int x = moveMinX; x <= moveMaxX; x++) {
            for (int y = moveMinY; y <= moveMaxY; y++) {
                boolean hasSlot = true;
                for (int i = 0; i < rectW; i++) {
                    for (int j = 0; j < rectH; j++) {
                        if (!rectSlot[j][i] && !fullSlot[j + y][i + x]) {
                            hasSlot = false;
                            break;
                        }
                    }
                    if (!hasSlot) {
                        break;
                    }
                }
                if (hasSlot) {
                    SEVector.SEVector2i slot = new SEVector.SEVector2i(x, y);
                    objectSlots.add(slot);
                }
            }
        }
        SEVector.SEVector2i nearestSlot = null;
        if (objectSlots.size() > 0) {
            SEVector.SEVector2i srcPosition = new SEVector.SEVector2i(rect.left, rect.top);
            float distance = Float.MAX_VALUE;
            for (SEVector.SEVector2i desPosition : objectSlots) {
                float curDistance = desPosition.subtract(srcPosition).getLength();
                if (curDistance < distance) {
                    distance = curDistance;
                    nearestSlot = desPosition;
                }
            }
        }
        if (nearestSlot != null) {
            nearestSlot = new SEVector.SEVector2i(nearestSlot.getX() - rect.left, nearestSlot.getY() - rect.top);
        }
        return nearestSlot;
    }

    private static Rect getRectOfMoveSlots(List<ObjectSlot> moveSlots) {
        Rect rect = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (ObjectSlot slot : moveSlots) {
            if (slot.mStartX < rect.left) {
                rect.left = slot.mStartX;
            }
            if (slot.mStartY < rect.top) {
                rect.top = slot.mStartY;
            }
            if (slot.mStartX + slot.mSpanX > rect.right) {
                rect.right = slot.mStartX + slot.mSpanX;
            }
            if (slot.mStartY + slot.mSpanY > rect.bottom) {
                rect.bottom = slot.mStartY + slot.mSpanY;
            }
        }
        return rect;
    }

    protected boolean isMovingObject(NormalObject movingObject) {
        return true;
    }

    protected VesselLayer getValidConflictVesselLayer() {
        return getMoveObject().getValidConflictVesselLayer(mExistentObjects);
    }

    protected List<ObjectSlot> getExistingObjectSlots() {
        return getMoveObject().getExistingObjectSlots(mExistentObjects);
    }

    protected Folder CreateFolderAtThePositionOfAppIcon(AppObject appObject) {
        appObject.hideBackground();
        ObjectInfo objInfo = new ObjectInfo();
        objInfo.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        objInfo.mType = "Folder";
        objInfo.mName = "app_folder";
        objInfo.mIndex = (int) System.currentTimeMillis();
        objInfo.mSceneName = getHomeScene().getSceneName();
        NormalObject parent = (NormalObject) appObject.getParent();
        objInfo.mObjectSlot.set(appObject.getObjectSlot());
        Folder folder = new Folder(getHomeScene(), objInfo.mName, objInfo.mIndex);
        folder.setObjectInfo(objInfo);
        parent.addChild(folder, true);
        folder.initStatus();
        appObject.changeParent(folder);
        appObject.getObjectSlot().mSlotIndex = 0;
        appObject.setVisible(false);
        folder.updateFolderCover();
        return folder;
    }

    public static class MoveObject {
        public NormalObject mMoveObject;
        public ObjectSlot mMoveSlot;
        public SETranslateAnimation mConflictAnimation;
        public SENeverStopAnimation mShakeAnimation;

        public ObjectInfo getObjectInfo() {
            return mMoveObject.getObjectInfo();
        }

        public ObjectSlot getObjectSlot() {
            return mMoveObject.getObjectSlot();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            MoveObject cmp = (MoveObject) obj;
            return (mMoveObject.equals(cmp.mMoveObject));
        }

        private void performNeverStopAnimation(final SEVector3f moveDirect) {
            if (null == moveDirect)
                return;
            final SEVector3f origin = mMoveObject.getLocalScale();
            mShakeAnimation = new SENeverStopAnimation(mMoveObject.getHomeScene(), 40) {
                @Override
                public void onAnimationRun(float value) {
                    float scale = 1 - value * 0.036f;
                    SEVector3f localScale = origin.clone().selfMul(scale);
                    mMoveObject.setLocalScale(localScale);
                    SEVector3f location = moveDirect.mul(value * mMoveObject.getHomeScene().getHouse().mCellWidth
                            * 0.036f);
                    mMoveObject.setLocalTranslate(location);
                }

                @Override
                public void onFinish() {
                    mMoveObject.setLocalScale(origin);
                    mMoveObject.setLocalTranslate(new SEVector3f(0, 0, 0));
                }

            };
            mShakeAnimation.setInterpolator(new CycleInterpolator(1));
            mShakeAnimation.execute();
        }

        public void performConflictAnimation(VesselObject vessel, boolean end) {
            if (mConflictAnimation != null) {
                mConflictAnimation.stop();
            }
            if (mShakeAnimation != null) {
                mShakeAnimation.stop();
            }

            if (mMoveSlot == null) {
                NormalObject object = mMoveObject;
                SEVector3f srcLocation = object.getUserTranslate();
                SETransParas desTransParas = vessel.getTransParasInVessel(object, getObjectSlot());
                SEVector3f desLocation = desTransParas.mTranslate;
                if (!srcLocation.equals(desLocation)) {
                    int animationFrames = (int) (desLocation.subtract(srcLocation).getLength() / 50);
                    if (animationFrames == 0) {
                        animationFrames = 1;
                    }
                    mConflictAnimation = new SETranslateAnimation(mMoveObject.getHomeScene(), object, srcLocation,
                            desLocation, animationFrames);
                    mConflictAnimation.execute();
                }
                return;
            }

            final NormalObject object = mMoveObject;
            SEVector3f desLocation = vessel.getTransParasInVessel(object, mMoveSlot).mTranslate;
            SEVector3f fromLocation = vessel.getTransParasInVessel(object, getObjectSlot()).mTranslate;
            SEVector3f srcLocation = object.getUserTranslate();
            final SEVector3f moveDirect = desLocation.subtract(fromLocation).normalize();
            int animationFrames = (int) (desLocation.subtract(srcLocation).getLength() / 50);
            if (animationFrames == 0) {
                animationFrames = 1;
            }

            mConflictAnimation = new SETranslateAnimation(mMoveObject.getHomeScene(), object, srcLocation, desLocation,
                    animationFrames);
            if (!end) {
                mConflictAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                    public void onAnimationfinish() {
                        performNeverStopAnimation(moveDirect);
                    }
                });
            } else {
                mMoveObject.updateSlotInfo(mMoveSlot);
            }
            mConflictAnimation.execute();
        }

        private List<MoveObject> mConflictObjects;

        protected VesselLayer getValidConflictVesselLayer(List<MoveObject> mExistentObjects) {
            mConflictObjects = calculateConflictObjects(mExistentObjects, mMoveSlot);
            if (mConflictObjects != null && mConflictObjects.size() == 1) {
                if (mConflictObjects.get(0).mMoveObject instanceof VesselObject) {
                    VesselObject vesselObject = (VesselObject) mConflictObjects.get(0).mMoveObject;
                    VesselLayer vesselLayer = vesselObject.getVesselLayer();
                    if (vesselLayer.canHandleSlot(mMoveObject, 0, 0)) {
                        return vesselLayer;
                    }
                }
            }
            return null;
        }

        protected List<MoveObject> calculateConflictObjects(List<MoveObject> existentObjs, ObjectSlot cmpSlot) {
            List<MoveObject> conflictSlots = null;
            for (MoveObject normalObject : existentObjs) {
                if (normalObject.getObjectSlot().isConflict(cmpSlot)) {
                    if (conflictSlots == null) {
                        conflictSlots = new ArrayList<MoveObject>();
                    }
                    conflictSlots.add(normalObject);
                }

            }
            return conflictSlots;
        }

        protected List<MoveObject> calculateConflictObjects(List<MoveObject> existentObjs,
                List<MoveObject> movingObjects, int moveX, int moveY) {
            List<MoveObject> conflictSlots = null;
            for (MoveObject normalObject : existentObjs) {
                for (MoveObject cmpObject : movingObjects) {

                    ObjectSlot cmpSlot = cmpObject.getObjectSlot().clone();
                    cmpSlot.mStartX += moveX;
                    cmpSlot.mStartY += moveY;
                    if (normalObject.getObjectSlot().isConflict(cmpSlot)) {
                        if (conflictSlots == null) {
                            conflictSlots = new ArrayList<MoveObject>();
                        }
                        conflictSlots.add(normalObject);
                        break;
                    }
                }

            }
            return conflictSlots;
        }

        protected List<MoveObject> copeConflictObject() {
            List<MoveObject> fillSlots = new ArrayList<MoveObject>();
            fillSlots.addAll(mConflictObjects);
            return fillSlots;
        }

        protected List<ObjectSlot> getExistingObjectSlots(List<MoveObject> objectList) {
            List<ObjectSlot> existentSlot = new ArrayList<ObjectSlot>();
            existentSlot.add(mMoveSlot);
            for (MoveObject moveObject : objectList) {
                if (!mConflictObjects.contains(moveObject)) {
                    existentSlot.add(moveObject.getObjectSlot());
                }
            }

            return existentSlot;
        }

        protected List<ObjectSlot> getConflictObjectSlots() {
            List<ObjectSlot> moveSlot = new ArrayList<ObjectSlot>();
            for (MoveObject moveObject : mConflictObjects) {
                moveSlot.add(moveObject.getObjectSlot());
            }
            return moveSlot;
        }

        protected boolean hasConflictObject() {
            return mConflictObjects != null;
        }

        protected MoveObject getFirstConflictObject() {
            if (null == mConflictObjects || mConflictObjects.isEmpty())
                return null;
            return mConflictObjects.get(0);
        }

        protected boolean placeConflictObjects(SEVector.SEVector2i nearestSlot) {
            if (nearestSlot != null) {
                for (MoveObject conflictObject : mConflictObjects) {
                    conflictObject.mMoveSlot = conflictObject.getObjectSlot().clone();
                    conflictObject.mMoveSlot.mStartX = nearestSlot.getX() + conflictObject.mMoveSlot.mStartX;
                    conflictObject.mMoveSlot.mStartY = nearestSlot.getY() + conflictObject.mMoveSlot.mStartY;
                }
                return true;
            }

            return false;
        }
    }

}
