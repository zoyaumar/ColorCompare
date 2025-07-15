package com.example.colorcompare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.widget.Toast;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<ColorData> imageList;
    private Context context;
    private boolean hasShownDeleteInstructions = false;

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
        
        // Setup delete button
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        removeImage(adapterPosition);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Hide delete button when touching elsewhere
        // (No longer needed, delete button is always visible)
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ImageViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Ensure square items
        holder.itemView.post(() -> {
            int width = holder.itemView.getWidth();
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params != null && width > 0 && params.height != width) {
                params.height = width;
                holder.itemView.setLayoutParams(params);
            }
        });
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
        Collections.sort(imageList, (a, b) -> Float.compare(a.getHue(), b.getHue()));
    }
    
    public void removeImage(int position) {
        if (position >= 0 && position < imageList.size()) {
            imageList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageList.size());
        }
    }

    public List<ColorData> getImageList() {
        return imageList;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_image);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}