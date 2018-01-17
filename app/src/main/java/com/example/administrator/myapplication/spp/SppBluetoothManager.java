package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.style.TtsSpan;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class SppBluetoothManager {

    private static final String TAG = "SppBluetoothManager";
    //    private static final UUID CONNECT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CONNECT_UUID = UUID.fromString("00000000-0000-0000-0099-aabbccddeeff");
    private static SppBluetoothManager mInstance;

    private Context mContext;
    private UUID mConnectUuid;

    private int mState;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptThread mAcceptThread;

    public BluetoothAdapter mBluetoothAdapter;


    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private BluetoothListener mBluetoothListener;
    private BluetoothReceiverMessageListener mMessageListener;

    private SppBluetoothManager(Context mContext) {
        this.mContext = mContext;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public static SppBluetoothManager getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (SppBluetoothManager.class) {
                if (mInstance == null) {
                    mInstance = new SppBluetoothManager(mContext);
                }
            }
        }
        return mInstance;
    }

    /**
     * 判断手机是否支持蓝牙
     **/
    public boolean supportBluetooth() {
        return (mBluetoothAdapter != null);
    }

    /**
     * 判断手机蓝牙是否打开
     */
    public boolean enableBluetooth() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
    }


    /**
     * 开始扫描蓝牙
     */
    public void startDiscovery() {
        if (enableBluetooth()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 停止扫描蓝牙
     */
    public void stopDiscovery() {
        if (enableBluetooth()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }

    /**
     * 设置发现蓝牙设备、连接状态监听
     */
    public void setBluetoothListener(BluetoothListener mBluetoothListener) {
        this.mBluetoothListener = mBluetoothListener;
    }

    /**
     * 设置蓝牙收取数据监听
     */
    public void setBluetoothReceiverMessageListener(BluetoothReceiverMessageListener mMessageListener) {
        this.mMessageListener = mMessageListener;
    }

    /**
     * 设置连接状态
     */
    private synchronized void setState(int mState) {
        this.mState = mState;
        mBluetoothListener.notifyChangeConnectstate(this.mState);
    }

    /**
     * 获取连接状态
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 设置用来连接的UUID
     */
    public void setConnectUUid(UUID mConnectUuid) {
        this.mConnectUuid = mConnectUuid;
    }

    /**
     * 连接蓝牙设备
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
//        setState(STATE_CONNECTING);
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        setState(STATE_LISTEN);

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }


        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
//        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {

        setState(STATE_NONE);
        // Start the service over to restart listening mode
        SppBluetoothManager.this.start();
    }

    /**
     * 写数据
     *
     * @param out The bytes to write
     * @see SppManager.ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        SppBluetoothManager.ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        setState(STATE_NONE);
        Log.e(TAG, "connect failed");
//        mState = STATE_NONE;


        // Start the service over to restart listening mode
        SppBluetoothManager.this.start();
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
//            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
//                if (secure) {
                tmp = device.createRfcommSocketToServiceRecord(
                        CONNECT_UUID);
//                } else {
//                    tmp = device.createInsecureRfcommSocketToServiceRecord(
//                            CONNECT_UUID);
//                }
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (SppBluetoothManager.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setState(STATE_CONNECTED);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    byte[] reads = new byte[bytes];
                    System.arraycopy(buffer, 0, reads, 0, bytes);

                    Log.e(TAG, "reads : " + Arrays.toString(reads));
                    mMessageListener.readByteFromOtherDevice(reads);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    SppBluetoothManager.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();
                mMessageListener.writeByteToOtherDeviceSuccess();
            } catch (IOException e) {
                mMessageListener.writeByteToOtherDeviceFailed();
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private class AcceptThread extends Thread {
        // The local server socket
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {

        }

        public void run() {

            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("dimon",
                        CONNECT_UUID);


            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
            setState(STATE_LISTEN);

            setName("AcceptThread");

            BluetoothSocket socket = null;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (SppBluetoothManager.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    public BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice mFoundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // TODO: 2018/1/16 0016 将device传递到界面
                mBluetoothListener.foundBluetoothDevice(mFoundDevice);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // TODO: 2018/1/16 0016 扫描结束
                mBluetoothListener.scanBluetoothFinish();
            }
        }
    };


    /**
     * 注册蓝牙发现、扫描结束广播
     */
    public void bluetoothRegisterReceiver() {
        IntentFilter mBluetoothFilter = new IntentFilter();
        mBluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mBluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mBluetoothReceiver, mBluetoothFilter);
    }

    /**
     * 注销广播
     */
    public void bluetoothUnregisterReceiver() {
        if (mBluetoothReceiver != null) {
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
    }


    interface BluetoothListener {

        /**
         * 通知蓝牙连接状态
         */
        void notifyChangeConnectstate(int mState);

        /**
         * 扫描时发现的设备
         */
        void foundBluetoothDevice(BluetoothDevice mDevice);

        /**
         * 扫描失败
         */
        void scanBluetoothFinish();

    }

    interface BluetoothReceiverMessageListener {
        /**
         * 通知已收到的message
         */
        void readByteFromOtherDevice(byte[] reads);

        /**
         * 通知已发送message成功
         */
        void writeByteToOtherDeviceSuccess();

        /**
         * 通知发送message失败
         */
        void writeByteToOtherDeviceFailed();

    }


//    interface BluetoothWriteMessageStateListener {
//        /**
//         * 通知已发送message成功
//         */
//        void writeByteToOtherDeviceSuccess();
//
//        /**
//         * 通知发送message失败
//         */
//        void writeByteToOtherDeviceFailed();
//    }

}
