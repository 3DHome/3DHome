package com.borqs.market.fragment;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.adapter.CommentListAdapter;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Comment;
import com.borqs.market.json.CommentJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.IntentUtil;

import butterknife.ButterKnife;

public class CommentListFragment extends BasicFragment implements OnScrollListener {

    private static final String TAG = CommentListFragment.class.getSimpleName();
    protected ListView  mListView;
    protected View layout_more;
    protected View btn_edit;
    protected TextView comment_total;
    protected RatingBar ratingBar;
    private CommentListAdapter mAdapter;
    private ArrayList<Comment> mDatas = new ArrayList<Comment>();
    private ApiUtil mApiUtil;
    private int mPage = 0;
    private final int count = 20;
    private boolean hasMore = true;
    private int totalCount = 0;
    
    private String product_id;
    private float rating;
    private int version_code;

    public static Bundle getArguments(String product_id, float rating, int version_code) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_ID, product_id);
        args.putFloat(ARGUMENTS_KEY_RATING, rating);
        args.putInt(ARGUMENTS_KEY_VERSION, version_code);
        return args;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        product_id = args.getString(ARGUMENTS_KEY_ID);
        rating = args.getFloat(ARGUMENTS_KEY_RATING);
        version_code = args.getInt(ARGUMENTS_KEY_VERSION);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        
        mListView = ButterKnife.findById(mConvertView, R.id.comment_list);
        layout_more = ButterKnife.findById(mConvertView, R.id.comment_loadingprogressbar);
        btn_edit = ButterKnife.findById(mConvertView, R.id.comment_edit);
        comment_total = ButterKnife.findById(mConvertView, R.id.comment_total);
        ratingBar = ButterKnife.findById(mConvertView, R.id.comment_ratingbar);
        mAdapter = new CommentListAdapter(mApplicationContext, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);
        
        btn_edit.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(!AccountSession.isLogin()) {
                    BasicActivity bac =  (BasicActivity)getActivity();
                    bac.login();
                    return;
                }
                IntentUtil.startCreateCommentActivity(getActivity().getApplicationContext(), product_id, version_code);
            }
        });
        
        ratingBar.setRating(rating);
        return mConvertView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mDatas != null && mDatas.size() > 0) {
            mPage = (mDatas.size()-1) / count;
            notifyDataChange();
        } else {
            getCommentList();
        }
    }

    public void onRefresh() {
        hasMore = true;
        mPage = 0;
        getCommentList();
    }

    public void loadMore() {
        mPage++;
        getCommentList();
    }

    private void notifyDataChange() {
        if (mAdapter == null) {
            mAdapter = new CommentListAdapter(mApplicationContext, mDatas);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged(mDatas);
        }
    }

    private boolean isLoading;
    private final Object mLocked = new Object();

    private void getCommentList() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }
        
        
        if (mPage == 0)
            begin();
        
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            if (mPage == 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showLoadMessage(R.string.dlg_msg_no_active_connectivity, R.drawable.biz_pic_empty_view,true);
                    }
                }, 500);
            }else {
                Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        synchronized (mLocked) {
            isLoading = true;
        }
        
        if(mApiUtil == null) {
            mApiUtil = new ApiUtil();
        }
        mApiUtil.getCommentList(mActivity.getApplicationContext(),  mPage, count, 0, product_id, new RequestListener() {
            
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
                    totalCount = obj.optInt("total");
                    ArrayList<Comment> commentList = CommentJSONImpl
                            .createCommentList(obj.optJSONArray("data"));
                    
                    if (mPage == 0 && mDatas != null) {
                        mDatas.clear();
                    }
                    if (commentList != null && commentList.size() > 0) {
                        mDatas.addAll(commentList);
                        if(mDatas.size() >= totalCount) {
                            hasMore = false;
                        }else {
                            hasMore = true;
                        }
                    }
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
    
    

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mDatas != null && mDatas.size() >0) {
            for(int i=0; i<mDatas.size(); i++) {
                mDatas.get(i).despose();
            }
            mDatas.clear();
        }
        mAdapter = null;
        mApiUtil = null;
        System.gc();
    }

    private final static int LOAD_END = 0;
    private final static String RESULT = "RESULT";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if(getActivity() == null) return;
            switch (msg.what) {

            case LOAD_END: {
                if(mPage == 0) {
                    mPage = (mDatas.size()-1) / count;
                    end();
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        notifyDataChange();
                    } else {
                        showLoadMessage(R.string.msg_loadi_failed);
                    }
                }else {
                    mPage = (mDatas.size()-1) / count;
                    end();
                    layout_more.setVisibility(View.GONE);
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        notifyDataChange();
                    } else {
                        showLoadMessage(R.string.msg_loadi_failed);
                    }
                }
                break;
            }
            }
        }
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.comment_list;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mAdapter == null) return;
        int itemsLastIndex = mAdapter.getCount(); 
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == itemsLastIndex && hasMore) {  
            if (mDatas == null || mDatas.size() == 0) {
                layout_more.setVisibility(View.GONE);
                return;
            }
                if (isLoading) {
                    BLog.v(TAG, "is Loading ...");
                    return;
                }
                layout_more.setVisibility(View.VISIBLE);
                loadMore();
        } else {
            layout_more.setVisibility(View.GONE);
        } 
        
    }
    int  visibleLastIndex = 0;  
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
//        if (mDatas == null || mDatas.size() == 0) {
//            layout_more.setVisibility(View.GONE);
//            return;
//        }
//        // mLastItemtPosition = firstVisibleItem + visibleItemCount -1;
//        if (firstVisibleItem + visibleItemCount == totalItemCount
//                && mDatas.size() % count == 0 && hasMore) {
//            if (isLoading || isLoadingRecommend) {
//                BLog.v(TAG, "is Loading ...");
//                return;
//            }
//            layout_more.setVisibility(View.VISIBLE);
//            loadMore();
//        } else {
//            layout_more.setVisibility(View.GONE);
//        }
//        
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;  
        
    }
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem,
//            int visibleItemCount, int totalItemCount) {
//        if (mDatas == null || mDatas.size() == 0) {
//            layout_more.setVisibility(View.GONE);
//            return;
//        }
//        // mLastItemtPosition = firstVisibleItem + visibleItemCount -1;
//        if (firstVisibleItem + visibleItemCount == totalItemCount
//                && mDatas.size() % count == 0 && hasMore) {
//            if (isLoading || isLoadingRecommend) {
//                BLog.v(TAG, "is Loading ...");
//                return;
//            }
//            layout_more.setVisibility(View.VISIBLE);
//            loadMore();
//        } else {
//            layout_more.setVisibility(View.GONE);
//        }
//
//    }

    @Override
    public void onLogin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub
        
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
