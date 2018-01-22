package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothGatt;

/**
 * Created by Administrator on 2018/1/20 0020.
 */

public interface IBleConnectCallback {

    void onStateChangeListener(int state);


    void onCharacteristicDiscovery(BluetoothGatt mGatt);

}
