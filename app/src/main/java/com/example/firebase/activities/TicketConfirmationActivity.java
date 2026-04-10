package com.example.firebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.MainActivity;
import com.example.firebase.R;
import com.example.firebase.adapters.TicketsAdapter;
import com.example.firebase.LoginActivity;
import com.example.firebase.data.FirebaseRepository;
import com.example.firebase.entities.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketConfirmationActivity extends AppCompatActivity {

    private RecyclerView rvTickets;
    private TextView tvEmpty;
    private Button btnBackHome;
    private TicketsAdapter adapter;
    private FirebaseRepository repository;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_confirmation);

        repository = FirebaseRepository.getInstance();
        userUid = LoginActivity.getCurrentUserUid(this);

        initViews();
        loadTickets();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Vé đã đặt");

        rvTickets = findViewById(R.id.rvTickets);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBackHome = findViewById(R.id.btnBackHome);

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketsAdapter(this, new ArrayList<>());
        rvTickets.setAdapter(adapter);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadTickets() {
        repository.getTicketsByUser(userUid, new FirebaseRepository.SingleCallback<List<Ticket>>() {
            @Override
            public void onResult(List<Ticket> tickets) {
                if (tickets.isEmpty()) {
                    rvTickets.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvTickets.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.updateData(tickets);
                }
            }

            @Override
            public void onError(String message) {
                rvTickets.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Khong tai duoc danh sach ve");
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
