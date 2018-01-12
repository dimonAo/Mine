package com.example.administrator.myapplication.headset;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.spp.SppManager;

public class HeadSetActivity extends AppCompatActivity {

    private Button open, stop;
    private SppManager mSppManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_set);

        mSppManager = SppManager.getInstance(this);
        open = findViewById(R.id.open);
        stop = findViewById(R.id.stop);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
