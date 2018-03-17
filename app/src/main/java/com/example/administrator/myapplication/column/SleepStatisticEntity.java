package com.example.administrator.myapplication.column;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/3/17 0017.
 */

public class SleepStatisticEntity implements Parcelable{


    private String mPeriod;
    private int mDeepSleepTime;
    private int mLightSleepTime;

    public SleepStatisticEntity(){

    }

    protected SleepStatisticEntity(Parcel in) {
        mPeriod = in.readString();
        mDeepSleepTime = in.readInt();
        mLightSleepTime = in.readInt();
    }

    public static final Creator<SleepStatisticEntity> CREATOR = new Creator<SleepStatisticEntity>() {
        @Override
        public SleepStatisticEntity createFromParcel(Parcel in) {
            return new SleepStatisticEntity(in);
        }

        @Override
        public SleepStatisticEntity[] newArray(int size) {
            return new SleepStatisticEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPeriod);
        dest.writeInt(mDeepSleepTime);
        dest.writeInt(mLightSleepTime);
    }

    public String getmPeriod() {
        return mPeriod;
    }

    public void setmPeriod(String mPeriod) {
        this.mPeriod = mPeriod;
    }

    public int getmDeepSleepTime() {
        return mDeepSleepTime;
    }

    public void setmDeepSleepTime(int mDeepSleepTime) {
        this.mDeepSleepTime = mDeepSleepTime;
    }

    public int getmLightSleepTime() {
        return mLightSleepTime;
    }

    public void setmLightSleepTime(int mLightSleepTime) {
        this.mLightSleepTime = mLightSleepTime;
    }

    @Override
    public String toString() {
        return "SleepStatisticEntity{" +
                "mPeriod='" + mPeriod + '\'' +
                ", mDeepSleepTime=" + mDeepSleepTime +
                ", mLightSleepTime=" + mLightSleepTime +
                '}';
    }
}
