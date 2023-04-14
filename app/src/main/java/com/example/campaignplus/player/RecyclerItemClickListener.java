package com.example.campaignplus.player;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector gestureDetector;
    private ClickListener clickListener;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                clickListener.onClick(child, recyclerView.getChildAdapterPosition(child));
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (child != null && clickListener != null) {
                    clickListener.onDoubleTap(child, recyclerView.getChildAdapterPosition(child));
                }
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface ClickListener {

        void onClick(View view, int position);

        void onDoubleTap(View view, int position);
    }
}