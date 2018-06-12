package com.borqs.digitalframe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.borqs.digitalframe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class Setting extends Activity{

    public static final int REQUEST_CODE_IMAGE = 10001;
    public static final int REQUEST_CODE_IMAGE_FOLDER = REQUEST_CODE_IMAGE+1;
    public static final int REQUEST_CODE_IMAGE_ALL = REQUEST_CODE_IMAGE_FOLDER+1;
    public static final int MATERIAL_LIST_SIZE = 4;
    private String[] mMaterialName = {
            "material_black.png",
            "material_white.png",
            "material_wood.png",
            "material_metal.png"
    };

    public static final int LAYOUT_LIST_SIZE = 4;
    private String[] mBlackLayoutName = {
            "layout1_black_1.png",
            "layout1_black_2.png",
            "layout1_black_3.png",
            "layout1_black_4.png"
    };

    private String[] mWhiteLayoutName = {
            "layout1_white_1.png",
            "layout1_white_2.png",
            "layout1_white_3.png",
            "layout1_white_4.png"
    };

    private String[] mWoodLayoutName = {
            "layout1_wood_1.png",
            "layout1_wood_2.png",
            "layout1_wood_3.png",
            "layout1_wood_4.png"
    };

    private String[] mMetalLayoutName = {
            "layout1_metal_1.png",
            "layout1_metal_2.png",
            "layout1_metal_3.png",
            "layout1_metal_4.png"
    };

    private TextView mChangeAlbum;
    private TextView mFrameMeterial;
    private TextView mInterval;
    private TextView mFrameLayout;

    private ImageView mLayoutSetting0;
    private ImageView mLayoutSetting1;
    private ImageView mLayoutSetting2;
    private ImageView mLayoutSetting3;

    private LinearLayout mIntervalSetting0;
    private LinearLayout mIntervalSetting1;
    private LinearLayout mIntervalSetting2;
    private ImageButton mIntervalSelected0;
    private ImageButton mIntervalSelected1;
    private ImageButton mIntervalSelected2;

    private LinearLayout mAlbumSetting0;
    private LinearLayout mAlbumSetting1;
    private LinearLayout mAlbumSetting2;
    private ImageButton mAlbumSelected0;
    private ImageButton mAlbumSelected1;
    private ImageButton mAlbumSelected2;

    private ImageButton mFrameMeterial0;
    private ImageButton mFrameMeterial1;
    private ImageButton mFrameMeterial2;
    private ImageButton mFrameMeterial3;
    private ImageButton mFrameMeterialSelected0;
    private ImageButton mFrameMeterialSelected1;
    private ImageButton mFrameMeterialSelected2;
    private ImageButton mFrameMeterialSelected3;

    public ImageView mDot1;
    public ImageView mDot2;

    public ArrayList<ImageView> mFrameLayoutList = new ArrayList<ImageView>();
    public ArrayList<Drawable> mLayoutBlackDrawable = new ArrayList<Drawable>();
    public ArrayList<Drawable> mLayoutWoodDrawable = new ArrayList<Drawable>();
    public ArrayList<Drawable> mLayoutMetalDrawable = new ArrayList<Drawable>();
    public ArrayList<Drawable> mLayoutWhiteDrawable = new ArrayList<Drawable>();
    public ArrayList<Drawable> mMeterialDrawable = new ArrayList<Drawable>();
    public ViewPager mLayoutAndNumViewPager;
    private LayoutAndNumPagerAdapter mLayoutAndNumPagerAdapter;

    private LayoutInflater mInflater;
    private List<View> mListViews;
    int mFrameMaterial = FrameSettingPrefManager.WOOD;
    int mNumAndLayout = 0;
    int mIntervalTime = 0;
    int mAlbumType  = 0; 

    private SharedPreferences mSharedPreferences;
    private Editor mEditor;

    private String mFrameID;
    private int mImageWidth;
    private int mImageHeight;

//    private Handler mHandler = new Handler(){ss
//        @Override
//        public void handleMessage(Message msg){
//            
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        FrameSettingPrefManager.initContext(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mFrameID = bundle.getString("frameID");
            mImageWidth = bundle.getInt("image_width");
            mImageHeight = bundle.getInt("image_height");
        }
        String prefName = FrameSettingPrefManager.getFramePrefByFrameID(this, this.mFrameID);
        if(TextUtils.isEmpty(prefName)){
            prefName = FrameSettingPrefManager.FRAME_SETTING_PRE+mFrameID;
        }
        mSharedPreferences = getSharedPreferences(prefName,Context.MODE_MULTI_PROCESS);
        mEditor = mSharedPreferences.edit();
        getPrefs();
        initMaterialDrawbleList();
        initFrameMeterialDrawableList();
        initRes();
    }

    private void initRes() {
        initAlbum();
        initInterval();
        initFrameMeterial();
        initLayoutSetting();
    }

    private void initAlbum(){
        mChangeAlbum = (TextView) findViewById(R.id.change_picture);

        mAlbumSetting0 = (LinearLayout)this.findViewById(R.id.album0);
        mAlbumSetting1 = (LinearLayout)this.findViewById(R.id.album1);
        mAlbumSetting2 = (LinearLayout)this.findViewById(R.id.album2);
        mAlbumSelected0 = (ImageButton)this.findViewById(R.id.album_btn0);
        mAlbumSelected1 = (ImageButton)this.findViewById(R.id.album_btn1);
        mAlbumSelected2 = (ImageButton)this.findViewById(R.id.album_btn2);

        mAlbumSetting0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlbumSelected0.setBackgroundResource(R.drawable.btn_selected);
                mAlbumSelected1.setBackgroundResource(R.drawable.btn_normal);
                mAlbumSelected2.setBackgroundResource(R.drawable.btn_normal);
                selectPhoto(0);
            }
        });
        mAlbumSetting1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlbumSelected0.setBackgroundResource(R.drawable.btn_normal);
                mAlbumSelected1.setBackgroundResource(R.drawable.btn_selected);
                mAlbumSelected2.setBackgroundResource(R.drawable.btn_normal);
                selectPhoto(1);
            }
        });
        mAlbumSetting2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlbumSelected0.setBackgroundResource(R.drawable.btn_normal);
                mAlbumSelected1.setBackgroundResource(R.drawable.btn_normal);
                mAlbumSelected2.setBackgroundResource(R.drawable.btn_selected);
                mAlbumType = 2;
                selectPhoto(mAlbumType);
                mEditor.putInt(FrameSettingPrefManager.ALBUM_TYPE,
                        mAlbumType);
                mEditor.commit();
            }
        });

        resetAlbumStatus();
    }

    private void resetAlbumStatus(){
        // init Album selected status
        switch(mAlbumType){
        case 0:
            mAlbumSelected0.setBackgroundResource(R.drawable.btn_selected);
            mAlbumSelected1.setBackgroundResource(R.drawable.btn_normal);
            mAlbumSelected2.setBackgroundResource(R.drawable.btn_normal);
            break;
        case 1:
            mAlbumSelected0.setBackgroundResource(R.drawable.btn_normal);
            mAlbumSelected1.setBackgroundResource(R.drawable.btn_selected);
            mAlbumSelected2.setBackgroundResource(R.drawable.btn_normal);
            break;
        case 2:
            mAlbumSelected0.setBackgroundResource(R.drawable.btn_normal);
            mAlbumSelected1.setBackgroundResource(R.drawable.btn_normal);
            mAlbumSelected2.setBackgroundResource(R.drawable.btn_selected);
            break;
        }
    }

    private void initLayoutSetting(){
        mFrameLayout = (TextView) findViewById(R.id.picture_num_and_layout);

        mListViews = new ArrayList<View>();
        mInflater = getLayoutInflater();
        View layout1 = mInflater.inflate(R.layout.framelayout0, null);
        View layout2 = mInflater.inflate(R.layout.framelayout1, null);

        mListViews.add(layout1);
        mListViews.add(layout2);

        mLayoutSetting0 = (ImageView) layout1.findViewById(R.id.layout1_white_1);
        mLayoutSetting1 = (ImageView) layout1.findViewById(R.id.layout1_white_2);
        mLayoutSetting2 = (ImageView) layout2.findViewById(R.id.layout1_white_3);
        mLayoutSetting3 = (ImageView) layout2.findViewById(R.id.layout1_white_4);


        mFrameLayoutList.add(mLayoutSetting0);
        mFrameLayoutList.add(mLayoutSetting1);
        mFrameLayoutList.add(mLayoutSetting2);
        mFrameLayoutList.add(mLayoutSetting3);

        switch (mFrameMaterial) {
        case FrameSettingPrefManager.BLACK:
            changeFrameMeterial(mLayoutBlackDrawable,mNumAndLayout);
            break;
        case FrameSettingPrefManager.WHITE:
            changeFrameMeterial(mLayoutWhiteDrawable,mNumAndLayout);
            break;
        case FrameSettingPrefManager.WOOD:
            changeFrameMeterial(mLayoutWoodDrawable,mNumAndLayout);
            break;
        case FrameSettingPrefManager.METAL:
            changeFrameMeterial(mLayoutMetalDrawable,mNumAndLayout);
            break;
        }

        for (int i = 0; i < mFrameLayoutList.size(); i++) {
            mFrameLayoutList.get(i).setOnClickListener(
                    new LayoutAndNumImageViewOnClickListener(i));
        }

        mLayoutAndNumPagerAdapter = new LayoutAndNumPagerAdapter();
        mLayoutAndNumViewPager = (ViewPager) findViewById(R.id.viewpagerLayout);
        mLayoutAndNumViewPager.setAdapter(mLayoutAndNumPagerAdapter);
        mLayoutAndNumViewPager
                .setOnPageChangeListener(new OnPageChangeListener() { 

                    @Override
                    public void onPageSelected(int arg0) {
                        mDot1 = (ImageView) findViewById(R.id.dot_0);
                        mDot2 = (ImageView) findViewById(R.id.dot_1);
                        if (arg0 == 0) {
                            mDot1.setBackgroundResource(R.drawable.dot_selected);
                            mDot2.setBackgroundResource(R.drawable.dot_normal);
                        } else if (arg0 == 1) {
                            mDot1.setBackgroundResource(R.drawable.dot_normal);
                            mDot2.setBackgroundResource(R.drawable.dot_selected);
                        } 
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });

        switch(mNumAndLayout){
        case 0:
        case 1:
            mLayoutAndNumViewPager.setCurrentItem(0);
            break;
        case 2:
        case 3:
            mLayoutAndNumViewPager.setCurrentItem(1);
            break;
        }
    }

    private void initFrameMeterial(){
        mFrameMeterial = (TextView) findViewById(R.id.select_frame_material);

        mFrameMeterial0 = (ImageButton)this.findViewById(R.id.material_black);
        mFrameMeterial1 = (ImageButton)this.findViewById(R.id.material_white);
        mFrameMeterial2 = (ImageButton)this.findViewById(R.id.material_wood);
        mFrameMeterial3 = (ImageButton)this.findViewById(R.id.material_metal);
        if(mMeterialDrawable.size()==MATERIAL_LIST_SIZE){
            mFrameMeterial0.setBackgroundDrawable(mMeterialDrawable.get(0));
            mFrameMeterial1.setBackgroundDrawable(mMeterialDrawable.get(1));
            mFrameMeterial2.setBackgroundDrawable(mMeterialDrawable.get(2));
            mFrameMeterial3.setBackgroundDrawable(mMeterialDrawable.get(3));
        }

        mFrameMeterialSelected0 = (ImageButton)this.findViewById(R.id.material_black_selected);
        mFrameMeterialSelected1 = (ImageButton)this.findViewById(R.id.material_white_selected);
        mFrameMeterialSelected2 = (ImageButton)this.findViewById(R.id.material_wood_selected);
        mFrameMeterialSelected3 = (ImageButton)this.findViewById(R.id.material_metal_selected);

        mFrameMeterial0.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mFrameMeterialSelected0.setVisibility(View.VISIBLE);
                mFrameMeterialSelected1.setVisibility(View.GONE);
                mFrameMeterialSelected2.setVisibility(View.GONE);
                mFrameMeterialSelected3.setVisibility(View.GONE);
                mFrameMaterial = 0;
                handleMeterialChange();
            }});
        mFrameMeterial1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mFrameMeterialSelected0.setVisibility(View.GONE);
                mFrameMeterialSelected1.setVisibility(View.VISIBLE);
                mFrameMeterialSelected2.setVisibility(View.GONE);
                mFrameMeterialSelected3.setVisibility(View.GONE);
                mFrameMaterial = 1;
                handleMeterialChange();
            }});
        mFrameMeterial2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mFrameMeterialSelected0.setVisibility(View.GONE);
                mFrameMeterialSelected1.setVisibility(View.GONE);
                mFrameMeterialSelected2.setVisibility(View.VISIBLE);
                mFrameMeterialSelected3.setVisibility(View.GONE);
                mFrameMaterial = 2;
                handleMeterialChange();
            }});
        mFrameMeterial3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mFrameMeterialSelected0.setVisibility(View.GONE);
                mFrameMeterialSelected1.setVisibility(View.GONE);
                mFrameMeterialSelected2.setVisibility(View.GONE);
                mFrameMeterialSelected3.setVisibility(View.VISIBLE);
                mFrameMaterial = 3;
                handleMeterialChange();
            }});

        //init frame meterial selected status
        switch(mFrameMaterial){
        case 0:
            mFrameMeterialSelected0.setVisibility(View.VISIBLE);
            mFrameMeterialSelected1.setVisibility(View.GONE);
            mFrameMeterialSelected2.setVisibility(View.GONE);
            mFrameMeterialSelected3.setVisibility(View.GONE);
            break;
        case 1:
            mFrameMeterialSelected0.setVisibility(View.GONE);
            mFrameMeterialSelected1.setVisibility(View.VISIBLE);
            mFrameMeterialSelected2.setVisibility(View.GONE);
            mFrameMeterialSelected3.setVisibility(View.GONE);
            break;
        case 2:
            mFrameMeterialSelected0.setVisibility(View.GONE);
            mFrameMeterialSelected1.setVisibility(View.GONE);
            mFrameMeterialSelected2.setVisibility(View.VISIBLE);
            mFrameMeterialSelected3.setVisibility(View.GONE);
            break;
        case 3:
            mFrameMeterialSelected0.setVisibility(View.GONE);
            mFrameMeterialSelected1.setVisibility(View.GONE);
            mFrameMeterialSelected2.setVisibility(View.GONE);
            mFrameMeterialSelected3.setVisibility(View.VISIBLE);
            break;
        }
    }

    private void initInterval(){
        mInterval = (TextView) findViewById(R.id.interval_time);

        mIntervalSetting0 = (LinearLayout)this.findViewById(R.id.interval0);
        mIntervalSetting1 = (LinearLayout)this.findViewById(R.id.interval1);
        mIntervalSetting2 = (LinearLayout)this.findViewById(R.id.interval2);
        mIntervalSelected0 = (ImageButton)this.findViewById(R.id.interval_btn0);
        mIntervalSelected1 = (ImageButton)this.findViewById(R.id.interval_btn1);
        mIntervalSelected2 = (ImageButton)this.findViewById(R.id.interval_btn2);

        mIntervalSetting0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntervalSelected0.setBackgroundResource(R.drawable.btn_selected);
                mIntervalSelected1.setBackgroundResource(R.drawable.btn_normal);
                mIntervalSelected2.setBackgroundResource(R.drawable.btn_normal);
                mIntervalTime = 0;
                mEditor.putInt(FrameSettingPrefManager.INTERVAL_KEY,
                        mIntervalTime);
                mEditor.commit();
            }
        });
        mIntervalSetting1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntervalSelected0.setBackgroundResource(R.drawable.btn_normal);
                mIntervalSelected1.setBackgroundResource(R.drawable.btn_selected);
                mIntervalSelected2.setBackgroundResource(R.drawable.btn_normal);
                mIntervalTime = 1;
                mEditor.putInt(FrameSettingPrefManager.INTERVAL_KEY,
                        mIntervalTime);
                mEditor.commit();
            }
        });
        mIntervalSetting2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntervalSelected0.setBackgroundResource(R.drawable.btn_normal);
                mIntervalSelected1.setBackgroundResource(R.drawable.btn_normal);
                mIntervalSelected2.setBackgroundResource(R.drawable.btn_selected);
                mIntervalTime = 2;
                mEditor.putInt(FrameSettingPrefManager.INTERVAL_KEY,
                        mIntervalTime);
                mEditor.commit();
            }
        });

        // init interval selected status
        switch(mIntervalTime){
        case 0:
            mIntervalSelected0.setBackgroundResource(R.drawable.btn_selected);
            mIntervalSelected1.setBackgroundResource(R.drawable.btn_normal);
            mIntervalSelected2.setBackgroundResource(R.drawable.btn_normal);
            break;
        case 1:
            mIntervalSelected0.setBackgroundResource(R.drawable.btn_normal);
            mIntervalSelected1.setBackgroundResource(R.drawable.btn_selected);
            mIntervalSelected2.setBackgroundResource(R.drawable.btn_normal);
            break;
        case 2:
            mIntervalSelected0.setBackgroundResource(R.drawable.btn_normal);
            mIntervalSelected1.setBackgroundResource(R.drawable.btn_normal);
            mIntervalSelected2.setBackgroundResource(R.drawable.btn_selected);
            break;
        }
    }

    private void getPrefs() {
        mIntervalTime = mSharedPreferences.getInt(
                FrameSettingPrefManager.INTERVAL_KEY, 0);
        mFrameMaterial = mSharedPreferences.getInt(
                FrameSettingPrefManager.FRAME_MERERIAL_KEY,
                FrameSettingPrefManager.WOOD);
        mNumAndLayout = mSharedPreferences.getInt(
                FrameSettingPrefManager.LAYOUT_KEY, 0);
        mAlbumType =  mSharedPreferences.getInt(
                FrameSettingPrefManager.ALBUM_TYPE, 0);
    }

    public class LayoutAndNumImageViewOnClickListener implements
            OnClickListener {
        int index;

        LayoutAndNumImageViewOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mNumAndLayout = mSharedPreferences.getInt(
                    FrameSettingPrefManager.LAYOUT_KEY, 0);
            if (index != mNumAndLayout) {
                mFrameLayoutList.get(index).getBackground().setAlpha(255);
                mFrameLayoutList.get(mNumAndLayout).getBackground().setAlpha(50);
                mEditor.putInt(FrameSettingPrefManager.LAYOUT_KEY, index);
                mEditor.commit();
                mNumAndLayout = index;
            }
        }
    }

    private class LayoutAndNumPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);

            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    private void selectPhoto(int which){
        Intent intent; 
        Bundle bundle;
        switch(which){
        case 0:
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
//            intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", mImageWidth);
//            intent.putExtra("aspectY", mImageHeight);
//            intent.putExtra("output", Uri.fromFile(new File(createImageTmpFile())));
//            intent.putExtra("outputFormat", "JPEG");
            this.startActivityForResult(intent, REQUEST_CODE_IMAGE);
            break;
        case 1:
            intent = new Intent();
            intent.setClass(this, AlbumFolderActivity.class);
            bundle = new Bundle();
            bundle.putInt("mode", which);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_FOLDER);
            break;
        case 2:
            String allPaths = "";
            Cursor cursor = this.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                String path = cursor.getString(1);
                if(TextUtils.isEmpty(allPaths)){
                    allPaths = path;
                }else{
                    allPaths = allPaths+";"+path;
                }
            }
            if(cursor!=null){
                cursor.close();
            }

            mEditor.putString(FrameSettingPrefManager.FOLDER_OR_FILE_PATH_KEY,allPaths);
            mEditor.commit();
            break;
        }
    }

    public static String createImageTmpFile() {
        String SDCARD = Environment.getExternalStorageDirectory().getPath();
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File("sdcard-ext");
            if (file.exists() && file.canWrite()) {
                SDCARD = file.getPath();
            }
        }

        String SDCARD_PATH = SDCARD + "/3DHome";
        String TMPDATA_PATH = SDCARD_PATH + "/tmp";
        String TMPDATA_IMAGE_PATH = SDCARD_PATH + "/.tmp_icon";

        File d = new File(TMPDATA_PATH);
        if (!d.exists()) {
            d.mkdirs();
        }
        String path = TMPDATA_IMAGE_PATH;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public class PictureSelectedListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {

            AlertDialog dialog = new AlertDialog.Builder(Setting.this)
                    .setTitle(Setting.this.getString(R.string.change_pic))
                    .setSingleChoiceItems(R.array.picture_selected,
                            mAlbumType,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    mAlbumType = which;
                                    selectPhoto(mAlbumType);
                                    mEditor.putInt(FrameSettingPrefManager.ALBUM_TYPE, mAlbumType);
                                    mEditor.commit();
                                    dialog.dismiss();
                                }
                            })
                    .create();
            dialog.show();
        }
    }

    private void handleMeterialChange() {
        int num_and_layout_index = mSharedPreferences.getInt(
                FrameSettingPrefManager.LAYOUT_KEY, 0);

        switch (mFrameMaterial) {
        case FrameSettingPrefManager.BLACK:
            changeFrameMeterial(mLayoutBlackDrawable,num_and_layout_index);
            break;
        case FrameSettingPrefManager.WHITE:
            changeFrameMeterial(mLayoutWhiteDrawable,num_and_layout_index);
            break;
        case FrameSettingPrefManager.WOOD:
            changeFrameMeterial(mLayoutWoodDrawable,num_and_layout_index);
            break;
        case FrameSettingPrefManager.METAL:
            changeFrameMeterial(mLayoutMetalDrawable,num_and_layout_index);
            break;
        }

        mEditor.putInt(FrameSettingPrefManager.FRAME_MERERIAL_KEY,
                mFrameMaterial);
        mEditor.commit();

    }
    
    private void changeFrameMeterial(ArrayList<Drawable> dList,int num_and_layout_index){
       if(dList.size()==LAYOUT_LIST_SIZE){
            for (int i = 0; i < mFrameLayoutList.size(); i++) {
                mFrameLayoutList.get(i).setBackgroundDrawable(dList.get(i));
                if (num_and_layout_index == i) {
                    mFrameLayoutList.get(i).getBackground().setAlpha(255);
                } else {
                    mFrameLayoutList.get(i).getBackground().setAlpha(50);
                }
            }
    	}
    }

    static final int SHOW_PHOTO = 100;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
        case KeyEvent.KEYCODE_BACK:
            Intent intent = new Intent("com.borqs.se.ALBUMCHANGED");
            Setting.this.sendBroadcast(intent);
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            resetAlbumStatus();
            return;
        }
        Bundle bundle = data.getExtras();
        switch(requestCode){
        case REQUEST_CODE_IMAGE:
            mAlbumType = 0;
            String file_path = "";
            Cursor cursor1 = this.getContentResolver().query(
                    data.getData(), null, null,
                    null, null);
            if(cursor1!=null && cursor1.moveToFirst()){
                file_path = cursor1.getString(1);
            }
            if(cursor1!=null){
                cursor1.close();
            }

            mEditor.putInt(FrameSettingPrefManager.ALBUM_TYPE,
                    mAlbumType);
            File file0 = new File(file_path);
            if(file0.isFile()){
                mEditor.putString(FrameSettingPrefManager.FOLDER_OR_FILE_PATH_KEY,
                file_path);
            }
            mEditor.commit();
            break;
        case REQUEST_CODE_IMAGE_FOLDER:
            mAlbumType = 1;
            mEditor.putInt(FrameSettingPrefManager.ALBUM_TYPE,
                    mAlbumType);

            File file = new File(bundle.getString("folder_path"));
            String folderPaths = "";
            if(file.isDirectory()){
                //Folder
                File[] files = file.listFiles();
                for(int i = 0;i < files.length;i++){
                    String absolutePath = files[i].getAbsolutePath();
                    if (absolutePath.endsWith("jpeg") || absolutePath.endsWith("png")
                            || absolutePath.endsWith("jpg")) {
                        if(TextUtils.isEmpty(folderPaths)){
                            folderPaths = absolutePath;
                        }else{
                            folderPaths = folderPaths+";"+absolutePath;
                        }
                    }
                }
            }
            mEditor.putString(FrameSettingPrefManager.FOLDER_OR_FILE_PATH_KEY,folderPaths);
            mEditor.commit();
            break; 
        }
        resetAlbumStatus();
    }

    private ArrayList<Drawable> initDrawableList(String[] nameArray){
        ArrayList<Drawable> drawableList = new ArrayList<Drawable>();
        String curName = null;
        FileInputStream fis=null;
        Bitmap tempBitmap=null;
        for(int i=0;i<4;i++){
            curName = nameArray[i];
            try {
                File fileIn = new File(this.getFilesDir().getAbsolutePath()+"/downloaded_resource/com.borqs.se.object.xiangkuangxin/"+curName);
                fis = new FileInputStream(fileIn);
                tempBitmap=BitmapFactory.decodeStream(fis);
                if(tempBitmap!=null){
                    drawableList.add(new BitmapDrawable(tempBitmap));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(fis!=null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return drawableList;
    }

    private void initMaterialDrawbleList(){
        ArrayList<Drawable> drawable = initDrawableList(mMaterialName);
        for(int i = 0;i<drawable.size();i++){
            mMeterialDrawable.add(drawable.get(i));
        }
    }

    private void initLayoutBlackDrawableList(){
        ArrayList<Drawable> drawable = initDrawableList(mBlackLayoutName);
        for(int i = 0;i<drawable.size();i++){
            mLayoutBlackDrawable.add(drawable.get(i));
        }
    }

    private void initLayoutWhiteDrawableList(){
        ArrayList<Drawable> drawable = initDrawableList(mWhiteLayoutName);
        for(int i = 0;i<drawable.size();i++){
            mLayoutWhiteDrawable.add(drawable.get(i));
        }
    }

    private void initLayoutWoodDrawableList(){
        ArrayList<Drawable> drawable = initDrawableList(mWoodLayoutName);
        for(int i = 0;i<drawable.size();i++){
            mLayoutWoodDrawable.add(drawable.get(i));
        }
    }

    private void initLayoutMetalDrawableList(){
        ArrayList<Drawable> drawable = initDrawableList(mMetalLayoutName);
        for(int i = 0;i<drawable.size();i++){
            mLayoutMetalDrawable.add(drawable.get(i));
        }
    }

    private void initFrameMeterialDrawableList() {
        initLayoutBlackDrawableList();
        initLayoutWhiteDrawableList();
        initLayoutWoodDrawableList();
        initLayoutMetalDrawableList();
    }
}
