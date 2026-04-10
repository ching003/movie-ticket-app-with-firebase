package com.example.firebase.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase.R;
import com.example.firebase.entities.Ticket;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.ViewHolder> {

    private Context context;
    private List<Ticket> tickets;

    public TicketsAdapter(Context context, List<Ticket> tickets) {
        this.context = context;
        this.tickets = tickets;
    }

    public void updateData(List<Ticket> newTickets) {
        this.tickets = newTickets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);

        holder.tvSeat.setText("Ghế: " + ticket.getSeatNumber());
        holder.tvStatus.setText(ticket.getStatus());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvBookingDate.setText(dateFormat.format(new Date(ticket.getBookingDate())));
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(format.format(ticket.getPrice()));

        holder.tvMovieTitle.setText(ticket.getMovieTitle() == null ? "Movie" : ticket.getMovieTitle());
        holder.tvShowtimeInfo.setText(ticket.getShowDate() + " - " + ticket.getShowTime() + " - Phong " + ticket.getScreenNumber());
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvShowtimeInfo, tvSeat, tvBookingDate, tvPrice, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvShowtimeInfo = itemView.findViewById(R.id.tvShowtimeInfo);
            tvSeat = itemView.findViewById(R.id.tvSeat);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
