package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


/**
 * Created by Administrator on 2018/1/18 0018.
 */

public class BleBluetoothManager {
    private static final String TAG = "BleBluetoothManager";

    private static BleBluetoothManager mInstance;
    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private boolean mScanning;

    private BleScanDeviceResult mBleScanDeviceResult;

    private BleBluetoothManager(Context mContext) {
        this.mContext = mContext;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mHandler = new Handler();
    }

    public static BleBluetoothManager getInstance(Context mContext) {
        if (null == mInstance) {
            synchronized (BleBluetoothManager.class) {
                if (null == mInstance) {
                    mInstance = new BleBluetoothManager(mContext);
                }
            }
        }
        return mInstance;
    }

    public boolean supportBle() {
        return mBluetoothAdapter != null;
    }

    public boolean enableBle() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
    }

    public void startScan(boolean enable, BleScanDeviceResult mBleScanDeviceResult) {
        this.mBleScanDeviceResult = mBleScanDeviceResult;
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //停止扫描
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 10000);
            //开始扫描BLE 10秒后停止
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    public void stopScan() {
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            this.startScan(false);
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //处理扫描到的设备
            Log.e(TAG, "address : " + device.getAddress() + " name : " + device.getName());
            mBleScanDeviceResult.getScanBleDevice(device);
        }
    };


    private String mBluetoothDeviceAddress;
    private IBleConnectCallback mCallback;

    public boolean connectBle(String address, IBleConnectCallback mCallback) {
        this.mCallback = mCallback;
        Log.e(TAG, "start connect : " + address);
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        if (mBluetoothDeviceAddress != null && mBluetoothDeviceAddress.equals(address) && mBluetoothGatt != null) {
            Log.e(TAG, "mBluetoothGatt connect : " + mBluetoothGatt.connect());
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            return false;
        }
        Log.e(TAG, "");
        mBluetoothGatt = mDevice.connectGatt(mContext, false, mBluetoothGattCallback);
        mBluetoothDeviceAddress = address;

        Log.e(TAG, "mBluetoothGatt : " + mBluetoothGatt);
        return true;
    }


    /**
     * BluetoothGatt实例化回调
     */
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, "status : " + status + "---> newState : " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    mCallback.onStateChangeListener(BluetoothProfile.STATE_CONNECTING);
                    break;


                case BluetoothProfile.STATE_CONNECTED:
                    mCallback.onStateChangeListener(BluetoothProfile.STATE_CONNECTED);
                    mBluetoothGatt.discoverServices();

                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    mCallback.onStateChangeListener(BluetoothProfile.STATE_DISCONNECTING);
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    mCallback.onStateChangeListener(BluetoothProfile.STATE_DISCONNECTED);
                    break;
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

    };


}
