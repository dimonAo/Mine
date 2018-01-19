package com.example.administrator.myapplication.inputkeybroad;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Administrator on 2018/1/18 0018.
 */

public class InputEdittext extends EditText {
    public InputEdittext(Context context) {
        super(context);
    }

    public InputEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InputEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ((Activity) this.getContext()).onKeyDown(KeyEvent.KEYCODE_BACK, event);

            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
}
