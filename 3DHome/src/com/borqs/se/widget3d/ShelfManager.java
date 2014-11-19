package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.widget3d.House.WallRadiusChangedListener;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class ShelfManager implements WallRadiusChangedListener{
    private static final float MOVE_Z = -43;
    private static final float MOVE_Y = 10;
    private SEVector3f mShelfSize;
    private House mHouse;
    private HomeScene mHomeScene;
    private List<ShelfFace> mShelfFaces;
    /**
     * 记住所有墙面上一次架子位置，结合新计算出的墙面状态计算出当前架子位置
     */
    private List<boolean[][]> mPreShelfSlots;
    private int mShelfIndex;
    private boolean mShowDeskObjectShef = true;
    private boolean mShowAppShef = false;

    public ShelfManager(HomeScene homeScene) {
        mHouse = homeScene.getHouse();
        mHomeScene = homeScene;
        updateShelfs();
    }

    private void initShelfFaces(boolean clearAll) {
        if (mShelfFaces != null) {
            for (ShelfFace shelfFace : mShelfFaces) {
                mHouse.removeComponenetObject(shelfFace, true);
            }
            mShelfFaces = null;
        }
        if ((!mShowDeskObjectShef && !mShowAppShef) || clearAll) {
            return;
        }
        ModelInfo modelInfo = HomeManager.getInstance().getModelManager().findModelInfo("shelf");
        if (modelInfo == null) {
            return;
        }
        mShelfSize = modelInfo.mMaxPoint.subtract(modelInfo.mMinPoint);
        mShelfFaces = new ArrayList<ShelfFace>();
        boolean isEmpty = false;
        if (mPreShelfSlots == null || mPreShelfSlots.size() != mHouse.mWallNum) {
            mPreShelfSlots = HomeUtils.getShelfSlotFromFile(mHomeScene.getContext(), mHomeScene.getSceneName(),
                    mHouse.mCellCountX, mHouse.mCellCountY);
            if (mPreShelfSlots == null || mPreShelfSlots.size() != mHouse.mWallNum) {
                mPreShelfSlots = new ArrayList<boolean[][]>();
                isEmpty = true;
            }
        }

        for (int i = 0; i < mHouse.mWallNum; i++) {
            if (isEmpty) {
                int sizeX = mHouse.mCellCountX;
                int sizeY = mHouse.mCellCountY;
                boolean[][] preShelfSlots = new boolean[sizeY][sizeX];
                mPreShelfSlots.add(preShelfSlots);
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        preShelfSlots[y][x] = true;
                    }
                }
            }

            ShelfFace shelfFace = new ShelfFace(mHomeScene, "shelfFace", i);
            mHouse.addComponenetObject(shelfFace, true);
            mShelfFaces.add(shelfFace);
            ObjectSlot slot = new ObjectSlot();
            slot.mSlotIndex = i;
            shelfFace.setUserTransParas(mHouse.getTransParasInVessel(null, slot));
        }
       

    }

    /**
     * 更新墙面架子
     */
    public void updateShelfs() {
        updateShelfs(false);
        mHouse.addWallRadiusChangedListener(this);
    }

    /**
     * 强制清除架子,在房间或者主题变化新前执行，清除后需调用updateShelfs()重建架子
     */
    public void clearShelfs() {
        updateShelfs(true);
        mHouse.removeWallRadiusChangedListener(this);
    }

    private void updateShelfs(boolean clearAll) {
        mShowDeskObjectShef = HomeManager.getInstance().getWhetherShowDeskObjectShelf();
        mShowAppShef = HomeManager.getInstance().getWhetherShowAppShelf();
        initShelfFaces(clearAll);
        if ((!mShowDeskObjectShef && !mShowAppShef) || clearAll) {
            return;
        }
        mShelfIndex = 10000;
        for (int i = 0; i < mHouse.mWallNum; i++) {
            Wall wall = mHouse.getWall(i);
            boolean[][] preShelfSlots = mPreShelfSlots.get(i);

            if (wall != null && wall.getChildObjects().size() > 0) {
                List<Shelf> shelfs = getShelfsOfWall(wall);
                ShelfFace shelfFace = mShelfFaces.get(i);
                for (Shelf shelf : shelfs) {
                    // 逐个地创建架子，摆放架子
                    HomeManager.getInstance().getModelManager().createQuickly(shelfFace, shelf);
                    SETransParas transparas = wall.getTransParasInVessel(null, shelf.mObjectSlot);
                    float shelfW = mHouse.getWallCellWidth(shelf.mObjectSlot.mSpanX) + mHouse.mWidthGap * 2;
                    float scaleX = shelfW / mShelfSize.getX();
                    transparas.mScale.set(scaleX, 1, 0.6f);
                    transparas.mTranslate.selfAdd(new SEVector3f(0, MOVE_Y, MOVE_Z));
                    shelf.setUserTransParas(transparas);

                    ObjectSlot objectSlot = shelf.mObjectSlot;
                    int startY = objectSlot.mStartY;
                    int startX = objectSlot.mStartX;
                    float sX = objectSlot.mSpanX;
                    float sY = objectSlot.mSpanY;
                    for (int y = startY; y < startY + sY; y++) {
                        for (int x = startX; x < startX + sX; x++) {
                            preShelfSlots[y][x] = false;

                        }
                    }

                }
            }
        }
        //保存架子位置到文件，在3DHome重启后从文件中读出
        new Thread() {
            @Override
            public void run() {
                HomeUtils.saveShelfSlotToFile(mHomeScene.getContext(), mHomeScene.getSceneName(), mPreShelfSlots,
                        mHouse.mCellCountX, mHouse.mCellCountY);
            }
        }.start();
    }

    /**
     * 计算出在该墙面什么地方应该显示架子，以及架子的大小
     * 
     * @param wall
     *            墙面
     * @return 在该墙面的架子
     * 
     */
    private List<Shelf> getShelfsOfWall(Wall wall) {
        List<Shelf> shelfs = new ArrayList<Shelf>();
        int wallIndex = wall.getObjectSlot().mSlotIndex;
        int sizeX = mHouse.mCellCountX;
        int sizeY = mHouse.mCellCountY;
        boolean[][] fullSlot = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                fullSlot[y][x] = true;
            }
        }
        boolean[][] iconSlot = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                iconSlot[y][x] = true;
            }
        }
        for (SEObject child : wall.getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                ObjectSlot objectSlot = normalObject.getObjectSlot();
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
                boolean isDesktopObj = false;
                if (normalObject.getObjectInfo().mIsNativeObject
                        && normalObject.getObjectInfo().mModelInfo.mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                    isDesktopObj = true;
                }
                if (mShowAppShef) {
                    if (normalObject instanceof AppObject || normalObject instanceof Folder || isDesktopObj) {
                        if (startY < sizeY && startX < sizeX) {
                            iconSlot[startY][startX] = false;
                        }
                    }
                } else if (mShowDeskObjectShef) {
                    if (isDesktopObj) {
                        if (startY < sizeY && startX < sizeX) {
                            iconSlot[startY][startX] = false;
                        }
                    }
                }
            }

        }
        boolean[][] preShelfSlots = mPreShelfSlots.get(wallIndex);
        for (int y = 0; y < sizeY; y++) {
            Shelf shelf = null;
            int emptySpan = 0;
            boolean hasIconOnShelf = false;
            for (int x = 0; x < sizeX; x++) {
                boolean slotHasIcon = !iconSlot[y][x];
                boolean slotIsEmpty = fullSlot[y][x];
                boolean slotHasEmptyShelf = (!preShelfSlots[y][x]) && slotIsEmpty;

                if (slotHasIcon || slotHasEmptyShelf) {
                    if (shelf == null) {
                        mShelfIndex++;
                        emptySpan = 0;
                        shelf = new Shelf(mHomeScene, "shelf", mShelfIndex);
                        shelf.mObjectSlot.mSlotIndex = wallIndex;
                        shelf.mObjectSlot.mStartX = x;
                        shelf.mObjectSlot.mStartY = y;
                        shelf.mObjectSlot.mSpanX = 1;
                        shelf.mObjectSlot.mSpanY = 1;
                    } else {
                        shelf.mObjectSlot.mSpanX++;
                        shelf.mObjectSlot.mSpanX += emptySpan;
                        emptySpan = 0;
                    }
                    if (slotHasIcon) {
                        hasIconOnShelf = true;
                    }
                } else if (slotIsEmpty) {
                    if (shelf != null) {
                        emptySpan++;
                    }
                } else if (!slotIsEmpty) {
                    if (shelf != null && hasIconOnShelf) {
                        shelfs.add(shelf);

                    }
                    shelf = null;
                    emptySpan = 0;
                    hasIconOnShelf = false;
                }
                preShelfSlots[y][x] = true;
            }
            if (shelf != null && hasIconOnShelf) {
                shelfs.add(shelf);
            }
            shelf = null;
            emptySpan = 0;
            hasIconOnShelf = false;
        }
        return shelfs;

    }

    private class ShelfFace extends SEObject {

        public ShelfFace(SEScene scene, String name, int index) {
            super(scene, name, index);
            setIsGroup(true);
        }

    }

    private class Shelf extends SEObject {
        private ObjectSlot mObjectSlot = new ObjectSlot();

        public Shelf(SEScene scene, String name, int index) {
            super(scene, name, index);
        }
    }

    @Override
    public void onWallRadiusChanged(int faceIndex) {
        int curIndex = faceIndex;
        int showFaceIndexA = curIndex - 1;
        if (showFaceIndexA < 0) {
            showFaceIndexA = mHouse.mWallNum - 1;
        }
        int showFaceIndexB = curIndex;
        int showFaceIndexC = curIndex + 1;
        if (showFaceIndexC > mHouse.mWallNum - 1) {
            showFaceIndexC = 0;
        }
        if (mShelfFaces != null) {
            int size = mShelfFaces.size();
            for (int i = 0; i < size; i++) {
                if (i == showFaceIndexA || i == showFaceIndexB || i == showFaceIndexC) {
                    mShelfFaces.get(i).setVisible(true);
                } else {
                    mShelfFaces.get(i).setVisible(false);
                }
            }
        }

    }
}
