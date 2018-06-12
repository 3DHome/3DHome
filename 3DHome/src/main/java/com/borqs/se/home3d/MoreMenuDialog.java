package com.borqs.se.home3d;

import com.borqs.se.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class MoreMenuDialog extends Dialog {
    // 实现默认构造函数
    public MoreMenuDialog(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub
    }

    protected MoreMenuDialog(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        // TODO Auto-generated constructor stub
    }

    public MoreMenuDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    // 所有的方法执行完都会返回一个Builder使得后面可以直接create和show
    public static class Builder {
        private Context context;

        private BaseAdapter adapter;// listview的adapter

        // listview的item点击事件
        private AdapterView.OnItemClickListener listViewOnclickListener;

        public Builder(Context context) {
            this.context = context;
        }

        // 设置适配器
        public Builder setAdapter(BaseAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        // 设置点击事件
        public Builder setOnClickListener(
                AdapterView.OnItemClickListener listViewOnclickListener) {
            this.listViewOnclickListener = listViewOnclickListener;
            return this;
        }

        // createview方法
        public MoreMenuDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 设置其风格
            final MoreMenuDialog dialog = new MoreMenuDialog(context,R.style.moreMenuDialogTheme);

            View layout = inflater.inflate(R.layout.menu_dialog_layout, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            // 设置listview的adapter如果没有就隐藏listview
            if (adapter != null && adapter.getCount() > 0) {
                ListView listView = (ListView) layout
                        .findViewById(R.id.moremenu_listView);
                listView.setAdapter(adapter);
                if (listViewOnclickListener != null) {

                    listView.setOnItemClickListener(listViewOnclickListener);
                }

            } else {
                layout.findViewById(R.id.moremenu_listView).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }
    }
}
