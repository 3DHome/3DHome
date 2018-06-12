package com.borqs.market.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.account.AccountSession;

/**
 * Created by b608 on 14-1-17.
 */

public class ExportDescriptionFragment extends Fragment {
    public interface ClickListener {
        void onclick();
    }

    public ExportDescriptionFragment() {
        super();
    }

    private TextView mAuthorView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallpaper_export_description, null);
//            ((TextView)view.findViewById(R.id.tv_title)).setText(getSharedTitle());
//            ((TextView)view.findViewById(R.id.tv_size)).setText(DownloadUtils.getAppSize(mData.size));
//            ((TextView)view.findViewById(R.id.tv_size)).setText("");
//            ((TextView)view.findViewById(R.id.tv_version)).setText("0");
        mAuthorView = ((TextView)view.findViewById(R.id.tv_author));
        ((TextView)view.findViewById(R.id.tv_desc)).setText(getSharedDescription());
        updateAuthorInfo();
//            SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd");
//            ((TextView)view.findViewById(R.id.tv_update_time)).setText(fmt.format(new Date(System.currentTimeMillis())));
        return view;
    }

    protected void updateAuthorInfo() {
        if (null == mAuthorView) return;
        final String author;
        if (!TextUtils.isEmpty(AccountSession.account_email)) {
            author = AccountSession.account_email;
        } else if (!TextUtils.isEmpty(AccountSession.account_id)) {
            author = AccountSession.account_id;
        } else {
            author = "";
        }
        mAuthorView.setText(author);
    }

    private String getSharedDescription() {
        return getResources().getString(R.string.user_share_desc, getString(R.string.app_name));
    }

//    private String getSharedTitle() {
//        return getResources().getString(R.string.user_share_title, System.currentTimeMillis());
//    }
}
