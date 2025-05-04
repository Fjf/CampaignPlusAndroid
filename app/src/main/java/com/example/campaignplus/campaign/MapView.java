package com.example.campaignplus.campaign;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.campaignplus._data.MapData;

import java.util.ArrayList;

public class MapView extends View {
    public interface OnMapSelectedCallback {
        void onMapSelected(MapData mapData);
    }

    private Bitmap bitmap;
    private ArrayList<MapData> childMaps;
    private Paint paint;
    private Paint textPaint;
    private float scaleFactor = 1.0f;
    private float translateX = 0;
    private float translateY = 0;
    private ScaleGestureDetector scaleDetector;

    private Matrix matrix = new Matrix();
    private Matrix transformMatrix = new Matrix();
    private float[] matrixValues = new float[9];


    private OnMapSelectedCallback cb = null;
    private Path path = new Path();

    public void resetZoom() {
        translateX = 0;
        translateY = 0;
        scaleFactor = 1.0f;
    }

    public MapView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
    }

    public void setOnMapSelectedCallback(OnMapSelectedCallback cb) {
        this.cb = cb;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public void setChildMaps(ArrayList<MapData> childMaps) {
        this.childMaps = childMaps;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.save();
            canvas.concat(transformMatrix);
            canvas.drawBitmap(bitmap, 0, 0, null);
            if (childMaps != null) {
                for (MapData childMap : childMaps) {
                    float x = (float) childMap.x;
                    float y = (float) childMap.y;
                    // Draw a marker
                    int rad = 10;
                    int height = 25;
                    canvas.drawCircle(x, y - height, rad, paint);
                    // Draw the triangle
                    path.reset();
                    path.moveTo(x, y); // Bottom of the triangle
                    path.lineTo(x - rad, y - height); // Left point of the triangle
                    path.lineTo(x + rad, y - height); // Right point of the triangle
                    path.close();
                    canvas.drawPath(path, paint);
                    canvas.drawText(childMap.name, x + 15, y + 5, textPaint);
                }
            }
            canvas.restore();
        }
    }

    private float lastX, lastY;
    private boolean isScaling = false;
    private boolean isDragging = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!isScaling && pointerCount == 1) {
                    lastX = event.getX();
                    lastY = event.getY();
                    isDragging = true;

                    // Check for child map clicks only on single-finger tap
                    float[] childMapCoords = new float[2];
                    for (MapData childMap : childMaps) {
                        childMapCoords[0] = (float) childMap.x;
                        childMapCoords[1] = (float) childMap.y;

                        Matrix inverse = new Matrix();
                        transformMatrix.invert(inverse);
                        float[] touchPoint = {event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);

                        float dx = touchPoint[0] - childMapCoords[0];
                        float dy = touchPoint[1] - childMapCoords[1];
                        if (Math.hypot(dx, dy) < 30) {
                            if (cb != null) {
                                cb.onMapSelected(childMap);
                            }
                            return true;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isDragging = false;  // Stop dragging when multi-touch begins
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isScaling && isDragging && pointerCount == 1) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    transformMatrix.postTranslate(dx, dy);
                    lastX = event.getX();
                    lastY = event.getY();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                isDragging = false;  // Prevent misaligned drag if a finger lifts
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            transformMatrix.postScale(scale, scale, focusX, focusY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (bitmap != null) {
            float scaleX = (float) width / bitmap.getWidth();
            float scaleY = (float) height / bitmap.getHeight();
            scaleFactor = Math.min(scaleX, scaleY);
            transformMatrix.reset();
            transformMatrix.postScale(scaleFactor, scaleFactor);
            transformMatrix.postTranslate(
                    (width - bitmap.getWidth() * scaleFactor) / 2f,
                    (height - bitmap.getHeight() * scaleFactor) / 2f
            );

            invalidate();
        }
    }
}
