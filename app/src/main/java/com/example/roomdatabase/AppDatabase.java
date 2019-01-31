package com.example.roomdatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Number.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract Dao_Numbers numberDao();
}

