package com.example.administrator.myapplication.headset;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public interface AudioStateChange {

    /**
     * 开始使用蓝牙耳机录音
     */
    void onStartRecoderUseBluetoothEar();

    /**
     * 停止使用蓝牙耳机录音
     */
    void onStopRecoderUseBluetoothEar();

    /**
     * 开始使用蓝牙耳机播放
     */
    void onStartPlayUseBluetoothEar();

    /**
     * 停止使用蓝牙耳机播放
     */
    void onStopPlayuseBluetoothEar();


    /**
     * 开始使用手机录音
     */
    void onStartRecoderUsePhone();

    /**
     * 停止使用手机录音
     */
    void onStopRecoderUsePhone();

    /**
     * 开始使用手机播放
     */
    void onStartPlayUsePhone();

    /**
     * 停止使用手机播放
     */
    void onStopPlayUsePhone();

    /**
     * 播放完成
     */
    void onPlayCompletion();

    /**
     * 播放错误
     */
    void onPlayError();


}
