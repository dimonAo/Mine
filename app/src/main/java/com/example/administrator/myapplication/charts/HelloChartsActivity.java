package com.example.administrator.myapplication.charts;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.administrator.myapplication.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class HelloChartsActivity extends AppCompatActivity {
    private ColumnChartView column_chart;
    private BarChart bar_chart;

    private int mColumnNum = 10;
    private int mCount;
    int maxNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_charts);
        column_chart = (ColumnChartView) findViewById(R.id.column_chart);
        bar_chart = (BarChart) findViewById(R.id.bar_chart);
        initMPChart(bar_chart);

        column_chart.setZoomEnabled(false);//手势缩放
        column_chart.setInteractive(true);//设置图表是可以交互的（拖拽，缩放等效果的前提）
        column_chart.setZoomType(ZoomType.HORIZONTAL);
        column_chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
//        column_chart.setMaxZoom((float) 4);
        findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHelloChartColumnChartData();
                mCount = 0;
            }
        });
        getHelloChartColumnChartData();

//        getMPChartColumnData();
    }

    private void initMPChart(BarChart mBarchart) {
        mBarchart.getDescription().setEnabled(false);//表格描述
        mBarchart.setDrawBarShadow(false);//柱子阴影
        mBarchart.setPinchZoom(false);//是否可以缩放
        mBarchart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        Legend mLegend = mBarchart.getLegend();
        mLegend.setEnabled(false);//设置图例说明是否显示

        XAxis mXAxis = mBarchart.getXAxis();//获取X轴实例
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //设置X轴位置  当前设置的在bottom
        mXAxis.setDrawGridLines(false); //设置是否允许绘制网格


        YAxis mYLeftAxis = mBarchart.getAxisLeft();//获取左侧Y轴实例
        mYLeftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        mYLeftAxis.setAxisMinimum(0F);
        mYLeftAxis.setLabelCount(5, true);


        YAxis mYRightAxis = mBarchart.getAxisRight();//获取右侧Y轴实例
        mYRightAxis.setEnabled(false);
//        mYRightAxis.setAxisLineColor(Color.TRANSPARENT);//设置Y轴颜色
//        mYRightAxis.setTextColor(Color.TRANSPARENT);//设置Y轴Label字体颜色


        getMpChartColumnData(mBarchart);
    }


    private void getMpChartColumnData(BarChart mBarChart) {

        int maxX = 10; // 初始化图表显示的最大数量
        List<BarEntry> mBarEntrys = new ArrayList<>(); // 定义单个数据集合
        for (int i = 0; i < maxX; i++) {
            mBarEntrys.add(new BarEntry(i, (float) (Math.random() * 1000F + 5))); //添加数据

        }

        BarDataSet mBarDataSet = new BarDataSet(mBarEntrys, "column1");
        mBarDataSet.setColor(ContextCompat.getColor(this, R.color.colorAccent));//设置柱状图填充颜色
        mBarDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimary));//设置点击柱子高亮颜色

        BarData mBarData = new BarData(mBarDataSet);
        mBarData.setBarWidth(0.3f); //设置柱子宽度
        mBarChart.setData(mBarData);//设置数据到图表


    }


    private void getMPChartColumnData() {


        int maxX = 10;
        List<BarEntry> mBarEntrys = new ArrayList<>();
        for (int i = 0; i < maxX; i++) {
            BarEntry mBarEntry = new BarEntry(i + 5, i);
            mBarEntrys.add(mBarEntry);
        }

        BarDataSet mBarDataSet = new BarDataSet(mBarEntrys, null);
        mBarDataSet.setColor(Color.BLUE);
        mBarDataSet.setDrawValues(true);


        List<IBarDataSet> mIBarDataSets = new ArrayList<>();
        mIBarDataSets.add(mBarDataSet);
        BarData barData = new BarData(mIBarDataSets);
        bar_chart.setData(barData);


    }


    private void getHelloChartColumnChartData() {
        ColumnChartData mColumnChartData;
        int mSubColumnNum = 1;
        List<Column> mColumns = new ArrayList<>();
        List<SubcolumnValue> mValues = null;


        for (int i = 0; i < mColumnNum; i++) {
            mValues = new ArrayList<>();
            for (int i1 = 0; i1 < mSubColumnNum; i1++) {
                SubcolumnValue mValue = new SubcolumnValue((float) (Math.random() * 1000F + 5), ChartUtils.pickColor());
                if (mValue.getValue() > maxNum) {
                    maxNum = (int) mValue.getValue();
                }
                mValues.add(mValue);
            }
            Column mColumn = new Column(mValues); //一个柱状图中显示几个柱子，取决于每个Column中没mValues集合中的数据
            mColumn.setHasLabels(true);
            mColumn.setHasLabelsOnlyForSelected(true);
            mColumns.add(mColumn);
        }
        mColumnChartData = new ColumnChartData(mColumns);
        mColumnChartData.setFillRatio(0.8f); // 设置柱子宽度
        List<AxisValue> mAxisXValues = new ArrayList<>();
        List<AxisValue> mAxisYValues = new ArrayList<>();
        for (int i = 0; i < mColumnNum; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel((i + 1) + ""));
        }


        /**按照值的最高数值设置Y轴坐标*/
//        Log.e("TAG", "maxNum : " + maxNum);
        for (int i = 0; i < (maxNum * 3 / 2); i += 200) {
            mAxisYValues.add(new AxisValue(i).setLabel((i) + ""));
        }

        Axis mXBottom = new Axis(mAxisXValues);
        Axis mYLeft = new Axis(mAxisYValues);
        mYLeft.setHasLines(true);
//        mYLeft.hasLines();
//        mYLeft.setAutoGenerated(true);


        mXBottom.setName("月份");
        mYLeft.setName("步数");

        mColumnChartData.setAxisXBottom(mXBottom);
        mColumnChartData.setAxisYLeft(mYLeft);
        column_chart.setColumnChartData(mColumnChartData);

        final float firstXValue = mAxisXValues.get(0).getValue();
        Log.e("TAG", "first x value : " + firstXValue);
        Viewport v = new Viewport(column_chart.getMaximumViewport());
        v.top = (maxNum * 3 / 2);
        column_chart.setMaximumViewport(v);
        v.left = mColumnNum - 5;
        v.right = mColumnNum;
        column_chart.setCurrentViewport(v);


        column_chart.setViewportChangeListener(new ViewportChangeListener() {
            @Override
            public void onViewportChanged(Viewport viewport) {
                if (viewport.left <= (0.0) && mCount == 0) {
                    mCount++;
                    Log.e("TAG", "left ");
                    Toast.makeText(HelloChartsActivity.this, "left ", Toast.LENGTH_SHORT).show();
                    loadData(column_chart, 10);
                }
            }
        });

    }


    private void loadData(ColumnChartView mChart, int columnCount) {

        if (null == mChart) {
            return;
        }

//        mCount = 0;
        ColumnChartData mColumnChartData;
        int mSubColumnNum = 1;
        List<Column> mColumns = mChart.getColumnChartData().getColumns();
        List<SubcolumnValue> mValues = null;


        for (int i = 0; i < columnCount; i++) {
            mValues = new ArrayList<>();
            for (int i1 = 0; i1 < mSubColumnNum; i1++) {
                SubcolumnValue mValue = new SubcolumnValue((float) (Math.random() * 1000F + 5), ChartUtils.pickColor());
                if (mValue.getValue() > maxNum) {
                    maxNum = (int) mValue.getValue();
                }
                mValues.add(mValue);
            }
            Column mColumn = new Column(mValues); //一个柱状图中显示几个柱子，取决于每个Column中没mValues集合中的数据
            mColumn.setHasLabels(true);
            mColumn.setHasLabelsOnlyForSelected(true);
            mColumns.add(i, mColumn);
        }
        mColumnChartData = new ColumnChartData(mColumns);
        mColumnChartData.setFillRatio(0.8f); // 设置柱子宽度
        List<AxisValue> mAxisXValues = new ArrayList<>();
        List<AxisValue> mAxisYValues = new ArrayList<>();
        mColumnNum += columnCount;
        for (int i = 0; i < mColumnNum; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel((i + 1) + ""));
        }


        /**按照值的最高数值设置Y轴坐标*/
        for (int i = 0; i < (maxNum * 3 / 2); i += 200) {
            mAxisYValues.add(new AxisValue(i).setLabel((i) + ""));
        }

        Axis mXBottom = new Axis(mAxisXValues);
//        mXBottom.setMaxLabelChars(5);
        mXBottom.setAutoGenerated(false);
        Axis mYLeft = new Axis(mAxisYValues);
        mYLeft.setHasLines(true);
//        mYLeft.hasLines();
//        mYLeft.setAutoGenerated(true);


//        mXBottom.setName("月份").setAutoGenerated(false);
        mXBottom.setName("月份");
        mYLeft.setName("步数");

        mColumnChartData.setAxisXBottom(mXBottom);
        mColumnChartData.setAxisYLeft(mYLeft);
        mChart.setViewportCalculationEnabled(true);
        mChart.setColumnChartData(mColumnChartData);

//        final float firstXValue = mAxisXValues.get(0).getValue();
//        Log.e("TAG", "first x value : " + firstXValue);
        Log.e("TAG", "column count : " + columnCount);
//        Viewport v = new Viewport(0, column_chart.getMaximumViewport().top, 5, 0);
        Viewport v = new Viewport(mChart.getMaximumViewport());
        v.top = (maxNum * 3 / 2f);
        v.bottom = 0.0f;
//        v.right = columnCount+5;
//        v.set(columnCount, (maxNum * 3 / 2f), (columnCount + 5), 0.0f);
        mChart.setMaximumViewport(v);
//        v.set((float) columnCount + 0, (maxNum * 3 / 2f) + 0, (columnCount + 5), 0.0f);
        v.left = columnCount;
        v.right = columnCount + 5;
        mChart.setCurrentViewport(v);
//        Viewport v1 = new Viewport(0, mChart.getMaximumViewport().height(), (mColumnNum > 5 ? 5 : mColumnNum), 0);
////        v.left = 5f;
////        v.right = 10f;
////        v.offsetTo(columnCount, v.top);
//        mChart.setCurrentViewport(v1);

        mChart.setViewportChangeListener(new ViewportChangeListener() {
            @Override
            public void onViewportChanged(Viewport viewport) {

                Log.e("TAG", "viewport left : " + viewport.toString() + ",[mCount : " + mCount + "]");
                if (viewport.left <= (0.0) && mCount == 0) {
                    mCount++;
                    Log.e("TAG", "load data left ");
//                    Toast.makeText(HelloChartsActivity.this, "left ", Toast.LENGTH_SHORT).show();
                    Snackbar.make(column_chart, "load data left", Snackbar.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(1);
                }
            }
        });

        mCount = 0;

    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                load(column_chart);
            }
        }
    };

    private void load(ColumnChartView mChartView) {
        loadData(mChartView, 1);
    }


}
