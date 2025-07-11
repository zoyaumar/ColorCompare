package com.example.colorcompare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {
    
    private Paint cropPaint;
    private Paint overlayPaint;
    private Paint cornerPaint;
    private RectF cropRect;
    private Bitmap bitmap;
    private boolean isDragging = false;
    private float lastTouchX, lastTouchY;
    private int cornerSize = 30;
    
    public CropOverlayView(Context context) {
        super(context);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        cropPaint = new Paint();
        cropPaint.setColor(Color.WHITE);
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setStrokeWidth(4f);
        
        overlayPaint = new Paint();
        overlayPaint.setColor(Color.BLACK);
        overlayPaint.setAlpha(128); // Semi-transparent
        
        cornerPaint = new Paint();
        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
        
        // Initialize with a default crop rect
        cropRect = new RectF();
    }
    
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        // Initialize crop rect to center square
        post(() -> {
            int size = Math.min(getWidth(), getHeight()) - 100;
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;
            cropRect.set(left, top, left + size, top + size);
            invalidate();
        });
    }
    
    public RectF getCropRect() {
        return new RectF(cropRect);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (cropRect.isEmpty()) {
            return;
        }
        
        // Draw dark overlay outside crop area
        canvas.drawRect(0, 0, getWidth(), cropRect.top, overlayPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, overlayPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), overlayPaint);
        
        // Draw crop rectangle
        canvas.drawRect(cropRect, cropPaint);
        
        // Draw corner handles
        drawCornerHandle(canvas, cropRect.left, cropRect.top);
        drawCornerHandle(canvas, cropRect.right, cropRect.top);
        drawCornerHandle(canvas, cropRect.left, cropRect.bottom);
        drawCornerHandle(canvas, cropRect.right, cropRect.bottom);
        
        // Draw grid lines
        float thirdWidth = cropRect.width() / 3;
        float thirdHeight = cropRect.height() / 3;
        
        // Vertical lines
        canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, 
                       cropRect.left + thirdWidth, cropRect.bottom, cropPaint);
        canvas.drawLine(cropRect.left + 2 * thirdWidth, cropRect.top, 
                       cropRect.left + 2 * thirdWidth, cropRect.bottom, cropPaint);
        
        // Horizontal lines
        canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, 
                       cropRect.right, cropRect.top + thirdHeight, cropPaint);
        canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdHeight, 
                       cropRect.right, cropRect.top + 2 * thirdHeight, cropPaint);
    }
    
    private void drawCornerHandle(Canvas canvas, float x, float y) {
        canvas.drawRect(x - cornerSize/2, y - cornerSize/2, 
                       x + cornerSize/2, y + cornerSize/2, cornerPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cropRect.contains(x, y)) {
                    isDragging = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;
                    
                    // Move the crop rectangle
                    RectF newRect = new RectF(cropRect);
                    newRect.offset(deltaX, deltaY);
                    
                    // Keep within bounds
                    if (newRect.left >= 0 && newRect.right <= getWidth() &&
                        newRect.top >= 0 && newRect.bottom <= getHeight()) {
                        cropRect.set(newRect);
                        invalidate();
                    }
                    
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Reset crop rect when size changes
        if (bitmap != null) {
            setBitmap(bitmap);
        }
    }
}