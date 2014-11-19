package com.borqs.se.widget3d;

import java.util.List;

import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class Wall extends VesselObject {
    private House mHouse;
    private boolean mShowAppShef = false;

    public Wall(HomeScene scene, String name, int index) {
        super(scene, name, index);
        setPressType(PRESS_TYPE.NONE);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        mShowAppShef = HomeManager.getInstance().getWhetherShowAppShelf();
        setVesselLayer(new WallCellLayer(getHomeScene(), this));
        LauncherModel.getInstance().addAppCallBack(this);
        setOnClickListener(null);
        if (getParent() instanceof House) {
            mHouse = (House) getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
//        test();
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        boolean showAppShef = HomeManager.getInstance().getWhetherShowAppShelf();
        if (showAppShef != mShowAppShef) {
            mShowAppShef = showAppShef;
        }
    }

    private void test() {
        for (int x = 0; x < mHouse.mCellCountX; x++) {
            for (int y = 0; y < mHouse.mCellCountY; y++) {
                SEObject line = new SEObject(getScene(), "line_" + x + y + System.currentTimeMillis());
                SERect3D rect = new SERect3D(mHouse.mCellWidth, mHouse.mCellHeight);
                SEObjectFactory.createRectLine(line, rect, new float[] { 1, 0, 0 });
                addChild(line, true);
                ObjectSlot slot = new ObjectSlot();
                slot.mStartX = x;
                slot.mStartY = y;
                slot.mSpanX = 1;
                slot.mSpanY = 1;
                line.getUserTransParas().set(getTransParasInVessel(null, slot));
                line.setUserTransParas();
            }
        }
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        float y = 0;
        if (mShowAppShef && needPlaceObj != null
                && (needPlaceObj instanceof AppObject || needPlaceObj instanceof Folder)) {
            y = -80;
        }
        SETransParas transparas = new SETransParas();
        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        float offsetX = (objectSlot.mStartX + objectSlot.mSpanX / 2.f) * gridSizeX - mHouse.mCellCountX * gridSizeX
                / 2.f + (mHouse.mWallPaddingLeft - mHouse.mWallPaddingRight) / 2;
        float offsetZ = mHouse.mCellCountY * gridSizeY / 2.f - (objectSlot.mStartY + objectSlot.mSpanY / 2.f)
                * gridSizeY + (mHouse.mWallPaddingBottom - mHouse.mWallPaddingTop) / 2;
        transparas.mTranslate.set(offsetX, y, offsetZ);
        return transparas;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeAppCallBack(this);
    }
    
    
    @Override
    public void onActivityRestart() {
        super.onActivityRestart();
        forceReloadWidget();
    }
    
    private void forceReloadWidget() {
        new SECommand(getScene()) {
            public void run() {
                List<NormalObject> matchApps = findAPP(null, "Widget");
                for (NormalObject widget : matchApps) {
                    WidgetObject myWidget = (WidgetObject) widget;
                    myWidget.bind();
                }
            }
        }.execute();
    }
}
