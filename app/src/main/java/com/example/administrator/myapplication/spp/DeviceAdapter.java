package com.example.administrator.myapplication.spp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.myapplication.R;

import java.util.List;

/**
 * Created by Administrator on 2018/1/11 0011.
 */

public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mDevices;
    private Context mContext;
    private LayoutInflater mInflater;

    public DeviceAdapter(Context mContext, List<BluetoothDevice> mDevices) {
        this.mContext = mContext;
        this.mDevices = mDevices;
        mInflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_device_item, parent, false);

            mViewHolder.device_name = convertView.findViewById(R.id.device_name);
            mViewHolder.device_mac = convertView.findViewById(R.id.device_mac);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mDevices.get(position);

        mViewHolder.device_name.setText(device.getName());
        mViewHolder.device_mac.setText(device.getAddress());

        return convertView;
    }


    class ViewHolder {
        TextView device_name, device_mac;
    }


}
