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
    private boolean isResizing = false;
    private float lastTouchX, lastTouchY;
    private int cornerSize = 40;
    private int touchMargin = 60;
    private int resizingCorner = -1; // 0=topLeft, 1=topRight, 2=bottomLeft, 3=bottomRight
    
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
        // Draw larger, more visible corner handles
        cornerPaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, cornerSize/2, cornerPaint);
        
        // Draw border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        canvas.drawCircle(x, y, cornerSize/2, borderPaint);
        
        // Draw inner dot
        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.BLACK);
        dotPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, 6f, dotPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resizingCorner = getCornerTouched(x, y);
                if (resizingCorner != -1) {
                    isResizing = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                } else if (cropRect.contains(x, y)) {
                    isDragging = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (isResizing) {
                    resizeCropRect(x, y, resizingCorner);
                    invalidate();
                    return true;
                } else if (isDragging) {
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
                isResizing = false;
                resizingCorner = -1;
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private int getCornerTouched(float x, float y) {
        // Check if touch is near any corner
        if (isNearPoint(x, y, cropRect.left, cropRect.top)) return 0; // top-left
        if (isNearPoint(x, y, cropRect.right, cropRect.top)) return 1; // top-right
        if (isNearPoint(x, y, cropRect.left, cropRect.bottom)) return 2; // bottom-left
        if (isNearPoint(x, y, cropRect.right, cropRect.bottom)) return 3; // bottom-right
        return -1;
    }
    
    private boolean isNearPoint(float touchX, float touchY, float pointX, float pointY) {
        return Math.abs(touchX - pointX) <= touchMargin && Math.abs(touchY - pointY) <= touchMargin;
    }
    
    private void resizeCropRect(float x, float y, int corner) {
        float newLeft = cropRect.left;
        float newTop = cropRect.top;
        float newRight = cropRect.right;
        float newBottom = cropRect.bottom;
        
        // Calculate new dimensions based on which corner is being dragged
        switch (corner) {
            case 0: // top-left
                newLeft = x;
                newTop = y;
                break;
            case 1: // top-right
                newRight = x;
                newTop = y;
                break;
            case 2: // bottom-left
                newLeft = x;
                newBottom = y;
                break;
            case 3: // bottom-right
                newRight = x;
                newBottom = y;
                break;
        }
        
        // Maintain square aspect ratio
        float width = newRight - newLeft;
        float height = newBottom - newTop;
        float size = Math.min(Math.abs(width), Math.abs(height));
        
        // Ensure minimum size
        size = Math.max(size, 100);
        
        // Update rectangle to be square and within bounds
        switch (corner) {
            case 0: // top-left
                newRight = newLeft + size;
                newBottom = newTop + size;
                break;
            case 1: // top-right
                newLeft = newRight - size;
                newBottom = newTop + size;
                break;
            case 2: // bottom-left
                newRight = newLeft + size;
                newTop = newBottom - size;
                break;
            case 3: // bottom-right
                newLeft = newRight - size;
                newTop = newBottom - size;
                break;
        }
        
        // Keep within view bounds
        if (newLeft >= 0 && newRight <= getWidth() && newTop >= 0 && newBottom <= getHeight()) {
            cropRect.set(newLeft, newTop, newRight, newBottom);
        }
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