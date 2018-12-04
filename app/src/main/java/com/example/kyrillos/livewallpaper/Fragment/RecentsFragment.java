package com.example.kyrillos.livewallpaper.Fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Adapter.MyRecyclerAdapter;
import com.example.kyrillos.livewallpaper.Database.DataSource.RecentRepository;
import com.example.kyrillos.livewallpaper.Database.LocalDatabase.LocalDatabase;
import com.example.kyrillos.livewallpaper.Database.LocalDatabase.RecentsDataSource;
import com.example.kyrillos.livewallpaper.Database.Recents;
import com.example.kyrillos.livewallpaper.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("ValidFragment")
public class RecentsFragment extends Fragment {
    private static RecentsFragment INSTANCE = null;
    Context context;
    RecyclerView recyclerView;
    List<Recents> recentsList;
    MyRecyclerAdapter adapter;


    //Room DB
    CompositeDisposable  compositeDisposable;
    RecentRepository recentRepository;

    @SuppressLint("ValidFragment")
    public RecentsFragment(Context context) {
        this.context=context;

        compositeDisposable = new CompositeDisposable();
        LocalDatabase database = LocalDatabase.getInstance(context);
        recentRepository = RecentRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));
        // Required empty public constructor
    }

    public static RecentsFragment getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = new RecentsFragment(context);
        }
        return INSTANCE;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_recents, container, false);
        recyclerView = view.findViewById(R.id.recycler_recent);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recentsList = new ArrayList<>();
        adapter = new MyRecyclerAdapter(context,recentsList);
        recyclerView.setAdapter(adapter);

        loadRecents();




        return view;
    }

    private void loadRecents() {
        Disposable  disposable= recentRepository.getAllRecents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Recents>>() {
                    @Override
                    public void accept(List<Recents> recents) throws Exception {
                        onGetAllRecentSuccess(recents);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(context, "Error " +throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllRecentSuccess(List<Recents> recents) {
        recentsList.clear();
        recentsList.addAll(recents);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
