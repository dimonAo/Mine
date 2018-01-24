package com.example.administrator.myapplication.blet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.spp.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/23 0023.
 */

public class BleScantActivity extends AppCompatActivity {

    private static final String TAG = "BleScantActivity";

    private Button start_scan;
    private Button stop_scan;
    private ListView device_list;
    private DeviceAdapter mDeviceAdapter;
    private List<BluetoothDevice> mDevices = new ArrayList<>();

    private IBluzScanHelper mBluzScanHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        mBluzScanHelper = BluzScanHelper.getInstance(this);
        mBluzScanHelper.addOnDiscoveryListener(mOndiscoveryListener);
//        mBluzScanHelper.addOnConnectionListener(mOnConnectionListener);
        start_scan = findViewById(R.id.start_scan);
        stop_scan = findViewById(R.id.stop_scan);
        device_list = findViewById(R.id.device_list);
        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        device_list.setAdapter(mDeviceAdapter);

        addListener();
    }

    private void addListener() {
        start_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevices.clear();
                mDeviceAdapter.notifyDataSetChanged();
                mBluzScanHelper.startDiscovery();
            }
        });

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mBluzScanHelper.connect(mDevices.get(position));

                Intent intent = new Intent(BleScantActivity.this, BletConnectActivity.class);
                intent.putExtra("device_address", mDevices.get(position).getAddress());
                startActivity(intent);
            }
        });


    }


    /**
     * 扫描设备回调
     */
    private IBluzScanHelper.OnDiscoveryListener mOndiscoveryListener = new IBluzScanHelper.OnDiscoveryListener() {
        @Override
        public void onFound(BluetoothDevice device) {
            for (int i = 0; i < mDevices.size(); i++) {
                if (mDevices.get(i).getAddress().equals(device.getAddress())) {
                    return;
                }
            }
            mDevices.add(device);
            mDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDiscoveryFinished() {
            Log.e(TAG, "discovery finished");
        }
    };

//    /**
//     * 连接设备回调
//     */
//    private IBluzScanHelper.OnConnectionListener mOnConnectionListener = new IBluzScanHelper.OnConnectionListener() {
//        @Override
//        public void onConnected(BluetoothDevice device) {
//            Log.e(TAG, "connected");
//
//
//        }
//
//        @Override
//        public void onDisconnected(BluetoothDevice device) {
//            Log.e(TAG, "disconnected");
//        }
//    };

    @Override
    protected void onStart() {
        super.onStart();
        mBluzScanHelper.registBroadcast(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluzScanHelper.unregistBroadcast(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
