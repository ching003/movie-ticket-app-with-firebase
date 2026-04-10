package com.example.firebase.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.R;
import com.example.firebase.activities.MoviesListActivity;
import com.example.firebase.entities.Theater;

import java.util.List;

public class TheatersAdapter extends RecyclerView.Adapter<TheatersAdapter.ViewHolder> {

    private Context context;
    private List<Theater> theaters;

    public TheatersAdapter(Context context, List<Theater> theaters) {
        this.context = context;
        this.theaters = theaters;
    }

    public void updateData(List<Theater> newTheaters) {
        this.theaters = newTheaters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_theater, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Theater theater = theaters.get(position);

        holder.tvName.setText(theater.getName());
        holder.tvLocation.setText(theater.getLocation());
        holder.tvAddress.setText(theater.getAddress());
        holder.tvPhone.setText("📞 " + theater.getPhone());
        holder.tvScreens.setText(theater.getTotalScreens() + " phòng chiếu");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoviesListActivity.class);
            intent.putExtra("theaterId", theater.getId());
            intent.putExtra("theaterName", theater.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return theaters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvAddress, tvPhone, tvScreens;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvScreens = itemView.findViewById(R.id.tvScreens);
        }
    }
}
