package com.example.firebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.firebase.LoginActivity;
import com.example.firebase.R;
import com.example.firebase.data.FirebaseRepository;
import com.example.firebase.entities.Movie;
import com.example.firebase.entities.Showtime;
import com.example.firebase.entities.Ticket;
import com.example.firebase.entities.Theater;
import com.example.firebase.notifications.ReminderScheduler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeatSelectionActivity extends AppCompatActivity {

    private GridLayout gridSeats;
    private TextView tvTotal, tvMovieInfo, tvShowtimeInfo;
    private Button btnConfirm;
    private FirebaseRepository repository;
    private int showtimeId;
    private Showtime showtime;
    private Movie movie;
    private Theater theater;
    private List<String> bookedSeats = new ArrayList<>();
    private List<String> selectedSeats = new ArrayList<>();
    private static final int ROWS = 10;
    private static final int COLS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        repository = FirebaseRepository.getInstance();

        showtimeId = getIntent().getIntExtra("showtimeId", -1);

        initViews();
        loadShowtimeData();
        loadBookedSeats();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chọn ghế");

        gridSeats = findViewById(R.id.gridSeats);
        tvTotal = findViewById(R.id.tvTotal);
        tvMovieInfo = findViewById(R.id.tvMovieInfo);
        tvShowtimeInfo = findViewById(R.id.tvShowtimeInfo);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void loadShowtimeData() {
        repository.getShowtimeById(showtimeId, new FirebaseRepository.SingleCallback<Showtime>() {
            @Override
            public void onResult(Showtime data) {
                showtime = data;
                repository.getMovieById(showtime.getMovieId(), new FirebaseRepository.SingleCallback<Movie>() {
                    @Override
                    public void onResult(Movie movieData) {
                        movie = movieData;
                        tvMovieInfo.setText(movie.getTitle());
                        tvShowtimeInfo.setText(showtime.getShowDate() + " - " + showtime.getShowTime() + " - Phong " + showtime.getScreenNumber());
                    }

                    @Override
                    public void onError(String message) { }
                });
                repository.getTheaters(new FirebaseRepository.SingleCallback<List<Theater>>() {
                    @Override
                    public void onResult(List<Theater> data) {
                        for (Theater t : data) {
                            if (t.getId() == showtime.getTheaterId()) {
                                theater = t;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onError(String message) { }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SeatSelectionActivity.this, "Khong tai duoc suat chieu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookedSeats() {
        repository.getBookedSeats(showtimeId, new FirebaseRepository.SingleCallback<List<String>>() {
            @Override
            public void onResult(List<String> data) {
                bookedSeats = data;
                createSeatLayout();
            }

            @Override
            public void onError(String message) {
                createSeatLayout();
            }
        });
    }

    private void createSeatLayout() {
        gridSeats.setColumnCount(COLS);
        gridSeats.removeAllViews();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String seatNumber = (char) ('A' + row) + String.valueOf(col + 1);
                Button seatButton = new Button(this);
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(col, 1f);
                params.setMargins(4, 4, 4, 4);
                seatButton.setLayoutParams(params);
                
                seatButton.setText(seatNumber);
                seatButton.setTextSize(10);
                seatButton.setPadding(8, 16, 8, 16);

                if (bookedSeats.contains(seatNumber)) {
                    seatButton.setBackgroundResource(R.drawable.seat_btn_booked);
                    seatButton.setTextColor(ContextCompat.getColor(this, R.color.on_primary));
                    seatButton.setEnabled(false);
                } else {
                    seatButton.setBackgroundResource(R.drawable.seat_btn_available);
                    seatButton.setTextColor(ContextCompat.getColor(this, R.color.on_primary));
                    seatButton.setOnClickListener(v -> toggleSeatSelection(seatButton, seatNumber));
                }

                gridSeats.addView(seatButton);
            }
        }
    }

    private void toggleSeatSelection(Button button, String seatNumber) {
        if (selectedSeats.contains(seatNumber)) {
            selectedSeats.remove(seatNumber);
            button.setBackgroundResource(R.drawable.seat_btn_available);
        } else {
            selectedSeats.add(seatNumber);
            button.setBackgroundResource(R.drawable.seat_btn_selected);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = selectedSeats.size() * showtime.getPrice();
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotal.setText("Tổng tiền: " + format.format(total));
    }

    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = LoginActivity.getCurrentUserUid(this);
        if (uid.isEmpty() || showtime == null || movie == null) {
            Toast.makeText(this, "Thong tin dat ve chua san sang", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.createTickets(uid, showtime, movie.getTitle(), theater == null ? "" : theater.getName(), selectedSeats, new FirebaseRepository.SingleCallback<List<Ticket>>() {
            @Override
            public void onResult(List<Ticket> tickets) {
                for (Ticket ticket : tickets) {
                    ReminderScheduler.schedule(SeatSelectionActivity.this, ticket.getId(), movie.getTitle(), showtime.getShowDate(), showtime.getShowTime());
                }
                Toast.makeText(SeatSelectionActivity.this, "Dat ve thanh cong!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SeatSelectionActivity.this, TicketConfirmationActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SeatSelectionActivity.this, "Dat ve that bai: " + message, Toast.LENGTH_SHORT).show();
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
