package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.spp.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class BleScanActivity extends AppCompatActivity {

    private Button start_scan;
    private Button stop_scan;
    private ListView device_list;
    private DeviceAdapter mDeviceAdapter;
    private BleBluetoothManager mBleBluetoothManager;

    List<BluetoothDevice> mDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        mBleBluetoothManager = BleBluetoothManager.getInstance(this);

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
                mBleBluetoothManager.startScan(true, mBleScanDeviceResult);
            }
        });

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBleBluetoothManager.stopScan();
                Intent addressIntent = new Intent();
                addressIntent.setClass(BleScanActivity.this, BleConnectActivity.class);
                addressIntent.putExtra("device_address", mDevices.get(position).getAddress());
                startActivity(addressIntent);
            }
        });

        stop_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleBluetoothManager.stopScan();
            }
        });


    }

    private BleScanDeviceResult mBleScanDeviceResult = new BleScanDeviceResult() {
        @Override
        public void getScanBleDevice(BluetoothDevice mDevice) {
            for (int i = 0; i < mDevices.size(); i++) {
                if (mDevice.getAddress().equals(mDevices.get(i).getAddress())) {
                    return;
                }
            }
            mDevices.add(mDevice);
            mDeviceAdapter.notifyDataSetChanged();
        }
    };
}
