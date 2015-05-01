package com.kekebox.hukewei.javlibraryapp;

/**
 * Created by hukewei on 01/05/15.
 */
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeableViewPager extends ViewPager {
    double x1;
    double x2;
    private boolean enabled;

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (getCurrentItem() == 3 && detectSwipeToLeft(event)) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCurrentItem() == 3 && detectSwipeToLeft(event)) {
            return false;
        }
        return super.onTouchEvent(event);

    }

    // To enable/disable swipe
    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Detects the direction of swipe. Right or left.
// Returns true if swipe is in right direction
    public boolean detectSwipeToLeft(MotionEvent event){

        int initialXValue = 0; // as we have to detect swipe to right
        final int SWIPE_THRESHOLD = 100; // detect swipe
        boolean result = false;

        try {
            float diffX = event.getX() - initialXValue;

            if (Math.abs(diffX) > SWIPE_THRESHOLD ) {
                if (diffX > 0) {
                    // swipe from left to right detected ie.SwipeRight
                    result = true;
                } else {
                    // swipe from right to left detected ie.SwipeLeft
                    result = false;
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
}