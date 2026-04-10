package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase.dal.AppDB;
import com.example.firebase.entities.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeatActivity extends AppCompatActivity {

    private AppDB db;
    private int showtimeId;
    private String selectedSeat = "";
    private double seatPrice = 75000; // Giá mặc định
    private TextView txtSelected;
    private Button btnBook;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        db = AppDB.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        showtimeId = getIntent().getIntExtra("showtime_id", -1);

        txtSelected = findViewById(R.id.txtSelected);
        btnBook = findViewById(R.id.btnBook);

        setupSeatClickListeners();
        markBookedSeats();

        btnBook.setOnClickListener(v -> handleBooking());
    }

    private void setupSeatClickListeners() {
        int[] seatIds = {
                R.id.seatA1, R.id.seatA2, R.id.seatA3, R.id.seatA4, R.id.seatA5, R.id.seatA6, R.id.seatA7, R.id.seatA8,
                R.id.seatB1, R.id.seatB2, R.id.seatB3, R.id.seatB4, R.id.seatB5, R.id.seatB6, R.id.seatB7, R.id.seatB8,
                R.id.seatC1, R.id.seatC2, R.id.seatC3, R.id.seatC4, R.id.seatC5, R.id.seatC6, R.id.seatC7, R.id.seatC8,
                R.id.seatD1, R.id.seatD2, R.id.seatD3, R.id.seatD4, R.id.seatD5, R.id.seatD6, R.id.seatD7, R.id.seatD8,
                R.id.seatE1, R.id.seatE2, R.id.seatE3, R.id.seatE4, R.id.seatE5, R.id.seatE6, R.id.seatE7, R.id.seatE8,
                R.id.seatF1, R.id.seatF2, R.id.seatF3, R.id.seatF4, R.id.seatF5, R.id.seatF6, R.id.seatF7, R.id.seatF8
        };

        View.OnClickListener listener = v -> {
            TextView seat = (TextView) v;
            String seatNum = seat.getText().toString();

            // Nếu click lại vào ghế đang chọn -> bỏ chọn
            if (selectedSeat.equals(seatNum)) {
                selectedSeat = "";
                seat.setBackgroundResource(R.drawable.bg_seat_available);
                txtSelected.setText("0đ");
            } else {
                // Bỏ chọn ghế cũ (nếu có)
                if (!selectedSeat.isEmpty()) {
                    int oldId = getResources().getIdentifier("seat" + selectedSeat, "id", getPackageName());
                    if (oldId != 0) findViewById(oldId).setBackgroundResource(R.drawable.bg_seat_available);
                }

                // Chọn ghế mới
                selectedSeat = seatNum;
                seat.setBackgroundResource(R.drawable.bg_seat_selected);
                txtSelected.setText(String.format("%,.0fđ", seatPrice));
            }
        };

        for (int id : seatIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void markBookedSeats() {
        executorService.execute(() -> {
            List<String> bookedSeats = db.ticketDAO().getBookedSeats(showtimeId);
            runOnUiThread(() -> {
                for (String seatNum : bookedSeats) {
                    int resId = getResources().getIdentifier("seat" + seatNum, "id", getPackageName());
                    if (resId != 0) {
                        View v = findViewById(resId);
                        v.setBackgroundResource(R.drawable.bg_seat_booked);
                        v.setClickable(false);
                    }
                }
            });
        });
    }

    private void handleBooking() {
        if (selectedSeat.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = LoginActivity.getCurrentUserId(this);
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Ticket ticket = new Ticket(
                    userId,
                    showtimeId,
                    selectedSeat,
                    System.currentTimeMillis(),
                    seatPrice,
                    "BOOKED"
            );
            long id = db.ticketDAO().insert(ticket);

            runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SeatActivity.this, TicketDetailActivity.class);
                    intent.putExtra("ticket_id", (int)id);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Đặt vé thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}