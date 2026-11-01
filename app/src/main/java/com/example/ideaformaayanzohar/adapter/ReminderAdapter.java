package com.example.ideaformaayanzohar.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideaformaayanzohar.R;
import com.example.ideaformaayanzohar.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnReminderActionListener {
        void onToggleCompleted(Reminder reminder, boolean isChecked);
        void onDeleteReminder(Reminder reminder);
        void onItemClick(Reminder reminder);
    }

    private List<Reminder> reminderList = new ArrayList<>();
    private final OnReminderActionListener listener;

    public ReminderAdapter(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void setReminderList(List<Reminder> list) {
        this.reminderList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        holder.tvTitle.setText(reminder.getTitle());
        holder.tvContent.setText(reminder.getContent());
        holder.tvCategory.setText(reminder.getCategory() != null ? reminder.getCategory() : "כללי");
        
        if (reminder.getLocationName() != null && !reminder.getLocationName().isEmpty()) {
            holder.tvLocation.setText(reminder.getLocationName());
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }

        // Format Date
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm | dd/MM", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(reminder.getDateTime())));

        // Priority Badge
        switch (reminder.getPriority()) {
            case 3:
                holder.tvPriority.setText("גבוהה");
                holder.tvPriority.setBackgroundColor(Color.parseColor("#FFEBEE"));
                holder.tvPriority.setTextColor(Color.parseColor("#D32F2F"));
                break;
            case 2:
                holder.tvPriority.setText("בינונית");
                holder.tvPriority.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.tvPriority.setTextColor(Color.parseColor("#E65100"));
                break;
            default:
                holder.tvPriority.setText("רגילה");
                holder.tvPriority.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.tvPriority.setTextColor(Color.parseColor("#2E7D32"));
                break;
        }

        // Checkbox & Strikethrough
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(reminder.isCompleted());
        updateStrikethrough(holder, reminder.isCompleted());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setCompleted(isChecked);
            updateStrikethrough(holder, isChecked);
            if (listener != null) {
                listener.onToggleCompleted(reminder, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteReminder(reminder);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(reminder);
            }
        });
    }

    private void updateStrikethrough(ReminderViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.GRAY);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(Color.parseColor("#1E1E1E"));
        }
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTitle, tvContent, tvCategory, tvLocation, tvDate, tvPriority;
        ImageButton btnDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
