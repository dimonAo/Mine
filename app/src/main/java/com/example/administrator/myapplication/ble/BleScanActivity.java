package com.example.administrator.myapplication.ble;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.example.administrator.myapplication.R;

public class BleScanActivity extends AppCompatActivity {

    private Button start_scan;
    private Button stop_scan;
    private ListView device_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        start_scan = findViewById(R.id.start_scan);
        stop_scan = findViewById(R.id.stop_scan);
        device_list = findViewById(R.id.device_list);


    }
}
