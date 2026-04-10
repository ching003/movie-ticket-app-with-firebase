package com.example.firebase.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "showtimes",
    foreignKeys = {
        @ForeignKey(
            entity = Movie.class,
            parentColumns = "id",
            childColumns = "movie_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Theater.class,
            parentColumns = "id",
            childColumns = "theater_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "movie_id"),
        @Index(value = "theater_id")
    }
)
public class Showtime {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    @ColumnInfo(name = "theater_id")
    private int theaterId;

    @ColumnInfo(name = "show_date")
    private String showDate; // Format: dd/MM/yyyy

    @ColumnInfo(name = "show_time")
    private String showTime; // Format: HH:mm

    @ColumnInfo(name = "screen_number")
    private int screenNumber; // Phòng chiếu số

    @ColumnInfo(name = "total_seats")
    private int totalSeats;

    @ColumnInfo(name = "available_seats")
    private int availableSeats;

    @ColumnInfo(name = "price")
    private double price;

    public Showtime() {}

    public Showtime(int movieId, int theaterId, String showDate, String showTime, int screenNumber, int totalSeats, int availableSeats, double price) {
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.screenNumber = screenNumber;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getTheaterId() { return theaterId; }
    public void setTheaterId(int theaterId) { this.theaterId = theaterId; }

    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public int getScreenNumber() { return screenNumber; }
    public void setScreenNumber(int screenNumber) { this.screenNumber = screenNumber; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
