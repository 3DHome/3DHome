package com.borqs.se.widget3d;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.util.Log;
import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEImageView;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;

public class AppObject extends NormalObject {
    public SEImageView mIconObject;
    public NormalObject mIconBoxObject;
    public Book mBookObject;
    private static int INDEX_OF_ICON_BOX = 1;
    private int mAppIconBackgroundType = 0;
    private boolean mHasCreated = false;
    private boolean mNeedShowLable = true;
    private House mHouse;

    public AppObject(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void onSlotTypeChanged(int slotType) {
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else if (getParent().getParent() != null && getParent().getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        if (slotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            if (mIconBoxObject == null && !mOnCreateIconBox) {
                createIconBoxObject();
            }
            if (mIconObject != null) {
                mIconObject.setVisible(false);
            }
            if (mIconBoxObject != null) {
                mIconBoxObject.setVisible(true);
            }
            if (mBookObject != null) {
                mBookObject.setVisible(false);
            }
        } else if (slotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            if (mBookObject == null && !mOnCreateBook) {
                createBookObject();
            }
            if (mIconObject != null) {
                mIconObject.setVisible(false);
            }
            if (mIconBoxObject != null) {
                mIconBoxObject.setVisible(false);
            }
            if (mBookObject != null) {
                mBookObject.setVisible(true);
            }
        } else {
            if (mIconObject == null && !mOnCreateIcon) {
                createIconObject();
            }
            if (mIconObject != null) {
                mIconObject.setVisible(true);
            }
            if (mIconBoxObject != null) {
                mIconBoxObject.setVisible(false);
            }
            if (mBookObject != null) {
                mBookObject.setVisible(false);
            }
        }
    }

    @Override
    public void initStatus() {
        super.initStatus();
        mNeedShowLable = !HomeManager.getInstance().getWhetherShowAppShelf();
        mAppIconBackgroundType = HomeManager.getInstance().getAppIconBackgroundType();
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else if (getParent().getParent() != null && getParent().getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        setOnLongClickListener(new SEObject.OnTouchListener() {
            public void run(SEObject obj) {
                SETransParas startTranspara = new SETransParas();
                startTranspara.mTranslate = getAbsoluteTranslate();
                float angle = getUserRotate().getAngle();
                SEObject parent = getParent();
                while (parent != null) {
                    angle = angle + parent.getUserRotate().getAngle();
                    parent = parent.getParent();
                }
                startTranspara.mRotate.set(angle, 0, 0, 1);
                setStartTranspara(startTranspara);
                setOnMove(true);
                hideBackground();
            }
        });
        if (!isSysApp(getObjectInfo().mComponentName)) {
            setCanUninstall(true);
        } else {
            setCanUninstall(false);
        }

        setCanChangeIcon(true);
        if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            setCanChangeLabel(false);
            setCanChangeBind(true);
        } else if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            setCanChangeLabel(true);
            setCanChangeBind(true);
        } else {
            setCanChangeLabel(true);
            setCanChangeBind(false);
        }
        if (!mHasCreated) {
            mHasCreated = true;
            if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                createIconBoxObject();
            } else if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
                createBookObject();
            } else {
                createIconObject();
            }
        }
    }

    public static AppObject create(HomeScene scene, ItemInfo itemInfo) {
        ObjectInfo info = new ObjectInfo();
        info.mName = "app_" + itemInfo.getComponentName() + "_" + System.currentTimeMillis();
        info.mSceneName = scene.getSceneName();
        info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        info.mObjectSlot.mSpanX = itemInfo.getSpanX();
        info.mObjectSlot.mSpanY = itemInfo.getSpanY();
        info.mComponentName = itemInfo.getComponentName();
        info.mType = "App";
        AppObject appObject = new AppObject(scene, info.mName, info.mIndex);
        appObject.setIsFresh(true);
        appObject.setObjectInfo(info);
        info.saveToDB();
        return appObject;
    }

    public static AppObject create(HomeScene scene, ComponentName componentName, int index) {
        ObjectInfo info = new ObjectInfo();
        info.mName = "app_" + componentName + "_" + System.currentTimeMillis() + "_" + index;
        info.mSceneName = scene.getSceneName();
        info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        info.mObjectSlot.mSpanX = 1;
        info.mObjectSlot.mSpanY = 1;
        info.mComponentName = componentName;
        info.mType = "App";
        AppObject appObject = new AppObject(scene, info.mName, info.mIndex);
        appObject.setIsFresh(true);
        appObject.setObjectInfo(info);
        info.saveToDB();
        return appObject;
    }

    @Override
    public void handOnClick() {
        Intent intent = getObjectInfo().getIntent();
        if (intent != null) {
            if (!HomeManager.getInstance().startActivity(intent)) {
                if (HomeUtils.DEBUG)
                    Log.e("SEHome", "not found bind activity");
            }
        }
    }

    @Override
    public void handleOutsideRoom() {
        if (isFresh()) {
            if (getObjectInfo().mComponentName != null) {
                startApplicationUninstallActivity(getObjectInfo().mComponentName);
            }
            setIsFresh(false);
        }
        super.handleOutsideRoom();
    }

    @Override
    public void onSlotSuccess() {
        super.onSlotSuccess();
        if (hasBeenReleased()) {
            return;
        }
        if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            setCanChangeLabel(false);
            setCanChangeBind(true);
            if (mIconObject != null) {
                removeComponenetObject(mIconObject, true);
                mIconObject = null;
            }
            if (mBookObject != null) {
                removeComponenetObject(mBookObject, true);
                mBookObject = null;
            }
        } else if (getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            setCanChangeLabel(true);
            setCanChangeBind(true);
            if (mIconObject != null) {
                removeComponenetObject(mIconObject, true);
                mIconObject = null;
            }
            if (mIconBoxObject != null) {
                removeChild(mIconBoxObject, true);
                mIconBoxObject = null;
            }
        } else {
            setCanChangeLabel(true);
            setCanChangeBind(false);
            if (mIconBoxObject != null) {
                removeChild(mIconBoxObject, true);
                mIconBoxObject = null;
            }
            if (mBookObject != null) {
                removeComponenetObject(mBookObject, true);
                mBookObject = null;
            }
        }
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        int appIconBackgroundType = HomeManager.getInstance().getAppIconBackgroundType();
        boolean needShowLable = !HomeManager.getInstance().getWhetherShowAppShelf();
        if (appIconBackgroundType != mAppIconBackgroundType || needShowLable != mNeedShowLable) {
            mAppIconBackgroundType = appIconBackgroundType;
            mNeedShowLable = needShowLable;
            new SECommand(getScene()) {
                public void run() {
                    if (mIconObject != null) {
                        createIconObject();
                        if (getParent() != null) {
                            changeTransParasInParent();
                        }
                    }
                }
            }.execute();
        }

    }

    @Override
    public boolean update(SEScene scene) {
        if (mIconObject != null) {
            createIconObject();
            changeTransParasInParent();
        } else if (mIconBoxObject != null) {
            changeImageOfIconBox();
        } else if (mBookObject != null) {
            changeImageOfBook();
        }
        return true;
    }

    public void updateIcon(final Bitmap icon) {
        if (icon == null) {
            return;
        }
        final ObjectInfo info = getObjectInfo();
        info.mShortcutIcon = icon.copy(Config.ARGB_8888, true);
        info.updateToDB(true, false, false);
        if (mIconObject != null) {
            createIconObject();
        } else if (mIconBoxObject != null) {
            changeImageOfIconBox();
        } else if (mBookObject != null) {
            changeImageOfBook();
        }
    }

    public void updateLabel(final String name) {
        if (name == null) {
            return;
        }
        ObjectInfo info = getObjectInfo();
        info.mDisplayName = name;
        info.updateToDB(false, true, false);
        if (mIconObject != null) {
            createIconObject();
        } else if (mBookObject != null) {
            changeImageOfBook();
        }
    }

    public void resetIcon() {
        ObjectInfo info = getObjectInfo();
        info.mShortcutIcon = null;
        info.updateToDB(true, false, false);
        if (mIconObject != null) {
            createIconObject();
        } else if (mIconBoxObject != null) {
            changeImageOfIconBox();
        } else if (mBookObject != null) {
            changeImageOfBook();
        }
    }

    @Override
    public void updateComponentName(ComponentName name) {
        super.updateComponentName(name);
        if (getObjectInfo().mComponentName != null) {
            if (mIconBoxObject != null) {
                changeImageOfIconBox();
            } else if (mBookObject != null) {
                changeImageOfBook();
            }
            if (!AppObject.isSysApp(getObjectInfo().mComponentName)) {
                setCanUninstall(true);
            } else {
                setCanUninstall(false);
            }
        }
    }

    private void startApplicationUninstallActivity(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        String className = componentName.getClassName();
        if (isSysApp(componentName)) {
            // System applications cannot be installed. For now, show a
            // toast explaining that.
            final int messageId = R.string.uninstall_system_app_text;
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    Toast.makeText(HomeManager.getInstance().getContext(), messageId, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            HomeManager.getInstance().startActivity(intent);
        }
    }

    public static boolean isSysApp(ComponentName componentName) {
        if (componentName == null) {
            return true;
        }
        ItemInfo itemInfo = LauncherModel.getInstance().findAppItem(componentName);
        if (itemInfo != null) {
            return itemInfo.mIsSysApp;
        }
        return false;
    }

    private boolean mOnCreateIcon = false;

    private void createIconObject() {
        mOnCreateIcon = true;
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                ResolveInfo resolveInfo = getObjectInfo().getResolveInfo();
                Drawable icon = null;
                if (getObjectInfo().mShortcutIcon == null) {
                    icon = HomeUtils.getAppIcon(getContext(), resolveInfo);
                }
                String label = null;
                if (mNeedShowLable) {
                    label = getDisplayLabel(resolveInfo);
                }
                Bitmap iconWithText = HomeUtils.getAppIconBitmap(mHouse, getObjectInfo().mShortcutIcon, icon,
                        label, true);
                final Bitmap iconImage = Bitmap.createScaledBitmap(iconWithText, 128, 128, true);
                new SECommand(getScene()) {
                    public void run() {
                        mOnCreateIcon = false;
                        if (mIconObject != null) {
                            removeComponenetObject(mIconObject, true);
                            mIconObject = null;
                        }
                        if (!hasBeenReleased()) {
                            if (isOnMove() || getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_WALL
                                    || getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_APP_WALL) {
                                mIconObject = new SEImageView(getScene(), mName + "_icon");
                                mIconObject.setSize(mHouse.mCellWidth, mHouse.mCellHeight);
                                mIconObject.setBackgroundBitmap(iconImage);
                                mIconObject.setBlendSortAxis(AXIS.Y);
                                addComponenetObject(mIconObject, true);
                                if (getObjectInfo().mSlotType != ObjectInfo.SLOT_TYPE_WALL
                                        && getObjectInfo().mSlotType != ObjectInfo.SLOT_TYPE_APP_WALL) {
                                    mIconObject.setVisible(false);
                                }
                            }
                        }
                    }
                }.execute();
            }
        });

    }

    private boolean mOnCreateIconBox = false;
    private String mImageName;
    private String mImagePath;

    private void createIconBoxObject() {
        mOnCreateIconBox = true;
        ObjectInfo objInfo = new ObjectInfo();
        objInfo.setModelInfo(HomeManager.getInstance().getModelManager().findModelInfo("group_iconbox"));
        mIconBoxObject = new NormalObject(getHomeScene(), objInfo.mName, INDEX_OF_ICON_BOX);
        INDEX_OF_ICON_BOX++;
        mIconBoxObject.setObjectInfo(objInfo);

        HomeManager.getInstance().getModelManager().create(this, mIconBoxObject, new Runnable() {

            @Override
            public void run() {
                mOnCreateIconBox = false;
                if (!hasBeenReleased()) {
                    if (isOnMove() || getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                        mIconBoxObject.initStatus();
                        mIconBoxObject.setBlendSortAxis(AXIS.Z);
                        mIconBoxObject.setIsEntirety(false);
                        mIconBoxObject.setClickable(false);
                        SEObject face = mIconBoxObject.findComponenetObjectByRegularName("face");
                        mImageName = face.getImageName();
                        mImagePath = mIconBoxObject.getObjectInfo().mModelInfo.mAssetsPath;
                        changeImageOfIconBox();
                        if (getObjectInfo().mSlotType != ObjectInfo.SLOT_TYPE_DESKTOP) {
                            mIconBoxObject.setVisible(false);
                        }
                    }

                }
            }
        });
    }

    private static Bitmap mIconBoxFaceBackground;

    private void changeImageOfIconBox() {
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                try {
                    if (mIconBoxObject == null || !mIconBoxObject.hasBeenAddedToEngine() || mImagePath == null) {
                        return;
                    }
                    if (mIconBoxFaceBackground == null) {
                        String imageAssetsPath = mImagePath.substring(7) + File.separator + "wall03.png";
                        InputStream is = getContext().getAssets().open(imageAssetsPath);
                        mIconBoxFaceBackground = BitmapFactory.decodeStream(is);
                        is.close();
                    }

                    Bitmap background = mIconBoxFaceBackground.copy(Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(background);
                    if (getObjectInfo().mShortcutIcon != null) {
                        Rect iconSrc = new Rect(0, 0, getObjectInfo().mShortcutIcon.getWidth(),
                                getObjectInfo().mShortcutIcon.getHeight());
                        Rect iconDes = new Rect(0, 0, 128, 128);
                        canvas.drawBitmap(getObjectInfo().mShortcutIcon, iconSrc, iconDes, new Paint());
                    } else if (getObjectInfo().mComponentName != null) {
                        ResolveInfo resolveInfo = getObjectInfo().getResolveInfo();
                        Drawable drawableIcon = HomeUtils.getAppIcon(getContext(), resolveInfo);
                        Rect oldBounds = drawableIcon.copyBounds();
                        drawableIcon.setBounds(0, 0, 128, 128);
                        drawableIcon.draw(canvas);
                        drawableIcon.setBounds(oldBounds);
                    }
                    final int imageData = SEObject.loadImageData_JNI(background);
                    background.recycle();
                    new SECommand(getScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mImageName, mImageName);
                            SEObject.addImageData_JNI(mImageName, imageData);
                        }
                    }.execute();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean mOnCreateBook = false;

    private void createBookObject() {
        mOnCreateBook = true;
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                mOnCreateBook = false;
                mBookObject = new Book(getScene(), mName + "_book");
                new SECommand(getScene()) {
                    public void run() {
                        if (!hasBeenReleased()) {
                            if (isOnMove() || getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
                                addComponenetObject(mBookObject, true);
                                if (getObjectInfo().mSlotType != ObjectInfo.SLOT_TYPE_BOOKSHELF) {
                                    mBookObject.setVisible(false);
                                }
                            }
                        }
                    }
                }.execute();
            }
        });
    }

    private String getDisplayLabel(ResolveInfo resolveInfo) {
        String label = getObjectInfo().mDisplayName;
        if (label == null) {
            label = HomeUtils.getAppLabel(getContext(), resolveInfo);
        }
        if (label == null && getObjectInfo().mComponentName != null) {
            label = getObjectInfo().mComponentName.getPackageName();
        }
        return label;
    }

    private void changeImageOfBook() {
        if (mBookObject != null) {
            mBookObject.changeImageOfFace();
        }
    }

    private class Book extends SEObject {
        private float[] mVector;

        public Book(SEScene scene, String name) {
            super(scene, name);
            Bitmap faceImage = null;
            try {
                faceImage = createFace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            float w = mHouse.mCellWidth;
            // 一般书本的长宽比例大约为根号2也就是约为1.414
            float h = mHouse.mCellWidth * 1.414f;
            mVector = new float[] { -w / 2, 0, -h / 2f, w / 2, 0, -h / 2f, w / 2, 0, h / 2f, -w / 2, 0, h / 2f, -w / 2,
                    -w / 10, -h / 2f, w / 2, -w / 10, -h / 2f, w / 2, -w / 10, h / 2f, -w / 2, -w / 10, h / 2f };
            setVertexArray(mVector);
            setFaceArray(new int[] { 2, 1, 0, 0, 3, 2, 3, 0, 4, 4, 7, 3, 4, 5, 6, 6, 7, 4 });
            setTexVertexArray(new float[] { 10 / 21f, 0, 0, 0, 0, 1, 10 / 21f, 1, 11 / 21f, 0, 1, 0, 1, 1, 11 / 21f, 1 });
            setImage(IMAGE_TYPE.BITMAP, mName + "_imageName", mName + "_imageKey");
            setBitmap(faceImage);
            setUserRotate(new SERotate(-25, 1, 0, 0));
            setBlendSortAxis(AXIS.Y);
        }

        public void changeImageOfFace() {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    try {
                        if (!Book.this.hasBeenAddedToEngine() || Book.this.hasBeenReleased()) {
                            return;
                        }
                        Bitmap bitmap = createFace();
                        final int imageData = SEObject.loadImageData_JNI(bitmap);
                        new SECommand(getScene()) {
                            public void run() {
                                SEObject.addImageData_JNI(mName + "_imageKey", imageData);
                            }
                        }.execute();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }

        private Bitmap createFace() throws IOException {
            ResolveInfo resolveInfo = getObjectInfo().getResolveInfo();
            Bitmap bitmapIcon = getObjectInfo().mShortcutIcon;
            if (bitmapIcon == null && getObjectInfo().mComponentName != null) {
                Drawable drawableIcon = HomeUtils.getAppIcon(getContext(), resolveInfo);
                if (drawableIcon instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawableIcon;
                    bitmapIcon = bitmapDrawable.getBitmap();
                }
            }
            int averageColor = HomeUtils.getAverageColorOfBitmap(bitmapIcon, 10, 10);

            Bitmap background = Bitmap.createBitmap(256, 164, Config.ARGB_8888);
            Canvas canvas = new Canvas(background);
            // 画书的背面贴图
            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            Rect rect = new Rect(0, 0, 10 * 256 / 21, 172);
            canvas.drawRect(rect, paint);
            // 画书的侧面贴图
            rect = new Rect(10 * 256 / 21, 0, 11 * 256 / 21, 172);
            paint.setColor(Color.GRAY);
            canvas.drawRect(rect, paint);
            // 画书的证么背景颜色
            rect = new Rect(11 * 256 / 21, 0, 256, 172);
            paint.setColor(averageColor);
            canvas.drawRect(rect, paint);

            canvas.save();
            float beginX = 0.5238f * background.getWidth();
            int w = (int) (background.getWidth() - beginX);
            int lableW = w - 6;
            int iconW = w - 30;
            canvas.translate(beginX, 0);
            String label = getObjectInfo().mDisplayName;

            if (label == null) {
                label = HomeUtils.getAppLabel(getContext(), resolveInfo);
            }
            if (label == null && getObjectInfo().mComponentName != null) {
                label = getObjectInfo().mComponentName.getPackageName();
            }
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(26);
            averageColor = Color.argb(Color.alpha(averageColor), 255 - Color.red(averageColor),
                    255 - Color.green(averageColor), 255 - Color.blue(averageColor));
            textPaint.setColor(averageColor);
            textPaint.setAntiAlias(true);
            // textPaint.setFakeBoldText(true);
            textPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
            StaticLayout titleLayout = new StaticLayout(label, textPaint, lableW, Alignment.ALIGN_CENTER, 1f, 0.0F,
                    false);
            int lineCount = titleLayout.getLineCount();
            if (lineCount > 1) {
                float allowW = lableW;
                int index = 0;
                String newlabel = null;
                float newLableW;
                while (true) {
                    index++;
                    if (index > label.length()) {
                        break;
                    }
                    newlabel = label.substring(0, index) + "...";
                    newLableW = StaticLayout.getDesiredWidth(newlabel, textPaint);
                    if (newLableW > allowW) {
                        newlabel = label.substring(0, --index) + "...";
                        break;
                    }
                }
                titleLayout = new StaticLayout(newlabel, textPaint, lableW, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
            }
            canvas.save();
            canvas.translate(3, 15);
            titleLayout.draw(canvas);
            canvas.restore();
            canvas.save();
            canvas.translate(15, 60);
            if (bitmapIcon != null) {
                Rect iconSrc = new Rect(0, 0, bitmapIcon.getWidth(), bitmapIcon.getHeight());
                Rect iconDes = new Rect(0, 0, iconW, iconW);
                canvas.drawBitmap(bitmapIcon, iconSrc, iconDes, new Paint());
            } else if (getObjectInfo().mComponentName != null) {
                Drawable drawableIcon = HomeUtils.getAppIcon(getContext(), resolveInfo);
                Rect oldBounds = drawableIcon.copyBounds();
                drawableIcon.setBounds(0, 0, iconW, iconW);
                drawableIcon.draw(canvas);
                drawableIcon.setBounds(oldBounds);
            }
            canvas.restore();
            canvas.restore();

            return background;
        }
    }
}
