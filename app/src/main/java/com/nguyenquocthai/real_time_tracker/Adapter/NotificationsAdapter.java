package com.nguyenquocthai.real_time_tracker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nguyenquocthai.real_time_tracker.Model.NotificationItem;
import com.nguyenquocthai.real_time_tracker.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<NotificationItem> notificationItems;

    public NotificationsAdapter(List<NotificationItem> notificationItems) {
        this.notificationItems = notificationItems;
    }

    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_layout, parent, false);
        return new ViewHolder(view);
    }
    private String convertTimestampToTimeString(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int position) {
        final NotificationItem item = notificationItems.get(position);
        holder.textView.setText(item.getMessage());
        holder.timeView.setText(convertTimestampToTimeString(item.getTimestamp()));
        Picasso.get().load(item.getAvatar()).into(holder.iconView);
        holder.itemView.setOnClickListener(v -> {
            // Sử dụng getAdapterPosition() để đảm bảo đúng vị trí item hiện tại
            int currentPosition = holder.getAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onNotificationItemClick(notificationItems.get(currentPosition));
            }
        });
    }
    public void clearNotifications() {
        notificationItems.clear();
        notifyDataSetChanged();
    }

    public interface OnNotificationItemClickListener {
        void onNotificationItemClick(NotificationItem item);
    }

    private OnNotificationItemClickListener listener;

    // Setter method for the listener
    public void setOnNotificationItemClickListener(OnNotificationItemClickListener listener) {
        this.listener = listener;
    }
    @Override
    public int getItemCount() {
        return notificationItems.size();
    }
    public List<NotificationItem> getNotificationItems() {
        return notificationItems;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView iconView;
        public TextView textView;
        public TextView timeView;

        public ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon);
            textView = itemView.findViewById(R.id.notification_text);
            textView.setSelected(true);
            timeView = itemView.findViewById(R.id.notification_time);
        }
    }

}
