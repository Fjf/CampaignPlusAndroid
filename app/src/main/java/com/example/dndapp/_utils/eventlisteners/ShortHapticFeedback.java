package com.example.dndapp._utils.eventlisteners;

import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

public class ShortHapticFeedback implements View.OnTouchListener {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        return false;
    }
}

