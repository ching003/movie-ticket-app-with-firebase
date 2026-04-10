package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase.dal.AppDB;
import com.example.firebase.entities.Showtime;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ShowtimeActivity extends AppCompatActivity {

    ListView listView;
    AppDB db;
    List<Showtime> showtimeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);

        listView = findViewById(R.id.listViewShowtime);
        db = AppDB.getInstance(this);

        int movieId = getIntent().getIntExtra("movie_id", -1);
        int theaterId = getIntent().getIntExtra("theater_id", -1);

        new Thread(() -> {
            if (movieId != -1 && theaterId != -1) {
                showtimeList = db.showtimeDAO().getByMovieAndTheater(movieId, theaterId);
            } else if (movieId != -1) {
                showtimeList = db.showtimeDAO().getByMovie(movieId);
            } else {
                showtimeList = db.showtimeDAO().getAll();
            }

            runOnUiThread(() -> {
                listView.setAdapter(new ArrayAdapter<Showtime>(this, 0, showtimeList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.item_showtime, parent, false);
                        }
                        Showtime s = getItem(position);
                        TextView tvTime = convertView.findViewById(R.id.tvTime);
                        TextView tvScreen = convertView.findViewById(R.id.tvScreen);
                        TextView tvDate = convertView.findViewById(R.id.tvDate);
                        TextView tvSeats = convertView.findViewById(R.id.tvSeats);
                        TextView tvPrice = convertView.findViewById(R.id.tvPrice);

                        tvTime.setText(s.getShowTime());
                        tvDate.setText(s.getShowDate());
                        tvScreen.setText("Phòng " + s.getScreenNumber());
                        tvSeats.setText(s.getAvailableSeats() + "/" + s.getTotalSeats() + " ghế");
                        tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(s.getPrice()));

                        return convertView;
                    }
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Showtime s = showtimeList.get(position);
                    Intent intent = new Intent(ShowtimeActivity.this, SeatActivity.class);
                    intent.putExtra("showtime_id", s.getId());
                    startActivity(intent);
                });
            });
        }).start();
    }
}