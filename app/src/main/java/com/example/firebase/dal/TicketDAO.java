package com.example.firebase.dal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.firebase.entities.Ticket;

import java.util.List;

@Dao
public interface TicketDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Ticket ticket);

    @Update
    void update(Ticket ticket);

    @Delete
    void delete(Ticket ticket);

    @Query("SELECT * FROM tickets WHERE id = :id")
    Ticket getById(int id);

    @Query("SELECT * FROM tickets")
    List<Ticket> getAll();

    @Query("SELECT * FROM tickets WHERE user_id = :userId ORDER BY booking_date DESC")
    List<Ticket> getByUser(int userId);

    @Query("SELECT * FROM tickets WHERE showtime_id = :showtimeId")
    List<Ticket> getByShowtime(int showtimeId);

    @Query("SELECT * FROM tickets WHERE user_id = :userId AND status = :status ORDER BY booking_date DESC")
    List<Ticket> getByUserAndStatus(int userId, String status);

    @Query("SELECT seat_number FROM tickets WHERE showtime_id = :showtimeId AND status != 'CANCELLED'")
    List<String> getBookedSeats(int showtimeId);

    @Query("UPDATE tickets SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);

    @Query("DELETE FROM tickets")
    void deleteAll();
}
