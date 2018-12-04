package com.example.kyrillos.livewallpaper.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kyrillos.livewallpaper.Interface.ItemClickListener;
import com.example.kyrillos.livewallpaper.R;

public class categoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView categoryName;
    public ImageView categoryImage;
    ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public categoryViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryImage = itemView.findViewById(R.id.image);
        categoryName = itemView.findViewById(R.id.name);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition());
    }


}
