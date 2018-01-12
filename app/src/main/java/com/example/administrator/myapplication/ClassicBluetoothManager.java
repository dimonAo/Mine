package com.example.administrator.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.logging.Handler;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class ClassicBluetoothManager implements BluetoothManagerInte {

    public BluetoothAdapter mBluetoothAdapter;


    public ClassicBluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    @Override
    public boolean supportBluetooth() {
        return mBluetoothAdapter != null;
    }

    @Override
    public boolean isOpenBluetooth() {
        Log.e("TAG", "mBluetoothAdapter : " + mBluetoothAdapter);

        if (null == mBluetoothAdapter || !mBluetoothAdapter.isEnabled()) {
            return true;
        }

        return false;
    }

    @Override
    public void startDiscovery() {
        if(mBluetoothAdapter != null){
            if (mBluetoothAdapter.startDiscovery()) {
                mBluetoothAdapter.cancelDiscovery();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.startDiscovery();
                    }
                }).start();
            }
        }

    }

    @Override
    public void cancelDiscovery() {

    }

    @Override
    public boolean paired() {
        return false;
    }

    @Override
    public void paired(BluetoothDevice device) {

    }

    @Override
    public boolean isConnected() {

        return false;
    }

    @Override
    public void connectedDevice(BluetoothDevice device) {

    }

    @Override
    public void disconnectedDevice(BluetoothDevice device) {


    }
}
