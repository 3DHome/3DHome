package com.borqs.se.widget3d;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class BookshelfL extends VesselObject {
    private float mBookshelfGridSizeX;
    private float mBookshelfGridSizeY;
    private SEObject mBookshelfUnit;
    private SEObject mBookshelfUnitBack;
    private float mScaleOfChild;
    private float mBookMoveZ;
    private House mHouse;
    public BookshelfL(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        getModelComponenet().setVisible(false);
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        float newW = getWidthOfWidget(getObjectInfo().mObjectSlot.mSpanX);
        float newH = getHeightOfWidget(getObjectInfo().mObjectSlot.mSpanY);
        float bookW = mHouse.mCellWidth;
        float bookH = (float) (mHouse.mCellWidth * 1.414f * Math.cos(25 * Math.PI / 180));
        mBookshelfGridSizeX = (newW - 15) / getObjectInfo().mObjectSlot.mSpanX;
        mBookshelfGridSizeY = (newH - 15) / getObjectInfo().mObjectSlot.mSpanY;
        mScaleOfChild = Math.min(mBookshelfGridSizeX / bookW, mBookshelfGridSizeY / bookH) * 0.76f;
        mBookMoveZ = 5 - (mBookshelfGridSizeY - mScaleOfChild * bookH) / 2;
        setCanChangeBind(false);
        setCanBeResized(true);
        setOnClickListener(null);
        setVesselLayer(new BookshelfLayer((HomeScene) getScene(), this));
        LauncherModel.getInstance().addAppCallBack(this);
        loadBookshelfImage();
        createBookshelfUint();
        createBookshelf();

    }

    @Override
    public void onSizeAndPositionChanged(Rect sizeRect) {
        getObjectSlot().mSpanX = sizeRect.width();
        getObjectSlot().mSpanY = sizeRect.height();
        getObjectSlot().mStartX = sizeRect.left;
        getObjectSlot().mStartY = sizeRect.top;
        getObjectInfo().updateSlotDB();
        resizeBookShelf();
        if (getParent() != null && (getParent() instanceof VesselObject)) {
            VesselObject vessel = (VesselObject) getParent();
            SETransParas transParas = vessel.getTransParasInVessel(this, getObjectSlot());
            if (transParas != null) {
                getUserTransParas().set(transParas);
                setUserTransParas();
            }
        }
        updateChildPostion();
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        super.handleBackKey(l);
        if (getHomeScene().getDragLayer().isOnResize()) {
            getHomeScene().getDragLayer().finishResize();
            return true;
        }
        return false;
    }

    private void resizeBookShelf() {
        float newW = getWidthOfWidget(getObjectInfo().mObjectSlot.mSpanX);
        float newH = getHeightOfWidget(getObjectInfo().mObjectSlot.mSpanY);

        float bookW = mHouse.mCellWidth;
        float bookH = (float) (mHouse.mCellWidth * 1.414f * Math.cos(25 * Math.PI / 180));

        mBookshelfGridSizeX = (newW - 15) / getObjectInfo().mObjectSlot.mSpanX;
        mBookshelfGridSizeY = (newH - 15) / getObjectInfo().mObjectSlot.mSpanY;
        mScaleOfChild = Math.min(mBookshelfGridSizeX / bookW, mBookshelfGridSizeY / bookH) * 0.76f;
        mBookMoveZ = 5 - (mBookshelfGridSizeY - mScaleOfChild * bookH) / 2;
        createBookshelf();
    }

    private void updateChildPostion() {
        for (SEObject child : getChildObjects()) {
            if (child instanceof NormalObject) {
                NormalObject normalObject = (NormalObject) child;
                normalObject.setUserTransParas(getTransParasInVessel(normalObject, normalObject.getObjectSlot()));
            }
        }
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        SETransParas transparas = new SETransParas();
        ObjectSlot vesselSlot = getObjectInfo().mObjectSlot;
        if (objectSlot.mStartX + objectSlot.mSpanX > vesselSlot.mSpanX
                || objectSlot.mStartY + objectSlot.mSpanY > vesselSlot.mSpanY) {
            transparas.mTranslate.set(0, -10000, 0);
            transparas.mScale.set(0, 0, 0);
            return transparas;
        } else {
            float offsetX = (objectSlot.mStartX + objectSlot.mSpanX / 2.f) * mBookshelfGridSizeX - vesselSlot.mSpanX
                    * mBookshelfGridSizeX / 2.f;
            float offsetZ = vesselSlot.mSpanY * mBookshelfGridSizeY / 2.f
                    - (objectSlot.mStartY + objectSlot.mSpanY / 2.f) * mBookshelfGridSizeY;
            transparas.mTranslate.set(offsetX, -50, offsetZ + mBookMoveZ);
            transparas.mScale.set(mScaleOfChild, mScaleOfChild, mScaleOfChild);
            return transparas;
        }
    }

    private float getWidthOfWidget(float spanX) {
        return mHouse.getWallCellWidth(spanX);
    }

    private float getHeightOfWidget(float spanY) {
        return mHouse.getWallCellHeight(spanY);
    }

    private void loadBookshelfImage() {
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                if (SESceneManager.getInstance().getTextureImage("bookshelf_background_image_key") == null) {
                    Bitmap res = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    res.setPixel(0, 0, Color.WHITE);
                    SESceneManager.getInstance().addTextureImage("bookshelf_background_image_key", res);
                }
            }
        });
    }

    /**
     * 创建一块板子，书架由一块块的板子拼接而成
     */
    private void createBookshelfUint() {
        mBookshelfUnit = new SEObject(getScene(), mName + "_unit_" + mIndex);
        mBookshelfUnit.setVertexArray(new float[] { -0.5f, -1, -0.05f, 0.5f, -1, -0.05f, 0.5f, 0, -0.05f, -0.5f, 0,
                -0.05f, -0.5f, -1, 0.05f, 0.5f, -1, 0.05f, 0.5f, 0, 0.05f, -0.5f, 0, 0.05f });
        mBookshelfUnit.setFaceArray(new int[] { 0, 3, 2, 2, 1, 0, 0, 1, 5, 5, 4, 0, 4, 5, 6, 6, 7, 4 });
        mBookshelfUnit.setTexVertexArray(new float[] { 0, 10 / 21f, 1, 10 / 21f, 1, 0, 0, 0, 0, 11 / 21f, 1, 11 / 21f,
                1, 1, 0, 1 });
        mBookshelfUnit.setImage(IMAGE_TYPE.BITMAP, mBookshelfUnit.getName() + "_image_name",
                "bookshelf_background_image_key");
        mBookshelfUnit.setNeedDepthTest(true);
        mBookshelfUnit.setBlendSortAxis(AXIS.Y);
        mBookshelfUnit.revertTextureImage();
        addComponenetObject(mBookshelfUnit, true);
        mBookshelfUnit.setNeedCullFace(true);
        mBookshelfUnit.applyLight_JNI("bookshelflight");
        mBookshelfUnit.setVisible(false);

        mBookshelfUnitBack = new SEObject(getScene(), mName + "_unit_back_" + mIndex);
        SERect3D rect = new SERect3D(1, 1);
        SEObjectFactory.createRectangle(mBookshelfUnitBack, rect, IMAGE_TYPE.BITMAP, mBookshelfUnitBack.getName()
                + "_image_name", "bookshelf_background_image_key", null, null, false);
        mBookshelfUnitBack.setNeedDepthTest(true);
        mBookshelfUnitBack.setBlendSortAxis(AXIS.Y);
        mBookshelfUnitBack.revertTextureImage();
        addComponenetObject(mBookshelfUnitBack, true);
        mBookshelfUnitBack.setNeedCullFace(true);
        mBookshelfUnitBack.applyLight_JNI("bookshelflight");
        mBookshelfUnitBack.setVisible(false);
    }

    private List<SEObject> mBookshelfUnits;

    private void createBookshelf() {
        if (mBookshelfUnits != null) {
            for (SEObject unit : mBookshelfUnits) {
                removeComponenetObject(unit, true);
            }
        }
        mBookshelfUnits = new ArrayList<SEObject>();
        int nunX = getObjectInfo().getSpanX();
        int nunY = getObjectInfo().getSpanY();
        for (int y = 1; y <= nunY; y++) {
            for (int x = 0; x < nunX; x++) {
                float moveX = mBookshelfGridSizeX * (x - (nunX - 1) * 0.5f);
                float moveY = mBookshelfGridSizeY * (-y + nunY * 0.5f);

                SEObject unit = new SEObject(getScene(), mBookshelfUnit.getName(), y * nunX + x + 1);
                mBookshelfUnit.cloneObject_JNI(this, unit.mIndex);
                unit.setHasBeenAddedToEngine(true);
                addComponenetObject(unit, false);
                unit.setUserScale(new SEVector3f(mBookshelfGridSizeX, 100, 100));
                unit.setUserTranslate(new SEVector3f(moveX, 0, moveY));
                unit.setVisible(true);
                mBookshelfUnits.add(unit);

            }
        }

        for (int y = 0; y < nunY; y++) {
            for (int x = 0; x < nunX; x++) {
                float moveX = mBookshelfGridSizeX * (x - (nunX - 1) * 0.5f);
                float moveY = mBookshelfGridSizeY * (-y + (nunY - 1) * 0.5f);
                SEObject unitB = new SEObject(getScene(), mBookshelfUnitBack.getName(), y * nunX + x + 1);
                mBookshelfUnitBack.cloneObject_JNI(this, unitB.mIndex);
                unitB.setHasBeenAddedToEngine(true);
                addComponenetObject(unitB, false);
                unitB.setUserScale(new SEVector3f(mBookshelfGridSizeX, 150, mBookshelfGridSizeY - 20));
                unitB.setUserTranslate(new SEVector3f(moveX, 0, moveY - 10));
                unitB.setVisible(true);
                mBookshelfUnits.add(unitB);
            }
        }
    }
    
    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }
}
