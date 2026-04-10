package com.example.firebase.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebase.R;
import com.example.firebase.activities.MovieDetailActivity;
import com.example.firebase.entities.Movie;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    private Context context;
    private List<Movie> movies;
    private int theaterId;

    public MoviesAdapter(Context context, List<Movie> movies, int theaterId) {
        this.context = context;
        this.movies = movies;
        this.theaterId = theaterId;
    }

    public void updateData(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());
        holder.tvDuration.setText(movie.getDuration() + " phút");
        holder.tvRating.setText("⭐ " + movie.getRating());

        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("theaterId", theaterId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvGenre, tvDuration, tvRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
