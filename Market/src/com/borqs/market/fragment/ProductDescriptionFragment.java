package com.borqs.market.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.json.Product;
import com.borqs.market.utils.DownloadUtils;


public class ProductDescriptionFragment extends Fragment {
    public static final String TAG = ProductDescriptionFragment.class.getSimpleName();
    private Product mData;
    private View view;
    private Context ctx;
    public interface ClickListener {
        void onclick();
    }

    public ProductDescriptionFragment() {
        super();
    }
    public ProductDescriptionFragment(Product data) {
        super();
        this.mData = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            this.mData = savedInstanceState.getParcelable("FLAG_DATA");
        }
        ctx = getActivity().getApplicationContext();
        Typeface fontFaceLight = Typeface.createFromAsset(ctx.getAssets(),"fonts/Roboto-Light.ttf");
        Typeface fontFace = Typeface.createFromAsset(ctx.getAssets(),"fonts/Roboto-Regular.ttf");
        view = inflater.inflate(R.layout.product_description, null);
        
        TextView lable_subject = ((TextView)view.findViewById(R.id.lable_subject));
        TextView lable_title = ((TextView)view.findViewById(R.id.lable_title));
        TextView lable_size = ((TextView)view.findViewById(R.id.lable_size));
        TextView lable_version = ((TextView)view.findViewById(R.id.lable_version));
        TextView lable_author = ((TextView)view.findViewById(R.id.lable_author));
        TextView lable_desc = ((TextView)view.findViewById(R.id.lable_desc));
        lable_update_time = ((TextView)view.findViewById(R.id.lable_update_time));
        lable_download_count = ((TextView)view.findViewById(R.id.lable_download_count));
        TextView lable_rating = ((TextView)view.findViewById(R.id.lable_rating));
        
        lable_subject.setTypeface(fontFaceLight);
        lable_title.setTypeface(fontFaceLight);
        lable_size.setTypeface(fontFaceLight);
        lable_version.setTypeface(fontFaceLight);
        lable_author.setTypeface(fontFaceLight);
        lable_desc.setTypeface(fontFaceLight);
        lable_update_time.setTypeface(fontFaceLight);
        lable_download_count.setTypeface(fontFaceLight);
        lable_rating.setTypeface(fontFaceLight);
        
        
        tv_title = ((TextView)view.findViewById(R.id.tv_title));
        tv_size = ((TextView)view.findViewById(R.id.tv_size));
        tv_version = ((TextView)view.findViewById(R.id.tv_version));
        tv_author = ((TextView)view.findViewById(R.id.tv_author));
        tv_desc = ((TextView)view.findViewById(R.id.tv_desc));
        tv_update_time = ((TextView)view.findViewById(R.id.tv_update_time));
        tv_download_count = ((TextView)view.findViewById(R.id.tv_download_count));
        id_rating = ((RatingBar)view.findViewById(R.id.id_rating));
//        tv_comment = ((TextView)view.findViewById(R.id.tv_comment));
        
        tv_title.setTypeface(fontFace);
        tv_size.setTypeface(fontFace);
        tv_version.setTypeface(fontFace);
        tv_author.setTypeface(fontFace);
        tv_desc.setTypeface(fontFace);
        tv_update_time.setTypeface(fontFace);
        tv_download_count.setTypeface(fontFace);
        
        initUI();
        return view;
    }
    
    private TextView tv_title;
    private TextView tv_size;
    private TextView tv_version;
    private TextView tv_author;
    private TextView tv_desc;
    private TextView tv_update_time;
    private TextView tv_download_count;
    private RatingBar id_rating;
    
    private TextView lable_update_time;
    private TextView lable_download_count;
    
    private void initUI() {
        if (mData != null && view != null) {

            tv_title.setText(TextUtils.isEmpty(mData.name) == true ? 
                                                   getActivity().getString(R.string.none) : mData.name);
            tv_size.setText(DownloadUtils.getAppSize(mData.size));
            tv_version.setText(TextUtils.isEmpty(mData.version_name) == true ? 
                                                   String.valueOf(mData.version_code) : mData.version_name);
            tv_author.setText(TextUtils.isEmpty(mData.author) == true ? 
                                                   getActivity().getString(R.string.none) : mData.author);
            tv_desc.setText(TextUtils.isEmpty(mData.description) == true ?  
                    "" : mData.description);
            
            if(mData.updated_time == 0L) {
                tv_update_time.setVisibility(View.GONE);
                lable_update_time.setVisibility(View.GONE);
            }else {
                SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd"); 
                tv_update_time.setText(fmt.format(new Date(mData.updated_time)));
                tv_update_time.setVisibility(View.VISIBLE);
                lable_update_time.setVisibility(View.VISIBLE);
            }
            
            if(mData.downloads_count == 0) {
                lable_download_count.setVisibility(View.GONE);
                tv_download_count.setVisibility(View.GONE);
            }else {
                tv_download_count.setText(String.valueOf(mData.downloads_count));
                lable_download_count.setVisibility(View.VISIBLE);
                tv_download_count.setVisibility(View.VISIBLE);
            }
            if(mData.rating == 0 || mData.rating == Float.NaN) {
                id_rating.setRating(4);
            }else {
                id_rating.setRating(mData.rating);
            }
        }
    }
    
    public void notifyDataChange(Product data) {
        if(data == null) return;
        this.mData = data;
        initUI();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("FLAG_DATA", mData);
        super.onSaveInstanceState(outState);
    }

}
