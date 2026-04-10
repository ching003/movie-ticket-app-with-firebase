package com.example.firebase.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.LoginActivity;
import com.example.firebase.R;
import com.example.firebase.activities.SeatSelectionActivity;
import com.example.firebase.entities.Showtime;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ShowtimesAdapter extends RecyclerView.Adapter<ShowtimesAdapter.ViewHolder> {

    private Context context;
    private List<Showtime> showtimes;

    public ShowtimesAdapter(Context context, List<Showtime> showtimes) {
        this.context = context;
        this.showtimes = showtimes;
    }

    public void updateData(List<Showtime> newShowtimes) {
        this.showtimes = newShowtimes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_showtime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Showtime showtime = showtimes.get(position);

        holder.tvDate.setText(showtime.getShowDate());
        holder.tvTime.setText(showtime.getShowTime());
        holder.tvScreen.setText("Phòng " + showtime.getScreenNumber());
        holder.tvSeats.setText(showtime.getAvailableSeats() + "/" + showtime.getTotalSeats() + " ghế");
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(format.format(showtime.getPrice()));

        holder.itemView.setOnClickListener(v -> {
            String userUid = LoginActivity.getCurrentUserUid(context);
            if (userUid.isEmpty()) {
                android.widget.Toast.makeText(context, "Vui lòng đăng nhập để đặt vé", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, SeatSelectionActivity.class);
            intent.putExtra("showtimeId", showtime.getId());
            context.startActivity(intent);
        });

        if (showtime.getAvailableSeats() == 0) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvScreen, tvSeats, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvScreen = itemView.findViewById(R.id.tvScreen);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
