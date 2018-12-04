package com.example.kyrillos.livewallpaper.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.kyrillos.livewallpaper.Interface.ItemClickListener;
import com.example.kyrillos.livewallpaper.R;

public class ListWallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView wallpaper;
    ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ListWallpaperViewHolder(@NonNull View itemView) {
        super(itemView);
        wallpaper = itemView.findViewById(R.id.wallpaperimage);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition());
    }
}
