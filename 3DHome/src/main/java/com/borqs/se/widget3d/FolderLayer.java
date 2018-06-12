package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import com.borqs.se.engine.SEScaleAnimation;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;

import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class FolderLayer extends VesselLayer {
    private Folder mFolder;
    private List<NormalObject> mExistentSlot;
    private SEScaleAnimation mSetScaleAnimation;

    public FolderLayer(HomeScene scene, VesselObject vesselObject) {
        super(scene, vesselObject);
        mFolder = (Folder) vesselObject;
    }

    @Override
    public boolean canHandleSlot(NormalObject object, float touchX, float touchY) {
        super.canHandleSlot(object, touchX, touchY);
        if (object instanceof AppObject) {
            mExistentSlot = getExistentSlot();
            if (mExistentSlot.size() < 12) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setOnLayerModel(NormalObject onMoveObject, boolean onLayerModel) {
        super.setOnLayerModel(onMoveObject, onLayerModel);
        if (onLayerModel) {
            mExistentSlot = getExistentSlot();
            if (mSetScaleAnimation != null) {
                mSetScaleAnimation.stop();
            }
            SEVector3f localScale = mFolder.getLocalScale();
            if (localScale.getX() < 1.2f) {
                int times = (int) ((1.2f - localScale.getX()) / 0.04f);
                mSetScaleAnimation = new SEScaleAnimation(getHomeScene(), mFolder, localScale, new SEVector3f(1.2f, 1,
                        1.2f), times);
                mSetScaleAnimation.setIsLocal(true);
                mSetScaleAnimation.execute();
            }
        } else {
            mExistentSlot = getExistentSlot();
            if (mSetScaleAnimation != null) {
                mSetScaleAnimation.stop();
            }
            SEVector3f localScale = mFolder.getLocalScale();
            if (localScale.getX() > 1) {
                int times = (int) ((localScale.getX() - 1) / 0.04f);
                mSetScaleAnimation = new SEScaleAnimation(getHomeScene(), mFolder, localScale, new SEVector3f(1, 1, 1),
                        times);
                mSetScaleAnimation.setIsLocal(true);
                mSetScaleAnimation.execute();
            }
        }
        return true;
    }

    @Override
    public boolean onObjectMoveEvent(ACTION event, SEVector3f location) {
        switch (event) {
        case FINISH:
            slotToFolder(location);
            break;
        }
        return true;
    }

    private void slotToFolder(SEVector3f location) {
        ObjectSlot objectSlot = calculateSlot();
        playSlotAnimation(objectSlot, new SEAnimFinishListener() {
            public void onAnimationfinish() {
                handleSlotSuccess();
            }                
        });
    }

    @Override
    public void handleSlotSuccess() {
        super.handleSlotSuccess();

        mFolder.updateFolderCover();

        getOnMoveObject().handleSlotSuccess();
        getOnMoveObject().setIsEntirety(false);
        getOnMoveObject().setVisible(false);
        getOnMoveObject().hideBackground();
    }

    private ObjectSlot calculateSlot() {
        ObjectSlot slot = getOnMoveObject().getObjectSlot().clone();
        slot.mSlotIndex = mExistentSlot.size();
        return slot;
    }

    private List<NormalObject> getExistentSlot() {
        List<NormalObject> fillSlots = new ArrayList<NormalObject>();
        for (SEObject object : mFolder.getChildObjects()) {
            if (object instanceof NormalObject) {
                NormalObject desktopObject = (NormalObject) object;
                fillSlots.add(desktopObject);
            }
        }
        return fillSlots;
    }

}
