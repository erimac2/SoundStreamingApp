package com.erimac2.soundstreamingapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class UserDB implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public long user_id;

    @ColumnInfo(name = "title")
    public String title;

    public UserDB(long user_id, String title) {
        this.user_id = user_id;
        this.title = title;
    }

    public long getID(){
        return user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(long id) {
        this.user_id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
