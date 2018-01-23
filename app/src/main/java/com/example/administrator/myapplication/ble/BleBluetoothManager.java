package com.example.administrator.myapplication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;


/**
 * Created by Administrator on 2018/1/18 0018.
 */

public class BleBluetoothManager {
    private static final String TAG = "BleBluetoothManager";

    private static BleBluetoothManager mInstance;
    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private boolean mScanning;

    private BleScanDeviceResult mBleScanDeviceResult;

    private BleBluetoothManager(Context mContext) {
        this.mContext = mContext;
        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
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
                Log.e(TAG, "connected");
                return true;
            } else {
                Log.e(TAG, "disconnected");
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
            //状态回调是在子线程中，做UI操作需要转到主线程
            Log.e(TAG, "status : " + status + "---> newState : " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onStateChangeListener(BluetoothProfile.STATE_CONNECTING);
                        }
                    });
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onStateChangeListener(BluetoothProfile.STATE_CONNECTED);

                        }
                    });
                    mBluetoothGatt.discoverServices();

                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onStateChangeListener(BluetoothProfile.STATE_DISCONNECTING);
                        }
                    });
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onStateChangeListener(BluetoothProfile.STATE_DISCONNECTED);
                        }
                    });

                    break;
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mCallback.onCharacteristicDiscovery(gatt);
            } else {
                Log.e(TAG, "onServices Discovered status : " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "characteristic read status : " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取数据成功
                Log.e(TAG, "read success ");
                Log.e(TAG, "characteristic read : " + Arrays.toString(characteristic.getValue()));

            } else {
                Log.e(TAG, "read fail ");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //写数据成功

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "characteristic changed : " + Arrays.toString(characteristic.getValue()));

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "descriptor read : " + Arrays.toString(descriptor.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {


            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "descriptor write : " + Arrays.toString(descriptor.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {


            }
        }


    };

    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);

        // TODO: 2018/1/22 0022 一些需要设备主动上报的UUID，需要设置descriptor可以进行Notification。例如:
        /**将特定的UUID设置成Notification*/
        if (enable) {
            if (UUID.fromString(HEART_RATE_MEASUREMENT).equals(characteristic.getUuid())) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(String service_uuid, String characteristic_uuid, byte[] data) {
        if (service_uuid != null && mBluetoothGatt != null) {

            BluetoothGattService mService = mBluetoothGatt.getService(UUID.fromString(service_uuid));
            if (mService != null && characteristic_uuid != null) {
                BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID.fromString(characteristic_uuid));
                if (characteristic.setValue(data))
                    if (mBluetoothGatt.writeCharacteristic(characteristic)) {
                        Log.e(TAG, "write success ");
                    }
            }
        }
    }

}
