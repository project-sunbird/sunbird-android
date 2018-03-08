package org.sunbird.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by nikith.shetty on 06/03/18.
 */

public class SwipeToRefresh extends SwipeRefreshLayout {

    private int mTouchSlop;
    private float mPrevX;

    public SwipeToRefresh(Context context) {
        super(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (xDiff > mTouchSlop) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}
