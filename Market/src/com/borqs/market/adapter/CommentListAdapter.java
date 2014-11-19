package com.borqs.market.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.market.json.Comment;

public class CommentListAdapter extends BaseAdapter {

    private ArrayList<Comment> mDatas;
    private Context mContext;
    public CommentListAdapter(Context context, ArrayList<Comment> datas) {
        mContext = context;
        mDatas = datas;
    }

    public int getCount() {
        return mDatas != null ? 
                mDatas.size(): 0;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CommentItemView holder = null;
        if(convertView == null) {
            holder = new CommentItemView(mContext);
        }else {
            holder = (CommentItemView) convertView.getTag();
        }
        holder.refreshUI(getItem(position));
        return holder.convertView;
    }

    public void notifyDataSetChanged(ArrayList<Comment> datas) {
        mDatas = datas;
        super.notifyDataSetChanged();
    }

    @Override
    public Comment getItem(int position) {
        if(mDatas == null) {
            return null;
        }else {
            return mDatas.get(position);
        }
    }

}
