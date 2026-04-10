package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.dal.AppDB;
import com.example.firebase.entities.Movie;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private AppDB db;
    private int theaterId;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        theaterId = getIntent().getIntExtra("theater_id", -1);
        db = AppDB.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadMovies();
    }

    private void loadMovies() {
        executorService.execute(() -> {
            // Hiển thị tất cả phim (hoặc lọc theo rạp nếu có bảng liên kết, 
            // ở đây ta lấy toàn bộ phim để đơn giản hóa theo database hiện tại)
            List<Movie> list = db.movieDAO().getAll();

            runOnUiThread(() -> {
                adapter = new MovieAdapter(list, movie -> {
                    Intent intent = new Intent(MovieActivity.this, ShowtimeActivity.class);
                    intent.putExtra("movie_id", movie.getId());
                    intent.putExtra("theater_id", theaterId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }
}