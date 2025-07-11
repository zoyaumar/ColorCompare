package com.example.colorcompare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<ColorData> imageList;
    private Context context;

    public ImageAdapter(Context context) {
        this.context = context;
        this.imageList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_grid, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ColorData colorData = imageList.get(position);
        
        // Set the color as background
        try {
            int color = Color.parseColor(colorData.getColorHex());
            holder.colorBackground.setCardBackgroundColor(color);
        } catch (IllegalArgumentException e) {
            holder.colorBackground.setCardBackgroundColor(Color.GRAY);
        }
        
        // Load image
        if (colorData.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(colorData.getImageBitmap());
        } else if (colorData.getImageUrl() != null && !colorData.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(colorData.getImageUrl())
                .centerCrop()
                .into(holder.imageView);
        }
        
        // Set color info
        holder.colorHex.setText(colorData.getColorHex());
        holder.colorPercent.setText(String.format("%.1f%%", colorData.getColorPercent()));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void addImage(ColorData colorData) {
        imageList.add(colorData);
        sortByColor();
        notifyDataSetChanged();
    }

    public void sortByColor() {
        Collections.sort(imageList, (a, b) -> Integer.compare(a.getColorValue(), b.getColorValue()));
    }

    public List<ColorData> getImageList() {
        return imageList;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView colorHex;
        TextView colorPercent;
        CardView colorBackground;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_image);
            colorHex = itemView.findViewById(R.id.color_hex_text);
            colorPercent = itemView.findViewById(R.id.color_percent_text);
            colorBackground = itemView.findViewById(R.id.color_background);
        }
    }
}