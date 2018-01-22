package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleConnectActivity extends AppCompatActivity {
    private static final String TAG = "BleConnectActivity";
    private String connectAddress;
    private Button connect;
    private BleBluetoothManager mBleBluetoothManager;
    private ExpandableListView gatt_services_list;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        mBleBluetoothManager = BleBluetoothManager.getInstance(this);

        connectAddress = getIntent().getStringExtra("device_address");
        Log.e(TAG, "connect address : " + connectAddress);
        connect = findViewById(R.id.connect);
        gatt_services_list = findViewById(R.id.gatt_services_list);
        gatt_services_list.setOnChildClickListener(servicesListClickListener);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleBluetoothManager.connectBle(connectAddress, mCallback);
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
                                mBleBluetoothManager.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBleBluetoothManager.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBleBluetoothManager.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
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
//            Log.e(TAG, "service uuid : " + uuid);
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
//                Log.e(TAG, "characteristic uuid : " + uuid);
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


    private IBleConnectCallback mCallback = new IBleConnectCallback() {
        @Override
        public void onStateChangeListener(int state) {

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

        @Override
        public void onCharacteristicDiscovery(BluetoothGatt mGatt) {
            displayGattServices(mGatt.getServices());
        }
    };
}
