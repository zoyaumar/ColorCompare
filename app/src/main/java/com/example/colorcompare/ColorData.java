package com.example.colorcompare;

import android.graphics.Bitmap;

public class ColorData implements Comparable<ColorData> {
    private String imageUrl;
    private Bitmap imageBitmap;
    private String selectedColor;
    private String colorHex;
    private double colorPercent;
    private String fileName;

    public ColorData(String imageUrl, Bitmap imageBitmap, String selectedColor, String colorHex, double colorPercent) {
        this.imageUrl = imageUrl;
        this.imageBitmap = imageBitmap;
        this.selectedColor = selectedColor;
        this.colorHex = colorHex;
        this.colorPercent = colorPercent;
        this.fileName = "";
    }

    // Getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Bitmap getImageBitmap() { return imageBitmap; }
    public void setImageBitmap(Bitmap imageBitmap) { this.imageBitmap = imageBitmap; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public double getColorPercent() { return colorPercent; }
    public void setColorPercent(double colorPercent) { this.colorPercent = colorPercent; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    // Compare by hex color for sorting
    @Override
    public int compareTo(ColorData other) {
        if (this.colorHex == null || other.colorHex == null) {
            return 0;
        }
        return this.colorHex.compareToIgnoreCase(other.colorHex);
    }

    // Convert hex to integer for better sorting
    public int getColorValue() {
        try {
            String hex = colorHex.replace("#", "");
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}