package com.example.kyrillos.livewallpaper.Database.DataSource;

import com.example.kyrillos.livewallpaper.Database.Recents;

import java.util.List;

import io.reactivex.Flowable;


public interface IRecentsDataSource {
    Flowable<List<Recents>> getAllRecents();

    void insertRecents(Recents... recents);
    void updateRecents(Recents... recents);
    void deleteRecents(Recents... recents);
    void deleteAllRecents();

}
