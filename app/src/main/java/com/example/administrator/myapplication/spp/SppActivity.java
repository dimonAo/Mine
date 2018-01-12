package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SppActivity extends AppCompatActivity {
    private static final String TAG = "SppActivity";


    private Button btn_discovery;
    private Button btn_cancel;
    private Button btn_bonded;

    private ListView device_list;
    private DeviceAdapter mDeviceAdapter;

    private SppManager mSppManager;


    private static final int ENABLE_BLUETOOTH_REQUEST = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spp);
        mSppManager =SppManager.getInstance(this);

        initView();


    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBluetoothReciver();
//        mSppManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBluetoothReceiver();
    }

    private void initView() {
        btn_discovery = findViewById(R.id.discovery);
        btn_cancel = findViewById(R.id.cancel);
        device_list = findViewById(R.id.device_list);
        btn_bonded = findViewById(R.id.bonded);


        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        device_list.setAdapter(mDeviceAdapter);

        if (!mSppManager.deviceSupportBluetooth()) {
            Log.e(TAG, "this device not support bluetooth");
            finish();
        }

        if (!mSppManager.deviceEnableBluetooth()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST);
        }

        addListener();
    }

    private void addListener() {

        btn_bonded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Set<BluetoothDevice> set = mSppManager.getBondDevices();
//                Log.e(TAG, "set size : " + set.size());
//                for (BluetoothDevice device : set) {
//                    Log.e(TAG, "bond name : " + device.getName() + " \n bond address : " + device.getAddress());
//                }
                startActivity(new Intent(SppActivity.this, MessageActivity.class));

            }
        });


        btn_discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevices.clear();
                mDeviceAdapter.notifyDataSetChanged();

//                mSppManager.deviceEnableBluetoothDiscovery(0);
                mSppManager.deviceStartDiscovery();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSppManager.deviceCancelDiscovery();
            }
        });

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSppManager.deviceCancelDiscovery();
                BluetoothDevice currentDevice = mDevices.get(position);
//                if (mSppManager.createBond(currentDevice)) {
//                    Log.e(TAG, "current device is bond");
//                mSppManager.connect(currentDevice);


                Intent intent = new Intent(SppActivity.this, MessageActivity.class);
                intent.putExtra("device_mac", currentDevice.getAddress());
                startActivity(intent);


//                }
//                mSppManager.connect(currentDevice);
//                ParcelUuid[] uuids = currentDevice.getUuids();
//                Log.e(TAG, "uuids : " + uuids.toString());


//                for (int i = 0; i < uuids.length; i++) {
//                    Log.e(TAG, "uuid ID (" + i + ") : " + uuids[i].toString() + "\n");
//                }


            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ENABLE_BLUETOOTH_REQUEST == requestCode) {
            switch (resultCode) {
                case RESULT_OK:
                    Log.e(TAG, "device take on the bluetooth");


                    break;

                case RESULT_CANCELED:
                    Log.e(TAG, "device not enable bluetooth");
                    break;
            }
        }
    }

    List<BluetoothDevice> mDevices = new ArrayList<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "device_address : " + device.getAddress());
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                for (int i = 0; i < mDevices.size(); i++) {
                    if (device.getAddress().equals(mDevices.get(i).getAddress())) {
//                            mDevices.set(i, device);
                        return;
                    }
                }
                mDevices.add(device);
                mDeviceAdapter.notifyDataSetChanged();


//                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "discovery is finish ");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                        BluetoothDevice.ERROR);


                switch (state) {
                    case BluetoothDevice.BOND_NONE:
                        Log.e(TAG, "bond none ");
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        Log.e(TAG, "bond bonded ");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e(TAG, "bond bonding");
                        break;
                }


            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.e(TAG, "device connecting ");
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.e(TAG, "device connected ");
                        break;

                    case BluetoothAdapter.STATE_DISCONNECTING:
                        Log.e(TAG, "device disconnecting");
                        break;

                    case BluetoothAdapter.STATE_DISCONNECTED:
                        Log.e(TAG, "device disconnected");
                        break;
                }


            }
        }
    };

    private void registerBluetoothReciver() {
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        foundFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, foundFilter);

        IntentFilter finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, finishFilter);
    }


    private void unregisterBluetoothReceiver() {
        unregisterReceiver(mReceiver);
    }


//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//
//        }
//    };
}
