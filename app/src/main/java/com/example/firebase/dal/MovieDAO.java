package com.example.firebase.dal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.firebase.entities.Movie;

import java.util.List;

@Dao
public interface MovieDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Movie movie);

    @Update
    void update(Movie movie);

    @Delete
    void delete(Movie movie);

    @Query("SELECT * FROM movies WHERE id = :id")
    Movie getById(int id);

    @Query("SELECT * FROM movies ORDER BY title ASC")
    List<Movie> getAll();

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :keyword || '%' OR genre LIKE '%' || :keyword || '%'")
    List<Movie> search(String keyword);

    @Query("SELECT * FROM movies WHERE genre = :genre ORDER BY title ASC")
    List<Movie> getByGenre(String genre);

    @Query("SELECT * FROM movies ORDER BY rating DESC LIMIT 10")
    List<Movie> getTopRated();

    @Query("DELETE FROM movies")
    void deleteAll();
}
