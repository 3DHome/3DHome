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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import com.borqs.se.R;
import com.borqs.se.addobject.AddObjectDialog.OnObjectsSelectedListener;


/**
 * Grid view for apps
 */
public class AddObjectGridView extends GridView implements OnItemClickListener {
    
    /**/
    private static final String TAG = "AddObjectGridView";
    
    private Context context;
    private GridAdapter adapter;
    private int checkedNum;
    private boolean[] itemSelected;
    private boolean multiChoice;
    private List<AddObjectItemInfo> itemList;
    
    private int tagIdx = -1;

    private boolean anyClick = false;

    public OnItemCheckListener mOnItemCheckListener;
    public interface OnItemCheckListener {
        void onCheck(int tagIdx, AddObjectItemInfo info, int position, boolean checked, int checkedNum);
    }
    
    public AddObjectGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setOnItemCheckListener(OnItemCheckListener mOnItemCheckListener) {
        this.mOnItemCheckListener = mOnItemCheckListener;
    }

    public void init(Context context, int tagIdx) {
        this.tagIdx = tagIdx;
        this.checkedNum = 0;
        this.multiChoice = true; // default true
        setOnItemClickListener(this);
        setSelector(R.drawable.addobject_gridview_item_selector);
    }

    public void setChoiceMode(boolean ifMulti) {
        multiChoice = ifMulti;
        if(adapter != null) {
            adapter.setChoiceMode(multiChoice);
        }
    }
    
    public void setItemList(List<AddObjectItemInfo> items,boolean needShowCheckedMark) {
        this.itemList = items; 
        if(items != null && items.size() >= 0) {
            itemSelected = new boolean[items.size()];

            //for add/remove app function need show checked mark
            // for folder needn't show it
            if(needShowCheckedMark){
                for (int i = 0; i < itemSelected.length; i++) {
                    itemSelected[i] = !this.itemList.get(i).isShowInPreface();
                }
            }

            adapter = new GridAdapter(context, items, itemSelected);
            adapter.setChoiceMode(multiChoice);
            setAdapter(adapter);
        }
    }
    
    public List<AddObjectItemInfo> getSelectedItems() {
        if(itemSelected == null || itemSelected.length < 0) {
            return null;
        }

        if(adapter == null) {
            return null;
        }

        List<AddObjectItemInfo> allData = adapter.getItemList();
        if(allData == null) {
            return null;
        }

        int size = itemSelected.length;
        if(size != adapter.getItemList().size()) {
            return null;
        }
        // update selected status
        for (int i = 0; i < size; i++) {
            allData.get(i).setShowInPreface(itemSelected[i]);
        }


        List<AddObjectItemInfo> selected = new ArrayList<AddObjectItemInfo>();
        for(int i=0;i<size;i++) {
            if(itemSelected[i] == true) {
                selected.add(allData.get(i));
            }
        }
        
        return selected;
    }
    
    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        anyClick = true;
        if(multiChoice) {
            if(itemSelected != null && itemSelected.length > arg2) {
                itemSelected[arg2] = !itemSelected[arg2];
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                    if(itemSelected[arg2]) {
                        checkedNum ++;
                    } else {
                        checkedNum --;
                    }
                    if(mOnItemCheckListener != null) {
                        mOnItemCheckListener.onCheck(tagIdx, adapter.getItem(arg2), arg2, itemSelected[arg2], checkedNum);   
                    }
                }
            }
        } else {
            if(adapter != null) {
                mOnItemCheckListener.onCheck(tagIdx, adapter.getItem(arg2), arg2, true, 1);   
            }
        }
    }

    public void unCheck(int position) {
        itemSelected[position] = false;
        if (checkedNum > 0) {
            checkedNum --;
        }
        adapter.notifyDataSetChanged();
    }

    public boolean isAnySelectOrUnselect() {
        return anyClick;        
    }

    public int checkAndSetDefault(ArrayList<AddObjectComponentNameExtend> defs, OnObjectsSelectedListener l) {
        if ((defs == null) || (defs.size() <= 0)) { return 0; }
        List<AddObjectItemInfo> list = adapter.getItemList();
        int c = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getComponentName() == null) { 
                //sc!
                c++; 
                itemSelected[i] = true;
                if (l != null) {
                    l.onObjectCheckedDirectly(list.get(i));
                }
            } else {
                AddObjectComponentNameExtend tmpEx = new AddObjectComponentNameExtend(list.get(i).getComponentName(), null, null, false);
                if (defs.contains(tmpEx)) { 
                    c++; 
                    itemSelected[i] = true;
                    if (l != null) {
                        l.onObjectCheckedDirectly(list.get(i));
                    }
                }
            }
        }
        checkedNum = c;
        return c;
    }

    private class GridAdapter extends ArrayAdapter<AddObjectItemInfo> {

        private static final String TAG = "GridAdapter";
        
        private LayoutInflater inflater = null;
        private Context context = null;
        private boolean multiChoice = true;
        /*data*/
        private List<AddObjectItemInfo> items = null;
        private int dwWH = 56;
        
        public GridAdapter(Context context, List<AddObjectItemInfo> items, boolean[] itemSelected) {
            super(context, -1);
            // TODO Auto-generated constructor stub
            this.context = context;
            this.items = items;
            if(context != null) {
                inflater = LayoutInflater.from(context);   
            }
            dwWH = context.getResources().getDimensionPixelSize(R.dimen.addobject_dialog_contentv_item_icon_wh);
        }
        
        @Override
        public int getCount() {
            if(items != null && items.size() > 0) {
                return items.size();
            }
            return 0;
        }

        @Override
        public AddObjectItemInfo getItem(int position) {
            if(items != null && items.size() > 0) {
                return items.get(position);
            }
            return null;
        }
        
        public List<AddObjectItemInfo> getItemList() {
            return items;
        }
        
        public void setChoiceMode(boolean ifMulti) {
            multiChoice = ifMulti;
        }

        class ViewHolder {
            TextView iconV;
            TextView nameV;
            TextView cBox;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if(convertView == null) {
                vh = new ViewHolder();
                convertView = inflater.inflate(R.layout.addobject_dialog_gridview_item, null);
                loadGridItemLayout(position, vh, convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            if(vh != null) {
                loadDataOnItem(getItem(position), vh, position);  
            }
            return convertView;
        }
        
        private void loadGridItemLayout(int position, ViewHolder vh, View convertView) {
            if(vh != null && convertView != null) {
                vh.iconV = (TextView) convertView.findViewById(R.id.addobject_gridview_item_image);
                vh.nameV = (TextView) convertView.findViewById(R.id.addobject_gridview_item_text);
                vh.cBox  = (TextView) convertView.findViewById(R.id.addobject_gridview_item_checkitem);
            }
        }
        
        private void loadDataOnItem(AddObjectItemInfo item, final ViewHolder vh, final int position) {
            if(vh != null && item != null) {
                Drawable dw = item.getIconWithCreating();
                vh.iconV.setBackgroundDrawable(dw);
                vh.nameV.setTextColor(Color.WHITE);
                vh.nameV.setText(item.getLabel());
                
                if (itemSelected[position] && multiChoice) {
                    vh.cBox.setVisibility(View.VISIBLE);
                } else {
                    vh.cBox.setVisibility(View.GONE);
                }
            }
        }
    }

    public void recycleMemory() {
        if(itemList != null & itemList.size() > 0){ 
            for(AddObjectItemInfo mInfo : itemList) {
                Drawable dw = mInfo.getIcon();
                if (dw != null) {
                    dw.setCallback(null);
                }
            }
            System.gc();
        }
    }
}
