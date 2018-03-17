package com.example.administrator.myapplication.charts;

/**
 * Created by Administrator on 2018/3/2 0002.
 */

public class StepEntity {

    private String whole_time;
    private String step_num;


    public String getWhole_time() {
        return whole_time;
    }

    public void setWhole_time(String whole_time) {
        this.whole_time = whole_time;
    }

    public String getStep_num() {
        return step_num;
    }

    public void setStep_num(String step_num) {
        this.step_num = step_num;
    }

    @Override
    public String toString() {
        return "StepEntity{" +
                "whole_time='" + whole_time + '\'' +
                ", step_num='" + step_num + '\'' +
                '}';
    }
}
