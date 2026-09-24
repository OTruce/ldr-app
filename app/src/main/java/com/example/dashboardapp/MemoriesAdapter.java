package com.example.dashboardapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MemoriesAdapter extends RecyclerView.Adapter<MemoriesAdapter.MemoryViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(MemoryItem item);
    }

    private final List<MemoryItem> items;
    private final OnMediaClickListener listener;

    public MemoriesAdapter(List<MemoryItem> items, OnMediaClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory, parent, false);
        return new MemoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        MemoryItem item = items.get(position);

        holder.textAuthor.setText(item.getSenderName());
        holder.textDate.setText(item.getCreatedAt());
        holder.textCaption.setText(item.getCaption() != null ? item.getCaption() : "");

        // Video vs Image thumbnail
        if ("VIDEO".equalsIgnoreCase(item.getMediaType())) {
            holder.iconPlayVideo.setVisibility(View.VISIBLE);
        } else {
            holder.iconPlayVideo.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getMediaUrl())
                .centerCrop()
                .into(holder.imgThumbnail);

        // PRIVACY RULE: ONLY Owner can view reactions!
        if (item.isOwner() && item.getReactions() != null && !item.getReactions().isEmpty()) {
            StringBuilder sb = new StringBuilder("Reactions:\n");
            for (MemoryItem.ReactionDetail rx : item.getReactions()) {
                sb.append(rx.getReactorName()).append(": ").append(rx.getReaction()).append("  ");
            }
            holder.textOwnerReactions.setVisibility(View.VISIBLE);
            holder.textOwnerReactions.setText(sb.toString());
        } else {
            holder.textOwnerReactions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onMediaClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, iconPlayVideo;
        TextView textAuthor, textDate, textCaption, textOwnerReactions;

        MemoryViewHolder(@NonNull View v) {
            super(v);
            imgThumbnail = v.findViewById(R.id.imgThumbnail);
            iconPlayVideo = v.findViewById(R.id.iconPlayVideo);
            textAuthor = v.findViewById(R.id.textAuthor);
            textDate = v.findViewById(R.id.textDate);
            textCaption = v.findViewById(R.id.textCaption);
            textOwnerReactions = v.findViewById(R.id.textOwnerReactions);
        }
    }
}