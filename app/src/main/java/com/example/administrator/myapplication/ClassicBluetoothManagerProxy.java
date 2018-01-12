package com.example.administrator.myapplication;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class ClassicBluetoothManagerProxy implements BluetoothManagerInte {
    private ClassicBluetoothManager mManager;
    private static ClassicBluetoothManagerProxy mInstance;

    private ClassicBluetoothManagerProxy() {
        mManager = new ClassicBluetoothManager();
    }

    public static ClassicBluetoothManagerProxy getmInstance() {
        if (mInstance == null) {
            synchronized (ClassicBluetoothManagerProxy.class) {
                if (mInstance == null) {
                    mInstance = new ClassicBluetoothManagerProxy();
                }
            }
        }
        return mInstance;
    }


    @Override
    public boolean supportBluetooth() {
        return mManager.supportBluetooth();
    }

    @Override
    public boolean isOpenBluetooth() {
        return mManager.isOpenBluetooth();
    }

    @Override
    public void startDiscovery() {
        mManager.startDiscovery();

    }

    @Override
    public void cancelDiscovery() {
        mManager.cancelDiscovery();
    }

    @Override
    public boolean paired() {
        return mManager.paired();
    }

    @Override
    public void paired(BluetoothDevice device) {
        mManager.paired(device);
    }

    @Override
    public boolean isConnected() {
        return mManager.isConnected();
    }

    @Override
    public void connectedDevice(BluetoothDevice device) {
        mManager.connectedDevice(device);
    }

    @Override
    public void disconnectedDevice(BluetoothDevice device) {
        mManager.disconnectedDevice(device);
    }
}
