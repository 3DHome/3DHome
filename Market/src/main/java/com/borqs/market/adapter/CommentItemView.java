package com.borqs.market.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.json.Comment;
import com.funyoung.androidfacade.CommonHelperUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentItemView {
    public static final String TAG = CommentItemView.class.getSimpleName();
    public View convertView;
    
//    private Context mContext;
//    private LayoutInflater inflater;
    private Comment mData;
    
    TextView content;
    TextView version;
    TextView description;
    RatingBar ratingBar;
    
    private String lableVersion;
    private String lable_comment_on;
//    private SimpleDateFormat fmt;
    
    public CommentItemView(Context context) {
        super();
//        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        lableVersion = context.getString(R.string.product_info_version);
        lable_comment_on = context.getString(R.string.lable_comment_on);
//        fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        convertView = inflater.inflate(R.layout.comment_list_item, null);
        content = (TextView)convertView.findViewById(R.id.comment_content);
        version = (TextView)convertView.findViewById(R.id.version);
        description = (TextView)convertView.findViewById(R.id.resource_comment_description);
        ratingBar = (RatingBar)convertView.findViewById(R.id.resource_comment_ratingbar);
        
        convertView.setTag(this);
    }
    
    private void updateUI() {
        content.setText(mData.message); 
        StringBuilder versionBuilder = new StringBuilder();
        versionBuilder.append("(").append(lableVersion).append(mData.version_name).append(")");
        version.setText(versionBuilder); 
        ratingBar.setRating(mData.rating); 
        
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(mData.commenter_name);
        descriptionBuilder.append(" ");
        descriptionBuilder.append(lable_comment_on);
        descriptionBuilder.append(" ");
        String text = CommonHelperUtils.formatDateTime(mData.updated_time);
//        descriptionBuilder.append(fmt.format(new Date(mData.updated_time)));
        descriptionBuilder.append(text);
        
        description.setText(descriptionBuilder);
    }
    
    public void refreshUI(Comment data) {
        if(data == null) return;
        mData = data;
        updateUI();
    }
}
