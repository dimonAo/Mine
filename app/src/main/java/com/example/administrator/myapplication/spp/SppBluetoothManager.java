package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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

import com.example.administrator.myapplication.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class SppBluetoothManager {

    private static final String TAG = "SppBluetoothManager";
    //    private static final UUID CONNECT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CONNECT_UUID = UUID.fromString("00000000-0000-0000-0099-aabbccddeeff");//00000000-0000-0000-0099-aabbccddeeff
    private static final byte[] UUID_AIROHA1520 = {0, 0, 0, 0, 0, 0, 0, 0, 0, -103, -86, -69, -52, -35, -18, -1};
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
        setState(STATE_NONE);

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
     * 获取已连接设备
     */
    public void getConnectBt() {
        int a2dp = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        if (flag != -1) {
            mBluetoothAdapter.getProfileProxy(mContext, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            Log.e(TAG, "" + device.getAddress());
                            mGetConnectedBluetoothDeviceFromSystem.onConnectedBluetoothDevice(mDevices.get(0));
                        }
                    } else {
                        mGetConnectedBluetoothDeviceFromSystem.onConnectedBluetoothDevice(null);
                    }
                }
            }, flag);
        }
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
                Log.e(TAG, "cancel discovery");
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
        Log.e(TAG, "mState : " + this.mState);
//        mBluetoothListener.notifyChangeConnectstate(this.mState);
        if (this.mState == STATE_CONNECTED) {
            mBluetoothListener.notifyChangeConnectstate(this.mState);
        }
    }

    /**
     * 获取连接状态
     */
    public synchronized int getState() {
        return this.mState;
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


        mConnectedThread = new ConnectedThread(socket, "Secure");
        mConnectedThread.start();

        setState(STATE_CONNECTED);

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
//        Log.e(TAG, "send out lenght : " + out.length);
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

    private UUID getUuidFromByteArray(byte[] paramArrayOfByte) {
        ByteBuffer localByteBuffer = ByteBuffer.wrap(paramArrayOfByte);
        return new UUID(localByteBuffer.getLong(), localByteBuffer.getLong());
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            mSocketType = "Secure";
            UUID localUUID = getUuidFromByteArray(UUID_AIROHA1520);
            // Create a new listening server socket
            try {
//                if (true) {
//                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothChatSecure",
//                        CONNECT_UUID);
//                } else {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        "BluetoothChatSecure", localUUID);
//                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + " listen() failed", e);
            }
            mmServerSocket = tmp;
            setState(STATE_LISTEN);
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + " accept() failed", e);
                    break;
                }

                // If a connection was accepted
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
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = "Secure";
            UUID localUUID = getUuidFromByteArray(UUID_AIROHA1520);
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
//                if (secure) {
//                tmp = device.createRfcommSocketToServiceRecord(
//                        CONNECT_UUID);
//                } else {
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                        localUUID);
//                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
//            mState = STATE_CONNECTING;
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
//            mAdapter.cancelDiscovery();
            stopDiscovery();
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
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
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

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
//            mState = STATE_CONNECTED;

        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    Log.e(TAG, "bytes : " + bytes);
                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
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
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
                Log.e(TAG, "write success");
            } catch (IOException e) {
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

    private GetConnectedBluetoothDeviceFromSystem mGetConnectedBluetoothDeviceFromSystem;

    private void setGetConnectedBluetoothDeviceFromSystem(GetConnectedBluetoothDeviceFromSystem mGetConnectedBluetoothDeviceFromSystem) {
        this.mGetConnectedBluetoothDeviceFromSystem = mGetConnectedBluetoothDeviceFromSystem;
    }


}
