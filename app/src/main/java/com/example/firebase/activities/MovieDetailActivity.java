package com.example.firebase.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebase.R;
import com.example.firebase.adapters.ShowtimesAdapter;
import com.example.firebase.data.FirebaseRepository;
import com.example.firebase.entities.Movie;
import com.example.firebase.entities.Showtime;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvGenre, tvDuration, tvRating, tvDescription, tvEmpty;
    private RecyclerView rvShowtimes;
    private ShowtimesAdapter adapter;
    private FirebaseRepository repository;
    private int movieId, theaterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        repository = FirebaseRepository.getInstance();

        movieId = getIntent().getIntExtra("movieId", -1);
        theaterId = getIntent().getIntExtra("theaterId", -1);

        initViews();
        loadMovieDetails();
        loadShowtimes();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chi tiết phim");

        ivPoster = findViewById(R.id.ivPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvGenre = findViewById(R.id.tvGenre);
        tvDuration = findViewById(R.id.tvDuration);
        tvRating = findViewById(R.id.tvRating);
        tvDescription = findViewById(R.id.tvDescription);
        rvShowtimes = findViewById(R.id.rvShowtimes);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowtimesAdapter(this, new ArrayList<>());
        rvShowtimes.setAdapter(adapter);
    }

    private void loadMovieDetails() {
        repository.getMovieById(movieId, new FirebaseRepository.SingleCallback<Movie>() {
            @Override
            public void onResult(Movie movie) {
                if (movie != null) {
                    tvTitle.setText(movie.getTitle());
                    tvGenre.setText(movie.getGenre() + " • " + movie.getReleaseYear());
                    tvDuration.setText(movie.getDuration() + " phút");
                    tvRating.setText("⭐ " + movie.getRating());
                    tvDescription.setText(movie.getDescription());

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    Glide.with(MovieDetailActivity.this)
                            .load(movie.getPosterUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(ivPoster);
                }
            }

            @Override
            public void onError(String message) { }
        });
    }

    private void loadShowtimes() {
        repository.getShowtimesByMovieAndTheater(movieId, theaterId, new FirebaseRepository.SingleCallback<List<Showtime>>() {
            @Override
            public void onResult(List<Showtime> showtimes) {
                if (showtimes.isEmpty()) {
                    rvShowtimes.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvShowtimes.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.updateData(showtimes);
                }
            }

            @Override
            public void onError(String message) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Khong tai duoc suat chieu");
                rvShowtimes.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
