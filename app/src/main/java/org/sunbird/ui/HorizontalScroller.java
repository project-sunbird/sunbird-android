package org.sunbird.ui;

import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by nikith.shetty on 02/03/18.
 */

public class HorizontalScroller implements View.OnTouchListener{
    OnScrollStopListener listener;
    int initialX = 0;
    HorizontalScrollView scrollView;

    public HorizontalScroller(HorizontalScrollView scrollView) {
        this.scrollView = scrollView;
        this.scrollView.setOnTouchListener(this);
    }

    public interface OnScrollStopListener {
        void onScrollStopped(int x);
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
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
                    if (listener != null) {
                        listener.onScrollStopped(scrollView.getScrollX());
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
}
