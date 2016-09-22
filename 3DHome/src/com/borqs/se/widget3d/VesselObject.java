package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.database.Cursor;

import com.borqs.se.engine.*;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.MountPointData.SEMountPointGroup;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.VesselColumns;
import com.borqs.se.home3d.UpdateDBThread;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.AppCallBack;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public abstract class VesselObject extends NormalObject implements AppCallBack {

    public VesselLayer mVesselLayer;
    private SEMountPointGroup mVesselMountPoint;

    public VesselObject(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    public VesselLayer getVesselLayer() {
        return mVesselLayer;
    }

    public void setVesselLayer(VesselLayer vesselLayer) {
        mVesselLayer = vesselLayer;
    }

    public SEMountPointGroup getContainerMountPoint() {
        if (mVesselMountPoint == null) {
            mVesselMountPoint = getObjectInfo().mModelInfo.mMountPointData.getContainerMountPoint(this);
        }
        return mVesselMountPoint;
    }

    public abstract SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot);

    public void onLoadFinished() {

    }

    @Override
    public boolean loadAll(SEObject parent, final Runnable finish) {
        loadMyself(parent, new Runnable() {
            public void run() {
                loadChild(finish);
            }
        });
        return true;
    }

    private boolean loadChild(final Runnable finish) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                final List<ObjectInfo> childrenObjectInfos = new ArrayList<ObjectInfo>();
                Cursor cursor = null;
                try {
                    String where = VesselColumns.VESSEL_ID + "=" + getObjectInfo().mID;
                    cursor = getContext().getContentResolver().query(ObjectInfoColumns.OBJECT_LEFT_JOIN_ALL, null,
                            where, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            ObjectInfo objectInfo = ObjectInfo.CreateFromDB(cursor);
                            childrenObjectInfos.add(objectInfo);
                        } while (cursor.moveToNext());
                    }
                } catch (IllegalStateException ex) {

                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                new SECommand(getScene()) {
                    public void run() {
                        for (ObjectInfo objectInfo : childrenObjectInfos) {
                            objectInfo.setModelInfo(HomeManager.getInstance().getModelManager()
                                    .findModelInfo(objectInfo.mName));
                            if (checkObjectInfo(objectInfo)) {
                                NormalObject object = objectInfo.CreateNormalObject(getHomeScene());
                                addChild(object, false);
                            } else {
                                objectInfo.releaseDB();
                            }
                        }
                        if (getChildObjects().size() > 0) {
                            List<SEObject> childObjects = new ArrayList<SEObject>();
                            childObjects.addAll(getChildObjects());
                            loadAllChild(childObjects, finish);
                        } else {
                            onLoadFinished();
                            if (finish != null) {
                                finish.run();
                            }
                        }
                    }
                }.execute();

            }
        });
        return true;
    }

    /**
     * 一：假如是模型创建出来的物体，但是模型没有找到，Object无效
     * 二：假如是shortcut类型但是没有图标，Object无效
     */
    private boolean checkObjectInfo(ObjectInfo objectInfo) {
        if (objectInfo.mIsNativeObject && objectInfo.mModelInfo == null) {
            return false;
        }
        if (objectInfo.isShortcut()) {
            objectInfo.mComponentName = null;
        }
        return !(objectInfo.isShortcut() && objectInfo.mShortcutIcon == null);
    }

    private int mLoadCount;

    private void loadAllChild(List<SEObject> childObjects, final Runnable finish) {
        final int size = childObjects.size();
        mLoadCount = 0;
        for (SEObject child : childObjects) {
            if (child instanceof NormalObject) {
                NormalObject childNormalObject = (NormalObject) child;
                childNormalObject.loadAll(this, new Runnable() {
                    public void run() {
                        mLoadCount++;
                        if (mLoadCount == size) {
                            onLoadFinished();
                            if (finish != null) {
                                finish.run();
                            }
                        }

                    }
                });
            } else {
                mLoadCount++;
                if (mLoadCount == size) {
                    onLoadFinished();
                    if (finish != null) {
                        finish.run();
                    }
                }
            }
        }

    }

    public void bindAppsAdded(List<ItemInfo> apps) {
        // TODO Auto-generated method stub

    }

    public void bindAppsUpdated(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        new SECommand(getScene()) {
            public void run() {
                for (ItemInfo info : myApps) {
                    List<NormalObject> matchApps = findAPP(info.getComponentName(), "App");
                    for (NormalObject object : matchApps) {
                        object.update(getScene());
                    }
                }
            }
        }.execute();
    }

    public void bindAppsRemoved(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        new SECommand(getScene()) {
            public void run() {
                for (ItemInfo info : myApps) {
                    List<NormalObject> matchApps = findAPP(info.getComponentName(), null);
                    for (NormalObject object : matchApps) {
                        final ObjectInfo objectInfo = object.getObjectInfo();
                        objectInfo.releaseDB();
                        object.getParent().removeChild(object, true);
                    }
                }
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
            }
        }.execute();
    }

    public void bindAllPackagesUpdated() {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(LauncherModel.getInstance().getAllDatas());
        new SECommand(getScene()) {
            public void run() {
                List<NormalObject> matchApps = findAPP(null, null);
                for (NormalObject object : matchApps) {
                    final ObjectInfo objectInfo = object.getObjectInfo();
                    if (objectInfo.mComponentName != null && isComponentNameExist(myApps, objectInfo.mComponentName)) {
                        object.update(getScene());
                    }
                }
            }
        }.execute();
    }

    public void bindAllPackages() {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindUnavailableApps(List<ItemInfo> apps) {

    }

    @Override
    public void bindAvailableApps(List<ItemInfo> apps) {
        final List<ItemInfo> myApps = new ArrayList<ItemInfo>();
        myApps.addAll(apps);
        new SECommand(getScene()) {
            public void run() {
                for (ItemInfo info : myApps) {
                    List<NormalObject> matchApps = findAPP(info.getComponentName(), null);
                    for (NormalObject object : matchApps) {
                        object.update(getScene());
                    }
                }
            }
        }.execute();
    }

    private boolean isComponentNameExist(List<ItemInfo> allApps, ComponentName componentName) {
        for (ItemInfo item : allApps) {
            if (componentName.equals(item.getComponentName())) {
                return true;
            }
        }
        return false;
    }

    public List<NormalObject> findAPP(ComponentName componentName, String myType) {
        List<NormalObject> newItems = new ArrayList<NormalObject>();
        for (SEObject item : getChildObjects()) {
            if (item instanceof NormalObject) {
                NormalObject appObject = (NormalObject) item;
                String type = appObject.getObjectInfo().mType;
                boolean flag = false;
                if (myType == null) {
                    flag = "App".equals(type) || "Shortcut".equals(type) || "Widget".equals(type)
                            || "IconBox".equals(type);
                } else {
                    flag = myType.equals(type);
                }
                if (flag) {
                    if (componentName != null) {
                        if (componentName.equals(appObject.getObjectInfo().mComponentName)) {
                            newItems.add(appObject);
                        }
                    } else {
                        newItems.add(appObject);
                    }
                }
            }
        }
        return newItems;
    }

    /**
     * 把物体放入容器
     */
    public boolean placeObjectToVessel(NormalObject normalObject, boolean needConflictTest) {
        if (needConflictTest) {
            return mVesselLayer.placeObjectToVessel(normalObject);
        } else {
            if (this != normalObject.getParent()) {
                normalObject.changeParent(this);
            }
            SETransParas desTransParas = getTransParasInVessel(normalObject, normalObject.getObjectSlot());
            if (desTransParas != null) {
                normalObject.getUserTransParas().set(desTransParas);
                normalObject.setUserTransParas();
            }
        }
        return true;
    }

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            LauncherModel.getInstance().removeAppCallBack(this);
        }
    }

    /**
     * 获取容器所有可以摆放物体的位置
     */
    public List<SEVector3f> getAllLocationForObject(NormalObject object) {
        List<SEVector.SEVector3f> allLocation = new ArrayList<SEVector.SEVector3f>();
        int spanX = getObjectInfo().getSpanX();
        int spanY = getObjectInfo().getSpanY();
        for (int x = 0; x < spanX; x++) {
            for (int y = 0; y < spanY; y++) {
                ObjectSlot slot = new ObjectSlot();
                slot.mSpanX = 1;
                slot.mSpanY = 1;
                slot.mStartX = x;
                slot.mStartY = y;
                SEVector3f targetTranslate = getTransParasInVessel(object, slot).mTranslate;
                SEVector3f location = new SEVector3f(localToWorld_JNI(targetTranslate.mD));
                allLocation.add(location);
            }
        }
        return allLocation;
    }

    /**
     * 获取容器旋转的角度
     */
    public float getAngle() {
        return 0f;
    }
    /**
     * 获取容器可容纳物体的数量
     */
    public int getVesselCapability() {
        ObjectInfo info = getObjectInfo();
        if (null != info) {
            int spans = info.getSpanX() * info.getSpanY();
            if (spans > 0) {
                return spans;
            }
        }
        return 0;
    }

    public void changeObjectTransParasInVessel(NormalObject object) {
        placeObjectToVessel(object, false);
    }

    public void moveAllChildToVesselObject(VesselObject newVessel) {
        List<SEObject> childObjects = new ArrayList<SEObject>();
        childObjects.addAll(getChildObjects());
        for (SEObject child : childObjects) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                normalObject.changeParent(newVessel);
                normalObject.initStatus();
            }
        }
    }

}