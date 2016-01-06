package io.demiseq.jetreader.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.demiseq.jetreader.R;

public class CustomViewPager extends ViewPager {

    float mStartDragX;
    OnSwipeOutListener mListener;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        mListener = listener;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = ev.getX();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(getAdapter() != null) {
            if (getCurrentItem() == getAdapter().getCount() - 1) {
                final int action = ev.getAction();
                float x = ev.getX();
                switch (action & MotionEventCompat.ACTION_MASK) {
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (getCurrentItem() == getAdapter().getCount() - 1 && x + 50 < mStartDragX) {
                            View view = findViewWithTag(getAdapter().getCount() - 1);
                            if (view != null) {
                                TouchImageView img = (TouchImageView) view.findViewById(R.id.img);
                                if (!img.isZoomed()) {
                                    mListener.onSwipeOutAtEnd();
                                }
                            }
                        }
                        break;
                }
            } else {
                mStartDragX = 0;
            }
        }
        return super.onTouchEvent(ev);
    }

    public interface OnSwipeOutListener {
//        void onSwipeOutAtStart();

        void onSwipeOutAtEnd();
    }

}