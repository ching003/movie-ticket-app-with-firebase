package com.example.firebase.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movies")
public class Movie {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "genre")
    private String genre;

    @ColumnInfo(name = "duration")
    private int duration; // Thời lượng phim (phút)

    @ColumnInfo(name = "poster_url")
    private String posterUrl;

    @ColumnInfo(name = "rating")
    private float rating; // Đánh giá phim (0-10)

    @ColumnInfo(name = "release_year")
    private int releaseYear;

    public Movie() {}

    public Movie(String title, String description, String genre, int duration, String posterUrl, float rating, int releaseYear) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.releaseYear = releaseYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
}
