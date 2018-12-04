package com.example.kyrillos.livewallpaper.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Common.Common;
import com.example.kyrillos.livewallpaper.Interface.ItemClickListener;
import com.example.kyrillos.livewallpaper.ListWallpaperActivity;
import com.example.kyrillos.livewallpaper.Model.WallpaperItem;
import com.example.kyrillos.livewallpaper.R;
import com.example.kyrillos.livewallpaper.ViewHolder.ListWallpaperViewHolder;
import com.example.kyrillos.livewallpaper.ViewWallpaper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrendingFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference categoryBackground;

    FirebaseRecyclerOptions<WallpaperItem> options;
    FirebaseRecyclerAdapter<WallpaperItem,ListWallpaperViewHolder> adapter;

    private static TrendingFragment INSTANCE = null;

    public TrendingFragment() {
        database = FirebaseDatabase.getInstance();
        categoryBackground = database.getReference(Common.STR_WALLPAPER);

        Query query = categoryBackground.orderByChild("viewCount")
                .limitToLast(10);

        options = new FirebaseRecyclerOptions.Builder<WallpaperItem>()
                .setQuery(query,WallpaperItem.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<WallpaperItem, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder holder, int position, @NonNull final WallpaperItem model) {
                Picasso.with(getActivity())
                        .load(model.getImageUrl())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.wallpaper, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getActivity())
                                        .load(model.getImageUrl())
                                        .error(R.mipmap.error)
                                        .into(holder.wallpaper, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Toast.makeText(getContext(), "Couldn't fetch images", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent(getActivity(),ViewWallpaper.class);
                        Common.select_background = model;
                        Common.select_background_key = adapter.getRef(position).getKey();
                        startActivity(intent);
                    }
                });
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
        };
    }


    public static TrendingFragment getInstance(){
        if (INSTANCE == null){
            INSTANCE = new TrendingFragment();
        }
        return INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_trending, container, false);
        recyclerView = view.findViewById(R.id.recycler_trending);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loadTrendingList();

        return view;
    }

    private void loadTrendingList() {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
    }
}
