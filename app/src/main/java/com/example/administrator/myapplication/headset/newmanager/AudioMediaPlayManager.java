package com.example.administrator.myapplication.headset.newmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.administrator.myapplication.headset.AudioStateChange;

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

    public String getFilePath() {
        return mFilePath;
    }

    /**
     * 设置播放状态监听
     */
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
            e.printStackTrace();
        }

        if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "系统不支持蓝牙录音");
            return;
        }
//        mAudioManager.stopBluetoothSco();
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
                    Log.e(TAG,"sco audio state connected");
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
            Log.e(TAG, "audio manager bluetooth sco on");
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
    }

    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void releaseMediaPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
//            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
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
            mAudioManager.stopBluetoothSco();
            mAudioManager.startBluetoothSco();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setBluetoothScoOn(true);

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
            e.printStackTrace();
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
        mMediaRecorder = new MediaRecorder();
        try {

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
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
        } catch (Exception e) {
            e.printStackTrace();
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
            mAudioManager.stopBluetoothSco();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.setSpeakerphoneOn(true);

            //设置播放音频数据文件
            mMediaPlayer.setDataSource(mFilePath);

            //设置播放监听事件
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放完成
                    releaseMediaPlay();
                    mAudioStateChange.onPlayCompletion();
                }
            });
            //播放发生错误监听事件
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    releaseMediaPlay();
                    mAudioStateChange.onPlayError();

                    return true;
                }
            });
            //是否循环播放
            mMediaPlayer.setLooping(false);
            //准备及播放
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mAudioStateChange.onStartPlayUsePhone();
        } catch (IOException e) {
            //播放失败
            e.printStackTrace();
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
