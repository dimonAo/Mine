package com.example.administrator.myapplication.spp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Administrator on 2018/1/17 0017.
 * 解析翻译机协议类
 * 1.解析设备按键当前状态
 * 2.解析设备发送过来的文字数据
 */

public class TranProtocalAnalysis implements SppBluetoothManager.BluetoothReceiverMessageListener {
    private static final String TAG = "TranProtocalAnalysis";
    /**
     * 一包最长数据长度
     */
    private static final int PACKAGE_LENGTH = 12;
    private static TranProtocalAnalysis mAnalysis;
    private Context mContext;
    private SppBluetoothManager mSppBluetoothmanager;

    /**
     * 每次接收数据时计数
     */
    private int count;
    /**
     * 接收数据集合
     */
    private List<byte[]> receiverByte = new ArrayList<>();

    /**
     * 设备F2按下时的flag
     */
    private static final int BUTTON_F2_PRESSED = 0x01;
    /**
     * 设备F2松开时的flag
     */
    private static final int BUTTON_F2_RELEASED = 0x02;

    /**
     * 设备F3按下时的flag
     */
    private static final int BUTTON_F3_PRESSED = 0x03;
    /**
     * 设备F3松开时的flag
     */
    private static final int BUTTON_F3_RELEASED = 0x04;

    /**
     * 监听设备按键状态，发送数据是否成功
     */
    private OnDeviceButtonPressedStateListener mOnDevicePressedStateListener;

    private TranProtocalAnalysis(Context mContext) {
        this.mContext = mContext;
        mSppBluetoothmanager = SppBluetoothManager.getInstance(mContext);
        mSppBluetoothmanager.setBluetoothReceiverMessageListener(this);
    }

    /**
     * 获取TranProtocalAnalysis对象
     */
    public static TranProtocalAnalysis getTranProtocalAnalysis(Context mContext) {
        if (mAnalysis == null) {
            synchronized (TranProtocalAnalysis.class) {
                if (mAnalysis == null) {
                    mAnalysis = new TranProtocalAnalysis(mContext);
                }
            }
        }
        return mAnalysis;
    }

    /**
     * 添加设备按键状态和收发数据监听
     */
    public void setOnDevicePressedStateListener(OnDeviceButtonPressedStateListener mOnDevicePressedStateListener) {
        this.mOnDevicePressedStateListener = mOnDevicePressedStateListener;
    }

    @Override
    public void readByteFromOtherDevice(byte[] reads) {
        Log.e(TAG, "tran read ");
        analysisCMD(reads);
    }

    @Override
    public void writeByteToOtherDeviceSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnDevicePressedStateListener.writeByteToOtherDeviceSuccess();

            }
        });
    }

    @Override
    public void writeByteToOtherDeviceFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnDevicePressedStateListener.writeByteToOtherDeviceFailed();

            }
        });
    }

    /**
     * 解析设备按键状态和处理接收数据
     */
    private void analysisCMD(byte[] cmd) {
        int digit = 0;
        int checkDigit = cmd[digit++];
        int buttonDigit = cmd[digit++];
        int buttonPressDigit = cmd[digit];

        int packageNum = cmd[6];
        Log.e(TAG, "cmd : " + Arrays.toString(cmd));
        Log.e(TAG, "button digit : " + buttonDigit);
        Log.e(TAG, "button press digit : " + buttonPressDigit);

        buttonDigit = (buttonDigit & 0xFF);

        if (0x63 == checkDigit) {
            //按键指令
            Log.e(TAG, "checkDigit : " + checkDigit);

            if (buttonDigit == 0x84) {
                //F2按键
                if (pressed(buttonPressDigit)) {
                    //F2按下
                    Log.e(TAG, "F2 p : " + buttonPressDigit);
                    mHandler.removeMessages(BUTTON_F2_PRESSED);
                    mHandler.obtainMessage(BUTTON_F2_PRESSED).sendToTarget();
                } else {
                    //F2松开
                    Log.e(TAG, "F2 r : " + buttonPressDigit);
                    mHandler.removeMessages(BUTTON_F2_RELEASED);
                    mHandler.obtainMessage(BUTTON_F2_RELEASED).sendToTarget();
                }

            } else if (buttonDigit == 0x85) {
                //F3按键
                if (pressed(buttonPressDigit)) {
                    //F3按下
                    Log.e(TAG, "F3 p : " + buttonPressDigit);
                    mHandler.removeMessages(BUTTON_F3_PRESSED);
                    mHandler.obtainMessage(BUTTON_F3_PRESSED).sendToTarget();

                } else {
                    //F3松开
                    Log.e(TAG, "F3 r : " + buttonPressDigit);
                    mHandler.removeMessages(BUTTON_F3_RELEASED);
                    mHandler.obtainMessage(BUTTON_F3_RELEASED).sendToTarget();
                }
            }
        } else {
            //收取文字
            if (packageNum <= 0) {
                return;
            }

            String msg = null;
            if (packageNum > 0) {
                ++count;
                receiverByte.add(cmd);
            }
            if (count == packageNum) {
                if (receiverByte.size() == 1) {
                    byte[] resultByte = new byte[receiverByte.get(0)[3] - 2];
                    System.arraycopy(receiverByte.get(0), 8, resultByte, 0, resultByte.length);
                    msg = new String(resultByte, 0, resultByte.length);
                    Log.e(TAG, "byte 1 msg : " + msg);
                } else if (receiverByte.size() > 1) {
                    //排序
                    Collections.sort(receiverByte, new Comparator<byte[]>() {
                        @Override
                        public int compare(byte[] o1, byte[] o2) {
                            if (o1[7] > o2[7])
                                return 1;
                            return -1;
                        }
                    });

                    int listSize = receiverByte.size();

                    byte[] result = new byte[(listSize - 1) * 12 + receiverByte.get(listSize - 1)[3] - 2];
                    Log.e(TAG, "result length : " + result.length);
                    for (int i = 0; i < receiverByte.size(); i++) {
                        System.arraycopy(receiverByte.get(i), 8, result, 12 * i, receiverByte.get(i).length - 8);
                    }
                    msg = new String(result, 0, result.length);
                    Log.e(TAG, "byte 2 msg : " + msg);
                }

                count = 0;
                receiverByte.clear();
            }

            mOnDevicePressedStateListener.onDeviceReceiverTextMessageListener(msg);
        }
    }


    /**
     * 判断设备按键是否按下
     */
    private boolean pressed(int type) {
        return (0x19 == type);
    }

    /***
     * 向设备按照协议发送数据包
     * @param msg 需要发送的string数据
     */
    public void writeToDevice(String msg) {
        List<byte[]> needList = dividedAndCombineString(msg);
        for (int i = 0; i < needList.size(); i++) {
            mSppBluetoothmanager.write(needList.get(i));
        }
    }


    /**
     * 按照协议添加包头，每包20个字节
     *
     * @param datas 需要添加包头的byte数组，小于或等于12个长度
     * @return 添加包头后的byte数组数据
     */
    public byte[] addHeadData(byte[] datas) {
        if (datas == null)
            return null;
        byte[] bytes = new byte[datas.length + 6];
        bytes[0] = (byte) 0x01;
        bytes[1] = (byte) 0x00;
        bytes[2] = (byte) 0xFC;
        bytes[3] = (byte) (datas.length + 2);
        bytes[4] = (byte) 0x01;
        bytes[5] = (byte) 0x52;
        for (int i = 0; i < datas.length; i++) {
            bytes[i + 6] = datas[i];
        }
        return bytes;
    }

    /**
     * 将string类型数据转换成协议要求的数据集合，每包20个字节，需要添加规定的包头
     *
     * @param msg 需要转换的字符串数据
     * @return 每包添加包头后的byte数组集合
     */
    public List<byte[]> dividedAndCombineString(String msg) {
        if (msg == null)
            return null;
        List<byte[]> list = new ArrayList<>();
        byte[] bytes = msg.getBytes();
//        Log.e(TAG, "bytes:" + Arrays.toString(bytes));

        if (bytes.length <= PACKAGE_LENGTH) {
            byte[] temp = new byte[bytes.length + 2];
            temp[0] = 1;
            temp[1] = 0;
            for (int i = 0; i < bytes.length; i++) {
                temp[i + 2] = bytes[i];
            }

            list.add(addHeadData(temp));
//            Log.e(TAG, "temp : " + Arrays.toString(list.get(0)));
            return list;
        } else {
            int count = bytes.length / PACKAGE_LENGTH;
            int sing = bytes.length % PACKAGE_LENGTH;
            int count_fix = count;
            if (sing != 0)
                count_fix = count + 1;
            for (int i = 0; i < count; i++) {
                byte[] temp = new byte[PACKAGE_LENGTH + 2];
                temp[0] = (byte) count_fix;
                temp[1] = (byte) i;
                for (int j = 0; j < PACKAGE_LENGTH; j++) {
                    temp[j + 2] = bytes[i * PACKAGE_LENGTH + j];
//                    Log.e(TAG, "temp" + i + ":" + Integer.toHexString((byte) temp[j + 2]));
                }
                list.add(addHeadData(temp));
            }
            if (sing != 0) {
                byte[] temp = new byte[sing + 2];
                temp[0] = (byte) count_fix;
                temp[1] = (byte) (count_fix - 1);
                for (int j = 0; j < sing; j++) {
                    temp[j + 2] = bytes[count * PACKAGE_LENGTH + j];
//                    Log.e(TAG, "temp" + (count_fix - 1) + ":" + Integer.toHexString((byte) temp[j + 2]));
                }
                list.add(addHeadData(temp));
            }
            return list;
        }
    }


    @SuppressWarnings("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUTTON_F2_PRESSED:
                    mOnDevicePressedStateListener.onDevicePressedStateListener(BUTTON_F2_PRESSED);
                    break;

                case BUTTON_F2_RELEASED:
                    mOnDevicePressedStateListener.onDevicePressedStateListener(BUTTON_F2_RELEASED);
                    break;

                case BUTTON_F3_PRESSED:
                    mOnDevicePressedStateListener.onDevicePressedStateListener(BUTTON_F3_PRESSED);
                    break;
                case BUTTON_F3_RELEASED:
                    mOnDevicePressedStateListener.onDevicePressedStateListener(BUTTON_F3_RELEASED);
                    break;
            }

        }
    };


    interface OnDeviceButtonPressedStateListener {
        /**
         * 返回设备按钮操作指令
         */
        void onDevicePressedStateListener(int type);

        /**
         * 返回设备发送的文字
         */
        void onDeviceReceiverTextMessageListener(String msg);

        /**
         * 通知已发送message成功
         */
        void writeByteToOtherDeviceSuccess();

        /**
         * 通知发送message失败
         */
        void writeByteToOtherDeviceFailed();


    }


}
