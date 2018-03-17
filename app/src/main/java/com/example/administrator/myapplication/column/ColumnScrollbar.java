package com.example.administrator.myapplication.column;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.myapplication.R;

/**
 * Created by Administrator on 2018/3/16 0016.
 */

public class ColumnScrollbar extends View {

    /**
     * 每屏绘制柱子数量
     */
    private int mVisibleItemCount;
    /**
     * X轴标签文字大小
     */
    private float mXLabelSize;
    /**
     * Y轴标签文字大小
     */
    private float mYLabelSize;

    /**
     * Y轴标签画笔
     */
    private Paint mYLabelPaint;
    /**
     * X轴标签画笔
     */
    private Paint mXLabelPaint;
    /**
     * 柱子画笔
     */
    private Paint mColumnPaint;
    /**
     * X轴轴线画笔
     */
    private Paint mXAxisPaint;

    /**
     * Y轴轴线画笔
     */
    private Paint mYAxisPaint;

    /**
     * 画布宽高
     */
    private int mHeight, mWidth;


    public ColumnScrollbar(Context context) {
        this(context, null);
    }

    public ColumnScrollbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColumnScrollbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mYLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


        mXLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


        mColumnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mXAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mYAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHeight = getHeight();
        mWidth = getWidth();

    }


    private void drawXLabel(Canvas canvas) {
        mXLabelPaint.setTextSize(mXLabelSize);
        mXLabelPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));



    }


    public void setVisibleItemCount(int mVisibleItemCount) {
        this.mVisibleItemCount = mVisibleItemCount;
    }

    public int getVisibleItemCount() {
        return mVisibleItemCount;
    }


    public float getXLabelSize() {
        return mXLabelSize;
    }

    public void setXLabelSize(float mXLabelSize) {
        this.mXLabelSize = mXLabelSize;
    }

    public float getYLabelSize() {
        return mYLabelSize;
    }

    public void setYLabelSize(float mYLabelSize) {
        this.mYLabelSize = mYLabelSize;
    }
}
