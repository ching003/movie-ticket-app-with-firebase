package com.example.firebase.dal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.firebase.entities.Showtime;

import java.util.List;

@Dao
public interface ShowtimeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Showtime showtime);

    @Update
    void update(Showtime showtime);

    @Delete
    void delete(Showtime showtime);

    @Query("SELECT * FROM showtimes WHERE id = :id")
    Showtime getById(int id);

    @Query("SELECT * FROM showtimes")
    List<Showtime> getAll();

    @Query("SELECT * FROM showtimes WHERE movie_id = :movieId ORDER BY show_date ASC, show_time ASC")
    List<Showtime> getByMovie(int movieId);

    @Query("SELECT * FROM showtimes WHERE theater_id = :theaterId ORDER BY show_date ASC, show_time ASC")
    List<Showtime> getByTheater(int theaterId);

    @Query("SELECT * FROM showtimes WHERE movie_id = :movieId AND theater_id = :theaterId ORDER BY show_date ASC, show_time ASC")
    List<Showtime> getByMovieAndTheater(int movieId, int theaterId);

    @Query("SELECT * FROM showtimes WHERE show_date = :date ORDER BY show_time ASC")
    List<Showtime> getByDate(String date);

    @Query("SELECT * FROM showtimes WHERE available_seats > 0 ORDER BY show_date ASC, show_time ASC")
    List<Showtime> getAvailable();

    @Query("UPDATE showtimes SET available_seats = :availableSeats WHERE id = :id")
    void updateAvailableSeats(int id, int availableSeats);

    @Query("DELETE FROM showtimes")
    void deleteAll();
}
