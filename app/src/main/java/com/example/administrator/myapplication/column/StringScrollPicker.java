package com.example.administrator.myapplication.column;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.example.administrator.myapplication.R;

import java.util.List;


/**
 * 字符串滚动选择器
 * Created by huangziwei on 16-12-6.
 */
public class StringScrollPicker extends ScrollPickerView<CharSequence> {


    private int mMeasureWidth;
    private int mMeasureHeight;

    private TextPaint mPaint; //
    private int mMinTextSize = 24; // 最小的字体
    private int mMaxTextSize = 32; // 最大的字体
    // 字体渐变颜色
    private int mStartColor = Color.BLACK; // 中间选中ｉｔｅｍ的颜色
    private int mEndColor = Color.GRAY; // 上下两边的颜色

    private int mMaxLineWidth = -1; // 最大的行宽,默认为itemWidth.超过后文字自动换行
    private Layout.Alignment mAlignment = Layout.Alignment.ALIGN_CENTER; // 对齐方式,默认居中


    public StringScrollPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StringScrollPicker(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        init(attrs);

//        setData(new ArrayList<CharSequence>(Arrays.asList(new String[]{
//                "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve"
//        })));

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

    /**
     * @param startColor 正中间的颜色
     * @param endColor   上下两边的颜色
     */
    public void setColor(int startColor, int endColor) {
        mStartColor = startColor;
        mEndColor = endColor;
        invalidate();
    }

    /**
     * item文字大小
     *
     * @param minText 沒有被选中时的最小文字
     * @param maxText 被选中时的最大文字
     */
    public void setTextSize(int minText, int maxText) {
        mMinTextSize = minText;
        mMaxTextSize = maxText;
        invalidate();
    }

    public int getStartColor() {
        return mStartColor;
    }

    public int getEndColor() {
        return mEndColor;
    }

    public int getMinTextSize() {
        return mMinTextSize;
    }

    public int getMaxTextSize() {
        return mMaxTextSize;
    }


    public int getMaxLineWidth() {
        return mMaxLineWidth;
    }

    /**
     * 最大的行宽,默认为itemWidth.超过后文字自动换行
     *
     * @param maxLineWidth
     */
    public void setMaxLineWidth(int maxLineWidth) {
        mMaxLineWidth = maxLineWidth;
    }

    /**
     * 最大的行宽,默认为itemWidth.超过后文字自动换行
     *
     * @return
     */
    public Layout.Alignment getAlignment() {
        return mAlignment;
    }

    public void setAlignment(Layout.Alignment alignment) {
        mAlignment = alignment;
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
     * 绘制item
     *
     * @param canvas
     * @param data       　数据集
     * @param position   在data数据集中的位置
     * @param relative   相对中间item的位置,relative==0表示中间item,relative<0表示上（左）边的item,relative>0表示下(右)边的item
     * @param moveLength 中间item滚动的距离，moveLength<0则表示向上（右）滚动的距离，moveLength＞0则表示向下（左）滚动的距离
     * @param top        当前绘制item的坐标,当垂直滚动时为顶部y的坐标；当水平滚动时为item最左边x的坐标
     */

    @Override
    public void drawItem(Canvas canvas, List<CharSequence> data, int position, int relative, float moveLength, float top) {
        CharSequence text = data.get(position);
        int itemSize = getItemSize();
        float textSize = 0;
        // 设置文字大小
        if (relative == -1) { // 上一个
            if (moveLength < 0) { // 向上滑动
                textSize = mMinTextSize;
//                mPaint.setTextSize(textSize);
            } else { // 向下滑动
                textSize = mMinTextSize + (mMaxTextSize - mMinTextSize) * moveLength / itemSize;
            }
            mPaint.setTextSize(textSize);
        } else if (relative == 0) { // 中间item,当前选中
            textSize = mMinTextSize + (mMaxTextSize - mMinTextSize) * (itemSize - Math.abs(moveLength)) / itemSize;
            mPaint.setTextSize(textSize);
        } else if (relative == 1) { // 下一个
            if (moveLength > 0) { // 向下滑动
                textSize = mMinTextSize;
                mPaint.setTextSize(textSize);
            } else { // 向上滑动
                textSize = mMinTextSize + (mMaxTextSize - mMinTextSize) * -moveLength / itemSize;
                mPaint.setTextSize(textSize);
            }
        } else { // 其他
            textSize = mMinTextSize;
            mPaint.setTextSize(textSize);
        }

        StaticLayout layout = new StaticLayout(text, 0, text.length(), mPaint, mMaxLineWidth, mAlignment, 1.0F, 0.0F, true, null, 0);
        float x = 0;
        float y = 0;
//        float lineWidth = layout.getWidth();
        float lineWidth = mPaint.measureText(text.toString());

        if (isHorizontal()) { // 水平滚动
            x = top + (getItemWidth() - lineWidth) / 2;
//            y = (getItemHeight() - layout.getHeight()) / 2;
//            y = (getItemHeight() + sp2px(getContext(), mMinTextSize)) / 2;
            y = (getItemHeight() + textSize) / 2;
        } else { // 垂直滚动
            x = (getItemWidth() - lineWidth) / 2;
//                        y = (getItemHeight() - layout.getHeight()) / 2;
//            y = top + (getItemHeight() + sp2px(getContext(), mMinTextSize)) / 2;
            y = top + (getItemHeight() + textSize) / 2;
        }
        // 计算渐变颜色
        computeColor(relative, itemSize, moveLength);
        canvas.drawText(text.toString(), x, y, mPaint);

//        canvas.save();
//        canvas.translate(x, y);
//        canvas.drawText(text.toString(), x, y, mPaint);
//        layout.draw(canvas);
//        canvas.restore();
    }

    /**
     * 计算字体颜色，渐变
     *
     * @param relative 　相对中间item的位置
     */
    private void computeColor(int relative, int itemSize, float moveLength) {

        int color = mEndColor; // 　其他默认为ｍEndColor

        if (relative == -1 || relative == 1) { // 上一个或下一个
            // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为mEndColor
            if ((relative == -1 && moveLength < 0)
                    || (relative == 1 && moveLength > 0)) {
                color = mEndColor;
            } else { // 计算渐变的颜色
                float rate = (itemSize - Math.abs(moveLength))
                        / itemSize;
                color = computeGradientColor(mStartColor, mEndColor, rate);
            }
        } else if (relative == 0) { // 中间item
            float rate = Math.abs(moveLength) / itemSize;
            color = computeGradientColor(mStartColor, mEndColor, rate);
        }

        mPaint.setColor(color);
    }
}
