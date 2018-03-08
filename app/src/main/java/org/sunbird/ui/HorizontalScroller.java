package org.sunbird.ui;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by nikith.shetty on 02/03/18.
 */

public class HorizontalScroller implements View.OnTouchListener{
    private static String TAG = "HorizontalScroller";
    OnScrollStopListener listener;
    int initialX = 0;
    int posOnTouch = 0;
    HorizontalScrollView scrollView;
    GestureDetector gestureDetector;
    int SWIPE_MIN_DISTANCE = 15;
    int SWIPE_THRESHOLD_VELOCITY = 300;

    public HorizontalScroller(HorizontalScrollView scrollView) {
        this.scrollView = scrollView;
        this.scrollView.setOnTouchListener(this);
        this.gestureDetector = new GestureDetector(scrollView.getContext(), new CustomGestureDetector());
    }

    public interface OnScrollStopListener {
        void onScrollStopped(int deltaX);
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        if (gestureDetector.onTouchEvent(ev)){
            Log.e(TAG, "onTouch: -> handled by gesture");
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                posOnTouch = scrollView.getScrollX();
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouch: -> handled by scroll stop");
                checkIfScrollStopped();
        }
        return false;
    }

    private void checkIfScrollStopped() {
        initialX = scrollView.getScrollX();
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int updatedX = scrollView.getScrollX();
                if (updatedX == initialX) {
                    //we've stopped
                    scrollView.smoothScrollBy(0,0);
                    if (listener != null) {
                        listener.onScrollStopped(scrollView.getScrollX() - posOnTouch);
                    }
                } else {
                    initialX = updatedX;
                    checkIfScrollStopped();
                }
            }
        }, 50);
    }

    public void setOnScrollStoppedListener(OnScrollStopListener yListener) {
        listener = yListener;
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                scrollView.smoothScrollBy(0,0);
                if (listener != null) {
                    listener.onScrollStopped(Math.round(e1.getX() - e2.getX()));
                }
                return true;
            }
            return false;
            //super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
