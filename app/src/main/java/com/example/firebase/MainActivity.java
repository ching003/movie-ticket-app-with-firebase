package com.example.firebase;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.adapters.TheatersAdapter;
import com.example.firebase.activities.TicketConfirmationActivity;
import com.example.firebase.data.FirebaseRepository;
import com.example.firebase.entities.Theater;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvTheaters;
    private TextView tvEmpty;
    private TheatersAdapter adapter;
    private FirebaseRepository repository;
    private ValueEventListener theatersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        repository = FirebaseRepository.getInstance();
        askNotificationPermission();

        initViews();
        repository.seedDataIfNeeded(() -> runOnUiThread(this::loadTheatersOnce));
        loadTheatersOnce();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (theatersListener == null) {
            theatersListener = repository.addTheatersValueEventListener(new FirebaseRepository.SingleCallback<List<Theater>>() {
                @Override
                public void onResult(List<Theater> theaters) {
                    runOnUiThread(() -> applyTheaters(theaters));
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Khong tai duoc du lieu rap: " + message);
                        rvTheaters.setVisibility(View.GONE);
                    });
                }
            });
            if (theatersListener == null) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Firebase chua san sang. Kiem tra google-services.json va khoi dong lai app.");
                rvTheaters.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStop() {
        repository.removeTheatersListener(theatersListener);
        theatersListener = null;
        super.onStop();
    }

    private void askNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chon rap chieu");
        
        rvTheaters = findViewById(R.id.rvTheaters);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        rvTheaters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TheatersAdapter(this, new ArrayList<>());
        rvTheaters.setAdapter(adapter);
        tvEmpty.setVisibility(View.VISIBLE);
        rvTheaters.setVisibility(View.GONE);
        tvEmpty.setText("Dang tai danh sach rap...");
    }

    private void loadTheatersOnce() {
        repository.getTheaters(new FirebaseRepository.SingleCallback<List<Theater>>() {
            @Override
            public void onResult(List<Theater> theaters) {
                runOnUiThread(() -> applyTheaters(theaters));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Khong tai duoc du lieu rap: " + message);
                    rvTheaters.setVisibility(View.GONE);
                });
            }
        });
    }

    private void applyTheaters(List<Theater> theaters) {
        if (theaters == null || theaters.isEmpty()) {
            rvTheaters.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Chua co du lieu rap.\n\n1) Firebase Console > Realtime Database: copy URL vao strings.xml (firebase_database_url).\n\n2) Rules: cho phep doc/ghi khi dang nhap (auth != null) hoac tam thoi .read/.write true khi test.");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTheaters.setVisibility(View.VISIBLE);
            adapter.updateData(theaters);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            LoginActivity.logout(this);
            return true;
        }
        if (item.getItemId() == R.id.action_my_tickets) {
            startActivity(new android.content.Intent(this, TicketConfirmationActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}