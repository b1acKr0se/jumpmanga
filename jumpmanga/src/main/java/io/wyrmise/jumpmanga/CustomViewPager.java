package io.wyrmise.jumpmanga;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.wyrmise.jumpmanga.widget.TouchImageView;

/**
 * Created by Thanh on 7/4/2015.
 */
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
        float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = x;
                break;
            case MotionEvent.ACTION_MOVE:
//                if (mStartDragX < x && getCurrentItem() == 0) {
//                    View view = findViewWithTag(0);
//                    if (view != null) {
//                        TouchImageView img = (TouchImageView) view.findViewById(R.id.img);
//                        if (!img.isZoomed()) {
//                            mListener.onSwipeOutAtStart();
//                        }
//                    }
//                } else
                if (mStartDragX > x && getCurrentItem() == getAdapter().getCount() - 1) {
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
        return super.onInterceptTouchEvent(ev);
    }

    public interface OnSwipeOutListener {
//        void onSwipeOutAtStart();

        void onSwipeOutAtEnd();
    }

}