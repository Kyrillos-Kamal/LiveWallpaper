package com.example.kyrillos.livewallpaper.Database.LocalDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


import com.example.kyrillos.livewallpaper.Database.Recents;

import static com.example.kyrillos.livewallpaper.Database.LocalDatabase.LocalDatabase.DATABASE_VERSION;


@Database(entities = Recents.class,version = DATABASE_VERSION,exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME ="LievWallpaper";

    public abstract RecentsDAO recentsDAO();

    private static LocalDatabase instance;

    public static LocalDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context,LocalDatabase.class,DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
