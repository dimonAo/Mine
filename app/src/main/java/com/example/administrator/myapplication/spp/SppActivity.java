package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.headset.HeadSetActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SppActivity extends AppCompatActivity implements TranProtocalAnalysis.OnDeviceButtonPressedStateListener {
    private static final String TAG = "SppActivity";


    private Button btn_discovery;
    private Button btn_cancel;
    private Button btn_bonded;
    private Button btn_audio;
    private EditText edit;

    private ListView device_list;
    private DeviceAdapter mDeviceAdapter;

    //    private SppManager mSppManager;
    private SppBluetoothManager mSppManager;
    //    private static final UUID CONNECT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CONNECT_UUID = UUID.fromString("00000000-0000-0000-0099-aabbccddeeff");
    private static final byte[] UUID_AIROHA1520 = {0, 0, 0, 0, 0, 0, 0, 0, 0, -103, -86, -69, -52, -35, -18, -1};

    private static final int ENABLE_BLUETOOTH_REQUEST = 0x01;
    private TranProtocalAnalysis mTranProtocalAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spp);
        mSppManager = SppBluetoothManager.getInstance(this);
        mTranProtocalAnalysis = TranProtocalAnalysis.getTranProtocalAnalysis(this);
        mTranProtocalAnalysis.setOnDevicePressedStateListener(this);
//        mSppManager.setConnectUUid(CONNECT_UUID);
        mSppManager.setBluetoothListener(new SppBluetoothManager.BluetoothListener() {
            @Override
            public void notifyChangeConnectstate(int mState) {
                Log.e(TAG, "notigy change connect state : " + mState);
                if (mState == SppBluetoothManager.STATE_CONNECTED) {
//                    Intent intent = new Intent(SppActivity.this, MessageActivity.class);
//                    startActivity(intent);
                    Log.e(TAG, "spp aty connected ");
                }
            }

            @Override
            public void foundBluetoothDevice(BluetoothDevice mDevice) {
                Log.e(TAG, "" + mDevice.getName() + " " + mDevice.getAddress());
                for (int i = 0; i < mDevices.size(); i++) {
                    if (mDevice.getAddress().equals(mDevices.get(i).getAddress())) {
//                            mDevices.set(i, device);
                        return;
                    }
                }
                mDevices.add(mDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void scanBluetoothFinish() {
                Log.e(TAG, "scan finish");
            }

        });

        initView();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSppManager != null) {
            if (mSppManager.getState() == SppBluetoothManager.STATE_NONE) {
                mSppManager.start();
            }
        }
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
//        mSppManager.stop();
    }

    private void initView() {
        btn_discovery = findViewById(R.id.discovery);
        btn_cancel = findViewById(R.id.cancel);
        device_list = findViewById(R.id.device_list);
        btn_bonded = findViewById(R.id.bonded);
        btn_audio = findViewById(R.id.audio);
        edit = findViewById(R.id.edit);

        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        device_list.setAdapter(mDeviceAdapter);

        if (!mSppManager.supportBluetooth()) {
            Log.e(TAG, "this device not support bluetooth");
            finish();
        }

        if (!mSppManager.enableBluetooth()) {
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
//                startActivity(new Intent(SppActivity.this, MessageActivity.class));
//                String ss = "{\"src\":\"zh-CN\",\"des\":\"en-US\",\"rec\":\"今天天气不错。\",\"tra\":\"Today's weather is fine.\"}";
//                String ss = edit.getText().toString();
//                mTranProtocalAnalysis.writeToDevice(ss);
                mSppManager.getConnectBt();

            }
        });

        btn_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SppActivity.this, HeadSetActivity.class));
            }
        });


        btn_discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevices.clear();
                mDeviceAdapter.notifyDataSetChanged();
                Set<BluetoothDevice> mdevice = mSppManager.mBluetoothAdapter.getBondedDevices();
                mDevices.addAll(mdevice);
                mDeviceAdapter.notifyDataSetChanged();
//                mSppManager.deviceEnableBluetoothDiscovery(0);
                mSppManager.startDiscovery();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSppManager.stopDiscovery();
                String ss = "{\"src\":\"zh-CN\",\"des\":\"en-US\",\"rec\":\"今\",\"tra\":\"Today's weather is fine.\"}";
                mTranProtocalAnalysis.writeToDevice(ss);
            }
        });

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSppManager.stopDiscovery();
                BluetoothDevice currentDevice = mDevices.get(position);
//                if (mSppManager.createBond(currentDevice)) {
//                    Log.e(TAG, "current device is bond");
//                mSppManager.connect(currentDevice);
                Log.e(TAG, "is main thread : " + (Looper.myLooper() == Looper.getMainLooper()));

//
                mSppManager.connect(currentDevice);


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
//        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        foundFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//        registerReceiver(mReceiver, foundFilter);
//
//        IntentFilter finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        registerReceiver(mReceiver, finishFilter);
    }


    private void unregisterBluetoothReceiver() {
//        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDevicePressedStateListener(int type) {
        Log.e("device button : ", "" + type);
    }

    @Override
    public void onDeviceReceiverTextMessageListener(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mConversationArrayAdapter.add("other : " + msg);

            }
        });
    }

    @Override
    public void writeByteToOtherDeviceSuccess() {
        Snackbar.make(btn_bonded, "发送成功", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void writeByteToOtherDeviceFailed() {
        Snackbar.make(btn_bonded, "发送失败", Snackbar.LENGTH_SHORT).show();
    }


//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//
//        }
//    };
}
