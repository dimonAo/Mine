package com.example.administrator.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.administrator.myapplication.camera.AppConstant;
import com.example.administrator.myapplication.camera.BitmapUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button discovery;
    private ListView list_view;
    private static final int REQUEST_ENABLE_BT = 0x01;
    private static final int BLUETOOTH_PERMISSION = 100;

    private BluetoothAdapter adapter;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        adapter = BluetoothAdapter.getDefaultAdapter();

        discovery = (Button) findViewById(R.id.discovery);
        list_view = (ListView) findViewById(R.id.bluetooth_device);

        try {
            File file = new File(getStringPicPath());
            img.setImageBitmap(BitmapUtils.getBitemapFromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set.clear();
                ClassicBluetoothManagerProxy.getmInstance().startDiscovery();
            }
        });

//        if (!supportBluetooth()) {
//            Log.e(TAG, "此设备不支持蓝牙");
//            showSnackLong("手机不支持蓝牙");
//            return;
//        }
//
//        if (!isOpenBluetooth()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_ENABLE_BT);
//        }
        checkPermission();

    }

    private String getStringPicPath() {
        if (getIntent() == null) {
            Log.e(TAG, "get intent is null");
            return null;
        }
        String tempPath = getIntent().getStringExtra(AppConstant.KEY.IMG_PATH);
        Log.e(TAG, "tempPath : " + tempPath);
        return tempPath;
    }

    private void checkPermission() {
        AndPermission.with(this)
                .requestCode(BLUETOOTH_PERMISSION)
                .permission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_PRIVILEGED)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (BLUETOOTH_PERMISSION == requestCode) {
                            Log.e(TAG, "get bluetooth permission success");
                            if (!supportBluetooth()) {
                                Log.e(TAG, "此设备不支持蓝牙");
                                showSnackLong("手机不支持蓝牙");
                                return;
                            }

                            if (!isOpenBluetooth()) {
                                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(intent, REQUEST_ENABLE_BT);
                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (BLUETOOTH_PERMISSION == requestCode) {
                            Log.e(TAG, "get bluetooth permission fail");
                            showSnackLong("get bluetooth permission fail");

                        }
                    }
                })
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(MainActivity.this, rationale).show();
                    }
                })


                .start();
    }


    private boolean supportBluetooth() {
//        Log.e(TAG, "bluetooth adapter : " + adapter);
//        return (adapter != null);
        return ClassicBluetoothManagerProxy.getmInstance().supportBluetooth();

    }


    private boolean isOpenBluetooth() {
        return ClassicBluetoothManagerProxy.getmInstance().isOpenBluetooth();
    }


    private void showSnackLong(String msg) {
        Snackbar.make(discovery, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ENABLE_BT == requestCode) {
            if (RESULT_OK == resultCode) {
                Log.e(TAG, "已启用蓝牙");
            } else {
                Log.e(TAG, "未开启蓝牙");
            }
        }
    }

    private void registerDeviceReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        if (broadcastReceiver != null) {
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private void unregisterDeviceReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    Set<BluetoothDevice> set = new HashSet<>();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "device name : " + device.getName() + " \n device address : " + device.getAddress());
                set.add(device);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        registerDeviceReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterDeviceReceiver();
        super.onDestroy();
    }


}
