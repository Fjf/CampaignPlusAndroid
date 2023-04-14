package com.example.campaignplus._utils;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ExpandedListView extends ListView {

    private int old_count = 0;
    private int childHeight = -1;

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != old_count) {
            // Initially store the row height.
            if (childHeight == -1) {
                childHeight = getChildAt(0).getHeight();
            }

            old_count = getCount();
            android.view.ViewGroup.LayoutParams params = getLayoutParams();

            params.height = getCount() * (old_count > 0 ? childHeight + 2 : 0);
            setLayoutParams(params);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE)
            return true;
        return super.dispatchTouchEvent(ev);
    }
}