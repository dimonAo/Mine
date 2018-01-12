package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.myapplication.R;

public class MessageActivity extends AppCompatActivity {

    private Button btn_edit;
    private EditText message;
    private Button reconnect;

    private SppManager mSppmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mSppmanager = SppManager.getInstance(this);

        btn_edit = findViewById(R.id.edit);
        message = findViewById(R.id.message);
        reconnect = findViewById(R.id.reconnect);
        final String macAddress = getMacAddress();

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(macAddress)) {
                    BluetoothDevice mDevice = mSppmanager.mSppAdapter.getRemoteDevice(macAddress);
                    mSppmanager.connect(mDevice);
                }
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                mSppmanager.write(msg.getBytes());
            }
        });


        if (TextUtils.isEmpty(macAddress)) {
            mSppmanager.start();
        } else {
            BluetoothDevice mDevice = mSppmanager.mSppAdapter.getRemoteDevice(macAddress);
            mSppmanager.connect(mDevice);
        }

    }

    private String getMacAddress() {
        if (getIntent() == null) {
            return null;
        }
        return getIntent().getStringExtra("device_mac");
    }


}
