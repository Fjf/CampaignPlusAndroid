package com.example.campaignplus.campaign.Listeners;

import android.view.MotionEvent;
import android.view.View;

import com.example.campaignplus._utils.CallBack;

public class SwipeDismissListener implements View.OnTouchListener {
    private final CallBack f;
    private float x1, y1;

    public SwipeDismissListener(CallBack f) {
        this.f = f;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float x2 = event.getX();
            float y2 = event.getY();

            // Only activate when not moving horizontally much, but moving a lot vertically.
            if (Math.abs(x1 - x2) < 100 &&
                    y1 - y2 > 100) {
                // Do callback
                f.success();
            }
        }

        return true;
    }
}
