package com.example.administrator.myapplication.column;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ColumnScrollActivity extends AppCompatActivity {

    private ColumnScrollbar scroll_bar;
    private SleepScrollPicker sleep_column;

    List<SleepStatisticEntity> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_scroll);

        scroll_bar = (ColumnScrollbar) findViewById(R.id.scroll_bar);

        sleep_column = (SleepScrollPicker) findViewById(R.id.sleep_column);

        for (int i = 0; i < 50; i++) {
//            mList.add((int) (12 * Math.random()));

            SleepStatisticEntity mEn = new SleepStatisticEntity();
            mEn.setmDeepSleepTime((int) (6 * Math.random()));
            mEn.setmLightSleepTime((int) (6 * Math.random()));
            mEn.setmPeriod(i + 1 + "");
            mList.add(mEn);

        }

        sleep_column.setVisibleItemCount(7);
        sleep_column.setHorizontal(true);
        sleep_column.setData(mList);
        sleep_column.setSelectedPosition(mList.size() - 1);


    }
}
