package com.example.administrator.myapplication.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Administrator on 2018/1/15 0015.
 */

public class AudioMediaPlayManager {

    private static final String TAG = "AudioMediaPlayManager";

    private Context mContext;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private String mFilePath;


    private static AudioMediaPlayManager mInstance;

    private AudioMediaPlayManager(Context mContext) {
        this.mContext = mContext;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        mMediaPlayer = new MediaPlayer();
//        mMediaRecorder = new MediaRecorder();
    }

    public static AudioMediaPlayManager getAudioMediaPlayManager(Context mContext) {
        if (mInstance == null) {
            synchronized (AudioMediaPlayManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioMediaPlayManager(mContext);
                }
            }
        }
        return mInstance;
    }

//    public void setFilePath(String mFilePath) {
//        this.mFilePath = mFilePath;
//    }

    public String getFilePath() {
        return mFilePath;
    }

    /**
     * 使用蓝牙耳机录音
     */
    public void startRecorderUseBluetoothEar(String mFilePath) {
        this.mFilePath = mFilePath;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(mFilePath);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();//打开录音文件
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
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.e(TAG, "state : " + state);
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    mMediaRecorder.start();//开始录音
                    mContext.unregisterReceiver(this);//开始录音后就可以注销掉广播，下次录音再次注册
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
     * 停止蓝牙耳机录音
     **/
    public void stopRecorderUseBluetoothEar() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
//                mRecorder.stop();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
    }


    /**
     * 使用蓝牙耳机播放
     */
    public void startPlayingUseBluetoothEar(String mFilePath) {
        mMediaPlayer = new MediaPlayer();
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
            mMediaPlayer.setDataSource(mFilePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }


    /**
     * 停止蓝牙耳机播放
     */
    public void stopPlayingUseBluetoothEar() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
        }
    }

    /**
     * 使用手机录音
     */
    public void startRecorderUsePhone() {

    }

    /**
     * 停止手机录音
     */
    public void stopRecorderUsePhone() {

    }


    /**
     * 使用手机播放
     **/
    public void startPlayingUsePhone() {

    }

    /**
     * 停止手机播放
     */
    public void stopPlayingUsePhone() {

    }
}
