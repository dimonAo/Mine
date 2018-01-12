package com.example.administrator.myapplication;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public interface BluetoothManagerInte {

    /**
     * 手机是否支持蓝牙
     * @return
     */
    boolean supportBluetooth();

    /**
     * 手机是否开启蓝牙
     * @return
     */
    boolean isOpenBluetooth();

    /**
     * 开始扫描
     * @return
     */
    void startDiscovery();

    /**
     * 取消扫描
     */
    void cancelDiscovery();

    /**
     * 是否已经配对过
     * @return
     */
    boolean paired();

    /**
     * 配对
     * @param device
     */
    void paired(BluetoothDevice device);

    /**
     * 是否已经连接
     * @return
     */
    boolean isConnected();

    /**
     * 连接设备
     * @param device
     */
    void connectedDevice(BluetoothDevice device);

    /**
     * 断开连接
     * @param device
     */
    void disconnectedDevice(BluetoothDevice device);

}
