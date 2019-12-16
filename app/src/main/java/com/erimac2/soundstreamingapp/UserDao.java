package com.erimac2.soundstreamingapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(UserDB userDB);

    @Update
    int update(UserDB userDB);

    @Query("DELETE FROM UserDB")
    void deleteAll();

    @Delete
    void delete(UserDB userDB);

    @Query("SELECT * FROM userDB WHERE user_id = :id ")
    UserDB get(long id);

    @Query("SELECT * FROM UserDB")
    List<UserDB> getAllUsers();

    @RawQuery
    Boolean insertDataRawFormat(SupportSQLiteQuery query);
}
