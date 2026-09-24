package com.example.dashboardapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(DeviceItem item);
    }

    private List<DeviceItem> deviceList;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<DeviceItem> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    public void updateData(List<DeviceItem> newList) {
        this.deviceList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceItem item = deviceList.get(position);
        holder.title.setText(item.getName());
        holder.subtitle.setText(item.getStatus());

        // 1. PROFILE PICTURE / EMOJI LOADING
        if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
            // CRITICAL: Clear the XML tint so the photo isn't tinted solid purple!
            holder.icon.setImageTintList(null);

            // Expand icon to fill the circle container
            holder.icon.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.icon.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.icon.requestLayout();

            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_splash_logo)
                    .into(holder.icon);
        } else {
            // Fallback: reset icon size and use default drawable
            holder.icon.getLayoutParams().width = (int) (22 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            holder.icon.getLayoutParams().height = (int) (22 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            holder.icon.requestLayout();
            holder.icon.setImageResource(item.getIconResId());
        }

        // 2. STATUS DOT (Green if Online, Red if Offline, Gone if Vibe/Emote)
        if (item.getConnectionStatus() != null) {
            holder.statusDot.setVisibility(View.VISIBLE);

            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);

            if ("online".equalsIgnoreCase(item.getConnectionStatus())) {
                dot.setColor(Color.parseColor("#22C55E")); // Green
            } else {
                dot.setColor(Color.parseColor("#EF4444")); // Red
            }

            dot.setStroke(2, Color.WHITE);
            holder.statusDot.setBackground(dot);
        } else {
            // Hide the dot for vibes/emotes
            holder.statusDot.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceClick(item);
            }
        });

        // 3. BT STATUS (Blue if Online, Text is grayed out if offline, Gone if Vibe/Emote)
        // Shows if user is nearby the device using their phone(adjust which range is acceptable on the device)
        if (item.getBtStatus() != null) {
            holder.statusText.setVisibility(View.VISIBLE);

            // Create a pill/rounded rectangle badge
            GradientDrawable badge = new GradientDrawable();
            badge.setShape(GradientDrawable.RECTANGLE);
            badge.setCornerRadius(18f); // Rounded corners

            if ("active".equalsIgnoreCase(item.getBtStatus())) {
                holder.statusText.setTextColor(Color.parseColor("#2563EB")); // Blue text
                badge.setColor(Color.parseColor("#DBEAFE"));               // Light blue tint background
                holder.statusText.setText("Nearby");
            } else {
                holder.statusText.setTextColor(Color.parseColor("#6B7280")); // Gray text
                badge.setColor(Color.parseColor("#F3F4F6"));               // Light gray tint background
                holder.statusText.setText("Away");
            }

            holder.statusText.setPadding(16, 6, 16, 6);
            holder.statusText.setBackground(badge);
        } else {
            holder.statusText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return deviceList != null ? deviceList.size() : 0;
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, subtitle, statusText;
        View statusDot;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.itemIcon);
            title = itemView.findViewById(R.id.itemTitle);
            subtitle = itemView.findViewById(R.id.itemSubtitle);
            statusDot = itemView.findViewById(R.id.itemStatusDot); // CONNECTED STATUS DOT
            statusText = itemView.findViewById(R.id.bluetooth);
        }
    }
}