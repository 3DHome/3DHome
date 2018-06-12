/*
 * Copyright © 2013 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, 
 * or disclosed, or disseminated outside of Borqs in whole or in part without Borqs 's permission.
 */

/* 
 * Description of the content in this file. 
 * 
 * History core-id Change purpose 
 * ---------- ------- ---------------------------------------------- 
 *
 */


package com.borqs.se.addobject;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.se.R;
import com.borqs.se.addobject.AddObjectGridView.OnItemCheckListener;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.ObjectInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Dialog for selecting apps to add.
 */
public class AddObjectDialog extends Dialog implements OnItemCheckListener {

    /**/
    public static final String TAG = "AddObjectDialog";

    public static final int APP = 0;
    public static final int FOLDER = 1;
    public static final int ADD_OR_HIDE = FOLDER+1;//added by b128 for hide app feature
    private static final int PAGE_MAX_NUM = Integer.MAX_VALUE;

    /**/
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<Integer> mSelectedNums = null;
    private int mMax = 0;
    private int mNum = 0;
    private int mTag = APP;

    /**/
    private IIPagerAdapter mPagerAdapter = null;
    private View mContentView = null;
//    private AddObjectViewPager mPager = null;
    private TextView mSelectdNumView = null;
    private LinearLayout mPageIndicator = null;

    /**/
    private OnObjectsSelectedListener mOnObjectsSelectedListener;

    /**/
    private ArrayList<AddObjectComponentNameExtend> defaultSelectedComponentNameExs;
    private String defaultFolderName;

    private AddObjectEditText mEditText;

    
    public AddObjectDialog(Context context) { super(context); }

    public AddObjectDialog(Context context, LayoutInflater inflater, List<AppItemInfo> itemInfos, int tag, int max, Bundle bundle, boolean needShowCheckedMark) {
        super(context, R.style.HomeDialogStyle);
        mContext = context;
        mInflater = inflater;

        if (bundle != null) {
            ArrayList list = bundle.getParcelableArrayList("componentnameexs");
            if (list != null) {
                defaultSelectedComponentNameExs = (ArrayList<AddObjectComponentNameExtend>)list.get(0);
            }
            defaultFolderName = bundle.getString("foldername");
            if (TextUtils.isEmpty(defaultFolderName)) { 
                defaultFolderName = null;
                defaultSelectedComponentNameExs = null; 
            } else {
                if ((defaultSelectedComponentNameExs == null) || (defaultSelectedComponentNameExs.size() <= 0)) {
                    defaultFolderName = null;
                    defaultSelectedComponentNameExs = null; 
                }
            }
            if (defaultSelectedComponentNameExs != null) {
                if (defaultSelectedComponentNameExs.size() > max) {
                    dismiss();
                    return;
                }
            }
        }

        ArrayList<AddObjectItemInfo> iItemInfos = new ArrayList<AddObjectItemInfo>();
        if (ifEditStatus() == true) {
            if (defaultSelectedComponentNameExs != null) {
                for (int i = 0; i < defaultSelectedComponentNameExs.size(); i++) {
                    AddObjectItemInfo iiTmp = null;
                    AddObjectComponentNameExtend ex = defaultSelectedComponentNameExs.get(i);
                    String objectInfoName = ex.getObjectInfoName();
                    if (ex.getIsShortcutOrUserUpdate()) {
                        //Ex!
                        iiTmp = getItemInfoByObjectInfoName(objectInfoName, ex.getType());
                    } else {
                        try {
                            ItemInfo tmp = LauncherModel.getInstance().findAppItem(ex.getComponentName());
                            if (tmp != null) {
                                tmp = new ItemInfo(tmp.getContext(), tmp.getResolveInfo(), tmp.getComponentName());
                            }
                            if (tmp != null) {
                                iiTmp = new AddObjectItemInfo(tmp);
                                iiTmp.mObjectInfoName = ex.getObjectInfoName();
                            }
                        } catch (Exception e) { iiTmp = null; }
                    }
                    if (iiTmp == null) { continue; }
                    iiTmp.setOriginal(true);
                    iItemInfos.add(iiTmp);
                }
                for (ItemInfo ii : itemInfos) { 
                    if (defaultSelectedComponentNameExs.contains(new AddObjectComponentNameExtend(ii.getComponentName(), null, null, false))) {
                        continue;
                    }
                    iItemInfos.add(new AddObjectItemInfo(ii)); 
                }
            } else {
                for (ItemInfo ii : itemInfos) { iItemInfos.add(new AddObjectItemInfo(ii)); }
            }
        } else {
            for (ItemInfo ii : itemInfos) { iItemInfos.add(new AddObjectItemInfo(ii)); }
        }

        mMax = max;
        mTag = tag;

        init(iItemInfos,needShowCheckedMark);
    }

    public void setInternalListener(OnObjectsSelectedListener onObjectsSelectedListener) {
        mOnObjectsSelectedListener = onObjectsSelectedListener;
    }

    public void setDefault() {
        if (ifEditStatus() == true) {
            mSelectdNumView.setText("" + defaultSelectedComponentNameExs.size() + "/" + mMax);
            if (mEditText != null) {
                mEditText.setText(defaultFolderName);
            }
        }
        if (mPagerAdapter != null) {
            mPagerAdapter.setDefaultSelectionData();
        }
    }

    private void init(ArrayList<AddObjectItemInfo> itemInfos,boolean needShowCheckedMark) {
        int or;
        if (HomeManager.getInstance().isLandscapeOrientation()) {
            or = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;            
        } else {
            or = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        mContentView = mInflater.inflate(R.layout.addobject_dialog_layout_multiple_select, null);

        int objw = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_object_w);
        int objh = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_item_icon_wh)
                     + mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_item_padding) * 2
                     + getIconTextHeight();

        int[] swh = getCurrentScreenHeight();
        int clearw = getAvailableContentLayoutMaxWidth(swh[0], or);
        int w = clearw - mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_bluelight_lr_w) * 2;
        int h = getAvailableContentLayoutMaxHeight(swh[1], or);

        View contentvpl = mContentView.findViewById(R.id.contentvpl);
        LayoutParams params = contentvpl.getLayoutParams();
        params.height = h;
        params.width = w;
        contentvpl.setLayoutParams(params);

        View contentvplparent = mContentView.findViewById(R.id.contentvplparent);
        LayoutParams paramsparent = contentvplparent.getLayoutParams();
        paramsparent.height = h + mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_indicator_h);
        paramsparent.width = clearw;
        contentvplparent.setLayoutParams(paramsparent);

        int nper = (int)(Math.floor((float)w / (float)objw) * Math.floor((float)h / (float)objh));
        mNum = (int)(Math.ceil(((double)itemInfos.size()) / ((double)nper)));
        if (mNum > PAGE_MAX_NUM) { mNum = PAGE_MAX_NUM; }

        params.height = (int)(Math.floor((float)h / (float)objh)) * objh;
        contentvpl.setLayoutParams(params);
        paramsparent.height = params.height + mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_indicator_h);
        contentvplparent.setLayoutParams(paramsparent);

        View contentvpcvl = mContentView.findViewById(R.id.contentvpcvl);
        LayoutParams paramscv = contentvpcvl.getLayoutParams();
        paramscv.height = params.height;
        paramscv.width = params.width;
        contentvpcvl.setLayoutParams(paramscv);
        contentvpcvl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideIM();
            }
        });
        
        View fvl = mContentView.findViewById(R.id.fadingv_left);
        LayoutParams paramsfvl = fvl.getLayoutParams();
        paramsfvl.height = params.height;
        paramsfvl.width = FadingView.getFVWidth(mContext);
        fvl.setLayoutParams(paramsfvl);
        View fvr = mContentView.findViewById(R.id.fadingv_right);
        LayoutParams paramsfvr = fvr.getLayoutParams();
        paramsfvr.height = params.height;
        paramsfvr.width = FadingView.getFVWidth(mContext);
        fvr.setLayoutParams(paramsfvr);
        
        mSelectedNums = new ArrayList<Integer>();
        for (int i = 0; i < mNum; i++) {
            mSelectedNums.add(Integer.valueOf(0));
        }

        mPagerAdapter = new IIPagerAdapter(mContext, mInflater, itemInfos, this, mNum, nper,needShowCheckedMark);
        AddObjectViewPager viewPager = (AddObjectViewPager)mContentView.findViewById(R.id.contentvp);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOnPageChangeListener(new AddObjectViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                setIndicator(arg0);
                setFadingViewVisible(false);
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { 
                if ((arg0 == 0) && (arg2 == 0)) { return; }
                setFadingViewVisible(true);
            }
            @Override
            public void onPageScrollStateChanged(int arg0) { 
                if (arg0 == AddObjectViewPager.SCROLL_STATE_IDLE) {
                    setFadingViewVisible(false);
                }
            }
        });
        
        mPageIndicator = (LinearLayout)mContentView.findViewById(R.id.pageindicatorl);
        ImageView iv = null;
        for (int i = 0; i < mNum; i++) {
            iv = new ImageView(mContext);
            Drawable dw = null;
            if (i == 0) {
                dw = mContext.getResources().getDrawable(R.drawable.addobject_indicator_on);
            } else {
                dw = mContext.getResources().getDrawable(R.drawable.addobject_indicator_off);
            }
            if (dw.getIntrinsicWidth() * mNum > w) {
                int ivwh = w / mNum;
                iv.setAdjustViewBounds(true);
                iv.setMaxWidth(ivwh);
                iv.setMaxHeight(ivwh);
            }
            iv.setImageDrawable(dw);
            iv.setPadding(0, 0, 0, 5);

            mPageIndicator.addView(iv, i);
        }

        mContentView.findViewById(R.id.addobject_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mContentView.findViewById(R.id.addobject_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ifEditStatus() == true) {
                    if (mOnObjectsSelectedListener != null) {
                        mOnObjectsSelectedListener.onObjectSelected(
                                mPagerAdapter.isAnySelectionUpdated() ? mPagerAdapter.getSelectedItems() : null);
                    }
                    dismiss();
                    return;
                }
                if (mOnObjectsSelectedListener != null) {
                    mOnObjectsSelectedListener.onObjectSelected(mPagerAdapter.getSelectedItems());
                }
                dismiss();
            }
        });

        mSelectdNumView = (TextView)mContentView.findViewById(R.id.selectedinfo);
        if (mTag == FOLDER) {
            mEditText = (AddObjectEditText)mContentView.findViewById(R.id.contenttitle_ev);
            mEditText.setListener(new AddObjectEditText.IMStatusListener() {
                public void statusChanged(boolean show) {
                    setContentVisibility(show);
                }
            });
            mEditText.setText(mContext.getString(R.string.folder_title));

            mSelectdNumView.setText("0/" + mMax);

            mContentView.findViewById(R.id.contenttitle_ev).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.contenttitle_tv).setVisibility(View.GONE);
            mContentView.findViewById(R.id.contenttitle_ev).setEnabled(true);
            mContentView.findViewById(R.id.folderl).setVisibility(View.VISIBLE);

        } else {
            mContentView.findViewById(R.id.contenttitle_ev).setVisibility(View.GONE);
            mContentView.findViewById(R.id.contenttitle_tv).setVisibility(View.VISIBLE);
            if(mTag == ADD_OR_HIDE){
                TextView tv = (TextView)mContentView.findViewById(R.id.contenttitle_tv);
                tv.setText(R.string.select_app_to_hide);
                mSelectdNumView.setVisibility(View.INVISIBLE);
            }else{
                mSelectdNumView.setVisibility(View.VISIBLE);
                mSelectdNumView.setText("0");
            }
        }

        viewPager.setCurrentItem(0);
        setFadingViewVisible(false);
        setContentView(mContentView);
    }

    public ArrayList<ImageView> getFolderImageViews() {
        ArrayList<ImageView> ivs = new ArrayList<ImageView>();
        ivs.add((ImageView)mContentView.findViewById(R.id.folderiv1));
        ivs.add((ImageView)mContentView.findViewById(R.id.folderiv2));
        ivs.add((ImageView)mContentView.findViewById(R.id.folderiv3));
        ivs.add((ImageView)mContentView.findViewById(R.id.folderiv4));
        return ivs;
    }

    private void setIndicator(int idx) {
        ImageView iv;
        for (int i = 0; i < mNum; i++) {
            iv = (ImageView)mPageIndicator.getChildAt(i);
            if (i == idx) {
                iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.addobject_indicator_on));
            } else {
                iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.addobject_indicator_off));
            }
        }
    }
    
    public void setContentVisibility(boolean visible) {
        if (visible == true) {
            mContentView.findViewById(R.id.contentvpcvl).setVisibility(View.VISIBLE);
        } else {
            mContentView.findViewById(R.id.contentvpcvl).setVisibility(View.GONE);
        }
    }

    public int getMax() { return mMax; }
    public String getContentTitle() {
        if (mTag == FOLDER) {
            return mEditText.getText().toString();
        }
        return null;
    }
    
    public void hideIM() {
        if (mTag == FOLDER) {
            if (mEditText != null) {
                mEditText.hideIM();
            }
        }
    }
    
    private boolean ifEditStatus() {
        return TextUtils.isEmpty(defaultFolderName) == false;
    }
    
    private void setFadingViewVisible(boolean bl) {
        ((FadingView)mContentView.findViewById(R.id.fadingv_left)).setVisibility(bl);
        ((FadingView)mContentView.findViewById(R.id.fadingv_right)).setVisibility(bl);
    }

    @Override
    public void onCheck(int tagIdx, AddObjectItemInfo info, int position, boolean checked, int checkedNum) {
        if (checked == true) {
            int fc = 0;
            for (int i = 0; i < mSelectedNums.size(); i++) {
                fc += mSelectedNums.get(i);
            }
            if (fc >= mMax) {
                mPagerAdapter.unCheck(tagIdx, position);
                if (mTag == FOLDER) {
                    Toast.makeText(mContext, R.string.addobject_select_to_add_folder_no_room, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, R.string.addobject_select_to_add_app_no_room, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        mSelectedNums.set(tagIdx, Integer.valueOf(checkedNum));
        int c = 0;
        for (int i = 0; i < mSelectedNums.size(); i++) {
            c += mSelectedNums.get(i);
        }

        if (mTag == FOLDER) {
            mSelectdNumView.setText("" + c + "/" + mMax);
        } else {
            mSelectdNumView.setText("" + c);
            if (c > mMax) {
                mSelectdNumView.setTextColor(mContext.getResources().getColor(R.color.addobject_select_overmax_color));
            } else {
                mSelectdNumView.setTextColor(Color.BLACK);
            }
        }

        if (mOnObjectsSelectedListener != null) {
            mOnObjectsSelectedListener.onObjectChecked(info, checked, c);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mPagerAdapter != null) {
            // TODO: free
        }
    }

    private int[] getCurrentScreenHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        SESceneManager.getInstance().getGLActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return new int[] { displaymetrics.widthPixels, displaymetrics.heightPixels };
    }
    
    private int getAvailableContentLayoutMaxWidth(int sw, int or) {
        if (or == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return ((int)(sw * 0.98f));
        }
        int w1 = sw - mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_folder_wh);
        int w2 = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_button_w);
        return (int)((Math.max(w1,  w2)) * 0.98f);
    }

    private int getAvailableContentLayoutMaxHeight(int sh, int or) {
        int ts = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_title_textsize);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(ts);
        FontMetrics fm = paint.getFontMetrics();
        int h1 = (int) Math.ceil(fm.descent - fm.top) + 2 + 20; //title height
        int h2 = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_bluelight_btm_h);
        if (or == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            h2 += mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_button_h);
        }
        
        int ind = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_indicator_h);
        
        if (or == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (mTag == FOLDER) {
                return (int)(((float)(sh - (h1 + h2 + ind) 
                                     - mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_folder_paddingbottom)
                                     - mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_folder_wh))) * 0.9f);
            }
        }
        return (int)(((float)(sh - (h1 + h2 + ind))) * 0.9f);
    }

    /**/
    private int getIconTextHeight() {
        int ts = mContext.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_object_icontext_size);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(ts);
        FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    /**/
    private AddObjectItemInfo getItemInfoByObjectInfoName(String name, String type) {
        if (TextUtils.isEmpty(name)) { return null; }
        String where = ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        Cursor cursor = null;
        ContentResolver resolver = mContext.getContentResolver();
        try {
            cursor = resolver.query(ObjectInfoColumns.OBJECT_LEFT_JOIN_ALL, null, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    ObjectInfo objectInfo = ObjectInfo.CreateFromDB(cursor);
                    AddObjectItemInfo itemInfo = new AddObjectItemInfo(mContext, objectInfo.getResolveInfo(), objectInfo.mComponentName); 
                    if (TextUtils.isEmpty(type)) { 
                        itemInfo.mItemType = ItemInfo.ITEM_TYPE_APP;
                    } else {
                        if (type.equals("Shortcut")) {
                            itemInfo.mItemType = ItemInfo.ITEM_TYPE_SHORTCUT;
                        } else {
                            itemInfo.mItemType = ItemInfo.ITEM_TYPE_APP;
                        }
                    }
                    itemInfo.setLabel(objectInfo.mDisplayName);
                    itemInfo.mObjectInfoName = name;
                    if (objectInfo.mShortcutIcon != null) {
                        itemInfo.setIcon(new BitmapDrawable(objectInfo.mShortcutIcon));
                    }
                    return itemInfo;
                }
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**/
    private class IIPagerAdapter extends PagerAdapter {

        private int mNum = 0;
        private ArrayList<AddObjectItemInfo> mItemInfos = null;
        private ArrayList<VContainer> mVs = null;


        public IIPagerAdapter(Context context, LayoutInflater inflater,
                ArrayList<AddObjectItemInfo> itemInfos,
                OnItemCheckListener onItemCheckedListener, int num, int nper,
                boolean needShowCheckedMark) {
            super();
            mItemInfos = itemInfos;
            mNum = num;

            mVs = new ArrayList<VContainer>();
            AddObjectGridView gridv = null;
            int start = 0;
            int end = 0;
            for (int i = 0; i < mNum; i++) {
                gridv = (AddObjectGridView)inflater.inflate(R.layout.addobject_dialog_layout_gridview, null);
                gridv.init(context, i);
                gridv.setOnItemCheckListener(onItemCheckedListener);
                start = i * nper;
                end = (i + 1) * nper;
                if (end >= mItemInfos.size()) { end = mItemInfos.size(); }
                ArrayList<AddObjectItemInfo> tmp = new ArrayList<AddObjectItemInfo>();
                for (int idx = start; idx < end; idx++) {
                    tmp.add(mItemInfos.get(idx));
                }
                gridv.setItemList(tmp, needShowCheckedMark);
                mVs.add(new VContainer(gridv, false));
            }
        }

        @Override
        public int getCount() { return mNum; }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            if (mVs.get(arg1).inited == false) {
                ((AddObjectViewPager)arg0).addView(mVs.get(arg1).v);
                mVs.get(arg1).inited = true;
            }
            return mVs.get(arg1).v;
        }

        public void setDefaultSelectionData() {
            int idx = 0;
            for (VContainer vc : mVs) {
                mSelectedNums.set(idx, Integer.valueOf(vc.v.checkAndSetDefault(defaultSelectedComponentNameExs, mOnObjectsSelectedListener)));
                idx++;
            }
        }

        public ArrayList<AddObjectItemInfo> getSelectedItems() {
            ArrayList<AddObjectItemInfo> selected = new  ArrayList<AddObjectItemInfo>();
            for (VContainer container : mVs) {
                selected.addAll(container.v.getSelectedItems());
            }
            return selected;
        }

        public boolean isAnySelectionUpdated() {
            for (VContainer container : mVs) {
                if (container.v.isAnySelectOrUnselect()) {
                    return true;
                }
            }            
            return false;
        }
        
        public void unCheck(int tagIdx, int position) {
            mVs.get(tagIdx).v.unCheck(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) { return (arg0 == (arg1)); }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) { }
        @Override
        public Parcelable saveState() { return null; }
        @Override
        public void startUpdate(View arg0) { }
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) { }
        @Override
        public void finishUpdate(View arg0) { }
    }

    /**/
    private class VContainer {
        public AddObjectGridView v;
        public boolean inited = false;
        public VContainer(AddObjectGridView v, boolean inited) { this.v = v; this.inited = inited; }
    }


    /**
     * Interface for select objects
     */ 
    public interface OnObjectsSelectedListener {
        void onObjectSelected(ArrayList<AddObjectItemInfo> selectedList);
        void onObjectChecked(AddObjectItemInfo selected, boolean checked, int currentNum);
        void onObjectCheckedDirectly(AddObjectItemInfo selected);
    }

} // class AddObjectDialog
