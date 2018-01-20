package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothProfile;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.administrator.myapplication.R;

public class BleConnectActivity extends AppCompatActivity {
    private static final String TAG = "BleConnectActivity";
    private String connectAddress;
    private Button connect;
    private BleBluetoothManager mBleBluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        mBleBluetoothManager = BleBluetoothManager.getInstance(this);

        connectAddress = getIntent().getStringExtra("device_address");
        Log.e(TAG, "connect address : " + connectAddress);
        connect = findViewById(R.id.connect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleBluetoothManager.connectBle(connectAddress, mCallback);
            }
        });


    }


    private IBleConnectCallback mCallback = new IBleConnectCallback() {
        @Override
        public void onStateChangeListener(int state) {
            //状态回调是在子线程中，做UI操作需要转到主线程
            Log.e(TAG, "main thread : " + (Looper.getMainLooper() == Looper.myLooper()));
            switch (state) {
                case BluetoothProfile.STATE_CONNECTING:
                    connect.setText("connecting");

                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    connect.setText("connected");
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:

                    connect.setText("disconnecting");
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    connect.setText("disconnected");
                    break;
            }
        }
    };
}
