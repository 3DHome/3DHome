package com.borqs.se.home3d;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.borqs.se.R;

public class SearchActivity extends Activity {
    private boolean mQueryWasEmpty = true;
    private EditText mQueryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setTitle(R.string.search_activity_title);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setContentView(R.layout.activity_search);
        mQueryTextView = findViewById(R.id.search_src_text);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        //mQueryTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE); 
        mQueryTextView.setHorizontallyScrolling(false);

        Button btnSearch = findViewById(R.id.search_go_btn);
        LayoutParams laParams= btnSearch.getLayoutParams();
        laParams.width = width / 3;
        btnSearch.setLayoutParams(laParams);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strSearch = mQueryTextView.getText().toString();
                if (!TextUtils.isEmpty(strSearch)) {
                    String url = "http://www.google.com/?cx=partner-pub-9392483945688336:4672723608&ie=UTF-8&q="+strSearch+"&sa=Search";
                    Uri uriUrl = Uri.parse(url);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                    finish();
                }
            }
        });
        mQueryTextView.addTextChangedListener(new SearchTextWatcher());
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
        }
    }
}
