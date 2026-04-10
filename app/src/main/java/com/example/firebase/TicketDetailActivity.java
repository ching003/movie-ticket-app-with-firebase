package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebase.dal.AppDB;
import com.example.firebase.entities.Movie;
import com.example.firebase.entities.Showtime;
import com.example.firebase.entities.Theater;
import com.example.firebase.entities.Ticket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketDetailActivity extends AppCompatActivity {
    private AppDB db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        db = AppDB.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        int ticketId = getIntent().getIntExtra("ticket_id", -1);

        if (ticketId != -1) {
            loadTicketInfo(ticketId);
        }

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            Intent intent = new Intent(TicketDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadTicketInfo(int ticketId) {
        executorService.execute(() -> {
            Ticket ticket = db.ticketDAO().getById(ticketId);
            if (ticket != null) {
                Showtime showtime = db.showtimeDAO().getById(ticket.getShowtimeId());
                Movie movie = db.movieDAO().getById(showtime.getMovieId());
                Theater theater = db.theaterDAO().getById(showtime.getTheaterId());

                runOnUiThread(() -> {
                    ((TextView)findViewById(R.id.tvMovieTitle)).setText(movie.getTitle());
                    ((TextView)findViewById(R.id.tvTheaterName)).setText(theater.getName());
                    ((TextView)findViewById(R.id.tvShowTime)).setText(showtime.getShowDate() + " " + showtime.getShowTime());
                    ((TextView)findViewById(R.id.tvSeatNumber)).setText("Ghế: " + ticket.getSeatNumber());
                    ((TextView)findViewById(R.id.tvPrice)).setText(String.format("%,.0fđ", ticket.getPrice()));
                    ((TextView)findViewById(R.id.tvTicketId)).setText("Mã vé: #" + ticket.getId());
                });
            }
        });
    }
}