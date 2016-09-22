package com.borqs.se.widget3d;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.borqs.digitalframe.FrameSettingPrefManager;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.shortcut.LauncherModel;

public class WallFrame2 extends WallObject {
    protected SEObject mPicture;
    protected SEObject mFrame;
    private int mImageWidth;
    private int mImageHeight;
    private int mFrameWidth;
    private int mFrameHeight;
    private String mImageName;
    private String mImageKey;
    private String mFrameName;
    private String mFrameKey;
    private boolean mEnableChangePhoto = false;

    private Context mContext;
    private String mAlbumDir;
    private int mAlbumType;//0,single file;  1,folder; 2,all
    private int mFrameMeterial;
    private int mInterval;
    private int mFrameLayoutID;

    private ArrayList<String> mPaths = new ArrayList<String>();
    private ArrayList<String> mTempPaths = new ArrayList<String>();
    private int mCurrentPic = 0;
    private long mLastDrawPhotoTime = System.currentTimeMillis();

    private boolean mNeedShowDefaultImg = false;
    private String mResourcePath;
    private House mHouse;
    
    public WallFrame2(HomeScene scene, String name, int index) {
        super(scene, name, index);

        // 加入是否需要显示设置按钮
        mImageWidth = 0;
        mImageHeight = 0;
        mFrameWidth = 0;
        mFrameHeight = 0;
        mContext = getContext();

    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        setCanBeResized(true);
        setCanChangeSetting(true);
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        mResourcePath = getObjectInfo().mModelInfo.mAssetsPath;
        mPicture = findComponenetObjectByRegularName("picture");
        mFrame = findComponenetObjectByRegularName("xiangkuangxin_frame");
        String imageSizeX = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "imageSizeX");
        if (!TextUtils.isEmpty(imageSizeX)) {
            mImageWidth = Integer.parseInt(imageSizeX);
        }
        String imageSizeY = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "imageSizeY");
        if (!TextUtils.isEmpty(imageSizeY)) {
            mImageHeight = Integer.parseInt(imageSizeY);
        }
        String frameSizeX = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "frameSizeX");
        if (!TextUtils.isEmpty(frameSizeX)) {
            mFrameWidth = Integer.parseInt(frameSizeX);
        }
        String frameSizeY = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "frameSizeY");
        if (!TextUtils.isEmpty(frameSizeY)) {
            mFrameHeight = Integer.parseInt(frameSizeY);
        }
        mImageName = mPicture.getImageName();
        mImageKey = SEObject.getImageKey_JNI(mImageName);
        mFrameName = mFrame.getImageName();
        mFrameKey = SEObject.getImageKey_JNI(mFrameName);

        resizeFrame();
        mEnableChangePhoto = true;
        mFrameID = mName + "_"+this.getScene().getSceneName()+"_" + mIndex;

        initPref();
        registerChangeFramePhotoListener(mFrameID);
        drawFrameMeterial();
        LauncherModel.getInstance().removeTask(mPlayCycle);
        LauncherModel.getInstance().process(mPlayCycle, 2000);
    }

    private String mFrameID;
    private int mCurrentPhotoType;
    private List<String> mCurrentPhotos;

    private void registerChangeFramePhotoListener(String frameID) {
        IntentFilter intentFilter = new IntentFilter(HomeUtils.PKG_CURRENT_NAME + ".ALBUMCHANGED");
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED); 
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private void unRegisterChangeFramePhotoListener(String frameID) {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
        }
    }

    private boolean notifyChangeFramePhoto(int photoType, List<String> photos) {
        mCurrentPhotoType = photoType;
        mCurrentPhotos = photos;
        return changeFramePhoto();
    }

    /**
     * 变换相片
     */
    private boolean changeFramePhoto() {
        if (mImageKey != null && mEnableChangePhoto) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {

                    Bitmap photo = getFramePhoto(mCurrentPhotoType, mCurrentPhotos);
                    if (photo != null) {
                        int data= -1;
                        if (mNeedShowDefaultImg) {
                            data = SEObject
                                    .loadImageData_JNI(mResourcePath + File.separator +"xiangpian.jpg");
                        } else {
                            data = SEObject.loadImageData_JNI(photo);
                        }
                        final int imageData = data;
                        if (imageData != 0) {
                            new SECommand(getScene()) {
                                public void run() {
                                    SEObject.applyImage_JNI(mImageName, mImageKey + "_" + mIndex);
                                    SEObject.addImageData_JNI(mImageKey + "_" + mIndex, imageData);
                                }
                            }.execute();
                        }
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param type
     *            图片的排布方式
     * @param photos
     *            当前需要的1-4张图片
     * @return 获取生成的图片来替换当前的相片,图片大小为512*512 图片有效区域来自配置文件special_config.xml
     */
    private Bitmap getFramePhoto(int type, List<String> photos) {
        Bitmap textureBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(textureBitmap);
        canvas.save();
        canvas.translate((512 - mImageWidth) / 2, (512 - mImageHeight) / 2);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        switch (type) {
        case 0:
            canvas.drawColor(Color.YELLOW);
            canvas.drawRect(new Rect(0, 0, mImageWidth, mImageHeight), paint);
            canvas.drawLine(0, mImageHeight / 2, mImageWidth, mImageHeight / 2, paint);
            canvas.drawLine(mImageWidth / 2, 0, mImageWidth / 2, mImageHeight, paint);
            break;

        case 1:
            drawFramePhoto(canvas,photos);
            break;
        }
        canvas.restore();
        return textureBitmap;
    }

    private boolean drawBitmap(String fileName, int w, int h, Paint paint,Canvas c,int left, int top){
        Bitmap b = getImageThumbnail(mContext,fileName,w,h);
        boolean success= false;
        if(b ==null){
            c.drawColor(Color.YELLOW);
            c.drawRect(new Rect(left, top, w, h), paint);
            success = false;
        }else{
            c.drawBitmap(b, left, top, null);
            b.recycle();
            success = true;
        }
        return success;
    }

    private void drawFramePhoto(Canvas canvas, List<String> photos) {
        if (photos == null || photos.size() == 0) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

        if (mAlbumType == 0) {
            boolean b1 = drawBitmap(photos.get(0), mImageWidth, mImageHeight,
                    paint, canvas, 0, 0);
            if (!b1) {
                // if draw failed, delay 5000ms,draw next pic
                LauncherModel.getInstance().removeTask(mPlayCycle);
                LauncherModel.getInstance().process(mPlayCycle, 5000);
                mNeedShowDefaultImg = true;
            }else{
                mNeedShowDefaultImg = false;
            }
        } else {
            switch (this.mFrameLayoutID) {
            case 0:
                boolean b00 = drawBitmap(photos.get(0), mImageWidth,
                        mImageHeight, paint, canvas, 0, 0);
                if (!b00) {
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    LauncherModel.getInstance().process(mPlayCycle, 5000);
                    mNeedShowDefaultImg = true;
                }else{
                    mNeedShowDefaultImg = false;
                }
                break;
            case 1:
                boolean b10 = drawBitmap(photos.get(0), mImageWidth,
                        mImageHeight / 2, paint, canvas, 0, 0);
                boolean b11 = drawBitmap(photos.get(1), mImageWidth,
                        mImageHeight / 2, paint, canvas, 0, mImageHeight / 2);
                if ((!b10) && (!b11)) {
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    LauncherModel.getInstance().process(mPlayCycle, 5000);
                    mNeedShowDefaultImg = true;
                }else{
                    mNeedShowDefaultImg = false;
                }

                canvas.drawLine(0, mImageHeight / 2, mImageWidth,
                        mImageHeight / 2, paint);
                break;
            case 2:
                boolean b20 = drawBitmap(photos.get(0), mImageWidth,
                        mImageHeight / 2, paint, canvas, 0, 0);
                boolean b21 = drawBitmap(photos.get(1), mImageWidth / 2,
                        mImageHeight / 2, paint, canvas, 0, mImageHeight / 2);
                boolean b22 = drawBitmap(photos.get(2), mImageWidth / 2,
                        mImageHeight / 2, paint, canvas, mImageWidth / 2,
                        mImageHeight / 2);
                if ((!b20) && (!b21) && (!b22)) {
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    LauncherModel.getInstance().process(mPlayCycle, 5000);
                    mNeedShowDefaultImg = true;
                }else{
                    mNeedShowDefaultImg = false;
                }
                canvas.drawLine(0, mImageHeight / 2, mImageWidth,
                        mImageHeight / 2, paint);
                canvas.drawLine(mImageWidth / 2, mImageHeight / 2,
                        mImageWidth / 2, mImageHeight, paint);
                break;
            case 3:
                boolean b30 = drawBitmap(photos.get(0), mImageWidth / 2,
                        mImageHeight / 2, paint, canvas, 0, 0);
                boolean b31 = drawBitmap(photos.get(1), mImageWidth / 2,
                        mImageHeight / 2, paint, canvas, mImageWidth / 2, 0);
                boolean b32 = drawBitmap(photos.get(2), mImageWidth,
                        mImageHeight / 2, paint, canvas, 0, mImageHeight / 2);
                if ((!b30) && (!b31) && (!b32)) {
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    LauncherModel.getInstance().process(mPlayCycle, 5000);
                    mNeedShowDefaultImg = true;
                }else{
                    mNeedShowDefaultImg = false;
                }

                canvas.drawLine(0, mImageHeight / 2, mImageWidth,
                        mImageHeight / 2, paint);
                canvas.drawLine(mImageWidth / 2, 0, mImageWidth / 2,
                        mImageHeight / 2, paint);
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            }
        }
        mLastDrawPhotoTime = System.currentTimeMillis();
    }

    private void drawFrameMeterial(){
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                int imageData=SEObject.loadImageData_JNI(mResourcePath + File.separator + "xiangkuangxin_hei.jpg");
                switch(mFrameMeterial){
                case FrameSettingPrefManager.BLACK:
                    imageData = SEObject.loadImageData_JNI(mResourcePath + File.separator + "xiangkuangxin_hei.jpg");
                    break;
                case FrameSettingPrefManager.WHITE:
                    imageData = SEObject.loadImageData_JNI(mResourcePath + File.separator + "xiangkuangxin_bai.jpg");
                    break;
                case FrameSettingPrefManager.WOOD:
                    imageData = SEObject.loadImageData_JNI(mResourcePath + File.separator + "xiangkuangxin_wood.jpg");
                    break;
                case FrameSettingPrefManager.METAL:
                    imageData = SEObject.loadImageData_JNI(mResourcePath + File.separator + "xiangkuangxin_jinshu.jpg");
                    break;
                }

                final int data = imageData;
                if (imageData != 0) {
                    new SECommand(getScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mFrameName, mFrameKey + "_" + mIndex);
                            SEObject.addImageData_JNI(mFrameKey + "_" + mIndex, data);
                        }
                    }.execute();
                }
            }
        });
    }

    public Bitmap getImageThumbnail(Context context, String fileName,int width, int height) {
        ContentResolver cr = context.getContentResolver();
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String whereClause = "upper(" + MediaStore.Images.ImageColumns.DATA
                + ") = '" + fileName.toUpperCase() + "'";
        Cursor cursor = null;
        try {
            cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.ORIENTATION }, whereClause,
                    null, null);
        } catch (Exception ex) {
        }
        if (cursor == null)
            return null;
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        String imageId = cursor.getString(cursor
                .getColumnIndex(MediaStore.Images.Media._ID));
        int orientation = cursor.getInt(cursor
                .getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        if (imageId == null) {
            cursor.close();
            return null;
        }
        cursor.close();
        long imageIdLong = Long.parseLong(imageId);

        Uri uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() +"/"+ imageIdLong);

        bitmap = HomeUtils.decodeSampledBitmapFromResource(context, uri, width, height);

        if (orientation > 0) {
            bitmap = rotateBitmap(bitmap, orientation);
        }
        if(bitmap == null) return null;
        int bw=bitmap.getWidth();
        int bh=bitmap.getHeight();
        Bitmap tmp = null;
        int x;//left
        int y;//top
        float scaleX = (float)width/bw;
        float scaleY = (float)height/bh;

        Matrix matrix = new Matrix(); 
        try {
            if(width<bw || height <bh){
                if(scaleX < scaleY){
                    matrix.postScale(scaleY, scaleY); 
                    tmp = Bitmap.createBitmap(bitmap, 0, 0,bw, bh, matrix, true);
                    x = (tmp.getWidth() - width-10)/2;
                    y =0;
                }else{
                    matrix.postScale(scaleX, scaleX); 
                    tmp = Bitmap.createBitmap(bitmap, 0, 0,bw, bh, matrix, true);
                    x = 0;
                    y =(tmp.getHeight()-height-10)/2;
                }
                tmp = Bitmap.createBitmap(tmp, x, y, width, height); 
            }else{
                tmp = Bitmap.createScaledBitmap(bitmap, width, height, false);
            }

            bitmap = tmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    private static Bitmap rotateBitmap(Bitmap b, int orientation) {
        if(b == null){
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(1, 1);
        switch (orientation) {
        case 90:
            matrix.setRotate(90);
            break;
        case 270:
            matrix.setRotate(270);
            break;
        case 180:
            matrix.setRotate(180);
            break;
        default:
            break;
        }
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                matrix, true);
    }

    @Override
    public void onSlotSuccess() {
        super.onSlotSuccess();
        if (isFresh()) {
            // 首次添加相框时自动进入设置界面
            String prefName = FrameSettingPrefManager.getFramePrefByFrameID(mContext, mFrameID);
            if(TextUtils.isEmpty(prefName)){
                FrameSettingPrefManager.putFrameMap(mContext,mFrameID, FrameSettingPrefManager.FRAME_SETTING_PRE+mFrameID);
                Intent intent = new Intent();
                intent.setAction("com.borqs.gallery.EditPhoto");
                intent.putExtra("frameID", this.mFrameID);
                intent.putExtra("image_width", this.mImageWidth);
                intent.putExtra("image_height", this.mImageHeight);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        }
    }

    @Override
    public void onSettingClick(){
        super.onSlotSuccess();
        Intent intent = new Intent();
        intent.setAction("com.borqs.gallery.EditPhoto");
        intent.putExtra("frameID", this.mFrameID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * 点击事件在此处加入
     */
    @Override
    public void handOnClick() {
        //点击进入Gallery
    }

    public void onWallRadiusChanged(int faceIndex) {
        if (isPressed()) {
            return;
        }

        long tempInterval = getIntervalTimeAfterWallChange(faceIndex);

        if (faceIndex == getObjectInfo().getSlotIndex()) {
            mEnableChangePhoto = true;
            LauncherModel.getInstance().removeTask(mPlayCycle);
            LauncherModel.getInstance().process(mPlayCycle,tempInterval);
        } else {
            mEnableChangePhoto = false;
        }
    }

    private long getIntervalTimeAfterWallChange(int faceIndex) {
        String prefName = FrameSettingPrefManager.getFramePrefByFrameID(
                mContext, mFrameID);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                prefName, Context.MODE_MULTI_PROCESS);
        Editor editor = sharedPreferences.edit();

        long tempInterval = 0;
        long usedTime = 0;
        long currentTime = System.currentTimeMillis();

        usedTime = currentTime
                - sharedPreferences.getLong("last_wall_radius_change_time",
                        0);

        editor.putLong("last_wall_radius_change_time", mLastDrawPhotoTime);
        editor.commit();

        tempInterval = this.getIntervalTime() - usedTime;
        if (tempInterval < 0)
            tempInterval = 5;
        return tempInterval;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onSizeAndPositionChanged(Rect sizeRect) {
        getObjectSlot().mSpanX = sizeRect.width();
        getObjectSlot().mSpanY = sizeRect.height();
        getObjectSlot().mStartX = sizeRect.left;
        getObjectSlot().mStartY = sizeRect.top;
        getObjectInfo().updateSlotDB();
        resizeFrame();
        if (getParent() != null && (getParent() instanceof VesselObject)) {
            VesselObject vessel = (VesselObject) getParent();
            SETransParas transParas = vessel.getTransParasInVessel(this, getObjectSlot());
            if (transParas != null) {
                getUserTransParas().set(transParas);
                setUserTransParas();
            }
        }
    }

    private void resizeFrame() {
        float scale = getScale(getObjectSlot().mSpanX, getObjectSlot().mSpanY);
        SEObject model = getModelComponenet();
        model.setUserScale(new SEVector3f(scale, 1, scale));
    }

    public float getScale(int spanX, int spanY) {
        float newW = getWidthOfWidget(spanX);
        float newH = getHeightOfWidget(spanY);
        float scale = Math.min(newW / mFrameWidth, newH / mFrameHeight);
        return scale;
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (getHomeScene().getDragLayer().isOnResize()) {
            getHomeScene().getDragLayer().finishResize();
            return true;
        }
        return false;
    }

    @Override
    public void onRelease() {
        super.onRelease();

        unRegisterChangeFramePhotoListener(mFrameID);
        String prefName = FrameSettingPrefManager.getFramePrefByFrameID(mContext, mFrameID);
        if(!TextUtils.isEmpty(prefName)){
            FrameSettingPrefManager.removeFramePrefByFrameID(mContext, mFrameID);
        }
        LauncherModel.getInstance().removeTask(mPlayCycle);
        deletePrefAfterRemoveWallframe(prefName);
    }

    private void deletePrefAfterRemoveWallframe(String prefName){
        File file = new File("/data/data/com.borqs.se/shared_prefs/"+prefName+".xml");
        if(file.exists()){
            file.delete();
        }
    }

    private float getWidthOfWidget(float spanX) {
        return mHouse.getWallCellWidth(spanX);
    }

    private float getHeightOfWidget(float spanY) {
        return mHouse.getWallCellHeight(spanY);
    }

    private void initPref() {
        String prefName = FrameSettingPrefManager.getFramePrefByFrameID(mContext, mFrameID);
        SharedPreferences settings = mContext.getSharedPreferences(prefName, Context.MODE_MULTI_PROCESS);

        this.mAlbumDir = settings.getString(FrameSettingPrefManager.FOLDER_OR_FILE_PATH_KEY, "");
        this.mAlbumType = settings.getInt(FrameSettingPrefManager.ALBUM_TYPE, 0);
        this.mFrameMeterial = settings.getInt(FrameSettingPrefManager.FRAME_MERERIAL_KEY, FrameSettingPrefManager.WOOD);
        this.mInterval = settings.getInt(FrameSettingPrefManager.INTERVAL_KEY, 0);
        this.mFrameLayoutID = settings.getInt(FrameSettingPrefManager.LAYOUT_KEY, 0);

        initpaths();
    }

    private long getIntervalTime(){
        long intervalTime = 5*60*1000;
        switch(mInterval){
        case 0:
            //5 min
            intervalTime = 5*60*1000;
            break;
        case 1:
            //10 min
            intervalTime = 10*60*1000;
            break;
        case 2:
            //15 min
            intervalTime = 15*60*1000;
            break;
        }
        return intervalTime;
    }

    private int getTempPathLength(){
        //refer photo frame UI design
        int tempPathLength = 1;
        switch(this.mFrameLayoutID){
        case 0:
            tempPathLength = 1;
            break;
        case 1:
            tempPathLength = 2;
            break;
        case 2:
            tempPathLength = 3;
            break;
        case 3:
            tempPathLength = 3;
            break;
        case 4:
            tempPathLength = 2;
            break;
        case 5:
            tempPathLength = 3;
            break;
        case 6:
            tempPathLength = 3;
            break;
        case 7:
            tempPathLength = 3;
            break;
        case 8:
            tempPathLength = 2;
            break;
        case 9:
            tempPathLength = 3;
            break;
        case 10:
            tempPathLength = 2;
            break;
        }
        return tempPathLength;
    }

    private void resetTempPaths(){
        int tempPathLength = getTempPathLength();
        for(int i = 0; i <tempPathLength; i++ ){
            if(mCurrentPic>= mPaths.size()){
                mCurrentPic = 0;
            }
            mTempPaths.add(mPaths.get(mCurrentPic));
            mCurrentPic++;
        }
    }

    Runnable mPlayCycle = new Runnable() {
        public void run() {
            mTempPaths.clear();
            if(mPaths.size() == 0) return;
            resetTempPaths();
            notifyChangeFramePhoto(1, mTempPaths);
            boolean hasProcessFinish = false;
            if(!hasProcessFinish){
                //mAlbumType=0, single picture; mAlbumType=1,folder; mAlbumType=2,all picture on phone
                LauncherModel.getInstance().removeTask(mPlayCycle);
                LauncherModel.getInstance().process(mPlayCycle, getIntervalTime());
                if(mAlbumType == 0){
                    hasProcessFinish = true;
                }
            }
        }
    };

    private void initpaths(){
        mPaths.clear();
        switch(mAlbumType){
        case 0:
            //single file
            mPaths.add(this.mAlbumDir);
            break;
        case 1:
        case 2:
            if(!TextUtils.isEmpty(this.mAlbumDir)){
                String[] paths  = this.mAlbumDir.split(";");
                for(int i=0;i<paths.length;i++){
                    mPaths.add(paths[i]);
                }
            }
            break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(HomeUtils.PKG_CURRENT_NAME + ".ALBUMCHANGED")
                    || action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                initPref();
                drawFrameMeterial();
                mCurrentPic = 0;
                LauncherModel.getInstance().removeTask(mPlayCycle);
                LauncherModel.getInstance().process(mPlayCycle, 5);
            } else if(action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_EJECT)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                LauncherModel.getInstance().removeTask(mPlayCycle);
            } else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
                //waiting for scan finish
            }
        }

    };

}
