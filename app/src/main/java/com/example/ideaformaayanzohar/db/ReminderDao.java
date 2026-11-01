package com.example.ideaformaayanzohar.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ideaformaayanzohar.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, priority DESC, dateTime ASC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE category = :category ORDER BY isCompleted ASC, priority DESC")
    List<Reminder> getRemindersByCategory(String category);

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY priority DESC, dateTime ASC")
    List<Reminder> getActiveReminders();

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getReminderById(int id);
}
