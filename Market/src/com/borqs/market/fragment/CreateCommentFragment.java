package com.borqs.market.fragment;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Comment;
import com.borqs.market.json.CommentJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.view.LightProgressDialog;

import butterknife.ButterKnife;

public class CreateCommentFragment extends BasicFragment {

    private static final String TAG = CreateCommentFragment.class.getSimpleName();
    protected RatingBar ratingBar;
    protected EditText comment_edit;
    protected View comment_publish;
    private Comment mData = null;
    private ApiUtil mApiUtil;
    
    private String product_id;
    private int version;
    
    private String message = null;
    private float rating;

    public static Bundle getArguments(String product_id, int version) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_ID, product_id);
        args.putInt(ARGUMENTS_KEY_VERSION, version);
        return args;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        product_id = args.getString(ARGUMENTS_KEY_ID);
        version = args.getInt(ARGUMENTS_KEY_VERSION);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        ratingBar = ButterKnife.findById(mConvertView, R.id.comment_ratingbar);
        comment_edit = ButterKnife.findById(mConvertView, R.id.comment_edit);
        comment_publish = ButterKnife.findById(mConvertView, R.id.comment_publish);
        
        comment_publish.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(!AccountSession.isLogin()) {
                    BasicActivity bac =  (BasicActivity)getActivity();
                    bac.login();
                    return;
                }
                rating = ratingBar.getRating()/5;
                if(rating <= 0) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.plase_give_a_score, Toast.LENGTH_SHORT).show();
                    return;
                }
                message = comment_edit.getText().toString();
                if(TextUtils.isEmpty(message)) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.comment_not_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                editComment();
            }
        });
        return mConvertView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mData != null ) {
            notifyDataChange();
        } else {
            getMyComment();
        }
    }

    public void onRefresh() {
        getMyComment();
    }

    private void notifyDataChange() {
        if(mData != null) {
            ratingBar.setRating(mData.rating);
            comment_edit.setText(mData.message);
        }
    }

    private boolean isLoading;
    private final Object mLocked = new Object();

    private void getMyComment() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }
        
        
        begin();
        
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        synchronized (mLocked) {
            isLoading = true;
        }
        
        if(mApiUtil == null) {
            mApiUtil = new ApiUtil();
        }
        mApiUtil.getMyComment(mActivity.getApplicationContext(), version, product_id, new RequestListener() {
            
            @Override
            public void onIOException(IOException e) {
                BLog.e(TAG,"getCommentList onIOException  "
                        + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(LOAD_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);
                
            }
            
            @Override
            public void onError(WutongException e) {
                BLog.e(TAG, "onError  " + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(LOAD_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);
                
            }
            
            @Override
            public void onComplete(String response) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    if (!obj.has("data"))
                        return;
                    mData = new CommentJSONImpl(obj.optJSONObject("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(LOAD_END);
                mds.getData().putBoolean(RESULT, true);
                mHandler.sendMessage(mds);
                
            }
        });
    }
    
    AlertDialog sendDialog = null;
    private void editComment() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }
        
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        if(sendDialog == null) {
            sendDialog = LightProgressDialog.create(getActivity(), R.string.label_comment_sending);
            sendDialog.setCanceledOnTouchOutside(false);
        }
        sendDialog.show();
        
        synchronized (mLocked) {
            isLoading = true;
        }
        
        if(mApiUtil == null) {
            mApiUtil = new ApiUtil();
        }
        mApiUtil.createComment(mActivity.getApplicationContext(), version, product_id, message, rating, new RequestListener() {
            
            @Override
            public void onIOException(IOException e) {
                BLog.e(TAG,"getCommentList onIOException  "
                                + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(SEND_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);
                
            }
            
            @Override
            public void onError(WutongException e) {
                BLog.e(TAG, "onError  " + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(SEND_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);
                
            }
            
            @Override
            public void onComplete(String response) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    if (!obj.has("data"))
                        return;
                    mData = new CommentJSONImpl(obj.optJSONObject("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                Message mds = mHandler.obtainMessage(SEND_END);
                mds.getData().putBoolean(RESULT, true);
                mHandler.sendMessage(mds);
                
            }
        });
    }
    
    

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mData != null) {
            mData.despose();
            mData = null;
        }
        mApiUtil = null;
        System.gc();
    }

    private final static int LOAD_END = 0;
    private final static int SEND_END = 1;
    private final static String RESULT = "RESULT";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if(getActivity() != null && !getActivity().isFinishing()) {
                boolean suc = msg.getData().getBoolean(RESULT);
                switch (msg.what) {
                case LOAD_END: {
                    end();
                    if (suc) {
                        notifyDataChange();
                    } else {
                        showLoadMessage(R.string.msg_loadi_failed);
                    }
                    break;
                }
                case SEND_END: {
                    if(sendDialog != null) {
                        sendDialog.dismiss();
                    }
                    if(suc) {
                        Toast.makeText(getActivity(), R.string.send_successed, Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }else {
                        Toast.makeText(getActivity(), R.string.send_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                }
            }
        }
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.comment_edit;
    }

    @Override
    public void onLogin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLogout() {
        getActivity().finish();
        
    }

    @Override
    public void onCancelLogin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void initView() {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void onLoging() {
        // TODO Auto-generated method stub
        
    }
}
