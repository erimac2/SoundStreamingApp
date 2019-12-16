package com.erimac2.soundstreamingapp;

import androidx.room.RoomDatabase;


@androidx.room.Database(entities = {UserDB.class}, version = 1)
public abstract class DatabaseClass extends RoomDatabase {
    public abstract UserDao userDao();
}
