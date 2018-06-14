/*
 * Copyright © 2013 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, 
 * or disclosed, or disseminated outside of Borqs in whole or in part without Borqs 's permission.
 */

/* 
 * Description of the content in this file. 
 * 
 * History core-id Change purpose 
 * ---------- ------- ---------------------------------------------- 
 *
 */

package com.borqs.se.addobject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.funyoung.androidfacade.CommonHelperUtils;

/**
 * For folder edit.
 */
public class AddObjectEditText extends EditText {
    
    public IMStatusListener listener;

    public AddObjectEditText(Context context) {
        super(context);
        init();
    }

    public AddObjectEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AddObjectEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    

    private void init() {
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    setCursorVisible(true);
                }
                if (listener != null) {
                    listener.statusChanged(hasFocus);
                }
            }
        });
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCursorVisible(true);
            }
        });
        setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) ||
                    (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    hideIM();
                    return true;
                }
                if (event != null) {
                    if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && 
                        (event.getAction() == KeyEvent.ACTION_DOWN)) {
                        hideIM();
                        return true;
                    }
                }
                return false;
            }
        });
    }
    
    public void setListener(IMStatusListener l) {
        listener = l; 
    }
    
    public void hideIM() {
        setCursorVisible(false);
        clearFocus();
        CommonHelperUtils.hideIme(getContext(), getWindowToken());
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            hideIM();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    
    public interface IMStatusListener {
        void statusChanged(boolean show);
    }
}
