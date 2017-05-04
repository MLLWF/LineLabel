package com.example.a20161005.custormlabel;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.xutil.ConvertUtils;

import java.util.ArrayList;

/**
 * Created by ML on 2017/3/21.
 */

public class LabelViews extends ViewGroup {


    private int xSpace = ConvertUtils.dip2px(getContext(), 10);
    private int ySpace = ConvertUtils.dip2px(getContext(), 10);
    /**
     * 是否平均分配多余的宽度
     */
    private boolean bAverageWidth;

    public LabelViews(Context context) {
        super(context);
    }

    public LabelViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.lable_view);
        xSpace = typedArray.getInt(R.styleable.lable_view_lable_view_horizontalSpace, 10);
        ySpace = typedArray.getInt(R.styleable.lable_view_lable_view_verticalSpace, 10);
        bAverageWidth = typedArray.getBoolean(R.styleable.lable_view_lable_view_average_width, false);
        typedArray.recycle();
    }

    private ArrayList<Line> lines = new ArrayList<>();
    private Line currentLine;

    /**
     * MeasureSpec是父控件提供给子控件用来
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lines.clear();
        currentLine = null;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int maxWidth = width - getPaddingLeft() - getPaddingRight();
        /**
         *  获取包含子View控件的大小
         */
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            /**
             *  测量每一个子View的宽高
             */
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            if (currentLine == null) {
                currentLine = new Line(maxWidth, xSpace, bAverageWidth);
                currentLine.addView(view);
                lines.add(currentLine);
            } else {
                if (currentLine.canAddView(view)) {
                    currentLine.addView(view);
                } else {
                    currentLine = new Line(maxWidth, xSpace, bAverageWidth);
                    currentLine.addView(view);
                    lines.add(currentLine);
                }
            }
        }
        /**
         *  算取所有行高的总和
         */
        int height = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < lines.size(); i++) {
            height += lines.get(i).height;
        }
        height += (lines.size() - 1) * ySpace;
        /**
         *  ViewGroup在计算自己尺寸的时候，必须预先知道所有子View的尺寸，当子View的宽高发生改变的时候要重新调用该方法进行计算
         */
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int j = 0; j < lines.size(); j++) {
            Line line = lines.get(j);
            line.layout(left, top);
            top += line.height;
            if (j != (lines.size() - 1)) {
                top += ySpace;
            }
        }
    }
}

/**
 * 面向对象思想：将每一行标签都看成是一个对象，因此定义一个行标签类
 */
class Line {
    /**
     * 一行所能包含的标签集合
     */
    private ArrayList<View> views = new ArrayList<>();
    /**
     * 一行的最大宽度
     */
    private int maxWidth;
    /**
     * 已经使用多少宽度
     */
    private int userWidth;
    /**
     * 行高
     */
    public int height;
    /**
     * 横向间隙
     */
    private int space;
    /**
     * 是否分配多余的宽度
     */
    private boolean isAverageWidth;

    public Line(int maxWidth, int space, boolean isAverageWidth) {
        this.maxWidth = maxWidth;
        this.space = space;
        this.isAverageWidth = isAverageWidth;
    }

    /**
     * 向行对象里添加标签
     */
    public void addView(View view) {
        /**
         *  记录使用的宽度和高度
         */
        if (views.size() == 0) {
            if (maxWidth < view.getMeasuredWidth()) {
                userWidth = maxWidth;
            } else {
                userWidth = view.getMeasuredWidth();
            }
            height = view.getMeasuredHeight();
        } else {
            userWidth += view.getMeasuredWidth() + space;
            height = height > view.getMeasuredHeight() ? height : view.getMeasuredHeight();
        }
        views.add(view);
    }

    /**
     * 根据当前行对象所剩余的宽度判断是否还能再添加标签
     */
    public boolean canAddView(View view) {
        if (views.size() == 0) {
            return true;
        }
        if (maxWidth < userWidth + view.getMeasuredWidth() + space) {
            return false;
        }
        return true;
    }

    /**
     * 对当前行所拥有的标签进行布局位置分配
     */
    public void layout(int l, int t) {
        /**
         *  算取剩余宽度平均分配到每个标签所需要的宽度
         */
        int avg = (maxWidth - userWidth) / views.size();
        /**
         *  循环配置标签所在位置
         */
        for (View view : views) {
            /**
             *  先获取到当前标签的宽高
             */
            int measureWidth = view.getMeasuredWidth();
            int measureHeight = view.getMeasuredHeight();
            /**
             *  指定需要的宽高值和模式，通过makeMeasureSpec方法获取到重新测量需要的参数
             */
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(isAverageWidth ? measureWidth + avg : measureWidth, View.MeasureSpec.EXACTLY);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measureHeight, View.MeasureSpec.EXACTLY);
            /**
             *  测量view
             */
            view.measure(widthMeasureSpec, heightMeasureSpec);
            /**
             *  获取重新侧量的宽高
             */
            measureWidth = view.getMeasuredWidth();
            int left = l;
            int top = t;
            int right = measureWidth + left;
            int bottom = height + top;
            /**
             *  配置位置参数
             */
            view.layout(left, top, right, bottom);
            l += measureWidth + space;
        }
    }
}
