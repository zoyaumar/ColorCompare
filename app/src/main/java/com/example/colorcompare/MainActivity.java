package com.example.colorcompare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    
    // UI Elements
    private com.google.android.material.button.MaterialButton addButton, uploadButton, color1Button, color2Button, color3Button, downloadButton;
    private com.google.android.material.button.MaterialButton grid2x2, grid3x3, grid4x4, grid5x5;
    private EditText inputUrl;
    private TextView color1Text, color2Text, color3Text;
    private ImageView logoImageView;
    private RecyclerView imagesRecyclerView;
    private int currentGridColumns = 3;
    
    // Data
    private ImageAdapter imageAdapter;
    private List<JSONObject> currentColors;
    private String currentImageUrl;
    private Bitmap currentImageBitmap;
    
    // Activity launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupRecyclerView();
        setupActivityLaunchers();
        requestPermissions();
        setupClickListeners();
    }

    private void initializeViews() {
        addButton = findViewById(R.id.button4);
        uploadButton = findViewById(R.id.button6);
        color1Button = findViewById(R.id.button2);
        color2Button = findViewById(R.id.button3);
        color3Button = findViewById(R.id.button);
        downloadButton = findViewById(R.id.download_button);
        
        grid2x2 = findViewById(R.id.grid_2x2);
        grid3x3 = findViewById(R.id.grid_3x3);
        grid4x4 = findViewById(R.id.grid_4x4);
        grid5x5 = findViewById(R.id.grid_5x5);
        
        inputUrl = findViewById(R.id.editTextText);
        color1Text = findViewById(R.id.textView4);
        color2Text = findViewById(R.id.textView5);
        color3Text = findViewById(R.id.textView6);
        logoImageView = findViewById(R.id.imageView2);
        imagesRecyclerView = findViewById(R.id.images_recycler_view);
        
        // Initialize with default state
        resetColorButtons();
        
        // Set placeholder hint
        inputUrl.setHint("Enter image URL here...");
    }



    private void setupRecyclerView() {
        imageAdapter = new ImageAdapter(this);
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, currentGridColumns));
        imagesRecyclerView.setAdapter(imageAdapter);
    }

    private void setupActivityLaunchers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        loadImageFromUri(imageUri);
                    }
                }
            }
        );
        
        cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == CropActivity.RESULT_CROP_SUCCESS) {
                    if (result.getData() != null) {
                        Uri croppedUri = result.getData().getParcelableExtra(CropActivity.EXTRA_CROPPED_URI);
                        if (croppedUri != null) {
                            handleCroppedImage(croppedUri);
                        }
                    }
                } else if (result.getResultCode() == CropActivity.RESULT_CROP_ERROR) {
                    Toast.makeText(this, "Image cropping failed", Toast.LENGTH_SHORT).show();
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Cropping cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupClickListeners() {
        addButton.setOnClickListener(v -> {
            String url = inputUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                loadImageFromUrl(url);
            } else {
                Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            }
        });

        uploadButton.setOnClickListener(v -> openImagePicker());

        color1Button.setOnClickListener(v -> selectColor(0));
        color2Button.setOnClickListener(v -> selectColor(1));
        color3Button.setOnClickListener(v -> selectColor(2));

        downloadButton.setOnClickListener(v -> downloadColorGrid());
        
        // Grid dimension buttons
        grid2x2.setOnClickListener(v -> changeGridLayout(2));
        grid3x3.setOnClickListener(v -> changeGridLayout(3));
        grid4x4.setOnClickListener(v -> changeGridLayout(4));
        grid5x5.setOnClickListener(v -> changeGridLayout(5));
    }
    
    private void changeGridLayout(int columns) {
        currentGridColumns = columns;
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));
        
        // Update button states
        updateGridButtonStates(columns);
    }
    
    private void updateGridButtonStates(int activeColumns) {
        // Reset all buttons to inactive state
        try {
            grid2x2.setBackgroundTintList(null);
            grid2x2.setTextColor(getColor(R.color.secondary_text));
            if (grid2x2 instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) grid2x2).setStrokeColorResource(R.color.button_stroke);
            }
            
            grid3x3.setBackgroundTintList(null);
            grid3x3.setTextColor(getColor(R.color.secondary_text));
            if (grid3x3 instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) grid3x3).setStrokeColorResource(R.color.button_stroke);
            }
            
            grid4x4.setBackgroundTintList(null);
            grid4x4.setTextColor(getColor(R.color.secondary_text));
            if (grid4x4 instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) grid4x4).setStrokeColorResource(R.color.button_stroke);
            }
            
            grid5x5.setBackgroundTintList(null);
            grid5x5.setTextColor(getColor(R.color.secondary_text));
            if (grid5x5 instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) grid5x5).setStrokeColorResource(R.color.button_stroke);
            }
            
            // Set active button
            com.google.android.material.button.MaterialButton activeButton = null;
            switch (activeColumns) {
                case 2: activeButton = grid2x2; break;
                case 3: activeButton = grid3x3; break;
                case 4: activeButton = grid4x4; break;
                case 5: activeButton = grid5x5; break;
            }
            
            if (activeButton != null) {
                activeButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
                activeButton.setTextColor(getColor(R.color.white));
                if (activeButton instanceof com.google.android.material.button.MaterialButton) {
                    ((com.google.android.material.button.MaterialButton) activeButton).setStrokeColorResource(R.color.primary_color);
                }
            }
        } catch (Exception e) {
            // Fallback: just update colors without stroke
            updateGridButtonStatesSimple(activeColumns);
        }
    }
    
    private void updateGridButtonStatesSimple(int activeColumns) {
        // Simple fallback method without MaterialButton-specific features
        int inactiveColor = getColor(R.color.secondary_text);
        int activeColor = getColor(R.color.white);
        int activeBgColor = getColor(R.color.primary_color);
        
        // Reset all to inactive
        grid2x2.setBackgroundTintList(null);
        grid2x2.setTextColor(inactiveColor);
        grid3x3.setBackgroundTintList(null);
        grid3x3.setTextColor(inactiveColor);
        grid4x4.setBackgroundTintList(null);
        grid4x4.setTextColor(inactiveColor);
        grid5x5.setBackgroundTintList(null);
        grid5x5.setTextColor(inactiveColor);
        
        // Set active button
        switch (activeColumns) {
            case 2:
                grid2x2.setBackgroundTintList(getColorStateList(R.color.primary_color));
                grid2x2.setTextColor(activeColor);
                break;
            case 3:
                grid3x3.setBackgroundTintList(getColorStateList(R.color.primary_color));
                grid3x3.setTextColor(activeColor);
                break;
            case 4:
                grid4x4.setBackgroundTintList(getColorStateList(R.color.primary_color));
                grid4x4.setTextColor(activeColor);
                break;
            case 5:
                grid5x5.setBackgroundTintList(getColorStateList(R.color.primary_color));
                grid5x5.setTextColor(activeColor);
                break;
        }
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadImageFromUrl(String imageUrl) {
        currentImageUrl = imageUrl;
        currentImageBitmap = null;
        
        // Load image preview
        Glide.with(this)
            .load(imageUrl)
            .into(logoImageView);
        
        // Show loading message
        Toast.makeText(this, "Analyzing colors...", Toast.LENGTH_SHORT).show();
        
        // Fetch colors from Imagga API
        new Thread(() -> fetchImageColors(imageUrl)).start();
    }

    private void loadImageFromUri(Uri imageUri) {
        currentImageUrl = imageUri.toString();
        
        // Start our custom CropActivity
        Intent cropIntent = new Intent(this, CropActivity.class);
        cropIntent.putExtra(CropActivity.EXTRA_IMAGE_URI, imageUri);
        cropImageLauncher.launch(cropIntent);
    }
    
    private void handleCroppedImage(Uri croppedImageUri) {
        Glide.with(this)
            .asBitmap()
            .load(croppedImageUri)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                    currentImageBitmap = resource;
                    logoImageView.setImageBitmap(resource);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Image cropped and loaded. Analyzing dominant colors...", Toast.LENGTH_SHORT).show();
                        // Analyze colors from the bitmap directly
                        analyzeImageColors(resource);
                    });
                }

                @Override
                public void onLoadCleared(Drawable placeholder) {}
            });
    }

    private void analyzeImageColors(Bitmap bitmap) {
        new Thread(() -> {
            try {
                currentColors = new ArrayList<>();
                
                // Extract dominant colors from bitmap
                java.util.Map<Integer, Integer> colorMap = new java.util.HashMap<>();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int totalPixels = width * height;
                
                // Sample pixels (every 10th pixel for performance)
                for (int x = 0; x < width; x += 10) {
                    for (int y = 0; y < height; y += 10) {
                        int pixel = bitmap.getPixel(x, y);
                        // Reduce color precision for grouping similar colors
                        int reducedColor = reduceColorPrecision(pixel);
                        colorMap.put(reducedColor, colorMap.getOrDefault(reducedColor, 0) + 1);
                    }
                }
                
                // Sort colors by frequency
                java.util.List<java.util.Map.Entry<Integer, Integer>> sortedColors = new java.util.ArrayList<>(colorMap.entrySet());
                sortedColors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                
                // Create color objects for top 3 colors
                int sampledPixels = (width / 10) * (height / 10);
                for (int i = 0; i < Math.min(3, sortedColors.size()); i++) {
                    java.util.Map.Entry<Integer, Integer> entry = sortedColors.get(i);
                    int color = entry.getKey();
                    int count = entry.getValue();
                    double percent = (count * 100.0) / sampledPixels;
                    
                    JSONObject colorObj = new JSONObject();
                    colorObj.put("html_code", String.format("#%06X", (0xFFFFFF & color)));
                    colorObj.put("percent", percent);
                    currentColors.add(colorObj);
                }
                
                runOnUiThread(() -> updateColorButtons());
                
            } catch (Exception e) {
                Log.e("ColorAnalysis", "Error analyzing image colors", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error analyzing colors", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    
    private int reduceColorPrecision(int color) {
        // Reduce precision by masking lower bits to group similar colors
        int r = (Color.red(color) / 32) * 32;
        int g = (Color.green(color) / 32) * 32;
        int b = (Color.blue(color) / 32) * 32;
        return Color.rgb(r, g, b);
    }

    private void fetchImageColors(String imageUrl) {
        String credentialsToEncode = "acc_4b98ddb60e3ded4" + ":" + "1e8a345cc9296bb0ad349432ee24d01a";
        String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

        String endpointUrl = "https://api.imagga.com/v2/colors";

        HttpURLConnection connection = null;
        try {
            String url = endpointUrl + "?image_url=" + imageUrl;
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();

            connection.setRequestProperty("Authorization", "Basic " + basicAuth);

            int responseCode = connection.getResponseCode();
            Log.d("ImaggaApi", "Sending 'GET' request to URL : " + url);
            Log.d("ImaggaApi", "Response Code : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader connectionInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = connectionInput.readLine()) != null) {
                    jsonResponse.append(line);
                }
                connectionInput.close();
                
                Log.d("ImaggaApi", "API Response: " + jsonResponse.toString());
                parseColorResponse(jsonResponse.toString());
                
            } else {
                Log.e("ImaggaApi", "Error: " + responseCode);
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch colors from API. Response code: " + responseCode, Toast.LENGTH_SHORT).show());
            }

        } catch (IOException e) {
            Log.e("ImaggaApi", "IOException: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void parseColorResponse(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONObject result = response.getJSONObject("result");
            JSONObject colors = result.getJSONObject("colors");
            JSONArray imageColors = colors.getJSONArray("image_colors");

            currentColors = new ArrayList<>();
            
            // Get top 3 colors
            for (int i = 0; i < Math.min(3, imageColors.length()); i++) {
                currentColors.add(imageColors.getJSONObject(i));
            }

            runOnUiThread(() -> updateColorButtons());

        } catch (JSONException e) {
            Log.e("ColorParsing", "Error parsing JSON response", e);
            runOnUiThread(() -> Toast.makeText(this, "Error parsing color data", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateColorButtons() {
        if (currentColors == null || currentColors.isEmpty()) {
            resetColorButtons();
            return;
        }

        try {
            for (int i = 0; i < currentColors.size(); i++) {
                JSONObject colorData = currentColors.get(i);
                String hexColor = colorData.getString("html_code");
                double percent = colorData.getDouble("percent");

                Button button;
                TextView textView;

                switch (i) {
                    case 0:
                        button = color1Button;
                        textView = color1Text;
                        break;
                    case 1:
                        button = color2Button;
                        textView = color2Text;
                        break;
                    case 2:
                        button = color3Button;
                        textView = color3Text;
                        break;
                    default:
                        continue;
                }

                button.setText(hexColor);
                button.setBackgroundColor(Color.parseColor(hexColor));
                button.setVisibility(View.VISIBLE);
                
                textView.setText(String.format("%.1f%%", percent));
                textView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Log.e("ColorUpdate", "Error updating color buttons", e);
        }
    }

    private void resetColorButtons() {
        color1Button.setText("#0000");
        color1Button.setBackgroundColor(Color.GRAY);
        color1Text.setText("0%");
        
        color2Button.setText("#0000");
        color2Button.setBackgroundColor(Color.GRAY);
        color2Text.setText("0%");
        
        color3Button.setText("#0000");
        color3Button.setBackgroundColor(Color.GRAY);
        color3Text.setText("0%");
    }

    private void selectColor(int colorIndex) {
        if (currentColors == null || colorIndex >= currentColors.size()) {
            Toast.makeText(this, "Please load an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject selectedColorData = currentColors.get(colorIndex);
            String hexColor = selectedColorData.getString("html_code");
            double percent = selectedColorData.getDouble("percent");

            ColorData colorData = new ColorData(currentImageUrl, currentImageBitmap, hexColor, hexColor, percent);
            imageAdapter.addImage(colorData);

            Toast.makeText(this, "Image added with color " + hexColor, Toast.LENGTH_SHORT).show();
            
            // Clear current image data
            resetColorButtons();
            currentColors = null;
            currentImageUrl = null;
            currentImageBitmap = null;
            inputUrl.setText("");
            
        } catch (JSONException e) {
            Log.e("ColorSelection", "Error selecting color", e);
            Toast.makeText(this, "Error selecting color", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadColorGrid() {
        List<ColorData> imageList = imageAdapter.getImageList();
        if (imageList.isEmpty()) {
            Toast.makeText(this, "No images to download", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Bitmap gridBitmap = createGridBitmap(imageList);
                saveGridToFile(gridBitmap);
                
                runOnUiThread(() -> Toast.makeText(this, "Grid saved to Downloads", Toast.LENGTH_LONG).show());
                
            } catch (Exception e) {
                Log.e("Download", "Error creating grid", e);
                runOnUiThread(() -> Toast.makeText(this, "Error creating grid: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private Bitmap createGridBitmap(List<ColorData> imageList) {
        int imageSize = 200;
        int columns = currentGridColumns;
        int rows = (int) Math.ceil(imageList.size() / (double) columns);
        
        int gridWidth = columns * imageSize;
        int gridHeight = rows * imageSize;
        
        Bitmap gridBitmap = Bitmap.createBitmap(gridWidth, gridHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gridBitmap);
        
        // Fill background with white
        canvas.drawColor(Color.WHITE);
        
        for (int i = 0; i < imageList.size(); i++) {
            ColorData colorData = imageList.get(i);
            int row = i / columns;
            int col = i % columns;
            
            int x = col * imageSize;
            int y = row * imageSize;
            
            // Draw colored border/background
            Paint paint = new Paint();
            try {
                int color = Color.parseColor(colorData.getColorHex());
                paint.setColor(color);
                canvas.drawRect(x, y, x + imageSize, y + imageSize, paint);
            } catch (IllegalArgumentException e) {
                paint.setColor(Color.GRAY);
                canvas.drawRect(x, y, x + imageSize, y + imageSize, paint);
            }
            
            // Draw image if available (slightly smaller to show the color border)
            if (colorData.getImageBitmap() != null) {
                int margin = 4;
                int innerSize = imageSize - (margin * 2);
                Bitmap scaledImage = Bitmap.createScaledBitmap(colorData.getImageBitmap(), innerSize, innerSize, true);
                canvas.drawBitmap(scaledImage, x + margin, y + margin, null);
            }
        }
        
        return gridBitmap;
    }

    private void saveGridToFile(Bitmap bitmap) throws IOException {
        String fileName = "color_grid_" + System.currentTimeMillis() + ".png";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);
        
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        
        // Add to media store
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions required for full functionality", Toast.LENGTH_LONG).show();
            }
        }
    }
}