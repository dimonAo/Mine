package com.example.administrator.myapplication.headset;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.spp.SppManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HeadSetActivity extends AppCompatActivity {
    private static final String TAG = "HeadSetActivity";
    private static final String CURRENT_DEVICE_ADDRESS = "00:00:00:FE:E4:DA";

    private Button open, stop, sco;
    private SppManager mSppManager;
    private BluetoothAdapter mAdapter;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothA2dp mA2Dp;

    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    private static String mFileName = "";
    private AudioManager mAudioManager;
    private boolean openMedia = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_set);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/btrecorder.3gp";
        mSppManager = SppManager.getInstance(this);
        mAdapter = mSppManager.mSppAdapter;

        mPlayer = new MediaPlayer();
        mRecorder = new MediaRecorder();

        open = findViewById(R.id.open);
        stop = findViewById(R.id.stop);
        sco = findViewById(R.id.sco);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        if (mSppManager.mBluetoothHeadset == null) {
//            mSppManager.deviceSetHeadsetProfile();
//            this.mBluetoothHeadset = mSppManager.mBluetoothHeadset;
//        }

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openMedia) {
                    startRecording();
                } else {
                    stopRecording();
                }

            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (getCurrentBluetoothDevice() != null) {
//                    if (BluetoothProfile.STATE_CONNECTED == mSppManager.deviceIsSCO(getCurrentBluetoothDevice())) {
//                        mSppManager.deviceStopSCO();
//                    }
//                }
                startPlaying();


            }
        });

        sco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (getCurrentBluetoothDevice() != null) {
//                    Log.e(TAG, "is sco connected : " + mSppManager.deviceIsSCOConnected(getCurrentBluetoothDevice()));
//                }
//                mSppManager.audioManagerState();
            }
        });


    }

    private void startRecording() {
        //获得文件保存路径。记得添加android.permission.WRITE_EXTERNAL_STORAGE权限
        openMedia = false;


        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();//如果文件打开失败，此步将会出错。
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "系统不支持蓝牙录音");
            return;
        }
        mAudioManager.stopBluetoothSco();
//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
        mAudioManager.startBluetoothSco();
        //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
        //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先stopBluetoothSco()
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.e(TAG, "state : " + state);
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    mRecorder.start();//开始录音
                    unregisterReceiver(this);  //别遗漏
                } else {//等待一秒后再尝试启动SCO
                    Log.e(TAG, "start sco fail");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mAudioManager.startBluetoothSco();
                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    /**
     * 停止录音
     */

    private void stopRecording() {
        openMedia = true;
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                mRecorder = null;
                mRecorder = new MediaRecorder();
//                mRecorder.stop();
            }
            mRecorder.release();
            mRecorder = null;
        }
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
    }


    /**
     * 开始播放
     */
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            if (!mAudioManager.isBluetoothA2dpOn())
                mAudioManager.setBluetoothA2dpOn(true); //如果A2DP没建立，则建立A2DP连接
            mAudioManager.stopBluetoothSco();//如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
            //让声音路由到蓝牙A2DP。此方法虽已弃用，但就它比较直接、好用。
            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_BLUETOOTH_A2DP, AudioManager.ROUTE_BLUETOOTH);
//            if (!mAudioManager.isBluetoothScoOn()) {
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//            mAudioManager.startBluetoothSco();
//            mAudioManager.setBluetoothScoOn(true);
//            mAudioManager.setSpeakerphoneOn(false);
//            }
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /**
     * 停止播放
     */
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
    }


    private boolean isConnectedHeadset(BluetoothDevice mDevice) {
        Log.e(TAG, "mBluetoothHeadset : " + this.mBluetoothHeadset);
        return mBluetoothHeadset.isAudioConnected(mDevice);
    }


    private BluetoothDevice getCurrentBluetoothDevice() {
        Set<BluetoothDevice> mDevices = mSppManager.getBondDevices();
        List<BluetoothDevice> list = new ArrayList<>();

        list.addAll(mDevices);
        for (int i = 0; i < list.size(); i++) {
            if (CURRENT_DEVICE_ADDRESS.equals(list.get(i).getAddress())) {
                return list.get(i);
            }
        }
        return null;
    }


}
