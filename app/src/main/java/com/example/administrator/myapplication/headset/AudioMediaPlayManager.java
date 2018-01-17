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
    private AudioStateChange mAudioStateChange;

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

    public void setAudioStateChange(AudioStateChange mAudioStateChange) {
        this.mAudioStateChange = mAudioStateChange;
    }

    public AudioStateChange getAudioStateChange() {
        return mAudioStateChange;
    }


    /**
     * 判断是否连接蓝牙耳机
     */
    private boolean bluetoothEarCliented() {
        return mAudioManager.isBluetoothScoOn() || mAudioManager.isBluetoothA2dpOn();
    }

    private boolean notClientedBluetoothEar() {
        if (!bluetoothEarCliented()) {
            // TODO: 2018/1/16 0016 未连接蓝牙耳机提示


            return true;
        }
        return false;
    }

    /**
     * 使用蓝牙耳机录音,3gp格式
     *
     * @param mFilePath 录音文件存储位置
     */
    public void startRecorderUseBluetoothEar(String mFilePath) {
        if (notClientedBluetoothEar()) {
            return;
        }

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
                    mAudioStateChange.onStartRecoderUseBluetoothEar();
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
        mAudioStateChange.onStopRecoderUseBluetoothEar();
        releaseRecorder();
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
    }

    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                //e.printStackTrace();
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
//                mRecorder.stop();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void releaseMediaPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
        }
    }

    /**
     * 使用蓝牙耳机播放
     *
     * @param mFilePath 录音文件存储位置
     */
    public void startPlayingUseBluetoothEar(String mFilePath) {
        if (notClientedBluetoothEar()) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        try {

            if (bluetoothEarCliented()) {
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }

//            if (mAudioManager.isBluetoothA2dpOn())
//                mAudioManager.setBluetoothA2dpOn(false); //如果A2DP没建立，则建立A2DP连接
            mAudioManager.stopBluetoothSco();//如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
            mAudioManager.startBluetoothSco();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);

//            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_ALL, AudioManager.ROUTE_SPEAKER);
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setBluetoothScoOn(true);


//            if (!mAudioManager.isBluetoothA2dpOn())
//                mAudioManager.setBluetoothA2dpOn(true); //如果A2DP没建立，则建立A2DP连接
//            mAudioManager.stopBluetoothSco();//如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
//            //让声音路由到蓝牙A2DP。此方法虽已弃用，但就它比较直接、好用。
//            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_BLUETOOTH_A2DP, AudioManager.ROUTE_BLUETOOTH);
            mMediaPlayer.setDataSource(mFilePath);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    /**播放完成*/
                    releaseMediaPlay();
                    mAudioStateChange.onPlayCompletion();
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    /**播放错误**/
                    releaseMediaPlay();
                    mAudioStateChange.onPlayError();
                    return true;
                }
            });

            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mAudioStateChange.onStartPlayUseBluetoothEar();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }


    /**
     * 停止蓝牙耳机播放
     */
    public void stopPlayingUseBluetoothEar() {

        releaseMediaPlay();
        mAudioStateChange.onStopPlayuseBluetoothEar();
    }

    /**
     * 使用手机录音
     */
    public void startRecorderUsePhone(String mFilePath) {
        this.mFilePath = mFilePath;
        //创建MediaRecorder对象
        mMediaRecorder = new MediaRecorder();
        try {

            //配置mMediaRecorder相应参数
            //从麦克风采集声音数据
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置保存文件格式为MP4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置声音数据编码格式,音频通用格式是AAC
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置编码频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置录音保存的文件
            mMediaRecorder.setOutputFile(mFilePath);
            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mAudioStateChange.onStartRecoderUsePhone();
            //记录开始录音时间
//            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
//            recordFail();
        }
    }

    /**
     * 停止手机录音
     */
    public void stopRecorderUsePhone() {
        releaseRecorder();
        mAudioStateChange.onStopRecoderUsePhone();
    }


    /**
     * 使用手机播放
     **/
    public void startPlayingUsePhone(String mFilePath) {
        try {
            //初始化播放器
            mMediaPlayer = new MediaPlayer();

            if (bluetoothEarCliented()) {
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }


//            if (mAudioManager.isBluetoothA2dpOn())
//                mAudioManager.setBluetoothA2dpOn(false); //如果A2DP没建立，则建立A2DP连接
            mAudioManager.stopBluetoothSco();//如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);

//            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_ALL, AudioManager.ROUTE_SPEAKER);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.setSpeakerphoneOn(true);

            //设置播放音频数据文件
            mMediaPlayer.setDataSource(mFilePath);


            //设置播放监听事件
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放完成
//                    playEndOrFail(true);
                    releaseMediaPlay();
                    mAudioStateChange.onPlayCompletion();
                }
            });
            //播放发生错误监听事件
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                    playEndOrFail(false);
                    releaseMediaPlay();
                    mAudioStateChange.onPlayError();

                    return true;
                }
            });
            //播放器音量配置
//            mMediaPlayer.setVolume(1, 1);
            //是否循环播放
            mMediaPlayer.setLooping(false);

            //准备及播放
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mAudioStateChange.onStartPlayUsePhone();
        } catch (IOException e) {
            e.printStackTrace();
            //播放失败正理
//            playEndOrFail(false);
        }

    }

    /**
     * 停止手机播放
     */
    public void stopPlayingUsePhone() {
        releaseMediaPlay();
        mAudioStateChange.onStopPlayUsePhone();
    }
}
