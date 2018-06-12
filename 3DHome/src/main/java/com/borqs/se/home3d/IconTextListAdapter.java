package com.borqs.se.home3d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.se.R;

import java.util.List;


public class IconTextListAdapter extends ArrayAdapter<IconTextListAdapter.IconListItem> {

    protected LayoutInflater mInflater;
    private static final int mResource = R.layout.icon_list_item;
//    private ViewHolder mViewHolder;

    static class ViewHolder {
        private View mView;
        private TextView mTextView;
        private ImageView mImageView;

        public ViewHolder(View view) {
            mView = view;
        }

        public TextView getTextView() {
            if (mTextView == null) {
                mTextView = (TextView) mView.findViewById(R.id.text1);
            }

            return mTextView;
        }

        public ImageView getImageView() {
            if (mImageView == null) {
                mImageView = (ImageView) mView.findViewById(R.id.icon);
            }

            return mImageView;
        }
    }
    public IconTextListAdapter(Context context,
            List<IconListItem> items) {
        super(context, mResource, items);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // Set text field
        TextView text = viewHolder.getTextView();
        text.setText(getItem(position).getTitle());

        // Set resource icon
        ImageView image = viewHolder.getImageView();
        image.setImageResource(getItem(position).getResource());

        return view;
    }

    public static class IconListItem {
        private final String mTitle;
        private final int mResource;

        public IconListItem(String title, int resource) {
            mResource = resource;
            mTitle = title;
        }

        public String getTitle() {
            return mTitle;
        }

        public int getResource() {
            return mResource;
        }
    }


}
