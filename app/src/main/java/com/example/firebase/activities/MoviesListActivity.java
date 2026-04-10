package com.example.firebase.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.R;
import com.example.firebase.adapters.MoviesAdapter;
import com.example.firebase.data.FirebaseRepository;
import com.example.firebase.entities.Movie;

import java.util.ArrayList;
import java.util.List;

public class MoviesListActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private TextView tvEmpty;
    private MoviesAdapter adapter;
    private FirebaseRepository repository;
    private int theaterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);

        repository = FirebaseRepository.getInstance();

        theaterId = getIntent().getIntExtra("theaterId", -1);
        String theaterName = getIntent().getStringExtra("theaterName");

        initViews(theaterName);
        loadMovies();
    }

    private void initViews(String theaterName) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(theaterName);

        rvMovies = findViewById(R.id.rvMovies);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoviesAdapter(this, new ArrayList<>(), theaterId);
        rvMovies.setAdapter(adapter);
    }

    private void loadMovies() {
        repository.getMoviesByTheater(theaterId, new FirebaseRepository.SingleCallback<List<Movie>>() {
            @Override
            public void onResult(List<Movie> movies) {
                if (movies.isEmpty()) {
                    rvMovies.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvMovies.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.updateData(movies);
                }
            }

            @Override
            public void onError(String message) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Khong tai duoc danh sach phim");
                rvMovies.setVisibility(View.GONE);
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
