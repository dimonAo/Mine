package com.example.administrator.myapplication.charts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;


/**
 * Created by Administrator on 2018/3/2 0002.
 */

public class MainPageStepColumnDisplay {
    private static final String TAG = "StepColumnDisplay";

    private Context mContext;
    private String[] whole_time;

    public MainPageStepColumnDisplay(Context mContext) {
        this.mContext = mContext;
        whole_time = mContext.getResources().getStringArray(R.array.whole_point);
    }


    private void initColumnChartView(ColumnChartView mColumnChartView) {
        mColumnChartView.setZoomEnabled(false);//手势缩放
        mColumnChartView.setInteractive(true);//设置图表是可以交互的（拖拽，缩放等效果的前提）
        mColumnChartView.setZoomType(ZoomType.HORIZONTAL);
        mColumnChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        mColumnChartView.setMaxZoom((float) 4);


    }

    public void setColumnChartViewData(ColumnChartView mColumnChartView) {

        initColumnChartView(mColumnChartView);

        ColumnChartData mColumnChartData;
        int mSubColumnNum = 1;
        List<Column> mColumns = new ArrayList<>();
        List<SubcolumnValue> mSubcolumnValues;

        float maxNum = 0;

        List<StepEntity> mSteps = new ArrayList<>();
        for (String aWhole_time : whole_time) {
            StepEntity mEn = new StepEntity();
            mEn.setWhole_time(aWhole_time);
            mEn.setStep_num((int) (Math.random() * 1000) + "");
            mSteps.add(mEn);
        }

        /**设置每个柱子的数据*/
        for (int i = 0; i < mSteps.size(); i++) {
            mSubcolumnValues = new ArrayList<>();
            float mCurrentStep = Float.parseFloat(mSteps.get(i).getStep_num());
            for (int i1 = 0; i1 < mSubColumnNum; i1++) {
                SubcolumnValue mValue = new SubcolumnValue(mCurrentStep, ContextCompat.getColor(mContext, R.color.colorAccent));

                if (mValue.getValue() > maxNum) {
                    maxNum = mValue.getValue();
                }
                mSubcolumnValues.add(mValue);
            }

            Column mColumn = new Column(mSubcolumnValues);
            mColumn.setHasLabels(true);
            mColumn.setHasLabelsOnlyForSelected(true);
            mColumns.add(mColumn);
        }


        mColumnChartData = new ColumnChartData(mColumns);
        //Y轴
        int mYLeftAxis;
        List<AxisValue> mAxisYValues = new ArrayList<>();
        if (((int) maxNum % 400) > 0) {
            mYLeftAxis = (((int) (maxNum / 400) + 2) * 400);
        } else {
            mYLeftAxis = (int) ((maxNum / 400) + 2) * 400;
        }
        Log.e(TAG, "mYLeftAxis : " + mYLeftAxis);

        int average = mYLeftAxis / 4;

        for (int i = 0; i <= mYLeftAxis; i += average) {
            Log.e(TAG, "i : " + i);
            if ((0 == (i % average)) && (i > 0)) {
                mAxisYValues.add(new AxisValue(i).setLabel((i) + ""));
            } else {
                mAxisYValues.add(new AxisValue(i).setLabel(""));
            }
        }

        Axis mYLeft = new Axis(mAxisYValues);
        mYLeft.setHasLines(true);
        mYLeft.setMaxLabelChars(5);
        mYLeft.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        mColumnChartData.setAxisYLeft(mYLeft);

        //X轴
        List<AxisValue> mAxisXValues = new ArrayList<>();
        for (int i = 0; i < mSteps.size(); i++) {
            if (0 == (i % 4)) {
                mAxisXValues.add(new AxisValue(i).setLabel(mSteps.get(i).getWhole_time()));
            } else {
                mAxisXValues.add(new AxisValue(i).setLabel(""));
            }
        }

        Axis mXBottom = new Axis(mAxisXValues);
        mXBottom.setHasLines(true);
        mXBottom.setTextSize((int) mContext.getResources().getDimension(R.dimen.text_4));
        mXBottom.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

        mColumnChartData.setAxisXBottom(mXBottom);
        mColumnChartView.setColumnChartData(mColumnChartData);

        Viewport v = new Viewport(mColumnChartView.getMaximumViewport());
        v.top = (float) mYLeftAxis;
        mColumnChartView.setMaximumViewport(v);
        mColumnChartView.setCurrentViewport(v);

    }


    private void setYAxisLeft(float num, ColumnChartData mColumnChartData) {
        int mYLeftAxis = 0;

        List<AxisValue> mAxisYValues = new ArrayList<>();

        if (((int) num % 400) > 0) {
            mYLeftAxis = (((int) (num / 400) + 1) * 400);
            Log.e(TAG, "mYLeftAxis : " + mYLeftAxis);
        }

        for (int i = 0; i <= mYLeftAxis; i += (mYLeftAxis / 4)) {
            mAxisYValues.add(new AxisValue(i).setLabel(i + ""));
        }

        Axis mYLeft = new Axis(mAxisYValues);
        mYLeft.setHasLines(false);
        mYLeft.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        mColumnChartData.setAxisYLeft(mYLeft);
    }


}
