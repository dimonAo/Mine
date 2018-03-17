package com.example.administrator.myapplication.column;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.example.administrator.myapplication.R;

import java.util.List;

/**
 * Created by Administrator on 2018/3/16 0016.
 */

public class SleepScrollPicker extends ScrollPickerView<SleepStatisticEntity> {
    private int mMeasureWidth;
    private int mMeasureHeight;

    private TextPaint mPaint; //
    private int mMinTextSize = 24; // 最小的字体
    private int mMaxTextSize = 32; // 最大的字体
    // 字体渐变颜色
    private int mStartColor = Color.BLUE; // 中间选中ｉｔｅｍ的颜色
    private int mEndColor = Color.GRAY; // 上下两边的颜色

    private int mMaxLineWidth = -1; // 最大的行宽,默认为itemWidth.超过后文字自动换行
    private Layout.Alignment mAlignment = Layout.Alignment.ALIGN_CENTER; // 对齐方式,默认居中

    private int mUnselectedDeepPurple, mUnselectedLightPurple;
    private int mSelectedDeepPurple, mSelectedLightPurple;


    public SleepScrollPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepScrollPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);

        init(attrs);
        mSelectedDeepPurple = ContextCompat.getColor(context, R.color.color_deep_pur);
        mSelectedLightPurple = ContextCompat.getColor(context, R.color.color_light_pur);
        mUnselectedDeepPurple = ContextCompat.getColor(context, R.color.color_deep_pur_5);
        mUnselectedLightPurple = ContextCompat.getColor(context, R.color.color_light_pur_5);


    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                    R.styleable.StringScrollPicker);
            mMinTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.StringScrollPicker_spv_min_text_size, mMinTextSize);
            mMaxTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.StringScrollPicker_spv_max_text_size, mMaxTextSize);
            mStartColor = typedArray.getColor(
                    R.styleable.StringScrollPicker_spv_start_color, mStartColor);
            mEndColor = typedArray.getColor(
                    R.styleable.StringScrollPicker_spv_end_color, mEndColor);
            mMaxLineWidth = typedArray.getDimensionPixelSize(R.styleable.StringScrollPicker_spv_max_line_width, mMaxLineWidth);
            int align = typedArray.getInt(R.styleable.StringScrollPicker_spv_alignment, 1);
            if (align == 2) {
                mAlignment = Layout.Alignment.ALIGN_NORMAL;
            } else if (align == 3) {
                mAlignment = Layout.Alignment.ALIGN_OPPOSITE;
            } else {
                mAlignment = Layout.Alignment.ALIGN_CENTER;
            }
            typedArray.recycle();
        }
    }

    @Override
    public void drawItem(Canvas canvas, List<SleepStatisticEntity> data, int position, int relative, float moveLength, float top) {
        drawText(canvas, data, position, relative, moveLength, top);


    }

    float textSize = 0;

    /*绘制X轴Label*/
    private void drawText(Canvas canvas, List<SleepStatisticEntity> data, int position, int relative, float moveLength, float top) {
        CharSequence text = data.get(position).getmPeriod();
        int itemSize = getItemSize();
        textSize = mMinTextSize;
        mPaint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(text, 0, text.length(), mPaint, mMaxLineWidth, mAlignment, 1.0F, 0.0F, true, null, 0);
        float x = 0;
        float y = 0;
        float lineWidth = mPaint.measureText(text.toString());

        if (isHorizontal()) { // 水平滚动
            x = top + (getItemWidth() - lineWidth) / 2;
            y = getHeight() - textSize;
        } else { // 垂直滚动
            x = (getItemWidth() - lineWidth) / 2;
            y = top + (getItemHeight() + textSize) / 2;
        }
        // 计算渐变颜色
        computeColumnColor(mSelectedDeepPurple, ContextCompat.getColor(getContext(), R.color.color_black), relative, itemSize, moveLength, mPaint);
        canvas.drawText(text.toString(), x, y, mPaint);


        drawColumn(canvas, data, position, relative, moveLength, top);
    }


    private void drawColumn(Canvas canvas, List<SleepStatisticEntity> data, int position, int relative, float moveLength, float top) {
        int targetNum = data.get(position).getmDeepSleepTime() + data.get(position).getmLightSleepTime();
        int itemSize = getItemSize();
        Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.parseColor("#ffffff"));
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(dip2px(1));
        canvas.drawLine(0, getHeight() - 3 * textSize, getWidth(), getHeight() - 3 * textSize, mLinePaint);

        Paint mColumnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColumnPaint.setStyle(Paint.Style.FILL);

        Paint mSubColumnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSubColumnPaint.setStyle(Paint.Style.FILL);

        computeColumnColor(mUnselectedLightPurple, mUnselectedDeepPurple, relative, itemSize, moveLength, mColumnPaint);
        computeColumnColor(mSelectedDeepPurple, mSelectedLightPurple, relative, itemSize, moveLength, mSubColumnPaint);
        if (isHorizontal()) {
            canvas.drawRoundRect(top + (0.2f) * getItemWidth(), getHeight() - ((getHeight() - 3 * textSize) * (targetNum / 12f)) - 3 * textSize, top + (0.8f) * getItemWidth(), getHeight() - 3 * textSize, 10, 10, mColumnPaint);
            canvas.drawRect(top + (0.2f) * getItemWidth(), getHeight() - ((getHeight() - 3 * textSize) * (data.get(position).getmDeepSleepTime() / 12f)) - 3 * textSize, top + (0.8f) * getItemWidth(), getHeight() - 3 * textSize, mSubColumnPaint);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        if (mMaxLineWidth < 0) {
            mMaxLineWidth = getItemWidth();
        }
    }

    /**
     * 计算字体颜色，渐变
     *
     * @param relative 　相对中间item的位置
     */
    private void computeColumnColor(int startcolor, int endcolor, int relative, int itemSize, float moveLength, Paint mPaint) {

        int color = endcolor; // 　其他默认为ｍEndColor

        if (relative == -1 || relative == 1) { // 上一个或下一个
            // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为mEndColor
            if ((relative == -1 && moveLength < 0)
                    || (relative == 1 && moveLength > 0)) {
                color = endcolor;
            } else { // 计算渐变的颜色
                float rate = (itemSize - Math.abs(moveLength))
                        / itemSize;
                color = computeGradientColor(startcolor, endcolor, rate);
            }
        } else if (relative == 0) { // 中间item
            float rate = Math.abs(moveLength) / itemSize;
            color = computeGradientColor(startcolor, endcolor, rate);
        }

        mPaint.setColor(color);
    }

}
