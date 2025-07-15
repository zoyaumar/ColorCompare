package com.example.colorcompare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CropActivity extends AppCompatActivity {
    
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_CROPPED_URI = "cropped_uri";
    public static final int RESULT_CROP_SUCCESS = 100;
    public static final int RESULT_CROP_ERROR = 101;
    
    private ImageView imageView;
    private Button cropButton, cancelButton;
    private CropOverlayView cropOverlay;
    private Uri imageUri;
    private Bitmap originalBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        
        initViews();
        setupListeners();
        loadImage();
    }
    
    private void initViews() {
        imageView = findViewById(R.id.crop_image_view);
        cropButton = findViewById(R.id.crop_button);
        cancelButton = findViewById(R.id.cancel_button);
        cropOverlay = findViewById(R.id.crop_overlay);
    }
    
    private void setupListeners() {
        cropButton.setOnClickListener(v -> cropImage());
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
    
    private void loadImage() {
        imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        if (imageUri == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CROP_ERROR);
            finish();
            return;
        }
        
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    originalBitmap = resource;
                    imageView.setImageBitmap(resource);
                    cropOverlay.setBitmap(resource);
                }
                
                @Override
                public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {}
            });
    }
    
    private void cropImage() {
        if (originalBitmap == null) {
            Toast.makeText(this, "Image not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get crop bounds from overlay
            android.graphics.RectF cropRect = cropOverlay.getCropRect();

            // Calculate the actual crop area on the bitmap, accounting for centerInside scaling
            float viewWidth = imageView.getWidth();
            float viewHeight = imageView.getHeight();
            float imageWidth = originalBitmap.getWidth();
            float imageHeight = originalBitmap.getHeight();

            float scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
            float displayedWidth = imageWidth * scale;
            float displayedHeight = imageHeight * scale;
            float dx = (viewWidth - displayedWidth) / 2f;
            float dy = (viewHeight - displayedHeight) / 2f;

            // Map cropRect from view coordinates to bitmap coordinates
            float cropLeft = (cropRect.left - dx) / scale;
            float cropTop = (cropRect.top - dy) / scale;
            float cropRight = (cropRect.right - dx) / scale;
            float cropBottom = (cropRect.bottom - dy) / scale;

            int startX = Math.max(0, Math.round(cropLeft));
            int startY = Math.max(0, Math.round(cropTop));
            int width = Math.min(originalBitmap.getWidth() - startX, Math.round(cropRight - cropLeft));
            int height = Math.min(originalBitmap.getHeight() - startY, Math.round(cropBottom - cropTop));

            // Ensure square crop
            int size = Math.min(width, height);

            // Create cropped bitmap
            Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, startX, startY, size, size);

            // Scale to desired size (800x800)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 800, 800, true);

            // Save to cache
            File croppedFile = new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(croppedFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Return result
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_CROPPED_URI, Uri.fromFile(croppedFile));
            setResult(RESULT_CROP_SUCCESS, resultIntent);
            finish();

        } catch (Exception e) {
            Log.e("CropActivity", "Error cropping image", e);
            Toast.makeText(this, "Error cropping image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CROP_ERROR);
            finish();
        }
    }
}