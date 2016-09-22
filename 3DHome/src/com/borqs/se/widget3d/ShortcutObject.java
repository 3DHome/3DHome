package com.borqs.se.widget3d;

import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;

public class ShortcutObject extends AppObject {

    @Override
    public void handOnClick() {
        Intent intent = getObjectInfo().getIntent();
        if (intent != null) {
            int x = getTouchX();
            int y = getTouchY();
            intent.setSourceBounds(new Rect(x, y, x, y));
            if (!HomeManager.getInstance().startActivity(intent)) {
                if (HomeUtils.DEBUG)
                    Log.e("SEHome", "not found bind activity");
            }
        }
    }

    public ShortcutObject(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    /// comment it to let super class show/hide label
//    @Override
//    public boolean update(SEScene scene) {
//        return true;
//    }

    @Override
    public void resetIcon() {
        // do nothing
    }
}
