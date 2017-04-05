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
        typedArray.recycle();
    }

    private ArrayList<Line> lines = new ArrayList<>();
    private Line currentLine;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lines.clear();
        currentLine = null;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int maxWidth = width - getPaddingLeft() - getPaddingRight();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            if (currentLine == null) {
                currentLine = new Line(maxWidth, xSpace);
                currentLine.addView(view);
                lines.add(currentLine);
            } else {
                if (currentLine.canAddView(view)) {
                    currentLine.addView(view);
                } else {
                    currentLine = new Line(maxWidth, xSpace);
                    currentLine.addView(view);
                    lines.add(currentLine);
                }
            }
        }
        int height = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < lines.size(); i++) {
            height += lines.get(i).height;
        }
        height += (lines.size() - 1) * ySpace;
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

class Line {

    private ArrayList<View> views = new ArrayList<>();

    private int maxWidth;

    private int userWidth;

    public int height;

    private int space;

    public Line(int maxWidth, int space) {
        this.maxWidth = maxWidth;
        this.space = space;
    }

    public void addView(View view) {
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

    public boolean canAddView(View view) {
        if (views.size() == 0) {
            return true;
        }
        if (maxWidth < userWidth + view.getMeasuredWidth() + space) {
            return false;
        }
        return true;
    }

    public void layout(int l, int t) {

        int avg = (maxWidth - userWidth) / views.size();
        for (View view : views) {

            int measureWidth = view.getMeasuredWidth();
            int measureHeight = view.getMeasuredHeight();
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(measureWidth + avg, View.MeasureSpec.EXACTLY);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measureHeight, View.MeasureSpec.EXACTLY);
            view.measure(widthMeasureSpec, heightMeasureSpec);

            measureWidth = view.getMeasuredWidth();
            int left = l;
            int top = t;
            int right = measureWidth + left;
            int bottom = height + top;
            view.layout(left, top, right, bottom);

            l += measureWidth + space;
        }
    }
}
