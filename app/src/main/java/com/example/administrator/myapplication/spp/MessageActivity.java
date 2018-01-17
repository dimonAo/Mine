package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.administrator.myapplication.R;

public class MessageActivity extends AppCompatActivity implements TranProtocalAnalysis.OnDeviceButtonPressedStateListener {

    private Button btn_edit;
    private EditText message;
    private Button reconnect;
    private ListView in;
    private SppBluetoothManager mSppmanager;
    private TranProtocalAnalysis mTranProtocalAnalysis;
    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mSppmanager = SppBluetoothManager.getInstance(this);
        mTranProtocalAnalysis = TranProtocalAnalysis.getTranProtocalAnalysis(this);
        mTranProtocalAnalysis.setOnDevicePressedStateListener(this);

        btn_edit = findViewById(R.id.edit);
        message = findViewById(R.id.message);
        reconnect = findViewById(R.id.reconnect);
        in = findViewById(R.id.in);
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.item_message);
        in.setAdapter(mConversationArrayAdapter);
        final String macAddress = getMacAddress();

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(macAddress)) {
                    BluetoothDevice mDevice = mSppmanager.mBluetoothAdapter.getRemoteDevice(macAddress);
                    mSppmanager.connect(mDevice);
                }
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
//                mSppmanager.write(msg.getBytes());
                mTranProtocalAnalysis.writeToDevice(msg);
                mConversationArrayAdapter.add("ME : " + msg);

            }
        });

//        mSppmanager.setBluetoothReceiverMessageListener(new SppBluetoothManager.BluetoothReceiverMessageListener() {
//            @Override
//            public void readByteFromOtherDevice(byte[] reads) {
//                final String readMessage = new String(reads, 0, reads.length);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mConversationArrayAdapter.add("other : " + readMessage);
//                    }
//                });
//            }
//        });

    }

    private String getMacAddress() {
        if (getIntent() == null) {
            return null;
        }
        return getIntent().getStringExtra("device_mac");
    }


    @Override
    public void onDevicePressedStateListener(int type) {

    }

    @Override
    public void onDeviceReceiverTextMessageListener(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConversationArrayAdapter.add("other : " + msg);

            }
        });
    }

    @Override
    public void writeByteToOtherDeviceSuccess() {
        Snackbar.make(in, "发送成功", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void writeByteToOtherDeviceFailed() {
        Snackbar.make(in, "发送失败", Snackbar.LENGTH_SHORT).show();
    }
}
