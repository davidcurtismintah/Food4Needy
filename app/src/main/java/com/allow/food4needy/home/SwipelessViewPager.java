package com.allow.food4needy.home;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SwipelessViewPager extends ViewPager{

    private boolean isPagingEnabled;

    public SwipelessViewPager(Context context) {
        super(context);
        isPagingEnabled = true;
    }

    public SwipelessViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isPagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isPagingEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isPagingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        return isPagingEnabled && super.executeKeyEvent(event);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        isPagingEnabled = pagingEnabled;
    }
}
