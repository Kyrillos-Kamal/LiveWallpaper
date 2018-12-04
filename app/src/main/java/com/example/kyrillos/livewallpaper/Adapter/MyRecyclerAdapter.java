package com.example.kyrillos.livewallpaper.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Common.Common;
import com.example.kyrillos.livewallpaper.Database.Recents;
import com.example.kyrillos.livewallpaper.Interface.ItemClickListener;
import com.example.kyrillos.livewallpaper.Model.WallpaperItem;
import com.example.kyrillos.livewallpaper.R;
import com.example.kyrillos.livewallpaper.ViewHolder.ListWallpaperViewHolder;
import com.example.kyrillos.livewallpaper.ViewWallpaper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<ListWallpaperViewHolder> {

    private Context context;
    private List<Recents>   recentses;


    public MyRecyclerAdapter(Context context, List<Recents> recentses) {
        this.context = context;
        this.recentses = recentses;
    }

    @NonNull
    @Override
    public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_wallpaper_item,viewGroup,false);
        int hight = viewGroup.getMeasuredHeight() / 2;
        itemView.setMinimumHeight(hight);
        return new ListWallpaperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListWallpaperViewHolder holder, final int i) {
        Picasso.with(context)
                .load(recentses.get(i).getImageUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.wallpaper, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context)
                                .load(recentses.get(i).getImageUrl())
                                .error(R.mipmap.error)
                                .into(holder.wallpaper, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Toast.makeText(context, "couldn't fetch image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(context,ViewWallpaper.class);
                WallpaperItem wallpaperItem = new WallpaperItem();
                wallpaperItem.setCategoryId(recentses.get(i).getCategoryId());
                wallpaperItem.setImageUrl(recentses.get(i).getImageUrl());
                Common.select_background = wallpaperItem;
                Common.select_background_key = recentses.get(position).getKey();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentses.size();
    }
}
