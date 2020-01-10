package com.sunny.livechat.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/26 10:02
 */
public class KeyboardChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private View mContentView;
    private boolean isShow = false;
    private int mOriginHeight;
    private int mPreHeight;
    private KeyBoardListener mKeyBoardListen;

    public interface KeyBoardListener {

        void onKeyboardChange(boolean isShow, int keyboardHeight);
    }

    public void setKeyBoardListener(KeyBoardListener keyBoardListen) {
        this.mKeyBoardListen = keyBoardListen;
    }

    public KeyboardChangeListener(Activity contextObj) {
        if (contextObj == null) {
            return;
        }
        mContentView = findContentView(contextObj);
        if (mContentView != null) {
            addContentTreeObserver();
        }
    }

    private View findContentView(Activity contextObj) {
        return contextObj.findViewById(android.R.id.content);
    }

    private void addContentTreeObserver() {
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (mOriginHeight == 0) {
            mOriginHeight = mContentView.getHeight();
        }

        Rect rect = new Rect();
        mContentView.getWindowVisibleDisplayFrame(rect);

        mPreHeight = rect.bottom;

        int keyboardHeight = mOriginHeight - mPreHeight;

        boolean flag = false;

        if (keyboardHeight > (mOriginHeight / 4)) {
            flag = true;
        }

        if (isShow != flag) {
            isShow = flag;
            onKeyboardChange(isShow, keyboardHeight);
        }


    }


    void onKeyboardChange(boolean isShow, int keyboardHeight) {
        if (mKeyBoardListen != null) {
            mKeyBoardListen.onKeyboardChange(isShow, keyboardHeight);
        }
    }
}
