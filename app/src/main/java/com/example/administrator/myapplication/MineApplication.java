package com.example.administrator.myapplication;

import android.app.Application;
import android.content.IntentFilter;

import com.example.administrator.myapplication.spp.SppBluetoothManager;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class MineApplication extends Application {

public SppBluetoothManager mManager;


    @Override
    public void onCreate() {
        super.onCreate();
//        mManager = SppBluetoothManager.getInstance(getApplicationContext());
//
//       mManager.bluetoothRegisterReceiver();
    }







}
