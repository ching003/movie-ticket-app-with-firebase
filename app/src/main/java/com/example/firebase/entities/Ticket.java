package com.example.firebase.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "tickets",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Showtime.class,
            parentColumns = "id",
            childColumns = "showtime_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "user_id"),
        @Index(value = "showtime_id")
    }
)
public class Ticket {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "showtime_id")
    private int showtimeId;

    @ColumnInfo(name = "seat_number")
    private String seatNumber; // Ví dụ: A1, B5, C10

    @ColumnInfo(name = "booking_date")
    private long bookingDate; // Timestamp

    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "status")
    private String status; // "BOOKED", "CANCELLED", "USED"

    @Ignore
    private String userUid;

    @Ignore
    private String movieTitle;

    @Ignore
    private String theaterName;

    @Ignore
    private String showDate;

    @Ignore
    private String showTime;

    @Ignore
    private int screenNumber;

    public Ticket() {}

    public Ticket(int userId, int showtimeId, String seatNumber, long bookingDate, double price, String status) {
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public long getBookingDate() { return bookingDate; }
    public void setBookingDate(long bookingDate) { this.bookingDate = bookingDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public int getScreenNumber() { return screenNumber; }
    public void setScreenNumber(int screenNumber) { this.screenNumber = screenNumber; }
}
