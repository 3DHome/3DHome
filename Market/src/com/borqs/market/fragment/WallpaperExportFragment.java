package com.borqs.market.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.json.Product;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.QiupuHelper;
import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.market.wallpaper.Wallpaper;
import com.borqs.market.wallpaper.WallpaperUtils;
import com.support.StaticUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;

public class WallpaperExportFragment extends BasicFragment implements View.OnClickListener {
    private static final String TAG = "WallpaperExportFragment";
    protected static int TOTAL_COUNT = 3;

    protected View pager_layout;
    private PhotoAdapter mAdapter;

    protected ViewPager mPager;
    protected LinearLayout mPage;
    protected LinearLayout mOpertionGroupView;
    protected Button mDownloadOrDeleteButton;
    private View processView;
    private View content_container;

    private static final int PRODUCT_STATUS_PUBLISH = 0;
    private static final int PRODUCT_STATUS_PUBLISHING = 1;
    private static final int PRODUCT_STATUS_PUBLISH_OK = 2;
    private static final int PRODUCT_STATUS_PUBLISH_FAIL = 3;
    private static final int PRODUCT_STATUS_LOGOUT = 4;
    private static final int PRODUCT_STATUS_LOGINING = 5;
    private static final int PRODUCT_STATUS_SCAN_FAIL = 6;
    private static final int PRODUCT_STATUS_QUOTA_FULL = 7;
    private static final int PRODUCT_STATUS_EMPTY_DATA = 8;
    private int PRODUCT_STATUS = PRODUCT_STATUS_LOGOUT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        performDecodingTask();
    }

    private void performDecodingTask() {
        if (null != mScanLocalProductTask && mScanLocalProductTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScanLocalProductTask.cancel(true);
        }
        mScanLocalProductTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                if (!QiupuHelper.isEnoughSpace()) {
                    setTitle(R.string.storage_full);
                    PRODUCT_STATUS = PRODUCT_STATUS_QUOTA_FULL;
                    cancel(true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                if (isCancelled()) {
                    return false;
                }

                mDecodedList = WallpaperUtils.decodePaper(mContext, mOriginList);
                return (null != mDecodedList && !mDecodedList.isEmpty());
            }

            @Override
            protected void onPostExecute(Boolean succeed) {
                Message mds = mHandler.obtainMessage(LOAD_END);
                mds.getData().putBoolean(RESULT, succeed);

                if (succeed) {
                    createAdapter();
                } else {
                    setTitle(R.string.scanning_fail);
                    PRODUCT_STATUS = PRODUCT_STATUS_SCAN_FAIL;
                }
                if (isCancelled()) {
                    Log.w(TAG, "onPostExecute, task was cancelled...");
                }
                mHandler.sendMessage(mds);
            }

            @Override
            protected void onCancelled() {
                Message mds = mHandler.obtainMessage(LOAD_END);
                mds.getData().putBoolean(RESULT, true);

                if (isCancelled()) {
                    Log.w(TAG, "onPostExecute, task was cancelled...");
                }
                mHandler.sendMessage(mds);
            }
        };

        mScanLocalProductTask.execute(null, null, null);
    }

    @Override
    protected void initView() {
        pager_layout = ButterKnife.findById(mConvertView, R.id.pager_layout);
        mPager = ButterKnife.findById(mConvertView, R.id.mPager);
        mOpertionGroupView = ButterKnife.findById(mConvertView, R.id.opertion_group_view);
        mPage = ButterKnife.findById(mConvertView, R.id.pages);
        mDownloadOrDeleteButton = ButterKnife.findById(mConvertView, R.id.delete_or_download);
        mDownloadOrDeleteButton.setOnClickListener(this);
        processView = ButterKnife.findById(mConvertView, R.id.process_view);
        content_container = ButterKnife.findById(mConvertView, R.id.content_container);

        PhotoOnPageChangeListener pageChangeListener = new PhotoOnPageChangeListener();
        mPager.setOnPageChangeListener(pageChangeListener);

        pager_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem
                // that we can swipe only the middle view.
                return mPager.dispatchTouchEvent(event);
            }
        });
    }

    @Override
    protected void begin() {
        super.begin();
        content_container.setVisibility(View.GONE);
    }

    @Override
    protected void end() {
        super.end();
        content_container.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parseIntent(activity.getIntent());
    }

    protected void parseIntent(Intent intent) {
        appVersionCode = intent.getIntExtra(MarketUtils.EXTRA_APP_VERSION, 0);
        packageName = intent.getStringExtra(MarketUtils.EXTRA_PACKAGE_NAME);
        supported_mod = intent.getStringExtra(MarketUtils.EXTRA_MOD);
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("package name is null");
        }
        mOriginList = (ArrayList<RawPaperItem>) intent.getSerializableExtra(WallpaperUtils.EXTRA_RAW_WALL_PAPERS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (loading_layout != null)
            loading_layout.setBackgroundResource(R.color.transparent);

        return mConvertView;
    }

    @Override
    public void onResume() {
        refreshUI();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void refreshProductStatus(int status) {
        PRODUCT_STATUS = status;
        processView.setVisibility(View.GONE);

        if (PRODUCT_STATUS == PRODUCT_STATUS_LOGOUT) {
            mDownloadOrDeleteButton.setText(R.string.action_login);
            mDownloadOrDeleteButton.setClickable(true);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_LOGINING) {
            mDownloadOrDeleteButton.setText(R.string.lable_being_login);
            mDownloadOrDeleteButton.setClickable(true);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_PUBLISH) {
            mDownloadOrDeleteButton.setText(R.string.publish);
            mDownloadOrDeleteButton.setClickable(true);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_PUBLISHING) {
            mDownloadOrDeleteButton.setText(R.string.publishing);
            mDownloadOrDeleteButton.setClickable(false);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_PUBLISH_OK) {
            mDownloadOrDeleteButton.setText(R.string.published);
            mDownloadOrDeleteButton.setClickable(false);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_PUBLISH_FAIL || PRODUCT_STATUS == PRODUCT_STATUS_SCAN_FAIL) {
            mDownloadOrDeleteButton.setText(R.string.publish_again);
            mDownloadOrDeleteButton.setClickable(true);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_QUOTA_FULL) {
            mDownloadOrDeleteButton.setText(R.string.storage_full);
            mDownloadOrDeleteButton.setClickable(true);
        }
        mDownloadOrDeleteButton.setVisibility(View.VISIBLE);
    }

    private void createAdapter() {
        mAdapter = new PhotoAdapter(getFragmentManager(), mDecodedList, supported_mod);
        mPager.setAdapter(mAdapter);
    }

    protected void refreshUI() {
        if (isDetached()) {
            BLog.d(TAG, "fragment is detached!");
            return;
        }

        if (mAdapter == null
                || (null == mScanLocalProductTask && mScanLocalProductTask.getStatus() == AsyncTask.Status.RUNNING)) {
            begin();
            setTitle(R.string.loading);

            return;
        } else {
            end();
            setTitle(R.string.action_export_wallpaper);
        }

        if (mAdapter == null) {
            createAdapter();
        } else {
            mAdapter.notifyDataSetChanged();
        }
        BLog.d(TAG, "page count = " + mAdapter.getCount());
        if (AccountSession.isLogin()) {
            refreshProductStatus(PRODUCT_STATUS == PRODUCT_STATUS_LOGOUT ? PRODUCT_STATUS_PUBLISH : PRODUCT_STATUS);
        } else {
            refreshProductStatus(PRODUCT_STATUS_LOGOUT);
        }

        mPage.removeAllViews();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getActivity());
            if (i == 0) {
                dot.setBackgroundResource(R.drawable.indicator_focus);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_normal);
            }
            mPage.addView(dot, params);
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final static int LOAD_END = 0;
    private final static int PURCHASE_END = 1;
    private final static int PUBLISH_BEGIN = 2;
    private final static int PUBLISH_END = 3;
    private final static String RESULT = "RESULT";
    private final static String URL = "URL";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOAD_END: {
                end();
                if (getActivity() == null)
                    return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    refreshUI();
                } else {
                    showLoadMessage(R.string.msg_loadi_failed);
                }
                break;
            }
            case PUBLISH_BEGIN: {
                if (getActivity() == null)
                    return;
                StaticUtil.staticWallpaper(mContext, "WALLPAPER_SHOW_LAUNCH");
                refreshProductStatus(PRODUCT_STATUS_PUBLISHING);
                break;
            }
            case PUBLISH_END: {
                if (getActivity() == null)
                    return;
                boolean suc = msg.getData().getBoolean(RESULT);

                if (suc) {
                    StaticUtil.staticWallpaper(mContext, "WALLPAPER_SHOW_SUCCESS");
                    refreshProductStatus(PRODUCT_STATUS_PUBLISH_OK);
                    Activity activity = getActivity();
                    if (null != activity && activity instanceof BasicActivity) {
                        ((BasicActivity) activity).goBackToScene();
                        activity.finish();
                        MarketUtils.startUserShareListIntent(activity, Product.ProductType.WALL_PAPER, supported_mod);
                    }
                } else {
                    StaticUtil.staticWallpaper(mContext, "WALLPAPER_SHOW_FAIL");
                    refreshProductStatus(PRODUCT_STATUS_PUBLISH_FAIL);
                    showLoadMessage(R.string.msg_show_paper_failed);
                }
                break;
            }
            case PURCHASE_END: {
                if (getActivity() == null)
                    return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {

                } else {

                }
                break;
            }
            }
        }
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.paper_export_fragment;
    }

    @Override
    public void onRefresh() {
    }

    public class PhotoOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < mPage.getChildCount(); i++) {
                ImageView dot = (ImageView) mPage.getChildAt(i);
                if (i == position) {
                    dot.setBackgroundResource(R.drawable.indicator_focus);
                } else {
                    dot.setBackgroundResource(R.drawable.indicator_normal);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (pager_layout != null) {
                pager_layout.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    private void postRefreshUi(final int status) {
        Activity activity = getActivity();
        if (null != activity && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUI();
                    refreshProductStatus(status);
                }
            });
        }
    }

    @Override
    public void onLogin() {
        postRefreshUi(PRODUCT_STATUS_PUBLISH);
    }

    @Override
    public void onLogout() {
        postRefreshUi(PRODUCT_STATUS_LOGOUT);
    }

    @Override
    public void onCancelLogin() {
        postRefreshUi(PRODUCT_STATUS_LOGOUT);
    }

    private static class PhotoAdapter extends FragmentPagerAdapter {
        private ArrayList<RawPaperItem> mItemList = null;
        private ExportDescriptionFragment mDescription;

        public PhotoAdapter(FragmentManager fm, ArrayList<RawPaperItem> wallList, String supported_mod) {
            super(fm);
            mItemList = wallList;
            Collections.reverse(mItemList);
        }

        @Override
        public int getCount() {
            int count = 1;
            count += null == mItemList ? 0 : mItemList.size();
            return count;
        }

        @Override
        public void notifyDataSetChanged() {
            if (null != mDescription) {
                mDescription.updateAuthorInfo();
            }

            super.notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                mDescription = new ExportDescriptionFragment();
                return mDescription;
            } else {
                final int wallSize = null == mItemList ? 0 : mItemList.size();
                String filePath = null;
                if (position < wallSize + 1) {
                    filePath = mItemList.get(position - 1).mImagePath;
                }

                if (TextUtils.isEmpty(filePath)) {
                    Log.e(TAG, "getItem, should not be here");
                }

                final String url = Uri.fromFile(new File(filePath)).toString();
                PhotoFragment f = new PhotoFragment();
                f.setArguments(PhotoFragment.getArguments(url, Product.ProductType.WALL_PAPER));

                return f;
            }
        }
    }

    protected String product_type = Product.ProductType.THEME;
    protected String packageName;
    private String supported_mod;
    protected int appVersionCode = 0;

    private ArrayList<RawPaperItem> mOriginList = new ArrayList<RawPaperItem>();
    private ArrayList<RawPaperItem> mDecodedList;

    public WallpaperExportFragment() {
        super();
        this.product_type = Product.ProductType.WALL_PAPER;
    }

    @Override
    protected void showLoadMessage(int resId) {
        Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT).show();
    }

    private void sendMessage(int code, boolean result) {
        Message mds = mHandler.obtainMessage(code);
        mds.getData().putBoolean(RESULT, result);
        mHandler.sendMessage(mds);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.delete_or_download) {
            if (PRODUCT_STATUS == PRODUCT_STATUS_LOGOUT) {
                ((BasicActivity) getActivity()).login();
                refreshProductStatus(PRODUCT_STATUS_LOGINING);
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_PUBLISH || PRODUCT_STATUS == PRODUCT_STATUS_PUBLISH_FAIL) {
                doPublish();
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_SCAN_FAIL) {
                performDecodingTask();
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_QUOTA_FULL) {
                // todo: show clean disk dialog
            }
        }
    }

    private void doPublish() {
        if (!checkAndPromptNetworking()) {
            return;
        }

        final String title = getSummary();
        if (TextUtils.isEmpty(title)) {
            mPager.setCurrentItem(0, true);
            showLoadMessage(R.string.prompt_empty_title);

            TextView textView = ButterKnife.findById(getActivity(), R.id.tv_title);
            if (null != textView) {
                textView.requestFocus();
            }
            return;
        }

        final String authorName = getAuthorName();
        if (TextUtils.isEmpty(authorName)) {
            TextView textView = ButterKnife.findById(getActivity(), R.id.tv_author);
            if (null != textView) {
                textView.requestFocus();
                return;
            }
        }

        final String description = getDescription();
        if (TextUtils.isEmpty(description)) {
            TextView textView = ButterKnife.findById(getActivity(), R.id.tv_desc);
            if (null != textView) {
                textView.requestFocus();
                return;
            }
        }

        final Wallpaper.Builder builder = new Wallpaper.Builder(AccountSession.account_id);
        builder.setPackageName(packageName).setAppVersion(appVersionCode).setEmail(AccountSession.account_email)
                .setSummary(title).setTitle(title).setUserName(authorName).setDescription(description)
                .setPapers(mDecodedList);

        sendMessage(PUBLISH_BEGIN, true);

        new Thread() {

            @Override
            public void run() {
                WallpaperUtils.exportWallpaperSuite(builder, supported_mod, new RequestListener() {
                    @Override
                    public void onComplete(String response) {
                        Log.i(TAG, "performUpload, wallpaper upload complete, " + response);
                        sendMessage(PUBLISH_END, true);
                    }

                    @Override
                    public void onIOException(IOException e) {
                        e.printStackTrace();
                        sendMessage(PUBLISH_END, false);
                        Log.e(TAG, "performUpload, wallpaper upload exception, " + e.getMessage());
                    }

                    @Override
                    public void onError(WutongException e) {
                        e.printStackTrace();
                        sendMessage(PUBLISH_END, false);
                        Log.e(TAG, "performUpload, wallpaper upload error, " + e.getMessage());
                    }
                });
            }

        }.start();
       
    }

    private String getSummary() {
        TextView textView = ButterKnife.findById(getActivity(), R.id.tv_title);
        if (null != textView) {
            String summary = textView.getText().toString();
            return null == summary ? "" : summary;
        }
        return "";
    }

    private String getAuthorName() {
        TextView textView = ButterKnife.findById(getActivity(), R.id.tv_author);
        if (null != textView) {
            String summary = textView.getText().toString();
            return null == summary ? "" : summary;
        }
        return "";
    }

    private String getDescription() {
        TextView textView = ButterKnife.findById(getActivity(), R.id.tv_desc);
        if (null != textView) {
            String summary = textView.getText().toString();
            return null == summary ? "" : summary;
        }
        return "";
    }

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub
    }

    private AsyncTask<Void, Void, Boolean> mScanLocalProductTask;

    @Override
    public void onDetach() {
        super.onDetach();
        if (mScanLocalProductTask != null && mScanLocalProductTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScanLocalProductTask.cancel(true);
        }
    }

    private boolean checkAndPromptNetworking() {
        Activity activity = getActivity();
        if (DataConnectionUtils.testValidConnection(activity)) {
            return true;
        }

        if (!activity.isFinishing()) {
            Toast.makeText(activity, R.string.no_networking, Toast.LENGTH_SHORT).show();
            PRODUCT_STATUS = PRODUCT_STATUS_PUBLISH_FAIL;
            refreshUI();
        }
        return false;
    }
}
