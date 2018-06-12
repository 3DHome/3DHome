package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;
import com.borqs.se.engine.SEMoveAnimation;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class BookshelfLayer extends VesselLayer {
    private List<ConflictObject> mExistentSlot;
    private ObjectSlot mObjectSlot;
    private ConflictObject mConflictSlot;
    private List<SEVector3f> mAllLocationOnAppWall;

    public BookshelfLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        if (object instanceof AppObject) {
            mExistentSlot = getExistentSlot();
            return searchEmptySlot(mExistentSlot) != null;
        }
        return false;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            onMoveObject.changeSlotType(ObjectInfo.SLOT_TYPE_BOOKSHELF);
            mExistentSlot = getExistentSlot();
            mObjectSlot = onMoveObject.getObjectSlot().clone();
            mConflictSlot = null;
            mAllLocationOnAppWall = getAllLocationInVessel();
        } else {
            cancelConflictAnimationTask();
        }
        return true;
    }

    public boolean onObjectMoveEvent(ACTION event, SEVector3f location) {
        ObjectSlot objectSlot = calculateSlotAndConflictObjects(location);
        boolean slotChanged = !cmpSlot(objectSlot, mObjectSlot);
        switch (event) {
        case BEGIN:
            if (slotChanged) {
                mObjectSlot = objectSlot;
                updateWallStatus(1000);
            }
            break;
        case MOVE:
            if (slotChanged) {
                mObjectSlot = objectSlot;
                updateWallStatus(250);
            }
            break;
        case UP:
            if (slotChanged) {
                mObjectSlot = objectSlot;
                updateWallStatus(250);
            }
            break;
        case FLY:
            if (slotChanged) {
                mObjectSlot = objectSlot;
                updateWallStatus(250);
            }
            break;
        case FINISH:
            slotToWall(location);
            break;
        }
        return true;
    }

    @Override
    public void changeExistentObject(NormalObject src, NormalObject des) {
        if (mExistentSlot != null) {
            for (ConflictObject existentObject : mExistentSlot) {
                if (existentObject.mConflictObject == src) {
                    existentObject.mConflictObject = des;
                    break;
                }
            }
        }
    }

    private void updateWallStatus(long delay) {
        cancelConflictAnimationTask();
        if (mObjectSlot != null && mConflictSlot != null) {
            searchNewSlot(mConflictSlot);
            playConflictAnimationTask(mConflictSlot, delay);
        }
    }

    private ObjectSlot calculateSlotAndConflictObjects(SEVector3f location) {
        ObjectSlot objectSlot = calculateNearestSlot(location);
        if (objectSlot != null && !objectSlot.equals(mObjectSlot)) {
            mConflictSlot = getConflictSlot(objectSlot);
        }
        return objectSlot;
    }

    private ObjectSlot calculateNearestSlot(SEVector3f location) {
        ObjectSlot slot = getOnMoveObject().getObjectSlot().clone();
        slot.mSpanX = 1;
        slot.mSpanY = 1;
        float distance = Float.MAX_VALUE;
        float maxDistance = Float.MAX_VALUE;
        int spanX = getVesselObjectInfo().getSpanX();
        int spanY = getVesselObjectInfo().getSpanY();
        for (int x = 0; x < spanX; x++) {
            for (int y = 0; y < spanY; y++) {
                SEVector3f canSlot = mAllLocationOnAppWall.get(x * spanY + y);
                float cmpDistance = location.subtract(canSlot).getLength();
                if (cmpDistance < distance && cmpDistance < maxDistance) {
                    distance = cmpDistance;
                    slot.mStartX = x;
                    slot.mStartY = y;
                }
            }
        }
        return slot;
    }

    public void slotToWall(SEVector3f location) {
        cancelConflictAnimationTask();
        mObjectSlot = calculateNearestSlot(location);
        mConflictSlot = getConflictSlot(mObjectSlot);
        if (mConflictSlot != null) {
            searchNewSlot(mConflictSlot);
            playConflictAnimationTask(mConflictSlot, 0);
        }
        playSlotAnimation(mObjectSlot, new SEAnimFinishListener() {
            public void onAnimationfinish() {
                handleSlotSuccess();
            }
        });
    }

    @Override
    protected boolean placeObjectToVesselDirectly(NormalObject normalObject) {
        normalObject.changeSlotType(ObjectInfo.SLOT_TYPE_BOOKSHELF);
        return super.placeObjectToVesselDirectly(normalObject);
    }

    private ConflictObject getConflictSlot(ObjectSlot cmpSlot) {
        if (cmpSlot == null) {
            return null;
        }
        for (ConflictObject desktopObject : mExistentSlot) {
            if (desktopObject.mConflictObject.getObjectInfo().getStartX() == cmpSlot.mStartX
                    && desktopObject.mConflictObject.getObjectInfo().getStartY() == cmpSlot.mStartY) {
                return desktopObject;
            }
        }
        return null;
    }

    private List<ConflictObject> getExistentSlot() {
        List<ConflictObject> fillSlots = new ArrayList<ConflictObject>();
        for (SEObject object : getVesselChildren()) {
            if (object instanceof NormalObject) {
                NormalObject desktopObject = (NormalObject) object;
                if (desktopObject.getUserTranslate().getY() > -10000) {
                    ConflictObject conflictObject = new ConflictObject();
                    conflictObject.mConflictObject = desktopObject;
                    fillSlots.add(conflictObject);
                }
            }
        }
        return fillSlots;
    }

    private void playConflictAnimationTask(ConflictObject conflictObject, long delay) {
        cancelConflictAnimationTask();
        if (conflictObject != null) {
            mPlayConflictAnimationTask = new ConflictAnimationTask(conflictObject);
            if (delay == 0) {
                mPlayConflictAnimationTask.run();
            } else {
                SELoadResThread.getInstance().process(mPlayConflictAnimationTask, delay);
            }
        }
    }

    private void cancelConflictAnimationTask() {
        if (mPlayConflictAnimationTask != null) {
            SELoadResThread.getInstance().cancel(mPlayConflictAnimationTask);
            mPlayConflictAnimationTask = null;
        }
    }

    private ConflictAnimationTask mPlayConflictAnimationTask;

    private class ConflictAnimationTask implements Runnable {
        private ConflictObject mMyConflictObject;

        public ConflictAnimationTask(ConflictObject conflictObject) {
            mMyConflictObject = conflictObject;
        }

        public void run() {
            mMyConflictObject.playConflictAnimation();

        }
    }

    private void searchNewSlot(ConflictObject conflictObject) {
        final ObjectSlot NextSlot = conflictObject.mConflictObject.getObjectInfo().mObjectSlot.clone();
        List<ObjectSlot> emptySlots = searchEmptySlot(mExistentSlot);
        float minDistance = Float.MAX_VALUE;
        for (ObjectSlot emptySlot : emptySlots) {
            float distance = getTranslateInVessel(conflictObject.mConflictObject.getObjectInfo().mObjectSlot).subtract(
                    getTranslateInVessel(emptySlot)).getLength();
            if (distance < minDistance) {
                minDistance = distance;
                NextSlot.mStartX = emptySlot.mStartX;
                NextSlot.mStartY = emptySlot.mStartY;
            }
        }
        conflictObject.mMoveSlot = NextSlot;
    }

    private List<ObjectSlot> searchEmptySlot(List<ConflictObject> existentSlot) {
        List<ObjectSlot> emptySlots = null;
        int sizeX = getVesselSlot().mSpanX;
        int sizeY = getVesselSlot().mSpanY;
        boolean[][] slot = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                slot[y][x] = true;
            }
        }
        for (ConflictObject conflictObject : existentSlot) {
            ObjectSlot objectSlot = conflictObject.mConflictObject.getObjectSlot();
            int startY = objectSlot.mStartY;
            int startX = objectSlot.mStartX;
            slot[startY][startX] = false;
        }
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                if (slot[y][x]) {
                    if (emptySlots == null) {
                        emptySlots = new ArrayList<ObjectSlot>();
                    }
                    ObjectSlot objectSlot = new ObjectSlot();
                    objectSlot.mSpanX = 1;
                    objectSlot.mSpanY = 1;
                    objectSlot.mStartX = x;
                    objectSlot.mStartY = y;
                    emptySlots.add(objectSlot);
                }
            }
        }
        return emptySlots;
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();
        getOnMoveObject().handleSlotSuccess();
    }

    private class ConflictObject {
        public SEMoveAnimation mConflictAnimation;
        public NormalObject mConflictObject;
        public ObjectSlot mMoveSlot;

        private void playConflictAnimation() {
            if (mConflictAnimation != null) {
                mConflictAnimation.stop();
            }
            if (mMoveSlot == null) {
                return;
            }
            mConflictObject.getObjectSlot().set(mMoveSlot);
            SETransParas desTransParas = getTransParasInVessel(null, mConflictObject.getObjectSlot());
            SETransParas srcTransParas = mConflictObject.getUserTransParas().clone();
            mConflictAnimation = new SEMoveAnimation(getHomeScene(), mConflictObject, AXIS.Z, srcTransParas,
                    desTransParas, 6);
            mConflictAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    mConflictObject.getObjectInfo().updateSlotDB();
                }
            });
            mConflictAnimation.execute();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            ConflictObject cmp = (ConflictObject) obj;
            return (mConflictObject.equals(cmp.mConflictObject));
        }

    }
}
