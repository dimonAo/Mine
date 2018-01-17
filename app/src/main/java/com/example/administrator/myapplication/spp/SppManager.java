package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.yanzhenjie.permission.Permission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

/**
 * Created by Administrator on 2018/1/11 0011.
 */

public class SppManager {
    private static final String TAG = "SppManager";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    private static final UUID CONNECT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //00001101-0000-1000-8000-00805F9B34FB
    private int mState;
    private Context mContext;
    public BluetoothAdapter mSppAdapter;

    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static SppManager mInstance;


    public static SppManager getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (SppManager.class) {
                if (mInstance == null) {
                    mInstance = new SppManager(mContext);

                }
            }
        }
        return mInstance;
    }


    private SppManager(Context mContext) {
        this.mContext = mContext;
        mSppAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean deviceSupportBluetooth() {
        return supportBluetooth(mSppAdapter);
    }

    public boolean deviceEnableBluetooth() {
        return enableBluetooth(mSppAdapter);
    }

    public void deviceEnableBluetoothDiscovery(int duration) {
        enableBluetoothDiscoveryed(duration);
    }

    public void deviceStartDiscovery() {
        startDiscovery(mSppAdapter);
    }

    public void deviceCancelDiscovery() {
        cancelDiscovery(mSppAdapter);
    }

    public Set<BluetoothDevice> getBondDevices() {
        return getBondDevices(mSppAdapter);
    }

    public boolean createBond(BluetoothDevice mDevice) {
        return bondDevice(mDevice);
    }


    private Set<BluetoothDevice> getBondDevices(BluetoothAdapter mAdapter) {
        return mAdapter.getBondedDevices();
    }

    private void enableBluetoothDiscoveryed(int duration) {
        if (mSppAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
            mContext.startActivity(discoverableIntent);
        }
    }

    private boolean bondDevice(BluetoothDevice mDevice) {
        return mDevice.createBond();
    }


    private boolean supportBluetooth(BluetoothAdapter mAdapter) {
        return (mAdapter != null);
    }

    private boolean enableBluetooth(BluetoothAdapter mAdapter) {
        return mAdapter != null && (mAdapter.isEnabled());
    }

    private void startDiscovery(BluetoothAdapter mAdapter) {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        mAdapter.startDiscovery();
    }

    private void cancelDiscovery(BluetoothAdapter mAdapter) {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    private synchronized void setState(int state) {
        this.mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }


    private class ConnectThread extends Thread {

        private BluetoothDevice mmDevice;
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice mDevice) {
            this.mmDevice = mDevice;

        }

        public void run() {
            deviceCancelDiscovery();

            try {
                BluetoothSocket tmp = null;
                Log.e(TAG, "connect thread head");
                tmp = mmDevice.createRfcommSocketToServiceRecord(CONNECT_UUID);
                mSocket = tmp;
                Log.e(TAG, "msocket : " + mSocket);
                setState(STATE_CONNECTING);
                mSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "rim connect exception : " + e.toString());
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionLost();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (SppManager.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mSocket, mmDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mSppAdapter.listenUsingRfcommWithServiceRecord("",
                        CONNECT_UUID);


            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {

            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
//                    socket = mmServerSocket.accept(5000);
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (SppManager.this) {
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
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
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

        public ConnectedThread(BluetoothSocket socket) {
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
                    Log.e(TAG, "bytes : " + bytes);

                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
//                    for (int i = 0; i < 30; i++)
//                        Log.e(TAG, "songjian receivedata:" + Integer.toHexString((byte) buffer[i]));
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    SppManager.this.start();
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
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
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

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
//        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }

    }


    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
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

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }
        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        SppManager.this.start();
    }

    /**
     * 连接蓝牙设备
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
//        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 写数据
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

//    public void audioManagerState() {
//        Log.e(TAG, "get ringer mode : " + mAudioManager.getRingerMode());
//        Log.e(TAG, "is bluetooth sco on : " + mAudioManager.isBluetoothScoOn());
//        Log.e(TAG, "is speaker phone on : " + mAudioManager.isSpeakerphoneOn());
//        Log.e(TAG, "get mode : " + mAudioManager.getMode());
//        Log.e(TAG, "is bluetooth sco availableoffcall : " + mAudioManager.isBluetoothScoAvailableOffCall());
//        Log.e(TAG, "is bluetooth a2dp on : " + mAudioManager.isBluetoothA2dpOn());
//        Log.e(TAG, "is wired headset on : " + mAudioManager.isWiredHeadsetOn());
//
//
//    }

}
