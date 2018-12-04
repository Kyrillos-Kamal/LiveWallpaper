package com.example.kyrillos.livewallpaper.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Common.Common;
import com.example.kyrillos.livewallpaper.ListWallpaperActivity;
import com.example.kyrillos.livewallpaper.ViewHolder.categoryViewHolder;
import com.example.kyrillos.livewallpaper.Interface.ItemClickListener;
import com.example.kyrillos.livewallpaper.Model.CategoryItem;
import com.example.kyrillos.livewallpaper.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class CategoryFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference categoryBackground;
    private FirebaseRecyclerOptions<CategoryItem> options;
    private FirebaseRecyclerAdapter<CategoryItem,categoryViewHolder> adapter;

    private RecyclerView recyclerView;

    private static CategoryFragment INSTANCE = null;
   public CategoryFragment() {
        // Required empty public constructor
       database =FirebaseDatabase.getInstance();
       categoryBackground = database.getReference(Common.STR_CATEGORY_BACKGROUND);
       categoryBackground.keepSynced(true);
       options = new FirebaseRecyclerOptions.Builder<CategoryItem>()
               .setQuery(categoryBackground,CategoryItem.class) // select all
               .build();
       adapter = new FirebaseRecyclerAdapter<CategoryItem, categoryViewHolder>(options) {
           @Override
           protected void onBindViewHolder(@NonNull final categoryViewHolder holder, int position, @NonNull final CategoryItem model) {
               Picasso.with(getActivity())
                       .load(model.getImageLink())
                       .networkPolicy(NetworkPolicy.OFFLINE)
                       .into(holder.categoryImage, new Callback() {
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {
                               Picasso.with(getActivity())
                                       .load(model.getImageLink())
                                       .error(R.mipmap.error)
                                       .into(holder.categoryImage, new Callback() {
                                           @Override
                                           public void onSuccess() {

                                           }

                                           @Override
                                           public void onError() {
                                               Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                           }
                                       });
                           }
                       });

               holder.categoryName.setText(model.getName());
               holder.setItemClickListener(new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position) {
                       Common.CATEGORY_ID_SELECTED = adapter.getRef(position).getKey();
                   Common.CATEGORY_SELECTED = model.getName();
                   startActivity(new Intent(getActivity(),ListWallpaperActivity.class));
               }
               });
           }

           @NonNull
           @Override
           public categoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
               View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_category_item,viewGroup,false);
               return new categoryViewHolder(view);
           }
       };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public static CategoryFragment getInstance(){
       if (INSTANCE == null){
           INSTANCE = new CategoryFragment();
       }
       return INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_category, container, false);
        recyclerView = view.findViewById(R.id.recycler_category);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);

        setCategory();

        return view;
    }

    private void setCategory() {
       adapter.startListening();
       recyclerView.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
       super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }



}
