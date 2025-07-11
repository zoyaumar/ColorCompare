package com.example.colorcompare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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
        
        // Load image
        if (colorData.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(colorData.getImageBitmap());
        } else if (colorData.getImageUrl() != null && !colorData.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(colorData.getImageUrl())
                .centerCrop()
                .into(holder.imageView);
        }
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

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_image);
        }
    }
}