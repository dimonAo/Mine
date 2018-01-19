package com.example.administrator.myapplication.inputkeybroad;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.myapplication.R;

import java.util.Timer;
import java.util.TimerTask;

public class InputMethodActivity extends AppCompatActivity {
    TextView textView;
    InputEdittext editText;
    Button show, hide;

    InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_method);

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        show = findViewById(R.id.show);
        hide = findViewById(R.id.hide);

//        KeyBoard(editText, "close");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            KeyBoard(editText, "close");
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 强制显示或关闭键盘
     *
     * @param txtSearchKey
     * @param status
     */
    public void KeyBoard(final EditText txtSearchKey, final String status) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager m = (InputMethodManager)
                        txtSearchKey.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (status.equals("open")) {
                    m.showSoftInput(txtSearchKey, InputMethodManager.SHOW_FORCED);
                } else {
                    m.hideSoftInputFromWindow(txtSearchKey.getWindowToken(), 0);
                }
            }
        }, 0);
    }
}
