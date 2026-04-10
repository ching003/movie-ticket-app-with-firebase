package com.example.firebase.dal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.firebase.entities.Theater;

import java.util.List;

@Dao
public interface TheaterDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Theater theater);

    @Update
    void update(Theater theater);

    @Delete
    void delete(Theater theater);

    @Query("SELECT * FROM theaters WHERE id = :id")
    Theater getById(int id);

    @Query("SELECT * FROM theaters ORDER BY name ASC")
    List<Theater> getAll();

    @Query("SELECT * FROM theaters WHERE name LIKE '%' || :keyword || '%' OR location LIKE '%' || :keyword || '%'")
    List<Theater> search(String keyword);

    @Query("SELECT * FROM theaters WHERE location = :location ORDER BY name ASC")
    List<Theater> getByLocation(String location);

    @Query("DELETE FROM theaters")
    void deleteAll();
}
