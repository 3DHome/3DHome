package com.borqs.se.home3d;

import java.util.ArrayList;
import java.util.List;

import com.borqs.borqsweather.weather.yahoo.PinYinUtil;
import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.shortcut.LauncherModel.AppCallBack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class AppSearchPane extends FrameLayout implements AppCallBack {
    public int DEFAULT_HIGH_LIGHT_DISPLAY_COLOR = R.color.color_searchresult_highlight_blue;
    private Context mContext;
    private EditText mEdtSearchText;
    private GridView mAppGridView;
    private List<Item> mFullAppsList;
    private boolean mQueryWasEmpty = true;
    private int mScreenWidth;
    //private int mScreenHeight;
    private OnAppSearchItemLongClickListener mItemLongClickListener;

    public interface OnAppSearchItemLongClickListener {
        public void onItemLongClick(Item holder);
    }

    public void setItemLongClickListener(OnAppSearchItemLongClickListener l) {
        mItemLongClickListener = l;
    }

    public AppSearchPane(Context context) {
        super(context, null, 0);
        mContext = context;
        LauncherModel.getInstance().addAppCallBack(this);
        initView(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LauncherModel.getInstance().removeAppCallBack(this);
    }

    public AppSearchPane(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initView(context);
    }

    public AppSearchPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.activity_search_app, null);
        addView(v);
        setBackgroundColor(Color.BLACK);
        mEdtSearchText = (EditText) findViewById(R.id.search_text);
        mEdtSearchText.addTextChangedListener(new SearchTextWatcher());

        mAppGridView = (GridView) findViewById(R.id.app_grid);
        mAppGridView.setNumColumns(4);
        mAppGridView.setGravity(Gravity.CENTER);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        displaymetrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = displaymetrics.widthPixels;
        //mScreenHeight= displaymetrics.heightPixels;
        mAppGridView.setPadding(mScreenWidth / 40, 5, mScreenWidth / 40, 0);
        //mAppGridView.setHorizontalSpacing(mScreenWidth / 15);
        //mAppGridView.setVerticalSpacing(mScreenWidth / 15);
        mAppGridView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        //mAppGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        loadAllApps();
        mAppGridView.invalidateViews();
//        AppsAdapter ap = new AppsAdapter();
//        ap.setAppList(mFullAppsList);
//        mAppGridView.setAdapter(ap);
        mAppGridView.setOnItemClickListener(new GridItemClickListener());
        mAppGridView.setOnItemLongClickListener(new GridItemLongClickListener());

        mEdtSearchText.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    class GridItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            Item holder = (Item) arg1.getTag();
            Intent itemIntent = mContext.getPackageManager().getLaunchIntentForPackage(holder.mPkgName);
            if (itemIntent != null) {
                mContext.startActivity(itemIntent);
            } else {
                return;
            }
            ((InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(mEdtSearchText.getWindowToken(), 0);
        }
    }

    class GridItemLongClickListener implements OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                int arg2, long arg3) {
            Item holder = (Item) arg1.getTag();

            int[] position = new int[2];
            arg1.getLocationInWindow(position);
            holder.mTouchX = position[0];
            holder.mTouchY = position[1];
            if (mItemLongClickListener != null) {
                mItemLongClickListener.onItemLongClick(holder);
            }
            ((InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(mEdtSearchText.getWindowToken(), 0);
            return false;
        }

    }

    private void loadAllApps() {
        mFullAppsList = getAppListSortByName();
    }

    private List<Item> loadApps(String strFilter) {
        List<Item> appItems = new ArrayList<Item>();
        Item singleAppItem;
        if (mFullAppsList.size() > 0) {
            for (int i = 0; i < mFullAppsList.size(); i++) {
                String label = mFullAppsList.get(i).mLabel.toString();
                //some app title include unicode 'no-break space', need change it to normal space
                label = label.replace('\u00a0', ' ');
                label = label.trim();
                singleAppItem = mFullAppsList.get(i);
                singleAppItem.mLabel = label;
                String labelPinYin = PinYinUtil.getPinYinWithFirstLetter(label);
                //FIXME, getFullPinyin only return list which size is one, if it's a polyphone, should return a list;
                List<String> listPinYinFull = PinYinUtil.getFullPinyin(label);
                List<String> listNameList = PinYinUtil.getFullName(label);
                int[] resultMatchInPinYin = matchInPinYin(labelPinYin, strFilter);
                int[] resultMatchInFullPinYin;
                int[] resultMatchInFullName;
                if (resultMatchInPinYin[0] == 1) {
                    singleAppItem.mHighlightStartPos =  resultMatchInPinYin[1];
                    singleAppItem.mHighlightCount = resultMatchInPinYin[2];
                    appItems.add(singleAppItem);
                } else {
                    resultMatchInFullName = matchInFullName(label, strFilter);
                    if (resultMatchInFullName[0] == 1) {
                        singleAppItem.mHighlightStartPos = resultMatchInFullName[1];
                        singleAppItem.mHighlightCount = resultMatchInFullName[2];
                        appItems.add(singleAppItem);
                    } else {
                        resultMatchInFullPinYin = matchInFullPinYin(listNameList.get(0), listPinYinFull.get(0), strFilter);
                        if (resultMatchInFullPinYin[0] == 1) {
                            singleAppItem.mHighlightStartPos = resultMatchInFullPinYin[1];
                            singleAppItem.mHighlightCount = resultMatchInFullPinYin[2];
                            appItems.add(singleAppItem);
                        }
                    }
                }
            }
        }
        return appItems;
    }

    private int[] matchInPinYin(String str, String key) {
        int[] result = new int[3];
        result[0] = 0;
        if (str.toLowerCase().contains(key.toLowerCase())) {
            result[0] = 1;
            result[1] = str.toLowerCase().indexOf(key.toLowerCase());
            result[2] = key.length();
        }
        return result;
    }

    private int[] matchInFullPinYin(String originStr, String pinyinStr, String key) {
        int[] result = new int[3];
        result[0] = 0;
        String[] sArrayOrigin = originStr.split(",");
        String[] sArray = pinyinStr.split(",");

        int startPos = 0;
        for (int i = 0; i < sArray.length; i++) {
            String resultStr = "";
            int j;
            int count = 0;
            for (j = i; j < sArray.length; j++) {
                resultStr += sArray[j];

                count++;
                if (resultStr.startsWith(key)) {
                    for(int k = 0; k < i; k++) {
                        if (sArrayOrigin[k].length()>1) {
                            startPos += sArrayOrigin[k].length();
                        } else {
                            startPos++;
                        }
                    }
                    result[0] = 1;
                    result[1] = startPos;
                    result[2] = count;
                    break;
                }
            }
            if (result[0] == 1) {
                break;
            }
        }
        return result;
    }

    private int[] matchInFullName(String str, String key) {
        int[] result = new int[3];
        result[0] = 0;
        if (str.toLowerCase().contains(key.toLowerCase())) {
            result[0] = 1;
            result[1] = str.toLowerCase().indexOf(key.toLowerCase());
            result[2] = key.length();
        }
        return result;
    }

    public class Item {
        public CharSequence mLabel;
        public Drawable mIcon;
        public String mPkgName;
        public String mClsName;
        public int mHighlightStartPos;
        public int mHighlightCount;
        public int mTouchX;
        public int mTouchY;

        public Item(CharSequence label, Drawable icon, String pkg, String cls) {
            mLabel = label;
            mIcon = icon;
            mPkgName = pkg;
            mClsName = cls;
            mHighlightStartPos = 0;
            mHighlightCount = 0;
            mTouchX = 0;
            mTouchY = 0;
        }

        @Override
        public String toString() {
            return mLabel.toString();
        }
    }

    private List<Item> getAppListSortByName() {
        List<AppItemInfo> apps = LauncherModel.getInstance().getApps();
        List<Item> items = new ArrayList<Item>();

        //for hidded app, just hide it here
        SharedPreferences pref = HomeManager
                .getInstance()
                .getContext()
                .getSharedPreferences("add_or_hidde_app",
                        Context.MODE_MULTI_PROCESS);

        for (ItemInfo info : apps) {
            String componetName = info.getComponentName().toString();
            boolean isShow = true;
            isShow = pref.getBoolean(componetName, true);
            if(isShow){
                ResolveInfo resolveInfo = info.getResolveInfo();
                if (resolveInfo != null) {
                    String pkg = resolveInfo.activityInfo.packageName;
                    String cls = resolveInfo.activityInfo.name;
                    Item item = new Item(info.getLabel(), info.getIcon(), pkg, cls);
                    items.add(item);
                }
            }
        }

        return items;
    }

    public class AppsAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<Item> mAppsList;

        public void setAppList(List<Item> appList) {
            mAppsList = appList;
        }

        public AppsAdapter() {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            Item holder;
            holder = new Item("", null, "", "");
            if (convertView == null) {
                v = mInflater.inflate(R.layout.app_grid_item, null);
            } else {
                v = convertView;
            }

            ImageView appIcon = (ImageView)v.findViewById(R.id.itemImage);
            TextView appLabel = (TextView)v.findViewById(R.id.itemText);
            appIcon.setImageDrawable(mAppsList.get(position).mIcon);

            appLabel.setText(mAppsList.get(position).mLabel, TextView.BufferType.SPANNABLE);
            Spannable str = (Spannable) appLabel.getText();
            str.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(DEFAULT_HIGH_LIGHT_DISPLAY_COLOR)),
                    mAppsList.get(position).mHighlightStartPos,
                    mAppsList.get(position).mHighlightStartPos + mAppsList.get(position).mHighlightCount,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//            str.setSpan(new StyleSpan(Typeface.BOLD),
//                    mAppsList.get(position).mHighlightStartPos,
//                    mAppsList.get(position).mHighlightStartPos + mAppsList.get(position).mHighlightCount,
//                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            appLabel.setText(str);
            appLabel.requestLayout();
            holder.mIcon = mAppsList.get(position).mIcon;
            holder.mLabel = mAppsList.get(position).mLabel;
            holder.mPkgName = mAppsList.get(position).mPkgName;
            holder.mClsName = mAppsList.get(position).mClsName;
            v.setTag(holder);

            return v;
        }

        public final int getCount() {
            return mAppsList.size();
        }

        public final Object getItem(int position) {
            return mAppsList.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    private class SearchTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            boolean empty = s.length() == 0;
            if (empty != mQueryWasEmpty) {
                mQueryWasEmpty = empty;
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String strSearch = s.toString();
            if (TextUtils.isEmpty(s)) {
                mAppGridView.setAdapter(null);
                mAppGridView.invalidate();
            } else {
                List<Item> appsList = loadApps(strSearch);
                mAppGridView.invalidateViews();
                AppsAdapter ap = new AppsAdapter();
                ap.setAppList(appsList);
                mAppGridView.setAdapter(ap);
            }
        }
    }

    private void reloadApps() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mFullAppsList = getAppListSortByName();
                String strSearch = "";
                if (mEdtSearchText != null) {
                    strSearch = mEdtSearchText.getText().toString();
                }
                if (TextUtils.isEmpty(strSearch)) {
                    mAppGridView.setAdapter(null);
                    mAppGridView.invalidate();
                } else {
                    List<Item> appsList = loadApps(strSearch);
                    mAppGridView.invalidateViews();
                    AppsAdapter ap = new AppsAdapter();
                    ap.setAppList(appsList);
                    mAppGridView.setAdapter(ap);
                }
            }
        });
    }

    @Override
    public void bindAppsAdded(List<ItemInfo> apps) {
        reloadApps();
    }

    @Override
    public void bindAppsUpdated(List<ItemInfo> apps) {
        reloadApps();
    }

    @Override
    public void bindAppsRemoved(List<ItemInfo> apps) {
        reloadApps();
    }

    @Override
    public void bindAllPackagesUpdated() {
        // TODO Auto-generated method stub
    }

    @Override
    public void bindAllPackages() {
        // TODO Auto-generated method stub
    }

    @Override
    public void bindUnavailableApps(List<ItemInfo> apps) {
        // TODO Auto-generated method stub
    }

    @Override
    public void bindAvailableApps(List<ItemInfo> apps) {
        // TODO Auto-generated method stub
    }
}
