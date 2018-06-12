package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.*;
import com.borqs.se.engine.SEVector.SEVector2i;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.UpdateDBThread;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class WallCellLayer extends VesselLayer {
    private int mWallIndex;
    private MOVE_DIRECTION mMoveDirection;
    private MOVE_DIRECTION mPreConflictDirection;
    private boolean mHasChangedSlot;
    private VesselLayer mCurrentLayer;
    private VesselLayer mCandidateLayer;
    private SEVector3f mMoveLocation;
    private House mHouse;

    public WallCellLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mWallIndex = vesselObject.getObjectSlot().mSlotIndex;
        mHouse = (House) vesselObject.getParent();
    }

    public int getWallIndex() {
        return mWallIndex;
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        return object.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_WALL
                || object.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_APP_WALL
                || object.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            getOnMoveObject().changeSlotType(ObjectInfo.SLOT_TYPE_WALL);
            mCurrentLayer = null;
            mCandidateLayer = null;
            mHasChangedSlot = false;
        } else {
            cancelConflictTask();
            disableCurrentLayer();
            clearMoveSlot();
            playAllConflictObjectsAnimation(false);
        }
        return true;
    }

    public boolean onObjectMoveEvent(ACTION event, SEVector3f location) {
        stopMoveAnimation();
        mMoveLocation = location;
        // 获取物体在墙面的最近的slot
        ObjectSlot objectSlot = calculateNearestSlot(location);
        // 判断物体slot是否已经改变
        boolean mSlotChanged = !cmpSlot(objectSlot, getMoveObject().mMoveSlot);
        // 假如物体的slot已经改变或者Move事件刚开始发生，获取和该物体有冲突的物体
        if (event == ACTION.BEGIN || mSlotChanged) {
            mHasChangedSlot = true;
            getMoveObject().mMoveSlot = objectSlot;
            cancelConflictTask();
            mCandidateLayer = null;
            //mConflictObjects = calculateConflictObjects(getExistentObjects(false), getMoveObject().mMoveSlot);
            //假如有冲突的物体是容器且Move事件刚开始发生，那么切换VesselLayer，否则等待300ms再切换mCandidateLayer
            validateCurrentLayer(event == ACTION.BEGIN);
        }
        // 抬手的时候，假如mCandidateLayer不为空那么直接切换到mCandidateLayer
        if (event == ACTION.FINISH && mCandidateLayer != null) {
            cancelConflictTask();
            mCandidateLayer.setOnLayerModel(getOnMoveObject(), true);
            mCurrentLayer = mCandidateLayer;
            mCandidateLayer = null;
        }
        if (mCurrentLayer != null) {
            return mCurrentLayer.onObjectMoveEvent(event, location);
        }
        getOnMoveObject().changeSlotType(ObjectInfo.SLOT_TYPE_WALL);
        long delayOfConflict = 300;
        switch (event) {
        case BEGIN:
            delayOfConflict = 1000;
        case MOVE:
        case UP:
        case FLY:
            // 假如物体slot有变化且有冲突的物体，那么隔300ms再把有冲突的物体移动到其它的空位置
            // 假如物体slot有变化但是没有有冲突的物体，那么通过播放动画来复位所有的物体
            if (mHasChangedSlot) {
                if (hasConflictObject()) {
                    mPreConflictDirection = mMoveDirection;
                    performConflictTask(mMoveDirection, delayOfConflict);
                } else {
                    clearMoveSlot();
                    playAllConflictObjectsAnimation(false);
                }
            }
            mHasChangedSlot = false;
            break;
        case FINISH:
            cancelConflictTask();
            MOVE_DIRECTION movDirection = (mHasChangedSlot || mPreConflictDirection == null) ? mMoveDirection
                    : mPreConflictDirection;
            //抬手的时候假如没有有冲突的物体那么直接把物体放下
            //假如有冲突的物体，那么把有冲突的物体移动到其它的空位置
            slotToWall(movDirection);
            mHasChangedSlot = false;

            break;
        }
        return true;
    }

    private void disableCurrentLayer() {
        if (mCurrentLayer != null) {
            if (mCurrentLayer instanceof FolderLayer) {
                final Folder folder = (Folder) mCurrentLayer.getVesselObject();
                if (folder.getChildObjects().size() == 1) {
                    NormalObject icon = folder.changeToAppIcon();
                    changeExistentObject(folder, icon);
                } else if (folder.getChildObjects().size() == 2 && folder.getObjectSlot().mVesselID == -1) {
                    folder.getObjectSlot().mVesselID = getVesselId();
                    folder.getObjectInfo().saveToDB();
                    for (SEObject child : folder.getChildObjects()) {
                        final NormalObject icon = (NormalObject) child;
                        final int index = folder.getChildObjects().indexOf(child);
                        UpdateDBThread.getInstance().process(new Runnable() {
                            public void run() {
                                icon.getObjectSlot().mVesselID = folder.getObjectInfo().mID;
                                icon.getObjectSlot().mSlotIndex = index;
                                icon.getObjectInfo().updateSlotDB();
                            }
                        });
                    }
                }
            }
            mCurrentLayer.setOnLayerModel(getOnMoveObject(), false);
            mCurrentLayer = null;
        }
    }

    protected void onConflictTaskRun(MOVE_DIRECTION mStartDirection) {
        MoveObject firstConflict = getFirstConflictObject();
        if (null == firstConflict) {
            return;
        }

        ObjectSlot layerSlot = firstConflict.mMoveSlot;
        if (layerSlot == null) {
            layerSlot = firstConflict.mMoveObject.getObjectSlot();
        }
        if (mCandidateLayer != null && isMoveInCenter(layerSlot)) {
            mCandidateLayer.setOnLayerModel(getOnMoveObject(), true);
            disableCurrentLayer();
            mCurrentLayer = mCandidateLayer;
            mCandidateLayer = null;
            clearMoveSlot();
            playAllConflictObjectsAnimation(true);
            return;
        } else if ((getOnMoveObject() instanceof AppObject)
                && (firstConflict.mMoveObject instanceof AppObject)
                && isMoveInCenter(layerSlot)) {
            AppObject appObject = (AppObject) firstConflict.mMoveObject;
            Folder folder = CreateFolderAtThePositionOfAppIcon(appObject);
            changeExistentObject(appObject, folder);
            mCandidateLayer = folder.getVesselLayer();
            mCandidateLayer.setOnLayerModel(getOnMoveObject(), true);
            disableCurrentLayer();
            mCurrentLayer = mCandidateLayer;
            mCandidateLayer = null;
            clearMoveSlot();
            playAllConflictObjectsAnimation(true);
            return;
        }

        mCandidateLayer = null;
        if (!canPlaceToWall(mStartDirection)) {
            clearMoveSlot();
        }
        playAllConflictObjectsAnimation(false);
    }

    private Rect getRectOfconflictObjects(List<MoveObject> conflictObjects) {
        Rect rect = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (MoveObject moveObject : conflictObjects) {
            ObjectSlot slot = moveObject.getObjectSlot();
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

    private boolean canPlaceToWall(MOVE_DIRECTION moveDirection) {
        MOVE_DIRECTION nextDirection =  moveDirection;
        boolean canPlace = canPlaceToDirection(copeExistentObject(), getMoveObject(), null, nextDirection);
      
        if (!canPlace) {
            nextDirection = getNextHorizontalPriorityDirection(nextDirection);
        } else {
            return true;
        }

        canPlace = canPlaceToDirection(copeExistentObject(), getMoveObject(), null, nextDirection);
        if (!canPlace) {
            nextDirection = getNextVerticalPriorityDirection(nextDirection);
        } else {
            return true;
        }

        canPlace = canPlaceToDirection(copeExistentObject(), getMoveObject(), null, nextDirection);
        if (!canPlace) {
            nextDirection = getNextHorizontalPriorityDirection(nextDirection);
        } else {
            return true;
        }

        canPlace = canPlaceToDirection(copeExistentObject(), getMoveObject(), null, nextDirection);

        // if could not place conflict object at above method, try to place them
        // to their nearest empty place
        if (!canPlace) {
            List<ObjectSlot> moveSlot = getConflictObjectSlots();
            List<ObjectSlot> existentSlot = getExistingObjectSlots();
            SEVector2i nearestSlot = getNearestEmptySlotAtWall(mHouse.mCellCountX, mHouse.mCellCountY, moveSlot,
                    existentSlot);
            clearMoveSlot();
            return placeConflictObjects(nearestSlot);
        } else {
            return true;
        }
    }

    private boolean canPlaceToDirection(List<MoveObject> existentObjects, MoveObject moveObject,
                                            boolean[][] slot, MOVE_DIRECTION direction) {

        List<MoveObject> conflictObjects = copeConflictObject();
        clearMoveSlot();
        return  canPlaceToDirection(existentObjects, moveObject, conflictObjects, slot, direction);
    }

    private boolean canPlaceToDirection(List<MoveObject> existentObjects, MoveObject moveObject,
                                        List<MoveObject> conflictObjects, boolean[][] slot, MOVE_DIRECTION direction) {
        int wallSX = mHouse.mCellCountX;
        int wallSY = mHouse.mCellCountY;
        int startY = moveObject.mMoveSlot.mStartY;
        int startX = moveObject.mMoveSlot.mStartX;
        int sX = moveObject.mMoveSlot.mSpanX;
        int sY = moveObject.mMoveSlot.mSpanY;
        if (slot == null) {
            slot = new boolean[wallSY][wallSX];
            for (int y = 0; y < wallSY; y++) {
                for (int x = 0; x < wallSX; x++) {
                    slot[y][x] = true;
                }
            }

            if (existentObjects.contains(moveObject)) {
                existentObjects.remove(moveObject);
            }
            existentObjects.removeAll(conflictObjects);

            for (int y = startY; y < startY + sY; y++) {
                for (int x = startX; x < startX + sX; x++) {
                    slot[y][x] = false;
                }
            }
        }

        Rect rect = getRectOfconflictObjects(conflictObjects);
        int moveX = 0;
        int moveY = 0;
        if (direction == MOVE_DIRECTION.LEFT) {
            moveX = startX - rect.right;
            moveY = 0;
        } else if (direction == MOVE_DIRECTION.RIGHT) {
            moveX = startX + sX - rect.left;
            moveY = 0;
        } else if (direction == MOVE_DIRECTION.UP) {
            moveX = 0;
            moveY = startY - rect.bottom;
        } else if (direction == MOVE_DIRECTION.DOWN) {
            moveX = 0;
            moveY = startY + sY - rect.top;
        }
        if (rect.left + moveX < 0 || rect.right + moveX > wallSX || rect.top + moveY < 0
                || rect.bottom + moveY > wallSY) {
            return false;
        }

        List<MoveObject> newConflictObjs = calculateConflictObjects(getMoveObject(),
                existentObjects, conflictObjects, moveX, moveY);

        if (newConflictObjs == null) {
            for (MoveObject conflictObject : conflictObjects) {
                conflictObject.mMoveSlot = conflictObject.getObjectSlot().clone();
                conflictObject.mMoveSlot.mStartX = conflictObject.mMoveSlot.mStartX + moveX;
                conflictObject.mMoveSlot.mStartY = conflictObject.mMoveSlot.mStartY + moveY;
            }
            return true;
        } else {
            existentObjects.removeAll(newConflictObjs);
            conflictObjects.addAll(newConflictObjs);
            return canPlaceToDirection(existentObjects, moveObject, conflictObjects, slot, direction);
        }

    }

    private List<MoveObject> calculateConflictObjects(List<MoveObject> existentObjs, ObjectSlot cmpSlot) {
        List<MoveObject> conflictSlots = null;
        for (MoveObject normalObject : existentObjs) {
            ObjectSlot objectSlot = normalObject.getObjectInfo().mObjectSlot;
            boolean xConflict = !((cmpSlot.mStartX <= objectSlot.mStartX && (cmpSlot.mStartX + cmpSlot.mSpanX) <= objectSlot.mStartX) || (cmpSlot.mStartX >= (objectSlot.mStartX + objectSlot.mSpanX) && (cmpSlot.mStartX + cmpSlot.mSpanX) >= (objectSlot.mStartX + objectSlot.mSpanX)));
            boolean YConflict = !((cmpSlot.mStartY <= objectSlot.mStartY && (cmpSlot.mStartY + cmpSlot.mSpanY) <= objectSlot.mStartY) || (cmpSlot.mStartY >= (objectSlot.mStartY + objectSlot.mSpanY) && (cmpSlot.mStartY + cmpSlot.mSpanY) >= (objectSlot.mStartY + objectSlot.mSpanY)));
            if (xConflict && YConflict) {
                if (conflictSlots == null) {
                    conflictSlots = new ArrayList<MoveObject>();
                }
                conflictSlots.add(normalObject);
            }

        }
        return conflictSlots;
    }

    private ObjectSlot calculateNearestSlot(SEVector3f location) {
        ObjectSlot slot = getOnMoveObject().getObjectSlot().clone();
        slot.mSlotIndex = mWallIndex;
        slot.mVesselID = getVesselId();
        float wallZ = mHouse.mWallHeight / 2;
        float centerX = (mHouse.mWallPaddingLeft - mHouse.mWallPaddingRight) / 2;
        float centerZ = wallZ + (mHouse.mWallPaddingBottom - mHouse.mWallPaddingTop) / 2;

        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        SEVector3f start = new SEVector3f();
        start.mD[0] = location.getX() - slot.mSpanX * gridSizeX / 2f;
        start.mD[2] = location.getZ() + slot.mSpanY * gridSizeY / 2f;
        float convertStartX = (start.getX() - centerX) / gridSizeX + mHouse.mCellCountX / 2f;
        float convertStartY = mHouse.mCellCountY / 2f - (start.getZ() - centerZ) / gridSizeY;

        slot.mStartX = Math.round(convertStartX);
        if (slot.mStartX < 0) {
            slot.mStartX = 0;
        } else if (slot.mStartX > mHouse.mCellCountX - slot.mSpanX) {
            slot.mStartX = mHouse.mCellCountX - slot.mSpanX;
        }
        slot.mStartY = Math.round(convertStartY);
        if (slot.mStartY < 0) {
            slot.mStartY = 0;
        } else if (slot.mStartY > mHouse.mCellCountY - slot.mSpanY) {
            slot.mStartY = mHouse.mCellCountY - slot.mSpanY;
        }
        float dX = convertStartX - slot.mStartX;
        float dY = convertStartY - slot.mStartY;
        if (Math.abs(dY) >= Math.abs(dX)) {
            if (dY >= 0) {
                mMoveDirection = MOVE_DIRECTION.UP;
            } else {
                mMoveDirection = MOVE_DIRECTION.DOWN;
            }
        } else {
            if (dX >= 0) {
                mMoveDirection = MOVE_DIRECTION.LEFT;
            } else {
                mMoveDirection = MOVE_DIRECTION.RIGHT;
            }
        }
        return slot;
    }

    private boolean isMoveInCenter(ObjectSlot conflictSlot) {
        ObjectSlot slot = getOnMoveObject().getObjectSlot();
        float wallZ = mHouse.mWallHeight / 2;
        float centerX = (mHouse.mWallPaddingLeft - mHouse.mWallPaddingRight) / 2;
        float centerZ = wallZ + (mHouse.mWallPaddingBottom - mHouse.mWallPaddingTop) / 2;

        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        SEVector3f start = new SEVector3f();
        start.mD[0] = mMoveLocation.getX() - slot.mSpanX * gridSizeX / 2f;
        start.mD[2] = mMoveLocation.getZ() + slot.mSpanY * gridSizeY / 2f;
        float convertStartX = (start.getX() - centerX) / gridSizeX + mHouse.mCellCountX / 2f;
        float convertStartY = mHouse.mCellCountY / 2f - (start.getZ() - centerZ) / gridSizeY;

        float mixX = conflictSlot.mStartX - 0.32f;
        float maxX = conflictSlot.mStartX + conflictSlot.mSpanX - slot.mSpanX + 0.32f;

        float mixY = conflictSlot.mStartY - 0.32f;
        float maxY = conflictSlot.mStartY + conflictSlot.mSpanY - slot.mSpanY + 0.32f;
        return (convertStartX > mixX && convertStartX < maxX) && (convertStartY > mixY && convertStartY < maxY);
    }

    @Override
    public void handleOutsideRoom() {
        getOnMoveObject().handleOutsideRoom();
    }

    @Override
    public void handleNoMoreRoom() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                Toast.makeText(HomeManager.getInstance().getContext(), R.string.no_room, Toast.LENGTH_SHORT).show();
            }
        });
        getOnMoveObject().handleNoMoreRoom();
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();
        getOnMoveObject().placeInVesselSlot(getVesselSlot().mSlotIndex);
    }

    @Override
    public boolean placeObjectToVessel(NormalObject normalObject) {
        super.placeObjectToVessel(normalObject);
        List<MoveObject> existentObjects = getExistentObjects(true);
        List<ObjectSlot> existentSlot = new ArrayList<ObjectSlot>();
        for (MoveObject moveObject : existentObjects) {
            existentSlot.add(moveObject.getObjectSlot());
        }
        SEVector2i nearestSlot = getTheBestEmptySlotAtWall(normalObject.getObjectSlot().mSpanX,
                normalObject.getObjectSlot().mSpanY, existentSlot);
        if (nearestSlot != null) {
            int wallIndex = getVesselSlot().mSlotIndex;
            normalObject.getObjectSlot().mVesselID = getVesselId();
            normalObject.getObjectSlot().mSlotIndex = wallIndex;
            ObjectSlot moveSlot = normalObject.getObjectSlot().clone();
            moveSlot.mStartX = nearestSlot.getX();
            moveSlot.mStartY = nearestSlot.getY();

            normalObject.updateSlotInfo(moveSlot);
            placeObjectToVesselDirectly(normalObject);

            return true;
        } else {
            normalObject.getParent().removeChild(normalObject, true);
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    Toast.makeText(HomeManager.getInstance().getContext(), R.string.no_room, Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    @Override
    protected boolean placeObjectToVesselDirectly(NormalObject normalObject) {
        normalObject.changeSlotType(ObjectInfo.SLOT_TYPE_WALL);
        return super.placeObjectToVesselDirectly(normalObject);
    }

    private void slotToWall(MOVE_DIRECTION moveDirection) {
        ObjectSlot placeSlot = null;
        if (!calculateConflictObjects()) {
            placeSlot = getMoveObject().mMoveSlot;
            clearMoveSlot();
            playAllConflictObjectsAnimation(true);
        } else {
            if (canPlaceToWall(moveDirection)) {
                playAllConflictObjectsAnimation(true);
                placeSlot = getMoveObject().mMoveSlot;
            } else {
                clearMoveSlot();
                playAllConflictObjectsAnimation(true);
                if (getOnMoveObject().getObjectSlot().mSlotIndex == mWallIndex) {
                    placeSlot = getOnMoveObject().getObjectSlot();
                } else {
                    List<ObjectSlot> moveSlot = new ArrayList<ObjectSlot>();
                    moveSlot.add(getMoveObject().mMoveSlot);
                    SEVector2i nearestSlot = getNearestEmptySlotAtWall(mHouse.mCellCountX, mHouse.mCellCountY,
                            moveSlot, getExistentSlots());
                    if (nearestSlot != null) {
                        placeSlot = getMoveObject().mMoveSlot.clone();
                        placeSlot.mStartX = nearestSlot.getX() + placeSlot.mStartX;
                        placeSlot.mStartY = nearestSlot.getY() + placeSlot.mStartY;
                        placeSlot.mVesselID = getVesselId();
                    }
                    if (placeSlot == null) {
                        placeSlot = getOnMoveObject().getObjectSlot();
                    }
                }
            }
        }
        if (placeSlot.mVesselID < 0) {
            getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
            handleNoMoreRoom();
        } else if (placeSlot.mVesselID != getVesselId()) {
            if (!getOnMoveObject().resetPostion()) {
                getOnMoveObject().getParent().removeChild(getOnMoveObject(), true);
            }
            handleNoMoreRoom();
        } else {
            playSlotAnimation(placeSlot, new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    handleSlotSuccess();
                }
            });
        }
    }

    private SEVector2i getTheBestEmptySlotAtWall(int spanX, int spanY, List<ObjectSlot> existentSlot) {
        int sizeX = mHouse.mCellCountX;
        int sizeY = mHouse.mCellCountY;
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
        int rectW = spanX;
        int rectH = spanY;
        boolean[][] rectSlot = new boolean[rectH][rectW];
        for (int y = 0; y < rectH; y++) {
            for (int x = 0; x < rectW; x++) {
                rectSlot[y][x] = false;
            }
        }
        int moveMinX = 0;
        int moveMaxX = sizeX - rectW;
        int moveMinY = 0;
        int moveMaxY = sizeY - rectH;
        SEVector2i theBestSlot = null;
        for (int y = moveMinY; y <= moveMaxY; y++) {
            for (int x = moveMinX; x <= moveMaxX; x++) {
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
                    theBestSlot = new SEVector2i(x, y);
                    return theBestSlot;
                }
            }
        }
        return theBestSlot;
    }


    /// conflict object management begin
    /// moving to Move object in MoveObject class
//    private List<MoveObject> mConflictObjects;

    private List<MoveObject> copeConflictObject() {
        return getMoveObject().copeConflictObject();
//        List<MoveObject> fillSlots = new ArrayList<MoveObject>();
//        fillSlots.addAll(mConflictObjects);
//        return fillSlots;
    }

//    private List<ObjectSlot> getExistingObjectSlots() {
//        /*List<ObjectSlot> existentSlot = new ArrayList<ObjectSlot>();
//        existentSlot.add(getMoveObject().mMoveSlot);
//        for (MoveObject moveObject : getExistentObjects(false)) {
//            if (!mConflictObjects.contains(moveObject)) {
//                existentSlot.add(moveObject.getObjectSlot());
//            }
//        }
//
//        return existentSlot;*/
//        return getMoveObject().getExistingObjectSlots(mExistentObjects);
////        List<ObjectSlot> existentSlot = new ArrayList<ObjectSlot>();
////        existentSlot.add(getMoveObject().mMoveSlot);
////        for (MoveObject moveObject : mExistentObjects) {
////            if (!mConflictObjects.contains(moveObject)) {
////                existentSlot.add(moveObject.getObjectSlot());
////            }
////        }
////
////        return existentSlot;
//    }

    private List<ObjectSlot> getConflictObjectSlots() {
        return getMoveObject().getConflictObjectSlots();
//        List<ObjectSlot> moveSlot = new ArrayList<ObjectSlot>();
//        for (MoveObject moveObject : mConflictObjects) {
//            moveSlot.add(moveObject.getObjectSlot());
//        }
//        return moveSlot;
    }

    private boolean hasConflictObject() {
        return getMoveObject().hasConflictObject();
//        return mConflictObjects != null;
    }

    private boolean calculateConflictObjects() {
        calculateConflictObjects(getExistentObjects(false), getMoveObject().mMoveSlot);
        return hasConflictObject();
    }

    private MoveObject getFirstConflictObject() {
        return getMoveObject().getFirstConflictObject();
//        if (null == mConflictObjects || mConflictObjects.isEmpty()) return null;
//        return mConflictObjects.get(0);
    }

    private void validateCurrentLayer(boolean begin) {
        VesselLayer vesselLayer = getValidConflictVesselLayer();
        if (vesselLayer == null) {
            disableCurrentLayer();
        } else if (vesselLayer != mCurrentLayer) {
            disableCurrentLayer();
            mCandidateLayer = vesselLayer;
            if (begin) {
                mCandidateLayer.setOnLayerModel(getOnMoveObject(), true);
                mCurrentLayer = mCandidateLayer;
                mCandidateLayer = null;
            }
        }
    }

    private boolean placeConflictObjects(SEVector2i nearestSlot) {
        return getMoveObject().placeConflictObjects(nearestSlot);
//        if (nearestSlot != null) {
//            for (MoveObject conflictObject : mConflictObjects) {
//                conflictObject.mMoveSlot = conflictObject.getObjectSlot().clone();
//                conflictObject.mMoveSlot.mStartX = nearestSlot.getX() + conflictObject.mMoveSlot.mStartX;
//                conflictObject.mMoveSlot.mStartY = nearestSlot.getY() + conflictObject.mMoveSlot.mStartY;
//            }
//            return true;
//        }
//
//        return false;
    }

    private static List<MoveObject> calculateConflictObjects(MoveObject moveObject,
                                                             List<MoveObject> existentObjs,
                                                             List<MoveObject> conflictObjects,
                                                             int moveX, int moveY) {
        return moveObject.calculateConflictObjects(existentObjs, conflictObjects, moveX, moveY);
    }
    /// conflict object management end

    /// iterate from current moveDirection to next direction in clockwise in
    /// horizontal prior to vertical iteration.
    ///       1
    ///  ^-------->
    ///  |        |
    /// 4|        |3
    ///  |        |
    ///  <--------V
    ///       2
    MOVE_DIRECTION getNextHorizontalPriorityDirection(MOVE_DIRECTION moveDirection) {
        MOVE_DIRECTION nextDirection;
        if (moveDirection == MOVE_DIRECTION.LEFT) {
            nextDirection = MOVE_DIRECTION.RIGHT;
        } else if (moveDirection == MOVE_DIRECTION.RIGHT) {
            nextDirection = MOVE_DIRECTION.LEFT;
        } else if (moveDirection == MOVE_DIRECTION.UP) {
            nextDirection = MOVE_DIRECTION.DOWN;
        } else {
            nextDirection = MOVE_DIRECTION.UP;
        }
        return nextDirection;
    }
    /// iterate from current moveDirection to next direction in clockwise in
    /// vertical prior to horizontal iteration.
    ///       3
    ///  ^-------->
    ///  |        |
    /// 1|        |2
    ///  |        |
    ///  <--------V
    ///       4
    MOVE_DIRECTION getNextVerticalPriorityDirection(MOVE_DIRECTION moveDirection) {
        MOVE_DIRECTION nextDirection;
        if (moveDirection == MOVE_DIRECTION.LEFT) {
            nextDirection = MOVE_DIRECTION.UP;
        } else if (moveDirection == MOVE_DIRECTION.RIGHT) {
            nextDirection = MOVE_DIRECTION.DOWN;
        } else if (moveDirection == MOVE_DIRECTION.UP) {
            nextDirection = MOVE_DIRECTION.RIGHT;
        } else {
            nextDirection = MOVE_DIRECTION.LEFT;
        }
        return nextDirection;
    }

  
       /// existing object management begin
    private List<MoveObject> copeExistentObject() {
        List<MoveObject> fillSlots = new ArrayList<MoveObject>();
        fillSlots.addAll(getExistentObjects(false));
        return fillSlots;
    }
}
