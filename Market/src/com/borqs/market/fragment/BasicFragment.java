package com.borqs.market.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.account.AccountListener;
import com.borqs.market.account.AccountObserver;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.view.LightProgressDialog;

import butterknife.ButterKnife;

public abstract class BasicFragment extends Fragment implements AccountListener{
    private static String TAG = "BasicFragment";
    
    protected static String ARGUMENTS_KEY_MOD = "ARGUMENTS_KEY_MOD";
    protected static String ARGUMENTS_KEY_TYPE = "ARGUMENTS_KEY_TYPE";
    protected static String ARGUMENTS_KEY_ID = "ARGUMENTS_KEY_ID";
    protected static String ARGUMENTS_KEY_VERSION = "ARGUMENTS_KEY_VERSION";
    protected static String ARGUMENTS_KEY_RATING = "ARGUMENTS_KEY_RATING";
    protected static String ARGUMENTS_KEY_SHOW = "ARGUMENTS_KEY_SHOW";
    protected static String ARGUMENTS_KEY_ORDERBY = "ARGUMENTS_KEY_ORDERBY";
    protected static String ARGUMENTS_KEY_PKG = "ARGUMENTS_KEY_PKG";
    protected static String ARGUMENTS_KEY_PATH = "ARGUMENTS_KEY_PATH";
    
    protected Activity mActivity;
    protected Handler mHandler;
    protected AlertDialog progress;
    protected View mConvertView;
    protected RelativeLayout loading_container;
    protected View load_content_container;
    protected View loading_layout;
    protected View btn_setting;
    private ProgressBar progressView;
    private ImageView img_empty;
    private TextView load_msg;
    
    protected Context mContext;
    protected Context mApplicationContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mContext = activity.getBaseContext();
        mApplicationContext = activity.getApplicationContext();
        createHandler();
    }

    protected abstract void createHandler();

    protected abstract int getInflatelayout();
    
    public abstract void onRefresh();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        AccountObserver.registerAccountListener(this.getClass().getName(), this);
        mConvertView = inflater.inflate(getInflatelayout(), container, false);
        loading_container = ButterKnife.findById(mConvertView, R.id.loading_container);
        if (loading_container != null) {
            loading_layout = inflater.inflate(R.layout.loading_layout, null);
            loading_layout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            loading_container.addView(loading_layout);

            load_content_container = ButterKnife.findById(loading_container, R.id.load_content_container);
            btn_setting = ButterKnife.findById(loading_container, R.id.btn_setting);
            progressView = ButterKnife.findById(loading_container, R.id.pb_loading);
            img_empty = ButterKnife.findById(loading_container, R.id.empty);
            load_msg = ButterKnife.findById(loading_container, R.id.load_msg);
        } else {
            BLog.v(TAG, "why I am null, *****************");
        }
        initView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    protected abstract void initView();

    protected void begin() {
        if (loading_container != null) {
            if (progressView != null)
                progressView.setVisibility(View.VISIBLE);
            if (load_content_container != null)
                load_content_container.setVisibility(View.GONE);
            loading_container.setVisibility(View.VISIBLE);

        }

    }

    protected void end() {
        if (loading_container != null) {
            loading_container.setVisibility(View.GONE);
            if (progressView != null)
                progressView.setVisibility(View.GONE);
            if (load_content_container != null)
                load_content_container.setVisibility(View.GONE);
        }
    }

    protected void showLoadMessage(int resId) {
        showLoadMessage(resId, R.drawable.biz_pic_empty_view, false);
    }
    
    protected void showLoadMessage(int resId, boolean showBtn) {
        showLoadMessage(resId, -1 , showBtn);
    }
    
    protected void showLoadMessage(int resId, int  img_resId, boolean showBtn) {
        if (loading_container != null) {
            if (load_content_container != null)
                load_content_container.setVisibility(View.VISIBLE);
            if (load_msg != null) {
                load_msg.setText(resId);
                load_msg.setVisibility(View.VISIBLE);
            }
            if(img_resId == -1) {
                img_empty.setVisibility(View.GONE);
            }else {
                img_empty.setVisibility(showBtn ? View.VISIBLE:View.GONE);
                img_empty.setImageResource(img_resId);
            }
            btn_setting.setVisibility(showBtn ? View.VISIBLE:View.GONE);
            btn_setting.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    IntentUtil.startWireLessSetting(getActivity());
                }
            });
            if (progressView != null)
                progressView.setVisibility(View.GONE);
            loading_container.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    onRefresh();
                }
            });
            loading_container.setVisibility(View.VISIBLE);
        }
    }

    protected void dismissProgress() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }

    }

    /**
     * Show indeterminate progress dialog with given message
     * 
     * @param message
     */
    protected void showIndeterminate(final CharSequence message) {
        dismissProgress();

        progress = LightProgressDialog.create(getActivity(), message);
        progress.setCancelable(false);
        progress.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     * 
     * @param resId
     */
    protected void showIndeterminate(final int resId) {
        dismissProgress();

        progress = LightProgressDialog.create(getActivity(), resId);
        progress.setCancelable(false);
        progress.show();
    }
    
    @Override
    public void onDestroyView() {
        AccountObserver.unRegisterAccountListener(this.getClass().getName());
        super.onDestroyView();
    }
    
    protected void registerContentObserver(Uri uri, boolean notifyForDescendents,ContentObserver observer) {
        getActivity().getContentResolver().registerContentObserver(uri, notifyForDescendents, observer);
    }
    
    protected void unregisterContentObserver(ContentObserver observer) {
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }

    protected void setTitle(String title) {
        getActivity().getActionBar().setTitle(title);
    }

    protected void setTitle(int titleId) {
        getActivity().getActionBar().setTitle(titleId);
    }
}
