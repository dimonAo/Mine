package com.example.administrator.myapplication.blet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/24 0024.
 */

public class BletConnectActivity extends AppCompatActivity {
    private static final String TAG = "BletConnectActivity";
    private String connectAddress;
    private Button connect;
    private IBluzScanHelper mBluzScanHelper;
    private BluzManager mBluzManager;
    private ExpandableListView gatt_services_list;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);

        mBluzManager = BluzManager.getInstance(this);
        mBluzScanHelper = BluzScanHelper.getInstance(this);

        mBluzScanHelper.addOnConnectionListener(mOnConnectionListener);

        connectAddress = getIntent().getStringExtra("device_address");
        Log.e(TAG, "connect address : " + connectAddress);
        connect = findViewById(R.id.connect);
        gatt_services_list = findViewById(R.id.gatt_services_list);
        gatt_services_list.setOnChildClickListener(servicesListClickListener);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluzScanHelper.connect(connectAddress);
            }
        });

    }

    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
//                                mBluzManager.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
//                            mBleBluetoothManager.readCharacteristic(characteristic);
                            mBluzManager.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
//                            mBleBluetoothManager.setCharacteristicNotification(
//                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };


    private void getData() {
        Log.e(TAG,"get data ");
        displayGattServices(mBluzManager.getBluetoothGattService());
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        Log.e(TAG, "displayGattServices size : " + gattServices.size());
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            Log.e(TAG, "service uuid : " + uuid);
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.e(TAG, "characteristic uuid : " + uuid);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gatt_services_list.setAdapter(gattServiceAdapter);
            }
        });
    }

    private HashMap<String, String> attributes = new HashMap();

    public String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    /**
     * 连接设备回调
     */
    private IBluzScanHelper.OnConnectionListener mOnConnectionListener = new IBluzScanHelper.OnConnectionListener() {
        @Override
        public void onConnected(BluetoothDevice device) {
            Log.e(TAG, "blet connect activity connected");

        }

        @Override
        public void onDisconnected(BluetoothDevice device) {
            Log.e(TAG, "disconnected");
        }

        @Override
        public void onServiceDiscovery() {
            getData();
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
    }
}
